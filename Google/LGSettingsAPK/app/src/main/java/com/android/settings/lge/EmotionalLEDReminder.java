package com.android.settings.lge;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

import android.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import com.android.settings.Utils;

public class EmotionalLEDReminder extends PreferenceActivity {
    private static final String TAG = "EmotionalLEDReminder";

    private CheckBoxPreference reminder_missed_call;
    private CheckBoxPreference reminder_msg;
    private CheckBoxPreference reminder_voicemail;
    private CheckBoxPreference reminder_email;
    private CheckBoxPreference reminder_cell_broad;
    //private CheckBoxPreference reminder_felica;
    private Preference reminder_desc;

    private static final String EMOTIONAL_LED_REMINDER_DESC = "emotional_led_reminder_missed_desc";
    private static final String EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY = "emotional_led_reminder_missed_call";
    private static final String EMOTIONAL_LED_REMINDER_MSG_KEY = "emotional_led_reminder_msg";
    private static final String EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY = "emotional_led_reminder_voicemail";
    private static final String EMOTIONAL_LED_REMINDER_EMAIL_KEY = "emotional_led_reminder_email";
    private static final String EMOTIONAL_LED_REMINDER_CELL_BROAD = "emotional_led_reminder_cell_broad";

    //private static final String EMOTIONAL_LED_REMINDER_FELICA = "emotional_led_reminder_felica";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.emotional_led_reminder);
        reminder_desc = (Preference)findPreference(EMOTIONAL_LED_REMINDER_DESC);
        reminder_missed_call = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MISSED_CALL_KEY);
        reminder_msg = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_MSG_KEY);
        reminder_voicemail = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_VOICEMAIL_KEY);
        reminder_email = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_EMAIL_KEY);
        reminder_cell_broad = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_CELL_BROAD);
        //reminder_felica = (CheckBoxPreference)findPreference(EMOTIONAL_LED_REMINDER_FELICA);
        if (!("ATT".equals(Config.getOperator())) && !("USC".equals(Config.getOperator())))
        {
            if (getPreferenceScreen() != null) {
                this.getPreferenceScreen().removePreference(reminder_voicemail);
            }
        }

        if (Utils.checkPackage(reminder_cell_broad.getContext(),
                "com.android.cellbroadcastreceiver"))
        {
            Log.i(TAG, "Cell Broadcasting : Include menu");
        } else {
            if (getPreferenceScreen() != null) {
                this.getPreferenceScreen().removePreference(reminder_cell_broad);
            }
            Log.i(TAG, "Cell Broadcasting : Remove menu");
        }

        if ("DCM".equals(Config.getOperator()))
        {
            Log.i(TAG, "Felica : Include menu");
            reminder_desc.setTitle(R.string.sp_home_button_led_reminder_summary_dcm);
        } else {
            /*if (getPreferenceScreen() != null) {
                this.getPreferenceScreen().removePreference(reminder_felica);
            }*/
            Log.i(TAG, "Felica : Remove menu");
        }
        if ("KR".equals(Config.getCountry())) {
            if (getPreferenceScreen() != null) {
                this.getPreferenceScreen().removePreference(reminder_cell_broad);
            }
        }
        loadsettings();

        //Action Bar Icon Display
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (Config.getFWConfigInteger(this, com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
                actionBar.setIcon(R.drawable.img_emotionl_led_main);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean mValue = false;
        String mClickItem;

        if (preference == reminder_missed_call) {
            mValue = reminder_missed_call.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_MISSED_CALL;
        } else if (preference == reminder_msg) {
            mValue = reminder_msg.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_MSG;
        } else if (preference == reminder_voicemail) {
            mValue = reminder_voicemail.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL;
        } else if (preference == reminder_email) {
            mValue = reminder_email.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_EMAIL;
        } else if (preference == reminder_cell_broad) {
            mValue = reminder_cell_broad.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_CELL_BROAD;
        } /*else if (preference == reminder_felica) {
            mValue = reminder_felica.isChecked();
            mClickItem = SettingsConstants.System.EMOTIONAL_LED_REMINDER_FELICA;
          }*/ else {
            return false;
        }

        Settings.System.putInt(getContentResolver(), mClickItem, mValue == true ? 1 : 0);
        loadsettings();

        return true;
    }

    private void loadsettings() {
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

        /*reminder_felica.setChecked(Settings.System.getInt(getContentResolver(),
                                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_FELICA, 0) == 0 ? false : true);*/
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
}
