package com.livenation.mobile.android.na.ui.fragments;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class VenueMapFragment extends Fragment implements LiveNationMapFragment.MapReadyListener {
    private static final String VENUE = "com.livenation.mobile.android.na.ui.fragments.VenueMapFragment.VENUE";
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private TextView venueTitle;
    private FavoriteCheckBox favoriteCheckBox;
    private LiveNationMapFragment mapFragment;
    private GoogleMap map;
    private Venue venue;

    public static VenueMapFragment newInstance(Venue venue) {
        VenueMapFragment venueMapFragment = new VenueMapFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENUE, venue);
        venueMapFragment.setArguments(bundle);
        return venueMapFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venue_header, container, false);

        venue = (Venue) getArguments().getSerializable(VENUE);

        venueTitle = (TextView) view.findViewById(R.id.fragment_venue_title);
        favoriteCheckBox = (FavoriteCheckBox) view.findViewById(R.id.fragment_venue_favorite_checkbox);

        mapFragment = new LiveNationMapFragment();
        mapFragment.setMapReadyListener(this);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_venue_map_container, mapFragment).commit();

        venueTitle.setText(venue.getName());
        favoriteCheckBox.bindToFavorite(Favorite.fromVenue(venue), AnalyticsCategory.VDP);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        if (map != null) {
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            setMapLocation(Double.valueOf(venue.getLat()), Double.valueOf(venue.getLng()));
        } else {
            //TODO: Possible No Google play services installed
        }
    }

    private void setMapLocation(double lat, double lng) {
        if (null == map) return;

        MarkerOptions marker = new MarkerOptions();
        LatLng latLng = new LatLng(lat, lng);
        marker.position(latLng);

        map.clear();
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM));
    }

}
