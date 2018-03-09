/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.magento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magento.CatalogCategoryEntity;
import magento.CatalogCategoryInfo;
import magento.CatalogCategoryTree;
import magento.CatalogProductEntity;
import magento.CatalogProductImageEntity;
import magento.CatalogProductImageEntityArray;
import magento.CatalogProductRelationEntity;
import magento.CatalogProductReturnEntity;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.product.catalog.CatalogWorker;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;

public class CatalogServices {
    public static final String module = CatalogServices.class.getName();
    public static final String resource = "MagentoUiLabels";

    public static Map<String, Object> getCategoryTree(DispatchContext dctx, Map<String, ?> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> serviceInMap = new HashMap<String, Object>();
        Map<String, Object> serviceOutMap = new HashMap<String, Object>();
        try {
            List<GenericValue> productStoreList = delegator.findList("MagentoProductStore", null, null, null, null, false);
            for (GenericValue productStore : productStoreList) {
                String productStoreId = productStore.getString("productStoreId");
                EntityCondition cond = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition(UtilMisc.toMap("productStoreId", productStoreId)),
                    EntityCondition.makeConditionDate("fromDate", "thruDate")));
                List<GenericValue> productStoreCatalogs = delegator.findList("ProductStoreCatalog", cond, null, null, null, false);
                GenericValue productStoreCatalog = productStoreCatalogs.get(0);
                String prodCatalogId = productStoreCatalog.getString("prodCatalogId");
                List<GenericValue> prodCatalogCategories = CatalogWorker.getProdCatalogCategories(delegator, prodCatalogId, "PCCT_BROWSE_ROOT"); 
                if(UtilValidate.isNotEmpty(prodCatalogCategories)) {
                    GenericValue prodCatalogCategory = prodCatalogCategories.get(0);
                    String brRootCategoryId = prodCatalogCategory.getString("productCategoryId");
                    MagentoClient magentoClient  = new MagentoClient(dispatcher, delegator);
                    CatalogCategoryTree catalogCategoryLevel0 = magentoClient.getCategoryTree(productStore.getString("magentoDefaultStoreId"));
                    CatalogCategoryInfo catalogCategoryInfo = null;
                    GenericValue productCategory = null;
                    List<GenericValue> productCategoryRollup = new ArrayList<GenericValue>();

                    List<CatalogCategoryEntity> catalogCategoryListLevel1 = catalogCategoryLevel0.getChildren().getComplexObjectArray();
                    for (CatalogCategoryEntity catalogCategoryLevel1 : catalogCategoryListLevel1) {
                        productCategory = delegator.findOne("ProductCategory", UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId()), true);
                        catalogCategoryInfo = magentoClient.getCategoryInfo(catalogCategoryLevel1.getCategoryId());
                        serviceInMap.put("productCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId());
                        serviceInMap.put("productCategoryTypeId", "CATALOG_CATEGORY");
                        serviceInMap.put("primaryParentCategoryId", brRootCategoryId);
                        serviceInMap.put("categoryName", catalogCategoryLevel1.getName());
                        serviceInMap.put("description", catalogCategoryInfo.getDescription());
                        serviceInMap.put("userLogin", userLogin);

                        serviceInMap  = dctx.getModelService("createProductCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                        if(UtilValidate.isEmpty(productCategory)) {
                            serviceOutMap = dispatcher.runSync("createProductCategory", serviceInMap);
                            if(ServiceUtil.isError(serviceOutMap)) {
                                ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                            }
                        } else {
                            serviceOutMap = dispatcher.runSync("updateProductCategory", serviceInMap);
                            if(ServiceUtil.isError(serviceOutMap)) {
                                ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                            }
                        }
                        cond = EntityCondition.makeCondition(UtilMisc.toList(
                                EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId())),
                                EntityCondition.makeCondition(UtilMisc.toMap("parentProductCategoryId", brRootCategoryId)),
                                EntityCondition.makeConditionDate("fromDate", "thruDate")));
                        productCategoryRollup = delegator.findList("ProductCategoryRollup", cond, null, null, null, false);
                        if(UtilValidate.isEmpty(productCategoryRollup)) {
                            serviceInMap  = dctx.getModelService("addProductCategoryToCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                            serviceInMap.put("parentProductCategoryId", brRootCategoryId);
                            serviceOutMap = dispatcher.runSync("addProductCategoryToCategory", serviceInMap);
                            if(ServiceUtil.isError(serviceOutMap)) {
                                ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                            }
                        }
                        List<CatalogCategoryEntity> catalogCategoryListLevel2 = catalogCategoryLevel1.getChildren().getComplexObjectArray();
                        for (CatalogCategoryEntity catalogCategoryLevel2 : catalogCategoryListLevel2) {
                            productCategory = delegator.findOne("ProductCategory", UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId()), true);
                            catalogCategoryInfo = magentoClient.getCategoryInfo(catalogCategoryLevel2.getCategoryId());
                            serviceInMap.put("productCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId());
                            serviceInMap.put("productCategoryTypeId", "CATALOG_CATEGORY");
                            serviceInMap.put("primaryParentCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId());
                            serviceInMap.put("categoryName", catalogCategoryLevel2.getName());
                            serviceInMap.put("description", catalogCategoryInfo.getDescription());
                            serviceInMap.put("userLogin", userLogin);

                            serviceInMap  = dctx.getModelService("createProductCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                            if(UtilValidate.isEmpty(productCategory)) {
                                serviceOutMap = dispatcher.runSync("createProductCategory", serviceInMap);
                                if(ServiceUtil.isError(serviceOutMap)) {
                                    ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                }
                            } else {
                                serviceOutMap = dispatcher.runSync("updateProductCategory", serviceInMap);
                                if(ServiceUtil.isError(serviceOutMap)) {
                                    ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                }
                            }
                            cond = EntityCondition.makeCondition(UtilMisc.toList(
                                    EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId())),
                                    EntityCondition.makeCondition(UtilMisc.toMap("parentProductCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId())),
                                    EntityCondition.makeConditionDate("fromDate", "thruDate")));
                            productCategoryRollup = delegator.findList("ProductCategoryRollup", cond, null, null, null, false);
                            if(UtilValidate.isEmpty(productCategoryRollup)) {
                                serviceInMap  = dctx.getModelService("addProductCategoryToCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                                serviceInMap.put("parentProductCategoryId", "MAG-"+catalogCategoryLevel1.getCategoryId());
                                serviceOutMap = dispatcher.runSync("addProductCategoryToCategory", serviceInMap);
                                if(ServiceUtil.isError(serviceOutMap)) {
                                    ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                }
                            }

                            List<CatalogCategoryEntity> catalogCategoryListLevel3 = catalogCategoryLevel2.getChildren().getComplexObjectArray();
                            for (CatalogCategoryEntity catalogCategoryLevel3 : catalogCategoryListLevel3) {
                                prodCatalogCategory = delegator.findOne("ProductCategory", UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId()), true);
                                catalogCategoryInfo = magentoClient.getCategoryInfo(catalogCategoryLevel3.getCategoryId());
                                serviceInMap.put("productCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId());
                                serviceInMap.put("productCategoryTypeId", "CATALOG_CATEGORY");
                                serviceInMap.put("primaryParentCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId());
                                serviceInMap.put("categoryName", catalogCategoryLevel3.getName());
                                serviceInMap.put("description", catalogCategoryInfo.getDescription());
                                serviceInMap.put("userLogin", userLogin);
                                serviceInMap  = dctx.getModelService("createProductCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                                if(UtilValidate.isEmpty(productCategory)) {
                                    serviceOutMap = dispatcher.runSync("createProductCategory", serviceInMap);
                                    if(ServiceUtil.isError(serviceOutMap)) {
                                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                    }
                                } else {
                                    serviceOutMap = dispatcher.runSync("updateProductCategory", serviceInMap);
                                    if(ServiceUtil.isError(serviceOutMap)) {
                                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                    }
                                }
                                cond = EntityCondition.makeCondition(UtilMisc.toList(
                                        EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId())),
                                        EntityCondition.makeCondition(UtilMisc.toMap("parentProductCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId())),
                                        EntityCondition.makeConditionDate("fromDate", "thruDate")));
                                productCategoryRollup = delegator.findList("ProductCategoryRollup", cond, null, null, null, false);
                                if(UtilValidate.isEmpty(productCategoryRollup)) {
                                    serviceInMap  = dctx.getModelService("addProductCategoryToCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                                    serviceInMap.put("parentProductCategoryId", "MAG-"+catalogCategoryLevel2.getCategoryId());
                                    serviceOutMap = dispatcher.runSync("addProductCategoryToCategory", serviceInMap);
                                    if(ServiceUtil.isError(serviceOutMap)) {
                                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                    }
                                }
                                List<CatalogCategoryEntity> catalogCategoryListLevel4 = catalogCategoryLevel3.getChildren().getComplexObjectArray();

                                for (CatalogCategoryEntity catalogCategoryLevel4 : catalogCategoryListLevel4) {
                                    prodCatalogCategory = delegator.findOne("ProductCategory", UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel4.getCategoryId()), true);
                                    catalogCategoryInfo = magentoClient.getCategoryInfo(catalogCategoryLevel4.getCategoryId());
                                    serviceInMap.put("productCategoryId", "MAG-"+catalogCategoryLevel4.getCategoryId());
                                    serviceInMap.put("productCategoryTypeId", "CATALOG_CATEGORY");
                                    serviceInMap.put("primaryParentCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId());
                                    serviceInMap.put("categoryName", catalogCategoryLevel4.getName());
                                    serviceInMap.put("description", catalogCategoryInfo.getDescription());
                                    serviceInMap.put("userLogin", userLogin);
                                    serviceInMap  = dctx.getModelService("createProductCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                                    if(UtilValidate.isEmpty(productCategoryRollup)) {
                                        serviceOutMap = dispatcher.runSync("createProductCategory", serviceInMap);
                                        if(ServiceUtil.isError(serviceOutMap)) {
                                            ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                        }
                                    } else {
                                        serviceOutMap = dispatcher.runSync("updateProductCategory", serviceInMap);
                                        if(ServiceUtil.isError(serviceOutMap)) {
                                            ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                        }
                                    }
                                    cond = EntityCondition.makeCondition(UtilMisc.toList(
                                            EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", "MAG-"+catalogCategoryLevel4.getCategoryId())),
                                            EntityCondition.makeCondition(UtilMisc.toMap("parentProductCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId())),
                                            EntityCondition.makeConditionDate("fromDate", "thruDate")));
                                    productCategoryRollup = delegator.findList("ProductCategoryRollup", cond, null, null, null, false);
                                    if(UtilValidate.isEmpty(productCategoryRollup)) {
                                        serviceInMap  = dctx.getModelService("addProductCategoryToCategory").makeValid(serviceInMap, ModelService.IN_PARAM);
                                        serviceInMap.put("parentProductCategoryId", "MAG-"+catalogCategoryLevel3.getCategoryId());
                                        serviceOutMap = dispatcher.runSync("addProductCategoryToCategory", serviceInMap);
                                        if(ServiceUtil.isError(serviceOutMap)) {
                                            ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            serviceOutMap.clear();
            serviceInMap.clear();
        } catch (GenericEntityException e) {
            Debug.logInfo(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            Debug.logInfo(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoErrorInConnectingWithMagento", locale)+ " Error Message: " +e.getMessage());
        }
        return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoCategoriesHaveBeenImportedSuccessfully", locale));
    }

    public static Map<String, Object> getMagentoProducts(DispatchContext dctx, Map<String, ?> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        File file = new File(System.getProperty("ofbiz.home") + "/runtime/tmp/MagentoProductInfo.csv");

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            MagentoClient magentoClient  = new MagentoClient(dispatcher, delegator);

            List<CatalogProductEntity> catalogProductEntityList = magentoClient.getMagentoProducts();
            List<String> csvFields = new ArrayList<String>();
            csvFields.add("Product Id");
            csvFields.add("SKU");
            csvFields.add("Product Type Id");
            csvFields.add("Product Name");
            csvFields.add("Bundle Parent Id");
            csvFields.add("Configurable Parent Id");
            csvFields.add("Grouped Parent Id");
            csvFields.add("Description");
            csvFields.add("Long Description");
            csvFields.add("Price");
            csvFields.add("Tax Class Id");
            csvFields.add("Category Ids");
            csvFields.add("Web Site Ids");
            csvFields.add("Thumbnail Image Url");
            csvFields.add("Small Image Url");
            csvFields.add("Original Image Url");
            csvFields.add("Url Key");
            csvFields.add("Url Path");
            csvFields.add("Meta Description");
            csvFields.add("Meta Keyword");
            csvFields.add("Meta Title");
            csvFields.add("Status");
            csvFields.add("Special From Date");
            csvFields.add("Special Price");
            csvFields.add("Created At");
            csvFields.add("Updated At");

            String header = StringUtil.join(csvFields, ",");
            output.write(header);
            String fieldValue = "";

            for (CatalogProductEntity catalogProductEntity : catalogProductEntityList) {
                CatalogProductReturnEntity productInfo = magentoClient.getProductInfo(catalogProductEntity.getProductId());
                String productId = catalogProductEntity.getProductId();
                String productTypeId = productInfo.getTypeId();
                Map <String, String> record = new HashMap<String, String>();
                CatalogProductImageEntityArray productImageInfoList = magentoClient.getProductImageInfoList(productId);
                List<CatalogProductImageEntity> catalogProductImageEntityList = productImageInfoList.getComplexObjectArray();
                for (CatalogProductImageEntity catalogProductImageEntity : catalogProductImageEntityList) {
                    for (String imageType : catalogProductImageEntity.getTypes().getComplexObjectArray()) {
                        if("thumbnail".equalsIgnoreCase(imageType)) {
                            record.put("Thumbnail Image Url", catalogProductImageEntity.getUrl());
                        }
                        if("small_image".equalsIgnoreCase(imageType)) {
                            record.put("Small Image Url", catalogProductImageEntity.getUrl());
                        }
                        if("image".equalsIgnoreCase(imageType)) {
                            record.put("Original Image Url", catalogProductImageEntity.getUrl());
                        }
                    }
                }
                if ("simple".equalsIgnoreCase(productTypeId) || "virtual".equalsIgnoreCase(productTypeId)) {
                    List<CatalogProductRelationEntity> catalogProductRelationEntityList = magentoClient.getParentProduct(productId);
                    for (CatalogProductRelationEntity catalogProductRelationEntity : catalogProductRelationEntityList) {
                        if("bundle".equalsIgnoreCase(catalogProductRelationEntity.getParentType())) {
                            record.put("Bundle Parent Id", catalogProductRelationEntity.getParentId());
                        }
                        if("configurable".equalsIgnoreCase(catalogProductRelationEntity.getParentType())) {
                            record.put("Configurable Parent Id", catalogProductRelationEntity.getParentId());
                        }
                        if("grouped".equalsIgnoreCase(catalogProductRelationEntity.getParentType())) {
                            record.put("Grouped Parent Id", catalogProductRelationEntity.getParentId());
                        }
                    }
                }

                record.put("Product Id", productId);
                record.put("SKU", productInfo.getSku());
                record.put("Product Type Id", productTypeId);
                record.put("Product Name", productInfo.getName());
                record.put("Description", productInfo.getShortDescription());
                record.put("Long Description", productInfo.getDescription());
                record.put("Price", productInfo.getPrice());
                record.put("Tax Class Id", productInfo.getTaxClassId());
                record.put("Category Ids", catalogProductEntity.getCategoryIds().getComplexObjectArray().toString());
                record.put("Web Site Ids", catalogProductEntity.getWebsiteIds().getComplexObjectArray().toString());
                record.put("Url Key", productInfo.getUrlKey());
                record.put("Url Path", productInfo.getUrlPath());
                record.put("Meta Description", productInfo.getMetaDescription());
                record.put("Meta Keyword", productInfo.getMetaKeyword());
                record.put("Meta Title", productInfo.getMetaTitle());
                record.put("Status", productInfo.getStatus());
                record.put("Special From Date", productInfo.getSpecialFromDate());
                record.put("Special Price", productInfo.getSpecialPrice());
                record.put("Created At", productInfo.getCreatedAt());
                record.put("Updated At", productInfo.getUpdatedAt());

                StringBuffer row = new StringBuffer();
                for (String fieldName : csvFields) {
                    fieldValue = record.get(fieldName);
                    if(UtilValidate.isNotEmpty(fieldValue)) {
                        fieldValue = fieldValue.replace("\"", "&quot;");
                    } else {
                        fieldValue = "NA";
                    }
                    row = row.append("\"" + fieldValue + "\"");
                    row = row.append(",");
                }
                output.append("\n");
                output.append(row);
            }
            output.close();
        } catch (IOException e) {
            Debug.logInfo(e.getMessage(), module);
        } catch (Exception e) {
            Debug.logInfo(e.getMessage(), module);
        }
        return result;
    }

    public static Map<String, Object> importMagentoProducts(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> serviceResult = new HashMap<String, Object>();
        int errorRecords = 0;
        int processedRecords = 0;
        try {
            serviceResult = dispatcher.runSync("getMagentoProducts", context);
            if(ServiceUtil.isError(serviceResult)) {
                ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoErrorInGettingProductsFromMagento", locale));
            }
            serviceResult.clear();

            File csvFile = new File(System.getProperty("ofbiz.home") + "/runtime/tmp/MagentoProductInfo.csv");
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String fieldDelimiter = ",";
            String fieldEncapsulator = "\"";

            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(fieldDelimiter.charAt(0))
                    .withQuote(fieldEncapsulator.charAt(0))
                    .withIgnoreEmptyLines(true)
                    .withIgnoreSurroundingSpaces(true);

            CSVParser parser = new CSVParser(reader, csvFormat);
            Boolean isFirstLine = true;
            String[] mappedKeys = null;
            Map<String, Object> processedResult = new HashMap<String, Object>();
            Map<String, Object> serviceResp = new HashMap<String, Object>();

            for (CSVRecord csvRecord : csvFormat.parse(reader)) {
                int csvRecordSize = csvRecord.size();
                if (isFirstLine) {
                    mappedKeys = new String[csvRecordSize];
                    for (int i = 0; i < csvRecordSize; i++) {
                        mappedKeys[i] = csvRecord.get(i);
                    }
                    isFirstLine = false;
                } else {
                    Map<String, Object> mappedValues = new HashMap<String, Object>();
                    for (int i = 0; i < mappedKeys.length; i++) {
                        String csvValue = csvRecord.get(i);
                        String value = (i < csvRecordSize? csvValue: "");
                        if (UtilValidate.isNotEmpty(value)) {
                            value = value.trim().replaceAll(">|<", "");
                        }
                        mappedValues.put(mappedKeys[i], value);
                    }
                    Map<String, Object> serviceInMap = UtilMisc.toMap(
                                        "userLogin", userLogin,
                                        "productId", mappedValues.get("Product Id"),
                                        "externalId", mappedValues.get("Product Id"),
                                        "sku", mappedValues.get("SKU"),
                                        "productTypeId", mappedValues.get("Product Type Id"),
                                        "productName", mappedValues.get("Product Name"),
                                        "bundleParentId", mappedValues.get("Bundle Parent Id"),
                                        "configurableParentId", mappedValues.get("Configurable Parent Id"),
                                        "groupedParentId", mappedValues.get("Grouped Parent Id"),
                                        "description", mappedValues.get("Description"),
                                        "longDescription", mappedValues.get("Long Description"),
                                        "price", mappedValues.get("Price"),
                                        "taxClassId", mappedValues.get("Tax Class Id"),
                                        "categoryIds", mappedValues.get("Category Ids"),
                                        "webSiteIds", mappedValues.get("Web Site Ids"),
                                        "thumbnailImageUrl", mappedValues.get("Thumbnail Image Url"),
                                        "smallImageUrl", mappedValues.get("Small Image Url"),
                                        "originalImageUrl", mappedValues.get("Original Image Url"),
                                        "urlKey", mappedValues.get("Url Key"),
                                        "urlPath", mappedValues.get("Url Path"),
                                        "metaDescription", mappedValues.get("Meta Description"),
                                        "metaKeyword", mappedValues.get("Meta Keyword"),
                                        "metaTitle", mappedValues.get("Meta Title"),
                                        "status", mappedValues.get("Status"),
                                        "specialFromDate", mappedValues.get("Special From Date"),
                                        "specialPrice", mappedValues.get("Special Price"),
                                        "createdDate", mappedValues.get("Created At"),
                                        "lastModifiedDate", mappedValues.get("Updated At")
                                    );
                    Boolean isError = false;
                    if (UtilValidate.isEmpty(serviceInMap.get("productId"))) {
                        isError = true;
                        Debug.logError("Product ID is missing : ", module);
                    }
                    String productId = (String) serviceInMap.get("productId");
                    productId = "MAG-"+productId.trim();
                    serviceInMap.put("productId", productId);
                    String bundleParentId = ((String) serviceInMap.get("bundleParentId")).trim();
                    if(!("NA".equalsIgnoreCase(bundleParentId))) {
                        bundleParentId = "MAG-"+bundleParentId;
                    }
                    serviceInMap.put("bundleParentId", bundleParentId);
                    String configurableParentId = ((String) serviceInMap.get("configurableParentId")).trim();
                    if(!("NA".equalsIgnoreCase(configurableParentId))) {
                        configurableParentId = "MAG-"+configurableParentId;
                    }
                    serviceInMap.put("configurableParentId", configurableParentId);
                    String groupedParentId = ((String) serviceInMap.get("groupedParentId")).trim();
                    if(!("NA".equalsIgnoreCase(groupedParentId))) {
                        groupedParentId = "MAG-"+groupedParentId;
                    }
                    serviceInMap.put("groupedParentId", groupedParentId);
                    if (UtilValidate.isEmpty(serviceInMap.get("productTypeId"))) {
                        isError = true;
                        Debug.logError("Product Type ID is missing for product id : "+productId, module);
                    }
                    if (UtilValidate.isEmpty(serviceInMap.get("productName"))) {
                        isError = true;
                        Debug.logError("Name is missing for product id : "+productId, module);
                    }
                    if (UtilValidate.isEmpty(serviceInMap.get("price"))) {
                        isError = true;
                        Debug.logError("Price is missing for product id : "+productId, module);
                    }
                    Debug.logInfo("Begin processing for productId ["+productId+"]", module);
                    if (!isError) {
                        Debug.logInfo("Create / Update product having productId ["+productId+"]", module);
                        serviceResult = dispatcher.runSync("createMagentoProducts", serviceInMap, 600, true);
                    }
                    if (ServiceUtil.isError(serviceResult) || isError) {
                        errorRecords++;
                        processedResult.put(productId, "Error");
                        Debug.logInfo("Completed processing for productId ["+productId+"] with ERROR ", module);
                        if (ServiceUtil.isError(serviceResult)) {
                            Debug.logInfo(ServiceUtil.getErrorMessage(serviceResp), module);
                        }
                    } else {
                        processedResult.put(productId, "Success");
                        Debug.logInfo("Processing successfully completed for product ["+productId+"]", module);
                    }
                    processedRecords++;
                }
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        } catch (IOException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        }
        return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoProductsHaveBeenImportedSuccessfully", UtilMisc.toMap("processedRecords", processedRecords, "successRecords", (processedRecords - errorRecords)), locale));
    }

    public static Map<String, Object> createMagentoProducts(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> serviceInMap = new HashMap<String, Object>();
        Map<String, Object> serviceOutMap = new HashMap<String, Object>();
        String productId = (String) context.get("productId");
        String productTypeId = (String) context.get("productTypeId");
        String originalImageUrl = (String) context.get("originalImageUrl");
        String smallImageUrl = (String) context.get("smallImageUrl");
        String thumbnailImageUrl = (String) context.get("thumbnailImageUrl");
        String bundleParentId = (String) context.get("bundleParentId");
        String configurableParentId = (String) context.get("configurableParentId");
        String groupedParentId = (String) context.get("groupedParentId");

        /*"NA" as an string is coming from magento if image url is not present there*/
        if(UtilValidate.isEmpty(originalImageUrl) || ("NA".equalsIgnoreCase(originalImageUrl))) {
            originalImageUrl = smallImageUrl;
        }
        if(UtilValidate.isEmpty(originalImageUrl) || ("NA".equalsIgnoreCase(originalImageUrl))) {
            originalImageUrl = thumbnailImageUrl;
        }
        if(UtilValidate.isEmpty(thumbnailImageUrl) || ("NA".equalsIgnoreCase(thumbnailImageUrl))) {
            thumbnailImageUrl = originalImageUrl;
        }
        if(UtilValidate.isEmpty(smallImageUrl) || ("NA".equalsIgnoreCase(smallImageUrl))) {
            smallImageUrl = thumbnailImageUrl;
        }
        try {
            //Create Product
            GenericValue product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), false);
            serviceInMap.put("productId", productId);
            serviceInMap.put("productTypeId", "FINISHED_GOOD");
            serviceInMap.put("isVirtual", "N");
            serviceInMap.put("isVariant", "N");
            if("configurable".equalsIgnoreCase(productTypeId)) {
                serviceInMap.put("isVirtual", "Y");
                serviceInMap.put("isVariant", "N");
            }
            if ("simple".equalsIgnoreCase(productTypeId)) {
                if(!("NA".equalsIgnoreCase(configurableParentId))) {
                    serviceInMap.put("isVirtual", "N");
                    serviceInMap.put("isVariant", "Y");
                }
            }
            if ("virtual".equalsIgnoreCase(productTypeId)) {
                serviceInMap.put("productTypeId", "SERVICE_PRODUCT");
            }
            serviceInMap.put("internalName", context.get("productName"));
            serviceInMap.put("productName", context.get("productName"));
            serviceInMap.put("originalImageUrl", originalImageUrl);
            serviceInMap.put("largeImageUrl", originalImageUrl);
            serviceInMap.put("detailImageUrl", originalImageUrl);
            serviceInMap.put("mediumImageUrl", smallImageUrl);
            serviceInMap.put("smallImageUrl", thumbnailImageUrl);
            serviceInMap.put("userLogin", userLogin);
            if(UtilValidate.isEmpty(product)) {
                serviceOutMap = dispatcher.runSync("createProduct", serviceInMap);
                if(ServiceUtil.isError(serviceOutMap)) {
                    ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                }
            } else {
                serviceOutMap = dispatcher.runSync("updateProduct", serviceInMap);
                if(ServiceUtil.isError(serviceOutMap)) {
                    ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                }
            }
                serviceInMap.clear();
            //Create Product Assoc
            //Step 1 : First Create Parent Products If they are not exists
            //Step 1.1 : Create Parent Product of Bundle Type
            if(!("NA".equalsIgnoreCase(bundleParentId))) {
                GenericValue bundleProduct = delegator.findOne("Product", UtilMisc.toMap("productId", bundleParentId), false);
                if(UtilValidate.isEmpty(bundleProduct)) {
                    serviceInMap.put("productId", bundleParentId);
                    serviceInMap.put("internalName", bundleParentId);
                    serviceInMap.put("userLogin", userLogin);
                    serviceInMap.put("productTypeId", "FINISHED_GOOD");
                    serviceOutMap = dispatcher.runSync("createProduct", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                    serviceInMap.clear();
                }
            }
            //Step 1.2 : Create Parent Product of Configurable Type
            if(!("NA".equalsIgnoreCase(configurableParentId))) {
                GenericValue configurableProduct = delegator.findOne("Product", UtilMisc.toMap("productId", configurableParentId), false);
                if(UtilValidate.isEmpty(configurableProduct)) {
                    serviceInMap.put("productId", configurableParentId);
                    serviceInMap.put("internalName", configurableParentId);
                    serviceInMap.put("userLogin", userLogin);
                    serviceInMap.put("productTypeId", "FINISHED_GOOD");
                    serviceOutMap = dispatcher.runSync("createProduct", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                    serviceInMap.clear();
                }
            }
            //Step 1.3 : Create Parent Product of Grouped Type
            if(!("NA".equalsIgnoreCase(groupedParentId))) {
                GenericValue groupedProduct = delegator.findOne("Product", UtilMisc.toMap("productId", groupedParentId), false);
                if(UtilValidate.isEmpty(groupedProduct)) {
                    serviceInMap.put("productId", groupedParentId);
                    serviceInMap.put("internalName", groupedParentId);
                    serviceInMap.put("userLogin", userLogin);
                    serviceInMap.put("productTypeId", "FINISHED_GOOD");
                    serviceOutMap = dispatcher.runSync("createProduct", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                    serviceInMap.clear();
                }
            }

            //Step 2 : Now create association of parent and child product, if it is not already exists
            //Step 2.1 : Associated simple product of Magento with its configurable parent product(Virtual-Variant Relationship in OFBiz)
            if("simple".equalsIgnoreCase(productTypeId) && !("NA".equalsIgnoreCase(configurableParentId))) {
                EntityCondition productAssocCond = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition(UtilMisc.toMap("productId", configurableParentId)),
                        EntityCondition.makeCondition(UtilMisc.toMap("productIdTo", productId)),
                        EntityCondition.makeCondition(UtilMisc.toMap("productAssocTypeId", "PRODUCT_VARIANT")),
                        EntityCondition.makeConditionDate("fromDate", "thruDate")));
                List<GenericValue> productAssoc = delegator.findList("ProductAssoc", productAssocCond, null, null, null, false);
                if(UtilValidate.isEmpty(productAssoc)) {
                    serviceInMap.put("productId", configurableParentId);
                    serviceInMap.put("productIdTo", productId);
                    serviceInMap.put("productAssocTypeId", "PRODUCT_VARIANT");
                    serviceInMap.put("fromDate", UtilDateTime.nowTimestamp());
                    serviceInMap.put("userLogin", userLogin);
                    serviceOutMap = dispatcher.runSync("createProductAssoc", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                    serviceInMap.clear();
                }
            }

            //Step 2.2 : Associated virtual product of Magento (Service Product in OFBiz) with its bundle parent product
            if("virtual".equalsIgnoreCase(productTypeId) && !("NA".equalsIgnoreCase(bundleParentId))) {
                EntityCondition productAssocCond = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition(UtilMisc.toMap("productId", bundleParentId)),
                        EntityCondition.makeCondition(UtilMisc.toMap("productIdTo", productId)),
                        EntityCondition.makeCondition(UtilMisc.toMap("productAssocTypeId", "PRODUCT_SERVICE")),
                        EntityCondition.makeConditionDate("fromDate", "thruDate")));
                List<GenericValue> productAssoc = delegator.findList("ProductAssoc", productAssocCond, null, null, null, false);
                if(UtilValidate.isEmpty(productAssoc)) {
                    serviceInMap.put("productId", bundleParentId);
                    serviceInMap.put("productIdTo", productId);
                    serviceInMap.put("productAssocTypeId", "PRODUCT_SERVICE");
                    serviceInMap.put("fromDate", UtilDateTime.nowTimestamp());
                    serviceInMap.put("userLogin", userLogin);
                    serviceOutMap = dispatcher.runSync("createProductAssoc", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                    serviceInMap.clear();
                }
            }

            //Create Product Category Member
            String categoryIds = (String) context.get("categoryIds");
            if (UtilValidate.isNotEmpty(categoryIds)) {
                categoryIds = categoryIds.replace("]", "").replace("[", "").trim();
                if (UtilValidate.isNotEmpty(categoryIds)) {
                    List<String> categoryIdList = new ArrayList<String>(Arrays.asList(categoryIds.split(",")));
                    for (String categoryId : categoryIdList) {
                        categoryId = "MAG-"+(categoryId.trim());
                        EntityCondition cond = EntityCondition.makeCondition(UtilMisc.toList(
                                EntityCondition.makeCondition(UtilMisc.toMap("productId", productId)),
                                EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", categoryId)),
                                EntityCondition.makeConditionDate("fromDate", "thruDate")));
                        List<GenericValue> productCategoryMember = delegator.findList("ProductCategoryMember", cond, null, null, null, false);
                        if(UtilValidate.isEmpty(productCategoryMember)) {
                            serviceInMap.put("productId", productId);
                            serviceInMap.put("productCategoryId", categoryId);
                            serviceInMap.put("userLogin", userLogin);
                            //To Do : Need to add fromDate Also
                            serviceOutMap = dispatcher.runSync("addProductToCategory", serviceInMap);
                            if(ServiceUtil.isError(serviceOutMap)) {
                                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                            }
                        }
                        //Following code is getting catolog information of a productCategory
                        String prodCatalogId = MagentoHelper.getProdCatalogId(delegator, categoryId);
                        if(UtilValidate.isNotEmpty(prodCatalogId)) {
                            String viewAllowCategoryId = CatalogWorker.getCatalogViewAllowCategoryId(delegator, prodCatalogId);
                            String purchAllowCategoryId = CatalogWorker.getCatalogPurchaseAllowCategoryId(delegator, prodCatalogId);
                            if("configurable".equalsIgnoreCase(productTypeId) || ("simple".equalsIgnoreCase(productTypeId) && ("NA".equalsIgnoreCase(configurableParentId)))) {
                                //Product is a virtulal || Simple product, associate it with PURCH_ALLOW and VIEW_ALLOW CATEGORY
                                if(UtilValidate.isNotEmpty(viewAllowCategoryId)) {
                                    cond = EntityCondition.makeCondition(UtilMisc.toList(
                                            EntityCondition.makeCondition(UtilMisc.toMap("productId", productId)),
                                            EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", viewAllowCategoryId)),
                                            EntityCondition.makeConditionDate("fromDate", "thruDate")));
                                    productCategoryMember = delegator.findList("ProductCategoryMember", cond, null, null, null, false);
                                    if(UtilValidate.isEmpty(productCategoryMember)) {
                                        serviceInMap.put("productId", productId);
                                        serviceInMap.put("productCategoryId", viewAllowCategoryId);
                                        serviceInMap.put("userLogin", userLogin);
                                        serviceOutMap = dispatcher.runSync("addProductToCategory", serviceInMap);
                                        if(ServiceUtil.isError(serviceOutMap)) {
                                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                        }
                                    }
                                    serviceInMap.clear();
                                }
                                if(UtilValidate.isNotEmpty(purchAllowCategoryId)) {
                                    cond = EntityCondition.makeCondition(UtilMisc.toList(
                                            EntityCondition.makeCondition(UtilMisc.toMap("productId", productId)),
                                            EntityCondition.makeCondition(UtilMisc.toMap("productCategoryId", purchAllowCategoryId)),
                                            EntityCondition.makeConditionDate("fromDate", "thruDate")));
                                    productCategoryMember = delegator.findList("ProductCategoryMember", cond, null, null, null, false);
                                    if(UtilValidate.isEmpty(productCategoryMember)) {
                                        serviceInMap.put("productId", productId);
                                        serviceInMap.put("productCategoryId", purchAllowCategoryId);
                                        serviceInMap.put("userLogin", userLogin);
                                        serviceOutMap = dispatcher.runSync("addProductToCategory", serviceInMap);
                                        if(ServiceUtil.isError(serviceOutMap)) {
                                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                                        }
                                    }
                                    serviceInMap.clear();
                                }
                            }
                        }
                    }
                }
            }
            serviceInMap.clear();

            //Create Product Price
            if(UtilValidate.isNotEmpty(context.get("price")) && !("NA".equalsIgnoreCase((String) context.get("price")))) {
                BigDecimal price = new BigDecimal((String) context.get("price"));
                EntityCondition cond = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition(UtilMisc.toMap("productId", productId)),
                        EntityCondition.makeCondition(UtilMisc.toMap("productPriceTypeId", "DEFAULT_PRICE")),
                        EntityCondition.makeCondition(UtilMisc.toMap("productPricePurposeId", "PURCHASE")),
                        EntityCondition.makeCondition(UtilMisc.toMap("currencyUomId", "USD")),
                        EntityCondition.makeCondition(UtilMisc.toMap("productStoreGroupId", "_NA_")),
                        EntityCondition.makeConditionDate("fromDate", "thruDate")));
                List<GenericValue> productPrices = delegator.findList("ProductPrice", cond, null, null, null, false);
                serviceInMap.put("productId", productId);
                serviceInMap.put("productPriceTypeId", "DEFAULT_PRICE");
                serviceInMap.put("productPricePurposeId", "PURCHASE");
                serviceInMap.put("currencyUomId", "USD");
                serviceInMap.put("productStoreGroupId", "_NA_");
                serviceInMap.put("price", price);
                serviceInMap.put("userLogin", userLogin);
                if(UtilValidate.isEmpty(productPrices)){
                    serviceOutMap = dispatcher.runSync("createProductPrice", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                } else {
                    GenericValue productPrice = EntityUtil.getFirst(productPrices);
                    serviceInMap.put("fromDate", productPrice.getTimestamp("fromDate"));
                    serviceOutMap = dispatcher.runSync("updateProductPrice", serviceInMap);
                    if(ServiceUtil.isError(serviceOutMap)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                    }
                }
                serviceInMap.clear();
            }

            //Good Identification
            GenericValue goodIdentification = EntityQuery.use(delegator).from("GoodIdentification").where("productId", productId, "goodIdentificationTypeId", "MAGENTO_ID").queryOne();
            //GenericValue goodIdentification = delegator.findOne("GoodIdentification", UtilMisc.toMap("productId", productId, "goodIdentificationTypeId", "MAGENTO_ID"), false);
            if(UtilValidate.isEmpty(goodIdentification)) {
                serviceInMap.put("productId", productId);
                serviceInMap.put("goodIdentificationTypeId", "MAGENTO_ID");
                serviceInMap.put("idValue", context.get("externalId"));
                serviceInMap.put("userLogin", context.get("userLogin"));
                serviceOutMap = dispatcher.runSync("createGoodIdentification", serviceInMap);
                if(ServiceUtil.isError(serviceOutMap)) {
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceOutMap));
                }
            }
        } catch(GenericServiceException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        }
        return ServiceUtil.returnSuccess();
    }
}