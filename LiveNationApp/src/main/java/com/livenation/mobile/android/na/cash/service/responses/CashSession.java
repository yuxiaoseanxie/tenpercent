package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashSession extends CashResponse {
    @JsonProperty("access_token")
    private String accessToken;

    // This is broken. We're not going to use it.
    // @JsonProperty("expires_at")
    // @JsonDeserialize(using = IsoDateDeserializer.class)
    // private Date expiresAt;

    @JsonProperty("customer_id")
    private String customerId;

    public CashSession() {

    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "CashSession{" +
                "accessToken='" + accessToken + '\'' +
                '}';
    }
}
