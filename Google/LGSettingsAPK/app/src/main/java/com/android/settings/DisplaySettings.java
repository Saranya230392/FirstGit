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

package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.preference.PreferenceCategory;

import com.android.internal.view.RotationPolicy;
import com.android.settings.DreamSettings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.DoubleTitleListPreference;

import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.view.View;
import android.graphics.Color;
import android.graphics.Typeface;

import com.android.settings.lge.BrightnessPreferenceTablet;
import com.android.settings.lge.NightModeInfo;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

import android.content.DialogInterface.OnClickListener;
import android.os.SystemProperties;
import android.app.DialogFragment;
import android.os.Message;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.widget.TextView;
import android.preference.SwitchPreference;
import android.os.UserHandle;
import android.os.UserManager;
import android.content.pm.UserInfo;

import com.android.settings.ModelFeatureUtils;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_HOME_CATEGORY = "key_home_category";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_BRIGHTNESS_TABLET = "brightness_tablet";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_ASPECT_RATIO_CORRECTION = "aspect_ratio_correction";
    private static final String KEY_FRONTKEY_LIGHT_SETTINGS = "frontkey_led_timeout";
    private static final String KEY_FRONT_TOUCH_KEY = "front_touch_key";
    private static final String KEY_NOTIFICATION_FLASH_ONE_COLOR = "notification_flash_one_color";
    private static final String KEY_MOTION_SENSOR_CALIBRATION = "motion_sensor_calibration";
    private static final String KEY_FONT_TYPE = "font_type";
    private static final String KEY_EMOTIONAL_LED = "emotional_led";
    private static final String KEY_KEEPSCREENON = "keepscreenon";
    private static final String KEY_KEEPSCREENON_UI_4_1 = "keepscreenon_ui_4_1";
    private static final String KEY_KEEPVIDEOON = "keepvideoon";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_WIFI_DISPLAY = "wifi_display";
    private static final String KEY_SCREEN_OFF_EFFECT_POPUP = "screen_off_effect_popup";
    private static final String KEY_SCREEN_MODE = "screen_mode";
    private static final String KEY_PLC_MODE = "plc_mode";
    private static final String KEY_KNOCK_ON = "knock_on";
    private static final String KEY_SCREEN_CAPTURE = "screen_capture";

    private static final String KEY_KEEPSCREENON_UI_4_2 = "keepscreenon_ui_4_2";
    private static final String KEY_ACCELEROMETER_UI_4_2 = "accelerometer_ui_4_2";
    private static final String KEY_FRONT_TOUCH_KEY_UI_4_2 = "front_touch_key_ui_4_2";
    private static final String KEY_SCREEN_OFF_EFFECT_POPUP_4_2 = "screen_off_effect_popup_ui_4_2";
    private static final String KEY_SCREEN_SAVER_4_2 = "screensaver_ui_4_2";

    private static final String KEY_SCREEN_CATEGORY = "screen_category";
    private static final String KEY_FONT_CATEGORY = "font_category";
    private static final String KEY_SMART_CATEGORY = "smart_category";
    private static final String KEY_ADVANCED_CATEGORY = "advanced_category";
    private static final int LCD_DENSITY_MDPI = 160;

    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;
    // [seungyeop.yeom][2013-12-17] add function of quick case
    private static final String KEY_QUICK_CASE = "quick_case";

    // [seungyeop.yeom][2014-01-27] add function of quick cover
    private static final String KEY_QUICK_COVER = "quick_cover";
    private static final String KNOCKON_LOCK_SET = "KNOCKON_LOCK_SET";
    private DisplayManager mDisplayManager;

    private CheckBoxPreference mAccelerometer;
    private WarnedListPreference mFontSizePref;
    private CheckBoxPreference mNotificationPulse;
    private ListPreference mFontTypePref;

    private final Configuration mCurConfig = new Configuration();
    private static final int MSG_CALL_FONTS = 109;

    // private static final String FONT_PREFERENCE = "display.font.preferences";
    private PreferenceCategory mHomeCategory;
    private PreferenceCategory mScreenCategory;
    private PreferenceCategory mFontCategory;
    private PreferenceCategory mSmartCategory;
    private PreferenceCategory mAdvancedCategory;
    private BrightnessPreference mBrightnessPreference;
    private BrightnessPreferenceTablet mBrightnessPreferenceTablet;
    private Preference mMotionSensorCalibration;
    private boolean bEASChecked = false;
    private Preference mAspectRatioCorrection;
    private Preference mFrontKeyLight;
    private Preference mFrontTouchKey;
    private Preference mScreenOffEffect; // [seungyeop.yeom] 2013-05-15
    private Preference mScreenMode; // [seungyeop.yeom] 2013-05-15
    private CheckBoxPreference mPlcMode; // [seungyeop.yeom] 2013-06-11
    private CheckBoxPreference mKnockOn;
    private Preference mScreenCapture;
    private DoubleTitleListPreference mScreenTimeoutPreference;
    private SwitchPreference mNotificationFlashOneColor;
    private SwitchPreference mEmotionLED;
    // CFONT_SERVER_SERVICE_JB Font server services.
    private static final String KEY_FONT_SETTINGS = "font_settings";
    private static final String KEY_FONT_SETTINGS_KK = "font_settings_kk";
    private boolean mIsSupportFontDelete = false;
    private FontSettingsPreference mFontSettingsPref;
    private Preference mFontSettingsPref_kk;
    // CFONT_SERVER_SERVICE_JB_END
    private CheckBoxPreference mKeepScreenOn;
    private CheckBoxPreference mKeepVideoOn;
    private SwitchPreference mScreenSaverPreference;

    private WifiDisplayStatus mWifiDisplayStatus;
    private Preference mWifiDisplayPreference;
    private boolean isSilentModeChecked = true;
    private DreamBackend mBackend;

    // [seungyeop.yeom] 2013-06-06
    private AlertDialog screenDialog = null;
    private AlertDialog videoDialog = null;
    private boolean isDialogCheck = false;
    private boolean isVideoDialogCheck = false;
    private CheckBox donotshow = null;

    // [NightMode]
    private NightModeInfo mNightModeInfo;

    // [seungyeop.yeom][2013-12-18] add function of quick case
    private Preference mQuickCase;
    // [seungyeop.yeom][2014-01-27] add function of quick cover
    // private Preference mQuickCover;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    
    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateAccelerometerRotationCheckbox();
        }
    };
    // LGE_CHANGE_S [k.cho@lge.com] 2012-05-25, KeepScreenOn
    private ContentObserver mKeepScreenOnObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateKeepScreenOnCheckbox();
        }
    };
    // LGE_CHANGE_E [k.cho@lge.com] 2012-05-25, KeepScreenOn

    private ContentObserver mBrightnessObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mBrightnessPreference.selectsetSubtitle(false);
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mBrightnessPreference.selectsetSubtitle(false);
        }
    };

    private ContentObserver mBrightnessTabletObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mBrightnessPreferenceTablet != null) {
                mBrightnessPreferenceTablet.onBrightnessChanged();
            }
        }
    };
    private ContentObserver mBrightnessModeTabletObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mBrightnessPreferenceTablet != null) {
                mBrightnessPreferenceTablet.onBrightnessModeChanged();
            }
        }
    };
    private ContentObserver mNightModeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateNightModeValue();
            mBrightnessPreference.selectsetSubtitle(false);
        }
    };

    private ContentObserver mNotificationFlashObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateNotificationFlash();
        }
    };

    private void updateNightModeValue() {
        boolean checkNightValue = Settings.System.getInt(getContentResolver(),
                "check_night_mode", 0) > 0;
        if (checkNightValue) {
            Log.d("jw", "night on");
            mNightModeInfo.startNotification();
        } else {
            Log.d("jw", "night off");
            mNightModeInfo.endNotification();
        }
    }

    private ContentObserver mScreenTimeoutObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateScreenTimeOutValue();
        }
    };

    // Define Notification Observer
    // 2013-05-09
    // A 4Team 2Part Yeom Seungyeop Y
    private ContentObserver mNotificationObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateNotificationCheckbox();
        }
    };

    // Define Plc mode Observer
    // 2013-06-11
    // A 4Team 2Part Yeom Seungyeop Y
    private ContentObserver mPlcModeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updatePlcModeOnCheckbox();
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_CALL_FONTS:
                callDisplayFont();
                break;
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNightModeInfo = new NightModeInfo(getActivity());
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.display_settings);

        mHomeCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_HOME_CATEGORY);
        mScreenCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_SCREEN_CATEGORY);
        mFontCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_FONT_CATEGORY);
        mSmartCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_SMART_CATEGORY);
        mAdvancedCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_ADVANCED_CATEGORY);

        getActivity()
                .getApplicationContext()
                .getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                        true, mBrightnessObserver);
        getActivity()
                .getApplicationContext()
                .getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                        true, mBrightnessTabletObserver);

        mNotificationPulse = (CheckBoxPreference)findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null
                && Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_intrusiveNotificationLed, 
                           "com.android.internal.R.bool.config_intrusiveNotificationLed") == false) {
            if (mFontCategory != null) {
                mFontCategory
                        .removePreference(findPreference(KEY_NOTIFICATION_PULSE));
            }
        } else {
            try {
                if (mNotificationPulse != null) {
                    mNotificationPulse
                            .setChecked(Settings.System.getInt(resolver,
                                    Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
                    mNotificationPulse.setOnPreferenceChangeListener(this);
                }
            } catch (SettingNotFoundException snfe) {
                Log.e(TAG, Settings.System.NOTIFICATION_LIGHT_PULSE
                        + " not found");
            }
        }
        mDisplayManager = (DisplayManager)getActivity().getSystemService(
                Context.DISPLAY_SERVICE);
        mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
        mWifiDisplayPreference = (Preference)findPreference(KEY_WIFI_DISPLAY);
        if (mFontCategory != null) {
            mFontCategory.removePreference(findPreference(KEY_WIFI_DISPLAY));
        }
        mWifiDisplayPreference = null;
        mBackend = new DreamBackend(getActivity());
        createAddedMenu();
        mIsFirst = true;
    }

    private void createAddedMenu() {
        setupBrightness();
        setupScreenTimeout();
        setupKeepScreenOnVideoOn();
        setupKnockOn();
        setupAutoRotate();
        setupScreenSaver();
        setupFontType();
        setupFontSize();
        setupAutofitScreen();
        setupFrontkeylight();
        setupFrontTouchKey();
        setupNotificationFlash();
        setupHomeButtonLED();
        setupMotionSensorCalibration();
        setupScreenOffEffect();
        setupScreenMode();
        setupPlcMode();
        setupScreenCapture();
        setupQuickCase();

        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            setChangeCategory();
        }

        if (!Utils.supportSplitView(getActivity())
            || (Utils.supportSplitView(getActivity())
                && ("VZW".equals(Config.getOperator()))
                    || "ATT".equals(Config.getOperator()))) {
            this.getPreferenceScreen().removePreference(mHomeCategory);
        }

        if (mAdvancedCategory != null
                && mAdvancedCategory.getPreferenceCount() == 0) {
            this.getPreferenceScreen().removePreference(mAdvancedCategory);
        }
    }

    private void setChangeCategory() {
        if (mHomeCategory != null) {
            mHomeCategory.setOrder(1);
        }
        if (mFontCategory != null) {
            mFontCategory.setOrder(2);
        }
        if (mScreenCategory != null) {
            mScreenCategory.setOrder(3);
            mScreenCategory.setTitle(R.string.display_basic_settings);
        }
        if (mSmartCategory != null) {
            mSmartCategory.setOrder(4);
        }
        if (mAdvancedCategory != null) {
            mAdvancedCategory.setOrder(5);
            mAdvancedCategory.setTitle(R.string.display_advanced_settings);
        }
    }

    private void setupBrightness() {
        mBrightnessPreference = (BrightnessPreference)findPreference(KEY_BRIGHTNESS);
        mBrightnessPreferenceTablet = (BrightnessPreferenceTablet)findPreference(KEY_BRIGHTNESS_TABLET);
        if (Utils.supportSplitView(getActivity())) {
            if (mScreenCategory != null) {
                mScreenCategory.removePreference(mBrightnessPreference);
            }
        } else {
            if (mScreenCategory != null) {
                mScreenCategory.removePreference(mBrightnessPreferenceTablet);
            }
        }
    }

    private void setupScreenTimeout() {
        mScreenTimeoutPreference = (DoubleTitleListPreference)findPreference(KEY_SCREEN_TIMEOUT);
        mScreenTimeoutPreference
                .setMainTitle(getString(R.string.sp_screen_timeout_NORMAL));
        mScreenTimeoutPreference
                .setSubTitle(getString(R.string.sp_screen_timeout_sub_title_NORMAL));
        final long currentTimeout = Settings.System.getLong(
                getContentResolver(), SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);

        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled") == true
                && Settings.System.getInt(getContentResolver(),
                        "screen_timeout_test", 0) == 0) {
            mScreenTimeoutPreference
                    .setEntries(R.array.screen_timeout_entries_oled_ex);
            mScreenTimeoutPreference
                    .setEntryValues(R.array.screen_timeout_values_oled);
        } else {
            if ("VZW".equals(Config.getOperator())) {
                mScreenTimeoutPreference
                        .setEntries(R.array.screen_timeout_entries4_ex);
                mScreenTimeoutPreference
                        .setEntryValues(R.array.screen_timeout_values4);
            } else {
                mScreenTimeoutPreference
                        .setEntries(R.array.sp_screen_timeout_entries3_NORMAL_ex);
                mScreenTimeoutPreference
                        .setEntryValues(R.array.screen_timeout_values3);
            }
        }

        disableUnusableTimeouts(mScreenTimeoutPreference);
        Log.d(TAG, "currentScreentTimeout: " + currentTimeout);
        if (!isContainfromValues(String.valueOf(currentTimeout))
                && (bEASChecked == false)) {
            Log.d(TAG, "defaultScreenTimeoutChange: " + currentTimeout);
            Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT,
                    (int)FALLBACK_SCREEN_TIMEOUT_VALUE);
            final long changeTimeout = Settings.System.getLong(
                    getContentResolver(), SCREEN_OFF_TIMEOUT,
                    FALLBACK_SCREEN_TIMEOUT_VALUE);
            mScreenTimeoutPreference.setValue(String.valueOf(changeTimeout));
            updateTimeoutPreferenceDescription(changeTimeout);

        } else {
            updateTimeoutPreferenceDescription(currentTimeout);
        }
    }

    private void setupKeepScreenOnVideoOn() {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mKeepScreenOn = (CheckBoxPreference)findPreference(KEY_KEEPSCREENON_UI_4_2);
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_KEEPSCREENON_UI_4_1));
            }
        } else {
            if ("true".equals(ModelFeatureUtils.getFeature(getActivity(),
                    "usesmartscreencategory"))) {
                mKeepScreenOn = (CheckBoxPreference)findPreference(KEY_KEEPSCREENON);
                if (mAdvancedCategory != null) {
                    mAdvancedCategory
                            .removePreference(findPreference(KEY_KEEPSCREENON_UI_4_1));
                }
                if (mScreenCategory != null) {
                    mScreenCategory
                            .removePreference(findPreference(KEY_KEEPSCREENON_UI_4_2));
                }
            } else {
                mKeepScreenOn = (CheckBoxPreference)findPreference(KEY_KEEPSCREENON_UI_4_1);
                if (mScreenCategory != null) {
                    mScreenCategory
                            .removePreference(findPreference(KEY_KEEPSCREENON_UI_4_2));
                }
            }
        }
        mKeepScreenOn.setPersistent(false);

        mKeepVideoOn = (CheckBoxPreference)findPreference(KEY_KEEPVIDEOON);
        if ("VZW".equals(Config.getOperator())) {
            mKeepVideoOn.setSummary(R.string.display_smart_video_summary);
        }
        mKeepVideoOn.setPersistent(false);

        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            this.getPreferenceScreen().removePreference(mSmartCategory);
            if (!Utils.checkPackage(mKeepScreenOn.getContext(),
                    "com.lge.keepscreenon")
                    || !Config.getFWConfigBool(getActivity(), com.lge.R.bool.support_wisescreen,
                           "com.lge.R.bool.support_wisescreen")) {
                if (mScreenCategory != null) {
                    mScreenCategory.removePreference(mKeepScreenOn);
                }
            }
        } else if ("true".equals(ModelFeatureUtils.getFeature(getActivity(),
                "usesmartscreencategory"))) {
            if (!Utils.checkPackage(mKeepScreenOn.getContext(),
                    "com.lge.keepscreenon")
                    || !Config.getFWConfigBool(getActivity(), com.lge.R.bool.support_wisescreen,
                           "com.lge.R.bool.support_wisescreen")) {
                this.getPreferenceScreen().removePreference(mSmartCategory);
            } else {
                if (!Utils.isSupportSmartVideo()) {
                    if (mSmartCategory != null) {
                        Log.d("YSY", "remove Category of SmartVideo");
                        mSmartCategory
                                .removePreference(findPreference(KEY_KEEPVIDEOON));
                    }
                }
            }
        } else {
            this.getPreferenceScreen().removePreference(mSmartCategory);
            if (!Utils.checkPackage(mKeepScreenOn.getContext(),
                    "com.lge.keepscreenon")
                    || !Config.getFWConfigBool(getActivity(), com.lge.R.bool.support_wisescreen,
                           "com.lge.R.bool.support_wisescreen")) {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory.removePreference(mKeepScreenOn);
                }
            }
        }
    }

    private void setupKnockOn() {
        mKnockOn = (CheckBoxPreference)findPreference(KEY_KNOCK_ON);
        if ("DCM".equals(Config.getOperator())) {
            mKnockOn.setTitle(R.string.gesture_title_turn_screen_on);
        }

        if (!isSupportKnockOn()) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mKnockOn);
            }
        }
    }

    private void setupAutoRotate() {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mAccelerometer = (CheckBoxPreference)findPreference(KEY_ACCELEROMETER_UI_4_2);
            if (mScreenCategory != null) {
                mScreenCategory
                        .removePreference(findPreference(KEY_ACCELEROMETER));
            }
        } else {
            mAccelerometer = (CheckBoxPreference)findPreference(KEY_ACCELEROMETER);
            if (mScreenCategory != null) {
                mScreenCategory
                        .removePreference(findPreference(KEY_ACCELEROMETER_UI_4_2));
            }
        }
        mAccelerometer.setPersistent(false);
        if (getActivity() != null) {
            try {
                getPackageManager().getApplicationInfo("com.lge.settings.easy",
                        0);
            } catch (NameNotFoundException e) {
                // TODO: handle exception
                mAccelerometer
                        .setSummary(R.string.sp_accelerometer_summary_NORMAL);
            }
        }
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            // If rotation lock is supported, then we do not provide this option
            // in
            // Display settings. However, is still available in Accessibility
            // settings,
            // if the device supports rotation.
            if (mScreenCategory != null) {
                mScreenCategory.removePreference(mAccelerometer);
            }
        }
    }

    private void setupScreenSaver() {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mScreenSaverPreference = (SwitchPreference)findPreference(KEY_SCREEN_SAVER_4_2);
            if (mScreenCategory != null) {
                mScreenCategory
                        .removePreference(findPreference(KEY_SCREEN_SAVER));
            }
        } else {
            mScreenSaverPreference = (SwitchPreference)findPreference(KEY_SCREEN_SAVER);
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_SCREEN_SAVER_4_2));
            }
        }

        mScreenSaverPreference.setOnPreferenceChangeListener(this);
        if (mScreenSaverPreference != null
                && Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_dreamsSupported, 
                           "com.android.internal.R.bool.config_dreamsSupported") == false) {
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory
                            .removePreference(mScreenSaverPreference);
                }
            } else {
                if (mScreenCategory != null) {
                    mScreenCategory
                            .removePreference(mScreenSaverPreference);
                }
            }
        }
    }

    private void setupFontType() {
        mFontTypePref = (ListPreference)findPreference(KEY_FONT_TYPE);
        if (getActivity() != null
                && Utils.checkPackage(mFontTypePref.getContext(),
                        "com.jungle.app.fonts")) {
            CharSequence[] blank = { "" };
            mFontTypePref.setEntries(blank);
            mFontTypePref.setEntryValues(blank);
            setDisplayFontEnable();
        } else {
            if (mFontCategory != null) {
                mFontCategory.removePreference(findPreference(KEY_FONT_TYPE));
            }
        }
        // CFONT_SERVER_SERVICE_JB Font server services.
        // 20140318 gaenoo.lee (Font setting update) [START]
        createFontSettings();
        // 20140318 gaenoo.lee (Font setting update) [END]
        // CFONT_SERVER_SERVICE_JB_END
    }

    private void setupFontSize() {
        mFontSizePref = (WarnedListPreference)findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);
        if (isSetTwoFontSize()) {
            CharSequence[] new_list_entries = new CharSequence[2];
            CharSequence[] new_list_entryValues = new CharSequence[2];
            CharSequence[] list_entries = getResources().getTextArray(
                    R.array.entries_font_size);
            CharSequence[] list_entryValues = getResources().getTextArray(
                    R.array.entryvalues_font_size);

            new_list_entries[0] = list_entries[1];
            new_list_entryValues[0] = list_entryValues[1];
            new_list_entries[1] = list_entries[2];
            new_list_entryValues[1] = list_entryValues[2];

            mFontSizePref.setEntries(new_list_entries);
            mFontSizePref.setEntryValues(new_list_entryValues);
        } else {
            if (isSetWineFontSize()) {
                mFontSizePref.setEntries(R.array.entries_font_size_wine);
                mFontSizePref
                        .setEntryValues(R.array.entryvalues_font_size_wine);
            } else {
                mFontSizePref.setEntries(R.array.sp_entries_font_size2);
                mFontSizePref.setEntryValues(R.array.entryvalues_font_size2);
            }
        }
    }

    private void setupAutofitScreen() {
        mAspectRatioCorrection = (Preference)findPreference(KEY_ASPECT_RATIO_CORRECTION);
        if (getActivity() != null
                && !Utils.checkPackage(mAspectRatioCorrection.getContext(),
                        "com.lge.settings.compatmode")) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_ASPECT_RATIO_CORRECTION));
            }
        }
    }

    private void setupFrontkeylight() {
        mFrontKeyLight = (Preference)findPreference(KEY_FRONTKEY_LIGHT_SETTINGS);
        if (Utils.isUpgradeModel() && !isNotUpgradeGModel()) {
            mFrontKeyLight.setTitle(R.string.sp_front_key_light_NORMAL);
        }

        boolean mIsQwerty = SystemProperties.getBoolean("ro.lge.capp_qwerty",
                false);
        if ("VZW".equals(Config.getOperator()) && mIsQwerty) {
            mFrontKeyLight.setTitle(R.string.sp_front_touch_key_light_ex2);
        }
        if (Utils.isFolderModel(getActivity())) {
            mFrontKeyLight.setTitle(R.string.keypad_light_title);
        }
        if (!Utils.supportFrontTouchKeyLight()) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mFrontKeyLight);
            }
        }
    }

    private void setupFrontTouchKey() {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mFrontTouchKey = (Preference)findPreference(KEY_FRONT_TOUCH_KEY_UI_4_2);
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_FRONT_TOUCH_KEY));
            }
        } else {
            mFrontTouchKey = (Preference)findPreference(KEY_FRONT_TOUCH_KEY);
            if (mScreenCategory != null) {
                mScreenCategory
                        .removePreference(findPreference(KEY_FRONT_TOUCH_KEY_UI_4_2));
            }
        }
        boolean naviBoolean = false;
        IWindowManager mWindowManagerService;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        try {
            naviBoolean = mWindowManagerService.hasNavigationBar();
            Log.d(TAG, "naviBoolean: " + naviBoolean);
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException front key");
        }

        if (!naviBoolean
                || "false".equals(ModelFeatureUtils.getFeature(getActivity(),
                        "usehometouchbutton"))) {
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                if (mScreenCategory != null) {
                    mScreenCategory.removePreference(mFrontTouchKey);
                }
            } else {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory.removePreference(mFrontTouchKey);
                }
            }
        }
    }

    private void setupScreenCapture() {
        mScreenCapture = (Preference)findPreference(KEY_SCREEN_CAPTURE);

        if (Utils.isUI_4_1_model(getActivity())) {
            mAdvancedCategory.removePreference(mScreenCapture);
            return;
        }

        boolean naviBoolean = false;
        IWindowManager mWindowManagerService;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        try {
            naviBoolean = mWindowManagerService.hasNavigationBar();
            if (!naviBoolean) {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory.removePreference(mScreenCapture);
                }
            } else if (Utils.isG2Model()
                    && !Utils.isUI_4_1_model(getActivity())) {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory.removePreference(mScreenCapture);
                }
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException Screen capture");
        }
    }

    private void setupNotificationFlash() {
        mNotificationFlashOneColor = (SwitchPreference)findPreference(KEY_NOTIFICATION_FLASH_ONE_COLOR);
        mNotificationFlashOneColor.setOnPreferenceChangeListener(this);
        if (!Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_powerLight_available,
                           "com.lge.R.bool.config_powerLight_available")) {
            Log.d(TAG,
                    "NotificationFlash - "
                            + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_powerLight_available,
                                  "com.lge.R.bool.config_powerLight_available"));
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mNotificationFlashOneColor);
            }
        }
    }

    private void setupHomeButtonLED() {
        mEmotionLED = (SwitchPreference)findPreference(KEY_EMOTIONAL_LED);
        if ("VZW".equals(Config.getOperator())) {
            mEmotionLED.setTitle(R.string.notification_led_vzw);
        }
        if (mScreenCategory != null) {
            mScreenCategory
                    .removePreference(findPreference(KEY_EMOTIONAL_LED));
        }
        Log.d(TAG,
                "EMOTIONAL LED type : "
                        + Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType"));
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 0) {
            if (mScreenCategory != null) {
                mScreenCategory.removePreference(mEmotionLED);
            }
        }
    }

    private void setupMotionSensorCalibration() {
        mMotionSensorCalibration = (Preference)findPreference(KEY_MOTION_SENSOR_CALIBRATION);
        if (!Utils.isCheckSensor(getActivity())
            || !(UserHandle.myUserId() == UserHandle.USER_OWNER)) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mMotionSensorCalibration);
            }
        }
    }

    private void setupScreenOffEffect() {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mScreenOffEffect = (Preference)findPreference(KEY_SCREEN_OFF_EFFECT_POPUP_4_2);
            if (mScreenCategory != null) {
                mScreenCategory
                        .removePreference(findPreference(KEY_SCREEN_OFF_EFFECT_POPUP));
            }
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_SCREEN_OFF_EFFECT_POPUP_4_2));
            }
        } else {
            mScreenOffEffect = (Preference)findPreference(KEY_SCREEN_OFF_EFFECT_POPUP);
            if (mAdvancedCategory != null) {
                mAdvancedCategory
                        .removePreference(findPreference(KEY_SCREEN_OFF_EFFECT_POPUP_4_2));
            }
        }

        if (isNotSupportScreenOffEffect()) {
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                if (mAdvancedCategory != null) {
                    mAdvancedCategory.removePreference(mScreenOffEffect);
                }
            } else {
                if (mScreenCategory != null) {
                    mScreenCategory.removePreference(mScreenOffEffect);
                }
            }
        }
    }

    private void setupScreenMode() {
        mScreenMode = (Preference)findPreference(KEY_SCREEN_MODE);
        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled") == false) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mScreenMode);
            }
        }
    }

    private void setupPlcMode() {
        mPlcMode = (CheckBoxPreference)findPreference(KEY_PLC_MODE);
        mPlcMode.setPersistent(false);
        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled") == false) {
            if (mAdvancedCategory != null) {
                mAdvancedCategory.removePreference(mPlcMode);
            }
        }
    }

    private void setupQuickCase() {
        mQuickCase = (Preference)findPreference(KEY_QUICK_CASE);
        if (!Utils.is_G_model_Device(Build.DEVICE)) {
            if (mAdvancedCategory != null) {
                Log.d("YSY", "GK");
                mAdvancedCategory
                        .removePreference(findPreference(KEY_QUICK_CASE));
                mAdvancedCategory
                        .removePreference(findPreference(KEY_QUICK_COVER));
            }
        } else {
            if (Config.getFWConfigBool(getActivity(),
                    com.lge.R.bool.config_smart_cover,
                    "com.lge.R.bool.config_smart_cover") == false
                    && Config.getFWConfigBool(getActivity(),
                            com.lge.R.bool.config_using_window_cover,
                            "com.lge.R.bool.config_using_window_cover") == false) {
                if (mAdvancedCategory != null) {
                    Log.d("YSY", "GK");
                    mAdvancedCategory
                            .removePreference(findPreference(KEY_QUICK_CASE));
                    mAdvancedCategory
                            .removePreference(findPreference(KEY_QUICK_COVER));
                }
            } else if (Config.getFWConfigBool(getActivity(),
                    com.lge.R.bool.config_smart_cover,
                    "com.lge.R.bool.config_smart_cover") == true
                    && Config.getFWConfigBool(getActivity(),
                            com.lge.R.bool.config_using_window_cover,
                            "com.lge.R.bool.config_using_window_cover") == false) {
                Log.d("YSY", "only ATT of GK");
                if (mAdvancedCategory != null) {
                    Log.d("YSY", "only ATT of GK");
                    mAdvancedCategory
                            .removePreference(findPreference(KEY_QUICK_CASE));
                }
            } else if (Config.getFWConfigBool(getActivity(),
                    com.lge.R.bool.config_smart_cover,
                    "com.lge.R.bool.config_smart_cover") == true
                    && Config.getFWConfigBool(getActivity(),
                            com.lge.R.bool.config_using_window_cover,
                            "com.lge.R.bool.config_using_window_cover") == true) {
                if (mAdvancedCategory != null) {
                    Log.d("YSY", "only not ATT of GK");
                    mAdvancedCategory
                            .removePreference(findPreference(KEY_QUICK_COVER));
                }
            }
        }
    }

    // Regist Screen-off effect Summary fun
    // 2013-05-15
    // A 4Team 2Part Yeom Seungyeop Y
    private void updateScreenOffEffectSummary() {
        int effectDbValue = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, 1);
        String summary;

        if (effectDbValue < 0) {
            summary = "";
        } else {
            final CharSequence[] effectSummaries = getResources().getTextArray(
                    R.array.screen_off_effect_entries_NORMAL);
            summary = mScreenOffEffect.getContext().getString(
                    R.string.screen_off_effect_summary,
                    effectSummaries[effectDbValue]);
            mScreenOffEffect.setSummary(summary);

            Log.d("TAG", "Screen-off effect : " + summary);
        }
    }

    // Regist Screen mode Summary fun
    // 2013-05-15
    // A 4Team 2Part Yeom Seungyeop Y
    private void updateScreenModeSummary() {
        int modeDbValue = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_MODE_SET, 1);
        String summary;

        if (modeDbValue < 0) {
            summary = "";
        } else {
            final CharSequence[] modeSummaries = getResources().getTextArray(
                    R.array.screen_mode_entries_NORMAL);
            summary = mScreenMode.getContext().getString(
                    R.string.screen_mode_summary, modeSummaries[modeDbValue]);
            mScreenMode.setSummary(summary);

            Log.d("TAG", "Screen Mode : " + summary);
        }
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                if (currentTimeout >= 1000000000) {
                    summary = (String)entries[best];
                } else {
                    if (Utils.isUI_4_1_model(getActivity())) {
                        summary = (String)entries[best];
                    } else {
                        summary = preference.getContext().getString(
                                R.string.screen_timeout_summary, entries[best]);
                    }
                }
            }
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm = (DevicePolicyManager)getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null)
                : 0;
        if (maxTimeout <= 0) {
            Log.d(TAG, "EAS Non_operation");
            bEASChecked = false;
            return; // policy not enforced
        }
        Log.d(TAG, "EAS Operation");
        Log.d(TAG, "maxTimeout : " + maxTimeout);
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length
                || revisedValues.size() != values.length) {
            screenTimeoutPreference.setEntries(revisedEntries
                    .toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(revisedValues
                    .toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.parseInt(screenTimeoutPreference
                    .getValue());
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference
                        .setValue(String.valueOf(userPreference));
            } else if (revisedValues.size() > 0
                    && Long.parseLong(revisedValues.get(
                            revisedValues.size() - 1).toString()) == maxTimeout) {
                // If the last one happens to be the same as the max timeout,
                // select that
                screenTimeoutPreference.setValue(String.valueOf(maxTimeout));
            } else {
                // There will be no highlighted selection since nothing in the
                // list matches
                // maxTimeout. The user can still select anything less than
                // maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
        bEASChecked = true;
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(
                R.array.entryvalues_font_size);
        if (!isSetTwoFontSize()
                && !Utils.isUpgradeModel()) {
            if (isSetWineFontSize()) {
                indices = getResources().getStringArray(
                        R.array.entryvalues_font_size_wine);
            } else {
                indices = getResources().getStringArray(
                        R.array.entryvalues_font_size2);
            }
        } 
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }

    // 20140318 gaenoo.lee (Font setting update) [START]
    private FontServerConnection mRemoteFontServer = null;
    private Handler mFontServerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (FontServerConnection.FONT_SERVER_CONNECTED == msg.what) {
                setSummaryFontSettings();
            }
        }
    };

    private void setSummaryFontSettings() {
        if (mFontSettingsPref_kk != null && mRemoteFontServer != null) {
            CharSequence summaryFontSettings = mRemoteFontServer.getSummary();
            mFontSettingsPref_kk.setSummary(summaryFontSettings);
        }
    }

    private void createFontSettings() {
        if (mFontCategory == null) {
            return;
        }
        mFontSettingsPref = (FontSettingsPreference)findPreference(KEY_FONT_SETTINGS);
        mFontSettingsPref_kk = findPreference(KEY_FONT_SETTINGS_KK);
        // Hide font type settings if not supported.
        if (!checkSupportChangeFont()) {
            mFontCategory.removePreference(mFontSettingsPref);
            mFontCategory.removePreference(mFontSettingsPref_kk);
            return;
        }

        mIsSupportFontDelete = getResources().getBoolean(
                R.bool.font_type_support_delete_feature);
        if (mIsSupportFontDelete) {
            mFontCategory.removePreference(mFontSettingsPref);
            mRemoteFontServer = new FontServerConnection(getActivity(),
                    mFontServerHandler);
            mRemoteFontServer.connectFontServerService();
        } else {
            mFontCategory.removePreference(mFontSettingsPref_kk);
            mFontSettingsPref.connectFontServer();
        }
    }

    private void destroyFontSettings() {
        // Do nothing if not supported font type settings.
        if (!checkSupportChangeFont()) {
            return;
        }

        // Disconnect FontServer
        if (mIsSupportFontDelete) {
            if (mRemoteFontServer != null) {
                mRemoteFontServer.disconnectFontServerService();
                mRemoteFontServer = null;
            }
        } else {
            mFontSettingsPref.disconnectFontServer();
        }
    }

    // 20140318 gaenoo.lee (Font setting update) [END]

    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault()
                    .getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        if (isSetTwoFontSize()) {
            if (index == 0) {
                index = 1;
            } else if (index == 3) {
                index = 2;
            }
            pref.setValueIndex(index - 1);
        } else if (isSetWineFontSize()) {
            int bIsMaximumFont = 0;
            bIsMaximumFont = Settings.Global.getInt(getActivity()
                    .getContentResolver(), "sync_large_text", 0);
            if (index == 2 && bIsMaximumFont == 1) {
                index = 3;
            }
            pref.setValueIndex(index);
        } else {
            int bIsMaximumFont = 0;
            bIsMaximumFont = Settings.Global.getInt(getActivity()
                    .getContentResolver(), "sync_large_text", 0);
            if (index == 4 && bIsMaximumFont == 1) {
                index = 5;
            }
            pref.setValueIndex(index);
        }

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        if (isSetWineFontSize()) {
            fontSizeNames = res.getStringArray(R.array.entries_font_size_wine);
        } else {
            fontSizeNames = res.getStringArray(R.array.sp_entries_font_size2);
        }
        pref.setSummary(String.format(
                res.getString(R.string.summary_font_size), fontSizeNames[index]));
    }

    @Override
    public void onResume() {
        super.onResume();

        mBrightnessPreference.updateThermalMAXBrightness();
        RotationPolicy.registerRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        if (mWifiDisplayPreference != null) {
            getActivity().registerReceiver(
                    mReceiver,
                    new IntentFilter(
                            DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
            mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
        }
        updateState();
        // [seungyeop.yeom][2013-09-25] add function of quick case
        setQuickCaseSummary();
        // LGE_CHANGE_S [k.cho@lge.com] 2012-05-25, KeepScreenOn
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(SettingsConstants.System.KEEP_SCREEN_ON),
                        true, mKeepScreenOnObserver);
        // LGE_CHANGE_E [k.cho@lge.com] 2012-05-25, KeepScreenOn

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(SCREEN_OFF_TIMEOUT), true,
                mScreenTimeoutObserver);
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        true, mBrightnessModeObserver);
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        true, mBrightnessModeTabletObserver);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor("check_night_mode"), true,
                mNightModeObserver);

        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(SettingsConstants.System.FRONT_KEY_ALL),
                        true, mNotificationFlashObserver);

        // Regist Notification Observer
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor("lge_notification_light_pulse"),
                true, mNotificationObserver);

        // Regist plc mode Observer
        // 2013-06-11
        // A 4Team 2Part Yeom Seungyeop Y
        getContentResolver()
                .registerContentObserver(
                        Settings.System
                                .getUriFor(SettingsConstants.System.PLC_MODE_SET),
                        true, mPlcModeObserver);
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra("perform",
                false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
    }

    private void startResult() {
        if (mSearch_result.equals(KEY_SCREEN_TIMEOUT)) {
            mScreenTimeoutPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_BRIGHTNESS)) {
            mBrightnessPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_FONT_SIZE)) {
            mFontSizePref.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_ACCELEROMETER)) {
            mAccelerometer.performClick(getPreferenceScreen());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);
        getContentResolver().unregisterContentObserver(mKeepScreenOnObserver); // LGE_CHANGE
                                                                               // [k.cho@lge.com]
                                                                               // 2012-05-25,
                                                                               // KeepScreenOn
        getContentResolver().unregisterContentObserver(mScreenTimeoutObserver);
        getContentResolver().unregisterContentObserver(mBrightnessModeObserver);
        getContentResolver().unregisterContentObserver(mNightModeObserver);
        getContentResolver().unregisterContentObserver(mPlcModeObserver); // [seungyeop.yeom][2013-06-11],
                                                                          // Unregist
                                                                          // Notification
                                                                          // Observer
                                                                          // Func
        getContentResolver().unregisterContentObserver(
                mNotificationFlashObserver);
        getContentResolver().unregisterContentObserver(
                mBrightnessModeTabletObserver);
        // Unregist Notification Observer Func
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        getContentResolver().unregisterContentObserver(mNotificationObserver);

        if (mWifiDisplayPreference != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 20140318 gaenoo.lee (Font setting update) [START]
        destroyFontSettings();
        // 20140318 gaenoo.lee (Font setting update) [END]
        ContentResolver resolver = getActivity().getContentResolver();
        resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mBrightnessTabletObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        // smart screen, smart video dialog control
        if (Utils.supportSplitView(getActivity())) {
            if (screenDialog != null && screenDialog.isShowing()) {
                screenDialog.dismiss();
                screenDialog = null;
            }

            if (videoDialog != null && videoDialog.isShowing()) {
                videoDialog.dismiss();
                videoDialog = null;
            }
        }

        if (mBrightnessPreference != null) {
            mBrightnessPreference.stopDialog();
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title, new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        }
        return null;
    }

    private void updateState() {
        updateAccelerometerRotationCheckbox();
        updateKeepScreenOnCheckbox(); // LGE_CHANGE [k.cho@lge.com] 2012-05-25,
                                      // KeepScreenOn
        updateKeepVideoOnCheckbox();
        readFontSizePreference(mFontSizePref);
        updateScreenSaver();
        updateHomeButtonLED();
        updateKnockOn();
        updateScreenSaverSummary();
        updateWifiDisplaySummary();
        updateScreenOffEffectSummary(); // [seungeyop.yeom] 2013-05-15
        updateScreenModeSummary(); // [seungeyop.yeom] 2013-05-15
        updatePlcModeOnCheckbox(); // [seungeyop.yeom] 2013-06-11
        updateScreenCaptureSummary();
        updateNightCheckBox();
        updateNotificationFlash();
    }

    private void updateHomeButtonLED() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        mEmotionLED.setChecked(isSilentModeChecked);
    }

    private void updateKnockOn() {
        boolean isKnockOnChecked = Settings.System.getInt(getContentResolver(),
                "gesture_trun_screen_on", 0) == 1 ? true : false;
        mKnockOn.setChecked(isKnockOnChecked);
        boolean mScurityKnock = Settings.Secure.getInt(getContentResolver(),
                KNOCKON_LOCK_SET, 0) > 0 ? true : false;
        if (mScurityKnock) {
            mKnockOn
                    .setSummary(R.string.gesture_screen_knock_code_summary);
        } else {
            mKnockOn
                    .setSummary(R.string.gesture_screen_on_off_summary);
        }
    }

    private void updateScreenSaver() {
        boolean dreamsEnabled = mBackend.isEnabled();
        if (mScreenSaverPreference.isChecked() != dreamsEnabled) {
            mScreenSaverPreference.setChecked(dreamsEnabled);
        }
    }

    private void updateNightCheckBox() {
        if (mBrightnessPreferenceTablet != null) {
            mBrightnessPreferenceTablet.updateTabletNightCheckBox();
        }
    }

    private void updateScreenSaverSummary() {
        if (mScreenSaverPreference != null) {
            mScreenSaverPreference.setSummary(DreamSettings
                    .getSummaryTextWithDreamName(getActivity()));
        }
    }

    // LGE_CHANGE_S [k.cho@lge.com] 2012-05-25, KeepScreenOn
    private void updateKeepScreenOnCheckbox() {
        int iKeepScreenOn = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.KEEP_SCREEN_ON, 0);
        mKeepScreenOn.setChecked(iKeepScreenOn != 0);
    }

    // LGE_CHANGE_E [k.cho@lge.com] 2012-05-25, KeepScreenOn
    private void updateKeepVideoOnCheckbox() {
        int iKeepVideoOn = Settings.System.getInt(getContentResolver(),
                "keep_video_on", 0);
        mKeepVideoOn.setChecked(iKeepVideoOn != 0);
    }

    // Regist Notification Observer Func
    // 2013-05-09
    // A 4Team 2Part Yeom Seungyeop Y
    private void updateNotificationCheckbox() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        mEmotionLED.setChecked(isSilentModeChecked);
        // Log.d(TAG, "Notification LED Status : " + isSilentModeChecked);
    }

    private void updateNotificationFlash() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.FRONT_KEY_ALL, 1) == 1 ? true : false;
        mNotificationFlashOneColor.setChecked(isSilentModeChecked);
    }

    // Regist Plc mode Observer Func
    // 2013-06-11
    // A 4Team 2Part Yeom Seungyeop Y
    private void updatePlcModeOnCheckbox() {
        int iPlcMode = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.PLC_MODE_SET, 1);
        mPlcMode.setChecked(iPlcMode != 0);
    }

    private void updateWifiDisplaySummary() {
        if (mWifiDisplayPreference != null) {
            switch (mWifiDisplayStatus.getFeatureState()) {
            case WifiDisplayStatus.FEATURE_STATE_OFF:
                mWifiDisplayPreference
                        .setSummary(R.string.wifi_display_summary_off);
                break;
            case WifiDisplayStatus.FEATURE_STATE_ON:
                mWifiDisplayPreference
                        .setSummary(R.string.wifi_display_summary_on);
                break;
            case WifiDisplayStatus.FEATURE_STATE_DISABLED:
            default:
                mWifiDisplayPreference
                        .setSummary(R.string.wifi_display_summary_disabled);
                break;
            }
        }
    }

    private void updateAccelerometerRotationCheckbox() {
        if (getActivity() == null) {
            return;
        }
        mAccelerometer.setChecked(!RotationPolicy
                .isRotationLocked(getActivity()));
    }

    public void writeFontSizePreference(Object objValue) {
        if (getActivity() == null) {
            return;
        }
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(
                    mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    public void updateScreenCaptureSummary() {
        int modeDbValue = Settings.System.getInt(getContentResolver(),
                "screen_capture_area", 0);
        String summary;

        if (modeDbValue < 0) {
            summary = "";
        } else if (modeDbValue == 0) {
            summary = getString(R.string.screen_capture_area_full);
        } else {
            summary = getString(R.string.screen_capture_area_part);
        }
        mScreenCapture.setSummary(summary);
        Log.d("TAG", "Screen Capture : " + summary);
    }

    // add onConfigurationChanged
    // 2013-06-06
    // A 4Team 2Part Yeom Seungyeop Y

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mCurConfig.updateFrom(newConfig);
        Log.d("YSY", "screen change");
        if (screenDialog != null && screenDialog.isShowing()) {
            isDialogCheck = donotshow.isChecked();
            screenDialog.dismiss();
            screenDialog = null;
            ShowWarningDialog();
        }

        if (videoDialog != null && videoDialog.isShowing()) {
            isVideoDialogCheck = donotshow.isChecked();
            videoDialog.dismiss();
            videoDialog = null;
            ShowWarningVideoDialog();
        }
    }

    // Modify Smart screen dialog
    // 2013-06-06
    // A 4Team 2Part Yeom Seungyeop Y
    protected void ShowWarningDialog() {
        String mScreenTitle = getString(R.string.sp_smart_screen_ex);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(mScreenTitle);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.smart_screen, null);
        alertDialog.setView(dialogView);

        donotshow = (CheckBox)dialogView.findViewById(R.id.checkbox_donotshow);

        TextView screenDescTitle = (TextView)dialogView
                .findViewById(R.id.smartscreen_desc_title);
        if ("ATT".equals(Config.getOperator())) {
            screenDescTitle.setText(R.string.display_help_desc_title_ex_att);
        }

        donotshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        donotshow.setChecked(isDialogCheck);

        alertDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                Log.i(TAG, "cancel listener");
                screenDialog = null;
                isDialogCheck = false;
            }
        });

        alertDialog.setPositiveButton(R.string.dlg_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (donotshow.isChecked()) {
                            Settings.System
                                    .putInt(getContentResolver(),
                                            SettingsConstants.System.KEEP_SCREEN_ON_DO_NOT_SHOW,
                                            1);
                            screenDialog = null;
                            isDialogCheck = false;
                        } else {
                            Settings.System
                                    .putInt(getContentResolver(),
                                            SettingsConstants.System.KEEP_SCREEN_ON_DO_NOT_SHOW,
                                            0);
                            screenDialog = null;
                            isDialogCheck = false;
                        }
                    }
                });

        if (screenDialog == null) {
            screenDialog = alertDialog.show();
        }
    }

    // Modify Smart video dialog
    // 2013-06-06
    // A 4Team 2Part Yeom Seungyeop Y
    protected void ShowWarningVideoDialog() {
        String mVideoTitle = getString(R.string.display_smart_video_ex);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(mVideoTitle);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.smart_video, null);
        alertDialog.setView(dialogView);

        donotshow = (CheckBox)dialogView
                .findViewById(R.id.checkbox_donotshow_video);
        TextView videoTitle = (TextView)dialogView
                .findViewById(R.id.smartsvideo_desc_title);
        TextView videoDescThird = (TextView)dialogView
                .findViewById(R.id.smartvideo_desc3);

        if ("ATT".equals(Config.getOperator())) {
            videoDescThird.setText(R.string.display_help_desc_4_ex_att);
            videoTitle.setText(R.string.display_help_desc_video_title_att);
        }

        donotshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        donotshow.setChecked(isVideoDialogCheck);

        alertDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "cancel listener");
                videoDialog = null;
                isVideoDialogCheck = false;
            }
        });

        alertDialog.setPositiveButton(R.string.dlg_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (donotshow.isChecked()) {
                            Settings.System.putInt(getContentResolver(),
                                    "keep_video_on_do_not_show", 1);
                            videoDialog = null;
                            isVideoDialogCheck = false;
                        } else {
                            Settings.System.putInt(getContentResolver(),
                                    "keep_video_on_do_not_show", 0);
                            videoDialog = null;
                            isVideoDialogCheck = false;
                        }
                    }
                });

        if (videoDialog == null) {
            videoDialog = alertDialog.show();
        }
    }

    // [S] add Maximum font size for Accessbility 2011.11.22 by sunghee.won
    public static class FontSizeWaningPopup extends DialogFragment implements
            DialogInterface.OnCancelListener, android.view.View.OnClickListener {
        public static void show(DisplaySettings parent) {
            if (!parent.isAdded()) {
                return;
            }
            final FontSizeWaningPopup dialog = new FontSizeWaningPopup();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), "FontSizeWaningPopup");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.dialog_title_font_size));
            TextView textView = new TextView(getActivity());

            ScrollView scrollView = new ScrollView(getActivity());
            LinearLayout linearLayout = new LinearLayout(getActivity());

            scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT));
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT));
            linearLayout.setOrientation(1);

            textView.setText(R.string.sp_fontsize_maximum_waning);
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                int padding = changeDIPtoPX(24);
                linearLayout.setPaddingRelative(padding, 0, padding, 0);
            } else {
                int padding = changeDIPtoPX(12);
                linearLayout.setPaddingRelative(padding, padding, padding, padding);
            }
            textView.setTextSize(18);
            textView.setTextColor(getResources().getColor(
                    R.color.normal_text_color));
            linearLayout.addView(textView);
            scrollView.addView(linearLayout);

            builder.setView(scrollView);

            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            // intent for apps
                        }
                    });

            return builder.create();
        }

        public void onClick(View arg0) {
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
        }

        public int changeDIPtoPX(int value) {
            return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    value, getActivity().getResources().getDisplayMetrics());
        }
    }

    // [E] add Maximum font size for Accessbility 2011.11.22 by sunghee.won

    private void updateScreenTimeOutValue() {
        long currentTimeout = Settings.System.getLong(getContentResolver(),
                SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        updateTimeoutPreferenceDescription(currentTimeout);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (Utils.supportRemoteFragment(getActivity())) {
            if ("key_homescreen".equals(preference.getKey())) {
                String packageName = preference.getIntent().getComponent()
                        .getPackageName();
                if (packageName == null || packageName.length() == 0) {
                    packageName = "com.lge.launcher2";
                }
                String fname = RemoteFragmentManager
                        .getFragmentName(packageName);
                if (fname != null) {
                    preference.setFragment(fname);
                    if (preference.getTitleRes() != 0) {
                        int resId = preference.getTitleRes();
                        preference.setTitle("wow");
                        preference.setTitle(getResources().getString(resId));
                    }
                }
            }
        }

        if (preference == mAccelerometer) {
            if (Utils.supportSplitView(getActivity())) {
                RotationPolicy.setRotationLock(getActivity(),
                        !mAccelerometer.isChecked());
            } else {
                RotationPolicy.setRotationLockForAccessibility(getActivity(),
                        !mAccelerometer.isChecked());
            }
        } else if (preference == mNotificationPulse) {
            boolean value = mNotificationPulse.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_PULSE, value ? 1 : 0);
            return true;
        } else if (preference == mFontTypePref) {
            mHandler.removeMessages(MSG_CALL_FONTS);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_CALL_FONTS));
        } else if (preference == mAspectRatioCorrection) {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.lge.settings.compatmode",
                        "com.lge.settings.compatmode.CompatibilityMode");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "onPreferenceTreeClick() : activity not found");
                Log.w(TAG,
                        "onPreferenceTreeClick() : com.lge.providers.packageconfig.PackageConfig_Act");
            }
        } else if (preference == mKeepScreenOn) {
            boolean value = mKeepScreenOn.isChecked();
            if (value) {
                Intent intent = new Intent();
                intent.setClassName("com.lge.keepscreenon",
                        "com.lge.keepscreenon.KeepScreenOnService");
                preference.getContext().startService(intent);
                // LGE_CHANGE_S [jonghen.han@lge.com] 2012-05-25, KeepScreenOn
                if ("true".equals(ModelFeatureUtils.getFeature(getActivity(),
                        "usesmartscreencategory"))) {
                    int iDoNotShow = Settings.System
                            .getInt(getContentResolver(),
                                    SettingsConstants.System.KEEP_SCREEN_ON_DO_NOT_SHOW,
                                    0);
                    if (iDoNotShow == 0) {
                        if ("VZW".equals(Config.getOperator())) {
                            isDialogCheck = true;
                        }
                        ShowWarningDialog();
                    }
                }
                // LGE_CHANGE_S [jonghen.han@lge.com] 2012-05-25, KeepScreenOn
            } else {
                if ("true".equals(ModelFeatureUtils.getFeature(getActivity(),
                        "usesmartscreencategory"))) {
                    if (!mKeepVideoOn.isChecked()) {
                        Intent intent = new Intent();
                        intent.setClassName("com.lge.keepscreenon",
                                "com.lge.keepscreenon.KeepScreenOnService");
                        preference.getContext().stopService(intent);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClassName("com.lge.keepscreenon",
                            "com.lge.keepscreenon.KeepScreenOnService");
                    preference.getContext().stopService(intent);
                }
            }
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.KEEP_SCREEN_ON, value ? 1 : 0);
        } else if (preference == mKeepVideoOn) { // LGE_CHANGE_E [k.cho@lge.com]
                                                 // 2012-05-25, KeepScreenOn
            boolean value = mKeepVideoOn.isChecked();
            if (value) {
                Intent intent = new Intent();
                intent.setClassName("com.lge.keepscreenon",
                        "com.lge.keepscreenon.KeepScreenOnService");
                preference.getContext().startService(intent);
                int iDoNotShow = Settings.System.getInt(getContentResolver(),
                        "keep_video_on_do_not_show", 0);
                if (iDoNotShow == 0) {
                    if ("VZW".equals(Config.getOperator())) {
                        isVideoDialogCheck = true;
                    }
                    ShowWarningVideoDialog();
                }
            } else {
                if (!mKeepScreenOn.isChecked()) {
                    Intent intent = new Intent();
                    intent.setClassName("com.lge.keepscreenon",
                            "com.lge.keepscreenon.KeepScreenOnService");
                    preference.getContext().stopService(intent);
                }
            }
            Settings.System.putInt(getContentResolver(), "keep_video_on",
                    value ? 1 : 0);
        } else if (preference == mPlcMode) { // [seungyeop.yeom][2013-06-11]
                                             // Click event of plc mode
            boolean value = mPlcMode.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.PLC_MODE_SET, value ? 1 : 0);
            Log.d("YSY",
                    "PLC mode : "
                            + Settings.System.getInt(getContentResolver(),
                                    SettingsConstants.System.PLC_MODE_SET, 1));
        } else if (preference == mKnockOn) {
            boolean value = mKnockOn.isChecked();
            Log.d("chan", "Knock on" + value);
            if (value) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        "gesture_trun_screen_on", 1);
            } else {
                Settings.System.putInt(getActivity().getContentResolver(),
                        "gesture_trun_screen_on", 0);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (getActivity() == null) {
            return false;
        }
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String)objValue);
            try {
                Settings.System.putInt(getContentResolver(),
                        SCREEN_OFF_TIMEOUT, value);
                updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        } else if (KEY_FONT_SIZE.equals(key)) {
            if ("1.60".equals(objValue)) {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "sync_large_text", 1);
                if (isSetWineFontSize()) {
                    writeFontSizePreference((Object)"1.40");
                } else {
                    writeFontSizePreference((Object)"1.30");
                }
                getActivity().sendBroadcast(
                        new Intent("lge.settings.intent.action.FONT_SIZE"));
                Log.d(TAG,
                        "Font Size send intent(lge.settings.intent.action.FONT_SIZE)");
                FontSizeWaningPopup.show(this);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "sync_large_text", 0);
                writeFontSizePreference(objValue);
            }
            readFontSizePreference(mFontSizePref);
        } else if (KEY_SCREEN_SAVER.equals(key)
                       || KEY_SCREEN_SAVER_4_2.equals(key)) {
            Log.d("hong", "Switch: " + (Boolean)objValue);
            mBackend.setEnabled((Boolean)objValue);
            updateScreenSaverSummary();
        } else if (KEY_NOTIFICATION_FLASH_ONE_COLOR.equals(key)) {
            boolean isChecked = (Boolean)objValue;
            if (isChecked) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_ALL, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NOTIFICATION_LIGHT_PULSE, 1);
            } else {
                Settings.System.putInt(getActivity().getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_ALL, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NOTIFICATION_LIGHT_PULSE, 0);
            }
            Intent intent = new Intent(
                    "com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_ALL);
            getActivity().sendBroadcast(intent);
        }
        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                mWifiDisplayStatus = (WifiDisplayStatus)intent
                        .getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
                updateWifiDisplaySummary();
            }
        }
    };

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            } else {
                mFontSizePref.click();
            }
        }
        return false;
    }

    private void setDisplayFontEnable() {
        boolean dimmedDisplayFlag = true;
        try {
            Configuration conf = getResources().getConfiguration();
            String curLocale = conf.locale.toString();
            String[] dimmedDisplayLists = getResources().getStringArray(
                    R.array.dimmed_display_font_list);
            for (int i = 0; i < dimmedDisplayLists.length; i++) {
                if (curLocale.contains(dimmedDisplayLists[i])) {
                    dimmedDisplayFlag = false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "setDisplayFontEnable", e);
        }

        mFontTypePref.setEnabled(dimmedDisplayFlag);
    }

    private String getCurrentLabel(String currentFamilyName) {
        ContentResolver cr = getContentResolver();
        Cursor c = cr
                .query(Uri
                        .parse("content://com.jungle.app.fonts.provider/fonts/current/label"),
                        null, null, new String[] { currentFamilyName }, null);
        String label = null;
        try {
            if (c != null) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    label = c.getString(c.getColumnIndex("current_label"));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Exception");
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return label;
    }

    private void callDisplayFont() {
        try {
            ComponentName compName = new ComponentName("com.jungle.app.fonts",
                    "com.jungle.app.fonts.Fonts");
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("KEY_CALL_FROM", 1);
            intent.putExtra("KEY_FONTDATA_TYPE", 2);
            intent.putExtra("KEY_UI_TYPE", 1);
            intent.setComponent(compName);
            intent.setAction("android.intent.action.MAIN");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "onPreferenceTreeClick() : activity not found");
            Log.w(TAG, "onPreferenceTreeClick() : com.jungle.app.fonts.Fonts");
        }
    }

    private boolean isContainfromValues(Object value) {
        CharSequence[] values = mScreenTimeoutPreference.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotUpgradeGModel() {
        if (Utils.is_G_model_Device(Build.DEVICE)
                && ("SHB".equals(Config.getOperator())
                        || "VDF".equals(Config.getOperator())
                        || "SCA".equals(android.os.SystemProperties
                                .get("ro.build.target_region"))
                        ||
                        // qingtai.wang@lge.com 20130104 changed for CUCC CN [S]
                        "CUCC".equals(Config.getOperator())
                        ||
                        // qingtai.wang@lge.com 20130104 changed for CUCC CN [E]
                        "STL".equals(Config.getOperator())
                        || "MON".equals(Config.getOperator())
                        || "ESA".equals(Config.getCountry())
                        || "AME".equals(Config.getCountry())
                        || Build.PRODUCT.equals("geehrc_byt_fr")
                        || Build.PRODUCT.equals("geehrc_free_fr")
                        || Build.PRODUCT.equals("geehrc_h3g_com")
                        || Build.PRODUCT.equals("geehrc_ncm_nw")
                        || Build.PRODUCT.equals("geehrc_nrj_fr")
                        || Build.PRODUCT.equals("geehrc_open_cis")
                        || Build.PRODUCT.equals("geehrc_open_eu")
                        || Build.PRODUCT.equals("geehrc_org_com")
                        || Build.PRODUCT.equals("geehrc_pls_pl")
                        || Build.PRODUCT.equals("geehrc_tel_au")
                        || Build.PRODUCT.equals("geehrc_tim_it")
                        || Build.PRODUCT.equals("geehrc_tlf_eu")
                        || Build.PRODUCT.equals("geehrc_tln_nw")
                        || Build.PRODUCT.equals("geehrc_tmn_pt") || Build.PRODUCT
                            .equals("geehrc_tmo_com"))) {
            return true;
        } else {
            return false;
        }
    }

    // 20120313 dongseok.lee :[L1a] if not support changing font, FontType menu
    // not be shown.
    private boolean checkSupportChangeFont() {
        boolean mEnableChangeFont = FontTypeFace.getUseCappFonts();

        UserManager userManager = (UserManager)getActivity().getSystemService(
                Context.USER_SERVICE);
        UserInfo userInfo = userManager
                .getUserInfo(userManager.getUserHandle());

        if (mEnableChangeFont == false
                || isNotSupportFontTypeOperator() == true
                || userInfo.isAdmin() == false) {
            Log.w(TAG, "warning : changing font not supported");
            return false;
        }
        return true;
    }

    private boolean isNotSupportFontTypeOperator() {
        // http://mlm.lge.com/di/browse/BTWOCHINA-378
        return false;
    }

    // Regist Quick Case Summary fun
    // 2013-12-17
    // seungyeop.yeom
    private void setQuickCaseSummary() {
        int coverDbValue = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);

        if (coverDbValue == 0) {
            mQuickCase.setSummary(R.string.lollipop_quickcover_title);
        } else if (coverDbValue == 1) {
            mQuickCase.setSummary(R.string.display_quick_cover_window_title);
        } else if (coverDbValue == 5) {
            mQuickCase.setSummary(R.string.lollipop_none_title);
        }

        Log.d(TAG, "cover type : " + coverDbValue);
    }

    private boolean isNotSupportScreenOffEffect() {
        if (Utils.is_G_model_Device(Build.DEVICE)
                || Utils.isGCAModel()
                || "omegar".equals(Build.DEVICE)
                || Utils.isUpgradeModel()
                || "false".equals(ModelFeatureUtils.getFeature(getActivity(),
                        "usescreenoffeffect"))) {
            return true;
        }
        return false;
    }

    public boolean isSupportKnockOn() {
        if (ConfigHelper.isSupportKnockCode2_0(getActivity())) {
            return true;
        }
        return false;
    }

    private boolean isSetTwoFontSize() {
        if ((Integer.parseInt(SystemProperties.get("ro.sf.lcd_density")) <= LCD_DENSITY_MDPI)
                && !Utils.supportSplitView(getActivity())
                && !isSetWineFontSize()) {
            return true;
        }
        return false;
    }

    private boolean isSetWineFontSize() {
        return Utils.isFolderModel(getActivity());
    }
    
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            int currentSettings = Settings.System.getInt(context.getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            
            setSearchIndexData(context, "display_settings", context.getString(R.string.display_settings), "main", null,
                    null, "android.settings.DISPLAY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            setSearchIndexData(context, KEY_BRIGHTNESS, context.getString(R.string.brightness),
                    context.getString(R.string.display_settings), null, null,
                    "android.settings.DISPLAY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            setSearchIndexData(context, KEY_SCREEN_TIMEOUT, context.getString(R.string.sp_screen_timeout_NORMAL),
                    context.getString(R.string.display_settings), null, null,
                    "android.settings.DISPLAY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            setSearchIndexData(context, KEY_SCREEN_SAVER, context.getString(R.string.screensaver_settings_title),
                    context.getString(R.string.display_settings), null, null,
                    "android.settings.DREAM_SETTINGS", null, null, 1, "Switch",
                    "Secure", "screensaver_enabled", 1, 0);
            setSearchIndexData(context, KEY_FONT_SETTINGS_KK,
                    context.getString(R.string.sp_fonttype_NORMAL),
                    context.getString(R.string.display_settings), null, null,
                    "android.intent.action.MAIN", "com.android.settings.Settings$FontSettingsActivity",
                    "com.android.settings", 1,
                    null, null, null, 1, 0);
            setSearchIndexData(context, KEY_FONT_SIZE, context.getString(R.string.dialog_title_font_size),
                    context.getString(R.string.display_settings), null, null,
                    "android.settings.DISPLAY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            setSearchIndexData(context, KEY_FRONT_TOUCH_KEY,
                    context.getString(R.string.display_home_touch_buttons),
                    context.getString(R.string.display_settings), null, null,
                    "android.intent.action.MAIN", "com.android.settings.Settings$FrontTouchKeyActivity",
                    "com.android.settings", 1,
                    null, null, null, 1, 0);
            if (RotationPolicy.isRotationSupported(context)) {
                setSearchIndexData(context, KEY_ACCELEROMETER, context.getString(R.string.accelerometer_title),
                        context.getString(R.string.display_settings), null, null,
                        "android.settings.DISPLAY_SETTINGS", null, null, 1,
                        "CheckBox", "System", "accelerometer_rotation", 1, 0);
            }
            
            if (Utils.isCheckSensor(context)) {
                setSearchIndexData(
                        context,
                        KEY_MOTION_SENSOR_CALIBRATION,
                        context.getString(R.string.sp_motion_sensor_calibration_NORMAL),
                        context.getString(R.string.display_settings),
                        context.getString(R.string.sp_motion_sensor_calibration_summary_NORMAL),
                        null, "android.intent.action.MAIN",
                        "com.android.settings.Settings$MotionSensorCalibrationActivity",
                        "com.android.settings", 1,
                        null, null, null, 1, 0);
            }
            setSearchIndexData(context, "easy_display_more_settings", context.getString(R.string.display_more),
                    context.getString(R.string.display_settings), null, null,
                    "com.lge.settings.MORE_DISPLAY_SETTINGS", null, null, 1,
                    null, null, null, currentSettings, 0);
            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
