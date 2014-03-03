/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.app;

import android.app.Application;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na2.helpers.DummySsoProvider;
import com.livenation.mobile.android.na2.helpers.LocationHelper;
import com.livenation.mobile.android.na2.helpers.SsoManager;
import com.livenation.mobile.android.na2.notifications.PushReceiver;
import com.livenation.mobile.android.na2.presenters.AccountPresenters;
import com.livenation.mobile.android.na2.presenters.EventsPresenter;
import com.livenation.mobile.android.na2.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na2.presenters.FeaturePresenter;
import com.livenation.mobile.android.na2.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na2.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na2.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na2.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.LiveNationApiServiceImpl;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;

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
	private AccountPresenters accountPresenters;
	private NearbyVenuesPresenter nearbyVenuesPresenter;
	private FavoritesPresenter favoritesPresenter;
	private SsoManager ssoManager;
	
	public static LiveNationApplication get() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		ssoManager = new SsoManager();
		
		ApiSsoProvider ssoTokenProvider = ssoManager.getConfiguredSsoProvider(getApplicationContext());
		
		if (null == ssoTokenProvider) {
			ssoTokenProvider = new DummySsoProvider();
		}

		serviceApi = new LiveNationApiServiceImpl("https", "stg-faceoff.herokuapp.com", Constants.clientId, Constants.deviceId, ssoTokenProvider, getApplicationContext());
		locationHelper = new LocationHelper();
		
		eventsPresenter = new EventsPresenter();
		singleEventPresenter = new SingleEventPresenter();
		featurePresenter = new FeaturePresenter();
		singleVenuePresenter = new SingleVenuePresenter();
		venueEventsPresenter = new VenueEventsPresenter();
		accountPresenters = new AccountPresenters(getSsoManager());
		nearbyVenuesPresenter = new NearbyVenuesPresenter();
		favoritesPresenter = new FavoritesPresenter();
		
		locationHelper.prepareCache(getApplicationContext());
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
		MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
		imageLoader = new ImageLoader(requestQueue, cache);

        setupNotifications();
	}

    private void setupNotifications()
    {
        Logger.logLevel = Log.VERBOSE;
        UAirship.takeOff(this);

        PushManager.enablePush();

        BasicPushNotificationBuilder notificationBuilder = new BasicPushNotificationBuilder();
        PushManager.shared().setNotificationBuilder(notificationBuilder);
        PushManager.shared().setIntentReceiver(PushReceiver.class);
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
	
	public NearbyVenuesPresenter getNearbyVenuesPresenter() {
		return nearbyVenuesPresenter;
	}
	
	public AccountPresenters getAccountPresenters() {
		return accountPresenters;
	}
	
	public FavoritesPresenter getFavoritesPresenter() {
		return favoritesPresenter;
	}
	
	public SsoManager getSsoManager() {
		return ssoManager;
	}
}
