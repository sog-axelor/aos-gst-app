package com.axelor.apps.gst.controller;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.gst.impl.GstImplmentation;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.inject.Inject;

public class GstController {

  @Inject GstImplmentation gi;

  public void test(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    System.err.println(invoice);
  }

  public void invoiceGst(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = gi.calculateGst(invoice);
    response.setValues(invoice);
  }

  public void invoiceLineGst(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().getParent().asType(Invoice.class);
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

    Company company = invoice.getCompany();
    Address address = invoice.getAddress();

    invoiceLine = gi.calculateInvoiceLine(invoiceLine, company, address);
    response.setValue("igst", invoiceLine.getIgst());
    response.setValue("sgst", invoiceLine.getSgst());
    response.setValue("cgst", invoiceLine.getCgst());
    response.setValue("grossAmount", invoiceLine.getInTaxTotal());
  }
}
