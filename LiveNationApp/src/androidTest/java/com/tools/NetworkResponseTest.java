package com.tools;

import android.content.Context;

import com.android.volley.NetworkResponse;

/**
 * Created by elodieferrais on 1/27/15.
 */
public class NetworkResponseTest {
    public static NetworkResponse getConfigFileResponseSample(Context context) {
        NetworkResponse networkResponse = new NetworkResponse(SampleJsonReader.loadJSONFromAsset(context, "config-file-response.json").getBytes());
        return networkResponse;
    }
}
