package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.UberHelper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.ui.OrderDetailsActivity;
import com.livenation.mobile.android.na.ui.OrderHistoryActivity;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.analytics.TimedEvent;
import com.livenation.mobile.android.ticketing.dialogs.PollingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.CommonUIResponseListener;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.livenation.mobile.android.ticketing.utils.orders.OrdersCacheManager;
import com.livenation.mobile.android.ticketing.utils.orders.UploadOrderHistoryTask;
import com.livenation.mobile.android.ticketing.utils.orders.ValueCallback;
import com.livenation.mobile.android.ticketing.widgets.VerticalDateView;
import com.mobilitus.tm.tickets.TicketLibrary;
import com.mobilitus.tm.tickets.interfaces.ResponseListener;
import com.mobilitus.tm.tickets.models.Cart;
import com.mobilitus.tm.tickets.models.Event;
import com.mobilitus.tm.tickets.models.OrderHistory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class OrderHistoryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final int LIMIT_PER_PAGE = 20;
    private static final int ACTIVITY_RESULT_UBER = 1;

    private UberClient uberClient;

    private SwipeRefreshLayout swipeRefreshLayout;

    private View emptyStateViewLoading;
    private View emptyStateViewNoOrders;
    private ViewGroup emptyView;

    private EmptyState emptyState = EmptyState.EMPTY;

    private EmptyStateObserver emptyStateObserver;
    private HistoryAdapter historyAdapter;
    private ArrayList<Cart> loadedCarts;

    private int pageOffset = 0;
    private boolean hasMore = false;
    private Handler offlinePromptHandler;
    private boolean isRefreshing = false;
    private SparseArray<Observable> uberCache = new SparseArray<>();

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loadedCarts = new ArrayList<Cart>();
        this.offlinePromptHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                Activity activity = getActivity();
                if (activity != null && OrdersCacheManager.getInstance().hasOrderHistorySaved(activity, pageOffset)) {
                    TicketingUtils.makeToast(activity.getApplicationContext(), R.string.toast_displaying_offline_order_history, Toast.LENGTH_SHORT).show();
                    loadOfflineCache(true);
                }

                return true;
            }
        });

        setRetainInstance(true);
        this.uberClient = new UberClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_order_history_swipe_layout);
        this.emptyView = (ViewGroup) view.findViewById(android.R.id.empty);
        setupEmptyStateViews();

        this.historyAdapter = new HistoryAdapter(getActivity());
        StickyListHeadersListView listView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
        listView.setAdapter(historyAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(new InfiniteScrollListener(swipeRefreshLayout, listView));
        listView.setAreHeadersSticky(false);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSinglePage(true);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);

        this.emptyStateObserver = new EmptyStateObserver();
        historyAdapter.registerDataSetObserver(emptyStateObserver);

        swipeRefreshLayout.setRefreshing(isRefreshing);
        if (!loadedCarts.isEmpty()) {
            historyAdapter.addAll(loadedCarts);
        } else {
            loadSinglePage(true);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        historyAdapter.unregisterDataSetObserver(emptyStateObserver);
    }

    //endregion


    public OrderHistoryActivity getOrderHistoryActivity() {
        return (OrderHistoryActivity) getActivity();
    }


    //region Empty State

    private void setupEmptyStateViews() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        this.emptyStateViewNoOrders = inflater.inflate(R.layout.sub_order_history_empty_no_content, emptyView, false);
        emptyStateViewNoOrders.findViewById(R.id.button_find_a_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                getActivity().finish();
            }
        });

        this.emptyStateViewLoading = inflater.inflate(R.layout.sub_order_history_empty_loading, emptyView, false);

        setEmptyState(emptyState);
    }

    private void setEmptyState(EmptyState state) {
        this.emptyState = state;
        emptyView.removeAllViews();

        switch (state) {
            case EMPTY:
                break;

            case NO_ORDERS:
                emptyView.addView(emptyStateViewNoOrders);
                break;

            case LOADING:
                emptyView.addView(emptyStateViewLoading);
                break;

            case SIGNED_OUT:
                getOrderHistoryActivity().showAccountActivity();
                break;
        }
    }

    private void updateEmptyState() {
        if (isRefreshing()) {
            setEmptyState(EmptyState.LOADING);
        } else if (Ticketing.getTicketService().hasSession()) {
            setEmptyState(EmptyState.NO_ORDERS);
        } else {
            setEmptyState(EmptyState.SIGNED_OUT);
        }
    }

    //endregion


    //region Loading Orders

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @SuppressWarnings("unchecked")
    private void uploadOrderHistory(List<Cart> orderHistory) {
        if (TicketingUtils.isCollectionEmpty(orderHistory) || !UploadOrderHistoryTask.shouldUpload())
            return;

        new UploadOrderHistoryTask().execute(orderHistory);
    }

    private void loadOfflineCache(final boolean isWaitingForOnlineOrders) {
        OrdersCacheManager.getInstance().loadOrderHistory(getActivity(), pageOffset, new ValueCallback<OrderHistory>() {
            @Override
            public void onValueLoaded(OrderHistory response) {
                if (!Ticketing.getTicketService().hasSession()) {
                    hasMore = false;
                    setRefreshing(false);
                    return;
                }

                if (!isWaitingForOnlineOrders)
                    setRefreshing(false);


                ArrayList<Cart> carts = response.getOrders();
                if (carts != null) {
                    historyAdapter.addAll(carts);
                    loadedCarts.addAll(carts);
                }


                hasMore = (carts != null && carts.size() >= LIMIT_PER_PAGE);

                updateEmptyState();
            }

            @Override
            public void onValueLoadFailed(Throwable error) {
                if (!Ticketing.getTicketService().hasSession()) {
                    hasMore = false;
                    setRefreshing(false);
                    return;
                }

                if (!isWaitingForOnlineOrders)
                    setRefreshing(false);

                Log.e(getClass().getSimpleName(), "Could not load cache", error);
            }
        });
    }

    public void loadSinglePage(final boolean clearAlreadyLoadedOrders) {
        if (isRefreshing())
            return;

        swipeRefreshLayout.setEnabled(Ticketing.getTicketService().hasSession());
        getOrderHistoryActivity().updateActionBar();
        if (!Ticketing.getTicketService().hasSession()) {
            historyAdapter.clear();
            loadedCarts.clear();

            updateEmptyState();
            hasMore = false;

            return;
        }

        if (clearAlreadyLoadedOrders)
            pageOffset = 0;

        setRefreshing(true);
        updateEmptyState();
        if (Ticketing.isConnectedToInternet()) {
            if (clearAlreadyLoadedOrders && pageOffset == 0)
                offlinePromptHandler.sendEmptyMessageDelayed(0, Constants.OFFLINE_MODE_CACHE_DELAY);

            PollingDialogFragment.PollingListener pollingListener = new PollingDialogFragment.PollingListener() {
                @Override
                public void onCountdownFinished() {
                    loadSinglePage(clearAlreadyLoadedOrders);
                }

                @Override
                public void onPollingCancelled() {
                    setRefreshing(false);
                }
            };
            final TimedEvent getOrderHistoryEvent = Ticketing.getAnalytics().startTimedEvent(TicketLibrary.Method.GET_ORDER_HISTORY);
            getOrderHistoryEvent.getProperties().put("Pagination Offset", pageOffset);
            Ticketing.getTicketService().getOrderHistory(pageOffset, LIMIT_PER_PAGE, new ResponseListener<OrderHistory>() {

                @Override
                public void onSuccess(int requestId, OrderHistory response) {
                    setRefreshing(false);
                    offlinePromptHandler.removeMessages(0);

                    if (!Ticketing.getTicketService().hasSession()) {
                        hasMore = false;
                        return;
                    }

                    if (pageOffset == 0)
                        uploadOrderHistory(response.getOrders());
                    OrdersCacheManager.getInstance().saveOrderHistory(getActivity(), response);

                    ArrayList<Cart> carts = response.getOrders();
                    if (clearAlreadyLoadedOrders) {
                        historyAdapter.clear();
                        loadedCarts.clear();
                    }

                    if (carts != null) {
                        historyAdapter.addAll(carts);
                        loadedCarts.addAll(carts);
                        hasMore = (carts.size() >= LIMIT_PER_PAGE);
                    } else {
                        hasMore = false;
                    }

                    updateEmptyState();

                    Ticketing.getAnalytics().finishTimedEvent(getOrderHistoryEvent);
                }
            }, new CommonUIResponseListener(getOrderHistoryActivity(), null, pollingListener) {
                @Override
                public void onError(int requestId, int httpStatusCode, com.mobilitus.tm.tickets.models.Error error) {
                    offlinePromptHandler.removeMessages(0);

                    if (!Ticketing.getTicketService().hasSession()) {
                        hasMore = false;
                        setRefreshing(false);
                        return;
                    }

                    if (!TicketingUtils.errorRequiresDisplay(httpStatusCode, error) && OrdersCacheManager.getInstance().hasOrderHistorySaved(getActivity(), pageOffset)) {
                        Log.e(getClass().getName(), "Could not load orders. Error: " + error);
                        loadOfflineCache(false);
                    } else {
                        setRefreshing(false);
                        super.onError(requestId, httpStatusCode, error);
                    }
                }
            }.finishTimedEvent(getOrderHistoryEvent));
        } else {
            loadOfflineCache(false);
        }
    }

    private void loadNextPage() {
        if (!hasMore || isRefreshing())
            return;

        if (!Ticketing.isConnectedToInternet() && !OrdersCacheManager.getInstance().hasOrderHistorySaved(getActivity(), pageOffset))
            return;

        pageOffset += LIMIT_PER_PAGE;
        loadSinglePage(false);
    }

    //endregion


    //region Displaying Order Details

    protected void showDetailsForCart(Cart cart) {
        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_CART, cart);
        startActivity(intent);
    }

    //region list item clicks

    @Override
    public void onItemClick(@NonNull AdapterView<?> adapterView, @NonNull View view, int position, long id) {
        Cart cart = historyAdapter.getItem(position);
        showDetailsForCart(cart);
    }

    private void onUberSignupClick(Cart cart) {
        UberHelper.trackUberkWebLaunch(AnalyticsCategory.YOUR_ORDERS);

        float lat = Double.valueOf(cart.getEvent().getVenue().getLatitude()).floatValue();
        float lng = Double.valueOf(cart.getEvent().getVenue().getLongitude()).floatValue();
        String venueAddress = UberHelper.getUberVenueAddress(cart.getEvent().getVenue());
        String venueName = UberHelper.getUberVenueName(cart.getEvent().getVenue());

        Intent intent = new Intent(Intent.ACTION_VIEW, UberHelper.getUberSignupLink(uberClient.getClientId(), lat, lng, venueAddress, venueName));
        startActivity(intent);
    }

    private void onUberRideClick(final Cart cart) {
        float lat = Double.valueOf(cart.getEvent().getVenue().getLatitude()).floatValue();
        float lng = Double.valueOf(cart.getEvent().getVenue().getLongitude()).floatValue();
        String venueAddress = UberHelper.getUberVenueAddress(cart.getEvent().getVenue());
        String venueName = UberHelper.getUberVenueName(cart.getEvent().getVenue());

        DialogFragment dialog = UberHelper.getUberEstimateDialog(uberClient, lat, lng, venueAddress, venueName);
        dialog.setTargetFragment(OrderHistoryFragment.this, ACTIVITY_RESULT_UBER);
        dialog.show(getFragmentManager(), UberDialogFragment.UBER_DIALOG_TAG);
    }


    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_RESULT_UBER:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(uberClient.getClientId(), data);
                    getActivity().startActivity(intent);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(uberClient.getClientId());
                    getActivity().startActivity(intent);
                }
                break;
        }
    }

    private static Cart getNextShow(List<Cart> response) {

        Cart nextShow = null;
        for (Cart target : response) {
            if (nextShow == null) {
                if (isNextShowCandidate(target)) {
                    nextShow = target;
                }
            } else {
                boolean isSooner = target.getEvent().getShowTime() < nextShow.getEvent().getShowTime();
                if (isNextShowCandidate(target) && isSooner) {
                    nextShow = target;
                }
            }
        }
        return nextShow;
    }

    private static boolean isNextShowCandidate(Cart cart) {
        long now = Calendar.getInstance().getTimeInMillis();
        //twenty four hour "next show!" buffer
        long nextShowBuffer = 1000L * 60L * 60L * 24L;
        return (cart.getEvent().getShowTime() >= (now - nextShowBuffer));
    }

    private static enum EmptyState {
        EMPTY,
        LOADING,
        NO_ORDERS,
        SIGNED_OUT,
    }

    private class HistoryAdapter extends ArrayAdapter<Cart> implements StickyListHeadersAdapter {
        private LayoutInflater mInflater;

        private final int ITEM_TYPE_NEXT_SHOW = 0;
        private final int ITEM_TYPE_OTHER_SHOWS = 1;

        public HistoryAdapter(Context context) {
            super(context, R.layout.item_order_history);

            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            ViewHolder holder = null;

            if (view == null) {
                view = mInflater.inflate(R.layout.item_order_history, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
                //view got recycled, cancel any pending uber request that may interfere with recycled view
                Subscription subscription = (Subscription) holder.uberContent.getTag();
                if (subscription != null) {
                    subscription.unsubscribe();
                    holder.uberContent.setTag(null);
                }
            }

            Cart cart = getItem(position);

            Event event = cart.getEvent();
            if (event != null) {
                holder.date.setDate(new Date(event.getShowTime()));
                holder.eventTitle.setText(event.getName());
                holder.address.setText(event.getVenue().getName());
            } else {
                holder.date.setDate(new Date());
                holder.eventTitle.setText(R.string.data_missing_placeholder);
                holder.address.setText(R.string.data_missing_placeholder);
            }
            holder.orderDate.setText(TicketingUtils.formatShortDate(cart.getOrderDate()));

            holder.orderId.setText(cart.getDisplayOrderID());

            holder.uberContent.removeAllViews();

            if (getHeaderId(position) == ITEM_TYPE_NEXT_SHOW) {
                if (UberHelper.isUberAppInstalled(getActivity())) {
                    fetchUberEstimate(holder.uberContent, cart);
                } else {
                    holder.uberContent.addView(getUberSignUpView(parent, cart));
                }
            }

            return view;
        }

        @Override
        public View getHeaderView(int position, View view, ViewGroup viewGroup) {
            if (getHeaderId(position) == ITEM_TYPE_NEXT_SHOW) {
                return mInflater.inflate(R.layout.header_order_history_next_show, viewGroup, false);
            }
            return mInflater.inflate(R.layout.header_order_history_other_shows, viewGroup, false);
        }

        @Override
        public long getHeaderId(int position) {
            Cart cart = getItem(position);
            if (position == 0 && isNextShowCandidate(cart)) return ITEM_TYPE_NEXT_SHOW;
            return ITEM_TYPE_OTHER_SHOWS;
        }

        @Override
        public void addAll(Collection<? extends Cart> collection) {
            List objects = new ArrayList(collection);
            if (getCount() == 0) {
                Cart nextShow = getNextShow(objects);
                if (nextShow != null) {
                    objects.remove(nextShow);
                    objects.add(0, nextShow);
                }
            }
            super.addAll(objects);
        }

        private View getUberSignUpView(@NonNull ViewGroup parent, final Cart cart) {
            View view = mInflater.inflate(R.layout.order_uber_signup, parent, false);
            TextView text = (TextView) view.findViewById(R.id.uber_free_ride_text);
            text.setText(LiveNationApplication.get().getInstalledAppConfig().getUberFreeRideText());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUberSignupClick(cart);
                    trackUberAnalytics(false);
                }
            });
            return view;
        }

        private void fetchUberEstimate(@NonNull final ViewGroup parent, final Cart cart) {
            final View view = mInflater.inflate(R.layout.order_uber_ride, parent, false);
            float lat = Double.valueOf(cart.getEvent().getVenue().getLatitude()).floatValue();
            float lng = Double.valueOf(cart.getEvent().getVenue().getLongitude()).floatValue();

            final Action1<Throwable> onError = new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    //Failed because Location Services unavailable, or network issues
                    //Just leave the Default "Get an Uber!" view here.
                }
            };

            //retrieve any previous uber api operation
            final int hashCode = ((Object) cart).hashCode();
            Observable uberFetch = uberCache.get(hashCode);

            if (uberFetch == null) {
                //no previous uber api operation, perform a new one, and cache the observable emission
                uberFetch = UberHelper.getQuickEstimate(uberClient, lat, lng).cache();
                uberCache.put(hashCode, uberFetch);
            }

            final long timeStarted = SystemClock.uptimeMillis();
            Subscription subscription = uberFetch.subscribe(new Action1<LiveNationEstimate>() {
                @Override
                public void call(LiveNationEstimate liveNationEstimate) {
                    UberHelper.trackUberDisplayedButton(AnalyticsCategory.YOUR_ORDERS);

                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                    String uberTitle = getResources().getString(R.string.uber_order_book_ride_mins);
                    uberTitle = String.format(uberTitle, liveNationEstimate.getTime().getEstimateMins());
                    text1.setText(uberTitle);
                    text2.setText(liveNationEstimate.getPrice().getEstimate());

                    //only animate adding the view if it took over 100ms to fetch the uber data
                    //(if the this operation is called from cache, and we have already animated
                    //adding the uber view, we don't want to animate it in again
                    if (SystemClock.uptimeMillis() - timeStarted > 100) {
                        LayoutTransition transition = new LayoutTransition();
                        transition.setAnimator(LayoutTransition.CHANGE_APPEARING, null);
                        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);
                        transition.setAnimator(LayoutTransition.DISAPPEARING, null);
                        parent.setLayoutTransition(transition);
                    } else {
                        parent.setLayoutTransition(null);
                    }

                    parent.addView(view);
                    parent.setTag(null);
                }
            }, onError);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUberRideClick(cart);
                    trackUberAnalytics(true);
                }
            });

            parent.setTag(subscription);
        }

        private void trackUberAnalytics(boolean isUberInstalled) {
            Props props = new Props();
            String uber_app_value = AnalyticConstants.UBER_APP_UNINSTALLED;
            if (isUberInstalled) {
                uber_app_value = AnalyticConstants.UBER_APP_INSTALLED;
            }
            props.put(AnalyticConstants.UBER_APP, uber_app_value);
            LiveNationAnalytics.track(AnalyticConstants.UBER_YOUR_ORDERS_TAP, AnalyticsCategory.YOUR_ORDERS, props);
        }

        private class ViewHolder {
            final VerticalDateView date;
            final TextView eventTitle;
            final TextView address;
            final TextView orderId;
            final TextView orderDate;
            final ViewGroup uberContent;

            public ViewHolder(View view) {
                this.date = (VerticalDateView) view.findViewById(R.id.item_order_history_date);
                this.eventTitle = (TextView) view.findViewById(R.id.item_order_history_event_title);
                this.address = (TextView) view.findViewById(R.id.item_order_history_address);
                this.orderId = (TextView) view.findViewById(R.id.item_order_history_id);
                this.orderDate = (TextView) view.findViewById(R.id.item_order_history_order_date);
                this.uberContent = (ViewGroup) view.findViewById(R.id.item_order_history_uber);
            }
        }
    }

    private class EmptyStateObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();

            if (historyAdapter.getCount() == 0)
                emptyView.setVisibility(View.VISIBLE);
            else
                emptyView.setVisibility(View.GONE);
        }
    }

    private class InfiniteScrollListener implements AbsListView.OnScrollListener {
        private int lastFirstVisibleItem;
        private int lastVisibleItemCount;
        private int lastTotalItemCount;
        private SwipeRefreshLayout refreshLayout;
        private StickyListHeadersListView listView;

        public InfiniteScrollListener(SwipeRefreshLayout refreshLayout, StickyListHeadersListView listView) {
            this.refreshLayout = refreshLayout;
            this.listView = listView;
        }

        @Override
        public void onScroll(@NonNull AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //start: StickyListHeaders workaround
            //workaround a compatibility issue with swiperefreshlayout and StickyListHeaders
            //stolen from: https://gist.github.com/Frikish/10025057
            View childView = listView.getWrappedList().getChildAt(0);
            int top = (childView == null) ? 0 : childView.getTop();
            if (top >= 0) {
                refreshLayout.setEnabled(true);
            } else {
                refreshLayout.setEnabled(false);
            }
            //end: stickylistheaders workaround

            if (totalItemCount == 0) {
                return;
            }

            if ((totalItemCount - visibleItemCount) <= firstVisibleItem) {
                if (lastFirstVisibleItem == firstVisibleItem &&
                        lastVisibleItemCount == visibleItemCount &&
                        lastTotalItemCount == totalItemCount) {
                    return;
                }

                loadNextPage();

                lastFirstVisibleItem = firstVisibleItem;
                lastVisibleItemCount = visibleItemCount;
                lastTotalItemCount = totalItemCount;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        }
    }
}
