package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.na.ui.ArtistShowsActivity;
import com.livenation.mobile.android.na.ui.adapters.EventAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationListFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

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

    //endregion


    private class ScrollPager extends BaseDecoratedScrollPager<Event> {

        private ScrollPager(ArrayAdapter<Event> adapter) {
            super(30, adapter);
        }

        @Override
        public FetchRequest<Event> getFetchRequest(int offset, int limit, FetchResultHandler callback) {
            return new EventsFetchRequest(offset, limit, callback);
        }

        private class EventsFetchRequest extends FetchRequest<Event> implements ArtistEventsView {

            private EventsFetchRequest(int offset, int limit, FetchResultHandler<Event> fetchResultHandler) {
                super(offset, limit, fetchResultHandler);
            }

            @Override
            public void run() {
                String artistId = getActivity().getIntent().getStringExtra(ArtistShowsActivity.EXTRA_ARTIST_ID);
                Bundle args = getArtistEventsPresenter().getArgs(artistId, getOffset(), getLimit());
                Log.i(getClass().getName(), "args: " + args);
                getArtistEventsPresenter().initialize(getActivity(), args, this);
            }

            @Override
            public void setArtistEvents(ArtistEvents artistEvents) {
                Log.i(getClass().getName(), "artistEvents: " + artistEvents.getAll());
                getFetchResultHandler().deliverResult(artistEvents.getAll());
            }

            @Override
            public void cancel() {
                getArtistEventsPresenter().cancel(this);
            }
        }
    }
}
