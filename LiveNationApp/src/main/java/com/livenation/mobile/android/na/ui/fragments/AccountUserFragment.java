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
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.utils.BitmapRequest;
import com.livenation.mobile.android.na.utils.ImageUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountUserFragment extends LiveNationFragment implements
        AccountUserView, Response.Listener<Bitmap>, Response.ErrorListener {
    private TextView name;
    private TextView email;

    private ImageView image;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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

        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.BroadCastReceiver.LOGOUT));
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
            bitmap = ImageUtils.getCircleBitmap(bitmap, getResources().getDimensionPixelSize(R.dimen.imageview_stroke_width));
            image.setImageBitmap(bitmap);
        } else {
            image.setImageBitmap(null);
            //TODO default image
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).unregisterReceiver(broadcastReceiver);

    }
}
