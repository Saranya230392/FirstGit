package com.android.settings.lge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.settings.lge.DeviceInfoLgeBadge;

public class DeviceInfoLgeBadgeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d("starmotor", "action = " + action);

        if ("com.lge.launcher.intent.action.BADGE_COUNT_UPDATE".equals(action)) {
            if (intent.getStringExtra("badge_count_class_name").equals(
                    "com.lge.updatecenter.UpdateCenterPrfActivity")) {
                int badgeCount = intent.getIntExtra("badge_count", 0);
                DeviceInfoLgeBadge.appUpdatesBadgeCount = badgeCount;
            }
        }
    }
}
