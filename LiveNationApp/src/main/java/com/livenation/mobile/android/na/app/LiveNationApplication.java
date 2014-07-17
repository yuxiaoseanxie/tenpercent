/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.util.Log;

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
import com.livenation.mobile.android.na.analytics.TicketingAnalyticsBridge;
import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.na.helpers.OrderHistoryUploadHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.notifications.PushReceiver;
import com.livenation.mobile.android.na.preferences.EnvironmentPreferences;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.providers.DeviceIdProviderImpl;
import com.livenation.mobile.android.na.youtube.YouTubeClient;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.provider.ProviderManager;
import com.livenation.mobile.android.platform.api.proxy.LiveNationProxy;
import com.livenation.mobile.android.platform.setup.LivenationLib;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.segment.android.Analytics;
import com.segment.android.models.Props;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;

public class LiveNationApplication extends Application {
    private static LiveNationApplication instance;
    private static SsoManager ssoManager;
    private ImageLoader imageLoader;
    private EventsPresenter eventsPresenter;
    private SingleEventPresenter singleEventPresenter;
    private SingleArtistPresenter singleArtistPresenter;
    private ArtistEventsPresenter artistEventsPresenter;
    private SingleVenuePresenter singleVenuePresenter;
    private VenueEventsPresenter venueEventsPresenter;
    private AccountPresenters accountPresenters;
    private FavoritesPresenter favoritesPresenter;
    private InboxStatusPresenter inboxStatusPresenter;
    private static LocationManager locationProvider;
    private static ProviderManager providerManager;
    private static LiveNationProxy liveNationProxy;

    //Migration
    private String oldUserId;
    private final BroadcastReceiver updateOldAppBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Props props = new Props();
            props.put(AnalyticConstants.AIS_USER_ID, oldUserId);
            LiveNationAnalytics.track(AnalyticConstants.UPDATED, AnalyticsCategory.HOUSEKEEPING, props);
        }
    };
    private BroadcastReceiver internetStateReceiver;

    private ConfigManager configManager;
    private boolean isMusicSync = false;

    public static LiveNationApplication get() {
        return instance;
    }

    public static SsoManager getSsoManager() {
        return ssoManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Declare object used to start the library
        EnvironmentPreferences environmentPreferences = new EnvironmentPreferences(this);
        locationProvider = new LocationManager(this);

        //Migration
        oldUserId = getIasId();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateOldAppBroadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.MIGRATION_UPDATE_INTENT_FILTER));

        //Start Library
        LiveNationLibrary.start(this, environmentPreferences.getConfiguredEnvironment(), new DeviceIdProviderImpl(this), locationProvider, oldUserId);
        Crashlytics.start(this);
        Analytics.initialize(this);

        //Object useful wild app
        providerManager = new ProviderManager();
        liveNationProxy = new LiveNationProxy();


        instance = this;

        ssoManager = new SsoManager(new DummySsoProvider());

        //App init
        providerManager.getConfigReadyFor(ProviderManager.ProviderType.APP_INIT);

        configManager = new ConfigManager(getApplicationContext(), ssoManager);

        eventsPresenter = new EventsPresenter();
        singleEventPresenter = new SingleEventPresenter();
        singleVenuePresenter = new SingleVenuePresenter();
        singleArtistPresenter = new SingleArtistPresenter();
        artistEventsPresenter = new ArtistEventsPresenter();
        venueEventsPresenter = new VenueEventsPresenter();
        accountPresenters = new AccountPresenters();
        favoritesPresenter = new FavoritesPresenter();
        inboxStatusPresenter = new InboxStatusPresenter();

        int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
        MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
        imageLoader = new ImageLoader(Volley.newRequestQueue(getApplicationContext()), cache);

        //Start and setup the library
        LivenationLib.start();
        LivenationLib.setErrorTracker(new LibraryErrorTracker());

        YouTubeClient.initialize(this, getString(R.string.youtube_api_key));

        setupNotifications();
        setupTicketing();
        setupInternetStateReceiver();



        //Analytics
        Props props = new Props();
        props.put(AnalyticConstants.FB_LOGGED_IN, LoginHelper.isUsingFacebook(this));
        props.put(AnalyticConstants.GOOGLE_LOGGED_IN, LoginHelper.isUsingGoogle(this));
        LiveNationAnalytics.track(AnalyticConstants.APPLICATION_OPEN, AnalyticsCategory.HOUSEKEEPING, props);

        //Handle migration

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Object imageLoader = getImageLoader();
        if (imageLoader instanceof LruCache) {
            LruCache cache = (LruCache) imageLoader;
            cache.evictAll();
        }
    }

    private void setupNotifications() {
        Logger.logLevel = Log.VERBOSE;
        AirshipConfigOptions airshipConfigOptions = AirshipConfigOptions.loadDefaultOptions(this);
        airshipConfigOptions.inProduction = !BuildConfig.DEBUG;
        UAirship.takeOff(this, airshipConfigOptions);

        PushManager.enablePush();

        BasicPushNotificationBuilder notificationBuilder = new BasicPushNotificationBuilder();
        notificationBuilder.iconDrawableId = R.drawable.ic_stat_notify;
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
        ticketingConfig.setOrderHistoryUploadHandler(new OrderHistoryUploadHelper());
        ticketingConfig.setEnvironment(Ticketing.Environment.PRODUCTION);
        Ticketing.init(ticketingConfig);
        Ticketing.setQaModeEnabled(BuildConfig.DEBUG);
    }

    private void setupInternetStateReceiver() {
        internetStateReceiver = new BroadcastReceiver() {
            @Override
            public synchronized void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    checkInstalledAppForAnalytics();
                    MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
                    musicSyncHelper.syncMusic(context, new BasicApiCallback<Void>() {
                        @Override
                        public void onResponse(Void response) {
                            LiveNationApplication.get().setIsMusicSync(true);
                            if (internetStateReceiver != null) {
                                unregisterReceiver(internetStateReceiver);
                                internetStateReceiver = null;
                            }
                        }

                        @Override
                        public void onErrorResponse(LiveNationError error) {
                        }
                    });
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetStateReceiver, intentFilter);
    }

    private void checkInstalledAppForAnalytics() {
        Props props = new Props();
        for (final ExternalApplicationAnalytics application : ExternalApplicationAnalytics.values()) {
            final boolean isInstalled = AnalyticsHelper.isAppInstalled(application.getPackageName(), this);
            props.put(application.getPackageName(), isInstalled);
        }
        LiveNationAnalytics.track(AnalyticConstants.TRACK_URL_SCHEMES, AnalyticsCategory.HOUSEKEEPING, props);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (internetStateReceiver != null) {
            unregisterReceiver(internetStateReceiver);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateOldAppBroadcastReceiver);

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

    public SingleVenuePresenter getSingleVenuePresenter() {
        return singleVenuePresenter;
    }

    public VenueEventsPresenter getVenueEventsPresenter() {
        return venueEventsPresenter;
    }

    public AccountPresenters getAccountPresenters() {
        return accountPresenters;
    }

    public FavoritesPresenter getFavoritesPresenter() {
        return favoritesPresenter;
    }

    public InboxStatusPresenter getInboxStatusPresenter() {
        return inboxStatusPresenter;
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

    public static LocationManager getLocationProvider() {
        return locationProvider;
    }

    public static ProviderManager getProviderManager() {
        return providerManager;
    }

    public static LiveNationProxy getLiveNationProxy() {
        return liveNationProxy;
    }

    private String getIasId() {
        SharedPreferences prefs = getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.SharedPreferences.INSTALLATION_ID, null);
    }
}