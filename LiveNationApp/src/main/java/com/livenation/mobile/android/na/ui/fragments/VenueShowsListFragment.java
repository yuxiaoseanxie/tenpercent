package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.pagination.VenueShowsScrollPager;
import com.livenation.mobile.android.na.ui.VenueShowsActivity;
import com.livenation.mobile.android.na.ui.adapters.EventAdapter;
import com.livenation.mobile.android.na.ui.views.ShowView;
import android.mobile.livenation.com.livenationui.activity.tools.ActivityOpener;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class VenueShowsListFragment extends ListFragment {
    private EventAdapter adapter;
    private VenueShowsScrollPager venueShowsScrollPager;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        this.adapter = new EventAdapter(getActivity(), ShowView.DisplayMode.VENUE);
        setListAdapter(adapter);

        String entityId = getActivity().getIntent().getStringExtra(VenueShowsActivity.EXTRA_VENUE_ID);
        long venueId = DataModelHelper.getNumericEntityId(entityId);
        this.venueShowsScrollPager = new VenueShowsScrollPager(venueId, adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(getResources().getDrawable(R.drawable.ui_gutter_divider));
        getListView().setDividerHeight((int) getResources().getDimension(R.dimen.divider_height));

        venueShowsScrollPager.connectListView(getListView());
        venueShowsScrollPager.load();
    }

    @Override
    public void onStop() {
        super.onStop();
        venueShowsScrollPager.stop();
    }

    //endregion


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position < adapter.getCount()) {
            Event event = adapter.getItem(position);
            ActivityOpener.redirectToSDP(getActivity(), event);
        }
    }
}
