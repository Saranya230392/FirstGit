package com.android.settings.powersave;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.lge.constants.SettingsConstants;
import android.provider.Settings;

public class PowerSaveEnabler implements OnCheckedChangeListener {

    private Switch mSwitch;
    private Context mContext;

    private Fragment mFragment;

    public PowerSaveEnabler(Context context, Switch switch_, Fragment fragment) {
        mContext = context;
        mSwitch = switch_;
        mFragment = fragment;

        //        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        //        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        //        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
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
        boolean isEnabled = (Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0);
        //        boolean isDisabled = false;
        mSwitch.setChecked(isEnabled);
        //        mSwitch.setEnabled(isEnabled || isDisabled);
    }

    public void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mSwitch.setChecked(checked);
        }
    }

    public void resume() {
        //        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);
        boolean isEnabled = (Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0);
        //      boolean isDisabled = false;
        mSwitch.setChecked(isEnabled);
    }

    public void pause() {
        //        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnCheckedChangeListener(null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int value = isChecked ? 1 : 0;
        setSwitchChecked(isChecked);

        int preState = Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED);
        if (preState != value) {
            Settings.System.putInt(mContext.getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED, value);
            doPowerSaveService(isChecked);
        }

        if (mFragment instanceof PowerSaveSettings) {
            PowerSaveSettings fragment = (PowerSaveSettings)mFragment;
            fragment.onSwitchCheckedChanged(isChecked);
        }

    }

    private void doPowerSaveService(boolean enabled) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                "com.android.settings.powersave.PowerSaveService");
        if (enabled) {
            mContext.startService(intent);
        }
        else {
            mContext.stopService(intent);
        }
    }

    public Switch getSwitch() {
        return mSwitch;
    }

}