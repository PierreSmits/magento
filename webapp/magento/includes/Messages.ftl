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
<#-- display the error messages -->
<#if errorNotificationMessageList?has_content>
  <#list errorNotificationMessageList as errorMsg>
    <!--TODO: alert-error is renamed to alert-danger class in bootstrap 3.0
     so alert-error class will be removed once all components will be migrated to bootstrap 3.0-->
    <div class="alert alert-block alert-error alert-danger fade in">
      <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i> </button>
      ${StringUtil.wrapString(errorMsg)?replace("<br/>", "")}
    </div>
  </#list>
</#if>
<#-- display the event messages -->
<#if eventNotificationMessageList?has_content>
  <#list eventNotificationMessageList as eventMsg>
    <div class="alert alert-block alert-success fade in">
      <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>
      ${StringUtil.wrapString(eventMsg)?replace("<br/>", "")}
    </div>
  </#list>
</#if>