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
    public static final float METERS_IN_A_MILE = 1609.34f;

    //arbitary elay between user keypresses and sending user text to the API
    public static final int TEXT_CHANGED_POST_DELAY = 667;

    public static enum Environment {
        Production("https://api.livenation.com", "0361b9ba-9381-11e3-bc4c-bad30bc9cabe"),
        ProductionDirect("https://prod-faceoff.herokuapp.com", "0361b9ba-9381-11e3-bc4c-bad30bc9cabe"),
        Staging("http://stg.api.livenation.com", "9e34bce8-5ea3-11e3-a9e5-5a59eb79d850"),
        StagingDirect("https://stg-faceoff.herokuapp.com", "9e34bce8-5ea3-11e3-a9e5-5a59eb79d850"),
        Integration("https://int-faceoff.herokuapp.com", "486e2ff6-98f5-11e3-b37c-ca4ec9c3f32c");

        private final String host;
        private final String clientId;

        Environment(String host, String clientId) {
            this.host = host;
            this.clientId = clientId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getHost() {
            return host;
        }
    }

    public class Notifications {
        public static final String EXTRA_TYPE = "type";

        public static final String TYPE_PUSH_CAPTCHA = "silent_push";
        public static final int TYPE_FEATURED_CONTENT = -1;
        public static final int TYPE_EVENT_ON_SALE_NOW = 0;
        public static final int TYPE_EVENT_ANNOUNCEMENT = 1;
        public static final int TYPE_EVENT_LAST_MINUTE = 2;
        public static final int TYPE_EVENT_MOBILE_PRESALE = 3;
        public static final int TYPE_IN_VENUE = 4;


        public static final String EXTRA_ENTITY_ID = "id";
        public static final String EXTRA_PUSH_CAPTCHA_PAYLOAD = "payload";

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
        public static final String DEVICE_UUID = "device_uuid";
        public static final String NOTIFICATIONS_SAVED_APID = "notifications_saved_apid";
        public static final String API_NAME = "api";
        public static final String API_ACCESS_TOKEN = "access_token";
        public static final String IAS_NAME = "ias_name";
        public static final String IAS_USER_ID = "ias_user_id";
        //1.X key
        public static final String PREF_NAME = "TM_USER_PREF";
        public static final String INSTALLATION_ID = "INSTALLATION_ID";

    }

    public class BroadCastReceiver {
        public static final String LOGOUT = "logout";
    }
}
