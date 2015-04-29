package com.android.settings.lge;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.lge.constants.SettingsConstants;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lge.IconTalkbackCheckBoxPreference;

public class EmotionalLEDEffectTabRear extends SettingsPreferenceFragment {

    private Switch mActionBarSwitch;
    private boolean isSilentModeChecked = true;
    private static final String TAG = "EmotionalLEDEffectTabRear";

    private IconTalkbackCheckBoxPreference incoming_back_led;
    private CheckBoxPreference alarm_back_led;
    private IconTalkbackCheckBoxPreference incall_back_led;
    private CheckBoxPreference missed_call_back_led;
    private CheckBoxPreference missed_messages_back_led;
    private CheckBoxPreference missed_emails_back_led;
    private CheckBoxPreference calendar_noti_back_led;
    private CheckBoxPreference voice_recording_back_led;
    private CheckBoxPreference camera_timer_back_led;
    private IconTalkbackCheckBoxPreference camera_face_detecting_back_led;
    private CheckBoxPreference download_apps_back_led;
    private IconTalkbackCheckBoxPreference missed_call_message_back_led;
    private IconTalkbackCheckBoxPreference missed_voice_mail_back_led;

    private static final String EMOTIONAL_LED_EFFECT_INCOMING_BACK_LED_KEY = "incoming_back";
    private static final String EMOTIONAL_LED_EFFECT_ALARM_BACK_LED_KEY = "alarm_back";
    private static final String EMOTIONAL_LED_EFFECT_INCALL_BACK_LED_KEY = "in_call";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL = "missed_call_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_MESSAGES = "missed_messages_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_EMAILS = "missed_emails_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_CALENDAR_NOTI = "calendar_noti_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_VOICE_RECORDING = "voice_recording_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_CAMERA_TIMER = "camera_timer_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_CAMERA_FACE_DETECTING = "camera_face_detecting_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_DOWNLOAD_APPS = "emotional_led_effect_download_apps_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL_MSG = "missed_call_msg_back";
    private static final String EMOTIONAL_LED_EFFECT_BACK_MISSED_VOICE_MAIL = "missed_voicemail_back";

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

        addPreferencesFromResource(R.xml.emotional_led_effect_tab_rear);
        updateTitle();

        incoming_back_led = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCOMING_BACK_LED_KEY);
        alarm_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_ALARM_BACK_LED_KEY);
        incall_back_led = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCALL_BACK_LED_KEY);
        missed_call_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL);
        missed_messages_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_MESSAGES);
        missed_emails_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_EMAILS);
        calendar_noti_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_CALENDAR_NOTI);
        voice_recording_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_VOICE_RECORDING);
        camera_timer_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_CAMERA_TIMER);
        camera_face_detecting_back_led = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_CAMERA_FACE_DETECTING);
        download_apps_back_led = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_DOWNLOAD_APPS);
        missed_call_message_back_led = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_CALL_MSG);
        missed_voice_mail_back_led = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BACK_MISSED_VOICE_MAIL);
        EmotionalLEDEffectUtils.sPackageName = getActivity().getPackageName();

        Log.d("hong",
                "Back_config: "
                        + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasBackLed,
                              "com.lge.R.bool.config_hasBackLed"));
        Log.d("hong",
                "LED control config: "
                        + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_userConfigLedBrightness,
                              "com.lge.R.bool.config_userConfigLedBrightness"));

        if (getPreferenceScreen() != null) {
            if ("KDDI".equals(Config.getOperator())) {
                getPreferenceScreen().removePreference(missed_messages_back_led);
                missed_call_message_back_led.setTitle(
                    R.string.sp_emotional_led_reminder_missed_calls);
            }
        }

        if (!("ATT".equals(Config.getOperator()))
                && !("VZW".equals(Config.getOperator()))) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(missed_voice_mail_back_led);
            }
        }

        if ("ATT".equals(Config.getOperator())) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(voice_recording_back_led);
            }
        }

            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(voice_recording_back_led);
                getPreferenceScreen().removePreference(alarm_back_led);
                getPreferenceScreen().removePreference(calendar_noti_back_led);
                getPreferenceScreen().removePreference(camera_timer_back_led);
                getPreferenceScreen().removePreference(missed_messages_back_led);
                getPreferenceScreen().removePreference(missed_emails_back_led);
                getPreferenceScreen().removePreference(missed_call_back_led);
                if (!Utils.isUI_4_1_model(getActivity())) {
                    getPreferenceScreen().removePreference(missed_voice_mail_back_led);
                }
            }

        loadsettings();
        updateImage();
        if (Utils.isBackLEDRGB(getActivity())) {
            setBackLEDRGBIcon();
            setTalkBackIcon();
        }

        if ("VZW".equals(Config.getOperator())) {
            download_apps_back_led.setSummary(
                    R.string.sp_emotional_led_download_apps_3_ex_vzw);
        }

        EmotionalLEDEffectUtils.setEmotionalLEDSettings(getActivity());

        //Action Bar Icon Display
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);

            }
        }
        
        if ("VZW".equals(Config.getOperator())) {
            getActivity().setTitle(R.string.notification_led_vzw);
        } else {
            getActivity().setTitle(R.string.notification_led);
        }
    }

    public void onResume() {
        super.onResume();

        // Regist Notification Observer 
        // 2013-05-09
        // A 4Team 2Part Yeom Seungyeop Y
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        mActionBarSwitch.setChecked(isSilentModeChecked);
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
        incoming_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL, 1) == 0 ? false : true);
        alarm_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM, 1) == 0 ? false : true);
        incall_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_IN_CALL, 1) == 0 ? false : true);

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

        camera_timer_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                "emotional_led_back_camera_timer_noti", 1) == 0 ? false : true);

        camera_face_detecting_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                "emotional_led_back_camera_face_detecting_noti", 1) == 0 ? false : true);

        download_apps_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                "notification_light_pulse_back", 1) == 0 ? false : true);
        missed_call_message_back_led
                .setChecked(Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL_MSG, 1) == 0 ? false
                        : true);

        missed_voice_mail_back_led.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_VOICE_MAIL, 1) == 0 ? false
                : true);

    }

    private void updateImage() {
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {

        }
    }

    private void setBackLEDRGBIcon() {
        incoming_back_led.setIcon(R.drawable.img_emotionl_led_incoming_back_noti);
        alarm_back_led.setIcon(R.drawable.img_emotionl_led_alarm_back_noti);
        incall_back_led.setIcon(R.drawable.img_emotionl_led_incall_back_red_noti);
        missed_call_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        missed_messages_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        missed_emails_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        missed_call_message_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        calendar_noti_back_led.setIcon(R.drawable.img_emotionl_led_calendar_back_noti);
        voice_recording_back_led.setIcon(R.drawable.img_emotionl_led_red_back_noti);
        camera_timer_back_led.setIcon(R.drawable.img_emotionl_led_timer_event_back_noti);
        camera_face_detecting_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        download_apps_back_led.setIcon(R.drawable.img_emotionl_led_downloaded_noti);
        missed_voice_mail_back_led.setIcon(R.drawable.img_emotionl_led_missed_event_back_noti);
        if ("DCM".equals(Config.getOperator())) {
            missed_call_back_led.setIcon(R.drawable.img_emotionl_led_calendar_back_noti);
            missed_messages_back_led.setIcon(R.drawable.img_emotionl_led_calendar_back_noti);
            missed_emails_back_led.setIcon(R.drawable.img_emotionl_led_calendar_back_noti);
            missed_call_message_back_led.setIcon(R.drawable.img_emotionl_led_calendar_back_noti);
        }
    }

    private void updateTitle() {
        mActionBarSwitch = new Switch(getActivity());
        final int padding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);

        mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        mActionBarSwitch.setChecked(isSilentModeChecked);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
            actionBar.setTitle(R.string.sp_home_button_led_title);
            actionBar.setIcon(R.drawable.img_emotionl_led_main);
        }
        actionBar.setCustomView(mActionBarSwitch,
                new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
        mActionBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        mActionBarSwitch.setOnClickListener(new CompoundButton.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void OnSilentGroupClick(boolean mValue) {
        mActionBarSwitch.setChecked(mValue);
        Settings.System.putInt(getContentResolver(), "lge_notification_light_pulse",
                mValue == true ? 1 : 0);

        isSilentModeChecked = mValue;
        setlayoutVisible();
    }

    private void setlayoutVisible() {
        Log.i(TAG, "Current Layout mode : " + isSilentModeChecked);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().setEnabled(isSilentModeChecked);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean mValue = false;
        String mClickItem;

        if (preference == incoming_back_led) {
            mValue = incoming_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_INCOMING_CALL_NOTI, getActivity());
        } else if (preference == alarm_back_led) {
            mValue = alarm_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_ALARM_NOTI, getActivity());
        } else if (preference == incall_back_led) {
            mValue = incall_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_IN_CALL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_IN_CALL_BACK_NOTI, getActivity());
        } else if (preference == missed_call_back_led) {
            mValue = missed_call_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == missed_messages_back_led) {
            mValue = missed_messages_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == missed_emails_back_led) {
            mValue = missed_emails_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_EMAILS;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == calendar_noti_back_led) {
            mValue = calendar_noti_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_CALENDAR_NOTI;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_CALENDAR_NOTI, getActivity());
        } else if (preference == voice_recording_back_led) {
            mValue = voice_recording_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_VOICE_RECORDING;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_VOICE_RECORDER_NOTI, getActivity());
        } else if (preference == camera_timer_back_led) {
            mValue = camera_timer_back_led.isChecked();
            mClickItem = "emotional_led_back_camera_timer_noti";
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_CAMERA_TIMER_NOTI, getActivity());
        } else if (preference == camera_face_detecting_back_led) {
            mValue = camera_face_detecting_back_led.isChecked();
            mClickItem = "emotional_led_back_camera_face_detecting_noti";
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_CAMERA_FACE_DETECTING_NOTI, getActivity());
        } else if (preference == download_apps_back_led) {
            mValue = download_apps_back_led.isChecked();
            mClickItem = "notification_light_pulse_back";
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_DOWNLOAD_APPS_NOTI, getActivity());
        } else if (preference == missed_call_message_back_led) {
            mValue = missed_call_message_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL_MSG;
            setReminderItemDB(mValue);
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else if (preference == missed_voice_mail_back_led) {
            mValue = missed_voice_mail_back_led.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_VOICE_MAIL;
            EmotionalLEDEffectUtils.startEmotionalBackLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else {
            return false;
        }
        Log.d("hong", "Click_DB: " + mClickItem + "/Value: " + mValue);
        Settings.System.putInt(getContentResolver(), mClickItem, mValue == true ? 1 : 0);
        loadsettings();

        return true;
    }

    private void setReminderItemDB(boolean mValue) {
        Settings.System.putInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL, mValue == true ? 1 : 0);
        Settings.System
                .putInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES,
                        mValue == true ? 1 : 0);

        if ("USC".equals(Config.getOperator())) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_VOICE_MAIL,
                    mValue == true ? 1 : 0);
        }
    }


    // Regist Notification Observer Func 
    // 2013-05-09
    // A 4Team 2Part Yeom Seungyeop Y
    private void updateNotificationCheckbox() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                "lge_notification_light_pulse", 1) == 1 ? true : false;
        mActionBarSwitch.setChecked(isSilentModeChecked);
        //Log.d(TAG, "Notification LED Status : " + isSilentModeChecked);
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
                //EmotionalLEDEffectUtils.sStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                Log.i("hsmodel",
                        "Battery Status : "
                                + com.android.settings.Utils.getBatteryStatus(getResources(),
                                        intent));
            }
        }
    };

    private void setTalkBackIcon() {
        String greenLED = getActivity().getResources().getString(R.string.color_green) + " " + getActivity().getResources().getString(R.string.settings_led);
        String redLED = getActivity().getResources().getString(R.string.color_red) + " " + getActivity().getResources().getString(R.string.settings_led);
        incoming_back_led.setIconTalkbackDescription(greenLED);
        incall_back_led.setIconTalkbackDescription(redLED);
        missed_voice_mail_back_led.setIconTalkbackDescription(greenLED);
        missed_call_message_back_led.setIconTalkbackDescription(greenLED);
        camera_face_detecting_back_led.setIconTalkbackDescription(greenLED);
    }
}
