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

import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.content.IntentFilter;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.RcseSettingsInfo;

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
public class RcseSettingsReceiverEx extends BroadcastReceiver {

    private static String PREFERENCE_NAME = "gsma.joyn.preferences";
    //private static String PREFERENCE_NAME_TWO = "com.android.settings.gsma.joyn.preferences";
    private static String KEY_NAME = "gsma.joyn.enabled";
    private SharedPreferences sp = null;
    private static String TAG = "RcsesettingsEx";
    private static final String ACTION_JOYN_CLIENT_CHECK =
            "com.lge.settings.ACTION_JOYN_CLIENT_CHECK";
    private static final String ACTION_JOYN_REQUEST_RCS_ACTIVATE =
            "com.lge.ims.rcsservice.action.REQUEST_RCS_ACTIVATE";
    private static final String ACTION_JOYN_RCSPREF_REMOVE =
            "com.lge.ims.rcsstarter.action.RCSPREF_REMOVE";

    @SuppressLint({ "WorldWriteableFiles", "WorldReadableFiles" })
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "RcseSettingsReceiverEx");
        Context mcontext = context;
        String action = intent.getAction();
        if (action.equals(ACTION_JOYN_CLIENT_CHECK)) {
            boolean mEnableStatus = false;
            mEnableStatus = RcseSettingsInfo.checkMultiClientEnabled(mcontext);
            if (mEnableStatus) {
                Intent i = new Intent("com.lge.settings.RcsBBSettingsAlertActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mcontext.startActivity(i);
            } else {
                if ((Settings.System.getString(context.getContentResolver(), "rcs_working") != null)
                        && Settings.System.getString(context.getContentResolver(), "rcs_working")
                                .equals("1")) {
                } else {
                    RcseSettingsInfo.checkValueChangedandBroadcast(mcontext, "service_onoff", true);
                }
            }
        } else if (action.equals(ACTION_JOYN_REQUEST_RCS_ACTIVATE)) {
            Log.d(TAG, "received com.lge.ims.rcsservice.action.REQUEST_RCS_ACTIVATE");
            boolean mEnableStatus = false;
            mEnableStatus = RcseSettingsInfo.checkMultiClientEnabled(mcontext);
            if (!mEnableStatus) {
                RcseSettingsInfo.checkValueChangedandBroadcast(mcontext, "service_onoff", true);
            }
        } else if (action.equals(ACTION_JOYN_RCSPREF_REMOVE)) {
            Log.d(TAG, "received com.lge.ims.rcsstarter.action.RCSPREF_REMOVE");
            String sJOYN_PREFERENCE = "gsma.joyn.preferences";
            String sJOYN_PREFERENCE_VALUE = "com.android.settings.gsma.joyn.preferences";
            String sDOWNLOAD_APP_ENABLED = "gsma.joyn.enabled";

            SharedPreferences prefs = mcontext.getSharedPreferences(sJOYN_PREFERENCE,
                    Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                            | Context.MODE_MULTI_PROCESS);
            if (prefs != null) {
                prefs.edit().remove(sDOWNLOAD_APP_ENABLED).commit();
            }

            SharedPreferences rcsePrefs = mcontext.getSharedPreferences(sJOYN_PREFERENCE_VALUE,
                    Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                            | Context.MODE_MULTI_PROCESS);
            if (rcsePrefs != null) {
                rcsePrefs.edit().remove(sDOWNLOAD_APP_ENABLED).commit();
            }
        } else {
            Editor editor = null;
            Boolean RcseStatus = false;
            RcseStatus = intent.getIntExtra("gsma.joyn.enabled", 0) == 1 ? true : false;
            Log.d(TAG, "RcseStatus = " + RcseStatus);
            sp = null;
            sp = mcontext.getApplicationContext().getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_WORLD_WRITEABLE |
                            Context.MODE_WORLD_READABLE |
                            Context.MODE_MULTI_PROCESS);
            editor = sp.edit().putBoolean(KEY_NAME, RcseStatus);
            Log.d(TAG, "editor = " + editor);
            editor.commit();
        }

        RcseSettingsInfo.setRCSSupportStatus(mcontext, true);
    }
}
