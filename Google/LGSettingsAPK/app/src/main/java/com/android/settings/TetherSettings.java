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

import com.android.settings.wifi.WifiApEnabler;
import com.android.settings.wifi.WifiApDialog;
import com.android.settings.lge.CupssHotspotConfigurationMng;
import com.android.settings.lge.TetherSettingsHelp;
import com.android.settings.R; // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com

import android.app.Activity;
import android.app.ActionBar; // Wi-Fi AP On/Off Switch
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.SharedPreferences; // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
// JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
import android.text.TextWatcher;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Editable;
import android.view.Gravity; // Wi-Fi AP On/Off Switch
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.text.Spanned;
import android.view.View;
// JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
// JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
import com.android.settings.deviceinfo.UsbSettingsControl;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager; // keypad display
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch; // Wi-Fi AP On/Off Switch
import android.widget.TextView;
import android.text.Editable;
import android.widget.Toast;
import android.os.Handler;
// JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]
import android.view.WindowManager; // Configure Wi-Fi hotspot keypad doesn't come up, 20130330, hyeondug.yeo@lge.com

// soonhyuk.choi@lge.com
import java.io.File; // allowed device
// soonhyuk.choi@lge.com

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Locale;

// JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com
import android.content.ContentResolver;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import java.util.regex.Pattern; // seodongjo@lge.com #140563 2012.08.09

// 3LM_MDM_DCM jihun.im@lge.com 20121015
// LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  SET_MAX_CLIENT
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.preference.PreferenceCategory;
import java.util.List;
import android.graphics.Color;
import java.util.Timer;
import java.util.TimerTask;
import android.net.NetworkInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.TelephonyIntents;
// LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  SET_MAX_CLIENT
import android.os.Build;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lgesetting.Config.Config;
import java.util.UUID;
import android.os.BatteryManager;

// soonhyuk.choi@lge.com for the WPS of softap
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.wifi.WpsApDialog;
import com.android.settings.wifi.WpsNfcDialog;
import com.android.settings.SettingsBreadCrumb;
import android.nfc.*;
import android.os.Message;
// soonhyuk.choi@lge.com for the WPS of softap

import android.widget.Button;
import java.util.WeakHashMap;
import android.text.format.DateFormat;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.DialogInterface.OnCancelListener;
import com.lge.wifi.impl.WifiExtManager;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;

/*
 * Displays preferences for Tethering.
 */
public class TetherSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener, View.OnClickListener,
        Preference.OnPreferenceChangeListener, TextWatcher
        {

    private static final String TAG = "TetherSettings";

    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    private static final String ENABLE_WIFI_AP = "enable_wifi_ap";

    // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com
    private static final String KEY_HOTSPOT_TIMEOUT = "hotspot_timeout";
    private static final int TETHERING_TIME_VALUE = 300000;
    private static final int TETHERING_TIME_VALUE_TMUS = 600000;
    private static int sTIME_VALUE = 0;

    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    private static final String TETHER_CHOICE = "TETHER_TYPE";
    private static final String TETHERING_HELP = "tethering_help";
    // seodongjo@lge.com Apply Unicode Menu
    private static final String ENABLE_UNICODE = "wifi_unicode";
    private static final int DIALOG_AP_SETTINGS = 1;
    private static final int DIALOG_TETHER_ALERT = 2;
    //private static final int DIALOG_DCM_TETHER_ALERT = DIALOG_TETHER_ALERT + 1;

    // LGE_CHANGE_S [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    private static final int DIALOG_NO_SIM_ALERT = DIALOG_TETHER_ALERT + 2;
    // LGE_CHANGE_E [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    // LGE_UPDATE_S jeongwook.kim [13/11/12] not using mView
    //    private WebView mView;
    // LGE_UPDATE_E jeongwook.kim [13/11/12] not using mView
    // soonhyuk.choi@lge.com for the WPS
    private static final int WPS_PBC_DIALOG_ID = DIALOG_TETHER_ALERT + 3;
    private static final int WPS_PIN_DIALOG_ID = DIALOG_TETHER_ALERT + 4;
    private static final int WPS_NFC_DIALOG_ID = DIALOG_TETHER_ALERT + 5;
    //private static final int WPS_NFC_CONFIG_TOKEN_WRITE_DIALOG_ID = DIALOG_TETHER_ALERT + 6;
    //    private static final int WPS_NFC_CONFIG_READ_WRITE_DIALOG_ID = DIALOG_TETHER_ALERT + 7;

    private static final int WPS_AP_PBC = 0;
    private static final int WPS_AP_DISPLAY = 1; // WPA-PIN
    private static final int WPS_AP_NFC = 2; // WPA-NFC
    private static final int WPS_NFC_CONFIG_PUSH_SUCCESS = 3; // WPS_NFC_CONFIG_TOKEN_PUSH_SUCCESS_TOAST
    private static final int WPS_NFC_CONFIG_PUSH_FAIL = 4; // WPS_NFC_CONFIG_TOKEN_PUSH_FAIL_TOAST

    private static final String SSID_ID = "1045";
    private static final String NETWORK_KEY_ID = "1027";
    private static final int NDEF_PREFIX_LEN = 52;
    private static final String SSID_FORMAT = "104500%s%s";
    private static final String PASSWORD_FORMAT = "102700%s%s";
    private static final String NETWORK_INDEX_FORMAT = "10260001";
    private static final int HEX_RADIX = 16;
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private NfcAdapter wpsnfcAdapter;
    private static NdefMessage mNdefMessage = null;
    private static boolean DBG = true;
    // soonhyuk.choi@lge.com for the WPS
    private CheckBoxPreference mUsbTether;

    private WifiApEnabler mWifiApEnabler;
    private CheckBoxPreference mEnableWifiAp;

    // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com
    private ListPreference hotSpotTimeoutPreference;
    //private String[] mEntry;
    private int mIndex = 0;
    private static final int CHANGE_TO_MINUTE = 60 * 1000;

    private CheckBoxPreference mBluetoothTether;
    // seodongjo@lge.com Apply Unicode Menu
    private CheckBoxPreference mEnableUnicode;
    private PreferenceScreen mTetherHelp;
    private BroadcastReceiver mTetherChangeReceiver;

    private String[] mUsbRegexs;

    private String[] mWifiRegexs;

    private String[] mBluetoothRegexs;
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference<BluetoothPan>();

    private static final String WIFI_AP_SSID_AND_SECURITY = "wifi_ap_ssid_and_security";

    private String[] mSecurityType;
    private Preference mCreateNetwork;
    private static final int CONFIG_SUBTEXT = R.string.wifi_tether_configure_subtext;

    private WifiApDialog mDialog;
    private WifiManager mWifiManager;
    private WifiConfiguration mWifiConfig = null;

    private boolean mUsbConnected;
    private boolean mMassStorageActive;

    private boolean mBluetoothEnableForTether;

    private static final int INVALID = -1;
    private static final int WIFI_TETHERING = 0;
    private static final int USB_TETHERING = 1;
    private static final int BLUETOOTH_TETHERING = 2;

    private int mAvailableTetherNum = 0;
    private int mDefaultTether = WIFI_TETHERING;

    /* One of INVALID, WIFI_TETHERING, USB_TETHERING or BLUETOOTH_TETHERING */
    private int mTetherChoice = INVALID;

    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private static final int PROVISION_REQUEST = 0;

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
    private View mAlertDialogView;
    private AlertDialog.Builder mAlertDialogBuilder;
    private AlertDialog mAlertDialog;
    private AlertDialog mDCMAlertDialog;

    private EditText mPassword;
    private boolean mInit = false;
    private boolean mDoNotShowAgain = false;
    private Switch actionBarSwitch; // Wi-Fi AP On/Off Switch
    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]

    // Hotspot Notice Dialog

    private Timer mTimerSharedP;
    private Handler mHandlerSharedP;
    private Preference mClientNoDevices;
    SettingsBreadCrumb mBreadCrumb;
    private UserManager mUm;
    private boolean mUnavailable;

    private static final String TARGET_OPERATOR = Config.getOperator();
    private static final String TARGET_COUNTRY = Config.getCountry();
    // soonhyuk.choi@lge.com for the WPS of softap
    private static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_WPS_NFC = Menu.FIRST + 2;

    // soonhyuk.choi@lge.com for the WPS of softap
    //[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC
    private static final int MENU_ID_WPS_NFC_CONFIG_TOKEN_WRITE = Menu.FIRST + 3;
    //    private static final int MENU_ID_WPS_NFC_CONFIG_TOKEN_READ = Menu.FIRST + 4;
    //[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC

    // wps_nfc
    //private Intent mWpsIntent = null;

    public static final String WPS_NFC_CONF_TOKEN_REQUEST = "com.lge.wifi.WPS_NFC_CONF_TOKEN_REQUEST";
    public static final String WPS_NFC_CONF_TOKEN_RECEIVED = "com.lge.wifi.WPS_NFC_CONF_TOKEN_RECEIVED";
    public static final String EXTRA_WPS_NFC_CONF_NDEF_TOKEN = "extra_wps_nfc_conf_ndef_token";

    @Override
    public void onCreate(Bundle icicle) {
        if (!"DCM".equals(Config.getOperator())) {
           if (getActivity().getIntent().getAction() != null
                    && getActivity().getIntent().getAction().equals("android.settings.TETHER_SETTINGS")) {
                Intent intent = new Intent();
                intent.setAction("android.settings.TETHER_SETTINGS_EXTERNAL");
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        }

        super.onCreate(icicle);

        // [S][chris.won@lge.com][2012-10-18] Applying New Tethering Icon for
        // DCM
        if ("DCM".equals(Config.getOperator())) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_hotspot_dcm);
        }
        // [E][chris.won@lge.com][2012-10-18] Applying New Tethering Icon for
        // DCM
        addPreferencesFromResource(R.xml.tether_prefs);
        Log.e(TAG, "onCreate() in Settings package");

        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        if (mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
            mUnavailable = true;
            setPreferenceScreen(new PreferenceScreen(getActivity(), null));
            return;
        }

        final Activity activity = getActivity();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(activity.getApplicationContext(), mProfileServiceListener,
                    BluetoothProfile.PAN);
        }

        mEnableWifiAp = (CheckBoxPreference)findPreference(ENABLE_WIFI_AP);
        mEnableUnicode = (CheckBoxPreference)findPreference(ENABLE_UNICODE); // seodongjo@lge.com Apply Unicode Menu
        Preference wifiApSettings = findPreference(WIFI_AP_SSID_AND_SECURITY);
        mUsbTether = (CheckBoxPreference)findPreference(USB_TETHER_SETTINGS);

        wifiApSettings.setTitle(R.string.sp_wifi_hotspot_set_up_title_NORMAL);

        if (/*!"KR".equals(Config.getCountry()) && */mEnableUnicode != null) { // 2014.11.20 seodongjo Erase Menu requested by UX.
            getPreferenceScreen().removePreference(mEnableUnicode);
        }
        // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [S]
        hotSpotTimeoutPreference = (ListPreference)findPreference(KEY_HOTSPOT_TIMEOUT);

        hotSpotTimeoutPreference.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.TETHERING_TIME,
                TETHERING_TIME_VALUE)));

        mIndex = hotSpotTimeoutPreference.findIndexOfValue(hotSpotTimeoutPreference.getValue());
        //[TD#32421] Set sTIME_VALUE to use EntryValues. 2013-05-20, ilyong.oh@lge.com
        sTIME_VALUE = Integer.parseInt(hotSpotTimeoutPreference.getValue()) / CHANGE_TO_MINUTE; //change to minute.

        updateTimeoutSummary();
        hotSpotTimeoutPreference.setOnPreferenceChangeListener(this);
        // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [E]


        mBluetoothTether = (CheckBoxPreference)findPreference(ENABLE_BLUETOOTH_TETHERING);
        mTetherHelp = (PreferenceScreen)findPreference(TETHERING_HELP);

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mUsbRegexs = cm.getTetherableUsbRegexs();
        mWifiRegexs = cm.getTetherableWifiRegexs();
        mBluetoothRegexs = cm.getTetherableBluetoothRegexs();

        //final boolean usbAvailable = mUsbRegexs.length != 0;
        // kerry - to remove usb tethering
        boolean usbAvailable = false;
        if (Config.getOperator().equals("DCM")) {
            usbAvailable = mUsbRegexs.length != 0;
        }

        final boolean wifiAvailable = mWifiRegexs.length != 0;

        //LG_BTUI : Disable PAN-[s]
        /* google original
        final boolean bluetoothAvailable = mBluetoothRegexs.length != 0;
        */
        final boolean bluetoothAvailable = false;
        //LG_BTUI : Disable PAN-[e]

        // For Tethering Help menu
        mAvailableTetherNum = 0;
        if (usbAvailable) {
            mAvailableTetherNum++;
            mDefaultTether = USB_TETHERING;
        }
        if (bluetoothAvailable) {
            mAvailableTetherNum++;
            mDefaultTether = BLUETOOTH_TETHERING;
        }
        if (wifiAvailable) {
            mAvailableTetherNum++;
            mDefaultTether = WIFI_TETHERING;
        }

        // Support the help in "Tethering & Network"

        if (!usbAvailable || Utils.isMonkeyRunning()) {
            getPreferenceScreen().removePreference(mUsbTether);
        }

        if (wifiAvailable && !Utils.isMonkeyRunning()) {
            Log.d(TAG, "wifiAvailable && !Utils.isMonkeyRunning()***");
            if (Config.getOperator().equals("DCM")) {
                mWifiApEnabler = new WifiApEnabler(activity, mEnableWifiAp);
            }
            initWifiTethering();
        } else {
            if (mEnableWifiAp != null) {
                getPreferenceScreen().removePreference(mEnableWifiAp);
            }
            getPreferenceScreen().removePreference(wifiApSettings);
        }

        //LG_BTUI : Disable PAN-[s]
        //        if (!bluetoothAvailable) {
        //        if (!bluetoothAvailable || !isSupportBtTether()) {
        //LG_BTUI : Disable PAN-[e]
        getPreferenceScreen().removePreference(mBluetoothTether); //[chris.won@lge.com][2013-04-05] BT Tethering moved
        //        } else {
        //            BluetoothPan pan = mBluetoothPan.get();
        //            if (pan != null && pan.isTetheringOn()) {
        //                mBluetoothTether.setChecked(true);
        //            } else {
        //                mBluetoothTether.setChecked(false);
        //            }
        //        }

        // LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check
        mProvisionApp = getResources().getStringArray(
                com.android.internal.R.array.config_mobile_hotspot_provision_app);

        // LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check

        // LGE_UPDATE_S jeongwook.kim [13/11/12] not using mView
        //        mView = new WebView(activity);
        // LGE_UPDATE_E jeongwook.kim [13/11/12] not using mView

        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
        SharedPreferences pref_ff = getActivity().getSharedPreferences("FIRST_FLAG",
                Activity.MODE_PRIVATE);
        boolean flag_ff = pref_ff.getBoolean("f_flag", true);

        if (flag_ff) {
            mInit = true;
        } else {
            mInit = false;
        }
        SharedPreferences pref_ns = getActivity().getSharedPreferences("NOT_SHOW",
                Activity.MODE_PRIVATE);
        boolean flag_ns = pref_ns.getBoolean("not_show", false);

        if (flag_ns) {
            mDoNotShowAgain = true;
        } else {
            mDoNotShowAgain = false;
        }

        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]
        if ("DCM".equals(Config.getOperator())) {
            activity.getActionBar().setTitle(R.string.sp_tethering_title_jp_NORMAL);
            if (mEnableWifiAp != null) {
                mEnableWifiAp.setTitle(R.string.sp_wifi_tethering_jp_NORMAL);
            }
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addTetherPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        // soonhyuk.choi@lge.com for the creation of the munu of WPS
        if (checkWPSAllfuncEnablebyProp() == true) {
            setHasOptionsMenu(true);
        }
        // soonhyuk.choi@lge.com for the creation of the munu of WPS
        if (Utils.isTablet()) {
            activity.getActionBar().setTitle(R.string.settings_label);
            activity.getActionBar().setIcon(R.mipmap.ic_launcher_settings);
        }
    }

    private void CreateNetworkUpdate() {
        final Activity activity = getActivity();
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiConfig = getCupssHotSpotConfigurations(mWifiManager);
        Log.e(TAG, "CreateNetworkUpdate() is called, Operator = " + Config.getOperator());

        mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);
        mCreateNetwork = findPreference(WIFI_AP_SSID_AND_SECURITY);

        if (mCreateNetwork != null) {
            if (mWifiConfig == null) {
                final String s = activity.getString(
                        com.android.internal.R.string.wifi_tether_configure_ssid_default);
                mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                        s, mSecurityType[WifiApDialog.OPEN_INDEX]));
            } else {
                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                        mWifiConfig.SSID,
                        mSecurityType[index]));
            }
        }
    }

    private void initWifiTethering() {
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiConfig = getCupssHotSpotConfigurations(mWifiManager);
        // V5_TLS_Wifi_SSID runqin.luo@lge.com 2012-12-13 Portable wifi hotspot
        // for telus[START]
        // LGE_UPDATE_S [kuhyun.kwon][2012/02/25] Status flag of shotspot ssid
        // modification for Telus
        Log.e(TAG, "initWifiTethering() is called, Operator = " + Config.getOperator());
        CreateNetworkUpdate();
    }

    private BluetoothProfile.ServiceListener mProfileServiceListener =
            new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mBluetoothPan.set((BluetoothPan)proxy);
                }

                public void onServiceDisconnected(int profile) {
                    mBluetoothPan.set(null);
                }
            };

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_AP_SETTINGS) {
            final Activity activity = getActivity();
            mWifiConfig = getCupssHotSpotConfigurations(mWifiManager);
            mDialog = new WifiApDialog(activity, this, mWifiConfig);
            // Configure Wi-Fi hotspot keypad doesn't come up, 20130330, hyeondug.yeo@lge.com [S]
            mDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            // Configure Wi-Fi hotspot keypad doesn't come up, 20130330, hyeondug.yeo@lge.com [E]
            return mDialog;
        }
        // soonhyuk.choi@lge.com for the WPS
        else if (id == WPS_PBC_DIALOG_ID) {
            return new WpsApDialog(getActivity(), WPS_AP_PBC);
        } else if (id == WPS_PIN_DIALOG_ID) {
            return new WpsApDialog(getActivity(), WPS_AP_DISPLAY);
        } else if (id == WPS_NFC_DIALOG_ID) {
            return new WpsApDialog(getActivity(), WPS_AP_NFC);
        }
        // soonhyuk.choi@lge.com for the WPS
        return null;
    }

    private class UsbDCMPopupFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // TODO Auto-generated method stub

            Locale locale = Locale.getDefault();
            WebView mAttentionView;
            View alertDialogView;

            AssetManager am = getActivity().getAssets();

            String path = UsbSettingsControl.TETHER_PATH.replace("%y", locale.getLanguage()
                    .toLowerCase());

            if (locale.getCountry().equals("JP")) {
                path = UsbSettingsControl.TETHER_PATH.replace("%y", locale.getLanguage()
                        .toLowerCase());
                path = path.replace("%z", "_" + locale.getCountry().toLowerCase());
                path = path.replace("%x", locale.getLanguage().toLowerCase());
            } else {
                path = UsbSettingsControl.TETHER_PATH.replace("%y", "en");
                path = path.replace("%z", "_" + "us");
                path = path.replace("%x", "en");
            }

            //Log.i("TetherSettings","path = "+ path);
            boolean useCountry = true;
            InputStream is = null;

            try {
                is = am.open(path);
            } catch (Exception e) {
                useCountry = false;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        Log.w("TetherSettings", "Exception");
                    }
                }
            }

            String url = UsbSettingsControl.TETHER_URL.replace("%y", locale.getLanguage()
                    .toLowerCase());

            if (locale.getCountry().equals("JP")) {
                url = UsbSettingsControl.TETHER_URL.replace("%y", locale.getLanguage()
                        .toLowerCase());
                url = url
                        .replace("%z", (useCountry ? "_" + locale.getCountry().toLowerCase() : ""));
                url = url.replace("%x", locale.getLanguage().toLowerCase());
            } else {
                url = UsbSettingsControl.TETHER_URL.replace("%y", "en");
                url = url.replace("%z", (useCountry ? "_" + "us" : ""));
                url = url.replace("%x", "en");
            }

            //Log.i("TetherSettings","url = "+ url);

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(
                    getActivity().LAYOUT_INFLATER_SERVICE);
            alertDialogView = inflater.inflate(R.layout.tether_notice_dialog, null);
            mAttentionView = (WebView)alertDialogView.findViewById(R.id.DialogWebView);

            // Fix a WBT issue
            WebSettings ws = mAttentionView.getSettings();
            if (ws != null) {
                ws.setBuiltInZoomControls(false);
                ws.setSupportZoom(false);
            }
            mAttentionView.loadUrl(url);

            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity());
            altDialog.setCancelable(true);
            altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            altDialog.setTitle(R.string.sp_wifi_tether_notice_title_attention_SHORT);
            altDialog.setView(alertDialogView);
            altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        setUsbTethering(false);
                        // connectUsbTether(false);
                        mUsbTether.setChecked(false);
                        updateState();

                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            altDialog.setNegativeButton(R.string.sp_wifi_tether_notice_button_cancel_SHORT,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setUsbTethering(false);
                            // connectUsbTether(false);
                            mUsbTether.setChecked(false);
                            updateState();
                        }
                    });
            altDialog.setPositiveButton(R.string.sp_wifi_tether_notice_button_OK_SHORT,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setUsbTethering(true);
                            // connectUsbTether(true);
                        }
                    });
            mDCMAlertDialog = altDialog.show();
            return mDCMAlertDialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            setUsbTethering(false);
            // connectUsbTether(false);
            mUsbTether.setChecked(false);
            updateState();
            super.onCancel(dialog);
        }
    }

    private class TetherChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                // TODO - this should understand the interface types
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);

                // seodongjo@lge.com TD.212165 Dismiss Portable hotspot Setting
                // Pop up When turn on the Wifi
                if (mWifiManager != null) {
                    if (WifiManager.WIFI_STATE_ENABLING == mWifiManager.getWifiState())
                    {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }
                }
                if (null != available && null != active && null != errored) {
                    updateState(available.toArray(new String[available.size()]),
                            active.toArray(new String[active.size()]),
                            errored.toArray(new String[errored.size()]));
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mMassStorageActive = true;
                updateState();
            } else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mMassStorageActive = false;
                updateState();
            } else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                mUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                updateState();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBluetoothEnableForTether) {
                    switch (intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_ON:
                        BluetoothPan bluetoothPan = mBluetoothPan.get();
                        if (bluetoothPan != null) {
                            bluetoothPan.setBluetoothTethering(true);
                            mBluetoothEnableForTether = false;
                        }
                        break;

                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.ERROR:
                        mBluetoothEnableForTether = false;
                        break;

                    default:
                        // ignore transition states
                    }
                }
                updateState();
            } else if (action.equals("com.lge.settings.WifiApEnabler.UpdateTethering")) {
                Log.e(TAG, "CreateNetworkUpdate() is called, UpdateTethering intent");
                CreateNetworkUpdate();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mUnavailable) {
            TextView emptyView = (TextView)getView().findViewById(android.R.id.empty);
            getListView().setEmptyView(emptyView);
            if (emptyView != null) {
                emptyView.setText(R.string.tethering_settings_not_available);
            }
            return;
        }

        final Activity activity = getActivity();

        mMassStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());
        mTetherChangeReceiver = new TetherChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        Intent intent = activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        if (intent != null) {
            mTetherChangeReceiver.onReceive(activity, intent);
        }
        if (mWifiApEnabler != null) {
            if (Config.getOperator().equals("DCM")) {
                mEnableWifiAp.setOnPreferenceChangeListener(this);
            }
            mWifiApEnabler.resume();
        }
        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 20120516, hyeondug.yeo@lge.com, Finish the Activity for
        // "Not tethering supported" operator.
        final Activity activity = getActivity();
        ConnectivityManager cm = (ConnectivityManager)activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        updateTimeoutSummary(); // JB : Wi-Fi hotspot "timeout", 120920, // hyeondug.yeo@lge.com
        CreateNetworkUpdate(); // Update for Wi-Fi hotspot menu summary

        //Set Swtich again for entitlement
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mWifiApEnabler != null) {
                mBreadCrumb = new SettingsBreadCrumb(getActivity());
                mBreadCrumb.addSwitch(actionBarSwitch);
                actionBarSwitch.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    }
                });
            }
        }

        if (checkWPSApSupportedbyProp() == true) {
            wpsnfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
            readyToSendWpsnfc();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (checkWPSApSupportedbyProp() == true) {
            WPSNfcPushModeOff();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mUnavailable) {
            return;
        }
        getActivity().unregisterReceiver(mTetherChangeReceiver);
        mTetherChangeReceiver = null;
        if (mWifiApEnabler != null) {
            if (Config.getOperator().equals("DCM")) {
                mEnableWifiAp.setOnPreferenceChangeListener(null);
            }
            mWifiApEnabler.pause();
        }
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //[START] soonhyuk.choi@lge.com for WPS on the softpap
        if (checkWPSAllfuncEnablebyProp() == true) {
            Log.d(TAG, "onCreateOptionsMenu()");
            if (!mWifiManager.isWifiApEnabled()) {
                menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
                        .setEnabled(true)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.add(Menu.NONE, MENU_ID_WPS_PIN, 0, R.string.wifi_menu_wps_pin)
                        .setEnabled(true)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            } else {
                menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
                        .setEnabled(true)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.add(Menu.NONE, MENU_ID_WPS_PIN, 0, R.string.wifi_menu_wps_pin)
                        .setEnabled(true)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
           }
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC
            if (checkWPSAllfuncEnablebyProp() == true) {
              menu.add(Menu.NONE, MENU_ID_WPS_NFC_CONFIG_TOKEN_WRITE, 0, R.string.wifi_menu_wps_nfc_config_token)
                    .setEnabled(true)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//                menu.add(Menu.NONE, MENU_ID_WPS_NFC_CONFIG_TOKEN_READ, 0, "CONFIG TOKEN READ")
//                        .setEnabled(true)
//                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC
            super.onCreateOptionsMenu(menu, inflater);
        }
        //[END] soonhyuk.choi@lge.com for WPS on the softpap
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //[START] soonhyuk.choi@lge.com for WPS on the softpap
        if (checkWPSAllfuncEnablebyProp() == true) {
            if (!mWifiManager.isWifiApEnabled()) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "To run the WPS, the SoftAP turn on !!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            Log.d(TAG, "onCreateOptionsMenu()" + item.getItemId());
            switch (item.getItemId()) {
            case MENU_ID_WPS_PBC:
                showDialog(WPS_PBC_DIALOG_ID);
                return true;

            case MENU_ID_WPS_PIN:
                showDialog(WPS_PIN_DIALOG_ID);
                return true;

            case MENU_ID_WPS_NFC:
                Log.i(TAG, "WPS: MENU_ID_WPS_NFC SELECTED");
                wpsnfcAdapter.invokeBeam(getActivity());
                readyToSendWpsnfc();
                return true;
                //[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC
            case MENU_ID_WPS_NFC_CONFIG_TOKEN_WRITE:
                Intent configIntent = new Intent();
                configIntent.setClassName(getActivity(), "com.android.settings.wifi.WpsNfcDialog");
                configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                configIntent.putExtra("ACTION", WPS_NFC_CONF_TOKEN_REQUEST);
                getActivity().startActivity(configIntent);

                //                    mWpsIntent = new Intent(WPS_NFC_CONF_TOKEN_REQUEST);
                //                    mWpsIntent.putExtra(EXTRA_WPS_NFC_CONF_NDEF_TOKEN, 0);
                //                    getActivity().sendBroadcast(mWpsIntent);
                return true;
                //[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC
            default:
                break;
            }
        }
        return super.onOptionsItemSelected(item);
        //[END] soonhyuk.choi@lge.com for WPS on the softpap
    }

//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : Hotspot Password Push
    private void readyToSendWpsnfc() {
        Log.i(TAG, "WPS: readyToSendWpsnfc");
        String wps_nfc_configuration_token = makeConfigNDEF();
        if(wps_nfc_configuration_token != null) {
            getConfigTokenMessage(wps_nfc_configuration_token);
            WPSNfcPushModeOn(mNdefMessage);
            wpsnfcAdapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
                @Override
                public void onNdefPushComplete(NfcEvent event) {
                    Log.d(TAG, "onNdefPushComplete");
                    mToastHandler.sendEmptyMessage(WPS_NFC_CONFIG_PUSH_SUCCESS);
                }
            }, getActivity());

        } else {
            mToastHandler.sendEmptyMessage(WPS_NFC_CONFIG_PUSH_FAIL);
        }
    }

    private String makeConfigNDEF() {
        WifiConfiguration configuration = mWifiManager.getWifiApConfiguration();

        String ssidHex = byteArrayToHexString(configuration.SSID.getBytes());

        String ssidLength = configuration.SSID.length() >= HEX_RADIX
                ? Integer.toString(configuration.SSID.length(), HEX_RADIX)
                : "0" + Character.forDigit(configuration.SSID.length(), HEX_RADIX);

        ssidHex = String.format(SSID_FORMAT, ssidLength, ssidHex).toUpperCase();

        String passwordHex = "00";
        String passwordLength = "00";
        if (configuration.preSharedKey != null && !configuration.preSharedKey.equals("")) {
            passwordHex = byteArrayToHexString(configuration.preSharedKey.getBytes());

            passwordLength = configuration.preSharedKey.length() >= HEX_RADIX
                    ? Integer.toString(configuration.preSharedKey.length(), HEX_RADIX)
                    : "0" + Character.forDigit(configuration.preSharedKey.length(), HEX_RADIX);

        }
        passwordHex = String.format(PASSWORD_FORMAT, passwordLength, passwordHex).toUpperCase();

        return NETWORK_INDEX_FORMAT + ssidHex + passwordHex;
    }

    private void getConfigTokenMessage(String pre_wps_nfc_config_token) {
             byte[] payload;
             String wps_nfc_config_token = "";

             if(DBG) Log.d(TAG, "WPS: pre_wps_nfc_config_token.length()= " + pre_wps_nfc_config_token.length());
             if(pre_wps_nfc_config_token == null) {
                 Log.i(TAG, "WPS: pre_wps_nfc_config_token is null");
                 Toast.makeText(getActivity(), "Fail to get data!",Toast.LENGTH_SHORT).show();
                 return;
             } else {
                wps_nfc_config_token = pre_wps_nfc_config_token;
             }

             if(DBG) Log.d(TAG, "WPS: before NDEF Header remove : " + pre_wps_nfc_config_token);

             payload = createStringToPayload(wps_nfc_config_token);

             if(wps_nfc_config_token == null) {
                 Log.e(TAG, "WPS: wps_nfc_config_token is null!!");
                 return;
             }
             if(DBG) Log.d(TAG, "WPS: wps_nfc_config_token : " + wps_nfc_config_token);

             NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "application/vnd.wfa.wsc".getBytes(), new byte[0], payload);
             mNdefMessage = new NdefMessage(new NdefRecord[] {record});
    }

    private byte[] createStringToPayload(String string) {
        int len = string.length();
        String[] payloadSplit = new String[len / 2];

        Log.d(TAG, "WPS: createStringToPayload string: "  + string);
        for (int i = 0; i < len / 2; i++) {
            int a = (i * 2);
            payloadSplit[i] = string.substring(a, a + 2);

        }

        Log.d(TAG, "WPS: payloadSplit: "  + payloadSplit);

        // make string to byte type.
        ArrayList<Byte> byteArray = new ArrayList<Byte>();
        for (String payloadbyte : payloadSplit) {
            try {
                int temp = Integer.parseInt(payloadbyte, 16);
                byteArray.add((byte) temp);
            } catch (Exception e) {
                 Log.d(TAG, "WPS: Parse fail!");
            }
        }

        Byte[] payloadByte = new Byte[byteArray.size()];
        payloadByte = byteArray.toArray(payloadByte);
        byte[] mPayload = new byte[byteArray.size()];
        for (int i = 0; i < payloadByte.length; i++) {
            mPayload[i] = payloadByte[i];
        }
        return mPayload;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private final Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WPS_NFC_CONFIG_PUSH_SUCCESS :
                if ("VZW".equals(TARGET_OPERATOR)) {
                    Toast.makeText(getActivity(), R.string.sp_wps_nfc_hotspot_info_shared_vzw_NORMAL, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.sp_wps_nfc_hotspot_info_shared_NORMAL, Toast.LENGTH_LONG).show();
                }
                break;
            case WPS_NFC_CONFIG_PUSH_FAIL :
                if ("VZW".equals(TARGET_OPERATOR)) {
                    Toast.makeText(getActivity(), R.string.sp_wps_nfc_hotspot_info_shared_fail_vzw_NORMAL, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.sp_wps_nfc_hotspot_info_shared_fail_NORMAL, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
            }
        }
    };

    private void WPSNfcPushModeOn(NdefMessage message) {
        wpsnfcAdapter.setNdefPushMessage(message, getActivity());
        Log.i(TAG, "WPS: push: " + message);
    }

    private void WPSNfcPushModeOff() {
        wpsnfcAdapter.setNdefPushMessage(null, getActivity());
        Log.d(TAG, "WPS: WPSNfcPushModeOff");
    }
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : Hotspot Password Push

    private void updateState() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        String[] available = cm.getTetherableIfaces();
        String[] tethered = cm.getTetheredIfaces();
        String[] errored = cm.getTetheringErroredIfaces();
        updateState(available, tethered, errored);
    }

    private void updateState(String[] available, String[] tethered,
            String[] errored) {
        updateUsbState(available, tethered, errored);
        updateBluetoothState(available, tethered, errored);
    }

    private void updateUsbState(String[] available, String[] tethered,
            String[] errored) {
        Log.d(TAG, "[AUTORUN] : TetherSettings updateUsbState()");

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean usbAvailable = mUsbConnected && !mMassStorageActive;
        int usbError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        for (String s : available) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        usbError = cm.getLastTetherError(s);
                    }
                }
            }
        }
        boolean usbTethered = false;
        for (String s : tethered) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    usbTethered = true;
                }
            }
        }
        boolean usbErrored = false;
        for (String s : errored) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    usbErrored = true;
                }
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-242][ID-MDM-75]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().setUsbTetheringMenu(mUsbTether)) {
            return;
        }
        // LGMDM_END
        if (usbTethered) {
            mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
            mUsbTether.setEnabled(true);
            mUsbTether.setChecked(true);
        } else if (usbAvailable) {
            if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
            } else {
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            }
            mUsbTether.setEnabled(true);
            mUsbTether.setChecked(false);
        } else if (usbErrored) {
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else if (mMassStorageActive) {
            mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else {
            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);

            if (mDCMAlertDialog != null) {
                mDCMAlertDialog.dismiss();
            }

        }
        // tethering
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Secure.getInt(getContentResolver(),
                    "tethering_blocked", 0) == 1) {
                mUsbTether.setSummary(R.string.tethering_blocked);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
        }
    }

    private void updateBluetoothState(String[] available, String[] tethered,
            String[] errored) {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
            return;
        }
        // LGMDM_END

        boolean bluetoothErrored = false;
        for (String s : errored) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) {
                    bluetoothErrored = true;
                }
            }
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        int btState = adapter.getState();
        if (btState == BluetoothAdapter.STATE_TURNING_OFF) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.wifi_stopping);
        } else if (btState == BluetoothAdapter.STATE_TURNING_ON) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
        } else {
            BluetoothPan bluetoothPan = mBluetoothPan.get();
            if (btState == BluetoothAdapter.STATE_ON && bluetoothPan != null &&
                    bluetoothPan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
                mBluetoothTether.setEnabled(true);
                int bluetoothTethered = bluetoothPan.getConnectedDevices().size();
                if (bluetoothTethered > 1) {
                    String summary = getString(
                            R.string.sp_bluetooth_tethering_devices_connected_subtext,
                            bluetoothTethered);
                    mBluetoothTether.setSummary(summary);
                } else if (bluetoothTethered == 1) {
                    mBluetoothTether.setSummary(
                            R.string.sp_bluetooth_tethering_device_connected_subtext);
                } else if (bluetoothErrored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
                }
            } else {
                mBluetoothTether.setEnabled(true);
                mBluetoothTether.setChecked(false);
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
            }
            // tethering
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                if (Settings.Secure.getInt(getContentResolver(),
                        "tethering_blocked", 0) == 1) {
                    mBluetoothTether.setSummary(R.string.tethering_blocked);
                    mBluetoothTether.setEnabled(false);
                    mBluetoothTether.setChecked(false);
                }
            }
            // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
        }
    }

    //[START][TD#32421] Set mTimeValue to use EntryValues. 2013-05-20, ilyong.oh@lge.com
    // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [S]
    private void updateTimeoutSummary() {
        try { // 20120317, hyeondug.yeo@lge.com, Fix the issue occurred when 'fake language' is on in Hidden menu.
            if (hotSpotTimeoutPreference != null) {
                hotSpotTimeoutPreference.setValue(String.valueOf(Settings.System.getInt(
                        getContentResolver(),
                        SettingsConstants.System.TETHERING_TIME, TETHERING_TIME_VALUE)));
                mIndex = hotSpotTimeoutPreference.findIndexOfValue(String.valueOf(Settings.System
                        .getInt(
                                getContentResolver(),
                                SettingsConstants.System.TETHERING_TIME, TETHERING_TIME_VALUE)));
            }
            if (mIndex == -1) { // 20120317, hyeondug.yeo@lge.com, Fix the issue occurred when 'fake language' is on in Hidden menu.
                mIndex = 0;
            }

            if (mIndex == 4) {
                if (hotSpotTimeoutPreference != null) {
                    hotSpotTimeoutPreference
                            .setSummary(getString(R.string.wifi_hotspot_timeout_never));
                }
            } else {
                if (hotSpotTimeoutPreference != null) {
                    hotSpotTimeoutPreference.setSummary(String.format(
                            getString(R.string.wifi_hotspot_timeout_x_minutes),
                            sTIME_VALUE));
                }
            }            
        } catch (NumberFormatException e) { // 20120317, hyeondug.yeo@lge.com, Fix the issue occurred when 'fake language' is on in Hidden menu.
            Log.e(TAG, "NumberFormatException", e);
        }
    }

    // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [E]
    //[E N D][TD#32421] Set mTimeValue to use EntryValues. 2013-05-20, ilyong.oh@lge.com

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com
    private void validate() {
        if (mAlertDialog != null) { // sunghee78.lee@lge.com 2012.06.27 WBT
                                    // #376359, #376360 Null Pointer Dereference
                                    // fixed
            if (((mPassword != null) && (mPassword.length() < 8))
                    || ((mPassword != null) && (mPassword.getText().toString().trim().length() == 0))) { // sunghee78.lee@lge.com
                                                                                                         // 2012.07.13
                                                                                                         // OFFICIAL_EVENT_Mobile_12
                                                                                                         // TD
                                                                                                         // #120163
                if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) {
                    mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            } else {
                if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) {
                    mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        }
    }

    public void afterTextChanged(Editable s) {
        StringBuffer sb = new StringBuffer();
        sb.append(mPassword.getText().toString());
        int bytelen = sb.toString().getBytes().length;
        validate();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    // JB : Wi-Fi hotspot popup, 120918, hyeondug@lge.com
    public void setInitFalse() {
        mInit = false;
        SharedPreferences pref_ff = getActivity().getSharedPreferences("FIRST_FLAG",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref_ff.edit();
        editor.putBoolean("f_flag", false);
        editor.commit();
    }

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com
    DialogInterface.OnClickListener pwDialogHandler = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE)
            {
                String password = mPassword.getText().toString();
                mWifiConfig = getCupssHotSpotConfigurations(mWifiManager);
                if (mWifiConfig != null) {
                    mWifiConfig.preSharedKey = password;
                }
                mWifiManager.setWifiApConfiguration(mWifiConfig);
                setInitFalse();
                startProvisioningIfNecessary(WIFI_TETHERING);
            }
            else if (button == DialogInterface.BUTTON_NEGATIVE)
            {
                mPassword.setText("");
            }
        }
    };

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com
    DialogInterface.OnClickListener alertDialogHandler = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            // [[ 2013.03.19 jonghyun.seon add battery popup
            mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            int wifiState = mWifiManager.getWifiState();
            String operator_numeric = SystemProperties
                    .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
            // ]] 2013.03.19 jonghyun.seon add battery popup

            if (button == DialogInterface.BUTTON_POSITIVE) {
                startProvisioningIfNecessary(WIFI_TETHERING);
            }
        }
    };

    public boolean onPreferenceChange(Preference preference, Object value) {

        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
        final String key = preference.getKey();
        //[jaewoong87.lee@lge.com] 2013.05.21 NullPointException check
        if (mWifiConfig == null) {
            return false;
        }
        int secureIndex = WifiApDialog.getSecurityTypeIndex(mWifiConfig);

        if (ENABLE_WIFI_AP.equals(key)) {
            boolean enable = (Boolean)value;

                if (enable) {
                    if ("DCM".equals(Config.getOperator())) { // moon-wifi@lge.com by wo0ngs 20121221, for Except Docomo
                        startProvisioningIfNecessary(WIFI_TETHERING);
                    }
                } else {
                    mWifiApEnabler.setSoftapEnabled(false);
                }
            
        }
        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]

        // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [S]
        else if (KEY_HOTSPOT_TIMEOUT.equals(key)) {
            int val = Integer.parseInt((String)value);

            Settings.System.putInt(getContentResolver(), SettingsConstants.System.TETHERING_TIME, val);

            mIndex = hotSpotTimeoutPreference.findIndexOfValue((String)value);
            //[TD#32421] Set mTimeValue to use EntryValues. 2013-05-20, ilyong.oh@lge.com
            sTIME_VALUE = val / CHANGE_TO_MINUTE; //change to minute.
            
            updateTimeoutSummary();
            return true;
        }
        // JB : Wi-Fi hotspot "timeout", 120920, hyeondug.yeo@lge.com [E]
        return false;
    }

    private boolean isProvisioningNeeded(String[] provisionApp) {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)
                || provisionApp == null) {
            return false;
        }
        return (provisionApp.length == 2);
    }

    private void startProvisioningIfNecessary(int choice) {
        mTetherChoice = choice;
        if (isProvisioningNeeded(mProvisionApp)) {
            Log.d(TAG, "[TetherSettings] Make Intent for Entitlement");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(mProvisionApp[0], mProvisionApp[1]);
            intent.putExtra(TETHER_CHOICE, mTetherChoice);
            startActivityForResult(intent, PROVISION_REQUEST);
        } else {
            startTethering();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "[TetherSettings] onActivityResult(), requestCode = " + requestCode
                + "resultCode = " + resultCode);
        if (requestCode == PROVISION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                startTethering();
            } else {
                //BT and USB need checkbox turned off on failure
                //Wifi tethering is never turned on until afterwards
                switch (mTetherChoice) {
                case BLUETOOTH_TETHERING:
                    mBluetoothTether.setChecked(false);
                    break;
                case USB_TETHERING:
                    mUsbTether.setChecked(false);
                    break;
                default:
                    break;
                }
                mTetherChoice = INVALID;
            }
        }
    }

    private void startTethering() {
        switch (mTetherChoice) {
        case WIFI_TETHERING:
            mWifiApEnabler.setSoftapEnabled(true);
            break;
        case BLUETOOTH_TETHERING:
            // turn on Bluetooth first
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
                mBluetoothEnableForTether = true;
                adapter.enable();
                mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
                mBluetoothTether.setEnabled(false);
            } else {
                BluetoothPan bluetoothPan = mBluetoothPan.get();
                if (bluetoothPan != null) {
                    bluetoothPan.setBluetoothTethering(true);
                }
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
            }
            break;
        case USB_TETHERING:
            setUsbTethering(true);
            break;
        default:
            //should not happen
            break;
        }
    }

    private void setUsbTethering(boolean enabled) {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return;
        }

        UsbSettingsControl.setTetherStatus(getActivity(), false);
        if (cm.setUsbTethering(enabled) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
            Log.d(TAG, "[AUTORUN] ============ USB Tethering ERROR !! ============");
            mUsbTether.setChecked(false);
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            return;
        }
        mUsbTether.setSummary("");
        mUsbTether.setEnabled(false);
        UsbSettingsControl.mTetherChanged = true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d(TAG, "[TetherSettings] onPreferenceTreeClick  ");
        if (preference == mUsbTether) {
            boolean newState = mUsbTether.isChecked();

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-242][ID-MDM-75]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setUsbTetheringMenu(mUsbTether)) {
                return true;
            }
            // LGMDM_END
            if (newState) {
                Log.d(TAG, "[TetherSettings] onPreferenceTreeClick newState = " + newState);
                mUsbTether.setChecked(false);
                mUsbTether.setEnabled(false);
                startProvisioningIfNecessary(USB_TETHERING);
            } else {
                Log.d(TAG, "[TetherSettings] onPreferenceTreeClick newState = " + newState);
                mUsbTether.setEnabled(false);
                setUsbTethering(newState);
            }
        } else if (preference == mBluetoothTether) {
            boolean bluetoothTetherState = mBluetoothTether.isChecked();
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
                return true;
            }
            // LGMDM_END

            if (bluetoothTetherState) {
                startProvisioningIfNecessary(BLUETOOTH_TETHERING);
            } else {
                boolean errored = false;

                String[] tethered = cm.getTetheredIfaces();
                String bluetoothIface = findIface(tethered, mBluetoothRegexs);
                if (bluetoothIface != null &&
                        cm.untether(bluetoothIface) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    errored = true;
                }

                BluetoothPan bluetoothPan = mBluetoothPan.get();
                if (bluetoothPan != null) {
                    bluetoothPan.setBluetoothTethering(false);
                }
                if (errored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether
                            .setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                }
            }
        } else if (preference == mTetherHelp) {
            if (mAvailableTetherNum > 1) {
                Preference p = new Preference(getActivity());
                p.setFragment(TetherSettingsHelp.class.getName());
                p.setTitle(R.string.help_label);
                p.setIcon(R.drawable.shortcut_hotspot);
                ((PreferenceActivity)getActivity()).onPreferenceStartFragment(null, p);
            }
            else {
                if (mDefaultTether == WIFI_TETHERING) {
                    // Preference p = new Preference(getActivity());
                    // p.setFragment(com.android.settings.wifi.WifiHotspotHelp01.class.getName());
                    // ((PreferenceActivity)
                    // getActivity()).onPreferenceStartFragment(null, p);
                    Intent mIntent = new Intent();
                    mIntent.setClassName("com.android.settings",
                            "com.android.settings.wifi.WifiHotspotHelp");
                    startActivity(mIntent);
                }
                else if (mDefaultTether == USB_TETHERING) {
                    // Go USB tethering help
                }
                else if (mDefaultTether == BLUETOOTH_TETHERING) {
                    // Go Bluetooth tethering help
                }
            }
            
            return true;
        } else if (preference == mCreateNetwork) {
            showDialog(DIALOG_AP_SETTINGS);
        }
        else if (preference == mEnableUnicode) {
            // seodongjo@lge.com Apply Unicode Menu
            SystemProperties.set("persist.sys.hotssid.ksc5601", mEnableUnicode.isChecked() ? "0"
                    : "1");
        }
        return super.onPreferenceTreeClick(screen, preference);
    }

    private static String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
    private boolean checkFormValid(String str) {
        int startIndex;
        int maxLen;
        maxLen = str.length();

        for (startIndex = 0; startIndex < maxLen; startIndex++) {
            boolean result = Pattern.matches("[\\uAC00-\\uD7A3\\u1100-\\u11F9\\u3131-\\u318E]",
                    str.substring(startIndex));

            if (str.length() > 0 && result) { // TURE
                return true;
            }
        }
        return false;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.show_password) {
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox)view).isChecked() ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                            InputType.TYPE_TEXT_VARIATION_PASSWORD));            
            mPassword.setSelection(mPassword.length());
        }

        else if (view.getId() == R.id.do_not_show_again) {
            if (((CheckBox)view).isChecked()) {
                mDoNotShowAgain = true;
                SharedPreferences pref_ns = getActivity().getSharedPreferences("NOT_SHOW",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref_ns.edit();
                editor.putBoolean("not_show", true);
                editor.commit();
                // JB : Block codes (Plan to apply them in Optimus UI 4.0
                // version), 130109, hyeondug.yeo@lge.com
                // JB : Disable "No" button when "Do not show again" checked.,
                // 130103, hyeondug.yeo@lge.com
                // if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                // != null) {
                // mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                // }
            } else {
                mDoNotShowAgain = false;
                SharedPreferences pref_ns = getActivity().getSharedPreferences("NOT_SHOW",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref_ns.edit();
                editor.putBoolean("not_show", false);
                editor.commit();
                // JB : Block codes (Plan to apply them in Optimus UI 4.0
                // version), 130109, hyeondug.yeo@lge.com
                // JB : Disable "No" button when "Do not show again" checked.,
                // 130103, hyeondug.yeo@lge.com
                // if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                // != null) {
                // mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                // }
            }
        }
    }

    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            mDialog.commitMHPChanges();
            mWifiConfig = mDialog.getAppliedConfig();

            if ((mWifiConfig != null) && (mDialog.isNewConfig())) { // Add Check
                                                                    // condition
                                                                    // seodongjo@lge.com
                /**
                * if soft AP is stopped, bring up
                * else restart with new config
                * TODO: update config on a running access point when framework support is added
                */

                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    mWifiManager.setWifiApEnabled(null, false);
                    mWifiManager.setWifiApEnabled(mWifiConfig, true);
                } else {
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }

                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
                        mWifiConfig.SSID, mSecurityType[index]));
                setInitFalse(); // JB : Wi-Fi hotspot popup, 120918,
                                // hyeondug.yeo@lge.com
                if (checkWPSApSupportedbyProp() == true) {
                    Log.d(TAG, "WPS: mDialog.isNewConfig() -> readyToSendWpsnfc()");
                    readyToSendWpsnfc();
                }
            }

            //[TD#27119] Shoud be set mInit when click save button. 2013-05-14, ilyong.oh@lge.com
            if (mInit) {
                setInitFalse();
            }
        }
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_tether;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
    // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveTetherPolicyChangeIntent(intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }
    };

    // LGMDM_END

    //LG_BTUI : Disable PAN-[s]
    private boolean isSupportBtTether() {
        if (Config.getCountry().equals("JP")) {
            return false;
        }

        if (Config.getCountry().equals("KR")) {
            return false;
        }

        return true;
    }

    //LG_BTUI : Disable PAN-[e]

    private WifiConfiguration getCupssHotSpotConfigurations(WifiManager wifiMgr) {

        TelephonyManager mTelMng = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        WifiConfiguration config =
                CupssHotspotConfigurationMng.getHotSpotConfiguration(wifiMgr, mTelMng,
                        getActivity());

        return config;
    }

    //[START] soonhyuk.choi@lge.com for WPS on the softpap
    public boolean checkWPSApSupportedbyProp() {
        if (SystemProperties.getBoolean("wlan.lge.softapwps", false) == false) {
            Log.d(TAG, "Tethering WPS is false");
            return false;
        } else {
            return true;
        }
    }
    //[END] soonhyuk.choi@lge.com for WPS on the softpap
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC all function enable
    public static boolean checkWPSAllfuncEnablebyProp() {
        if (SystemProperties.getBoolean("wlan.lge.wpsnfcall", false) == false) {
            Log.d(TAG, "WPS_NFC follows LG Scenario");
            return false;
        } else {
            Log.d(TAG, "WPS_NFC enabled all function");
            return true;
        }
    }
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for WPS_NFC all function enable
}
