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

package com.android.settings.lge;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessorySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "AccessorySettings";
    private static int USER_OWNER;

    /**
     * Earjack ; USB ; OTG ; Dock; Pen T100 : Ear-jack port position <br>
     * BC : USB port position<br>
     * O X : Whether support OTG or not.<br>
     * O X : Whether support dock or not. <br>
     * BC : Pen position<br>
     */
    private static final String DEFAULT_DEVICE_CONFIG = "T90,BC,O,X,X";
    private static final String TANGIBLE_DEVICE_CONFIG = "tangible_device_config";

    private static final String QUICK_COVER = "quick_cover_settings"; // [seungyeop.yeom][2013-07-03] only tablet func
    private static final String QUICK_COVER_WINDOW = "quick_cover_window_settings";
    private static final String QUICK_COVER_LOLLIPOP = "quick_cover_lollipop_settings"; // [seungyeop.yeom] 2013-06-17

    private static final String KEY_CAR_HOME = "car_home";
    private static final String EAR_JACK_KEY = "earphone_settings";
    private static final String USB_STORAGE_KEY = "usb_storage_settings";
    private static final String DOCK_KEY = "dock_settings";
    private static final String PEN_KEY = "pen_settings";
    private static final String EAR_JACK_SWITCH_KEY = "earphone_settings_switch";
    private static final String USB_STORAGE_SWITCH_KEY = "usb_storage_settings_switch";
    private static final String DOCK_SWITCH_KEY = "dock_settings_switch";

    private static final String AUDIO_DOCK_SOUND_EFFECT_KEY = "audio_dock_sound_effect";
    //private static final String AUDIO_DOCK_EXTERNAL_SPEAKER_KEY = "audio_dock_external_speaker";
    // private static final String AUDIO_DOCK_SCREENSAVER_KEY = "audio_dock_screensaver";
    private static final String DOCK_CATEGORY_KEY = "dock_category";
    private static final String EARPHONE_CATEGORY_KEY = "earphone_category";
    private static final String USBSTORAGE_CATEGORY_KEY = "usbstorage_category";

    public static String supportPen = "NA";
    public static String supportOTG = "NA";
    public static String supportDOCK = "NA";

    // define object of preference
    private Preference mEarjack;
    private Preference mUsbStorage;
    private Preference mDock;
    private Preference mPen;

    private SwitchPreference mEarjack_switch;
    private SwitchPreference mUsbStorage_switch;
    private SwitchPreference mDock_switch;

    private CheckBoxPreference mCarHome;
    private SwitchPreference mQuickCover; // [seungyeop.yeom][2013-07-03] only tablet func, add switch button[2013-08-01]
    private SwitchPreference mQuickCoverWindow;
    private Preference mQuickCoverLollipop; // [seungyeop.yeom] 2013-06-17

    private PreferenceCategory mDockCategory;
    private PreferenceCategory mEarphoneCategory;
    private PreferenceCategory mUsbstorageCategory;
    private CheckBoxPreference mDockSoundEffect;

    Intent intent;

    static {
        try {
            Class<?> userHandleInstance = Class
                    .forName("android.os.UserHandle");
            if (userHandleInstance != null) {
                USER_OWNER = getIntField(userHandleInstance, "USER_OWNER");
            }
        } catch (Exception e) {
            Log.w(TAG, "Fail to get the USER_OWNER.", e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.accessory_settings);

        PreferenceScreen root = getPreferenceScreen();

        // A070 Audio dock
        mDockCategory = (PreferenceCategory)findPreference(DOCK_CATEGORY_KEY);
        mEarphoneCategory = (PreferenceCategory)findPreference(EARPHONE_CATEGORY_KEY);
        mUsbstorageCategory = (PreferenceCategory)findPreference(USBSTORAGE_CATEGORY_KEY);
        mDockSoundEffect = (CheckBoxPreference)findPreference(AUDIO_DOCK_SOUND_EFFECT_KEY);
        mDockSoundEffect.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.Global.DOCK_SOUNDS_ENABLED, 1) != 0);

        mEarjack_switch = (SwitchPreference)findPreference(EAR_JACK_SWITCH_KEY);
        mEarjack_switch.setOnPreferenceChangeListener(this);
        mEarjack_switch.setChecked(getPanelEnable(getContentResolver(),
                SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE));
        mUsbStorage_switch = (SwitchPreference)findPreference(USB_STORAGE_SWITCH_KEY);
        mUsbStorage_switch.setOnPreferenceChangeListener(this);
        mUsbStorage_switch.setChecked(getPanelEnable(getContentResolver(),
                SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE));
        mDock_switch = (SwitchPreference)findPreference(DOCK_SWITCH_KEY);
        mDock_switch.setOnPreferenceChangeListener(this);
        mDock_switch.setChecked(getPanelEnable(getContentResolver(),
                SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE));

        // Preferences are generated.
        mEarjack = (Preference)findPreference(EAR_JACK_KEY);
        mPen = (Preference)root.findPreference(PEN_KEY);
        mUsbStorage = (Preference)root.findPreference(USB_STORAGE_KEY);
        mDock = (Preference)root.findPreference(DOCK_KEY);
        mCarHome = (CheckBoxPreference)findPreference(KEY_CAR_HOME);
        mCarHome.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.CAR_HOME_AUTO_LAUNCH, 1) != 0);
        mQuickCover = (SwitchPreference)root.findPreference(QUICK_COVER); // [seungyeop.yeom] 2013-07-03
        // [seungyeop.yeom] 2013-07-09
        mQuickCoverWindow = (SwitchPreference)root.findPreference(QUICK_COVER_WINDOW);
        mQuickCoverLollipop = (Preference)root.findPreference(QUICK_COVER_LOLLIPOP); // [seungyeop.yeom] 2013-06-17

        // [seungyeop.yeom][2013-08-01] operate switch of quick cover
        mQuickCover.setOnPreferenceChangeListener(this);
        mQuickCover.setChecked(Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, 0) != 0);

        // [seungyeop.yeom][2013-08-20] operate switch of quickWindow case
        mQuickCoverWindow.setOnPreferenceChangeListener(this);
        mQuickCoverWindow.setChecked(Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 0) != 0);

        Log.d(TAG, "[Accessory] supportOTG =" + supportOTG + " , supportDOCK = " + supportDOCK
                + ", supportPen = " + supportPen);

        try {
            int temp_earphone = Settings.Global.getInt(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE);
            Log.d("tangible", "creat() temp_earphone = " + temp_earphone);
        } catch (SettingNotFoundException e) {
            if ("VZW".equals(Config.getOperator())) {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "tangible_earphone_panel_enable", 0);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "tangible_usbstorage_panel_enable", 0);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "tangible_earphone_panel_enable", 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        "tangible_usbstorage_panel_enable", 1);
            }
            Settings.Global.putInt(getActivity().getContentResolver(),
                    "tangible_dock_panel_enable", 1);
            Log.d("tangible", "temp_earphone SettingNotFoundException ");
        }

        if (getPanelEnableDB(getContentResolver(),
                SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE) != 2) {
            root.removePreference(mEarjack);
            root.removePreference(mUsbStorage);
            root.removePreference(mDock);

            if (!"OT".equals(supportOTG)) {
                root.removePreference(mUsbStorage_switch);
            }
            if (!"DO".equals(supportDOCK)) {
                root.removePreference(mDock_switch);
            }
        } else {
            root.removePreference(mEarjack_switch);
            root.removePreference(mUsbStorage_switch);
            root.removePreference(mDock_switch);

            if (!"OT".equals(supportOTG)) {
                root.removePreference(mUsbStorage);
            }
            if (!"DO".equals(supportDOCK)) {
                root.removePreference(mDock);
            }
        }

        if ("NA".equals(supportPen)) {
            root.removePreference(mPen);
        }

        if (!("VZW".equals(Config.getOperator()))) {
            root.removePreference(mCarHome);
        } else {
            if (Utils.supportSplitView(getActivity().getApplicationContext()) && !isOwner()) {
                mCarHome.setEnabled(false);
            }

            if (!Utils.isCarHomeSupport(getActivity())) {
                if (root != null) {
                    root.removePreference(mCarHome);
                }
            }
        }

        if ((SystemProperties.get("ro.product.name").contains("070"))) {
            root.removePreference(mDock_switch);
            root.removePreference(mEarjack_switch);
            root.removePreference(mUsbStorage_switch);
            mDockCategory.addPreference(mDock_switch);
            mEarphoneCategory.addPreference(mEarjack_switch);
            mUsbstorageCategory.addPreference(mUsbStorage_switch);
        } else {
            root.removePreference(mDockCategory);
            root.removePreference(mEarphoneCategory);
            root.removePreference(mUsbstorageCategory);
        }

        if (Utils.supportTangibleSettings(getActivity()) == false) {
            root.removePreference(mDock_switch);
            root.removePreference(mEarjack_switch);
            root.removePreference(mUsbStorage_switch);
            root.removePreference(mDockCategory);
            root.removePreference(mEarphoneCategory);
            root.removePreference(mUsbstorageCategory);
        }

        // This log provides information related to DB of Cover
        // 2013-08-25
        // A 8Team 2Part Yeom Seungyeop Y
        Log.d("YSY", "Global Cover type DB: " + Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0));
        Log.d("YSY", "Global QuickCover DB" + Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_COVER_ENABLE, 0));
        Log.d("YSY", "Global QuickWindow case DB" + Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 0));
        Log.d("YSY", "Global LollipopCoverType DB" + Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.LOLLIPOP_COVER_TYPE, 0));

        int width = getActivity().getResources().
                getDimensionPixelSize(com.lge.R.dimen.config_cover_window_width);
        int height = getActivity().getResources().
                getDimensionPixelSize(com.lge.R.dimen.config_cover_window_height);
        int x_pos = getActivity().getResources().
                getDimensionPixelOffset(com.lge.R.dimen.config_cover_window_x_pos);
        int y_pos = getActivity().getResources().
                getDimensionPixelOffset(com.lge.R.dimen.config_cover_window_y_pos);

        Log.d("YSY", "Width of Cover size: " + width);
        Log.d("YSY", "Height of Cover size: " + height);
        Log.d("YSY", "X_pos of Cover size: " + x_pos);
        Log.d("YSY", "Y_pos of Cover size: " + y_pos);

        boolean quickCover = Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover");
        boolean quickWindow = Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_window_cover,
                "com.lge.R.bool.config_using_window_cover");
        boolean quickVu = Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover");
        boolean quickCircle = Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_circle_cover,
                "com.lge.R.bool.config_using_circle_cover");

        Log.d("YSY", "exist QuickCover: " + quickCover);
        Log.d("YSY", "exist QuickWindow: " + quickWindow);
        Log.d("YSY", "exist QuickVu: " + quickVu);
        Log.d("YSY", "exist QuickCircle: " + quickCircle);

        // Cover is provided for each model.
        // 2013-08-25
        // A 8Team 2Part Yeom Seungyeop Y
        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Log.d("YSY", "Quick Cover true");
            if (("KDDI".equals(Config.getOperator()))) {
                Log.d("YSY", "KDDI feature");
                root.removePreference(mQuickCover);
                root.removePreference(mQuickCoverWindow);
                root.removePreference(mQuickCoverLollipop);
            } else {
                // root.removePreference(mQuickCover);
                root.removePreference(mQuickCoverWindow);
                root.removePreference(mQuickCoverLollipop);
            }
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Log.d("YSY", "QuickWindow case true");
            root.removePreference(mQuickCover);
            // root.removePreference(mQuickCoverWindow);
            root.removePreference(mQuickCoverLollipop);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Log.d("YSY", "QuickVu case true");
            root.removePreference(mQuickCover);
            root.removePreference(mQuickCoverWindow);
            // root.removePreference(mQuickCoverLollipop);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == true) {
            Log.d("YSY", "Quick Circle true");

            mQuickCoverWindow.setTitle(R.string.quick_circle_case_title);
            mQuickCoverWindow.setSummary(R.string.quick_circle_case_summary);
            root.removePreference(mQuickCover);
            // root.removePreference(mQuickCoverWindow);
            root.removePreference(mQuickCoverLollipop);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Log.d("YSY", "All Cover false");
            root.removePreference(mQuickCover);
            root.removePreference(mQuickCoverWindow);
            root.removePreference(mQuickCoverLollipop);
        } else {
            Log.d("YSY", "All remove");
            root.removePreference(mQuickCover);
            root.removePreference(mQuickCoverWindow);
            root.removePreference(mQuickCoverLollipop);
        }

        // We confirm Lollipop apk exists in the model.
        if (Utils.checkPackage(getActivity(), "com.lge.lollipop")) {
            Log.d("YSY", "Lollipop apk exists.");
        }

        // Back setting is disable. (only tablet)
        // Back Setting is enable. (others)
        // [seungyeop.yeom] 2013-06-20
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.supportSplitView(getActivity().getApplicationContext()) && !isOwner()
                && Utils.supportTangibleSettings(getActivity())) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getActivity().getResources().getString(
                            R.string.tangible_editpanel_guest_warning), Toast.LENGTH_LONG).show();
            return false;
        }

        if (EAR_JACK_KEY.equals(preference.getKey())) {
            startTangibleService();
            ComponentName c = new ComponentName("com.lge.tangible",
                    "com.lge.tangible.HeadsetPreferenceAcitivity");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        } else if (PEN_KEY.equals(preference.getKey())) {
            startTangibleService();
            ComponentName c = new ComponentName("com.lge.tangible",
                    "com.lge.tangible.PenPreferenceActivity");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        } else if (USB_STORAGE_KEY.equals(preference.getKey())) {
            startTangibleService();
            ComponentName c = new ComponentName("com.lge.tangible",
                    "com.lge.tangible.UsbStoragePreferenceActivity");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        } else if (DOCK_KEY.equals(preference.getKey())) {
            startTangibleService();
            ComponentName c = new ComponentName("com.lge.tangible",
                    "com.lge.tangible.DockPreferenceActivity");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        } else if (KEY_CAR_HOME.equals(preference.getKey())) {
            boolean value = mCarHome.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.CAR_HOME_AUTO_LAUNCH, value ? 1 : 0);
        } else if (EAR_JACK_SWITCH_KEY.equals(preference.getKey())) {
            startTangibleService();
            if (Utils.supportRemoteFragment(getActivity()) == false) {
                ComponentName c = new ComponentName("com.lge.tangible",
                        "com.lge.tangible.btnpanel.ButtonEditActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                i.putExtra("com.lge.tangible.btnpanel.KEY", "headset_pref_key");
                startActivity(i);
            } else {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                setPreferenceToTangible(preference,
                        "com.lge.tangible.btnpanel.ButtonEditFragment",
                        "com.lge.tangible.btnpanel.KEY", "headset_pref_key");
                preferenceActivity.onPreferenceStartFragment(this, preference);
                return true;
            }
        } else if (USB_STORAGE_SWITCH_KEY.equals(preference.getKey())) {
            startTangibleService();
            if (Utils.supportRemoteFragment(getActivity()) == false) {
                ComponentName c = new ComponentName("com.lge.tangible",
                        "com.lge.tangible.btnpanel.ButtonEditActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                i.putExtra("com.lge.tangible.btnpanel.KEY", "otg_ums_button_manager");
                startActivity(i);
            } else {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                setPreferenceToTangible(preference,
                        "com.lge.tangible.btnpanel.ButtonEditFragment",
                        "com.lge.tangible.btnpanel.KEY", "otg_ums_button_manager");
                preferenceActivity.onPreferenceStartFragment(this, preference);
                return true;
            }
        } else if (DOCK_SWITCH_KEY.equals(preference.getKey())) {
            startTangibleService();
            if (Utils.supportRemoteFragment(getActivity()) == false) {
                ComponentName c = new ComponentName("com.lge.tangible",
                        "com.lge.tangible.btnpanel.ButtonEditActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                i.putExtra("com.lge.tangible.btnpanel.KEY", "dock_pref_key");
                startActivity(i);
            } else {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                setPreferenceToTangible(preference,
                        "com.lge.tangible.btnpanel.ButtonEditFragment",
                        "com.lge.tangible.btnpanel.KEY", "dock_pref_key");
                preferenceActivity.onPreferenceStartFragment(this, preference);
                return true;
            }
        } else if (QUICK_COVER_WINDOW.equals(preference.getKey())) {
            ComponentName c;
            c = new ComponentName("com.android.settings",
                    "com.android.settings.lge.QuickWindowCase");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void setPreferenceToTangible(
            Preference preference, String fname, String keyName, String keyValue) {
        preference.setFragment(fname);

        if (keyName != null) {
            Bundle args = preference.getExtras();
            args.putString(keyName, keyValue);
        }

        if (preference.getTitleRes() != 0) {
            int resId = preference.getTitleRes();
            preference.setTitle("wow");
            preference.setTitle(getResources().getString(resId));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        // TODO Auto-generated method stub

        final String key = preference.getKey();
        if (getActivity() == null)
        {
            return false;
        }

        // If you click the button, the cover of the operation is performed.
        // 2013-08-01
        // [seungyeop.yeom]
        if (QUICK_COVER.equals(key)) {
            Log.d("quick cover switch", "Switch: " + (Boolean)objValue);
            mQuickCover.setChecked((mQuickCover.isChecked() ? false : true));
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, (Boolean)objValue ? 1 : 0);
        }

        if (QUICK_COVER_WINDOW.equals(key)) {
            Log.d("quick window case switch", "Switch: " + (Boolean)objValue);
            mQuickCoverWindow.setChecked((mQuickCoverWindow.isChecked() ? false : true));
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, (Boolean)objValue ? 1 : 0);
        }

        if (EAR_JACK_SWITCH_KEY.equals(key)) {
            Log.d("tangible earjack switch", "Switch: " + (Boolean)objValue);
            mEarjack_switch.setChecked((mEarjack_switch.isChecked() ? false : true));
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE, (Boolean)objValue ? 1
                            : 0);
        }

        if (USB_STORAGE_SWITCH_KEY.equals(key)) {
            Log.d("tangible usbstorage switch", "Switch: " + (Boolean)objValue);
            mUsbStorage_switch.setChecked((mUsbStorage_switch.isChecked() ? false : true));
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE,
                    (Boolean)objValue ? 1 : 0);
        }

        if (DOCK_SWITCH_KEY.equals(key)) {
            Log.d("tangible dock switch", "Switch: " + (Boolean)objValue);
            mDock_switch.setChecked((mDock_switch.isChecked() ? false : true));
            Settings.Global.putInt(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE, (Boolean)objValue ? 1 : 0);
        }
        return false;
    }

    private void startTangibleService() {
        if (!isLGTangibleIOServiceRunning()) {
            Intent i = new Intent();
            ComponentName tangibleServiceComp = new ComponentName(
                    "com.lge.tangible", "com.lge.tangible.core.TangibleUIService");
            i.setComponent(tangibleServiceComp);
            getActivity().startService(i);
        }
    }

    public boolean getPanelEnable(ContentResolver mcr, String field) {
        int result = getPanelEnableDB(mcr, field);

        if (result == 1) {
            Log.d(TAG, "result == 1");
            return true;
        } else if (result == 0) {
            Log.d(TAG, "result == 0");
            return false;
        }
        Log.d(TAG, "result == else");
        return false;
    }

    public int getPanelEnableDB(ContentResolver mcr, String field) {
        int panel_enable = 2; //Vu3 default value or Nothing DB

        try {
            panel_enable = Settings.Global.getInt(mcr, field);
            Log.d(TAG, field + " try " + panel_enable);
        } catch (SettingNotFoundException e) {
            panel_enable = 2;
            Log.d(TAG, field + " catch " + panel_enable);
        }
        return panel_enable;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If you have the Quick Cover Lollipop, you can see a summary of Quick Cover Lollipop
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == true) {
            Log.d("YSY", "enter lollipop summary");
            setQuickCoverLollipopSummary();
        }

        // Operating state of the button is checked.
        // 2013-08-01
        // Yeom Seungyeop Y
        if (mQuickCover != null) {
            mQuickCover.setChecked(Settings.Global.getInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0) != 0);
        }

        if (mQuickCoverWindow != null) {
            mQuickCoverWindow.setChecked(Settings.Global.getInt(getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0) != 0);
        }

        try {
            int temp_earphone = Settings.Global.getInt(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE);
            Log.d("tangible", "temp_earphone = " + temp_earphone);
        } catch (SettingNotFoundException e) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    "tangible_earphone_panel_enable", 1);
            Log.d("tangible", "temp_earphone SettingNotFoundException ");
        }

        if (getPanelEnableDB(getContentResolver(),
                SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE) != 2) {
            mEarjack_switch.setChecked(getPanelEnable(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE));
            mUsbStorage_switch.setChecked(getPanelEnable(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE));
            mDock_switch.setChecked(getPanelEnable(getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE));
        }
    }

    public boolean isOwner() {
        try {
            Method getCurrentUser = ActivityManager.class.getMethod(
                    "getCurrentUser", (Class[])null);
            int userId = (Integer)getCurrentUser.invoke(null, (Object[])null);
            return (USER_OWNER == userId);
        } catch (Exception e) {
            Log.w(TAG, "This OS version is not support "
                    + "getCurrentUser in isOwner().", e);
        }
        return true;
    }

    public static int getIntField(Class<?> ref, String intFieldName)
            throws Exception {
        Field resField = ref.getField(intFieldName);
        return resField.getInt(resField);
    }

    private boolean isLGTangibleIOServiceRunning() {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if ("com.lge.tangible.core.TangibleUIService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void parseSystemPropertyNew(String prop) {
        String property = prop;
        property = property.trim().toUpperCase();

        String property_out[] = property.split(",");
        if (property_out.length != 5) {
            Log.e(TAG, "This property is not 5 item.");
        } else {
            if ("O".equals(property_out[2])) {
                supportOTG = "OT";
            } else {
                supportOTG = "NA";
            }
            if ("O".equals(property_out[3])) {
                supportDOCK = "DO";
            } else {
                supportDOCK = "NA";
            }
            if ("X".equals(property_out[4])) {
                supportPen = "NA";
            } else {
                supportPen = "B7";
            }
        }
    }

    private void parseSystemPropertyOld() {
        String property = SystemProperties.get(TANGIBLE_DEVICE_CONFIG);
        if (TextUtils.isEmpty(property)) {
            //"System property not found : "
            return;
        }

        property = property.trim().toUpperCase();

        // Number of features. Earjack, usb, etc..
        final int NUM_OF_FEATURE = 5;
        // If tokens on tail is NA, it can be omitted. Fill NA.
        if (property.length() != (NUM_OF_FEATURE * 2)) {
            StringBuffer buf = new StringBuffer(property);
            while (buf.length() < (NUM_OF_FEATURE * 2)) {
                buf.append("NA");
            }
            property = buf.toString();
        }

        supportPen = property.substring(4, 6);
        supportOTG = property.substring(6, 8);
        supportDOCK = property.substring(8, 10);
    }

    // Regist Quick cover lollipop Summary fun
    // 2013-06-18
    // A 4Team 2Part Yeom Seungyeop Y
    private void setQuickCoverLollipopSummary() {
        int coverDbValue = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0);
        int lollipopCoverDbValue = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.LOLLIPOP_COVER_TYPE, 0);

        if (coverDbValue == 0) {
            mQuickCoverLollipop.setSummary(R.string.lollipop_quickcover_title);
        } else if (coverDbValue == 2 && lollipopCoverDbValue == 1) {
            mQuickCoverLollipop.setSummary(R.string.lollipop_quickcover_lollipop_title_silver);
        } else if (coverDbValue == 2 && lollipopCoverDbValue == 0) {
            mQuickCoverLollipop.setSummary(R.string.lollipop_quickcover_lollipop_title_black);
        } else if (coverDbValue == 5) {
            mQuickCoverLollipop.setSummary(R.string.lollipop_none_title);
        }

        Log.d(TAG, "Set: " + Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.COVER_TYPE, 0));
    }
}
