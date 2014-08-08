package com.livenation.mobile.android.na.cash.model;

import java.io.Serializable;

public class PhoneNumber implements Serializable {
    private final String displayName;
    private final String phoneNumber;

    public PhoneNumber(String phoneNumber, String displayName) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
    }


    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    @Override
    public String toString() {
        return "PhoneNumber{" +
                "displayName='" + displayName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
