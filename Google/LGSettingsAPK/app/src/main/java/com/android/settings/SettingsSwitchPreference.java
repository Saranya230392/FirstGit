package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsSwitchPreference extends SwitchPreference {

    private boolean mOnDivider = true;
    private final Listener mListener = new Listener();
    private Switch mSwitch;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }
            setChecked(isChecked);
        }
    }

    public SettingsSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
    }

    public SettingsSwitchPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable)checkableView).setChecked(super.isChecked());
            if (checkableView instanceof Switch) {
                mSwitch = (Switch)checkableView;
                mSwitch.setOnCheckedChangeListener(mListener);
                mSwitch.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        }

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            if (vertical_divider != null) {
                vertical_divider.setVisibility(View.INVISIBLE);
            }
        }
    }
}
