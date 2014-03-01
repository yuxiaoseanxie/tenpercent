package com.livenation.mobile.android.na2.presenters.views;

import java.util.List;

import com.livenation.mobile.android.na2.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;

public interface FeatureView extends PresenterView {
	void setFeatured(List<Chart> featured);
}
