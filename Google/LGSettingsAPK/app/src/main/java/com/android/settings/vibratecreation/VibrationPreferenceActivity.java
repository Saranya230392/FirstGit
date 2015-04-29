package com.android.settings.vibratecreation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.media.AudioManager;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.VibrateVolumePreference;

public class VibrationPreferenceActivity extends PreferenceActivity {

    //Setting DB
    private static final String GENTLE_VIBRATION_STATUS = "gentle_vibration_status";

    // Key
//    private static final String KEY_GENTLE_VIBRATION = "gentle_vibration";
//    private static final String KEY_VIBRATE_VOLUME = "vibrate_volume";
    private static final String KEY_INCOMING_VIBRATION = "incoming_vibration";
    private static final String KEY_SUB_INCOMING_VIBRATION = "sub_incoming_vibration";
    private static final String KEY_THIRD_INCOMING_VIBRATION = "third_incoming_vibration";
    private static final String KEY_MESSAGE_VIBRATION = "message_vibration";
    private static final String KEY_EMAIL_VIBRATION = "email_vibration";
    private static final String KEY_ALARM_VIBRATION = "alarm_vibration";
    private static final String KEY_CALENDAR_VIBRATION = "calendar_vibration";

    private VibratePatternInfo mVibratePatternInfo;
    private CheckBoxPreference mGentleVibration;
    private VibrateVolumePreference mVibrateVolumePreference;
    private Preference mIncomingVibration;
    private Preference mSubIncomingVibration;
    private Preference mThirdIncomingVibration;
    private Preference mMessageVibration;
    private Preference mEmailVibration;
    private Preference mAlarmVibration;
    private Preference mCalendarVibration;
    private int ringerMode;
    private AudioManager mAudioManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                ringerMode = mAudioManager.getRingerMode();
                updateState(ringerMode);
            }
        }
    };

    private void updateState(int mode) {
        updateUIStatus(true);
    }

    private void updateUIStatus(boolean status) {
        if (mGentleVibration != null) {
            mGentleVibration.setEnabled(status);
        }
        if (mVibrateVolumePreference != null) {
            mVibrateVolumePreference.setEnabled(status);
        }
        if (mIncomingVibration != null) {
            mIncomingVibration.setEnabled(status);
        }
        if (mSubIncomingVibration != null) {
            mSubIncomingVibration.setEnabled(status);
        }

        if (mThirdIncomingVibration != null) {
            mThirdIncomingVibration.setEnabled(status);
        }
        if (mMessageVibration != null) {
            mMessageVibration.setEnabled(status);
        }

        if (mEmailVibration != null) {
            mEmailVibration.setEnabled(status);
        }
        if (mAlarmVibration != null) {
            mAlarmVibration.setEnabled(status);
        }
        if (mCalendarVibration != null) {
            mCalendarVibration.setEnabled(status);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.vibration_settings);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.shortcut_vibrate_type);
        }
        mVibratePatternInfo = new VibratePatternInfo(getApplicationContext(), 0);
        mIncomingVibration = (Preference)findPreference(KEY_INCOMING_VIBRATION);
        mSubIncomingVibration = (Preference)findPreference(KEY_SUB_INCOMING_VIBRATION);
        mThirdIncomingVibration = (Preference)findPreference(KEY_THIRD_INCOMING_VIBRATION);
        mMessageVibration = (Preference)findPreference(KEY_MESSAGE_VIBRATION);
        mEmailVibration = (Preference)findPreference(KEY_EMAIL_VIBRATION);
        mAlarmVibration = (Preference)findPreference(KEY_ALARM_VIBRATION);
        mCalendarVibration = (Preference)findPreference(KEY_CALENDAR_VIBRATION);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (Utils.isSPRModel()) {
            /*
            if (mGentleVibration != null)
                getPreferenceScreen().removePreference(mGentleVibration);
            if (mVibrateVolumePreference != null)
                getPreferenceScreen().removePreference(mVibrateVolumePreference);
                */
            if (!Utils.isVoiceCapable(getApplicationContext())) {
                if (null != mIncomingVibration) {
                    getPreferenceScreen().removePreference(mIncomingVibration);
                }
                if (null != mMessageVibration) {
                    getPreferenceScreen().removePreference(mMessageVibration);
                }
            }
        } else {
            if (null != mEmailVibration) {
                getPreferenceScreen().removePreference(mEmailVibration);
            }
            if (null != mMessageVibration) {
                getPreferenceScreen().removePreference(mMessageVibration);
            }
            if (null != mAlarmVibration) {
                getPreferenceScreen().removePreference(mAlarmVibration);
            }
            if (null != mCalendarVibration) {
                getPreferenceScreen().removePreference(mCalendarVibration);
            }
        }

        boolean isDualSim = Utils.isMultiSimEnabled(); // Dual Sim status check
        boolean isTripleSim = Utils.isTripleSimEnabled();
        if (isTripleSim) {
        }
        else if (false == isDualSim) {
            if (null != mSubIncomingVibration) {
                getPreferenceScreen().removePreference(mSubIncomingVibration);
            }
            if (null != mThirdIncomingVibration) {
                getPreferenceScreen().removePreference(mThirdIncomingVibration);
            }
        }
        else {
            if (mIncomingVibration != null) {
                mIncomingVibration.setTitle(R.string.sp_vibrate_Call_SIM1_NORMAL);
            }
            if (null != mThirdIncomingVibration) {
                getPreferenceScreen().removePreference(mThirdIncomingVibration);
            }
        }
        Intent intent = getIntent();
        Bundle bun = intent.getExtras();

        if (bun.getBoolean("easysetting") == true)
        {
            if (!Utils.isUpgradeModel()) {
                if (Utils.isSPRModel()) {
                }
                else {
                    if (mIncomingVibration != null) {
                        mIncomingVibration.setTitle(R.string.sp_quiet_mode_incoming_call_vibration);
                    }
                }
            }
            if (mGentleVibration != null) {
                mGentleVibration.setIcon(R.drawable.ic_gentle_vibration);
            }
            if (mVibrateVolumePreference != null) {
                mVibrateVolumePreference.setIcon(R.drawable.ic_vibration_strength);
            }
            int res_vibration = R.drawable.shortcut_vibrate_type;
            if (Utils.isUI_4_1_model(this)) {
                res_vibration = R.drawable.ic_vibrate_type_new;
            }
            if (mIncomingVibration != null) {
                mIncomingVibration.setIcon(res_vibration);
            }
            if (isDualSim) {
                if (mSubIncomingVibration != null) {
                    mSubIncomingVibration.setIcon(res_vibration);
                }
            }
            if (isTripleSim) {
                if (mSubIncomingVibration != null) {
                    mSubIncomingVibration.setIcon(res_vibration);
                }
                if (mThirdIncomingVibration != null) {
                    mThirdIncomingVibration.setIcon(res_vibration);
                }
            }
            if (mMessageVibration != null) {
                mMessageVibration.setIcon(res_vibration);
            }
            if (mEmailVibration != null) {
                mEmailVibration.setIcon(res_vibration);
            }
            if (mAlarmVibration != null) {
                mAlarmVibration.setIcon(res_vibration);
            }
            if (mCalendarVibration != null) {
                mCalendarVibration.setIcon(res_vibration);
            }

            if (!(Utils.getVibrateTypeProperty().equals("1") || Utils.getVibrateTypeProperty()
                    .equals("2"))) {
                if (mGentleVibration != null) {
                    getPreferenceScreen().removePreference(mGentleVibration);
                }
                if (mVibrateVolumePreference != null) {
                    getPreferenceScreen().removePreference(mVibrateVolumePreference);
                }
            }
        } else {
            if (mGentleVibration != null) {
                getPreferenceScreen().removePreference(mGentleVibration);
            }
            if (mVibrateVolumePreference != null) {
                getPreferenceScreen().removePreference(mVibrateVolumePreference);
            }
        }
        //remove
        /*
        if ("FX1".equals(SystemProperties.get("ro.product.board"))) {

        }
        else {
            getPreferenceScreen().removePreference(mGentleVibration);
        }
        */
        if (Utils.isUpgradeModel() || Utils.isVeeModel()) {
            if ((mGentleVibration != null) || (getPreferenceScreen() != null)) {
                getPreferenceScreen().removePreference(mGentleVibration);
            }
        }
        //getPreferenceScreen().removePreference(mVibrateVolumePreference);

        String hapticfeedback = SystemProperties.get("ro.device.hapticfeedback", "1");
        if (hapticfeedback.equals("0")) {
            if (mVibrateVolumePreference != null) {
                getPreferenceScreen().removePreference(mVibrateVolumePreference);
            }
        }
    }

    private void updateUI() {
        if (null != mIncomingVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM1));
            mIncomingVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM1));
        }

        if (null != mSubIncomingVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM2));
            mSubIncomingVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM2));
        }
        if (null != mThirdIncomingVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.INCOMING_CALL_SIM3,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.INCOMING_CALL_SIM3));
            mThirdIncomingVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.INCOMING_CALL_SIM3));
        }

        if (null != mGentleVibration) {
            if (null != mGentleVibration) {
                mGentleVibration.setChecked(Settings.System.getInt(getContentResolver(),
                        GENTLE_VIBRATION_STATUS, 1) != 0);
            }
        }

        if (null != mMessageVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.MESSAGE,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.MESSAGE));
            mMessageVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.MESSAGE));
        }
        if (null != mEmailVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.EMAIL,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.EMAIL));
            mEmailVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.EMAIL));
        }
        if (null != mAlarmVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.ALARM,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.ALARM));
            mAlarmVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.ALARM));
        }
        if (null != mCalendarVibration) {
            mVibratePatternInfo.checkedLGVibrateName(VibratePatternInfo.CALENDAR,
                    mVibratePatternInfo.getDBVibratePattern(VibratePatternInfo.CALENDAR));
            mCalendarVibration.setSummary(mVibratePatternInfo
                    .getDBVibrateName(VibratePatternInfo.CALENDAR));
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        IntentFilter filter = new IntentFilter(
                AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        updateUI();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        if (preference.equals(mGentleVibration)) {
            setGentleVibration(mGentleVibration.isChecked() == true ? 1 : 0);
            return true;
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
        } else if (preference.equals(mThirdIncomingVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.INCOMING_CALL_SIM3);
            startActivity(i);
        } else if (preference.equals(this.mMessageVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.MESSAGE);
            startActivity(i);
        }
        else if (preference.equals(this.mEmailVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.EMAIL);
            startActivity(i);
        } else if (preference.equals(this.mAlarmVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.ALARM);
            startActivity(i);
        }
        else if (preference.equals(this.mCalendarVibration)) {
            Intent i = new Intent();
            i.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibratePicker");
            i.putExtra(VibratePatternInfo.PARENT_TYPE, VibratePatternInfo.CALENDAR);
            startActivity(i);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isGentleVibration() {
        return getDBGentlevibration() == 1 ? true : false;
    }

    private int getDBGentlevibration() {
        return Settings.System.getInt(getContentResolver(), GENTLE_VIBRATION_STATUS, 1);
    }

    private void setGentleVibration(int check) {
        Settings.System.putInt(getContentResolver(), GENTLE_VIBRATION_STATUS, check);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
