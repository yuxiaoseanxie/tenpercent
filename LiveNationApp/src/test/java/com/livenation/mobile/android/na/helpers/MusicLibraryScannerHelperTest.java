package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

/**
 * Created by elodieferrais on 3/31/14.
 */
public class MusicLibraryScannerHelperTest extends ActivityInstrumentationTestCase2 {

    public MusicLibraryScannerHelperTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.SharedPreferences.MUSIC_SYNC_NAME, Context.MODE_PRIVATE).edit();
        editor.clear().commit();
    }

    public void testGetMusicDiffSinceLastSyncForTheFirstSync() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(getActivity(), new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                startApiCall.countDown();
            }

            @Override
            public void onFailure(int errorCode, String message) {
                startApiCall.countDown();
                fail();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testGetMusicDiffSinceLastSyncForTheSecondSync() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        Calendar sinceDate = Calendar.getInstance();
        sinceDate.set(Calendar.YEAR, sinceDate.get(Calendar.YEAR) - 1);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.SharedPreferences.MUSIC_SYNC_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(Constants.SharedPreferences.MUSIC_SYNC_LAST_SYNC_DATE_KEY, sinceDate.getTimeInMillis());
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(getActivity(), new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                startApiCall.countDown();
            }

            @Override
            public void onFailure(int errorCode, String message) {
                startApiCall.countDown();
                fail();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

    }
}
