package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("UnusedDeclaration")
public class ConfirmationActionButton extends FrameLayout {
    private ImageView image;
    private TextView title;
    private TextView tagLine;

    public ConfirmationActionButton(Context context) {
        super(context);
        initialize();
    }

    public ConfirmationActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ConfirmationActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }


    //region Properties

    public String getTitle() {
        return title.getText().toString();
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getTagLine() {
        return tagLine.getText().toString();
    }

    public void setTagLine(String tagLine) {
        this.tagLine.setText(tagLine);
    }

    public Drawable getImageDrawable() {
        return image.getDrawable();
    }

    public void setImageDrawable(Drawable drawable) {
        this.image.setImageDrawable(drawable);
    }

    public void setImageResource(int resId) {
        this.image.setImageResource(resId);
    }

    //endregion


    protected void initialize() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_confirmation_action_button, this, false);

        this.image = (ImageView) view.findViewById(R.id.view_confirmation_action_image);
        this.title = (TextView) view.findViewById(R.id.view_confirmation_action_button_title);
        this.tagLine = (TextView) view.findViewById(R.id.view_confirmation_action_button_tag_line);

        addView(view);
    }
}
