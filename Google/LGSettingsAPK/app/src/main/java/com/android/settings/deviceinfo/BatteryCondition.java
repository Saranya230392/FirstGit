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

package com.android.settings.deviceinfo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.android.settings.R;

import android.util.Log;
import android.widget.TextView;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;
import com.lge.constants.SettingsConstants;
import android.app.ActionBar;
import android.view.MenuItem;
import android.provider.Settings;

public class BatteryCondition extends PreferenceActivity {

    private static final String LOG_TAG = "BatteryCondition";

    private CheckBoxPreference mbattery_condition;
    private static final String KEY_BATTERY_CONDITION = "battery_condition_alarm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int condition = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.BATTERY_CONDITION, 1);

        addPreferencesFromResource(R.xml.device_info_battery_condition);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switch (condition)
        {
        case 0:
        case 1:
            removePreferenceFromScreen("battery_good");
            removePreferenceFromScreen("battery_bad");
            break;

        case 2:
            removePreferenceFromScreen("battery_verygood");
            removePreferenceFromScreen("battery_bad");
            break;

        case 3:
            removePreferenceFromScreen("battery_verygood");
            removePreferenceFromScreen("battery_good");
            break;
        default:
            break;
        }

        mbattery_condition = (CheckBoxPreference)findPreference(KEY_BATTERY_CONDITION);
        mbattery_condition.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.ALARM_BATTERY_CONDITION, 1) != 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        Log.d("starmotor", "itemId 11= " + itemId);
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mbattery_condition) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.ALARM_BATTERY_CONDITION,
                    mbattery_condition.isChecked() ? 1 : 0);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
    * Removes the specified preference, if it exists.
    * @param key the key for the Preference item
    */
    //[S][12.05.09][never1029]WBT
    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        //[2012.08.24][munjohn.kang] added a Null pointer exception handling
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        }
        else
        {
            if (pref == null) {
                Log.i(LOG_TAG,
                        "removePreferenceFromScreen() removePreferenceFromScreen pref == null");
            }
            else {
                Log.i(LOG_TAG, "removePreferenceFromScreen() getPreferenceScreen() == null");
            }
        }
    }
    //[E][12.05.09][never1029]WBT
}
