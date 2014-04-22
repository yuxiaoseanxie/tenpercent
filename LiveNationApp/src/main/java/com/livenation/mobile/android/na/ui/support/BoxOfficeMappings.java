package com.livenation.mobile.android.na.ui.support;

public enum BoxOfficeMappings {
    TRANSIT("directions", "parking_info", "public_transit_info"),
    BOX_OFFICE("general_info", "misc_info", "will_call_info", "general_rules", "child_rules");

    public String[] getItems() {
        return items;
    }

    private String[] items;
    private BoxOfficeMappings(String... items) {
        this.items = items;
    }
}
