package com.livenation.mobile.android.na.youtube;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YouTubeVideo implements Serializable {
    private final String identifier;
    private final String published;
    private final String title;
    private final List<String> thumbnailURLs;

    public YouTubeVideo(JSONObject object) throws JSONException {
        this.identifier = object.getJSONObject("id").getString("videoId");

        JSONObject snippet = object.getJSONObject("snippet");
        this.published = snippet.optString("publishedAt");
        this.title = snippet.getString("title");
        this.thumbnailURLs = new ArrayList<String>();

        JSONObject thumbnails = snippet.getJSONObject("thumbnails");
        Iterator keys = thumbnails.keys();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            JSONObject thumbnail = thumbnails.getJSONObject(key);
            this.thumbnailURLs.add(thumbnail.getString("url"));
        }
    }

    public static List<YouTubeVideo> processJsonItems(JSONArray rawVideos) throws JSONException {
        List<YouTubeVideo> videos = new ArrayList<YouTubeVideo>();
        for (int i = 0; i < rawVideos.length(); i++) {
            JSONObject rawVideo = rawVideos.getJSONObject(i);
            videos.add(new YouTubeVideo(rawVideo));
        }

        return videos;
    }

    //region Getters

    public String getIdentifier() {
        return identifier;
    }

    public String getPublished() {
        return published;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getThumbnailURLs() {
        return thumbnailURLs;
    }

    public Uri getViewUri() {
        return Uri.parse("http://www.youtube.com/watch?v=" + Uri.encode(getIdentifier()));
    }

    //endregion
}
