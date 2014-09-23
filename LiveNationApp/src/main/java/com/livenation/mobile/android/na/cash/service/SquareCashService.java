package com.livenation.mobile.android.na.cash.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkInfo;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

public class SquareCashService {
    public static final String ACTION_SESSION_CHANGED = "com.livenation.mobile.android.na.cash.service.SquareCashService.ACTION_SESSION_CHANGED";

    private final Context context;
    private final RequestQueue requestQueue;
    private final SessionPersistenceProvider persistenceProvider;
    private CashSession session;

    public SquareCashService(@NonNull Context context,
                             @NonNull RequestQueue requestQueue,
                             @NonNull SessionPersistenceProvider persistenceProvider) {
        this.context = context.getApplicationContext();
        this.requestQueue = requestQueue;
        this.persistenceProvider = persistenceProvider;

        this.session = persistenceProvider.loadSession();
    }
    private static SquareCashService instance = null;
    public static SquareCashService getInstance() {
        return instance;
    }
    public static void init(@NonNull Context context,
                            @NonNull RequestQueue requestQueue,
                            @NonNull SessionPersistenceProvider persistenceProvider) {
        instance = new SquareCashService(context, requestQueue, persistenceProvider);
    }

    //region Building Requests

    public String makeRoute(String endComponents) {
        return "v1/" + getEncodedCustomerId() + endComponents;
    }

    public String makeUrl(@NonNull String route, @Nullable Map<String, String> params) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("https");
        builder.encodedAuthority(CashUtils.getHostForEnvironment(LiveNationLibrary.getEnvironmentProvider().getEnvironment()));
        builder.appendEncodedPath(route);

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet())
                builder.appendQueryParameter(param.getKey(), param.getValue());
        }

        return builder.build().toString();
    }

    private <T extends CashResponse> void injectHeaders(SquareRequest<T> request) {
        HashMap<String, String> headers = new HashMap<>();
        if (hasSession()) {
            headers.put("Authorization", "Bearer " + session.getAccessToken());
        }
        headers.put("Content-Type", "application/json");
        request.setHeaders(headers);
    }


    private <T extends CashResponse> Observable<T> doRequest(int method,
                                                             @NonNull String url,
                                                             @Nullable String requestBody,
                                                             @NonNull Class<T> responseClass) {
        Log.i(CashUtils.LOG_TAG, "Outgoing request to '" + url + "' with body: " + requestBody);

        ReplaySubject<T> subject = ReplaySubject.create(1);
        VolleyObservableAdapter<T> adapter = new VolleyObservableAdapter<>(subject);
        SquareRequest<T> request = new SquareRequest<>(method, url, responseClass, requestBody, adapter, adapter);

        injectHeaders(request);
        requestQueue.add(request);

        return subject;
    }

    private <T extends CashResponse> Observable<T> doGetRequest(@NonNull String route,
                                                                @Nullable Map<String, String> params,
                                                                @NonNull Class<T> responseClass) {
        return doRequest(SquareRequest.Method.GET, makeUrl(route, params), null, responseClass);
    }

    private <T extends CashResponse> Observable<T> doDeleteRequest(@NonNull String route,
                                                                   @Nullable Map<String, String> params,
                                                                   @NonNull Class<T> responseClass) {
        return doRequest(SquareRequest.Method.DELETE, makeUrl(route, params), null, responseClass);
    }

    private <T extends CashResponse> Observable<T> doPostRequest(@NonNull String route,
                                                                 @Nullable String requestBody,
                                                                 @NonNull Class<T> responseClass) {
        return doRequest(SquareRequest.Method.POST, makeUrl(route, null), requestBody, responseClass);
    }

    //endregion


    //region Sessions

    private void setSession(@Nullable CashSession session) {
        persistenceProvider.saveSession(session);
        this.session = session;

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SESSION_CHANGED));
    }

    public @Nullable CashSession getSession() {
        return session;
    }

    public void clearSession() {
        setSession(null);
    }

    public boolean hasSession() {
        return (getSession() != null);
    }

    public @Nullable String getStoredPhoneNumber() {
        return persistenceProvider.retrievePhoneNumber();
    }

    protected JSONObject makeRequestBody(@NonNull String... args) {
        JSONObject json = new JSONObject();
        if (args.length > 0) {
            if (args.length % 2 != 0)
                throw new IllegalArgumentException("Must specify an even number of arguments");

            String key = null;
            for (String arg : args) {
                if (key == null) {
                    key = arg;
                } else {
                    try {
                        json.put(key, arg);
                    } catch (JSONException e) {
                        Log.e(CashUtils.LOG_TAG, "Error constructing request args");
                    }
                    key = null;
                }
            }
        }

        return json;
    }

    protected String getEncodedCustomerId() {
        return Uri.encode(session.getCustomerId());
    }

    protected void assertSession() {
        if (!hasSession())
            throw new RuntimeException("session required");
    }

    public void startSession(final String email, final String phoneNumber, final ApiCallback<CashSession> callback) {
        if (email == null && phoneNumber == null)
            throw new IllegalArgumentException("email or phoneNumber must be supplied");

        LiveNationLibrary.getAccessTokenProvider().getAccessToken(new BasicApiCallback<AccessToken>() {
            @Override
            public void onResponse(AccessToken accessToken) {
                JSONObject body = new JSONObject();
                try {
                    body.put("force_new", "true");

                    if (phoneNumber != null)
                        body.put("phone_number", phoneNumber);

                    if (email != null)
                        body.put("email", email);
                } catch (JSONException e) {
                    throw new RuntimeException("Unexpected JSON exception during body construction", e);
                }

                String url = LiveNationLibrary.getHost() + "/users/square-auth?access_token=" + Uri.encode(accessToken.getToken());
                Observable<CashSession> request = doRequest(Request.Method.POST, url, body.toString(), CashSession.class);
                request.subscribe(new Observer<CashSession>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onErrorResponse((VolleyError) e);
                    }

                    @Override
                    public void onNext(CashSession session) {
                        setSession(session);
                        callback.onResponse(session);
                    }
                });
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }

    public void deleteUser(final ApiCallback<CashResponse> callback) {
        if (persistenceProvider.loadSession() == null) {
            callback.onErrorResponse(new VolleyError("No user to delete."));
            return;
        }

        /*SquareRequest<CashResponse> request = doDeleteRequest(makeRoute("/cash"), null, CashResponse.class, new Response.Listener<CashResponse>() {
            @Override
            public void onResponse(CashResponse response) {
                callback.onResponse(response);
            }
        }, callback);

        try {
            Map<String, String> headers = request.getHeaders();
            headers.put("Authorization", context.getString(R.string.cash_debug_delete_auth_header));
            request.setHeaders(headers);
        } catch (AuthFailureError ignored) {
        }

        requestQueue.add(request);*/
    }

    public Observable<CashCustomerStatus> retrieveCustomerStatus() {
        assertSession();

        return doGetRequest(makeRoute("/cash"), null, CashCustomerStatus.class);
    }

    public Observable<CashResponse> updateUserFullName(@NonNull String name) {
        assertSession();

        JSONObject requestBody = makeRequestBody("full_name", name);
        return doPostRequest(makeRoute("/cash/name"), requestBody.toString(), CashResponse.class);
    }

    public Observable<CashResponse> requestPhoneVerification(@NonNull String phoneNumber) {
        assertSession();

        JSONObject body = makeRequestBody("phone_number", phoneNumber);
        return doPostRequest(makeRoute("/cash/phone-number"), body.toString(), CashResponse.class);
    }

    public Observable<CashResponse> verifyPhoneNumber(@NonNull String phoneNumber, @NonNull String code) {
        assertSession();

        JSONObject body = makeRequestBody("phone_number", phoneNumber,
                "verification_code", code);
        return doPostRequest(makeRoute("/cash/phone-verification"), body.toString(), CashResponse.class);
    }

    //endregion


    //region Payments

    public Observable<CashCardLinkResponse> linkCard(@NonNull CashCardLinkInfo info) {
        assertSession();

        if (!info.validateForJsonConversion())
            throw new IllegalStateException("invalid card info given");

        try {
            return doPostRequest(makeRoute("/cash/card"), info.toJsonString(), CashCardLinkResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert card info into json payload", e);
        }
    }

    public Observable<CashResponse> unlinkCard() {
        assertSession();

        return doDeleteRequest(makeRoute("/cash/card"), null, CashResponse.class);
    }

    public Observable<CashPayment> initiatePayment(@NonNull CashPayment payment) {
        assertSession();

        try {
            return doPostRequest(makeRoute("/cash/payments"), payment.toJsonString(), CashPayment.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert payment to json", e);
        }
    }

    public Observable<CashPayment> retrievePayment(@NonNull String paymentId) {
        assertSession();

        return doGetRequest(makeRoute("/cash/payments/") + paymentId, null, CashPayment.class);
    }

    public Observable<CashPayment> cancelPayment(@NonNull String paymentId) {
        return doDeleteRequest(makeRoute("/cash/payments/") + paymentId, null, CashPayment.class);
    }

    //endregion


    public interface ApiCallback<T> extends Response.Listener<T>, Response.ErrorListener {}
}
