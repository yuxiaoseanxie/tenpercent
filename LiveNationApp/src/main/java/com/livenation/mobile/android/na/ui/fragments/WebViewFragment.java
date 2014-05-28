package com.livenation.mobile.android.na.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FrameLayout relativeLayout = new FrameLayout(getActivity());
        final ProgressBar progressBar = new ProgressBar(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        progressBar.setLayoutParams(params);
        webView = new WebView(getActivity());
        relativeLayout.addView(webView);
        relativeLayout.addView(progressBar);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });


        if (savedInstanceState == null) {
            String url = getArguments().getString(ARG_URL);
            webView.loadUrl(url);
        } else {
            webView.restoreState(savedInstanceState);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        return relativeLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

}