package com.livenation.mobile.android.na.presenters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteAddView;
import com.livenation.mobile.android.na.presenters.views.FavoriteRemoveView;
import com.livenation.mobile.android.na.presenters.views.FavoritesView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.SingleVenueParameters;

public class FavoritesPresenter extends
		BasePresenter<FavoritesPresenter.FavoritesState> implements Presenter<FavoritesView>,
		StateListener<FavoritesPresenter.FavoritesState> {
	private AddFavoritePresenter addFavoritePresenter = new AddFavoritePresenter();
	private RemoveFavoritePresenter removeFavoritePresenter = new RemoveFavoritePresenter();
	
	public static final String INTENT_DATA_KEY = FavoritesPresenter.class.getName();
	public static final String PARAMETER_EVENT_ID = "item_id";
	@Override
	public void initialize(Context context, Bundle args, FavoritesView view) {
		FavoritesState state = new FavoritesState(FavoritesPresenter.this, args, view);
		state.run();
	}

	@Override
	public void onStateReady(FavoritesState state) {
		super.onStateReady(state);
		FavoritesView view = state.getView();
		
		List<Favorite> result = state.getResult();
		view.setFavorites(result);
	}

	@Override
	public void onStateFailed(int failureCode, FavoritesState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}
	
	public void addFavorite(Context context, Bundle args, FavoriteAddView view) {
		addFavoritePresenter.initialize(context, args, view);
	}
	
	public void removeFavorite(Context context, Bundle args, FavoriteRemoveView view) {
		removeFavoritePresenter.initialize(context, args, view);
	}
	
	public Bundle getArgsBundle(Favorite favorite) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_DATA_KEY, favorite);
		return bundle;
	}
	
	static class FavoritesState extends BaseState<ArrayList<Favorite>, FavoritesView> implements
			LiveNationApiService.GetFavoritesApiCallback {
		private SingleVenueParameters apiParams;
	
		public static final int FAILURE_API_GENERAL = 0;

		public FavoritesState(StateListener<FavoritesState> listener, Bundle args, FavoritesView view) {
			super(listener, args, INTENT_DATA_KEY, view);
		}

		@Override
		public void onHasResult(ArrayList<Favorite> result) {
			onGetFavorites(result);
		}
		
		@Override
		public void retrieveResult() {
			getApiService().getFavorites(FavoritesState.this);			
		}
		
		@Override
		public void applyArgs(Bundle args) {
			super.applyArgs(args);
			apiParams = ApiParameters.createSingleVenueParameters();

			String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
			long venueId = DataModelHelper.getNumericEntityId(venueIdRaw);
			apiParams.setVenueId(venueId);
		}

		@Override
		public void onGetFavorites(List<Favorite> favorites) {
			setResult((ArrayList<Favorite>) favorites);
			notifyReady();
		}
		
		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}
		
	}
	
	private static class AddFavoritePresenter extends
			BasePresenter<AddFavoritePresenter.AddFavoriteState> implements
			Presenter<FavoriteAddView>,
			StateListener<AddFavoritePresenter.AddFavoriteState> {

		@Override
		public void initialize(Context context, Bundle args,
				FavoriteAddView view) {
			AddFavoriteState state = new AddFavoriteState(AddFavoritePresenter.this, args, view);
			state.run();
		}
		
		@Override
		public void onStateReady(AddFavoriteState state) {
			super.onStateReady(state);
			FavoriteAddView view = state.getView();
			view.onFavoriteAddSuccess();
		}

		@Override
		public void onStateFailed(int failureCode, AddFavoriteState state) {
			super.onStateFailed(failureCode, state);
			FavoriteAddView view = state.getView();
			view.onFavoriteAddFailed();
		}
		
		static class AddFavoriteState extends BaseState<Favorite, FavoriteAddView> implements
		LiveNationApiService.AddRemoveFavoriteApiCallback {
			private ApiParameters.FavoriteWithNameParameters apiParams;
			
			public static final int FAILURE_API_GENERAL = 0;

			public AddFavoriteState(StateListener<AddFavoriteState> listener, Bundle args, FavoriteAddView view) {
				super(listener, args, INTENT_DATA_KEY, view);
			}

			@Override
			public void onHasResult(Favorite result) {
				retrieveResult();
			}
			
			@Override
			public void retrieveResult() {
				getApiService().addFavorite(apiParams, AddFavoriteState.this);			
			}
			
			@Override
			public void applyArgs(Bundle args) {
				super.applyArgs(args);
				apiParams = ApiParameters.createFavoriteWithNameParameters();
				Favorite favorite = (Favorite) args.getSerializable(INTENT_DATA_KEY);
				String idValue = Long.valueOf(favorite.getId()).toString();
				apiParams.setId(idValue, favorite.getType());
				apiParams.setName(favorite.getName());
			}

			@Override
			public void onSuccess() {
				notifyReady();
			}
			
			@Override
			public void onFailure() {
				notifyFailed(FAILURE_API_GENERAL);
			}

			@Override
			public void onFailure(int failureCode, String message) {
				notifyFailed(FAILURE_API_GENERAL);
			}
			
		}
	}
	
	private static class RemoveFavoritePresenter extends
			BasePresenter<RemoveFavoritePresenter.RemoveFavoriteState> implements
			Presenter<FavoriteRemoveView>,
			StateListener<RemoveFavoritePresenter.RemoveFavoriteState> {
	
		@Override
		public void initialize(Context context, Bundle args,
				FavoriteRemoveView view) {
			RemoveFavoriteState state = new RemoveFavoriteState(
					RemoveFavoritePresenter.this, args, view);
			state.run();
		}

		@Override
		public void onStateReady(RemoveFavoriteState state) {
			super.onStateReady(state);
			FavoriteRemoveView view = state.getView();
			view.onFavoriteRemoveSuccess();
		}

		@Override
		public void onStateFailed(int failureCode, RemoveFavoriteState state) {
			super.onStateFailed(failureCode, state);
			FavoriteRemoveView view = state.getView();
			view.onFavoriteRemoveFailed();
		}
		
		static class RemoveFavoriteState extends
				BaseState<Favorite, FavoriteRemoveView> implements
				LiveNationApiService.AddRemoveFavoriteApiCallback {
			private ApiParameters.FavoriteParameters apiParams;

			public static final int FAILURE_API_GENERAL = 0;

			public RemoveFavoriteState(StateListener<RemoveFavoriteState> listener,
					Bundle args, FavoriteRemoveView view) {
				super(listener, args, INTENT_DATA_KEY, view);
			}

			@Override
			public void onHasResult(Favorite result) {
				retrieveResult();
			}

			@Override
			public void retrieveResult() {
				getApiService().removeFavorite(apiParams, RemoveFavoriteState.this);
			}

			@Override
			public void applyArgs(Bundle args) {
				super.applyArgs(args);
				apiParams = ApiParameters.createFavoriteParameters();
				Favorite favorite = (Favorite) args
						.getSerializable(INTENT_DATA_KEY);
				String idValue = Long.valueOf(favorite.getId()).toString();
				apiParams.setId(idValue, favorite.getType());
			}

			@Override
			public void onSuccess() {
				notifyReady();
			}

			@Override
			public void onFailure() {
				notifyFailed(FAILURE_API_GENERAL);
			}

			@Override
			public void onFailure(int failureCode, String message) {
				notifyFailed(FAILURE_API_GENERAL);
			}

		}
	}
}
