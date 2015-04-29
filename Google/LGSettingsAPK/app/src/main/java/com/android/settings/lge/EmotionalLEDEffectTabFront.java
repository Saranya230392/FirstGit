package com.android.settings.lge;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
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
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;

public class EmotionalLEDEffectTabFront extends SettingsPreferenceFragment implements Indexable {
    private Switch actionBarSwitch;
    private boolean isSilentModeChecked = true;
    private static final String TAG = "EmotionalLEDEffect";
    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";
    private CheckBoxPreference led_effect_alarm;
    private CheckBoxPreference led_effect_calendar;
    private CheckBoxPreference led_effect_gps;
    private IconTalkbackCheckBoxPreference led_effect_incoming_call;
    private CheckBoxPreference led_effect_download_apps;
    //    private CheckBoxPreference led_effect_missed_reminder;
    private CheckBoxPreference led_effect_favorites_incoming;
    private IconTalkbackCheckBoxPreference led_effect_battery_charging;
    private CheckBoxPreference led_effect_osaifu_keitai;
    private CheckBoxPreference led_effect_voice_recorder;
    private DoubleTitlePreference led_effect_area_mail;
    private CheckBoxPreference reminder_missed_call;
    private CheckBoxPreference reminder_msg;
    private IconTalkbackCheckBoxPreference reminder_voicemail;
    private CheckBoxPreference reminder_email;
    private CheckBoxPreference reminder_cell_broad;
    private IconTalkbackCheckBoxPreference reminder_call_msg;
    private PreferenceCategory mForntLEDCategory;

    private static final String KEY_EMOTIONAL_LED = "emotional_led";
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
    //private static final String EMOTIONAL_LED_REMINDER_DESC = "emotional_led_reminder_missed_desc";
    private static final String EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY = "emotional_led_reminder_missed_call";
    private static final String EMOTIONAL_LED_REMINDER_MSG_KEY = "emotional_led_reminder_msg";
    private static final String EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY = "emotional_led_reminder_voicemail";
    private static final String EMOTIONAL_LED_REMINDER_EMAIL_KEY = "emotional_led_reminder_email";
    private static final String EMOTIONAL_LED_REMINDER_CELL_BROAD = "emotional_led_reminder_cell_broad";
    private static final String EMOTIONAL_LED_REMINDER_CALL_MSG = "emotional_led_reminder_call_msg";

    public boolean mUseCellBroad = true;
    public boolean mUseVoiceMail = true;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    
    private ContentObserver mNotificationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateNotificationCheckbox();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.emotional_led_effect_tab_front);
        updateTitle();

        mForntLEDCategory = (PreferenceCategory)findPreference(FRONT_LED_CATEGORY_KEY);
        led_effect_alarm = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_ALARM_KEY);
        led_effect_calendar = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_CALENDAR_KEY);
        led_effect_gps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_GPS_KEY);
        led_effect_incoming_call = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY);
        led_effect_download_apps = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY);
        //        led_effect_missed_reminder = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_MISSED_REMINDER_KEY);
        led_effect_area_mail = (DoubleTitlePreference)findPreference(EMOTIONAL_LED_EFFECT_AREA_MAIL_KEY);
        led_effect_area_mail.setSubTitle("(" + getString(R.string.sp_emotion_led_always_on) + ")");
        //led_effect_area_mail.setSubTitleGravity(Gravity.END);
        led_effect_favorites_incoming = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_FAVORITES_KEY);
        led_effect_battery_charging = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY);
        led_effect_osaifu_keitai = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_OSAIFU_KEITAI_KEY);
        led_effect_voice_recorder = (CheckBoxPreference)findPreference(EMOTIONAL_LED_EFFECT_VOICE_RECORDER_LED_KEY);
        /*        lgSoftwareLinearLayout = (Preference)findPreference(EMOTIONAL_LED_EFFECT_SELECT_MISSED_EVENT_KEY);
                lgSoftwareLinearLayout.setLayoutResource(R.layout.emotional_select_preference); */
        reminder_missed_call = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY);
        reminder_msg = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MSG_KEY);
        reminder_voicemail = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY);
        reminder_email = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_EMAIL_KEY);
        reminder_cell_broad = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_CELL_BROAD);
        reminder_call_msg = (IconTalkbackCheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_CALL_MSG);
        EmotionalLEDEffectUtils.sPackageName = getActivity().getPackageName();

        PreferenceCategory brightnessCategory = (PreferenceCategory)getPreferenceScreen()
                .findPreference(EMOTIONAL_LED_EFFECT_BRIGHTNESS_CATEGORY_KEY);
        Log.d("hong",
                "LED control config: "
                        + Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_userConfigLedBrightness,
                              "com.lge.R.bool.config_userConfigLedBrightness"));
        
        if (!Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasOneColorLed,
                              "com.lge.R.bool.config_hasOneColorLed")) {
            setlayoutIcon();
            setTalkBackIcon();
        }

        if (!"DCM".equals(Config.getOperator())) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(led_effect_area_mail);
                getPreferenceScreen().removePreference(led_effect_gps);
                if ("KDDI".equals(Config.getOperator())) {
                    getPreferenceScreen().removePreference(reminder_msg);
                    reminder_call_msg.setTitle(
                        R.string.sp_emotional_led_reminder_missed_calls);
                }
                else {
                    getPreferenceScreen().removePreference(led_effect_osaifu_keitai);
                }
            }
            Log.i(TAG, "Area mail : Remove menu");
        }

        loadsettings();

        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(led_effect_favorites_incoming);

            if (!Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_userConfigLedBrightness,
                              "com.lge.R.bool.config_userConfigLedBrightness")) {
                getPreferenceScreen().removePreference(brightnessCategory);
                getPreferenceScreen().removePreference(mForntLEDCategory);
            } else {
                if (mForntLEDCategory != null) {
                    mForntLEDCategory.setTitle(R.string.sp_emotional_led_notification_title);
                }
            }
        }


        if (Utils.checkPackage(reminder_cell_broad.getContext(),
                "com.android.cellbroadcastreceiver"))
        {
            Log.i(TAG, "Cell Broadcasting : Include menu");
        } else {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_cell_broad);
                mUseCellBroad = false;
            }
            Log.i(TAG, "Cell Broadcasting : Remove menu");
        }
        if (!("ATT".equals(Config.getOperator())) && !("VZW".equals(Config.getOperator())))
        {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_voicemail);
                mUseVoiceMail = false;
            }
        }
        if ("KR".equals(Config.getCountry())
                || "HK".equals(Config.getCountry())) {
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_cell_broad);
                mUseCellBroad = false;
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

            if (getPreferenceScreen() != null) {
                getPreferenceScreen().removePreference(reminder_missed_call);
                getPreferenceScreen().removePreference(reminder_msg);
                getPreferenceScreen().removePreference(led_effect_calendar);
                getPreferenceScreen().removePreference(led_effect_alarm);
                getPreferenceScreen().removePreference(led_effect_voice_recorder);
                getPreferenceScreen().removePreference(reminder_email);
                getPreferenceScreen().removePreference(reminder_cell_broad);
                getPreferenceScreen().removePreference(led_effect_gps);
                getPreferenceScreen().removePreference(led_effect_osaifu_keitai);
                getPreferenceScreen().removePreference(led_effect_area_mail);
                if (!Utils.isUI_4_1_model(getActivity())) {
                    getPreferenceScreen().removePreference(reminder_voicemail);
                }
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
        mIsFirst = true;
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
        
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerform = getActivity().getIntent().getBooleanExtra("perform",
                false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerform) {
            startResult();
            mIsFirst = false;
        }
        setlayoutVisible();
    }
    
    public void startResult() {
        if (mSearch_result.equals(KEY_EMOTIONAL_LED)) {
            boolean newValue = getActivity().getIntent().getBooleanExtra(
                    "newValue", false);
            if (newValue) {
                Settings.System.putInt(getContentResolver(),
                        "lge_notification_light_pulse", 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        "lge_notification_light_pulse", 0);
            }
            updateTitle();
        } else if (mSearch_result.equals(EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY)) {
            led_effect_incoming_call.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(EMOTIONAL_LED_REMINDER_CALL_MSG)) {
            reminder_call_msg.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY)) {
            led_effect_battery_charging.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY)) {
            led_effect_download_apps.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY)) {
            reminder_voicemail.performClick(getPreferenceScreen());
        }  
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
        reminder_call_msg.setChecked(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_MISSED_CALL_MSG, 0) == 0 ? false : true);

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
        if (Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            updateSearchItemStatus();
        }
    }
    
    private void updateSearchItemStatus() {
        int enable =  isSilentModeChecked == true ? 1 : 0;
        
        String[] searchLEDItem;
        if ("ATT".equals(Config.getOperator())
                || "VZW".equals(Config.getOperator())) {
            searchLEDItem = new String[] {
                    EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY,
                    EMOTIONAL_LED_REMINDER_CALL_MSG,
                    EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY,
                    EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY,
                    EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY
            };
        } else {
            searchLEDItem = new String[] {
                    EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY,
                    EMOTIONAL_LED_REMINDER_CALL_MSG,
                    EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY,
                    EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY
            };
        }
        
        for (int i = 0; i < searchLEDItem.length; i++) {
            ContentResolver cr = getActivity().getContentResolver();
            ContentValues row = new ContentValues();
            row.put("current_enable", enable);
            String where = "data_key_reference = ? AND class_name = ?";
            cr.update(Uri.parse(CONTENT_URI), row, where,
                    new String[] { searchLEDItem[i],
                            EmotionalLEDEffectTabFront.class.getName() });
        }
    }

    private void setlayoutIcon() {
        led_effect_area_mail.setIcon(R.drawable.img_emotionl_led_red_noti);
        led_effect_alarm.setIcon(R.drawable.img_emotionl_led_alarm_noti);
        led_effect_calendar.setIcon(R.drawable.img_emotionl_led_calendar_noti);
        led_effect_gps.setIcon(R.drawable.img_emotionl_led_gps_noti);
        led_effect_incoming_call.setIcon(R.drawable.img_emotionl_led_incoming_noti);
        led_effect_download_apps.setIcon(R.drawable.img_emotionl_led_downloaded_noti);
        led_effect_favorites_incoming.setIcon(R.drawable.img_emotionl_led_incoming2_noti);
        led_effect_battery_charging.setIcon(R.drawable.img_emotionl_led_red_noti);
        led_effect_osaifu_keitai.setIcon(R.drawable.img_emotionl_led_felica_noti);
        led_effect_voice_recorder.setIcon(R.drawable.img_emotionl_led_red_noti);
        reminder_missed_call.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        reminder_msg.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        reminder_voicemail.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        reminder_email.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        reminder_cell_broad.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        reminder_call_msg.setIcon(R.drawable.img_emotionl_led_missed_event_noti);
        if ("DCM".equals(Config.getOperator())) {
            reminder_missed_call.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_msg.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_voicemail.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_email.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            reminder_cell_broad.setIcon(R.drawable.img_emotionl_led_calendar_noti);
            //led_effect_missed_reminder.setIcon(R.drawable.img_emotionl_led_missed_event_dcm);
            //led_effect_incoming_call.setSummary(R.string.sp_emotional_led_incoming_call_summary_dcm);
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
            startEmotionalLED(mValue, EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI );
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
        } else if (preference == reminder_call_msg) {
            mValue = reminder_call_msg.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_MISSED_CALL_MSG;
            setReminderItemDB(mValue);
            EmotionalLEDEffectUtils.startEmotionalLED(mValue, EmotionalLEDEffectUtils.EMOTIONAL_LED_MISSED_EVENT_REMINDER_NOTI, getActivity());
        } else {
            return false;
        }
        Log.d("hong", "Click_DB: " + mClickItem + "/Value: " + mValue);
        Settings.System.putInt(getContentResolver(), mClickItem, mValue == true ? 1 : 0);
        loadsettings();

        return true;
    }

    private void setReminderItemDB(boolean mValue) {
        Settings.System
                .putInt(getContentResolver(),
                        SettingsConstants.System.EMOTIONAL_LED_REMINDER_MISSED_CALL,
                        mValue == true ? 1 : 0);
        Settings.System.putInt(getContentResolver(),
                SettingsConstants.System.EMOTIONAL_LED_REMINDER_MSG, mValue == true ? 1 : 0);
        if (mUseCellBroad) {
            Intent intent = new Intent("com.lge.intent.action.EMOTIONAL_LED_CHANGE_CB");
            if (mValue) {
                intent.putExtra("name", "ON");
            } else {
                intent.putExtra("name", "OFF");
            }
            getActivity().sendBroadcast(intent);
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_CELL_BROAD, mValue == true ? 1
                            : 0);
        }

        if ("USC".equals(Config.getOperator())) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL, mValue == true ? 1
                            : 0);
        }
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

    private void setTalkBackIcon() {
        String greenLED = getActivity().getResources().getString(R.string.color_green) + " " + getActivity().getResources().getString(R.string.settings_led);
        String redLED = getActivity().getResources().getString(R.string.color_red) + " " + getActivity().getResources().getString(R.string.settings_led);
        led_effect_incoming_call.setIconTalkbackDescription(greenLED);
        reminder_call_msg.setIconTalkbackDescription(greenLED);
        reminder_voicemail.setIconTalkbackDescription(greenLED);
        led_effect_battery_charging.setIconTalkbackDescription(redLED);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            
            int isSwitchChecked = Settings.System.getInt(context.getContentResolver(),
                    "lge_notification_light_pulse", 1);
            String ledTitle;
            String downloadSummary;
            if ("VZW".equals(Config.getOperator())) {
                ledTitle = context.getString(R.string.notification_led_vzw);
                downloadSummary = context.getString(R.string.sp_emotional_led_download_apps_3_ex_vzw);
            } else {
                ledTitle = context.getString(R.string.notification_led);
                downloadSummary = context.getString(R.string.sp_emotional_led_download_apps_3_ex);
            }
            String screenTitle = context.getString(R.string.notification_settings)
                    + " > "
                    + ledTitle;
            int visible = Config.getFWConfigInteger(context,
                    com.lge.R.integer.config_emotionalLedType,
                    "com.lge.R.integer.config_emotionalLedType");
            if (visible == 2) {
                visible = 1;
            }

            setSearchIndexData(context, EMOTIONAL_LED_EFFECT_INCOMING_CALL_KEY,
                    context.getString(R.string.sp_emotional_led_incoming_call),
                    screenTitle, null, null, "com.lge.settings.EMOTIONAL_LED",
                    null, null, isSwitchChecked, "CheckBox", "System",
                    SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL, visible, 0);
            setSearchIndexData(
                    context,
                    EMOTIONAL_LED_REMINDER_CALL_MSG,
                    context.getString(R.string.display_led_missed_call_messages),
                    screenTitle, null, null, "com.lge.settings.EMOTIONAL_LED",
                    null, null, isSwitchChecked, "CheckBox", "System",
                    SettingsConstants.System.EMOTIONAL_LED_MISSED_CALL_MSG, visible,
                    0);
            setSearchIndexData(
                    context,
                    EMOTIONAL_LED_EFFECT_BATTERY_CHARGING_KEY,
                    context.getString(R.string.sp_home_button_battery_charging),
                    screenTitle, null, null, "com.lge.settings.EMOTIONAL_LED",
                    null, null, isSwitchChecked, "CheckBox", "System",
                    SettingsConstants.System.EMOTIONAL_LED_BATTERY_CHARGING, visible,
                    0);
            setSearchIndexData(context, EMOTIONAL_LED_EFFECT_DOWNLOAD_APPS_KEY,
                    context.getString(R.string.sp_emotional_led_download_apps),
                    screenTitle, downloadSummary, null,
                    "com.lge.settings.EMOTIONAL_LED", null, null,
                    isSwitchChecked, "CheckBox", "System",
                    Settings.System.NOTIFICATION_LIGHT_PULSE, visible, 0);
            if ("ATT".equals(Config.getOperator())
                    || "VZW".equals(Config.getOperator())) {
                setSearchIndexData(context, EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY,
                        context.getString(R.string.display_led_missed_voice),
                        screenTitle, null, null, "com.lge.settings.EMOTIONAL_LED",
                        null, null, isSwitchChecked, "CheckBox", "System",
                        SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL,
                        visible, 0);
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
