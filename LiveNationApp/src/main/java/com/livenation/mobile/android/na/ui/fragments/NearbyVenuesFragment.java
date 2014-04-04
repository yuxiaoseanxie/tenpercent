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

import io.segment.android.Analytics;
import io.segment.android.models.Props;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
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


public class NearbyVenuesFragment extends LiveNationFragment implements ApiServiceBinder {
	private StickyListHeadersListView listView;
	private EventVenueAdapter adapter;
	private Double lat;
    private Double lng;
    private ScrollPager pager;

	private static SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);
    private static float METERS_IN_A_MILE = 1609.34f;
    private static final String START_TIME_FORMAT = "h:mm a zzz";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EventVenueAdapter(getActivity());
        pager = new ScrollPager(adapter);

        LiveNationApplication.get().getApiHelper().persistentBindApi(this);

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
        pager.connectListView(listView);

        return view;
	}

    @Override
    public void onStop() {
        super.onStop();
        deinit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
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
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.lat = apiService.getApiConfig().getLat();
        this.lng = apiService.getApiConfig().getLng();
        init();
    }

    private void init() {
        pager.reset();
        pager.load();
	}

    private void deinit() {
        pager.stop();
    }

	private class EventVenueAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter {
	    private LayoutInflater inflater;

		public EventVenueAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1, new ArrayList<Event>());
            inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View view = null;
			
			if (null == convertView) {
				view = inflater.inflate(R.layout.list_show_nearby_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}
			
			Event event = getItem(position);
			holder.getTitle().setText(event.getName());

			//TODO: Move date parsing to Data Model Entity helper. This is ugly 
			try {
				Date date = sdf.parse(event.getLocalStartTime());
                String startTime = DateFormat.format(START_TIME_FORMAT, date).toString();

                holder.getStartTime().setText(startTime);
                holder.getDate().setDate(date);
			} catch (ParseException e) {
				//wtf'y f.
				e.printStackTrace();
			}

			view.setOnClickListener(new OnShowClick(event, position));

			return view;
		}

		@Override
		public View getHeaderView(int position, View convertView,
				ViewGroup parent) {
			View view = null;
			ViewHeaderHolder holder = null;
			if (null == convertView) {
				view = inflater.inflate(R.layout.list_venue_nearby_header, null);
				holder = new ViewHeaderHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHeaderHolder) view.getTag();
			}
			
			TextView title = holder.getVenueTitle();
			Event event = getItem(position);
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

            holder.getVenueTextContainer().setOnClickListener(new OnVenueClick(event.getVenue(), position));

			return view;	
		}

		@Override
		public long getHeaderId(int position) {
			Event event = getItem(position);
			long venueId = event.getVenue().getNumericId();
			return venueId;
		}
		
		private class ViewHolder {
			private final TextView title;
			private final TextView startTime;
			private final VerticalDate date;

			public ViewHolder(View view) {
				this.title = (TextView) view.findViewById(R.id.list_nearby_show_title);
				this.startTime = (TextView) view.findViewById(R.id.list_nearby_show_time);
				this.date = (VerticalDate) view.findViewById(R.id.list_nearby_show_date);
			}
			
			public TextView getTitle() {
				return title;
			}

            public TextView getStartTime() {
                return startTime;
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
                    Analytics.track("Favorite Venue Star Tap");
                    getFavorite().setChecked(true);
                }

                @Override
                public void onFavoriteRemoved(Favorite favorite) {
                    Analytics.track("Unfavorite Venue Star Tap");
                    getFavorite().setChecked(false);
                }
            }

        }
	}

    private class ScrollPager extends BaseDecoratedScrollPager<Event> {

        /*
        We manually track the paging offset value as the adapter's itemCount() value will be incorrect
        for this purpose (since the size of venue events (adapter) != size of venues (api)
         */
        private int offset = 0;

        private ScrollPager(ArrayAdapter<Event> adapter) {
            super(10, adapter);
        }

        @Override
        public void reset() {
            super.reset();
            offset = 0;
        }

        @Override
        public FetchRequest<Event> getFetchRequest(int offset, int limit, FetchResultHandler callback) {
            Bundle args = getNearbyVenuesPresenter().getArgs(offset, limit);
            FetchRequest request = new VenuesFetchRequest(offset, limit, callback);
            return request;
        }

        @Override
        protected int getOffset() {
            return offset;
        }

        @Override
        public void stop() {
            for (FetchLoader fetchLoader : getFetchLoaders()) {
                fetchLoader.cancel();
            }
        }

        private class VenuesFetchRequest extends FetchRequest<Event> implements VenuesView {

            private VenuesFetchRequest(int offset, int limit, FetchResultHandler<Event> fetchResultHandler) {
                super(offset, limit, fetchResultHandler);
            }

            @Override
            public void run() {
                Bundle args = getNearbyVenuesPresenter().getArgs(getOffset(), getLimit());
                getNearbyVenuesPresenter().initialize(getActivity(), args, this);
            }

            @Override
            public void setVenues(List<Venue> venues) {
                ScrollPager.this.offset += venues.size();
                List<Event> transformed = DataModelHelper.flattenVenueEvents(venues);
                getFetchResultHandler().deliverResult(transformed);
            }

            @Override
            public void cancel() {
                getNearbyVenuesPresenter().cancel(this);
            }
        }
    }

    private class OnShowClick implements View.OnClickListener {
        private final Event event;
        private final int position;

        private OnShowClick(Event event, int position) {
            this.event = event;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), ShowActivity.class);

            Bundle args = SingleEventPresenter.getAruguments(event.getId());
            SingleEventPresenter.embedResult(args, event);

            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put("Cell Position", position);
            Analytics.track("Event Cell Tap");

            intent.putExtras(args);
            getActivity().startActivity(intent);
        }
    }

    private class OnVenueClick implements View.OnClickListener {
        private final Venue venue;
        private final int position;

        private OnVenueClick(Venue venue, int position) {
            this.venue = venue;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), VenueActivity.class);

            Bundle args = SingleVenuePresenter.getAruguments(venue.getId());
            SingleVenuePresenter.embedResult(args, venue);

            //Analytics
            Props props = new Props();
            props.put("Venue Name", venue.getName());
            props.put("Cell Position", position);
            Analytics.track("Venue Cell Tap");

            intent.putExtras(args);
            getActivity().startActivity(intent);
        }
    }
}
