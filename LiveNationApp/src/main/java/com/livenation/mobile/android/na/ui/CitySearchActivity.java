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

import java.lang.ref.WeakReference;

/**
 * Created by cchilton on 4/2/14.
 */
public class CitySearchActivity extends LiveNationFragmentActivity implements TextWatcher {
    private SearchForText fragment;
    private EditText input;
    private final LimiterHandler limiter = new LimiterHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_city_search);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.view_city_search_actionbar, null);
        getActionBar().setCustomView(view);

        DecoratedEditText editText = (DecoratedEditText) view.findViewById(R.id.view_search_actionbar_input);
        fragment = (SearchForText) getSupportFragmentManager().findFragmentByTag("search");
        input = editText.getEditText();

        limiter.setFragment(fragment);
        limiter.setInput(input);

        input.addTextChangedListener(this);
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
                fragment.searchFor(input.getText().toString());
            }
        }
    }
}
