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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.AllShowsScrollPager;
import com.livenation.mobile.android.na.pagination.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventStickyHeaderAdapter;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.TopChartParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

import com.segment.android.models.Props;

public class AllShowsFragment extends LiveNationFragmentTab implements OnItemClickListener, ApiServiceBinder, ApiService.BasicApiCallback<List<Chart>> {
    private EventStickyHeaderAdapter adapter;
    private ViewGroup chartingContainer;
    private List<Chart> featured;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventStickyHeaderAdapter(getActivity(), ShowView.DisplayMode.EVENT);
        scrollPager = new AllShowsScrollPager(adapter);
        featured = new ArrayList<Chart>();
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.sub_empty_list);
        listView.setOnItemClickListener(AllShowsFragment.this);
        View result = inflater.inflate(R.layout.fragment_featured, null, false);
        chartingContainer = (ViewGroup) result.findViewById(R.id.featured_charting_container);

        listView.addHeaderView(result);
        listView.setAdapter(adapter);

        RefreshBar refreshBar = (RefreshBar) view.findViewById(R.id.fragment_all_shows_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);
        setFeatured(featured);

        View scrollView = result.findViewById(R.id.featured_charting_horizontal_scrollview);

        //Block vertical swipes while the featured carousel is being swiped from triggering the swipeToRefresh layout
        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //first work out whether the scrollview handled the userevent
                boolean handled = v.onTouchEvent(event);

                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    //if the user if lifting their finger, always re-enable the swipeRefresh
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    //if the scrollview handled the user event, disable the swipeRefresh
                    swipeRefreshLayout.setEnabled(!handled);
                }

                return handled;
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(view.getContext(), ShowActivity.class);
        //Since this listview has a header view (featured), we can not use adapter.getItem(x) as x
        //will be offset by the number of header views. This is the alternative according to:
        // http://stackoverflow.com/questions/11106397/listview-addheaderview-causes-position-to-increase-by-one
        Event event = (Event) parent.getItemAtPosition(position);
        if (event == null) {
            //user clicked the footer/loading view
            return;
        }

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.ALL_SHOWS, props);

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);
        intent.putExtras(args);

        startActivity(intent);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        scrollPager.reset();
        scrollPager.load();
        retrieveCharts(apiService, apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
    }

    @Override
    public void onApiServiceNotAvailable() {
        if (emptyListViewControl != null) {
            emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
        }
    }

    private void setFeatured(List<Chart> featured) {
        this.featured = featured;
        if (chartingContainer == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(chartingContainer.getContext());

        chartingContainer.removeAllViews();
        for (Chart chart : featured) {
            if (TextUtils.isEmpty(chart.getImageUrl())) continue;

            View spacer = inflater.inflate(R.layout.view_featured_spacer, chartingContainer, false);
            chartingContainer.addView(spacer);

            View view = inflater.inflate(R.layout.view_featured_item, chartingContainer, false);

            NetworkImageView image = (NetworkImageView) view.findViewById(android.R.id.icon);
            image.setImageUrl(chart.getImageUrl(), getImageLoader());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(chart.getArtistName());
            view.setOnClickListener(new FeaturedOnClick(chart));

            chartingContainer.addView(view);
        }
    }

    private void retrieveCharts(LiveNationApiService apiService, double lat, double lng) {
        TopChartParameters params = new TopChartParameters();
        params.setLocation(lat, lng);
        apiService.getTopCharts(params, this);
    }

    @Override
    BaseDecoratedScrollPager getScrollPager() {
        return scrollPager;
    }

    @Override
    public void onResponse(List<Chart> response) {
        featured = response;
        setFeatured(response);
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
        //TODO add retry view when error
    }

    private class FeaturedOnClick implements View.OnClickListener {
        private final Chart chart;

        private FeaturedOnClick(Chart chart) {
            this.chart = chart;
        }

        @Override
        public void onClick(View v) {
            String eventId = Event.makeTypedId(chart.getChartableId().toString());
            Intent intent = new Intent(getActivity(), ShowActivity.class);
            intent.putExtras(SingleEventPresenter.getAruguments(eventId));
            startActivity(intent);
        }
    }
}
