package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.WebViewFragment;

/**
 * Created by elodieferrais on 4/25/14.
 */
public class LegalActivity extends LiveNationFragmentActivity {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final String TERMS_OF_USE_FRAGMENT_TAG = "terms_of_use_fragment_tab";
    private static final String PRIVACY_POLICY_FRAGMENT_TAG = "terms_of_use_fragment_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);


        ActionBar.Tab termOfUseTab, privacyPolicyTab;
        WebViewFragment termOfUseTabFragment;
        WebViewFragment privacyPolicyTabFragment;
        if (savedInstanceState != null) {
            termOfUseTabFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(TERMS_OF_USE_FRAGMENT_TAG);
            privacyPolicyTabFragment = (WebViewFragment) getFragmentManager().findFragmentByTag(PRIVACY_POLICY_FRAGMENT_TAG);
        } else {
            termOfUseTabFragment = WebViewFragment.newInstance(getString(R.string.legal_terms_of_use_url));
            privacyPolicyTabFragment = WebViewFragment.newInstance(getString(R.string.legal_privacy_policy_url));
        }

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        termOfUseTab = actionBar.newTab().setText(getString(R.string.legal_terms_of_use));
        privacyPolicyTab = actionBar.newTab().setText(getString(R.string.legal_privacy_policy));

        termOfUseTab.setTabListener(new WebViewTabListener(termOfUseTabFragment, TERMS_OF_USE_FRAGMENT_TAG));
        privacyPolicyTab.setTabListener(new WebViewTabListener(privacyPolicyTabFragment, PRIVACY_POLICY_FRAGMENT_TAG));

        actionBar.addTab(termOfUseTab);
        actionBar.addTab(privacyPolicyTab);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
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

        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.hide(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }
}
