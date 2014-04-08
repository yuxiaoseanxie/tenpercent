package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.model.GraphUser;
import com.livenation.mobile.android.na.helpers.BaseSsoProvider.BaseSessionState.SessionPayload;
import com.livenation.mobile.android.na.helpers.BaseSsoProvider.BaseSessionState.SessionPayloadListener;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

import java.util.Arrays;
import java.util.Map;

public class FacebookSsoProvider extends BaseSsoProvider<Session> implements BaseSsoProvider.BaseSessionState.SessionPayloadListener<Session> {
    private final String PARAMETER_ACCESS_KEY = "facebook_access_token";
    private User user;
    private String accessToken;
    private SessionState currentSession;

    FacebookSsoProvider(ActivityProvider activityProvider) {
        super(activityProvider);
    }

    public static User getAppUser(GraphUser graphUser) {
        Map<String, Object> map = graphUser.asMap();

        String id = graphUser.getId();
        String email = map.get("email").toString();
        String name = graphUser.getName();
        String pictureUrl = String.format("http://graph.facebook.com/%s/picture?type=large", id);

        User user = new User();
        user.setId(graphUser.getId());
        user.setDisplayName(name);
        user.setEmail(email);
        user.setUrl(pictureUrl);

        return user;
    }

    @Override
    public void openSession(final boolean allowForeground, final OpenSessionCallback callback) {
        if (hasSessionCache()) {
            callback.onOpenSession(accessToken);
            return;
        }

        GetTokenAndUserPayload payload = new GetTokenAndUserPayload(FacebookSsoProvider.this) {

            @Override
            public void onComplete(String accessToken, User user) {
                FacebookSsoProvider.this.accessToken = accessToken;
                FacebookSsoProvider.this.user = user;

                callback.onOpenSession(accessToken);
            }

            @Override
            public void onSessionFailed() {
                callback.onOpenSessionFailed(new Exception(), allowForeground);
            }

            @Override
            void onNoNetwork() {
                callback.onNoNetwork();
            }
        };

        currentSession = new SessionState(getActivity(), allowForeground, payload);
        currentSession.open();
    }

    @Override
    public void onPayloadComplete(SessionPayload<Session> payload) {
        destroySession(payload.getSession());
    }

    @Override
    public void getUser(GetUserCallback callback) {
        if (null == user) throw new IllegalStateException("Session must be opened first");
        callback.onGetUser(user);
    }

    @Override
    public void clearSession() {
        clearSessionCache();
        LogoutPayload payload = new LogoutPayload(FacebookSsoProvider.this);
        currentSession = new SessionState(getActivity(), false, payload);
        currentSession.open();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode,
                                 int resultCode, Intent data) {
        if (null == currentSession) return;
        Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public String getTokenKey() {
        return PARAMETER_ACCESS_KEY;
    }

    @Override
    void destroySession(Session session) {
        if (null == Session.getActiveSession()) return;
        if (null == currentSession) return;
        Session.getActiveSession().removeCallback(currentSession.getStatusCallback());
        Session.setActiveSession(null);
        currentSession = null;
    }

    @Override
    public int getId() {
        return SsoManager.SSO_FACEBOOK;
    }

    public boolean hasSessionCache() {
        return user != null && accessToken != null;
    }

    public void clearSessionCache() {
        user = null;
        accessToken = null;
    }

    private static class SessionState extends BaseSessionState<Session> {
        private final Session.StatusCallback statusCallback = new SessionStatusCallback();

        public SessionState(Activity activity, boolean allowForeground, SessionPayload<Session> sessionPayload) {
            super(activity, allowForeground, sessionPayload);
        }

        public void open() {
            OpenRequest op = new Session.OpenRequest(activity);
            op.setCallback(statusCallback);
            op.setPermissions(Arrays.asList("email"));
            if (!allowForeground) {
                op.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);
            }

            Session session = new Builder(activity).build();
            Session.setActiveSession(session);
            session.openForRead(op);
        }

        public Session.StatusCallback getStatusCallback() {
            return statusCallback;
        }

        private class SessionStatusCallback implements Session.StatusCallback {

            @Override
            public void call(final Session session, com.facebook.SessionState state,
                             Exception exception) {
                if (null != exception) {
                    //TODO: catch the "no network/offline" error details here
                    //route to sessionPayload->onNoNetwork
                    sessionPayload.onSessionFailed();
                    return;
                }
                if (session.isOpened()) {
                    sessionPayload.setSession(session);
                    sessionPayload.run();
                }
            }
        }
    }

    private class LogoutPayload extends BaseSessionState.SessionPayload<Session> {

        public LogoutPayload(SessionPayloadListener<Session> listener) {
            super(listener);
        }

        @Override
        public void run() {
            getSession().closeAndClearTokenInformation();
            getListener().onPayloadComplete(this);
        }

        @Override
        public void onSessionFailed() {
            getListener().onPayloadComplete(this);
        }

        @Override
        void onNoNetwork() {
            getListener().onPayloadComplete(this);
        }
    }

    private abstract class GetTokenAndUserPayload extends BaseSessionState.SessionPayload<Session> {

        public GetTokenAndUserPayload(SessionPayloadListener<Session> listener) {
            super(listener);
        }

        @Override
        public void run() {
            Request.newMeRequest(getSession(), new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser graphUser, Response response) {
                    try {
                        if (null == graphUser) {
                            onComplete(getSession().getAccessToken(), null);
                            return;
                        }
                        User user = FacebookSsoProvider.getAppUser(graphUser);
                        onComplete(getSession().getAccessToken(), user);
                    } finally {
                        getListener().onPayloadComplete(GetTokenAndUserPayload.this);
                    }
                }
            }).executeAsync();
        }

        abstract void onComplete(String accessToken, User user);
    }
}
