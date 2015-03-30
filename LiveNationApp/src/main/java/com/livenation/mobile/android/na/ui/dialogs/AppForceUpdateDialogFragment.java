package com.livenation.mobile.android.na.ui.dialogs;

import com.livenation.mobile.android.na.R;

import android.content.DialogInterface;

/**
 * Created by elodieferrais on 3/30/15.
 */
public class AppForceUpdateDialogFragment extends ForceUpdateDialogFragment {
    public static final String TAG = AppForceUpdateDialogFragment.class.getSimpleName();

    @Override
    protected int getTitle() {
        return R.string.force_update_error_title;
    }

    @Override
    protected int getMessage() {
        return R.string.force_update_app_error_message;
    }

    @Override
    protected void performNeutralButtonAction() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    protected boolean isDialogCancelable() {
        return false;
    }
}
