/*
 * 
 * @author Charlie Chilton 2014/02/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.presenters.views.VenuesView;
import com.livenation.mobile.android.na2.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na2.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;


public class NearbyVenuesFragment extends LiveNationFragment implements VenuesView{
	private StickyListHeadersListView listView;
	private EventVenueAdapter adapter;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.DATE_TIME_Z_FORMAT, Locale.US);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EventVenueAdapter(getActivity());
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_nearby_venues, container, false);
		listView = (StickyListHeadersListView) view.findViewById(R.id.fragment_nearby_venues_list);
		listView.setAdapter(adapter);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		init();
	}

    @Override
    public void onStop() {
        super.onStop();
        deinit();
    }

    @Override
	public void setVenues(List<Venue> venues) {
		List<Event> transformed = DataModelHelper.flattenVenueEvents(venues);
		adapter.getItems().clear();
		adapter.getItems().addAll(transformed);
		adapter.notifyDataSetChanged();
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
	
	private void init() {
		Context context = getActivity();
		Bundle args = getActivity().getIntent().getExtras();
		getNearbyVenuesPresenter().initialize(context, args, NearbyVenuesFragment.this);
	}

    private void deinit() {
        getNearbyVenuesPresenter().cancel(NearbyVenuesFragment.this);
    }

	private class EventVenueAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	    private LayoutInflater inflater;
		private final List<Event> items;
	
		public EventVenueAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			this.items = new ArrayList<Event>();
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
				view = inflater.inflate(R.layout.list_show_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}
			
			Event event = items.get(position);
			holder.getTitle().setText(event.getName());
			holder.getLocation().setText(event.getVenue().getName());
			
			//TODO: Move date parsing to Data Model Entity helper. This is ugly 
			try {
				Date date = sdf.parse(event.getStartTime());
				holder.getDate().setDate(date);
			} catch (ParseException e) {
				//wtf'y f.
				e.printStackTrace();
			}
			
			return view;
		}
		
		public List<Event> getItems() {
			return items;
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			View view = null;
			ViewHeaderHolder holder = null;
			if (null == convertView) {
				view = inflater.inflate(R.layout.list_venue_header, null);
				holder = new ViewHeaderHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHeaderHolder) view.getTag();
			}
			
			TextView text = holder.getText();
			Event event = items.get(position);
			text.setText(event.getVenue().getName());
			
			return view;	
		}

		@Override
		public long getHeaderId(int position) {
			Event event = items.get(position);
			long venueId = event.getVenue().getNumericId();
			return venueId;
		}
		
		private class ViewHolder {
			private final TextView title;
			private final TextView location;
			private final VerticalDate date;
			
			public ViewHolder(View view) {
				this.title = (TextView) view.findViewById(R.id.list_generic_show_title);
				this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
				this.date = (VerticalDate) view.findViewById(R.id.list_generic_show_date);
			}
			
			public TextView getTitle() {
				return title;
			}
			
			public TextView getLocation() {
				return location;
			}
			
			public VerticalDate getDate() {
				return date;
			}
		}
		
		
		private class ViewHeaderHolder {
			private final TextView text;
			
			public ViewHeaderHolder(View view) {
				this.text = (TextView) view.findViewById(R.id.list_venue_header_textview);
			}
			
			public TextView getText() {
				return text;
			}
		}
	}
	
}
