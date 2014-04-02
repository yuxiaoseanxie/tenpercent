/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import android.app.Application;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.PushReceiver;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationSetsPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationsPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.mobilitus.tm.tickets.TicketLibrary;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;

public class LiveNationApplication extends Application {
    private static LiveNationApplication instance;
    private LocationManager locationManager;
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
    private InboxStatusPresenter inboxStatusPresenter;
    private RecommendationsPresenter recommendationsPresenter;
    private RecommendationSetsPresenter recommendationSetsPresenter;

    private ApiHelper apiHelper;

    public static LiveNationApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        ssoManager = new SsoManager(new DummySsoProvider());

        apiHelper = new ApiHelper(ssoManager, getApplicationContext());

        locationManager = new LocationManager(getApplicationContext());

        eventsPresenter = new EventsPresenter();
        singleEventPresenter = new SingleEventPresenter();
        featurePresenter = new FeaturePresenter();
        singleVenuePresenter = new SingleVenuePresenter();
        venueEventsPresenter = new VenueEventsPresenter();
        accountPresenters = new AccountPresenters(getSsoManager());
        nearbyVenuesPresenter = new NearbyVenuesPresenter();
        favoritesPresenter = new FavoritesPresenter();
        inboxStatusPresenter = new InboxStatusPresenter();
        recommendationsPresenter = new RecommendationsPresenter();
        recommendationSetsPresenter = new RecommendationSetsPresenter();

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
        MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
        imageLoader = new ImageLoader(requestQueue, cache);

        setupNotifications();
        setupTicketing();
    }


    private void setupNotifications() {
        Logger.logLevel = Log.VERBOSE;
        UAirship.takeOff(this);

        PushManager.enablePush();

        BasicPushNotificationBuilder notificationBuilder = new BasicPushNotificationBuilder();
        PushManager.shared().setNotificationBuilder(notificationBuilder);
        PushManager.shared().setIntentReceiver(PushReceiver.class);
    }

    private void setupTicketing() {
        TicketLibrary.getInstance().init(getApplicationContext(), "35abec5b15804303aaedbf6bdeb259b2", "tmus");

        //TODO: Kill this image loader crap
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);
    }


    public LocationManager getLocationManager() {
        return locationManager;
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

    public InboxStatusPresenter getInboxStatusPresenter() {
        return inboxStatusPresenter;
    }

    public RecommendationsPresenter getRecommendationsPresenter() {
        return recommendationsPresenter;
    }

    public RecommendationSetsPresenter getRecommendationSetsPresenter() {
        return recommendationSetsPresenter;
    }

    public ApiHelper getApiHelper() {
        return apiHelper;
    }

}