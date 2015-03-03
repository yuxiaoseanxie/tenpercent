package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.PermissionView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTips;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Created by elodieferrais on 3/2/15.
 */
public class ShowPermissionsFragment extends LiveNationFragment {
    private static final String EVENT_TIPS = "com.livenation.mobile.android.na.ui.fragments.ShowPermissionsFragment.EVENT_TIPS";
    private ViewGroup permissionContainer;

    public static ShowPermissionsFragment newInstance(EventTips eventTips) {
        ShowPermissionsFragment fragment = new ShowPermissionsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EVENT_TIPS, eventTips);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_permissions, container, false);
        TextView header = (TextView) view.findViewById(R.id.fragment_permissions_header);
        header.setText(R.string.tips_allowed_section_title);
        permissionContainer = (ViewGroup) view.findViewById(R.id.fragment_permissions_container);

        EventTips eventTips = (EventTips) getArguments().getSerializable(EVENT_TIPS);
        String[] allowed = eventTips.getTipsRules().getAllowed();
        String[] notAllowed = eventTips.getTipsRules().getNotAllowed();

        permissionContainer.removeAllViews();
        populateList(allowed, R.drawable.show_tip_allowed_checkmark);
        populateList(notAllowed, R.drawable.show_tip_disallowed);
        return view;
    }

    private void populateList(String[] permissions, int iconRes) {
        for (int i = 0; i < permissions.length; i++) {
            PermissionView view = new PermissionView(getActivity());
            view.getTitle().setText(permissions[i]);
            view.getIcon().setImageDrawable(getResources().getDrawable(iconRes));
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            permissionContainer.addView(view, layoutParams);
        }
    }
}
