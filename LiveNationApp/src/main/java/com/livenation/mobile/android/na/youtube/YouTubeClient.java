package com.livenation.mobile.android.na.youtube;

import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.VisibleForTesting;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.Iterator;
import java.util.List;

public class YouTubeClient {
    private RequestQueue requestQueue;
    private final String apiKey;

    public YouTubeClient(@NonNull String inApiKey) {
        if (inApiKey == null) {
            throw new NullPointerException("The Youtube Api jey cannot be null");
        }
        this.requestQueue = LiveNationApplication.get().getRequestQueue();
        apiKey = inApiKey;
    }

    @VisibleForTesting
    public void setRequestQueue(RequestQueue requestQueue) {
        if (requestQueue != null) {
            this.requestQueue = requestQueue;
        }
    }

    //region Searching

    public Cancelable search(String query,
                             int limit,
                             Response.Listener<List<YouTubeVideo>> success,
                             Response.ErrorListener failure) {

        YouTubeSearchRequest searchRequest = new YouTubeSearchRequest(apiKey, query, limit, success, failure);
        searchRequest.setFilterResults(true);
        requestQueue.add(searchRequest);
        return new RequestCancelable(searchRequest);
    }


    public void getArtistBlackList(final BasicApiCallback<List<String>> callback) {
        ConfigFileProvider provider = LiveNationApplication.getConfigFileProvider();
        provider.getConfigFile(new BasicApiCallback<ConfigFileProvider.ConfigFile>() {
            @Override
            public void onResponse(ConfigFileProvider.ConfigFile response) {
                callback.onResponse(response.youtubeBlackList);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
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
            if (!request.isCanceled())
                request.cancel();
        }
    }
}
