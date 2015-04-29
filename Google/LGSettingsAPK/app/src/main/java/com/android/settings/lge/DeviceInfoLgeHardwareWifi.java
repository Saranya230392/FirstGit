package com.android.settings.lge;

import android.bluetooth.BluetoothAdapter;
//import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

import java.util.Locale;
import java.lang.ref.WeakReference;

public class DeviceInfoLgeHardwareWifi extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "aboutphone #DeviceinfolgePhoneidentity";

    //[S][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
    private static final String KEY_MANUFACTURE_SN = "manufacture_serial_number";
    //private static final String KEY_MANUFACTURE_C_C = "manufacture_company_country";
    private static final String KEY_MANUFACTURE_DATE = "manufacture_serial_number_date";
    private static final String KEY_SERIAL_NUMBER = "serial_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_WIFI_IP_ADDRESS = "wifi_ip_address";
    private static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    private static final String KEY_BT_ADDRESS = "bt_address";
    private static final String KEY_CSN = "csn";
    private static final String KEY_LGU_SN = "manufacture_lgu_serial_number";

    //private IntentFilter mWiFi_Filter;
    //private BroadcastReceiver mWiFi_Receiver;
    //private IntentFilter mBT_Filter;
    //private BroadcastReceiver mBT_Receiver;
    private Preference mRev;
    private static String operator_code;
    private Preference mUptime;
    private Resources mRes;
    private String sUnknown;

    private static final int EVENT_UPDATE_STATS = 500;

    private Handler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<DeviceInfoLgeHardwareWifi> mStatus;

        public MyHandler(DeviceInfoLgeHardwareWifi activity) {
            mStatus = new WeakReference<DeviceInfoLgeHardwareWifi>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceInfoLgeHardwareWifi status = mStatus.get();
            if (status == null) {
                return;
            }

            // Log.d(LOG_TAG, "MyHandler handleMessage msg.what = " + msg.what);

            switch (msg.what) {
            case EVENT_UPDATE_STATS:
                status.updateTimes();
                sendEmptyMessageDelayed(EVENT_UPDATE_STATS, 1000);
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
            Log.i(LOG_TAG, "m_display_run1 (4010 , Request .. serial number)");
            strResponseData = ATClientUtils.atClient_readValue(4010, getActivity()
                    .getApplicationContext(), "");
            Log.i(LOG_TAG, "Serial Number:" + strResponseData);

            mRev = (Preference)findPreference(KEY_SERIAL_NUMBER);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    Runnable m_display_run2 = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run2 (4015 , Request ..  S/N)");
            strResponseData = ATClientUtils.atClient_readValue(4015, getActivity()
                    .getApplicationContext(), "");
            Log.i(LOG_TAG, "Manufacture SN:" + strResponseData);

            mRev = (Preference)findPreference(KEY_MANUFACTURE_SN);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    Runnable m_display_run3 = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run3 (4016 , Request .. Manufacture date)");
            strResponseData = ATClientUtils.atClient_readValue(4016, getActivity()
                    .getApplicationContext(), "");
            Log.i(LOG_TAG, "Manufacture date:" + strResponseData);

            mRev = (Preference)findPreference(KEY_MANUFACTURE_DATE);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    Runnable m_display_run4 = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run4 (4018 , Request .. CSN)");
            strResponseData = ATClientUtils.atClient_readValue(4018, getActivity()
                    .getApplicationContext(), "");
            Log.i(LOG_TAG, "CSN :" + strResponseData);

            mRev = (Preference)findPreference(KEY_CSN);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    Runnable m_display_run5 = new Runnable() {
        String mStrResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run5 (4018 , Request .. LG U+ SN)");
            mStrResponseData = ATClientUtils.atClient_readValue(4018,
                    getActivity().getApplicationContext(), "");
            Log.i(LOG_TAG, "LG U+ SN:" + mStrResponseData);

            mRev = (Preference)findPreference(KEY_LGU_SN);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(mStrResponseData);
            }
        }
    };

    //[E][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
/*
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
*/
    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.device_info_status);

        Log.i(LOG_TAG, "onCreate");

        mHandler = new MyHandler(this);
        mRes = getResources();
        sUnknown = mRes.getString(R.string.device_info_default);

        operator_code = Config.getOperator();
        Log.i(LOG_TAG, "operator_code:" + operator_code);

        setSummaryText(KEY_DEVICE_MODEL, Build.MODEL);
        setWifiStatus();
        setBtStatus();

        mUptime = findPreference("up_time");

        ATClientUtils.atClient_BindService(getActivity().getApplicationContext());
        regul_mHandler.postDelayed(m_display_run1, 50);
        regul_mHandler.postDelayed(m_display_run2, 50);
        regul_mHandler.postDelayed(m_display_run3, 50);
        regul_mHandler.postDelayed(m_display_run4, 50);
        regul_mHandler.postDelayed(m_display_run5, 50);

        removeUnnecessaryPreference();
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

    private void setWifiStatus() {
        Log.i(LOG_TAG, "setWifiStatus");

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
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
        String ipAddress = Utils.getWifiIpAddresses(getActivity().getApplicationContext());

        if (wifiIpAddressPref != null) {
            if (ipAddress != null) {
                wifiIpAddressPref.setSummary(ipAddress);
            } else {
                wifiIpAddressPref.setSummary(getString(R.string.status_unavailable));
            }
        }
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

    private void setSummaryText(String preference, String text) {
        // some preferences may be missing
        if (TextUtils.isEmpty(text)) {
            text = sUnknown;
        }
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text);
        }
    }

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

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mHandler.sendEmptyMessage(EVENT_UPDATE_STATS); // Up time
        Log.i(LOG_TAG, "Resume");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mHandler.removeMessages(EVENT_UPDATE_STATS);
        Log.i(LOG_TAG, "onPause");
    }

    //[S][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");

        if (Utils.isEmbededBattery(getActivity().getApplicationContext())) {
            if ("KR".equals(Config.getCountry())) {
                regul_mHandler.removeCallbacks(m_display_run1);
                regul_mHandler.removeCallbacks(m_display_run2);
                regul_mHandler.removeCallbacks(m_display_run3);
                regul_mHandler.removeCallbacks(m_display_run4);
                regul_mHandler.removeCallbacks(m_display_run5);
                ATClientUtils.atClient_unBindService();
            }
        } else {
            Log.i(LOG_TAG, "onPause (normal)");
        }

        super.onDestroy();
    }

    //[E][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

    protected void removeUnnecessaryPreference() {
        Preference removablePref;

        String[] keyStrings = {
                "button_aboutphone_msim_status",
                "battery_status",
                "battery_level",
                "operator_name",
                "signal_strength",
                "network_type",
                "network_type_strength",
                "service_state",
                "roaming_state",
                "data_state",
                "number",
                "min_number",
                "prl_version",
                "eri_version",
                "esn_number",
                "meid_number",
                "meid_hexa",
                "meid_decimal",
                "imei",
                "imei_sv",
                "imei_svn_gsm",
                "imei_svn",
                "channel",
                "sid",
                "icc_id",
                "last_factory_date_reset",
                "rooting_status",
                //"wifi_ip_address",
                //"wifi_mac_address",
                //"bt_address",
                //"serial_number",
                //"wimax_mac_address",
                //"up_time",
                "ims_registration_status",
                //"life_time_call",
                //"warranty_date_code",
                //"life_time_data",
                "battery_use",
                //"rev_check",
                "battery_condition_dcm",
                "support_technology",
                //"serial_number",
                "life_time_call",
                "warranty_date_code",
                "life_time_data",
                "rev_check",
                "smsc",
                "last_factory_date_reset",
                "rooting_status",
                "imsi",
                "refubish_counter",
                "wimax_mac_address",
                "memory_size",
                "cell_id",
                "prl_list",
                //"manufacture_serial_number",
                "manufacture_company_country",
                "latest_area_info",
                "main_software_version"
                //"manufacture_serial_number_date"
                //"device_model" 
        };

        for (String string : keyStrings) {
            removablePref = findPreference(string);
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }
        }

        if (!"awifi070u".equals(SystemProperties.get("ro.build.product"))) {
            super.getPreferenceScreen().removePreference(findPreference("serial_number"));
            super.getPreferenceScreen().removePreference(
                    findPreference("manufacture_serial_number"));
            super.getPreferenceScreen().removePreference(
                    findPreference("manufacture_serial_number_date"));
            super.getPreferenceScreen().removePreference(
                    findPreference("manufacture_lgu_serial_number"));
        }

        if (!Config.SPR.equals(Config.getOperator())) {
            super.getPreferenceScreen().removePreference(findPreference("sprint_brand"));
        }

    }
}
