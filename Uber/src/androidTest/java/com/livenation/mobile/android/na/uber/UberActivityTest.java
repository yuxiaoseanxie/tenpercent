package com.livenation.mobile.android.na.uber;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.uber.activities.UberTestActivity;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberActivityTest extends ActivityInstrumentationTestCase2<UberTestActivity> {

    public UberActivityTest() {
        super(UberTestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testLoaded() {


    }
}
