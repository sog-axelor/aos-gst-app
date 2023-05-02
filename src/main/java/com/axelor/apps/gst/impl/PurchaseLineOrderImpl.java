package com.axelor.apps.gst.impl;

import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.Map;

public class PurchaseLineOrderImpl extends PurchaseOrderLineServiceProjectImpl {

	@Override
	public Map<String, BigDecimal> compute(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
			throws AxelorException {

		BigDecimal exTaxTotal;
		BigDecimal inTaxTotal;
		
		  if (purchaseOrder.getCompany().getAddress().getState() != purchaseOrder.getContactPartner().getMainAddress().getState()) {
			  
			  purchaseOrderLine.setSgst(BigDecimal.ZERO);
			  purchaseOrderLine.setCgst(BigDecimal.ZERO);
			  purchaseOrderLine.setIgst(
			  purchaseOrderLine.getExTaxTotal().multiply(purchaseOrderLine.getGstRate()).divide(new BigDecimal(100)));
			  
			  
		    } 
		    
		return super.compute(purchaseOrderLine, purchaseOrder);
	}
}
