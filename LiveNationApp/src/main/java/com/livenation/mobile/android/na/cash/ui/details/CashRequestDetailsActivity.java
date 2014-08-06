package com.livenation.mobile.android.na.cash.ui.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

public class CashRequestDetailsActivity extends LiveNationFragmentActivity {
    private static final String SAVED_CUSTOMER_STATUS = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_CUSTOMER_STATUS";
    private static final String SAVED_PHONE_NUMBER = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_PHONE_NUMBER";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private CashCustomerStatus customerStatus;
    private String phoneNumber;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        if (savedInstanceState != null) {
            this.customerStatus = (CashCustomerStatus) savedInstanceState.getSerializable(SAVED_CUSTOMER_STATUS);
            this.phoneNumber = savedInstanceState.getString(SAVED_PHONE_NUMBER);
        }

        showPage(Page.ENTER_PHONE_NUMBER);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVED_CUSTOMER_STATUS, getCustomerStatus());
        outState.putString(SAVED_PHONE_NUMBER, getPhoneNumber());
    }

    //endregion


    public CashCustomerStatus getCustomerStatus() {
        return customerStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void continueWithCustomerStatus(CashCustomerStatus customerStatus) {
        this.customerStatus = customerStatus;

        if (customerStatus.getBlockers() != null && customerStatus.getBlockers().getCard() != null) {
            showPage(Page.ENTER_DEBIT_CARD);
        } else {
            showPage(Page.ENTER_VERIFICATION_CODE);
        }
    }

    public void continueToPhoneVerification() {
        showPage(Page.ENTER_VERIFICATION_CODE);
    }

    public void setupCompleted() {
        setResult(RESULT_OK);
        finish();
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
        ENTER_PHONE_NUMBER(CashPhoneNumberFragment.class),
        ENTER_DEBIT_CARD(CashCardFragment.class),
        ENTER_NAME(Fragment.class),
        ENTER_VERIFICATION_CODE(CashVerificationCodeFragment.class);

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
