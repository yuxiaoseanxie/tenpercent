/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.presenters.views;

import com.livenation.mobile.android.na2.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public interface SingleEventView extends PresenterView {
	void setEvent(Event event);
}
