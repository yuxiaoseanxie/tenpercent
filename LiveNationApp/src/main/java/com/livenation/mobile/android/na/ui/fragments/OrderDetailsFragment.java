package com.livenation.mobile.android.na.ui.fragments;

import com.android.volley.VolleyError;
import com.experience.android.activities.ExpActivityConfig;
import com.experience.android.activities.ExperienceWebViewActivity;
import com.livenation.mobile.android.na.ExperienceApp.ExperienceAppClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.OrderDetailsActivity;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.activities.BarcodeActivity;
import com.livenation.mobile.android.ticketing.activities.PostResaleActivity;
import com.livenation.mobile.android.ticketing.activities.TransferTicketsActivity;
import com.livenation.mobile.android.ticketing.analytics.AnalyticConstants;
import com.livenation.mobile.android.ticketing.analytics.Properties;
import com.livenation.mobile.android.ticketing.analytics.TimedEvent;
import com.livenation.mobile.android.ticketing.dialogs.LoadingDialogFragment;
import com.livenation.mobile.android.ticketing.dialogs.PollingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.CommonUIResponseListener;
import com.livenation.mobile.android.ticketing.utils.Constants;
import com.livenation.mobile.android.ticketing.utils.OnThrottledClickListener;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.livenation.mobile.android.ticketing.utils.orders.OrdersCacheManager;
import com.livenation.mobile.android.ticketing.utils.orders.ValueCallback;
import com.livenation.mobile.android.ticketing.widgets.TicketView;
import com.mobilitus.tm.tickets.TicketLibrary;
import com.mobilitus.tm.tickets.interfaces.ResponseListener;
import com.mobilitus.tm.tickets.models.Cart;
import com.mobilitus.tm.tickets.models.PostResaleCart;
import com.mobilitus.tm.tickets.models.ResaleSeat;
import com.mobilitus.tm.tickets.models.Seat;

import java.util.ArrayList;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OrderDetailsFragment extends Fragment {
    // When we load an order details cart from mTopia immediately after a purchase,
    // it will not have a fully formed event entity. But we already have a cart on
    // hand at the end of the purchase that does. That cart with the event info is
    // passed in as an intent extra. We load the full cart from the server. We keep
    // both around so we can display all of the data we need to.
    private Cart eventInfoCart;
    private Cart ticketsCart;
    private boolean hasLoadedTicketsCart = false;
    private SparseBooleanArray selectedTickets;
    private ArrayList<TicketView> ticketViews;
    private SelectionMode selectionMode = SelectionMode.NONE;
    private boolean isRefreshing = false;

    private SwipeRefreshLayout refreshLayout;
    private TextView instructions;
    private LinearLayout ticketContainer;
    private TextView eventDate;
    private TextView eventVenue;
    private TextView eventTitle;

    private Handler offlinePromptHandler;
    private Button resaleButton;
    private Button transferButton;

    private ActionMode transferResaleActionMode;
    private View mainView;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.eventInfoCart = getOrderDetailsActivity().getEventInfoCart();
        this.ticketsCart = eventInfoCart;
        this.selectedTickets = new SparseBooleanArray();
        this.ticketViews = new ArrayList<TicketView>();

        this.offlinePromptHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message message) {
                Activity activity = getActivity();
                if (activity != null && OrdersCacheManager.getInstance().hasOrderDetailsSavedForId(activity, ticketsCart.getOrderID())) {
                    TicketingUtils.makeToast(activity.getApplicationContext(), R.string.toast_displaying_offline_order_details, Toast.LENGTH_SHORT).show();
                    loadOfflineCache(true);
                }

                return true;
            }
        });

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_order_details, container, false);

        this.instructions = (TextView) mainView.findViewById(R.id.order_detail_textview_instructions);

        this.ticketContainer = (LinearLayout) mainView.findViewById(R.id.order_detail_ticket_container);
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(300);
        ticketContainer.setLayoutTransition(transition);

        this.eventTitle = (TextView) mainView.findViewById(R.id.textview_title);
        this.eventVenue = (TextView) mainView.findViewById(R.id.textview_address);
        this.eventDate = (TextView) mainView.findViewById(R.id.textview_date);

        this.refreshLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.order_detail_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTicketsCart();
            }
        });
        refreshLayout.setColorSchemeResources(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);
        refreshLayout.setRefreshing(isRefreshing);

        this.resaleButton = (Button) mainView.findViewById(R.id.order_detail_button_resale);
        resaleButton.setOnClickListener(new OnResaleClickedListener());

        this.transferButton = (Button) mainView.findViewById(R.id.order_detail_button_transfer);
        transferButton.setOnClickListener(new OnTransferClickedListener());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onTicketTransferred, new IntentFilter(TransferTicketsActivity.ACTION_TICKET_TRANSFERRED));

        return mainView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateEventInfo();
        if (!hasLoadedTicketsCart && !isRefreshing)
            loadTicketsCart();
        else
            displayTickets();
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onTicketTransferred);

        super.onDestroyView();
    }

    //endregion


    public OrderDetailsActivity getOrderDetailsActivity() {
        return (OrderDetailsActivity) getActivity();
    }

    public Cart getTicketsCart() {
        return ticketsCart;
    }

    //region Refreshing

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
        refreshLayout.setRefreshing(isRefreshing);
    }

    private void loadOfflineCache(final boolean isWaitingForOnlineOrders) {
        OrdersCacheManager.getInstance().loadOrderDetails(getActivity(), ticketsCart.getOrderID(), new ValueCallback<Cart>() {
            @Override
            public void onValueLoaded(Cart fullCart) {
                OrderDetailsFragment.this.hasLoadedTicketsCart = true;
                OrderDetailsFragment.this.ticketsCart = fullCart;

                displayTickets();

                if (!isWaitingForOnlineOrders)
                    setRefreshing(false);
            }

            @Override
            public void onValueLoadFailed(Throwable error) {
                if (!isWaitingForOnlineOrders) {
                    setRefreshing(false);

                    instructions.setText(R.string.order_details_incomplete_offline);
                    instructions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadTicketsCart() {
        if (ticketsCart == null || ticketsCart.getEvent() == null)
            return;

        setRefreshing(true);
        if (Ticketing.isConnectedToInternet()) {
            offlinePromptHandler.sendEmptyMessageDelayed(0, Constants.OFFLINE_MODE_CACHE_DELAY);

            PollingDialogFragment.PollingListener pollingListener = new PollingDialogFragment.PollingListener() {
                @Override
                public void onCountdownFinished() {
                    loadTicketsCart();
                }

                @Override
                public void onPollingCancelled() {
                    setRefreshing(false);
                }
            };
            final TimedEvent getOrderEvent = Ticketing.getAnalytics().startTimedEvent(TicketLibrary.Method.GET_ORDER);
            Ticketing.getTicketService().getOrder(ticketsCart.getEvent().getEventID(), ticketsCart.getOrderID(), new ResponseListener<Cart>() {
                @Override
                public void onSuccess(int i, Cart fullCart) {
                    offlinePromptHandler.removeMessages(0);

                    OrdersCacheManager.getInstance().saveOrderDetails(getActivity(), fullCart);

                    OrderDetailsFragment.this.hasLoadedTicketsCart = true;
                    OrderDetailsFragment.this.ticketsCart = fullCart;
                    if (eventInfoCart.getEvent() == null) {
                        OrderDetailsFragment.this.eventInfoCart = fullCart;
                        updateEventInfo();
                    }

                    displayTickets();

                    fetchUpgradeStatus();

                    Ticketing.getAnalytics().finishTimedEvent(getOrderEvent);
                }
            }, new CommonUIResponseListener(getOrderDetailsActivity(), null, pollingListener) {
                @Override
                public void onError(int requestId, int httpStatusCode, com.mobilitus.tm.tickets.models.Error error) {
                    offlinePromptHandler.removeMessages(0);

                    if (!TicketingUtils.errorRequiresDisplay(httpStatusCode, error) && getTicketsCart().getOrderID() != null && OrdersCacheManager.getInstance().hasOrderDetailsSavedForId(getActivity(), getTicketsCart().getOrderID())) {
                        Log.e(getClass().getName(), "Could not load order details. Error: " + error);
                        loadOfflineCache(false);
                    } else {
                        setRefreshing(false);
                        super.onError(requestId, httpStatusCode, error);
                    }
                }
            }.finishTimedEvent(getOrderEvent));
        } else {
            loadOfflineCache(false);
        }
    }

    private final BroadcastReceiver onTicketTransferred = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadTicketsCart();
        }
    };

    //endregion


    //region Displaying Information

    private void updateEventInfo() {
        if (eventInfoCart != null && eventInfoCart.getEvent() != null) {
            eventTitle.setText(eventInfoCart.getEvent().getName());
            if (eventInfoCart.getEvent().getVenue() != null)
                eventVenue.setText(eventInfoCart.getEvent().getVenue().getName());
            else
                eventVenue.setText(R.string.data_missing_placeholder);
            eventDate.setText(TicketingUtils.formatDate(eventInfoCart.getEvent().getShowTime()));
        } else {
            eventTitle.setText(R.string.data_missing_placeholder);
            eventVenue.setText(R.string.data_missing_placeholder);
            eventDate.setText(R.string.data_missing_placeholder);
        }
    }

    public void displayTickets() {
        Activity activity = getActivity();
        if (activity == null)
            return;

        ticketContainer.removeAllViews();
        ticketViews.clear();

        if (getTicketsCart().isVoided()) {
            instructions.setText(R.string.order_details_voided);
            instructions.setVisibility(View.VISIBLE);

            return;
        }

        ArrayList<Seat> seats = TicketingUtils.calculateSeatsForCart(ticketsCart);
        if (TicketingUtils.isCollectionEmpty(seats))
            return;

        instructions.setVisibility(View.GONE);

        for (int i = 0, count = seats.size(); i < count; i++) {
            Seat seat = seats.get(i);

            TicketView ticketView = new TicketView(activity);
            ticketView.setOnClickListener(new OnTicketClickListener(i));
            ticketView.setOnCheckedChangeListener(new OnTicketCheckedListener(i));
            ticketView.displayTicket(seat, i + 1, count);
            updateTicketView(ticketView);

            ticketContainer.addView(ticketView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ticketViews.add(ticketView);
        }
    }

    //endregion


    //region Resale/Transfer

    public void showTransferResaleActionMode() {
        if (transferResaleActionMode != null)
            return;

        getActivity().startActionMode(new ActionMode.Callback() {
            private MenuItem nextItem;

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                transferResaleActionMode = actionMode;
                switch (selectionMode) {
                    case TRANSFER:
                        actionMode.setTitle(R.string.transfer);
                        break;

                    case RESALE:
                        actionMode.setTitle(R.string.resale);
                        break;

                    default:
                        throw new IllegalStateException();
                }
                resaleButton.setEnabled(false);
                transferButton.setEnabled(false);

                getActivity().getMenuInflater().inflate(R.menu.menu_transfer_resale, menu);
                nextItem = menu.findItem(R.id.menu_transfer_resale_next);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                nextItem.setEnabled(selectedTickets.size() > 0);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_transfer_resale_next) {
                    switch (selectionMode) {
                        case TRANSFER:
                            transferSelection();
                            return true;

                        case RESALE:
                            resellSelection();
                            return true;

                        default:
                            break;
                    }
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                transferResaleActionMode = null;

                resaleButton.setEnabled(true);
                transferButton.setEnabled(true);

                updateSelectionMode(SelectionMode.NONE);
            }
        });
    }

    public void dismissTransferResaleActionMode() {
        if (transferResaleActionMode != null)
            transferResaleActionMode.finish();
    }

    public void updateTicketView(@NonNull TicketView ticketView) {
        switch (selectionMode) {
            case NONE:
                ticketView.setCheckVisibility(View.INVISIBLE);
                dismissTransferResaleActionMode();
                break;

            case RESALE:
                if (TicketingUtils.isSeatResaleable(ticketView.getSeat()))
                    ticketView.setCheckVisibility(View.VISIBLE);
                else
                    ticketView.setCheckVisibility(View.INVISIBLE);
                showTransferResaleActionMode();
                break;

            case TRANSFER:
                if (TicketingUtils.isSeatTransferable(ticketView.getSeat()))
                    ticketView.setCheckVisibility(View.VISIBLE);
                else
                    ticketView.setCheckVisibility(View.INVISIBLE);
                showTransferResaleActionMode();
                break;
        }

        ticketView.setChecked(false);
    }

    public void updateSelectionMode(SelectionMode mode) {
        if (mode == selectionMode)
            return;

        this.selectionMode = mode;

        for (TicketView ticketView : ticketViews)
            updateTicketView(ticketView);
    }

    public void transferSelection() {
        ArrayList<String> seatIds = new ArrayList<String>();
        for (int i = 0, size = ticketViews.size(); i < size; i++) {
            if (selectedTickets.get(i)) {
                TicketView ticketView = ticketViews.get(i);
                seatIds.add(ticketView.getSeat().getId());
            }
        }

        Intent intent = new Intent(getActivity(), TransferTicketsActivity.class);
        intent.putExtras(TransferTicketsActivity.getArguments(ticketsCart, seatIds));
        startActivity(intent);

        updateSelectionMode(SelectionMode.NONE);
    }

    public void resellSelection() {
        ResaleSeat resaleSeat = new ResaleSeat();
        ArrayList<String> resaleIds = new ArrayList<String>();
        for (int i = 0, size = ticketViews.size(); i < size; i++) {
            if (selectedTickets.get(i)) {
                TicketView ticketView = ticketViews.get(i);
                resaleIds.add(ticketView.getSeat().getResaleStatus().getSeatPostingID());
            }
        }
        resaleSeat.setIds(resaleIds);

        final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), LoadingDialogFragment.TAG);
        TicketLibrary.getInstance().createResalePosting(ticketsCart.getEvent().getEventID(), ticketsCart.getOrderID(), resaleSeat, new ResponseListener<PostResaleCart>() {

            @Override
            public void onSuccess(int requestId, PostResaleCart response) {
                Intent intent = new Intent(getActivity(), PostResaleActivity.class);
                intent.putExtra("postResaleCart", response);
                startActivity(intent);

                loadingDialogFragment.dismissAllowingStateLoss();
            }
        }, new CommonUIResponseListener(getOrderDetailsActivity(), loadingDialogFragment));

        updateSelectionMode(SelectionMode.NONE);
    }

    //endregion


    //region Click Listeners

    private class OnTicketClickListener extends OnThrottledClickListener {
        private final int position;

        public OnTicketClickListener(int position) {
            this.position = position;
        }

        private void track() {
            Properties properties = getOrderDetailsActivity().getScreenProperties();
            properties.put(AnalyticConstants.PROP_POSITION, position + 1);
            properties.put(AnalyticConstants.PROP_TOTAL_ITEMS, ticketViews.size());
            Ticketing.getAnalytics().track(AnalyticConstants.SHOW_BARCODES_TAP, AnalyticConstants.CATEGORY_ORDER_DETAILS, properties);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);

            track();

            if (selectionMode != SelectionMode.NONE) {
                TicketView ticketView = (TicketView) view;
                ticketView.setChecked(!ticketView.isChecked());
            } else {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                intent.putExtra(BarcodeActivity.EXTRA_EVENT_INFO_CART, getOrderDetailsActivity().getEventInfoCart());
                intent.putExtra(BarcodeActivity.EXTRA_TICKETS_CART, getTicketsCart());
                intent.putExtra(BarcodeActivity.EXTRA_TICKET_INDEX, position);
                startActivity(intent);
            }
        }
    }

    private class OnTicketCheckedListener implements CompoundButton.OnCheckedChangeListener {
        private final int position;

        private OnTicketCheckedListener(int position) {
            this.position = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked)
                selectedTickets.put(position, true);
            else
                selectedTickets.delete(position);

            if (transferResaleActionMode != null)
                transferResaleActionMode.invalidate();
        }
    }

    private class OnTransferClickedListener extends OnThrottledClickListener {
        private void track() {
            Properties properties = getOrderDetailsActivity().getScreenProperties();
            properties.put(AnalyticConstants.PROP_NUM_TICKETS, selectedTickets.size());
            properties.put(AnalyticConstants.PROP_TOTAL_ITEMS, ticketViews.size());
            Ticketing.getAnalytics().track(AnalyticConstants.TRANSFER_TAP, AnalyticConstants.CATEGORY_ORDER_DETAILS, properties);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);

            track();

            updateSelectionMode(SelectionMode.TRANSFER);
        }
    }

    private class OnResaleClickedListener extends OnThrottledClickListener {
        private void track() {
            Properties properties = getOrderDetailsActivity().getScreenProperties();
            properties.put(AnalyticConstants.PROP_NUM_TICKETS, selectedTickets.size());
            properties.put(AnalyticConstants.PROP_TOTAL_ITEMS, ticketViews.size());
            Ticketing.getAnalytics().track(AnalyticConstants.RESELL_TAP, AnalyticConstants.CATEGORY_ORDER_DETAILS, properties);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);

            track();

            DialogFragment resaleDialog = ResaleDialog.newInstance(getTicketsCart());
            resaleDialog.show(getFragmentManager(), "ResaleDialogFragment");
        }
    }

    //endregion

    public static class ResaleDialog extends DialogFragment {
        private static final String EXTRA_URI = "uri";

        public static ResaleDialog newInstance(Cart cart) {
            Bundle args = new Bundle();
            args.putString(EXTRA_URI, TicketingUtils.getResaleTransferUri(cart).toString());
            ResaleDialog fragment = new ResaleDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Uri uri = Uri.parse(getArguments().getString(EXTRA_URI));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_resale_title);
            builder.setMessage(R.string.dialog_resale_message);
            builder.setPositiveButton(R.string.dialog_resale_confirm_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    private static enum SelectionMode {
        NONE,
        RESALE,
        TRANSFER,
    }

    private void fetchUpgradeStatus() {
        final Context context = getActivity();
        if (ticketsCart != null && context != null) {
            ExperienceAppClient experienceAppClient = new ExperienceAppClient(context);

            //reach out to the experience api
            experienceAppClient.makeRequest(ticketsCart.getEvent().getEventID(), new ExperienceAppClient.ExperienceAppListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(OrderDetailsFragment.class.getSimpleName(), "Experience App Endpoint failed", error);
                    setRefreshing(false);
                }

                @Override
                public void onResponse(Boolean response) {
                    if (response) {
                        displayUpgradeButton();
                    }
                    setRefreshing(false);
                }
            });
        }
    }

    private void displayUpgradeButton() {
        View divider = mainView.findViewById(R.id.order_detail_upgrade_divider);
        Button upgadeButton = (Button) mainView.findViewById(com.livenation.mobile.android.na.R.id.order_detail_upgrade_button);
        upgadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticketsCart != null && getActivity() != null) {
                    final Intent intent = new Intent(getActivity(), ExperienceWebViewActivity.class);

                    intent.putExtra(ExpActivityConfig.SSO_ORDER_ID, ticketsCart.getOrderID());
                    intent.putExtra(ExpActivityConfig.SSO_EVENT_ID, ticketsCart.getEvent().getEventID());
                    intent.putExtra(ExpActivityConfig.SSO_FAN_ID, Ticketing.getTicketService().getUser().getEmail());

                    intent.putExtra(ExpActivityConfig.SSO_TICKET_SYSTEM, ExpActivityConfig.TicketSystem.TICKETMASTER_TAP);

                    startActivity(intent);
                }
            }
        });
        divider.setVisibility(View.VISIBLE);
        upgadeButton.setVisibility(View.VISIBLE);

    }
}
