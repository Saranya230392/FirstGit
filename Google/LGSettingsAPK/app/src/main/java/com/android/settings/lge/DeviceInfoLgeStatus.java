package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.Status;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

public class DeviceInfoLgeStatus extends Status implements Indexable {
    //private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
    private static final String LOG_TAG = "aboutphone #DeviceinfolgePhoneidentity";
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        super.setTitle(R.string.device_status);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        removeUnnecessaryPreference();
        Log.i(LOG_TAG, "onCreate");
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
                //"number",
                //"min_number",
                //"prl_version",
                //"eri_version",
                //"esn_number",
                //"meid_number",
                //"meid_hexa",
                //"meid_decimal",
                //"imei",
                //"imei_sv",
                "channel",
                "sid",
                //"icc_id",
                "wifi_ip_address",
                "wifi_mac_address",
                "bt_address",
                "serial_number",
                "wimax_mac_address",
                "up_time",
                //"ims_registration_status",
                "life_time_call",
                "warranty_date_code",
                "life_time_data",
                "battery_use",
                "rev_check",
                "battery_condition_dcm",
                "support_technology",
                "manufacture_serial_number",
                "manufacture_company_country",
                "manufacture_serial_number_date",
                "device_model",
                "smsc",
                "csn",
                "memory_size",
                "latest_area_info",
                "cell_id",
                "prl_list",
                "manufacture_lgu_serial_number"
                //"imsi"
        };

        for (String string : keyStrings) {
            //must show some fields for Iusacell/Unefon MX
            if ("up_time".equals(string) || "smsc".equals(string) ||
                    "device_model".equals(string))
            {
                if ("UNF".equals(Config.getOperator()) && "MX".equals(Config.getCountry()))
                {
                    continue;
                }
            }

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
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            String myClassName = "com.android.settings.lge.DeviceInfoLgeStatus";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings)
                    + " > "
                    + context.getString(R.string.device_status);

            setSearchIndexData(context, "number",
                    context.getString(R.string.status_number),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "imei",
                    context.getString(R.string.status_imei),
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
