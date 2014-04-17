package com.livenation.mobile.android.na.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YouTubeVideo {
    private final String identifier;
    private final String published;
    private final String title;
    private final List<String> thumbnailURLs;

    public YouTubeVideo(JSONObject object) throws JSONException {
        this.identifier = object.getJSONObject("id").getString("videoId");

        JSONObject snippet = object.getJSONObject("snippet");
        this.published = snippet.getString("publishedAt");
        this.title = snippet.getString("title");
        this.thumbnailURLs = new ArrayList<String>();

        JSONObject thumbnails = snippet.getJSONObject("thumbnails");
        Iterator keys = thumbnails.keys();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            JSONObject thumbnail = thumbnails.getJSONObject(key);
            this.thumbnailURLs.add(thumbnail.getString("thumbnail"));
        }
    }

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
}
