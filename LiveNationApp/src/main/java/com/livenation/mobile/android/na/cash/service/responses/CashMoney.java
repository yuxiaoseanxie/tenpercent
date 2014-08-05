package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CashMoney extends CashResponse {
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency_code")
    private String currencyCode;


    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
