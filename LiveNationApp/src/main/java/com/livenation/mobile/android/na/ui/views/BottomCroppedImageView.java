package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by elodieferrais on 5/15/14.
 */
public class BottomCroppedImageView extends ImageView {

    public BottomCroppedImageView(Context context) {
        super(context);
    }

    public BottomCroppedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomCroppedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageResource(int resId) {
        if (resId >= 0) {
            Drawable drawable = getResources().getDrawable(resId);
            setCroppedImageDrawable(drawable);
        } else {
            setCroppedImageDrawable(null);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        setCroppedImageDrawable(drawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setCroppedImageDrawable(new BitmapDrawable(getResources(), bm));
    }


    protected void setCroppedImageDrawable(final Drawable drawable) {
        if (drawable == null) {
            super.setImageBitmap(null);
            return;
        }

        //oh no! the ui LayoutPass has not run yet. panic.
        if (getMeasuredWidth() == 0) {
            final View target = BottomCroppedImageView.this;

            target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //layout has changed (visibility, or other), but we still have not been measured yet, keep waiting.
                    if (getMeasuredWidth() == 0) return;
                    target.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    calculateCropMatrix(drawable.getIntrinsicWidth());

                    BottomCroppedImageView.super.setImageDrawable(drawable);
                }
            });
        } else {
            calculateCropMatrix(drawable.getIntrinsicWidth());
            super.setImageDrawable(drawable);
        }
    }

    private void calculateCropMatrix(int width) {
        setScaleType(ScaleType.MATRIX);
        Matrix matrix = getImageMatrix();

        int viewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        float scaleFactor = (float) viewWidth / (float) width;

        matrix.setScale(scaleFactor, scaleFactor, 0, 0);
        setImageMatrix(matrix);
    }
}


