<div class="panel panel-default">
  <div class="panel-heading clearfix">
    <div class="panel-title">
      <h4><a href="<@ofbizUrl>main</@ofbizUrl>"><i class="fa fa-cog fa-lg"></i></a> ${uiLabelMap.CommonConfiguration}</h4>
    </div>
  </div>
  <div class="panel-body">
    <form method="post" action="<@ofbizUrl>createMagentoConfiguration</@ofbizUrl>" class="form-vertical requireValidation">
      <input type="hidden" name="enumId" value="MAGENTO_SALE_CHANNEL">
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="serverUrl">${uiLabelMap.MagentoMagentoConnectUrl}</label>
          <small class="text-muted">(eg. http://magentohost/api/v2_soap?wsdl)</small>
          <input type="url" id="serverUrl" name="serverUrl" class="required form-control" data-label="${uiLabelMap.MagentoMagentoConnectUrl}" value="${(magentoConfiguration.serverUrl)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="xmlRpcUserName">${uiLabelMap.MagentoSoapUserName}</label>
          <input type="text" id="xmlRpcUserName" name="xmlRpcUserName" data-label="${uiLabelMap.CommonUsername}" class="form-control required" value="${(magentoConfiguration.xmlRpcUserName)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="password">${uiLabelMap.CommonPassword}</label>
          <input type="password" id="password" name="password" class="required form-control" data-label="${uiLabelMap.CommonPassword}" value="${(magentoConfiguration.password)!}"/>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-1 col-md-1">
          <button type="submit" class="btn btn-primary">${uiLabelMap.CommonSave}</button>
        </div>
        <div class="col-lg-1 col-md-1">
          <input class="btn btn-default" type="reset" value="${uiLabelMap.CommonReset}" />
        </div>
      </div>
    </form>
  </div>
</div>