package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;

public class GestureMotionReceiver extends BroadcastReceiver {
    //[jongwon007.kim][T-Action_Receiver]
    public final static String SKT_MOTION_SETTINGS_CHANED = "com.skt.motions.SKT_MOTION_SETTINGS_CHANGED";
    public final static String T_ACTION_SETTING = "t_action_setting";
    public final static String GESTURE_VOICE_CALL = "gesture_voice_call";
    public final static String GESTURE_ALARM = "gesture_alarm";



    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        if (action.equals(SKT_MOTION_SETTINGS_CHANED)) {
            if (intent.hasExtra("enabled") && !intent.hasExtra("motion_setting")) {
                boolean mMotionEnabled = intent.getBooleanExtra("enabled", true);
                if (mMotionEnabled) {
                    Settings.System.putInt(context.getContentResolver(), T_ACTION_SETTING, 1);
                    setGestureDBWhenTactionEnabled(context);
                } else {
                    Settings.System.putInt(context.getContentResolver(), T_ACTION_SETTING, 0);
                }
            }

            if (intent.hasExtra("motion_setting")) {
                String mMotionType = intent.getStringExtra("motion_setting");
                if ("motion_voicecall".equals(mMotionType)) {
                    Settings.System.putInt(context.getContentResolver(), GESTURE_VOICE_CALL, 0);
                } else if ("motion_alarm".equals(mMotionType)) {
                    Settings.System.putInt(context.getContentResolver(), GESTURE_ALARM, 0);
                }
            }

        }
    }

    private void setGestureDBWhenTactionEnabled(Context context) {

        String[] mVioceCall = { "motion_voicecall" };
        String[] mAlaram = { "motion_alarm" };

        if (getTActionDB(mVioceCall, context)
                && getTActionDB(mAlaram, context)) {
            Settings.System.putInt(context.getContentResolver(), "gesture_voice_call", 0);
            Settings.System.putInt(context.getContentResolver(), "gesture_alarm", 0);
        } else if (getTActionDB(mVioceCall, context)) {
            Settings.System.putInt(context.getContentResolver(), "gesture_voice_call", 0);
        } else if (getTActionDB(mAlaram, context)) {
            Settings.System.putInt(context.getContentResolver(), "gesture_alarm", 0);
        }
    }

    public static boolean getTActionDB(String[] selectionArgs, Context context) {

        String[] projection = { "on_off" };
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                Uri.parse("content://com.skt.taction.provider.TProvider/taction_setting"),
                projection, "motion_action=?", selectionArgs, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0).equals("ON")) {
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

}