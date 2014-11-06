package com.livenation.mobile.android.na.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by elodieferrais on 5/9/14.
 */

public class BitmapRequest extends Request<Bitmap> {

    private final Response.Listener<Bitmap> listener;


    public BitmapRequest(int method, String url, Response.ErrorListener errorListener, Response.Listener<Bitmap> listener) {
        super(method, url, errorListener);
        this.listener = listener;
        setShouldCache(true);
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        try {
            InputStream stream = new ByteArrayInputStream(response.data);
            Bitmap img = BitmapFactory.decodeStream(stream);
            stream.close();
            return Response.success(img, HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            return Response.error(new VolleyError(e));
        }
    }

    @Override
    protected void deliverResponse(Bitmap response) {
        listener.onResponse(response);
    }
}


