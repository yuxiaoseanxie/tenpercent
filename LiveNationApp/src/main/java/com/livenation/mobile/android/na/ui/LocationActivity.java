package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationActivity extends TrackedFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        getActionBar().setTitle("Location");
    }
}
