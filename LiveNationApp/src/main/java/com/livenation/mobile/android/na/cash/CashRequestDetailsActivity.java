package com.livenation.mobile.android.na.cash;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

public class CashRequestDetailsActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_QUANTITIES = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.EXTRA_QUANTITIES";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private CashCustomerStatus customerStatus;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        showPage(Page.ENTER_PHONE_NUMBER);
    }

    //endregion


    public void continueWithCustomerStatus(CashCustomerStatus customerStatus) {
        this.customerStatus = customerStatus;

        if (customerStatus.getBlockers() != null && customerStatus.getBlockers().getCard() != null) {
            showPage(Page.ENTER_DEBIT_CARD);
        } else {
            showPage(Page.ENTER_VERIFICATION_CODE);
        }
    }

    public void showPage(Page page) {
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_request_details_container, page.newInstance(), FRAGMENT_TAG)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_request_details_container, page.newInstance(), FRAGMENT_TAG)
                    .commit();
        }
    }

    private static enum Page {
        ENTER_PHONE_NUMBER(CashLoginFragment.class),
        ENTER_DEBIT_CARD(Fragment.class),
        ENTER_NAME(Fragment.class),
        ENTER_VERIFICATION_CODE(Fragment.class);

        public Fragment newInstance() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        private Class<? extends Fragment> clazz;
        private Page(Class<? extends Fragment> clazz) {
            this.clazz = clazz;
        }
    }
}
