package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.Preference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.deviceinfo.Status;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

public class DeviceInfoLgePhoneIdentity extends Status implements Indexable {
    //private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
    private static final String LOG_TAG = "aboutphone #DeviceinfolgePhoneidentity";

    //[S][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
    private static final String KEY_MANUFACTURE_SN = "manufacture_serial_number";
    //private static final String KEY_MANUFACTURE_C_C = "manufacture_company_country";
    private static final String KEY_MANUFACTURE_DATE = "manufacture_serial_number_date";
    private static final String KEY_SERIAL_NUMBER = "serial_number";
    private Preference mRev;
    private static String operator_code;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    private Handler regul_mHandler = new Handler();

    Runnable m_display_run1 = new Runnable() {
        String strResponseData = "";

        public void run() {
            Log.i(LOG_TAG, "m_display_run1 (4010 , Request .. serial number)");
            strResponseData = ATClientUtils.atClient_readValue(4010, getApplicationContext(), "");
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
            strResponseData = ATClientUtils.atClient_readValue(4015, getApplicationContext(), "");
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
            strResponseData = ATClientUtils.atClient_readValue(4016, getApplicationContext(), "");
            Log.i(LOG_TAG, "Manufacture date:" + strResponseData);

            mRev = (Preference)findPreference(KEY_MANUFACTURE_DATE);
            if (mRev != null) {
                //Log.i(LOG_TAG, "finded preference");
                mRev.setSummary(strResponseData);
            }
        }
    };

    //[E][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

    protected class SerialHandler extends Handler {

        private static final int EVENT_SERIAL_NUMBER = 1;
        private static final int EVENT_MANUFACTURE_DATE = 2;
        private static final int EVENT_MANUFACTURE_NUMBER = 3;

        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult)msg.obj;
            StringBuilder m_sbPhoneSerial = new StringBuilder();
            Log.d("starmotor", "ar.result 1= " + ar.result);
            switch (msg.what) {
            case EVENT_SERIAL_NUMBER: //serial number
                m_sbPhoneSerial.append(ar.result);
                setSummaryText(KEY_SERIAL_NUMBER, m_sbPhoneSerial.toString());
                break;
            case EVENT_MANUFACTURE_DATE: //date
                m_sbPhoneSerial.append(ar.result);
                setSummaryText(KEY_MANUFACTURE_DATE, m_sbPhoneSerial.toString());
                break;
            case EVENT_MANUFACTURE_NUMBER: //SN
                m_sbPhoneSerial.append(ar.result);
                setSummaryText(KEY_MANUFACTURE_SN, m_sbPhoneSerial.toString());
                break;

            default:
                break;

            }
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        super.setTitle(R.string.sp_HardwareInfo_NORMAL);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.i(LOG_TAG, "onCreate");

        /*
        Preference prefModelNumber = new Preference(this, null, android.R.attr.preferenceInformationStyle);
        prefModelNumber.setTitle(R.string.model_number);
        try {
            prefModelNumber.setSummary(Build.MODEL);
        } catch (RuntimeException e) {
            prefModelNumber.setSummary(R.string.device_info_default);
        }

        if (super.getPreferenceScreen() != null) {
            super.getPreferenceScreen().addPreference(prefModelNumber);
        }
        */

        //[S][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
        operator_code = Config.getOperator();
        Log.i(LOG_TAG, "operator_code:" + operator_code);

        if (Build.DEVICE.equals("geefhd4g") || Build.DEVICE.equals("geefhd")
                || Build.DEVICE.equals("omegar")) {
            if ("KR".equals(Config.getCountry())) {
                ATClientUtils.atClient_BindService(getApplicationContext());
                regul_mHandler.postDelayed(m_display_run1, 150);
            }
        } else if (Utils.isEmbededBattery(this)) {
            if ("KR".equals(Config.getCountry())) {
                    ATClientUtils.atClient_BindService(getApplicationContext());
                    regul_mHandler.postDelayed(m_display_run1, 50);
                    regul_mHandler.postDelayed(m_display_run2, 50);
                    regul_mHandler.postDelayed(m_display_run3, 50);
            } else {
                Log.i(LOG_TAG, "operator_code is is not support that embedded battery (x)");
            }
        } else {
            if ("KR".equals(Config.getCountry())) {
                ATClientUtils.atClient_BindService(getApplicationContext());
                regul_mHandler.postDelayed(m_display_run1, 150);
            }
            Log.i(LOG_TAG, "operator_code is is not support that embedded battery (x)");
        }
        //[E][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

        removeUnnecessaryPreference();
    }

    private void setSummaryText(String preference, String text) {
        // some preferences may be missing
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((Config.getOperator().equals("VZW")
            && com.lge.os.Build.LGUI_VERSION.RELEASE == com.lge.os.Build.LGUI_VERSION_NAMES.V4_1)
                || Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            getMenuInflater().inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        Log.d("starmotor", "itemId = " + itemId);
        if (item.getItemId() == R.id.search) {
            switchToSearchResults();
        }
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchToSearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i(LOG_TAG, "Resume");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    //[S][2012.10.10][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");

        if (Build.DEVICE.equals("geefhd4g")) {
            regul_mHandler.removeCallbacks(m_display_run1);
            ATClientUtils.atClient_unBindService();
        } else if (Utils.isEmbededBattery(this)) {
            if ("KR".equals(Config.getCountry())) {
                regul_mHandler.removeCallbacks(m_display_run1);
                regul_mHandler.removeCallbacks(m_display_run2);
                regul_mHandler.removeCallbacks(m_display_run3);
                ATClientUtils.atClient_unBindService();
            }
        } else {
            if ("KR".equals(Config.getCountry())) {
                regul_mHandler.removeCallbacks(m_display_run1);
                ATClientUtils.atClient_unBindService();
            }
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
                "wifi_ip_address",
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
                "imsi",
                "csn",
                "latest_area_info",
                "cell_id",
                "prl_list",
                "sprint_brand",
                "manufacture_lgu_serial_number",
                "main_software_version"
                //"manufacture_serial_number",
                //"manufacture_company_country",
                //"manufacture_serial_number_date",
                //"device_model"
        };

        String[] keyStrings_tablet = {
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
                "cell_id",
                "prl_list",
                "sprint_brand",
                "memory_size",
                "main_software_version"
                //"manufacture_serial_number",
                //"manufacture_company_country",
                //"manufacture_serial_number_date",
                //"device_model"
        };

        if (Utils.isWifiOnly(getApplicationContext())) {
            keyStrings = keyStrings_tablet;
        }

        for (String string : keyStrings) {
            removablePref = findPreference(string);
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }
        }
        // if persist.sys.cust.rmprefimeisv=true, remove imeisv field.
        if (SystemProperties.getBoolean("persist.sys.cust.rmprefimeisv", false)) {
            removablePref = findPreference("imei_sv");
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }
        }

        //must hide some fields for Iusacell/Unefon MX
        if ("UNF".equals(Config.getOperator()) &&
                "MX".equals(Config.getCountry()))
        {
            removablePref = findPreference("up_time");
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }

            removablePref = findPreference("smsc");
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }

            removablePref = findPreference("device_model");
            if (removablePref != null && super.getPreferenceScreen() != null) {
                super.getPreferenceScreen().removePreference(removablePref);
            }
        }

    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            String myClassName = "com.android.settings.lge.DeviceInfoLgePhoneIdentity";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings)
                    + " > "
                    + context.getString(R.string.sp_HardwareInfo_NORMAL);

            setSearchIndexData(context, "device_model",
                    context.getString(R.string.model_number),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "serial_number",
                    context.getString(R.string.status_serial_number),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "up_time",
                    context.getString(R.string.status_up_time),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "wifi_mac_address",
                    context.getString(R.string.status_wifi_mac_address),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "bt_address",
                    context.getString(R.string.status_bt_address),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);

            return mResult;
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
