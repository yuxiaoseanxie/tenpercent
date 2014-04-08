package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.na.presenters.views.SingleArtistView;
import com.livenation.mobile.android.na.ui.ArtistEventsActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.DetailShowView;
import com.livenation.mobile.android.na.ui.views.OverflowView;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

public class ArtistFragment extends LiveNationFragment implements SingleArtistView, ArtistEventsView {
    private final static int BIO_TRUNCATION_LENGTH = 300;
    private final static String[] IMAGE_PREFERRED_ARTIST_KEYS = {"mobile_detail", "tap"};
    private final static int MAX_INLINE_EVENTS = 3;

    private Artist artist;
    private ArtistEvents artistEvents;

    private NetworkImageView artistImageView;
    private TextView artistTitle;
    private TextView showsHeader;
    private ShowsListNonScrollingFragment shows;

    private LinearLayout bioContainer;
    private TextView bioText;
    private OverflowView bioShowMore;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.shows = ShowsListNonScrollingFragment.newInstance(DetailShowView.DisplayMode.ARTIST);
        shows.setMaxEvents(MAX_INLINE_EVENTS);
        shows.setDisplayMode(DetailShowView.DisplayMode.ARTIST);
        addFragment(R.id.fragment_artist_shows_container, shows, "shows");

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        this.artistImageView = (NetworkImageView) view.findViewById(R.id.fragment_show_image);
        this.artistTitle = (TextView) view.findViewById(R.id.fragment_show_artist_title);

        this.showsHeader = (TextView) view.findViewById(R.id.fragment_artist_shows_header);

        OverflowView showMoreView = new OverflowView(getActivity());
        showMoreView.setTitle(R.string.artist_events_overflow);
        showMoreView.setOnClickListener(new ShowMoreOnClickListener());
        shows.setShowMoreItemsView(showMoreView);

        this.bioContainer = (LinearLayout) view.findViewById(R.id.fragment_artist_bio_container);
        this.bioText = (TextView) bioContainer.findViewById(R.id.fragment_artist_bio);
        this.bioShowMore = (OverflowView) bioContainer.findViewById(R.id.fragment_artist_bio_overflow);
        bioShowMore.setTitle(R.string.artist_bio_overflow);
        bioShowMore.setOnClickListener(new ShowFullBioOnClickListener());
        suppressBio();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        init();
    }

    @Override
    public void onStop() {
        super.onStop();

        deinit();
    }

    //endregion


    //region Bios

    private void showBio(String bio, boolean truncate) {
        if (truncate) {
            if (bio.length() <= BIO_TRUNCATION_LENGTH) {
                bioText.setText(bio);
                bioShowMore.setVisibility(View.GONE);
            } else {
                bioText.setText(bio.substring(0, BIO_TRUNCATION_LENGTH) + "…");
                bioShowMore.setVisibility(View.VISIBLE);
            }
        } else {
            bioText.setText(bio);
        }
        bioContainer.setVisibility(View.VISIBLE);
    }

    private void suppressBio() {
        bioContainer.setVisibility(View.GONE);
    }

    //endregion


    //region Presenters

    @Override
    public void setSingleArtist(Artist artist) {
        if (artist == null)
            return;

        this.artist = artist;

        artistTitle.setText(artist.getName());

        String imageKey = artist.getBestImageKey(IMAGE_PREFERRED_ARTIST_KEYS);
        if (imageKey != null) {
            String imageUrl = artist.getImageURL(imageKey);
            artistImageView.setImageUrl(imageUrl, getImageLoader());
        }

        String bio = artist.getBio();
        if (bio != null && !bio.isEmpty())
            showBio(bio, true);
        else
            suppressBio();
    }

    @Override
    public void setArtistEvents(ArtistEvents artistEvents) {
        if (artistEvents == null)
            return;

        this.artistEvents = artistEvents;

        if (artistEvents.getNearby().isEmpty()) {
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
            Intent intent = new Intent(getActivity(), ArtistEventsActivity.class);
            intent.putExtras(ArtistEventsActivity.getArguments(artist, artistEvents.getAll()));
            startActivity(intent);
        }
    }

    private class ShowFullBioOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (bioShowMore.isExpanded()) {
                showBio(artist.getBio(), true);
                bioShowMore.setTitle(R.string.artist_bio_overflow);
                bioShowMore.setExpanded(false);
            } else {
                showBio(artist.getBio(), false);
                bioShowMore.setTitle(R.string.artist_bio_overflow_close);
                bioShowMore.setExpanded(true);
            }
        }
    }
}
