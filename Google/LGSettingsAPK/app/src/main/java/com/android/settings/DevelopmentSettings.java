/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.settings.lgesetting.Config.Config;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.app.backup.IBackupManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HardwareRenderer;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.media.AudioManager;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.hardware.usb.UsbManager;
import android.widget.TextView;

import dalvik.system.VMRuntime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.android.settings.lge.OverlayUtils;
import com.lge.constants.UsbManagerConstants;

import android.os.IDeviceManager3LM;

import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.preference.Preference.OnPreferenceClickListener;

/*
 * Displays preferences for application developers.
 */
public class DevelopmentSettings extends RestrictedSettingsFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
        OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener,
        CompoundButton.OnClickListener, OnPreferenceClickListener { //[2013.03.19][jamy.yeom]Add Switch sound
    private static final String TAG = "DevelopmentSettings";

    /**
     * Preference file were development settings prefs are stored.
     */
    public static final String PREF_FILE = "development";

    /**
     * Whether to show the development settings to the user.  Default is false.
     */
    public static final String PREF_SHOW = "show";
    public static final String PREF_HIDE = "hide";

    private static final String ENABLE_ADB = "enable_adb";
    private static final String CLEAR_ADB_KEYS = "clear_adb_keys";
    private static final String ENABLE_TERMINAL = "enable_terminal";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String BT_HCI_SNOOP_LOG = "bt_hci_snoop_log";
    private static final String ENABLE_OEM_UNLOCK = "oem_unlock_enable";
    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";
    private static final String HDCP_CHECKING_KEY = "hdcp_checking";
    private static final String HDCP_CHECKING_PROPERTY = "persist.sys.hdcp_checking";
    private static final String ENABLE_QUICKBOOT = "enable_quickboot";
    private static final String LOCAL_BACKUP_PASSWORD = "local_backup_password";
    private static final String HARDWARE_UI_PROPERTY = "persist.sys.ui.hw";
    private static final String MSAA_PROPERTY = "debug.egl.force_msaa";
    private static final String BUGREPORT = "bugreport";
    private static final String BUGREPORT_IN_POWER_KEY = "bugreport_in_power";
    private static final String OPENGL_TRACES_PROPERTY = "debug.egl.trace";

    private static final String DEBUG_APP_KEY = "debug_app";
    private static final String WAIT_FOR_DEBUGGER_KEY = "wait_for_debugger";
    private static final String VERIFY_APPS_OVER_USB_KEY = "verify_apps_over_usb";
    private static final String STRICT_MODE_KEY = "strict_mode";
    private static final String POINTER_LOCATION_KEY = "pointer_location";
    private static final String SHOW_TOUCHES_KEY = "show_touches";
    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
    private static final String DISABLE_OVERLAYS_KEY = "disable_overlays";
    private static final String SIMULATE_COLOR_SPACE = "simulate_color_space";
    private static final String USE_NUPLAYER_KEY = "use_nuplayer";
    private static final String USB_AUDIO_KEY = "usb_audio";
    private static final String USE_AWESOMEPLAYER_PROPERTY = "persist.sys.media.use-awesome";
    private static final String SHOW_CPU_USAGE_KEY = "show_cpu_usage";
    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
    private static final String FORCE_MSAA_KEY = "force_msaa";
    private static final String TRACK_FRAME_TIME_KEY = "track_frame_time";
    private static final String SHOW_NON_RECTANGULAR_CLIP_KEY = "show_non_rect_clip";
    private static final String SHOW_HW_SCREEN_UPDATES_KEY = "show_hw_screen_udpates";
    private static final String SHOW_HW_LAYERS_UPDATES_KEY = "show_hw_layers_udpates";
    private static final String DEBUG_HW_OVERDRAW_KEY = "debug_hw_overdraw";
    private static final String DEBUG_LAYOUT_KEY = "debug_layout";
    private static final String FORCE_RTL_LAYOUT_KEY = "force_rtl_layout_all_locales";
    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
    private static final String TRANSITION_ANIMATION_SCALE_KEY = "transition_animation_scale";
    private static final String ANIMATOR_DURATION_SCALE_KEY = "animator_duration_scale";
    private static final String OVERLAY_DISPLAY_DEVICES_KEY = "overlay_display_devices";
    private static final String DEBUG_DEBUGGING_CATEGORY_KEY = "debug_debugging_category";
    private static final String DEBUG_APPLICATIONS_CATEGORY_KEY = "debug_applications_category";
    private static final String WIFI_DISPLAY_CERTIFICATION_KEY = "wifi_display_certification";
    private static final String WIFI_VERBOSE_LOGGING_KEY = "wifi_verbose_logging";
    private static final String WIFI_AGGRESSIVE_HANDOVER_KEY = "wifi_aggressive_handover";
    private static final String WIFI_ALLOW_SCAN_WITH_TRAFFIC_KEY = "wifi_allow_scan_with_traffic";
    private static final String SELECT_LOGD_SIZE_KEY = "select_logd_size";
    private static final String SELECT_LOGD_SIZE_PROPERTY = "persist.logd.size";
    private static final String SELECT_LOGD_DEFAULT_SIZE_PROPERTY = "ro.logd.size";

    private static final String OPENGL_TRACES_KEY = "enable_opengl_traces";

    private static final String IMMEDIATELY_DESTROY_ACTIVITIES_KEY = "immediately_destroy_activities";
    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";

    private static final String SHOW_ALL_ANRS_KEY = "show_all_anrs";

    private static final String WEBVIEW_DATA_REDUCTION_PROXY_KEY = "webview_data_reduction_proxy";

    private static final String PROCESS_STATS = "proc_stats";

    //private static final String TAG_CONFIRM_ENFORCE = "confirm_enforce";

    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

    private static final String TERMINAL_APP_PACKAGE = "com.android.terminal";

    private static final int RESULT_DEBUG_APP = 1000;
    // [S][2012.03.29][changyu0218.lee@lge.com][D1L]Add wireless charging
    private static final int BATTERY_PLUGGED_WIRELESS = 4;
    // [E][2012.03.29][changyu0218.lee@lge.com][D1L]Add wireless charging

    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";

    private static String sDEFAULT_LOG_RING_BUFFER_SIZE_IN_BYTES = "262144"; // 256K

    private static final String KEY_DEVELOPMENT_SETTINGS_MIRRORLINK = "development_settings_mirrorlink";
    private static final int DEVELOPER_ID_MAXLEN = 40;

    private IWindowManager mWindowManager;
    private IBackupManager mBackupManager;
    private DevicePolicyManager mDpm;
    private UserManager mUm;
    private WifiManager mWifiManager;

    private Switch mEnabledSwitch;
    private boolean mLastEnabledState;
    private boolean mHaveDebugSettings;
    private boolean mDontPokeProperties;

    private CheckBoxPreference mEnableAdb;
    // [S][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
    SettingsBreadCrumb mBreadCrumb;
    // [E][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
    private Preference mClearAdbKeys;
    private CheckBoxPreference mEnableTerminal;
    private Preference mBugreport;
    private CheckBoxPreference mBugreportInPower;
    private CheckBoxPreference mKeepScreenOn;
    private CheckBoxPreference mBtHciSnoopLog;
    private CheckBoxPreference mEnableOemUnlock;
    private CheckBoxPreference mQuickBoot;
    private CheckBoxPreference mAllowMockLocation;
    private PreferenceScreen mPassword;

    private String mDebugApp;
    private Preference mDebugAppPref;
    private CheckBoxPreference mWaitForDebugger;
    private CheckBoxPreference mVerifyAppsOverUsb;
    private CheckBoxPreference mWifiDisplayCertification;
    private CheckBoxPreference mWifiVerboseLogging;
    private CheckBoxPreference mWifiAggressiveHandover;

    private CheckBoxPreference mWifiAllowScansWithTraffic;
    private CheckBoxPreference mStrictMode;
    private CheckBoxPreference mPointerLocation;
    private CheckBoxPreference mShowTouches;
    private CheckBoxPreference mShowScreenUpdates;
    private CheckBoxPreference mDisableOverlays;
    private CheckBoxPreference mShowCpuUsage;
    private CheckBoxPreference mForceHardwareUi;
    private CheckBoxPreference mForceMsaa;
    private CheckBoxPreference mShowHwScreenUpdates;
    private CheckBoxPreference mShowHwLayersUpdates;
    private CheckBoxPreference mDebugLayout;
    private CheckBoxPreference mForceRtlLayout;
    private ListPreference mDebugHwOverdraw;
    private ListPreference mLogdSize;
    private ListPreference mTrackFrameTime;
    private ListPreference mShowNonRectClip;
    private ListPreference mWindowAnimationScale;
    private ListPreference mTransitionAnimationScale;
    private ListPreference mAnimatorDurationScale;
    private ListPreference mOverlayDisplayDevices;
    private ListPreference mOpenGLTraces;

    private ListPreference mSimulateColorSpace;

    private CheckBoxPreference mUseNuplayer;
    private CheckBoxPreference mUSBAudio;
    private CheckBoxPreference mImmediatelyDestroyActivities;

    private ListPreference mAppProcessLimit;

    private CheckBoxPreference mShowAllANRs;

    private CheckBoxPreference mWebViewDataReductionProxy;

    private EditTextPreference mMirrorLinkEditPref;
    private DeveloperIDEditTextPreference mDeveloperIDEditPref;

    //[S][2012.02.07][changyu0218.lee@lge.com][U0][5527] Add Connectivity action
    private UsbManager mUsbManager = null;
    //private boolean adbChecked = false;
    private boolean usbConnected = false;
    //[E][2012.02.07][changyu0218.lee@lge.com][U0][5527] Add Connectivity action

    private PreferenceScreen mProcessStats;
    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
    private final ArrayList<CheckBoxPreference> mResetCbPrefs = new ArrayList<CheckBoxPreference>();

    private final HashSet<Preference> mDisabledPrefs = new HashSet<Preference>();

    // To track whether a confirmation dialog was clicked.
    private boolean mDialogClicked;
    private Dialog mEnableDialog;
    private Dialog mAdbDialog;
    private Dialog mAdbKeysDialog;
    private Dialog mAttentionDialog;

    private CheckBox visible;

    private boolean mUnavailable;

    private static final String MirrorLink_APP_PACKAGE = "com.lge.mirrorlinkcertupdate";

    public DevelopmentSettings() {
        super(RESTRICT_IF_OVERRIDABLE);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        mDpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if (android.os.Process.myUserHandle().getIdentifier() != UserHandle.USER_OWNER
                || mUm.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES)) {
            mUnavailable = true;
            setPreferenceScreen(new PreferenceScreen(getActivity(), null));
            return;
        }

        addPreferencesFromResource(R.xml.development_prefs);

        final PreferenceGroup debugDebuggingCategory = (PreferenceGroup)
                findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);

        mEnableAdb = findAndInitCheckboxPref(ENABLE_ADB);
        mClearAdbKeys = findPreference(CLEAR_ADB_KEYS);
        if (!SystemProperties.getBoolean("ro.adb.secure", false)) {
            if (debugDebuggingCategory != null) {
                debugDebuggingCategory.removePreference(mClearAdbKeys);
            }
        }
        mAllPrefs.add(mClearAdbKeys);
        mEnableTerminal = findAndInitCheckboxPref(ENABLE_TERMINAL);
        if (!isPackageInstalled(getActivity(), TERMINAL_APP_PACKAGE)) {
            if (debugDebuggingCategory != null) { //[WBT] NPE.COND (TD2170525088)
                debugDebuggingCategory.removePreference(mEnableTerminal);
            }
            mEnableTerminal = null;
        }

        mBugreport = findPreference(BUGREPORT);
        mBugreportInPower = findAndInitCheckboxPref(BUGREPORT_IN_POWER_KEY);
        mKeepScreenOn = findAndInitCheckboxPref(KEEP_SCREEN_ON);
        mBtHciSnoopLog = findAndInitCheckboxPref(BT_HCI_SNOOP_LOG);
        mEnableOemUnlock = findAndInitCheckboxPref(ENABLE_OEM_UNLOCK);
        if (!showEnableOemUnlockPreference()) {
            removePreference(mEnableOemUnlock);
            mEnableOemUnlock = null;
        }
        mQuickBoot = findAndInitCheckboxPref(ENABLE_QUICKBOOT);
        getPreferenceScreen().removePreference(mQuickBoot);

        mAllowMockLocation = findAndInitCheckboxPref(ALLOW_MOCK_LOCATION);
        mPassword = (PreferenceScreen)findPreference(LOCAL_BACKUP_PASSWORD);
        mAllPrefs.add(mPassword);

        if (!android.os.Process.myUserHandle().equals(UserHandle.OWNER)) {
            disableForUser(mEnableAdb);
            disableForUser(mClearAdbKeys);
            disableForUser(mEnableTerminal);
            disableForUser(mPassword);
        }

        mDebugAppPref = findPreference(DEBUG_APP_KEY);
        mAllPrefs.add(mDebugAppPref);
        mWaitForDebugger = findAndInitCheckboxPref(WAIT_FOR_DEBUGGER_KEY);
        mVerifyAppsOverUsb = findAndInitCheckboxPref(VERIFY_APPS_OVER_USB_KEY);
        if (!showVerifierSetting()) {
            if (debugDebuggingCategory != null) {
                debugDebuggingCategory.removePreference(mVerifyAppsOverUsb);
            } else {
                mVerifyAppsOverUsb.setEnabled(false);
            }
        }
        mStrictMode = findAndInitCheckboxPref(STRICT_MODE_KEY);
        mPointerLocation = findAndInitCheckboxPref(POINTER_LOCATION_KEY);
        mShowTouches = findAndInitCheckboxPref(SHOW_TOUCHES_KEY);
        mShowScreenUpdates = findAndInitCheckboxPref(SHOW_SCREEN_UPDATES_KEY);
        mDisableOverlays = findAndInitCheckboxPref(DISABLE_OVERLAYS_KEY);
        mShowCpuUsage = findAndInitCheckboxPref(SHOW_CPU_USAGE_KEY);
        mForceHardwareUi = findAndInitCheckboxPref(FORCE_HARDWARE_UI_KEY);
        mForceMsaa = findAndInitCheckboxPref(FORCE_MSAA_KEY);
        mTrackFrameTime = addListPreference(TRACK_FRAME_TIME_KEY);
        mShowNonRectClip = addListPreference(SHOW_NON_RECTANGULAR_CLIP_KEY);
        mShowHwScreenUpdates = findAndInitCheckboxPref(SHOW_HW_SCREEN_UPDATES_KEY);
        mShowHwLayersUpdates = findAndInitCheckboxPref(SHOW_HW_LAYERS_UPDATES_KEY);
        mDebugLayout = findAndInitCheckboxPref(DEBUG_LAYOUT_KEY);
        mForceRtlLayout = findAndInitCheckboxPref(FORCE_RTL_LAYOUT_KEY);
        mDebugHwOverdraw = addListPreference(DEBUG_HW_OVERDRAW_KEY);
        mWifiDisplayCertification = findAndInitCheckboxPref(WIFI_DISPLAY_CERTIFICATION_KEY);
        mWifiVerboseLogging = findAndInitCheckboxPref(WIFI_VERBOSE_LOGGING_KEY);
        mWifiAggressiveHandover = findAndInitCheckboxPref(WIFI_AGGRESSIVE_HANDOVER_KEY);
        mWifiAllowScansWithTraffic = findAndInitCheckboxPref(WIFI_ALLOW_SCAN_WITH_TRAFFIC_KEY);
        mLogdSize = addListPreference(SELECT_LOGD_SIZE_KEY);

        mWindowAnimationScale = addListPreference(WINDOW_ANIMATION_SCALE_KEY);
        mTransitionAnimationScale = addListPreference(TRANSITION_ANIMATION_SCALE_KEY);
        mAnimatorDurationScale = addListPreference(ANIMATOR_DURATION_SCALE_KEY);
        mOverlayDisplayDevices = addListPreference(OVERLAY_DISPLAY_DEVICES_KEY);
        mOpenGLTraces = addListPreference(OPENGL_TRACES_KEY);
        mSimulateColorSpace = addListPreference(SIMULATE_COLOR_SPACE);
        mUseNuplayer = findAndInitCheckboxPref(USE_NUPLAYER_KEY);
        mUSBAudio = findAndInitCheckboxPref(USB_AUDIO_KEY);

        mImmediatelyDestroyActivities = (CheckBoxPreference)findPreference(
                IMMEDIATELY_DESTROY_ACTIVITIES_KEY);
        mAllPrefs.add(mImmediatelyDestroyActivities);
        mResetCbPrefs.add(mImmediatelyDestroyActivities);
        mAppProcessLimit = addListPreference(APP_PROCESS_LIMIT_KEY);

        mShowAllANRs = (CheckBoxPreference)findPreference(
                SHOW_ALL_ANRS_KEY);
        mAllPrefs.add(mShowAllANRs);
        mResetCbPrefs.add(mShowAllANRs);

        Preference hdcpChecking = findPreference(HDCP_CHECKING_KEY);
        if (hdcpChecking != null) {
            mAllPrefs.add(hdcpChecking);
            removePreferenceForProduction(hdcpChecking);
        }
        mProcessStats = (PreferenceScreen)findPreference(PROCESS_STATS);
        mAllPrefs.add(mProcessStats);

        mWebViewDataReductionProxy = findAndInitCheckboxPref(WEBVIEW_DATA_REDUCTION_PROXY_KEY);
        String key = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.WEBVIEW_DATA_REDUCTION_PROXY_KEY);
        // Disable the selection if the key is not available for some reason.
        if (key == null || key.isEmpty()) {
            disableForUser(mWebViewDataReductionProxy);
        }
        //[S][2012.06.29][changyu0218.lee]Remove Strict mode
        removeStrictMode();
        //[E][2012.06.29][changyu0218.lee]Remove Strict mode

        //[S][2012.06.18][yongjaeo.lee][E0][NA] Move developer settings to inside of about phone from top header (ORG request)
        if ("ORG".equals(SystemProperties.get("ro.build.target_operator"))) {
            getActivity().setTitle(getString(R.string.advanced_settings));
        }
        //[E][2012.06.18][yongjaeo.lee][E0][NA] Move developer settings to inside of about phone from top header (ORG request)
        //[S][2012.05.17][changyu0218.lee@lge.com][Common] Add popup entering development options VZW models
        showEnteringDialog();
        //[E][2012.05.17][changyu0218.lee@lge.com][Common] Add popup entering development options VZW models
        //[S][2012.8.31][changyu0218.lee@lge.com][Common] Add options menu Reset to default
        setHasOptionsMenu(true);
        //[E][2012.8.31][changyu0218.lee@lge.com][Common] Add options menu Reset to default

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-74][ID-MDM-258][ID-MDM-207][ID-MDM-413]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addDevelopmentPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        // [S][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
        if (Config.SPR.equals(Config.getOperator()) || Config.BM.equals(Config.getOperator()))
        {
            if (false == OverlayUtils.getAllowUsbPort(null))
            {
                mEnableAdb.setEnabled(false);
            }
        }
        // [E][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.

        final PreferenceGroup debugApplicationsCategory = (PreferenceGroup)
                findPreference(DEBUG_APPLICATIONS_CATEGORY_KEY);
        mMirrorLinkEditPref = (EditTextPreference)findPreference(KEY_DEVELOPMENT_SETTINGS_MIRRORLINK);

        if (mMirrorLinkEditPref != null) {
            if (!isSupportMirrorLink(getActivity())) {
                Log.d(TAG, "This device doesn't support MirrorLink");
                if (debugApplicationsCategory != null) {
                    debugApplicationsCategory.removePreference(mMirrorLinkEditPref);
                }
            } else {
                initMirrorLinkDevPref();
            }
        }

    }

    private void initMirrorLinkDevPref() {
        mMirrorLinkEditPref.setOnPreferenceClickListener(this);
        mMirrorLinkEditPref.getEditText().setFilters(new InputFilter[] {
                new Utf8ByteLengthFilter(DEVELOPER_ID_MAXLEN,
                        getActivity(), mMirrorLinkEditPref.getEditText())
        });

        mDeveloperIDEditPref = new DeveloperIDEditTextPreference(getActivity(), null);
        mDeveloperIDEditPref.setDeveloperIDPref(mMirrorLinkEditPref);

        mMirrorLinkEditPref.setSummary(read_db_developerID());
        mMirrorLinkEditPref.setText(read_db_developerID());
        mMirrorLinkEditPref.getEditText().addTextChangedListener(mDeveloperIDEditPref);
    }

    public static boolean isSupportMirrorLink(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(MirrorLink_APP_PACKAGE, 0) != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public String read_db_developerID() {
        String id = Settings.Global.getString(getContentResolver(), "mirrorlink_developer_id");
        if (id == null) {
            Log.d(TAG, "read_db_developerID() : id : (on DB) , default ~~~ : "
                    + id);
            id = getString(R.string.development_settings_mirrorlink_developer_id);
        } else {
            Log.d(TAG, "read_db_developerID() : id : (on DB) " + id);
        }
        return id;
    }

    public boolean onPreferenceClick(Preference pref) {
        Log.d(TAG, "onPreferenceClick");

        if (mMirrorLinkEditPref != null) {
            mMirrorLinkEditPref.setText(read_db_developerID());
            mMirrorLinkEditPref.getEditText().selectAll();
        }
        return false;
    }

    private ListPreference addListPreference(String prefKey) {
        ListPreference pref = (ListPreference)findPreference(prefKey);
        mAllPrefs.add(pref);
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    private void disableForUser(Preference pref) {
        if (pref != null) {
            pref.setEnabled(false);
            mDisabledPrefs.add(pref);
        }
    }

    private CheckBoxPreference findAndInitCheckboxPref(String key) {
        CheckBoxPreference pref = (CheckBoxPreference)findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        mAllPrefs.add(pref);
        mResetCbPrefs.add(pref);
        return pref;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        mEnabledSwitch = new Switch(activity);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mEnabledSwitch.setPaddingRelative(0, 0, padding, 0);
        if (mUnavailable) {
            mEnabledSwitch.setEnabled(false);
            return;
        }
        mEnabledSwitch.setOnCheckedChangeListener(this);
        mEnabledSwitch.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        // [S][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity()) == false)
        // [E][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mEnabledSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        // [S][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity()) == false)
        // [E][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        {
            activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(null);
        }
    }

    private boolean removePreferenceForProduction(Preference preference) {
        if ("user".equals(Build.TYPE)) {
            removePreference(preference);
            return true;
        }
        return false;
    }

    //[S][2012.06.29][changyu0218.lee]Remove Strict mode
    private void removeStrictMode() {
    }

    //[E][2012.06.29][changyu0218.lee]Remove Strict mode

    private void removePreference(Preference preference) {
        getPreferenceScreen().removePreference(preference);
        mAllPrefs.remove(preference);
    }

    private void setPrefsEnabledState(boolean enabled) {
        for (int i = 0; i < mAllPrefs.size(); i++) {
            Preference pref = mAllPrefs.get(i);
            pref.setEnabled(enabled && !mDisabledPrefs.contains(pref));
        }
        updateAllOptions();
    }

    @Override
    public void onResume() {
        super.onResume();
        // [S][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mEnabledSwitch);
            }
        }
        // [E][2013.09.02][ry.jeong@lge.com] Add switch for tablet.

        if (mUnavailable) {
            // Show error message
            TextView emptyView = (TextView)getView().findViewById(android.R.id.empty);
            getListView().setEmptyView(emptyView);
            if (emptyView != null) {
                emptyView.setText(R.string.development_settings_not_available);
            }
            return;
        }

        if (mDpm.getMaximumTimeToLock(null) > 0) {
            // A DeviceAdmin has specified a maximum time until the device
            // will lock...  in this case we can't allow the user to turn
            // on "stay awake when plugged in" because that would defeat the
            // restriction.
            mDisabledPrefs.add(mKeepScreenOn);
        } else {
            mDisabledPrefs.remove(mKeepScreenOn);
        }

        final ContentResolver cr = getActivity().getContentResolver();

        // 3LM_MDM L taewon.jang@lge.com
        // Check if 3LM has disabled ADB, if so then gray out the item
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Global.getInt(cr, Settings.Global.ADB_BLOCKED, 0) == 1) {
                mDisabledPrefs.add(mEnableAdb);
            } else {
                mDisabledPrefs.remove(mEnableAdb);
            }
        }
        // 3LM_MDM L END
        mLastEnabledState = Settings.Global.getInt(cr,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        mEnabledSwitch.setChecked(mLastEnabledState);
        setPrefsEnabledState(mLastEnabledState);

        if (mHaveDebugSettings && !mLastEnabledState) {
            // Overall debugging is disabled, but there are some debug
            // settings that are enabled.  This is an invalid state.  Switch
            // to debug settings being enabled, so the user knows there is
            // stuff enabled and can turn it all off if they want.
            Settings.Global.putInt(getActivity().getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            mLastEnabledState = true;
            mEnabledSwitch.setChecked(mLastEnabledState);
            setPrefsEnabledState(mLastEnabledState);
        }

        updateAllOptions();

        //[S][2012.02.07][changyu0218.lee@lge.com][U0][5527] Add Connectivity action
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
        //[E][2012.02.07][changyu0218.lee@lge.com][U0][5527] Add Connectivity action
        // [S][2012.03.09][changyu0218.lee@lge.com][Common]Fix adb check state keep by lock & unlock
        if (mAdbDialog != null && mLastEnabledState) {
            mEnableAdb.setChecked(true);
        }
        // [E][2012.03.09][changyu0218.lee@lge.com][Common]Fix adb check state keep by lock & unlock

        //[S][2012.04.19][changyu0218.lee@lge.com][NA] Add VZW exception case
        if (usbConnected && mLastEnabledState) {
            if (Config.VZW.equals(Config.getOperator()))
            {
                Context mContext = (Context)getActivity();
                if (mUsbManager == null) {
                    mUsbManager = (UsbManager)mContext.getSystemService(mContext.USB_SERVICE);
                }
                if (!(mUsbManager.getDefaultFunction()
                        .equals(UsbManagerConstants.USB_FUNCTION_TETHER))
                        && !(mUsbManager.getDefaultFunction()
                                .equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)))
                {
                    mEnableAdb.setEnabled(false);
                }
                else {
                    mEnableAdb.setEnabled(true);
                }
            }
        }
        //[E][2012.04.19][changyu0218.lee@lge.com][NA] Add VZW exception case

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-258][ID-MDM-74][ID-MDM-290]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setAdbEnableMenu(mEnableAdb);
            MDMSettingsAdapter.getInstance().setProcessLimitMenu(mAppProcessLimit);
        }
        // LGMDM_END

        // [S][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
        if (Config.SPR.equals(Config.getOperator()) || Config.BM.equals(Config.getOperator()))
        {
            if (false == OverlayUtils.getAllowUsbPort(null))
            {
                mEnableAdb.setEnabled(false);
            }
        }
        // [E][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
    }

    void updateCheckBox(CheckBoxPreference checkBox, boolean value) {
        checkBox.setChecked(value);
        mHaveDebugSettings |= value;
        if (checkBox == mShowScreenUpdates) {
            Settings.System.putInt(getActivity().getContentResolver(), "Screen_Update_Flag",
                    value ? 1 : 0);
        }
    }

    private void updateAllOptions() {
        final Context context = getActivity();
        final ContentResolver cr = context.getContentResolver();
        mHaveDebugSettings = false;
        Log.i(TAG, "[Start]Display DevelopmentSettings check status");
        Log.i(TAG, "Build Type : " + Build.TYPE);

        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            // If 3LM blocks ADB, checkbox needs to be disabled
            updateCheckBox(
                    mEnableAdb,
                    (Settings.Secure.getInt(cr, Settings.Secure.ADB_ENABLED, 0) != 0) &&
                            (Settings.Secure.getInt(cr, "adb_blocked", 0) == 0));
            // Check if 3LM has disabled ADB, if so then gray out the item too
            mEnableAdb.setEnabled(Settings.Secure.getInt(cr, "adb_blocked", 0) == 0);
        } else {
            // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
            updateCheckBox(mEnableAdb, Settings.Global.getInt(cr,
                    Settings.Global.ADB_ENABLED, 0) != 0);
        }

        //[S][2013.05.04][jamy.yeom] USB Debugging Option disable when the main switch is off.
        if (!mLastEnabledState && !mEnabledSwitch.isChecked()) {
            Log.i(TAG, "updateAllOptions()   : mLastEnabledState :" + mLastEnabledState
                    + ", mEnabledSwitch.isChecked() :" + mEnabledSwitch.isChecked());
            mEnableAdb.setEnabled(false);
        }
        //[E][2013.05.04][jamy.yeom] USB Debugging Option disable when the main switch is off.

        // [S][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
        if (Config.SPR.equals(Config.getOperator()) || Config.BM.equals(Config.getOperator()))
        {
            if (false == OverlayUtils.getAllowUsbPort(null))
            {
                mEnableAdb.setEnabled(false);
            }
        }
        // [E][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.

        if (mEnableTerminal != null) {
            updateCheckBox(mEnableTerminal,
                    context.getPackageManager().getApplicationEnabledSetting(TERMINAL_APP_PACKAGE)
                    == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        }
        updateCheckBox(mBugreportInPower, Settings.Secure.getInt(cr,
                Settings.Secure.BUGREPORT_IN_POWER_MENU, 0) != 0);
        Log.i(TAG, "USB debugging checked : " + mEnableAdb.isChecked());
        updateCheckBox(mKeepScreenOn, Settings.Global.getInt(cr,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
        Log.i(TAG, "Stay awake checked : " + mKeepScreenOn.isChecked());
        updateCheckBox(mBtHciSnoopLog, Settings.Secure.getInt(cr,
                Settings.Secure.BLUETOOTH_HCI_LOG, 0) != 0);
        if (mEnableOemUnlock != null) {
            updateCheckBox(mEnableOemUnlock, Utils.isOemUnlockEnabled(getActivity()));
        }
        updateCheckBox(mAllowMockLocation, Settings.Secure.getInt(cr,
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
        Log.i(TAG, "Allow mock locations checked : " + mAllowMockLocation.isChecked());
        updateHdcpValues();
        updatePasswordSummary();
        updateDebuggerOptions();
        updateStrictModeVisualOptions();
        updatePointerLocationOptions();
        updateShowTouchesOptions();
        updateFlingerOptions();
        updateCpuUsageOptions();
        updateHardwareUiOptions();
        updateMsaaOptions();
        updateTrackFrameTimeOptions();
        updateShowNonRectClipOptions();
        updateShowHwScreenUpdatesOptions();
        updateShowHwLayersUpdatesOptions();
        updateDebugHwOverdrawOptions();
        updateDebugLayoutOptions();
        updateAnimationScaleOptions();
        updateOverlayDisplayDevicesOptions();
        updateOpenGLTracesOptions();
        updateImmediatelyDestroyActivitiesOptions();
        updateAppProcessLimitOptions();
        updateShowAllANRsOptions();
        updateWebViewDataReductionProxyOptions();
        updateVerifyAppsOverUsbOptions();
        updateBugreportOptions();
        updateForceRtlOptions();
        updateLogdSizeValues();
        updateWifiDisplayCertificationOptions();
        updateWifiVerboseLoggingOptions();
        updateWifiAggressiveHandoverOptions();
        updateWifiAllowScansWithTrafficOptions();
        updateSimulateColorSpace();
        updateUseNuplayerOptions();
        updateUSBAudioOptions();
        Log.i(TAG, "[End]Display DevelopmentSettings check status");
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-258][ID-MDM-74][ID-MDM-207]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setAdbEnableMenu(mEnableAdb);
            MDMSettingsAdapter.getInstance().setMockLocationMenu(mAllowMockLocation);
        }
        // LGMDM_END

    }

    private void resetDangerousOptions() {
        mDontPokeProperties = true;
        for (int i = 0; i < mResetCbPrefs.size(); i++) {
            CheckBoxPreference cb = mResetCbPrefs.get(i);
            if (cb.isChecked()) {
                cb.setChecked(false);
                onPreferenceTreeClick(null, cb);
            }
        }
        resetDebuggerOptions();
        writeLogdSizeOption(null);
        writeAnimationScaleOption(0, mWindowAnimationScale, null);
        writeAnimationScaleOption(1, mTransitionAnimationScale, null);
        writeAnimationScaleOption(2, mAnimatorDurationScale, null);
        // Only poke the color space setting if we control it.
        if (usingDevelopmentColorSpace()) {
            writeSimulateColorSpace(-1);
        }
        writeOverlayDisplayDevicesOptions(null);
        writeAppProcessLimitOptions(null);
        mHaveDebugSettings = false;
        updateAllOptions();
        mDontPokeProperties = false;
        pokeSystemProperties();
    }
    private boolean isGKmodel() {
        if ("geefhd4g_lgu_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_skt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_kt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "omega_lgu_kr".equals(SystemProperties.get("ro.product.name")) ||
                "gvfhd_kt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geevl04e".equals(SystemProperties.get("ro.product.name")) ||
                "geevl04e".equals(Build.DEVICE) ||
                "geefhd_open_hk".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_tw".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_cis".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_my".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_stl_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_shb_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_esa".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_ame".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_il".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_att_us".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_tcl_mx".equals(SystemProperties.get("ro.product.name")) ||
                "KDDI".equals(SystemProperties.get("ro.build.target_operator")) ||
                "mt6572".equals(SystemProperties.get("ro.product.board")) ||
                "mt6577".equals(SystemProperties.get("ro.product.board")) ||
                "altev".equals(Build.DEVICE) ||
                "geehrc4g_lgu_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geehrc_kt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geehrc_vdf_eu".equals(SystemProperties.get("ro.product.name")) ||
                "geehrc_skt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "mt6582".equals(SystemProperties.get("ro.board.platform"))) { //GK hongkong
            return true;
        }
        else {
            return false;
        }
    }

    private void updateHdcpValues() {
        ListPreference hdcpChecking = (ListPreference)findPreference(HDCP_CHECKING_KEY);
        if (hdcpChecking != null) {
            String currentValue = SystemProperties.get(HDCP_CHECKING_PROPERTY);
            String[] values = getResources().getStringArray(R.array.hdcp_checking_values);
            String[] summaries = getResources().getStringArray(R.array.hdcp_checking_summaries);
            int index = 1; // Defaults to drm-only. Needs to match with R.array.hdcp_checking_values
            for (int i = 0; i < values.length; i++) {
                if (currentValue.equals(values[i])) {
                    index = i;
                    break;
                }
            }
            hdcpChecking.setValue(values[index]);
            hdcpChecking.setSummary(summaries[index]);
            hdcpChecking.setOnPreferenceChangeListener(this);
        }
    }

    private void updatePasswordSummary() {
        try {
            if (mBackupManager.hasBackupPassword()) {
                mPassword.setSummary(R.string.local_backup_password_summary_change);
            } else {
                mPassword.setSummary(R.string.local_backup_password_summary_none);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeBtHciSnoopLogOptions() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.configHciSnoopLog(mBtHciSnoopLog.isChecked());
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.BLUETOOTH_HCI_LOG,
                mBtHciSnoopLog.isChecked() ? 1 : 0);
    }

    private void writeDebuggerOptions() {
        try {
            ActivityManagerNative.getDefault().setDebugApp(
                    mDebugApp, mWaitForDebugger.isChecked(), true);
            Log.i(TAG, "Wait for debugger checked : " + mWaitForDebugger.isChecked());
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private static void resetDebuggerOptions() {
        try {
            ActivityManagerNative.getDefault().setDebugApp(
                    null, false, true);
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateDebuggerOptions() {
        mDebugApp = Settings.Global.getString(
                getActivity().getContentResolver(), Settings.Global.DEBUG_APP);
        updateCheckBox(mWaitForDebugger, Settings.Global.getInt(
                getActivity().getContentResolver(), Settings.Global.WAIT_FOR_DEBUGGER, 0) != 0);
        Log.i(TAG, "Wait for debugger checked : " + mWaitForDebugger.isChecked());
        if (mDebugApp != null && mDebugApp.length() > 0) {
            String label;
            try {
                ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(
                        mDebugApp,
                        PackageManager.GET_DISABLED_COMPONENTS);
                CharSequence lab = getActivity().getPackageManager().getApplicationLabel(ai);
                label = lab != null ? lab.toString() : mDebugApp;
            } catch (PackageManager.NameNotFoundException e) {
                label = mDebugApp;
            }
            mDebugAppPref.setSummary(getResources().getString(R.string.debug_app_set, label));
            mWaitForDebugger.setEnabled(true);
            mHaveDebugSettings = true;
        } else {
            mDebugAppPref.setSummary(getResources().getString(R.string.debug_app_not_set));
            mWaitForDebugger.setEnabled(false);
        }
    }

    private void updateVerifyAppsOverUsbOptions() {
        updateCheckBox(mVerifyAppsOverUsb,
                Settings.Global.getInt(getActivity().getContentResolver(),
                        Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 1) != 0);
        mVerifyAppsOverUsb.setEnabled(enableVerifierSetting());
    }

    private void writeVerifyAppsOverUsbOptions() {
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, mVerifyAppsOverUsb.isChecked() ? 1
                        : 0);
    }

    private boolean enableVerifierSetting() {
        final ContentResolver cr = getActivity().getContentResolver();
        if (Settings.Global.getInt(cr, Settings.Global.ADB_ENABLED, 0) == 0) {
            return false;
        }
        if (Settings.Global.getInt(cr, Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) == 0) {
            return false;
        } else {
            final PackageManager pm = getActivity().getPackageManager();
            final Intent verification = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
            verification.setType(PACKAGE_MIME_TYPE);
            verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final List<ResolveInfo> receivers = pm.queryBroadcastReceivers(verification, 0);
            if (receivers.size() == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean showVerifierSetting() {
        return Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_SETTING_VISIBLE, 1) > 0;
    }

    private static boolean showEnableOemUnlockPreference() {
        return !SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("");
    }

    private void updateBugreportOptions() {
        if ("user".equals(Build.TYPE)) {
            /* //[S][2013.03.19][jamy.yeom]Remove TakeBugReport, TakebugreportInpower menu on user mode - revert
                        PreferenceCategory category = (PreferenceCategory)getPreferenceScreen().findPreference("debug_debugging_category");

                        if (category != null)
                        {
                            category.removePreference(mBugreportInPower);
                        }

                        if (mBugreport != null) {
                            // Remove the preference
                            getPreferenceScreen().removePreference(mBugreport);
                            mAllPrefs.remove(mBugreport);
                        }
                        else {
                            throw new IllegalArgumentException("Cannot find preference with key ");
                        }

            */
            //[E][2013.03.19][jamy.yeom]Remove TakeBugReport menu on user mode
            final ContentResolver resolver = getActivity().getContentResolver();
            final boolean adbEnabled = Settings.Global.getInt(
                    resolver, Settings.Global.ADB_ENABLED, 0) != 0;
            if (adbEnabled) {
                mBugreport.setEnabled(true);
                mBugreportInPower.setEnabled(true);
            } else {
                mBugreport.setEnabled(false);
                mBugreportInPower.setEnabled(false);
                mBugreportInPower.setChecked(false);
                Settings.Secure.putInt(resolver, Settings.Secure.BUGREPORT_IN_POWER_MENU, 0);
            }

        } else {
            mBugreportInPower.setEnabled(true);
        }
    }

    // Returns the current state of the system property that controls
    // strictmode flashes.  One of:
    //    0: not explicitly set one way or another
    //    1: on
    //    2: off
    private static int currentStrictModeActiveIndex() {
        if (TextUtils.isEmpty(SystemProperties.get(StrictMode.VISUAL_PROPERTY))) {
            return 0;
        }
        boolean enabled = SystemProperties.getBoolean(StrictMode.VISUAL_PROPERTY, false);
        return enabled ? 1 : 2;
    }

    private void writeStrictModeVisualOptions() {
        try {
            mWindowManager.setStrictModeVisualIndicatorPreference(mStrictMode.isChecked()
                    ? "1" : "");
            Log.i(TAG, "Strict mode checked : " + mStrictMode.isChecked());
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateStrictModeVisualOptions() {
        updateCheckBox(mStrictMode, currentStrictModeActiveIndex() == 1);
        Log.i(TAG, "Strict mode checked : " + mStrictMode.isChecked());
    }

    private void writePointerLocationOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.POINTER_LOCATION, mPointerLocation.isChecked() ? 1 : 0);
        Log.i(TAG, "Show touch data checked : " + mPointerLocation.isChecked());
    }

    private void updatePointerLocationOptions() {
        updateCheckBox(mPointerLocation, Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POINTER_LOCATION, 0) != 0);
        Log.i(TAG, "Show touch data checked : " + mPointerLocation.isChecked());
    }

    private void writeShowTouchesOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.SHOW_TOUCHES, mShowTouches.isChecked() ? 1 : 0);
        Log.i(TAG, "Show touches checked : " + mShowTouches.isChecked());
    }

    private void updateShowTouchesOptions() {
        updateCheckBox(mShowTouches, Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_TOUCHES, 0) != 0);
        Log.i(TAG, "Show touches checked : " + mShowTouches.isChecked());
    }

    private void updateFlingerOptions() {
        // magic communication with surface flinger.
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(1010, data, reply, 0);
                @SuppressWarnings("unused")
                int showCpu = reply.readInt();
                @SuppressWarnings("unused")
                int enableGL = reply.readInt();
                int showUpdates = reply.readInt();
                updateCheckBox(mShowScreenUpdates, showUpdates != 0);
                @SuppressWarnings("unused")
                int showBackground = reply.readInt();
                int disableOverlays = reply.readInt();
                updateCheckBox(mDisableOverlays, disableOverlays != 0);
                reply.recycle();
                data.recycle();
                Log.i(TAG, "Show screen update checked : " + mShowScreenUpdates.isChecked());
                Log.i(TAG, "Disable HW overlays checked : " + mDisableOverlays.isChecked());
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeShowUpdatesOption() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                final int showUpdates = mShowScreenUpdates.isChecked() ? 1 : 0;
                data.writeInt(showUpdates);
                flinger.transact(1002, data, null, 0);
                data.recycle();
                Log.i(TAG, "Show screen update checked : " + mShowScreenUpdates.isChecked());
                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeDisableOverlaysOption() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                final int disableOverlays = mDisableOverlays.isChecked() ? 1 : 0;
                data.writeInt(disableOverlays);
                flinger.transact(1008, data, null, 0);
                data.recycle();
                Log.i(TAG, "Disable HW overlays checked : " + mDisableOverlays.isChecked());
                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateHardwareUiOptions() {
        updateCheckBox(mForceHardwareUi, SystemProperties.getBoolean(HARDWARE_UI_PROPERTY, false));
        Log.i(TAG, "Force GPU rendering checked : " + mForceHardwareUi.isChecked());
    }

    private void writeHardwareUiOptions() {
        SystemProperties.set(HARDWARE_UI_PROPERTY, mForceHardwareUi.isChecked() ? "true" : "false");
        pokeSystemProperties();
        Log.i(TAG, "Force GPU rendering checked : " + mForceHardwareUi.isChecked());

    }

    private void updateMsaaOptions() {
        updateCheckBox(mForceMsaa, SystemProperties.getBoolean(MSAA_PROPERTY, false));
    }

    private void writeMsaaOptions() {
        SystemProperties.set(MSAA_PROPERTY, mForceMsaa.isChecked() ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateTrackFrameTimeOptions() {
        String value = SystemProperties.get(HardwareRenderer.PROFILE_PROPERTY);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mTrackFrameTime.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mTrackFrameTime.setValueIndex(i);
                mTrackFrameTime.setSummary(mTrackFrameTime.getEntries()[i]);
                //            Log.i(TAG,"Profile GPU rendering checked (update) : " + i);
                return;
            }
        }
        mTrackFrameTime.setValueIndex(0);
        mTrackFrameTime.setSummary(mTrackFrameTime.getEntries()[0]);
    }

    private void writeTrackFrameTimeOptions(Object newValue) {
        SystemProperties.set(HardwareRenderer.PROFILE_PROPERTY,
                newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        //      Log.i(TAG, "Profile GPU rendering checked (write) : " + newValue == null);
        updateTrackFrameTimeOptions();
    }

    private void updateShowNonRectClipOptions() {
        String value = SystemProperties.get(
                HardwareRenderer.DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY);
        if (value == null) {
            value = "hide";
        }

        CharSequence[] values = mShowNonRectClip.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mShowNonRectClip.setValueIndex(i);
                mShowNonRectClip.setSummary(mShowNonRectClip.getEntries()[i]);
                //        Log.i(TAG, "Debug non-rectangular clip operations (update) : " + i);
                return;
            }
        }
        mShowNonRectClip.setValueIndex(0);
        mShowNonRectClip.setSummary(mShowNonRectClip.getEntries()[0]);
    }

    private void writeShowNonRectClipOptions(Object newValue) {
        SystemProperties.set(HardwareRenderer.DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY,
                newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        //       Log.i(TAG, "Debug non-rectangular clip operations (write) : " + newValue == null);
        updateShowNonRectClipOptions();
    }

    private void updateShowHwScreenUpdatesOptions() {
        updateCheckBox(mShowHwScreenUpdates,
                SystemProperties.getBoolean(HardwareRenderer.DEBUG_DIRTY_REGIONS_PROPERTY, false));
        //       Log.i(TAG,"Show GPU view updates checked : " + mShowHwScreenUpdates.isChecked());
    }

    private void writeShowHwScreenUpdatesOptions() {
        SystemProperties.set(HardwareRenderer.DEBUG_DIRTY_REGIONS_PROPERTY,
                mShowHwScreenUpdates.isChecked() ? "true" : null);
        //       Log.i(TAG,"Show GPU view updates checked : " + mShowHwScreenUpdates.isChecked());

        pokeSystemProperties();
    }

    private void updateShowHwLayersUpdatesOptions() {
        updateCheckBox(mShowHwLayersUpdates, SystemProperties.getBoolean(
                HardwareRenderer.DEBUG_SHOW_LAYERS_UPDATES_PROPERTY, false));
    }

    private void writeShowHwLayersUpdatesOptions() {
        SystemProperties.set(HardwareRenderer.DEBUG_SHOW_LAYERS_UPDATES_PROPERTY,
                mShowHwLayersUpdates.isChecked() ? "true" : null);
        pokeSystemProperties();
    }

    private void updateDebugHwOverdrawOptions() {
        String value = SystemProperties.get(HardwareRenderer.DEBUG_OVERDRAW_PROPERTY);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mDebugHwOverdraw.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mDebugHwOverdraw.setValueIndex(i);
                mDebugHwOverdraw.setSummary(mDebugHwOverdraw.getEntries()[i]);
                return;
            }
        }
        mDebugHwOverdraw.setValueIndex(0);
        mDebugHwOverdraw.setSummary(mDebugHwOverdraw.getEntries()[0]);
    }

    private void writeDebugHwOverdrawOptions(Object newValue) {
        SystemProperties.set(HardwareRenderer.DEBUG_OVERDRAW_PROPERTY,
                newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateDebugHwOverdrawOptions();
    }

    private void updateDebugLayoutOptions() {
        updateCheckBox(mDebugLayout,
                SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, false));
        Log.i(TAG, "Show layout bounds checked : " + mDebugLayout.isChecked());
    }

    private void writeDebugLayoutOptions() {
        SystemProperties.set(View.DEBUG_LAYOUT_PROPERTY,
                mDebugLayout.isChecked() ? "true" : "false");
        Log.i(TAG, "Show layout bounds checked : " + mDebugLayout.isChecked());
        pokeSystemProperties();
    }

    private void updateSimulateColorSpace() {
        final ContentResolver cr = getContentResolver();
        final boolean enabled = Settings.Secure.getInt(
                cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0) != 0;
        if (enabled) {
            final String mode = Integer.toString(Settings.Secure.getInt(
                    cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER,
                    AccessibilityManager.DALTONIZER_DISABLED));
            mSimulateColorSpace.setValue(mode);
            final int index = mSimulateColorSpace.findIndexOfValue(mode);
            if (index < 0) {
                // We're using a mode controlled by accessibility preferences.
                mSimulateColorSpace.setSummary(getString(R.string.daltonizer_type_overridden,
                        getString(R.string.accessibility_display_daltonizer_preference_title)));
            } else {
                mSimulateColorSpace.setSummary("%s");
            }
        } else {
            mSimulateColorSpace.setValue(
                    Integer.toString(AccessibilityManager.DALTONIZER_DISABLED));
        }
    }

    /**
     * @return <code>true</code> if the color space preference is currently
     *         controlled by development settings
     */
    private boolean usingDevelopmentColorSpace() {
        final ContentResolver cr = getContentResolver();
        final boolean enabled = Settings.Secure.getInt(
                cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0) != 0;
        if (enabled) {
            final String mode = Integer.toString(Settings.Secure.getInt(
                    cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER,
                    AccessibilityManager.DALTONIZER_DISABLED));
            final int index = mSimulateColorSpace.findIndexOfValue(mode);
            if (index >= 0) {
                // We're using a mode controlled by developer preferences.
                return true;
            }
        }
        return false;
    }

    private void writeSimulateColorSpace(Object value) {
        final ContentResolver cr = getContentResolver();
        final int newMode = Integer.parseInt(value.toString());
        if (newMode < 0) {
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0);
        } else {
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 1);
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER, newMode);
        }
    }

    private void updateUseNuplayerOptions() {
        updateCheckBox(
                mUseNuplayer, !SystemProperties.getBoolean(USE_AWESOMEPLAYER_PROPERTY, false));
    }

    private void writeUseNuplayerOptions() {
        SystemProperties.set(
                USE_AWESOMEPLAYER_PROPERTY, mUseNuplayer.isChecked() ? "false" : "true");
        pokeSystemProperties();
    }

    private void updateUSBAudioOptions() {
        updateCheckBox(mUSBAudio, Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.USB_AUDIO_AUTOMATIC_ROUTING_DISABLED, 0) != 0);
    }

    private void writeUSBAudioOptions() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.USB_AUDIO_AUTOMATIC_ROUTING_DISABLED,
                    mUSBAudio.isChecked() ? 1 : 0);
    }

    private void updateForceRtlOptions() {
        updateCheckBox(mForceRtlLayout, Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.DEVELOPMENT_FORCE_RTL, 0) != 0);
    }

    private void writeForceRtlOptions() {
        boolean value = mForceRtlLayout.isChecked();
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.DEVELOPMENT_FORCE_RTL, value ? 1 : 0);
        SystemProperties.set(Settings.Global.DEVELOPMENT_FORCE_RTL, value ? "1" : "0");
        LocalePicker.updateLocale(getActivity().getResources().getConfiguration().locale);
    }

    private void updateWifiDisplayCertificationOptions() {
        updateCheckBox(mWifiDisplayCertification, Settings.Global.getInt(
                getActivity().getContentResolver(),
                Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON, 0) != 0);
    }

    private void writeWifiDisplayCertificationOptions() {
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON,
                mWifiDisplayCertification.isChecked() ? 1 : 0);
    }

    private void updateWifiVerboseLoggingOptions() {
        boolean enabled = mWifiManager.getVerboseLoggingLevel() > 0;
        updateCheckBox(mWifiVerboseLogging, enabled);
    }

    private void writeWifiVerboseLoggingOptions() {
        mWifiManager.enableVerboseLogging(mWifiVerboseLogging.isChecked() ? 1 : 0);
    }

    private void updateWifiAggressiveHandoverOptions() {
        boolean enabled = mWifiManager.getAggressiveHandover() > 0;
        updateCheckBox(mWifiAggressiveHandover, enabled);
    }

    private void writeWifiAggressiveHandoverOptions() {
        mWifiManager.enableAggressiveHandover(mWifiAggressiveHandover.isChecked() ? 1 : 0);
    }

    private void updateWifiAllowScansWithTrafficOptions() {
        boolean enabled = mWifiManager.getAllowScansWithTraffic() > 0;
        updateCheckBox(mWifiAllowScansWithTraffic, enabled);
    }

    private void writeWifiAllowScansWithTrafficOptions() {
        mWifiManager.setAllowScansWithTraffic(mWifiAllowScansWithTraffic.isChecked() ? 1 : 0);
    }

    private void updateLogdSizeValues() {
        if (mLogdSize != null) {
            String currentValue = SystemProperties.get(SELECT_LOGD_SIZE_PROPERTY);
            if (currentValue == null) {
                currentValue = SystemProperties.get(SELECT_LOGD_DEFAULT_SIZE_PROPERTY);
                if (currentValue == null) {
                    currentValue = "256K";
                }
            }
            String[] values = getResources().getStringArray(R.array.select_logd_size_values);
            String[] titles = getResources().getStringArray(R.array.select_logd_size_titles);
            String[] summaries = getResources().getStringArray(R.array.select_logd_size_summaries);
            int index = 1; // punt to second entry if not found
            for (int i = 0; i < values.length; i++) {
                if (currentValue.equals(values[i])
                        || currentValue.equals(titles[i])) {
                    index = i;
                    break;
                }
            }
            mLogdSize.setValue(values[index]);
            mLogdSize.setSummary(summaries[index]);
            mLogdSize.setOnPreferenceChangeListener(this);
        }
    }

    private void writeLogdSizeOption(Object newValue) {
        final String size = (newValue != null) ?
                newValue.toString() : sDEFAULT_LOG_RING_BUFFER_SIZE_IN_BYTES;
        SystemProperties.set(SELECT_LOGD_SIZE_PROPERTY, size);
        pokeSystemProperties();
        try {
            Process p = Runtime.getRuntime().exec("logcat -b all -G " + size);
            p.waitFor();
            Log.i(TAG, "Logcat ring buffer sizes set to: " + size);
        } catch (Exception e) {
            Log.w(TAG, "Cannot set logcat ring buffer sizes", e);
        }
        updateLogdSizeValues();
    }

    private void updateCpuUsageOptions() {
        updateCheckBox(mShowCpuUsage, Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_PROCESSES, 0) != 0);
        Log.i(TAG, "Show CPU usage checked : " + mShowCpuUsage.isChecked());
    }

    private void writeCpuUsageOptions() {
        boolean value = mShowCpuUsage.isChecked();
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_PROCESSES, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.LoadAverageService");
        if (value) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
        Log.i(TAG, "Show CPU usage checked : " + mShowCpuUsage.isChecked());
    }

    private void writeImmediatelyDestroyActivitiesOptions() {
        try {
            ActivityManagerNative.getDefault().setAlwaysFinish(
                    mImmediatelyDestroyActivities.isChecked());
            Log.i(TAG, "Don't keep activity checked : " + mImmediatelyDestroyActivities.isChecked());
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateImmediatelyDestroyActivitiesOptions() {
        updateCheckBox(mImmediatelyDestroyActivities,
                Settings.Global.getInt(
                        getActivity().getContentResolver(),
                        Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0) != 0);
        Log.i(TAG, "Don't keep activity checked : " + mImmediatelyDestroyActivities.isChecked());
    }

    private void updateAnimationScaleValue(int which, ListPreference pref) {
        try {
            float scale = mWindowManager.getAnimationScale(which);
            if (Float.compare(scale, 1f) != 0) { //[WBT] CMPF.FLOAT (#508916) //if (scale != 1) {
                mHaveDebugSettings = true;
            }
            CharSequence[] values = pref.getEntryValues();
            for (int i = 0; i < values.length; i++) {
                float val = Float.parseFloat(values[i].toString());
                if (scale <= val) {
                    pref.setValueIndex(i);
                    pref.setSummary(pref.getEntries()[i]);
                    return;
                }
            }
            pref.setValueIndex(values.length - 1);
            pref.setSummary(pref.getEntries()[0]);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateAnimationScaleOptions() {
        updateAnimationScaleValue(0, mWindowAnimationScale);
        updateAnimationScaleValue(1, mTransitionAnimationScale);
        updateAnimationScaleValue(2, mAnimatorDurationScale);
    }

    private void writeAnimationScaleOption(int which, ListPreference pref, Object newValue) {
        try {
            //float scale = newValue != null ? Float.parseFloat(newValue.toString()) : 1;
            float scale;
            if (Utils.isAnimationOff()) {
                scale = newValue != null ? Float.parseFloat(newValue.toString()) : 0;
            } else {
                scale = newValue != null ? Float.parseFloat(newValue.toString()) : 1;
            }
            mWindowManager.setAnimationScale(which, scale);
            updateAnimationScaleValue(which, pref);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateOverlayDisplayDevicesOptions() {
        String value = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.OVERLAY_DISPLAY_DEVICES);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mOverlayDisplayDevices.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mOverlayDisplayDevices.setValueIndex(i);
                mOverlayDisplayDevices.setSummary(mOverlayDisplayDevices.getEntries()[i]);
                return;
            }
        }
        mOverlayDisplayDevices.setValueIndex(0);
        mOverlayDisplayDevices.setSummary(mOverlayDisplayDevices.getEntries()[0]);
    }

    private void writeOverlayDisplayDevicesOptions(Object newValue) {
        Settings.Global.putString(getActivity().getContentResolver(),
                Settings.Global.OVERLAY_DISPLAY_DEVICES, (String)newValue);
        updateOverlayDisplayDevicesOptions();
    }

    private void updateOpenGLTracesOptions() {
        String value = SystemProperties.get(OPENGL_TRACES_PROPERTY);
        if (value == null) {
            value = "";
        }

        CharSequence[] values = mOpenGLTraces.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                mOpenGLTraces.setValueIndex(i);
                mOpenGLTraces.setSummary(mOpenGLTraces.getEntries()[i]);
                return;
            }
        }
        mOpenGLTraces.setValueIndex(0);
        mOpenGLTraces.setSummary(mOpenGLTraces.getEntries()[0]);
    }

    private void writeOpenGLTracesOptions(Object newValue) {
        SystemProperties.set(OPENGL_TRACES_PROPERTY, newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateOpenGLTracesOptions();
    }

    private void updateAppProcessLimitOptions() {
        try {
            int limit = ActivityManagerNative.getDefault().getProcessLimit();
            CharSequence[] values = mAppProcessLimit.getEntryValues();
            for (int i = 0; i < values.length; i++) {
                int val = Integer.parseInt(values[i].toString());
                if (val >= limit) {
                    if (i != 0) {
                        mHaveDebugSettings = true;
                    }
                    mAppProcessLimit.setValueIndex(i);
                    mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[i]);
                    return;
                }
            }
            mAppProcessLimit.setValueIndex(0);
            mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[0]);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeAppProcessLimitOptions(Object newValue) {
        try {
            int limit = newValue != null ? Integer.parseInt(newValue.toString()) : -1;
            ActivityManagerNative.getDefault().setProcessLimit(limit);
            updateAppProcessLimitOptions();
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeShowAllANRsOptions() {
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.ANR_SHOW_BACKGROUND,
                mShowAllANRs.isChecked() ? 1 : 0);
        Log.i(TAG, "Show all ANRs : " + mShowAllANRs.isChecked());
    }

    private void updateShowAllANRsOptions() {
        updateCheckBox(mShowAllANRs, Settings.Secure.getInt(
                getActivity().getContentResolver(), Settings.Secure.ANR_SHOW_BACKGROUND, 0) != 0);
        Log.i(TAG, "Show all ANRs : " + mShowAllANRs.isChecked());
    }

    private void writeWebViewDataReductionProxyOptions() {
        // FIXME : Settings.Secure.WEBVIEW_DATA_REDUCTION_PROXY was removed.
        Settings.Secure.putInt(getActivity().getContentResolver(),
                "webview_data_reduction_proxy",
                mWebViewDataReductionProxy.isChecked() ? 1 : 0);
        Intent intent = new Intent(WebView.DATA_REDUCTION_PROXY_SETTING_CHANGED);
        // Broadcast to all apps running as current user.
        getActivity().sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void updateWebViewDataReductionProxyOptions() {
        // FIXME : Settings.Secure.WEBVIEW_DATA_REDUCTION_PROXY was removed.
        updateCheckBox(mWebViewDataReductionProxy, Settings.Secure.getInt(
                getActivity().getContentResolver(),
                "webview_data_reduction_proxy", 0) != 0);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mEnabledSwitch) {
            if (isChecked != mLastEnabledState) {
                if (isChecked) {
                    mDialogClicked = false;
                    if (mEnableDialog != null) {
                        dismissDialogs();
                    }
                    mEnableDialog = new AlertDialog.Builder(getActivity()).setMessage(
                            getActivity().getResources().getString(
                                    R.string.dev_settings_warning_message))
                            .setTitle(R.string.dev_settings_warning_title)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.yes, this)
                            .setNegativeButton(android.R.string.no, this)
                            .show();
                    mEnableDialog.setOnDismissListener(this);
                } else {
                    resetDangerousOptions();
                    Settings.Global.putInt(getActivity().getContentResolver(),
                            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
                    mLastEnabledState = isChecked;
                    setPrefsEnabledState(mLastEnabledState);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_DEBUG_APP) {
            if (resultCode == Activity.RESULT_OK) {
                mDebugApp = data.getAction();
                writeDebuggerOptions();
                updateDebuggerOptions();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mDialogClicked = false;
                if (mAdbDialog != null) {
                    dismissDialogs();
                }
                mAdbDialog = new AlertDialog.Builder(getActivity()).setMessage(
                        getActivity().getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mAdbDialog.setOnDismissListener(this);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0);
                // [S][2012.03.09][changyu0218.lee@lge.com][Common]Fix adb check state keep by lock & unlock
                dismissDialogs();
                Log.i(TAG, "USB debugging checked : " + mEnableAdb.isChecked());
                // [E][2012.03.09][changyu0218.lee@lge.com][Common]Fix adb check state keep by lock & unlock

                mVerifyAppsOverUsb.setEnabled(false);
                mVerifyAppsOverUsb.setChecked(false);
                updateBugreportOptions();
            }
        } else if (preference == mClearAdbKeys) {
            if (mAdbKeysDialog != null) {
                dismissDialogs();
            }
            mAdbKeysDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.adb_keys_warning_message)
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (preference == mEnableTerminal) {
            final PackageManager pm = getActivity().getPackageManager();
            pm.setApplicationEnabledSetting(TERMINAL_APP_PACKAGE,
                    mEnableTerminal.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
        } else if (preference == mBugreportInPower) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.BUGREPORT_IN_POWER_MENU,
                    mBugreportInPower.isChecked() ? 1 : 0);
        } else if (preference == mKeepScreenOn) {
            Settings.Global
                    .putInt(getActivity().getContentResolver(),
                            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                            mKeepScreenOn.isChecked() ?
                                    (BatteryManager.BATTERY_PLUGGED_AC
                                            | BatteryManager.BATTERY_PLUGGED_USB | BATTERY_PLUGGED_WIRELESS)
                                    : 0);
            Log.i(TAG, "Stay awake checked : " + mKeepScreenOn.isChecked());
        } else if (preference == mBtHciSnoopLog) {
            writeBtHciSnoopLogOptions();
        } else if (preference == mEnableOemUnlock) {
            Utils.setOemUnlockEnabled(getActivity(), mEnableOemUnlock.isChecked());
        } else if (preference == mAllowMockLocation) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION,
                    mAllowMockLocation.isChecked() ? 1 : 0);
            Log.i(TAG, "Allow mock locations checked : " + mAllowMockLocation.isChecked());
        } else if (preference == mQuickBoot) {
            Settings.System.putInt(getActivity().getContentResolver(), ENABLE_QUICKBOOT,
                    mQuickBoot.isChecked() ? 1 : 0);
        } else if (preference == mDebugAppPref) {
            startActivityForResult(new Intent(getActivity(), AppPicker.class), RESULT_DEBUG_APP);
        } else if (preference == mWaitForDebugger) {
            writeDebuggerOptions();
        } else if (preference == mVerifyAppsOverUsb) {
            writeVerifyAppsOverUsbOptions();
        } else if (preference == mStrictMode) {
            writeStrictModeVisualOptions();
        } else if (preference == mPointerLocation) {
            writePointerLocationOptions();
        } else if (preference == mShowTouches) {
            writeShowTouchesOptions();
        } else if (preference == mShowScreenUpdates) {
            writeShowUpdatesOption();
        } else if (preference == mDisableOverlays) {
            writeDisableOverlaysOption();
        } else if (preference == mShowCpuUsage) {
            writeCpuUsageOptions();
        } else if (preference == mImmediatelyDestroyActivities) {
            writeImmediatelyDestroyActivitiesOptions();
        } else if (preference == mShowAllANRs) {
            writeShowAllANRsOptions();
        } else if (preference == mWebViewDataReductionProxy) {
            writeWebViewDataReductionProxyOptions();
        } else if (preference == mForceHardwareUi) {
            writeHardwareUiOptions();
        } else if (preference == mForceMsaa) {
            writeMsaaOptions();
        } else if (preference == mShowHwScreenUpdates) {
            writeShowHwScreenUpdatesOptions();
        } else if (preference == mShowHwLayersUpdates) {
            writeShowHwLayersUpdatesOptions();
        } else if (preference == mDebugLayout) {
            writeDebugLayoutOptions();
        } else if (preference == mForceRtlLayout) {
            writeForceRtlOptions();
        } else if (preference == mWifiDisplayCertification) {
            writeWifiDisplayCertificationOptions();
        } else if (preference == mWifiVerboseLogging) {
            writeWifiVerboseLoggingOptions();
        } else if (preference == mWifiAggressiveHandover) {
            writeWifiAggressiveHandoverOptions();
        } else if (preference == mWifiAllowScansWithTraffic) {
            writeWifiAllowScansWithTrafficOptions();
        } else if (preference == mUseNuplayer) {
            writeUseNuplayerOptions();
        } else if (preference == mUSBAudio) {
            writeUSBAudioOptions();
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (HDCP_CHECKING_KEY.equals(preference.getKey())) {
            SystemProperties.set(HDCP_CHECKING_PROPERTY, newValue.toString());
            updateHdcpValues();
            pokeSystemProperties();
            return true;
        } else if (preference == mLogdSize) {
            writeLogdSizeOption(newValue);
            return true;
        } else if (preference == mWindowAnimationScale) {
            writeAnimationScaleOption(0, mWindowAnimationScale, newValue);
            return true;
        } else if (preference == mTransitionAnimationScale) {
            writeAnimationScaleOption(1, mTransitionAnimationScale, newValue);
            return true;
        } else if (preference == mAnimatorDurationScale) {
            writeAnimationScaleOption(2, mAnimatorDurationScale, newValue);
            return true;
        } else if (preference == mOverlayDisplayDevices) {
            writeOverlayDisplayDevicesOptions(newValue);
            return true;
        } else if (preference == mOpenGLTraces) {
            writeOpenGLTracesOptions(newValue);
            return true;
        } else if (preference == mTrackFrameTime) {
            writeTrackFrameTimeOptions(newValue);
            return true;
        } else if (preference == mDebugHwOverdraw) {
            writeDebugHwOverdrawOptions(newValue);
            return true;
        } else if (preference == mShowNonRectClip) {
            writeShowNonRectClipOptions(newValue);
            return true;
        } else if (preference == mAppProcessLimit) {
            writeAppProcessLimitOptions(newValue);
            return true;
        } else if (preference == mSimulateColorSpace) {
            writeSimulateColorSpace(newValue);
            return true;
        }
        return false;
    }

    private void dismissDialogs() {
        if (mAdbDialog != null) {
            mAdbDialog.dismiss();
            mAdbDialog = null;
        }
        if (mAdbKeysDialog != null) {
            mAdbKeysDialog.dismiss();
            mAdbKeysDialog = null;
        }
        if (mAttentionDialog != null) {
            mAttentionDialog.dismiss();
            mAttentionDialog = null;
        }
        /*
        if (mEnableDialog != null) {
            mEnableDialog.dismiss();
            mEnableDialog = null;
        }
        */
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mAdbDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-258][ID-MDM-74]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                        && MDMSettingsAdapter.getInstance().checkDisabledUsbType(
                                com.lge.cappuccino.IMdm.USB_ADB)) {
                    return;
                }
                // LGMDM_END
                mDialogClicked = true;

                // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                    if (Settings.Secure.getInt(getActivity().getContentResolver(),
                            "adb_blocked", 0) == 0) {
                        Settings.Secure.putInt(getActivity().getContentResolver(),
                                Settings.Secure.ADB_ENABLED, 1);
                        mVerifyAppsOverUsb.setEnabled(true);
                        updateVerifyAppsOverUsbOptions();
                        updateBugreportOptions();
                    } else {
                        // 3LM does not allow ADB, uncheck the box
                        mEnableAdb.setChecked(false);
                    }
                } else {
                    // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
                    Settings.Global.putInt(getActivity().getContentResolver(),
                            Settings.Global.ADB_ENABLED, 1);
                    mVerifyAppsOverUsb.setEnabled(true);
                    updateVerifyAppsOverUsbOptions();
                    updateBugreportOptions();
                }

                //[S][2013.04.19][jamy.yeom@lge.com]Check USB debugging option
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
                mLastEnabledState = true;
                mEnabledSwitch.setChecked(mLastEnabledState);
                setPrefsEnabledState(mLastEnabledState);
                //[E][2013.04.19][jamy.yeom@lge.com]Check USB debugging option

            } else if (which != DialogInterface.BUTTON_NEUTRAL) {
                // Reset the toggle
                mEnableAdb.setChecked(false);
                dismissDialogs();
            }
        } else if (dialog == mAdbKeysDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                try {
                    IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                    IUsbManager service = IUsbManager.Stub.asInterface(b);
                    service.clearUsbDebuggingKeys();
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to clear adb keys", e);
                }
            }
        } else if (dialog == mEnableDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
                mLastEnabledState = true;
                setPrefsEnabledState(mLastEnabledState);
            } else {
                // Reset the toggle
                mEnabledSwitch.setChecked(false);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (dialog == mAdbDialog) {
            if (!mDialogClicked) {
                mEnableAdb.setChecked(false);
            }
            mAdbDialog = null;
        } else if (dialog == mEnableDialog) {
            if (!mDialogClicked) {
                mEnabledSwitch.setChecked(false);
            }
            mEnableDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-74][ID-MDM-258][ID-MDM-207][ID-MDM-413]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    void pokeSystemProperties() {
        if (!mDontPokeProperties) {
            //noinspection unchecked
            (new SystemPropPoker()).execute();
        }
    }

    static class SystemPropPoker extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String[] services;
            try {
                services = ServiceManager.listServices();
            } catch (RemoteException e) {
                return null;
            }
            for (String service : services) {
                IBinder obj = ServiceManager.checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(IBinder.SYSPROPS_TRANSACTION, data, null, 0);
                    } catch (RemoteException e) {
                        Log.w(TAG, "RemoteException");
                    } catch (Exception e) {
                        Log.i(TAG, "Someone wrote a bad service '" + service
                                + "' that doesn't like to be poked: " + e);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0) != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    //[S][2012.02.07][changyu0218.lee@lge.com][U0][5527] Add Connectivity action
    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE) && mLastEnabledState
                    && mEnabledSwitch.isChecked()) {
                if (intent.getBooleanExtra(UsbManager.USB_CONNECTED, false)) {
                    //[S][2012.04.19][changyu0218.lee@lge.com][NA] Add VZW exception case
                    usbConnected = true;
                    Context mContext = (Context)getActivity();
                    if (mUsbManager == null) {
                        mUsbManager = (UsbManager)mContext.getSystemService(mContext.USB_SERVICE);
                    }
                    if (Config.VZW.equals(Config.getOperator())) {
                        if (!(mUsbManager.getDefaultFunction()
                                .equals(UsbManagerConstants.USB_FUNCTION_TETHER))
                                && !(mUsbManager.getDefaultFunction()
                                        .equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)))
                        {
                            mEnableAdb.setEnabled(false);
                        }
                    } else {
                        if (!(Config.BM.equals(Config.getOperator()))
                                && !(Config.SPR.equals(Config.getOperator()))) { //[2013.05.29][jamy.yeom@lge.com][Operator request] Add SPR Operator
                            mEnableAdb.setEnabled(false);
                        }
                    }
                } else {
                    usbConnected = false;
                    mEnableAdb.setEnabled(true);
                    // [S][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
                    if (Config.SPR.equals(Config.getOperator())
                            || Config.BM.equals(Config.getOperator()))
                    {
                        if (false == OverlayUtils.getAllowUsbPort(null))
                        {
                            mEnableAdb.setEnabled(false);
                        }
                    }
                    // [E][2013.08.11][ry.jeong@lge.com][SPR,BM] When USB_PORT restricted, Request USB debugging menu disable for SPRINT, BM operator.
                }
                //[E][2012.04.19][changyu0218.lee@lge.com][NA] Add VZW exception case
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-258][ID-MDM-74]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMSettingsAdapter.getInstance().setAdbEnableMenu(mEnableAdb);
                }
                // LGMDM_END

                // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                    //if MDM locks ADB, disable mEnableAdb
                    boolean isBlockedByMdm = !(Settings.Secure.getInt(getActivity()
                            .getContentResolver(), "adb_blocked", 0) == 0);
                    if (isBlockedByMdm) {
                        if (mEnableAdb != null) {
                            mEnableAdb.setEnabled(false);
                        }
                    }
                }
                // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        // [S][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
        // [E][2013.09.02][ry.jeong@lge.com] Add switch for tablet.
        if (!mUnavailable) {
            getActivity().unregisterReceiver(mStateReceiver);
        }
    }

    //[S][2012.8.31][changyu0218.lee@lge.com][Common] Add options menu Reset to default
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        menu.add(0, 1, 0, getResources().getString(R.string.develop_reset_default));
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case 1:
            resetDangerousOptions();
            return true;
        default:
            break;
        }
        return false;
    }

    //[E][2012.8.31][changyu0218.lee@lge.com][Common] Add options menu Reset to default

    //[S][2012.05.17][changyu0218.lee@lge.com][Common]Add popup entering development options VZW models
    private void showEnteringDialog()
    {
        int currentValue = Settings.System.getInt(getActivity().getContentResolver(),
                "DEVELOPER_DO_NOT_SHOW", 0);
        if (currentValue == 1) {
            return;
        }
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View inpuView = factory.inflate(R.layout.developer_do_not_show, null);
        visible = (CheckBox)inpuView.findViewById(R.id.developer_check);
        visible.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                //visible.playSoundEffect(AudioManager.FX_KEY_CLICK);
            }
        });

        mAttentionDialog = new AlertDialog.Builder(getActivity())
                //.setMessage(R.string.sp_develop_enter_popup_NORMAL)
                .setTitle(R.string.sp_warning_NORMAL)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setView(inpuView)
                .setNeutralButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (visible.isChecked()) {
                            Settings.System.putInt(getActivity().getContentResolver(),
                                    "DEVELOPER_DO_NOT_SHOW", 1);
                        }
                    }
                })
                .show();
        //[S][2013.06.12][jamy.yeom@lge.com][VZW]Set "Don't show this again" check box default value to "checked" on VZW Operator.
        if (Config.VZW.equals(Config.getOperator()))
        {
            visible.setChecked(true);
        }
        //[E][2013.06.12][jamy.yeom@lge.com][VZW]Set "Don't show this again" check box default value to "checked" on VZW Operator.

        mAttentionDialog.setCanceledOnTouchOutside(false);
    }

    //[S][2012.05.17][changyu0218.lee@lge.com][Common]Add popup entering development options VZW models

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    // [ID-MDM-74][ID-MDM-258][ID-MDM-207][ID-MDM-413]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveDevelopmentPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };

    // LGMDM_END

    //[S][2013.03.19][jamy.yeom]Add Switch sound
    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }
    //[E][2013.03.19][jamy.yeom]Add Switch sound

}
