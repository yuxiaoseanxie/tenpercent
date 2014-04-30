package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.livenation.mobile.android.na.R;

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
        FragmentTab termOfUseTabFragment = FragmentTab.newInstance(getString(R.string.legal_terms_of_use_url));
        FragmentTab privacyPolicyTabFragment = FragmentTab.newInstance(getString(R.string.legal_privacy_policy_url));

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        termOfUseTab = actionBar.newTab().setText(getString(R.string.legal_terms_of_use));
        privacyPolicyTab = actionBar.newTab().setText(getString(R.string.legal_privacy_policy));

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
                getFragmentManager().beginTransaction().add(R.id.activity_legal_container, fragment).commit();
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


    private static class FragmentTab extends Fragment {

        private static final String ARG_URL = "arg_url";

        public static FragmentTab newInstance(String url) {
            FragmentTab fragment = new FragmentTab();
            Bundle args = new Bundle();
            args.putString(ARG_URL, url);
            fragment.setArguments(args);
            return fragment;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());

            String url = getArguments().getString(ARG_URL);
            webView.loadUrl(url);

            setRetainInstance(true);

            return webView;
        }
    }
}
