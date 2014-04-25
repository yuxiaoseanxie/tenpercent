package com.livenation.mobile.android.na.youtube;

import android.net.Uri;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class YouTubeSearchRequest extends JsonRequest<List<YouTubeVideo>> {
    private static final String API_HOST = "www.googleapis.com";
    private static final String API_PATH = "/youtube/v3/search";

    private String query;
    private boolean filterResults;


    private static String generateUrl(String apiKey, String query, int limit) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("https")
               .encodedAuthority(API_HOST)
               .encodedPath(API_PATH);

        builder.appendQueryParameter("part", "id,snippet")
               .appendQueryParameter("key", apiKey)
               .appendQueryParameter("fields", "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
               .appendQueryParameter("order", "relevance")
               .appendQueryParameter("type", "video")
               .appendQueryParameter("videoCategoryId", "10");

        builder.appendQueryParameter("maxResults", Integer.toString(limit))
               .appendQueryParameter("q", query + " (artist)");

        return builder.build().toString();
    }

    public YouTubeSearchRequest(String apiKey,
                                String query,
                                int limit,
                                Response.Listener<List<YouTubeVideo>> success,
                                Response.ErrorListener failure) {
        super(Method.GET, generateUrl(apiKey, query, limit), null, success, failure);

        this.query = query;
    }


    //region Properties

    public boolean shouldFilterResults() {
        return filterResults;
    }

    public void setFilterResults(boolean filterResults) {
        this.filterResults = filterResults;
    }

    public String getQuery() {
        return query;
    }

    //endregion


    @Override
    protected Response<List<YouTubeVideo>> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject json = new JSONObject(responseString);
            if(json.has("error")) {
                long errorCode = json.getLong("code");
                return Response.error(new VolleyError("YouTube returned error code: " + errorCode));
            }

            JSONArray rawVideos = json.getJSONArray("items");
            List<YouTubeVideo> videos = YouTubeVideo.processJsonItems(rawVideos);
            if(shouldFilterResults()) {
                YouTubeClient.filterVideos(videos, getQuery());
            }
            return Response.success(videos, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException e) {
            return Response.error(new VolleyError("JSONException: " + e.getMessage(), e));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new VolleyError("UnsupportedEncodingException: " + e.getMessage(), e));
        }
    }
}