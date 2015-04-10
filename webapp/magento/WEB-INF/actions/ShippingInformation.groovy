/*
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
 */
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.magento.MagentoHelper;
import org.ofbiz.product.store.ProductStoreWorker;

magentoStoreList = MagentoHelper.getMagentoProductStoreList(delegator);
context.magentoStoreList = magentoStoreList;
if(magentoStoreList) {
    if (parameters.productStoreId) {
        magentoStore = ProductStoreWorker.getProductStore(parameters.productStoreId, delegator);
    } else {
        magentoProductStore = EntityUtil.getFirst(magentoStoreList);
        if (magentoProductStore) {
            magentoStore = ProductStoreWorker.getProductStore(magentoProductStore.productStoreId, delegator);
        }
    }
    if (magentoStore) {
        exprBldr = new EntityConditionBuilder();
        expr = exprBldr.AND() {
            EQUALS(roleTypeId: "CARRIER")
            IN(partyId: ["DHL", "FEDEX", "UPS", "USPS"])
        }
        carrierParties = delegator.findList("PartyRoleAndPartyDetail", expr,
                null, ["groupName"], null, false);
        if (carrierParties) {
            carrierAndShipmentMethod = [:];
            storeShipMethMap = [:];
            shippingServiceNameMap = [:];
            carrierParties.each { carrier ->
                expr = exprBldr.AND() {
                    EQUALS(productStoreId: magentoStore.productStoreId);
                    EQUALS(partyId: carrier.partyId);
                }
                existingStoreShipMethList = delegator.findList("ProductStoreShipmentMeth", expr, null, ["includeGeoId", "shipmentMethodTypeId"], null, false);
                expr = exprBldr.AND() {
                    EQUALS(roleTypeId: "CARRIER");
                    EQUALS(partyId: carrier.partyId);
                    if (existingStoreShipMethList) {
                        NOT_IN(shipmentMethodTypeId : existingStoreShipMethList.shipmentMethodTypeId);
                    }
                }
                carrierAndShipmentMethodList = delegator.findList("CarrierAndShipmentMethod", expr, null, null, null, false);
                if (carrierAndShipmentMethodList) {
                    carrierAndShipmentMethod.(carrier.partyId) = carrierAndShipmentMethodList;
                }
                storeShipMethList = [];
                existingStoreShipMethMap = [:];
                existingStoreShipMethList.each { existingStoreShipMeth ->
                    storeShipingMethMap = [:];
                    shipmentMethodType = delegator.findOne("ShipmentMethodType", false, [shipmentMethodTypeId : existingStoreShipMeth.shipmentMethodTypeId]);
                    if (shipmentMethodType) {
                        storeShipingMethMap.description = shipmentMethodType.description;
                        storeShipingMethMap.productStoreShipMethId = existingStoreShipMeth.productStoreShipMethId;
                        existingStoreShipMethMap.(shipmentMethodType.shipmentMethodTypeId) = storeShipingMethMap;
                        storeShipMethMap.(carrier.partyId) = existingStoreShipMethMap;
                    }
                }
                if ("DHL".equalsIgnoreCase(carrier.partyId)) {
                    shippingServiceNameMap.(carrier.partyId) = "";
                } else if ("UPS".equalsIgnoreCase(carrier.partyId)) {
                    shippingServiceNameMap.(carrier.partyId) = "";
                } else if ("USPS".equalsIgnoreCase(carrier.partyId)) {
                    shippingServiceNameMap.(carrier.partyId) = "uspsRateInquire";
                } else if ("FEDEX".equalsIgnoreCase(carrier.partyId)) {
                    shippingServiceNameMap.(carrier.partyId) = "";
                }
            }
            context.shippingServiceNameMap = shippingServiceNameMap;
            context.carrierAndShipmentMethod = carrierAndShipmentMethod;
            context.carrierParties = carrierParties;
            context.storeShipMethMap = storeShipMethMap;
            context.magentoStore = magentoStore;
        }
    }
}