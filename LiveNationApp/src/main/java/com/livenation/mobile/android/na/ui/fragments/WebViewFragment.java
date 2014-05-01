package com.livenation.mobile.android.na.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.livenation.mobile.android.na.R;

/**
 * Created by elodieferrais on 4/28/14.
 */
public class WebViewFragment extends Fragment {

    private static final String ARG_URL = "arg_url";
    private WebView webView;

    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        webView = new WebView(getActivity());
        if (savedInstanceState == null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());

            String url = getArguments().getString(ARG_URL);
            webView.loadUrl(url);
        } else {
            webView.restoreState(savedInstanceState);
        }

        return webView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

}