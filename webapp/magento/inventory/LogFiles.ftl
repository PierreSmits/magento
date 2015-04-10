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
    <h3 class="panel-title">${uiLabelMap.MagentoLogFiles}</h3>
  </div>
  <div class="panel-body">
    <#if partialContentList?has_content>
        <table class="table table-striped table-bordered table-hover table-responsive">
          <thead>
            <th>${uiLabelMap.Date}</th>
            <th>${uiLabelMap.UserLogin}</th>
            <th>${uiLabelMap.Status}</th>
            <th>${uiLabelMap.FileName}</th>
            <th>${uiLabelMap.Results}</th>
            <th>${uiLabelMap.Logs}</th>
          </thead>
          <tbody>
            <#list partialContentList as content>
              <tr>
                <#assign errorContent = "" />
                <#assign resultContent = "" />
                <td>${content.createdDate?if_exists}</td>
                <td>${content.createdByUserLogin?if_exists}</td>
                <td><#if content.statusId == "PROD_FAC_CSV_SUCCESS">${uiLabelMap.Success}<#elseif content.statusId == "PROD_FAC_CSV_PARTIAL">${uiLabelMap.PartialCompleted}<#elseif content.statusId == "PROD_FAC_CSV_FAIL">${uiLabelMap.Fail}<#else>${uiLabelMap.MagentoNotLoaded}</#if></td>
                <td><a href="<@ofbizUrl>DownloadLogFile?contentId=${content.contentId}&isCsv=Y</@ofbizUrl>" target="_blank">Uploaded_Data_${content.contentId?if_exists}.csv</a></td>
                <#if contentAssocMap.get(content.contentId)?has_content>
                <#assign contentAssocList = contentAssocMap.get(content.contentId)?if_exists>
                <#if contentAssocList?has_content>
                  <#assign resultContent = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(Static["org.ofbiz.entity.util.EntityUtil"].filterByCondition(contentAssocList, Static["org.ofbiz.entity.condition.EntityCondition"].makeCondition("caContentAssocTypeId", Static["org.ofbiz.entity.condition.EntityOperator"].EQUALS, "PROD_FAC_CSV_RESULT")))?if_exists>
                  <#assign errorContent = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(Static["org.ofbiz.entity.util.EntityUtil"].filterByCondition(contentAssocList, Static["org.ofbiz.entity.condition.EntityCondition"].makeCondition("caContentAssocTypeId", Static["org.ofbiz.entity.condition.EntityOperator"].EQUALS, "PROD_FAC_CSV_ERROR")))?if_exists>
                </#if>
                </#if>
                <td><#if resultContent?has_content><a href="<@ofbizUrl>DownloadLogFile?contentId=${resultContent.caContentIdTo}</@ofbizUrl>" target="_blank">${resultContent.contentName?if_exists}</a></#if></td>
                <td><#if errorContent?has_content><a href="<@ofbizUrl>DownloadLogFile?contentId=${errorContent.caContentIdTo}</@ofbizUrl>" target="_blank">${errorContent.contentName?if_exists}</a></#if></td>
              </tr>
            </#list>
          </tbody>
        </table>
    <#else>
      <h6>${uiLabelMap.MagentoNoErrorOrResultLogFilesExist}</h6>
    </#if>
  </div>
</div>
