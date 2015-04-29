/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View.OnClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import android.text.Html;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DataSleepPreferenceActivity extends PreferenceActivity {
    private static final String TAG = "DataSleepPreferenceActivity";
    public static final int ON = 1;
    public static final int OFF = 0;

    private static final String KEY_DATA_SLEEP_TIME = "data_sleep_time";
    private static final String KEY_DATA_SLEEP_DAY = "data_sleep_day";
    private static Configuration config;
    private DataSleepTimePreference mDataSleepTimePreference;
    private DataSleepDayPreference mDataSleepDayPreference;
    private DataSleepInfo mDataSleepInfo;
    private Switch mDataSleepEnabledSwitch;
    private boolean mIsDataSleepEnabledChecked = false;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ContentResolver resolver = getContentResolver();
        mContext = this.getApplicationContext();
        addPreferencesFromResource(R.xml.data_sleep_preference_activity);

        mDataSleepTimePreference = (DataSleepTimePreference)findPreference(KEY_DATA_SLEEP_TIME);
        mDataSleepDayPreference = (DataSleepDayPreference)findPreference(KEY_DATA_SLEEP_DAY);
        mDataSleepInfo = new DataSleepInfo(mContext);
        //mDataSleepInfo.initDataSleepTime();
        mDataSleepEnabledSwitch = new Switch(this);
        mIsDataSleepEnabledChecked = mDataSleepInfo.isDataSleepEnabled();
        menuSetEnabled(mIsDataSleepEnabledChecked);
        final int padding = getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
        mDataSleepEnabledSwitch.setPaddingRelative(0, 0, padding, 0);

        mDataSleepEnabledSwitch.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_UP: {
                    mIsDataSleepEnabledChecked = !mDataSleepEnabledSwitch.isChecked();
                    SLog.i("mIsDataSleepEnabledChecked = " + mIsDataSleepEnabledChecked);
                    mDataSleepInfo.setDBDataSleepEnabled(mIsDataSleepEnabledChecked);
                    menuSetEnabled(mIsDataSleepEnabledChecked);

                    v.performClick();

                    mDataSleepTimePreference.save();
                    if (null != mDataSleepDayPreference) {
                        mDataSleepDayPreference.save();
                    }
                    return true;
                }
                default:
                    break;
                }
                return false;
            }
        });

        mDataSleepEnabledSwitch.setChecked(mIsDataSleepEnabledChecked);
        mDataSleepEnabledSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onResume() {
        mDataSleepTimePreference.onResume(mDataSleepInfo);
        if (null != mDataSleepDayPreference) {
            mDataSleepDayPreference.onResume(mDataSleepInfo);
        }
        mDataSleepEnabledSwitch.setChecked(mDataSleepInfo.isDataSleepEnabled());
        mIsDataSleepEnabledChecked = mDataSleepInfo.isDataSleepEnabled();
        mDataSleepEnabledSwitch.setChecked(mIsDataSleepEnabledChecked);
        menuSetEnabled(mDataSleepEnabledSwitch.isChecked());
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        save();
        //mDataSleepDayPreference.save();
        menuSetEnabled(mDataSleepEnabledSwitch.isChecked());

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();

        mDataSleepTimePreference.onPause();
        if (null != mDataSleepDayPreference) {
            mDataSleepDayPreference.onPause();
        }
        //save();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);

        getActionBar().setCustomView(mDataSleepEnabledSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(null);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void save() {
        mDataSleepTimePreference.save();
        if (null != mDataSleepDayPreference) {
            mDataSleepDayPreference.save();
        }
        //mDataSleepInfo.updateTimeSettingForSchedule();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void menuSetEnabled(boolean _state) {
        mDataSleepTimePreference.setEnabled(_state);
        if (null != mDataSleepDayPreference) {
            mDataSleepDayPreference.setEnabled(_state);
        }
    }

}
