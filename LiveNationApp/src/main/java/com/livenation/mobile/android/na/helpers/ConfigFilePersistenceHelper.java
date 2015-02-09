package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigFilePersistenceHelper {

    public static final String ACTION_INSTALLED_APP_CONFIG_UPDATED = "com.livenation.mobile.android.na.helpers.InstalledAppConfig.ACTION_INSTALLED_APP_CONFIG_UPDATED";

    private static final String DEFAULT_FEATURED_CAROUSEL_CHART = "mobile-featured";
    private static final String DEFAULT_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.livenation.mobile.android.na";
    private static final long MINIMUM_UPDATE_TIME_ELAPSED = 60 * 60 * 1000; // 60 minutes

    private final String DEFAULT_UBER_FREE_RIDE_TEXT;
    //endregion


    private final Context applicationContext;
    private final SharedPreferences preferences;
    private final ConfigFileProvider configFileProvider;

    private boolean isUpdating = false;
    private long timeOfLastUpdate = 0;

    public ConfigFilePersistenceHelper(@NonNull Context context, ConfigFileProvider configFileProvider) {
        this.applicationContext = context.getApplicationContext();
        this.preferences = applicationContext.getSharedPreferences(getClass().getName(), 0);
        this.configFileProvider = configFileProvider;
        this.DEFAULT_UBER_FREE_RIDE_TEXT = context.getString(R.string.uber_free_ride_default);
    }


    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    public boolean isUpdateAdvisable() {
        long timeSinceLastUpdate = System.currentTimeMillis() - getLastUpdateTimestamp();
        return (!isUpdating && isConnectedToInternet() && timeSinceLastUpdate > MINIMUM_UPDATE_TIME_ELAPSED);
    }

    public void update() {
        if (isUpdating || !isConnectedToInternet())
            return;

        isUpdating = true;
        Log.i(getClass().getSimpleName(), "Updating installed app config");

        configFileProvider.getConfigFile(new BasicApiCallback<ConfigFileProvider.ConfigFile>() {
            @Override
            public void onResponse(ConfigFileProvider.ConfigFile response) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(Constants.SharedPreferences.MINIMUM_CHECKOUT_VERSION, response.minimumCheckoutVersion);
                editor.putString(Constants.SharedPreferences.FEATURED_CAROUSEL_CHART, response.featuredCarouselName);
                editor.putString(Constants.SharedPreferences.UPGRADE_MAXIMUM_VERSION, response.upgradeMaximumVersion);
                editor.putString(Constants.SharedPreferences.UPGRADE_MESSAGE, response.upgradeMessage);
                editor.putString(Constants.SharedPreferences.UPGRADE_PLAY_STORE_LINK, response.upgradePlayStoreLink);
                editor.putString(Constants.SharedPreferences.UBER_FREE_RIDE_TEXT, response.uberFreeRide);

                JSONArray confirmationActions = response.confirmationActions;
                if (confirmationActions != null) {
                    String accumulator = "";
                    for (int i = 0, size = confirmationActions.length(); i < size; i++) {
                        accumulator += confirmationActions.optString(i) + ",";
                    }
                    if (accumulator.length() > 0)
                        accumulator = accumulator.substring(0, accumulator.length() - 1);
                    editor.putString(Constants.SharedPreferences.CONFIRMATION_ACTIONS, accumulator);

                }

                timeOfLastUpdate = System.currentTimeMillis();

                editor.apply();

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(ACTION_INSTALLED_APP_CONFIG_UPDATED));

                Log.i(getClass().getSimpleName(), "Updated installed app config");
                isUpdating = false;
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                isUpdating = false;
                Log.e(ConfigFilePersistenceHelper.class.getSimpleName(), "Could not update installed app config.", error);
            }
        });
    }


    public long getLastUpdateTimestamp() {
        return timeOfLastUpdate;
    }

    public
    @Nullable
    String getMinimumCheckoutVersion() {
        return preferences.getString(Constants.SharedPreferences.MINIMUM_CHECKOUT_VERSION, null);
    }

    public
    @NonNull
    String getFeaturedCarouselChartName() {
        return preferences.getString(Constants.SharedPreferences.FEATURED_CAROUSEL_CHART, DEFAULT_FEATURED_CAROUSEL_CHART);
    }

    public
    @Nullable
    String getUpgradeMaximumVersion() {
        return preferences.getString(Constants.SharedPreferences.UPGRADE_MAXIMUM_VERSION, null);
    }

    public
    @Nullable
    String getUpgradeMessage() {
        return preferences.getString(Constants.SharedPreferences.UPGRADE_MESSAGE, null);
    }

    public
    @NonNull
    List<String> getConfirmationActions() {
        String rawActions = preferences.getString(Constants.SharedPreferences.CONFIRMATION_ACTIONS, null);
        if (!TextUtils.isEmpty(rawActions)) {
            return Arrays.asList(TextUtils.split(rawActions, ","));
        }
        return Collections.emptyList();
    }

    public
    @NonNull
    String getUpgradePlayStoreLink() {
        return preferences.getString(Constants.SharedPreferences.UPGRADE_PLAY_STORE_LINK, DEFAULT_PLAY_STORE_LINK);
    }

    public
    @NonNull
    String getUberFreeRideText() {
        return preferences.getString(Constants.SharedPreferences.UBER_FREE_RIDE_TEXT, DEFAULT_UBER_FREE_RIDE_TEXT);
    }


    public boolean isCommerceAvailable() {
        String minimumCheckoutVersion = getMinimumCheckoutVersion();
        return (minimumCheckoutVersion == null || minimumCheckoutVersion.compareTo(BuildConfig.VERSION_NAME) < 1);
    }

    public boolean isUpgradeRequired() {
        String upgradeMaximumVersion = getUpgradeMaximumVersion();
        return (upgradeMaximumVersion != null && upgradeMaximumVersion.compareTo(BuildConfig.VERSION_NAME) >= 0);
    }
}
