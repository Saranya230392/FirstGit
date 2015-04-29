package com.android.settings.powersave;

import java.util.Locale;

import com.lge.constants.LGIntent;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;

public class BatterySaverSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, OnClickListener, OnCheckedChangeListener {
    private static final String TAG = "BatterySaverSettings";
    private static final String KEY_BATTERY_SAVER_CATEGORY = "battery_saver_category";
    private static final String KEY_BATTERY_SAVER_IMMEDI = "battery_saver_immediately";
    private static final String KEY_BATTERY_SAVER_LEVEL_05 = "battery_saver_level_05";
    private static final String KEY_BATTERY_SAVER_LEVEL_10 = "battery_saver_level_10";
    private static final String KEY_BATTERY_SAVER_LEVEL_15 = "battery_saver_level_15";
    private static final String KEY_BATTERY_SAVER = "battery_saver";

    private RadioButtonPreference mBatterySaverImmedi;
    private RadioButtonPreference mBatterySaver_05;
    private RadioButtonPreference mBatterySaver_10;
    private RadioButtonPreference mBatterySaver_15;

    private PreferenceCategory mBatterySaverCategory;

    // Battery BackGroundData CheckBox
    private CheckBoxPreference mBatteryBackGround;
    public static final String KEY_BATTERY_SAVER_BACKGROUND = "battery_saver_background";

    private PowerSaveListPreference mPowerSaveMode;
    private static final String KEY_POWER_SAVE_MODE = "power_save_mode";


    private Switch mSwitch;
    SettingsBreadCrumb mBreadCrumb;
    private Context mContext;
    private int mPlugType = 0;
    private PowerManager mPowerManager;

    private final Handler mHandler = new Handler();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final long WAIT_FOR_SWITCH_ANIM = 500;

    private String mSearch_result;
    private boolean mIsFirst = false;

    private final Runnable mStartMode = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) {
                        Log.d(TAG, "Starting low power mode from settings");
                    }
                    trySetPowerSaveMode(true);
                }
            });
        }
    };

    private final Runnable mUpdateSwitch = new Runnable() {
        @Override
        public void run() {
            updateSwitch();
        }
    };

    private void updateSwitch() {
        boolean isChecked = Settings.Global.getInt
                (getContentResolver(), "battery_saver_mode", 0) > 0;
        mSwitch.setChecked(isChecked);
    }

    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        Log.d(TAG, "mSwitch SET : " + isChecked);
        int value = isChecked ? 1 : 0;
        Settings.Global.putInt(mContext.getContentResolver(),
                "battery_saver_mode", value);

        mHandler.removeCallbacks(mStartMode);
        if (isChecked) {
            mHandler.postDelayed(mStartMode, WAIT_FOR_SWITCH_ANIM);
        } else {
            if (DEBUG) {
                Log.d(TAG, "Stopping low power mode from settings");
            }
            trySetPowerSaveMode(false);
        }
    }

    private void trySetPowerSaveMode(boolean mode) {
        setSwitch(mode, mContext, mPowerManager);
        mHandler.post(mUpdateSwitch);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String mBatteryIntent = null;
            String mPlugKeyValue = null;

            if (Config.VZW.equals(Config.getOperator())) {
                mBatteryIntent = LGIntent.ACTION_BATTERYEX_CHANGED;
                mPlugKeyValue = LGIntent.EXTRA_BATTERY_PLUGGED;
            } else {
                mBatteryIntent = Intent.ACTION_BATTERY_CHANGED;
                mPlugKeyValue = BatteryManager.EXTRA_PLUGGED;
            }

            if (mBatteryIntent.equals(action)) {
                mPlugType = intent.getIntExtra(mPlugKeyValue, 0);
                Log.d(TAG, "BatterySaverSettings PlugType : " + mPlugType);
                setDisabedSwitchWhenPlugIn(context);

            }

        }

    };

    /**
     * @param context
     */
    private void setDisabedSwitchWhenPlugIn(Context context) {
        if (mPlugType > 0 && (mSwitch != null)) {
            int modeValue = Settings.Global
                    .getInt(context.getContentResolver(), "battery_saver_mode_value", 15);
            if (modeValue == 100) {
                Log.d(TAG, "disabed switch");
                mSwitch.setChecked(false);
                mSwitch.setEnabled(false);
            } else {
                mSwitch.setEnabled(true);
            }
        } else {
            mSwitch.setEnabled(true);
        }
    }

    private ContentObserver mLOW_POWER_MODE = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "selfChange :" + selfChange);
            boolean mIsLowPower = getLowPowerModeDB() > 0;
            Settings.Global.putInt(mContext.getContentResolver(),
                    "battery_saver_mode", mIsLowPower ? 1 : 0);
            mSwitch.setChecked(mIsLowPower);

            setEnabledRadio();

        }

    };

    private void setEnabledRadio() {
        if (mBatterySaverImmedi != null) {
            mBatterySaverImmedi.setEnabled(!(getLowPowerModeDB() > 0));
        }
        if (mBatterySaver_05 != null) {
            mBatterySaver_05.setEnabled(!(getLowPowerModeDB() > 0));
        }
        if (mBatterySaver_15 != null) {
            mBatterySaver_15.setEnabled(!(getLowPowerModeDB() > 0));
        }
        if (mPowerSaveMode != null) {
            mPowerSaveMode.setEnabled(!(getLowPowerModeDB() > 0));
        }
    }

    private ContentObserver mBatterySaverObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean isChecked = Settings.Global.getInt
                    (getContentResolver(), "battery_saver_mode", 0) > 0;
            Log.d(TAG, "mBatterySaverObserver :" + isChecked);
            if (mSwitch != null) {
                mSwitch.setChecked(isChecked);
            }
        }

    };

    @Override
    public void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        createToggles();
        mContext = getActivity().getApplicationContext();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mIsFirst = true;
    }

    public void createToggles() {
        addPreferencesFromResource(R.layout.battery_saver_settings);
        PreferenceScreen root = getPreferenceScreen();

        mBatterySaverCategory = (PreferenceCategory)findPreference(KEY_BATTERY_SAVER_CATEGORY);

        mBatterySaverImmedi = (RadioButtonPreference)root.findPreference(KEY_BATTERY_SAVER_IMMEDI);
        mBatterySaverImmedi.setOnPreferenceChangeListener(this);

        mBatterySaver_05 = (RadioButtonPreference)root.findPreference(KEY_BATTERY_SAVER_LEVEL_05);
        mBatterySaver_05.setOnPreferenceChangeListener(this);

        mBatterySaver_10 = (RadioButtonPreference)root.findPreference(KEY_BATTERY_SAVER_LEVEL_10);
        mBatterySaver_10.setOnPreferenceChangeListener(this);

        mBatterySaver_15 = (RadioButtonPreference)root.findPreference(KEY_BATTERY_SAVER_LEVEL_15);
        mBatterySaver_15.setOnPreferenceChangeListener(this);

        String[] entry = getResources().getStringArray(
                R.array.sp_battery_saver_mode_entries_NORMAL);

        mBatterySaverImmedi.setTitle(entry[0]);
        mBatterySaver_05.setTitle(entry[1]);
        mBatterySaver_15.setTitle(entry[2]);

        /*L-OS Level 5%, 15% */
        mBatterySaverCategory.removePreference(mBatterySaver_10);

        mPowerSaveMode = (PowerSaveListPreference)root.findPreference(KEY_POWER_SAVE_MODE);
        mPowerSaveMode.setOnPreferenceChangeListener(this);
        mBatteryBackGround = (CheckBoxPreference)root.findPreference(KEY_BATTERY_SAVER_BACKGROUND);
        mBatteryBackGround.setOnPreferenceChangeListener(this);

        /* Add switch */
        Activity activity = getActivity();
        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mBatterySaverCategory.removeAll();
            getPreferenceScreen().removePreference(mBatterySaverCategory);
        } else {
            getPreferenceScreen().removePreference(mBatteryBackGround);
            getPreferenceScreen().removePreference(mPowerSaveMode);
        }
    }

    public static void setSwitch(boolean isChecked, Context context, PowerManager pm) {
        int modeValue = Settings.Global.getInt(context.getContentResolver(),
                "battery_saver_mode_value", 15);
        int mLowPowerValue = Settings.Global.getInt(context.getContentResolver(),
                Global.LOW_POWER_MODE, 0);
        int mTriggerValue = Settings.Global.getInt(context.getContentResolver(),
                Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);

        if (isChecked) {
            if (modeValue == PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY) {
                Settings.Global.putInt(context.getContentResolver(),
                        Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);
                pm.setPowerSaveMode(isChecked);
            } else if (modeValue == 5) {
                if (mTriggerValue != 5) {
                    Settings.Global.putInt(context.getContentResolver(),
                            Global.LOW_POWER_MODE_TRIGGER_LEVEL, 5);
                }
            } else if (modeValue == 15) {
                if (mTriggerValue != 15) {
                    Settings.Global.putInt(context.getContentResolver(),
                            Global.LOW_POWER_MODE_TRIGGER_LEVEL, 15);
                }
            }

        } else {
            pm.setPowerSaveMode(isChecked);
            Settings.Global.putInt(context.getContentResolver(),
                    Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);
        }

        Log.d(TAG, "setSwitch Immedi : " + mLowPowerValue);
        Log.d(TAG, "setSwitch BatterySaverLevel : " + mTriggerValue);
    }

    public boolean onPreferenceChange(Preference preference, Object arg1) {
        // TODO Auto-generated method stub
        if (Utils.isMonkeyRunning()) {
            Log.d(TAG, "Enter Monkey");
            return true;
        }
        if (preference == mBatteryBackGround) {
            if (mBatteryBackGround.isChecked()) {
                Settings.System.putInt(getContentResolver(),
                        "battery_saver_restrict_bg_data", 0);
            } else {
                showBatterySaverBackGroundDataDlg();
            }
            return true;
        } else if (preference == mPowerSaveMode) {
            setPowerSaveModeAllowed(arg1);
            updatePowerSaverModeSummary(arg1);
            return true;
        }


        initToggles();
        boolean mBatterySaverSwitch = Settings.Global.getInt(getContentResolver(),
                "battery_saver_mode", 0) != 0;

        if (preference == mBatterySaverImmedi) {
            Settings.Global.putInt(getContentResolver(), "battery_saver_mode_value", 100);
            mBatterySaverImmedi.setChecked(true);
        } else if (preference == mBatterySaver_05) {
            Settings.Global.putInt(getContentResolver(), "battery_saver_mode_value", 5);
            mBatterySaver_05.setChecked(true);
        } else if (preference == mBatterySaver_15) {
            Settings.Global.putInt(getContentResolver(), "battery_saver_mode_value", 15);
            mBatterySaver_15.setChecked(true);
        }

        setSwitch(mBatterySaverSwitch, mContext, mPowerManager);

        Log.d(TAG, "Set Immedi : " + Global.getInt(getContentResolver(), Global.LOW_POWER_MODE, 0));
        Log.d(TAG, "Set BatterySaverLevel : " + Global.getInt(getContentResolver(),
                Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0));
        setDisabedSwitchWhenPlugIn(mContext);
        return true;
    }

    private void showBatterySaverBackGroundDataDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(getString(R.string.sp_dlg_note_NORMAL));
        // C-1 content
        builder.setMessage(getString(R.string.sp_battery_saver_restrict_apps_in_background_pop_up_NORMAL));

        // B-3 button
        builder.setPositiveButton(getText(R.string.sp_lg_software_note_yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Settings.System.putInt(getContentResolver(),
                                "battery_saver_restrict_bg_data", 1);

                    }
                });
        builder.setNegativeButton(getText(R.string.sp_lg_software_note_no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mBatteryBackGround.setChecked(Settings.System.getInt(getActivity()
                                .getContentResolver(), "battery_saver_restrict_bg_data", 0) != 0);
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                // UpDate BatterySaverBackGround CheckBox
                mBatteryBackGround.setChecked(Settings.System.getInt(getActivity()
                        .getContentResolver(), "battery_saver_restrict_bg_data", 0) != 0);
            }
        });
        builder.show();
    }

    private void initToggles() {
        if (mBatterySaverImmedi != null) {
            mBatterySaverImmedi.setChecked(false);
        }
        if (mBatterySaver_05 != null) {
            mBatterySaver_05.setChecked(false);
        }
        if (mBatterySaver_10 != null) {
            mBatterySaver_10.setChecked(false);
        }
        if (mBatterySaver_15 != null) {
            mBatterySaver_15.setChecked(false);
        }
    }

    private void updateToggles() {
        initToggles();

        int modeValue = Settings.Global
                .getInt(getContentResolver(), "battery_saver_mode_value", 15);
        int mBatterySaverSwitch = Settings.Global.getInt(getContentResolver(),
                "battery_saver_mode", 0);

        int mBatterySaverOn = getLowPowerModeDB();
        int mBatterySaverLevel = getLowPowerModeTriggerDB();

        Log.d(TAG, "updateToggles mBatterySaverOn : " + mBatterySaverOn);
        Log.d(TAG, "updateToggles mBatterySaverLevel : " + mBatterySaverLevel);
        Log.d(TAG, "updateToggles mBatterySaverSwitch : " + mBatterySaverSwitch);

        if (mBatterySaverImmedi != null && modeValue == 100) {
            mBatterySaverImmedi.setChecked(true);
        } else if (mBatterySaver_05 != null && modeValue == 5) {
            mBatterySaver_05.setChecked(true);
        } else if (mBatterySaver_15 != null && modeValue == 15) {
            mBatterySaver_15.setChecked(true);
        }

        setEnabledRadio();
        mSwitch.setChecked(mBatterySaverSwitch != 0);

        if (mBatterySaverOn > 0) {
            mSwitch.setChecked(mBatterySaverOn > 0);
            Settings.Global.putInt(mContext.getContentResolver(),
                    "battery_saver_mode", mBatterySaverOn);
        }

    }

    /**
     * Global.LOW_POWER_MODE_TRIGGER_LEVEL DB
     */
    private int getLowPowerModeTriggerDB() {
        return Global.getInt(getContentResolver(), Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);
    }

    /**
     * Global.LOW_POWER_MODE DB
     */
    private int getLowPowerModeDB() {
        return Global.getInt(getContentResolver(), Global.LOW_POWER_MODE, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        getPreferenceScreen().removeAll();
        createToggles();
        updateToggles();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Config.VZW.equals(Config.getOperator())) {
            getActivity().registerReceiver(mReceiver,
                    new IntentFilter(LGIntent.ACTION_BATTERYEX_CHANGED));
        } else {
            getActivity().registerReceiver(mReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Global.LOW_POWER_MODE), true, mLOW_POWER_MODE);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor("battery_saver_mode"), true, mBatterySaverObserver);
        updateToggles();

        String mode = String.valueOf(Settings.Global.getInt(
                getContentResolver(), "battery_saver_mode_value", 15));
        mPowerSaveMode.setValue(mode);
        updatePowerSaverModeSummary(mode);

        // UpDate BatterySaverBackGround CheckBox
        mBatteryBackGround
                .setChecked(Settings.System.getInt(getActivity()
                        .getContentResolver(),
                        "battery_saver_restrict_bg_data", 0) != 0);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra("perform",
                false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }

    }
    
    private void startResult() {
        if (mSearch_result.equals(KEY_BATTERY_SAVER)) {
            int newValue = getActivity().getIntent().getBooleanExtra(
                    "newValue", false) == true ? 1 : 0;
            Settings.Global.putInt(mContext.getContentResolver(),
                    "battery_saver_mode", newValue);
            updateSwitch();
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
        getContentResolver().unregisterContentObserver(mLOW_POWER_MODE);
        getContentResolver().unregisterContentObserver(mBatterySaverObserver);

    }

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
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    private void updatePowerSaverModeSummary(Object value) {
        try {

            String immediatly_value = Integer
                    .toString(PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY);
            if (immediatly_value.equals(value.toString())) { // Immediately
                mPowerSaveMode.setValue(immediatly_value);
                String entry = (mPowerSaveMode.getEntry() != null) ? mPowerSaveMode
                        .getEntry().toString() : "";
                mPowerSaveMode.setSummary(entry);
            } else {
                if (Utils.isRTLLanguage()) {
                    mPowerSaveMode.setSummary(getString(
                            R.string.sp_power_saver_on_summary_NORMAL,
                            String.format(Locale.getDefault(), "%d",
                                    Integer.parseInt(value.toString()))
                                    + "%"));
                } else {
                    mPowerSaveMode.setSummary(getString(
                            R.string.sp_power_saver_on_summary_NORMAL, "\u200E"
                                    + value.toString() + "%"));
                }
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist Auto power save mode", e);
        }
    }

    private void setPowerSaveModeAllowed(Object objValue) {
        int value = Integer.parseInt((String)objValue);
        Log.d("jw", "setPowerSaveModeAllowed value : " + value);

        boolean mBatterySaverSwitch = Settings.Global.getInt(
                getContentResolver(), "battery_saver_mode", 0) != 0;

        Settings.Global.putInt(getContentResolver(),
                "battery_saver_mode_value", value);

        setSwitch(mBatterySaverSwitch, mContext, mPowerManager);

        Log.d(TAG,
                "Set Global 4_2 LOW_POWER_MODE : "
                        + Global.getInt(getContentResolver(),
                                Global.LOW_POWER_MODE, 0));
        Log.d(TAG,
                "Set Global 4_2 LOW_POWER_MODE_TRIGGER_LEVEL : "
                        + Global.getInt(getContentResolver(),
                                Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0));
        setDisabedSwitchWhenPlugIn(mContext);
    }
}