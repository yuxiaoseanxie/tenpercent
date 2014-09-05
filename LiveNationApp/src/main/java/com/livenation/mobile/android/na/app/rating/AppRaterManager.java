package com.livenation.mobile.android.na.app.rating;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.preferences.RatingPreferences;
import com.livenation.mobile.android.na.utils.ContactUtils;

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
