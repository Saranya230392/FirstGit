package com.android.settings.lge;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.os.UserHandle;

import com.lge.constants.SettingsConstants;
import com.android.settings.DisplaySettings;
import com.android.settings.DreamBackend;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;

import java.util.List;

public class DisplayMoreSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "DisplayMoreSettings";
    private static final String KEY_KNOCK_ON = "knock_on";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_PLC_MODE = "plc_mode";
    private static final String KEY_SCREEN_MODE = "screen_mode";
    private static final String KEY_MOTION_SENSOR_CALIBRATION = "motion_sensor_calibration";
    private static final String KNOCKON_LOCK_SET = "KNOCKON_LOCK_SET";

    private Preference mScreenOffEffect;
    private CheckBoxPreference mKnockOn;
    private SwitchPreference mScreenSaver;
    private Preference mScreenMode;
    private CheckBoxPreference mPlcMode;
    private DreamBackend mBackend;
    private Preference mMotionSensorCalibration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_more_settings);
  
        setupKnockOn();
        setupScreenSaver();
        setupPlcMode();
        setupScreenMode();
        setupMotionSensor();

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateKnockOn();
        updateScreenSaver();
        updateScreenSaverSummary();
        updatePlcModeOnCheckbox();
        updateScreenModeSummary();
    }

    private void setupKnockOn() {
        mKnockOn = (CheckBoxPreference)findPreference(KEY_KNOCK_ON);
        if ("DCM".equals(Config.getOperator())) {
            mKnockOn.setTitle(R.string.gesture_title_turn_screen_on);
        }
        if (mKnockOn != null && !isSupportKnockOn()) {
            getPreferenceScreen().removePreference(mKnockOn);
        }
    }

    private void updateKnockOn() {
        boolean isKnockOnChecked = Settings.System.getInt(getActivity()
                .getContentResolver(), "gesture_trun_screen_on", 0) == 1 ? true
                : false;
        mKnockOn.setChecked(isKnockOnChecked);
        boolean mScurityKnock = Settings.Secure.getInt(getContentResolver(),
                KNOCKON_LOCK_SET, 0) > 0 ? true : false;
        if (mScurityKnock) {
            mKnockOn
                    .setSummary(R.string.gesture_screen_knock_code_summary);
        } else {
            mKnockOn
                    .setSummary(R.string.gesture_screen_on_off_summary);
        }
    }

    private void setupScreenSaver() {
        mScreenSaver = (SwitchPreference)findPreference(KEY_SCREEN_SAVER);
        mScreenSaver.setOnPreferenceChangeListener(this);
        if (mScreenSaver != null
                && Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_dreamsSupported, 
                           "com.android.internal.R.bool.config_dreamsSupported") == false) {
            getPreferenceScreen().removePreference(mScreenSaver);
        }
        mBackend = new DreamBackend(getActivity());
    }

    private void updateScreenSaver() {
        boolean dreamsEnabled = mBackend.isEnabled();
        if (mScreenSaver.isChecked() != dreamsEnabled) {
            mScreenSaver.setChecked(dreamsEnabled);
        }
    }

    private void setupPlcMode() {
        mPlcMode = (CheckBoxPreference)findPreference(KEY_PLC_MODE);

        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled") == false) {
            getPreferenceScreen()
                    .removePreference(findPreference(KEY_PLC_MODE));
        }
    }

    private void updatePlcModeOnCheckbox() {
        int iPlcMode = Settings.System
                .getInt(getActivity().getContentResolver(),
                        SettingsConstants.System.PLC_MODE_SET, 1);
        mPlcMode.setChecked(iPlcMode != 0);
    }

    private void setupScreenMode() {
        mScreenMode = (Preference)findPreference(KEY_SCREEN_MODE);

        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled") == false) {
            getPreferenceScreen().removePreference(
                    findPreference(KEY_SCREEN_MODE));
        }
    }

    private void updateScreenModeSummary() {
        int modeDbValue = Settings.System.getInt(getActivity()
                .getContentResolver(),
                SettingsConstants.System.SCREEN_MODE_SET, 1);
        String summary;

        if (modeDbValue < 0) {
            summary = "";
        } else {
            final CharSequence[] modeSummaries = getResources().getTextArray(
                    R.array.screen_mode_entries_NORMAL);
            summary = mScreenMode.getContext().getString(
                    R.string.screen_mode_summary, modeSummaries[modeDbValue]);
            mScreenMode.setSummary(summary);

            Log.d("TAG", "mScreen Mode : " + summary);
        }
    }

    private void updateScreenSaverSummary() {
        if (mScreenSaver != null) {
            mScreenSaver.setSummary(getSummaryTextWithDreamName());
        }
    }

    private void setupMotionSensor() {
        mMotionSensorCalibration = (Preference)findPreference(KEY_MOTION_SENSOR_CALIBRATION);
        if (!Utils.isCheckSensor(getActivity())
            || !(UserHandle.myUserId() == UserHandle.USER_OWNER)) {
            getPreferenceScreen().removePreference(mMotionSensorCalibration);
        }
    }

    public CharSequence getSummaryTextWithDreamName() {
        boolean isEnabled = mBackend.isEnabled();
        if (!isEnabled) {
            return getString(R.string.screensaver_settings_summary_off);
        } else {
            return mBackend.getActiveDreamName();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mScreenSaver == preference) {
            mBackend.setEnabled((Boolean)newValue);
            updateScreenSaverSummary();
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mPlcMode) {
            boolean value = mPlcMode.isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    SettingsConstants.System.PLC_MODE_SET, value ? 1 : 0);
            Log.d(TAG,
                    "PLC mode : "
                            + Settings.System.getInt(getActivity()
                                    .getContentResolver(),
                                    SettingsConstants.System.PLC_MODE_SET, 1));
        } else if (preference == mKnockOn) {
            updateKnockOn();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean isSupportKnockOn() {
        if (ConfigHelper.isSupportKnockCode2_0(getActivity())) {
            return true;
        }
        return false;
    }
}
