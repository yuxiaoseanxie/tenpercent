package com.livenation.mobile.android.na.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.experience.android.activities.ExpActivityConfig;
import com.experience.android.activities.ExperienceWebViewActivity;
import com.livenation.mobile.android.na.ExperienceApp.ExperienceAppClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.app.rating.AppRaterManager;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.na.ui.views.ConfirmationActionButton;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.na.utils.CalendarUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.analytics.AnalyticConstants;
import com.livenation.mobile.android.ticketing.analytics.Analytics;
import com.livenation.mobile.android.ticketing.analytics.CartAnalytic;
import com.livenation.mobile.android.ticketing.analytics.Properties;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Cart;
import com.mobilitus.tm.tickets.models.DeliveryMethod;
import com.mobilitus.tm.tickets.models.Total;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OrderConfirmationActivity extends DetailBaseFragmentActivity {
    public static final String EXTRA_EVENT = "com.livenation.mobile.android.na.ui.OrderConfirmationActivity.EXTRA_EVENT";
    public static final String EXTRA_SHOW_UPGRADABLE = "com.livenation.mobile.android.na.ui.OrderConfirmationActivity.EXTRA_SHOW_UPGRADABLE";

    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};
    private static final SimpleDateFormat DISPLAY_DATE_FORMATTER = new SimpleDateFormat("EEE, MMM d, yyyy\nhh:mm a ZZZZ", Locale.US);
    private static final SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);

    private Event event;
    private Cart cart;
    private CartAnalytic charges;
    private boolean isResale;
    private String deliveryMethod;
    //temporary workaround on the assumption this field will be later accessible from mTopia via the library
    private boolean isUpgradable;

    private TransitioningImageView image;
    private TextView headerThankYouText;
    private TextView eventNameText;

    private LinearLayout actionsContainer;

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
        this.isUpgradable = getIntent().getBooleanExtra(EXTRA_SHOW_UPGRADABLE, false);
        DeliveryMethod deliveryMethod = (DeliveryMethod) getIntent().getSerializableExtra(Constants.EXTRA_DELIVERY_METHOD);
        if (deliveryMethod != null) {
            this.deliveryMethod = deliveryMethod.getName();
        } else {
            this.deliveryMethod = "default";
        }
        this.image = (TransitioningImageView) findViewById(R.id.activity_order_confirmation_image);
        this.headerThankYouText = (TextView) findViewById(R.id.activity_order_confirmation_quantity);
        this.eventNameText = (TextView) findViewById(R.id.activity_order_confirmation_event_name);

        this.actionsContainer = (LinearLayout) findViewById(R.id.activity_order_confirmation_actions_container);

        this.orderNumberText = (TextView) findViewById(R.id.activity_order_confirmation_number);
        this.orderEventDateText = (TextView) findViewById(R.id.activity_order_confirmation_date);
        this.orderVenueText = (TextView) findViewById(R.id.activity_order_confirmation_venue);
        this.orderCostText = (TextView) findViewById(R.id.activity_order_confirmation_cost);
        this.orderSeatText = (TextView) findViewById(R.id.activity_order_confirmation_seats);
        this.orderAccountText = (TextView) findViewById(R.id.activity_order_confirmation_note);

        List<String> confirmationActions = LiveNationApplication.get().getInstalledAppConfig().getConfirmationActions();

        addActionButtons(confirmationActions);

        if (null == savedInstanceState) {
            trackScreenLoad();
            fetchUpgradeStatus();
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
        String quantityString = getResources().getQuantityString(R.plurals.order_confirmation_quantity, numberOfTickets, numberOfTickets);
        headerThankYouText.setText(quantityString);

        eventNameText.setText(getEvent().getName());

        String subtitle = getResources().getQuantityString(R.plurals.order_confirmation_title_detail, numberOfTickets, numberOfTickets);
        //noinspection ConstantConditions
        getActionBar().setSubtitle(subtitle);
    }

    private void displayDetails() {
        if (getCart() != null) {
            orderNumberText.setText(getCart().getDisplayOrderID());
            if (getCart().getOrderSummary() != null) {
                String seats = getCart().getOrderSummary().getSeats();
                String section = getCart().getOrderSummary().getSection();
                String seatDescription = "";
                if (!TextUtils.isEmpty(section)) {
                    seatDescription += getCart().getOrderSummary().getSection();
                }

                if (!TextUtils.isEmpty(seats)) {
                    if (seatDescription.length() > 0)
                        seatDescription += " – ";

                    seatDescription += seats;
                }

                if (seatDescription.length() == 0)
                    seatDescription = getString(R.string.data_missing_placeholder);

                orderSeatText.setText(seatDescription);
            } else {
                orderSeatText.setText(R.string.data_missing_placeholder);
            }
            com.mobilitus.tm.tickets.models.User user = Ticketing.getTicketService().getUser();
            String email;
            if (user != null && !TextUtils.isEmpty(user.getEmail())) {
                email = user.getEmail();
            } else {
                email = getString(R.string.data_missing_placeholder);
            }
            orderAccountText.setText(getString(R.string.order_confirmation_note_fmt, email));

            Total total = getCart().getTotal();
            if (total != null) {
                orderCostText.setText(TicketingUtils.formatCurrency(total.getCurrency(), total.getGrandTotal()));
            } else {
                orderCostText.setText(R.string.data_missing_placeholder);
            }
        } else {
            orderNumberText.setText(R.string.data_missing_placeholder);
            orderSeatText.setText(R.string.data_missing_placeholder);
            orderAccountText.setText(getString(R.string.order_confirmation_note_fmt, getString(R.string.data_missing_placeholder)));
        }

        TimeZone timeZone;
        if (getEvent().getVenue() != null && getEvent().getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        DISPLAY_DATE_FORMATTER.setTimeZone(timeZone);
        orderEventDateText.setText(DISPLAY_DATE_FORMATTER.format(getEvent().getLocalStartTime()));
        if (getEvent() != null && getEvent().getVenue() != null) {
            orderVenueText.setText(getEvent().getVenue().getName());
        } else {
            orderVenueText.setText(R.string.data_missing_placeholder);
        }
    }

    private void addActionButtons(List<String> confirmationActions) {
        //temporary removeViews hack to support the WIP async event upgrade support
        actionsContainer.removeAllViews();

        int margin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        int numberAdded = 0;

        for (String name : confirmationActions) {
            try {
                Action action = Action.valueOf(name);
                if (!action.isAvailable(this))
                    continue;

                ConfirmationActionButton button = action.newConfirmationActionButton(this);
                button.setOnClickListener(createOnClickListenerForAction(action));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.bottomMargin = margin;
                actionsContainer.addView(button, layoutParams);

                if (++numberAdded >= 3)
                    break;
            } catch (IllegalArgumentException e) {
                Log.w(getClass().getSimpleName(), "Invalid action name '" + name + "', ignoring.", e);
            }
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

    //region ExperienceApp

    private void fetchUpgradeStatus() {
        ExperienceAppClient experienceAppClient = new ExperienceAppClient(getApplicationContext());

        //reach out to the experience api
        experienceAppClient.makeRequest(getCart().getEvent().getEventID(), experienceResponseListener);
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
        props.put(AnalyticConstants.PROP_DELIVERY_OPTION, deliveryMethod);
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

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_CHECKOUT_CONFIRMATION_SCREEN_LOAD;
    }

    private void trackScreenLoad() {
        Ticketing.getAnalytics().track(AnalyticConstants.ORDER_CONFIRMATION_SCREEN_LOAD, AnalyticConstants.CATEGORY_CONFIRMATION, getProperties());
    }

    private View.OnClickListener createOnClickListenerForAction(@NonNull Action action) {
        switch (action) {
            case ADD_TO_CALENDAR:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CalendarDialogFragment.CalendarItem item = new CalendarDialogFragment.CalendarItem(event.getVenue().getTimeZone(), event.getDisplayName());
                        item.setStartDate(event.getLocalStartTime());
                        CalendarUtils.addEventToCalendar(item, event.getId(), OrderConfirmationActivity.this);
                    }
                };

            case SHARE:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onShare();
                    }
                };

            case UPGRADE:
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getCart() != null) {
                            final Intent intent = new Intent(OrderConfirmationActivity.this, ExperienceWebViewActivity.class);

                            intent.putExtra(ExpActivityConfig.SSO_ORDER_ID, getCart().getOrderID());
                            intent.putExtra(ExpActivityConfig.SSO_EVENT_ID, getCart().getEvent().getEventID());
                            intent.putExtra(ExpActivityConfig.SSO_FAN_ID, Ticketing.getTicketService().getUser().getEmail());

                            intent.putExtra(ExpActivityConfig.SSO_TICKET_SYSTEM, ExpActivityConfig.TicketSystem.TICKETMASTER_TAP);

                            startActivity(intent);
                        }
                    }
                };

            default:
                return null;
        }
    }

    private static enum Action {
        ADD_TO_CALENDAR(R.string.add_to_calendar, R.string.confirmation_action_tag_line_add_to_calendar, R.drawable.confirmation_add_to_calendar),

        SPLIT_COST(R.string.confirmation_action_split_cost, R.string.confirmation_action_tag_line_split_cost, R.drawable.confirmation_split_cost) {
            @Override
            public boolean isAvailable(@NonNull OrderConfirmationActivity activity) {
                return (TicketingUtils.getTicketCountForCart(activity.cart) > 1);
            }
        },

        UPGRADE(R.string.confirmation_action_seat_upgrade, R.string.confirmation_action_tag_line_seat_upgrade, R.drawable.confirmation_upgrade) {
            @Override
            public boolean isAvailable(@NonNull OrderConfirmationActivity activity) {
                return activity.isUpgradable;
            }
        },

        SHARE(R.string.action_share, R.string.confirmation_action_tag_line_share, R.drawable.confirmation_share) {
            @Override
            public boolean isAvailable(@NonNull OrderConfirmationActivity activity) {
                return activity.isShareAvailable();
            }
        };

        public final int titleResId;
        public final int tagLineResId;
        public final int imageResId;

        public ConfirmationActionButton newConfirmationActionButton(@NonNull Context context) {
            ConfirmationActionButton actionButton = new ConfirmationActionButton(context);
            actionButton.setTitle(context.getString(titleResId));
            actionButton.setTagLine(context.getString(tagLineResId));
            actionButton.setImageResource(imageResId);
            actionButton.setTag(this.name());
            return actionButton;
        }

        public boolean isAvailable(@NonNull OrderConfirmationActivity activity) {
            return true;
        }

        private Action(int titleResId, int tagLineResId, int imageResId) {
            this.titleResId = titleResId;
            this.tagLineResId = tagLineResId;
            this.imageResId = imageResId;
        }
    }

    private ExperienceAppClient.ExperienceAppListener experienceResponseListener = new ExperienceAppClient.ExperienceAppListener() {
        @Override
        public void onResponse(Boolean response) {
            doResponse(response);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            doResponse(false);
        }

        private void doResponse(boolean upgradable) {
            isUpgradable = upgradable;
            getIntent().putExtra(EXTRA_SHOW_UPGRADABLE, isUpgradable);

            List<String> confirmationActions = LiveNationApplication.get().getInstalledAppConfig().getConfirmationActions();
            addActionButtons(confirmationActions);
        }
    };

    @Override
    protected Map<String, Object> getAnalyticsProps() {
        Map<String, Object> props = new HashMap<String, Object>();
        if (getCart() != null) {
            props = Analytics.createBaseTrackingProperties(getCart().getEvent()).toMap();
            props.put(AnalyticConstants.PROP_TICKET_QUANTITY, getCharges().getTicketQuantity());
            props.put(AnalyticConstants.PROP_REVENUE, getCharges().getRevenue());
            props.put(AnalyticConstants.PROP_TOTAL_CONVENIENCE_CHARGE, getCharges().getConvFee());
            props.put(AnalyticConstants.PROP_ORDER_PROCESSING_FEE, getCharges().getOrderProcessingFee());
            props.put(AnalyticConstants.PROP_DELIVERY_FEE, getCharges().getDeliveryFee());
            props.put(AnalyticConstants.PROP_OTHER_FEE, getCharges().getOrderProcessingFee());
            props.put(AnalyticConstants.PROP_ORIGINAL_FACE_VALUE, getCharges().getOriginalFaceValueOfTicket());
            props.put(AnalyticConstants.PROP_UPSELL_QUANTITY, getCharges().getUpsellUnits());
            props.put(AnalyticConstants.PROP_UPSELL_TOTAL, getCharges().getUpsellRevenue());
            Log.i(getClass().getSimpleName(), "Charges for cart " + getCart() + ": " + getCharges());
            String resale = AnalyticConstants.PROP_TYPE_PRIMARY;
            if (isResale) {
                resale = AnalyticConstants.PROP_TYPE_RESALE;
            }
            props.put(AnalyticConstants.PROP_TYPE, resale);
            props.put(AnalyticConstants.PROP_DELIVERY_METHOD, deliveryMethod);

        }
        return props;
    }

    private CartAnalytic getCharges() {
        if (charges == null && getCart() != null) {
            charges = Analytics.calculateChargesForCart(getCart());
        }
        return charges;
    }

    @Override
    protected Map<String, Object> getOmnitureProductsProps() {
        HashMap cdata = new HashMap<String, Object>();
        String data = "";
        if (getCart() != null) {
            data += ";" + getCart().getEvent().getEventID();
        }
        if (getCharges() != null) {
            data += ";" + getCharges().getTicketQuantity();
            data += ";" + getCharges().getRevenue();
            data += ";event20=" + getCharges().getConvFee();
            data += "|event21=" + getCharges().getOtherFees();
            data += "|event19=" + getCharges().getDeliveryFee();
            data += "|event26=" + getCharges().getOrderProcessingFee();
            data += "|event41=" + getCharges().getOriginalFaceValueOfTicket();
            data += "|event17=" + getCharges().getUpsellUnits();
            data += "|event18=" + getCharges().getUpsellRevenue();
            String resale = AnalyticConstants.PROP_TYPE_PRIMARY;
            if (isResale) {
                resale = AnalyticConstants.PROP_TYPE_RESALE;
            }
            data += ";eVar43=" + resale;
        }
        cdata.put("&&products", data);
        return cdata;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppRaterManager raterManager = new AppRaterManager(this);
        int ticketsCount = 1;
        if (getCart() != null) {
            ticketsCount = TicketingUtils.getTicketCountForCart(getCart());
        }
        raterManager.purchaseCompleted(getApplicationContext(), ticketsCount);
    }

}
