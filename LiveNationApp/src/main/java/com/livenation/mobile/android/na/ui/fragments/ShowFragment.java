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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apsalar.sdk.Apsalar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LibraryErrorTracker;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.helpers.InstalledAppConfig;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.OrderConfirmationActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment;
import com.livenation.mobile.android.na.ui.dialogs.CommerceUnavailableDialogFragment;
import com.livenation.mobile.android.na.ui.dialogs.TicketOfferingsDialogFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener.OnVenueFavoriteClick;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.na.ui.views.ShowVenueView;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.na.utils.EventUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.utils.OnThrottledClickListener;
import com.segment.android.models.Props;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class ShowFragment extends LiveNationFragment implements SingleEventView, LiveNationMapFragment.MapReadyListener {
    private static final String CALENDAR_DATE_FORMAT = "EEE MMM d'.' yyyy 'at' h:mm aa";
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};
    private final String MAP_FRAGMENT_TAG = "maps";
    private TextView artistTitle;
    private TextView calendarText;
    private ViewGroup calendarContainer;
    private ViewGroup lineupContainer;
    private TransitioningImageView artistImage;
    private ShowVenueView venueDetails;
    private Button findTicketsOptions;
    private Button findTickets;
    private GoogleMap map;
    private LiveNationMapFragment mapFragment;
    private LatLng mapLocationCache = null;
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_show, container,
                false);
        artistTitle = (TextView) result.findViewById(R.id.fragment_show_artist_title);
        lineupContainer = (ViewGroup) result.findViewById(R.id.fragment_show_artist_lineup_container);
        artistImage = (TransitioningImageView) result.findViewById(R.id.fragment_show_image);
        venueDetails = (ShowVenueView) result.findViewById(R.id.fragment_show_venue_details);
        calendarText = (TextView) result.findViewById(R.id.sub_show_calendar_text);
        calendarContainer = (ViewGroup) result.findViewById(R.id.sub_show_calendar_container);
        findTicketsOptions = (Button) result.findViewById(R.id.fragment_show_ticketbar_options);
        findTickets = (Button) result.findViewById(R.id.fragment_show_ticketbar_find);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapFragment = (LiveNationMapFragment) getChildFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new LiveNationMapFragment();
            addFragment(R.id.fragment_show_map_container, mapFragment, MAP_FRAGMENT_TAG);
        }
        mapFragment.setMapReadyListener(this);
    }

    @Override
    public void setEvent(Event event) {
        this.event = event;

        artistTitle.setText(event.getName());

        SimpleDateFormat dateFormatter = new SimpleDateFormat(CALENDAR_DATE_FORMAT);
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        dateFormatter.setTimeZone(timeZone);
        String calendarValue = dateFormatter.format(event.getLocalStartTime());
        calendarText.setText(calendarValue);
        OnCalendarViewClick onCalendarViewClick = new OnCalendarViewClick(event);
        calendarContainer.setOnClickListener(onCalendarViewClick);


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

            OnVenueFavoriteClick onVenueFavoriteClick = new OnVenueFavoriteClick(venue, AnalyticsCategory.SDP);
            venueDetails.getFavorite().setOnClickListener(onVenueFavoriteClick);
            venueDetails.getFavorite().bindToFavorite(Favorite.fromVenue(venue), AnalyticsCategory.SDP);

            //Longitude and Latitude can be null
            if (venue.getLat() != null && venue.getLng() != null) {
                double lat = Double.valueOf(venue.getLat());
                double lng = Double.valueOf(venue.getLng());
                setMapLocation(lat, lng);
            } else {
                new LibraryErrorTracker().track("This venue does not have a longitute and/or a latitude. Venue Id:" + venue.getId(), null);
            }
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

        artistImage.setDefaultImage(DefaultImageHelper.computeDefaultDpDrawableId(getActivity(), event.getNumericId()));

        String imageUrl = null;
        //TODO: Refactor this when Activity -> Fragment data lifecycle gets implemented
        lineupContainer.removeAllViews();
        for (Artist lineup : event.getLineup()) {
            LineupView view = new LineupView(getActivity());
            view.getTitle().setText(lineup.getName());

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lineupContainer.addView(view, layoutParams);

            view.bindToFavoriteArtist(lineup);

            view.setOnClickListener(new OnLineupViewClick(lineup, event));

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
            artistImage.setImageUrl(imageUrl, getImageLoader(), TransitioningImageView.LoadAnimation.FADE_ZOOM);
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
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    venueDetails.performClick();
                }
            });
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    venueDetails.performClick();
                    return true;
                }
            });
        } else {
            //TODO: Possible No Google play services installed
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

    private InstalledAppConfig getInstalledAppConfig() {
        return LiveNationApplication.get().getInstalledAppConfig();
    }


    //region Find Tickets

    protected void showTicketOffering(TicketOffering offering) {
        String buyLink = offering.getPurchaseUrl();
        if (Ticketing.isTicketmasterUrl(buyLink)) {
            if (getInstalledAppConfig().isCommerceAvailable()) {
                Intent confirmIntent = new Intent(getActivity(), OrderConfirmationActivity.class);
                confirmIntent.putExtra(OrderConfirmationActivity.EXTRA_EVENT, event);
                Ticketing.showFindTicketsActivityForUrl(getActivity(), confirmIntent, buyLink);
            } else {
                CommerceUnavailableDialogFragment dialogFragment = new CommerceUnavailableDialogFragment();
                dialogFragment.show(getFragmentManager(), CommerceUnavailableDialogFragment.TAG);
            }
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
            Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.OPTIONS_BUTTON_TAP, AnalyticsCategory.SDP, props);

            TicketOfferingsDialogFragment dialogFragment = TicketOfferingsDialogFragment.newInstance(event);
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
            List<TicketOffering> offerings = event.getTicketOfferings();
            if (offerings.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.no_ticket_offerings,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            TicketOffering ticketOffering = offerings.get(0);

            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put(com.livenation.mobile.android.ticketing.analytics.AnalyticConstants.PROP_IS_SDP_SHOWN, EventUtils.isSDPAvoidable(event, v.getContext()));
            LiveNationAnalytics.track(AnalyticConstants.FIND_TICKETS_TAP, AnalyticsCategory.SDP, props);
            LiveNationApplication.getAccessTokenProvider().getAccessToken(new BasicApiCallback<AccessToken>() {
                @Override
                public void onResponse(AccessToken response) {
                    Apsalar.event(AnalyticConstants.APSALAR_FIND_TICKET_TAP, AnalyticConstants.TOKEN, response.getToken(), AnalyticConstants.TOKEN_TYPE, response.getType());
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    Apsalar.event(AnalyticConstants.APSALAR_FIND_TICKET_TAP);
                }
            });
            showTicketOffering(ticketOffering);
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

            Bundle args = VenueActivity.getArguments(venue);
            intent.putExtras(args);

            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
            LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, AnalyticsCategory.SDP, props);

            startActivity(intent);
        }
    }

    private class OnLineupViewClick implements View.OnClickListener {
        private Artist lineupArtist;
        private Event event;

        public OnLineupViewClick(Artist lineupArtist, Event event) {
            this.lineupArtist = lineupArtist;
            this.event = event;
        }

        @Override
        public void onClick(View view) {
            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put(AnalyticConstants.ARTIST_NAME, lineupArtist.getName());
            props.put(AnalyticConstants.ARTIST_ID, lineupArtist.getId());
            LiveNationAnalytics.track(AnalyticConstants.ARTIST_CELL_TAP, AnalyticsCategory.SDP, props);

            Intent intent = new Intent(getActivity(), ArtistActivity.class);

            Bundle args = ArtistActivity.getArguments(lineupArtist);
            intent.putExtras(args);

            startActivity(intent);
        }
    }

    private class OnCalendarViewClick extends OnThrottledClickListener {
        private CalendarDialogFragment dialogFragment;
        private Event event;

        public OnCalendarViewClick(Event event) {
            this.event = event;
            this.dialogFragment = CalendarDialogFragment.newInstance(event);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            if (dialogFragment.isAdded()) return;
            Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.CALENDAR_ROW_TAP, AnalyticsCategory.SDP, props);

            dialogFragment.show(getFragmentManager(), "CalendarDialogFragment");
        }
    }
}
