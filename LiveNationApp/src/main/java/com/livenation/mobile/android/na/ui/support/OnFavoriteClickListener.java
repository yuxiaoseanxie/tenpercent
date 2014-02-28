package com.livenation.mobile.android.na.ui.support;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteAddView;
import com.livenation.mobile.android.na.presenters.views.FavoriteRemoveView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LineupEntry;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

public class OnFavoriteClickListener {
	public static class OnVenueFavoriteClick extends AbstractOnFavoriteClick {
		private final Venue venue;
			
		public OnVenueFavoriteClick(Venue venue, FavoritesPresenter favoritesPresenter, Activity activity) {
			super(favoritesPresenter, activity);
			this.venue = venue;
		}

		public Favorite getFavorite() {
			Favorite favorite = new Favorite();
			favorite.setName(venue.getName());
			favorite.setType(Favorite.FAVORITE_VENUE_KEY);
			favorite.setId(venue.getNumericId());
			return favorite;
		};
	}

	public static class OnArtistFavoriteClick extends AbstractOnFavoriteClick {
		private final LineupEntry artist;
			
		public OnArtistFavoriteClick(LineupEntry artist, FavoritesPresenter favoritesPresenter, Activity activity) {
			super(favoritesPresenter, activity);
			this.artist = artist;
		}

		public Favorite getFavorite() {
			Favorite favorite = new Favorite();
			favorite.setName(artist.getName());
			favorite.setType(Favorite.FAVORITE_ARTIST_KEY);
			favorite.setId(artist.getNumericId());
			return favorite;
		};
	}
	
	public static class OnFavoriteClick extends AbstractOnFavoriteClick {
		private final Favorite favorite;
			
		public OnFavoriteClick(Favorite favorite, FavoritesPresenter favoritesPresenter, Activity activity) {
			super(favoritesPresenter, activity);
			this.favorite = favorite;
		}

		public Favorite getFavorite() {
			return favorite;
		};
	}

	private static abstract class AbstractOnFavoriteClick implements OnClickListener, FavoriteAddView, FavoriteRemoveView {
		private boolean inProgress;
		private final FavoritesPresenter favoritesPresenter;
		private final Activity activity;
		
		public AbstractOnFavoriteClick(FavoritesPresenter favoritesPresenter, Activity activity) {
			this.favoritesPresenter = favoritesPresenter;
			this.activity = activity;
			setInProgress(false);
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
				getFavoritesPresenter().addFavorite(getActivity(), args, AbstractOnFavoriteClick.this);
				checkbox.setChecked(value);
			} else {
				getFavoritesPresenter().removeFavorite(getActivity(), args, AbstractOnFavoriteClick.this);
				checkbox.setChecked(value);
			}
		}

		@Override
		public void onFavoriteRemoveSuccess() {
			setInProgress(false);
		}

		@Override
		public void onFavoriteRemoveFailed() {
			setInProgress(false);
		}

		@Override
		public void onFavoriteAddSuccess() {
			setInProgress(false);
		}

		@Override
		public void onFavoriteAddFailed() {
			setInProgress(false);
		}
		
		public FavoritesPresenter getFavoritesPresenter() {
			return favoritesPresenter;
		}
		
		public Activity getActivity() {
			return activity;
		}
		
		private void setInProgress(boolean inProgress) {
			this.inProgress = inProgress;	
		}
		
		public boolean isInProgress() {
			return inProgress;
		}
		
		public abstract Favorite getFavorite();
	
	}

}

