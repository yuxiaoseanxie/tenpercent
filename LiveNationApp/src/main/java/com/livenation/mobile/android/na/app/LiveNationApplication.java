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
import com.crashlytics.android.Crashlytics;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.analytics.LibraryErrorTracker;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.analytics.TicketingAnalyticsBridge;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.notifications.PushReceiver;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationSetsPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationsPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.youtube.YouTubeClient;
import com.livenation.mobile.android.platform.setup.LivenationLib;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

public class LiveNationApplication extends Application {
    private static LiveNationApplication instance;
    private LocationManager locationManager;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;
    private EventsPresenter eventsPresenter;
    private SingleEventPresenter singleEventPresenter;
    private SingleArtistPresenter singleArtistPresenter;
    private ArtistEventsPresenter artistEventsPresenter;
    private FeaturePresenter featurePresenter;
    private SingleVenuePresenter singleVenuePresenter;
    private VenueEventsPresenter venueEventsPresenter;
    private AccountPresenters accountPresenters;
    private NearbyVenuesPresenter nearbyVenuesPresenter;
    private FavoritesPresenter favoritesPresenter;
    private static SsoManager ssoManager;
    private InboxStatusPresenter inboxStatusPresenter;
    private RecommendationsPresenter recommendationsPresenter;
    private RecommendationSetsPresenter recommendationSetsPresenter;

    private ConfigManager configManager;
    private boolean isMusicSync = false;

    public static LiveNationApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);
        instance = this;

        ssoManager = new SsoManager(new DummySsoProvider());

        configManager = new ConfigManager(getApplicationContext(), ssoManager);

        locationManager = new LocationManager(getApplicationContext());

        eventsPresenter = new EventsPresenter();
        singleEventPresenter = new SingleEventPresenter();
        featurePresenter = new FeaturePresenter();
        singleVenuePresenter = new SingleVenuePresenter();
        singleArtistPresenter = new SingleArtistPresenter();
        artistEventsPresenter = new ArtistEventsPresenter();
        venueEventsPresenter = new VenueEventsPresenter();
        accountPresenters = new AccountPresenters();
        nearbyVenuesPresenter = new NearbyVenuesPresenter();
        favoritesPresenter = new FavoritesPresenter();
        inboxStatusPresenter = new InboxStatusPresenter();
        recommendationsPresenter = new RecommendationsPresenter();
        recommendationSetsPresenter = new RecommendationSetsPresenter();

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
        MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
        imageLoader = new ImageLoader(requestQueue, cache);

        //Start and setup the library
        LivenationLib.start();
        LivenationLib.setErrorTracker(new LibraryErrorTracker());

        YouTubeClient.initialize(this, getString(R.string.youtube_api_key));

        setupNotifications();
        setupTicketing();
        checkInstalledAppForAnalytics();

        getConfigManager().buildApi();
    }


    private void setupNotifications() {
        Logger.logLevel = Log.VERBOSE;
        AirshipConfigOptions airshipConfigOptions = AirshipConfigOptions.loadDefaultOptions(this);
        airshipConfigOptions.inProduction = !BuildConfig.DEBUG;
        UAirship.takeOff(this, airshipConfigOptions);

        PushManager.enablePush();

        BasicPushNotificationBuilder notificationBuilder = new BasicPushNotificationBuilder();
        PushManager.shared().setNotificationBuilder(notificationBuilder);
        PushManager.shared().setIntentReceiver(PushReceiver.class);

        NotificationsRegistrationManager notificationsRegistrationManager = NotificationsRegistrationManager.getInstance();
        if (notificationsRegistrationManager.shouldRegister())
            notificationsRegistrationManager.register();
    }

    private void setupTicketing() {
        Ticketing.Config ticketingConfig = new Ticketing.Config();
        ticketingConfig.setContext(this);
        ticketingConfig.setImageLoader(getImageLoader());
        ticketingConfig.setAnalyticsHandler(new TicketingAnalyticsBridge());
        ticketingConfig.setPushTokenProvider(NotificationsRegistrationManager.getInstance());
        ticketingConfig.setEnvironment(Ticketing.Environment.PRODUCTION);
        Ticketing.init(ticketingConfig);
    }

    private void checkInstalledAppForAnalytics() {
        Analytics.initialize(this);
        for (final ExternalApplicationAnalytics application : ExternalApplicationAnalytics.values()) {
            final boolean isInstalled = AnalyticsHelper.isAppInstalled(application.getPackageName(), this);
            Props props = new Props();
            props.put(application.getPackageName(), isInstalled);
            LiveNationAnalytics.track(AnalyticConstants.TRACK_URL_SCHEMES, AnalyticsCategory.HOUSEKEEPING, props);
        }
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

    public SingleArtistPresenter getSingleArtistPresenter() {
        return singleArtistPresenter;
    }

    public ArtistEventsPresenter getArtistEventsPresenter() {
        return artistEventsPresenter;
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

    public static SsoManager getSsoManager() {
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isMusicSync() {
        return isMusicSync;
    }

    public void setIsMusicSync(boolean isMusicSync) {
        this.isMusicSync = isMusicSync;
    }



}