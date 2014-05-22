package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.mobilitus.tm.tickets.models.Cart;

public class OrderConfirmationActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_EVENT = "com.livenation.mobile.android.na.ui.OrderConfirmationActivity.EXTRA_EVENT";

    private Event event;
    private Cart cart;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        this.event = (Event) getIntent().getSerializableExtra(EXTRA_EVENT);
        this.cart = (Cart) getIntent().getSerializableExtra(Constants.EXTRA_CART);
    }

    //endregion


    public Event getEvent() {
        return event;
    }

    public Cart getCart() {
        return cart;
    }
}
