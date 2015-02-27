package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTips;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class ShowTipsFragment extends Fragment {
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
            View view = inflater.inflate(R.layout.fragment_showtips, container, false);
        Event event = (Event) getArguments().getSerializable(EVENT);
        final ProgressBar pb = (ProgressBar) view.findViewById(R.id.lineup_pb);
        pb.setVisibility(View.VISIBLE);
        event.getTips(new BasicApiCallback<EventTips>() {
            @Override
            public void onResponse(EventTips response) {
                pb.setVisibility(View.GONE);
                LineUpTipsFragment lineUpFragment = LineUpTipsFragment.newInstance(response);
                getChildFragmentManager().beginTransaction().add(R.id.lineup_container, lineUpFragment).commit();
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                pb.setVisibility(View.GONE);
                //TODO display an error message
            }
        });

        return view;
    }
}
