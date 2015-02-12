package com.tools;

import com.android.volley.NetworkResponse;

import android.content.Context;

/**
 * Created by elodieferrais on 1/27/15.
 */
public class NetworkResponseTest {
    public static NetworkResponse getConfigFileResponseSample(Context context) {
        NetworkResponse networkResponse = new NetworkResponse(SampleJsonReader.loadJSONFromAsset(context, "config-file-response.json").getBytes());
        return networkResponse;
    }
}
