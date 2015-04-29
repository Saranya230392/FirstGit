/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;

//[2012.05.25][Jaeyoon.hyun] Add sui start
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.DoubleTitleListPreference;
//[2012.05.25][Jaeyoon.hyun] Add sui End
//import com.android.settings.lgesetting.Config.Config;

import java.util.Calendar;

public class TimeSettingsForRoaming extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private DoubleTitleListPreference mDisPlayModeList;
    private Preference mRecommendation;
    private static final String SELECT_DISPLAY_MODE = "displaymode";
    private static final String SELECT_RECOMMENDATION = "recommendation";

    private String[] mValues;
    private String[] mDisPlay;
    private String mDisPlayMode = null;
    private final Calendar now = Calendar.getInstance();

    private BroadcastReceiver mRecommendationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.lge.intent.action.SINGLE_TIMEZONE_CT")) {
                mRecommendation.setEnabled(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.time_settings_for_roaming);
        mDisPlayModeList = (DoubleTitleListPreference)findPreference(SELECT_DISPLAY_MODE);
        mRecommendation = (Preference)findPreference(SELECT_RECOMMENDATION);
        Log.d("starmotor", "now.getTimeZone() : " + now.getTimeZone());
        /*
        if ("CN".equals(Config.getCountry())) {
            mRecommendation.setEnabled(false);
        } else {
            mRecommendation.setEnabled(true);
        }
        */
        if ("Asia/Shanghai".equals(now.getTimeZone().getID())) {
            mDisPlayModeList.setEnabled(false);
        } else {
            mDisPlayModeList.setEnabled(true);
        }
        DispalyValue();
    }

    private void DispalyValue() {
        if (mDisPlayModeList != null) {
            mDisPlayModeList.setMainTitle(getString(R.string.sp_time_display_mode));
            mDisPlayModeList.setSubTitle(getString(R.string.sp_time_display_mode_detail));
            String mCurrentMode = getDisplayMode();
            mValues = getResources().getStringArray(R.array.time_display_mode_values);
            mDisPlay = getResources().getStringArray(R.array.time_display_mode);
            int index = 0;
            for (int i = 0; i < mValues.length; i++) {
                if (mCurrentMode.equals(mValues[i])) {
                    index = i;
                    break;
                }
            }
            mDisPlayModeList.setValue(mValues[index]);
            mDisPlayModeList.setSummary(mDisPlay[index]);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(SELECT_DISPLAY_MODE) && (getActivity() != null)) {
            Log.d("starmotor", "mDisPlayMode2 : " + mDisPlayMode);
            if ("1".equals(mDisPlayMode)) {
                Settings.System.putString(getContentResolver(), "display_mode", "0");
                setAndBroadcastNetworkSetTimeZone(getActivity(), "Asia/Shanghai");
            } else {
                Settings.System.putString(getContentResolver(), "display_mode", "1");
                setAndBroadcastNetworkSetTimeZone(getActivity(), now.getTimeZone().getID());
            }
            DispalyValue();
        }
    }

    private void setAndBroadcastNetworkSetTimeZone(Context context, String zoneId) {
        Log.d("starmotor", "context : " + context);
        Log.d("starmotor", "zoneId : " + zoneId);
        if (context != null) {
            AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarm.setTimeZone(zoneId);
            Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIMEZONE);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("time-zone", zoneId);
            context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private String getDisplayMode() {
        mDisPlayMode = Settings.System.getString(getContentResolver(), "display_mode");
        Log.d("starmotor", "mDisPlayMode3 : " + mDisPlayMode);
        Log.d("starmotor", "now.getTimeZone().getID()) : " + now.getTimeZone().getID());
        if ("Asia/Shanghai".equals(now.getTimeZone().getID())) { // || mDisPlayMode.equals("0")) {
            mDisPlayMode = "0";
            Settings.System.putString(getContentResolver(), "display_mode", "0");
        } else {
            mDisPlayMode = "1";
            Settings.System.putString(getContentResolver(), "display_mode", "1");
        }
        /*
        if (mDisPlayMode == null) {
            mDisPlayMode = "1";
        }
        */
        Log.d("starmotor", "mDisPlayMode1 : " + mDisPlayMode);
        return mDisPlayMode;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mRecommendationReceiver,
                new IntentFilter("com.lge.intent.action.SINGLE_TIMEZONE_CT"));

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

}
