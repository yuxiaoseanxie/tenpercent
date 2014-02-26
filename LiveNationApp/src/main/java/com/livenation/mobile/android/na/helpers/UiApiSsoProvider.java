package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

public interface UiApiSsoProvider extends ApiSsoProvider  {
	void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data);
	
	public static interface ActivityProvider {
		Activity getActivity();
	}
	
}
