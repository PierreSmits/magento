<div class="panel panel-default" id="inventory_panel" role="tablist" aria-multiselectable="true">
  <div class="panel-heading" role="tab" id="inventory_panel_heading">
    <h5>
      <a data-toggle="collapse" data-parent="#inventory_panel" href="#inventory_panel_body" aria-expanded="true" aria-controls="collapse_inventory_panel_body">
        <i class="fa fa-lg fa-cubes"></i>&nbsp<b>${uiLabelMap.CommonInventory}</b>
      </a>
    </h5>
  </div>
  <div class="panel-body panel-collapse collapse in" id="inventory_panel_body" role="tabpanel" aria-labelledby="#inventory_panel_heading">
    <div class="row">
      <div class="col-lg-12 col-md-12">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoImportInventoryCountFromMagentoStore}</b></h5>
            <p>${uiLabelMap.MagentoImportInventoryCountFromMagentoStoreInfo}</p>
            <form method="post" action="<@ofbizUrl>importInventoryFromMagento</@ofbizUrl>" name="ImportInventoryFromMagento" class="form-vertical" enctype="multipart/form-data">
              <input type="hidden" name="productStoreId" value="${(magentoStore.productStoreId)!}"/>
              <input type="hidden" name="facilityId" value="${(facility.facilityId)!}"/>
              <button type="submit" class="btn btn-default"><i class="fa fa-download"></i> ${uiLabelMap.CommonImport}</button>
            </form>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12 col-md-12">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoExportProductCountWithWarehouseLocation}</b></h5>
            <p>${uiLabelMap.MagentoExportProductCountWithWarehouseLocationInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>DownloadWarehouseLocationCSV?productStoreId=${(magentoStore.productStoreId)!}&&facilityId=${(facility.facilityId)!}</@ofbizUrl>" target="_blank"><i class="fa fa-upload"></i> ${uiLabelMap.CommonExport}</a>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-12 col-md-12">
        <div class="thumbnail">
          <div class="caption">
            <h5><b>${uiLabelMap.MagentoUpdateAndImportCSVWithWarehouseLocation}</b></h5>
            <p>${uiLabelMap.MagentoUpdateAndImportCSVWithWarehouseLocationInfo}</p>
            <a class="btn btn-default" href="<@ofbizUrl>ImportWarehouseLocation?productStoreId=${(magentoStore.productStoreId)!}&&facilityId=${(facility.facilityId)!}</@ofbizUrl>"><i class="fa fa-download"></i> ${uiLabelMap.CommonImport}</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>