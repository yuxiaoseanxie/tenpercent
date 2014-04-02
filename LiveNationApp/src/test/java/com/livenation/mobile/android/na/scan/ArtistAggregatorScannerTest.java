package com.livenation.mobile.android.na.scan;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class ArtistAggregatorScannerTest extends ActivityInstrumentationTestCase2 {

    public ArtistAggregatorScannerTest() {
        super(TestActivity.class);
    }

    public void testAggregateWithoutSinceDate() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        ArtistAggregatorScanner artistAggregatorScanner = new ArtistAggregatorScanner();
        artistAggregatorScanner.aggregate(getActivity(), new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(MusicLibrary musicLibrary) {
                startApiCall.countDown();
            }

            @Override
            public void onError(int errorCode, String message) {
                startApiCall.countDown();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testAggregateWithSinceDate() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        ArtistAggregatorScanner artistAggregatorScanner = new ArtistAggregatorScanner();
        Calendar sinceDate = Calendar.getInstance();
        sinceDate.set(Calendar.YEAR, sinceDate.get(Calendar.YEAR) - 1);
        artistAggregatorScanner.aggregate(getActivity(), new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(MusicLibrary musicLibrary) {
                startApiCall.countDown();
            }

            @Override
            public void onError(int errorCode, String message) {
                startApiCall.countDown();
            }
        }, sinceDate.getTime());
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
