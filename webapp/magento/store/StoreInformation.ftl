<div id="storeInformation">
  <a href="<@ofbizUrl>EditStoreDetails?productStoreId=${(magentoStore.productStoreId)!}</@ofbizUrl>" data-dialog-href="<@ofbizUrl>EditStoreDetails?productStoreId=${parameters.productStoreId?if_exists}</@ofbizUrl>" data-dialog-width="half" title="${uiLabelMap.MagentoEditStoreDetails}" class="pull-right"><i class="fa fa-pencil pull-left"></i></a>
  <dl class="dl-horizontal dl-small">
    <dt>${uiLabelMap.MagentoProductStore}</dt>
    <dd>${(magentoStore.storeName)!}</dd>
    <dt>${uiLabelMap.MagentoCompanyName}</dt>
    <dd>${(companyMap.groupName)!}</dd>
    <dt>${uiLabelMap.MagentoStoreName}</dt>
    <dd>${(magentoStore.storeName)!}</dd>
    <dt>${uiLabelMap.MagentoPhone}</dt>
    <dd>${(companyMap.telecomNumber.countryCode)!}${(companyMap.telecomNumber.areaCode)!}${(companyMap.telecomNumber.contactNumber)!}</dd>
    <dt>${uiLabelMap.CommonEmail}</dt>
    <dd>${(companyMap.companyEmail.emailAddress)!}</dd>
    <dt>${uiLabelMap.MagentoAddress} <i class="fa fa-info-circle" data-tooltip-title="${uiLabelMap.MagentoMagentoStoreAddress}" data-tooltip-target="#magentoStoreAddress"></i></dt>
    <dd>
      <ul class="list-unstyled clearfix">
      <li>${(companyMap.postalAddress.address1)?default('N/A')}</li>
      <li>${(companyMap.postalAddress.address2)!}</li>
      <li>${(companyMap.postalAddress.city)!} ${(companyMap.postalAddress.stateProvinceGeoId)!} ${(companyMap.postalAddress.postalCode)!}</li>
      <li>${(companyMap.postalAddress.countryGeoId)!}</li>
    </dd>
  </dl>
  <span id="magentoStoreAddress" style="display:none;">
    ${(contentMapFacade)!}
  </span>
</div>