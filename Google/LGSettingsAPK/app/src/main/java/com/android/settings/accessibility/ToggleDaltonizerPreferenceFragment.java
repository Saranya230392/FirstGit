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

package com.android.settings.accessibility;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.SettingsBreadCrumb;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.Switch;
import com.android.settings.R;
import android.util.Log;

public class ToggleDaltonizerPreferenceFragment extends ToggleFeaturePreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String ENABLED = Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED;
    private static final String TYPE = Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER;
    private static final int DEFAULT_TYPE = AccessibilityManager.DALTONIZER_CORRECT_DEUTERANOMALY;

    private ListPreference mType;
    private Switch mToggleSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.accessibility_daltonizer_settings);
        mType = (ListPreference)findPreference("type");
        initSwitch();
        initPreferences();
    }

    @Override
    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Settings.Secure.putInt(getContentResolver(), ENABLED, enabled ? 1 : 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mType) {
            Settings.Secure.putInt(getContentResolver(), TYPE, Integer.parseInt((String)newValue));
            preference.setSummary("%s");
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         setTitle(getString(R.string.accessibility_display_daltonizer_preference_title));
    }

    private void initPreferences() {
        final String value = Integer.toString(
                Settings.Secure.getInt(getContentResolver(), TYPE, DEFAULT_TYPE));
        mType.setValue(value);
        mType.setOnPreferenceChangeListener(this);
        final int index = mType.findIndexOfValue(value);
        if (index < 0) {
            // We're using a mode controlled by developer preferences.
            mType.setSummary(getString(R.string.daltonizer_type_overridden,
                    getString(R.string.simulate_color_space)));
        }
        getPreferenceScreen().setEnabled(mToggleSwitch.isChecked());
    }

    private void initSwitch() {
        mToggleSwitch = new Switch(getActivity());
        try {
            Activity activity = getActivity();
            if (null != activity) {
                activity = getActivity();
            }
            if (null != activity) {
                if (SettingsBreadCrumb.isAttached(getActivity())) {
                    mToggleSwitch.setPaddingRelative(0, 0, 0, 0);
                } else {
                    setSwitchPadding();
                }
            }
            else {
                Log.e(TAG, "activity is null!!!");
                setSwitchPadding();
            }
        } catch (ClassCastException e) {
            Log.i(TAG, e.getLocalizedMessage());
        }
        mToggleSwitch.setChecked(Settings.Secure.getInt(getContentResolver(), ENABLED, 0) == 1);
        mToggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            }
        });
        mToggleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                  Settings.Secure.putInt(getContentResolver(), ENABLED, mToggleSwitch.isChecked() ? 1 : 0);
                  getPreferenceScreen().setEnabled(mToggleSwitch.isChecked());
            }
        });
    }

    private void setSwitchPadding() {
        final int padding = getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mToggleSwitch.setPaddingRelative(0, 0, padding, 0);
        if (null != actionBar) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(mToggleSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
    }

}
