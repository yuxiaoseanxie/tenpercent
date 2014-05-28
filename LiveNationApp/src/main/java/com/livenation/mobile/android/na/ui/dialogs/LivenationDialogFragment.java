package com.livenation.mobile.android.na.ui.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elodieferrais on 5/27/14.
 */
public class LiveNationDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(true);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
