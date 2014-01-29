/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.LiveNationApiServiceImpl;

public class LiveNationApplication extends Application {
	private static LiveNationApplication instance;
	private LiveNationApiService serviceApi;
	private LocationHelper locationHelper;
	private ImageLoader imageLoader;
	private RequestQueue requestQueue;
	private EventsPresenter eventsPresenter;
	private SingleEventPresenter singleEventPresenter;
	private FeaturePresenter featurePresenter;
	private SingleVenuePresenter singleVenuePresenter;
	private VenueEventsPresenter venueEventsPresenter;
	
	public static LiveNationApplication get() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		serviceApi = new LiveNationApiServiceImpl("http", "api.livenation.com", Constants.clientId, Constants.deviceId, null, getApplicationContext());
		locationHelper = new LocationHelper();
		
		eventsPresenter = new EventsPresenter();
		singleEventPresenter = new SingleEventPresenter();
		featurePresenter = new FeaturePresenter();
		singleVenuePresenter = new SingleVenuePresenter();
		venueEventsPresenter = new VenueEventsPresenter();
		
		locationHelper.prepareCache(getApplicationContext());
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
		MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
		imageLoader = new ImageLoader(requestQueue, cache);
	}
	
	public LiveNationApiService getServiceApi() {
		return serviceApi;
	}
	
	public LocationHelper getLocationHelper() {
		return locationHelper;
	}
	
	public ImageLoader getImageLoader() {
		return imageLoader;
	}
	
	public EventsPresenter getEventsPresenter() {
		return eventsPresenter;
	}

	public SingleEventPresenter getSingleEventPresenter() {
		return singleEventPresenter;
	}

	public FeaturePresenter getFeaturePresenter() {
		return featurePresenter;
	}

	public SingleVenuePresenter getSingleVenuePresenter() {
		return singleVenuePresenter;
	}
	
	public VenueEventsPresenter getVenueEventsPresenter() {
		return venueEventsPresenter;
	}
}
