package com.android.settings.bootcompleted;

import android.content.Context;
import android.util.Log;

import com.android.settings.lge.NightModeInfo;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;

public class NightModeBoot {
    private static NightModeInfo mNightmodeInfo;
    private static final String TAG = "SettingBootReceiver";

    public static void onReceive(Context context) {
        Log.d(TAG, "[Nightmode] Enter receiver");
        mNightmodeInfo = new NightModeInfo(context);
        Boolean mAutomaticAvailable = Config.getFWConfigBool(context, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");

        if (mAutomaticAvailable) {
            return;
        }

        Log.d(TAG, "[Nightmode] BOOT_COMPLETED");        
        setNightMode(context);
        mNightmodeInfo.cancelPendingIntent(context);
        mNightmodeInfo.requestPendingIntent(context);
        if (mNightmodeInfo.getNightCheckDB() == 1) {
            mNightmodeInfo.startNotification();
        } else {
            mNightmodeInfo.endNotification();
        }
    }

    public static void setNightMode(Context context) {
        int CheckSettingStyle = mNightmodeInfo.getSettingsStyle();
        if (CheckSettingStyle == 0
                && !Utils.supportSplitView(context)) {
            mNightmodeInfo.setNightNode();
        } else {
            if (mNightmodeInfo.isEasySetting()
                    || Utils.supportSplitView(context)) {
                mNightmodeInfo.setNightNodeTabSettings();
            } else {
                mNightmodeInfo.setNightNode();
            }
        }
    }
}
