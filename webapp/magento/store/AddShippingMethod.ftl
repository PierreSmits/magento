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
<#if parameters.partyId?has_content>
  <div class="row">
    <div class="col-md-12">
      <a class="btn btn-default pull-right" data-ajax-update="#shippingInformation" data-update-url="<@ofbizUrl>ShippingInformation?partyId=${(parameters.partyId)!}&amp;productStoreId=${(parameters.productStoreId)!}</@ofbizUrl>"><i class="fa fa-times"></i></a>
    </div>
  </div>
  <form method="post" action="<@ofbizUrl>createRemoveProductStoreShipMeth</@ofbizUrl>" class="requireValidation ajaxMe" data-successMethod="#shippingMethods" data-errorMethod="#shippingMethods" data-ajax-loader="#addShippingMethod-ajax-loader">
    <input type="hidden" name="productStoreId" value="${(parameters.productStoreId)!}"/>
    <input type="hidden" name="partyId" value="${(parameters.partyId)!}"/>
    <input type="hidden" name="roleTypeId" value="CARRIER"/>
    <input type="hidden" name="serviceName" value="${(shippingServiceNameMap[parameters.carrierPartyId])!}"/>
    <div class="form-group row">
      <div class="col-lg-3 col-md-3">
        <label for="countryGeoId">${uiLabelMap.MagentoShippingMethod}</label>
        <select name="shipmentMethodTypeId" multiple="multiple" class="form-control chosen-select required" data-label="${uiLabelMap.MagentoShippingMethod}">
          <#if carrierAndShipmentMethod?has_content>
            <#assign shipmentMethodList = carrierAndShipmentMethod[parameters.partyId]>
            <#list shipmentMethodList as shipmentMethod>
              <option value="${(shipmentMethod.shipmentMethodTypeId)!}"> ${(shipmentMethod.description)!}</option>
            </#list>
          </#if>
        </select>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-3 col-md-3">
        <button type="submit" class="btn btn-primary" title="${uiLabelMap.CommonAdd}">${uiLabelMap.CommonAdd}
          <span id="addShippingMethod-ajax-loader" class="ajax-loader" style="display:none"></span>
        </button>
      </div>
    </div>
  </form>
</#if>