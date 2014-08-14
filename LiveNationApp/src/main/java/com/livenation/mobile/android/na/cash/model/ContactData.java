package com.livenation.mobile.android.na.cash.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;

public class ContactData implements Serializable {
    private final String id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String photoUri;

    public ContactData(@NonNull String id,
                       @NonNull String name,
                       @Nullable String email,
                       @Nullable String phoneNumber,
                       @Nullable Uri photoUri) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri != null? photoUri.toString() : null;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Uri getPhotoUri() {
        if (photoUri != null)
            return Uri.parse(photoUri);
        else
            return null;
    }

    public String getDisplayName() {
        if (!TextUtils.isEmpty(getName()))
            return getName();
        else if (!TextUtils.isEmpty(getPhoneNumber()))
            return getPhoneNumber();
        else if (!TextUtils.isEmpty(getEmail()))
            return getEmail();
        else
            return "?";
    }


    @Override
    public String toString() {
        return "ContactData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photoUri='" + photoUri + '\'' +
                '}';
    }
}
