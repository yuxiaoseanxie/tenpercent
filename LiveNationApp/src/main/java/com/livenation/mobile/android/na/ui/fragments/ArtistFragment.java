package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleArtistView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.DetailShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

public class ArtistFragment extends LiveNationFragment implements SingleArtistView, ArtistEventsView {
    private final static String[] IMAGE_PREFERRED_ARTIST_KEYS = {"mobile_detail", "tap"};

    private NetworkImageView artistImageView;
    private TextView artistTitle;
    private TextView showsHeader;
    private ShowsListNonScrollingFragment shows;

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        this.artistImageView = (NetworkImageView)view.findViewById(R.id.fragment_show_image);
        this.artistTitle = (TextView)view.findViewById(R.id.fragment_show_artist_title);

        View showMoreView = inflater.inflate(R.layout.list_overflow_item, container, false);
        showMoreView.setOnClickListener(new ShowMoreOnClickListener());

        this.showsHeader = (TextView)view.findViewById(R.id.fragment_artist_shows_header);

        this.shows = ShowsListNonScrollingFragment.newInstance(DetailShowView.DisplayMode.ARTIST);
        shows.setMaxEvents(3);
        shows.setShowMoreItemsView(showMoreView);
        addFragment(R.id.fragment_artist_shows_container, shows, "shows");

        init();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        deinit();
    }

    //endregion


    //region Presenters

    @Override
    public void setSingleArtist(Artist artist) {
        artistTitle.setText(artist.getName());

        String imageKey = artist.getBestImageKey(IMAGE_PREFERRED_ARTIST_KEYS);
        if(imageKey != null) {
            String imageUrl = artist.getImageURL(imageKey);
            artistImageView.setImageUrl(imageUrl, getImageLoader());
        }
    }

    @Override
    public void setArtistEvents(ArtistEventsPresenter.ArtistEvents artistEvents) {
        if(artistEvents.getNearby().isEmpty()) {
            showsHeader.setText(R.string.artist_all_shows);
            shows.setEvents(artistEvents.getAll());
        } else {
            showsHeader.setText(R.string.artist_nearby_shows);
            shows.setAlwaysShowMoreItemsView(true);
            shows.setEvents(artistEvents.getNearby());
        }
    }

    private void init() {
        getSingleArtistPresenter().initialize(getActivity(), getActivity().getIntent().getExtras(), this);
        getArtistEventsPresenter().initialize(getActivity(), getActivity().getIntent().getExtras(), this);
    }

    private void deinit() {
        getSingleArtistPresenter().cancel(this);
        getArtistEventsPresenter().cancel(this);
    }

    private SingleArtistPresenter getSingleArtistPresenter() {
        return LiveNationApplication.get().getSingleArtistPresenter();
    }

    private ArtistEventsPresenter getArtistEventsPresenter() {
        return LiveNationApplication.get().getArtistEventsPresenter();
    }

    //endregion


    private class ShowMoreOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.i(getClass().getName(), "onClick");
        }
    }
}
