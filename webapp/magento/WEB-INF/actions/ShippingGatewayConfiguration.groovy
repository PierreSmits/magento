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
import org.apache.ofbiz.entity.util.EntityUtil;
if (parameters.partyId) {
    partyId = parameters.partyId;
    if ("DHL".equalsIgnoreCase(partyId)) {
        shipmentGatewayConfiguration = EntityUtil.getFirst(delegator.findList("ShipmentGatewayDhl", null, null, null, null, false));
    } else if ("FEDEX".equalsIgnoreCase(partyId)) {
        shipmentGatewayFedex = EntityUtil.getFirst(delegator.findList("ShipmentGatewayFedex", null, null, null, null, false));
        if (shipmentGatewayFedex) {
            shipmentGatewayConfiguration = [:];
            shipmentGatewayConfiguration.shipmentGatewayConfigId = shipmentGatewayFedex.shipmentGatewayConfigId;
            shipmentGatewayConfiguration.accessUserId = shipmentGatewayFedex.accessUserKey;
            shipmentGatewayConfiguration.accessPassword = shipmentGatewayFedex.accessUserPwd;
            shipmentGatewayConfiguration.connectUrl = shipmentGatewayFedex.connectUrl;
            shipmentGatewayConfiguration.accessAccountNbr = shipmentGatewayFedex.accessAccountNbr;
            shipmentGatewayConfiguration.accessMeterNumber = shipmentGatewayFedex.accessMeterNumber;
        }
    } else if ("UPS".equalsIgnoreCase(partyId)) {
        shipmentGatewayConfiguration = EntityUtil.getFirst(delegator.findList("ShipmentGatewayUps", null, null, null, null, false));
    } else if ("USPS".equalsIgnoreCase(partyId)) {
        shipmentGatewayConfiguration = EntityUtil.getFirst(delegator.findList("ShipmentGatewayUsps", null, null, null, null, false));
    }
    context.shipmentGatewayConfiguration = shipmentGatewayConfiguration;
}