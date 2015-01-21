package com.livenation.mobile.android.na.app.rating;

import android.content.Context;
import android.content.Intent;

import com.livenation.mobile.android.na.preferences.RatingPreferences;

/**
 * Created by elodieferrais on 8/14/14.
 */
public class AppRaterManager {
    final RatingPreferences prefs;
    final Context context;

    public AppRaterManager(Context context) {
        this.prefs = new RatingPreferences(context);
        this.context = context;
    }

    public void purchaseCompleted(Context context, int ticketCount) {
        if (!prefs.hasBeenClicked()) {
            Intent ratingIntent = new Intent(context, RatingActivity.class);
            ratingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ratingIntent.putExtra(RatingActivity.TICKET_COUNT_KEY, ticketCount);
            context.startActivity(ratingIntent);
        }
    }
}
