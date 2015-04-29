package com.android.settings.powersave;

import com.android.settings.R;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

public class PowerSaveSwitchPreference extends SwitchPreference {

    private Context mContext;

    private final Listener mListener = new Listener();
    private Switch mSwitch;
    private boolean mSwitchEnabled = true;
    private PowerManager mPowerManager;

    private final Handler mHandler = new Handler();
    private static final long WAIT_FOR_SWITCH_ANIM = 500;
    private static final String TAG = "PowerSaveSwitchPreference";

    private class Listener implements CompoundButton.OnCheckedChangeListener,
            View.OnClickListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            int value = isChecked ? 1 : 0;
            if ("battery_saver".equals(getKey())) {

                int preState = Settings.Global.getInt(
                        mContext.getContentResolver(), "battery_saver_mode", 0);
                Log.d(TAG, "preState : " + preState);
                if (preState != value) {
                    Settings.Global.putInt(mContext.getContentResolver(),
                            "battery_saver_mode", value);
                    mHandler.removeCallbacks(mStartMode);
                    if (isChecked) {
                        mHandler.postDelayed(mStartMode, WAIT_FOR_SWITCH_ANIM);
                    } else {
                        trySetPowerSaveMode(false);
                    }

                }
            } else {
                if (isChecked != mSwitch.isChecked()) {
                    mSwitch.setChecked(isChecked);
                }
            }

            if ("power_save_enabler".equals(getKey())) {
                int preState = Settings.System.getInt(mContext.getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_ENABLED,
                        PowerSave.DEFAULT_POWER_SAVE_ENABLED);
                if (preState != value) {
                    Settings.System.putInt(mContext.getContentResolver(),
                            SettingsConstants.System.POWER_SAVE_ENABLED, value);
                    doPowerSaveService(isChecked);
                }
            } else if ("power_save_eco_mode".equals(getKey())) {
                int preState = Settings.System.getInt(mContext.getContentResolver(),
                        SettingsConstants.System.ECO_MODE, 0);
                if (preState != value) {
                    Settings.System.putInt(mContext.getContentResolver(),
                            SettingsConstants.System.ECO_MODE, value);
                }
            } else if ("power_smart_saver".equals(getKey())) {
                int preState = Settings.System.getInt(mContext.getContentResolver(),
                        "power_save_smart_saver", 0);
                if (preState != value) {
                    Settings.System.putInt(mContext.getContentResolver(),
                            "power_save_smart_saver", value);
                }
            }
        }

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
        }
    }

    public void setCheckedUpdate(boolean checked) {
        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
    }

    public void setSwitchEnabled(boolean enabled) {
        mSwitchEnabled = enabled;
    }

    public PowerSaveSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mContext = context;
        mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);

    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        mSwitch = (Switch)view.findViewById(R.id.switchWidget);

        if ("power_save_enabler".equals(getKey())) {
            mSwitch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED,
                    PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0);
        } else if ("power_save_eco_mode".equals(getKey())) {
            ImageView mDivider = (ImageView)view.findViewById(R.id.switchImage);
            mDivider.setVisibility(View.GONE);

            mSwitch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.ECO_MODE, 0) != 0);
        } else if ("power_smart_saver".equals(getKey())) {
            ImageView mDivider = (ImageView)view.findViewById(R.id.switchImage);
            mDivider.setVisibility(View.GONE);

            mSwitch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                    "power_save_smart_saver", 1) != 0);
        } else if ("battery_saver".equals(getKey())) {
            mSwitch.setChecked(Settings.Global.getInt(mContext.getContentResolver(),
                    "battery_saver_mode", 0) > 0);
            if (mSwitch != null) {
                mSwitch.setEnabled(mSwitchEnabled);
            }
        }
        mSwitch.setOnCheckedChangeListener(mListener);
        mSwitch.setOnClickListener(mListener);
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

    private final Runnable mStartMode = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    trySetPowerSaveMode(true);
                }
            });
        }
    };

    private final Runnable mUpdateSwitch = new Runnable() {
        @Override
        public void run() {
            updateSwitch();
        }
    };

    private void updateSwitch() {
        boolean isChecked = Settings.Global.getInt(
                mContext.getContentResolver(), "battery_saver_mode", 0) > 0;
        mSwitch.setChecked(isChecked);
    }

    private void trySetPowerSaveMode(boolean mode) {
        BatterySaverSettings.setSwitch(mode, mContext, mPowerManager);
        mHandler.post(mUpdateSwitch);

    }

}
