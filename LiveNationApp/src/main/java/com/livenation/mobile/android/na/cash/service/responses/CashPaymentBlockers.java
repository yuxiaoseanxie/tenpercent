package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashPaymentBlockers extends CashResponse {
    @JsonProperty("url")
    private String url;

    @JsonProperty("card")
    private CashCardSummary card;

    @JsonProperty("passcode_verification")
    private Object passcodeVerification;


    public String getUrl() {
        return url;
    }

    public CashCardSummary getCard() {
        return card;
    }

    public Object getPasscodeVerification() {
        return passcodeVerification;
    }

    @Override
    public String toString() {
        return "CashCardBlockers{" +
                "url='" + url + '\'' +
                ", card=" + card +
                ", passcodeVerification=" + passcodeVerification +
                '}';
    }
}
