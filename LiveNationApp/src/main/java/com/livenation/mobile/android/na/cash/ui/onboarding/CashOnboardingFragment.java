package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.support.v4.app.Fragment;

public abstract class CashOnboardingFragment extends Fragment {
    public CashOnboardingActivity getCashRequestDetailsActivity() {
        return (CashOnboardingActivity) getActivity();
    }

    public boolean isNextAvailable() {
        return true;
    }

    public abstract void next();
}
