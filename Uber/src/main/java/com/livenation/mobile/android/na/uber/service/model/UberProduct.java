package com.livenation.mobile.android.na.uber.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberProduct {
    private int capacity;
    private String image;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("product_id")
    private String productId;
    private String description;

    public int getCapacity() {
        return capacity;
    }

    public String getImage() {
        return image;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProductId() {
        return productId;
    }

    public String getDescription() {
        return description;
    }
}
