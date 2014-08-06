package com.livenation.mobile.android.na.cash.service.responses;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashCardLinkInfo extends CashResponse {
    @JsonProperty("pan")
    private String number;

    @JsonProperty("expiration")
    private String expiration;

    @JsonProperty("security_code")
    private String securityCode;

    @JsonProperty("postal_code")
    private String postalCode;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String month, String day) {
        this.expiration = month + day;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }


    @Override
    public boolean validateForJsonConversion() {
        return (!TextUtils.isEmpty(getNumber()) &&
                !TextUtils.isEmpty(getExpiration()) &&
                !TextUtils.isEmpty(getSecurityCode()) &&
                !TextUtils.isEmpty(getPostalCode()));
    }

    @Override
    public String toString() {
        return "CashCardLinkInfo{" +
                "number='" + number + '\'' +
                ", expiration='" + expiration + '\'' +
                ", securityCode='" + securityCode + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
