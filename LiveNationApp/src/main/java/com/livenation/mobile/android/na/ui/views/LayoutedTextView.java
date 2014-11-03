package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by elodieferrais on 10/31/14.
 */
public class LayoutedTextView extends TextView {

    private OnLayoutListener onLayoutListener;

    public void setOnLayoutListener(OnLayoutListener listener) {
        onLayoutListener = listener;
    }

    public LayoutedTextView(Context context) {
        super(context);
    }

    public LayoutedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (onLayoutListener != null) {
            onLayoutListener.onLayouted(this);
        }
    }

    public interface OnLayoutListener {
        public void onLayouted(TextView view);
    }
}
