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
import com.livenation.mobile.android.na.ui.fragments.SearchFragment;
import com.livenation.mobile.android.na.ui.views.DecoratedEditText;

import java.lang.ref.WeakReference;

/**
 * Created by elodieferrais on 11/5/14.
 */
public abstract class SearchActivity extends LiveNationFragmentActivity implements TextWatcher {
    private SearchFragment fragment;
    private EditText input;
    private LimiterHandler limiter = new LimiterHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_search);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);

        View view = getLayoutInflater().inflate(R.layout.view_search_actionbar, null);
        getActionBar().setCustomView(view);

        fragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag("search");

        if (fragment == null) {
            fragment = getFragmentInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_search_container, fragment, "search").commit();
        }

        DecoratedEditText editText = (DecoratedEditText) view.findViewById(R.id.view_search_actionbar_input);

        input = editText.getEditText();
        limiter.setFragment(fragment);
        limiter.setInput(input);
        input.addTextChangedListener(this);

        editText.setHint(getSearchMode().hintId);

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

    abstract protected SEARCH_MODE getSearchMode();

    abstract protected SearchFragment getFragmentInstance();

    public enum SEARCH_MODE {
        ARTISTS(R.string.search_input_hint_artists),
        ARTISTS_VENUES(R.string.search_input_hint_artists_venues),
        ARTISTS_VENUES_SHOWS(R.string.search_input_hint_events_artists_venues),
        CITY(R.string.city_search_input_hint);

        protected int hintId;

        SEARCH_MODE(int hintId) {
            this.hintId = hintId;
        }

    }

    private static class LimiterHandler extends Handler {
        private final WeakReference<LiveNationFragmentActivity> mActivity;
        private SearchForText fragment;
        private EditText input;

        public LimiterHandler(LiveNationFragmentActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        public void setFragment(SearchForText fragment) {
            this.fragment = fragment;
        }

        public void setInput(EditText input) {
            this.input = input;
        }

        @Override
        public void handleMessage(Message msg) {
            LiveNationFragmentActivity activity = mActivity.get();
            if (activity != null) {
                fragment.searchForText(input.getText().toString());
            }
        }
    }

}