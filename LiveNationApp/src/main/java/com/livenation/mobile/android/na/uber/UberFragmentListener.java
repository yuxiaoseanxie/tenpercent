package com.livenation.mobile.android.na.uber;

import com.livenation.mobile.android.na.ui.fragments.UberFragment;

import java.io.Serializable;

/**
* Created by elodieferrais on 3/4/15.
*/
public interface UberFragmentListener extends Serializable {
    public void onUberFragmentReady(UberFragment uberFragment);

    public void onUberFragmentNotAvailable(Throwable error);
}
