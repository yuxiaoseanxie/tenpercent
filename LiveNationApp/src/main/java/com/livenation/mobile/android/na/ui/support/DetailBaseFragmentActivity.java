package com.livenation.mobile.android.na.ui.support;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.ui.FavoriteSearchActivity;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class DetailBaseFragmentActivity extends LiveNationFragmentActivity {
    protected Bundle args;
    private MenuItem shareItem;

    @Override
    protected void onCreate(Bundle savedInstanceState, int res) {
        super.onCreate(savedInstanceState, res);
        args = getIntent().getExtras();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isShareAvailable()) {
            invalidateOptionsMenu();
        }
    }

    //region Menus

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_base_menu, menu);

        shareItem = menu.findItem(R.id.action_share);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_search) {
            onSearch();
            return true;
        } else if (i == R.id.action_share) {
            onShare();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (shareItem == null)
            throw new IllegalStateException("Subclasses of DetailBaseFragmentActivity must call super.onCreateOptionsMenu");

        shareItem.setEnabled(isShareAvailable());

        return true;
    }

    //endregion


    //region Actions

    protected void onShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getShareSubject());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText());
        startActivity(Intent.createChooser(shareIntent, getShareIntentChooserTitle()));
        OmnitureTracker.trackState(AnalyticConstants.OMNITURE_SCREEN_SHARE, null);
    }

    protected void onSearch() {
        startActivity(new Intent(this, FavoriteSearchActivity.class));
    }

    //endregion


    //region Sharing

    protected String getShareIntentChooserTitle() {
        return getString(R.string.share_chooser_title);
    }

    protected abstract boolean isShareAvailable();

    public void invalidateIsShareAvailable() {
        invalidateOptionsMenu();
    }

    protected abstract String getShareSubject();

    protected abstract String getShareText();

    //endregion
}
