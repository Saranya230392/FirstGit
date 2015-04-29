package com.android.settings;

import java.util.ArrayList;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

public class VibrateTypePreference extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    //db
    public static final String DISTINCTIVE_VIBRATION_INCOMING_CALLS = "distinctive_vibration_incoming_calls";
    public static final String DISTINCTIVE_VIBRATION_MESSAGING = "distinctive_vibration_messaging";
    public static final String DISTINCTIVE_VIBRATION_EMAIL = "distinctive_vibration_email";
    public static final String DISTINCTIVE_VIBRATION_ALARM = "distinctive_vibration_alarm";
    public static final String DISTINCTIVE_VIBRATION_CALENDAR = "distinctive_vibration_calendar";

    //key
    private final static String KEY_IMCOMMING = "incomming_vibrate";
    private final static String KEY_MESSAGE = "message_vibrate";
    private final static String KEY_EMAIL = "email_vibrate";
    private final static String KEY_ALARM = "alarm_vibrate";
    private final static String KEY_CALENDAR = "calendar_vibrate";

    private final static int DEFAULT_VALUE = 1;
    private final static int INCOMMING_CALL_DEFAULT_VALUE = 2;

    private final static String TAG = "VibrateTypePreference";

    private VibrateTypeListPreference mIncomming_vibrateType;
    private VibrateTypeListPreference mMessage_vibrateType;
    private VibrateTypeListPreference mEmail_vibrateType;
    private VibrateTypeListPreference mAlarm_vibrateType;
    private VibrateTypeListPreference mCalendarPreference;
    private VibrateTypeListPreference currentPreference = null;

    CharSequence[] vibrate_type_entry;
    CharSequence[] vibrate_type_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sp_vibrate_type_title_SHORT);
        }

        // set layout
        addPreferencesFromResource(R.xml.vibrate_type);
        mIncomming_vibrateType = (VibrateTypeListPreference)findPreference(KEY_IMCOMMING);
        mMessage_vibrateType = (VibrateTypeListPreference)findPreference(KEY_MESSAGE);
        mEmail_vibrateType = (VibrateTypeListPreference)findPreference(KEY_EMAIL);
        mAlarm_vibrateType = (VibrateTypeListPreference)findPreference(KEY_ALARM);
        mCalendarPreference = (VibrateTypeListPreference)findPreference(KEY_CALENDAR);

        setListener(); // set listener
        steArrayList(); // set arrayList
        setEntiesValues(); // setEntiesValues
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateState();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (null != currentPreference) {
            currentPreference.vibrateStop();
        }
    }

    /** Called when the activity is first created. */

    private void steArrayList() {
        vibrate_type_entry = getResources().getStringArray(R.array.preferences_labels);
        vibrate_type_value = getResources().getStringArray(R.array.preferences_values);
    }

    private void setListener() {
        //listener
        mIncomming_vibrateType.setOnPreferenceChangeListener(this);
        mMessage_vibrateType.setOnPreferenceChangeListener(this);
        mEmail_vibrateType.setOnPreferenceChangeListener(this);
        mAlarm_vibrateType.setOnPreferenceChangeListener(this);
        mCalendarPreference.setOnPreferenceChangeListener(this);
    }

    private void setEntiesValues() {
        mIncomming_vibrateType.setEntries(vibrate_type_entry);
        mMessage_vibrateType.setEntries(vibrate_type_entry);
        mEmail_vibrateType.setEntries(vibrate_type_entry);
        mAlarm_vibrateType.setEntries(vibrate_type_entry);
        mCalendarPreference.setEntries(vibrate_type_entry);

        mIncomming_vibrateType.setEntryValues(vibrate_type_value);
        mMessage_vibrateType.setEntryValues(vibrate_type_value);
        mEmail_vibrateType.setEntryValues(vibrate_type_value);
        mAlarm_vibrateType.setEntryValues(vibrate_type_value);
        mCalendarPreference.setEntryValues(vibrate_type_value);
    }

    private void setSummary() {
        // vibrate type get DB & setvalue
        mIncomming_vibrateType.setValue(String.valueOf(getCall_VibrateType()));
        mMessage_vibrateType.setValue(String.valueOf(getMessage_VibrateType()));
        mEmail_vibrateType.setValue(String.valueOf(getEmail_VibrateType()));
        mAlarm_vibrateType.setValue(String.valueOf(getAlarm_VibrateType()));
        mCalendarPreference.setValue(String.valueOf(getCalendar_VibrateType()));

        // all type set summary
        mIncomming_vibrateType.setSummary(mIncomming_vibrateType.getEntry());
        mMessage_vibrateType.setSummary(mMessage_vibrateType.getEntry());
        mEmail_vibrateType.setSummary(mEmail_vibrateType.getEntry());
        mAlarm_vibrateType.setSummary(mAlarm_vibrateType.getEntry());
        mCalendarPreference.setSummary(mCalendarPreference.getEntry());
    }

    /**
     * Activity UI Update - All summary
     */
    private void updateState() {
        setSummary();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        currentPreference = (VibrateTypeListPreference)preference;
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        final String key = preference.getKey();
        int value = Integer.parseInt((String)newValue);
        if (KEY_IMCOMMING.equals(key)) {
            setCall_VibrateType(value);
        } else if (KEY_MESSAGE.equals(key)) {
            setMessage_VibrateType(value);
        } else if (KEY_EMAIL.equals(key)) {
            setEmail_VibrateType(value);
        } else if (KEY_ALARM.equals(key)) {
            setAlarm_VibrateType(value);
        } else if (KEY_CALENDAR.equals(key)) {
            setCalendar_VibrateType(value);
        }
        updateState();
        return false;
    }

    /**
     * Get Call VibrateType[1~4],
     */
    private int getCall_VibrateType() {
        int type = Settings.System.getInt(getContentResolver(),
                DISTINCTIVE_VIBRATION_INCOMING_CALLS, INCOMMING_CALL_DEFAULT_VALUE);
        if (true == typeIntegrityCheck(type)) {
            return type;
        }
        Log.e(TAG, "get call vibrate value is bad[" + type + "]");
        setCall_VibrateType(INCOMMING_CALL_DEFAULT_VALUE);
        return INCOMMING_CALL_DEFAULT_VALUE;
    }

    /**
     * Get Message VibrateType[1~4],
     */
    private int getMessage_VibrateType() {
        int type = Settings.System.getInt(getContentResolver(), DISTINCTIVE_VIBRATION_MESSAGING,
                DEFAULT_VALUE);
        if (true == typeIntegrityCheck(type)) {
            return type;
        }
        Log.e(TAG, "get call vibrate value is bad[" + type + "]");
        setMessage_VibrateType(DEFAULT_VALUE);
        return DEFAULT_VALUE;
    }

    /**
     * Get Email VibrateType[1~4],
     */
    private int getEmail_VibrateType() {
        int type = Settings.System.getInt(getContentResolver(), DISTINCTIVE_VIBRATION_EMAIL,
                DEFAULT_VALUE);
        if (true == typeIntegrityCheck(type)) {
            return type;
        }
        Log.e(TAG, "get call vibrate value is bad[" + type + "]");
        setEmail_VibrateType(DEFAULT_VALUE);
        return DEFAULT_VALUE;
    }

    /**
     * Get Alarm VibrateType[1~4],
     */
    private int getAlarm_VibrateType() {
        int type = Settings.System.getInt(getContentResolver(), DISTINCTIVE_VIBRATION_ALARM,
                DEFAULT_VALUE);
        if (true == typeIntegrityCheck(type)) {
            return type;
        }
        Log.e(TAG, "get call vibrate value is bad[" + type + "]");
        setAlarm_VibrateType(DEFAULT_VALUE);
        return DEFAULT_VALUE;
    }

    /**
     * Get Calendar VibrateType[1~4],
     */
    private int getCalendar_VibrateType() {
        int type = Settings.System.getInt(getContentResolver(), DISTINCTIVE_VIBRATION_CALENDAR,
                DEFAULT_VALUE);
        if (true == typeIntegrityCheck(type)) {
            return type;
        }
        Log.e(TAG, "get call vibrate value is bad[" + type + "]");
        setCalendar_VibrateType(DEFAULT_VALUE);
        return DEFAULT_VALUE;
    }

    private boolean typeIntegrityCheck(int type) {
        if (type > 0 && type < 6) {
            return true;
        }
        return false;
    }

    /**
     * Set Call VibrateType[1~4]
     */
    private void setCall_VibrateType(int type) {
        if (true == typeIntegrityCheck(type)) {
            Settings.System
                    .putInt(getContentResolver(), DISTINCTIVE_VIBRATION_INCOMING_CALLS, type);
        }
        else {
            Log.e(TAG, "Imcomming call vibrtate type - bad Value[" + type + "]");
        }
    }

    /**
     * Set Message VibrateType[1~4]
     */
    private void setMessage_VibrateType(int type) {
        if (true == typeIntegrityCheck(type)) {
            Settings.System.putInt(getContentResolver(), DISTINCTIVE_VIBRATION_MESSAGING, type);
        }
        else {
            Log.e(TAG, "Message vibrtate type - bad Value[" + type + "]");
        }
    }

    /**
     * Set Email VibrateType[1~4]
     */
    private void setEmail_VibrateType(int type) {
        if (true == typeIntegrityCheck(type)) {
            Settings.System.putInt(getContentResolver(), DISTINCTIVE_VIBRATION_EMAIL, type);
        }
        else {
            Log.e(TAG, "Email vibrtate type - bad Value[" + type + "]");
        }
    }

    /**
     * Set Alarm VibrateType[1~4]
     */
    private void setAlarm_VibrateType(int type) {
        if (true == typeIntegrityCheck(type)) {
            Settings.System.putInt(getContentResolver(), DISTINCTIVE_VIBRATION_ALARM, type);
        }
        else {
            Log.e(TAG, "Alarm vibrtate type - bad Value[" + type + "]");
        }
    }

    /**
     * Set Calendar VibrateType[1~4]
     */
    private void setCalendar_VibrateType(int type) {
        if (true == typeIntegrityCheck(type)) {
            Settings.System.putInt(getContentResolver(), DISTINCTIVE_VIBRATION_CALENDAR, type);
        }
        else {
            Log.e(TAG, "Calendar vibrtate type - bad Value[" + type + "]");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
