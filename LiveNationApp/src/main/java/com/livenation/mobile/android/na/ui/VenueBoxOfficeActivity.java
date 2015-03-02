package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.helpers.SlidingTabLayout;
import com.livenation.mobile.android.na.ui.fragments.VenueBoxOfficeTabFragment;
import com.livenation.mobile.android.na.ui.support.BoxOfficeTabs;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class VenueBoxOfficeActivity extends LiveNationFragmentActivity implements ViewPager.OnPageChangeListener {
    private static final String EXTRA_VENUE = "com.livenation.mobile.android.na.ui.VenueBoxOfficeActivity.EXTRA_VENUE";
    private SlidingTabLayout tabs;
    private ViewPager pager;
    private String[] analyticsEvent = {AnalyticConstants.PARKING_TRANSIT_TAB_TAP, AnalyticConstants.BOX_OFFICE_TAB_TAP};
    private Venue venue;

    //region Lifecycle
    private BoxOffice boxOfficeInfo;

    public static Bundle getArguments(Venue venue) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_VENUE, venue);
        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_tabs_with_pager);

        this.pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));

        this.tabs = (SlidingTabLayout) findViewById(R.id.slidingtab);
        tabs.setOnPageChangeListener(this);
        tabs.setViewPager(pager);
        int tabAccentColor = getResources().getColor(R.color.tab_accent_color);
        tabs.setBottomBorderColor(tabAccentColor);
        tabs.setSelectedIndicatorColors(tabAccentColor);

        this.venue = (Venue) getIntent().getSerializableExtra(EXTRA_VENUE);
        this.boxOfficeInfo = venue.getBoxOffice();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        //Analytics
        Props props = new Props();
        String eventName = analyticsEvent[position];
        props.put(AnalyticConstants.VENUE_NAME, venue.getName());
        props.put(AnalyticConstants.VENUE_ID, venue.getId());

        LiveNationAnalytics.track(eventName, AnalyticsCategory.VDP, props);

    }

    @Override
    public void onPageScrollStateChanged(int state) {
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
            VenueBoxOfficeTabFragment fragment = VenueBoxOfficeTabFragment.newInstance(boxOfficeInfo, mapping.getSections());
            return fragment;
        }
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_VDP_VENUE_INFO;
    }
}
