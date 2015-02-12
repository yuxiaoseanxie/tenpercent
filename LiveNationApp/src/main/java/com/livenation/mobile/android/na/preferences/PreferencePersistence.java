package com.livenation.mobile.android.na.preferences;

import com.livenation.mobile.android.na.helpers.VisibleForTesting;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencePersistence {
    protected final String name;
    private final Context context;

    public PreferencePersistence(String name, Context context) {
        this.name = name;
        this.context = context;
    }

    public void write(String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void write(String key, Long value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String readString(String key) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public Long readLong(String key) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return prefs.getLong(key, Long.MAX_VALUE);
    }

    public boolean remove(String key) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (!prefs.contains(key)) return false;

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();

        return true;
    }

    @VisibleForTesting
    public boolean reset() {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        return true;
    }

}