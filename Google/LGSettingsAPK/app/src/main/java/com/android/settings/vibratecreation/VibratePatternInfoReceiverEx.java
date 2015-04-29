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

package com.android.settings.vibratecreation;

import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.settings.Utils;

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

public class VibratePatternInfoReceiverEx extends BroadcastReceiver {
    private static final String TAG = "VibratePatternInfoReceiverEx";

    private final static String CONFIGURATION_CHANGED = Intent.ACTION_LOCALE_CHANGED;
    private final static String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    private String mAction = null;
    private Context mContext;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        mContext = context;

        mAction = intent.getAction();
        if (CONFIGURATION_CHANGED.equals(mAction) ||
                BOOT_COMPLETED.equals(mAction)) {
            Log.d(TAG, "CONFIGURATION_CHANGED");
            VibratePatternInfo mVibratePatternInfo = new VibratePatternInfo(mContext, 0);
            mVibratePatternInfo.getDBMyVibrationCount();
        }
    }
}
