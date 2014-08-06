package com.livenation.mobile.android.na.cash.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.livenation.mobile.android.na.cash.ui.CharacterDrawable;

import java.io.Serializable;
import java.util.ArrayList;

public class ContactData implements Serializable {
    private final String id;
    private final String name;
    private final ArrayList<String> emails;
    private final ArrayList<PhoneNumber> phoneNumbers;
    private final String photoUri;

    public ContactData(@NonNull String id,
                       @NonNull String name,
                       @Nullable ArrayList<String> emails,
                       @Nullable ArrayList<PhoneNumber> phoneNumbers,
                       @Nullable Uri photoUri) {
        this.id = id;
        this.name = name;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
        this.photoUri = photoUri != null? photoUri.toString() : null;
    }


    public Drawable makePlaceholderImage() {
        return new CharacterDrawable(getName().charAt(0), 0xFFeaeaea);
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

    public Uri getPhotoUri() {
        if (photoUri != null)
            return Uri.parse(photoUri);
        else
            return null;
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
                ", photoUri=" + photoUri +
                '}';
    }
}
