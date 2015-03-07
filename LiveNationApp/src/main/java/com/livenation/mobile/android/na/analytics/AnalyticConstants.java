package com.livenation.mobile.android.na.analytics;

/**
 * Created by elodieferrais on 4/7/14.
 */
public class AnalyticConstants {
    public static final String PLATFORM_EVENT_SUFFIX = " (LN_ANDROID)";

    /**
     * Event
     */
    //Common
    public static final String GOOGLE_SIGN_IN_TAP = "Google Sign In Tap";
    public static final String FACEBOOK_CONNECT_TAP = "Facebook Connect Tap";
    public static final String NOTIFICATION_ICON_TAP = "Notification Icon Tap";
    public static final String GOOGLE_CONNECT = "Google Sign In";
    public static final String FACEBOOK_CONNECT = "Facebook Connect";
    public static final String FAVORITES_UPSELL_TAP = "Favorites Upsell Tap";
    public static final String EVENT_CELL_TAP = "Event Cell Tap";
    public static final String VENUE_CELL_TAP = "Venue Cell Tap";
    public static final String ARTIST_CELL_TAP = "Artist Cell Tap";
    public static final String FAVORITE_VENUE_STAR_TAP = "Favorite Venue Star Tap";
    public static final String FAVORITE_ARTIST_STAR_TAP = "Favorite Artist Star Tap";
    public static final String USER_ID = "user Id";

    //On Boarding
    public static final String SKIP_TAP = "Skip Tap";
    public static final String ON_BOARDING_FIRST_LAUNCH = "Onboarding First Launch";
    //Action bar
    public static final String SEARCH_ICON_TAP = "Search Icon Tap";
    public static final String SHARE_ICON_TAP = "Share Icon Tap";
    public static final String MENU_ICON_TAP = "Menu Icon Tap";
    public static final String LEGAL_CREDIT_TAP = "Legal Credits Tap";
    public static final String CONTACT_TAP = "Contact Tap";
    public static final String HELP_TAP = "Help Tap";
    public static final String LOGOUT_TAP = "Logout Tap";

    //Drawer
    public static final String OPEN_DRAWER = "Open Drawer";
    public static final String YOUR_ORDERS_TAP = "Your Orders Tap";
    public static final String YOUR_FAVORITES_TAP = "Your Favorites Tap";
    public static final String YOUR_LOCATION_TAP = "Your Location Tap";

    //Home Screen
    public static final String ALL_SHOWS_VIEW = "All Shows View";
    public static final String NEARBY_VIEW = "Nearby View";
    public static final String RECOMMENDED_VIEW = "Recommended View";

    //SDP
    public static final String FIND_TICKETS_TAP = "Find Tickets Tap";
    public static final String CALENDAR_ROW_TAP = "Calendar Row Tap";
    public static final String ADD_TO_CALENDAR_TAP = "Add to Calendar Tap";
    public static final String OPTIONS_BUTTON_TAP = "Options Button Tap";
    public static final String FIND_TICKETS_OPTIONS_SELECTION = "Find Tickets Options Selection";

    //ADP
    public static final String SEE_MORE_SHOWS_TAP = "See More Shows Tap";
    public static final String SEE_MORE_VIDEOS_TAP = "See More Videos Tap";
    public static final String VIDEO_TAP = "Video Tap";

    //VDP
    public static final String VENUE_ADDRESS_TAP = "Venue Address Tap";
    public static final String VENUE_PHONE_TAP = "Venue Phone Tap";
    public static final String MORE_VENUE_INFO_TAP = "More Venue Info Tap";
    public static final String PARKING_TRANSIT_TAB_TAP = "Parking Transit Tab Tap";
    public static final String BOX_OFFICE_TAB_TAP = "Box Office Tab Tap";

    //Favorites
    public static final String ARTISTS_TAB_TAP = "Artists Tab Tap";
    public static final String VENUES_TAB_TAP = "Venues Tab Tap";

    //Legal
    public static final String TERMS_OF_USE_TAP = "Terms of Use Tap";
    public static final String PRIVACY_POLICY_TAP = "Privacy Policy Tap";

    //Notification
    public static final String NOTIFICATION_CELL_TAP = "Notification Tap";
    public static final String DEEP_LINK_BUTTON_TAP = "Deep Link Button Tap";

    //Push Notification
    public static final String PUSH_NOTIFICATION_RECEIVE = "Push Notification Receive";
    public static final String PUSH_NOTIFICATION_TAP = "Push Notification Tap";

    //Search
    public static final String SEARCH_RESULT_TAP = "Search Result Tap";

    //Location
    public static final String CURRENT_LOCATION_TAP = "Current Location Tap";
    public static final String SUBMIT_LOCATION_QUERY = "Submit Location Query";
    public static final String PREVIOUS_LOCATION_TAP = "Previous Location Tap";

    //HouseKeeping 1.x Updated (LN_Android)
    public static final String UPDATED = "1.x Updated";
    public static final String MIGRATION_COMPLETED = "1.x Migration Completed";
    public static final String GRANTED_ACCESS_TO_MUSIC = "1.x Granted Access to Music Library";
    public static final String APPLICATION_OPEN = "User Opens App";
    public static final String DEEP_LINK_REDIRECTION = "Deep Link Redirection";

    //Music Library Analysis
    public static final String AFFINITY_MUSIC_LIBRARY_SCAN_COMPLETED = "Affinity Music Library Scan Completed";

    //Uber
    public static final String UBER_WEB_LAUNCH = "Uber Web Launch";
    public static final String UBER_DISPLAYED_BUTTON = "Displayed Uber Button";

    public static final String UBER_VDP_MENU_TAP = "VDP Address Menu Tap";
    public static final String UBER_VDP_UBER_TAP = "VDP Uber Tap";
    public static final String UBER_YOUR_ORDERS_TAP = "Your Orders Uber Tap";
    public static final String UBER_MODAL_LOAD = "Uber Modal Load";
    public static final String UBER_MODAL_PRODUCT_OPTION_TAP = "Uber Modal Product Option Tap";
    public static final String UBER_MODAL_DISMISS = "Uber Modal Dismiss";

    /**
     * Screen name
     */
    public static final String SCREEN_FAVORITES = "Favorites";
    public static final String SCREEN_LEGAL_CREDITS = "Legal";
    public static final String SCREEN_VDP_ALL_SHOWS = "All Shows";
    public static final String SCREEN_LOCATION = "Location";
    public static final String SCREEN_CONTACTS_US = "Contact Us";
    public static final String SCREEN_HELP = "Help";
    public static final String SCREEN_SEARCH = "Search";
    public static final String SCREEN_ONBOARDING = "Onboarding screen";
    public static final String SCREEN_ADP = "ADP";
    public static final String SCREEN_SDP = "SDP";
    public static final String SCREEN_VDP = "VDP";
    public static final String SCREEN_NOTIFICATIONS = "Notifications";
    public static final String SCREEN_HOME = "Home Screen Load";
    public static final String SCREEN_ADP_TOUR = "ADP Tour";


    //Omniture Analytics
    public static final String OMNITURE_SCREEN_HOME = "LN_Mob: NA App Android: Home";
    public static final String OMNITURE_SCREEN_FAVORITES = "LN_Mob: NA App Android: Favorites";
    public static final String OMNITURE_SCREEN_LEGAL_CREDITS = "LN_Mob: NA App Android: Legal";
    public static final String OMNITURE_SCREEN_VDP = "LN_Mob: NA App Android: VDP";
    public static final String OMNITURE_SCREEN_VDP_ALL_SHOWS = "LN_Mob: NA App Android: VDP: All Shows";
    public static final String OMNITURE_SCREEN_ADP = "LN_Mob: NA App Android: Artist";
    public static final String OMNITURE_SCREEN_ADP_TOUR = "LN_Mob: NA App Android: Artist: Tours";
    public static final String OMNITURE_SCREEN_LOCATION = "LN_Mob: NA App Android: Location";
    public static final String OMNITURE_SCREEN_NOTIFICATIONS = "LN_Mob: NA App Android: Notifications";
    public static final String OMNITURE_SCREEN_CONTACTS_US = "LN_Mob: NA App Android: Contact Us";
    public static final String OMNITURE_SCREEN_HELP = "LN_Mob: NA App Android: Help";
    public static final String OMNITURE_SCREEN_SEARCH = "LN_Mob: NA App Android: Search";
    public static final String OMNITURE_SCREEN_SDP = "LN_Mob: NA App Android: SDP";
    public static final String OMNITURE_DEEP_LINK = "LN_Mob: NA App Android: Deep Link Redirection";


    public static final String OMNITURE_SCREEN_ORDERS = "LN_Mob: NA App Android: Orders";
    public static final String OMNITURE_SCREEN_SHARE = "LN_Mob: NA App Android: Share";
    public static final String OMNITURE_SCREEN_VDP_VENUE_INFO = "LN_Mob: NA App Android: VDP: Venue Info";

    //Apsalar
    public static final String APSALAR_LN_LOGIN = "Livenation Login";
    public static final String APSALAR_FIND_TICKET_TAP = "Find Ticket Tap";
    public static final String APSALAR_PURCHASE_CONFIRMATION = "Purchase Confirmation";


    /**
     * Attributes
     */
    //Common
    public static final String CELL_POSITION = "Cell Position";
    public static final String USER_LOGGED_IN = "User Logged In";
    public static final String DEEP_LINK_URL = "Deep Link Url";
    public static final String DEEP_LINK_OPEN_FROM = "Open from";
    public static final String DEEP_LINK_HOST = "Host";

    //Category
    public static final String CATEGORY = "Category";
    public static final String CATEGORY_UNKNOWN = "Category Unknown";
    public static final String CATEGORY_ONBOARDING_VALUE = "OnBoarding";
    public static final String CATEGORY_ACTION_BAR_VALUE = "Action Bar";
    public static final String CATEGORY_DRAWER_VALUE = "Drawer";
    public static final String CATEGROY_RECOMMENDED_VALUE = "Recommended";
    public static final String CATEGROY_NEARBY_VALUE = "Nearby";
    public static final String CATEGROY_ALL_SHOWS_VALUE = "All Shows";
    public static final String CATEGORY_HOME_SCREEN_VALUE = "Home Screen";
    public static final String CATEGORY_ADP_VALUE = "Adp";
    public static final String CATEGORY_SDP_VALUE = "Sdp";
    public static final String CATEGORY_VDP_VALUE = "Vdp";
    public static final String CATEGORY_FAVORITES_VALUE = "Favorites";
    public static final String CATEGORY_SEARCH_VALUE = "Search";
    public static final String CATEGORY_LEGAL_VALUE = "Legal";
    public static final String CATEGORY_NOTIFICATION_VALUE = "Notification";
    public static final String CATEGORY_PUSH_NOTIFICATION_VALUE = "Push Notification";
    public static final String CATEGORY_LOCATION_VALUE = "Location";
    public static final String CATEGORY_RATE_US_MODAL = "RateUsModal";
    public static final String CATEGORY_YOUR_ORDERS = "Your Orders";
    public static final String CATEGORY_UBER_MODAL = "Uber Modal";


    public static final String CATEGORY_ERROR_VALUE = "Error";
    public static final String CATEGORY_HOUSEKEEPING_VALUE = "HouseKeeping";

    //Source
    public static final String SOURCE = "Source";
    /* "Source value" are the same as CATEGORY*/

    //Platform
    public static final String PLATFORM = "Platform";
    public static final String PLATFORM_VALUE = "LNANDROID";

    //Event
    public static final String EVENT_NAME = "Event Name";
    public static final String EVENT_ID = "Event Id";

    public static final String ARTIST_NAME = "Artist Name";
    public static final String ARTIST_ID = "Artist Id";

    public static final String VENUE_NAME = "Venue Name";
    public static final String VENUE_ID = "Venue Id";

    //Favorites
    public static final String STATE = "state";
    public static final String STATE_FAVORITED_VALUE = "favorited";
    public static final String STATE_UNFAVORITED_VALUE = "unfavorited";

    //Sdp
    public static final String TYPE_OF_FIND_TICKETS_OPTIONS_SELECTED = "Type of Find Tickets option selected";

    //Video
    public static final String VIDEO_NAME = "Video name";
    public static final String VIDEO_URL = "Video URL";

    //Notification
    public static final String NOTIFICATION_NAME = "Notification name";
    public static final String NOTIFICATION_ID = "Notification Id";

    //Location
    public static final String LOCATION_LATLONG = "Location LatLong";
    public static final String LOCATION_NAME = "Location Name";
    public static final String LOCATION_CURRENT_LOCATION_USE = "Use current Location";

    //HouseKeeping
    public static final String AIS_USER_ID = "AIS User ID";
    public static final String ANDROID_DEVICE_ID = "Android Device ID";
    public static final String TOKEN = "Token";
    public static final String TOKEN_TYPE = "Token type";
    public static final String GRANTED_ACCESS_TO_MUSIC_LIBRARY = "Granted Access to Music Library";

    //Music Library Analysis
    public static final String NUMBER_OF_ARTISTS_FOUND = "Number of artists found";

    //Login
    public static final String FB_LOGGED_IN = "FB Logged In";
    public static final String GOOGLE_LOGGED_IN = "Google+ Logged In";

    //Deep Link
    public static final String BTID = "BTID";
    public static final String UI = "UI";
    public static final String C = "C";
    public static final String FROM = "From";

    //Push Notification
    public static final String MESSAGE_VALUE = "Message Value";
    public static final String MESSAGE_ID = "Message Id";

    //App Rating
    public static final String RATE_US_SHOWN = "Rate Us Shown";
    public static final String RATE_US_TAPPED = "Rate Us Tapped";
    public static final String DONT_SHOW_AGAIN_TAPPED = "Don't Show Again Tapped";
    public static final String NOT_NOW = "Not Now";

    //Uber
    public static final String UBER_APP = "uber app";
    public static final String UBER_APP_UNINSTALLED = "uninstalled";
    public static final String UBER_APP_INSTALLED = "installed";
    public static final String UBER_ORIGIN = "origin";
    public static final String UBER_ORIGIN_VDP = "vdp";
    public static final String UBER_ORIGIN_YOUR_ORDERS = "your orders";
    public static final String UBER_PRODUCT = "uber product";
    public static final String UBER_USER_CURRENT_LOCATION = "user current location";
    public static final String UBER_USER_CURRENT_DESTINATION = "destination";


    public static final String TRACK_URL_SCHEMES = "Track URL Schemes";


}
