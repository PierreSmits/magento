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
<form method="post" action="<@ofbizUrl>updateMagentoConfiguration</@ofbizUrl>" class="form-vertical requireValidation ajaxMe" data-successMethod="#common-container" data-successMethod="#common-container" data-ajax-loader="#updateMagentoConfiguration-ajax-loader">
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
      <button type="submit" class="btn btn-primary">${uiLabelMap.CommonSave}
        <span id="updateMagentoConfiguration-ajax-loader" class="ajax-loader" style="display:none"></span>
      </button>
    </div>
    <div class="col-lg-2 col-md-2">
      <input class="btn btn-default" type="reset" value="${uiLabelMap.CommonReset}" />
    </div>
  </div>
</form>