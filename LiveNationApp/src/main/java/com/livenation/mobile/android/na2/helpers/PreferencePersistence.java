package com.livenation.mobile.android.na2.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencePersistence implements PersistenceProvider<String> {
	protected final String name;
	
	public PreferencePersistence(String name) {
		this.name = name;
	}
	
	@Override
	public void write(String key, String value, Context context) {
		SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
		editor.putString(key, (String) value);
		editor.apply();
	}

	@Override
	public String read(String key, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		return prefs.getString(key, null);
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

}
