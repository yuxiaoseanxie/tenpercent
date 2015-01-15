package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.na.presenters.views.SingleArtistView;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.ArtistShowsActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.na.ui.views.OverflowView;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.segment.android.models.Props;

import java.util.ArrayList;
import java.util.List;

public class ArtistFragment extends LiveNationFragment implements SingleArtistView, ArtistEventsView {
    private final static int BIO_TRUNCATION_LENGTH = 300;
    private final static String[] IMAGE_PREFERRED_ARTIST_KEYS = {"mobile_detail", "tap"};
    private final static int MAX_INLINE = 3;

    private Artist artist;
    private ArtistEvents artistEvents;

    private TransitioningImageView artistImageView;
    private FavoriteCheckBox favoriteCheckBox;
    private TextView artistTitle;
    private TextView showsHeader;
    private ShowsListNonScrollingFragment shows;

    private LinearLayout bioContainer;
    private TextView bioText;
    private OverflowView bioShowMore;

    private YouTubeFragment youTube;
    private OverflowView showMoreVideos;

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        this.artistImageView = (TransitioningImageView) view.findViewById(R.id.fragment_artist_image);
        this.favoriteCheckBox = (FavoriteCheckBox) view.findViewById(R.id.fragment_artist_favorite_checkbox);
        this.artistTitle = (TextView) view.findViewById(R.id.fragment_artist_title);

        this.showsHeader = (TextView) view.findViewById(R.id.fragment_artist_shows_header);


        this.bioContainer = (LinearLayout) view.findViewById(R.id.fragment_artist_bio_container);
        this.bioText = (TextView) bioContainer.findViewById(R.id.fragment_artist_bio);
        this.bioShowMore = (OverflowView) bioContainer.findViewById(R.id.fragment_artist_bio_overflow);
        bioShowMore.setTitle(R.string.artist_bio_overflow);
        bioShowMore.setOnClickListener(new ShowFullBioOnClickListener());
        suppressBio();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shows = (ShowsListNonScrollingFragment) getChildFragmentManager().findFragmentByTag("shows");
        if (shows == null) {
            this.shows = ShowsListNonScrollingFragment.newInstance(ShowView.DisplayMode.ARTIST, AnalyticsCategory.ADP);
            addFragment(R.id.fragment_artist_shows_container, shows, "shows");
        }
        shows.setMaxEvents(MAX_INLINE);
        shows.setDisplayMode(ShowView.DisplayMode.ARTIST);

        OverflowView showMoreView = new OverflowView(getActivity());
        showMoreView.setTitle(R.string.artist_events_overflow);
        showMoreView.setOnClickListener(new ShowAllEventsOnClickListener());
        shows.setShowMoreItemsView(showMoreView);

        youTube = (YouTubeFragment) getChildFragmentManager().findFragmentByTag("youtube");
        if (youTube == null) {
            this.youTube = new YouTubeFragment();
            addFragment(R.id.fragment_artist_youtube_container, youTube, "youtube");
        }
        youTube.setMaxVideos(MAX_INLINE);
        youTube.setShowMoreItemsView(showMoreVideos);

        this.showMoreVideos = new OverflowView(getActivity());
        if (youTube.getMaxVideos() > MAX_INLINE) {
            showMoreVideos.setTitle(R.string.artist_videos_overflow_close);
            showMoreVideos.setExpanded(true);
        } else {
            showMoreVideos.setTitle(R.string.artist_videos_overflow);
        }
        showMoreVideos.setOnClickListener(new ShowAllVideosOnClickListener());

        init();
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
        if (artist == null || getActivity() == null)
            return;

        this.artist = artist;

        artistTitle.setText(artist.getName());

        String imageKey = artist.getBestImageKey(IMAGE_PREFERRED_ARTIST_KEYS);
        artistImageView.setDefaultImage(DefaultImageHelper.computeDefaultDpDrawableId(getActivity(), artist.getNumericId()));
        if (imageKey != null) {
            String imageUrl = artist.getImageURL(imageKey);
            artistImageView.setImageUrl(imageUrl, LiveNationApplication.get().getImageLoader(), TransitioningImageView.LoadAnimation.FADE_ZOOM);
        }

        String bio = artist.getBio();
        if (bio != null && !bio.isEmpty())
            showBio(bio, true);
        else
            suppressBio();

        favoriteCheckBox.bindToFavorite(Favorite.fromArtist(artist), AnalyticsCategory.ADP);

        youTube.setArtistName(artist.getName());
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
            shows.setAlwaysShowMoreItemsView(artistEvents.getNearby().size() < artistEvents.getAll().size());
            shows.setEvents(artistEvents.getNearby());
        }
    }

    private void init() {
        LiveNationApplication.getProviderManager().getConfigReadyFor(new ConfigCallback() {
            @Override
            public void onResponse(LiveNationConfig config) {
                if (getActivity() == null) return;
                final double lat = config.getLat();
                final double lng = config.getLng();

                EventParameters apiParams = new EventParameters();

                String artistIdRaw = getActivity().getIntent().getStringExtra(ArtistActivity.PARAMETER_ARTIST_ID);

                apiParams.setPage(0, 10);

                LiveNationApplication.getLiveNationProxy().getArtistEvents(DataModelHelper.getNumericEntityId(artistIdRaw), new BasicApiCallback<List<Event>>() {
                    @Override
                    public void onResponse(List<Event> response) {
                        if (getActivity() == null) return;
                        ArtistEvents result = ArtistEvents.from((ArrayList<Event>) response, lat, lng);
                        setArtistEvents(result);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                    }
                }, apiParams);
            }

            @Override
            public void onErrorResponse(int errorCode) {
            }
        }, ProviderManager.ProviderType.LOCATION);
    }

    //endregion


    //region Getters

    public Artist getArtist() {
        return artist;
    }


    //endregion


    private class ShowAllEventsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (artist == null) {
                return;
            }
            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.ARTIST_NAME, artist.getName());
            props.put(AnalyticConstants.ARTIST_ID, artist.getId());

            LiveNationAnalytics.track(AnalyticConstants.SEE_MORE_SHOWS_TAP, AnalyticsCategory.ADP, props);

            Intent intent = new Intent(getActivity(), ArtistShowsActivity.class);
            intent.putExtras(ArtistShowsActivity.getArguments(artist));
            startActivity(intent);
        }
    }

    private class ShowAllVideosOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.ARTIST_NAME, artist.getName());
            props.put(AnalyticConstants.ARTIST_ID, artist.getId());

            LiveNationAnalytics.track(AnalyticConstants.SEE_MORE_VIDEOS_TAP, AnalyticsCategory.ADP, props);

            if (showMoreVideos.isExpanded()) {
                youTube.setMaxVideos(MAX_INLINE);
                showMoreVideos.setTitle(R.string.artist_videos_overflow);
                showMoreVideos.setExpanded(false);
            } else {
                youTube.setMaxVideos(youTube.getVideoCount());
                showMoreVideos.setTitle(R.string.artist_videos_overflow_close);
                showMoreVideos.setExpanded(true);
            }
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
