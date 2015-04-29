package com.android.settings.powersave;

import com.android.settings.R;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.BatteryStats.HistoryItem;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.PowerSaveBatteryPreference2;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.lge.constants.SettingsConstants;
import android.os.Build;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.lge.constants.LGIntent;

import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.Global;

public class BatterySettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "BatterySettings";
    public static final boolean DEBUG = false;

    private static final String KEY_POWER_SAVER_CATEGORY = "power_saver_category";
    private static final String KEY_POWER_SAVE_BATTERY = "power_save_battery";
    private static final String KEY_POWER_SAVE_ENABLER = "power_save_enabler";
    private static final String KEY_POWER_SAVE_BATTERY_PERCENTAGE = "power_save_battery_percentage";
    private static final String KEY_POWER_SAVE_ECO_MODE = "power_save_eco_mode";

    private PowerSave mPowerSave;

    private Context mContext;

    private PowerSaveBatteryInfoPreference mBattery;
    private CheckBoxPreference mBatteryPercentage;
    private PowerSaveSwitchPreference mPowerSaveSwitch;
    private PowerSaveSwitchPreference mEcoMode;
    private PreferenceCategory mPowerSaverCategory;
    //ADD JW FastBoot
    private CheckBoxPreference mBatteryFastBoot;
    private static final String KEY_POWER_SAVE_FASTBOOT = "power_save_fastboot";
    private static final String PERSIST_ZWAIT_ENABLED = "persist.sys.lge.zwait_enable";

    private PreferenceCategory mFastBootCategory;
    private static final String KEY_FASTBOOT_CATEGORY = "power_saver_category_fastboot";

    //ADD JW SMART POWER SAVER
    private PowerSaveSwitchPreference mBatterySmartPowerSaver;
    private static final String KEY_POWER_SMART_SAVER = "power_smart_saver";

    //ADD JW L-OS BatterySaver
    private PowerSaveSwitchPreference mBatterySaver;
    private static final String KEY_BATTERY_SAVER = "battery_saver";

    private PreferenceCategory mSmartPowerSaverCategory;
    private static final String KEY_SMART_SAVER_CATEGORY = "power_saver_category_smart_saver";

    //ADD JW SmartCharging
    private CheckBoxPreference mBatterySmart;
    private static final String KEY_POWER_SAVE_SMART = "power_save_battery_smart";

    IBatteryStats mBatteryInfo;
    BatteryStatsImpl mStats;
    private static BatteryStatsImpl sStatsXfer;

    private static final int REFERENCE_HOUR = 3;

    //private final int minTime = 3; // minimum time ( min )
    private final int minRefTime = 10; // minimum reference time ( min )

    private int graphPadding;

    final Calendar now = Calendar.getInstance();

    private Layout mRefTimeLayout;

    private int unitWidth;
    private double realTimeGraphWidth;

    private long timeStart;
    private long timeChange;

    private long onDischargingTotalTime;
    private int onDischargingTotalLevel;

    private long onChargingTotalTime;
    private int onChargingTotalLevel;

    private long lastTime = 0;
    private int lastLevel = 0;
    private int lastStatus = 0;

    private int curStatus = 0;

    private long mHistStart;
    private long mHistEnd;

    private int mBatHigh = 100;
    private static String batterySummary;

    public static final String REFRESH_INTENT = "refresh";

    private static int sTemperature;
    private Intent mSmartIntent;

    //[jongwon007.kim] BatteryTips
    private PreferenceScreen mPowerSaverTips;
    private PreferenceScreen mBatteryTips;
    private static final String POWER_SAVER_TIPS = "power_saver_tips";
    private static final String BATTERY_SAVER_TIPS = "battery_saver_tips";
    private static final long INVALID_ESTIMATE_CHARGING_TIME = -1;
    private static final long INVALID_ESTIMATE_DISCHARGING_TIME = -1;
    boolean mSupportSmartBattery = false;
    static boolean sSmartChargOn;
    private int mPlugType = 0;
 
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    private BroadcastReceiver mBatteryVZWReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LGIntent.ACTION_BATTERYEX_CHANGED.equals(action)) {
                //                int level = intent.getIntExtra("level", 0);
                //                int scale = intent.getIntExtra("scale", 100);
                mSmartIntent = intent;
                int level = intent.getIntExtra(LGIntent.EXTRA_BATTERY_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                curStatus = intent.getIntExtra(LGIntent.EXTRA_BATTERY_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                Log.d(TAG,
                        "Battery Status : "
                                + com.android.settings.Utils.getSmartBatteryStatus(getResources(),
                                        intent, mContext));
                String batteryLevel = com.android.settings.Utils.getBatteryPercentage(intent);
                String batteryStatus = com.android.settings.Utils.getSmartBatteryStatus(
                        getResources(), intent, mContext);

                setBatteryStatusDisplay(context, intent, level, scale,
                        batteryLevel, batteryStatus);

            }

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                sTemperature = intent.getIntExtra("temperature", 0);

            }
        }
    };

    private BroadcastReceiver mBatteryOpenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

                // TODO Auto-generated method stub
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                curStatus = intent.getIntExtra("status",
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                Log.d(TAG,
                        "Battery Status : "
                                + com.android.settings.Utils.getBatteryStatus(
                                        getResources(), intent));

                String batteryLevel = com.android.settings.Utils
                        .getBatteryPercentage(intent);
                String batteryStatus = com.android.settings.Utils.getBatteryStatus(
                        getResources(), intent);

                setBatteryStatusDisplay(context, intent, level, scale,
                        batteryLevel, batteryStatus);
            }

        }

    };


    private void setBatteryStatusDisplay(Context context, Intent intent,
            int level, int scale, String batteryLevel, String batteryStatus) {

        Locale systemLocale = getResources().getConfiguration().locale;
        String language = systemLocale.getLanguage();
        if (language.equals("ko")
                && (batteryStatus.equals(getResources().getString(
                        R.string.battery_info_status_discharging)) ||
                batteryStatus.equals(getResources().getString(
                        R.string.battery_info_status_not_charging)))) {
            batterySummary = batteryLevel;
        } else {
            batterySummary = context.getResources().getString(
                    R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
        }
        mBattery.setIcon(mPowerSave.getBatteryImgId(getResources(), intent, level * 100
                / scale));

        setExpectedTime();
        if (Config.VZW.equals(Config.getOperator())) {
            mPlugType = intent.getIntExtra(LGIntent.EXTRA_BATTERY_PLUGGED, 0);

        } else {
            mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        }
        Log.d(TAG, "BatterySettings PlugType : " + mPlugType);
        setDisabledSwitchWhenPlugIn(context);
    }

    /**
     * @param context
     */
    private void setDisabledSwitchWhenPlugIn(Context context) {
        if (mPlugType > 0 && (mBatterySaver != null)) {
            int modeValue = Settings.Global
                    .getInt(context.getContentResolver(), "battery_saver_mode_value", 15);
            if (modeValue == 100) {
                Log.d(TAG, "disabed switch");
                mBatterySaver.setChecked(false);
                mBatterySaver.setSwitchEnabled(false);
            } else {
                mBatterySaver.setSwitchEnabled(true);
            }
        } else {
            mBatterySaver.setSwitchEnabled(true);
        }
    }

    public static int getTemperature() {
        return sTemperature;
    }

    private Handler enable_handler = new Handler();
    private ContentObserver mEnableObserver = new ContentObserver(enable_handler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean isChecked = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED, 0) > 0;
            Log.d(TAG, "BatterySettings mEnableObserver::onChange:" + isChecked);
            if (mPowerSaveSwitch != null) {
                mPowerSaveSwitch.setChecked(isChecked);
            }
        }
    };

    private ContentObserver mEcoObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean isChecked = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.ECO_MODE, 0) > 0;
            Log.d(TAG, "BatterySettings mEcoObserver::onChange:" + isChecked);
            if (mEcoMode != null) {
                mEcoMode.setCheckedUpdate(isChecked);
            }
        }
    };

    private ContentObserver mBatterySaverObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean isChecked = Settings.Global.getInt
                    (getContentResolver(), "battery_saver_mode", 0) > 0;
            Log.d(TAG, "mBatterySaverObserver :" + isChecked);
            if (mBatterySaver != null) {
                mBatterySaver.setChecked(isChecked);
            }
        }

    };

    private ContentObserver mLOW_POWER_MODE = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean isLowPowerValue = Settings.Global.getInt
                    (getContentResolver(), Global.LOW_POWER_MODE, 0) > 0;
            Log.d(TAG, "mLOW_POWER_MODE : " + isLowPowerValue);
        }

    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mContext = getActivity().getApplicationContext();
        if (Utils.supportSplitView(mContext)) {
            getActivity().getActionBar().setTitle(R.string.settings_label);
        } else if ("ATT".equals(Config.getOperator())) {
            getActivity().getActionBar().setTitle(R.string.shortcut_batteryusage_att);
        }
        initPreference();
        checkPreference();

        if (icicle != null) {
            mStats = sStatsXfer;
        }

        // Change KK 2013_09_30
        //        mBatteryInfo = IBatteryStats.Stub.asInterface(
        //                ServiceManager.getService("batteryinfo"));

        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService(BatteryStats.SERVICE_NAME));
        mPowerSave = new PowerSave(mContext);

        mRefTimeLayout = makeLayout(DateFormat.getTimeFormat(mContext).format(now.getTime()));
        mIsFirst = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        checkPreference();

        mStats = null;
        load();

        setExpectedTime();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(LGIntent.ACTION_BATTERYEX_CHANGED);
        if(Config.VZW.equals(Config.getOperator())) {
            getActivity().registerReceiver(mBatteryVZWReceiver, filter);
        } else {
            getActivity().registerReceiver(mBatteryOpenReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        Uri uri = Settings.System.getUriFor(SettingsConstants.System.POWER_SAVE_ENABLED);
        Log.d(TAG, "register observer uri: " + uri.toString());
        getContentResolver().registerContentObserver(uri, true, mEnableObserver);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(SettingsConstants.System.ECO_MODE), true,
                mEcoObserver);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor("battery_saver_mode"), true, mBatterySaverObserver);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Global.LOW_POWER_MODE), true, mLOW_POWER_MODE);

        setDisabledSwitchWhenPlugIn(mContext);
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
        if (mSearch_result.equals(KEY_POWER_SAVE_BATTERY_PERCENTAGE)) {
            mBatteryPercentage.performClick(getPreferenceScreen());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mEnableObserver);
        getContentResolver().unregisterContentObserver(mEcoObserver);
        getContentResolver().unregisterContentObserver(mBatterySaverObserver);
        getContentResolver().unregisterContentObserver(mLOW_POWER_MODE);
        if (Config.VZW.equals(Config.getOperator())) {
            getActivity().unregisterReceiver(mBatteryVZWReceiver);
        } else {
            getActivity().unregisterReceiver(mBatteryOpenReceiver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            sStatsXfer = mStats;
        }
    }

    private void initPreference() {

        addPreferencesFromResource(R.xml.battery_settings);

        mPowerSaverCategory = (PreferenceCategory)findPreference(KEY_POWER_SAVER_CATEGORY);

        mBattery = (PowerSaveBatteryInfoPreference)findPreference(KEY_POWER_SAVE_BATTERY);
        mBattery.setLayoutResource(R.layout.power_preference_battery_info);
        mBatteryPercentage = (CheckBoxPreference)findPreference(KEY_POWER_SAVE_BATTERY_PERCENTAGE);
        mPowerSaveSwitch = (PowerSaveSwitchPreference)findPreference(KEY_POWER_SAVE_ENABLER);

        // jongtak0920.kim 130105 Add quad-core control [START]
        mEcoMode = (PowerSaveSwitchPreference)findPreference(KEY_POWER_SAVE_ECO_MODE);
        mPowerSaverCategory.removePreference(mEcoMode);
        // jongtak0920.kim 130105 Add quad-core control [END]
        //ADD JW FastBoot
        mFastBootCategory = (PreferenceCategory)findPreference(KEY_FASTBOOT_CATEGORY);
        mBatteryFastBoot = (CheckBoxPreference)findPreference(KEY_POWER_SAVE_FASTBOOT);

        if (!SystemProperties.get("ro.lge.zwait", "false").equals("true")) {
            Log.d(TAG, "FastBoot Not Exist");
            mFastBootCategory.removeAll();
            removePreference(KEY_FASTBOOT_CATEGORY);

        }
        Log.d(TAG, "FastBoot Exist");

        mBatterySmart = (CheckBoxPreference)findPreference(KEY_POWER_SAVE_SMART);
        mPowerSaverTips = (PreferenceScreen)findPreference(POWER_SAVER_TIPS);
        mBatteryTips = (PreferenceScreen)findPreference(BATTERY_SAVER_TIPS);

        mSupportSmartBattery = Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_smart_battery,
                "com.lge.R.bool.config_smart_battery");

        Log.d(TAG, "mSupportSmartBattery : " + mSupportSmartBattery);
        if (!mSupportSmartBattery) {
            mPowerSaverCategory.removePreference(mBatterySmart);
            mPowerSaverCategory.setTitle(String.format(getResources().getString(
                    R.string.sp_power_saver_NORMAL)));
        }

        if (Utils.isUI_4_1_model(mContext)) {
            mPowerSaverCategory.removePreference(mPowerSaverTips);
        } else {
            mPowerSaverCategory.removePreference(mBatteryTips);
        }

        initSmartPowerSaver();
        initNativeBatterySaver();

    }

    /**
     * L-OS Native BatterySaver
     */
    private void initNativeBatterySaver() {
        mBatterySaver = (PowerSaveSwitchPreference)findPreference(KEY_BATTERY_SAVER);
        mPowerSaverCategory.removePreference(mPowerSaveSwitch);
    }

    private void initSmartPowerSaver() {
        //ADD JW SMART POWER SAVER
        mSmartPowerSaverCategory = (PreferenceCategory)findPreference(KEY_SMART_SAVER_CATEGORY);
        mBatterySmartPowerSaver = (PowerSaveSwitchPreference)findPreference(KEY_POWER_SMART_SAVER);
        if (!Utils.isFolderModel(mContext)) {
            mSmartPowerSaverCategory.removeAll();
            removePreference(KEY_POWER_SMART_SAVER);
            removePreference(KEY_SMART_SAVER_CATEGORY);

        }

    }

    private void checkPreference() {
        boolean isEnabled = (Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, PowerSave.DEFAULT_POWER_SAVE_ENABLED) > 0);
        mPowerSaveSwitch.setChecked(isEnabled);

        String mode = String.valueOf(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE,
                PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE));
        updatePowerSaverModeSummary(mode);

        mBatteryPercentage
                .setChecked(Settings.System.getInt(
                        getContentResolver(),
                        SettingsConstants.System.POWER_SAVE_BATTERY_INDICATOR, 0) != 0); //yonguk.kim 20120516 To be false in upgrade model
        mEcoMode.setChecked(Settings.System.getInt(
                getContentResolver(), SettingsConstants.System.ECO_MODE, 0) != 0);
        mBatteryFastBoot.setChecked(SystemProperties.get
                (PERSIST_ZWAIT_ENABLED, "false").equals("true") ? true : false);

        mBatterySmart.setChecked(Settings.System.getInt(
                getContentResolver(), "power_save_smart_charge", 0) != 0);

        mBatterySmartPowerSaver.setChecked(Settings.System.getInt(
                getContentResolver(), "power_save_smart_saver", 1) != 0);

        mBatterySaver.setChecked(Settings.Global.getInt(
                getContentResolver(), "battery_saver_mode", 0) != 0);


        updateBatterySaverSummary();
    }

    private void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
            mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        } catch (NullPointerException e) {
            Log.i(TAG, " NullPointerException " + e.getMessage());
        }

        boolean first = true;
        if (mStats != null) {
            if (mStats.startIteratingHistoryLocked()) {
                final HistoryItem rec = new HistoryItem();
                while (mStats.getNextHistoryLocked(rec)) {
                    if (rec.cmd == HistoryItem.CMD_UPDATE) {
                        if (first) {
                            first = false;
                            mHistStart = rec.time;
                        }
                        mHistEnd = rec.time;
                    }
                }
            }
        }

        timeChange = mHistEnd - mHistStart;
        timeStart = mHistStart;

        graphPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                38, getResources().getDisplayMetrics());

        int windowSize = getResources().getDisplayMetrics().widthPixels
                - graphPadding * 2;

        unitWidth = windowSize / 6; // 1hour
        realTimeGraphWidth = timeChange * unitWidth / HOUR_IN_MILLIS;
    }

    private void setExpectedTime() {
        int x = 0;
        int lastX = -1;

        int startPoint = mRefTimeLayout.getWidth() / 2;

        onDischargingTotalLevel = 0;
        onDischargingTotalTime = 0;

        onChargingTotalLevel = 0;
        onChargingTotalTime = 0;

        lastTime = 0;
        lastLevel = 0;
        lastStatus = 0;

        if (mStats == null) {
            return;
        }
        Log.d(TAG, "mStats not null");

        if (mStats.startIteratingHistoryLocked()) {
            final HistoryItem rec = new HistoryItem();
            while (mStats.getNextHistoryLocked(rec)) {
                if (rec.cmd == HistoryItem.CMD_UPDATE) {

                    x = startPoint
                            + (int)(((rec.time - timeStart) * realTimeGraphWidth) / timeChange);

                    if (lastX != x && lastLevel != rec.batteryLevel) {
                        if (rec.time >= mHistEnd - REFERENCE_HOUR * HOUR_IN_MILLIS) {
                            if (rec.batteryStatus == lastStatus) {
                                Log.d("hong", "rec.time : " + rec.time);
                                Log.d("hong", "rec.batteryLevel : " + rec.batteryLevel);
                                if (rec.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
                                        || rec.batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                                    if (Math.abs(rec.batteryLevel - lastLevel) <= 1) {
                                        onChargingTotalLevel += Math.abs(rec.batteryLevel
                                                - lastLevel);
                                        onChargingTotalTime += Math.abs(rec.time - lastTime);
                                    }
                                } else {
                                    if (Math.abs(rec.batteryLevel - lastLevel) <= 1) {
                                        onDischargingTotalLevel += Math.abs(rec.batteryLevel
                                                - lastLevel);
                                        onDischargingTotalTime += Math.abs(rec.time - lastTime);
                                    }
                                }
                            }
                        }

                        lastStatus = rec.batteryStatus;
                        lastLevel = rec.batteryLevel;
                        lastTime = rec.time;

                        lastX = x;
                    }
                }
            }
        }

        if (curStatus == BatteryManager.BATTERY_STATUS_FULL) {
            setBatteryFull();
        } else if (curStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
            setChargingSummary(getEstimateChargingTime());
        } else {
            setDischargingSummary(getEstimateDisChargingTime());
        }
    }

    private void setBatteryFull() {
        mBattery.setTitle(String.format(getResources().getString(
                R.string.battery_info_status_full_vzw)));
        mBattery.setSummary("");
    }

    private void setChargingSummary(long estimateChargingTime) {
        if (estimateChargingTime == INVALID_ESTIMATE_CHARGING_TIME) {
            mBattery.setTitle(String.format(getResources().getString(
                    R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
            mBattery.setSummary(getResources().getString(
                    R.string.sp_powersave_battery_no_data_text_NORMAL));

            long chargingTime10MinPer1percent = (mBatHigh - lastLevel) * (minRefTime * MINUTE_IN_MILLIS); // 10 min per 1% battery (default)
            Log.d(TAG, "onChargingTotalTime : " + onChargingTotalTime + ", onChargingTotalLevel : "
                    + onChargingTotalLevel + ", mEstimateChargingTime : " + chargingTime10MinPer1percent);
        } else {
            if (millisToHour(estimateChargingTime) == 0) {
                if (millisToMinutes(estimateChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_minute_text_NORMAL,
                            millisToMinutes(estimateChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_minutes_text_NORMAL,
                            millisToMinutes(estimateChargingTime)));
                }
            } else if (millisToHour(estimateChargingTime) == 1) {
                if (millisToMinutes(estimateChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hour_minute_text_NORMAL,
                            millisToHour(estimateChargingTime),
                            millisToMinutes(estimateChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hour_minutes_text_NORMAL,
                            millisToHour(estimateChargingTime),
                            millisToMinutes(estimateChargingTime)));
                }
            }
            else {
                if (millisToMinutes(estimateChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hours_minute_text_NORMAL,
                            millisToHour(estimateChargingTime),
                            millisToMinutes(estimateChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.sp_powersave_battery_charging_expect_time_text_NORMAL)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hours_minutes_text_NORMAL,
                            millisToHour(estimateChargingTime),
                            millisToMinutes(estimateChargingTime)));
                }
            }
            Log.d(TAG, "onChargingTotalTime : " + onChargingTotalTime + ", onChargingTotalLevel : "
                    + onChargingTotalLevel + ", mEstimateChargingTime : " + estimateChargingTime);
        }
    }

    private long getEstimateChargingTime() {
        final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;

        final long chargeTime = mStats.computeChargeTimeRemaining(
                elapsedRealtimeUs);

        long estimateChargingTime;
        if (chargeTime == -1/*onChargingTotalTime < minTime * MINUTE_IN_MILLIS || onChargingTotalLevel == 0*/) {
            estimateChargingTime = INVALID_ESTIMATE_CHARGING_TIME;
        } else {
            estimateChargingTime = chargeTime / 1000;
        }

        return estimateChargingTime;
    }

    private long getEstimateDisChargingTime() {
        final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;

        final long drainTime = mStats.computeBatteryTimeRemaining(
                elapsedRealtimeUs);

        long estimateDisChargingTime;
        if (drainTime == -1/*onDischargingTotalTime < minTime * MINUTE_IN_MILLIS || onDischargingTotalLevel == 0*/) {
            estimateDisChargingTime = INVALID_ESTIMATE_DISCHARGING_TIME;
        } else {
            estimateDisChargingTime = drainTime / 1000;
        }

        return estimateDisChargingTime;
    }

    private void setDischargingSummary(long estimateDisChargingTime) {
        if (estimateDisChargingTime == INVALID_ESTIMATE_DISCHARGING_TIME) {
            long disChargingTime10MinPer1percent = lastLevel * (minRefTime * MINUTE_IN_MILLIS); // 10 min per 1% battery (default)
            mBattery.setTitle(String.format(getResources().getString(
                    R.string.powersave_estimated_remaining)));
            mBattery.setSummary(getResources().getString(
                    R.string.sp_powersave_battery_no_data_text_NORMAL));

            Log.d(TAG, "onDischargingTotalTime : " + onDischargingTotalTime
                    + ", onDischargingTotalLevel : " + onDischargingTotalLevel
                    + ", disChargingTime10MinPer1percent : " + disChargingTime10MinPer1percent);
        } else {

            if (millisToHour(estimateDisChargingTime) == 0) {
                if (millisToMinutes(estimateDisChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_minute_text_NORMAL,
                            millisToMinutes(estimateDisChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_minutes_text_NORMAL,
                            millisToMinutes(estimateDisChargingTime)));
                }
            } else if (millisToHour(estimateDisChargingTime) == 1) {
                if (millisToMinutes(estimateDisChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hour_minute_text_NORMAL,
                            millisToHour(estimateDisChargingTime),
                            millisToMinutes(estimateDisChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hour_minutes_text_NORMAL,
                            millisToHour(estimateDisChargingTime),
                            millisToMinutes(estimateDisChargingTime)));
                }

            } else {
                if (millisToMinutes(estimateDisChargingTime) == 1) {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hours_minute_text_NORMAL,
                            millisToHour(estimateDisChargingTime),
                            millisToMinutes(estimateDisChargingTime)));
                } else {
                    mBattery.setTitle(String.format(getResources().getString(
                            R.string.powersave_estimated_remaining)));
                    mBattery.setSummary(getString(
                            R.string.sp_powersave_battery_expected_time_hours_minutes_text_NORMAL,
                            millisToHour(estimateDisChargingTime),
                            millisToMinutes(estimateDisChargingTime)));
                }
            }
            Log.d(TAG, "onDischargingTotalTime : " + onDischargingTotalTime
                    + ", onDischargingTotalLevel : " + onDischargingTotalLevel
                    + ", estimateDisChargingTime : " + estimateDisChargingTime);
        }
    }

    private int millisToMinutes(long time) {
        return (int)((time / 1000) / 60) % 60;
    }

    private int millisToHour(long time) {
        return (int)(((time / 1000) / 60) / 60);
    }

    public static String getBatterySummary() {
        return batterySummary;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mBatteryPercentage) {
            Settings.System.putInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BATTERY_INDICATOR,
                    mBatteryPercentage.isChecked() ? 1 : 0);

            /* update battery percentage in indicator */
            Intent intent = new Intent(PowerSave.ACTION_POWERSAVE_BATTERY_INDICATOR_CHANGED);
            //            mContext.sendBroadcast(intent);
            mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        } else if (preference == mEcoMode) {
            int value = Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.ECO_MODE, 0);

            Settings.System.putInt(mContext.getContentResolver(),
                    SettingsConstants.System.ECO_MODE, value == 0 ? 1 : 0);
        } else if (preference == mBatteryFastBoot) {

            if (mBatteryFastBoot.isChecked() == true) {
                ShowFastBootDlg();
            } else {
                mBatteryFastBoot.setChecked(false);
                SystemProperties.set(PERSIST_ZWAIT_ENABLED, "false");
                Log.d(TAG, "PERSIST_ZWAIT_ENABLED False");
            }
        } else if (preference == mBatterySmart) {
            int value = Settings.System.getInt(mContext.getContentResolver(),
                    "power_save_smart_charge", 0);

            Settings.System.putInt(mContext.getContentResolver(), "power_save_smart_charge",
                    value == 0 ? 1 : 0);
            setSmartCharging();

        } else if (preference == mBatterySmartPowerSaver) {
            int value = Settings.System.getInt(mContext.getContentResolver(),
                    "power_save_smart_saver", 1);
            Settings.System.putInt(mContext.getContentResolver(), "power_save_smart_saver",
                    value == 0 ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void setSmartCharging() {

        String batteryLevel = com.android.settings.Utils.getBatteryPercentage(mSmartIntent);
        String batteryStatus = com.android.settings.Utils.getSmartBatteryStatus(
                getResources(), mSmartIntent, mContext);
        // yonguk.kim 20120323 Remove Not charging string requested by UI
        Locale systemLocale = getResources().getConfiguration().locale;
        String language = systemLocale.getLanguage();
        if (language.equals("ko")
                && (batteryStatus.equals(getResources().getString(
                        R.string.battery_info_status_discharging)) ||
                batteryStatus.equals(getResources().getString(
                        R.string.battery_info_status_not_charging)))) {
            batterySummary = batteryLevel;
        } else {
            batterySummary = mContext.getResources().getString(
                    R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
        }
        if (batteryStatus.equals(getResources().getString(
                R.string.sp_power_save_battery_smart_charge))) {

            Log.d(TAG, "Smart Charging VISIBLE");
            sSmartChargOn = true;
        } else {
            Log.d(TAG, "Smart Charging INVISIBLE");
            sSmartChargOn = false;
        }
    }

    public static boolean getSmartChargeOn() {
        return sSmartChargOn;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
    }

    private void updatePowerSaverModeSummary(Object value) {
        try {
            if ("-1".equals(value)) { // Never activate
                mPowerSaveSwitch
                        .setSummary(getString(R.string.sp_activate_power_saver_never_summary_NORMAL));
            } else {
                String immediatly_value = Integer
                        .toString(PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY);
                if (immediatly_value.equals(value.toString())) { // Immediately
                    String[] entry = getResources().getStringArray(
                            R.array.sp_power_save_mode2_entries_NORMAL); // yonguk.kim 20120522 WBT
                    mPowerSaveSwitch.setSummary(entry[0]);
                } else {
                    if (Utils.isRTLLanguage()) {
                        mPowerSaveSwitch.setSummary(getString(
                                R.string.sp_power_saver_on_summary_NORMAL,
                                String.format(Locale.getDefault(), "%d",
                                        Integer.parseInt(value.toString()))
                                        + "%"));
                    } else {
                        mPowerSaveSwitch.setSummary(getString(
                                R.string.sp_power_saver_on_summary_NORMAL,
                                "\u200E" + value.toString() + "%"));
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist Auto power save mode", e);
        }
    }

    private void updateBatterySaverSummary() {

        int modeValue = Settings.Global
                .getInt(getContentResolver(), "battery_saver_mode_value", 15);

        if (modeValue == PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY) {
            String[] entry = getResources().getStringArray(
                    R.array.sp_battery_saver_mode_entries_NORMAL);
            mBatterySaver.setSummary(entry[0]);
        } else {
            if (modeValue != 0) {
                if (!(Utils.isRTLLanguage())) {
                    mBatterySaver.setSummary(getString(R.string.sp_power_saver_on_summary_NORMAL,
                        "\u200E" + modeValue + "%"));
                } else {
                    if ("ar".equals(Locale.getDefault().getLanguage())) {
                        mBatterySaver.setSummary(getString(R.string.sp_power_saver_on_summary_NORMAL,
                        String.format(Locale.getDefault(), "%d", modeValue) + "%"));
                    } else {
                        mBatterySaver.setSummary(getString(R.string.sp_power_saver_on_summary_NORMAL,
                        "%" + String.format(Locale.getDefault(), "%d", modeValue)));
                    }
                }
            } else {
                mBatterySaver.setSummary(getString(
                        R.string.sp_activate_power_saver_never_summary_NORMAL));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            if (settingStyle == 1 && Utils.supportEasySettings(getActivity())) {
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                Log.d("kimyow", "top=" + topActivityClassName + "  base=" + baseActivityClassName);
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    getActivity().onBackPressed();
                    return true;
                } else {
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                    Log.d("kimyow", "tabIndex=" + tabIndex);
                    settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                    startActivity(settings);
                    finish();
                    return true;
                }
            }
            break;
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getEasyTabIndex(String topActivity) {
        if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity)) {
            return "sound";
        } else if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity) ||
                "com.android.settings.Settings$LocationSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$AccountSyncSettingsActivity".equals(topActivity)) {
            return "general";
        }
        return null;
    }

    private Layout makeLayout(CharSequence text) {
        final Resources res = getResources();
        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.density = res.getDisplayMetrics().density;
        paint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);
        paint.setColor(Color.BLACK);
        paint.setTextSize(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, res.getDisplayMetrics()));

        return new StaticLayout(text, paint,
                (int)Math.ceil(Layout.getDesiredWidth(text, paint)),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    private void ShowFastBootDlg() {
        Builder mAlerter = new AlertDialog.Builder(getActivity());
        mAlerter.setTitle(R.string.sp_power_save_fastboot_title);
        mAlerter.setMessage(R.string.sp_power_save_fastboot_dlg_summary);
        mAlerter.setPositiveButton(R.string.dlg_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mBatteryFastBoot.setChecked(true);
                        SystemProperties.set(PERSIST_ZWAIT_ENABLED,
                                mBatteryFastBoot.isChecked() ? "true" : "false");
                        Log.d(TAG, "PERSIST_ZWAIT_ENABLED True");
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mBatteryFastBoot.setChecked(false);
                SystemProperties.set(PERSIST_ZWAIT_ENABLED,
                        mBatteryFastBoot.isChecked() ? "true" : "false");
                Log.d(TAG, "PERSIST_ZWAIT_ENABLED False");

            }
        }).show();
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            
            String screenTitle = context.getString(R.string.sp_battery_power_saving_title);
            // Battery main
            setSearchIndexData(context, "battery_settings", context.getString(R.string.sp_battery_power_saving_title),
                    "main", null, null, "com.lge.settings.BATTERY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);

            setSearchIndexData(context, "battery_usage",
                    context.getString(R.string.sp_battery_usage_title),
                    screenTitle, null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$PowerUsageSummaryActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);

            // battery percentage on status bar
            setSearchIndexData(
                    context,
                    KEY_POWER_SAVE_BATTERY_PERCENTAGE,
                    context.getString(R.string.sp_power_save_battery_percentage_NORMAL2), // checkbox
                    screenTitle, null, null,
                    "com.lge.settings.BATTERY_SETTINGS", null, null, 1,
                    "CheckBox", "System", "power_save_battery_indicator", 1, 0);

            setSearchIndexData(context, KEY_BATTERY_SAVER,
                    context.getString(R.string.sp_battery_saver_NORMAL),
                    screenTitle, null, null,
                    "com.lge.settings.BatterySaverSettings", null, null, 1,
                    "Switch", "Global", "battery_saver_mode", 1, 0);

            setSearchIndexData(context, POWER_SAVER_TIPS,
                    context.getString(R.string.sp_battery_saver_tips_NORMAL2),
                    screenTitle, null, null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$BatterySaverTipsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
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
