package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by elodieferrais on 5/15/14.
 */
public class BottomCroppedImageView extends NetworkImageView {
    public BottomCroppedImageView(Context context) {
        super(context);
        init();
    }

    public BottomCroppedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomCroppedImageView(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageResource(int resId) {
        if (resId >= 0) {
            Drawable drawable = getResources().getDrawable(resId);
            applyPerfectWidthMatrix(drawable);
        }
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        applyPerfectWidthMatrix(drawable);
        super.setImageDrawable(drawable);
    }

    private void applyPerfectWidthMatrix(Drawable drawable) {
        if (drawable != null) {
            Matrix matrix = getImageMatrix();
            int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            float scaleFactor = (float) viewWidth / (float) drawable.getIntrinsicWidth();
            matrix.setScale(scaleFactor, scaleFactor, 0, 0);
            setImageMatrix(matrix);
        }
    }

}


