package com.livenation.mobile.android.na.notifications;

import com.livenation.mobile.android.na.presenters.support.PresenterView;

/**
 * Created by km on 3/5/14.
 */
public interface InboxStatusView extends PresenterView {
    public void setHasUnreadNotifications(boolean hasUnreadNotifications);
}
