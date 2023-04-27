package com.axelor.apps.gst.impl;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.service.InvoiceLineGstService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceLineGstImpl extends InvoiceLineProjectServiceImpl
    implements InvoiceLineGstService {

  @Inject
  public InvoiceLineGstImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService,
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      SupplierCatalogService supplierCatalogService,
      TaxService taxService,
      InternationalService internationalService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        accountManagementAccountService,
        productCompanyService,
        invoiceLineRepo,
        appBaseService,
        accountConfigService,
        invoiceLineAnalyticService,
        supplierCatalogService,
        taxService,
        internationalService);
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

  @Override
  public void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
    super.compute(invoice, invoiceLine);

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(invoiceLine, invoice.getInAti());

    invoiceLine.setPriceDiscounted(priceDiscounted);

    BigDecimal taxRate = BigDecimal.ZERO;
    if (invoiceLine.getTaxLine() != null) {
      taxRate = invoiceLine.getTaxLine().getValue();
      invoiceLine.setTaxRate(taxRate);
      invoiceLine.setTaxCode(invoiceLine.getTaxLine().getTax().getCode());
    }

    if (!invoice.getInAti()) {
      exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate.divide(new BigDecimal(100))));
    } else {
      inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    companyExTaxTotal = this.getCompanyExTaxTotal(exTaxTotal, invoice);
    companyInTaxTotal = this.getCompanyExTaxTotal(inTaxTotal, invoice);

    invoiceLine.setExTaxTotal(exTaxTotal);

    invoiceLine.setCompanyInTaxTotal(companyInTaxTotal);
    invoiceLine.setCompanyExTaxTotal(companyExTaxTotal);

    if (invoice.getCompany().getAddress().getState() != invoice.getAddress().getState()) {
      invoiceLine.setSgst(BigDecimal.ZERO);
      invoiceLine.setCgst(BigDecimal.ZERO);
      invoiceLine.setIgst(
          invoiceLine
              .getExTaxTotal()
              .multiply(invoiceLine.getGstRate())
              .divide(new BigDecimal(100)));
      invoiceLine.setInTaxTotal(
          inTaxTotal.add(
              invoiceLine
                  .getExTaxTotal()
                  .multiply(invoiceLine.getGstRate())
                  .divide(new BigDecimal(100))));
    } else {
      invoiceLine.setIgst(BigDecimal.ZERO);
      BigDecimal SgstAndCgst =
          invoiceLine
              .getExTaxTotal()
              .multiply(invoiceLine.getGstRate())
              .divide(new BigDecimal(2))
              .divide(new BigDecimal(100));
      invoiceLine.setSgst(SgstAndCgst);
      invoiceLine.setCgst(SgstAndCgst);
      invoiceLine.setInTaxTotal(inTaxTotal.add(SgstAndCgst.multiply(new BigDecimal(2))));
    }
  }
}
