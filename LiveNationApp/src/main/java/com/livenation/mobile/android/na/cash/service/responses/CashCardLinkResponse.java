package com.livenation.mobile.android.na.cash.service.responses;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashCardLinkResponse extends CashResponse {
    @JsonProperty("blockers")
    private CashPaymentBlockers blockers;

    // TODO: Talk to platform about whether or not we want to require this
    @JsonProperty("card_token")
    private String cardToken;

    @JsonProperty("field")
    private String errorField;

    @JsonProperty("type")
    private String errorType;

    @JsonProperty("message")
    private String errorMessage;


    public CashPaymentBlockers getBlockers() {
        return blockers;
    }

    public String getCardToken() {
        return cardToken;
    }


    public boolean isValid() {
        return (getErrorField() == null &&
                getErrorMessage() == null &&
                getErrorType() == null);
    }

    public @Nullable String getErrorField() {
        return errorField;
    }

    public @Nullable String getErrorType() {
        return errorType;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }


    @Override
    public String toString() {
        return "CashCardLinkInfo{" +
                "blockers=" + blockers +
                ", cardToken='" + cardToken + '\'' +
                ", errorField='" + errorField + '\'' +
                ", errorType='" + errorType + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
