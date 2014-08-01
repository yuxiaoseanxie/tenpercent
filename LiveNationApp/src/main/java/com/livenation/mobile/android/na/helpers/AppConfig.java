package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.livenation.mobile.android.na.BuildConfig;

import org.json.JSONObject;

public class AppConfig {
    public static final String ACTION_APP_CONFIG_UPDATED = "com.livenation.mobile.android.na.helpers.AppConfig.ACTION_APP_CONFIG_UPDATED";

    //region Internal Constants

    private static final String LAST_UPDATE_TIMESTAMP = "last_update_timestamp";

    private static final String MINIMUM_CHECKOUT_VERSION = "checkout_requires";
    private static final String UPGRADE_MAXIMUM_VERSION = "upgrade_maximum_version";
    private static final String UPGRADE_MESSAGE = "upgrade_message";
    private static final String UPGRADE_PLAY_STORE_LINK = "upgrade_play_store_link";

    private static final String FEATURED_CAROUSEL_NAME = "featured_carousel_chart";


    private static final String DEFAULT_FEATURED_CAROUSEL_NAME = "mobile-featured";
    private static final long MINIMUM_UPDATE_TIME_ELAPSED = 60 * 60 * 1000;

    //endregion


    private final Context applicationContext;
    private final RequestQueue requestQueue;
    private final SharedPreferences preferences;
    private final String url;
    private boolean isUpdating = false;


    public AppConfig(@NonNull Context context, @NonNull RequestQueue requestQueue) {
        this.applicationContext = context.getApplicationContext();
        this.requestQueue = requestQueue;
        this.preferences = applicationContext.getSharedPreferences(getClass().getName(), 0);

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

        JsonObjectRequest updateRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreferences.Editor editor = preferences.edit();

                String minimumCheckoutVersion = response.optString(MINIMUM_CHECKOUT_VERSION);
                editor.putString(MINIMUM_CHECKOUT_VERSION, minimumCheckoutVersion);

                String featuredCarouselName = response.optString(FEATURED_CAROUSEL_NAME);
                editor.putString(FEATURED_CAROUSEL_NAME, featuredCarouselName);

                String upgradeMaximumVersion = response.optString(UPGRADE_MAXIMUM_VERSION);
                editor.putString(UPGRADE_MAXIMUM_VERSION, upgradeMaximumVersion);

                String upgradeMessage = response.optString(UPGRADE_MESSAGE);
                editor.putString(UPGRADE_MESSAGE, upgradeMessage);

                String upgradePlayStoreLink = response.optString(UPGRADE_PLAY_STORE_LINK);
                editor.putString(UPGRADE_PLAY_STORE_LINK, upgradePlayStoreLink);

                editor.putLong(LAST_UPDATE_TIMESTAMP, System.currentTimeMillis());

                editor.apply();

                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(ACTION_APP_CONFIG_UPDATED));

                isUpdating = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isUpdating = false;

                Log.e(AppConfig.class.getSimpleName(), "Could not update app config.", error);
            }
        });
        requestQueue.add(updateRequest);
    }


    public long getLastUpdateTimestamp() {
        return preferences.getLong(LAST_UPDATE_TIMESTAMP, 0);
    }

    public @Nullable String getMinimumCheckoutVersion() {
        return preferences.getString(MINIMUM_CHECKOUT_VERSION, null);
    }

    public @Nullable String getFeaturedCarouselName() {
        return preferences.getString(FEATURED_CAROUSEL_NAME, DEFAULT_FEATURED_CAROUSEL_NAME);
    }

    public @Nullable String getUpgradeMaximumVersion() {
        return preferences.getString(UPGRADE_MAXIMUM_VERSION, null);
    }

    public @Nullable String getUpgradeMessage() {
        return preferences.getString(UPGRADE_MESSAGE, null);
    }

    public @Nullable String getUpgradePlayStoreLink() {
        return preferences.getString(UPGRADE_PLAY_STORE_LINK, null);
    }
}
