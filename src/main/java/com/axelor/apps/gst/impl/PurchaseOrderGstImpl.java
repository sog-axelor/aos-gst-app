package com.axelor.apps.gst.impl;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.PurchaseOrderServiceProductionImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class PurchaseOrderGstImpl extends PurchaseOrderServiceProductionImpl {

  @Inject
  public PurchaseOrderGstImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      BudgetSupplychainService budgetSupplychainService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderLineService purchaseOrderLineService,
      ManufOrderRepository manufOrderRepo,
      AppProductionService appProductionService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {
    super(
        appSupplychainService,
        accountConfigService,
        appAccountService,
        appBaseService,
        purchaseOrderStockService,
        budgetSupplychainService,
        purchaseOrderLineRepository,
        purchaseOrderLineService,
        manufOrderRepo,
        appProductionService,
        partnerStockSettingsService,
        stockConfigService);
  }

  @Override
  public PurchaseOrder computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    PurchaseOrder po = super.computePurchaseOrder(purchaseOrder);

    if (purchaseOrder.getPurchaseOrderLineList() == null) {
      purchaseOrder.setNetCgst(BigDecimal.ZERO);
      purchaseOrder.setNetIgst(BigDecimal.ZERO);
      purchaseOrder.setNetSgst(BigDecimal.ZERO);
    }

    BigDecimal igst = purchaseOrder.getPurchaseOrderLineList().stream().map(PurchaseOrderLine::getIgst).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal cgst = purchaseOrder.getPurchaseOrderLineList().stream().map(PurchaseOrderLine::getCgst).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal sgst = purchaseOrder.getPurchaseOrderLineList().stream().map(PurchaseOrderLine::getSgst).reduce(BigDecimal.ZERO, BigDecimal::add);
    purchaseOrder.setNetIgst(igst);
    purchaseOrder.setNetSgst(sgst);
    purchaseOrder.setNetCgst(cgst);
    po.setTaxTotal(po.getTaxTotal().add(sgst).add(cgst).add(igst));
    po.setInTaxTotal(po.getInTaxTotal().add(sgst).add(cgst).add(igst));
    return po;
  }
}
