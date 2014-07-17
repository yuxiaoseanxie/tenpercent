package com.livenation.mobile.android.na.ui.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cchilton on 7/16/14.
 */
public class AwesomeImageView extends FrameLayout {
    private BottomCroppedImageView networkImage;
    private ImageView defaultImage;
    private ImageLoader.ImageContainer imageContainer;
    private AnimatorSet animation;

    //cache for default images, avoid UI jank by loading these all the time
    private static SparseArray<Drawable> defaultCache;

    public static enum LoadAnimation {NONE, FADE, FADE_ZOOM}

    private static int NO_SIZE = 0;

    public AwesomeImageView(Context context) {
        super(context);
        init();
    }

    public AwesomeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AwesomeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        defaultCache = new SparseArray<Drawable>();

        networkImage = new BottomCroppedImageView(getContext());
        defaultImage = new BottomCroppedImageView(getContext());
        addView(defaultImage);
        addView(networkImage);
    }

    public void setDefaultImage(int resId) {
        Drawable cache = defaultCache.get(resId);
        if (cache == null) {
            cache = getResources().getDrawable(resId);
            defaultCache.put(resId, cache);
        }
        defaultImage.setImageDrawable(cache);
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setImageUrl(String url, ImageLoader imageLoader) {
        setImageUrl(url, imageLoader, LoadAnimation.NONE);
    }

    public void setImageUrl(String url, ImageLoader imageLoader, final LoadAnimation loadAnimation) {
        cancelAnimation();
        //cancel any pending network request from listview view recycling.
        //this actually saves *tons* of UI jank
        cancelPendingRequest();
        showDefaultImage();

        if (TextUtils.isEmpty(url)) {
            return;
        }

        imageContainer = imageLoader.get(url,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imageContainer = null;
                        showDefaultImage();
                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        imageContainer = null;
                        if (response.getBitmap() == null) {
                            return;
                        }

                        networkImage.setImageBitmap(response.getBitmap());
                        //isImmediate = image was returned from cache, not network. We dont want
                        //to animate this scenario
                        if (!isImmediate && isHardwareAccelerated() && loadAnimation != LoadAnimation.NONE) {
                            animation = AnimationFun.getAnimation(loadAnimation, networkImage);
                            animation.addListener(animationListener);
                            animation.start();
                        } else {
                            showNetworkImage();
                        }
                    }
                }, NO_SIZE, NO_SIZE
        );


    }

    private void showDefaultImage() {
        defaultImage.setVisibility(VISIBLE);
        networkImage.setVisibility(INVISIBLE);
    }

    private void showNetworkImage() {
        networkImage.setAlpha(1f);

        defaultImage.setVisibility(INVISIBLE);
        networkImage.setVisibility(VISIBLE);
    }

    private void cancelAnimation() {
        if (animation == null) return;
        animation.cancel();
        animation = null;
    }

    private void cancelPendingRequest() {
        if (imageContainer == null) return;
        imageContainer.cancelRequest();
        imageContainer = null;
    }

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            defaultImage.setVisibility(VISIBLE);
            networkImage.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            showNetworkImage();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    private static class AnimationFun {
        private static final int DURATION = 400;
        private static final float START_ZOOM = 1.3f;
        private static final float START_FADE = 0f;

        static AnimatorSet getAnimation(LoadAnimation animationType, View target) {
            AnimatorSet animation = new AnimatorSet();
            switch (animationType) {
                case FADE:
                    animation.playTogether(getFadeAnimation(target));
                    return animation;
                case FADE_ZOOM:
                    animation.playTogether(getFadeZoomAnimation(target));
                    return animation;
            }
            throw new IllegalArgumentException("Unknown animation: " + animationType.name());
        }

        private static List<Animator> getZoomAnimation(View target) {
            Animator zoomX = ObjectAnimator.ofFloat(target, SCALE_X,
                    START_ZOOM, 1f);

            Animator zoomY = ObjectAnimator.ofFloat(target, SCALE_Y,
                    START_ZOOM, 1f);

            zoomX.setDuration(DURATION);
            zoomY.setDuration(DURATION);
            return Arrays.asList(zoomX, zoomY);
        }


        private static List<Animator> getFadeAnimation(View target) {
            Animator fadeIn = ObjectAnimator.ofFloat(target, ALPHA,
                    START_FADE, 1f);
            fadeIn.setDuration(DURATION);

            return Arrays.asList(fadeIn);
        }

        private static List<Animator> getFadeZoomAnimation(View target) {
            List<Animator> result = new ArrayList<Animator>();
            result.addAll(getFadeAnimation(target));
            result.addAll(getZoomAnimation(target));
            return result;
        }
    }

}