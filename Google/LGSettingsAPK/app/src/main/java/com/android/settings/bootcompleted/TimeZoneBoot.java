package com.android.settings.bootcompleted;

import android.app.AlarmManager;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;

public class TimeZoneBoot {
    private static final String TAG = "SettingBootReceiver";

    public static void onReceive(Context context) {

        final boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;

        if (true == isSecondaryUser) {
            return; // kerry - temp code for a_wifi timing issue requested by chanho.pi.
        }

        String timezoneID = System.getString(context.getContentResolver(), "time_zone_id" /* KLP SettingsConstants.System.TIME_ZONE_ID */);
        if (!TextUtils.isEmpty(timezoneID)) {
            final AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarm.setTimeZone(timezoneID);
            //[essin@lge.com][2012-07-24]Only first boot setting for default time zone
            System.putString(context.getContentResolver(), "time_zone_id" /* KLP SettingsConstants.System.TIME_ZONE_ID */, "");
            Log.d(TAG, "[timezoneID] : " + timezoneID);
        } else {
            Log.d(TAG, "timezoneID is null");
            return;
        }
    }
}
