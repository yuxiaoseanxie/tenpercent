package com.livenation.mobile.android.na.utils;


import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {

    static public void redirectToSDP(Context context, Event event) {
        Intent intent = new Intent(context, ShowActivity.class);
        Bundle args = ShowActivity.getArguments(event);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    static public void redirectToSDP(Context context, String eId) {
        String eventId = Event.makeTypedId(eId);
        Intent intent = new Intent(context, ShowActivity.class);
        intent.putExtras(ShowActivity.getArguments(eventId));
        context.startActivity(intent);
    }
}