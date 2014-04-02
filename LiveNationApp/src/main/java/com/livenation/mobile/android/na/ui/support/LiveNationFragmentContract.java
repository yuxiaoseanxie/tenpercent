/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationSetsPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationsPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;

public interface LiveNationFragmentContract {
	LocationManager getLocationManager();
	ImageLoader getImageLoader();

    SingleEventPresenter getSingleEventPresenter();
	EventsPresenter getEventsPresenter();
	FeaturePresenter getFeaturePresenter();
	NearbyVenuesPresenter getNearbyVenuesPresenter();
	FavoritesPresenter getFavoritesPresenter();
    AccountPresenters getAccountPresenters();
    RecommendationsPresenter getRecommendationsPresenter();
    RecommendationSetsPresenter getRecommendationSetsPresenter();
}
