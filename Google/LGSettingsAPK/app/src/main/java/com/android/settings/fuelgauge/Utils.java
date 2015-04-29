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

import android.content.Context;

import com.android.settings.R;
import java.util.Locale;

/**
 * Contains utility functions for formatting elapsed time and consumed bytes
 */
public class Utils {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    //private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    /**
     * Formats data size in KB, MB, from the given bytes.
     * @param context the application context
     * @param bytes data size in bytes
     * @return the formatted size such as 4.52 MB or 245 KB or 332 bytes
     */
    public static String formatBytes(Context context, double bytes) {
        // TODO: I18N
        if (bytes > 1000 * 1000) {
            return String.format("%.2f MB", ((int)(bytes / 1000)) / 1000f);
        } else if (bytes > 1024) {
            return String.format("%.2f KB", ((int)(bytes / 10)) / 100f);
        } else {
            return String.format("%d bytes", (int)bytes);
        }
    }

    public static String formatOnBatteryTime(Context context, double millis) {
        StringBuilder sb = new StringBuilder();
        int seconds = (int)Math.floor(millis / 1000);

        int hours = 0;
        int minutes = 0;
        //        if (seconds > SECONDS_PER_DAY) {
        //            days = seconds / SECONDS_PER_DAY;
        //            seconds -= days * SECONDS_PER_DAY;
        //        }
        if (seconds > SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds > SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }

        if (hours > 0) {
            if (hours > 1) {
                sb.append(String.format(Locale.getDefault(), "%d", hours)
                        + " "
                        + context.getResources().getString(
                                R.string.sp_powersave_battery_use_hours_text_NORMAL));
            } else {
                sb.append(String.format(Locale.getDefault(), "%d", hours)
                        + " "
                        + context.getResources().getString(
                                R.string.sp_powersave_battery_use_hour_text_NORMAL));
            }
        }

        if (minutes == 1) {
            sb.append(" "
                    + String.format(Locale.getDefault(), "%d", minutes)
                    + " "
                    + context.getResources().getString(
                            R.string.sp_powersave_battery_use_minute_text_NORMAL));
        } else {
            sb.append(" "
                    + String.format(Locale.getDefault(), "%d", minutes)
                    + " "
                    + context.getResources().getString(
                            R.string.sp_powersave_battery_use_minutes_text_NORMAL));
        }

        return sb.toString();
    }
}
