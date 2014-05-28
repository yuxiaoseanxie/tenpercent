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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.AllShowsScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventStickyHeaderAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;
import java.util.List;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class AllShowsFragment extends LiveNationFragment implements OnItemClickListener, ApiServiceBinder, FeatureView {
    private StickyListHeadersListView listView;
    private EventStickyHeaderAdapter adapter;
    private AllShowsScrollPager scrollPager;
    private EmptyListViewControl emptyListViewControl;
    private ViewGroup chartingContainer;
    private List<Chart> featured;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new EventStickyHeaderAdapter(getActivity(), ShowView.DisplayMode.EVENT);
        scrollPager = new AllShowsScrollPager(adapter);
        featured = new ArrayList<Chart>();
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sub_empty_list, container, false);
        listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
        listView.setOnItemClickListener(AllShowsFragment.this);
        View result = inflater.inflate(R.layout.fragment_featured, null, false);
        chartingContainer = (ViewGroup) result.findViewById(R.id.featured_charting_container);

        listView.addHeaderView(result);

        //Important: connect the listview (which set a footer) before to set the adapter
        scrollPager.connectListView(listView);
        listView.setAdapter(adapter);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        scrollPager.setEmptyView(emptyListViewControl);

        listView.setEmptyView(emptyListViewControl);

        RefreshBar refreshBar = (RefreshBar) view.findViewById(id.fragment_all_shows_refresh_bar);
        scrollPager.setRefreshBarView(refreshBar);
        setFeatured(featured);
        return view;
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
        //Since this listview has a header view (featured), we can not use adapter.getItem(x) as x
        //will be offset by the number of header views. This is the alternative according to:
        // http://stackoverflow.com/questions/11106397/listview-addheaderview-causes-position-to-increase-by-one
        Event event = (Event) parent.getItemAtPosition(position);

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
        getFeaturePresenter().initialize(getActivity(), null, this);
    }

    @Override
    public void onApiServiceNotAvailable() {
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
    }


    @Override
    public void setFeatured(List<Chart> featured) {
        this.featured = featured;
        if (null != chartingContainer) {
            setFeaturedView(featured);
        }
    }

    private void setFeaturedView(List<Chart> featured) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

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
