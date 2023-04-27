package com.axelor.apps.gst.impl;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentService;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.gst.service.InvoiceGstService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceGstImpl extends InvoiceServiceManagementImpl implements InvoiceGstService {

  @Inject
  public InvoiceGstImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService,
      InvoiceTermService invoiceTermService,
      InvoiceTermPfpService invoiceTermPfpService,
      AppBaseService appBaseService,
      TaxService taxService,
      InvoiceProductStatementService invoiceProductStatementService,
      InvoiceLineRepository invoiceLineRepo,
      IntercoService intercoService,
      StockMoveRepository stockMoveRepository,
      InvoiceEstimatedPaymentService invoiceEstimatedPaymentService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService,
        moveToolService,
        invoiceTermService,
        invoiceTermPfpService,
        appBaseService,
        taxService,
        invoiceProductStatementService,
        invoiceLineRepo,
        intercoService,
        stockMoveRepository,
        invoiceEstimatedPaymentService);
  }

  @Override
  public Invoice compute(Invoice invoice) throws AxelorException {

    Invoice invoice1 = super.compute(invoice);

    BigDecimal netIgst = BigDecimal.ZERO;
    BigDecimal netCgst = BigDecimal.ZERO;
    BigDecimal netSgst = BigDecimal.ZERO;

    if (invoice.getInvoiceLineList() == null) {
      invoice.setNetCgst(BigDecimal.ZERO);
      invoice.setNetIgst(BigDecimal.ZERO);
      invoice.setNetSgst(BigDecimal.ZERO);
    }

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

    invoice.setNetCgst(netCgst);
    invoice.setNetIgst(netIgst);
    invoice.setNetSgst(netSgst);

 
	invoice1.setTaxTotal(invoice1.getTaxTotal().add(netIgst).add(netCgst).add(netSgst));
    invoice1.setInTaxTotal(invoice1.getTaxTotal().add(invoice1.getExTaxTotal()));

    
    return invoice1;
  }
}
