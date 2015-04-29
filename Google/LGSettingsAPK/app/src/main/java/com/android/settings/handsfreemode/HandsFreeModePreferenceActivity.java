/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.handsfreemode;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View.OnClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.R;
import com.android.settings.Utils;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import android.telephony.TelephonyManager;
import android.text.Html;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.Field;

//LGE_CHANGE [jonghen.han@lge.com] 2012-05-25, KeepScreenOn
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.graphics.drawable.Drawable;
import com.android.settings.Utils;

public class HandsFreeModePreferenceActivity extends PreferenceActivity
        implements Indexable {

    private static final String KEY_TTS_LANGUAGE = "hands_free_mode_tts_settings";
    private static final String KEY_READ_OUT_CATEGORY = "hands_free_mode_settings_category";
    private static final String KEY_CALL = "hands_free_mode_call";
    private static final String KEY_MESSAGE = "hands_free_mode_message";
    private static final String KEY_READ_MESSAGE = "hands_free_mode_readmessage";
    private static final String SETTING_STYLE = "settings_style";
    private Preference mTTSLanguage;
    private PreferenceCategory mReadOutCategory;
    private CheckBoxPreference mCall;
    private CheckBoxPreference mMessage;
    private HandsFreeModeSubPreference mReadMessage;

    private HandsFreeModeInfo mHandsFreeModeInfo;
    private Switch mHandsFreeModeSwitch;
    private Context mContext;
    // Search
    private boolean mNewValue;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        do_Init();
        do_InitPreferenceMenu();
        this.getListView().setFooterDividersEnabled(false);
        mIsFirst = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        do_updateUI();
        setSearchPerformClick();

    }
    private void do_Init() {
        mContext = this.getApplicationContext();
        mHandsFreeModeInfo = new HandsFreeModeInfo(mContext);
        addPreferencesFromResource(R.xml.hands_free_mode_main_activity);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.shortcut_hands_free_mode);
            if (Utils.isUI_4_1_model(getApplicationContext())) {
                if (false == "KDDI".equals(Config.getOperator())) {
                    getActionBar().setTitle(R.string.voice_notifications_title_changed);
                    this.setTitle(R.string.voice_notifications_title_changed);    
                }
                if ("DCM".equals(Config.getOperator())) {
                    getActionBar().setTitle(R.string.voice_notifications_title);
                    this.setTitle(R.string.voice_notifications_title);
                }
            }
        }

        mHandsFreeModeSwitch = new Switch(this);

        // TODO Where to put the switch in tablet multipane layout?
        final int padding = getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
        mHandsFreeModeSwitch.setPaddingRelative(0, 0, padding, 0);

        mHandsFreeModeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                do_setMenuStatus(isChecked);
				mHandsFreeModeInfo
                            .setDBHandsFreeModeState(mHandsFreeModeSwitch.isChecked() == true ? 1
                                    : 0);
                    if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
                        mHandsFreeModeInfo.setDBHandsFreeModeCall(mHandsFreeModeInfo
                                .getDBHandsFreeModeState());
                    }
            }
        });
        mHandsFreeModeSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void do_setMenuStatus(boolean value) {
        mCall.setEnabled(value);
        if (mMessage != null && mReadMessage != null) {
            mMessage.setEnabled(value);
            mReadMessage.setEnabled(value);
        }
    }

    private void do_InitPreferenceMenu() {
        mReadOutCategory = (PreferenceCategory)findPreference(KEY_READ_OUT_CATEGORY);
        mTTSLanguage = (Preference)findPreference(KEY_TTS_LANGUAGE);
        mCall = (CheckBoxPreference)findPreference(KEY_CALL);
        mMessage = (CheckBoxPreference)findPreference(KEY_MESSAGE);
        mReadMessage = (HandsFreeModeSubPreference)findPreference(KEY_READ_MESSAGE);
        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(findPreference(KEY_READ_OUT_CATEGORY));
            mReadOutCategory.removePreference(mCall);
            mReadOutCategory.removePreference(mMessage);
            mReadOutCategory.removePreference(mReadMessage);
        }
        if ("JP".equals(Config.getCountry())
                && "DCM".equals(Config.getOperator())) {
            mReadOutCategory
                    .setTitle(R.string.hands_free_mode_read_out_title);
            mReadOutCategory.removePreference(mCall);

        }
        if (Utils.isUI_4_1_model(getApplicationContext()) ) {
            if (false == "KDDI".equals(Config.getOperator())
                    ) {
                mTTSLanguage.setTitle(R.string.voice_notifications_language_changed);
            }
            if ("DCM".equals(Config.getOperator())) {
                mTTSLanguage.setTitle(R.string.voice_notifications_language);
            }
        }
    }

    private void do_updateUI() {
        mHandsFreeModeSwitch.setChecked(mHandsFreeModeInfo.getDBHandsFreeModeState() == 1 ? true
                : false);
        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            mHandsFreeModeInfo.setDBHandsFreeModeCall(mHandsFreeModeInfo.getDBHandsFreeModeState());
        }
        mCall.setChecked(mHandsFreeModeInfo.getDBHandsFreeModeCall() == 1 ? true : false);
        if (mMessage != null && mReadMessage != null) {
            mMessage.setChecked(mHandsFreeModeInfo.getDBHandsFreeModeMessage() == 1 ? true : false);
            do_setMenuStatus(mHandsFreeModeSwitch.isChecked());
            if (mMessage.isChecked()) {
                mReadMessage
                        .setChecked(mHandsFreeModeInfo.getDBHandsFreeModeReadMessage() == 1 ? true
                                : false);
            } else {
                mReadMessage.setChecked(false);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(mHandsFreeModeSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.equals(mTTSLanguage)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setAction("com.android.settings.TTS_SETTINGS");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getBaseContext(), R.string.sp_quiet_mode_not_install_NORMAL,
                        Toast.LENGTH_SHORT).show();
            }

        }
        else if (preference.equals(mCall)) {
            mHandsFreeModeInfo.setDBHandsFreeModeCall(mCall.isChecked() == true ? 1 : 0);
        }
        else if (preference.equals(mMessage)) {
            mHandsFreeModeInfo.setDBHandsFreeModeMessage(mMessage.isChecked() == true ? 1 : 0);
            if (mMessage.isChecked()) {
            } else {
                mReadMessage.setChecked(false);
            }
        }
        else if (preference.equals(mReadMessage)) {
            mHandsFreeModeInfo.setDBHandsFreeModeReadMessage(mReadMessage.isChecked() == true ? 1
                    : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (android.provider.Settings.Global.getInt(
                    mContext.getContentResolver(),
                    android.provider.Settings.Global.DEVICE_PROVISIONED, 0) == 0) {
                onBackPressed();
            } else {
                String mSetActionName;
                int settingStyle = Settings.System.getInt(getContentResolver(),
                        SETTING_STYLE, 0);
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);

                String baseActivityClassName = info.get(0).baseActivity
                        .getClassName();
                Log.d("starmotor", "settingStyle : " + settingStyle);
                Log.d("starmotor",
                        "Utils.supportEasySettings(this) : "
                                + Utils.supportEasySettings(this));
                Log.d("starmotor", "baseActivityClassName : "
                        + baseActivityClassName);
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    if (settingStyle == 1) {
                        mSetActionName = "com.android.settings.SOUND_MORE_SETTING";
                    } else {
                        mSetActionName = "com.android.settings.SOUND_SETTINGS";
                    }
                } else {
                    mSetActionName = "com.android.settings.SOUND_SETTINGS";
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.setAction(mSetActionName);
                startActivity(i);
                finish();
                return true;
            }

        }
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            if (item.getItemId() == R.id.search) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    private void setSearchPerformClick() {
        mSearch_result = getIntent().getStringExtra("search_item");
        mNewValue = getIntent().getBooleanExtra("newValue", false);

        Log.d("jw", "mSearch_result : " + mSearch_result);
        Log.d("jw", "mNewValue : " + mNewValue);

        boolean checkPerfrom = getIntent().getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult(mNewValue);
            mIsFirst = false;
        }
    }

    private void startResult(boolean value) {
        if (mSearch_result.equals(KEY_CALL)) {
            mCall.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_MESSAGE)) {
            mMessage.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_READ_MESSAGE)) {
            mReadMessage.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_TTS_LANGUAGE)) {
            mTTSLanguage.performClick(getPreferenceScreen());
        } else {
            mHandsFreeModeInfo.setDBHandsFreeModeState(value ? 1 : 0);
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            if (!Utils.supportSplitView(context)) {
                setHandsFreeSearch(context);
            }

            return mResult;
        }

        private void setHandsFreeSearch(Context context) {
            // Category Title
            String mTitle;
            if (Utils.istargetOperator("KDDI")) {
                mTitle = context.getString(R.string.voice_notifications_title);
            } else if (Utils.istargetOperator("DCM")) {
                mTitle = context.getString(R.string.voice_notifications_title);
            } else {
                mTitle = context
                        .getString(R.string.voice_notifications_title_changed);
            }
            String mHelpCategory = context
                    .getString(R.string.notification_settings) + " > " + mTitle;

            if (!"KDDI".equals(Config.getOperator())) {
                if (!"DCM".equals(Config.getOperator())) {
                    // Calls
                    setSearchIndexData(
                            context,
                            KEY_CALL,
                            context.getString(R.string.hands_free_mode_read_out_call_title),
                            mHelpCategory,
                            context.getString(R.string.hands_free_mode_read_out_call_summary_new),
                            null,
                            null,
                            "com.android.settings.handsfreemode.HandsFreeModePreferenceActivity",
                            "com.android.settings", 1, "CheckBox", "System",
                            "hands_free_mode_call", 1, 0);
                }
                // Messages
                setSearchIndexData(
                        context,
                        KEY_MESSAGE,
                        context.getString(R.string.hands_free_mode_read_out_message_title),
                        mHelpCategory,
                        context.getString(R.string.hands_free_mode_read_out_message_summary_new),
                        null,
                        null,
                        "com.android.settings.handsfreemode.HandsFreeModePreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        "hands_free_mode_message", 1, 0);

                // KEY_READ_MESSAGE
                setSearchIndexData(
                        context,
                        KEY_READ_MESSAGE,
                        context.getString(R.string.hands_free_mode_read_out_readmessage_title),
                        mHelpCategory,
                        context.getString(R.string.hands_free_mode_read_out_readmessage_summary),
                        null,
                        null,
                        "com.android.settings.handsfreemode.HandsFreeModePreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        "hands_free_mode_read_message", 1, 0);
            }

            // KEY_TTS_LANGUAGE
            String mTTS_Title;
            if ("KDDI".equals(Config.getOperator())
                    || "DCM".equals(Config.getOperator())) {
                mTTS_Title = context
                        .getString(R.string.voice_notifications_language);
            } else {
                mTTS_Title = context
                        .getString(R.string.voice_notifications_language_changed);
            }
            setSearchIndexData(
                    context,
                    KEY_TTS_LANGUAGE,
                    mTTS_Title,
                    mHelpCategory,
                    null,
                    null,
                    null,
                    "com.android.settings.handsfreemode.HandsFreeModePreferenceActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
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
