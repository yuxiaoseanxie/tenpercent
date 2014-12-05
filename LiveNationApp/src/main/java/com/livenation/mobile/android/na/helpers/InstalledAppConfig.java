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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InstalledAppConfig {
    public static final String ACTION_INSTALLED_APP_CONFIG_UPDATED = "com.livenation.mobile.android.na.helpers.InstalledAppConfig.ACTION_INSTALLED_APP_CONFIG_UPDATED";

    //region Internal Constants

    private static final String MINIMUM_CHECKOUT_VERSION = "checkout_requires";
    private static final String UPGRADE_MAXIMUM_VERSION = "upgrade_maximum_version";
    private static final String UPGRADE_MESSAGE = "upgrade_message";
    private static final String UPGRADE_PLAY_STORE_LINK = "upgrade_play_store_link";

    private static final String FEATURED_CAROUSEL_CHART = "featured_carousel_chart";

    private static final String UBER_FREE_RIDE_TEXT = "uber_free_ride_text";

    private static final String CONFIRMATION_ACTIONS = "confirmation_actions";


    private static final String DEFAULT_FEATURED_CAROUSEL_CHART = "mobile-featured";
    private static final String DEFAULT_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.livenation.mobile.android.na";
    private static final long MINIMUM_UPDATE_TIME_ELAPSED = 60 * 60 * 1000; // 60 minutes

    private final String DEFAULT_UBER_FREE_RIDE_TEXT;
    //endregion


    private final Context applicationContext;
    private final RequestQueue requestQueue;
    private final SharedPreferences preferences;
    private final String url;

    private boolean isUpdating = false;
    private long timeOfLastUpdate = 0;

    public InstalledAppConfig(@NonNull Context context, @NonNull RequestQueue requestQueue) {
        this.applicationContext = context.getApplicationContext();
        this.requestQueue = requestQueue;
        this.preferences = applicationContext.getSharedPreferences(getClass().getName(), 0);

        this.DEFAULT_UBER_FREE_RIDE_TEXT = context.getString(R.string.uber_free_ride_default);

        if (BuildConfig.DEBUG) {
            this.url = "http://adr-static-content.s3.amazonaws.com/config/debug.json";
        } else {
            this.url = "http://adr-static-content.s3.amazonaws.com/config/prod.json";
        }
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

        JsonObjectRequest updateRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreferences.Editor editor = preferences.edit();

                String minimumCheckoutVersion = response.optString(MINIMUM_CHECKOUT_VERSION);
                editor.putString(MINIMUM_CHECKOUT_VERSION, minimumCheckoutVersion);

                String featuredCarouselName = response.optString(FEATURED_CAROUSEL_CHART);
                editor.putString(FEATURED_CAROUSEL_CHART, featuredCarouselName);

                String upgradeMaximumVersion = response.optString(UPGRADE_MAXIMUM_VERSION);
                editor.putString(UPGRADE_MAXIMUM_VERSION, upgradeMaximumVersion);

                String upgradeMessage = response.optString(UPGRADE_MESSAGE);
                editor.putString(UPGRADE_MESSAGE, upgradeMessage);

                String upgradePlayStoreLink = response.optString(UPGRADE_PLAY_STORE_LINK);
                editor.putString(UPGRADE_PLAY_STORE_LINK, upgradePlayStoreLink);

                String uberFreeRide = response.optString(UBER_FREE_RIDE_TEXT);
                editor.putString(UBER_FREE_RIDE_TEXT, uberFreeRide);

                JSONArray confirmationActions = response.optJSONArray(CONFIRMATION_ACTIONS);
                String accumulator = "";
                for (int i = 0, size = confirmationActions.length(); i < size; i++) {
                    accumulator += confirmationActions.optString(i) + ",";
                }
                if (accumulator.length() > 0)
                    accumulator = accumulator.substring(0, accumulator.length() - 1);
                editor.putString(CONFIRMATION_ACTIONS, accumulator);

                timeOfLastUpdate = System.currentTimeMillis();

                editor.apply();

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(ACTION_INSTALLED_APP_CONFIG_UPDATED));

                Log.i(getClass().getSimpleName(), "Updated installed app config");
                isUpdating = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isUpdating = false;

                Log.e(InstalledAppConfig.class.getSimpleName(), "Could not update installed app config.", error);
            }
        });
        requestQueue.add(updateRequest);
    }


    public long getLastUpdateTimestamp() {
        return timeOfLastUpdate;
    }

    public @Nullable String getMinimumCheckoutVersion() {
        return preferences.getString(MINIMUM_CHECKOUT_VERSION, null);
    }

    public @NonNull String getFeaturedCarouselChartName() {
        return preferences.getString(FEATURED_CAROUSEL_CHART, DEFAULT_FEATURED_CAROUSEL_CHART);
    }

    public @Nullable String getUpgradeMaximumVersion() {
        return preferences.getString(UPGRADE_MAXIMUM_VERSION, null);
    }

    public @Nullable String getUpgradeMessage() {
        return preferences.getString(UPGRADE_MESSAGE, null);
    }

    public @NonNull List<String> getConfirmationActions() {
        String rawActions = preferences.getString(CONFIRMATION_ACTIONS, null);
        if (!TextUtils.isEmpty(rawActions)) {
            return Arrays.asList(TextUtils.split(rawActions, ","));
        }
        return Collections.emptyList();
    }

    public @NonNull String getUpgradePlayStoreLink() {
        return preferences.getString(UPGRADE_PLAY_STORE_LINK, DEFAULT_PLAY_STORE_LINK);
    }

    public @NonNull String getUberFreeRideText() {
        return preferences.getString(UBER_FREE_RIDE_TEXT, DEFAULT_UBER_FREE_RIDE_TEXT);
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
