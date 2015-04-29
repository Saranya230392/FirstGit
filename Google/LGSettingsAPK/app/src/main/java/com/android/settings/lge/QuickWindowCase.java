package com.android.settings.lge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.MDMSettingsAdapter;
import com.lge.constants.SettingsConstants;

/**
 * @class    QuickWindowCase
 * @date     2013-11-15
 * @update   2013-11-20
 * @author   seungyeop.yeom
 * @brief    Function of new QuickWindow case for KK(B1,G2)
 * @warnig   Nothing
 */
public class QuickWindowCase extends PreferenceActivity
        implements OnCheckedChangeListener, OnPreferenceChangeListener, Indexable {

    private static final String KEY_MESSAGES_SETTING = "quick_window_messages_setting";
    private static final String KEY_AUTO_UNLOCK_SETTING = "quick_window_auto_unlock_setting";
    private static final String KEY_SELECT_APPS_SETTING = "quick_window_select_apps_setting";
    private static final String KEY_CLOSING_SOUND_SETTING = "cover_closing_sound_setting";
    private static final String KEY_HELP_SETTING = "quick_window_help_setting";
    private static final String KEY_CIRCLE_WINDOW_LIGHTING = "circle_window_lighting_setting";
    private static final String KEY_PREVIEW_EMAILS_MESSAGES = "preview_emails_messages_setting";
    private static final boolean NEW_UI_VERSION = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;

    private CheckBoxPreference mMessagesCheck;
    private CheckBoxPreference mAutoUnlockCheck;
    private CheckBoxPreference mClosingSoundCheck;
    private CheckBoxPreference mCircleLightingCheck;
    private CheckBoxPreference mPreviewEmailsMessagesCheck;

    private Preference mSelectApps;

    private ContentResolver mResolver;
    private Switch mSwitch;
    private Context mContext;
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Define QuickWindow case Observer
    // 2013-11-15
    // seungyeop.yeom
    private ContentObserver mQuickWindowObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean isEnabled = (Settings.Global.getInt(mResolver,
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 1) != 0);

            if (Utils.is_G_model_Device(Build.DEVICE)) {
                mMessagesCheck.setEnabled(true);
                mSelectApps.setEnabled(true);
                mAutoUnlockCheck.setEnabled(true);
                mClosingSoundCheck.setEnabled(true);
                mCircleLightingCheck.setEnabled(true);
                mPreviewEmailsMessagesCheck.setEnabled(true);
            } else {
                mMessagesCheck.setEnabled(isEnabled);
                mSelectApps.setEnabled(isEnabled);
                mAutoUnlockCheck.setEnabled(isEnabled);
                mClosingSoundCheck.setEnabled(isEnabled);
                mCircleLightingCheck.setEnabled(isEnabled);
                mPreviewEmailsMessagesCheck.setEnabled(isEnabled);
            }
        }
    };

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content
            .BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (MDMSettingsAdapter.getInstance().receiveQuickWindowCaseChangeIntent(intent)) {
                finish();
            }
        }
    };
    // LGMDM_END

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        mContext = getApplicationContext();
        mResolver = getContentResolver();
        addPreferencesFromResource(R.xml.quick_window_case);

        PreferenceScreen root = getPreferenceScreen();

        // Create and set up the switch of QuickWindow case.
        mSwitch = new Switch(this);
        if (Utils.is_G_model_Device(Build.DEVICE)) {
            mSwitch.setVisibility(View.GONE);
            mSwitch.setEnabled(false);
            mSwitch.setFocusable(false);
        }
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub	
            }
        });
        mSwitch.setOnCheckedChangeListener(this);
        final int padding = getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
        mSwitch.setPaddingRelative(0, 0, padding, 0);

        // Action bar where the switch is located.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (Config.getFWConfigBool(this,
                    com.lge.R.bool.config_using_window_cover,
                    "com.lge.R.bool.config_using_window_cover") == true) {
                setTitle(R.string.display_quick_cover_window_title);
            } else if (Config.getFWConfigBool(this,
                            com.lge.R.bool.config_using_circle_cover,
                            "com.lge.R.bool.config_using_circle_cover") == true) {
                setTitle(R.string.quick_circle_case_title);
            } else if (Config.getFWConfigBool(this,
                    com.lge.R.bool.config_using_disney_cover,
                    "com.lge.R.bool.config_using_disney_cover") == true) {
                setTitle(R.string.disney_case_title_ex);
            }

            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);

            getActionBar().setCustomView(mSwitch,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                                    | Gravity.END));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        boolean isEnabled = (Settings.Global.getInt(mResolver,
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 1) != 0);
        mSwitch.setChecked(isEnabled);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
        if (false == com.lge.mdm.LGMDMManager.getInstance()
                .getAllowQuickCircle(null)) {
            mSwitch.setEnabled(false);
        } else {
            mSwitch.setEnabled(true);
        }
        // LGMDM_END

        // create an object of Select App
        mSelectApps = (Preference)root.findPreference(KEY_SELECT_APPS_SETTING);

        // create and set up object of View call logs and messages
        mMessagesCheck = (CheckBoxPreference)root.findPreference(KEY_MESSAGES_SETTING);

        if (!Utils.checkPackage(this, "com.lge.widget.quickcovercontacts")) {
            root.removePreference(mMessagesCheck);
        }

        mMessagesCheck.setOnPreferenceChangeListener(this);

        boolean isMessagesEnable = Settings.Global.getInt(mResolver,
                SettingsConstants.Global.WINDOW_MESSAGES_ENABLE, 0) == 1 ? true : false;

        mMessagesCheck.setChecked(isMessagesEnable);

        // create and set up object of Auto-unlock screen
        mAutoUnlockCheck = (CheckBoxPreference)root.findPreference(KEY_AUTO_UNLOCK_SETTING);

        if (NEW_UI_VERSION) {
            root.removePreference(mAutoUnlockCheck);
        }

        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_using_circle_cover,
                "com.lge.R.bool.config_using_circle_cover") == false
                && Config.getFWConfigBool(mContext,
                        com.lge.R.bool.config_using_disney_cover,
                        "com.lge.R.bool.config_using_disney_cover") == false) {
            root.removePreference(mAutoUnlockCheck);
        }

        mAutoUnlockCheck.setOnPreferenceChangeListener(this);
        boolean isAutoUnlockEnable = Settings.Global.getInt(mResolver,
                SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 1) == 1 ? true : false;

        mAutoUnlockCheck.setChecked(isAutoUnlockEnable);

        // create and set up object of Cover closing sound
        mClosingSoundCheck = (CheckBoxPreference)root.findPreference(KEY_CLOSING_SOUND_SETTING);

        root.removePreference(mClosingSoundCheck);

        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_using_circle_cover,
                "com.lge.R.bool.config_using_circle_cover") == false) {
            root.removePreference(mClosingSoundCheck);
        }

        mClosingSoundCheck.setOnPreferenceChangeListener(this);
        boolean isCoverSoundEnable = Settings.Global.getInt(mResolver,
                SettingsConstants.Global.COVER_CLOSING_SOUND, 0) == 1 ? true : false;

        mClosingSoundCheck.setChecked(isCoverSoundEnable);

        // create and set up object of Circle window lighting
        mCircleLightingCheck = (CheckBoxPreference)root.findPreference(KEY_CIRCLE_WINDOW_LIGHTING);

        root.removePreference(mCircleLightingCheck);

        mCircleLightingCheck.setOnPreferenceChangeListener(this);
        boolean isCircleLightingEnable = Settings.Global.getInt(mResolver,
                SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 1) == 1 ? true : false;

        mCircleLightingCheck.setChecked(isCircleLightingEnable);

        Log.d("YSY", "Circle Lighting Check : " + isCircleLightingEnable);

        // create and set up object of preview emails and messages
        mPreviewEmailsMessagesCheck = (CheckBoxPreference)root
                .findPreference(KEY_PREVIEW_EMAILS_MESSAGES);

        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_using_disney_cover,
                "com.lge.R.bool.config_using_disney_cover") == false) {
            root.removePreference(mPreviewEmailsMessagesCheck);
        }

        mPreviewEmailsMessagesCheck.setOnPreferenceChangeListener(this);
        boolean isPreviewEmailsMessagesEnable = Settings.Global.getInt(
                mResolver, "preview_emails_messages", 1) == 1 ? true
                : false;

        mPreviewEmailsMessagesCheck.setChecked(isPreviewEmailsMessagesEnable);

        Log.d("YSY", "Preview Emails Messages Check : " + isPreviewEmailsMessagesEnable);

        // this is the code that works if the switch is off.
        if (Utils.is_G_model_Device(Build.DEVICE)) {
            mMessagesCheck.setEnabled(true);
            mSelectApps.setEnabled(true);
            mAutoUnlockCheck.setEnabled(true);
            mClosingSoundCheck.setEnabled(true);
            mCircleLightingCheck.setEnabled(true);
            mPreviewEmailsMessagesCheck.setEnabled(true);
        } else {
            mMessagesCheck.setEnabled(isEnabled);
            mSelectApps.setEnabled(isEnabled);
            mAutoUnlockCheck.setEnabled(isEnabled);
            mClosingSoundCheck.setEnabled(isEnabled);
            mCircleLightingCheck.setEnabled(isEnabled);
            mPreviewEmailsMessagesCheck.setEnabled(isEnabled);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
        android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
        MDMSettingsAdapter.getInstance().addQuickWindowCasePolicyChangeIntentFilter(
                filterLGMDM);
        registerReceiver(mLGMDMReceiver, filterLGMDM);
        // LGMDM_END
        mIsFirst = true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        ComponentName mComp;
        Intent mIntent;

        if (preference == mSelectApps) {
            if (NEW_UI_VERSION) {
                mComp = new ComponentName("com.lge.smartcover",
                        "com.lge.smartcover.quickcoversettings.QuickCoverAppListActivity");
                mIntent = new Intent(Intent.ACTION_MAIN);
            } else {
                mComp = new ComponentName("com.lge.lockscreensettings",
                        "com.lge.lockscreensettings.QuickWindowWidgetListActivity");
                mIntent = new Intent(Intent.ACTION_MAIN);
            }

            mIntent.setComponent(mComp);
            startActivity(mIntent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // update the state of the switch of QuickWindow case.
        boolean isEnabled = (Settings.Global.getInt(mResolver,
                SettingsConstants.Global.QUICK_VIEW_ENABLE, 1) != 0);
        mSwitch.setChecked(isEnabled);

        // update the state of the check box
        if (Utils.is_G_model_Device(Build.DEVICE)) {
            mMessagesCheck.setEnabled(true);
            mSelectApps.setEnabled(true);
            mAutoUnlockCheck.setEnabled(true);
            mClosingSoundCheck.setEnabled(true);
            mCircleLightingCheck.setEnabled(true);
            mPreviewEmailsMessagesCheck.setEnabled(true);
        } else {
            mMessagesCheck.setEnabled(isEnabled);
            mSelectApps.setEnabled(isEnabled);
            mAutoUnlockCheck.setEnabled(isEnabled);
            mClosingSoundCheck.setEnabled(isEnabled);
            mCircleLightingCheck.setEnabled(isEnabled);
            mPreviewEmailsMessagesCheck.setEnabled(isEnabled);
        }

        // update QuickWindow case Observer
        getContentResolver().registerContentObserver(Settings.Global.
                getUriFor(SettingsConstants.Global.QUICK_VIEW_ENABLE), true, mQuickWindowObserver);
        mSearch_result = getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue = getIntent().getBooleanExtra("newValue", false);
            Log.d("skku", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void startResult(boolean newValue) {
        if (mSearch_result.equals(KEY_SELECT_APPS_SETTING)) {
            finish();
        } else if (mSearch_result.equals(KEY_MESSAGES_SETTING)) {
            mMessagesCheck.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_PREVIEW_EMAILS_MESSAGES)) {
            mPreviewEmailsMessagesCheck.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_AUTO_UNLOCK_SETTING)) {
            mAutoUnlockCheck.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_CLOSING_SOUND_SETTING)) {
            mClosingSoundCheck.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_CIRCLE_WINDOW_LIGHTING)) {
            mCircleLightingCheck.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_HELP_SETTING)) {
            finish();
        }
    }
    
    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d("skku", "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d("skku", "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
                "com.android.settings.lge.QuickWindowCase" });
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        // Unregist QuickWindow case Observer
        getContentResolver().unregisterContentObserver(mQuickWindowObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        View view = getActionBar().getCustomView();
        if (view != null) {
            view.destroyDrawingCache();
        }
        getActionBar().setCustomView(null);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
        try {
            unregisterReceiver(mLGMDMReceiver);
        } catch (Exception e) {
            android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
        }
        // LGMDM_END
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.search) {
            switchTosearchResults();
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchTosearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    public void onCheckedChanged(CompoundButton button, boolean status) {
        // TODO Auto-generated method stub
        // It is a part of the operation of the switch occurs.
        if (mSwitch != null) {
            mSwitch.setChecked(status);
        }
        Settings.Global.putInt(mResolver,
                SettingsConstants.Global.QUICK_VIEW_ENABLE, status ? 1 : 0);
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        // TODO Auto-generated method stub
        final String key = arg0.getKey();

        int value = arg1.toString().equals("true") ? 1 : 0;
        // It is a part of the operation of View call logs and messages
        if (key.equals(KEY_MESSAGES_SETTING)) {
            Settings.Global.putInt(mResolver, SettingsConstants.Global.WINDOW_MESSAGES_ENABLE,
                    value);
            mMessagesCheck.setChecked(value != 0);
            searchDBUpdate(value != 0, KEY_MESSAGES_SETTING, "check_value");
        }

        // It is a part of the operation of Auto-unlock screen
        if (key.equals(KEY_AUTO_UNLOCK_SETTING)) {
            Settings.Global.putInt(mResolver, SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE,
                    value);
            mAutoUnlockCheck.setChecked(value != 0);
            searchDBUpdate(value != 0, KEY_AUTO_UNLOCK_SETTING, "check_value");
        }

        // It is a part of the operation of Cover closing sound
        if (key.equals(KEY_CLOSING_SOUND_SETTING)) {
            Settings.Global.putInt(mResolver, SettingsConstants.Global.COVER_CLOSING_SOUND, value);
            mClosingSoundCheck.setChecked(value != 0);
            searchDBUpdate(value != 0, KEY_CLOSING_SOUND_SETTING, "check_value");
        }

        // It is a part of the operation of Circle window lighting
        if (key.equals(KEY_CIRCLE_WINDOW_LIGHTING)) {
            Settings.Global.putInt(mResolver, SettingsConstants.Global.QUICK_CIRCLE_LIGHT, value);
            mCircleLightingCheck.setChecked(value != 0);
            searchDBUpdate(value != 0, KEY_CIRCLE_WINDOW_LIGHTING, "check_value");
        }

        // It is a part of the operation of Circle window lighting
        if (key.equals(KEY_PREVIEW_EMAILS_MESSAGES)) {
            Settings.Global.putInt(mResolver,
                    "preview_emails_messages", value);
            mPreviewEmailsMessagesCheck.setChecked(value != 0);
            searchDBUpdate(value != 0, KEY_PREVIEW_EMAILS_MESSAGES, "check_value");
        }

        return false;
    }
    
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        
        private Map<String, Integer> mDisplayCheck = new HashMap<String, Integer>();
        int mCheckValue = 0;
        String mTitle = "";
        String mClassName = "";
        int mCoverType = 1;
        
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            
            setRemoveVisible(context);
            
            mTitle = setMainTitle(context);
            
            if (mTitle.equals(context.getString(R.string.quickcover_title))) {
                mClassName = "com.android.settings.Settings$QuickCoverActivity";
                mCoverType = 0;
            } else {
                mClassName = "com.android.settings.lge.QuickWindowCase";
                mCoverType = 1;
            }
            
            setSearchIndexData(context, "quick_window_case_settings",
                    mTitle, "main",
                    null, null,
                    "android.intent.action.MAIN",
                    mClassName, "com.android.settings", 1,
                    null, null, null, 1, 0);
            
            if (mCoverType != 0) {
                if (mDisplayCheck.get(KEY_SELECT_APPS_SETTING) == null) {
                    setSearchIndexData(context, KEY_SELECT_APPS_SETTING,
                            context.getString(R.string.quick_window_case_select_apps_title), 
                            context.getString(R.string.quick_circle_case_title),
                            null, null, 
                            "android.intent.action.MAIN",
                            "com.lge.smartcover.quickcoversettings.QuickCoverAppListActivity", "com.lge.smartcover", 1,
                            null, null, null, 1, 0);
                }
                
                setCheckBoxType(context);
                
                setSearchIndexData(context, KEY_HELP_SETTING,
                        context.getString(R.string.tethering_help_button_text), 
                        context.getString(R.string.quick_circle_case_title),
                        null, 
                        null, "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCaseHelp", "com.android.settings", 1,
                        null, null, null, 1, 0);
            }
            
            return mResult;
        }

        private String setMainTitle(Context context) {
            String title = "";
            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_smart_cover,
                    "com.lge.R.bool.config_smart_cover") == true) {
                title = context.getString(R.string.quickcover_title);
            } else if (Config.getFWConfigBool(context,
                            com.lge.R.bool.config_using_window_cover,
                            "com.lge.R.bool.config_using_window_cover") == true) {
                title = context.getString(R.string.display_quick_cover_window_title);
            } else if (Config.getFWConfigBool(context,
                            com.lge.R.bool.config_using_circle_cover,
                            "com.lge.R.bool.config_using_circle_cover") == true) {
                title = context.getString(R.string.quick_circle_case_title);
            } else if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_using_disney_cover,
                    "com.lge.R.bool.config_using_disney_cover") == true) {
                title = context.getString(R.string.disney_case_title_ex);
            }
            return title;
        }

        private void setCheckBoxType(Context context) {
            if (mDisplayCheck.get(KEY_MESSAGES_SETTING) == null) {
                mCheckValue = Settings.Global.getInt(context.getContentResolver(),
                        SettingsConstants.Global.WINDOW_MESSAGES_ENABLE, 0);
                setSearchIndexData(context, KEY_MESSAGES_SETTING,
                        context.getString(R.string.quick_window_case_messages_title_ex1),
                        context.getString(R.string.quick_circle_case_title),
                        null, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCase", "com.android.settings", 1,
                        "CheckBox", null, null, 1, mCheckValue);
            }
            
            if (mDisplayCheck.get(KEY_PREVIEW_EMAILS_MESSAGES) == null) {
                mCheckValue = Settings.Global.getInt(context.getContentResolver(),
                        "preview_emails_messages", 0);
                setSearchIndexData(context, KEY_PREVIEW_EMAILS_MESSAGES,
                        context.getString(R.string.disney_case_preview_emails_messages),
                        context.getString(R.string.quick_circle_case_title),
                        null, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCase", "com.android.settings", 1,
                        "CheckBox", null, null, 1, mCheckValue);
            }
            
            if (mDisplayCheck.get(KEY_AUTO_UNLOCK_SETTING) == null) {
                mCheckValue = Settings.Global.getInt(context.getContentResolver(),
                        SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
                setSearchIndexData(context, KEY_AUTO_UNLOCK_SETTING,
                        context.getString(R.string.quick_window_case_auto_unlock_title_ex),
                        context.getString(R.string.quick_circle_case_title),
                        null, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCase", "com.android.settings", 1,
                        "CheckBox", null, null, 1, mCheckValue);
            }
            
            if (mDisplayCheck.get(KEY_CLOSING_SOUND_SETTING) == null) {
                mCheckValue = Settings.Global.getInt(context.getContentResolver(),
                        SettingsConstants.Global.COVER_CLOSING_SOUND, 0);
                setSearchIndexData(context, KEY_CLOSING_SOUND_SETTING,
                        context.getString(R.string.quick_circle_closing_sound_title),
                        context.getString(R.string.quick_circle_case_title),
                        null, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCase", "com.android.settings", 1,
                        "CheckBox", null, null, 1, mCheckValue);
            }
            
            if (mDisplayCheck.get(KEY_CIRCLE_WINDOW_LIGHTING) == null) {
                mCheckValue = Settings.Global.getInt(context.getContentResolver(),
                        SettingsConstants.Global.QUICK_CIRCLE_LIGHT, 0);
                setSearchIndexData(context, KEY_CIRCLE_WINDOW_LIGHTING,
                        context.getString(R.string.quick_circle_lighting_title_ex),
                        context.getString(R.string.quick_circle_case_title),
                        null, null,
                        "android.intent.action.MAIN",
                        "com.android.settings.lge.QuickWindowCase", "com.android.settings", 1,
                        "CheckBox", null, null, 1, mCheckValue);
            }
        }

        private void setRemoveVisible(Context context) {
            // TODO Auto-generated method stub
            if (!Utils.checkPackage(context, "com.lge.widget.quickcovercontacts")) {
                mDisplayCheck.put(KEY_MESSAGES_SETTING, 0);
            }

            if (NEW_UI_VERSION) {
                mDisplayCheck.put(KEY_AUTO_UNLOCK_SETTING, 0);
            }

            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_using_circle_cover,
                    "com.lge.R.bool.config_using_circle_cover") == false
                    && Config.getFWConfigBool(context,
                            com.lge.R.bool.config_using_disney_cover,
                            "com.lge.R.bool.config_using_disney_cover") == false) {
                mDisplayCheck.put(KEY_AUTO_UNLOCK_SETTING, 0);
            }
            
            mDisplayCheck.put(KEY_CLOSING_SOUND_SETTING, 0);

            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_using_circle_cover,
                    "com.lge.R.bool.config_using_circle_cover") == false) {
                mDisplayCheck.put(KEY_CLOSING_SOUND_SETTING, 0);
            }
            mDisplayCheck.put(KEY_CIRCLE_WINDOW_LIGHTING, 0);

            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_using_disney_cover,
                    "com.lge.R.bool.config_using_disney_cover") == false) {
                mDisplayCheck.put(KEY_PREVIEW_EMAILS_MESSAGES, 0);
            }
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
