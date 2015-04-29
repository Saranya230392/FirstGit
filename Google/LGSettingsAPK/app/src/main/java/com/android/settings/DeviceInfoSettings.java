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

import android.app.Activity;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
//[2012.08.24][NA] Enable Software update menu (Software update -> Software Update)
import com.android.settings.lgesetting.Config.Config;

// [S][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version
import android.telephony.TelephonyManager;
import android.content.Context;

// [E][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version

//[S] insook.kim@lge.com 2012.03.20: Add VZW feature
import java.lang.ref.WeakReference;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.settings.lge.OverlayUtils;
//[E] insook.kim@lge.com 2012.03.20: Add VZW feature
import com.android.settings.lge.ATClientUtils;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.settings.lge.Svc_cmd;
import com.android.settings.Utils;

public class DeviceInfoSettings extends RestrictedSettingsFragment {
    private static final String LOG_TAG = "aboutphone # DeviceInfoSettings";

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";

    // [S][2012.06.06][munjohn.kang]added Chameleon Carrier Legal Information
    private static final String KEY_CARRIER_LEGAL = "carrier_legal";
    private static final String KEY_MPCS_LEGAL = "mpcs_legal";
    // [E][munjohn.kang]

    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PROPERTY_SELINUX_STATUS = "ro.build.selinux";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    //private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_SELINUX_STATUS = "selinux_status";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";
    // [S][2011.12.06][hyoungjun21.lee@lge.com][Common] Add Software version
    private static final String KEY_SOFTWARE_VERSION = "software_version";
    private static final String SOFTWARE_PROPERTY = "ro.lge.swversion";
    private static final String SOFTWARE_SHORT_PROPERTY = "ro.lge.swversion_short"; // g2mv
    // [E][2011.12.06][hyoungjun21.lee@lge.com][Common] Add Software version
    // [S][2013.01.07][yongjaeo.lee] Add swversion with MCCMNC in NTcode
    private static final String SOFTWARE_PROPERTY_MCCMNC = "ril.lge.swversion";
    // [E][2013.01.07][yongjaeo.lee]
    // [S][2012.04.06][youngmin.jeon@lge.com][USC] Add PRI version
    private static final String KEY_PRI_VERSION = "pri_version";
    //private static final String PRI_PROPERTY = "ro.lge.priversion";
    // [E][2012.04.06][youngmin.jeon@lge.com][USC] Add PRI version
    // [S][2012.04.09][never1029@lge.com][325_DCM] Add operater spec
    private static String operator_code;
    // [E][2012.04.09][never1029@lge.com][325_DCM] Add operater spec

    static final int TAPS_TO_BE_A_DEVELOPER = 7;

    // [S][2012.06.15][yongjaeo.lee@lge.com] Factory Serial Number ( F180L )
    private static final String KEY_MANUFACTURE_SN = "manufacture_serial_number";
    private static final String KEY_MANUFACTURE_C_C = "manufacture_company_country";
    private static final String KEY_MANUFACTURE_DATE = "manufacture_serial_number_date";
    // [E][2012.06.15][yongjaeo.lee@lge.com] Factory Serial Number ( F180L )
    long[] mHits = new long[3];
    int mDevHitCountdown;
    Toast mDevHitToast;

    private static final String KEY_REFUBISH = "refubish_counter";

    private static final String KEY_SCRIPT_VERSION = "script_version";
    private String mScriptVersion;
    private static final int EVENT_SCRIPT_VERSION_LOADED = 200;

    //[S] insook.kim@lge.com 2012.03.20: Add VZW feature
    private static final int EVENT_BASEBAND_VERSION_LOADED = 100;
    private String mBasebandVersion;
    private Handler mHandler;
    //LgSvcCmdIds.CMD_GET_SW_VERSION
    private static final int CMD_GET_SW_VERSION = 5000;
    private Preference mRev;

    private static final String KEY_ROOTING_STATUS = "rooting_status";
    private static final String KEY_VZW_CONFIGURATION_VERSION = "vzw_configuration_version";
    private static final String KEY_SECURITY_SOFTWARE_VERSION = "security_software_version";

    private static class MyHandler extends Handler {
        private WeakReference<DeviceInfoSettings> mDeviceInfoSettings;

        public MyHandler(DeviceInfoSettings activity) {
            mDeviceInfoSettings = new WeakReference<DeviceInfoSettings>(activity);
        }

        //@Override
        public void handleMessage(Message msg) {
            DeviceInfoSettings deviceInfoSettings = mDeviceInfoSettings.get();
            if (deviceInfoSettings == null) {
                return;
            }

            switch (msg.what) {
            case EVENT_BASEBAND_VERSION_LOADED:
                if (deviceInfoSettings.mBasebandVersion != null) {
                    deviceInfoSettings
                            .onBasebandeVersionLoaded(deviceInfoSettings.mBasebandVersion);
                }
                break;

            case EVENT_SCRIPT_VERSION_LOADED:
                if (deviceInfoSettings.mScriptVersion != null) {
                    deviceInfoSettings.onScriptVersionLoaded(deviceInfoSettings.mScriptVersion);
                }
                break;

            default:
                break;
            }
        }
    }

    private Handler regul_mHandler = new Handler();

    public static boolean isHypenValue(char value) {
        switch (value)
        {
        case '-':
            return true;

        default:
            return false;
        }
    }

    Runnable m_display_run_factory = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run_factory (4011 , Request .. factory version)");
            try {
                strResponseData = ATClientUtils.atClient_readValue(4011, getActivity()
                        .getApplicationContext(), "");
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            if (SystemProperties.getBoolean("persist.sys.cust.unifiedversion", false)) {
                mRev = (Preference)findPreference(KEY_SOFTWARE_VERSION);
                if (mRev != null) {
                    String swversion;
                    String[] factoryVersion = strResponseData.split("-");

                    if (factoryVersion.length < 8) {
                        swversion = SystemProperties.get(SOFTWARE_PROPERTY);
                    }
                    else {
                        swversion = factoryVersion[0].substring(2, factoryVersion[0].length() - 2) + //remove sim lock info
                                factoryVersion[2].substring(1, factoryVersion[2].length()) + "-" + // swversion
                                factoryVersion[3] + "-" + factoryVersion[4];
                    }
                    mRev.setSummary(swversion);
                }
            } else {

                StringBuilder sMccMnc = new StringBuilder();
                StringBuilder sSoftware_ver = new StringBuilder();
                int hypen_cnt = 0;
                String before_sw_ver = "";

                if (SystemProperties.get("ro.build.sbp").equals("1")
                        && (SystemProperties.get("persist.radio.cupss.next-root", "/").contains(
                                "/cust")
                        || SystemProperties.get("persist.sys.cupss.next-root", "/").contains(
                                "/cust"))) {
                    before_sw_ver = SystemProperties.get("ro.lge.swversion_short", null);
                    if (before_sw_ver == null || before_sw_ver.equals("")
                            || SystemProperties.get("ro.lge.global_officialbuild").equals("no")) {
                        before_sw_ver = SystemProperties.get(SOFTWARE_PROPERTY);
                    }
                } else {
                    before_sw_ver = SystemProperties.get(SOFTWARE_PROPERTY);
                }
                sSoftware_ver.append(before_sw_ver);

                for (int index = 0; index < strResponseData.length(); index++) {
                    if (isHypenValue(strResponseData.charAt(index))) {
                        hypen_cnt++;
                        //[Start][07/02/2014][sumanth.kumarv]TSC Settings SW Version V08a-TSC-234-01
                        if (hypen_cnt == 1 &&
                                (Config.getOperator().equals("TSC"))) {
                            sMccMnc.append("-TSC");
                        }
                        //[End][07/02/2014][sumanth.kumarv]TSC Settings SW Version V08a-TSC-234-01
                    }

                    if (hypen_cnt >= 3 && hypen_cnt < 5) {
                        sMccMnc.append(strResponseData.charAt(index));
                    }
                    Log.d(LOG_TAG, "sMccMnc:" + sMccMnc);
                }

                Log.d(LOG_TAG, "sMccMnc:" + sMccMnc);
                sSoftware_ver.append(sMccMnc);

                mRev = (Preference)findPreference(KEY_SOFTWARE_VERSION);
                if (mRev != null) {
                    Log.i(LOG_TAG, "finded preference");
                    mRev.setSummary(sSoftware_ver);
                }
            }
        }
    };

    //[E] insook.kim@lge.com 2012.03.20: Add VZW feature

    public DeviceInfoSettings() {
        super(null /* Don't PIN protect the entire screen */);
    }


// [S][2013.04.10][yongjaeo.lee@lge.com][VZW] Rooting status
    private String getRootedStatus() {
        byte[] flag = new byte[4];
        int result = 0;
        String sNotRooted = getResources().getString(R.string.status_official);
        String sRooted = getResources().getString(R.string.status_modified);
        DataInputStream in = null;

        try {
            in = new DataInputStream(new FileInputStream("/persist/rct"));
            in.read(flag);

            result = ((flag[3] & 0xFF) << 24) | ((flag[2] & 0xFF) << 16) | ((flag[1] & 0xFF) << 8) | ((flag[0] & 0xFF));

            Log.i(LOG_TAG, "getRootedStatus() , result:" + result);

            if (result == 1000000) {
                if (in != null) {
                    in.close();
                }
                return sNotRooted;
            } else if (result > 1000000) {
                if (in != null) {
                    in.close();
                }
                return sRooted;
            } else {
                if (in != null) {
                    in.close();
                }
                return sNotRooted;
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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.i(LOG_TAG, "onCreate");

        //[S] insook.kim@lge.com 2012.03.20: Add VZW feature
        mHandler = new MyHandler(this);
        //[E] insook.kim@lge.com 2012.03.20: Add VZW feature

        addPreferencesFromResource(R.xml.device_info_settings);

        // [S][2012.04.09][never1029@lge.com][325_DCM] Add operater spec
        operator_code = Config.getOperator();
        // [E][2012.04.09][never1029@lge.com][325_DCM] Add operater spec

        //[2012.08.31][munjohn.kang]
        // [S][2012.02.13][jm.lee2] Dual SIM Status
        //        if (Utils.isMultiSimEnabled()) {
        //            removePreferenceFromScreen("status_info");
        //        } else {
        removePreferenceFromScreen("dualsim_status_info");
        //        }
        findPreference("status_info").setSummary((R.string.device_status_summary));
        setStringSummary(KEY_FIRMWARE_VERSION, Build.VERSION.RELEASE);
        findPreference(KEY_FIRMWARE_VERSION).setEnabled(true);
        // [S][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version
        String targetOperator = Config.getOperator();
        Log.i(LOG_TAG, "targetOperator: " + targetOperator);
        // [E][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version

        //[S][2012.09.03][yongjaeo.lee@lge.com][EUR] Europe Integrated SW Version
        String targetCountry = Config.getCountry();
        Log.i(LOG_TAG, "taregetCountry: " + targetCountry);
        //[E][2012.09.03][yongjaeo.lee@lge.com][EUR] Europe Integrated SW Version

        // [2012.03.15][seungeene][Common] Rearrange baseband version condition (from model feature to device feature)
        Log.i(LOG_TAG, "Build.DEVICE: " + Build.DEVICE + ", Build.Model: " + Build.MODEL);

        display_basebandVersion();

        //      setStringSummary(KEY_DEVICE_MODEL, Build.MODEL + getMsvSuffix());
        setValueSummary(KEY_EQUIPMENT_ID, PROPERTY_EQUIPMENT_ID);
        //      setStringSummary(KEY_DEVICE_MODEL, Build.MODEL);
        setStringSummary(KEY_BUILD_NUMBER, Build.DISPLAY);
        findPreference(KEY_BUILD_NUMBER).setEnabled(true);
        findPreference(KEY_KERNEL_VERSION).setSummary(getFormattedKernelVersion());
        findPreference(KEY_VZW_CONFIGURATION_VERSION).setEnabled(true);

        String configurationVersion = SystemProperties.get("persist.configurationver");
        if (configurationVersion != null && !"".equals(configurationVersion)) {
            setStringSummary(KEY_VZW_CONFIGURATION_VERSION, configurationVersion);
        }
        // [s][2013.02.05][yongjaeo.lee@lge.com] for runtime_error
        if (!SELinux.isSELinuxEnabled()) {
            String status = getResources().getString(R.string.selinux_status_disabled);
            setStringSummary(KEY_SELINUX_STATUS, status);
        } else if (!SELinux.isSELinuxEnforced()) {
            String status = getResources().getString(R.string.selinux_status_permissive);
            setStringSummary(KEY_SELINUX_STATUS, status);
        }
        /*
        // imsi apply
        String status = getResources().getString(R.string.selinux_status_disabled);
        setStringSummary(KEY_SELINUX_STATUS, status);
        */
        // [E][2013.02.05][yongjaeo.lee@lge.com]

        // Remove selinux information if property is not present
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_SELINUX_STATUS,
                PROPERTY_SELINUX_STATUS);

        String device_name = null;
        String v_device_name = null;
        device_name = Build.DEVICE;
        Log.d(LOG_TAG, "device_name.length() = " + device_name.length());
        if (device_name.length() >= 4)
        {
            v_device_name = device_name.substring(0, 4);
        }
        else
        {
            v_device_name = device_name;
        }
        Log.d(LOG_TAG, "device_name = " + device_name);
        Log.d(LOG_TAG, "v_device_name = " + v_device_name);

        if (SystemProperties.getBoolean("persist.sys.cust.unifiedversion", false)) {
            regul_mHandler.postDelayed(m_display_run_factory, 150);
            ATClientUtils.atClient_BindService(getActivity().getApplicationContext());
        } else if ("LG-LU5400".equals(Build.MODEL)) {
            // [S][2012.04.30][never1029@lge.com][LG5400] S/W version date update
            //         if ("LG-LU5400".equals(Build.MODEL)) {
            findPreference(KEY_SOFTWARE_VERSION).setSummary(getSWVersionInfo());
        } else if ("SCA".equals(SystemProperties.get("ro.build.target_region"))
                && !("TCL".equals(Config.getOperator()))
                && !(SystemProperties.get("ro.build.sbp").equals("1"))
                && !Utils.isWifiOnly(getActivity())) {
            // E612 2012.05.10 alex.branti S/W version name for LATAM CAs
            findPreference(KEY_SOFTWARE_VERSION).setSummary(getLatinAmericaSWVersion());
        } else if ("TCL".equals(Config.getOperator())) { // [2013.01.03][never1029@lge.com]TCL operator req. -> Software version hardcoding : V10a
            if ("g2mv".equals(v_device_name)) {
                setValueSummary(KEY_SOFTWARE_VERSION, SOFTWARE_SHORT_PROPERTY);
            }
            else if (("w7n".equals(v_device_name)) || ("w5".equals(v_device_name))
                || "luv30ss".equals(device_name) || "luv20ss".equals(device_name)
                || "g3".equals(device_name)) {
                setValueSummary(KEY_SOFTWARE_VERSION, "ro.lge.swversion_shortdate");
            }
            else {
                setStringSummary(KEY_SOFTWARE_VERSION, "V10a");
            }
        } else if ("LG-E975".equals(Build.MODEL) || "LG-E975K".equals(Build.MODEL)
                || "LG-E977".equals(Build.MODEL)
                || SystemProperties.get("ro.build.sbp").equals("1")
                || "CTC".equals(Config.getOperator())
                || "CTO".equals(Config.getOperator())
                || "CMO".equals(Config.getOperator())
                || "CMCC".equals(Config.getOperator())) { // || "g2".equals(device_name)) {
            regul_mHandler.postDelayed(m_display_run_factory, 150);
            ATClientUtils.atClient_BindService(getActivity().getApplicationContext());
        } else if (!SystemProperties.get(SOFTWARE_PROPERTY_MCCMNC).equals("")) { // [2013.01.07][yongjaeo.lee] Add swversion with MCCMNC in NTcode
            setValueSummary(KEY_SOFTWARE_VERSION, SOFTWARE_PROPERTY_MCCMNC);
        //[G2] zhaofeng.yang@lge.com add to modify device info[start]
        } else if (!Utils.isWifiOnly(getActivity()) 
       && ((targetCountry.equals("HK") /*|| targetCountry.equals("CIS")*/ 
            || targetCountry.equals("ESA")
            || targetCountry.equals("AME")
            || targetCountry.equals("IL")
            || targetCountry.equals("TH")
            || targetCountry.equals("EU")
            || targetCountry.equals("TW"))
        && targetOperator.equals("OPEN"))) {
            String new_SW_v = SystemProperties.get(SOFTWARE_PROPERTY);
            String country_info = "";
            if (targetCountry.equals("HK")) {
                country_info = "HKG";
            } else if (targetCountry.equals("TW")) {
                country_info = "TWN";
            } else if (targetCountry.equals("ESA")) {
                country_info = "ESA";
            } else if (targetCountry.equals("AME")) {
                country_info = "AME";
            } else if (targetCountry.equals("TH")) {
                country_info = "THA";
            } else if (targetCountry.equals("IL")) {
                country_info = "ISR";
            } else if (targetCountry.equals("EU")) {
                country_info = "EUR";
            } /*else if (targetCountry.equals("CIS")) {
                country_info = "CIS";
              }*/
            new_SW_v = new_SW_v.concat("-");
            new_SW_v = new_SW_v.concat(country_info);
            new_SW_v = new_SW_v.concat("-");
            new_SW_v = new_SW_v.concat("XX");
            findPreference(KEY_SOFTWARE_VERSION).setSummary(new_SW_v);
            //[G2] zhaofeng.yang@lge.com add to modify device info[end]
        } else {
            setValueSummary(KEY_SOFTWARE_VERSION, SOFTWARE_PROPERTY);
        }
        // [E][2012.04.30][never1029@lge.com][LG5400] S/W version date update

        // [S][2012.06.14][munjohn.kang]
        /*/
        if ("L-06DJOJO".equals(SystemProperties.get("ro.product.model"))) {
            findPreference(KEY_DEVICE_MODEL).setEnabled(true);
        }
        */
        // [E][munjohn.kang]

        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal",
                PROPERTY_URL_SAFETYLEGAL);

        // Remove Equipment id preference if FCC ID is not set by RIL
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_EQUIPMENT_ID,
                PROPERTY_EQUIPMENT_ID);

        // Remove Baseband version if wifi-only device
        if (Utils.isWifiOnly(getActivity())) {
            removePreferenceFromScreen(KEY_BASEBAND_VERSION);
        }

        /*
        * Settings is a generic app and should not contain any device-specific
        * info.
        */
        final Activity act = getActivity();
        // These are contained in the "container" preference group
        PreferenceGroup parentPreference = (PreferenceGroup)findPreference(KEY_CONTAINER);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TEAM,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        //[S][2012.06.06][munjohn.kang]added Chameleon Carrier Legal Information
        if ("SPR".equals(operator_code) || "BM".equals(operator_code)) {
            if (Config.getOperator().equals("TRF")) { // added for exception in L25L
                if (parentPreference != null) {
                    parentPreference.removePreference(parentPreference
                            .findPreference(KEY_CARRIER_LEGAL));
                }
            } else {
                Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                        KEY_CARRIER_LEGAL,
                        Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
            }
        } else {
            if (parentPreference != null) {
                parentPreference.removePreference(parentPreference
                        .findPreference(KEY_CARRIER_LEGAL));
            }
        }
        // [E][munjohn.kang]

        // [S][2012.07.09][munjohn.kang]added Metro PCS Legal Information
        if ("MPCS".equals(operator_code)) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_MPCS_LEGAL,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            if (parentPreference != null) {
                parentPreference.removePreference(parentPreference.findPreference(KEY_MPCS_LEGAL));
            }
        }
        // [E][munjohn.kang]

        // These are contained by the root preference screen
        parentPreference = getPreferenceScreen();

        // [s][2013.02.05][yongjaeo.lee@lge.com] for runtime_error
        /*
        if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_SYSTEM_UPDATE_SETTINGS, Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            // Remove for secondary users
            removePreference(KEY_SYSTEM_UPDATE_SETTINGS);
        }
        */
        // imsi apply
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                KEY_SYSTEM_UPDATE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        // [E][2013.08.10][yongjaeo.lee@lge.com]

        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_CONTRIBUTORS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // Read platform settings for additional system update setting
        removePreferenceIfBoolFalse(KEY_UPDATE_SETTING,
                R.bool.config_additional_system_update_setting_enable);

        // Remove regulatory information if not enabled.
        removePreferenceIfBoolFalse(KEY_REGULATORY_INFO,
                R.bool.config_show_regulatory_info);

        //[S][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo
        if (!(Config.DCM).equals(Config.getOperator())) {
            removePreferenceFromScreen("software_update_settings_for_dcm");
        }
        // [E][2012.03.17][seungeene][DCM][NA] Add Next-i FOTA for Docomo
        boolean isUpdateSettingAvailable = getResources().getBoolean(
                R.bool.config_additional_system_update_setting_enable);
        if (isUpdateSettingAvailable == false) {
            removePreferenceFromScreen(KEY_UPDATE_SETTING);
        }

        removePreferenceFromScreen(KEY_PRI_VERSION);
        removePreferenceFromScreen("regulatory_dcm");
        removePreferenceFromScreen("battery_dcm");

        // [s][2012.08.10][yongjaeo.lee@lge.com] embedded battery model
        Log.i(LOG_TAG, "Build.DEVICE:" + Build.DEVICE);
        removePreferenceFromScreen("device_serial_info");
        // [E][2012.08.10][yongjaeo.lee@lge.com] embedded battery model

        // [S][2012.06.15][yongjaeo.lee@lge.com] Factory Serial Number ( F180L )
        removePreferenceFromScreen(KEY_MANUFACTURE_SN);
        removePreferenceFromScreen(KEY_MANUFACTURE_C_C);
        removePreferenceFromScreen(KEY_MANUFACTURE_DATE);
        // [E][2012.06.15][yongjaeo.lee@lge.com] Factory Serial Number ( F180L )

        if (!Config.CRK.equals(Config.getOperator())) {
            removePreferenceFromScreen(KEY_REFUBISH);
        }

        //[S][2012.06.15][yongjaeo.lee@lge.com] Factory Serial Number ( F180L )
        removePreferenceFromScreen(KEY_MANUFACTURE_SN);
        removePreferenceFromScreen(KEY_MANUFACTURE_C_C);
        removePreferenceFromScreen(KEY_MANUFACTURE_DATE);
        // getPreferenceScreen().removePreference(findPreference(KEY_MANUFACTURE_SN));

        //[S][2012.06.18][yongjaeo.lee][E0][NA] Move developer settings to inside of about phone from top header (ORG request)
        /*
        if (!"ORG".equals(Config.getOperator())) {
            removePreferenceFromScreen("development_settings");
        }
        */
        if ("LRA".equals(operator_code) || "ACG".equals(operator_code)) {
            ScriptVersionThread thread = new ScriptVersionThread();
            thread.start();
        } else {
            removePreferenceFromScreen(KEY_SCRIPT_VERSION);
        }

        if ("VZW".equals(operator_code)) {
            findPreference(KEY_ROOTING_STATUS).setSummary(getRootedStatus());
        } else {
            removePreferenceFromScreen(KEY_ROOTING_STATUS);
        }

        if (!"VZW".equals(operator_code)
                || (!Utils.checkPackage(getActivity(), "com.lge.lgdmsclient"))) {
            removePreferenceFromScreen(KEY_VZW_CONFIGURATION_VERSION);
        }

        String security_sw_version = SystemProperties.get("ro.sys.sec.version.info");
        if (security_sw_version != null && !security_sw_version.isEmpty()) {
            findPreference(KEY_SECURITY_SOFTWARE_VERSION).setSummary(security_sw_version);
        } else {
            removePreferenceFromScreen(KEY_SECURITY_SOFTWARE_VERSION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        regul_mHandler.removeCallbacks(m_display_run_factory);
        regul_mHandler = null;
        ATClientUtils.atClient_unBindService();
    }

    private void display_basebandVersion() {
        Log.i(LOG_TAG, "ro.lge.basebandversion: " + SystemProperties.get("ro.lge.basebandversion"));
        Log.i(LOG_TAG, "gsm.version.baseband: " + SystemProperties.get("gsm.version.baseband"));

        Preference pref = findPreference(KEY_BASEBAND_VERSION);
        String ro_lge_baseband = getBasebandProperty("ro.lge.basebandversion");
        String gsm_baseband = getBasebandProperty("gsm.version.baseband");

        if (pref != null) {
            if (!"".equals(ro_lge_baseband) && ro_lge_baseband != null) {
                pref.setSummary(ro_lge_baseband);
            } else if (!"".equals(gsm_baseband) && gsm_baseband != null) {
                pref.setSummary(gsm_baseband);
            } else {
                if ("VZW".equals(Config.getOperator())
                        && !Build.DEVICE.equals("altev")) {
                    BasebandVersionThread thread = new BasebandVersionThread();
                    thread.start();
                } else {
                    pref.setSummary(R.string.device_info_default);
                }
            }
        }
    }

    private static String getBasebandProperty(String property) {
        String prop = SystemProperties.get(property);

        if ((prop != null) && (prop.length() > 0)) {
            String values[] = prop.split(",");
            Log.i(LOG_TAG, "values:" + values[0]);
            return values[0];
        }
        return null;
    }

    // [S][2012.04.30][never1029@lge.com][LG5400] S/W version date update
    private String getSWVersionInfo() {
        String swver = "";
        String build_date = "";

        try {
            swver = SystemProperties.get("ro.lge.swversion");
            build_date = SystemProperties.get("ro.build.lge.version.date");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return swver + "\n" + build_date;
    }

    // [E][2012.04.30][never1029@lge.com][LG5400] S/W version date update

    /**
     * Returns the software version for the SCA target region (LGESP).
     * Note that this method won't be called if persist.sys.cust.unifiedversion
     * is set, if ro.build.sbp is 1, or if ro.build.target_operator is TCL.
     *
     * @return the software version string.
     */
    private String getLatinAmericaSWVersion() {
        StringBuilder swversion = new StringBuilder();

        // Format: LGE612fAT-00-V09a-724-31-MAY-07-2012+0

        String version = SystemProperties.get("lge.version.factorysw", "");
        if (TextUtils.isEmpty(version)) {
            // For G and newer models
            version = SystemProperties.get("ro.lge.factoryversion", "Unknown");
        }

        String[] factoryVersion = version.split("-");
        if (factoryVersion.length < 8) {
            return factoryVersion[0];
        }

        String shortVersion = SystemProperties.get("ro.lge.swversion_short", null);
        if (shortVersion != null) {
            swversion.append(shortVersion);
        } else {
            swversion.append(factoryVersion[0].substring(2, factoryVersion[0].length() - 2)); //remove sim lock info
            swversion.append(factoryVersion[2].substring(1, factoryVersion[2].length())); // swversion
        }

        swversion.append('-');
        swversion.append(factoryVersion[3]);
        swversion.append('-');
        swversion.append(factoryVersion[4]);

        return swversion.toString();
    }

    // [S][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version
    private String makeSWversion_VDF(String mccmnc) {
        Log.i(LOG_TAG,
                "makeSWversion_VDF / Software_property:" + SystemProperties.get(SOFTWARE_PROPERTY)); // "ro.lge.swversion"
        Log.i(LOG_TAG, "makeSWversion_VDF (mccmnc:" + mccmnc + ")");

        if (mccmnc == null || mccmnc.length() < 5) {
            Log.i(LOG_TAG, "makeSWversion_VDF ( mcccmc is Wrong )");
            return null;
        }

        String VDF_country = getVDF_country(mccmnc);
        if (VDF_country == null) {
            Log.e(LOG_TAG, "VDF_country is null !!!");
            return null;
        }

        String before_sw_ver = SystemProperties.get(SOFTWARE_PROPERTY);
        //int nIndex = before_sw_ver.indexOf("-", "LGP760-VDF".length());
        //String new_sw_ver = before_sw_ver.substring(0, nIndex + 1);
        //new_sw_ver = new_sw_ver.concat("VDF-").concat(VDF_country).concat("-");
        //new_sw_ver = new_sw_ver.concat(before_sw_ver.substring(nIndex + 1));

        /*
        String new_sw_ver = before_sw_ver;
        new_sw_ver = new_sw_ver.concat("( ");
        new_sw_ver = new_sw_ver.concat("VDF-");
        new_sw_ver = new_sw_ver.concat(VDF_country);
        new_sw_ver = new_sw_ver.concat(" )");
        Log.i(LOG_TAG, "new_sw_ver:" + new_sw_ver);
        */
        String new_sw_ver = before_sw_ver;
        new_sw_ver = new_sw_ver.concat("-");
        new_sw_ver = new_sw_ver.concat("VDF-");
        new_sw_ver = new_sw_ver.concat(VDF_country);
        // new_sw_ver = new_sw_ver.concat(" )");
        Log.i(LOG_TAG, "new_sw_ver:" + new_sw_ver);

        return new_sw_ver;
    }

    private String getVDF_country(String mccmnc) {
        Log.i(LOG_TAG, "getVDF_country (mccmnc:" + mccmnc + ")");

        if (mccmnc == null || mccmnc.length() < 5) {
            Log.i(LOG_TAG, "getVDF_country ( mcccmc is Wrong )");
            return null;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        String[][] VDF_contry = {
                { "262", "02", "DE" },
                { "226", "01", "RO" },
                { "204", "04", "NL" },
                { "208", "10", "SFR" },
                { "647", "10", "SRR" },
                { "214", "02", "ES" }, // spain
                { "222", "10", "IT" }, // italy
                { "202", "05", "GR" }, // greece
                { "280", "01", "CY" }, // cyprus
                { "219", "10", "HR" }, // croatia
        };

        for (int i = 0; i < VDF_contry.length; i++) {
            if (mcc.equals(VDF_contry[i][0]) && mnc.equals(VDF_contry[i][1])) {
                return VDF_contry[i][2];
            }
        }

        return null;
    }

    // [E][2012.08.08][yongjaeo.lee@lge.com][VDF] Modify Software version

    @Override
    public void onResume() {
        super.onResume();
        mDevHitCountdown = getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE).getBoolean(DevelopmentSettings.PREF_SHOW,
                android.os.Build.TYPE.equals("eng")) ? -1 : TAPS_TO_BE_A_DEVELOPER;
        mDevHitToast = null;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_FIRMWARE_VERSION)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("android",
                        com.android.internal.app.PlatLogoActivity.class.getName());
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                }
            }
        } else if (preference.getKey().equals(KEY_BUILD_NUMBER)) {

            // Don't enable developer options for secondary users.
            if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                return true;
            }

            final UserManager um = (UserManager)getSystemService(Context.USER_SERVICE);
            if (um.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES)) {
                return true;
            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-413]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.lge.mdm.LGMDMManager.getInstance().getAllowDeveloperMode(null) == false) {
                    mDevHitToast = Toast.makeText(getActivity(), R.string.developer_options_mdm_toast, Toast.LENGTH_SHORT);
                    mDevHitToast.show();
                    return true;
                }
            }
            // LGMDM_END

            if (mDevHitCountdown > 0) {
                mDevHitCountdown--;

                if (mDevHitCountdown == 0) {
                    getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
                            Context.MODE_PRIVATE).edit().putBoolean(
                            DevelopmentSettings.PREF_SHOW, true).apply();
                    updateSharedPreferenceForEasySettings(); // yonguk.kim 20130322 Care of TabView

                    if (mDevHitToast != null) {
                        mDevHitToast.cancel();
                    }
                    mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_on,
                            Toast.LENGTH_LONG);
                    mDevHitToast.show();
                } else if (mDevHitCountdown > 0
                        && mDevHitCountdown < (TAPS_TO_BE_A_DEVELOPER - 2)) {
                    if (mDevHitToast != null) {
                        mDevHitToast.cancel();
                    }

                    mDevHitToast = Toast.makeText(getActivity(), getResources().getQuantityString(
                            R.plurals.show_dev_countdown, mDevHitCountdown, mDevHitCountdown),
                            Toast.LENGTH_SHORT);
                    /*
                    mDevHitToast = Toast.makeText(getActivity(), getResources().getString(
                            R.string.show_dev_countdown, mDevHitCountdown),
                            Toast.LENGTH_SHORT);
                    */
                    mDevHitToast.show();

                }
            } else if (mDevHitCountdown < 0) {
                if (mDevHitToast != null) {
                    mDevHitToast.cancel();
                }
                mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_already,
                        Toast.LENGTH_LONG);
                mDevHitToast.show();
                updateSharedPreferenceForEasySettings();
            }
        } else if (preference.getKey().equals(KEY_VZW_CONFIGURATION_VERSION)) {
            String configurationVersion = SystemProperties.get("persist.configurationver");
            if (configurationVersion == null || "".equals(configurationVersion)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.lge.lgdmsclient",
                        "com.lge.lgdmsclient.ui.DmConfigurationView"));
                startActivity(intent);
            }
        }

        // [S][2012.06.14][munjohn.kang]
        /*
        if ("L-06DJOJO".equals(SystemProperties.get("ro.product.model"))) {
            if (preference.getKey().equals(KEY_DEVICE_MODEL)) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.DeviceInfoLogoView");
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                    }
                }
            }
        }
        */
        // [E][munjohn.kang]

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property) {
        if (SystemProperties.get(property).equals("")) {
            // Property is missing so remove preference from group
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '" + preference
                        + "' preference");
            }
        }
    }

    private void removePreferenceIfBoolFalse(String preference, int resId) {
        if (!getResources().getBoolean(resId)) {
            Preference pref = findPreference(preference);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            if (Utils.isRTLLanguage() || Utils.isEnglishDigitRTLLanguage()) {
                findPreference(preference).setSummary("\u200F" + value);
            } else {
                findPreference(preference).setSummary(value);
            }
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                    getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "RuntimeException");
        }
    }

    /**
    * Reads a line from the specified file.
    * @param filename the file to read from
    * @return the first line, if any.
    * @throws IOException if the file couldn't be read
    */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));
        } catch (NullPointerException e) {
            return "Unavailable";
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " + /* group 2: "x@y.com" (kernel builder) */
                        "(?:\\(gcc.+? \\)) " + /* ignore: GCC version information */
                        "(#\\d+) " + /* group 3: "#1" */
                        "(?:.*?)?" + /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        } else {
            // [S][2012.06.08][yongjaeo.lee@lge.com][LG-P940] LG-P940 kernel
            // version display

            // display

            // String verInfo = (new StringBuilder(m.group(1))).toString();
            String verInfo = null;
            if (m.group(1) == null) {
                return "Unavailable";
            } else {
                verInfo = (new StringBuilder(m.group(1))).toString();
            }

            // [S][2012.06.12][yongjaeo.lee@lge.com][COMMON] ALL Model kernel version display
            //if("LG-P940".equals(Build.MODEL)) {
            if (true) {
                // [E][2012.06.12][yongjaeo.lee@lge.com][COMMON] All Model kernel version display
                int strIndex = verInfo.indexOf("-");
                if (strIndex > 0) {
                    verInfo = verInfo.substring(0, strIndex);
                }
                if ("VZW".equals(Config.getOperator())) {
                    return (new StringBuilder(m.group(1)).append("\n").append("lge@android-build "))
                            .toString();
                } else {
                    return verInfo;
                }
            } else {
                return verInfo;
            }
            // [E][2012.06.08][yongjaeo.lee@lge.com][LG-P940] LG-P940 kernel version display
        }

        /*
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
            */
    }

    /**
    * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
    * @return a string to append to the model number description.
    */
    private String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
            Log.w(LOG_TAG, "IOException");
        } catch (NumberFormatException nfe) {
            Log.w(LOG_TAG, "NumberFormatException");
        }
        return "";
    }

    //[S] insook.kim@lge.com 2012.03.20: Add VZW feature
    class BasebandVersionThread extends Thread {
        public void run() {
            Message msg = mHandler.obtainMessage(EVENT_BASEBAND_VERSION_LOADED);
            //mBasebandVersion = LgSvcCmd.getCmdValue(LgSvcCmdIds.CMD_GET_SW_VERSION);

            if (Svc_cmd.getQcrilMsgTunnelServiceStatus(getActivity().getApplicationContext()) == false) {
                try {
                    Svc_cmd.LgSvcCmd_getCmdValue(CMD_GET_SW_VERSION, getActivity()
                            .getApplicationContext());
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

            mBasebandVersion = Svc_cmd.LgSvcCmd_getCmdValue(CMD_GET_SW_VERSION, getActivity()
                    .getApplicationContext());
            mHandler.sendMessage(msg);
        }
    }

    private void onBasebandeVersionLoaded(String text) {
        Preference pref = findPreference(KEY_BASEBAND_VERSION);

        if (pref != null) {
            pref.setSummary(!TextUtils.isEmpty(text) ? text
                    : getString(R.string.status_unavailable));
        }
    }

    //[E] insook.kim@lge.com 2012.03.20: Add VZW feature

    class ScriptVersionThread extends Thread {
        public void run() {
            Message msg = mHandler.obtainMessage(EVENT_SCRIPT_VERSION_LOADED);
            mScriptVersion = Svc_cmd.LgSvcCmd_getCmdValue(1000, getActivity()
                    .getApplicationContext());
            Log.i(LOG_TAG, "ScriptVersionThread mScriptVersion =  " + mScriptVersion);
            mHandler.sendMessage(msg);
        }
    }

    private void onScriptVersionLoaded(String text) {
        Preference pref = findPreference(KEY_SCRIPT_VERSION);
        Log.i(LOG_TAG, "onScriptVersionLoaded text = " + text);

        if (pref != null) {
            pref.setSummary(!TextUtils.isEmpty(text) ? text : getString(R.string.unknown));
        }
    }

    /**
    * Removes the specified preference, if it exists.
    * @param key the key for the Preference item
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

    private void updateSharedPreferenceForEasySettings() {
        Context easyContext = null;
        try {
            easyContext = getActivity().createPackageContext("com.lge.settings.easy",
                    Context.CONTEXT_IGNORE_SECURITY);
            easyContext
                    .getSharedPreferences(
                            "easy_development",
                            Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                                    | Context.MODE_MULTI_PROCESS)
                    .edit().putBoolean(DevelopmentSettings.PREF_SHOW, true).apply();
        } catch (NameNotFoundException e) {
            Log.w(LOG_TAG, "NameNotFoundException");
        }
    }

}
