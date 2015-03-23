package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.ui.fragments.CitySearchFragment;

import android.mobile.livenation.com.livenationui.activity.SearchActivity;
import android.mobile.livenation.com.livenationui.adapter.SearchFragment;

/**
 * Created by cchilton on 4/2/14.
 */
public class CitySearchActivity extends SearchActivity {

    @Override
    protected SEARCH_MODE getSearchMode() {
        return SEARCH_MODE.CITY;
    }

    @Override
    protected SearchFragment getFragmentInstance() {
        return new CitySearchFragment();
    }
}