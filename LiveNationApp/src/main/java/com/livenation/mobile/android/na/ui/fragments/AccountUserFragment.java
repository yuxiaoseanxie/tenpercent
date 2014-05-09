package com.livenation.mobile.android.na.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountUserFragment extends LiveNationFragment implements
        AccountUserView {
    private TextView name;
    private TextView email;
    private NetworkImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_user, container,
                false);
        name = (TextView) view.findViewById(R.id.fragment_account_user_name);
        email = (TextView) view.findViewById(R.id.fragment_account_user_email);
        image = (NetworkImageView) view.findViewById(R.id.fragment_account_user_image);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onResume();
            }
        }, new IntentFilter(Constants.BroadCastReceiver.LOGOUT));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = LoginHelper.getSavedUser();
        SsoManager.AuthConfiguration authConfiguration = LoginHelper.getAuthConfiguration();
        setUser(user, authConfiguration);
    }

    @Override
    public void setUser(User user, SsoManager.AuthConfiguration authConfiguration) {
        if (null == user) {
            getParentFragment().onResume();
            return;
        }

        name.setText(user.getDisplayName());
        email.setText(user.getEmail());
        email.setCompoundDrawablesWithIntrinsicBounds(authConfiguration.getSsoProviderId().getLogoResId(), 0, 0, 0);
        image.setImageUrl(user.getUrl(), getImageLoader());
    }
}
