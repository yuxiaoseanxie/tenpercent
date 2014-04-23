package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;

import java.util.HashMap;
import java.util.Map;

public class BoxOfficeTabFragment extends Fragment {
    private BoxOffice boxOfficeInfo;
    private String[] displayedItems;
    private Map<String, String> itemsToStrings;

    private ScrollView scrollView;
    private TextView text;
    private int textScrollY;

    public static BoxOfficeTabFragment newInstance(BoxOffice boxOfficeInfo, String[] displayedItems) {
        BoxOfficeTabFragment fragment = new BoxOfficeTabFragment();
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

        this.scrollView = (ScrollView) view.findViewById(R.id.fragment_box_office_info_scroll_view);
        this.text = (TextView) view.findViewById(R.id.fragment_box_office_info_text);
        render();

        if (textScrollY != 0) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, textScrollY);
                }
            });
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        textScrollY = scrollView.getScrollY();
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

        final String sectionTerminator = "<br><br>\n";
        String content = "";
        for (String item : getDisplayedItems()) {
            String value = values.get(item);
            if(value == null)
                continue;

            content += "<h3>" + itemsToStrings.get(item) + "</h3>\n";
            content += value.replace("\n", "<br>\n");
            content += sectionTerminator;
        }

        if(text.length() > 0)
            content = content.substring(0, content.length() - sectionTerminator.length());

        text.setText(Html.fromHtml(content));
    }
}
