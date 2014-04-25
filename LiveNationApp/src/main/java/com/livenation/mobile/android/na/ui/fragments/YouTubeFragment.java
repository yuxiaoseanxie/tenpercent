package com.livenation.mobile.android.na.ui.fragments;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
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
import com.livenation.mobile.android.na.ui.views.YouTubeVideoView;
import com.livenation.mobile.android.na.youtube.YouTubeClient;
import com.livenation.mobile.android.na.youtube.YouTubeVideo;

import java.util.List;

public class YouTubeFragment extends LiveNationFragment implements Response.Listener<List<YouTubeVideo>>, Response.ErrorListener {
    private YouTubeClient.Cancelable currentSearchRequest;
    private String artistName;
    private int maxVideos;
    private View showMoreItemsView;

    private List<YouTubeVideo> videos;
    private ViewGroup videoContainer;
    private EmptyListViewControl empty;

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);

        videoContainer = (ViewGroup) view;
        empty = (EmptyListViewControl) videoContainer.findViewById(R.id.fragment_youtube_empty);
        empty.setRetryOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load();
            }
        });
        if(getArtistName() == null)
            empty.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(videos != null) {
            displayVideos(videos);
        }
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
        if(this.artistName != null && this.artistName.equals(artistName))
            return;

        this.artistName = artistName;

        load();
    }

    public int getMaxVideos() {
        return maxVideos;
    }

    public void setMaxVideos(int maxVideos) {
        this.maxVideos = maxVideos;
        if (videos != null)
            displayVideos(videos);
    }

    public View getShowMoreItemsView() {
        return showMoreItemsView;
    }

    public void setShowMoreItemsView(View showMoreItemsView) {
        this.showMoreItemsView = showMoreItemsView;
    }

    public int getVideoCount() {
        if (videos != null)
            return videos.size();
        else
            return 0;
    }

    //endregion


    //region Loading

    private void displayVideos(List<YouTubeVideo> videos) {
        if (videos.isEmpty()) {
            empty.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
            return;
        }

        empty.setVisibility(View.GONE);
        while (videoContainer.getChildCount() > 1) {
            videoContainer.removeViewAt(videoContainer.getChildCount() - 1);
        }

        int position = 0;
        for (YouTubeVideo video : videos) {
            YouTubeVideoView view = new YouTubeVideoView(getActivity());
            view.displayVideo(video);
            view.setOnClickListener(new VideoOnClickListener(video));

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            videoContainer.addView(view, layoutParams);

            position++;

            if(position >= getMaxVideos())
                break;
        }

        if (getShowMoreItemsView() != null && position >= getMaxVideos() && position < videos.size()) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            videoContainer.addView(getShowMoreItemsView(), layoutParams);
        }
    }

    private void load() {
        if(currentSearchRequest != null) {
            return;
        }

        empty.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        empty.setVisibility(View.VISIBLE);

        YouTubeClient.search(getArtistName(), 30, this, this);
    }


    @Override
    public void onResponse(List<YouTubeVideo> response) {
        currentSearchRequest = null;

        this.videos = response;
        displayVideos(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        currentSearchRequest = null;

        empty.setVisibility(View.VISIBLE);
        empty.setViewMode(EmptyListViewControl.ViewMode.RETRY);
        Log.e(getClass().getName(), "Could not load YouTube videos. " + error.getLocalizedMessage());
    }

    //endregion


    private class VideoOnClickListener implements View.OnClickListener {
        private YouTubeVideo video;

        public VideoOnClickListener(YouTubeVideo video) {
            this.video = video;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW, video.getViewUri());
            startActivity(intent);
        }
    }
}
