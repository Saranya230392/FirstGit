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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
//import android.text.InputFilter;
//20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [start]
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
//20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [end]
/*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
import android.os.SystemProperties;
/*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;
import android.widget.Toast; // sunghee78.lee@lge.com 2012.07.14 OFFICIAL_EVENT_Mobile_12 TD #119741
import java.util.regex.Pattern; // seodongjo@lge.com #140563 2012.08.09

//[Wi-Fi Settings]_START, [For USC Feature],sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
import android.app.Activity;
import android.content.SharedPreferences;
//[Wi-Fi Settings]_END, [For USC Feature],sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
// LGE_UPDATE_S bluetooth.kang@lge.com   [SSID HIDDEN] 2010.5.10
import android.widget.AdapterView.OnItemSelectedListener;
//import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import android.net.wifi.WifiManager;
// import com.lge.provider.SettingsEx; KLP
//import android.provider.Settings;
import com.lge.constants.SettingsConstants;
import android.view.LayoutInflater;
// LGE_UPDATE_E
/* LGE_CHANGE_S, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
import android.net.NetworkUtils;
import java.net.InetAddress;
/* LGE_CHANGE_E, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
import com.android.settings.Utils;
import android.provider.Settings.SettingNotFoundException;
//import android.widget.Toast;
//import android.net.wifi.WifiConfiguration.KeyMgmt;
//import android.net.wifi.WifiConfiguration.AuthAlgorithm;
//import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;

// SPR
import android.widget.ArrayAdapter;
import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import com.lge.telephony.LGServiceState;
import android.telephony.TelephonyManager;
//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneFactory;

/**
 * Dialog to configure the SSID and security settings
 * for Access Point operation
 */
public class WifiApDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, OnCheckedChangeListener, AdapterView.OnItemSelectedListener {  //TD:241288 Check Showpassword -> input password -> language change -> password show "***" 

    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    private static final String TAG = "WifiApDialog";

    private final DialogInterface.OnClickListener mListener;

    public static final int OPEN_INDEX = 0;
    public static final int WPA_INDEX = 1;
    public static final int WPA2_INDEX = 2;

    private View mView;
    private TextView mSsid;
    private CheckBox mVisibility;
    /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
    //private String mCurrentSsid;
    /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
    private int mSecurityTypeIndex = OPEN_INDEX;
    // private int mOldSecurityTypeIndex = OPEN_INDEX;// [Wi-Fi Settings]
    private EditText mPassword;
    //[Wi-Fi Settings]_START, [For USC Feature],sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
    private CheckBox mShowPassword;
    private Spinner mSecurity;
    private Spinner mMaxClient;
    private Spinner mBroadcastChannel;
    private Spinner mBroadcastChannel_5ghz;
    private Spinner m802_11;
    private Spinner m802_11_5ghz;
    private CheckBox mBC_2_4ghz;
    private CheckBox mBC_5ghz;    
    // private CheckBox mAdvancedSetting;
    private EditText mIpAddress;
    private EditText mSubnetMask;
    private EditText mStartIp;
    private EditText mEndIp;    
    private TextView mBroadcastChannelText;
    private final int mBCH_2_4GHZ = 0;
    private final int mBCH_5GHZ = 1;
    
    private SharedPreferences showPasswordCheckboxPref;
    private SharedPreferences.Editor showPasswordCheckboxPrefEditor;
    //[Wi-Fi Settings]_END, [For USC Feature], sangheon.shim/hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
    
    private Context callerContext = null;

    //[START][TD#204043] resartInput when over max vlaue on SSID and Password, 2012-10-30, ilyong.oh@lge.com
    private final int MAX_SSID_COUNTER = 32;
    private final int MAX_PASS_COUNTER = 63;
    //[E N D][TD#204043] resartInput when over max vlaue on SSID and Password, 2012-10-30, ilyong.oh@lge.com

    /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
    //private static final String TELSTRA = "Telstra";
    /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/

    private boolean warningDisplayed_ssid = false;
    private boolean warningDisplayed_pass = false;

    private boolean allowSsidChanging = false;

    // LGE_UPDATE_S bluetooth.kang@lge.com   [SSID HIDDEN] 2010.5.10
    // private static final int SHOW_INDEX = 0;
    // private static final int HIDE_INDEX = 1;
    private WifiManager mWifiManager;
    // LGE_UPDATE_E

    // [[ 2013.03.20 jonghyun.seon add ssid hide alert popup
    private AlertDialog mDialog;
    private AlertDialog.Builder mDialogBuilder;
    private boolean mDoNotShowAgain = false;
    private View mAlertDialogView;
    // private static final int DIALOG_HIDE_ALERT = 1;
    // ]] 2013.03.20 jonghyun.seon add ssid hide alert popup

    WifiConfiguration mWifiConfig;
    private boolean mIsCreteMode = true;

    
    //[jaewoong87.lee@lge.com] Fixed default IME in English.
    private EditText mSsidView;

    /*Netcfg Hotspot*/
    // private Spinner mHotspotMaxUser = null;
    private static final int HOTSPOT_DEF_TOT_MAXUSER = 8;/*default total max user num*/
    private static final int HOTSPOT_DEF_MAXUSER = 8; /*default max user num*/

    //Chameleon
    // private Phone phone = null;
    private int roamingState = LGServiceState.HOME;
    //private static final String cmln_wifi_root = new String("/carrier/wifi");
    //private static final String cmln_sap_ssid = new String("/carrier/wifi/si");
    private static final String cmln_sap_h_maxuser = new String("/carrier/wifi/hm");
    private static final String cmln_sap_d_maxuser = new String("/carrier/wifi/dm");
    //private static final String cmln_sap_g_maxuser = new String("/carrier/wifi/gm");
    private static final String cmln_sap_i_maxuser = new String("/carrier/wifi/im");
    // [END][LGE_WIFI][SPR][neo-wifi@lge.com] : [SPEC] Hiddenssid

    // if defined in the array, the 5g softap is blocked by regulation of country.
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

    class ConfigurationInfo {
        private String mSsid;
        private int mHiddenssid;
        private String mSecurity;
        private String mSecuritykey;
        // for WEP private int mSecuritykeyIndex;
        private int mBroadcastChannel;
        private int mBroadcast5gChannel;
        private String mHWmode;
        private int mMaxConnect;
        private int mFrequency;
        private String mGateway;
        private String mMask;
        private String mStartip;
        private String mEndip;

        private void copyValue(ConfigurationInfo src) {
            Log.d(TAG, "ConfigurationInfo.copyValue " + src);
            this.mSsid = src.mSsid;
            this.mHiddenssid = src.mHiddenssid;
            this.mSecurity = src.mSecurity;
            this.mSecuritykey = src.mSecuritykey;            
            // for WEP this.mSecuritykeyIndex = src.mSecuritykeyIndex;
            this.mBroadcastChannel = src.mBroadcastChannel;
            this.mBroadcast5gChannel = src.mBroadcast5gChannel;            
            this.mHWmode = src.mHWmode;
            this.mMaxConnect = src.mMaxConnect;
            this.mFrequency = src.mFrequency;
            this.mGateway = src.mGateway;
            this.mMask = src.mMask;
            this.mStartip = src.mStartip;
            this.mEndip = src.mEndip;

        }
    }
    
    private ConfigurationInfo mConfigInfo;
    private ConfigurationInfo mBackupConfig;

    public WifiApDialog(Context context, DialogInterface.OnClickListener listener,
            WifiConfiguration wifiConfig) {
        super(context);
        mListener = listener;
        mWifiConfig = wifiConfig;
        callerContext = context;
        mWifiManager = (WifiManager)callerContext.getSystemService(Context.WIFI_SERVICE);

            mConfigInfo = new ConfigurationInfo();
            mBackupConfig = new ConfigurationInfo();

        Log.d(TAG, "WifiApDialog mWifiCOnfig.ssid = " + mWifiConfig.SSID);
        Log.d(TAG, "WifiApDialog mWifiCOnfig.security = " + getSecurityTypeIndex(mWifiConfig));
    }

    // [BEGIN][LGE_WIFI][SPR][neo-wifi@lge.com] : [SPEC] Hiddenssid
    private int readChameleonValue(String fn, int ref_value) {

        FileReader    fr = null;
        BufferedReader inFile = null;
        String  value = null;
        int ret_value = ref_value;

        File fh = new File(fn);
        if(!fh.exists())
            return ret_value;

        try {
            fr = new FileReader (fn);
            inFile = new BufferedReader(fr);
            String  line = inFile.readLine();
            if(line != null && line.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(line);
                value = tokenizer.nextToken();
                //Log.d("[node wifi] read "+ fn + " : " + value);
            }
        } catch (IOException e) {
        } finally {
            if (inFile != null) {
                try {
                    inFile.close();
                } catch (IOException e) {
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
            }
        }

        try {

            if(value != null)
                ret_value = Integer.parseInt(value);
            else
                ret_value = ref_value;

        } catch(Exception e) {
            ret_value = ref_value;
        }

        return ret_value;
    }
    // [END][LGE_WIFI][SPR][neo-wifi@lge.com] : [SPEC] Hiddenssid

    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return WPA_INDEX;
        } else if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }

    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */
        config.SSID = mSsid.getText().toString();

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }

    // User wifi hotspot on check source in TeterSettings. 
    public boolean isNewConfig() {
        boolean changeFlag = false;

        try {   //TD2422126278 checking NullPointerException

        //ssid
        if (!mBackupConfig.mSsid.equals(mConfigInfo.mSsid)) {
            Log.d(TAG, "SSID old = " + mBackupConfig.mSsid+ " new = " + mConfigInfo.mSsid);
            changeFlag = true;
        }

        //password
        if (!mBackupConfig.mSecuritykey.equals(mConfigInfo.mSecuritykey)) {
            Log.d(TAG, "password old = " + mBackupConfig.mSecuritykey + " new = " + mConfigInfo.mSecuritykey);
            changeFlag = true;
        }

        //hidden ssid
        if (mBackupConfig.mHiddenssid != mConfigInfo.mHiddenssid) {
            Log.d(TAG, "Hidden old = " + mBackupConfig.mHiddenssid + " new = " + mConfigInfo.mHiddenssid);
            changeFlag = true;
        } 

        //security
        if (!mBackupConfig.mSecurity.equals(mConfigInfo.mSecurity)) {
            Log.d(TAG, "mSecurity old = " + mBackupConfig.mSecurity + " new = " + mConfigInfo.mSecurity);
            changeFlag = true;
        } 

        //2g channel
        if (mBackupConfig.mBroadcastChannel != mConfigInfo.mBroadcastChannel) {
            Log.d(TAG, "mBroadcastChannel old = " + mBackupConfig.mBroadcastChannel + " new = " + mConfigInfo.mBroadcastChannel);
            changeFlag = true;
        } 

        //5g channel
        if (mBackupConfig.mBroadcast5gChannel != mConfigInfo.mBroadcast5gChannel) {
            Log.d(TAG, "mBroadcast5gChannel old = " + mBackupConfig.mBroadcast5gChannel + " new = " + mConfigInfo.mBroadcast5gChannel);
            changeFlag = true;
        } 

        if ("VZW".equals(Config.getOperator())) {
            //hw mode
            if (!mBackupConfig.mHWmode.equals(mConfigInfo.mHWmode)) {
                Log.d(TAG, "mHWmode old = " + mBackupConfig.mHWmode + " new = " + mConfigInfo.mHWmode);
                changeFlag = true;
            }                        
        }

        //max client
        if (mBackupConfig.mMaxConnect!= mConfigInfo.mMaxConnect) {
            Log.d(TAG, "mMaxConnect old = " + mBackupConfig.mMaxConnect + " new = " + mConfigInfo.mMaxConnect);
            changeFlag = true;
        } 

        //frequency
        if (mBackupConfig.mFrequency != mConfigInfo.mFrequency) {
            Log.d(TAG, "mFrequency old = " + mBackupConfig.mFrequency + " new = " + mConfigInfo.mFrequency);
            changeFlag = true;
        } 

        if (Config.getOperator().equals("KDDI")) {
                        
            //security
            if (!mBackupConfig.mGateway.equals(mConfigInfo.mGateway)) {
                Log.d(TAG, "mGateway old = " + mBackupConfig.mGateway + " new = " + mConfigInfo.mGateway);
                changeFlag = true;
            }             
            //security
            if (!mBackupConfig.mMask.equals(mConfigInfo.mMask)) {
                Log.d(TAG, "mMask old = " + mBackupConfig.mMask + " new = " + mConfigInfo.mMask);
                changeFlag = true;
            }                         
            //security
            if (!mBackupConfig.mStartip.equals(mConfigInfo.mStartip)) {
                Log.d(TAG, "mStartip old = " + mBackupConfig.mStartip + " new = " + mConfigInfo.mStartip);
                changeFlag = true;
            }                         
            //security
            if (!mBackupConfig.mEndip.equals(mConfigInfo.mEndip)) {
                Log.d(TAG, "mEndip old = " + mBackupConfig.mEndip + " new = " + mConfigInfo.mEndip);
                changeFlag = true;
            } 
                        
        }

        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException : " + e.getMessage());
            changeFlag = true;
        }

        return changeFlag;        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    
        if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
            Log.w(TAG, this.getClass().getName() + " : " + "onCreate()");
            // [[ 2013.03.20 jonghyun.seon add ssid hide alert popup
            SharedPreferences pref_ns = callerContext.getSharedPreferences("NOT_SHOW", callerContext.MODE_PRIVATE);
            boolean flag_ns = pref_ns.getBoolean("not_show", false);

            if (flag_ns) {
                mDoNotShowAgain = true;  
            }  else {
                mDoNotShowAgain = false;
            }
            Log.d(TAG, "## onCreate(),  mDoNotShowAgain = " + mDoNotShowAgain);
            mIsCreteMode = true;
        }

        //Define View according to the Target carrier
        if ((Config.getCountry().equals("US") && (Config.getOperator().equals("ATT") || Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")))
            || (Config.getCountry().equals("CA") && (Config.getOperator().equals("RGS") || Config.getOperator().equals("TLS") || Config.getOperator().equals("BELL")))) {
            if (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) {
                mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog_tmus, null);
             }  else {
                mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog_att, null);
             }
        } else {
            if (isSupportAdvancedOptions()) {
                mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog_com, null);
                mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                ((CheckBox)mView.findViewById(R.id.wifi_advanced_togglebox))
                            .setOnCheckedChangeListener(this);
                mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
                if (Config.getOperator().equals("KDDI")) {
                    mIpAddress = (EditText)mView.findViewById(R.id.ipaddress);
                    mSubnetMask = (EditText)mView.findViewById(R.id.subnetmask);
                    mStartIp = (EditText)mView.findViewById(R.id.start_ip);
                    mEndIp = (EditText)mView.findViewById(R.id.end_ip);
                    mIpAddress.setText(Settings.System.getString(callerContext.getContentResolver(), "gateway"));
                    mSubnetMask.setText(Settings.System.getString(callerContext.getContentResolver(), "mask"));
                    mStartIp.setText(Settings.System.getString(callerContext.getContentResolver(), "start_ip"));
                    mEndIp.setText(Settings.System.getString(callerContext.getContentResolver(), "end_ip"));
                    mIpAddress.addTextChangedListener(this);
                    mSubnetMask.addTextChangedListener(this);
                    mStartIp.addTextChangedListener(this);
                    mEndIp.addTextChangedListener(this);
                }
                    
            } else {
                mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog, null);
                if (Config.getOperator().equals("KDDI")) {
                    mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
                    mIpAddress = (EditText)mView.findViewById(R.id.ipaddress);
                    mSubnetMask = (EditText)mView.findViewById(R.id.subnetmask);
                    mStartIp = (EditText)mView.findViewById(R.id.start_ip);
                    mEndIp = (EditText)mView.findViewById(R.id.end_ip);
                    mIpAddress.setText(Settings.System.getString(callerContext.getContentResolver(), "gateway"));
                    mSubnetMask.setText(Settings.System.getString(callerContext.getContentResolver(), "mask"));
                    mStartIp.setText(Settings.System.getString(callerContext.getContentResolver(), "start_ip"));
                    mEndIp.setText(Settings.System.getString(callerContext.getContentResolver(), "end_ip"));
                    mIpAddress.addTextChangedListener(this);
                    mSubnetMask.addTextChangedListener(this);
                    mStartIp.addTextChangedListener(this);
                    mEndIp.addTextChangedListener(this);
                }
            }
        }

        if (Config.getOperator().equals("ORG")) {
            // ORG Operator Req. - remove max client setting
            mView.findViewById(R.id.max_users).setVisibility(View.GONE);
        }

        //[jaewoong87.lee@lge.com] Fixed default IME in English.
        if ("KR".equals(Config.getCountry())) {
            mSsidView = (EditText)mView.findViewById(R.id.ssid);
            mSsidView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }

        setView(mView);
        setInverseBackgroundForced(true);

        Context context = getContext();

        //Title change by Target Carrier
        if (Config.getCountry().equals("US") && (Config.getOperator().equals("ATT") || Config.getOperator().equals("VZW"))) {
            setTitle(R.string.sp_wifi_hotspot_set_up_att_title_NORMAL);
        } else if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {		
            setTitle(R.string.wifi_tether_configure_ap_tmus_text); 
        } else {
            setTitle(R.string.sp_wifi_hotspot_set_up_title_NORMAL); // JB : Configure Wi-Fi hotspot ==> Set up Wi-Fi hotspot, 120929, hyeondug.yeo@lge.com 
        }
        

        //find view here in order
        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);

        //ssid
        mSsid = (TextView)mView.findViewById(R.id.ssid);

        //hidden issid
        mVisibility = ((CheckBox)mView.findViewById(R.id.visibility));
        mVisibility.setOnClickListener(this);                    
        mVisibility.setOnCheckedChangeListener(this);

        //security
        mSecurity = ((Spinner)mView.findViewById(R.id.security));
        mSecurity.setOnItemSelectedListener(this);
        
        //password
        mPassword = (EditText)mView.findViewById(R.id.password);

        if ("VZW".equals(Config.getOperator())) {
            mPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus == true) {
                        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(callerContext);
                        alertDlgBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
                        alertDlgBuilder.setTitle(R.string.sp_notification_NORMAL);
                        alertDlgBuilder.setMessage(R.string.sp_check_security_key_combination_NORMAL);
                        alertDlgBuilder.setPositiveButton(R.string.sp_ok_NORMAL, null);
                        alertDlgBuilder.create().show();
                    }
                }
            });
        }
        //show password
        mShowPassword = (CheckBox)mView.findViewById(R.id.show_password);
        mShowPassword.setOnClickListener(this); //Save the status of checkbox(show password) by using shared preferences.
        //[Wi-Fi Settings]_END, [For USC Feature], sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.        
        //20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [start]
        mShowPassword.setOnCheckedChangeListener(this);
        //20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [end]
                

        //max users
        mMaxClient = ((Spinner)mView.findViewById(R.id.max_client_num));
        if ("US".equals(Config.getCountry()) && ("SPR".equals(Config.getOperator()) || "BM".equals(Config.getOperator()))) {
            roamingState = Settings.System.getInt(callerContext.getContentResolver(), SettingsConstants.System.WIFI_CHAMELEON_ROAMING_TYPE, LGServiceState.HOME);
            if (mMaxClient != null) {
                int hotspotMaxuser = Settings.System.getInt(callerContext.getContentResolver(), "mhs_max_client", HOTSPOT_DEF_MAXUSER);

                //Chameleon feature
                int hotspotTotuser = HOTSPOT_DEF_TOT_MAXUSER;

                if (roamingState == LGServiceState.DOMESTIC_ROAMING) {
                    hotspotTotuser = readChameleonValue(cmln_sap_d_maxuser, HOTSPOT_DEF_TOT_MAXUSER);
                } else if (roamingState == LGServiceState.INTERNATIONAL_ROAMING) {
                    hotspotTotuser = readChameleonValue(cmln_sap_i_maxuser, HOTSPOT_DEF_TOT_MAXUSER);
                } else {
                    hotspotTotuser = readChameleonValue(cmln_sap_h_maxuser, HOTSPOT_DEF_TOT_MAXUSER);
                }

                //20130524, yeongsu.wu, hotspot max user settings upon GSM network roaming
                if (isNetworkTypeGSM()) {
                    hotspotTotuser = 1; // readChameleonValue(cmln_sap_g_maxuser, 1);
                }

                Log.w(TAG, "onCreate hotspotMaxuser is " + hotspotMaxuser + " total is " + hotspotTotuser + " roam : " + roamingState);

                if (hotspotMaxuser > hotspotTotuser) {
                    hotspotMaxuser = hotspotTotuser;
                }
                ArrayList<String> maxUserList = new ArrayList<String>();

                for (int i = 0; i < hotspotTotuser; i++) {
                    maxUserList.add("" + (i + 1));
                }

                ArrayAdapter<String> maxUserAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, maxUserList);
                maxUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mMaxClient.setAdapter(maxUserAdapter); // NullPointerException!!
                mMaxClient.setSelection(hotspotMaxuser - 1);
            }
        }
        else {
            mMaxClient.setOnItemSelectedListener(this);
            mMaxClient.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "## mMaxClient   OnItemSeleectedListener() position=" + position);
                        mConfigInfo.mMaxConnect = position + 1;
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        // [andrew75.kim@lge.com] 2.4Ghz / 5Ghz [S]
        mView.findViewById(R.id.channel).setVisibility(View.VISIBLE);

        mBC_2_4ghz = (CheckBox)mView.findViewById(R.id.broadcast_channel);
        mBC_2_4ghz.setOnClickListener(this);

        mBC_5ghz = (CheckBox)mView.findViewById(R.id.broadcast_channel_5ghz);
        mBC_5ghz.setOnClickListener(this);

        mBroadcastChannel = ((Spinner)mView.findViewById(R.id.ap_broadcast_channel));
        mBroadcastChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "## mBroadcatChannel   OnItemSeleectedListener() position=" + position);
                //mBroadcastChannelNum = position;
                mConfigInfo.mBroadcastChannel = position;
                mBroadcastChannelText = (TextView)((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).getSelectedView();
                if (isSupportAdvancedOptions() && check5gApSupportedbyCC()) {
                    if (mBC_2_4ghz.isChecked()) {
                        ((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).setEnabled(true);
                        mBroadcastChannelText.setEnabled(true);
                    } else {
                        ((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).setEnabled(false);
                        mBroadcastChannelText.setEnabled(false);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mBroadcastChannel_5ghz = ((Spinner)mView.findViewById(R.id.ap_broadcast_channel_5ghz));
        mBroadcastChannel_5ghz.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "## mBroadcastChannel_5ghz   OnItemSeleectedListener() position=" + position);
                //mBroadcastChannel5GHzNum = position;
                mConfigInfo.mBroadcast5gChannel = changeIndexToDb(position);
                mBroadcastChannelText = (TextView)((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).getSelectedView();
                if (isSupportAdvancedOptions() && check5gApSupportedbyCC()) {
                    if (mBC_2_4ghz.isChecked()) {
                        ((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).setEnabled(true);
                        mBroadcastChannelText.setEnabled(true);
                    } else {
                        ((Spinner)mView.findViewById(R.id.ap_broadcast_channel)).setEnabled(false);
                        mBroadcastChannelText.setEnabled(false);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //802.11 mode
        if ("VZW".equals(Config.getOperator()) && isSupportAdvancedOptions()) {
            mView.findViewById(R.id.wifi_802_11_mode).setVisibility(View.VISIBLE);
            m802_11 = ((Spinner)mView.findViewById(R.id.ap_802_11_mode));
            m802_11_5ghz = ((Spinner)mView.findViewById(R.id.ap_802_11_mode_5ghz));
        }

        //Set Button        
        setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.wifi_cancel), mListener);

        /* if ("VZW".equals(Config.getOperator())) {
            setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.sp_reset_to_default_NORMAL), mListener);
        } */

        //[Wi-Fi Settings]_START, [For USC Feature], sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
        if ("USC".equals(Config.getOperator()) || "SPR".equals(Config.getOperator())) {
            showPasswordCheckboxPref = callerContext.getSharedPreferences("showpassword", Activity.MODE_PRIVATE);
            showPasswordCheckboxPrefEditor = showPasswordCheckboxPref.edit();
        }
        //[Wi-Fi Settings]_END, [For USC Feature], sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.

        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();
        mSsid.addTextChangedListener(this);
        


        RestartInputByteLengthFilter ssidByteLengthFilter = new RestartInputByteLengthFilter(context, MAX_SSID_COUNTER, "UTF-8");
        // 141016, hyeondug.yeo@lge.com, Prevent to insert Emoji into the Hotspot name field by adding InputFilter. (#TD2422098898)
        if (Config.getCountry().equals("JP")) {
            EmojiFilter emojiFilter = new EmojiFilter();
            mSsid.setFilters(new InputFilter[] {ssidByteLengthFilter, emojiFilter});
        } else {
            mSsid.setFilters(new InputFilter[] {ssidByteLengthFilter});
        }
        mPassword.addTextChangedListener(this);
        RestartInputByteLengthFilter passwordByteLengthFilter = new RestartInputByteLengthFilter(context, MAX_PASS_COUNTER, "UTF-8");
        mPassword.setFilters(new InputFilter[] {passwordByteLengthFilter});

        if (SystemProperties.get("ro.build.target_country").equals("AU") && SystemProperties.get("ro.build.target_operator").equals("TEL")) {
            mPassword.setInputType(
                InputType.TYPE_CLASS_NUMBER | (mShowPassword.isChecked() ?
                InputType.TYPE_NUMBER_VARIATION_NORMAL : 
                InputType.TYPE_NUMBER_VARIATION_PASSWORD));
        }

        if (Config.getCountry().equals("US") && (Config.getOperator().equals("ATT") || Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
            mShowPassword.setChecked(true);
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (mShowPassword.isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));			
            mVisibility.setOnClickListener(this);                    
            mVisibility.setOnCheckedChangeListener(this);
        }

        if (isSupportAdvancedOptions()) {
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (mShowPassword.isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            mVisibility.setOnClickListener(this);
            mVisibility.setOnCheckedChangeListener(this);
        }

        if ("USC".equals(Config.getOperator()) || "SPR".equals(Config.getOperator())) {
            boolean showPassword = false;
            showPassword = showPasswordCheckboxPref.getBoolean("check", true);
            mShowPassword.setChecked(showPassword);
            
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (mShowPassword.isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
        }
        mShowPassword.setOnClickListener(this);
        //[Wi-Fi Settings]_END, [For USC Feature], sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.

        

        // [andrew75.kim@lge.com] 2.4Ghz / 5Ghz [S]
        if (check5gApSupportedbyProp()) {
            int selectBand = Settings.System.getInt(callerContext.getContentResolver(), "mhs_frequency", 0);
            mBC_2_4ghz.setChecked(selectBand == mBCH_2_4GHZ ? true : false);
            mBC_5ghz.setChecked(selectBand == mBCH_5GHZ ? true : false);

            if (check5gApSupportedbyCC()) {
                  if (mBC_2_4ghz.isChecked()) {
                    mBroadcastChannel.setVisibility(View.VISIBLE);
                    //mBroadcastChannel.setSelection(mBroadcastChannelNum);
                      mBC_2_4ghz.setVisibility(View.VISIBLE);
                      mBC_5ghz.setVisibility(View.VISIBLE);
                    mBroadcastChannel_5ghz.setVisibility(View.GONE);
                  } 

                  if (mBC_5ghz.isChecked()) {
                      mBroadcastChannel.setVisibility(View.GONE);
                      mBC_2_4ghz.setVisibility(View.VISIBLE);
                      mBC_5ghz.setVisibility(View.VISIBLE);

                      if ("VZW".equals(Config.getOperator())) {
                          mBroadcastChannel_5ghz.setVisibility(View.VISIBLE);
                      } else {
                          mBroadcastChannel_5ghz.setVisibility(View.GONE);
                      }
                }
            } else {

                mBroadcastChannel.setVisibility(View.VISIBLE);
                //mBroadcastChannel.setSelection(mBroadcastChannelNum);
                mBC_2_4ghz.setVisibility(View.GONE);
                mBC_5ghz.setVisibility(View.GONE);
                mBroadcastChannel_5ghz.setVisibility(View.GONE);

            }
        // G2_KK isn't support BroadcastChannel. 2013-12-11, ilyong.oh@lgepartner.com
        } else if (!isSupportBroadcastChannel()) {
            mView.findViewById(R.id.channel).setVisibility(View.GONE);
            mBroadcastChannel.setVisibility(View.GONE);
            mBC_2_4ghz.setVisibility(View.GONE);
            mBC_5ghz.setVisibility(View.GONE);
            mBroadcastChannel_5ghz.setVisibility(View.GONE);
        } else {
            mBroadcastChannel.setVisibility(View.VISIBLE);
            //mBroadcastChannel.setSelection(mConfigInfo.mBroadcastChannel);
            mBC_2_4ghz.setVisibility(View.GONE);
            mBC_5ghz.setVisibility(View.GONE);
            mBroadcastChannel_5ghz.setVisibility(View.GONE);
        }
        // [andrew75.kim@lge.com] 2.4Ghz / 5Ghz [E]
            

        //Each Carrier Setup
        resumeCarrierMHPValues();

        if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") ||
                        Config.getOperator().equals("MPCS"))) {

            if (mSecurityTypeIndex != OPEN_INDEX && ("").equals(mPassword.getText().toString())) {
                Log.d(TAG, "wifi ap dialog mPassword requestFocus, ");
                mPassword.requestFocus();
            } else {
                mSsid.requestFocus();
            }
        } else {
            mSsid.requestFocus();
            ((EditText)mSsid).setSelection(mSsid.length()); //TD2422120491
        }
        
    }

    private void warnSsidChangeDialog() {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(callerContext);
        alertDlgBuilder.setTitle(R.string.sp_notification_NORMAL);
        alertDlgBuilder.setMessage(R.string.sp_vzw_mhp_ssid_change_warn_text_NORMAL);
        alertDlgBuilder.setCancelable(false);
        alertDlgBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSsid.setText(mWifiConfig.SSID);
                        ((EditText)mSsid).setSelection(mSsid.length());
                    }
                }
            );
        alertDlgBuilder.setPositiveButton(R.string.sp_wifi_inter_help_continue,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        allowSsidChanging = true;
                    }
                }
            );
        mDialog = alertDlgBuilder.create();
        mDialog.show();
    }

    private boolean checkFormValid(String str) {
        int startIndex;
        int maxLen;
        maxLen = str.length();

        for ( startIndex = 0; startIndex < maxLen; startIndex++)  {
            boolean result = Pattern.matches("[\\uAC00-\\uD7A3\\u1100-\\u11F9\\u3131-\\u318E]", str.substring(startIndex));
            if (str.length() > 0 && result) {
                return true;
            }
        }
        return false;
    }
    
    private void Hotspot_ssid() {
        StringBuffer sb = new StringBuffer();
        sb.append(mSsid.getText().toString());

        int bytelen = sb.toString().getBytes().length;


        if ("KR".equals(Config.getCountry())) {
            if (bytelen != mSsid.getText().length()) {
                if (warningDisplayed_ssid == false) {
                    if (checkFormValid(mSsid.getText().toString())) { // seodongjo@lge.com #140563 2012.08.09
                        Toast.makeText(callerContext, R.string.sp_wifihotspot_ssid_korean_NOTSUPPORT, Toast.LENGTH_SHORT).show();
                        warningDisplayed_ssid = true;
                    }
                }
            } else {
                if (warningDisplayed_ssid == true) {
                    warningDisplayed_ssid = false;
                }
            }
        } else if ("VZW".equals(Config.getOperator())) {
            if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED && allowSsidChanging == false) {
                if (mDialog == null || !mDialog.isShowing()) {
                    warnSsidChangeDialog();
                }
            }
        }
    }

    private void Hotspot_pass() {
        StringBuffer sb = new StringBuffer();
        sb.append(mPassword.getText().toString());

        int bytelen = sb.toString().getBytes().length;


        if ("KR".equals(Config.getCountry())) {
            if (bytelen != mPassword.getText().length()) {
                if (warningDisplayed_pass == false) {
                    if (checkFormValid(mPassword.getText().toString())) { // seodongjo@lge.com #140563 2012.08.09
                        Toast.makeText(callerContext, R.string.sp_wifihotspot_password_korean_NOTSUPPORT, Toast.LENGTH_SHORT).show();
                        warningDisplayed_pass = true;
                    }
                }
            } else {
                if (warningDisplayed_pass == true) {
                    warningDisplayed_pass = false;
                }
            }
        }
    }
    /* LGE_CHANGE_S, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
    private boolean validate_ipconfig() {
        String ipAddr = mIpAddress.getText().toString();
        String subNet = mSubnetMask.getText().toString();
        String startAddr = mStartIp.getText().toString();
        String endAddr = mEndIp.getText().toString();
        if (TextUtils.isEmpty(ipAddr) || TextUtils.isEmpty(subNet)
            || TextUtils.isEmpty(startAddr) || TextUtils.isEmpty(endAddr)) { return false; }
        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            inetAddr = NetworkUtils.numericToInetAddress(subNet);
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            inetAddr = NetworkUtils.numericToInetAddress(startAddr);
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            inetAddr = NetworkUtils.numericToInetAddress(endAddr);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
//    private int NetmaskToPrefixLength(String netNotation) {
//        long mask = 0;
//        int zeroCnt = 0;
//        String[] values = netNotation.split("\\.");
//        for (int i = 0; i < values.length; i++) {
//            mask |= Integer.valueOf(values[i]);
//            if (i + 1 < values.length) {
//                mask <<= 8;
//            }
//        }
//        for (int i = 0; i < 32; i++) {
//            if ((mask & 1) == 0) {
//                zeroCnt++;
//                mask >>= 1;
//            } else {
//                return 32 - zeroCnt;
//            }
//        }
//        return 32 - zeroCnt;
//    }
    /* LGE_CHANGE_E, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
    private void validate() {
        //[START][TD#262530] Check Space SSID and Password from ICS, 2012-12-24, ilyong.oh@lge.com
        try { // sunghee78.lee@lge.com 2012.07.03 WBT #378761, #378762 Null Pointer Dereference fixed
            if ( (mSsid != null && mSsid.getText().toString().trim().length() == 0) ||
               (((mSecurityTypeIndex == WPA_INDEX) || (mSecurityTypeIndex == WPA2_INDEX) ) && ((mPassword != null) && (mPassword.getText().toString().trim().length() == 0))) || // // sunghee78.lee@lge.com 2012.07.27 WBT #386320 Null Pointer Dereference fixed
                       (((mSecurityTypeIndex == WPA_INDEX) || (mSecurityTypeIndex == WPA2_INDEX))&&
                           ((mPassword != null) && (mPassword.length() < 8))) ) {
                if (getButton(BUTTON_SUBMIT) != null) { // sunghee78.lee@lge.com 2012.07.27 WBT #386320 Null Pointer Dereference fixed
                    getButton(BUTTON_SUBMIT).setEnabled(false);
                }
            /* LGE_CHANGE_S, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
            } else if ((Config.getOperator().equals("KDDI"))) {
                if (getButton(BUTTON_SUBMIT) != null) {
                    if (validate_ipconfig()) {
                        getButton(BUTTON_SUBMIT).setEnabled(true);
                    }
                    else {
                        getButton(BUTTON_SUBMIT).setEnabled(false);
                    }
                }
            /* LGE_CHANGE_E, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
            } else {
                if (getButton(BUTTON_SUBMIT) != null) { // sunghee78.lee@lge.com 2012.07.27 WBT #386320 Null Pointer Dereference fixed
                    getButton(BUTTON_SUBMIT).setEnabled(true);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //[E N D][TD#262530] Check Space SSID and Password from ICS, 2012-12-24, ilyong.oh@lge.com
    }

//20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [start]
    @Override
    public void onCheckedChanged(CompoundButton c, boolean cb) {
        //Log.d(TAG, "[myseokil] onCheckedChanged");
        if (c.getId() == R.id.show_password) {
            int selectionStart = mPassword.getSelectionStart();
            int selectionEnd = mPassword.getSelectionEnd();

            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
            if (SystemProperties.get("ro.build.target_country").equals("AU") && SystemProperties.get("ro.build.target_operator").equals("TEL")) {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_NUMBER | (cb?
                    InputType.TYPE_NUMBER_VARIATION_NORMAL :
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD));
            }
            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/

            else {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (cb ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            }

            mPassword.setSelection(selectionStart, selectionEnd);
        }
        else if ((c.getId() == R.id.visibility)) {
            if (isSupportAdvancedOptions() || (Config.getCountry().equals("US") && (Config.getOperator().equals("ATT") || Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) || (Config.getCountry().equals("CA") && (Config.getOperator().equals("RGS") || Config.getOperator().equals("TLS") || Config.getOperator().equals("BELL")))) {
                mConfigInfo.mHiddenssid = mVisibility.isChecked() ?  0 : 1;
                Log.d(TAG, "## onCheckedChanged(),   mVisibilityType=" + mConfigInfo.mHiddenssid + "mDoNotShowAgain = " + mDoNotShowAgain);
                if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
                    // [[ 2013.03.20 jonghyun.seon add ssid hide alert popup
                    if (mConfigInfo.mHiddenssid == 1 && !mDoNotShowAgain) {
                        LayoutInflater inflater = (LayoutInflater)callerContext.getSystemService(callerContext.LAYOUT_INFLATER_SERVICE);
                        mAlertDialogView = inflater.inflate(R.layout.tether_hide_dialog, null);
                        
                        mDialogBuilder = new AlertDialog.Builder(callerContext)
                                .setIcon(com.lge.R.drawable.ic_dialog_info_holo)
                                .setTitle(R.string.tether_settings_ssid_hide_title)
                                //.setMessage("test message") // (R.string.tether_settings_ssid_hide_text)
                                //.setIconAttribute(android.R.attr.alertDialogIcon)
                                .setView(mAlertDialogView)
                                .setNeutralButton(R.string.dlg_ok, null);
                        ((CheckBox)mAlertDialogView.findViewById(R.id.do_not_show_again)).setOnClickListener(new View.OnClickListener () 
                        { 
                            //@Overide 
                            public void onClick(View v) 
                            { 
                                if (((CheckBox)v).isChecked()) {
                                    mDoNotShowAgain = true;
                                    SharedPreferences pref_ns = callerContext.getSharedPreferences("NOT_SHOW", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref_ns.edit(); 
                                    editor.putBoolean("not_show", true);
                                    editor.commit();
                                } else {
                                    mDoNotShowAgain = false;
                                    SharedPreferences pref_ns = callerContext.getSharedPreferences("NOT_SHOW", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref_ns.edit(); 
                                    editor.putBoolean("not_show", false);
                                    editor.commit();
                                }
                                    Log.d(TAG, "## onCheckedChanged(),  mDoNotShowAgain = " + mDoNotShowAgain);
                            } 
                        }); 

                        mDialog = mDialogBuilder.create();
                        mDialog.show();

                    }
                    // ]] 2013.03.20 jonghyun.seon add ssid hide alert popup

                }
            }
        }
        else if (isSupportAdvancedOptions() && c.getId() == R.id.wifi_advanced_togglebox) {
            if (cb) {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                if (Config.getOperator().equals("KDDI")) {
                    mView.findViewById(R.id.dhcp_setting_fields).setVisibility(View.VISIBLE);
                    validate();
                }
            } else {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
                if (Config.getOperator().equals("KDDI")) {
                    //mView.findViewById(R.id.dhcp_setting_fields).setVisibility(View.GONE);
                    validate();
                }
            }
        }
    }
//20121207, sijeon@lge.com, TD:241288 Check Showpassword -> input password -> language change -> password show "***" [end]

    public void onClick(View view) {
        //if (Config.getCountry().equals("US") &&
        //    (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS"))) {
        //[START] soonhyuk.choi@lge.com for 5G ap mode
            if (check5gApSupportedbyProp()) {
                if (view.getId() == R.id.broadcast_channel) {
                    mBC_2_4ghz.setChecked(true);
                    mBC_5ghz.setChecked(false);
                    //mSelectChannelNum = mBCH_2_4GHZ;
                    mView.findViewById
                        (R.id.ap_broadcast_channel).setVisibility(View.VISIBLE);
                    mView.findViewById
                        (R.id.ap_broadcast_channel_5ghz).setVisibility(View.GONE);
                    if (isSupportAdvancedOptions() && mBroadcastChannelText != null) {
                        mBroadcastChannelText.setEnabled(true);
                        mView.findViewById(R.id.ap_broadcast_channel).setEnabled(true);
                    }
                    
                    if ("VZW".equals(Config.getOperator())) {
                        mView.findViewById
                            (R.id.ap_802_11_mode).setVisibility(View.VISIBLE);
                        mView.findViewById
                            (R.id.ap_802_11_mode_5ghz).setVisibility(View.GONE);
                        //m802_11_Mode.setSelection(mode);
                    }
                }
                if (view.getId() == R.id.broadcast_channel_5ghz) {
                    mBC_2_4ghz.setChecked(false);
                    mBC_5ghz.setChecked(true);
                    //mSelectChannelNum = mBCH_5GHZ;
                    if (isSupportAdvancedOptions() && mBroadcastChannelText != null) {
                        mBroadcastChannelText.setEnabled(false);
                        mView.findViewById(R.id.ap_broadcast_channel).setEnabled(false);
                    } else {
                        mView.findViewById
                            (R.id.ap_broadcast_channel).setVisibility(View.GONE);
// soonhyuk.choi@lge.com the Spinner for the 5G is disabled
// until the Auto channel Selection function is completed.
//                    mView.findViewById
//                        (R.id.ap_broadcast_channel_5ghz).setVisibility(View.VISIBLE);
                    }

                    if ("VZW".equals(Config.getOperator())) {
                        mView.findViewById
                            (R.id.ap_802_11_mode).setVisibility(View.GONE);
                        mView.findViewById
                            (R.id.ap_802_11_mode_5ghz).setVisibility(View.VISIBLE);
                        //m802_11_Mode.setSelection(mode);
                    }
                }
            }
        //[END] soonhyuk.choi@lge.com for 5G ap mode
        //}

        //[Wi-Fi Settings]_START, [For USC Feature], sangheon.shim/hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.
        if ( "USC".equals(Config.getOperator()) || "SPR".equals(Config.getOperator()) ) {
            showPasswordCheckboxPrefEditor.putBoolean("check", mShowPassword.isChecked());
            showPasswordCheckboxPrefEditor.commit();
        }
        //[Wi-Fi Settings]_END, [For USC Feature], sangheon.shim/ hyeondug.yeo@lge.com, Save the status of checkbox(show password) by using shared preferences.        

        // Cursor Index when click password view.
        if (view.getId() == R.id.show_password) {
            int selectionStart = mPassword.getSelectionStart();
            int selectionEnd = mPassword.getSelectionEnd();
            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
            if (SystemProperties.get("ro.build.target_country").equals("AU") && SystemProperties.get("ro.build.target_operator").equals("TEL")) {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_NUMBER | (((CheckBox)view).isChecked() ?
                    InputType.TYPE_NUMBER_VARIATION_NORMAL :
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD));
            }
            /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
            else {
                mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            }
                mPassword.setSelection(selectionStart, selectionEnd);
        }

        /* LGE_CHANGE_S, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
        if (!isSupportAdvancedOptions() && Config.getOperator().equals("KDDI")) {
            if (view.getId() == R.id.wifi_advanced_togglebox) {
                if (((CheckBox)view).isChecked()) {
                    Log.d(TAG, "[KDDI]Tethering setCheckBoxSection - wifi_advanced_togglebox - isChecked(TRUE)");
                    mView.findViewById(R.id.dhcp_setting_fields).setVisibility(View.VISIBLE);
                    validate();
                } else {
                    Log.d(TAG, "[KDDI]Tethering setCheckBoxSection - wifi_advanced_togglebox - isChecked(FALSE)");
                    mView.findViewById(R.id.dhcp_setting_fields).setVisibility(View.GONE);
                    validate();
                }
            }
        }
        /* LGE_CHANGE_E, [WiFi][hyunseong.lee@lge.com], 2013-08-06, add KDDI Tethering feature */
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [START]*/
        /*if (SystemProperties.get("ro.build.target_country").equals("AU") && SystemProperties.get("ro.build.target_operator").equals("TEL")) {
            if (mSsid != null) {
                String str = mSsid.getText().toString();
                if (null == str) {
                    return;
                }

                if (!str.startsWith(TELSTRA)) {
                    //Toast.makeText(getContext(), "SSID TelstraXXXX\nX=Alphanumeric only", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getContext(), "SSID TelstraXXXX\nX=Alphanumeric only", Toast.LENGTH_SHORT).show();
                    mSsid.setText(mCurrentSsid);
                    ((EditText)mSsid).setSelection(start + before);
                    return;
                }
                for (int index = TELSTRA.length(); index < str.length(); ++index) {
                    if (!Character.isLetterOrDigit(str.charAt(index))) {
                        Toast.makeText(getContext(), "SSID TelstraXXXX\nX=Alphanumeric only", Toast.LENGTH_SHORT).show();
                        mSsid.setText(mCurrentSsid);
                        ((EditText)mSsid).setSelection(start);
                        return;
                    }
                }
            }
        }*/
        /*20130123 raghu.a@lge.com Telstra Australia : Wi-Fi Encryption key format [END]*/
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //mCurrentSsid = mSsid.getText().toString();
    }

    public void afterTextChanged(Editable editable) {
        if (mWifiConfig.SSID != null && mSsid.getText().toString() != null) {
            if (!mWifiConfig.SSID.equals(mSsid.getText().toString())) {
                Hotspot_ssid();
            }
        }
        if (mWifiConfig.preSharedKey != null && !mWifiConfig.preSharedKey.equals(mPassword.getText().toString())) {
            Hotspot_pass();
        }
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSecurityTypeIndex = position;
        showSecurityFields();
        showVZWNoneDialog();
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
    private void showVZWNoneDialog() {
        if ("VZW".equals(Config.getOperator()) && mSecurityTypeIndex == OPEN_INDEX) {
            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(callerContext);
            alertDlgBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(R.string.sp_notification_NORMAL);
            alertDlgBuilder.setMessage(R.string.sp_none_security_warning_NORMAL);
            alertDlgBuilder.setPositiveButton(R.string.sp_ok_NORMAL, null);
            alertDlgBuilder.create().show();
        }
    }
    private void showSecurityFields() {
        if (mSecurityTypeIndex == OPEN_INDEX) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
//[START] hoon2.lim 20120822  When changing the security type to open, the toast pop-up is displayed
            if (Config.getCountry().equals("US") && (Config.getOperator().equals("TMO") || Config.getOperator().equals("MPCS")) && (mIsCreteMode == false)) {
                Toast warningMessage = Toast.makeText(callerContext, R.string.sp_mobile_hotspot_open_warn_NORMAL, Toast.LENGTH_LONG);
                warningMessage.show();
            }
//[END] hoon2.lim 20120822  When changing the security type to open, the toast pop-up is displayed	
            //[TD#286593] Move Focus to SSID when selected Security Open. 2013-01-21, ilyong.oh@lge.com
            if (mSsid != null) {
                mSsid.requestFocus();
            }
            return;
        }
        mIsCreteMode = false;	
        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }

    //[START][TD#204043] resartInput when over max vlaue on SSID and Password, 2012-10-30, ilyong.oh@lge.com
    private class RestartInputByteLengthFilter extends ByteLengthFilter {
        private Toast mToast = null;    //[TD#245041] Applied Toast When input MaxLength, 2012-12-05, ilyong.oh@lge.com

        public RestartInputByteLengthFilter(Context context, int max, String encode) {
            super(context, max, encode);
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence cs = super.filter(source, start, end, dest, dstart, dend);

            if (TextUtils.equals(cs, "") || cs != null) {
                InputMethodManager iMethodManager;
                iMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                View editorView = getCurrentFocus();
                if (iMethodManager != null && editorView != null) {
                    iMethodManager.restartInput(editorView);
                }

                //[START][TD#245041] Applied Toast When input MaxLength, 2012-12-05, ilyong.oh@lge.com
                if (mToast == null) {
                    mToast = Toast.makeText(getContext(), R.string.sp_auto_reply_maxlength_NORMAL,
                            Toast.LENGTH_SHORT);
                }
                mToast.show();
                //[E N D][TD#245041] Applied Toast When input MaxLength, 2012-12-05, ilyong.oh@lge.com
            }
            return cs;
        }
    }
    //[E N D][TD#204043] resartInput when over max vlaue on SSID and Password, 2012-10-30, ilyong.oh@lge.com

    // 141016, hyeondug.yeo@lge.com, Prevent to insert Emoji into the Hotspot name field by adding InputFilter. (#TD2422098898)
    private class EmojiFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String regex = "[^\u0000-\uFFFF]";
            String s = source.toString();
            if (s.matches(".*"+regex+".*")) {
                Toast.makeText(callerContext, R.string.sp_wifi_hotspot_ssid_invalid_NORMAL, Toast.LENGTH_SHORT).show();
                return s.replaceAll(regex, "");
            }
            return null;
        }
    }

    //20130524, yeongsu.wu, hotspot max user settings upon GSM network roaming
    private boolean isNetworkTypeGSM() {
        TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        networkType = tm.getNetworkType();
        Log.e(TAG, "isNetworkTypeGSM(), networkType = " + networkType);

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true;
            default:
                break;
        }

        return false;
    }

    //[START] soonhyuk.choi@lge.com for 5G ap mode
    // To check the 5G ap feature enabled with system property.
    public int changeDbToIndex(int indexDb) {
        switch (indexDb) {
            case 149:
                return 0;
            case 153:
                return 1;
            case 157:
                return 2;
            case 161:
                return 3;
            default:
                return 0;
        }
    }

    public int changeIndexToDb(int Index) {
        switch (Index) {
            case 0:
                return 149;
            case 1:
                return 153;
            case 2:
                return 157;
            case 3:
                return 161;
            default:
                return 0;
        }
    }   
    public boolean check5gApSupportedbyProp() {
        if (SystemProperties.getBoolean("wlan.lge.softap5g", false) == false) {
            Log.d(TAG, "Tethering 5G property is false");
            return false;
        } else {
            return true;
        }
    }

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
        String mCountry = Settings.Global.getString(callerContext.getContentResolver(),
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

    public boolean isSupportBroadcastChannel() {
        // Check each model Ex.F320
        /* Target ProductName Check.
        String targetProductName = SystemProperties.get("ro.product.name");
        String[] productNames = {"g2_lgu_kr", "g2_skt_kr", "g2_kt_kr"};

        Log.d(TAG, "isSupportBroadcastChannel() : " + targetProductName);

        for (int i = 0; i < productNames.length; i++) {
            if (productNames[i].equals(targetProductName)) {
                return false;
            }
        }
        */

        // Tartget DeviceName Check.
        // Not Support KK Upgrade models.
        String targetDeviceName = SystemProperties.get("ro.product.device");
        String[] deviceNames = {"g2", "vu3", "zee", "geefhd", "geehrc", 
                                "vu2", "fx1sk", "awifi070u", "omega", "gvfhd" };

        Log.d(TAG, "isSupportBroadcastChannel() : " + targetDeviceName);

        for (int i = 0; i < deviceNames.length; i++) {
            if (targetDeviceName.contains(deviceNames [i])) {
                if (Utils.isUI_4_1_model(getContext())) { // for Zero day OTA
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isSupportAdvancedOptions() {
        if (Utils.isUI_4_1_model(getContext())
            && !(Config.getCountry().equals("US")
                && (Config.getOperator().equals("ATT")
                    || Config.getOperator().equals("TMO")
                        || Config.getOperator().equals("MPCS")))
                            && !(Config.getCountry().equals("CA")
                                && (Config.getOperator().equals("RGS")
                                    || Config.getOperator().equals("TLS")
                                        || Config.getOperator().equals("BELL")))) {
            return true;
        }

        /*if (SystemProperties.getBoolean("wifi.lge.common_hotspot", false) == true) {
            return true;
        }*/

        return false;
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        switch (wifiConfig.getAuthType()) {
            case KeyMgmt.WPA_PSK:
                return "WPA_PSK";
            case KeyMgmt.WPA2_PSK:
                return "WPA2_PSK";
            default:
                return "OPEN";
        }
    }

    //change db if wificonfig value and db are different
    private void resumeCarrierMHPValues() {
        Log.d(TAG, "resumeCarrierMHPValues");

        //ssid
        String ssid = mWifiConfig.SSID;

        //security, Authentication, Protocol, Encryption
        String securitytype = getSecurityType(mWifiConfig);
        int securitytypeIndex = getSecurityTypeIndex(mWifiConfig);
        Settings.System.putString(callerContext.getContentResolver(), "mhs_security", securitytype);

        //password
        String presharedkey = mWifiConfig.preSharedKey;
        Settings.System.putString(callerContext.getContentResolver(), "mhs_wpakey", presharedkey);

        if (securitytypeIndex == WPA_INDEX) {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            mWifiConfig.allowedProtocols.set(Protocol.WPA);
            mWifiConfig.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
            mWifiConfig.preSharedKey = presharedkey;
        } else if (securitytypeIndex == WPA2_INDEX) {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
            mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            mWifiConfig.allowedProtocols.set(Protocol.RSN);
            mWifiConfig.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
            mWifiConfig.preSharedKey = presharedkey;
        } else {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
            mWifiConfig.preSharedKey = "";
        }

        //mhs_max_client
        int mhs_max_client = 0;

        if (Config.getOperator().equals("VZW")) {
            mhs_max_client = Settings.System.getInt(callerContext.getContentResolver(), "mhs_max_client", 10);
        } else {
            mhs_max_client = Settings.System.getInt(callerContext.getContentResolver(),
                                            "mhs_max_client", 8);
        }

        //hidden ssid
        int hiddenssid = 0;

        if (mWifiConfig.hiddenSSID == false) {
            hiddenssid = 0;
        } else {
            hiddenssid = 1;
        }
        Settings.System.putInt(callerContext.getContentResolver(), "mhs_hidden_ssid", hiddenssid);

        //channel
        //2G channel
        int ap_2g_channel = 0;
        try {
             ap_2g_channel = Settings.System.getInt(callerContext.getContentResolver(), "mhs_2g_channel");
        } catch (SettingNotFoundException e) {
             ap_2g_channel = 6;
        }

        //5G channel
        int ap_5g_channel = 0;
        try {
             ap_5g_channel = Settings.System.getInt(callerContext.getContentResolver(), "mhs_5g_channel");
        } catch (SettingNotFoundException e) {
             ap_5g_channel = 149;
        }

        //hw_mode
        String hw_mode = Settings.System.getString(callerContext.getContentResolver(), "mhs_hw_mode");
//        int ieee_mode = Settings.System.getInt(callerContext.getContentResolver(), "mhs_ieee_80211n" , 0);

        //Allowed device
        int mac_Acl = Settings.System.getInt(callerContext.getContentResolver(), "mhs_mac_acl" , 0);

        // 0 = 2.4GHz (Default)
        // 1 = 5GHz
        int selectBand = Settings.System.getInt(callerContext.getContentResolver(), "mhs_frequency", 0);

        mConfigInfo.mSsid = mWifiConfig.SSID;

        if (mWifiConfig.hiddenSSID == true) {
            mConfigInfo.mHiddenssid = 1;
        } else {
            mConfigInfo.mHiddenssid = 0;
        }

        //dhcp
        String gateway = Settings.System.getString(callerContext.getContentResolver(), "gateway");
        String mask = Settings.System.getString(callerContext.getContentResolver(), "mask");
        String start_ip = Settings.System.getString(callerContext.getContentResolver(), "start_ip");
        String end_ip = Settings.System.getString(callerContext.getContentResolver(), "end_ip");

        mConfigInfo.mSecurity = securitytype;
        mConfigInfo.mHWmode = hw_mode;
        mConfigInfo.mMaxConnect = mhs_max_client;
        mConfigInfo.mSecuritykey = presharedkey;
        mConfigInfo.mBroadcastChannel =  ap_2g_channel;
        mConfigInfo.mBroadcast5gChannel =  ap_5g_channel;
        mConfigInfo.mFrequency =  selectBand;
        mConfigInfo.mGateway = gateway;
        mConfigInfo.mMask = mask;
        mConfigInfo.mStartip = start_ip;
        mConfigInfo.mEndip = end_ip;

        Log.d(TAG, "resumeMHPValues " + ssid + "," + hiddenssid
                + "," + securitytype
                + "," + hw_mode + " ," + mhs_max_client + ","
                + presharedkey + "," + ap_2g_channel + "," + ap_5g_channel + "," + selectBand + "," + mac_Acl);

        backupConfig();

        setCarrierMHPValues(mConfigInfo.mSsid, mConfigInfo.mHiddenssid,
                mConfigInfo.mSecurity,
                mConfigInfo.mSecuritykey, mConfigInfo.mBroadcastChannel,
                mConfigInfo.mBroadcast5gChannel,
                mConfigInfo.mHWmode, mConfigInfo.mMaxConnect,
                mConfigInfo.mFrequency, mConfigInfo.mGateway,
                mConfigInfo.mMask, mConfigInfo.mStartip,
                mConfigInfo.mEndip);
    }

    private void backupConfig() {
        Log.d(TAG, "backupConfig");
        // Use to recovery
        mBackupConfig.copyValue(mConfigInfo);
    }   

    private void recoveryConfig() {
        Log.d(TAG, "recoveryConfig");
        mConfigInfo.copyValue(mBackupConfig);
    }

    public void doRecovery() {
        Log.d(TAG, "doRecovery");
        recoveryConfig();
        Log.d(TAG, "mConfigInfo.SSID = " + mConfigInfo.mSsid);
        Log.d(TAG, "mBackupConfig.SSID = " + mBackupConfig.mSsid);

        //Backup --> Display
        setCarrierMHPValues(mConfigInfo.mSsid, mConfigInfo.mHiddenssid,
                mConfigInfo.mSecurity, 
                mConfigInfo.mSecuritykey, mConfigInfo.mBroadcastChannel,
                mConfigInfo.mBroadcast5gChannel,
                mConfigInfo.mHWmode, mConfigInfo.mMaxConnect,
                mConfigInfo.mFrequency, mConfigInfo.mGateway,
                mConfigInfo.mMask, mConfigInfo.mStartip,
                mConfigInfo.mEndip);        
    }

    public WifiConfiguration getAppliedConfig() {
        Log.d(TAG, "doApplyConfig");
        Log.d(TAG, "mConfigInfo.SSID = " + mConfigInfo.mSsid);
        Log.d(TAG, "mConfigInfo.mSecurity = " + mConfigInfo.mSecurity);
        Log.d(TAG, "mConfigInfo.mSecuritykey = " + mConfigInfo.mSecuritykey);
        Log.d(TAG, "mConfigInfo.mBroadcastChannel = " + mConfigInfo.mBroadcastChannel);
        Log.d(TAG, "mConfigInfo.mMaxConnect = " + mConfigInfo.mMaxConnect);
        Log.d(TAG, "mConfigInfo.mHiddenssid = " + mConfigInfo.mHiddenssid);

        WifiConfiguration mWifiConfig = new WifiConfiguration();

        mWifiConfig.SSID  = mConfigInfo.mSsid;

        //security, Authentication, Protocol, Encryption
        Settings.System.putString(callerContext.getContentResolver(), "mhs_security", mConfigInfo.mSecurity);

        if ("OPEN".equals(mConfigInfo.mSecurity)) {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
            mWifiConfig.preSharedKey = "";
        } else if ("WPA_PSK".equals(mConfigInfo.mSecurity)) {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            mWifiConfig.allowedProtocols.set(Protocol.WPA);
            mWifiConfig.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
            mWifiConfig.preSharedKey = mConfigInfo.mSecuritykey;
        } else if ( "WPA2_PSK".equals(mConfigInfo.mSecurity)) {
            mWifiConfig.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
            mWifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            mWifiConfig.allowedProtocols.set(Protocol.RSN);
            mWifiConfig.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
            mWifiConfig.preSharedKey = mConfigInfo.mSecuritykey;
        }

        Settings.System.putString(callerContext.getContentResolver(), "mhs_wpakey", mConfigInfo.mSecuritykey);
        if (mConfigInfo.mHiddenssid == 0) {
            mWifiConfig.hiddenSSID = false;
        } else {
            mWifiConfig.hiddenSSID = true;
        }
        Settings.System.putInt(callerContext.getContentResolver(), "mhs_hidden_ssid", mConfigInfo.mHiddenssid);

        mWifiConfig.preSharedKey = mConfigInfo.mSecuritykey;

        Settings.System.putInt(callerContext.getContentResolver(), "mhs_2g_channel", mConfigInfo.mBroadcastChannel);
        Settings.System.putInt(callerContext.getContentResolver(), "mhs_5g_channel", mConfigInfo.mBroadcast5gChannel);

        Settings.System.putInt(callerContext.getContentResolver(), "mhs_max_client", mConfigInfo.mMaxConnect);
        Settings.System.putInt(callerContext.getContentResolver(), "mhs_frequency", mConfigInfo.mFrequency);

        if (Config.getOperator().equals("VZW")) {
            Settings.System.putString(callerContext.getContentResolver(), "mhs_hw_mode", mConfigInfo.mHWmode);
            Toast.makeText(callerContext,
                    callerContext.getResources().getString(R.string.sp_CONFIGSAVED_NORMAL),
                    Toast.LENGTH_SHORT).show();
        }

        if (Config.getOperator().equals("KDDI")) {
            Settings.System.putString(callerContext.getContentResolver(), "gateway", mConfigInfo.mGateway);
            Settings.System.putString(callerContext.getContentResolver(), "mask", mConfigInfo.mMask);
            Settings.System.putString(callerContext.getContentResolver(), "start_ip", mConfigInfo.mStartip);
            Settings.System.putString(callerContext.getContentResolver(), "end_ip", mConfigInfo.mEndip);
        }
        return mWifiConfig;
    }

    public void commitMHPChanges() {
//need to fix 
        Log.d(TAG, "commitMHPChanges");
        
        if (mConfigInfo == null) {  
            return;
        }

        mConfigInfo.mSsid = mSsid.getText().toString();
        mConfigInfo.mHiddenssid = mVisibility.isChecked() ?  0 : 1;

        int index = mSecurity.getSelectedItemPosition();
        if (index == WPA_INDEX) {
            mConfigInfo.mSecurity = "WPA_PSK";
        } else if (index == WPA2_INDEX) {
            mConfigInfo.mSecurity = "WPA2_PSK";
        } else {
            mConfigInfo.mSecurity = "OPEN";
        }
        mConfigInfo.mSecuritykey = mPassword.getText().toString(); // need to be fixed
        Log.d(TAG, "security : " + mConfigInfo.mSecurity);
        Log.d(TAG, "security key : " + mConfigInfo.mSecuritykey);

        int hwmode_index;
        if (mBC_2_4ghz.isChecked()) {
             mConfigInfo.mBroadcastChannel = mBroadcastChannel.getSelectedItemPosition();
             mConfigInfo.mFrequency = 0;
        }

        if (mBC_5ghz.isChecked()) {
            int mBC5GHIndex = mBroadcastChannel_5ghz.getSelectedItemPosition();
            mConfigInfo.mBroadcast5gChannel = changeIndexToDb(mBC5GHIndex);
            mConfigInfo.mFrequency = 1;
        }

        if (Config.getOperator().equals("VZW")) { 
            if (mBC_2_4ghz.isChecked()) {
             hwmode_index = m802_11.getSelectedItemPosition();
             if (hwmode_index == 0) {
                mConfigInfo.mHWmode = "b";
             } else if (hwmode_index == 1) {
                mConfigInfo.mHWmode = "g";
             } else if (hwmode_index == 2) {
                mConfigInfo.mHWmode = "n";
             } else {
                mConfigInfo.mHWmode = "n";
             }
        }

        if (mBC_5ghz.isChecked()) {
             hwmode_index = m802_11.getSelectedItemPosition();
            if (hwmode_index == 0) {
                mConfigInfo.mHWmode = "a";
             } else {
                mConfigInfo.mHWmode = "n";
             }             
        }        
        }

        if (Config.getOperator().equals("KDDI")) {
            mConfigInfo.mGateway = mIpAddress.getText().toString();
            mConfigInfo.mMask = mSubnetMask.getText().toString();
            mConfigInfo.mStartip = mStartIp.getText().toString();
            mConfigInfo.mEndip = mEndIp.getText().toString();
        }
        
        mConfigInfo.mMaxConnect = mMaxClient.getSelectedItemPosition() + 1;

    }

    private void setCarrierMHPValues(String ssid, int hiddenssid, String security,
            String securitykey,
            int ap_2g_channel, int ap_5g_channel, String hwmode, int maxclient, int frequency, String gateway, 
            String mask, String startip, String endip) {

        Log.d(TAG, "setCarrierMHPValues " + ssid + ",   " + hiddenssid
                + ",   " + securitykey + " ,   " + ap_2g_channel + ",   " + ap_5g_channel + ",   " + hwmode + ",   " + maxclient + ",   " + frequency);
        
        mSsid.setText(ssid);
        if ("WEP".equals(security)) {
            //wep_key
            Log.d(TAG, "does not support WEP");
        } else if ("WPA_PSK".equals(security)) {
            mSecurityTypeIndex = WPA_INDEX;                          
        } else if ("WPA2_PSK".equals(security)) {            
            mSecurityTypeIndex = WPA2_INDEX;
        } else {
            mSecurityTypeIndex = OPEN_INDEX;
        }

        mSecurity.setSelection(mSecurityTypeIndex);
        if (mSecurityTypeIndex == WPA_INDEX || mSecurityTypeIndex == WPA2_INDEX) {
                if ((securitykey == null) || (securitykey.equals("        "))) {
                    mPassword.setText("");
                } else {
                    mPassword.setText(securitykey);
                }            
            
        } else {
            mPassword.setText("");
        }

        mMaxClient.setSelection(maxclient - 1);

        mVisibility.setChecked(hiddenssid == 1 ? false : true);

        if (frequency == 0) {
            mBC_2_4ghz.setChecked(true);
            mBC_5ghz.setChecked(false);
            mBroadcastChannel.setSelection(ap_2g_channel);
        } else {
            int mBC5GHIndex = changeDbToIndex(ap_5g_channel);
            mBC_2_4ghz.setChecked(false);
            mBC_5ghz.setChecked(true);
            mBroadcastChannel_5ghz.setSelection(mBC5GHIndex);
        }

        
        int hwmode_index = 0;
        if (Config.getOperator().equals("VZW")) {        
            if (frequency == 0) {
                if ("b".equals(hwmode)) {
                    hwmode_index = 0;
                } else if ("g".equals(hwmode)) {
                    hwmode_index = 1;                          
                } else if ("n".equals(hwmode)) {            
                    hwmode_index = 2;
                } else {
                    hwmode_index = 2;
                }
                m802_11.setVisibility(View.VISIBLE);
                m802_11.setSelection(hwmode_index);
                m802_11_5ghz.setVisibility(View.GONE);
            } else {
                if ("a".equals(hwmode)) {
                    hwmode_index = 0;
                } else {
                    hwmode_index = 0;
                }
            
                m802_11.setVisibility(View.GONE);
                m802_11_5ghz.setSelection(hwmode_index);
                m802_11_5ghz.setVisibility(View.VISIBLE);
            }
        }

        if (Config.getOperator().equals("KDDI")) {
            mIpAddress.setText(gateway);
            mSubnetMask.setText(mask);
            mStartIp.setText(startip);
            mEndIp.setText(endip);
            mIpAddress.addTextChangedListener(this);
            mSubnetMask.addTextChangedListener(this);
            mStartIp.addTextChangedListener(this);
            mEndIp.addTextChangedListener(this);
        }

    }

}
