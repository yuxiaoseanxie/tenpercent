package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
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
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LocationProvider;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationFragment extends LiveNationFragment implements ListView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {
    private Switch autoLocationSwitch;
    private LocationAdapter adapter;

    private TextView currentLocationText;

    private City actualLocation;
    private City configuredLocation;

    private LocationManager locationManager;

    private String UNKNOWN_LOCATION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        locationManager = LiveNationApplication.get().getLocationManager();

        //would be final, but need a context to set it, so CAPS are preserved...
        UNKNOWN_LOCATION = getActivity().getString(R.string.location_unknown);

        List<City> previousLocations = new ArrayList<City>(locationManager.getLocationHistory());

        if (previousLocations.size() > 0) {
            //Item 0 on the location history list will be our current location, chop it off.
            previousLocations = previousLocations.subList(1, previousLocations.size());
        }

        this.adapter = new LocationAdapter(getActivity().getApplicationContext(), 0, previousLocations);

        //get our actual location, so that we can show valid "distance from you in miles" values.
        final Context appContext = getActivity().getApplicationContext();
        locationManager.getSystemLocationProvider().getLocation(getActivity(), new LocationProvider.LocationCallback() {
            @Override
            public void onLocation(final double lat, final double lng) {
                //we now have our actual location, lets get a name for it.
                locationManager.reverseGeocodeCity(lat, lng, appContext, new LocationManager.GetCityCallback() {
                    @Override
                    public void onGetCity(City city) {
                        actualLocation = city;
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onGetCityFailure() {
                        //reverse geocode failed, make up an "unknown" label name
                        actualLocation = new City(UNKNOWN_LOCATION, lat, lng);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onLocationFailure(int failureCode) {
                //todo: need comps: bug user with modal dialog screaming "WHERE ARE YOU!?!"
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        currentLocationText = (TextView) view.findViewById(R.id.fragment_location_current_text);

        listView.setOnItemClickListener(this);

        autoLocationSwitch = (Switch) view.findViewById(R.id.fragment_location_current_location);

        int mode = locationManager.getLocationMode(getActivity());
        switch (mode) {
            case LocationManager.MODE_SYSTEM:
                autoLocationSwitch.setChecked(true);
                break;
            case LocationManager.MODE_USER:
                autoLocationSwitch.setChecked(false);
                break;

        }
        autoLocationSwitch.setOnCheckedChangeListener(this);
        //manually trip the onCheckedChanged listener for the UI, as the switch above wont trip it if
        //isChecked() == false and then you setChecked(false);
        onCheckedChanged(autoLocationSwitch, autoLocationSwitch.isChecked());

        final Context appContext = getActivity().getApplicationContext();

        //retrieve a city for where the API is currently configured
        locationManager.getLocation(getActivity(), new LocationProvider.LocationCallback() {
            @Override
            public void onLocation(final double lat, final double lng) {
                locationManager.reverseGeocodeCity(lat, lng, appContext, new LocationManager.GetCityCallback() {
                    @Override
                    public void onGetCity(City apiLocation) {
                        showActiveLocation(apiLocation);
                    }

                    @Override
                    public void onGetCityFailure() {
                        City apiLocation = new City(UNKNOWN_LOCATION, lat, lng);
                        showActiveLocation(apiLocation);
                    }
                });
            }

            @Override
            public void onLocationFailure(int failureCode) {

            }
        });

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        City activeLocation = null;

        if (isChecked) {
            activeLocation = actualLocation;
        } else {
            if (null == configuredLocation) {
                //no initial manual location!
                if (null != actualLocation) {
                    //set initial manual location to our actual location
                    setConfiguredLocation(actualLocation);
                }
            }
            activeLocation = configuredLocation;
        }

        if (null != activeLocation) {
            showActiveLocation(activeLocation);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City city = adapter.getItem(position);
        setConfiguredLocation(city);
    }

    @Override
    public void onDestroyView() {
        //persist the location mode changes to preferences
        if (isLocationAutomatic()) {
            //automatic location
            locationManager.setLocationMode(LocationManager.MODE_SYSTEM, getActivity());
        } else {
            //manual location
            if (configuredLocation != null) {
                //manual location set, and we have a manual location specified.
                locationManager.setLocationMode(LocationManager.MODE_USER, getActivity());
                locationManager.setUserLocation(configuredLocation.getLat(), configuredLocation.getLng(), getActivity());
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onPause();
        //build a new API on exiting
        LiveNationApplication.get().getConfigManager().buildApi();
    }

    /**
     * set the configuredLocation pointer to some city.
     * The value of this member field will be used as the manual location when the fragment/activity
     * finishes.
     *
     * @param city The location to track as the user's manual location
     */
    public void setConfiguredLocation(City city) {
        if (null == city) throw new NullPointerException();
        if (isLocationAutomatic()) {
            //if we're going to set a manual/configured city, then force auto location mode to off
            //...Is this smelly?...
            autoLocationSwitch.setChecked(false);
        }
        configuredLocation = city;
        showActiveLocation(city);
    }

    private void showActiveLocation(City city) {
        currentLocationText.setText(city.getName());
    }

    private boolean isLocationAutomatic() {
        return autoLocationSwitch.isChecked();
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
                float miles = result[0] / Constants.METERS_IN_A_MILE;
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
