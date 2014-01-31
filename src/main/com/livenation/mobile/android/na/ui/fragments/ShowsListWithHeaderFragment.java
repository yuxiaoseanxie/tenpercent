/*
 * 
 * @author Charlie Chilton 2014/01/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowsListWithHeaderFragment extends LiveNationFragment implements EventsView, OnItemClickListener  {
	private StickyListHeadersListView listView;
	private EventAdapter adapter;
	private static SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.DATE_TIME_Z_FORMAT, Locale.US);
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_all_shows_headers, container, false);
		listView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
		adapter = new EventAdapter(getActivity());
		listView.setOnItemClickListener(ShowsListWithHeaderFragment.this);
		listView.setAdapter(adapter);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		return view;
	}
	
	@Override
	public void setEvents(List<Event> events) {
		adapter.getItems().clear();
		adapter.getItems().addAll(events);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ShowActivity.class);
		Event event = adapter.getItems().get(position);
		intent.putExtra(SingleEventPresenter.PARAMETER_EVENT_ID, event.getId());
		startActivity(intent);
	}
	
	
	public class EventAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	    private LayoutInflater inflater;
		private final List<Event> items;
	
		public EventAdapter(Context context) {
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
				view = inflater.inflate(R.layout.list_show_header, null);
				holder = new ViewHeaderHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHeaderHolder) view.getTag();
			}
			
			TextView text = holder.getText();
			
			//TODO: refactor this into Model helpers (inline or sub-helper classes?)
			String dateRaw = items.get(position).getStartTime();
			try {
				Date date = sdf.parse(dateRaw);
				String dateValue = DateFormat.format("MMMM", date).toString();
				text.setText(dateValue);
			} catch (ParseException e) {
				throw new IllegalStateException("Unparsable date: " + dateRaw);
			}
			
			return view;	
		}

		@Override
		public long getHeaderId(int position) {
			String dateRaw = items.get(position).getStartTime();
			try {
				
				Date date = sdf.parse(dateRaw);
				String dateValue = DateFormat.format("yyyyMM", date).toString();
				return Long.valueOf(dateValue);
			} catch (ParseException e) {
				throw new IllegalStateException("Unparsable date: " + dateRaw);
			}
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
				this.text = (TextView) view.findViewById(R.id.list_show_header_textview);
			}
			
			public TextView getText() {
				return text;
			}
		}
	}
	

	
}
