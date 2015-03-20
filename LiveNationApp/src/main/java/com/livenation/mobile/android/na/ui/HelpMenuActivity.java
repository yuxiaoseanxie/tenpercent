package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.ui.adapters.HelpListAdapter;
import com.livenation.mobile.android.na.ui.fragments.HelpMenuFragment;
import com.livenation.mobile.android.na.ui.fragments.WebViewFragment;

import android.app.FragmentTransaction;
import android.mobile.livenation.com.livenationui.activity.base.LiveNationFragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by elodieferrais on 4/28/14.
 */
public class HelpMenuActivity extends LiveNationFragmentActivity implements AdapterView.OnItemClickListener {

    private WebViewFragment currentfragment;
    private HelpMenuFragment helpMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_help_menu);

        helpMenuFragment = (HelpMenuFragment) getFragmentManager().findFragmentById(R.id.activity_menu_fragment);
        helpMenuFragment.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HelpListAdapter adapter = (HelpListAdapter) helpMenuFragment.getListAdapter();
        String slug = adapter.getItem(position).slug;
        String url = getString(R.string.help_base_url_section) + slug;
        currentfragment = WebViewFragment.newInstance(url);
        getFragmentManager().beginTransaction().add(android.R.id.content, currentfragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_HELP;
    }


    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_HELP;
    }
}
