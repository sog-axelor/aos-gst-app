package com.axelor.apps.gst.impl;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.db.State;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceLineGstImpl extends InvoiceLineProjectServiceImpl {

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

  public void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
    super.compute(invoice, invoiceLine);

    if (invoiceLine.getProduct().getGstRate() == null) {
      invoiceLine.setGstRate(BigDecimal.ZERO);
    } else {
      invoiceLine.setGstRate(invoiceLine.getProduct().getGstRate());
    }

    if (invoiceLine.getProduct().getHsbn() == null) {
      invoiceLine.setHsbn(" ");
    } else {
      invoiceLine.setHsbn(invoiceLine.getProduct().getHsbn());
    }

    // for statewise gst implementations
    State companyState = invoice.getCompany().getAddress().getState();
    State partnerState = invoice.getAddress().getState();
    BigDecimal totalTaxValue = invoiceLine.getInTaxTotal();

    if (partnerState == null) {
      invoiceLine.setSgst(BigDecimal.ZERO);
      invoiceLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          invoiceLine
              .getGstRate()
              .multiply(invoiceLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      invoiceLine.setIgst(total);
      invoiceLine.setInTaxTotal(totalTaxValue.add(total));
    }

    if (companyState != partnerState) {
      invoiceLine.setSgst(BigDecimal.ZERO);
      invoiceLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          invoiceLine
              .getGstRate()
              .multiply(invoiceLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      invoiceLine.setIgst(total);
      invoiceLine.setInTaxTotal(totalTaxValue.add(total));
    } else {
      invoiceLine.setIgst(BigDecimal.ZERO);
      BigDecimal SgstAndCgst =
          invoiceLine
              .getGstRate()
              .multiply(invoiceLine.getExTaxTotal())
              .divide(new BigDecimal(200));
      invoiceLine.setSgst(SgstAndCgst);
      invoiceLine.setCgst(SgstAndCgst);
      invoiceLine.setInTaxTotal(totalTaxValue.add(SgstAndCgst));
    }
  }
}
