package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationFragment extends LiveNationFragment implements ListView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, LocationUpdateReceiver.LocationUpdateListener {
    private Switch autoLocationSwitch;
    private LocationAdapter adapter;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);


    private TextView currentLocationText;

    private City actualLocation;

    private City configuredLocation;

    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        locationManager = LiveNationApplication.getLocationProvider();

        List<City> previousLocations = new ArrayList<City>(locationManager.getLocationHistory());

        if (previousLocations.size() > 0) {
            //Item 0 on the location history list will be our current location, chop it off.
            previousLocations = previousLocations.subList(1, previousLocations.size());
        }

        this.adapter = new LocationAdapter(getActivity().getApplicationContext(), 0, previousLocations);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        currentLocationText = (TextView) view.findViewById(R.id.fragment_location_current_text);

        listView.setOnItemClickListener(this);

        autoLocationSwitch = (Switch) view.findViewById(R.id.fragment_location_current_location);
        autoLocationSwitch.setEnabled(false);

        int mode = locationManager.getMode();
        showActiveMode(mode);

        if (locationManager.getLocationHistory().size() > 0) {
            showActiveLocation(locationManager.getLocationHistory().get(0));
        }

        //get our actual location, so that we can show valid "distance from you in miles" values.
        locationManager.getSystemLocation(new BasicApiCallback<City>() {
            @Override
            public void onResponse(City response) {
                updateActualLocation(response);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                //Never called
            }
        });

        autoLocationSwitch.setOnCheckedChangeListener(this);
        //manually trip the onCheckedChanged listener for the UI, as the switch above wont trip it if
        //isChecked() == false and then you setChecked(false);
        onCheckedChanged(autoLocationSwitch, autoLocationSwitch.isChecked());

        //retrieve a city for where the API is currently configured
        locationManager.getLocation(new BasicApiCallback<City>() {
            @Override
            public void onResponse(City response) {
                showActiveLocation(response);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                //Never called
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(locationUpdateReceiver);
    }

    private void updateActualLocation(City city) {
        actualLocation = city;
        autoLocationSwitch.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (actualLocation == null) {
            return;
        }
        //actualLocation cannot be null because the switch is disable until we get the user location
        if (isChecked) {
            locationManager.setLocationMode(LocationManager.MODE_SYSTEM);
        } else {
            locationManager.setUserLocation(LocationManager.MODE_USER, actualLocation.getLat(), actualLocation.getLng());
            setNewLocation(actualLocation);
        }

        //Analytics
        final Props props = new Props();
        props.put(AnalyticConstants.LOCATION_CURRENT_LOCATION_USE, isChecked);
        props.put(AnalyticConstants.LOCATION_LATLONG, actualLocation.getLat() + "," + actualLocation.getLng());
        props.put(AnalyticConstants.LOCATION_NAME, actualLocation.getName());
        LiveNationAnalytics.track(AnalyticConstants.CURRENT_LOCATION_TAP, AnalyticsCategory.LOCATION, props);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City city = adapter.getItem(position);
        Props props = new Props();
        props.put(AnalyticConstants.LOCATION_NAME, city.getName());
        props.put(AnalyticConstants.LOCATION_LATLONG, city.getLat() + "," + city.getLng());
        LiveNationAnalytics.track(AnalyticConstants.PREVIOUS_LOCATION_TAP, AnalyticsCategory.LOCATION, props);

        locationManager.setUserLocation(LocationManager.MODE_USER, city.getLat(), city.getLng());
        setExistingLocation(city);
    }


    /**
     * set the configuredLocation pointer to some city.
     * The value of this member field will be used as the manual location when the fragment/activity
     * finishes.
     *
     * @param city The location to track as the user's manual location
     */
    public void setNewLocation(@NonNull City city) {
        if (null == city) throw new NullPointerException();
        locationManager.addLocationHistory(city);
        setExistingLocation(city);

    }

    private void setExistingLocation(@NonNull City city) {
        if (null == city) throw new NullPointerException();
        configuredLocation = city;
        showActiveLocation(city);
    }

    private void showActiveLocation(@NonNull City city) {
        currentLocationText.setText(city.getName());
    }

    private void showActiveMode(@NonNull int mode) {
        switch (mode) {
            case LocationManager.MODE_SYSTEM:
                autoLocationSwitch.setChecked(true);
                break;
            case LocationManager.MODE_USER:
            case LocationManager.MODE_UNKNOWN_BECAUSE_ERROR:
                autoLocationSwitch.setChecked(false);
                break;

        }
    }

    @Override
    public void onLocationUpdated(int mode, City city) {
        showActiveMode(mode);
        showActiveLocation(city);
    }

    private class LocationAdapter extends ArrayAdapter<City> {
        private final LayoutInflater inflater;
        private final String MILES_AWAY = getString(R.string.location_miles_away);

        private LocationAdapter(Context context, int resource, List<City> objects) {
            super(context, resource, objects);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (null == view) {
                view = inflater.inflate(R.layout.list_previous_location_item, parent, false);
                ViewHolder holder = new ViewHolder(view);
                view.setTag(holder);
            }

            City city = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.getText1().setText(city.getName());

            String distance = null;
            if (actualLocation != null) {
                float[] result = new float[1];
                Location.distanceBetween(actualLocation.getLat(), actualLocation.getLng(), city.getLat(), city.getLng(), result);
                int miles = (int) (result[0] / Constants.METERS_IN_A_MILE);
                distance = String.format(MILES_AWAY, miles);
            }
            holder.getText2().setText(distance);

            return view;
        }

        private class ViewHolder {
            private final TextView text1;
            private final TextView text2;

            private ViewHolder(View view) {
                this.text1 = (TextView) view.findViewById(android.R.id.text1);
                this.text2 = (TextView) view.findViewById(android.R.id.text2);
            }

            public TextView getText1() {
                return text1;
            }

            public TextView getText2() {
                return text2;
            }
        }
    }

}
