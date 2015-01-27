package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        public ConfigFile(JSONObject config) {

            //skipSDPFeature
            this.skipSDPFeature = config.optBoolean("skip_SDP_feature", false);

            //youtubeBlackList
            JSONObject youtubeConfig = config.optJSONObject("youtube");
            if (youtubeConfig != null) {
                JSONArray youtubeBlackJSONArray = youtubeConfig.optJSONArray("blacklisted_artist_ids");
                if (youtubeBlackJSONArray != null) {
                    this.youtubeBlackList = new ArrayList<>(youtubeBlackJSONArray.length());
                    for (int i = 0; i < youtubeBlackJSONArray.length(); i++) {
                        this.youtubeBlackList.add(youtubeBlackJSONArray.optString(i));
                    }
                }
            }
        }
    }
}
