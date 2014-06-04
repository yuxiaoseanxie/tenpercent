package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.LoginHelper;

import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 5/22/14.
 */
public class OnBoardingActivity extends LiveNationFragmentActivity {

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
}

