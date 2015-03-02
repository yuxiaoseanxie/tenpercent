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
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

public class ArtistActivity extends DetailBaseFragmentActivity {
    public static final String PARAMETER_ARTIST_ID = "artist_id";
    public static final String PARAMETER_ARTIST_CACHED = "artists_cached";
    private Uri appUrl;
    private GoogleApiClient googleApiClient;
    private Artist artist;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_artist);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();


        Long artistId = null;
        if (args != null && args.containsKey(PARAMETER_ARTIST_ID)) {
            String artistIdRaw = args.getString(PARAMETER_ARTIST_ID);
            artistId = DataModelHelper.getNumericEntityId(artistIdRaw);
        }

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_ARTIST_CACHED)) {
            artist = (Artist) args.getSerializable(PARAMETER_ARTIST_CACHED);
            setArtist(artist);
        } else if (artistId != null) {
            final View pb = findViewById(R.id.activity_artist_pb);
            pb.setVisibility(View.VISIBLE);


            if (artistId != null) {
                LiveNationApplication.getLiveNationProxy().getSingleArtist(artistId, new BasicApiCallback<Artist>() {
                    @Override
                    public void onResponse(Artist artist) {
                        pb.setVisibility(View.GONE);
                        setArtist(artist);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), R.string.internet_broken, Toast.LENGTH_SHORT).show();
                        finish();
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
        Map<String, Object> props = new HashMap<String, Object>();
        if (args.containsKey(ArtistActivity.PARAMETER_ARTIST_ID)) {
            String artistIdRaw = args.getString(ArtistActivity.PARAMETER_ARTIST_ID);
            props.put(AnalyticConstants.ARTIST_ID, DataModelHelper.getNumericEntityId(artistIdRaw));
        } else if (artist != null) {
            props.put(AnalyticConstants.ARTIST_ID, artist.getNumericId());
        }
        return props;
    }

    //endregion

    @Override
    protected void onShare() {
        Props props = new Props();
        props.put(AnalyticConstants.ARTIST_NAME, artist.getName());
        props.put(AnalyticConstants.ARTIST_ID, artist.getId());
        trackActionBarAction(AnalyticConstants.SHARE_ICON_TAP, props);
        super.onShare();
    }

    @Override
    protected void onSearch() {
        trackActionBarAction(AnalyticConstants.SEARCH_ICON_TAP, null);
        super.onSearch();
    }

    @Override
    protected boolean isShareAvailable() {
        return (artist != null);
    }

    @Override
    protected String getShareSubject() {
        return artist.getName();
    }

    @Override
    protected String getShareText() {
        String artistTemplate = getString(R.string.share_template_artist);
        return artistTemplate.replace("$ARTIST", artist.getName())
                .replace("$LINK", artist.getWebUrl());
    }

    private void setArtist(Artist artist) {
        addFragment(ArtistFragment.newInstance(artist), R.id.activity_artist_fragment_container);
        this.artist = artist;
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
