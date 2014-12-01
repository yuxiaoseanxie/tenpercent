/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.platform.Constants;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.segment.android.models.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FavoritesFragment extends LiveNationFragment implements TabHost.OnTabChangeListener {
    public static final String ARG_SHOW_TAB = "show_tab";
    public static final int ARG_VALUE_ARTISTS = 0;
    public static final int ARG_VALUE_VENUES = 1;
    public static final String TAB_TAG_ARTISTS = "artists";
    public static final String TAB_TAG_VENUES = "venues";
    private static final FavoriteComparator favoriteComparator = new FavoriteComparator();
    private FavoritesAdapter artistAdapter;
    private FavoritesAdapter venueAdapter;
    private TabHost tabHost;
    private StickyListHeadersListView artistList;
    private StickyListHeadersListView venueList;
    private EmptyListViewControl artistEmptyView;
    private EmptyListViewControl venueEmptyView;
    private Bundle instanceState;
    private BroadcastReceiver updateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            syncFavorites();
        }
    };
    private ListView.OnItemClickListener artistListClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.ARTIST_NAME, artistAdapter.getItem(position).getName());
            props.put(AnalyticConstants.ARTIST_ID, String.valueOf(artistAdapter.getItem(position).getId()));
            LiveNationAnalytics.track(AnalyticConstants.ARTIST_CELL_TAP, AnalyticsCategory.FAVORITES, props);

            Favorite favorite = artistAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), ArtistActivity.class);
            String entityId = Artist.getAlphanumericId(favorite.getId());
            Bundle args = ArtistActivity.getArguments(entityId);
            intent.putExtras(args);
            startActivity(intent);
        }
    };
    private ListView.OnItemClickListener venueListClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.VENUE_NAME, venueAdapter.getItem(position).getName());
            props.put(AnalyticConstants.VENUE_ID, String.valueOf(venueAdapter.getItem(position).getId()));
            LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, AnalyticsCategory.FAVORITES, props);


            Favorite favorite = venueAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), VenueActivity.class);
            String entityId = Artist.getAlphanumericId(favorite.getId());
            Bundle args = VenueActivity.getArguments(entityId);
            intent.putExtras(args);
            startActivity(intent);
        }
    };

    private static List<Favorite> filterFavorites(List<Favorite> favorites, String type) {
        List<Favorite> filtered = new ArrayList<Favorite>();
        for (Favorite favorite : favorites) {
            if (type.equalsIgnoreCase(favorite.getType())) {
                filtered.add(favorite);
            }
        }
        return filtered;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artistAdapter = new FavoritesAdapter(getActivity().getApplicationContext());
        venueAdapter = new FavoritesAdapter(getActivity().getApplicationContext());

        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).registerReceiver(updateBroadcastReceiver, new IntentFilter(Constants.FAVORITE_UPDATE_INTENT_FILTER));

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_favorites, container,
                false);
        tabHost = (TabHost) result.findViewById(android.R.id.tabhost);
        tabHost.setup();

        String title;
        View view;
        TabSpec tabSpec;

        title = getString(R.string.tab_favorites_artists);
        view = createTab(getActivity(), title);
        tabSpec = tabHost.newTabSpec(TAB_TAG_ARTISTS);
        tabSpec.setIndicator(view);
        tabSpec.setContent(R.id.fragment_favorites_artists);
        tabHost.addTab(tabSpec);
        tabHost.setOnTabChangedListener(this);

        title = getString(R.string.tab_favorites_venues);
        view = createTab(getActivity(), title);
        tabSpec = tabHost.newTabSpec(TAB_TAG_VENUES);
        tabSpec.setIndicator(view);
        tabSpec.setContent(R.id.fragment_favorites_venues);

        tabHost.addTab(tabSpec);

        artistList = (StickyListHeadersListView) result.findViewById(R.id.fragment_favorite_artists_list);
        artistEmptyView = (EmptyListViewControl) result.findViewById(R.id.fragment_favorite_artists_empty);

        artistList.setEmptyView(artistEmptyView);
        artistList.setAdapter(artistAdapter);
        artistList.setDivider(null);
        artistList.setAreHeadersSticky(false);
        artistList.setOnItemClickListener(artistListClickListener);

        venueList = (StickyListHeadersListView) result.findViewById(R.id.fragment_favorite_venues_list);
        venueEmptyView = (EmptyListViewControl) result.findViewById(R.id.fragment_favorite_venues_empty);

        venueList.setEmptyView(venueEmptyView);
        venueList.setAdapter(venueAdapter);
        venueList.setDivider(null);
        venueList.setAreHeadersSticky(false);
        venueList.setOnItemClickListener(venueListClickListener);

        if (getActivity().getIntent().hasExtra(ARG_SHOW_TAB)) {
            int showTab = getActivity().getIntent().getIntExtra(ARG_SHOW_TAB, -1);
            switch (showTab) {
                case ARG_VALUE_ARTISTS:
                    tabHost.setCurrentTab(ARG_VALUE_ARTISTS);
                    break;
                case ARG_VALUE_VENUES:
                    tabHost.setCurrentTab(ARG_VALUE_VENUES);
                    break;
            }
            //remove the opening tab, so it doesnt force the page back on rotate
            getActivity().getIntent().removeExtra(ARG_SHOW_TAB);
        }

        if (instanceState != null) {
            applyInstanceState(instanceState);
        }

        syncFavorites();

        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //The tabhost widget doesn't automatically save its instance state despite having an id :(
        instanceState = new Bundle();
        instanceState.putInt(ARG_SHOW_TAB, tabHost.getCurrentTab());
    }

    public void applyInstanceState(Bundle state) {
        int currentTab = state.getInt(ARG_SHOW_TAB);
        tabHost.setCurrentTab(currentTab);
    }

    public void syncFavorites() {
        Set<Favorite> favorites = LiveNationLibrary.getFavoritesHelper().getFavorites();
        List<Favorite> favs = new ArrayList<Favorite>(favorites);
        Collections.sort(favs, favoriteComparator);

        final int venueScrollPosition = venueList.getFirstVisiblePosition();
        final View venueItemView = venueList.getWrappedList().getChildAt(0);
        int venueOffset = (venueItemView == null) ? 0 : venueItemView.getTop();
        final int artistScrollPosition = artistList.getFirstVisiblePosition();
        final View artistItemView = artistList.getWrappedList().getChildAt(0);
        int artistOffset = (artistItemView == null) ? 0 : artistItemView.getTop();


        List<Favorite> artistFavorites = filterFavorites(favs, "artist");
        artistAdapter.clear();
        artistAdapter.addAll(artistFavorites);

        List<Favorite> venueFavorites = filterFavorites(favs, "venue");
        venueAdapter.clear();
        venueAdapter.addAll(venueFavorites);

        if (venueAdapter.getCount() == 0) {
            venueEmptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        } else {
            venueEmptyView.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
        }

        if (artistAdapter.getCount() == 0) {
            artistEmptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        } else {
            artistEmptyView.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
        }
        venueList.setSelectionFromTop(venueScrollPosition, venueOffset);
        artistList.setSelectionFromTop(artistScrollPosition, artistOffset);

    }

    /**
     * Here we have to return our own Tab View object to get our desired LiveNation red tab.
     * <p/>
     * Because Google forgot to make the default tabs in the TabHost XML stylable....
     */
    private View createTab(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_tab, null);
        TextView text = (TextView) view.findViewWithTag("titleText");
        text.setText(title);
        return view;
    }

    @Override
    public void onTabChanged(String tabId) {
        String eventName;
        if (TAB_TAG_ARTISTS.equals(tabId)) {
            eventName = AnalyticConstants.ARTISTS_TAB_TAP;
        } else {
            eventName = AnalyticConstants.VENUES_TAB_TAP;
        }
        LiveNationAnalytics.track(eventName, AnalyticsCategory.FAVORITES);
    }

    private static class FavoriteComparator implements Comparator<Favorite> {

        @Override
        public int compare(Favorite lhs, Favorite rhs) {
            String lhsName = lhs.getName();
            String rhsName = rhs.getName();
            return String.CASE_INSENSITIVE_ORDER.compare(lhsName, rhsName);
        }

    }

    private class FavoritesAdapter extends ArrayAdapter<Favorite> implements StickyListHeadersAdapter {
        private LayoutInflater inflater;

        private FavoritesAdapter(Context context) {
            super(context, 0, new ArrayList<Favorite>());
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;


            if (null == convertView) {
                view = inflater.inflate(R.layout.favorite_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            Favorite favorite = getItem(position);
            holder.getTitle().setText(favorite.getName());
            holder.getCheckbox().bindToFavorite(favorite, AnalyticsCategory.FAVORITES);

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            View view = null;
            ViewHeaderHolder holder = null;
            if (null == convertView) {
                view = inflater.inflate(R.layout.favorite_item_header, parent, false);
                holder = new ViewHeaderHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHeaderHolder) view.getTag();
            }

            TextView text = holder.getText();
            Favorite favorite = getItem(position);
            String textValue = "-";
            if (!TextUtils.isEmpty(favorite.getName())) {
                textValue = "" + favorite.getName().charAt(0);
            }
            text.setText(textValue);

            return view;
        }

        @Override
        public long getHeaderId(int position) {
            Favorite favorite = getItem(position);
            if (null == favorite.getName()) {
                return 0;
            }
            long headerId = favorite.getName().charAt(0);
            return headerId;
        }

        private class ViewHolder {
            private final TextView title;
            private final FavoriteCheckBox checkbox;

            public ViewHolder(View view) {
                this.title = (TextView) view.findViewById(R.id.favorite_item_title);
                this.checkbox = (FavoriteCheckBox) view.findViewById(R.id.favorite_item_checkbox);
            }

            public TextView getTitle() {
                return title;
            }

            public FavoriteCheckBox getCheckbox() {
                return checkbox;
            }
        }


        private class ViewHeaderHolder {
            private final TextView text;

            public ViewHeaderHolder(View view) {
                this.text = (TextView) view.findViewById(R.id.favorite_item_header);
            }

            public TextView getText() {
                return text;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).unregisterReceiver(updateBroadcastReceiver);
    }
}
