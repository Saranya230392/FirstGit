package com.android.settings.lge;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.Preference;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.MenuItem;

import com.android.internal.telephony.PhoneConstants;
import com.android.settings.R;
import com.android.settings.SLog;
import com.android.settings.deviceinfo.Status;
import com.android.settings.utils.MSIMUtils;
import com.android.settings.utils.LGSubscriptionManager;

public class DeviceInfoLgeNetworkCtc extends Status
{
    public static String sNew_currentTab = null;

    @Override
    protected void onCreate(Bundle icicle)
    {
        // set the setCtcNetworkInfo() before called onCreate().
    	int mSubscription = getIntent().getIntExtra("subscription",
                MSIMUtils.getSlotIdBySubId(LGSubscriptionManager.getDefaultSubId()));
        setCtcNetworkInfo(mSubscription, 0);
        SLog.i("mSubscription = " + mSubscription);

        if (mSubscription == 0) {
            sNew_currentTab = "sim1";
        } else if (mSubscription == 1) {
            sNew_currentTab = "sim2";
        } else {
            sNew_currentTab = "common";
        }
        MSIMUtils.write_SharedPreference("tab", sNew_currentTab, getApplicationContext());
        super.onCreate(icicle);
        super.setTitle(R.string.ct_cur_network_info);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        Log.d("starmotor","itemId 11= " + itemId);
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        removeUnnecessaryPreference();
    }

    protected void removeUnnecessaryPreference()
    {
        Preference removablePref;

        String[] Sim1CdmakeyStrings = {
            "button_aboutphone_msim_status",
            "battery_status",
            "battery_level",
            // "operator_name",
            // "signal_strength",
            // "network_type",
            "service_state",
            "roaming_state",
            "data_state",
            "number",
            "min_number",
            //"prl_version",
            "eri_version",
            "esn_number",
            "meid_number",
            "meid_hexa",
            "meid_decimal",
            "imei",
            "imei_sv",
            "imei_svn",
            "imei_svn_gsm",
            "channel",
            //"sid",
            "icc_id",
            "last_factory_date_reset",
            "rooting_status",
            "wifi_ip_address",
            "wifi_mac_address",
            "bt_address",
            "up_time",
            "life_time_call",
            "warranty_date_code",
            "life_time_data",
            "battery_use",
            "rev_check",
            "manufacture_serial_number",
            "manufacture_company_country",
            "manufacture_serial_number_date",
            "battery_condition_dcm",
            "serial_number",
            "device_model",
            "smsc",
            //"imsi",
            "csn",
            "memory_size",
            "manufacture_lgu_serial_number",
            //"cell_id",
            "prl_list",
            "ims_registration_status",
            "main_software_version"
        };
        String[] Sim1GsmkeyStrings = {
            "button_aboutphone_msim_status",
            "battery_status",
            "battery_level",
            // "operator_name",
            // "signal_strength",
            // "network_type",
            "service_state",
            "roaming_state",
            "data_state",
            "number",
            "min_number",
            //"prl_version",
            "eri_version",
            "esn_number",
            "meid_number",
            "meid_hexa",
            "meid_decimal",
            "imei",
            "imei_sv",
            "imei_svn",
            "imei_svn_gsm",
            "channel",
            "sid",
            "icc_id",
            "last_factory_date_reset",
            "rooting_status",
            "wifi_ip_address",
            "wifi_mac_address",
            "bt_address",
            "up_time",
            "life_time_call",
            "warranty_date_code",
            "life_time_data",
            "battery_use",
            "rev_check",
            "manufacture_serial_number",
            "manufacture_company_country",
            "manufacture_serial_number_date",
            "battery_condition_dcm",
            "serial_number",
            "device_model",
            //"smsc",
            //"imsi",
            "csn",
            "memory_size",
            "manufacture_lgu_serial_number",
            //"cell_id",
            "prl_list",
            "ims_registration_status",
            "main_software_version"
        };
        String[] Sim2keyStrings = {
            "button_aboutphone_msim_status",
            "battery_status",
            "battery_level",
            // "operator_name",
            // "signal_strength",
            // "network_type",
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
            "imei_svn",
            "imei_svn_gsm",
            "channel",
            "sid",
            "icc_id",
            "last_factory_date_reset",
            "rooting_status",
            "wifi_ip_address",
            "wifi_mac_address",
            "bt_address",
            "up_time",
            "life_time_call",
            "warranty_date_code",
            "life_time_data",
            "battery_use",
            "rev_check",
            "manufacture_serial_number",
            "manufacture_company_country",
            "manufacture_serial_number_date",
            "battery_condition_dcm",
            "serial_number",
            "device_model",
            //"smsc",
            //"imsi",
            "csn",
            "memory_size",
            "manufacture_lgu_serial_number",
            "ims_registration_status",
            "cell_id",
            "prl_list",
            "main_software_version"
        };
        String[] keyStrings;
        if (mCtcSlotID == GEMINI_SIM_2) {
            keyStrings = Sim2keyStrings;
        } else if (mCtcNetworkType == PhoneConstants.PHONE_TYPE_CDMA) {
            keyStrings = Sim1CdmakeyStrings;
        } else {
            keyStrings = Sim1GsmkeyStrings;
        }

        for(String string : keyStrings)
        {
            removablePref = findPreference(string);
            if(removablePref != null && super.getPreferenceScreen() != null)
            {
                super.getPreferenceScreen().removePreference(removablePref);
            }
        }
    }
    
}
