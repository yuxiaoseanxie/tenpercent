package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.livenation.mobile.android.na.app.Constants;

/**
 * Created by elodieferrais on 8/14/14.
 */
public class RatingPreferences {
    SharedPreferences sharedPreferences;


    public RatingPreferences(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(Constants.SharedPreferences.ENVIRONMENT, Context.MODE_PRIVATE);
    }

    public boolean hasBeenClicked() {
        return sharedPreferences.getBoolean(Constants.SharedPreferences.RATING_DIALOG_CLICKED, false);
    }

    public void setHasBeenClicked(boolean hasBeenShown) {
        sharedPreferences.edit().putBoolean(Constants.SharedPreferences.RATING_DIALOG_CLICKED, hasBeenShown).apply();
    }


}
