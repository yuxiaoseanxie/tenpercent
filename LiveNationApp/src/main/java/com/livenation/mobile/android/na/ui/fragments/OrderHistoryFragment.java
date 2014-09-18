package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.livenation.mobile.android.na.R;
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
import java.util.Date;
import java.util.List;

public class OrderHistoryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final int LIMIT_PER_PAGE = 10;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_order_history_swipe_layout);
        this.emptyView = (ViewGroup) view.findViewById(android.R.id.empty);
        setupEmptyStateViews();

        this.historyAdapter = new HistoryAdapter(getActivity());
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(historyAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(new InfiniteScrollListener());

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

    @Override
    public void onItemClick(@NonNull AdapterView<?> adapterView, @NonNull View view, int position, long id) {
        Cart cart = historyAdapter.getItem(position);
        showDetailsForCart(cart);
    }

    //endregion



    private static enum EmptyState {
        EMPTY,
        LOADING,
        NO_ORDERS,
        SIGNED_OUT,
    }

    private class HistoryAdapter extends ArrayAdapter<Cart> {
        private LayoutInflater mInflater;

        public HistoryAdapter(Context context) {
            super(context, R.layout.item_order_history);

            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_order_history, parent, false);
                view.setTag(new ViewHolder(view));
            }

            ViewHolder holder = (ViewHolder) view.getTag();
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

            return view;
        }


        private class ViewHolder {
            final VerticalDateView date;
            final TextView eventTitle;
            final TextView address;
            final TextView orderId;
            final TextView orderDate;

            public ViewHolder(View view) {
                this.date = (VerticalDateView) view.findViewById(R.id.item_order_history_date);
                this.eventTitle = (TextView) view.findViewById(R.id.item_order_history_event_title);
                this.address = (TextView) view.findViewById(R.id.item_order_history_address);
                this.orderId = (TextView) view.findViewById(R.id.item_order_history_id);
                this.orderDate = (TextView) view.findViewById(R.id.item_order_history_order_date);

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

        @Override
        public void onScroll(@NonNull AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
