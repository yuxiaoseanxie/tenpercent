package com.livenation.mobile.android.na.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.providers.sso.SsoUpdatedUserCallback;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.utils.BitmapRequest;
import com.livenation.mobile.android.na.utils.ImageUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoManager;

public class AccountUserFragment extends LiveNationFragment implements
        AccountUserView, Response.Listener<Bitmap>, Response.ErrorListener {
    private TextView name;
    private TextView email;

    private ImageView image;
    private BroadcastReceiver logoutBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    };
    private BitmapRequest bitmapRequest;
    private RequestQueue requestQueue = Volley.newRequestQueue(LiveNationApplication.get().getApplicationContext());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_user, container,
                false);
        name = (TextView) view.findViewById(R.id.fragment_account_user_name);
        email = (TextView) view.findViewById(R.id.fragment_account_user_email);
        image = (ImageView) view.findViewById(R.id.fragment_account_user_image);

        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).registerReceiver(logoutBroadcastReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGOUT_INTENT_FILTER));

        //User cached data
        User user = LoginHelper.getSavedUser();
        setUser(user, LoginHelper.getAuthConfiguration());

        LoginHelper.getUpdatedUser(new SsoUpdatedUserCallback() {
            @Override
            public void onResponse(boolean hasChanged, String accessToken, User user) {
                if (hasChanged) {
                    setUser(user, LoginHelper.getAuthConfiguration());
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                //When the login fail, the user is automatically removed and the "logout" broadcast is triggered.
                //That's we this fragment should be destroy when the onErrorResponse method is called.
            }
        }, getActivity());

        return view;
    }

    @Override
    public void setUser(User user, SsoManager.AuthConfiguration authConfiguration) {
        if (null == user) {
            getParentFragment().onResume();
            return;
        }

        name.setText(user.getDisplayName());
        email.setText(user.getEmail());
        int logoId = -1;
        if (authConfiguration.getSsoProviderId() != null) {
            switch (authConfiguration.getSsoProviderId()) {
                case SSO_FACEBOOK:
                    logoId = R.drawable.facebook_logo;
                    break;
                case SSO_GOOGLE:
                    logoId = R.drawable.google_plus_logo;
                    break;
            }

        }
        email.setCompoundDrawablesWithIntrinsicBounds(logoId, 0, 0, 0);

        bitmapRequest = new BitmapRequest(Request.Method.GET, user.getUrl(), this, this);
        requestQueue.add(bitmapRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        //TODO default image
    }

    @Override
    public void onResponse(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = ImageUtils.getCircleBitmap(bitmap, getResources().getDimensionPixelSize(R.dimen.one_dp));
            image.setImageBitmap(bitmap);
        } else {
            image.setImageBitmap(null);
            //TODO default image
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).unregisterReceiver(logoutBroadcastReceiver);

    }
}
