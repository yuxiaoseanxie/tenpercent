package com.livenation.mobile.android.na.cash.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;

public class CashErrorDialogFragment extends DialogFragment {
    public static final String TAG = CashErrorDialogFragment.class.getSimpleName();

    private static final String ARG_ERROR = "com.livenation.mobile.android.na.cash.ui.CashErrorDialogFragment.ARG_ERROR";

    //region Lifecycle

    public static CashErrorDialogFragment newInstance(VolleyError error) {
        CashErrorDialogFragment errorDialogFragment = new CashErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_ERROR, error);
        errorDialogFragment.setArguments(arguments);

        Log.e(CashUtils.LOG_TAG, "An error occurred", error);

        return errorDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.error_title_generic);
        if (TextUtils.isEmpty(getError().getMessage()))
            builder.setMessage(getString(R.string.cash_generic_error, getError().networkResponse.statusCode));
        else
            builder.setMessage(getError().getMessage());
        builder.setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }

    //endregion


    public VolleyError getError() {
        return (VolleyError) getArguments().getSerializable(ARG_ERROR);
    }
}
