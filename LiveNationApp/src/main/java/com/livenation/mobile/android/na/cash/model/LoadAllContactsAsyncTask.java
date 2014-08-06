package com.livenation.mobile.android.na.cash.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class LoadAllContactsAsyncTask extends AsyncTask<Void, Void, ArrayList<ContactData>> {
    private final ContentResolver contentResolver;
    private final DataCallback<ArrayList<ContactData>> callback;

    public LoadAllContactsAsyncTask(@NonNull ContentResolver contentResolver, @NonNull DataCallback<ArrayList<ContactData>> callback) {
        this.contentResolver = contentResolver;
        this.callback = callback;
    }


    private ArrayList<String> getEmails(@NonNull String id) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Email.DATA
        };
        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
        String[] selectionArgs = new String[] { id };
        Cursor cursorEmail = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                                   projection,
                                                   selection,
                                                   selectionArgs,
                                                   null);

        ArrayList<String> emails = new ArrayList<String>();
        if (cursorEmail.moveToNext()) {
            String email = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emails.add(email);
        }
        cursorEmail.close();

        return emails;
    }

    private ArrayList<PhoneNumber> getPhoneNumbers(@NonNull String id) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = new String[] { id };
        Cursor cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                   projection,
                                                   selection,
                                                   selectionArgs,
                                                   null);

        ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
        if (cursorPhone.moveToNext()) {
            String displayName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumbers.add(new PhoneNumber(number, displayName));
        }
        cursorPhone.close();

        return phoneNumbers;
    }

    private Uri getPhotoUri(String id) {
        try {
            Cursor cur = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null
            );
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    @Override
    protected ArrayList<ContactData> doInBackground(Void... voids) {
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String sortQuery = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                                              projection,
                                              null,
                                              null,
                                              sortQuery);
        ArrayList<ContactData> accumulator = new ArrayList<ContactData>();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            ArrayList<String> emails = getEmails(id);
            ArrayList<PhoneNumber> phoneNumbers = getPhoneNumbers(id);
            if (emails.isEmpty() && phoneNumbers.isEmpty()) {
                continue;
            }

            Uri photoUri = getPhotoUri(id);
            String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            accumulator.add(new ContactData(id, displayName, emails, phoneNumbers, photoUri));
        }
        cursor.close();

        return accumulator;
    }

    @Override
    protected void onPostExecute(ArrayList<ContactData> contacts) {
        callback.onDataReady(contacts);
    }
}
