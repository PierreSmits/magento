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