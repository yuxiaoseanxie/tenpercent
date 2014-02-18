package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.AccountPresenter;

public class AccountSignInFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_account_sign_in, container, false);
		Button button = (Button) result.findViewById(R.id.fragment_account_sign_in_button);
		button.setOnClickListener(new OnSignInClick());
		return result;
	}
	
	private class OnSignInClick implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			startActivity(new AccountPresenter().getFacebookSigninIntent(getActivity()));
		}
		
	}
}
