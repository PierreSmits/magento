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
<div id="shippingInformation">
  <div class="panel panel-default" id="shipping_panel" role="tablist" aria-multiselectable="true">
    <div class="panel-heading" role="tab" id="shipping_panel_heading">
      <h5>
        <a data-toggle="collapse" data-parent="#shipping_panel" href="#shipping_panel_body" aria-expanded="true" aria-controls="collapse_shipping_panel_body">
          <i class="fa fa-lg fa-truck"></i>&nbsp<b>${uiLabelMap.CommonShipping}</b>
        </a>
      </h5>
    </div>
    <div class="panel-collapse collapse in" id="shipping_panel_body" role="tabpanel" aria-labelledby="#shipping_panel_heading">
      <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
        <#include "component://magento/webapp/magento/store/ShippingMethods.ftl"/>
      </div>
    </div>
  </div>
</div>