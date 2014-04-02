package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setCustomView(R.layout.view_search_actionbar);
    }
}
