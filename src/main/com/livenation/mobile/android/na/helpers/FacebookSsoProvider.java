package com.livenation.mobile.android.na.helpers;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.ui.FacebookLaunchActivity;
import com.livenation.mobile.android.platform.util.Logger;

public class FacebookSsoProvider implements SsoProvider {

	@Override
	public Intent getSignInIntent(Activity activity) {
		return new Intent(activity, FacebookLaunchActivity.class);
	}
	
	@Override
	public void logout() {
		if (null != Session.getActiveSession()) {
			Session.getActiveSession().closeAndClearTokenInformation();
			Logger.log("Facebook", "Session cleared");
		} else {
			Logger.log("Facebook", "Active session is null :-/");
		}
	}
	
	public static User getAppUser(GraphUser graphUser) {
		Map<String, Object> map = graphUser.asMap();
		
		String id = graphUser.getId();
		String name = graphUser.getName();
		String email = map.get("email").toString();
		String pictureUrl = String.format("http://graph.facebook.com/%s/picture?type=large", id);
		
		User user = new User(id);
		user.setName(name);
		user.setEmail(email);
		user.setPictureUrl(pictureUrl);
		
		return user;
	}

}
