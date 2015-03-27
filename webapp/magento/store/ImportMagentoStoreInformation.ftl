<div class="panel panel-default">
  <div class="panel-heading clearfix">
    <div class="panel-title">
      <h4><a href="<@ofbizUrl>ImportMagentoStoreInformation</@ofbizUrl>"><img src="../img/Magento-Store-Import.png" width="30px"></img></a> ${uiLabelMap.MagentoImportMagentoStoreInformation}
      <#if magentoConfiguration?has_content>
        <a class="pull-right" data-tooltip-target="#testConnectionInfo" data-tooltip-title="${uiLabelMap.MagentoTestConnection}" href="<@ofbizUrl>testMagentoConnection</@ofbizUrl>"><i class="fa fa-bolt fa-lg"></i></a>
        <span id="testConnectionInfo" style="display:none;">
          <p>${uiLabelMap.MagentoTestConnectionInfo}</p>
        </span>
      </#if>
      </h4>
    </div>
  </div>
  <div class="panel-body">
    <p>${uiLabelMap.MagentoImportMagentoStoreInformationInfo}</p>
    <a href="<@ofbizUrl>setupMagentoStore</@ofbizUrl>" class="btn btn-default"><i class="fa fa-download"></i> ${uiLabelMap.CommonImport}</a>
  </div>
</div>