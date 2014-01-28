/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

public class VenueFragment extends LiveNationFragment implements SingleVenueView {
	public static final String PARAMETER_VENUE_ID = "venue_id";
	
	private TextView venueTitle;
	@SuppressWarnings("unused")
	private ViewGroup showContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_venue, container,
				false);
		venueTitle = (TextView) result.findViewById(R.id.fragment_venue_title);
		showContainer = (ViewGroup) result.findViewById(R.id.fragment_venue_show_container);
		return result;
	}

	@Override
	public void setVenue(Venue venue) {
		venueTitle.setText(venue.getName());
	}
}
