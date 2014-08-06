package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CashMoney extends CashResponse {
    public static final String CURRENCY_CODE_USD = "USD";


    @JsonProperty("amount")
    private long amount;

    @JsonProperty("currency_code")
    private String currencyCode;


    public long getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
