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

package com.android.settings.deviceinfo;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellBroadcastMessage;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManagerEx;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.R;
import com.android.settings.Utils;
/*LGSI_CHANGE_S:chinese PLMN implementaion
2011-12-28,manikanta.varmak@lge.com,
Implentination for chinese language when the language of device is tawin or chinese in manual search*/
import java.util.Locale;

/*LGSI_CHANGE_E:chinese PLMN implementaion*/
import java.lang.ref.WeakReference;
import com.android.settings.lgesetting.Config.Config;

//[2012.05.29][munjohn.kang]changed roaming_menu display by the Chameleon status
import com.lge.constants.SettingsConstants;

// [S][2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.lge.OverlayUtils;
// [E][2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber

// LGE_CHANGE_S, IMEI Barcode
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
// LGE_CHANGE_E, IMEI Barcode

/* LGSI_CHANGE_S: Same operator name is coming in about phone for different operator
 * 2013-01-17,arshad.mumtaz@lge.com@lge.com,
 * Imsi API was giving only first slot IMSI, so changed to get as per shared preference
 */
// import android.telephony.MSimTelephonyManager;
/* LGSI_CHANGE_E: Same operator name is coming in about phone for different operator */

import com.android.settings.lge.Svc_cmd;

// [S][2013.04.02][yongjaeo.lee@lge.com][VZW] Last factory data reset
import java.io.File;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
// [E]

import com.android.settings.lge.ATClientUtils;
import com.android.settings.utils.MSIMUtils;

// [S][2013.04.10][yongjaeo.lee@lge.com][VZW] Rooting status
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
// [E]
import com.android.settings.Utils;

// [S][2013.06.26][yongjaeo.lee@lge.com][SPR] get value of UICC IMSI
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.IccCardConstants;
// [E]

import com.lge.telephony.LGServiceState;
import com.android.settings.SLog;
import com.android.internal.telephony.PhoneConstants;
import com.lge.telephony.provider.TelephonyProxy;

import com.lge.uicc.LGUiccManager;

import com.android.settings.utils.LGSubscriptionManager;

/**
 * Display the following information
 * # Phone Number
 * # Network
 * # Roaming
 * # Device Id (IMEI in GSM and MEID in CDMA)
 * # Network type
 * # Signal Strength
 * # Battery Strength  : TODO
 * # Uptime
 * # Awake Time
 * # XMPP/buzz/tickle status : TODO
 *
 */
public class Status extends PreferenceActivity {
    private static final String LOG_TAG = "aboutphone # Status";
    private static final boolean DBG = false;

    private static final String KEY_DATA_STATE = "data_state";
    private static final String KEY_SERVICE_STATE = "service_state";
    private static final String KEY_OPERATOR_NAME = "operator_name";
    private static final String KEY_ROAMING_STATE = "roaming_state";
    private static final String KEY_NETWORK_TYPE = "network_type";
    private static final String KEY_LATEST_AREA_INFO = "latest_area_info";
    private static final String KEY_NETWORK_TYPE_STRENGTH = "network_type_strength";
    private static final String KEY_IMS_REGISTRATION_STATUS = "ims_registration_status";
    private static final String KEY_PHONE_NUMBER = "number";
    private static final String KEY_IMEI_SV = "imei_sv";
    private static final String KEY_MAIN_SOFTWARE_VERSION = "main_software_version";
    private static final String KEY_IMEI = "imei";
    private static final String KEY_PRL_VERSION = "prl_version";
    // [2012.04.05][youngmin.jeon] This menu item is only for MPCS Operator
    private static final String KEY_ERI_VERSION = "eri_version";
    private static final String KEY_MIN_NUMBER = "min_number";
    // [2012.01.15][jm.lee2] ES4 merge
    private static final String KEY_ESN_NUMBER = "esn_number";
    private static final String KEY_MEID_NUMBER = "meid_number";
    // [S][2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
    private static final String KEY_MEID_HEX = "meid_hexa";
    private static final String KEY_MEID_DEC = "meid_decimal";
    // [E][2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
    private static final String KEY_SIGNAL_STRENGTH = "signal_strength";
    private static final String KEY_BATTERY_STATUS = "battery_status";
    private static final String KEY_BATTERY_LEVEL = "battery_level";
    private static final String KEY_WIFI_IP_ADDRESS = "wifi_ip_address";
    private static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    private static final String KEY_BT_ADDRESS = "bt_address";
    private static final String KEY_SERIAL_NUMBER = "serial_number";
    private static final String KEY_ICC_ID = "icc_id";
    private static final String KEY_LAST_FACTORY_DATA_RESET = "last_factory_date_reset";
    private static final String KEY_ROOTING_STATUS = "rooting_status";

    private static final String KEY_WIMAX_MAC_ADDRESS = "wimax_mac_address";
    // [START_LGE_VOICECALL] , ADD, soora.bang , 2012-05-10, add NV Lifetimer calls, Only MPCS menu
    private static final String KEY_LIFETIME_CALL = "life_time_call";
    // [END_LGE_VOICECALL] , ADD, soora.bang , 2012-05-10, add NV Lifetimer calls, Only MPCS menu
    // [S] 20120513 eddie.kim@lge.com for data life timer
    private static final String KEY_LIFETIME_DATA = "life_time_data";
    // [E] 20120513 eddie.kim@lge.com for data life timer
    // [START_LGE_VOICECALL] , ADD, soora.bang , 2012-05-16, [VoiceCall][LW770][NVitem] add Lifetimecalls and WDC for CRK model
    private static final String KEY_WDC = "warranty_date_code";
    // [END_LGE_VOICECALL] , ADD, soora.bang , 2012-05-16, [VoiceCall][LW770][NVitem] add Lifetimecalls and WDC for CRK model
    private static final String KEY_BATTERY_USE = "battery_use"; //taewon.hwang@lge.com 2012.11.26: Remove Battery use menu because #helphelp* is not support battery use.

    private static final String KEY_BATTERY_CONDITION = "battery_condition_dcm"; //taewon.hwang@lge.com 2013.01.07: Remove Battery condition menu because #helphelp* is not support battery condition.
    private static final String KEY_REV_CHECK = "rev_check";
    private static final String KEY_IMEI_SVN_GSM = "imei_svn_gsm";
    private static final String KEY_IMEI_SVN = "imei_svn";
    private String mLifeTimer;
    private String mMDNbyRTNValue;
    private String mMINbyRTNValue;
    private String mPRLbyRTNValue;

    // embedded battery model ( regulatory & factory date & SN )
    private static final String KEY_MANUFACTURE_SN = "manufacture_serial_number";
    private static final String KEY_MANUFACTURE_C_C = "manufacture_company_country";
    private static final String KEY_MANUFACTURE_DATE = "manufacture_serial_number_date";

    // H/W Version
    private static final String PROPERTY_PCB_VER = "ro.pcb_ver";
    private static final String PROPERTY_HW_REVISION = "ro.lge.hw.revision";
    /* LGSI_CHANGE_S: Same operator name is coming in about phone for different operator
     * 2013-01-17,arshad.mumtaz@lge.com@lge.com,
     * Imsi API was giving only first slot IMSI, so changed to get as per shared preference
     */
    public static final int SIM_SLOT_1_SEL = 0;
    public static final int SIM_SLOT_2_SEL = 1;
    /* LGSI_CHANGE_E: Same operator name is coming in about phone for different operator */
    public static final int SIM_COMMON_SEL = 9999;

    private static final String KEY_SUPPORT_TECHNOLOGY = "support_technology";
    private static String SUPPORT_TECHNOLOGY = "CDMA 1X: 850/1900/1700/2100\nCDMA EVDO Rev.A: 850/1900/1700/2100\nLTE:Band 2,Band 4";

    private static final String KEY_SMSC = "smsc";
    private static final String KEY_IMSI = "imsi";
    private Preference mSMSC;
    private static final int EVENT_QUERY_SMSC_DONE = 1005;

    private static final String KEY_DEVICE_MODEL = "device_model";

    private static final String FACTORY_RESET_DATE = "factroy_reset_date";

    private static final String KEY_CSN = "csn";
    private static final String KEY_LGU_SN = "manufacture_lgu_serial_number";

    private static final String KEY_CELL_ID = "cell_id";
    //private static final String KEY_PRL_LIST = "prl_list";

    boolean csActive = false;

    private static final String[] PHONE_RELATED_ENTRIES = {
            KEY_DATA_STATE,
            KEY_SERVICE_STATE,
            KEY_OPERATOR_NAME,
            KEY_ROAMING_STATE,
            KEY_NETWORK_TYPE,
            KEY_LATEST_AREA_INFO,
            KEY_PHONE_NUMBER,
            KEY_IMEI,
            KEY_IMEI_SV,
            KEY_PRL_VERSION,
            KEY_MIN_NUMBER,
            KEY_ESN_NUMBER, //[2012.01.15][jm.lee2] ES4 merge
            KEY_MEID_NUMBER,
            KEY_MEID_HEX, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_MEID_DEC, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_SIGNAL_STRENGTH,
            KEY_ICC_ID
    };

    //[S][2012.02.06][jm.lee2] hide Item for LGU+ (request from CDMA protocol<seunggyu.lee@lge.com>)
    // [2012.03.11][seungeene] remove IMEI, IMEI_SV from LGU_HIDE_ENTRIES because of IMEI blacklist enforcement
    private String[] LGU_HIDE_ENTRIES = {
            KEY_MIN_NUMBER,
            KEY_PRL_VERSION,
            KEY_ESN_NUMBER,
            KEY_MEID_NUMBER,
            KEY_MEID_HEX, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_MEID_DEC, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_ICC_ID
    };
    //[E][2012.02.06][jm.lee2] hide Item for LGU+ (request from CDMA protocol<seunggyu.lee@lge.com>)

    //[S][2012.02.09][jm.lee2] hide Item for DCM (request from DCM operator)
    private String[] DCM_HIDE_ENTRIES = {
            KEY_IMEI_SV
    };
    //[E][2012.02.09][jm.lee2] hide Item for DCM (request from DCM operator)

    //[S][2012.02.17][jm.lee2] Dual SIM status
    private static final String[] DUAL_SIM_HIDE_ENTRIES = {
            KEY_OPERATOR_NAME,
            KEY_SIGNAL_STRENGTH,
            KEY_NETWORK_TYPE,
            KEY_SERVICE_STATE,
            KEY_ROAMING_STATE,
            KEY_DATA_STATE,
            KEY_PHONE_NUMBER,
            KEY_MIN_NUMBER,
            KEY_PRL_VERSION,
            KEY_ESN_NUMBER, //[2012.01.15][jm.lee2] ES4 merge
            KEY_MEID_NUMBER,
            KEY_MEID_HEX, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_MEID_DEC, // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
            KEY_IMEI,
            KEY_IMEI_SV,
            KEY_ICC_ID,
            KEY_WIFI_IP_ADDRESS,
            KEY_SERIAL_NUMBER
    };
    //[E][2012.02.17][jm.lee2] Dual SIM status

    static final String CB_AREA_INFO_RECEIVED_ACTION =
            "android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED";

    static final String GET_LATEST_CB_AREA_INFO_ACTION =
            "android.cellbroadcastreceiver.GET_LATEST_CB_AREA_INFO";

    // Require the sender to have this permission to prevent third-party spoofing.
    static final String CB_AREA_INFO_SENDER_PERMISSION =
            "android.permission.RECEIVE_EMERGENCY_BROADCAST";

    private static final int EVENT_SIGNAL_STRENGTH_CHANGED = 200;
    private static final int EVENT_SERVICE_STATE_CHANGED = 300;
    private static final int EVENT_UPDATE_STATS = 500;

    // [S][2012.09.26][munjohn.kang] applied the VZW requirements(eri_version, life_time_call, warranty_date_code) for About-phone.
    private static final int EVENT_WDC_LOADED = 600;
    private static final int EVENT_LIFETIME_LOADED = 700;

    //[S] 20120513 eddie.kim@lge.com for data life timer
    private static final int EVENT_SIDCH_LOADED = 800;
    private static final int EVENT_LIFETIME_DATA_LOADED = 900;
    //[E] 20120513 eddie.kim@lge.com for data life timer

    private static final int EVENT_MDN_SPR_RTNVALUE = 1200;
    private static final int EVENT_MIN_SPR_RTNVALUE = 1300;
    private static final int EVENT_PRL_SPR_RTNVALUE = 1400;

    private static final int CMD_LIFETIME_DATA = 8000; //LgSvcCmdIds.CMD_LIFETIME_DATA
    private static final int CMD_LIFETIME_DATA_PUT = 8001; //LgSvcCmdIds.CMD_LIFETIME_DATA_PUT

    // ./android/vendor/qcom/proprietary/qcril/qcrilhook_oem/lgrilhook.h
    private final int CMD_CALL_WDC = 1010; // LgSvcCmdIds.CMD_CALL_WDC = 1010
    private final int CMD_CALL_LIFETIMER = 1011; // LgSvcCmdIds.CMD_CALL_LIFETIMER = 1011
    private final int CMD_SERVICE_SCRN = 13; //LgSvcCmdIds.CMD_SERVICE_SCRN
    private final int CMD_MDN_RTN_VALUE = 209;
    private final int CMD_MIN_RTN_VALUE = 9001; //CMD_SPRINT_CDMA_MSID
    private final int CMD_PRL_RTN_VALUE = 9058; //CMD_SPRINT_CDMA_PRL_VERSION

    private static final int CMD_REFURBISH_DATE = 1; //LgSvcCmdIds.CMD_REFURBISH_DATE
    private static final int CMD_REFURBISH_COUNTER = 2; //LgSvcCmdIds.CMD_REFURBISH_COUNTER

    private String mWdc;
    // [E][munjohn.kang]
    // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-05-02 , [VoiceCall][VZW][NV] Retrying to read NV item if it's null is done only for limited counts.
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCountToReadNv = 0;
    // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-05-02 , [VoiceCall][VZW][NV] Retrying to read NV item if it's null is done only for limited counts.

    private TelephonyManager mTelephonyManager;
    private Phone mPhone = null;
    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private Resources mRes;
    private Preference mSignalStrength;
    private Preference mUptime;

    private boolean mShowLatestAreaInfo;

    private String sUnknown = "";

    private Preference mBatteryStatus;
    private Preference mBatteryLevel;
    private IntentFilter mWiFi_Filter;
    private BroadcastReceiver mWiFi_Receiver;
    private IntentFilter mBT_Filter;
    private BroadcastReceiver mBT_Receiver;

    private Handler mHandler;
    /* LGSI_CHANGE_S: IMSI and regioncode
     * 2012-04-04,sharath.gosavi@lge.com,
     * Get the IMSI and region code.
     * "target_country" is replaced with "target_region" to Applicable for all the operators in that region
     * Repository : android/frameworks/base/telephony
     */
    /*LGSI_CHANGE_S:chinese PLMN implementaion
    2011-12-28,manikanta.varmak@lge.com,
    Implentination for chinese language when the language of device is tawin or chinese in manual search*/

    /*LGSI_CHANGE_E:chinese PLMN implementaion*/
    public static final String SYSTEM_PROPERTY_IMSI = "gsm.sim.operator.imsi";
    private static String regioncode = SystemProperties.get("ro.build.target_region");

    /* LGSI_CHANGE_E: IMSI and regioncode */

    // [S][2012.05.07][never1029]MPCS HWversion,channel,sid menu add
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_SID = "sid";
    private static final int ITEM_LEN = 50;
    // [E][2012.05.07][never1029]MPCS HWversion,channel,sid menu add

    // [S][2012.07.13][munjohn.kang] add a Hardware version
    private Preference mRev;
    // [E][munjohn.kang]
    SignalStrength mSignalStrength_LTE;
    private String sNetworkType = "";

    public static int GEMINI_SIM_1 = 0;
    public static int GEMINI_SIM_2 = 1;

    private static final int SERVICE_CATEGORY = 50;

    // CUPSS : show SWOV on settings
    static final int TAPS_TO_SHOW_SWOV = 7;
    int mSWOVHitCountdown;
    Toast mSWOVHitToast;
    private Preference mMainPref;

    public final int mSim1CdmaNetworkType = 0;
    public final int mSim1GsmNetworkType = 1;
    public final int mSim2NetworkType = 2;
    protected int mCtcSlotID = 0;
    protected long mCtcSubscriber = 0;
    protected int mCtcNetworkType = 0;
    protected boolean mIsCtcNetworkInfo = false;
    protected void setCtcNetworkInfo(int mSubscriber, int mNetworkType) {
        mCtcSlotID = mSubscriber;
        mCtcSubscriber = (long)mSubscriber;
        mCtcNetworkType = mNetworkType;
        mIsCtcNetworkInfo = true;
    }

    protected void setSlotIDFromAboutphone(int mSlotID) {
        mCtcSlotID = mSlotID;
    }

    protected boolean getCtcNetworkInfo() {
        return mIsCtcNetworkInfo;
    }

    private BroadcastReceiver mAreaInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CB_AREA_INFO_RECEIVED_ACTION.equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    return;
                }
                
                if (!SystemProperties.getBoolean("persist.sys.cust.cbinfo_not_bar", false)) {
                	CellBroadcastMessage cbMessage = (CellBroadcastMessage)extras.get("message");
                	if (cbMessage != null && cbMessage.getServiceCategory() == SERVICE_CATEGORY) {
                		String latestAreaInfo = cbMessage.getMessageBody();
                		updateAreaInfo(latestAreaInfo);
                	}
                } else {
                	String cbMessage = extras.getString("message");
                	updateAreaInfo(cbMessage);
                }
            }
        }
    };

    private static class MyHandler extends Handler {
        private WeakReference<Status> mStatus;

        public MyHandler(Status activity) {
            mStatus = new WeakReference<Status>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Status status = mStatus.get();
            if (status == null) {
                return;
            }

            // Log.d(LOG_TAG, "MyHandler handleMessage msg.what = " + msg.what);

            switch (msg.what) {
            case EVENT_SIGNAL_STRENGTH_CHANGED:
                status.updateSignalStrength();
                break;

            case EVENT_SERVICE_STATE_CHANGED:
                ServiceState serviceState = status.mPhoneStateReceiver.getServiceState();
                status.updateServiceState(serviceState);
                break;

            case EVENT_UPDATE_STATS:
                status.updateTimes();
                sendEmptyMessageDelayed(EVENT_UPDATE_STATS, 1000);
                break;

            // [S][2012.09.26][munjohn.kang] applied the VZW requirements(eri_version, life_time_call, warranty_date_code) for About-phone.
            case EVENT_WDC_LOADED:
                Log.d(LOG_TAG, "Status EVENT_WDC_LOADED ");
                // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
                if (status.mWdc == null && status.retryCountToReadNv <= status.MAX_RETRY_COUNT) {
                    Log.d(LOG_TAG, "Retry to get Wdc NV item ");
                    status.getWdcNvItem();
                    status.retryCountToReadNv++;
                } else {
                    // [END_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
                    status.onWdcLoaded(status.mWdc);
                }
                break;

            case EVENT_LIFETIME_LOADED:
                Log.d(LOG_TAG, "Status EVENT_LIFETIME_LOADED ");

                if (status.mLifeTimer != null) {
                    try {
                        status.onLifeTimeLoaded(Integer.parseInt(status.mLifeTimer));
                    } catch (NumberFormatException nfe) {
                        status.onLifeTimeLoaded(0);
                    }
                    // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
                } else if (status.retryCountToReadNv <= status.MAX_RETRY_COUNT) {
                    status.mLifeTimer = "0";
                    Log.d(LOG_TAG, "Retry to get lifetimer NV item ");
                    status.getLifetimeCallsNvItem();
                    status.retryCountToReadNv++;
                    // [END_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
                }
                break;

            case EVENT_SIDCH_LOADED:
                status.onCHSIDLoaded((String)Integer.toString(msg.arg1),
                        (String)Integer.toString(msg.arg2));
                break;

            //[S] 20120513 eddie.kim@lge.com for data life timer
            case EVENT_LIFETIME_DATA_LOADED:
                status.onLifeTimeDataLoaded(msg.obj);
                break;
            //[E] 20120513 eddie.kim@lge.com for data life timer

            case EVENT_QUERY_SMSC_DONE:
                String ret = "";
                String temp = "";
                AsyncResult ar = (AsyncResult)msg.obj;
                String mSmsc = (String)ar.result;
                Log.d(LOG_TAG, "mSmsc = " + mSmsc);

                if (mSmsc == null) {
                    status.updateSMSC(null);
                } else {
                    if (mSmsc.contains(",")) {
                        temp = mSmsc.split(",")[0];
                    } else {
                        temp = mSmsc;
                    }
                    char[] char_quote = { '"' };
                    String quote = new String(char_quote);

                    if (temp.contains(quote)) {
                        ret = temp.substring(1, (temp.length() - 1));
                        Log.d(LOG_TAG, "ret = " + ret);
                    }

                    if (ar.exception == null && mSmsc != null) {
                        if (!ret.isEmpty()) {
                            status.updateSMSC(ret);
                        } else if (!temp.isEmpty()) {
                            status.updateSMSC(temp);
                        } else {
                            status.updateSMSC(mSmsc);
                        }
                    } else {
                        status.updateSMSC(null);
                    }
                }
                break;

            case EVENT_MDN_SPR_RTNVALUE:
                Log.d(LOG_TAG, "Status EVENT_MDN_SPR_RTNVALUE ");

                if (status.mMDNbyRTNValue != null) {
                    status.updatePhoneNumber(status.mMDNbyRTNValue);
                }
                break;

            case EVENT_MIN_SPR_RTNVALUE:
                Log.d(LOG_TAG, "Status EVENT_MIN_SPR_RTNVALUE ");

                if (status.mMINbyRTNValue != null) {
                    status.updateMINValue(status.mMINbyRTNValue);
                }
                break;

            case EVENT_PRL_SPR_RTNVALUE:
                Log.d(LOG_TAG, "Status EVENT_PRL_SPR_RTNVALUE ");

                if (status.mPRLbyRTNValue != null) {
                    status.updatePRLValue(status.mPRLbyRTNValue);
                }
                break;

            default:
                break;
            // [E][munjohn.kang]
            }
        }
    }

    private Handler regul_mHandler = new Handler();

    Runnable m_display_run1 = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run1 (4012 , Request .. serial number)");
            try {
                strResponseData = ATClientUtils.atClient_readValue(4012, getApplicationContext(),
                        "hw_version");
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            mRev = (Preference)findPreference(KEY_REV_CHECK);
            if (mRev != null) {
                Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    public void WifiSettings() {
        mWiFi_Filter = new IntentFilter();
        mWiFi_Filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        mWiFi_Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setWifiStatus();
            }
        };
    }

    public void BTSettings() {
        mBT_Filter = new IntentFilter();
        mBT_Filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        mBT_Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setBtStatus();
            }
        };
    }

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "[onReceive] action = " + action);
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                if (mBatteryLevel != null) {
                    mBatteryLevel.setSummary(Utils.getBatteryPercentage(intent));
                }
                if (mBatteryStatus != null) {
                    mBatteryStatus.setSummary(Utils.getBatteryStatus(getResources(), intent));
                }
            }
        }
    };

    private int mRadioTech = -1;
    private int mVoiceTech = -1;
    private PhoneStateListener mPhoneStateListener;
    public PhoneStateListener getPhoneStateListener(long subscription) {
        SLog.i("getPhoneStateListener() subscription = " + subscription);
        PhoneStateListener mMSimPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state) {
                updateDataState();
                updateNetworkType();
            }

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                mSignalStrength_LTE = signalStrength;
                Log.d("starmotor", "signal1");
                updateSignalStrength();
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state != TelephonyManager.CALL_STATE_IDLE) {
                    csActive = true;
                } else {
                    csActive = false;
                }
                Log.d("starmotor", "csActive = " + csActive);
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                if (getCtcNetworkInfo()) {
                    mRadioTech = state.getRilDataRadioTechnology();
                    mVoiceTech = state.getRilVoiceRadioTechnology();
                    SLog.d("onServiceStateChanged state=" + state.getState());
                    SLog.i("mPhone.getPhoneType() = " + mPhone.getPhoneType());
                    SLog.i("mRadioTech = " + mRadioTech);
                    updateServiceState(state);
                }
            }

            @Override
            public void onCellLocationChanged(CellLocation location) {
                if (getCtcNetworkInfo()) {
                    SLog.d("onCellLocationChanged");
                    updateLocation(location);
                }
            }
        };
        return mMSimPhoneStateListener;
    }

    /* LGE_CHANGE_S : TRACFONE_ONS
    ** 2012.01.26, kyemyung.an@lge.com
    ** TRACFONE ONS requirement : Home/Roam
    ** Repository : None */
    private static String operator_code;
    private static String target_country;

    /* LGE_CHANGE_E : TRACFONE_ONS */

    private void onCHSIDLoaded(String CH, String SID) {
        setSummaryText("channel", CH);
        setSummaryText("sid", SID);
    }

    private static String emmc_size;

    // [S][2013.04.10][yongjaeo.lee@lge.com][VZW] Rooting status
    private String getRootedStatus() {
        byte[] flag = new byte[4];
        int result = 0;
        String sNotRooted = mRes.getString(R.string.status_unrooted);
        String sRooted = mRes.getString(R.string.status_rooted);
        DataInputStream in = null;

        try {
            in = new DataInputStream(new FileInputStream("/persist/rct"));
            in.read(flag);

            result = ((flag[3] & 0xFF) << 24) | ((flag[2] & 0xFF) << 16) | ((flag[1] & 0xFF) << 8)
                    | ((flag[0] & 0xFF));

            Log.i(LOG_TAG, "getRootedStatus() , result:" + result);

            if (result == 1000000) {
                if (in != null) {
                    in.close();
                }
                return sNotRooted;
            }

            if (result > 1000000) {
                if (in != null) {
                    in.close();
                }
                return sRooted;
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FineNotFoundException");
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException");
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException");
            }
        }

        return sNotRooted;
    }

    // [E]

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");

        operator_code = Config.getOperator();

        mHandler = new MyHandler(this);
        WifiSettings();
        BTSettings();

        if ("CTC".equals(operator_code) || "CTO".equals(operator_code)) {
            if (getCtcNetworkInfo()) {
                mPhone = OverlayUtils.getMSimPhoneFactoryCtc(
                            getApplicationContext(), mCtcSlotID);
            } else {
                mPhone = OverlayUtils.getPhoneFactory(getApplicationContext());
            }
            if (mPhone == null) {
                SLog.i("CTC mCtcNetworkType mPhone is null");
                mPhone = PhoneFactory.getDefaultPhone();
            }
            SLog.i("CTC mCtcNetworkType = " + mPhone.getPhoneName());
        } else {
            mPhone = PhoneFactory.getDefaultPhone();
            SLog.i("Not CTC mCtcNetworkType = " + mPhone.getPhoneName());
        }

        if (getCtcNetworkInfo()) {
            mCtcNetworkType = mPhone.getPhoneType();
            SLog.i("mCtcNetworkType = " + mCtcNetworkType);
            SLog.i("PhoneConstants.PHONE_TYPE_CDMA = " + PhoneConstants.PHONE_TYPE_CDMA);
            if (mCtcSlotID == GEMINI_SIM_2) {
                addPreferencesFromResource(R.xml.ct_sim2_device_info_status);
            } else if (mCtcNetworkType == PhoneConstants.PHONE_TYPE_CDMA) {
                addPreferencesFromResource(R.xml.ct_sim1_cdma_device_info_status);
            } else {
                addPreferencesFromResource(R.xml.ct_sim1_gsm_device_info_status);
            }

        } else {
            addPreferencesFromResource(R.xml.device_info_status);
        }
        mBatteryStatus = findPreference(KEY_BATTERY_STATUS);
        mBatteryLevel = findPreference(KEY_BATTERY_LEVEL);
        mUptime = findPreference("up_time");

        if (("CTC".equals(operator_code) || "CTO".equals(operator_code)) &&
            (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())) {
            mCtcSubscriber = MSIMUtils.getSubIdBySlotId(mCtcSlotID);
            Log.d(LOG_TAG, "New sub :" + mCtcSubscriber);
            mPhoneStateListener = getPhoneStateListener(mCtcSubscriber);
        } else {
            mPhoneStateListener = getPhoneStateListener(MSIMUtils.getSubIdBySlotId(GEMINI_SIM_1));
        }

        //[S][2012.06.29][never1029@lge.com][LW770]Add Refubish Counter
        String refurbish_Date = null;
        String refurbish_Counter = null;
        Preference mRefurbish;
        Preference mEmmcSize;

        try {
            /*
            refurbish_Date = LgSvcCmd.getCmdValue(LgSvcCmdIds.CMD_GET_REFURBISH_DATE);
            refurbish_Counter = LgSvcCmd.getCmdValue(LgSvcCmdIds.CMD_GET_REFURBISH_COUNTER);
            */
            refurbish_Date = OverlayUtils.LgSvcCmd_getCmdValue(CMD_REFURBISH_DATE);
            refurbish_Counter = OverlayUtils.LgSvcCmd_getCmdValue(CMD_REFURBISH_COUNTER);

            mRefurbish = findPreference("refubish_counter");

            if ((refurbish_Date != null) && (refurbish_Counter != null)) {
                mRefurbish.setSummary(refurbish_Date + "\n"
                        + getResources().getString(R.string.sp_device_info_refurbish_NORMAL)
                        + " = " + refurbish_Counter);
            } else {
                mRefurbish.setSummary("Unavailable");
            }

        } catch (Exception e) {
            Log.w(LOG_TAG, "Exception");
        }
        //[E][2012.06.29][never1029@lge.com][LW770]Add Refubish Counter

        // [S][2012.02.17][jm.lee2] Dual SIM status
        mSMSC = findPreference(KEY_SMSC);
        /*
        Log.d(LOG_TAG, "[onCreate] Utils.isMultiSimEnabled() = " + Utils.isMultiSimEnabled());
        if (!Utils.isMultiSimEnabled()) {
        hideItemForDualSIM();
        dualSIMStatusCommon();

        return;
        }
        */
        // [E][2012.02.17][jm.lee2] Dual SIM status

        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        mRes = getResources();

        sUnknown = mRes.getString(R.string.device_info_default);
        if (SystemProperties.get("ro.build.sbp").equals("1")
                && (SystemProperties.get("persist.radio.cupss.next-root", "/").contains("/cust")
                || SystemProperties.get("persist.sys.cupss.next-root", "/").contains("/cust"))) {
            String mModelName = SystemProperties.get("ro.model.name", null);
            Log.d(LOG_TAG, "mModelName : " + mModelName);
            if (mModelName == null || mModelName.equals("")) {
                setSummaryText(KEY_DEVICE_MODEL, Build.MODEL);
            } else {
                setSummaryText(KEY_DEVICE_MODEL, mModelName);
            }
        } else {
            setSummaryText(KEY_DEVICE_MODEL, Build.MODEL);
        }

        Log.d(LOG_TAG, "[onCreate] sUnknown= " + sUnknown);

        // CUPSS : show SWOV on Settings
        if (SystemProperties.get("ro.build.sbp").equals("1")) {
            findPreference(KEY_IMEI_SV).setEnabled(true);
        }
        //[s][2013.02.05][yongjaeo.lee@lge.com] for runtime_error
        /*
        if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
            mPhone = PhoneFactory.getDefaultPhone();
        }
        */

        // imsi
        //mPhone = PhoneFactory.getDefaultPhone();
        //[E][2013.02.05][yongjaeo.lee@lge.com]

        /* LGE_CHANGE_S : TRACFONE_ONS
        ** 2012.01.26, kyemyung.an@lge.com
        ** TRACFONE ONS requirement : Home/Roam
        ** Repository : None */
        target_country = Config.getCountry();
        /* LGE_CHANGE_E : TRACFONE_ONS */

        if ("true".equals(SystemProperties.get("ro.lge.one_binary_16G_32G"))
                || "true".equals(SystemProperties.get("ro.lge.one_binary_support"))) {
            emmc_size = SystemProperties.get("persist.sys.emmc_size");
            mEmmcSize = findPreference("memory_size");
            mEmmcSize.setSummary(emmc_size);
        } else {
            removePreferenceFromScreen("memory_size");
        }

        if (Build.DEVICE.equals("altev") 
            || ("VZW".equals(operator_code) && Utils.supportSplitView(getApplicationContext()))) {
            //            removePreferenceFromScreen("ims_registration_status");
            //            removePreferenceFromScreen("number");
            removePreferenceFromScreen("life_time_call");
            removePreferenceFromScreen("warranty_date_code");
        }

        // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
        if ("VZW".equals(operator_code)) {
            getSvcCmdInstants();
        }
        // [END_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).

        // Note - missing in zaku build, be careful later...
        mSignalStrength = findPreference(KEY_SIGNAL_STRENGTH);
        if (false == "MPCS".equals(operator_code)
                && (false == getCtcNetworkInfo())) {
            removePreferenceFromScreen(KEY_CHANNEL);
            removePreferenceFromScreen(KEY_SID);
        }

        // [S][2012.03.05][seungeene][Common][QE_139002] Remove signal strength for DCM operator
        // [2014.11.03]Add SBM operator
        if ("DCM".equals(operator_code) || "SBM".equals(operator_code)) {
            removePreferenceFromScreen(KEY_SIGNAL_STRENGTH);
            mSignalStrength = null;
        }
        // [E][2012.03.05][seungeene][Common][QE_139002] Remove signal strength for DCM operator

        if ("VZW".equals(operator_code) || "LRA".equals(operator_code)) {
            removePreferenceFromScreen(KEY_SIGNAL_STRENGTH);
            removePreferenceFromScreen(KEY_NETWORK_TYPE);
        } else {
            removePreferenceFromScreen(KEY_NETWORK_TYPE_STRENGTH);
        }
        // LRA operator Removed lifetimecall and wdc - request by bangsoora
        if (!"VZW".equals(operator_code) && !Config.CRK.equals(Config.getOperator()))
        {
            removePreferenceFromScreen(KEY_LIFETIME_CALL);
        }

        if (!"VZW".equals(operator_code) && !Config.CRK.equals(Config.getOperator()))
        {
            removePreferenceFromScreen(KEY_WDC);
        }

        if (!Config.CRK.equals(Config.getOperator()))
        {
            removePreferenceFromScreen(KEY_LIFETIME_DATA);
        }

        if ("LG-MS790".equals(SystemProperties.get("ro.product.model")))
        {
            // support technology
            setSummaryText(KEY_SUPPORT_TECHNOLOGY, SUPPORT_TECHNOLOGY);
        }
        else
        {
            removePreferenceFromScreen(KEY_SUPPORT_TECHNOLOGY);
        }

        if (!Config.CRK.equals(Config.getOperator())) {
            removePreferenceFromScreen("refubish_counter");
        }

        if (Config.SPR.equals(Config.getOperator())
                && !"".equals(SystemProperties.get("ro.cdma.home.operator.alpha"))) {
            setSummaryText("sprint_brand", SystemProperties.get("ro.cdma.home.operator.alpha"));
        } else {
            removePreferenceFromScreen("sprint_brand");
        }

        hideItemForOperator(); // [2012.02.06][jm.lee2] hide Item for operator.
        if ("MPCS".equals(operator_code) && mPhone.getPhoneName().equals("CDMA")) {
            getSIDCH();
        }

        Log.d(LOG_TAG, "mPhone.getPhoneName() = " + mPhone.getPhoneName());
        Log.d(LOG_TAG, "mPhone.getLteOnCdmaMode() = " + mPhone.getLteOnCdmaMode()
                + ", PhoneConstants.LTE_ON_CDMA_TRUE = " + PhoneConstants.LTE_ON_CDMA_TRUE);

        if (mPhone == null || Utils.isWifiOnly(getApplicationContext())) {
            for (String key : PHONE_RELATED_ENTRIES) {
                removePreferenceFromScreen(key);
            }
        } else {
            // [S][2012.01.15][jm.lee2] ES4 merge
            if ((SystemProperties.getBoolean("ro.config.multimode_cdma", false))
                    || (mPhone.getPhoneName().equals("CDMA"))
                    || (mCtcSlotID == GEMINI_SIM_1 && true == getCtcNetworkInfo())) {
                if ("CTC".equals(operator_code) || "CTO".equals(operator_code)) {
                    setSummaryText(KEY_PRL_VERSION, LGUiccManager.getProperty("PrlVersion", 0, sUnknown));
                } else {
                    String cdmaPrlValue = mPhone.getCdmaPrlVersion();
                    setSummaryText(KEY_PRL_VERSION, mPhone.getCdmaPrlVersion());

                    if ("SPR".equals(operator_code)) {
                        if (cdmaPrlValue == null || "".equals(cdmaPrlValue)) {
                            getPRLByRTNValueNvItem();
                        }
                    }
                }
            } else {
                // device does not support CDMA, do not display PRL
                removePreferenceFromScreen(KEY_PRL_VERSION);
            }
            // [E][2012.01.15][jm.lee2] ES4 merge

            if ("VZW".equals(operator_code)) {
                setSummaryText(KEY_LAST_FACTORY_DATA_RESET, ""
                        + getDateString(getLastFactoryResetTime()) + " "
                        + getTimeString(getLastFactoryResetTime()));
                removePreferenceFromScreen(KEY_ROOTING_STATUS);
                //                setSummaryText(KEY_ROOTING_STATUS, getRootedStatus() );
            } else {
                removePreferenceFromScreen(KEY_LAST_FACTORY_DATA_RESET);
                removePreferenceFromScreen(KEY_ROOTING_STATUS);
            }

            // NOTE "imei" is the "Device ID" since it represents
            // the IMEI in GSM and the MEID in CDMA
            if (mPhone.getPhoneName().equals("CDMA")) {
                //                Log.d(LOG_TAG, "OverlayUtils.get_device_IMEI()() = " + OverlayUtils.get_device_IMEI());
                if ("VZW".equals(operator_code) || "MPCS".equals(operator_code)) {
                    removePreferenceFromScreen(KEY_ESN_NUMBER);
                    if (mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                        removePreferenceFromScreen(KEY_MEID_NUMBER);
                    } else {
                        if ("VZW".equals(operator_code)) {
                            setSummaryText(KEY_MEID_NUMBER, getVzwMeid());
                        } else {
                            setSummaryText(KEY_MEID_NUMBER, mPhone.getMeid());
                        }
                    }
                } else {
                    //                    setSummaryText(KEY_ESN_NUMBER, mPhone.getEsn());
                    if ("LRA".equals(operator_code)) {
                        removePreferenceFromScreen(KEY_MEID_NUMBER);
                        removePreferenceFromScreen(KEY_ESN_NUMBER);
                    } else {
                        setSummaryText(KEY_ESN_NUMBER, mPhone.getEsn());
                        setSummaryText(KEY_MEID_NUMBER, mPhone.getMeid());
                    }
                }

                // [2012.06.28][munjohn.kang] applied to ACG
                // [S][2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator    //[2012.05.16][munjohn.kang] applied to LGL86C(TRF)
                if (Config.getOperator().equals(Config.USC)
                        || Config.getOperator().equals(Config.TRF) || "ACG".equals(operator_code)) {
                    removePreferenceFromScreen(KEY_MEID_NUMBER);
                    setSummaryText(KEY_MEID_HEX, mPhone.getMeid());

                    {
                        String mMEID;
                        long l_MEID_DEC;
                        String mMEID_DEC;
                        String sTemp = "";
                        String sTemp2 = "";

                        mMEID = mPhone.getMeid();
                        if (TextUtils.isEmpty(mMEID)) {
                            mMEID_DEC = mRes.getString(R.string.radioInfo_unknown);
                        } else {
                            String str_high = mMEID.substring(0, 8);
                            String str_low = mMEID.substring(8, 14);
                            long l_high = Long.parseLong(str_high, 16);
                            long l_low = Long.parseLong(str_low, 16);
                            l_MEID_DEC = l_high * 100000000 + l_low;
                            mMEID_DEC = Long.toString(l_MEID_DEC);
                            int dec_len = mMEID_DEC.length();

                            sTemp = sTemp.concat("000000000000000000");
                            sTemp = sTemp.concat(mMEID_DEC);
                            sTemp2 = sTemp.substring(dec_len);
                        }

                        // [S][2012.04.09][youngmin.jeon][US730] Extend MEID DEC value to 18 digits.
                        if (mMEID_DEC.equals("0")) {
                            setSummaryText(KEY_MEID_DEC, "000000000000000000");
                        } else {
                            setSummaryText(KEY_MEID_DEC, sTemp2);
                        }
                        // [E][2012.04.09][youngmin.jeon][US730] Extend MEID DEC value to 18 digits.
                    }
                }
                //[S][2012.06.16][munjohn.kang] modified the MEID display for LG-LG730 SPR(BM)
                else if (Config.getOperator().equals(Config.SPR)
                        || Config.getOperator().equals(Config.BM)) {
                    removePreferenceFromScreen(KEY_MEID_NUMBER);
                    setSummaryText(KEY_MEID_HEX, mPhone.getMeid());
                    {
                        String mMEID;
                        long l_MEID_DEC;
                        String mMEID_DEC;

                        mMEID = mPhone.getMeid();
                        if (DBG) {
                            Log.d(LOG_TAG, "mMEID:" + mMEID);
                        }
                        if (!TextUtils.isEmpty(mMEID) && mMEID.length() == 14) {
                            String str_high = mMEID.substring(0, 8);
                            String str_low = mMEID.substring(8, 14);
                            long l_high = Long.parseLong(str_high, 16);
                            long l_low = Long.parseLong(str_low, 16);
                            l_MEID_DEC = l_high * 100000000 + l_low;
                            mMEID_DEC = Long.toString(l_MEID_DEC);

                            //Log.d(LOG_TAG, "mMEID_DEC:" + mMEID_DEC);

                            String mMEID_DEC_SPR;
                            String sTemp = "";
                            String sTemp2 = "";
                            int dec_len = mMEID_DEC.length();

                            sTemp = sTemp.concat("000000000000000000");
                            sTemp = sTemp.concat(mMEID_DEC);

                            sTemp2 = sTemp.substring(dec_len);

                            mMEID_DEC_SPR = sTemp2.substring(0, 0 + 3) + "-"
                                    + sTemp2.substring(3, 3 + 3) + "-"
                                    + sTemp2.substring(6, 6 + 3) + "-"
                                    + sTemp2.substring(9, 9 + 3) + "-"
                                    + sTemp2.substring(12, 12 + 3) + "-"
                                    + sTemp2.substring(15);

                            setSummaryText(KEY_MEID_DEC, mMEID_DEC_SPR);
                        } else {
                            setSummaryText(KEY_MEID_DEC, mRes.getString(R.string.radioInfo_unknown));
                        }
                    }
                }
                // [E][munjohn.kang]
                else {
                    removePreferenceFromScreen(KEY_MEID_HEX);
                    removePreferenceFromScreen(KEY_MEID_DEC);
                }
                // [E][2012.04.03][youngmin.jeon][US730] This menu item is only
                // for US Cellular Operator
                if ("VZW".equals(operator_code)) {
                    removePreferenceFromScreen(KEY_MIN_NUMBER);
                } else {
                    String cdmaMin = mPhone.getCdmaMin();
                    if (DBG) {
                        Log.d(LOG_TAG, " mPhone.getCdmaMin() : " + mPhone.getCdmaMin());
                    }

                    setSummaryText(KEY_MIN_NUMBER, mPhone.getCdmaMin());

                    if ("SPR".equals(operator_code)) {
                        if (cdmaMin == null || "".equals(cdmaMin)) {
                            getMINByRTNValueNvItem();
                        }
                    }
                    if (findPreference(KEY_MIN_NUMBER) != null) {
                        if (getResources().getBoolean(R.bool.config_msid_enable)) {
                            findPreference(KEY_MIN_NUMBER).setTitle(R.string.status_msid_number);
                        }
                    }
                }
                // [2012.01.15][jm.lee2] ES4 merge
                // setSummaryText(KEY_PRL_VERSION, mPhone.getCdmaPrlVersion());
                removePreferenceFromScreen(KEY_IMEI_SV);
                //Log.d(LOG_TAG, "getEriVersion() = " + Integer.toString(mPhone.getEriFileVersion()));
                if ("VZW".equals(operator_code)) {
                    // imsi block ; for build
                    setSummaryText(KEY_ERI_VERSION, Integer.toString(mPhone.getEriFileVersion()));
                    //Log.d(LOG_TAG, "KEY_IMEI_SVN = " + mPhone.getDeviceSvn());
                    if (mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                        findPreference(KEY_IMEI_SVN).setTitle("IMEISV");
                        setSummaryText(KEY_IMEI_SVN, mPhone.getDeviceSvn());
                    } else {
                        removePreferenceFromScreen(KEY_IMEI_SVN);
                    }
                    removePreferenceFromScreen(KEY_IMEI_SVN_GSM);
                } else if ("MPCS".equals(operator_code)) {
                    // imsi block ; for build
                    setSummaryText(KEY_ERI_VERSION, Integer.toString(mPhone.getEriFileVersion()));
                    removePreferenceFromScreen(KEY_IMEI_SVN);
                    removePreferenceFromScreen(KEY_IMEI_SVN_GSM);
                } else {
                    removePreferenceFromScreen(KEY_ERI_VERSION);
                    removePreferenceFromScreen(KEY_IMEI_SVN);
                    removePreferenceFromScreen(KEY_IMEI_SVN_GSM);
                }

                if (mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) { // Show ICC ID and IMEI for LTE device
                    // IMEI
                    //                  String mImei = OverlayUtils.get_device_IMEI();
                    String mImei = mPhone.getDeviceId();
                    //                    Log.d(LOG_TAG, "IMEI = " + mImei);

                    if (operator_code.equals("VZW")) {
                        //                        setSummaryText(KEY_IMEI, mPhone.getImei());
                        setSummaryText(KEY_IMEI, getVzwImei());
                    } else if (operator_code.equals("MPCS") || operator_code.equals("USC")
                            || operator_code.equals("ACG")) {
                        if (mImei != null && !mImei.equals("")) {
                            Log.d(LOG_TAG, "mImei.length() = " + mImei.length());
                            if (mImei.length() > 1 && mImei.length() == 15) {
                                setSummaryText(KEY_IMEI, mImei.substring(0, mImei.length() - 1));
                            } else {
                                setSummaryText(KEY_IMEI, mImei);
                            }
                        } else {
                            setSummaryText(KEY_IMEI, mImei);
                        }
                    } else if ("LRA".equals(operator_code)) { // || operator_code.equals("VZW")) {
                        setSummaryText(KEY_IMEI, mImei);
                    } else {
                        if ("CTC".equals(operator_code) || "CTO".equals(operator_code)) {
                            removePreferenceFromScreen(KEY_IMEI);
                        } else {
                            setSummaryText(KEY_IMEI, mPhone.getImei());
                        }
                    }

                    // ICC_ID
                    if (DBG) {
                        Log.d(LOG_TAG, "getIccSerialNumber = " + mPhone.getIccSerialNumber());
                    }
                    if (operator_code.equals("VZW")) {
                        setSummaryText(KEY_ICC_ID, getVzwIccId());
                    } else {
                        setSummaryText(KEY_ICC_ID, mPhone.getIccSerialNumber());
                    }
                } else {
                    // device is not GSM/UMTS, do not display GSM/UMTS features
                    // check Null in case no specified preference in overlay xml
                    removePreferenceFromScreen(KEY_IMEI);
                    removePreferenceFromScreen(KEY_ICC_ID);
                }
            } else { //if (mPhone.getPhoneName().equals("CDMA"))
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    String simIMEI = "";
                    simIMEI = MSIMUtils
                            .get_device_ID(getApplicationContext(), mTelephonyManager);
                    setSummaryText(KEY_IMEI, simIMEI);
                } else {
                    /*
                    String mImei = mPhone.getDeviceId();
                    if ("VZW".equals(operator_code)) {
                       if (mImei != null && !mImei.equals("")) {
                            Log.d(LOG_TAG, "mImei.length() = " + mImei.length());
                            if (mImei.length() > 1) {
                                setSummaryText(KEY_IMEI, mImei.substring(0, mImei.length() - 1));
                            } else {
                                setSummaryText(KEY_IMEI, mImei);
                            }
                        } else {
                            setSummaryText(KEY_IMEI, mImei);
                        }

                    //                        setSummaryText(KEY_IMEI, OverlayUtils.get_device_IMEI());
                    } else {
                    */
                    if ("VZW".equals(operator_code)) {
                        //                        setSummaryText(KEY_IMEI, mPhone.getImei());
                        setSummaryText(KEY_IMEI, getVzwImei());
                    } else {
                        setSummaryText(KEY_IMEI, mPhone.getDeviceId());
                    }
                    //}
                }

                if ("VZW".equals(operator_code) || "SPR".equals(operator_code)
                        || ("UNF".equals(operator_code) && "MX".equals(target_country))
                        || "MPCS".equals(operator_code) || "USC".equals(operator_code)
                        || "ACG".equals(operator_code))
                {
                    if (operator_code.equals("VZW")) {
                        setSummaryText(KEY_ICC_ID, getVzwIccId());
                    } else {
                        setSummaryText(KEY_ICC_ID, mPhone.getIccSerialNumber());
                    }
                }
                else
                {
                    removePreferenceFromScreen(KEY_ICC_ID);
                }

                if ("u2_vdf_com".equals(SystemProperties.get("ro.product.name")))
                { // [2012.08.14][Settings][never1029] U2 VDF IMEI_SV :00 -> changed IMEI_SV : IMEI + IMEI_SV  [U2 VDF operator req]
                    setSummaryText(KEY_IMEI_SV,
                            mPhone.getDeviceId() + mTelephonyManager.getDeviceSoftwareVersion());
                }
                else
                {
                    if ("KR".equals(Config.getCountry()))
                    {
                        removePreferenceFromScreen(KEY_IMEI_SV);
                    }
                    else
                    {
                        if ("VZW".equals(operator_code)) {
                            findPreference(KEY_IMEI_SV).setTitle("IMEISV");
                        }
                        setSummaryText(KEY_IMEI_SV, mTelephonyManager.getDeviceSoftwareVersion());
                    }
                }

                // device is not CDMA, do not display CDMA features
                // check Null in case no specified preference in overlay xml
                // [2012.01.15][jm.lee2] ES4 merge
                removePreferenceFromScreen(KEY_ESN_NUMBER);
                removePreferenceFromScreen(KEY_MEID_NUMBER);
                if (Config.getOperator().equals(Config.SPR)
                        || Config.getOperator().equals(Config.USC)) {
                    String mImei = mPhone.getImei();
                    if (mImei != null && !mImei.equals("")) {
                        Log.d(LOG_TAG, "mImei.length() = " + mImei.length());
                        if (mImei.length() > 1 && mImei.length() == 15) {
                            setSummaryText(KEY_MEID_HEX, mImei.substring(0, mImei.length() - 1));
                        } else {
                            setSummaryText(KEY_MEID_HEX, mImei);
                        }
                    } else {
                        setSummaryText(KEY_MEID_HEX, mImei);
                    }

                    setSummaryText(KEY_MEID_DEC, getUSC_MEID_DEC(mPhone.getImei()));
                } else {
                    removePreferenceFromScreen(KEY_MEID_HEX); // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
                    removePreferenceFromScreen(KEY_MEID_DEC); // [2012.04.03][youngmin.jeon][US730] This menu item is only for US Cellular Operator
                }
                removePreferenceFromScreen(KEY_MIN_NUMBER);
                //                removePreferenceFromScreen(KEY_ICC_ID);
                removePreferenceFromScreen(KEY_ERI_VERSION); //[2012.10.04][munjohn.kang]
                removePreferenceFromScreen(KEY_IMEI_SVN);
                removePreferenceFromScreen(KEY_IMEI_SVN_GSM);

                // only show area info when SIM country is Brazil 
                if ("br".equals(mTelephonyManager.getSimCountryIso())) {
                    mShowLatestAreaInfo = true;
                }

                // if ("VZW".equals(operator_code)) {
                // setSummaryText(KEY_IMEI_SVN_GSM, mPhone.getDeviceSvn());
                // } else {
                // removePreferenceFromScreen(KEY_IMEI_SVN_GSM);
                // }
            }//else {//if (mPhone.getPhoneName().equals("CDMA"))

            // [S][2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber
            String sim_phone_number = MSIMUtils.get_SIM_phone_number(getApplicationContext());

            /* LGSI_CHANGE_S: TD 253437
            * 2012-11-19,sheik.ahmed@lge.com,
            * Proper NULL check comparison for the sim_phone_number
            * Repository : android\vendor\lge\apps\Settings3_2
            */
            //            if (sim_phone_number.equals("NULL")) {
            //Log.i(LOG_TAG, "sim_phone_number:" + sim_phone_number);
            if ("NULL".equals(sim_phone_number)) {
                /* LGSI_CHANGE_E: TD 253437 */

                //                Log.i(LOG_TAG, "mPhone.getLine1Number(): " + mPhone.getLine1Number());

                String rawNumber = null; // may be null or empty

                if (!((Utils.isTablet()) && "SPR".equals(operator_code))) {
                    rawNumber = mPhone.getLine1Number();
                }
                Log.i(LOG_TAG, "rawNumber :" + rawNumber);
                if (rawNumber == null && "SPR".equals(operator_code)) {
                    getMDNByRTNValueNvItem();
                }

                if ("KR".equals(Config.getCountry())) {
                    if ((!TextUtils.isEmpty(rawNumber)) && (rawNumber.indexOf("+82") == 0)) {
                        rawNumber = "0" + rawNumber.substring(3);
                    }
                }

                IccCard card = mPhone.getIccCard();
                //IccRecords record = null;

                String formattedNumber = null;
                String mMcc = null;
                String mIsoCountry = null;
                //String mMccMnc = mPhone.getSubscriberId();

                Log.d("starmotor", "card = " + card);
                Log.d("starmotor", "card.getState() = " + card.getState());
                if (card != null &&
                        (card.getState() == IccCardConstants.State.READY)) {
                    //                    mMcc = mMccMnc.substring(0, 3);
                    mMcc = SystemProperties.get("gsm.sim.operator.iso-country");
                    if (mMcc != null) {
                        mIsoCountry = mMcc.toUpperCase();
                    }
                    //                      Log.d("starmotor" , "mMccMnc = " + mMccMnc);
                    //                      Log.d("starmotor" , "mMcc = " + mMcc);
                    Log.d("starmotor", "mIsoCountry = " + mIsoCountry);
                }
                if (!TextUtils.isEmpty(rawNumber)) {
                    // formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);

                    Log.d("starmotor", "PhoneNumberFormatter.getCurrentCountryIso(this) = "
                            + PhoneNumberFormatter.getCurrentCountryIso(this));
                    //                    if ("450".equals(mMcc)) {
                    //                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber, "KR");
                    //                    } else {
                    if ("VZW".equals(operator_code)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber, "US");
                    } else {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber,
                                PhoneNumberFormatter.getCurrentCountryIso(this));
                    }
                    Log.d("starmotor", "formattedNumber");
                    if (TextUtils.isEmpty(formattedNumber)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber, mIsoCountry);
                        Log.d("starmotor", "formattedNumber IN1");
                        if (TextUtils.isEmpty(formattedNumber)) {
                            Log.d("starmotor", "formattedNumber IN2");
                            formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
                        }
                    }
                    //                    }
                }

                // [S][2013.04.10][yongjaeo.lee@lge.com][U2] request Revert.
                /* request Revert : U2 2013
                String strNewCo = SystemProperties.get("ro.build.target_operator_ext");

                if (SystemProperties.get("ro.build.target_operator").equals("TMO") && "MPCS_TMO".equals(strNewCo)) {
                    if (formattedNumber != null && formattedNumber.length() > 2 && formattedNumber.startsWith("1-")) {
                        formattedNumber = formattedNumber.substring(2);
                    }
                }
                */
                // [E]

                Log.i(LOG_TAG, "gsm.sim.state:" + SystemProperties.get("gsm.sim.state"));

                if ("DCM".equals(operator_code)
                        && SystemProperties.get("gsm.sim.state").equals("PERM_DISABLED")) {
                    setSummaryText(KEY_PHONE_NUMBER, mRes.getString(R.string.sp_dcm_puk_lock));
                } else {
                    setSummaryText(KEY_PHONE_NUMBER, formattedNumber); //If formattedNumber is null or empty, it'll display as "Unknown".
                }
            } else {
                Log.i(LOG_TAG, "onCreate (), sim phone number read is Success !!");
                setSummaryText(KEY_PHONE_NUMBER, sim_phone_number);
            }
            // [E][2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber

            // SPR operator requirement. Tablet doesn't need the phone number. [E7]
            mPhoneStateReceiver = new PhoneStateIntentReceiver(this, mHandler);
            mPhoneStateReceiver.notifySignalStrength(EVENT_SIGNAL_STRENGTH_CHANGED);
            if (false == getCtcNetworkInfo()) {
                mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
            }
            if (!mShowLatestAreaInfo) {
                removePreferenceFromScreen(KEY_LATEST_AREA_INFO);
            }

        }

        /*
                if ("UNF".equals(operator_code) && "MX".equals(target_country)) {
                    mPhone.getSmscAddress(mHandler.obtainMessage(EVENT_QUERY_SMSC_DONE));
                    setSummaryText(KEY_IMSI , SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_IMSI , ""));
                } else
        */

        if (getCtcNetworkInfo()) {
            updateMccMncForCtc(mPhone.getServiceState());
            mPhone.getSmscAddress(mHandler.obtainMessage(EVENT_QUERY_SMSC_DONE));
        }
        else if ("SPR".equals(operator_code)
                || ("UNF".equals(operator_code) && "MX".equals(target_country))) {
            // [S][2013.06.26][yongjaeo.lee@lge.com][SPR] get value of UICC IMSI ; add null pointer exception handling
            // String uicc_imsi = mPhone.getIccCard().getIccRecords().getIMSI(); // block ; null pointer exception, in case CDMA searching
            String uicc_imsi = "";
            IccCard card = mPhone.getIccCard();
            IccRecords record = null;

            if (card != null && (card.getState() != IccCardConstants.State.ABSENT) /* Oma-lock*/) {
                record = card.getIccRecords();

                if (record != null) {
                    uicc_imsi = record.getIMSI();
                    // Log.i(LOG_TAG, "uicc_imsi:" + uicc_imsi);
                } else {
                    Log.e(LOG_TAG, "iccRecord is null !!");
                }
            } else {
                Log.e(LOG_TAG, "icccard is null !!");
            }
            // [E]

            if (uicc_imsi == null || uicc_imsi.length() == 0) {
                setSummaryText(KEY_IMSI, sUnknown);
            } else {
                setSummaryText(KEY_IMSI, uicc_imsi);
            }

            if ("UNF".equals(operator_code) && "MX".equals(target_country)) {
                mPhone.getSmscAddress(mHandler.obtainMessage(EVENT_QUERY_SMSC_DONE));
            } else {
                removePreferenceFromScreen(KEY_SMSC);
            }
        } else {
            removePreferenceFromScreen(KEY_IMSI);
            removePreferenceFromScreen(KEY_SMSC);
        }

        if ("ACG".equals(operator_code)) {
            removePreferenceFromScreen(KEY_ESN_NUMBER);
        }
        setWimaxStatus();
        setWifiStatus();
        setBtStatus();
        if ("MPCS".equals(operator_code) || Config.CRK.equals(Config.getOperator()))
        {
            putDataLifeTime();
        }

        //String serial = Build.SERIAL;
        /*
        if (serial != null && !serial.equals("")) {
            setSummaryText(KEY_SERIAL_NUMBER, serial);
        } else {
            removePreferenceFromScreen(KEY_SERIAL_NUMBER);
        }
        */

        // [S][2012.07.17][Serial number removed only KR
        if (!"KR".equals(Config.getCountry())) {
            removePreferenceFromScreen(KEY_SERIAL_NUMBER);
        }
        // [S][2012.07.17][Serial number removed only KR

        // embedded battery model ( regulatory & factory date & SN )
        if (Utils.isEmbededBattery(this)) {
            if ("KR".equals(Config.getCountry())) {
                // embedded battery model ( regulatory & factory date & SN )
            } else {
                removePreferenceFromScreen(KEY_MANUFACTURE_SN);
                removePreferenceFromScreen(KEY_MANUFACTURE_C_C);
                removePreferenceFromScreen(KEY_MANUFACTURE_DATE);
            }
        } else {
            removePreferenceFromScreen(KEY_MANUFACTURE_SN);
            removePreferenceFromScreen(KEY_MANUFACTURE_C_C);
            removePreferenceFromScreen(KEY_MANUFACTURE_DATE);
        }

        if ("VZW".equals(operator_code)) {
            setImsRegistrationStatus();

        } else {
            removePreferenceFromScreen(KEY_IMS_REGISTRATION_STATUS);
        }

        // H/W Version
        if ("TCL".equals(operator_code) && "MX".equals(target_country)) {
            // operator requirement - MEX Telcel. It is exist embededBattery model.
            removePreferenceFromScreen(KEY_REV_CHECK);
        } else if ("VZW".equals(operator_code) || "MPCS".equals(operator_code)
                || "SPR".equals(operator_code) || "BM".equals(operator_code)
                || Utils.isEmbededBattery(this) || "USC".equals(operator_code)
                || ("UNF".equals(operator_code) && "MX".equals(target_country))
                || "CMCC".equals(operator_code) || "CTC".equals(operator_code)
                || "CTO".equals(operator_code)
                || "CUCC".equals(operator_code) || ("CMO".equals(operator_code))
                || ("OPEN".equals(operator_code) && (Config.getCountry()).equals("CN"))) {
            if (("VZW".equals(operator_code)) || ("SPR".equals(operator_code))
                    || "USC".equals(operator_code) || "ATT".equals(operator_code)
                    || ("CMCC".equals(operator_code)) || "MPCS".equals(operator_code)
                    || "CTC".equals(operator_code) || ("CMO".equals(operator_code))
                    || "CTO".equals(operator_code)
                    || "CUCC".equals(operator_code) || "KR".equals(Config.getCountry())
                    || ("OPEN".equals(operator_code) && (Config.getCountry()).equals("CN"))) {
                regul_mHandler.postDelayed(m_display_run1, 150);
                ATClientUtils.atClient_BindService(getApplicationContext());
            } else {
                //                if ("SPR".equals(operator_code)) {
                //                    HardwareRoVersion();
                //                } else {
                HardwareVersion();
                //                }
            }
        } else {
            removePreferenceFromScreen(KEY_REV_CHECK);
        }

        /*
                if ("SPR".equals(operator_code)) {
                    regul_mHandler.postDelayed(m_display_run1, 150);
                    ATClientUtils.atClient_BindService(getApplicationContext());
                } else {
                    if ("TCL".equals(Config.getOperator())) //[never1029][TCL operator req.hardware version list removed]
                    {
                        removePreferenceFromScreen(KEY_REV_CHECK);
                    }
                    else
                    {
                        HardwareVersion();
                    }
                }
        */
        //[S] taewon.hwang@lge.com 2012.11.26: Remove Battery use menu because #helphelp* is not support battery use.
        if ("VZW".equals(operator_code)) {
            removePreferenceFromScreen(KEY_BATTERY_USE);
            removePreferenceFromScreen(KEY_BATTERY_CONDITION); //taewon.hwang@lge.com 2013.01.07: Remove Battery condition menu because #helphelp* is not support battery condition.
            removePreferenceFromScreen(KEY_CSN);
            removePreferenceFromScreen(KEY_LGU_SN);

        }
        //[E] taewon.hwang@lge.com 2012.11.26: Remove Battery use menu because #helphelp* is not support battery use.

        if (Utils.isEmbededBattery(this) && "DCM".equals(operator_code)) {
            findPreference(KEY_IMEI).setEnabled(true);
        }
    }

    // CUPSS : show SWOV on settings
    private String getMainSWVersion() {
        String swversion;

        // format: V10a-OP1-HQ

        String version = SystemProperties.get("lge.version.factorysw", "");
        if (TextUtils.isEmpty(version)) {
            // For G and newer models
            version = SystemProperties.get("ro.lge.factoryversion", "Unknown");
        }

        String[] factoryVersion = version.split("-");
        if (factoryVersion.length < 8) {
            return factoryVersion[0];
        }

        swversion =
        factoryVersion[2].substring(0, factoryVersion[2].length()) + "-"; // swversion

        //Make SWOV with CUPSS info.
        String cupssGroup = SystemProperties.get("ro.lge.cupssgroup", "");
        String cupssRootDir = SystemProperties.get("ro.lge.rootdir", "");
        if (!TextUtils.isEmpty(cupssGroup)) {
            swversion += cupssGroup;
        } else if (!TextUtils.isEmpty(cupssRootDir) && cupssRootDir.equals("/cust")) {
            //OPEN Version for single cupss
            String cupssOpenSW = SystemProperties.get("ro.lge.opensw", "");
            if (!TextUtils.isEmpty(cupssOpenSW)) {
                swversion += cupssOpenSW;
            } else {
                swversion += factoryVersion[3] + "-" + factoryVersion[4];
            }
        } else {
            swversion += factoryVersion[3] + "-" + factoryVersion[4];
        }
        return swversion;
    }

    private String getVzwMeid() {
        String mMeid;
        String mMeid_VZW;

        mMeid = mPhone.getMeid();

        if (DBG) {
            Log.d(LOG_TAG, "mMeid:" + mMeid);
        }

        if (!TextUtils.isEmpty(mMeid) && mMeid.length() == 14) {
            Log.d(LOG_TAG, "mMeid.length() = " + mMeid.length());
            mMeid_VZW = mMeid.substring(0, 4) + " "
                    + mMeid.substring(4, 8) + " "
                    + mMeid.substring(8, 12) + " "
                    + mMeid.substring(12, 13)
                    + mMeid.substring(13);
        } else {
            mMeid_VZW = mMeid;
        }

        return mMeid_VZW;
    }

    private String getVzwImei() {
        String mImei;
        String mImei_VZW;

        mImei = mPhone.getImei();

        if (DBG) {
            Log.d(LOG_TAG, "mImei:" + mImei);
        }

        if (!TextUtils.isEmpty(mImei) && mImei.length() == 15) {
            Log.d(LOG_TAG, "mImei.length() = " + mImei.length());
            mImei_VZW = mImei.substring(0, 4) + " "
                    + mImei.substring(4, 8) + " "
                    + mImei.substring(8, 12) + " "
                    + mImei.substring(12, 14)
                    + mImei.substring(14);
        } else {
            mImei_VZW = mImei;
        }

        return mImei_VZW;
    }

    private String getVzwIccId() {
        String mIccId;
        String mIccId_VZW;
        mIccId = mPhone.getIccSerialNumber();

        if (DBG) {
            Log.d(LOG_TAG, "mIccId:" + mIccId);
        }

        if (!TextUtils.isEmpty(mIccId) && mIccId.length() >= 17) {
            Log.d(LOG_TAG, "mIccId.length() = " + mIccId.length());
            mIccId_VZW = mIccId.substring(0, 4) + " "
                    + mIccId.substring(4, 8) + " "
                    + mIccId.substring(8, 12) + " "
                    + mIccId.substring(12, 16) + " "
                    + mIccId.substring(16, mIccId.length());
        } else {
            mIccId_VZW = mIccId;
        }
        return mIccId_VZW;
    }

    // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
    private void getSvcCmdInstants() {
        new Thread() {
            public void run() {
                mWdc = Svc_cmd.LgSvcCmd_getCmdValue(CMD_CALL_WDC, getApplicationContext());
            }
        }.start();
    }

    // [END_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).

    // [S][2013.04.02][yongjaeo.lee@lge.com][VZW] Last factory data reset
    public long getLastFactoryResetTime() {
        try {
            Log.d(LOG_TAG,
                    "getLastFactoryResetTime:"
                            + getTimeString(android.provider.Settings.Secure.getLong(
                                    getContentResolver(), FACTORY_RESET_DATE)));
            return getCalendarTimeMillis(android.provider.Settings.Secure.getLong(
                    getContentResolver(), FACTORY_RESET_DATE));
        } catch (SettingNotFoundException e) {
            Log.e(LOG_TAG, "getLastFactoryResetTime - SettingNotFoundException e : " + e);
            return getDummyTime(false);
        }
    }

    public String getTimeString(Long time) {
        return DateFormat.getTimeFormat(this).format(time);
    }

    public String getDateString(Long time) {
        return DateFormat.getDateFormat(this).format(time);
    }

    public long getDummyTime(boolean isAmPm) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        Date date = new Date();
        if (isAmPm == false) {
            date.setHours(22);
            date.setMinutes(00);
            date.setSeconds(00);
        } else {
            date.setHours(06);
            date.setMinutes(00);
            date.setSeconds(00);
        }

        c.setTime(date);
        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);
        c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);
        c.add(java.util.Calendar.DAY_OF_YEAR, +1);

        return c.getTimeInMillis();
    }

    private long getCalendarTimeMillis(long time) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(time);

        int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);
        int seconds = c.get(java.util.Calendar.SECOND);

        c.set(java.util.Calendar.AM_PM, 0);
        c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(java.util.Calendar.MINUTE, minute);
        c.set(java.util.Calendar.SECOND, seconds);

        return c.getTimeInMillis();
    }

    // [E][2013.04.02][yongjaeo.lee@lge.com][VZW] Last factory data reset

    // LGE_CHANGE_S, IMEI Barcode
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.e(LOG_TAG, "onPreferenceTreeClick!!");

        if (preference.getKey().equals(KEY_IMEI)) {
            String imei = mPhone.getImei(); // get imei
            Dialog dialog = new Dialog(this); // set dialog

            dialog.setContentView(R.layout.imei_barcode);
            dialog.setTitle("IMEI");

            // generate IMEI Barcode Image
            final int WHITE = 0xFFFFFFFF;
            final int BLACK = 0xFF000000;
            final int BARCODE_BITMAP_WIDTH = 600;
            final int BARCODE_BITMAP_HEIGHT = 200;

            MultiFormatWriter barcodeWriter = new MultiFormatWriter();
            BitMatrix bitmatrix;
            try {
                bitmatrix = barcodeWriter.encode(imei, BarcodeFormat.CODE_128,
                        BARCODE_BITMAP_WIDTH, BARCODE_BITMAP_HEIGHT);
            } catch (Exception e) {
                e.printStackTrace();
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            int width = bitmatrix.getWidth();
            int height = bitmatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitmatrix.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            ImageView imeiImageView = (ImageView)dialog.findViewById(R.id.imei_image_view); // set imei barcode image view
            imeiImageView.setImageBitmap(bitmap);

            TextView imeiTextView = (TextView)dialog.findViewById(R.id.imei_text_view); // set imei barcode text view
            imeiTextView.setText(imei);

            dialog.show(); // show dialog
        } else if (preference.getKey().equals(KEY_IMEI_SV)
                && SystemProperties.get("ro.build.sbp").equals("1") ) {
            if (mSWOVHitCountdown > 0) {
                mSWOVHitCountdown--;
                if (mSWOVHitCountdown == 0) {
                    // CUPSS : show SWOV on Settings
                    this.getSharedPreferences(KEY_MAIN_SOFTWARE_VERSION,
                            Context.MODE_PRIVATE).edit().putBoolean(
                                "SHOW", true).apply();
                    addPreferenceFromScreen(mMainPref);
                    findPreference(KEY_MAIN_SOFTWARE_VERSION).setSummary(
                            getMainSWVersion());
                    if (mSWOVHitToast != null) {
                        mSWOVHitToast.cancel();
                    }
                    mSWOVHitToast = Toast.makeText(this, R.string.show_swov_on,
                            Toast.LENGTH_LONG);
                    mSWOVHitToast.show();
                } else if (mSWOVHitCountdown > 0
                        && mSWOVHitCountdown < (TAPS_TO_SHOW_SWOV - 2)) {
                    if (mSWOVHitToast != null) {
                        mSWOVHitToast.cancel();
                    }
                    mSWOVHitToast = Toast.makeText(this, getResources().getQuantityString(
                            R.plurals.show_swov_countdown, mSWOVHitCountdown, mSWOVHitCountdown),
                                Toast.LENGTH_SHORT);
                    mSWOVHitToast.show();
                }
            } else if (mSWOVHitCountdown < 0) {
                if (mSWOVHitToast != null) {
                    mSWOVHitToast.cancel();
                }
                findPreference(KEY_MAIN_SOFTWARE_VERSION).setSummary(
                        getMainSWVersion());
                mSWOVHitToast = Toast.makeText(this, R.string.show_swov_already,
                        Toast.LENGTH_LONG);
                mSWOVHitToast.show();
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // LGE_CHANGE_E, IMEI Barcode

    private void HardwareRoVersion() {
        Log.d(LOG_TAG, "HardwareRoVersion()");

        mRev = (Preference)findPreference(KEY_REV_CHECK);
        String hardware_version = mRes.getString(R.string.device_info_default);
        String hwver = SystemProperties.get("ro.revision");
        Log.d(LOG_TAG, "hwver : " + hwver);
        try {
            if (hwver.equals("0")) {
                hardware_version = "Rev.0";
            } else if (hwver.equals("1")) {
                hardware_version = "Rev.A";
            } else if (hwver.equals("2")) {
                hardware_version = "Rev.B";
            } else if (hwver.equals("3")) {
                hardware_version = "Rev.C";
            } else if (hwver.equals("4")) {
                hardware_version = "Rev.D";
            } else if (hwver.equals("5")) {
                hardware_version = "Rev.E";
            } else if (hwver.equals("6")) {
                hardware_version = "Rev.1.0";
            } else if (hwver.equals("7")) {
                hardware_version = "Rev.1.1";
            } else if (hwver.equals("8")) {
                hardware_version = "Rev.MAX";
            } else {
                hardware_version = sUnknown;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        mRev.setSummary(hardware_version);
    }

    private void HardwareVersion() {
        Log.d(LOG_TAG, "HardwareVersion() "
                + PROPERTY_PCB_VER + " :" + SystemProperties.get(PROPERTY_PCB_VER) + ", "
                + PROPERTY_HW_REVISION + " :" + SystemProperties.get(PROPERTY_HW_REVISION));

        mRev = (Preference)findPreference(KEY_REV_CHECK);
        String sDef_HWversion = getResources().getString(R.string.device_info_default);

        if (mRev != null && !SystemProperties.get(PROPERTY_PCB_VER).equals("")) {
            mRev.setSummary(Utils.readHwVersion_pcb(sDef_HWversion));
        } else if (mRev != null && !SystemProperties.get(PROPERTY_HW_REVISION).equals("")) {
            mRev.setSummary(Utils.readHwVersion_rev(sDef_HWversion));
        } else if (mRev != null && !SystemProperties.get("ro.revision").equals("")) {
            String tempHwRev = SystemProperties.get("ro.revision");
            String hwVer = "";
            if (tempHwRev.charAt(0) == '0' && tempHwRev.length() >= 3) {
                hwVer = "" + tempHwRev.charAt(2);
                hwVer = "Rev." + hwVer.toUpperCase();
                if (tempHwRev.length() >= 4) {
                    if ((tempHwRev.charAt(3) > '0' ) && (tempHwRev.charAt(3) <= '9')) {
                        hwVer = hwVer + tempHwRev.charAt(3);
                    }
                }
            } else if (tempHwRev.charAt(0) == '1' && tempHwRev.length() >= 3) {
                hwVer = "Rev" + tempHwRev.substring(0, 3);
            } else {
                hwVer = "Rev1.0";
            }
            mRev.setSummary(hwVer);
        } else {
            try {
                removePreferenceFromScreen(KEY_REV_CHECK);
                Log.d(LOG_TAG, "removePreferenceFromScreen(KEY_REV_CHECK)");
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + PROPERTY_PCB_VER + " or "
                        + PROPERTY_HW_REVISION + "' missing and no '"
                        + KEY_REV_CHECK + "' preference");
            }
        }
    }

    // [S][2012.02.17][jm.lee2] Dual SIM status
    private void dualSIMStatusCommon() {
        mPhoneStateReceiver = new PhoneStateIntentReceiver(this, mHandler);
        setWifiStatus(); // Wi-Fi MAC address
        setBtStatus(); // Bluetooth address
    }

    private void hideItemForDualSIM() {
        for (String key : DUAL_SIM_HIDE_ENTRIES) {
            removePreferenceFromScreen(key);
        }
    }

    // [E][2012.02.17][jm.lee2] Dual SIM status

    // [S][2012.02.06][jm.lee2] hide Item for operator
    private void hideItemForOperator() {
        String[] arr;

        Log.d(LOG_TAG, "[hideItemForOperator] getOperator = " + Config.getOperator());
        if ((Config.LGU).equals(Config.getOperator())) {
            arr = LGU_HIDE_ENTRIES; // LGU+
        } else if ((Config.DCM).equals(Config.getOperator())) {
            arr = DCM_HIDE_ENTRIES; // DCM
        } else {
            return;
        }

        for (String key : arr) {
            removePreferenceFromScreen(key);
        }
    }

    // [E][2012.02.06][jm.lee2] hide Item for operator

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume ()");

        registerReceiver(mWiFi_Receiver, mWiFi_Filter);
        registerReceiver(mBT_Receiver, mBT_Filter);
        // [S][2012.02.17][jm.lee2] Dual SIM status
        /*
        if (!Utils.isMultiSimEnabled()) {
        mPhoneStateReceiver.registerIntent();
        } else {
        */
        if (mPhone != null && !Utils.isWifiOnly(getApplicationContext())) {
            mPhoneStateReceiver.registerIntent();

            mRadioTech = mPhone.getServiceState().getRilDataRadioTechnology();
            mVoiceTech = mPhone.getServiceState().getRilVoiceRadioTechnology();

            updateSignalStrength();
            updateServiceState(mPhone.getServiceState());
            updateDataState();

            if (OverlayUtils.isMultiSimEnabled()) {
                OverlayUtils.set_MsimTelephonyListener(getApplicationContext(), mPhoneStateListener);
            } else {
                mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                            | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                            | PhoneStateListener.LISTEN_CELL_LOCATION
                            | PhoneStateListener.LISTEN_SERVICE_STATE
                            | PhoneStateListener.LISTEN_CALL_STATE);
            }
            if (mShowLatestAreaInfo) {
                registerReceiver(mAreaInfoReceiver, new IntentFilter(CB_AREA_INFO_RECEIVED_ACTION),
                        CB_AREA_INFO_SENDER_PERMISSION, null);
                // Ask CellBroadcastReceiver to broadcast the latest area info received
                Intent getLatestIntent = new Intent(GET_LATEST_CB_AREA_INFO_ACTION);
                sendBroadcastAsUser(getLatestIntent, UserHandle.ALL,
                        CB_AREA_INFO_SENDER_PERMISSION);
            }

            if (getCtcNetworkInfo()) {
                //updateLocation(mTelephonyManager.getCellLocation());
                updateLocation(
                    OverlayUtils.getCellLocation(getApplicationContext(), mCtcSlotID)
                );
            }
        }
        //        }
        // [E][2012.02.17][jm.lee2] Dual SIM status

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED)); // Battery status, Battery level
        mHandler.sendEmptyMessage(EVENT_UPDATE_STATS); // Up time

        // [S][2012.09.26][munjohn.kang] applied the VZW requirements(eri_version, life_time_call, warranty_date_code) for About-phone.
        getWdcNvItem();
        getLifetimeCallsNvItem();
        // [E][munjohn.kang]

        if (SystemProperties.get("ro.build.sbp").equals("1")) {
            mSWOVHitCountdown = this.getSharedPreferences(KEY_MAIN_SOFTWARE_VERSION,
                    Context.MODE_PRIVATE).getBoolean(
                        "SHOW", false) ? -1 : TAPS_TO_SHOW_SWOV;
            mMainPref = (Preference)findPreference(KEY_MAIN_SOFTWARE_VERSION);
            if ((mMainPref != null) && (mSWOVHitCountdown == -1)) {
                addPreferenceFromScreen(mMainPref);
                findPreference(KEY_MAIN_SOFTWARE_VERSION).setSummary(getMainSWVersion());
            } else {
                removePreferenceFromScreen(KEY_MAIN_SOFTWARE_VERSION);
            }
            mSWOVHitToast = null;
        } else {
            removePreferenceFromScreen(KEY_MAIN_SOFTWARE_VERSION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ("SPR".equals(operator_code)) {
            regul_mHandler.removeCallbacks(m_display_run1);
            regul_mHandler = null;
            ATClientUtils.atClient_unBindService();
        }
    }

    // [START_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).
    private void getWdcNvItem() {
        new Thread() {
            public void run() {
                Message msg = mHandler.obtainMessage(EVENT_WDC_LOADED);
                mWdc = Svc_cmd.LgSvcCmd_getCmdValue(CMD_CALL_WDC, getApplicationContext());
                Log.d(LOG_TAG, "[LG_CALL_NV] CMD_CALL_WDC = " + mWdc);
                mHandler.sendMessage(msg);
            }
        }
                .start();
    }

    private void getLifetimeCallsNvItem() {
        new Thread() {
            public void run() {
                Message msg = mHandler.obtainMessage(EVENT_LIFETIME_LOADED);
                mLifeTimer = Svc_cmd.LgSvcCmd_getCmdValue(CMD_CALL_LIFETIMER,
                        getApplicationContext());
                Log.d(LOG_TAG, "[LG_CALL_NV] CMD_CALL_LIFETIMER1 = " + mLifeTimer);
                mHandler.sendMessage(msg);
            }
        }
                .start();
    }

    private void getMDNByRTNValueNvItem() {
        new Thread() {
            public void run() {
                Message msg = mHandler.obtainMessage(EVENT_MDN_SPR_RTNVALUE);
                mMDNbyRTNValue = Svc_cmd.LgSvcCmd_getCmdValue(CMD_MDN_RTN_VALUE,
                        getApplicationContext());
                mHandler.sendMessage(msg);
            }
        }
                .start();
    }

    private void getMINByRTNValueNvItem() {
        new Thread() {
            public void run() {
                Message msg = mHandler.obtainMessage(EVENT_MIN_SPR_RTNVALUE);
                mMINbyRTNValue = Svc_cmd.LgSvcCmd_getCmdValue(CMD_MIN_RTN_VALUE,
                        getApplicationContext());
                mHandler.sendMessage(msg);
            }
        }
                .start();
    }

    private void getPRLByRTNValueNvItem() {
        new Thread() {
            public void run() {
                Message msg = mHandler.obtainMessage(EVENT_PRL_SPR_RTNVALUE);
                mPRLbyRTNValue = Svc_cmd.LgSvcCmd_getCmdValue(CMD_PRL_RTN_VALUE,
                        getApplicationContext());
                mHandler.sendMessage(msg);
            }
        }
                .start();
    }

    // [END_LGE_VOICECALL] , ADD, hyejiny.kim , 2013-04-18 , [VoiceCall][VZW][NV] getSvcCmd() shall be called after QcrilMsgTunnelService Connected Successfully (onServiceConnected).

    public void putDataLifeTime() {
        Log.i(LOG_TAG, "status:: putDataLifeTime(), ENTER");

        Thread lt_t = new Thread() {
            public void run() {
                long iLifeTimeData = 0;
                String sLifeTimeData = null;

                try {
                    sLifeTimeData = OverlayUtils.LgSvcCmd_getCmdValue(CMD_LIFETIME_DATA);
                    Log.i(LOG_TAG, "status:: GET CMD_LIFETIME_DATA : " + sLifeTimeData);

                    if (sLifeTimeData != null) {
                        iLifeTimeData = Integer.parseInt(sLifeTimeData);
                    } else {
                        final String zero = Long.toString(0);
                        Log.i(LOG_TAG, "status:: SET init Value(0) to LifeTimeData NV : " + zero);
                        OverlayUtils.LgSvcCmd_setCmdValue(CMD_LIFETIME_DATA_PUT, zero);
                    }

                    long iTrafficStats = TrafficStats.getMobileTxBytes()
                            + TrafficStats.getMobileRxBytes();
                    long iLastWrittenBytes = Settings.System.getLong(getContentResolver(),
                            OverlayUtils.LifeTime_NvBackup(), 0);
                    Log.d(LOG_TAG, "iLastWrittenBytes = " + iLastWrittenBytes);

                    if (iLastWrittenBytes > iTrafficStats) {
                        Log.i(LOG_TAG, "status:: TrafficStats was reset: " + iTrafficStats);
                        Log.i(LOG_TAG, "status:: Reset LastWrittenBytes from " + iTrafficStats
                                + " to 0");
                        Settings.System.putLong(getContentResolver(),
                                OverlayUtils.LifeTime_NvBackup(), 0);
                    }

                    long iNewKBytes = (iTrafficStats - iLastWrittenBytes) / 1024;

                    Log.i(LOG_TAG, "status:: current TrafficStats: " + iTrafficStats);
                    Log.i(LOG_TAG, "status:: current LastWrittenBytes: " + iLastWrittenBytes);
                    Log.i(LOG_TAG, "status:: current NewKBytes: " + iNewKBytes + " (KB)");

                    mHandler.sendMessage(mHandler.obtainMessage(EVENT_LIFETIME_DATA_LOADED,
                            iLifeTimeData + iNewKBytes));

                    if (iNewKBytes > 0) {
                        sLifeTimeData = Long.toString(iLifeTimeData + iNewKBytes);
                        OverlayUtils.LgSvcCmd_setCmdValue(CMD_LIFETIME_DATA_PUT, sLifeTimeData);
                        Log.i(LOG_TAG, "status:: PUT CMD_LIFETIME_DATA : " + sLifeTimeData);

                        Settings.System.putLong(getContentResolver(),
                                OverlayUtils.LifeTime_NvBackup(), iTrafficStats);
                    } else {
                        Log.i(LOG_TAG, "status:: Skip NV Update because new traffic is small");
                    }

                    Log.i(LOG_TAG, "status:: putDataLifeTime(), EXIT");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "status::putDataLifeTime fail " + e);
                }
            }
        };

        lt_t.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause ()");
        // [S][2012.02.17][jm.lee2] Dual SIM status
        //        if (Utils.isMultiSimEnabled()) {
        //            mPhoneStateReceiver.unregisterIntent();
        //        } else {
        if (mPhone != null && !Utils.isWifiOnly(getApplicationContext())) {
            mPhoneStateReceiver.unregisterIntent();
            if (OverlayUtils.isMultiSimEnabled()) {
                OverlayUtils.release_MsimTelephonyListener(
                    getApplicationContext(), mPhoneStateListener);
            } else {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
        //        }
        // [E][2012.02.17][jm.lee2] Dual SIM status

        if (mShowLatestAreaInfo) {
            unregisterReceiver(mAreaInfoReceiver);
        }

        unregisterReceiver(mBatteryInfoReceiver);
        unregisterReceiver(mWiFi_Receiver);
        unregisterReceiver(mBT_Receiver);
        mHandler.removeMessages(EVENT_UPDATE_STATS);
    }
    // CUPSS : show SWOV on Settings
    private void addPreferenceFromScreen(Preference pref) {
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(pref);
        } else {
            if (pref == null) {
                Log.i(LOG_TAG, "addPreferenceFromScreen() removePreferenceFromScreen pref == null");
            } else {
                Log.i(LOG_TAG, "addPreferenceFromScreen() getPreferenceScreen() == null");
            }
        }
    }

    /**
    * Removes the specified preference, if it exists.
    *
    * @param key
    *            the key for the Preference item
    */
    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        // [2012.08.24][munjohn.kang] added a Null pointer exception handling
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        } else {
            if (pref == null) {
                //Log.i(LOG_TAG, "removePreferenceFromScreen() removePreferenceFromScreen pref == null");
            } else {
                Log.i(LOG_TAG, "removePreferenceFromScreen() getPreferenceScreen() == null");
            }
        }
    }

    /**
    * @param preference The key for the Preference item
    * @param property The system property to fetch
    * @param alt The default value, if the property doesn't exist
    */
    private void setSummary(String preference, String property, String alt) {
        try {
            if (findPreference(preference) != null) {
                String read_val = SystemProperties.get(property, alt);
                //Log.i(LOG_TAG, "setSummary , read_val:"+read_val);

                if (read_val.equals("Unknown") || read_val.equals("unknown")) { // exception handling for unknown or Unknown
                    findPreference(preference).setSummary(alt);
                } else {
                    findPreference(preference).setSummary(read_val);
                }
            }
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "setSummary RuntimeException");
        }
    }

    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            text = sUnknown;
        }
        // some preferences may be missing
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text);
        }
    }

    private void updateNetworkType() {

        //        String mMccMnc = mPhone.getServiceState().getOperatorNumeric();
        String mOperator_Imsi = mPhone.getSubscriberId();
        String mMccMnc = null;
        if (mOperator_Imsi != null && (mOperator_Imsi.length() > 7)) {
            mMccMnc = mOperator_Imsi.substring(0, 5);
        }

        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            if ("CTC".equals(Config.getOperator()) || "CTO".equals(operator_code)) {
                sNetworkType = MSIMUtils.get_network_type_ctc(mCtcSlotID, sUnknown);
            } else {
                sNetworkType = MSIMUtils.get_network_type(getApplicationContext(), sUnknown);
            }
            Log.i(LOG_TAG, "sNetworkType:" + sNetworkType);

            if (sUnknown.equals(sNetworkType)) {
                int mVoiceTechNum = mPhone.getServiceState().getRilVoiceRadioTechnology();
                sNetworkType = MSIMUtils.network_type_name(mVoiceTechNum, sUnknown);
            }

            if (!sNetworkType.equals("common_pushed")) {
                setSummaryText(KEY_NETWORK_TYPE, sNetworkType);
            } else {
                setSummaryText(KEY_NETWORK_TYPE, sUnknown);
            }
            updateNetworkTypeCTC();
            return;
        }

        sNetworkType = SystemProperties.get(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE);

        Log.i(LOG_TAG, "sNetworkType:" + sNetworkType);

        if (sNetworkType.equalsIgnoreCase("unknown") || TextUtils.isEmpty(sNetworkType)
                || sNetworkType == null) {
            setSummaryText(KEY_NETWORK_TYPE, sUnknown);
        } else if (sNetworkType.equalsIgnoreCase("LTE") && "VDF".equals(operator_code)) {
            if ("26201".equals(mMccMnc)) {
                Log.i(LOG_TAG, "26201 sNetworkType:" + sNetworkType);
                setSummaryText(KEY_NETWORK_TYPE, sNetworkType);
            } else {
                setSummaryText(KEY_NETWORK_TYPE, "4G");
            }
        } else if (sNetworkType.equalsIgnoreCase("LTE") && "KDDI".equals(operator_code)) {
            setSummaryText(KEY_NETWORK_TYPE, "4G");
        } else if ((mMccMnc != null && "21407".equals(mMccMnc) && sNetworkType.equalsIgnoreCase("LTE"))) {
            setSummaryText(KEY_NETWORK_TYPE, "4G");
        } else if ((mMccMnc != null && "23410".equals(mMccMnc) && sNetworkType.equalsIgnoreCase("LTE"))) {
            String gid = new com.lge.uicc.LGUiccCard().getGid1();
            if ((gid != null && gid.startsWith("0affffff")) == true) {
                setSummaryText(KEY_NETWORK_TYPE, sNetworkType);
            } else {
                setSummaryText(KEY_NETWORK_TYPE, "4G");
            } 
        } else {
            if ("CA".equals(Config.getCountry())) {
                setSummaryText(KEY_NETWORK_TYPE, mTelephonyManager.getNetworkTypeName() + ":"
                        + mTelephonyManager.getNetworkType());
            } else {
                setSummaryText(KEY_NETWORK_TYPE, sNetworkType);
            }
        }

        updateNetworkTypeCTC();
    }

    private void updateNetworkTypeCTC() {
        if ("CTC".equals(operator_code) || "CTO".equals(operator_code)) {
            if (sNetworkType.equalsIgnoreCase("GSM") || sNetworkType.equalsIgnoreCase("GPRS")
                    || sNetworkType.equalsIgnoreCase("EDGE")) {
                setSummaryText(KEY_NETWORK_TYPE, "GSM");
            } else if (sNetworkType.equalsIgnoreCase("WCDMA")
                    || sNetworkType.equalsIgnoreCase("UMTS")) {
                setSummaryText(KEY_NETWORK_TYPE, "WCDMA");
            } else if (sNetworkType.equalsIgnoreCase("HSPA")
                    || sNetworkType.equalsIgnoreCase("HSUPA")
                    || sNetworkType.equalsIgnoreCase("HSDPA")) {
                setSummaryText(KEY_NETWORK_TYPE, "HSPA");
            } else {
                setSummaryText(KEY_NETWORK_TYPE, sNetworkType);
            }
        }
    }

    private void updateDataState() {
        int state = 0;
        int serState = mPhone.getServiceState().getState();
        int dataServiceState = mPhone.getServiceState().getDataRegState();

        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            state = MSIMUtils
                    .get_mobileNetwork_state(getApplicationContext(), mTelephonyManager);
        } else {
            state = mTelephonyManager.getDataState();
        }

        String display = mRes.getString(R.string.radioInfo_unknown);

        Log.d(LOG_TAG, "serState = " + serState);
        Log.d(LOG_TAG, "dataServiceState = " + dataServiceState);
        Log.d(LOG_TAG, "state = " + state);
        if ((serState == ServiceState.STATE_OUT_OF_SERVICE)
                && (dataServiceState == ServiceState.STATE_OUT_OF_SERVICE)) {
            display = mRes.getString(R.string.radioInfo_data_disconnected);
        } else {
            switch (state) {
            case TelephonyManager.DATA_CONNECTED:
                display = mRes.getString(R.string.radioInfo_data_connected);
                break;

            case TelephonyManager.DATA_SUSPENDED:
                display = mRes.getString(R.string.radioInfo_data_suspended);
                break;

            case TelephonyManager.DATA_CONNECTING:
                display = mRes.getString(R.string.radioInfo_data_connecting);
                break;

            case TelephonyManager.DATA_DISCONNECTED:
                display = mRes.getString(R.string.radioInfo_data_disconnected);
                break;

            default:
                break;
            }
        }

        setSummaryText(KEY_DATA_STATE, display);
    }

    private void updateServiceState(ServiceState serviceState) {
        int state = set_service_state(serviceState);
        if (getCtcNetworkInfo()) {
            SLog.i("mPhone.getPhoneType() = " + mPhone.getPhoneType());
            int radioTech = 0;
            radioTech = serviceState.getRilDataRadioTechnology();
            SLog.i("mPhone.getPhoneType() = " + mPhone.getPhoneType());
            SLog.i("radioTech = " + radioTech);
            if (LGSubscriptionManager.isNetworkRoaming(mCtcSubscriber)) {
                updateSidNidState(serviceState);
            } else {
                if (ServiceState.isCdma(radioTech)
                        || radioTech == ServiceState.RIL_RADIO_TECHNOLOGY_LTE) {
                    updateSidNidState(serviceState);
                }
            }
            updateMccMncForCtc(serviceState);
        }

        set_roaming_state(serviceState);

        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            String operator_name = "";
            if ("CTC".equals(Config.getOperator()) || "CTO".equals(operator_code)) {
                operator_name = MSIMUtils
                    .get_operator_name_ctc(mCtcSlotID, sUnknown);
            } else {
                operator_name = MSIMUtils
                        .get_operator_name(getApplicationContext(), sUnknown);
            }
            /* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [START]  */
            String serving_MccMnc = MSIMUtils.get_operator_numeric(getApplicationContext());
            Log.d(LOG_TAG, "serving_MccMnc " + serving_MccMnc);
            /* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [END]  */
            /* LGSI_CHANGE_S: PLMN name wrong TD issue for Airtel and Vodafone
             * 2012-12-24,arshad.mumtaz@lge.com,
             * Change the network name as per requirement
             */
            Log.d(LOG_TAG, "operator_name " + operator_name);

            if (operator_name.equalsIgnoreCase("COSCOM")) {
                operator_name = "Ucell";
            } else if (operator_name.equalsIgnoreCase("DW-GSM")) {
                operator_name = "Beeline-Uz";
            }

            /* LGSI_CHANGE_E: PLMN name wrong TD issue for Airtel and Vodafone */
            if (!operator_name.equals("common_pushed")) {
                //20121214 lolly.liu@lge.com add for CN dual Chinese string
                if ((Config.getCountry()).equalsIgnoreCase("CN")) {
                    String operatiorName = "";
                    if (operator_name != null) {
                        if (operator_name.equalsIgnoreCase("CMCC") || operator_name.equalsIgnoreCase("CMO")
                                || operator_name.equalsIgnoreCase("CHINA MOBILE")) {
                            operatiorName = getString(R.string.sp_CMCC_China_Mobile_SHORT);
                        } else if (operator_name.equalsIgnoreCase("CHN-CUGSM")
                                || operator_name.equalsIgnoreCase("CHN-UNICOM")
                                || operator_name.equalsIgnoreCase("CUCC")) {
                            operatiorName = getString(R.string.sp_CUCC_China_Telecom_SHORT);
                        } else {
                            operatiorName = operator_name;
                        }
                        setSummaryText(KEY_OPERATOR_NAME, operatiorName);
                    }
                }
                //20130201 lolly.liu@lge.com for TWN operator T Chinese [Start]
                //jing01.ma add for HK plmn display error 2013.3.9
                else if ((Config.getCountry()).equals("TW")
                        || (Config.getCountry()).equals("HK")) {
                    String NwName = new String();

                    NwName = operator_name;
                    if (("zh".equals(Locale.getDefault().getLanguage())) && NwName != null) {
                        if (NwName.equalsIgnoreCase("Chunghwa")) {
                            char SPN_chars[] = { 0x4E2D, 0x83EF, 0x96FB, 0x4FE1 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("VIBO") || NwName.equalsIgnoreCase("T Star")) {
                            char SPN_chars[] = { 0x53F0, 0x7063, 0x4E4B, 0x661F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("TW Mobile")
                                || NwName.equalsIgnoreCase("TWN GSM")
                                || NwName.equalsIgnoreCase("TRANSASIA")
                                || NwName.equalsIgnoreCase("MOBITAI")) {
                            char SPN_chars[] = { 0x53F0, 0x6E7E, 0x5927, 0x54E5, 0x5927 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("Far EasTone")
                                || NwName.equalsIgnoreCase("Far EasTone")
                                || NwName.equalsIgnoreCase("KG Telecom")
                                || NwName.equalsIgnoreCase("KGT-ONLINE")
                                || NwName.equalsIgnoreCase("KGT Online")
                                || NwName.equalsIgnoreCase("KG Telecom")) {
                            char SPN_chars[] = { 0x9060, 0x50B3, 0x96FB, 0x4FE1 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        }
                        /* LGSI_P970_TD :77724_ESA_AMITKUMAR_START: For Twn Chinese the CMHK String shld be same HKG Chinese._(22.02.11)*/
                        else if (NwName.equalsIgnoreCase("China Mobile HK")
                                || NwName.equalsIgnoreCase("CMHK")
                                || NwName.equalsIgnoreCase("PEOPLES")) {
                            char SPN_chars[] = { 0x4E2D, 0x570B, 0x79FB, 0x52D5, 0x9999, 0x6E2F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        }
                        /* LGSI_P970_TD :77724_ESA_AMITKUMAR_END_(22.02.11) */
                        setSummaryText(KEY_OPERATOR_NAME, NwName);
                    }
                    else {
                        setSummaryText(KEY_OPERATOR_NAME, NwName);
                    }
                }
                //20130201 lolly.liu@lge.com for TWN operator T Chinese [End]
                else {
                    /* LGSI_CHANGE_S: PLMN name wrong TD issue for Airtel and Vodafone
                    * 2012-12-24,arshad.mumtaz@lge.com,
                    * Change the network name as per requirement
                    */
                    /* LGSI_CHANGE_S: Same operator name is coming in about phone for different operator
                    * 2013-01-17,arshad.mumtaz@lge.com@lge.com,
                    * Imsi API was giving only first slot IMSI, so changed to get as per shared preference
                    */
                    String operator_Imsi = MSIMUtils.get_IMSI_Current_Tab(getApplicationContext());
                    //String Operator_Imsi = mPhone.getSubscriberId();
                    /* LGSI_CHANGE_E: Same operator name is coming in about phone for different operator */
                    String mcc = null;
                    String mcc_mnc = null;
                    String mcc_mnc_ind = null;
                    // LGSI-Telephony-Sujit added mcc_mnc_gid for MVNO :START
                    String mcc_mnc_gid = null;
                    // LGSI-Telephony-Sujit added mcc_mnc_gid for MVNO :END
                    if (operator_Imsi != null && (operator_Imsi.length() > 7)) {
                        mcc = operator_Imsi.substring(0, 3);
                        mcc_mnc = operator_Imsi.substring(0, 5);
                        mcc_mnc_ind = operator_Imsi.substring(0, 6);
                        // LGSI-Telephony-Sujit added mcc_mnc_gid for MVNO
                        mcc_mnc_gid = operator_Imsi.substring(0, 7);
                    }
                    String cellinfo = serviceState.getOperatorNumeric();
                    /*
                    Log.d(LOG_TAG, "mcc: " + mcc + "mcc_mnc: " + mcc_mnc + "mcc_mnc_ind: " 
                        + mcc_mnc_ind + "mcc_mnc_ind: " + mcc_mnc_ind + "Operator_Imsi: " 
                        + Operator_Imsi + " mcc_mnc_gid: " + mcc_mnc_gid + " cellinfo: " + cellinfo);
                    */
                    Log.d(LOG_TAG, "regioncode = " + regioncode);
                    if (regioncode.compareTo("ESA") == 0 || regioncode.compareTo("AME") == 0) {
                        if (state == ServiceState.STATE_IN_SERVICE) {
                            Log.d(LOG_TAG, "regioncode ESA or regioncode AME ");

                            // LGSI-Telephony-Sujit added logs for condition check

                            if (mcc != null && (("404".equals(mcc)) || ("405".equals(mcc)))) {
                                if (operator_name.equalsIgnoreCase("UNINOR")) {
                                    operator_name = "Uninor";
                                }
                                else if (operator_name.equalsIgnoreCase("Airtel")
                                        || (operator_name.equalsIgnoreCase("IND airtel"))) {
                                    operator_name = "airtel";
                                }

                                if (operator_name.equalsIgnoreCase("VODAFONE")) {
                                    operator_name = "Vodafone IN";
                                }
                                // LGSI-Telephony-Sujit added to display "Idea" instead of "IDEA":START
                                else if (operator_name.equalsIgnoreCase("IDEA")) {
                                    operator_name = "Idea";
                                }
                                // LGSI-Telephony-Sujit added to display "Idea" instead of "IDEA":END
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            else if (mcc_mnc != null && "41603".equals(mcc_mnc)) {
                                if (serviceState.getRoaming() == false) {
                                    operator_name = "Umniah";
                                }
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            else if ((state == ServiceState.STATE_IN_SERVICE && serviceState
                                    .getRoaming() == true)
                                    && (cellinfo != null && mcc_mnc_ind != null)
                                    && (("405853".equals(mcc_mnc_ind)) && (cellinfo
                                            .compareTo("40430") == 0))) {
                                operator_name = "Vodafone IN";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            else if (mcc_mnc != null && "40486".equals(mcc_mnc)) {
                                operator_name = "Vodafone IN";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* LGSI_CHANGE_S:Network Name Display in Status Info
                            * 2012-02-18,amit3g.kumar@lge.com,
                            * WBS_HDB Network Name Display in Status info
                            */
                            if (mcc != null && ("520".equals(mcc))
                                    && ("TRUE-H/my".equalsIgnoreCase(operator_name))) {
                                if ((mcc_mnc_gid != null) && ("5200020".equals(mcc_mnc_gid))) {
                                    operator_name = "TRUE-H";
                                } else if ((mcc_mnc_gid != null) && ("5200019".equals(mcc_mnc_gid))) {
                                    operator_name = "my";
                                }
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for TOT 3G is displayed as TOT Mobile , should be TOT 3G */
                            if (mcc_mnc != null && "52015".equals(mcc_mnc)
                                    && (operator_name.equalsIgnoreCase("TOT Mobile"))) {
                                operator_name = "TOT 3G";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for RED BULL [ MVNO : Cell C] is displayed as CELL C , should be RED BULL */
                            if (mcc_mnc_gid != null && "6550713".equals(mcc_mnc_gid)
                                    && (operator_name.equalsIgnoreCase("Cell C"))) {
                                operator_name = "Red Bull";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for Maroc Telecom is displayed as IAM , should be Maroc Telecom */
                            if (mcc != null && "604".equals(mcc)
                                    && (operator_name.equalsIgnoreCase("IAM"))) {
                                operator_name = "Maroc Telecom";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* LGSI_TELEPHONY_TD#198764_operator_name for dtac is displayed as DTAC , should be dtac */
                            if (mcc != null && "520".equals(mcc)
                                    && ("dtac".equalsIgnoreCase(operator_name))) {
                                operator_name = "dtac";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for Loop Mobile is displayed as BPL Mobile , should be Loop Mobile */
                            if (mcc != null && "404".equals(mcc)
                                    && (operator_name.equalsIgnoreCase("BPL Mobile"))) {
                                operator_name = "Loop Mobile";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for TELKOMSEL is displayed as T-SEL/TELKOMSE , should be TELKOMSEL */
                            if (mcc != null
                                    && "510".equals(mcc)
                                    && (operator_name.equalsIgnoreCase("TELKOMSE") || operator_name
                                            .equalsIgnoreCase("T-SEL"))) {
                                operator_name = "TELKOMSEL";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for StarHub is displayed as STARHUB , should be StarHub */
                            if (mcc != null && "525".equals(mcc)
                                    && (operator_name.equalsIgnoreCase("STARHUB"))) {
                                operator_name = "StarHub";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for Vodafone TR is displayed as VODAFONE TR , should be VODAFONE TR */
                            if (mcc != null && "286".equals(mcc)
                                    && (operator_name.equalsIgnoreCase("VODAFONE TR"))) {
                                operator_name = "Vodafone TR";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /* operator_name for Gmobile is displayed as Gmobil , should be Gmobile */
                            if (mcc_mnc != null && "45207".equals(mcc_mnc)) {
                                operator_name = "Gmobile";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /*LGSI_CHANGE_E:Network Name Display in Status info*/
                            /*LGSI_CHANGE_START giribabu.gogineni 2013-09-04 South_Africa_Requirement_TelkomSA_Instead_Of_8.SA*/
                            else if (mcc_mnc != null && "65502".equals(mcc_mnc)
                                    && operator_name.equalsIgnoreCase("8.ta")) {
                                operator_name = "TelkomSA";
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                            /*LGSI_CHANGE_END giribabu.gogineni 2013-07-04 South_Africa_Requirement_TelkomSA_Instead_Of_8.SA*/
                            else {
                                setSummaryText(KEY_OPERATOR_NAME, operator_name);
                            }
                        }
                        else {
                            setSummaryText(KEY_OPERATOR_NAME, operator_name);
                        }
                    } else if (mcc != null && (("404".equals(mcc)) || ("405".equals(mcc)))) {
                        if (state == ServiceState.STATE_IN_SERVICE) {
                            if (operator_name.equalsIgnoreCase("UNINOR")) {
                                operator_name = "Uninor";
                            } else if (operator_name.equalsIgnoreCase("Airtel")
                                    || (operator_name.equalsIgnoreCase("IND airtel"))) {
                                operator_name = "airtel";
                            }

                            if (operator_name.equalsIgnoreCase("VODAFONE")
                                    || operator_name.equalsIgnoreCase("HUTCH")) {
                                operator_name = "Vodafone IN";
                            } else if (operator_name.equalsIgnoreCase("IDEA")) {
                                operator_name = "Idea";
                            }
                        }
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    } else if (mcc_mnc != null && "41603".equals(mcc_mnc)
                            && (state == ServiceState.STATE_IN_SERVICE)) {
                        if (serviceState.getRoaming() == false) {
                            operator_name = "Umniah";
                        }
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    } else if ((state == ServiceState.STATE_IN_SERVICE && serviceState.getRoaming() == true)
                            && (cellinfo != null && mcc_mnc_ind != null)
                            && (("405853".equals(mcc_mnc_ind))
                            && (cellinfo.compareTo("40430") == 0))) {
                        operator_name = "Vodafone IN";
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    } else if (mcc_mnc != null && "40486".equals(mcc_mnc)
                            && (state == ServiceState.STATE_IN_SERVICE)) {
                        operator_name = "Vodafone IN";
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                        /* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [START]  */
                    } else if ((state == ServiceState.STATE_IN_SERVICE)
                            && operator_name.equalsIgnoreCase("mobilcom-debitel")) {
                        if ("26201".equals(serving_MccMnc)) {
                            operator_name = "Telekom.de";
                        }
                        if ("26202".equals(serving_MccMnc)) {
                            operator_name = "Vodafone.de";
                        }
                        if ("26203".equals(serving_MccMnc)) {
                            operator_name = "Eplus";
                        }
                        Log.d(LOG_TAG, "serving_MccMnc " + serving_MccMnc + " operator_name "
                                + operator_name);
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    }
                    else if ((state == ServiceState.STATE_IN_SERVICE)
                            && operator_name.equalsIgnoreCase("congstar")) {
                        if ("26201".equals(serving_MccMnc)) {
                            operator_name = "Telekom.de";
                        }
                        if ("26202".equals(serving_MccMnc)) {
                            operator_name = "Telekom.de";
                        }
                        Log.d(LOG_TAG, "Mahendra serving_MccMnc " + serving_MccMnc
                                + " operator_name " + operator_name);
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    }
                    /* 2014-03-26 sujitk.mohapatra@lge.com LGSI_NWNAME_DEBITEL_HARD_CODING  [End]  */
                    else {
                        /* LGSI_CHANGE_E: PLMN name wrong TD issue for Airtel and Vodafone */
                        setSummaryText(KEY_OPERATOR_NAME, operator_name);
                    }
                }

                return;
            }
        }

        /*
        * LGE_CHANGE_S : TRACFONE_ONS* 2012.01.26, kyemyung.an@lge.com*
        * TRACFONE ONS requirement : Home/Roam* Repository : None
        */
        //[S][M4_E610][operator request] PLMN 24005 displayed operator name Sweden 3G
        String tele_sweden = serviceState.getOperatorNumeric();
        Log.d(LOG_TAG, "tele_sweden(mccmnc) = " + tele_sweden);
        Log.d(LOG_TAG,
                "serviceState.getOperatorAlphaLong() : " + serviceState.getOperatorAlphaLong());

        if (tele_sweden != null && tele_sweden.length() >= 3 && tele_sweden.substring(0, 3).equals("240")) {
            /*String longName_sweden = serviceState.changePlmnNameForSwedish(serviceState.getOperatorAlphaLong(),
                serviceState.getOperatorAlphaShort(), serviceState.getOperatorNumeric());*/
            String longName_sweden = mPhone.changePlmnNameForSwedish(serviceState.getOperatorAlphaLong(), serviceState.getOperatorAlphaShort(), serviceState.getOperatorNumeric());
            setSummaryText("operator_name", longName_sweden);
        } else if (operator_code.equals("TRF")) {
            if (state == ServiceState.STATE_IN_SERVICE) {
                if (serviceState.getRoaming()) {
                    setSummaryText(KEY_OPERATOR_NAME, mRes.getString(R.string.sp_Roam_NORMAL));
                } else {
                    setSummaryText(KEY_OPERATOR_NAME, mRes.getString(R.string.sp_Home_NORMAL));
                }
            } else {
                setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
            }
        } else if ("VZW".equals(operator_code)) {
            String mVoiceNetwork = LGServiceState.getDefault(serviceState).getVoiceNetworkName();
            String mDataNetwork = LGServiceState.getDefault(serviceState).getDataNetworkName();
            String mVoiceNetwork_name = "1xRTT";
            String mDataNetwork_name = null;
            String mNetworkType = SystemProperties
                    .get(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE);
            Log.d(LOG_TAG, "mVoiceNetwork : " + mVoiceNetwork + "\n" + "mDataNetwork : "
                    + mDataNetwork);
            Log.d(LOG_TAG, "mPhone.getPhoneName() : " + mPhone.getPhoneName());
            Log.d(LOG_TAG, "mNetworkType : " + mNetworkType);
            if (mPhone.getPhoneName().equals("CDMA")) {
                if (TextUtils.isEmpty(mVoiceNetwork) && TextUtils.isEmpty(mDataNetwork)) {
                    setSummaryText(KEY_OPERATOR_NAME, sUnknown);
                } else {
                    if (!TextUtils.isEmpty(mVoiceNetwork) && !TextUtils.isEmpty(mDataNetwork)) {
                        if ("Verizon Wireless".equals(mDataNetwork) && "LTE".equals(mNetworkType)) {
                            mDataNetwork_name = "LTE";
                        } else {
                            mDataNetwork_name = "3G";
                        }
                        setSummaryText(KEY_OPERATOR_NAME, mVoiceNetwork_name + " : "
                                + mVoiceNetwork
                                + "\n" + mDataNetwork_name + " : " + mDataNetwork);
                    } else if (!TextUtils.isEmpty(mVoiceNetwork) && TextUtils.isEmpty(mDataNetwork)) {
                        setSummaryText(KEY_OPERATOR_NAME, mVoiceNetwork);
                    } else if (TextUtils.isEmpty(mVoiceNetwork) && !TextUtils.isEmpty(mDataNetwork)) {
                        setSummaryText(KEY_OPERATOR_NAME, mDataNetwork);
                    } else {
                        Log.d(LOG_TAG, "mVoiceNetwork is null  and mDataNetwork is null");
                        setSummaryText(KEY_OPERATOR_NAME, sUnknown);
                    }
                }
            } else 
            {
                Log.d(LOG_TAG, "serviceState.getState() = " + serviceState.getState());
                if ((serviceState.getState() == ServiceState.STATE_IN_SERVICE)) {
                    setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
                } else {
                    setSummaryText(KEY_OPERATOR_NAME, sUnknown);
                }
            }
        } else if ("LGU".equals(operator_code)) {
            String name = null;
            serviceState = mPhone.getServiceState();

            Log.d(LOG_TAG, "serviceState.getState() = " + serviceState.getState());
            Log.d(LOG_TAG, "serviceState.getDataRegState() = "
                    + serviceState.getDataRegState());
            Log.d(LOG_TAG, "serviceState = " + serviceState);

            if ((serviceState.getState() == ServiceState.STATE_IN_SERVICE)
                    || (serviceState.getDataRegState() == ServiceState.STATE_IN_SERVICE)) {
                name = serviceState.getOperatorAlphaShort();
                if (TextUtils.isEmpty(name)) {
                    name = serviceState.getOperatorAlphaLong();
                }
                if (TextUtils.isEmpty(name)) {
                    name = "LG U+";
                }
                setSummaryText(KEY_OPERATOR_NAME, name);
            } else {
                setSummaryText(KEY_OPERATOR_NAME, sUnknown);
            }
        } else if ("SKT".equals(operator_code)) {
            String NwName = new String();
            NwName = serviceState.getOperatorAlphaLong();

            if (TextUtils.isEmpty(NwName) && serviceState.getOperatorAlphaShort() != null) {
                setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaShort());
            } else {
                setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
            }
        } else if ("KDDI".equals(operator_code)) {
            String mNwName = new String();
            mNwName = serviceState.getOperatorAlphaLong();

            if (TextUtils.isEmpty(mNwName)) {
                mNwName = serviceState.getOperatorAlphaShort();
            }
            if ("KDDI".equals(mNwName)) {
                mNwName = "au";
            }
            Log.d(LOG_TAG, "mNwName = " + mNwName);
            setSummaryText(KEY_OPERATOR_NAME, mNwName);
        }
        /* LGE_CHANGE_E : TRACFONE_ONS */
        /* LGSI_CHANGE_S: region Code
        * 2012-04-04,sharath.gosavi@lge.com,
        * "target_country" is replaced with "target_region" to Applicable for all the operators in that region
        */
        /* LGSI_CHANGE_S: region code AME added for Umniah display
        * 2012-04-23,sharath.gosavi@lge.com,
        * AME regioncode is added
        */
        //jing01.ma@lge.com add for HK Plmn display error 2013.3.9
        else if (regioncode.compareTo("ESA") == 0 || regioncode.compareTo("AME") == 0
                || (Config.getCountry()).equals("HK") || (Config.getCountry()).equals("TW")) {
            Log.d(LOG_TAG, "In UpdateServiceState executing ESA and AME Code for Single SIM");

            /* LGSI_CHANGE_E: region code AME added for Umniah display */
            /* LGSI_CHANGE_E: region Code */
            /* LGSI_CHANGE_S: chinese and india operators PLMN implementation
            * 2012-04-04,sharath.gosavi@lge.com,
            * Implementation for chinese language when the language of device is taiwan or chinese
            * Network name as FQMS sheet in the status of the phone for india operators
            */

            /*LGSI_CHANGE_S:chinese PLMN implementaion
            2011-12-28,manikanta.varmak@lge.com,
            Implentination for chinese language when the language of device is tawin or chinese*/
            /* LGSI_CHANGE_S: Force close in status if Airplane mode ON
            * 2012-06-05,sharath.gosavi@lge.com,
            * Force close is observed in status after Airplane mode ON
            * Network Status should be displayed as Unknown if the service state is other than STATE_IN_SERVICE
            */
            if (state == ServiceState.STATE_IN_SERVICE) {
                /* LGSI_CHANGE_E: Force close in status if Airplane mode ON */

                String currentLanguage = Locale.getDefault().getLanguage();
                String currentcountry = Locale.getDefault().getCountry();

                //                if (((countrycode.compareTo("ESA") == 0) && (operator_code.equals("OPEN")))&&( "zh".equals(currentLanguage)))
                //                {
                String NwName = new String();

                NwName = serviceState.getOperatorAlphaLong();
                setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
                //[S][2012.04.16][seungeene][Common][NA] remove the code to get imsi from property
                //String Operator_Imsi =SystemProperties.get(SYSTEM_PROPERTY_IMSI);
                String Operator_Imsi = mPhone.getSubscriberId();
                //[E][2012.04.16][seungeene][Common][NA] remove the code to get imsi from property

                /* LGSI_CHANGE_S: Get the mcc and mnc value for Umniah operator
                * 2012-04-23,sharath.gosavi@lge.com,
                * Get the MNC and MCC value for checking of Umniah operator in status of phone
                */
                /* LGSI_CHANGE_S: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming
                * 2012-05-28,sharath.gosavi@lge.com,
                * when West Bengal IDEA SIM is latched to Vodafone network in roaming, Status of network displaying as Unknown
                * mcc and mnc for India region has been taken in mcc_mnc_ind.
                */
                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_Start*/
                String mcc = null;
                String mcc_mnc = null;
                String mcc_mnc_ind = null;
                String mcc_mnc_gid = null;
                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_End*/
                /* LGSI_CHANGE_E: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming */
                if (Operator_Imsi != null && (Operator_Imsi.length() > 7)) {
                    mcc = Operator_Imsi.substring(0, 3);
                    mcc_mnc = Operator_Imsi.substring(0, 5);
                    /* LGSI_CHANGE_S: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming
                    * 2012-05-28,sharath.gosavi@lge.com,
                    * when West Bengal IDEA SIM is latched to Vodafone network in roaming, Status of network displaying as Unknown
                    * mcc and mnc for India region has been taken in mcc_mnc_ind.
                    */
                    mcc_mnc_ind = Operator_Imsi.substring(0, 6);
                    /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_Start*/
                    mcc_mnc_gid = Operator_Imsi.substring(0, 7);
                    /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_End*/
                    /* LGSI_CHANGE_E: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming */
                }
                /* LGSI_CHANGE_S: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming
                * 2012-05-28,sharath.gosavi@lge.com,
                * when West Bengal IDEA SIM is latched to Vodafone network in roaming, Status of network displaying as Unknown
                * get the operator numeric mcc-mnc in cellinfo.
                */
                String cellinfo = serviceState.getOperatorNumeric();

                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_Start*/
                if (DBG) {
                    Log.d(LOG_TAG, "In UpdateServiceState before processing NwName " + NwName
                            + "Operator_Imsi" + Operator_Imsi + "cellinfo" + cellinfo);
                    Log.d(LOG_TAG, "In UpdateServiceState before processing mcc_mnc " + mcc_mnc
                            + "mcc" + mcc + "mcc_mnc_ind" + mcc_mnc_ind
                            + "serviceState.getRoaming()" + serviceState.getRoaming());
                }
                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_End*/

                /* LGSI_CHANGE_E: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming */
                /* LGSI_CHANGE_E: Get the mcc and mnc value for Umniah operator */

                //lolly.liu 20141125 modify HK plmn
                String spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA);
                Log.d(LOG_TAG, "Enter default spn: " + spn + "mcc :" + mcc_mnc );

                if (mcc_mnc != null && (mcc_mnc.equals("45400") || mcc_mnc.equals("45402") ||
                                    mcc_mnc.equals("45418") || mcc_mnc.equals("45410") || mcc_mnc.equals("45416") || mcc_mnc.equals("45419"))) {
                    if (!TextUtils.isEmpty(spn))
                    {
                        NwName = spn;
                    } else {
                        NwName = "CSL";
                    }
                }
                if ("zh".equals(currentLanguage)
                        && (mcc != null && ("454".equals(mcc) || "455".equals(mcc) || "466"
                                .equals(mcc)))) {
                    if ("TW".equals(currentcountry)) {
                        if (NwName.equalsIgnoreCase("Chunghwa")
                                || NwName.equalsIgnoreCase("Chunghwa Telecom")) {
                            char SPN_chars[] = { 0x4E2D, 0x83EF, 0x96FB, 0x4FE1 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("VIBO") || "46689".equals(mcc_mnc) || NwName.equalsIgnoreCase("T Star")) {
                            char SPN_chars[] = { 0x53F0, 0x7063, 0x4E4B, 0x661F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("TW Mobile")
                                || NwName.equalsIgnoreCase("TWN GSM")
                                || NwName.equalsIgnoreCase("TRANSASIA")
                                || NwName.equalsIgnoreCase("MOBITAI")
                                || NwName.equalsIgnoreCase("TWN TransAsia Telecom GSM")
                                || NwName.equalsIgnoreCase("TWN MOBITAI")
                                || NwName.equalsIgnoreCase("TWM")) {
                            char SPN_chars[] = { 0x53F0, 0x6E7E, 0x5927, 0x54E5, 0x5927 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("Far EasTone")
                                || NwName.equalsIgnoreCase("Far EasTone")
                                || NwName.equalsIgnoreCase("KG Telecom")
                                || NwName.equalsIgnoreCase("KGT-ONLINE")
                                || NwName.equalsIgnoreCase("KGT Online")
                                || NwName.equalsIgnoreCase("KG Telecom")
                                || NwName.equalsIgnoreCase("FET")) {
                            char SPN_chars[] = { 0x9060, 0x50B3, 0x96FB, 0x4FE1 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        }
                        /* LGSI_P970_TD :77724_ESA_AMITKUMAR_START: For Twn Chinese the CMHK String shld be same HKG Chinese._(22.02.11)*/
                        else if (NwName.equalsIgnoreCase("China Mobile HK")
                                || NwName.equalsIgnoreCase("CMHK")) {
                            char SPN_chars[] = { 0x4E2D, 0x570B, 0x79FB,
                                    0x52D5, 0x9999, 0x6E2F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (mcc_mnc.equals("45403") || mcc_mnc.equals("45404")) {
                            if (NwName.equals("3 (2G)")) {
                                NwName = "3(2G)";
                            }
                        }
                        /* LGSI_P970_TD :77724_ESA_AMITKUMAR_END_(22.02.11) */
                        setSummaryText(KEY_OPERATOR_NAME, NwName);
                    } else if ("HK".equals(currentcountry)) {
                        if (NwName.equalsIgnoreCase("China Mobile HK")
                                || NwName.equalsIgnoreCase("CMHK")) {
                            char SPN_chars[] = { 0x4E2D, 0x570B, 0x79FB, 0x52D5, 0x9999, 0x6E2F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("CTM") || NwName.equalsIgnoreCase("ctm")) {
                            char SPN_chars[] = { 0x6FB3, 0x9580, 0x96FB, 0x8A0A };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (mcc_mnc.equals("45403") || mcc_mnc.equals("45404")) {
                            if (NwName.equals("3 (2G)")) {
                                NwName = "3(2G)";
                            }
                        }
                        setSummaryText(KEY_OPERATOR_NAME, NwName);
                    } else if ("CN".equals(currentcountry)) {
                        if (NwName.equalsIgnoreCase("China Mobile HK")
                                || NwName.equalsIgnoreCase("CMHK")) {
                            char SPN_chars[] = { 0x4E2D, 0x56FD, 0x79FB, 0x52A8, 0x9999, 0x6E2F };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("CTM")) {
                            char SPN_chars[] = { 0x6FB3, 0x95E8, 0x7535, 0x8BAF };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (mcc_mnc.equals("46689") || NwName.equalsIgnoreCase("VIBO") || NwName.equalsIgnoreCase("T Star")) {
                            char SPN_chars[] = { 0x53F0, 0x6E7E, 0x4E4B, 0x661F };
                            String VIBO_SPN = new String(SPN_chars);
                            NwName = VIBO_SPN;
                        } else if (NwName.equalsIgnoreCase("Far EasTone")
                                || NwName.equalsIgnoreCase("FET")) {
                            char SPN_chars[] = { 0x8FDC, 0x4F20, 0x7535, 0x4FE1 };
                            String FET_SPN = new String(SPN_chars);
                            NwName = FET_SPN;
                        } else if (NwName.equalsIgnoreCase("TW Mobile")
                                || NwName.equalsIgnoreCase("TWN GSM")
                                || NwName.equalsIgnoreCase("TRANSASIA")
                                || NwName.equalsIgnoreCase("MOBITAI")
                                || NwName.equalsIgnoreCase("TWN TransAsia Telecom GSM")
                                || NwName.equalsIgnoreCase("TWN MOBITAI")
                                || NwName.equalsIgnoreCase("TWM")) {
                            char SPN_chars[] = { 0x53F0, 0x6E7E, 0x5927, 0x54E5, 0x5927 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (NwName.equalsIgnoreCase("Chunghwa")
                                || NwName.equalsIgnoreCase("Chunghwa Telecom")) {
                            char SPN_chars[] = { 0x4E2D, 0x534E, 0x7535, 0x4FE1 };
                            String SPN = new String(SPN_chars);
                            NwName = SPN;
                        } else if (mcc_mnc.equals("45403") || mcc_mnc.equals("45404")) {
                            if (NwName.equals("3 (2G)")) {
                                NwName = "3(2G)";
                            }
                        }
                        setSummaryText(KEY_OPERATOR_NAME, NwName);
                    }
                }
                /*
                * LGEYT zhaofeng.yang add for TW PLMN issue 20130830
                * under English language
                */
                //weiyt.jiang@lge.com 2014.4.18 modify Chunghwa display in English for settings-network[s]
                else if (mcc_mnc != null && mcc != null && ("466".equals(mcc))) {
                    if ("en".equals(currentLanguage) && mcc_mnc.equals("46689")) {
                        NwName = "T Star";
                    } else if ("en".equals(currentLanguage) && mcc_mnc.equals("46692")) {
                        if (NwName.equalsIgnoreCase("Chunghwa Telecom")) {
                            NwName = "Chunghwa";
                        }
                    }
                    if ("en".equals(currentLanguage)
                            && NwName.equalsIgnoreCase("TWN TransAsia Telecom GSM")
                            || NwName.equalsIgnoreCase("TWN MOBITAI")
                            || NwName.equalsIgnoreCase("TWM")) {
                        NwName = "TW Mobile";
                    } else if ("en".equals(currentLanguage) && NwName.equalsIgnoreCase("FET")) {
                        NwName = "Far EasTone";
                    }
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                } else if (mcc != null && ("454".equals(mcc))) {
                    if (mcc_mnc.equals("45403") || mcc_mnc.equals("45404")) {
                        if (NwName.equals("3 (2G)")) {
                            NwName = "3(2G)";
                        }
                    } else if (mcc_mnc.equals("45412") || mcc_mnc.equals("45413")) {
                        NwName = "China Mobile HK";
                    }
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                //weiyt.jiang@lge.com 2014.4.18 modify Chunghwa display in English for settings-network[e]
                else if (mcc_mnc != null  && mcc != null && ("434".equals(mcc))) {
                    if (mcc_mnc.equals("43404")) {
                        NwName = "Beeline-Uz";
                    }
                    if (mcc_mnc.equals("43405")) {
                        NwName = "Ucell";
                    }
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /*
                * LGSI_CHANGE_S: Network name as FQMS sheet in the status of
                * the phone 2012-04-04,sharath.gosavi@lge.com, Network name as
                * FQMS sheet in the status of the phone
                */
                else if (mcc != null && (("404".equals(mcc)) || ("405".equals(mcc)))) {
                    //[LGSI][TdId:196529]NwName being null[swapnil.shrivastava@lge.com][9.5.2014][start]
                    if ("UNINOR".equalsIgnoreCase(NwName)) {
                        NwName = "Uninor";
                    }
                    /*
                    * LGSI_CHANGE_S: airtel network displaying as "IND airtel"
                    * 2012-07-27,sharath.gosavi@lge.com, In phone status
                    * network name has to display "airtel"
                    */
                    // else if(NwName.equalsIgnoreCase("Airtel")){
                    else if (("Airtel".equalsIgnoreCase(NwName))
                            || ("IND airtel".equalsIgnoreCase(NwName))) {
                        /* LGSI_CHANGE_E: airtel network displaying as "IND airtel" */
                        NwName = "airtel";
                    }
                    /* LGSI_CHANGE_S: PLMN displaying as VODAFONE in Mumbai as short name is coming only
                     * 2012-12-05,arshad.mumtaz@lge.com,
                     * In Mumbai only short operator name is coming as VODAFONE
                     */
                    else if ("VODAFONE".equalsIgnoreCase(NwName)) {
                        NwName = "Vodafone IN";
                    }
                    /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_Start*/
                    else if ("IDEA".equalsIgnoreCase(NwName)) {
                        NwName = "Idea";
                    }
                    //[LGSI][TdId:196529]NwName being null[swapnil.shrivastava@lge.com][9.5.2014][end]
                    /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_End*/
                    /* LGSI_CHANGE_S: PLMN displaying as VODAFONE in Mumbai as short name is coming only */
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* LGSI_CHANGE_E: Network name as FQMS sheet in the status of the phone */
                /* LGSI_CHANGE_S: Umniah network displaying as "Un known"
                * 2012-04-23,sharath.gosavi@lge.com,
                * In phone status network name has to display "Umniah"
                */
                else if (mcc_mnc != null && "41603".equals(mcc_mnc)) {
                    if (state == ServiceState.STATE_IN_SERVICE
                            && serviceState.getRoaming() == false) {
                        NwName = "Umniah";
                    }
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                } else {
                    // [LTE-DATA]jinseok83.kim : Operator name display
                    if (!TextUtils.isEmpty(serviceState.getOperatorAlphaLong())) {
                        setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong()); //QCT SBA 53404001 CR#240559
                    } else {
                        setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaShort());
                    }

                    Log.d(LOG_TAG, "[NWINFO_Debug] serviceState.getOperatorAlphaLong = "
                            + serviceState.getOperatorAlphaLong());
                    Log.d(LOG_TAG, "[NWINFO_Debug] serviceState.getOperatorAlphaShort = "
                            + serviceState.getOperatorAlphaShort());
                    // [LTE-DATA]jinseok83.kim : Operator name display
                }
                /* LGSI_CHANGE_E: Umniah network displaying as "Un known" */
                /* LGSI_CHANGE_S: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming
                * 2012-05-28,sharath.gosavi@lge.com,
                * when West Bengal IDEA SIM is latched to Vodafone network in roaming, the Status is displayed as Unknown
                * get the operator numeric mcc-mnc in cellinfo.
                */
                if ((state == ServiceState.STATE_IN_SERVICE && serviceState.getRoaming() == true)
                        && (cellinfo != null && mcc_mnc_ind != null)
                        && (("405853".equals(mcc_mnc_ind)) && (cellinfo.compareTo("40430") == 0))) {
                    NwName = "Vodafone IN";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* LGSI_CHANGE_E: Status of network displaying as Unknown when West Bengal IDEA SIM latched to Vodafone in roaming */
                /* LGSI_CHANGE_S: Vodafone Karnataka displaying in Junk character in Status
                * 2012-06-06,sharath.gosavi@lge.com,
                * Network Status for Vodafone karnataka is changed to Vodafone IN
                */
                if (mcc_mnc != null && "40486".equals(mcc_mnc)) {
                    NwName = "Vodafone IN";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_Start*/
                /*
                  * LGSI_CHANGE_S: Network Name Display
                  * 2013-02-18,vinay.gc25@lge.com,
                  * Network Name Display based on operator name from PLMN
                  */
                if (mcc_mnc != null && "45207".equals(mcc_mnc)) {
                    NwName = "Gmobile";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* LGSI_CHANGE_E: Network Name Display based on operator name from PLMN */
                /* LGSI_CHANGE_E: Vodafone Karnataka displaying in Junk character in Status */
                /*LGSI_CHANGE_E:chinese PLMN implementaion*/
                /*
                * LGSI_CHANGE_S:Thai Network Name Display in Status Info
                * 2012-02-18,amit3g.kumar@lge.com,
                * Thai Network Name Display in Status info
                */
                if (mcc != null && ("520".equals(mcc))
                        && ("TRUE-H/my".equalsIgnoreCase(NwName))) {
                    if ((mcc_mnc_gid != null) && ("5200020".equals(mcc_mnc_gid))) {
                        NwName = "TRUE-H";
                    } else if ((mcc_mnc_gid != null) && ("5200019".equals(mcc_mnc_gid))) {
                        NwName = "my";
                    }
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for TOT 3G is displayed as TOT Mobile , should be TOT 3G */
                //[LGSI][TdId:196529]NwName being null[swapnil.shrivastava@lge.com][9.5.2014][start]
                if (mcc_mnc != null && "52015".equals(mcc_mnc)
                        && ("TOT Mobile".equalsIgnoreCase(NwName))) {
                    NwName = "TOT 3G";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for RED BULL [ MVNO : Cell C] is displayed as CELL C , should be RED BULL */
                if (mcc_mnc_gid != null && "6550713".equals(mcc_mnc_gid)
                        && ("Cell C".equalsIgnoreCase(NwName))) {
                    NwName = "Red Bull";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for Maroc Telecom is displayed as IAM , should be Maroc Telecom */
                if (mcc != null && "604".equals(mcc) && ("IAM".equalsIgnoreCase(NwName))) {
                    NwName = "Maroc Telecom";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* LGSI_TELEPHONY_TD#198764_operator_name for dtac is displayed as DTAC , should be dtac */
                if (mcc != null && "520".equals(mcc) && ("dtac".equalsIgnoreCase(NwName))) {
                    NwName = "dtac";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for Loop Mobile is displayed as BPL Mobile , should be Loop Mobile */
                if (mcc != null && "404".equals(mcc) && ("BPL Mobile".equalsIgnoreCase(NwName))) {
                    NwName = "Loop Mobile";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for TELKOMSEL is displayed as T-SEL/TELKOMSE , should be TELKOMSEL */
                if (mcc != null
                        && "510".equals(mcc)
                        && ("TELKOMSE".equalsIgnoreCase(NwName) || "T-SEL".equalsIgnoreCase(NwName))) {
                    NwName = "TELKOMSEL";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for StarHub is displayed as STARHUB , should be StarHub */
                if (mcc != null && "525".equals(mcc) && ("STARHUB".equalsIgnoreCase(NwName))) {
                    NwName = "StarHub";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /* NwName for Vodafone TR is displayed as VODAFONE TR , should be VODAFONE TR */
                if (mcc != null && "286".equals(mcc) && ("VODAFONE TR".equalsIgnoreCase(NwName))) {
                    NwName = "Vodafone TR";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                /*LGSI_CHANGE_START giribabu.gogineni
                2013-09-04 South_Africa_Requirement_TelkomSA_Instead_Of_8.SA*/
                if (mcc_mnc != null && "65502".equals(mcc_mnc)
                        && "8.ta".equalsIgnoreCase(NwName)) {
                    NwName = "TelkomSA";
                    setSummaryText(KEY_OPERATOR_NAME, NwName);
                }
                //[LGSI][TdId:196529]NwName being null[swapnil.shrivastava@lge.com][9.5.2014][end]
                /*LGSI_CHANGE_END giribabu.gogineni
                2013-09-04 South_Africa_Requirement_TelkomSA_Instead_Of_8.SA*/
                /*LGSI_CHANGE_E:Thai Network Name Display in Manual search*/
                /*LGP875_HDB_SPN_PLMN_Issues_Mahendra_End*/
                if ("CN".equals(Config.getCountry())) {
                    String curNetwork = serviceState.getOperatorNumeric();
                    if ((curNetwork != null)
                            && ((curNetwork.compareTo("46000") == 0)
                                    || (curNetwork.compareTo("46002") == 0)
                                    || (curNetwork.compareTo("46007") == 0)
                                    || (curNetwork.compareTo("CMCC") == 0)
                                    || (curNetwork.compareTo("CMO") == 0))) {
                        setSummaryText(KEY_OPERATOR_NAME,
                                getString(R.string.sp_CMCC_China_Mobile_SHORT));
                    } else if ((curNetwork != null)
                            && ((curNetwork.compareTo("46001") == 0) || (curNetwork
                                    .compareTo("CHN-CUGSM") == 0))) {
                        setSummaryText(KEY_OPERATOR_NAME,
                                getString(R.string.sp_CUCC_China_Telecom_SHORT));
                    }
                }
                /* LGSI_CHANGE_S: Force close in status if Airplane mode ON
                * 2012-06-05,sharath.gosavi@lge.com,
                * Force close is observed in status after Airplane mode ON
                * Network Status should be displayed as Unknown if the service state is other than STATE_IN_SERVICE
                */
                Log.d(LOG_TAG, "In UpdateServiceState after Processing NwName" + NwName);
            } else {
                setSummaryText(KEY_OPERATOR_NAME, serviceState.getOperatorAlphaLong());
            }
            /* LGSI_CHANGE_E: Force close in status if Airplane mode ON */
        }

        //OPEN_CN_Settings chaoying.hou@lge.com 2012-02-24 for network = CMCC/CHN-CUGSM. [START]
        /* LGSI_CHANGE_S: region Code
        * 2012-04-04,sharath.gosavi@lge.com,
        * "target_country" is replaced with "target_region" to Applicable for all the operators in that region
        *///20121211 lolly.liu@lge.com modify for Chinese operator
        else if ((Config.getCountry()).equalsIgnoreCase("CN")) {
            /* LGSI_CHANGE_E: region Code */
            String operatiorName = "";
            if (serviceState.getOperatorAlphaLong() != null) {
                if (serviceState.getOperatorAlphaLong().equals("CMCC")
                        || serviceState.getOperatorAlphaLong().equals("CMO")
                        || serviceState.getOperatorAlphaLong().equalsIgnoreCase("CHINA MOBILE")) {
                    operatiorName = getString(R.string.sp_CMCC_China_Mobile_SHORT);
                } else if (serviceState.getOperatorAlphaLong().equals("CHN-CUGSM")
                        || serviceState.getOperatorAlphaLong().equals("CHN-UNICOM")
                        || serviceState.getOperatorAlphaLong().equals("CUCC")) {
                    operatiorName = getString(R.string.sp_CUCC_China_Telecom_SHORT);
                } else {
                    operatiorName = serviceState.getOperatorAlphaLong();
                }
                setSummaryText(KEY_OPERATOR_NAME, operatiorName);
            } else {
                setSummaryText(KEY_OPERATOR_NAME,
                        serviceState.getOperatorAlphaLong());
            }
        }
        //20130201 lolly.liu@lge.com for TWN operator T Chinese [Start]
        else if ((Config.getCountry()).equals("TW")) {
            String NwName = new String();

            NwName = serviceState.getOperatorAlphaLong();
            if (("zh".equals(Locale.getDefault().getLanguage())) && NwName != null) {
                if (NwName.equalsIgnoreCase("Chunghwa")) {
                    char SPN_chars[] = { 0x4E2D, 0x83EF, 0x96FB, 0x4FE1 };
                    String SPN = new String(SPN_chars);
                    NwName = SPN;
                } else if (NwName.equalsIgnoreCase("VIBO") || NwName.equalsIgnoreCase("T Star")) {
                    char SPN_chars[] = { 0x53F0, 0x7063, 0x4E4B, 0x661F };
                    String SPN = new String(SPN_chars);
                    NwName = SPN;
                } else if (NwName.equalsIgnoreCase("TW Mobile")
                        || NwName.equalsIgnoreCase("TWN GSM")
                        || NwName.equalsIgnoreCase("TRANSASIA")
                        || NwName.equalsIgnoreCase("MOBITAI")) {
                    char SPN_chars[] = { 0x53F0, 0x6E7E, 0x5927, 0x54E5, 0x5927 };
                    String SPN = new String(SPN_chars);
                    NwName = SPN;
                } else if (NwName.equalsIgnoreCase("Far EasTone")
                        || NwName.equalsIgnoreCase("Far EasTone")
                        || NwName.equalsIgnoreCase("KG Telecom")
                        || NwName.equalsIgnoreCase("KGT-ONLINE")
                        || NwName.equalsIgnoreCase("KGT Online")
                        || NwName.equalsIgnoreCase("KG Telecom")) {
                    char SPN_chars[] = { 0x9060, 0x50B3, 0x96FB, 0x4FE1 };
                    String SPN = new String(SPN_chars);
                    NwName = SPN;
                }
                /* LGSI_P970_TD :77724_ESA_AMITKUMAR_START: For Twn Chinese the CMHK String shld be same HKG Chinese._(22.02.11)*/
                else if (NwName.equalsIgnoreCase("China Mobile HK")
                        || NwName.equalsIgnoreCase("CMHK") || NwName.equalsIgnoreCase("PEOPLES")) {
                    char SPN_chars[] = { 0x4E2D, 0x570B, 0x79FB,
                            0x52D5, 0x9999, 0x6E2F };
                    String SPN = new String(SPN_chars);
                    NwName = SPN;
                }
                /* LGSI_P970_TD :77724_ESA_AMITKUMAR_END_(22.02.11) */
                setSummaryText(KEY_OPERATOR_NAME, NwName);
            }
            else {
                setSummaryText(KEY_OPERATOR_NAME, NwName);
            }
        }
        //20130201 lolly.liu@lge.com for TWN operator T Chinese [End]
        else if (serviceState.getOperatorAlphaLong() != null &&
                serviceState.getOperatorAlphaLong().equals("mobilcom-debitel")) {
            String mNwName = new String();
            mNwName = serviceState.getOperatorAlphaLong();
            String mNummeric = serviceState.getOperatorNumeric();

            if (mNummeric != null && "26201".equals(mNummeric)) {
                mNwName = "Telekom.de";
            }

            if (mNummeric != null && "26202".equals(mNummeric)) {
                mNwName = "Vodafone.de";
            }

            if (mNummeric != null && "26203".equals(mNummeric)) {
                mNwName = "E-Plus";
            }
            setSummaryText(KEY_OPERATOR_NAME, mNwName);
        }
        /* LGE_CHANGE_S giribabu.gogineni - Network name 3SE Display instead of 3 */
        /*LGE_CHANGE_S prabhakara.pediredia - Network name o2 - uk display instead of Tesco*/
        else if (serviceState.getOperatorAlphaLong() != null &&
            serviceState.getOperatorAlphaLong().equalsIgnoreCase("O2 - UK")) {
            String mNwName = new String();
            mNwName = serviceState.getOperatorAlphaLong();
            String simImsi = mPhone.getSubscriberId();
            if ((null != simImsi && simImsi.length() >= 5) && simImsi.substring(0, 5).equalsIgnoreCase("23410"))
            {
                String gid = new com.lge.uicc.LGUiccCard().getGid1();
                if (null != gid && gid.length() >= 3 && gid.substring(0, 2).equalsIgnoreCase("0a"))
                {
                    mNwName = "TESCO";
                }
            }
            setSummaryText(KEY_OPERATOR_NAME, mNwName);
        }
        /*LGE_CHANGE_E prabhakara.pediredia - Network name o2 - uk display instead of Tesco*/
        else if (serviceState.getOperatorAlphaLong() != null &&
                serviceState.getOperatorAlphaLong().equals("3")) {
            String mNwName = new String();
            mNwName = serviceState.getOperatorAlphaLong();
            String sim_imsi = mPhone.getSubscriberId();
            String mNummeric = serviceState.getOperatorNumeric();
            if (sim_imsi.length() >= 5 && sim_imsi.substring(0, 5).equals("24002"))
            {
                if (mNummeric != null && "24002".equals(mNummeric)) {
                    mNwName = "3SE";
                }
                if (mNummeric != null && "24004".equals(mNummeric)) {
                    mNwName = "3SE";
                }
            }
            setSummaryText(KEY_OPERATOR_NAME, mNwName);
        } else if (serviceState.getOperatorAlphaLong() != null &&
                serviceState.getOperatorAlphaLong().equals("congstar")) {
            String mNwName = new String();
            mNwName = serviceState.getOperatorAlphaLong();
            String mNummeric = serviceState.getOperatorNumeric();

            if (mNummeric != null && "26201".equals(mNummeric)) {
                mNwName = "Telekom.de";
            }

            if (mNummeric != null && "26202".equals(mNummeric)) {
                mNwName = "Telekom.de";
            }
            setSummaryText(KEY_OPERATOR_NAME, mNwName);
        } else if (tele_sweden != null && tele_sweden.length() >= 3
                && tele_sweden.substring(0, 3).equals("214")) {
            String numeric = SystemProperties
                    .get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);

            TelephonyManagerEx telephonyMgrEx 
                    = (TelephonyManagerEx)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = numeric + telephonyMgrEx.getMSIN();

            Log.d(LOG_TAG,
                    "serviceState.getOperatorAlphaLong() : " + serviceState.getOperatorAlphaLong());
            if ("21406".equals(numeric)) {
                if ("2140612".equals(imsi.substring(0, 7))) {
                    if (serviceState.getOperatorAlphaLong() != null) {
                        // && serviceState.getOperatorAlphaLong().equalsIgnoreCase("vodafone ES")) {
                        setSummaryText(KEY_OPERATOR_NAME, "MobilR");
                    } else {
                        setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
                    }
                } else if ("2140613".equals(imsi.substring(0, 7))) {
                    if (serviceState.getOperatorAlphaLong() != null) {
                        // && serviceState.getOperatorAlphaLong().equalsIgnoreCase("vodafone ES")) {
                        setSummaryText(KEY_OPERATOR_NAME, "Telecable");
                    } else {
                        setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
                    }
                } else {
                    setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
                }
            } else if ("21421".equals(numeric)) {
                setSummaryText(KEY_OPERATOR_NAME, "Jazztel");
            } else if ("2140352".equals(imsi.substring(0, 7))) {
                if (serviceState.getOperatorAlphaLong() != null
                        && serviceState.getOperatorAlphaLong().equalsIgnoreCase("Orange")) {
                    setSummaryText(KEY_OPERATOR_NAME, "Carrefour");
                } else {
                    setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
                }
            } else if (serviceState.getOperatorAlphaLong() != null &&
                    serviceState.getOperatorAlphaLong().equals("Movistar")) {
                String mNummeric = serviceState.getOperatorNumeric();
                if (mNummeric != null && "214018".equals(mNummeric)) {
                    setSummaryText(KEY_OPERATOR_NAME, "ONO");
                } else {
                    setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
                }
            } else {
                setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
            }
        } else {
            if (state == ServiceState.STATE_OUT_OF_SERVICE) {
                setSummaryText(KEY_OPERATOR_NAME, mRes.getString(R.string.sp_searching_for_service));
            } else {
                setSummaryText("operator_name", serviceState.getOperatorAlphaLong());
            }
        }
        //OPEN_CN_Settings chaoying.hou@lge.com 2012-02-24 for network = CMCC/CHN-CUGSM. [END]
        /* LGSI_CHANGE_E: chinese and india operators PLMN implementation */
    }

    private void updateAreaInfo(String areaInfo) {
        if (areaInfo != null) {
            setSummaryText(KEY_LATEST_AREA_INFO, areaInfo);
        }
    }

    private boolean is2G(int RAT) {
        switch (RAT) {
            case ServiceState.RIL_RADIO_TECHNOLOGY_GSM:
            case ServiceState.RIL_RADIO_TECHNOLOGY_GPRS:
            case ServiceState.RIL_RADIO_TECHNOLOGY_EDGE:
            case ServiceState.RIL_RADIO_TECHNOLOGY_IS95A:
            case ServiceState.RIL_RADIO_TECHNOLOGY_IS95B:
            case ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT:
                return true;
            default:
                return false;
        }
    }

    void updateSignalStrength() {
        // TODO PhoneStateIntentReceiver is deprecated and PhoneStateListener
        // should probably used instead.
        int state = 0;
        int datastate = 0;
        int signalDbm = 0;
        int signalAsu = 0;
        final int INVALID_DBM = 0x7FFFFFFF;
        final int INVALID_ASU = 0xFF;
        boolean isCTC_LTE = false;

        // not loaded in some versions of the code (e.g., zaku)
        if (mSignalStrength != null) {
            // [2012.12.31][munjohn.kang] added a triple SIM condition
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                state = OverlayUtils.get_service_state(getApplicationContext());
                if ("CTC".equals(Config.getOperator()) || "CTO".equals(operator_code)) {
                    signalDbm = MSIMUtils.get_signal_strength_Dbm_ctc(mCtcSlotID,
                            mPhoneStateReceiver);
                    signalAsu = MSIMUtils.get_signal_strength_Asu_ctc(mCtcSlotID,
                            mPhoneStateReceiver);
                    if (mCtcSlotID == 0) {
                        if (mRadioTech > -1 && mVoiceTech > -1) {
                            if (is2G(mVoiceTech)
                                && mRadioTech == ServiceState.RIL_RADIO_TECHNOLOGY_LTE) {
                                isCTC_LTE = true;
                            }
                        }
                    }
                } else {
                    signalDbm = MSIMUtils.get_signal_strength_Dbm(getApplicationContext(),
                            mPhoneStateReceiver);
                    signalAsu = MSIMUtils.get_signal_strength_Asu(getApplicationContext(),
                            mPhoneStateReceiver);
                }
            } else {
                if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                    state = OverlayUtils.get_service_state(getApplicationContext());
                } else {
                    state = mPhoneStateReceiver.getServiceState().getState();
                }
                datastate = mPhone.getServiceState().getDataRegState();
                signalDbm = mPhoneStateReceiver.getSignalStrengthDbm();
                signalAsu = mPhoneStateReceiver.getSignalStrengthLevelAsu();
                Log.d("starmotor", "signalDbm = " + signalDbm);
                Log.d("starmotor", "signalAsu = " + signalAsu);
                Log.d("starmotor", "mSignalStrength_LTE = " + mSignalStrength_LTE);
                if (mSignalStrength_LTE != null) {
                    if ("SPR".equals(operator_code) || "USC".equals(operator_code)) {
                        Log.d("starmotor", "sNetworkType = " + sNetworkType);
                        Log.d("starmotor", "csActive = " + csActive);
                        if (csActive) {
                            signalDbm = mSignalStrength_LTE.getCdmaDbm();
                            signalAsu = mSignalStrength_LTE.getCdmaAsuLevel();
                        } else {
                            if (sNetworkType.equalsIgnoreCase("LTE")
                                    || sNetworkType.equalsIgnoreCase("4G")) {
                                signalDbm = mSignalStrength_LTE.getLteDbm();
                                signalAsu = mSignalStrength_LTE.getLteAsuLevel();
                            } else if (sNetworkType.equalsIgnoreCase("1xRTT")) {
                                signalDbm = mSignalStrength_LTE.getCdmaDbm();
                                signalAsu = mSignalStrength_LTE.getCdmaAsuLevel();
                            } else if (sNetworkType.equalsIgnoreCase("EvDo-rev.0")
                                    || sNetworkType.equalsIgnoreCase("EvDo-rev.A")
                                    || sNetworkType.equalsIgnoreCase("EvDo-rev.B")) {
                                signalDbm = mSignalStrength_LTE.getEvdoDbm();
                                signalAsu = mSignalStrength_LTE.getEvdoAsuLevel();
                            } else {
                                Log.d("starmotor", "signalAsu_lte = " + signalAsu);
                                Log.d("starmotor", "signalDbm_lte = " + signalDbm);
                            }
                        }
                        if (signalDbm == INVALID_DBM) {
                            signalDbm = 0;
                        }
                        if (signalAsu == INVALID_ASU) {
                            signalAsu = 0;
                        }
                    }
                }
            }

            if ((state == ServiceState.STATE_OUT_OF_SERVICE)
                    && (datastate == ServiceState.STATE_OUT_OF_SERVICE)) {
                //    mSignalStrength.setSummary("0");
                signalDbm = 0;
                signalAsu = 0;
            }

            if (signalDbm == -1) {
                signalDbm = 0;
            }
            if (signalAsu == -1) {
                signalAsu = 0;
            }

            //The ase value is not avalilable in CDMA network, so does not show it in phone info.
            if (("USC".equals(operator_code) || "SPR".equals(operator_code))
                    && mPhone.getPhoneName().equals("CDMA")) {
                int networkType = TelephonyManager.getDefault().getNetworkType();
                switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    mSignalStrength.setSummary(String.valueOf(signalDbm) + " "
                            + getResources().getString(R.string.radioInfo_display_dbm));
                    break;

                default:
                    mSignalStrength.setSummary(String.valueOf(signalDbm) + " "
                            + getResources().getString(R.string.radioInfo_display_dbm) + "   "
                            + String.valueOf(signalAsu) + " "
                            + getResources().getString(R.string.radioInfo_display_asu));
                    break;
                }
            } else {
                String strengthSummary = String.valueOf(signalDbm) + " "
                        + getResources().getString(R.string.radioInfo_display_dbm) + "   "
                        + String.valueOf(signalAsu) + " "
                        + getResources().getString(R.string.radioInfo_display_asu);

                // LTE strength
                if (isCTC_LTE && mSignalStrength_LTE != null) {
                    int lteRsrp = mSignalStrength_LTE.getLteRsrp();
                    int lteSnr = mSignalStrength_LTE.getLteRssnr();
                    mSignalStrength.setSummary("CDMA " + strengthSummary + "\n" + "LTE " + String.valueOf(lteRsrp) + " "
                            + getResources().getString(R.string.radioInfo_display_dbm) + "   "
                            + String.valueOf(lteSnr) + " "
                            + getResources().getString(R.string.radioInfo_display_asu));
                } else {
                    mSignalStrength.setSummary(strengthSummary);
                }
            }
        }
    }

    private void setWimaxStatus() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        if (ni == null) {
            PreferenceScreen root = getPreferenceScreen();
            Preference ps = (Preference)findPreference(KEY_WIMAX_MAC_ADDRESS);
            if (ps != null) {
                root.removePreference(ps);
            }
        } else {
            Preference wimaxMacAddressPref = findPreference(KEY_WIMAX_MAC_ADDRESS);
            String macAddress = SystemProperties.get("net.wimax.mac.address",
                    getString(R.string.status_unavailable));
            wimaxMacAddressPref.setSummary(macAddress);
        }
    }

    private void setWifiStatus() {
        Log.i(LOG_TAG, "setWifiStatus");

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo == null) {
            Log.i(LOG_TAG, "setWifiStatus , wifiInfo is NULL !!!");
        }

        Preference wifiMacAddressPref = findPreference(KEY_WIFI_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();

        if (wifiMacAddressPref != null) {
            // [2012.02.11][seungeene][Common][QE_130489] Change mac-address to upper case
            wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress) ? macAddress.toUpperCase()
                    : getString(R.string.status_unavailable));
        }

        Preference wifiIpAddressPref = findPreference(KEY_WIFI_IP_ADDRESS);

        String ipAddress = Utils.getDefaultIpAddresses(this);
        if (wifiIpAddressPref != null) {
            if (ipAddress != null) {
                wifiIpAddressPref.setSummary(ipAddress);
            } else {
                wifiIpAddressPref.setSummary(getString(R.string.status_unavailable));
            }
        }
        /*
                String ipAddress = Utils.getWifiIpAddresses(this);
                if (wifiIpAddressPref != null) {
                    if (ipAddress != null) {
                        wifiIpAddressPref.setSummary(ipAddress);
                    } else {
                        wifiIpAddressPref.setSummary(getString(R.string.status_unavailable));
                    }
                }
        */
    }

    private void setBtStatus() {
        Log.i(LOG_TAG, "setBtStatus");

        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        Preference btAddressPref = findPreference(KEY_BT_ADDRESS);

        if (btAddressPref != null) {
            if (bluetooth == null) {
                // device not BT capable
                removePreferenceFromScreen(KEY_BT_ADDRESS);
            } else {
                String address = bluetooth.isEnabled() ? bluetooth.getAddress() : null;
                btAddressPref.setSummary(!TextUtils.isEmpty(address) ? address
                        : getString(R.string.status_unavailable));
            }
        }
    }

    void updateTimes() {
        //long at = SystemClock.uptimeMillis() / 1000;
        long ut = SystemClock.elapsedRealtime() / 1000;

        if (ut == 0) {
            ut = 1;
        }

        if (mUptime != null) {
            mUptime.setSummary(convert(ut));
        }
    }

    private String pad(int n) {
        if (n >= 10) {
            return String.valueOf(n);
        } else {
            return "0" + String.valueOf(n);
        }
    }

    private String convert(long t) {
        int s = (int)(t % 60);
        int m = (int)((t / 60) % 60);
        int h = (int)((t / 3600));
        /*
        /*LGSI_CHANGE_S: Up time in device info option
        2011-03-26,anuj.singhaniya@lge.com,
        Numbers are changed according to the current language*/
        if (Utils.isRTLLanguage()) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
        } else {
            return h + ":" + pad(m) + ":" + pad(s);
        }
        /*LGSI_CHANGE_E: Up time in device info option*/
    }

    void updateSMSC(String smscAddress) {
        if (mSMSC != null) {
            if (smscAddress != null) {
                mSMSC.setSummary(smscAddress);
            } else {
                Log.d(LOG_TAG, "get SMSC fail!");
            }
        }
    }

    void updatePhoneNumber(String mdnValue) {
        if ("SPR".equals(operator_code)) {
            mdnValue = PhoneNumberUtils.formatNumber(mdnValue);
        }
        setSummaryText(KEY_PHONE_NUMBER, mdnValue);
    }

    void updateMINValue(String nvValue) {
        Log.d(LOG_TAG, "updateMINValue nvValue = " + nvValue);
        setSummaryText(KEY_MIN_NUMBER, nvValue);
    }

    void updatePRLValue(String nvValue) {
        Log.d(LOG_TAG, "updatePRLValue nvValue = " + nvValue);
        setSummaryText(KEY_PRL_VERSION, nvValue);
    }

    private void setImsRegistrationStatus() {
        Preference pref = findPreference(KEY_IMS_REGISTRATION_STATUS);

        String text = null;

        //insook.kim 2012.03.20: insert temperately for avioding compile error
        // hwangoo.park@lge.com 2012-03-28 apply the ims registration property
        boolean isRegistered = (SystemProperties.getInt("net.ims.reg", 0) != 0);
        if (isRegistered) {
            text = getString(R.string.sp_registered);
        } else {
            text = getString(R.string.sp_not_registered);
        }

        if (pref != null) {
            pref.setSummary(!TextUtils.isEmpty(text) ? text
                    : getString(R.string.status_unavailable));
        }
    }

    // [S][2012.09.26][munjohn.kang] applied the VZW requirements(eri_version, life_time_call, warranty_date_code) for About-phone.
    private void onWdcLoaded(String dateCode) {
        Log.d(LOG_TAG, "onWdcLoaded dateCode = " + dateCode);

        if (dateCode == null) {
            setSummaryText(KEY_WDC, getString(R.string.status_unavailable));
            return;
        }

        if (!TextUtils.isEmpty(dateCode) && !dateCode.equals("NotActive")) {
            setSummaryText(KEY_WDC, dateCode);
        } else {
            setSummaryText(KEY_WDC, getString(R.string.status_unavailable));
        }
    }

    private void onLifeTimeLoaded(int lifeTime) {
        Log.d(LOG_TAG, "onLifeTimeLoaded lifeTime = " + lifeTime);
        //[START_LGE_VOICECALL], MOD, jongwany.lee   2012-11-26 : LifeTime convert is changed due to VS930 OS upgrade issue.
        if ("VS930 4G".equals(SystemProperties.get("ro.product.model"))) {
            setSummaryText(KEY_LIFETIME_CALL, convert(lifeTime));
        } else {
            setSummaryText(KEY_LIFETIME_CALL, convertforLifetime(lifeTime));
        }
        //[END_LGE_VOICECALL], MOD, jongwany.lee   2012-11-26 : LifeTime convert is changed due to VS930 OS upgrade issue.
    }

    private String convertforLifetime(long t) {
        Log.d(LOG_TAG, "convertforLifetime t = " + t);
        int c_hour = (int)(t / 3600000);
        int c_minute = (int)((t % 3600000) / 60000);
        int c_second = (int)((t % 60000) / 1000);

        Log.d(LOG_TAG, "call duration convert c_hour is " + c_hour + " c_minute is " + c_minute
                + " c_second is " + c_second);
        return pad(c_hour) + ":" + pad(c_minute) + ":" + pad(c_second);
    }

    // [E][munjohn.kang]

    //[S] 20120513 eddie.kim@lge.com for data life timer
    private void onLifeTimeDataLoaded(Object lifeTime) {
        Log.d(LOG_TAG, "lifeTime = " + lifeTime);
        if (lifeTime != null) {
            Log.d(LOG_TAG, "lifeTime.toString() = " + lifeTime.toString());
            setSummaryText("life_time_data", lifeTime.toString() + " KB");
        }
    }

    //[E] 20120513 eddie.kim@lge.com for data life timer

    private void set_roaming_state(ServiceState serviceState) {
        Log.d(LOG_TAG, "set_roaming_state");

        String sIsRoamming = mRes.getString(R.string.radioInfo_roaming_in);
        String sNotRoamming = mRes.getString(R.string.radioInfo_roaming_not);

        // [S][2013.06.05][yongjaeo.lee@lge.com][VZW] opeator request ( Roaming -> Roaming state )
        if ("VZW".equals(operator_code)) {
            if (findPreference(KEY_ROAMING_STATE) != null) {
                findPreference(KEY_ROAMING_STATE).setTitle(R.string.status_roaming_state);
            }
        }
        // [E]

        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            String sRoamingState = MSIMUtils.get_roamming_state(getApplicationContext(),
                    mTelephonyManager, sIsRoamming, sNotRoamming);

            if (!sRoamingState.equals("common_pushed")) {
                //Toast.makeText(this, "0", Toast.LENGTH_SHORT).show();
                setSummaryText(KEY_ROAMING_STATE, sRoamingState);
                return;
            }
        }

        boolean mDataRoaming = LGServiceState.getDefault(serviceState).getDataRoaming();
        boolean mVoiceRoaming = LGServiceState.getDefault(serviceState).getVoiceRoaming();
        Log.d(LOG_TAG, "getDataRoaming() = " + mDataRoaming);
        Log.d(LOG_TAG, "getVoiceRoaming() = " + mVoiceRoaming);

        if (!"VZW".equals(operator_code)) {
            if (serviceState.getRoaming()) {
                setSummaryText(KEY_ROAMING_STATE, sIsRoamming);
            } else {
                setSummaryText(KEY_ROAMING_STATE, sNotRoamming);
            }
        } else {
            if (mVoiceRoaming && !mDataRoaming) { //voice
                sIsRoamming = mRes.getString(R.string.vzw_voice_roaming_settings);
            } else if (!mVoiceRoaming && mDataRoaming) { //data
                sIsRoamming = mRes.getString(R.string.data_usage_menu_roaming);
            } else if (mVoiceRoaming && mDataRoaming) { //voice and data
                sIsRoamming = mRes.getString(R.string.vzw_voice_data_connected);
            } else {
                sIsRoamming = mRes.getString(R.string.radioInfo_roaming_not);
            }
            setSummaryText(KEY_ROAMING_STATE, sIsRoamming);
        }
    }

    private int set_service_state(ServiceState serviceState) {
        int state = ServiceState.STATE_POWER_OFF;

        int dataServiceState = mPhone.getServiceState().getDataRegState();
        int serState = serviceState.getState();

        boolean mtk_dual = SystemProperties.getBoolean("ro.lge.mtk_dualsim", false);

        Log.d(LOG_TAG, "dataServiceState : " + dataServiceState);
        Log.d(LOG_TAG, "serState : " + serState);
        Log.d(LOG_TAG, "mtk_dual : " + mtk_dual);

        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            if (mtk_dual) {
                state = OverlayUtils
                        .get_service_state(getApplicationContext(), mPhoneStateReceiver);
            } else {
                state = OverlayUtils.get_service_state(getApplicationContext());
            }
            if (state == SIM_COMMON_SEL) {
                state = serviceState.getState();
            }
        } else {
            if ((serState == ServiceState.STATE_IN_SERVICE)
                    && (dataServiceState == ServiceState.STATE_OUT_OF_SERVICE)) {
                state = serState;
            } else if ((serState == ServiceState.STATE_OUT_OF_SERVICE)
                    && (dataServiceState == ServiceState.STATE_IN_SERVICE)) {
                state = dataServiceState;
            } else if ((serState == ServiceState.STATE_POWER_OFF)
                    || (dataServiceState == ServiceState.STATE_POWER_OFF)) {
                state = ServiceState.STATE_POWER_OFF;
            } else {
                state = serState;
            }
        }

        String display = mRes.getString(R.string.radioInfo_unknown);

        switch (state) {
        case ServiceState.STATE_IN_SERVICE:
            //Toast.makeText(this, "STATE_IN_SERVICE", Toast.LENGTH_SHORT).show();
            display = mRes.getString(R.string.radioInfo_service_in);
            break;

        case ServiceState.STATE_OUT_OF_SERVICE:
        case ServiceState.STATE_EMERGENCY_ONLY:
            //Toast.makeText(this, "STATE_OUT_OF_SERVICE", Toast.LENGTH_SHORT).show();
            display = mRes.getString(R.string.radioInfo_service_out);
            break;

        case ServiceState.STATE_POWER_OFF:
            //Toast.makeText(this, "STATE_POWER_OFF", Toast.LENGTH_SHORT).show();
            display = mRes.getString(R.string.radioInfo_service_off);
            break;

        default:
            break;
        }

        setSummaryText(KEY_SERVICE_STATE, display);
        return state;
    }

    public synchronized int getSIDCH() {
        Thread t = new Thread() {
            public void run() {
                String result = null;
                String SID = null;
                String CH = null;

                try {
                    result = OverlayUtils.LgSvcCmd_getCmdValue(CMD_SERVICE_SCRN);

                    if (result != null) {
                        CH = result.substring(ITEM_LEN * 4, ITEM_LEN * 5).trim();
                        SID = result.substring(ITEM_LEN * 5, ITEM_LEN * 6).trim();
                    }

                    mHandler.sendMessage(mHandler.obtainMessage(EVENT_SIDCH_LOADED,
                            Integer.parseInt(CH), Integer.parseInt(SID)));
                } catch (Exception e) {
                    ;
                }

            }
        };
        t.start();

        return 0;
    }

    private void updateLocation(CellLocation mLocation) {
        String mSummary = "";
        if (null == mLocation) {
            mSummary = sUnknown;
        } else if (mLocation instanceof GsmCellLocation) {
            GsmCellLocation mLoc = (GsmCellLocation)mLocation;
            int mCid = mLoc.getCid();
            SLog.i("updateLocation mCid = " + mCid);
            mSummary = ((mCid == -1) ? sUnknown : Integer.toHexString(mCid));
        } else if (mLocation instanceof CdmaCellLocation) {
            CdmaCellLocation mLoc = (CdmaCellLocation)mLocation;
            int mBid = mLoc.getBaseStationId();
            SLog.i("updateLocation mBid = " + mBid);
            mSummary = ((mBid == -1) ? sUnknown : Integer.toHexString(mBid));
        } else {
            mSummary = sUnknown;
        }
        SLog.i("updateLocation cell id = " + mSummary);
        setSummaryText(KEY_CELL_ID, mSummary);
    }

    private void updateSidNidState(ServiceState mServiceState) {
        String mSummary = "";
        if (null == mServiceState) {
            mSummary = sUnknown;
        } else {
            int mSidValue = mServiceState.getSystemId();
            int mNidValue = mServiceState.getNetworkId();
            String mSidStr = ((mSidValue == -1) ? "unknown" : Integer.toString(mSidValue));
            //String mNidStr = ((mNidValue == -1) ? "unknown" : Integer.toString(mNidValue));
            mSummary = mSidStr + ", " + mNidValue;
        }
        SLog.i("updateLocation sid, nid = " + mSummary);
        setSummaryText(KEY_SID, mSummary);
    }

    private void updateMccMncForCtc(ServiceState mServiceState) {
        String mValue = "";
        if (mCtcSlotID == GEMINI_SIM_2) {
            if (!LGSubscriptionManager.isNetworkRoaming(
                    MSIMUtils.getSubIdBySlotId(mCtcSlotID))) {
                mValue = TelephonyProxy.Carriers.getProperty(
                             "gsm.apn.sim.operator.numeric", 
                             mCtcSubscriber, "");
            } else {
                mValue = OverlayUtils.getPhoneFactoryCtc(getApplicationContext(), mCtcSlotID);
                if (mValue == null) {
                    SLog.i("CTC mPhone is null");
                    mValue = mPhone.getServiceState().getOperatorNumeric();
                }
            }
            SLog.i(LOG_TAG, "mValue 1 :" + mValue);
        } else {
            if (!LGSubscriptionManager.isNetworkRoaming(mCtcSubscriber)) {
                mValue = getOperatorNumericForCtc(mServiceState);
                SLog.i(LOG_TAG, "mValue 2 :" + mValue);
            } else {
                mValue = OverlayUtils.getPhoneFactoryCtc(getApplicationContext(), mCtcSlotID);
                if (mValue == null) {
                    SLog.i("CTC mPhone is null");
                    mValue = mPhone.getServiceState().getOperatorNumeric();
                }
            }
        }

        if (mValue != null && mValue.length() > 4) {
            String mcc = mValue.substring(0, 3);
            String mnc = mValue.substring(3);
            mValue = mcc + ", " + mnc;
            SLog.i(LOG_TAG, "mValue 3 :" + mValue);
        }

        if (mValue == null || mValue.length() == 0) {
            setSummaryText(KEY_IMSI, sUnknown);
        } else {
            setSummaryText(KEY_IMSI, mValue);
        }
    }

    private String getOperatorNumericForCtc(ServiceState mServiceState) {
        String mNumeric = " ";
        int radioTech = 0;
        if (null == mServiceState) {
            mNumeric = TelephonyProxy.Carriers.getNumeric(mCtcSubscriber);
            SLog.i("isGsmNetwork radioTech = " + radioTech);
        } else {
            radioTech = mServiceState.getRilDataRadioTechnology();
            SLog.i("isGsmNetwork radioTech = " + radioTech);
            if (ServiceState.isGsm(radioTech)
                    || radioTech == ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD) {
                SLog.i("isGsmNetwork isGsm = " + radioTech);
                mNumeric = TelephonyProxy.Carriers.getProperty(
                        "gsm.apn.sim.operator.numeric", mCtcSubscriber, "");
            } else if (ServiceState.isCdma(radioTech)) {
                SLog.i("isGsmNetwork isCdma = " + radioTech);
                mNumeric = TelephonyProxy.Carriers.getNumeric(mCtcSubscriber);
            } else {
                SLog.i("isGsmNetwork no svc = " + radioTech);
                mNumeric = TelephonyProxy.Carriers.getNumeric(mCtcSubscriber);
            }
        }
        SLog.i("isGsmNetwork = mNumeric" + mNumeric);
        return mNumeric;
    }

    private String getUSC_MEID_DEC(String meid) {
        String mMEID_DEC = "";
        long l_MEID_DEC;
        String sTemp = "";
        String sTemp2 = "";

        if (meid == null || TextUtils.isEmpty(meid)) {
            mMEID_DEC = mRes.getString(R.string.radioInfo_unknown);
        } else {
            if (meid.length() >= 14 ) {
                String str_high = meid.substring(0, 8);
                String str_low = meid.substring(8, 14);
                long l_high = Long.parseLong(str_high, 16);
                long l_low = Long.parseLong(str_low, 16);
                l_MEID_DEC = l_high * 100000000 + l_low;
                mMEID_DEC = Long.toString(l_MEID_DEC);
                int dec_len = mMEID_DEC.length();
                sTemp = sTemp.concat("000000000000000000");
                sTemp = sTemp.concat(mMEID_DEC);
                sTemp2 = sTemp.substring(dec_len);
                mMEID_DEC = sTemp2;
            }
        }

        if (mMEID_DEC.equals("0")) {
            mMEID_DEC = "000000000000000000";
        }
        return mMEID_DEC;
    }

}
