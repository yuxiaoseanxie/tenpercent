package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.activities.OrderDetailsActivity;
import com.livenation.mobile.android.ticketing.analytics.AnalyticConstants;
import com.livenation.mobile.android.ticketing.analytics.Analytics;
import com.livenation.mobile.android.ticketing.analytics.CartAnalytic;
import com.livenation.mobile.android.ticketing.analytics.Properties;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Cart;
import com.mobilitus.tm.tickets.models.Total;
import com.segment.android.models.Props;

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
    private boolean isResale;

    private TransitioningImageView image;
    private TextView headerThankYouText;
    private TextView eventNameText;

    private TextView orderNumberText;
    private TextView orderEventDateText;
    private TextView orderCostText;
    private TextView orderVenueText;
    private TextView orderSeatText;
    private TextView orderAccountText;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_order_confirmation);

        this.event = (Event) getIntent().getSerializableExtra(EXTRA_EVENT);
        this.cart = (Cart) getIntent().getSerializableExtra(Constants.EXTRA_CART);
        this.isResale = getIntent().getBooleanExtra(Constants.EXTRA_IS_CART_TMPLUS, false);

        this.image = (TransitioningImageView) findViewById(R.id.activity_order_confirmation_image);
        this.headerThankYouText = (TextView) findViewById(R.id.activity_order_confirmation_quantity);
        this.eventNameText = (TextView) findViewById(R.id.activity_order_confirmation_event_name);

        this.orderNumberText = (TextView) findViewById(R.id.activity_order_confirmation_number);
        this.orderEventDateText = (TextView) findViewById(R.id.activity_order_confirmation_date);
        this.orderVenueText = (TextView) findViewById(R.id.activity_order_confirmation_venue);
        this.orderCostText = (TextView) findViewById(R.id.activity_order_confirmation_cost);
        this.orderSeatText = (TextView) findViewById(R.id.activity_order_confirmation_seats);
        this.orderAccountText = (TextView) findViewById(R.id.activity_order_confirmation_account);

        Button cashButton = (Button) findViewById(R.id.activity_order_confirmation_cash);
        cashButton.setOnClickListener(new CashClickListener());

        Button detailsButton = (Button) findViewById(R.id.activity_order_confirmation_details_button);
        detailsButton.setOnClickListener(new DetailsClickListener());

        if (null == savedInstanceState) {
            trackScreenLoad();
        }
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
        image.setDefaultImage(DefaultImageHelper.computeDefaultDpDrawableId(this, event.getNumericId()));

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
            if (getCart().getOrderSummary() != null) {
                if (!TextUtils.isEmpty(getCart().getOrderSummary().getSeats()))
                    orderSeatText.setText(getCart().getOrderSummary().getSeats());
                else if (!TextUtils.isEmpty(getCart().getOrderSummary().getSection()))
                    orderSeatText.setText(getCart().getOrderSummary().getSection());
                else
                    orderSeatText.setText(R.string.data_missing_placeholder);
            } else {
                orderSeatText.setText(R.string.data_missing_placeholder);
            }
            com.mobilitus.tm.tickets.models.User user = Ticketing.getTicketService().getUser();
            if (user != null && !TextUtils.isEmpty(user.getEmail())) {
                orderAccountText.setText(user.getEmail());
            } else {
                orderAccountText.setText(R.string.data_missing_placeholder);
            }

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
        if (getEvent() != null && getEvent().getVenue() != null) {
            orderVenueText.setText(getEvent().getVenue().getName());
        } else {
            orderVenueText.setText(R.string.data_missing_placeholder);
        }
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
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        SHORT_DATE_FORMATTER.setTimeZone(timeZone);

        String eventTemplate = getString(R.string.share_template_order_confirmation);
        return eventTemplate.replace("$HEADLINE_ARTIST", event.getDisplayName())
                .replace("$SHORT_DATE", SHORT_DATE_FORMATTER.format(event.getLocalStartTime()))
                .replace("$VENUE", event.getVenue().getName())
                .replace("$LINK", event.getWebUrl());
    }

    //endregion

    private Properties getProperties() {
        com.mobilitus.tm.tickets.models.Event event = getCart().getEvent();
        String orderTotal = TicketingUtils.formatCurrency(getCart().getTotal().getCurrency(), getCart().getTotal().getGrandTotal());
        String preciseTotal = getCart().getTotal().getGrandTotal().toString();

        Properties props = Analytics.createBaseTrackingProperties(event);

        props.put(AnalyticConstants.PROP_TICKET_TYPE, getCart().getOrderType());
        props.put(AnalyticConstants.PROP_PRICE, preciseTotal);
        props.put(AnalyticConstants.PROP_ORDER_TOTAL, orderTotal);

        if (getCart().getPaymentCard() != null) {
            props.put(AnalyticConstants.PROP_PAYMENT_METHOD, getCart().getPaymentCard().getType());
        }
        if (getCart().getOrderSummary() != null) {
            props.put(AnalyticConstants.PROP_SECTION, getCart().getOrderSummary().getSection());
        }
        if (getCart().getDeliveryMethod() != null) {
            props.put(AnalyticConstants.PROP_DELIVERY_OPTION, getCart().getDeliveryMethod().getName());
        }
        if (getCart().getEvent() != null) {
            props.put(AnalyticConstants.PROP_TM_EVENT_ID, getCart().getEvent().getEventID());
        }
        if (getCart().getTickets() != null) {
            props.put(AnalyticConstants.PROP_NUM_TICKETS, getCart().getTickets().size());
        }
        if (getCart().getBuyer() != null) {
            props.put(AnalyticConstants.PROP_ZIP, getCart().getBuyer().getZip());
        }
        return props;
    }

    private void trackScreenLoad() {
        Ticketing.getAnalytics().track(AnalyticConstants.ORDER_CONFIRMATION_SCREEN_LOAD, AnalyticConstants.CATEGORY_CONFIRMATION, getProperties());

        if (getCart() != null) {
            CartAnalytic charges = Analytics.calculateChargesForCart(getCart());
            Props ticketQuantityProps = getPreBuiltCartProps();
            ticketQuantityProps.put(AnalyticConstants.PROP_TICKET_QUANTITY, charges.getTicketQuantity());
            LiveNationAnalytics.track(AnalyticConstants.PROP_TICKET_QUANTITY, AnalyticConstants.CATEGORY_CHECKOUT, ticketQuantityProps);
            Props revenueProps = getPreBuiltCartProps();
            revenueProps.put(AnalyticConstants.PROP_REVENUE, charges.getRevenue());
            LiveNationAnalytics.track(AnalyticConstants.PROP_REVENUE, AnalyticConstants.CATEGORY_CHECKOUT, revenueProps);
            Props convFeeProps = getPreBuiltCartProps();
            convFeeProps.put(AnalyticConstants.PROP_TOTAL_CONVENIENCE_CHARGE, charges.getConvFee());
            LiveNationAnalytics.track(AnalyticConstants.PROP_TOTAL_CONVENIENCE_CHARGE, AnalyticConstants.CATEGORY_CHECKOUT, convFeeProps);
            Props otherFeesProps = getPreBuiltCartProps();
            otherFeesProps.put(AnalyticConstants.PROP_ORDER_PROCESSING_FEE, charges.getOrderProcessingFee());
            LiveNationAnalytics.track(AnalyticConstants.PROP_ORDER_PROCESSING_FEE, AnalyticConstants.CATEGORY_CHECKOUT, otherFeesProps);
            Props deliveryFeeProps = getPreBuiltCartProps();
            deliveryFeeProps.put(AnalyticConstants.PROP_DELIVERY_FEE, charges.getDeliveryFee());
            LiveNationAnalytics.track(AnalyticConstants.PROP_DELIVERY_FEE, AnalyticConstants.CATEGORY_CHECKOUT, deliveryFeeProps);
            Props orderProcessingFeeProps = getPreBuiltCartProps();
            orderProcessingFeeProps.put(AnalyticConstants.PROP_OTHER_FEE, charges.getOrderProcessingFee());
            LiveNationAnalytics.track(AnalyticConstants.PROP_OTHER_FEE, AnalyticConstants.CATEGORY_CHECKOUT, orderProcessingFeeProps);
            Props originalFaceValueOfTicketProps = getPreBuiltCartProps();
            originalFaceValueOfTicketProps.put(AnalyticConstants.PROP_ORIGINAL_FACE_VALUE, charges.getOriginalFaceValueOfTicket());
            LiveNationAnalytics.track(AnalyticConstants.PROP_ORIGINAL_FACE_VALUE, AnalyticConstants.CATEGORY_CHECKOUT, originalFaceValueOfTicketProps);
            Props upsellUnitsProps = getPreBuiltCartProps();
            upsellUnitsProps.put(AnalyticConstants.PROP_UPSELL_QUANTITY, charges.getUpsellUnits());
            LiveNationAnalytics.track(AnalyticConstants.PROP_UPSELL_QUANTITY, AnalyticConstants.CATEGORY_CHECKOUT, upsellUnitsProps);
            Props upsellRevenueProps = getPreBuiltCartProps();
            upsellRevenueProps.put(AnalyticConstants.PROP_UPSELL_TOTAL, charges.getUpsellRevenue());
            LiveNationAnalytics.track(AnalyticConstants.PROP_UPSELL_TOTAL, AnalyticConstants.CATEGORY_CHECKOUT, upsellRevenueProps);
            Log.i(getClass().getSimpleName(), "Charges for cart " + getCart() + ": " + charges);
            Props typeProps = getPreBuiltCartProps();
            String resale  = AnalyticConstants.PROP_TYPE_PRIMARY;
            if (isResale) {
                resale = AnalyticConstants.PROP_TYPE_RESALE;
            }
            typeProps.put(AnalyticConstants.PROP_TYPE, resale);
            LiveNationAnalytics.track(AnalyticConstants.PROP_TYPE, AnalyticConstants.CATEGORY_CHECKOUT, typeProps);

            boolean isResaleTicket = getIntent().getBooleanExtra(Constants.EXTRA_IS_CART_TMPLUS, false);
            Log.i(getClass().getSimpleName(), "Ticket Type: " + (isResaleTicket? "resale" : "primary"));
        }
    }

    private Props getPreBuiltCartProps() {
        Props props = new Props();
        if (event != null) {
            props.put(AnalyticConstants.PROP_EVENT_ID, event.getId());
            props.put(AnalyticConstants.PROP_VENUE_ID, event.getVenue().getId());
            Artist artist = event.getLineup().get(0);
            if (artist != null) {
                props.put(AnalyticConstants.PROP_ARTIST_ID, event.getLineup().get(0));
            }
        }
        return props;
    }

    private void trackFullDetailsTap() {
        Ticketing.getAnalytics().track(AnalyticConstants.VIEW_FULL_DETAILS_TAP, AnalyticConstants.CATEGORY_CONFIRMATION, getProperties());
    }

    private class CashClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

        }
    }

    private class DetailsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            trackFullDetailsTap();
            Intent intent = new Intent(OrderConfirmationActivity.this, OrderDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_CART, getCart());
            startActivity(intent);
        }
    }
}
