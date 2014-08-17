package com.livenation.mobile.android.na.app.rating;

import android.os.Bundle;

import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

/**
 * Created by elodieferrais on 8/15/14.
 */
public class RatingActivity extends LiveNationFragmentActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        showRateDialog();

    }

    private void showRateDialog() {
        final RatingDialogFragment dialog = new RatingDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "RatingFragmentDialog");
    }
}
