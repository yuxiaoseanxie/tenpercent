package com.livenation.mobile.android.na.listener;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.utils.ContactUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.app.Activity;
import android.view.View;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class OnPhoneNumberClick implements View.OnClickListener {
    private Activity activity;
    private Venue venue;

    public OnPhoneNumberClick(Activity activity, Venue venue) {
        this.activity = activity;
        this.venue = venue;
    }

    @Override
    public void onClick(View v) {
        Props props = new Props();
        props.put(AnalyticConstants.VENUE_NAME, venue.getName());
        props.put(AnalyticConstants.VENUE_ID, venue.getId());
        LiveNationAnalytics.track(AnalyticConstants.VENUE_PHONE_TAP, AnalyticsCategory.VDP, props);
        String phoneNumber = venue.getFormattedPhoneNumber();
        phoneNumber.replace("[^0-9+]", "");
        if (phoneNumber != null || !phoneNumber.trim().isEmpty())
            ContactUtils.dial(phoneNumber, activity);
    }
}
