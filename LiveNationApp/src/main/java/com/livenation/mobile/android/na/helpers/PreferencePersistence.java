package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencePersistence {
    protected final String name;

    public PreferencePersistence(String name) {
        this.name = name;
    }

    public void write(String key, String value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void write(String key, Long value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String readString(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public Long readLong(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return prefs.getLong(key, Long.MAX_VALUE);
    }

    public boolean remove(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (!prefs.contains(key)) return false;

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();

        return true;
    }

    public boolean reset(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        return true;
    }

}