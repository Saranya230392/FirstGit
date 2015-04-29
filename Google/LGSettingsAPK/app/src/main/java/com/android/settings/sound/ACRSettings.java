package com.android.settings.sound;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.SettingsConstants;

public class ACRSettings extends SettingsPreferenceFragment implements
        OnClickListener, Indexable {
    private Switch mSwitch;
    SettingsBreadCrumb mBreadCrumb;
    private ListPreference mApplyFor;
    private Preference mHelp;

    private static final String KEY_ACR_APPLY_FOR = "applyfor";
    private static final String KEY_ACR_HELP = "acrhelp";

    private static int sACREnabled = 0;
    private static int sApplyValue = 0;

    private final static int FOR_ALL = 0;
    private final static int FOR_CONTACTS = 1;
    private final static int FOR_FAVORITES = 2;
    // Search
    private boolean mNewValue;

    // ACR Preference
    private Preference mACRPref;
    private static final String KEY_ARC = "acrprf";

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);

        Activity activity = getActivity();

        sACREnabled = Settings.System.getInt(getContentResolver(), SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, 0);

        mSwitch = new Switch(activity);
        mSwitch.setChecked(1 == sACREnabled ? true : false);
        mSwitch.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSwitch.setChecked(isChecked);
                Settings.System.putInt(getContentResolver(), SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, true == isChecked ? 1 : 0);
            }
        });
        setActionBarBreadCrumb(activity);

        addPreferencesFromResource(R.xml.acr_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        mACRPref = (Preference)prefSet.findPreference(KEY_ARC);
        mACRPref.setLayoutResource(R.layout.battery_use_detail_preference_information);
        mACRPref.setTitle(getResources().getString(
                R.string.acr_main_scren_category));
        mACRPref.setSelectable(false);

        mApplyFor = (ListPreference)prefSet.findPreference(KEY_ACR_APPLY_FOR);
        mApplyFor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // do whatever you want with new value
                mApplyFor.setValue((String)newValue);
                Settings.System.putInt(getContentResolver(), SettingsConstants.System.AUTO_COMPOSED_RINGTONES_APPLY_TO, Integer.valueOf((String)newValue).intValue());
                updateApplyForSummary(Integer.valueOf((String)newValue).intValue());

                // true to update the state of the Preference with the new value
                // in case you want to disallow the change return false
                return true;
            }
        });

        mHelp = (Preference)prefSet.findPreference(KEY_ACR_HELP);

        sApplyValue = Settings.System.getInt(getContentResolver(), SettingsConstants.System.AUTO_COMPOSED_RINGTONES_APPLY_TO, 1);
        updateApplyForSummary(sApplyValue);
        mApplyFor.setValue(Integer.toString(sApplyValue));
        mIsFirst = true;
    }


    private void setActionBarBreadCrumb(Activity activity) {
        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(
                    ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(
                    mSwitch,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
        }

        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));

        if (activity.getActionBar() != null) {
            if (Utils.supportSplitView(getActivity())) {
                activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            } else {
                activity.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mApplyFor) {
            int applyValue = Settings.System.getInt(getContentResolver(), SettingsConstants.System.AUTO_COMPOSED_RINGTONES_APPLY_TO, 1);
            mApplyFor.setValue(Integer.toString(applyValue));
        }

        return false;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
        if (getActivity().getIntent().hasExtra("newValue")) {
            Log.d("jw", "hasExtra(newValue)");
            mNewValue = getActivity().getIntent().getBooleanExtra("newValue", false);
            startResult(mNewValue);
        }
        setSearchPerformClick();
    }

    public void startResult(boolean value) {
        Settings.System.putInt(getContentResolver(),
                SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED,
                value ? 1 : 0);
        mSwitch.setChecked(value);
    }

    public void onClick(View arg0) {
    };

    private void updateApplyForSummary(int mode) {
        switch (mode) {
        case FOR_ALL:
            mApplyFor.setSummary(R.string.acr_main_preference_applyfor_all);
            break;
        case FOR_CONTACTS:
            mApplyFor.setSummary(R.string.acr_main_preference_applyfor_contacts);
            break;
        case FOR_FAVORITES:
            mApplyFor.setSummary(R.string.zen_mode_from_starred);
            break;
        default:
            break;
        }
    }

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    private void setSearchPerformClick() {
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        mNewValue = getActivity().getIntent()
                .getBooleanExtra("newValue", false);

        Log.d("jw", "mSearch_result : " + mSearch_result);
        Log.d("jw", "mNewValue : " + mNewValue);

        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra(
                "perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
    }

    private void startResult() {
        if (mSearch_result.equals(KEY_ACR_APPLY_FOR)) {
            mApplyFor.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_ACR_HELP)) {
            mHelp.performClick(getPreferenceScreen());
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_opera_ringtone,
                    "com.lge.R.bool.config_opera_ringtone")) {

                String mHelpCategory = context
                        .getString(R.string.notification_settings)
                        + " > "
                        + context.getString(R.string.acr_preference_main);

                setSearchIndexData(context, KEY_ACR_APPLY_FOR,
                        context.getString(R.string.acr_main_preference_applyfor),
                        mHelpCategory, null, null,
                        "com.android.settings.ACR_SETTINGS", null, null, 1, null,
                        null, null, 1, 0);

                setSearchIndexData(context, KEY_ACR_HELP,
                        context.getString(R.string.help_label), mHelpCategory,
                        null, null, "com.android.settings.ACR_SETTINGS", null,
                        null, 1, null, null, null, 1, 0);
            }


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
