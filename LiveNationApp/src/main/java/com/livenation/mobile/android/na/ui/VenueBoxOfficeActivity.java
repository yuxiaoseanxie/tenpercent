package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.SlidingTabLayout;
import com.livenation.mobile.android.na.ui.fragments.VenueBoxOfficeTabFragment;
import com.livenation.mobile.android.na.ui.support.BoxOfficeTabs;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

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
        pager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));

        this.tabs = (SlidingTabLayout) findViewById(R.id.activity_venue_box_office_tabs);
        tabs.setViewPager(pager);
        int tabAccentColor = getResources().getColor(R.color.tab_accent_color);
        tabs.setBottomBorderColor(tabAccentColor);
        tabs.setSelectedIndicatorColors(tabAccentColor);

        this.boxOfficeInfo = (BoxOffice) getIntent().getSerializableExtra(EXTRA_BOX_OFFICE);
    }

    //endregion


    private class TabFragmentAdapter extends FragmentStatePagerAdapter {
        public TabFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return BoxOfficeTabs.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            BoxOfficeTabs mapping = BoxOfficeTabs.values()[position];
            return getString(mapping.getTitleResId());
        }

        @Override
        public Fragment getItem(int position) {
            BoxOfficeTabs mapping = BoxOfficeTabs.values()[position];
            return VenueBoxOfficeTabFragment.newInstance(boxOfficeInfo, mapping.getSections());
        }
    }
}
