package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.gst.impl.GstImplmentation;
import com.axelor.apps.gst.impl.InvoiceGstImpl;
import com.axelor.apps.gst.impl.InvoiceLineGstImpl;
import com.axelor.apps.gst.impl.PurchaseLineOrderGstImpl;
import com.axelor.apps.gst.impl.PurchaseOrderGstImpl;
import com.axelor.apps.gst.impl.PurchaseOrderGstReportSettingsImpl;
import com.axelor.apps.gst.impl.SaleOrderGstImpl;
import com.axelor.apps.gst.impl.SaleOrderGstReportSettingimpl;
import com.axelor.apps.gst.impl.SaleOrderLineGstImpl;
import com.axelor.apps.gst.impl.invoiceGstPrintReportImpl;
import com.axelor.apps.gst.service.GstService;
import com.axelor.apps.gst.service.InvoiceGstService;
import com.axelor.apps.production.service.PurchaseOrderServiceProductionImpl;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderComputeServiceSupplychainImpl;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(GstService.class).to(GstImplmentation.class);

    // For invoice
    bind(InvoiceGstService.class).to(InvoiceGstImpl.class);
    bind(InvoiceLineProjectServiceImpl.class).to(InvoiceLineGstImpl.class);
    bind(InvoiceServiceManagementImpl.class).to(InvoiceGstImpl.class);
    bind(InvoicePrintServiceImpl.class).to(invoiceGstPrintReportImpl.class);

    // for Purchase
    bind(PurchaseOrderLineServiceProjectImpl.class).to(PurchaseLineOrderGstImpl.class);
    bind(PurchaseOrderServiceProductionImpl.class).to(PurchaseOrderGstImpl.class);
    bind(PurchaseOrderPrintServiceImpl.class).to(PurchaseOrderGstReportSettingsImpl.class);

    // for sales
    bind(SaleOrderLineBusinessProductionServiceImpl.class).to(SaleOrderLineGstImpl.class);
    bind(SaleOrderComputeServiceSupplychainImpl.class).to(SaleOrderGstImpl.class);
    bind(SaleOrderPrintServiceImpl.class).to(SaleOrderGstReportSettingimpl.class);
  }
}
