package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.pagination.ArtistShowsScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.ui.ArtistShowsActivity;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.adapters.EventAdapter;
import com.livenation.mobile.android.na.ui.support.LiveNationListFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ArtistShowsListFragment extends LiveNationListFragment implements ApiServiceBinder {
    private EventAdapter adapter;
    private ArtistShowsScrollPager artistShowsScrollPager;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new EventAdapter(getActivity(), ShowView.DisplayMode.ARTIST);
        setListAdapter(adapter);

        String entityId = getActivity().getIntent().getStringExtra(ArtistShowsActivity.EXTRA_ARTIST_ID);
        long artistId = DataModelHelper.getNumericEntityId(entityId);
        this.artistShowsScrollPager = new ArtistShowsScrollPager(artistId, adapter);

        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        artistShowsScrollPager.connectListView(getListView());
    }

    @Override
    public void onStop() {
        super.onStop();
        artistShowsScrollPager.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
    }

    //endregion


    //region Api Binding

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        artistShowsScrollPager.reset();
        artistShowsScrollPager.load();
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
}
