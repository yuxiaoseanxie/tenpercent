package com.livenation.mobile.android.na.utils;


import com.livenation.mobile.android.na.ui.ShowActivityV2;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {

    static public void redirectToSDP(Context context, Event event) {
        Intent intent = new Intent(context, ShowActivityV2.class);
        Bundle args = ShowActivityV2.getArguments(event);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    static public void redirectToSDP(Context context, String eId) {
        String eventId = Event.makeTypedId(eId);
        Intent intent = new Intent(context, ShowActivityV2.class);
        intent.putExtras(ShowActivityV2.getArguments(eventId));
        context.startActivity(intent);
    }
}