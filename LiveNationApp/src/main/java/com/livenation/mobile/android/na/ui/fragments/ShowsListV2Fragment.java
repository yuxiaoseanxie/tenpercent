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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventStickyHeaderAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ShowsListV2Fragment extends LiveNationFragment implements OnItemClickListener, ApiServiceBinder {
    private StickyListHeadersListView listView;
    private EventStickyHeaderAdapter adapter;
    private ScrollPager scrollPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventStickyHeaderAdapter(getActivity(), ShowView.DisplayMode.EVENT);
        scrollPager = new ScrollPager(adapter);
        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shows_list, container, false);
        listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
        listView.setOnItemClickListener(ShowsListV2Fragment.this);
        listView.setAdapter(adapter);
        listView.setEmptyView(view.findViewById(android.R.id.empty));
        scrollPager.connectListView(listView);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollPager.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    }

    private class ScrollPager extends BaseDecoratedScrollPager<Event> {

        private ScrollPager(ArrayAdapter<Event> adapter) {
            super(30, adapter);
        }

        @Override
        public FetchRequest<Event> getFetchRequest(int offset, int limit, FetchResultHandler callback) {
            FetchRequest request = new EventsFetchRequest(offset, limit, callback);
            return request;
        }

        private class EventsFetchRequest extends FetchRequest<Event> implements ApiService.BasicApiCallback<List<Event>> {

            private EventsFetchRequest(int offset, int limit, FetchResultHandler<Event> fetchResultHandler) {
                super(offset, limit, fetchResultHandler);
            }

            @Override
            public void run() {
                LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
                    @Override
                    public void onApiServiceAttached(LiveNationApiService apiService) {
                        EventParameters params = new EventParameters();
                        params.setPage(getOffset(), getLimit());
                        params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
                        params.setSortMethod("start_time");
                        apiService.getEvents(params, EventsFetchRequest.this);
                    }

                    @Override
                    public void onApiServiceNotAvailable() {
                        //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                    }
                });
            }

            @Override
            public void cancel() {
                //TODO: cancel any inprogress API request here
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("fail", "fail");
                //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
            }

            @Override
            public void onResponse(List<Event> response) {
                getFetchResultHandler().deliverResult(response);
                if (response.size() == 0) {
                    //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
                }

            }
        }
    }
}
