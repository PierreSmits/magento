import org.ofbiz.content.content.ContentMapFacade;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.magento.MagentoHelper;
import org.ofbiz.party.party.PartyHelper;
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
                contentMapFacade = new ContentMapFacade(dispatcher, delegator, magentoStoreAddressContent.contentId, [:], locale, "text/html", true);
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