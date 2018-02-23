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
<form method="post" id="companyInfo" class="requireValidation ajaxMe" action="<@ofbizUrl>createUpdateWarehouseInformation</@ofbizUrl>" data-successMethod="#warehouseInformation" data-errorMethod="#warehouseInformation" data-ajax-loader="#editWarehouseDetails-ajax-loader">
  <input type="hidden" name="productStoreId" value="${(magentoStore.productStoreId)!}"/>
  <input type="hidden" name="partyId" value="${(magentoStore.payToPartyId)!}"/>
  <input type="hidden" name="facilityId" value="${(facility.facilityId)!}"/>
  <input type="hidden" name="facilityPostalContactMechId" value="${(warehousePostalAddress.contactMechId)!}"/>
  <input type="hidden" name="facilityTelecomContactMechId" value="${(warehouseTelecomNumber.contactMechId)!}"/>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="facilityName">${uiLabelMap.MagentoWarehouseName}</label>
      <input type="text" name="facilityName" id="facilityName"  class="required form-control" data-label="${uiLabelMap.MagentoWarehouseName}" value="${(facility.facilityName)!}" maxLength="100">
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="contactNumber">${uiLabelMap.MagentoPhone}</label>
      <input type="tel" name="contactNumber" id="contactNumber" class="form-control validate-phone" value="${(warehouseTelecomNumber.countryCode)!}${(warehouseTelecomNumber.areaCode)!}${(warehouseTelecomNumber.contactNumber)!}">
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="address1">${uiLabelMap.CommonAddress1}</label>
      <input type="text" name="address1" id="address1" data-label="${uiLabelMap.CommonAddress1}" class="required form-control" value="${(warehousePostalAddress.address1)!}">
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="address2">${uiLabelMap.CommonAddress2}</label>
      <input type="text" name="address2" id="address2" class="form-control" value="${(warehousePostalAddress.address2)!}">
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-6 col-md-6">
      <label for="city">${uiLabelMap.CommonCity}</label>
      <input type="text" name="city" id="city" class="required form-control" data-label="${uiLabelMap.CommonCity}" value="${(warehousePostalAddress.city)!}">
    </div>
    <div class="col-lg-6 col-md-6">
      <label for="">${uiLabelMap.CommonZipPostalCode}</label>
      <input type="text" name="postalCode" id="postalCode" class="required form-control validate-usCanadaZip" data-label="${uiLabelMap.CommonZipPostalCode}" data-country-box="#countryGeoId" value="${(warehousePostalAddress.postalCode)!}" maxLength="60"/>
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-6 col-md-6">
      <label for="countryGeoId">${uiLabelMap.CommonCountry}</label>
      <select name="countryGeoId" id="countryGeoId" data-label="${uiLabelMap.CommonCountry}" data-dependent="#stateProvinceGeoId" class="form-control">
        <option value='' title=''>${uiLabelMap.CommonSelect}</option>
        <#list countryList as country>
          <option value='${country.geoId}' title='${country.geoId}' <#if country.geoId == (warehousePostalAddress.countryGeoId)?default("USA")> selected="selected" </#if>>${country.get("geoName")?default(country.geoId)}</option>
        </#list>
      </select>
    </div>
    <div class="col-lg-6 col-md-6">
      <label for="stateProvinceGeoId">${uiLabelMap.CommonStateProvince}</label>
      <select name="stateProvinceGeoId" id="stateProvinceGeoId" class="required form-control" data-label="${uiLabelMap.CommonStateProvince}">
        <#list countryList as country>
          <#assign stateAssocs = Static["org.apache.ofbiz.common.CommonWorkers"].getAssociatedStateList(delegator,country.geoId)>
          <#if stateAssocs?has_content>
            <optgroup label="${country.geoId}">
              <option value='' title=''>${uiLabelMap.CommonSelect}</option>
              <#list stateAssocs as stateAssoc>
                <option value='${stateAssoc.geoId}' title='${stateAssoc.geoId}' <#if stateAssoc.geoId == (warehousePostalAddress.stateProvinceGeoId)?if_exists> selected</#if>>${stateAssoc.geoName?default(stateAssoc.geoId)}</option>
              </#list>
            </optgroup>
          <#else>
            <optgroup label="${country.geoId}">
              <option value='_NA_'>${uiLabelMap.CommonNA}</option>
            </optgroup>
          </#if>
        </#list>
      </select>
    </div>
  </div>
  <div class="row">
    <div class="col-lg-12 col-md-12">
      <button type="submit" class="btn btn-primary pull-left">
        ${uiLabelMap.CommonSave}
        <span id="editWarehouseDetails-ajax-loader" class="ajax-loader" style="display:none"></span>
      </button>
    </div>
  </div>
</form>