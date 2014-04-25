/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;

import io.segment.android.models.Props;

public class AllShowsFragment extends LiveNationFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackScreenWithLocation("User views All Shows screen", new Props());

        Fragment featured = new FeaturedFragment();
        Fragment showList = new ShowsListFragment();

        addFragment(R.id.fragment_all_shows_container_featured, featured, "featured");
        addFragment(R.id.fragment_all_shows_container_list, showList, "show_list");

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_shows, container, false);
        return view;
    }


}
