package com.livenation.mobile.android.na.cash.service;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * This class only exists because you can't make a regular VolleyError with a message,
 * cause, <b>and</b> network response. So this class just adds support for all three.
 */
public class StupidVolleyError extends VolleyError {
    private final String reason;
    private final Throwable cause;

    public StupidVolleyError(String reason, Throwable cause, NetworkResponse networkResponse) {
        super(networkResponse);

        this.reason = reason;
        this.cause = cause;
    }

    public StupidVolleyError(String reason, NetworkResponse networkResponse) {
        this(reason, null, networkResponse);
    }

    public StupidVolleyError(String reason, VolleyError volleyError) {
        this(reason, volleyError, volleyError != null? volleyError.networkResponse : null);
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        return reason;
    }

    @Override
    public String getLocalizedMessage() {
        return reason;
    }
}
