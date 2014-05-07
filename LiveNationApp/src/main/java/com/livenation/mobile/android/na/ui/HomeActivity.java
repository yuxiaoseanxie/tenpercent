/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SlidingTabLayout;
import com.livenation.mobile.android.na.notifications.InboxStatusView;
import com.livenation.mobile.android.na.notifications.ui.InboxActivity;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.ui.fragments.AllShowsFragment;
import com.livenation.mobile.android.na.ui.fragments.NearbyVenuesFragment;
import com.livenation.mobile.android.na.ui.fragments.RecommendationSetsFragment;
import com.livenation.mobile.android.na.utils.ContactUtils;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AppInitData;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.Map;

public class HomeActivity extends LiveNationFragmentActivity implements AccountSaveAuthTokenView, AccountSignOutView {

    private static final int RC_SSO_REPAIR = 0;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager pager;
    private FragmentAdapter adapter;
    private SlidingTabLayout slidingTabLayout;
    private boolean hasUnreadNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        DrawerLayout rootView = (DrawerLayout) findViewById(R.id.activity_landing_drawer);
        drawerToggle = new ActionBarDrawerToggle(HomeActivity.this, rootView,
                R.drawable.ic_drawer,
                R.string.actionbar_drawer_open,
                R.string.actionbar_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                LiveNationAnalytics.track(AnalyticConstants.ACCOUNT_ICON_TAP);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                LiveNationAnalytics.track(AnalyticConstants.X_TAP);
            }
        };
        rootView.setDrawerListener(drawerToggle);
        adapter = new FragmentAdapter(getSupportFragmentManager(), getApplicationContext());

        pager = (ViewPager) findViewById(R.id.activity_home_pager);
        pager.setAdapter(adapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.activity_home_sliding_tabs);
        slidingTabLayout.setViewPager(pager);
        int tabAccentColor = getResources().getColor(R.color.tab_accent_color);
        slidingTabLayout.setBottomBorderColor(tabAccentColor);
        slidingTabLayout.setSelectedIndicatorColors(tabAccentColor);

        LiveNationApplication.get().getInboxStatusPresenter().initialize(this, null, new InboxStatusUpdater());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notificationsItem = menu.findItem(R.id.menu_home_notifications_item);
        if (hasUnreadNotifications) {
            notificationsItem.setIcon(R.drawable.notifications_unread);
        } else {
            notificationsItem.setIcon(R.drawable.notifications_normal);
        }

        MenuItem debug = menu.findItem(R.id.menu_home_debug_item);
        debug.setVisible(BuildConfig.DEBUG);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_home_notifications_item:
                LiveNationAnalytics.track(AnalyticConstants.NOTIFICATION_ICON_TAP);
                startActivity(new Intent(this, InboxActivity.class));
                return true;

            case R.id.menu_home_debug_item:
                startActivity(new Intent(this, DebugActivity.class));
                return true;

            case R.id.menu_home_search_item:
                LiveNationAnalytics.track(AnalyticConstants.SEARCH_ICON_TAP);
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.menu_home_help_item:
                startActivity(new Intent(this, HelpMenuActivity.class));
                LiveNationAnalytics.track(AnalyticConstants.HELP_CELL_TAP);
                return true;

            case R.id.menu_home_legal_item:
                LiveNationAnalytics.track(AnalyticConstants.LEGAL_CELL_TAP);
                startActivity(new Intent(this, LegalActivity.class));
                return true;

            case R.id.menu_home_contact_item:
                buildAndOpenContactEmail();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void buildAndOpenContactEmail() {

        final String emailAddress = getString(R.string.contact_email_address);
        final String subject = getString(R.string.contact_email_subject);
        final String message = "\n\n" + getString(R.string.contact_email_signature_message)
                + getString(R.string.contact_email_signature_message_appversion) + BuildConfig.VERSION_NAME
                + getString(R.string.contact_email_signature_message_device) + Build.MANUFACTURER +"  " + Build.MODEL
                + getString(R.string.contact_email_signature_message_platform) + Build.VERSION.SDK_INT;
        LiveNationApplication.get().getConfigManager().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                Map<String, String> userInfo = apiService.getApiConfig().getAppInitResponse().getData().getUserInfo();
                String userId = userInfo.get(AppInitData.USER_INFO_ID_KEY);
                String signature = message + getString(R.string.contact_email_signature_message_userid) + userId;
                ContactUtils.emailTo(emailAddress, subject,  signature, HomeActivity.this);
            }

            @Override
            public void onApiServiceNotAvailable() {
                ContactUtils.emailTo(emailAddress, subject, message, HomeActivity.this);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SSO_REPAIR:
                if (resultCode != RESULT_OK) {
                    //the attempt to fix the SSO config with the user failed, lets wipe the auth configuration.
                    getAccountPresenters().getSignOut().initialize(HomeActivity.this, null, HomeActivity.this);
                    //finish the app. this will reset any tokens in memory.
                    //alternatively, the serviceApi.setSsoProvider() could be set to null here, but lets not try to be clever.
                    finish();
                }
                break;
        }
    }

    @Override
    public void onSaveAuthTokenSuccess() {
        Logger.log("AuthToken", "Updated it");
    }

    @Override
    public void onSignOut() {
        Logger.log("Account", "Signed out");
    }

    @Override
    public void onSaveAuthTokenFailure() {
        throw new IllegalStateException("Should not happen..");
    }


    private AccountPresenters getAccountPresenters() {
        return LiveNationApplication.get().getAccountPresenters();
    }

    public static class FragmentAdapter extends FragmentPagerAdapter {
        private final static int TAB_COUNT = 3;
        private final String[] tabTitles = new String[TAB_COUNT];

        public FragmentAdapter(FragmentManager fm, Context context) {
            super(fm);
            tabTitles[0] = context.getString(R.string.tab_title_your_shows);
            tabTitles[1] = context.getString(R.string.tab_title_nearby);
            tabTitles[2] = context.getString(R.string.tab_title_all_shows);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RecommendationSetsFragment();
                case 1:
                    return new NearbyVenuesFragment();
                case 2:
                    return new AllShowsFragment();
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private class InboxStatusUpdater implements InboxStatusView {
        @Override
        public void setHasUnreadNotifications(boolean hasUnreadNotifications) {
            HomeActivity.this.hasUnreadNotifications = hasUnreadNotifications;
            invalidateOptionsMenu();
        }
    }
}
