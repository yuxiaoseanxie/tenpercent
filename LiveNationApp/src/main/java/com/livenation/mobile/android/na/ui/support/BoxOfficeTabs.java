package com.livenation.mobile.android.na.ui.support;

public enum BoxOfficeTabs {
    TRANSIT("directions", "parking_info", "public_transit_info"),
    BOX_OFFICE("general_info", "misc_info", "will_call_info", "general_rules", "child_rules");

    public String[] getFields() {
        return fields;
    }

    private String[] fields;
    private BoxOfficeTabs(String... fields) {
        this.fields = fields;
    }
}
