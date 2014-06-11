package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.livenation.mobile.android.na.helpers.BaseSsoProvider.BaseSessionState.SessionPayload;
import com.livenation.mobile.android.na.helpers.BaseSsoProvider.BaseSessionState.SessionPayloadListener;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GoogleSsoProvider extends BaseSsoProvider<GoogleApiClient> implements BaseSsoProvider.BaseSessionState.SessionPayloadListener<GoogleApiClient> {
    @SuppressWarnings("unused")
    private static final String CLIENT_ID = "898638177791-oj5jfa34nqjs7abh8pu5p3j9li1momi5.apps.googleusercontent.com";
    private static final String PLUS_LOGIN_SCOPE = "https://www.googleapis.com/auth/plus.login";
    private final String PARAMETER_ACCESS_KEY = "google_plus_code";
    private User user;
    private String accessToken;
    private SessionState currentSession;

    public GoogleSsoProvider(ActivityProvider activityProvider) {
        super(activityProvider);
    }

    @Override
    public void openSession(final boolean allowForeground,
                            final OpenSessionCallback callback) {
        if (hasSessionCache()) {
            callback.onOpenSession(accessToken);
            return;
        }

        SessionState.SessionPayload<GoogleApiClient> payload = new GetTokenAndUserPayload(
                getActivity(), GoogleSsoProvider.this) {

            @Override
            public void onComplete(String accessToken, User user) {
                GoogleSsoProvider.this.accessToken = accessToken;
                GoogleSsoProvider.this.user = user;

                callback.onOpenSession(accessToken);
            }

            @Override
            public void onSessionFailed() {
                callback.onOpenSessionFailed(new Exception(), allowForeground);
            }
        };

        currentSession = new SessionState(getActivity(), allowForeground, payload);
        currentSession.open();
    }

    @Override
    public void onPayloadComplete(SessionPayload<GoogleApiClient> payload) {
        destroySession(payload.getSession());
    }

    @Override
    public void getUser(ApiService.BasicApiCallback<User> callback) {
        if (null == user)
            throw new IllegalStateException("Session must be opened first");
        callback.onResponse(user);
    }

    @Override
    public void clearSession() {
        clearSessionCache();
        SessionState.SessionPayload<GoogleApiClient> payload = new LogoutPayload(GoogleSsoProvider.this);
        currentSession = new SessionState(getActivity(), false, payload);
        currentSession.open();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode,
                                 int resultCode, Intent data) {
        if (null == currentSession)
            return;
        currentSession
                .onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public String getTokenKey() {
        return PARAMETER_ACCESS_KEY;
    }

    @Override
    void destroySession(GoogleApiClient googleApiClient) {
        if (null != googleApiClient) {
            googleApiClient.disconnect();
        }
        currentSession = null;
    }

    public SsoManager.SSO_TYPE getId() {
        return SsoManager.SSO_TYPE.SSO_GOOGLE;
    }

    public boolean hasSessionCache() {
        return user != null && accessToken != null;
    }

    public void clearSessionCache() {
        user = null;
        accessToken = null;
    }

    private static class LogoutPayload extends
            SessionState.SessionPayload<GoogleApiClient> {

        public LogoutPayload(SessionPayloadListener<GoogleApiClient> listener) {
            super(listener);
        }

        @Override
        public void run() {
            if (getSession().isConnected()) {
                Plus.AccountApi.revokeAccessAndDisconnect(getSession());
                Plus.AccountApi.clearDefaultAccount(getSession());
            }
            getListener().onPayloadComplete(this);
        }

        @Override
        public void onSessionFailed() {
            getListener().onPayloadComplete(this);
        }

    }

    private abstract static class GetTokenAndUserPayload extends
            BaseSessionState.SessionPayload<GoogleApiClient> {

        private final Activity activity;

        public GetTokenAndUserPayload(Activity activity,
                                      SessionPayloadListener<GoogleApiClient> listener) {
            super(listener);
            this.activity = activity;
        }

        @Override
        public void run() {
            String accessToken = null;
            try {
                final String SCOPE = String.format("oauth2:%s", PLUS_LOGIN_SCOPE);

                accessToken = GoogleAuthUtil.getToken(activity, Plus.AccountApi.getAccountName(getSession()), SCOPE);
                User user = getProfileInformation(getSession());

                onComplete(accessToken, user);

                getListener().onPayloadComplete(this);

            } catch (IOException transientEx) {
                onSessionFailed();
                return;
            } catch (UserRecoverableAuthException e) {
                throw new IllegalStateException("Initial Google Scope was not wide enough");
            } catch (GoogleAuthException authEx) {
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ;


        private User getProfileInformation(GoogleApiClient googleApiClient) {
            try {
                if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
                    Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                    String email = Plus.AccountApi.getAccountName(googleApiClient);

                    String name = currentPerson.getDisplayName();

                    User user = new User();
                    user.setId(currentPerson.getId());
                    user.setDisplayName(name);
                    user.setEmail(email);
                    String profilePicUrl = currentPerson.getImage().getUrl();
                    profilePicUrl = getLargerProfileImage(profilePicUrl);
                    user.setUrl(profilePicUrl);
                    return user;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getLargerProfileImage(String url) {
            //search the google image profile pic string for the "sz=50" query param, and remove it if found
            Pattern pattern = Pattern.compile("[?]sz=[0-9]*$");
            Matcher m = pattern.matcher(url);
            if (m.find()) {
                return m.replaceFirst("?sz=500");
            }
            return url;
        }

        abstract void onComplete(String accessToken, User user);

    }

    private static class SessionState extends BaseSessionState<GoogleApiClient>
            implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        private final int RC_SIGN_IN = 6613;
        private final int RESOLVE_COUNT_MAX = 2;
        private GoogleApiClient googleApiClient;
        private int resolveCount;

        public SessionState(Activity activity, boolean allowForeground,
                            BaseSessionState.SessionPayload<GoogleApiClient> sessionPayload) {

            super(activity, allowForeground, sessionPayload);
            this.googleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(SessionState.this)
                    .addOnConnectionFailedListener(SessionState.this)
                    .addApi(Plus.API, null).addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
        }

        @Override
        public void open() {
            googleApiClient.connect();
        }

        @Override
        public void onConnected(Bundle args) {
            sessionPayload.setSession(googleApiClient);
            new Thread(sessionPayload).start();
        }

        @Override
        public void onConnectionSuspended(int arg0) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            if (!allowForeground) {
                sessionPayload.onSessionFailed();
                return;
            }

            if (resolveCount < RESOLVE_COUNT_MAX) {
                if (result.hasResolution()) {
                    try {
                        resolveCount++;
                        activity.startIntentSenderForResult(
                                result.getResolution().getIntentSender(), RC_SIGN_IN,
                                null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        googleApiClient.connect();
                    }
                } else {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            result.getErrorCode(), activity, RC_SIGN_IN,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    sessionPayload.onSessionCanceled();
                                }
                            }
                    );
                    dialog.show();
                }
            }
        }

        public void onActivityResult(Activity activity, int requestCode,
                                     int resultCode, Intent data) {

            if (requestCode == RC_SIGN_IN) {
                if (resultCode == Activity.RESULT_OK) {
                    if (!googleApiClient.isConnecting()) {
                        googleApiClient.connect();
                    }
                } else {
                    sessionPayload.onSessionFailed();
                }
            }
        }

    }
}
