package com.livenation.mobile.android.na2.ui.fragments;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.presenters.views.EventsView;
import com.livenation.mobile.android.na2.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na2.ui.views.VenueShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowsListNonScrollingFragment extends LiveNationFragment implements EventsView {
	private ViewGroup showContainer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_show_fixed, container,
				false);
		
		showContainer = (ViewGroup) result;

		return result;
	}
	
	@Override
	public void setEvents(List<Event> events) {
		showContainer.removeAllViews();
		for (Event event : events) {
			VenueShowView show = new VenueShowView(getActivity());
			show.setEvent(event);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			showContainer.addView(show, layoutParams);
		}
	}
	
}
