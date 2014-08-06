package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPaymentBlockers;
import com.livenation.mobile.android.na.cash.ui.CashCompleteRequestActivity;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

public class CashOnBoardingActivity extends LiveNationFragmentActivity {
    private static final int WEBSITE_REQUEST_CODE = 0xeb;

    private static final String SAVED_CUSTOMER_STATUS = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_CUSTOMER_STATUS";
    private static final String SAVED_PHONE_NUMBER = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_PHONE_NUMBER";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private CashCustomerStatus customerStatus;
    private String phoneNumber;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        if (savedInstanceState != null) {
            this.customerStatus = (CashCustomerStatus) savedInstanceState.getSerializable(SAVED_CUSTOMER_STATUS);
            this.phoneNumber = savedInstanceState.getString(SAVED_PHONE_NUMBER);
        } else {
            this.customerStatus = (CashCustomerStatus) getIntent().getSerializableExtra(CashUtils.EXTRA_CUSTOMER_STATUS);

            if (customerStatus != null && customerStatus.getBlockers() != null) {
                CashPaymentBlockers blockers = customerStatus.getBlockers();
                if (blockers.getPhoneNumber() != null)
                    showPage(Page.ENTER_PHONE_NUMBER);
                else if (blockers.getCard() != null)
                    showPage(Page.ENTER_DEBIT_CARD);
                else if (blockers.getPasscodeVerification() != null)
                    showPage(Page.ENTER_VERIFICATION_CODE);
                else if (blockers.getUrl() != null)
                    showWebSite(blockers.getUrl());
            } else {
                showPage(Page.ENTER_PHONE_NUMBER);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVED_CUSTOMER_STATUS, getCustomerStatus());
        outState.putString(SAVED_PHONE_NUMBER, getPhoneNumber());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WEBSITE_REQUEST_CODE) {
            setupCompleted();
        }
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
        if (customerStatus.getBlockers().getPhoneNumber() != null)
            showPage(Page.ENTER_VERIFICATION_CODE);
        else
            setupCompleted();
    }

    public void setupCompleted() {
        Intent intent = new Intent(this, CashCompleteRequestActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);

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

    public void showWebSite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, WEBSITE_REQUEST_CODE);
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
