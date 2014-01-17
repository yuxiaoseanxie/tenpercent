package com.livenation.mobile.android.na.ui.fragments.support;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

import android.support.v4.app.ListFragment;

public abstract class LiveNationListFragment extends ListFragment implements LiveNationFragmentContract {
	
	@Override
	public LiveNationApiService getApiService() {
		return LiveNationApplication.get().getServiceApi();
	}
	
	@Override
	public LocationHelper getLocationHelper() {
		return LiveNationApplication.get().getLocationHelper();
	}	
}
