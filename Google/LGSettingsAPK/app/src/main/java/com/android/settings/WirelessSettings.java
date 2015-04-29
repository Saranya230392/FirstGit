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

package com.android.settings;

import android.net.NetworkInfo;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
//[START] change new nfcadapterIf
//import android.nfc.NfcAdapter;
// [END]
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
//+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable; // rebestm_KK_SMS
import android.app.Activity;
import java.util.ArrayList;
import java.util.Collection; // rebestm_KK_SMS

import com.lge.constants.SettingsConstants;
// [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
//+++ BRCM
import android.bluetooth.BluetoothDevice;
import java.util.List;
//--- BRCM
// [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
//+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.deviceinfo.UsbSettingsControl;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location

//[START] change new nfcadapterIf
//import com.android.settings.nfc.NfcEnabler;
//import com.android.settings.nfc.LGNfcEnabler;
//import com.android.settings.nfc.NfcSwitchPreference;
//[END]
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.NsdEnabler;
import com.android.settings.Utils;
//LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
import com.android.settings.lge.WifiCallCheckBox;
//LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial
//+s LGBT_DCM 20130425 joon1979.kim@lge.com added for DCM BT Tethering Alert [[
import android.view.KeyEvent;
import android.webkit.WebSettings;
import java.util.Locale;
import java.io.InputStream;
import android.content.res.AssetManager;
//+e LGBT_DCM 20130425 ]]
import android.content.res.Resources;
import android.app.AlertDialog.Builder;

// rebestm_KK_SMS
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.android.settings.WirelessStorageSwitchPreference;
import com.android.settings.SmartShareBeamSwitchPreference;

//[START] change new nfcadapterIf
import android.preference.PreferenceGroup;
import com.android.settings.nfc.NfcSettingAdapter;
import com.android.settings.nfc.NfcSettingAdapterIf;
//[END]
public class WirelessSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {


    private static final String TAG = "WirelessSettings";

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    //[START] change new nfcadapterIf
    //private static final String KEY_NFC_SETTINGS = "nfc_settings";
    // [END]
    private static final String KEY_SPRINT_MANAGER = "sprint_manager";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_VPN_SELECTOR = "vpn_selector"; // [iamjm.oh] Add LG VPN UI
    private static final String KEY_WIFI_P2P_SETTINGS = "wifi_p2p_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_USB_TETHER_SETTINGS = "usb_tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private static final String KEY_MOBILE_NETWORK_SETTING_DUALSIM = "mobile_network_settings_dualsim";
    private static final String KEY_TOGGLE_NSD = "toggle_nsd"; //network service discovery
    private static final String KEY_NFS_SETTINGS = "network_storage_settings"; //File networking
    private static final String KEY_MANAGE_MOBILE_PLAN = "manage_mobile_plan"; //rebestm - kk migration
    private static final String KEY_SMARTSHARE = "smart_share_beam"; //rebestm - SmartShareBean porting
    private static final String KEY_NEARBY_DEVICE = "nearby_device";
    private static final String CHECK_PACKAGE_NAME_SMARTSHARE = "com.lge.smartshare";
    private static final String CHECK_LABEL_NAME_DLNA = "DLNA"; //After A1(G2) & exception Main Launcher
    private static final String KEY_MIRACAST_SETTINGS = "wifi_screen_settings";

    private static final int OPTION_ID_DLNA = Menu.FIRST;;
    private static final int SHARTSHARE_VERSIONCODE = 300000;

    //[START] change new nfcadapterIf
    private NfcSettingAdapterIf mNfcSettingAdapterIf;
    //private LGNfcEnabler mNfcEnabler;

    // rebestm - Add SMS Warning popup
    private static final String LGSMS_PACKAGENAME = "com.android.mms";
    //private static final String HANGOUT_PACKAGENAME = "com.google.android.talk";

    //rebestm - kk migration
    private static final int MANAGE_MOBILE_PLAN_DIALOG_ID = 1;
    private static final String SAVED_MANAGE_MOBILE_PLAN_MSG = "mManageMobilePlanMessage";

    private ConnectivityManager mCm;

    // rebestm_KK_SMS
    private static final String KEY_SMS_APPLICATION = "sms_application";
    private AppListPreference mSmsApplicationPreference;


    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private CheckBoxPreference mAirplaneModePreference;
    //[START] change new nfcadapterIf
    //private LGNfcEnabler mNfcEnabler;
    //private NfcAdapter mNfcAdapter;
    //[130405] phj
    //private PreferenceScreen mNfc_setting;
    // [END]
    private NsdEnabler mNsdEnabler;
    private PackageManager mPm;
    private UserManager mUm;

    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private CheckBoxPreference mBluetoothTether;
    private BluetoothPan mBluetoothPan;
    private boolean mBluetoothEnableForTether;
    private String[] mBluetoothRegexs;
    private boolean bluetoothAvailable = false;
    private static final int MHS_REQUEST = 2;
    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
    private boolean mIsProvisioned = false;
    private boolean mPROVISION = "VZW".equals(Config.getOperator()) && bluetoothAvailable
            && !(SystemProperties.get("ro.build.type", null).equals("eng"));
    // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
    // kerry start
    private static final String KEY_DATA_NETWORK_SETTINGS = "data_network_settings";

    private PreferenceScreen mDataNetworkSetting;
    private PreferenceScreen mMobileNetworkSettings;

    //LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
    private static final String BUTTON_WIFI_CALLING_KEY = "button_wifi_calling_key";
    private WifiCallCheckBox mWifiCallingEnabler;
    boolean mIsWifiCallingSupport = false;
    //LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial

    //+s LGBT_DCM 20130425 joon1979.kim@lge.com added for DCM BT Tethering Alert [[
    AlertDialog mTetherAlertDialog;
    public static final String TETHER_BT_URL = "file:///android_asset/html/%y%z/tether_attention_%x.html"; //"file:///android_asset/html/%y%z/bluetooth_dun_attention_%x.html" ;
    public static final String TETHER_BT_PATH = "html/%y%z/tether_attention_%x.html"; //"html/%y%z/bluetooth_dun_attention_%x.html" ;
    //+e LGBT_DCM 20130425 ]]

    private TelephonyManager mTm;
    protected PhoneStateListener mPhoneStateListener; // kerry - for dcm req
    boolean csActive = false;
    // kerry end
    //private TelephonyManager tm;

    //heehoon.lee Local value -> Global value
    private ConnectivityManager btcm;
    //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
    private static Context mContext;
    private static Intent btintent;
    //+e heehoon.lee@lge.com 13.06.12

    //jaewoong87.lee change switchpreference to preference for UI 4.2 scenario
    private PreferenceScreen mMiracast;
    boolean isSecondaryUser;
    // rebestm - ssb
    private SmartShareBeamSwitchPreference mSmartBeam;

    // rebestm - wireless storage switch
    private WirelessStorageSwitchPreference mWirelessStorage;

    private Preference nearby_device;

    //[youngju.do]PCSuiteSetting_v4.1
    private Preference mPCSuiteSettings;
    private static final String KEY_PCSUITE_SETTINGS = "pcsuite_settings";
    private static final String PACKAGE_NAME_PCSUITE = "com.lge.sync";
    private static final String PACKAGE_NAME_PCSUITEUI = "com.lge.pcsyncui";
    private static final String CLASS_NAME_PCSUITEUI = "com.lge.pcsyncui.settings.PCSuiteSettingsActivity";
    private static final String INTENT_USB_CONNECT_STATUS = "usb_connect_status";

    private IntentFilter mFilterSSB = null;
    private BeamStatusReceiver mBeamReceiver = new BeamStatusReceiver();

    public class BeamStatusReceiver extends BroadcastReceiver {

        public static final int BEAM_STATUS_ON = 1;
        public static final int BEAM_STATUS_OFF = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "broadcast received action : " + action);
            if (action.equals("smartsharebeam.status")) {
                int status = intent.getIntExtra("smartsharebeam.status.key", BEAM_STATUS_OFF);
                mSmartBeam.setEnabled(true);
                if (status == BEAM_STATUS_ON) {
                    Log.d(TAG, "SmartShare Beam On.");
                    mSmartBeam.setState(true);
                } else {
                    Log.d(TAG, "SmartShare Beam Off.");
                    mSmartBeam.setState(false);
                }
            }
        }
    }
    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (KEY_MIRACAST_SETTINGS.equals(preference.getKey())) {
            Intent intent = new Intent();
            intent.setAction("android.settings.WIFI_SCREEN_SHARE");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;

        } else

        //rebestm - SmartShareBean porting
        if (KEY_SMARTSHARE.equals(preference.getKey())) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().checkDisallowSmartShareBeam(getActivity(),
                        true)) {
                    return super.onPreferenceTreeClick(preferenceScreen, preference);
                }
            }
            // LGMDM_END
            Log.d(TAG, "smart_share_beam");

            // yonguk.kim 2014-03-06 Apply remote fragment feature [START]
            if (Utils.supportRemoteFragment(getActivity()) == true) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                boolean result = RemoteFragmentManager.setPreferenceForRemoteFragment(
                        "itectokyo.wiflus.service", preference);
                if (result == true) {
                    preferenceActivity.onPreferenceStartFragment(this, preference);
                    return true;
                }
            }
            // yonguk.kim 2014-03-06 Apply remote fragment feature [END]

            Intent intent = new Intent("com.lge.smartsharebeam.setting");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        else if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode launch ECM app dialog
            startActivityForResult(
                    new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                    REQUEST_CODE_EXIT_ECM);
            return true;
            //rebestm - kk migration
        } else if (preference == findPreference(KEY_MANAGE_MOBILE_PLAN)) {
            onManageMobilePlanClick();
        }
        else if (preference == mAirplaneModePreference && true == csActive) {
            //Toast.makeText(this, R.string.sp_warning_not_available_during_conversation_NORMAL, Toast.LENGTH_SHORT).show();
            //            Log.d("WirelessSettings", "onPreferenceTreeClick222");
            mAirplaneModeEnabler.updatePreferences();
            return true;
        }
        else if (preference == mDataNetworkSetting) { // kerry SKT SPEC - Data Network Settings [S]
            Intent mIntent = new Intent();
            mIntent.setClassName("com.android.settings",
                    "com.android.settings.lgesetting.wireless.DataNetworkModeSetting");
            mIntent.putExtra("cancelable", "true");
            startActivity(mIntent);
            // kerry SKT SPEC - Data Network Settings [E]
            //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        } else if (preference == findPreference(KEY_NFS_SETTINGS)) {
            if (appIsEnabled()) {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            } else {
                confirmDialog();
            }
        } else if (preference == mBluetoothTether) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
                return true;
            }
            // LGMDM_END
            //ConnectivityManager cm =
            //        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean bluetoothTetherState = mBluetoothTether.isChecked();

            // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
            if (mPROVISION && (bluetoothTetherState == true)) {
                checkProvision();
            } else {
                gotoNextStep(bluetoothTetherState);
            }

            // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
            //LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
        } else if (preference == mWifiCallingEnabler) {
            Intent mIntent = new Intent();
            mIntent.setClassName("com.movial.wificall", "com.movial.wificall.Settings");
            startActivity(mIntent);
            //LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial
        }
        //[START] change new nfcadapterIf    
        //[130405] phj
        //else if (preference == mNfc_setting
        //        && (TelephonyManager.SIM_STATE_UNKNOWN == TelephonyManager.getDefault()
        //                .getSimState())) {
        //    // In ECM mode launch ECM app dialog
        //    Log.d(TAG, "onPreferenceTreeClick - nfc Settings.");
        //    return true;
        //} 
        else if (KEY_NEARBY_DEVICE.equals(preference.getKey())) {
            Intent intent = new Intent();
            intent.setAction("com.lge.smartshare.dlna.action.launcher");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        } else if (KEY_MOBILE_NETWORK_SETTINGS.equals(preference.getKey())) {
            if (Config.getOperator().equals("DCM")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
            }
        }
        //[START] change new nfcadapterIf
        else {
            if (mNfcSettingAdapterIf != null) {
                boolean result = mNfcSettingAdapterIf.processPreferenceEvent(preference.getKey());
                if (result == false) {
                    return true;
                }
            }
        }
        //[END]
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    //rebestm - kk migration
    private String mManageMobilePlanMessage;
    private static final String CONNECTED_TO_PROVISIONING_NETWORK_ACTION = "com.android.server.connectivityservice.CONNECTED_TO_PROVISIONING_NETWORK_ACTION";

    public void onManageMobilePlanClick() {
        Log.d(TAG, "onManageMobilePlanClick:");
        mManageMobilePlanMessage = null;
        Resources resources = getActivity().getResources();
        NetworkInfo ni = mCm.getProvisioningOrActiveNetworkInfo();
        if (mTm.hasIccCard() && (ni != null)) {
            // Get provisioning URL
            String url = mCm.getMobileProvisioningUrl();
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent(CONNECTED_TO_PROVISIONING_NETWORK_ACTION);
                intent.putExtra("EXTRA_URL", url);
                Context context = getActivity().getBaseContext();
                context.sendBroadcast(intent);
                mManageMobilePlanMessage = null;
            } else {
                // No provisioning URL
                String operatorName = mTm.getSimOperatorName();
                if (TextUtils.isEmpty(operatorName)) {
                    // Use NetworkOperatorName as second choice in case there is no
                    // SPN (Service Provider Name on the SIM). Such as with T-mobile.
                    operatorName = mTm.getNetworkOperatorName();
                    if (TextUtils.isEmpty(operatorName)) {
                        mManageMobilePlanMessage = resources.getString(
                                R.string.mobile_unknown_sim_operator);
                    } else {
                        mManageMobilePlanMessage = resources.getString(
                                R.string.mobile_no_provisioning_url, operatorName);
                    }
                } else {
                    mManageMobilePlanMessage = resources.getString(
                            R.string.mobile_no_provisioning_url, operatorName);
                }
            }
        } else if (mTm.hasIccCard() == false) {
            // No sim card
            mManageMobilePlanMessage = resources.getString(R.string.mobile_insert_sim_card);
        } else {
            // NetworkInfo is null, there is no connection
            mManageMobilePlanMessage = resources.getString(R.string.mobile_connect_to_internet);
        }
        if (!TextUtils.isEmpty(mManageMobilePlanMessage)) {
            Log.d(TAG, "onManageMobilePlanClick: message=" + mManageMobilePlanMessage);
            showDialog(MANAGE_MOBILE_PLAN_DIALOG_ID);
        }
    }

    private void updateSmsApplicationSetting() {
        Log.d(TAG, "updateSmsApplicationSetting:");
        ComponentName appName = SmsApplication.getDefaultSmsApplication(getActivity(), true);
        if (appName != null) {
            String packageName = appName.getPackageName();

            CharSequence[] values = mSmsApplicationPreference.getEntryValues();
            for (int i = 0; i < values.length; i++) {
                if (packageName.contentEquals(values[i])) {
                    mSmsApplicationPreference.setValueIndex(i);
                    mSmsApplicationPreference.setSummary(mSmsApplicationPreference.getEntries()[i]);
                    break;
                }
            }
        }
    }

    private void initSmsApplicationSetting() {
        Log.d(TAG, "initSmsApplicationSetting:");
        Collection<SmsApplicationData> smsApplications =
                SmsApplication.getApplicationCollection(getActivity());

        // If the list is empty the dialog will be empty, but we will not crash.
        int count = smsApplications.size();
        String[] packageNames = new String[count];
        int i = 0;
        for (SmsApplicationData smsApplicationData : smsApplications) {
            packageNames[i] = smsApplicationData.mPackageName;
            i++;
        }
        String defaultPackageName = null;
        ComponentName appName = SmsApplication.getDefaultSmsApplication(getActivity(), true);
        if (appName != null) {
            defaultPackageName = appName.getPackageName();
        }
        mSmsApplicationPreference.setPackageNames(packageNames, defaultPackageName);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        Log.d(TAG, "onCreateDialog: dialogId=" + dialogId);
        switch (dialogId) {
        case MANAGE_MOBILE_PLAN_DIALOG_ID:
            return new AlertDialog.Builder(getActivity())
                    .setMessage(mManageMobilePlanMessage)
                    .setCancelable(false)
                    .setPositiveButton(com.android.internal.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.d(TAG, "MANAGE_MOBILE_PLAN_DIALOG.onClickListener id=" + id);
                                    mManageMobilePlanMessage = null;
                                }
                            })
                    .create();
        default:
            break;
        }
        return super.onCreateDialog(dialogId);
    }

    public static boolean isRadioAllowed(Context context, String type) {
        if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
            return true;
        }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.Global.getString(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }

    // rebestm_KK_SMS
    private boolean isSmsSupported() {
        // Some tablet has sim card but could not do telephony operations. Skip those.
        Log.d(TAG, "mTm.isSmsCapable() = " + mTm.isSmsCapable());
        return mTm.isSmsCapable();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //rebestm - kk migration
        mCm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (savedInstanceState != null) {
            mManageMobilePlanMessage = savedInstanceState.getString(SAVED_MANAGE_MOBILE_PLAN_MSG);
        }

        Log.d(TAG, "onCreate: mManageMobilePlanMessage=" + mManageMobilePlanMessage);

        mTm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mPm = getPackageManager();
        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        int nSettingStyle = (Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SETTING_STYLE, 0) == 1 &&
                Utils.supportEasySettings(getActivity().getApplicationContext())) ? 1 : 0;
        addPreferencesFromResource(R.xml.wireless_settings);
        //boolean mtk_dual = SystemProperties.getBoolean("ro.lge.mtk_dualsim", false);
        isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
        final Activity activity = getActivity();
        //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
        mContext = activity.getApplicationContext();
        //+e heehoon.lee@lge.com 13.06.12
        setHasOptionsMenu(true); // rebestm - Use DLNA feature

        if (Utils.supportEasySettings((Context)activity)) {
            activity.getActionBar().setIcon(R.drawable.shortcut_networks_setting);
            activity.getActionBar().setTitle(R.string.sp_connectivity_NORMAL);
        }
        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter != null && !Utils.isTablet()) { //rebestm
            adapter.getProfileProxy(activity.getApplicationContext(), mProfileServiceListener,
                    BluetoothProfile.PAN);
        }
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        updateAirplaneModeState(isAirplaneMode == 1 ? true : false);
        mAirplaneModePreference = (CheckBoxPreference)findPreference(KEY_TOGGLE_AIRPLANE);
        //[START] change new nfcadapterIf
        //Preference nfc = (Preference)findPreference(KEY_TOGGLE_NFC);
        //[END]
        mWirelessStorage = (WirelessStorageSwitchPreference)findPreference(KEY_NFS_SETTINGS);

        //NFS S : mihan.kim File networking menu is enabled when CAPP_NFS config is enabled.
        if (!Utils.isEnableCheckPackage(getActivity(), "com.lge.wireless_storage")
                || isSecondaryUser == true) {
            if (mWirelessStorage != null) {
                getPreferenceScreen().removePreference(mWirelessStorage);
                mWirelessStorage = null;
            }
        }
        //NFS E
        CheckBoxPreference nsd = (CheckBoxPreference)findPreference(KEY_TOGGLE_NSD);

        PreferenceScreen wdPref = (PreferenceScreen)findPreference(KEY_WIFI_P2P_SETTINGS);
        getPreferenceScreen().removePreference(findPreference(KEY_WIFI_P2P_SETTINGS));

        nearby_device = (Preference)findPreference(KEY_NEARBY_DEVICE);

        if (nearby_device != null) {
            if (!isNearbyMenu(getActivity())) {
                getPreferenceScreen().removePreference(nearby_device);
            } else {
                if (isEnableCheckPackage(getActivity(), CHECK_PACKAGE_NAME_SMARTSHARE)) {
                    nearby_device.setEnabled(true);
                } else {
                    nearby_device.setEnabled(false);
                }
            }
        }

        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        mBluetoothTether = (CheckBoxPreference)findPreference(ENABLE_BLUETOOTH_TETHERING);
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

        // rebestm - ssb
        mSmartBeam = (SmartShareBeamSwitchPreference)findPreference(KEY_SMARTSHARE);

        if (Config.getFWConfigBool(mContext,
                com.lge.config.ConfigBuildFlags.CAPP_WFDS_ASP,
                "com.lge.config.ConfigBuildFlags.CAPP_WFDS_ASP") == false) {
            if (mSmartBeam != null) {
                getPreferenceScreen().removePreference(mSmartBeam);
                mSmartBeam = null;
            }
        }

        if (Config.supportAirplaneListMenu()) {
            getPreferenceScreen().removePreference(findPreference(KEY_TOGGLE_AIRPLANE));
        } else {
            mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference, wdPref);
        }
        //[youngju.do]PCSuiteSetting_v4.1
        mPCSuiteSettings = (Preference)findPreference(KEY_PCSUITE_SETTINGS);

        if (null != mPCSuiteSettings) {
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                mPCSuiteSettings.setTitle(R.string.lg_bridge_title);
                mPCSuiteSettings.setSummary(R.string.sp_pcdavid_list_desc);
            }

            if (!isPCSuiteUISupportSettingMenu(getActivity())) {
                getPreferenceScreen().removePreference(mPCSuiteSettings);
            } else {
                mPCSuiteSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (Utils.supportRemoteFragment(getActivity()) == true) {
                            String fname = RemoteFragmentManager
                                    .getFragmentName(PACKAGE_NAME_PCSUITEUI);
                            if (fname != null) {
                                preference.setFragment(fname);
                                if (preference.getTitleRes() != 0) {
                                    int resId = preference.getTitleRes();
                                    preference.setTitle("wow");
                                    preference.setTitle(getResources().getString(resId));
                                }

                                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                                preferenceActivity.onPreferenceStartFragment(WirelessSettings.this,
                                        preference);
                                return true;
                            }
                        }

                        Intent intent = new Intent();
                        intent.setPackage(PACKAGE_NAME_PCSUITEUI);
                        intent.setClassName(PACKAGE_NAME_PCSUITEUI, CLASS_NAME_PCSUITEUI);
                        intent.addCategory("com.lge.pcsyncui.category");
                        intent.putExtra(INTENT_USB_CONNECT_STATUS,
                                UsbSettingsControl.getUsbConnected(getActivity()));
                        startActivity(intent);
                        return true;
                    }
                });
            }
        }

        //[START] change new nfcadapterIf
        mNfcSettingAdapterIf = NfcSettingAdapter.getInstance((Context)activity, (PreferenceGroup)this.getPreferenceScreen());

        if (mNfcSettingAdapterIf == null) {
            Log.d(TAG, "This device doesn't support NFC");
        }
        /*
            // LGE_S [NFC] - sy.yoon.2012.01.26 J1 - to support I30 UI
            mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (mNfcAdapter == null) {
                Log.d(TAG, "This device doesn't support NFC");
                getPreferenceScreen().removePreference(nfc);
                getPreferenceScreen().removePreference(androidBeam);
                getPreferenceScreen().removePreference(nfc_settings);
                getPreferenceScreen().removePreference(sprint_manager);
                mNfcEnabler = null;
            } else {
                if (Utils.hasFeatureNfcLock()) {
                    getPreferenceScreen().removePreference(nfc);
                    getPreferenceScreen().removePreference(androidBeam);
                    //[130405] phj
                    mNfc_setting = (PreferenceScreen)findPreference(KEY_NFC_SETTINGS);
                    if (Utils.hasFeatureNfcLockForKDDI()) {
                        mNfc_setting.setSummary(R.string.nfc_preference_summary_main);
                    } else {
                        mNfc_setting.setTitle(R.string.nfc_preference_title_main_dcm);
                        mNfc_setting.setSummary(R.string.nfc_preference_summary_main_dcm);
                    }
                    mNfcEnabler = new LGNfcEnabler(activity, mNfc_setting);
                    Log.d(TAG, "hasFeatureNfcLock");
                } else {
                    if (Utils.isWifiOnly(getActivity())) { // A070 - rebestm
                        getPreferenceScreen().removePreference(androidBeam);
                        getPreferenceScreen().removePreference(nfc_settings);
                        mNfcEnabler = new LGNfcEnabler(activity, (NfcSwitchPreference)nfc);
                    } else {
                        getPreferenceScreen().removePreference(nfc_settings);
                        mNfcEnabler = new LGNfcEnabler(activity, (NfcSwitchPreference)nfc,
                                (NfcSwitchPreference)androidBeam);
                    }
                }

                if (false == Utils.hasSprintTouchV2(getActivity())) {
                    Log.d(TAG, "doesn't have sprint touch v2 or up version.");
                    getPreferenceScreen().removePreference(sprint_manager);
                }
            }
            */
      //[END] 
        mSmsApplicationPreference = (AppListPreference)findPreference(KEY_SMS_APPLICATION);
        mSmsApplicationPreference.setOnPreferenceChangeListener(this);
        initSmsApplicationSetting();

        // Remove NSD checkbox by default
        if (nsd != null) {
            getPreferenceScreen().removePreference(nsd);
            //mNsdEnabler = new NsdEnabler(activity, nsd);
        }

        //String toggleable = Settings.Global.getString(activity.getContentResolver(),
        //        Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        //[s][chris.won][lgu_xml_merge][20121206]
        if ("LGU".equals(Config.getOperator())) {
            getPreferenceScreen().findPreference(KEY_VPN_SETTINGS).setTitle(
                    R.string.sp_vpn_settings_title_NORMAL);
        }
        //[e][chris.won][lgu_xml_merge][20121206]

        //enable/disable wimax depending on the value in config.xml
        boolean config_wimax = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_wimaxEnabled, 
            "com.android.internal.R.bool.config_wimaxEnabled");
        final boolean isWimaxEnabled = !isSecondaryUser && config_wimax;
        if (!isWimaxEnabled
                || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            PreferenceScreen root = getPreferenceScreen();
            Preference ps = (Preference)findPreference(KEY_WIMAX_SETTINGS);
            if (ps != null) {
                root.removePreference(ps);
            }
        }
        /*
                else {
                    if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIMAX )
                            && isWimaxEnabled) {
                        Preference ps = (Preference) findPreference(KEY_WIMAX_SETTINGS);
                        ps.setDependency(KEY_TOGGLE_AIRPLANE);
                    }
                }

                // Manually set dependencies for Wifi when not toggleable.
                if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
                    findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
                }

                // Manually set dependencies for Bluetooth when not toggleable.
                if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
                    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
                    mBluetoothTether.setDependency(KEY_TOGGLE_AIRPLANE);
                    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
                }

                // Manually set dependencies for NFC when not toggleable.
                   if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_NFC)) {
                       nfc.setDependency(KEY_TOGGLE_AIRPLANE);
                       androidBeam.setDependency(KEY_TOGGLE_AIRPLANE);
                   }
                   */
        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        //[LGE_UPDATE_S] 20120627, sangyeol.lee@lge.com, [START]
        String ispan = SystemProperties.get("bluetooth.pan", "false");
        if ("true".equals(ispan)) {
            bluetoothAvailable = true;
        }
        Log.d("Settings", "[wirelessSettings] bluetoothAvailable=" + bluetoothAvailable);
        //20130810 bh3.lee@lge.com for move bluetooth tethering menu
        if ((mBluetoothTether != null && !bluetoothAvailable) || Utils.isWifiOnly(getActivity())) {
            getPreferenceScreen().removePreference(mBluetoothTether);
        } else {
            //+s heehoon.lee@lge.com 13.05.27 removed for TD30459 BT Tethering checkbox issue
            /*
                        if (mBluetoothPan != null && mBluetoothPan.isTetheringOn()) {
                            if (mBluetoothTether != null)
                                mBluetoothTether.setChecked(true);
                        } else {
                            if (mBluetoothTether != null)
                                mBluetoothTether.setChecked(false);
                        }
            */
            //+e heehoon.lee@lge.com 13.05.27
        }
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING


        // Remove Mobile Network Settings and Manage Mobile Plan for secondary users,
        // if it's a wifi-only device, or if the settings are restricted.
        if (isSecondaryUser || Utils.isWifiOnly(getActivity())
                || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTINGS));
            getPreferenceScreen().removePreference(
                    findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM));
        }
        Log.d(TAG, "isSecondaryUser=" + isSecondaryUser);
        // Disable VPN.
        if (isSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)) {
            removePreference(KEY_VPN_SETTINGS);
            removePreference(KEY_VPN_SELECTOR);
        }

        //[andrew75.kim@lge.com]_S 2012-06-15 Add Wi-Fi Screen Share
        if (!getPackageManager().hasSystemFeature("com.lge.software.wfdService"))
        {
            getPreferenceScreen().removePreference(findPreference("wifi_screen_settings"));
        }
        else {
            mMiracast = (PreferenceScreen)findPreference("wifi_screen_settings"); //Miracast swtich
        }
        //[andrew75.kim@lge.com]_E 2012-06-15 Add Wi-Fi Screen Share

        // kerry SKT start
        mDataNetworkSetting = (PreferenceScreen)findPreference(KEY_DATA_NETWORK_SETTINGS);
        //boolean CARRIER_KT = "KT".equals(Config.getOperator());
        boolean CARRIER_SKT = "SKT".equals(Config.getOperator());
        //boolean SKT_KT = CARRIER_KT && CARRIER_SKT;

        if (false == CARRIER_SKT) {
            getPreferenceScreen().removePreference(mDataNetworkSetting);
        }
        else {
            if (Config.isDataRoaming()
                    || (Utils.hasReadyMobileRadio(getActivity()) == false)) {
                mDataNetworkSetting.setEnabled(false);
            } else {
                mDataNetworkSetting.setEnabled(true);
            }
            mDataNetworkSetting.setSummary(R.string.sp_data_network_summary_skt_NORMAL);
        }
        // kerry SKT end


        if (true) { // Lg Scenario
            getPreferenceScreen().removePreference(findPreference(KEY_MANAGE_MOBILE_PLAN));
        }


        // rebestm_KK_SMS
        // Remove SMS Application if the device does not support SMS
        if ((isSmsSupported() == false)
                || (Utils.isUI_4_1_model(getActivity())
                && !"VZW".equals(Config.getOperator()))) {
            removePreference(KEY_SMS_APPLICATION);
        }
        // rebestm - kk migration
        // Remove Airplane Mode settings if it's a stationary device such as a TV.
        if (mPm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
            removePreference(KEY_TOGGLE_AIRPLANE);
        }

        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
        final DevicePolicyManager mDPM = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support
        getPreferenceScreen().removePreference(mGlobalProxy);
        mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);

        // Disable Tethering if it's not allowed or if it's a wifi-only device
        //heehoon.lee@lge.com Local value -> Global value
        btcm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        mBluetoothRegexs = btcm.getTetherableBluetoothRegexs();
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

            Preference pf = (Preference)findPreference(KEY_USB_TETHER_SETTINGS);
            if (pf != null) {
                getPreferenceScreen().removePreference(pf);
            }
        Log.d("settings", "[wirelessSettings] isSecondaryUser = " + isSecondaryUser);
        Log.d("settings",
                "[wirelessSettings] btcm.isTetheringSupported() = " + btcm.isTetheringSupported());


        if (isSecondaryUser || !btcm.isTetheringSupported()
                || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
        } else {

            //[Wi-Fi Settings]_S [andrew75.kim@lge.com] 20120305 for MobileHotspot VZW
            if ("VZW".equals(Config.getOperator()) || "TRF".equals(Config.getOperator())) {
                Log.d("WirelessSettings", "remove Preference()");

                if (findPreference(KEY_SPRINT_MANAGER) != null) {
                    getPreferenceScreen().removePreference(findPreference(KEY_SPRINT_MANAGER));
                }
                if (findPreference(KEY_TETHER_SETTINGS) != null) {
                    getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
                }
            } else if (true == "DCM".equals(Config.getOperator())) { //[Wi-Fi Settings]_S [andrew75.kim@lge.com] 2013-03-14 Add the Wi-Fi tethering setting for Docomo.
                Preference p = findPreference(KEY_TETHER_SETTINGS);
                if (p != null) {
                    p.setTitle(R.string.sp_tethering_title_jp_NORMAL);
                    p.setSummary(R.string.sp_tether_summary_DCM_NORMAL);
                }
            } else if (true == "LGU".equals(Config.getOperator())) {
                Preference p = findPreference(KEY_TETHER_SETTINGS);
                if (p != null) {
                    p.setTitle(R.string.sp_wifi_hotspot_NORMAL); // Restore 20121217 seodongjo@lge.com  Portable hotspot ==> Portable Wi-Fi hotspot
                    p.setSummary(R.string.sp_wifi_tether_summary_NORMAL);
                }
            } else if (true == "ATT".equals(Config.getOperator())) {
                Preference p = findPreference(KEY_TETHER_SETTINGS);

                if (p != null) {
                    p.setTitle(R.string.sp_mobile_hotspot_NORMAL);
                }
            } else {
                Preference p = findPreference(KEY_TETHER_SETTINGS);
                if (p != null) {
                    p.setTitle(R.string.sp_wifi_hotspot_NORMAL); // Restore 20121023 seodongjo@lge.com // JB : Portable Wi-Fi hotspot ==> Portable hotspot, 120924, hyeondug.yeo@lge.com
                }
            }
        }

        // kerry start for VZW
        if ("VZW".equals(Config.getOperator()) && !Utils.isTablet()) {
            mMobileNetworkSettings = (PreferenceScreen)getPreferenceScreen().findPreference(
                    KEY_MOBILE_NETWORK_SETTINGS);
            if (SystemProperties.getInt("ro.telephony.default_network", 0) == 4) {
                mMobileNetworkSettings.setSummary(R.string.sp_mobile_network_contens_NORMAL);
            } else {
                mMobileNetworkSettings.setSummary(R.string.sp_network_settings_summary_NORMAL);
            }
        }

        if (0 == isAirplaneMode && nSettingStyle == 0) {
            if (Utils.isWifiOnly(getActivity()) && !Utils.isTablet()) {
                mAirplaneModePreference.setSummary(R.string.sp_airplane_mode_summary_NORMAL_jb_plus_wifi_only);
            } else {
                mAirplaneModePreference.setSummary(R.string.sp_airplane_mode_summary_NORMAL_jb_plus);
            }
        }
        if (null != wdPref) {
            wdPref.setSummary(R.string.sp_wifi_direct_summary_NORMAL);
        }
        // kerry end

        // kerry - for qm req start
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state != TelephonyManager.CALL_STATE_IDLE) {
                    csActive = true;
                } else {
                    csActive = false;
                }
            }

            @Override
            public void onDataActivity(int direction) {
            }
        };

        // kerry - for qm req end

        /* [S][2012.2.17] kyochun.shin@lge.com mobileSetting for Dual SIM or Single SIM*/
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (!Utils.isWifiOnly(getActivity())) {
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTINGS));
            } else {
                getPreferenceScreen().removePreference(
                        findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM));
            }
        }
        /* [E][2012.2.17] kyochun.shin@lge.com mobileSetting for Dual SIM or Single SIM*/

        //LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
        mWifiCallingEnabler = (WifiCallCheckBox)getPreferenceScreen().findPreference(
                BUTTON_WIFI_CALLING_KEY);
        mIsWifiCallingSupport = WifiCallCheckBox.isSupport();
        if (!mIsWifiCallingSupport) {
            if (mWifiCallingEnabler != null) {
                getPreferenceScreen().removePreference(mWifiCallingEnabler);
                mWifiCallingEnabler = null;
            }
        }
        //LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial

        // [iamjm.oh] Add LG VPN UI [START]
        boolean isLGEVPN = Utils.isSupportVPN(getActivity());
        Log.d(TAG, "[onCreate] isLGEVPN" + isLGEVPN);
        if (isLGEVPN) {
            removePreference(KEY_VPN_SETTINGS);
        } else {
            removePreference(KEY_VPN_SELECTOR);
        }
        // [iamjm.oh] Add LG VPN UI [END]

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-223][ID-MDM-42]
        // [ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-47][ID-MDM-195][ID-MDM-75][ID-MDM-225]
        // [ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-274][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWirelessSettingsPolicyChangeIntentFilter(
                    filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
            MDMSettingsAdapter.getInstance().setVpnMenu(
                    getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
            MDMSettingsAdapter.getInstance().setVpnMenu(
                    getPreferenceScreen().findPreference(KEY_VPN_SELECTOR));
        }
        // LGMDM_END

        mFilterSSB = new IntentFilter();
        mFilterSSB.addAction("smartsharebeam.status");
    }

    // rebestm_KK_SMS
    public void onStart() {
        super.onStart();
        initSmsApplicationSetting();
    }

    @Override
    public void onResume() {
        super.onResume();

        //ask130402 :: +
        Utils.set_TelephonyListener(mTm, mPhoneStateListener);
        //ask130402 :: -

        //[S][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        //[E][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        Activity activity = getActivity();
        updateAirplaneModeState(isAirplaneMode == 1 ? true : false); //[chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        activity.registerReceiver(mAirplaneModeReceiver, new IntentFilter(
                Intent.ACTION_AIRPLANE_MODE_CHANGED));
        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
        //+++ BRCM
        filter.addAction(BluetoothPan.ACTION_CONNECTION_STATE_CHANGED);
        //--- BRCM
        // [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
        filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
        if (mPROVISION)
        {
            filter.addAction("com.lge.hotspotprovision.STATE_CHANGED");
        }
        // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
        activity.registerReceiver(mBTTetheringReceiver, filter);
        if (bluetoothAvailable) {
            updateState();
        }
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        if (mTm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            csActive = true;
        } else {
            csActive = false;
        } // kerry

        if (mAirplaneModeEnabler != null) {
            mAirplaneModeEnabler.resume();
        }
        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.resume();
        }
       
        //if (mNfcEnabler != null) {
        //    mNfcEnabler.resume();
        //}
        //[END]
        if (mNsdEnabler != null) {
            mNsdEnabler.resume();
        }

        if (mSmartBeam != null) {
            mSmartBeam.resume();
        }
        if (mWirelessStorage != null) {
            mWirelessStorage.resume();
        }
        getActivity().registerReceiver(mBeamReceiver, mFilterSSB);

        //tm = TelephonyManager.getDefault();
        if ("SKT".equals(Config.getOperator())) {
            if (Config.isDataRoaming()
                    || (Utils.hasReadyMobileRadio(getActivity()) == false)) {
                mDataNetworkSetting.setEnabled(false);
            } else {
                if ((isAirplaneMode == 1)
                        || (Utils.hasReadyMobileRadio(getActivity()) == false)) {
                    mDataNetworkSetting.setEnabled(false);
                } else {
                    mDataNetworkSetting.setEnabled(true);
                }
            }
        }
        if (!Utils.isWifiOnly(getActivity())) {
            if ((isAirplaneMode == 1) || (Utils.hasReadyMobileRadio(getActivity()) == false)) {
                getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
            } else {
                getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
            }
        }

        //LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
        if (mIsWifiCallingSupport && mWifiCallingEnabler != null) {
            mWifiCallingEnabler.resume();
        }
        //LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial

        activity.getApplicationContext().getContentResolver().registerContentObserver(
                Telephony.Carriers.CONTENT_URI,
                true, mApnChangeObserver);

        if (true == Config.isVZWAdminDisabled(getActivity().getApplicationContext())) {
            mAirplaneModePreference.setEnabled(false);
        }

        //rebestm - resume exception
        getActivity().invalidateOptionsMenu();

        if (Config.getFWConfigBool(mContext,
                com.lge.config.ConfigBuildFlags.CAPP_WFDS_ASP,
                "com.lge.config.ConfigBuildFlags.CAPP_WFDS_ASP") == false) {
            if (mSmartBeam != null) {
                getPreferenceScreen().removePreference(mSmartBeam);
                mSmartBeam = null;
            }
        }

        if (nearby_device != null) {
            if (isEnableCheckPackage(getActivity(), CHECK_PACKAGE_NAME_SMARTSHARE)) {
                nearby_device.setEnabled(true);
            } else {
                nearby_device.setEnabled(false);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167][ID-MDM-274]
        //                           [ID-MDM-47][ID-MDM-277][ID-MDM-195]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setMobileNetworkMenu(mDataNetworkSetting);
            MDMSettingsAdapter.getInstance().setMiracastPreference(mMiracast);
            MDMSettingsAdapter.getInstance().setSmartShareBeamMenu(mSmartBeam);
            if (!isSecondaryUser) {
                com.android.settings.MDMSettingsAdapter.getInstance().setWirelessStorageMenu(
                        findPreference(KEY_NFS_SETTINGS));
            }
        }
        // LGMDM_END
    }

    // rebestm - kk migration
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(mManageMobilePlanMessage)) {
            outState.putString(SAVED_MANAGE_MOBILE_PLAN_MSG, mManageMobilePlanMessage);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //ask130402 :: +
        Utils.release_TelephonyListener(mTm, mPhoneStateListener);
        //ask130402 :: -

        if (mAirplaneModeEnabler != null) {
            mAirplaneModeEnabler.pause();
        }

        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.pause();
        }

        //if (mNfcEnabler != null) {
        //    mNfcEnabler.pause();
        //}
        // [END]
        if (mNsdEnabler != null) {
            mNsdEnabler.pause();
        }

        if (mSmartBeam != null) {
            mSmartBeam.pause();
        }
        if (mWirelessStorage != null) {
            mWirelessStorage.pause();
        }
        getActivity().unregisterReceiver(mBeamReceiver);
        Activity activity = getActivity();
        activity.unregisterReceiver(mAirplaneModeReceiver);
        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        activity.unregisterReceiver(mBTTetheringReceiver);
        // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
        if (mPROVISION) {
            mIsProvisioned = false;
        }
        // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

        activity.getApplicationContext().getContentResolver().unregisterContentObserver(
                mApnChangeObserver);
        //LGE_S 2012-04-03 TMUS WiFi Calling - Initial release from Movial
        if (mIsWifiCallingSupport && mWifiCallingEnabler != null) {
            mWifiCallingEnabler.pause();
        }
        //LGE_E 2012-04-03 TMUS WiFi Calling - Initial release from Movial
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            //[S][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
            if (Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0) == 0
            //getActivity().getIntent().getIntExtra("easy_setting_call", 0) == 0 &&
            //"DCM".equalsIgnoreCase(SystemProperties.get("ro.build.operator"))
            ) {
                mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                        mAirplaneModePreference.isChecked());
            }
            //[E][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
        }
        //+s LGBT_ENTITLEMENT_FOR_TETHERING ATT's requirement sinyoung.jun@lge.com
        if (requestCode == MHS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtra("Tether_Type").equals("Bluetooth")) {
                    onBluetoothTether();
                }
            } else {
                Log.e(TAG, "ENTITLEMENT check fail");
                if (mBluetoothTether != null) {
                    mBluetoothTether.setChecked(false);
                }
            }
        }
        //+e LGBT_ENTITLEMENT_FOR_TETHERING
    }

    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private BluetoothProfile.ServiceListener mProfileServiceListener =
            new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mBluetoothPan = (BluetoothPan)proxy;
                    //+s heehoon.lee@lge.com 13.05.27 added for TD30459 BT Tethering checkbox issue
                    Log.e(TAG, "ServiceListener onServiceConnected");
                    if (mBluetoothTether != null) {
                        mBluetoothTether.setChecked(true);
                    }
                    updateState();
                    //+e heehoon.lee@lge.com 13.05.27
                }

                public void onServiceDisconnected(int profile) {
                    mBluetoothPan = null;
                    //+s heehoon.lee@lge.com 13.05.27 added for TD30459 BT Tethering checkbox issue
                    Log.e(TAG, "ServiceListener onServiceDisConnected");
                    //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
                    unsendTetherStateChangedBroadcast();
                    //+e heehoon.lee@lge.com 13.06.12
                    if (mBluetoothTether != null) {
                        mBluetoothTether.setChecked(false);
                    }
                    updateState();
                    //+e heehoon.lee@lge.com 13.05.27
                }
            };

    private final BroadcastReceiver mBTTetheringReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                //+s heehoon.lee@lge.com 13.06.13 DCM APN Disable after BT Tethering ON
                btintent = intent;
                //+e heehoon.lee@lge.com 13.06.12

                // TODO - this should understand the interface types
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);

                if (null != available && null != active && null != errored) {
                    if (bluetoothAvailable) {
                        updateBluetoothState(available.toArray(new String[available.size()]),
                                active.toArray(new String[active.size()]),
                                errored.toArray(new String[errored.size()]));
                    }
                }
            }
            // [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
            //+++ BRCM
            else if (action.equals(BluetoothPan.ACTION_CONNECTION_STATE_CHANGED)) {
                updateState();
            }
            //--- BRCM
            // [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBluetoothEnableForTether) {
                    switch (intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_ON:
                        mBluetoothPan.setBluetoothTethering(true);
                        mBluetoothEnableForTether = false;
                        break;

                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.ERROR:
                        mBluetoothEnableForTether = false;
                        break;

                    default:
                        // ignore transition states
                    }
                }
                if (bluetoothAvailable) {
                    updateState();
                }
            }
            // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
            else if (mPROVISION
                    && intent.getAction().equals("com.lge.hotspotprovision.STATE_CHANGED"))
            {
                context.removeStickyBroadcast(intent);
                Log.d(TAG,
                        "[BTUI]hotspotprovision.STATE_CHANGED : " + intent.getIntExtra("result", 0));
                if (mBluetoothTether != null) {
                    if (intent.getIntExtra("result", 0) == 1)
                    {
                        Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION OK.");
                        mBluetoothTether.setChecked(true);
                        mIsProvisioned = true;
                        gotoNextStep(true);
                    } else {
                        Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION FAIL.");
                        mBluetoothTether.setChecked(false);
                        mIsProvisioned = false;
                    }
                }
            }
            // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
        }
    };

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

    private void updateState() {

        String[] available = btcm.getTetherableIfaces();
        String[] tethered = btcm.getTetheredIfaces();
        String[] errored = btcm.getTetheringErroredIfaces();
        updateBluetoothState(available, tethered, errored);
    }

    private void updateBluetoothState(String[] available, String[] tethered,
            String[] errored) {
        int bluetoothTethered = 0;
        // LG_BTUI: ADD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [S]
        int bluetoothTethered_BRCM = 0;
        // LG_BTUI: ADD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [E]

        if (mBluetoothTether != null) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
                return;
            }
            // LGMDM_END
        }

        // [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
        for (String s : tethered) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) {
                    bluetoothTethered++;
                }
            }
        }
        // [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch

        boolean bluetoothErrored = false;
        for (String s : errored) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) {
                    bluetoothErrored = true;
                }
            }
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int btState = adapter.getState();
        if (btState == BluetoothAdapter.STATE_TURNING_OFF) {
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376376 Null Pointer Dereference fixed
                mBluetoothTether.setEnabled(false);
                //YJSYS_CHANG_S : 2012-11-22 choja@lge.com string change
                //mBluetoothTether.setSummary(R.string.wifi_stopping);
                mBluetoothTether.setSummary(R.string.bluetooth_turning_off);
                //YJSYS_CHANG_E : 2012-11-22 choja@lge.com
            }
        } else if (btState == BluetoothAdapter.STATE_TURNING_ON) {
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376377 Null Pointer Dereference fixed
                mBluetoothTether.setEnabled(false);
                mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
            }
        } else if (btState == BluetoothAdapter.STATE_ON && mBluetoothPan != null) {
            if (mBluetoothPan.isTetheringOn()) {
                // [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
                //+++ BRCM
                List<BluetoothDevice> devices = mBluetoothPan.getConnectedDevices();
                if (devices != null) {
                    bluetoothTethered_BRCM = devices.size();
                }
                //--- BRCM
                // [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch

                if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376378 Null Pointer Dereference fixed
                    mBluetoothTether.setChecked(true);
                    mBluetoothTether.setEnabled(true);
                }
                // LG_BTUI: MOD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [S]
                if (bluetoothTethered > 1 || bluetoothTethered_BRCM > 1) {
                    int mTetheredNum = 0;
                    if (bluetoothTethered_BRCM > 1)
                    {
                        mTetheredNum = bluetoothTethered_BRCM;
                    }
                    else if (bluetoothTethered > 1)
                    {
                        mTetheredNum = bluetoothTethered;
                    }
                    String summary = getString(
                            R.string.sp_bluetooth_tethering_devices_connected_subtext, mTetheredNum);
                    // LG_BTUI: MOD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [E]
                    mBluetoothTether.setSummary(summary);
                } else if (bluetoothTethered == 1) {
                    mBluetoothTether
                            .setSummary(R.string.sp_bluetooth_tethering_device_connected_subtext);
                } else if (bluetoothErrored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
                }
                //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
                sendTetherStateChangedBroadcast();
                //+e heehoon.lee@lge.com 13.06.12
            } else {
                if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.08.21 WBT #376379 Null Pointer Dereference fixed
                    mBluetoothTether
                            .setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                    mBluetoothTether.setEnabled(true);
                    // LKZ_CHANGE_S : 20121205, namespace26.lee@lge.com, Go to WirelessSettings at Bluetooth tethering.
                    mBluetoothTether.setChecked(false);
                    // LKZ_CHANGE_E : 20121205, namespace26.lee@lge.com, Go to WirelessSettings at Bluetooth tethering.
                    //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
                    unsendTetherStateChangedBroadcast();
                    //+e heehoon.lee@lge.com 13.06.12
                }
            }
        } else {
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376379 Null Pointer Dereference fixed
                mBluetoothTether.setEnabled(true);
                mBluetoothTether.setChecked(false);
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
            }
        }
    }

    //+s LGBT_ENTITLEMENT_FOR_TETHERING ATT's requirement sinyoung.jun@lge.com 2012-05-24

    private void onBluetoothTether() {
        // turn on Bluetooth first
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
            mBluetoothEnableForTether = true;
            adapter.enable();
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376372, #376373 Null Pointer Dereference fixed
                mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
                mBluetoothTether.setEnabled(false);
            }
        } else {
            mBluetoothPan.setBluetoothTethering(true);
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376372, #376373 Null Pointer Dereference fixed
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
            }
        }
        //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
        sendTetherStateChangedBroadcast();
        //+e heehoon.lee@lge.com 13.06.12
    }

    //+e LGBT_ENTITLEMENT_FOR_TETHERING

    // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
    private void checkProvision() {

        //TODO: check is pan already enabled? then return
        /*
           if (mLocalManager.isMobileHotspotOn())
           return;
         */
        if (!mIsProvisioned)
        {
            Intent proIntent = new Intent("com.lge.hotspot.provision_start");
            startActivity(proIntent);
        } else {
            gotoNextStep(true);
        }
    }

    private void gotoNextStep(boolean value) {
        if (value) {
            //+s LGBT_ENTITLEMENT_FOR_TETHERING ATT's requirement sinyoung.jun@lge.com 2012-05-24
            String[] appDetails;
            if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
                appDetails = getResources().getStringArray(
                        R.array.config_mobile_bt_tether_provision_app);
            } else {
                appDetails = getResources().getStringArray(
                        com.android.internal.R.array.config_mobile_hotspot_provision_app);
            }

            if (appDetails.length != 2) {
                Log.d(TAG, "Bluetooth tethering on");
                onBluetoothTether();
            } else {
                Log.d(TAG, "Check to ENTITLEMENT for BT tethering");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName(appDetails[0], appDetails[1]);
                if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
                    if (Settings.System.getInt(getContentResolver(),
                            SettingsConstants.System.TETHER_ENTITLEMENT_CHECK_STATE, 1) > 0) {
                        intent.putExtra("Tether_Type", "Bluetooth");
                        startActivityForResult(intent, MHS_REQUEST);
                    } else {
                        onBluetoothTether();
                    }
                }
            }
            //+e LGBT_ENTITLEMENT_FOR_TETHERING
        } else {
            boolean errored = false;
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            String[] tethered = cm.getTetheredIfaces();
            String bluetoothIface = findIface(tethered, mBluetoothRegexs);
            if (bluetoothIface != null &&
                    cm.untether(bluetoothIface) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                errored = true;
            }
            if (mBluetoothPan != null)//LGBT_DCM 20130425 joon1979.kim@lge.com added for DCM BT Tethering Alert
            {
                mBluetoothPan.setBluetoothTethering(false);
            }

            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376374, #376375 Null Pointer Dereference fixed
                if (errored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether
                            .setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                }
            }
            //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
            unsendTetherStateChangedBroadcast();
            //+e heehoon.lee@lge.com 13.06.12
        }

    }

    // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION

    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }

    // rebestm_KK_SMS
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSmsApplicationPreference && newValue != null) {
            // rebestm - Add SMS Warning popup
            ComponentName appName = SmsApplication.getDefaultSmsApplication(getActivity(), true);
            String oldpackageName = appName.getPackageName();

            if (oldpackageName.toString().equals(newValue.toString())) {
                Log.d(TAG, "Default SMS set SAME!!!");
                return false;
            }
            if (!newValue.toString().equals(LGSMS_PACKAGENAME)) {
                SmsDefaultDialogWarning(newValue.toString());
            } else {
                SmsApplication.setDefaultApplication(newValue.toString(), getActivity());
                updateSmsApplicationSetting();
            }
            return true;
        }
        return false;
    }

    private void updateAirplaneModeState(boolean isAirplaneMode) {

        String toggleable = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        if (isAirplaneMode) {
            // Manually set dependencies for Wifi when not toggleable.
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
                getPreferenceScreen().findPreference(KEY_VPN_SETTINGS).setEnabled(false);
            }
            // Manually set dependencies for NFC when not toggleable.
            //[START] change new nfcadapterIf
            if (mNfcSettingAdapterIf != null) {
            //if (mNfcAdapter != null) {
            // [END]
                if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_NFC)) {
                    getPreferenceScreen().findPreference(KEY_TOGGLE_NFC).setEnabled(false);
                    getPreferenceScreen()
                            .findPreference(KEY_ANDROID_BEAM_SETTINGS).setEnabled(false);
                    Log.d(TAG, "NFC native disabled");
                }
            }

            if (!Utils.isWifiOnly(getActivity())) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())
                {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM)
                            .setEnabled(false);
                }
                else
                {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(
                            false);
                }
            }
            if ("SKT".equals(Config.getOperator())) {
                findPreference(KEY_DATA_NETWORK_SETTINGS).setEnabled(false);
            }
            // Manually set dependencies for Bluetooth when not toggleable.
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
                // No bluetooth-dependent items in the list. Code kept in case one is added later.
            }

        } else {
            // Manually set dependencies for Wifi when not toggleable.
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
                getPreferenceScreen().findPreference(KEY_VPN_SETTINGS).setEnabled(true);
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMSettingsAdapter.getInstance().setVpnMenu(
                            getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
                }
                // LGMDM_END
            }
            // Manually set dependencies for NFC when not toggleable.
            //[START] change new nfcadapterIf
            if (mNfcSettingAdapterIf != null) {
            //if (mNfcAdapter != null) {
            // [END]
                if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_NFC)) {
                    getPreferenceScreen().findPreference(KEY_TOGGLE_NFC).setEnabled(true);
                    getPreferenceScreen()
                            .findPreference(KEY_ANDROID_BEAM_SETTINGS).setEnabled(true);
                    Log.d(TAG, "Native NFC settings enabled");
                }
            }

            // [2012.12.31][munjohn.kang] added a triple SIM condition
            if (!Utils.isWifiOnly(getActivity()) && Utils.hasReadyMobileRadio(getActivity())) {
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())
                {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM)
                            .setEnabled(true);
                }
                else
                {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(
                            true);
                }
            }
            if ("SKT".equals(Config.getOperator())) {
                findPreference(KEY_DATA_NETWORK_SETTINGS).setEnabled(true);
            }
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
                // No bluetooth-dependent items in the list. Code kept in case one is added later.
            }

        }

    }

    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CHRISWON", "Airplane Evenr arrives!!");
            if (intent.getBooleanExtra("state", false) == true) {
                updateAirplaneModeState(true);
                //getView().invalidate();
            }
            else {
                updateAirplaneModeState(false);
                //getView().invalidate();
            }
        }
    };
    private ContentObserver mApnChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean value = Config.isVZWAdminDisabled(getActivity().getApplicationContext());
            //[S][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
            if (Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0) == 0) {
                mAirplaneModePreference.setEnabled(!value);
            }
            //[E][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
        }
    };

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-223][ID-MDM-42]
    // [ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-47][ID-MDM-195][ID-MDM-75][ID-MDM-225]
    // [ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-274][ID-MDM-282]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveWirelessSettingsPolicyChangeIntent(
                        intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }
    };

    // LGMDM_END

    @Override
    public void onDestroy() {
        super.onDestroy();

        //[START] change new nfcadapterIf
        if (mNfcSettingAdapterIf != null) {
            mNfcSettingAdapterIf.destroy(); ////[PORTING_FOR_NFCSETTING]
            mNfcSettingAdapterIf = null;
        }       
        //[END]

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35][ID-MDM-167][ID-MDM-198][ID-MDM-223][ID-MDM-42]
        // [ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-47][ID-MDM-195][ID-MDM-75][ID-MDM-225]
        // [ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-274][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    //[s][chris.won@lge.com][2013-01-09] Popup on File Networking Disable
    public void confirmDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(getText(R.string.sp_dlg_note_NORMAL))
                .setPositiveButton(getText(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                final Intent intent = new Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + "com.lge.wireless_storage"));
                                startActivity(intent);

                            }
                        })
                .setNegativeButton(getText(R.string.dlg_cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {

                            }
                        }).setMessage(getText(R.string.sp_app_enable_confirm_message_NORMAL))
                .create();
        dialog.show();
    }

    public boolean appIsEnabled() {

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo("com.lge.wireless_storage", PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return info.enabled;

    }

    //[e][chris.won@lge.com][2013-01-09] Popup on File Networking Disable


    // [Start] rebestm - Use DLNA feature
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onPrepareOptionsMenu(menu);
        menu.clear();

        if (IsDLNAOptionMenu(CHECK_PACKAGE_NAME_SMARTSHARE)) {
            if (menu.findItem(OPTION_ID_DLNA) == null) {
                menu.add(0, OPTION_ID_DLNA, 0,
                        getString(R.string.sp_shareconnect_dlna_option_NORMAL));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == OPTION_ID_DLNA) {
            Intent intent = new Intent();
            intent.setAction("com.lge.smartshare.dlna.action.launcher");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            //    finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean IsDLNAOptionMenu(String packageName) {

        PackageManager pm = getPackageManager();
        android.content.pm.ApplicationInfo pmApplicationInfo = null;

        if (isNearbyMenu(getActivity())) {
            return false;
        }
        try {
            pmApplicationInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException com.lge.smartshare");
            return false;
        }

        if (pmApplicationInfo == null) {
            Log.e(TAG, "pmApplicationInfo is null");
            return false;
        }

        CharSequence charSequence = pm.getApplicationLabel(pmApplicationInfo);
        Log.d(TAG, "EnableApplication ->   " + charSequence);

        try {
            if (pm.getApplicationEnabledSetting(packageName)
                        == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    && charSequence.equals(CHECK_LABEL_NAME_DLNA)) {
                Log.d(TAG, "EnableApplication ->   "
                        + pm.getApplicationEnabledSetting(packageName)); // 0    
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception IsDLNAOptionMenu Disable");
            return false;
        }
        Log.e(TAG, "IsDLNAOptionMenu Disable");
        return false;
    }

    public boolean isCheckPackage(Context context, String packageName, int type) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, type);
            if (pi.packageName.equals(packageName)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isCheckPackage() is not found(" + packageName
                    + ")");
            return false;
        }
        return false;
    }

    public boolean isNearbyMenu(Context context) {
        PackageInfo pi = null;
        int versionCode = 0;

        try {
            pi = context.getPackageManager().getPackageInfo(CHECK_PACKAGE_NAME_SMARTSHARE, 0);
            versionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isNearbyMenu() is not found(" + CHECK_PACKAGE_NAME_SMARTSHARE
                    + ")");
            return false;
        }

        if (versionCode >= SHARTSHARE_VERSIONCODE) {
            Log.i(TAG, "isNearbyMenu is true = " + versionCode);
            return true;
        }
        Log.i(TAG, "isNearbyMenu is false = " + versionCode);
        return false;
    }

    //[End] rebestm - Use DLNA feature

    public boolean isEnableCheckPackage(Context context, String packageName) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, 0);
            if (pi.packageName.equals(packageName) && (pi.applicationInfo.enabled == true)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isEnableCheckPackage() is not found(" + packageName
                    + ")");
            return false;
        }
        return false;
    }

    //+s heehoon.lee@lge.com 13.06.12 DCM APN Disable after BT Tethering ON
    private void sendTetherStateChangedBroadcast() {
        Log.d(TAG, "[BTUI] sendTetherStateChangedBroadcast");
        ArrayList<String> available = new ArrayList<String>();
        ArrayList<String> active = new ArrayList<String>();
        ArrayList<String> errored = new ArrayList<String>();
        if (btintent != null)
        {
            available = btintent.getStringArrayListExtra(
                    ConnectivityManager.EXTRA_AVAILABLE_TETHER);
            active = btintent.getStringArrayListExtra(
                    ConnectivityManager.EXTRA_ACTIVE_TETHER);
            errored = btintent.getStringArrayListExtra(
                    ConnectivityManager.EXTRA_ERRORED_TETHER);
        }

        if (!(active.size() > 0)) {
            Log.d(TAG, "[BTUI] sendTetherStateChangedBroadcast !!");
            ArrayList<String> activeList = new ArrayList<String>();
            activeList.add("Bluetooth");
            Intent broadcast = new Intent(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
            broadcast.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING |
                    Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER,
                    available);
            broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER,
                    activeList);
            broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER,
                    errored);

            mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
        } else {
            Log.d(TAG, "[BTUI] sendTetherStateChangedBroadcast ??");
        }
    }

    private void unsendTetherStateChangedBroadcast() {
        Log.d(TAG, "[BTUI] UnsendTetherStateChangedBroadcast");
        if (btintent != null) {
            //ArrayList<String> available = btintent.getStringArrayListExtra(
            //        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
            ArrayList<String> active = btintent.getStringArrayListExtra(
                    ConnectivityManager.EXTRA_ACTIVE_TETHER);
            //ArrayList<String> errored = btintent.getStringArrayListExtra(
            //        ConnectivityManager.EXTRA_ERRORED_TETHER);

            if (active.contains("Bluetooth")) {
                Log.d(TAG, "[BTUI] unsendTetherStateChangedBroadcast !!");
                ArrayList<String> availableList = new ArrayList<String>();
                ArrayList<String> activeList = new ArrayList<String>();
                ArrayList<String> erroredList = new ArrayList<String>();
                Intent broadcast = new Intent(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
                broadcast.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING |
                        Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
                broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER,
                        availableList);
                broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER,
                        activeList);
                broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER,
                        erroredList);
                mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            } else {
                Log.d(TAG, "[BTUI] unsendTetherStateChangedBroadcast ??");
            }
        }
    }

    //+e heehoon.lee@lge.com 13.06.12

    // rebestm - Add SMS Warning popup
    public void SmsDefaultDialogWarning(String packageName) {
        final String string = packageName;
        final Context mContext = this.getActivity();
        Builder AlertDlg = new AlertDialog.Builder(mContext);
        AlertDlg.setTitle(getResources().getString(R.string.sp_dlg_note_NORMAL));
        AlertDlg.setMessage(getString(R.string.sms_change_default_app_warning));
        AlertDlg.setPositiveButton(R.string.def_yes_btn_caption
                , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SmsApplication.setDefaultApplication(string, getActivity());
                        updateSmsApplicationSetting();
                    }
                })
                .setNegativeButton(R.string.def_no_btn_caption,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                dialog.dismiss();
                                updateSmsApplicationSetting();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        updateSmsApplicationSetting();
                    }
                }).setIconAttribute(android.R.attr.alertDialogIcon)
                .show();
    }

    //[youngju.do]PCSuiteSetting_v4.1
    private boolean isPCSuiteUISupportSettingMenu(Context context) {

        if (!isEnableCheckPackage(context, PACKAGE_NAME_PCSUITE)) {
            Log.e("YJDO", "[isPCSuiteUISupportSettingMenu] not support com.lge.sync");
            return false;
        }

        if (Utils.isUI_4_1_model(context)) {
            PackageManager pm = context.getPackageManager();
            if (null != pm) {
                try {
                    PackageInfo pkgInfo = pm.getPackageInfo(PACKAGE_NAME_PCSUITEUI, 0);
                    if (null != pkgInfo && (pkgInfo.versionCode >= 40200000)) {
                        return true;
                    } else {
                        Log.e("YJDO",
                                "[isPCSuiteUISupportSettingMenu] pcsyncui versionCode is lower than 40200000");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("YJDO", "[isPCSuiteUISupportSettingMenu] not support com.lge.pcsyncui");
                    return false;
                }
            }
        }
        return false;
    }
}
