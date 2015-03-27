import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.magento.MagentoHelper;
import org.ofbiz.party.party.PartyWorker;
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
        EntityCondition condition = EntityCondition.makeCondition(
                    EntityCondition.makeCondition("productStoreId", magentoStore.productStoreId),
                    EntityCondition.makeConditionDate("fromDate", "thruDate")
            );
        facilityIds = EntityUtil.getFieldListFromEntityList(delegator.findList("ProductStoreFacility", condition, null, null, null, false), "facilityId", true);
        if (facilityIds) {
            facilityList = delegator.findList("Facility", EntityCondition.makeCondition("facilityId", EntityOperator.IN, facilityIds),, null, null, null, false);
            context.facilityList = facilityList;
        }
        if (parameters.facilityId) {
            facilityId = parameters.facilityId;
        } else {
            facilityId = magentoStore.inventoryFacilityId;
        }
        partyId = magentoStore.payToPartyId;
        if (facilityId) {
            condition = EntityCondition.makeCondition(
                    EntityCondition.makeCondition("facilityId", facilityId),
                    EntityCondition.makeConditionDate("fromDate", "thruDate")
            );
            facilityContactMechList = delegator.findList("FacilityContactMech", condition, null, null, null, false);
            if (facilityContactMechList) {
                contactMechList = delegator.findList("ContactMech", EntityCondition.makeCondition("contactMechId", EntityOperator.IN, facilityContactMechList.contactMechId), null, null, null, false);
                if (contactMechList) {
                    /*Get postal address of facility*/
                    postalAddressContactMech = EntityUtil.getFirst(EntityUtil.filterByCondition(contactMechList, EntityCondition.makeCondition("contactMechTypeId", EntityOperator.EQUALS, "POSTAL_ADDRESS")));
                    if (postalAddressContactMech) {
                        postalAddress = delegator.findOne("PostalAddress", [contactMechId: postalAddressContactMech.contactMechId], false);
                        if (postalAddress) {
                            context.warehousePostalAddress = postalAddress;
                        }
                    }
                    /*Get contact number of company*/
                    telecomNumberContactMech = EntityUtil.getFirst(EntityUtil.filterByCondition(contactMechList, EntityCondition.makeCondition("contactMechTypeId", EntityOperator.EQUALS, "TELECOM_NUMBER")));
                    if (telecomNumberContactMech) {
                        telecomNumber = delegator.findOne("TelecomNumber", [contactMechId: telecomNumberContactMech.contactMechId], false);
                        if (telecomNumber) {
                            context.warehouseTelecomNumber= telecomNumber;
                        }
                    }
                }
            }
            /*Get facility values*/
            facility = delegator.findOne("Facility", [facilityId: facilityId], false);
            context.facility = facility;
            context.magentoStore = magentoStore;
        }
    }
}