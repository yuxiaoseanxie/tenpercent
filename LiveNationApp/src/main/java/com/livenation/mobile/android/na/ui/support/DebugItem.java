package com.livenation.mobile.android.na.ui.support;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by km on 2/28/14.
 */
public class DebugItem implements Serializable {
    public static final int TYPE_INFO = 0;
    public static final int TYPE_ACTION = 1;


    private String name;
    private String value;

    public DebugItem(String name, String value) {
        this.name = name;

        if(value != null)
            this.value = value;
        else
            this.value = "(None)";
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getType() { return TYPE_INFO; }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void doAction(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getValue());

        context.startActivity(Intent.createChooser(shareIntent, "Share debug item"));
    }


    public static String convertListToString(List<DebugItem> items) {
        String string = "";
        for (DebugItem item : items) {
            if(item.getType() != TYPE_INFO)
                continue;

            string += item + "\n";
        }
        return string;
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }
}
