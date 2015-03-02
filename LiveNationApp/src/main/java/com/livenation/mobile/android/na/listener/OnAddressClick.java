package com.livenation.mobile.android.na.listener;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.utils.MapUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.content.Context;
import android.view.View;

/**
* Created by elodieferrais on 2/26/15.
*/
public class OnAddressClick implements View.OnClickListener {
    private Double lat;
    private Double lng;
    private Context context;
    private String address;
    private Venue venue;

    public OnAddressClick(Venue venue, Context context) {
        if (venue.getLng() == null || venue.getLng() == null) {
            return;
        }
        this.lat = Double.parseDouble(venue.getLat());
        this.lng = Double.parseDouble(venue.getLng());
        this.context = context;
        this.address = venue.getAddress().getSmallFriendlyAddress(false);
        this.venue = venue;
    }

    @Override
    public void onClick(View v) {
        if (lat == null) {
            return;
        }
        Props props = new Props();
        props.put(AnalyticConstants.VENUE_NAME, venue.getName());
        props.put(AnalyticConstants.VENUE_ID, venue.getId());
        LiveNationAnalytics.track(AnalyticConstants.VENUE_ADDRESS_TAP, AnalyticsCategory.VDP, props);

        MapUtils.redirectToMapApplication(lat, lng, address, context);
    }
}
