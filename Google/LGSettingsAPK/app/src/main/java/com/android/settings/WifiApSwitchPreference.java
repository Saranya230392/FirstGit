package com.android.settings;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.android.settings.wifi.WifiApEnabler;

public class WifiApSwitchPreference extends SwitchPreference {

    private boolean mOnDivider = true;
    //private final Listener mListener = new Listener();
    private Switch mSwitch;
    private final WifiApEnabler mWifiApEnabler;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //int value = isChecked ? 1 : 0;
            if (isChecked != mSwitch.isChecked()) {
                mSwitch.setChecked(isChecked);
            }

        }
    }

    public WifiApSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mWifiApEnabler = new WifiApEnabler(context, new Switch(context));
    }

    public WifiApSwitchPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mWifiApEnabler.setSwitch(mSwitch);

        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void resume() {
        if (mWifiApEnabler != null) {
            mWifiApEnabler.resume();
        }
    }

    public void pause() {
        if (mWifiApEnabler != null) {
            mWifiApEnabler.pause();
        }
    }
}
