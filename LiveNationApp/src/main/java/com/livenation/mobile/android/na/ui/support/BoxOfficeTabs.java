package com.livenation.mobile.android.na.ui.support;

import com.livenation.mobile.android.na.R;

public enum BoxOfficeTabs {
    TRANSIT(R.string.box_office_tab_transit, "directions", "parking_info", "public_transit_info"),
    BOX_OFFICE(R.string.box_office_tab_general, "general_info", "misc_info", "will_call_info", "general_rules", "child_rules");

    public int getTitleResId() {
        return titleResId;
    }

    public String[] getFields() {
        return fields;
    }

    private int titleResId;
    private String[] fields;
    private BoxOfficeTabs(int titleResId, String... fields) {
        this.titleResId = titleResId;
        this.fields = fields;
    }
}
