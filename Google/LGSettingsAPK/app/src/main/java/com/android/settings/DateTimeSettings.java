/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.admin.DevicePolicyManager;
import android.util.Log;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import android.telephony.TelephonyManager;
//[S][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.content.res.Configuration;
//[E][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
//RTC force sync start
import android.os.SystemProperties;
//RTC force sync end
import android.os.Build;
//[2012.05.25][Jaeyoon.hyun] Add sui start
import com.lge.sui.widget.control.SUIDrumDatePicker;
import com.lge.sui.widget.control.SUIDrumTimePicker;
import com.lge.sui.widget.dialog.SUIDrumDatePickerDialog;
import com.lge.sui.widget.dialog.SUIDrumTimePickerDialog;
//[2012.05.25][Jaeyoon.hyun] Add sui End
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.SettingsConstants;
// RTC force Sync Start
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
// RTC force sync end
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/*LGSI_CHANGE_S:Handeling the TimeZone issues for viethnam and turkey
2011-03-26,manikanta.varmak@lge.com,
Hard codeing the time zone name for "Indochina Time" in Vietnam language
and "Istanbul" for turkey as it is not provided by google
Repository : LG_apps_master*/
import java.util.Locale;
/*LGSI_CHANGE_E:Handeling the TimeZone issues for viethnam and turkey*/
import java.util.TimeZone;
import android.telephony.ServiceState;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;

import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import libcore.icu.TimeZoneNames;
import android.preference.PreferenceCategory;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class DateTimeSettings extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener,
        SUIDrumDatePickerDialog.OnDateSetListener, SUIDrumTimePickerDialog.OnTimeSetListener, Indexable {
    private static final String LOG_TAG = "Date and Time Settings";
    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    // Used for showing the current date format, which looks like "12/31/2010", "2010/12/13", etc.
    // The date value is dummy (independent of actual date).
    private Calendar mDummyDate;

    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_AUTO_TIME = "auto_time";
    private static final String KEY_AUTO_TIME_ZONE = "auto_zone";
    private static final String KEY_TIME_ROAMING = "time_roaming";

    //jaeyoon.hyun [2012.03.19] remove set time menu on CDMA
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_TIME_ZONE = "timezone";
    //jaeyoon.hyun [2012.03.19] remove set time menu on CDMA
    private static final String KEY_DUAL_CLOCK = "troamingdualclock";

    private static final int DIALOG_DATEPICKER = 0;
    private static final int DIALOG_TIMEPICKER = 1;
    private static final int DIALOG_DUALCLOCK = 2;
    // have we been launched from the setup wizard?
    protected static final String EXTRA_IS_FIRST_RUN = "firstRun";

    private static final String KEY_CATEGORY_BASIC = "key_basic";
    private static final String KEY_CATEGORY_FORMAT = "key_date_time_format";

    private CheckBoxPreference mAutoTimePref;
    private Preference mTimePref;
    private Preference mTime24Pref;
    private CheckBoxPreference mAutoTimeZonePref;
    private Preference mTimeZone;
    private Preference mDatePref;
    private ListPreference mDateFormat;
    private Preference mDualClock;
    private Preference mTimeRoaming;
    private PreferenceCategory mBasicCategory;
    private PreferenceCategory mFormatCategory;
    //RTC force sync start
    //private static final String SEND_SYNC_PATH = "/sys/devices/platform/rs300000a7.65536/send_sync";
    private String chipset = "Unknown";
    //RTC force sync end
    private boolean isRoaming = false;

    //LGE_UPDATE_S jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone
    private static final String TIMEUPDATE_PROPERTY = "persist.radio.timeupdate"; // "ril.gsm.timeupdate";
    //LGE_UPDATE_E jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone
    private static String operator_code;

    private Dialog dialog;

    public static final int DATE_13 = 13;
    public static final int DATE_31 = 31;
    public static final int DATE_1970 = 1970;
    public static final int DATE_2037 = 2037;
    public static final int DATE_1 = 1;
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    private PhoneStateIntentReceiver mPhoneStateReceiver;
    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case EVENT_SERVICE_STATE_CHANGED:
                updateServiceState();
                break;

            default:
                break;

            }
        }
    };

    private static final int EVENT_SERVICE_STATE_CHANGED = 300;
    ServiceState mServiceState;

    private final void updateServiceState() {
        mServiceState = mPhoneStateReceiver.getServiceState();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.date_time_prefs);
        //int activePhoneType = TelephonyManager.getDefault().getPhoneType();
        chipset = SystemProperties.get("ro.board.platform", "Unknown");
        //Log.i(LOG_TAG, "chipset: " + chipset);
        operator_code = Config.getOperator();

        initUI();
        if (com.lge.os.Build.LGUI_VERSION.RELEASE < com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            do_settingsUI4_1();
        }
        //jaeyoon.hyun [2012.03.19] remove set time menu on CDMA
        // [goodluck@lge.com][2012.12.11][US780] Remove set time menu of FX1(F7-US780)
        // [goodluck@lge.com][2012.12.12][VS870] Remove set time menu of L1V(VS870)
        if ("w3c".equals(Build.DEVICE)
                || ("w5c".equals(Build.DEVICE) && !"w5c_vzw".equals(Build.PRODUCT))) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 1);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
            getPreferenceScreen().removePreference(findPreference(KEY_AUTO_TIME));
            getPreferenceScreen().removePreference(findPreference(KEY_AUTO_TIME_ZONE));
            getPreferenceScreen().removePreference(findPreference(KEY_DATE));
            getPreferenceScreen().removePreference(findPreference(KEY_TIME));
            getPreferenceScreen().removePreference(findPreference(KEY_TIME_ZONE));
        }
        mPhoneStateReceiver = new PhoneStateIntentReceiver(getActivity(), mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);

        mServiceState = mPhoneStateReceiver.getServiceState();
        Log.d("starmotor", "serviceState.getRoaming() :" + mServiceState.getRoaming());
        Log.d("starmotor", "operator_code : " + operator_code);

        if (!TelephonyManager.getDefault().isNetworkRoaming()) {
            mTimeRoaming.setEnabled(false);
        }

        if (!"dsda".equals(SystemProperties.get("persist.radio.multisim.config"))) {
            if (!"CTC".equals(operator_code) && !"CTO".equals(operator_code)) {
                getPreferenceScreen().removePreference(findPreference("time_roaming"));
            }
        }
        //jaeyoon.hyun [2012.03.19] remove set time menu on CDMA
        //Log.e("JJJJJ", ""+SystemProperties.get("ro.product.model"));
        //        if ("LG-F260S".equals(SystemProperties.get("ro.product.model")) || "LG-F240S".equals(SystemProperties.get("ro.product.model"))) {
        if ("SKT".equals(operator_code)) {
            Log.i(LOG_TAG, "" + operator_code);
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_DUAL_CLOCK));
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-363]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addDateTimeSettingChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        mIsFirst = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            switchTosearchResults();
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchTosearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            inflater.inflate(R.menu.settings_search, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initUI() {
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);

        Intent intent = getActivity().getIntent();
        boolean isFirstRun = intent.getBooleanExtra(EXTRA_IS_FIRST_RUN, false);

        mDummyDate = Calendar.getInstance();
        //[S][2011.12.05][jaeyoon.hyun@lge.com][P860_D1LA][TD:102386] Sync Date format summary and dialog list
        mDummyDate.set(mDummyDate.get(Calendar.YEAR), 0, 31, 13, 0, 0);
        //[E][2011.12.05][jaeyoon.hyun@lge.com][P860_D1LA][TD:102386] Sync Date format summary and dialog list
        mBasicCategory = (PreferenceCategory)findPreference(KEY_CATEGORY_BASIC);
        mFormatCategory = (PreferenceCategory)findPreference(KEY_CATEGORY_FORMAT);

        mAutoTimePref = (CheckBoxPreference)findPreference(KEY_AUTO_TIME);

        DevicePolicyManager dpm = (DevicePolicyManager)getSystemService(Context
                .DEVICE_POLICY_SERVICE);
        if (dpm.getAutoTimeRequired()) {
            mAutoTimePref.setEnabled(false);

            // If Settings.Global.AUTO_TIME is false it will be set to true
            // by the device policy manager very soon.
            // Note that this app listens to that change.
        }

        mAutoTimePref.setChecked(autoTimeEnabled);
        mAutoTimeZonePref = (CheckBoxPreference)findPreference(KEY_AUTO_TIME_ZONE);
        // Override auto-timezone if it's a wifi-only device or if we're still in setup wizard.
        // TODO: Remove the wifiOnly test when auto-timezone is implemented based on wifi-location.
        if (Utils.isWifiOnly(getActivity()) || isFirstRun) {
            getPreferenceScreen().removePreference(mAutoTimeZonePref);
            autoTimeZoneEnabled = false;
        }
        mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);

        mTimePref = findPreference("time");
        mTime24Pref = findPreference("24 hour");
        mTimeZone = findPreference("timezone");
        mDatePref = findPreference("date");
        //[S][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        mDualClock = findPreference("troamingdualclock");
        mTimeRoaming = findPreference("time_roaming");
        //[E][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        mDateFormat = (ListPreference)findPreference(KEY_DATE_FORMAT);
        if (isFirstRun) {
            getPreferenceScreen().removePreference(mTime24Pref);
            getPreferenceScreen().removePreference(mDateFormat);
        }

        String[] dateFormats = getResources().getStringArray(R.array.date_format_values);
        String[] formattedDates = new String[dateFormats.length];
        String currentFormat = getDateFormat();
        // Initialize if DATE_FORMAT is not set in the system settings
        // This can happen after a factory reset (or data wipe)
        if (currentFormat == null) {
            currentFormat = "MM-dd-yyyy";
        }

        // Prevents duplicated values on date format selector.
        mDummyDate.set(mDummyDate.get(Calendar.YEAR), mDummyDate.DECEMBER, DATE_31, DATE_13, 0, 0);

        for (int i = 0; i < formattedDates.length; i++) {
            String formatted =
                    DateFormat.getDateFormatForSetting(getActivity(), dateFormats[i])
                            .format(mDummyDate.getTime());
            //[S][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
            formatted = getKoreanFormat(formatted);

            //[E][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
            if (dateFormats[i].length() == 0) {
                formattedDates[i] = getResources().
                        getString(R.string.normal_date_format, formatted);
            } else {
                formattedDates[i] = formatted;
            }
        }

        mDateFormat.setEntries(formattedDates);
        mDateFormat.setEntryValues(R.array.date_format_values);
        mDateFormat.setValue(currentFormat);

        mTimePref.setEnabled(!autoTimeEnabled);
        mDatePref.setEnabled(!autoTimeEnabled);
        mTimeZone.setEnabled(!autoTimeZoneEnabled);
        Log.i(LOG_TAG, "initUI  autoTimeZoneEnabled: " + autoTimeZoneEnabled);
        if (!(UserHandle.myUserId() == UserHandle.USER_OWNER)) {
            mAutoTimePref.setEnabled(false);
            mAutoTimeZonePref.setEnabled(false);
            mDatePref.setEnabled(false);
            mTimePref.setEnabled(false);
            mTimeZone.setEnabled(false);            
            mTime24Pref.setEnabled(false);
            mDateFormat.setEnabled(false);
        }
    }
    private void do_settingsUI4_1() {
        getPreferenceScreen().removePreference(mBasicCategory);
        getPreferenceScreen().removePreference(mFormatCategory);
        mAutoTimePref.setOrder(15);
        mAutoTimeZonePref.setOrder(16);
        mDatePref.setOrder(17);
        mTimePref.setOrder(18);
        mTimeZone.setOrder(19);
        mDualClock.setOrder(20);
        mTime24Pref.setOrder(21);
        mDateFormat.setOrder(22);
        mTimeRoaming.setOrder(23);
    }
    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        ((CheckBoxPreference)mTime24Pref).setChecked(is24Hour());
        //[S][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        isRoaming = "true".equals(SystemProperties
                .get(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING));
        if (mDualClock != null) {
            mDualClock.setEnabled(isRoaming);
        }
        if (isRoaming) {
            if (mDualClock != null) {
                mDualClock.setSummary(getClockSummaryIndex());
            }
        }
        else {
            if (mDualClock != null) {
                mDualClock.setSummary(R.string.troaming_overseasnetwork);
            }
        }
        //[E][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay(getActivity());

        Dialog mDialog = dialog;
        if (mDialog != null) {
            if (mDialog instanceof SUIDrumDatePickerDialog) {
                ((SUIDrumDatePickerDialog)mDialog).getDatePicker().refresh();
            } else if (mDialog instanceof SUIDrumTimePickerDialog) {
                ((SUIDrumTimePickerDialog)mDialog).getTimePicker().refresh();
            }
        }

        updateServiceState();
        mPhoneStateReceiver.registerIntent();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-363]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance()
                    .setDateAndTimeMenu(mAutoTimePref, mDatePref, mTimePref);
            MDMSettingsAdapter.getInstance().setTimeZoneMenu(mAutoTimeZonePref, mTimeZone);
        }
        // LGMDM_END
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            Log.d("skku", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void startResult(boolean newValue) { 
        if (mSearch_result.equals(KEY_AUTO_TIME)) {
        //    mAutoTimePref.performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_DATE)) {
            mDatePref.performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_TIME)) {
            findPreference(KEY_TIME).performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_AUTO_TIME_ZONE)) {
       //     mAutoTimeZonePref.performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_TIME_ZONE)) { 
        } else if (mSearch_result.equals(KEY_DUAL_CLOCK)) {
            findPreference(KEY_DUAL_CLOCK).performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_TIME_ROAMING)) { 
        } else if (mSearch_result.equals("24 hour")) {
        //    mTime24Pref.performClick(getPreferenceScreen()); 
        } else if (mSearch_result.equals(KEY_DATE_FORMAT)) {
            mDateFormat.performClick(getPreferenceScreen()); 
        }
    }

    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d("skku", "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d("skku", "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
                "com.android.settings.PrivacySettings" });
    }


    //RTC Force sync start
    /*       private void sendSyncWrite(){
                  try{
                          BufferedWriter bwSendSync = new BufferedWriter(new FileWriter(SEND_SYNC_PATH));
                          bwSendSync.write(Integer.toString(1));
                          bwSendSync.flush();
                          bwSendSync.close();
                  }catch(IOException e){
                          e.printStackTrace();
                  }
          }*/
    //RTC Force Sync end
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
        mPhoneStateReceiver.unregisterIntent();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updateTimeAndDateDisplay(Context context) {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(context);
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        // We use December 31st because it's unambiguous when demonstrating the date format.
        // We use 13:00 so we can demonstrate the 12/24 hour options.
        mDummyDate.set(now.get(Calendar.YEAR), 0, 31, 13, 0, 0);
        //Date dummyDate = mDummyDate.getTime();
        mTimePref.setSummary(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
        mTimeZone.setSummary(getTimeZoneText(now.getTimeZone(), true));
        //[S][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
        //mDatePref.setSummary(shortDateFormat.format(now.getTime()));
        //mDateFormat.setSummary(shortDateFormat.format(dummyDate));
        mDatePref.setSummary(getKoreanFormat(shortDateFormat.format(now.getTime())));
        // mDateFormat.setSummary(getKoreanFormat(shortDateFormat.format(dummyDate)));
        mDateFormat.setSummary(getKoreanFormat(shortDateFormat.format(now.getTime()))); //shlee1219.lee A1 ODR issue
        //[E][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd

    }

    @Override
    public void onDateSet(SUIDrumDatePicker view, int year, int month, int day) {
        Log.d("starmotor", "year= " + year);
        Log.d("starmotor", "month= " + month);
        Log.d("starmotor", "day= " + day);

        final Activity activity = getActivity();
        setDate(activity, year, month - 1, day);

        //RTC Force sync start
        if (chipset.equals("msm7627a")) {
            //Log.e("JJJJJ", "GGGG");
            //sendSyncWrite();
        }
        //RTC Force sync end
        if (activity != null) {
            updateTimeAndDateDisplay(activity);
            Intent userTimeChanged = new Intent("com.lge.settings.TIME_SET");
            getActivity().sendBroadcast(userTimeChanged);
        }

        //LGE_UPDATE_S jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone
        android.os.SystemProperties.set(TIMEUPDATE_PROPERTY, "manual");
        //LGE_UPDATE_E jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone

    }

    @Override
    public void onTimeSet(SUIDrumTimePicker view, int hourOfDay, int minute) {
        Log.d("starmotor", "hourOfDay= " + hourOfDay);
        Log.d("starmotor", "minute= " + minute);

        final Activity activity = getActivity();
        setTime(activity, hourOfDay, minute);

        //RTC Force sync start
        if (chipset.equals("msm7627a")) {
            //Log.e("JJJJJ", "GGGG");
            //sendSyncWrite();
        }
        //RTC Force sync end
        if (activity != null) {
            updateTimeAndDateDisplay(activity);
            Intent userTimeChanged = new Intent("com.lge.settings.TIME_SET");
            getActivity().sendBroadcast(userTimeChanged);
        }

        //LGE_UPDATE_S jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone
        android.os.SystemProperties.set(TIMEUPDATE_PROPERTY, "manual");
        //LGE_UPDATE_E jake.choi@lge.com 2011-12-20 [325/Cayman] Support automatic time configuration when abroad - in case of traveling a country with multiple timezone

        // We don't need to call timeUpdated() here because the TIME_CHANGED
        // broadcast is sent by the AlarmManager as a side effect of setting the
        // SystemClock time.
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(KEY_DATE_FORMAT)) {
            String format = preferences.getString(key,
                    getResources().getString(R.string.default_date_format));
            Settings.System.putString(getContentResolver(),
                    Settings.System.DATE_FORMAT, format);
            updateTimeAndDateDisplay(getActivity());
        } else if (key.equals(KEY_AUTO_TIME)) {
            boolean autoEnabled = preferences.getBoolean(key, true);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME,
                    autoEnabled ? 1 : 0);
            mTimePref.setEnabled(!autoEnabled);
            mDatePref.setEnabled(!autoEnabled);
            if (getActivity() != null) {
                updateTimeAndDateDisplay(getActivity());
                Intent userTimeChanged = new Intent("com.lge.settings.TIME_SET");
                getActivity().sendBroadcast(userTimeChanged);
            }
        } else if (key.equals(KEY_AUTO_TIME_ZONE)) {
            boolean autoZoneEnabled = preferences.getBoolean(key, true);
            boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
            mAutoTimeZonePref.setChecked(!autoTimeZoneEnabled);
            Log.i(LOG_TAG, "autoTimeZoneEnabled: " + autoTimeZoneEnabled);
            Settings.Global.putInt(
                    getContentResolver(), Settings.Global.AUTO_TIME_ZONE, autoZoneEnabled ? 1 : 0);
            autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
            mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);
            Log.i(LOG_TAG, "autoTimeZoneEnabled: " + autoTimeZoneEnabled);
            mTimeZone.setEnabled(!autoZoneEnabled);

            /*
            if (mOperator.equals("VDF") && nMCC == 202 || // VDF GR
                mOperator.equals("VDF") && nMCC == 230 || // VDF CZ
                mOperator.equals("TMO") && nMCC == 230 || // TMO CZ
                mOperator.equals("O2") && nMCC == 230){   // O2 CZ
                mAutoTimeZonePref.setChecked(autoZoneEnabled);
                }
            else {
                Settings.System.putInt(
                        getContentResolver(), Settings.System.AUTO_TIME_ZONE, autoZoneEnabled ? 1 : 0);
                autoTimeZoneEnabled = getAutoState(Settings.System.AUTO_TIME_ZONE);
                mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);
                Log.i(LOG_TAG, "autoTimeZoneEnabled: " + autoTimeZoneEnabled);
                }
            */
            mTimeZone.setEnabled(!autoZoneEnabled);
            if (getActivity() != null) {
                updateTimeAndDateDisplay(getActivity());
                Intent userTimeChanged = new Intent("com.lge.settings.TIME_SET");
                getActivity().sendBroadcast(userTimeChanged);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {

        switch (id) {
        case DIALOG_DATEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            SUIDrumDatePickerDialog mSUIDialog =
                    new SUIDrumDatePickerDialog(getActivity()
                            , this
                            , calendar.get(Calendar.YEAR)
                            , calendar.get(Calendar.MONTH) + 1
                            , calendar.get(Calendar.DAY_OF_MONTH)
                            , 0 /*com.lge.sui.widget.R.layout.sui_drum_date_picker_dialog*/
                            , 0 /*com.lge.sui.widget.R.id.datePicker*/
                            , false
                            , new SUIDrumDatePickerDialog.TitleBuilder() {
                                public String getTitle(Calendar c, int dateFormat) {
                                    return getString(R.string.date_time_set_date);
                                }
                            }
                    );

            mSUIDialog.getDatePicker().setStartEndYear(1970, 2036); // yw2.kim SE.142506 SUIDateDrumpicker range setting error. 120306

            mSUIDialog.setButton(getResources().getString(R.string.dlg_ok), mSUIDialog);
            dialog = mSUIDialog;
            break;
        }
        case DIALOG_TIMEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            //int layout = DateFormat.is24HourFormat(getActivity()) ? R.layout.sui_drum_timepicker_24hour
            //        : R.layout.sui_drum_timepicker;
            SUIDrumTimePickerDialog mSUIDialog =
                    new SUIDrumTimePickerDialog(getActivity()
                            , this
                            , calendar.get(Calendar.HOUR_OF_DAY)
                            , calendar.get(Calendar.MINUTE)
                            , calendar.get(Calendar.SECOND)
                            , 0 /*layout*/
                            , 0 /*R.id.timePicker*/
                            , false
                            , new SUIDrumTimePickerDialog.TitleBuilder() {
                                public String getTitle(Calendar c, int dateFormat) {
                                    return getString(R.string.date_time_set_time);
                                }
                            }
                    );
            SUIDrumTimePicker timePicker = mSUIDialog.getTimePicker();

            if (is24Hour()) {
                timePicker.setTimeFormat(SUIDrumTimePicker.TIMEFORMAT_24_HHMM);
            } else {
                timePicker.setTimeFormat(SUIDrumTimePicker.TIMEFORMAT_12_AMPM);
            }

            mSUIDialog.setButton(getResources().getString(R.string.dlg_ok), mSUIDialog);
            dialog = mSUIDialog;
            break;
        }
        //[S][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        case DIALOG_DUALCLOCK: {
            final String[] itemsDualClock = { getString(R.string.enable),
                    getString(R.string.disable) };
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.troamingdualclock)
                    .setSingleChoiceItems(itemsDualClock,
                            Settings.Secure.getInt(getContentResolver(),
                                    SettingsConstants.Secure.SKT_ROAMING_DUALCLOCK, 0),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    Settings.Secure.putInt(getContentResolver(),
                                            SettingsConstants.Secure.SKT_ROAMING_DUALCLOCK, item);
                                    mDualClock.setSummary(getClockSummaryIndex());
                                    Toast.makeText(getActivity(), getCompleteString(item),
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                    )
                    .setPositiveButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
            dialog = alertDialog;
            break;
            //[E][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
        }
        default:
            dialog = null;
            break;
        }

        return dialog;
    }

    static void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(DATE_1970, Calendar.JANUARY, DATE_1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(DATE_2037, Calendar.DECEMBER, DATE_31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    /*
    @Override
    public void onPrepareDialog(int id, Dialog d) {
        switch (id) {
        case DIALOG_DATEPICKER: {
            DatePickerDialog datePicker = (DatePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            datePicker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            break;
        }
        case DIALOG_TIMEPICKER: {
            TimePickerDialog timePicker = (TimePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            timePicker.updateTime(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));
            break;
        }
        default:
            break;
        }
    }
    */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDatePref) {
            showDialog(DIALOG_DATEPICKER);
        } else if (preference == mTimePref) {
            // The 24-hour mode may have changed, so recreate the dialog
            removeDialog(DIALOG_TIMEPICKER);
            showDialog(DIALOG_TIMEPICKER);
        } else if (preference == mTime24Pref) {
            final boolean is24Hour = ((CheckBoxPreference)mTime24Pref).isChecked();
            set24Hour(is24Hour);
            updateTimeAndDateDisplay(getActivity());
            timeUpdated(is24Hour);
        } else if (preference == mDualClock) { //[S][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
            showDialog(DIALOG_DUALCLOCK);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }

    private void timeUpdated(boolean is24Hour) {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        timeChanged.putExtra(Intent.EXTRA_TIME_PREF_24_HOUR_FORMAT, is24Hour);
        getActivity().sendBroadcast(timeChanged);
        Log.i(LOG_TAG, "Send Intent.ACTION_TIME_CHANGED");
    }

    /*  Get & Set values from the system settings  */

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour ? HOURS_24 : HOURS_12);
    }

    private String getDateFormat() {
        return Settings.System.getString(getContentResolver(),
                Settings.System.DATE_FORMAT);
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getContentResolver(), name) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }

    /*  Helper routines to format timezone */

    /* package *//*static void setDate(int year, int month, int day) { 
                     Calendar c = Calendar.getInstance();

                     c.set(Calendar.YEAR, year);
                     c.set(Calendar.MONTH, month);
                     c.set(Calendar.DAY_OF_MONTH, day);
                     long when = c.getTimeInMillis();

                     if (when / 1000 < Integer.MAX_VALUE) {
                     SystemClock.setCurrentTimeMillis(when);
                     }
                     }
                     */
    /* package */static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /* package *//* static void setTime(int hourOfDay, int minute) { 
                     Calendar c = Calendar.getInstance();

                     c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                     c.set(Calendar.MINUTE, minute);
                     c.set(Calendar.SECOND, 0);
                     c.set(Calendar.MILLISECOND, 0);
                     long when = c.getTimeInMillis();

                     if (when / 1000 < Integer.MAX_VALUE) {
                     SystemClock.setCurrentTimeMillis(when);
                     }
                     }*/

    /* package */static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    public static String getTimeZoneText(TimeZone tz, boolean includeName) {
        Date now = new Date();

        // Use SimpleDateFormat to format the GMT+00:00 string.
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("ZZZZ");
        gmtFormatter.setTimeZone(tz);
        String gmtString = gmtFormatter.format(now);

        // Ensure that the "GMT+" stays with the "00:00" even if the digits are RTL.
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        Locale l = Locale.getDefault();
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
        gmtString = bidiFormatter.unicodeWrap(gmtString,
                isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR);

        if (!includeName) {
            return gmtString;
        }

        // Optionally append the time zone name.
        SimpleDateFormat zoneNameFormatter = new SimpleDateFormat("zzzz");
        zoneNameFormatter.setTimeZone(tz);
        String zoneNameString = zoneNameFormatter.format(now);
        if ("Asia/Saigon".equals(tz.getID())) {
                if ("vi".equals(Locale.getDefault().getLanguage())
                        || "en".equals(Locale.getDefault().getLanguage())) {
                    zoneNameString = new String("Hanoi");
                }
        }
            else if ("Europe/Istanbul".equals(tz.getID()))
            {
                if ("en".equals(Locale.getDefault().getLanguage())
                        || "tr".equals(Locale.getDefault().getLanguage())) {
                   zoneNameString = new String("Istanbul");
                }
            } else if (tz.getID().equals("Europe/Samara")) {
                final String localeNameSa = Locale.getDefault().toString();
                 zoneNameString = TimeZoneNames.getExemplarLocation(localeNameSa, tz.getID());    
            } else if (tz.getID().equals("Europe/Simferopol")) {
                final String localeNameSi = Locale.getDefault().toString();
                 zoneNameString = TimeZoneNames.getExemplarLocation(localeNameSi, tz.getID());
            } else if (tz.getID().equals("Europe/Kaliningrad")) {
                final String localeNameKa = Locale.getDefault().toString();
                 zoneNameString = TimeZoneNames.getExemplarLocation(localeNameKa, tz.getID());
            }
            
        // We don't use punctuation here to avoid having to worry about localizing that too!
        return gmtString + " " + zoneNameString;
    }

    private static char[] formatOffset(int off) {
        off = off / 1000 / 60;

        char[] buf = new char[9];
        buf[0] = 'G';
        buf[1] = 'M';
        buf[2] = 'T';

        if (off < 0) {
            buf[3] = '-';
            off = -off;
        } else {
            buf[3] = '+';
        }

        int hours = off / 60;
        int minutes = off % 60;

        buf[4] = (char)('0' + hours / 10);
        buf[5] = (char)('0' + hours % 10);

        buf[6] = ':';

        buf[7] = (char)('0' + minutes / 10);
        buf[8] = (char)('0' + minutes % 10);

        return buf;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                updateTimeAndDateDisplay(activity);
            }
        }
    };

    //[S][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
    private String getKoreanFormat(String str) {
        String returnStr = str;

        // goodluck@lge.com 121113 yyyy.mm.dd -> yyyy.mm.dd. [START]
        /*try {
            IActivityManager am = ActivityManagerNative.getDefault();

            if(am != null){
                Configuration config = am.getConfiguration();
                if(config.locale.getLanguage().equals("ko")) {
                        if(returnStr.endsWith(".")) {
                            returnStr = returnStr.substring(0, returnStr.length() - 1);
                        }
                    }
                }

        } catch(Exception e) {
                android.util.Log.i("DateTimeSettings", "getKoreanFormat Exception");
        }*/
        // goodluck@lge.com 121113 yyyy.mm.dd -> yyyy.mm.dd. [END]

        return returnStr;
    }

    //[E][jaeyoon.hyun@lge.com][2012.01.05][F160L][TD:SE 115710] Fix Korean date format yyyy.mm.dd. -> yyyy.mm.dd
    //[S][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S
    private int getClockSummaryIndex() {
        if (Settings.Secure.getInt(getContentResolver(),
                SettingsConstants.Secure.SKT_ROAMING_DUALCLOCK, 0) == DATE_1) {
            return R.string.disable;
        }
        return R.string.enable;
    }

    public String getCompleteString(int mode) {
        if (mode == 1) {
            return getString(R.string.troamingdualclock_disable);
        }
        return getString(R.string.troamingdualclock_enable);
    }

    //[E][jaeyoon.hyun@lge.com][2013.01.15] Add preference of Roaming dual clock for F240S, F260S

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-363]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "DateTimeSettings : mLGMDMReceiver unregisterReceiver ",
                        e);
            }
        }
        // LGMDM_END
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-363]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveDateTimeSettingChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
    
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            setSearchIndexData(context, "datetime_settings_main",
                    context.getString(R.string.date_and_time_settings_title), "main", null,
                    null, "android.settings.DATE_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_AUTO_TIME,
                    context.getString(R.string.date_time_auto_new),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_DATE,
                    context.getString(R.string.date_time_set_date),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_TIME,
                    context.getString(R.string.date_time_set_time),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_AUTO_TIME_ZONE,
                    context.getString(R.string.zone_auto_new),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_TIME_ZONE,
                    context.getString(R.string.date_time_set_timezone),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);

            /*
            setSearchIndexData(context, KEY_DUAL_CLOCK,
                    context.getString(R.string.troamingdualclock),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);*/

            /*
            setSearchIndexData(context, KEY_TIME_ROAMING,
                    context.getString(R.string.sp_time_settings_for_roaming),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);*/
            
            setSearchIndexData(context, "24 hour",
                    context.getString(R.string.date_time_24hour),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);
            
            setSearchIndexData(context, KEY_DATE_FORMAT,
                    context.getString(R.string.date_time_date_format),
                    context.getString(R.string.date_and_time_settings_title), null,
                    null, "android.settings.DATE_SETTINGS",
                    null, null, 1, 
                    null, null, null, 1, 0);

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
