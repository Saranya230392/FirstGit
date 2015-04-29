/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActionBar;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.os.UserHandle;

import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.settings.SettingsSwitchPreference;
import android.widget.TextView;
import android.os.Build;

import com.android.internal.content.PackageMonitor;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.accessibility.SettingsAccessibilityUtility;
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//OPEN_CN_SETTINGS jingming.wang@lge.com [remove] remove speak password in China version [START]
import android.os.SystemProperties;

//OPEN_CN_SETTINGS jingming.wang@lge.com [remove] remove speak password in China version [END]
/**
 * Activity with the accessibility settings.
 */
public class AccessibilitySettings extends SettingsPreferenceFragment implements DialogCreatable,
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AccessibilitySettings";
    private static final String DEFAULT_SCREENREADER_MARKET_LINK =
            "market://search?q=pname:com.google.android.marvin.talkback";

    private static final float LARGE_FONT_SCALE = 1.3f;

    // [S][2012.3.29][changyu0218.lee@lge.com][M4][35609] Change Large text value in M4 models
    private static final float LARGE_FONT_SCALE_LARGE = 1.15f;
    // [E][2012.3.29][changyu0218.lee@lge.com][M4][35609] Change Large text value in M4 models
    private static final String SYSTEM_PROPERTY_MARKET_URL = "ro.screenreader.market";

    // Timeout before we update the services if packages are added/removed since
    // the AccessibilityManagerService has to do that processing first to
    // generate
    // the AccessibilityServiceInfo we need for proper presentation.

    static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';

    private static final String KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE =
            "key_install_accessibility_service_offered_once";

    // Preference categories
    private static final String SERVICES_CATEGORY = "services_category";
    private static final String SYSTEM_CATEGORY = "system_category";

    // Preferences
    private static final String TOGGLE_LARGE_TEXT_PREFERENCE =
            "toggle_large_text_preference";
    private static final String TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE =
            "toggle_high_text_contrast_preference";
    private static final String TOGGLE_INVERSION_PREFERENCE =
            "toggle_inversion_preference";
    private static final String TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE =
            "toggle_power_button_ends_call_preference";
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE =
            "toggle_lock_screen_rotation_preference";
    private static final String TOGGLE_SPEAK_PASSWORD_PREFERENCE =
            "toggle_speak_password_preference";
    private static final String SELECT_LONG_PRESS_TIMEOUT_PREFERENCE =
            "select_long_press_timeout_preference";
    private static final String ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN =
            "enable_global_gesture_preference_screen";
    private static final String CAPTIONING_PREFERENCE_SCREEN =
            "captioning_preference_screen";
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN =
            "screen_magnification_preference_screen";
    private static final String DISPLAY_DALTONIZER_PREFERENCE_SCREEN =
            "daltonizer_preference_screen";

    // Extras passed to sub-fragments.
    static final String EXTRA_PREFERENCE_KEY = "preference_key";
    static final String EXTRA_CHECKED = "checked";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_SUMMARY = "summary";
    static final String EXTRA_ENABLE_WARNING_TITLE = "enable_warning_title";
    static final String EXTRA_ENABLE_WARNING_MESSAGE = "enable_warning_message";
    static final String EXTRA_DISABLE_WARNING_TITLE = "disable_warning_title";
    static final String EXTRA_DISABLE_WARNING_MESSAGE = "disable_warning_message";
    static final String EXTRA_SETTINGS_TITLE = "settings_title";
    static final String EXTRA_COMPONENT_NAME = "component_name";
    static final String EXTRA_SETTINGS_COMPONENT_NAME = "settings_component_name";
    static final String EXTRA_SERVICE_COMPONENT_NAME = "service_component_name";

    // [S][2012.1.26][changyu0218.lee@lge.com][VS900][125459] fix accessibility pop up issue
    private AlertDialog alertedialog = null;
    private static final int CREATE = 0;
    private static final int PAUSE = 1;
    // [E][2012.1.26][changyu0218.lee@lge.com][VS900][125459] fix accessibility pop up issue

    // Dialog IDs.
    private static final int DIALOG_ID_NO_ACCESSIBILITY_SERVICES = 1;

    // presentation.
    private static final long DELAY_UPDATE_SERVICES_MILLIS = 1000;

    // Auxiliary members.
    final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

    static final Set<ComponentName> sInstalledServices = new HashSet<ComponentName>();

    private final Map<String, String> mLongPressTimeoutValuetoTitleMap =
            new HashMap<String, String>();

    private final Configuration mCurConfig = new Configuration();

    private final Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            loadInstalledServices();
            updateServicesPreferences();
        }
    };

    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            loadInstalledServices();
            updateServicesPreferences();
        }
    };

    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        @Override
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            mHandler.postDelayed(mUpdateRunnable, DELAY_UPDATE_SERVICES_MILLIS);
        }
    };

    private final SettingsContentObserver mSettingsContentObserver =
            new SettingsContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    loadInstalledServices();
                    updateServicesPreferences();
                }
            };
            
    private final ScreenContentObserver mScreenContentObserver = 
            new ScreenContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                updateAllPreferences();
                }
            };
            
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        @Override
        public void onChange() {
            updateLockScreenRotationCheckbox();
        }
    };

    // Preference controls.
    private PreferenceCategory mServicesCategory;
    private PreferenceCategory mSystemsCategory;

    private CheckBoxPreference mToggleLargeTextPreference;
    private CheckBoxPreference mToggleHighTextContrastPreference;
    private CheckBoxPreference mTogglePowerButtonEndsCallPreference;
    private CheckBoxPreference mToggleLockScreenRotationPreference;
    private CheckBoxPreference mToggleSpeakPasswordPreference;
    private ListPreference mSelectLongPressTimeoutPreference;
    private Preference mNoServicesMessagePreference;
    private PreferenceScreen mCaptioningPreferenceScreen;
    private PreferenceScreen mDisplayMagnificationPreferenceScreen;
    private PreferenceScreen mGlobalGesturePreferenceScreen;
    private PreferenceScreen mDisplayDaltonizerPreferenceScreen;
    private DevicePolicyManager mDpm;
    private SettingsSwitchPreference mToggleInversionPreference;
    private int mLongPressTimeoutDefault;

    private int mflag;
    private static final String KEY_WHERE_ARE_YOU_FROM = "where_are_you_from";
    private static final String VALUE_FROM_STARTUP_WIZARD = "from_startup_wizard";

    private static final int LCD_DENSITY_MDPI = 160; //mdpi

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.accessibility_settings);
        initializeAllPreferences();
        setHomeButtonOfActionBar();
        // [S][2012.1.19][changyu0218.lee@lge.com][U0][123621] fix accessibility pop up issue
        mflag = Settings.System.getInt(getContentResolver(), "LargeText_Flag", 0);
        // [E][2012.1.19][changyu0218.lee@lge.com][U0][123621] fix accessibility pop up issue
        mDpm = (DevicePolicyManager)(getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE));

    }

    @Override
    public void onResume() {
        super.onResume();
        loadInstalledServices();
        updateAllPreferences();
        if (mServicesCategory.getPreference(0) == mNoServicesMessagePreference &&
                mflag == CREATE) {
            if (alertedialog != null) {
                if (false == alertedialog.isShowing()) {
                    offerInstallAccessibilitySerivceOnce();
                }
            }
            else if (alertedialog == null) {
                offerInstallAccessibilitySerivceOnce();
            }
        }
        // [E][2012.1.19][changyu0218.lee@lge.com][U0][123621] fix accessibility pop up issue
        mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        mScreenContentObserver.register(getContentResolver());
        mSettingsContentObserver.register(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.registerRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
    }

    @Override
    public void onPause() {
        mSettingsPackageMonitor.unregister();
        mScreenContentObserver.unregister(getContentResolver());
        mSettingsContentObserver.unregister(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
        super.onPause();
        if (Settings.System.getInt(getContentResolver(), "LargeText_Flag", 0) == CREATE) {
            mflag = PAUSE;
        }
    }

    // [S][2012.2.09][changyu0218.lee@lge.com][X3][7046] fix indicator landscape issue
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mCurConfig.updateFrom(newConfig);
    }

    // [E][2012.2.09][changyu0218.lee@lge.com][X3][7046] fix indicator landscape issue

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mSelectLongPressTimeoutPreference == preference) {
            handleLongPressTimeoutPreferenceChange((String)newValue);
            return true;
        } else if (mToggleInversionPreference == preference) {
            handleToggleInversionPreferenceChange((Boolean)newValue);
            return true;
        }
        return false;
    }

    private void handleLongPressTimeoutPreferenceChange(String stringValue) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, Integer.parseInt(stringValue));
        mSelectLongPressTimeoutPreference.setSummary(
                mLongPressTimeoutValuetoTitleMap.get(stringValue));
    }

    private void handleToggleInversionPreferenceChange(boolean checked) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, (checked ? 1 : 0));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mToggleLargeTextPreference == preference) {
            Settings.System.putInt(getContentResolver(), "LargeText_Flag", 1);
            handleToggleLargeTextPreferenceClick();
            return true;
        } else if (mToggleHighTextContrastPreference == preference) {
            handleToggleTextContrastPreferenceClick();
            return true;
        } else if (mTogglePowerButtonEndsCallPreference == preference) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
            return true;
        } else if (mToggleSpeakPasswordPreference == preference) {
            handleToggleSpeakPasswordPreferenceClick();
            return true;
        } else if (mGlobalGesturePreferenceScreen == preference) {
            handleToggleEnableAccessibilityGesturePreferenceClick();
            return true;
        } else if (mDisplayMagnificationPreferenceScreen == preference) {
            handleDisplayMagnificationPreferenceScreenClick();
            return true;
        } else if (mDisplayDaltonizerPreferenceScreen == preference) {
            handleDisplayDaltonizerPreferenceScreenClick();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void handleToggleLargeTextPreferenceClick() {
        try {
            Log.d(TAG, "Toggle, ro.sf.lcd_density = " + SystemProperties.get("ro.sf.lcd_density"));
            if (Integer.parseInt(SystemProperties.get("ro.sf.lcd_density")) <= LCD_DENSITY_MDPI) {
                mCurConfig.fontScale = mToggleLargeTextPreference.isChecked()
                        ? LARGE_FONT_SCALE_LARGE : 1;
            } else {
                    mCurConfig.fontScale = mToggleLargeTextPreference.isChecked()
                            ? LARGE_FONT_SCALE : 1;
                if (!Utils.isUpgradeModel()) {
                    if (mToggleLargeTextPreference.isChecked()) {
                        Settings.Global.putInt(getContentResolver(), "sync_large_text", 1);
                        getActivity().sendBroadcast(
                                new Intent("lge.settings.intent.action.FONT_SIZE"));
                        Log.d(TAG, "lge.settings.intent.action.FONT_SIZE in accessibility");
                    } else {
                        Settings.Global.putInt(getContentResolver(), "sync_large_text", 0);
                    }
                }

            }
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException re) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void handleToggleTextContrastPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
                (mToggleHighTextContrastPreference.isChecked() ? 1 : 0));
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                (mTogglePowerButtonEndsCallPreference.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleLockScreenRotationPreferenceClick() {
        RotationPolicy.setRotationLockForAccessibility(getActivity(),
                !mToggleLockScreenRotationPreference.isChecked());
        Intent intent = new Intent("com.android.settings.rotation.CHANGED");
        getActivity().sendBroadcast(intent);
    }

    private void handleToggleSpeakPasswordPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD,
                mToggleSpeakPasswordPreference.isChecked() ? 1 : 0);
    }

    private void handleToggleEnableAccessibilityGesturePreferenceClick() {
        Bundle extras = mGlobalGesturePreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_global_gesture_preference_title));
        extras.putString(EXTRA_SUMMARY, getString(
                R.string.accessibility_global_gesture_preference_description));
        extras.putBoolean(EXTRA_CHECKED, Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mGlobalGesturePreferenceScreen,
                mGlobalGesturePreferenceScreen);
    }

    private void handleDisplayMagnificationPreferenceScreenClick() {
        Bundle extras = mDisplayMagnificationPreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_screen_magnification_title));
        extras.putCharSequence(EXTRA_SUMMARY, getActivity().getResources().getText(
                R.string.accessibility_screen_magnification_summary));
        extras.putBoolean(EXTRA_CHECKED, Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mDisplayMagnificationPreferenceScreen,
                mDisplayMagnificationPreferenceScreen);
    }

    private void handleDisplayDaltonizerPreferenceScreenClick() {
        Bundle extras = mDisplayDaltonizerPreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_display_daltonizer_preference_title));
        extras.putBoolean(EXTRA_CHECKED, Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mDisplayDaltonizerPreferenceScreen,
                mDisplayDaltonizerPreferenceScreen);
    }

    private void initializeAllPreferences() {
        mServicesCategory = (PreferenceCategory)findPreference(SERVICES_CATEGORY);
        mSystemsCategory = (PreferenceCategory)findPreference(SYSTEM_CATEGORY);

        // Large text.
        mToggleLargeTextPreference =
                (CheckBoxPreference)findPreference(TOGGLE_LARGE_TEXT_PREFERENCE);

        // Text contrast.
        mToggleHighTextContrastPreference =
                (CheckBoxPreference)findPreference(TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE);

        // Display inversion.
        mToggleInversionPreference = (SettingsSwitchPreference)findPreference(TOGGLE_INVERSION_PREFERENCE);
        mToggleInversionPreference.setDivider(false);
        mToggleInversionPreference.setOnPreferenceChangeListener(this);

        // Power button ends calls.
        mTogglePowerButtonEndsCallPreference =
                (CheckBoxPreference)findPreference(TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE);
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                || !Utils.isVoiceCapable(getActivity())) {
            mSystemsCategory.removePreference(mTogglePowerButtonEndsCallPreference);
        }

        if (Utils.isWifiOnly(getActivity())) {
            mSystemsCategory.removePreference(mTogglePowerButtonEndsCallPreference);
        }

        // Lock screen rotation.
        mToggleLockScreenRotationPreference =
                (CheckBoxPreference)findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);
        }

        // Speak passwords.
        mToggleSpeakPasswordPreference =
                (CheckBoxPreference)findPreference(TOGGLE_SPEAK_PASSWORD_PREFERENCE);

        if ("CN".equals(Config.getCountry())) {
            mSystemsCategory.removePreference(mToggleSpeakPasswordPreference);
        }
        // Long press timeout.
        mSelectLongPressTimeoutPreference =
                (ListPreference)findPreference(SELECT_LONG_PRESS_TIMEOUT_PREFERENCE);
        mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
        if (mLongPressTimeoutValuetoTitleMap.size() == 0) {
            String[] timeoutValues = getResources().getStringArray(
                    R.array.long_press_timeout_selector_values);
            mLongPressTimeoutDefault = Integer.parseInt(timeoutValues[0]);
            String[] timeoutTitles = getResources().getStringArray(
                    R.array.long_press_timeout_selector_titles);
            final int timeoutValueCount = timeoutValues.length;
            for (int i = 0; i < timeoutValueCount; i++) {
                mLongPressTimeoutValuetoTitleMap.put(timeoutValues[i], timeoutTitles[i]);
            }
        }

        // Captioning.
        mCaptioningPreferenceScreen = (PreferenceScreen)findPreference(
                CAPTIONING_PREFERENCE_SCREEN);

        // Display magnification.
        mDisplayMagnificationPreferenceScreen = (PreferenceScreen)findPreference(
                DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);

        // Display color adjustments.
        mDisplayDaltonizerPreferenceScreen = (PreferenceScreen) findPreference(
                DISPLAY_DALTONIZER_PREFERENCE_SCREEN);

        // Global gesture.
        mGlobalGesturePreferenceScreen =
                (PreferenceScreen)findPreference(ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN);
        final int longPressOnPowerBehavior = Config.getFWConfigInteger(getActivity(), com.android.internal.R.integer.config_longPressOnPowerBehavior, 
            "com.android.internal.R.integer.config_longPressOnPowerBehavior");
        
        final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                || longPressOnPowerBehavior != LONG_PRESS_POWER_GLOBAL_ACTIONS) {
            // Remove accessibility shortcut if power key is not present
            // nor long press power does not show global actions menu.
            mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
        }
    }

    private void setHomeButtonOfActionBar() {
        final Activity activity = getActivity();
        final Intent intent = getActivity().getIntent();
        Log.d(TAG, "enter - setHomeButtonOfActionBar");
        Bundle extra = intent.getExtras();
        if (intent != null && extra != null) {
            Log.d(TAG, "intent != null && extra != null");
            String from = extra.getString(KEY_WHERE_ARE_YOU_FROM);
            Log.d(TAG, "from =>" + from);
            if (from != null && VALUE_FROM_STARTUP_WIZARD.equals(from)) {
                activity.getActionBar().setHomeButtonEnabled(false);
                activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            }
            else {
                activity.getActionBar().setHomeButtonEnabled(true);
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void updateAllPreferences() {
        updateServicesPreferences();
        updateSystemPreferences();
    }

    private void updateServicesPreferences() {
        // Since services category is auto generated we have to do a pass
        // to generate it since services can come and go and then based on
        // the global accessibility state to decided whether it is enabled.

        // Generate.
        mServicesCategory.removeAll();

        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(getActivity());

        List<AccessibilityServiceInfo> installedServices =
                accessibilityManager.getInstalledAccessibilityServiceList();
        Set<ComponentName> enabledServices = AccessibilityUtils.getEnabledServicesFromSettings(
                getActivity());
        List<String> permittedServices = mDpm.getPermittedAccessibilityServices(
                UserHandle.myUserId());
        final boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;

        for (int i = 0, count = installedServices.size(); i < count; ++i) {
            AccessibilityServiceInfo info = installedServices.get(i);

            PreferenceScreen preference = getPreferenceManager().createPreferenceScreen(
                    getActivity());
            String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();

            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            ComponentName componentName = new ComponentName(serviceInfo.packageName,
                    serviceInfo.name);

            preference.setKey(componentName.flattenToString());

            preference.setTitle(title);
            final boolean serviceEnabled = accessibilityEnabled
                    && enabledServices.contains(componentName);
            String serviceEnabledString;
            if (serviceEnabled) {
                serviceEnabledString = getString(R.string.accessibility_feature_state_on);
            } else {
                serviceEnabledString = getString(R.string.accessibility_feature_state_off);
            }

            // Disable all accessibility services that are not permitted.
            String packageName = serviceInfo.packageName;
            boolean serviceAllowed =
                    permittedServices == null || permittedServices.contains(packageName);
            preference.setEnabled(serviceAllowed || serviceEnabled);

            String summaryString;
            if (serviceAllowed) {
                summaryString = serviceEnabledString;
            } else  {
                summaryString = getString(R.string.accessibility_feature_or_input_method_not_allowed);
            }
            preference.setSummary(summaryString);

            preference.setOrder(i);
            preference.setFragment(ToggleAccessibilityServicePreferenceFragment.class.getName());
            preference.setPersistent(true);

            Bundle extras = preference.getExtras();
            extras.putString(EXTRA_PREFERENCE_KEY, preference.getKey());
            extras.putBoolean(EXTRA_CHECKED, serviceEnabled);
            extras.putString(EXTRA_TITLE, title);

            String description = info.loadDescription(getPackageManager());
            if (TextUtils.isEmpty(description)) {
                description = getString(R.string.accessibility_service_default_description);
            }
            extras.putString(EXTRA_SUMMARY, description);

            CharSequence applicationLabel = info.getResolveInfo().loadLabel(getPackageManager());

            extras.putString(EXTRA_ENABLE_WARNING_TITLE, getString(
                    R.string.accessibility_service_security_warning_title, applicationLabel));
            extras.putString(EXTRA_ENABLE_WARNING_MESSAGE, getString(
                    R.string.accessibility_service_security_warning_summary, applicationLabel));

            extras.putString(EXTRA_DISABLE_WARNING_TITLE, getString(
                    R.string.accessibility_service_disable_warning_title,
                    applicationLabel));
            extras.putString(EXTRA_DISABLE_WARNING_MESSAGE, getString(
                    R.string.accessibility_service_disable_warning_summary,
                    applicationLabel));

            String settingsClassName = info.getSettingsActivityName();
            if (!TextUtils.isEmpty(settingsClassName)) {
                extras.putString(EXTRA_SETTINGS_TITLE,
                        getString(R.string.accessibility_menu_item_settings));
                extras.putString(EXTRA_SETTINGS_COMPONENT_NAME,
                        new ComponentName(info.getResolveInfo().serviceInfo.packageName,
                                settingsClassName).flattenToString());
            }

            extras.putParcelable(EXTRA_COMPONENT_NAME, componentName);

            //mServicesCategory.addPreference(preference);
            if (!isIgnoreService(info)) {
                mServicesCategory.addPreference(preference);
            }
        }

        if (mServicesCategory.getPreferenceCount() == 0) {
            if (mNoServicesMessagePreference == null) {
                mNoServicesMessagePreference = new Preference(getActivity()) {
                    @Override
                    protected void onBindView(View view) {
                        super.onBindView(view);

                        LinearLayout containerView =
                                (LinearLayout)view.findViewById(R.id.message_container);
                        containerView.setGravity(Gravity.CENTER);

                        TextView summaryView = (TextView)view.findViewById(R.id.summary);
                        String title = getString(R.string.accessibility_no_services_installed);
                        summaryView.setText(title);
                    }
                };
                mNoServicesMessagePreference.setPersistent(false);
                mNoServicesMessagePreference.setLayoutResource(
                        R.layout.text_description_preference);
                mNoServicesMessagePreference.setSelectable(false);
            }
            mServicesCategory.addPreference(mNoServicesMessagePreference);
        }
    }

    private boolean isIgnoreService(AccessibilityServiceInfo info) {
        Bundle b = info.getResolveInfo().serviceInfo.metaData;

        if (b == null) {
            return false;
        }

        return b.getBoolean("ignore");
    }

    private void updateSystemPreferences() {
        // Large text.
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException re) {
            Log.w(TAG, "RemoteException");
        }
        Log.d(TAG, "lcd_density = " + SystemProperties.get("ro.sf.lcd_density"));
        if (Integer.parseInt(SystemProperties.get("ro.sf.lcd_density")) <= LCD_DENSITY_MDPI) {
            mToggleLargeTextPreference.setChecked(mCurConfig.fontScale == LARGE_FONT_SCALE_LARGE);
        } else {
            if (!Utils.isUpgradeModel()) {
                int isMaxFont = Settings.Global.getInt(getContentResolver(), "sync_large_text", 0);
                mToggleLargeTextPreference.setChecked((mCurConfig.fontScale == LARGE_FONT_SCALE)
                        && (isMaxFont == 1));
            } else {
                mToggleLargeTextPreference.setChecked(mCurConfig.fontScale == LARGE_FONT_SCALE);
            }
        }

        mToggleHighTextContrastPreference.setChecked(
                Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0) == 1);

        // If the quick setting is enabled, the preference MUST be enabled.
        mToggleInversionPreference.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1);

        // Power button ends calls.
        if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                && Utils.isVoiceCapable(getActivity())) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mTogglePowerButtonEndsCallPreference.setChecked(powerButtonEndsCall);
        }

        // Auto-rotate screen
        updateLockScreenRotationCheckbox();

        // Speak passwords.
        final boolean speakPasswordEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0) != 0;
        mToggleSpeakPasswordPreference.setChecked(speakPasswordEnabled);

        // Long press timeout.
        final int longPressTimeout = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, mLongPressTimeoutDefault);
        String value = String.valueOf(longPressTimeout);
        mSelectLongPressTimeoutPreference.setValue(value);
        mSelectLongPressTimeoutPreference.setSummary(mLongPressTimeoutValuetoTitleMap.get(value));

        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED,
                mCaptioningPreferenceScreen);
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED,
                mDisplayMagnificationPreferenceScreen);
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED,
                mDisplayDaltonizerPreferenceScreen);

        // Global gesture
        final boolean globalGestureEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1;
        if (globalGestureEnabled) {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_on);
        } else {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_off);
        }
    }

    private void updateFeatureSummary(String prefKey, Preference pref) {
        final boolean enabled = Settings.Secure.getInt(getContentResolver(), prefKey, 0) == 1;
        pref.setSummary(enabled ? R.string.accessibility_feature_state_on
                : R.string.accessibility_feature_state_off);
    }

    private void updateLockScreenRotationCheckbox() {
        Context context = getActivity();
        if (context != null) {
            mToggleLockScreenRotationPreference.setChecked(
                    !RotationPolicy.isRotationLocked(context));
        }
    }

    private void offerInstallAccessibilitySerivceOnce() {
        // There is always one preference - if no services it is just a message.
        if (mServicesCategory.getPreference(0) != mNoServicesMessagePreference) {
            return;
        }
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean offerInstallService = !preferences.getBoolean(
                KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE, false);
        if (mServicesCategory.getPreference(0).getTitle() == null) {
            offerInstallService = true;
        }
        if (offerInstallService) {
            String screenreaderMarketLink = SystemProperties.get(
                    SYSTEM_PROPERTY_MARKET_URL,
                    DEFAULT_SCREENREADER_MARKET_LINK);
            Uri marketUri = Uri.parse(screenreaderMarketLink);
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);

            if (getPackageManager().resolveActivity(marketIntent, 0) == null) {
                // Don't show the dialog if no market app is found/installed.
                return;
            }
            if (mServicesCategory.getPreference(0).getTitle() == null) {
                preferences.edit().putBoolean(
                        KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE, false).commit();
            } else {
                preferences.edit().putBoolean(
                        KEY_INSTALL_ACCESSIBILITY_SERVICE_OFFERED_ONCE, true).commit();
            }
            // Notify user that they do not have any accessibility
            // services installed and direct them to Market to get TalkBack.
            //OPEN_CN_SETTINGS qingshan.song@lge.com fix crash in Chinese version[START]
            if ("CN".equals(Config.getCountry())) {
                //Do not show dialog in Chinese version
            }
            //OPEN_CN_SETTINGS qingshan.song@lge.com fix crash in Chinese version[END]
            else {
                showDialog(DIALOG_ID_NO_ACCESSIBILITY_SERVICES);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
        case DIALOG_ID_NO_ACCESSIBILITY_SERVICES:
            alertedialog =
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.accessibility_service_no_apps_title)
                            .setMessage(R.string.sp_accessibility_service_no_apps_message_NORMAL)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // dismiss the dialog before launching
                                            // the activity otherwise the dialog
                                            // removal occurs after
                                            // onSaveInstanceState which triggers an
                                            // exception
                                            removeDialog(DIALOG_ID_NO_ACCESSIBILITY_SERVICES);
                                            String screenreaderMarketLink = SystemProperties.get(
                                                    SYSTEM_PROPERTY_MARKET_URL,
                                                    DEFAULT_SCREENREADER_MARKET_LINK);
                                            Uri marketUri = Uri.parse(screenreaderMarketLink);
                                            Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                                                    marketUri);
                                            try {
                                                startActivity(marketIntent);
                                            } catch (ActivityNotFoundException anfe) {
                                                Log.w(TAG, "Couldn't start play store activity",
                                                        anfe);
                                            }
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
            return alertedialog;
        default:
            return null;
        }
    }

    private void loadInstalledServices() {
        Set<ComponentName> installedServices = sInstalledServices;
        installedServices.clear();

        List<AccessibilityServiceInfo> installedServiceInfos =
                AccessibilityManager.getInstance(getActivity())
                        .getInstalledAccessibilityServiceList();
        if (installedServiceInfos == null) {
            return;
        }

        final int installedServiceInfoCount = installedServiceInfos.size();
        for (int i = 0; i < installedServiceInfoCount; i++) {
            ResolveInfo resolveInfo = installedServiceInfos.get(i).getResolveInfo();
            ComponentName installedService = new ComponentName(
                    resolveInfo.serviceInfo.packageName,
                    resolveInfo.serviceInfo.name);
            installedServices.add(installedService);
        }
    }
}
