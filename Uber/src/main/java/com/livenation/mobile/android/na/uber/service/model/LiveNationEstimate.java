package com.livenation.mobile.android.na.uber.service.model;

import android.content.res.Resources;

/**
 * Created by cchilton on 11/18/14.
 *
 * Wrapper class for List Adapters.
 *
 * This class wraps both the Uber Product model and the Uber Price model.
 *
 * We need this to show the car's person capacity of each Uber Estimate to our users
 */
public class LiveNationEstimate {
    private final UberProduct product;
    private final UberPrice price;

    public LiveNationEstimate(UberPrice price, UberProduct uberProduct) {
        this.price = price;
        this.product = uberProduct;
    }

    public UberProduct getProduct() {
        return product;
    }

    public UberPrice getPrice() {
        return price;
    }

    public boolean hasProduct() {
        return product != null;
    }

}
