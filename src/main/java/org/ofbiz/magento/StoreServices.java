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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magento.StoreAddressEntity;
import magento.StoreConfigEntity;
import magento.StoreShippingMethodsEntity;
import magento.StoreShippingMethodsEntityArray;
import magento.StoreShippingOriginEntity;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.magento.MagentoClient;
import org.apache.ofbiz.magento.MagentoHelper;
import org.apache.ofbiz.party.party.PartyWorker;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;

public class StoreServices {
    public static final String module = MagentoServices.class.getName();
    public static final String resource = "MagentoUiLabels";

    public static Map<String, Object> createUpdateCompany(DispatchContext dctx, Map<String, Object>context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Map<String, String> contactNumberMap = new HashMap<String, String>();
        Locale locale = (Locale) context.get("locale");
        String partyId = (String) context.get("partyId");
        String postalContactMechId = (String) context.get("postalContactMechId");
        String emailContactMechId = (String) context.get("emailContactMechId");
        String telecomContactMechId = (String) context.get("telecomContactMechId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {
            // Create Company
            serviceCtx.put("groupName", (String) context.get("groupName"));
            serviceCtx.put("userLogin", userLogin);
            if (UtilValidate.isNotEmpty(partyId)) {
                serviceCtx.put("partyId", partyId);
                result = dispatcher.runSync("updatePartyGroup", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            } else {
                result = dispatcher.runSync("createPartyGroup", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                partyId = (String) result.get("partyId");

                serviceCtx.clear();
                //Create Company roles
                List<String> companyRoles = new ArrayList<String>();
                companyRoles.add("BILL_FROM_VENDOR");
                companyRoles.add("BILL_TO_CUSTOMER");
                companyRoles.add("INTERNAL_ORGANIZATIO");
                companyRoles.add("PARENT_ORGANIZATION");
                serviceCtx.put("userLogin", userLogin);
                serviceCtx.put("partyId", partyId);
                for (String companyRole : companyRoles) {
                    serviceCtx.put("roleTypeId", companyRole);
                    result = dispatcher.runSync("createPartyRole", serviceCtx);
                    if(!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }

                //Create Relationship of logged in user with Company
                serviceCtx.put("partyIdFrom", partyId);
                serviceCtx.put("partyIdTo", userLogin.get("partyId"));
                serviceCtx.put("roleTypeIdFrom", "INTERNAL_ORGANIZATIO");
                serviceCtx.put("roleTypeIdTo", "APPLICATION_USER");
                serviceCtx.put("partyRelationshipTypeId", "OWNER");
                result = dispatcher.runSync("createPartyRelationship", serviceCtx);
            }

            serviceCtx.clear();
            result.clear();
            if (UtilValidate.isNotEmpty(postalContactMechId)) {
                serviceCtx = dctx.getModelService("updatePartyPostalAddress").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("partyId", partyId);
                serviceCtx.put("contactMechId", postalContactMechId);
                result = dispatcher.runSync("updatePartyPostalAddress", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            } else if (UtilValidate.isNotEmpty(context.get("address1"))){
                // Create Company Postal Address.
                serviceCtx = dctx.getModelService("createPartyPostalAddress").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("partyId", partyId);
                result = dispatcher.runSync("createPartyPostalAddress", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                postalContactMechId = (String) result.get("contactMechId");
    
                //Create postal address purposes
                List<String> postalContactMechPurposes = new ArrayList<String>();
                postalContactMechPurposes.add("BILLING_LOCATION");
                postalContactMechPurposes.add("GENERAL_LOCATION");
                postalContactMechPurposes.add("PAYMENT_LOCATION");
    
                serviceCtx.clear();
                serviceCtx.put("partyId", partyId);
                serviceCtx.put("contactMechId", postalContactMechId);
                serviceCtx.put("userLogin", userLogin);
                for(String postalContactMechPurpose : postalContactMechPurposes) {
                    serviceCtx.put("contactMechPurposeTypeId", postalContactMechPurpose);
                    result = dispatcher.runSync("createPartyContactMechPurpose", serviceCtx);
                    if(!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }
            }

            result.clear();
            serviceCtx.clear();
            if (UtilValidate.isNotEmpty(context.get("infoString"))) {
                // Create Company Email Address.
                serviceCtx.put("partyId", partyId);
                serviceCtx.put("emailAddress", (String) context.get("infoString"));
                serviceCtx.put("userLogin", userLogin);
                if (UtilValidate.isNotEmpty(emailContactMechId)) {
                    serviceCtx.put("contactMechId", emailContactMechId);
                    result = dispatcher.runSync("updatePartyEmailAddress", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                } else  {
                    result = dispatcher.runSync("createPartyEmailAddress", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    emailContactMechId = (String) result.get("contactMechId");

                  //Create email purposes 
                    serviceCtx.clear();
                    serviceCtx.put("partyId", partyId);
                    serviceCtx.put("userLogin", userLogin);
                    serviceCtx.put("contactMechId", emailContactMechId);
                    serviceCtx.put("contactMechPurposeTypeId", "PRIMARY_EMAIL");
                    result = dispatcher.runSync("createPartyContactMechPurpose",serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }
            }

            serviceCtx.clear();
            if (UtilValidate.isNotEmpty(context.get("contactNumber"))) {
                contactNumberMap = MagentoHelper.getMapForContactNumber((String) context.get("contactNumber"));
                serviceCtx = dctx.getModelService("createPartyTelecomNumber").makeValid(contactNumberMap, ModelService.IN_PARAM);
                serviceCtx.put("partyId", partyId);
                serviceCtx.put("userLogin", userLogin);
                if (UtilValidate.isNotEmpty(telecomContactMechId)) {
                    serviceCtx.put("contactMechId", telecomContactMechId);
                    result = dispatcher.runSync("updatePartyTelecomNumber", serviceCtx);
                } else {
                    serviceCtx.put("contactMechPurposeTypeId", "PRIMARY_PHONE");
                    result = dispatcher.runSync("createPartyTelecomNumber", serviceCtx);
                }
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            } else if (UtilValidate.isNotEmpty(telecomContactMechId)) {
                //Remove previous contact number if any
                serviceCtx.clear();
                serviceCtx.put("userLogin", userLogin);
                serviceCtx.put("partyId", partyId);
                serviceCtx.put("contactMechId", telecomContactMechId);
                result = dispatcher.runSync("updatePartyTelecomNumber", serviceCtx);
                if (ServiceUtil.isError(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            }
            result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoCompanyIsCreatedSuccessfully", locale));
            result.put("partyId", partyId);
        } catch (GenericServiceException e) {
            Debug.logError(e.getMessage(), module);
            ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    public static Map<String, Object> createProdCatalogAndProductStoreCatalog(DispatchContext dctx, Map<String, Object>context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            serviceCtx = dctx.getModelService("createProdCatalog").makeValid(context, ModelService.IN_PARAM);
            result = dispatcher.runSync("createProdCatalog", serviceCtx);
            if (!ServiceUtil.isSuccess(result)) {
                Debug.logError(ServiceUtil.getErrorMessage(result), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
            }
            String prodCatalogId = (String) result.get("prodCatalogId");
            if (UtilValidate.isNotEmpty(prodCatalogId)) {
                serviceCtx = dctx.getModelService("createProductStoreCatalog").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                result = dispatcher.runSync("createProductStoreCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento browse root category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento Browse Root");
                serviceCtx.put("longDescription", "Magento Catalog Primary Browse Root Category");
                serviceCtx.put("productCategoryTypeId", "CATALOG_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                String productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento view allow category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento View Allow Category");
                serviceCtx.put("longDescription", "Magento Catalog View Allow Category");
                serviceCtx.put("productCategoryTypeId", "CATALOG_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_VIEW_ALLW");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento purchase allow category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento Purchase Allow Category");
                serviceCtx.put("longDescription", "Magento Catalog Purchase Allow Category");
                serviceCtx.put("productCategoryTypeId", "CATALOG_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_PURCH_ALLW");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento search category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento Search");
                serviceCtx.put("longDescription", "Magento Catalog Search Category");
                serviceCtx.put("productCategoryTypeId", "SEARCH_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_SEARCH");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento quick add category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento Quick Add");
                serviceCtx.put("longDescription", "Magento Catalog Quick Add Category");
                serviceCtx.put("productCategoryTypeId", "QUICKADD_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_QUICK_ADD");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }

                //Create magento promotions category
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("categoryName", "Magento Promotions");
                serviceCtx.put("longDescription", "Magento Promotions Category");
                serviceCtx.put("productCategoryTypeId", "CATALOG_CATEGORY");
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("createProductCategory", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productCategoryId = (String) result.get("productCategoryId");
                serviceCtx.clear();
                result.clear();
                serviceCtx.put("prodCatalogId", prodCatalogId);
                serviceCtx.put("productCategoryId", productCategoryId);
                serviceCtx.put("prodCatalogCategoryTypeId", "PCCT_PROMOTIONS");
                serviceCtx.put("fromDate", UtilDateTime.nowTimestamp());
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("addProductCategoryToProdCatalog", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            }
        } catch (GenericServiceException gse) {
            Debug.logError(gse.getMessage(), module);
            return ServiceUtil.returnError(gse.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> createUpdateProductStore(DispatchContext dctx, Map<String, Object>context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Locale locale = (Locale) context.get("locale");
        String partyId = (String) context.get("partyId");
        String productStoreId = (String) context.get("productStoreId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            if (UtilValidate.isNotEmpty(productStoreId)) {
                GenericValue productStore = delegator.findOne("ProductStore", false, UtilMisc.toMap("productStoreId", productStoreId));
                if (UtilValidate.isNotEmpty(productStore)) {
                    serviceCtx = dctx.getModelService("updateProductStore").makeValid(productStore, ModelService.IN_PARAM);
                    serviceCtx.put("storeName", (String) context.get("storeName"));
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("updateProductStore", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoStoreIsUpdatedSuccessfully", locale));
                    result.put("partyId", partyId);
                    result.put("productStoreId", productStoreId);
                }
            } else {
                serviceCtx = dctx.getModelService("createProductStore").makeValid(context, ModelService.IN_PARAM);

                //Add basic setting values for product store
                serviceCtx.put("isDemoStore", "N");
                serviceCtx.put("requireInventory", "N");
                serviceCtx.put("isImmediatelyFulfilled", "N");
                serviceCtx.put("prodSearchExcludeVariants", "Y");
                serviceCtx.put("shipIfCaptureFails", "N");
                serviceCtx.put("retryFailedAuths", "Y");
                serviceCtx.put("explodeOrderItems", "N");
                serviceCtx.put("checkGcBalance", "Y");
                serviceCtx.put("autoApproveInvoice", "Y");
                serviceCtx.put("autoApproveOrder", "Y");
                serviceCtx.put("autoApproveReviews", "N");
                serviceCtx.put("allowPassword", "Y");
                serviceCtx.put("usePrimaryEmailUsername", "Y");
                serviceCtx.put("manualAuthIsCapture", "Y");
                serviceCtx.put("requireCustomerRole", "Y");
                serviceCtx.put("daysToCancelNonPay", Long.valueOf("0"));
                serviceCtx.put("storeCreditAccountEnumId", "FIN_ACCOUNT");
                serviceCtx.put("defaultSalesChannelEnumId", "WEB_SALES_CHANNEL");
                serviceCtx.put("reqReturnInventoryReceive", "Y");
                serviceCtx.put("headerApprovedStatus", "ORDER_APPROVED");
                serviceCtx.put("itemApprovedStatus", "ITEM_APPROVED");
                serviceCtx.put("digitalItemApprovedStatus", "ITEM_APPROVED");
                serviceCtx.put("headerDeclinedStatus", "ORDER_REJECTED");
                serviceCtx.put("itemDeclinedStatus", "ITEM_REJECTED");
                serviceCtx.put("headerCancelStatus", "ORDER_CANCELLED");
                serviceCtx.put("itemCancelStatus", "ITEM_CANCELLED");

                serviceCtx.put("autoSaveCart", "N");
                serviceCtx.put("showCheckoutGiftOptions", "N");
                serviceCtx.put("prorateShipping", "N");
                serviceCtx.put("prorateTaxes", "N");
                serviceCtx.put("checkInventory", "Y");
                serviceCtx.put("balanceResOnOrderCreation", "N");
                serviceCtx.put("payToPartyId", partyId);
                serviceCtx.put("defaultCurrencyUomId", "USD");
                serviceCtx.put("reserveInventory", "Y");
                
                result = dispatcher.runSync("createProductStore", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productStoreId = (String) result.get("productStoreId");
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    GenericValue magentoConfiguration = EntityUtil.getFirst(delegator.findList("MagentoConfiguration", EntityCondition.makeCondition("enumId", "MAGENTO_SALE_CHANNEL"), null, null, null, false));
                    if (UtilValidate.isNotEmpty(magentoConfiguration) && UtilValidate.isEmpty(magentoConfiguration.getString("productStoreId"))) {
                    serviceCtx = dctx.getModelService("createUpdateMagentoConfiguration").makeValid(magentoConfiguration, ModelService.IN_PARAM);
                    serviceCtx.put("enumId", "MAGENTO_SALE_CHANNEL");
                    serviceCtx.put("productStoreId", productStoreId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createUpdateMagentoConfiguration", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    }

                    serviceCtx.clear();
                    serviceCtx.put("catalogName", "Demo Magento Catalog");
                    serviceCtx.put("productStoreId", productStoreId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createProdCatalogAndProductStoreCatalog", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }

                    serviceCtx.clear();
                    serviceCtx.put("productStoreId", productStoreId);
                    serviceCtx.put("partyId", "_NA_");
                    serviceCtx.put("roleTypeId", "CARRIER");
                    serviceCtx.put("shipmentMethodTypeId", "FLAT_RATE");
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createProductStoreShipMeth", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }
                result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoStoreIsCreatedSuccessfully", locale));
                result.put("partyId", partyId);
                result.put("productStoreId", productStoreId);
            }
        } catch (GenericServiceException e) {
             Debug.logError(e.getMessage(), module);
        } catch (GenericEntityException gee) {
            Debug.logError(gee.getMessage(), module);
        }
        return result;
    }
    public static Map<String, Object> createUpdateStoreInformation(DispatchContext dctx, Map<String, Object>context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId = null;
        String productStoreId = null;
        try {
            serviceCtx = dctx.getModelService("createUpdateCompany").makeValid(context, ModelService.IN_PARAM);
            result = dispatcher.runSync("createUpdateCompany", serviceCtx);
            if (!ServiceUtil.isSuccess(result)) {
                Debug.logError(ServiceUtil.getErrorMessage(result), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
            }
            partyId = (String) result.get("partyId");
            if (UtilValidate.isNotEmpty(partyId)) {
                serviceCtx = dctx.getModelService("createUpdateProductStore").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("partyId", partyId);
                result = dispatcher.runSync("createUpdateProductStore", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productStoreId = (String) result.get("productStoreId");
                serviceCtx.clear();

                List<GenericValue> glAccountOrganizationList = delegator.findList("GlAccountOrganization", EntityCondition.makeCondition("organizationPartyId", partyId), null, null, null, false);;
                if (UtilValidate.isEmpty(glAccountOrganizationList)) {
                serviceCtx.put("userLogin", userLogin);
                serviceCtx.put("partyId", partyId);
                result = dispatcher.runSync("setupDefaultGeneralLedger", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                }
            }
        } catch (GenericEntityException gee) {
            Debug.logInfo(gee.getMessage(), module);
            ServiceUtil.returnError(gee.getMessage());
        } catch (GenericServiceException gse) {
            Debug.logInfo(gse.getMessage(), module);
            ServiceUtil.returnError(gse.getMessage());
        }
        result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoStoreInformationIsUpdatedSuccessfully", locale));
        result.put("partyId", partyId);
        result.put("productStoreId", productStoreId);
        return result;
    }
    public static Map<String, Object> createUpdateWarehouse (DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Map<String, String> contactNumberMap = new HashMap<String, String>();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId = (String) context.get("partyId");
        String productStoreId = (String) context.get("productStoreId");
        String facilityId = (String) context.get("facilityId");
        String postalContactMechId = (String) context.get("facilityPostalContactMechId");
        String telecomContactMechId = (String) context.get("facilityTelecomContactMechId");


        try {
            if (UtilValidate.isNotEmpty(facilityId)) { 
                serviceCtx = dctx.getModelService("updateFacility").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("ownerPartyId", partyId);
                result = dispatcher.runSync("updateFacility", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            } else {
                serviceCtx = dctx.getModelService("createFacility").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("facilityTypeId", "WAREHOUSE");
                serviceCtx.put("ownerPartyId", partyId);
                serviceCtx.put("defaultWeightUomId", "WT_lb");
                result = dispatcher.runSync("createFacility", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                facilityId = (String) result.get("facilityId");
                serviceCtx.clear();
                result.clear();

                serviceCtx.put("productStoreId", productStoreId);
                serviceCtx.put("inventoryFacilityId", facilityId);
                serviceCtx.put("userLogin", userLogin);
                if (UtilValidate.isEmpty(context.get("checkInventory"))) {
                    serviceCtx.put("checkInventory", "Y");
                } else {
                    serviceCtx.put("checkInventory", context.get("checkInventory"));
                }
                if (UtilValidate.isEmpty(context.get("balanceResOnOrderCreation"))) {
                    serviceCtx.put("balanceResOnOrderCreation", "N");
                } else {
                    serviceCtx.put("balanceResOnOrderCreation", context.get("balanceResOnOrderCreation"));
                }
                serviceCtx.put("reserveOrderEnumId", context.get("reserveOrderEnumId"));
                result = dispatcher.runSync("updateProductStore", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            }
            serviceCtx.clear();
            result.clear();
            if (UtilValidate.isNotEmpty(postalContactMechId)) {
                serviceCtx = dctx.getModelService("updateFacilityPostalAddress").makeValid(context, ModelService.IN_PARAM);
                serviceCtx.put("contactMechId", postalContactMechId);
                serviceCtx.put("toName", (String) context.get("facilityName"));
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("updateFacilityPostalAddress", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            } else {
                if (UtilValidate.isNotEmpty(context.get("isWarehouseAddressSameAsCompanyAddress"))) {
                    GenericValue postalAddress = PartyWorker.findPartyLatestPostalAddress(partyId, delegator);
                    serviceCtx = dctx.getModelService("createFacilityPostalAddress").makeValid(UtilMisc.toMap(postalAddress), ModelService.IN_PARAM);
                    serviceCtx.remove("contactMechId");
                } else if (UtilValidate.isNotEmpty(context.get("city")) && UtilValidate.isNotEmpty("countryGeoId") && UtilValidate.isNotEmpty("address1")) {
                    serviceCtx = dctx.getModelService("createFacilityPostalAddress").makeValid(context, ModelService.IN_PARAM);
                }
                if (UtilValidate.isNotEmpty(serviceCtx)) {
                    serviceCtx.put("userLogin", userLogin);
                    serviceCtx.put("facilityId", facilityId);
                    serviceCtx.put("toName", (String) context.get("facilityName"));
                    result = dispatcher.runSync("createFacilityPostalAddress", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    postalContactMechId = (String) result.get("contactMechId");
                    serviceCtx.clear();
                    result.clear();

                    //Create Facility Postal Contact Mech Purpose.
                    serviceCtx.put("contactMechId", postalContactMechId);
                    serviceCtx.put("facilityId", facilityId);
                    serviceCtx.put("userLogin", userLogin);

                    List<String> postalContactMechPurpose = new ArrayList<String>();
                    postalContactMechPurpose.add("SHIP_ORIG_LOCATION");
                    postalContactMechPurpose.add("SHIPPING_LOCATION");
                    for(String contactMechPurpose : postalContactMechPurpose) {
                        serviceCtx.put("contactMechPurposeTypeId", contactMechPurpose);
                        result = dispatcher.runSync("createFacilityContactMechPurpose", serviceCtx);
                        if(!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }
                    }
                }
            }
            serviceCtx.clear();

            if (UtilValidate.isNotEmpty(context.get("contactNumber"))) {
                //Create Facility Telecom Number.
                contactNumberMap = MagentoHelper.getMapForContactNumber((String) context.get("contactNumber"));
                serviceCtx = dctx.getModelService("createFacilityTelecomNumber").makeValid(contactNumberMap, ModelService.IN_PARAM);
                serviceCtx.put("facilityId", facilityId);
                serviceCtx.put("userLogin", userLogin);
                if (UtilValidate.isNotEmpty(telecomContactMechId)) {
                    serviceCtx.put("contactMechId", telecomContactMechId);
                    result = dispatcher.runSync("updateFacilityTelecomNumber", serviceCtx);
                } else {
                    serviceCtx.put("contactMechPurposeTypeId", "PRIMARY_PHONE");
                    result = dispatcher.runSync("createFacilityTelecomNumber", serviceCtx);
                    if(!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    telecomContactMechId = (String) result.get("contactMechId");
                    serviceCtx.clear();
                    contactNumberMap.clear();

                    //Create telecom contact mech purposes
                    serviceCtx.put("userLogin", userLogin);
                    serviceCtx.put("facilityId", facilityId);
                    serviceCtx.put("contactMechId", telecomContactMechId);
                    List<String> telecomContactMechPurpose = new ArrayList<String>();
                    telecomContactMechPurpose.add("PHONE_SHIPPING");
                    telecomContactMechPurpose.add("PHONE_SHIP_ORIG");
                    for(String contactMechPurpose : telecomContactMechPurpose) {
                        serviceCtx.put("contactMechPurposeTypeId", contactMechPurpose);
                        result = dispatcher.runSync("createFacilityContactMechPurpose", serviceCtx);
                        if(!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }
                    }
                }
            } else if (UtilValidate.isNotEmpty(telecomContactMechId)) {
                //Remove previous contact number if any
                serviceCtx.put("facilityId", facilityId);
                serviceCtx.put("contactMechId", telecomContactMechId);
                serviceCtx.put("userLogin", userLogin);
                result = dispatcher.runSync("deleteFacilityContactMech", serviceCtx);
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
            }
            serviceCtx.clear();

            result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoWarehouseInformationIsUpdatedSuccessfully", locale));
            result.put("facilityId", facilityId);
            result.put("partyId", partyId);
            result.put("productStoreId", productStoreId);

        } catch (GenericServiceException gse) {
            Debug.logError(gse.getMessage(), module);
            return ServiceUtil.returnError(gse.getMessage());
        }
        return result;
    }

    public static Map<String, Object> createRemoveProductStoreShipMeth (DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        String productStoreShipMethId = (String) context.get("productStoreShipMethId");
        Locale locale = (Locale) context.get("locale");
        List<String> shipmentMethodTypeIdList = UtilGenerics.toList(context.get("shipmentMethodTypeId"));
        String successMessage = null;
        try {
            if (UtilValidate.isNotEmpty(productStoreShipMethId)) {
                serviceCtx = dctx.getModelService("removeProductStoreShipMeth").makeValid(context, ModelService.IN_PARAM);
                dispatcher.runSync("removeProductStoreShipMeth", serviceCtx);
                successMessage = UtilProperties.getMessage(resource, "MagentoShippingMethodIsRemovedSuccessfully", locale);
            } else {
                if (UtilValidate.isNotEmpty(shipmentMethodTypeIdList)) {
                    for (String shipmentMethodTypeId : shipmentMethodTypeIdList) {
                        serviceCtx = dctx.getModelService("createProductStoreShipMeth").makeValid(context, ModelService.IN_PARAM);
                        serviceCtx.put("shipmentMethodTypeId", shipmentMethodTypeId);
                        result = dispatcher.runSync("createProductStoreShipMeth", serviceCtx);
                    }
                    successMessage = UtilProperties.getMessage(resource, "MagentoShippingMethodsAreAddedSuccessfully", locale);
                }
                
            }
            if (!ServiceUtil.isSuccess(result)) {
                Debug.logError(ServiceUtil.getErrorMessage(result), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
            }
        } catch (GenericServiceException gse) {
            Debug.logInfo(gse.getMessage(), module);
            ServiceUtil.returnError(gse.getMessage());
        }
        result = ServiceUtil.returnSuccess(successMessage);
        result.put("partyId", (String) context.get("partyId"));
        result.put("productStoreId", (String) context.get("productStoreId"));
        return result;
    }
    public static Map<String, Object> createUpdateShipmentGatewayConfig (DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        String shipmentGatewayConfigId = (String) context.get("shipmentGatewayConfigId");
        String carrierPartyId = (String) context.get("partyId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String serviceName = null;
        String shipmentGatewayConfTypeId = null;
        try {
            if (UtilValidate.isNotEmpty(shipmentGatewayConfigId)) {
                if (UtilValidate.isNotEmpty(carrierPartyId)) {
                    if ("DHL".equalsIgnoreCase(carrierPartyId)) {
                        serviceName = "updateShipmentGatewayDhl";
                    } else if ("FEDEX".equalsIgnoreCase(carrierPartyId)) {
                        serviceName = "updateShipmentGatewayFedex";
                    } else if ("UPS".equalsIgnoreCase(carrierPartyId)) {
                        serviceName = "updateShipmentGatewayUps";
                    } else if ("USPS".equalsIgnoreCase(carrierPartyId)) {
                        serviceName = "updateShipmentGatewayUsps";
                    }
                    serviceCtx = dctx.getModelService(serviceName).makeValid(context, ModelService.IN_PARAM);
                    if ("FEDEX".equalsIgnoreCase(carrierPartyId)) {
                        serviceCtx.put("accessUserKey", (String) context.get("accessUserId"));
                        serviceCtx.put("accessUserPwd", (String) context.get("accessPassword"));
                    }
                    result = dispatcher.runSync(serviceName, serviceCtx);
                }
            } else {
                if (UtilValidate.isNotEmpty(carrierPartyId)) {
                    if ("DHL".equalsIgnoreCase(carrierPartyId)) {
                        shipmentGatewayConfTypeId = "DHL";
                        serviceName = "createShipmentGatewayConfigDhl";
                    } else if ("FEDEX".equalsIgnoreCase(carrierPartyId)) {
                        shipmentGatewayConfTypeId = "FEDEX";
                        serviceName = "createShipmentGatewayConfigFedex";
                    } else if ("UPS".equalsIgnoreCase(carrierPartyId)) {
                        shipmentGatewayConfTypeId = "UPS";
                        serviceName = "createShipmentGatewayConfigUps";
                    } else if ("USPS".equalsIgnoreCase(carrierPartyId)) {
                        shipmentGatewayConfTypeId = "USPS";
                        serviceName = "createShipmentGatewayConfigUsps";
                    }
                    serviceCtx.put("shipmentGatewayConfTypeId", shipmentGatewayConfTypeId);
                    serviceCtx.put("description", carrierPartyId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createShipmentGatewayConfig", serviceCtx);

                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    shipmentGatewayConfigId = (String) result.get("shipmentGatewayConfigId");
                    if (UtilValidate.isNotEmpty(shipmentGatewayConfigId)) {
                        serviceCtx.clear();
                        serviceCtx = dctx.getModelService(serviceName).makeValid(context, ModelService.IN_PARAM);
                        serviceCtx.put("shipmentGatewayConfigId", shipmentGatewayConfigId);
                        if ("FEDEX".equalsIgnoreCase(carrierPartyId)) {
                            serviceCtx.put("accessUserKey", (String) context.get("accessUserId"));
                            serviceCtx.put("accessUserPwd", (String) context.get("accessPassword"));
                        }
                        result = dispatcher.runSync(serviceName, serviceCtx);
                    }
                }
            }
            if (!ServiceUtil.isSuccess(result)) {
                Debug.logError(ServiceUtil.getErrorMessage(result), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
            }
        } catch (GenericServiceException gse) {
            Debug.logInfo(gse.getMessage(), module);
            return ServiceUtil.returnError(gse.getMessage());
        }
        result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoShippingGatewayConfigurationIsUpdatedSuccessfully", locale));
        result.put("partyId", carrierPartyId);
        result.put("productStoreId", (String) context.get("productStoreId"));
        result.put("shipmentGatewayConfigId", shipmentGatewayConfigId);
        return result;
    }
    public static Map<String, Object> setupDefaultGeneralLedger (DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        String partyId = (String) context.get("partyId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String glJournalId = null;
        try {
            if (UtilValidate.isNotEmpty(partyId)) {
                EntityCondition cond = EntityCondition.makeCondition (
                        EntityCondition.makeCondition("organizationPartyId", partyId),
                        EntityCondition.makeCondition("glJournalName", "Suspense transactions")
                        );
                List<GenericValue> glJournalList = delegator.findList("GlJournal", cond, null, null, null, false);
                if (UtilValidate.isEmpty(glJournalList)) {
                    serviceCtx.put("glJournalName", "Suspense transactions");
                    serviceCtx.put("organizationPartyId", partyId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createGlJournal", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    glJournalId = (String) result.get("glJournalId");
                }

                GenericValue partyAcctgPreference = delegator.findOne("PartyAcctgPreference", false, UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isEmpty(partyAcctgPreference)) {
                    serviceCtx.clear();
                    serviceCtx.put("partyId", partyId);
                    serviceCtx.put("taxFormId", "US_IRS_1120");
                    serviceCtx.put("cogsMethodId", "COGS_AVG_COST");
                    serviceCtx.put("invoiceSequenceEnumId", "INVSQ_ENF_SEQ");
                    serviceCtx.put("invoiceIdPrefix", "CI");
                    serviceCtx.put("quoteSequenceEnumId", "INVSQ_ENF_SEQ");
                    serviceCtx.put("quoteIdPrefix", "CQ");
                    serviceCtx.put("orderSequenceEnumId", "INVSQ_ENF_SEQ");
                    serviceCtx.put("orderIdPrefix", "CO");
                    serviceCtx.put("baseCurrencyUomId", "USD");
                    serviceCtx.put("errorGlJournalId", glJournalId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createPartyAcctgPreference", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }

                URL outputPath = MagentoHelper.getTempDataFileUrlToImport(delegator, partyId);
                if (UtilValidate.isNotEmpty(outputPath)) {
                    result = dispatcher.runSync("parseEntityXmlFile", UtilMisc.toMap("url", outputPath, "userLogin", userLogin));
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }
            }
        } catch (GenericServiceException gse) {
            Debug.logInfo(gse.getMessage(), module);
            return ServiceUtil.returnError(gse.getMessage());
        } catch (GenericEntityException gee) {
            Debug.logInfo(gee.getMessage(), module);
            return ServiceUtil.returnError(gee.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> setupMagentoStore (DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> serviceCtx = new HashMap<String, Object>();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String magentoConfigurationId = null;
        String productStoreId = null;
        String partyId = null;
        try {
            GenericValue magentoConfiguration = MagentoHelper.getMagentoConfiguration(delegator);
            magentoConfigurationId = magentoConfiguration.getString("magentoConfigurationId");

            MagentoClient magentoClient = new MagentoClient(dispatcher, delegator);
            List<StoreConfigEntity> storeConfigList = magentoClient.getStoreConfig();
            for(StoreConfigEntity storeConfig : storeConfigList) {
                StoreAddressEntity storeAddress = storeConfig.getAddress();
                String productStoreName = storeConfig.getStoreGroupName();
                serviceCtx.put("userLogin", userLogin);
                serviceCtx.put("storeName", productStoreName);
                if (UtilValidate.isEmpty(partyId)) {
                    serviceCtx.put("groupName", "Magento Company");
                    serviceCtx.put("contactNumber", storeAddress.getContactNumber());
                    serviceCtx.put("infoString", storeAddress.getEmailAddress());
                    result = dispatcher.runSync("createUpdateStoreInformation", serviceCtx);
                    partyId = (String) result.get("partyId");
                } else {
                    serviceCtx.put("partyId", partyId);
                    result = dispatcher.runSync("createUpdateProductStore", serviceCtx);
                }
                if (!ServiceUtil.isSuccess(result)) {
                    Debug.logError(ServiceUtil.getErrorMessage(result), module);
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                }
                productStoreId = (String) result.get("productStoreId");
                result.clear();
                serviceCtx.clear();

                if (UtilValidate.isNotEmpty(partyId) && UtilValidate.isNotEmpty(storeAddress.getAddress())) {
                    String textData = null;
                    if (UtilValidate.isNotEmpty(storeAddress.getCountry())) {
                        EntityCondition cond = EntityCondition.makeCondition(
                                EntityCondition.makeCondition("geoCode", storeAddress.getCountry()),
                                EntityCondition.makeCondition("geoTypeId", "COUNTRY")
                                );
                        GenericValue geo = EntityUtil.getFirst(delegator.findList("Geo", cond, null, null, null, false));
                        if (UtilValidate.isNotEmpty(geo) && UtilValidate.isNotEmpty(geo.getString("geoName"))) {
                            textData = geo.getString("geoName");
                        }
                    }
                    textData = storeAddress.getAddress()+", "+textData;
                    serviceCtx.put("textData", textData);
                    serviceCtx.put("partyContentTypeId", "MAGENTO_STORE_ADDR");
                    serviceCtx.put("partyId", partyId);
                    serviceCtx.put("userLogin", userLogin);
                    result = dispatcher.runSync("createPartyTextContent", serviceCtx);
                    if (!ServiceUtil.isSuccess(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                    serviceCtx.clear();
                }

                if (UtilValidate.isNotEmpty(productStoreId)) {
                    if (UtilValidate.isNotEmpty(magentoConfigurationId)) {
                        serviceCtx.put("magentoConfigurationId", magentoConfigurationId);
                        serviceCtx.put("productStoreId", productStoreId);
                        serviceCtx.put("magentoDefaultStoreId", storeConfig.getDefaultStoreId());
                        serviceCtx.put("magentoStoreGroupId", storeConfig.getGroupId());
                        serviceCtx.put("magentoRootCategoryId", storeConfig.getRootCategoryId());
                        serviceCtx.put("userLogin", userLogin);
                        result = dispatcher.runSync("createMagentoProductStore", serviceCtx);
                        if (!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }
                        serviceCtx.clear();
                    }

                    StoreShippingOriginEntity shippingOrigin = storeConfig.getShippingOrigin();
                    if (UtilValidate.isNotEmpty(shippingOrigin)) {
                        serviceCtx.put("productStoreId", productStoreId);
                        serviceCtx.put("facilityName", productStoreName+" Warehouse");
                        serviceCtx.put("partyId", partyId);
                        serviceCtx.put("city", shippingOrigin.getCity());
                        serviceCtx.put("countryGeoId", MagentoHelper.getCountryGeoId(shippingOrigin.getCountryId(), delegator));
                        serviceCtx.put("stateProvinceGeoId", MagentoHelper.getStateGeoId(shippingOrigin.getRegionCode(), shippingOrigin.getCountryId(), delegator));
                        serviceCtx.put("postalCode", shippingOrigin.getPostcode());
                        serviceCtx.put("address1", shippingOrigin.getStreetLine1());
                        serviceCtx.put("address2", shippingOrigin.getStreetLine2());
                        serviceCtx.put("userLogin", userLogin);
                        result = dispatcher.runSync("createUpdateWarehouse", serviceCtx);
                        if (!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }

                        serviceCtx.clear();
                        result.clear();
                    }
                    StoreShippingMethodsEntityArray storeShippingMethodsEntityArray = storeConfig.getShippingMethods();
                    List<StoreShippingMethodsEntity> shippingMethodListByCarrier = storeShippingMethodsEntityArray.getComplexObjectArray();
                    for (StoreShippingMethodsEntity shippingMethod : shippingMethodListByCarrier) {
                        String carrierCode = shippingMethod.getCarrierCode();

                        serviceCtx.put("partyId", shippingMethod.getCarrierCode().toUpperCase());
                        serviceCtx.put("productStoreId", productStoreId);
                        serviceCtx.put("userLogin", userLogin);
                        serviceCtx.put("accessPassword", shippingMethod.getPassword());
                        serviceCtx.put("connectUrl", shippingMethod.getGatewayUrl());
                        serviceCtx.put("accessLicenseNumber", shippingMethod.getAccessLicenseNumber());
                        serviceCtx.put("accessAccountNbr", shippingMethod.getAccount());
                        serviceCtx.put("accessMeterNumber", shippingMethod.getMeterNumber());

                        String accessUserId = null;
                        String shipmentGatewayConfigId = null;
                        if ("UPS".equalsIgnoreCase(carrierCode)) {
                            accessUserId = shippingMethod.getUsername();
                            shipmentGatewayConfigId = "UPS_CONFIG";
                        } else if ("USPS".equalsIgnoreCase(carrierCode)) {
                            accessUserId = shippingMethod.getUserId();
                            shipmentGatewayConfigId = "USPS_CONFIG";
                        } else if ("DHL".equalsIgnoreCase(carrierCode)) {
                            accessUserId = shippingMethod.getId();
                            shipmentGatewayConfigId = "DHL_CONFIG";
                        } else if ("FEDEX".equalsIgnoreCase(carrierCode)) {
                            accessUserId = shippingMethod.getKey();
                            shipmentGatewayConfigId = "FEDEX_CONFIG";
                        } else {
                            continue;
                        }
                        serviceCtx.put("shipmentGatewayConfigId", shipmentGatewayConfigId);
                        serviceCtx.put("accessUserId", accessUserId);
                        result = dispatcher.runSync("createUpdateShipmentGatewayConfig", serviceCtx);
                        if (!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }
                        serviceCtx.clear();

                        if (UtilValidate.isNotEmpty(carrierCode)) {
                            if ("UPS".equalsIgnoreCase(carrierCode)) {
                                serviceCtx.put("serviceName", "");
                            } else if ("USPS".equalsIgnoreCase(carrierCode)) {
                                serviceCtx.put("serviceName", "");
                            } else if ("DHL".equalsIgnoreCase(carrierCode)) {
                                serviceCtx.put("serviceName", "");
                            } else if ("FEDEX".equalsIgnoreCase(carrierCode)) {
                                serviceCtx.put("serviceName", "");
                            } else  {
                                continue;
                            }
                            serviceCtx.put("shipmentGatewayConfigId", shipmentGatewayConfigId);
                            serviceCtx.put("partyId", shippingMethod.getCarrierCode().toUpperCase());
                            serviceCtx.put("productStoreId", productStoreId);
                            serviceCtx.put("roleTypeId", "CARRIER");
                        }
                        if (UtilValidate.isNotEmpty(shippingMethod.getAllowedMethods())) {
                            List<String> shipmentMethodTypeIdList = StringUtil.split(shippingMethod.getAllowedMethods(), ",");
                            EntityCondition cond = EntityCondition.makeCondition(
                                    EntityCondition.makeCondition("systemResourceId", "Magento"),
                                    EntityCondition.makeCondition("systemPropertyId", EntityOperator.IN, shipmentMethodTypeIdList)
                                    );
                            shipmentMethodTypeIdList = EntityUtil.getFieldListFromEntityList(delegator.findList("SystemProperty", cond, null, null, null, false), "systemPropertyValue", true);
                            if (UtilValidate.isNotEmpty(shipmentMethodTypeIdList)) {
                                serviceCtx.put("shipmentMethodTypeId", shipmentMethodTypeIdList);
                            }
                        }
                        serviceCtx.put("userLogin", userLogin);
                        result = dispatcher.runSync("createRemoveProductStoreShipMeth", serviceCtx);
                        if (!ServiceUtil.isSuccess(result)) {
                            Debug.logError(ServiceUtil.getErrorMessage(result), module);
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                        }
                        serviceCtx.clear();

                    }
                }
            }
        } catch (GenericServiceException gse) {
            Debug.logInfo(gse.getMessage(), module);
            return ServiceUtil.returnError(gse.getMessage());
        } catch (GenericEntityException gee) {
            Debug.logInfo(gee.getMessage(), module);
            return ServiceUtil.returnError(gee.getMessage());
        } 
        result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "MagentoMagentoStoreInformationHasBeenImportedSuccessfully", locale));
        result.put("partyId", partyId);
        return result;
    }
}