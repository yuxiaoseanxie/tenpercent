/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Image;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LineupEntry;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.SingleEventParameters;

public class ShowFragment extends LiveNationFragment {
	public static final String PARAMETER_EVENT_ID = "event_id";
	private TextView artistTitle;
	private ViewGroup lineupContainer;
	private NetworkImageView artistImage;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getData(getActivity().getIntent());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_show, container,
				false);
		artistTitle = (TextView) result.findViewById(R.id.fragment_show_artist_title);
		lineupContainer = (ViewGroup) result.findViewById(R.id.fragment_show_artist_lineup_container);
		artistImage = (NetworkImageView) result.findViewById(R.id.fragment_show_image);
		return result;
	}
	
	public void getData(Intent bundle) {
		String eventIdRaw = bundle.getStringExtra(PARAMETER_EVENT_ID);

		long eventId = Event.getNumericEventId(eventIdRaw);
		
		SingleEventParameters parameters = ApiParameters.createSingleEventParameters();
		parameters.setEventId(eventId);
		
		getApiService().getSingleEvent(parameters, new GetSingleEventCallback());
	}
	
	private class GetSingleEventCallback implements LiveNationApiService.GetSingleEventApiCallback {
		@Override
		public void onGetEvent(Event event) {
			artistTitle.setText(event.getName());
			String imageUrl = null;
			//TODO: Refactor this when Activity -> Fragment data lifecycle gets implemented
			for (LineupEntry lineup : event.getLineup()) {
				LineupView view = new LineupView(getActivity());
				view.getTitle().setText(lineup.getName());
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				lineupContainer.addView(view, layoutParams);
				if (null == imageUrl) {
					if (lineup.getImages().length > 0) {
						for (Image image : lineup.getImages()) {
							if (image.hasTapImage()) {
								imageUrl = image.getTapImage();
								break;
							}
						}
					}
				}
			}
			if (null != imageUrl) {
				artistImage.setImageUrl(imageUrl, getImageLoader());
			}
		}
		
		@Override
		public void onFailure(int failureCode, String message) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
