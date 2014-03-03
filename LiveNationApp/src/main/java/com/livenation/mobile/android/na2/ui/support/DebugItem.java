package com.livenation.mobile.android.na2.ui.support;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.livenation.mobile.android.na2.R;

import java.io.Serializable;

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


    public void doAction(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getValue());

        context.startActivity(Intent.createChooser(shareIntent, "Share debug item"));
    }
}
