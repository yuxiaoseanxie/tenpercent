package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class RefreshBar extends RelativeLayout {

    public RefreshBar(Context context) {
        super(context);
        init();
    }

    public RefreshBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.refresh_bar, this, true);
    }
}
