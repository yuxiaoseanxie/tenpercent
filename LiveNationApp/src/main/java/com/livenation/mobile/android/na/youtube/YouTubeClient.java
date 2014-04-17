package com.livenation.mobile.android.na.youtube;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class YouTubeClient {
    private static RequestQueue requestQueue;
    private static String apiKey;

    public static void initialize(Context context, String inApiKey) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        apiKey = inApiKey;
    }

    public static RequestQueue getRequestQueue() {
        if(requestQueue == null)
            throw new IllegalStateException("Must call YouTubeClient.initialize");

        return requestQueue;
    }

    public static String getApiKey() {
        if(apiKey == null)
            throw new IllegalStateException("Must call YouTubeClient.initialize");

        return apiKey;
    }


    public static Cancelable search(String query,
                                    int limit,
                                    Response.Listener<List<YouTubeVideo>> success,
                                    Response.ErrorListener failure) {
        YouTubeSearchRequest searchRequest = new YouTubeSearchRequest(getApiKey(), query, limit, success, failure);
        getRequestQueue().add(searchRequest);
        return new RequestCancelable(searchRequest);
    }


    public interface Cancelable {
        public void cancel();
    }

    private static class RequestCancelable implements Cancelable {
        private YouTubeSearchRequest request;

        public RequestCancelable(YouTubeSearchRequest request) {
            this.request = request;
        }

        @Override
        public void cancel() {
            if(!request.isCanceled())
                request.cancel();
        }
    }
}
