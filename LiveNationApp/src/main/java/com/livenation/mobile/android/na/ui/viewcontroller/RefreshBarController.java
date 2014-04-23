package com.livenation.mobile.android.na.ui.viewcontroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.view.View;
import android.view.ViewPropertyAnimator;

import com.livenation.mobile.android.na.R;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class RefreshBarController {
    private static final int HIDE_DELAY = 10000;
    private View refreshView;
    private ViewPropertyAnimator animator;
    private Handler handler = new Handler();

    private View.OnClickListener refreshListener;

    public RefreshBarController(View refreshView, final View.OnClickListener refreshListener) {
        this.refreshView = refreshView;
        this.animator = refreshView.animate();
        this.refreshListener = refreshListener;

        refreshView.findViewById(R.id.refreshbar_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideUndoBar(false);
                        refreshListener.onClick(view);
                    }
                });
        hideUndoBar(true);
    }

    public void showRefreshBar(boolean immediate) {

        handler.removeCallbacks(mHideRunnable);
        handler.postDelayed(mHideRunnable, HIDE_DELAY);

        refreshView.setVisibility(View.VISIBLE);
        if (immediate) {
            refreshView.setAlpha(1);
        } else {
            animator.cancel();
            animator
                    .alpha(1)
                    .setDuration(
                            refreshView.getResources()
                                    .getInteger(android.R.integer.config_shortAnimTime)
                    )
                    .setListener(null);
        }
    }

    private void hideUndoBar(boolean immediate) {
        handler.removeCallbacks(mHideRunnable);
        if (immediate) {
            refreshView.setVisibility(View.GONE);
            refreshView.setAlpha(0);

        } else {
            animator.cancel();
            animator
                    .alpha(0)
                    .setDuration(refreshView.getResources()
                            .getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            refreshView.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideUndoBar(false);
        }
    };
}
