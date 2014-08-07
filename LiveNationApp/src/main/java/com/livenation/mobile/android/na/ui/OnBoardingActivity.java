package com.livenation.mobile.android.na.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ScrollView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.sso.SsoManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by elodieferrais on 5/22/14.
 */
public class OnBoardingActivity extends LiveNationFragmentActivity implements View.OnClickListener {

    private final static int FACEBOOK_LOGIN_REQUEST_CODE = 1010;
    private final static int GOOGLE_LOGIN_REQUEST_CODE = 1011;
    private final static int DELAY_ANIMATION = 1000;
    private final static int MUSIC_SCAN_DELAY_ANIMATION = 350;
    private final static int SCANNING_DURATION_ANIMATION = 350;
    private final static int LOGIN_DURATION_ANIMATION = 850;
    private final static int BACKGROUND_DURATION_ANIMATION = 600;
    private final static int TITLE_DURATION_ANIMATION = 450;
    private final static int SCROLLING_DURATION_ANIMATION = 450;
    private final static int ITEM_DURATION_ANIMATION = 520;
    private final static int SCANNING_DURATION = 2800;
    private View facebookButton;
    private View googleButton;
    private TextView skip;
    private View scanningView;
    private View backgroundView;
    private View titleView;
    private View ticketsView;
    private View recommandationsView;
    private View presalesView;
    private View loginContainerView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        //On boarding never show because is not develop yet. Keep it for analytics
        if (isOnBoardingAlreadyDisplayed()) {
            goToTheApp();
        } else {
            LiveNationAnalytics.track(AnalyticConstants.ON_BOARDING_FIRST_LAUNCH, AnalyticsCategory.ON_BOARDING);
        }

        facebookButton = findViewById(R.id.on_boarding_facebook_sign_in_button);
        googleButton = findViewById(R.id.on_boarding_google_sign_in_button);
        skip = (TextView) findViewById(R.id.on_boarding_skip_textview);
        scanningView = findViewById(R.id.on_boarding_scanning_view);
        backgroundView = findViewById(R.id.on_boarding_background);
        scanningView.setVisibility(View.INVISIBLE);
        titleView = findViewById(R.id.on_boarding_description_textview);
        loginContainerView = findViewById(R.id.on_boarding_login_container);
        recommandationsView = findViewById(R.id.on_boarding_recommendations_container);
        ticketsView = findViewById(R.id.on_boarding_tickets_container);
        presalesView = findViewById(R.id.on_boarding_presales_container);
        scrollView = (ScrollView) findViewById(R.id.on_boarding_scrollview);
        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        skip.setOnClickListener(this);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startAnimations();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, DELAY_ANIMATION);


    }

    private void setOnboardingAlreadyDisplay() {
        setOnBoardingAlreadyDisplayed();
    }

    private void startAnimations() {
        fadeInBackground();
    }

    private void showHideScanning() {
        final AlphaAnimation animDown = new AlphaAnimation(0, 1);
        animDown.setDuration(SCANNING_DURATION_ANIMATION);
        animDown.setFillAfter(true);
        animDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                scanningView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlphaAnimation animUp = new AlphaAnimation(1, 0);
                                animUp.setDuration(SCANNING_DURATION_ANIMATION);
                                animUp.setFillAfter(true);
                                scanningView.startAnimation(animUp);

                            }
                        });
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, SCANNING_DURATION);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        scanningView.startAnimation(animDown);
        int scrollviewHeight = scrollView.getChildAt(0).getHeight();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        Resources r = getResources();
        //25 is the height of the status bar
        int statusbarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, r.getDisplayMetrics());
        int paddingBottom = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollY", 0, scrollviewHeight - height + statusbarHeight - paddingBottom).setDuration(SCROLLING_DURATION_ANIMATION);
        objectAnimator.start();
    }

    public void fadeInBackground() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(BACKGROUND_DURATION_ANIMATION);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                backgroundView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeInTitle();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        backgroundView.startAnimation(alphaAnimation);
    }

    public void fadeInTitle() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(TITLE_DURATION_ANIMATION);
        animationSet.addAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(TITLE_DURATION_ANIMATION);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                titleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popItems();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        titleView.startAnimation(animationSet);
    }

    public void popItems() {
        popItem(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                popItem(new AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        popItem(new AnimationEndListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                showLoginView();
                            }
                        }, ticketsView);
                    }
                }, recommandationsView);
            }
        }, presalesView);
    }

    private void popItem(final AnimationEndListener listener, final View itemView) {
        AnimationSet animationSet = new AnimationSet(false);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(ITEM_DURATION_ANIMATION);
        scaleAnimation.setInterpolator(new OvershootInterpolator(1.5f));
        animationSet.addAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 100);
        alphaAnimation.setDuration(ITEM_DURATION_ANIMATION);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                itemView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        itemView.startAnimation(animationSet);
    }

    public void showLoginView() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(LOGIN_DURATION_ANIMATION);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                loginContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showScanningView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        loginContainerView.startAnimation(alphaAnimation);
    }

    private void showScanningView() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showHideScanning();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, MUSIC_SCAN_DELAY_ANIMATION);
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
        } else if (R.id.on_boarding_skip_textview == v.getId()) {
            LiveNationAnalytics.track(AnalyticConstants.SKIP_TAP, AnalyticsCategory.ON_BOARDING);
            goToTheApp();
        }
        setOnboardingAlreadyDisplay();
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_ONBOARDING;
    }

    private interface AnimationEndListener {
        public void onAnimationEnd(Animation animation);
    }
}

