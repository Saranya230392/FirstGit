package com.android.settings;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.android.settings.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.text.format.DateFormat;
import android.text.format.Time;

import android.util.Log;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

public class DataSleepInfo extends DataSleepTimeInfo {

    private Context mContext;
    private ContentResolver mContentResolver;

    public static final int ON = 1;
    public static final int OFF = 0;
    public static final int DEFAULT_EMPTY = -1;
    public static final int VIBRATE_TIME = 200;
    public static final int BOOT_QUIETMODE_ON_CASE = 2;

    public static final int MON = 0;
    public static final int TUE = 1;
    public static final int WED = 2;
    public static final int THU = 3;
    public static final int FRI = 4;
    public static final int SAT = 5;
    public static final int SUN = 6;

    public static final int MON_IW = 1;
    public static final int TUE_IW = 2;
    public static final int WED_IW = 3;
    public static final int THU_IW = 4;
    public static final int FRI_IW = 5;
    public static final int SAT_IW = 6;
    public static final int SUN_IW = 0;

    private static final int REMOVE_COMMA = -1;
    public static final int SHIFT_DAY = -1;
    private static final int ONE_DAY_SELECT = 1;

    public static final int UID = 0;
    public static final int END = 2;
    public static final int PKG = 3;
    public static final int EMP = -1;

    public static final long ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long REMOVE_SECOND = 10000;
    public static final boolean START_TIME = false;
    public static final boolean END_TIME = true;
    public static final String DEFAULT_DAY = "1111100";

    static final String START_TIME_BTN = "start_time_btn";
    static final String END_TIME_BTN = "end_time_btn";
    private String mPosition = START_TIME_BTN;

    private static final String TAG = "DataSleepInfo";

    public DataSleepInfo(Context _context) {
        super(_context);
        mContext = _context;
        mContentResolver = mContext.getContentResolver();
    }

    public void setTime(boolean start_end) {
        Date date;
        if (start_end == START_TIME) {
            date = new Date(getDBDataSleepStartTime());
            date.setHours(getDBDataSleepStartTimeHour());
            date.setMinutes(getDBDataSleepStartTimeMinute());
            date.setSeconds(00);
        }
        else {
            date = new Date(getDBDataSleepEndTime());
            date.setHours(getDBDataSleepEndTimeHour());
            date.setMinutes(getDBDataSleepEndTimeMinute());
            date.setSeconds(00);
        }

        if (start_end == START_TIME) {
            setDBDataSleepStartTime(date.getTime());
        }
        else {
            setDBDataSleepEndTime(date.getTime());
        }
    }

    public boolean isScheduledTime() {
        return isSilentTime();
    }

    public long getCurrentTimeMillis() {
        long time = new Date().getTime();
        time = time / REMOVE_SECOND;
        time = time * REMOVE_SECOND;
        return time;
    }

    public boolean isSilentTime() {
        // TODO Auto-generated method stub
        long current = getDayInfo(getCurrentTimeMillis());
        long start = getDayInfo(getDBDataSleepStartTime());
        long end = getDayInfo(getDBDataSleepEndTime());

        SLog.i(TAG, "[isSilentTime] current : " + getDayInfo(getCurrentTimeMillis()));
        SLog.i(TAG, "[isSilentTime] start : " + getDayInfo(getDBDataSleepStartTime()));
        SLog.i(TAG, "[isSilentTime] end : " + getDayInfo(getDBDataSleepEndTime()));

        if (start <= current && end > current) {
            SLog.i(TAG, "[isSilentTime] true case");
            return true;
        }
        return false;
    }

    public long getDayInfo(long time) {
        String dayInfo = getStringDayInfo(time);
        return Long.parseLong(dayInfo);
    }

    public long removeSecond(Long time) {
        time = time / REMOVE_SECOND;
        time = time * REMOVE_SECOND;
        return time;
    }

    public boolean currentOnCase(long currentTime, long startTime, long endTime) {
        int start = Integer.parseInt(getStringTimeInfo(startTime));
        int end = Integer.parseInt(getStringTimeInfo(endTime));
        int current = Integer.parseInt(getStringTimeInfo(currentTime));

        if (current < start && current < end && start > end) {
            return true;
        }
        return false;
    }

    public void updateDBCurrentOnCase() {
        Date sDate = new Date(getCurrentTimeMillis());
        Date eDate = new Date(getCurrentTimeMillis());

        //start time
        sDate.setHours(getDBDataSleepStartTimeHour());
        sDate.setMinutes(getDBDataSleepStartTimeMinute());
        sDate.setSeconds(0);

        //end time
        eDate.setHours(getDBDataSleepEndTimeHour());
        eDate.setMinutes(getDBDataSleepEndTimeMinute());
        eDate.setSeconds(0);

        if (sDate.getTime() > eDate.getTime() && eDate.getTime() > getCurrentTimeMillis()) {
            setDBDataSleepStartTime(removeSecond(sDate.getTime() - ONE_DAY));
            setDBDataSleepEndTime(removeSecond(eDate.getTime()));

        }
    }

    public boolean beforeDayRunCase() {

        long start = getDayInfo(getDBDataSleepStartTime()) / REMOVE_SECOND;
        long end = getDayInfo(getDBDataSleepEndTime()) / REMOVE_SECOND;
        long current = getDayInfo(getCurrentTimeMillis()) / REMOVE_SECOND;

        if (current > start && start < end && current == end) {
            return true;
        }
        return false;
    }

    public boolean isSilentDay() {
        //SLog.i(TAG, "dummy : " + mDays);
        int[] checkDay = getToggleDayToArray();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        if (dayOfWeek < Time.TUESDAY) {
            dayOfWeek = Time.SATURDAY;
        } else {
            dayOfWeek = dayOfWeek - Time.TUESDAY;
        }

        if (true == beforeDayRunCase()) {
            if (true == isSilentDayForDaybreakCase()) {
                return true;
            }
            else {
                return false;
            }
        }

        if (currentOnCase(getCurrentTimeMillis(), getDBDataSleepStartTime(),
                getDBDataSleepEndTime())) {
            if (true == isSilentDayForDaybreakCase()) {
                return true;
            }
        }

        if (checkDay[dayOfWeek] == ON) {
            return true;
        }
        if (isNorepeatCase() == true) {
            return true;
        }

        return false;
    }

    private void registerLog(long currentTime, long startTime, long endTime) {
        SLog.i(TAG, "--------------------------------------------------");
        SLog.i(TAG, "start time : " + startTime);
        SLog.i(TAG, "End time : " + endTime);
        SLog.i(TAG, "current time : " + currentTime);
        SLog.i(TAG, "=======================================");
        SLog.i(TAG, "start time string : " + getStringDayInfo(startTime));
        SLog.i(TAG, "end time string : " + getStringDayInfo(endTime));
        SLog.i(TAG, "current time string : " + getStringDayInfo(currentTime));
        SLog.i(TAG, "--------------------------------------------------");
    }

    public boolean updateTimeSettingForSchedule() {
        updateDBCurrentOnCase();
        SLog.i(TAG, "[updateTimeSettingForSchedule] start time : "
                + getStringDayInfo(getDBDataSleepStartTime()));
        SLog.i(TAG, "[updateTimeSettingForSchedule] end time : "
                + getStringDayInfo(getDBDataSleepEndTime()));
        return true;
    }

    public void updateTime(int hourOfDay, int minute, String mPosition) {
        Calendar calendar = Calendar.getInstance();
        Date mDate;
        long removeSecond;
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.HOUR, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));

        if (mPosition.equals(START_TIME_BTN)) {
            SLog.i(TAG, "Start hour : " + getDBDataSleepStartTimeHour());
            SLog.i(TAG, "Start minite : " + getDBDataSleepStartTimeMinute());

            removeSecond = calendar.getTimeInMillis() / REMOVE_SECOND;
            removeSecond = removeSecond * REMOVE_SECOND;
            setDBDataSleepStartTime(removeSecond);

            // end time set
            calendar.set(Calendar.AM_PM, 0);
            calendar.set(Calendar.HOUR, getDBDataSleepEndTimeHour());
            calendar.set(Calendar.MINUTE, getDBDataSleepEndTimeMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                    calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));

            removeSecond = calendar.getTimeInMillis() / REMOVE_SECOND;
            removeSecond = removeSecond * REMOVE_SECOND;
            setDBDataSleepEndTime(removeSecond);
        }
        else if (mPosition.equals(END_TIME_BTN)) {
            SLog.i(TAG, "End hour : " + getDBDataSleepEndTimeHour());
            SLog.i(TAG, "End minite : " + getDBDataSleepEndTimeMinute());

            removeSecond = calendar.getTimeInMillis() / REMOVE_SECOND;
            removeSecond = removeSecond * REMOVE_SECOND;
            setDBDataSleepEndTime(removeSecond);
            SLog.i(TAG, "End time : " + getDBDataSleepEndTime());

            // start time set
            calendar.set(Calendar.AM_PM, 0);
            calendar.set(Calendar.HOUR, getDBDataSleepStartTimeHour());
            calendar.set(Calendar.MINUTE, getDBDataSleepStartTimeMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                    calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            removeSecond = calendar.getTimeInMillis() / REMOVE_SECOND;
            removeSecond = removeSecond * REMOVE_SECOND;
            setDBDataSleepStartTime(removeSecond);
        }

        mDate = new Date(getDBDataSleepEndTime());
        SLog.i(TAG, "before date : " + mDate.getTime());
        SLog.i(TAG, "Start time : " + getDBDataSleepStartTime());
        SLog.i(TAG, "End time : " + getDBDataSleepEndTime());

        if (getDBDataSleepEndTime() < getDBDataSleepStartTime()) {
            SLog.i(TAG, "after day case ");
            mDate.setTime(mDate.getTime() + DataSleepInfo.ONE_DAY);
            setDBDataSleepEndTime(mDate.getTime());
        }
        SLog.i(TAG, "after Start time : " + getDBDataSleepStartTime() +
                "                   " +
                getDayInfo(getDBDataSleepStartTime()));
        SLog.i(TAG, "after End time : " + getDBDataSleepEndTime() +
                "                   " +
                getDayInfo(getDBDataSleepEndTime()));

        SLog.i(TAG, "after end full string : " + mDate.toGMTString());
        mDate.setTime(getDBDataSleepStartTime());
        SLog.i(TAG, "after start full string : " + mDate.toGMTString());
        mDate = null;
    }

    public String getDataSleepOnSummary(Activity activity) {
        if (null == activity) {
            return " ";
        }
        if (java.util.Locale.getDefault().getLanguage().equals("fa")
                && !(DateFormat.is24HourFormat(mContext))) {
            return set24TimeString(false, getTimeString(getDBDataSleepEndTime()))
                    + " " + activity.getString(R.string.sp_tilde_NORMAL)
                    + " " + set24TimeString(true, getTimeString(getDBDataSleepStartTime()));
        } else {
            return set24TimeString(true, getTimeString(getDBDataSleepStartTime()))
                    + " " + activity.getString(R.string.sp_tilde_NORMAL)
                    + " " + set24TimeString(false, getTimeString(getDBDataSleepEndTime()));
        }
    }

    public String getDataSleepOnSummary(Activity activity, String selectDays) {
        if (null == activity) {
            return " ";
        }
        if (selectDays.equals(activity.getString(R.string.sp_no_repeat_SHORT))) {
            if (java.util.Locale.getDefault().getLanguage().equals("fa")
                    && !(DateFormat.is24HourFormat(mContext))) {
                return selectDays + "\n"
                        + set24TimeString(false, getTimeString(getDBDataSleepEndTime()))
                        + " " + activity.getString(R.string.sp_tilde_NORMAL)
                        + " " + set24TimeString(true, getTimeString(getDBDataSleepStartTime()));
            } else {
                return selectDays + "\n"
                        + set24TimeString(true, getTimeString(getDBDataSleepStartTime()))
                        + " " + activity.getString(R.string.sp_tilde_NORMAL)
                        + " " + set24TimeString(false, getTimeString(getDBDataSleepEndTime()));
            }

        } else {
            if (java.util.Locale.getDefault().getLanguage().equals("fa")
                    && !(DateFormat.is24HourFormat(mContext))) {
                return selectDays + "\n"
                        + set24TimeString(false, getTimeString(getDBDataSleepEndTime()))
                        + " " + activity.getString(R.string.sp_tilde_NORMAL)
                        + " " + set24TimeString(true, getTimeString(getDBDataSleepStartTime()));
            } else {
                return selectDays + "\n"
                        + set24TimeString(true, getTimeString(getDBDataSleepStartTime()))
                        + " " + activity.getString(R.string.sp_tilde_NORMAL)
                        + " " + set24TimeString(false, getTimeString(getDBDataSleepEndTime()));
            }
        }
    }

    public String getDataSleepSummaryText(Activity activity, String days) {
        if (activity == null) {
            return " ";
        }
        Locale clsLocale = activity.getResources().getConfiguration().locale;
        SLog.i(TAG, "language info : " + clsLocale.getLanguage());
        if ("iw".equals(clsLocale.getLanguage())) {
            return getSummaryIW(activity, days);
        }
        else {
            return getSummaryNormal(activity, days);
        }
    }

    private String getSummaryIW(Activity activity, String days) {
        // parsing days for iw
        StringBuilder iwdays = new StringBuilder();
        iwdays.append(days.substring(days.length() + SHIFT_DAY, days.length()));
        iwdays.append(days.substring(0, days.length() + SHIFT_DAY));
        SLog.i(TAG, "days value : " + days);
        SLog.i(TAG, "s value : " + iwdays.toString());
        days = iwdays.toString();

        String summary = "empty";
        if (days.equals("1111111")) {
            return activity.getString(R.string.sp_every_day_SHORT);
        } else if (days.equals("1111110")) {
            return summary = activity.getString(R.string.sp_sun_SHORT)
                    + " - " + activity.getString(R.string.sp_fri_SHORT);
        } else if (days.equals("1111100")) {
            return summary = activity.getString(R.string.sp_sun_SHORT)
                    + " - " + activity.getString(R.string.sp_thu_SHORT);
        } else if (days.equals("0111111")) {
            return summary = activity.getString(R.string.sp_mon_SHORT)
                    + " - " + activity.getString(R.string.sp_sat_SHORT);
        } else if (days.equals("0011111")) {
            return summary = activity.getString(R.string.sp_tue_SHORT)
                    + " - " + activity.getString(R.string.sp_sat_SHORT);
        } else if (days.equals("0111110")) {
            return summary = activity.getString(R.string.sp_mon_SHORT)
                    + " - " + activity.getString(R.string.sp_fri_SHORT);
        } else if (days.equals("0000000")) {
            return activity.getString(R.string.sp_no_repeat_SHORT);
        } else {
            StringBuilder s = new StringBuilder();
            int count = 0;
            int dummyDays = 0;
            for (int i = 0; i < Time.WEEK_DAY; i++) {
                String c = String.valueOf(days.charAt(i));
                if (c.equals("1")) {
                    count++;
                    dummyDays = i;

                    if (i == SUN_IW) {
                        s.append(activity.getString(R.string.sp_sun_SHORT));
                    } else if (i == MON_IW) {
                        s.append(activity.getString(R.string.sp_mon_SHORT));
                    } else if (i == TUE_IW) {
                        s.append(activity.getString(R.string.sp_tue_SHORT));
                    } else if (i == WED_IW) {
                        s.append(activity.getString(R.string.sp_wed_SHORT));
                    } else if (i == THU_IW) {
                        s.append(activity.getString(R.string.sp_thu_SHORT));
                    } else if (i == FRI_IW) {
                        s.append(activity.getString(R.string.sp_fri_SHORT));
                    } else if (i == SAT_IW) {
                        s.append(activity.getString(R.string.sp_sat_SHORT));
                    }
                    s.append(",");
                }
            }
            if (count == ONE_DAY_SELECT) {
                if (dummyDays == SUN_IW) {
                    return activity.getString(R.string.sp_sunday_SHORT);
                } else if (dummyDays == MON_IW) {
                    return activity.getString(R.string.sp_monday_SHORT);
                } else if (dummyDays == TUE_IW) {
                    return activity.getString(R.string.sp_tuesday_SHORT);
                } else if (dummyDays == WED_IW) {
                    return activity.getString(R.string.sp_wednesday_SHORT);
                } else if (dummyDays == THU_IW) {
                    return activity.getString(R.string.sp_thursday_SHORT);
                } else if (dummyDays == FRI_IW) {
                    return activity.getString(R.string.sp_friday_SHORT);
                } else if (dummyDays == SAT_IW) {
                    return activity.getString(R.string.sp_saturday_SHORT);
                }

            } else {
                return s.substring(0, s.length() + REMOVE_COMMA);
            }
            return s.substring(0, s.length() + REMOVE_COMMA);
        }
    }

    private String getSummaryNormal(Activity activity, String days) {

        if (days.equals("1111111")) {
            return activity.getString(R.string.sp_every_day_SHORT);
        } else if (days.equals("1111110")) {
            return activity.getString(R.string.sp_FromMonToSat_SHORT);
        } else if (days.equals("1111100")) {
            return activity.getString(R.string.sp_FromMonToFri_SHORT);
        } else if (days.equals("0111111")) {
            return activity.getString(R.string.sp_FromTueToSun_SHORT);
        } else if (days.equals("0011111")) {
            String summary = activity.getString(R.string.sp_wed_SHORT)
                    + " - " + activity.getString(R.string.sp_sun_SHORT);
            return summary;
        } else if (days.equals("0111110")) {
            return activity.getString(R.string.sp_FromTueToSat_SHORT);
        } else if (days.equals("0000000")) {
            return activity.getString(R.string.sp_no_repeat_SHORT);
        } else {
            StringBuilder s = new StringBuilder();
            int count = 0;
            int dummyDays = 0;
            for (int i = 0; i < Time.WEEK_DAY; i++) {
                String c = String.valueOf(days.charAt(i));
                if (c.equals("1")) {
                    count++;
                    dummyDays = i;

                    if (i == MON) {
                        s.append(activity.getString(R.string.sp_mon_SHORT));
                    } else if (i == TUE) {
                        s.append(activity.getString(R.string.sp_tue_SHORT));
                    } else if (i == WED) {
                        s.append(activity.getString(R.string.sp_wed_SHORT));
                    } else if (i == THU) {
                        s.append(activity.getString(R.string.sp_thu_SHORT));
                    } else if (i == FRI) {
                        s.append(activity.getString(R.string.sp_fri_SHORT));
                    } else if (i == SAT) {
                        s.append(activity.getString(R.string.sp_sat_SHORT));
                    } else if (i == SUN) {
                        s.append(activity.getString(R.string.sp_sun_SHORT));
                    }
                    s.append(",");
                }
            }
            if (count == ONE_DAY_SELECT) {
                switch (dummyDays) {
                case MON: // mon
                    return activity.getString(R.string.sp_monday_SHORT);
                case TUE: // tue
                    return activity.getString(R.string.sp_tuesday_SHORT);
                case WED: // wed
                    return activity.getString(R.string.sp_wednesday_SHORT);
                case THU: // thu
                    return activity.getString(R.string.sp_thursday_SHORT);
                case FRI: // fri
                    return activity.getString(R.string.sp_friday_SHORT);
                case SAT: // sat
                    return activity.getString(R.string.sp_saturday_SHORT);
                case SUN: // sun
                    return activity.getString(R.string.sp_sunday_SHORT);
                default:
                    return s.substring(0, s.length() + REMOVE_COMMA);
                }
            } else {
                return s.substring(0, s.length() + REMOVE_COMMA);
            }
        }
    }

    private void setOriginalAutoReplyOption() {
    }

    public void deleteSelectedContactsAllowed() {
    }

    public void updateScheduledTimeInfo() {
        Date date = new Date();
        date.setHours(getDBDataSleepStartTimeHour());
        date.setMinutes(getDBDataSleepStartTimeMinute());
        long start_time = date.getTime();
        start_time = start_time / DataSleepInfo.REMOVE_SECOND;
        start_time = start_time * DataSleepInfo.REMOVE_SECOND;

        date.setHours(getDBDataSleepEndTimeHour());
        date.setMinutes(getDBDataSleepEndTimeMinute());
        long end_time = date.getTime();
        end_time = end_time / DataSleepInfo.REMOVE_SECOND;
        end_time = end_time * DataSleepInfo.REMOVE_SECOND;

        if (start_time > end_time) {
            end_time = end_time + DataSleepInfo.ONE_DAY;
        }

        setDBDataSleepStartTime(start_time);
        setDBDataSleepEndTime(end_time);
    }

    private static final int DEFAULT_START_HOUR = 22;
    private static final int DEFAULT_END_HOUR = 06;

    public void reSetQuieMode() {
        // Quiet time option reset
    }

}
