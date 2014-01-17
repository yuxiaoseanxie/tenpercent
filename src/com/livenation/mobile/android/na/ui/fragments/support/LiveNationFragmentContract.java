package com.livenation.mobile.android.na.ui.fragments.support;

import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

public interface LiveNationFragmentContract {
	LiveNationApiService getApiService();
	LocationHelper getLocationHelper();
}
