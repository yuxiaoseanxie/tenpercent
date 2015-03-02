package com.livenation.mobile.android.na.listener;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.ui.VenueBoxOfficeActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.content.Context;
import android.content.Intent;
import android.view.View;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class OnVenueDetailClick implements View.OnClickListener {
    private Context context;
    private Venue venue;

    public OnVenueDetailClick(Context context, Venue venue) {
        this.context = context;
        this.venue = venue;
    }

    @Override
    public void onClick(View v) {
        Props props = new Props();
        props.put(AnalyticConstants.VENUE_NAME, venue.getName());
        props.put(AnalyticConstants.VENUE_ID, venue.getId());
        LiveNationAnalytics.track(AnalyticConstants.MORE_VENUE_INFO_TAP, AnalyticsCategory.VDP, props);

        Intent intent = new Intent(context, VenueBoxOfficeActivity.class);
        intent.putExtras(VenueBoxOfficeActivity.getArguments(venue));
        context.startActivity(intent);
    }
}
