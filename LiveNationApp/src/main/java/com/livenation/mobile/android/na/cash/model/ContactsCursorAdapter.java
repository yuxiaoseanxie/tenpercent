package com.livenation.mobile.android.na.cash.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import com.livenation.mobile.android.na.cash.ui.views.ContactView;
import java.io.InputStream;

public class ContactsCursorAdapter extends CursorAdapter {
    private static final Uri CONTACT_DATA_URI = ContactsContract.Data.CONTENT_URI;
    private static final String PHONE_DATA_COLUMN = getPhoneDataColumn();
    private static final String[] CONTACT_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            PHONE_DATA_COLUMN,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.Data.MIMETYPE
    };

    private static final String CONTACTS_SORT_QUERY = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";


    private final ContentResolver contentResolver;

    //region Lifecycle

    public static ContactsCursorAdapter forAllContacts(@NonNull Context context) {
        return new ContactsCursorAdapter(context, null, 0);
    }

    protected ContactsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        this.contentResolver = context.getContentResolver();

        setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence query) {
                if (TextUtils.isEmpty(query)) {
                    return contentResolver.query(CONTACT_DATA_URI,
                                                 CONTACT_PROJECTION,
                                                 null,
                                                 null,
                                                 CONTACTS_SORT_QUERY);
                } else {

                    String wildcardQuery = "%" + query.toString() + "%";
                    String selection = "(" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? OR " +
                            ContactsContract.CommonDataKinds.Email.DATA + " LIKE ? OR " +
                            PHONE_DATA_COLUMN + " LIKE ?) AND (" +
                            ContactsContract.Data.MIMETYPE + " = ? OR " +
                            ContactsContract.Data.MIMETYPE + " = ?)";

                            String[] selectionArgs = new String[] { wildcardQuery, wildcardQuery, wildcardQuery,
                                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
                    return contentResolver.query(CONTACT_DATA_URI,
                                                 CONTACT_PROJECTION,
                                                 selection,
                                                 selectionArgs,
                                                 CONTACTS_SORT_QUERY);
                }
            }
        });
    }

    //endregion


    //region Subqueries

    public ContactData getContactAt(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);

        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));


        /**
         * This cursor returns data accros many contact tables using joins
         *
         * This means that the generic 'data1' and 'data2' columns differ in meaning between rows.
         *
         * For this reason, we can not extract both the phone and email from a single row, and
         * instead have to identify the row type, and then extract the relevant data for that row.
         */
        String phone = null;
        String email = null;
        if (ContactsContract.CommonDataKinds.Phone.MIMETYPE.equals(mimeType)) {
            phone = cursor.getString(cursor.getColumnIndex(PHONE_DATA_COLUMN));
        }
        if (ContactsContract.CommonDataKinds.Email.MIMETYPE.equals(mimeType)) {
            email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        }

        Uri photoUri = getPhotoUri(id);

        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        return new ContactData(id, displayName, email, phone, photoUri);
    }

    private Uri getPhotoUri(String id) {
        String selection = (ContactsContract.Data.CONTACT_ID + "= ? AND "
                          + ContactsContract.Data.MIMETYPE + "='"
                          + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'");
        String[] selectionArgs = new String[] { id };
        Cursor photoCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                                                   null,
                                                   selection,
                                                   selectionArgs,
                                                   null);
        if (photoCursor != null) {
            Uri photoUri = null;
            if (photoCursor.moveToFirst()) {
                photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
            }
            photoCursor.close();
            return photoUri;
        } else {
            return null;
        }
    }

    //endregion

    //region compatibility

    private static String getPhoneDataColumn() {
        if (Build.VERSION.SDK_INT >= 16) {
            return ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER;
        }
        return ContactsContract.CommonDataKinds.Phone.DATA;
    }

    //endregion

    //region Overrides

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public Object getItem(int position) {
        // This seemingly doesn't break the cursor, who knew?
        return getContactAt(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ContactView contactView = new ContactView(context);
        contactView.setBigDetails("");
        return contactView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(PHONE_DATA_COLUMN));
        String smallDetails = null;

        if (!TextUtils.isEmpty(email)) {
            smallDetails = email;
        }
        if (!TextUtils.isEmpty(phoneNumber)) {
            smallDetails = phoneNumber;
        }

        ContactView contactView = (ContactView) view;
        contactView.setName(displayName);
        contactView.setSmallDetails(smallDetails);

        Uri photoUri = getPhotoUri(id);
        if (photoUri != null) {
            InputStream contactPhotoStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, photoUri, true);
            if (contactPhotoStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(contactPhotoStream);
                contactView.getPhotoImageView().setImageBitmap(bitmap);
            } else {
                contactView.getPhotoImageView().setImageDrawable(null);
            }
        } else {
            contactView.getPhotoImageView().setImageDrawable(null);
        }
    }

    //endregion
}
