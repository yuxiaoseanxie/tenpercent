package com.livenation.mobile.android.na.cash.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.CircularImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

@SuppressWarnings("UnusedDeclaration")
public class ContactView extends FrameLayout {
    @InjectView(R.id.view_cash_contact_photo) CircularImageView photoImageView;
    @InjectView(R.id.view_cash_contact_name) TextView name;
    @InjectView(R.id.view_cash_contact_price) TextView priceText;

    @InjectView(R.id.view_cash_contact_quantity) TextView quantity;
    @InjectView(R.id.view_cash_contact_quantity_tag) TextView quantityTag;


    public ContactView(Context context) {
        super(context);
        initialize();
    }

    public ContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ContactView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }


    //region Properties

    public void setName(CharSequence name) {
        this.name.setText(name);
    }

    public CharSequence getName() {
        return name.getText();
    }

    public void setPrice(String price) {
        priceText.setText(price);
    }

    public String getPrice() {
        return priceText.getText().toString();
    }

    public void setQuantity(int quantity) {
        this.quantity.setText(Integer.toString(quantity));
        this.quantityTag.setText(getResources().getQuantityText(R.plurals.cash_tickets, quantity));
    }

    public ImageView getPhotoImageView() {
        return photoImageView;
    }

    public void setEditable(boolean editable) {
        if (editable) {
            quantity.setBackgroundResource(R.drawable.cash_quantity_background_editable);
            quantity.setTextColor(getResources().getColor(R.color.white));
        } else {
            quantity.setBackgroundResource(R.drawable.cash_quantity_background_non_editable);
            quantity.setTextColor(getResources().getColor(R.color.disabled));
        }
    }

    //endregion


    protected void initialize() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_cash_contact, isInEditMode()? null : this, true);
        ButterKnife.inject(this);
    }
}
