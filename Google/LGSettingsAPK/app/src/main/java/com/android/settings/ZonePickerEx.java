/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

//import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.ZonePicker;

import java.util.Date;

/**
 * Receives android.intent.action.EVENT_REMINDER intents and handles
 * event reminders.  The intent URI specifies an alert id in the
 * CalendarAlerts database table.  This class also receives the
 * BOOT_COMPLETED intent so that it can add a status bar notification
 * if there are Calendar event alarms that have not been dismissed.
 * It also receives the TIME_CHANGED action so that it can fire off
 * snoozed alarms that have become ready.  The real work is done in
 * the QuietModeSettingsService class.
 *
 * To trigger this code after pushing the apk to device:
 * adb shell am broadcast -a "android.intent.action.EVENT_REMINDER"
 *    -n "com.android.calendar/.alerts.AlertReceiver"
 */
public class ZonePickerEx extends BroadcastReceiver {
    private static final String TAG = "ZonePickerEx";
    private NotificationManager mNotificationManager;
    private String mAction = null;
    private Context mContext;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        mContext = context;

        mAction = intent.getAction();

        Log.d("starmotor", "ZonePicker mAction : " + mAction);

        if ("com.lge.intent.action.LGE_CAMPED_MCC_CHANGE".equals(mAction)) {
            if (mNotificationManager == null) {
                mNotificationManager =
                        (NotificationManager)mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);
            }
            mNotificationManager.cancel(ZonePicker.NOTIFICATION_ID);
        }

        if ("com.lge.intent.action.MULTI_TIMEZONE_CT".equals(mAction)) { //CT Multi timezone
            if (mNotificationManager == null) {
                mNotificationManager =
                        (NotificationManager)mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);
            }
            onCTnotification();
        }

        if ("com.lge.intent.action.SINGLE_TIMEZONE_CT".equals(mAction)) {
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-413]
        if ("com.lge.mdm.intent.action.ACTION_DEVELOPER_MODE_POLICY_CHANGE".equals(mAction)) { // developer list removed

            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                Log.d(TAG, "com.lge.mdm.LGMDMManager.getInstance().getAllowDeveloperMode(null) :"
                        + com.lge.mdm.LGMDMManager.getInstance().getAllowDeveloperMode(null));
                if (com.lge.mdm.LGMDMManager.getInstance().getAllowDeveloperMode(null) == false) {
                    mContext.getSharedPreferences(DevelopmentSettings.PREF_FILE,
                            Context.MODE_PRIVATE).edit().putBoolean(
                            DevelopmentSettings.PREF_HIDE, true).apply();
                } else {
                    mContext.getSharedPreferences(DevelopmentSettings.PREF_FILE,
                            Context.MODE_PRIVATE).edit().putBoolean(
                            DevelopmentSettings.PREF_HIDE, false).apply();
                }
                updateSharedPreferenceForEasySettings();
            }
        }
        // LGMDM_END
    }

    private void updateSharedPreferenceForEasySettings() {
        Context easyContext = null;
        try {
            easyContext = mContext.createPackageContext("com.lge.settings.easy",
                    Context.CONTEXT_IGNORE_SECURITY);
            if (com.lge.mdm.LGMDMManager.getInstance().getAllowDeveloperMode(null) == false) {
                easyContext
                        .getSharedPreferences(
                                "easy_development",
                                Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                                        | Context.MODE_MULTI_PROCESS)
                        .edit().putBoolean(DevelopmentSettings.PREF_HIDE, true).apply();
            } else {
                easyContext
                        .getSharedPreferences(
                                "easy_development",
                                Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                                        | Context.MODE_MULTI_PROCESS)
                        .edit().putBoolean(DevelopmentSettings.PREF_HIDE, false).apply();
            }
        } catch (NameNotFoundException e) {
            Log.d(TAG, "NameNotFoundException");
        }
    }

    private void onCTnotification() {
        SLog.d(TAG, "onPause()");

        //        final Activity mActivity = getActivity();
        final int mNOTIFICATION_ID = R.drawable.detail;

        //String text = "";
        String title = mContext.getString(R.string.sp_time_settings_for_roaming);

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        mNotificationManager = (NotificationManager)mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification mNotification = new Notification();

        mNotification.icon = R.drawable.detail;
        mNotification.flags = mNotification.flags | Notification.FLAG_AUTO_CANCEL;
        mNotification.tickerText = mContext.getString(R.string.sp_time_settings_for_roaming);
        //        text = mActivity.getString(R.string.sp_default_timezone_notification_summary);
        mNotification.setLatestEventInfo(mContext, title, null, contentIntent);

        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(mNOTIFICATION_ID, mNotification);
    }
}
