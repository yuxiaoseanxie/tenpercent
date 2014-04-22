package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.SlidingTabLayout;
import com.livenation.mobile.android.na.ui.fragments.BoxOfficeInfoFragment;
import com.livenation.mobile.android.na.ui.support.BoxOfficeMappings;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.HashMap;

public class VenueBoxOfficeActivity extends FragmentActivity {
    private SlidingTabLayout tabs;
    private ViewPager pager;

    private BoxOffice boxOfficeInfo;

    //region Lifecycle

    private static final String EXTRA_BOX_OFFICE = "com.livenation.mobile.android.na.ui.VenueBoxOfficeActivity.EXTRA_BOX_OFFICE";

    public static Bundle getArguments(Venue venue) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_BOX_OFFICE, venue.getBoxOffice());
        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_box_office);

        this.pager = (ViewPager) findViewById(R.id.activity_venue_box_pager);
        pager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));

        this.tabs = (SlidingTabLayout) findViewById(R.id.activity_venue_box_office_tabs);
        tabs.setViewPager(pager);
        tabs.setBottomBorderColor(0xffe11d39);
        tabs.setSelectedIndicatorColors(0xffe11d39);

        this.boxOfficeInfo = (BoxOffice) getIntent().getSerializableExtra(EXTRA_BOX_OFFICE);
    }

    //endregion


    private class FragmentAdapter extends FragmentPagerAdapter {
        private final String[] items;

        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            this.items = new String[] { "Parking & Transit", "Box Office" };
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return items[position];
        }

        @Override
        public Fragment getItem(int position) {
            BoxOfficeMappings mapping = BoxOfficeMappings.values()[position];
            return BoxOfficeInfoFragment.newInstance(boxOfficeInfo, mapping.getItems());
        }
    }
}
