/*
 * 
 * @author Charlie Chilton 2014/01/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.pagination.RecommendationSetsScrollPager;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter.RecommendationItem;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.na.utils.EventUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class RecommendationSetsFragment extends LiveNationFragmentTab implements OnItemClickListener {

    private RecommendationsAdapter adapter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scrollPager.resetDataAndClearView();
            scrollPager.load();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new RecommendationsAdapter(getActivity(), new ArrayList<RecommendationItem>());
        scrollPager = new RecommendationSetsScrollPager(adapter);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.sub_empty_list);
        listView.setOnItemClickListener(RecommendationSetsFragment.this);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setAreHeadersSticky(false);


        RefreshBar refreshBar = (RefreshBar) view.findViewById(id.fragment_all_shows_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.BroadCastReceiver.MUSIC_LIBRARY_UPDATE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGOUT_INTENT_FILTER));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGIN_INTENT_FILTER));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.FAVORITE_UPDATE_INTENT_FILTER));

        scrollPager.load();
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position,
                            long id) {
        RecommendationItem recommendationItem = (RecommendationItem) parent.getItemAtPosition(position);

        if (recommendationItem == null || recommendationItem.get() == null) {
            //user clicked the footer/loading view
            return;
        }

        final Event event = recommendationItem.get();
        EventUtils.redirectToSDP(getActivity(), event);


        //Analytics
        final Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.RECOMMENDATIONS, props);

    }

    @Override
    BaseDecoratedScrollPager getScrollPager() {
        return scrollPager;
    }
}
