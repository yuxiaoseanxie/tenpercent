/*
 * 
 * @author Charlie Chilton 2014/02/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsHelper;
import android.mobile.livenation.com.livenationui.receiver.LocationUpdateReceiver;

import android.mobile.livenation.com.livenationui.analytics.ConstantAnalytics;
import android.mobile.livenation.com.livenationui.fragment.base.LiveNationFragmentTab;
import android.mobile.livenation.com.livenationui.view.listener.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.pagination.NearbyVenuesScrollPager;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.adapters.EventVenueAdapter;
import android.mobile.livenation.com.livenationui.view.RefreshBar;

import android.mobile.livenation.com.livenationui.activity.tools.ActivityOpener;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class NearbyVenuesFragment extends LiveNationFragmentTab implements ListView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener, ConfigCallback, LocationUpdateReceiver.LocationUpdateListener, SwipeRefreshLayout.OnRefreshListener {
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);
    private EventVenueAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventVenueAdapter(getActivity());
        scrollPager = new NearbyVenuesScrollPager(adapter);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_nearby_venues);

        listView.setAdapter(adapter);

        listView.setDivider(null);
        listView.setAreHeadersSticky(false);

        listView.setOnItemClickListener(this);
        listView.setOnHeaderClickListener(this);


        Context context = LiveNationApplication.get().getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));


        RefreshBar refreshBar = (RefreshBar) view.findViewById(R.id.fragment_nearby_venues_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);

        LiveNationApplication.getProviderManager().getConfigReadyFor(this, ProviderManager.ProviderType.LOCATION);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Context context = LiveNationApplication.get().getApplicationContext();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver);
        scrollPager.resetDataAndClearView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        final Event event = (Event) parent.getItemAtPosition(position);
        if (event == null) {
            //user clicked the footer/loading view
            return;
        }

        ActivityOpener.openSDP(getActivity(), event);

        //Analytics
        final Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.NEARBY, props);


    }

    @Override
    public void onHeaderClick(StickyListHeadersListView stickyListHeadersListView, View view, int position, long id, boolean b) {
        Venue venue = adapter.getItem(position).getVenue();

        Intent intent = new Intent(getActivity(), VenueActivity.class);

        Bundle args = VenueActivity.getArguments(venue);

        //Analytics
        Props props = new Props();
        props.put(ConstantAnalytics.VENUE_NAME, venue.getName());
        props.put(ConstantAnalytics.VENUE_ID, venue.getId());
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, AnalyticsCategory.NEARBY, props);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    private void cleanAndRefresh() {
        scrollPager.resetDataAndClearView();
        scrollPager.load();
    }

    //Get config for starting the screen
    @Override
    public void onResponse(LiveNationConfig response) {
        scrollPager.load();
    }

    @Override
    public void onErrorResponse(int errorCode) {
        //TODO what we should do when we cannot get the user location
    }

    //Location update
    @Override
    public void onLocationUpdated(int mode, City city) {
        cleanAndRefresh();
    }

    @Override
    protected BaseDecoratedScrollPager getScrollPager() {
        return scrollPager;
    }
}
