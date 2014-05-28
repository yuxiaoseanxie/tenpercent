package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.activities.OrderDetailsActivity;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Cart;
import com.mobilitus.tm.tickets.models.Total;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderConfirmationActivity extends DetailBaseFragmentActivity {
    public static final String EXTRA_EVENT = "com.livenation.mobile.android.na.ui.OrderConfirmationActivity.EXTRA_EVENT";

    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};
    private static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);

    private Event event;
    private Cart cart;

    private NetworkImageView image;
    private TextView headerThankYouText;
    private TextView eventNameText;

    private TextView orderNumberText;
    private TextView orderEventDateText;
    private TextView orderCostText;
    private TextView orderVenueText;
    private TextView orderSeatText;
    private TextView orderAccountText;

    private Button detailsButton;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        this.event = (Event) getIntent().getSerializableExtra(EXTRA_EVENT);
        this.cart = (Cart) getIntent().getSerializableExtra(Constants.EXTRA_CART);

        this.image = (NetworkImageView) findViewById(R.id.activity_order_confirmation_image);
        this.headerThankYouText = (TextView) findViewById(R.id.activity_order_confirmation_quantity);
        this.eventNameText = (TextView) findViewById(R.id.activity_order_confirmation_event_name);

        this.orderNumberText = (TextView) findViewById(R.id.activity_order_confirmation_number);
        this.orderEventDateText = (TextView) findViewById(R.id.activity_order_confirmation_date);
        this.orderVenueText = (TextView) findViewById(R.id.activity_order_confirmation_venue);
        this.orderCostText = (TextView) findViewById(R.id.activity_order_confirmation_cost);
        this.orderSeatText = (TextView) findViewById(R.id.activity_order_confirmation_seats);
        this.orderAccountText = (TextView) findViewById(R.id.activity_order_confirmation_account);

        this.detailsButton = (Button) findViewById(R.id.activity_order_confirmation_details_button);
        detailsButton.setOnClickListener(new DetailsClickListener());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        displayImage();
        displayHeaderInfo();
        displayDetails();
    }

    //endregion


    //region Displaying Data

    private void displayImage() {
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

    private void displayHeaderInfo() {
        int numberOfTickets = TicketingUtils.getTicketCountForCart(getCart());
        String quantityString = getResources().getQuantityString(R.plurals.order_confirmation_quantity, numberOfTickets);
        headerThankYouText.setText(quantityString);

        eventNameText.setText(getEvent().getName());

        String subtitle = getResources().getQuantityString(R.plurals.order_confirmation_title_detail, numberOfTickets, numberOfTickets);
        getActionBar().setSubtitle(subtitle);
    }

    private void displayDetails() {
        if (getCart() != null) {
            orderNumberText.setText(getCart().getDisplayOrderID());
            orderSeatText.setText(getCart().getOrderSummary().getSeats());
            orderAccountText.setText(getCart().getBuyer().getEmail());

            Total total = getCart().getTotal();
            if (total != null) {
                orderCostText.setText(TicketingUtils.formatCurrency(total.getCurrency(), total.getGrandTotal()));
            } else {
                orderCostText.setText(R.string.data_missing_placeholder);
            }
        } else {
            orderNumberText.setText(R.string.data_missing_placeholder);
            orderSeatText.setText(R.string.data_missing_placeholder);
            orderAccountText.setText(R.string.data_missing_placeholder);
        }

        orderEventDateText.setText(TicketingUtils.formatDate(getEvent().getLocalStartTime()));
        orderVenueText.setText(getEvent().getVenue().getName());
    }

    //endregion


    //region Getters

    public Event getEvent() {
        return event;
    }

    public Cart getCart() {
        return cart;
    }

    //endregion


    //region Sharing

    @Override
    protected boolean isShareAvailable() {
        return (getEvent() != null);
    }

    @Override
    protected String getShareSubject() {
        return getEvent().getName();
    }

    @Override
    protected String getShareText() {
        TimeZone timeZone = TimeZone.getTimeZone(getEvent().getVenue().getTimeZone());
        SHORT_DATE_FORMATTER.setTimeZone(timeZone);

        String eventTemplate = getString(R.string.share_template_order_confirmation);
        return eventTemplate.replace("$HEADLINE_ARTIST", event.getDisplayName())
                .replace("$SHORT_DATE", SHORT_DATE_FORMATTER.format(event.getLocalStartTime()))
                .replace("$VENUE", event.getVenue().getName())
                .replace("$LINK", event.getWebUrl());
    }

    //endregion


    private class DetailsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(OrderConfirmationActivity.this, OrderDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_CART, getCart());
            startActivity(intent);
        }
    }
}
