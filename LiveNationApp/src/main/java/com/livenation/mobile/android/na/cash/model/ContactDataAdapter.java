package com.livenation.mobile.android.na.cash.model;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.ui.views.ContactView;

import java.io.InputStream;
import java.util.ArrayList;

public class ContactDataAdapter extends ArrayAdapter<ContactData> {
    private final ContentResolver contentResolver;

    private final DataProvider dataProvider;

    public ContactDataAdapter(@NonNull Context context, @NonNull DataProvider dataProvider, ArrayList<ContactData> contacts) {
        super(context, R.layout.view_cash_contact, contacts);

        this.contentResolver = context.getContentResolver();
        this.dataProvider = dataProvider;
    }

    public ContactDataAdapter(@NonNull Context context, @NonNull DataProvider dataProvider) {
        this(context, dataProvider, new ArrayList<ContactData>());
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactView view = (ContactView) convertView;
        if (view == null) {
            view = new ContactView(getContext());
        }

        ContactData contactData = getItem(position);
        view.setName(contactData.getDisplayName());
        if (contactData.getPhotoUri() != null) {
            InputStream contactPhotoStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactData.getPhotoUri(), true);
            if (contactPhotoStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(contactPhotoStream);
                view.getPhotoImageView().setImageBitmap(bitmap);
            } else {
                view.getPhotoImageView().setImageDrawable(contactData.makePlaceholderImage());
            }
        } else {
            view.getPhotoImageView().setImageDrawable(contactData.makePlaceholderImage());
        }

        view.setSmallDetails(dataProvider.getSmallDetails(position, contactData));
        view.setBigDetails(dataProvider.getBigDetails(position, contactData));

        return view;
    }

    public interface DataProvider {
        @NonNull String getSmallDetails(int position, @NonNull ContactData contact);
        @NonNull String getBigDetails(int position, @NonNull ContactData contact);
    }
}
