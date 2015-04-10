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
<div class="panel panel-default" id="schedule_services_panel" role="tablist" aria-multiselectable="true">
  <div class="panel-heading" role="tab" id="schedule_services_panel_heading">
    <h5>
      <a data-toggle="collapse" data-parent="#schedule_services_panel" href="#schedule_services_panel_body" aria-expanded="true" aria-controls="collapse_schedule_services_panel_body">
        <i class="fa fa-lg fa-clock-o"></i> <b>${uiLabelMap.MagentoScheduleServices}</b>
      </a>
    </h5>
  </div>
  <div class="panel-body panel-collapse collapse in" id="schedule_services_panel_body" role="tabpanel" aria-labelledby="#schedule_services_panel_heading">
    <div class="row">
      <div class="col-lg-4 col-md-4">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoImportPendingOrdersFromMagento}</b></h5>
            <p>${uiLabelMap.MagentoImportPendingOrdersFromMagentoInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>importPendingOrdersFromMagento</@ofbizUrl>" data-dataSyncImage="Y"><i class="fa fa-play"></i> ${uiLabelMap.CommonRun}</a>
          </div>
        </div>
      </div>
      <div class="col-lg-4 col-md-4">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoUpdateHeldOrdersStatusFromMagento}</b></h5>
            <p>${uiLabelMap.MagentoUpdateHeldOrdersStatusInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>importHeldOrdersFromMagento</@ofbizUrl>" data-dataSyncImage="Y"><i class="fa fa-play"></i> ${uiLabelMap.CommonRun}</a>
          </div>
        </div>
      </div>
      <div class="col-lg-4 col-md-4">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoUpdateCancelledOrdersStatusFromMagento}</b></h5>
            <p>${uiLabelMap.MagentoUpdateCancelledOrdersStatusInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>importCancelledOrdersFromMagento</@ofbizUrl>" data-dataSyncImage="Y"><i class="fa fa-play"></i> ${uiLabelMap.CommonRun}</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>