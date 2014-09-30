package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.ui.fragments.WebViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/25/14.
 */
public class LegalActivity extends LiveNationFragmentActivity {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final String TERMS_OF_USE_FRAGMENT_TAB = "terms_of_use_fragment_tab";
    private static final String PRIVACY_POLICY_FRAGMENT_TAB = "privacy_policy_tab";
    private static final String CREDITS_FRAGMENT_TAB = "credits_tab";
    private String[] tagsByIndex = new String[]{TERMS_OF_USE_FRAGMENT_TAB, PRIVACY_POLICY_FRAGMENT_TAB, CREDITS_FRAGMENT_TAB};
    private List<Fragment> fragmentsByIndex = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_legal);
        fragmentsByIndex.clear();
        ActionBar.Tab termOfUseTab, privacyPolicyTab, creditsTab;
        WebViewFragment termOfUseTabFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(TERMS_OF_USE_FRAGMENT_TAB);
        WebViewFragment privacyPolicyTabFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(PRIVACY_POLICY_FRAGMENT_TAB);
        WebViewFragment creditsTabFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(CREDITS_FRAGMENT_TAB);

        if (termOfUseTabFragment == null) {
            termOfUseTabFragment = WebViewFragment.newInstance(getString(R.string.legal_terms_of_use_url));
        }
        fragmentsByIndex.add(termOfUseTabFragment);
        if (privacyPolicyTabFragment == null) {
            privacyPolicyTabFragment = WebViewFragment.newInstance(getString(R.string.legal_privacy_policy_url));
        }
        fragmentsByIndex.add(privacyPolicyTabFragment);
        if (creditsTabFragment == null) {
            creditsTabFragment = WebViewFragment.newInstance(getString(R.string.credits_url));
        }
        fragmentsByIndex.add(creditsTabFragment);
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        termOfUseTab = actionBar.newTab().setText(getString(R.string.legal_terms_of_use));
        privacyPolicyTab = actionBar.newTab().setText(getString(R.string.legal_privacy_policy));
        creditsTab = actionBar.newTab().setText(getString(R.string.legal_credits));

        termOfUseTab.setTabListener(new WebViewTabListener(termOfUseTabFragment, TERMS_OF_USE_FRAGMENT_TAB));
        privacyPolicyTab.setTabListener(new WebViewTabListener(privacyPolicyTabFragment, PRIVACY_POLICY_FRAGMENT_TAB));
        creditsTab.setTabListener(new WebViewTabListener(creditsTabFragment, CREDITS_FRAGMENT_TAB));


        actionBar.addTab(termOfUseTab);
        actionBar.addTab(privacyPolicyTab);
        actionBar.addTab(creditsTab);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            int index = savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
            getActionBar().setSelectedNavigationItem(index);
            int j;
            for (j = 0; j < fragmentsByIndex.size(); j++) {
                Fragment fragment = fragmentsByIndex.get(j);
                if (j == index) {
                    if (!fragment.isAdded()) {
                        getFragmentManager().beginTransaction().add(R.id.activity_legal_container, fragment, tagsByIndex[j]).commit();
                    } else {
                        getFragmentManager().beginTransaction().show(fragment).commit();
                    }
                } else {
                    if (fragment.isAdded()) {
                        getFragmentManager().beginTransaction().hide(fragment).commit();
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_LEGAL_CREDITS;
    }

    private class WebViewTabListener implements ActionBar.TabListener {

        private Fragment fragment;
        private String tag;

        private WebViewTabListener(Fragment fragment, String tag) {
            this.fragment = fragment;
            this.tag = tag;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (!fragment.isAdded()) {
                getFragmentManager().beginTransaction().add(R.id.activity_legal_container, fragment, tag).commit();
            } else {
                getFragmentManager().beginTransaction().show(fragment).commit();
            }
            if (TERMS_OF_USE_FRAGMENT_TAB.equals(tag)) {
                LiveNationAnalytics.track(AnalyticConstants.TERMS_OF_USE_TAP, AnalyticsCategory.LEGAL);
            } else {
                LiveNationAnalytics.track(AnalyticConstants.PRIVACY_POLICY_TAP, AnalyticsCategory.LEGAL);
            }

        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (fragment.isAdded()) {
                getFragmentManager().beginTransaction().hide(fragment).commit();
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_LEGAL_CREDITS;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    int index = getActionBar().getSelectedNavigationIndex();
                    WebViewFragment webViewFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(tagsByIndex[index]);
                    if(webViewFragment != null && webViewFragment.getWebView() != null && webViewFragment.getWebView().canGoBack()){
                        webViewFragment.getWebView().goBack();
                    }else{
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}
