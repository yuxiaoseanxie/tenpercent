package com.livenation.mobile.android.na.helpers;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by cchilton on 8/1/14.
 */
public class AnimationHelper {
    public static LayoutTransition getListAnimation() {
        LayoutTransition transition = new LayoutTransition();
        Animator appearingAnimation = ObjectAnimator.ofFloat(null, View.ALPHA, 0, 1f);
        appearingAnimation.setStartDelay(0);
        appearingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        transition.setAnimator(LayoutTransition.APPEARING, appearingAnimation);
        //transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        //transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);

        transition.disableTransitionType(LayoutTransition.CHANGING);
        return transition;
    }
}
