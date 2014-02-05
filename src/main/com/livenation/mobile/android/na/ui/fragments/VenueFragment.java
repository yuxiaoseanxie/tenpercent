/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Address;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

public class VenueFragment extends LiveNationFragment implements SingleVenueView, EventsView {
	public static final String PARAMETER_VENUE_ID = "venue_id";
	
	private TextView venueTitle;
	private TextView location;
	private TextView telephone;
	private View link;
	private EventsView shows;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_venue, container,
				false);
		venueTitle = (TextView) result.findViewById(R.id.fragment_venue_title);

		location = (TextView) result.findViewById(R.id.venue_detail_location);
		telephone = (TextView) result.findViewById(R.id.venue_detail_telephone);
		link = result.findViewById(R.id.venue_detail_venue_info_link);
		
		Fragment showsFragment = new ShowsListNonScrollingFragment();
		addFragment(R.id.fragment_venue_container_list, showsFragment, "shows");
		
		shows = (EventsView) showsFragment;
		
		return result;
	}

	@Override
	public void setVenue(Venue venue) {
		venueTitle.setText(venue.getName());
		if (null != venue.getAddress()) {
			Address address = venue.getAddress();
			location.setText(address.getSmallFriendlyAddress(true));
		} else {
			location.setText("");
		}
		telephone.setText(venue.getFormattedPhoneNumber());
		OnVenueDetailClick onVenueClick = new OnVenueDetailClick(venue.getId());
		link.setOnClickListener(onVenueClick);
	}
	
	@Override
	public void setEvents(List<Event> events) {
		shows.setEvents(events);
	}
	
	private class OnVenueDetailClick implements View.OnClickListener {
		private String venueId;
		
		public OnVenueDetailClick(String venueId) {
			this.venueId = venueId;
		}
		
		@Override
		public void onClick(View v) {
			Toast.makeText(getActivity(), "Herro: " + venueId, Toast.LENGTH_SHORT).show();
		}
	}
}
