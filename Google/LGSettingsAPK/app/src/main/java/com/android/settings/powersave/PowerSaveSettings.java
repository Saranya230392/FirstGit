package com.android.settings.powersave;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import android.os.Build;

import android.os.SystemProperties;
import android.provider.Settings;

public class PowerSaveSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, PowerSaveCheckBoxPreference.OnTreeClickListener {

    private static final String TAG = "PowerSaveSettings";
    public static final boolean DEBUG = false;

    private static final String KEY_POWER_SAVE_MODE = "power_save_mode";
    private static final String KEY_POWER_SAVING_CATEGORY = "power_saving_items_category";
    private static final String KEY_TOGGLE_POWER_SAVE_WIFI = "toggle_power_save_wifi";
    private static final String KEY_TOGGLE_POWER_SAVE_SYNC = "toggle_power_save_sync";
    private static final String KEY_TOGGLE_POWER_SAVE_BLUETOOTH = "toggle_power_save_bluetooth";
    //private static final String KEY_TOGGLE_POWER_SAVE_GPS = "toggle_power_save_gps";
    private static final String KEY_TOGGLE_POWER_SAVE_NFC = "toggle_power_save_nfc";
    //private static final String KEY_TOGGLE_POWER_SAVE_ANIMATIONS = "toggle_power_save_animations";
    //private static final String KEY_TOGGLE_POWER_SAVE_BRIGHTNESS = "toggle_power_save_brightness";
    private static final String KEY_POWER_SAVE_BRIGHTNESS = "power_save_brightness";
    private static final String KEY_POWER_SAVE_ECO_MODE = "power_save_ecomode";
    // yonguk.kim 20120210 ICS PowerSave
    private static final String KEY_POWER_SAVE_SCREEN_TIMEOUT = "power_save_screen_timeout";
    private static final String KEY_POWER_SAVE_FRONT_LED = "power_save_front_led";
    private static final String KEY_POWER_SAVE_TOUCH = "toggle_power_save_touch";
    // jongtak0920.kim Add Notification LED
    private static final String KEY_POWER_SAVE_NOTI_LED = "toggle_power_save_notification_led";
    // jongtak0920.kim Add Emotional LED
    private static final String KEY_POWER_SAVE_EMOTIONAL_LED = "toggle_power_save_emotional_led";
    // jongtak0920.kim Add Auto-adjust screen tone
    private static final String KEY_POWER_SAVE_AUTO_SCREEN_TONE = "toggle_power_save_oled";

    // JW QuickCase
    private static final String KEY_POWER_SAVE_QUICK_CASE = "power_save_quick_case";
    private static final String KEY_TOGGLE_POWER_SAVE_QUICK_CASE = "toggle_power_save_quick_case";
    //private static final String KEY_POWER_SAVE_BATTERY = "power_save_battery";
    //private static final String KEY_POWER_SAVE_BATTERY_PERCENTAGE = "power_save_battery_percentage";

    private PowerSaveListPreference mPowerSaveMode;
    private PreferenceCategory mPowerSavingItemsCategory;
    private PowerSaveCheckBoxPreference mToggleWifi;
    private PowerSaveCheckBoxPreference mToggleSync;
    private PowerSaveCheckBoxPreference mToggleBluetooth;
    private PowerSaveCheckBoxPreference mToggleGps;
    private PowerSaveCheckBoxPreference mToggleNfc;
    private PowerSaveCheckBoxPreference mToggleAnimations;
    //    private PowerSaveCheckBoxPreference mToggleBrightness;
    // yonguk.kim 20120209 ICS PowerSave
    //private PowerSaveBrightnessPreference mBrightness;
    private PowerSaveCheckBoxPreference mEcoMode;
    private PowerSaveBrightnessPreference2 mBrightness;
    private PowerSaveCheckBoxPreference mToggleTouch;
    private PowerSaveCheckBoxPreference mScreenTimeout;
    private PowerSaveCheckBoxPreference mFrontLed;
    private PowerSaveCheckBoxPreference mNotiLED;
    private PowerSaveCheckBoxPreference mEmotionalLED;
    // jongtak0920.kim Add Auto-adjust screen tone
    private PowerSaveCheckBoxPreference mAutoScreenTone;

    // jongwon007.kim Add Keypad light    
    private PowerSaveCheckBoxPreference mKeyPad;
    private static final String KEY_TOGGLE_POWER_SAVE_KEYPAD = "toggle_power_save_keypad_light";

    private PowerSave mPowerSave;
    private PowerSaveEnabler mPowerSaveEnabler;

    private Context mContext;
    //jw
    private PowerSaveCheckBoxPreference mQuickCase;

    //    private Preference mBattery;
    //    private CheckBoxPreference mBatteryPercentage;

    public static final String POWER_SAVE_EMOTIONAL_LED = "power_save_emotional_led";
    public static final String POWER_SAVE_AUTO_SCREEN_TONE = "power_save_auto_screen_tone";

    // test code
    private static final int MENU_POWERSAVE_TEST = Menu.FIRST;
    private SettingsBreadCrumb mBreadCrumb;

    /*
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);

                String batteryLevel = com.android.settings.Utils.getBatteryPercentage(intent);
                String batteryStatus = com.android.settings.Utils.getBatteryStatus(getResources(), intent);
                // yonguk.kim 20120323 Remove Not charging string requested by UI
                Locale systemLocale = getResources().getConfiguration().locale;
                String language = systemLocale.getLanguage();
                if (language.equals("ko") && (batteryStatus.equals(getResources().getString(R.string.battery_info_status_discharging)) ||
                        batteryStatus.equals(getResources().getString(R.string.battery_info_status_not_charging)))) {
                    mBattery.setTitle(batteryLevel);
                } else {
                    String batterySummary = context.getResources().getString(
                            R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
                    mBattery.setTitle(batterySummary);
                }
                mBattery.setIcon(mPowerSave.getBatteryImgId(level * 100 / scale));
            }
        }
    };
    */

    private Handler handler = new Handler();
    private ContentObserver mStartObserver = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "onChange:" + selfChange);
            boolean isChecked = Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_STARTED, -1) > 0;
            setItemMenuEnabled(isChecked);
        }
    };

    private Handler enable_handler = new Handler();
    private ContentObserver mEnableObserver = new ContentObserver(enable_handler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean isChecked = Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED, 0) > 0;
            Log.d(TAG, "PowerSaveSettings mEnableObserver::onChange:" + isChecked);
            if (mPowerSaveEnabler != null) {
                mPowerSaveEnabler.setSwitchChecked(isChecked);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        initPreference();
        checkPreference();

        mPowerSave = new PowerSave(mContext);

        /*
         * Check the Power save service in power save mode on.
         * If it is not running, re-start the PowerSave service.
         */
        if (Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_ENABLED,
                PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0) {
            if (!mPowerSave.isRunningPowerSaveService()) {
                Log.d(TAG, "Re-start PowerSaveService");
                Intent intent = new Intent(mContext, PowerSaveService.class);
                mContext.startService(intent);
            }
        }
        if (DEBUG == true) {
            setHasOptionsMenu(true);
        }
    }

    // yonguk.kim 20120209 PowerSaver Add Switch
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        Switch actionBarSwitch = new Switch(activity);
        actionBarSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
        //        if (activity instanceof PreferenceActivity) {
        //            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
        //            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        actionBarSwitch.setPaddingRelative(0, 0, padding, 0);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            actionBarSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            actionBarSwitch.setPaddingRelative(0, 0, padding, 0);
        }

        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
        //            }
        //        }

        mPowerSaveEnabler = new PowerSaveEnabler(activity, actionBarSwitch, this);
        boolean isEnabled = (Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0);
        mPowerSaveEnabler.setSwitchChecked(isEnabled);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Uri uri = Settings.System.getUriFor(SettingsConstants.System.POWER_SAVE_STARTED);
        Log.d(TAG, "register observer uri: " + uri.toString());
        getContentResolver().registerContentObserver(uri, true, mStartObserver);

        uri = Settings.System.getUriFor(SettingsConstants.System.POWER_SAVE_ENABLED);
        Log.d(TAG, "register observer uri: " + uri.toString());
        getContentResolver().registerContentObserver(uri, true, mEnableObserver);

        // yonguk.kim 20120209 PowerSaver Add Switch
        if (mPowerSaveEnabler != null) {
            mPowerSaveEnabler.resume();
        }
        updateState();

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null && mPowerSaveEnabler != null) {
                mBreadCrumb.addSwitch(mPowerSaveEnabler.getSwitch());
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mStartObserver);
        getContentResolver().unregisterContentObserver(mEnableObserver);

        // yonguk.kim 20120209 PowerSaver Add Switch
        if (mPowerSaveEnabler != null) {
            mPowerSaveEnabler.pause();
        }

        // yonguk.kim 20120601 Restore brightness in previewing
        if (mBrightness != null) {
            mBrightness.restorePreview();
        }
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.powersave_settings);

        mPowerSaveMode = (PowerSaveListPreference)findPreference(KEY_POWER_SAVE_MODE);
        mPowerSaveMode.setOnPreferenceChangeListener(this);
        //mPowerSaveMode.setOnPreferenceClickListener(this);
        mPowerSavingItemsCategory = (PreferenceCategory)findPreference(KEY_POWER_SAVING_CATEGORY);
        mToggleWifi = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_WIFI);
        mToggleWifi.setOnTreeClickListener(this);
        mToggleSync = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_SYNC);
        mToggleSync.setOnTreeClickListener(this);
        mToggleBluetooth = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_BLUETOOTH);
        mToggleBluetooth.setOnTreeClickListener(this);
        //mToggleGps = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_GPS);
        //mToggleGps.setOnTreeClickListener(this);
        mToggleNfc = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_NFC);
        mToggleNfc.setOnTreeClickListener(this);
        //mToggleAnimations = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_ANIMATIONS);
        //mToggleAnimations.setOnPreferenceClickListener(this);
        //mToggleBrightness = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_BRIGHTNESS);
        //mToggleBrightness.setOnTreeClickListener(this);

        //yonguk.kim 20120210 ICS PowerSave
        //mBrightness = (PowerSaveBrightnessPreference)findPreference(KEY_POWER_SAVE_BRIGHTNESS);

        mEcoMode = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_ECO_MODE);
        mPowerSavingItemsCategory.removePreference(mEcoMode);

        mBrightness = (PowerSaveBrightnessPreference2)findPreference(KEY_POWER_SAVE_BRIGHTNESS);
        mBrightness.setOnPreferenceChangeListener(this);
        mBrightness.setOnTreeClickListener(this);

        mScreenTimeout = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_SCREEN_TIMEOUT);
        mScreenTimeout.setOnPreferenceChangeListener(this);
        mScreenTimeout.setOnTreeClickListener(this);

        mFrontLed = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_FRONT_LED);
        if (!Utils.supportFrontTouchKeyLight() || Utils.isFolderModel(mContext)) { // yonguk.kim 20120228 Remove Front Led Setting for L_DCM
            mPowerSavingItemsCategory.removePreference(mFrontLed);
        } else {
            mFrontLed.setOnPreferenceChangeListener(this);
            mFrontLed.setOnTreeClickListener(this);
        }

        mToggleTouch = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_TOUCH);
        String hapticfeedback = SystemProperties.get("ro.device.hapticfeedback", "1");
        if (Utils.isUI_4_1_model(mContext)) {
            mToggleTouch.setTitle(R.string.haptic_feedback_enable_title_tap);
        }
        if (hapticfeedback.equals("0")) { // yonguk.kim 20120522 remove touch preference in M4
            mToggleTouch.setChecked(false);
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_TOUCH,
                    mToggleTouch.isChecked() ? 1 : 0);
            mPowerSavingItemsCategory.removePreference(mToggleTouch);
        }
        else {
            mToggleTouch.setOnPreferenceChangeListener(this);
            mToggleTouch.setOnTreeClickListener(this);
        }

        // jongtak0920.kim 120816 Remove NFC if its not available [START]
        if (NfcAdapter.getDefaultAdapter(mContext) == null || !"KR".equals(Config.getCountry())) {
            mPowerSavingItemsCategory.removePreference(mToggleNfc);
        }
        // jongtak0920.kim 120816 Remove NFC if its not available [END]

        // jongtak0920.kim Add Notification LED and Home Button LED
        mNotiLED = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_NOTI_LED);
        mEmotionalLED = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_EMOTIONAL_LED);

        if (!Utils.supportEmotionalLED(mContext)) {
            mPowerSavingItemsCategory.removePreference(mNotiLED);
            mPowerSavingItemsCategory.removePreference(mEmotionalLED);
        } else {
            if (Utils.supportHomeKey(mContext)) {
                mPowerSavingItemsCategory.removePreference(mNotiLED);
                mEmotionalLED.setOnPreferenceChangeListener(this);
                mEmotionalLED.setOnTreeClickListener(this);
                if ("VZW".equals(Config.getOperator())) {
                    mEmotionalLED.setTitle
                            (R.string.sp_power_saver_select_notification_led_NORMAL);
                    mEmotionalLED.setSummary
                            (R.string.sp_power_saver_select_notification_led_summary_NORMAL);
                }
            } else {
                mPowerSavingItemsCategory.removePreference(mEmotionalLED);
                mNotiLED.setOnPreferenceChangeListener(this);
                mNotiLED.setOnTreeClickListener(this);
            }
        }

        // jongtak0920.kim Add Auto-adjust screen tone
        mAutoScreenTone = (PowerSaveCheckBoxPreference)findPreference(KEY_POWER_SAVE_AUTO_SCREEN_TONE);
        if (!Utils.isOLEDModel(mContext)) {
            mPowerSavingItemsCategory.removePreference(mAutoScreenTone);
        } else {
            mAutoScreenTone.setOnPreferenceChangeListener(this);
            mAutoScreenTone.setOnTreeClickListener(this);
        }

        // JW QuickCase
        mQuickCase = (PowerSaveCheckBoxPreference)
                findPreference(KEY_TOGGLE_POWER_SAVE_QUICK_CASE);
        mQuickCase.setOnPreferenceChangeListener(this);
        mQuickCase.setOnTreeClickListener(this);
        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover") == false) {
            mPowerSavingItemsCategory.removePreference(mQuickCase);
        }
        /* Remove Animation in ICS, Start */
        //        mPowerSavingItemsCategory.removePreference(mToggleAnimations);
        /* Remove Animation in ICS, End */

        mKeyPad = (PowerSaveCheckBoxPreference)findPreference(KEY_TOGGLE_POWER_SAVE_KEYPAD);
        if (!Utils.isFolderModel(mContext)) {
            mPowerSavingItemsCategory.removePreference(mKeyPad);
        } else {
            mKeyPad.setOnPreferenceChangeListener(this);
            mKeyPad.setOnTreeClickListener(this);
        }

    }

    private void checkPreference() {
        mToggleWifi.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_WIFI, 1) != 0);
        mToggleSync.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_SYNC, 0) != 0);
        mToggleBluetooth.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_BT, 1) != 0);
        /*        mToggleGps.setChecked(Settings.System.getInt(
                        getContentResolver(), SettingsConstants.System.POWER_SAVE_GPS, 0) != 0);*/
        mToggleNfc.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_NFC, 1) != 0);
        mToggleTouch.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_TOUCH, 1) != 0);
        /* Remove Animation in ICS, Start */
        //mToggleAnimations.setChecked(Settings.System.getInt(
        //        getContentResolver(), SettingsConstants.System.POWER_SAVE_ANIMATIONS, 0) != 0);
        /* Remove Animation in ICS, End */

        mEcoMode.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_CPU_ADJUST, 1) != 0);
        /*        mEcoMode.setValue(String.valueOf(Settings.System.getInt(
                        getContentResolver(), SettingsConstants.System.POWER_SAVE_CPU, PowerSave.DEFAULT_CPU)));*/
        //yonguk.kim 20120210 ICS PowerSave
        mBrightness
                .setChecked(Settings.System.getInt(
                        getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST, 1) != 0);
        mBrightness.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_BRIGHTNESS,
                PowerSave.DEFAULT_BRIGHTNESS)));

        mScreenTimeout
                .setChecked(Settings.System.getInt(
                        getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST, 1) != 0);
        mScreenTimeout.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT,
                PowerSave.DEFAULT_SCREEN_TIMEOUT)));

        mKeyPad.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 0) != 0);
        mFrontLed.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 1) != 0);
        if ("ATT".equals(Config.getOperator())) {
            mFrontLed.setValue(String.valueOf(Settings.System.getInt(
                    getContentResolver(), SettingsConstants.System.POWER_SAVE_FRONT_LED,
                    PowerSave.DEFAULT_FRONT_LED_ATT)));
        } else {
            mFrontLed.setValue(String.valueOf(Settings.System.getInt(
                    getContentResolver(), SettingsConstants.System.POWER_SAVE_FRONT_LED,
                    PowerSave.DEFAULT_FRONT_LED)));
        }

        // jongtak0920.kim Add Notification LED
        mNotiLED.setChecked(Settings.System.getInt(
                getContentResolver(), POWER_SAVE_EMOTIONAL_LED, 0) != 0);

        // jongtak0920.kim Add Emotional LED
        mEmotionalLED.setChecked(Settings.System.getInt(
                getContentResolver(), POWER_SAVE_EMOTIONAL_LED, 0) != 0);

        // jongtak0920.kim Add Auto-adjust screen tone
        mAutoScreenTone.setChecked(Settings.System.getInt(
                getContentResolver(), POWER_SAVE_AUTO_SCREEN_TONE, 1) != 0);
        //jw
        mQuickCase.setChecked(Settings.System.getInt(
                getContentResolver(), KEY_POWER_SAVE_QUICK_CASE, 1) != 0);

        updateSummary(mBrightness);
        //        updateSummary(mEcoMode);
        updateSummary(mScreenTimeout);
        updateSummary(mFrontLed);
    }

    private void updateState() {

        /* Set the Auto power save mode*/
        String mode = String.valueOf(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE,
                PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE));

        boolean isChecked = Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_STARTED, -1) > 0;
        setItemMenuEnabled(isChecked);

        mPowerSaveMode.setValue(mode);
        updatePowerSaverModeSummary(mode);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mPowerSaveMode) {
            //mPowerSaveMode.getDialog().setTitle(R.string.sp_activate_power_saver_title_NORMAL);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onTreeClick(Preference preference) {

        // yonguk.kim 20120615 At least one item should be checked [START]
        if (CheckIfNoItemChecked()) {
            ShowToast(mContext.getString(R.string.sp_powersave_select_feature_guide_NORMAL));
            if (preference instanceof PowerSaveCheckBoxPreference) {
                ((PowerSaveCheckBoxPreference)preference).setChecked(true);
            }
            return;
        }
        // yonguk.kim 20120615 At least one item should be checked [END]

        if (preference == mToggleWifi) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_WIFI,
                    mToggleWifi.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleSync) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_SYNC,
                    mToggleSync.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleBluetooth) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_BT,
                    mToggleBluetooth.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleGps) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_GPS,
                    mToggleGps.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleNfc) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_NFC,
                    mToggleNfc.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleAnimations) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ANIMATIONS,
                    mToggleAnimations.isChecked() ? 1 : 0);
        }
        else if (preference == mEcoMode) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_CPU_ADJUST,
                    mEcoMode.isChecked() ? 1 : 0);
            //            updateSummary(mEcoMode);
        }
        // yonguk.kim 20120209
        else if (preference == mBrightness) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST,
                    mBrightness.isChecked() ? 1 : 0);
            updateSummary(mBrightness);
        }
        else if (preference == mScreenTimeout) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST,
                    mScreenTimeout.isChecked() ? 1 : 0);
            updateSummary(mScreenTimeout);
        }
        else if (preference == mFrontLed) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST,
                    mFrontLed.isChecked() ? 1 : 0);
            updateSummary(mFrontLed);
        }
        else if (preference == mToggleTouch) {
            Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_TOUCH,
                    mToggleTouch.isChecked() ? 1 : 0);
        }
        else if (preference == mNotiLED) {
            Settings.System.putInt(getContentResolver(), POWER_SAVE_EMOTIONAL_LED,
                    mNotiLED.isChecked() ? 1 : 0);
        }
        else if (preference == mEmotionalLED) {
            Settings.System.putInt(getContentResolver(), POWER_SAVE_EMOTIONAL_LED,
                    mEmotionalLED.isChecked() ? 1 : 0);
        }
        else if (preference == mAutoScreenTone) {
            Settings.System.putInt(getContentResolver(), POWER_SAVE_AUTO_SCREEN_TONE,
                    mAutoScreenTone.isChecked() ? 1 : 0);
        } else if (preference == mQuickCase) {
            Settings.System.putInt(getContentResolver(), KEY_POWER_SAVE_QUICK_CASE,
                    mQuickCase.isChecked() ? 1 : 0);
        } else if (preference == mKeyPad) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST,
                    mKeyPad.isChecked() ? 1 : 0);
        }
    }

    // yonguk.kim 20120615 At least one item should be checked [START]
    private boolean CheckIfNoItemChecked() {

        if (Utils.isFolderModel(mContext)) {
            mFrontLed.setChecked(false);
        } else {
            mKeyPad.setChecked(false);
        }

        boolean result = !(mToggleWifi.isChecked()
                || mToggleBluetooth.isChecked()
                || mToggleSync.isChecked()
                || mToggleTouch.isChecked()
                || mBrightness.isChecked()
                || mScreenTimeout.isChecked()
                || (mPowerSavingItemsCategory.findPreference(KEY_POWER_SAVE_FRONT_LED) != null && mFrontLed
                        .isChecked()) || mKeyPad.isChecked());

        if (NfcAdapter.getDefaultAdapter(mContext) != null && "KR".equals(Config.getCountry())) {
            result = result && !mToggleNfc.isChecked();
        }

        // jongtak0920.kim Add Notification LED and Emotional LED
        if (Utils.supportEmotionalLED(mContext)) {
            if (Utils.supportHomeKey(mContext)) {
                result = result && !mEmotionalLED.isChecked();
            } else {
                result = result && !mNotiLED.isChecked();
            }
        }

        // jongtak0920.kim Add Auto-adjust screen tone
        if (Utils.isOLEDModel(mContext)) {
            result = result && !mAutoScreenTone.isChecked();
        }
        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover")) {
            result = result && !mQuickCase.isChecked();
        }

        return result;
    }

    private void ShowToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    // yonguk.kim 20120615 At least one item should be checked [END]

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mPowerSaveMode) {
            setPowerSaveModeAllowed(objValue);
            updatePowerSaverModeSummary(objValue);
        } else if (preference == mBrightness) {
            if (objValue instanceof Boolean) {
                // Do not have to do anything...
            } else {
                setPowerSaveListValue(SettingsConstants.System.POWER_SAVE_BRIGHTNESS, objValue);
                updateSummary(mBrightness);
            }
        } else if (preference == mEcoMode) {
            if (objValue instanceof Boolean) {
                // Do not have to do anything...
            } else {
                setPowerSaveListValue(SettingsConstants.System.POWER_SAVE_CPU, objValue);
                updateSummary(mEcoMode);
            }
        } else if (preference == mScreenTimeout) {
            if (objValue instanceof Boolean) {
                // Do not have to do anything...
            } else {
                setPowerSaveListValue(SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT, objValue);
                updateSummary(mScreenTimeout);
            }
        }
        else if (preference == mFrontLed) {
            if (objValue instanceof Boolean) {
                // Do not have to do anything...
            } else {
                setPowerSaveListValue(SettingsConstants.System.POWER_SAVE_FRONT_LED, objValue);
                updateSummary(mFrontLed);
            }
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG == true) {
            menu.add(0, MENU_POWERSAVE_TEST, 0, "Do PowerSave");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_POWERSAVE_TEST:
            Intent intent = new Intent("com.android.settings.powersave.POWERSAVE_GO");
            mContext.sendBroadcast(intent);
            break;
        /*
        case android.R.id.home:
        int settingStyle = Settings.System.getInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
        if (settingStyle == 1 && Utils.supportEasySettings(getActivity()) && Utils.isUpgradeModel()) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            if (info != null) {
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                Log.d("kimyow", "top=" + topActivityClassName + "  base=" + baseActivityClassName);
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    getActivity().onBackPressed();
                    return true;
                } else {
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                    Log.d("kimyow", "tabIndex=" + tabIndex);
                    settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                    startActivity(settings);
                    finish();
                     return true;
                }
            }

        }
        break;
        */
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePowerSaverModeSummary(Object value) {
        try {
            if ("-1".equals(value)) { // Never activate
                mPowerSaveMode
                        .setSummary(getString(R.string.sp_activate_power_saver_never_summary_NORMAL));
            } else {
                String immediatly_value = Integer
                        .toString(PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY);
                if (immediatly_value.equals(value.toString())) { // Immediately
                    mPowerSaveMode.setValue(immediatly_value);
                    String entry = (mPowerSaveMode.getEntry() != null) ? mPowerSaveMode.getEntry()
                            .toString() : ""; // yonguk.kim 20120522 WBT
                    mPowerSaveMode.setSummary(entry);
                } else {
                    if (Utils.isRTLLanguage()) {
                        mPowerSaveMode.setSummary(getString(
                                R.string.sp_power_saver_on_summary_NORMAL,
                                String.format(Locale.getDefault(), "%d",
                                        Integer.parseInt(value.toString()))
                                        + "%"));
                    } else {
                        mPowerSaveMode.setSummary(getString(
                                R.string.sp_power_saver_on_summary_NORMAL,
                                "\u200E" + value.toString() + "%"));
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist Auto power save mode", e);
        }
    }

    private void doPowerSaveService(boolean enabled) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                "com.android.settings.powersave.PowerSaveService");
        if (enabled) {
            mContext.startService(intent);
        }
        else {
            mContext.stopService(intent);
        }
    }

    private void setPowerSaveModeAllowed(Object objValue) {
        int value = Integer.parseInt((String)objValue);
        //int preState = Settings.System.getInt(getContentResolver(),
        //        SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
        Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE,
                value);

        Log.d("jw", "setPowerSaveModeAllowed value : " + value);
        mPowerSave.onBatterySaverModeValue(value);
        int enabled = Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED);
        if (enabled > 0) {
            Intent intent = new Intent(PowerSave.ACTION_POWERSAVE_MODE_CHANGED);
            mContext.sendBroadcast(intent);
        }
    }

    // yonguk.kim 20120210 ICS PowerSave
    private void setPowerSaveListValue(String name, Object objValue) {
        int value = Integer.parseInt((String)objValue);

        Settings.System.putInt(getContentResolver(), name, value);
    }

    private void updateSummary(Preference preference) {
        //String value = Settings.System.getString(
        //        getContentResolver(), SettingsConstants.System.POWER_SAVE_BRIGHTNESS)+"% ";
        if (preference instanceof PowerSaveCheckBoxPreference) {
            if (((PowerSaveCheckBoxPreference)preference).isChecked()) {
                preference.setSummary(""); // need this action for "%"
                if (preference == mBrightness) {
                    preference.setSummary("%s " + Utils.getString(
                            R.string.sp_power_saver_select_brightness_NORMAL).toLowerCase());
                } else {
                    preference.setSummary("%s");
                }
            }
            else {
                if (preference == mBrightness) {
                    preference.setSummary(R.string.sp_power_saver_brightness_adjust_summary_NORMAL);
                }
                else if (preference == mScreenTimeout) {
                    preference.setSummary(R.string.sp_power_saver_screen_timeout_summary_NORMAL);
                }
                else if (preference == mFrontLed) {
                    preference.setSummary(R.string.sp_power_saver_front_touch_light_summary_NORMAL);
                }
            }
        }
    }

    // yonguk.kim 20120210 ICS PowerSave
    public void onSwitchCheckedChanged(boolean checked) {
        //            setItemMenuEnabled(checked);
    }

    public void setItemMenuEnabled(boolean checked) {
        mPowerSaveMode.setEnabled(!checked);
        mPowerSavingItemsCategory.setEnabled(!checked);
    }

    public String getEasyTabIndex(String topActivity) {
        if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity)) {
            return "sound";
        } else if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity) ||
                "com.android.settings.Settings$LocationSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$AccountSyncSettingsActivity".equals(topActivity)) {
            return "general";
        }
        return null;
    }
}
