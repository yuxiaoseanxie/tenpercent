package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by elodieferrais on 5/15/14.
 */
public class BottomCroppedImageView extends NetworkImageView {
    public BottomCroppedImageView(Context context) {
        super(context);
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
    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            Matrix matrix = getImageMatrix();
            float scaleFactor = getWidth() / (float) bm.getWidth();
            matrix.setScale(scaleFactor, scaleFactor, 0, 0);
            setImageMatrix(matrix);
        }
        super.setImageBitmap(bm);
    }
}


