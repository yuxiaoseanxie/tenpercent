package com.livenation.mobile.android.na.cash.service;

import android.support.annotation.NonNull;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import rx.subjects.Subject;

public class VolleyObservableAdapter<T> implements Response.Listener<T>, Response.ErrorListener {
    private final Subject<T, T> subject;

    public VolleyObservableAdapter(@NonNull Subject<T, T> subject) {
        this.subject = subject;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        subject.onError(error);
        subject.onCompleted();
    }

    @Override
    public void onResponse(T response) {
        subject.onNext(response);
        subject.onCompleted();
    }
}
