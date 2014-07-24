package com.livenation.mobile.android.na.ui.support;

import android.widget.CompoundButton;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.FavoriteWithNameParameters;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.segment.android.models.Props;

public class OnFavoriteClickListener {
    public static class OnVenueFavoriteClick extends OnFavoriteClick {
        private final Venue venue;

        public OnVenueFavoriteClick(Venue venue, AnalyticsCategory category) {
            super(null, category);
            this.venue = venue;
        }

        @Override
        public Favorite getFavorite() {
            Favorite favorite = new Favorite();
            favorite.setName(venue.getName());
            favorite.setType(Favorite.FAVORITE_VENUE_KEY);
            favorite.setId(venue.getNumericId());
            return favorite;
        }
    }

    public static class OnFavoriteClick implements CompoundButton.OnCheckedChangeListener {
        private final Favorite favorite;
        private AnalyticsCategory category;

        public OnFavoriteClick(Favorite favorite, AnalyticsCategory category) {
            this.favorite = favorite;
            this.category = category;
        }

        public Favorite getFavorite() {
            return favorite;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String idValue = Long.valueOf(getFavorite().getId()).toString();
            if (isChecked && !LiveNationLibrary.getFavoritesHelper().contains(getFavorite())) {
                FavoriteWithNameParameters apiParams = new FavoriteWithNameParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().addFavorite(apiParams, null);
            } else if (LiveNationLibrary.getFavoritesHelper().contains(getFavorite())) {
                FavoriteWithNameParameters apiParams = new FavoriteWithNameParameters();
                apiParams.setId(idValue, getFavorite().getType());
                apiParams.setName(getFavorite().getName());
                LiveNationApplication.getLiveNationProxy().removeFavorite(apiParams, null);
            }

            trackFavoriteChanged(isChecked);
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
    }
}

