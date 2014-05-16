package com.livenation.mobile.android.na.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
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
public class SearchActivity extends FragmentActivity implements TextWatcher {
    public static final String SEARCH_MODE = "search_mode";
    public static final int SEARCH_MODE_DEFAULT = 0;
    public static final int SEARCH_MODE_ARTIST_ONLY = 1;

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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.view_search_actionbar, null);
        getActionBar().setCustomView(view);

        DecoratedEditText editText = (DecoratedEditText) view.findViewById(R.id.view_search_actionbar_input);

        input = editText.getEditText();
        input.addTextChangedListener(this);
        fragment = (SearchForText) getSupportFragmentManager().findFragmentByTag("search");
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
}
