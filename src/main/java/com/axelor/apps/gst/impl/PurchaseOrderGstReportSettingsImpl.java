package com.axelor.apps.gst.impl;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class PurchaseOrderGstReportSettingsImpl extends PurchaseOrderPrintServiceImpl {

  @Inject
  public PurchaseOrderGstReportSettingsImpl(
      AppPurchaseService appPurchaseService, AppBaseService appBaseService) {
    super(appPurchaseService, appBaseService);
  }

  @Override
  public ReportSettings prepareReportSettings(PurchaseOrder purchaseOrder, String formatPdf)
      throws AxelorException {
    if (purchaseOrder.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MISSING_PRINTING_SETTINGS),
              purchaseOrder.getPurchaseOrderSeq()),
          purchaseOrder);
    }
    String locale = ReportSettings.getPrintingLocale(purchaseOrder.getSupplierPartner());
    String title = getFileName(purchaseOrder);
    AppBase appBase = appBaseService.getAppBase();
    ReportSettings reportSetting =
        ReportFactory.createReport("po1.rptdesign", title + " - ${date}");

    return reportSetting
        .addParam("PurchaseOrderId", purchaseOrder.getId())
        .addParam(
            "Timezone",
            purchaseOrder.getCompany() != null ? purchaseOrder.getCompany().getTimezone() : null)
        .addParam("Locale", locale)
        .addParam(
            "GroupProducts",
            appBase.getIsRegroupProductsOnPrintings()
                && purchaseOrder.getGroupProductsOnPrintings())
        .addParam("GroupProductTypes", appBase.getRegroupProductsTypeSelect())
        .addParam("GroupProductLevel", appBase.getRegroupProductsLevelSelect())
        .addParam("GroupProductProductTitle", appBase.getRegroupProductsLabelProducts())
        .addParam("GroupProductServiceTitle", appBase.getRegroupProductsLabelServices())
        .addParam("HeaderHeight", purchaseOrder.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", purchaseOrder.getPrintingSettings().getPdfFooterHeight())
        .addParam(
            "AddressPositionSelect", purchaseOrder.getPrintingSettings().getAddressPositionSelect())
        .addFormat(formatPdf);
  }
}
