package com.livenation.mobile.android.na.notifications;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by km on 3/5/14.
 */
public class InboxStatusPresenter extends BasePresenter<InboxStatusView, InboxStatusPresenter.InboxStatusState> {
    @Override
    public void initialize(Context context, Bundle args, InboxStatusView view) {
        InboxStatusState state = new InboxStatusState(this, args, view);
        state.run();
    }

    public class InboxStatusState extends BaseState<InboxStatusView> implements RichPushInbox.Listener {
        private RichPushInbox inbox;

        public InboxStatusState(StateListener listener, Bundle args, InboxStatusView view) {
            super(listener, args, view);

            this.inbox = UAirship.shared().getRichPushManager().getRichPushInbox();
            inbox.addListener(this);
        }

        private boolean hasUnreadNotifications() {
            return (inbox.getUnreadCount() > 0);
        }

        @Override
        public void cancel() {
            super.cancel();

            inbox.removeListener(this);
        }

        @Override
        public void run() {
            getView().setHasUnreadNotifications(hasUnreadNotifications());
        }

        @Override
        public void onUpdateInbox() {
            getView().setHasUnreadNotifications(hasUnreadNotifications());
        }
    }
}
