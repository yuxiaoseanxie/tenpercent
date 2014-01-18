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
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.support.LiveNationListFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.util.Logger;

public class ShowsListFragment extends LiveNationListFragment {
	//TODO: Refactor this out of scope
	private List<Event> items;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//TODO: saveInstanceState data serializaiton
		items = new ArrayList<Event>();
		//TODO: Refactor redundant layout parameter out
		setListAdapter(new EventAdapter(getActivity(), R.layout.list_show_item, items));
		getData();
	}
	
	private void getData() {
		//TODO: Parameters from intent
		ApiParameters.EventParameters parameters = ApiParameters.createEventParameters();
		GetEventsCallback callback = new GetEventsCallback();
		getApiService().getEvents(parameters, callback);
	}
	
	private class GetEventsCallback implements LiveNationApiService.GetEventsApiCallback {
		@Override
		public void onGetEvents(List<Event> result) {
			items.clear();
			items.addAll(result);
			EventAdapter adapter = (EventAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
		}
		
		@Override
		public void onFailure(int failureCode, String message) {
			Logger.log("WTF", "Error: " + failureCode + " " + message);
			// TODO: this
		}
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
			SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.TIME_FORMAT, Locale.US);
			
			try {
				Date date = sdf.parse(event.getStartTime());
				String day = DateFormat.format("d", date).toString();
				String dotw = DateFormat.format("EEE", date).toString();
				String month = DateFormat.format("MMM", date).toString();
				
				holder.getDateDotw().setText(dotw);
				holder.getDateDay().setText(day);
				holder.getDateMonth().setText(month);
			} catch (ParseException e) {
				//wtf'y f.
				e.printStackTrace();
			}
			
			return view;
		}
		
		private class ViewHolder {
			private final TextView title;
			private final TextView location;
			private final TextView dateDotw;
			private final TextView dateDay;
			private final TextView dateMonth;
			
			public ViewHolder(View view) {
				this.title = (TextView) view.findViewById(R.id.list_generic_show_title);
				this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
				this.dateDotw = (TextView) view.findViewById(R.id.list_show_item_date_dotw);
				this.dateDay = (TextView) view.findViewById(R.id.list_show_item_date_day);
				this.dateMonth = (TextView) view.findViewById(R.id.list_show_item_date_month);
			}
			
			public TextView getTitle() {
				return title;
			}
			
			public TextView getLocation() {
				return location;
			}
			
			public TextView getDateDotw() {
				return dateDotw;
			}
						
			public TextView getDateDay() {
				return dateDay;
			}

			public TextView getDateMonth() {
				return dateMonth;
			}
		}
	}
	
}
