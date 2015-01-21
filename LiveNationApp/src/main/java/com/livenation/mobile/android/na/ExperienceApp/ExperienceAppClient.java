package com.livenation.mobile.android.na.ExperienceApp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

/**
 * Created by cchilton on 8/19/14.
 */
public class ExperienceAppClient {
    private final RequestQueue requestQueue;

    public ExperienceAppClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void makeRequest(String eventId, ExperienceAppListener responseListener) {
        ExperienceAppRequest request = new ExperienceAppRequest(eventId, responseListener, responseListener);
        requestQueue.add(request);
    }

    public static abstract class ExperienceAppListener implements Response.Listener<Boolean>, Response.ErrorListener {
    }

    ;
}
