package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by Charlie on 5/28/14.
 *
 *
 * This class is a workaround for the NetworkImageView being unsuitable for listviews.
 *
 * The NetworkImageView component from Volley only supports a "default" non-network image via the
 * setDefaultResId() method.
 *
 * Any other images set by setImageDrawable() get nullified/ignored by NetworkImageView's setDefaultImageOrNull() method
 *
 * Unfortunately, setDefaultResId() extracts images from resources, which introduces so much Jank to listscrolling during view recycling.
 *
 * This class provides a quick a dirty solution! It maintains its own Drawable cache for any single given resId.
 *
 */
public class CachedBottomCroppedImageView extends BottomCroppedImageView {
    private int cacheResourceId = -1;
    private Drawable cache;

    public CachedBottomCroppedImageView(Context context) {
        super(context);
    }

    public CachedBottomCroppedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CachedBottomCroppedImageView(Context context, AttributeSet attrs,
                                        int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageResource(int resId) {
        if (resId >= 0) {
            if (cacheResourceId != resId) {
                cache = getResources().getDrawable(resId);
                cacheResourceId = resId;
            }
            applyPerfectWidthMatrix(cache);
            //skip the expense extract from resources step
            setImageDrawable(cache);
            return;
        }
        super.setImageResource(resId);
    }

}


