package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.ui.fragments.ArtistFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

import io.segment.android.models.Props;

public class ArtistActivity extends DetailBaseFragmentActivity {
    private ArtistFragment artistFragment;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_artist);
        artistFragment = (ArtistFragment) getSupportFragmentManager().findFragmentById(R.id.activity_artist_fragment);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_ADP;
    }

    @Override
    protected Props getAnalyticsProps() {
        if (artistFragment != null) {
            Props props = new Props();
            if (args.containsKey(SingleArtistPresenter.PARAMETER_ARTIST_ID)) {
                String artistIdRaw = args.getString(SingleArtistPresenter.PARAMETER_ARTIST_ID);
                props.put(AnalyticConstants.ARTIST_ID, artistIdRaw);
            }
            return props;
        }
        return null;
    }

    //endregion

    @Override
    protected void onShare() {
        Props props = new Props();
        if (artistFragment != null) {
            props.put(AnalyticConstants.ARTIST_NAME, artistFragment.getArtist().getName());
            props.put(AnalyticConstants.ARTIST_ID, artistFragment.getArtist().getId());
        }
        trackActionBarAction(AnalyticConstants.SHARE_ICON_TAP, props);
        super.onShare();
    }

    @Override
    protected void onSearch() {
        trackActionBarAction(AnalyticConstants.SEARCH_ICON_TAP, null);
        super.onSearch();
    }

    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (artistFragment.getArtist() != null);
    }

    @Override
    protected String getShareSubject() {
        return artistFragment.getArtist().getName();
    }

    @Override
    protected String getShareText() {
        Artist artist = artistFragment.getArtist();
        String artistTemplate = getString(R.string.share_template_artist);
        return artistTemplate.replace("$ARTIST", artist.getName())
                .replace("$LINK", artist.getWebUrl());
    }

    //endregion

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.ADP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }
}
