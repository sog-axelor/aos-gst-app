package com.axelor.apps.gst.impl;

import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.gst.db.State;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.Map;

public class PurchaseLineOrderGstImpl extends PurchaseOrderLineServiceProjectImpl {

  @Override
  public Map<String, BigDecimal> compute(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    Map<String, BigDecimal> compute = super.compute(purchaseOrderLine, purchaseOrder);

    State supplierState = purchaseOrder.getSupplierPartner().getMainAddress().getState();
    State companyState = purchaseOrder.getCompany().getAddress().getState();

    if (supplierState == null) {
      purchaseOrderLine.setSgst(BigDecimal.ZERO);
      purchaseOrderLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          purchaseOrderLine
              .getGstRate()
              .multiply(purchaseOrderLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      purchaseOrderLine.setIgst(total);
      compute.put("igst", total);
    }
    if (companyState != supplierState) {
      purchaseOrderLine.setSgst(BigDecimal.ZERO);
      purchaseOrderLine.setCgst(BigDecimal.ZERO);
      BigDecimal total =
          purchaseOrderLine
              .getGstRate()
              .multiply(purchaseOrderLine.getExTaxTotal())
              .divide(new BigDecimal(100));
      purchaseOrderLine.setIgst(total);
      compute.put("igst", total);
    } else {
      purchaseOrderLine.setIgst(BigDecimal.ZERO);
      BigDecimal SgstAndCgst =
          purchaseOrderLine
              .getGstRate()
              .multiply(purchaseOrderLine.getExTaxTotal())
              .divide(new BigDecimal(200));
      compute.put("sgst", SgstAndCgst);
      compute.put("cgst", SgstAndCgst);
      purchaseOrderLine.setSgst(SgstAndCgst);
      purchaseOrderLine.setCgst(SgstAndCgst);
    }

    return compute;
  }
}
