package com.livenation.mobile.android.na.cash.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;

public class CashErrorDialogFragment extends DialogFragment {
    public static final String TAG = CashErrorDialogFragment.class.getSimpleName();

    private static final String ARG_ERROR = "com.livenation.mobile.android.na.cash.ui.CashErrorDialogFragment.ARG_ERROR";

    public static CashErrorDialogFragment newInstance(VolleyError error) {
        CashErrorDialogFragment errorDialogFragment = new CashErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_ERROR, error);
        errorDialogFragment.setArguments(arguments);

        return errorDialogFragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.error_title_generic);
        builder.setMessage(getError().getLocalizedMessage());
        builder.setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }


    public VolleyError getError() {
        return (VolleyError) getArguments().getSerializable(ARG_ERROR);
    }
}
