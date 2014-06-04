package com.livenation.mobile.android.na.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;

import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 5/22/14.
 */
public class OnBoardingActivity extends LiveNationFragmentActivity implements View.OnClickListener {

    private final static int FACEBOOK_LOGIN_REQUEST_CODE = 1010;
    private final static int GOOGLE_LOGIN_REQUEST_CODE = 1011;
    private Button facebookButton;
    private Button googleButton;
    private TextView skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        //On boarding never show because is not develop yet. Keep it for analytics
        if (isOnBoardingAlreadyDisplayed()) {
            //goToTheApp();
        } else {
            LiveNationAnalytics.track(AnalyticConstants.ON_BOARDING_FIRST_LAUNCH, AnalyticsCategory.ON_BOARDING);
            setOnBoardingAlreadyDisplayed();
            //goToTheApp();
        }

        facebookButton = (Button) findViewById(R.id.on_boarding_facebook_sign_in_button);
        googleButton = (Button) findViewById(R.id.on_boarding_google_sign_in_button);
        skip = (TextView) findViewById(R.id.on_boarding_skip_textview);

        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

    }

    public boolean isOnBoardingAlreadyDisplayed() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SharedPreferences.ON_BOARDING_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SharedPreferences.ON_BOARDING_HAS_BEEN_DISPLAYED, false);
    }

    public void setOnBoardingAlreadyDisplayed() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SharedPreferences.ON_BOARDING_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SharedPreferences.ON_BOARDING_HAS_BEEN_DISPLAYED, true).commit();
    }

    private void goToTheApp() {
        setOnBoardingAlreadyDisplayed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginWithFacebook() {
        LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT_TAP, AnalyticsCategory.ON_BOARDING);
        Intent intent = new Intent(this, SsoActivity.class);
        intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_FACEBOOK.name());
        startActivityForResult(intent, FACEBOOK_LOGIN_REQUEST_CODE);
    }

    private void loginWithGoogle() {
        LiveNationAnalytics.track(AnalyticConstants.GOOGLE_SIGN_IN_TAP, AnalyticsCategory.ON_BOARDING);
        Intent intent = new Intent(this, SsoActivity.class);
        intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_GOOGLE.name());
        startActivityForResult(intent, GOOGLE_LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == FACEBOOK_LOGIN_REQUEST_CODE || requestCode == GOOGLE_LOGIN_REQUEST_CODE)
                && resultCode == RESULT_OK) {
            goToTheApp();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.on_boarding_facebook_sign_in_button == v.getId()) {
            loginWithFacebook();
        } else if (R.id.on_boarding_google_sign_in_button == v.getId()) {
            loginWithGoogle();
        }
    }
}

