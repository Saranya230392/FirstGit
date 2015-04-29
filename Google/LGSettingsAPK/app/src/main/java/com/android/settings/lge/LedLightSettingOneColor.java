package com.android.settings.lge;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SettingsPreferenceFragment;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.lge.constants.SettingsConstants;

import android.app.ActionBar;
import android.view.MenuItem;

public class LedLightSettingOneColor extends SettingsPreferenceFragment implements OnClickListener
{
    private static final String TAG = "LedLightSettingOneColor";
    private static final String FRONT_KEY_SCREEN = "front_key_screen";
    //private static final String FRONT_KEY_ALL = "front_key_all";
    private static final String FRONT_KEY_AREA_MAIL = "front_key_area_mail";
    private static final String CHECK_FRONT_KEY_MISSED_CALL = "checkbox_front_key_missed_call";
    private static final String CHECK_FRONT_KEY_MESSAGING = "checkbox_front_key_messaging";
    private static final String CHECK_FRONT_KEY_GPS = "checkbox_front_key_gps";
    private static final String CHECK_FRONT_KEY_VOICE_MAIL = "checkbox_front_key_voice_mail";
    private static final String CHECK_FRONT_KEY_FELICA = "checkbox_front_key_felica";
    private static final String CHECK_FRONT_KEY_ALARM = "checkbox_front_key_alarm";
    private static final String CHECK_FRONT_KEY_CALANDAR_REMINDER = "checkbox_front_key_calandar_reminder";
    private static final String CHECK_FRONT_KEY_SOCIAL_EVENT = "checkbox_front_key_social_event";
    private static final String CHECK_FRONT_KEY_EMAIL = "checkbox_front_key_email";
    private static final String FRONT_KEY_CELL_BROAD = "front_key_cell_broad";

    private Switch actionBarSwitch;
    private boolean isSilentModeChecked = true;
    private Preference pf_key_area_mail;
    private Preference pf_key_cell_broad;
    //private CheckBoxPreference ck_front_key_all;
    private CheckBoxPreference ck_front_key_missed;
    private CheckBoxPreference ck_front_key_messaging;
    private CheckBoxPreference ck_front_key_voice_mail;
    private CheckBoxPreference ck_front_key_gps;
    private CheckBoxPreference ck_front_key_felica;
    private CheckBoxPreference ck_front_key_alarm;
    private CheckBoxPreference ck_front_key_calendar_reminder;
    private CheckBoxPreference ck_front_key_social_event;
    private CheckBoxPreference ck_front_key_email;

    @Override
    public void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.led_light_setting_one_color);
        PreferenceScreen parent = (PreferenceScreen)findPreference(FRONT_KEY_SCREEN);
        updateTitle();

        pf_key_area_mail = (Preference)findPreference(FRONT_KEY_AREA_MAIL);
        pf_key_cell_broad = (Preference)findPreference(FRONT_KEY_CELL_BROAD);

        //ck_front_key_all = (CheckBoxPreference)findPreference(FRONT_KEY_ALL);
        ck_front_key_missed = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_MISSED_CALL);
        ck_front_key_messaging = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_MESSAGING);
        ck_front_key_voice_mail = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_VOICE_MAIL);
        ck_front_key_gps = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_GPS);
        ck_front_key_felica = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_FELICA);
        ck_front_key_alarm = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_ALARM);
        ck_front_key_calendar_reminder = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_CALANDAR_REMINDER);
        ck_front_key_social_event = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_SOCIAL_EVENT);
        ck_front_key_email = (CheckBoxPreference)findPreference(CHECK_FRONT_KEY_EMAIL);

        //pf_key_area_mail.setSelectable(false);

        /*ck_front_key_all.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_ALL, 0) != 0); */
        ck_front_key_missed.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_MISSED_CALL, 0) != 0);
        ck_front_key_messaging.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_MESSAGING, 0) != 0);
        ck_front_key_voice_mail.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_VOICE_MAIL, 0) != 0);
        ck_front_key_gps.setChecked(Settings.System.getInt(
                getContentResolver(), "front_key_gps", 0) != 0);
        ck_front_key_felica.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_FELICA, 0) != 0);
        ck_front_key_alarm.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_ALARM, 0) != 0);
        ck_front_key_calendar_reminder
                .setChecked(Settings.System.getInt(
                        getContentResolver(), SettingsConstants.System.FRONT_KEY_CALENDAR_REMINDER,
                        0) != 0);
        ck_front_key_social_event.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_SOCIAL_EVENT, 0) != 0);
        ck_front_key_email.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.FRONT_KEY_EMAIL, 0) != 0);

        parent.removePreference(pf_key_area_mail);
        parent.removePreference(pf_key_cell_broad);
        parent.removePreference(ck_front_key_gps);
        parent.removePreference(ck_front_key_felica);
        parent.removePreference(ck_front_key_alarm);
        parent.removePreference(ck_front_key_social_event);
        parent.removePreference(ck_front_key_calendar_reminder);

        //Action Bar Icon Display
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateTitle() {
        actionBarSwitch = new Switch(getActivity());
        final int padding = getActivity().getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);

        actionBarSwitch.setPaddingRelative(0, 0, padding, 0);
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.FRONT_KEY_ALL, 1) == 1 ? true : false;
        actionBarSwitch.setOnClickListener(this);
        actionBarSwitch.setChecked(isSilentModeChecked);
        setlayoutVisible(isSilentModeChecked);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        if ("VZW".equals(Config.getOperator())) {
            getActivity().setTitle(R.string.notification_led_vzw);
        }
        actionBar.setCustomView(actionBarSwitch,
                new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
        actionBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.System.putInt(getContentResolver(),
                            SettingsConstants.System.FRONT_KEY_ALL, 1);
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.NOTIFICATION_LIGHT_PULSE, 1);
                }
                else {
                    Settings.System.putInt(getContentResolver(),
                            SettingsConstants.System.FRONT_KEY_ALL, 0);
                    Settings.System.putInt(getContentResolver(),
                            Settings.System.NOTIFICATION_LIGHT_PULSE, 0);
                }
                Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
                intent.putExtra("name", SettingsConstants.System.FRONT_KEY_ALL);
                getActivity().sendBroadcast(intent);
                setlayoutVisible(isChecked);
            }
        });

        actionBarSwitch.setOnClickListener(new CompoundButton.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void setlayoutVisible(boolean isChecked) {
        Log.i(TAG, "Current Layout mode : " + isChecked);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().setEnabled(isChecked);
        }
    }

    private void updateNotificationFlashOneColor() {
        isSilentModeChecked = Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.FRONT_KEY_ALL, 0) == 1 ? true : false;
        actionBarSwitch.setChecked(isSilentModeChecked);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    { // TODO Auto-generated method stub

        if (preference == ck_front_key_missed)
        {
            if (ck_front_key_missed.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_MISSED_CALL, 1);

            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_MISSED_CALL, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_MISSED_CALL);
            getActivity().sendBroadcast(intent);

        }

        else if (preference == ck_front_key_messaging)
        {
            if (ck_front_key_messaging.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_MESSAGING, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_MESSAGING, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_MESSAGING);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_voice_mail)
        {
            if (ck_front_key_voice_mail.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_VOICE_MAIL, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_VOICE_MAIL, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_VOICE_MAIL);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_gps)
        {
            if (ck_front_key_gps.isChecked()) {
                Settings.System.putInt(getContentResolver(), "front_key_gps", 1);
            } else {
                Settings.System.putInt(getContentResolver(), "front_key_gps", 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", "front_key_gps");
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_felica)
        {
            if (ck_front_key_felica.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_FELICA, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_FELICA, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_FELICA);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_alarm)
        {
            if (ck_front_key_alarm.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_ALARM, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_ALARM, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_ALARM);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_calendar_reminder)
        {
            if (ck_front_key_calendar_reminder.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_CALENDAR_REMINDER, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_CALENDAR_REMINDER, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_CALENDAR_REMINDER);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_social_event)
        {
            if (ck_front_key_social_event.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_SOCIAL_EVENT, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_SOCIAL_EVENT, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_SOCIAL_EVENT);
            getActivity().sendBroadcast(intent);
        }

        else if (preference == ck_front_key_email)
        {
            if (ck_front_key_email.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_EMAIL, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_EMAIL, 0);
            }
            Intent intent = new Intent("com.lge.intent.action.NOTIFICATION_FLASH_CHANGED");
            intent.putExtra("name", SettingsConstants.System.FRONT_KEY_EMAIL);
            getActivity().sendBroadcast(intent);
        }

        else {
            ;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
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
    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }
}
