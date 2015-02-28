package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class ShowVenueInfoFragment extends LiveNationFragment {
    private static final String VENUE = "com.livenation.mobile.android.na.ui.fragments.ShowVenueInfoFragment.VENUE";
    private Venue venue;

    public static ShowVenueInfoFragment newInstance(Venue venue) {
        ShowVenueInfoFragment showVenueInfoFragment = new ShowVenueInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENUE, venue);
        showVenueInfoFragment.setArguments(bundle);
        return showVenueInfoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_venue_info, container, false);
        venue = (Venue) getArguments().getSerializable(VENUE);

        addFragment(R.id.fragment_show_venue_map_container, VenueMapFragment.newInstance(venue, false, R.dimen.fragment_show_venue_map_height), VenueMapFragment.class.getSimpleName());
        addFragment(R.id.fragment_show_venue_detail_container, VenueDetailFragment.newInstance(venue, false), VenueDetailFragment.class.getSimpleName());

        return view;
    }
}
