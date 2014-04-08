package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

import java.lang.ref.WeakReference;

public class SsoManager implements UiApiSsoProvider.ActivityProvider {
    public static final int SSO_FACEBOOK = 0;
    public static final int SSO_GOOGLE = 1;
    public static final int SSO_DUMMY = 2;

    private final FacebookSsoProvider facebookSso = new FacebookSsoProvider(this);
    private final GoogleSsoProvider googleSso = new GoogleSsoProvider(this);
    private final DummySsoProvider dummySso = new DummySsoProvider();
    private final PersistenceProvider<String> persistance = new PreferencePersistence("auth_configuration");
    private final String PARAMETER_ACCESS_TOKEN_KEY = "access_token";
    private final String PARAMETER_SSO_PROVIDER_ID_KEY = "sso_provider_id";
    private final String USER_ID = "user_id";
    private final String USER_NAME = "user_name";
    private final String USER_EMAIL = "user_email";
    private final String USER_PIC_URL = "user_pic_url";
    private final UiApiSsoProvider defaultProvider;
    private WeakReference<Activity> weakActivity;

    public SsoManager(UiApiSsoProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public static int getProviderId(ApiSsoProvider provider) {
        if (provider instanceof FacebookSsoProvider) {
            return SSO_FACEBOOK;
        }
        if (provider instanceof GoogleSsoProvider) {
            return SSO_GOOGLE;
        }
        if (provider instanceof DummySsoProvider) {
            return SSO_DUMMY;
        }
        throw new IllegalArgumentException("Unknown provider type");
    }

    @Override
    public Activity getActivity() {
        if (null != weakActivity) {
            return weakActivity.get();
        }
        return null;
    }

    public void setActivity(Activity activity) {
        if (null == activity) return;
        weakActivity = new WeakReference<Activity>(activity);
    }

    public void logout(Activity activity) {
        ApiSsoProvider ssoProvider = getConfiguredSsoProvider(activity);
        if (ssoProvider != null) {
            ssoProvider.clearSession();
        }
    }

    public UiApiSsoProvider getConfiguredSsoProvider(Context context) {
        AuthConfiguration authConfig = getAuthConfiguration(context);
        if (null == authConfig) {
            return defaultProvider;
        }
        int ssoProviderId = authConfig.getSsoProviderId();
        return getSsoProvider(ssoProviderId, context);
    }

    public void saveAuthConfiguration(int ssoProviderId, String token, Context context) {
        persistance.write(PARAMETER_ACCESS_TOKEN_KEY, token, context);
        String ssoIdValue = Integer.valueOf(ssoProviderId).toString();
        persistance.write(PARAMETER_SSO_PROVIDER_ID_KEY, ssoIdValue, context);
    }

    public AuthConfiguration getAuthConfiguration(Context context) {
        String accessToken = persistance.read(PARAMETER_ACCESS_TOKEN_KEY, context);
        String ssoId = persistance.read(PARAMETER_SSO_PROVIDER_ID_KEY, context);

        if (TextUtils.isEmpty(ssoId)) {
            return null;
        }

        Integer ssoIdValue = Integer.valueOf(ssoId);
        return new AuthConfiguration(ssoIdValue, accessToken);
    }

    public void removeAuthConfiguration(Context context) {
        persistance.remove(PARAMETER_ACCESS_TOKEN_KEY, context);
        persistance.remove(PARAMETER_SSO_PROVIDER_ID_KEY, context);
    }

    public void saveUser(User user, Context context) {
        if (null == user) throw new IllegalArgumentException("User is null");
        persistance.write(USER_ID, user.getId(), context);
        persistance.write(USER_NAME, user.getDisplayName(), context);
        persistance.write(USER_EMAIL, user.getEmail(), context);
        persistance.write(USER_PIC_URL, user.getUrl(), context);
    }

    public User readUser(Context context) {
        String userId = persistance.read(USER_ID, context);
        String userName = persistance.read(USER_NAME, context);

        String userEmail = persistance.read(USER_EMAIL, context);
        String userPicUrl = persistance.read(USER_PIC_URL, context);

        if (null == userId) return null;

        User user = new User();
        user.setId(userId);
        user.setDisplayName(userName);
        user.setEmail(userEmail);
        user.setUrl(userPicUrl);

        return user;
    }

    public void removeUser(Context context) {
        persistance.remove(USER_ID, context);
        persistance.remove(USER_NAME, context);
        persistance.remove(USER_EMAIL, context);
        persistance.remove(USER_PIC_URL, context);
    }

    /**
     * Force anything that wants to use the SsoProvider to supply an Activity reference.
     * This means that the SSOManager is always tracking the latest activity.
     * <p/>
     * The SSOProviders do not store the activity directly, instead when/if they need it they
     * request it from the SSOManager.
     * <p/>
     * Since this Activity is stored in a weakreference, the SSOManager will not cause a
     * context/activity memory leak.
     */
    public UiApiSsoProvider getSsoProvider(int ssoProviderId, Context context) {
        if (context instanceof Activity) {
            setActivity((Activity) context);
        }
        switch (ssoProviderId) {
            case SSO_FACEBOOK:
                return facebookSso;
            case SSO_GOOGLE:
                return googleSso;
            case SSO_DUMMY:
                return dummySso;
            default:
                throw new IllegalArgumentException("Unknown SSO provider id: " + ssoProviderId);
        }
    }

    public static class AuthConfiguration {
        private final int ssoProviderId;
        private final String accessToken;

        public AuthConfiguration(int ssoProviderId, String accessToken) {
            this.ssoProviderId = ssoProviderId;
            this.accessToken = accessToken;
        }

        public int getSsoProviderId() {
            return ssoProviderId;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

}
