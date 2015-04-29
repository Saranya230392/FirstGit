package com.android.settings;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.util.Log;

import com.android.settings.wifi.HotSpotEnabler;

public class HotSpotPreference extends SwitchPreference {

    private boolean mOnDivider = true;
    private Switch mSwitch;
    private final HotSpotEnabler mHotSpotEnabler;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if ((null != mSwitch) && (isChecked != mSwitch.isChecked())) {
                mSwitch.setChecked(isChecked);
            }
        }
    }

    public HotSpotPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mHotSpotEnabler = new HotSpotEnabler(context, new Switch(context));
    }

    public HotSpotPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mHotSpotEnabler.setSwitch(mSwitch);

        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("HotSpotPreference", "came into the HotSpotEnabler");

            }
        });

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isSwitchChecked() {
        if (null != mSwitch) {
            return mSwitch.isChecked();
        } else {
            return false;
        }
    }

    public void resume() {
        Log.d("HotSpotPreference", "resume");
        if (mHotSpotEnabler != null) {
            mHotSpotEnabler.resume();
        }
    }

    public void pause() {
        Log.d("HotSpotPreference", "pause");
        if (mHotSpotEnabler != null) {
            mHotSpotEnabler.pause();
        }
    }

    public void settingVZWMobileHotspot(int tethervalue) {
        mHotSpotEnabler.settingVZWMobileHotspot(tethervalue);

    }

    public void restoreVZWMobileHotspotSetting() {
        mHotSpotEnabler.restoreVZWMobileHotspotSetting();

    }
}
