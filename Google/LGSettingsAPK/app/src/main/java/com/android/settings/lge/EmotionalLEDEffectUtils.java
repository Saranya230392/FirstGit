package com.android.settings.lge;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.os.BatteryManager;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.lgesetting.Config.Config;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.EmotionalLedManager;
import com.lge.systemservice.core.LGLedRecord;

import com.android.settings.R;

public class EmotionalLEDEffectUtils {
    public static final int EMOTIONAL_LED_INCOMING_CALL_NOTI = 0;
    public static final int EMOTIONAL_LED_INCOMING_CALL_FAVORITE_NOTI = 1;
    public static final int EMOTIONAL_LED_ALARM_NOTI = 2;
    public static final int EMOTIONAL_LED_DOWNLOAD_APPS_NOTI = 3;
    public static final int EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI = 4;
    public static final int EMOTIONAL_LED_BATTERY_CHARGING_NOTI = 5;
    public static final int EMOTIONAL_LED_CALENDAR_NOTI = 6;
    public static final int EMOTIONAL_LED_GPS_NOTI = 7;
    public static final int EMOTIONAL_LED_OSAIFU_KEITAI_NOTI = 8;
    public static final int EMOTIONAL_LED_BACK_NOTI = 9;
    public static final int EMOTIONAL_LED_SEEK_BAR_NOTI = 10;
    public static final int EMOTIONAL_LED_IN_CALL_BACK_NOTI = 11;
    public static final int EMOTIONAL_LED_VOICE_RECORDER_NOTI = 12;
    public static final int EMOTIONAL_LED_CAMERA_TIMER_NOTI = 13;
    public static final int EMOTIONAL_LED_CAMERA_FACE_DETECTING_NOTI = 14;

    public static String sPackageName = "";
    public static int sShareFunction = 0;
    public static int sLastSelectedFunction = 0;
    public static ValueAnimator sValueAnimator;
    public static EmotionalLedManager sLedManager;
    public static int sStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    public static LGContext sServiceContext;

    public static void setEmotionalLEDSettings(Context context) {
        sServiceContext = new LGContext(context);
        sLedManager = (EmotionalLedManager)sServiceContext
                .getLGSystemService(LGContext.EMOTIONALLED_SERVICE);
        sValueAnimator = ValueAnimator.ofInt(0, 4000);
        sValueAnimator.setDuration(4000);

        sValueAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                Log.d("hsmodel", "onAnimationEnd");
                // [Stop LED] 4sec is passed.
                sLedManager.stop(sPackageName, sLastSelectedFunction);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
            }
        });
    }
    
    public static void startEmotionalBackLED(boolean mValue, int function, Context context) {
        if (mValue != false) {
            if (sLedManager == null) {
                sLedManager = (EmotionalLedManager)sServiceContext
                        .getLGSystemService(LGContext.EMOTIONALLED_SERVICE);
            }
            if (function == EMOTIONAL_LED_DOWNLOAD_APPS_NOTI) {
                sLedManager.stop(sPackageName, sLastSelectedFunction);
                if (mValue) {
                    Toast.makeText(context, R.string.sp_home_button_led_download_toast,
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            sValueAnimator.start();
            Log.d("hsmodel", "EMOTIONAL_LED_ALARM is true. Now try to start a LED play.");
            try {
                // [Stop LED] something is being on
                Log.d("hong", "sLastSelectedFunction : " + sLastSelectedFunction);
                sLedManager.stop(sPackageName, sLastSelectedFunction);
                sLastSelectedFunction = function;
                LGLedRecord record = new LGLedRecord();
                record.priority = LGLedRecord.PRIORITY_DEFAULT;
                record.whichLedPlay = LGLedRecord.ONLY_BACK_LED_PLAY;
                matchLEDRecord(function, record);
                record.flags = LGLedRecord.FLAG_SHOW_LIGHTS_ONLY_LED_ON;
                sLedManager.start(sPackageName, function, record);
            } catch (Throwable e) {
                Log.w("hsmodel",
                        "EmotionalLED may not be supported for this device. See the below stack trace.",
                        e);
            }
        } else if (sLastSelectedFunction == function) {
            // [Stop LED] Because a user selects other item.
            sLedManager.stop(sPackageName, function);
            Log.d("hsmodel", "EMOTIONAL_LED_ALARM is false. Now try to stop a LED play.");
        }
    }

    public static void startEmotionalLED(boolean mValue, int function, Context context) {
        if (sLedManager == null) {
            sLedManager = (EmotionalLedManager)sServiceContext
                    .getLGSystemService(LGContext.EMOTIONALLED_SERVICE);
        }
        if (function == EMOTIONAL_LED_DOWNLOAD_APPS_NOTI) {
            Log.w("hsmodel", "Download apps make EmotionalLED off.");
            Log.d("hong", "download sLastSelectedFunction: " + sShareFunction);
            if (sShareFunction == EMOTIONAL_LED_SEEK_BAR_NOTI) {
                sLedManager.stop(sPackageName, sShareFunction);
            } else {
                sLedManager.stop(sPackageName, sLastSelectedFunction);
            }
            if (mValue) {
                Toast.makeText(context, R.string.sp_home_button_led_download_toast,
                        Toast.LENGTH_SHORT).show();
            }
            return;
        } else if (!mValue && (function == EMOTIONAL_LED_BATTERY_CHARGING_NOTI)) {
            sLedManager.stop(sPackageName, sLastSelectedFunction);
            Toast.makeText(context, R.string.emotional_led_battery_charging_off_text,
                    Toast.LENGTH_SHORT).show();
        } else if ("DCM".equals(Config.getOperator()) && mValue
                && (function == EMOTIONAL_LED_BATTERY_CHARGING_NOTI)) {
            if (sStatus == BatteryManager.BATTERY_STATUS_CHARGING
                    || sStatus == BatteryManager.BATTERY_STATUS_FULL) {
                Log.i("hsmodel", "with charging");
                sLedManager.stop(sPackageName, sLastSelectedFunction);
                return;
            } else {
                Log.i("hsmodel", "discharging");
            }
        }
        if (mValue != false) {
            sValueAnimator.start();
            Log.d("hsmodel", "EMOTIONAL_LED_ALARM is true. Now try to start a LED play.");
            try {
                // [Stop LED] something is being on
                Log.d("hong", "sLastSelectedFunction : " + sShareFunction);
                if (sShareFunction == EMOTIONAL_LED_SEEK_BAR_NOTI) {
                    sLedManager.stop(sPackageName, sShareFunction);
                } else {
                    sLedManager.stop(sPackageName, sLastSelectedFunction);
                }
                sLastSelectedFunction = function;
                sShareFunction = function;
                LGLedRecord record = new LGLedRecord();
                record.priority = LGLedRecord.PRIORITY_DEFAULT;
                record.whichLedPlay = LGLedRecord.ONLY_FRONT_LED_PLAY;
                //record.patternId = LGLedRecord.ID_ALARM;
                matchLEDRecord(function, record);
                record.flags = LGLedRecord.FLAG_SHOW_LIGHTS_ONLY_LED_ON;
                sLedManager.start(sPackageName, function, record);
            } catch (Throwable e) {
                Log.w("hsmodel",
                        "EmotionalLED may not be supported for this device. See the below stack trace.",
                        e);
            }
        } else if (sLastSelectedFunction == function) {
            // [Stop LED] Because a user selects other item.
            sLedManager.stop(sPackageName, function);
            Log.d("hsmodel", "EMOTIONAL_LED_ALARM is false. Now try to stop a LED play.");
        }
    }

    public static void matchLEDRecord(int function, LGLedRecord record) {
        switch (function) {
        case EMOTIONAL_LED_INCOMING_CALL_NOTI:
            record.patternId = LGLedRecord.ID_CALL_01;
            break;
        case EMOTIONAL_LED_INCOMING_CALL_FAVORITE_NOTI:
            record.patternId = LGLedRecord.ID_CALL_02;
            break;
        case EMOTIONAL_LED_ALARM_NOTI:
            record.patternId = LGLedRecord.ID_ALARM;
            break;
        case EMOTIONAL_LED_CALENDAR_NOTI:
            record.patternId = LGLedRecord.ID_CALENDAR_REMIND;
            break;
        case EMOTIONAL_LED_GPS_NOTI:
            //record.patternId = LGLedRecord.ID_ALARM;
            record.patternId = LGLedRecord.ID_GPS_ENABLED;
            break;
        case EMOTIONAL_LED_BATTERY_CHARGING_NOTI:
            record.patternId = LGLedRecord.ID_CHARGING;
            break;
        case EMOTIONAL_LED_DOWNLOAD_APPS_NOTI:
            // temporary code
            record.patternId = LGLedRecord.ID_MISSED_NOTI;
            break;
        case EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI:
            record.patternId = LGLedRecord.ID_MISSED_NOTI;
            break;
        case EMOTIONAL_LED_OSAIFU_KEITAI_NOTI:
            record.patternId = LGLedRecord.ID_FELICA_ON;
            break;
        case EMOTIONAL_LED_VOICE_RECORDER_NOTI:
            record.patternId = LGLedRecord.ID_SOUND_RECORDING;
            break;
        case EMOTIONAL_LED_BACK_NOTI:
            record.patternId = LGLedRecord.ID_LCD_ON;
            break;
        case EMOTIONAL_LED_IN_CALL_BACK_NOTI:
            record.patternId = LGLedRecord.ID_CALLING;
            break;
        case EMOTIONAL_LED_CAMERA_TIMER_NOTI:
            //record.patternId = LGLedRecord.ID_CAMERA_TIMER_EFFECT_3SEC;
            record.patternFilePath = "CameraTimer3s.txt";
            break;
        case EMOTIONAL_LED_CAMERA_FACE_DETECTING_NOTI:
            //record.patternId = LGLedRecord.ID_CAMERA_SHOT_BEST_GUIDE;
            record.patternFilePath = "CameraBestShotGuide2.txt";
            break;
        default:
            // temporary code
            record.patternId = LGLedRecord.ID_MISSED_NOTI;
            break;
        }
    }
}
 
