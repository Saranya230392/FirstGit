package com.android.settings.notification;

import java.util.Calendar;
import android.app.INotificationManager;
import android.os.ServiceManager;
import android.service.notification.ZenModeConfig;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.content.Context;
import android.widget.TextView;
import android.widget.Button;
import android.content.res.Resources;
import android.text.format.DateFormat;
import com.android.settings.R;

import android.util.Log;

public class ZenModeUtils {

    private static final String TAG = "ZenModeUtils";

    private static TextView sTimeStart = null;
    private static Button sTimeStartBtn = null;
    private static TextView sTimeEnd = null;
    private static Button sTimeEndBtn = null;

    public static ZenModeConfig getZenModeConfig() {
        final INotificationManager nm = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        try {
              return nm.getZenModeConfig();
            } catch (Exception e) {
              Log.w(TAG, "Error calling NoMan", e);
              return new ZenModeConfig();
        }
    }

    public static boolean setZenModeConfig(ZenModeConfig config) {
        final INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        try {
            final boolean success = nm.setZenModeConfig(config);
            if (success) {
                Log.d(TAG, "setZenModeConfig Save Success");
            }
            return success;
        } catch (Exception e) {
           Log.w(TAG, "Error calling NoMan", e);
           return false;
        }
    }

    public static void setZenModeTimeComponent(TextView starttime, Button startbutton, TextView endtime, Button endtbutton) {
        sTimeStart = starttime;
        sTimeStartBtn = startbutton;
        sTimeEnd = endtime;
        sTimeEndBtn = endtbutton;
    }

    public static void setZenModeTimeComponentEnable(boolean isEnable) {
       if (sTimeStart != null && sTimeStartBtn != null && sTimeEnd != null && sTimeEndBtn != null) {
           if (sTimeStart.isEnabled() == isEnable) {
               return;
           }
           sTimeStart.setEnabled(isEnable);
           sTimeStartBtn.setEnabled(isEnable);
           sTimeEnd.setEnabled(isEnable);
           sTimeEndBtn.setEnabled(isEnable);
       }
    }

    public static void updateZenModeDayConfig(Context context, ZenModeConfig newconfig) {
       final int[] days = ZenModeConfig.tryParseDays(newconfig.sleepMode);
       updateZenModeTimeConfig(context, newconfig, days);
    }

    private static void updateZenModeTimeConfig(Context context, ZenModeConfig newconfig, int[] days) {
       if (sTimeStartBtn != null && sTimeEndBtn != null) {
           sTimeStartBtn.setText(getZenModeTimeString(context, newconfig.sleepStartHour, newconfig.sleepStartMinute, false));
           sTimeEndBtn.setText(getZenModeTimeString(context, newconfig.sleepEndHour, newconfig.sleepEndMinute, 
                                      zenModeCheckNextdayString(newconfig.sleepStartHour, newconfig.sleepStartMinute, newconfig.sleepEndHour, newconfig.sleepEndMinute)));
           if (days == null) {
               if (sTimeStart != null && sTimeStart.isEnabled()) {
                  setZenModeTimeComponentEnable(false);
               }
           } else {
               if (sTimeStart != null && sTimeStart.isEnabled() == false) {
                  setZenModeTimeComponentEnable(true);
               }
           }
       }
    }

    public static String getZenModeTimeString(Context context, int hourOfDay, int minute, boolean isnextday) {
         final Calendar c = Calendar.getInstance();
         c.set(Calendar.HOUR_OF_DAY, hourOfDay);
         c.set(Calendar.MINUTE, minute);
         String time = DateFormat.getTimeFormat(context).format(c.getTime());
         if (isnextday) {
            time = context.getResources().getString(R.string.zen_mode_end_time_summary_format, time);
         }
         return time;
    }

    public static boolean zenModeCheckNextdayString(int startHourOfDay, int startMinute, int endHourOfDay, int endMinute) {
        final int startMin = 60 * startHourOfDay + startMinute;
        final int endMin = 60 * endHourOfDay + endMinute;
        final boolean nextDay = startMin >= endMin;

        return nextDay;
    }

}
