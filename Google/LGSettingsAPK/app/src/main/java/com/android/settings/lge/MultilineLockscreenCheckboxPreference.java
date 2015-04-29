
package com.android.settings.lge;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class MultilineLockscreenCheckboxPreference extends CheckBoxPreference {
    public MultilineLockscreenCheckboxPreference(Context context) {
        super(context);
    }

    public MultilineLockscreenCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultilineLockscreenCheckboxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        makeMultiline(view);
    }

    protected void makeMultiline(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            View v = vg.findViewById(android.R.id.title);
            if (v != null && v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setSingleLine(false);
                tv.setEllipsize(null);
            }
        }
    }
}
