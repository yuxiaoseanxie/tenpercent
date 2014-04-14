package com.livenation.mobile.android.na.notifications;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elodieferrais on 4/11/14.
 */
public class UrbanAirshipRequest extends JsonObjectRequest {
    final String password;
    final String userId;

    public UrbanAirshipRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String userId, String password) {
        super(method, url, jsonRequest, listener, errorListener);
        this.password = password;
        this.userId = userId;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return createBasicAuthHeader(userId, password);
    }

    Map<String, String> createBasicAuthHeader(String username, String password) {
        Map<String, String> headerMap = new HashMap<String, String>();

        String credentials = username + ":" + password;
        String base64EncodedCredentials =
                Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

        return headerMap;
    }
}
