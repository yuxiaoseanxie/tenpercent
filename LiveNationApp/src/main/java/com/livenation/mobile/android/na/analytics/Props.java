package com.livenation.mobile.android.na.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by cchilton on 1/12/15.
 */
public class Props {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Set<String> keySet() {
        return data.keySet();
    }
}
