package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.vibratecreation.VibratePatternInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.content.ComponentName;

import com.lge.os.Build;

public class TouchFeedbackAndSystemPreference extends
        SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    public static final String KEY_DTMF_TONE = "dtmf_tone";
    public static final String KEY_SOUND_EFFECTS = "sound_effects";
    public static final String KEY_LOCK_SOUNDS = "lock_sounds";
    public static final String KEY_ROAMING_SOUND = "Eri_sounds";
    public static final String KEY_EMERGENCY_TONE = "emergency_tone";
    public static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    public static final String KEY_MPCS_POWERUPTONE = "mpcs_power_up_tone";
    public static final String KEY_AND_TOUCH_SOUND = "key_and_touch_sound";
    public static final String KEY_FOLDER_SOUND = "folder_sound";
    public static final String KEY_COVER_SOUNDS = "cover_sounds";

    private CheckBoxPreference mDtmfTone;
    private CheckBoxPreference mSoundEffects;
    private CheckBoxPreference mLockSounds;
    private CheckBoxPreference mRoamingSound;
    private CheckBoxPreference mHapticFeedback;
    private ListPreference emergencyTonePreference;
    private Preference mMPCSPowerUpTonePreference;
    private CheckBoxPreference mCoverSounds;

    private CheckBoxPreference mKeyAndToudSound;
    private CheckBoxPreference mFolderSound;

    private static final int MSG_UPDATE_MPCSPOWERUPTONE_SUMMARY = 9;
    private static final int MSG_UPDATE_SOUND_EFFECT = 5;
    private static final int FALLBACK_EMERGENCY_TONE_VALUE = 0;

    private AudioManager mAudioManager;
    private long mStandbyTime = 0;
    private boolean mbIsSoundPoolLoaded = true;
    private Runnable mTouchSoundsRunnable;
    private Context mContext;
    private int ringerMode;
    private ContentResolver mContentResolver;

    private SettingsObserver mHapticSettingsObserver;

    private static final String[] NEED_VOICE_CAPABILITY_TOUCH = {
            KEY_DTMF_TONE,
            KEY_EMERGENCY_TONE
    };

    private static final String[] NOT_FOLDER_MODEL_LIST = {
            KEY_DTMF_TONE,
            KEY_SOUND_EFFECTS,
            KEY_LOCK_SOUNDS,
            KEY_ROAMING_SOUND,
            KEY_EMERGENCY_TONE,
            KEY_HAPTIC_FEEDBACK,
            KEY_MPCS_POWERUPTONE            
    };

    private static final String[] FOLDER_MODEL_LIST = {
            KEY_AND_TOUCH_SOUND,
            KEY_FOLDER_SOUND
    };    

    private static final String DISABLE = "0";
    private static final String ENABLE = "1";

    private static final int[] MPCS_RING_RESID = {
            R.string.ringtone_silent,
            R.string.sp_powertone_mpcs1,
            R.string.sp_powertone_mpcs2,
            R.string.sp_powertone_mpcs3
    };

    /**
    * enable : "1", disable : "0"
    * mUIStatus[x][0] 0 : Silent mode status
    * mUIStatus[x][1] 1 : Vibrate mode status
    * mUIStatus[x][2] 2 : Sound mode status
    * mUIStatus[x][3] 3 : Preference key name
    *
    * call method : do_ShowMenuCheck_touch()
    **/

    private static final String[][] UISTATUS = { { DISABLE, DISABLE, ENABLE, KEY_DTMF_TONE },
            { DISABLE, DISABLE, ENABLE, KEY_SOUND_EFFECTS },
            { DISABLE, DISABLE, ENABLE, KEY_LOCK_SOUNDS },
            { DISABLE, ENABLE, ENABLE, KEY_ROAMING_SOUND },
            { DISABLE, DISABLE, ENABLE, KEY_EMERGENCY_TONE },
            { ENABLE, ENABLE, ENABLE, KEY_HAPTIC_FEEDBACK },
            { DISABLE, DISABLE, ENABLE, KEY_MPCS_POWERUPTONE }
    };

    private final static String TAG = "TouchFeedbackAndSystemPreference";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_MPCSPOWERUPTONE_SUMMARY:
                mMPCSPowerUpTonePreference.setSummary(getPowerToneSummary());
                break;
            case MSG_UPDATE_SOUND_EFFECT:
                Log.v(TAG, "Handler()MSG_UPDATE_SOUND_EFFECT");
                UpdateSoundEffect((boolean)(msg.obj).equals(true));
                break;
            default:
                break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                updateAllPreferences();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        do_Init_touch();
        do_InitPreferenceMenu_touch();
        do_InitDefaultSetting_touch();
        do_InitOperatorDependancyMenu_touch();
        do_InitFuctionalMenu_touch();
        do_InitRunnableMenu_touch();
        if (Utils.supportSplitView(getActivity())) {
            do_DeleteMenuForWifiModel();
        } else {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setIcon(R.drawable.img_sound_touch_feedback_system);
                if (Utils.isUI_4_1_model(mContext)) {
                    actionBar.setTitle(R.string.sp_sound_menu_sound_effects);
                    getActivity().setTitle(R.string.sp_sound_menu_sound_effects);
                } else {
                    actionBar.setTitle(R.string.sp_sound_category_feedback_title_NORMAL);
                    getActivity().setTitle(R.string.sp_sound_category_feedback_title_NORMAL);
                }
            }
        }
        mIsFirst = true;
    }

    protected void do_DeleteMenuForWifiModel() {
        if (mDtmfTone != null) {
            getPreferenceScreen().removePreference(mDtmfTone);
        }
        if (mRoamingSound != null) {
            getPreferenceScreen().removePreference(mRoamingSound);
        }
        if (mMPCSPowerUpTonePreference != null) {
            getPreferenceScreen().removePreference(mMPCSPowerUpTonePreference);
        }
        if (emergencyTonePreference != null) {
            getPreferenceScreen().removePreference(emergencyTonePreference);
        }

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mContentResolver = getContentResolver();
        if (mContentResolver != null) {
            mContentResolver.registerContentObserver(Uri.parse("content://settings/system"), true,
                    mObserver);
        }
        mHapticSettingsObserver.observe();
        updateAllPreferences();
        IntentFilter filter1 = new IntentFilter(
                AudioManager.RINGER_MODE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter1);
        setSearchPerformClick();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mHapticSettingsObserver.pause();
        getContentResolver().unregisterContentObserver(mObserver);
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        if (preference == mDtmfTone) {
            Settings.System.putInt(getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING,
                    mDtmfTone.isChecked() ? 1 : 0);
        } else if (preference == mSoundEffects) {
            if (mSoundEffects.isChecked()) {
                mbIsSoundPoolLoaded = true;
            } else {
                mbIsSoundPoolLoaded = false;
            }
            new Thread(mTouchSoundsRunnable).start();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SOUND_EFFECTS_ENABLED, mSoundEffects.isChecked() ? 1 : 0);
        } else if (preference == mLockSounds) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
                    mLockSounds.isChecked() ? 1 : 0);
        } else if (preference == mCoverSounds) {
            Settings.System.putInt(getContentResolver(), "cover_sounds",
                    mCoverSounds.isChecked() ? 1 : 0);
        } else if (preference == mHapticFeedback) {
            Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    mHapticFeedback.isChecked() ? 1 : 0);
        } else if (preference == mRoamingSound) {
            Settings.System.putInt(getContentResolver(), "lg_eri_set",
                    mRoamingSound.isChecked() ? 1 : 0);
        } else if (preference == mMPCSPowerUpTonePreference) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.MPCSPowerTonePicker"));
            startActivity(intent);
        } else if (preference == mKeyAndToudSound) {
            Settings.System.putInt(getContentResolver(), "physical_key_and_touch_sound",
                    mKeyAndToudSound.isChecked() ? 1 : 0);
        } else if (preference == mFolderSound) {
            Settings.System.putInt(getContentResolver(), "screen_lock_and_folder_sound",
                    mFolderSound.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_EMERGENCY_TONE.equals(key)) {
            try {
                int value = Integer.parseInt((String)objValue);
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.EMERGENCY_TONE, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist emergency tone setting", e);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    synchronized private void UpdateSoundEffect(boolean isSoundEffect) {
        if (true == isSoundEffect) {
            mAudioManager.loadSoundEffects();
            Log.v(TAG, "UpdateSoundEffect()load!!");
        } else {
            long kStandbyTime = 2000;
            mStandbyTime = SystemClock.uptimeMillis() + kStandbyTime;
            while (!mbIsSoundPoolLoaded)
            {
                if (SystemClock.uptimeMillis() > mStandbyTime)
                {
                    mAudioManager.unloadSoundEffects();
                    Log.v(TAG, "UpdateSoundEffect()unload!!");
                    break;
                }

                try {
                    Thread.sleep((long)50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "UpdateSoundEffect()end");
        }
    }

    private void do_InitPreferenceMenu_touch() {
        mDtmfTone = (CheckBoxPreference)findPreference(KEY_DTMF_TONE);
        mSoundEffects = (CheckBoxPreference)findPreference(KEY_SOUND_EFFECTS);
        mHapticFeedback = (CheckBoxPreference)findPreference(KEY_HAPTIC_FEEDBACK);
        if (mHapticFeedback != null) {
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator() && Utils.isHapticfeedbackSupport()) {
            } else {
                getPreferenceScreen().removePreference(mHapticFeedback);
            }
        }
        mLockSounds = (CheckBoxPreference)findPreference(KEY_LOCK_SOUNDS);
        mRoamingSound = (CheckBoxPreference)findPreference(KEY_ROAMING_SOUND);
        mMPCSPowerUpTonePreference = findPreference(KEY_MPCS_POWERUPTONE);
        emergencyTonePreference = (ListPreference)findPreference(KEY_EMERGENCY_TONE);
        mCoverSounds = (CheckBoxPreference)findPreference(KEY_COVER_SOUNDS);
        if (!ConfigHelper.isSupportSlideCover(mContext)) {
            getPreferenceScreen().removePreference(mCoverSounds);
        }

        mKeyAndToudSound = (CheckBoxPreference)findPreference(KEY_AND_TOUCH_SOUND);
        mFolderSound = (CheckBoxPreference)findPreference(KEY_FOLDER_SOUND);

        if (!(Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2)
                && Config.getOperator().equals("VZW")) {
            mDtmfTone
                    .setSummary(getString(R.string.dtmf_tone_enable_summary_on));
            mLockSounds
                    .setSummary(getString(R.string.lock_sounds_enable_summary_on));

        }
    }

    private void do_InitRunnableMenu_touch() {
        mTouchSoundsRunnable = new Runnable() {
            public void run() {
                if (true == mSoundEffects.isChecked()) {
                    UpdateSoundEffect(true);
                }
                else {
                    UpdateSoundEffect(false);
                }
            }
        };
    }

    private void do_InitFuctionalMenu_touch() {
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        ContentResolver resolver = getActivity().getContentResolver();

        if (("VZW").equals(Config.getOperator()) ||
                (TelephonyManager.PHONE_TYPE_CDMA == activePhoneType)) {
            emergencyTonePreference.setValue(String.valueOf(Settings.Global
                    .getInt(resolver, Settings.Global.EMERGENCY_TONE,
                            FALLBACK_EMERGENCY_TONE_VALUE)));
            emergencyTonePreference.setOnPreferenceChangeListener(this);
        }
        else if ((TelephonyManager.PHONE_TYPE_CDMA != activePhoneType
        && emergencyTonePreference != null)) {
            getPreferenceScreen().removePreference(emergencyTonePreference);
        }

        if (!Utils.isVoiceCapable(getActivity())) {
            for (String prefKey : NEED_VOICE_CAPABILITY_TOUCH) {
                Preference pref = findPreference(prefKey);
                if (pref != null) {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        }

        if (Utils.isFolderModel(getActivity())) {
            for (String prefKey : NOT_FOLDER_MODEL_LIST) {
                Preference pref = findPreference(prefKey);
                if (pref != null) {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        } else {
              for (String prefKey : FOLDER_MODEL_LIST) {
                Preference pref = findPreference(prefKey);
                if (pref != null) {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        }
    }

    private void updateAllPreferences() {
        if (null != mDtmfTone) {
            mDtmfTone.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0);
        }

        if (null != mSoundEffects) {
            mSoundEffects.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.SOUND_EFFECTS_ENABLED, 0) != 0);
        }
        if (null != mHapticFeedback) {
            mHapticFeedback.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0);
        }
        if ("VZW".equals(Config.getOperator())) {
            mLockSounds.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 0) != 0);
        } else {
            mLockSounds.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0);
        }

        if (null != mCoverSounds) {
            mCoverSounds.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                "cover_sounds", 1) != 0);
        }

        if (null != mRoamingSound) {
            mRoamingSound.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    "lg_eri_set", 0) != 0);
        }

        if ("MPCS".equals(Config.getOperator()) == true &&
                null != mMPCSPowerUpTonePreference) {
            mMPCSPowerUpTonePreference.setSummary(
                    getString(MPCS_RING_RESID[Utils.getPowerTonePosition()]));
        }

        if (null != mKeyAndToudSound) {
            mKeyAndToudSound.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    "physical_key_and_touch_sound", 0) != 0);
        }

        if (null != mFolderSound) {
            mFolderSound.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    "screen_lock_and_folder_sound", 0) != 0);
        }        

        ringerMode = mAudioManager.getRingerMode();

    }

    private void do_InitDefaultSetting_touch() {

        ContentResolver resolver = getActivity().getContentResolver();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null || !vibrator.hasVibrator() || Utils.isUI_4_1_model(mContext)) {
            if (null != findPreference(KEY_HAPTIC_FEEDBACK)) {
                getPreferenceScreen().removePreference(findPreference(KEY_HAPTIC_FEEDBACK));
            }
        }

        mDtmfTone.setPersistent(false);
        mDtmfTone.setChecked(Settings.System.getInt(resolver,
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0);

        mSoundEffects.setPersistent(false);
        mSoundEffects.setChecked(Settings.System.getInt(resolver,
                Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0);

        mHapticFeedback.setPersistent(false);
        mHapticFeedback.setChecked(Settings.System.getInt(resolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0);

        mLockSounds.setPersistent(false);
        mLockSounds.setChecked(Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0);

        mCoverSounds.setPersistent(false);
        mCoverSounds.setChecked(Settings.System.getInt(resolver,
                "cover_sounds", 1) != 0);

        mRoamingSound.setPersistent(false);
        mRoamingSound.setChecked(Settings.System.getInt(resolver, "lg_eri_set", 0) != 0);

        mKeyAndToudSound.setPersistent(false);
        mKeyAndToudSound.setChecked(Settings.System.getInt(resolver, "physical_key_and_touch_sound", 0) != 0);

        mFolderSound.setPersistent(false);
        mFolderSound.setChecked(Settings.System.getInt(resolver, "screen_lock_and_folder_sound", 0) != 0);        
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //to do
            do_updateRingtoneName_touch();
        }
    };

    private class SettingsObserver extends ContentObserver {
        private ContentResolver resolver;

        SettingsObserver(Handler handler) {
            super(handler);
            resolver = getActivity().getContentResolver();
        }

        void observe() {
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED), false, this);
        }

        void pause() {
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            do_updateRingtoneName_touch();

            if (null != mHapticFeedback) {
                try {
                    mHapticFeedback.setChecked(Settings.System.getInt(
                            mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            1) == 1 ? true : false);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void do_Init_touch() {
        mContext = getActivity().getApplicationContext();
        addPreferencesFromResource(R.xml.touch_feedback_and_system_settings);

        mHapticSettingsObserver = new SettingsObserver(new Handler());
    }

    private void do_InitOperatorDependancyMenu_touch() {
        if (!"VZW".equals(Config.getOperator()) && mRoamingSound != null) {
            getPreferenceScreen().removePreference(mRoamingSound);
        }
        if (null != mMPCSPowerUpTonePreference) {
            getPreferenceScreen().removePreference(mMPCSPowerUpTonePreference);
            mMPCSPowerUpTonePreference = null;
        }
    }

    private void do_updateRingtoneName_touch() {
        if (mMPCSPowerUpTonePreference != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_MPCSPOWERUPTONE_SUMMARY,
                    getPowerToneSummary()));
        }
    }

    private void do_ShowMenuCheck_touch() {
        final int KEY = 3;
        int preference_length = UISTATUS.length;
        for (int i = 0; i < preference_length; i++) {
            if (null != findPreference(UISTATUS[i][KEY])) {
                findPreference(UISTATUS[i][KEY]).setEnabled(
                        ENABLE.equals(UISTATUS[i][ringerMode]) ? true : false);
            }
        }
    }

    private String getPowerToneSummary() {
        try {
            int numberPowertone = Utils.getPowerTonePosition();
            switch (numberPowertone) {
            case 0:
                return mContext.getResources().getString(R.string.ringtone_silent);
            case 1:
                return mContext.getResources().getString(R.string.sp_powertone_mpcs1);
            case 2:
                return mContext.getResources().getString(R.string.sp_powertone_mpcs2);
            case 3:
                return mContext.getResources().getString(R.string.sp_powertone_mpcs3);
            default:
                return mContext.getResources().getString(R.string.ringtone_silent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mContext.getResources().getString(R.string.ringtone_silent);
    }

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    private boolean mNewValue;

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
        if (mSearch_result.equals(KEY_DTMF_TONE)) {
            mDtmfTone.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_ROAMING_SOUND)) {
            mRoamingSound.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_EMERGENCY_TONE)) {
            emergencyTonePreference.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SOUND_EFFECTS)) {
            mSoundEffects.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_LOCK_SOUNDS)) {
            mLockSounds.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_AND_TOUCH_SOUND)) {
            mKeyAndToudSound.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_FOLDER_SOUND)) {
            mFolderSound.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_COVER_SOUNDS)) {
            mCoverSounds.performClick(getPreferenceScreen());
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            // Category Title
            String mHelpCategory = context
                    .getString(R.string.notification_settings)
                    + " > "
                    + context.getString(R.string.sp_sound_menu_sound_effects);
            if (!Utils.supportSplitView(context)) {
                // Dialpad touch sounds
                setSearchIndexData(
                        context,
                        KEY_DTMF_TONE,
                        context.getString(R.string.dtmf_tone_enable_title_new),
                        mHelpCategory,
                        context.getString(R.string.dtmf_tone_enable_summary_on_new),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        Settings.System.DTMF_TONE_WHEN_DIALING, 1, 0);

                // Sound when roaming
                if ("VZW".equals(Config.getOperator())) {
                    setSearchIndexData(
                            context,
                            KEY_ROAMING_SOUND,
                            context.getString(R.string.sp_Sound_when_roaming_NORMAL),
                            mHelpCategory,
                            null,
                            null,
                            null,
                            "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                            "com.android.settings", 1, "CheckBox", "System",
                            "lg_eri_set", 1, 0);
                }

                // Emergency tone
                int activePhoneType = TelephonyManager.getDefault()
                        .getCurrentPhoneType();
                if (("VZW").equals(Config.getOperator())
                        || (TelephonyManager.PHONE_TYPE_CDMA == activePhoneType)) {
                    setSearchIndexData(
                            context,
                            KEY_EMERGENCY_TONE,
                            context.getString(R.string.emergency_tone_title),
                            mHelpCategory,
                            null,
                            null,
                            null,
                            "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                            "com.android.settings", 1, null, null, null, 1, 0);
                }

                // Touch sound
                setSearchIndexData(
                        context,
                        KEY_SOUND_EFFECTS,
                        context.getString(R.string.sound_effects_enable_title_new),
                        mHelpCategory,
                        context.getString(R.string.sound_effects_enable_summary_on),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        Settings.System.SOUND_EFFECTS_ENABLED, 1, 0);

                // Screen lock sound
                setSearchIndexData(
                        context,
                        KEY_LOCK_SOUNDS,
                        context.getString(R.string.lock_sounds_enable_title),
                        mHelpCategory,
                        context.getString(R.string.lock_sounds_enable_summary_on_new),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1, 0);
            }
            // Key and touch sound, Folder sound
            if (Utils.isFolderModel(context)) {
                setSearchIndexData(
                        context,
                        KEY_AND_TOUCH_SOUND,
                        context.getString(R.string.key_and_touch_sound_title),
                        mHelpCategory,
                        context.getString(R.string.key_and_touch_sound_summary),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        "physical_key_and_touch_sound", 1, 0);

                setSearchIndexData(
                        context,
                        KEY_FOLDER_SOUND,
                        context.getString(R.string.folder_sound_title),
                        mHelpCategory,
                        context.getString(R.string.folder_sound_summary),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        "screen_lock_and_folder_sound", 1, 0);
            }

            if (ConfigHelper.isSupportSlideCover(context)) {
                setSearchIndexData(
                        context,
                        KEY_COVER_SOUNDS,
                        context.getString(R.string.sound_effects_cover_sound_title),
                        mHelpCategory,
                        context.getString(R.string.sound_effects_cover_sound_summary),
                        null,
                        null,
                        "com.android.settings.Settings$TouchFeedbackAndSystemPreferenceActivity",
                        "com.android.settings", 1, "CheckBox", "System",
                        "cover_sounds", 1, 0);
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
