package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;

public interface InvoiceLineGstService {
  public InvoiceLine calculateInvoiceLine(
      InvoiceLine invoiceLine, Company company, Address address);
}
