/*
 * 
 * @author Charlie Chilton 2014/02/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.pagination.NearbyVenuesScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.adapters.EventVenueAdapter;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class NearbyVenuesFragment extends LiveNationFragmentTab implements ListView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener, ApiServiceBinder, SwipeRefreshLayout.OnRefreshListener {

    private EmptyListViewControl emptyListViewControl;
    private EventVenueAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventVenueAdapter(getActivity());
        scrollPager = new NearbyVenuesScrollPager(adapter);
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.fragment_nearby_venues);

        listView.setAdapter(adapter);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        listView.setEmptyView(emptyListViewControl);
        scrollPager.setEmptyView(emptyListViewControl);

        listView.setDivider(null);
        listView.setAreHeadersSticky(false);

        listView.setOnItemClickListener(this);
        listView.setOnHeaderClickListener(this);

        RefreshBar refreshBar = (RefreshBar) view.findViewById(R.id.fragment_nearby_venues_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroyView();
        scrollPager.stop();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Event event = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), ShowActivity.class);

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.NEARBY, props);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView stickyListHeadersListView, View view, int position, long id, boolean b) {
        Venue venue = adapter.getItem(position).getVenue();

        Intent intent = new Intent(getActivity(), VenueActivity.class);

        Bundle args = SingleVenuePresenter.getAruguments(venue.getId());
        SingleVenuePresenter.embedResult(args, venue);

        //Analytics
        Props props = new Props();
        props.put(AnalyticConstants.VENUE_NAME, venue.getName());
        props.put(AnalyticConstants.VENUE_ID, venue.getId());
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, AnalyticsCategory.NEARBY, props);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        scrollPager.reset();
        scrollPager.load();
    }

    @Override
    public void onApiServiceNotAvailable() {
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
    }

    @Override
    BaseDecoratedScrollPager getScrollPager() {
        return scrollPager;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setSoundEffectsEnabled(true);
        scrollPager.reset();
        scrollPager.load();
    }
}
