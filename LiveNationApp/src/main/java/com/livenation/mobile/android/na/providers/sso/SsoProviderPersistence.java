package com.livenation.mobile.android.na.providers.sso;

import android.content.Context;
import android.text.TextUtils;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.sso.SsoManager;

/**
 * Created by elodieferrais on 7/21/14.
 */
public class SsoProviderPersistence {

    private final PreferencePersistence persistance;

    public SsoProviderPersistence(Context context) {
        persistance = new PreferencePersistence(Constants.SharedPreferences.AUTHENTIFICATION_NAME, context);
    }

    public SsoManager.SSO_TYPE getConfiguredSsoProvider() {
        SsoManager.AuthConfiguration authConfig = getAuthConfiguration();
        SsoManager.SSO_TYPE type = null;
        if (authConfig != null) {
            type = authConfig.getSsoProviderId();
        }
        return type;
    }

    public void saveAuthConfiguration(SsoManager.SSO_TYPE ssoProviderId, String token) {
        persistance.write(Constants.SharedPreferences.PARAMETER_ACCESS_TOKEN_KEY, token);
        String ssoIdValue = ssoProviderId.name();
        persistance.write(Constants.SharedPreferences.PARAMETER_SSO_PROVIDER_ID_KEY, ssoIdValue);
        persistance.write(Constants.SharedPreferences.PARAMETER_TIMESTAMP, System.currentTimeMillis());
    }

    public SsoManager.AuthConfiguration getAuthConfiguration() {
        String accessToken = persistance.readString(Constants.SharedPreferences.PARAMETER_ACCESS_TOKEN_KEY);
        String ssoId = persistance.readString(Constants.SharedPreferences.PARAMETER_SSO_PROVIDER_ID_KEY);
        Long timestamp = persistance.readLong(Constants.SharedPreferences.PARAMETER_TIMESTAMP);

        if (TextUtils.isEmpty(ssoId)) {
            return null;
        }

        SsoManager.SSO_TYPE ssoIdValue = SsoManager.SSO_TYPE.valueOf(ssoId);
        return new SsoManager.AuthConfiguration(ssoIdValue, accessToken, timestamp);
    }

    protected void removeAuthConfiguration() {
        persistance.remove(Constants.SharedPreferences.PARAMETER_ACCESS_TOKEN_KEY);
        persistance.remove(Constants.SharedPreferences.PARAMETER_SSO_PROVIDER_ID_KEY);
    }

    public void saveUser(User user, SsoManager.SSO_TYPE type) {
        if (null == user) throw new IllegalArgumentException("User is null");
        persistance.write(Constants.SharedPreferences.USER_ID, user.getId());
        persistance.write(Constants.SharedPreferences.USER_NAME, user.getDisplayName());
        persistance.write(Constants.SharedPreferences.USER_EMAIL, user.getEmail());
        persistance.write(Constants.SharedPreferences.USER_PIC_URL, user.getUrl());

        if (type != null) {
            if (type.equals(SsoManager.SSO_TYPE.SSO_FACEBOOK)) {
                LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT, AnalyticsCategory.HOUSEKEEPING);
            } else if (type.equals(SsoManager.SSO_TYPE.SSO_GOOGLE)) {
                LiveNationAnalytics.track(AnalyticConstants.GOOGLE_CONNECT, AnalyticsCategory.HOUSEKEEPING);
            }
        }

    }

    public User readUser() {
        String userId = persistance.readString(Constants.SharedPreferences.USER_ID);
        String userName = persistance.readString(Constants.SharedPreferences.USER_NAME);

        String userEmail = persistance.readString(Constants.SharedPreferences.USER_EMAIL);
        String userPicUrl = persistance.readString(Constants.SharedPreferences.USER_PIC_URL);

        if (null == userId) return null;

        User user = new User();
        user.setId(userId);
        user.setDisplayName(userName);
        user.setEmail(userEmail);
        user.setUrl(userPicUrl);

        return user;
    }

    protected void removeUser() {
        persistance.remove(Constants.SharedPreferences.USER_ID);
        persistance.remove(Constants.SharedPreferences.USER_NAME);
        persistance.remove(Constants.SharedPreferences.USER_EMAIL);
        persistance.remove(Constants.SharedPreferences.USER_PIC_URL);
    }
}
