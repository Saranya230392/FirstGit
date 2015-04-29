/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import java.util.Locale;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.graphics.drawable.Drawable;
import java.io.BufferedReader;
import java.io.FileReader;
import com.android.settings.lge.NightModeInfo;
import com.android.settings.lgesetting.Config.Config;
//LGE_CHANGE_S Thermal configuration
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.android.settings.Utils;
//LGE_CHANGE_E Thermal configuration
//LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import android.content.Intent;
import android.content.IntentFilter;
//LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
import com.lge.constants.SettingsConstants;
import com.lge.systemservice.core.LGPowerManagerHelper;
import com.lge.systemservice.core.LGContext;
import android.os.SystemProperties;

public class BrightnessPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener, OnClickListener {
    private static final String TAG = "BrightnessPreference";

    private final int mScreenBrightnessMinimum;
    private final int mScreenBrightnessMaximum;

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;
    private CheckBox mNightCheckBox;

    private int mOldBrightness;
    private int mOldAutomatic;
    private int mOldNightCheck;
    private int mOldNightEnabled;

    private int mNightmode;
    private boolean mAutomaticAvailable;
    private boolean mAutomaticMode;

    private int mCurBrightness = -1;

    private boolean mRestoredOldState;
    private TextView mSubtitle;
    //LGE_CHANGE_S Thermal configuration
    private Context mContext;
    private Toast mToast;
    //LGE_CHANGE_E Thermal configuration
    private boolean isBinding = false;

    private int seekBarRange = 10000;
    private TextView mSubTitleTV;
    private TextView mSubTitleNight;
    private CharSequence mSubTitleText;
    private int mSubTitleGravity;

    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private int mThermalMaxBrightness;
    //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
    private int mBatteryTemperature = 0;
    //private static final int WARNING_TEMPERATURE = 480;
    static final int WARNING_BRIGHTNESS_VALUE = 196;
    //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
    LGContext mServiceContext;
    private NightModeInfo mNightModeInfo;

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mCurBrightness = -1;
            onBrightnessChanged();
            selectsetSubtitle(false);
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };

    // LGE_CHANGE_S [sunghee.won@lge.com]  TD:QA 14760 - settings > sound > audible selection
    public void onClick(View arg0) {
    };

    // LGE_CHANGE_E [sunghee.won@lge.com]  TD:QA 14760 - settings > sound > audible selection
    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //LGE_CHANGE_S Thermal configuration
        mContext = context;
        //LGE_CHANGE_E Thermal configuration

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
        mScreenBrightnessMaximum = pm.getMaximumScreenBrightnessSetting();
        mThermalMaxBrightness = pm.getMaximumScreenBrightnessSetting();
        seekBarRange = mScreenBrightnessMaximum - mScreenBrightnessMinimum;

        setLayoutResource(R.layout.preference_double_title);

        /*TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconPreferenceScreen, 0, 0);*/

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference_double_title, null);
        mSubTitleTV = (TextView)view.findViewById(R.id.subtitle);

        mAutomaticAvailable = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");
        Log.d("hong", "AutoCheck: " + mAutomaticAvailable);
        Log.d("hong", "MAX: " + mScreenBrightnessMaximum);
        Log.d("hong", "MIN: " + mScreenBrightnessMinimum);

        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        //setDialogIcon(R.drawable.ic_settings_display);
        //LGE_CHANGE_S Thermal configuration
        getThermalBrightMax();
        //LGE_CHANGE_E Thermal configuration
        selectsetSubtitle(false);
        mNightModeInfo = new NightModeInfo(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);
        //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario

        Log.d(TAG, "showDialog()");
        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);
        mRestoredOldState = false;

        setBrightnessTitle(getPercent());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Log.d(TAG, "onBindView()");
        mSubTitleTV = (TextView)view.findViewById(R.id.subtitle);
        mSubTitleTV.setText(mSubTitleText);
        mSubTitleTV.setGravity(mSubTitleGravity);
    }

    public void setSubTitle(CharSequence text) {
        Log.d(TAG, "setSubTitle()");
        if (Utils.isUpgradeModel()) {
            return;
        } else {
            mSubTitleText = text;

            if (mSubTitleTV != null) {
                mSubTitleTV.setText(mSubTitleText);
                mSubTitleTV.invalidate();
            }
        }
    }

    public void setSummary(boolean force) {
        Log.d(TAG, "setSummary()");
        CharSequence summary = null;
        int checked;

        //if (Utils.isUpgradeModel()) {
        //    return;
        //}

        CharSequence str = getBrightnessPercent(force, true);
        checked = getBrightnessMode(0);

        if (mAutomaticAvailable) {
            if (checked == 1) {
                if (noapplyMultiALC()) {
                    summary = getContext().getString(R.string.automatic_brightness_ex);
                } else {
                    summary = getContext().getString(R.string.automatic_brightness_ex) + ", " + str;
                }
            } else {
                summary = str;
            }
        } else {
            NightModeInfo mNightModeInfoSummary = new NightModeInfo(getContext());
            if (mNightModeInfoSummary.isNightModeAble()) {
                summary = getContext().getString(R.string.night_brightness_title) + ", " + "\u200E"
                        + str;
            } else {
                summary = str;
            }
        }
        this.setSummary(summary);
    }

    public void setSubTitle(int resid) {
        Log.d(TAG, "setSubTitle()");
        //if (Utils.isUpgradeModel()) {
        //    return;
        //} else {
        mSubTitleText = getContext().getResources().getText(resid, "");

        if (mSubTitleTV != null) {
            mSubTitleTV.setText(mSubTitleText);
            mSubTitleTV.invalidate();
        }
        //}
    }

    public void setSubTitleGravity(int gravity) {
        Log.d(TAG, "setSubTitleGravity()");
        mSubTitleGravity = gravity;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Log.d(TAG, "onBindDialogView()");
        isBinding = true;

        mSeekBar = (SeekBar)view.findViewById(R.id.seekbar);
        mSeekBar.setMax(seekBarRange);
        mOldBrightness = getBrightness();
        Log.d("hong", "mOldBrightness: " + mOldBrightness);

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        // LGE_CHANGE_S [sunghee.won@lge.com]  TD:QA 14760 - settings > sound > audible selection
        mCheckBox.setOnClickListener(this);
        // LGE_CHANGE_E [sunghee.won@lge.com]  TD:QA 14760 - settings > sound > audible selection
        mNightCheckBox = (CheckBox)view.findViewById(R.id.night_mode);
        mNightCheckBox.setOnClickListener(this);
        mSubtitle = (TextView)view.findViewById(R.id.subtitle);
        mSubTitleNight = (TextView)view.findViewById(R.id.subtitle_night);
        //LGE_CHANGE_S Thermal configuration

        if (mOldBrightness > (mThermalMaxBrightness - mScreenBrightnessMinimum)) {
            mSeekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessMinimum);
        } else {
            mSeekBar.setProgress(mOldBrightness);
        }
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                mSeekBar.getProgress() + mScreenBrightnessMinimum);

        //LGE_CHANGE_E Thermal configuration

        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            mOldAutomatic = getBrightnessMode(0);
            mAutomaticMode = mOldAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            mCheckBox.setChecked(mAutomaticMode);

            if (mCheckBox.isChecked()) {
                if (Config.VZW.equals(Config.getOperator())
                        || Config.ATT.equals(Config.getOperator())) {
                    mSubtitle.setText(R.string.display_brightness_vzw_summary);
                }
                else {
                    mSubtitle.setText(R.string.sp_brightness_auto_checked);
                }
            }
            else {
                if (Config.VZW.equals(Config.getOperator())
                        || Config.ATT.equals(Config.getOperator())) {
                    mSubtitle.setText(R.string.sp_brightness_auto_unchecked_summary_NORMAL);
                }
                else {
                    mSubtitle.setText(R.string.sp_brightness_auto_checked);
                }
            }
            mNightCheckBox.setVisibility(View.GONE);
            mSubTitleNight.setVisibility(View.GONE);
        } else {
            if (mNightModeInfo.getNightCheckDB() == 0 || !mNightModeInfo.isNightModeAble()) {
                mNightModeInfo.saveTempOldBrightness(mOldBrightness + mScreenBrightnessMinimum);
                mNightModeInfo.saveOldBrightness(mOldBrightness + mScreenBrightnessMinimum);
            }
            mSeekBar.setEnabled(true);
            mCheckBox.setVisibility(View.GONE);
            mSubtitle.setVisibility(View.GONE);
            mSubTitleTV.setVisibility(View.GONE);
            mNightmode = mNightModeInfo.getNightCheckDB();
            mOldNightCheck = mNightmode;
            mOldNightEnabled = mNightModeInfo.getNightModeEnabled();
            mNightCheckBox.setChecked(mNightmode == 1);
            mNightCheckBox.setOnCheckedChangeListener(this);

            boolean is24 = DateFormat.is24HourFormat(mContext);
            String text = Utils.getResources().getString(is24 ?
                    R.string.brightness_night_mode_24 : R.string.brightness_night_mode_12);
            mSubTitleNight.setText(text);
        }

        mSeekBar.setOnSeekBarChangeListener(this);
        if (noapplyMultiALC())
        {

            //mSubtitle.setText(R.string.brightness_summary);
            if (mCheckBox.isChecked())
            {
                mSeekBar.setEnabled(false);
            }
            else
            {
                mSeekBar.setEnabled(true);
            }
        }

        isBinding = false;

    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        getThermalBrightMax();
        Log.d(TAG, "onProgressChanged() : progress + mScreenBrightnessDim = "
                + ((int)progress + mScreenBrightnessMinimum));
        Log.d(TAG, "onProgressChanged() : mThermalMaxBrightness = " + mThermalMaxBrightness);
        if (progress > mThermalMaxBrightness - mScreenBrightnessMinimum) {
            //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
            if (Config.VZW.equals(Config.getOperator())) {
                try {
                    mServiceContext = new LGContext(mContext);
                    LGPowerManagerHelper pmh = (LGPowerManagerHelper)mServiceContext
                            .getLGSystemService(LGContext.LGPOWERMANAGER_HELPER_SERVICE);
                    if (pmh != null) {
                        Log.d("hong", "turnOffThermald");
                        pmh.turnOffThermald();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                Log.d("hong", "Toast popup");
                showToast(R.string.sp_brightness_over_max_2_NORMAL);
                setBrightness(progress, false);
                if (!mAutomaticAvailable) {
                    if (fromTouch && mNightModeInfo.isNightModeAble()) {
                        mNightModeInfo.setUserBrightnessChange(1);
                    } else if (fromTouch && !mNightModeInfo.isNightModeAble()) {
                        mNightModeInfo.setUserBrightnessChangeNoNight(1);
                    }
                }
            } else {
                seekBar.setProgress(mThermalMaxBrightness - mScreenBrightnessMinimum);
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        mSeekBar.getProgress() + mScreenBrightnessMinimum);
                setBrightness(progress, false);
                showToast(R.string.sp_brightness_over_max_NORMAL);
                if (!mAutomaticAvailable) {
                    if (fromTouch && mNightModeInfo.isNightModeAble()) {
                        mNightModeInfo.setUserBrightnessChange(1);
                    } else if (fromTouch && !mNightModeInfo.isNightModeAble()) {
                        mNightModeInfo.setUserBrightnessChangeNoNight(1);
                    }
                }
                //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
            }
        } else {
            setBrightness(progress, false);
            if (!mAutomaticAvailable) {
                if (fromTouch && mNightModeInfo.isNightModeAble()) {
                    mNightModeInfo.setUserBrightnessChange(1);
                } else if (fromTouch && !mNightModeInfo.isNightModeAble()) {
                    mNightModeInfo.setUserBrightnessChangeNoNight(1);
                }
            }
        }
        setBrightnessTitle(getPercent());

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
        //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
        Log.d(TAG, "onStopTrackingTouch()");
        /*    if (Config.VZW.equals(Config.getOperator())) {
            int progress = seekBar.getProgress();
                int bValue = progress + mScreenBrightnessMinimum;
                if (mBatteryTemperature >= WARNING_TEMPERATURE && bValue > WARNING_BRIGHTNESS_VALUE) {
                    showToast(R.string.sp_brightness_over_max_2_NORMAL);
                }
            }
        */
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                seekBar.getProgress() + mScreenBrightnessMinimum);
        //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mCheckBox) {
            setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        mSeekBar.setProgress(getBrightness());
        if (mAutomaticAvailable) {
            if (mCheckBox.isChecked()) {
                if (Config.VZW.equals(Config.getOperator())
                        || Config.ATT.equals(Config.getOperator())) {
                    mSubtitle.setText(R.string.display_brightness_vzw_summary);
                }
                else {
                    mSubtitle.setText(R.string.sp_brightness_auto_checked);
                }
            }
            else {
                if (Config.VZW.equals(Config.getOperator())
                        || Config.ATT.equals(Config.getOperator())) {
                    mSubtitle.setText(R.string.sp_brightness_auto_unchecked_summary_NORMAL);
                }
                else {
                    mSubtitle.setText(R.string.sp_brightness_auto_checked);
                }
            }
        } else {
            if (mNightCheckBox.isChecked()) {
                mNightmode = 1;
            } else {
                mNightmode = 0;
            }
            mNightModeInfo.preViewNightModeAble(mNightCheckBox.isChecked(), mSeekBar,
                    mOldBrightness);
        }
        if (noapplyMultiALC())
        {

            //mSubtitle.setText(R.string.brightness_summary);
            if (mCheckBox.isChecked())
            {
                mSeekBar.setEnabled(false);
            }
            else
            {
                mSeekBar.setEnabled(true);
            }
        }
        setBrightness(mSeekBar.getProgress(), false);
        if (!isBinding) {
            setBrightnessTitle(getPercent());
        }
    }

    private int getBrightness() {
        //int mode = getBrightnessMode(0);
        float brightness = 0;
        if (mCurBrightness < 0) {
            brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
            Log.d("hong", "getBrightness() : " + brightness);
        } else {
            brightness = mCurBrightness;
        }
        brightness = (brightness - mScreenBrightnessMinimum);
        if (brightness < 0) {
            brightness = 0;
        }

        //}
        return (int)(brightness);
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
            Log.w(TAG, "SettingNotFoundException");
        }
        Log.d(TAG, "getBrightnessMode(): brightnessMode = " + brightnessMode);
        return brightnessMode;
    }

    private void onBrightnessChanged() {
        int brightness = getBrightness();
        mSeekBar.setProgress(getBrightness());
        Log.d(TAG, "onBrightnessChanged() : brightness = " + brightness);
    }

    private void onBrightnessModeChanged() {
        boolean checked = getBrightnessMode(0)
                == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        mCheckBox.setChecked(checked);
        Log.d(TAG, "onBrightnessModeChanged() : checked = " + checked);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        final ContentResolver resolver = getContext().getContentResolver();
        //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
        mContext.unregisterReceiver(mIntentReceiver);
        //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
        resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mBrightnessModeObserver);

        if (positiveResult) {
            setBrightness(mSeekBar.getProgress(), true);
            Settings.System.putInt(resolver,
                    SettingsConstants.System.SCREEN_BRIGHTNESS_CUSTOM,
                    mSeekBar.getProgress() + mScreenBrightnessMinimum);
            int mode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            Settings.System.putInt(getContext().getContentResolver(),
                    SettingsConstants.System.SCREEN_BRIGHTNESS_MODE_CUSTOM, mode
                    );
            Settings.System.putInt(getContext().getContentResolver(),
                    SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS, 1
                    );
            selectsetSubtitle(false);

            if (!mAutomaticAvailable) {
                mNightModeInfo.setNightDB(mNightmode);
                if (mNightModeInfo.getNightCheckDB() == 1) {
                    mNightModeInfo.requestPendingIntent(mContext);
                } else {
                    mNightModeInfo.setAlreadyNightModeDB(0);
                }
                if (!mNightModeInfo.isNightModeAble()) {
                    mNightModeInfo.setUserBrightnessChangeNoNight(1);
                }
            }

        } else {
            restoreOldState();
            selectsetSubtitle(false);
        }

    }

    private void restoreOldState() {
        if (mRestoredOldState) {
            return;
        }
        if (mAutomaticAvailable) {
            setMode(mOldAutomatic);
        } else {
            mNightModeInfo.setNightDB(mOldNightCheck);
            mNightModeInfo.setNightModeEnabled(mOldNightEnabled);
        }
        setBrightness(mOldBrightness, false);
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, mOldBrightness + mScreenBrightnessMinimum);
        mRestoredOldState = true;
        mCurBrightness = -1;
    }

    private void setBrightness(int brightness, boolean write) {
        if (mAutomaticMode) {
            Log.d("hong", "auto brightness : " + brightness);
            float valf = (((float)brightness * 2) / seekBarRange) - 1.0f;
            Log.d("hong", "auto brightness(valf) : " + valf);
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                if (power != null) {
                    power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf);
                }
                if (write) {
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putFloat(resolver,
                            Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
                }
            } catch (RemoteException doe) {
                Log.w(TAG, "RemoteException");
            }
        } else {
            //int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
            Log.d("hong", "brightness : " + brightness);
            brightness = brightness + mScreenBrightnessMinimum;
            Log.d("hong", "brightness2 : " + brightness);
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                if (power != null) {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness);
                }
                if (write) {
                    mCurBrightness = -1;
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                    Log.d("hong", "setBrightness() : " + brightness);
                } else {
                    mCurBrightness = brightness;
                }
            } catch (RemoteException doe) {
                Log.w(TAG, "RemoteException");
            }
        }
    }

    private void setMode(int mode) {
        mAutomaticMode = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }
        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.automatic = mCheckBox.isChecked();
        myState.progress = mSeekBar.getProgress();
        myState.oldAutomatic = mOldAutomatic == 1;
        myState.oldProgress = mOldBrightness;
        myState.curBrightness = mCurBrightness;

        // Restore the old state when the activity or dialog is being paused
        restoreOldState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOldBrightness = myState.oldProgress;
        mOldAutomatic = myState.oldAutomatic ? 1 : 0;
        setMode(myState.automatic ? 1 : 0);
        setBrightness(myState.progress, false);
        mCurBrightness = myState.curBrightness;
    }

    private static class SavedState extends BaseSavedState {

        boolean automatic;
        boolean oldAutomatic;
        int progress;
        int oldProgress;
        int curBrightness;

        public SavedState(Parcel source) {
            super(source);
            automatic = source.readInt() == 1;
            progress = source.readInt();
            oldAutomatic = source.readInt() == 1;
            oldProgress = source.readInt();
            curBrightness = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(automatic ? 1 : 0);
            dest.writeInt(progress);
            dest.writeInt(oldAutomatic ? 1 : 0);
            dest.writeInt(oldProgress);
            dest.writeInt(curBrightness);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public void stopDialog() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    //LGE_CHANGE_S Thermal configuration
    private void getThermalBrightMax()
    {
        String flag_read_path = new String("/sys/class/leds/lcd-backlight/max_brightness");
        String frag = "";
        BufferedReader inFlagFile = null;

        try
        {
            inFlagFile = new BufferedReader(new FileReader(flag_read_path));
            frag = inFlagFile.readLine();
            Log.d(TAG, "getThermalBrightMax() : thermal max bright = " + frag);
            inFlagFile.close();
            mThermalMaxBrightness = Integer.parseInt(frag);
        } catch (Exception e)
        {
            Log.d(TAG, "getThermalBrightMax() : thermal max bright read fail" + frag);
            mThermalMaxBrightness = mScreenBrightnessMaximum;
        } finally
        {
            try {
                if (inFlagFile != null) {
                    inFlagFile.close();
                }
            } catch (Exception e) { /* return value; */
                Log.w(TAG, "Exception");
            }
        }
    }

    private void setBrightnessTitle(int percent) {
        String title = getContext().getString(R.string.brightness);
        //if (!mCheckBox.isChecked()) {
        //lavanya added
        if (Utils.isRTLLanguage()) {
            title = title + " (" + String.format(Locale.getDefault(), "%d", percent) + "%)";
        } else if (Utils.isEnglishDigitRTLLanguage()) {
            title = title + " (" + "\u200E" + percent + "%)";
        } else {
            title = title + " (" + percent + "%)";
        }
        //lavanya added
        //}
        if (getDialog() != null && getDialog().getWindow() != null)
        {
            TextView tv = (TextView)getDialog().getWindow().findViewById(
                    com.android.internal.R.id.alertTitle);
            if (tv != null)
            {
                tv.setText(title);
                if (noapplyMultiALC() && mCheckBox.isChecked())
                {
                    tv.setText(R.string.brightness);
                }
                tv.invalidate();
            }
        }
        Log.d(TAG, "setBrightnessTitle() : title = " + title);
    }

    private int getPercent() {
        Log.d(TAG, "mSeekBar.getProgress() : " + mSeekBar.getProgress());
        Log.d(TAG, "getPercent() : " + (mSeekBar.getProgress() * 100) / (seekBarRange) + "%");

        if (((mSeekBar.getProgress() * 100) / (seekBarRange) == 0) && (mSeekBar.getProgress() != 0)) {
            return 1;
        } else {
            return (mSeekBar.getProgress() * 100) / (seekBarRange);
        }
    }

    public CharSequence getBrightnessPercent(boolean force, boolean summary) {
        int brightness = getBrightness();
        int percent = 0;

        if (force) {
            if (((brightness * 100) / (seekBarRange) == 0) && (brightness != 0)) {
                percent = 1;
            } else {
                percent = (brightness * 100) / (seekBarRange);
            }
        } else {
            if (brightness > (mThermalMaxBrightness - mScreenBrightnessMinimum)) {
                percent = (mThermalMaxBrightness - mScreenBrightnessMinimum) * 100 / (seekBarRange);
                Log.d("hong", "getPercent()_thermal : " + percent);
            } else {
                if (((brightness * 100) / (seekBarRange) == 0) && (brightness != 0)) {
                    percent = 1;
                } else {
                    percent = (brightness * 100) / (seekBarRange);
                }
                Log.d("hong", "getPercent()_not_thermal : " + percent);
            }
        }

        CharSequence str;
        if (summary) {
            if (Utils.isRTLLanguage()) {
                str = String.format(Locale.getDefault(), "%d", percent) + "\u200F" + "%";
            } else {
                str = percent + "%";
                if (Utils.isEnglishDigitRTLLanguage()) {
                    str = "\u200E" + (percent + "%");
                }
            }
        } else {
            if (Utils.isRTLLanguage()) {
                str = "(%" + String.format(Locale.getDefault(), "%d", percent) + ")";
            } else {
                str = "(" + percent + "%)";
            }
        }
        Log.d(TAG, "getBrightnessPercent() : str = " + str);
        return str;
    }

    private void showToast(int aStrID)
    {
        if (mContext == null) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(mContext, aStrID, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(aStrID);
        }
        mToast.show();
    }

    //whether to apply MutiALC
    public boolean noapplyMultiALC() {
        if ("DCM".equals(Config.getOperator())
                || ("TMO".equals(Config.getOperator()) && "US".equals(Config.getCountry()))) {
            return true;
        }
        return false;
    }

    public void updateThermalMAXBrightness() {
        getThermalBrightMax();
        selectsetSubtitle(false);
    }

    public void selectsetSubtitle(boolean force)
    {
        //if (Utils.isVeeModel()) {
        //    Log.d(TAG, "selectsetSubtitle() : vee model device");
        //    setSubTitle(getBrightnessPercent(force, false));
        //} else {
        //    Log.d(TAG, "selectsetSubtitle() : No vee model device");
        setSummary(force);
        //}
    }

    //LGE_CHANGE_S [VZW Only] Brightness / Heat sensor POP_UP scenario
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 100);
            Log.d("hong", "getBatteryTemp(): " + mBatteryTemperature);
        }
    };
    //LGE_CHANGE_E [VZW Only] Brightness / Heat sensor POP_UP scenario
}
