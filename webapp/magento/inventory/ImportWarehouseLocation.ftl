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
<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">${uiLabelMap.MagentoUpdateAndImportCSVWithWarehouseLocation}
    <a href="<@ofbizUrl>IntegrationManager</@ofbizUrl>" class="pull-right"><i class="fa fa-arrow-left"></i> ${uiLabelMap.CommonBack}</a>
    </h3>
  </div>
  <div class="panel-body">
    <form method="post" action="<@ofbizUrl>loadWarehouseLocations</@ofbizUrl>" name="LoadWarehouseLocations" class="form-vertical requireValidation" enctype="multipart/form-data">
      <input type="hidden" name="filePathInServer" value="${loadFilePath?if_exists}">
      <input type="hidden" name="facilityId" value="${(facility.facilityId)?if_exists}"/>
      <div class="form-group row">
        <div class="col-lg-4 col-md-4">
          <label for="uploadedFile">${uiLabelMap.MagentoChooseFileToBeUploaded}:</label>
          <input type="file" id="uploadedFile" name="uploadedFile" class="form-control required"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-4 col-md-4">
          <label for="facilityName">${uiLabelMap.MagentoWarehouse}</label>
          <input type="text" id="facilityName" name="facilityName" class="form-control required" value="${(facility.facilityName)!}" disabled=disabled/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-4 col-md-4">
          <button type="submit" class="btn btn-primary">${uiLabelMap.MagentoLoadLocations}</button>
        </div>
      </div>
    </form>
    <#if loadFileContent?has_content && !isError?has_content>
      <form method="post" action="<@ofbizUrl>importWarehouseLocations</@ofbizUrl>" name="ImportWarehouseLocations" class="form-vertical" enctype="multipart/form-data">
        <input type="hidden" name="externalLoginKey" value="${externalLoginKey}"/>
        <input type="hidden" name="contentId" value="${loadFileContent?if_exists}">
        <input type="hidden" name="facilityId" value="${facility.facilityId?if_exists}">
        <div class="row">
          <div class="col-lg-4 col-md-4">
            <button type="submit" class="btn btn-primary">${uiLabelMap.MagentoImportLocations}</button>
          </div>
        </div>
      </form>
    </#if>
    <#if productFacilityLocations?has_content>
      <#if !isError?has_content>
        <label>${uiLabelMap.MagentoPreviewProductFacilityLocations}</label>
      <#else>
        <label>${uiLabelMap.MagentoErrorInUploadedCSV}</label>
      </#if>
      <table class="table table-striped table-bordered table-hover table-responsive">
        <thead>
            <th>${uiLabelMap.FormFieldTitle_productId}</th>
            <th>${uiLabelMap.CommonQuantity}</th>
            <th>${uiLabelMap.MagentoWarehouse}</th>
            <th>${uiLabelMap.FormFieldTitle_locationSeqId}</th>
            <th>${uiLabelMap.FormFieldTitle_areaId}</th>
            <th>${uiLabelMap.FormFieldTitle_aisleId}</th>
            <th>${uiLabelMap.FormFieldTitle_sectionId}</th>
            <th>${uiLabelMap.FormFieldTitle_levelId}</th>
            <th>${uiLabelMap.FormFieldTitle_positionId}</th>
            <th>${uiLabelMap.CommonMessage}</th>
        </thead>
        <tbody>
          <#list productFacilityLocations as productFacilityLocation>
            <tr <#if productFacilityLocation.isError> class="danger"<#else> class="success"</#if>>
              <td>${productFacilityLocation.productId?if_exists}</td>
              <td>${productFacilityLocation.inventoryCount?if_exists}</td>
              <td>${facility.facilityName?if_exists}</td>
              <td>${productFacilityLocation.locationSeqId?if_exists}</td>
	          <td>${productFacilityLocation.areaId?if_exists}</td>
	          <td>${productFacilityLocation.aisleId?if_exists}</td>
	          <td>${productFacilityLocation.sectionId?if_exists}</td>
	          <td>${productFacilityLocation.levelId?if_exists}</td>
	          <td>${productFacilityLocation.positionId?if_exists}</td>
              <td>${productFacilityLocation.message?if_exists}</td>
            </tr>
          </#list>
        </tbody>
      </table>
    </#if>
    <#if processedInventoryLocations?has_content>
      <label>${uiLabelMap.MagentoProductFacilityLocationImportResultSummary}</label>
      <table class="table table-striped table-bordered table-hover">
        <thead>
          <th>${uiLabelMap.FormFieldTitle_productId}</th>
          <th>${uiLabelMap.FormFieldTitle_locationSeqId}</th>
          <th>${uiLabelMap.FacilityFacility}</th>
          <th>${uiLabelMap.FormFieldTitle_areaId}</th>
          <th>${uiLabelMap.FormFieldTitle_aisleId}</th>
          <th>${uiLabelMap.FormFieldTitle_sectionId}</th>
          <th>${uiLabelMap.FormFieldTitle_levelId}</th>
          <th>${uiLabelMap.FormFieldTitle_positionId}</th>
        </thead>
        <tbody>
          <#list processedProductFacilityLocations as processedProductFacilityLocation>
            <tr>
              <td>${processedProductFacilityLocation.productId?if_exists}</td>
              <td>${processedProductFacilityLocation.locationSeqId?if_exists}</td>
              <td>${productFacilityLocation.facilityId?if_exists}</td>
              <td>${productFacilityLocation.areaId?if_exists}</td>
              <td>${productFacilityLocation.aisleId?if_exists}</td>
              <td>${productFacilityLocation.sectionId?if_exists}</td>
              <td>${productFacilityLocation.levelId?if_exists}</td>
              <td>${productFacilityLocation.positionId?if_exists}</td>
              <td>
                <#if processedProductFacilityLocation.errorMessage?has_content>
                  ${processedProductFacilityLocation.errorMessage?if_exists}
                <#else>
                  ${processedProductFacilityLocation.successMessage?if_exists}
                </#if>
              </td>
            </tr>
          </#list>
        </tbody>
      </table>
    </#if>
  </div>
</div>