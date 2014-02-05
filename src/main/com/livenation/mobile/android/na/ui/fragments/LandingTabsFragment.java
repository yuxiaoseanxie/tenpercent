package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost.TabSpec;

import com.livenation.mobile.android.na.R;

public class LandingTabsFragment extends Fragment {
	private FragmentTabHost tabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		tabHost = new FragmentTabHost(getActivity());
		tabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.activity_landing_container);
		
		String title = "";
		TabSpec tabSpec;
		
		title = getString(R.string.tab_title_all_shows);
		tabSpec = tabHost.newTabSpec("all_shows");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				AllShowsFragment.class, null);
		
		title = getString(R.string.tab_title_nearby);
		tabSpec = tabHost.newTabSpec("nearby");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				Fragment.class, null);
		
		title = getString(R.string.tab_title_your_shows);
		tabSpec = tabHost.newTabSpec("your_shows");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				Fragment.class, null);
		
		return tabHost;
	}
	

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		tabHost = null;
	}

}
