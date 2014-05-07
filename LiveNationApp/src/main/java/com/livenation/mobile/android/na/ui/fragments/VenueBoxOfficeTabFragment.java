package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.BoxOfficeTabs;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.BoxOffice;

import java.util.Map;

public class VenueBoxOfficeTabFragment extends Fragment {
    private BoxOffice boxOfficeInfo;
    private String[] displayedSections;

    private ScrollView scrollView;
    private TextView text;
    private int textScrollY;

    //region Lifecycle

    public static VenueBoxOfficeTabFragment newInstance(BoxOffice boxOfficeInfo, String[] displayedSections) {
        VenueBoxOfficeTabFragment fragment = new VenueBoxOfficeTabFragment();
        fragment.setBoxOfficeInfo(boxOfficeInfo);
        fragment.setDisplayedSections(displayedSections);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
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

    public String[] getDisplayedSections() {
        return displayedSections;
    }

    public void setDisplayedSections(String[] displayedSections) {
        this.displayedSections = displayedSections;
    }

    //endregion


    //region Rendering

    private static final String SECTION_TERMINATOR = "<br><br>\n";

    private void render() {
        Map<String, String> values = getBoxOfficeInfo().getValues();

        String content = "";
        for (String section : getDisplayedSections()) {
            String value = values.get(section);
            if(value == null)
                continue;

            content += "<h3>" + getString(BoxOfficeTabs.getTitleResIdForSection(section)) + "</h3>\n";
            content += value.replace("\n", "<br>\n");
            content += SECTION_TERMINATOR;
        }

        if (content.length() > 0) {
            content = content.substring(0, content.length() - SECTION_TERMINATOR.length());
        } else {
            content = "<i>No Info</i>";
        }

        text.setText(Html.fromHtml(content));
    }

    //endregion
}
