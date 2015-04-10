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
