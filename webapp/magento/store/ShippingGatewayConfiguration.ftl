<#if parameters.partyId?has_content>
  <div class="row">
    <div class="col-md-12">
      <a class="btn btn-default pull-right" data-ajax-update="#shippingInformation" data-update-url="<@ofbizUrl>ShippingInformation?partyId=${(parameters.partyId)!}&amp;productStoreId=${(parameters.productStoreId)!}</@ofbizUrl>"><i class="fa fa-times"></i></a>
    </div>
  </div>
  <form method="post" action="<@ofbizUrl>createUpdateShipmentGatewayConfig</@ofbizUrl>" class="form-vertical requireValidation ajaxMe" data-successMethod="#shippingMethods" data-errorMethod="#shippingMethods">
    <input type="hidden" name="shipmentGatewayConfigId" value="${(shipmentGatewayConfiguration.shipmentGatewayConfigId)!}">
    <input type="hidden" name="partyId" value="${(parameters.partyId)!}">
    <input type="hidden" name="productStoreId" value="${(parameters.productStoreId)!}"/>
    <div class="form-group row">
      <div class="col-lg-6 col-md-6">
        <label for="accessUserId">${uiLabelMap.MagentoAccessUserKey}</label>
        <input type="text" id="accessUserId" name="accessUserId" data-label="${uiLabelMap.MagentoAccessUserKey}" class="form-control required" value="${(shipmentGatewayConfiguration.accessUserId)!}"/>
      </div>
    </div>
    <div class="form-group row">
      <div class="col-lg-6 col-md-6">
        <label for="accessPassword">${uiLabelMap.MagentoAccessUserPassword}</label>
        <input type="text" id="accessPassword" name="accessPassword" class="required form-control" data-label="${uiLabelMap.MagentoAccessUserPassword}" value="${(shipmentGatewayConfiguration.accessPassword)!}"/>
      </div>
    </div>
    <#if parameters.partyId=="DHL">
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="accessAccountNbr">${uiLabelMap.MagentoAccessAccountNumber}</label>
          <input type="text" id="accessAccountNbr" name="accessAccountNbr" class="required form-control" data-label="${uiLabelMap.MagentoAccessAccountNumber}" value="${(shipmentGatewayConfiguration.accessAccountNbr)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="accessShippingKey">${uiLabelMap.MagentoAccessShippingKey}</label>
          <input type="text" id="accessShippingKey" name="accessShippingKey" class="form-control" data-label="${uiLabelMap.MagentoAccessShippingKey}" value="${(shipmentGatewayConfiguration.accessShippingKey)!}"/>
        </div>
      </div>
    </#if>
    <#if parameters.partyId=="FEDEX">
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="accessAccountNbr">${uiLabelMap.MagentoAccessAccountNumber}</label>
          <input type="text" id="accessAccountNbr" name="accessAccountNbr" class="required form-control" data-label="${uiLabelMap.MagentoAccessAccountNumber}" value="${(shipmentGatewayConfiguration.accessAccountNbr)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="accessMeterNumber">${uiLabelMap.MagentoAccessMeterNumber}</label>
          <input type="text" id="accessMeterNumber" name="accessMeterNumber" class="required form-control" data-label="${uiLabelMap.MagentoAccessMeterNumber}" value="${(shipmentGatewayConfiguration.accessMeterNumber)!}"/>
        </div>
      </div>
    </#if>
    <#if parameters.partyId=="UPS">
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="accessLicenseNumber">${uiLabelMap.MagentoAccessLicenseNumber}</label>
          <input type="text" id="accessLicenseNumber" name="accessLicenseNumber" class="required form-control" data-label="${uiLabelMap.MagentoAccessLicenseNumber}" value="${(shipmentGatewayConfiguration.accessLicenseNumber)!}"/>
        </div>
      </div>
    </#if>
    <div class="form-group row">
      <div class="col-lg-6 col-md-6">
        <label for="connectUrl">${uiLabelMap.MagentoConnectUrl}</label>
        <input type="url" id="connectUrl" name="connectUrl" class="required form-control" data-label="${uiLabelMap.MagentoConnectUrl}" value="${(shipmentGatewayConfiguration.connectUrl)!}"/>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-6 col-md-6">
        <button type="submit" class="btn btn-primary">${uiLabelMap.CommonSave}</button>
      </div>
    </div>
  </form>
</#if>