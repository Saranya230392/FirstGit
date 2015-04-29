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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.net.NetworkInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//+++ BRCM
import android.bluetooth.BluetoothDevice;
import android.app.AlertDialog.Builder;
//--- BRCM
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;   //rebestm_KK_SMS
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.webkit.WebSettings;

import com.android.settings.deviceinfo.UsbSettingsControl;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException; //rebestm_KK_SMS
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
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
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.hardware.usb.UsbManager;
import android.app.Activity;
import java.util.ArrayList;
import android.net.Uri;
// [S] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch
//+++ BRCM
import java.util.List;
//--- BRCM
// [E] LGE_BT: ADD/ilbeom.kim/'12-12-20 - [GK] fixed tethering information mismatch

import com.lge.constants.SettingsConstants;
//+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.NsdEnabler;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lge.TetherSettingsHelp;
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.Utils;
/* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */
import com.android.settings.HotSpotPreference;
/* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */

//myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling
import com.android.settings.lge.WifiCallCheckBox;

import com.lge.constants.UsbManagerConstants;
import com.lge.os.Build;

//rebestm_KK_SMS
import android.graphics.drawable.Drawable;
import java.util.Collection;
import android.provider.Telephony.Sms.Intents;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;

// rebestm - HTTP POST intent added
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
//BT_S : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
import android.widget.CheckBox;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
//BT_E : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

import com.android.settings.Utils;
import com.lge.wifi.config.LgeWifiConfig;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.HashMap;
import java.util.Map;

import android.view.WindowManager;
//BT_S : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
import com.lge.bluetooth.LGBluetoothFeatureConfig;
//BT_E : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
public class TetherNetworkSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    //private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    // [shpark82.park] New VPN UI [START]
    private static final String KEY_VPN_SELECTOR = "vpn_selector";
    // [shpark82.park] New VPN UI [END]
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    //rebestm - kk migration
    private static final String KEY_MANAGE_MOBILE_PLAN = "manage_mobile_plan";
    private static final String KEY_MOBILE_NETWORK_SETTING_DUALSIM = "mobile_network_settings_dualsim";
    private static final String KEY_TOGGLE_NSD = "toggle_nsd"; //network service discovery
    //private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";
// rebestm - delete airplane category
//    private static final String KEY_AIRPLANE_CATEGORY = "airplane_category";
    private static final String KEY_TETHERING_CATEGORY = "tether_category";
    private static final String KEY_NETWORK_SETTING_CATEGORY = "network_setting_category";
    //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling
    private static final String KEY_WIFI_CALLING_CATEGORY = "wfc_category";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING

    //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec [s]
    private static final String CLASSNAME_MHP_PROVISION_FAILED_UPGRADE_DIALOG = "com.lge.mobilehotspot.ui.MHPProvisionFailActivity";

    //[minwoo2.kim, 2012/12/20] [e]
    public static final int REQUEST_CODE_EXIT_ECM = 1;
    private static final String KEY_EXCHANGE_PHONE = "exchage_phone_capability";
    //private static final String KEY_VZW_ACCOUNT = "view_verizon_account";
    // request CMCC
    private static final String KEY_CMCC_PM = "switch_pm";
    private static final String CMCC_DM = "cmcc_device_management";
// rebestm - Add SMS Warning popup
    private static final String VZWMSG_PACKAGENAME = "com.verizon.messaging.vzmsgs";
    private static final String LGSMS_PACKAGENAME = "com.android.mms";
    //private static final String HANGOUT_PACKAGENAME = "com.google.android.talk";

//LGE_ATT_WIFI_S, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot Setting menu
    private static final int ATT_DEFAULT = 0;
    private static final int ATT_TABLET_DEFAULT = 1;
//LGE_ATT_WIFI_E, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot Setting menu

// rebestm - delete airplane category
//    private PreferenceCategory mAirplaneModeCategory;
    private PreferenceCategory mTetheringCategory;
    private PreferenceCategory mNetworkSettingsCategory;
    //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling
    private PreferenceCategory mWiFiCallingCategory;

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private CheckBoxPreference mAirplaneModePreference;
    private NsdEnabler mNsdEnabler;
    private WifiApSwitchPreference mWifiTether; // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com]

    // USB tethering
    //private static final String KEY_USB_TETHER_SETTINGS_VZW = "usb_tether_settings_vzw";
    private static final String KEY_USB_TETHERING = "usb_tethering";
    private SettingsSwitchPreference mUsbTether;
    private String[] mUsbRegexs = {""};
    boolean mUsbAvailable = false;
    boolean mWifiAvailable = false;
    private boolean mUsbConnected;
    private boolean mMassStorageActive;
    private boolean mUsbTetherTurningOn = false;
    private boolean mCdromStorage = false;

    private static final String TETHERING_HELP = "tethering_help";
    private PreferenceScreen mTetherHelp;
    private static final String VIEW_VERIZON_ACCOUNT_TABLET = "view_verizon_account_tablet";
    private PreferenceScreen mViewVerizonAccount;
    /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */
    private HotSpotPreference mVZWTether;
    private static final String KEY_VZW_MOBILEHOTSPOT_CATEGORY = "vzw_tether_settings";
    /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */

    AlertDialog mTetherAlertDialog;
    private static final int DIALOG_TETHER_ALERT = 1;
    private static final int DIALOG_WIFI_ON = DIALOG_TETHER_ALERT + 1;
    //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec [s]
    private static final int DIALOG_WIFI_ON_BT = DIALOG_WIFI_ON + 1;
    //[minwoo2.kim, 2012/12/20] [e]
    private static final int DIALOG_MOBIL_DATA_ENABLED_ERROR = DIALOG_WIFI_ON_BT + 1;
    private static final int DIALOG_NO_SIM_ALERT = DIALOG_MOBIL_DATA_ENABLED_ERROR + 1;
    private static final int DIALOG_TETHER_ALERT_NEW = 100;
    private final int REQUEST_FIRST_TIME_USE_USB = DIALOG_NO_SIM_ALERT + 1;
    private static final int DIALOG_CHECK_WFC_USB = 300;

    public final static String SHARED_PREFERENCES_NAME = "first_time_use_Settings";
    public final static String EXTRA_FIRST_HELP_ASK = "help_ask";
    public final static String SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK = "wifi_help_ask";

// rebestm - HTTP POST intent added
    private static final String Production_URL_HTTPS_WO_QMARK = "https://quickaccess.verizonwireless.com/bbportal/oem/start.do";

    // TMUS - Upsell
    private Timer mTimer;
    //private boolean mCheckingMobileData = false;
    //private Handler mHandler;
    //private int loopCounting;
    private static final String  UPSELL_CHECK_USB_SUCCESS = "com.lge.upsell.Upsell.UPSELL_CHECK_USB_SUCCESS";
    private static final String  UPSELL_CHECK_USB_FAIL = "com.lge.upsell.Upsell.UPSELL_CHECK_USB_FAIL";
    public static final String IMS_REGISTRATION = "IMS_REGISTRATION";
    public static final String IMS_REG_STATUS = "IMS_REG_STATUS";

    private static final int INVALID             = -1;
    private static final int WIFI_TETHERING      = 0;
    private static final int USB_TETHERING       = 1;
    private static final int BLUETOOTH_TETHERING = 2;

    private static final int PRESENCE_EAB_STATUS_ERROR = -1;
    private static final int PRESENCE_EAB_STATUS_FALSE = 0;
    private static final int PRESENCE_EAB_STATUS_TRUE  = 1;


    private int mAvailableTetherNum = 0;
    private int mDefaultTether = WIFI_TETHERING;

    /* One of INVALID, WIFI_TETHERING, USB_TETHERING or BLUETOOTH_TETHERING */
    private int mTetherChoice = INVALID;

    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private static final int PROVISION_REQUEST = 0;
    //+s LGBT_KDDI_TETHERING_POP_UP sinyoung.jun@lge.com 2013-05-21
    private static final int TETHER_KDDI_REQUEST = 10;
    private static final String TETHER_POPUP_KDDI = "com.android.settings.deviceinfo.TetherPopupKDDIActivity";
    private static final String PACKAGE_NAME = "com.android.settings";
    //+e LGBT_KDDI_TETHERING_POP_UP

    private static final int ATT_ENTITLEMENT_CHECK_REQUEST = 100;
    private static final int USB_TETHER_USC_REQUEST = 101;
    private static final int USB_TETHER_KDDI_REQUEST = 102;

    //private boolean mCheckingEntitlement = false;

    private static final String TAG = "TetherNetworkSettings";
    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private /*CheckBoxPreference*/ SettingsSwitchPreference mBluetoothTether;
    private BluetoothPan mBluetoothPan;
    private boolean mBluetoothEnableForTether;
    private String[] mBluetoothRegexs;
    private boolean bluetoothAvailable = false;
    private static final int MHS_REQUEST = 2;
    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
    private boolean mIsProvisioned = false;
    //private boolean PROVISION = false; // provision app is currently not available.
    private boolean PROVISION = "VZW".equals(Config.getOperator());
    //    private boolean PROVISION = "VZW".equals(Config.getOperator()) && bluetoothAvailable;
    // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
    // kerry start
    //myeonghwan.kim@lge.com Delete data network for SKT
    //private static final String KEY_DATA_NETWORK_SETTINGS = "data_network_settings";

    //myeonghwan.kim@lge.com Delete data network for SKT
    //private PreferenceScreen mDataNetworkSetting;
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
    //rebestm - kk migration
    private static final int MANAGE_MOBILE_PLAN_DIALOG_ID = 1;
    private static final String SAVED_MANAGE_MOBILE_PLAN_MSG = "mManageMobilePlanMessage";
    private ConnectivityManager mCm;

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
     private View mInputView;
     private CheckBox mVisible;
     boolean mIsSecondaryUser;
     private UserManager mUm;

    private Preference mEmergencyAlert;
    private static final String KEY_EMERGENCY_ALERTS = "emergency_alert";
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode launch ECM app dialog
            startActivityForResult(
                    new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                    REQUEST_CODE_EXIT_ECM);
            return true;
        }
        else if (preference == mAirplaneModePreference && true == csActive) {
            //Toast.makeText(this, R.string.sp_warning_not_available_during_conversation_NORMAL, Toast.LENGTH_SHORT).show();
            //            Log.d("WirelessSettings", "onPreferenceTreeClick222");
            mAirplaneModeEnabler.updatePreferences();
            return true;
        }
        /* myeonghwan.kim@lge.com Delete data network for SKT [S]
        else if (preference == mDataNetworkSetting) { // kerry SKT SPEC - Data Network Settings [S]
            Intent mIntent = new Intent();
            mIntent.setClassName("com.android.settings", "com.android.settings.lgesetting.wireless.DataNetworkModeSetting");
            mIntent.putExtra("cancelable", "true");
            startActivity(mIntent);
            // kerry SKT SPEC - Data Network Settings [E]
            //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        }
        myeonghwan.kim@lge.com Delete data network for SKT [E] */
        else if (preference == mWifiTether || preference == mVZWTether) {
            Intent intent = new Intent();
            if (Config.getOperator().equals("DCM")) {
                intent.setAction("android.settings.TETHER_SETTINGS");
            } else {
                intent.setAction("android.settings.TETHER_SETTINGS_EXTERNAL");
            }
            startActivity(intent);
        }
        else if (preference == mTetherHelp) {
            Preference p = new Preference(getActivity());
            p.setTitle(R.string.help_label);
            Log.d (TAG, "mAvailableTetherNum = " + mAvailableTetherNum);
            Log.d (TAG, "mDefaultTether = " + mDefaultTether);
            //p.setIcon(R.drawable.shortcut_hotspot);
            if (mAvailableTetherNum > 1) {
                p.setFragment(TetherSettingsHelp.class.getName());
                ((PreferenceActivity)getActivity()).onPreferenceStartFragment(null, p);
            }
            else {
                Intent intent = new Intent();
                if (mDefaultTether == WIFI_TETHERING) {
                    intent.setClassName("com.lge.wifisettings", "com.lge.wifisettings.WifiHotspotHelp");
                    startActivity(intent);
                }
                else if (mDefaultTether == USB_TETHERING) {
                    intent.setClassName("com.android.settings", "com.android.settings.lge.TetherSettingsHelpUsb");
                    startActivity(intent);
                }
                else if (mDefaultTether == BLUETOOTH_TETHERING) {
                    // Go Bluetooth tethering help
                    intent.setClassName("com.lge.bluetoothsetting",
                    "com.lge.bluetoothsetting.LGBluetoothTetheringHelp");
                    startActivity(intent);
                }
            }
            return true;
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        } else if (preference == mWifiCallingEnabler) {
            Intent mIntent = new Intent();
            mIntent.setClassName("com.movial.wificall", "com.movial.wificall.Settings");
            startActivity(mIntent);
           //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]
        //rebestm - kk migration
        } else if (preference == findPreference(KEY_MANAGE_MOBILE_PLAN)) {
            onManageMobilePlanClick();
        }
       // rebestm - HTTP POST intent added
        else if (preference == mViewVerizonAccount) {
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
        }
        else if (preference == mCheckExchangephone) {
            int isResut = getExchangephonecapability();
            Log.d (TAG, "mCheckExchangephone is click = " + isResut);

            long startTime = getCheckStartTime();

            if ( startTime == 0 ) {
                setCheckStartTime();
            } else {
                long now = System.currentTimeMillis();
                long interval = now - startTime;

                if ( interval > CHECK_EXCHANGEPHONE_INTERVAL ) {
                    setCheckStartTime();
                    Log.d (TAG, "30s upper = " + interval);
                } else {
                    Log.d (TAG, "30s under = " + interval);
                    //ExchangeDialogWarning();
                    Toast.makeText(getActivity(),
                            R.string.settings_tether_exchange_wait, Toast.LENGTH_SHORT).show();
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
        } else if (KEY_MOBILE_NETWORK_SETTINGS.equals(preference.getKey())) {
            if (Config.getOperator().equals("DCM")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
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

      if (Utils.isAirplaneModeOn(getActivity().getApplicationContext())) {
          Log.d(TAG, "Airplane mode is in On");
          return;
      }

      if (pco_ims == 5) {
          mPhone.setRadioPower(true);
          mPhone.setDataEnabled(true);
          Settings.Secure.putInt(this.getContentResolver(),
                      "radio_off_by_pco5", 0);
      } else if (pco_internet == 3) {
          mPhone.setDataEnabled(true);
      }
  }

  //rebestm - kk migration
      private String mManageMobilePlanMessage;
      private static final String CONNECTED_TO_PROVISIONING_NETWORK_ACTION
              = "com.android.server.connectivityservice.CONNECTED_TO_PROVISIONING_NETWORK_ACTION";
      public void onManageMobilePlanClick() {
          Log.d(TAG, "onManageMobilePlanClick:");
          mManageMobilePlanMessage = null;
          Resources resources = getActivity().getResources();
          NetworkInfo ni = mCm.getProvisioningOrActiveNetworkInfo();
          if (mPhone.hasIccCard() && (ni != null)) {
              String url = mCm.getMobileProvisioningUrl();
              if (!TextUtils.isEmpty(url)) {
                  Intent intent = new Intent(CONNECTED_TO_PROVISIONING_NETWORK_ACTION);
                  intent.putExtra("EXTRA_URL", url);
                  Context context = getActivity().getBaseContext();
                  context.sendBroadcast(intent);
                  mManageMobilePlanMessage = null;
              } else {
                  // No provisioning URL
                  String operatorName = mPhone.getSimOperatorName();
                  if (TextUtils.isEmpty(operatorName)) {
                      // Use NetworkOperatorName as second choice in case there is no
                      // SPN (Service Provider Name on the SIM). Such as with T-mobile.
                      operatorName = mPhone.getNetworkOperatorName();
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
          } else if (mPhone.hasIccCard() == false) {
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
          }
          return super.onCreateDialog(dialogId);
      }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange preference = " + preference );
        if (preference == mUsbTether) {

            if ((Config.getOperator().equals(Config.TMO) || Config.getOperator().equals(Config.MPCS)) && Config.getCountry().equals("US")) {
            TelephonyManager tm = TelephonyManager.getDefault();
                if (!tm.hasIccCard()) {
                    onCreateTetherAlertDialg(DIALOG_NO_SIM_ALERT);
                    mUsbTether.setChecked(false);
                    return false;
                }
            }

            boolean newState = (Boolean)newValue ? true : false;

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-242][ID-MDM-75]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setUsbTetheringMenu(mUsbTether)) {
                return true;
            }
            // LGMDM_END
            if (newState) {
                //mUsbTether.setChecked(false);
                if (Config.getOperator().equals(Config.VZW)) {
                    WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                    if (mWifiManager != null) {
                        boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                        if (isWifiEnabled == true) {
                            Log.d("Tethersettings", "WiFi is ON! " );
                            mUsbTether.setChecked(false);
                            onCreateTetherAlertDialg(DIALOG_WIFI_ON);
                            return true;
                        }
                    }

                    if (PROVISION) {
                        if (mIsProvisioned) {
                            //processTurnOnOff();
                            setUsbTethering(newState);
                        }
                        else {
                            mTetherChoice = USB_TETHERING;
                            Intent proIntent = new Intent("com.lge.hotspot.provision_start");
                            startActivity(proIntent);
                            return true;
                        }
                    }
            else {  //  add case of "PROVISION is false"
            setUsbTethering(newState);
            }
                }
                else {
                    if ("MPCS_TMO".equals(SystemProperties.get("ro.build.target_operator_ext"))) {
                        if ( !getbFirstTimeUse()) {
                            Intent intent = new Intent();
                            intent.putExtra("Tethering_Type", "USB");
                            intent.setClassName("com.lge.upsell", "com.lge.upsell.FirstTimeUse");
                            startActivityForResult(intent, REQUEST_FIRST_TIME_USE_USB);
                        }
                        setUsbTethering(newState);
                    } else {
                        startProvisioningIfNecessary(USB_TETHERING);
                    }
                }
            } else {
                setUsbTethering(newState);
            }

            return true;
        } else if (preference == mBluetoothTether) {
            Log.d("CHRISWON", "mSwitchListener");

            boolean bluetoothTetherState = (Boolean)newValue ? true : false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
                return true;
            }
            // LGMDM_END
            //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec [s]
             if (Config.getOperator().equals(Config.VZW)) {
                if (bluetoothTetherState) {
                    WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                    if (mWifiManager != null) {
                        boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                        if (isWifiEnabled == true) {
                            Log.d("Tethersettings", "WiFi is ON! " );
                            mBluetoothTether.setChecked(false);
                            onCreateTetherAlertDialg(DIALOG_WIFI_ON_BT);
                            return true;
                        }
                    }

                    if (PROVISION) {
                        checkProvision();
                    } else {
                        gotoNextStep(bluetoothTetherState);
                    }
                }
                else {
                    gotoNextStep(bluetoothTetherState);
                }
    //+s LGBT_KDDI_TETHERING_POP_UP sinyoung.jun@lge.com 2013-05-21
            } else if (Config.getOperator().equals(Config.KDDI)){
                if(bluetoothTetherState){
                    int checkShow = Settings.System.getInt(this.getContentResolver(),"TETHER_POPUP_KDDI", 0);
                    if(checkShow == 0){
                                Intent send = new Intent(Intent.ACTION_MAIN);
                                send.setClassName(PACKAGE_NAME, TETHER_POPUP_KDDI);
                                startActivityForResult(send, TETHER_KDDI_REQUEST);
                    } else{
                        Log.d(TAG, "KDDI tethering TETHER_POPUP_KDDI is " + checkShow);
                        gotoNextStep(bluetoothTetherState);
                    }
               } else {
                   gotoNextStep(bluetoothTetherState);
               }
            }else
    //+e LGBT_KDDI_TETHERING_POP_UP
            //[minwoo2.kim, 2012/12/20] [e]
            // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
            //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec (Remove) [s]
            /*if (PROVISION && (bluetoothTetherState == true)) {
                checkProvision();
            } else*/
            //[minwoo2.kim, 2012/12/20] [e]
            {
                gotoNextStep(bluetoothTetherState);
            }
            // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
            return true;
        } else if (preference == mSmsApplicationPreference && newValue != null) {
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
            if ( oldpackageName.toString().equals(LGSMS_PACKAGENAME)
                    && (defaultPackage.equalsIgnoreCase(VZWMSG_PACKAGENAME) == false)) {
                final Intent intent = new Intent
                                        ("com.lge.settings.ACTION_CHANGE_DEFAULT_WARMING_POPUP");
                intent.putExtra(Intents.EXTRA_PACKAGE_NAME, newValue.toString());
                startActivity(intent);
            } else {
              SmsApplication.setDefaultApplication(newValue.toString(), getActivity());
              updateSmsApplicationSetting();
            }
            return true;
        } else if ( preference == mSwitchPM ) {
            // boolean bValue = (Boolean)newValue;
            boolean value = android.provider.Settings.System.getInt(
                    getActivity().getContentResolver(), CMCC_DM, 1) == 1 ? true : false;

            value = value == true ? false : true;

            Log.d (TAG, "onPreferenceChange = " + value);

            if (mSwitchPM != null) {
                mSwitchPM.setChecked(value);
                android.provider.Settings.System.putInt(getActivity()
                        .getContentResolver(), CMCC_DM, value == true ? 1 : 0);
            } else {
                Log.e (TAG, "mSwitch is null");
            }
            return true;
        }

        return false;
    }

    boolean isProvisioningNeeded() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            return false;
        }
        return mProvisionApp.length == 2;
    }

    private void startProvisioningIfNecessary(int choice) {
        mTetherChoice = choice;
        if (isProvisioningNeeded()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(mProvisionApp[0], mProvisionApp[1]);
            startActivityForResult(intent, PROVISION_REQUEST);
        } else {
            startTethering();
        }
    }

    private void startTethering() {
        switch (mTetherChoice) {
            case WIFI_TETHERING:
                break;
            case BLUETOOTH_TETHERING:
                break;
            case USB_TETHERING:
                if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
                    if (isUpsell()) {
                        //if (mWifiCallRegistered) {
                        //   onCreateTetherAlertDialg(DIALOG_CHECK_WFC_USB);
                        //} else {
                            startUpSellUSBTethering();
                        //}
                    }
                    else {
                        onCreateTetherAlertDialg(DIALOG_TETHER_ALERT);
                    }
                }
                else if (Config.getOperator().equals("ATT")
                    && (Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.TETHER_ENTITLEMENT_CHECK_STATE,
                            1) > 0)) {
                     //if (!mCheckingEntitlement) {
                        checkEntitlement();
                        //mCheckingEntitlement = true;
                    //}
                }
                else if (Config.getOperator().equals("USC")) {
                    startUsbTetherIntroPopup();
                }
                else if (Config.getOperator().equals("KDDI")
                    && !"KDDI_LCC".equals(SystemProperties.get("ro.lge.sub_operator"))) {
                    startTetherKDDIPopup();
                }
                else {
                    onCreateTetherAlertDialg(DIALOG_TETHER_ALERT);
                }
                //setUsbTethering(true);
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

        if (!Config.getOperator().equals(Config.VZW)) {
            UsbSettingsControl.setTetherStatus(getActivity(), false);

            if (enabled) {
                mUsbTetherTurningOn = true;
            }
            else {
                mUsbTetherTurningOn = false;
            }
        }
        Log.d(TAG, "[AUTORUN] TetherNetworkSettings USB Tethering!! enabled : " + enabled);
        if (cm.setUsbTethering(enabled) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
            Log.d(TAG, "[AUTORUN] ============ USB Tethering ERROR !! ============");
            mUsbTetherTurningOn = false;
            mUsbTether.setChecked(false);
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            return;
        }

        if (Config.getOperator().equals(Config.SPRINT)) {
            String userdebugmode = SystemProperties.get("persist.service.usb_ther", "");
            if (userdebugmode.contains("true")) {
                if (enabled) {
                    SystemProperties.set("persist.sys.usb.state", "on");
                } else {
                    SystemProperties.set("persist.sys.usb.state", "off");
                }
            }
        }

        mUsbTether.setSummary("");
        UsbSettingsControl.mTetherChanged = true;
    }

    private void onCreateTetherAlertDialg(int dialogId) {
        if (dialogId == DIALOG_TETHER_ALERT) {
            if (Utils.isSupportUSBMultipleConfig(getActivity())) {
                onCreateTetherAlertDialg(DIALOG_TETHER_ALERT_NEW);
            } else {
                AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sp_usb_tethering_NORMAL)
                .setMessage(R.string.sp_usb_tethering_desc_NORMAL)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            mUsbTether.setChecked(false);
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mUsbTether.setChecked(false);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUsbTether.setChecked(false);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setUsbTethering(true);

                        dialog.dismiss();
                    }
                });

                mTetherAlertDialog = altDialog.create();
                mTetherAlertDialog.show();
            }
        }
        else if (dialogId == DIALOG_WIFI_ON) {
            mUsbTether.setChecked(false);
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity());
            altDialog.setCancelable(true);
            altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            altDialog.setTitle(R.string.sp_notify_wifi_to_usbtethering_title_NORMAL);
            altDialog.setMessage(R.string.sp_notify_wifi_to_usbtethering_message_NORMAL);
            altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mUsbTether.setChecked(false);
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            altDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mUsbTether.setChecked(false);
                    dialog.dismiss();
                }
            });
            altDialog.setPositiveButton(R.string.def_yes_btn_caption, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    disableWifi();
                    mUsbTether.setChecked(true);
                    //onPreferenceTreeClick(null, mUsbTether);
                    onPreferenceChange(mUsbTether, true);
                    dialog.dismiss();
                }
            });
            altDialog.setNegativeButton(R.string.def_no_btn_caption, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mUsbTether.setChecked(false);
                    dialog.dismiss();
                }
            });
            mTetherAlertDialog = altDialog.create();
            mTetherAlertDialog.show();
        }
        //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec [s]
         else if (dialogId == DIALOG_WIFI_ON_BT) {
            mBluetoothTether.setChecked(false);
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity());
            altDialog.setCancelable(true);
//BT_S : TD36163 Don't need to display alert dialog icon 20140429, [START]
/*            altDialog.setIconAttribute(android.R.attr.alertDialogIcon); */
//BT_E : TD36163 Don't need to display alert dialog icon 20140429, [END]
            altDialog.setTitle(R.string.sp_notify_wifi_to_usbtethering_title_NORMAL);
            altDialog.setMessage(R.string.sp_notify_wifi_to_bt_message_NORMAL);
            altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mBluetoothTether.setChecked(false);
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            altDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mBluetoothTether.setChecked(false);
                    dialog.dismiss();
                }
            });
            altDialog.setPositiveButton(R.string.def_yes_btn_caption, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    disableWifi();
                    mBluetoothTether.setChecked(true);
                    //onPreferenceTreeClick(null, mUsbTether);
                    onPreferenceChange(mBluetoothTether, true);
                    dialog.dismiss();
                }
            });
            altDialog.setNegativeButton(R.string.def_no_btn_caption, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mBluetoothTether.setChecked(false);
                    dialog.dismiss();
                }
            });
            mTetherAlertDialog = altDialog.create();
            mTetherAlertDialog.show();
        }
        //[minwoo2.kim, 2012/12/20] [e]
        else if (dialogId == DIALOG_MOBIL_DATA_ENABLED_ERROR) {
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity());
            altDialog.setTitle(R.string.wifi_error);
            altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            altDialog.setCancelable(false);
            altDialog.setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ;
                } } );
            altDialog.setMessage(R.string.data_enable_message);
            altDialog.create().show();
        }

        else if (dialogId == DIALOG_NO_SIM_ALERT) {
            AlertDialog.Builder noSimAlertDialog = new AlertDialog.Builder(getActivity());
            noSimAlertDialog.setTitle(R.string.no_sim_card);
            noSimAlertDialog.setMessage(R.string.no_sim_card_message);
            noSimAlertDialog.setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            noSimAlertDialog.create().show();
        }

        else if (dialogId == DIALOG_CHECK_WFC_USB) {
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.check_wfc_title)
            .setMessage(R.string.check_wfc_desc_usb)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
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
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mUsbTether.setChecked(false);
                    dialog.dismiss();
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    disableWifi();
                    startUpSellUSBTethering();
                    dialog.dismiss();
                }
            });

            mTetherAlertDialog = altDialog.create();
            mTetherAlertDialog.show();
        }
        else if (dialogId == DIALOG_TETHER_ALERT_NEW) {
            int currentValue = Settings.System.getInt(getActivity().getContentResolver(),
                "usb_tether_do_not_show", 0);
            if (currentValue == 1) {
                setUsbTethering(true);
                return;
            }

            LayoutInflater factory = LayoutInflater.from(getActivity());
            mInputView = factory.inflate(R.layout.usb_tether_do_not_show, null);
            mVisible = (CheckBox)mInputView.findViewById(R.id.usb_tether_check);
            mVisible.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                }
            });
            AlertDialog.Builder altDialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.sp_usb_tethering_NORMAL)
            .setView(mInputView)
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mUsbTether.setChecked(false);
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mUsbTether.setChecked(false);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mUsbTether.setChecked(false);
                    dialog.dismiss();
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (mVisible.isChecked()) {
                        Settings.System.putInt(getActivity().getContentResolver(),
                            "usb_tether_do_not_show", 1);
                    }
                    setUsbTethering(true);
                    dialog.dismiss();
                }
            });

            mTetherAlertDialog = altDialog.create();
            mTetherAlertDialog.show();
        }
    }

    public void startUpSellUSBTethering() {
        String operator_numeric = SystemProperties.get(
            TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        Log.d(TAG, "[AUTORUN] isTmusSimCard " + operator_numeric);
        if (isTmusSimCard(operator_numeric)) {
            connectUpsell(USB_TETHERING);
        } else {
            if ( !getbFirstTimeUse()) {
                Intent intent = new Intent();
                intent.putExtra("Tethering_Type", "USB");
                intent.setClassName("com.lge.upsell",
                    "com.lge.upsell.FirstTimeUse");
                startActivityForResult(intent, REQUEST_FIRST_TIME_USE_USB);
            }
            setUsbTethering(true);
        }
    }

    public void disableWifi() {
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            //boolean isWifiEnabled = mWifiManager.isWifiEnabled();
//          if (DBG) {
//              Log.e(TAG, isWifiEnabled ? "WiFi WifiOffloading changed to False": "WiFi WifiOffloading changed to True");
//          }
            mWifiManager.setWifiEnabled(false);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //rebestm - kk migration
        mCm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (savedInstanceState != null) {
            mManageMobilePlanMessage = savedInstanceState.getString(SAVED_MANAGE_MOBILE_PLAN_MSG);
        }

        mUm = (UserManager)getSystemService(Context.USER_SERVICE);


        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = null;

        // Remove Mobile Network Settings if it's a wifi-only device.
        b_isWifiOnly = Utils.isWifiOnly(getActivity());
        mIsSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
        Log.i (TAG, "b_isWifiOnly = " + b_isWifiOnly);
        Log.i (TAG, "mIsSecondaryUser = " + mIsSecondaryUser);

        final Activity activity = getActivity();
        if (!(Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2)
            && ("SPR".equals(Config.getOperator())
            || "TRF".equals(Config.getOperator())
            || "VZW".equals(Config.getOperator())
            || "CTC".equals(Config.getOperator())
            || "CTO".equals(Config.getOperator())
            || ("ATT".equals(Config.getOperator()) && Utils.isTablet())
            || "AIO".equals(Config.getOperator())
            || "CRK".equals(SystemProperties.get("ro.build.target_operator"))
            || "vfp".equals(android.os.Build.DEVICE)
            || "vfpv".equals(android.os.Build.DEVICE)
            || Utils.isSBMUserBuild())) {
             activity.getActionBar().setIcon(R.mipmap.ic_launcher_settings);

            if (Utils.isTablet() || Utils.isWifiOnly(getActivity())) {
                activity.getActionBar().setTitle(R.string.settings_label);
            } else {
                activity.getActionBar().setTitle(R.string.wireless_networks_settings_title);
            }
        } else {
            if (Utils.isTablet() || Utils.isWifiOnly(getActivity())) {
                activity.getActionBar().setTitle(R.string.settings_label);
            } else {
                if ("VZW".equals(Config.getOperator())) {
                    activity.getActionBar().setTitle(R.string.wireless_more_settings_title);
                } else {
                    activity.getActionBar().setTitle(R.string.tether_network_label);
                }
            }
        }

        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        boolean isAirplaneGone = false;
        if (Config.supportAirplaneListMenu()) {
            isAirplaneGone = true;
        }

        /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */
        if (Config.getOperator().equals(Config.VZW)) {
            addPreferencesFromResource(R.xml.tether_network_settings_vzw);
        } else {
        /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */
            addPreferencesFromResource(R.xml.tether_network_settings);
        }

        if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2
            && !"VZW".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHERING_CATEGORY));
            getPreferenceScreen().removePreference(findPreference(KEY_NETWORK_SETTING_CATEGORY));
            getPreferenceScreen().removePreference(findPreference(KEY_WIFI_CALLING_CATEGORY));
        } else {
            mTetheringCategory = (PreferenceCategory)findPreference(KEY_TETHERING_CATEGORY);
            mNetworkSettingsCategory = (PreferenceCategory)findPreference(KEY_NETWORK_SETTING_CATEGORY);
            mWiFiCallingCategory = (PreferenceCategory)findPreference(KEY_WIFI_CALLING_CATEGORY);
        }
        mCheckExchangephone = (CheckBoxPreference)findPreference(KEY_EXCHANGE_PHONE);

        boolean isUCE = getPackageManager().hasSystemFeature("com.lge.ims.service.eab");
        Log.d (TAG, "isUCE = " + isUCE);

        if (mNetworkSettingsCategory != null) {
            Log.d (TAG, "Delete Exchangephone menu111");
            mNetworkSettingsCategory.removePreference(mCheckExchangephone);
            mCheckExchangephone = null;
        }

        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter != null) {
            adapter.getProfileProxy(activity.getApplicationContext(), mProfileServiceListener, BluetoothProfile.PAN);
        }
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        if (mNetworkSettingsCategory != null) { //wbt
            updateAirplaneModeState(isAirplaneMode == 1 ? true : false);
        }
        // WBT
        CheckBoxPreference nsd = null;

        if (mNetworkSettingsCategory != null) {
            mAirplaneModePreference = (CheckBoxPreference)mNetworkSettingsCategory.findPreference(KEY_TOGGLE_AIRPLANE);
            nsd = (CheckBoxPreference)mNetworkSettingsCategory.findPreference(KEY_TOGGLE_NSD);
        }

        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        mBluetoothTether = /*(CheckBoxPreference)*/(SettingsSwitchPreference)findPreference(ENABLE_BLUETOOTH_TETHERING);
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
        mWifiTether = (WifiApSwitchPreference)findPreference(KEY_TETHER_SETTINGS);
        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]

        if (isAirplaneGone == false && mNetworkSettingsCategory != null) {
            mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference, null);
        }
        else if (isAirplaneGone == true && mNetworkSettingsCategory != null) {
            mNetworkSettingsCategory.removePreference(findPreference(KEY_TOGGLE_AIRPLANE));
        }

// rebestm_KK_SMS
        if (mNetworkSettingsCategory != null) {
            mSmsApplicationPreference = (AppListPreference)findPreference(KEY_SMS_APPLICATION);
            mSmsApplicationPreference.setOnPreferenceChangeListener(this);
            initSmsApplicationSetting();
        }
        // Remove NSD checkbox by default
        if ( (nsd != null) && (mNetworkSettingsCategory != null) ){
            mNetworkSettingsCategory.removePreference(nsd);
        }

        mProvisionApp = getResources().getStringArray(com.android.internal.R.array.config_mobile_hotspot_provision_app);

        if ((b_isWifiOnly || mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) && (mNetworkSettingsCategory != null)) {
            Log.d("settings", "[TetherNetworkSettings] is Wi-Fi only Device - delete Network settings ");
            mNetworkSettingsCategory.removePreference(mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTINGS));
        }

        // Enable Proxy selector settings if allowed.
        if (mNetworkSettingsCategory != null) {
            mGlobalProxy = mNetworkSettingsCategory.findPreference(KEY_PROXY_SETTINGS);
        }

        DevicePolicyManager mDPM = (DevicePolicyManager)
            activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support

        if ( (mNetworkSettingsCategory != null) && (mGlobalProxy != null) ) {
            mNetworkSettingsCategory.removePreference(mGlobalProxy);
        }

        if (mGlobalProxy != null) {
            mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);
        }

        // Disable Tethering if it's not allowed or if it's a wifi-only device
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        mUsbRegexs = cm.getTetherableUsbRegexs();
        mUsbTether = (SettingsSwitchPreference)findPreference(KEY_USB_TETHERING);
        mUsbTether.setOnPreferenceChangeListener(this);
        mUsbTether.setDivider(false);
        if ((Config.getCountry().equals("US") && (Config.getOperator().equals(Config.TMO) || Config.getOperator().equals(Config.MPCS)))
                || Config.getOperator().equals(Config.VZW)
                || Config.getOperator().equals(Config.SPRINT)
                || Config.getOperator().equals(Config.BM)
                || !UsbSettingsControl.isNotSupplyUSBTethering(getActivity())) {
            mUsbAvailable = mUsbRegexs.length != 0;

            if (Config.getOperator().equals(Config.VZW) && mUsbAvailable) {
                mUsbTether.setTitle(R.string.sp_mobile_broadband_connection_NORMAL);
            }
            if ((Config.getOperator().equals(Config.SPRINT) || Config.getOperator().equals(Config.BM)) && !Utils.getChameleonUsbTetheringMenuEnabled()) {
                mUsbAvailable = false;
            }
            if ("w3c_vzw".equals(SystemProperties.get("ro.product.name")) || "w5c_vzw".equals(SystemProperties.get("ro.product.name"))) {
                mUsbAvailable = false;
            }
        }
        if (!mUsbAvailable || Utils.isMonkeyRunning() || mIsSecondaryUser || !cm.isTetheringSupported() || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
            getPreferenceScreen().removePreference(mUsbTether);
            mUsbAvailable = false;
        }

        //[LGE_UPDATE_S] 20130313, BT tethering menu seodongjo@lge.com, [START]
        mBluetoothRegexs = cm.getTetherableBluetoothRegexs();
        String ispan = SystemProperties.get("bluetooth.pan", "false");
        if ("true".equals(ispan)) {
            bluetoothAvailable = true;
        }
//*s LGBT_TMUS_BT_TETHERING_DISABLE, [hyuntae0.kim@lge.com 2013.06.18]
        if ("US".equals(Config.getCountry()) && ("TMO".equals(Config.getOperator()) || "MPCS".equals(Config.getOperator()))) {
            bluetoothAvailable = false;
        }
        if (Utils.isSBMUserBuild()) {
            bluetoothAvailable = false;
        }
//*e LGBT_TMUS_BT_TETHERING_DISABLE

        Log.d (TAG, "bluetoothAvailable = " + bluetoothAvailable);
        if (!bluetoothAvailable || mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
            getPreferenceScreen().removePreference(mBluetoothTether);
            bluetoothAvailable = false;
        } else {
            if (mBluetoothPan != null && mBluetoothPan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
            } else {
                mBluetoothTether.setChecked(false);
            }
            if (mBluetoothTether != null) {
                mBluetoothTether.setDivider(false);
                mBluetoothTether.setOnPreferenceChangeListener(this);
            }
        }
        Log.d(TAG, " cm.isTetheringSupported()  " + cm.isTetheringSupported());
        //[LGE_UPDATE_E] 20130313, seodongjo@lge.com, [END]

        if (!cm.isTetheringSupported() || b_isWifiOnly || mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)
           || (("ATT".equals(Config.getOperator())) && (SystemProperties.getInt("wlan.lge.atthotspot", ATT_DEFAULT) == ATT_TABLET_DEFAULT))) {
           //LGE_ATT_WIFI, [jeongwook.kim@lge.com], 2014-05-15, Hide Mobile Hotspot Setting menu
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
            if (getPreferenceScreen().findPreference("mobile_hotspot_vzw") != null) {
                getPreferenceScreen().removePreference(findPreference("mobile_hotspot_vzw"));
               }
        } else {
            //[Wi-Fi Settings]_S [andrew75.kim@lge.com] 20120305 for MobileHotspot VZW
            if ( "VZW".equals(Config.getOperator()) || "TRF".equals(Config.getOperator())
                || "AIO".equals(Config.getOperator()) || "CRK".equals(SystemProperties.get("ro.build.target_operator"))) {
                // LGE_CHANGE_S : support usb tether(Mobile Broadbanc Connect) at VZW, on 20120401, by infomagic
                //Preference p = findPreference(KEY_TETHER_SETTINGS);
                Log.d("WirelessSettings", "remove Preference()");

                if (getPreferenceScreen().findPreference(KEY_TETHER_SETTINGS) != null) {
                    getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
                }
                // LGE_CHANGE_E : support usb tether(Mobile Broadbanc Connect) at VZW, on 20120401, by infomagic
            }
            else
            //[Wi-Fi Settings]_E [andrew75.kim@lge.com] 20120305 for MobileHotspot VZW
            {
                //[s][chris.won@lge.com][2013-04-30] VZW Mobile hostpot menu
                if (getPreferenceScreen().findPreference("mobile_hotspot_vzw") != null) {
                    getPreferenceScreen().removePreference(findPreference("mobile_hotspot_vzw"));
                }
                //[e][chris.won@lge.com][2013-04-30] VZW Mobile hostpot menu
                Preference p = findPreference(KEY_TETHER_SETTINGS);
                // origin : p.setTitle(Utils.getTetheringLabel(cm));
                if (true == "DCM".equals(Config.getOperator()))//[Wi-Fi Settings]_S [andrew75.kim@lge.com] 2013-03-14 Add the Wi-Fi tethering setting for Docomo.
                {
                    p.setTitle(R.string.sp_tethering_title_jp_NORMAL);
                    p.setSummary(R.string.sp_tether_summary_DCM_NORMAL);
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-12-07, change the token for ATT*/
                } else if (true == "ATT".equals(Config.getOperator())) {
                    p.setTitle(R.string.sp_mobile_hotspot_NORMAL);
                    p.setSummary(R.string.sp_wifi_hotspot_summary_NORMAL);
/*LGE_CHANGE_E, [jongpil.yoon@lge.com], 2012-12-07, change the token for ATT*/
                } else if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
                    p.setTitle(R.string.wifi_tether_checkbox_tmus_text);
                    p.setSummary(R.string.sp_wifi_hotspot_summary_NORMAL);
                } else {
                    p.setTitle(R.string.sp_wifi_hotspot_NORMAL); // LG-HUSIS JB Guide, 20121122.neo-wifi@lge.com[sangheon.shim], Poratble hotspot --> Portable Wi-Fi hotspot.
                }
                mWifiAvailable = true;

                if (Utils.isSBMUserBuild()) {
                    if (getPreferenceScreen().findPreference(KEY_TETHER_SETTINGS) != null) {
                        getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
                        mWifiAvailable = false;
                    }
                }

             }
        }

        // kerry start for VZW
        if ( "VZW".equals(Config.getOperator()) && (mNetworkSettingsCategory != null) && (!b_isWifiOnly) && (!mIsSecondaryUser) && (!mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS))) {
            mMobileNetworkSettings = (PreferenceScreen) mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTINGS);
            if (SystemProperties.getInt("ro.telephony.default_network", 0) == 4) {
                mMobileNetworkSettings.setSummary(R.string.sp_mobile_network_contens_NORMAL);
            } else if (Utils.isTablet()) {
                mMobileNetworkSettings.setSummary(R.string.settings_tether_netwokr_sumary_noroaming);
            } else {
                mMobileNetworkSettings.setSummary(R.string.sp_network_settings_summary_NORMAL);
            }
        }
        /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */
        if ( "VZW".equals(Config.getOperator())) {
            mVZWTether = (HotSpotPreference)findPreference(KEY_VZW_MOBILEHOTSPOT_CATEGORY);

            if ((android.provider.Settings.System.getInt(this.getContentResolver(),
                SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0) == 1) || mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
                Log.d(TAG, "##feature remove Mobile Hotspot");
                getPreferenceScreen().removePreference(mVZWTether);
            }
        }
        /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2013-08-07, Mobile Hotspot Setting Switch */


        if (0 == isAirplaneMode && isAirplaneGone == false && mAirplaneModePreference != null) {
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
        // kerry - for qm req end

        /* [S][2012.2.17] kyochun.shin@lge.com mobileSetting for Dual SIM or Single SIM*/

        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (mNetworkSettingsCategory != null) {
            PreferenceScreen mobileNetworkSettings;
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                mobileNetworkSettings = (PreferenceScreen) mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTINGS);
            } else {
                mobileNetworkSettings = (PreferenceScreen) mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM);
            }

            if (mobileNetworkSettings != null) {
                mNetworkSettingsCategory.removePreference(mobileNetworkSettings);
            }
        }
        /* [E][2012.2.17] kyochun.shin@lge.com mobileSetting for Dual SIM or Single SIM*/

// rebestm_KK_SMS
        if (mNetworkSettingsCategory != null) {
            if (isSmsSupported() == false || (Utils.isUI_4_1_model(getActivity()) && !"VZW".equals(Config.getOperator()))) {
                mNetworkSettingsCategory.removePreference(findPreference(KEY_SMS_APPLICATION));
            }
        }
// rebestm_temp_PLNA_block
        if ( true && mNetworkSettingsCategory != null ) {
            mNetworkSettingsCategory.removePreference(findPreference(KEY_MANAGE_MOBILE_PLAN));
        }

        mViewVerizonAccount = null;
        if (Config.getOperator().equals(Config.VZW) && mNetworkSettingsCategory != null) {
            mViewVerizonAccount = (PreferenceScreen)findPreference(VIEW_VERIZON_ACCOUNT_TABLET);
            if (Utils.isTablet()) {
                Log.d (TAG, "support View Verizon Account menu");
            }

            if (Utils.isTablet() == false || mIsSecondaryUser) {
                Log.d (TAG, "delete a ViewVerizonAccount menu");
                mNetworkSettingsCategory.removePreference(findPreference(VIEW_VERIZON_ACCOUNT_TABLET));
                mViewVerizonAccount = null;
            }
        }

        // For Tethering Help menu
        mTetherHelp = (PreferenceScreen)findPreference(TETHERING_HELP);
        mAvailableTetherNum = 0;
        if (mUsbAvailable) {
            Log.d (TAG, "mUsbAvailable ok");
            mAvailableTetherNum++;
            mDefaultTether = USB_TETHERING;
        }
        if (bluetoothAvailable) {
            Log.d (TAG, "bluetoothAvailable ok");
            mAvailableTetherNum++;
            mDefaultTether = BLUETOOTH_TETHERING;
        }
        if (mWifiAvailable) {
            Log.d (TAG, "mWifiAvailable ok");
            mAvailableTetherNum++;
            mDefaultTether = WIFI_TETHERING;
        }

        if ("w3c_vzw".equals(SystemProperties.get("ro.product.name")) || "w5c_vzw".equals(SystemProperties.get("ro.product.name"))) {
            mAvailableTetherNum = 2;
        }

        Log.d (TAG, "mAvailableTetherNum = " + mAvailableTetherNum  );


        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        if (mWiFiCallingCategory != null) {
            mWifiCallingEnabler = (WifiCallCheckBox)mWiFiCallingCategory.findPreference(BUTTON_WIFI_CALLING_KEY);
            mIsWifiCallingSupport = WifiCallCheckBox.isSupport();
            Log.i (TAG, "mIsWifiCallingSupport = " + mIsWifiCallingSupport);
            if (!mIsWifiCallingSupport) {
                if (mWifiCallingEnabler != null) {
                    mWiFiCallingCategory.removePreference(findPreference(BUTTON_WIFI_CALLING_KEY));
                    mWifiCallingEnabler = null;
                }
            }
        }
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]


        if (mAvailableTetherNum <= 0 || Utils.isWifiOnly(getActivity()) || mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING)) {
            Preference summary = findPreference("lg_category_summary");
            getPreferenceScreen().removePreference(mTetherHelp);
            if (summary != null) {
                getPreferenceScreen().removePreference(summary);
            }
        }

        // [shpark82.park] New VPN UI [START]
        if (mNetworkSettingsCategory != null) { // cgyu.yu@lge.com 2013.08.29 WBT #508977 Null Pointer Dereference fixed
            if (mIsSecondaryUser || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN)) {
                mNetworkSettingsCategory.removePreference(findPreference(KEY_VPN_SETTINGS));
                mNetworkSettingsCategory.removePreference(findPreference(KEY_VPN_SELECTOR));
            } else {
                if (Utils.isSupportVPN(getActivity())) {
                    mNetworkSettingsCategory.removePreference(findPreference(KEY_VPN_SETTINGS));
                } else {
                    mNetworkSettingsCategory.removePreference(findPreference(KEY_VPN_SELECTOR));
                    }
                }
        }
        // [shpark82.park] New VPN UI [END]

        if (mNetworkSettingsCategory != null) {
            mEmergencyAlert = findPreference(KEY_EMERGENCY_ALERTS);
            if (mEmergencyAlert != null) {
                if (!isSupportEmergencyAlerts(getActivity()) || !Config.getOperator().equals("ATT")) {
                    Log.d(TAG, "This device doesn't support EmergencyAlert");
                    mNetworkSettingsCategory.removePreference(mEmergencyAlert);
                }
            }
        }

        mSwitchPM = (PMSwitchPreference)findPreference(KEY_CMCC_PM);

        boolean isCMCCPM = false;    // Not supported by CMCC from L OS

        if (mNetworkSettingsCategory != null) {
            if (!Utils.isUI_4_1_model(getActivity()) || !isCMCCPM) {
                Log.d (TAG, "Delete mSwitchPM menu");
                 mNetworkSettingsCategory.removePreference(mSwitchPM);
                 mSwitchPM = null;
            } else {
                mSwitchPM.setOnPreferenceChangeListener(this);
            }
        }

        if (mTetheringCategory != null && mAvailableTetherNum <= 0) {
            getPreferenceScreen().removePreference(mTetheringCategory);
        }

        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        if (mWiFiCallingCategory != null && mWiFiCallingCategory.getPreferenceCount() <= 0) {
            getPreferenceScreen().removePreference(mWiFiCallingCategory);
        }
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]

        if (mNetworkSettingsCategory != null && mNetworkSettingsCategory.getPreferenceCount() <= 0) {
            getPreferenceScreen().removePreference(mNetworkSettingsCategory);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250][ID-MDM-282]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addTetherPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
            MDMSettingsAdapter.getInstance().setVpnMenu(
                    getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
            MDMSettingsAdapter.getInstance().setVpnMenu(
                    getPreferenceScreen().findPreference(KEY_VPN_SELECTOR));
        }
        // LGMDM_END
        mIsFirst = true;
    }

    @Override
    public void onStart() {
        super.onStart();

        Activity activity = getActivity();

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
        if (PROVISION) {
            filter.addAction("com.lge.hotspotprovision.STATE_CHANGED");
        }
        // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
        //+s LGBT_ENTITLEMENT_FOR_TETHERING, younghyun.kwon@lge.com, 20130422, To check Entitlement of BT Tethering for AT&T
        if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
            filter.addAction(BT_ENTITLEMENT_CHECK_OK);
            filter.addAction(BT_ENTITLEMENT_CHECK_FAILURE);
        }
        //+e LGBT_ENTITLEMENT_FOR_TETHERING
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        activity.registerReceiver(mTetherChangeReceiver, filter);

        // [S] TMUS : Update for Upsell
        if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
            filter = new IntentFilter();
            filter.addAction(UPSELL_CHECK_USB_SUCCESS);
            filter.addAction(UPSELL_CHECK_USB_FAIL);
            filter.addAction(IMS_REGISTRATION);
            activity.registerReceiver(mTetherChangeReceiver, filter);
        }
        // [E] TMUS : Update for Upsell

        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
        if (mWifiTether != null) {
            mWifiTether.resume();

            //+s setSummary with updateWifiApState
            //WifiManager mWifiManager = (WifiManager)activity.getSystemService(Context.WIFI_SERVICE);
            filter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
            activity.registerReceiver(mTetherChangeReceiver, filter);
            //+e setSummary with updateWifiApState
        }
        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]


        if (mVZWTether != null) {
            if (!Utils.isLUpgradeModelForMHP()) {
                mVZWTether.resume();
            }

            //WifiManager mWifiManager = (WifiManager)activity.getSystemService(Context.WIFI_SERVICE);
            filter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
            activity.registerReceiver(mTetherChangeReceiver, filter);
        }

        // rebestm_KK_SMS
        if (mSmsApplicationPreference != null) {
            initSmsApplicationSetting();
        }
        if (mSwitchPM != null) {
            mSwitchPM.resume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
        if (mWifiTether != null) {
            mWifiTether.pause();
        }
        // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]

        Activity activity = getActivity();

        //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
        activity.unregisterReceiver(mTetherChangeReceiver);
        // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
        if (PROVISION) {
            mIsProvisioned = false;
        }
        // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTimer != null ) {
            mTimer.cancel();
            mTimer = null;
        }

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

    @Override
    public void onResume() {
        super.onResume();
//ask130402 :: +
        Utils.set_TelephonyListener(mPhone, mPhoneStateListener);
//ask130402 :: -
        //[S][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        int isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        //[E][chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        Activity activity = getActivity();

        mMassStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());

        updateAirplaneModeState(isAirplaneMode == 1 ? true : false); //[chris.won@lge.com][2012-07-23] Add Airplane status change code for onResume()
        activity.registerReceiver(mAirplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

        if (bluetoothAvailable) {
            updateState();
        }
        //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
        if (mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE || getTetherNetworkAirplaneModeVoLTE()) {
            csActive = true;
        } else {
            csActive = false;
        } // kerry

        if (mVZWTether != null) {
            mVZWTether.resume();
        }

        if (mAirplaneModeEnabler != null) {
            mAirplaneModeEnabler.resume();
        }

        if (mNsdEnabler != null) {
            mNsdEnabler.resume();
        }

        /* myeonghwan.kim@lge.com Delete data network for SKT [S]
        tm = TelephonyManager.getDefault();
        if ("SKT".equals(Config.getOperator())){
            if (tm != null) {
                if (tm.isNetworkRoaming() || (Utils.hasReadyMobileRadio(getActivity()) == false)) {
                    mDataNetworkSetting.setEnabled(false);
                } else {
                    mDataNetworkSetting.setEnabled(true);
                }
            }
        }
        myeonghwan.kim@lge.com Delete data network for SKT [E] */
        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS) && (mNetworkSettingsCategory != null)) {
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                if ((isAirplaneMode == 1) || (Utils.hasReadyMobileRadio(getActivity()) == false) ) {
                    mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(false);
                } else {
                    mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(true);
                }
            } else {
                if (("VZW".equals(Config.getOperator()) || "SPR".equals(Config.getOperator()))
                        && isAirplaneMode == 0 ) {
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
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[S]
        if (mIsWifiCallingSupport && mWifiCallingEnabler != null) {
            mWifiCallingEnabler.resume();
        }
        //myeonghwan.kim@lge.com 20130214 Add TMUS WiFi Calling[E]

        updateState();

        if (true == Config.isVZWAdminDisabled(getActivity().getApplicationContext())) {
            if (mAirplaneModePreference != null) {
                mAirplaneModePreference.setEnabled(false);
            }
            //ask130313 :: it disabled the MobileNetworkSetting Menu item in VZW.
        if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS) && (mNetworkSettingsCategory != null)) {
            mMobileNetworkSettings.setEnabled(false);
        }
            Log.d(TAG, "onCreate :: mMobileNetworkSettings value = false");
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setWifiApEnablerMenu(mWifiTether);
            if (Config.VZW.equals(Config.getOperator())) {
                MDMSettingsAdapter.getInstance().setWifiApEnablerMenu(
                        findPreference(KEY_VZW_MOBILEHOTSPOT_CATEGORY));
            }
        }
        // LGMDM_END
        if (mSmsApplicationPreference != null) {
            updateSmsApplicationSetting();
        }
        if ( mCheckExchangephone != null && ( mNetworkSettingsCategory != null)) {
            int result = getExchangephonecapability();

            switch (result) {
            case PRESENCE_EAB_STATUS_ERROR:
                mNetworkSettingsCategory.removePreference(mCheckExchangephone);
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

        if ( mViewVerizonAccount != null ) {
            mViewVerizonAccount.setEnabled(
                    Utils.isEnableCheckPackage(getActivity(), "com.android.chrome"));
        }
        
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            Log.d("skku", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }
    
    private void startResult(boolean newValue) {
        /*
        if (mSearch_result.equals("lg_category_summary")) {
            getActivity().finish();
        } else if (mSearch_result.equals("usb_tethering")) {
            getActivity().finish();
        } else if (mSearch_result.equals("mobile_hotspot_vzw")) {
            getActivity().finish();
        } else if (mSearch_result.equals("enable_bluetooth_tethering")) {
            getActivity().finish();
        } else if (mSearch_result.equals("tethering_help")) {
            getActivity().finish();
        } else if (mSearch_result.equals("button_wifi_calling_key")) {
            getActivity().finish();
        } else if (mSearch_result.equals("toggle_airplane")) {
            getActivity().finish();
        } else if (mSearch_result.equals("mobile_network_settings")) {
            getActivity().finish();
        } else if (mSearch_result.equals("exchage_phone_capability")) {
            getActivity().finish();
        } else if (mSearch_result.equals("manage_mobile_plan")) {
            getActivity().finish();
        } else if (mSearch_result.equals("vpn_settings")) {
            getActivity().finish();
        } else if (mSearch_result.equals("vpn_selector")) {
            getActivity().finish();
        } else if (mSearch_result.equals("proxy_settings")) {
            getActivity().finish();
        } else if (mSearch_result.equals("toggle_nsd")) {
            getActivity().finish();
        } else if (mSearch_result.equals("mobile_network_settings_dualsim")) {
            getActivity().finish();
        } else if (mSearch_result.equals("switch_pm")) {
            getActivity().finish();
        } else if (mSearch_result.equals("emergency_alert")) {
            getActivity().finish();
        }*/
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
        if ( "VZW".equals(Config.getOperator()) && mVZWTether != null) {
            mVZWTether.pause();
        }

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
                mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                        mAirplaneModePreference.isChecked());
            }
            //[E][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
        } else if (requestCode == PROVISION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                startTethering();
            } else {
                //BT and USB need checkbox turned off on failure
                //Wifi tethering is never turned on until afterwards
                switch (mTetherChoice) {
                    case BLUETOOTH_TETHERING:
                        //mBluetoothTether.setChecked(false);
                        break;
                    case USB_TETHERING:
                        mUsbTether.setChecked(false);
                        break;
                    default :
                        break;
                }
                mTetherChoice = INVALID;
            }
    //+s LGBT_KDDI_TETHERING_POP_UP sinyoung.jun@lge.com 2013-05-21
        } else if (requestCode == TETHER_KDDI_REQUEST){
            if(resultCode ==Activity.RESULT_OK){
                gotoNextStep(true);
            } else if (resultCode == Activity.RESULT_CANCELED){
                Log.d(TAG, "Bluetooth Tethering canceled");
            }
            updateState();

        } else if (requestCode == REQUEST_FIRST_TIME_USE_USB)
    //+e LGBT_KDDI_TETHERING_POP_UP
    {
        Log.d(TAG, "[AUTORUN] REQUEST_FIRST_TIME_USE_USB" + resultCode);
        int askcheck = data.getIntExtra(EXTRA_FIRST_HELP_ASK, 0);
        setbFirstTimeUse(askcheck);
        if (resultCode == Activity.RESULT_OK)
        {
            Intent rcv_intent = new Intent();
            rcv_intent.putExtra("Tethering_Type", "USB");
            rcv_intent.setClassName("com.android.settings", "com.android.settings.lge.TetherSettingsHelpUsb");
            startActivity(rcv_intent);
        }
    }
        else if (requestCode == ATT_ENTITLEMENT_CHECK_REQUEST) {
            if (data != null) {
                if (resultCode == Activity.RESULT_OK) {
                    if ("USB".equals(data.getExtra("Tether_Type"))) {
                        if (!UsbSettingsControl.getUsbConnected(getActivity())) {
                            mUsbTether.setChecked(false);
                        } else {
                            onCreateTetherAlertDialg(DIALOG_TETHER_ALERT);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mUsbTether.setChecked(false);
                }
            }
        }

        else if (requestCode == USB_TETHER_KDDI_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (!UsbSettingsControl.getUsbConnected(getActivity())) {
                    mUsbTether.setChecked(false);
                } else {
                    setUsbTethering(true);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mUsbTether.setChecked(false);
            }
        }

        else if (requestCode == USB_TETHER_USC_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (!UsbSettingsControl.getUsbConnected(getActivity())) {
                    mUsbTether.setChecked(false);
                } else {
                    setUsbTethering(true);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mUsbTether.setChecked(false);
            }
        }
    }

    //+s LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING add to bluetooth tethering menu sinyoung.jun@lge.com 2012-04-27
    private BluetoothProfile.ServiceListener mProfileServiceListener =
        new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                mBluetoothPan = (BluetoothPan)proxy;
            }
            public void onServiceDisconnected(int profile) {
                mBluetoothPan = null;
            }
        };

    private final BroadcastReceiver mTetherChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                // TODO - this should understand the interface types
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);

                if (null != available && null != active && null != errored) {
                    updateUsbState(available.toArray(new String[available.size()]),
                            active.toArray(new String[active.size()]),
                            errored.toArray(new String[errored.size()]));
                    if (bluetoothAvailable) {
                        updateBluetoothState(available.toArray(new String[available.size()]),
                                active.toArray(new String[active.size()]),
                                errored.toArray(new String[errored.size()]));
                    }
                }
            }
            else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mMassStorageActive = true;
                updateState();
            }
            else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mMassStorageActive = false;
                updateState();
            }
            else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                mUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                mCdromStorage = intent.getBooleanExtra(UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false);
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);

                if (usbConnected) {
                    mUsbTetherTurningOn = false;
//                    UsbSettingsControl.getTetherStatus(getActivity());
//                    if (!UsbSettingsControl.getTetherStatus(getActivity())) {
//
//                    }
                }
                else {
                    if (mTetherAlertDialog != null && mTetherAlertDialog.isShowing()) {
                        mTetherAlertDialog.dismiss();
                        mTetherAlertDialog = null;
                    }
                }
                updateState();
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
                            // + suhui.kim@lge.com  20130721  handling NullPointerException
                            if (mBluetoothPan != null) {
                                mBluetoothPan.setBluetoothTethering(true);
//BT_S : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
                                if (LGBluetoothFeatureConfig.get("BT_COMMON_BTUX_4.2")) {
                                    Toast.makeText(getActivity(),
                                        R.string.bluetooth_tethering_on_toast_msg, Toast.LENGTH_SHORT).show();
                                }
//BT_E : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
                            }
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
            else if (PROVISION && intent.getAction().equals("com.lge.hotspotprovision.STATE_CHANGED"))
            {
                context.removeStickyBroadcast(intent);
                Log.d(TAG, "[BTUI]hotspotprovision.STATE_CHANGED : " + intent.getIntExtra("result", 0));
                Log.d(TAG, "4G MOBILE HOTSPOT PROVISION - mTetherChoice : " + mTetherChoice);

                if (intent.getIntExtra("result", 0) == 1) {
                    Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION OK.");

                    // LGE_VERIZON_WIFI_S, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    /*
                    if (mVZWTether.isSwitchChecked()) {
                        mVZWTether.settingVZWMobileHotspot(mTetherChoice);
                        //mTetherChoice = WIFI_TETHERING;
                    }
                    */
                    // LGE_VERIZON_WIFI_E, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    /*else*/
                    if (mTetherChoice == USB_TETHERING) {
                        mUsbTether.setChecked(true);
                        //processTurnOnOff();
                        setUsbTethering(true);
                    }
                    /*else*/ if (mTetherChoice == BLUETOOTH_TETHERING) {
                        mBluetoothTether.setChecked(true);
                        gotoNextStep(true);
                    }

                    mIsProvisioned = true;
                } else {
                    // LGE_VERIZON_WIFI_S, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    /*
                    if (mVZWTether.isSwitchChecked()) {
                        mVZWTether.restoreVZWMobileHotspotSetting();
                        mTetherChoice = WIFI_TETHERING;
                    }
                    */
                    // LGE_VERIZON_WIFI_E, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    if (mTetherChoice == USB_TETHERING) {
                        mUsbTether.setChecked(false);
                    }
                    else if (mTetherChoice == BLUETOOTH_TETHERING) {
                        mBluetoothTether.setChecked(false);
                    }

                    // LGE_VERIZON_WIFI_S, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    mTetherChoice = INVALID;
                    // LGE_VERIZON_WIFI_E, [junho.lim@lge.com],
                    // 2013-08-13, Mobile Hotspot Setting Switch
                    mIsProvisioned = false;

                    if (intent.getIntExtra("result", 0) == 2) {
                        Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION REDIRECT.");
                        Intent failintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"));
                            startActivity(failintent);
            }
                    else if (intent.getIntExtra("result", 0) == 3) {
                         Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION FAIL in tablet.");
                    } else {
                        Log.d(TAG, "[BTUI] 4G MOBILE HOTSPOT PROVISION FAIL.");
                        //[LG_BTUI][SPG_SPEC] Add for VZW SPG Spec [s]
                        Intent failIntent;
                        if (LgeWifiConfig.useMobileHotspot()) {
                            failIntent = new Intent();
                            failIntent.setAction("android.intent.action.MAIN");
                            failIntent.setClassName("com.lge.mobilehotspot.ui", CLASSNAME_MHP_PROVISION_FAILED_UPGRADE_DIALOG);
                            startActivity(failIntent);

                        } else {
                            failIntent = new Intent();
                            failIntent.setAction("android.intent.action.MAIN");
                            failIntent.setClassName("com.lge.wifisettings", "com.lge.wifisettings.vzwmhp.MHPProvisionFailActivity");
                            startActivity(failIntent);
                        }

                        //[minwoo2.kim, 2012/12/20] [e]
                    }

                }
            }
            // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION
            else if (action.equals(UPSELL_CHECK_USB_SUCCESS)) {
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                String[] available = cm.getTetherableIfaces();
                //String usbIface = findIface(available, mUsbRegexs);

                setUsbTethering(true);

                //updateState();
            }
            else if (action.equals(UPSELL_CHECK_USB_FAIL)) {
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                String[] available = cm.getTetherableIfaces();
                //String usbIface = findIface(available, mUsbRegexs);

                setUsbTethering(false);

                updateState();
            //+s setSummary with updateWifiApState
            } else if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                updateWifiApState(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            }
            //+e setSummary with updateWifiApState

            else if (action.equals(IMS_REGISTRATION)) {
                mWifiCallRegistered = intent.getBooleanExtra(IMS_REG_STATUS, false);
                Log.d(TAG, "[AUTORUN] WifiCall registered : " + mWifiCallRegistered);
            }
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                if ( intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED) == WifiManager.WIFI_AP_STATE_DISABLED ) {
                    if ( mTetherChoice == WIFI_TETHERING ) {
                         mTetherChoice = INVALID;
                    }
                }
            }

            //+s LGBT_ENTITLEMENT_FOR_TETHERING, younghyun.kwon@lge.com, 20130422, To check Entitlement of BT Tethering for AT&T
            if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
                if (action.equals(BT_ENTITLEMENT_CHECK_OK)) {
                    Log.d(TAG, "BT_ENTITLEMENT_CHECK_OK recieved");
                    onBluetoothTether();
                } else if (action.equals(BT_ENTITLEMENT_CHECK_FAILURE)) {
                    Log.d(TAG, "BT_ENTITLEMENT_CHECK_FAILURE recieved");
                    mBluetoothTether.setChecked(false);
                }
            }
            //+e LGBT_ENTITLEMENT_FOR_TETHERING
        }
    };

    /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */
    private boolean processTurnOnOff() {
        boolean newState = mUsbTether.isChecked();
        Log.d("USB_TETHER", "[MHP_GOOKY] processTurnOnOff : STATE : " + newState);

        ConnectivityManager cm =
            (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
       if (cm  == null) {
           return true;
       }

       if (newState) {
           if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
               mUsbTether.setChecked(false);
               mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
               return true;
           }
           //mUsbTether.setSummary("");
       }
       else {
           if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
               mUsbTether.setChecked(false);
               mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
               return true;
           }

           mUsbTether.setSummary("");
        }
        return true;
    }
    /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */

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
        if (bluetoothAvailable) {
            updateBluetoothState(available, tethered, errored);
        }
    }

    private void updateUsbState(String[] available, String[] tethered,
            String[] errored) {
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
        for (String s: errored) {
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

        if (Config.getOperator().equals(Config.VZW)) {
            if (usbTethered) {
                mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
                mUsbTether.setEnabled(true);
                mUsbTether.setChecked(true);
            } else if (usbErrored) {
                 mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                 mUsbTether.setEnabled(false);
                 mUsbTether.setChecked(false);
            } else if (mMassStorageActive) {
                mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            } else {
                if (mUsbConnected) {
                    if (mCdromStorage) {
                        if (Utils.isUI_4_1_model(getActivity())
                                && !"VZW".equals(Config.getOperator())) {
                            if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())
                                    || "MPCS".equals(Config.getOperator())) {
                                mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2_tmo_mpcs);
                            } else {
                                mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2);
                            }
                        } else {
                            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                        }
                        mUsbTether.setEnabled(false);
                        mUsbTether.setChecked(false);
                    } else {
                        if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                            mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
                            if (mUsbTetherTurningOn) {
                                mUsbTether.setEnabled(false);
                                mUsbTether.setChecked(true);
                            } else {
                                mUsbTether.setEnabled(true);
                                mUsbTether.setChecked(false);
                            }
                        } else {
                            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                            mUsbTether.setEnabled(true);
                            mUsbTether.setChecked(false);
                            mUsbTetherTurningOn = false;
                        }
                    }
                } else {
                    if (Utils.isUI_4_1_model(getActivity())
                            && !"VZW".equals(Config.getOperator())) {
                        if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())
                                || "MPCS".equals(Config.getOperator())) {
                            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2_tmo_mpcs);
                        } else {
                            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2);
                        }
                    } else {
                        mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                    }
                    mUsbTether.setEnabled(false);
                    mUsbTether.setChecked(false);
                }
            }
        }
        else {
            if (usbTethered) {
                mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
                mUsbTether.setEnabled(true);
                mUsbTether.setChecked(true);
                mUsbTetherTurningOn = false;
            /*    // Delete automatically wifi off by tmus operator.
                if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())) {
                    WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

                    if (mWifiManager != null) {
                        if (mWifiManager.isWifiEnabled()) {
                            mWifiManager.setWifiEnabled(false);
                        }
                    }
                }
            */
            }
            else if (usbAvailable) {
                if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    if (Utils.isUI_4_1_model(getActivity())
                            && !"VZW".equals(Config.getOperator())) {
                        mUsbTether.setSummary(R.string.usb_tethering_available_subtext_ex);
                    } else {
                        mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
                    }
                    if (mUsbTetherTurningOn) {
                        mUsbTether.setEnabled(false);
                        mUsbTether.setChecked(true);
                    }
                    else {
                        mUsbTether.setEnabled(true);
                        mUsbTether.setChecked(false);
                    }
                }
                else {
                    mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                    mUsbTether.setEnabled(true);
                    mUsbTether.setChecked(false);
                    mUsbTetherTurningOn = false;
                }
            }
            else if (usbErrored) {
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
            else if (mMassStorageActive) {
                mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
            else {
                if (Utils.isUI_4_1_model(getActivity())
                        && !"VZW".equals(Config.getOperator())) {
                    if ("US".equals(Config.getCountry()) && "TMO".equals(Config.getOperator())
                            || "MPCS".equals(Config.getOperator())) {
                        mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2_tmo_mpcs);
                    } else {
                        mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext_ex2);
                    }
                } else {
                    mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                }
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
        }

        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015 Check if 3LM has disabled tethering
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Secure.getInt(getContentResolver(),
                    "tethering_blocked", 0) == 1) {
                mUsbTether.setSummary(R.string.tethering_blocked);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

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

        // TMUS
        //if (mCheckingMobileData) {
        //    mUsbTether.setEnabled(false);
        //}
    }
    
    public static final String A2DP_SINK_PREFERENCE = "preference_sink_mode";
    public static final String KEY_SINK_MODE = "key_sink_mode";

    private void updateBluetoothState(String[] available, String[] tethered,
            String[] errored) {
        int bluetoothTethered = 0;
// LG_BTUI: MOD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [S]
        int bluetoothTethered_BRCM = 0;
// LG_BTUI: MOD/ilbeom.kim/'13-05-27 - [Z] fixed tethering information mismatch [E]


        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-235][ID-MDM-35][ID-MDM-197]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().setBluetoothTetheringMenu(mBluetoothTether)) {
            return;
        }
        // LGMDM_END
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
        for (String s: errored) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) {
                    bluetoothErrored = true;
                }
            }
        }

        // Bluetooth Thering is not support using SinkMode
        if (com.lge.bluetooth.LGBluetoothDeviceConfig.isDeviceSupported("A2DP_SINK_COMMON")) {

            SharedPreferences prefSinkMode = getActivity().getSharedPreferences(
                A2DP_SINK_PREFERENCE, Context.MODE_PRIVATE);
            boolean mSinkMode = prefSinkMode.getBoolean(KEY_SINK_MODE, false);

            if (mBluetoothTether != null && mSinkMode) {
                mBluetoothTether.setEnabled(false);
                mBluetoothTether.setChecked(false);
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                return;
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
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_device_connected_subtext);
                } else if (bluetoothErrored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
                }
            } else {
                if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.08.21 WBT #376379 Null Pointer Dereference fixed
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                    mBluetoothTether.setEnabled(true);
// +s LG_BTUI_DENIED_PAN_WHEN_FAILED_NAI  suhui.kim@lge.com  20130604, Sprint Requirement, need to update SwitchPreference for BT in case of failed NAI
                    if (!mBluetoothPan.isTetheringOn()) {
                        mBluetoothTether.setChecked(false);
                    }
// +e LG_BTUI_DENIED_PAN_WHEN_FAILED_NAI
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
            // + suhui.kim@lge.com  20130721  handling NullPointerException
            if (mBluetoothPan != null) {
                mBluetoothPan.setBluetoothTethering(true);
            }
            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376372, #376373 Null Pointer Dereference fixed
                mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_available_subtext);
//BT_S : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
                if (LGBluetoothFeatureConfig.get("BT_COMMON_BTUX_4.2")) {
                    Toast.makeText(getActivity(),
                        R.string.bluetooth_tethering_on_toast_msg, Toast.LENGTH_SHORT).show();
                }
//BT_E : [CONBT-1442] LGC_BT_COMMON_UX_PAN_NEW_SCENARIO_FOR_UX_4_2
            }
        }
    }
    //+e LGBT_ENTITLEMENT_FOR_TETHERING

//BT_S : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
    // +s LGBT_CNDTL_SCENARIO_CHECK_PROVISION check vzw provision before enter bt tethering
    private void checkProvision() {

        //TODO: check is pan already enabled? then return
        /*
           if (mLocalManager.isMobileHotspotOn())
           return;
         */

        if (!mIsProvisioned) {
            if (NO_SIM_MODEL) {
                check3gProvision();
            } else {
                check4gProvision();
            }
        } else {
            gotoNextStep(true);
        }
    }

    private void check3gProvision() {
         Log.w(TAG, "[BTUI] check3gProvision isChecked3gProvisionUseReminder >> "
                      + isChecked3gProvisionUseReminder());
//BT_S : [PSIX-6270] VZW 3G Provsion
/*
        if (!checkMobileNetworkState()) {
            Toast.makeText(getActivity(),
                R.string.sp_3g_sign_data_not_availible_NORMAL, Toast.LENGTH_SHORT).show();
            mBluetoothTether.setChecked(false);
            return;
        }
*/
//BT_E : [PSIX-6270] VZW 3G Provsion
        if (isChecked3gProvisionUseReminder() == 0) {
            ProvisionDialog();
        } else {
            Intent proIntent = new Intent("com.lge.hotspot.provision_start");
            startActivity(proIntent);
        }

    }

    private void check4gProvision() {

        Log.d(TAG, "[BTUI] checkProvision " + mIsProvisioned);
        mTetherChoice = BLUETOOTH_TETHERING;

        try {
            Intent proIntent = new Intent("com.lge.hotspot.provision_start");
            startActivity(proIntent);
        } catch (ActivityNotFoundException activityNotFoundException) {
           activityNotFoundException.printStackTrace();
        }

    }

    public int isChecked3gProvisionUseReminder() {
        return Settings.System.getInt(this.getContentResolver(),
                MHP_3G_PROVISION_REMINDER, 0);
    }

    public boolean confirm3gPrivisionUseReminder(int check) {
        return Settings.System.putInt(this.getContentResolver(),
                MHP_3G_PROVISION_REMINDER, check);
    }

    public boolean confirm3gPrivisionNotiDone(int check) {
        return Settings.System.putInt(this.getContentResolver(),
                MHP_3G_PROVISION_NOTI_DONE, check);
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

    public void ProvisionDialog() {
        Log.d(TAG, "ProvisionDialog");
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater factory = LayoutInflater.from(getActivity());

        final View dialogView = factory.inflate(
                R.layout.mobilehotspot_3g_provision, null);

        final CheckBox reminderCheck = ( CheckBox )dialogView
                .findViewById(R.id.sign_noti_reminder);

        reminderCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reminderCheck.isChecked()) {
                    confirm3gPrivisionUseReminder(1);
                } else {
                    confirm3gPrivisionUseReminder(0);
                }
            }
        });

        alertDlgBuilder.setView(dialogView);
        // alertDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDlgBuilder
                .setIconAttribute(android.R.attr.alertDialogIcon);
        alertDlgBuilder.setTitle(this.getResources().getString(
                R.string.sp_notification_NORMAL));
        // Token Update
        alertDlgBuilder.setOnCancelListener(mCancelEvent);
        alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
                mDoNotTurnOn);
        alertDlgBuilder.setPositiveButton(R.string.dlg_ok,
                mDoAccept3gProvision);
        //alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
        //        mCancelEvent);

        alertDlgBuilder.create();
        alertDlgBuilder.show();
    }

    DialogInterface.OnClickListener mDoNotTurnOn = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, "[BTUI] doNotTurnOn onClick");
            mBluetoothTether.setChecked(false);
        }
    };

    DialogInterface.OnCancelListener mCancelEvent = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            Log.w(TAG, "[BTUI] Dialog canceled");
            mBluetoothTether.setChecked(false);
        }
    };

    DialogInterface.OnClickListener mDoAccept3gProvision = new DialogInterface.OnClickListener() {
       @Override
       public void onClick(DialogInterface dialog, int which) {
           Log.w(TAG, "[BTUI] doAccept3gProvision onClick ");
           mTetherChoice = BLUETOOTH_TETHERING;
           Intent proIntent = new Intent("com.lge.hotspot.provision_start");
           startActivity(proIntent);
       }
    };
//BT_E : [PSIX-6270] VZW 3G Provsion
// from yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

    private void gotoNextStep(boolean value) {
        if (value) {
            //+s LGBT_ENTITLEMENT_FOR_TETHERING ATT's requirement sinyoung.jun@lge.com 2012-05-24
            String[] appDetails;
            if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
                appDetails = getResources().getStringArray(R.array.config_mobile_bt_tether_provision_app);
            } else {
                appDetails = getResources().getStringArray(com.android.internal.R.array.config_mobile_hotspot_provision_app);
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
                        intent.putExtra("Tether_Type", "BT");
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

            String [] tethered = cm.getTetheredIfaces();
            String bluetoothIface = findIface(tethered, mBluetoothRegexs);
            if (bluetoothIface != null &&
                    cm.untether(bluetoothIface) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                errored = true;
            }

            // + suhui.kim@lge.com  20130721  handling NullPointerException
            if (mBluetoothPan != null) {
                mBluetoothPan.setBluetoothTethering(false);
            }

            if (mBluetoothTether != null) { // sunghee78.lee@lge.com 2012.06.28 WBT #376374, #376375 Null Pointer Dereference fixed
                if (errored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.sp_bluetooth_tethering_off_subtext_jb_plus);
                }
            }
        }

    }
    // +e LGBT_CNDTL_SCENARIO_CHECK_PROVISION

    //+s setSummary with updateWifiApState
    private void updateWifiApState(int state) {
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration mWifiConfig = mWifiManager.getWifiApConfiguration();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if ((Config.VZW.equals(Config.getOperator()) && MDMSettingsAdapter.getInstance()
                    .setWifiApEnablerMenu(findPreference("mobile_hotspot_vzw")))
                    || MDMSettingsAdapter.getInstance().setWifiApEnablerMenu(mWifiTether)) {
                return;
            }
        }
        // LGMDM_END
        if (null != mVZWTether) {
            switch (state) {
                case WifiManager.WIFI_AP_STATE_ENABLING:
                    mVZWTether.setEnabled(false);
                    break;
                case WifiManager.WIFI_AP_STATE_ENABLED:
                    mVZWTether.setEnabled(true);
                    break;
                case WifiManager.WIFI_AP_STATE_DISABLING:
                    mVZWTether.setEnabled(false);
                    break;
                case WifiManager.WIFI_AP_STATE_DISABLED:
                    mVZWTether.setEnabled(true);
                    break;
                default:
                    mVZWTether.setEnabled(true);
                    break;
            }
        } else {
            switch (state) {
                case WifiManager.WIFI_AP_STATE_ENABLING:
                    mWifiTether.setSummary(R.string.wifi_hotspot_starting);
                    break;
                case WifiManager.WIFI_AP_STATE_ENABLED:
                    mWifiTether.setSummary(getString(R.string.wifi_tether_enabled_subtext_dcm,
                                                     mWifiConfig.SSID));
                    break;
                case WifiManager.WIFI_AP_STATE_DISABLING:
                    mWifiTether.setSummary(R.string.wifi_hotspot_stopping);
                    break;
                case WifiManager.WIFI_AP_STATE_DISABLED:
                default:
                    if ("ATT".equals(Config.getOperator())
                        || (Config.getCountry().equals("US")
                            && (Config.getOperator().equals("TMO")
                                || Config.getOperator().equals("MPCS"))) ) {
                        mWifiTether.setSummary(R.string.sp_wifi_hotspot_summary_NORMAL);
                    } else {
                        mWifiTether.setSummary(R.string.sp_wifi_hotspot_summary_ver2_NORMAL);
                    }
                    break;
            }
        }
    }
    //+e setSummary with updateWifiApState

    //+e LGBT_COMMON_SCENARIO_BLUETOOTH_TETHERING
    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }
    private void updateAirplaneModeState(boolean isAirplaneMode) {
        Log.d (TAG, "updateAirplaneModeState -> " +  isAirplaneMode );
        String toggleable = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        if (isAirplaneMode) {
            // Manually set dependencies for Wifi when not toggleable.
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
                if (mNetworkSettingsCategory != null) {
                    mNetworkSettingsCategory.findPreference(KEY_VPN_SETTINGS).setEnabled(false);
                }
            }
            // Manually set dependencies for NFC when not toggleable.
            // [2012.12.31][munjohn.kang] added a triple SIM condition
            if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS) && (mNetworkSettingsCategory != null)) {
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(false);
                }
                else {
                    if (mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTINGS) != null) {
                        mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
                    }
                }
            }
            // Manually set dependencies for Bluetooth when not toggleable.
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
                // No bluetooth-dependent items in the list. Code kept in case one is added later.
            }

        } else {
            // Manually set dependencies for Wifi when not toggleable.
            if ((toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) && (mNetworkSettingsCategory != null)) {
                mNetworkSettingsCategory.findPreference(KEY_VPN_SETTINGS).setEnabled(true);
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-282]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMSettingsAdapter.getInstance().setVpnMenu(
                            getPreferenceScreen().findPreference(KEY_VPN_SETTINGS));
                }
                // LGMDM_END
            }

            if (!b_isWifiOnly && !mIsSecondaryUser && !mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS) && (mNetworkSettingsCategory != null)) {
                // Manually set dependencies for NFC when not toggleable.
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    mNetworkSettingsCategory.findPreference(KEY_MOBILE_NETWORK_SETTING_DUALSIM).setEnabled(true);
                }
                else {
                    if ("VZW".equals(Config.getOperator()) || "SPR".equals(Config.getOperator()) ) {
                        getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                    } else {
                        if ( !Utils.hasReadyMobileRadio(getActivity())) {
                            getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(false);
                        } else {
                            getPreferenceScreen().findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(true);
                        }
                    }
                }
            }
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
                // No bluetooth-dependent items in the list. Code kept in case one is added later.
            }

        }

    }

    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String action = intent.getAction();
                Log.d (TAG, "Airplane Evenr arrives!!");
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

            if (value && mMobileNetworkSettings != null) {
                //ask130313 :: it disabled the MobileNetworkSetting Menu item in VZW.
                mMobileNetworkSettings.setEnabled(false);
                Log.d(TAG, "mApnChangeObserver mMobileNetworkSettings.setEnabled = false");
            }

            //[S][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
            if (mAirplaneModePreference != null) {
                mAirplaneModePreference.setEnabled(!value);
            }
            //[E][chris.won@lge.com][2012-07-16][GJ_DCM] Temporal code for Airplane Mode
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

    //[S] TMUS requirement
    private void connectUpsell(int type) {
        //WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        //final int wifiState = wifiManager.getWifiState();
        final int tetherType = type;

//        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
//            Log.w("Hoon,USB", this.getClass().getName() + " : " + "USB:WiFi Off ..ing");

//            wifiManager.setWifiEnabled(false);
            ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (!cm.getMobileDataEnabled()) {
                Log.w("Hoon,USB", this.getClass().getName() + " : " + "!cm.getMobileDataEnabled()");
                onCreateTetherAlertDialg(DIALOG_MOBIL_DATA_ENABLED_ERROR);
                mUsbTether.setEnabled(true);
                return;
            }

        //mCheckingMobileData = true;

            startUpsell(tetherType);
/*
            if (tetherType == USB_TETHERING) {
                //mUsbTether.setEnabled(false);
            }
*/
/*
            loopCounting = 0;
            //mTimer = new Timer(true);
            mHandler = new Handler();
            if (mTimer != null ) {
                mTimer.cancel();
                mTimer = null;
            }
            mTimer = new Timer(true);
            mTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            public void run() {
                                ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                                if (info.isConnected()) {
                                    mTimer.cancel(); //Timer End.
                                    Log.w("Hoon, USB", this.getClass().getName() + " : " + "info.isConnected()1=" + info.isConnected());
                                    mCheckingMobileData = false;
                                    startUpsell(tetherType);
                                    //mUsbTether.setEnabled(false);
                                }
                                if (loopCounting > 50) {
                                    Log.w("Hoon, USB", this.getClass().getName() + " : " + "T_COUNT Over : loopCounting=" + loopCounting);
                                    mTimer.cancel(); //Timer End.
                                    if (tetherType == USB_TETHERING) {
                                        mUsbTether.setEnabled(true);
                                    }
                                    mCheckingMobileData = false;
                                    return;
                                }
                                else {
                                    loopCounting++;
                                    Log.w("Hoon, USB", "T_COUNT : loopCounting=" + loopCounting);
                                }
                            }
                        });
                    }
                }, 500, 500
            );
*/
//        }
/*
        else {
            Log.w("Hoon,USB", this.getClass().getName() + " : " + "intent.setClassName(com.lge.upsell, com.lge.upsell.DataService);");
            startUpsell(tetherType);
            //mUsbTether.setChecked(false);
        }
*/
    }

    private boolean isUpsell() {
        boolean supportUpsell = false;
        PackageManager pm = getActivity().getApplicationContext().getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo("com.lge.upsell", 0);
            supportUpsell = true;
            Log.d(TAG, "pkgInfo = " + pkgInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "not support Upsell");
            supportUpsell = false;
        }

        return supportUpsell;
    }

    private void startUpsell(int type) {
        Intent intent = new Intent();
        if (type == USB_TETHERING) {
            mUsbTetherTurningOn = true;
            intent.putExtra("Tethering_Type", "USB");
        }
        else if (type == WIFI_TETHERING) {
            intent.putExtra("Tethering_Type", "Wifi");
        }
        //intent.setClassName("com.lge.upsell", "com.lge.upsell.DataService");
        //getActivity().startService(intent);
    intent.setClassName("com.lge.upsell", "com.lge.upsell.UpsellDialogActivity");
        startActivity(intent);
    }
    //[E] TMUS requirement
        //[S]joon7.lim@lge.com add first time use dialog for NewCo
    private boolean getbFirstTimeUse() {
        boolean bfirstcheck;

        Log.d(TAG, "CSC_B getbFirstTimeUse");
        if (getSettingInt(SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK, 0) == 0)
        {
            bfirstcheck = false;
        }
            else
        {
                bfirstcheck = true;
        }

        return bfirstcheck;
    }

    private void setbFirstTimeUse(int ask) {

    setSettingInt(SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK, ask);

    }
    private int getSettingInt(String key, int defaultValue) {
            Log.d(TAG, "CSC_A getSettingInt");
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME,
                Activity.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }
    private void setSettingInt(String key, int value) {
            Log.d(TAG, "CSC_A setSettingInt");
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME,
                Activity.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }
    //[E]joon7.lim@lge.com add first time use dialog for NewCo

    private boolean isTmusSimCard(String numeric)
    {
        //String numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        if ( numeric == null ) {
            Log.d(TAG, "MCC + MNC : null!!!" );
            return false;
        }
        Log.d(TAG, "gsm.sim.operator.gid : " + SystemProperties.get("gsm.sim.operator.gid"));
        if ( "6D38".equals(SystemProperties.get("gsm.sim.operator.gid")) ) {
            return false;
        }
        Log.d(TAG, "MCC + MNC : " + numeric);
        if ( numeric.equals("00101") || numeric.equals("310160") || numeric.equals("310200") || numeric.equals("310210") ||
            numeric.equals("310220") || numeric.equals("310230") || numeric.equals("310240") || numeric.equals("310250") ||
            numeric.equals("310260") || numeric.equals("310270") || numeric.equals("310300") || numeric.equals("310310") ||
            numeric.equals("310490") || numeric.equals("310530") || numeric.equals("310580") || numeric.equals("310590") ||
            numeric.equals("310640") || numeric.equals("310660") || numeric.equals("310800")) {
            return true;
        }
        return false;
    }

 // rebestm_KK_SMS
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

    private void initSmsApplicationSetting() {
        Log.d (TAG, "initSmsApplicationSetting:");
        Collection<SmsApplicationData> smsApplications =
                SmsApplication.getApplicationCollection(getActivity());

        // If the list is empty the dialog will be empty, but we will not crash.
        int count = smsApplications.size();
        String[] packageNames = new String[count];

        int i = 0;
        for (SmsApplicationData smsApplicationData : smsApplications) {
            CharSequence LgSMS = smsApplicationData.mPackageName ;
            Log.d (TAG, "smsApplicationData.mApplicationName111 = "
                                                            + smsApplicationData.mApplicationName);

            if ( LgSMS.equals(LGSMS_PACKAGENAME)) {
                packageNames[i] = smsApplicationData.mPackageName;
                i = 1;
                break;
            }
        }

        for (SmsApplicationData smsApplicationData : smsApplications) {
            CharSequence LgSMS = smsApplicationData.mPackageName ;
            Log.d (TAG, "smsApplicationData.mApplicationName222 = "
                                                            + smsApplicationData.mApplicationName);

            if ( !LgSMS.equals(LGSMS_PACKAGENAME)) {
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

    private boolean isSmsSupported() {
        // Some tablet has sim card but could not do telephony operations. Skip those.
        Log.d (TAG, "mPhone.getPhoneType() = " + mPhone.isSmsCapable());
        return (mPhone.isSmsCapable());
    }


       private Intent makeHttpPostIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName("com.android.chrome", "com.google.android.apps.chrome.Main");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Telephony
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String mdn = null;
        String iccid = null;
        String imei = null;

        if (telephonyManager != null) {
            mdn = telephonyManager.getLine1Number();
            iccid = telephonyManager.getSimSerialNumber();
            imei = telephonyManager.getDeviceId();
        }
// rebestm temp test
//        mdn = "2015724361";
//        iccid = "89148000000671569136";
//        imei = "99000262000298";

        if ((mdn == null) || "".equals(mdn) == true) {
            mdn = "0000000000";
        }

        if ((iccid == null) || "".equals(iccid) == true) {
            iccid = "00000000000000000000";
        }

        if ((imei == null) || "".equals(imei) == true) {
            imei = "00000000000000";
        }

        String finalHTML = "";
        String html = readTrimRawTextFile(context, R.raw.http_post_file);

        if ("".equals(html) == false && html != null) {

            String url_valuded_changed = html.replace("url_value", Production_URL_HTTPS_WO_QMARK);

            String mdn_valuded_changed = url_valuded_changed.replace("mdn_value", mdn);

            // Log.e(TAG, "++++++++++++++++++");
            // Log.e(TAG, "HTML MDN : " + mdn_valuded_changed);

            String oem_valuded_changed = mdn_valuded_changed.replace("oem_value", "MOT");

            // Log.e(TAG, "++++++++++++++++++");
            // Log.e(TAG, "HTML OEM : " + oem_valuded_changed);

            String iccid_valuded_changed = oem_valuded_changed.replace("iccid_value", iccid);

            // Log.e(TAG, "++++++++++++++++++");
            // Log.e(TAG, "HTML ICCID : " + iccid_valuded_changed);

            String imei_valuded_changed = iccid_valuded_changed.replace("imei_value", imei);
            // Log.e(TAG, "++++++++++++++++++");
            // Log.e(TAG, "HTML IMEI : " + imei_valuded_changed);

            String encodedURL = "";

            try {
                encodedURL = URLEncoder.encode(imei_valuded_changed, "UTF-8");

                // Log.e(TAG, "++++++++++++++++++");
                // Log.e(TAG, "after encode URL  : " + encodedURL);

                finalHTML = encodedURL.replaceAll("\\+", "%20");

                // Log.e(TAG, "++++++++++++++++++");
                // Log.e(TAG, "HTML replaced all : " + finalHTML);

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "url encode failed : " + e.toString());
                return intent;
            }
        } else {
            Log.e(TAG, "failed to read the raw file");
        }

        StringBuilder sb = new StringBuilder("data:text/html,");
        sb.append(finalHTML);

        Log.v(TAG, "++++++++++++++++++");
        Log.v(TAG, "Uri parsed : " + Uri.parse(sb.toString()));
        Log.v(TAG, "++++++++++++++++++");

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

   private void checkEntitlement() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.putExtra("Tether_Type", "USB");
        send.setClassName("com.lge.entitlementcheckservice",
            "com.lge.entitlementcheckservice.EntitlementDialogActivity");
        startActivityForResult(send, ATT_ENTITLEMENT_CHECK_REQUEST);
   }

   private void startTetherKDDIPopup() {
        int checkShow = Settings.System.getInt(
            getActivity().getContentResolver(),
                "TETHER_POPUP_KDDI", 0);
        if (checkShow == 1) {
            setUsbTethering(true);
            return;
        }

        Intent send = new Intent(Intent.ACTION_MAIN);
        send.setClassName("com.android.settings",
            "com.android.settings.deviceinfo.TetherPopupKDDIActivity");
        startActivityForResult(send, USB_TETHER_KDDI_REQUEST);
    }

    private void startUsbTetherIntroPopup() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.setClassName("com.android.settings",
            "com.android.settings.deviceinfo.UsbTetherIntroUSCActivity");
        startActivityForResult(send, USB_TETHER_USC_REQUEST);
    }


    public int getExchangephonecapability() {
        int isResut = PRESENCE_EAB_STATUS_FALSE;

        Cursor c = getContentResolver().query(CONTENT_URI, new String[] {"presence_eab_status"},
                    null,
                    null,
                    null);

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

    public void setExchangephonecapability(boolean isCheck) {
        ContentValues cv = new ContentValues();

        Log.i(TAG, "setExchangephonecapability " +
                "update Exchangephonecapability is isCheck = " + isCheck);

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


    public void setCheckStartTime() {
        SharedPreferences preferences = getActivity().getSharedPreferences("apps_prefs", 0);
        long now = System.currentTimeMillis();
        Log.i (TAG, "setCheckStartTime = " + now);
        preferences.edit().putLong("StartTime", now).commit();
    }

    public long getCheckStartTime() {
        SharedPreferences preferences = getActivity().getSharedPreferences("apps_prefs", 0);
        long starttime = preferences.getLong("StartTime", 0);
        Log.i (TAG, "getCheckStartTime = " + starttime);
        return starttime;
    }

    public void setEnableExchangePhoneMenu() {
        Cursor c = getContentResolver().query(IMS_REG_CONTENT_URI, new String[] {"uc_reg_state"},
                null,
                null,
                null);
        int isCheck = -1;

        try {
            if (c != null ) {
                c.moveToFirst();
                isCheck = c.getInt(0);
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

        switch (isCheck) {
        case 1:
        case 2:
        case 3:
            mCheckExchangephone.setEnabled(true);
            Log.d (TAG, "IMS reg ok!!!");
            break;
        case 0:
        default:
            mCheckExchangephone.setEnabled(false);
            break;
        }
        Log.d (TAG, "uc_reg_state = " + isCheck);
    }


    public void setEnableExchangePhoneMenu_back() {
        Log.i (TAG, "setEnableExchangePhoneMenu ->");

        int isChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                                SettingsConstants.Secure.DATA_NETWORK_ENHANCED_4G_LTE_MODE, 0);

        Log.d (TAG, "DATA_NETWORK_ENHANCED_4G_LTE_MODE =  " + isChecked);

        if (isChecked == 1) {
            boolean isAvDataRoaming = getDataRoaming();
            boolean isDataRoaming = Config.isDataRoaming();


            Log.d (TAG, "isAvDataRoaming =  " + isAvDataRoaming);
            Log.d (TAG, "isDataRoaming =  " + isDataRoaming);

//            if (isDataRoaming) {
                if (isAvDataRoaming) {
                    mCheckExchangephone.setEnabled(true);
                } else {
                    mCheckExchangephone.setEnabled(false);
                }
//            }
//            else
//            {
//                mCheckExchangephone.setEnabled(false);
//            }
        } else {
            mCheckExchangephone.setEnabled(false);
        }

        Log.i (TAG, "<- setEnableExchangePhoneMenu ");
    }

    private boolean getDataRoaming() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Secure.getInt(resolver, Settings.Secure.DATA_ROAMING, 0) != 0;
    }

    private boolean getTetherNetworkAirplaneModeVoLTE() {
       if (mAirplaneModeEnabler == null) {
           return false;
       } else {
           return mAirplaneModeEnabler.getAirplaneModeVoLTE();
       }
    }

    private static boolean isSupportEmergencyAlerts(Context context) {
        try {
            return context.getPackageManager().getPackageInfo("com.lge.cmas", 0) != null;
        } catch (NameNotFoundException e) {
            return false;
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
  
      public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {

        private Map<String, Integer> mDisplayCheck = new HashMap<String, Integer>();        
        private boolean sIsSubUserOnly;
        String title = "";
        
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            setRemoveVisible(context);
            sIsSubUserOnly = UserHandle.myUserId() != UserHandle.USER_OWNER; 
            
            if ("VZW".equals(Config.getOperator())) {
                title = context.getString(R.string.wireless_more_settings_title);
            } else {
                title = context.getString(R.string.tether_network_label); 
            }            
            setSearchIndexData(context, "tethernetwork_settings_main",
                    title, "main", null,
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);           

            String usb_tethering_title = "";
            if ("VZW".equals(Config.getOperator())) {
                usb_tethering_title = context.getString(R.string.sp_mobile_broadband_connection_NORMAL);
            } else {
                usb_tethering_title = context.getString(R.string.tether_settings_title_usb); 
            }             
            setSearchIndexData(context, "usb_tethering",
                    usb_tethering_title,
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);        
            
            setSearchIndexData(context, "mobile_hotspot_vzw",
                    context.getString(R.string.sp_mobile_hotspot_NORMAL),   //Mobile Hotspot
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);    

                /*
            setSearchIndexData(context, "tether_settings",
                    context.getString(R.string.sp_wifi_hotspot_NORMAL),
                    title, context.getString(R.string.sp_wifi_hotspot_summary_ver2_NORMAL), 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "Switch", null, null, 1, 0);*/
            
            setSearchIndexData(context, "enable_bluetooth_tethering",
                    context.getString(R.string.bluetooth_tether_checkbox_text),  // Bluetooth tethering
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, "tethering_help",
                    context.getString(R.string.tethering_help_button_text),   // Help
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);
            
                /*
            setSearchIndexData(context, "button_wifi_calling_key",
                    context.getString(R.string.sp_wifi_calling_mode_title_NORMAL),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "CheckBox", null, null, 1, 0);
            
            setSearchIndexData(context, "toggle_airplane",
                    context.getString(R.string.airplane_mode),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "CheckBox", null, null, 1, 0);*/


            String network_summary = "";
            if ( "VZW".equals(Config.getOperator()) && (!sIsSubUserOnly)) {
                if (SystemProperties.getInt("ro.telephony.default_network", 0) == 4) {
                    network_summary = context.getString(R.string.sp_mobile_network_contens_NORMAL);
                } else if (Utils.isTablet()) {
                    network_summary = context.getString(R.string.settings_tether_netwokr_sumary_noroaming);                   
                } else {
                    network_summary = context.getString(R.string.sp_network_settings_summary_NORMAL);                    
                }
            }                
            setSearchIndexData(context, "mobile_network_settings",
                    context.getString(R.string.network_settings_title),    // Mobile networks
                    title, network_summary, 
                    null, "android.intent.action.MAIN",
                    "com.lge.networksettings.MobileNetworkSettings", "com.lge.networksettings", 1,
                    null, null, null, 1, 0);
            
                /*
            setSearchIndexData(context, "exchage_phone_capability",
                    context.getString(R.string.settings_tether_exchange_phone_capability),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "CheckBox", null, null, 1, 0);*/
            if (mDisplayCheck.get("sms_application") == null) {
                setSearchIndexData(context, "sms_application",
                        context.getString(R.string.sms_application_title2),   // Default message app
                        title, null, 
                        null, "android.settings.TETHERNETWORK", 
                        null, null, 1,
                        null, null, null, 1, 0);
            }
            
                /*
            setSearchIndexData(context, "manage_mobile_plan",
                    context.getString(R.string.manage_mobile_plan_title),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, "vpn_settings",
                    context.getString(R.string.vpn_title_jb_plus),
                    title, null, 
                    null, "android.intent.action.MAIN",
                    "com.android.settings.vpn2.VpnSettings", "com.android.settings", 1,
                    null, null, null, 1, 0);*/
            
            setSearchIndexData(context, "vpn_selector",
                    context.getString(R.string.vpn_title_jb_plus),    // VPN
                    title, context.getString(R.string.sp_vpn_settings_summary_NORMAL_jb_plus), 
                    null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$VpnSelectorActivity",
                    "com.android.settings", 1,
                    null, null, null, 1, 0);

            if (mDisplayCheck.get("view_verizon_account_tablet") == null) {                    
                setSearchIndexData(context, "view_verizon_account_tablet",
                        context.getString(R.string.view_verizon_account),
                        title, null, 
                        null, "android.settings.TETHERNETWORK", 
                        null, null, 1,
                        null, null, null, 1, 0);
            }

                /*
            setSearchIndexData(context, "proxy_settings",
                    context.getString(R.string.proxy_settings_title),
                    title, null, 
                    null, "android.intent.action.MAIN",
                    "com.android.settings.ProxySelector", "com.android.settings", 1,
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, "toggle_nsd",
                    context.getString(R.string.nsd_quick_toggle_title),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "CheckBox", null, null, 1, 0);
            
            setSearchIndexData(context, "mobile_network_settings_dualsim",
                    context.getString(R.string.network_settings_title),
                    title, null, 
                    null, "android.intent.action.MAIN",
                    "com.lge.networksettings.msim.MSimMobileNetworkSettings",
                    "com.lge.networksettings", 1,
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, "switch_pm",
                    context.getString(R.string.settings_tether_pm),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    "Switch", null, null, 1, 0);
            
            setSearchIndexData(context, "emergency_alert",
                    context.getString(R.string.emergency_alert_title),
                    title, null, 
                    null, "android.settings.TETHERNETWORK", 
                    null, null, 1,
                    null, null, null, 1, 0);*/
            
            
            return mResult;
        }

        public void setRemoveVisible(Context context) {
            if (Utils.isTablet()) {
                mDisplayCheck.put("sms_application", 0);
            }

            if (!"VZW".equals(Config.getOperator()) || !Utils.isTablet()) {
                mDisplayCheck.put("view_verizon_account_tablet", 0);
            }
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
