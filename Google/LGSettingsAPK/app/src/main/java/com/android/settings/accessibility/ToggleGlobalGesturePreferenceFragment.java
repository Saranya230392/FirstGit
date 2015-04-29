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

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.Utils;
import android.view.View;
import android.widget.Switch;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import com.android.settings.SettingsBreadCrumb;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;

public class ToggleGlobalGesturePreferenceFragment
        extends ToggleFeaturePreferenceFragment {
    
    private static final String TAG = "ToggleGlobalGesturePreferenceFragment";
    private Switch mToggleSwitch;
    private static boolean sisAppliedFunctionIcon = true;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSwitch();
    }

    @Override
    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, mToggleSwitch.isChecked() ? 1 : 0);
    }

    protected void initSwitch() {
        mToggleSwitch = new Switch(getActivity());
        mToggleSwitch.setChecked(
                Settings.Global.getInt(getContentResolver(),
                        Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
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

        mToggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            }
        });

        mToggleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, mToggleSwitch.isChecked() ? 1 : 0);
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
    // [S][2013.2.28][namchul.ha@lge.com][common] add Accessibility function Icon
    @Override
    public void onResume() {
        //mSettingsContentObserver.register(getContentResolver());
        super.onResume();
        sisAppliedFunctionIcon = Utils.supportFunctionIcon();
        if (sisAppliedFunctionIcon) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_accessibility);
        }
    }
    // [E][2013.2.28][namchul.ha@lge.com][common] add Accessibility function Icon
}
