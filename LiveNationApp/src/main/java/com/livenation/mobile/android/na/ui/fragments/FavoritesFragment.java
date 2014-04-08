/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.FavoritesView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

public class FavoritesFragment extends LiveNationFragment implements FavoritesView {
	private FavoritesAdapter artistAdapter;
	private FavoritesAdapter venueAdapter;
	
	private TabHost tabHost;
	private StickyListHeadersListView artistList;
	private StickyListHeadersListView venueList;
    private EmptyListViewControl artistEmptyView;
    private EmptyListViewControl venueEmptyView;


    public static final String ARG_SHOW_TAB = "show_tab";
	public static final int ARG_VALUE_ARTISTS = 0;
	public static final int ARG_VALUE_VENUES = 1;	
	
	private static final FavoriteComparator favoriteComparator = new FavoriteComparator();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		artistAdapter = new FavoritesAdapter(getActivity());
		venueAdapter = new FavoritesAdapter(getActivity());
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
		tabSpec = tabHost.newTabSpec("artists");
		tabSpec.setIndicator(view);
		tabSpec.setContent(R.id.fragment_favorite_artists_list);
		tabHost.addTab(tabSpec);
		
		title = getString(R.string.tab_favorites_venues);
		view = createTab(getActivity(), title);
		tabSpec = tabHost.newTabSpec("venues");
		tabSpec.setIndicator(view);
		tabSpec.setContent(R.id.fragment_favorite_venues_list);
		
		tabHost.addTab(tabSpec);	

		artistList = (StickyListHeadersListView) result.findViewById(R.id.fragment_favorite_artists_list);
        artistEmptyView = (EmptyListViewControl) result.findViewById(R.id.fragment_favorite_artists_empty);

		artistList.setEmptyView(artistEmptyView);
		artistList.setAdapter(artistAdapter);
		artistList.setDivider(null);
		artistList.setAreHeadersSticky(false);
		
		venueList = (StickyListHeadersListView) result.findViewById(R.id.fragment_favorite_venues_list);
        venueEmptyView = (EmptyListViewControl) result.findViewById(R.id.fragment_favorite_venues_empty);

		venueList.setEmptyView(venueEmptyView);
		venueList.setAdapter(venueAdapter);
		venueList.setDivider(null);
		venueList.setAreHeadersSticky(false);
		
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
		
		return result;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Parcelable artistState = artistList.getWrappedList().onSaveInstanceState();
		Parcelable venueState = venueList.getWrappedList().onSaveInstanceState();
		
		outState.putParcelable("" + artistList.getId(), artistState);
		outState.putParcelable("" + venueList.getId(), venueState);
		outState.putInt(ARG_SHOW_TAB, tabHost.getCurrentTab());
	}
	
	@Override
	public void applyInstanceState(Bundle state) {
		super.applyInstanceState(state);
		Parcelable artistState = state.getParcelable("" + artistList.getId());
		Parcelable venueState = state.getParcelable("" + venueList.getId());
		int currentTab = state.getInt(ARG_SHOW_TAB);
		
		artistList.getWrappedList().onRestoreInstanceState(artistState);
		venueList.getWrappedList().onRestoreInstanceState(venueState);
		tabHost.setCurrentTab(currentTab);
	}

	@Override
	public void setFavorites(List<Favorite> favorites) {
		Collections.sort(favorites, favoriteComparator);
		
		List<Favorite> artistFavorites = filterFavorites(favorites, "artist");
		artistAdapter.getItems().clear();
		artistAdapter.getItems().addAll(artistFavorites);

		List<Favorite> venueFavorites = filterFavorites(favorites, "venue");
		venueAdapter.getItems().clear();
		venueAdapter.getItems().addAll(venueFavorites);

        if (venueAdapter.getCount() == 0) {
            venueEmptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }

        if (artistAdapter.getCount() == 0) {
            artistEmptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }

        artistAdapter.notifyDataSetChanged();
		venueAdapter.notifyDataSetChanged();
	}
	
	private static List<Favorite> filterFavorites(List<Favorite> favorites, String type) {
		List<Favorite> filtered = new ArrayList<Favorite>();
		for (Favorite favorite : favorites) {
			if (type.equalsIgnoreCase(favorite.getType())) {
				filtered.add(favorite);
			}
 		}
		return filtered;
	}

	/**
	 * Here we have to return our own Tab View object to get our desired LiveNation red tab.
	 * 
	 * Because Google forgot to make the default tabs in the TabHost XML stylable....
	 * 
	 */
	private View createTab(Context context, String title) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_tab, null);
		TextView text = (TextView) view.findViewWithTag("titleText");
		text.setText(title);
		return view;
	}
	
	private class FavoritesAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	    private LayoutInflater inflater;
		private final List<Favorite> items;
	
		public FavoritesAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			this.items = new ArrayList<Favorite>();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View view = null;
			
			
			if (null == convertView) {
				view = inflater.inflate(R.layout.favorite_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}
			
			Favorite favorite = items.get(position);
			holder.getTitle().setText(favorite.getName());
            holder.getCheckbox().setChecked(true);
			holder.getCheckbox().setOnClickListener(new OnFavoriteClickListener.OnFavoriteClick(favorite, getFavoritesPresenter(), getActivity()));
			return view;
		}
		
		public List<Favorite> getItems() {
			return items;
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			View view = null;
			ViewHeaderHolder holder = null;
			if (null == convertView) {
				view = inflater.inflate(R.layout.favorite_item_header, null);
				holder = new ViewHeaderHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHeaderHolder) view.getTag();
			}
			
			TextView text = holder.getText();
			Favorite favorite = items.get(position);
			String textValue = "-";
			if (!TextUtils.isEmpty(favorite.getName())) {
				textValue = "" + favorite.getName().charAt(0);
			}
			text.setText(textValue);
			
			return view;	
		}

		@Override
		public long getHeaderId(int position) {
			Favorite favorite = items.get(position);
			if (null == favorite.getName()) {
				return 0;
			}
			long headerId = favorite.getName().charAt(0);
			return headerId;
		}
		
		private class ViewHolder {
			private final TextView title;
			private final CheckBox checkbox;
			
			public ViewHolder(View view) {
				this.title = (TextView) view.findViewById(R.id.favorite_item_title);
				this.checkbox = (CheckBox) view.findViewById(R.id.favorite_item_checkbox);
			}
			
			public TextView getTitle() {
				return title;
			}
			
			public CheckBox getCheckbox() {
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
	
	private static class FavoriteComparator implements Comparator<Favorite> {

		@Override
		public int compare(Favorite lhs, Favorite rhs) {
			String lhsName = lhs.getName();
			String rhsName = rhs.getName();
			return String.CASE_INSENSITIVE_ORDER.compare(lhsName, rhsName);
		}
		
	}
}
