package com.livenation.mobile.android.na.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.InstalledAppConfig;

public class CommerceUnavailableDialogFragment extends DialogFragment {
    public static final String TAG = CommerceUnavailableDialogFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
    }

    @Override
    public
    @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.commerce_unavailable_error_title);
        builder.setMessage(R.string.commerce_unavailable_error_message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(R.string.commerce_unavailable_error_button_play_store, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri playStore = Uri.parse(getInstalledAppConfig().getUpgradePlayStoreLink());
                startActivity(new Intent(Intent.ACTION_VIEW, playStore));
            }
        });
        return builder.create();
    }


    private InstalledAppConfig getInstalledAppConfig() {
        return LiveNationApplication.get().getInstalledAppConfig();
    }
}
