package com.android.settings.lge;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;
import android.view.View;

import com.android.settings.R;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

public class ConnectivityPouchNotification extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String KEY_POUCH_MISSED_CALL = "pouch_missed_call";
    private static final String KEY_POUCH_MESSAGE = "pouch_message";
    private static final String KEY_POUCH_VOICE_MESSAGE = "pouch_voice_message";
    private static final String KEY_POUCH_CALENDAR_EVENTS = "pouch_calendar_events";
    private static final String KEY_POUCH_EMAIL = "pouch_email";

    private CheckBoxPreference mPouchMissedCall;
    private CheckBoxPreference mPouchMessage;
    private CheckBoxPreference mPouchVoiceMessage;
    private CheckBoxPreference mPouchCalendarEvents;
    private CheckBoxPreference mPouchEmail;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        ContentResolver resolver = getContentResolver();

        //set layout
        addPreferencesFromResource(R.layout.connectivity_pouch_notification_list);
        //        setContentView(R.xml.connectivity_pouch_notification);
        View header = getLayoutInflater().inflate(R.xml.connectivity_pouch_notification, null);
        getListView().addHeaderView(header, null, false);
        getListView().setHeaderDividersEnabled(false);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //binding
        mPouchMissedCall = (CheckBoxPreference)findPreference(KEY_POUCH_MISSED_CALL);
        mPouchMessage = (CheckBoxPreference)findPreference(KEY_POUCH_MESSAGE);
        mPouchVoiceMessage = (CheckBoxPreference)findPreference(KEY_POUCH_VOICE_MESSAGE);
        mPouchCalendarEvents = (CheckBoxPreference)findPreference(KEY_POUCH_CALENDAR_EVENTS);
        mPouchEmail = (CheckBoxPreference)findPreference(KEY_POUCH_EMAIL);

        //set check status
        mPouchMissedCall.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MISSED_CALL, 0) != 0);
        mPouchMessage.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MESSAGE, 0) != 0);
        mPouchEmail.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_EMAIL, 0) != 0);
        mPouchVoiceMessage.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_VOICE_MESSAGE, 0) != 0);
        mPouchCalendarEvents.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_CALENDAR_EVENTS, 0) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        if (preference == mPouchMissedCall)
        {
            if (!mPouchMissedCall.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MISSED_CALL, 0);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MISSED_CALL, 1);
            }
        }
        else if (preference == mPouchMessage)
        {
            if (!mPouchMessage.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MESSAGE, 0);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_MESSAGE, 1);
            }
        }
        else if (preference == mPouchVoiceMessage)
        {
            if (!mPouchVoiceMessage.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_VOICE_MESSAGE, 0);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_VOICE_MESSAGE, 1);
            }
        }
        else if (preference == mPouchCalendarEvents)
        {
            if (!mPouchCalendarEvents.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_CALENDAR_EVENTS, 0);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_CALENDAR_EVENTS, 1);
            }
        }
        else if (preference == mPouchEmail)
        {
            if (!mPouchEmail.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_EMAIL, 0);
            } else {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH_EMAIL, 1);
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
