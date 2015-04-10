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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class ReturnServices {
    public static final String module = ReturnServices.class.getName();
    public static final String resource = "MagentoUiLabels";

    public static Map<String, Object> createCreditMemoInMagento(DispatchContext dctx, Map<String, ?> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        String returnId = (String) context.get("returnId");
        List<String> orderIdList = new ArrayList<String>();

        try {
            MagentoClient magentoClient = new MagentoClient(dispatcher, delegator);
            EntityCondition returnItemcondition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition(UtilMisc.toMap("returnId", returnId)),
                    EntityCondition.makeCondition(UtilMisc.toMap("returnTypeId", "RTN_REFUND"))));
             List<GenericValue> returnItems = delegator.findList("ReturnItem", returnItemcondition, null,null, null, false);
             //This block is for getting unique orders included in the Return, as the credit memo will be created per order
             for(GenericValue returnItem : returnItems) {
                 String orderId = returnItem.getString("orderId");
                 if(!orderIdList.contains(orderId)) {
                     orderIdList.add(orderId);
                 }
             }
             for(String orderId : orderIdList) {
                 GenericValue orderHeader = delegator.findOne("OrderHeader", UtilMisc.toMap("orderId", orderId), false);
                 Map<String, Object> creditMemoDetailMap = new HashMap<String, Object>();
                 String magOrderId = orderHeader.getString("externalId");
                 creditMemoDetailMap.put("orderIncrementId", magOrderId);

                 Map<Integer, Double> orderItemQtyMap = new HashMap<Integer, Double>();
                 List<GenericValue> filteredReturnItems = EntityUtil.filterByAnd(returnItems, UtilMisc.toMap("orderId", orderId));
                 for (GenericValue filteredReturnItem : filteredReturnItems) {
                     GenericValue orderItem = delegator.findOne("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", filteredReturnItem.getString("orderItemSeqId")), false);
                     Integer magOrderItemSeqId = Integer.valueOf(orderItem.getString("externalId"));
                     Double qty = filteredReturnItem.getBigDecimal("returnQuantity").doubleValue();
                     orderItemQtyMap.put(magOrderItemSeqId, qty);
                 }
                 creditMemoDetailMap.put("orderItemQtyMap", orderItemQtyMap);
                 String creditMemoIncrementId = magentoClient.createCreditMemo(creditMemoDetailMap);
                 if(UtilValidate.isEmpty(creditMemoIncrementId)) {
                     Debug.logError("Problem in creating credit-memo for Order ["+orderId+"]", module);
                 } else {
                     Debug.log("Created credit-memo for Order ["+orderId+"]", module);
                 }
             }
        } catch (Exception e) {
            Debug.logInfo(e.getMessage(), module);
        }
        return ServiceUtil.returnSuccess();
    }
}