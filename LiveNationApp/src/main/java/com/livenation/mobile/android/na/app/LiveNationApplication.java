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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.notifications.InboxStatusPresenter;
import com.livenation.mobile.android.na.notifications.PushReceiver;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;
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

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        int defaultCacheSize = MemoryImageCache.getDefaultLruSize();
        MemoryImageCache cache = new MemoryImageCache(defaultCacheSize);
        imageLoader = new ImageLoader(requestQueue, cache);

        setupNotifications();
        syncMusic();
    }


    private void setupNotifications() {
        Logger.logLevel = Log.VERBOSE;
        UAirship.takeOff(this);

        PushManager.enablePush();

        BasicPushNotificationBuilder notificationBuilder = new BasicPushNotificationBuilder();
        PushManager.shared().setNotificationBuilder(notificationBuilder);
        PushManager.shared().setIntentReceiver(PushReceiver.class);
    }

    private void syncMusic() {
        Toast.makeText(this, "Music Scan started", Toast.LENGTH_SHORT).show();
        final Toast successToast = Toast.makeText(LiveNationApplication.this, "Music Scan done! ", Toast.LENGTH_SHORT);
        final Toast failToast = Toast.makeText(LiveNationApplication.this, "Music Scan has failed! ", Toast.LENGTH_SHORT);
        MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
        musicSyncHelper.getMusicDiffSinceLastSync(this, new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                successToast.setText("Music Scan done! " +String.valueOf(result.getData().size())+ " artist has been synchronyzed");
                successToast.show();
            }

            @Override
            public void onFailure(int errorCode, String message) {
                failToast.show();
            }
        });
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

    public ApiHelper getApiHelper() {
        return apiHelper;
    }

}