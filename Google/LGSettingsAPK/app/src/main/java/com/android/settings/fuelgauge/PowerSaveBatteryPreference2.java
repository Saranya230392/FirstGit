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

package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryStats;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.android.settings.R;
import com.android.settings.Utils;

/**
 * Custom preference for displaying power consumption as a bar and an icon on the left for the
 * subsystem/app type.
 *
 */
public class PowerSaveBatteryPreference2 extends Preference {

    private static final String TAG = "PowerSaveBatteryPreference2";

    private BatteryStats mStats;
    private PowerSaveBatteryChart2 chart2;
    private HorizontalScrollView hScrollView;
    private ImageView imageView;

    public PowerSaveBatteryPreference2(Context context, BatteryStats stats) {
        super(context);
        setLayoutResource(R.layout.powersave_preference_battery_jb);

        mStats = stats;
    }

    BatteryStats getStats() {
        return mStats;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Log.i(TAG, "onBindView");

        hScrollView = (HorizontalScrollView)view.findViewById(R.id.horizontal_scroll_view);
        hScrollView.setFocusable(false);

        chart2 = (PowerSaveBatteryChart2)view.findViewById(
                R.id.battery_history_chart);
        chart2.setFocusable(false);
        chart2.setStats(mStats);

        imageView = (ImageView)view.findViewById(R.id.graph_dot);
        imageView.setFocusable(false);

        Intent intent = new Intent();
        intent.setAction(PowerSaveBatteryDetail.REFRESH_INTENT);
        getContext().sendBroadcast(intent);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public PowerSaveBatteryChart2 getChart() {
        return chart2;
    }

    public HorizontalScrollView getHorizontalScrollView() {
        return hScrollView;
    }
}
