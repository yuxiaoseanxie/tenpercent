package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;

public class CashCustomerStatus extends CashResponse {
    @JsonProperty("customer")
    private CashCustomer customer;

    @JsonProperty("payments")
    @JsonDeserialize(contentAs = CashPayment.class)
    private ArrayList<CashPayment> payments;

    @JsonProperty("blockers")
    private CashPaymentBlockers blockers;

    @JsonProperty("profile")
    private CashCustomerProfile profile;


    public CashCustomer getCustomer() {
        return customer;
    }

    public ArrayList<CashPayment> getPayments() {
        return payments;
    }

    public CashPaymentBlockers getBlockers() {
        return blockers;
    }

    public CashCustomerProfile getProfile() {
        return profile;
    }


    @Override
    public String toString() {
        return "CashCustomerStatus{" +
                "customer=" + customer +
                ", payments=" + payments +
                ", blockers=" + blockers +
                ", profile=" + profile +
                '}';
    }
}
