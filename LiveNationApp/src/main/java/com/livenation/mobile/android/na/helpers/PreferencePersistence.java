package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

public class PreferencePersistence implements PersistenceProvider<Object> {
	protected final String name;
	
	public PreferencePersistence(String name) {
		this.name = name;
	}
	
	@Override
	public void write(String key, Object value, Context context) {
		SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        if (value != null && value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value != null && value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value != null && value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value != null && value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value != null && value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value != null && value instanceof Set) {
            Set valueSet = (Set) value;
            if (!valueSet.isEmpty()) {
                if (!(valueSet.iterator().next() instanceof String)) {
                    throw new IllegalArgumentException("This object Type is not handle by the PreferencePersistence class");
                }
            }
            editor.putStringSet(key, (Set<String>) value);
        } else {
            throw new IllegalArgumentException("This object Type is not handle by the PreferencePersistence class");
        }
		editor.apply();
	}

	@Override
	public Object read(String key, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        Map<String, ?> objectMap = prefs.getAll();
		return objectMap.get(key);
	}
	
	@Override
	public boolean remove(String key, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		if (!prefs.contains(key)) return false;
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.apply();
		
		return true;
	}

    @Override
    public boolean reset(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        return true;
    }

}
