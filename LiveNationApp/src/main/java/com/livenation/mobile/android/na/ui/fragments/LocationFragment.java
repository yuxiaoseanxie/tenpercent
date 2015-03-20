package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import android.mobile.livenation.com.livenationui.receiver.LocationUpdateReceiver;
import android.mobile.livenation.com.livenationui.provider.SystemLocationAppProvider;
import android.mobile.livenation.com.livenationui.provider.location.LocationManager;
import android.mobile.livenation.com.livenationui.fragment.base.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
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
import android.widget.Toast;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationFragment extends LiveNationFragment implements ListView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, LocationUpdateReceiver.LocationUpdateListener {
    private Switch autoLocationSwitch;
    private LocationAdapter adapter;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);


    private TextView currentLocationText;

    private Double[] actualLocation;

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
        showActiveLocation();

        //get our actual location, so that we can show valid "distance from you in miles" values.
        new SystemLocationAppProvider().getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                updateActualLocation(response);
            }

            @Override
            public void onErrorResponse() {
                updateActualLocation(LocationManager.DEFAULT_LOCATION);
            }
        });

        autoLocationSwitch.setOnCheckedChangeListener(this);

        locationManager.getLocation(new BasicApiCallback<City>() {
            @Override
            public void onResponse(City response) {
                showActiveLocation();
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

    private void updateActualLocation(Double[] actualLocation) {
        this.actualLocation = actualLocation;
        autoLocationSwitch.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (actualLocation == null) {
            return;
        }

        if (isChecked) {
            locationManager.setLocationMode(LocationManager.MODE_SYSTEM);
        } else {
            locationManager.setLocationMode(LocationManager.MODE_USER);
        }

        //Analytics
        final Props props = new Props();
        props.put(AnalyticConstants.LOCATION_CURRENT_LOCATION_USE, isChecked);
        props.put(AnalyticConstants.LOCATION_LATLONG, actualLocation[0] + "," + actualLocation[1]);
        LiveNationAnalytics.track(AnalyticConstants.CURRENT_LOCATION_TAP, AnalyticsCategory.LOCATION, props);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        City city = adapter.getItem(position);
        Props props = new Props();
        props.put(AnalyticConstants.LOCATION_NAME, city.getName());
        props.put(AnalyticConstants.LOCATION_LATLONG, city.getLat() + "," + city.getLng());
        LiveNationAnalytics.track(AnalyticConstants.PREVIOUS_LOCATION_TAP, AnalyticsCategory.LOCATION, props);

        locationManager.addLocationHistory(city);
        locationManager.setLocationMode(LocationManager.MODE_USER);
    }

    private void showActiveLocation() {
        List<City> city = locationManager.getLocationHistory();
        if (city.size() > 0) {
            currentLocationText.setText(locationManager.getLocationHistory().get(0).getName());
        }
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
        showActiveLocation();

        if (mode == LocationManager.MODE_UNKNOWN_BECAUSE_ERROR) {
            Toast.makeText(LiveNationApplication.get().getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
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
                Location.distanceBetween(actualLocation[0], actualLocation[1], city.getLat(), city.getLng(), result);
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
