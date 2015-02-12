package com.livenation.mobile.android.na.ui;

import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.livenation.mobile.android.na.R;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;


import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;


/**
 * Created by elodieferrais on 2/7/15.
 */
public class LocationFragmentTest extends ActivityInstrumentationTestCase2 {


    public LocationFragmentTest() {
        super(LocationActivity.class);
    }

    Activity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    public void testAllComponentsOnScreen() {

        Espresso.onView(withId(R.id.fragment_location_current_header)).check(matches(withText("Current Location")));
        Espresso.onView(withId(R.id.fragment_location_history_header)).check(matches(withText("Previous Locations")));
        Espresso.onView(withId(R.id.fragment_location_current_location)).check(matches(withText("Use Current Location")));

    }

}
