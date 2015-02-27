package com.livenation.mobile.android.na.ui;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.fragments.ArtistFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleArtistParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;

public class ArtistActivity extends DetailBaseFragmentActivity {
    private ArtistFragment artistFragment;
    public static final String PARAMETER_ARTIST_ID = "artist_id";
    public static final String PARAMETER_ARTIST_CACHED = "artists_cached";
    private Uri appUrl;
    private GoogleApiClient googleApiClient;
    private Artist artist;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_artist);
        artistFragment = (ArtistFragment) getSupportFragmentManager().findFragmentById(R.id.activity_artist_fragment);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_ARTIST_CACHED)) {
            artist = (Artist) args.getSerializable(PARAMETER_ARTIST_CACHED);
            setArtist(artist);
        } else {
            Long artistId = null;
            SingleArtistParameters apiParams = new SingleArtistParameters();
            if (args.containsKey(PARAMETER_ARTIST_ID)) {
                String artistIdRaw = args.getString(PARAMETER_ARTIST_ID);
                artistId = DataModelHelper.getNumericEntityId(artistIdRaw);
            }

            if (artistId != null) {
                LiveNationApplication.getLiveNationProxy().getSingleArtist(artistId, new BasicApiCallback<Artist>() {
                    @Override
                    public void onResponse(Artist artist) {
                        setArtist(artist);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        //TODO display an error message
                    }
                });
            } else {
                finish();
                return;
            }

        }
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_ADP;
    }

    @Override
    protected Map<String, Object> getAnalyticsProps() {
        if (artistFragment != null) {
            Map<String, Object> props = new HashMap<String, Object>();
            if (args.containsKey(ArtistActivity.PARAMETER_ARTIST_ID)) {
                String artistIdRaw = args.getString(ArtistActivity.PARAMETER_ARTIST_ID);
                props.put(AnalyticConstants.ARTIST_ID, DataModelHelper.getNumericEntityId(artistIdRaw));
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
        return (artistFragment != null && artistFragment.getArtist() != null);
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

    private void setArtist(Artist artist) {
        artistFragment.setSingleArtist(artist);
        ArtistActivity.this.artist = artist;
        invalidateIsShareAvailable();
        googleViewStart(artist);
    }

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.ADP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_ADP;
    }

    public static Bundle getArguments(String artistIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_ARTIST_ID, artistIdRaw);
        return bundle;
    }

    public static Bundle getArguments(Artist artist) {
        if (artist == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_ARTIST_ID, artist.getId());
        bundle.putSerializable(PARAMETER_ARTIST_CACHED, artist);
        return bundle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (appUrl != null) {
            googleViewEnd();
        }
        googleApiClient.disconnect();
    }

    private void googleViewStart(Artist artist) {
        Uri webUrl = Uri.parse(getString(R.string.web_url_artist) + DataModelHelper.getNumericEntityId(artist.getId()));
        String suffixUrl;
        if (artist.getId().contains("art")) {
            suffixUrl = artist.getId();
        } else {
            suffixUrl = "art_" + artist.getId();
        }
        appUrl = Uri.parse(getString(R.string.app_url_artist) + suffixUrl);

        notifyGoogleViewStart(googleApiClient, webUrl, appUrl, artist.getName());

    }

    private void googleViewEnd() {
        notifyGoogleViewEnd(googleApiClient, appUrl);
        appUrl = null;
    }
}
