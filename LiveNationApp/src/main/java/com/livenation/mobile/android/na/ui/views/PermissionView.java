package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by elodieferrais on 3/2/15.
 */
public class PermissionView extends LinearLayout {
    private ImageView icon;
    private TextView title;

    public PermissionView(Context context) {
        super(context);
        init();
    }

    public PermissionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PermissionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_permission_cell, this);

        icon = (ImageView) findViewById(R.id.view_permission_icon);
        title = (TextView) findViewById(R.id.view_permission_title);
    }

    public ImageView getIcon() {
        return icon;
    }

    public TextView getTitle() {
        return title;
    }
}
