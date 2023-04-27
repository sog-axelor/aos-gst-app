package com.axelor.apps.gst.impl;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.gst.service.GstService;
import java.math.BigDecimal;

public class GstImplmentation implements GstService {

  @Override
  public Invoice calculateGst(Invoice invoice) {
    BigDecimal netIgst = BigDecimal.ZERO;
    BigDecimal netCgst = BigDecimal.ZERO;
    BigDecimal netSgst = BigDecimal.ZERO;
    BigDecimal totalGst = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;

    netIgst =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getIgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    netCgst =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getCgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    netSgst =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getSgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalTaxWithPrice =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getInTaxTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalTaxWithOutPrice =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getExTaxTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    totalGst = totalTaxWithPrice.subtract(totalTaxWithOutPrice);
    totalAmount = totalTaxWithOutPrice.add(totalGst);

    invoice.setTaxTotal(totalGst);
    invoice.setInTaxTotal(totalAmount);
    invoice.setNetCgst(netCgst);
    invoice.setNetIgst(netIgst);
    invoice.setNetSgst(netSgst);
    return invoice;
  }

  @Override
  public InvoiceLine calculateInvoiceLine(
      InvoiceLine invoiceLine, Company company, Address address) {
    BigDecimal gstTotal = invoiceLine.getExTaxTotal();
    BigDecimal gstWithTax = invoiceLine.getInTaxTotal();
    BigDecimal tax = invoiceLine.getTaxLine().getValue();

    if (company.getAddress().getState() != address.getState()) {
      invoiceLine.setSgst(BigDecimal.ZERO);
      invoiceLine.setCgst(BigDecimal.ZERO);

      // get igst
      BigDecimal igst = gstTotal.multiply(invoiceLine.getGstRate()).divide(new BigDecimal(100));
      invoiceLine.setIgst(igst);

      gstWithTax = gstTotal.add(gstTotal.multiply(tax)).add(igst);
      invoiceLine.setInTaxTotal(gstWithTax);
    } else {
      invoiceLine.setIgst(BigDecimal.ZERO);
      BigDecimal SgstAndCgst =
          gstTotal
              .multiply(invoiceLine.getGstRate())
              .divide(new BigDecimal(2))
              .divide(new BigDecimal(100));
      invoiceLine.setSgst(SgstAndCgst);
      invoiceLine.setCgst(SgstAndCgst);

      gstWithTax = gstTotal.add(gstTotal.multiply(tax)).add(SgstAndCgst);
      invoiceLine.setInTaxTotal(gstWithTax);
    }

    return invoiceLine;
  }
}
