package com.android.settings;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.os.SystemProperties;

import com.lge.constants.SettingsConstants;

public class DataSleepTimeInfo {
    private Context context;

    private static final String TAG = "DataSleepTimeInfo";
    //SettingsConstants.MOBILE_DATA_SLEEP
    public static final String MOBILE_DATA_SLEEP = "mobile_data_sleep";
    //SettingsConstants.MOBILE_DATA_SLEEP_DAYS
    public static final String MOBILE_DATA_SLEEP_DAYS = "mobile_data_sleep_days";
    //SettingsConstants.MOBILE_DATA_SLEEP_START_TIME
    public static final String MOBILE_DATA_SLEEP_START_TIME = "mobile_data_sleep_start_time";
    //SettingsConstants.MOBILE_DATA_SLEEP_END_TIME
    public static final String MOBILE_DATA_SLEEP_END_TIME = "mobile_data_sleep_end_time";

    public static final int ON = 1;
    public static final int OFF = 0;
    public static final int DEFAULT_START_TIME = 00;
    public static final int DEFAULT_END_TIME = 07;
    public static final String DEFAULT_DATA_SLEEP_DAYS = "1111100";
    public static final boolean START_TIME = false;
    public static final boolean END_TIME = true;

    public int mDataSleepStartHour = DEFAULT_START_TIME;
    public int mDataSleepStartMin = 0;
    public int mDataSleepEndHour = DEFAULT_END_TIME;
    public int mDataSleepEndMin = 0;

    public DataSleepTimeInfo(Context _context) {
        context = _context;
        //setDBDataSleepEnabled(getDBDataSleepEnabled());
        initDataSleepTime();
    }

    // set/get data sleep enabled DB.
    public boolean isDataSleepEnabled() {
        return getDBDataSleepEnabled();
    }

    public void setDBDataSleepEnabled(boolean mValue) {
        SLog.e(TAG, "setDBDataSleepEnabled() = " + mValue);
        Settings.System.putInt(context.getContentResolver(), MOBILE_DATA_SLEEP, mValue ? ON : OFF);
    }

    private boolean getDBDataSleepEnabled() {
        int mValue;
        boolean mIsEnabled;
        try {
            mValue = Settings.System.getInt(context.getContentResolver(), MOBILE_DATA_SLEEP);
            mIsEnabled = mValue == ON ? true : false;
            SLog.e(TAG, "getDBDataSleepEnabled() = " + mIsEnabled);
            return mIsEnabled;
        } catch (SettingNotFoundException e) {
            SLog.e(TAG, "SettingNotFoundException - getDBDataSleepEnabled()");
            setDBDataSleepEnabled(true);
            return true;
        }
    }

    // set/get data sleep start time.
    public void setDBDataSleepStartTime(long startTime) {
        SLog.d(TAG, "setDBDataSleepStartTime() - start time string : " + startTime);
        Settings.System.putLong(context.getContentResolver(), MOBILE_DATA_SLEEP_START_TIME,
                startTime);
    }

    public long getDBDataSleepStartTime() {
        long mValue;
        try {
            mValue = getCalendarTimeMillis(Settings.System.getLong(context.getContentResolver(),
                    MOBILE_DATA_SLEEP_START_TIME));
            SLog.d(TAG, "getDBDataSleepStartTime() - start time : " + mValue);
            return mValue;
        } catch (SettingNotFoundException e) {
            SLog.d(TAG, "getDBDataSleepStartTime() - Default start time : "
                    + getTimeString(getDefaultDataSleepTime(false)));
            setDBDataSleepStartTime(getDefaultDataSleepTime(false));
            return getDefaultDataSleepTime(false);
        }
    }

    public void setDBDataSleepStartTimeHour(int hour) {
        mDataSleepStartHour = hour;
    }

    public int getDBDataSleepStartTimeHour() {
        return mDataSleepStartHour;
    }

    public void setDBDataSleepStartTimeMinute(int minute) {
        mDataSleepStartMin = minute;
    }

    public int getDBDataSleepStartTimeMinute() {
        return mDataSleepStartMin;
    }

    // set/get data sleep end time.
    public void setDBDataSleepEndTime(long endTime) {
        Settings.System.putLong(context.getContentResolver(), MOBILE_DATA_SLEEP_END_TIME, endTime);
        SLog.d(TAG, "setDBDataSleepEndTime() - End time : " + endTime);
    }

    public long getDBDataSleepEndTime() {
        long mValue;
        try {
            mValue = getCalendarTimeMillis(Settings.System.getLong(context.getContentResolver(),
                    MOBILE_DATA_SLEEP_END_TIME));
            SLog.d(TAG, "getDBDataSleepEndTime() - End time : " + mValue);
            return mValue;
        } catch (SettingNotFoundException e) {
            SLog.d(TAG, "getDBDataSleepStartTime() - Default end time : "
                    + getTimeString(getDefaultDataSleepTime(true)));
            setDBDataSleepEndTime(getDefaultDataSleepTime(true));
            return getDefaultDataSleepTime(true);
        }
    }

    public void setDBDataSleepEndTimeHour(int hour) {
        mDataSleepEndHour = hour;
    }

    public int getDBDataSleepEndTimeHour() {
        return mDataSleepEndHour;
    }

    public void setDBDataSleepEndTimeMinute(int minute) {
        mDataSleepEndMin = minute;
    }

    public int getDBDataSleepEndTimeMinute() {
        return mDataSleepEndMin;
    }

    // set/get data sleep days.
    public void setDBDataSleepDays(String days) {
        SLog.d(TAG, "setDBDataSleepDays() : " + days);
        Settings.System.putString(context.getContentResolver(), MOBILE_DATA_SLEEP_DAYS, days);
    }

    public String getDBDataSleepDays() {
        String days = Settings.System.getString(context.getContentResolver(),
                MOBILE_DATA_SLEEP_DAYS);
        if (days == null) {
            setDBDataSleepDays(DEFAULT_DATA_SLEEP_DAYS);
            return DEFAULT_DATA_SLEEP_DAYS;
        }
        SLog.d(TAG, "getDBDataSleepDays() : " + days);
        return days;
    }

    public void initDataSleepTime() {
        //Initializae the DataSleep db.
        getDBDataSleepEnabled();
        getDBDataSleepDays();

        //Initialize the DataSleep field.
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getDBDataSleepStartTime());
        setDBDataSleepStartTimeHour(c.getTime().getHours());
        setDBDataSleepStartTimeMinute(c.getTime().getMinutes());

        c.setTimeInMillis(getDBDataSleepEndTime());
        setDBDataSleepEndTimeHour(c.getTime().getHours());
        setDBDataSleepEndTimeMinute(c.getTime().getMinutes());
    }

    public long getCurrentTimeMillis() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return c.getTimeInMillis();
    }

    public long getCalendarTimeMillis(long time) {
        java.util.Calendar dummy = java.util.Calendar.getInstance();
        dummy.setTimeInMillis(time);

        int hourOfDay = dummy.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = dummy.get(java.util.Calendar.MINUTE);
        int seconds = dummy.get(java.util.Calendar.SECOND);

        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(java.util.Calendar.AM_PM, 0);
        c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        return c.getTimeInMillis();
    }

    public long getBeforeDay(long time) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(time);
        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        c.add(java.util.Calendar.DAY_OF_YEAR, -1);
        return c.getTimeInMillis();
    }

    public long getAfterDay(long time) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(time);
        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        c.add(java.util.Calendar.DAY_OF_YEAR, +1);
        return c.getTimeInMillis();
    }

    public String getTimeString(Long time) {
        return DateFormat.getTimeFormat(context).format(time);
    }

    public long getDefaultDataSleepTime(boolean isAmPm) {
        long mValue;
        java.util.Calendar c = java.util.Calendar.getInstance();
        Date date = new Date();
        if (isAmPm == false) {
            date.setHours(DEFAULT_START_TIME);
            date.setMinutes(00);
            date.setSeconds(00);
        }
        else {
            date.setHours(DEFAULT_END_TIME);
            date.setMinutes(00);
            date.setSeconds(00);
        }
        c.setTime(date);
        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        c.add(java.util.Calendar.DAY_OF_YEAR, +1);
        mValue = c.getTimeInMillis();
        SLog.i("getDefaultDataSleepTime() mValue = " + mValue);
        return mValue;
    }

    public long getTime(boolean start_end) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        Date date = new Date();
        if (start_end == START_TIME) {
            date.setHours(getDBDataSleepStartTimeHour());
            date.setMinutes(getDBDataSleepStartTimeMinute());
            date.setSeconds(00);
        }
        else {
            date.setHours(getDBDataSleepEndTimeHour());
            date.setMinutes(getDBDataSleepEndTimeMinute());
            date.setSeconds(00);
        }

        c.setTime(date);
        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        c.add(java.util.Calendar.DAY_OF_YEAR, +1);
        return c.getTimeInMillis();
    }

    public void setTime(boolean start_end) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        Date date = new Date();
        if (start_end == START_TIME) {
            date.setHours(getDBDataSleepStartTimeHour());
            date.setMinutes(getDBDataSleepStartTimeMinute());
            date.setSeconds(00);
        }
        else {
            date.setHours(getDBDataSleepEndTimeHour());
            date.setMinutes(getDBDataSleepEndTimeMinute());
            date.setSeconds(00);
        }

        c.setTime(date);
        //int hourOfDay = c.get(java.util.Calendar.HOUR);
        //int minute = c.get(java.util.Calendar.MINUTE);
        //int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR_OF_DAY, date.getHours());
        c.set(java.util.Calendar.MINUTE, date.getMinutes());
        c.set(java.util.Calendar.SECOND, date.getSeconds());
        c.add(java.util.Calendar.DAY_OF_YEAR, +1);

        if (start_end == START_TIME) {
            setDBDataSleepStartTime(c.getTimeInMillis());
        } else {
            setDBDataSleepEndTime(c.getTimeInMillis());
        }
    }

    public String getStringTimeInfo(long time) {
        Date date = new Date(time);
        int hours = date.getHours();
        int monute = date.getMinutes();
        StringBuffer sb = new StringBuffer();

        Log.i(TAG, "hours : " + hours);
        Log.i(TAG, "minutes : " + monute);

        sb.append(String.format("%02d", hours));
        sb.append(String.format("%02d", monute));
        return sb.toString();
    }

    public String getStringDayInfo(long time) {
        final int defaultYear = 1900;
        final int defaultMonth = 1;

        Date date = new Date(time);
        int year = date.getYear() + defaultYear;
        int month = date.getMonth() + defaultMonth;
        int day = date.getDate();
        int hours = date.getHours();
        int monute = date.getMinutes();
        StringBuffer sb = new StringBuffer();

        sb.append(year);
        sb.append(String.format("%02d", month));
        sb.append(String.format("%02d", day));
        sb.append(String.format("%02d", hours));
        sb.append(String.format("%02d", monute));
        return sb.toString();
    }

    public boolean isNorepeatCase() {
        return getDBDataSleepDays().equals("0000000");
    }

    public boolean isNorepeatEndTimeCase() {
        boolean timedelay = false;
        long current = getCurrentTimeMillis();
        long end;
        long result;

        end = getDBDataSleepEndTime();
        result = current - end;
        SLog.d(TAG, "[NR_isNorepeatEndTimeCase] EndTime- Cur : " + result);
        if (result < 2000 && result > -2000) {
            timedelay = true;
        }
        return timedelay;
    }

    public boolean isNorepeatStartTimeCase() {
        boolean timedelay = true;
        long current = getCurrentTimeMillis();
        long start;
        long result;

        start = getDBDataSleepStartTime();
        result = current - start;
        SLog.d(TAG, "[NR_isNorepeatStartTimeCase] Cur - StartTime : " + result);
        if (result > 1000) {
            timedelay = false;
        }
        return timedelay;
    }

    public boolean isSilentDay() {
        //SLog.d(TAG, "dummy : " + mDays);
        int[] checkDay = getToggleDayToArray();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        //SLog.d(TAG, "dayOfWeek : " + dayOfWeek);

        if (dayOfWeek < Time.TUESDAY) {
            dayOfWeek = Time.SATURDAY;
        } else {
            dayOfWeek = dayOfWeek - Time.TUESDAY;
        }

        long current = getCurrentTimeMillis();
        if (getDBDataSleepStartTime() > getDBDataSleepEndTime()
                && getDBDataSleepEndTime() > current && getDBDataSleepStartTime() > current) {
            if (true == isSilentDayForDaybreakCase()) {
                return true;
            }
        }

        if (checkDay[dayOfWeek] == 1) {
            return true;
        }
        if (isNorepeatCase() == true)
            return true;

        return false;
    }

    public boolean isSilentDayForDaybreakCase() {
        //SLog.d(TAG, "dummy : " + mDays);
        int[] checkDay = getToggleDayToArray();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        //SLog.d(TAG, "dayOfWeek : " + dayOfWeek);

        if (dayOfWeek < Time.TUESDAY) {
            dayOfWeek = Time.SATURDAY;
        } else {
            dayOfWeek = dayOfWeek - Time.TUESDAY;
        }

        if (isNorepeatCase() == true)
            return true;

        if (1 <= dayOfWeek && dayOfWeek < 7) {
            if (checkDay[dayOfWeek - 1] == 0) {
                return false;
            }
        } else if (dayOfWeek == 0) {
            if (checkDay[6] == 0) {
                return false;
            }
        }

        return true;
    }

    public int[] getToggleDayToArray() {
        int[] tmp = { 1, 1, 1, 1, 1, 1, 1 };

        try {
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = Integer.parseInt(getDBDataSleepDays().substring(i, i + 1));
            }
        } catch (StringIndexOutOfBoundsException e) {
            int[] exceptionResult = { 1, 1, 1, 1, 1, 1, 1 };
            return exceptionResult;
        }
        return tmp;
    }

    public String set24TimeString(boolean isStartTime, String timeString) {
        int hour = 0;
        int minute = 0;
        String result = "";
        if (DateFormat.is24HourFormat(context)) {
            if (true == isStartTime) {
                hour = getDBDataSleepStartTimeHour();
                minute = getDBDataSleepStartTimeMinute();
                if (java.util.Locale.getDefault().getLanguage().equals("ar")) {
                    result = String.format("%02d", minute) + " : " + String.format("%02d", hour);
                } else {
                    result = String.format("%02d", hour) + " : " + String.format("%02d", minute);
                }
            } else {
                hour = getDBDataSleepEndTimeHour();
                minute = getDBDataSleepEndTimeMinute();
                if (java.util.Locale.getDefault().getLanguage().equals("ar")) {
                    result = String.format("%02d", minute) + " : " + String.format("%02d", hour);
                } else {
                    result = String.format("%02d", hour) + " : " + String.format("%02d", minute);
                }
            }
            return result;
        }
        else {
            return timeString;
        }
    }

    public void reSetDataSleep() {
        setDBDataSleepEnabled(true);
        setDBDataSleepStartTime(getDefaultDataSleepTime(false));
        setDBDataSleepEndTime(getDefaultDataSleepTime(true));
        setDBDataSleepDays(DEFAULT_DATA_SLEEP_DAYS);
    }

}
