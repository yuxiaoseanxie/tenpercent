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
import com.crashlytics.android.Crashlytics;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.analytics.LibraryErrorTracker;
import com.livenation.mobile.android.na.analytics.TicketingAnalyticsBridge;
import com.livenation.mobile.android.na.analytics.services.AmplitudeAnalytics;
import com.livenation.mobile.android.na.analytics.services.CrashlyticsAnalytics;
import com.livenation.mobile.android.na.analytics.services.GoogleAnalytics;
import com.livenation.mobile.android.na.helpers.ConfigFilePersistenceHelper;
import com.livenation.mobile.android.na.helpers.OrderHistoryUploadHelper;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.preferences.TicketingEnvironmentPreferences;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.na.providers.DeviceIdAppProvider;
import com.livenation.mobile.android.na.providers.EnvironmentAppProvider;
import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.na.providers.sso.FacebookSsoProvider;
import com.livenation.mobile.android.na.providers.sso.GoogleSsoProvider;
import com.livenation.mobile.android.platform.api.proxy.LiveNationProxy;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.provider.AccessTokenProvider;
import com.livenation.mobile.android.platform.sso.SsoManager;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.mobile.livenation.com.livenationui.LivenationUILibrary;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsHelper;
import android.mobile.livenation.com.livenationui.analytics.ConstantAnalytics;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.mobile.livenation.com.livenationui.persistence.ConstantSharedPreferences;
import android.mobile.livenation.com.livenationui.scan.MusicSyncHelper;
import android.mobile.livenation.com.livenationui.sso.LoginHelper;
import android.mobile.livenation.com.livenationui.sso.SsoProviderPersistence;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;

public class LiveNationApplication extends Application {
    private static LiveNationApplication instance;
    private static LocationManager locationProvider;
    private static ProviderManager providerManager;
    private static EnvironmentAppProvider environmentProvider;
    private static ConfigFileProvider configFileProvider;
    private VenueEventsPresenter venueEventsPresenter;
    private InboxStatusPresenter inboxStatusPresenter;
    //Migration
    private String oldUserId;
    private BroadcastReceiver updateOldAppBroadcastReceiver;

    private final BroadcastReceiver updateSsoAccessTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getAccessTokenProvider().getAccessToken(null);
        }
    };

    private BroadcastReceiver internetStateReceiver;
    private ConfigFilePersistenceHelper installedAppConfig;

    public static LiveNationApplication get() {
        return instance;
    }

    public static SsoManager getSsoManager() {
        return LivenationUILibrary.getInstance().getSsoManager();
    }

    public static LocationManager getLocationProvider() {
        return locationProvider;
    }

    public static ProviderManager getProviderManager() {
        return providerManager;
    }

    public static LiveNationProxy getLiveNationProxy() {
        return LivenationUILibrary.getInstance().getLiveNationProxy();
    }

    public static EnvironmentAppProvider getEnvironmentProvider() {
        return environmentProvider;
    }

    public static AccessTokenProvider getAccessTokenProvider() {
        return LivenationUILibrary.getInstance().getAccessTokenProvider();
    }

    public static SsoProviderPersistence getSsoProviderPersistence() {
        return LivenationUILibrary.getInstance().getSsoProviderPersistence();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //UI library
        LivenationUILibrary.getInstance().start(this);
        getSsoManager().addSsoProvider(new FacebookSsoProvider(this));
        getSsoManager().addSsoProvider(new GoogleSsoProvider(this));

        //Object useful wild app
        providerManager = new ProviderManager();

        //Declare object used to start the library
        locationProvider = new LocationManager(this);
        environmentProvider = new EnvironmentAppProvider(this);

        //Migration
        oldUserId = getIasId();
        if (oldUserId != null) {
            updateOldAppBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Props props = new Props();
                    props.put(ConstantAnalytics.IAS_USER_ID, oldUserId);
                    LiveNationAnalytics.track(AnalyticConstants.UPDATED, AnalyticsCategory.HOUSEKEEPING, props);
                    LocalBroadcastManager.getInstance(LiveNationApplication.this).unregisterReceiver(updateOldAppBroadcastReceiver);
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(updateOldAppBroadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.MIGRATION_UPDATE_INTENT_FILTER));
        }

        //ssoProvider token - server update
        LocalBroadcastManager.getInstance(this).registerReceiver(updateSsoAccessTokenReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.SSO_ACCESS_TOKEN_UPDATE_INTENT_FILTER));


        //Start Platform Library
        LiveNationLibrary.start(this, environmentProvider, new DeviceIdAppProvider(this), locationProvider, getAccessTokenProvider(), oldUserId);
        LiveNationLibrary.setSsoProvider(getSsoManager());
        LiveNationLibrary.setErrorTracker(new LibraryErrorTracker());

        //Initialize the UI library
        Crashlytics.start(this);
        LiveNationAnalytics.initializeAnalyticTools(new GoogleAnalytics(this), new AmplitudeAnalytics(this));
        LiveNationAnalytics.initializeCrashTools(new CrashlyticsAnalytics());

        //App init
        providerManager.getConfigReadyFor(ProviderManager.ProviderType.APP_INIT);

        venueEventsPresenter = new VenueEventsPresenter();
        inboxStatusPresenter = new InboxStatusPresenter();

        configFileProvider = new ConfigFileProvider(this, getRequestQueue());

        installedAppConfig = new ConfigFilePersistenceHelper(this, configFileProvider);
        if (installedAppConfig.isUpdateAdvisable())
            installedAppConfig.update();

        setupNotifications();
        setupTicketing();
        setupInternetStateReceiver();


        //Analytics
        Props props = new Props();
        props.put(AnalyticConstants.FB_LOGGED_IN, LoginHelper.isUsingFacebook());
        props.put(AnalyticConstants.GOOGLE_LOGGED_IN, LoginHelper.isUsingGoogle());
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
                            LivenationUILibrary.getInstance().setIsMusicSync(true);
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
        return LivenationUILibrary.getInstance().getRequestQueue();
    }

    public ImageLoader getImageLoader() {
        return LivenationUILibrary.getInstance().getImageLoader();
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

    public static ConfigFileProvider getConfigFileProvider() {
        return configFileProvider;
    }

    private String getIasId() {
        SharedPreferences prefs = getSharedPreferences(ConstantSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(ConstantSharedPreferences.INSTALLATION_ID, null);
    }

}