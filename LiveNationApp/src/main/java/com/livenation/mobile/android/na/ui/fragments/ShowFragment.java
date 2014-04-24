/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.net.Uri;
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
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener.OnVenueFavoriteClick;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.na.ui.views.ShowVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.util.List;

import io.segment.android.models.Props;

public class ShowFragment extends LiveNationFragment implements SingleEventView, LiveNationMapFragment.MapReadyListener {
    private static final String CALENDAR_DATE_FORMAT = "EEE MMM d'.' yyyy 'at' h:mm aa";
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};
    private TextView artistTitle;
    private TextView calendarText;
    private ViewGroup lineupContainer;
    private NetworkImageView artistImage;
    private ShowVenueView venueDetails;
    private Button findTicketsOptions;
    private Button findTickets;
    private GoogleMap map;
    private LiveNationMapFragment mapFragment;
    private VenueFavoriteObserver venueFavoriteObserver;
    private LatLng mapLocationCache = null;

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

        findTicketsOptions = (Button) result.findViewById(R.id.fragment_show_ticketbar_options);
        findTickets = (Button) result.findViewById(R.id.fragment_show_ticketbar_find);

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

        String calendarValue = DateFormat.format(CALENDAR_DATE_FORMAT, event.getLocalStartTime()).toString();
        calendarText.setText(calendarValue);

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

        if (event.getTicketOfferings().size() < 2)
            findTicketsOptions.setVisibility(View.GONE);
        else
            findTicketsOptions.setVisibility(View.VISIBLE);
        findTicketsOptions.setOnClickListener(new OnFindTicketsOptionsClick(event));

        OnFindTicketsClick onFindTicketsClick = new OnFindTicketsClick(event);
        findTickets.setOnClickListener(onFindTicketsClick);

        String imageUrl = null;
        //TODO: Refactor this when Activity -> Fragment data lifecycle gets implemented
        lineupContainer.removeAllViews();
        for (Artist lineup : event.getLineup()) {
            LineupView view = new LineupView(getActivity());
            view.getTitle().setText(lineup.getName());

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lineupContainer.addView(view, layoutParams);

            view.bindToFavoriteArtist(lineup, getFavoritesPresenter());

            view.setOnClickListener(new OnLineupViewClick(lineup));

            if (null == imageUrl) {
                String imageKey = lineup.getBestImageKey(IMAGE_PREFERRED_SHOW_KEYS);

                if (null != imageKey) {
                    imageUrl = lineup.getImageURL(imageKey);
                }
            }

            boolean lastItem = (event.getLineup().indexOf(lineup) == event.getLineup().size() - 1);
            if (lastItem) {
                view.getDivider().setVisibility(View.GONE);
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
            if (null != mapLocationCache) {
                setMapLocation(mapLocationCache.latitude, mapLocationCache.longitude);
            }
        } else {
            //TODO: Possible No Google play services installed
        }

    }

    ;

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
        mapLocationCache = new LatLng(lat, lng);
        if (null == map) return;

        MarkerOptions marker = new MarkerOptions();
        marker.position(mapLocationCache);

        map.clear();
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocationCache, DEFAULT_MAP_ZOOM));
    }


    //region Find Tickets

    protected void showTicketOffering(TicketOffering offering) {
        String buyLink = offering.getPurchaseUrl();
        if (Ticketing.isTicketmasterUrl(buyLink)) {
            Ticketing.showFindTicketsActivityForUrl(getActivity(), buyLink);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(buyLink)));
            Toast.makeText(getActivity(), R.string.tickets_third_party_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private class OnFindTicketsOptionsClick implements View.OnClickListener, TicketOfferingsDialogFragment.OnTicketOfferingClickedListener {
        private final Event event;

        private OnFindTicketsOptionsClick(Event event) {
            this.event = event;
        }

        @Override
        public void onClick(View view) {
            TicketOfferingsDialogFragment dialogFragment = TicketOfferingsDialogFragment.newInstance(event.getTicketOfferings());
            dialogFragment.setOnTicketOfferingClickedListener(this);
            dialogFragment.show(getFragmentManager(), "TicketOfferingsDialogFragment");
        }

        @Override
        public void onTicketOfferingClicked(TicketOffering offering) {
            showTicketOffering(offering);
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
            LiveNationAnalytics.track(AnalyticConstants.FIND_TICKETS_TAP, props);

            List<TicketOffering> offerings = event.getTicketOfferings();
            if(offerings.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.no_ticket_offerings,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showTicketOffering(offerings.get(0));
        }
    }

    //endregion


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
            LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, props);

            startActivity(intent);
        }
    }

    private class OnLineupViewClick implements View.OnClickListener {
        private Artist lineupArtist;

        public OnLineupViewClick(Artist lineupArtist) {
            this.lineupArtist = lineupArtist;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), ArtistActivity.class);

            Bundle args = SingleArtistPresenter.getAruguments(lineupArtist.getId());
            SingleArtistPresenter.embedResult(args, lineupArtist);
            intent.putExtras(args);

            startActivity(intent);
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
            LiveNationAnalytics.track(AnalyticConstants.FAVORITE_VENUE_STAR_TAP, props);
            checkbox.setChecked(true);
        }

        @Override
        public void onFavoriteRemoved(Favorite favorite) {
            Props props = new Props();
            props.put("Venue Name", favorite.getName());
            LiveNationAnalytics.track(AnalyticConstants.UNFAVORITE_VENUE_STAR_TAP, props);
            checkbox.setChecked(false);
        }
    }
}
