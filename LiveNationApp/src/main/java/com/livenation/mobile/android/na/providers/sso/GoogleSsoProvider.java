package com.livenation.mobile.android.na.providers.sso;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;
import com.livenation.mobile.android.platform.sso.SsoManager;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleSsoProvider extends SsoProviderPersistence implements ApiSsoProvider {
    private static final String CLIENT_ID = "898638177791-oj5jfa34nqjs7abh8pu5p3j9li1momi5.apps.googleusercontent.com";
    private static final String PLUS_LOGIN_SCOPE = "https://www.googleapis.com/auth/plus.login";
    private final int RC_SIGN_IN = 6613;
    private final int RESOLVE_COUNT_MAX = 2;
    private GoogleApiClient googleApiClient;
    private int resolveCount;

    public GoogleSsoProvider(Context context) {
        super(context);
    }


    @Override
    public void login(final boolean allowForeground, final SsoLoginCallback callback, Activity activity) {

        GoogleSessionWorker googleSessionWorker = new GoogleSessionWorker(new SsoLoginCallback() {
            @Override
            public void onLoginSucceed(String ssoAccessToken, User user) {
                saveAuthConfiguration(getType(), ssoAccessToken);
                saveUser(user, getType());
                if (callback != null) {
                    callback.onLoginSucceed(ssoAccessToken, user);
                }
            }

            @Override
            public void onLoginFailed(LiveNationError error) {
                removeAuthConfiguration();
                removeUser();
                if (callback != null) {
                    callback.onLoginFailed(error);
                }
            }

            @Override
            public void onLoginCanceled() {
                removeAuthConfiguration();
                removeUser();
                if (callback != null) {
                    callback.onLoginCanceled();
                }
            }
        }, allowForeground, activity);
        this.googleApiClient = new GoogleApiClient.Builder(LiveNationApplication.get().getApplicationContext())
                .addConnectionCallbacks(googleSessionWorker)
                .addOnConnectionFailedListener(googleSessionWorker)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void login(boolean allowForeground, Activity activity) {
        login(allowForeground, null, activity);
    }

    @Override
    public void logout() {
        logout(null);
    }

    @Override
    public void logout(SsoLogoutCallback callback) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
        }

        removeAuthConfiguration();
        removeUser();

        if (callback != null) {
            callback.onLogoutSucceed();
        }
    }

    @Override
    public SsoManager.SSO_TYPE getType() {
        return SsoManager.SSO_TYPE.SSO_GOOGLE;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SsoLoginCallback callback) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == SsoActivity.RESULT_CANCELED) {
                callback.onLoginCanceled();
            } else if (resultCode == SsoActivity.RESULT_OK) {
                if (!googleApiClient.isConnecting()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    private class GoogleUserClient {

        private final SsoLoginCallback loginCallback;

        public GoogleUserClient(SsoLoginCallback loginCallback) {
            this.loginCallback = loginCallback;
        }

        public void run(GoogleApiClient client, Context context) {
            String accessToken = null;
            try {
                final String SCOPE = String.format("oauth2:%s", PLUS_LOGIN_SCOPE);

                accessToken = GoogleAuthUtil.getToken(context, Plus.AccountApi.getAccountName(client), SCOPE);
                User user = getProfileInformation(client);

                loginCallback.onLoginSucceed(accessToken, user);

            } catch (IOException transientEx) {
                loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_LOGIN_FAILED));
                return;
            } catch (UserRecoverableAuthException e) {
                loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_LOGIN_FAILED));
            } catch (GoogleAuthException authEx) {
                loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_LOGIN_FAILED));
                return;
            } catch (Exception e) {
                loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_LOGIN_FAILED));
                return;
            }
        }

        private User getProfileInformation(GoogleApiClient googleApiClient) {
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

    }

    private class GoogleSessionWorker implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
        final Activity activity;
        final private SsoLoginCallback loginCallback;
        final private boolean allowForeground;

        private GoogleSessionWorker(SsoLoginCallback loginCallback, boolean allowForeground, Activity activity) {
            this.loginCallback = loginCallback;
            this.allowForeground = allowForeground;
            this.activity = activity;
        }


        @Override
        public void onConnected(Bundle bundle) {
            final GoogleUserClient userClient = new GoogleUserClient(loginCallback);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    userClient.run(googleApiClient, LiveNationApplication.get().getApplicationContext());
                }
            }).start();
        }

        @Override
        public void onConnectionSuspended(int i) {
            loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_LOGIN_FAILED_CONNECTION_SUSPENDED));
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (!allowForeground) {
                loginCallback.onLoginFailed(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_GOOGLE_SESSION_NOT_OPEN));
                return;
            }

            if (resolveCount < RESOLVE_COUNT_MAX) {
                if (connectionResult.hasResolution()) {
                    try {
                        activity.startIntentSenderForResult(
                                connectionResult.getResolution().getIntentSender(), RC_SIGN_IN,
                                null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        resolveCount++;
                        googleApiClient.connect();
                    }
                } else {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionResult.getErrorCode(), activity, RC_SIGN_IN,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    loginCallback.onLoginCanceled();
                                }
                            }
                    );
                    dialog.show();
                }
            }
        }
    }
}
