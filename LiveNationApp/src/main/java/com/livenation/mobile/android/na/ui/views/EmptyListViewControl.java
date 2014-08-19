package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.livenation.mobile.android.na.R;

/**
 * Created by cchilton on 4/4/14.
 * <p/>
 * This class represents a view that shows while a ListView is empty.
 * <p/>
 * Its default mode (LOADING) is to show a spinner progress wheel, to illustrate there is a request
 * occuring in the background.
 * <p/>
 * Once the request completes, if there are no items for the listview, the mode of this View should
 * be changed to "NO_DATA", so that it can hide the loading spinning wheel.
 * <p/>
 * If the request for data fails, for example due to network connectivity, then the mode of this
 * View can be set to "RETRY", where it will present a rety button to the user. This is similar to
 * how it works in the Play Store.
 * <p/>
 * The activity/fragment can set a retry click listener via the "setRetryOnClickListener()" to handle
 * retrying the request.
 */
public class EmptyListViewControl extends LinearLayout {
    private static final String VIEW_MODE_STATE_KEY = "viewModeState";
    private static final String INSTANCE_STATE_KEY = "instanceState";
    private final LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private ViewMode currentMode;
    private View loading;
    private View retry;
    private View noData;
    private View inactive;

    public EmptyListViewControl(Context context) {
        super(context);
        initializeViews(context);
    }

    public EmptyListViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public EmptyListViewControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE_KEY, super.onSaveInstanceState());
        bundle.putInt(VIEW_MODE_STATE_KEY, getViewMode().ordinal());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            ViewMode viewMode = ViewMode.values()[bundle.getInt(VIEW_MODE_STATE_KEY)];
            setViewMode(viewMode);
            state = bundle.getParcelable(INSTANCE_STATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }

    public ViewMode getViewMode() {
        return currentMode;
    }

    /**
     * Changes the display mode of the view.
     * LOADING = show spinning loading wheel
     * NO_DATA = show nothing, ie. an empty list (showing the loading wheel here would give the user
     * the wrong idea)
     * RETRY = show retry button to try the operation that loads the list again.
     *
     * @param mode The view state that this view is meant to represent.
     */
    public void setViewMode(ViewMode mode) {
        if (currentMode == mode) {
            //if already in the requested view mode, don't bother with
            // another redundant layout request
            return;
        }
        currentMode = mode;
        removeAllViews();
        switch (mode) {
            case INACTIVE:
                addView(inactive, containerParams);
                break;
            case LOADING:
                addView(loading, containerParams);
                break;
            case NO_DATA:
                addView(noData, containerParams);
                break;
            case RETRY:
                addView(retry, containerParams);
                break;
            default:
        }
        invalidate();
    }

    /**
     * Set the click listener for the Retry button that shows when this View's mode is set to "RETRY"
     *
     * @param listener
     */
    public void setRetryOnClickListener(OnClickListener listener) {
        View view = this.retry.findViewById(android.R.id.button1);
        view.setOnClickListener(listener);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        inactive = inflater.inflate(R.layout.view_empty_list_inactive, this, false);
        loading = inflater.inflate(R.layout.view_empty_list_loading, this, false);
        retry = inflater.inflate(R.layout.view_empty_list_retry, this, false);
        noData = new View(context);

        setViewMode(ViewMode.LOADING);
    }

    public static enum ViewMode {INACTIVE, LOADING, NO_DATA, RETRY}
}
