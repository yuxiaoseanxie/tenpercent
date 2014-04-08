package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;

public class OverflowView extends LinearLayout {
    private ImageView icon;
    private TextView title;

    private boolean expanded;

    public OverflowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public OverflowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OverflowView(Context context) {
        super(context);
        init(context);
    }

    //region Properties

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setTitle(int resid) {
        this.title.setText(resid);
    }

    public String getTitle() {
        return this.title.getText().toString();
    }


    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if(expanded)
            icon.setImageResource(R.drawable.overflow_minimize);
        else
            icon.setImageResource(R.drawable.overflow_maximize);
    }

    //endregion

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.view_overflow, isInEditMode()? null : this, false);

        this.icon = (ImageView)view.findViewById(R.id.view_overflow_icon);
        this.title = (TextView)view.findViewById(R.id.view_overflow_title);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setExpanded(false);
    }
}
