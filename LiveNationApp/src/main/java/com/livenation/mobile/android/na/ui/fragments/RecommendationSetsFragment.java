/*
 * 
 * @author Charlie Chilton 2014/01/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.RecommendationSetsScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter.RecommendationItem;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class RecommendationSetsFragment extends LiveNationFragment implements OnItemClickListener , ApiServiceBinder, SwipeRefreshLayout.OnRefreshListener{
    private StickyListHeadersListView listView;
    private RecommendationsAdapter adapter;
    private RecommendationSetsScrollPager scrollPager;
    private EmptyListViewControl emptyListViewControl;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scrollPager.reset();
            scrollPager.load();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new RecommendationsAdapter(getActivity(), new ArrayList<RecommendationItem>());
        scrollPager = new RecommendationSetsScrollPager(adapter);
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sub_empty_list, container, false);
        listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
        listView.setOnItemClickListener(RecommendationSetsFragment.this);
        //Important: connect the listview (which set a footer) before to set the adapter
        scrollPager.connectListView(listView);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setAreHeadersSticky(false);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        scrollPager.setEmptyView(emptyListViewControl);
        listView.setEmptyView(emptyListViewControl);

        RefreshBar refreshBar = (RefreshBar) view.findViewById(id.fragment_all_shows_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(id.fragment_all_shows_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        scrollPager.connectSwipeRefreshLayout(swipeRefreshLayout);

        //Scroll until the top of the list. Refresh only when the first item of the listview is visible.
        listView.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
            }
        });
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.BroadCastReceiver.MUSIC_LIBRARY_UPDATE));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
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
        Event event = adapter.getItem(position).get();

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);
        intent.putExtras(args);

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.RECOMMENDATIONS, props);

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

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setSoundEffectsEnabled(true);
        scrollPager.reset();
        scrollPager.load();
    }
}
