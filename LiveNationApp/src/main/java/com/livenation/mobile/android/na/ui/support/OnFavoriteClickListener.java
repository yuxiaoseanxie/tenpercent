package com.livenation.mobile.android.na.ui.support;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.FavoriteWithNameParameters;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.na.analytics.Props;

public class OnFavoriteClickListener {
    public static class OnVenueFavoriteClick extends OnFavoriteClick {
        private final Venue venue;

        public OnVenueFavoriteClick(@NonNull Venue venue, AnalyticsCategory category) {
            super(null, category);
            this.venue = venue;
        }

        @Override
        public Favorite getFavorite() {
            return Favorite.fromVenue(venue);
        }
    }

    public static class OnFavoriteClick implements View.OnClickListener {
        private final Favorite favorite;
        private AnalyticsCategory category;

        public OnFavoriteClick(Favorite favorite, AnalyticsCategory category) {
            this.favorite = favorite;
            this.category = category;
        }

        public Favorite getFavorite() {
            return favorite;
        }

        private void trackFavoriteChanged(boolean added) {
            Favorite favorite = getFavorite();
            Props props = new Props();
            switch (favorite.getIntType()) {
                case Favorite.FAVORITE_ARTIST:

                    props.put(AnalyticConstants.ARTIST_ID, String.valueOf(favorite.getId()));
                    props.put(AnalyticConstants.ARTIST_NAME, favorite.getName());
                    if (added) {
                        props.put(AnalyticConstants.STATE, AnalyticConstants.STATE_FAVORITED_VALUE);
                    } else {
                        props.put(AnalyticConstants.STATE, AnalyticConstants.STATE_UNFAVORITED_VALUE);
                    }
                    LiveNationAnalytics.track(AnalyticConstants.FAVORITE_ARTIST_STAR_TAP, category, props);
                    break;
                case Favorite.FAVORITE_VENUE:
                    props.put(AnalyticConstants.VENUE_NAME, favorite.getName());
                    props.put(AnalyticConstants.VENUE_ID, String.valueOf(favorite.getId()));
                    if (added) {
                        props.put(AnalyticConstants.STATE, AnalyticConstants.STATE_FAVORITED_VALUE);
                    } else {
                        props.put(AnalyticConstants.STATE, AnalyticConstants.STATE_UNFAVORITED_VALUE);
                    }
                    LiveNationAnalytics.track(AnalyticConstants.FAVORITE_VENUE_STAR_TAP, category, props);

                    break;
                default:
                    throw new IllegalStateException("Unknown favorite ID");

            }
        }

        @Override
        public void onClick(View v) {
            CompoundButton checkbox = (CompoundButton) v;
            checkbox.setChecked(checkbox.isChecked());
            String idValue = Long.valueOf(getFavorite().getId()).toString();
            if (checkbox.isChecked()) {
                FavoriteWithNameParameters apiParams = new FavoriteWithNameParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().addFavorite(apiParams, null);
            } else {
                FavoriteWithNameParameters apiParams = new FavoriteWithNameParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().removeFavorite(apiParams, null);
            }

            trackFavoriteChanged(checkbox.isChecked());
        }
    }
}

