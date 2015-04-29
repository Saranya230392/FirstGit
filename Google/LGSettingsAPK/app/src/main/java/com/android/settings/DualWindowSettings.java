package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.lge.constants.SettingsConstants;
import com.android.settings.MDMSettingsAdapter;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;

public class DualWindowSettings extends SettingsPreferenceFragment implements
        OnCheckedChangeListener, Indexable {

    //private static final String TAG = "DualWindowSettings";
    private static final String INDEX_DUAL_WINDOW_ACTION_NAME =
            "com.lge.settings.ACTION_DUAL_WINDOW";
    private Switch mSwitch;
    private CheckBoxPreference mAppSplitViewPreferences;
    //private Preference mHelpPreferences;

    SettingsBreadCrumb mBreadCrumb;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Activity activity = getActivity();

        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });

        mSwitch.setOnCheckedChangeListener(this);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }

        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(
                    mSwitch,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                                    | Gravity.END));
        }
        setHasOptionsMenu(true);
        initPreference();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
        if (com.lge.mdm.LGMDMManager.getInstance() != null) {
            IntentFilter filterLGMDM = new android.content.IntentFilter();
            MDMSettingsAdapter.getInstance().addDualWindowSettingChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        mIsFirst = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            Log.d("search", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void goMainScreen() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.android.settings",
                "com.android.settings.Settings$DualWindowSettingsActivity");
        startActivity(i);
        getActivity().finish();
    }

    private void startResult(boolean newValue) {
        if (Utils.supportSplitView(getActivity())) {
            goMainScreen();
            return;
        }

        if (mSearch_result.equals("multitasking_app_split_view")) {
            mAppSplitViewPreferences.performClick(getPreferenceScreen());
            return;
        }
        goMainScreen();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Activity activity = getActivity();
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
        if (com.lge.mdm.LGMDMManager.getInstance() != null) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    private void initPreference() {

        addPreferencesFromResource(R.xml.dualwindow_settings);

        if (Utils.supportSplitView(getActivity()) == false) {
            Preference p = findPreference("multitasking_dual_window");
            if (p != null && getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(p);
            }
        }

        mAppSplitViewPreferences = (CheckBoxPreference)findPreference("multitasking_app_split_view");
        //mHelpPreferences = (Preference)findPreference("multitasking_help");
        if (DualWindowHelp.checkThinkFreeVersion(getActivity()) == false) {
        	mAppSplitViewPreferences.setSummary(R.string.dualwindow_splitview_description2);
        }

        ContentResolver resolver = getContentResolver();

        if (Settings.System.getInt(resolver, "auto_split_view", 0) == 1) {
            mAppSplitViewPreferences.setChecked(true);
        } else {
            mAppSplitViewPreferences.setChecked(false);
        }

        boolean isEnabled = (Settings.System.getInt(getContentResolver(),
                "dual_window", 1) > 0);
        mSwitch.setChecked(isEnabled);
        mAppSplitViewPreferences.setEnabled(isEnabled);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
        if (com.lge.mdm.LGMDMManager.getInstance() != null) {
            if (MDMSettingsAdapter.getInstance().checkDisallowDualWindow(getActivity())) {
                // Switch
                mSwitch.setChecked(false);
                mSwitch.setEnabled(false);

                // Split View
                mAppSplitViewPreferences.setChecked(false);
                mAppSplitViewPreferences.setEnabled(false);
                mAppSplitViewPreferences.setSummary(R.string.sp_lgmdm_block_dualwindow_NORMAL);
                getActivity().sendBroadcast(new Intent(INDEX_DUAL_WINDOW_ACTION_NAME));
            }
        }
        // LGMDM_END
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();

        if (preference == mAppSplitViewPreferences) {
            Settings.System.putInt(resolver, "auto_split_view",
                    mAppSplitViewPreferences.isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            int resultOption = Utils.getUsingSettings(settingStyle, getActivity());
            if (resultOption == Utils.sUSING_EASYSETTINGS) {
                getActivity().onBackPressed();
                return true;
            } else if (resultOption == Utils.sUSING_SETTINGS) {
                finish();
                return true;
            }
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean status) {
        /*if (mSwitch != null) {
            mSwitch.setChecked(status);
        }*/
        Settings.System.putInt(getContentResolver(), "dual_window", status ? 1 : 0);
        mAppSplitViewPreferences.setEnabled(status);
        getActivity().sendBroadcast(new Intent(INDEX_DUAL_WINDOW_ACTION_NAME));
    }

    //LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (com.lge.mdm.LGMDMManager.getInstance() != null) {
                if (MDMSettingsAdapter.getInstance().receiveDualWindowPolicyChangeIntent(intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }
    };
   // LGMDM_END
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            setSearchIndexData(context, "dualwindow_settings",
                    context.getString(R.string.app_split_view_dual_window), "main",
                    null, null,
                    "com.lge.settings.DUALWINDOW_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);

            String strSearchCategoryTitle = context.getString(R.string.multitasking_split_view_title);
            int checkValue = Settings.System.getInt(context.getContentResolver(), "auto_split_view", 0);
            setSearchIndexData(context, "multitasking_app_split_view",
                    context.getString(R.string.multitasking_split_view_title),
                    strSearchCategoryTitle,
                    null, null, "com.lge.settings.DUALWINDOW_SETTINGS",
                    null, null, 1,
                    "CheckBox", "System", "auto_split_view", 1, checkValue);

            setSearchIndexData(context, "multitasking_help",
                    context.getString(R.string.sp_gesture_category_help_NOMAL),
                    strSearchCategoryTitle,
                    null, null, "com.lge.settings.DUALWINDOW_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);
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
