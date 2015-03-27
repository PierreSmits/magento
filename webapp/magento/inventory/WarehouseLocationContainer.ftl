<#if !productFacilityUploadRunningJobs?has_content>
  <#include "component://magento/webapp/magento/inventory/ImportWarehouseLocation.ftl" />
  <#include "component://magento/webapp/magento/inventory/LogFiles.ftl" />
<#else>
  <#include "component://magento/webapp/magento/inventory/WarehouseLocationUploadsInProgress.ftl" />
</#if>