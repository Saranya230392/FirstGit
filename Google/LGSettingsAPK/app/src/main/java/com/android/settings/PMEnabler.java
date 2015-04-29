package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;

public class PMEnabler implements CompoundButton.OnCheckedChangeListener,
        Preference.OnPreferenceChangeListener {

    private String TAG = "PMEnabler";
    private final Context mContext;
    private Switch mSwitch;
    private static final String CMCC_DM = "cmcc_device_management";

    public PMEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
        Log.i(TAG, "setSwitch()");
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });

        boolean value = android.provider.Settings.System.getInt(
                mContext.getContentResolver(), CMCC_DM, 1) == 1 ? true : false;
        Log.i(TAG, "setSwitch() isChecked: " + value);
        mSwitch.setChecked(value);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "onCheckedChanged() isChecked: " + isChecked);

        if (mSwitch != null) {
            mSwitch.setChecked(isChecked);
            android.provider.Settings.System.putInt(mContext
                    .getContentResolver(), CMCC_DM, isChecked == true ? 1 : 0);
        } else {
            Log.e(TAG, "mSwitch is null");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub

        boolean bValue = (Boolean)newValue;
        Log.d(TAG, "onPreferenceChange = " + bValue);
        if (mSwitch != null) {
            mSwitch.setChecked(bValue);
            android.provider.Settings.System.putInt(mContext
                    .getContentResolver(), CMCC_DM, bValue == true ? 1 : 0);
        } else {
            Log.e(TAG, "mSwitch is null");
        }
        return false;
    }

}
