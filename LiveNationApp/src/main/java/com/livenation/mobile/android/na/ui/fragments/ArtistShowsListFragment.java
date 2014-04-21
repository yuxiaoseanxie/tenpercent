package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.na.ui.ArtistShowsActivity;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationListFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ArtistEventsParameters;

import java.util.ArrayList;
import java.util.List;

public class ArtistShowsListFragment extends LiveNationListFragment implements ApiServiceBinder {
    private EventAdapter adapter;
    private ScrollPager scrollPager;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new EventAdapter(getActivity(), ShowView.DisplayMode.ARTIST);
        setListAdapter(adapter);

        this.scrollPager = new ScrollPager(adapter);

        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollPager.connectListView(getListView());
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

    //endregion


    //region Api Binding

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        scrollPager.reset();
        scrollPager.load();
    }

    @Override
    public void onApiServiceNotAvailable() {

    }


    //endregion


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = adapter.getItem(position);
        Bundle arguments = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(arguments, event);
        startActivity(new Intent(getActivity(), ShowActivity.class).putExtras(arguments));
    }


    private class ScrollPager extends BaseDecoratedScrollPager<Event> {

        private ScrollPager(ArrayAdapter<Event> adapter) {
            super(30, adapter);
        }

        @Override
        public void fetch(final int offset, final int limit, final ApiService.BasicApiCallback<List<Event>> callback) {
            LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
                @Override
                public void onApiServiceAttached(LiveNationApiService apiService) {
                    ArtistEventsParameters params = new ArtistEventsParameters();
                    params.setPage(offset, limit);
                    apiService.getArtistEvents(params, new ApiService.BasicApiCallback<List<Event>>() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                        }

                        @Override
                        public void onResponse(final List<Event> response) {
                            //TODO remove that. Should be able to access location without binding the API
                            LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
                                @Override
                                public void onApiServiceAttached(LiveNationApiService apiService) {
                                    double lat = apiService.getApiConfig().getLat();
                                    double lng = apiService.getApiConfig().getLng();
                                    ArtistEvents artistEvents = ArtistEvents.from((ArrayList<Event>) response, lat, lng);
                                    callback.onResponse(artistEvents.getAll());
                                }

                                @Override
                                public void onApiServiceNotAvailable() {
                                    //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                                }
                            });
                        }
                    });
                }

                @Override
                public void onApiServiceNotAvailable() {
                    //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                }
            });
        }
    }
}
