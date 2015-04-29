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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.UsbSettings;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import com.lge.constants.UsbManagerConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ConnectivitySettings extends SettingsPreferenceFragment {

    private static final String TAG = "ConnectivitySettings";
    private static final String KEY_USB_CONNECTION_CATEGORY = "usb_connection";
    private static final String KEY_USB_CONNECTION_TYPE = "usb_connection_type";
    private static final String KEY_USB_ALWAYS_ASK = "usb_always_ask";
    private static final String KEY_USB_HELP = "connectivity_helper";

    private static final String KEY_LG_CATEGORY = "lg_category";
    private static final String KEY_LG_CATEGORY_SUMMARY = "lg_category_summary";
    //private static final String KEY_OSP_SETTINGS = "osp_settings";
    private static final String KEY_WIFI_PCSUITE_CONNECTION_ONOFF = "wifi_pcsuite_connection_onoff";
    // [PCSync][BEGIN][ilhwan.choi/20120424]
    private static final String KEY_WIFI_PCSUITE_CONNECTION_STATUS = "pcsuite_wifi_connection_status";
    // [PCSync][END][ilhwan.choi/20120424]
    private static final String KEY_WIFI_OSP_CONNECTION_ONOFF = "wifi_osp_connection_onoff";
    private static final String KEY_LG_SOFTWARE_HELP = "lg_software_help";

    private static final String OSP_PACKAGE_NAME = "com.lge.osp";
    private static final String OSP_SERVICE_NAME = OSP_PACKAGE_NAME + ".OSPService";
    //private static final String ACTION_OSP_ALERT_CONNECTION = OSP_PACKAGE_NAME
    //        + ".ALERT_CONNECTION";
    private static final String ACTION_OSP_DISCONNECT = OSP_PACKAGE_NAME + ".NOTI_DISCONNECT";
    private static final String ACTION_LINKCLOUD_WIFI_CONNECTION = "com.android.settings.WIFIConnectionStatus";
    private static final String ACTION_OSP_WIFI_CONNECTION = "com.lge.osp.OSPWIFIConnectionStatus";

    private static boolean sSupportPCSuite = true;
    private static boolean sSupportOSP3 = false;
    private static boolean sNotSupportOSP = false;
    //ANDY_END

    // Sprint requirement - Tethering
    private static final String KEY_USB_TETHER = "usb_tether";
    private CheckBoxPreference mUsbTether;
    private boolean mUsbTethered = false;
    // VZW requirement - USB tethering
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private Preference mTetherSettings;

    // LGE_CHANGE_S : Auto-launch for VZW
    private static final String KEY_AUTO_LAUNCH = "auto_launch";
    private static final String KEY_CAR_HOME = "car_home";
    private static final String KEY_DESK_HOME = "desk_home";
    private static final String KEY_MEDIA_MODE = "media_home";
    private static final String KEY_POUCH_MODE = "pouch_mode";
    private static final String KEY_POUCH_NOTIFICATION = "pouch_notification";

    private PreferenceCategory mAutoLaunch;
    private CheckBoxPreference mCarHome;
    private CheckBoxPreference mDeskHome;
    private CheckBoxPreference mMediaHome;
    private CheckBoxPreference mPouchMode;
    // LGE_CHANGE_E : Auto-launch for VZW

    private UsbManager mUsbManager;
    private PreferenceCategory mUsbConnectionCategory;
    private Preference mUsbConnectionType;
    private Preference mUsbHelp;
    private CheckBoxPreference mUsbAlwaysAsk;
    private boolean mUsbConnection;

    private PreferenceCategory mLGCategory;
    private Preference mLGCategorySummary;
    private CheckBoxPreference mPCSuiteWIFIConnectionOnOff;
    private CheckBoxPreference mOSPWIFIConnectionOnOff;
    private WIFIConnectionEnabler mPCSuiteWIFIConnectionEnabler;
    private WIFIConnectionEnabler mOSPWIFIConnectionEnabler;
    private Preference mLGSoftwareHelp;
    private Object mIOSPService = null;
    private static Method mOSPAsInterfaceMethod = null;
    private static Method mOSPIsConnectedMethod = null;
    private static Method mOSPgetTransportMethod = null;

    static {
        if (com.lge.config.ConfigBuildFlags.CAPP_OSP) {
            try {
                Class ospInterface = Class.forName("com.lge.osp.IOSPService");
                Class stubClass = null;

                if (ospInterface != null) {
                    for (Class cls : ospInterface.getDeclaredClasses()) {
                        if (cls.getSimpleName().equals("Stub")) {
                            stubClass = cls;
                            break;
                        }
                    }

                    if (stubClass != null) {
                        Class binderClass = Class.forName("android.os.IBinder");
                        mOSPAsInterfaceMethod = stubClass.
                                getDeclaredMethod("asInterface", binderClass);
                    }

                    // get IOSPService.isConnected()
                    if (mOSPIsConnectedMethod == null) {
                        mOSPIsConnectedMethod = ospInterface
                                .getMethod("isConnected", (Class[])null);
                    }

                    // get IOSPService.getTransport()
                    if (mOSPgetTransportMethod == null) {
                        mOSPgetTransportMethod = ospInterface.getMethod("getTransport",
                                (Class[])null);
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                Log.w(TAG, "ClassNotFoundException");
            } catch (NoSuchMethodException nsme) {
                Log.w(TAG, "NoSuchMethodException");
            }
        }
    }
    //ANDY_END

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                boolean usbConfigured = intent.getBooleanExtra(UsbManager.USB_CONFIGURED, false);

                Log.d(TAG, "mStateReceiver() : usbConnected=" + usbConnected);
                Log.d(TAG, "mStateReceiver() : usbConfigured=" + usbConfigured);

                if ((Config.getCountry().equals("US") && Config.getOperator().equals("TMO"))
                        || Config.getOperator().equals("DCM")
                        || Config.getOperator().equals(Config.SPRINT)
                        || Config.getOperator().equals(Config.BM)) {
                    boolean usbTethered = UsbSettingsControl.getTetherStatus(getActivity());
                    Log.d(TAG, "mStateReceiver() : usbTethered=" + usbTethered);
                    if (usbTethered) {
                        mUsbConnectionType.setEnabled(false);
                    }
                    else {
                        mUsbConnectionType.setEnabled(true);
                    }
                }

                if (usbConnected) {
                    mUsbConnection = true;
                    if (usbConfigured) {
                        updateToggles(mUsbManager.getDefaultFunction());
                    }
                }
                else {
                    mUsbConnection = false;

                    mUsbConnectionType.setEnabled(true);

                    // Sprint requirement - Tethering
                    if (Config.getOperator().equals(Config.SPRINT)
                            || Config.getOperator().equals(Config.BM)) {
                        if (mUsbTethered) {
                            connectUsbTether(false);
                        }
                        UsbSettingsControl.mTetherChanged = false;
                    }
                    updateToggles(mUsbManager.getDefaultFunction());
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.connectivity_settings);

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        mUsbConnectionCategory = (PreferenceCategory)findPreference(KEY_USB_CONNECTION_CATEGORY);
        mUsbConnectionType = findPreference(KEY_USB_CONNECTION_TYPE);
        mUsbHelp = findPreference(KEY_USB_HELP);
        mUsbAlwaysAsk = (CheckBoxPreference)findPreference(KEY_USB_ALWAYS_ASK);
        mUsbAlwaysAsk.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.USB_ASK_ON_CONNECTION, 0) != 0);

        if ("VZW".equals(Config.getOperator())) {
            mUsbAlwaysAsk.setSummary(R.string.sp_askon_summary_NORMAL);
        }

        // VZW requirement - USB tethering
        mTetherSettings = (Preference)findPreference(KEY_TETHER_SETTINGS);
        mUsbConnectionCategory.removePreference(mTetherSettings);

        // Sprint requirement - Tethering
        mUsbTether = (CheckBoxPreference)findPreference(KEY_USB_TETHER);
        if ((Config.getOperator().equals(Config.SPRINT) || Config.getOperator().equals(Config.BM))
                && Utils.isUpgradeModel()) {

            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            String[] mUsbRegexs = { "" };
            mUsbRegexs = cm.getTetherableUsbRegexs();
            boolean usbAvailable = false;
            usbAvailable = mUsbRegexs.length != 0;
            mUsbConnection = usbAvailable;
            Log.d(TAG, "onCreate() : mUsbRegexs=" + mUsbRegexs);
            Log.d(TAG, "onCreate() : usbAvailable=" + usbAvailable);

            mUsbTethered = UsbSettingsControl.getTetherStatus(getActivity());
            if (mUsbTethered) {
                mUsbTether.setChecked(true);
                mUsbConnectionType.setEnabled(false);
            }
            boolean allowTethering = OverlayUtils.getAllowTethering(null);
            if (mUsbTether.isEnabled()) {
                mUsbTether.setEnabled(allowTethering);
            }
            if (!Utils.getChameleonUsbTetheringMenuEnabled()) {
                mUsbConnectionCategory.removePreference(mUsbTether);
                mUsbConnectionType.setEnabled(true);
            }
        }
        else {
            mUsbConnectionCategory.removePreference(mUsbTether);
        }

        mLGCategory = (PreferenceCategory)findPreference(KEY_LG_CATEGORY);
        mLGCategorySummary = findPreference(KEY_LG_CATEGORY_SUMMARY);
        mPCSuiteWIFIConnectionOnOff = (CheckBoxPreference)findPreference(KEY_WIFI_PCSUITE_CONNECTION_ONOFF);
        mOSPWIFIConnectionOnOff = (CheckBoxPreference)findPreference(KEY_WIFI_OSP_CONNECTION_ONOFF);
        mPCSuiteWIFIConnectionEnabler = new WIFIConnectionEnabler(getActivity());
        mOSPWIFIConnectionEnabler = new WIFIConnectionEnabler(getActivity());
        mLGSoftwareHelp = findPreference(KEY_LG_SOFTWARE_HELP);
        mPCSuiteWIFIConnectionOnOff.setChecked(Settings.System.getInt(
                getContentResolver(),
                SettingsConstants.System.LINKCLOUD_WIFI_CONNECTION, 0) != 0);
        mOSPWIFIConnectionOnOff.setChecked(Settings.System.getInt(
                getContentResolver(),
                SettingsConstants.System.OSP_WIFI_CONNECTION, 0) != 0);

        PackageManager pm = this.getPackageManager();

        try {
            PackageInfo pkgInfo = pm.getPackageInfo("com.lge.osp", 0);

            if (pkgInfo.versionCode >= 30000 && Config.getOperator().equals("ATT")) {
                sSupportOSP3 = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "not support OSP");
            sSupportOSP3 = false;
        }
        // [PCSync][Begin][ilhwan.choi/20120424]
        try {
            PackageInfo pkgInfo = pm.getPackageInfo("com.lge.sync", 0);
            sSupportPCSuite = true;
            Log.w(TAG, "pkgInfo = " + pkgInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "not support PCSuite");
            sSupportPCSuite = false;
        }

        //[youngju.do]PCSuiteSetting_v4.1 : move to Share&Connect 
        if (Utils.isUI_4_1_model(getActivity())) {
            sSupportPCSuite = false;
        }

        if (!sSupportPCSuite) {
            mLGCategory.removePreference(mPCSuiteWIFIConnectionOnOff);
        }
        // [PCSync][End][ilhwan.choi/20120424]

        if (!sSupportOSP3) {
            mLGCategory.removePreference(mOSPWIFIConnectionOnOff);
        }

        if (!sSupportOSP3 && !sSupportPCSuite) {
            mLGCategory.removePreference(mLGSoftwareHelp);
            mLGCategory.removePreference(mLGCategorySummary);
        }
        //AND_END

        if (mLGCategory.getPreferenceCount() <= 0) {
            getPreferenceScreen().removePreference(mLGCategory);
        }
        // LGE_CHANGE_S : Auto-launch for VZW
        initAutoLaunch();
        // LGE_CHANGE_E : Auto-launch for VZW
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (Config.SPRINT.equals(Config.getOperator())
                    || Config.BM.equals(Config.getOperator())) {
                IntentFilter filterLGMDM = new IntentFilter();
                com.android.settings.MDMSettingsAdapter.getInstance()
                        .addUsbPolicyChangeIntentFilter(filterLGMDM);
                getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
            }
        }
        // LGMDM_END
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateToggles(mUsbManager.getDefaultFunction());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mPCSuiteWIFIConnectionEnabler != null) {
            mPCSuiteWIFIConnectionEnabler.resume();
        }

        if (mOSPWIFIConnectionEnabler != null) {
            mOSPWIFIConnectionEnabler.resume();
        }
        //ANDY_END

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_STATE);
        getActivity().registerReceiver(mStateReceiver, intentFilter);

        updateState();
        updateToggles(mUsbManager.getDefaultFunction());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mPCSuiteWIFIConnectionEnabler != null) {
            mPCSuiteWIFIConnectionEnabler.pause();
        }
        if (mOSPWIFIConnectionEnabler != null) {
            mOSPWIFIConnectionEnabler.pause();
        }
        //ANDY_END
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (Config.SPRINT.equals(Config.getOperator())
                    || Config.BM.equals(Config.getOperator())) {
                try {
                    getActivity().unregisterReceiver(mLGMDMReceiver);
                } catch (Exception e) {
                    android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
                }
            }
        }
        // LGMDM_END
    }

    public static boolean getSupportPCSuite() {
        return sSupportPCSuite;
    }

    public static boolean getSupportOSP() {
        return sSupportOSP3;
    }

    //ANDY_END

    private void updateState() {
    }

    private void updateToggles(String function) {
        if (function.equals(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY)) {
            mUsbConnectionType.setSummary(R.string.sp_usb_charger_title_ex_NORMAL);
        } else if (function.equals(UsbManagerConstants.USB_FUNCTION_MTP_ONLY)) {
            if (Utils.isUI_4_1_model(getActivity())) {
                mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL_ex);
            } else {
                mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL);
            }
        } else if (function.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            if (Config.getOperator().equals("VZW")) {
                mUsbConnectionType.setSummary(R.string.sp_connection_ethernet_NORMAL);
            } else {
                mUsbConnectionType.setSummary(R.string.tether_settings_title_usb_ex);
            }
        } else if (function.equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
            if (Config.getOperator().equals("VZW")) {
                mUsbConnectionType.setSummary(R.string.sp_connection_modem_NORMAL);
            }
            else {
                if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
                    mUsbConnectionType.setSummary(R.string.sp_pc_software_NORMAL);
                }
                else {
                    mUsbConnectionType.setSummary(R.string.sp_lg_software_NORMAL);
                }
            }
        } else if (function.equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
                mUsbConnectionType.setSummary(R.string.sp_mass_storage_NORMAL);
        } else if (function.equals(UsbManagerConstants.USB_FUNCTION_PTP_ONLY)) {
            if ("VZW".equals(Config.getOperator())) {
                mUsbConnectionType.setSummary(R.string.usb_ptp_title);
            } else {
                mUsbConnectionType.setSummary(R.string.usb_ptp_title_ex);
            }
        } else if (function.equals(UsbManagerConstants.USB_FUNCTION_AUTO_CONF)) {
            if (Utils.isUI_4_1_model(getActivity())) {
                mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL_ex);
            } else {
                mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL);
            }
        } else {
            // default value check
            if (Config.getOperator().equals("VZW")) {
                if (Utils.isUI_4_1_model(getActivity())) {
                    mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL_ex);
                } else {
                    mUsbConnectionType.setSummary(R.string.sp_usbtype_mtp_title_NORMAL);
                }
            }
            else {
                mUsbConnectionType.setSummary(R.string.sp_usb_charger_title_ex_NORMAL);
            }
            //mUsbConnectionType.setSummary("");
        }
        // Sprint requirement - Tethering
        if (Config.getOperator().equals(Config.SPRINT) || Config.getOperator().equals(Config.BM)) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && com.android.settings.MDMSettingsAdapter.getInstance().setUsbTetherMenu(
                            mUsbTether)) {
                return;
            }
            // LGMDM_END
            if (mUsbTether != null) {
                if (mUsbConnection) {
                    mUsbTether.setEnabled(true);
                    mUsbTether.setSummary(R.string.sp_usbtype_tethering_summary_NORMAL);

                    mUsbTethered = UsbSettingsControl.getTetherStatus(getActivity());
                    if (mUsbTethered) {
                        mUsbTether.setChecked(true);
                        mUsbConnectionType.setEnabled(false);
                    }
                }
                else {
                    mUsbTether.setChecked(false);
                    mUsbTether.setEnabled(false);
                    mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                }
                //SPRINT_USB : //Only Check "restricted"
                //Sprint Android Requirements Version 4.0-1   - 6.9.3.8 USB
                //USB_DATA
                boolean allowTethering = OverlayUtils.getAllowTethering(null); //spr/overlayutils.java
                if (mUsbTether.isEnabled()) {
                    if (allowTethering == false) {
                        mUsbTether.setEnabled(false);
                        mUsbTether.setSummary(R.string.sp_block_usb_thering_NORMAL);
                    }
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mUsbConnectionType) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(UsbSettingsControl.EXTRA_USB_LAUNCHER, false);
            //            if(mUsbManager.getDefaultFunction().equals(UsbManager.USB_FUNCTION_PC_SUITE) && !mUsbConnection) {
            //                bundle.putBoolean(UsbSettingsControl.EXTRA_DIRECT_AUTORUN, true);
            //            }
            //            startFragment(this, UsbSettings.class.getCanonicalName(), -1, bundle);

            if (Utils.supportSplitView(getActivity().getApplicationContext())) {
                startFragment(this, UsbSettings.class.getCanonicalName(), -1, bundle,
                        R.string.sp_usb_connection_type_ex_NORMAL);
            }
            else {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$UsbSettingsActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(UsbSettingsControl.EXTRA_USB_LAUNCHER, false);
                getActivity().getApplicationContext().startActivity(intent);
            }
        } else if (preference == mUsbAlwaysAsk) {
            boolean value = mUsbAlwaysAsk.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.USB_ASK_ON_CONNECTION, value ? 1 : 0);
        }
        else if (preference == mCarHome) {
            boolean value = mCarHome.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.CAR_HOME_AUTO_LAUNCH, value ? 1 : 0);
        }
        else if (preference == mDeskHome) {
            boolean value = mDeskHome.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.DESK_HOME_AUTO_LAUNCH, value ? 1 : 0);
        }
        else if (preference == mMediaHome) {
            boolean value = mMediaHome.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.MEDIA_HOME_AUTO_LAUNCH, value ? 1 : 0);
        }
        else if (preference == mPouchMode) {
            boolean value = mPouchMode.isChecked();
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH, value ? 1 : 0);
        }
        // Sprint requirement - Tethering
        else if (preference == mUsbTether) {
            boolean newState = mUsbTether.isChecked();

            if (newState) {
                onCreateTetherAlertDialg(UsbSettingsControl.DIALOG_TETHERING_ALERT);
            }
            else {
                connectUsbTether(false);
                mUsbConnectionType.setEnabled(true);

                mUsbManager.setCurrentFunction(mUsbManager.getDefaultFunction(), true);
            }
        }
        else if (preference == mUsbHelp) {
            if (Utils.supportSplitView(getActivity().getApplicationContext())) {
                startFragment(this, ConnectivityHelperPopup.class.getCanonicalName(), -1, null,
                        R.string.sp_connectivity_helper_NORMAL);
            } else {
                Intent send = new Intent(Intent.ACTION_MAIN);
                send.setClassName("com.android.settings",
                        "com.android.settings.Settings$ConnectivityHelperPopupActivity");
                startActivity(send);
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public class WIFIConnectionEnabler implements
            Preference.OnPreferenceChangeListener {
        private static final int TYPE_UNKOWN = 0;
        private static final int TYPE_PCSUITE = 1;
        private static final int TYPE_OSP = 2;

        // enum OSP_XPT_TYPE
        public static final int XPT_NONE = -1;
        public static final int XPT_BLUETOOTH = 0;
        public static final int XPT_USB = 1;
        public static final int XPT_TCP_IP = 2;

        private Boolean mOSPServiceBound = false;
        private final Context mContext;
        private final ConnectivityManager mConnManager;

        public WIFIConnectionEnabler(Context context) {
            mContext = context;
            mConnManager = (ConnectivityManager)mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        public void resume() {
            if (mPCSuiteWIFIConnectionOnOff != null) {
                mPCSuiteWIFIConnectionOnOff.setOnPreferenceChangeListener(this);
            }
            if (mOSPWIFIConnectionOnOff != null) {
                mOSPWIFIConnectionOnOff.setOnPreferenceChangeListener(this);
            }

            if (mOSPServiceBound == false && !sNotSupportOSP) {
                Intent intent = new Intent();
                intent.setClassName(OSP_PACKAGE_NAME, OSP_SERVICE_NAME);

                if (mOSPConnection != null) {
                    mOSPServiceBound = mContext.bindService(intent, mOSPConnection, 0);
                }
            }
        }

        public void pause() {
            if (mPCSuiteWIFIConnectionOnOff != null) {
                mPCSuiteWIFIConnectionOnOff.setOnPreferenceChangeListener(null);
            }
            if (mOSPWIFIConnectionOnOff != null) {
                mOSPWIFIConnectionOnOff.setOnPreferenceChangeListener(null);
            }

            if (mOSPServiceBound && !sNotSupportOSP)
            {
                mOSPServiceBound = false;
                if (mOSPConnection != null) {
                    mContext.unbindService(mOSPConnection);
                }
            }
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            if (preference != null) {
                final boolean bEnable = (Boolean)value ? true : false;
                final int nSoftwareType;
                if (preference == mPCSuiteWIFIConnectionOnOff) {
                    nSoftwareType = TYPE_PCSUITE;
                }
                else if (preference == mOSPWIFIConnectionOnOff) {
                    nSoftwareType = TYPE_OSP;
                }
                else {
                    nSoftwareType = TYPE_UNKOWN;
                }

                if (bEnable) {
                    SendWIFIConnectionStatus(true, nSoftwareType);
                } else {
                    String message = getString(R.string.sp_lg_software_wifi_disconnection1_NORMAL);
                    if (nSoftwareType == TYPE_PCSUITE) {
                        // [PCSync][BEGIN][ilhwan.choi] uncheck
                        int pcsuiteWifiConnectionStatus = Settings.System.getInt(
                                getContentResolver(), KEY_WIFI_PCSUITE_CONNECTION_STATUS, 0);
                        if (pcsuiteWifiConnectionStatus == 1) {
                            message = getString(R.string.sp_pcsuite_disable_activated_NORMAL);
                        } else {
                            message = getString(R.string.sp_pcsuite_disable_NORMAL);
                        }
                        // [PCSync][END][ilhwan.choi]
                    }
                    else if (nSoftwareType == TYPE_OSP) {
                        if (isOSPWIFIConnected()) {
                            message = getString(R.string.sp_osp_wifi_disconnection_client_NORMAL);
                        }
                    }

                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.sp_lg_software_note)
                            .setMessage(message)
                            .setPositiveButton(R.string.sp_lg_software_note_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            SendWIFIConnectionStatus(bEnable, nSoftwareType);
                                        }
                                    })
                            .setNegativeButton(R.string.sp_lg_software_note_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            ((CheckBoxPreference)preference).setChecked(true);
                                            SendWIFIConnectionStatus(true, nSoftwareType);
                                        }
                                    })
                            .setOnCancelListener(
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(
                                                DialogInterface dialog) {
                                            ((CheckBoxPreference)preference).setChecked(true);
                                            SendWIFIConnectionStatus(true, nSoftwareType);
                                        }
                                    }).create();

                    if (!Utils.isUI_4_1_model(mContext)) {
                        dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                    }
                    dialog.show();
                }
                return true;
            }
            return false;
        }

        private boolean isOSPWIFIConnected() {
            Boolean connected = false;
            try {
                if (mIOSPService != null) {
                    int transport = (Integer)mOSPgetTransportMethod.invoke(mIOSPService,
                            (Object[])null);
                    if (transport == XPT_TCP_IP) {
                        connected = (Boolean)mOSPIsConnectedMethod.invoke(mIOSPService,
                                (Object[])null);
                    }
                }
            } catch (IllegalAccessException iae) {
                Log.w(TAG, "IllegalAccessException");
            } catch (InvocationTargetException ite) {
                Log.w(TAG, "InvocationTargetException");
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
            return connected;
        }

        private boolean isWIFIAvailable() {
            boolean ret = false;
            if (mConnManager != null) {
                NetworkInfo info = mConnManager
                        .getActiveNetworkInfo();
                if ((info != null)
                        && (info.getType() == ConnectivityManager.TYPE_WIFI)) {
                    ret = true;
                }
            }
            return ret;
        }

        private void SendWIFIConnectionStatus(boolean value, int softwareType) {
            String message = "";
            String action = "";
            if (softwareType == TYPE_PCSUITE) {
                message = SettingsConstants.System.LINKCLOUD_WIFI_CONNECTION;
                action = ACTION_LINKCLOUD_WIFI_CONNECTION;
            }
            else if (softwareType == TYPE_OSP) {
                message = SettingsConstants.System.OSP_WIFI_CONNECTION;
                action = ACTION_OSP_WIFI_CONNECTION;
            }

            int oldValue = Settings.System.getInt(getContentResolver(), message, 0);
            int newValue = value ? 1 : 0;
            if (oldValue != newValue) {
                Intent intent = new Intent(action);
                intent.putExtra("connected", value);
                Settings.System.putInt(mContext.getContentResolver(), message, newValue);
                getActivity().sendBroadcast(intent);

                if (softwareType == TYPE_OSP) {
                    if (isOSPWIFIConnected()) {
                        Intent stopIntent = new Intent(ACTION_OSP_DISCONNECT);
                        stopIntent.setPackage(OSP_PACKAGE_NAME);
                        getActivity().sendBroadcast(stopIntent);
                    }
                }
            }
        }

        private final ServiceConnection mOSPConnection = (com.lge.config.ConfigBuildFlags.CAPP_OSP) ?
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        if (mOSPWIFIConnectionOnOff != null) {
                            try {
                                if (mOSPAsInterfaceMethod != null) {
                                    mIOSPService = mOSPAsInterfaceMethod.invoke(null, service);
                                }
                            } catch (IllegalAccessException iae) {
                                Log.w(TAG, "IllegalAccessException");
                            } catch (InvocationTargetException ite) {
                                Log.w(TAG, "InvocationTargetException");
                            } catch (Exception e) {
                                Log.w(TAG, "Exception");
                            }
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName className) {
                        mIOSPService = null;
                    }
                }
                : null;
    }

    //ANDY_END

    // LGE_CHANGE_S : Auto-launch for VZW
    private void initAutoLaunch() {
        mAutoLaunch = (PreferenceCategory)findPreference(KEY_AUTO_LAUNCH);

        mCarHome = (CheckBoxPreference)findPreference(KEY_CAR_HOME);
        mCarHome.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.CAR_HOME_AUTO_LAUNCH, 1) != 0);

        mDeskHome = (CheckBoxPreference)findPreference(KEY_DESK_HOME);
        mDeskHome.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.DESK_HOME_AUTO_LAUNCH, 1) != 0);

        mMediaHome = (CheckBoxPreference)findPreference(KEY_MEDIA_MODE);
        mMediaHome.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.MEDIA_HOME_AUTO_LAUNCH, 1) != 0);

        mPouchMode = (CheckBoxPreference)findPreference(KEY_POUCH_MODE);
        mPouchMode.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH, 1) != 0);

        if ((Config.VZW).equals(Config.getOperator())) {
            if (Build.DEVICE.equals("geeb")) { // G VZW Model
                mAutoLaunch.removePreference(mCarHome);
                mAutoLaunch.removePreference(mDeskHome);
                mAutoLaunch.removePreference(mPouchMode);
                mAutoLaunch.removePreference(findPreference(KEY_POUCH_NOTIFICATION));
            }
            else {
                getPreferenceScreen().removePreference(mAutoLaunch);
            }
        }
        else {
            getPreferenceScreen().removePreference(mAutoLaunch);
        }

    }

    // LGE_CHANGE_E : Auto-launch for VZW
    //    private String getCurrentFunction() {
    //        String functions = SystemProperties.get("sys.usb.config", "");
    //        int commaIndex = functions.indexOf(',');
    //        if (commaIndex > 0) {
    //            return functions.substring(0, commaIndex);
    //        } else
    //            return functions;
    //    }

    // Sprint requirement - Tethering
    private void onCreateTetherAlertDialg(int dialogId) {
        if (dialogId == UsbSettingsControl.DIALOG_TETHERING_ALERT) {
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.sp_usb_tethering_NORMAL)
                    .setMessage(R.string.sp_usbtether_may_incur_charges_NORMAL)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                mUsbTether.setChecked(false);
                                dialog.dismiss();
                            }
                            return true;
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mUsbTether.setChecked(false);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mUsbTether.setChecked(false);
                                    dialog.dismiss();
                                }
                            })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            connectUsbTether(true);

                            dialog.dismiss();
                        }
                    });
            altDialog.show();
        }
    }

    private void connectUsbTether(boolean connection) {
        final ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return;
        }

        if (connection == true) {
            UsbSettingsControl.setTetherStatus(getActivity(), false);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.w(TAG, "InterruptedException");
            }
            if (cm.setUsbTethering(true) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                Log.d(TAG, "[AUTORUN] ============ Tethering ERROR !! ============");
                //showDialog(UsbSettingsControl.DIALOG_TETHERING_DISCONNECTION);
                mUsbTether.setChecked(false);
            } else {
                mUsbTethered = true;
                mUsbConnectionType.setEnabled(false);
                Log.d(TAG, "[AUTORUN] ============ Tethering OK !!  ==============");
            }
        } else {
            UsbSettingsControl.setTetherStatus(getActivity(), false);
            cm.setUsbTethering(false);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Log.w(TAG, "InterruptedException");
            }
            mUsbTethered = false;
            mUsbConnectionType.setEnabled(true);
        }
        UsbSettingsControl.mTetherChanged = true;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (Config.SPRINT.equals(Config.getOperator())
                        || Config.BM.equals(Config.getOperator())) {
                    if (com.android.settings.MDMSettingsAdapter.getInstance()
                            .receiveUsbPolicyChangeIntent(intent)) {
                        getActivity().finish();
                    }
                }
            }
        }
    };
    // LGMDM_END
}
