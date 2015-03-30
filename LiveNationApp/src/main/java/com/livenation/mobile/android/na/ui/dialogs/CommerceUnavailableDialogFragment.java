package com.livenation.mobile.android.na.ui.dialogs;

import com.livenation.mobile.android.na.R;

public class CommerceUnavailableDialogFragment extends ForceUpdateDialogFragment {
    public static final String TAG = CommerceUnavailableDialogFragment.class.getSimpleName();

    @Override
    protected int getTitle() {
        return R.string.force_update_error_title;
    }

    @Override
    protected int getMessage() {
        return R.string.commerce_unavailable_error_message;
    }

    @Override
    protected void performNeutralButtonAction() {

    }

    @Override
    protected boolean isDialogCancelable() {
        return true;
    }
}
