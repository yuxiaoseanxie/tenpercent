/*
 * 
 * @author Charlie Chilton 2014/01/16
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationListFragment;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowsListFragment extends LiveNationListFragment implements EventsView {
	//TODO: Refactor this out of scope
	private List<Event> items;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//TODO: saveInstanceState data serializaiton
		items = new ArrayList<Event>();
		//TODO: Refactor redundant layout parameter out
		setListAdapter(new EventAdapter(getActivity(), R.layout.list_show_item, items));
	}
	
	@Override
	public void setEvents(List<Event> events) {
		items.clear();
		items.addAll(events);
		EventAdapter adapter = (EventAdapter) getListAdapter();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getActivity(), ShowActivity.class);
		Event event = items.get(position);
		intent.putExtra(EventsPresenter.PARAMETER_EVENT_ID, event.getId());
		startActivity(intent);
	}
	
	private class EventAdapter extends ArrayAdapter<Event> {
		private final List<Event> events;
		private final LayoutInflater inflater;
		
		public EventAdapter(Context context, int resource, List<Event> events) {
			super(context, resource, events);
			this.events = events;
			this.inflater = LayoutInflater.from(context);
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
			
			Event event = events.get(position);
			holder.getTitle().setText(event.getName());
			holder.getLocation().setText(event.getVenue().getName());
			
			//TODO: Move date parsing to Data Model Entity helper. This is ugly 
			SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.DATE_TIME_Z_FORMAT, Locale.US);
			
			try {
				Date date = sdf.parse(event.getStartTime());
				holder.getDate().setDate(date);
			} catch (ParseException e) {
				throw new RuntimeException("Error parsing date: " + event.getStartTime());
			}
			
			return view;
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
	}
	
}
