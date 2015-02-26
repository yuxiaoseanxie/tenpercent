package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.pagination.ArtistShowsScrollPager;
import com.livenation.mobile.android.na.ui.ArtistShowsActivity;
import com.livenation.mobile.android.na.ui.adapters.EventAdapter;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.na.utils.EventUtils;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class ArtistShowsListFragment extends ListFragment {
    private EventAdapter adapter;
    private ArtistShowsScrollPager artistShowsScrollPager;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        this.adapter = new EventAdapter(getActivity(), ShowView.DisplayMode.ARTIST);
        setListAdapter(adapter);

        String entityId = getActivity().getIntent().getStringExtra(ArtistShowsActivity.EXTRA_ARTIST_ID);
        long artistId = DataModelHelper.getNumericEntityId(entityId);
        this.artistShowsScrollPager = new ArtistShowsScrollPager(artistId, adapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(getResources().getDrawable(R.drawable.ui_gutter_divider));
        getListView().setDividerHeight((int) getResources().getDimension(R.dimen.divider_height));

        artistShowsScrollPager.connectListView(getListView());
        artistShowsScrollPager.load();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        artistShowsScrollPager.stop();
    }

    //endregion

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position < adapter.getCount()) {
            Event event = adapter.getItem(position);
            EventUtils.redirectToSDP(getActivity(), event);
        }
    }
}
