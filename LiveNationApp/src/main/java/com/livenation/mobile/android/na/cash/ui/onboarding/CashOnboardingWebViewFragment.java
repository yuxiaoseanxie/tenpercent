package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.livenation.mobile.android.na.R;

public class CashOnboardingWebViewFragment extends CashOnboardingFragment {
    private static final String ARG_URL = "com.livenation.mobile.android.na.cash.ui.onboarding.CashOnboardingWebViewFragment.ARG_URL";

    private WebView webView;


    public static CashOnboardingWebViewFragment newInstance(@NonNull String url) {
        CashOnboardingWebViewFragment fragment = new CashOnboardingWebViewFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_URL, url);
        fragment.setArguments(arguments);

        return fragment;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_web_view, container, false);

        this.webView = (WebView) view.findViewById(R.id.fragment_cash_onboarding_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            String url = getUrl();
            if (!TextUtils.isEmpty(url))
                webView.loadUrl(url);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        webView.saveState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        webView.destroy();
    }


    private String getUrl() {
        return getArguments().getString(ARG_URL);
    }


    @Override
    public void next() {
        getCashRequestDetailsActivity().setupCompleted();
    }
}
