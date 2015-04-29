package com.android.settings.lge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;

public class NightModeReceiver extends BroadcastReceiver {
    private NightModeInfo mNightmodeInfo;
    private static final String TAG = "NightModeReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "[Nightmode] Enter receiver");
        mNightmodeInfo = new NightModeInfo(context);
        mContext = context;
        String action = intent.getAction();
        Boolean mAutomaticAvailable = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");

        if (mAutomaticAvailable) {
            receiverDisable(context);
        } else {
            if (NightModeInfo.NIGHT_MODE_ACTION_START.equals(action)) {
                Log.d(TAG, "[Nightmode] NIGHT_MODE_ACTION_START");
                setNightMode();
            } else if (NightModeInfo.NIGHT_MODE_ACTION_END.equals(action)) {
                Log.d(TAG, "[Nightmode] NIGHT_MODE_ACTION_END");
                setNightMode();
            } else if (NightModeInfo.TIME_SET.equals(action)) {
                Log.d(TAG, "[Nightmode] TIME_SET");
                setNightMode();
                mNightmodeInfo.cancelPendingIntent(context);
                mNightmodeInfo.requestPendingIntent(context);
            } else if (NightModeInfo.TIMEZONE_CHANGED.equals(action)) {
                Log.d(TAG, "[Nightmode] TIMEZONE_CHANGED");
                setNightMode();
                mNightmodeInfo.cancelPendingIntent(context);
                mNightmodeInfo.requestPendingIntent(context);
            } else if (NightModeInfo.SET_NIGHT_MODE_NOTI_ACTION.equals(action)) {
                Log.d("jw", "SET_NIGHT_MODE_NOTI_ACTION !!!!");
                boolean isChecked = intent.getBooleanExtra(NightModeInfo.NIGHT_MODE_CECHKED, false);
                if (isChecked) {
                    Log.d("jw", "SET_NIGHT_MODE_NOTI_ACTION ON!");
                    mNightmodeInfo.startNotification();
                } else {
                    Log.d("jw", "SET_NIGHT_MODE_NOTI_ACTION OFF!");
                    mNightmodeInfo.endNotification();
                }
                if (mNightmodeInfo.getSettingsStyle() == 1
                        && mNightmodeInfo.isEasySetting()
                        && !mNightmodeInfo.isNightModeAble()) {
                    Log.d(TAG, "[Nightmode] No start Night mode");
                    mNightmodeInfo.setTabNightCheck(0);
                } else if (Utils.supportSplitView(mContext)
                        && !mNightmodeInfo.isNightModeAble()) {
                    mNightmodeInfo.setTabNightCheck(0);
                }
            }
        }
    }

    public void setNightMode() {
        int CheckSettingStyle = mNightmodeInfo.getSettingsStyle();
        if (CheckSettingStyle == 0
                && !Utils.supportSplitView(mContext)) {
            mNightmodeInfo.setNightNode();
        } else {
            if (mNightmodeInfo.isEasySetting()
                    || Utils.supportSplitView(mContext)) {
                mNightmodeInfo.setNightNodeTabSettings();
            } else {
                mNightmodeInfo.setNightNode();
            }

        }
    }

    private void receiverDisable(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, NightModeReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG, "[Nightmode] Use Auto Brightness model. Disable receiver");
    }
}
