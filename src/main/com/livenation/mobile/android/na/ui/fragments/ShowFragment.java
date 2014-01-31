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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.na.ui.views.ShowVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Image;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LineupEntry;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

public class ShowFragment extends LiveNationFragment implements SingleEventView {
	private TextView artistTitle;
	private ViewGroup lineupContainer;
	private NetworkImageView artistImage;
	private ShowVenueView venueDetails;
	private GoogleMap map;
	
	private static final float DEFAULT_MAP_ZOOM = 13f;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_show, container,
				false);
		artistTitle = (TextView) result.findViewById(R.id.fragment_show_artist_title);
		lineupContainer = (ViewGroup) result.findViewById(R.id.fragment_show_artist_lineup_container);
		artistImage = (NetworkImageView) result.findViewById(R.id.fragment_show_image);
		venueDetails = (ShowVenueView) result.findViewById(R.id.fragment_show_venue_details);

		SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_show_map);
		map = mapFragment.getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setAllGesturesEnabled(false);
		return result;
	}
	
	@Override
	public void setEvent(Event event) {
		artistTitle.setText(event.getName());
		
		if (null != event.getVenue()) {
			Venue venue = event.getVenue();
			
			venueDetails.getTitle().setText(venue.getName());
			
			if (null != venue.getAddress()) {
				String address = venue.getAddress().getSmallFriendlyAddress(false);
				venueDetails.getLocation().setText(address);		
			} else {
				venueDetails.getLocation().setText("");
			}
			 
			venueDetails.getTelephone().setText(venue.getFormattedPhoneNumber());
			
			OnVenueDetailsClick onVenueClick = new OnVenueDetailsClick(event.getVenue());
			venueDetails.setOnClickListener(onVenueClick);
			
			double lat = Double.valueOf(venue.getLat());
			double lng = Double.valueOf(venue.getLng());
			setMapLocation(lat, lng);
			
		} else {
			venueDetails.setOnClickListener(null);
		}
			
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
	
	private void setMapLocation(double lat, double lng) {
		LatLng latLng = new LatLng(lat, lng);

		MarkerOptions marker = new MarkerOptions();
		marker.position(latLng);

		map.clear();
		map.addMarker(marker);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM));
	}

	private class OnVenueDetailsClick implements View.OnClickListener {
		private final Venue venue;
		
		public OnVenueDetailsClick(Venue venue) {
			this.venue = venue;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), VenueActivity.class);
			intent.putExtra(VenueFragment.PARAMETER_VENUE_ID, venue.getId());
			intent.putExtra(SingleVenuePresenter.INTENT_DATA_KEY, venue);
			startActivity(intent);
		}
	}
	
}
