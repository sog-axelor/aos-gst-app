package com.axelor.apps.gst.impl;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.SaleOrderComputeServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderGstImpl extends SaleOrderComputeServiceSupplychainImpl {

  @Inject
  public SaleOrderGstImpl(
      SaleOrderLineService saleOrderLineService, SaleOrderLineTaxService saleOrderLineTaxService) {
    super(saleOrderLineService, saleOrderLineTaxService);
  }

  @Override
  public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
    SaleOrder cso = super.computeSaleOrder(saleOrder);
    
    if(saleOrder.getClientPartner().getGstIn()==null) {
    	cso.setGstin("");
    }else {
    	cso.setGstin(saleOrder.getClientPartner().getGstIn());
    }

    if (saleOrder.getSaleOrderLineList() == null) {
      saleOrder.setNetCgst(BigDecimal.ZERO);
      saleOrder.setNetIgst(BigDecimal.ZERO);
      saleOrder.setNetSgst(BigDecimal.ZERO);
    }
    BigDecimal igst =
        saleOrder.getSaleOrderLineList().stream()
            .map(SaleOrderLine::getIgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal cgst =
        saleOrder.getSaleOrderLineList().stream()
            .map(SaleOrderLine::getCgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal sgst =
        saleOrder.getSaleOrderLineList().stream()
            .map(SaleOrderLine::getSgst)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    saleOrder.setNetSgst(sgst);
    saleOrder.setNetIgst(igst);
    saleOrder.setNetCgst(cgst);
    cso.setTaxTotal(cso.getTaxTotal().add(sgst).add(cgst).add(igst));
    cso.setInTaxTotal(cso.getInTaxTotal().add(sgst).add(cgst).add(igst));

    return cso;
  }
}
