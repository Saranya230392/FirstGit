package com.android.settings.powersave;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import com.android.settings.bluetooth.LocalBluetoothAdapter;
import com.android.settings.bluetooth.LocalBluetoothManager;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lge.QuadGearBox;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.Toast;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.IPowerManager;
import android.os.SystemProperties;

import com.lge.constants.SettingsConstants;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

import com.lge.constants.LGIntent;

public class PowerSave {

    final long mACTION_FEATURE_USAGE = 0x02000001;
    final long mACTION_FEATURE_SETTING_CHANGED = 0x02000002;
    public static final boolean DBG = true;
    private static final String TAG = "PowerSave";

    public static final String ACTION_POWERSAVE_ACTIVATION = "com.android.settings.powersave.POWERSAVE_ACTIVATION";
    // yonguk.kim 20120220 Broadcast to others (Wi-Fi, Bluetooth,...etc.)
    public static final String ACTION_POWERSAVE_ACTIVATION_TO_OTHERS = "com.android.settings.powersave.POWERSAVE_ACTIVATION_TO_OTHERS";
    public static final String EXTRA_POWERSAVE_NAME = "powersave_name";
    public static final String EXTRA_POWERSAVE_ACTIVATION = "powersave_activation";
    public static final String ACTION_POWERSAVE_STOP_SERVICE = "com.android.settings.powersave.POWERSAVE_STOP_SERVICE";

    public static final String BATTERY_SERVICE_CLASS_NAME = "com.android.settings.powersave.PowerSaveBatteryService";
    public static final String POWERSAVE_SERVICE_CLASS_NAME = "com.android.settings.powersave.PowerSaveService";
    public static final String EXTRA_RESTART = "extra_restart";

    // Power Control widget update
    public static final String ACTION_POWERSAVE_STATE_CHANGED = "com.android.settings.powersave.action.STATE_CHANGED";
    public static final String ACTION_POWERSAVE_ACTIVATED = "com.android.settings.powersave.action.ACTIVATED";

    // Power save default value
    public static final int DEFAULT_BRIGHTNESS = 20;
    public static final int DEFAULT_SCREEN_TIMEOUT = 15000;
    // yonguk.kim 20120210 ICS PowerSave
    public static final int DEFAULT_FRONT_LED = 1500;
    public static final int DEFAULT_FRONT_LED_ATT = 3000;
    public static final int DEFAULT_POWER_SAVE_ENABLED = 0; // yonguk.kim 20120410 Default value should be false.

    public static final int defBrightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC; // Not automatic
    //public static final int defBrightnessAutomaticOption = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC_OPTIMIZED; // Optimized.
    //public static final int defBrightnessAutomaticOption = 1; // Optimized.

    // Default Auto power save mode
    public static final int FALLBACK_POWER_SAVE_MODE_VALUE = 30;
    public static final int POWER_SAVE_MODE_VALUE_IMMEDIATLY = 100; // yonguk.kim 20120423 Add Powersave immediatly

    //private static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM;// + 10;
    private static int MINIMUM_BACKLIGHT;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;

    public static final String PACKAGENAME_CAMERA = "com.lge.camera";
    public static final String PACKAGENAME_VOICERECORDER = "com.lge.voicerecorder";
    public static final String PACKAGENAME_PHONE = "com.android.phone";

    private static ContentResolver mContentResolver;
    private static Context mContext;

    private SharedPreferences mPowerSavePref = null;
    public static final String PREF_POWER_SAVE = "pref_power_save";
    public static final String PREF_POWER_SAVE_START_TIME = "power_save_start_time";
    public static final String PREF_BATTERY = "battery";

    public static final int NOTIFICATION_SELECT = 0;
    public static final int NOTIFICATION_ACTIVATED = NOTIFICATION_SELECT + 1;
    public static final int NOTIFICATION_ON_CHARGING = NOTIFICATION_SELECT + 2;
    public static final int NOTIFICATION_WAIT_BATTERY_LEVEL = NOTIFICATION_SELECT + 3;

    public static final String ACTION_POWERSAVE_MODE_CHANGED = "com.android.settings.powersave.action.MODE_CHANGED";
    public static final String ACTION_POWERSAVE_BATTERY_INDICATOR_CHANGED = "com.android.settings.powersave.action.BATTERY_INDICATOR_CHANGED";
    public static final int ECO_MODE = QuadGearBox.ECO_MODE;

    public static final String POWER_SAVE_NOTIFICATION_FLASH = "power_save_notification_flash";
    public static final String POWER_SAVE_NOTIFICATION_FLASH_RESTORE = "power_save_notification_flash_restore";

    public static final String LGE_NOTIFICATION_LIGHT_PULSE = "lge_notification_light_pulse";
    public static final String POWER_SAVE_EMOTIONAL_LED = "power_save_emotional_led";
    public static final String POWER_SAVE_EMOTIONAL_LED_RESTORE = "power_save_emotional_led_restore";

    public static final String PLC_MODE_SET = "plc_mode_set";
    public static final String POWER_SAVE_AUTO_SCREEN_TONE = "power_save_auto_screen_tone";
    public static final String POWER_SAVE_AUTO_SCREEN_TONE_RESTORE = "power_save_auto_screen_tone_restore";

    public PowerSave(Context c) {
        mContext = c;
        mContentResolver = mContext.getContentResolver();

        mPowerSavePref = mContext.getSharedPreferences(PREF_POWER_SAVE,
                mContext.MODE_WORLD_READABLE | mContext.MODE_WORLD_WRITEABLE);

        MINIMUM_BACKLIGHT = Config.getFWConfigInteger(mContext, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim");

        Log.d(TAG, "MINIMUM_BACKLIGHT : " + MINIMUM_BACKLIGHT);
    }

    public void doPowerSave() {

        Log.d(TAG, "Start doPowerSave()");

        applyPowerSaveBt();

        new Thread() {
            @Override
            public void run() {
                applyPowerSaveWifi();

                //applyPowerSaveGps();
                if (NfcAdapter.getDefaultAdapter(mContext) != null
                        && "KR".equals(Config.getCountry())) {
                    applyPowerSaveNfc();
                }
                applyPowerSaveSync();
                applyPowerSaveBrightness();
                //applyPowerSaveAnimations();
                applyPowerSaveTouch();
                // jongtak0920.kim Add Notification Flash
                //                applyPowerSaveNotificationFlash();
                // jongtak0920.kim Add Emotional LED
                applyPowerSaveEmotionalLED();
                applyPowerSaveCpu();
                applyPowerSaveScreenTimeout();
                applyPowerSaveFrontLed();
                // jongtak0920.kim Add Auto-adjust screen tone
                applyPowerSaveAutoScreenTone();
                applyPowerSaveQuickCase();
                Log.d("jw", "doPowerSave");

                /* Power control activation update */
                updateActivationPowerControl();
            }
        }.start();
    }

    public void writeSharedPreference(String mode) {
        if (PREF_POWER_SAVE_START_TIME.equals(mode)) {
            String batteryHistory = readSharedPreference(PREF_BATTERY);
            String[] data = batteryHistory.split(",");
            String lastData = data[data.length - 1];
            String[] lastBatteryData = lastData.split(":");

            long when = System.currentTimeMillis();

            String value = Long.toString(when) + ":" + lastBatteryData[1];

            writeSharedPreference(mode, value);
        }
    }

    public void writeSharedPreference(String mode, String value) {
        SharedPreferences.Editor editor = mPowerSavePref.edit();

        if (!mode.equals(PREF_POWER_SAVE_START_TIME)) {
            /* Check the activation sate */
            if (Settings.System.getInt(mContentResolver,
                    SettingsConstants.System.POWER_SAVE_MODE_ACTIVATED, 0) != 0) {
                value = value + ":" + "1"; // Activated
            }
            else {
                value = value + ":" + "0"; // Not activated
            }
        }

        editor.putString(mode, value);
        editor.commit();
    }

    public String readSharedPreference(String mode) {
        return mPowerSavePref.getString(mode, "");
    }

    private void applyPowerSaveQuickCase() {

        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover") == true
                && Settings.System.getInt(mContentResolver, "power_save_quick_case", 1) != 0) {
            Log.d("jw", "doapplyPowerSaveQuickCase");
            Settings.System.putInt(
                    mContentResolver, "power_save_quick_case_restore", 1);
            int coverSelect = Settings.Global.getInt(mContentResolver,
                    SettingsConstants.Global.COVER_TYPE, 0);

            if (coverSelect == 0) {

                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            } else if (coverSelect == 2) {
                int lolli = Settings.Global.getInt(mContentResolver,
                        SettingsConstants.Global.LOLLIPOP_COVER_TYPE, 0);
                Settings.System.putInt(
                        mContentResolver, "power_save_quick_case_restore_lolli_type", lolli);
            }

            Settings.System.putInt(
                    mContentResolver, "power_save_quick_case_restore_type", coverSelect);

            Settings.Global.putInt(
                    mContentResolver, SettingsConstants.Global.COVER_TYPE, 5);
        }
    }

    private void applyPowerSaveWifi() {

        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI, 1) != 0) {
            // lyang2000 2012/03/13 Wi-Fi process oneself.
            sendToOthers(SettingsConstants.System.POWER_SAVE_WIFI, true); // yonguk.kim 2012 Send broadcast to others

            /*WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if(WifiManager.WIFI_STATE_DISABLED != wifiManager.getWifiState()) {
                wifiManager.setWifiEnabled(false);
                // LGE_CHANGE_S, Power save restore
                Settings.System.putInt(
                        mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI_RESTORE, 1);
                // LGE_CHANGE_E, Power save restore
            }*/
        }
    }

    /*private void applyPowerSave3g() {
        if(Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_3G, 0) != 0) {

            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getMobileDataEnabled()) {
                cm.setMobileDataEnabled(false);
            }
        }
    }*/
    /*private void applyPowerSaveLTE() {
        if(Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_LTE, 0) != 0) {

            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getMobileDataEnabled()) {
                cm.setMobileDataEnabled(false);
            }
        }
    }*/
    private void applyPowerSaveSync() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC, 0) != 0) {
            /* Background data
            ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            cm.setBackgroundDataSetting(false);
            */
            if (ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(false);

                /* LGE_CHANGE_S, Power save restore */
                Settings.System.putInt(
                        mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC_RESTORE, 1);
                /* LGE_CHANGE_E, Power save restore */
            }
        }
    }

    private void applyPowerSaveBt() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_BT, 1) != 0) {
            sendToOthers(SettingsConstants.System.POWER_SAVE_BT, true); // yonguk.kim 2012 Send broadcast to others

            /*LocalBluetoothManager localBluetoothManager = LocalBluetoothManager.getInstance(mContext);
            if(BluetoothAdapter.STATE_OFF != localBluetoothManager.getBluetoothState()) {
                localBluetoothManager.setBluetoothEnabled(false);
            }*/
            //[S][2012.01.29][jm.lee2][common] WBT.310916
            // lyang2000 2012/03/21 BT process oneself.
            /*LocalBluetoothManager instance = LocalBluetoothManager.getInstance(mContext);
            if (instance != null) {
                LocalBluetoothAdapter localAdapter = instance.getBluetoothAdapter();
                if(BluetoothAdapter.STATE_OFF != localAdapter.getBluetoothState()) {
                    localAdapter.setBluetoothEnabled(false);
                    // LGE_CHANGE_S, Power save restore
                    Settings.System.putInt(
                            mContentResolver, SettingsConstants.System.POWER_SAVE_BT_RESTORE, 1);
                    // LGE_CHANGE_E, Power save restore
                }
            }*/
            //[E][2012.01.29][jm.lee2][common] WBT.310916
        }
    }

    private void applyPowerSaveGps() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_GPS, 0) != 0) {

            boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                    mContentResolver, LocationManager.GPS_PROVIDER);
            if (gpsEnabled) {
                Settings.Secure.setLocationProviderEnabled(mContentResolver,
                        LocationManager.GPS_PROVIDER, false);
            }
        }
    }

    private void applyPowerSaveNfc() {
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_NFC, 1) != 0) {
            sendToOthers(SettingsConstants.System.POWER_SAVE_NFC, true); // yonguk.kim 2012 Send broadcast to others
        }
    }

    // yonguk.kim 20120320 Add CPU Core

    private void applyPowerSaveCpu() {
        if (Settings.System.getInt(mContentResolver,
                        SettingsConstants.System.POWER_SAVE_CPU_ADJUST, 1) == 0) {
            return;
        }
        int current_mode = Settings.System.getInt(mContentResolver,
                SettingsConstants.System.ECO_MODE, 0);
        Settings.System.putInt(mContentResolver, SettingsConstants.System.POWER_SAVE_CPU_RESTORE,
                current_mode);
        Settings.System.putInt(mContentResolver, SettingsConstants.System.ECO_MODE, 1);
    }

    private void applyPowerSaveBrightness() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST, 1) == 0) {
            return;
        }

        int brightnessPercent = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_BRIGHTNESS,
                DEFAULT_BRIGHTNESS);

        int brightness = getValue(brightnessPercent);

        //int currentBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, MAXIMUM_BACKLIGHT);
        int currentBrightness = getCurrentBrightness();

        Log.d(TAG, "applyPowerSaveBrightness()- currentBrightness : " + currentBrightness
                + ", brightness : " + brightness);

        if (currentBrightness > (brightness + MINIMUM_BACKLIGHT)) {
            if (noapplyMultiALC()) {
                if (Settings.System.getInt(mContentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE, 1) == 1) {
                    Log.d(TAG, "applyPowerSaveBrightness_DCM_TMO");
                    Settings.System.putInt(mContentResolver,
                            "power_save_auto_brightness", 1);
                    Settings.System.putInt(mContentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
                } else {
                    Settings.System.putInt(mContentResolver,
                            "power_save_auto_brightness", 0);
                }
            }
            setBrightness(brightness + MINIMUM_BACKLIGHT);
            // yonguk.kim 20120521 Restore error fix if AutoBrightness set to true
            int current_db_to_restore = Settings.System.getInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, MAXIMUM_BACKLIGHT);
            Settings.System.putInt(
                    mContentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness
                            + MINIMUM_BACKLIGHT);

            Log.d(TAG, "current_db_to_restore: " + current_db_to_restore);
            if (current_db_to_restore > MAXIMUM_BACKLIGHT || current_db_to_restore < 0) {
                current_db_to_restore = currentBrightness;
            }
            /* LGE_CHANGE_S, Power save restore */
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE,
                    current_db_to_restore);
            /* LGE_CHANGE_E, Power save restore */
        }

        // Not use
        // Auto brightness
        /*boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            int changedMode =  Settings.System.getInt(
                    mContentResolver, Settings.System.POWER_SAVE_BRIGHTNESS_MODE, defBrightnessMode);
                    //mContentResolver, "power_save_brightness_mode", defBrightnessMode);

            int currentMode =  Settings.System.getInt(
                    mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, defBrightnessMode);

            //if(changedMode != 0) {

                int option =  Settings.System.getInt(
                        mContentResolver, Settings.System.POWER_SAVE_BRIGHTNESS_AUTOMATIC_OPTION, defBrightnessAutomaticOption);
                Settings.System.putInt(mContentResolver,  // 0:power saving, 1:optimized brightness
                        Settings.System.SCREEN_BRIGHTNESS_AUTOMATIC_MODE, option);


                Settings.System.putInt(mContentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE, changedMode);
            //}
        }*/
    }

    /*private void applyPowerSaveScreenTimeout() {
        int value = Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_SCREEN_TIMEOUT, DEFAULT_SCREEN_TIMEOUT);

        Settings.System.putInt(
                mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, value);
    }*/
    /*private void applyPowerSaveAccelerometer() {
        if(Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_ACCELEROMETER, 0) != 0) {

            if(Settings.System.getInt(
                    mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) != 0) {
                Settings.System.putInt(
                        mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 0);
            }
        }
    }*/
    private void applyPowerSaveAnimations() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_ANIMATIONS, 0) != 0) {
            /* GB version
            IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            float[] mAnimationScales = null;
            try {
                mAnimationScales = mWindowManager.getAnimationScales();
            } catch(RemoteException e) {
            }

            String noAnimationValue = "00";
            try {
                int value = Integer.parseInt(noAnimationValue);
                if (mAnimationScales != null) {
                    if (mAnimationScales.length >= 1) {
                        mAnimationScales[0] = value%10;
                    }
                    if (mAnimationScales.length >= 2) {
                        mAnimationScales[1] = (value/10)%10;
                    }
                    try {
                        mWindowManager.setAnimationScales(mAnimationScales);
                    } catch (RemoteException e) {
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist animation setting", e);
            }
            */

            IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager
                    .getService("window"));
            try {
                float scale = 0;
                if (mWindowManager != null) {
                    mWindowManager.setAnimationScale(0, scale);
                    mWindowManager.setAnimationScale(1, scale);
                }
            } catch (Exception e) { //[2012.01.29][jm.lee2][common] WBT.310918
            }
        }
    }

    /*private void applyPowerSaveGesture() {
        if(Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_GESTURE, 0) != 0) {
            //
        }
    }*/
    /*private void applyPowerSaveSilent() {
        if(Settings.System.getInt(
                mContentResolver, Settings.System.POWER_SAVE_SILENT, 0) != 0) {

            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            boolean vibeInSilent = (1 == Settings.System.getInt(
                    mContentResolver, Settings.System.VIBRATE_IN_SILENT, 1));

            mAudioManager.setRingerMode(
                vibeInSilent ? AudioManager.RINGER_MODE_VIBRATE
                             : AudioManager.RINGER_MODE_SILENT);
        }
    }*/
    /*private void applyPowerSaveVibrate() {
        String value = Settings.System.getString(
                mContentResolver, Settings.System.POWER_SAVE_VIBRATE);

        setPhoneVibrateSettingValue(value);
    }*/
    /*private void setPhoneVibrateSettingValue(String value) {
        boolean vibeInSilent;
        int callsVibrateSetting;
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (value.equals(VALUE_VIBRATE_UNLESS_SILENT)) {
            callsVibrateSetting = AudioManager.VIBRATE_SETTING_ON;
            vibeInSilent = false;
        } else if (value.equals(VALUE_VIBRATE_NEVER)) {
            callsVibrateSetting = AudioManager.VIBRATE_SETTING_OFF;
            vibeInSilent = false;
        } else if (value.equals(VALUE_VIBRATE_ONLY_SILENT)) {
            callsVibrateSetting = AudioManager.VIBRATE_SETTING_ONLY_SILENT;
            vibeInSilent = true;
        } else { //VALUE_VIBRATE_ALWAYS
            callsVibrateSetting = AudioManager.VIBRATE_SETTING_ON;
            vibeInSilent = true;
        }

        Settings.System.putInt(mContentResolver,
            Settings.System.VIBRATE_IN_SILENT,
            vibeInSilent ? 1 : 0);

        // might need to switch the ringer mode from one kind of "silent" to
        // another
        final int ringerMode = mAudioManager.getRingerMode();
        final boolean silentOrVibrateMode =
                ringerMode != AudioManager.RINGER_MODE_NORMAL;
        if (silentOrVibrateMode) {
            mAudioManager.setRingerMode(
                vibeInSilent ? AudioManager.RINGER_MODE_VIBRATE
                             : AudioManager.RINGER_MODE_SILENT);
        }

        mAudioManager.setVibrateSetting(
            AudioManager.VIBRATE_TYPE_RINGER,
            callsVibrateSetting);
    }*/

    /* LGE_CHANGE_S, ICS scenario update */
    private void applyPowerSaveTouch() {
        // yonguk.kim 20120404 Add Power Save Touch DB Check routine
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH, 1) == 0) {
            return;
        }
        if (Settings.System.getInt(mContentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0) {

            Settings.System.putInt(mContentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);

            /* LGE_CHANGE_S, Power save restore */
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH_RESTORE, 1);
            /* LGE_CHANGE_E, Power save restore */

        }
    }

    // jongtak0920.kim Add Notification Flash
    private void applyPowerSaveNotificationFlash() {
        if (Utils.supportNotificationFlash(mContext) == false
                || Settings.System.getInt(mContentResolver, POWER_SAVE_NOTIFICATION_FLASH, 1) == 0) {
            return;
        }

        int current_mode = Settings.System.getInt(mContentResolver,
                SettingsConstants.System.FRONT_KEY_ALL, 0);
        Settings.System.putInt(mContentResolver, POWER_SAVE_NOTIFICATION_FLASH_RESTORE,
                current_mode);
        Settings.System.putInt(mContentResolver, SettingsConstants.System.FRONT_KEY_ALL, 0);
    }

    private void applyPowerSaveEmotionalLED() {
        if (Utils.supportEmotionalLED(mContext) == false
                || Settings.System.getInt(mContentResolver, POWER_SAVE_EMOTIONAL_LED, 0) == 0) {
            return;
        }

        int current_mode = Settings.System
                .getInt(mContentResolver, LGE_NOTIFICATION_LIGHT_PULSE, 0);
        Settings.System.putInt(mContentResolver, POWER_SAVE_EMOTIONAL_LED_RESTORE, current_mode);
        Settings.System.putInt(mContentResolver, LGE_NOTIFICATION_LIGHT_PULSE, 0);
    }

    private void applyPowerSaveAutoScreenTone() {
        if (Utils.isOLEDModel(mContext) == false
                || Settings.System.getInt(mContentResolver, POWER_SAVE_AUTO_SCREEN_TONE, 1) == 0) {
            return;
        }

        int current_mode = Settings.System.getInt(mContentResolver, PLC_MODE_SET, 1);
        Settings.System.putInt(mContentResolver, POWER_SAVE_AUTO_SCREEN_TONE_RESTORE, current_mode);
        Settings.System.putInt(mContentResolver, PLC_MODE_SET, 1);
    }

    private void applyPowerSaveScreenTimeout() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST, 1) == 0) {
            return;
        }

        int currentTimeout = Settings.System.getInt(mContentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, 30000);
        int value = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT, 15000);

        Log.d(TAG, "applyPowerSaveScreenTimeout()- currentTimeout : " + currentTimeout
                + ", value : " + value);

        if (currentTimeout > value && value >= 15000) {
            Settings.System.putInt(
                    mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, value);

            /* LGE_CHANGE_S, Power save restore */
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_RESTORE,
                    currentTimeout);
            /* LGE_CHANGE_E, Power save restore */
        }
    }

    private void applyPowerSaveFrontLed() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 1) == 0) {
            return;
        }
        int currentTimeout = Settings.Secure.getInt(mContentResolver,
                SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500);

        if (Utils.isFolderModel(mContext)) {
            Settings.Secure.putInt(mContentResolver,
                    SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 0);
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED_RESTORE,
                    currentTimeout);

        } else {


            int value;
            if ("ATT".equals(Config.getOperator())) {
                value = Settings.System.getInt(
                        mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED,
                        DEFAULT_FRONT_LED_ATT);
            } else {
                value = Settings.System.getInt(
                        mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED,
                        DEFAULT_FRONT_LED);
            }

            Log.d(TAG, "applyPowerSaveFrontLed()- currentTimeout : " + currentTimeout
                    + ", value : " + value);

            if (currentTimeout > value) {
                Settings.Secure.putInt(
                        mContentResolver, SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, value);

                /* LGE_CHANGE_S, Power save restore */
                Settings.System.putInt(
                        mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED_RESTORE,
                        currentTimeout);
                /* LGE_CHANGE_E, Power save restore */
            }
        }

    }

    /* LGE_CHANGE_E, ICS scenario update */

    public static void setBrightness(int brightness) {
        int automatic_mode = getBrightnessMode();
        boolean automatic_sensor_supported = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");
        Log.d(TAG, "setBrightness, automatic_sensor_supported:" + automatic_sensor_supported);
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                // Multi ALC
                if (automatic_sensor_supported == true &&
                        automatic_mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    // jongtak0920.kim 20121005 Fix Multi ALC in Power saver
                    brightness -= MINIMUM_BACKLIGHT;
                    float valf = (((float)brightness * 2) / (MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT)) - 1.0f; //JB native
                    power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf); //JB native
                    //                    power.setTemporaryScreenBrightnessSettingOverride(brightness+1000);
                } else {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness);
                }
            }
        } catch (RemoteException doe) {

        }
    }

    public static int getBrightnessMode() {
        int automatic_mode = 0;
        try {
            automatic_mode = Settings.System.getInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automatic_mode;
    }

    public static int getValue(int percent) {
        double value = (MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT) * (double)percent / 100;
        return (int)(Math.ceil(value));
    }

    public boolean isRunningBatteryService() {
        boolean isRunning = false;
        String className = "";

        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);

        if (rs == null) {
            return isRunning;
        }

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            className = rsi.service.getClassName();

            //Log.d(TAG, "Package PackageName : " + rsi.service.getPackageName());
            //Log.d(TAG, "Package ClassName : " + className);

            if (className.equals(BATTERY_SERVICE_CLASS_NAME)) {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    public boolean isRunningPowerSaveService() {
        boolean isRunning = false;
        String className = "";

        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);

        if (rs == null) {
            return true; // Not re-start because can not check.
        }

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            className = rsi.service.getClassName();

            //Log.d(TAG, "Package PackageName : " + rsi.service.getPackageName());
            //Log.d(TAG, "Package ClassName : " + className);

            if (className.equals(POWERSAVE_SERVICE_CLASS_NAME)) {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    /* Power control update */
    public void updatePowerControl() {
        Intent i = new Intent(PowerSave.ACTION_POWERSAVE_STATE_CHANGED);
        mContext.sendBroadcast(i);
    }

    public void updateActivationPowerControl() {
        Intent i = new Intent(PowerSave.ACTION_POWERSAVE_ACTIVATED);
        mContext.sendBroadcast(i);
    }

    public boolean isPortraitOri() {
        Configuration conf = mContext.getResources().getConfiguration();
        return (conf.orientation == Configuration.ORIENTATION_PORTRAIT)
                || (conf.orientation == Configuration.ORIENTATION_UNDEFINED);
    }

    public int getPopupBatteryImgId(int battery) {
        if ("VZW".equals(Config.getOperator()) && battery < 21) {
            if (battery >= 18) {
                return R.drawable.img_battery_charging_20_vzw;
            }
            else if (battery >= 13) {
                return R.drawable.img_battery_charging_15_vzw;
            }
            else if (battery >= 8) {
                return R.drawable.img_battery_charging_10_vzw;
            }
            else {
                return R.drawable.img_battery_charging_05_vzw;
            }
        } else if ("ATT".equals(Config.getOperator()) && battery < 21) {
            if (battery >= 18) {
                return R.drawable.img_battery_charging_03;
            }
            else if (battery >= 11) {
                return R.drawable.img_battery_charging_02;
            }
            else if (battery >= 8) {
                return R.drawable.img_battery_charging_10_vzw;
            }
            else {
                return R.drawable.img_battery_charging_05_vzw;
            }
        } else {
            if (battery >= 98) {
                return R.drawable.img_battery_charging_20;
            }
            else if (battery >= 93) {
                return R.drawable.img_battery_charging_19;
            }
            else if (battery >= 88) {
                return R.drawable.img_battery_charging_18;
            }
            else if (battery >= 83) {
                return R.drawable.img_battery_charging_17;
            }
            else if (battery >= 78) {
                return R.drawable.img_battery_charging_16;
            }
            else if (battery >= 73) {
                return R.drawable.img_battery_charging_15;
            }
            else if (battery >= 68) {
                return R.drawable.img_battery_charging_14;
            }
            else if (battery >= 63) {
                return R.drawable.img_battery_charging_13;
            }
            else if (battery >= 58) {
                return R.drawable.img_battery_charging_12;
            }
            else if (battery >= 53) {
                return R.drawable.img_battery_charging_11;
            }
            else if (battery >= 48) {
                return R.drawable.img_battery_charging_10;
            }
            else if (battery >= 43) {
                return R.drawable.img_battery_charging_09;
            }
            else if (battery >= 38) {
                return R.drawable.img_battery_charging_08;
            }
            else if (battery >= 33) {
                return R.drawable.img_battery_charging_07;
            }
            else if (battery >= 28) {
                return R.drawable.img_battery_charging_06;
            }
            else if (battery >= 21) {
                return R.drawable.img_battery_charging_05;
            }
            else if (battery >= 16) {
                return R.drawable.img_battery_charging_04;
            }
            else if (battery >= 13) {
                return R.drawable.img_battery_charging_15_vzw;
            }
            else if (battery >= 8) {
                return R.drawable.img_battery_charging_10_vzw;
            }
            else if (battery >= 5) {
                return R.drawable.img_battery_charging_05_vzw;
            }
            else {
                return R.drawable.img_battery_charging_00;
            }
        }
    }

    /*
     * EXTRA_CHARGING_CURRENT
     * 0 = CHARGING_CURRENT_NOT_CHARGING
     * 1 = CHARGING_CURRENT_NORMAL_CHARGING.
     * 2 = CHARGING_CURRENT_INCOMPATIBLE_CHARGING
     * 3 = CHARGING_CURRENT_UNDER_CURRENT_CHARGING
     * 4 = CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
     * 5 = CHARGING_CURRENT_LLK_NOT_CHARGING
    */
    public int getBatteryImgId(Resources res, Intent intent, int battery) {
        boolean bSupportChargingCurrent = "VZW".equals(Config.getOperator());
        Log.d("jw ", "battery :" + battery);

        int plugType;
        if (bSupportChargingCurrent) {
            plugType = intent.getIntExtra(LGIntent.EXTRA_BATTERY_PLUGGED, 0);
        } else {
            plugType = intent.getIntExtra("plugged", 0);
        }
        int status = intent.getIntExtra(LGIntent.EXTRA_BATTERY_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN);

        boolean isPluged = plugType > 0;

        if (bSupportChargingCurrent) {
            int high_temperature_min = 430;
            int low_temperature_min = -80;

            int mBatteryTemperature = intent.getIntExtra("temperature", 0);
            int mChargingCurrent = intent.getIntExtra(LGIntent.EXTRA_CHARGING_CURRENT, 0);
            if (isPluged) {
                if (mBatteryTemperature > high_temperature_min
                        || mBatteryTemperature < low_temperature_min) {
                    return getChargingBatteryImgId(battery);
                } else {
                    if (mChargingCurrent == 2) { // CHARGING_CURRENT_INCOMPATIBLE_CHARGING
                        return getIncompatibleBatteryImgId(battery);
                    } else if (mChargingCurrent == 3) { // CHARGING_CURRENT_UNDER_CURRENT_CHARGING
                        return getChargingBatteryImgId(battery);
                    } else if (mChargingCurrent == 4) { // CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
                        return getIncompatibleBatteryImgId(battery);
                    } else if (mChargingCurrent == 5) { // CHARGING_CURRENT_LLK_NOT_CHARGING
                        return getIncompatibleBatteryImgId(battery);
                    } else {
                        if (status == BatteryManager.BATTERY_STATUS_FULL) {
                            return R.drawable.img_homescreen_battery_not_100_vzw;
                        } else {
                            return getChargingBatteryImgId(battery);
                        }
                    }
                }
            } else {
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    return getChargingBatteryImgId(battery);
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    return getDischargingBatteryImgId(battery);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    return getIncompatibleBatteryImgId(battery);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    return R.drawable.img_homescreen_battery_not_100_vzw;
                } else {
                    return R.drawable.img_homescreen_battery_unknown;
                }
            }
        } else {

            if (Utils.isUI_4_1_model(mContext)) {
                return getDischargingBatteryImgId(battery);
            }

            if (battery >= 98) {
                return R.drawable.img_homescreen_battery_100;
            }
            else if (battery >= 93) {
                return R.drawable.img_homescreen_battery_95;
            }
            else if (battery >= 88) {
                return R.drawable.img_homescreen_battery_90;
            }
            else if (battery >= 83) {
                return R.drawable.img_homescreen_battery_85;
            }
            else if (battery >= 78) {
                return R.drawable.img_homescreen_battery_80;
            }
            else if (battery >= 73) {
                return R.drawable.img_homescreen_battery_75;
            }
            else if (battery >= 68) {
                return R.drawable.img_homescreen_battery_70;
            }
            else if (battery >= 63) {
                return R.drawable.img_homescreen_battery_65;
            }
            else if (battery >= 58) {
                return R.drawable.img_homescreen_battery_60;
            }
            else if (battery >= 53) {
                return R.drawable.img_homescreen_battery_55;
            }
            else if (battery >= 48) {
                return R.drawable.img_homescreen_battery_50;
            }
            else if (battery >= 43) {
                return R.drawable.img_homescreen_battery_45;
            }
            else if (battery >= 38) {
                return R.drawable.img_homescreen_battery_40;
            }
            else if (battery >= 33) {
                return R.drawable.img_homescreen_battery_35;
            }
            else if (battery >= 28) {
                return R.drawable.img_homescreen_battery_30;
            }
            else if (battery >= 21) {
                return R.drawable.img_homescreen_battery_25;
            }
            else if (battery >= 16) {
                return R.drawable.img_homescreen_battery_20;
            }
            else if (battery >= 13) {
                return R.drawable.img_homescreen_battery_15;
            }
            else if (battery >= 8) {
                return R.drawable.img_homescreen_battery_10_vzw;
            }
            else if (battery >= 3) {
                return R.drawable.img_homescreen_battery_05_vzw;
            }
            else {
                return R.drawable.img_homescreen_battery_bar_s_00;
            }
        }
    }

    public int getIncompatibleBatteryImgId(int battery) {
        if (battery >= 98) {
            return R.drawable.img_homescreen_battery_incompatible_100_vzw;
        }
        else if (battery >= 93) {
            return R.drawable.img_homescreen_battery_incompatible_95_vzw;
        }
        else if (battery >= 88) {
            return R.drawable.img_homescreen_battery_incompatible_90_vzw;
        }
        else if (battery >= 83) {
            return R.drawable.img_homescreen_battery_incompatible_85_vzw;
        }
        else if (battery >= 78) {
            return R.drawable.img_homescreen_battery_incompatible_80_vzw;
        }
        else if (battery >= 73) {
            return R.drawable.img_homescreen_battery_incompatible_75_vzw;
        }
        else if (battery >= 68) {
            return R.drawable.img_homescreen_battery_incompatible_70_vzw;
        }
        else if (battery >= 63) {
            return R.drawable.img_homescreen_battery_incompatible_65_vzw;
        }
        else if (battery >= 58) {
            return R.drawable.img_homescreen_battery_incompatible_60_vzw;
        }
        else if (battery >= 53) {
            return R.drawable.img_homescreen_battery_incompatible_55_vzw;
        }
        else if (battery >= 48) {
            return R.drawable.img_homescreen_battery_incompatible_50_vzw;
        }
        else if (battery >= 43) {
            return R.drawable.img_homescreen_battery_incompatible_45_vzw;
        }
        else if (battery >= 38) {
            return R.drawable.img_homescreen_battery_incompatible_40_vzw;
        }
        else if (battery >= 33) {
            return R.drawable.img_homescreen_battery_incompatible_35_vzw;
        }
        else if (battery >= 28) {
            return R.drawable.img_homescreen_battery_incompatible_30_vzw;
        }
        else if (battery >= 21) {
            return R.drawable.img_homescreen_battery_incompatible_25_vzw;
        }
        else if (battery >= 16) {
            return R.drawable.img_homescreen_battery_incompatible_20_vzw;
        }
        else if (battery >= 13) {
            return R.drawable.img_homescreen_battery_incompatible_15_vzw;
        }
        else if (battery >= 8) {
            return R.drawable.img_homescreen_battery_incompatible_10_vzw;
        }
        else {
            return R.drawable.img_homescreen_battery_incompatible_05_vzw;
        }
    }

    public int getChargingBatteryImgId(int battery) {
        if (battery >= 98) {
            return R.drawable.img_homescreen_battery_not_100_vzw;
        }
        else if (battery >= 93) {
            return R.drawable.img_homescreen_battery_not_95_vzw;
        }
        else if (battery >= 88) {
            return R.drawable.img_homescreen_battery_not_90_vzw;
        }
        else if (battery >= 83) {
            return R.drawable.img_homescreen_battery_not_85_vzw;
        }
        else if (battery >= 78) {
            return R.drawable.img_homescreen_battery_not_80_vzw;
        }
        else if (battery >= 73) {
            return R.drawable.img_homescreen_battery_not_75_vzw;
        }
        else if (battery >= 68) {
            return R.drawable.img_homescreen_battery_not_70_vzw;
        }
        else if (battery >= 63) {
            return R.drawable.img_homescreen_battery_not_65_vzw;
        }
        else if (battery >= 58) {
            return R.drawable.img_homescreen_battery_not_60_vzw;
        }
        else if (battery >= 53) {
            return R.drawable.img_homescreen_battery_not_55_vzw;
        }
        else if (battery >= 48) {
            return R.drawable.img_homescreen_battery_not_50_vzw;
        }
        else if (battery >= 43) {
            return R.drawable.img_homescreen_battery_not_45_vzw;
        }
        else if (battery >= 38) {
            return R.drawable.img_homescreen_battery_not_40_vzw;
        }
        else if (battery >= 33) {
            return R.drawable.img_homescreen_battery_not_35_vzw;
        }
        else if (battery >= 28) {
            return R.drawable.img_homescreen_battery_not_30_vzw;
        }
        else if (battery >= 21) {
            return R.drawable.img_homescreen_battery_not_25_vzw;
        }
        else if (battery >= 16) {
            return R.drawable.img_homescreen_battery_not_20_vzw;
        }
        else if (battery >= 13) {
            return R.drawable.img_homescreen_battery_not_15_vzw;
        }
        else if (battery >= 8) {
            return R.drawable.img_homescreen_battery_not_10_vzw;
        }
        else {
            return R.drawable.img_homescreen_battery_not_05_vzw;
        }
    }

    public int getDischargingBatteryImgId(int battery) {

        if ("VZW".equals(Config.getOperator()) && battery < 21) {
            if (battery >= 16) {
                return R.drawable.img_homescreen_battery_20_vzw;
            }
            else if (battery >= 13) {
                return R.drawable.img_homescreen_battery_15_vzw;
            }
            else if (battery >= 8) {
                return R.drawable.img_homescreen_battery_10_vzw;
            } else {
                return R.drawable.img_homescreen_battery_05_vzw;
            }
        } else if ("ATT".equals(Config.getOperator()) && battery < 21) {
            if (battery >= 16) {
                return R.drawable.img_homescreen_battery_15;
            }
            else if (battery >= 11) {
                return R.drawable.img_homescreen_battery_10;
            }
            else if (battery >= 5) {
                return R.drawable.img_homescreen_battery_10_vzw;
            } else {
                return R.drawable.img_homescreen_battery_05_vzw;
            }
        } else {
            if (battery >= 98) {
                return R.drawable.img_homescreen_battery_100;
            }
            else if (battery >= 93) {
                return R.drawable.img_homescreen_battery_95;
            }
            else if (battery >= 88) {
                return R.drawable.img_homescreen_battery_90;
            }
            else if (battery >= 83) {
                return R.drawable.img_homescreen_battery_85;
            }
            else if (battery >= 78) {
                return R.drawable.img_homescreen_battery_80;
            }
            else if (battery >= 73) {
                return R.drawable.img_homescreen_battery_75;
            }
            else if (battery >= 68) {
                return R.drawable.img_homescreen_battery_70;
            }
            else if (battery >= 63) {
                return R.drawable.img_homescreen_battery_65;
            }
            else if (battery >= 58) {
                return R.drawable.img_homescreen_battery_60;
            }
            else if (battery >= 53) {
                return R.drawable.img_homescreen_battery_55;
            }
            else if (battery >= 48) {
                return R.drawable.img_homescreen_battery_50;
            }
            else if (battery >= 43) {
                return R.drawable.img_homescreen_battery_45;
            }
            else if (battery >= 38) {
                return R.drawable.img_homescreen_battery_40;
            }
            else if (battery >= 33) {
                return R.drawable.img_homescreen_battery_35;
            }
            else if (battery >= 28) {
                return R.drawable.img_homescreen_battery_30;
            }
            else if (battery >= 21) {
                return R.drawable.img_homescreen_battery_25;
            }
            else if (battery >= 16) {
                return R.drawable.img_homescreen_battery_20;
            }
            else if (battery >= 13) {
                return R.drawable.img_homescreen_battery_15_vzw;
            }
            else if (battery >= 8) {
                return R.drawable.img_homescreen_battery_10_vzw;
            } else {
                return R.drawable.img_homescreen_battery_05_vzw;
            }
        }

    }

    /* LGE_CHANGE_S, Power save restore */
    public void doRestore() {

        sendToOthers(null, false); // yonguk.kim 2012 Send broadcast to others

        // lyang2000 2012/03/21 BT process oneself.
        //doBt();
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Start restore");

                // lyang2000 2012/03/13 Wi-Fi process oneself.
                //doWifi();
                doSync();
                //doGps();
                //if (NfcAdapter.getDefaultAdapter(mContext) != null) {
                //    doNfc();
                //}
                //doAnimations();
                doBrightness();
                doTouch();
                doScreenTimeout();
                doFrontLed();
                doCpu();
                // jongtak0920.kim Add Notification Flash
                //                doNotificationFlash();
                // jongtak0920.kim Add Emotional LED
                doEmotionalLED();
                // jongtak0920.kim Add Auto-adjust screen tone
                doAutoScreenTone();
                //jw
                doQuickCase();
            }
        }.start();

        //Toast.makeText(mContext, "Restore", Toast.LENGTH_SHORT).show();
    }

    private void sendToOthers(String name, boolean activate) {
        Log.d(TAG, "sendToOthers, Power save mode: " + name + " "
                + (activate ? "activate" : "restore"));
        Intent intent = new Intent(PowerSave.ACTION_POWERSAVE_ACTIVATION_TO_OTHERS);
        intent.putExtra(PowerSave.EXTRA_POWERSAVE_ACTIVATION, activate ? 1 : 0);
        if (name != null) {
            intent.putExtra(PowerSave.EXTRA_POWERSAVE_NAME, name);
        }
        mContext.sendBroadcast(intent);
    }

    private void doQuickCase() {
        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover") == true
                && Settings.System.getInt(
                        mContentResolver, "power_save_quick_case_restore", 0) != 0) {
            Log.d("jw", "doPowerSave");

            Settings.System.putInt(
                    mContentResolver, "power_save_quick_case_restore", 0);

            int restoreType = Settings.System.getInt(
                    mContentResolver, "power_save_quick_case_restore_type", 200);

            if (restoreType == 0) {
                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.QUICK_COVER_ENABLE, 1);
                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            } else if (restoreType == 2) {
                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
                int lolli = Settings.System.getInt(
                        mContentResolver, "power_save_quick_case_restore_lolli_type", 200);
                Settings.Global.putInt(mContentResolver,
                        SettingsConstants.Global.LOLLIPOP_COVER_TYPE, lolli);
            }

            Settings.Global.putInt(
                    mContentResolver, SettingsConstants.Global.COVER_TYPE, restoreType);

            Log.d("jw", "doPowerSave");
        }
    }

    private void doWifi() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI_RESTORE, 0) != 0) {

            WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            if (WifiManager.WIFI_STATE_ENABLED != wifiManager.getWifiState()) {
                wifiManager.setWifiEnabled(true);
            }

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI_RESTORE, 0);
        }
    }

    private void doSync() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC_RESTORE, 0) != 0) {

            if (!ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(true);
            }

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC_RESTORE, 0);
        }
    }

    private void doBt() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_BT_RESTORE, 0) != 0) {

            LocalBluetoothManager instance = LocalBluetoothManager.getInstance(mContext);
            if (instance != null) {
                LocalBluetoothAdapter localAdapter = instance.getBluetoothAdapter();
                if (BluetoothAdapter.STATE_ON != localAdapter.getBluetoothState()) {
                    localAdapter.setBluetoothEnabled(true);
                }
            }

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_BT_RESTORE, 0);
        }
    }

    /*
    private void doGps() {
        if(Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_GPS_RESTORE, 0) != 0) {

            boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                    mContentResolver, LocationManager.GPS_PROVIDER);
            if(!gpsEnabled) {
                Settings.Secure.setLocationProviderEnabled(mContentResolver,
                        LocationManager.GPS_PROVIDER, true);
            }
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_GPS_RESTORE, 0);
        }
    }
    */
    private void doNfc() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_NFC_RESTORE, 0) != 0) {

            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
            if (nfcAdapter != null) {
                if (!nfcAdapter.isEnabled()) {
                    nfcAdapter.enable();
                }
            }
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_NFC_RESTORE, 0);
        }
    }

    /*
    private void doAnimations() {
        int animationScaleRestore = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_ANIMATIONS_RESTORE, -1);

        if(animationScaleRestore > -1) {

            IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

            if( mWindowManager == null ) {
                return;
            }

            float[] mAnimationScales = null;
            try {
                mAnimationScales = mWindowManager.getAnimationScales();
            } catch(RemoteException e) {
            }

            String[] animationValue = {"00", "01", "11"};

            try {
                int value = Integer.parseInt(animationValue[animationScaleRestore]);
                if (mAnimationScales != null) {
                    if (mAnimationScales.length >= 1) {
                        mAnimationScales[0] = value%10;
                    }
                    if (mAnimationScales.length >= 2) {
                        mAnimationScales[1] = (value/10)%10;
                    }
                    try {
                        mWindowManager.setAnimationScales(mAnimationScales);
                    } catch (RemoteException e) {
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist animation setting", e);
            }
            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_ANIMATIONS_RESTORE, -1);
        }
    }
    */

    private void doCpu() {
        int restore_cpu = Settings.System.getInt(mContentResolver,
                SettingsConstants.System.POWER_SAVE_CPU_RESTORE, -1);
        Log.d(TAG, "doCpu() - restore_cpu : " + restore_cpu);
        if (restore_cpu >= 0) {
            Settings.System
                    .putInt(mContentResolver, SettingsConstants.System.ECO_MODE, restore_cpu);
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.POWER_SAVE_CPU_RESTORE, -1);
        }
    }

    private void doBrightness() {
        int brightnessRestore = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE, -1);

        Log.d(TAG, "doBrightness() - brightnessRestore : " + brightnessRestore);

        if (brightnessRestore > -1 && brightnessRestore >= MINIMUM_BACKLIGHT) {
            setBrightness(brightnessRestore);
            Settings.System.putInt(
                    mContentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessRestore);

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE, -1); // Since not apply.
            if (noapplyMultiALC()) {
                if (Settings.System.getInt(mContentResolver,
                        "power_save_auto_brightness", -1) == 1) {
                    Log.d(TAG, "DCM TMO Remove Multi ALC");
                    Settings.System.putInt(mContentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
                } else {
                    Settings.System.putInt(mContentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
                }
            }
        }
    }

    private void doTouch() {
        if (Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH_RESTORE, 0) != 0) {
            if (Settings.System.getInt(mContentResolver,
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 1) {
                Settings.System
                        .putInt(mContentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
            }

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH_RESTORE, 0);
        }
    }

    private void doScreenTimeout() {
        int screenTimeoutRestore = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_RESTORE, -1);

        Log.d(TAG, "doScreenTimeout - screenTimeoutRestore : " + screenTimeoutRestore);

        //if(screenTimeoutRestore > -1) {
        if (screenTimeoutRestore >= 15000) { // Minimum value : 15000
            Settings.System.putInt(
                    mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeoutRestore);

            Settings.System.putInt(
                    mContentResolver, SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_RESTORE,
                    -1); // Since not apply.
        }
    }

    private void doFrontLed() {
        int frontLedRestore = Settings.System.getInt(
                mContentResolver, SettingsConstants.System.POWER_SAVE_FRONT_LED_RESTORE, -1);

        Log.d(TAG, "doFrontLed - frontLedRestore : " + frontLedRestore);

        if (frontLedRestore > -1) {

            Settings.Secure.putInt(
                    mContentResolver, SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT,
                    frontLedRestore);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.POWER_SAVE_FRONT_LED_RESTORE, -1); // Since not apply.

        }
    }

    /* LGE_CHANGE_E, Power save restore */

    // jongtak0920.kim Add Notification Flash
    private void doNotificationFlash() {
        int restoreValue = Settings.System.getInt(mContentResolver,
                POWER_SAVE_NOTIFICATION_FLASH_RESTORE, -1);
        Log.d(TAG, "doNotificationFlash() - restoreValue : " + restoreValue);
        if (restoreValue >= 0) {
            Settings.System.putInt(mContentResolver, SettingsConstants.System.FRONT_KEY_ALL,
                    restoreValue);
            Settings.System.putInt(mContentResolver, POWER_SAVE_NOTIFICATION_FLASH_RESTORE, -1);
        }
    }

    private void doEmotionalLED() {
        int restoreValue = Settings.System.getInt(mContentResolver,
                POWER_SAVE_EMOTIONAL_LED_RESTORE, -1);
        Log.d(TAG, "doEmotionalLED() - restoreValue : " + restoreValue);
        if (restoreValue >= 0) {
            Settings.System.putInt(mContentResolver, LGE_NOTIFICATION_LIGHT_PULSE, restoreValue);
            Settings.System.putInt(mContentResolver, POWER_SAVE_EMOTIONAL_LED_RESTORE, -1);
        }
    }

    private void doAutoScreenTone() {
        int restoreValue = Settings.System.getInt(mContentResolver,
                POWER_SAVE_AUTO_SCREEN_TONE_RESTORE, -1);
        Log.d(TAG, "doAutoScreenTone() - restoreValue : " + restoreValue);
        if (restoreValue >= 0) {
            Settings.System.putInt(mContentResolver, PLC_MODE_SET, restoreValue);
            Settings.System.putInt(mContentResolver, POWER_SAVE_AUTO_SCREEN_TONE_RESTORE, -1);
        }
    }

    public static int getCurrentBrightness() {
        String path = "sys/class/leds/lcd-backlight/brightness";
        String tegrapath = "sys/class/backlight/pwm-backlight/brightness";

        BufferedReader inFile = null;
        FileReader fileReader = null;
        String currentBrightness = "";
        Log.d("jw", "SystemProperties : " + SystemProperties.get("ro.board.platform"));
        try {

            if ("tegra".equals(SystemProperties.get("ro.board.platform"))) {
                fileReader = new FileReader(tegrapath);
                inFile = new BufferedReader(fileReader);
                Log.d("jw", "tegra inFile.readLine()");
                if (inFile != null) {
                    currentBrightness = inFile.readLine();
                }
            } else {
                fileReader = new FileReader(path);
                inFile = new BufferedReader(fileReader);
                Log.d("jw", "inFile.readLine()");
                if (inFile != null) {
                    currentBrightness = inFile.readLine();
                }
            }
            Log.d(TAG, "Current brightness -> " + currentBrightness);

        } catch (Exception e) {
            Log.e(TAG, "Brightness file read fail");
        } finally {
            try {
                if (inFile != null) {
                    inFile.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (Exception e) {
                /* return value; */
            }
        }

        if (currentBrightness != null && !"".equals(currentBrightness)) {
            return Integer.parseInt(currentBrightness);
        }
        else {
            return Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS,
                    MAXIMUM_BACKLIGHT);
            //return -1;
        }
    }

    public static boolean checkPowerSaveItems() {
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC, 0) != 0) {
            return true;
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI, 1) != 0) {
            return true;
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_BT, 1) != 0) {
            return true;
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH, 1) != 0) {
            return true;
        }
        if (Settings.System.getInt(mContentResolver,
                SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST, 1) != 0) {
            return true;
        }
        if (Settings.System.getInt(mContentResolver,
                SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST, 1) != 0) {
            return true;
        }
        return false;
    }

    public boolean noapplyMultiALC() {
        if ("DCM".equals(Config.getOperator())
                || ("TMO".equals(Config.getOperator()) && "US".equals(Config.getCountry()))) {
            return true;
        }
        return false;
    }

    public void onBatterySaverEnabled(boolean setting_enabled) {
        Log.d("jw", "setting_enabled " + setting_enabled);
        Bundle log = new Bundle();
        log.putLong("action", mACTION_FEATURE_SETTING_CHANGED);

        log.putString("feature_name", "ugc_battery_power_saver");
        log.putInt("setting", setting_enabled ? 1 : 0);

        Intent i = new Intent("com.lge.mrg.service.intent.action.APPEND_USER_LOG");
		i.setPackage("com.lge.mrg.service");
        i.putExtras(log);
        mContext.startService(i);
    }

    public void onBatterySaverModeValue(int value) {
        Log.d("jw", "onBatterySaverModeValue " + value);

        Bundle log = new Bundle();
        log.putLong("action", mACTION_FEATURE_SETTING_CHANGED);

        log.putString("feature_name", "ugc_battery_power_saver_level");
        switch (value) {
        case 100:
            log.putInt("setting", 0);
            break;
        case 10:
            log.putInt("setting", value);
            break;
        case 20:
            log.putInt("setting", value);
            break;
        case 30:
            log.putInt("setting", value);
            break;
        case 50:
            log.putInt("setting", value);
            break;
        default:
            break;

        }

        Intent i = new Intent("com.lge.mrg.service.intent.action.APPEND_USER_LOG");
		i.setPackage("com.lge.mrg.service");
        i.putExtras(log);
        mContext.startService(i);
    }

}
