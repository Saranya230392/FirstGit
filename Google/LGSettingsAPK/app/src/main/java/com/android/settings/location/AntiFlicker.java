/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.location;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

public class AntiFlicker extends SettingsPreferenceFragment
        implements RadioButtonPreference.OnClickListener {
    private static final String KEY_AUTO = "auto";
    private RadioButtonPreference mAuto;
    private static final String KEY_50HZ = "m50hz";
    private RadioButtonPreference m50Hz;
    private static final String KEY_60HZ = "m60hz";
    private RadioButtonPreference m60Hz;

    private static final String TAG = "AntiFlicker";

    // Add switch for tablet. [S]
    SettingsBreadCrumb mBreadCrumb;
    // Add switch for tablet. [E]




    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        int mode = 0;
        super.onResume();
        try {
            mode = Settings.System.getInt(getActivity().getContentResolver(), "anti_flicker_on");
        } catch (SettingNotFoundException snfe) {
        }
       
        createPreferenceHierarchy();
        onModeChanged(mode);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.anti_flicker);
        root = getPreferenceScreen();

        mAuto = (RadioButtonPreference) root.findPreference(KEY_AUTO);
        m50Hz = (RadioButtonPreference) root.findPreference(KEY_50HZ);
        m60Hz = (RadioButtonPreference) root.findPreference(KEY_60HZ);
        mAuto.setOnClickListener(this);
        m50Hz.setOnClickListener(this);
        m60Hz.setOnClickListener(this);

        return root;
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
            mAuto.setChecked(false);
            m50Hz.setChecked(false);
            m60Hz.setChecked(false);
        } else if (activated == mAuto) {
            mAuto.setChecked(true);
            m50Hz.setChecked(false);
            m60Hz.setChecked(false);
        } else if (activated == m50Hz) {
            mAuto.setChecked(false);
            m50Hz.setChecked(true);
            m60Hz.setChecked(false);
        } else if (activated == m60Hz) {
            mAuto.setChecked(false);
            m50Hz.setChecked(false);
            m60Hz.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        int mode = 0;
        if (emiter == mAuto) {
            mode = 0;
            Settings.System.putInt(getActivity().getContentResolver(), "anti_flicker_on", 0);
            Intent mIntent = new Intent();
            mIntent.setAction("android.wifi.FLICKER_MODE_AUTO_SELECT");
            getActivity().sendBroadcast(mIntent);
        } else if (emiter == m50Hz) {
            mode = 1;
            Settings.System.putInt(getActivity().getContentResolver(), "anti_flicker_on", 1);
//            Settings.System.putString(getActivity().getContentResolver(), "wifi_country_mcc", "235");
            SystemProperties.set("persist.sys.wificountrymcc", "235");
        } else if (emiter == m60Hz) {
            mode = 2;
            Settings.System.putInt(getActivity().getContentResolver(), "anti_flicker_on", 2);
//            Settings.System.putString(getActivity().getContentResolver(), "wifi_country_mcc", "310");
            SystemProperties.set("persist.sys.wificountrymcc", "310");
        }
        onModeChanged(mode);
    }

    public void onModeChanged(int mode) {
        Log.d(TAG, "onModeChanged mode = " + mode);
        switch (mode) {
            case 0:
                updateRadioButtons(mAuto);
                break;
            case 1:
                updateRadioButtons(m50Hz);
                break;
            case 2:
                updateRadioButtons(m60Hz);
                break;
            default:
                break;
        }
    }
}
