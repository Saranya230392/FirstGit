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

import com.android.settings.bluetooth.DockEventReceiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
//import android.media.RingtoneManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.lge.media.RingtoneManagerEx;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import android.app.ActionBar;
import android.content.ContentUris;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.Ringtone;
import android.os.SystemClock;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.SettingNotFoundException;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import android.os.SystemProperties;
import android.os.Build;

//[S][2012.02.09][susin.park][common][common] Add to Quiet time menu
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.database.ContentObserver;
import android.text.format.DateFormat;
import android.content.SharedPreferences;
import android.view.MenuItem;
import java.util.Calendar;
import java.util.prefs.PreferenceChangeListener;

//import com.lge.constants.SettingsConstants;
//[E][2012.02.09][susin.park][common][common] Add to Quiet time menu
import android.content.ComponentName;
//[AUDIO_FWK]_START, 2012126, seil.park@lge.com, Display DEFAULT ringtone string instead of unknown ringtone.
// KLP import android.provider.DrmStore;
//[AUDIO_FWK]_END, 2012126, seil.park@lge.com, Display DEFAULT ringtone string instead of unknown ringtone.
//[S][2012.03.08][donghan07.lee][common][common] Ringtone and Noti summary update when SD card change event
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
//[E][2012.03.08][donghan07.lee][common][common] Ringtone and Noti summary update when SD card change event

//[S]expired DRM check
import android.provider.Settings.System;

import com.lge.constants.SettingsConstants;
import com.lge.lgdrm.Drm;
import com.lge.lgdrm.DrmManager;
import com.lge.lgdrm.DrmContentSession;

//[E]expired DRM check
import com.android.settings.vibratecreation.VibratePatternInfo;

import com.android.settings.handsfreemode.HandsFreeModeInfo;
import com.android.settings.handsfreemode.HandsFreeModeSwitchPreference;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.RingtonePickerInfo;

// [S] Vibrate strength reset
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.VolumeVibratorManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.sound.SoundSwitchPreference;
//[E] Vibrate strength reset
import com.android.settings.soundprofile.SoundProfileInfo;
import android.widget.ListView;
import java.lang.CharSequence;

import com.android.settings.notification.DropDownPreference;
import com.android.internal.widget.LockPatternUtils;
import android.provider.Settings.Global;
import com.android.settings.ModelFeatureUtils;

public class SoundSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "SoundSettings";

    private static final int DIALOG_NOT_DOCKED = 1;
    public static final int ON = 1;
    public static final int OFF = 0;
    public static final int REQUEST_CODE = -1;
    private static final int DEFAULT_EMPTY = -1;
    private static final int COUNT_ONE = 1;

    private static final int DELAY_TIME = 2000;
    private static final int SLEEP_TIME = 50;
    private static final int DELAY_STORAGE = 1000;
    private static final int LIM_RESOLVEINFO_SIZE = 2;
    private static final int PREVIEW_VIBRATE = 200;
    private static final int DEFAULT_KEY = 3;

    public static final int TYPE_RINGTONE_SIM2 = 8;
    public static final int TYPE_NOTIFICATION_SIM2 = 16;
    public static final int TYPE_RINGTONE_VC = 32;
    public static final int TYPE_RINGTONE_SIM3 = 128;
    public static final int TYPE_NOTIFICATION_SIM3 = 256;

    //native
    private static final String KEY_CATEGORY_SOUND_PROFILE = "category_sound_profile";
    private static final String KEY_VIBRATE = "vibrate_when_ringing";
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String KEY_MUSICFX = "musicfx";
    private static final String KEY_SOUND_SETTINGS = "sound_settings";
    private static final String KEY_RINGTONE = "ringtone";
    //private static final String KEY_NOTIFICATION_SOUND = "notification_sound";
    private static final String KEY_CATEGORY_CALLS = "category_calls_and_notification";
    private static final String KEY_DOCK_CATEGORY = "dock_category";
    private static final String KEY_DOCK_AUDIO_SETTINGS = "dock_audio";
    private static final String KEY_DOCK_SOUNDS = "dock_sounds";
    private static final String KEY_DOCK_AUDIO_MEDIA_ENABLED = "dock_audio_media_enabled";
    private static final String KEY_ROAMING_SOUND = "Eri_sounds";
    private static final String KEY_TOUCHFEEDBACK_AND_SYSTEM = "touch_feedback_and_system";
    //private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_CATEGORY_FEEDBACK_AND_VIBRATE =
            "category_feedback_and_vibrate";

    private static final String KEY_DOWNLOAD_RINGTONES = "download_ringtones";
    private static final String KEY_COLORING = "coloring";
    private static final String KEY_RINGTOYOU = "ringtoyou";

    private static final String KEY_CATEGORY_VIBRATE = "category_vibrate";
    private static final String KEY_MORE = "key_more";
    private static final String KEY_CATEGORY_NOTIFICATION = "category_notification";
    private static final String KEY_AUTO_COMPOSE = "auto_compose";


    //[E][2012.02.09][susin.park][common][common] system field category

    private static final String KEY_SOUND_PROFILE = "sound_profile";

    private static final String KEY_CATEGORY_CALLS_AND_NOTIFICATION =
            "category_calls_and_notification";
    private static final String KEY_VC_RINGTONE = "vt_ringtone";

    private static final String KEY_LG_RINGTONE = "lg_ringtone";
    private static final String KEY_SUB_RINGTONE = "sub_ringtone"; // sub sim
    private static final String KEY_THIRD_RINGTONE = "third_ringtone"; // sub sim

    private static final String KEY_LG_NOTIFICATION = "lg_notification";
    private static final String KEY_SUB_NOTIFICATION_SOUND = "sub_notification_sound"; // sub
    private static final String KEY_THIRD_NOTIFICATION_SOUND = "third_notification_sound"; // sub

    private static final String KEY_GENTLE_VIBRATION = "gentle_vibration";
    private static final String KEY_VIBRATE_TYPE = "vibrate_type";
    private static final String KEY_VIBRATE_VOLUME = "vibrate_volume";
    private static final String KEY_INCOMING_VIBRATION = "incoming_vibration";
    private static final String KEY_SUB_INCOMING_VIBRATION = "sub_incoming_vibration";
    private static final String KEY_THIRD_INCOMING_VIBRATION = "third_incoming_vibration";
    private static final String KEY_SMART_RINGTONE = "smart_ringtone_sound";
    private static final String KEY_HANDS_FREE_MODE = "hands_free_mode";
    private static final String KEY_ZEN_MODE = "zen_mode";
    private static final String KEY_APP_NOTIFICATIONS = "app_notifications";
    private static final int RESULT_HANDSFREE_MODE = 17;

    public static final String EASYSETTINGS = "com.lge.easysettings.SOUND_SETTING";
    private static final String[] NEED_VOICE_CAPABILITY = {
            KEY_RINGTONE, KEY_CATEGORY_CALLS
    };

    private static final int MSG_UPDATE_RINGTONE_SUMMARY = 1;
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY = 2;
    // LGE_CHANGE_S [susin.park@lge.com 2011.11.22.] Optimus 3.0 scenario
    private static final int MSG_UPDATE_SUB_RINGTONE_SUMMARY = 3;
    private static final int MSG_UPDATE_SUB_NOTIFICATION_SUMMARY = 4;
    // LGE_CHANGE_E [susin.park@lge.com 2011.11.22.] Optimus 3.0 scenario

    private static final int MSG_UPDATE_VC_RINGTONE_SUMMARY = 6;

    //third sim for v4
    private static final int MSG_UPDATE_THIRD_RINGTONE_SUMMARY = 9;
    private static final int MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY = 10;

    private static final String DISABLE = "0";
    private static final String ENABLE = "1";

    private static final int POSITION_SOUND = 0;
    private static final int POSITION_VIBRATE = 1;
    private static final int POSITION_SILENT = 2;
    public static final int FLAG_TRUE = 1;
    public static final int FLAG_FALSE = 0;

    private static final int RENAME_SIM_TYPE_RINGTONE = 0;
    private static final int RENAME_SIM_TYPE_NOTIFICATION = 1;
    private static final int RENAME_SIM_TYPE_VIBRATION = 2;

    private static final int RENAME_SIM1_INDEX = 0;
    private static final int RENAME_SIM2_INDEX = 1;
    private static final int RENAME_SIM3_INDEX = 2;
    ArrayList<String> mItems;

    //MySoundProfile
    private static final String KEY_MY_SOUND_PROFILE = "my_sound_profile";
    private Preference mMySoundProfile;

    // Notification LED
    private static final String KEY_EMOTIONAL_LED_UI_4_2 = "emotional_led_ui_4_2";
    private SwitchPreference mEmotionLED;


    /**
     * enable : "1", disable : "0"
     * mUIStatus[x][0] 0 : Silent mode status
     * mUIStatus[x][1] 1 : Vibrate mode status
     * mUIStatus[x][2] 2 : Sound mode status
     * mUIStatus[x][3] 3 : Preference key name
     *
     * call method : do_ShowMenuCheck()
     **/
    private static final String[][] UISTATUS = {
            { DISABLE, DISABLE, ENABLE, KEY_VC_RINGTONE },
            { DISABLE, DISABLE, ENABLE, KEY_LG_RINGTONE },
            { DISABLE, DISABLE, ENABLE, KEY_SUB_RINGTONE },
            { DISABLE, DISABLE, ENABLE, KEY_THIRD_RINGTONE },
            { DISABLE, DISABLE, ENABLE, KEY_LG_NOTIFICATION },
            { DISABLE, DISABLE, ENABLE, KEY_SUB_NOTIFICATION_SOUND },
            { DISABLE, DISABLE, ENABLE, KEY_THIRD_NOTIFICATION_SOUND },
            { DISABLE, ENABLE, ENABLE, KEY_GENTLE_VIBRATION },
            { ENABLE, ENABLE, ENABLE, KEY_VIBRATE_TYPE },
            { DISABLE, ENABLE, ENABLE, KEY_VIBRATE_VOLUME },
            { ENABLE, ENABLE, ENABLE, KEY_INCOMING_VIBRATION },
            { ENABLE, ENABLE, ENABLE, KEY_SUB_INCOMING_VIBRATION },
            { ENABLE, ENABLE, ENABLE, KEY_THIRD_INCOMING_VIBRATION },
            { DISABLE, DISABLE, ENABLE, KEY_SMART_RINGTONE },
            { DISABLE, DISABLE, ENABLE, KEY_ROAMING_SOUND },
            { ENABLE, ENABLE, ENABLE, KEY_HANDS_FREE_MODE },
            { DISABLE, DISABLE, ENABLE, TouchFeedbackAndSystemPreference.KEY_DTMF_TONE },
            { DISABLE, DISABLE, ENABLE, TouchFeedbackAndSystemPreference.KEY_SOUND_EFFECTS },
            { DISABLE, DISABLE, ENABLE, TouchFeedbackAndSystemPreference.KEY_LOCK_SOUNDS },
            { ENABLE, ENABLE, ENABLE, TouchFeedbackAndSystemPreference.KEY_HAPTIC_FEEDBACK },
    };

    private static final String[][] UI41_UISTATUS = {
            { ENABLE, ENABLE, ENABLE, KEY_VC_RINGTONE },
            { ENABLE, ENABLE, ENABLE, KEY_LG_NOTIFICATION },
            { ENABLE, ENABLE, ENABLE, KEY_SUB_NOTIFICATION_SOUND },
            { ENABLE, ENABLE, ENABLE, KEY_THIRD_NOTIFICATION_SOUND },
            { ENABLE, ENABLE, ENABLE, KEY_VIBRATE_VOLUME },
    };

    private static final String[] REMOVE_MENU_TABLET_WIFI = {
            //KEY_CATEGORY_SOUND_PROFILE,
            //KEY_SOUND_PROFILE,
            //KEY_RING_VOLUME,
            KEY_VC_RINGTONE,
            KEY_LG_RINGTONE,
            KEY_SUB_RINGTONE,
            KEY_THIRD_RINGTONE,
            KEY_RINGTONE,
            //KEY_LG_NOTIFICATION,
            //KEY_SUB_NOTIFICATION_SOUND,
            //KEY_THIRD_NOTIFICATION_SOUND,
            //KEY_VIBRATE_VOLUME,
            KEY_GENTLE_VIBRATION,
            //KEY_VIBRATE_TYPE,
            KEY_INCOMING_VIBRATION,
            KEY_SUB_INCOMING_VIBRATION,
            KEY_THIRD_INCOMING_VIBRATION,
            KEY_SMART_RINGTONE,
            KEY_VIBRATE,
            KEY_HANDS_FREE_MODE,
            KEY_DOWNLOAD_RINGTONES,
            KEY_CATEGORY_CALLS_AND_NOTIFICATION,
            KEY_TOUCHFEEDBACK_AND_SYSTEM,
            KEY_COLORING,
            KEY_RINGTOYOU
    };

    private static final String[] REMOVE_MENU_TABLET_070 = {
            //KEY_CATEGORY_SOUND_PROFILE,
            //KEY_SOUND_PROFILE,
            //KEY_RING_VOLUME,
            KEY_VC_RINGTONE,
            //KEY_LG_RINGTONE,
            KEY_SUB_RINGTONE,
            KEY_THIRD_RINGTONE,
            KEY_RINGTONE,
            //KEY_LG_NOTIFICATION,
            KEY_SUB_NOTIFICATION_SOUND,
            KEY_THIRD_NOTIFICATION_SOUND,
            //KEY_VIBRATE_VOLUME,
            KEY_GENTLE_VIBRATION,
            KEY_VIBRATE_TYPE,
            KEY_INCOMING_VIBRATION,
            KEY_SUB_INCOMING_VIBRATION,
            KEY_THIRD_INCOMING_VIBRATION,
            KEY_SMART_RINGTONE,
            //KEY_VIBRATE,
            KEY_HANDS_FREE_MODE,
            KEY_DOWNLOAD_RINGTONES,
            //KEY_CATEGORY_CALLS_AND_NOTIFICATION,
            KEY_TOUCHFEEDBACK_AND_SYSTEM
    };

    private static final String[] RINGTONE_WITH_VIBRATION_UPDATE_MENU = {
            KEY_GENTLE_VIBRATION,
//            KEY_VIBRATE_TYPE,
//            KEY_INCOMING_VIBRATION,
//            KEY_SUB_INCOMING_VIBRATION,
//            KEY_THIRD_INCOMING_VIBRATION
    };

    /* [BACKUP_SOUND_DB_LIST]
     00. Smart ringtone
     01. Gentle vibration
     02. Ringtone with vibration
     03. Dial pad touch tone
     04. Touch Sound
     05. Screen lock sound
     06. Sound when roming
     07. Emergency Tone
     08. Vibrate on Touch
     */
    private static final String SOUND_SETTING_BACKUP = "sound_setting_backup";
    private static final String EMERGENCY_TONE = "emergency_tone";
    private static final String ERI_SET = "lg_eri_set";
    private static final String SMART_RINGTONE = "smart_ringtone";
    public static final String GENTLE_VIBRATION_STATUS = "gentle_vibration_status";
    //private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";

    private static final String[] BACKUP_SOUND_DB_LIST = {
            SMART_RINGTONE,
            GENTLE_VIBRATION_STATUS,
            Settings.System.VIBRATE_WHEN_RINGING,
            Settings.System.DTMF_TONE_WHEN_DIALING,
            Settings.System.SOUND_EFFECTS_ENABLED,
            Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
            ERI_SET,
            EMERGENCY_TONE,
            Settings.System.HAPTIC_FEEDBACK_ENABLED
    };

    private CheckBoxPreference mVibrateWhenRinging;
    private CheckBoxPreference mDockAudioMediaEnabled;
    private CheckBoxPreference mDockSounds;
    private CheckBoxPreference mGentleVibration;
    private CheckBoxPreference mSmart_ringtone;

    private Preference mMusicFx;
    private Preference mRingtonePreference;
    private Preference mDockAudioSettings;
    private Preference mLGRingtonePreference;
    private Preference mLGNotificationPreference;
    private Preference mSubRingtonePreference;
    private Preference mSubNotificationPreference;
    private Preference mThirdRingtonePreference;
    private Preference mThirdNotificationPreference;
    private Preference mVCRingtonePreference;
    private Preference mDownloadRingtonesPreference;
    private Preference mColoringPreference;
    private Preference mRingtoyouPreference;
    private Preference mTouchFeedbackAndSystemPreference;

    private Preference mIncomingVibration;
    private Preference mSubIncomingVibration;
    private Preference mThirdIncomingVibration;
    private Preference mVibrateTypePreference;
    private Preference mSoundProfilePreference;
    private Preference mZenModePreference;
    private Preference mAppNotificationPreference;

    private PreferenceCategory touchFeedback_system_category; //susin.park
    private PreferenceCategory category; //ringtone/notification
    private PreferenceCategory mBasicCategory; //basic category
    private PreferenceCategory mVibrateCategory;
    private PreferenceCategory mNotificationCategory;
    private Preference mMorePreference;
    private SoundSwitchPreference mAutoCompose;

    private HandsFreeModeSwitchPreference mHandsFreeMode;

    /* [S] Tablet menu add */
    private CheckBoxPreference mDialPadTouchSoundsPreference;
    private CheckBoxPreference mTouchSoundsPreference;
    private CheckBoxPreference mLockSoundPreference;
    private CheckBoxPreference mVibrateOnTouchPreference;
    private boolean mbIsSoundPoolLoaded = true;
    private Runnable mTouchSoundsRunnable;
    /* [E] Tablet menu add */
    private Runnable mRingtoneLookupRunnable;

    private AudioManager mAudioManager;
    private VibrateVolumePreference mVibrateVolumePreference;
    private StorageManager mStorageManager = null;
    private Intent mDockIntent;
    private VibratePatternInfo mVibratePatternInfo;
    private Context mContext;
    private Activity activity;
    private HandsFreeModeInfo mHandsFreeModeInfo;
    private ContentResolver resolver;
    private int ringerMode;
    private int mZenDBmode;
    private int temp_ringermode_value = AudioManager.RINGER_MODE_NORMAL;
    public static final int INDEX_SOUNDPROFILE = 0;

    private AlertDialog resultDialog;

    SoundProfileInfo mSoundProfileInfo;

    ArrayList<String[]> sound_profile_entry;
    ArrayList<String[]> sound_profile_value;

    ArrayList<String[]> sound_profile_entry2;
    ArrayList<String[]> sound_profile_value2;

    ArrayList<String[]> sound_lockscreen_entry;
    ArrayList<String[]> sound_lockscreen_value;

    ArrayList<String[]> sound_lockscreen_off_entry;
    ArrayList<String[]> sound_lockscreen_off_value;

    RingtonePickerInfo mRingtoneInfo;

    private LGContext mServiceContext;
    private VolumeVibratorManager mVolumeVibrator;
    private SettingsObserver mHapticSettingsObserver;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_RINGTONE_SUMMARY:
            case MSG_UPDATE_SUB_RINGTONE_SUMMARY:
            case MSG_UPDATE_THIRD_RINGTONE_SUMMARY:
            case MSG_UPDATE_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_SUB_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_VC_RINGTONE_SUMMARY:
                do_handleMessage_Ringtone(msg);
                break;
            default:
                break;
            }
        }
    };

    private ContentObserver mEmotionLEDObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateHomeButtonLED();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                if (resultDialog != null) {
                    int radioPosition = 0;
                    int ringermode = mAudioManager.getRingerMode();
                    if (mSoundProfileInfo.getUserProfileName().equals("")) {
                        int zenDBMode = Global.getInt(getActivity()
                                .getContentResolver(), Global.ZEN_MODE, 0);
                        if (zenDBMode == 1 || zenDBMode == 2) {
                            ringermode = 0;
                        }
                        switch (ringermode) {
                        case AudioManager.RINGER_MODE_SILENT:
                            radioPosition = POSITION_SILENT;
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            radioPosition = POSITION_VIBRATE;
                            break;
                        case AudioManager.RINGER_MODE_NORMAL:
                            radioPosition = POSITION_SOUND;
                            if ("CMCC".equals(Config.getOperator()) || "CTC".equals(Config.getOperator()) ||
                                "CMO".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                                int value = Settings.System.getInt(context.getContentResolver(),
                                        Settings.System.VIBRATE_WHEN_RINGING, OFF);
                                if (value == ON) {
                                    radioPosition = 3;
                                }
                            }
                            break;
                        default:
                            return;
                        }
                    } else {
                        for (int i = 0; i < mItems.size(); i++) {
                            if (mSoundProfileInfo.getUserProfileName().equals(
                                    mItems.get(i))) {
                                radioPosition = i;
                            }
                        }
                    }
                    resultDialog.getListView().setItemChecked(radioPosition,
                            true);
                }
                updateState(false);
            }
            else if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT)) {
                handleDockChange(intent);
            }
            else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Log.i(TAG, "#####ACTION_MEDIA_SCANNER_FINISHED");
                do_updateRingtoneName();
            }
        }
    };

    private PreferenceGroup mSoundSettings;

    private Preference mLockscreen;
    private boolean mSecure;
    private static final String KEY_LOCK_SCREEN_NOTIFICATIONS = "lock_screen_notifications";
    private int mLockscreenSelectedValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        do_Init();
        do_InitPreferenceMenu();
        do_InitDefaultSetting();
        do_InitOperatorDependancyMenu();
        do_InitModelDependancyMenu();
        do_InitFuctionalMenu();
        do_InitRunnableMenu();
        do_InitRunnableMenu_touch();

        initDockSettings();

        do_InitMenuForWifiModel();
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            do_setMenuUI_4_2();
        } else {
            do_setMenuUI_4_1_MODEL();
        }
        updateLockscreenNotifications();
        mIsFirst = true;
    }

    protected void do_InitMenuForWifiModel() {
        if (Utils.supportSplitView(getActivity())) {
            int preference_length = 0;
            // Wi-Fi tablet model only
            if (false == Utils.is070Model(getActivity())) {
                preference_length = REMOVE_MENU_TABLET_WIFI.length;
                for (int i = 0; i < preference_length; i++) {
                    if (null != findPreference(REMOVE_MENU_TABLET_WIFI[i])) {
                        getPreferenceScreen()
                                .removePreference(findPreference(REMOVE_MENU_TABLET_WIFI[i]));
                    }
                }
                mVibrateVolumePreference.setSummary(R.string.vibrate_volumes_summary_wifi);
            }
            // 070 tablet model only
            else {
                preference_length = REMOVE_MENU_TABLET_070.length;
                for (int i = 0; i < preference_length; i++) {

                    if (null != findPreference(REMOVE_MENU_TABLET_070[i])) {
                        getPreferenceScreen()
                                .removePreference(findPreference(REMOVE_MENU_TABLET_070[i]));
                    }
                }

                // add TouchFeedback & System menu for 070 only
                /* DialPad touch tone */
                mDialPadTouchSoundsPreference = new CheckBoxPreference(mContext);
                mDialPadTouchSoundsPreference.setTitle(R.string.dtmf_tone_enable_title_new);
                mDialPadTouchSoundsPreference.setSummary(R.string.dtmf_tone_enable_summary_on_new);
                mDialPadTouchSoundsPreference
                        .setKey(TouchFeedbackAndSystemPreference.KEY_DTMF_TONE);
                getPreferenceScreen().addPreference(mDialPadTouchSoundsPreference);
                mVibrateVolumePreference.setSummary(R.string.vibrate_volumes_summary);
            }
            // add TouchFeedback & System menu

            /* Toouch sounds */
            mTouchSoundsPreference = new CheckBoxPreference(mContext);
            mTouchSoundsPreference.setTitle(R.string.sound_effects_enable_title_new);
            mTouchSoundsPreference.setSummary(R.string.sound_effects_enable_summary_on);
            mTouchSoundsPreference.setKey(TouchFeedbackAndSystemPreference.KEY_SOUND_EFFECTS);
            getPreferenceScreen().addPreference(mTouchSoundsPreference);
            mTouchSoundsPreference.setOrder(261);

            /* Screen lock sound */
            mLockSoundPreference = new CheckBoxPreference(mContext);
            mLockSoundPreference.setTitle(R.string.lock_sounds_enable_title);
            mLockSoundPreference.setSummary(R.string.lock_sounds_enable_summary_on_new);
            mLockSoundPreference.setKey(TouchFeedbackAndSystemPreference.KEY_LOCK_SOUNDS);
            getPreferenceScreen().addPreference(mLockSoundPreference);
            mLockSoundPreference.setOrder(262);

            /* Vibrate on touch */
            if (mVibrateOnTouchPreference == null) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator() && Utils.isHapticfeedbackSupport()) {
                    Log.d(TAG, "vibrator exist");
                    mVibrateOnTouchPreference = new CheckBoxPreference(mContext);
                    if (true == Utils.isUI_4_1_model(mContext)) {
                        mVibrateOnTouchPreference.setTitle(R.string.haptic_feedback_enable_title_tap);
                        mVibrateOnTouchPreference.setSummary(R.string.vibrate_on_touch_summary_tap_new);
                    } else {
                        mVibrateOnTouchPreference.setTitle(R.string.haptic_feedback_enable_title);
                        mVibrateOnTouchPreference.setSummary(R.string.vibrate_on_touch_summary);
                    }
                    mVibrateOnTouchPreference
                            .setKey(TouchFeedbackAndSystemPreference.KEY_HAPTIC_FEEDBACK);
                    getPreferenceScreen().addPreference(mVibrateOnTouchPreference);
                    mVibrateOnTouchPreference.setOrder(263);
                }
            }

        } else {
            if (mVibrateVolumePreference != null) {
                mVibrateVolumePreference.setSummary(R.string.vibrate_volumes_summary);
            }
        }
    }

    // === Lockscreen (public / private) notifications ===
    private void updateLockscreenNotifications() {
        if (mLockscreen == null) {
            return;
        }
        if (isDisneyModel()) {
            boolean notiEnabled = getLockscreenNotificationsEnabled();
            boolean privNotiEnabled = getLockscreenAllowPrivateNotifications();

            if (notiEnabled && privNotiEnabled) {
                mLockscreenSelectedValue = 0;
            } else if (notiEnabled && !privNotiEnabled) {
                mLockscreenSelectedValue = 1;
            } else {
                mLockscreenSelectedValue = 2;
            }
        } else {
            final boolean enabled = getLockscreenNotificationsEnabled();
            final boolean allowPrivate = !mSecure
                    || getLockscreenAllowPrivateNotifications();
            Log.d(TAG, "enabled : " + enabled);
            Log.d(TAG, "allowPrivate : " + allowPrivate);

            mLockscreenSelectedValue = !enabled ? 2 : allowPrivate ? 0 : 1;
            Log.d(TAG, "mLockscreenSelectedValue : " + mLockscreenSelectedValue);
            mUpdateLockScreenSummary(mLockscreenSelectedValue);
        }
        mUpdateLockScreenSummary(mLockscreenSelectedValue);
    }

    private boolean getLockscreenNotificationsEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0) != 0;
    }

    private boolean getLockscreenAllowPrivateNotifications() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0) != 0;
    }

    private Handler enable_handler = new Handler();
    private ContentObserver mAutoComposedObserver = new ContentObserver(
            enable_handler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, " mAutoComposedObserver::onChange:" + selfChange);
            setRingtoneEabledWhenAutoComposed();
        }
    };
 
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        do_InitSIMDependancyMenu();

        if (true == Utils.isUI_4_1_model(mContext) && null != mHapticSettingsObserver) {
            mHapticSettingsObserver.observe();
        }

        checkDrmRingtoneAndNotificationSound();

        updateState(true);
        lookupRingtoneNames();
        updateAllPreferences();

        IntentFilter filter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        getActivity().registerReceiver(mReceiver, filter);
        IntentFilter filter1 = new IntentFilter(
                AudioManager.RINGER_MODE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter2.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter2.addDataScheme("file");
        getActivity().registerReceiver(mReceiver, filter2, null, null);

        //AutoComposed DB Observer
        getActivity().getContentResolver()
                .registerContentObserver(Settings.System.getUriFor
                        (SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED), true, mAutoComposedObserver);

        RingerVolumePreference ringerVolumePref =
                (RingerVolumePreference)findPreference("ring_volume");
        if (ringerVolumePref != null) {
            ringerVolumePref.setEnabled(!mAudioManager.isMasterMute());
            ringerVolumePref.updateUI();
            if (Utils.supportSplitView(getActivity()) &&
                    !Utils.is070Model(getActivity())) {
                ringerVolumePref.setSummary(R.string.sp_sound_volume_summary_wifi);
            }

            if (null != ringerVolumePref.getDialog() && true == mAudioManager.isMasterMute()) {
                ringerVolumePref.getDialog().dismiss();
            }

            if (Utils.isUI_4_1_model(mContext)) {
                ringerVolumePref.setSummary(null);
            }
        }

        // When AutoComposed On Set Disabled Ring-tone
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            setRingtoneEabledWhenAutoComposed();

        }
        setSearchPerformClick();
        // mEmotionLED
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor("lge_notification_light_pulse"),
                true, mEmotionLEDObserver);

    }

    private void setRingtoneEabledWhenAutoComposed() {
        boolean isAutoCompose = Settings.System.getInt(getActivity()
                .getContentResolver(),
                SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, 0) != 0 ? true
                : false;
        if (mLGRingtonePreference != null) {
            mLGRingtonePreference.setEnabled(!isAutoCompose);
        }
        if (mSubRingtonePreference != null) {
            mSubRingtonePreference.setEnabled(!isAutoCompose);

        }
        if (mThirdRingtonePreference != null) {
            mThirdRingtonePreference.setEnabled(!isAutoCompose);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RingerVolumePreference ringerVolumePref =
                (RingerVolumePreference)findPreference("ring_volume");
        if (ringerVolumePref != null) {
            ringerVolumePref.RingStop();
        }

        if (true == Utils.isUI_4_1_model(mContext) && null != mHapticSettingsObserver) {
            mHapticSettingsObserver.pause();
        }

        getActivity().unregisterReceiver(mReceiver);
        getActivity().getContentResolver().unregisterContentObserver(
                mAutoComposedObserver);
        getActivity().getContentResolver().unregisterContentObserver(
                mEmotionLEDObserver);
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

    private void do_updateRingtoneName() {
        if (mRingtonePreference != null) {
            updateRingtoneName(RingtoneManagerEx.TYPE_RINGTONE,
                    mRingtonePreference, MSG_UPDATE_RINGTONE_SUMMARY);
        }

        if (mLGRingtonePreference != null) {
            updateRingtoneName(RingtoneManagerEx.TYPE_RINGTONE,
                    mLGRingtonePreference, MSG_UPDATE_RINGTONE_SUMMARY);
        }
        if (mLGNotificationPreference != null) {
            updateRingtoneName(RingtoneManagerEx.TYPE_NOTIFICATION,
                    mLGNotificationPreference,
                    MSG_UPDATE_NOTIFICATION_SUMMARY);
        }

        if (mSubRingtonePreference != null) {
            updateRingtoneName(TYPE_RINGTONE_SIM2,
                    mSubRingtonePreference,
                    MSG_UPDATE_SUB_RINGTONE_SUMMARY);
        }
        if (mSubNotificationPreference != null) {
            updateRingtoneName(TYPE_NOTIFICATION_SIM2,
                    mSubNotificationPreference,
                    MSG_UPDATE_SUB_NOTIFICATION_SUMMARY);
        }

        if (mThirdRingtonePreference != null) {
            updateRingtoneName(TYPE_RINGTONE_SIM3,
                    mThirdRingtonePreference,
                    MSG_UPDATE_THIRD_RINGTONE_SUMMARY);
        }
        if (mThirdNotificationPreference != null) {
            updateRingtoneName(TYPE_NOTIFICATION_SIM3,
                    mThirdNotificationPreference,
                    MSG_UPDATE_THIRD_NOTIFICATION_SUMMARY);
        }

        if (mVCRingtonePreference != null) {
            updateRingtoneName(TYPE_RINGTONE_VC,
                    mVCRingtonePreference,
                    MSG_UPDATE_VC_RINGTONE_SUMMARY);
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

    private void updateRingtoneName(int type, Preference preference, int msg) {
        Context context = getActivity();
        if (context == null || preference == null) {
            return;
        }

        int index = SoundProfileInfo.INDEX_RINGTONE;
        switch (type) {
        case RingtoneManagerEx.TYPE_RINGTONE:
            index = SoundProfileInfo.INDEX_RINGTONE;
            break;
        case SoundProfileInfo.TYPE_RINGTONE_SIM2:
            index = SoundProfileInfo.INDEX_RINGTONE_SIM2;
            break;
        case SoundProfileInfo.TYPE_RINGTONE_SIM3:
            index = SoundProfileInfo.INDEX_RINGTONE_SIM3;
            break;
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

        /* //native code
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        CharSequence summary = context.getString(com.android.internal.R.string.ringtone_unknown);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = context.getString(com.android.internal.R.string.ringtone_silent);
        } else {
            // Fetch the ringtone title from the media provider
            try {
                Cursor cursor = context.getContentResolver().query(ringtoneUri,
                        new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                    cursor.close();
                }
            } catch (SQLiteException sqle) {
                // Unknown title for the ringtone
            }
        }
        */
        Uri ringtoneUri = null;
        Log.d(TAG, "updateRingtoneName mSoundProfileInfo.getUserProfileName() :" + mSoundProfileInfo.getUserProfileName());

        if (Utils.isChina()) {
            ringtoneUri = getDefaultRingtoneUriForChina(context, type, index);
        } else {
            ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(
                    context, type);
        }

        Log.d(TAG , "ringtoneUri : " + ringtoneUri);
        CharSequence summary = null;
        summary = getTitle(context, ringtoneUri, true, type);
        mHandler.sendMessage(mHandler.obtainMessage(msg, summary));
    }

    private void lookupRingtoneNames() {
        new Thread(mRingtoneLookupRunnable).start();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mVibrateWhenRinging) {
            Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,
                    mVibrateWhenRinging.isChecked() ? ON : OFF);
            do_ShowMenuCheck_for_Ringtone_with_vibration();
        } else if (preference == mMusicFx) {
            // let the framework fire off the intent
            return false;
        } else if (preference.equals(mHandsFreeMode)) {
            Intent i = new Intent("com.lge.settings.HANDSFREE_MODE_SETTING");
            startActivityForResult(i, RESULT_HANDSFREE_MODE);
        } else if (preference.equals(findPreference("test_lock_screen"))) {
            Intent i = new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION");
            startActivity(i);
        } else if (preference == mDockAudioSettings) {
            int dockState = mDockIntent != null
                    ? mDockIntent.getIntExtra(Intent.EXTRA_DOCK_STATE, OFF)
                    : Intent.EXTRA_DOCK_STATE_UNDOCKED;

            if (dockState == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                showDialog(DIALOG_NOT_DOCKED);
            } else {
                if (mDockIntent != null) {
                    boolean isBluetooth =
                            mDockIntent.getParcelableExtra(
                                    BluetoothDevice.EXTRA_DEVICE) != null;

                    if (isBluetooth) {
                        Intent i = new Intent(mDockIntent);
                        i.setAction(DockEventReceiver.ACTION_DOCK_SHOW_UI);
                        i.setClass(getActivity(), DockEventReceiver.class);
                        getActivity().sendBroadcast(i);
                    } else {
                        PreferenceScreen ps = (PreferenceScreen)mDockAudioSettings;
                        Bundle extras = ps.getExtras();
                        extras.putBoolean(
                                "checked",
                                Settings.Global.getInt(
                                        getContentResolver(),
                                        Settings.Global.DOCK_AUDIO_MEDIA_ENABLED,
                                        OFF) == ON);
                        super.onPreferenceTreeClick(ps, ps);
                    }
                }
            }
        } else if (preference == mDockSounds) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DOCK_SOUNDS_ENABLED,
                    mDockSounds.isChecked() ? ON : OFF);
        } else if (preference == mDockAudioMediaEnabled) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DOCK_AUDIO_MEDIA_ENABLED,
                    mDockAudioMediaEnabled.isChecked() ? ON : OFF);
        } else if (preference.equals(mSoundProfilePreference)) {
            if (SystemProperties.getBoolean("settings.sound.profile", false) == true) {
                startTwoSoundProfile();
            } else {
                showSoundProfileDialog();
            }
        } else if (preference == mTouchFeedbackAndSystemPreference) {
            startFragment(this,
                    "com.android.settings.TouchFeedbackAndSystemPreference",
                    REQUEST_CODE,
                    null,
                    R.string.sp_sound_category_feedback_title_NORMAL);
        } else if (preference == mLGRingtonePreference ||
                preference == mLGNotificationPreference ||
                preference == mSubRingtonePreference ||
                preference == mSubNotificationPreference ||
                preference == mThirdRingtonePreference ||
                preference == mThirdNotificationPreference ||
                preference == mVCRingtonePreference ||
                preference == mDownloadRingtonesPreference ||
                preference == mColoringPreference ||
                preference == mRingtoyouPreference ||
                preference == mSmart_ringtone) {
            do_onPreferenceTreeClick_Ringtone(preference);
        } else if (preference == mVibrateTypePreference ||
                preference == mIncomingVibration ||
                preference == mSubIncomingVibration ||
                preference == mThirdIncomingVibration ||
                preference == mGentleVibration) {
            do_onPreferenceTreeClick_Vibrate(preference);
        } else if (preference == mTouchSoundsPreference) {
            if (mTouchSoundsPreference.isChecked()) {
                mbIsSoundPoolLoaded = true;
            } else {
                mbIsSoundPoolLoaded = false;
            }

            new Thread(mTouchSoundsRunnable).start();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SOUND_EFFECTS_ENABLED,
                    mTouchSoundsPreference.isChecked() ? ON : OFF);

        } else if (preference == mLockSoundPreference) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
                    mLockSoundPreference.isChecked() ? ON : OFF);

        } else if (preference == mVibrateOnTouchPreference) {
            Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    mVibrateOnTouchPreference.isChecked() ? ON : OFF);
        } else if (preference == mDialPadTouchSoundsPreference) {
            Settings.System.putInt(getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING,
                    mDialPadTouchSoundsPreference.isChecked() ? ON : OFF);
        } else if (preference == mZenModePreference) {
            startFragment(this,
                    "com.android.settings.notification.ZenModeSettings",
                    REQUEST_CODE,
                    null,
                    R.string.zen_mode_settings_title);
        }
        else if (preference == mAppNotificationPreference) {
            startFragment(this,
                    "com.android.settings.notification.NotificationAppList",
                    REQUEST_CODE,
                    null,
                    R.string.app_notifications_title);
        } else if (preference.equals(mLockscreen)) {
            mLockscreenTreeClick();
        } else if (preference.equals(mMorePreference)) {
            startFragment(this,
                    "com.android.settings.SoundSettingsMore",
                    REQUEST_CODE,
                    null,
                    R.string.display_more);
        } else if (preference.equals(mAutoCompose)) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
        } else if (preference.equals(mMySoundProfile)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.soundprofile.SoundProfileSetting");
            startActivity(i);
        } else if (preference.equals(mEmotionLED)) {
            startFragment(this,
                    "com.android.settings.lge.EmotionalLEDEffectTab",
                    REQUEST_CODE, null, R.string.notification_led);
        }
        updateState(true);
        return true;
    }

    private void mLockscreenTreeClick() {
        if (isDisneyModel()) {
            showLockScreenDialog();
        } else {
            if (mSecure) {
                showLockScreenDialog();
            } else {
                showLockScreenOffDialog();
            }
        }
    }

    private boolean isDisneyModel() {
        return Config.getOperator().equals("DCM")
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_disney_cover,
                        "com.lge.R.bool.config_using_disney_cover");
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    synchronized private void UpdateSoundEffect(boolean isSoundEffect) {
        if (true == isSoundEffect) {
            mAudioManager.loadSoundEffects();
            Log.i(TAG, "UpdateSoundEffect()load!!");
        } else {
            long kStandbyTime = DELAY_TIME;
            long mStandbyTime = SystemClock.uptimeMillis() + kStandbyTime;
            while (!mbIsSoundPoolLoaded) {
                if (SystemClock.uptimeMillis() > mStandbyTime) {
                    mAudioManager.unloadSoundEffects();
                    Log.i(TAG, "UpdateSoundEffect()unload!!");
                    break;
                }

                try {
                    Thread.sleep((long)SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "UpdateSoundEffect()end");
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_sound;
    }

    private boolean needsDockSettings() {
        return getResources().getBoolean(R.bool.has_dock_settings);
    }

    private void initDockSettings() {
        if (needsDockSettings()) {
            mDockSounds = (CheckBoxPreference)findPreference(KEY_DOCK_SOUNDS);
            mDockSounds.setPersistent(false);
            mDockSounds.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.DOCK_SOUNDS_ENABLED, OFF) != OFF);
            mDockAudioSettings = findPreference(KEY_DOCK_AUDIO_SETTINGS);
            mDockAudioSettings.setEnabled(false);
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_DOCK_CATEGORY));
            getPreferenceScreen().removePreference(findPreference(KEY_DOCK_AUDIO_SETTINGS));
            getPreferenceScreen().removePreference(findPreference(KEY_DOCK_SOUNDS));
            Settings.Global.putInt(resolver, Settings.Global.DOCK_AUDIO_MEDIA_ENABLED, ON);
        }
    }

    private void handleDockChange(Intent intent) {
        if (mDockAudioSettings != null) {
            int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, OFF);

            boolean isBluetooth =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) != null;

            mDockIntent = intent;

            if (dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                // remove undocked dialog if currently showing.
                try {
                    removeDialog(DIALOG_NOT_DOCKED);
                } catch (IllegalArgumentException iae) {
                    ;// Maybe it was already dismissed
                }

                if (isBluetooth) {
                    mDockAudioSettings.setEnabled(true);
                } else {
                    if (dockState == Intent.EXTRA_DOCK_STATE_LE_DESK) {
                        ContentResolver resolver = getContentResolver();
                        mDockAudioSettings.setEnabled(true);
                        if (Settings.Global.getInt(resolver,
                                Settings.Global.DOCK_AUDIO_MEDIA_ENABLED,
                                DEFAULT_EMPTY) == DEFAULT_EMPTY) {
                            Settings.Global.putInt(resolver,
                                    Settings.Global.DOCK_AUDIO_MEDIA_ENABLED, OFF);
                        }
                        mDockAudioMediaEnabled =
                                (CheckBoxPreference)findPreference(KEY_DOCK_AUDIO_MEDIA_ENABLED);
                        mDockAudioMediaEnabled.setPersistent(false);
                        mDockAudioMediaEnabled.setChecked(
                                Settings.Global.getInt(resolver,
                                        Settings.Global.DOCK_AUDIO_MEDIA_ENABLED, 0) != 0);
                    } else {
                        mDockAudioSettings.setEnabled(false);
                    }
                }
            } else {
                mDockAudioSettings.setEnabled(false);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOT_DOCKED) {
            return createUndockedMessage();
        }
        return null;
    }

    private Dialog createUndockedMessage() {
        final AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(R.string.dock_not_found_title);
        ab.setMessage(R.string.dock_not_found_text);
        ab.setPositiveButton(android.R.string.ok, null);
        return ab.create();
    }

    private void do_onPreferenceTreeClick_Ringtone(Preference preference) {
        if (preference == mLGRingtonePreference) {
            int title_res = R.string.ringtone_title;
            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    RingtoneManagerEx.TYPE_RINGTONE);
            sharedPreferencesEditor.commit();
            if (Utils.isUI_4_1_model(mContext)) {
                title_res = R.string.ringtone_title_ex;
            }
            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE,
                    null,
                    title_res);
        } else if (preference == mLGNotificationPreference) {
            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    RingtoneManagerEx.TYPE_NOTIFICATION);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null,
                    R.string.sp_sound_noti_NORMAL);
        }
        else if (preference == mSubRingtonePreference) {
            SharedPreferences pref = getActivity().getSharedPreferences(
                    "RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();

            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_RINGTONE_SIM2);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE,
                    null,
                    R.string.sp_sub_sim2_ringtone_title_NORMAL);
        } else if (preference == mSubNotificationPreference) {
            SharedPreferences pref = getActivity().getSharedPreferences("RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();

            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_NOTIFICATION_SIM2);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE,
                    null,
                    R.string.sp_sub_sim2_notification_sound_title_NORMAL);
        }
        else if (preference == mThirdRingtonePreference) {
            SharedPreferences pref = getActivity().getSharedPreferences("RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_RINGTONE_SIM3);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE,
                    null,
                    R.string.sp_sub_sim3_ringtone_title_NORMAL);
        } else if (preference == mThirdNotificationPreference) {
            SharedPreferences pref = getActivity().getSharedPreferences("RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                    TYPE_NOTIFICATION_SIM3);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null,
                    R.string.sp_sub_sim3_notification_sound_title_NORMAL);
        }
        else if (preference == mVCRingtonePreference) {
            SharedPreferences pref = getActivity().getSharedPreferences("RINGTONE_PARENT",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = pref.edit();
            sharedPreferencesEditor.putInt(RingtoneManagerEx.EXTRA_RINGTONE_TYPE, TYPE_RINGTONE_VC);
            sharedPreferencesEditor.commit();

            startFragment(this,
                    "com.android.settings.RingtonePicker",
                    REQUEST_CODE, null, R.string.sp_vc_ringtone_title_NORMAL);
        } else if (preference.equals(mDownloadRingtonesPreference)) {
            Uri uri = Uri.parse("http://waprd.telstra.com/redirect?target=3glatesttones");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (preference.equals(mColoringPreference)) {
            Uri uri = Uri.parse("http://www.tcoloring.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (preference.equals(mRingtoyouPreference)) {
            Uri uri = Uri.parse("http://ringtoyou.olleh.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (preference == mSmart_ringtone) {
            Settings.System.putInt(getContentResolver(),
                    "smart_ringtone",
                    mSmart_ringtone.isChecked() ? ON : OFF);
        }
    }

    private void do_InitSIMDependancyMenu_single() {
        if (null != mSubRingtonePreference
                || null != mSubNotificationPreference
                || null != mThirdRingtonePreference
                || null != mThirdNotificationPreference
                || null != mSubIncomingVibration
                || null != mThirdIncomingVibration
                ) {
            if (null != mSubRingtonePreference) {
                category.removePreference(mSubRingtonePreference);
                getPreferenceScreen().removePreference(mSubRingtonePreference);
            }
            if (null != mSubNotificationPreference) {
                category.removePreference(mSubNotificationPreference);
                getPreferenceScreen().removePreference(mSubNotificationPreference);
            }
            if (null != mThirdRingtonePreference) {
                category.removePreference(mThirdRingtonePreference);
                getPreferenceScreen().removePreference(mThirdRingtonePreference);
            }
            if (null != mThirdNotificationPreference) {
                category.removePreference(mThirdNotificationPreference);
                getPreferenceScreen().removePreference(mThirdNotificationPreference);
            }

            if ( null != mSubIncomingVibration ) {
                getPreferenceScreen().removePreference(mSubIncomingVibration);
            }
            if ( null != mThirdIncomingVibration ) {
                getPreferenceScreen().removePreference(mThirdIncomingVibration);
            }
        }
        if (Utils.isUI_4_1_model(getActivity())) {
            if (null != mLGRingtonePreference) {
                mLGRingtonePreference.setTitle(R.string.ringtone_title_ex);
            }
            if (null != mIncomingVibration) {
                mIncomingVibration.setTitle(R.string.sp_quiet_mode_vibration_type);
            }
        }
    }

    private void do_InitSIMDependancyMenu_dual() {
        if (Utils.isUI_4_1_model(mContext)) {
            if (null != mLGRingtonePreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_RINGTONE,
                                    RENAME_SIM1_INDEX);
                mLGRingtonePreference.setTitle(sim_name);
            }
            if (null != mLGNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM1_INDEX);
                mLGNotificationPreference.setTitle(sim_name);
            }
            if (null != mSubRingtonePreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_RINGTONE,
                                    RENAME_SIM2_INDEX);
                mSubRingtonePreference.setTitle(sim_name);
            }
            if (null != mSubNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM2_INDEX);
                mSubNotificationPreference.setTitle(sim_name);
            }
            if (null != mIncomingVibration) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM1_INDEX);
                mIncomingVibration.setTitle(sim_name);
            }
            if (null != mSubIncomingVibration) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM2_INDEX);
                mSubIncomingVibration.setTitle(sim_name);
            }
            if (null != mIncomingVibration) {
                mIncomingVibration
                    .setSummary(mVibratePatternInfo
                                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1));
            }
            if (null != mSubIncomingVibration) {
                mSubIncomingVibration
                    .setSummary(mVibratePatternInfo
                                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2));
            }
        } else {
            if (null != mLGRingtonePreference) {
                mLGRingtonePreference.setTitle(R.string.sp_sub_sim1_ringtone_title_NORMAL);
            }
            if (null != mLGNotificationPreference) {
                mLGNotificationPreference
                    .setTitle(R.string.sp_sub_sim1_notification_sound_title_NORMAL);
            }
            if (null != mIncomingVibration) {
                mIncomingVibration.setTitle(R.string.sim1_incoming_call_vibration);
            }
            if (null != mSubIncomingVibration) {
                mSubIncomingVibration.setTitle(R.string.sim2_incoming_call_vibration);
            }
            if (null != mIncomingVibration) {
                mIncomingVibration
                        .setSummary(mVibratePatternInfo
                                .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1));
            }
            if (null != mSubIncomingVibration) {
                mSubIncomingVibration
                        .setSummary(mVibratePatternInfo
                                .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2));
            }
        }
        if (null != mThirdNotificationPreference) {
            getPreferenceScreen().removePreference(mThirdNotificationPreference);
        }
        if (null != mThirdIncomingVibration) {
            getPreferenceScreen().removePreference(mThirdIncomingVibration);
        }
        if (null != mThirdRingtonePreference
                && null != mThirdNotificationPreference) {
            category.removePreference(mThirdRingtonePreference);
            category.removePreference(mThirdNotificationPreference);
        }
        if (null != mThirdRingtonePreference) {
            getPreferenceScreen().removePreference(mThirdRingtonePreference);
        }
    }
    
    private void do_InitSIMDependancyMenu_triple() {
        if (Utils.isUI_4_1_model(mContext)) {
            if (null != mLGRingtonePreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_RINGTONE,
                                    RENAME_SIM1_INDEX);
                mLGRingtonePreference.setTitle(sim_name);
            }
            if (null != mLGNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM1_INDEX);
                mLGNotificationPreference.setTitle(sim_name);
            }
            if (null != mIncomingVibration) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM1_INDEX);
                mIncomingVibration.setTitle(sim_name);
            }
            if (null != mSubRingtonePreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_RINGTONE,
                                    RENAME_SIM2_INDEX);
                mSubRingtonePreference.setTitle(sim_name);
            }
            if (null != mSubNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM2_INDEX);
                mSubNotificationPreference.setTitle(sim_name);
            }
            if (null != mThirdRingtonePreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_RINGTONE,
                                    RENAME_SIM3_INDEX);
                mThirdRingtonePreference.setTitle(sim_name);
            }
            if (null != mThirdNotificationPreference) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_NOTIFICATION,
                                    RENAME_SIM3_INDEX);
                mThirdNotificationPreference.setTitle(sim_name);
            }
            if (null != mSubIncomingVibration) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM2_INDEX);
                mSubIncomingVibration.setTitle(sim_name);
            }
            if (null != mThirdIncomingVibration) {
                String sim_name = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM3_INDEX);
                mThirdIncomingVibration.setTitle(sim_name);
            }
            if (null != mIncomingVibration) {
                mIncomingVibration
                    .setSummary(mVibratePatternInfo
                                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1));
            }
            if (null != mSubIncomingVibration) {
                mSubIncomingVibration
                    .setSummary(mVibratePatternInfo
                                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2));
            }
            if (null != mThirdIncomingVibration) {
                mThirdIncomingVibration
                    .setSummary(mVibratePatternInfo
                                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM3));
            }
        } else {
            if (null != mLGRingtonePreference) {
                mLGRingtonePreference.setTitle(R.string.sp_sub_sim1_ringtone_title_NORMAL);
            }

            if (null != mLGNotificationPreference) {
                mLGNotificationPreference
                    .setTitle(R.string.sp_sub_sim1_notification_sound_title_NORMAL);
            }

            if (null != mIncomingVibration) {
                mIncomingVibration.setTitle(R.string.sim1_incoming_call_vibration);
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

    private void do_handleMessage_Ringtone(Message msg) {
        switch (msg.what) {
        case MSG_UPDATE_RINGTONE_SUMMARY:
            if (mRingtonePreference != null) {
                mRingtonePreference.setSummary((CharSequence)msg.obj);
            }
            if (mLGRingtonePreference != null) {
                mLGRingtonePreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_SUB_RINGTONE_SUMMARY:
            mSubRingtonePreference.setSummary((CharSequence)msg.obj);
            break;
        case MSG_UPDATE_THIRD_RINGTONE_SUMMARY:
            mThirdRingtonePreference.setSummary((CharSequence)msg.obj);
            break;
        case MSG_UPDATE_NOTIFICATION_SUMMARY:
            if (mLGNotificationPreference != null) {
                mLGNotificationPreference.setSummary((CharSequence)msg.obj);
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
        case MSG_UPDATE_VC_RINGTONE_SUMMARY:
            mVCRingtonePreference.setSummary((CharSequence)msg.obj);
            break;
        default:
            break;
        }
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //to do
            do_updateRingtoneName();
        }
    };

    public static String getTitle(Context context,
            Uri uri,
            boolean followSettingsUri,
            int ringtoneType) {
        Cursor cursor = null;
        ContentResolver res = context.getContentResolver();
        String selection = null;

        if (((ringtoneType & RingtoneManagerEx.TYPE_RINGTONE) != 0) ||
                ((ringtoneType & TYPE_RINGTONE_SIM2) != 0) ||
                ((ringtoneType & TYPE_RINGTONE_SIM3) != 0) ||
                ((ringtoneType & TYPE_RINGTONE_VC) != 0)) {
            selection = MediaStore.Audio.Media.IS_RINGTONE + " =? ";
        }

        if (((ringtoneType & RingtoneManagerEx.TYPE_NOTIFICATION) != 0) ||
                ((ringtoneType & TYPE_NOTIFICATION_SIM2) != 0) ||
                ((ringtoneType & TYPE_NOTIFICATION_SIM3) != 0)) {
            selection = MediaStore.Audio.Media.IS_NOTIFICATION + " =? ";
        }

        String title = null;
        try {
            if (uri != null && !uri.getPath().equals("") &&
                    !uri.toString().equals("content://media/external/audio/media/null")) {
                String authority = uri.getAuthority();
                Log.i(TAG, "uri.getpath = " + uri.getPath());

                if (Settings.AUTHORITY.equals(authority)) {
                    if (followSettingsUri) {
                        Uri actualUri = RingtoneManagerEx.getActualDefaultRingtoneUri(context,
                                RingtoneManagerEx.getDefaultType(uri));
                        String actualTitle = getTitle(context, actualUri, false, ringtoneType);
                        title = context.getString(
                                com.android.internal.R.string.ringtone_default_with_actual,
                                actualTitle);
                    }
                } else {
                    //KLP
                    /*
                                        if (DrmStore.AUTHORITY.equals(authority)) {
                                            cursor = res.query(
                                                            uri,
                                                            new String[]
                                                                    {DrmStore.Audio.TITLE},
                                                                     selection,
                                                                     new String[] {"1"},
                                                                     null);
                                        } else */ if (MediaStore.AUTHORITY.equals(authority)) {
                        cursor = res.query(
                                uri,
                                new String[]
                                { MediaStore.Audio.Media.TITLE },
                                selection,
                                new String[] { "1" },
                                null);
                    }
                    if (cursor != null && cursor.getCount() == ON) {
                        cursor.moveToFirst();
                        Log.i(TAG, "file exist title = " + cursor.getString(0));
                        title = cursor.getString(0);
                    } else {

                        String defaultpath = null;
                        String soundtype = null;
                        Log.i(TAG, "no ringtone/notification data  = " + uri.getPath());

                        if (ringtoneType == RingtoneManagerEx.TYPE_RINGTONE)
                        {
                            defaultpath = SystemProperties.get("ro.config.ringtone");
                            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
                        }
                        else if (ringtoneType == RingtoneManagerEx.TYPE_NOTIFICATION)
                        {
                            defaultpath = SystemProperties.get("ro.config.notification_sound");
                            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
                        }
                        else if (ringtoneType == RingtoneManagerEx.TYPE_ALARM)
                        {
                            defaultpath = SystemProperties.get("ro.config.alarm_alert");
                            soundtype = MediaStore.Audio.AudioColumns.IS_ALARM;
                        }
                        else if (ringtoneType == TYPE_RINGTONE_SIM2)
                        {
                            defaultpath = SystemProperties.get("ro.config.ringtone");
                            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
                        }
                        else if (ringtoneType == TYPE_NOTIFICATION_SIM2)
                        {
                            defaultpath = SystemProperties.get("ro.config.notification_sound");
                            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
                        }
                        else if (ringtoneType == TYPE_RINGTONE_SIM3)
                        {
                            defaultpath = SystemProperties.get("ro.config.ringtone");
                            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
                        }
                        else if (ringtoneType == TYPE_NOTIFICATION_SIM3)
                        {
                            defaultpath = SystemProperties.get("ro.config.notification_sound");
                            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
                        }
                        else if (ringtoneType == TYPE_RINGTONE_VC)
                        {
                            defaultpath = SystemProperties.get("ro.config.ringtone");
                            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
                        }
                        else
                        {
                            defaultpath = SystemProperties.get("ro.config.notification_sound");
                            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
                        }

                        cursor = res.query(
                                MediaStore.Audio.Media.INTERNAL_CONTENT_URI
                                , new String[] { MediaStore.Audio.Media.TITLE }
                                , MediaStore.Audio.Media.DISPLAY_NAME +
                                        " =?" + " AND " + soundtype + " =? "
                                , new String[] { defaultpath, "1" }
                                , null);

                        if (cursor != null && cursor.getCount() == COUNT_ONE)
                        {
                            //Find default content title
                            cursor.moveToFirst();
                            title = cursor.getString(0);
                        } else {
                            Log.e(TAG, "It has not default contents !! ");
                            title = null;
                        }

                        if (cursor != null) {
                            cursor = null;
                        }

                        if (title == null) {
                            title = context.getString(
                                    com.android.internal.R.string.ringtone_unknown);

                            if (title == null) {
                                title = "";
                            }
                        }
                        Log.i(TAG, "getTitle = " + title);
                    }
                }
            }
            else
            {
                if ("ATT".equals(Config.getOperator()) &&
                        ((ringtoneType == RingtoneManagerEx.TYPE_NOTIFICATION) ||
                                (ringtoneType == TYPE_NOTIFICATION_SIM2) ||
                        (ringtoneType == TYPE_NOTIFICATION_SIM3))) {
                    Log.i(TAG, "att notification getTitle  uri = null");
                    title = context.getString(R.string.ringtone_silent);
                } else {
                    int tempTypes = 0;
                    if (ringtoneType == RingtoneManagerEx.TYPE_RINGTONE) {
                        tempTypes = RingtonePickerInfo.TYPE_RINGTONE;
                    }
                    else if (ringtoneType == RingtoneManagerEx.TYPE_NOTIFICATION) {
                        tempTypes = RingtonePickerInfo.TYPE_NOTIFICATION;
                    }
                    else if (ringtoneType == RingtoneManagerEx.TYPE_ALARM) {
                        tempTypes = RingtonePickerInfo.TYPE_ALARM;
                    }
                    else if (ringtoneType == TYPE_RINGTONE_SIM2) {
                        tempTypes = RingtonePickerInfo.TYPE_RINGTONE_SIM2;
                    }
                    else if (ringtoneType == TYPE_NOTIFICATION_SIM2) {
                        tempTypes = RingtonePickerInfo.TYPE_NOTIFICATION_SIM2;
                    }
                    else if (ringtoneType == TYPE_RINGTONE_SIM3) {
                        tempTypes = RingtonePickerInfo.TYPE_RINGTONE_SIM3;
                    }
                    else if (ringtoneType == TYPE_NOTIFICATION_SIM3) {
                        tempTypes = RingtonePickerInfo.TYPE_NOTIFICATION_SIM3;
                    }
                    else if (ringtoneType == TYPE_RINGTONE_VC) {
                        tempTypes = RingtonePickerInfo.TYPE_RINGTONE_VC;
                    }

                    String defaultRingtoneName = "";
                    String soundtype = "";

                    if (tempTypes == RingtonePickerInfo.TYPE_RINGTONE ||
                            tempTypes == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                            tempTypes == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                            tempTypes == RingtonePickerInfo.TYPE_RINGTONE_VC) {
                        defaultRingtoneName = SystemProperties.get("ro.config.ringtone");
                        soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
                    }
                    //for Notification ringtone
                    else if (tempTypes == RingtonePickerInfo.TYPE_NOTIFICATION ||
                            tempTypes == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                            tempTypes == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
                        defaultRingtoneName = SystemProperties.get("ro.config.notification_sound");
                        soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
                    }
                    //for Alarm ringtone
                    else if (tempTypes == RingtonePickerInfo.TYPE_ALARM) {
                        ;//to do
                    }

                    Cursor tempcursor = res.query(
                            MediaStore.Audio.Media.INTERNAL_CONTENT_URI
                            , new String[] { MediaStore.Audio.Media.TITLE }
                            , MediaStore.Audio.Media.DISPLAY_NAME +
                                    " =?" + " AND " + soundtype + " =? "
                            , new String[] { defaultRingtoneName, "1" }
                            , null);

                    if (tempcursor != null && tempcursor.getCount() == COUNT_ONE) {
                        //Find default content title
                        tempcursor.moveToFirst();
                        title = tempcursor.getString(0);
                    } else {
                        Log.e(TAG, "It has not default contents !! ");
                        title = null;
                    }

                    if (tempcursor != null) {
                        tempcursor.close();
                    }

                    if (title == null) {
                        title = context.getString(com.android.internal.R.string.ringtone_unknown);

                        if (title == null) {
                            title = "";
                        }
                    }
                }
                Log.i(TAG, "getTitle  =  " + title);
            }
        } finally {
            if (cursor != null) {
                Log.d("jw", "cursor.close()");
                cursor.close();
            }
        }
        return title;
    }

    public String[] do_DBTokenizer(String db_values) {

        if (null == db_values) {
            db_values = "empty";
        }

        StringTokenizer stk = new StringTokenizer(db_values, ",");
        Log.i(TAG, "[token] token count :" + stk.countTokens());
        String[] sounds_db = new String[stk.countTokens()];
        int i = 0;
        while (stk.hasMoreTokens()) {
            sounds_db[i] = stk.nextToken();
            i++;
        }
        return sounds_db;
    }

    public void reSetSoundSetting(Activity _activity) {
        // Sound profile
        AudioManager am = (AudioManager)_activity.getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        // other settings
        String backup_DB = Settings.System.getString(_activity.getContentResolver(),
                SOUND_SETTING_BACKUP);
        Log.i(TAG, "backup db : " + backup_DB);

        if (null != backup_DB) {
            String[] db_values = do_DBTokenizer(backup_DB);
            Log.i(TAG, "db_values : " + db_values.toString());
            for (int i = 0; i < BACKUP_SOUND_DB_LIST.length; i++) {
                Log.i(TAG, BACKUP_SOUND_DB_LIST[i] + " : " + db_values[i]);
                if (!BACKUP_SOUND_DB_LIST[i].equals(EMERGENCY_TONE)) {
                    Settings.System.putString(_activity.getContentResolver(),
                            BACKUP_SOUND_DB_LIST[i], db_values[i]);
                }
                else {
                    Settings.Global.putString(_activity.getContentResolver(),
                            BACKUP_SOUND_DB_LIST[i], db_values[i]);
                }
            }
        }

        if (true == Utils.isUI_4_1_model(_activity)) {
            Settings.System.putInt(_activity.getContentResolver(), "smart_ringtone", OFF);
        }

        // Ringtone & notification
        RingtonePickerInfo mRingtonePickerInfo = new RingtonePickerInfo(_activity);
        mRingtonePickerInfo.resetRingtone();

        // Incoming call vibration
        VibratePatternInfo mVibratePatternInfo = new VibratePatternInfo(_activity, OFF);
        mVibratePatternInfo.reSetVibratePattern();

        // Voice notification
        HandsFreeModeInfo mHandsFreeModeInfo = new HandsFreeModeInfo(_activity);
        mHandsFreeModeInfo.reSetVoiceNotification();

        // Volume level
        // FIXME : DEFAULT_STREAM_VOLUME is removed.
        
        mAudioManager = (AudioManager)_activity.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                5/*AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_RING]*/,
                0);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                5/*AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_NOTIFICATION]*/,
                0);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_SYSTEM,
                7/*AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_SYSTEM]*/,
                0);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                11/*AudioManager.DEFAULT_STREAM_VOLUME[AudioManager.STREAM_MUSIC]*/,
                AudioManager.FLAG_SHOW_UI_WARNINGS);

        // Vibrate Strength
        mServiceContext = new LGContext(_activity);
        mVolumeVibrator = (VolumeVibratorManager)mServiceContext
                .getLGSystemService(LGContext.VOLUMEVIBRATOR_SERVICE);

        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                VolumeVibratorManager.VIBRATE_DEFAULT_VOLUME);
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                VolumeVibratorManager.VIBRATE_DEFAULT_VOLUME);
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION,
                VolumeVibratorManager.VIBRATE_DEFAULT_VOLUME);
        // MPCS power up tonei
        // no use power-up tone. leave to check history
        if (false) {
            String dir = "data/data/com.android.settings/";
            File folder = new File(dir + "powersound");
            File[] childFileList = folder.listFiles();
            if (childFileList != null) {
                for (File childFile : childFileList) {
                    Log.i(TAG, "[deletePowerSoundFolder] for");
                    childFile.delete();
                }
            }
            folder.delete();
        }
    }

    private void reSetRingtone(Context context, int types) {
        String defaultRingtoneName = "";
        String soundtype = "";

        if (types == RingtonePickerInfo.TYPE_RINGTONE ||
                types == RingtonePickerInfo.TYPE_RINGTONE_SIM2 ||
                types == RingtonePickerInfo.TYPE_RINGTONE_SIM3 ||
                types == RingtonePickerInfo.TYPE_RINGTONE_VC) {
            defaultRingtoneName = SystemProperties.get("ro.config.ringtone");
            soundtype = MediaStore.Audio.AudioColumns.IS_RINGTONE;
        }
        //for Notification ringtone
        else if (types == RingtonePickerInfo.TYPE_NOTIFICATION ||
                types == RingtonePickerInfo.TYPE_NOTIFICATION_SIM2 ||
                types == RingtonePickerInfo.TYPE_NOTIFICATION_SIM3) {
            defaultRingtoneName = SystemProperties.get("ro.config.notification_sound");
            soundtype = MediaStore.Audio.AudioColumns.IS_NOTIFICATION;
        }
        //for Alarm ringtone
        else if (types == RingtonePickerInfo.TYPE_ALARM) {
            //to do

        }
        // [E][Settings][donghan07.lee] Add default rigtone URI according to stream type

        Log.e(TAG, "Default Ringtone Name(stream:" + defaultRingtoneName);

        String[] internal_columns = new String[] {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"",
                MediaStore.Audio.Media.TITLE_KEY };

        // get uri of defaultRingtone
        Cursor cursor = mRingtoneInfo.query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                internal_columns,
                MediaStore.Audio.Media.DISPLAY_NAME + " = " + "\""
                        + defaultRingtoneName + "\""
                        + " AND " + soundtype + " =  " + "\""
                        + COUNT_ONE + "\"",
                null,
                null);
        // LGE_CHANGE_E : 120615, MFW, byungju.ko
        Uri uri = getValidRingtoneUriFromCursorAndClose(cursor);
        setDefaultRingtone(context, uri, types);
    }

    private void setDefaultRingtone(Context context, Uri uri, int types) {
        //for Phone Ringtone
        mRingtoneInfo.do_setURI(uri, types);
    }

    private static Uri getValidRingtoneUriFromCursorAndClose(Cursor cursor) {
        if (cursor != null) {
            Uri uri = null;
            try {
                if (cursor.moveToFirst()) {
                    uri = getUriFromCursor(cursor);
                }
            } finally {
                Log.d(TAG, "getValidRingtoneUriFromCursorAndClose Closed");
                cursor.close();
            }
            return uri;
        } else {
            return null;
        }
    }

    private static Uri getUriFromCursor(Cursor cursor) {

        return ContentUris.withAppendedId(
                Uri.parse(cursor.getString(RingtonePickerInfo.URI_COLUMN_INDEX)),
                cursor.getLong(RingtonePickerInfo.ID_COLUMN_INDEX));
    }

    private void do_InitOperatorDependancyMenu() {
        if ("CMCC".equals(Config.getOperator()) || "CTC".equals(Config.getOperator()) ||
            "CMO".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            getPreferenceScreen().removePreference(mVibrateWhenRinging);
        }
        if (!("KT").equals(Config.getOperator()) || Utils.isFolderModel(mContext)) {
            getPreferenceScreen().removePreference(mVCRingtonePreference);
        }

        if (((!("SKT".equals(Config.getOperator())
                || "KT".equals(Config.getOperator())
                || "LGU".equals(Config.getOperator())) && Utils.isUpgradeModel())
                && mVibrateVolumePreference != null)) {
            getPreferenceScreen().removePreference(mVibrateVolumePreference);
        }
        if (!"TEL".equals(Config.getOperator())) {
            if (null != mDownloadRingtonesPreference) {
                category.removePreference(mDownloadRingtonesPreference);
                getPreferenceScreen().removePreference(mDownloadRingtonesPreference);
            }
        }

        if ("VZW".equals(Config.getOperator())) {
            /*[2014-12-17][seungyeop.yeom] modify for VZW ODR (Since applying the G2 LOS, G3 LOS)
            mSoundProfilePreference.setTitle(R.string.silent_mode_title);
            //mSoundProfilePreference.setDialogTitle(R.string.silent_mode_title);
            sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT] =
                    getResources().getString(R.string.sp_off_NORMAL);
            sound_profile_entry.get(0)[AudioManager.RINGER_MODE_VIBRATE] =
                    getResources().getString(R.string.vibrate_title);*/
            sound_profile_entry.get(0)[AudioManager.RINGER_MODE_NORMAL] =
                    getResources().getString(R.string.zen_mode_settings_title_vzw);
        }
        if (true == Utils.istargetOperator("DCM")) {
            sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT] =
                    getResources().getString(R.string.sp_SoundProfile_Sound_vibrate_NORMAL);
            mSoundProfilePreference.setTitle(R.string.sp_MannerMode_DCM);
            mVibrateWhenRinging.setTitle(R.string.vibrate_title);
        }

        if ("ATT".equals(Config.getOperator())) {
            mLGNotificationPreference
                    .setTitle(getResources().getString(R.string.default_notification_sound));
        }

        if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
            mHandsFreeMode.setSummary(R.string.hands_free_mode_read_out_call_summary_ex);
        }
    }

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

    private void do_updateState_vibrate_summary() {
        if (null != mIncomingVibration) {
            mVibratePatternInfo.checkedLGVibrateName(
                    VibratePatternInfo.INCOMING_CALL_SIM1,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM1));
            mIncomingVibration
                    .setSummary(mVibratePatternInfo
                            .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1));
        }

        if (null != mSubIncomingVibration) {
            mVibratePatternInfo
                    .checkedLGVibrateName(
                            VibratePatternInfo.INCOMING_CALL_SIM2,
                            mVibratePatternInfo
                                    .getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM2));
            mSubIncomingVibration
                    .setSummary(mVibratePatternInfo
                            .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2));
        }
        if (null != mThirdIncomingVibration) {
            mVibratePatternInfo
                    .checkedLGVibrateName(
                            VibratePatternInfo.INCOMING_CALL_SIM3,
                            mVibratePatternInfo
                                    .getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM3));
            mThirdIncomingVibration
                    .setSummary(mVibratePatternInfo
                            .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM3));
        }
        if (Utils.getVibrateTypeProperty().equals("2")) {
            mVibrateVolumePreference.setSummary(R.string.vibrate_volumes_summary_calls_noti);
        }
    }

    private boolean isSoundVibrateSelectedCMCC() {
        boolean isSoundVibateSelected = true;
        if (!"CMCC".equals(Config.getOperator()) && !"CTC".equals(Config.getOperator()) &&
            !"CMO".equals(Config.getOperator()) && !"CTO".equals(Config.getOperator())) {
            return false;
        }
        ArrayList userprofile_items = new ArrayList<String>();
        userprofile_items.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT]);
        userprofile_items.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_VIBRATE]);
        userprofile_items.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_NORMAL]);
        /*userprofile_items.add(getResources()
        .getString(R.string.sp_SoundProfile_Sound_vibrate_NORMAL));*/

        if (mSoundProfileInfo.getSoundProfilesCount() > 0) {
            ArrayList<String> userProfileName = mSoundProfileInfo
                    .getAllProfileName_User();
            for (int i = 0; i < mSoundProfileInfo.getSoundProfilesCount(); i++) {
                if (userProfileName.get(i).equals(getResources()
                        .getString(R.string.sp_sound_profile_default_summary_NORMAL))) {
                    mSoundProfileInfo.removeProfiles(getResources()
                            .getString(R.string.sp_sound_profile_default_summary_NORMAL));
                } else {
                    userprofile_items.add(userProfileName.get(i));
                }
            }
        }
        if (mSoundProfileInfo.getUserProfileName().equals("")) {
            int value = Settings.System.getInt(getContentResolver(),
                    Settings.System.VIBRATE_WHEN_RINGING, OFF);
            Log.d(TAG, "VIBRATE_WHEN_RINGING = " + value);
            if (value == ON && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                isSoundVibateSelected = true;
            } else {
                isSoundVibateSelected = false;
            }
        } else {
            for (int i = 0; i < userprofile_items.size(); i++) {
                if (mSoundProfileInfo.getUserProfileName().equals(userprofile_items.get(i))) {
                    isSoundVibateSelected = false;
                }
            }
        }
        Log.d(TAG, "isSoundVibateSelected = " + isSoundVibateSelected);
        return isSoundVibateSelected;
    }

    private void updateState(boolean force) {

        if (getActivity() == null) {
            return;
        }

        ringerMode = mAudioManager.getRingerMode();
        mZenDBmode = Global.getInt(getContentResolver(), Global.ZEN_MODE, 0);
        if (mZenDBmode == 1 || mZenDBmode == 2) {
            ringerMode = 0;
        }
        final int vibrateMode = mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        Log.i(TAG, "[updateState] ##### ringerMode = " + ringerMode);
        Log.i(TAG, "[updateState] ##### vibrateMode = " + vibrateMode);

        updateSoundProfile();
        do_updateState_vibrate_summary();

        mVibrateWhenRinging.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0) != 0);

        do_ShowMenuCheck();
        do_updateRingtoneName();

        if (null != mHandsFreeMode) {
            mHandsFreeMode.setCheckedUpdate();
            if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
                mHandsFreeModeInfo
                        .setDBHandsFreeModeCall(mHandsFreeModeInfo.getDBHandsFreeModeState());
            }
        }

        if (null != mVibrateOnTouchPreference) {
            mVibrateOnTouchPreference.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            ON) == ON ? true : false);
        }

        setupAutoCompose();
    }

    private void setupAutoCompose() {
        if (mAutoCompose != null) {
            mAutoCompose
                    .setChecked(Settings.System
                            .getInt(getActivity().getContentResolver(),
                                    SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED,
                                    0) != 0 ? true : false);

            mAutoCompose.setOnPreferenceChangeListener(null);
            mAutoCompose.setOnPreferenceChangeListener(this);
        }
    }

    private void updateSoundProfile() {
        // weiyt.jiang[s]
        if (mSoundProfileInfo.getUserProfileName().equals("")) {
            int value = Integer
                    .parseInt(sound_profile_value.get(0)[ringerMode]);
            if (isSoundVibrateSelectedCMCC()) {
                mSoundProfilePreference
                        .setSummary(R.string.sp_SoundProfile_Sound_vibrate_NORMAL);
            } else {
                if (SystemProperties
                        .getBoolean("settings.sound.profile", false) == true) {
                    upDateTwoSoundProfile();
                } else {
                    mSoundProfilePreference.setSummary(sound_profile_entry
                            .get(0)[value]);
                }
            }
            if (mMySoundProfile != null) {
                mMySoundProfile
                        .setSummary(R.string.sound_settings_profile_normal);
            }
        } else {
            if (isSoundVibrateSelectedCMCC()) {
                mSoundProfilePreference
                        .setSummary(R.string.sp_SoundProfile_Sound_vibrate_NORMAL);
            } else {
                mSoundProfilePreference.setSummary(mSoundProfileInfo
                        .getUserProfileName());
            }
            if (mMySoundProfile != null) {
                mMySoundProfile.setSummary(mSoundProfileInfo
                        .getUserProfileName());
            }
        }
        // weiyt.jiang[e]
    }
    // [START] Remove DND from SoundProfile
    private void upDateTwoSoundProfile() {

        if (AudioManager.RINGER_MODE_NORMAL == ringerMode) {
            mSoundProfilePreference.setSummary(sound_profile_entry2.get(0)[0]);
        } else if (AudioManager.RINGER_MODE_VIBRATE == ringerMode) {
            mSoundProfilePreference.setSummary(sound_profile_entry2.get(0)[1]);
        } else {
            mSoundProfilePreference.setSummary("");
        }
    }

    private void startTwoSoundProfile() {
        if (AudioManager.RINGER_MODE_NORMAL == ringerMode
                || AudioManager.RINGER_MODE_VIBRATE == ringerMode) {
            showTwoSoundProfileDialog();
        } else {
            Toast.makeText(getActivity(), R.string.sound_settings_dnd_toast,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showTwoSoundProfileDialog() {

        Log.i(TAG, "showTwoSoundProfileDialog");
        RingerVolumePreference ringerVolumePref = (RingerVolumePreference)findPreference("ring_volume");

        if (null != ringerVolumePref.getDialog()
                && ringerVolumePref.getDialog().isShowing()) {
            Log.i(TAG, "showing Ringervolume dialog - return");
            return;
        }
        if (null != resultDialog && true == resultDialog.isShowing()) {
            return;
        }

        if (AudioManager.RINGER_MODE_NORMAL == ringerMode) {
            temp_ringermode_value = 0;
        } else if (AudioManager.RINGER_MODE_VIBRATE == ringerMode) {
            temp_ringermode_value = 1;
        }
        resultDialog = new AlertDialog.Builder(mContext)
                .setTitle(mSoundProfilePreference.getTitle())
                .setSingleChoiceItems(sound_profile_entry2.get(0),
                        temp_ringermode_value,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface _dialog,
                                    int which) {
                                temp_ringermode_value = Integer
                                        .parseInt(sound_profile_value2.get(0)[which]);

                                if (temp_ringermode_value == 1) {
                                    ((Vibrator)mContext
                                            .getSystemService(Context.VIBRATOR_SERVICE))
                                            .vibrate(PREVIEW_VIBRATE);
                                }
                                if (temp_ringermode_value == 2
                                        || temp_ringermode_value == 1) {
                                    Global.putInt(getActivity()
                                            .getContentResolver(),
                                            Global.ZEN_MODE, 0);
                                    mAudioManager
                                            .setRingerMode(temp_ringermode_value);
                                }
                                _dialog.cancel();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
    }
    // [END] Remove DND from SoundProfile

    private void updateAllPreferences() {
        ringerMode = mAudioManager.getRingerMode();
        mZenDBmode = Global.getInt(getContentResolver(), Global.ZEN_MODE, 0);
        if (mZenDBmode == 1 || mZenDBmode == 2) {
            ringerMode = 0;
        }

        if (null != mSmart_ringtone) {
            mSmart_ringtone.setChecked(
                    Settings.System.getInt(activity.getContentResolver(),
                            "smart_ringtone",
                            ON) != OFF);
        }

        if (null != mGentleVibration) {
            mGentleVibration.setChecked(
                    Settings.System.getInt(activity.getContentResolver(),
                            GENTLE_VIBRATION_STATUS,
                            ON) != OFF);
        }

        if (true == Utils.supportSplitView(mContext)) {
            do_updateStatePreference_Tablet_Only();
        }

        updateHomeButtonLED();

    }

    private void updateHomeButtonLED() {
        if (mEmotionLED != null) {
            mEmotionLED.setChecked(Settings.System.getInt(getContentResolver(),
                    "lge_notification_light_pulse", 1) == 1 ? true : false);
        }
    }

    private void do_updateStatePreference_Tablet_Only() {
        if (null != mTouchSoundsPreference) {
            mTouchSoundsPreference.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.SOUND_EFFECTS_ENABLED,
                            ON) == ON ? true : false);
        }

        if (null != mLockSoundPreference) {
            mLockSoundPreference.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
                            ON) == ON ? true : false);
        }

        if (null != mVibrateOnTouchPreference) {
            mVibrateOnTouchPreference.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            ON) == ON ? true : false);
        }

        if (null != mDialPadTouchSoundsPreference) {
            mDialPadTouchSoundsPreference.setChecked(
                    Settings.System.getInt(resolver,
                            Settings.System.DTMF_TONE_WHEN_DIALING,
                            ON) == ON ? true : false);
        }
    }

    private void do_InitPreferenceMenu() {
        mVibrateWhenRinging = (CheckBoxPreference)findPreference(KEY_VIBRATE);

        mRingtonePreference = findPreference(KEY_RINGTONE);
        // Add MySoundProfile
        mMySoundProfile = findPreference(KEY_MY_SOUND_PROFILE);
        //hakgyu
        mBasicCategory = (PreferenceCategory)findPreference(KEY_CATEGORY_SOUND_PROFILE);
        category = (PreferenceCategory)findPreference(KEY_CATEGORY_CALLS_AND_NOTIFICATION);
        mVibrateCategory = (PreferenceCategory)findPreference(KEY_CATEGORY_VIBRATE);
        mNotificationCategory = (PreferenceCategory)findPreference(KEY_CATEGORY_NOTIFICATION);
        mMorePreference = findPreference(KEY_MORE);
        mAutoCompose = (SoundSwitchPreference)findPreference(KEY_AUTO_COMPOSE);
        touchFeedback_system_category =
                (PreferenceCategory)findPreference(KEY_CATEGORY_FEEDBACK_AND_VIBRATE);
        mDownloadRingtonesPreference = findPreference(KEY_DOWNLOAD_RINGTONES);
        mColoringPreference = findPreference(KEY_COLORING);
        mRingtoyouPreference = findPreference(KEY_RINGTOYOU);
        mLGRingtonePreference = findPreference(KEY_LG_RINGTONE);
        mLGNotificationPreference = findPreference(KEY_LG_NOTIFICATION);
        mSubRingtonePreference = findPreference(KEY_SUB_RINGTONE);
        mSubNotificationPreference = findPreference(KEY_SUB_NOTIFICATION_SOUND);
        mThirdRingtonePreference = findPreference(KEY_THIRD_RINGTONE);
        mThirdNotificationPreference = findPreference(KEY_THIRD_NOTIFICATION_SOUND);
        mVCRingtonePreference = findPreference(KEY_VC_RINGTONE);
        mTouchFeedbackAndSystemPreference = findPreference(KEY_TOUCHFEEDBACK_AND_SYSTEM);

        mVibratePatternInfo = new VibratePatternInfo(mContext, 0);
        mGentleVibration = (CheckBoxPreference)findPreference(KEY_GENTLE_VIBRATION);
        mIncomingVibration = (Preference)findPreference(KEY_INCOMING_VIBRATION);
        mSubIncomingVibration = (Preference)findPreference(KEY_SUB_INCOMING_VIBRATION);
        mThirdIncomingVibration = (Preference)findPreference(KEY_THIRD_INCOMING_VIBRATION);
        mVibrateTypePreference = findPreference(KEY_VIBRATE_TYPE);

        mVibrateVolumePreference = (VibrateVolumePreference)findPreference(KEY_VIBRATE_VOLUME);

        mSmart_ringtone = (CheckBoxPreference)findPreference(KEY_SMART_RINGTONE);
        mSmart_ringtone.setOnPreferenceChangeListener(this);

        mSoundProfilePreference =
                (Preference)getPreferenceScreen().findPreference(KEY_SOUND_PROFILE);

        mLockscreen =
            (Preference)getPreferenceScreen().findPreference(KEY_LOCK_SCREEN_NOTIFICATIONS);

        mZenModePreference = findPreference(KEY_ZEN_MODE);
        mAppNotificationPreference = findPreference(KEY_APP_NOTIFICATIONS);


        mHandsFreeModeInfo = new HandsFreeModeInfo(mContext);
        mHandsFreeMode = (HandsFreeModeSwitchPreference)findPreference(KEY_HANDS_FREE_MODE);
        
        //Notification LED
        mEmotionLED = (SwitchPreference)findPreference(KEY_EMOTIONAL_LED_UI_4_2);
        setUpNotificationLED();
    }

    private void setUpNotificationLED() {
        if (getResources()
                .getInteger(com.lge.R.integer.config_emotionalLedType) == 0
                || (com.lge.os.Build.LGUI_VERSION.RELEASE < com.lge.os.Build.LGUI_VERSION_NAMES.V4_2)) {
            getPreferenceScreen().removePreference(mEmotionLED);
        }
    }

    private void do_InitDefaultSetting() {
        ContentResolver resolver = getContentResolver();

        mRingtoneInfo = new RingtonePickerInfo(mContext);

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        ringerMode = mAudioManager.getRingerMode();

        mZenDBmode = Global.getInt(getContentResolver(), Global.ZEN_MODE, 0);
        if (mZenDBmode == 1 || mZenDBmode == 2) {
            ringerMode = 0;
        }

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            removePreference(KEY_VIBRATE);
        }
        if (!Utils.isVoiceCapable(getActivity())) {
            if (true == Utils.supportSplitView(mContext) &&
                    Utils.is070Model(getActivity())) {

            }
            else {
                removePreference(KEY_VIBRATE);
            }

        }

        mVibrateWhenRinging.setPersistent(false);
        mVibrateWhenRinging.setChecked(Settings.System.getInt(resolver,
                Settings.System.VIBRATE_WHEN_RINGING, 0) != 0);

        mSmart_ringtone.setPersistent(false);
        mSmart_ringtone.setChecked(Settings.System.getInt(resolver,
                "smart_ringtone",
                ON) == ON ? true : false);

        sound_profile_entry = new ArrayList<String[]>();
        sound_profile_value = new ArrayList<String[]>();
        sound_profile_entry
                .add(0, getResources().getStringArray(R.array.sp_sound_profile_los_entries_NORMAL));
        sound_profile_value
                .add(0, getResources().getStringArray(R.array.sp_sound_profile_values_NORMAL));

        sound_lockscreen_entry = new ArrayList<String[]>();
        sound_lockscreen_value = new ArrayList<String[]>();
        sound_lockscreen_entry
                .add(0, getResources().getStringArray(R.array.sp_lockscreen_entries_NORMAL));
        sound_lockscreen_value
                .add(0, getResources().getStringArray(R.array.sp_lockscreen_values_NORMAL));

        sound_lockscreen_off_entry = new ArrayList<String[]>();
        sound_lockscreen_off_value = new ArrayList<String[]>();
        sound_lockscreen_off_entry
                .add(0, getResources().getStringArray(R.array.sp_lockscreen_off_entries_NORMAL));
        sound_lockscreen_off_value
                .add(0, getResources().getStringArray(R.array.sp_lockscreen_off_values_NORMAL));

        //Remove DND from SoundProfile [START]
        sound_profile_entry2 = new ArrayList<String[]>();
        sound_profile_value2 = new ArrayList<String[]>();

        sound_profile_entry2.add(0, getResources().
                getStringArray(R.array.sp_sound_profile_two_entries_NORMAL));
        sound_profile_value2.add(0, getResources().
                getStringArray(R.array.sp_sound_profile_two_values_NORMAL));
        // Remove DND from SoundProfile [END]

    }

    private void do_InitModelDependancyMenu() {
        if (Utils.isUpgradeModel()) {
            if (null != mIncomingVibration) {
                getPreferenceScreen().removePreference(mIncomingVibration);
            }
            if (null != mSubIncomingVibration) {
                getPreferenceScreen().removePreference(mSubIncomingVibration);
            }
            if (null != mThirdIncomingVibration) {
                getPreferenceScreen().removePreference(mThirdIncomingVibration);
            }
        }
        else if (!Utils.isUpgradeModel()) {
            if (Utils.isSPRModel()) {
                if (null != mIncomingVibration) {
                    getPreferenceScreen().removePreference(mIncomingVibration);
                }
                if (null != mSubIncomingVibration) {
                    getPreferenceScreen().removePreference(mSubIncomingVibration);
                }
                if (null != mThirdIncomingVibration) {
                    getPreferenceScreen().removePreference(mThirdIncomingVibration);
                }
            }
        }

        if (Utils.isUpgradeModel() ||
                Utils.isVeeModel() ||
                SystemProperties.get("ro.build.product").equals("l1e")) {
            getPreferenceScreen().removePreference(mGentleVibration);
        }

        if (!(Utils.getVibrateTypeProperty().equals("1") || Utils.getVibrateTypeProperty().equals(
                "2"))) {
            if (null != mGentleVibration) {
                getPreferenceScreen().removePreference(mGentleVibration);
            }
            if (null != mVibrateVolumePreference) {
                getPreferenceScreen().removePreference(mVibrateVolumePreference);
            }
        }

        if (!Utils.isSPRModel()) {
            if (null != mVibrateTypePreference) {
                category.removePreference(mVibrateTypePreference);
                getPreferenceScreen().removePreference(mVibrateTypePreference);
            }
        }

        if (Utils.isnotSupportHandsFreeMode() || isGKmodel() || Utils.isUpgradeModel()) {
            getPreferenceScreen().removePreference(mHandsFreeMode);
        }

        if (!Utils.isSupprotColoring(getActivity())) {
            getPreferenceScreen().removePreference(mColoringPreference);
        }

        if ("true".equals(ModelFeatureUtils.getFeature(getActivity(), "add_kt_ui_ver12"))) {
            if (!Config.getOperator().equals("KT")) {
                getPreferenceScreen().removePreference(mRingtoyouPreference);
            }
        } else {
            getPreferenceScreen().removePreference(mRingtoyouPreference);
        }
    }

    // yonguk.kim JB+ migration 20130130 build error fix add isGKmodel method
    private boolean isGKmodel() {
        if ("geefhd4g_lgu_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_skt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_kt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geevl04e".equals(SystemProperties.get("ro.product.name")) ||
                "geevl04e".equals(Build.DEVICE) ||
                "geefhd_open_hk".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_tw".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_cis".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_my".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_stl_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_shb_sg".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_esa".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_ame".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_open_il".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_att_us".equals(SystemProperties.get("ro.product.name")) ||
                "gvfhd_kt_kr".equals(SystemProperties.get("ro.product.name")) ||
                "omegar_lgu_kr".equals(SystemProperties.get("ro.product.name")) ||
                "geefhd_tcl_mx".equals(SystemProperties.get("ro.product.name"))) { //GK hongkong
            return true;
        }
        else {
            return false;
        }
    }

    private void do_InitFuctionalMenu() {
        //int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        //ContentResolver resolver = getContentResolver();

        if (!getResources().getBoolean(R.bool.has_silent_mode)) {
            findPreference(KEY_RING_VOLUME).setDependency(null);
        }

        mSoundSettings = (PreferenceGroup)findPreference(KEY_SOUND_SETTINGS);

        mMusicFx = mSoundSettings.findPreference(KEY_MUSICFX);
        Intent i = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        PackageManager p = getPackageManager();
        List<ResolveInfo> ris = p.queryIntentActivities(i, PackageManager.GET_DISABLED_COMPONENTS);
        if (ris.size() <= LIM_RESOLVEINFO_SIZE) {
            // no need to show the item if there is no choice for the user to make
            // note: the built in musicfx panel has two activities (one being a
            // compatibility shim that launches either the other activity, or a
            // third party one), hence the check for <=2. If the implementation
            // of the compatbility layer changes, this check may need to be updated.
            mSoundSettings.removePreference(mMusicFx);
        }

        if (!Utils.isVoiceCapable(getActivity())) {
            for (String prefKey : NEED_VOICE_CAPABILITY) {
                Preference pref = findPreference(prefKey);
                if (pref != null) {
                    if (true == Utils.supportSplitView(mContext) &&
                            Utils.is070Model(getActivity())) {

                    }
                    else {
                        getPreferenceScreen().removePreference(pref);
                    }

                }
            }
        }

        if (Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_use_smart_ringtone,
                "com.lge.R.bool.config_use_smart_ringtone")) {

            Log.i(TAG, "smart ringtone true");
            if (!Utils.isUpgradeModel()) {
                mSmart_ringtone.setTitle(R.string.sp_smart_ringtone_title_NORMAL);
            }
            else {
            }
        }
        else {
            Log.i(TAG, "smart ringtone false");
            category.removePreference(mSmart_ringtone);
            getPreferenceScreen().removePreference(mSmart_ringtone);
        }
    }

    private void do_onPreferenceTreeClick_Vibrate(Preference preference) {
        if (preference == mVibrateTypePreference) {
            Intent i = new Intent();
            Bundle bun = new Bundle();
            bun.putBoolean("easysetting", false);
            if (Utils.isUpgradeModel()) {
                Log.i(TAG, "upgrade model");
                i.setClassName("com.android.settings",
                        "com.android.settings.VibrateTypePreference");
            } else {
                Log.i(TAG, "not upgrade model");
                i.setClassName("com.android.settings",
                        "com.android.settings.vibratecreation.VibrationPreferenceActivity");
            }

            i.putExtras(bun);
            startActivity(i);
        }
        else if (preference.equals(mIncomingVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.INCOMING_CALL_SIM1);
            startActivity(i);
        }

        else if (preference.equals(mSubIncomingVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.INCOMING_CALL_SIM2);
            startActivity(i);
        }
        else if (preference.equals(mThirdIncomingVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.INCOMING_CALL_SIM3);
            startActivity(i);
        }
        //[E][2012.04.12][yj1.cho][SPR][Common] Vibrate when change vibrate mode
        else if (preference.equals(mGentleVibration)) {
            Settings.System.putInt(activity.getContentResolver(),
                    GENTLE_VIBRATION_STATUS,
                    mGentleVibration.isChecked() ? ON : OFF);
        }
    }

    private void do_InitRunnableMenu() {
        mRingtoneLookupRunnable = new Runnable() {
            public void run() {
                do_updateRingtoneName();
            }
        };
    }

    private AlertDialog createDialog(final int soundProfile) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
        alt_bld.setIconAttribute(android.R.attr.alertDialogIcon);
        alt_bld.setMessage(R.string.sp_dlg_exit_quiet_mode).setCancelable(
                true).setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mAudioManager.setRingerMode(soundProfile);
                        if (soundProfile == AudioManager.RINGER_MODE_VIBRATE) {
                            ((Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE))
                                    .vibrate(PREVIEW_VIBRATE);
                        }
                        showSoundProfileDialog();
                    }
                }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alt_bld.create();
        alert.setTitle(R.string.sp_dlg_note_NORMAL);
        return alert;
    }
    private void showLockScreenDialog() {
            Log.i(TAG, "show LockScreen dialog");
            updateLockscreenNotifications();
            new AlertDialog.Builder(mContext)
                    .setTitle(mLockscreen.getTitle())
                    .setSingleChoiceItems(sound_lockscreen_entry.get(0), mLockscreenSelectedValue,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface _dialog, int which) {
                                    mLockscreenSelectedValue = Integer.parseInt(sound_lockscreen_value
                                            .get(0)[which]);

                                if (isDisneyModel()) {
                                    if (mLockscreenSelectedValue == 0) {
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 1);
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 1);
                                    } else if (mLockscreenSelectedValue == 1) {
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0);
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 1);
                                    } else {
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0);
                                        Settings.Secure.putInt(mContext.getContentResolver(),
                                                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0);
                                    }
                                } else {
                                    final boolean enabled = mLockscreenSelectedValue != 2;
                                    final boolean show = mLockscreenSelectedValue == 0;

                                    Settings.Secure
                                            .putInt(getContentResolver(),
                                                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS,
                                                    show ? 1 : 0);
                                    Settings.Secure
                                            .putInt(getContentResolver(),
                                                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS,
                                                    enabled ? 1 : 0);
                                    Log.d(TAG, "which : " + which);

                                    }
                                    mUpdateLockScreenSummary(mLockscreenSelectedValue);
                                    _dialog.cancel();
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
    }

    private void showLockScreenOffDialog() {
            Log.i(TAG, "show LockScreenOff dialog");
            updateLockscreenNotifications();
            if (mLockscreenSelectedValue == 2) {
                mLockscreenSelectedValue = 1;
            }
            new AlertDialog.Builder(mContext)
                    .setTitle(mLockscreen.getTitle())
                    .setSingleChoiceItems(sound_lockscreen_off_entry.get(0), mLockscreenSelectedValue,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface _dialog, int which) {
                                    mLockscreenSelectedValue = Integer.parseInt(sound_lockscreen_off_value
                                            .get(0)[which]);
                                    Log.d(TAG, "mLockscreenSelectedValue : " + mLockscreenSelectedValue);

                                    final boolean enabled = mLockscreenSelectedValue != 2;
                                    final boolean show = mLockscreenSelectedValue == 0;

                                    Log.d(TAG, "showLockScreenOffDialog enabled : " + enabled);
                                    Log.d(TAG, "showLockScreenOffDialog show : " + show);

                                    Settings.Secure.putInt(getContentResolver(),
                                            Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, show ? 1 : 0);
                                    Settings.Secure.putInt(getContentResolver(),
                                            Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, enabled ? 1 : 0);
                                    Log.d(TAG, "which : " + which);
                                    mUpdateLockScreenSummary(mLockscreenSelectedValue);
                                    _dialog.cancel();
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
    }
    private void mUpdateLockScreenSummary(int value) {
        switch (value) {
          case 0:
               mLockscreen.setSummary(R.string.lock_screen_notifications_summary_show);
               break;
          case 1:
               mLockscreen.setSummary(R.string.lock_screen_notifications_summary_hide);
               break;
          case 2:
               mLockscreen.setSummary(R.string.lock_screen_notifications_summary_disable);
               break;
           default:
               mLockscreen.setSummary(R.string.lock_screen_notifications_summary_show);
               break;
        }
    }
    private void showSoundProfileDialog() {
        if ("CMCC".equals(Config.getOperator()) || "CTC".equals(Config.getOperator())
            || "CMO".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
            mItems = new ArrayList<String>();
            mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT]);
            mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_VIBRATE]);
            mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_NORMAL]);
            mItems.add(getResources()
                    .getString(R.string.sp_SoundProfile_Sound_vibrate_NORMAL));

            if (mSoundProfileInfo.getSoundProfilesCount() > 0) {
                ArrayList<String> userProfileName = mSoundProfileInfo
                        .getAllProfileName_User();
                for (int i = 0; i < mSoundProfileInfo.getSoundProfilesCount(); i++) {
                    if (userProfileName.get(i).equals(getResources()
                            .getString(R.string.sp_sound_profile_default_summary_NORMAL))) {
                        mSoundProfileInfo.removeProfiles(getResources()
                                .getString(R.string.sp_sound_profile_default_summary_NORMAL));
                    } else {
                        mItems.add(userProfileName.get(i));
                    }
                }
            }
            CharSequence[] cs = mItems.toArray(new CharSequence[mItems.size()]);
            if (mSoundProfileInfo.getUserProfileName().equals("")) {
                if (isSoundVibrateSelectedCMCC() == true) {
                    temp_ringermode_value = 3;
                } else {
                    temp_ringermode_value = Integer
                            .parseInt(sound_profile_value.get(0)[ringerMode]);
                }
            } else {
                boolean isSoundVibateSelected = true;
                for (int i = 0; i < mItems.size(); i++) {
                    if (mSoundProfileInfo.getUserProfileName().equals(mItems.get(i))) {
                        temp_ringermode_value = i;
                        isSoundVibateSelected = false;
                    }
                }
                if (isSoundVibateSelected == true) {
                    temp_ringermode_value = 3;
                }
            }
            String mNeutralButtonText;
            OnClickListener mOnClickListener;
            if (ConfigHelper.isSupportSlideCover(mContext)) {
                mNeutralButtonText = null;
                mOnClickListener = null;

            } else {
                mNeutralButtonText = getString(R.string.edit);
                mOnClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent i = new Intent();
                        i.setClassName("com.android.settings",
                                "com.android.settings.soundprofile.SoundProfileSetting");
                        startActivity(i);
                    }
                };
            }
            // temp_ringermode_value = Integer.parseInt(sound_profile_value.get(0)[ringerMode]);
            resultDialog = new AlertDialog.Builder(mContext)
                    .setTitle(mSoundProfilePreference.getTitle())
                    .setSingleChoiceItems(cs, temp_ringermode_value,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface _dialog,
                                        int which) {
                                    Log.d(TAG, "which" + which);
                                    if (which < 3) {
                                        Settings.System.putInt(getContentResolver(),
                                                Settings.System.VIBRATE_WHEN_RINGING,
                                                OFF);
                                        temp_ringermode_value = Integer
                                                .parseInt(sound_profile_value.get(0)[which]);
                                        if (temp_ringermode_value != 0) {
                                            mAudioManager.setRingerMode(temp_ringermode_value);
                                        }
                                        if (temp_ringermode_value == AudioManager.RINGER_MODE_NORMAL) {
                                            for (int i = 0; i < RINGTONE_WITH_VIBRATION_UPDATE_MENU.length; i++) {
                                                if (null != findPreference(RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])) {
                                                    findPreference(
                                                            RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])
                                                            .setEnabled(false);
                                                }
                                            }
                                        }
                                        if (temp_ringermode_value == AudioManager.RINGER_MODE_VIBRATE) {
                                            ((Vibrator)mContext
                                                    .getSystemService(Context.VIBRATOR_SERVICE))
                                                    .vibrate(PREVIEW_VIBRATE);
                                        }
                                        do_SoundProfie(temp_ringermode_value);
                                        updateState(true);
                                    } else if (which == 3) {
                                        // update menu for Ringtone with vibration
                                        Settings.System.putInt(getContentResolver(),
                                                Settings.System.VIBRATE_WHEN_RINGING,
                                                ON);
                                        mAudioManager
                                                .setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                        for (int i = 0; i < RINGTONE_WITH_VIBRATION_UPDATE_MENU.length; i++) {
                                            if (null != findPreference(RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])) {
                                                findPreference(
                                                        RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])
                                                        .setEnabled(true);
                                            }
                                        }
                                        mSoundProfileInfo.setProfileDatatoDefault(which);
                                        Global.putInt(getContentResolver(), Global.ZEN_MODE, 0);
                                        mSoundProfileInfo.setUserProfileName("");
                                        ((Vibrator)mContext
                                                .getSystemService(Context.VIBRATOR_SERVICE))
                                                .vibrate(PREVIEW_VIBRATE);
                                        updateState(true);
                                    } else {
                                        Settings.System.putInt(getContentResolver(),
                                                Settings.System.VIBRATE_WHEN_RINGING,
                                                OFF);
                                        mSoundProfileInfo.setProfileDatatoSystem(mItems.get(which));
                                        mSoundProfileInfo.setUserProfileName(mItems.get(which));
                                        updateState(true);
                                    }
                                    _dialog.cancel();
                                }
                            }).setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(mNeutralButtonText, mOnClickListener)
                    .show();
        } else {
            Log.i(TAG, "show Soundprofile dialog");
            RingerVolumePreference ringerVolumePref =
                    (RingerVolumePreference)findPreference("ring_volume");

            if (null != ringerVolumePref.getDialog() && ringerVolumePref.getDialog().isShowing()) {
                Log.i(TAG, "showing Ringervolume dialog - return");
                return;
            }
            if (null != resultDialog &&
                    true == resultDialog.isShowing()) {
                return;
            }
            temp_ringermode_value = Integer.parseInt(sound_profile_value.get(0)[ringerMode]);
            resultDialog = new AlertDialog.Builder(mContext)
                    .setTitle(mSoundProfilePreference.getTitle())
                    .setSingleChoiceItems(sound_profile_entry.get(0), temp_ringermode_value,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface _dialog, int which) {
                                    temp_ringermode_value = Integer.parseInt(sound_profile_value
                                            .get(0)[which]);

                                    if (temp_ringermode_value == AudioManager.RINGER_MODE_VIBRATE) {
                                        ((Vibrator)mContext
                                                .getSystemService(Context.VIBRATOR_SERVICE))
                                                .vibrate(PREVIEW_VIBRATE);
                                    }
                                    if (temp_ringermode_value == 2 || temp_ringermode_value == 1) {
                                        Global.putInt(getContentResolver(), Global.ZEN_MODE, 0);
                                        mAudioManager.setRingerMode(temp_ringermode_value);
                                    } else if (temp_ringermode_value == 0) {
                                        Global.putInt(getContentResolver(), Global.ZEN_MODE, 2);
                                    }
                                    _dialog.cancel();
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void do_ShowMenuCheck() {
        final int key = DEFAULT_KEY;
        // UI 4.1 menu update
        if (Utils.isUI_4_1_model(mContext)) {
            int preference_length = UI41_UISTATUS.length;
            for (int i = 0; i < preference_length; i++) {
                if (null != findPreference(UI41_UISTATUS[i][key])) {
                    findPreference(UI41_UISTATUS[i][key])
                            .setEnabled(ENABLE.equals(UI41_UISTATUS[i][ringerMode]) ? true : false);
                }
            }
        } else {
            int preference_length = UISTATUS.length;
            for (int i = 0; i < preference_length; i++) {
                if (null != findPreference(UISTATUS[i][key])) {
                    findPreference(UISTATUS[i][key])
                            .setEnabled(ENABLE.equals(UISTATUS[i][ringerMode]) ? true : false);
                }
            }
        }
        do_ShowMenuCheck_for_Ringtone_with_vibration();
/*
        if (null != mHandsFreeMode) {
            mHandsFreeMode.setSwitchEnableStatus(
                    ringerMode == AudioManager.RINGER_MODE_NORMAL ? true : false);
        }
*/
    }

    private void mDonotDisturbDBchanged (int mTemp_ringermode_value) {
        if (mTemp_ringermode_value == 2 || mTemp_ringermode_value == 1) {
            Global.putInt(getActivity().getContentResolver(), Global.ZEN_MODE, 0);
        } else if (mTemp_ringermode_value == 0) {
            Global.putInt(getActivity().getContentResolver(), Global.ZEN_MODE, 2);
        }
    }

    private void do_SoundProfie (int mTemp_ringermode_value) {
        mSoundProfileInfo.setProfileDatatoDefault(temp_ringermode_value);
        mDonotDisturbDBchanged(temp_ringermode_value);
        mSoundProfileInfo.setUserProfileName("");
    }

    private void do_ShowMenuCheck_for_Ringtone_with_vibration() {
        // update menu for Ringtone with vibration
        if (AudioManager.RINGER_MODE_NORMAL == ringerMode && null != mVibrateWhenRinging) {

            for (int i = 0; i < RINGTONE_WITH_VIBRATION_UPDATE_MENU.length; i++) {
                if (null != findPreference(RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])) {
                    findPreference(RINGTONE_WITH_VIBRATION_UPDATE_MENU[i])
                            .setEnabled(mVibrateWhenRinging.isChecked());
                }
            }
        }
    }

    private void do_Init() {
        if (mStorageManager == null) {
            mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }
        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setIcon(R.drawable.shortcut_sound);
            }
        }
        mContext = activity = getActivity();
        resolver = getActivity().getContentResolver();
        resolver.registerContentObserver(Uri.parse("content://settings/system"), true, mObserver);

        mSecure = new LockPatternUtils(getActivity()).isSecure();

        addPreferencesFromResource(R.xml.sound_settings);
        mSoundProfileInfo = new SoundProfileInfo(mContext);
        mHapticSettingsObserver = new SettingsObserver(new Handler());

        Log.d(TAG, "mSoundProfileInfo.getUserProfileName() onResume : " + mSoundProfileInfo.getUserProfileName());
        if (mSoundProfileInfo.getUserProfileName().equals("")) {
            mSoundProfileInfo.setDeafultSoundValue_Default();
        }
    }

    private void do_InitRunnableMenu_touch() {
        mTouchSoundsRunnable = new Runnable() {
            public void run() {
                if (true == mTouchSoundsPreference.isChecked()) {
                    UpdateSoundEffect(true);
                } else {
                    UpdateSoundEffect(false);
                }
            }
        };
    }

    private String getFilepathFromContentUri(Uri uri) {
        String filepath = null;
        Cursor c = null;

        try {
            ContentResolver resolver = getContentResolver();
            c = resolver.query(uri, new String[] { MediaStore.Audio.Media.DATA }, null, null, null);

            int count = (c != null) ? c.getCount() : 0;
            if (count != COUNT_ONE) {
                // If there is not exactly one result, throw an appropriate exception.
                if (count == 0) {
                    return null;
                }
            }
            if (c != null) {
                c.moveToFirst();
                int i = c.getColumnIndex("_data");
                filepath = (i >= 0 ? c.getString(i) : null);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (filepath == null) {
            return null;
        }

        Log.i(TAG, "getFilepathFromContentUri Media _data result = " + filepath);
        return filepath;
    }

    /*
        0 : Not DRM
        1 : Rights expired or can't be used as ringtone
        2 : Valid Rights exist for ringtone
    */
    private int checkDRM(String filename, Context context) {
        if (filename == null) {
            return 0;
        }

        int length = filename.length();
        if (false == (filename.regionMatches(true, length - 3, ".dm", 0, 3)
                || filename.regionMatches(true, length - 4, ".dcf", 0, 4)
                || filename.regionMatches(true, length - 4, ".odf", 0, 4)
                || filename.regionMatches(true, length - 4, ".o4a", 0, 4)
                || filename.regionMatches(true, length - 4, ".o4v", 0, 4))) {
            return 0; // Normal file
        }

        int mDrmFile = DrmManager.isDRM(filename);
        if (mDrmFile < Drm.CONTENT_TYPE_DM || mDrmFile > Drm.CONTENT_TYPE_DCFV2) {
            mDrmFile = 0;
            return 0; // Normal file or not wrapped format
        }

        try {
            DrmContentSession session = DrmManager.createContentSession(filename, context);
            if (false == session.isActionSupported(Drm.CONTENT_ACT_RINGTONE)) {
                return 1; // Expired
            }

            return 2;
        } catch (Exception e) {
            Log.w(TAG, "Exception");
        }

        return 1;
    }

    private void checkDrmRingtoneAndNotificationSound() {
        final int[] type_number = { RingtoneManagerEx.TYPE_RINGTONE,
                RingtoneManagerEx.TYPE_NOTIFICATION,
                TYPE_RINGTONE_SIM2,
                TYPE_RINGTONE_SIM3,
                TYPE_NOTIFICATION_SIM2,
                TYPE_NOTIFICATION_SIM3,
                TYPE_RINGTONE_VC };

        Uri ringtoneUri = null;
        Context context = getActivity();
        if (context == null) {
            return;
        }

        // Check DRM ringtone
        for (int i = 0; i < type_number.length; i++) {
            ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(context, type_number[i]);

            if (ringtoneUri != null) {
                if (checkDRM(getFilepathFromContentUri(ringtoneUri), context) == ON) {
                    reSetRingtone(context, type_number[i]);
                }
            }
        }
    }
    private void do_setMenuUI_4_2() {
        RingerVolumePreference ringerVolumePref =
                (RingerVolumePreference)findPreference("ring_volume");
        mBasicCategory.setTitle(R.string.sp_sound_profile_category_title_upgrade_NORMAL);
        touchFeedback_system_category.setTitle(R.string.display_advanced_settings);
        category.setTitle(R.string.sound_settings);

        mVibrateWhenRinging.setSummary(null);
        if (mVibrateVolumePreference != null) {
            mVibrateVolumePreference.setSummary(null);
        }

        if ("VZW".equals(Config.getOperator()) ||
            true == Utils.istargetOperator("DCM")) {
        } else {
            mSoundProfilePreference.setTitle(R.string.sp_sound_profile_title_NORMAL_change);
        }

        category.setOrder(22);
        ringerVolumePref.setOrder(23);
        mAutoCompose.setOrder(24);
        mLGRingtonePreference.setOrder(25);
        mSubRingtonePreference.setOrder(26);
        mThirdRingtonePreference.setOrder(27);
        mSmart_ringtone.setOrder(28);
        mDownloadRingtonesPreference.setOrder(29);
        mColoringPreference.setOrder(31);
        mRingtoyouPreference.setOrder(31);
        mVCRingtonePreference.setOrder(32);

        mVibrateCategory.setOrder(36);
        mVibrateVolumePreference.setOrder(37);
        mVibrateWhenRinging.setOrder(38);
        mIncomingVibration.setOrder(39);
        mSubIncomingVibration.setOrder(40);
        mThirdIncomingVibration.setOrder(41);
        mVibrateTypePreference.setOrder(42);
        mGentleVibration.setOrder(43);
        mNotificationCategory.setOrder(44);
        mZenModePreference.setOrder(45);
        mLockscreen.setOrder(46);
        mAppNotificationPreference.setOrder(47);
        mEmotionLED.setOrder(48);


        touchFeedback_system_category.setOrder(49);
        mMorePreference.setOrder(50);

        if (Utils.supportSplitView(getActivity())) {
            mVibrateVolumePreference.setOrder(33);
            mLGNotificationPreference.setOrder(34);
            mSubNotificationPreference.setOrder(35);
            mThirdNotificationPreference.setOrder(36);
        } else {
            mLGNotificationPreference.setOrder(51);
            mSubNotificationPreference.setOrder(52);
            mThirdNotificationPreference.setOrder(53);
        }

        setPreferenceTitle();
        setVibrateOnTap();
        do_removedList();
    }

    private void setPreferenceTitle() {
        mTouchFeedbackAndSystemPreference
                .setTitle(R.string.sp_sound_menu_sound_effects);
        mTouchFeedbackAndSystemPreference.setSummary(null);
        mTouchFeedbackAndSystemPreference.setOrder(253);

        mSmart_ringtone.setTitle(R.string.sp_sound_menu_noise_detection);
        mSmart_ringtone.setSummary(getString(R.string.sp_sound_menu_summary_smart_ringtone));
        mGentleVibration.setTitle(getString(R.string.sp_sound_menu_gentle_vibration_ex));
        mGentleVibration.setSummary(getString(R.string.sp_sound_menu_summary_gentle_vibration_ex));
    }
    private void do_removedList() {
        // Common UI_4_2 RemoveList
        if (null != mGentleVibration) {
            getPreferenceScreen().removePreference(mGentleVibration);
        }
        if (null != mSmart_ringtone) {
            getPreferenceScreen().removePreference(mSmart_ringtone);
        }
        if (null != mAutoCompose
                && !Config.getFWConfigBool(mContext,
                        com.lge.R.bool.config_opera_ringtone,
                        "com.lge.R.bool.config_opera_ringtone")) {
            getPreferenceScreen().removePreference(mAutoCompose);
        }
        if (null != mMorePreference) {
            getPreferenceScreen().removePreference(mMorePreference);
        }

        // UI_4_2 Tablet RemoveList
        if (Utils.supportSplitView(getActivity())) {
            if (null != mVibrateCategory) {
                getPreferenceScreen().removePreference(mVibrateCategory);
            }
        }

        // Remove MySoundProfile except China Model
        if(!Utils.isChina()) {
            if (null != mMySoundProfile) {
                getPreferenceScreen().removePreference(mMySoundProfile);
            }

        }
    }

    private void removeNotificationMenu() {
        if (null != mLGNotificationPreference) {
            getPreferenceScreen().removePreference(mLGNotificationPreference);
        }

        if (null != mSubNotificationPreference) {
            getPreferenceScreen().removePreference(mSubNotificationPreference);
        }

        if (null != mThirdNotificationPreference) {
            getPreferenceScreen()
                    .removePreference(mThirdNotificationPreference);
        }
    }
    private void do_setMenuUI_4_1_MODEL() {
        if (Utils.isUI_4_1_model(mContext)) {
            //boolean isDualSim = Utils.isMultiSimEnabled();
            //boolean isTripleSim = Utils.isTripleSimEnabled();

            Log.d(TAG, "isUI_4_1_model = true");
            mBasicCategory.setTitle(R.string.sp_sound_profile_category_title_upgrade_NORMAL);
            touchFeedback_system_category.setTitle(R.string.advanced_settings);
            getPreferenceScreen().removePreference(mVibrateCategory);
            getPreferenceScreen().removePreference(mMorePreference);
            getPreferenceScreen().removePreference(mAutoCompose);


            mVibrateWhenRinging.setSummary(null);
            if (mVibrateVolumePreference != null) {
                mVibrateVolumePreference.setSummary(null);
            }

            if ("VZW".equals(Config.getOperator()) ||
                true == Utils.istargetOperator("DCM")) {
            } else {
                mSoundProfilePreference.setTitle(R.string.sp_sound_profile_title_NORMAL_change);
            }

            mSmart_ringtone.setTitle(R.string.sp_sound_menu_noise_detection);
            mSmart_ringtone.setSummary(getString(R.string.sp_sound_menu_summary_smart_ringtone));
            mGentleVibration.setTitle(getString(R.string.sp_sound_menu_gentle_vibration_ex));
            mGentleVibration
                    .setSummary(getString(R.string.sp_sound_menu_summary_gentle_vibration_ex));
            mTouchFeedbackAndSystemPreference.setTitle(R.string.sp_sound_menu_sound_effects);
            mTouchFeedbackAndSystemPreference.setSummary(null);

            mLGNotificationPreference.setOrder(132);
            mSubNotificationPreference.setOrder(133);
            mThirdNotificationPreference.setOrder(134);
            mVibrateWhenRinging.setOrder(135);
            mIncomingVibration.setOrder(136);
            mSubIncomingVibration.setOrder(137);
            mThirdIncomingVibration.setOrder(138);
            mVibrateTypePreference.setOrder(139);

            mTouchFeedbackAndSystemPreference.setOrder(253);
            mSmart_ringtone.setOrder(213);
            mGentleVibration.setOrder(214);
            if (null != mGentleVibration) {
                getPreferenceScreen().removePreference(mGentleVibration);
            }
            if (null != mSmart_ringtone) {
                getPreferenceScreen().removePreference(mSmart_ringtone);
            }
            // Remove UI4_1 MySoundProfile
            if (null != mMySoundProfile) {
                getPreferenceScreen().removePreference(mMySoundProfile);
            }

            // VibrateOnTap
            setVibrateOnTap();
        }
    }

    private class SettingsObserver extends ContentObserver {
        private ContentResolver mResolver;
        private final Uri LOCK_SCREEN_PRIVATE_URI =
                Settings.Secure.getUriFor(Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS);
        private final Uri LOCK_SCREEN_SHOW_URI =
                Settings.Secure.getUriFor(Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS);

        private final Uri ZEN_MODE_URI = Global.getUriFor(Global.ZEN_MODE);
        SettingsObserver(Handler handler) {
            super(handler);
            mResolver = getActivity().getContentResolver();
        }

        void observe() {
            mResolver.registerContentObserver(
                    Settings.System.getUriFor(
                            Settings.System.HAPTIC_FEEDBACK_ENABLED), false, this);
            mResolver.registerContentObserver(LOCK_SCREEN_PRIVATE_URI, false, this);
            mResolver.registerContentObserver(LOCK_SCREEN_SHOW_URI, false, this);
            mResolver.registerContentObserver(ZEN_MODE_URI, false, this);
        }

        void pause() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (null != mVibrateOnTouchPreference) {
                try {
                    mVibrateOnTouchPreference.setChecked(
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
            Log.d(TAG, "onChange ToDo");
	     Log.d(TAG, "onChange ToDo uri : " + uri);
            if (LOCK_SCREEN_PRIVATE_URI.equals(uri) || LOCK_SCREEN_SHOW_URI.equals(uri)) {
                updateLockscreenNotifications();
            }
     	     if (ZEN_MODE_URI.equals(uri)) {
                updateState(true);
     	     }
        }
    }
    private void setVibrateOnTap() {
        if (mVibrateOnTouchPreference == null) {
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator() && Utils.isHapticfeedbackSupport()) {
                Log.d(TAG, "vibrator exist");
                mVibrateOnTouchPreference = new CheckBoxPreference(mContext);
                mVibrateOnTouchPreference.setTitle(R.string.haptic_feedback_enable_title_tap);
                mVibrateOnTouchPreference.setSummary(R.string.vibrate_on_touch_summary_tap_new);
                mVibrateOnTouchPreference
                        .setKey(TouchFeedbackAndSystemPreference.KEY_HAPTIC_FEEDBACK);
                mVibrateOnTouchPreference.setOrder(252);
                mVibrateOnTouchPreference.setChecked(
                        Settings.System.getInt(resolver,
                                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                                ON) == ON ? true : false);
                getPreferenceScreen().addPreference(mVibrateOnTouchPreference);
            }
        }
    }

    // [Sound Settings] SearchProvider

    private void setSearchPerformClick() {
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        mNewValue = getActivity().getIntent()
                .getBooleanExtra("newValue", false);

        Log.d(TAG, "mSearch_result : " + mSearch_result);
        Log.d(TAG, "mNewValue : " + mNewValue);

        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra(
                "perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
    }

    private void startResult() {
        Log.d(TAG, "startResult");
        if (mSearch_result.equals(KEY_SOUND_PROFILE)) {
            mSoundProfilePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_MY_SOUND_PROFILE)) {
            mMySoundProfile.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_RING_VOLUME)) {
            findPreference(KEY_RING_VOLUME).performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_LG_RINGTONE)) {
            mLGRingtonePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SUB_RINGTONE)) {
            mSubRingtonePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_THIRD_RINGTONE)) {
            mThirdRingtonePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_INCOMING_VIBRATION)) {
            mIncomingVibration.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SUB_INCOMING_VIBRATION)) {
            mSubIncomingVibration.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_LG_NOTIFICATION)) {
            mLGNotificationPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SUB_NOTIFICATION_SOUND)) {
            mSubNotificationPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_DOWNLOAD_RINGTONES)) {
            mDownloadRingtonesPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_VC_RINGTONE)) {
            mVCRingtonePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_VIBRATE_VOLUME)) {
            mVibrateVolumePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_VIBRATE)) {
            mVibrateWhenRinging.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_ZEN_MODE)) {
            mZenModePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_LOCK_SCREEN_NOTIFICATIONS)) {
            mLockscreen.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_APP_NOTIFICATIONS)) {
            mAppNotificationPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals("haptic_feedback")) {
            mVibrateOnTouchPreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_TOUCHFEEDBACK_AND_SYSTEM)) {
            mTouchFeedbackAndSystemPreference
                    .performClick(getPreferenceScreen());
        } else if (mSearch_result
                .equals(TouchFeedbackAndSystemPreference.KEY_SOUND_EFFECTS)) {
            mTouchSoundsPreference.performClick(getPreferenceScreen());

        } else if (mSearch_result
                .equals(TouchFeedbackAndSystemPreference.KEY_LOCK_SOUNDS)) {
            mLockSoundPreference.performClick(getPreferenceScreen());
        }
    }

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    private boolean mNewValue;

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            // Search Sound Main
            setSearchIndexData(context, "sound_settings",
                    context.getString(R.string.notification_settings), "main",
                    null, null, "com.android.settings.SOUND_SETTINGS", null,
                    null, 1, null, null, null, 1, 0);

            // Search SoundProfile
            setSearchSoundProfile(context);

            // Search MySoundProfile
            if (Utils.isChina()) {
                setSearchIndexData(
                        context,
                        KEY_MY_SOUND_PROFILE,
                        context.getString(R.string.sp_my_sound_profile_title_NORMAL),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            }

            // Search Volume
            setSearchIndexData(context, KEY_RING_VOLUME,
                    context.getString(R.string.all_volume_title_new),
                    context.getString(R.string.notification_settings), null,
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);

            if (Config.getFWConfigBool(context,
                    com.lge.R.bool.config_opera_ringtone,
                    "com.lge.R.bool.config_opera_ringtone")) {
                // Search AutoComposed
                setSearchIndexData(
                        context,
                        KEY_AUTO_COMPOSE,
                        context.getString(R.string.acr_preference_main),
                        context.getString(R.string.notification_settings),
                        context.getString(R.string.acr_preference_main_summary),
                        null,
                        "com.android.settings.ACR_SETTINGS",
                        null,
                        null,
                        1,
                        "Switch",
                        "System",
                        SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED,
                        1, 0);
            }

            if (!Utils.supportSplitView(context)) {
                // Search Ringtones
                setSearchRingtone(context);

                // Search DOWNLOAD_RINGTONES
                setSearchDownloadRingtone(context);

                // Search KEY_VC_RINGTONE
                setSearchVCRingtone(context);

                // Search KEY_VIBRATE_VOLUME
                setSearchVibrateVolume(context);

                // Search KEY_VIBRATE
                setSearchSoundWithVibrate(context);

                // Search SoundEffect
                setSearchSoundEffect(context);

                // Search KEY_HANDS_FREE_MODE
                setSeartchHandsFree(context);
            }

            // Search KEY_ZEN_MODE
            setSearchZenMode(context);

            // Search KEY_LOCK_SCREEN_NOTIFICATIONS
            setSearchLockScreen(context);

            // Search KEY_APP_NOTIFICATIONS
            setSearchAppNoti(context);

            // Search VibrateOnTap
            setSearchVibrateOnTap(context);

            // Touch sounds ,Screen lock sound
            if (Utils.supportSplitView(context)) {
                setSearchTabletOnly(context);
            }

            // EMOTIONAL_LED
            setSearchEmotionLED(context);

            return mResult;
        }

        private void setSearchEmotionLED(Context context) {
            if (!(context.getResources().getInteger(
                    com.lge.R.integer.config_emotionalLedType) == 0)
                    && (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2)) {

                String mTitle;
                if ("VZW".equals(Config.getOperator())) {
                    mTitle = context.getString(R.string.notification_led_vzw);
                } else {
                    mTitle = context.getString(R.string.notification_led);
                }
                setSearchIndexData(context, KEY_EMOTIONAL_LED_UI_4_2,
                        context.getString(R.string.notification_led),
                        context.getString(R.string.notification_settings),
                        null, null, "com.lge.settings.EMOTIONAL_LED", null,
                        null, 1, "Switch", "System",
                        "lge_notification_light_pulse", 1, 0);
            }

        }

        private void setSearchTabletOnly(Context context) {
            // Touch sounds
            setSearchIndexData(
                    context,
                    TouchFeedbackAndSystemPreference.KEY_SOUND_EFFECTS,
                    context.getString(R.string.sound_effects_enable_title_new),
                    context.getString(R.string.notification_settings),
                    context.getString(R.string.sound_effects_enable_summary_on),
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    "CheckBox", "System",
                    Settings.System.SOUND_EFFECTS_ENABLED, 1, 0);
            // Screen lock sound
            setSearchIndexData(
                    context,
                    TouchFeedbackAndSystemPreference.KEY_LOCK_SOUNDS,
                    context.getString(R.string.lock_sounds_enable_title),
                    context.getString(R.string.notification_settings),
                    context.getString(R.string.lock_sounds_enable_summary_on_new),
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    "CheckBox", "System",
                    Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1, 0);
        }

        private void setSearchSoundProfile(Context context) {
            setSearchIndexData(
                    context,
                    KEY_SOUND_PROFILE,
                    context.getString(R.string.sp_sound_profile_title_NORMAL_change),
                    context.getString(R.string.notification_settings), null,
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
        }

        private void setSeartchHandsFree(Context context) {
            String mTitle;
            String mSummary;
            if (Utils.istargetOperator("KDDI")) {
                mTitle = context.getString(R.string.voice_notifications_title);
                mSummary = context
                        .getString(R.string.hands_free_mode_read_out_call_summary_ex);
            } else if (Utils.istargetOperator("DCM")) {
                mTitle = context.getString(R.string.voice_notifications_title);
                mSummary = context
                        .getString(R.string.hands_free_mode_read_out_readmessage_summary);
            } else {
                mTitle = context
                        .getString(R.string.voice_notifications_title_changed);
                mSummary = context
                        .getString(R.string.hands_free_mode_off_summary_new_change);
            }
            setSearchIndexData(context, KEY_HANDS_FREE_MODE, mTitle,
                    context.getString(R.string.notification_settings),
                    mSummary, null, "com.lge.settings.HANDSFREE_MODE_SETTING",
                    null, null, 1, "Switch", "System",
                    "hands_free_mode_status", 1, 0);
        }

        private void setSearchSoundEffect(Context context) {
            setSearchIndexData(context, KEY_TOUCHFEEDBACK_AND_SYSTEM,
                    context.getString(R.string.sp_sound_menu_sound_effects),
                    context.getString(R.string.notification_settings), null,
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
        }

        private void setSearchVibrateOnTap(Context context) {
            Vibrator vibrator = (Vibrator)context
                    .getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()
                    && Utils.isHapticfeedbackSupport()) {
                setSearchIndexData(
                        context,
                        "haptic_feedback",
                        context.getString(R.string.haptic_feedback_enable_title_tap),
                        context.getString(R.string.notification_settings),
                        context.getString(R.string.vibrate_on_touch_summary_tap_new),
                        null, "com.android.settings.SOUND_SETTINGS", null,
                        null, 1, "CheckBox", "System",
                        Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, 0);
            }
        }

        private void setSearchAppNoti(Context context) {
            setSearchIndexData(context, KEY_APP_NOTIFICATIONS,
                    context.getString(R.string.app_notifications_title),
                    context.getString(R.string.notification_settings),
                    context.getString(R.string.app_notifications_summary),
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
        }

        private void setSearchLockScreen(Context context) {
            setSearchIndexData(
                    context,
                    KEY_LOCK_SCREEN_NOTIFICATIONS,
                    context.getString(R.string.lock_screen_notifications_title),
                    context.getString(R.string.notification_settings), null,
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
        }

        private void setSearchZenMode(Context context) {
            setSearchIndexData(context, KEY_ZEN_MODE,
                    context.getString(R.string.zen_mode_settings_title),
                    context.getString(R.string.notification_settings),
                    context.getString(R.string.zen_mode_settings_summary),
                    null, "com.android.settings.SOUND_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
        }

        private void setSearchSoundWithVibrate(Context context) {
            if (!Utils.isChina()) {
                String mTitle;
                if (true == Utils.istargetOperator("DCM")) {
                    mTitle = context.getString(R.string.vibrate_title);
                } else {
                    mTitle = context
                            .getString(R.string.sp_ringtone_with_vibration_title_NORMAL_new);
                }
                setSearchIndexData(context, KEY_VIBRATE, mTitle,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, "CheckBox", "System",
                        Settings.System.VIBRATE_WHEN_RINGING, 1, 0);
            }
        }

        private void setSearchVibrateVolume(Context context) {
            if ((Utils.getVibrateTypeProperty().equals("1") || Utils
                    .getVibrateTypeProperty().equals("2"))) {
                setSearchIndexData(context, KEY_VIBRATE_VOLUME,
                        context.getString(R.string.vibrate_volume_title_new),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            }
        }

        private void setSearchVCRingtone(Context context) {
            if (("KT").equals(Config.getOperator())
                    && !Utils.isFolderModel(context)) {
                setSearchIndexData(
                        context,
                        KEY_VC_RINGTONE,
                        context.getString(R.string.sp_vc_ringtone_title_NORMAL),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            }
        }

        private void setSearchDownloadRingtone(Context context) {
            if ("TEL".equals(Config.getOperator())) {
                setSearchIndexData(
                        context,
                        KEY_DOWNLOAD_RINGTONES,
                        context.getString(R.string.sp_download_ringtones_NORMAL),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            }
        }

        private void setSearchRingtone(Context context) {
            boolean isDualSim = Utils.isMultiSimEnabled(); // Dual Sim status
                                                           // check
            boolean isTripleSim = Utils.isTripleSimEnabled();
            String sim_name_ringtone1 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_RINGTONE, RENAME_SIM1_INDEX);
            String sim_name_ringtone2 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_RINGTONE, RENAME_SIM2_INDEX);
            String sim_name_ringtone3 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_RINGTONE, RENAME_SIM3_INDEX);

            String sim_name_vibration1 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_VIBRATION, RENAME_SIM1_INDEX);
            String sim_name_vibration2 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_VIBRATION, RENAME_SIM2_INDEX);

            String sim_name_noti1 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_NOTIFICATION, RENAME_SIM1_INDEX);
            String sim_name_noti2 = Utils.do_getSoundSimName(context,
                    RENAME_SIM_TYPE_NOTIFICATION, RENAME_SIM2_INDEX);

            if (isTripleSim == true) { // triple
                setSearchIndexData(context, KEY_LG_RINGTONE,
                        sim_name_ringtone1,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                setSearchIndexData(context, KEY_SUB_RINGTONE,
                        sim_name_ringtone2,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                setSearchIndexData(context, KEY_THIRD_RINGTONE,
                        sim_name_ringtone3,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            } else if (false == isDualSim) { // sigle
                setSearchIndexData(context, KEY_LG_RINGTONE,
                        context.getString(R.string.ringtone_title_ex),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);

                setSearchIndexData(
                        context,
                        KEY_INCOMING_VIBRATION,
                        context.getString(R.string.sp_quiet_mode_vibration_type),
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);

                if ("ATT".equals(Config.getOperator())) {
                    sim_name_noti1 = context
                            .getString(R.string.default_notification_sound);
                } else {
                    sim_name_noti1 = context
                            .getString(R.string.sp_sound_noti_NORMAL);
                }
                setSearchIndexData(context, KEY_LG_NOTIFICATION,
                        sim_name_noti1,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
            } else {
                // Ringtone
                setSearchIndexData(context, KEY_LG_RINGTONE,
                        sim_name_ringtone1,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                setSearchIndexData(context, KEY_SUB_RINGTONE,
                        sim_name_ringtone2,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);

                // Vibrate
                setSearchIndexData(context, KEY_INCOMING_VIBRATION,
                        sim_name_vibration1,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                setSearchIndexData(context, KEY_SUB_INCOMING_VIBRATION,
                        sim_name_vibration2,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);

                // Notification
                setSearchIndexData(context, KEY_LG_NOTIFICATION,
                        sim_name_noti1,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                setSearchIndexData(context, KEY_SUB_NOTIFICATION_SOUND,
                        sim_name_noti2,
                        context.getString(R.string.notification_settings),
                        null, null, "com.android.settings.SOUND_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
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
