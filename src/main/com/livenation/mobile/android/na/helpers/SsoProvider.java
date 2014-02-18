package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

public interface SsoProvider {
	Intent getSignInIntent(Activity activity);
	void logout();
}
