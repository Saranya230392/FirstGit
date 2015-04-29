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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import android.app.admin.DevicePolicyManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import android.text.TextUtils;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.UserHandle;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;
import android.widget.Switch;
import android.content.pm.PackageManager.NameNotFoundException;

import android.util.Log;

import android.provider.Telephony.Sms.Intents;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import com.android.settings.lge.WifiCallCheckBox;
import com.android.settings.lgesetting.Config.Config;

import com.lge.constants.SettingsConstants;
import android.view.WindowManager;

public class WirelessMoreSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    // [shpark82.park] New VPN UI [START]
    private static final String KEY_VPN_SELECTOR = "vpn_selector";
    // [shpark82.park] New VPN UI [END]
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    //rebestm - kk migration
    private static final String KEY_MOBILE_NETWORK_SETTING_DUALSIM = "mobile_network_settings_dualsim";
    private static final String KEY_TOGGLE_NSD = "toggle_nsd";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";

    //[minwoo2.kim, 2012/12/20] [e]
    public static final int REQUEST_CODE_EXIT_ECM = 1;
    private static final String KEY_EXCHANGE_PHONE = "exchage_phone_capability";
    // request CMCC
    private static final String KEY_CMCC_PM = "switch_pm";
    private static final String CMCC_DM = "cmcc_device_management";
// rebestm - Add SMS Warning popup
    private static final String VZWMSG_PACKAGENAME = "com.verizon.messaging.vzmsgs";
    private static final String LGSMS_PACKAGENAME = "com.android.mms";

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private SettingsSwitchPreference mAirplaneModePreference;
    private NsdEnabler mNsdEnabler;

    private static final String VIEW_VERIZON_ACCOUNT_TABLET = "view_verizon_account_tablet";
    private PreferenceScreen mViewVerizonAccount;

    public final static String SHARED_PREFERENCES_NAME = "first_time_use_Settings";
    public final static String EXTRA_FIRST_HELP_ASK = "help_ask";
    public final static String SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK = "wifi_help_ask";

// rebestm - HTTP POST intent added
    private static final String Production_URL_HTTPS_WO_QMARK = "https://quickaccess.verizonwireless.com/bbportal/oem/start.do";

    public static final String IMS_REGISTRATION = "IMS_REGISTRATION";
    public static final String IMS_REG_STATUS = "IMS_REG_STATUS";

    private static final int PRESENCE_EAB_STATUS_ERROR = -1;
    private static final int PRESENCE_EAB_STATUS_FALSE = 0;
    private static final int PRESENCE_EAB_STATUS_TRUE  = 1;

    private static final String TAG = "MoreSettings";
    private PreferenceScreen mMobileNetworkSettings;

    protected TelephonyManager mPhone;
    protected PhoneStateListener mPhoneStateListener; // kerry - for dcm req
    boolean csActive = false;
    // kerry end

    //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
    private static final String BUTTON_WIFI_CALLING_KEY = "button_wifi_calling_key";
    private WifiCallCheckBox mWifiCallingEnabler;
    boolean mIsWifiCallingSupport = false;
    //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]

    //+s LGBT_ENTITLEMENT_FOR_TETHERING, younghyun.kwon@lge.com, 20130422, To check Entitlement of BT Tethering for AT&T
    public static final String BT_ENTITLEMENT_CHECK_OK =
        "com.android.settings.EntitlementCheckService.BT_ENTITLEMENT_CHECK_OK";
    public static final String BT_ENTITLEMENT_CHECK_FAILURE =
        "com.android.settings.EntitlementCheckService.BT_ENTITLEMENT_CHECK_FAILURE";
    //+e LGBT_ENTITLEMENT_FOR_TETHERING

    boolean b_isWifiOnly = false;    //rebestm
    boolean mWifiCallRegistered = false;

    // rebestm_KK_SMS
    private static final String KEY_SMS_APPLICATION = "sms_application";
    private AppListPreference mSmsApplicationPreference;

//BT_S : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
    public static final boolean NO_SIM_MODEL =
             (SystemProperties.getInt("ro.telephony.default_network", -1) == 4 ? true : false);
    public static final String MHP_3G_PROVISION_REMINDER = "mhp_3g_provision_reminder";
    public static final String MHP_3G_PROVISION_NOTI_DONE = "mhp_3g_provision_noti_done";
//BT_E : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

    PMSwitchPreference mSwitchPM;
    CheckBoxPreference  mCheckExchangephone;

    private static final long CHECK_EXCHANGEPHONE_INTERVAL = 30000; // 30s
    public static final Uri CONTENT_URI
            = Uri.parse("content://com.lge.ims.provider.lgims/lgims_com_service_eab");
    public static final Uri IMS_REG_CONTENT_URI
            = Uri.parse("content://com.lge.ims.provider.uc/ucstate");
    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
     boolean mIsSecondaryUser;
     private UserManager mUm;

     private Preference mEmergencyAlert;
     private static final String KEY_EMERGENCY_ALERTS = "emergency_alert";

    private boolean exchangephoneOnPPreferenceTreeClick() {
        int isResut = getExchangephonecapability();
        Log.d (TAG, "mCheckExchangephone is click = " + isResut);

        long startTime = getCheckStartTime();

        if (startTime == 0 ) {
            setCheckStartTime();
        } else {
            long now = System.currentTimeMillis();
            long interval = now - startTime;

            if (interval > CHECK_EXCHANGEPHONE_INTERVAL ) {
                setCheckStartTime();
                Log.d (TAG, "30s upper = " + interval);
            } else {
                Log.d (TAG, "30s under = " + interval);
                Toast.makeText(getActivity(), R.string.settings_tether_exchange_wait, Toast.LENGTH_SHORT).show();
                switch (isResut) {
                    case PRESENCE_EAB_STATUS_TRUE:
                        mCheckExchangephone.setChecked(true);
                        break;
                    case PRESENCE_EAB_STATUS_FALSE:
                        mCheckExchangephone.setChecked(false);
                        break;
                    default:
                        break;
            }
            return false;
           }
       }

       switch (isResut) {
             case PRESENCE_EAB_STATUS_ERROR:
                  break;
             case PRESENCE_EAB_STATUS_TRUE:
                  setExchangephonecapability(false);
                  break;
             case PRESENCE_EAB_STATUS_FALSE:
                  setExchangephonecapability(true);
                  break;
             default:
                  break;
      }
      return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAirplaneModePreference && Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode launch ECM app dialog
            startActivityForResult(new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null), REQUEST_CODE_EXIT_ECM);
            return true;
        } else if (preference == mAirplaneModePreference && true == csActive) {
            mAirplaneModeEnabler.updatePreferences();
            return true;
        } else if (preference == mWifiCallingEnabler) {
            Intent mIntent = new Intent();
            mIntent.setClassName("com.movial.wificall", "com.movial.wificall.Settings");
            startActivity(mIntent);
        } else if (preference == mViewVerizonAccount) {
            Log.d (TAG, "click View Verizon Account");
            if ("VZW".equals(Config.getOperator()) && Utils.isTablet()) {
                if (IsSupportPCO()) {
                    showSupportPCO_Dialog();
                } else {
                    prepareEnteringPortalVZW();

                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            "com.android.settings.VerizonOnlinePortal");
                    startActivity(intent);
                }
            } else {
                Intent intent = makeHttpPostIntent(getActivity());
                startActivity(intent);
            }
            return true;
        } else if (preference == mCheckExchangephone) {
            boolean ret = exchangephoneOnPPreferenceTreeClick();
            if (ret == false) {
                return false;
            }
        } else if (KEY_EMERGENCY_ALERTS.equals(preference.getKey())) {
            if (isSupportEmergencyAlerts(getActivity()) == true && Config.getOperator().equals("ATT")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.lge.cmas", "com.lge.cmas.ui.CmasThreadList"));
                startActivity(intent);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void prepareEnteringPortalVZW() {
        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);

        if (pco_ims == 5) {
            mPhone.setRadioPower(true);
            mPhone.setDataEnabled(true);
            Settings.Secure.putInt(this.getContentResolver(),
                        "radio_off_by_pco5", 0);
        } else if (pco_internet == 3) {
            mPhone.setDataEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange preference = " + preference );
        if (preference == mSmsApplicationPreference && newValue != null) {
// rebestm - Add SMS Warning popup
            ComponentName appName = SmsApplication.getDefaultSmsApplication(getActivity(), true);
            String oldpackageName = appName.getPackageName();
            Resources r =  getActivity().getResources();
            String defaultPackage = r.getString(com.android.internal.R.string.default_sms_application);
            Log.d  (TAG, "defaultPackage = " + defaultPackage);

            if ( oldpackageName.toString().equals(newValue.toString()) ) {
                Log.d (TAG, "Default SMS set SAME!!!");
                return false;
            }
            if ( oldpackageName.toString().equals(LGSMS_PACKAGENAME) && (defaultPackage.equalsIgnoreCase(VZWMSG_PACKAGENAME) == false)) {
                final Intent intent = new Intent("com.lge.settings.ACTION_CHANGE_DEFAULT_WARMING_POPUP");
                intent.putExtra(Intents.EXTRA_PACKAGE_NAME, newValue.toString());
                startActivity(intent);
            } else {
              SmsApplication.setDefaultApplication(newValue.toString(), getActivity());
              updateSmsApplicationSetting();
            }
            return true;
        } else if ( preference == mSwitchPM ) {
            boolean value = android.provider.Settings.System.getInt(getActivity().getContentResolver(), CMCC_DM, 1) == 1 ? true : false;

            value = value == true ? false : true;

            Log.d (TAG, "onPreferenceChange = " + value);

            if (mSwitchPM != null) {
                mSwitchPM.setChecked(value);
                android.provider.Settings.System.putInt(getActivity().getContentResolver(), CMCC_DM, value == true ? 1 : 0);
            } else {
                Log.e (TAG, "mSwitch is null");
            }
            return true;
        }

        return false;
    }

    private void airplaneModeOnCreate() {
        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
        boolean isAirplaneGone = false;
        if (Config.supportAirplaneListMenu()) {
            isAirplaneGone = true;
        }
        updateAirplaneModeState(isAirplaneMode == 1 ? true : false);
        mAirplaneModePreference = (SettingsSwitchPreference)findPreference(KEY_TOGGLE_AIRPLANE);
        mAirplaneModePreference.setDivider(false);
        if (isAirplaneGone == false) {
            mAirplaneModeEnabler = new AirplaneModeEnabler(getActivity(), mAirplaneModePreference, null);
        }
        else if (isAirplaneGone == true ) {
            getPreferenceScreen().removePreference(findPreference(KEY_TOGGLE_AIRPLANE));
        }
        if (0 == isAirplaneMode && isAirplaneGone == false) {
            if (Utils.isWifiOnly(getActivity()) && !Utils.isTablet()) {
                mAirplaneModePreference.setSummary(R.string.sp_airplane_mode_summary_NORMAL_jb_plus_wifi_only);
            } else {
                mAirplaneModePreference.setSummary(R.string.sp_airplane_mode_summary_NORMAL_jb_plus);
            }
        }
        // kerry - for qm req start
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state != TelephonyManager.CALL_STATE_IDLE || getTetherNetworkAirplaneModeVoLTE()) {
                    csActive = true;
                } else {
                    csActive = false;
                }
            }

            @Override
            public void onDataActivity(int direction) {
            }
        };

        mPhone = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

    }

    private void mobileNetworkOnCreate(boolean isSecondaryUser) {
        if ((b_isWifiOnly || isSecondaryUser) || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            Log.d("settings", "[TetherNetworkSettings] is Wi-Fi only Device - delete Network settings ");
            getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTINGS));
        }
        // kerry start for VZW
        if ( "VZW".equals(Config.getOperator()) && (!b_isWifiOnly) && (!mIsSecondaryUser) && (!mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS))) {
            mMobileNetworkSettings = (PreferenceScreen)findPreference(KEY_MOBILE_NETWORK_SETTINGS);
            if (SystemProperties.getInt("ro.telephony.default_network", 0) == 4) {
                mMobileNetworkSettings.setSummary(R.string.sp_mobile_network_contens_NORMAL);
            } else if (Utils.isTablet()) {
                mMobileNetworkSettings.setSummary(R.string.settings_tether_netwokr_sumary_noroaming);
            } else {
                mMobileNetworkSettings.setSummary(R.string.sp_network_settings_summary_NORMAL);
            }
        }
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTINGS));
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM));
        }
        /* [E][2012.2.17] kyochun.shin@lge.com mobileSetting for Dual SIM or Single SIM*/

    }

    private void vpnSettingsOnCreate(boolean isSecondaryUser) {
        // [shpark82.park] New VPN UI [START]
        if (isSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)) {
            getPreferenceScreen().removePreference(findPreference(KEY_VPN_SETTINGS));
            getPreferenceScreen().removePreference(findPreference(KEY_VPN_SELECTOR));
        } else {
            if (Utils.isSupportVPN(getActivity())) {
                getPreferenceScreen().removePreference(findPreference(KEY_VPN_SETTINGS));
            } else {
                getPreferenceScreen().removePreference(findPreference(KEY_VPN_SELECTOR));
            }
        }

        // [shpark82.park] New VPN UI [END]
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addTetherPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
            MDMSettingsAdapter.getInstance().setVpnMenu(getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
            MDMSettingsAdapter.getInstance().setVpnMenu(getPreferenceScreen().findPreference(KEY_VPN_SELECTOR));
        }
        // LGMDM_END

    }

    private void verizonAccountOnCreate() {
        mViewVerizonAccount = (PreferenceScreen)findPreference(VIEW_VERIZON_ACCOUNT_TABLET);
        if (Config.getOperator().equals(Config.VZW)) {
            if (Utils.isTablet()) {
                Log.d (TAG, "support View Verizon Account menu");
            }

            if (Utils.isTablet() == false || mIsSecondaryUser) {
                Log.d (TAG, "delete a ViewVerizonAccount menu");
                getPreferenceScreen().removePreference(findPreference(VIEW_VERIZON_ACCOUNT_TABLET));
                mViewVerizonAccount = null;
            }
        } else {
			getPreferenceScreen().removePreference(findPreference(VIEW_VERIZON_ACCOUNT_TABLET));
			mViewVerizonAccount = null;
        }
    }

    private void cmccPmSettingsOnCreate() {
        mSwitchPM = (PMSwitchPreference)findPreference(KEY_CMCC_PM);
        boolean isCMCCPM = false;   // Not supported by CMCC from L OS

        if (!isCMCCPM) {
            Log.d (TAG, "Delete mSwitchPM menu");
            getPreferenceScreen().removePreference(mSwitchPM);
            mSwitchPM = null;
        } else {
            mSwitchPM.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUm = (UserManager)getSystemService(Context.USER_SERVICE);

        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = null;

        // Remove Mobile Network Settings if it's a wifi-only device.
        b_isWifiOnly = Utils.isWifiOnly(getActivity());
        mIsSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
        Log.i (TAG, "b_isWifiOnly = " + b_isWifiOnly);
        Log.i (TAG, "mIsSecondaryUser = " + mIsSecondaryUser);

        final Activity activity = getActivity();

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.wireless_more_settings);
		airplaneModeOnCreate();
        mobileNetworkOnCreate(mIsSecondaryUser);
        mCheckExchangephone = (CheckBoxPreference)findPreference(KEY_EXCHANGE_PHONE);

        boolean isUCE = getPackageManager().hasSystemFeature("com.lge.ims.service.eab");
        Log.d (TAG, "isUCE = " + isUCE);

        Log.d (TAG, "Delete Exchangephone menu111");
        getPreferenceScreen().removePreference(mCheckExchangephone);
        mCheckExchangephone = null;

        // WBT
        CheckBoxPreference nsd = null;

        nsd = (CheckBoxPreference)findPreference(KEY_TOGGLE_NSD);

// rebestm_KK_SMS
        mSmsApplicationPreference = (AppListPreference)findPreference(KEY_SMS_APPLICATION);
        mSmsApplicationPreference.setOnPreferenceChangeListener(this);
        initSmsApplicationSetting();

        // Remove NSD checkbox by default
        if (nsd != null) {
            getPreferenceScreen().removePreference(nsd);
        }

        mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);

        DevicePolicyManager mDPM = (DevicePolicyManager)activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support

        if (mGlobalProxy != null) {
            getPreferenceScreen().removePreference(mGlobalProxy);
        }

        if (mGlobalProxy != null) {
            mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);
        }

// rebestm_KK_SMS

       if (isSmsSupported() == false || (Utils.isUI_4_1_model(getActivity()) && !"VZW".equals(Config.getOperator()))) {
           getPreferenceScreen().removePreference(findPreference(KEY_SMS_APPLICATION));
       }
// rebestm_temp_PLNA_block

        verizonAccountOnCreate();
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        mWifiCallingEnabler = (WifiCallCheckBox)findPreference(BUTTON_WIFI_CALLING_KEY);
        mIsWifiCallingSupport = WifiCallCheckBox.isSupport();
        Log.i (TAG, "mIsWifiCallingSupport = " + mIsWifiCallingSupport);
        if (!mIsWifiCallingSupport) {
            if (mWifiCallingEnabler != null) {
                getPreferenceScreen().removePreference(findPreference(BUTTON_WIFI_CALLING_KEY));
                mWifiCallingEnabler = null;
            }
        }
        cmccPmSettingsOnCreate();
	    vpnSettingsOnCreate(mIsSecondaryUser);
        emergencyAlertOnCreate();
    }

    @Override
    public void onStart() {
        super.onStart();

        // rebestm_KK_SMS
        initSmsApplicationSetting();

        if (mSwitchPM != null) {
            mSwitchPM.resume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    private void emergencyAlertOnCreate() {
        mEmergencyAlert = findPreference(KEY_EMERGENCY_ALERTS);
        if (mEmergencyAlert != null) {
            if (!isSupportEmergencyAlerts(getActivity()) || !Config.getOperator().equals("ATT")) {
                Log.d(TAG, "This device doesn't support EmergencyAlert");
                getPreferenceScreen().removePreference(mEmergencyAlert);
            }
        }
    }

    private static boolean isSupportEmergencyAlerts(Context context) {
        try {
            return context.getPackageManager().getPackageInfo("com.lge.cmas", 0) != null;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void airplaneModeOnResume() {
        Activity activity = getActivity();
        //ask130402 :: +
        Utils.set_TelephonyListener(mPhone, mPhoneStateListener);
        //ask130402 :: -
        //[S][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);
        //[E][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        updateAirplaneModeState(isAirplaneMode == 1 ? true : false); //[chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        activity.registerReceiver(mAirplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        if (mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE || getTetherNetworkAirplaneModeVoLTE()) {
            csActive = true;
        } else {
            csActive = false;
        } // kerry

        if (mAirplaneModeEnabler != null) {
            mAirplaneModeEnabler.resume();
        }
        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                if ((isAirplaneMode == 1) || (Utils.hasReadyMobileRadio(getActivity()) == false) ) {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(false);
                } else {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(true);
                }
            } else {
                if (("VZW".equals(Config.getOperator()) || "SPR".equals(Config.getOperator())) && isAirplaneMode == 0 ) {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                } else {
                    if ( isAirplaneMode == 1 || !Utils.hasReadyMobileRadio(getActivity())) {
                        getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
                    } else {
                        getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                    }
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        airplaneModeOnResume();

        if (mNsdEnabler != null) {
            mNsdEnabler.resume();
        }

        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        if (mIsWifiCallingSupport && mWifiCallingEnabler != null) {
            mWifiCallingEnabler.resume();
        }
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]

        if (true == Config.isVZWAdminDisabled(getActivity().getApplicationContext())) {
            mAirplaneModePreference.setEnabled(false);
            //ask130313 :: it disabled the MobileNetworkSetting Menu item in VZW.
        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            mMobileNetworkSettings.setEnabled(false);
        }
            Log.d(TAG, "onCreate :: mMobileNetworkSettings value = false");
        }

        updateSmsApplicationSetting();

        if ( mCheckExchangephone != null) {
            int result = getExchangephonecapability();

            switch (result) {
            case PRESENCE_EAB_STATUS_ERROR:
                getPreferenceScreen().removePreference(mCheckExchangephone);
                Log.d (TAG, "Delete Exchangephone menu222");
                break;
            case PRESENCE_EAB_STATUS_TRUE:
                Log.d (TAG, "onResume - PRESENCE_EAB_STATUS_TRUE");
                mCheckExchangephone.setChecked(true);
                setEnableExchangePhoneMenu();
                break;
            case PRESENCE_EAB_STATUS_FALSE:
                Log.d (TAG, "onResume -PRESENCE_EAB_STATUS_FALSE");
                mCheckExchangephone.setChecked(false);
                setEnableExchangePhoneMenu();
                break;
            default:
                break;
            }
        }

        if (mViewVerizonAccount != null) {
            mViewVerizonAccount.setEnabled(Utils.isEnableCheckPackage(getActivity(), "com.android.chrome"));
        }
    }

    // rebestm - kk migration
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

//ask130402 :: +
        Utils.release_TelephonyListener(mPhone, mPhoneStateListener);
//ask130402 :: -

        if (mAirplaneModeEnabler != null) {
            mAirplaneModeEnabler.pause();
        }

        if (mNsdEnabler != null) {
            mNsdEnabler.pause();
        }
        Activity activity = getActivity();
        activity.unregisterReceiver(mAirplaneModeReceiver);

        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        if (mIsWifiCallingSupport && mWifiCallingEnabler != null) {
            mWifiCallingEnabler.pause();
        }
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]

        if (mSwitchPM != null) {
            mSwitchPM.pause();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            //[S][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
            if (mAirplaneModePreference != null) {
                mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes, mAirplaneModePreference.isChecked());
            }
            //[E][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
        }
    }

    private boolean checkMobileNetworkState()
    {
        ConnectivityManager cm = (ConnectivityManager)this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileConnected = false;

        if (null == cm) {
            Log.e(TAG, "Fail to get a CONNECTIVITY_SERVICE!!!");
            return false;
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        isMobileConnected = true;
                        Log.d(TAG, "MOBILE NETOWRK CONNECTED");
                    }
                }
            }
        }

        if (isMobileConnected == true) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }

    private void setAirplaneModeOnMenuStatus(String toggleable) {
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
            getPreferenceScreen().findPreference(KEY_VPN_SETTINGS).setEnabled(false);
        }
        // Manually set dependencies for NFC when not toggleable.
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(false);
            }
            else {
                 if (getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS) != null) {
                     getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
                 }
            }
        }
    }

    private void setAirplaneModeOffMenuStatus(String toggleable) {
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
            getPreferenceScreen().findPreference(KEY_VPN_SETTINGS).setEnabled(true);
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                MDMSettingsAdapter.getInstance().setVpnMenu(getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
            }
            // LGMDM_END
        }

        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            // Manually set dependencies for NFC when not toggleable.
            // [2012.12.31][munjohn.kang] added a triple SIM condition
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
               getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(true);
            } else {
                if ("VZW".equals(Config.getOperator()) || "SPR".equals(Config.getOperator()) ) {
                    getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                } else {
                    if (!Utils.hasReadyMobileRadio(getActivity())) {
                        getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
                    } else {
                        getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                    }
                }
            }
        }
    }
    private void updateAirplaneModeState(boolean isAirplaneMode) {
        Log.d (TAG, "updateAirplaneModeState -> " +  isAirplaneMode );
        String toggleable = Settings.Global.getString(getActivity().getContentResolver(), Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        if (isAirplaneMode) {
            // Manually set dependencies for Wifi when not toggleable.
            setAirplaneModeOnMenuStatus(toggleable);
        } else {
            // Manually set dependencies for Wifi when not toggleable.
            setAirplaneModeOffMenuStatus(toggleable);
        }

    }

    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                Log.d (TAG, "Airplane Evenr arrives!!");
            if (intent.getBooleanExtra("state", false) == true) {
                updateAirplaneModeState(true);
            }
            else {
                updateAirplaneModeState(false);
            }
        }
    };

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
    // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-282]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveTetherPolicyChangeIntent(intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        // if tetering policy changed, finish activity
                        activity.finish();
                    }
                }
            }
        }
    };
    // LGMDM_END

 // rebestm_KK_SMS
    private boolean isSmsSupported() {
        // Some tablet has sim card but could not do telephony operations. Skip those.
        Log.d (TAG, "mPhone.getPhoneType() = " + mPhone.isSmsCapable());
        return (mPhone.isSmsCapable());
    }

    private void initSmsApplicationSetting() {
        Collection<SmsApplicationData> smsApplications = SmsApplication.getApplicationCollection(getActivity());

        // If the list is empty the dialog will be empty, but we will not crash.
        int count = smsApplications.size();
        String[] packageNames = new String[count];

        int i = 0;
        for (SmsApplicationData smsApplicationData : smsApplications) {
            CharSequence lgSMS = smsApplicationData.mPackageName;
            if ( lgSMS.equals(LGSMS_PACKAGENAME)) {
                packageNames[i] = smsApplicationData.mPackageName;
                i = 1;
                break;
            }
        }

        for (SmsApplicationData smsApplicationData : smsApplications) {
            CharSequence lgSMS = smsApplicationData.mPackageName;
            if ( !lgSMS.equals(LGSMS_PACKAGENAME)) {
                packageNames[i] = smsApplicationData.mPackageName;
                i++;
            }
        }
        String defaultPackageName = null;
        ComponentName appName = SmsApplication.getDefaultSmsApplication(getActivity(), true);
        if (appName != null) {
            defaultPackageName = appName.getPackageName();
        }
        mSmsApplicationPreference.setPackageNames(packageNames, defaultPackageName);
    }

    private void updateSmsApplicationSetting() {
        Log.d (TAG, "updateSmsApplicationSetting:");
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

    private Intent makeHttpPostIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName("com.android.chrome", "com.google.android.apps.chrome.Main");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Telephony
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        String mdn = null;
        String iccid = null;
        String imei = null;

        if (telephonyManager != null) {
            mdn = telephonyManager.getLine1Number();
            iccid = telephonyManager.getSimSerialNumber();
            imei = telephonyManager.getDeviceId();
        }

        if ((iccid == null) || "".equals(iccid) == true) {
            iccid = "00000000000000000000";
        }

        if ((mdn == null) || "".equals(mdn) == true) {
            mdn = "0000000000";
        }

        if ((imei == null) || "".equals(imei) == true) {
            imei = "00000000000000";
        }

        String finalHTML = "";
        String html = readTrimRawTextFile(context, R.raw.http_post_file);

        if ("".equals(html) == false && html != null) {

            String url_valuded_change = html.replace("url_value", Production_URL_HTTPS_WO_QMARK);
            String mdn_valuded_change = url_valuded_change.replace("mdn_value", mdn);
            String oem_valuded_change = mdn_valuded_change.replace("oem_value", "MOT");
            String iccid_valuded_change = oem_valuded_change.replace("iccid_value", iccid);
            String imei_valuded_change = iccid_valuded_change.replace("imei_value", imei);
            String encodedURL = "";

            try {
                encodedURL = URLEncoder.encode(imei_valuded_change, "UTF-8");
                finalHTML = encodedURL.replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "url encode failed : " + e.toString());
                return intent;
            }
        } else {
            Log.e(TAG, "failed to read the raw file");
        }

        StringBuilder sb = new StringBuilder("data:text/html,");
        sb.append(finalHTML);

        Log.v(TAG, "Uri parsed : " + Uri.parse(sb.toString()));
        intent.setData(Uri.parse(sb.toString()));

        Log.d(TAG, "HTTP POST URL intent created ");

        return intent;
    }

    private String readTrimRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder("");
        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line.trim());
            }
        } catch (IOException e) {
            Log.d(TAG, " Exception occurred : " + e.getMessage());
            return "";
        }
        return text.toString();
    }
    public void setExchangephonecapability(boolean isCheck) {
        ContentValues cv = new ContentValues();

        Log.i(TAG, "setExchangephonecapability " + "update Exchangephonecapability is isCheck = " + isCheck);

        if (isCheck) {
            cv.put("presence_eab_status", "true");
        } else {
            cv.put("presence_eab_status", "false");
        }
        int result = getContentResolver().update(CONTENT_URI, cv, null, null);

        Log.i (TAG, "setExchangephonecapability update result : " + result);
        getActivity().sendBroadcast(new Intent("com.lge.ims.eab.set_presence_status"));
        mCheckExchangephone.setChecked(isCheck);
        if (isCheck) {
            Toast.makeText(getActivity(),
                    R.string.settings_tether_exchange_turning_on, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),
                    R.string.settings_tether_exchange_turning_off, Toast.LENGTH_SHORT).show();
        }
    }

    public int getExchangephonecapability() {
        int isResut = PRESENCE_EAB_STATUS_FALSE;

        Cursor c = getContentResolver().query(CONTENT_URI, new String[] {"presence_eab_status"}, null, null, null);

        try {
            if (c != null ) {
                c.moveToFirst();
                String isCheck = c.getString(0);
                if (isCheck.equalsIgnoreCase("true")) {
                    isResut = PRESENCE_EAB_STATUS_TRUE;
                }
                c.close();
            } else {
                Log.i (TAG, "IMS DB null!!!");
                isResut = PRESENCE_EAB_STATUS_ERROR;
            }
        } catch (IllegalArgumentException e) {
            Log.e (TAG, "[presence_eab_status] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            isResut = PRESENCE_EAB_STATUS_ERROR;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e (TAG, "[presence_eab_status] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            isResut = PRESENCE_EAB_STATUS_ERROR;
        }

        Log.i (TAG, "getExchangephonecapability = " + isResut);
        return isResut;
    }
    public long getCheckStartTime() {
        SharedPreferences preferences = getActivity().getSharedPreferences("apps_prefs", 0);
        long starttime = preferences.getLong("StartTime", 0);
        Log.i (TAG, "getCheckStartTime = " + starttime);
        return starttime;
    }

    private void setEnableExchangePhoneMenuEnabled(int isChecking) {
       switch (isChecking) {
            case 1:
            case 2:
            case 3:
                  mCheckExchangephone.setEnabled(true);
                  Log.d (TAG, "setEnableExchangePhoneMenu-IMS reg ok!!!");
                  break;
            case 0:
            default:
                  mCheckExchangephone.setEnabled(false);
                  break;
       }
    }
    public void setEnableExchangePhoneMenu() {
        Cursor c = getContentResolver().query(IMS_REG_CONTENT_URI, new String[] {"uc_reg_state"}, null, null, null);
        int isChecking = -1;

        try {
            if (c != null ) {
                c.moveToFirst();
                isChecking = c.getInt(0);
                c.close();
            } else {
                Log.e (TAG, "uc_reg_state DB null!!!");
            }
        } catch (IllegalArgumentException e) {
            Log.e (TAG, "[uc_reg_state] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e (TAG, "[uc_reg_state] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
        }
        setEnableExchangePhoneMenuEnabled(isChecking);
        Log.d (TAG, "setEnableExchangePhoneMenu-uc_reg_state = " + isChecking);
    }

    public void setEnableExchangePhoneMenu_back() {
        Log.i (TAG, "setEnableExchangePhoneMenu ->");

        int isChecked = Settings.Secure.getInt(getActivity().getContentResolver(), SettingsConstants.Secure.DATA_NETWORK_ENHANCED_4G_LTE_MODE, 0);

        Log.d (TAG, "DATA_NETWORK_ENHANCED_4G_LTE_MODE =  " + isChecked);

        if (isChecked == 1) {
            boolean isAvDataRoaming = getDataRoaming();
            boolean isDataRoaming = Config.isDataRoaming();


            Log.d (TAG, "isAvDataRoaming =  " + isAvDataRoaming);
            Log.d (TAG, "isDataRoaming =  " + isDataRoaming);

            if (isAvDataRoaming) {
                mCheckExchangephone.setEnabled(true);
            } else {
                mCheckExchangephone.setEnabled(false);
            }

        } else {
            mCheckExchangephone.setEnabled(false);
        }

        Log.i (TAG, "<- setEnableExchangePhoneMenu ");
    }

    public void setCheckStartTime() {
        SharedPreferences preferences = getActivity().getSharedPreferences("apps_prefs", 0);
        long now = System.currentTimeMillis();
        Log.i (TAG, "setCheckStartTime = " + now);
        preferences.edit().putLong("StartTime", now).commit();
    }

    private boolean getDataRoaming() {
        final Context context = getActivity();
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Secure.getInt(resolver, Settings.Secure.DATA_ROAMING + Utils.getCurrentDDS(context), 0) != 0;
    }

    private boolean getTetherNetworkAirplaneModeVoLTE() {
       if (mAirplaneModeEnabler == null) {
           return false;
       } else {
           return mAirplaneModeEnabler.getAirplaneModeVoLTE();
       }
    }

  private void showSupportPCO_Dialog() {
      final Context context = getActivity();
      AlertDialog.Builder builder = new AlertDialog.Builder(
              context,
              com.lge.R.style.Theme_LGE_White_Dialog_Alert);

      builder.setTitle(R.string.pco_dialog_title);

      // C-1 content
      builder.setMessage(R.string.pco_dialog_message);

      // B-3 button
      builder.setPositiveButton(R.string.def_yes_btn_caption,
              new Dialog.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      prepareEnteringPortalVZW();

                      Intent intent = new Intent();
                      intent.setClassName("com.android.settings",
                              "com.android.settings.VerizonOnlinePortal");
                      startActivity(intent);
                  }
              });
      builder.setNegativeButton(R.string.user_setup_button_setup_later,
              new Dialog.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      Log.d(TAG, "pressed setNegativeButton");
                      int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);
                      if (pco_ims == 5) {
                          mPhone.setRadioPower(false);
                          Settings.Secure.putInt(getActivity().getContentResolver(),
                                     "radio_off_by_pco5", 1);
                      }
                  }
              });
      builder.setOnCancelListener(new Dialog.OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
              Log.d(TAG, "pressed cancel");
          }
      });

      AlertDialog alert = builder.create();
      alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
      alert.show();
  }

    private boolean IsSupportPCO() {
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);
        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);

        if (pco_internet == 3 || pco_ims == 5) {
            return true;
        } else {
            return false;
        }
    }
}
