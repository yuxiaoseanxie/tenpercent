package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationActivity extends LiveNationFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        getActionBar().setTitle("Location");
    }
}
