package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.mobilitus.tm.tickets.models.Cart;

import java.util.List;

public class OrderConfirmationActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_EVENT = "com.livenation.mobile.android.na.ui.OrderConfirmationActivity.EXTRA_EVENT";
    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};

    private Event event;
    private Cart cart;

    private NetworkImageView image;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        this.event = (Event) getIntent().getSerializableExtra(EXTRA_EVENT);
        this.cart = (Cart) getIntent().getSerializableExtra(Constants.EXTRA_CART);

        this.image = (NetworkImageView) findViewById(R.id.activity_order_confirmation_image);

        List<Artist> lineup = event.getLineup();
        if (!lineup.isEmpty()) {
            Artist headliner = lineup.get(0);
            String imageKey = headliner.getBestImageKey(IMAGE_PREFERRED_SHOW_KEYS);
            if (imageKey != null) {
                String imageUrl = headliner.getImageURL(imageKey);
                image.setImageUrl(imageUrl, LiveNationApplication.get().getImageLoader());
            }
        }
    }

    //endregion


    public Event getEvent() {
        return event;
    }

    public Cart getCart() {
        return cart;
    }
}
