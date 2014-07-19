package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.VenueEventsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

public class VenueEventsPresenter extends
        BasePresenter<EventsView, VenueEventsPresenter.VenueEventsState> implements Presenter<EventsView>,
        StateListener<VenueEventsPresenter.VenueEventsState> {
    public static final String INTENT_DATA_KEY = VenueEventsPresenter.class.getName();
    public static final String PARAMETER_EVENT_ID = "venue_id";
    public static final String PARAMETER_LIMIT = "limit";

    @Override
    public void initialize(Context context, Bundle args, EventsView view) {
        VenueEventsState state = new VenueEventsState(VenueEventsPresenter.this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(VenueEventsState state) {
        super.onStateReady(state);

        EventsView view = state.getView();
        List<Event> events = state.getResult();
        view.setEvents(events);
    }

    @Override
    public void onStateFailed(int failureCode, VenueEventsState state) {
        super.onStateFailed(failureCode, state);
        // TODO: this
    }

    static class VenueEventsState extends BaseResultState<ArrayList<Event>, EventsView> implements
            BasicApiCallback<List<Event>> {
        private VenueEventsParameters apiParams;

        public VenueEventsState(StateListener<VenueEventsState> listener, Bundle args, EventsView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(ArrayList<Event> result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            LiveNationApplication.getLiveNationProxy().getVenueEvents(apiParams, VenueEventsState.this);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = new VenueEventsParameters();

            String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
            apiParams.setVenueId(DataModelHelper.getNumericEntityId(venueIdRaw));

            if (args.containsKey(PARAMETER_LIMIT)) {
                int limit = args.getInt(PARAMETER_LIMIT);
                apiParams.setPage(0, limit);
            }
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }


        @Override
        public void onErrorResponse(LiveNationError error) {
            int errorCode = error.getErrorCode();
            notifyFailed(errorCode);
        }

        @Override
        public void onResponse(List<Event> response) {
            setResult((ArrayList<Event>) response);
            notifyReady();
        }
    }
}
