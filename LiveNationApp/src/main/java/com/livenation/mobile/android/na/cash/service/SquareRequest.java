package com.livenation.mobile.android.na.cash.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

public class SquareRequest<T extends CashResponse> extends JsonRequest<T> {
    private final Class<T> responseClass;
    private Map<String, String> headers = Collections.emptyMap();

    public SquareRequest(int method,
                         String url,
                         Class<T> responseClass,
                         String requestBody,
                         Response.Listener<T> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);

        this.responseClass = responseClass;
    }


    public Class<T> getResponseClass() {
        return responseClass;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    public void setHeaders(@NonNull Map<String, String> headers) {
        this.headers = headers;
    }


    protected Response<T> parseSuccess(String body, NetworkResponse response) {
        try {
            T result = CashResponse.fromJsonString(body, getResponseClass());
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            return Response.error(new VolleyError("Parse error", e));
        }
    }

    protected VolleyError parseError(String body, VolleyError outerError) {
        try {
            JSONObject json = new JSONObject(body);
            Log.e(CashUtils.LOG_TAG, "Error for '" + getUrl() + "': " + body);

            // Temporary support for crashes on their server.
            if (json.has("message")) {
                String errorMessage = json.getString("message");
                return new VolleyError(errorMessage, outerError);
            }

            String error = json.getString("error");
            String description = json.optString("error_description");
            return new VolleyError(error + ": " + description, outerError);
        } catch (JSONException e) {
            if (outerError != null) {
                Log.w(CashUtils.LOG_TAG, "Json parsing error for error, ignoring.", e);
                return outerError;
            } else {
                return new VolleyError("Malformed error response", e);
            }
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String body = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.i(CashUtils.LOG_TAG, "Response for '" + getUrl() + "': " + body);

            if (response.statusCode == 200) {
                return parseSuccess(body, response);
            } else {
                return Response.error(parseError(body, null));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new VolleyError("Unsupported encoding in response", e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        NetworkResponse response = volleyError.networkResponse;
        if (response != null) {
            try {
                String body = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return parseError(body, volleyError);
            } catch (UnsupportedEncodingException ignored) {
            }
        }

        return super.parseNetworkError(volleyError);
    }
}
