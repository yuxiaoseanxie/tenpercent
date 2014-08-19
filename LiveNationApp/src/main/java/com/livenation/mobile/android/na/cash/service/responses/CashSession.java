package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashSession extends CashResponse {
    public static final String TOKEN_TYPE_BEARER = "bearer";

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("state")
    private String state;

    @JsonProperty("token_type")
    private String tokenType;


    public CashSession() {

    }


    public String getAccessToken() {
        return accessToken;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getState() {
        return state;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
