package com.livenation.mobile.android.na.providers;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * Created by elodieferrais on 10/27/14.
 */
public class ConfigFileProvider {
    private RequestQueue queue;
    private Context context;
    static private ConfigFile configFile;

    public ConfigFileProvider(Context context, RequestQueue queue) {
        this.context = context;
        if (context == null) {
            throw (new NullPointerException());
        }

        this.queue = queue;
    }

    public void getConfigFile(final BasicApiCallback<ConfigFile> callback) {
        if (configFile != null) {
            callback.onResponse(configFile);
            return;
        } else {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, context.getString(R.string.config_file_url), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    configFile = new ConfigFile(response);
                    callback.onResponse(configFile);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callback.onErrorResponse(new LiveNationError(error));
                }
            });
            queue.add(request);
        }
    }

    public class ConfigFile {
        public boolean skipSDPFeature;
        public List<String> youtubeBlackList;
        public String minimumCheckoutVersion;
        public String featuredCarouselName;
        public String upgradeMaximumVersion;
        public String upgradeMessage;
        public String upgradePlayStoreLink;
        public String uberFreeRide;
        public JSONArray confirmationActions;

        public ConfigFile(JSONObject config) {

            final String SKIP_SDP_FEATURE = "skip_SDP_feature";
            final String YOUTUBE = "youtube";
            final String BLACKLISTED_ARTIST_IDS = "blacklisted_artist_ids";
            final String MINIMUM_CHECKOUT_VERSION = "checkout_requires";
            final String UPGRADE_MAXIMUM_VERSION = "upgrade_maximum_version";
            final String UPGRADE_MESSAGE = "upgrade_message";
            final String UPGRADE_PLAY_STORE_LINK = "upgrade_play_store_link";
            final String FEATURED_CAROUSEL_CHART = "featured_carousel_chart";
            final String UBER_FREE_RIDE_TEXT = "uber_free_ride_text";
            final String CONFIRMATION_ACTIONS = "confirmation_actions";

            //skipSDPFeature
            this.skipSDPFeature = config.optBoolean(SKIP_SDP_FEATURE, false);

            //youtubeBlackList
            JSONObject youtubeConfig = config.optJSONObject(YOUTUBE);
            if (youtubeConfig != null) {
                JSONArray youtubeBlackJSONArray = youtubeConfig.optJSONArray(BLACKLISTED_ARTIST_IDS);
                if (youtubeBlackJSONArray != null) {
                    this.youtubeBlackList = new ArrayList<>(youtubeBlackJSONArray.length());
                    for (int i = 0; i < youtubeBlackJSONArray.length(); i++) {
                        this.youtubeBlackList.add(youtubeBlackJSONArray.optString(i));
                    }
                }
            }

            minimumCheckoutVersion = config.optString(MINIMUM_CHECKOUT_VERSION);
            featuredCarouselName = config.optString(FEATURED_CAROUSEL_CHART);
            upgradeMaximumVersion = config.optString(UPGRADE_MAXIMUM_VERSION);
            upgradeMessage = config.optString(UPGRADE_MESSAGE);
            upgradePlayStoreLink = config.optString(UPGRADE_PLAY_STORE_LINK);
            uberFreeRide = config.optString(UBER_FREE_RIDE_TEXT);
            confirmationActions = config.optJSONArray(CONFIRMATION_ACTIONS);

        }
    }
}
