/*
*
* @author Charlie Chilton 2014/01/24
*
* Copyright (C) 2014 Live Nation Labs. All rights reserved.
*
*/

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.ConfigFilePersistenceHelper;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.pagination.AllShowsScrollPager;
import com.livenation.mobile.android.na.pagination.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.ui.adapters.EventStickyHeaderAdapter;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.na.utils.EventUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.TopChartParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AllShowsFragment extends LiveNationFragmentTab implements OnItemClickListener, BasicApiCallback<List<Chart>>, LocationUpdateReceiver.LocationUpdateListener {
    private EventStickyHeaderAdapter adapter;
    private ViewGroup chartingContainer;
    private List<Chart> featured;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventStickyHeaderAdapter(getActivity(), ShowView.DisplayMode.EVENT);
        scrollPager = new AllShowsScrollPager(adapter);
        featured = new ArrayList<Chart>();
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


        scrollPager.load();
        retrieveCharts();
        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position,
                            long id) {
        //Since this listview has a header view (featured), we can not use adapter.getItem(x) as x
        //will be offset by the number of header views. This is the alternative according to:
        // http://stackoverflow.com/questions/11106397/listview-addheaderview-causes-position-to-increase-by-one
        final Event event = (Event) parent.getItemAtPosition(position);
        if (event == null) {
            //user clicked the footer/loading view
            return;
        }

        List<TicketOffering> offerings = event.getTicketOfferings();
        EventUtils.redirectToSDP(getActivity(), event);

        //Analytics
        final Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.CELL_POSITION, position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, AnalyticsCategory.ALL_SHOWS, props);
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

            TransitioningImageView image = (TransitioningImageView) view.findViewById(android.R.id.icon);
            image.setImageUrl(chart.getImageUrl(), LiveNationApplication.get().getImageLoader(), TransitioningImageView.LoadAnimation.FADE_ZOOM);

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(chart.getArtistName());
            view.setOnClickListener(new FeaturedOnClick(chart));

            chartingContainer.addView(view);
        }
    }

    private void retrieveCharts() {
        ConfigFilePersistenceHelper installedAppConfig = LiveNationApplication.get().getInstalledAppConfig();

        TopChartParameters params = new TopChartParameters();
        LiveNationApplication.getLiveNationProxy().getChart(installedAppConfig.getFeaturedCarouselChartName(), params, this);
    }

    @Override
    BaseDecoratedScrollPager getScrollPager() {
        return scrollPager;
    }

    //getMobileFeatured Callback
    @Override
    public void onResponse(List<Chart> response) {
        featured = response;
        setFeatured(response);
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
        if (emptyListViewControl != null) {
            emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
        }
    }

    //LocationUpdateReceiver callback
    @Override
    public void onLocationUpdated(int mode, City city) {
        scrollPager.resetDataAndClearView();
        scrollPager.load();
        retrieveCharts();
    }

    private class FeaturedOnClick implements View.OnClickListener {
        private final Chart chart;

        private FeaturedOnClick(Chart chart) {
            this.chart = chart;
        }

        @Override
        public void onClick(View v) {
            EventUtils.redirectToSDP(getActivity(), chart.getChartableId().toString());
        }
    }
}
