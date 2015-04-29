package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;

import com.android.settings.DeviceInfoSettings;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

public class DeviceInfoLgeSoftwareInformation extends DeviceInfoSettings implements Indexable
{

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    public void onCreate(Bundle icicle)
    {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        //        getActivity().setTitle(R.string.softwareinformation_title);
        removeUnnecessaryPreference();
    }

    /*
        @Override
        protected void onResume()
        {
            // TODO Auto-generated method stub
            super.onResume();
            removeUnnecessaryPreference();
        }

        @Override
        public void onPause()
        {
            // TODO Auto-generated method stub
            super.onPause();
        }
    */
    protected void removeUnnecessaryPreference()
    {
        Preference removablePref;

        String[] keyStrings = {
                "system_update_settings",
                "additional_system_update_settings",
                "status_info",
                "power_usage",
                "container",
                "safetylegal",
                "device_model",
                "phone_serial",
                "carrier_legal",
                "software_update_settings_for_dcm",
                "rev_check",
                "mpcs_legal",
                "manufacture_serial_number",
                "manufacture_company_country",
                "manufacture_serial_number_date",
                "main_software_version"
        };

        for (String string : keyStrings)
        {
            removablePref = findPreference(string);
            if (removablePref != null && super.getPreferenceScreen() != null) //[2012.09.27][munjohn.kang] WBT
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
            String myClassName = "com.android.settings.Settings$DeviceInfoLgeSoftwareInformationActivity";
            String myPackageName = "com.android.settings";
            String screenTitle = context.getString(R.string.about_settings)
                    + " > "
                    + context.getString(R.string.sp_Softwareinfo_NORMAL);

            setSearchIndexData(context, "firmware_version",
                    context.getString(R.string.firmware_version),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "baseband_version",
                    context.getString(R.string.baseband_version),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "kernel_version",
                    context.getString(R.string.kernel_version),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "build_number",
                    context.getString(R.string.build_number),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "vzw_configuration_version",
                    context.getString(R.string.sp_configuration_version_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    myClassName, myPackageName, 1, null, null, null, 1, 0);
            setSearchIndexData(context, "software_version",
                    context.getString(R.string.sp_SoftwareVersion_NORMAL),
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
