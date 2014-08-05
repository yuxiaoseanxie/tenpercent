package com.livenation.mobile.android.na.cash;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.livenation.mobile.android.na.R;

public class CashLoadingDialogFragment extends DialogFragment {
    public static final String TAG = CashLoadingDialogFragment.class.getSimpleName();

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.dialog_loading));
        return dialog;
    }
}
