package com.livenation.mobile.android.na.cash.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.CircularImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

@SuppressWarnings("UnusedDeclaration")
public class ContactView extends FrameLayout {
    @InjectView(R.id.view_cash_contact_name) TextView name;
    @InjectView(R.id.view_cash_contact_small_details) TextView smallDetails;
    @InjectView(R.id.view_cash_contact_big_details) TextView bigDetails;
    @InjectView(R.id.view_cash_contact_photo) CircularImageView photoImageView;


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

    public void setSmallDetails(CharSequence smallDetails) {
        this.smallDetails.setText(smallDetails);
    }

    public CharSequence getSmallDetails() {
        return smallDetails.getText();
    }

    public void setBigDetails(CharSequence bigDetails) {
        if (TextUtils.isEmpty(bigDetails)) {
            this.bigDetails.setVisibility(GONE);
        } else {
            this.bigDetails.setVisibility(VISIBLE);
            this.bigDetails.setText(bigDetails);
        }
    }

    public CharSequence getBigDetails() {
        return bigDetails.getText();
    }

    public ImageView getPhotoImageView() {
        return photoImageView;
    }

    //endregion


    protected void initialize() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_cash_contact, isInEditMode()? null : this, true);
        ButterKnife.inject(this);
    }
}
