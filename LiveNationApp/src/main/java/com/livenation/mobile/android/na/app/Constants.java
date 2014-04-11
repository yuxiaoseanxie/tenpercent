/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

public class Constants {

    public static final int DEFAULT_RADIUS = 50;

    public class Notifications {
        public static final String EXTRA_TYPE = "type";

        public static final int TYPE_FEATURED_CONTENT = -1;
        public static final int TYPE_EVENT_ON_SALE_NOW = 0;
        public static final int TYPE_EVENT_ANNOUNCEMENT = 1;
        public static final int TYPE_EVENT_LAST_MINUTE = 2;
        public static final int TYPE_EVENT_MOBILE_PRESALE = 3;
        public static final int TYPE_IN_VENUE = 4;


        public static final String EXTRA_ENTITY_ID = "id";

        public static final String EXTRA_MESSAGE_ACTION_URL = "message_action_url";
        public static final String EXTRA_MESSAGE_ACTION_NAME = "message_action_name";

        public static final String EXTRA_IMAGE_URL = "image_url";
        public static final String EXTRA_VIDEO_URL = "video_url";

        public static final String EXTRA_EVENT_INFO_ON_SALE_DATE = "on_sale_date";
        public static final String EXTRA_EVENT_INFO_ARTIST_NAME = "artist_name";
        public static final String EXTRA_EVENT_INFO_VENUE_NAME = "venue_name";
        public static final String EXTRA_EVENT_INFO_LOCAL_START_TIME = "local_start_time";

        public static final String EXTRA_RICH_MESSAGE_ID = "_uamid";
    }

    public class SharedPreferences {
        public static final String MUSIC_SYNC_NAME = "music_sync_name";
        public static final String MUSIC_SYNC_LAST_SYNC_DATE_KEY = "music_sync_last_sync_date_key";
        public static final String DEBUG_MODE_DATA = "debug_mode_data";
        public static final String DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED = "debug_mode_is_debug_mode_activated";
        public static final String ENVIRONMENT = "environment";
    }
}
