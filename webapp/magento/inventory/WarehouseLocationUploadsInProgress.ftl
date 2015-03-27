<#assign productFacilityUploadRunningJob = productFacilityUploadRunningJobs?first />
<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">${uiLabelMap.MagentoImportJobScheduledOrRunning}</h3>
  </div>
  <div class="panel-body">
    <table class="table table-striped table-bordered table-hover">
      <tr>
        <td><label>${uiLabelMap.MagentoJobId}:</label></td>
        <td>${(productFacilityUploadRunningJob.jobId)?if_exists}</td>
      </tr>
      <tr>
        <td><label>${uiLabelMap.CommonStatus}:</label></td>
        <#assign statusItem = productFacilityUploadRunningJob.getRelatedOne("StatusItem")?if_exists />
        <td>${(statusItem.description)?if_exists}</td>
      </tr>
      <tr>
        <td><label>${uiLabelMap.CommonDate}:</label></td>
        <td>${nowTimestamp?string.short}</td>
      </tr>
      <tr>
        <td></td>
        <td>
          <a href="<@ofbizUrl>ImportWarehouseLocation?externalLoginKey=${externalLoginKey}</@ofbizUrl>" class='buttontext'>${uiLabelMap.MagentoRefreshPage}</a>
        </td>
      </tr>
    </table>
  </div>
</div>
