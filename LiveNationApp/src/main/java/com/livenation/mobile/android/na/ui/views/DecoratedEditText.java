package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;

/**
 * Created by cchilton on 4/7/14.
 * <p/>
 * This class provides the business logic for handling an EditText view that is "decorated"
 * <p/>
 * "Decorated" = 1) has hint text with an image in it, 2) Has a clear text button
 * <p/>
 * This EditText's UI/UX is modelled after the search edittext found in Google Play Music app.
 */
public class DecoratedEditText extends LinearLayout implements TextWatcher {
    private EditText editText;
    private TextView hint;
    private ImageButton clear;

    public DecoratedEditText(Context context) {
        super(context);
        initializeView(context, null);
    }

    ;

    public DecoratedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);

    }

    public DecoratedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context, attrs);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s.toString())) {
            //no user text, show hint
            setViewState(ViewState.STATE_EMPTY);
        } else {
            //there is user text, hide hint and show clear button
            setViewState(ViewState.STATE_TEXT);
        }
    }

    public EditText getEditText() {
        return editText;
    }

    /**
     * Set the EditText's view state. Here we have two states, STATE_EMPTY for "no text entered",
     * and STATE_TEXT for "there is some text"
     * Depending on the state above, we need to selectively show or hide our decorating views, these
     * are the views that contain the hint text + image, and also the view which contains the
     * "clear text" button
     *
     * @param state The current view state that this decorated-edittext should display, ie:
     *              "has no text" or "has text"
     */
    private void setViewState(ViewState state) {
        switch (state) {
            case STATE_EMPTY:
                clear.setVisibility(View.GONE);
                hint.setVisibility(View.VISIBLE);
                break;
            case STATE_TEXT:
                clear.setVisibility(View.VISIBLE);
                hint.setVisibility(View.GONE);
                break;
            default:

        }
    }

    private void initializeView(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_decorated_edittext, this, false);

        editText = (EditText) view.findViewById(android.R.id.edit);
        clear = (ImageButton) view.findViewById(android.R.id.button1);
        hint = (TextView) view.findViewById(android.R.id.hint);

        if (null != attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DecoratedEditText, 0, 0);
            String hintText = a.getString(R.styleable.DecoratedEditText_hint);
            hint.setText(hintText);
            a.recycle();
        }

        editText.addTextChangedListener(this);
        if (TextUtils.isEmpty(editText.getText())) {
            setViewState(ViewState.STATE_EMPTY);
        } else {
            setViewState(ViewState.STATE_TEXT);
        }

        ViewGroup.LayoutParams layoutParams = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, layoutParams);

        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(null);
            }
        });
    }

    private enum ViewState {STATE_EMPTY, STATE_TEXT}
}
