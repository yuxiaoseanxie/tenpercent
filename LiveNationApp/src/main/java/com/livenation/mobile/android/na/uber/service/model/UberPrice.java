package com.livenation.mobile.android.na.uber.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberPrice implements Serializable {
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("currency_code")
    private String currencyCode;
    @JsonProperty("display_name")
    private String displayName;
    private String estimate;
    @JsonProperty("low_estimate")
    private Integer lowEstimate;
    @JsonProperty("high_estimate")
    private Integer highEstimate;
    @JsonProperty("surge_multiplier")
    private float surgeMultiplier;
    private int duration;
    private float distance;

    public String getProductId() {
        return productId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEstimate() {
        return estimate;
    }

    public Integer getLowEstimate() {
        return lowEstimate;
    }

    public Integer getHighEstimate() {
        return highEstimate;
    }

    public float getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public int getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }
}
