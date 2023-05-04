package com.axelor.apps.gst.impl;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SaleOrderGstReportSettingimpl extends SaleOrderPrintServiceImpl {

  @Inject
  public SaleOrderGstReportSettingimpl(
      SaleOrderService saleOrderService, AppSaleService appSaleService) {
    super(saleOrderService, appSaleService);
  }

  @Override
  public ReportSettings prepareReportSettings(SaleOrder saleOrder, boolean proforma, String format)
      throws AxelorException {

    if (saleOrder.getPrintingSettings() == null) {
      if (saleOrder.getCompany().getPrintingSettings() != null) {
        saleOrder.setPrintingSettings(saleOrder.getCompany().getPrintingSettings());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            String.format(
                I18n.get(SaleExceptionMessage.SALE_ORDER_MISSING_PRINTING_SETTINGS),
                saleOrder.getSaleOrderSeq()),
            saleOrder);
      }
    }
    

    String locale = ReportSettings.getPrintingLocale(saleOrder.getClientPartner());

    String title = saleOrderService.getFileName(saleOrder);

    ReportSettings reportSetting =
        ReportFactory.createReport("SO1.rptdesign", title + " - ${date}");

    return reportSetting
        .addParam("SaleOrderId", saleOrder.getId())
        .addParam(
            "Timezone",
            saleOrder.getCompany() != null ? saleOrder.getCompany().getTimezone() : null)
        .addParam("Locale", locale)
        .addParam("ProformaInvoice", proforma)
        .addParam("HeaderHeight", saleOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", saleOrder.getPrintingSettings().getPdfFooterHeight())
        .addParam(
            "AddressPositionSelect", saleOrder.getPrintingSettings().getAddressPositionSelect())
        .addFormat(format);
  }
}
