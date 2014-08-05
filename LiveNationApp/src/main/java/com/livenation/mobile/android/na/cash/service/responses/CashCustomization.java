package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashCustomization extends CashResponse {
    @JsonProperty("statement_description")
    private String statementDescription;


    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        this.statementDescription = statementDescription;
    }


    @Override
    public String toString() {
        return "CashCustomization{" +
                "statementDescription='" + statementDescription + '\'' +
                '}';
    }
}
