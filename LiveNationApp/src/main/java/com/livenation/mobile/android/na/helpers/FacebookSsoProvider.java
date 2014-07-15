package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.model.GraphUser;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.ActivityProvider;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;

import java.util.Arrays;
import java.util.Map;

public class FacebookSsoProvider extends ApiSsoProvider {
    private final String PARAMETER_ACCESS_KEY = "facebook_access_token";

    public FacebookSsoProvider(ActivityProvider activityProvider) {
        super(activityProvider);
    }


    //ApiSsoProvider interface --begin

    @Override
    public void login(final boolean allowForeground, final SsoLoginCallback callback) {
        Session session = new Builder(LiveNationApplication.get().getApplicationContext()).build();
        Session.StatusCallback statusCallback = new FacebookSessionWorker(new SsoLoginCallback() {
            @Override
            public void onLoginSucceed(String accessToken, User user) {
                if (callback != null) {
                    callback.onLoginSucceed(accessToken, user);
                }
            }

            @Override
            public void onLoginFailed(LiveNationError error) {
                Context context = LiveNationApplication.get().getApplicationContext();
                Toast toast = Toast.makeText(context, context.getString(R.string.login_connection_problem), Toast.LENGTH_SHORT);
                toast.show();
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

        final OpenRequest op = new OpenRequest(getActivity());
        op.setCallback(statusCallback);
        op.setPermissions(Arrays.asList("email"));
        if (!allowForeground) {
            op.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);
        }

        Session.setActiveSession(session);

        session.openForRead(op);
    }

    @Override
    public void login(boolean allowForeground) {
        login(allowForeground, null);
    }

    @Override
    public void logout() {
        logout(null);
    }

    @Override
    public void logout(SsoLogoutCallback callback) {
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        }
        if (callback != null) {
            callback.onLogoutSucceed();
        }
    }

    @Override
    public String getTokenKey() {
        return PARAMETER_ACCESS_KEY;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, SsoLoginCallback callback) {
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    //ApiSsoProvider interface --end


    public SsoManager.SSO_TYPE getId() {
        return SsoManager.SSO_TYPE.SSO_FACEBOOK;
    }


    private class FacebookSessionWorker implements Session.StatusCallback {
        final private SsoLoginCallback loginCallback;

        private FacebookSessionWorker(SsoLoginCallback loginCallback) {
            this.loginCallback = loginCallback;
        }

        @Override
        public void call(final Session session, com.facebook.SessionState state,
                         Exception exception) {

            if (null != exception && !(exception instanceof FacebookOperationCanceledException)) {
                loginCallback.onLoginFailed(new LiveNationError(exception));
                return;
            }
            if (exception instanceof FacebookOperationCanceledException) {
                loginCallback.onLoginCanceled();
                return;
            }
            if (session.isOpened()) {
                new FacebookUserClient(loginCallback).run(session);
            }
        }
    }

    private class FacebookUserClient {

        private final SsoLoginCallback loginCallback;

        public FacebookUserClient(SsoLoginCallback loginCallback) {
            this.loginCallback = loginCallback;
        }


        public void run(final Session session) {
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser graphUser, Response response) {
                    if (null == graphUser) {
                        loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_FACEBOOK_LOGIN_FAILED));
                        return;
                    }
                    User user = FacebookSsoProvider.getAppUser(graphUser);
                    loginCallback.onLoginSucceed(session.getAccessToken(), user);

                }
            }).executeAsync();
        }
    }

    private static User getAppUser(GraphUser graphUser) {
        Map<String, Object> map = graphUser.asMap();

        String id = graphUser.getId();
        String email = null;
        if (map.get("email") != null) {
            email = map.get("email").toString();
        }
        String name = graphUser.getName();
        String pictureUrl = String.format("http://graph.facebook.com/%s/picture?type=large", id);

        User user = new User();
        user.setId(graphUser.getId());
        user.setDisplayName(name);
        user.setEmail(email);
        user.setUrl(pictureUrl);

        return user;
    }
}
