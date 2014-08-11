package com.livenation.mobile.android.na.cash.service.responses;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.livenation.mobile.android.na.cash.model.CashUtils;

public class CashCustomization extends CashResponse {
    public static int MAX_LENGTH = 38;

    @JsonProperty("statement_description")
    private String statementDescription;


    public CashCustomization(String statementDescription) {
        this.statementDescription = statementDescription;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        if (statementDescription.length() > MAX_LENGTH) {
            Log.w(CashUtils.LOG_TAG, "Statement description exceeding max length " + MAX_LENGTH + " passed to CashCustomization, truncating.");
            statementDescription = statementDescription.substring(0, MAX_LENGTH);
        }

        this.statementDescription = statementDescription;
    }


    @Override
    public String toString() {
        return "CashCustomization{" +
                "statementDescription='" + statementDescription + '\'' +
                '}';
    }
}
