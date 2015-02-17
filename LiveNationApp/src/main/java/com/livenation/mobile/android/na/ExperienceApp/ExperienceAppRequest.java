package com.livenation.mobile.android.na.ExperienceApp;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import android.net.Uri;

public class ExperienceAppRequest extends JsonRequest<Boolean> {
    private static final String API_HOST = "api.expapp.com";
    private static final String API_PATH = "/v1/externalEvents/search";
    private static final String JSON_SUCCESS_FIELD = "success";

    public ExperienceAppRequest(String eventId,
                                Response.Listener<Boolean> success,
                                Response.ErrorListener failure) {
        super(Request.Method.GET, generateUrl(eventId), null, success, failure);
    }

    private static String generateUrl(String eventId) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .encodedAuthority(API_HOST)
                .encodedPath(API_PATH);

        builder.appendQueryParameter("externalEventId", eventId)
                .appendQueryParameter("ticketSystemId", "5");

        return builder.build().toString();
    }

    @Override
    protected Response<Boolean> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject json = new JSONObject(responseString);
            if (json.has(JSON_SUCCESS_FIELD)) {
                boolean success = json.getBoolean(JSON_SUCCESS_FIELD);
                return Response.success(success, HttpHeaderParser.parseCacheHeaders(response));
            }
            return Response.error(new VolleyError("Error decoding experience response: no success field"));
        } catch (JSONException e) {
            return Response.error(new VolleyError("JSONException: " + e.getMessage(), e));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new VolleyError("UnsupportedEncodingException: " + e.getMessage(), e));
        }
    }


}
