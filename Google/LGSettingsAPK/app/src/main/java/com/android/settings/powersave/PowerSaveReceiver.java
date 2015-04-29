package com.android.settings.powersave;

import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.Global;

public class PowerSaveReceiver extends BroadcastReceiver {

    private static final String POWER_SAVE_START = "com.lge.settings.POWER_SAVER_START";
    private static final String TAG = "PowerSaveReceiver";
    private static final String ACTION_STOP_SAVER = "PNW.stopSaver";

    private PowerSave mPowerSave;
    private PowerManager mPowerManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        /*KK receive POWER_SAVE_START below
        To enable KK PowerSaver receive change TAG to POWER_SAVE_START*/
        if (TAG.equals(intent.getAction())) {
            int value = intent.getIntExtra("start", 0);
            Log.d(TAG, "POWER_SAVE_START:" + value);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED, value);

            //            int backup_value = Settings.System.getInt(context.getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
            //            Settings.System.putInt(context.getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE, 100 );

            doPowerSaveService(context, value > 0);
        }

        if (POWER_SAVE_START.equals(intent.getAction())) {
            Log.d(TAG, "Receive Intent : POWER_SAVE_START");
            boolean isChecked = Settings.Global.getInt(context.getContentResolver(),
                    "battery_saver_mode", 0) > 0;
            mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            BatterySaverSettings.setSwitch(isChecked, context, mPowerManager);
        }

        if (ACTION_STOP_SAVER.equals(intent.getAction())) {
            Log.d(TAG, "Receive Intent : ACTION_STOP_SAVER");

            Settings.Global.putInt(context.getContentResolver(),
                    "battery_saver_mode", 0);
            mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mPowerManager.setPowerSaveMode(false);
            Settings.Global.putInt(context.getContentResolver(),
                    Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);
        }
    }

    private void doPowerSaveService(Context context, boolean enabled) {
        mPowerSave = new PowerSave(context);

        if (!mPowerSave.isRunningPowerSaveService()) {
            Log.d(TAG, "Re-start PowerSaveService");
            Intent i = new Intent(context, PowerSaveService.class);
            context.startService(i);
        }

        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                "com.android.settings.powersave.PowerSaveService");
        if (enabled) {
            context.startService(intent);
        }
        else {
            context.stopService(intent);
        }
    }
}