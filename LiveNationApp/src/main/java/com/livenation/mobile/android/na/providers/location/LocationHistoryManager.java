package com.livenation.mobile.android.na.providers.location;

import android.content.Context;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cchilton on 4/22/14.
 */
public class LocationHistoryManager {
    private static final String PREFS_PREVIOUS_LOCATIONS = "previous_locations";
    private static final String PREVIOUS_LOCATION_KEY = "previous_location";
    private final Context context;
    private LocationHistoryList locations;
    private ObjectMapper mapper = new ObjectMapper();

    public LocationHistoryManager(Context context) {
        this.context = context;
        restoreState(context);
    }

    public void addLocationHistory(City city, Context context) {
        locations.add(city);
        saveState(context);
    }

    public List<City> getLocationHistory() {
        return locations.items();
    }

    private void saveState(Context context) {
        try {
            String json = mapper.writeValueAsString(locations.items());
            PreferencePersistence prefs = new PreferencePersistence(PREFS_PREVIOUS_LOCATIONS, context);
            prefs.write(PREVIOUS_LOCATION_KEY, json);
        } catch (Exception ignored) {
        }
    }

    private void restoreState(Context context) {
        try {
            PreferencePersistence prefs = new PreferencePersistence(PREFS_PREVIOUS_LOCATIONS, context);
            String json = prefs.readString(PREVIOUS_LOCATION_KEY);
            JavaType type = mapper.getTypeFactory().
                    constructCollectionType(List.class, City.class);
            List<City> cities = mapper.readValue(json, type);
            locations = new LocationHistoryList(cities);
        } catch (Exception e) {
            List<City> items = new ArrayList<City>();
            locations = new LocationHistoryList(items);
        }
    }

    private class LocationHistoryList {
        private static final int MAX_SIZE = 5;
        private List<City> items;

        private LocationHistoryList(List<City> items) {
            this.items = items;
            trimToSize(MAX_SIZE);
        }

        public void add(City city) {
            removeExisting(city);
            items.add(0, city);
            trimToSize(MAX_SIZE);
        }

        public List<City> items() {
            return items;
        }

        /**
         * Remove any existing instances of this location from the list
         *
         * @param target the location to match
         */
        private void removeExisting(City target) {
            Iterator<City> iterator = items.iterator();
            while (iterator.hasNext()) {
                City city = iterator.next();
                if (city.idEquals(target)) {
                    iterator.remove();
                }
            }
        }

        /**
         * Trim the list to a pre-defined size
         *
         * @param maxSize size to trim to.
         */

        private void trimToSize(int maxSize) {
            if (items.size() > maxSize) {
                items = items.subList(0, maxSize);
            }
        }
    }
}
