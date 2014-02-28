package com.livenation.mobile.android.na2.ui.support;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.livenation.mobile.android.na2.R;

import java.io.Serializable;

/**
 * Created by km on 2/28/14.
 */
public class DebugAction implements Serializable {
    private String name;
    private String value;

    public DebugAction(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    public void doAction(Context context) {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getName(), getValue());
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(context, R.string.debug_copied_toast, Toast.LENGTH_SHORT).show();
    }
}
