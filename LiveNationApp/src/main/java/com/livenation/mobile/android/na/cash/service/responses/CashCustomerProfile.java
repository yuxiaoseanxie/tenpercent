package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashCustomerProfile extends CashResponse {
    @JsonProperty("card_summary")
    private CashCardSummary cashCardSummary;

    @JsonProperty("passcode_confirmation_enabled")
    private boolean passcodeConfirmationEnabled;


    public CashCardSummary getCardSummary() {
        return cashCardSummary;
    }

    public boolean isPasscodeConfirmationEnabled() {
        return passcodeConfirmationEnabled;
    }

    @Override
    public String toString() {
        return "CardCustomerProfile{" +
                "cardSummary=" + cashCardSummary +
                ", passcodeConfirmationEnabled=" + passcodeConfirmationEnabled +
                '}';
    }
}
