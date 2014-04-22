package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;

import java.util.HashMap;
import java.util.Map;

public class BoxOfficeInfoFragment extends Fragment {
    private BoxOffice boxOfficeInfo;
    private String[] displayedItems;
    private Map<String, String> itemsToStrings;

    private TextView text;

    public static BoxOfficeInfoFragment newInstance(BoxOffice boxOfficeInfo, String[] displayedItems) {
        BoxOfficeInfoFragment fragment = new BoxOfficeInfoFragment();
        fragment.setBoxOfficeInfo(boxOfficeInfo);
        fragment.setDisplayedItems(displayedItems);
        return fragment;
    }

    //region Lifecycle


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        this.itemsToStrings = new HashMap<String, String>();

        itemsToStrings.put("directions", getString(R.string.box_office_directions));
        itemsToStrings.put("parking_info", getString(R.string.box_office_parking_info));
        itemsToStrings.put("public_transit_info", getString(R.string.box_office_public_transit_info));

        itemsToStrings.put("general_info", getString(R.string.box_office_general_info));
        itemsToStrings.put("misc_info", getString(R.string.box_office_misc_info));
        itemsToStrings.put("will_call_info", getString(R.string.box_office_will_call_info));
        itemsToStrings.put("general_rules", getString(R.string.box_office_general_rules));
        itemsToStrings.put("child_rules", getString(R.string.box_office_child_rules));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box_office_info, container, false);

        this.text = (TextView) view.findViewById(R.id.fragment_box_office_info_text);
        render();

        return view;
    }


    //endregion


    //region Properties

    public BoxOffice getBoxOfficeInfo() {
        return boxOfficeInfo;
    }

    public void setBoxOfficeInfo(BoxOffice boxOfficeInfo) {
        this.boxOfficeInfo = boxOfficeInfo;
    }

    public String[] getDisplayedItems() {
        return displayedItems;
    }

    public void setDisplayedItems(String[] displayedItems) {
        this.displayedItems = displayedItems;
    }

    //endregion


    private void render() {
        Map<String, String> values = getBoxOfficeInfo().getValues();

        String content = "";
        for (String item : getDisplayedItems()) {
            String value = values.get(item);
            if(value == null)
                continue;

            content += "<b>" + itemsToStrings.get(item) + "</b><br>\n";
            content += value.replace("\n", "<br>\n");
            content += "<br><br>\n";
        }

        text.setText(Html.fromHtml(content));
    }
}
