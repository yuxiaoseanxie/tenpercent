package com.livenation.mobile.android.na.cash.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.CircularImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContactDataAdapter extends ArrayAdapter<ContactData> {
    private final LayoutInflater inflater;
    private final Mode mode;
    private final DataProvider dataProvider;

    public ContactDataAdapter(@NonNull Context context, Mode mode, @NonNull DataProvider dataProvider) {
        super(context, R.layout.list_cash_recipient);

        this.inflater = LayoutInflater.from(context);
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
            view = inflater.inflate(R.layout.list_cash_recipient, parent, false);
            view.setTag(new ViewHolder(view));
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        ContactData contactData = getItem(position);
        holder.name.setText(contactData.getDisplayName());
        if (contactData.getPhotoUri() != null)
            holder.photo.setImageURI(contactData.getPhotoUri());
        else
            holder.photo.setImageDrawable(null);

        holder.details.setText(dataProvider.getDetails(position, contactData));

        switch (mode) {
            case SELECTION:
                holder.selection.setVisibility(View.VISIBLE);
                holder.price.setVisibility(View.GONE);

                if (dataProvider.isContactSelected(position, contactData))
                    holder.selection.setImageResource(R.drawable.cash_item_checkmark_on);
                else
                    holder.selection.setImageResource(R.drawable.cash_item_checkmark_off);

                break;

            case REVIEW:
                holder.selection.setVisibility(View.GONE);
                holder.price.setVisibility(View.VISIBLE);

                String price = dataProvider.getPrice(position, contactData);
                holder.price.setText(price);

                break;
        }

        return view;
    }

    class ViewHolder {
        @InjectView(R.id.list_cash_recipient_name) TextView name;
        @InjectView(R.id.list_cash_recipient_details) TextView details;
        @InjectView(R.id.list_cash_recipient_photo) CircularImageView photo;
        @InjectView(R.id.list_cash_recipient_selection) ImageView selection;
        @InjectView(R.id.list_cash_recipient_price) TextView price;

        ViewHolder(@NonNull View view) {
            ButterKnife.inject(this, view);
        }
    }


    public interface DataProvider {
        boolean isContactSelected(int position, @NonNull ContactData contact);
        @NonNull String getDetails(int position, @NonNull ContactData contact);
        @NonNull String getPrice(int position, @NonNull ContactData contact);
    }

    public static enum Mode {
        SELECTION,
        REVIEW,
    }
}
