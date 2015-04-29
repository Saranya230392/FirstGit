package com.android.settings.lge;

import com.android.settings.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DoubleTitlePreference extends Preference {

    private TextView mSubTitleTV;

    private Drawable mIcon;
    private CharSequence mSubTitleText;
    private int mSubTitleGravity;

    public DoubleTitlePreference(Context context) {
        this(context, null, 0);
    }

    public DoubleTitlePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleTitlePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.preference_double_title);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if (imageView != null && mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }

        mSubTitleTV = (TextView) view.findViewById(R.id.subtitle);
        mSubTitleTV.setText(mSubTitleText);
        mSubTitleTV.setGravity(mSubTitleGravity);
    }

    public void setSubTitle(CharSequence text) {
        mSubTitleText = text;
    }

    public void setSubTitle(int resid) {
        mSubTitleText = getContext().getResources().getText(resid, "");
    }

    public void setSubTitleGravity(int gravity) {
        mSubTitleGravity = gravity;
    }
}
