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
import com.livenation.mobile.android.na.R;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        builder.scheme("http");
        builder.encodedAuthority(context.getString(R.string.square_cash_environment));
        builder.appendEncodedPath(route);

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet())
                builder.appendQueryParameter(param.getKey(), param.getValue());
        }

        return builder.build().toString();
    }

    private <T extends CashResponse> void injectHeaders(SquareRequest<T> request) {
        HashMap<String, String> headers = new HashMap<String, String>();
        if (hasSession()) {
            headers.put("Authorization", "Bearer " + session.getAccessToken());
        }
        headers.put("Content-Type", "application/json");
        request.setHeaders(headers);
    }


    private <T extends CashResponse> SquareRequest<T> makeRequest(int method,
                                                                  @NonNull String url,
                                                                  @Nullable String requestBody,
                                                                  @NonNull Class<T> responseClass,
                                                                  @NonNull Response.Listener<T> listener,
                                                                  @NonNull Response.ErrorListener errorListener) {
        Log.i(CashUtils.LOG_TAG, "Outgoing request to '" + url + "' with body: " + requestBody);

        SquareRequest<T> request = new SquareRequest<T>(method, url, responseClass, requestBody, listener, errorListener);
        injectHeaders(request);
        return request;
    }

    private <T extends CashResponse> SquareRequest<T> makeGetRequest(@NonNull String route,
                                                                     @Nullable Map<String, String> params,
                                                                     @NonNull Class<T> responseClass,
                                                                     @NonNull Response.Listener<T> listener,
                                                                     @NonNull Response.ErrorListener errorListener) {
        return makeRequest(SquareRequest.Method.GET, makeUrl(route, params), null, responseClass, listener, errorListener);
    }

    private <T extends CashResponse> SquareRequest<T> makeDeleteRequest(@NonNull String route,
                                                                        @Nullable Map<String, String> params,
                                                                        @NonNull Class<T> responseClass,
                                                                        @NonNull Response.Listener<T> listener,
                                                                        @NonNull Response.ErrorListener errorListener) {
        return makeRequest(SquareRequest.Method.DELETE, makeUrl(route, params), null, responseClass, listener, errorListener);
    }

    private <T extends CashResponse> SquareRequest<T> makePostRequest(@NonNull String route,
                                                                      @Nullable String requestBody,
                                                                      @NonNull Class<T> responseClass,
                                                                      @NonNull Response.Listener<T> listener,
                                                                      @NonNull Response.ErrorListener errorListener) {
        return makeRequest(SquareRequest.Method.POST, makeUrl(route, null), requestBody, responseClass, listener, errorListener);
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
        return (getSession() != null &&
                getSession().getExpiresAt() != null &&
                new Date().before(getSession().getExpiresAt()));
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
                    if (phoneNumber != null)
                        body.put("phone_number", phoneNumber);

                    if (email != null)
                        body.put("email", email);
                } catch (JSONException e) {
                    throw new RuntimeException("Unexpected JSON exception during body construction", e);
                }

                String url = LiveNationLibrary.getHost() + "/users/square-auth?access_token=" + Uri.encode(accessToken.getToken());
                SquareRequest<CashSession> request = makeRequest(Request.Method.POST, url, body.toString(), CashSession.class, new Response.Listener<CashSession>() {
                    @Override
                    public void onResponse(CashSession response) {
                        setSession(response);
                        callback.onResponse(response);
                    }
                }, callback);
                requestQueue.add(request);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }

    public void retrieveCustomerStatus(ApiCallback<CashCustomerStatus> callback) {
        assertSession();

        SquareRequest<CashCustomerStatus> request = makeGetRequest(makeRoute("/cash"), null, CashCustomerStatus.class, callback, callback);
        requestQueue.add(request);
    }

    public void updateUserFullName(@NonNull String name, ApiCallback<CashResponse> callback) {
        assertSession();

        JSONObject requestBody = makeRequestBody("full_name", name);
        SquareRequest<CashResponse> request = makePostRequest(makeRoute("/cash/name"), requestBody.toString(), CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    public void requestPhoneVerification(@NonNull String phoneNumber, ApiCallback<CashResponse> callback) {
        assertSession();

        JSONObject body = makeRequestBody("phone_number", phoneNumber);
        SquareRequest<CashResponse> request = makePostRequest(makeRoute("/cash/phone-number"), body.toString(), CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    public void verifyPhoneNumber(@NonNull String phoneNumber, @NonNull String code, ApiCallback<CashResponse> callback) {
        assertSession();

        JSONObject body = makeRequestBody("phone_number", phoneNumber,
                                          "verification_code", code);
        SquareRequest<CashResponse> request = makePostRequest(makeRoute("/cash/phone-verification"), body.toString(), CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    //endregion


    //region Payments

    public void linkCard(@NonNull CashCardLinkInfo info, ApiCallback<CashCardLinkResponse> callback) {
        assertSession();

        if (!info.validateForJsonConversion())
            throw new IllegalStateException("invalid card info given");

        try {
            SquareRequest<CashCardLinkResponse> request = makePostRequest(makeRoute("/cash/card"), info.toJsonString(), CashCardLinkResponse.class, callback, callback);
            requestQueue.add(request);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert card info into json payload", e);
        }
    }

    public void unlinkCard(ApiCallback<CashResponse> callback) {
        assertSession();

        SquareRequest<CashResponse> request = makeDeleteRequest(makeRoute("/cash/card"), null, CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    public void initiatePayment(@NonNull CashPayment payment, ApiCallback<CashPayment> callback) {
        assertSession();

        try {
            SquareRequest<CashPayment> request = makePostRequest(makeRoute("/cash/payments"), payment.toJsonString(), CashPayment.class, callback, callback);
            requestQueue.add(request);
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert payment to json", e);
        }
    }

    public void retrievePayment(@NonNull String paymentId, ApiCallback<CashPayment> callback) {
        assertSession();

        SquareRequest<CashPayment> request = makeGetRequest(makeRoute("/cash/payments/") + paymentId, null, CashPayment.class, callback, callback);
        requestQueue.add(request);
    }

    //endregion


    public interface ApiCallback<T> extends Response.Listener<T>, Response.ErrorListener {}
    public interface CustomerIdProvider {
        void provideSquareCustomerId(Response.Listener<String> onResponse);
    }
}
