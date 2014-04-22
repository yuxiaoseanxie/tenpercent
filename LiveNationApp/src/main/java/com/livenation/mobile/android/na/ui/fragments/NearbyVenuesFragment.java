/*
 * 
 * @author Charlie Chilton 2014/02/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.pagination.NearbyVenuesScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.ArrayList;

import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class NearbyVenuesFragment extends LiveNationFragment implements ListView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener, ApiServiceBinder {
    private static final String START_TIME_FORMAT = "h:mm a zzz";
    private static float METERS_IN_A_MILE = 1609.34f;
    private StickyListHeadersListView listView;
    private EmptyListViewControl emptyListViewControl;
    private EventVenueAdapter adapter;
    private Double lat;
    private Double lng;
    private NearbyVenuesScrollPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackScreenWithLocation("User views Nearby screen", new Props());

        adapter = new EventVenueAdapter(getActivity());
        pager = new NearbyVenuesScrollPager(adapter);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_venues, container, false);
        listView = (StickyListHeadersListView) view.findViewById(R.id.fragment_nearby_venues_list);
        listView.setAdapter(adapter);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        listView.setEmptyView(emptyListViewControl);

        pager.setEmptyView(emptyListViewControl);

        listView.setDivider(null);
        listView.setAreHeadersSticky(false);

        listView.setOnItemClickListener(this);
        listView.setOnHeaderClickListener(this);

        pager.connectListView(listView);

        pager.load();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //If I use persistentApi in the on create() method, I would need to click on every single retry button
        LiveNationApplication.get().getApiHelper().bindApi(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroyView();
        pager.stop();
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
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.lat = apiService.getApiConfig().getLat();
        this.lng = apiService.getApiConfig().getLng();
    }

    @Override
    public void onApiServiceNotAvailable() {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Event event = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), ShowActivity.class);

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);

        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put("Cell Position", position);
        LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TYPE);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView stickyListHeadersListView, View view, int position, long id, boolean b) {
        Venue venue = adapter.getItem(position).getVenue();

        Intent intent = new Intent(getActivity(), VenueActivity.class);

        Bundle args = SingleVenuePresenter.getAruguments(venue.getId());
        SingleVenuePresenter.embedResult(args, venue);

        //Analytics
        Props props = new Props();
        props.put("Venue Name", venue.getName());
        props.put("Cell Position", position);
        LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP);

        intent.putExtras(args);
        getActivity().startActivity(intent);
    }

    private class EventVenueAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter {
        private LayoutInflater inflater;

        public EventVenueAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, new ArrayList<Event>());
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;

            if (null == convertView) {
                view = inflater.inflate(R.layout.list_show_nearby_item, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            Event event = getItem(position);
            holder.getTitle().setText(event.getName());

            String startTime = DateFormat.format(START_TIME_FORMAT, event.getLocalStartTime()).toString();

            holder.getStartTime().setText(startTime);
            holder.getDate().setDate(event.getLocalStartTime());

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            View view = null;
            ViewHeaderHolder holder = null;
            if (null == convertView) {
                view = inflater.inflate(R.layout.list_venue_nearby_header, null);
                holder = new ViewHeaderHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHeaderHolder) view.getTag();
            }

            TextView title = holder.getVenueTitle();
            Event event = getItem(position);
            title.setText(event.getVenue().getName());

            TextView location = holder.getLocation();
            location.setText(event.getVenue().getAddress().getSmallFriendlyAddress(false));
            TextView distance = holder.getDistance();

            if (null != lat && null != lng) {
                distance.setVisibility(View.VISIBLE);
                float[] result = new float[1];
                double venueLat = Double.valueOf(event.getVenue().getLat());
                double venueLng = Double.valueOf(event.getVenue().getLng());
                Location.distanceBetween(lat, lng, venueLat, venueLng, result);
                float miles = result[0] / METERS_IN_A_MILE;
                distance.setText(String.format("%.1f mi", miles));
            } else {
                distance.setVisibility(View.GONE);
            }
            holder.getFavorite().setChecked(false);

            Venue venue = event.getVenue();
            holder.getFavorite().bindToFavorite(Favorite.FAVORITE_VENUE, venue.getName(), venue.getNumericId(), getFavoritesPresenter());

            return view;
        }

        @Override
        public long getHeaderId(int position) {
            Event event = getItem(position);
            long venueId = event.getVenue().getNumericId();
            return venueId;
        }

        private class ViewHolder {
            private final TextView title;
            private final TextView startTime;
            private final VerticalDate date;

            public ViewHolder(View view) {
                this.title = (TextView) view.findViewById(R.id.list_nearby_show_title);
                this.startTime = (TextView) view.findViewById(R.id.list_nearby_show_time);
                this.date = (VerticalDate) view.findViewById(R.id.list_nearby_show_date);
            }

            public TextView getTitle() {
                return title;
            }

            public TextView getStartTime() {
                return startTime;
            }

            public VerticalDate getDate() {
                return date;
            }
        }


        private class ViewHeaderHolder {
            private final TextView venueTitle;
            private final TextView venueLocation;
            private final FavoriteCheckBox venueFavorite;
            private final TextView venueDistance;
            private final ViewGroup venueTextContainer;

            public ViewHeaderHolder(View view) {
                this.venueTitle = (TextView) view.findViewById(R.id.list_venue_header_title);
                this.venueLocation = (TextView) view.findViewById(R.id.list_venue_header_location);
                this.venueFavorite = (FavoriteCheckBox) view.findViewById(R.id.list_venue_header_checkbox);
                this.venueDistance = (TextView) view.findViewById(R.id.list_venue_header_distance);
                this.venueTextContainer = (ViewGroup) view.findViewById(R.id.list_venue_header_text_container);
            }

            public TextView getVenueTitle() {
                return venueTitle;
            }

            public TextView getLocation() {
                return venueLocation;
            }

            public FavoriteCheckBox getFavorite() {
                return venueFavorite;
            }

            public TextView getDistance() {
                return venueDistance;
            }

            public ViewGroup getVenueTextContainer() {
                return venueTextContainer;
            }

        }
    }
}
