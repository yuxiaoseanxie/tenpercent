package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPaymentBlockers;
import com.livenation.mobile.android.na.cash.ui.CashCompleteRequestActivity;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

public class CashOnboardingActivity extends LiveNationFragmentActivity {
    private static final int WEBSITE_REQUEST_CODE = 0xeb;

    private static final String SAVED_CUSTOMER_STATUS = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_CUSTOMER_STATUS";
    private static final String SAVED_PHONE_NUMBER = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.SAVED_PHONE_NUMBER";

    private static final String PAGE_FRAGMENT_TAG = "PAGE_FRAGMENT_TAG";

    private CashCustomerStatus customerStatus;
    private String phoneNumber;
    private String name;

    private MenuItem nextItem;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_onboarding);

        View footer = findViewById(R.id.activity_cash_onboarding_info_footer);
        footer.setOnClickListener(footerClickListener);

        if (savedInstanceState != null) {
            this.customerStatus = (CashCustomerStatus) savedInstanceState.getSerializable(SAVED_CUSTOMER_STATUS);
            this.phoneNumber = savedInstanceState.getString(SAVED_PHONE_NUMBER);
        } else {
            this.customerStatus = (CashCustomerStatus) getIntent().getSerializableExtra(CashUtils.EXTRA_CUSTOMER_STATUS);

            if (customerStatus != null && customerStatus.getBlockers() != null) {
                CashPaymentBlockers blockers = customerStatus.getBlockers();
                if (blockers.getPhoneNumber() != null)
                    showPage(Page.PHONE);
                else if (blockers.getCard() != null)
                    showPage(Page.CARD);
                else if (blockers.getPasscodeVerification() != null)
                    showPage(Page.VERIFY);
                else if (blockers.getUrl() != null)
                    showWebSite(blockers.getUrl());
            } else {
                showPage(Page.PHONE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isChangingConfigurations()) {
            SquareCashService.getInstance().clearSession();
        }
    }

    //endregion


    //region Menus

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash, menu);

        this.nextItem = menu.findItem(R.id.action_next);
        nextItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentPage() != null)
                    getCurrentPage().next();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        nextItem.getActionView().setEnabled(getCurrentPage() == null || getCurrentPage().isNextAvailable());

        return super.onPrepareOptionsMenu(menu);
    }

    //endregion


    //region Properties

    public CashCustomerStatus getCustomerStatus() {
        return customerStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //endregion


    ///region Moving between steps

    public void continueWithCustomerStatus(CashCustomerStatus customerStatus) {
        this.customerStatus = customerStatus;

        if (customerStatus.getBlockers() != null && customerStatus.getBlockers().getCard() != null) {
            showPage(Page.CARD);
        } else if (TextUtils.isEmpty(customerStatus.getFullName())) {
            continueToName();
        } else {
            continueToPhoneVerification();
        }
    }

    public void continueToName() {
        showPage(Page.NAME);
    }

    public void continueToPhoneVerification() {
        if (customerStatus != null && customerStatus.getBlockers() != null && customerStatus.getBlockers().getPhoneNumber() != null)
            showPage(Page.VERIFY);
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
        if (getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.activity_request_details_container, page.newInstance(), PAGE_FRAGMENT_TAG)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.activity_request_details_container, page.newInstance(), PAGE_FRAGMENT_TAG)
                    .commit();
        }

        invalidateOptionsMenu();
    }

    public @Nullable CashOnboardingFragment getCurrentPage() {
        return (CashOnboardingFragment) getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG);
    }

    public void showWebSite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, WEBSITE_REQUEST_CODE);
    }

    //endregion


    private final View.OnClickListener footerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CashUtils.showHelpWebsite(CashOnboardingActivity.this);
        }
    };


    private static enum Page {
        PHONE(CashOnboardingPhoneFragment.class),
        CARD(CashOnboardingCardFragment.class),
        NAME(CashOnboardingNameFragment.class),
        VERIFY(CashOnboardingVerifyFragment.class);

        public CashOnboardingFragment newInstance() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        private Class<? extends CashOnboardingFragment> clazz;
        private Page(Class<? extends CashOnboardingFragment> clazz) {
            this.clazz = clazz;
        }
    }
}
