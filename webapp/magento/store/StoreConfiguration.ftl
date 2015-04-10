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
  <div class="panel-heading clearfix">
    <div class="panel-title">
    <img src="../img/Int.png" width="65px"/>
    <#if magentoConfiguration?has_content>
      <a class="pull-right" data-tooltip-target="#testConnectionInfo" data-tooltip-title="${uiLabelMap.MagentoTestConnection}" href="<@ofbizUrl>testMagentoConnection</@ofbizUrl>"> <img src="../img/connect.gif" width="40px"/></a>
      <span id="testConnectionInfo" style="display:none;">
        <p>${uiLabelMap.MagentoTestConnectionInfo}</p>
      </span>
    </#if>
    </div>
  </div>
  <div class="panel-body">
    <form method="post" action="<@ofbizUrl>createUpdateMagentoConfiguration</@ofbizUrl>" class="form-vertical requireValidation">
      <#if magentoConfiguration?has_content>
        <input type="hidden" name="magentoConfigurationId" value="${(magentoConfiguration.magentoConfigurationId)!}">
      <#else>
        <input type="hidden" name="enumId" value="MAGENTO_SALE_CHANNEL">
      </#if>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="xmlRpcUserName">${uiLabelMap.MagentoSoapUserName}</label>
          <input type="text" id="xmlRpcUserName" name="xmlRpcUserName" data-label="${uiLabelMap.MagentoSoapUserName}" class="form-control required" value="${(magentoConfiguration.xmlRpcUserName)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="password">${uiLabelMap.CommonPassword}</label>
          <input type="password" id="password" name="password" class="required form-control" data-label="${uiLabelMap.CommonPassword}" value="${(magentoConfiguration.password)!}"/>
        </div>
      </div>
      <div class="form-group row">
        <div class="col-lg-6 col-md-6">
          <label for="serverUrl">${uiLabelMap.MagentoMagentoConnectUrl}</label>
          <small class="text-muted">(eg. http://magentohost/api/v2_soap?wsdl)</small>
          <input type="url" id="serverUrl" name="serverUrl" class="required form-control" data-label="${uiLabelMap.MagentoMagentoConnectUrl}" value="${(magentoConfiguration.serverUrl)!}"/>
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
