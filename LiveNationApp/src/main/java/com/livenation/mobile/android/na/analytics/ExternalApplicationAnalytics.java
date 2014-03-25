package com.livenation.mobile.android.na.analytics;

public enum ExternalApplicationAnalytics {
    TICKETMASTER("Ticketmaster", "com.ticketmaster.mobile.android.na", ApplicationType.LIVE_NATION_APPS),
    INSOMNIAC("Insomniac", "com.mobileroadie.app_12058", ApplicationType.LIVE_NATION_APPS),
    FACEBOOK("Facebook", "com.facebook.katana", ApplicationType.SOCIAL_APPS),
    TWITTER("Twitter", "com.twitter.android", ApplicationType.SOCIAL_APPS),
    VINE("Vine", "co.vine.android", ApplicationType.SOCIAL_APPS),
    INSTAGRAM("Instagram", "com.instagram.android", ApplicationType.SOCIAL_APPS),
    FOURSQUARE("Foursquare", "com.joelapenna.foursquared", ApplicationType.SOCIAL_APPS),
    GOOGLE_PLUS("Google+", "com.google.android.apps.plus", ApplicationType.SOCIAL_APPS),
    PATH("Path", "com.path", ApplicationType.SOCIAL_APPS),
    SNAPCHAT("Snapchat", "com.snapchat.android", ApplicationType.SOCIAL_APPS),
    YOUTUBE("Youtube", "com.google.android.youtube", ApplicationType.VIDEO_AND_CURATED_CONTENT_APPS),
    VIMEO("Vimeo", "com.vimeo.android.videoapp", ApplicationType.VIDEO_AND_CURATED_CONTENT_APPS),
    NETFLIX("Netflix", "com.netflix.mediaclient", ApplicationType.VIDEO_AND_CURATED_CONTENT_APPS),
    HULU("Hulu", "com.hulu.plus", ApplicationType.VIDEO_AND_CURATED_CONTENT_APPS),
    FLIPBOARD("Flipboard", "flipboard.app", ApplicationType.VIDEO_AND_CURATED_CONTENT_APPS),
    RDIO("Rdio", "com.rdio.android.ui", ApplicationType.MUSIC_APPS),
    PANDORA("Pandora", "com.pandora.android", ApplicationType.MUSIC_APPS),
    SPOTIFY("Spotify", "com.spotify.mobile.android.ui", ApplicationType.MUSIC_APPS),
    SHAZAM("Shazam", "com.shazam.android", ApplicationType.MUSIC_APPS),
    SONGZA("Songza", "com.ad60.songza", ApplicationType.MUSIC_APPS),
    VEVO("Vevo", "com.vevo", ApplicationType.MUSIC_APPS),
    SOUNDCLOUD("SoundCloud", "com.soundcloud.android", ApplicationType.MUSIC_APPS),
    IHEARTRADIO("iHeartRadio", "com.clearchannel.iheartradio.controller", ApplicationType.MUSIC_APPS),
    AMAZONCLOUDPLAYER("Amazon Cloud Player", "com.amazon.mp3", ApplicationType.MUSIC_APPS),
    SOUNDHOUND("SoundHound", "com.melodis.midomiMusicIdentifier.freemium", ApplicationType.MUSIC_APPS),
    SOUNDTRACKING("Soundtracking", "com.schematiclabs.soundtracking", ApplicationType.MUSIC_APPS),
    MIXCLOUD("MixCloud", "com.mixcloud.player", ApplicationType.MUSIC_APPS),
    BEATS_MUSIC("Beats Music", "com.beatsmusic.android.client", ApplicationType.MUSIC_APPS),
    GOOGLE_MAPS("Google Maps", "com.google.android.apps.maps", ApplicationType.MAPS_APPS),
    BANDSINTOWN("BandsInTown", "com.bandsintown", ApplicationType.CONCERTS_APPS),
    SONGKICK("Songkick", "com.songkick", ApplicationType.CONCERTS_APPS),
    WILLCALL("WillCall", "com.getwillcall.WillCallApp", ApplicationType.CONCERTS_APPS),
    APPLAUZE("Applauze", "com.applauze.bod", ApplicationType.CONCERTS_APPS),
    THRILLCALL("Thrillcall", "com.thrillcall.thrillcall", ApplicationType.CONCERTS_APPS),
    SEATGEEK("SeatGeek", "com.seatgeek.android", ApplicationType.CONCERTS_APPS),
    ALOOMPA("Aloompa", "com.ultra.ultrafest", ApplicationType.CONCERTS_APPS),
    AXS("AXS", "com.axs.android", ApplicationType.TICKETINGS_APP),
    EVENTBRITE("Eventbrite", "com.eventbrite.attendee", ApplicationType.TICKETINGS_APP),
    STUBHUB("StubHub", "com.stubhub", ApplicationType.TICKETINGS_APP),
    WHATSAPP("Whatsapp", "com.whatsapp", ApplicationType.MESSAGING_APPS),
    WECHAT("WeChat", "com.tencent.mm", ApplicationType.MESSAGING_APPS),
    FACEBOOK_MESSENGER("Facebook Messenger", "com.facebook.orca", ApplicationType.MESSAGING_APPS),
    SKYPE("Skype", "com.skype.raider", ApplicationType.MESSAGING_APPS),
    LINE("Line", "jp.naver.line.android", ApplicationType.MESSAGING_APPS),
    KAKAOTALK("KakaoTalk", "com.kakao.talk", ApplicationType.MESSAGING_APPS),
    GROUPME("GroupMe", "com.groupme.android", ApplicationType.MESSAGING_APPS),
    UBER("Uber", "com.ubercab", ApplicationType.TRANSPORTATION_APPS),
    LYFT("Lyft", "me.lyft.android", ApplicationType.TRANSPORTATION_APPS),
    ZIPCAR("Zipcar", "com.zc.android", ApplicationType.TRANSPORTATION_APPS),
    SIDECAR("Sidecar", "com.sidecarPassenger", ApplicationType.TRANSPORTATION_APPS),
    TAXIMAGIC("TaxiMagic", "com.ridecharge.android.taximagic", ApplicationType.TRANSPORTATION_APPS),
    TINDER("Tinder", "com.tinder", ApplicationType.DATING_APPS),
    EHARMONY("eHarmony", "com.eharmony", ApplicationType.DATING_APPS),
    MATCH("Match", "com.match.android.matchmobile", ApplicationType.DATING_APPS),
    OKCUPID("OKCupid", "com.okcupid.okcupid", ApplicationType.DATING_APPS),
    PLENTYOFFISH("PlentyOfFish", "com.pof.android", ApplicationType.DATING_APPS),
    ZOOSK("Zoosk", "com.zoosk.zoosk", ApplicationType.DATING_APPS),
    SKOUT("Skout", "com.skout.android", ApplicationType.DATING_APPS),
    BADOO("Badoo", "com.badoo.mobile", ApplicationType.DATING_APPS),
    GRINDR("Grindr", "com.grindrapp.android", ApplicationType.DATING_APPS),
    HOWABOUTWE("HowAboutWe", "com.howaboutwe.singles", ApplicationType.DATING_APPS),
    LIFE360("Life360", "com.life360.android.safetymapd", ApplicationType.FAMILY_SMALL_COMMUNITY_APPS),
    NEXT_DOOR("Next Door", "com.nextdoor", ApplicationType.FAMILY_SMALL_COMMUNITY_APPS),
    AVOCADO("Avocado", "io.avocado.android", ApplicationType.COUPLE_APPS),
    PAIR("Pair", "com.tenthbit.juliet", ApplicationType.COUPLE_APPS),
    BETWEEN("Between", "kr.co.vcnc.android.couple", ApplicationType.COUPLE_APPS),
    AMEX("Amex", "com.americanexpress.android.acctsvcs.us", ApplicationType.BANKING_APPS),
    CITIBANK("Citibank", "com.citi.citimobile", ApplicationType.BANKING_APPS),
    CHASE("Chase", "com.chase.sig.android", ApplicationType.BANKING_APPS),
    CAPITALONE("CapitalOne", "com.konylabs.capitalone", ApplicationType.BANKING_APPS),
    BANKOFAMERICA("BankOfAmerica", "com.infonow.bofa", ApplicationType.BANKING_APPS),
    WELLS_FARGO("Wells Fargo", "com.wf.wellsfargomobile", ApplicationType.BANKING_APPS),
    STARWOOD("Starwood", "com.starwood.spg", ApplicationType.OTHER_APPS);

    private String packageName;
    private String label;
    private ApplicationType type;

    ExternalApplicationAnalytics(String label, String packageName, ApplicationType type) {
        this.packageName = packageName;
        this.type = type;
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLabel() {
        return label;
    }

    public ApplicationType getType() {
        return type;
    }

    private enum ApplicationType {
        LIVE_NATION_APPS,
        SOCIAL_APPS,
        VIDEO_AND_CURATED_CONTENT_APPS,
        MUSIC_APPS,
        MAPS_APPS,
        CONCERTS_APPS,
        TICKETINGS_APP,
        ARTISTS_APPS,
        MESSAGING_APPS,
        TRANSPORTATION_APPS,
        DATING_APPS,
        FAMILY_SMALL_COMMUNITY_APPS,
        COUPLE_APPS,
        BANKING_APPS,
        OTHER_APPS
    }

}

