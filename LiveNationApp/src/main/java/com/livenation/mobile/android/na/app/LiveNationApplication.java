/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import com.adobe.mobile.Config;
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
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.analytics.TicketingAnalyticsBridge;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.ConfigFilePersistenceHelper;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.na.helpers.OrderHistoryUploadHelper;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.preferences.TicketingEnvironmentPreferences;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.providers.AccessTokenAppProvider;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.na.providers.DeviceIdAppProvider;
import com.livenation.mobile.android.na.providers.EnvironmentAppProvider;
import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.na.providers.sso.FacebookSsoProvider;
import com.livenation.mobile.android.na.providers.sso.GoogleSsoProvider;
import com.livenation.mobile.android.na.providers.sso.SsoAppManager;
import com.livenation.mobile.android.na.providers.sso.SsoProviderPersistence;
import com.livenation.mobile.android.platform.api.proxy.LiveNationProxy;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.provider.AccessTokenProvider;
import com.livenation.mobile.android.platform.sso.SsoManager;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;

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

public class LiveNationApplication extends Application {
    private static LiveNationApplication instance;
    private static SsoManager ssoManager;
    private static LocationManager locationProvider;
    private static ProviderManager providerManager;
    private static LiveNationProxy liveNationProxy;
    private static EnvironmentAppProvider environmentProvider;
    private static AccessTokenProvider accessTokenProvider;
    private static SsoProviderPersistence ssoProviderPersistence;
    private static ConfigFileProvider configFileProvider;
    private ImageLoader imageLoader;
    private EventsPresenter eventsPresenter;
    private VenueEventsPresenter venueEventsPresenter;
    private InboxStatusPresenter inboxStatusPresenter;
    private RequestQueue requestQueue;

    //Migration
    private String oldUserId;
    private BroadcastReceiver updateOldAppBroadcastReceiver;

    private final BroadcastReceiver updateSsoAccessTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            accessTokenProvider.getAccessToken(null);
        }
    };

    private BroadcastReceiver internetStateReceiver;
    private ConfigFilePersistenceHelper installedAppConfig;

    private boolean isMusicSync = false;

    public static LiveNationApplication get() {
        return instance;
    }

    public static SsoManager getSsoManager() {
        return ssoManager;
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

    public static EnvironmentAppProvider getEnvironmentProvider() {
        return environmentProvider;
    }

    public static AccessTokenProvider getAccessTokenProvider() {
        return accessTokenProvider;
    }

    public static SsoProviderPersistence getSsoProviderPersistence() {
        return ssoProviderPersistence;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //Object useful wild app
        providerManager = new ProviderManager();
        liveNationProxy = new LiveNationProxy(this);

        //Declare object used to start the library
        locationProvider = new LocationManager(this);
        environmentProvider = new EnvironmentAppProvider(this);
        accessTokenProvider = new AccessTokenAppProvider(liveNationProxy);
        ssoManager = new SsoAppManager(this);
        ssoManager.addSsoProvider(new FacebookSsoProvider(this));
        ssoManager.addSsoProvider(new GoogleSsoProvider(this));
        ssoProviderPersistence = new SsoProviderPersistence(this);
        SsoManager.AuthConfiguration configuration = ssoProviderPersistence.getAuthConfiguration();
        if (configuration != null) {
            ssoManager.setAuthConfiguration(configuration);
        }

        //Migration
        oldUserId = getIasId();
        if (oldUserId != null) {
            updateOldAppBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Props props = new Props();
                    props.put(AnalyticConstants.AIS_USER_ID, oldUserId);
                    LiveNationAnalytics.track(AnalyticConstants.UPDATED, AnalyticsCategory.HOUSEKEEPING, props);
                    LocalBroadcastManager.getInstance(LiveNationApplication.this).unregisterReceiver(updateOldAppBroadcastReceiver);
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(updateOldAppBroadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.MIGRATION_UPDATE_INTENT_FILTER));
        }

        //ssoProvider token - server update
        LocalBroadcastManager.getInstance(this).registerReceiver(updateSsoAccessTokenReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.SSO_ACCESS_TOKEN_UPDATE_INTENT_FILTER));


        //Start Library
        LiveNationLibrary.start(this, environmentProvider, new DeviceIdAppProvider(this), locationProvider, accessTokenProvider, oldUserId);
        LiveNationLibrary.setSsoProvider(ssoManager);
        LiveNationLibrary.setErrorTracker(new LibraryErrorTracker());

        Crashlytics.start(this);
        LiveNationAnalytics.initialize(this);

        //App init
        providerManager.getConfigReadyFor(ProviderManager.ProviderType.APP_INIT);

        eventsPresenter = new EventsPresenter();
        venueEventsPresenter = new VenueEventsPresenter();
        inboxStatusPresenter = new InboxStatusPresenter();

        int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
        MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();

        configFileProvider = new ConfigFileProvider(this, requestQueue);
        imageLoader = new ImageLoader(requestQueue, cache);

        installedAppConfig = new ConfigFilePersistenceHelper(this, configFileProvider);
        if (installedAppConfig.isUpdateAdvisable())
            installedAppConfig.update();

        setupNotifications();
        setupTicketing();
        setupInternetStateReceiver();


        //Analytics
        Props props = new Props();
        props.put(AnalyticConstants.FB_LOGGED_IN, LoginHelper.isUsingFacebook(this));
        props.put(AnalyticConstants.GOOGLE_LOGGED_IN, LoginHelper.isUsingGoogle(this));
        LiveNationAnalytics.track(AnalyticConstants.APPLICATION_OPEN, AnalyticsCategory.HOUSEKEEPING, props);

        //Omniture
        Config.setContext(getApplicationContext());

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
        UAirship.takeOff(this, airshipConfigOptions, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship airship) {
                airship.getPushManager().setPushEnabled(true);
            }
        });

        // Getting the UAirship instance asynchronously
        // Be carefull: Calling shared() now blocks the first time until airship is ready
        UAirship.shared(new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship airship) {
                airship.getPushManager().setPushEnabled(true);
                airship.getPushManager().setUserNotificationsEnabled(true);
                DefaultNotificationFactory notificationBuilder = new DefaultNotificationFactory(LiveNationApplication.this);
                notificationBuilder.setSmallIconId(R.drawable.ic_stat_notify);
                UAirship.shared().getPushManager().setNotificationFactory(notificationBuilder);
                NotificationsRegistrationManager notificationsRegistrationManager = NotificationsRegistrationManager.getInstance();
                if (notificationsRegistrationManager.shouldRegister())
                    notificationsRegistrationManager.register();
            }
        });
    }

    private void setupTicketing() {
        Ticketing.Config ticketingConfig = new Ticketing.Config();
        ticketingConfig.setContext(this);
        ticketingConfig.setImageLoader(getImageLoader());
        ticketingConfig.setAnalyticsHandler(new TicketingAnalyticsBridge());
        ticketingConfig.setPushTokenProvider(NotificationsRegistrationManager.getInstance());
        ticketingConfig.setOrderHistoryUploadHandler(new OrderHistoryUploadHelper());
        ticketingConfig.setEnvironment(getTicketingEnvironment(getApplicationContext()));
        Ticketing.init(ticketingConfig);
        Ticketing.setQaModeEnabled(BuildConfig.DEBUG);
    }

    private synchronized void setupInternetStateReceiver() {
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
                            unregisterInternetStateReceiver();
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

    private synchronized void unregisterInternetStateReceiver() {
        if (internetStateReceiver != null) {
            unregisterReceiver(internetStateReceiver);
            internetStateReceiver = null;
        }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateSsoAccessTokenReceiver);
    }

    public Ticketing.Environment getTicketingEnvironment(Context context) {
        TicketingEnvironmentPreferences preferences = new TicketingEnvironmentPreferences(context);
        return preferences.getConfiguredEnvironment();
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public VenueEventsPresenter getVenueEventsPresenter() {
        return venueEventsPresenter;
    }

    public InboxStatusPresenter getInboxStatusPresenter() {
        return inboxStatusPresenter;
    }

    public ConfigFilePersistenceHelper getInstalledAppConfig() {
        return installedAppConfig;
    }

    public boolean isMusicSync() {
        return isMusicSync;
    }

    public void setIsMusicSync(boolean isMusicSync) {
        this.isMusicSync = isMusicSync;
    }

    public static ConfigFileProvider getConfigFileProvider() {
        return configFileProvider;
    }

    private String getIasId() {
        SharedPreferences prefs = getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.SharedPreferences.INSTALLATION_ID, null);
    }

}