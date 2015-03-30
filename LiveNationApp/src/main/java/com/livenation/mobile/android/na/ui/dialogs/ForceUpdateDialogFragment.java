package com.livenation.mobile.android.na.ui.dialogs;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ConfigFilePersistenceHelper;
import com.livenation.mobile.android.na.ui.SsoActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by elodieferrais on 3/30/15.
 */
public abstract class ForceUpdateDialogFragment extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(isDialogCancelable());
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performNeutralButtonAction();
            }
        });
        builder.setNeutralButton(R.string.force_update_button_play_store, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri playStore = Uri.parse(getInstalledAppConfig().getUpgradePlayStoreLink());
                startActivity(new Intent(Intent.ACTION_VIEW, playStore));
            }
        });
        return builder.create();
    }


    private ConfigFilePersistenceHelper getInstalledAppConfig() {
        return LiveNationApplication.get().getInstalledAppConfig();
    }

    abstract protected int getTitle();
    abstract protected int getMessage();
    abstract protected void performNeutralButtonAction();
    abstract protected boolean isDialogCancelable();
}
