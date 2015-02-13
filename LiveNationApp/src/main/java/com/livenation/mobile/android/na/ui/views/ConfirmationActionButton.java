package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
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
        initialize(null);
    }

    public ConfirmationActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationActionButton);
        initialize(attributes);
    }

    public ConfirmationActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationActionButton, defStyle, 0);
        initialize(attributes);
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


    protected void initialize(@Nullable TypedArray attributes) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_confirmation_action_button, this, false);

        this.image = (ImageView) view.findViewById(R.id.view_confirmation_action_image);
        this.title = (TextView) view.findViewById(R.id.view_confirmation_action_button_title);
        this.tagLine = (TextView) view.findViewById(R.id.view_confirmation_action_button_tag_line);

        addView(view);

        if (attributes != null) {
            String title = attributes.getString(R.styleable.ConfirmationActionButton_title);
            setTitle(title);

            String tagLine = attributes.getString(R.styleable.ConfirmationActionButton_tagLine);
            setTagLine(tagLine);

            Drawable image = attributes.getDrawable(R.styleable.ConfirmationActionButton_image);
            setImageDrawable(image);
        }
    }
}
