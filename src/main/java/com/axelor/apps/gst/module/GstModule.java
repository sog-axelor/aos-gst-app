package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gst.impl.GstImplmentation;
import com.axelor.apps.gst.service.GstService;

public class GstModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(GstService.class).to(GstImplmentation.class);
	}
}
