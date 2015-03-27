<div id="warehouseInformation">
  <a href="<@ofbizUrl>EditWarehouseDetails?facilityId=${facility.facilityId?if_exists}</@ofbizUrl>" data-dialog-href="<@ofbizUrl>EditWarehouseDetails?facilityId=${facility.facilityId?if_exists}</@ofbizUrl>" data-dialog-width="half" title="${uiLabelMap.MagentoEditWarehouseDetails}" class="pull-right"><i class="fa fa-pencil pull-left"></i></a>
  <dl class="dl-horizontal dl-small">
    <dt>${uiLabelMap.MagentoWarehouseName}</dt>
    <dd>${(facility.facilityName)!}</dd>
    <dt>${uiLabelMap.MagentoPhone}</dt>
    <dd>${(warehouseTelecomNumber.countryCode)?default('N/A')}${(warehouseTelecomNumber.areaCode)!}${(warehouseTelecomNumber.contactNumber)!}</dd>
    <dt>${uiLabelMap.MagentoAddress}</dt>
    <dd>
      <ul class="list-unstyled clearfix">
      <li>${(warehousePostalAddress.address1)?default('N/A')}</li>
      <li>${(warehousePostalAddress.address2)!}</li>
      <li>${(warehousePostalAddress.city)!} ${(warehousePostalAddress.stateProvinceGeoId)!} ${(warehousePostalAddress.postalCode)!}</li>
      <li>${(warehousePostalAddress.countryGeoId)!}</li>
    </dd>
  </dl>
</div>