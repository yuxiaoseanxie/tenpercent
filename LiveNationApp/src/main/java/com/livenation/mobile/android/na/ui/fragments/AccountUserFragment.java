package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountUserFragment extends LiveNationFragment implements
		AccountUserView, AccountSignOutView {
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
		image.setOnClickListener(new OnImageClickListener());
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		getAccountPresenters().getGetUser().initialize(getActivity(), getArguments(), this);
	}

	@Override
	public void setUser(User user) {
		name.setText(user.getDisplayName());
		email.setText(user.getEmail());
		image.setImageUrl(user.getUrl(), getImageLoader());
	}

	@Override
	public void onSignOut() {
		//Update the parent fragment view to reflect signed out state
		getParentFragment().onResume();
	}
	
	private class OnImageClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
            getAccountPresenters().getSignOut().initialize(getActivity(), null, AccountUserFragment.this);
		}
		
	}
}
