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

package com.android.settings.powersave;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lge.constants.SettingsConstants;
import android.os.SystemProperties;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import android.provider.Settings;

/**
 * Custom preference for displaying power consumption as a bar and an icon on the left for the
 * subsystem/app type.
 *
 */
public class PowerSaveBatteryInfoPreference extends Preference {

    private static final String TAG = "PowerSaveBatteryInfoPreference";

    private Context mContext;

    private TextView mBatteryLevel;
    private TextView mConditionTitle;
    private TextView mConditionSummary;
    private ImageView mSmartImage;

    public PowerSaveBatteryInfoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.power_preference_battery_info);

        mContext = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mBatteryLevel = (TextView)view.findViewById(R.id.batteryLevel);
        mConditionTitle = (TextView)view.findViewById(R.id.conditionTitle);
        mConditionSummary = (TextView)view.findViewById(R.id.conditionSummary);
        mSmartImage = (ImageView)view.findViewById(R.id.smarticon);
        mSmartImage.setImageResource(R.drawable.img_homescreen_battery_light);
        if (BatterySettings.getSmartChargeOn()) {
            setSmartViable();
        } else {
            setSmartInViable();
        }
        mBatteryLevel.setText(BatterySettings.getBatterySummary());

        if (!("true".equals(Utils.isEmbededBattery(mContext)) && "DCM".equals(Config
                .getOperator()))) {
            mConditionTitle.setVisibility(View.GONE);
            mConditionSummary.setVisibility(View.GONE);
        } else {
            mConditionTitle.setText(mContext.getResources().getString(
                    R.string.sp_battery_condition_NORMAL));
            mConditionSummary.setText(batteryCondition());
        }

    }

    public void setSmartViable() {
        Log.d(TAG, "setSmartViable");
        mSmartImage.setVisibility(View.VISIBLE);
    }

    public void setSmartInViable() {
        Log.d(TAG, "setSmartInViable");
        mSmartImage.setVisibility(View.INVISIBLE);
    }

    private String batteryCondition() {
        int condition = Settings.System.getInt(mContext.getContentResolver(),
                "KLP" /* SettingsConstants.System.BATTERY_CONDITION */, 1);
        String mCondition = "";

        switch (condition) {
        case 0:
        case 1:
            mCondition = mContext.getResources().getString(R.string.sp_battery_condition_b_NORMAL);
            break;

        case 2:
            mCondition = mContext.getResources().getString(R.string.sp_battery_condition_g_NORMAL);
            break;

        case 3:
            mCondition = mContext.getResources().getString(R.string.sp_battery_condition_ba_NORMAL);
            break;

        default:
            break;
        }

        return mCondition;
    }
}
