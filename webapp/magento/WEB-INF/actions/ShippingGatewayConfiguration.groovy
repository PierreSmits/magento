import org.ofbiz.entity.util.EntityUtil;
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