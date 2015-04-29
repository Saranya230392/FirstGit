/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.os.BatteryStatsImpl;
import com.android.settings.R;

public class BatteryHistoryDetail extends Fragment {
    public static final String EXTRA_STATS = "stats";

    private BatteryStatsImpl mStats;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // [S][2012.01.06][hyoungjun21.lee@lge.com][Common] Fix WBT issues
        try {
            byte[] data = getArguments().getByteArray(EXTRA_STATS);
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
        } catch (NullPointerException npe) {
        }
        // [E][2012.01.06][hyoungjun21.lee@lge.com][Common] Fix WBT issues
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preference_batteryhistory, null);
        BatteryHistoryChart chart = (BatteryHistoryChart)view
                .findViewById(R.id.battery_history_chart);
        chart.setStats(mStats);
        return view;
    }
}
