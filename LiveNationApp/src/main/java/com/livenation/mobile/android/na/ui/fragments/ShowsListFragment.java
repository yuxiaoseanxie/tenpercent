/*
 * 
 * @author Charlie Chilton 2014/01/24
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
import android.widget.AdapterView.OnItemClickListener;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.AllShowsScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventStickyHeaderAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ShowsListFragment extends LiveNationFragment implements OnItemClickListener, ApiServiceBinder{
    private StickyListHeadersListView listView;
    private EventStickyHeaderAdapter adapter;
    private AllShowsScrollPager scrollPager;
    private EmptyListViewControl emptyListViewControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventStickyHeaderAdapter(getActivity(), ShowView.DisplayMode.EVENT);
        scrollPager = new AllShowsScrollPager(adapter);
        LiveNationApplication.get().getApiHelper().persistentBindApi(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shows_list, container, false);
        listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
        listView.setOnItemClickListener(ShowsListFragment.this);
        //Important: connect the listview (which set a footer) before to set the adapter
        scrollPager.connectListView(listView);
        listView.setAdapter(adapter);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        scrollPager.setEmptyView(emptyListViewControl);

        listView.setEmptyView(emptyListViewControl);

        RefreshBar refreshBar = (RefreshBar) view.findViewById(id.fragment_all_shows_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
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
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(getActivity(), ShowActivity.class);
        Event event = adapter.getItem(position);

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put("Cell Position", position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TYPE);

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);
        intent.putExtras(args);

        startActivity(intent);
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
}
