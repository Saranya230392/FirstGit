/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.notification;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.INotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TimePicker;
import android.telephony.TelephonyManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.notification.ZenModeUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.android.settings.lgesetting.Config.Config;


public class ZenModeSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "ZenModeSettings";
    private static final boolean DEBUG = true;

    private static final String KEY_ZEN_MODE = "zen_mode_setting";
    private static final String KEY_IMPORTANT = "important";
    private static final String KEY_ALARM_INFO = "alarm_info";
    private static final String KEY_EVENTS = "events";
    private static final String KEY_CALLS = "phone_calls";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_STARRED = "starred";
    private static final String KEY_DOWNTIME = "downtime";
    private static final String KEY_DAYS = "zen_mode_day";
    private static final String KEY_AUTOMATION = "automation";
    private static final String KEY_ENTRY = "entry";
    private static final String KEY_CONDITION_PROVIDERS = "manage_condition_providers";


    private static final int GLOBAL_ZEN_MODE_OFF = 0;
    private static final int GLOBAL_ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;
    private static final int GLOBAL_ZEN_MODE_NO_INTERRUPTIONS = 2;

    // [START][2015-02-16][seungyeop.yeom] Create value for Search function
    private boolean mIsFirst = false;
    private String mSearch_result;
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";
    // [END][2015-02-16][seungyeop.yeom] Create value for Search function

    private static final SettingPrefWithCallback PREF_ZEN_MODE = new SettingPrefWithCallback(
            SettingPref.TYPE_GLOBAL, KEY_ZEN_MODE, Global.ZEN_MODE, Global.ZEN_MODE_OFF,
            Global.ZEN_MODE_OFF, Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS,
            Global.ZEN_MODE_NO_INTERRUPTIONS) {
        protected String getCaption(Resources res, int value) {
            switch (value) {
                case Global.ZEN_MODE_NO_INTERRUPTIONS:
                    return res.getString(R.string.zen_mode_option_no_interruptions);
                case Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS:
                    return res.getString(R.string.zen_mode_option_important_interruptions);
                default:
                    return res.getString(R.string.zen_mode_option_off);
            }
        }
    };

    private static SparseArray<String> allKeyTitles(Context context) {
        final SparseArray<String> rt = new SparseArray<String>();
        rt.put(R.string.zen_mode_important_category, KEY_IMPORTANT);
        rt.put(R.string.zen_mode_events, KEY_EVENTS);
        if (Utils.isVoiceCapable(context)) {
            rt.put(R.string.zen_mode_phone_calls, KEY_CALLS);
            rt.put(R.string.zen_mode_option_title, KEY_ZEN_MODE);
        } else {
            rt.put(R.string.zen_mode_option_title_novoice, KEY_ZEN_MODE);
        }
        rt.put(R.string.zen_mode_messages, KEY_MESSAGES);
        rt.put(R.string.zen_mode_from_starred, KEY_STARRED);
        rt.put(R.string.zen_mode_alarm_info, KEY_ALARM_INFO);
        rt.put(R.string.zen_mode_downtime_category, KEY_DOWNTIME);
        rt.put(R.string.zen_mode_downtime_days, KEY_DAYS);
        rt.put(R.string.zen_mode_automation_category, KEY_AUTOMATION);
        rt.put(R.string.manage_condition_providers, KEY_CONDITION_PROVIDERS);
        return rt;
    }

    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private Context mContext;
    private PackageManager mPM;
    private ZenModeConfig mConfig;
    private boolean mDisableListeners;
    private boolean mIsNeedUpdateSettingsValue;
    private CheckBoxPreference mEvents;
    private CheckBoxPreference mCalls;
    private CheckBoxPreference mMessages;
    private ZenModeDayPreference mDays;
    private Preference mStarred;
    private PreferenceCategory mAutomationCategory;
    private Preference mEntry;
    private Preference mConditionProviders;
    private Preference mZenModeSetting;
    private ZenModeConditionSelection mZenModeConditionSelection;
    ArrayList<String[]> mZenMode_entry;
    ArrayList<String[]> mZenMode_value;
    ArrayList<String[]> mZenMode_starred_entry;
    ArrayList<String[]> mZenMode_starred_value;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mPM = mContext.getPackageManager();

        addPreferencesFromResource(R.xml.zen_mode_settings);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        final PreferenceScreen root = getPreferenceScreen();

        // [2015-02-16][seungyeop.yeom] support perform click for Search
        mIsFirst = true;

        mConfig = ZenModeUtils.getZenModeConfig();
        Log.d(TAG, "Loaded mConfig=" + mConfig);

        mZenModeSetting = (Preference)findPreference(KEY_ZEN_MODE);
        if (!Utils.isVoiceCapable(mContext)) {
           mZenModeSetting.setTitle(R.string.zen_mode_option_title_novoice);
        }



        final PreferenceCategory important =
                (PreferenceCategory)root.findPreference(KEY_IMPORTANT);


        mEvents = (CheckBoxPreference)important.findPreference(KEY_EVENTS);
        mEvents.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                      if (mDisableListeners) {
                          return true;
                      }
                      final boolean val = (Boolean)newValue;
                      if (val == mConfig.allowEvents) {
                         return true;
                      }
                      Log.d(TAG, "onPrefChange allowEvents=" + val);
                      final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();
                      newConfig.allowEvents = val;
                      
                      // [seungyeop.yeom] update DB for Search function
                      searchDBUpdate(newConfig.allowEvents, KEY_EVENTS, "check_value");
                      return setZenModeConfigMain(newConfig);
                }
        });

        mCalls = (CheckBoxPreference)important.findPreference(KEY_CALLS);
        if (Utils.isVoiceCapable(mContext)) {
            mCalls.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (mDisableListeners) {
                        return true;
                    }
                    final boolean val = (Boolean)newValue;
                    if (val == mConfig.allowCalls) {
                        return true;
                    }
                    Log.d(TAG, "onPrefChange allowCalls=" + val);
                    final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();
                    newConfig.allowCalls = val;

                    // [seungyeop.yeom] update DB for Search function
                    searchDBUpdate(newConfig.allowCalls, KEY_CALLS, "check_value");
                    searchDBUpdate(newConfig.allowCalls || newConfig.allowMessages, KEY_STARRED, "current_enable");
                    return setZenModeConfigMain(newConfig);
                }
            });
        } else {
            important.removePreference(mCalls);
            mCalls = null;
        }

        mMessages = (CheckBoxPreference)important.findPreference(KEY_MESSAGES);
        if (isMessageCapable(mContext)) {
            mMessages.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (mDisableListeners) {
                        return true;
                    }
                    final boolean val = (Boolean)newValue;
                    if (val == mConfig.allowMessages) {
                        return true;
                    }
                    Log.d(TAG, "onPrefChange allowMessages=" + val);
                    final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();
                    newConfig.allowMessages = val;

                    // [seungyeop.yeom] update DB for Search function
                    searchDBUpdate(newConfig.allowMessages, KEY_MESSAGES, "check_value");
                    searchDBUpdate(newConfig.allowCalls || newConfig.allowMessages, KEY_STARRED, "current_enable");
                    return setZenModeConfigMain(newConfig);
                }
            });
        } else {
           important.removePreference(mMessages);
           mMessages = null;
        }
        mStarred = important.findPreference(KEY_STARRED);
        if (mCalls == null && mMessages == null) {
          important.removePreference(mStarred);
          mStarred = null;
        }

        final PreferenceCategory downtime = (PreferenceCategory)root.findPreference(KEY_DOWNTIME);
        mDays = (ZenModeDayPreference)downtime.findPreference(KEY_DAYS);

        mAutomationCategory = (PreferenceCategory)findPreference(KEY_AUTOMATION);
        mEntry = findPreference(KEY_ENTRY);
        mEntry.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(mContext)
                    .setTitle(R.string.zen_mode_entry_conditions_title)
                    .setView(new ZenModeAutomaticConditionSelection(mContext))
                    .setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            refreshAutomationSection();
                        }
                    })
                    .setPositiveButton(R.string.dlg_ok, null)
                    .show();
                return true;
            }
        });
        mConditionProviders = findPreference(KEY_CONDITION_PROVIDERS);

        updateControls();
        setZenModeAlertDialogValue();
    }


    private void updateControls() {
        mDisableListeners = true;
        if (mCalls != null) {
            mCalls.setChecked(mConfig.allowCalls);
        }
        mEvents.setChecked(mConfig.allowEvents);
        if (mMessages != null) {
            mMessages.setChecked(mConfig.allowMessages);
        }
        if (mStarred != null) {
            updateZenModeStarredSummary(mConfig.allowFrom);
            updateStarredEnabled();
        }

        mDisableListeners = false;
        refreshAutomationSection();
    }

    private void updateStarredEnabled() {
        mStarred.setEnabled(mConfig.allowCalls || mConfig.allowMessages);
    }

    private void refreshAutomationSection() {
        if (mConditionProviders != null) {
            final int total = ConditionProviderSettings.getProviderCount(mPM);
			Log.d(TAG , "total : " + total);
            if (total == 0) {
                getPreferenceScreen().removePreference(mAutomationCategory);
            } else {
                final int n = ConditionProviderSettings.getEnabledProviderCount(mContext);
                if (n == 0) {
                    mConditionProviders.setSummary(getResources().getString(
                            R.string.manage_condition_providers_summary_zero));
                } else {
                    mConditionProviders.setSummary(String.format(getResources().getQuantityString(
                            R.plurals.manage_condition_providers_summary_nonzero,
                            n, n)));
                }
                final String entrySummary = getEntryConditionSummary();
                if (n == 0 || entrySummary == null) {
                    mEntry.setSummary(R.string.zen_mode_entry_conditions_summary_none);
                } else {
                    mEntry.setSummary(entrySummary);
                }
            }
        }
    }

    private String getEntryConditionSummary() {
        final INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        try {
            final Condition[] automatic = nm.getAutomaticZenModeConditions();
            if (automatic == null || automatic.length == 0) {
                return null;
            }
            final String divider = getString(R.string.summary_divider_text);
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < automatic.length; i++) {
                if (i > 0) {
                    sb.append(divider);
                }
                sb.append(automatic[i].summary);
            }
            return sb.toString();
        } catch (Exception e) {
            Log.w(TAG, "Error calling getAutomaticZenModeConditions", e);
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG , "onResume");
        updateZenModeSummary(getZenModeSetting());
        updateZenModeConfig();
        mSettingsObserver.register();

        // [START][2015-02-16][seungyeop.yeom] support perform click for Search function
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra(
                "perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
        // [END][2015-02-16][seungyeop.yeom] support perform click for Search function
    }

    /*
     * Author : seungyeop.yeom Type : startResult() method Date : 2015-02-16
     * Brief : perform click for search function
     */
    private void startResult() {
        if (mSearch_result.equals(KEY_ZEN_MODE)) {
            mZenModeSetting.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_EVENTS)) {
            mEvents.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_CALLS)) {
            mCalls.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_MESSAGES)) {
            mMessages.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_STARRED)) {
            mStarred.performClick(getPreferenceScreen());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG , "onPause");
        mSettingsObserver.unregister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        zenModeConditionSelectionRelease();
        Log.d(TAG , "onDestroy");
    }

    private void updateZenModeConfig() {
        final ZenModeConfig config = ZenModeUtils.getZenModeConfig();
        if (Objects.equals(config, mConfig)) {
            return;
        }
        mConfig = config;
        if (DEBUG) {
            Log.d(TAG, "updateZenModeConfig mConfig=" + mConfig);
        }
        updateControls();
        if (mDays != null && mDays.getToggleGroup() != null) {
            mDays.onResume(config);
        }
        ZenModeUtils.updateZenModeDayConfig(mContext, mConfig);
    }

    private boolean setZenModeConfigMain(ZenModeConfig config) {
       if (ZenModeUtils.setZenModeConfig(config)) {
           mConfig = config;
           if (mStarred != null) {
              updateStarredEnabled();
           }
          return true;
        } else {
          return false;
        }
    }

    protected void putZenModeSetting(int value) {
        Global.putInt(getContentResolver(), Global.ZEN_MODE, value);
    }

    private int getZenModeSetting() {
        return Global.getInt(getContentResolver(), Global.ZEN_MODE, 0);
    }

    protected void showConditionSelection(int newSettingsValue, int oldSettingsValue) {
        ArrayList<String> items = mZenModeConditionSelection.getZenModeConditionEntry();
        CharSequence[] conditionEntry = items.toArray(new CharSequence[items.size()]);

        int titlevalue = R.string.zen_mode_option_important_interruptions;
        if (newSettingsValue == GLOBAL_ZEN_MODE_NO_INTERRUPTIONS) {
            titlevalue = R.string.zen_mode_option_no_interruptions;
        }
        final int resetSettingsValue = oldSettingsValue;
        new AlertDialog.Builder(mContext)
                 .setTitle(titlevalue)
                 .setSingleChoiceItems(conditionEntry, 0,
                         new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface _dialog, int which) {
                               Log.d(TAG , "OnClickListener = onClick - which : " + which);
                               mZenModeConditionSelection.setZenModeConditionValue(which);
                               mZenModeConditionSelection.confirmCondition();
                               mIsNeedUpdateSettingsValue = false;
                               _dialog.cancel();
                           }
                   })
                   .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
                       @Override
                        public void onClick(DialogInterface dialog, int which) {
                           cancelDialog(resetSettingsValue);
                           zenModeConditionSelectionRelease();
                       }
                   })
                  .setOnCancelListener(new DialogInterface.OnCancelListener() {
                       @Override
                       public void onCancel(DialogInterface dialog) {
                           if (mIsNeedUpdateSettingsValue) {
                               cancelDialog(resetSettingsValue);
                           }
                           zenModeConditionSelectionRelease();
                       }
                   }).show();
    }

    protected void cancelDialog(int oldSettingsValue) {
        // If not making a decision, reset drop down to current setting.
        PREF_ZEN_MODE.setValueWithoutCallback(mContext, oldSettingsValue);
    }

    private static class SettingPrefWithCallback extends SettingPref {

        private Callback mCallback;
        private int mValue;

        public SettingPrefWithCallback(int type, String key, String setting, int def,
                int... values) {
            super(type, key, setting, def, values);
        }

        public void setCallback(Callback callback) {
            mCallback = callback;
        }

        @Override
        public void update(Context context) {
            // Avoid callbacks from non-user changes.
            mValue = getValue(context);
            super.update(context);
        }

        @Override
        protected boolean setSetting(Context context, int value) {
            if (value == mValue) {
               return true;
            }
            mValue = value;
            if (mCallback != null) {
                mCallback.onSettingSelected(value);
            }
            return super.setSetting(context, value);
        }

        @Override
        public Preference init(SettingsPreferenceFragment settings) {
            Preference ret = super.init(settings);
            mValue = getValue(settings.getActivity());
            return ret;
        }

        public boolean setValueWithoutCallback(Context context, int value) {
            // Set the current value ahead of time, this way we won't trigger a callback.
            mValue = value;
            return putInt(mType, context.getContentResolver(), mSetting, value);
        }

        public int getValue(Context context) {
            return getInt(mType, context.getContentResolver(), mSetting, mDefault);
        }

        public interface Callback {
            void onSettingSelected(int value);
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE_URI = Global.getUriFor(Global.ZEN_MODE);
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor(Global.ZEN_MODE_CONFIG_ETAG);

        public SettingsObserver() {
            super(mHandler);
        }

        public void register() {
            getContentResolver().registerContentObserver(ZEN_MODE_URI, false, this);
            getContentResolver().registerContentObserver(ZEN_MODE_CONFIG_ETAG_URI, false, this);
        }

        public void unregister() {
            getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (ZEN_MODE_URI.equals(uri)) {
               Log.d(TAG , "onChange - ZEN_MODE_URI ");
               updateZenModeSummary(getZenModeSetting());
            }
            if (ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {	
                Log.d(TAG , "onChange - ZEN_MODE_CONFIG_ETAG_URI ");
                updateZenModeConfig();
            }
        }
    }

    private void setZenModeAlertDialogValue() {
        mZenMode_entry = new ArrayList<String[]>();
        mZenMode_value = new ArrayList<String[]>();
        mZenMode_entry.add(0, getResources().getStringArray(R.array.zen_mode_setting_entry));
        mZenMode_value.add(0, getResources().getStringArray(R.array.zen_mode_setting_values));

        mZenMode_starred_entry = new ArrayList<String[]>();
        mZenMode_starred_value = new ArrayList<String[]>();
        mZenMode_starred_entry.add(0, getResources().getStringArray(R.array.zen_mode_starred_entry));
        mZenMode_starred_value.add(0, getResources().getStringArray(R.array.zen_mode_starred_values));
    }

    private void updateZenModeStarredSummary(int value) {
        switch (value) {
          case ZenModeConfig.SOURCE_ANYONE:
               mStarred.setSummary(R.string.zen_mode_from_anyone);
               break;
          case ZenModeConfig.SOURCE_STAR:
               mStarred.setSummary(R.string.zen_mode_from_starred);
               break;
          case ZenModeConfig.SOURCE_CONTACT:
               mStarred.setSummary(R.string.zen_mode_from_contacts);
               break;
           default:
               mStarred.setSummary(R.string.zen_mode_from_anyone);
               break;
        }
    }

    private void updateZenModeSummary(int value) {
        switch (value) {
          case GLOBAL_ZEN_MODE_OFF:
               mZenModeSetting.setSummary(R.string.zen_mode_option_off);
               break;
          case GLOBAL_ZEN_MODE_IMPORTANT_INTERRUPTIONS:
               mZenModeSetting.setSummary(R.string.zen_mode_option_important_interruptions);
               break;
          case GLOBAL_ZEN_MODE_NO_INTERRUPTIONS:
               mZenModeSetting.setSummary(R.string.zen_mode_option_no_interruptions);
               break;
           default:
               mZenModeSetting.setSummary(R.string.zen_mode_option_off);
               break;
        }
    }

    private void showZenModeEntrySettingDialog() {
         mIsNeedUpdateSettingsValue = true;
         new AlertDialog.Builder(mContext)
                .setTitle(mZenModeSetting.getTitle())
                .setSingleChoiceItems(mZenMode_entry.get(0), getZenModeSetting(),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface _dialog, int which) {
                                Log.d(TAG , "getZenModeSetting() : " + getZenModeSetting() + "which : " + which);
                                final int oldSettingValue = getZenModeSetting();
                                if (oldSettingValue != which) {
                                    if (which != Global.ZEN_MODE_OFF) {
                                        putZenModeSetting(which);
                                        showConditionSelection(which, oldSettingValue);
                                        mIsNeedUpdateSettingsValue = false;
                                    } else {
                                        putZenModeSetting(which);
                                    }
                                }
                                _dialog.cancel();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                zenModeConditionSelectionRelease();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (mIsNeedUpdateSettingsValue) {
                                  zenModeConditionSelectionRelease();
                                } else {
                                  mIsNeedUpdateSettingsValue = true;                                 	
                                }
                            }
                        }).show();

        mZenModeConditionSelection = new ZenModeConditionSelection(mContext);
        mZenModeConditionSelection.onConditionAttachedToWindow();
    }

    private int getAllowFrowSelectValue(int value) {
       if (value == 1) {
        return ZenModeConfig.SOURCE_STAR;
       } else if (value == 2) {
        return ZenModeConfig.SOURCE_CONTACT;
       } else {
        return ZenModeConfig.SOURCE_ANYONE;
       }
    }

    private void showZenModeStarredSettingDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(mStarred.getTitle())
                .setSingleChoiceItems(mZenMode_starred_entry.get(0), getAllowFrowSelectValue(mConfig.allowFrom),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface _dialog, int which) {
                                if (mDisableListeners) {
                                   return;
                                }
                                if (which == 1) {
                                    which = ZenModeConfig.SOURCE_STAR;
                                } else if (which == 2) {
                                    which = ZenModeConfig.SOURCE_CONTACT;
                                }
                                final int val = (Integer)which;
                                if (val != mConfig.allowFrom) {
                                    if (DEBUG) {
                                        Log.d(TAG, "onPrefChange allowFrom=" + ZenModeConfig.sourceToString(val));
                                    }
                                    final ZenModeConfig newConfig = ZenModeUtils.getZenModeConfig();
                                    newConfig.allowFrom = val;
                                    setZenModeConfigMain(newConfig);
                                    updateZenModeStarredSummary(val);
                            	}
                                _dialog.cancel();
                            }
                        }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.equals(mZenModeSetting)) {
          showZenModeEntrySettingDialog();
        } else if (mStarred != null && preference.equals(mStarred)) {
          showZenModeStarredSettingDialog();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void zenModeConditionSelectionRelease() {
        Log.d(TAG, "zenModeConditionSelectionRelease");
        if (mZenModeConditionSelection != null) {
            Log.d(TAG, "zenModeConditionSelectionRelease - mZenModeConditionSelection != null");
            mZenModeConditionSelection.onConditionDetachedFromWindow();
            mZenModeConditionSelection = null;
        }
    }

    private boolean isMessageCapable(Context context) {
          TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
          // return telephony != null && telephony.isSmsCapable();
          // [2014-01-16][seungyeop.yeom@lge.com] message menu unconditional support (Google CTS items)
          return true;
    }

    /*
     * Author : seungyeop.yeom
     * Type : Method
     * Date : 2015-03-03
     * Brief : BD update method for Search function
     */
    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d("YSY", "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d("YSY", "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
                ZenModeSettings.class.getName() });
    }

    /*
     * Author : seungyeop.yeom
     * Type : Search object
     * Date : 2015-02-16
     * Brief : Create of Interruption (DND) search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {

        private ZenModeConfig mConfig;
        private String mMainTitle;

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            mConfig = ZenModeUtils.getZenModeConfig();
            mMainTitle = context.getString(R.string.notification_settings)
                    + " > "
                    + context.getString(R.string.zen_mode_settings_title);
            
            // Search Calls and notification
            setSearchZenMode(context);

            // Search Calendar events
            setSearchEvent(context);

            // Search Incoming calls
            setSearchCalls(context);

            // Search New messages
            setSearchMessages(context);
            
            // Search Allowed contacts
            setSearchStarred(context);

            return mResult;
        }

        private void setSearchZenMode(Context context) {
            String mNotiText;

            if (Utils.isVoiceCapable(context)) {
                mNotiText = context.getString(R.string.zen_mode_option_title);
            } else {
                mNotiText = context
                        .getString(R.string.zen_mode_option_title_novoice);
            }

            setSearchIndexData(context, KEY_ZEN_MODE, mNotiText, mMainTitle,
                    null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ZenModeSettingsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
        }

        private void setSearchEvent(Context context) {
            int checkEvent = 0;
            if (mConfig.allowCalls) {
                checkEvent = 1;
            } else {
                checkEvent = 0;
            }

            setSearchIndexData(context, KEY_EVENTS,
                    context.getString(R.string.zen_mode_events), mMainTitle,
                    null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ZenModeSettingsActivity",
                    "com.android.settings", 1, "CheckBox", null, null, 1,
                    checkEvent);
        }

        private void setSearchCalls(Context context) {
            int isCalls = 1;
            if (Utils.isVoiceCapable(context)) {
                isCalls = 1;
            } else {
                isCalls = 0;
            }

            int checkCalls = 0;
            if (mConfig.allowCalls) {
                checkCalls = 1;
            } else {
                checkCalls = 0;
            }

            setSearchIndexData(context, KEY_CALLS,
                    context.getString(R.string.zen_mode_phone_calls),
                    mMainTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ZenModeSettingsActivity",
                    "com.android.settings", 1, "CheckBox", null, null, isCalls,
                    checkCalls);
        }

        private void setSearchMessages(Context context) {
            int checkMessages = 0;
            if (mConfig.allowMessages) {
                checkMessages = 1;
            } else {
                checkMessages = 0;
            }

            setSearchIndexData(context, KEY_MESSAGES,
                    context.getString(R.string.zen_mode_messages), mMainTitle,
                    null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ZenModeSettingsActivity",
                    "com.android.settings", 1, "CheckBox", null, null, 1,
                    checkMessages);
        }

        private void setSearchStarred(Context context) {
            int canContacts = 1;
            if (mConfig.allowCalls || mConfig.allowMessages) {
                canContacts = 1;
            } else {
                canContacts = 0;
            }

            setSearchIndexData(context, KEY_STARRED,
                    context.getString(R.string.zen_mode_from), mMainTitle,
                    null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$ZenModeSettingsActivity",
                    "com.android.settings", canContacts, null, null, null, 1, 0);
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
