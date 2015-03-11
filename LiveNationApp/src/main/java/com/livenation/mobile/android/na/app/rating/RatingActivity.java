package com.livenation.mobile.android.na.app.rating;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.os.Bundle;

/**
 * Created by elodieferrais on 8/15/14.
 */
public class RatingActivity extends LiveNationFragmentActivity {
    public static String TICKET_COUNT_KEY = "ticketcount";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            showRateDialog();
        }
    }


    private void showRateDialog() {
        final RatingDialogFragment dialog = new RatingDialogFragment();
        int ticketCount = 1;
        if (getIntent() != null) {
            ticketCount = getIntent().getIntExtra(TICKET_COUNT_KEY, 1);
        }
        dialog.setTicketCount(ticketCount);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "RatingFragmentDialog");
        LiveNationAnalytics.track(AnalyticConstants.RATE_US_SHOWN, AnalyticsCategory.RATEUSMODAL);
    }
}
