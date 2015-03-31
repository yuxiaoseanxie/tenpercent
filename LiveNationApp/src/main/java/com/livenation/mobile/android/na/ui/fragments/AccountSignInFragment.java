package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import android.mobile.livenation.com.livenationui.activity.SsoActivity;
import com.livenation.mobile.android.platform.sso.SsoManager;

import android.content.Intent;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.ConstantAnalytics;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.sso.LoginHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccountSignInFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_account_sign_in, container, false);

        View button = result.findViewById(R.id.fragment_account_google_sign_in_button);
        button.setOnClickListener(new OnGoogleSignInClick());

        button = result.findViewById(R.id.fragment_account_facebook_sign_in_button);
        button.setOnClickListener(new OnFacebookSignInClick());

        return result;
    }

    private class OnFacebookSignInClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            LiveNationAnalytics.track(ConstantAnalytics.FACEBOOK_CONNECT_TAP, AnalyticsCategory.DRAWER);
            LoginHelper.login(getActivity(), SsoManager.SSO_TYPE.SSO_FACEBOOK);

        }

    }

    private class OnGoogleSignInClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            LiveNationAnalytics.track(AnalyticConstants.GOOGLE_SIGN_IN_TAP, AnalyticsCategory.DRAWER);
            LoginHelper.login(getActivity(), SsoManager.SSO_TYPE.SSO_GOOGLE);
        }

    }
}
