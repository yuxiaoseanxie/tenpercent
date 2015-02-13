package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.ticketing.activities.BaseActivity;
import com.livenation.mobile.android.ticketing.analytics.AnalyticConstants;
import com.livenation.mobile.android.ticketing.analytics.Analytics;
import com.livenation.mobile.android.ticketing.analytics.Properties;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.mobilitus.tm.tickets.models.Cart;

import android.os.Bundle;

public class OrderDetailsActivity extends BaseActivity {
    private Cart eventInfoCart;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        getActionBar().setTitle(R.string.activity_order_details);
        getActionBar().setSubtitle(getEventInfoCart().getOrderID());
    }

    //endregion

    public Cart getEventInfoCart() {
        if (eventInfoCart == null) {
            this.eventInfoCart = (Cart) getIntent().getSerializableExtra(Constants.EXTRA_CART);
        }
        return eventInfoCart;
    }


    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_ORDER_DETAILS;
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_ORDER_DETAILS_SCREEN_LOAD;
    }

    @Override
    public Properties getScreenProperties() {
        return Analytics.createBaseTrackingProperties(getEventInfoCart().getEvent());
    }
}
