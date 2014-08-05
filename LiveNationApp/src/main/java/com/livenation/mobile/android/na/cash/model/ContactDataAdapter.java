package com.livenation.mobile.android.na.cash.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.CashRecipientsFragment;
import com.livenation.mobile.android.na.ui.views.CircularImageView;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContactDataAdapter extends ArrayAdapter<ContactData> {
    private final LayoutInflater inflater;
    private final Set<ContactData> selectedContacts;
    private PriceProvider priceProvider;

    public ContactDataAdapter(@NonNull Context context, @Nullable Set<ContactData> selectedContacts) {
        super(context, R.layout.list_cash_recipient);

        this.inflater = LayoutInflater.from(context);
        this.selectedContacts = selectedContacts;
    }


    public PriceProvider getPriceProvider() {
        return priceProvider;
    }

    public void setPriceProvider(PriceProvider priceProvider) {
        this.priceProvider = priceProvider;
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
        holder.details.setText(contactData.getDetails());
        if (contactData.getPhotoUri() != null)
            holder.photo.setImageURI(contactData.getPhotoUri());
        else
            holder.photo.setImageDrawable(null);

        if (selectedContacts != null) {
            holder.selection.setVisibility(View.VISIBLE);

            if (selectedContacts.contains(contactData))
                holder.selection.setImageResource(R.drawable.cash_item_checkmark_on);
            else
                holder.selection.setImageResource(R.drawable.cash_item_checkmark_off);

            holder.price.setVisibility(View.GONE);
        } else {
            holder.price.setVisibility(View.VISIBLE);

            if (priceProvider != null) {
                String price = priceProvider.getPrice(position, contactData);
                holder.price.setText(price);
            } else {
                holder.price.setText(null);
            }

            holder.selection.setVisibility(View.GONE);
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


    public interface PriceProvider {
        @NonNull String getPrice(int position, @NonNull ContactData contact);
    }
}
