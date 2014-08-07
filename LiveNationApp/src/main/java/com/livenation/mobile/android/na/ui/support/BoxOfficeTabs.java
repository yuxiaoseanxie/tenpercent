package com.livenation.mobile.android.na.ui.support;

import com.livenation.mobile.android.na.R;

import java.util.HashMap;
import java.util.Map;

public enum BoxOfficeTabs {
    TRANSIT(R.string.box_office_tab_transit, "directions", "parking_info", "public_transit_info"),
    BOX_OFFICE(R.string.box_office_tab_general, "general_info", "misc_info", "will_call_info", "general_rules", "child_rules");


    //region Section Titles

    private static final Map<String, Integer> SECTION_TITLE_MAPPINGS = new HashMap<String, Integer>();

    static {
        SECTION_TITLE_MAPPINGS.put("directions", R.string.box_office_directions);
        SECTION_TITLE_MAPPINGS.put("parking_info", R.string.box_office_parking_info);
        SECTION_TITLE_MAPPINGS.put("public_transit_info", R.string.box_office_public_transit_info);

        SECTION_TITLE_MAPPINGS.put("general_info", R.string.box_office_general_info);
        SECTION_TITLE_MAPPINGS.put("misc_info", R.string.box_office_misc_info);
        SECTION_TITLE_MAPPINGS.put("will_call_info", R.string.box_office_will_call_info);
        SECTION_TITLE_MAPPINGS.put("general_rules", R.string.box_office_general_rules);
        SECTION_TITLE_MAPPINGS.put("child_rules", R.string.box_office_child_rules);
    }

    private int titleResId;

    //endregion


    //region Section Data
    private String[] sections;

    private BoxOfficeTabs(int titleResId, String... sections) {
        this.titleResId = titleResId;
        this.sections = sections;
    }

    public static int getTitleResIdForSection(String section) {
        return SECTION_TITLE_MAPPINGS.get(section);
    }

    public int getTitleResId() {
        return titleResId;
    }

    public String[] getSections() {
        return sections;
    }

    //endregion
}
