package com.livenation.mobile.android.na.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.adobe.mobile.Config;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.segment.android.Analytics;
import com.segment.android.models.Props;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by elodieferrais on 4/2/14.
 */
public abstract class LiveNationFragmentActivity extends FragmentActivity {

    protected void onCreate(Bundle savedInstanceState, int res) {
        super.onCreate(savedInstanceState);
        setContentView(res);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Segment.io
        Analytics.onCreate(this);

        if (!LiveNationApplication.get().isMusicSync()) {
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(this, new BasicApiCallback<Void>() {
                @Override
                public void onResponse(Void response) {
                    LiveNationApplication.get().setIsMusicSync(true);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            trackScreenWithLocation(getScreenName());
            if (getOmnitureScreenName() != null) {
                OmnitureTracker.trackState(getOmnitureScreenName(), getAnalyticsProps());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Segment.io
        Analytics.activityPause(this);
        //Omniture
        Config.pauseCollectingLifecycleData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Segment.io
        Analytics.activityResume(this);
        //Omniture
        Config.collectLifecycleData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }

    public void trackScreenWithLocation(final String screenName) {
        final Props properties = getPropsFromMapProperies(getAnalyticsProps());
        LiveNationLibrary.getLocationProvider().getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                properties.put("Location", response[0] + "," + response[1]);
                String name = screenName;
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                LiveNationAnalytics.screen(name, properties);
            }

            @Override
            public void onErrorResponse() {
                String name = screenName;
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                LiveNationAnalytics.screen(name, properties);
            }
        });

    }

    protected String getScreenName() {
        return this.getClass().getSimpleName();
    }

    protected String getOmnitureScreenName() {
        return null;
    }

    protected Map<String, Object> getAnalyticsProps() {
        return null;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

            if (taskList.size() > 0 && taskList.get(0).numActivities == 1 &&
                    taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {
                if (this.getClass().getName() != HomeActivity.class.getName()) {
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
            } else {
                onBackPressed();
                return true;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private Props getPropsFromMapProperies(Map<String, Object> properties) {
        Props props = new Props();
        if (properties == null) {
            return props;
        }
        Iterator<String> iterator = properties.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            props.put(key, properties.get(key));
        }
        return props;
    }

    protected void notifyGoogleViewStart(GoogleApiClient googleApiClient, Uri webUrl, Uri appUrl, String title) {
        // Call the App Indexing API view method
        AppIndex.AppIndexApi.view(googleApiClient, this,
                appUrl,
                title,
                webUrl, null);

    }

    protected void notifyGoogleViewEnd(GoogleApiClient googleApiClient, Uri appUrl) {
        if (appUrl != null) {
            AppIndex.AppIndexApi.viewEnd(googleApiClient, this,
                    appUrl);
        }
    }
}
