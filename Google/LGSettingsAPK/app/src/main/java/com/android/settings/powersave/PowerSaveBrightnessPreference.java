package com.android.settings.powersave;

import android.preference.SeekBarDialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;

import com.lge.constants.SettingsConstants;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;

public class PowerSaveBrightnessPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;

    private boolean isBinding = false;

    private boolean mAutomaticAvailable;

    // ALC option
    private int mAutomatic;
    private int mOldAutomatic;
    //private int mALCOption;
    //private int mOldALCOption;
    //private RadioGroup mRadioGroup;
    //private RadioButton mRadioButtonPowerSaving;
    //private RadioButton mRadioButtonOptimized;
    //private LinearLayout mLayout1;
    //private LinearLayout mLayout2;

    private int mBrightnessPercent;

    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private static int MINIMUM_BACKLIGHT/* = android.os.Power.BRIGHTNESS_DIM + 10*/;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;

    public PowerSaveBrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAutomaticAvailable = Config.getFWConfigBool(context, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available");

        MINIMUM_BACKLIGHT = Config.getFWConfigInteger(context, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim") + 10;

        //setDialogLayoutResource(R.layout.powersave_preference_dialog_brightness);
        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        setDialogIcon(R.drawable.ic_settings_display);
    }

    private void init() {
        mBrightnessPercent = Settings.System.getInt(getContext().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_BRIGHTNESS, PowerSave.DEFAULT_BRIGHTNESS);
        mOldAutomatic = Settings.System.getInt(getContext().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_BRIGHTNESS_MODE, PowerSave.defBrightnessMode);
        //"power_save_brightness_mode", PowerSave.defBrightnessMode);
        mAutomatic = mOldAutomatic;
        /*mOldALCOption = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.POWER_SAVE_BRIGHTNESS_AUTOMATIC_OPTION, PowerSave.defBrightnessAutomaticOption);
                //"power_save_brightness_automatic_option", PowerSave.defBrightnessAutomaticOption);
        mALCOption = mOldALCOption;*/
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        init();

        isBinding = true;

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);

        mSeekBar.setProgress(getValue(mBrightnessPercent));

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);

        /* ALC option
        mRadioGroup = (RadioGroup)view.findViewById(R.id.automodegroup);
        mRadioButtonOptimized = (RadioButton)view.findViewById(R.id.radiobuttonoptimized);
        mRadioButtonPowerSaving  = (RadioButton)view.findViewById(R.id.radiobuttonpowersaving);
        mLayout1 = (LinearLayout)view.findViewById(R.id.layout1);
        mLayout2 = (LinearLayout)view.findViewById(R.id.layout2);
        mLayout1.setOnClickListener(SelectProtocolListener);
        mLayout2.setOnClickListener(SelectProtocolListener);
        mRadioButtonOptimized.setOnClickListener(SelectProtocolListener);
        mRadioButtonPowerSaving.setOnClickListener(SelectProtocolListener);
        */

        if (mAutomaticAvailable) {
            //if (true) {
            mCheckBox.setOnCheckedChangeListener(this);

            mCheckBox.setChecked(mAutomatic != 0);

            /* ALC option
            if(mALCOption == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC_POWERSAVING){
            //if(mALCOption == 0){
                mRadioButtonOptimized.setChecked(false);
                mRadioButtonPowerSaving.setChecked(true);
            }
            else {
                mRadioButtonOptimized.setChecked(true);
                mRadioButtonPowerSaving.setChecked(false);
            }

            if(mAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                mRadioGroup.setVisibility(View.GONE);
            }
            */

        }
        else {
            mCheckBox.setVisibility(View.GONE);

            /* ALC option
            mRadioGroup.setVisibility(View.GONE);
            */
        }
        mSeekBar.setOnSeekBarChangeListener(this);

        isBinding = false;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mBrightnessPercent = getPercent(mSeekBar.getProgress());
            Settings.System.putInt(getContext().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BRIGHTNESS, mBrightnessPercent);

            if (mAutomaticAvailable) {
                Settings.System.putInt(getContext().getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_BRIGHTNESS_MODE, mAutomatic);
                //"power_save_brightness_mode", mAutomatic);
                /* ALC option
                Settings.System.putInt(getContext().getContentResolver(),
                        Settings.System.POWER_SAVE_BRIGHTNESS_AUTOMATIC_OPTION, mALCOption);
                        //"power_save_brightness_automatic_option", mALCOption);
                */
            }

            callChangeListener(null);
        } else {
            if (mAutomaticAvailable) {
                setMode(mOldAutomatic);
            }
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        setBrightnessTitle(mBrightnessPercent);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {

        // Display the changed percentage
        setBrightnessTitle(getPercent(progress));
    }

    private void setBrightnessTitle(int percent) {
        String title = getContext().getString(R.string.sp_power_saver_select_brightness_NORMAL);
        //if(!mCheckBox.isChecked()) {
        title = title + " (" + percent + "%)";
        //}
        TextView tv = (TextView)getDialog().getWindow().findViewById(
                com.android.internal.R.id.alertTitle);
        tv.setText(title);
        tv.invalidate();
    }

    private int getValue(int percent) {
        return (MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT) * percent / 100;
    }

    private int getPercent(int value) {
        return (value * 100) / (MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        if (!isBinding) {
            setBrightnessTitle(getPercent(mSeekBar.getProgress()));
        }
    }

    private void setMode(int mode) {
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            //mSeekBar.setVisibility(View.GONE);
            /* ALC option
            mRadioGroup.setVisibility(View.VISIBLE);
            */
        } else {
            //mSeekBar.setVisibility(View.VISIBLE);
            /* ALC option
            mRadioGroup.setVisibility(View.GONE);
            */
        }
        mAutomatic = mode;
    }

    /* ALC option
    OnClickListener SelectProtocolListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.layout1:
                case R.id.radiobuttonoptimized:
                    mRadioButtonOptimized.setChecked(true);
                    mRadioButtonPowerSaving.setChecked(false);
                    mALCOption = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC_OPTIMIZED;    // 0:power saving, 1:optimized brightness
                    //mALCOption = 1;    // 0:power saving, 1:optimized brightness
                    break;
                case R.id.layout2:
                case R.id.radiobuttonpowersaving:
                    mRadioButtonOptimized.setChecked(false);
                    mRadioButtonPowerSaving.setChecked(true);
                    mALCOption = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC_POWERSAVING;
                    //mALCOption = 0;
                    break;
                default :
                    break;
             }
         }
    };
    */
}
