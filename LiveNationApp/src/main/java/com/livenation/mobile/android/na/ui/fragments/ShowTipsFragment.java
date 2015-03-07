package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.uber.UberFragmentListener;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTips;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class ShowTipsFragment extends LiveNationFragment {
    private static final String EVENT = "com.livenation.mobile.android.na.ui.fragments.ShowTipsFragment.EVENT";

    public static ShowTipsFragment newInstance(Event event) {
        ShowTipsFragment showTips = new ShowTipsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EVENT, event);
        showTips.setArguments(bundle);

        return showTips;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_showtips, container, false);
        final Event event = (Event) getArguments().getSerializable(EVENT);
        final ProgressBar pb = (ProgressBar) view.findViewById(R.id.lineup_pb);


        if (getChildFragmentManager().findFragmentByTag(LineUpTipsFragment.class.getSimpleName()) == null) {
            pb.setVisibility(View.VISIBLE);
            event.getTips(new BasicApiCallback<EventTips>() {
                @Override
                public void onResponse(EventTips response) {
                    pb.setVisibility(View.GONE);
                    addFragment(R.id.lineup_container, LineUpTipsFragment.newInstance(response), LineUpTipsFragment.class.getSimpleName());
                    addFragment(R.id.permissions_container, ShowPermissionsFragment.newInstance(response), ShowPermissionsFragment.class.getSimpleName());

                    if (getActivity() == null) {
                        return;
                    }
                    ViewGroup uberContainer = (ViewGroup) view.findViewById(R.id.uber_view_container);
                    final ViewGroup permissionContainer = (ViewGroup) view.findViewById(R.id.permissions_container);
                    LayoutTransition lt = new LayoutTransition();
                    ValueAnimator anim = ObjectAnimator.ofFloat(uberContainer, "translationY",-getResources().getDimension(R.dimen.list_item_height), 0);
                    anim.setDuration(500);
                    lt.setAnimator(LayoutTransition.APPEARING, anim);
                    uberContainer.setLayoutTransition(lt);
                    UberFragment fragment = UberFragment.newInstance(event.getVenue(), getActivity(), AnalyticsCategory.SDP);
                    fragment.fetchEstimation(new UberFragmentListener() {
                        @Override
                        public void onUberFragmentReady(UberFragment uberFragment) {
                            addFragment(R.id.uber_view_container, uberFragment, UberFragment.class.getSimpleName());
                            TranslateAnimation animation = new TranslateAnimation(0,0,-getResources().getDimension(R.dimen.list_item_height),0);
                            animation.setFillAfter(true);
                            animation.setDuration(500);
                            permissionContainer.setAnimation(animation);
                            animation.start();
                        }

                        @Override
                        public void onUberFragmentNotAvailable(Throwable error) {
                            //Do nothing
                        }
                    });

                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    pb.setVisibility(View.GONE);
                    //TODO display an error message?
                }
            });
        }

        return view;
    }
}
