package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.ui.SsoActivity;

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
            LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT_TAP);
            Intent intent = new Intent(AccountSignInFragment.this.getActivity(), SsoActivity.class);
            intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_FACEBOOK.name());
            startActivity(intent);
        }

    }

    private class OnGoogleSignInClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AccountSignInFragment.this.getActivity(), SsoActivity.class);
            intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_GOOGLE.name());
            startActivity(intent);
        }

    }
}
