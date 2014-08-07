package com.livenation.mobile.android.na.cash.model;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.CircularImageView;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContactDataAdapter extends FilteredArrayAdapter<ContactData> {
    private final LayoutInflater inflater;
    private final ContentResolver contentResolver;

    private final DataProvider dataProvider;

    public ContactDataAdapter(@NonNull Context context, @NonNull DataProvider dataProvider, ArrayList<ContactData> contacts) {
        super(context, R.layout.list_cash_contact, contacts);

        this.inflater = LayoutInflater.from(context);
        this.contentResolver = context.getContentResolver();

        this.dataProvider = dataProvider;
    }

    public ContactDataAdapter(@NonNull Context context, @NonNull DataProvider dataProvider) {
        this(context, dataProvider, new ArrayList<ContactData>());
    }


    public DataProvider getDataProvider() {
        return dataProvider;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_cash_contact, parent, false);
            view.setTag(new ViewHolder(view));
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        ContactData contactData = getItem(position);
        holder.name.setText(contactData.getDisplayName());
        if (contactData.getPhotoUri() != null) {
            InputStream contactPhotoStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactData.getPhotoUri(), true);
            if (contactPhotoStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(contactPhotoStream);
                holder.photo.setImageBitmap(bitmap);
            } else {
                holder.photo.setImageDrawable(contactData.makePlaceholderImage());
            }
        } else {
            holder.photo.setImageDrawable(contactData.makePlaceholderImage());
        }

        holder.smallDetails.setText(dataProvider.getSmallDetails(position, contactData));
        String price = dataProvider.getBigDetails(position, contactData);
        if (TextUtils.isEmpty(price)) {
            holder.bigDetails.setVisibility(View.GONE);
        } else {
            holder.bigDetails.setVisibility(View.VISIBLE);
            holder.bigDetails.setText(price);
        }

        return view;
    }

    class ViewHolder {
        @InjectView(R.id.list_cash_recipient_name) TextView name;
        @InjectView(R.id.list_cash_recipient_small_details) TextView smallDetails;
        @InjectView(R.id.list_cash_recipient_photo) CircularImageView photo;
        @InjectView(R.id.list_cash_recipient_big_details) TextView bigDetails;

        ViewHolder(@NonNull View view) {
            ButterKnife.inject(this, view);
        }
    }


    @Override
    protected boolean keepObject(ContactData contact, String mask) {
        return contact.matches(mask);
    }

    public interface DataProvider {
        @NonNull String getSmallDetails(int position, @NonNull ContactData contact);
        @NonNull String getBigDetails(int position, @NonNull ContactData contact);
    }
}
