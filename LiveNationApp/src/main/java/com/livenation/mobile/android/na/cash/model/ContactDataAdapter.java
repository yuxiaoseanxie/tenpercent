package com.livenation.mobile.android.na.cash.model;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.CircularImageView;

import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContactDataAdapter extends ArrayAdapter<ContactData> {
    private final LayoutInflater inflater;
    private final ContentResolver contentResolver;

    private final Mode mode;
    private final DataProvider dataProvider;

    public ContactDataAdapter(@NonNull Context context, Mode mode, @NonNull DataProvider dataProvider) {
        super(context, R.layout.list_cash_contact);

        this.inflater = LayoutInflater.from(context);
        this.contentResolver = context.getContentResolver();

        this.mode = mode;
        this.dataProvider = dataProvider;
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

        switch (mode) {
            case SELECTION:
                holder.selection.setVisibility(View.VISIBLE);
                holder.bigDetails.setVisibility(View.GONE);

                if (dataProvider.isContactSelected(position, contactData))
                    holder.selection.setImageResource(R.drawable.cash_item_checkmark_on);
                else
                    holder.selection.setImageResource(R.drawable.cash_item_checkmark_off);

                break;

            case REVIEW:
                holder.selection.setVisibility(View.GONE);
                holder.bigDetails.setVisibility(View.VISIBLE);

                String price = dataProvider.getBigDetails(position, contactData);
                holder.bigDetails.setText(price);

                break;
        }

        return view;
    }

    class ViewHolder {
        @InjectView(R.id.list_cash_recipient_name) TextView name;
        @InjectView(R.id.list_cash_recipient_small_details) TextView smallDetails;
        @InjectView(R.id.list_cash_recipient_photo) CircularImageView photo;
        @InjectView(R.id.list_cash_recipient_selection) ImageView selection;
        @InjectView(R.id.list_cash_recipient_big_details) TextView bigDetails;

        ViewHolder(@NonNull View view) {
            ButterKnife.inject(this, view);
        }
    }


    public interface DataProvider {
        boolean isContactSelected(int position, @NonNull ContactData contact);
        @NonNull String getSmallDetails(int position, @NonNull ContactData contact);
        @NonNull String getBigDetails(int position, @NonNull ContactData contact);
    }

    public static enum Mode {
        SELECTION,
        REVIEW,
    }
}
