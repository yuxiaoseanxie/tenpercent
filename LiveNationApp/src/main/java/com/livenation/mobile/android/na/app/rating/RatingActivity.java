package com.livenation.mobile.android.na.app.rating;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.preferences.RatingPreferences;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.na.utils.ContactUtils;

/**
 * Created by elodieferrais on 8/15/14.
 */
public class RatingActivity extends LiveNationFragmentActivity {
    RatingPreferences prefs;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.prefs = new RatingPreferences(this);
        showRateDialog();

    }

    private void showRateDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.rating_dialog_title);


        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);

        Button rateButton = (Button) layout.findViewById(R.id.rate);
        rateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rateApp();
                prefs.setHasBeenClicked(true);
                finish();
            }
        });

        Button sendUsFeedBack = (Button) layout.findViewById(R.id.rateLater);
        sendUsFeedBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ContactUtils.buildAndOpenContactUsEmail(RatingActivity.this);
                prefs.setHasBeenClicked(true);
                finish();
            }
        });

        Button cancelButton = (Button) layout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.setHasBeenClicked(true);
                finish();
            }
        });

        dialog.setContentView(layout);
        dialog.show();
    }

    private void rateApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.rating_dialog_google_play_app_url), getPackageName()))));
    }
}
