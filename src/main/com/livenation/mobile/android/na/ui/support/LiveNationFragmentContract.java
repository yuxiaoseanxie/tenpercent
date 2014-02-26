/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

public interface LiveNationFragmentContract {
	LiveNationApiService getApiService();
	LocationHelper getLocationHelper();
	ImageLoader getImageLoader();
	
	EventsPresenter getEventsPresenter();
	FeaturePresenter getFeaturePresenter();
	NearbyVenuesPresenter getNearbyVenuesPresenter();
	FavoritesPresenter getFavoritesPresenter();
}
