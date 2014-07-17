package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.ActivityProvider;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;
import com.segment.android.Analytics;
import com.segment.android.models.Traits;

import java.lang.ref.WeakReference;

import static com.livenation.mobile.android.na.helpers.SsoManager.SSO_TYPE.SSO_FACEBOOK;

public class SsoManager implements ActivityProvider {
    public enum SSO_TYPE {
        SSO_FACEBOOK(R.drawable.facebook_logo),
        SSO_GOOGLE(R.drawable.google_plus_logo),
        SSO_DUMMY(0),;

        public int logoResId;

        SSO_TYPE(int logoResId) {
            this.logoResId = logoResId;
        }

        public int getLogoResId() {
            return logoResId;
        }
    }

    private final FacebookSsoProvider facebookSso = new FacebookSsoProvider(this);
    private final GoogleSsoProvider googleSso = new GoogleSsoProvider(this);
    private final DummySsoProvider dummySso = new DummySsoProvider();
    private final PreferencePersistence persistance = new PreferencePersistence("auth_configuration");
    private final String PARAMETER_ACCESS_TOKEN_KEY = "access_token";
    private final String PARAMETER_TIMESTAMP = "timestamp";
    private final String PARAMETER_SSO_PROVIDER_ID_KEY = "sso_provider_id";
    private final String USER_ID = "user_id";
    private final String USER_NAME = "user_name";
    private final String USER_EMAIL = "user_email";
    private final String USER_PIC_URL = "user_pic_url";
    private final ApiSsoProvider defaultProvider;
    private WeakReference<Activity> weakActivity;

    public SsoManager(ApiSsoProvider defaultProvider) {

        this.defaultProvider = defaultProvider;
    }

    /**public static SSO_TYPE getProviderId(ApiSsoProvider provider) {
        if (provider instanceof FacebookSsoProvider) {
            return SSO_FACEBOOK;
        }
        if (provider instanceof GoogleSsoProvider) {
            return SSO_TYPE.SSO_GOOGLE;
        }
        if (provider instanceof DummySsoProvider) {
            return SSO_TYPE.SSO_DUMMY;
        }
        throw new IllegalArgumentException("Unknown provider type");
    }**/

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

    public void logout(final Context context, final SsoLogoutCallback callback) {
        ApiSsoProvider ssoProvider = getConfiguredSsoProvider(context);
        if (ssoProvider != null) {
            ssoProvider.logout(new SsoLogoutCallback() {
                @Override
                public void onLogoutSucceed() {
                    removeAuthConfiguration(context);
                    removeUser(context);
                    if (callback != null) {
                        callback.onLogoutSucceed();
                    }
                }

                @Override
                public void onLogoutFailed(LiveNationError error) {
                    if (callback != null) {
                        callback.onLogoutFailed(error);
                    }
                }
            });
        }
        LiveNationApplication.get().getConfigManager().clearAccessToken();
        LiveNationApplication.get().getConfigManager().buildApi();
    }

    public void login(final SSO_TYPE ssoType, final Context context, boolean allowForeground, final SsoLoginCallback callback) {
        getSsoProvider(ssoType, context).login(allowForeground, new SsoLoginCallback() {
            @Override
            public void onLoginSucceed(String accessToken, User user) {
                saveAuthConfiguration(ssoType, accessToken, context);
                saveUser(user, context);

                if (callback != null) {
                    callback.onLoginSucceed(accessToken, user);
                }

            }

            @Override
            public void onLoginFailed(LiveNationError error) {
                removeAuthConfiguration(context);
                removeUser(context);
                if (callback != null) {
                    callback.onLoginFailed(error);
                }
            }

            @Override
            public void onLoginCanceled() {
                if (callback != null) {
                    callback.onLoginCanceled();
                }
            }
        });
    }

    public ApiSsoProvider getConfiguredSsoProvider(Context context) {
        AuthConfiguration authConfig = getAuthConfiguration(context);
        if (null == authConfig) {
            return defaultProvider;
        }
        SSO_TYPE ssoProviderId = authConfig.getSsoProviderId();
        return getSsoProvider(ssoProviderId, context);
    }

    private void saveAuthConfiguration(SSO_TYPE ssoProviderId, String token, Context context) {
        persistance.write(PARAMETER_ACCESS_TOKEN_KEY, token, context);
        String ssoIdValue = ssoProviderId.name();
        persistance.write(PARAMETER_SSO_PROVIDER_ID_KEY, ssoIdValue, context);
        persistance.write(PARAMETER_TIMESTAMP, System.currentTimeMillis(), context);
    }

    public AuthConfiguration getAuthConfiguration(Context context) {
        String accessToken = persistance.readString(PARAMETER_ACCESS_TOKEN_KEY, context);
        String ssoId = persistance.readString(PARAMETER_SSO_PROVIDER_ID_KEY, context);
        Long timestamp = persistance.readLong(PARAMETER_TIMESTAMP, context);

        if (TextUtils.isEmpty(ssoId)) {
            return null;
        }

        SSO_TYPE ssoIdValue = SSO_TYPE.valueOf(ssoId);
        return new AuthConfiguration(ssoIdValue, accessToken, timestamp);
    }

    private void removeAuthConfiguration(Context context) {
        persistance.remove(PARAMETER_ACCESS_TOKEN_KEY, context);
        persistance.remove(PARAMETER_SSO_PROVIDER_ID_KEY, context);
    }

    private void saveUser(User user, Context context) {
        if (null == user) throw new IllegalArgumentException("User is null");
        persistance.write(USER_ID, user.getId(), context);
        persistance.write(USER_NAME, user.getDisplayName(), context);
        persistance.write(USER_EMAIL, user.getEmail(), context);
        persistance.write(USER_PIC_URL, user.getUrl(), context);
        Analytics.identify(user.getId(), new Traits("name", user.getDisplayName(),
                "email", user.getEmail()));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.BroadCastReceiver.LOGIN));

        ApiSsoProvider provider = getConfiguredSsoProvider(context);
        if (provider != null) {
            if (provider instanceof FacebookSsoProvider) {
                LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT, AnalyticsCategory.HOUSEKEEPING);
            } else if (provider instanceof GoogleSsoProvider) {
                LiveNationAnalytics.track(AnalyticConstants.GOOGLE_CONNECT, AnalyticsCategory.HOUSEKEEPING);
            }
        }

    }

    public User readUser(Context context) {
        String userId = persistance.readString(USER_ID, context);
        String userName = persistance.readString(USER_NAME, context);

        String userEmail = persistance.readString(USER_EMAIL, context);
        String userPicUrl = persistance.readString(USER_PIC_URL, context);

        if (null == userId) return null;

        User user = new User();
        user.setId(userId);
        user.setDisplayName(userName);
        user.setEmail(userEmail);
        user.setUrl(userPicUrl);

        return user;
    }

    private void removeUser(Context context) {
        persistance.remove(USER_ID, context);
        persistance.remove(USER_NAME, context);
        persistance.remove(USER_EMAIL, context);
        persistance.remove(USER_PIC_URL, context);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.BroadCastReceiver.LOGOUT));
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
    public ApiSsoProvider getSsoProvider(SSO_TYPE ssoProviderId, Context context) {
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
        private final SSO_TYPE ssoProviderId;
        private final String accessToken;
        private final Long timestamp;

        public AuthConfiguration(SSO_TYPE ssoProviderId, String accessToken, Long timestamp) {
            this.ssoProviderId = ssoProviderId;
            this.accessToken = accessToken;
            this.timestamp = timestamp;
        }

        public SSO_TYPE getSsoProviderId() {
            return ssoProviderId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public Long getTimestamp() {
            return timestamp;
        }
    }

}
