/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.app.rating.AppRaterManager;
import com.livenation.mobile.android.na.helpers.InstalledAppConfig;
import com.livenation.mobile.android.na.helpers.LoginHelper;
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
import com.segment.android.models.Props;

public class HomeActivity extends LiveNationFragmentActivity implements AccountSaveAuthTokenView, AccountSignOutView {

    private static final int RC_SSO_REPAIR = 0;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager pager;
    private FragmentAdapter adapter;
    private SlidingTabLayout slidingTabLayout;
    private boolean hasUnreadNotifications;
    private BroadcastReceiver broadcastReceiver;
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_landing);


        contentLayout = (LinearLayout) findViewById(R.id.activity_landing_content);

        DrawerLayout rootView = (DrawerLayout) findViewById(R.id.activity_landing_drawer);
        drawerToggle = new ActionBarDrawerToggle(HomeActivity.this, rootView,
                R.drawable.ic_drawer,
                R.string.actionbar_drawer_open,
                R.string.actionbar_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                LiveNationAnalytics.track(AnalyticConstants.OPEN_DRAWER, AnalyticsCategory.DRAWER);
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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (InstalledAppConfig.ACTION_INSTALLED_APP_CONFIG_UPDATED.equals(intent.getAction())) {
                    updateUpdateRequiredHeader();
                } else {
                    invalidateOptionsMenu();
                }
            }
        };

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGIN_INTENT_FILTER));
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGOUT_INTENT_FILTER));
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(InstalledAppConfig.ACTION_INSTALLED_APP_CONFIG_UPDATED));
    }

    @Override
    protected void onResume() {
        super.onResume();

        final InstalledAppConfig installedAppConfig = LiveNationApplication.get().getInstalledAppConfig();
        if (installedAppConfig.isUpdateAdvisable())
            installedAppConfig.update();

        updateUpdateRequiredHeader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_HOME;
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

        MenuItem logout = menu.findItem(R.id.menu_home_logout_item);
        logout.setVisible(LoginHelper.isLogin());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        switch (item.getItemId()) {
            case R.id.menu_home_notifications_item:
                LiveNationAnalytics.track(AnalyticConstants.NOTIFICATION_ICON_TAP, AnalyticsCategory.ACTION_BAR);
                startActivity(new Intent(this, InboxActivity.class));
                return true;

            case R.id.menu_home_debug_item:
                startActivity(new Intent(this, DebugActivity.class));
                return true;

            case R.id.menu_home_search_item:
                //Analytics attributes
                Props props = new Props();
                props.put(AnalyticConstants.SOURCE, AnalyticsCategory.HOME_SCREEN);

                LiveNationAnalytics.track(AnalyticConstants.SEARCH_ICON_TAP, AnalyticsCategory.ACTION_BAR, props);
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.menu_home_help_item:
                startActivity(new Intent(this, HelpMenuActivity.class));
                LiveNationAnalytics.track(AnalyticConstants.HELP_TAP, AnalyticsCategory.ACTION_BAR);
                return true;

            case R.id.menu_home_legal_item:
                LiveNationAnalytics.track(AnalyticConstants.LEGAL_CREDIT_TAP, AnalyticsCategory.ACTION_BAR);
                startActivity(new Intent(this, LegalActivity.class));
                return true;

            case R.id.menu_home_contact_item:
                LiveNationAnalytics.track(AnalyticConstants.CONTACT_TAP, AnalyticsCategory.ACTION_BAR);
                LiveNationAnalytics.screen(AnalyticConstants.SCREEN_CONTACTS_US, null);
                OmnitureTracker.trackState(AnalyticConstants.OMNITURE_SCREEN_CONTACTS_US, null);
                ContactUtils.buildAndOpenContactUsEmail(this.getApplicationContext());
                return true;

            case R.id.menu_home_logout_item:
                LiveNationAnalytics.track(AnalyticConstants.LOGOUT_TAP, AnalyticsCategory.ACTION_BAR);
                LoginHelper.logout(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateUpdateRequiredHeader() {
        final InstalledAppConfig installedAppConfig = LiveNationApplication.get().getInstalledAppConfig();

        View updateRequiredLayout = contentLayout.findViewById(R.id.sub_update_required_layout);
        if (installedAppConfig.isUpgradeRequired()) {
            if (updateRequiredLayout == null) {
                updateRequiredLayout = getLayoutInflater().inflate(R.layout.sub_update_required, contentLayout, false);
                contentLayout.addView(updateRequiredLayout, 1); // After the tab strip
            }

            TextView text = (TextView) updateRequiredLayout.findViewById(R.id.sub_update_required_text);
            text.setText(installedAppConfig.getUpgradeMessage());

            Button button = (Button) updateRequiredLayout.findViewById(R.id.sub_update_required_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri playStore = Uri.parse(installedAppConfig.getUpgradePlayStoreLink());
                    startActivity(new Intent(Intent.ACTION_VIEW, playStore));
                }
            });
        } else if (updateRequiredLayout != null) {
            contentLayout.removeView(updateRequiredLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SSO_REPAIR:
                if (resultCode != RESULT_OK) {
                    //the attempt to fix the SSO config with the user failed, lets wipe the auth configuration.
                    onSignOut();
                    //finish the app. this will reset any tokens in memory.
                    //alternatively, the serviceApi.setSsoProvider() could be set to null here, but lets not try to be clever.
                    finish();
                }
                break;
        }
    }

    @Override
    public void onSaveAuthTokenSuccess() {
    }

    @Override
    public void onSignOut() {
    }

    @Override
    public void onSaveAuthTokenFailure() {
        throw new IllegalStateException("Should not happen..");
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_HOME;
    }

    public static class FragmentAdapter extends FragmentPagerAdapter {
        private final static int TAB_COUNT = 3;
        private final String[] tabTitles = new String[TAB_COUNT];

        public FragmentAdapter(FragmentManager fm, Context context) {
            super(fm);
            tabTitles[0] = context.getString(R.string.tab_title_your_shows);
            tabTitles[1] = context.getString(R.string.tab_title_all_shows);
            tabTitles[2] = context.getString(R.string.tab_title_nearby);
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
                    return new AllShowsFragment();
                case 2:
                    return new NearbyVenuesFragment();

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
