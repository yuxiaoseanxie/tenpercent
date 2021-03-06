package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.os.Bundle;

public class VenueShowsActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_VENUE_ID = "com.livenation.mobile.android.na.ui.VenueShowsActivity.EXTRA_VENUE_ID";
    public static final String EXTRA_VENUE_NAME = "com.livenation.mobile.android.na.ui.VenueShowsActivity.EXTRA_VENUE_NAME";

    //region Lifecycle

    public static Bundle getArguments(Venue venue) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_VENUE_ID, venue.getId());
        arguments.putSerializable(EXTRA_VENUE_NAME, venue.getName());
        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_venue_shows);

        String venueName = getIntent().getStringExtra(VenueShowsActivity.EXTRA_VENUE_NAME);
        getActionBar().setTitle(venueName);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_VDP_ALL_SHOWS;
    }

    //endregion


    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_VDP_ALL_SHOWS;
    }
}
