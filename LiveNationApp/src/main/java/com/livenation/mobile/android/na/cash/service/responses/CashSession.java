package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IsoDateDeserializer;

import java.util.Date;

public class CashSession extends CashResponse {
    public static final String TOKEN_TYPE_BEARER = "bearer";

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_at")
    @JsonDeserialize(using = IsoDateDeserializer.class)
    private Date expiresAt;

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

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
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
                ", expiresAt='" + expiresAt + '\'' +
                '}';
    }
}
