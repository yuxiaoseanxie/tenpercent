package com.livenation.mobile.android.na.uber;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.uber.activities.UberExampleActivity;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberActivityTest extends ActivityInstrumentationTestCase2<UberExampleActivity> {

    public UberActivityTest() {
        super(UberExampleActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testLoaded() {


    }
}
