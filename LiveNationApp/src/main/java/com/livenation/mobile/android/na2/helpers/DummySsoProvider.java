package com.livenation.mobile.android.na2.helpers;

import android.app.Activity;
import android.content.Intent;

public class DummySsoProvider implements UiApiSsoProvider {

	@Override
	public void openSession(boolean allowForeground,
			OpenSessionCallback callback) {
		callback.onOpenSession(null);
	}

	@Override
	public void getUser(GetUserCallback callback) {
		callback.onGetUser(null);
	}

	@Override
	public void clearSession() {
	}

	@Override
	public String getTokenKey() {
		return null;
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public void onActivityResult(Activity activity, int requestCode,
			int resultCode, Intent data) {}

}
