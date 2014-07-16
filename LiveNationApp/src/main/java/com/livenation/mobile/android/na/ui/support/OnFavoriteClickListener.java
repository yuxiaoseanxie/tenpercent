package com.livenation.mobile.android.na.ui.support;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteAddView;
import com.livenation.mobile.android.na.presenters.views.FavoriteRemoveView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.segment.android.models.Props;

public class OnFavoriteClickListener {
    public static class OnVenueFavoriteClick extends AbstractOnFavoriteClick {
        private final Venue venue;

        public OnVenueFavoriteClick(Venue venue, FavoritesPresenter favoritesPresenter, Activity activity, AnalyticsCategory category) {
            super(favoritesPresenter, activity, category);
            this.venue = venue;
        }

        public Favorite getFavorite() {
            Favorite favorite = new Favorite();
            favorite.setName(venue.getName());
            favorite.setType(Favorite.FAVORITE_VENUE_KEY);
            favorite.setId(venue.getNumericId());
            return favorite;
        }
    }

    public static class OnFavoriteClick extends AbstractOnFavoriteClick {
        private final Favorite favorite;

        public OnFavoriteClick(Favorite favorite, FavoritesPresenter favoritesPresenter, Context context, AnalyticsCategory category) {
            super(favoritesPresenter, context, category);
            this.favorite = favorite;
        }

        public Favorite getFavorite() {
            return favorite;
        }
    }

    private static abstract class AbstractOnFavoriteClick implements OnClickListener, FavoriteAddView, FavoriteRemoveView {
        private final FavoritesPresenter favoritesPresenter;
        private final Context context;
        private boolean inProgress;
        private AnalyticsCategory category;

        public AbstractOnFavoriteClick(FavoritesPresenter favoritesPresenter, Context context, AnalyticsCategory category) {
            this.favoritesPresenter = favoritesPresenter;
            this.context = context;
            setInProgress(false);
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            CheckBox checkbox = (CheckBox) v;

            final boolean value = checkbox.isChecked();
            if (isInProgress()) {
                checkbox.setChecked(!value);
            }

            setInProgress(true);

            Favorite favorite = getFavorite();

            Bundle args = getFavoritesPresenter().getArgsBundle(favorite);
            if (value) {
                getFavoritesPresenter().addFavorite(getContext(), args, AbstractOnFavoriteClick.this);
                checkbox.setChecked(value);
            } else {
                getFavoritesPresenter().removeFavorite(getContext(), args, AbstractOnFavoriteClick.this);
                checkbox.setChecked(value);
            }
        }

        @Override
        public void onFavoriteRemoveSuccess() {
            setInProgress(false);
            trackFavoriteChanged(false);
        }

        @Override
        public void onFavoriteRemoveFailed() {
            setInProgress(false);
        }

        @Override
        public void onFavoriteAddSuccess() {
            setInProgress(false);
            trackFavoriteChanged(true);
        }

        @Override
        public void onFavoriteAddFailed() {
            setInProgress(false);
        }

        public FavoritesPresenter getFavoritesPresenter() {
            return favoritesPresenter;
        }

        public Context getContext() {
            return context;
        }

        public boolean isInProgress() {
            return inProgress;
        }

        private void setInProgress(boolean inProgress) {
            this.inProgress = inProgress;
        }

        public abstract Favorite getFavorite();

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

