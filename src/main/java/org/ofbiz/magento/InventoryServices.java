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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;

import magento.CatalogInventoryStockItemEntity;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.GeneralRuntimeException;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.content.content.ContentWorker;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;

public class InventoryServices {
    public static final String module = InventoryServices.class.getName();
    public static final String resource = "MagentoUiLabels";

    public static Map<String, Object> importInventoryFromMagento(DispatchContext dctx, Map<String, ?> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> serviceResult = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String facilityId = (String) context.get("facilityId");
        MagentoClient magentoClient = new MagentoClient(dispatcher, delegator);

        try {
            List<GenericValue> magentoProducts = delegator.findList("GoodIdentification", EntityCondition.makeCondition("goodIdentificationTypeId",EntityOperator.EQUALS, "MAGENTO_ID"), UtilMisc.toSet("productId", "idValue"), null, null, true);
            if (UtilValidate.isNotEmpty(magentoProducts)) {
                for (GenericValue magentoProduct : magentoProducts) {
                    String productId = magentoProduct.getString("productId");
                    CatalogInventoryStockItemEntity catalogInventoryStock = magentoClient.getCatalogInventoryStock(magentoProduct.getString("idValue"));
                    BigDecimal inventoryCount = (BigDecimal) ObjectType.simpleTypeConvert(catalogInventoryStock.getQty(), "BigDecimal", null, locale);

                    serviceCtx.put("productId", productId);
                    serviceCtx.put("facilityId", facilityId);
                    serviceCtx.put("lastInventoryCount", inventoryCount);
                    serviceCtx.put("userLogin", userLogin);
                    GenericValue productFacility = delegator.findOne("ProductFacility", false, UtilMisc.toMap("productId", productId, "facilityId", facilityId));
                    if(UtilValidate.isEmpty(productFacility)) {
                        serviceResult = dispatcher.runSync("createProductFacility", serviceCtx);
                        if(ServiceUtil.isError(serviceResult)) {
                           return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
                        }
                    } else {
                        serviceResult = dispatcher.runSync("updateProductFacility", serviceCtx);
                        if(ServiceUtil.isError(serviceResult)) {
                           return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
                        }
                    }
                }
            }
        } catch (GenericServiceException e) {
            Debug.logError(e.getMessage(), module);
            e.printStackTrace();
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            e.printStackTrace();
        } catch (GeneralException e) {
            Debug.logError(e.getMessage(), module);
            e.printStackTrace();
        }
        return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoInventoryHasBeenImportedSuccessfully", locale));
    }

    public static Map<String, Object> loadAndImportWarehouseLocations(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String fileName = (String) context.get("_uploadedFile_fileName");
        String processData = (String) context.get("processData");
        String contentId = (String) context.get("contentId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        List<Map<String, Object>> productFacilityLocations = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> processedProductFacilityLocations = new ArrayList<Map<String, Object>>();
        String facilityId = (String) context.get("facilityId");

        Map<String, Object> serviceResult = new HashMap<String, Object>();
        try {
            if (UtilValidate.isEmpty(fileName)&& "N".equalsIgnoreCase(processData)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoPleaseSelectCSVFileForImport", locale));
            }
            boolean isCsv = false;
            if (UtilValidate.isNotEmpty(fileName)&& "N".equalsIgnoreCase(processData)) {
                isCsv = fileName.contains(".csv");
            }
            if (!isCsv && "N".equalsIgnoreCase(processData)) {
                isCsv = fileName.contains(".CSV");
            }
            // If file passed and it is not csv file
            if (!isCsv && "N".equalsIgnoreCase(processData)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoPleaseSelectTheFileInCSVFormat", locale));
            }
            if (UtilValidate.isEmpty(contentId)&& "Y".equalsIgnoreCase(processData)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoNoDataExistsToProcess", locale));
            }
            if (UtilValidate.isNotEmpty(fileName)) {
                Map<String, Object> fileUploadToServerCtx = dctx.getModelService("fileUploadToServer").makeValid(context, ModelService.IN_PARAM);
                fileUploadToServerCtx.put("contentTypeId", "PROD_FAC_CSV_CNT");
                fileUploadToServerCtx.put("statusId", "PROD_FAC_CSV_INPRGRS");
                fileUploadToServerCtx.put("userLogin", userLogin);
                Map<String, Object> fileUploadToServerResp = dispatcher.runSync("fileUploadToServer", fileUploadToServerCtx);
                if (ServiceUtil.isError(fileUploadToServerResp)) {
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(fileUploadToServerResp));
                }
                contentId = (String) fileUploadToServerResp.get("contentId");
            }
        } catch (GenericServiceException ex) {
            // TODO Auto-generated catch block
            return ServiceUtil.returnError(ex.getMessage());
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        try {
            String xmlString = ContentWorker.renderContentAsText(dispatcher, contentId, null, locale, "text/plain", false);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(xmlString.getBytes())));
            String fieldDelimiter = ",";
            String fieldEncapsulator = "\"";
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(fieldDelimiter.charAt(0))
                    .withQuote(fieldEncapsulator.charAt(0))
                    .withIgnoreEmptyLines(true)
                    .withIgnoreSurroundingSpaces(true);

            Boolean isFirstLine = true;
            String[] mappedKeys = null;
            List<String> serviceFields = new ArrayList<String>();
            serviceFields.add("Product Id");
            serviceFields.add("Location Seq Id");
            serviceFields.add("Area Id");
            serviceFields.add("Aisle Id");
            serviceFields.add("Section Id");
            serviceFields.add("Level Id");
            serviceFields.add("Position Id");
            serviceFields.add("Inventory Count");
            for (CSVRecord csvRecord : csvFormat.parse(reader)) {
                int csvRecordSize = csvRecord.size();
                if (isFirstLine) {
                    mappedKeys = new String[csvRecordSize];
                    for (int i = 0; i < csvRecordSize; i++) {
                        if (serviceFields.contains(csvRecord.get(i).trim())) {
                            mappedKeys[i] = csvRecord.get(i);
                        } else {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "MagentoInvalidColumnFoundInCSV", locale)+csvRecord.get(i));
                        }
                    }
                    isFirstLine = false;
                } else {
                    Map<String, Object> mappedValues = new HashMap<String, Object>();
                    for (int i = 0; i < mappedKeys.length; i++) {
                        String csvValue = csvRecord.get(i);
                        String value = (i < csvRecordSize? csvValue: "");
                        if (UtilValidate.isNotEmpty(value)) {
                            value = value.trim();
                        }
                        mappedValues.put(mappedKeys[i], value);
                    }
                    Map<String, Object> serviceInMap = new HashMap<String, Object>();
                    Boolean isError = false;
                    StringBuilder errorMessage = new StringBuilder();
                    String productId = (String) mappedValues.get("Product Id");
                    String locationSeqId = (String) mappedValues.get("Location Seq Id");
                    String areaId = (String) mappedValues.get("Area Id");
                    String aisleId = (String) mappedValues.get("Aisle Id");
                    String sectionId = (String) mappedValues.get("Section Id");
                    String levelId = (String) mappedValues.get("Level Id");
                    String positionId = (String) mappedValues.get("Position Id");
                    String inventoryCount = (String) mappedValues.get("Inventory Count");
                    if(UtilValidate.isEmpty(productId)) {
                        errorMessage.append(UtilProperties.getMessage(resource, "MagentoErrorProductIdIsMissing", locale));
                        isError = true;
                    }
                    if(UtilValidate.isNotEmpty(productId)) {
                        GenericValue goodIdentification = delegator.findOne("GoodIdentification", UtilMisc.toMap("productId", productId, "goodIdentificationTypeId", "MAGENTO_ID"), true);
                        if(UtilValidate.isEmpty(goodIdentification)) {
                            errorMessage.append(UtilProperties.getMessage(resource, "MagentoErrorProductNotFound",UtilMisc.toMap("productId", productId), locale));
                            isError = true;
                        } else {
                        	GenericValue product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), true);
                        	if(UtilValidate.isNotEmpty(product) && (product.getString("isVirtual").equalsIgnoreCase("Y"))) {
                                errorMessage.append(UtilProperties.getMessage(resource, "MagentoErrorProductIsAVirtualProduct",UtilMisc.toMap("productId", productId), locale));
                                isError = true;
                        	}
                        }
                    }
                    if(UtilValidate.isEmpty(locationSeqId)) {
                        if (UtilValidate.isNotEmpty(errorMessage)) {
                            errorMessage.append(", ");
                        } else {
                            errorMessage.append(UtilProperties.getMessage(resource, "MagentoError", locale)+ " : ");
                        }
                        errorMessage.append(UtilProperties.getMessage(resource, "MagentoLocationSeqIdIsMissing", locale));
                        isError = true;
                    }
                    if(UtilValidate.isEmpty(inventoryCount)) {
                        if (UtilValidate.isNotEmpty(errorMessage)) {
                            errorMessage.append(", ");
                        } else {
                            errorMessage.append(UtilProperties.getMessage(resource, "MagentoError", locale)+ " : ");
                        }
                        errorMessage.append(UtilProperties.getMessage(resource, "MagentoQuantityIsMissing", locale));
                        isError = true;
                    } else {
                        Integer inventory = Integer.valueOf(inventoryCount);
                        if (inventory < 0) {
                            if (UtilValidate.isNotEmpty(errorMessage)) {
                                errorMessage.append(", ");
                            } else {
                                errorMessage.append(UtilProperties.getMessage(resource, "MagentoError", locale)+ " : ");
                            }
                            errorMessage.append(UtilProperties.getMessage(resource, "MagentoQuantityCannotBeNegative", locale));
                            isError = true;
                        }
                    }
                    serviceInMap.put("productId", productId);
                    serviceInMap.put("locationSeqId", locationSeqId);
                    serviceInMap.put("areaId", areaId);
                    serviceInMap.put("aisleId", aisleId);
                    serviceInMap.put("sectionId", sectionId);
                    serviceInMap.put("levelId", levelId);
                    serviceInMap.put("positionId", positionId);
                    serviceInMap.put("inventoryCount", inventoryCount);
                    serviceInMap.put("facilityId", facilityId);
                    if(isError) {
                        serviceInMap.put("message", errorMessage);
                        serviceResult.put("isError", "Y");
                    } else {
                        serviceInMap.put("message", "Success");
                    }
                    serviceInMap.put("isError", isError);
                    productFacilityLocations.add(serviceInMap);
                }
            }
        } catch (IOException e) {
            throw new GeneralRuntimeException(UtilProperties.getMessage(resource, "MagentoErrorInResponseWriterOutputStream", locale)+ e.toString(), e);
        } catch (GeneralException e) {
            throw new GeneralRuntimeException(UtilProperties.getMessage(resource, "MagentoErrorRenderingContent", locale)+ e.toString(), e);
        }
        Map<String, Object> processedResult = new HashMap<String, Object>();
        int errorRecords=0;
        int processedRecords=0;
        if ("Y".equalsIgnoreCase(processData)) {
            if (!productFacilityLocations.isEmpty()) {
                for (Map<String, Object> productFacilityLocation: productFacilityLocations) {
                    String productId = (String) productFacilityLocation.get("productId");
                    processedRecords++;
                    try {
                        productFacilityLocation.put("userLogin", userLogin);
                        productFacilityLocation.remove("message");
                        productFacilityLocation.remove("isError");
                        Map<String, Object> serviceResp = dispatcher.runSync("createUpdateProductFacilityAndLocation", productFacilityLocation, 3600, true);
                        if (ServiceUtil.isError(serviceResp)) {
                            processedResult.put(productId, "Error");
                            errorRecords++;
                        } else {
                            processedResult.put(productId, "Success");
                        }
                        processedProductFacilityLocations.add(productFacilityLocation);
                    } catch (Exception ex) {
                        return ServiceUtil.returnError(ex.getMessage());
                    }
                }
            }
            String jobId = FileUploadHelper.getJobId(delegator, "importWarehouseLocations");
            if (UtilValidate.isNotEmpty(jobId)) {
                String statusId = null;
                if(errorRecords == processedRecords){
                    statusId = "PROD_FAC_CSV_FAIL";
                } else if (errorRecords == 0) {
                    statusId = "PROD_FAC_CSV_SUCCESS";
                } else {
                    statusId = "PROD_FAC_CSV_PARTIAL";
                }
                try {
                    String message = FileUploadHelper.getPlainCustomMessage(processedResult, errorRecords, processedRecords);
                    Map<String, Object> updateContentResp = dispatcher.runSync("updateContent", UtilMisc.<String, Object>toMap("contentId", contentId,"statusId", statusId, "userLogin", userLogin));
                    if (ServiceUtil.isError(updateContentResp)) {
                        Debug.logError(ServiceUtil.getErrorMessage(updateContentResp), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(updateContentResp));
                    }
                    Map<String, Object> createSimpleTextContentDataResp = dispatcher.runSync("createSimpleTextContentData", UtilMisc.<String, Object>toMap("contentName", "Result_"+jobId+".txt","contentTypeId", "PROD_FAC_CSV_LOG", "text", message, "userLogin", userLogin));
                    if (ServiceUtil.isError(createSimpleTextContentDataResp)) {
                        Debug.logError(ServiceUtil.getErrorMessage(createSimpleTextContentDataResp), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createSimpleTextContentDataResp));
                    }
                    Map<String, Object> createContentAssocResp = dispatcher.runSync("createContentAssoc", UtilMisc.toMap("contentIdFrom", contentId, "contentIdTo", createSimpleTextContentDataResp.get("contentId"), "contentAssocTypeId", "PROD_FAC_CSV_RESULT", "userLogin", userLogin));
                    if (ServiceUtil.isError(createContentAssocResp)) {
                        Debug.logError(ServiceUtil.getErrorMessage(createContentAssocResp), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createContentAssocResp));
                    }
                } catch (GenericServiceException ex) {
                    return ServiceUtil.returnError(ex.getMessage());
                }
            }
        }
        serviceResult.put("productFacilityLocations", productFacilityLocations);
        if ("Y".equalsIgnoreCase(processData)) {
            serviceResult.put("processedProductFacilityLocations", processedProductFacilityLocations);
        } else {
            serviceResult.put("loadFileContent", contentId);
        }
        return serviceResult;
    }

    public static Map<String, Object> createUpdateProductFacilityAndLocation(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        String facilityId = (String) context.get("facilityId");
        String locationSeqId = (String) context.get("locationSeqId");
        String areaId = (String) context.get("areaId");
        String aisleId = (String) context.get("aisleId");
        String sectionId = (String) context.get("sectionId");
        String levelId = (String) context.get("levelId");
        String positionId = (String) context.get("positionId");
        String inventoryCount = (String) context.get("inventoryCount");
        Map<String, Object> serviceResult = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        try {
            GenericValue facilityLocation = delegator.findOne("FacilityLocation", false, UtilMisc.toMap("locationSeqId", locationSeqId, "facilityId", facilityId));
            if(UtilValidate.isEmpty(facilityLocation)) {
                facilityLocation = delegator.makeValue("FacilityLocation", UtilMisc.toMap("locationSeqId", locationSeqId, "facilityId", facilityId, "locationTypeEnumId", "FLT_PICKLOC", "areaId", areaId, "aisleId", aisleId, "sectionId", sectionId, "levelId", levelId, "positionId", positionId));
                facilityLocation.create();
            }
            GenericValue productFacility = delegator.findOne("ProductFacility", false, UtilMisc.toMap("productId", productId, "facilityId", facilityId));
            if(UtilValidate.isEmpty(productFacility)) {
                serviceResult = dispatcher.runSync("createProductFacility", UtilMisc.toMap("productId", productId, "facilityId", facilityId, "userLogin", userLogin));
                if(ServiceUtil.isError(serviceResult)) {
                   return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
                }
            }
            GenericValue productFacilityLocation = delegator.findOne("ProductFacilityLocation", false, UtilMisc.toMap("productId", productId, "locationSeqId", locationSeqId, "facilityId", facilityId));
            if(UtilValidate.isEmpty(productFacilityLocation)) {
                serviceResult = dispatcher.runSync("createProductFacilityLocation", UtilMisc.toMap("productId", productId, "locationSeqId", locationSeqId, "facilityId", facilityId, "userLogin", userLogin));
                if(ServiceUtil.isError(serviceResult)) {
                   return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
                }
            }

            serviceCtx.put("userLogin", userLogin);
            serviceCtx.put("facilityId", facilityId);
            serviceCtx.put("productId", productId);
            serviceCtx.put("locationSeqId", locationSeqId);
            serviceCtx.put("quantityAccepted", inventoryCount);
            serviceCtx.put("quantityRejected", BigDecimal.ZERO);
            serviceCtx.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
            dispatcher.runSync("receiveInventoryProduct",serviceCtx);
            serviceCtx.clear();

        } catch (GenericServiceException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
            e.printStackTrace();
        }
        return ServiceUtil.returnSuccess();
    }
}