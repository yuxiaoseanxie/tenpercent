package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.youtube.YouTubeClient;
import com.livenation.mobile.android.na.youtube.YouTubeSearchRequest;
import com.livenation.mobile.android.na.youtube.YouTubeVideo;

import java.util.List;

public class YouTubeFragment extends LiveNationFragment implements Response.Listener<List<YouTubeVideo>>, Response.ErrorListener {
    private YouTubeClient.Cancelable currentSearchRequest;
    private String artistName;

    private ViewGroup videoContainer;
    private EmptyListViewControl empty;

    //region Lifecycle

    public static YouTubeFragment newInstance(String artistName) {
        YouTubeFragment fragment = new YouTubeFragment();
        fragment.setArtistName(artistName);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);

        videoContainer = (ViewGroup) view;
        empty = (EmptyListViewControl) videoContainer.findViewById(R.id.fragment_youtube_empty);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(currentSearchRequest != null)
            currentSearchRequest.cancel();
    }

    //endregion


    //region Properties

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    //endregion


    //region Loading

    private void load() {
        if(currentSearchRequest != null)
            return;

        YouTubeClient.search(getArtistName(), 30, this, this);
    }

    @Override
    public void onResponse(List<YouTubeVideo> response) {
        currentSearchRequest = null;

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        currentSearchRequest = null;
        Log.e(getClass().getName(), "Could not load YouTube videos. " + error.getLocalizedMessage());
    }

    //endregion
}
