package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.livenation.mobile.android.na.models.User;

public class SsoManager {
	public static final int SSO_FACEBOOK = 0;
	public static final int SSO_GOOGLE = 1;
	private FacebookSsoProvider facebookSso = new FacebookSsoProvider();
	private final PersistenceProvider<String> persistance = new PreferencePersistence("sso");
	private final String AUTH_TOKEN = "auth_token";
	private final String AUTH_SSO_PROVIDER_ID = "sso_provider_id";
	private final String USER_ID = "user_id";
	private final String USER_NAME = "user_name";
	private final String USER_EMAIL = "user_email";
	private final String USER_PIC_URL = "user_pic_url";
	
	public Intent getSignInIntent(int ssoProviderId, Activity activity) {
		SsoProvider ssoProvider = getSsoProvider(ssoProviderId, activity);
		return ssoProvider.getSignInIntent(activity);
	}
	
	
	public void logout(Context context) {
		AuthToken authToken = getAuthToken(context);
		if (null == authToken) {
			//not logged in
			return;
		}
		int ssoProviderId = authToken.getSsoProviderId();
		logout(ssoProviderId, context);
	}
	
	private void logout(int ssoProviderId, Context context) {
		SsoProvider ssoProvider = getSsoProvider(ssoProviderId, context);
		ssoProvider.logout();
	}
	
	private SsoProvider getSsoProvider(int ssoProviderId, Context context) {
		switch (ssoProviderId) {
		case SSO_FACEBOOK:
			return facebookSso;
		default:
			throw new IllegalArgumentException("Unknown SSO provider id: " + ssoProviderId);
		}
	}
	
	public void saveAuthToken(int ssoProviderId, String token, Context context) {
		persistance.write(AUTH_TOKEN, token, context);
		String ssoIdValue = Integer.valueOf(ssoProviderId).toString();
		persistance.write(AUTH_SSO_PROVIDER_ID, ssoIdValue, context);
	}
	
	public AuthToken getAuthToken(Context context) {
		String authToken = (String) persistance.read(AUTH_TOKEN, context);
		String ssoId = persistance.read(AUTH_SSO_PROVIDER_ID, context);
		Integer ssoIdValue = Integer.valueOf(ssoId);
		return new AuthToken(ssoIdValue, authToken);
	}
	
	public void removeAuthToken(Context context) {
		persistance.remove(AUTH_TOKEN, context);
		persistance.remove(AUTH_SSO_PROVIDER_ID, context);
	}
	
	public void saveUser(User user, Context context) {
		if (null == user) throw new IllegalArgumentException("User is null");
		persistance.write(USER_ID, user.getId(), context);
		persistance.write(USER_NAME, user.getName(), context);
		persistance.write(USER_EMAIL, user.getEmail(), context);
		persistance.write(USER_PIC_URL, user.getPictureUrl(), context);
	}
	
	public User readUser(Context context) {
		String userId = persistance.read(USER_ID, context);
		String userName = persistance.read(USER_NAME, context);
		String userEmail = persistance.read(USER_EMAIL, context);
		String userPicUrl = persistance.read(USER_PIC_URL, context);
		
		if (null == userId) return null;
		
		User user = new User(userId);
		user.setName(userName);
		user.setEmail(userEmail);
		user.setPictureUrl(userPicUrl);
		
		return user;
	}
	
	public void removeUser(Context context) {
		persistance.remove(USER_ID, context);
		persistance.remove(USER_NAME, context);
		persistance.remove(USER_EMAIL, context);
		persistance.remove(USER_PIC_URL, context);
	}
	
	public static class AuthToken {
		private final int ssoProviderId;
		private final String authToken;
		
		public AuthToken(int ssoProviderId, String authToken) {
			this.ssoProviderId = ssoProviderId;
			this.authToken = authToken;
		}
		
		public int getSsoProviderId() {
			return ssoProviderId;
		}
		
		public String getAuthToken() {
			return authToken;
		}
	}
}
