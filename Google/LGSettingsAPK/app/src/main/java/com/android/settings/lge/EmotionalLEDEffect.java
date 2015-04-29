package com.android.settings.lge;

import android.app.ActionBar;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

import android.app.ActionBar;
import android.view.MenuItem;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;

import android.preference.PreferenceCategory;
import com.android.settings.Utils;
import android.app.ActivityManager;
import java.util.List;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.os.SystemProperties;

public class EmotionalLEDEffect extends SettingsPreferenceFragment {
    private Switch actionBarSwitch;
    private boolean isSilentModeChecked = true;
    private static final String TAG = "EmotionalLEDEffect";

    private CheckBoxPreference led_effect_alarm;
    private CheckBoxPreference led_effect_calendar;
    private CheckBoxPreference led_effect_gps;
    private CheckBoxPreference led_effect_incoming_call;
    private CheckBoxPreference led_effect_download_apps;
    //    private CheckBoxPreference led_effect_missed_reminder;
    private CheckBoxPreference led_effect_favorites_incoming;
    private CheckBoxPreference led_effect_battery_charging;
    private CheckBoxPreference led_effect_osaifu_keitai;
    private CheckBoxPreference led_effect_voice_recorder;
    private CheckBoxPreference incoming_back_led;
    private CheckBoxPreference alarm_back_led;
    private CheckBoxPreference incall_back_led;
    private DoubleTitlePreference led_effect_area_mail;
    private CheckBoxPreference reminder_missed_call;
    private CheckBoxPreference reminder_msg;
    private CheckBoxPreference reminder_voicemail;
    private CheckBoxPreference reminder_email;
    private CheckBoxPreference reminder_cell_broad;
    private PreferenceCategory fornt_led_category;

    private CheckBoxPreference missed_call_back_led;
    private CheckBoxPreference missed_messages_back_led;
    private CheckBoxPreference missed_emails_back_led;
    private CheckBoxPreference calendar_noti_back_led;
    private CheckBoxPreference voice_recording_back_led;

    //    private Preference lgSoftwareLinearLayout;
    private static final String FRONT_LED_CATEGORY_KEY = "home_button_led_category";
    private static final String EMOTIONAL_LED_EFFECT_ALARM_KEY = "emotional_led_effect_alarm";
    private static final String EMOTIONAL_LED_EFFECT_CALENDAR_KEY = "emotional_led_effect_calendar";
    private static final String EMOTIONAL_LED_EFFECT_GPS_KEY = "emotional_led_effect_gps";
    private static final String EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY = "emotional_led_effect_incoming_call";
    private static final String EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY = "emotional_led_effect_download_apps";
    //    private static final String EMOTIONAL_LED_EFFECT_MISSED_REMINDER_KEY = "emotional_led_effect_reminder";
    //private static final String EMOTIONAL_LED_EFFECT_SELECT_MISSED_EVENT_KEY = "emotional_led_effect_select_missed_event";
    private static final String EMOTIONAL_LED_EFFECT_AREA_MAIL_KEY = "emotional_led_effect_area_mail";
    private static final String EMOTIONAL_LED_EFFECT_FAVORITES_KEY = "emotional_led_effect_favorites_incoming";
    private static final String EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY = "emotional_led_effect_battery_charging";
    private static final String EMOTIONAL_LED_EFFECT_OSAIFU_KEITAI_KEY = "emotional_led_effect_osaifu";
    private static final String EMOTIONAL_LED_EFFECT_VOICE_RECORDER_LED_KEY = "emotional_led_effect_voice_recording";
    private static final String EMOTIONAL_LED_EFFECT_BRIGHTNESS_CATEGORY_KEY = "home_button_brightness_category";
    private static final String EMOTIONAL_LED_EFFECT_BACK_CATEGORY_KEY = "home_button_back_category";
    private static final String EMOTIONAL_LED_EFFECT_INCOMING_BACK_LED_KEY = "incoming_back";
    private static final String EMOTIONAL_LED_EFFECT_ALARM_BACK_LED_KEY = "alarm_back";
    private static final String EMOTIONAL_LED_EFFECT_INCALL_BACK_LED_KEY = "in_call";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL = "missed_call_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_MESSAGES = "missed_messages_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_EMAILS = "missed_emails_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_CALENDAR_NOTI = "calendar_noti_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_VOICE_RECORDING = "voice_recording_back";

    //private static final String EMOTIONAL_LED_REMINDER_DESC = "emotional_led_reminder_missed_desc";
    private static final String EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY = "emotional_led_reminder_missed_call";
    private static final String EMOTIONAL_LED_REMINDER_MSG_KEY = "emotional_led_reminder_msg";
    private static final String EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY = "emotional_led_reminder_voicemail";
    private static final String EMOTIONAL_LED_REMINDER_EMAIL_KEY = "emotional_led_reminder_email";
    private static final String EMOTIONAL_LED_REMINDER_CELL_BROAD = "emotional_led_reminder_cell_broad";

    // Define Notification Observer
    // 2013-05-09
    // A 4Team 2Part Yeom Seungyeop Y
    private ContentObserver mNotificationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateNotificationCheckbox();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.emotional_led_effect);
        updateTitle();

        fornt_led_category = (PreferenceCategory)findPreference(FRONT_LED_CATEGORY_KEY);
        led_effect_alarm = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_ALARM_KEY);
        led_effect_calendar = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_CALENDAR_KEY);
        led_effect_gps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_GPS_KEY);
        led_effect_incoming_call = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY);
        led_effect_download_apps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY);
        //        led_effect_missed_reminder = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_MISSED_REMINDER_KEY);
        led_effect_area_mail = (DoubleTitlePreference)findPreference(EMOTIONAL_LED_EFFECT_AREA_MAIL_KEY);
        led_effect_area_mail.setSubTitle("(" + getString(R.string.sp_emotion_led_always_on) + ")");
        //led_effect_area_mail.setSubTitleGravity(Gravity.END);
        led_effect_favorites_incoming = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_FAVORITES_KEY);
        led_effect_battery_charging = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY);
        led_effect_osaifu_keitai = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_OSAIFU_KEITAI_KEY);
        led_effect_voice_recorder = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_VOICE_RECORDER_LED_KEY);
        incoming_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCOMING_BACK_LED_KEY);
        alarm_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_ALARM_BACK_LED_KEY);
        incall_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCALL_BACK_LED_KEY);
        missed_call_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL);
        missed_messages_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_MESSAGES);
        missed_emails_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_EMAILS);
        calendar_noti_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_CALENDAR_NOTI);
        voice_recording_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_VOICE_RECORDING);
        /*        lgSoftwareLinearLayout = (Preference)findPreference(EMOTIONAL_LED_EFFECT_SELECT_MISSED_EVENT_KEY);
                lgSoftwareLinearLayout.setLayoutResource(R.layout.emotional_select_preference); */
        reminder_missed_call = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY);
        reminder_msg = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MSG_KEY);
        reminder_voicemail = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY);
        reminder_email = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_EMAIL_KEY);
        reminder_cell_broad = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_CELL_BROAD);

        EmotionalLEDEffectUtils.sPackageName = getActivity().getPackageName();

        PreferenceCategory brightnessCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(EMOTIONAL_LED_EFFECT_BRIGHTNESS_CATEGORY_KEY);
        PreferenceCategory backCategory = (PreferenceCategory)getPreferenceScreen().findPreference(
                EMOTIONAL_LED_EFFECT_BACK_CATEGORY_KEY);
        Log.d("hong",
                "Back_config: "
                        + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasBackLed,
                           "com.lge.R.bool.config_hasBackLed"));
        Log.d("hong",
                "LED control config: "
                        + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_userConfigLedBrightness,
                              "com.lge.R.bool.config_userConfigLedBrightness"));

        if ("vu3".equals(Build.DEVICE)) {
            fornt_led_category.setTitle(R.string.sp_emotional_led_notification_title);
        }

        if ("DCM".equals(Config.getOperator()))
        {
            reminder_missed_call.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_msg.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_voicemail.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_email.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_cell_broad.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            //led_effect_missed_reminder.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
            //led_effect_incoming_call.setSummary(R.string.sp_emotional_led_incoming_call_summary_dcm);
            Log.i(TAG, "Area mail : Include menu");
        } else {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(led_effect_area_mail);
                getPreferenceScreen().removePreference(led_effect_gps);
                if ("KDDI".equals(Config.getOperator())) {
                    getPreferenceScreen().removePreference(reminder_msg);
                    if (backCategory != null) {
                        backCategory.removePreference(missed_messages_back_led);
                    }
                }
                else {
                    getPreferenceScreen().removePreference(led_effect_osaifu_keitai);
                }
            }
            Log.i(TAG, "Area mail : Remove menu");
        }

        loadsettings();
        updateImage();

        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(led_effect_favorites_incoming);
            if (!Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasBackLed,
                           "com.lge.R.bool.config_hasBackLed")) {
                getPreferenceScreen().removePreference(backCategory);
                //            brightnessCategory.removePreference(led_effect_back_led);
            }

            if (!Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_userConfigLedBrightness,
                              "com.lge.R.bool.config_userConfigLedBrightness")) {
                getPreferenceScreen().removePreference(brightnessCategory);
            }

            if (backCategory != null) {
                    backCategory.removePreference(missed_call_back_led);
                    backCategory.removePreference(missed_messages_back_led);
                    backCategory.removePreference(missed_emails_back_led);
                    backCategory.removePreference(calendar_noti_back_led);
                    backCategory.removePreference(voice_recording_back_led);
            }
        }

        if (Utils.checkPackage(reminder_cell_broad.getContext(),
                "com.android.cellbroadcastreceiver"))
        {
            Log.i(TAG, "Cell Broadcasting : Include menu");
        } else {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_cell_broad);
            }
            Log.i(TAG, "Cell Broadcasting : Remove menu");
        }
        if (!("ATT".equals(Config.getOperator())) && !("USC".equals(Config.getOperator()))
                && !("VZW".equals(Config.getOperator())))
        {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_voicemail);
            }
        }
        if ("KR".equals(Config.getCountry())) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_cell_broad);
            }
        }

        if ("ATT".equals(Config.getOperator())
                || Utils.isG2Model()) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(led_effect_voice_recorder);
            }
        }

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

        // Regist Notification Observer
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        actionBarSwitch.setChecked(isSilentModeChecked);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor("lge_notification_light_pulse"),
                true, mNotificationObserver);

        if ("DCM".equals(Config.getOperator())) {
            // Register receiver for intents
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            getActivity().registerReceiver(mCableReceiver, filter);
        }
        setlayoutVisible();
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
        /*        led_effect_missed_reminder.setChecked(Settings.System.getInt(getContentResolver(),
                                            SettingsConstants.System.EMOTIONAL_LED_MISSED_EVENT_REMINDER, 0) == 0 ? false : true); */
        led_effect_favorites_incoming.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL_FAVORITE, 0) == 0 ? false
                : true);
        led_effect_battery_charging.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BATTERY_CHARGING, 0) == 0 ? false : true);
        led_effect_osaifu_keitai.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_FELICA, 0) == 0 ? false : true);
        led_effect_voice_recorder.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_VOICE_RECORDER, 0) == 0 ? false : true);
        incoming_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL, 1) == 0 ? false : true);
        alarm_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM, 1) == 0 ? false : true);
        incall_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_IN_CALL, 1) == 0 ? false : true);
        reminder_missed_call
                .setChecked(Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_REMINDER_MISSED_CALL, 0) == 0 ? false
                        : true);

        reminder_msg.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_MSG, 0) == 0 ? false : true);

        reminder_voicemail.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL, 0) == 0 ? false : true);

        reminder_email.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_EMAIL, 0) == 0 ? false : true);

        reminder_cell_broad.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_CELL_BROAD, 0) == 0 ? false : true);

        missed_call_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL, 1) == 0 ? false : true);

        missed_messages_back_led
                .setChecked(Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES, 1) == 0 ? false
                        : true);

        missed_emails_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_EMAILS, 1) == 0 ? false : true);

        calendar_noti_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_CALENDAR_NOTI, 1) == 0 ? false : true);

        voice_recording_back_led
                .setChecked(Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_BACK_VOICE_RECORDING, 1) == 0 ? false
                        : true);

        /*if (!led_effect_missed_reminder.isChecked()) {
            lgSoftwareLinearLayout.setEnabled(false);
        } else {
            lgSoftwareLinearLayout.setEnabled(true);
        }*/
    }

    private void updateImage() {
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
            led_effect_alarm.setIcon(R.drawable.img_emotionl_led_alarm);
            led_effect_calendar.setIcon(R.drawable.img_emotionl_led_calendar);
            led_effect_gps.setIcon(R.drawable.img_emotionl_led_gps);
            led_effect_incoming_call.setIcon(R.drawable.img_emotionl_led_incoming);
            if ("DCM".equals(Config.getOperator())) {
                reminder_missed_call.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
                reminder_msg.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
                reminder_voicemail.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
                reminder_email.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
                reminder_cell_broad.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
            } else {
                reminder_missed_call.setIcon(R.drawable.img_emotionl_led_reminder);
                reminder_msg.setIcon(R.drawable.img_emotionl_led_reminder);
                reminder_voicemail.setIcon(R.drawable.img_emotionl_led_reminder);
                reminder_email.setIcon(R.drawable.img_emotionl_led_reminder);
                reminder_cell_broad.setIcon(R.drawable.img_emotionl_led_reminder);
            }
            //            led_effect_missed_reminder.setIcon(R.drawable.emotionl_led_reminder);
            led_effect_favorites_incoming.setIcon(R.drawable.img_emotionl_led_favorites_incoming_call2);
            led_effect_battery_charging.setIcon(R.drawable.img_emotionl_led_red);
            led_effect_osaifu_keitai.setIcon(R.drawable.img_emotionl_led_felica);
            led_effect_voice_recorder.setIcon(R.drawable.img_emotionl_led_red);
            led_effect_download_apps.setIcon(R.drawable.img_emotionl_led_downloaded);
        }
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
        } /*else if (preference == led_effect_missed_reminder) {
            mValue = led_effect_missed_reminder.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_MISSED_EVENT_REMINDER;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
          }*/ else if (preference == led_effect_favorites_incoming) {
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
        } else if (preference == led_effect_voice_recorder) {
            mValue = led_effect_voice_recorder.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_VOICE_RECORDER;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_VOICE_RECORDER_NOTI, getActivity());
        } else if (preference == incoming_back_led) {
            mValue = incoming_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL;
            if ("b1".equals(Build.DEVICE)) {
                EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_INCOMING_CALL_NOTI, getActivity());
                Log.d("YSY", "incoming_back_led");
            } else {
                EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BACK_NOTI, getActivity());
                Log.d("YSY", "no incoming_back_led");
            }
        } else if (preference == alarm_back_led) {
            mValue = alarm_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM;
            if ("b1".equals(Build.DEVICE)) {
                EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_ALARM_NOTI, getActivity());
                Log.d("YSY", "alarm_back_led");
            } else {
                EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BACK_NOTI, getActivity());
                Log.d("YSY", "no alarm_back_led");
            }
        } else if (preference == incall_back_led) {
            mValue = incall_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_IN_CALL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_IN_CALL_BACK_NOTI, getActivity());
        } else if (preference == reminder_missed_call) {
            mValue = reminder_missed_call.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_MISSED_CALL;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == reminder_msg) {
            mValue = reminder_msg.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_MSG;
            Intent intent = new Intent("com.lge.intent.action.EMOTIONAL_LED_CHANGE_MSG");
            if (mValue) {
                intent.putExtra("name", "ON");
            } else {
                intent.putExtra("name", "OFF");
            }
            getActivity().sendBroadcast(intent);
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == reminder_voicemail) {
            mValue = reminder_voicemail.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == reminder_email) {
            mValue = reminder_email.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_EMAIL;
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == reminder_cell_broad) {
            mValue = reminder_cell_broad.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_CELL_BROAD;
            Intent intent = new Intent("com.lge.intent.action.EMOTIONAL_LED_CHANGE_CB");
            if (mValue) {
                intent.putExtra("name", "ON");
            } else {
                intent.putExtra("name", "OFF");
            }
            getActivity().sendBroadcast(intent);
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == missed_call_back_led) {
            mValue = missed_call_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BACK_NOTI, getActivity());
        } else if (preference == missed_messages_back_led) {
            mValue = missed_messages_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BACK_NOTI, getActivity());
        } else if (preference == missed_emails_back_led) {
            mValue = missed_emails_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_EMAILS;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_BACK_NOTI, getActivity());
        } else if (preference == calendar_noti_back_led) {
            mValue = calendar_noti_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_CALENDAR_NOTI;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_CALENDAR_NOTI, getActivity());
        } else if (preference == voice_recording_back_led) {
            mValue = voice_recording_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_VOICE_RECORDING;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_VOICE_RECORDER_NOTI, getActivity());
        }
        else {
            return false;
        }
        Log.d("hong", "Click_DB: " + mClickItem + "/Value: " + mValue);
        Settings.System.putInt(getContentResolver(), mClickItem, mValue == true ? 1 : 0);
        loadsettings();

        return true;
    }

    // Regist Notification Observer Func
    // 2013-05-09
    // A 4Team 2Part Yeom Seungyeop Y
    private void updateNotificationCheckbox() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        actionBarSwitch.setChecked(isSilentModeChecked);
        //Log.d(TAG, "Notification LED Status : " + isSilentModeChecked);
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
    public void onPause() {
        super.onPause();
        // [Stop LED] This activity is not on the foreground
        if (EmotionalLEDEffectUtils.sLedManager != null) {
            EmotionalLEDEffectUtils.sLedManager.stop(EmotionalLEDEffectUtils.sPackageName, EmotionalLEDEffectUtils.sLastSelectedFunction);
        }
        if ("DCM".equals(Config.getOperator())) {
            getActivity().unregisterReceiver(mCableReceiver);
        }

        // Unregist Notification Observer Func
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        getContentResolver().unregisterContentObserver(mNotificationObserver);
    }

    private BroadcastReceiver mCableReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                EmotionalLEDEffectUtils.sStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                Log.i("hsmodel",
                        "Battery Status : "
                                + com.android.settings.Utils.getBatteryStatus(getResources(),
                                        intent));
            }
        }
    };
}
