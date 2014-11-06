package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.ui.fragments.CitySearchFragment;
import com.livenation.mobile.android.na.ui.fragments.SearchFragment;
import com.livenation.mobile.android.na.ui.views.DecoratedEditText;

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
