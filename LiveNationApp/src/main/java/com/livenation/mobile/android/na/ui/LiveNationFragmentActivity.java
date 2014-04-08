package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.helpers.MusicSyncHelper;

import io.segment.android.Analytics;

/**
 * Created by elodieferrais on 4/2/14.
 */
public class LiveNationFragmentActivity extends FragmentActivity {
    static boolean isMusicSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.onCreate(this);
        if (!isMusicSync) {
            isMusicSync = true;
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onPause() {
        Analytics.activityPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.activityResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }
}
