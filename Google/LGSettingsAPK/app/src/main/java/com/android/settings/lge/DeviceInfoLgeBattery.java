package com.android.settings.lge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.LGIntent;
import com.lge.constants.SettingsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeviceInfoLgeBattery extends SettingsPreferenceFragment
                implements Indexable {
    private static final String KEY_BATTERY_STATUS = "battery_status";
    private static final String KEY_BATTERY_LEVEL = "battery_level";
    private static final String KEY_BATTERY_USE = "battery_use";
    private static final String KEY_BATTERY_CONDITION = "battery_condition_dcm";

    private Preference mBatteryStatus;
    private Preference mBatteryLevel;
    private Preference mBatteryCondition;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LGIntent.ACTION_BATTERYEX_CHANGED.equals(action)
                    || Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                String batteryLevel = Utils.getBatteryPercentage(intent);
                String batteryStatus = Utils.getBatteryStatus(getResources(),
                        intent);

                Locale systemLocale = getResources().getConfiguration().locale;
                String language = systemLocale.getLanguage();

                String str_Discharging = getResources().getString(
                        R.string.battery_info_status_discharging);
                String str_Notcharging = getResources().getString(
                        R.string.battery_info_status_not_charging);

                if (mBatteryLevel != null) {
                    mBatteryLevel.setSummary(batteryLevel);
                }

                if (mBatteryStatus != null) {
                    if ("ko".equals(language)
                            && (batteryStatus.equals(str_Discharging) || batteryStatus
                                    .equals(str_Notcharging))) {
                        mBatteryStatus.setSummary("");
                    } else {
                        mBatteryStatus.setSummary(batteryStatus);
                    }
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.device_info_status);

        mBatteryStatus = (Preference)findPreference(KEY_BATTERY_STATUS);
        mBatteryLevel = (Preference)findPreference(KEY_BATTERY_LEVEL);

        if ((Utils.isEmbededBattery(getActivity().getApplicationContext()) && "DCM".equals(Config
                .getOperator()))) {
            batteryCondition();
            removePreferenceFromScreen(KEY_BATTERY_STATUS);
            removePreferenceFromScreen(KEY_BATTERY_LEVEL);
            removePreferenceFromScreen(KEY_BATTERY_USE);

        } else {
            removePreferenceFromScreen(KEY_BATTERY_CONDITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        removeUnnecessaryPreference();
        return v;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (Config.VZW.equals(Config.getOperator())) {
            getActivity().registerReceiver(mBatteryInfoReceiver,
                    new IntentFilter(LGIntent.ACTION_BATTERYEX_CHANGED));
        } else {
            getActivity().registerReceiver(mBatteryInfoReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getActivity().unregisterReceiver(mBatteryInfoReceiver);
    }

    private void batteryCondition() {
        int condition = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.BATTERY_CONDITION, 1);

        mBatteryCondition = (Preference)findPreference(KEY_BATTERY_CONDITION);

        switch (condition) {
        case 0:
        case 1:
            mBatteryCondition.setSummary(R.string.sp_battery_condition_b_NORMAL);
            break;

        case 2:
            mBatteryCondition.setSummary(R.string.sp_battery_condition_g_NORMAL);
            break;

        case 3:
            mBatteryCondition.setSummary(R.string.sp_battery_condition_ba_NORMAL);
            break;

        default:
            break;
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    protected void removeUnnecessaryPreference() {
        Preference removablePref;

        String[] keyStrings = {
                "button_aboutphone_msim_status",
                "number",
                "min_number",
                "prl_version",
                "meid_number",
                "imei",
                "imei_sv",
                "imei_svn",
                "imei_svn_gsm",
                "operator_name",
                "signal_strength",
                "network_type",
                "service_state",
                "roaming_state",
                "data_state",
                "wimax_mac_address",
                "wifi_mac_address",
                "bt_address",
                "esn_number",
                "wifi_ip_address",
                "up_time",
                "serial_number",
                "icc_id",
                "last_factory_date_reset",
                "rooting_status",
                "eri_version",
                "ims_registration_status",
                "life_time_call",
                "warranty_date_code",
                "network_type_strength",
                "meid_hexa",
                "meid_decimal",
                "channel",
                "sid",
                "life_time_data",
                "carrier_legal",
                "rev_check",
                "refubish_counter",
                "mpcs_legal",
                "manufacture_serial_number",
                "manufacture_company_country",
                "manufacture_serial_number_date",
                "support_technology",
                "device_model",
                "smsc",
                "imsi",
                "csn",
                "memory_size",
                "manufacture_lgu_serial_number",
                "latest_area_info",
                "cell_id",
                "prl_list",
                "sprint_brand",
                "main_software_version"
        };

        for (String string : keyStrings) {
            removablePref = findPreference(string);
            if (removablePref != null && super.getPreferenceScreen() != null) {
                //                if(!(string == "channel" || string == "sid" ))
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
            String myClassName = "com.android.settings.Settings$DeviceInfoLgeBatteryActivity";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings)
                    + " > "
                    + context.getString(R.string.power_usage_summary_title);

            setSearchIndexData(context, "battery_status",
                    context.getString(R.string.battery_status_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "battery_level",
                    context.getString(R.string.battery_level_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "battery_use",
                    context.getString(R.string.sp_powersave_battery_usage_on_battery_text_vzw_NORMAL),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$PowerSaveBatteryDetailActivity",
                    myPackageName, 1, null, null, null, 1, 0);

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
