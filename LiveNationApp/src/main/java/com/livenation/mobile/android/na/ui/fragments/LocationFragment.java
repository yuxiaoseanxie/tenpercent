package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LocationProvider;
import com.livenation.mobile.android.na.helpers.PreferencePersistence;
import com.livenation.mobile.android.na.helpers.UserLocationProvider;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by cchilton on 3/12/14.
 */
public class LocationFragment extends LiveNationFragment implements CompoundButton.OnCheckedChangeListener {
    private Switch currentLocation;
    private LocationAdapter adapter;
    private List<City> previousLocations = new Stack<City>();
    private static final int MAX_CITY_ITEMS = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferencePersistence prefs = new PreferencePersistence("test");
        String in = prefs.read("cities", getActivity());
        List<City> cities = deserialize(in);
        if (cities != null) {
            previousLocations.clear();
            previousLocations.addAll(cities);
        }
        this.adapter = new LocationAdapter(getActivity().getApplicationContext(), 0, previousLocations);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        currentLocation = (Switch) view.findViewById(R.id.fragment_location_current_location);
        currentLocation.setOnCheckedChangeListener(this);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    public void setCity(City city) {
        Logger.log("SetCity", "SetCity");
        adapter.insert(city, 0);
        int difference = adapter.getCount() - MAX_CITY_ITEMS;
        if (difference > 0) {
            for (int i = 0; i < difference; i++) {
                City last = adapter.getItem(adapter.getCount() - 1);
                adapter.remove(last);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String serialize(List<City> cities) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String out = mapper.writeValueAsString(cities);
            return out;
        } catch (Exception failed) {}
        return null;
    }

    private List<City> deserialize(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JavaType type = mapper.getTypeFactory().
                    constructCollectionType(List.class, City.class);
            List<City> in = mapper.readValue(json, type);
            return in;
        } catch (Exception failed) {}
        return null;
    }

    @Override
    public void onDestroy() {
        Logger.log("OnPause", "Pause");
        super.onPause();
        String out = serialize(previousLocations);
        PreferencePersistence prefs = new PreferencePersistence("test");
        prefs.write("cities", out, getActivity());
    }


    private class LocationAdapter extends ArrayAdapter<City> {
        private final LayoutInflater inflater;

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
            holder.getText2().setText("blah blah blah");

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
