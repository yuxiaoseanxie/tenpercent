package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LocationProvider;
import com.livenation.mobile.android.na.helpers.UserLocationProvider;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationFragment extends LiveNationFragment implements LiveNationMapFragment.MapReadyListener, GoogleMap.OnMapClickListener {
    private LiveNationMapFragment mapFragment;
    private GoogleMap map;
    private ActionMode actionMode;
    private LatLng locationCache;
    private FrameLayout mapContainer;
    private FrameLayout mapContainerForeground;
    private View overlayTapToChange;
    private View overlayAutomaticLocation;


    private static final float DEFAULT_MAP_ZOOM = 8f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapFragment = new LiveNationMapFragment();
        mapFragment.setMapReadyListener(this);

        addFragment(R.id.fragment_location_map_container, mapFragment, "location_map");
        getLocationManager().getUserLocationProvider().getLocation(getActivity(), initialUserLocationCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        mapContainer = (FrameLayout) view.findViewById(R.id.fragment_location_map_container);
        mapContainerForeground = (FrameLayout) view.findViewById(R.id.fragment_location_map_container_foreground);

        RadioGroup locationGroup = (RadioGroup) view.findViewById(R.id.fragment_location_radio_group);
        locationGroup.setOnCheckedChangeListener(radioListener);

        overlayAutomaticLocation = inflater.inflate(R.layout.view_location_overlay_automatic, null);
        overlayTapToChange = inflater.inflate(R.layout.view_location_overlay_manual, null);
        overlayTapToChange.setOnClickListener(onTapToChangeListener);

        RadioButton manualRadio = (RadioButton) view.findViewById(R.id.fragment_location_manual_radio);
        RadioButton autoRadio = (RadioButton) view.findViewById(R.id.fragment_location_automatic_radio);
        switch (getLocationManager().getLocationMode(getActivity())) {
            case LocationManager.MODE_USER:
                manualRadio.setChecked(true);
                break;
            default:
                autoRadio.setChecked(true);
                break;
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnMapClickListener(this);
        setMapEnabled(false);
        if (null != locationCache) {
            setMapMarker(locationCache.latitude, locationCache.longitude, true);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (null == actionMode) return;
        setMapMarker(latLng.latitude, latLng.longitude, false);
        locationCache = latLng;
    }

    @Override
    public void onStop() {
        super.onStop();
        LiveNationApplication.get().getApiHelper().buildDefaultApi();
    }

    private void setMapMarker(double lat, double lng, boolean center) {
       setMapMarker(lat, lng, center, 0);
    }

    private void setMapMarker(double lat, double lng, boolean center, int zoomModifier) {
        LatLng latLng = new LatLng(lat, lng);

        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);

        map.clear();
        map.addMarker(marker);

        if (center) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM + zoomModifier);
            map.animateCamera(update);
        }
    }

    private void setMapEnabled(boolean enabled) {
        map.getUiSettings().setMyLocationButtonEnabled(enabled);
        map.getUiSettings().setAllGesturesEnabled(enabled);
        map.getUiSettings().setZoomControlsEnabled(enabled);
        map.getUiSettings().setMyLocationButtonEnabled(enabled);
        map.getUiSettings().setZoomGesturesEnabled(enabled);
    }

    private View.OnClickListener onTapToChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        if (null == actionMode) {
            actionMode = getActivity().startActionMode(actionModeCallback);
        };
        }
    };

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            setMapEnabled(true);

            setMapMarker(locationCache.latitude, locationCache.longitude, true, -2);

            MenuInflater inflater = actionMode.getMenuInflater();

            inflater.inflate(R.menu.location_set_menu, menu);

            mapContainerForeground.setVisibility(View.INVISIBLE);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            LocationFragment.this.actionMode = null;
            setMapEnabled(false);

            if (null != locationCache) {
                getLocationManager().getUserLocationProvider().setLocation(locationCache.latitude, locationCache.longitude, getActivity());
                setMapMarker(locationCache.latitude, locationCache.longitude, true);
            }

            mapContainerForeground.setVisibility(View.VISIBLE);
        }
    };

    private void centerMapWithConfiguredLocation() {
        getLocationManager().getLocation(getActivity(), configuredLocationCallback);
    }

    private RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.fragment_location_automatic_radio:
                    getLocationManager().setLocationMode(LocationManager.MODE_SYSTEM, getActivity());
                    if (null != actionMode) {
                        actionMode.finish();
                    }
                    mapContainerForeground.removeAllViews();
                    mapContainerForeground.addView(overlayAutomaticLocation);
                    break;
                case R.id.fragment_location_manual_radio:
                    getLocationManager().setLocationMode(LocationManager.MODE_USER, getActivity());
                    mapContainerForeground.removeAllViews();
                    mapContainerForeground.addView(overlayTapToChange);
                    break;
                default:
                    throw new IllegalArgumentException();

            }
            centerMapWithConfiguredLocation();

        }
    };

    private LocationProvider.LocationCallback configuredLocationCallback = new LocationProvider.LocationCallback() {
        @Override
        public void onLocation(double lat, double lng) {
            locationCache = new LatLng(lat, lng);
            if (null != map) {
                setMapMarker(lat, lng, true);
            }
        }

        @Override
        public void onLocationFailure(int failureCode) {}
    };

    private LocationProvider.LocationCallback initialUserLocationCallback = new LocationProvider.LocationCallback() {
        @Override
        public void onLocation(double lat, double lng) {
            locationCache = new LatLng(lat, lng);
            if (null != map) {
                setMapMarker(lat, lng, true);
            }
        }

        @Override
        public void onLocationFailure(int failureCode) {
            if (failureCode == UserLocationProvider.FAILURE_NO_USER_LOCATION_SET) {
                getLocationManager().getSystemLocationProvider().getLocation(getActivity(), this);
            }
        }
    };
}