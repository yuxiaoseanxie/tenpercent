package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
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

        itemsToStrings.put("directions", "Directions");
        itemsToStrings.put("parking_info", "Parking Info");
        itemsToStrings.put("public_transit_info", "Public Transit Info");

        itemsToStrings.put("general_info", "General Info");
        itemsToStrings.put("misc_info", "Misc");
        itemsToStrings.put("will_call_info", "Will Call");
        itemsToStrings.put("general_rules", "General Rules");
        itemsToStrings.put("child_rules", "Child Rules");
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

        text.setText(linkifyHtml(content, Linkify.ALL));
    }

    public static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = Html.fromHtml(html);
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }
}
