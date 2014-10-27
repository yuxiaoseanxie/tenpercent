package com.livenation.mobile.android.na.providers;

import android.content.Context;
import android.os.SystemClock;
import android.text.format.Time;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import org.json.JSONObject;

/**
 * Created by elodieferrais on 10/27/14.
 */
public class ConfigFileProvider {
    private RequestQueue queue;
    private Context context;
    private int VALID_PERIOD = 60 * 30 * 1000;
    static private ConfigFile configFile;
    private long syncDate;


    public ConfigFileProvider(Context context) {
        this.context = context;
        if (context == null) {
            throw (new NullPointerException());
        }

        queue = Volley.newRequestQueue(context);
    }

    public void getConfigFile(final BasicApiCallback<ConfigFile> callback) {
        if (configFile == null && ((syncDate - System.currentTimeMillis()) > VALID_PERIOD)) {
            callback.onResponse(configFile);
            return;
        } else {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, context.getString(R.string.help_url_json), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    boolean isSdpOn = response.optBoolean("isSdpOn", true);
                    configFile = new ConfigFile(isSdpOn);
                    syncDate = System.currentTimeMillis();
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
        public boolean skipSDP;

        public ConfigFile(boolean skipSDP) {
            this.skipSDP = skipSDP;
        }
    }
}
