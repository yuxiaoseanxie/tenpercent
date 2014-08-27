package com.livenation.mobile.android.na.cash.service.responses;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.livenation.mobile.android.na.cash.model.CashUtils;

public class CashCustomization extends CashResponse {
    public static int MAX_SUBJECT_LENGTH = 87;
    public static int MAX_CAPTION_LENGTH = 255;
    public static int MAX_STATEMENT_LENGTH = 38;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("button")
    private CashButton button;

    @JsonProperty("amount_caption")
    private String amountCaption;

    @JsonProperty("statement_description")
    private String statementDescription;


    public CashCustomization(String statementDescription, String caption) {
        this.statementDescription = statementDescription;
        this.subject = statementDescription;
        this.amountCaption = caption;
    }

    public CashCustomization() {

    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        if (subject != null && subject.length() > MAX_SUBJECT_LENGTH) {
            Log.w(CashUtils.LOG_TAG, "Subject exceeding max length " + MAX_SUBJECT_LENGTH + " passed to CashCustomization, truncating.");
            subject = subject.substring(0, MAX_SUBJECT_LENGTH);
        }

        this.subject = subject;
    }

    public String getAmountCaption() {
        return amountCaption;
    }

    public void setAmountCaption(String amountCaption) {
        if (amountCaption != null && amountCaption.length() > MAX_CAPTION_LENGTH) {
            Log.w(CashUtils.LOG_TAG, "Amount caption exceeding max length " + MAX_CAPTION_LENGTH + " passed to CashCustomization, truncating.");
            amountCaption = amountCaption.substring(0, MAX_CAPTION_LENGTH);
        }

        this.amountCaption = amountCaption;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        if (statementDescription != null && statementDescription.length() > MAX_STATEMENT_LENGTH) {
            Log.w(CashUtils.LOG_TAG, "Statement description exceeding max length " + MAX_STATEMENT_LENGTH + " passed to CashCustomization, truncating.");
            statementDescription = statementDescription.substring(0, MAX_STATEMENT_LENGTH);
        }

        this.statementDescription = statementDescription;
    }

    public CashButton getButton() {
        return button;
    }

    public void setButton(CashButton button) {
        this.button = button;
    }

    @Override
    public String toString() {
        return "CashCustomization{" +
                "subject='" + subject + '\'' +
                ", amountCaption='" + amountCaption + '\'' +
                ", statementDescription='" + statementDescription + '\'' +
                '}';
    }
}
