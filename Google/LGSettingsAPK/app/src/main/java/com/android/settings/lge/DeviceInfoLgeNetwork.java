package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.Status;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.utils.MSIMUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

public class DeviceInfoLgeNetwork extends Status implements Indexable
{
    private static final String LOG_TAG = "aboutphone # DeviceInfoLgeNetwork";
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    protected void onCreate(Bundle icicle)
    {
        if (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) &&
                (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())) {
            String tabIndex = MSIMUtils.read_SharedPreference("tab", getApplicationContext());
            setSlotIDFromAboutphone(MSIMUtils.makeSlotIDByTabName(tabIndex));
            Log.d(LOG_TAG, "tabIndex = " + tabIndex);
        }
        super.onCreate(icicle);
        super.setTitle(R.string.status_operator);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        Log.d("starmotor","itemId 11= " + itemId);
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
            // "operator_name",
            // "signal_strength",
            // "network_type",
            // "service_state",
            // "roaming_state",
            // "data_state",
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
            // "channel",
            // "sid",
            "icc_id",
            "last_factory_date_reset",
            "rooting_status",
            // "wifi_ip_address",
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
            "imsi",
            "csn",
            "memory_size",
            "manufacture_lgu_serial_number",
            "ims_registration_status",
            "cell_id",
            "prl_list",
            "sprint_brand",
            "main_software_version"
        };

        for(String string : keyStrings)
        {
            removablePref = findPreference(string);
            if(removablePref != null && super.getPreferenceScreen() != null) //[2012.09.27][munjohn.kang] WBT
            {
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
            String myClassName = "com.android.settings.lge.DeviceInfoLgeNetwork";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings)
                    + " > "
                    + context.getString(R.string.status_operator);

            setSearchIndexData(context, "operator_name",
                    context.getString(R.string.status_operator_lge),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "signal_strength",
                    context.getString(R.string.status_signal_strength),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "network_type",
                    context.getString(R.string.status_network_type),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "service_state",
                    context.getString(R.string.status_service_state),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "roaming_state",
                    context.getString(R.string.status_roaming),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "data_state",
                    context.getString(R.string.status_data_state),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "wifi_ip_address",
                    context.getString(R.string.wifi_advanced_ip_address_title),
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
