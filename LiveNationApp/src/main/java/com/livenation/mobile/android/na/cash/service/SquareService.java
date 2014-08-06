package com.livenation.mobile.android.na.cash.service;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkInfo;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SquareService {
    private static final String CLIENT_ID = "a2jqttf932pokmmkp0xtzz8ku";
    private static final String CLIENT_SECRET = "31842a1e8aba240fcc85c20d2ed74f83";
    private static final String AUTHORITY = "cash.square-sandbox.com";

    private final RequestQueue requestQueue;
    private CashSession session;

    private SquareService(@NonNull RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }
    private static SquareService instance = null;
    public static SquareService getInstance() {
        return instance;
    }
    public static void init(@NonNull RequestQueue requestQueue) {
        instance = new SquareService(requestQueue);
    }

    //region Building Requests

    private String makeUrl(@NonNull String route, @Nullable Map<String, String> params) {
        Uri.Builder builder = new Uri.Builder();

        // TODO: This is really dangerous
        builder.scheme("http");
        builder.encodedAuthority(AUTHORITY);
        builder.appendEncodedPath(route);

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet())
                builder.appendQueryParameter(param.getKey(), param.getValue());
        }

        return builder.build().toString();
    }

    private <T extends CashResponse> void injectHeaders(SquareRequest<T> request) {
        String authorizationHeader = "Client " + CLIENT_ID + " " + CLIENT_SECRET;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", authorizationHeader);
        headers.put("Content-Type", "application/json");
        request.setHeaders(headers);
    }


    private <T extends CashResponse> SquareRequest<T> makeRequest(int method,
                                                                  @NonNull String url,
                                                                  @Nullable String requestBody,
                                                                  @NonNull Class<T> responseClass,
                                                                  @NonNull Response.Listener<T> listener,
                                                                  @NonNull Response.ErrorListener errorListener) {
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

    public @Nullable CashSession getSession() {
        return session;
    }

    public boolean hasSession() {
        return (getSession() != null);
    }

    protected JSONObject makeRequestBody(String... args) {
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
                        Log.e(getClass().getSimpleName(), "Error constructing request args");
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

    public void startSession(String email, String phoneNumber, final ApiCallback<CashSession> callback) {
        if (email == null && phoneNumber == null)
            throw new IllegalArgumentException("email or phoneNumber must be supplied");

        JSONObject body = makeRequestBody("response_type", "token",
                                          "client_id", CLIENT_ID);
        try {
            if (phoneNumber != null)
                body.put("phone_number", phoneNumber);

            if (email != null)
                body.put("email", email);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSON exception during body construction", e);
        }

        SquareRequest<CashSession> request = makePostRequest("oauth2/authorize/cash", body.toString(), CashSession.class, new Response.Listener<CashSession>() {
            @Override
            public void onResponse(CashSession response) {
                SquareService.this.session = response;
                callback.onResponse(response);
            }
        }, callback);
        requestQueue.add(request);
    }

    public void retrieveCustomerStatus(ApiCallback<CashCustomerStatus> callback) {
        assertSession();

        SquareRequest<CashCustomerStatus> request = makeGetRequest("v1/" + getEncodedCustomerId() + "/cash", null, CashCustomerStatus.class, callback, callback);
        requestQueue.add(request);
    }

    public void verifyPhoneNumber(@NonNull String phoneNumber, ApiCallback<CashResponse> callback) {
        assertSession();

        String customerId = Uri.encode(session.getCustomerId());
        JSONObject body = makeRequestBody("phone_number", phoneNumber);
        SquareRequest<CashResponse> request = makePostRequest("v1/" + getEncodedCustomerId() + "/cash/phone-number", body.toString(), CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    //endregion


    //region Payments

    public void linkCard(@NonNull CashCardLinkInfo info, ApiCallback<CashCardLinkResponse> callback) {
        assertSession();

        if (!info.validateForJsonConversion())
            throw new IllegalStateException("invalid card info given");

        try {
            SquareRequest<CashCardLinkResponse> request = makePostRequest("v1/" + getEncodedCustomerId() + "/cash/card", info.toJsonString(), CashCardLinkResponse.class, callback, callback);
            requestQueue.add(request);
        } catch (IOException e) {

        }
    }

    public void unlinkCard(ApiCallback<CashResponse> callback) {
        assertSession();

        SquareRequest<CashResponse> request = makeDeleteRequest("v1/" + getEncodedCustomerId() + "/cash/card", null, CashResponse.class, callback, callback);
        requestQueue.add(request);
    }

    public void initiatePayment(@NonNull CashPayment payment, ApiCallback<CashPayment> callback) {
        assertSession();

        try {
            SquareRequest<CashPayment> request = makePostRequest("v1/" + getEncodedCustomerId() + "/cash/payments", payment.toJsonString(), CashPayment.class, callback, callback);
            requestQueue.add(request);
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert payment to json", e);
        }
    }

    public void retrievePayment(@NonNull String paymentId, ApiCallback<CashPayment> callback) {
        assertSession();

        SquareRequest<CashPayment> request = makeGetRequest("v1/" + getEncodedCustomerId() + "/cash/payments/" + paymentId, null, CashPayment.class, callback, callback);
        requestQueue.add(request);
    }

    //endregion


    public interface ApiCallback<T> extends Response.Listener<T>, Response.ErrorListener {}
}
