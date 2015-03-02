package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/25/15.
 */
public class ComingShowFragment extends LiveNationFragment {
    private static final String EVENT = "com.livenation.mobile.android.na.ui.fragments.ComingShowFragment.EVENT";
    private static final String TAB_TAG_SHOWTIPS = "show_tips";
    private static final String TAB_TAG_VENUE = "venue";

    private Event event;

    public static ComingShowFragment newInstance(Event event) {
        ComingShowFragment comingShowFragment = new ComingShowFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EVENT, event);
        comingShowFragment.setArguments(bundle);
        return comingShowFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        event = (Event) getArguments().getSerializable(EVENT);

        View rootView = inflater.inflate(R.layout.fragment_coming_show, container, false);
        TabHost tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
        tabHost.setup();

        String title;
        View view;
        TabHost.TabSpec tabSpec;

        title = getString(R.string.show_tips_title);
        view = createTab(getActivity(), title);
        tabSpec = tabHost.newTabSpec(TAB_TAG_SHOWTIPS);
        tabSpec.setIndicator(view);
        tabSpec.setContent(R.id.fragment_show_tips_container);
        tabHost.addTab(tabSpec);

        title = getString(R.string.show_venue_info_title);
        view = createTab(getActivity(), title);
        tabSpec = tabHost.newTabSpec(TAB_TAG_VENUE);
        tabSpec.setIndicator(view);
        tabSpec.setContent(R.id.fragment_venue_container);

        tabHost.addTab(tabSpec);

        addFragment(R.id.fragment_show_tips_container, ShowTipsFragment.newInstance(event), ShowTipsFragment.class.getSimpleName());
        addFragment(R.id.fragment_venue_container, ShowVenueInfoFragment.newInstance(event.getVenue()), ShowVenueInfoFragment.class.getSimpleName());

        return rootView;
    }

    private View createTab(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_tab, null);
        TextView text = (TextView) view.findViewWithTag("titleText");
        text.setText(title);
        return view;
    }
}
