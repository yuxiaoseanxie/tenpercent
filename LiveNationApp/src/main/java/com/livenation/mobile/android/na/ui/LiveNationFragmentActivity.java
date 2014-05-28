package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/2/14.
 */
public abstract class LiveNationFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.onCreate(this);
        if (!LiveNationApplication.get().isMusicSync()) {
            LiveNationApplication.get().setIsMusicSync(true);
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(this);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            trackScreenWithLocation(getScreenName());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onPause() {
        Analytics.activityPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.activityResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }

    public void trackScreenWithLocation(final String screenName) {
        LiveNationApplication.get().getConfigManager().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                Props properties = getAnalyticsProps();
                if (properties == null) {
                    properties = new Props();
                }
                properties.put("Location", apiService.getApiConfig().getLat() + "," + apiService.getApiConfig().getLng());
                String name = screenName;
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                LiveNationAnalytics.screen(name, properties);
            }

            @Override
            public void onApiServiceNotAvailable() {

            }
        });
    }

    protected String getScreenName() {
        return this.getClass().getSimpleName();
     }

     protected Props getAnalyticsProps() {
        return null;
     }
}
