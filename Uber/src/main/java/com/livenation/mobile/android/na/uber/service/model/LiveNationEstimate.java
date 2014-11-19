package com.livenation.mobile.android.na.uber.service.model;

import java.io.Serializable;

/**
 * Created by cchilton on 11/18/14.
 * <p/>
 * Wrapper class for List Adapters.
 * <p/>
 * This class wraps both the Uber Product model and the Uber Price model.
 * <p/>
 * We need this to show the car's person capacity of each Uber Estimate to our users
 */
public class LiveNationEstimate implements Serializable {
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
