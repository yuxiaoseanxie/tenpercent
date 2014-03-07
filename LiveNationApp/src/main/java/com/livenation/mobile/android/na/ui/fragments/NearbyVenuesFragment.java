/*
 * 
 * @author Charlie Chilton 2014/02/24
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
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.na.presenters.views.VenuesView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;


public class NearbyVenuesFragment extends LiveNationFragment implements VenuesView, LocationHelper.LocationCallback {
	private StickyListHeadersListView listView;
	private EventVenueAdapter adapter;
	private Double lat;
    private Double lng;
	private static SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.DATE_TIME_Z_FORMAT, Locale.US);
    private static float METERS_IN_A_MILE = 1609.34f;

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
		listView.setDivider(null);
        listView.setAreHeadersSticky(false);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
        getLocationHelper().getLocation(getActivity(), NearbyVenuesFragment.this);
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

    @Override
    public void onLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        init();
    }

    @Override
    public void onLocationFailure(int failureCode) {
        Toast.makeText(getActivity(), "Failed to get location", Toast.LENGTH_SHORT).show();
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

			view.setOnClickListener(new OnShowClick(event));

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
			
			TextView title = holder.getVenueTitle();
			Event event = items.get(position);
			title.setText(event.getVenue().getName());

            CheckBox checkBox = holder.getFavorite();

            TextView location = holder.getLocation();
            location.setText(event.getVenue().getAddress().getSmallFriendlyAddress(false));
            TextView distance = holder.getDistance();

            if (null != lat && null != lng) {
                distance.setVisibility(View.VISIBLE);
                float[] result = new float[1];
                double venueLat = Double.valueOf(event.getVenue().getLat());
                double venueLng = Double.valueOf(event.getVenue().getLng());
                Location.distanceBetween(lat, lng, venueLat, venueLng, result);
                float miles = result[0] / METERS_IN_A_MILE;
                distance.setText(String.format("%.1f mi", miles));
            } else {
                distance.setVisibility(View.GONE);
            }
            holder.getFavorite().setChecked(false);
            holder.setFavoriteControl(event.getVenue(), getFavoritesPresenter());

            holder.getVenueTextContainer().setOnClickListener(new OnVenueClick(event.getVenue()));

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
			private final TextView venueTitle;
            private final TextView venueLocation;
            private final CheckBox venueFavorite;
            private final TextView venueDistance;
            private final ViewGroup venueTextContainer;

			private FavoriteListener favoriteListener;

			public ViewHeaderHolder(View view) {
				this.venueTitle = (TextView) view.findViewById(R.id.list_venue_header_title);
                this.venueLocation = (TextView) view.findViewById(R.id.list_venue_header_location);
                this.venueFavorite = (CheckBox) view.findViewById(R.id.list_venue_header_checkbox);
                this.venueDistance = (TextView) view.findViewById(R.id.list_venue_header_distance);
                this.venueTextContainer = (ViewGroup) view.findViewById(R.id.list_venue_header_text_container);
			}
			
			public TextView getVenueTitle() {
				return venueTitle;
			}

            public TextView getLocation() { return venueLocation; }

            public CheckBox getFavorite() { return venueFavorite; }

            public TextView getDistance() { return venueDistance; }

            public ViewGroup getVenueTextContainer() { return venueTextContainer; }

            private void setFavoriteControl(Venue venue, FavoritesPresenter presenter) {
                if (null != favoriteListener) {
                    presenter.getObserverPresenter().cancel(favoriteListener);
                }
                favoriteListener = new FavoriteListener();
                Bundle args = presenter.getObserverPresenter().getBundleArgs(Favorite.FAVORITE_VENUE, venue.getNumericId());
                presenter.getObserverPresenter().initialize(getActivity(), args, favoriteListener);
                getFavorite().setOnClickListener(new OnFavoriteClickListener.OnVenueFavoriteClick(venue, presenter, getActivity()));
            }

            private class FavoriteListener implements FavoriteObserverView {
                @Override
                public void onFavoriteAdded(Favorite favorite) {
                    getFavorite().setChecked(true);
                }

                @Override
                public void onFavoriteRemoved(Favorite favorite) {
                    getFavorite().setChecked(false);
                }
            }

        }
	}


    private class OnShowClick implements View.OnClickListener {
        private final Event event;

        private OnShowClick(Event event) {
            this.event = event;

        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), ShowActivity.class);

            Bundle args = SingleEventPresenter.getAruguments(event.getId());
            SingleEventPresenter.embedResult(args, event);

            intent.putExtras(args);
            getActivity().startActivity(intent);
        }
    }

    private class OnVenueClick implements View.OnClickListener {
        private final Venue venue;

        private OnVenueClick(Venue venue) {
            this.venue = venue;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), VenueActivity.class);

            Bundle args = SingleVenuePresenter.getAruguments(venue.getId());
            SingleVenuePresenter.embedResult(args, venue);

            intent.putExtras(args);
            getActivity().startActivity(intent);
        }
    }
}