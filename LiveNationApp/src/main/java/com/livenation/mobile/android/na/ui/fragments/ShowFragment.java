/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener.OnArtistFavoriteClick;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener.OnVenueFavoriteClick;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.na.ui.views.ShowVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LineupEntry;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.util.Logger;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

public class ShowFragment extends LiveNationFragment implements SingleEventView, LiveNationMapFragment.MapReadyListener {
	private TextView artistTitle;
	private TextView calendarText;
	private ViewGroup lineupContainer;
	private NetworkImageView artistImage;
	private ShowVenueView venueDetails;
	private Button findTickets;
	private GoogleMap map;
	
	private static final String CALENDAR_DATE_FORMAT = "EEE MMM d'.' yyyy 'at' h:mm aa";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);
	
	private static final float DEFAULT_MAP_ZOOM = 13f;
	private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"tap"};
	private LiveNationMapFragment mapFragment;
	private VenueFavoriteObserver venueFavoriteObserver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mapFragment = new LiveNationMapFragment();
		mapFragment.setMapReadyListener(this);

		addFragment(R.id.fragment_show_map_container, mapFragment, "map");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_show, container,
				false);
		artistTitle = (TextView) result.findViewById(R.id.fragment_show_artist_title);
		lineupContainer = (ViewGroup) result.findViewById(R.id.fragment_show_artist_lineup_container);
		artistImage = (NetworkImageView) result.findViewById(R.id.fragment_show_image);
		venueDetails = (ShowVenueView) result.findViewById(R.id.fragment_show_venue_details);
		calendarText = (TextView) result.findViewById(R.id.sub_show_calendar_text);
		
		findTickets = (Button) result.findViewById(R.id.fragment_show_ticketbar_button);
		
		return result;
	}

    @Override
    public void onStop() {
        super.onStop();
        deinitVenueFavoriteObserver();
    }

    @Override
	public void setEvent(Event event) {
        //Analytics
        Props props = AnalyticsHelper.getPropsForEvent(event);
        trackScreenWithLocation("User views SDP screen", props);

		artistTitle.setText(event.getName());
		
		try {
			Date date = DATE_FORMATTER.parse(event.getLocalStartTime());
			String calendarValue = DateFormat.format(CALENDAR_DATE_FORMAT, date).toString();
			calendarText.setText(calendarValue);
		} catch (ParseException e) {
			calendarText.setText("");
			Logger.log("ShowFragment", "Error parsing date", e);
			e.printStackTrace();
		} 
	
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
			
			OnVenueDetailsClick onVenueClick = new OnVenueDetailsClick(event);
			venueDetails.setOnClickListener(onVenueClick);
			
			OnVenueFavoriteClick onVenueFavoriteClick = new OnVenueFavoriteClick(venue, getFavoritesPresenter(), getActivity());
			venueDetails.getFavorite().setOnClickListener(onVenueFavoriteClick);

			double lat = Double.valueOf(venue.getLat());
			double lng = Double.valueOf(venue.getLng());
			setMapLocation(lat, lng);

            initVenueFavoriteObserver(venue, venueDetails.getFavorite());

        } else {
			venueDetails.setOnClickListener(null);
		}
		
		OnFindTicketsClick onFindTicketsClick = new OnFindTicketsClick(event);
		findTickets.setOnClickListener(onFindTicketsClick);
			
		String imageUrl = null;		
		//TODO: Refactor this when Activity -> Fragment data lifecycle gets implemented
		lineupContainer.removeAllViews();
		for (LineupEntry lineup : event.getLineup()) {
			LineupView view = new LineupView(getActivity());
			view.getTitle().setText(lineup.getName());
			
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			lineupContainer.addView(view, layoutParams);
			
			OnArtistFavoriteClick onCheckBoxClick = new OnFavoriteClickListener.OnArtistFavoriteClick(lineup, getFavoritesPresenter(), getActivity());
			view.getFavorite().setOnClickListener(onCheckBoxClick);

            view.setFavoriteObserver(lineup);

			if (null == imageUrl) {
				String imageKey = lineup.getBestImageKey(IMAGE_PREFERRED_SHOW_KEYS);
				
				if (null == imageKey) continue;
				
				imageUrl = lineup.getImageURL(imageKey);
			}
		}
		if (null != imageUrl) {
			artistImage.setImageUrl(imageUrl, getImageLoader());
		}
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		this.map = map;
		if (map != null) {
			map.getUiSettings().setZoomControlsEnabled(false);
			map.getUiSettings().setAllGesturesEnabled(false);
		} else {
			//TODO: Possible No Google play services installed
		}

	};

    private void initVenueFavoriteObserver(Venue venue, CheckBox checkbox) {
        deinitVenueFavoriteObserver();
        venueFavoriteObserver = new VenueFavoriteObserver(checkbox);

        Bundle args = getFavoritesPresenter().getObserverPresenter().getBundleArgs(Favorite.FAVORITE_VENUE, venue.getNumericId());
        getFavoritesPresenter().getObserverPresenter().initialize(getActivity(), args, venueFavoriteObserver);
    }

    private void deinitVenueFavoriteObserver() {
        if (null != venueFavoriteObserver) {
            getFavoritesPresenter().getObserverPresenter().cancel(venueFavoriteObserver);
        }
    }
	
	private void setMapLocation(double lat, double lng) {
		if (null == map) return;
		
		LatLng latLng = new LatLng(lat, lng);

		MarkerOptions marker = new MarkerOptions();
		marker.position(latLng);

		map.clear();
		map.addMarker(marker);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM));
	}

	private class OnVenueDetailsClick implements View.OnClickListener {
        private final Event event;
		
		public OnVenueDetailsClick(Event event) {
            this.event = event;
		}

		@Override
		public void onClick(View v) {
            Venue venue = event.getVenue();
			Intent intent = new Intent(getActivity(), VenueActivity.class);

            Bundle args = SingleVenuePresenter.getAruguments(venue.getId());
            SingleVenuePresenter.embedResult(args, venue);
            intent.putExtras(args);

            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            Analytics.track("Venue Cell Tap", props);

			startActivity(intent);
		}
	}
	
	private class OnFindTicketsClick implements View.OnClickListener {
		private final Event event;

		public OnFindTicketsClick(Event event) {
			this.event = event;
		}
		
		@Override
		public void onClick(View v) {
            Props props = AnalyticsHelper.getPropsForEvent(event);
            Analytics.track("Find Tickets Tap", props);
			Toast.makeText(getActivity(), "Find tickets: " + event.getId(), Toast.LENGTH_SHORT).show();
		}
	}

    private class VenueFavoriteObserver implements FavoriteObserverView {
        private final CheckBox checkbox;

        private VenueFavoriteObserver(CheckBox checkbox) {
            this.checkbox = checkbox;
        }

        @Override
        public void onFavoriteAdded(Favorite favorite) {

            Props props = new Props();
            props.put("Venue Name", favorite.getName());
            Analytics.track("Favorite Venue Star Tap", props);
            checkbox.setChecked(true);
        }

        @Override
        public void onFavoriteRemoved(Favorite favorite) {
            Props props = new Props();
            props.put("Venue Name", favorite.getName());
            Analytics.track("Unfavorite Venue Star Tap", props);
            checkbox.setChecked(false);
        }
    }
}
