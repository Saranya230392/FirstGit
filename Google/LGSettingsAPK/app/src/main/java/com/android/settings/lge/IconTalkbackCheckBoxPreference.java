package com.android.settings.lge;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.view.View;

import com.android.settings.R;

public class IconTalkbackCheckBoxPreference extends CheckBoxPreference {
    private ImageView mIcon;
    private String mTalkbcakDesc;

    public IconTalkbackCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public IconTalkbackCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mIcon = (ImageView)view.findViewById(android.R.id.icon);
        if (mIcon != null && mTalkbcakDesc != null) {
            mIcon.setContentDescription(mTalkbcakDesc);
        }
    }

    public void setIconTalkbackDescription(String description) {
        mTalkbcakDesc = description;
    }
}
