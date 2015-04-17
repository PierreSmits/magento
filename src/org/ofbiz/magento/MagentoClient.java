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
package org.ofbiz.magento;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import magento.ArrayOfString;
import magento.CatalogCategoryInfo;
import magento.CatalogCategoryInfoRequestParam;
import magento.CatalogCategoryInfoResponseParam;
import magento.CatalogCategoryTree;
import magento.CatalogCategoryTreeRequestParam;
import magento.CatalogCategoryTreeResponseParam;
import magento.CatalogInventoryStockItemEntity;
import magento.CatalogInventoryStockItemEntityArray;
import magento.CatalogInventoryStockItemListRequestParam;
import magento.CatalogInventoryStockItemListResponseParam;
import magento.CatalogInventoryStockItemUpdateEntity;
import magento.CatalogInventoryStockItemUpdateRequestParam;
import magento.CatalogInventoryStockItemUpdateResponseParam;
import magento.CatalogProductAttributeMediaInfoRequestParam;
import magento.CatalogProductAttributeMediaInfoResponseParam;
import magento.CatalogProductAttributeMediaListRequestParam;
import magento.CatalogProductAttributeMediaListResponseParam;
import magento.CatalogProductEntity;
import magento.CatalogProductEntityArray;
import magento.CatalogProductImageEntity;
import magento.CatalogProductImageEntityArray;
import magento.CatalogProductInfoRequestParam;
import magento.CatalogProductInfoResponseParam;
import magento.CatalogProductListRequestParam;
import magento.CatalogProductListResponseParam;
import magento.CatalogProductRelationEntity;
import magento.CatalogProductRelationEntityArray;
import magento.CatalogProductRelationRequestParam;
import magento.CatalogProductRelationResponseParam;
import magento.CatalogProductReturnEntity;
import magento.DirectoryRegionEntity;
import magento.DirectoryRegionEntityArray;
import magento.DirectoryRegionListRequestParam;
import magento.DirectoryRegionListResponseParam;
import magento.EditSalesOrderAddressRequestParam;
import magento.EditSalesOrderAddressResponseParam;
import magento.Filters;
import magento.LoginParam;
import magento.LoginResponseParam;
import magento.MageApiModelServerWsiHandlerPortType;
import magento.MagentoService;
import magento.OrderItemIdQty;
import magento.OrderItemIdQtyArray;
import magento.SalesOrderAddressEntity;
import magento.SalesOrderCancelRequestParam;
import magento.SalesOrderCancelResponseParam;
import magento.SalesOrderCreditmemoCreateRequestParam;
import magento.SalesOrderCreditmemoCreateResponseParam;
import magento.SalesOrderCreditmemoData;
import magento.SalesOrderEntity;
import magento.SalesOrderHoldRequestParam;
import magento.SalesOrderHoldResponseParam;
import magento.SalesOrderInfoRequestParam;
import magento.SalesOrderInfoResponseParam;
import magento.SalesOrderInvoiceCaptureRequestParam;
import magento.SalesOrderInvoiceCaptureResponseParam;
import magento.SalesOrderInvoiceCreateRequestParam;
import magento.SalesOrderInvoiceCreateResponseParam;
import magento.SalesOrderInvoiceEntity;
import magento.SalesOrderInvoiceInfoRequestParam;
import magento.SalesOrderInvoiceInfoResponseParam;
import magento.SalesOrderListEntity;
import magento.SalesOrderListEntityArray;
import magento.SalesOrderListRequestParam;
import magento.SalesOrderListResponseParam;
import magento.SalesOrderShipmentAddTrackRequestParam;
import magento.SalesOrderShipmentAddTrackResponseParam;
import magento.SalesOrderShipmentCreateRequestParam;
import magento.SalesOrderShipmentCreateResponseParam;
import magento.SalesOrderUnholdRequestParam;
import magento.SalesOrderUnholdResponseParam;
import magento.SalesOrderAddressUpdateEntity;
import magento.StoreConfigEntity;
import magento.StoreConfigEntityArray;
import magento.StoreConfigRequestParam;
import magento.StoreConfigResponseParam;
import magento.StoreEntity;
import magento.StoreInfoRequestParam;
import magento.StoreInfoResponseParam;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;

public class MagentoClient {
    public static final String module = MagentoClient.class.getName();
    private static String soapUserName;
    private static String soapPassword;
    private static String magentoServiceWsdlLocation;
    private static MageApiModelServerWsiHandlerPortType port;
    private String sessionId;

    public MagentoClient (LocalDispatcher dispatcher, Delegator delegator) {
        GenericValue system = null;
        try {
            system = delegator.findOne("UserLogin", true, "userLoginId", "system");
            GenericValue magentoConfiguration = EntityUtil.getFirst((delegator.findList("MagentoConfiguration", null, null, null, null, false))); 
            if (UtilValidate.isNotEmpty(magentoConfiguration)) { 
                soapUserName = (String) magentoConfiguration.get("xmlRpcUserName");
                soapPassword = (String) magentoConfiguration.get("password");
                magentoServiceWsdlLocation = magentoConfiguration.getString("serverUrl");
                sessionId = getMagentoSession();
                port = getPort();
            }
        } catch (GenericEntityException gee) {
            Debug.logError(gee, module);
            system = delegator.makeValue("UserLogin");
            system.set("userLoginId", "system");
            system.set("partyId", "admin");
            system.set("isSystem", "Y");
        }
    }

    public static MageApiModelServerWsiHandlerPortType getPort() {
        URL url = null;
        MageApiModelServerWsiHandlerPortType port = null;
        try {
            url = new URL(magentoServiceWsdlLocation);
            QName serviceName = new QName("urn:Magento", "MagentoService");

            MagentoService mage = new MagentoService(url, serviceName);
            port = mage.getMageApiModelServerWsiHandlerPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Debug.logError(e.getMessage(), module);
        } 
        return port;
    }
    public String getMagentoSession() {
        if (UtilValidate.isEmpty(sessionId)) {
            LoginParam loginParams = new LoginParam();
            loginParams.setUsername(soapUserName);
            loginParams.setApiKey(soapPassword);
            MageApiModelServerWsiHandlerPortType port = getPort();
            LoginResponseParam loginResponseParam = port.login(loginParams);

            sessionId = loginResponseParam.getResult();
            Debug.logInfo("Got Magento session  with sessionId:" +sessionId, module);
        }
        return sessionId;
    }

    // Fetches sales order List from magento
    public List<SalesOrderListEntity> getSalesOrderList(Filters filters) {
        List<SalesOrderListEntity> salesOrderList = new ArrayList<SalesOrderListEntity>();

        SalesOrderListRequestParam salesOrderListRequestParam = new SalesOrderListRequestParam();
        salesOrderListRequestParam.setSessionId(sessionId);
        salesOrderListRequestParam.setFilters(filters);
        SalesOrderListResponseParam salesOrderListResponseParam = port.salesOrderList(salesOrderListRequestParam);
        SalesOrderListEntityArray salesOrderListEntityArray = salesOrderListResponseParam.getResult();
        salesOrderList = salesOrderListEntityArray.getComplexObjectArray();
        return salesOrderList;
    }

    // Fetches sales order info from magento
    public SalesOrderEntity getSalesOrderInfo(String orderIncrementId) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId.", module);
            return null;
        }
        SalesOrderInfoRequestParam salesOrderInfoRequestParam = new SalesOrderInfoRequestParam();
        salesOrderInfoRequestParam.setSessionId(sessionId);
        salesOrderInfoRequestParam.setOrderIncrementId(orderIncrementId);

        SalesOrderInfoResponseParam salesOrderListResponseParam = port.salesOrderInfo(salesOrderInfoRequestParam);
        SalesOrderEntity salesOrder = salesOrderListResponseParam.getResult();
        return salesOrder;
    }

    public CatalogInventoryStockItemEntity getCatalogInventoryStock (String sku) {
        if (UtilValidate.isEmpty(sku)) {
            Debug.logInfo("Empty product's sku.", module);
            return null;
        }
        CatalogInventoryStockItemListRequestParam catalogInventoryStockItemListRequestParam = new CatalogInventoryStockItemListRequestParam();
        catalogInventoryStockItemListRequestParam.setSessionId(sessionId);
        ArrayOfString productIds = new ArrayOfString();
        productIds.getComplexObjectArray().add(sku);
        catalogInventoryStockItemListRequestParam.setProductIds(productIds);
        CatalogInventoryStockItemListResponseParam catalogInventoryStockItemListResponseParam = port.catalogInventoryStockItemList(catalogInventoryStockItemListRequestParam);
        CatalogInventoryStockItemEntityArray catalogInventoryStockItemEntityArray = catalogInventoryStockItemListResponseParam.getResult();

        List<CatalogInventoryStockItemEntity>stockItemList = catalogInventoryStockItemEntityArray.getComplexObjectArray();
        if(UtilValidate.isNotEmpty(stockItemList)) {
            return stockItemList.get(0);
        }
        return null;
    }

    public List<DirectoryRegionEntity> getDirectoryRegionList(String countryGeoCode) {
        if (UtilValidate.isEmpty(countryGeoCode)) {
            Debug.logInfo("Empty countryGeoCode.", module);
            return null;
        }
        List<DirectoryRegionEntity> directoryRegionList = new ArrayList<DirectoryRegionEntity>();
        DirectoryRegionListRequestParam directoryRegionListRequestParam = new DirectoryRegionListRequestParam();
        directoryRegionListRequestParam.setSessionId(sessionId);
        directoryRegionListRequestParam.setCountry(countryGeoCode);

        DirectoryRegionListResponseParam directoryRegionListResponseParam = port.directoryRegionList(directoryRegionListRequestParam);
        DirectoryRegionEntityArray directorRegionEntityArray = directoryRegionListResponseParam.getResult();
        directoryRegionList = directorRegionEntityArray.getComplexObjectArray();
        return directoryRegionList;
    }
    public int cancelSalesOrder(String orderIncrementId) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId.", module);
            return 0;
        }
        int isCancelled = 0;
        SalesOrderCancelRequestParam salesOrderCancelRequestParam = new SalesOrderCancelRequestParam();
        salesOrderCancelRequestParam.setSessionId(sessionId);
        salesOrderCancelRequestParam.setOrderIncrementId(orderIncrementId);

        MageApiModelServerWsiHandlerPortType port = getPort();
        SalesOrderCancelResponseParam salesOrderCancelResponseParam = port.salesOrderCancel(salesOrderCancelRequestParam);
        isCancelled = salesOrderCancelResponseParam.getResult();

        return isCancelled;
    }
    public int holdSalesOrder(String orderIncrementId) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId.", module);
            return 0;
        }
        int isMarkedHold = 0;
        SalesOrderHoldRequestParam requestParam = new SalesOrderHoldRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setOrderIncrementId(orderIncrementId);

        SalesOrderHoldResponseParam responseParam = port.salesOrderHold(requestParam);
        isMarkedHold = responseParam.getResult();

        return isMarkedHold;
    }
    public int unholdSalesOrder(String orderIncrementId) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId.", module);
            return 0;
        }
        int isMarkedUnhold = 0;
        SalesOrderUnholdRequestParam requestParam = new SalesOrderUnholdRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setOrderIncrementId(orderIncrementId);

        SalesOrderUnholdResponseParam responseParam = port.salesOrderUnhold(requestParam);
        isMarkedUnhold = responseParam.getResult();

        return isMarkedUnhold;
    }
    public String createShipment(String orderIncrementId, Map<Integer, Double> orderItemQtyMap) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId. Not going to create shipment.", module);
            return null;
        }
        String shipmentIncrementId = null;
        SalesOrderShipmentCreateRequestParam salesOrderShipmentCreateRequestParam = new SalesOrderShipmentCreateRequestParam();

        OrderItemIdQtyArray orderItemIdQtyArray = new OrderItemIdQtyArray();
        if (UtilValidate.isNotEmpty(orderItemQtyMap)) {
            for (int orderItemId : orderItemQtyMap.keySet()) {
                OrderItemIdQty orderItemIdQty = new OrderItemIdQty();
                orderItemIdQty.setOrderItemId(orderItemId);
                orderItemIdQty.setQty(orderItemQtyMap.get(orderItemId));
                orderItemIdQtyArray.getComplexObjectArray().add(orderItemIdQty);
            }
        }

        salesOrderShipmentCreateRequestParam.setSessionId(sessionId);
        salesOrderShipmentCreateRequestParam.setOrderIncrementId(orderIncrementId);
        salesOrderShipmentCreateRequestParam.setEmail(1);
        salesOrderShipmentCreateRequestParam.setItemsQty(orderItemIdQtyArray);

        SalesOrderShipmentCreateResponseParam salesOrderShipmentCreateResponseParam = port.salesOrderShipmentCreate(salesOrderShipmentCreateRequestParam);
        shipmentIncrementId = salesOrderShipmentCreateResponseParam.getResult();
        return shipmentIncrementId;
    }
    public int addTrack(String shipmentIncrementId, String carrierPartyId, String carrierTitle, String trackNumber) {
        if (UtilValidate.isEmpty(shipmentIncrementId) || UtilValidate.isEmpty(carrierPartyId) || UtilValidate.isEmpty(carrierTitle) || UtilValidate.isEmpty(trackNumber)) {
            Debug.logInfo("Not getting complete information while going to add tracking code.", module);
            Debug.logInfo("shipmentIncrementId = "+shipmentIncrementId, module);
            Debug.logInfo("carrierPartyId = "+carrierPartyId, module);
            Debug.logInfo("carrierTitle = "+carrierTitle, module);
            Debug.logInfo("trackNumber = "+trackNumber, module);
            return 0;
        }
        int isTrackingCodeAdded = 0;
        SalesOrderShipmentAddTrackRequestParam requestParam = new SalesOrderShipmentAddTrackRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setShipmentIncrementId(shipmentIncrementId);
        requestParam.setCarrier(carrierPartyId);
        requestParam.setTitle(carrierTitle);
        requestParam.setTrackNumber(trackNumber);

        SalesOrderShipmentAddTrackResponseParam responseParam = port.salesOrderShipmentAddTrack(requestParam);
        isTrackingCodeAdded = responseParam.getResult();
        return isTrackingCodeAdded;
    }
    public String createInvoice(String orderIncrementId, Map<Integer, Double> orderItemQtyMap) {
        if (UtilValidate.isEmpty(orderIncrementId)) {
            Debug.logInfo("Empty orderIncrementId. Not going to create invoice.", module);
            return null;
        }
        String invoiceIncrementId = null;
        OrderItemIdQtyArray orderItemIdQtyArray = new OrderItemIdQtyArray();
        if (UtilValidate.isNotEmpty(orderItemQtyMap)) {
            for (int orderItemId : orderItemQtyMap.keySet()) {
                OrderItemIdQty orderItemIdQty = new OrderItemIdQty();
                orderItemIdQty.setOrderItemId(orderItemId);
                orderItemIdQty.setQty(orderItemQtyMap.get(orderItemId));
                orderItemIdQtyArray.getComplexObjectArray().add(orderItemIdQty);
            }
        }

        SalesOrderInvoiceCreateRequestParam salesOrderInvoiceCreateRequestParam = new SalesOrderInvoiceCreateRequestParam();
        salesOrderInvoiceCreateRequestParam.setSessionId(sessionId);
        salesOrderInvoiceCreateRequestParam.setInvoiceIncrementId(orderIncrementId);
        salesOrderInvoiceCreateRequestParam.setEmail("true");
        salesOrderInvoiceCreateRequestParam.setItemsQty(orderItemIdQtyArray);

        SalesOrderInvoiceCreateResponseParam salesOrderInvoiceCreateResponseParam = port.salesOrderInvoiceCreate(salesOrderInvoiceCreateRequestParam);
        invoiceIncrementId = salesOrderInvoiceCreateResponseParam.getResult();
        return invoiceIncrementId;
    }
    public int catalogInventoryStockItemUpdate (String productId, String inventoryCount) {
        if (UtilValidate.isEmpty(productId) || UtilValidate.isEmpty(inventoryCount)) {
            Debug.logInfo("Not getting complete information while going to update catalog inventory stock.", module);
            Debug.logInfo("productId = "+productId+" inventoryCount = "+inventoryCount, module);
        }
        int isStockItemUpdated = 0;
        CatalogInventoryStockItemUpdateRequestParam requestParam = new CatalogInventoryStockItemUpdateRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setProductId(productId);

        CatalogInventoryStockItemUpdateEntity catalogInventoryStockItemUpdateEntity = new CatalogInventoryStockItemUpdateEntity();
        catalogInventoryStockItemUpdateEntity.setQty(inventoryCount);
        catalogInventoryStockItemUpdateEntity.setIsInStock(1);
        requestParam.setData(catalogInventoryStockItemUpdateEntity);

        CatalogInventoryStockItemUpdateResponseParam responseParam = port.catalogInventoryStockItemUpdate(requestParam);
        isStockItemUpdated = responseParam.getResult();
        return isStockItemUpdated;
    }
    public SalesOrderInvoiceEntity getInvoiceInfo(String invoiceIncrementId) {
        if (UtilValidate.isEmpty(invoiceIncrementId)) {
            Debug.logInfo("Empty invoiceIncrementId.", module);
            return null;
        }
        SalesOrderInvoiceInfoRequestParam requestParam = new SalesOrderInvoiceInfoRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setInvoiceIncrementId(invoiceIncrementId);

        SalesOrderInvoiceInfoResponseParam responseParam = port.salesOrderInvoiceInfo(requestParam);
        SalesOrderInvoiceEntity invoice = responseParam.getResult();
        return invoice;
    }
    public String captureInvoice(String invoiceIncrementId) {
        if (UtilValidate.isEmpty(invoiceIncrementId)) {
            Debug.logInfo("Empty invoiceIncrementId.", module);
            return null;
        }
        SalesOrderInvoiceCaptureRequestParam requestParam = new SalesOrderInvoiceCaptureRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setInvoiceIncrementId(invoiceIncrementId);

        SalesOrderInvoiceCaptureResponseParam responseParam = port.salesOrderInvoiceCapture(requestParam);
        String isCaptured = responseParam.getResult();
        return isCaptured;
    }
    public StoreEntity getStoreInfo(String magentoStoreId) {
        if (UtilValidate.isEmpty(magentoStoreId)) {
            Debug.logInfo("Empty magentoStoreId.", module);
            return null;
        }
        StoreInfoRequestParam requestParam = new StoreInfoRequestParam();
        requestParam.setSessionId(sessionId);
        requestParam.setStoreId(magentoStoreId);

        StoreInfoResponseParam responseParam = port.storeInfo(requestParam);
        StoreEntity storeInfo = responseParam.getResult();
        return storeInfo;
    }
    public List<StoreConfigEntity> getStoreConfig() {
        StoreConfigRequestParam requestParam = new StoreConfigRequestParam();
        requestParam.setSessionId(sessionId);

        StoreConfigResponseParam responseParam = port.magentoStoreConfig(requestParam);
        StoreConfigEntityArray storeConfigEntityArray = responseParam.getResult();
        List<StoreConfigEntity> storeConfigList = storeConfigEntityArray.getComplexObjectArray();
        return storeConfigList;
    }
    public CatalogCategoryTree getCategoryTree(String storeView) {
        MageApiModelServerWsiHandlerPortType port = getPort();
        CatalogCategoryTreeRequestParam catalogCategoryTreeRequestParam = new CatalogCategoryTreeRequestParam();
        catalogCategoryTreeRequestParam.setSessionId(sessionId);
        catalogCategoryTreeRequestParam.setStore(storeView);
        CatalogCategoryTreeResponseParam catalogCategoryTreeResponseParam = port.catalogCategoryTree(catalogCategoryTreeRequestParam);
        CatalogCategoryTree catalogCategory0 = catalogCategoryTreeResponseParam.getResult();
        return catalogCategory0;
    }
    
    public CatalogCategoryInfo getCategoryInfo(int categoryId) {
        CatalogCategoryInfoRequestParam catalogCategoryInfoRequestParam = new CatalogCategoryInfoRequestParam();
        catalogCategoryInfoRequestParam.setSessionId(sessionId);
        catalogCategoryInfoRequestParam.setCategoryId(categoryId);
        CatalogCategoryInfoResponseParam catalogCategoryInfoResponseParam = port.catalogCategoryInfo(catalogCategoryInfoRequestParam);
        CatalogCategoryInfo catalogCategoryInfo = catalogCategoryInfoResponseParam.getResult();
     
        return catalogCategoryInfo;
    }
    
    public List<CatalogProductEntity> getMagentoProducts() {
        CatalogProductListRequestParam catalogProductListRequestParam = new CatalogProductListRequestParam();
        catalogProductListRequestParam.setSessionId(sessionId);

        CatalogProductListResponseParam catalogProductListResponseParam = port.catalogProductList(catalogProductListRequestParam);
        CatalogProductEntityArray catalogProductEntityArray = catalogProductListResponseParam.getResult();

        return catalogProductEntityArray.getComplexObjectArray();
    }

    public CatalogProductReturnEntity getProductInfo(String productId) {

        CatalogProductInfoRequestParam catalogProductInfoRequestParam = new CatalogProductInfoRequestParam();
        catalogProductInfoRequestParam.setSessionId(sessionId);
        catalogProductInfoRequestParam.setProductId(productId);

        CatalogProductInfoResponseParam catalogProductInfoResponseParam = port.catalogProductInfo(catalogProductInfoRequestParam);
        return catalogProductInfoResponseParam.getResult();
    }

    public CatalogProductImageEntity getProductImageInfo(String productId, String file) {

        CatalogProductAttributeMediaInfoRequestParam catalogProductAttributeMediaInfoRequestParam = new CatalogProductAttributeMediaInfoRequestParam();
        catalogProductAttributeMediaInfoRequestParam.setSessionId(sessionId);
        catalogProductAttributeMediaInfoRequestParam.setProductId(productId);
        catalogProductAttributeMediaInfoRequestParam.setFile(file);

        CatalogProductAttributeMediaInfoResponseParam catalogProductAttributeMediaInfoResponseParam = port.catalogProductAttributeMediaInfo(catalogProductAttributeMediaInfoRequestParam);
        return catalogProductAttributeMediaInfoResponseParam.getResult();
    }

    public CatalogProductImageEntityArray getProductImageInfoList(String productId) {
        CatalogProductAttributeMediaListRequestParam catalogProductAttributeMediaListRequestParam = new CatalogProductAttributeMediaListRequestParam();
        catalogProductAttributeMediaListRequestParam.setSessionId(sessionId);
        catalogProductAttributeMediaListRequestParam.setProductId(productId);

        CatalogProductAttributeMediaListResponseParam catalogProductAttributeMediaListResponseParam = port.catalogProductAttributeMediaList(catalogProductAttributeMediaListRequestParam);
        return catalogProductAttributeMediaListResponseParam.getResult();
    }

    public boolean editOrderAddressInMagento (String orderIncrementId, Map<String, Object> orderData) {
        EditSalesOrderAddressRequestParam editSalesOrderAddressRequestParam = new EditSalesOrderAddressRequestParam();
        editSalesOrderAddressRequestParam.setSessionId(sessionId);
        editSalesOrderAddressRequestParam.setOrderIncrementId(orderIncrementId);

        SalesOrderAddressUpdateEntity salesOrderAddressUpdateEntity = new SalesOrderAddressUpdateEntity();

        salesOrderAddressUpdateEntity.setAddressType((String) orderData.get("addressType"));
        salesOrderAddressUpdateEntity.setFirstname((String) orderData.get("firstName"));
        salesOrderAddressUpdateEntity.setLastname((String) orderData.get("lastName"));
        salesOrderAddressUpdateEntity.setAddress1((String) orderData.get("address1"));
        salesOrderAddressUpdateEntity.setAddress2((String) orderData.get("address2"));
        salesOrderAddressUpdateEntity.setCity((String) orderData.get("city"));
        salesOrderAddressUpdateEntity.setPostcode((String) orderData.get("postalCode"));
        salesOrderAddressUpdateEntity.setCountryId((String) orderData.get("countryId"));
        salesOrderAddressUpdateEntity.setRegionId((String) orderData.get("regionId"));
        salesOrderAddressUpdateEntity.setTelephone((String) orderData.get("telephone"));
        salesOrderAddressUpdateEntity.setFax((String) orderData.get("fax"));

        editSalesOrderAddressRequestParam.setOrderData(salesOrderAddressUpdateEntity);
        EditSalesOrderAddressResponseParam editSalesOrderAddressResponseParam = port.magentoOrderEditSalesOrderAddress(editSalesOrderAddressRequestParam);
        return editSalesOrderAddressResponseParam.isResult();
    }

    public List<CatalogProductRelationEntity> getParentProduct(String productId) {
        CatalogProductRelationRequestParam catalogProductRelationRequestParam = new CatalogProductRelationRequestParam();
        catalogProductRelationRequestParam.setProductId(productId);
        catalogProductRelationRequestParam.setSessionId(sessionId);

        CatalogProductRelationResponseParam catalogProductRelationResponseParam = port.magentoCatalogGetProductRelation(catalogProductRelationRequestParam);
        CatalogProductRelationEntityArray catalogProductRelationEntityArray = catalogProductRelationResponseParam.getResult();
        return catalogProductRelationEntityArray.getComplexObjectArray();
    }

    public String createCreditMemo(Map<String, Object> creditMemoDetailMap) {
        if (UtilValidate.isEmpty(creditMemoDetailMap)) {
            Debug.logInfo("Empty orderIncrementId. Not going to create credit memo.", module);
            return null;
        }
        String orderIncrementId = (String) creditMemoDetailMap.get("orderIncrementId");
        String creditMemoIncrementId = null;
        @SuppressWarnings("unchecked")
        Map<Integer, Double> orderItemQtyMap = (Map<Integer, Double>) creditMemoDetailMap.get("orderItemQtyMap");
        OrderItemIdQtyArray orderItemIdQtyArray = new OrderItemIdQtyArray();
        if (UtilValidate.isNotEmpty(orderItemQtyMap)) {
            for (int orderItemId : orderItemQtyMap.keySet()) {
                OrderItemIdQty orderItemIdQty = new OrderItemIdQty();
                orderItemIdQty.setOrderItemId(orderItemId);
                orderItemIdQty.setQty(orderItemQtyMap.get(orderItemId));
                orderItemIdQtyArray.getComplexObjectArray().add(orderItemIdQty);
            }
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Double> returnAdjustmentMap = (Map<Integer, Double>) creditMemoDetailMap.get("returnAdjustmentMap");
        Double positiveAdjustments = returnAdjustmentMap.get("positiveAdjustments");
        Double shippingAdjustments = returnAdjustmentMap.get("shippingAdjustments");

        SalesOrderCreditmemoData salesOrderCreditmemoData = new SalesOrderCreditmemoData();
        salesOrderCreditmemoData.setQtys(orderItemIdQtyArray);
        salesOrderCreditmemoData.setAdjustmentPositive(positiveAdjustments);
        salesOrderCreditmemoData.setShippingAmount(shippingAdjustments);

        SalesOrderCreditmemoCreateRequestParam salesOrderCreditmemoCreateRequestParam = new SalesOrderCreditmemoCreateRequestParam();
        salesOrderCreditmemoCreateRequestParam.setSessionId(sessionId);
        salesOrderCreditmemoCreateRequestParam.setOrderIncrementId(orderIncrementId);
        salesOrderCreditmemoCreateRequestParam.setCreditmemoData(salesOrderCreditmemoData);

        SalesOrderCreditmemoCreateResponseParam salesOrderCreditmemoCreateResponseParam = port.salesOrderCreditmemoCreate(salesOrderCreditmemoCreateRequestParam);
        creditMemoIncrementId =  salesOrderCreditmemoCreateResponseParam.getResult();
        return creditMemoIncrementId;
    }
}