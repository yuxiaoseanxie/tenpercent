package com.livenation.mobile.android.na.cash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashRequestDetailsActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_QUANTITIES = "com.livenation.mobile.android.na.cash.CashRequestDetailsActivity.EXTRA_QUANTITIES";

    @InjectView(R.id.activity_request_details_view_pager) ViewPager viewPager;

    private PageAdapter adapter;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);
        ButterKnife.inject(this);

        this.adapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    //endregion


    public void nextPage() {

    }


    private class PageAdapter extends FragmentPagerAdapter {
        private PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return Page.values().length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (Page.values()[position]) {
                case ENTER_PHONE_NUMBER:
                    return null;

                case ENTER_DEBIT_CARD:
                    return null;

                case ENTER_NAME:
                    return null;

                case ENTER_VERIFICATION_CODE:
                    return null;
            }

            return null;
        }
    }

    private static enum Page {
        ENTER_PHONE_NUMBER,
        ENTER_DEBIT_CARD,
        ENTER_NAME,
        ENTER_VERIFICATION_CODE,
    }
}
