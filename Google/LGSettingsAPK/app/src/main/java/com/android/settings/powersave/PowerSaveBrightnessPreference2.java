package com.android.settings.powersave;

import com.android.internal.widget.DialogTitle;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.Settings; // yonguk.kim 20120610 Preview restore fix
import com.android.settings.Utils;

public class PowerSaveBrightnessPreference2 extends PowerSaveCheckBoxPreference {

    private int MINIMUM_BACKLIGHT; //= android.os.Power.BRIGHTNESS_DIM;// + 10;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;
    private static final String TAG = "PowerSaveBrightnessPreference2";
    private static final int CHECK_AUTO_BRIGHTNESS = 1;
    private static final int DEFAULT_AUTO_BRIGHTNESS = -1;
    private int mClickedIndex;
    private ImageView mSettingsButton;
    private ImageButtonListener mButtonClickListener = new ImageButtonListener();
    private int mCurrentBrightness = 0;
    private View mView;

    //[jongwon007.kim_2013_10_28] Remove Multi ALC(DCM TMO)
    private static int sBRIGHTNESS_MODE_AUTOMATIC;

    private class ImageButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            initPreview();
            showDialog(null);
        }
    }

    public PowerSaveBrightnessPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);
        MINIMUM_BACKLIGHT = Config.getFWConfigInteger(context, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim");
        Log.d(TAG, "MINIMUM_BACKLIGHT : " + MINIMUM_BACKLIGHT);
    }

    public PowerSaveBrightnessPreference2(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        mSettingsButton = (ImageView)view.findViewById(R.id.inputmethod_settings);
        mSettingsButton.setOnClickListener(null);
        mSettingsButton.setOnClickListener(mButtonClickListener);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        View view = (View)getDialog().getWindow().findViewById(
                com.android.internal.R.id.titleDivider);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.dialog_title_secondlines, null);

        TextView dt = (TextView)layout.findViewById(R.id.title);
        mView = (View)layout.findViewById(R.id.view);
        dt.setText(getContext().getString(R.string.sp_power_saver_select_brightness_NORMAL));

        if (Utils.isUI_4_1_model(getContext())) {
            dt.setBackgroundResource(R.color.double_title_background);
            mView.setVisibility(View.GONE);
        }

        mClickedIndex = findIndexOfValue(getValue()); // yonguk.kim 20120611 variable initialization

        TextView tv = (TextView)layout.findViewById(R.id.subtitle);
        tv.setText(getContext().getString(R.string.sp_power_save_brightness_dialog_text_NORMAL));
        builder.setCustomTitle(layout);
        builder.setSingleChoiceItems(getEntries(), findIndexOfValue(getValue()),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedIndex = which;
                        int activated = Settings.System.getInt(getContext().getContentResolver(),
                                SettingsConstants.System.POWER_SAVE_STARTED, -1);
                        if (activated != PowerSave.NOTIFICATION_ACTIVATED) {
                            setPreviewBrightness(which);
                        }
                    }
                });
        builder.setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sBRIGHTNESS_MODE_AUTOMATIC == 1 && noapplyMultiALC()) {
                    Settings.System.putInt(getContext().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            CHECK_AUTO_BRIGHTNESS);
                }
            }
        });
        builder.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PowerSaveBrightnessPreference2.this
                        .onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                if (sBRIGHTNESS_MODE_AUTOMATIC == 1 && noapplyMultiALC()) {
                    Settings.System.putInt(getContext().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            CHECK_AUTO_BRIGHTNESS);
                }
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedIndex >= 0) {
            String value = getEntryValues()[mClickedIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
        if (!isActivated()) {
            mCurrentBrightness = Settings.System.getInt(getContext().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE, -1);
            Log.d(TAG, "onDialogClosed, restore_brightness=" + mCurrentBrightness);
            if (mCurrentBrightness > 0) {
                PowerSave.setBrightness(mCurrentBrightness);
                Settings.System.putInt(
                        getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        mCurrentBrightness);
            }
        }
        if (sBRIGHTNESS_MODE_AUTOMATIC == 1 && noapplyMultiALC()) {
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, CHECK_AUTO_BRIGHTNESS);
        }
    }

    public void restorePreview() {
        if (!isActivated() && getDialog() != null && getDialog().isShowing()) {
            mCurrentBrightness = Settings.System.getInt(getContext().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE, -1);
            Log.d(TAG, "restorePreview, restore_brightness=" + mCurrentBrightness);
            if (mCurrentBrightness > 0) {
                PowerSave.setBrightness(mCurrentBrightness);
            }
        }
    }

    private void initPreview() {
        if (!isActivated()) {
            // yonguk.kim 20120610 Preview restore fix
            //            mCurrentBrightness = PowerSave.getCurrentBrightness();
            if (noapplyMultiALC()) {
                sBRIGHTNESS_MODE_AUTOMATIC = Settings.System.getInt
                        (getContext().getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE, DEFAULT_AUTO_BRIGHTNESS);
                Log.d(TAG, "Check sBRIGHTNESS_MODE_AUTOMATIC " +
                        sBRIGHTNESS_MODE_AUTOMATIC);
            }
            mCurrentBrightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, MAXIMUM_BACKLIGHT);
            Settings.System.putInt(getContext().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BRIGHTNESS_RESTORE, mCurrentBrightness);
            Log.d(TAG, "initPreview, mCurrentBrightness=" + mCurrentBrightness);
        }
    }

    private boolean isActivated() {
        int activated = Settings.System.getInt(getContext().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_STARTED, -1);
        return (activated == PowerSave.NOTIFICATION_ACTIVATED);
    }

    private void setPreviewBrightness(int which) {
        if (sBRIGHTNESS_MODE_AUTOMATIC == 1 && noapplyMultiALC()) {
            Log.d(TAG, "setPreviewBrightness");
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
        }
        int percent = Integer.parseInt(getEntryValues()[mClickedIndex].toString());
        int brightness = PowerSave.getValue(percent);
        Log.d(TAG, "percen=" + percent + " brightness=" + brightness);
        PowerSave.setBrightness(brightness + MINIMUM_BACKLIGHT);
    }

    public boolean noapplyMultiALC() {
        if ("DCM".equals(Config.getOperator())
                || ("TMO".equals(Config.getOperator()) && "US".equals(Config.getCountry()))) {
            return true;
        }
        return false;
    }
}
