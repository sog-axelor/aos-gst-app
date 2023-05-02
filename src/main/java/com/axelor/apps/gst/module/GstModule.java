package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.gst.impl.GstImplmentation;
import com.axelor.apps.gst.impl.InvoiceGstImpl;
import com.axelor.apps.gst.impl.InvoiceLineGstImpl;
import com.axelor.apps.gst.impl.PurchaseLineOrderImpl;
import com.axelor.apps.gst.impl.invoiceGstPrintReportImpl;
import com.axelor.apps.gst.service.GstService;
import com.axelor.apps.gst.service.InvoiceGstService;

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
    bind(PurchaseOrderLineServiceProjectImpl.class).to(PurchaseLineOrderImpl.class);
  }
}
