package com.livenation.mobile.android.na.cash.model;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;

public class ContactData {
    private final String id;
    private final String name;
    private final ArrayList<String> emails;
    private final ArrayList<PhoneNumber> phoneNumbers;

    public ContactData(@NonNull String id, @NonNull String name, @Nullable ArrayList<String> emails, @Nullable ArrayList<PhoneNumber> phoneNumbers) {
        this.id = id;
        this.name = name;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
    }


    public Bitmap getImage() {
        return null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public ArrayList<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public String getDisplayName() {
        if (!TextUtils.isEmpty(getName()))
            return getName();
        else
            return null;
    }

    public String getDetails() {
        String emails = TextUtils.join(", ", getEmails());

        String details = "";
        if (!TextUtils.isEmpty(emails)) {
            details += emails;
        }

        String phoneNumbers = "";
        for (PhoneNumber phoneNumber : getPhoneNumbers())
            phoneNumbers += phoneNumber.getPhoneNumber() + ", ";

        if (!TextUtils.isEmpty(phoneNumbers)) {
            if (!TextUtils.isEmpty(details))
                details += "\n";

            details += phoneNumbers;
        }

        return details;
    }


    @Override
    public String toString() {
        return "ContactData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", emails=" + emails +
                ", phoneNumbers=" + phoneNumbers +
                '}';
    }
}
