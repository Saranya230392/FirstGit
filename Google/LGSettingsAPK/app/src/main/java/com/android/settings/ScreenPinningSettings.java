/*
 * Copyright (C) 2014 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import android.widget.Toast;


/**
 * Screen pinning settings.
 */
public class ScreenPinningSettings extends SettingsPreferenceFragment implements OnClickListener,
        OnCheckedChangeListener {

    private Switch mSwitch;
    SettingsBreadCrumb mBreadCrumb;
    static final String TAG = "ScreenPinningSettings";
    private static final String KDDI_SCREEN_PINNING_CONDITION
                         = "com.kddi.agent.action.SCREEN_PINNING_CONDITION";
    private static final String KDDI_SCREEN_PINNING_EXDATA = "status";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(isLockToAppEnabled());
        Log.d(TAG, "isLockToAppEnabled : " + isLockToAppEnabled());
        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_pinning_instructions, null);
    }

    private boolean isLockToAppEnabled() {
        try {
            return Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCK_TO_APP_ENABLED) != 0;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void setLockToAppEnabled(boolean isEnabled) {
        Settings.System.putInt(getContentResolver(), Settings.System.LOCK_TO_APP_ENABLED,
                isEnabled ? 1 : 0);
    }

    /**
     * Listens to the state change of the lock-to-app master switch.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        if (Settings.Secure.getInt(getContentResolver(),
            SettingsConstants.Secure.ACCESSIBILITY_TOUCH_CONTROL_AREAS_ENABLE, 0) == 1) {
                if (isChecked) {
                        setLockToAppEnabled(false);
                        mSwitch.setChecked(false);
                        Toast.makeText(getActivity(), R.string.turn_off_touch_control_area, Toast.LENGTH_SHORT).show();
                        return;
                }
        }
        setLockToAppEnabled(isChecked);
        mSwitch.setChecked(isChecked);

        // KDDI SCREEN PINNING Broadcast Intent 20150125 sungsuplus.kim
        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            Intent intent = new Intent(KDDI_SCREEN_PINNING_CONDITION);
            intent.putExtra(KDDI_SCREEN_PINNING_EXDATA, isChecked);
            getActivity().sendBroadcast(intent);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

}
