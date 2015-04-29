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

package com.android.settings.wifi;

import com.android.settings.R;
import com.android.settings.WirelessSettings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
//import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.android.settings.lgesetting.Config.Config;

/* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */
import android.telephony.TelephonyManager;
import android.os.SystemProperties;
/* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */

/* LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-07-10, <AlertDialog: When wifi is turned on> */
import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
/* LGE_CHANGE_E, [jongpil.yoon@lge.com], 2012-07-10, <AlertDialog: When wifi is turned on> */


// JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
import android.app.Activity;
//import android.os.Bundle;
import android.os.Handler;
//import android.content.res.AssetManager;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Spanned;
import android.view.KeyEvent;
//import android.view.LayoutInflater;
import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
//import java.io.InputStream;
//import java.util.Locale;
import java.util.regex.Pattern;
//import java.util.Timer;
//import java.util.TimerTask;
import com.android.settings.Utils;
import com.android.internal.telephony.TelephonyProperties;
//import com.android.internal.telephony.TelephonyIntents;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
// JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]
//import com.movial.ipphone.IPUtils;

public class WifiApEnabler implements CompoundButton.OnCheckedChangeListener, TextWatcher, DialogInterface.OnClickListener, View.OnClickListener {
    private final Context mContext;
    private Switch mSwitch;

    // DCM CheckBox
    private CheckBoxPreference mCheckBox;
    private CharSequence mOriginalSummary;
    private CheckBoxPreference mTempCheckBox;

    private WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;

    /* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */
    private TelephonyManager    mTelephonyManager       = null;
    //private ConnectivityManager mConnectivityManager    = null;
    /* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */

    ConnectivityManager mCm;
    private String[] mWifiRegexs;
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
    private static final String TAG = "WifiApEnabler";
    private boolean mDelayedApEnabled = false;
    /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */
    public static final String UPSELL_CHECK_SUCCESS = "com.lge.upsell.Upsell.UPSELL_CHECK_SUCCESS";
    public static final String UPSELL_CHECK_FAIL = "com.lge.upsell.Upsell.UPSELL_CHECK_FAIL";
    public static final String UPSELL_SVC_STOP = "com.lge.upsell.Upsell.UPSELL_STOP";
    public static final String UPSELL_SET_HELP_VALUE = "com.lge.upsell.Upsell.HELP_VAL";
    public static final String UPSELL_SVC_STATUS = "status";
    /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */

    /* 130802 soonhyuk.choi@lge.com */
    public static final int UPSELL_SVC_STOPPED = 0;
    public static final int UPSELL_SVC_STARTED = 1;
    public static final int UPSELL_SVC_GOTO_NOSVC = 2;
    public static final int UPSELL_SVC_GOTO_UPGRADE = 3;
    public static final int UPSELL_SVC_DATA_STOPPED = 4;

// LGE_UPDATE_S bluetooth.kang[13/04/15] ATT_FEATURE  Entitlement Check
    public static final String WIFI_ENTITLEMENT_CHECK_OK = "com.android.settings.EntitlementCheckService.WIFI_ENTITLEMENT_CHECK_OK";
    public static final String WIFI_ENTITLEMENT_CHECK_FAILURE = "com.android.settings.EntitlementCheckService.WIFI_ENTITLEMENT_CHECK_FAILURE";
// LGE_UPDATE_E bluetooth.kang[13/04/15] ATT_FEATURE  Entitlement Check

    private View mAlertDialogView;
/*LGE_CHANGE_E, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/

    // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
    //private View mAlertDialogView;
    private AlertDialog.Builder mAlertDialogBuilder;
    private AlertDialog mAlertDialog;
    private EditText mPassword;
    //private boolean mWifiHotspotStatus;
    private static boolean mInit = false;
    private static boolean mDoNotShowAgain = false;
    private boolean mDoNotShowAgainWifiDisabled = false;

    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private static final int PROVISION_REQUEST = 0;

// LGE_CHANGE_S [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    //private int originTimeoutValue = 600000;
    private boolean mIscheckedTethering = false;
//    private Timer mTimer;
//    private Handler mHandler;
//    private int loopCounting;
    private static boolean mTimerSoftApOn = false;
    private static boolean mUpsellSoftApOn = false;
//    private ConnectivityManager mConnMgr;
    //sprivate AlertDialog.Builder wifiErrorDialog;
    //private AlertDialog wifiErrorDlog;
// LGE_CHANGE_E [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable

//    private static final String TETHER_URL = "file:///android_asset/html/%y%z/tether_attention_%x.html";
//    private static final String TETHER_PATH = "html/%y%z/tether_attention_%x.html";
    private View alertDialogView;
    private static final String KDDI_TETHER_URL = "https://cs.kddi.com/smt_i/te/";
    private static final String USC_TETHER_URL = "http://www.uscellular.com/uscellular/common/common.jsp?path=/android/modem_feature.html";
    private WifiConfiguration mWifiConfig;
    private WifiApDialog mDialog;

    private boolean mStateMachineEvent;

// LGE_CHANGE_S [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    private boolean mIsNoSimAlert = false;
//    private boolean mSimEnabled = true;
    //private boolean mCheckingMobileData = false;
    //public static final String  UPSELL_CHECK_USB_SUCCESS = "com.lge.upsell.Upsell.UPSELL_CHECK_USB_SUCCESS";
    //public static final String  UPSELL_CHECK_USB_FAIL = "com.lge.upsell.Upsell.UPSELL_CHECK_USB_FAIL";
// LGE_CHANGE_E [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    //[S]joon7.lim@lge.com Apply first time use dialog popup for NewCo
    private final int REQUEST_FIRST_TIME_USE = 882;
    public final static String SHARED_PREFERENCES_NAME = "first_time_use_Settings";
    public final static String EXTRA_FIRST_HELP_ASK = "help_ask";  // intent ask
    public final static String SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK = "wifi_help_ask";
    //[E]joon7.lim@lge.com Apply first time use dialog popup for NewCo
    // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]
    private int maxPassword = 8;

    public static boolean mbIsWFCRegistered = false;
    public static boolean mbTurnoffWifiForWFC = false;
    public static boolean mbShowingDialogWFC = false;

    private static final boolean mSupported5G = SystemProperties.getBoolean("wlan.lge.softap5g", false);
    // if defined in the array, the 5g softap is blocked by regulation of country.
    public static final String IMS_REGISTRATION = "IMS_REGISTRATION";
    public static final String IMS_REG_STATUS = "IMS_REG_STATUS";
    public String[] mCheckBlock5gbyCC = {
               "AF",  // AFGHANISTAN
               "AL",  // ALBANIA
               "DZ",  // ALGERIA
               "AD",  // ANDORRA
               "AO",  // ANGOLA
               "AM",  // ARMENIA
               "AT",  // AUSTRIA
               "BY",  // BELARUS
               "BE",  // BELGIUM
               "BZ",  // BELIZE
               "BJ",  // BENIN
               "BO",  // BOLIVIA, PLURINATIONAL STATE OF
               "BA",  // BOSNIA AND HERZEGOVINA
               "VG",  // VIRGIN ISLANDS, BRITISH
               "BG",  // BULGARIA
               "BI",  // BURUNDI
               "CM",  // CAMEROON
               "CV",  // CAPE VERDE
               "CF",  // CENTRAL AFRICAN REPUBLIC
               "TD",  // CHAD
               "KM",  // COMOROS
               "CD",  // CONGO, THE DEMOCRATIC REPUBLIC OF THE
               "CG",  // CONGO
               "CI",  // COTE D'IVOIRE
               "HR",  // CROATIA
               "CY",  // CYPRUS
               "CZ",  // CZECH REPUBLIC
               "DK",  // DENMARK
               "DJ",  // DJIBOUTI
               "CQ",  // CYPRUS
               "ER",  // ERITREA
               "EE",  // ESTONIA
               "ET",  // ETHIOPIA
               "FJ",  // FIJI
               "FI",  // FINLAND
               "FR",  // FRANCE
               "GF",  // FRENCH GUIANA
               "PF",  // FRENCH POLYNESIA
               "TF",  // FRENCH SOUTHERN TERRITORIES
               "GA",  // GABON
               "GM",  // GAMBIA
               "GE",  // GEORGIA
               "DE",  // FRENCH SOUTHERN TERRITORIES
               "GH",  // GHANA
               "GR",  // GREECE
               "GP",  // GUADELOUPE
               "GN",  // GUINEA
               "GW",  // GUINEA-BISSAU
               "HU",  // HUNGARY
               "IS",  // ICELAND
               "IQ",  // IRAQ
               "IE",  // IRELAND
               "BO",  // BOLIVIA, PLURINATIONAL STATE OF
               "IT",  // ITALY
               "JP",  // JAPAN
               "KE",  // KENYA
               "KG",  // KYRGYZSTAN
               "KW",  // KUWAIT
               "LV",  // LATVIA
               "LS",  // LESOTHO
               "LY",  // LIBYA
               "LI",  // LIECHTENSTEIN
               "LT",  // LITHUANIA
               "LU",  // LUXEMBOURG
               "MK",  // MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF
               "MG",  // MADAGASCAR
               "ML",  // MALI
               "MT",  // MALTA
               "MQ",  // MARTINIQUE
               "MR",  // MAURITANIA
               "MU",  // MAURITIUS
               "YT",  // MAYOTTE
               "MD",  // MOLDOVA, REPUBLIC OF
               "MC",  // MONACO
               "ME",  // MONTENEGRO
               "MS",  // MONTSERRAT
               "MA",  // MOROCCO
               "IL",
               "MM",  // MYANMAR
               "NR",  // NAURU
               "NL",  // NETHERLANDS
               "NC",  // NEW CALEDONIA
               "NE",  // NIGER
               "NO",  // NORWAY
               "OM",  // OMAN
               "PL",  // POLAND
               "PT",  // PORTUGAL
               "RE",  // REUNION
               "RO",  // ROMANIA
               "MF",  // SAINT MARTIN (FRENCH PART)
               "SM",  // SAN MARINO
               "ST",  // SAO TOME AND PRINCIPE
               "SN",  // SENEGAL
               "RS",  // SERBIA
               "SC",  // SEYCHELLES
               "SL",  // SIERRA LEONE
               "SK",  // SLOVAKIA
               "SI",  // SLOVENIA
               "SB",  // SOLOMON ISLANDS
               "SO",  // SOMALIA
               "ZA",  // SOUTH AFRICA
               "SR",  // SURINAME
               "SZ",  // SWAZILAND
               "SE",  // SWEDEN
               "CH",  //SWITZERLAND
               "TJ",  //TAJIKISTAN
               "TG",  // TOGO
               "TO",  //TONGA
               "TN",  // TUNISIA
               "TR",  // TURKEY
               "TM",  // TURKMENISTAN
               "TC",  //TURKS AND CAICOS ISLANDS
               "TV",  //TUVALU
               "GB",  // UNITED KINGDOM
               "VU",  //VANUATU
               "VA",  // HOLY SEE (VATICAN CITY STATE)
               "FR",  // FRANCE
               "MA",  //MOROCCO
               "YE"  // YEMEN
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

// LGE_UPDATE_S bluetooth.kang[13/04/15] ATT_FEATURE  Entitlement Check
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
/*
            if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
                if (WIFI_ENTITLEMENT_CHECK_OK.equals(action)) {
                    Log.d(TAG, "WIFI_ENTITLEMENT_CHECK_OK recieved");
              setSoftapEnabled(true);
                } else if (WIFI_ENTITLEMENT_CHECK_FAILURE.equals(action)) {
                    Log.d(TAG, "WIFI_ENTITLEMENT_CHECK_FAILURE recieved");
                    setSoftapEnabled(false);
                    setSwitchChecked(false);
                    if (mSwitch != null) {
                        mSwitch.setEnabled(true);
                        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                        }
                        // LGMDM_END
                    }
                }
            }
*/
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
// LGE_UPDATE_E bluetooth.kang[13/04/15] ATT_FEATURE  Entitlement Check

            /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */
            if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                ContentResolver cr = mContext.getContentResolver();

                if (UPSELL_CHECK_SUCCESS.equals(action)) {
                    Log.d(TAG, "000000000000000 Upsell UPSELL_CHECK_SUCCESS recieved 0000000000");
                    //LGE_UPDATE_S  [hoon2.lim@lge.com][2013/02/07] modify for AirPlaneMode Check #302698
                    boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                    Log.w(TAG, this.getClass().getName() + " : " + "isAirplaneMode=" + isAirplaneMode);
                    if (!isAirplaneMode) {
                        setSoftapEnabled(true);
                    }
                     mUpsellSoftApOn = true;
                     Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                    //LGE_UPDATE_E  [hoon2.lim@lge.com][2013/02/07] modify for AirPlaneMode Check #302698
                }
                else if (UPSELL_CHECK_FAIL.equals(action)) {
                    Log.d(TAG, "000000000000000 Upsell UPSELL_CHECK_FAIL recieved 0000000000");
                    setSwitchChecked(false);
                    setSoftapEnabled(false);
                    mSwitch.setEnabled(true);
                    Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                    int wifiSavedState = 0;
                    try {
                        wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
                    } catch (Settings.SettingNotFoundException e) {
                        ;
                    }
                    Log.d(TAG, "wifiSavedState == " + wifiSavedState);
                    if (wifiSavedState == 1) {
                        mWifiManager.setWifiEnabled(true);
                        Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
                        Log.d(TAG, "So, restore Wi-Fi state");
                    }
                }
                else if (UPSELL_SVC_STOP.equals(action)) {
                    int upsellStatus = intent.getIntExtra(UPSELL_SVC_STATUS, 0);
                    Log.d(TAG, "UPSELL_SVC_STOP got : upsellStatus = " + upsellStatus);

                    switch(upsellStatus) {
                        case UPSELL_SVC_GOTO_NOSVC:  // NO data service.
                            Log.d(TAG, "UPSELL_SVC_GOTO_NOSVC ");
                            Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                            setSwitchChecked(false);
                            mSwitch.setEnabled(true);
                            break;

                        case UPSELL_SVC_GOTO_UPGRADE:  // goto upgrade
                            Log.d(TAG, "UPSELL_SVC_GOTO_UPGRADE ");
                            Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                            setSwitchChecked(false);
                            setSoftapEnabled(false);
                            mSwitch.setEnabled(true);
                            break;

                        case UPSELL_SVC_DATA_STOPPED:
                            break;
                            
                        case UPSELL_SVC_STARTED:  // upsell started
                            Log.d(TAG, "UPSELL_SVC_STARTED ");
                            break;
                        
                        default:
                            Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                            break;
                    }
                } 
                else if (UPSELL_SET_HELP_VALUE.equals(action)) {
                    String s = intent.getStringExtra("HelpValue");
                    Log.d(TAG, "000000000000000 Upsell UPSELL_SET_HELP_VALUE recieved 0000000000");
                    if (s != null) {
                        if ("1".equals(s)) {
                            setbFirstTimeUse(1);
                        } else {
                            setbFirstTimeUse(0);
                        }
                    }
                }
                else if (IMS_REGISTRATION.equals(action)) {
                    mbIsWFCRegistered = intent.getBooleanExtra(IMS_REG_STATUS, false);
                    mbTurnoffWifiForWFC = false;
                    Log.d(TAG, "IMS Registration status : " + mbIsWFCRegistered);
                }
            }
            /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */

            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
                updateTetherState(available.toArray(), active.toArray(), errored.toArray());
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                //enableWifiCheckBox();
                if (Config.getOperator().equals("DCM")) {
                    enableWifiCheckBox();
                } else {
                    enableWifiSwitch();
                }
            }
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
            else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
                    if (mDelayedApEnabled) {
                        int mWifiState = intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                        if (mWifiState == WifiManager.WIFI_STATE_DISABLING ||
                            mWifiState == WifiManager.WIFI_STATE_DISABLED) {
                            mDelayedApEnabled = false;
                            setSoftapEnabled(true);
                        }
                    }
                }
            }
/*LGE_CHANGE_E, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
        }
    };
//[jaewoong87.lee@lge.com] 2013.05.23 Fixed for jenkins error[S]
    public WifiApEnabler(Context context, CheckBoxPreference checkBox) {
        Log.d(TAG, "WifiApEnabler overlay checkBox***");
        mContext = context;

        if (Config.getOperator().equals("DCM")) {
            mCheckBox = checkBox;
            mOriginalSummary = checkBox.getSummary();
        }
        checkBox.setPersistent(false);

        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/

        /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            mIntentFilter.addAction(UPSELL_CHECK_SUCCESS);
            mIntentFilter.addAction(UPSELL_CHECK_FAIL);
            mIntentFilter.addAction(UPSELL_SVC_STOP);
            mIntentFilter.addAction(UPSELL_SET_HELP_VALUE);
            mIntentFilter.addAction(IMS_REGISTRATION);
        }
        // mContext.registerReceiver(mReceiver, mIntentFilter);
        /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */

        /* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
        //mConnectivityManager    = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mTelephonyManager       = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        /* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
    } 
//[jaewoong87.lee@lge.com] 2013.05.23 Fixed for jenkins error[E]


// public WifiApEnabler(Context context, CheckBoxPreference checkBox) {
    public WifiApEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        /* Change switch button for hotspot */
//        mCheckBox = null; //fixed jenkins error
//        mOriginalSummary = null; //fixed jenkins error
        //checkBox.setPersistent(false);

        if (Config.getOperator().equals("DCM")) {
            mCheckBox = mTempCheckBox;
            mOriginalSummary = "Wi-Fi Tethering";
        }

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);

        if (Config.getOperator().equals("DCM")) {
            return;
        }

        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
/*LGE_CHANGE_S, [jongpil.yoon@lge.com], 2012-09-15, WiFi state change*/
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
/*
        if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
            mIntentFilter.addAction(WIFI_ENTITLEMENT_CHECK_OK);
            mIntentFilter.addAction(WIFI_ENTITLEMENT_CHECK_FAILURE);
        }
*/
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
        /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            mIntentFilter.addAction(UPSELL_CHECK_SUCCESS);
            mIntentFilter.addAction(UPSELL_CHECK_FAIL);
            mIntentFilter.addAction(UPSELL_SVC_STOP);
            mIntentFilter.addAction(UPSELL_SET_HELP_VALUE);
            mIntentFilter.addAction(IMS_REGISTRATION);
        }
        //        mContext.registerReceiver(mReceiver, mIntentFilter);
        /* 20110616 : Update for Upsell <hyunyong08.park@lge.com> */

        /* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
        //mConnectivityManager    = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mTelephonyManager       = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        /* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 

        // [andrew75.kim@lge.com] hotspot max user settings upon GSM network roaming [S]
        if (Config.getCountry().equals("US") && Config.getOperator().equals("SPR")) {
            int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

            networkType = mTelephonyManager.getNetworkType();
            //Log.d(TAG, "networkType = " + networkType);

            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    if (1 != Settings.System.getInt(mContext.getContentResolver(),
                            "mhs_max_client", 8)) {
                        Settings.System.putInt(mContext.getContentResolver(),
                            "mhs_max_client", 1);
                    }
                    break;
                default:
                    break;
            }
        }
        // [andrew75.kim@lge.com] hotspot max user settings upon GSM network roaming [E]

        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [S]
        SharedPreferences pref_ff = mContext.getSharedPreferences("FIRST_FLAG",
                Activity.MODE_PRIVATE);
        boolean flag_ff = pref_ff.getBoolean("f_flag", true);
        if (flag_ff == true) {
            mInit = true;
        } else {
            mInit = false;
        }

        SharedPreferences pref_ns = mContext.getSharedPreferences("NOT_SHOW",
                Activity.MODE_PRIVATE);
        boolean flag_ns = pref_ns.getBoolean("not_show", false);
        if (flag_ns == true) {
            mDoNotShowAgain = true;
        } else {
            mDoNotShowAgain = false;
        }

        boolean mFlag_ns_wifi_diabled_att = pref_ns.getBoolean("not_show_wifi_disabled_att", false);
        if (mFlag_ns_wifi_diabled_att == true) {
            mDoNotShowAgainWifiDisabled = true;
        } else {
            mDoNotShowAgainWifiDisabled = false;
        }
        // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]

    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);

        if (Config.getOperator().equals("DCM")) {
            enableWifiCheckBox();
        }

        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            resume_switch();
        }       

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][D-MDM-243]
        if (Config.getOperator().equals("DCM") && 
            com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMWifiSettingsAdapter.getInstance().addTetherPolicyChangeIntentFilter(filterLGMDM);
            mContext.registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        if (!Config.getOperator().equals("DCM")) {
            enableWifiSwitch();
            mSwitch.setOnCheckedChangeListener(this);
        }
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        if (!Config.getOperator().equals("DCM")) {
            mSwitch.setOnCheckedChangeListener(null);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][D-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                mContext.unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    private void enableWifiCheckBox() {
        if (!Config.getOperator().equals("DCM")) {
            return;
        }

        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (!isAirplaneMode) {
            mCheckBox.setEnabled(true);
        } else {
            mCheckBox.setSummary(mOriginalSummary);
            mCheckBox.setEnabled(false);
        }
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Global.getInt(mContext.getContentResolver(),
                    "tethering_blocked", 0) == 1) {
                //mCheckBox.setSummary(R.string.tethering_blocked);
                mCheckBox.setEnabled(false);
                mCheckBox.setChecked(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mCheckBox);
        }
        // LGMDM_END
    }

    private void createWifiOffWarningDialogForATT() {
        final ContentResolver cr = mContext.getContentResolver();

        int wifiState = mWifiManager.getWifiState();
        if (((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED))
                && (mDoNotShowAgainWifiDisabled == false)) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog_wifi_disable_att, null);
            ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again_wifi_disabled_att)).setOnClickListener(this);

            AlertDialog altDialog = new AlertDialog.Builder(mContext)
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.sp_wifi_is_activated_att)
                .setView(mAlertDialogView)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final ContentResolver cr = mContext.getContentResolver();
                        Log.d(TAG, "Wi-Fi off in createWifiOffWarningDialogForATT()");
                        mWifiManager.setWifiEnabled(false);
                        Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
                        createHotspotWarningPopupForATT();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setSwitchChecked(false);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        setSwitchChecked(false);
                    }
                })
                .create();
                altDialog.show();
        } else {
            if (mDoNotShowAgainWifiDisabled) {
              Log.d(TAG, "Wi-Fi off in createWifiOffWarningDialogForATT()");
              mWifiManager.setWifiEnabled(false);
              Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
            }
            createHotspotWarningPopupForATT();
        }
    }

    DialogInterface.OnClickListener alertDialogHandlerATT = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
//            int wifiApState = mWifiManager.getWifiApState();
//            int wifiState = mWifiManager.getWifiState();

            if (button == DialogInterface.BUTTON_POSITIVE) {
                int secureIndex = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                if ((secureIndex == WifiApDialog.WPA_INDEX || secureIndex == WifiApDialog.WPA2_INDEX)) {
                    String password = mPassword.getText().toString();
                    mWifiConfig = mWifiManager.getWifiApConfiguration();
                    if (mWifiConfig != null && password != null) {
                        mWifiConfig.preSharedKey = password;
                        Settings.System.putString(mContext.getContentResolver(), "mhs_wpakey", password);
                    }
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
                //setSwitchChecked(true);
                //mSwitch.setEnabled(true);
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
                startProvisioningIfNecessary();
            } else if (button == DialogInterface.BUTTON_NEGATIVE) {
                setSwitchChecked(false);
                setSoftapEnabled(false);
                mSwitch.setEnabled(true);
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                }
                // LGMDM_END
            }
        }
    };

    private void createHotspotWarningPopupForATT() {
//        final ContentResolver cr = mContext.getContentResolver();

        if (mDoNotShowAgain == false) {

            int secureIndex = -1;

            if (mWifiConfig != null) {
                secureIndex = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
            }

            if ((secureIndex == WifiApDialog.WPA_INDEX || secureIndex == WifiApDialog.WPA2_INDEX)) {
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
                mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog_att, null);
                mAlertDialogBuilder = new AlertDialog.Builder(mContext)
                    .setCancelable(false/*true*/)
                    .setTitle(R.string.sp_mobile_hotspot_dialog_att_title)
                    .setView(mAlertDialogView)
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                                }
                                // LGMDM_END
                                dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    })
                    .setPositiveButton(R.string.sp_mobile_hotspot_btn_enable_NORMAL, alertDialogHandlerATT)
                    .setNegativeButton(R.string.sp_mobile_hotspot_btn_disable_NORMAL, alertDialogHandlerATT);

                mPassword = (EditText)mAlertDialogView.findViewById(R.id.password);
                if (mWifiConfig != null) {
                    mPassword.setText(mWifiConfig.preSharedKey);
                }

                mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
                mPassword.addTextChangedListener(mTextWatcher);
                mPassword.requestFocus();
                ((CheckBox)mAlertDialogView.findViewById(R.id.show_password)).setChecked(true);

                ((CheckBox)mAlertDialogView.findViewById(R.id.show_password)).setOnClickListener(this);
                ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again)).setOnClickListener(this);
       
                /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
                // MaxLength, 2012-12-21, ilyong.oh@lge.com
                mAlertDialog = mAlertDialogBuilder.create();
                //[jaewoong87.lee@lge.com] 2013.06.10 fix Wi-Fi hotspot keypad donesn't come up.
                mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                mAlertDialog.show();
            } else {
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog, null);
                mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog_att_open, null);

                mAlertDialogBuilder = new AlertDialog.Builder(mContext)
                    .setCancelable(false/*true*/)
                    .setTitle(R.string.sp_mobile_hotspot_dialog_att_title)
                    //.setIconAttribute(android.R.attr.alertDialogIcon)
                    .setView(mAlertDialogView)
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                setSwitchChecked(false);
                                mSwitch.setEnabled(true);
                                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                                }
                                // LGMDM_END
                                mDoNotShowAgain = false;
                                dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    })
                    .setPositiveButton(R.string.sp_mobile_hotspot_btn_enable_NORMAL, alertDialogHandlerATT)
                    .setNegativeButton(R.string.sp_mobile_hotspot_btn_disable_NORMAL, alertDialogHandlerATT);

                ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again)).setOnClickListener(this);
                mAlertDialog = mAlertDialogBuilder.create();
                mAlertDialog.show();                        
            }
        } else {
            startProvisioningIfNecessary();
        }
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final EditText textfield = mPassword;
            if (mAlertDialog instanceof AlertDialog) {
                if (textfield.getText().length() >= maxPassword) {
                    if (((AlertDialog)mAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE) != null) { //WBT 540460 2013.12.24 eunjungjud.kim
                        ((AlertDialog)mAlertDialog)
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);
                    }
                } else {
                    if (((AlertDialog)mAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE) != null) { //WBT 540461 2013.12.24 eunjungjud.kim
                         ((AlertDialog)mAlertDialog)
                         .getButton(AlertDialog.BUTTON_POSITIVE)
                         .setEnabled(false);
                    }
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void setSoftapEnabled(boolean enable) {
        //[jaewoong87.lee@lge.com] 2013.05.16 Switch off if airplane mode in on [S]    
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (Config.getOperator().equals("DCM") && isAirplaneMode) {            
            mCheckBox.setEnabled(false);  
            return;
        }

        final ContentResolver cr = mContext.getContentResolver();
        
        int wifiState = mWifiManager.getWifiState();
        Log.d(TAG, "setSoftapEnabled(),wifiState = " + wifiState);
        if ((wifiState == WifiManager.WIFI_STATE_ENABLING) || (wifiState == WifiManager.WIFI_STATE_ENABLED)) {
            if ( enable ) { // WiFi Off when enable(true)
                mWifiManager.setWifiEnabled(false);

                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
            }
            //Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (mSupported5G == true) {
            
            if (enable && !check5gApSupportedbyCC() &&
                Settings.System.getInt(cr, "mhs_frequency", 0) != 0) {
                Settings.System.putInt(cr, "mhs_frequency", 0);
            }            
        }

        if (mWifiManager.setWifiApEnabled(null, enable)) {
            /* Disable here, enabled on receiving success broadcast */
            if (Config.getOperator().equals("DCM")) {
                mCheckBox.setEnabled(false);
            } else {
                mSwitch.setEnabled(false);
            }
        } else {
            if (Config.getOperator().equals("DCM")) {
                mCheckBox.setSummary(R.string.wifi_error);
            }
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        } else { // sunghee78.lee@lge.com 2012.08.15 Wi-Fi hotspot enable message display modify.
            Toast mToast = null;
            int margin = 0;
            if ("KR".equals(Config.getCountry())) {
                Log.d(TAG, " condition1  == " + mCm.getMobileDataEnabled());
                Log.d(TAG, " condition2  == " + !mTelephonyManager.hasIccCard());
             
                /* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
                if ( checkLGU() && ( (mCm.getMobileDataEnabled() == false)  
                    || !mTelephonyManager.hasIccCard()) ) { 
                    mToast = Toast.makeText(mContext, R.string.sp_wifi_hotspot_enable_warning_network_off_toast_NORMAL, Toast.LENGTH_LONG);
                    margin = getToastMargin();
                    mToast.setGravity(Gravity.CENTER, 0, margin);
                    mToast.show();
                }
                else
                {
                    mToast = Toast.makeText(mContext, R.string.sp_wifi_hotspot_enable_warning_toast_NORMAL, Toast.LENGTH_LONG);
                    margin = getToastMargin();
                    mToast.setGravity(Gravity.CENTER, 0, margin);
                    mToast.show();
                }
                /* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
            }
// TD_36048
// Occurs twice in the same pop-up about 70% during test.
//                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
//                if ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
//                       (wifiState == WifiManager.WIFI_STATE_ENABLED)) {
//                    Toast.makeText(mContext,
//                        R.string.tmo_wifi_off_toast, Toast.LENGTH_LONG).show();
//                }
//            }
        }
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
        if (!Config.getOperator().equals("DCM")) {
            return;
        }

        String s = mContext.getString(
                com.android.internal.R.string.wifi_tether_configure_ssid_default);
        mCheckBox.setSummary(String.format(
                    mContext.getString(R.string.wifi_tether_enabled_subtext),
                    (wifiConfig == null) ? s : wifiConfig.SSID));
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiTethered = true;
            }
        }
        for (Object o: errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiErrored = true;
            }
        }

        if (wifiTethered) {
            WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
            updateConfigSummary(wifiConfig);
        } else if (wifiErrored && Config.getOperator().equals("DCM")) {
            mCheckBox.setSummary(R.string.wifi_error);
        }
    }

    private void handleWifiApStateChanged(int state) {
        if (Config.getOperator().equals("DCM")) {
            switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
                mCheckBox.setSummary(R.string.wifi_hotspot_starting);   //sangheon.shim@lge.com.2012.12.26 Turning Wi-Fi on --> Turning on.
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /**
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
                mCheckBox.setChecked(true);
                /* Doesnt need the airplane check */
                mCheckBox.setEnabled(true);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                mCheckBox.setSummary(R.string.wifi_hotspot_stopping);   //sangheon.shim@lge.com.2012.12.26 Turning Wi-Fi off --> Turning off.
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(mOriginalSummary);
                enableWifiCheckBox();
                break;
            default:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(R.string.wifi_error);
                enableWifiCheckBox();
            }
        } else {
            switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
                //mCheckBox.setSummary(R.string.wifi_hotspot_starting);   //sangheon.shim@lge.com.2012.12.26 Turning Wi-Fi on --> Turning on.
                //mCheckBox.setEnabled(false);
                
                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                    mUpsellSoftApOn = false;
                } else if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
                    // LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.08.23 AT&T Switch UI
                    setSwitchChecked(true);
                }
                // LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.08.23 AT&T Switch UI
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /**
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
                //mCheckBox.setChecked(true);
                /* Doesnt need the airplane check */
                //mCheckBox.setEnabled(true);
                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                    mUpsellSoftApOn = false;
                }
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                    mUpsellSoftApOn = false;
                }                
                //mCheckBox.setSummary(R.string.wifi_hotspot_stopping);   //sangheon.shim@lge.com.2012.12.26 Turning Wi-Fi off --> Turning off.
                //mCheckBox.setEnabled(false);
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                //mCheckBox.setChecked(false);
                //mCheckBox.setSummary(mOriginalSummary);
                //enableWifiCheckBox();
                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                        ContentResolver cr = mContext.getContentResolver();
                    if (Settings.Secure.getInt(cr, "upsell_svc_started", 0) == 0) {  // for exeption routine
                        Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
                    }
                    if ((mTimerSoftApOn) ||
                            (Settings.Secure.getInt(cr, "upsell_check_ongoing", 0) == 1)) {
                        setSwitchChecked(true);
                        enableWifiSwitchDuringUpsell();
                    } else {
                        setSwitchChecked(false);
                        enableWifiSwitch();
                    }
                } else {
                    setSwitchChecked(false);
                    enableWifiSwitch();
                }
                break;
            default:
                //mCheckBox.setChecked(false);
                //mCheckBox.setSummary(R.string.wifi_error);
                //enableWifiCheckBox();
                setSwitchChecked(false);
                enableWifiSwitch();
                break;
            }
        }
    }

    // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    private void enableWifiSwitchDuringUpsell()
    {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                                 Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {

            int currentSimStatus = TelephonyManager.getDefault().getSimState();            
            if (currentSimStatus != TelephonyManager.SIM_STATE_READY)
            {
                mSwitch.setChecked(false);
            }

            else if (!isAirplaneMode)
            {
                mSwitch.setEnabled(false);
            }
            else
            {            
                mSwitch.setEnabled(false);
            }               
        }
        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Global.getInt(mContext.getContentResolver(),
                    "tethering_blocked", 0) == 1) {
                //mCheckBox.setSummary(R.string.tethering_blocked);
                mSwitch.setEnabled(false);
                setSwitchChecked(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void resume_switch() {
        Log.d(TAG, "resume_switch()");
        
        final int wifiApState = mWifiManager.getWifiApState();
//        boolean isEnabled = wifiApState == WifiManager.WIFI_AP_STATE_ENABLED;
//        boolean isDisabled = wifiApState == WifiManager.WIFI_AP_STATE_DISABLED;
        ContentResolver cr = mContext.getContentResolver();

        if (Settings.Secure.getInt(cr, "upsell_svc_started", 0) == 0) {  // for exeption routine
            Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
        }

        if ((mTimerSoftApOn) || 
                (Settings.Secure.getInt(cr, "upsell_check_ongoing", 0) == 1)) {
            setSwitchChecked(true);
        } else {
            switch (wifiApState)
            {
                case WifiManager.WIFI_AP_STATE_ENABLING:
                    setSwitchChecked(true);
                    break;
                case WifiManager.WIFI_AP_STATE_ENABLED:
                    setSwitchChecked(true);
                    break;
                default:
                    Log.d(TAG, "setSwitch(), wifiApState = " + wifiApState);
                    Log.d(TAG, "setSwitch(), mUpsellSoftApOn = " + mUpsellSoftApOn);
                    if (mUpsellSoftApOn) {
                        setSwitchChecked(true);
                    } else {
                        setSwitchChecked(false);
                    }
                    break;
            }
        }
    }

    private void enableWifiSwitch()
    {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                                 Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {

            int currentSimStatus = TelephonyManager.getDefault().getSimState();            
            if (currentSimStatus != TelephonyManager.SIM_STATE_READY)
            {
                mSwitch.setChecked(false);
            }

            else if (!isAirplaneMode)
            {
                mSwitch.setEnabled(true);
            }
            else
            {            
                mSwitch.setEnabled(false);
            }               
        }
        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]

        else {
            if (Config.getOperator().equals("ATT")) {
                final int wifiApState = mWifiManager.getWifiApState();
                boolean isEnabled = wifiApState == WifiManager.WIFI_AP_STATE_ENABLED;
                boolean isDisabled = wifiApState == WifiManager.WIFI_AP_STATE_DISABLED;

                if (WifiManager.WIFI_AP_STATE_DISABLED == wifiApState) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(isEnabled || isDisabled);
                }
            }
            if (!isAirplaneMode)
            {
                mSwitch.setEnabled(true);
            }
            else
            {
                mSwitch.setEnabled(false);
            }
        }
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Global.getInt(mContext.getContentResolver(),
                    "tethering_blocked", 0) == 1) {
               // mSwitch.setSummary(R.string.tethering_blocked);
                mSwitch.setEnabled(false);
                setSwitchChecked(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_ && !(Config.getOperator().equals("ATT"))) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;

        if (Config.getOperator().equals("DCM")) {
            return;
        }

        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });

        final int wifiApState = mWifiManager.getWifiApState();
        boolean isEnabled = wifiApState == WifiManager.WIFI_AP_STATE_ENABLED;
        boolean isDisabled = wifiApState == WifiManager.WIFI_AP_STATE_DISABLED;
        //mSwitch.setChecked(isEnabled);
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            ContentResolver cr = mContext.getContentResolver();
            if (Settings.Secure.getInt(cr, "upsell_svc_started", 0) == 0) {  // for exeption routine
                Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
            }

            if ((mTimerSoftApOn) ||
                    (Settings.Secure.getInt(cr, "upsell_check_ongoing", 0) == 1)) {
                setSwitchChecked(true);
                mSwitch.setEnabled(false);
            } else {
                switch (wifiApState)
                {
                    case WifiManager.WIFI_AP_STATE_ENABLING:
                        setSwitchChecked(true);
                        mSwitch.setEnabled(false);
                        break;
                    case WifiManager.WIFI_AP_STATE_ENABLED:
                        setSwitchChecked(true);
                        mSwitch.setEnabled(true);
                        break;
                    default:
                        Log.d(TAG, "setSwitch(), wifiApState = " + wifiApState);
                        Log.d(TAG, "setSwitch(), mUpsellSoftApOn = " + mUpsellSoftApOn);
                        if (mUpsellSoftApOn) {
                            setSwitchChecked(true);
                            mSwitch.setEnabled(false);
                        } else {
                            setSwitchChecked(isEnabled);
                            mSwitch.setEnabled(true);
                            mSwitch.setEnabled(isEnabled || isDisabled);
                        }
                        break;
                }
            }
        }
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.08.23 AT&T Switch UI
        else if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                Log.d(TAG, "setSwitch(), mAlertDialog.isShowing");
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
            } else {
                // add conditon ( Select to Wi-Fi ON ( Wi-Fi ON -> Wi-Fi OFF -> Wi-Fi ON )
                if(WifiManager.WIFI_AP_STATE_ENABLING == wifiApState) {
                    setSwitchChecked(true);
                    mSwitch.setEnabled(false);
                } else if (WifiManager.WIFI_AP_STATE_DISABLED == wifiApState) { //
                    setSwitchChecked(false);
                    mSwitch.setEnabled(isEnabled || isDisabled);
                } else {
                    setSwitchChecked(isEnabled);
                    mSwitch.setEnabled(isEnabled || isDisabled);
                }
            }
            Log.d(TAG, "mSwitch(), getSwitchChecked()" + mSwitch.isChecked());
            Log.d(TAG, "mSwitch(), getEnabled()" + mSwitch.isEnabled());
        }
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.08.23 AT&T Switch UI
        else {
            // add conditon ( Select to Wi-Fi ON ( Wi-Fi ON -> Wi-Fi OFF -> Wi-Fi ON )
            if(WifiManager.WIFI_AP_STATE_ENABLING == wifiApState) {
                setSwitchChecked(true);
                mSwitch.setEnabled(false);
            } else {
                setSwitchChecked(isEnabled);
                mSwitch.setEnabled(isEnabled || isDisabled);
            }
        }
        //[START][TD#20571] Swith should be disabled when AirplaneMode. 2013-04-30, ilyong.oh@lge.com
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                                 Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (isAirplaneMode)
        {
            mSwitch.setEnabled(false);
        }
        //[E N D][TD#20571] Swith should be disabled when AirplaneMode. 2013-04-30, ilyong.oh@lge.com

        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            int currentSimStatus = TelephonyManager.getDefault().getSimState();        
            if (currentSimStatus != TelephonyManager.SIM_STATE_READY) {
                mSwitch.setChecked(false);
            }
        }
        //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }

        mWifiConfig = mWifiManager.getWifiApConfiguration();

        //wbt #544335
        int secureIndex = -1;
        if (mWifiConfig != null) {
            secureIndex = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
        }
        
// LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check    
        if (Config.getCountry().equals("US") && Config.getOperator().equals("ATT")) {
            mProvisionApp = mContext.getResources().getStringArray(R.array.config_mobile_hotspot_provision_app);
        } else {
            mProvisionApp = mContext.getResources().getStringArray(com.android.internal.R.array.config_mobile_hotspot_provision_app);
        }
// LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check

        //setSoftapEnabled(isChecked);
        boolean enable = (Boolean) isChecked;
        //mWifiHotspotStatus = enable;
        Log.e(TAG, "onCheckedChanged(), enable = " + enable);
        // LGE_CHANGE_S [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
        if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
            int currentSimStatus = TelephonyManager.getDefault().getSimState();            
            if (currentSimStatus != TelephonyManager.SIM_STATE_READY) {
                updateTetherEnable();
                enableWifiSwitch();
            }
            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]
            else if (enable) {
                SharedPreferences pref_ff = mContext.getSharedPreferences("FIRST_FLAG", Activity.MODE_PRIVATE);
                boolean flag_ff = pref_ff.getBoolean("f_flag", true);
                if (flag_ff == true) {
                    mInit = true;
                } else {
                    mInit = false;
                }
                Log.e(TAG, "onCheckedChanged(), mInit = " + mInit);
                if ( mWifiConfig != null && enable == true && mInit) {
                    Log.w(TAG, this.getClass().getName() + " : " + "showDialog()");           
                    mIscheckedTethering = true;
                    //showDialog(DIALOG_AP_SETTINGS);
                    mDialog = new WifiApDialog(mContext, this, mWifiConfig);
                    // Configure Wi-Fi hotspot keypad doesn't come up, 20130330, hyeondug.yeo@lge.com [S]
                    mDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    // Configure Wi-Fi hotspot keypad doesn't come up, 20130330, hyeondug.yeo@lge.com [E]
                    setSwitchChecked(false);
                    mDialog.show();
                } else if ( mWifiConfig != null && enable == true && !mInit) {
                    Log.w(TAG, this.getClass().getName() + " : " + "checkProvWithUpsell()");                              
                    checkProvWithUpsell();
                } else {
                    Log.w(TAG, this.getClass().getName() + " : " + "startUpsell()");
                    startUpsell();
                }
            } else  {
                setSoftapEnabled(false);
            }
        }
        // LGE_CHANGE_E [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
        else {
            if (enable) {
                if ("DCM".equals(Config.getOperator()) || ("KDDI".equals(Config.getOperator()) && !SystemProperties.get("ro.lge.sub_operator").equals("KDDI_LCC"))) { // moon-wifi@lge.com
                                                              // by wo0ngs 20121221,
                                                              // for Except Docomo. added KDDI by hyunseong.lee 2013.07.10
                    startProvisioningIfNecessary();
                } else if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
                    createWifiOffWarningDialogForATT();
                } else if (Config.getOperator().equals("USC") && Config.getCountry().equals("US")) {
                    CreateWarningDialogForUSC();
                } else {
                    //[TD#27119] Refresh mInit already f_flag is set false. 2013-05-14, ilyong.oh@lge.com
                    SharedPreferences pref_ff = mContext.getSharedPreferences("FIRST_FLAG",
                            Activity.MODE_PRIVATE);
                    mInit = pref_ff.getBoolean("f_flag", true);

                    if (mInit
                            && (secureIndex == WifiApDialog.WPA_INDEX || secureIndex == WifiApDialog.WPA2_INDEX)) {
                        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
                                Context.LAYOUT_INFLATER_SERVICE);
                        mAlertDialogView = inflater.inflate(R.layout.wifi_pw_dialog, null);

                        mAlertDialogBuilder = new AlertDialog.Builder(mContext)
                            .setCancelable(false/*true*/)
                            .setTitle(R.string.sp_wifi_hotspot_set_up_title_NORMAL)
                            .setView(mAlertDialogView)
                            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode,
                                        KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        setSwitchChecked(false);
                                        //[TD#189574]Wi-Fi Hotspot off when BUTTON_NEGATIVE
                                        setSoftapEnabled(false);
                                        mSwitch.setEnabled(true);
                                        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                                            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                                        }
                                        // LGMDM_END
                                        dialog.dismiss();
                                        return true;
                                    }
                                    return false;
                                }
                            })
                            .setPositiveButton(R.string.wifi_save, pwDialogHandler)
                            .setNegativeButton(R.string.wifi_cancel, pwDialogHandler);

                        mPassword = (EditText)mAlertDialogView.findViewById(R.id.password);
                        // [START][TD#245041] Applied Toast When input
                        // MaxLength, 2012-12-21, ilyong.oh@lge.com
                        // Password EditText length Fix(63)
                        mPassword.setFilters(new InputFilter[] {
                                new Utf8ByteLengthFilter(63)
                            });
                        // [E N D][TD#245041] Applied Toast When input
                        /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
                        if (Config.getOperator().equals("TEL") && Config.getCountry().equals("AU")) { //moon-wifi@lge.com by kwisuk.kwon 20130611 Telstra HotSpot
                            mPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        }

                        // LGE_CHANGE_S [neo_wifi@lge.com] 2013.05.02 change Popup for <CDR-LAN-1470> ATT requirement
                        if (Config.getOperator().equals("ATT") && Config.getCountry().equals("US")) {
                            if (mWifiConfig != null) {
                                mPassword.setText(mWifiConfig.preSharedKey);
                            }
                            //[jaewoong87.lee@lge.com] block to show nomal keboard TD#36836
                            //mPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            ((CheckBox)mAlertDialogView.findViewById(R.id.show_password)).setChecked(true);
                        }
                        // LGE_CHANGE_E [neo_wifi@lge.com] 2013.05.02 change Popup for <CDR-LAN-1470> ATT requirement

                        /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
                        // MaxLength, 2012-12-21, ilyong.oh@lge.com
                        mPassword.addTextChangedListener(this);
                        ((CheckBox)mAlertDialogView.findViewById(R.id.show_password))
                                .setOnClickListener(this);

                        mAlertDialog = mAlertDialogBuilder.create();
                        // remove this RESIZE set because of UI Scenario do not
                        // have a scrolling UI. //hoon2.lim modify for scrolling
                        // wifipwdialog.xml #TD250327
                        // mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        mAlertDialog.show();

                        // seodongjo@lge.com 121015 fixed display keypad(
                        // password at first hotspot on).
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                                    InputMethodManager mImm = (InputMethodManager)mAlertDialog
                                            .getContext().
                                            getSystemService(Context.INPUT_METHOD_SERVICE);
                                    mImm.showSoftInput(mPassword, InputMethodManager.SHOW_IMPLICIT);
                                } catch (Exception e) {
                                    ;
                                }
                            }
                        }, 150);
                        if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) { //WBT 540462  2013.12.24 eunjungjud.kim
                            mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        }
                    } else {
                        if (mDoNotShowAgain == false) {
                            // && "DCM".equals(Config.getOperator()) ==
                            // false){//moon-wifi@lge.com by wo0ngs 20121023,
                            // for Except Docomo
                            LayoutInflater inflater = (LayoutInflater)mContext
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            
                         // 2013.02.04, jaeshick: If start hotspot with OPEN, show warning message about open AP for ORG
                            if ( "ORG".equals(Config.getOperator()) && ((mWifiConfig != null) && (WifiApDialog.getSecurityTypeIndex(mWifiConfig) == WifiApDialog.OPEN_INDEX)) ) {
                                mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog_start_open, null);
                            } else {
                                mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog, null);
                            }

                            mAlertDialogBuilder = new AlertDialog.Builder(mContext)
                                    .setCancelable(false/*true*/)
                                    .setTitle(R.string.sp_wifi_hotspot_is_on_NORMAL_1)
                                    //.setIconAttribute(android.R.attr.alertDialogIcon) // Alert icon add
                                    .setView(mAlertDialogView)
                                    // 20121203, connectivity, sijeon@lge.com,
                                    // TD:240978 add Back key action [start]
                                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialog, int keyCode,
                                                KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                Log.e(TAG, "[myseokil] alertDialogHandler - KEYCODE_BACK");
                                                setSwitchChecked(false);
                                                //[TD#189574]Wi-Fi Hotspot off when BUTTON_NEGATIVE
                                                setSoftapEnabled(false);
                                                mSwitch.setEnabled(true);
                                                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                                                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                                                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                                                }
                                                // LGMDM_END
                                                mDoNotShowAgain = false;
                                                dialog.dismiss();
                                                return true;
                                            }
                                            return false;
                                        }
                                    })
                                    // 20121203, connectivity, sijeon@lge.com,
                                    // TD:240978 add Back key action [end]

                                    .setPositiveButton(R.string.yes, alertDialogHandler)
                                    .setNegativeButton(R.string.no, alertDialogHandler);

                            ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again))
                                    .setOnClickListener(this);

                            mAlertDialog = mAlertDialogBuilder.create();
                            mAlertDialog.show();
                        // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
                        } else if (Utils.isUI_4_1_model(mContext) &&
                                Config.getOperator().equals(Config.KT)) {
                            createHotspotWarningPopupForKT();
                        } else {
                            startProvisioningIfNecessary();
                        }
                    }
                }
            }
            else {
                setSoftapEnabled(false);
            }
        }
    }

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

        if ("KR".equals(Config.getCountry())) {
            if (bytelen != mPassword.getText().length()) {
                if (mDoNotShowAgain == false) {
                    if (checkFormValid(mPassword.getText().toString())) { // seodongjo@lge.com
                                                                          // #140563
                                                                          // 2012.08.09
                        Toast.makeText(mContext.getApplicationContext(),
                                R.string.sp_wifihotspot_password_korean_NOTSUPPORT,
                                Toast.LENGTH_SHORT).show();
                        mDoNotShowAgain = true;
                    }
                }
            } else {
                if (mDoNotShowAgain == true) {
                    mDoNotShowAgain = false;
                }
            }
        }
        validate();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    // JB : Wi-Fi hotspot popup, 120918, hyeondug@lge.com
    public void setInitFalse() {
        mInit = false;
        SharedPreferences pref_ff = mContext.getSharedPreferences("FIRST_FLAG",
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
                mWifiConfig = mWifiManager.getWifiApConfiguration();
                if (mWifiConfig != null) {
                    mWifiConfig.preSharedKey = password;
                    Settings.System.putString(mContext.getContentResolver(), "mhs_wpakey", password);
                }
                mWifiManager.setWifiApConfiguration(mWifiConfig);
                setInitFalse();
                startProvisioningIfNecessary();
            }
            else if (button == DialogInterface.BUTTON_NEGATIVE)
            {
                setSwitchChecked(false);
                //[TD#189574]Wi-Fi Hotspot off when BUTTON_NEGATIVE
                setSoftapEnabled(false);
                mSwitch.setEnabled(true);
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                }
                // LGMDM_END
                mPassword.setText("");
            }
        }
    };

    DialogInterface.OnClickListener alertDialogHandler = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
// [[ 2013.03.19 jonghyun.seon add battery popup 
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            int wifiApState = mWifiManager.getWifiApState();
//            int wifiState = mWifiManager.getWifiState();
            String operator_numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);

            //seodongjo@lge.com #30287 Add Check condition AP enable.
            boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
// ]] 2013.03.19 jonghyun.seon add battery popup 

            if (button == DialogInterface.BUTTON_POSITIVE) {

                //seodongjo@lge.com #30287 Add Check condition AP enable.
                if (isAirplaneMode) {
                    Log.e(TAG, " Start AP Check : alrey AP Airplain Mode ");
                    setSwitchChecked(false);
                    mSwitch.setEnabled(false);
                    return;
                }
   
                // seodongjo@lge.com #20885 Add Check condition AP enable.
                if( wifiApState == WifiManager.WIFI_AP_STATE_ENABLED ) {
                    Log.e(TAG, " Start AP Check : alrey AP enabled ");
                    setSwitchChecked(true);
                    mSwitch.setEnabled(true);
                } else {
                    Log.e(TAG, " Start AP Check : now start AP enable ");
                    if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                        if (isTmusSimCard(operator_numeric)) {
                            startUpsell();
                        } else {
                            setSoftapEnabled(true);
                            if ( !getbFirstTimeUse()) {
                                Intent intent = new Intent();
                                intent.putExtra("select", 1);
                                intent.setClassName("com.lge.upsell", "com.lge.upsell.FirstTimeUse");
                                ((Activity)mContext).startActivityForResult(intent, REQUEST_FIRST_TIME_USE);
                            }
                        }
                    // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
                    } else if (Utils.isUI_4_1_model(mContext) &&
                               Config.getOperator().equals(Config.KT)) {
                        createHotspotWarningPopupForKT();
                    } else {
                        startProvisioningIfNecessary();
                    }
                }
            } else if (button == DialogInterface.BUTTON_NEGATIVE) {
                setSwitchChecked(false);
                //[TD#189574]Wi-Fi Hotspot off when BUTTON_NEGATIVE
                setSoftapEnabled(false);
                mSwitch.setEnabled(true);

                // seodongjo@lge.com #30287 Add Check condition AP enable.
                if (isAirplaneMode) {
                    Log.e(TAG, " Start AP Check : alrey AP Airplain Mode ");
                    mSwitch.setEnabled(false);
                    return;
                }
                
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                }
                // LGMDM_END
            }
        }
    };

    // LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check
    protected void ShowWarningDialogForATT()
    {
/*
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        TextView textView = new TextView(mContext);
        final CheckBox checkBox = new CheckBox(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);

        linearLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        linearLayout.setOrientation(1);

        //textView.setText("As other devices are connected to Wi-Fi hotspot through 3G/4G network on this device, it may charge and battery may run out quickly. Apply this?");
        //checkBox.setText("Do not show again");
        textView.setText(R.string.sp_wifi_hotspot_eanble_warning_consent_NORMAL);
        checkBox.setText(R.string.sp_do_not_show_again_NORMAL);

        textView.setPaddingRelative(16, 8, 16, 1);
        textView.setTextSize(18);

        linearLayout.addView(textView);
        linearLayout.addView(checkBox);
        alertDialog.setView(linearLayout);
        alertDialog.setTitle(R.string.sp_wifi_tether_notice_title_attention_SHORT);
        alertDialog.setIconAttribute(android.R.attr.alertDialogIcon);

        alertDialog.setPositiveButton(R.string.sp_wifi_tether_notice_button_OK_SHORT, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if(checkBox.isChecked())
                    Settings.System.putInt(mContext.getContentResolver(),"tether_use_first_time", 1);
                setSoftapEnabled(true);
            }
        });
        alertDialog.setNegativeButton(R.string.sp_wifi_tether_notice_button_cancel_SHORT, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        alertDialog.show();
*/
    }
    // LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check

    boolean isProvisioningNeeded(String[] provisionApp) {
        // LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check
        if ("US".equals(Config.getCountry()) && "ATT".equals(Config.getOperator())) { // Case for preprovisioning.
            if (Settings.System.getInt(mContext.getContentResolver(), SettingsConstants.System.TETHER_ENTITLEMENT_CHECK_STATE, 1) > 0) {
                 Log.e(TAG, "[TetherSettings] Need to provision for AT&T");
                 return true;
            } else {
                Log.e(TAG, "[TetherSettings] Provisioning for AT&T is already done");
                return false;
            }
        }
        // LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check

        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)
                || provisionApp == null) {
            return false;
        }
        return (provisionApp.length == 2);
    }

    private void startProvisioningIfNecessary() {
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
        if ("US".equals(Config.getCountry()) && "ATT".equals(Config.getOperator())) {
            startTethering();
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
        } else {
        if (isProvisioningNeeded(mProvisionApp)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);

            // LGE_UPDATE_S bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check
// LGE_CHANGE_S [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
//                if ("US".equals(Config.getCountry()) && "ATT".equals(Config.getOperator())) {
//              Log.d(TAG, "[TetherSettings] Make Intent for Entitlement");
//                    intent.putExtra("Tether_Type", "WIFI");
//                }
// LGE_CHANGE_E [jeongwook.kim@lge.com] 2013.10.28 AT&T change EntitlementCheck
            // LGE_UPDATE_E bluetooth.kang[11/07/02] ATT_FEATURE  Entitlement Check

            intent.setClassName(mProvisionApp[0], mProvisionApp[1]);
            intent.putExtra("TETHER_TYPE", 0); // TETHER_CHOICE, mTetherChoice
            ((Activity)mContext).startActivityForResult(intent, PROVISION_REQUEST);
        } else {
            startTethering();
            }
        }
    }

    private void startTethering() {
        /*if ("DCM".equals(Config.getOperator())) {
            CreateWarningDialogForDCM();
        }*/
        //LGE_CHANGE_S [WiFi][hyunseong.lee@lge.com] 2013.07.10, Add the Wi-Fi hotspot attention popup for KDDI.
        //else 
        if ("KDDI".equals(Config.getOperator()) && !SystemProperties.get("ro.lge.sub_operator").equals("KDDI_LCC")) {
            int checkShow = Settings.System.getInt(mContext.getContentResolver(), "TETHER_POPUP_KDDI", 0);
            Log.d(TAG, "[KDDI] show again : " + checkShow);
            if (checkShow == 0) {
                CreateWarningDialogForKDDI();
            }
            else {
                setSoftapEnabled(true);
            }
        //LGE_CHANGE_E [WiFi][hyunseong.lee@lge.com] 2013.07.10, Add the Wi-Fi hotspot attention popup for KDDI.
        }
        else {
            setSoftapEnabled(true);
        }
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
            Log.e(TAG, "[YHD] onClick1(View view) : SHOW_DIALOG ");
            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
            if (SystemProperties.get("ro.build.target_country").equals("AU") && Config.getOperator().equals("TEL")) {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_NUMBER | (((CheckBox)view).isChecked() ?
                    InputType.TYPE_NUMBER_VARIATION_NORMAL :
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD));
            }
            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
            else {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox)view).isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            }
            mPassword.setSelection(mPassword.length());
        }

        else if (view.getId() == R.id.do_not_show_again || view.getId() == R.id.do_not_show_again_wifi_disabled_att) {
            Log.e(TAG, "[YHD] onClick1(View view) : DO_NOT_SHOW_AGAIN ");
            String mValName = "not_show";

            if (view.getId() == R.id.do_not_show_again_wifi_disabled_att) {
                mValName = "not_show_wifi_disabled_att";
            }

            if (((CheckBox)view).isChecked()) {
                SharedPreferences pref_ns = mContext.getSharedPreferences("NOT_SHOW",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref_ns.edit();
                editor.putBoolean(mValName, true);
                editor.commit();

                mDoNotShowAgain = true;
                if (view.getId() == R.id.do_not_show_again_wifi_disabled_att) {
                    mDoNotShowAgainWifiDisabled = true;
                    return;
                }

                // JB+ : Disable "No" button when "Do not show again" checked., 130103, hyeondug.yeo@lge.com
                if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
                    mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                }
            } else {
                SharedPreferences pref_ns = mContext.getSharedPreferences("NOT_SHOW",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref_ns.edit();
                editor.putBoolean(mValName, false);
                editor.commit();

                mDoNotShowAgain = false;
                if (view.getId() == R.id.do_not_show_again_wifi_disabled_att) {
                    mDoNotShowAgainWifiDisabled = false;
                    return;
                }

                // JB+ : Disable "No" button when "Do not show again" checked., 130103, hyeondug.yeo@lge.com
                if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
                    mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                }
            }
            Log.e(TAG, "[YHD] mDoNotShowAgain = " + mDoNotShowAgain);
        }
    }
    // JB : Wi-Fi hotspot popup, 120918, hyeondug.yeo@lge.com [E]

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            if (!Config.getOperator().equals("SPR") || !Config.getOperator().equals("BM")) { 
                mDialog.commitMHPChanges();
                mWifiConfig = mDialog.getAppliedConfig();
            } else {
                mWifiConfig = mDialog.getConfig();
            }

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
//                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                //mCreateNetwork.setSummary(String.format(mContext.getString(CONFIG_SUBTEXT), mWifiConfig.SSID, mSecurityType[index]));

                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                    if (mInit == true && mIscheckedTethering) {
                        Log.d(TAG, "[TetherSettings]    checkProvWithUpsell( ) ");
                        Intent intent = new Intent();
                        intent.setAction("com.lge.settings.WifiApEnabler.UpdateTethering");
                        mContext.sendBroadcast(intent);
                        checkProvWithUpsell();
                    }
                }

                setInitFalse(); // JB : Wi-Fi hotspot popup, 120918,
                                // hyeondug.yeo@lge.com
            }
        }
    }

    // LGE_UPDATE_S, moon-wifi@lge.com by wo0ngs 20121023, for Docomo Wi-Fi
    // Hotspot Notice Dialog
    /*
    protected boolean CreateWarningDialogForDCM()
    {
        Locale locale = Locale.getDefault();

        AssetManager am = mContext.getAssets();
        String path = TETHER_PATH.replace("%y", locale.getLanguage().toLowerCase());
        if (locale.getCountry().equals("JP")) {
            path = TETHER_PATH.replace("%y", locale.getLanguage().toLowerCase());
            path = path.replace("%z", "_" + locale.getCountry().toLowerCase());
            path = path.replace("%x", locale.getLanguage().toLowerCase());
        // Log.i("TetherSettings","path = "+ path);
        }
        else {
            path = TETHER_PATH.replace("%y", "en");
            path = path.replace("%z", "_" + "us"); 
            path = path.replace("%x", "en");
        }
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
                }
            }
        }

        String url = TETHER_URL.replace("%y", locale.getLanguage().toLowerCase());
        if (locale.getCountry().equals("JP")) {
            url = TETHER_URL.replace("%y", locale.getLanguage().toLowerCase());
            url = url.replace("%z", (useCountry ? "_" + locale.getCountry().toLowerCase() : ""));      
            url = url.replace("%x", locale.getLanguage().toLowerCase());
        // Log.e(TAG, "url = "+ url);
        }
        else {
            url = TETHER_URL.replace("%y", "en");
            url = url.replace("%z", (useCountry ? "_" + "us" : ""));      
            url = url.replace("%x", "en");
        }

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        alertDialogView = inflater.inflate(R.layout.tether_notice_dialog, null);
        mAttentionView = (WebView)alertDialogView.findViewById(R.id.DialogWebView);
        if (mAttentionView != null) { // sunghee78.lee@lge.com 2012.06.27 WBT
                                      // #376361, #376362 Null Pointer
                                      // Dereference fixed
            mAttentionView.getSettings().setBuiltInZoomControls(false);
            mAttentionView.getSettings().setSupportZoom(false);
            mAttentionView.loadUrl(url);
        }

        AlertDialog.Builder altDialog = new AlertDialog.Builder(mContext);
        altDialog.setCancelable(false/*true*);
        //altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        altDialog.setTitle(R.string.sp_wifi_tether_notice_title_attention_SHORT);
        altDialog.setView(alertDialogView);
        altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(true);
                    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                    if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                        MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                    }
                    // LGMDM_END
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        altDialog.setPositiveButton(R.string.sp_wifi_tether_notice_button_OK_SHORT,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            if (!mWifiManager.isWifiApEnabled()) {
                                setSoftapEnabled(true);                                
                            }
                    }
                });
        altDialog.setNegativeButton(R.string.sp_wifi_tether_notice_button_cancel_SHORT,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setSwitchChecked(false);
                        mSwitch.setEnabled(true);
                        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                        }
                        // LGMDM_END
                    }
                });
        AlertDialog altAttention = altDialog.create();
        altAttention.show();

        return false;
    }
    */
    // LGE_UPDATE_E, moon-wifi@lge.com by wo0ngs 20121023, for Docomo Wi-Fi
    // Hotspot Notice Dialog
    //LGE_CHANGE_S [WiFi][hyunseong.lee@lge.com] 2013.07.10, Add the Wi-Fi hotspot attention popup for KDDI.
    protected void CreateWarningDialogForKDDI()
    {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        alertDialogView = inflater.inflate(R.layout.tether_popup_kddi, null);
        final CheckBox donotshow = (CheckBox)alertDialogView.findViewById(R.id.tether_popup_kddi_check);
        //[jaewoong87.lee@lge.com] 2013.09.02 Make attention popup donotshow switch sound.
        donotshow.setOnClickListener(this);
        final TextView link = (TextView)alertDialogView.findViewById(R.id.intro_link);
        AlertDialog.Builder altDialog = new AlertDialog.Builder(mContext);
        altDialog.setCancelable(false);
        //altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        altDialog.setTitle(R.string.tethering_popup_kddi_title);
        altDialog.setView(alertDialogView);
        String urlText = " <a href='" + KDDI_TETHER_URL + "'>"
                + mContext.getResources().getString(R.string.tethering_popup_kddi_web) + "</a>";
        CharSequence textlink = Html.fromHtml(mContext.getResources().getString(R.string.tethering_popup_kddi_link, urlText));
        link.setText(textlink);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(true);
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        altDialog.setPositiveButton(R.string.tethering_popup_kddi_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (donotshow.isChecked()) {
                            Settings.System.putInt(mContext.getContentResolver(), "TETHER_POPUP_KDDI", 1);
                        }
                        if (!mWifiManager.isWifiApEnabled()) {
                            setSoftapEnabled(true);
                        }
                    }
                });
        altDialog.setNegativeButton(R.string.tethering_popup_kddi_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setSwitchChecked(false);
                        mSwitch.setEnabled(true);
                    }
                });
        //AlertDialog altAttention = altDialog.create();
        //altAttention.show();
        mAlertDialog = altDialog.create();
        mAlertDialog.show();

    }
    //LGE_CHANGE_E [WiFi][hyunseong.lee@lge.com] 2013.07.10, Add the Wi-Fi hotspot attention popup for KDDI.

    public void CreateWarningDialogForUSC() {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        alertDialogView = inflater.inflate(R.layout.wifi_tether_warning_usc, null);
        final TextView link = (TextView)alertDialogView.findViewById(R.id.intro_link);
        AlertDialog.Builder altDialog = new AlertDialog.Builder(mContext);
        altDialog.setCancelable(false);
        //altDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        altDialog.setTitle(R.string.sp_wifi_tether_introduction_usc_NORMAL);
        altDialog.setView(alertDialogView);
        String urlText = " <a href='" + USC_TETHER_URL + "'>"
                + mContext.getResources().getString(R.string.sp_wifi_tether_intro_usc_link_NORMAL) + "</a>";
        CharSequence textlink = Html.fromHtml(mContext.getResources().getString(R.string.sp_wifi_tether_intro_usc_text3_NORMAL, urlText));
        link.setText(textlink);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        altDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(true);
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        altDialog.setPositiveButton(R.string.sp_accept_NORMAL,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!mWifiManager.isWifiApEnabled()) {
                            setSoftapEnabled(true);
                        }
                    }
                });
        altDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setSwitchChecked(false);
                        mSwitch.setEnabled(true);
                    }
                });
        //AlertDialog altAttention = altDialog.create();
        //altAttention.show();
        mAlertDialog = altDialog.create();
        mAlertDialog.show();
    }

    // V5_TLS_Wifi_SSID runqin.luo@lge.com 2012-12-13 Portable wifi hotspot for
    // telus[START]
    // LGE_UPDATE_S [kuhyun.kwon][2012/02/25] Status flag of shotspot ssid
    // modification for Telus
    public void ChangeDefaultHotspotSSID()
    {
        String imsi = SystemProperties.get("gsm.sim.operator.numeric");
        //String gid = SystemProperties.get("gsm.sim.operator.gid");  // change the method.
        String gid = new com.lge.uicc.LGUiccCard().getGid1();

        Log.e(TAG, "ChangeDefaultHotspotSSID() is called");
        Log.e(TAG, "imsi = " + imsi);
        Log.e(TAG, "gid = " + gid);

        mWifiConfig.SSID = "Android AP"; // For telus carrier

        if ((imsi != null) && (gid != null))
        {
            if ((!TextUtils.isEmpty(imsi)) && (!TextUtils.isEmpty(gid)))
            {
                Log.e(TAG, "ChangeDefaultHotspotSSID()::IMSI = " + imsi);
                Log.e(TAG, "ChangeDefaultHotspotSSID()::GID = " + gid);
                Log.e(TAG, "imsi is not null,gid is not null~~~~~~");
                if (gid.equals("4B4F"))
                {
                    mWifiConfig.SSID = "Koodo 3G+"; // For koodo
                    Log.e(TAG, "ChangeDefaultHotspotSSID():Setup SSID =" + mWifiConfig.SSID);
                    Settings.Secure.putInt(mContext.getContentResolver(),
                            SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS, 1);
                    Log.e(TAG, " SET SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS = 1");
                } else if (gid.equals("5455"))
                {
                    mWifiConfig.SSID = "Telus 3G+"; // For telus
                    Log.e(TAG, "ChangeDefaultHotspotSSID():Setup SSID =" + mWifiConfig.SSID);
                    Settings.Secure.putInt(mContext.getContentResolver(),
                            SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS, 1);
                    Log.e(TAG, " SET SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS = 1");
                } else if (gid.equals("5043"))
                {
                    mWifiConfig.SSID = "Mobile Hotspot"; // For rockhopper
                    Log.e(TAG, "ChangeDefaultHotspotSSID():Setup SSID =" + mWifiConfig.SSID);
                    Settings.Secure.putInt(mContext.getContentResolver(),
                            SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS, 1);
                    Log.e(TAG, " SET SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS = 1");
                }
            }
            else
            {
                Log.e(TAG, "imsi or gid is empty , Set default SSID with Android AP");
            }
        }
        else
        {
            Log.e(TAG, "SIM Info is not available, Set default SSID with Android AP");
        }

        try {
            // Settings.Secure.putInt(mContext.getContentResolver(),
            // SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS, 1);
            // Log.e(TAG," SET SettingsConstants.Secure.WIFI_AP_DEFAULT_STATUS = 1");
        } catch (NumberFormatException e) {
            Log.e(TAG, "ChangeDefaultHotspotSSID()::Failed on writing setting DB");
        }
    }
    // LGE_UPDATE_E [kuhyun.kwon][2012/02/25] Status flag of shotspot ssid
    // modification for Telus
    // V5_TLS_Wifi_SSID runqin.luo@lge.com 2012-12-13 Portable wifi hotspot for
    // telus[END]

    //[S] TMUS requirement
//    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
//                updateTetherEnable();
//            }
//        }
//    };

    private void updateTetherEnable() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch)) {
            Log.i(TAG, "LGMDM Block Hotspot : updateTetherEnable()");
            return;
        }
        // LGMDM_END
        int currentSimStatus = TelephonyManager.getDefault().getSimState();
        if (currentSimStatus != TelephonyManager.SIM_STATE_READY) {
//            mSimEnabled = false;
            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
            if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                mIsNoSimAlert = false;
            }
            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]
            if (!mIsNoSimAlert) {
                //showDialog(DIALOG_NO_SIM_ALERT);
                
                mIsNoSimAlert = true;
                if ((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US")) {
                    AlertDialog noSimAlertDialog = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.no_sim_card)
                    .setMessage(R.string.no_sim_card_message)
                    .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
                    noSimAlertDialog.show();
                    //return noSimAlertDialog;
                }
            }

            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[S]
            if (!((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US"))) {
                if (mSwitch != null) {
                    mSwitch.setEnabled(false);
                }
            }
            //[jaewoong87.lee@lge.com] 2013.04.24 Switch off if there is no Sim for TMUS requirement[E]
            Log.i(TAG, "currentSimStatus : " + currentSimStatus);
            Log.i(TAG, "TelephonyManager.SIM_STATE_READY : " + TelephonyManager.SIM_STATE_READY);
            Log.i(TAG, "Tether : false");
        }
        else {
//            mSimEnabled = true;
    //LGE_UPDATE_S  [hoon2.lim@lge.com][2013/02/07] modify for AirPlaneMode Check #302698
            boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            if (mSwitch != null && !isAirplaneMode) {
    //LGE_UPDATE_E  [hoon2.lim@lge.com][2013/02/07] modify for AirPlaneMode Check #302698  
                mSwitch.setEnabled(true);
            }
            
            //updateState();
            Log.d(TAG, "Tether : true");
        }
    }

//    private void setUpsellTimer() {
//        Log.d(TAG, "setUpsellTimer( )");
//        loopCounting = 0;
//        mTimerSoftApOn = true;
//        //mTimer = new Timer(true);
//        mHandler = new Handler();
//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
//        mTimer = new Timer(true);
//        mTimer.schedule(
//            new TimerTask() {
//                @Override
//                public void run() {
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            ConnectivityManager mConnMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//                            NetworkInfo info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//                            if (info.isConnected()) {
//                                loopCounting = 0;
//                                mTimerSoftApOn = false;
//                                mTimer.cancel(); //Timer End.
//                                startUpsell();
//                                mSwitch.setEnabled(false);
//                            }
//                            if (loopCounting > 50) {
//                                Log.w(TAG, this.getClass().getName() + " : " + "T_COUNT Over : loopCounting=" + loopCounting);
//                                // [START][sunghee78.lee@lge.com] 2012.06.07 - first password set warn popup
////                                mInit = false;
////                                setFirstFlagPreference(false);
//                                // [END][sunghee78.lee@lge.com] 2012.06.07 - first password set warn popup
//                                mTimer.cancel(); //Timer End.
//                                mSwitch.setEnabled(true);
//                                setSoftapEnabled(true);
//                                if ( !getbFirstTimeUse()) {
//                                    Intent intent = new Intent();
//                                    intent.putExtra("select", 1);
//                                    intent.setClassName("com.lge.upsell", "com.lge.upsell.FirstTimeUse");
//                                    ((Activity)mContext).startActivityForResult(intent, REQUEST_FIRST_TIME_USE);
//                                }
//                                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
//                                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
//                                    MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
//                                }
//                                // LGMDM_END
//                                loopCounting = 0;
//                                mTimerSoftApOn = false;
//                                return;
//                            } else {
//                                loopCounting++;
//                                Log.w(TAG, "T_COUNT : loopCounting=" + loopCounting);
//                                Log.e(TAG, "Mobile network isn't connected. and 50 time loop.");
//                            }
//                        }
//                    });
//                }
//            }, 500, 500);
//        }

// LGE_CHANGE_S [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable
    public void checkProvWithUpsell() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch)) {
            Log.i(TAG, "LGMDM Block Hotspot : checkProvWithUpsell()");
            return;
        }
        // LGMDM_END
        final int wifiState = mWifiManager.getWifiState();
        //final int tetherType = type;

        //Hoon Test_S 0906
//        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
        mSwitch.setEnabled(false);
//            mWifiManager.setWifiEnabled(false);
        ConnectivityManager mConnMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mConnMgr.getMobileDataEnabled()) {
            if (((Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && Config.getCountry().equals("US"))) {
                showNotavailableDataNetworkPopupTMUS();
            } else {
                showNotavailableDataNetworkPopup();
            }
            mSwitch.setEnabled(true);
            // [START][sunghee78.lee@lge.com] 2012.06.07 - first password set warn popup
            mInit = false;
            setFirstFlagPreference(false);
            // [END][sunghee78.lee@lge.com] 2012.06.07 - first password set warn popup
            return;
        }
//        }
//        else
//        {*/
//            mSwitch.setEnabled(false);
        //}

        int wifiApState = mWifiManager.getWifiApState();
        String operator_numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

        //seodongjo@lge.com #30287 Add Check condition AP enable.
        if (isAirplaneMode) {
            Log.e(TAG, " Start AP Check : already AP Airplain Mode ");
            setSwitchChecked(false);
            mSwitch.setEnabled(false);
            return;
        }

        // seodongjo@lge.com #20885 Add Check condition AP enable.
        if ( wifiApState == WifiManager.WIFI_AP_STATE_ENABLED ) {
            Log.e(TAG, " Start AP Check : alrey AP enabled ");
            setSwitchChecked(true);
            mSwitch.setEnabled(true);
        } else {
            Log.e(TAG, " Start AP Check : now start AP enable ");
            if (isTmusSimCard(operator_numeric)) {
        //WFC Registered. Show Dialog for turn off the Wifi
//        if (mbIsWFCRegistered == true) {
//            if (mbShowingDialogWFC == false) {
//                Log.d(TAG, "WFC is registered. Show Dialog");
//                mbShowingDialogWFC = true;
//                showWFCRegisteredPopupTMUS();
//            }
//            Log.d(TAG, "mbTurnoffWifiForWFC : " + mbTurnoffWifiForWFC);
//            if (mbTurnoffWifiForWFC == true) {
//                mbShowingDialogWFC = false;
//                startUpsell();
//            }
//        } else {
                Log.d(TAG, "WFC is not registered");
                startUpsell();
//        }
            } else {

                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.tmo_wifi_off_toast, Toast.LENGTH_LONG).show();
                }
                setSoftapEnabled(true);
                if ( !getbFirstTimeUse()) {
                    Intent intent = new Intent();
                    intent.putExtra("select", 1);
                    intent.setClassName("com.lge.upsell", "com.lge.upsell.FirstTimeUse");
                    ((Activity)mContext).startActivityForResult(intent, REQUEST_FIRST_TIME_USE);
                }
            }
        }
    }

    public void setFirstFlagPreference(boolean value) {
        SharedPreferences pref = mContext.getSharedPreferences("FIRST_FLAG", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("f_flag", value);
        editor.commit();
    }       

    public void showNotavailableDataNetworkPopup() {
        Log.w(TAG, this.getClass().getName() + " : " + "!mConnMgr.getMobileDataEnabled()");
        AlertDialog.Builder wifiErrorDialog = new AlertDialog.Builder(mContext);
        wifiErrorDialog.setTitle(R.string.wifi_error);
        //wifiErrorDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        wifiErrorDialog.setCancelable(false);
        wifiErrorDialog.setPositiveButton(R.string.sp_wifi_close_NORMAL, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    //setResult(Activity.RESULT_CANCELED, getIntent());
                    //sendResult(Activity.RESULT_CANCELED);
                    //finish();
                    ;
                }
            });
        wifiErrorDialog.setMessage(R.string.upsell_data_enable_message);
        AlertDialog wifiErrorDlog = wifiErrorDialog.create();
        wifiErrorDlog.show();
    }
    
    public void showNotavailableDataNetworkPopupTMUS() {
        AlertDialog.Builder wifiErrorDialog = new AlertDialog.Builder(mContext);
        wifiErrorDialog.setTitle(R.string.wifi_error);
        //wifiErrorDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        wifiErrorDialog.setCancelable(false);
        wifiErrorDialog.setNegativeButton(android.R.string.cancel,
                                             new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
            }
        });
        wifiErrorDialog.setPositiveButton(android.R.string.ok,
                                             new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mIntent = new Intent();
                mIntent.setAction("android.intent.action.MAIN");
                mIntent.setClassName("com.android.phone",
                                     "com.android.phone.MobileNetworkSettings");
                mContext.startActivity(mIntent);
            }
        });
        wifiErrorDialog.setMessage(R.string.upsell_data_enable_message);
        AlertDialog wifiErrorDlog = wifiErrorDialog.create();
        wifiErrorDlog.show();
    }

    public void showWFCRegisteredPopupTMUS() {
        AlertDialog.Builder wifiErrorDialog = new AlertDialog.Builder(mContext);
        wifiErrorDialog.setTitle(R.string.wfc_registered_title);
        //wifiErrorDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        wifiErrorDialog.setCancelable(false);
        wifiErrorDialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Remain Current State
                mbTurnoffWifiForWFC = false;
                mbShowingDialogWFC = false;
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                Log.d(TAG, "Don't turn off Wi-Fi");
            }
        });
        wifiErrorDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Turn off Wi-Fi and Save the state.
                mbTurnoffWifiForWFC = true;
                mWifiManager.setWifiEnabled(false);
                Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.WIFI_SAVED_STATE, 1);
                checkProvWithUpsell();
                Log.d(TAG, "turn off Wi-Fi and save the state");
            }
        });
        wifiErrorDialog.setMessage(R.string.wfc_registered_message);
        AlertDialog wifiErrorDlog = wifiErrorDialog.create();
        wifiErrorDlog.show();
    }

//    private void KillUpsellBeforeStartUpsell() {
//        Log.e(TAG, "######## KillUpsellBeforeStartUpsell########");
//
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.DESTROY_UPSELL");
//        mContext.sendBroadcast(intent);
//     }

    private void startUpsell() {
        final ContentResolver cr = mContext.getContentResolver();

        if (Settings.Secure.getInt(cr, "upsell_svc_started", 0) == 0) {  // for exeption routine
            Settings.Secure.putInt(cr, "upsell_check_ongoing", 0);
        } 
        
        if (Settings.Secure.getInt(cr, "upsell_check_ongoing", 0) == 1) {
            Log.e(TAG, "WifiApEnabler::Skip launch Upsell check");
            return ;
        } else {
//            KillUpsellBeforeStartUpsell();
            Intent intent = new Intent();
            intent.putExtra("Tethering_Type", "Wifi");
            //intent.setClassName("com.lge.upsell", "com.lge.upsell.DataService");
            //mContext.startService(intent);
            intent.setClassName("com.lge.upsell", "com.lge.upsell.UpsellDialogActivity");
            mContext.startActivity(intent);
            Settings.Secure.putInt(cr, "upsell_check_ongoing", 1);
            Log.e(TAG, "WifiApEnabler::Start Upsell check");
        }
    }
// LGE_CHANGE_E [neo_wifi@lge.com] 2013.03.13 Add TMO upsell check before AP enable

    //[S]joon7.lim@lge.com add first time use dialog for NewCo
    private boolean getbFirstTimeUse() {
        boolean bfirstcheck;

        if (getSettingInt(SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK, 0) == 0)
        {
            bfirstcheck = false;
        }
        else
        {
            bfirstcheck = true;
        }
        Log.d(TAG, "getbFirstTimeUse() return = " + bfirstcheck);
        return bfirstcheck;
    }
    
    private void setbFirstTimeUse(int ask) {
        setSettingInt(SHARED_PREFERENCES_KEY_FIRST_WIFI_HELP_ASK, ask);
    }

    private int getSettingInt(String key, int defaultValue) {
        Log.d(TAG, "CSC_A getSettingInt");
        SharedPreferences sharedPref = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,  Activity.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }

    private void setSettingInt(String key, int value) {
        Log.d(TAG, "CSC_A setSettingInt");
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }
    //[E]joon7.lim@lge.com add first time use dialog for NewCo

// [[ 3.03.19 jonghyun.seon add battery popup
//    private void setBatteryPopup(int wifiState)
//    {
//        Log.d(TAG, "setBatteryPopup( ), mDoNotShowAgain = " + mDoNotShowAgain);
//        String operator_numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
//        if (mDoNotShowAgain == false && "DCM".equals(Config.getOperator()) == false) { //moon-wifi@lge.com by wo0ngs 20121023, for Except Docomo
//            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
//            mAlertDialogView = inflater.inflate(R.layout.wifi_alert_dialog, null);
//
//            mAlertDialogBuilder = new AlertDialog.Builder(mContext)
//                .setCancelable(true)
//                .setTitle(R.string.sp_wifi_hotspot_is_on_NORMAL)
//                //.setIconAttribute(android.R.attr.alertDialogIcon) // Alert icon add
//                .setView(mAlertDialogView)
//                .setPositiveButton(R.string.yes, alertDialogHandler)
//                .setNegativeButton(R.string.no, alertDialogHandler);
//
//            ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again)).setOnClickListener(this);
//
//            mAlertDialog = mAlertDialogBuilder.create();
//            mAlertDialog.show();
//        } else if (isTmusSimCard(operator_numeric)) {
//            startUpsell();
//         } else {
//            setSoftapEnabled(true);
///*          if (!getbFirstTimeUse()) {
//                Intent intent = new Intent();
//                intent.setClassName("com.lge.upsell", "com.lge.upsell.FirstTimeUse");
//                ((Activity)mContext).startActivityForResult(intent, REQUEST_FIRST_TIME_USE);
//               } */
//        }
//    }
// ]] 2013.03.19 jonghyun.seon add battery popup

    private boolean isTmusSimCard(String numeric)
    { 
        if ( numeric == null ) {
//            Log.d(TAG, "MCC + MNC : null!!!" );
            return false;
        }
//        Log.d(TAG, "MCC + MNC : " + numeric);
        if ( numeric.equals("00101") || numeric.equals("310160") || numeric.equals("310200") || numeric.equals("310210") || 
            numeric.equals("310220") || numeric.equals("310230") || numeric.equals("310240") || numeric.equals("310250") || 
            numeric.equals("310260") || numeric.equals("310270") || numeric.equals("310300") || numeric.equals("310310") || 
            numeric.equals("310490") || numeric.equals("310530") || numeric.equals("310580") || numeric.equals("310590") || 
            numeric.equals("310640") || numeric.equals("310660") || numeric.equals("310800") ) {
            Log.d(TAG, "TMUS SIM CARD inserted");            
            return true;
        }
        Log.d(TAG, "NON TMUS SIM CARD inserted");
        return false;
    }

    // [START][TD#245041] Applied Toast When input MaxLength, 2012-12-21,
    // ilyong.oh@lge.com
    // Password EditText length Fix(63)
    class Utf8ByteLengthFilter implements InputFilter {
        private final int mMaxBytes;
        private Toast mToast = null;
        //private Context mContext = getContext();
        private InputMethodManager mImm = (InputMethodManager)mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        Utf8ByteLengthFilter(int maxBytes) {
            mMaxBytes = maxBytes;
        }

        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            int srcByteCount = 0;
            // count UTF-8 bytes in source substring
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                srcByteCount += (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
            }
            int destLen = dest.length();
            int destByteCount = 0;
            // count UTF-8 bytes in destination excluding replaced section
            for (int i = 0; i < destLen; i++) {
                if (i < dstart || i >= dend) {
                    char c = dest.charAt(i);
                    destByteCount += (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
                }
            }
            int keepBytes = mMaxBytes - destByteCount;
            if (keepBytes <= 0) {
                if (mImm != null && mPassword != null) {
                    mImm.restartInput(mPassword);
                }

                if (mToast == null) {
                    mToast = Toast.makeText(mContext, R.string.sp_auto_reply_maxlength_NORMAL,
                            Toast.LENGTH_SHORT);
                }
                mToast.show();
                return "";
            } else if (keepBytes >= srcByteCount) {
                return null; // use original dest string
            } else {
                // find end position of largest sequence that fits in keepBytes
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    keepBytes -= (c < (char)0x0080) ? 1 : (c < (char)0x0800 ? 2 : 3);
                    if (keepBytes < 0) {
                        if (mImm != null && mPassword != null) {
                            mImm.restartInput(mPassword);
                        }
                        if (mToast == null) {
                            mToast = Toast.makeText(mContext,
                                    R.string.sp_auto_reply_maxlength_NORMAL,
                                    Toast.LENGTH_SHORT);
                        }
                        mToast.show();
                        return source.subSequence(start, i);
                    }
                }
                // If the entire substring fits, we should have returned null
                // above, so this line should not be reached. If for some
                // reason it is, return null to use the original dest string.
                return null;
            }
        }
    }
    // [E N D][TD#245041] Applied Toast When input MaxLength, 2012-12-21,
    // ilyong.oh@lge.com
    // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [E]

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][D-MDM-243]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMWifiSettingsAdapter.getInstance().receiveTetherPolicyChangeIntent(intent)) {
                    int wifiApState = WifiManager.WIFI_AP_STATE_FAILED;
                    if (mWifiManager != null) {
                        wifiApState = mWifiManager.getWifiApState();
                    }
                    handleWifiApStateChanged(wifiApState);
                }
            }
        }
    };
    // LGMDM_END




    /* LGE_CHANGE_S, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 
    public static boolean checkLGU() {
        if (!(Config.LGU).equals(Config.getOperator())) {
            return false;
        }

        return true;
        /* Not Check Model below not use
        //To Do: Check each model Ex.F240, F260
        String targetProductName = SystemProperties.get("ro.product.name");
        String[] productNames = {"geefhd4g_lgu_kr"};

        Log.d(TAG, "checkLGU() : " + targetProductName);

        for (int i=0; i<productNames.length; i++) {
            if (productNames[i].equals(targetProductName)) {
                return true;
            }
        }
        return false;
        */
    }
    /* LGE_CHANGE_E, [seodongjo@lge.com] Modify toast when off Data for LGU+ */ 

    // from wifi_get_default_country_code() in the wifi.c
/*
    public boolean getDefaultCountryCode() {
        int loopcnt = 0;

        for (loopcnt = 0; loopcnt < mCheckBlock5gbyCC.length; loopcnt++) {
            if (Config.getCountry().equalsIgnoreCase(mCheckBlock5gbyCC[loopcnt])) {
                Log.e(TAG, "Not supported 5G softap :  CheckBlock5gbyCC = "
                  + mCheckBlock5gbyCC[loopcnt] + "Config.getCountry() = " +  Config.getCountry());
                return false;
            }
        }
        Log.e(TAG, "supported 5G softap :   Config.getCountry " + Config.getCountry() );
        return true;
    }
*/
    // To check the 5G ap feature enabled with countrycode.
    // http://www.iso.org/iso/home/standards/country_codes/country_names_and_code_elements.htm
    public boolean check5gApSupportedbyCC() {
        int loopcnt = 0;
        String mCountry = Settings.Global.getString(mContext.getContentResolver(),
            Settings.Global.WIFI_COUNTRY_CODE);

        if (SystemProperties.getBoolean("wlan.lge.softap5g", false) == false) {
            Log.d(TAG, "Tethering 5G property is false");
            return false;
        }

        if (mCountry == null) {
            Log.d(TAG, "check5gApSupportedbyCC() : mCountry is null ");
            mCountry =  Config.getCountry();
        }

        Log.d(TAG, "check5gApSupportedbyCC() : mCountry " + mCountry
            + ", Length = " + mCheckBlock5gbyCC.length);
        for (loopcnt = 0; loopcnt < mCheckBlock5gbyCC.length; loopcnt++) {
            if (mCountry.equalsIgnoreCase(mCheckBlock5gbyCC[loopcnt])) {
                Log.e(TAG, "Not supported 5G softap : mCountry " + mCountry
                    + " , CheckBlock5gbyCC = " + mCheckBlock5gbyCC[loopcnt] );
                return false;
            }
        }
        Log.d(TAG, "Supported 5G softap : mCountry " + mCountry);
        return true;
    }

    //[END] soonhyuk.choi@lge.com for 5G ap mode

    // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
    private void createHotspotWarningPopupForKT() {

        if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            startProvisioningIfNecessary();
            return;
        }

        AlertDialog mKT_Hotspot_warning_dialog = new AlertDialog.Builder(mContext)
            .setTitle(R.string.sp_note_normal)
            //.setIconAttribute(android.R.attr.alertDialogIcon) //Not Used
            .setMessage(R.string.sp_wifi_incompatible_hotspot_wanning_NORMAL)
            .setCancelable(false)
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        setSwitchChecked(false);
                        setSoftapEnabled(false);
                        mSwitch.setEnabled(true);
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                                             Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

                    setSwitchChecked(false);
                    setSoftapEnabled(false);

                    if (isAirplaneMode) {
                        mSwitch.setEnabled(false);
                    } else {
                        mSwitch.setEnabled(true);
                    }
                    dialog.dismiss();
                    return;
                }
            })
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                                             Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

                    if (isAirplaneMode) {
                        setSwitchChecked(false);
                        setSoftapEnabled(false);
                        mSwitch.setEnabled(false);
                    } else if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                        setSwitchChecked(true);
                        mSwitch.setEnabled(true);
                    } else {
                        startProvisioningIfNecessary();
                    }
                    dialog.dismiss();
                    return;
                }
            }).create();
        mKT_Hotspot_warning_dialog.show();
    }

    private int getToastMargin() {

        double margin = 0;
        double sizeHight = ((Activity)mContext).getResources().getDisplayMetrics().heightPixels;
        
        Log.d(TAG, "getToastMargin sizeHight: " + sizeHight);

        margin = (sizeHight / 2) * 0.34;

        // check landscape.
        if (mContext.getResources().getConfiguration().orientation
           == Configuration.ORIENTATION_LANDSCAPE) {
           margin = 0;
        }

        Log.d(TAG, "getToastMargin return margin : " + (int)margin);
     
        return (int)margin;
    }

    public Switch getmSwitch() {
            return mSwitch;
    }

}
