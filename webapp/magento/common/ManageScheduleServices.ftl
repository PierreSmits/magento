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
            <a class="btn btn-default" href="<@ofbizUrl>importPendingOrdersFromMagento</@ofbizUrl>"><i class="fa fa-download"></i> ${uiLabelMap.CommonImport}</a>
          </div>
        </div>
      </div>
      <div class="col-lg-4 col-md-4">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoSyncHeldOrders}</b></h5>
            <p>${uiLabelMap.MagentoSyncHeldOrdersInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>importHeldOrdersFromMagento</@ofbizUrl>"><i><img src="../img/interact.png"/></i> ${uiLabelMap.MagentoSync}</a>
          </div>
        </div>
      </div>
      <div class="col-lg-4 col-md-4">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoSyncCancelledOrders}</b></h5>
            <p>${uiLabelMap.MagentoSyncCancelledOrdersInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>importCancelledOrdersFromMagento</@ofbizUrl>"><i><img src="../img/interact.png"/></i> ${uiLabelMap.MagentoSync}</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>