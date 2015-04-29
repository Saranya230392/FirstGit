package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.deviceinfo.Status;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

public class DeviceInfoLgeHardwareInformation extends Status implements Indexable
{
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    protected void onCreate(Bundle icicle)
    {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        super.setTitle(R.string.sp_HardwareInfo_NORMAL);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        Log.d("starmotor", "itemId = " + itemId);
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
                "icc_id",
                "last_factory_date_reset",
                "rooting_status",
                "wifi_ip_address",
                "serial_number",
                "wimax_mac_address",
                "up_time",
                "battery_use",
                "imei_svn",
                "ims_registration_status",
                "life_time_call",
                "warranty_date_code",
                "life_time_data",
                "imei_svn_gsm", //insook.kim@lge.com 2012.05.13: add item
                "channel",
                "sid",
                "manufacture_serial_number",
                "manufacture_company_country",
                "manufacture_serial_number_date",
                "battery_condition_dcm",
                "support_technology",
                "csn",
                "manufacture_lgu_serial_number",
                "cell_id",
                "prl_list",
                "main_software_version"
        };

        for (String string : keyStrings)
        {
            removablePref = findPreference(string);
            if (removablePref != null)
            {
                if (super.getPreferenceScreen() != null)
                {
                    super.getPreferenceScreen().removePreference(removablePref);
                }
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
