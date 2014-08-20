package com.livenation.mobile.android.na.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.adobe.mobile.Config;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.segment.android.Analytics;
import com.segment.android.models.Props;

import java.util.List;

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
        Props properties = getAnalyticsProps();
        if (properties == null) {
            properties = new Props();
        }
        final Props finalProps = properties;
        LiveNationLibrary.getLocationProvider().getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                finalProps.put("Location", response[0] + "," + response[1]);
                String name = screenName;
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                LiveNationAnalytics.screen(name, finalProps);
            }

            @Override
            public void onErrorResponse() {
                String name = screenName;
                if (name == null) {
                    name = getClass().getSimpleName();
                }
                LiveNationAnalytics.screen(name, finalProps);
            }
        });

    }

    protected String getScreenName() {
        return this.getClass().getSimpleName();
    }

    protected Props getAnalyticsProps() {
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
}
