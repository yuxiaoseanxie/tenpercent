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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);


        ActionBar.Tab termOfUseTab, privacyPolicyTab;
        WebViewFragment termOfUseTabFragment = WebViewFragment.newInstance(getString(R.string.terms_of_use_url));
        WebViewFragment privacyPolicyTabFragment = WebViewFragment.newInstance(getString(R.string.privacy_policy_url));

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        termOfUseTab = actionBar.newTab().setText(getString(R.string.terms_of_use));
        privacyPolicyTab = actionBar.newTab().setText(getString(R.string.privacy_policy));

        termOfUseTab.setTabListener(new WebViewTabListener(termOfUseTabFragment));
        privacyPolicyTab.setTabListener(new WebViewTabListener(privacyPolicyTabFragment));

        actionBar.addTab(termOfUseTab);
        actionBar.addTab(privacyPolicyTab);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
    }


    private class WebViewTabListener implements ActionBar.TabListener {

        private Fragment fragment;

        private WebViewTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (!fragment.isAdded()) {
                getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
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
