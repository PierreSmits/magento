import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.magento.MagentoHelper;
import org.ofbiz.product.store.ProductStoreWorker;

magentoStoreList = MagentoHelper.getMagentoProductStoreList(delegator);
context.magentoStoreList = magentoStoreList;
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
    if (facilityId) {
        facility = delegator.findOne("Facility", [facilityId: facilityId], false);
        context.facility = facility;
    }
    context.magentoStore = magentoStore;
}