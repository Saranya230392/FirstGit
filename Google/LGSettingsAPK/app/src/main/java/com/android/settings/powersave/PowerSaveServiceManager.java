package com.android.settings.powersave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.lge.constants.SettingsConstants;

public class PowerSaveServiceManager extends BroadcastReceiver {

    private static final String TAG = "PowerSaveServiceManager";
    private Context mContext = null;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage run");
            /* init PowerSaveStarted : False */
            initPowerSaveStarted();
            //doPowerSaveBatteryService(context);
            doPowerSaveService();
        }

    };

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "onReceive android.intent.action.BOOT_COMPLETED");
            mContext = context;

            Thread doThread = new Thread() {
                @Override
                public void run() {
                    Log.i(TAG, "doThread run handler=" + handler);
                    if (handler != null) {
                        handler.sendEmptyMessage(0);
                    }
                }
            };
            doThread.start();
        }
        else {
            Log.i(TAG, "onReceive unexpected intent" + intent.toString());
        }
    }

    private void doPowerSaveService() {
        if (mContext == null) {
            return;
        }
        if (Settings.System.getInt(
                mContext.getContentResolver(), SettingsConstants.System.POWER_SAVE_ENABLED,
                PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0) {

            Log.i(TAG, "doPowerSaveService, Start the Power save service");

            Intent intent = new Intent(mContext, PowerSaveService.class);
            mContext.startService(intent);
        }
    }

    private void initPowerSaveStarted() {
        if (mContext == null) {
            return;
        }
        int powerSaveStarted = Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_STARTED, -1);

        Log.i(TAG, "initPowerSaveStarted, POWER_SAVE_STARTED : " + powerSaveStarted);
        if (powerSaveStarted == PowerSave.NOTIFICATION_ACTIVATED) {
            /* init Restore */
            PowerSave powerSave = new PowerSave(mContext);
            powerSave.doRestore();
        }
        Settings.System.putInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_STARTED, -1);
    }
    /*private void doPowerSaveBatteryService(Context context) {
        Log.i(TAG, "doPowerSaveBatteryService, Start the Power save service");

        Intent intent = new Intent(context, PowerSaveBatteryService.class);
        context.startService(intent);
    }*/

}
