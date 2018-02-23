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
<html>
  <#if session.getAttribute("userLogin")?has_content>
    <#if page?has_content && page.permission?has_content && page.action?has_content && !security.hasEntityPermission(page.permission, page.action, session)>
      <#assign hasPermission = false>
    <#elseif userHasPermission?has_content && !(userHasPermission?default('N') == 'Y')>
      <#assign hasPermission = false>
    </#if>
  </#if>
  <head>
    <title>${layoutSettings.companyName}<#if hasPermission?default(true)><#if (page.titleProperty)?has_content>: ${StringUtil.wrapString(uiLabelMap[page.titleProperty])}<#else>${(page.title)?if_exists}</#if></#if></title>
    <meta name="viewport" content="width=device-width, user-scalable=no"/>
    <#if layoutSettings.shortcutIcon?has_content>
      <#assign shortcutIcon = layoutSettings.shortcutIcon/>
    </#if>
    <#if shortcutIcon?has_content>
      <link rel="shortcut icon" href="<@ofbizContentUrl>${StringUtil.wrapString(shortcutIcon)}</@ofbizContentUrl>" />
    </#if>
    <#list styleSheets as styleSheet>
      <link rel="stylesheet" href="${styleSheet}" type="text/css"/>
    </#list>
    <#list javaScripts as javaScript>
      <script type="text/javascript" src="${javaScript}" ></script>
    </#list>
  </head>
  <body>
    <div class="container">
      <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
          <div class="navbar-header">
            <a class="navbar-brand" href="#">
              <i class="fa fa-gears fa-lg"></i> ${uiLabelMap.MagentoMagentoIntegrationManager}
            </a>
          </div>
          <div class="collapse navbar-collapse" >
            <ul class="nav navbar-nav navbar-right">
              <li>
                <form method="post" action="<@ofbizUrl>setMagentoStore</@ofbizUrl>" class="navbar-form js-change-submit">
                  <div class="form-group">
                    <#if facilityList?has_content>
                      <#if (magentoStoreList.size() > 1)>
                        <select name="productStoreId" id="productStoreId" data-label="${uiLabelMap.MagentoProductStore}" class="form-control">
                          <#list magentoStoreList as store>
                            <option value="${(store.productStoreId)!}" title="${(store.storeName)!}" <#if magentoStore?has_content && magentoStore.productStoreId == store.productStoreId>selected="selected"</#if>> ${(store.storeName)!}</option>
                          </#list>
                        </select>
                      </#if>
                      <#if (magentoStoreList.size() = 1)>
                        <#assign store = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFirst(magentoStoreList) />
                        <input type="text" class="form-control" value="${store.storeName!}" disabled=disabled/>
                      </#if>
                    </#if>
                  </div>
                </form>
              </li>
              <li>
                <form method="post" action="" class="navbar-form">
                  <div class="form-group">
                    <#if facilityList?has_content>
                      <#if (facilityList.size() > 1)>
                        <select name="facilityId" id="facilityId" class="form-control productStoreAndFacility" data-label="${uiLabelMap.MagentoFacility}" data-ajax-update="#common-container" data-update-url="<@ofbizUrl>setMagentoStoreAndWarehouse</@ofbizUrl>" data-param-source=".productStoreAndFacility">
                          <option value=''>${uiLabelMap.CommonSelect}</option>
                          <#list facilityList as magentoFacility>
                            <option value='${(magentoFacility.facilityId)!}' <#if facility?has_content && facility.facilityId == magentoFacility.facilityId>selected="selected"</#if>>${(magentoFacility.facilityName)!}</option>
                          </#list>
                        </select>
                      </#if>
                      <#if (facilityList.size() = 1)>
                        <#assign facility = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFirst(facilityList) />
                        <input type="text" class="form-control" value="${facility.facilityName!}" disabled=disabled/>
                      </#if>
                    </#if>
                  </div>
                </form>
              </li>
              <#if magentoConfiguration?has_content && magentoStoreList?has_content>
                <li>
                  <a href="<@ofbizUrl>testMagentoConnectionFromContainer</@ofbizUrl>" class="pull-right" title="${uiLabelMap.MagentoTestConnection}" data-dataSyncImage="Y"><i class="fa fa-bolt fa-lg"></i></a>
                </li>
                <li>
                  <a href="<@ofbizUrl>UpdateMagentoConfiguration</@ofbizUrl>" data-dialog-href="<@ofbizUrl>UpdateMagentoConfiguration</@ofbizUrl>" data-dialog-width="half" title="${uiLabelMap.MagentoUpdateConfiguration}" class="pull-right"><i class="fa fa-cog fa-lg"></i></a>
                </li>
              </#if>
              <li>
                <a href="<@ofbizUrl>logout</@ofbizUrl>" title="${uiLabelMap.CommonLogout}"><i class="fa fa-sign-out fa-lg"></i></a>
              </li>
            </ul>
          </div>
        </div>
      </nav>
      <div class="row">
        <div class="col-md-12">
          <div id="notification-messages">
            ${screens.render("component://magento/widget/magento/CommonScreens.xml#Messages")}
          </div>