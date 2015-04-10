<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
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