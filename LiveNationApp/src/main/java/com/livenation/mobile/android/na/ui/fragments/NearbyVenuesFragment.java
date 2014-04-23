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
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.NearbyVenuesScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.adapters.EventVenueAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class NearbyVenuesFragment extends LiveNationFragment implements ListView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener, ApiServiceBinder {

    private StickyListHeadersListView listView;
    private EmptyListViewControl emptyListViewControl;
    private EventVenueAdapter adapter;
    private NearbyVenuesScrollPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackScreenWithLocation("User views Nearby screen", new Props());

        adapter = new EventVenueAdapter(getActivity());
        pager = new NearbyVenuesScrollPager(adapter);
        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_venues, container, false);
        listView = (StickyListHeadersListView) view.findViewById(R.id.fragment_nearby_venues_list);

        //Important: connect the listview (which set a footer) before to set the adapter
        pager.connectListView(listView);
        listView.setAdapter(adapter);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        listView.setEmptyView(emptyListViewControl);

        pager.setEmptyView(emptyListViewControl);

        listView.setDivider(null);
        listView.setAreHeadersSticky(false);

        listView.setOnItemClickListener(this);
        listView.setOnHeaderClickListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroyView();
        pager.stop();
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Parcelable listState = listView.getWrappedList().onSaveInstanceState();
        outState.putParcelable(getViewKey(listView), listState);
    }

    @Override
    public void applyInstanceState(Bundle state) {
        Parcelable listState = state.getParcelable(getViewKey(listView));
        if (null != listState) {
            listView.getWrappedList().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Event event = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), ShowActivity.class);

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put("Cell Position", position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TYPE);

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
        props.put("Venue Name", venue.getName());
        props.put("Cell Position", position);
        LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        pager.reset();
        pager.load();
    }

    @Override
    public void onApiServiceNotAvailable() {
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
    }
}
