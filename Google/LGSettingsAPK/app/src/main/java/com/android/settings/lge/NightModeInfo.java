package com.android.settings.lge;

import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.SeekBar;

import com.android.settings.DisplaySettings;
import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

public class NightModeInfo {
    public final static String NIGHT_MODE_ACTION_START = "com.lge.settings.NIGHT_MODE_START";
    public final static String NIGHT_MODE_ACTION_END = "com.lge.settings.NIGHT_MODE_END";
    public final static String TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";
    public final static String TIME_SET = "com.lge.settings.TIME_SET";
    public final static String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    public final static int END_REQUEST_CODE = 20001;
    public final static int START_REQUEST_CODE = 20000;
    public static final long ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long SIX_HOUR = 60000 * 60 * 6;
    public Context mContext;

    public int mScreenBrightnessMinimum = 0;

    private static String TAG = "NightModeInfo";
    //[NightMode] Notification
    private Notification mNotification;
    private NotificationManager mNotificationManager = null;

    public static final int NOTIFICATION_ID = R.drawable.notify_nightmode_vzw;
    public static final String SET_NIGHT_MODE_NOTI_ACTION = "com.lge.settings.SET_NIGHT_MODE";
    public static final String NIGHT_MODE_CECHKED = "night_check";

    public NightModeInfo(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
    }

    public void requestPendingIntent(Context context) {
        Log.d(TAG, "[Nightmode] requestPendingIntent");

        setAlarmStart(context);
        setAlarmEnd(context);
        Log.i(TAG, "getStartTime" + getStringDayInfo(getStartTime()));
        Log.i(TAG, "getEndTime" + getStringDayInfo(getStartTime() + SIX_HOUR));
    }

    public void setAlarmStart(Context context) {

        Log.d(TAG, "[Nightmode] setAlarmStart~!!~!!~!!");
        Intent startIntent = new Intent();
        startIntent.setAction(NIGHT_MODE_ACTION_START);
        PendingIntent startSender = PendingIntent.getBroadcast(context, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmStart.setExact(AlarmManager.RTC_WAKEUP, getStartTime(), startSender);
        Log.i(TAG, "JW getStartTime" + getStringDayInfo(getStartTime()));

    }

    public void setAlarmEnd(Context context) {
        Log.d(TAG, "[Nightmode] setAlarmEnd~!!~!!~!!");
        Intent endIntent = new Intent();
        endIntent.setAction(NIGHT_MODE_ACTION_END);

        PendingIntent endSender = PendingIntent.getBroadcast(context, END_REQUEST_CODE, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmEnd = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        //AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmEnd.setExact(AlarmManager.RTC_WAKEUP, getStartTime() + SIX_HOUR, endSender);

        Log.i(TAG, "JW getEndTime" + getStringDayInfo(getStartTime() + SIX_HOUR));

    }

    public void setAlarmNextStart(Context context) {

        Log.d(TAG, "[Nightmode] setAlarmStart~!!~!!~!!");
        Intent startIntent = new Intent();
        startIntent.setAction(NIGHT_MODE_ACTION_START);
        PendingIntent startSender = PendingIntent.getBroadcast(context, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmStart.setExact(AlarmManager.RTC_WAKEUP, getStartTime() + ONE_DAY, startSender);
        Log.i(TAG, "JW NEXT getStartTime" + getStringDayInfo(getStartTime() + ONE_DAY));

    }

    public void setAlarmNextEnd(Context context) {
        Log.d(TAG, "[Nightmode] setAlarmEnd~!!~!!~!!");
        Intent endIntent = new Intent();
        endIntent.setAction(NIGHT_MODE_ACTION_END);

        PendingIntent endSender = PendingIntent.getBroadcast(context, END_REQUEST_CODE, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmEnd = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        //AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmEnd.setExact(AlarmManager.RTC_WAKEUP, getStartTime() + SIX_HOUR, endSender);
        Log.i(TAG, "JW NEXT getEndTime" + getStringDayInfo(getStartTime() + SIX_HOUR));

    }

    public void cancelAlarmStart(Context context) {
        Log.d(TAG, "[Nightmode] JW !! cancelAlarmStart");

        Intent startIntent = new Intent();
        startIntent.setAction(NIGHT_MODE_ACTION_START);

        PendingIntent startSender = PendingIntent.getBroadcast(context, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmStart.cancel(startSender);
        startSender.cancel();

    }

    public void cancelAlarmEnd(Context context) {

        Log.d(TAG, "[Nightmode] JW !! cancelAlarmEnd");

        Intent endIntent = new Intent();
        endIntent.setAction(NIGHT_MODE_ACTION_END);

        PendingIntent endSender = PendingIntent.getBroadcast(context, END_REQUEST_CODE, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmEnd = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmEnd.cancel(endSender);
        endSender.cancel();
    }

    /*
    public void requestStartPendingIntent(Context context) {
        Log.d(TAG, "[requestStartPendingIntent] requestPendingIntent");
        Intent startIntent = new Intent();
        startIntent.setAction(NIGHT_MODE_ACTION_START);
        PendingIntent startSender = PendingIntent.getBroadcast(context, START_REQUEST_CODE, startIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmStart.set(AlarmManager.RTC_WAKEUP, getStartTime() + ONE_DAY, startSender);
    }

    public void requestEndPendingIntent(Context context) {
        Log.d(TAG, "[requestEndPendingIntent] requestPendingIntent");
        Intent endIntent = new Intent();
        endIntent.setAction(NIGHT_MODE_ACTION_END);
        PendingIntent endSender = PendingIntent.getBroadcast(context, END_REQUEST_CODE, endIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmEnd = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmEnd.set(AlarmManager.RTC_WAKEUP, getEndTime() + ONE_DAY, endSender);
    }*/

    public void setRepeating(AlarmManager alarmManager, int type, long triggerAtMillis,
            long intervalMillis, PendingIntent operation) {
        alarmManager.set(type, triggerAtMillis, AlarmManager.WINDOW_EXACT, intervalMillis,
                operation, null);
    }

    public void cancelPendingIntent(Context context) {
        Log.d(TAG, "[Nightmode] cancelPendingIntent");

        Intent endIntent = new Intent();
        endIntent.setAction(NIGHT_MODE_ACTION_END);

        PendingIntent endSender = PendingIntent.getBroadcast(context, END_REQUEST_CODE, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmEnd = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent startIntent = new Intent();

        startIntent.setAction(NIGHT_MODE_ACTION_START);

        PendingIntent startSender = PendingIntent.getBroadcast(context, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmStart = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmStart.cancel(startSender);
        alarmEnd.cancel(endSender);
        startSender.cancel();
        endSender.cancel();
    }

    public void saveOldBrightness(int mode) {
        Log.d(TAG, "[Nightmode] saveOldBrightness()");
        Log.d(TAG, "JW Save Old Brightness :  " + mode);
        Settings.System.putInt(mContext.getContentResolver(),
                "save_old_brightness", mode);
    }

    public int getOldBrightness() {
        Log.d(TAG,
                "[Nightmode] getOldBrightness : "
                        + Settings.System.getInt(mContext.getContentResolver(),
                                "save_old_brightness", getCurrentBrightness()));
        return Settings.System.getInt(mContext.getContentResolver(),
                "save_old_brightness", getCurrentBrightness());
    }

    public void setBrightness(int brightness, boolean write) {
        Log.d(TAG, "[Nightmode] setBrightness : " + brightness);

        if (write) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }

    public void setRestoreBrightness(boolean write) {
        Log.d(TAG, "[Nightmode] setRestoreBrightness");
        setBrightness(getOldBrightness(), write);
    }

    public int getCurrentBrightness() {
        Log.d(TAG, "[Nightmode] getCurrentBrightness");
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 100);
    }

    public boolean isNightModeAble() {
        int checkDB = getNightCheckDB();

        if (checkDB == 1 && isNightModeTime()) {
            return true;
        }
        return false;
    }

    public void preViewNightModeAble(boolean checked, SeekBar seekbar, int oldBrightness) {
        if (checked && isNightModeTime()) {
            seekbar.setProgress(0);
            setBrightness(mScreenBrightnessMinimum, true);
            setNightDB(1);
            setNightModeEnabled(1);
            Log.d(TAG, "[Nightmode] preview set night mode");
        }
        else if (!checked && getNightModeEnabled() == 1) {
            seekbar.setProgress(getOldBrightness() - mScreenBrightnessMinimum);
            setBrightness(getOldBrightness(), true);
            setNightDB(0);
            setNightModeEnabled(0);
            Log.d(TAG, "[Nightmode] preview resotre temp old brightness : " + getOldBrightness());
        }
        else if (!checked && isNightModeTime()) {
            seekbar.setProgress(getTempOldBrightness() - mScreenBrightnessMinimum);
            setBrightness(getTempOldBrightness(), true);
            setNightDB(0);
            setNightModeEnabled(0);
            Log.d(TAG, "[Nightmode] preview resotre temp old brightness : "
                    + getTempOldBrightness());
        }
    }

    private long getStartTime() {
        int currentHour = getHour(getCurrentTimeMillis());
        Date date = new Date(getCurrentTimeMillis());
        date.setHours(00);
        date.setMinutes(00);
        date.setSeconds(00);

        long milisecond = date.getTime();
        if (currentHour >= 6) {
            Log.d(TAG, "[Nightmode] currentHour > 6");
            return milisecond + ONE_DAY;
        } else {
            Log.d(TAG, "[Nightmode] !!!Not currentHour > 6");
            return milisecond;
        }
    }

    private long getEndTime() {
        int currentHour = getHour(getCurrentTimeMillis());
        Date date = new Date(getCurrentTimeMillis());
        date.setHours(06);
        date.setMinutes(00);
        date.setSeconds(00);

        long milisecond = date.getTime();
        if (currentHour > 6) {
            milisecond = +ONE_DAY;
        } else {

        }
        Log.i(TAG, "End time : " + getStringDayInfo(milisecond));
        return milisecond;
    }

    public String getStringDayInfo(long time) {
        Date date = new Date(time);
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
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

    public long getCurrentTimeMillis() {
        long time = new Date().getTime();
        time = time / 10000;
        time = time * 10000;
        return time;
    }

    public int getHour(long time) {
        Date date = new Date(time);
        return date.getHours();
    }

    public boolean isNightModeTime() {
        int checkHour = getHour(getCurrentTimeMillis());
        if (checkHour >= 0 && checkHour <= 5) {
            return true;
        }
        return false;
    }

    public int getNightCheckDB() {
        return Settings.System.getInt(mContext.getContentResolver(), "check_night_mode", 0);
    }

    public void setNightDB(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), "check_night_mode", mode);
    }

    public void setAlreadyNightModeDB(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), "already_night_mode", mode);
    }

    public int getAlreadyNightModeDB() {
        return Settings.System.getInt(mContext.getContentResolver(), "already_night_mode", 0);
    }

    public void setUserBrightnessChange(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), "user_change_brightness", mode);
    }

    public int getUserBrightnessChange() {
        return Settings.System.getInt(mContext.getContentResolver(), "user_change_brightness", 0);
    }

    public void setUserBrightnessChangeNoNight(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), "user_change_brightness_no_night",
                mode);
    }

    public int getUserBrightnessChangeNoNight() {
        return Settings.System.getInt(mContext.getContentResolver(),
                "user_change_brightness_no_night", 0);
    }

    public void saveTempOldBrightness(int brightness) {
        Settings.System.putInt(mContext.getContentResolver(), "temp_night_old_brightness",
                brightness);
    }

    public int getTempOldBrightness() {
        return Settings.System.getInt(mContext.getContentResolver(), "temp_night_old_brightness",
                100);
    }

    public int getSettingsStyle() {
        return Settings.System.getInt(mContext.getContentResolver(), "settings_style", 0);
    }

    public void setNightModeEnabled(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), "night_mode_enabled", mode);
    }

    public int getNightModeEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), "night_mode_enabled", 0);
    }

    public void setTabNightCheck(int check) {
        Log.d(TAG, "[Nightmode] setTabNightCheck : " + check);
        Settings.System.putInt(mContext.getContentResolver(),
                "save_tab_night_check", check);
    }

    public int getTabNightCheck() {
        Log.d(TAG, "[Nightmode] getTabNightCheck");
        return Settings.System.getInt(mContext.getContentResolver(),
                "save_tab_night_check", 0);
    }

    public void setNightNode() {
        Log.d(TAG, "[Nightmode] setNightNodeListSettings");
        Log.d(TAG, "[Nightmode] getAlreadyNightModeDB() : " + getAlreadyNightModeDB());
        Log.d(TAG, "[Nightmode] getUserBrightnessChange() : " + getUserBrightnessChange());
        Log.d(TAG, "[Nightmode] getUserBrightnessChangeNoNight() : "
                + getUserBrightnessChangeNoNight());
        if (isNightModeAble()) {
            if (getAlreadyNightModeDB() == 0 && getUserBrightnessChange() == 0) {
                startNotification();
                saveOldBrightness(getTempOldBrightness());
                cancelAlarmStart(mContext);
                setAlarmNextStart(mContext);
                setBrightness(mScreenBrightnessMinimum, true);
                setAlreadyNightModeDB(1);
            } else if (getAlreadyNightModeDB() == 0 && getUserBrightnessChange() == 1) {
                startNotification();
                cancelAlarmStart(mContext);
                setAlarmNextStart(mContext);
                saveOldBrightness(getTempOldBrightness());
                setBrightness(getCurrentBrightness(), true);
                setAlreadyNightModeDB(1);
                setUserBrightnessChange(0);
            }
            setNightModeEnabled(1);
        } else {
            if (getAlreadyNightModeDB() == 1 && !isNightModeTime()) {
                startNotification();
                cancelAlarmEnd(mContext);
                setAlarmNextEnd(mContext);
                setRestoreBrightness(true);
            }
            setAlreadyNightModeDB(0);
            setUserBrightnessChange(0);
            setUserBrightnessChangeNoNight(0);
            setNightModeEnabled(0);
        }
    }

    public void setNightNodeTabSettings() {
        Log.d(TAG, "[Nightmode] setNightNodeTabSettings");
        Log.d(TAG, "[Nightmode] getAlreadyNightModeDB() : " + getAlreadyNightModeDB());
        Log.d(TAG, "[Nightmode] getUserBrightnessChange() : " + getUserBrightnessChange());
        Log.d(TAG, "[Nightmode] getUserBrightnessChangeNoNight() : "
                + getUserBrightnessChangeNoNight());
        if (isNightModeAble()) {
            if (getAlreadyNightModeDB() == 0 && getUserBrightnessChange() == 0) {
                startNotification();
                cancelAlarmStart(mContext);
                setAlarmNextStart(mContext);
                saveOldBrightness(getCurrentBrightness());
                setBrightness(mScreenBrightnessMinimum, true);
                setAlreadyNightModeDB(1);
            } else if (getAlreadyNightModeDB() == 0 && getUserBrightnessChange() == 1) {
                startNotification();
                cancelAlarmStart(mContext);
                setAlarmNextStart(mContext);
                saveOldBrightness(getCurrentBrightness());
                setBrightness(getCurrentBrightness(), true);
                setAlreadyNightModeDB(1);
                setUserBrightnessChange(0);
            }
            setNightModeEnabled(1);
            setTabNightCheck(0);
        } else {
            if (isNightModeTime()) {
                //startNotification();
                //cancelAlarmEnd(mContext);
                //setAlarmNextEnd(mContext);
                if (getTabNightCheck() == 1) {
                    Log.d(TAG, "[Nightmode] getTabNightCheck == 1");
                    setRestoreBrightness(true);
                }
            } else if (getAlreadyNightModeDB() == 1 && !isNightModeTime()) {
                startNotification();
                cancelAlarmEnd(mContext);
                setAlarmNextEnd(mContext);
                setRestoreBrightness(true);
            }
            setAlreadyNightModeDB(0);
            setUserBrightnessChange(0);
            setUserBrightnessChangeNoNight(0);
            setNightModeEnabled(0);
            setTabNightCheck(0);
        }
    }

    /* Notification */
    public void startNotification() {
        if (!isNightNoti()) {
            Log.d("jw", "Only VZW Supported start return");
            return;
        }

        int icon;
        String tickerText;
        String text;
        String title = Utils.getResources().getString(R.string.night_brightness_title);
        Intent intent = new Intent(mContext, DisplaySettings.class);

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (isNightModeTime()) {

            icon = R.drawable.notify_nightmode_vzw;
            tickerText = Utils.getResources().getString(R.string.night_mode_turned_on);
            text = Utils.getResources().getString(R.string.night_mode_turned_on);
            intent = new Intent("android.settings.DISPLAY_SETTINGS");

        } else {

            icon = R.drawable.notify_nightmode_vzw;
            boolean is24 = DateFormat.is24HourFormat(mContext);
            String notiText = Utils.getResources().getString(is24 ?
                    R.string.brightness_night_mode_24 : R.string.brightness_night_mode_12);
            tickerText = notiText;
            text = notiText;
            intent = new Intent("android.settings.DISPLAY_SETTINGS");

        }

        mNotification = new Notification(icon, tickerText, 0);
        mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;

        if (Utils.supportSplitView(mContext.getApplicationContext())) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        mNotification.setLatestEventInfo(mContext, title, text, contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void endNotification() {
        if (!isNightNoti()) {
            Log.d("jw", "Only VZW Supported end return");
            return;
        }

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public boolean isEasySetting() {
        return Utils.supportEasySettings(mContext);
    }

    public boolean isNightNoti() {
        if (Utils.isUI_4_1_model(mContext) && "ATT".equals(Config.getOperator())) {
            return true;
        } else {
            return false;
        }

    }
}
