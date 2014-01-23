/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.BasePresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public interface SingleEventView extends BasePresenterView {
	void setEvent(Event event);
}
