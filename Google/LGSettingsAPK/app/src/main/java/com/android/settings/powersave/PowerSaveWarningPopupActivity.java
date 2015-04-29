package com.android.settings.powersave;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.lge.constants.SettingsConstants;

import android.os.SystemProperties;
/*LGSI_CHANGE_S: Arabic Number Conversion
2011-04-14,anuj.singhaniya@lge.com,
Numbers are changed according to the current language*/
import java.util.Locale;
/*LGSI_CHANGE_E: Arabic Number Conversion*/
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;

public class PowerSaveWarningPopupActivity extends AlertActivity
        implements DialogInterface.OnClickListener {

    private static final String TAG = "PowerSaveWarningPopupActivity";
    private boolean mDoPowerSave = false;

    private View mView;
    private TextView mTextView;
    private ImageView mBatteryImg;

    private TextView mTextViewSub;

    private boolean checked = false;
    private boolean noActivation = false;
    private boolean pressHomeKey = false;

    private ContentResolver mContentResolver;
    private PowerSave mPowerSave;

    private static final String POWER_SAVE_START = "com.lge.settings.POWER_SAVER_START";

    public static final String POWER_SAVE_EMOTIONAL_LED = "power_save_emotional_led";
    public static final String POWER_SAVE_AUTO_SCREEN_TONE = "power_save_auto_screen_tone";

    private static final String KEY_POWER_SAVE_QUICK_CASE = "power_save_quick_case";
    private BroadcastReceiver mPowerSaveReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // jongtak0920.kim 121029 Receive Intent from System UI [START]
            if (POWER_SAVE_START.equals(intent.getAction())) {
                int value = intent.getIntExtra("start", 0);
                Log.d(TAG, "POWER_SAVE_START:" + value);
                Settings.System.putInt(context.getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_ENABLED, value);

                //                int backup_value = Settings.System.getInt(context.getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
                //                Settings.System.putInt(context.getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE, 100 );

                finishDialog(value > 0);
            }
            // jongtak0920.kim 121029 Receive Intent from System UI [END]

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);

                int battery = level * 100 / scale;
                int plugType = intent.getIntExtra("plugged", 0);

                int powerSaveModeValue = getPowerSaveModeValue(context);

                mTextView.setVisibility(View.VISIBLE);
                /*LGSI_CHANGE_S: Arabic Number Conversion
                  2011-04-14,anuj.singhaniya@lge.com,
                  Numbers are changed according to the current language*/
                if (Utils.isRTLLanguage()) {
                    mTextView.setText(String.format(
                            Utils.getResources().getString(
                                    R.string.sp_power_save_warning_battery_level_NORMAL),
                            String.format(Locale.getDefault(), "%d", battery) + "%"));
                } else if (Utils.isEnglishDigitRTLLanguage()) {
                    mTextView.setText(String.format(
                            Utils.getResources().getString(
                                    R.string.sp_power_save_warning_battery_level_NORMAL),
                            "\u200E" + String.valueOf(battery) + "%"));
                } else {
                    mTextView.setText(String.format(
                            getString(R.string.sp_power_save_warning_battery_level_NORMAL),
                            String.valueOf(battery) + "%"));
                }
                /*LGSI_CHANGE_E: Arabic Number Conversion*/
                //mBatteryImg.setImageResource(R.drawable.powersave_battery);
                mBatteryImg.setImageResource(mPowerSave.getPopupBatteryImgId(battery));

                if (PowerSaveSettings.DEBUG == false) {
                    if (battery > powerSaveModeValue) {
                        noActivation = true;
                        finish();
                    }
                    else if (plugType > 0) {
                        noActivation = true;
                        finish();
                    }
                }

            }
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {

            }
        }
    };

    private void finishDialog(boolean enabled) {
        if (!enabled) {
            dismiss();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNotiPannel();
        mContentResolver = getContentResolver();
        mPowerSave = new PowerSave(this);

        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = Utils.getResources().getString(R.string.sp_power_saver_on_NORMAL);

        TypedValue out = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.alertDialogIcon, out, true);
        //p.mIconId = out.resourceId;//android.R.drawable.ic_dialog_alert;

        p.mView = createView();
        p.mPositiveButtonText = Utils.getResources().getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        int mode_value = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
        if (mode_value == PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY) {
            p.mNegativeButtonText = Utils.getResources().getString(R.string.dlg_cancel);
        } else {
            p.mNegativeButtonText = Utils.getResources().getString(
                    R.string.sp_power_save_later_NORMAL);
        }
        p.mNegativeButtonListener = this;
        //p.mOnCancelListener = this;
        //mAlert.mDialogInterface.setOnDismissListener(this);
        setupAlert();
    }

    private void checkNotiPannel() {
        StatusBarManager statusBarService = (StatusBarManager)this
                .getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarService.collapsePanels();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register receiver for intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(POWER_SAVE_START);
        registerReceiver(mPowerSaveReceiver, filter);

        pressHomeKey = false;

    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mPowerSaveReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (noActivation) {
            // pass
        }
        else if (!checked) {
            //            mDoPowerSave = false;
            //            sendDoPowerSave();
            //            checked = true;
            //            finish();
        }
        if (pressHomeKey) {
            mDoPowerSave = false;
            sendDoPowerSave();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        if (!checked) {
            mDoPowerSave = false;
            sendDoPowerSave();
        }
        super.onDestroy();
    }

    public void onClick(DialogInterface arg0, int arg1) {
        // TODO Auto-generated method stub

        switch (arg1) {
        case DialogInterface.BUTTON_POSITIVE:
            mDoPowerSave = true;
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            mDoPowerSave = false;
            break;
        default:
            break;
        }
        sendDoPowerSave();
        checked = true;
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            mDoPowerSave = false;
            sendDoPowerSave();
            checked = true;
            finish();
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        // jongtak0920.kim Press Home key
        pressHomeKey = true;
    }

    private View createView() {
        mView = getLayoutInflater().inflate(R.layout.powersave_warning, null);

        mTextView = (TextView)mView.findViewById(R.id.warning_battery_text);
        mTextView.setVisibility(View.GONE);

        mBatteryImg = (ImageView)mView.findViewById(R.id.warning_battery_img);

        mTextViewSub = (TextView)mView.findViewById(R.id.warning_battery_text_sub);

        String subText = "";
        //jongtak0920.kim 120811 Delete a colon [START]
        if ("BR".equals(Config.getCountry())) {
            if ("pt".equals(Locale.getDefault().getLanguage()) ||
                    "es".equals(Locale.getDefault().getLanguage())) {
                subText = "";
            }
        }
        //jongtak0920.kim 120811 Delete a colon [END]

        // yonguk.kim 20120526
        if ("JP".equals(Config.getCountry())) { // jongtak0920.kim 120830 Fix Power Save Warning Popup SubText Token in Japan Model
            subText = Utils.getResources().getString(R.string.sp_power_saver_items_NORMAL) + " : ";
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_SYNC, 0) != 0) {
            subText = subText
                    + Utils.getResources().getString(R.string.sp_power_save_auto_sync_NORMAL)
                    + ", ";
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_WIFI, 1) != 0) {
            subText = subText
                    + Utils.getResources().getString(R.string.sp_power_saver_select_wifi_NORMAL)
                    + ", ";
        }
        if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_BT, 1) != 0) {
            subText = subText
                    + Utils.getResources().getString(
                            R.string.sp_power_saver_select_bluetooth_NORMAL) + ", ";
        }

        // jongtak0920.kim 120816 Add 'NFC' text in pop up [START]
        if (NfcAdapter.getDefaultAdapter(this) != null && "KR".equals(Config.getCountry())) {
            if (Settings.System
                    .getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_NFC, 1) != 0) {
                subText = subText
                        + Utils.getResources().getString(R.string.sp_power_saver_select_nfc_NORMAL)
                        + ", ";
            }
        }
        // jongtak0920.kim 120816 Add 'NFC' text in pop up [END]

        String hapticfeedback = SystemProperties.get("ro.device.hapticfeedback", "1");
        if (hapticfeedback.equals("0")) {
            //Remove haptic feedback for U0
        }
        else {
            if (Settings.System.getInt(mContentResolver, SettingsConstants.System.POWER_SAVE_TOUCH,
                    1) != 0) {
                if (Utils.isUI_4_1_model(this)) {
                    subText = subText
                            + Utils.getResources().getString(
                                    R.string.haptic_feedback_enable_title_tap)
                            + ", ";
                } else {
                    subText = subText
                            + Utils.getResources().getString(
                                    R.string.sp_power_saver_select_touch_NORMAL)
                            + ", ";
                }
            }
        }

        if (Settings.System.getInt(mContentResolver,
                SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST, 1) != 0) {
            subText = subText
                    + Utils.getResources().getString(
                            R.string.sp_power_saver_select_brightness_NORMAL) + ", ";
        }
        if (Settings.System.getInt(mContentResolver,
                SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST, 1) != 0) {
            subText = subText
                    + Utils.getResources().getString(
                            R.string.sp_power_saver_select_screen_timeout_NORMAL)
                    + ", ";
        }
        if (!Utils.supportFrontTouchKeyLight()) {
            // Remove Front key light Setting for L_DCM, V5
        }
        else {
            if (Utils.isFolderModel(this)) {
                if (Settings.System.getInt(mContentResolver,
                        SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 0) != 0) {
                    subText = subText + Utils.getResources().getString(
                            R.string.keypad_light_title) + ", ";
                }
            } else {
                if (Settings.System.getInt(mContentResolver,
                        SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 1) != 0) {
                    subText = subText
                            + Utils.getResources().getString(
                                    R.string.sp_power_saver_select_front_touch_light_NORMAL) + ", ";
                }
            }
        }

        // jongtak0920.kim Add Emotional LED and Notification LED
        if (Utils.supportEmotionalLED(this)) {
            if (Utils.supportHomeKey(this)) {
                if (Settings.System.getInt(mContentResolver, POWER_SAVE_EMOTIONAL_LED, 0) != 0) {
                    subText = subText
                            + Utils.getResources().getString(R.string.sp_home_button_led_title)
                            + ", ";
                }
            } else {
                if (Settings.System.getInt(mContentResolver, POWER_SAVE_EMOTIONAL_LED, 0) != 0) {
                    subText = subText
                            + Utils.getResources().getString(
                                    R.string.sp_power_saver_select_notification_led_NORMAL)
                            + ", ";
                }
            }

        }

        // jongtak0920.kim Add Auto-adjust screen tone
        if (Utils.isOLEDModel(this)) {
            if (Settings.System.getInt(mContentResolver, POWER_SAVE_AUTO_SCREEN_TONE, 1) != 0) {
                subText = subText
                        + Utils.getResources().getString(
                                R.string.sp_power_saver_select_auto_screen_tone_NORMAL) + ", ";
            }
        }

        if (Config.getFWConfigBool(this,
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover") == true) {
            if (Settings.System.getInt(mContentResolver, KEY_POWER_SAVE_QUICK_CASE, 1) != 0) {
                subText = subText
                        + Utils.getResources().getString(R.string.accessory_quick_case_title)
                        + ", ";
            }
        }

        if (subText.length() >= 2) {
            subText = subText.substring(0, subText.length() - 2);
        }

        if (subText != null && subText.length() > 0) {
            mTextViewSub.setText(subText);
        }

        return mView;
    }

    private void sendDoPowerSave() {
        Log.i(TAG, "sendDoPowerSave, Power save mode " + (mDoPowerSave ? "Start" : "Stop"));
        Intent intent = new Intent(PowerSave.ACTION_POWERSAVE_ACTIVATION);
        intent.putExtra(PowerSave.EXTRA_POWERSAVE_ACTIVATION, mDoPowerSave ? 1 : 0);
        sendBroadcast(intent);
    }

    private int getPowerSaveModeValue(Context context) {
        return Settings.System.getInt(
                context.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
    }
}
