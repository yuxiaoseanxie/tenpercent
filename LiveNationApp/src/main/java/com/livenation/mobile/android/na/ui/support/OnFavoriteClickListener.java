package com.livenation.mobile.android.na.ui.support;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.FavoriteParameters;

import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

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
                FavoriteParameters apiParams = new FavoriteParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().addFavorite(null, apiParams);
            } else {
                FavoriteParameters apiParams = new FavoriteParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().removeFavorite(null, apiParams);
            }

            trackFavoriteChanged(checkbox.isChecked());
        }
    }
}

