package com.axelor.apps.gst.impl;

import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.gst.db.State;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineGstImpl extends SaleOrderLineBusinessProductionServiceImpl {

  @Inject
  public SaleOrderLineGstImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService) {
    super(
        currencyService,
        priceListService,
        productMultipleQtyService,
        appBaseService,
        appSaleService,
        accountManagementService,
        saleOrderLineRepo,
        saleOrderService,
        appAccountService,
        analyticMoveLineService,
        appSupplychainService,
        accountConfigService,
        pricingService,
        taxService,
        saleOrderMarginService);
  }

  @Override
  public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Map<String, BigDecimal> cv = super.computeValues(saleOrder, saleOrderLine);

    // for rate
    if (saleOrderLine.getProduct().getGstRate() == null) {
      saleOrderLine.setGstRate(BigDecimal.ZERO);
      cv.put("gstRate", BigDecimal.ZERO);
    } else {
      saleOrderLine.setGstRate(saleOrderLine.getProduct().getGstRate());
      cv.put("gstRate", saleOrderLine.getProduct().getGstRate());
    }

    // for statewise gst implementations
    State companyState = saleOrder.getCompany().getAddress().getState();
    State partnerState = saleOrder.getMainInvoicingAddress().getState();

    if (partnerState == null) {
      saleOrderLine.setSgst(BigDecimal.ZERO);
      saleOrderLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          saleOrderLine
              .getGstRate()
              .multiply(saleOrderLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      saleOrderLine.setIgst(total);
      cv.put("igst", total);
    }

    if (companyState != partnerState) {
      saleOrderLine.setSgst(BigDecimal.ZERO);
      saleOrderLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          saleOrderLine
              .getGstRate()
              .multiply(saleOrderLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      saleOrderLine.setIgst(total);
      cv.put("igst", total);
    } else {
      saleOrderLine.setIgst(BigDecimal.ZERO);
      BigDecimal SgstAndCgst =
          saleOrderLine
              .getGstRate()
              .multiply(saleOrderLine.getExTaxTotal())
              .divide(new BigDecimal(200));
      cv.put("sgst", SgstAndCgst);
      cv.put("cgst", SgstAndCgst);
      saleOrderLine.setSgst(SgstAndCgst);
      saleOrderLine.setCgst(SgstAndCgst);
    }
    return cv;
  }
}
