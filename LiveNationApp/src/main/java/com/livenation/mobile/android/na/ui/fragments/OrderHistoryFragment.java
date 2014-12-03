package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.UberHelper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.ui.OrderDetailsActivity;
import com.livenation.mobile.android.na.ui.OrderHistoryActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
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
import com.mobilitus.tm.tickets.models.Venue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    private EmptyStateObserver emptyStateObserver;
    private HistoryAdapter historyAdapter;
    //private Handler offlinePromptHandler;
    private boolean isFetching = false;
    private EmptyState emptyState;

    PollingDialogFragment.PollingListener pollingListener = new PollingDialogFragment.PollingListener() {
        @Override
        public void onCountdownFinished() {
            loadOrRefreshFullHistory();
        }

        @Override
        public void onPollingCancelled() {
        }
    };

    Comparator<Cart> comparator = new Comparator<Cart>() {
        @Override
        public int compare(Cart lhs, Cart rhs) {
            Long diff = rhs.getEvent().getShowTime() - lhs.getEvent().getShowTime();
            Long sign = 0l;
            if (diff != 0) {
                sign = diff / Math.abs(diff);
            }
            return sign.intValue();
        }
    };
    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        this.uberClient = new UberClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_order_history_swipe_layout);

        this.emptyView = (ViewGroup) view.findViewById(android.R.id.empty);

        setupEmptyStateViews();

        StickyListHeadersListView listView = (StickyListHeadersListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(new ListScrollListener(swipeRefreshLayout, listView));
        listView.setAreHeadersSticky(false);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOrRefreshFullHistory();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        swipeRefreshLayout.setEnabled(Ticketing.getTicketService().hasSession());
        swipeRefreshLayout.setRefreshing(isFetching);

        this.emptyStateObserver = new EmptyStateObserver();

        if (historyAdapter == null) {
            this.historyAdapter = new HistoryAdapter(getActivity());
            listView.setAdapter(historyAdapter);
            loadOrRefreshFullHistory();
        } else {
            listView.setAdapter(historyAdapter);
            historyAdapter.notifyDataSetChanged();
        }
        historyAdapter.registerDataSetObserver(emptyStateObserver);

        setEmptyState(emptyState);
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
    }

    public void clearUserData() {
        if (historyAdapter != null) {
            historyAdapter.clear();
        }
        if (isFetching) {
            isFetching = false;
        }

        swipeRefreshLayout.setRefreshing(false);
        setEmptyState(EmptyState.SIGNED_OUT);
    }

    private void setEmptyState(EmptyState state) {
        if (state == null) {
            return;
        }
        emptyView.removeAllViews();
        this.emptyState = state;
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

    //endregion


    //region Loading Orders

    public void setRefreshing(boolean isRefreshing) {
        this.isFetching = isRefreshing;
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @SuppressWarnings("unchecked")
    private void uploadOrderHistory(List<Cart> orderHistory) {
        if (TicketingUtils.isCollectionEmpty(orderHistory) || !UploadOrderHistoryTask.shouldUpload())
            return;

        new UploadOrderHistoryTask().execute(orderHistory);
    }

    private void loadOfflineCache(final BasicApiCallback<List<Cart>> callback) {
        OrdersCacheManager.getInstance().loadOrderHistoryFromCache(getActivity(), new ValueCallback<OrderHistory>() {
            @Override
            public void onValueLoaded(OrderHistory response) {

                ArrayList<Cart> carts = response.getOrders();
                if (carts == null) {
                    carts = new ArrayList<Cart>();
                }
                callback.onResponse(carts);
            }

            @Override
            public void onValueLoadFailed(Throwable error) {
                callback.onErrorResponse(new LiveNationError(error));
                Log.e(getClass().getSimpleName(), "Could not load cache", error);
            }
        });
    }

    public void loadOrRefreshFullHistory() {
        if (isFetching)
            return;

        swipeRefreshLayout.setEnabled(Ticketing.getTicketService().hasSession());

        historyAdapter.clear();
        isFetching = true;
        swipeRefreshLayout.setRefreshing(true);
        setEmptyState(EmptyState.LOADING);

        getOrderHistoryActivity().updateActionBar();

        if (!Ticketing.getTicketService().hasSession()) {
            return;
        }

        BasicApiCallback<List<Cart>> callback = new BasicApiCallback<List<Cart>>() {
            @Override
            public void onResponse(List<Cart> response) {
                response = new ArrayList<>();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                Cart cart = new Cart();
                Event event = new Event();
                event.setName("aa");
                event.setShowTime(calendar.getTimeInMillis());
                Venue venue = new Venue();
                venue.setAddress1("aaa");
                venue.setAddress2("bbb");
                event.setVenue(venue);
                cart.setDisplayOrderID("eee");
                cart.setOrderDate(calendar.getTimeInMillis());
                cart.setEvent(event);
                response.add(cart);

                swipeRefreshLayout.setRefreshing(false);
                isFetching = false;
                if (!Ticketing.getTicketService().hasSession()) {
                    return;
                }

                Collections.sort(response, comparator);

                //Put the "Next show" at the first position, otherwise the first section is "All other shows"
                int position = 0;
                long now = Calendar.getInstance().getTimeInMillis();
                while (position < response.size()
                        && (response.get(position).getEvent() == null
                            || response.get(position).getEvent().getShowTime() - now > 0)
                        && !isNextShow(response, position)) {
                    position++;
                }

                List<Cart> carts = new ArrayList<>();

                if (position != 0 && position < response.size()) {
                    Cart nextShow = response.get(position);
                    response.remove(position);

                    carts.add(nextShow);
                    carts.addAll(response);

                } else {
                    carts = response;
                }

                if (carts.isEmpty()) {
                    setEmptyState(EmptyState.NO_ORDERS);
                } else {
                    setEmptyState(EmptyState.EMPTY);
                }
                historyAdapter.addAll(carts);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                swipeRefreshLayout.setRefreshing(false);
                isFetching = false;
                if (emptyState == EmptyState.LOADING) {
                    setEmptyState(EmptyState.EMPTY);
                }
            }
        };

        if (Ticketing.isConnectedToInternet()) {
            //offlinePromptHandler.sendEmptyMessageDelayed(0, Constants.OFFLINE_MODE_CACHE_DELAY);
            fetchOrderHistory(0, new ArrayList<Cart>(), callback);
        } else {
            loadOfflineCache(callback);
        }
    }

    private boolean isNextShow(List<Cart> response, int position) {
        Cart currentCart = response.get(position);

        long now = Calendar.getInstance().getTimeInMillis();
        if (currentCart.getEvent() == null || currentCart.getEvent().getShowTime() - now < 0) {
            return false;
        }

        Cart nextCart = null;
        if (position + 1 < response.size()) {
            nextCart = response.get(position + 1);
        }

        if (nextCart == null || nextCart.getEvent().getShowTime() - now < 0) {
            return true;
        }

        return false;
    }

    //endregion


    //region Displaying Order Details

    protected void showDetailsForCart(Cart cart) {
        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_CART, cart);
        startActivity(intent);
    }

    //endregion

    //region list item clicks
    @Override
    public void onItemClick(@NonNull AdapterView<?> adapterView, @NonNull View view, int position, long id) {
        Cart cart = historyAdapter.getItem(position);
        showDetailsForCart(cart);
    }

    private void onUberSignupClick(Cart cart) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uberClient.getUberSignupLink());
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
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case ACTIVITY_RESULT_UBER:
                Intent intent = UberHelper.getUberAppLaunchIntent(uberClient, data);
                getActivity().startActivity(intent);
                break;
        }
    }

    private void fetchOrderHistory(final int pageOffset, @NonNull final List<Cart> previousCarts, final BasicApiCallback<List<Cart>> cartsCallback) {
        final Context context = getActivity().getApplicationContext();

        //Analytics
        final TimedEvent getOrderHistoryEvent = Ticketing.getAnalytics().startTimedEvent(TicketLibrary.Method.GET_ORDER_HISTORY);
        getOrderHistoryEvent.getProperties().put("Pagination Offset", pageOffset);

        Ticketing.getTicketService().getOrderHistory(pageOffset, LIMIT_PER_PAGE, new ResponseListener<OrderHistory>() {

            @Override
            public void onSuccess(int requestId, OrderHistory response) {
                //offlinePromptHandler.removeMessages(0);

                uploadOrderHistory(response.getOrders());
                OrdersCacheManager.getInstance().saveOrderHistory(context, response);

                ArrayList<Cart> carts = response.getOrders();
                historyAdapter.clear();

                if (previousCarts != null) {
                    previousCarts.addAll(carts);
                }

                Ticketing.getAnalytics().finishTimedEvent(getOrderHistoryEvent);

                if (response.getOrders() != null && response.getOrders().size() >= LIMIT_PER_PAGE && pageOffset == 0) {
                    fetchOrderHistory(pageOffset + LIMIT_PER_PAGE, previousCarts, cartsCallback);
                } else {
                    cartsCallback.onResponse(previousCarts);
                }
            }
        }, new CommonUIResponseListener(getOrderHistoryActivity(), null, pollingListener) {
            @Override
            public void onError(int requestId, int httpStatusCode, com.mobilitus.tm.tickets.models.Error error) {
                //offlinePromptHandler.removeMessages(0);

                if (!TicketingUtils.errorRequiresDisplay(httpStatusCode, error) && OrdersCacheManager.getInstance().hasOrderHistorySaved(context)) {
                    Log.e(getClass().getName(), "Could not load orders. Error: " + error);
                    loadOfflineCache(cartsCallback);
                } else {
                    super.onError(requestId, httpStatusCode, error);
                    cartsCallback.onErrorResponse(new LiveNationError(error.getCode(), error.getMessage()));
                }
            }
        }.finishTimedEvent(getOrderHistoryEvent));
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
            if (view == null || view.getTag() == null) {
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

            holder.uberContent.removeAllViews();

            if (getHeaderId(position) == ITEM_TYPE_NEXT_SHOW) {
                if (uberClient.isUberAppInstalled()) {
                    holder.uberContent.addView(getUberRideView(parent, cart));
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
            } else {
                return mInflater.inflate(R.layout.header_order_history_other_shows, viewGroup, false);
            }
        }

        @Override
        public long getHeaderId(int position) {
            Cart currentCart = getItem(position);
            long result = ITEM_TYPE_OTHER_SHOWS;

            long now = Calendar.getInstance().getTimeInMillis();
            if (currentCart.getEvent() != null && currentCart.getEvent().getShowTime() - now > 0 && position == 0) {
                result = ITEM_TYPE_NEXT_SHOW;
            }
            return result;
        }

        private View getUberSignUpView(@NonNull ViewGroup parent, final Cart cart) {
            View view = mInflater.inflate(R.layout.order_uber_signup, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUberSignupClick(cart);
                }
            });
            return view;
        }

        private View getUberRideView(@NonNull ViewGroup parent, final Cart cart) {
            final View view = mInflater.inflate(R.layout.order_uber_ride, parent, false);
            float lat = Double.valueOf(cart.getEvent().getVenue().getLatitude()).floatValue();
            float lng = Double.valueOf(cart.getEvent().getVenue().getLongitude()).floatValue();

            UberHelper.getQuickEstimate(uberClient, lat, lng).
                    subscribe(new Action1<LiveNationEstimate>() {
                        @Override
                        public void call(LiveNationEstimate liveNationEstimate) {
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                            String uberTitle = getResources().getString(R.string.uber_popup_book_ride_mins);
                            uberTitle = String.format(uberTitle, liveNationEstimate.getTime().getEstimateMins());
                            text1.setText(uberTitle);
                            text2.setText(liveNationEstimate.getPrice().getEstimate());
                        }
                    });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUberRideClick(cart);
                }
            });
            return view;
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

    private class ListScrollListener implements AbsListView.OnScrollListener {
        private SwipeRefreshLayout refreshLayout;
        private StickyListHeadersListView listView;

        public ListScrollListener(SwipeRefreshLayout refreshLayout, StickyListHeadersListView listView) {
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

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    }

}
