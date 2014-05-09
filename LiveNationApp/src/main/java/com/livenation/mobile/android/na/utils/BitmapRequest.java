package com.livenation.mobile.android.na.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by elodieferrais on 5/9/14.
 */

public class BitmapRequest extends Request<Bitmap> {

    private final Response.Listener<Bitmap> listener;

    public BitmapRequest(int method, String url, Response.ErrorListener errorListener, Response.Listener<Bitmap> listener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        InputStream stream = new ByteArrayInputStream(response.data);
        Bitmap img = BitmapFactory.decodeStream(stream);
        return Response.success(img, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Bitmap response) {
        listener.onResponse(response);
    }
}


