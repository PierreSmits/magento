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