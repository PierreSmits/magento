<div id="common-container">
  <#if !magentoConfiguration?has_content>
    <#include "component://magento/webapp/magento/common/Configuration.ftl"/>
  <#else>
    <#if !magentoStoreList?has_content>
        <#include "component://magento/webapp/magento/store/ImportMagentoStoreInformation.ftl"/>
    <#else>
      <div class="panel panel-default">
        <div class="panel-body">
           <div class="row">
            <div class="col-lg-6 col-md-6 divider-vertical">
              <#include "component://magento/webapp/magento/store/StoreInformation.ftl"/>
            </div>
            <div class="col-lg-6 col-md-6">
              <#include "component://magento/webapp/magento/store/WarehouseInformation.ftl"/>
            </div>
          </div>
        </div>
      </div>
      <#include "component://magento/webapp/magento/store/ShippingInformation.ftl"/>
      <#include "component://magento/webapp/magento/catalog/CatalogSetup.ftl"/>
      <#include "component://magento/webapp/magento/inventory/InventorySetup.ftl"/>
      <#include "component://magento/webapp/magento/common/ManageScheduleServices.ftl"/>
    </#if>
  </#if>
</div>