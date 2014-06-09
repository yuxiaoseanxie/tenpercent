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
import com.livenation.mobile.android.na.ui.views.DecoratedEditText;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchActivity extends LiveNationFragmentActivity implements TextWatcher {
    public static enum ExtraSearchMode {
        DEFAULT, ARTIST_ONLY;

        public static String getKey() {
            return ExtraSearchMode.class.getName();
        }
    }

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
            case ARTIST_ONLY:
                editText.setHint(R.string.search_input_hint_artists);
                break;
            case DEFAULT:
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

    public ExtraSearchMode getSearchMode() {
        if (getIntent() != null) {
            int searchModeOrdinal = getIntent().getIntExtra(SearchActivity.ExtraSearchMode.getKey(), SearchActivity.ExtraSearchMode.DEFAULT.ordinal());
            SearchActivity.ExtraSearchMode searchMode = SearchActivity.ExtraSearchMode.values()[searchModeOrdinal];
            return searchMode;
        }
        return ExtraSearchMode.DEFAULT;
    }
}
