/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.ui.fragments.VenueFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class VenueActivity extends DetailBaseFragmentActivity {
    private static final int EVENTS_PER_VENUE_LIMIT = 30;

    public static final String PARAMETER_VENUE_ID = "venue_id";
    private static final String PARAMETER_VENUE_CACHED = "venue_cached";
    private Venue venue;
    private Uri appUrl;
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_venue);


        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();

        if (!getIntent().hasExtra(VenueEventsPresenter.PARAMETER_LIMIT)) {
            getIntent().putExtra(VenueEventsPresenter.PARAMETER_LIMIT, EVENTS_PER_VENUE_LIMIT);
        }

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_VENUE_CACHED)) {
            venue = (Venue) args.getSerializable(PARAMETER_VENUE_CACHED);
            setVenue(venue);
        } else {

            //Get venue detail
            final View pb = findViewById(R.id.activity_pb);
            pb.setVisibility(View.VISIBLE);
            String venueIdRaw = args.getString(PARAMETER_VENUE_ID);
            long venueId = DataModelHelper.getNumericEntityId(venueIdRaw);
            LiveNationApplication.getLiveNationProxy().getSingleVenue(venueId, new BasicApiCallback<Venue>() {
                @Override
                public void onResponse(Venue venue) {
                    pb.setVisibility(View.GONE);
                    setVenue(venue);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(VenueActivity.this, R.string.internet_broken, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

    }

    private void setVenue(Venue venue) {
        VenueActivity.this.venue = venue;
        getSupportFragmentManager().beginTransaction().add(R.id.activity_venue_container, VenueFragment.newInstance(venue)).commit();
        invalidateIsShareAvailable();
        googleViewStart(venue);
    }

    @Override
    protected void onShare() {
        Props props = new Props();
        if (this.venue != null) {
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
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
        return (venue != null);
    }

    @Override
    protected String getShareSubject() {
        return venue.getName();
    }

    @Override
    protected String getShareText() {
        String venueTemplate = getString(R.string.share_template_venue);
        return venueTemplate.replace("$VENUE", venue.getName())
                .replace("$LINK", venue.getWebUrl());
    }

    //endregion

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.VDP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_VDP;
    }

    @Override
    protected Map<String, Object> getAnalyticsProps() {
        Map<String, Object> props = new HashMap<String, Object>();
        if (args.containsKey(VenueActivity.PARAMETER_VENUE_ID)) {
            String venueIdRaw = args.getString(VenueActivity.PARAMETER_VENUE_ID);
            props.put(AnalyticConstants.VENUE_ID, DataModelHelper.getNumericEntityId(venueIdRaw));
        }
        return props;
    }


    public static Bundle getArguments(String venueIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(VenueActivity.PARAMETER_VENUE_ID, venueIdRaw);
        return bundle;
    }

    public static Bundle getArguments(Long venueIdRaw) {
        Bundle bundle = new Bundle();
        if (venueIdRaw != null) {
            bundle.putString(VenueActivity.PARAMETER_VENUE_ID, venueIdRaw.toString());
        }
        return bundle;
    }

    public static Bundle getArguments(Venue venue) {
        if (venue == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_VENUE_ID, venue.getId().toString());
        bundle.putSerializable(PARAMETER_VENUE_CACHED, venue);
        return bundle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_VDP;
    }


    protected void onStop() {
        super.onStop();
        if (appUrl != null) {
            googleViewEnd();
        }
        googleApiClient.disconnect();
    }

    private void googleViewStart(Venue venue) {
        Uri webUrl = Uri.parse(getString(R.string.web_url_venue) + DataModelHelper.getNumericEntityId(venue.getId()));
        String suffixUrl;
        if (venue.getId().contains("ven")) {
            suffixUrl = venue.getId();
        } else {
            suffixUrl = "ven_" + venue.getId();
        }
        appUrl = Uri.parse(getString(R.string.app_url_venue) + suffixUrl);

        notifyGoogleViewStart(googleApiClient, webUrl, appUrl, venue.getName());

    }

    private void googleViewEnd() {
        notifyGoogleViewEnd(googleApiClient, appUrl);
        appUrl = null;
    }
}
