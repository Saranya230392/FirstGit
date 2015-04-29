package com.android.settings.lge;

import com.android.settings.R;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;
import android.util.Log;
import android.app.ActionBar;
import android.view.MenuItem;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

import android.content.res.Configuration;
import com.lge.config.ConfigBuildFlags;

public class FrontTouchKey extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {
    private static final String TAG = "FrontTouchKey";
    private static final String KEY_EDIT_HOME_TOUCH_BUTTON = "edit_home_touch_buttons";
    private static final String KEY_THEME = "front_touch_key_theme";
    private static final String KEY_HIDE_NAVIGATION_NEW = "hide_navigation_new";
    private static final String KEY_BUTTON_COMBINATION_DRAG = "buttons_combination_drag";

    private static final String WHITE_DB = "#fff5f5f5";
    private static final String BLACK_DB = "#ff000000";

    private Preference mTheme;
    private Preference mHideNavigationNew;
    private Preference mButtonCombination;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    @Override
    public void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        createToggles();

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
                if (Utils.isUI_4_1_model(getActivity())) {
                    actionBar.setTitle(R.string.display_home_touch_buttons);
                }
            }
        }
    }

    public void createToggles() {
        addPreferencesFromResource(R.layout.front_touch_key);
        PreferenceScreen root = getPreferenceScreen();

        mTheme = (Preference)findPreference(KEY_THEME);
        mHideNavigationNew = (Preference)findPreference(KEY_HIDE_NAVIGATION_NEW);
        mButtonCombination = (Preference)findPreference(KEY_BUTTON_COMBINATION_DRAG);

        PreferenceCategory homeButtonEditCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(KEY_EDIT_HOME_TOUCH_BUTTON);

        if (!Config.getFWConfigBool(getActivity(),
                com.lge.config.ConfigBuildFlags.CAPP_HIDENAV,
                "com.lge.config.ConfigBuildFlags.CAPP_HIDENAV")) {
            if (homeButtonEditCategory != null) {
                homeButtonEditCategory
                        .removePreference(findPreference(KEY_HIDE_NAVIGATION_NEW));
            }
        }

        if ("VZW".equals(Config.getOperator())) {
            mHideNavigationNew
                    .setSummary(R.string.hide_home_touch_button_summary_ex_vzw);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Don't allow any changes to take effect as the USB host will be
        // disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        return true;
    }

    private void updateToggles() {
        String themeDbValue = Settings.System.getString(getContentResolver(),
                "navigation_bar_theme");
        Log.d(TAG, "Theme: " + themeDbValue);
        mTheme.setSummary(getThemeName(themeDbValue));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        getPreferenceScreen().removeAll();
        createToggles();
        updateToggles();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToggles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getThemeName(String themeDB) {
        if (themeDB.equals(WHITE_DB)) {
            return getResources().getString(R.string.display_theme_name_white);
        } else if (themeDB.equals(BLACK_DB)) {
            return getResources().getString(R.string.display_theme_name_black);
        } else {
            return "";
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            String screenTitle = context.getString(R.string.display_settings)
                    + " > "
                    + context.getString(R.string.display_home_touch_buttons);

            setSearchIndexData(context, KEY_BUTTON_COMBINATION_DRAG,
                    context.getString(R.string.button_combi_title),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ButtonCombinationDragActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
            setSearchIndexData(context, KEY_THEME,
                    context.getString(R.string.color_home_button),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$FrontTouchKeyThemeActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
            if (Config.getFWConfigBool(context,
                    com.lge.config.ConfigBuildFlags.CAPP_HIDENAV,
                    "com.lge.config.ConfigBuildFlags.CAPP_HIDENAV")) {
                String hide_nave_summary;
                if ("VZW".equals(Config.getOperator())) {
                    hide_nave_summary = context
                            .getString(R.string.hide_home_touch_button_summary_ex_vzw);
                } else {
                    hide_nave_summary = context
                            .getString(R.string.hide_home_touch_button_summary_ex);
                }
                setSearchIndexData(context, KEY_HIDE_NAVIGATION_NEW,
                        context.getString(R.string.hide_home_navigation_title),
                        screenTitle, hide_nave_summary, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.Settings$HideNavigationAppSelectActivity",
                        "com.android.settings", 1, null, null, null, 1, 0);
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
