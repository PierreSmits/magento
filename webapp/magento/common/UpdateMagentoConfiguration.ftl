<form method="post" action="<@ofbizUrl>updateMagentoConfiguration</@ofbizUrl>" class="form-vertical requireValidation ajaxMe" data-successMethod="#common-container" data-successMethod="#common-container">
  <input type="hidden" name="magentoConfigurationId" value="${(magentoConfiguration.magentoConfigurationId)!}">
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="serverUrl">${uiLabelMap.MagentoMagentoConnectUrl}</label>
      <small class="text-muted">(eg. http://magentohost/api/v2_soap?wsdl)</small>
      <input type="url" id="serverUrl" name="serverUrl" class="required form-control" data-label="${uiLabelMap.MagentoMagentoConnectUrl}" value="${(magentoConfiguration.serverUrl)!}"/>
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="xmlRpcUserName">${uiLabelMap.MagentoSoapUserName}</label>
      <input type="text" id="xmlRpcUserName" name="xmlRpcUserName" data-label="${uiLabelMap.CommonUsername}" class="form-control required" value="${(magentoConfiguration.xmlRpcUserName)!}"/>
    </div>
  </div>
  <div class="form-group row">
    <div class="col-lg-12 col-md-12">
      <label for="password">${uiLabelMap.CommonPassword}</label>
      <input type="password" id="password" name="password" class="required form-control" data-label="${uiLabelMap.CommonPassword}" value="${(magentoConfiguration.password)!}"/>
    </div>
  </div>
  <div class="row">
    <div class="col-lg-2 col-md-2">
      <button type="submit" class="btn btn-primary">${uiLabelMap.CommonSave}</button>
    </div>
    <div class="col-lg-2 col-md-2">
      <input class="btn btn-default" type="reset" value="${uiLabelMap.CommonReset}" />
    </div>
  </div>
</form>