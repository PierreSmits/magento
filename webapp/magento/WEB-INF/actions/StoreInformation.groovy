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
import org.apache.ofbiz.content.content.ContentMapFacade;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.magento.MagentoHelper;
import org.apache.ofbiz.party.party.PartyHelper;
import org.apache.ofbiz.party.party.PartyWorker;
import org.apache.ofbiz.product.store.ProductStoreWorker;

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
        partyId = magentoStore.payToPartyId;
        if (partyId) {
            companyMap = [:];
            groupName = PartyHelper.getPartyName(delegator, partyId, false);
            companyMap.groupName = groupName;

            /*Get postal address of company*/
            postalAddress = PartyWorker.findPartyLatestPostalAddress(partyId, delegator);
            companyMap.postalAddress = postalAddress;

            magentoStoreAddressContent = EntityUtil.getFirst(delegator.findList("PartyContent", EntityCondition.makeCondition("partyContentTypeId", "MAGENTO_STORE_ADDR"), null, null, null, false));
            if (magentoStoreAddressContent) {
                contentMapFacade = new ContentMapFacade(dispatcher, magentoStoreAddressContent, [:], locale, "text/html", true);
                context.contentMapFacade = contentMapFacade;
            }

            /*Get email address of company*/
            companyEmail = dispatcher.runSync("getPartyEmail", ['partyId': partyId, 'userLogin': parameters.userLogin]);
            companyMap.companyEmail = companyEmail;

            /*Get contact number of company*/
            telecomNumber = PartyWorker.findPartyLatestTelecomNumber(partyId, delegator);
            if (telecomNumber) {
                companyMap.telecomNumber= telecomNumber;
            }
            context.companyMap = companyMap;
            context.partyId = partyId;
            context.magentoStore = magentoStore;
        } 
    }
}