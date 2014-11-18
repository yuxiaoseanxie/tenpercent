package com.livenation.mobile.android.na.uber;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.uber.activities.UberActivity;

import static org.hamcrest.Matchers.*;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.*;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.*;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.*;
/**
 * Created by cchilton on 11/17/14.
 */
public class UberActivityTest extends ActivityInstrumentationTestCase2<UberActivity> {

    public UberActivityTest() {
        super(UberActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testLoaded() {
        onView(withId(android.R.id.button1));
    }
}
