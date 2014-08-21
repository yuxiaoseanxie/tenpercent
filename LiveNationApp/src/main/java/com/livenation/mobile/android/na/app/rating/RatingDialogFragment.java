package com.livenation.mobile.android.na.app.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.preferences.RatingPreferences;
import com.livenation.mobile.android.na.ui.dialogs.LiveNationDialogFragment;
import com.livenation.mobile.android.na.utils.ContactUtils;

/**
 * Created by elodieferrais on 8/17/14.
 */
public class RatingDialogFragment extends LiveNationDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_rating, null);
        final RatingPreferences prefs = new RatingPreferences(getActivity());


        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.setCancelable(false);

        Button rateButton = (Button) view.findViewById(R.id.rating_dialog_rate);
        rateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rateApp();
                prefs.setHasBeenClicked(true);
                getActivity().finish();
            }
        });

        Button sendUsFeedBack = (Button) view.findViewById(R.id.rating_dialog_rateLater);
        sendUsFeedBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ContactUtils.buildAndOpenContactUsEmail(getActivity());
                prefs.setHasBeenClicked(true);
                getActivity().finish();
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.rating_dialog_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.setHasBeenClicked(true);
                getActivity().finish();
            }
        });

        return dialog;
    }

    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.rating_dialog_market_app_url), getActivity().getPackageName()))));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.rating_dialog_google_play_app_url), getActivity().getPackageName()))));
        }
    }
}
