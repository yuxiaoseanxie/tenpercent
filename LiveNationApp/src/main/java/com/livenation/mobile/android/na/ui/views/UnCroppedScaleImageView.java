package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by elodieferrais on 5/15/14.
 */
public class UnCroppedScaleImageView extends NetworkImageView {
    public UnCroppedScaleImageView(Context context) {
        super(context);
    }

    public UnCroppedScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnCroppedScaleImageView(Context context, AttributeSet attrs,
                                   int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            float ratio = (float) bm.getHeight() / bm.getWidth();
            super.setImageBitmap(Bitmap.createScaledBitmap(bm, viewWidth, (int) (ratio * viewWidth), false));
        }

    }
}
