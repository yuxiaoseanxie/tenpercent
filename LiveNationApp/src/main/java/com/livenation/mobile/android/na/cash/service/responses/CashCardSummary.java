package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashCardSummary extends CashResponse {
    public static final String BRAND_UNKNOWN = "UNKNOWN";
    public static final String BRAND_VISA = "VISA";
    public static final String BRAND_MASTER_CARD = "MASTER_CARD";
    public static final String BRAND_AMERICAN_EXPRESS = "AMERICAN_EXPRESS";
    public static final String BRAND_DISCOVER = "DISCOVER";
    public static final String BRAND_DISCOVER_DINERS = "DISCOVER_DINERS";
    public static final String BRAND_JCB = "JCB";


    @JsonProperty("brand")
    private String brand;

    @JsonProperty("pan_suffix")
    private String suffix;


    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }


    @Override
    public String toString() {
        return "CardSummary{" +
                "brand='" + brand + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }
}
