package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.ui.views.DecoratedEditText;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchActivity extends LiveNationFragmentActivity implements TextWatcher {
    public static final String EXTRA_KEY_SEARCH_MODE = "com.livenation.mobile.android.na.ui.SearchActivity.EXTRA_KEY_SEARCH_MODE";
    public static final String EXTRA_KEY_ON_CLICK_ACTION = "com.livenation.mobile.android.na.ui.SearchActivity.EXTRA_KEY_ON_CLICK_ACTION";
    public static final int EXTRA_VALUE_SEARCH_MODE_DEFAULT = 0;
    public static final int EXTRA_VALUE_SEARCH_MODE_ARTIST = 1;
    public static final int EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES = 2;
    public static final int EXTRA_VALUE_ON_CLICK_ACTION_OPEN = 0;
    public static final int EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE = 1;

    private SearchForText fragment;
    private EditText input;

    private Handler limiter = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            fragment.searchFor(input.getText().toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_search);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.view_search_actionbar, null);
        getActionBar().setCustomView(view);

        DecoratedEditText editText = (DecoratedEditText) view.findViewById(R.id.view_search_actionbar_input);

        input = editText.getEditText();
        input.addTextChangedListener(this);
        fragment = (SearchForText) getSupportFragmentManager().findFragmentByTag("search");
        switch (getSearchMode()) {
            case EXTRA_VALUE_SEARCH_MODE_ARTIST:
                editText.setHint(R.string.search_input_hint_artists);
                break;
            case EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES:
                editText.setHint(R.string.search_input_hint_artists_venues);
                break;
            default:
                //leave with XML default
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void afterTextChanged(final Editable editable) {
        //Buffer user keypresses within Xmilliseconds so that we don't hit the API on every keystroke
        limiter.removeMessages(0);
        limiter.sendEmptyMessageDelayed(0, Constants.TEXT_CHANGED_POST_DELAY);
    }

    public int getSearchMode() {
        if (getIntent() != null) {
            return getIntent().getIntExtra(SearchActivity.EXTRA_KEY_SEARCH_MODE, SearchActivity.EXTRA_VALUE_SEARCH_MODE_DEFAULT);
        }
        return SearchActivity.EXTRA_VALUE_SEARCH_MODE_DEFAULT;
    }

    public int getOnClickActionMode() {
        if (getIntent() != null) {
            return getIntent().getIntExtra(SearchActivity.EXTRA_KEY_ON_CLICK_ACTION, SearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_OPEN);
        }
        return SearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_OPEN;
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_SEARCH;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            OmnitureTracker.trackAction(AnalyticConstants.OMNITURE_SCREEN_SEARCH, null);
        }
    }
}
