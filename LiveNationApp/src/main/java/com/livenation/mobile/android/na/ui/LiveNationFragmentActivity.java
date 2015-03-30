package com.livenation.mobile.android.na.ui;

import com.adobe.mobile.Config;
import com.apsalar.sdk.Apsalar;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.na.ui.dialogs.AppForceUpdateDialogFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

/**
 * Created by elodieferrais on 4/2/14.
 */
public abstract class LiveNationFragmentActivity extends FragmentActivity {
    private static boolean isApsalarStarted = false;
    private static boolean isUpdateRequirementChecked = false;
    protected static Class apsalarSessionActivity = null;

    protected void onCreate(Bundle savedInstanceState, int res) {
        LiveNationAnalytics.logTrace("Activity", "Create: " + getClass().getName() + " savedInstance: " + (savedInstanceState == null ? "null" : "not null"));
        super.onCreate(savedInstanceState);
        if (!isApsalarStarted) {
            //Initialize apsalar
            isApsalarStarted = true;
            apsalarSessionActivity = getClass();
            Apsalar.setFBAppId(getString(R.string.facebook_app_id));
            Apsalar.startSession(this, getString(R.string.apsalar_key), getString(R.string.apsalar_secret));

        }

        setContentView(res);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (!LiveNationApplication.get().isMusicSync()) {
            LiveNationApplication.get().setIsMusicSync(true);
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(this, new BasicApiCallback<Void>() {
                @Override
                public void onResponse(Void response) {
                    LiveNationApplication.get().setIsMusicSync(true);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    LiveNationApplication.get().setIsMusicSync(false);
                }
            });
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!isUpdateRequirementChecked) {
            ConfigFileProvider provider = LiveNationApplication.getConfigFileProvider();
            provider.getConfigFile(new BasicApiCallback<ConfigFileProvider.ConfigFile>() {
                @Override
                public void onResponse(ConfigFileProvider.ConfigFile response) {
                    if (response.upgradeMinimumRequired != null) {
                        PackageInfo pInfo = null;
                        try {
                            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            String version = pInfo.versionName;
                            if (version.compareTo(response.upgradeMinimumRequired) < 0) {
                                if (getSupportFragmentManager().findFragmentByTag(AppForceUpdateDialogFragment.TAG) == null && !isDestroyed() && !isFinishing()) {
                                    AppForceUpdateDialogFragment forceUpdateDialogFragment = new AppForceUpdateDialogFragment();
                                    forceUpdateDialogFragment.show(getSupportFragmentManager(), AppForceUpdateDialogFragment.TAG);
                                }
                            } else {
                                isUpdateRequirementChecked = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        LiveNationAnalytics.logTrace("Activity", "Post Create: " + getClass().getName());
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            trackScreenWithLocation(getScreenName());
            if (getOmnitureScreenName() != null) {
                Map<String, Object> omnitureProps = getAnalyticsProps();
                Map<String, Object> omnitureProductsProps = getOmnitureProductsProps();
                if (omnitureProps == null) {
                    omnitureProps = omnitureProductsProps;
                } else {

                    if (omnitureProductsProps != null) {
                        Iterator<String> iterator = omnitureProductsProps.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            omnitureProps.put(key, omnitureProductsProps.get(key));
                        }
                    }
                }
                OmnitureTracker.trackState(getOmnitureScreenName(), omnitureProps);
            }
        }
    }

    @Override
    protected void onStart() {
        LiveNationAnalytics.logTrace("Activity", "Start: " + getClass().getName());
        super.onStart();
    }

    @Override
    protected void onPause() {
        LiveNationAnalytics.logTrace("Activity", "Pause: " + getClass().getName());
        super.onPause();
        //Omniture
        Config.pauseCollectingLifecycleData();
    }

    @Override
    protected void onResume() {
        LiveNationAnalytics.logTrace("Activity", "Resume: " + getClass().getName());
        super.onResume();
        //Omniture
        Config.collectLifecycleData();
    }

    @Override
    protected void onStop() {
        LiveNationAnalytics.logTrace("Activity", "Stop: " + getClass().getName());
        super.onStop();
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


    protected Map<String, Object> getOmnitureProductsProps() {
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

    @Override
    protected void onDestroy() {

        if (this.getClass() == apsalarSessionActivity) {
            try {
                Apsalar.unregisterApsalarReceiver();
            } catch (Exception e) {
                LiveNationLibrary.getErrorTracker().track("Apsalar Error:" + e.toString(), null);
            }
            Apsalar.endSession();
            isApsalarStarted = false;
        }

        super.onDestroy();
    }
}
