package com.android.settings.lge;

import android.app.ActionBar;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SettingsPreferenceFragment;

import com.lge.constants.SettingsConstants;
import android.provider.Settings;

import android.app.ActionBar;
import android.view.MenuItem;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceCategory;
import android.os.SystemProperties;
import com.android.settings.Utils;
import android.os.Build;

public class EmotionalLEDEffectGK extends SettingsPreferenceFragment {
    private Switch actionBarSwitch;
    private boolean isSilentModeChecked = true;
    private static final String TAG = "EmotionalLEDEffectGK";

    private CheckBoxPreference led_effect_alarm;
    private CheckBoxPreference led_effect_calendar;
    private CheckBoxPreference led_effect_gps;
    private CheckBoxPreference led_effect_incoming_call;
    private CheckBoxPreference led_effect_download_apps;
    private CheckBoxPreference led_effect_missed_reminder;
    private CheckBoxPreference led_effect_favorites_incoming;
    private CheckBoxPreference led_effect_battery_charging;
    private CheckBoxPreference led_effect_osaifu_keitai;
    private DoubleTitlePreference led_effect_area_mail;

    private Preference lgSoftwareLinearLayout;

    private static final String EMOTIONAL_LED_EFFECT_ALARM_KEY = "emotional_led_effect_alarm";
    private static final String EMOTIONAL_LED_EFFECT_CALENDAR_KEY = "emotional_led_effect_calendar";
    private static final String EMOTIONAL_LED_EFFECT_GPS_KEY = "emotional_led_effect_gps";
    private static final String EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY = "emotional_led_effect_incoming_call";
    private static final String EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY = "emotional_led_effect_download_apps";
    private static final String EMOTIONAL_LED_EFFECT_MISSED_REMINDER_KEY = "emotional_led_effect_reminder";
    private static final String EMOTIONAL_LED_EFFECT_SELECT_MISSED_EVENT_KEY = "emotional_led_effect_select_missed_event";
    private static final String EMOTIONAL_LED_EFFECT_AREA_MAIL_KEY = "emotional_led_effect_area_mail";
    private static final String EMOTIONAL_LED_EFFECT_FAVORITES_KEY = "emotional_led_effect_favorites_incoming";
    private static final String EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY = "emotional_led_effect_battery_charging";
    private static final String EMOTIONAL_LED_EFFECT_OSAIFU_KEITAI_KEY = "emotional_led_effect_osaifu";
    private static final String EMOTIONAL_LED_EFFECT_BRIGHTNESS_CATEGORY_KEY = "home_button_brightness_category";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.emotional_led_effect_gk);
        updateTitle();

        led_effect_alarm = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_ALARM_KEY);
        led_effect_calendar = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_CALENDAR_KEY);
        led_effect_gps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_GPS_KEY);
        led_effect_incoming_call = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY);
        led_effect_download_apps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY);
        led_effect_missed_reminder = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_MISSED_REMINDER_KEY);
        led_effect_area_mail = (DoubleTitlePreference)findPreference(EMOTIONAL_LED_EFFECT_AREA_MAIL_KEY);
        led_effect_area_mail.setSubTitle("(" + getString(R.string.sp_emotion_led_always_on) + ")");
        //led_effect_area_mail.setSubTitleGravity(Gravity.END);
        led_effect_favorites_incoming = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_FAVORITES_KEY);
        led_effect_battery_charging = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY);
        led_effect_osaifu_keitai = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_OSAIFU_KEITAI_KEY);
        lgSoftwareLinearLayout = (Preference)findPreference(EMOTIONAL_LED_EFFECT_SELECT_MISSED_EVENT_KEY);
        lgSoftwareLinearLayout.setLayoutResource(R.layout.emotional_select_preference);

        EmotionalLEDEffectUtils.sPackageName = getActivity().getPackageName();
        PreferenceCategory brightnessCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(EMOTIONAL_LED_EFFECT_BRIGHTNESS_CATEGORY_KEY);
        if (getPreferenceScreen() != null) {
            //config value is applied in Settings4_0, but Settings3_2
            if (Build.BOARD.contains("fx3") || Build.BOARD.contains("f6")) {
                this.getPreferenceScreen().removePreference(brightnessCategory);
            }
            getPreferenceScreen().removePreference(led_effect_favorites_incoming);
        }

        if ("DCM".equals(Config.getOperator()))
        {
            led_effect_missed_reminder.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            //led_effect_incoming_call.setSummary(R.string.sp_emotional_led_incoming_call_summary_dcm);
            Log.i(TAG, "Area mail : Include menu");
        } else {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(led_effect_area_mail);
                getPreferenceScreen().removePreference(led_effect_osaifu_keitai);
                getPreferenceScreen().removePreference(led_effect_gps);
            }
            Log.i(TAG, "Area mail : Remove menu");
        }

        loadsettings();

        if ("VZW".equals(Config.getOperator())) {
            led_effect_download_apps.setSummary(
                    R.string.sp_emotional_led_download_apps_3_ex_vzw);
        }
        EmotionalLEDEffectUtils.setEmotionalLEDSettings(getActivity());

        //Action Bar Icon Display
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if ("VZW".equals(Config.getOperator())) {
                actionBar.setTitle(R.string.notification_led_vzw);
            }
        }
    }

    public void onResume() {
        super.onResume();
        if ("DCM".equals(Config.getOperator())) {
            // Register receiver for intents
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            getActivity().registerReceiver(mCableReceiver, filter);
        }
        setlayoutVisible();
    }

    private void updateTitle() {
        actionBarSwitch = new Switch(getActivity());
        final int padding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);

        actionBarSwitch.setPaddingRelative(0, 0, padding, 0);
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        actionBarSwitch.setChecked(isSilentModeChecked);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
            actionBar.setTitle(R.string.sp_home_button_led_title);
            actionBar.setIcon(R.drawable.img_emotionl_led_main);
        }
        actionBar.setCustomView(actionBarSwitch,
                new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
        actionBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && EmotionalLEDEffectUtils.sValueAnimator.isRunning()) {
                    Log.i("hsmodel", "cancel animator");
                    EmotionalLEDEffectUtils.sValueAnimator.end();
                }
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OnSilentGroupClick(isChecked);
            }
        });

        actionBarSwitch.setOnClickListener(new CompoundButton.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    private void loadsettings() {
        led_effect_alarm.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_ALARM, 0) == 0 ? false : true);
        led_effect_calendar.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_CALENDAR, 0) == 0 ? false : true);
        led_effect_gps.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_GPS, 0) == 0 ? false : true);
        led_effect_incoming_call.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL, 0) == 0 ? false : true);
        led_effect_download_apps.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0) == 0 ? false : true);
        led_effect_missed_reminder.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_MISSED_EVENT_REMINDER, 0) == 0 ? false
                : true);
        led_effect_favorites_incoming.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL_FAVORITE, 0) == 0 ? false
                : true);
        led_effect_battery_charging.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BATTERY_CHARGING, 0) == 0 ? false : true);
        led_effect_osaifu_keitai.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_FELICA, 0) == 0 ? false : true);

        /*if (!led_effect_missed_reminder.isChecked()) {
        	lgSoftwareLinearLayout.setEnabled(false);
        } else {
        	lgSoftwareLinearLayout.setEnabled(true);
        }*/
    }

    private void OnSilentGroupClick(boolean mValue) {
        actionBarSwitch.setChecked(mValue);
        Settings.System.putInt(getContentResolver(), "lge_notification_light_pulse",
                mValue == true ? 1 : 0);

        isSilentModeChecked = mValue;
        setlayoutVisible();
    }

    private void setlayoutVisible() {
        Log.i(TAG, "Current Layout mode : " + isSilentModeChecked);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().setEnabled(isSilentModeChecked);
            if (led_effect_area_mail != null) {
                led_effect_area_mail.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean mValue = false;
        String mClickItem;

        if (preference == led_effect_alarm) {
            mValue = led_effect_alarm.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_ALARM;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_ALARM_NOTI, getActivity());
        } else if (preference == led_effect_calendar) {
            mValue = led_effect_calendar.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_CALENDAR;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_CALENDAR_NOTI, getActivity());
        } else if (preference == led_effect_gps) {
            mValue = led_effect_gps.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_GPS;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_GPS_NOTI, getActivity());
        } else if (preference == led_effect_incoming_call) {
            mValue = led_effect_incoming_call.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_INCOMING_CALL_NOTI, getActivity());
        } else if (preference == led_effect_download_apps) {
            mValue = led_effect_download_apps.isChecked();
            mClickItem = Settings.System.NOTIFICATION_LIGHT_PULSE;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_DOWNLOAD_APPS_NOTI, getActivity());
        } else if (preference == led_effect_missed_reminder) {
            mValue = led_effect_missed_reminder.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_MISSED_EVENT_REMINDER;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == led_effect_favorites_incoming) {
            mValue = led_effect_favorites_incoming.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL_FAVORITE;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_INCOMING_CALL_FAVORITE_NOTI, getActivity());
        } else if (preference == led_effect_battery_charging) {
            mValue = led_effect_battery_charging.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BATTERY_CHARGING;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BATTERY_CHARGING_NOTI, getActivity());
        } else if (preference == led_effect_osaifu_keitai) {
            mValue = led_effect_osaifu_keitai.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_FELICA;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_OSAIFU_KEITAI_NOTI, getActivity());
        } else {
            return false;
        }
        Log.d("hong", "Click_DB: " + mClickItem + "/Value: " + mValue);
        Settings.System.putInt(getContentResolver(), mClickItem, mValue == true ? 1 : 0);
        loadsettings();

        return true;
    }

    //Action Bar Icon Back key
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        // [Stop LED] This activity is not on the foreground
        if (EmotionalLEDEffectUtils.sLedManager != null) {
            EmotionalLEDEffectUtils.sLedManager.stop(EmotionalLEDEffectUtils.sPackageName, EmotionalLEDEffectUtils.sLastSelectedFunction);
        }
        if ("DCM".equals(Config.getOperator())) {
            getActivity().unregisterReceiver(mCableReceiver);
        }
    }

    private BroadcastReceiver mCableReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                EmotionalLEDEffectUtils.sStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                Log.i("hsmodel",
                        "Battery Status : " + Utils.getBatteryStatus(getResources(), intent));
            }
        }
    };
}
