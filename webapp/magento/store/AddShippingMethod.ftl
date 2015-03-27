<#if parameters.partyId?has_content>
  <div class="row">
    <div class="col-md-12">
      <a class="btn btn-default pull-right" data-ajax-update="#shippingInformation" data-update-url="<@ofbizUrl>ShippingInformation?partyId=${(parameters.partyId)!}&amp;productStoreId=${(parameters.productStoreId)!}</@ofbizUrl>"><i class="fa fa-times"></i></a>
    </div>
  </div>
  <form method="post" action="<@ofbizUrl>createRemoveProductStoreShipMeth</@ofbizUrl>" class="requireValidation ajaxMe" data-successMethod="#shippingMethods" data-errorMethod="#shippingMethods">
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
        <button type="submit" class="btn btn-primary" title="${uiLabelMap.CommonAdd}">${uiLabelMap.CommonAdd}</button>
      </div>
    </div>
  </form>
</#if>