package com.livenation.mobile.android.na.youtube;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.livenation.mobile.android.na.app.LiveNationApplication;

import java.util.Iterator;
import java.util.List;

public class YouTubeClient {
    private static RequestQueue requestQueue;
    private static String apiKey;

    public static void initialize(Context context, String inApiKey) {
        requestQueue = LiveNationApplication.get().getRequestQueue();
        apiKey = inApiKey;
    }


    //region Getters

    private static void checkInitialized() {
        if (requestQueue == null || apiKey == null)
            throw new IllegalStateException("Must call YouTubeClient.initialize");
    }

    public static RequestQueue getRequestQueue() {
        checkInitialized();
        return requestQueue;
    }

    public static String getApiKey() {
        checkInitialized();
        return apiKey;
    }

    //endregion


    //region Searching

    public static Cancelable search(String query,
                                    int limit,
                                    Response.Listener<List<YouTubeVideo>> success,
                                    Response.ErrorListener failure) {
        checkInitialized();

        YouTubeSearchRequest searchRequest = new YouTubeSearchRequest(getApiKey(), query, limit, success, failure);
        searchRequest.setFilterResults(true);
        getRequestQueue().add(searchRequest);
        return new RequestCancelable(searchRequest);
    }

    //endregion


    //region Utils

    private static boolean shouldFilterVideo(YouTubeVideo video, String query) {
        String title = video.getTitle();
        String queryWithoutSpaces = query.replace(" ", "");

        boolean match1 = !title.regionMatches(true, 0, query, 0, query.length());
        boolean match2 = !title.regionMatches(true, 0, queryWithoutSpaces, 0, queryWithoutSpaces.length());

        return (!(match1 || match2));
    }

    public static void filterVideos(List<YouTubeVideo> videos, String query) {
        Iterator<YouTubeVideo> videoIterator = videos.iterator();
        while (videoIterator.hasNext()) {
            YouTubeVideo video = videoIterator.next();
            if (shouldFilterVideo(video, query))
                videoIterator.remove();
        }
    }

    //endregion


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
