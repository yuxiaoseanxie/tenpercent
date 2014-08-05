package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashPaymentBlockers extends CashResponse {
    @JsonProperty("url")
    private String url;

    @JsonProperty(value = "card", required = false)
    private CashCardSummary card;

    @JsonProperty(value = "phone_number", required = false)
    private Object phoneNumber;

    @JsonProperty(value = "passcode_verification", required = false)
    private Object passcodeVerification;


    public String getUrl() {
        return url;
    }

    public CashCardSummary getCard() {
        return card;
    }

    public Object getPhoneNumber() {
        return phoneNumber;
    }

    public Object getPasscodeVerification() {
        return passcodeVerification;
    }


    @Override
    public String toString() {
        return "CashPaymentBlockers{" +
                "url='" + url + '\'' +
                ", card=" + card +
                ", phoneNumber=" + phoneNumber +
                ", passcodeVerification=" + passcodeVerification +
                '}';
    }
}
