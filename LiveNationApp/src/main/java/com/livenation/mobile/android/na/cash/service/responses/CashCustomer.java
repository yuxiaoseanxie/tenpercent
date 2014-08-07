package com.livenation.mobile.android.na.cash.service.responses;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

public class CashCustomer extends CashResponse {
    public static final String ROLE_RECIPIENT = "RECIPIENT";
    public static final String ROLE_SENDER = "SENDER";

    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;


    public static CashCustomer fromContactData(@NonNull ContactData contact) {
        CashCustomer customer = new CashCustomer();
        if (!TicketingUtils.isCollectionEmpty(contact.getPhoneNumbers()))
            customer.setPhoneNumber(contact.getPhoneNumbers().get(0).getPhoneNumber());
        if (!TicketingUtils.isCollectionEmpty(contact.getEmails()))
            customer.setEmail(contact.getEmails().get(0));
        return customer;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
