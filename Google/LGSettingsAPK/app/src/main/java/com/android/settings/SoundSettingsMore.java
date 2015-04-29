/*
* Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.preference.PreferenceActivity;
import com.android.settings.handsfreemode.HandsFreeModeInfo;
import com.android.settings.handsfreemode.HandsFreeModeSwitchPreference;
import android.database.ContentObserver;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.provider.Settings.System;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.soundprofile.SoundProfileInfo;
import com.android.settings.vibratecreation.VibratePatternInfo;
import com.android.settings.SettingsPreferenceFragment;
import com.lge.media.RingtoneManagerEx;

import android.provider.Settings;


public class SoundSettingsMore extends SettingsPreferenceFragment {

    private CheckBoxPreference mVibrateOnTap;
    private Preference mSoundEffects;
    private Preference mNotificationSound;
    private Preference mSubNotificationPreference;
    private Preference mThirdNotificationPreference;


    private HandsFreeModeSwitchPreference mVoiceNotifications;
    private HandsFreeModeInfo mVoiceNotificationsInfo;
    private Context mContext;
    private ContentResolver resolver;

    public static final int REQUEST_CODE = -1;
    private static final int RESULT_HANDSFREE_MODE = 17;

    private static final String KEY_VIBRATE_ON_TAP = "vibrate_on_tap";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_VOICE_NOTIFICATIONS = "voice_notifications";

    private static final String KEY_LG_NOTIFICATIONS = "lg_notification";
    private static final String KEY_SUB_NOTIFICATION_SOUND = "sub_notification_sound"; // sub
    private static final String KEY_THIRD_NOTIFICATION_SOUND = "third_notification_sound"; // sub

    private static final int RENAME_SIM_TYPE_NOTIFICATION = 1;
    private static final int RENAME_SIM1_INDEX = 0;
    private static final int RENAME_SIM2_INDEX = 1;
    private static final int RENAME_SIM3_INDEX = 2;

    public static final int TYPE_NOTIFICATION_SIM2 = 16;
    public static final int TYPE_NOTIFICATION_SIM3 = 256;

    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY = 2;
    private static final int MSG_UPDATE_SUB_NOTIFICATION_SUMMARY = 4;
    private static final int MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY = 10;

    private StorageManager mStorageManager = null;
    private static final int DELAY_STORAGE = 1000;
    private Runnable mRingtoneLookupRunnable;



    SoundProfileInfo mSoundProfileInfo;

    private SettingsObserver mVibrateOnTapObserver;

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // to do
            do_updateRingtoneName();
        }
    };

    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            try {
                Thread.sleep(DELAY_STORAGE);
            } catch (Exception e) {
                ;
            }
            do_updateRingtoneName();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("SoundSettingsMore", "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sound_settings_more);

        mContext = getActivity();
        resolver = getActivity().getContentResolver();

        mVibrateOnTap = (CheckBoxPreference)findPreference(KEY_VIBRATE_ON_TAP);
        mSoundEffects = findPreference(KEY_SOUND_EFFECTS);
        mVoiceNotificationsInfo = new HandsFreeModeInfo(mContext);
        mVoiceNotifications = (HandsFreeModeSwitchPreference)findPreference(KEY_VOICE_NOTIFICATIONS);

        mNotificationSound = findPreference(KEY_LG_NOTIFICATIONS);
        mSubNotificationPreference = findPreference(KEY_SUB_NOTIFICATION_SOUND);
        mThirdNotificationPreference = findPreference(KEY_THIRD_NOTIFICATION_SOUND);

        mVibrateOnTapObserver = new SettingsObserver(new Handler());

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        mSoundProfileInfo = new SoundProfileInfo(mContext);
        resolver.registerContentObserver(
                Uri.parse("content://settings/system"), true, mObserver);
        do_InitRunnableMenu();

        if (mStorageManager == null) {
            mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }

        updateState(true);

    }

    private void do_InitRunnableMenu() {
        mRingtoneLookupRunnable = new Runnable() {
            public void run() {
                do_updateRingtoneName();
            }
        };
    }
    private void lookupRingtoneNames() {
        new Thread(mRingtoneLookupRunnable).start();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        resolver.unregisterContentObserver(mObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mVibrateOnTapObserver) {
            mVibrateOnTapObserver.observe();
        }
        IntentFilter filter = new IntentFilter(
                Intent.ACTION_MEDIA_SCANNER_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

        do_InitSIMDependancyMenu();
        updateState(true);
        lookupRingtoneNames();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mVibrateOnTapObserver) {
            mVibrateOnTapObserver.pause();
        }
        getActivity().unregisterReceiver(mReceiver);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.equals(mSoundEffects)) {
            // Sound effect
            startFragment(this,
                    "com.android.settings.TouchFeedbackAndSystemPreference",
                    REQUEST_CODE, null,
                    R.string.sp_sound_category_feedback_title_NORMAL);
        } else if (preference.equals(mVoiceNotifications)) {
            //voice notifications
            Intent i = new Intent("com.lge.settings.HANDSFREE_MODE_SETTING");
            startActivityForResult(i, RESULT_HANDSFREE_MODE);
        } else if (preference.equals(mVibrateOnTap)) {
            //vibrate on tap
            Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    mVibrateOnTap.isChecked() ? 1 : 0);
        } else if (preference.equals(mNotificationSound)) {
            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT", Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(
                    RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    RingtoneManagerEx.TYPE_NOTIFICATION);
            sharedPreferencesEditor.commit();
            startFragment(this, "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null, R.string.sp_sound_noti_NORMAL);
        } else if (preference == mSubNotificationPreference) {
            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT", Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();

            sharedPreferencesEditor.putInt(
                    RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_NOTIFICATION_SIM2);
            sharedPreferencesEditor.commit();

            startFragment(this, "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null,
                    R.string.sp_sub_sim2_notification_sound_title_NORMAL);

        } else if (preference == mThirdNotificationPreference) {

            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT", Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(
                    RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_NOTIFICATION_SIM3);
            sharedPreferencesEditor.commit();

            startFragment(this, "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null,
                    R.string.sp_sub_sim3_notification_sound_title_NORMAL);
        }

        updateState(true);
        return true;
    }

    private void updateState(boolean force) {
        if (null != mVoiceNotifications) {
            mVoiceNotifications.setCheckedUpdate();
            if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
                mVoiceNotificationsInfo
                        .setDBHandsFreeModeCall(mVoiceNotificationsInfo.getDBHandsFreeModeState());
                mVoiceNotifications.setSummary(R.string.hands_free_mode_read_out_call_summary_ex);
            } else {
                mVoiceNotifications.setTitle(R.string.voice_notifications_title_changed);
            }
        }


        if (null != mVibrateOnTap) {
            mVibrateOnTap.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            1) == 1 ? true : false);
        }

        do_updateRingtoneName();

    }

    private class SettingsObserver extends ContentObserver {
        private ContentResolver mResolver;

        SettingsObserver(Handler handler) {
            super(handler);
            mResolver = getActivity().getContentResolver();
        }

        void observe() {
            mResolver.registerContentObserver(
                    Settings.System.getUriFor(
                            Settings.System.HAPTIC_FEEDBACK_ENABLED), false, this);
        }

        void pause() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (null != mVibrateOnTap) {
                try {
                    mVibrateOnTap.setChecked(
                            Settings.System.getInt(
                                    mContext.getContentResolver(),
                                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1
                                    ?
                                    true : false);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void do_InitSIMDependancyMenu() {
        boolean isDualSim = Utils.isMultiSimEnabled(); // Dual Sim status check
        boolean isTripleSim = Utils.isTripleSimEnabled();
        if (isTripleSim == true) { //triple
            do_InitSIMDependancyMenu_triple();
        }
        else if (false == isDualSim) { //sigle
            do_InitSIMDependancyMenu_single();
        }
        else {
            do_InitSIMDependancyMenu_dual();
        }
    }

    private void do_InitSIMDependancyMenu_triple() {
        if (Utils.isUI_4_1_model(mContext)) {

            if (null != mNotificationSound) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM1_INDEX);
                mNotificationSound.setTitle(sim_name);
            }
            if (null != mSubNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM2_INDEX);
                mSubNotificationPreference.setTitle(sim_name);
            }
            if (null != mThirdNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM3_INDEX);
                mThirdNotificationPreference.setTitle(sim_name);
            }
        }
    }

    private void do_InitSIMDependancyMenu_single() {

        if (null != mSubNotificationPreference) {
            getPreferenceScreen().removePreference(mSubNotificationPreference);
        }

        if (null != mThirdNotificationPreference) {
            getPreferenceScreen()
                    .removePreference(mThirdNotificationPreference);
        }
    }

    private void do_InitSIMDependancyMenu_dual() {

        if (null != mNotificationSound) {
            String sim_name = Utils.do_getSoundSimName(mContext,
                    RENAME_SIM_TYPE_NOTIFICATION, RENAME_SIM1_INDEX);
            mNotificationSound.setTitle(sim_name);
        }

        if (null != mSubNotificationPreference) {
            String sim_name = Utils.do_getSoundSimName(mContext,
                    RENAME_SIM_TYPE_NOTIFICATION, RENAME_SIM2_INDEX);
            mSubNotificationPreference.setTitle(sim_name);
        }

        if (null != mThirdNotificationPreference) {
            getPreferenceScreen()
                    .removePreference(mThirdNotificationPreference);
        }

    }

    private void do_updateRingtoneName() {
        if (mNotificationSound != null) {
            updateRingtoneName(RingtoneManagerEx.TYPE_NOTIFICATION,
                    mNotificationSound,
                    MSG_UPDATE_NOTIFICATION_SUMMARY);
        }

        if (mSubNotificationPreference != null) {
            updateRingtoneName(TYPE_NOTIFICATION_SIM2,
                    mSubNotificationPreference,
                    MSG_UPDATE_SUB_NOTIFICATION_SUMMARY);
        }

        if (mThirdNotificationPreference != null) {
            updateRingtoneName(TYPE_NOTIFICATION_SIM3,
                    mThirdNotificationPreference,
                    MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY);
        }

    }

    private void updateRingtoneName(int type, Preference preference, int msg) {
        Context context = getActivity();
        if (context == null || preference == null) {
            return;
        }

        int index = SoundProfileInfo.INDEX_RINGTONE;
        switch (type) {
        case RingtoneManagerEx.TYPE_NOTIFICATION:
            index = SoundProfileInfo.INDEX_NOTIFICATION;
            break;
        case SoundProfileInfo.TYPE_NOTIFICATION_SIM2:
            index = SoundProfileInfo.INDEX_NOTIFICATION_SIM2;
            break;
        case SoundProfileInfo.TYPE_NOTIFICATION_SIM3:
            index = SoundProfileInfo.INDEX_NOTIFICATION_SIM3;
            break;
        default:
            break;
        }

        Uri ringtoneUri = null;
        Log.d("jw",
                "updateRingtoneName mSoundProfileInfo.getUserProfileName() :"
                        + mSoundProfileInfo.getUserProfileName());

        if (isChina()) {
            ringtoneUri = getDefaultRingtoneUriForChina(context, type, index);
        } else {
            ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(
                    context, type);
        }

        Log.d("jw" , "ringtoneUri : " + ringtoneUri);
        CharSequence summary = null;
        summary = SoundSettings.getTitle(context, ringtoneUri, true, type);
        mHandler.sendMessage(mHandler.obtainMessage(msg, summary));
    }

    private boolean isChina() {
        if ("CMCC".equals(Config.getOperator())
                || "CMO".equals(Config.getOperator())) {
            return true;
        } else if ("CTC".equals(Config.getOperator())
                || "CTO".equals(Config.getOperator())) {
            return true;
        } else {
            return false;
        }
    }

    private Uri getDefaultRingtoneUriForChina(Context context,
            int ringtoneType, int ringtonePrefIndex) {
        Uri ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(
                context, ringtoneType);
        if (mSoundProfileInfo.getUserProfileName().equals("")) {
            Uri ringtoneUriFromPref = Uri.parse(mSoundProfileInfo
                    .getSoundDefaultData(ringtonePrefIndex));
            if (!ringtoneUriFromPref.equals(ringtoneUri)) {
                mSoundProfileInfo.setDeafultSoundValue_Default();
                ringtoneUri = Uri.parse(mSoundProfileInfo
                        .getSoundDefaultData(ringtonePrefIndex));
            } else {
                ringtoneUri = ringtoneUriFromPref;
            }
        }
        return ringtoneUri;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_SUB_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY:
                do_handleMessage_Ringtone(msg);
                break;
            default:
                break;
            }
        }
    };

    private void do_handleMessage_Ringtone(Message msg) {
        switch (msg.what) {

        case MSG_UPDATE_NOTIFICATION_SUMMARY:
            if (mNotificationSound != null) {
                mNotificationSound.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_SUB_NOTIFICATION_SUMMARY:
            if (mSubNotificationPreference != null) {
                mSubNotificationPreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY:
            if (mThirdNotificationPreference != null) {
                mThirdNotificationPreference.setSummary((CharSequence)msg.obj);
            }
            break;
        default:
            break;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Log.i("jw", "#####ACTION_MEDIA_SCANNER_FINISHED");
                do_updateRingtoneName();
            }
        }
    };
}
