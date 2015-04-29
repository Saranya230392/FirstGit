/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport.BatteryInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
//import com.android.settings.IconPreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.PowerUsageDetail.DrainType;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.powersave.PowerSave;
import com.android.settings.SettingsBreadCrumb;

import com.lge.constants.SettingsConstants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Displays a list of apps and subsystems that consume power, ordered by how much power was
 * consumed since the last time it was unplugged.
 */
public class PowerSaveBatteryDetail extends PreferenceFragment implements
        PowerGaugePreference2.OnTreeClickListener {

    private static final boolean DEBUG = false;

    private static final String TAG = "PowerSaveBatteryDetail";

    private static final String KEY_GRAPH = "graph";
    private static final String KEY_APP_LIST = "app_list";
    private static final int MENU_STATS_TYPE = Menu.FIRST;
    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
    private static final int MENU_HELP = Menu.FIRST + 2;



    IBatteryStats mBatteryInfo;
    UserManager mUm;


    private final List<BatterySipper> mWifiSippers = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mBluetoothSippers = new ArrayList<BatterySipper>();


    private PreferenceGroup mAppListGroup;
    private PreferenceGroup mGraphGroup;
    //private Preference mBatteryPercent;

    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

    //private static final int MIN_POWER_THRESHOLD = 5;
    private static final int MAX_ITEMS_TO_LIST = 10;

    private long mStatsPeriod = 0;

    /** Queue for fetching name and icon for an application */
    private ArrayList<BatterySipper> mRequestQueue = new ArrayList<BatterySipper>();

    //jongtak0920 remove not use instance
    //    private PowerSave mPowerSave;



    //jongtak
    public static final String REFRESH_INTENT = "refresh";

    private PowerSaveBatteryPreference2 hist;
    private Preference useOnBatteryPref;
    private Preference expectedTimePref;
    private Preference lastTimePref;

    private int totalLevel;
    private int status = BatteryManager.BATTERY_STATUS_UNKNOWN;

    public static final int CHART_STATUS_DISCHARGING = 0;
    public static final int CHART_STATUS_CHARGING = 1;
    public static final int CHART_STATUS_NONE = 2;

    private static int lastScrollX;

    private boolean first = true;
    private boolean onRotation = false;

    private static int curLevel;
    private static int lastState = CHART_STATUS_NONE;
    private static int curState = CHART_STATUS_NONE;

    private String mStringUse;
    private String mStringNoData;

    private Context mContext;

    private BatteryStatsHelper mStatsHelper;

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        long expectedTime;
        long onBatteryTime;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (REFRESH_INTENT.equals(action)) {
                refreshInformation();
            }

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                curLevel = intent.getIntExtra("level", 0);
                status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);

                Log.i(TAG,
                        "Battery Status : "
                                + com.android.settings.Utils.getBatteryStatus(getResources(),
                                        intent));

                if (status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL) {
                    curState = CHART_STATUS_CHARGING;
                } else {
                    curState = CHART_STATUS_DISCHARGING;
                }

                if (curState != lastState) {
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL) {
                        lastState = CHART_STATUS_CHARGING;
                    } else {
                        lastState = CHART_STATUS_DISCHARGING;
                    }
                    refreshInformation();
                }
            }
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUm = (UserManager)activity.getSystemService(Context.USER_SERVICE);
        mStatsHelper = new BatteryStatsHelper(activity, true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        onRotation = true;

        if (hist.getChart() != null) {
            lastScrollX = hist.getHorizontalScrollView().getScrollX();
            lastScrollX = (this.getResources().getDisplayMetrics().widthPixels * lastScrollX)
                    / this.getResources().getDisplayMetrics().heightPixels;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mStatsHelper.create(icicle);

        mContext = getActivity().getApplicationContext();

        addPreferencesFromResource(R.xml.powersave_usage_summary);

        // Change KK 2013_09_30
        //      mBatteryInfo = IBatteryStats.Stub.asInterface(
        //              ServiceManager.getService("batteryinfo"));

        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService(BatteryStats.SERVICE_NAME));


        //mBatteryPercent = findPreference("powersaveuse");
        //mBatteryPercent.setSelectable(false);
        //mBatteryPercent.setTitle(TAG);
        mAppListGroup = (PreferenceGroup)findPreference(KEY_APP_LIST);
        mGraphGroup = (PreferenceGroup)findPreference(KEY_GRAPH);

        //jongtak0920 remove not use instance
        //        mPowerSave = new PowerSave(getActivity());

        useOnBatteryPref = new Preference(getActivity());
        useOnBatteryPref.setLayoutResource(R.layout.preference_battery_info);
        useOnBatteryPref.setSelectable(false);
        useOnBatteryPref.setIcon(R.drawable.img_battery_list_clock);

        expectedTimePref = new Preference(getActivity());
        expectedTimePref.setLayoutResource(R.layout.preference_battery_info);
        expectedTimePref.setSelectable(false);
        expectedTimePref.setIcon(R.drawable.img_battery_list_clock_plus);

        lastTimePref = new Preference(getActivity());
        lastTimePref.setLayoutResource(R.layout.preference_battery_info);
        lastTimePref.setSelectable(false);
        lastTimePref.setIcon(R.drawable.img_battery_list_portion);

        if (Utils.isFolderModel(mContext)) {
            useOnBatteryPref.setSelectable(true);
            expectedTimePref.setSelectable(true);
            lastTimePref.setSelectable(true);
        }

        mStringUse = getResources().getString(R.string.sp_powersave_battery_use_text_NORMAL);
        mStringNoData = getResources().getString(R.string.sp_powersave_battery_no_data_text_NORMAL);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
                //actionBar.setDisplayHomeAsUpEnabled(true);
                getActivity().setTitle(R.string.sp_battery_usage_title);
            }
        }

        if (isOverconsumMenu() || isUnnecessaryMenu()) {
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mStatsHelper.clearStats();
    }

    @Override
    public void onResume() {
        super.onResume();

        // jongtak0920.kim refresh when preference created
        lastState = CHART_STATUS_NONE;
        curState = CHART_STATUS_NONE;
        first = true;

        refreshStats();

        // jongtak0920.kim register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(REFRESH_INTENT);
        getActivity().registerReceiver(mBatteryInfoReceiver, filter);
        // jongtak0920
        onRotation = false;

        if (Utils.supportSplitView(getActivity())) {
            SettingsBreadCrumb breadCrumb = SettingsBreadCrumb.get(getActivity());
            if (breadCrumb != null) {
                breadCrumb.setTitle(getString(R.string.sp_battery_usage_title));
            }
        }

    }

    @Override
    public void onPause() {
        synchronized (mRequestQueue) {
        }
        mHandler.removeMessages(MSG_UPDATE_NAME_ICON);

        // jongtak0920.kim register receiver
        getActivity().unregisterReceiver(mBatteryInfoReceiver);

        lastState = CHART_STATUS_NONE;
        curState = CHART_STATUS_NONE;
        first = false;

        BatteryEntry.stopRequestQueue();

        super.onPause();
    }

    @Override
    public void onDestroy() {

        Utils.recycleView(getView());
        System.gc();

        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            mStatsHelper.storeState();
        } else {
            BatteryEntry.clearUidCache();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        /* if (preference instanceof BatteryHistoryPreference) {
             Parcel hist = Parcel.obtain();
             mStats.writeToParcelWithoutUids(hist, 0);
             byte[] histData = hist.marshall();
             Intent intent = new Intent(this, BatteryHistoryDetail.class);
             intent.putExtra(BatteryHistoryDetail.EXTRA_STATS, histData);
             startActivity(intent);
             return super.onPreferenceTreeClick(preferenceScreen, preference);
         }*/
        if (!(preference instanceof PowerGaugePreference)) {
            return false;
        }
        PowerGaugePreference2 pgp = (PowerGaugePreference2)preference;
        mStatsHelper.getStats();

        final int dischargeAmount = mStatsHelper.getStats().getDischargeAmount(mStatsType);
        BatteryEntry entry = pgp.getInfo();

        Bundle args = new Bundle();
        args.putString(PowerUsageDetail.EXTRA_TITLE, entry.name);
        args.putInt(PowerUsageDetail.EXTRA_PERCENT, (int)
                ((entry.sipper.value * dischargeAmount / mStatsHelper.getTotalPower()) + .5));
        args.putInt(PowerUsageDetail.EXTRA_GAUGE, (int)
                (int)Math.ceil(entry.sipper.value * 100 / mStatsHelper.getMaxPower()));
        args.putLong(PowerUsageDetail.EXTRA_USAGE_DURATION, mStatsPeriod);
        args.putString(PowerUsageDetail.EXTRA_ICON_PACKAGE, entry.defaultPackageName);
        args.putInt(PowerUsageDetail.EXTRA_ICON_ID, entry.iconId);
        args.putDouble(PowerUsageDetail.EXTRA_NO_COVERAGE, entry.sipper.noCoveragePercent);
        if (entry.sipper.uidObj != null) {
            args.putInt(PowerUsageDetail.EXTRA_UID, entry.sipper.uidObj.getUid());
        }
        args.putSerializable(PowerUsageDetail.EXTRA_DRAIN_TYPE, entry.sipper.drainType);

        int[] types;
        double[] values;
        switch (entry.sipper.drainType) {
        case APP:
        case USER: {
            Uid uid = entry.sipper.uidObj;
            types = new int[] {
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_gps,
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv,
                    R.string.usage_type_audio,
                    R.string.usage_type_video,
            };
            values = new double[] {
                    entry.sipper.cpuTime,
                    entry.sipper.cpuFgTime,
                    entry.sipper.wakeLockTime,
                    entry.sipper.gpsTime,
                    entry.sipper.wifiRunningTime,
                    entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes,
                    0,
                    0
            };

            try {
                if (entry.sipper.drainType == BatterySipper.DrainType.APP) {
                    Writer result = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(result);
                    //[S][2012.01.29][jm.lee2][common] WBT.310915
                    if (uid != null) {
                        mStatsHelper.getStats().dumpLocked(mContext, printWriter, "",
                                mStatsType, uid.getUid());
                    }
                    //[E][2012.01.29][jm.lee2][common] WBT.310915
                    args.putString(PowerUsageDetail.EXTRA_REPORT_DETAILS, result.toString());

                    result = new StringWriter();
                    printWriter = new PrintWriter(result);
                    //[S][2012.01.29][jm.lee2][common] WBT.310915
                    if (uid != null) {
                        mStatsHelper.getStats().dumpCheckinLocked(mContext, printWriter,
                                mStatsType, uid.getUid());
                    }
                    args.putString(PowerUsageDetail.EXTRA_REPORT_CHECKIN_DETAILS,
                            result.toString());
                }
            } catch (NullPointerException npe) {
                Log.w(TAG, "NullPointerException");
            }
            //[E][2012.01.29][jm.lee2][common] WBT.310915
        }
            break;
        case CELL: {
            types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_no_coverage
            };
            values = new double[] {
                    entry.sipper.usageTime,
                    entry.sipper.noCoveragePercent
            };
        }
            break;
        case WIFI: {
            types = new int[] {
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv,
            };
            values = new double[] {
                    entry.sipper.usageTime,
                    entry.sipper.cpuTime,
                    entry.sipper.cpuFgTime,
                    entry.sipper.wakeLockTime,
                    entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes,
            };
        }
            break;
        case BLUETOOTH: {
            types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv,
            };
            values = new double[] {
                    entry.sipper.usageTime,
                    entry.sipper.cpuTime,
                    entry.sipper.cpuFgTime,
                    entry.sipper.wakeLockTime,
                    entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes,
            };
        }
            break;
        default: {
            types = new int[] {
                    R.string.usage_type_on_time
            };
            values = new double[] {
                    entry.sipper.usageTime
            };
        }
        }
        args.putIntArray(PowerUsageDetail.EXTRA_DETAIL_TYPES, types);
        args.putDoubleArray(PowerUsageDetail.EXTRA_DETAIL_VALUES, values);

        // 20131113 yonguk.kim Apply UsageManager for ATT
        if (getActivity() instanceof PreferenceActivity) {
            PreferenceActivity pa = (PreferenceActivity)getActivity();
            pa.startPreferencePanel(PowerUsageDetail.class.getName(), args,
                    R.string.details_title, null, null, 0);
        } else {
            Intent intent = Utils.buildStartFragmentIntent(PowerUsageDetail.class.getName(), args,
                    R.string.details_title, 0, getActivity(), R.drawable.shortcut_battery);
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) {
            menu.add(0, MENU_STATS_TYPE, 0, R.string.menu_stats_total)
                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
                    .setAlphabeticShortcut('t');
        }
        MenuItem refresh = menu.add(0, MENU_STATS_REFRESH, 0, R.string.menu_stats_refresh)
                .setIcon(R.drawable.ic_menu_refresh_holo_dark)
                .setAlphabeticShortcut('r');
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        refresh.setVisible(false);
        String helpUrl;
        if (!TextUtils.isEmpty(helpUrl = getResources().getString(R.string.help_url_battery))) {
            final MenuItem help = menu.add(0, MENU_HELP, 0, R.string.help_label);
            Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
            helpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            help.setIntent(helpIntent);
            help.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            help.setVisible(false);
        }

        // L-OS Overconsumption info. Unnecessary consumption Menu
        inflater.inflate(R.menu.battery_use_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub

        menu.findItem(R.id.battery_drain_info).setVisible(
                isOverconsumMenu() ? true : false);
        menu.findItem(R.id.restrict_idle_apps).setVisible(
                isUnnecessaryMenu() ? true : false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        ComponentName c;
        switch (item.getItemId()) {
        case MENU_STATS_TYPE:
            if (mStatsType == BatteryStats.STATS_SINCE_CHARGED) {
                mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
            } else {
                mStatsType = BatteryStats.STATS_SINCE_CHARGED;
            }
            refreshStats();
            return true;

        case MENU_STATS_REFRESH:

            refreshStats();
            return true;

        case android.R.id.home:
            getActivity().finish();
            return true;

        case R.id.battery_drain_info:
            intent = new Intent();
            c = new ComponentName("com.lge.eodengine",
                    "com.lge.eodengine.EodAppsManageActivity");
            intent.setComponent(c);
            startActivity(intent);
            return true;

        case R.id.restrict_idle_apps:
            intent = new Intent();
            c = new ComponentName("com.lge.lpd",
                    "com.lge.lpd.BatteryConsumptionAppsManageActivity");
            intent.setComponent(c);
            startActivity(intent);
            return true;

        default:
            return false;
        }
    }

    private boolean isOverconsumMenu() {
        return Utils.checkPackage(mContext, "com.lge.eodengine");
    }

    private boolean isUnnecessaryMenu() {
        return Utils.checkPackage(mContext, "com.lge.lpd");
    }

    // jongtak0920.kim not use method
    //    private void addNotAvailableMessage() {
    //        Preference notAvailable = new Preference(getActivity());
    //        notAvailable.setTitle(R.string.power_usage_not_available);
    //        mAppListGroup.addPreference(notAvailable);
    //    }
    
    enum DrainType {
        IDLE,
        CELL,
        PHONE,
        WIFI,
        BLUETOOTH,
        FLASHLIGHT,
        SCREEN,
        APP,
        USER,
        UNACCOUNTED,
        OVERCOUNTED,
    }

    private void refreshStats() {

        mAppListGroup.removeAll();
        mGraphGroup.removeAll();

        mWifiSippers.clear();
        mBluetoothSippers.clear();

        

        mStatsPeriod = mStatsHelper.getStats().computeBatteryRealtime(
                SystemClock.elapsedRealtime() * 1000, mStatsType);

        mAppListGroup.setOrderingAsAdded(true);
        mGraphGroup.setOrderingAsAdded(true);

        //jongtak0920
        Preference useDetailTextPref = new Preference(getActivity());
        useDetailTextPref.setLayoutResource(R.layout.battery_use_detail_preference_information);
        useDetailTextPref.setTitle(getResources().getString(
                R.string.sp_powersave_battery_use_detail_summary_NORMAL));
        useDetailTextPref.setSelectable(false);
        mAppListGroup.addPreference(useDetailTextPref);

        Preference graphTipsPref = new Preference(getActivity());
        graphTipsPref.setLayoutResource(R.layout.battery_use_detail_preference_information);
        graphTipsPref.setTitle(getResources().getString(
                R.string.sp_powersave_battery_graph_information_summary_NORMAL));
        graphTipsPref.setSelectable(false);
        graphTipsPref.setOrder(-2);
        mGraphGroup.addPreference(graphTipsPref);

        // pole1101
        hist = new PowerSaveBatteryPreference2(getActivity(), mStatsHelper.getStats());
        hist.setOrder(-1);
        hist.setSelectable(false);
        //mAppListGroup.addPreference(hist);

        mGraphGroup.addPreference(hist);

        //jongtak0920[START]
        mGraphGroup.addPreference(useOnBatteryPref);
        mGraphGroup.addPreference(expectedTimePref);
        mGraphGroup.addPreference(lastTimePref);
        // jongtak0920 [END]

        boolean bSkipWiFi = false;

        if ((Config.getOperator().equals("VZW")) &&
                (android.provider.Settings.System.getInt(getActivity().getContentResolver(),
                        SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0) == 1)) {
            bSkipWiFi = true;
        }        
        final List<UserHandle> profiles = mUm.getUserProfiles();
        mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);
        final List<BatterySipper> mUsageList = mStatsHelper.getUsageList();
        
        Log.d(TAG, "mUsageList : " + mUsageList.size());

        for (BatterySipper sipper : mUsageList) {
            //            if (sipper.getSortValue() < MIN_POWER_THRESHOLD) continue;

            final BatteryEntry entry = new BatteryEntry(getActivity(), mHandler, mUm, sipper);
            double[] values = entry.sipper.getValues();
            
            switch (sipper.drainType) {
            case UNACCOUNTED:
                continue;
            case OVERCOUNTED:
                continue;
            case USER:
                UserInfo info = mUm.getUserInfo(sipper.userId);
                if (info == null) {
                    continue;
                }
            default:
                break;
            }
        
            final double percentOfTotal = ((values[0] / mStatsHelper.getComputedPower()) * 100);

            if (percentOfTotal < 1) {
                continue;
            }
            final UserHandle userHandle = new UserHandle(UserHandle.getUserId(sipper.getUid()));
            final Drawable badgedIcon = mUm.getBadgedIconForUser(entry.getIcon(), userHandle);
            final CharSequence contentDescription = mUm.getBadgedLabelForUser(entry.getLabel(),
                    userHandle);

            final PowerGaugePreference2 pref = new PowerGaugePreference2(getActivity(),
                    badgedIcon, contentDescription, entry);
            final double percentOfMax = (values[0] * 100) / mStatsHelper.getMaxPower();
            pref.setSummary(R.string.details_title);
            pref.setTitle(entry.getLabel());
            pref.setPercent(percentOfMax, percentOfTotal);
            if (sipper.uidObj != null) {
                pref.setKey(Integer.toString(entry.sipper.uidObj.getUid()));
            }
            //jongtak
            pref.setOnTreeClickListener(this);
            mAppListGroup.addPreference(pref);

            // Remove Wifi for VZW
            if ((bSkipWiFi == true) && (entry.name.equals("Wi-Fi"))) {
                Log.i(TAG, "##feature remove Battery use details - Wi-Fi");
                mAppListGroup.removePreference(pref);
            }

            if (entry.defaultPackageName != null) {
                if (entry.defaultPackageName.equals("com.skt.prod.phone")) {
                    mAppListGroup.removePreference(pref);
                }
            }

            if (entry.name != null) {
                if (entry.name.equals("com.skt.prod.phone")) {
                    mAppListGroup.removePreference(pref);
                }
            }

            if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST + 1)) {
                break;
            }
        }
        BatteryEntry.startRequestQueue();
        
        final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;

        final long drainTime = mStatsHelper.getStats().computeBatteryTimeRemaining(
                elapsedRealtimeUs);
        final long chargeTime = mStatsHelper.getStats().computeChargeTimeRemaining(
                elapsedRealtimeUs);

        Log.d("jw", "drainTime : " + drainTime);
        Log.d("jw", "chargeTime : " + chargeTime);
    }

    private void refreshInformation() {
        long expectedTime;

        if (hist.getChart() == null) {
            return;
        }

        Log.i(TAG, "refreshInformation()");

        if (first) {
            lastScrollX = hist.getChart().getScrollPosition();
            first = false;
        }

        int mDotHeight = hist.getImageView().getDrawable().getIntrinsicHeight() / 2;
        int mDotWidth = hist.getImageView().getDrawable().getIntrinsicWidth() / 2;

        Log.d(TAG, "mDotHeight : " + mDotHeight);
        Log.d(TAG, "mDotWidth : " + mDotWidth);

        final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;

        final long drainTime = mStatsHelper.getStats().computeBatteryTimeRemaining(
                elapsedRealtimeUs);
        final long chargeTime = mStatsHelper.getStats().computeChargeTimeRemaining(
                elapsedRealtimeUs);

        if (curState == CHART_STATUS_CHARGING) {
            expectedTime = hist.getChart().getEstimateChargingTime();
            totalLevel = hist.getChart().getOnDischargingTotalLevel();

            useOnBatteryPref.setTitle(getResources().getString(
                    R.string.sp_powersave_battery_usage_on_battery_text_NORMAL));
            useOnBatteryPref.setSummary(com.android.settings.fuelgauge.Utils.formatOnBatteryTime(
                    getActivity(), mStatsPeriod / 1000));

            expectedTimePref.setTitle(getResources().getString(
                    R.string.sp_powersave_battery_charging_expect_time_text_NORMAL));

            expectedTime = chargeTime / 1000;
            if (chargeTime == -1 /*hist.getChart().getChargingDefaultstate() == hist.getChart().DEFAULT_LINE*/) {
                expectedTimePref.setSummary(mStringNoData);
            } else if (millisToHour(expectedTime) == 0) {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_minute_text_NORMAL,
                            millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_minutes_text_NORMAL,
                            millisToMinutes(expectedTime)));
                }

            } else if (millisToHour(expectedTime) == 1) {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hour_minute_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hour_minutes_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                }

            } else {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hours_minute_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hours_minutes_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                }
            }

            lastTimePref.setTitle(getResources().getString(
                    R.string.sp_powersave_battery_last_three_hour_text_NORMAL));
            lastTimePref.setSummary(getResources().getString(
                    R.string.sp_powersave_battery_during_charging_text_NORMAL));

            hist.getChart().invalidate();

            Log.i(TAG, "Image Location : "
                    + (hist.getChart().getLastTimePosition() - mDotWidth));
            Log.i(TAG, "Image Location : "
                    + (hist.getChart().getLastLevelPosition() - mDotHeight));

            hist.getImageView().setPaddingRelative(
                    hist.getChart().getLastTimePosition() - mDotWidth,
                    hist.getChart().getLastLevelPosition() - mDotHeight, 0, 0);

            hist.getImageView().setVisibility(View.VISIBLE);

            hist.getHorizontalScrollView().post(new Runnable() {
                public void run() {
                	if (hist == null || hist.getChart() == null) {
                		return;
                	}
                	
                    if (hist.getChart().getScrollPosition() != hist.getChart().getScrollX()) {
                        if (onRotation) {
                            hist.getHorizontalScrollView().smoothScrollTo(lastScrollX, 0);
                            onRotation = false;
                        } else {
                            hist.getHorizontalScrollView().smoothScrollTo(
                                    hist.getChart().getScrollPosition(), 0);
                        }
                    }
                }
            });
        } else {
            expectedTime = hist.getChart().getEstimateDischargingTime();
            expectedTime = drainTime / 1000;
            totalLevel = hist.getChart().getOnDischargingTotalLevel();

            useOnBatteryPref.setTitle(getResources().getString(
                    R.string.sp_powersave_battery_usage_on_battery_text_NORMAL));
            useOnBatteryPref.setSummary(com.android.settings.fuelgauge.Utils.formatOnBatteryTime(
                    getActivity(), mStatsPeriod / 1000));

            expectedTimePref.setTitle(getResources().getString(
                    R.string.powersave_estimated_remaining));

            if (drainTime == -1/*hist.getChart().getDischargingDefaultstate() == hist.getChart().DEFAULT_LINE*/) {
                expectedTimePref.setSummary(mStringNoData);
            } else if (millisToHour(expectedTime) == 0) {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_minute_text_NORMAL,
                            millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_minutes_text_NORMAL,
                            millisToMinutes(expectedTime)));
                }

            } else if (millisToHour(expectedTime) == 1) {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hour_minute_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hour_minutes_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                }

            } else {
                if (millisToMinutes(expectedTime) == 1) {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hours_minute_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                } else {
                    expectedTimePref.setSummary(getActivity().getString(
                            R.string.sp_powersave_battery_expected_time_hours_minutes_text_NORMAL,
                            millisToHour(expectedTime), millisToMinutes(expectedTime)));
                }
            }

            lastTimePref.setTitle(getResources().getString(
                    R.string.sp_powersave_battery_last_three_hour_text_NORMAL));
            if (Utils.isRTLLanguage()) {
                lastTimePref.setSummary(String.format(Locale.getDefault(), "%d", totalLevel)
                        + "\u200F" + "% " + mStringUse);

            } else {
                lastTimePref.setSummary(String.format(Locale.getDefault(), "%d", totalLevel) + "% "
                        + mStringUse);
            }

            hist.getChart().invalidate();

            Log.i(TAG, "Image Location : "
                    + (hist.getChart().getLastTimePosition() - mDotWidth));
            Log.i(TAG, "Image Location : "
                    + (hist.getChart().getLastLevelPosition() - mDotHeight));

            hist.getImageView().setPaddingRelative(
                    hist.getChart().getLastTimePosition() - mDotWidth,
                    hist.getChart().getLastLevelPosition() - mDotHeight, 0, 0);

            hist.getImageView().setVisibility(View.VISIBLE);

            hist.getHorizontalScrollView().post(new Runnable() {
                public void run() {
                    if (hist.getChart().getScrollPosition() != hist.getChart().getScrollX()) {
                        if (onRotation) {
                            hist.getHorizontalScrollView().smoothScrollTo(lastScrollX, 0);
                            onRotation = false;
                        } else {
                            hist.getHorizontalScrollView().smoothScrollTo(
                                    hist.getChart().getScrollPosition(), 0);
                        }
                    }
                }
            });
        }

    }

    private int millisToMinutes(long time) {
        return (int)((time / 1000) / 60) % 60;
    }

    private int millisToHour(long time) {
        return (int)(((time / 1000) / 60) / 60);
    }

    public static int getCurLevel() {
        return curLevel;
    }

    public static int getLastState() {
        return lastState;
    }

    public static int getCurState() {
        return curState;
    }

    public static void setLastScrollX(int lastScrollX) {
        PowerSaveBatteryDetail.lastScrollX = lastScrollX;
    }


    static final int MSG_UPDATE_NAME_ICON = 1;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_NAME_ICON:

                BatteryEntry entry = (BatteryEntry)msg.obj;

                PowerGaugePreference2 pgp =
                        (PowerGaugePreference2)findPreference(
                        Integer.toString(entry.sipper.uidObj.getUid()));
                if (pgp != null) {
                    final int userId = UserHandle.getUserId(entry.sipper.getUid());
                    final UserHandle userHandle = new UserHandle(userId);
                    pgp.setIcon(mUm.getBadgedIconForUser(entry.getIcon(), userHandle));
                    pgp.setTitle(entry.name);
                }
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }
    };

    //jongtak
    public void onTreeClick(Preference preference) {
        // TODO Auto-generated method stub
        if (!(preference instanceof PowerGaugePreference2)) {
            return;
        }
        PowerGaugePreference2 pgp = (PowerGaugePreference2)preference;

        mStatsHelper.getStats();
        BatteryEntry entry = pgp.getInfo();
        double[] mValues = entry.sipper.getValues();

        Bundle args = new Bundle();
        args.putString(PowerUsageDetail.EXTRA_TITLE, entry.name);

        args.putInt(PowerUsageDetail.EXTRA_PERCENT,
                ((int)Math.round(mValues[0] * 100 / mStatsHelper.getComputedPower())));
        args.putInt(PowerUsageDetail.EXTRA_GAUGE, 
                ((int)Math.round(mValues[0] * 100 / mStatsHelper.getComputedPower())));

        args.putLong(PowerUsageDetail.EXTRA_USAGE_DURATION, mStatsPeriod);
        args.putString(PowerUsageDetail.EXTRA_ICON_PACKAGE, entry.defaultPackageName);
        args.putInt(PowerUsageDetail.EXTRA_ICON_ID, entry.iconId);
        args.putDouble(PowerUsageDetail.EXTRA_NO_COVERAGE, entry.sipper.noCoveragePercent);
        if (entry.sipper.uidObj != null) {
            args.putInt(PowerUsageDetail.EXTRA_UID, entry.sipper.uidObj.getUid());
        }
        args.putSerializable(PowerUsageDetail.EXTRA_DRAIN_TYPE, entry.sipper.drainType);

        int[] types;
        double[] values;
        switch (entry.sipper.drainType) {
        case APP: {
            Uid uid = entry.sipper.uidObj;
            types = new int[] {
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock, R.string.usage_type_gps,
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv, R.string.usage_type_audio,
                    R.string.usage_type_video, };
            values = new double[] {
                    entry.sipper.cpuTime, entry.sipper.cpuFgTime,
                    entry.sipper.wakeLockTime, entry.sipper.gpsTime,
                    entry.sipper.wifiRunningTime, entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes, 0, 0 };

            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            // [S][2012.01.29][jm.lee2][common] WBT.310915
            if (uid != null) {
                mStatsHelper.getStats().dumpLocked(mContext, printWriter, "", mStatsType,
                        uid.getUid());
            }
            // [E][2012.01.29][jm.lee2][common] WBT.310915
            args.putString(PowerUsageDetail.EXTRA_REPORT_DETAILS,
                    result.toString());

            result = new StringWriter();
            printWriter = new PrintWriter(result);
            // [S][2012.01.29][jm.lee2][common] WBT.310915
            if (uid != null) {
                mStatsHelper.getStats().dumpCheckinLocked(mContext, printWriter, mStatsType,
                        uid.getUid());
            }
            // [E][2012.01.29][jm.lee2][common] WBT.310915
            args.putString(PowerUsageDetail.EXTRA_REPORT_CHECKIN_DETAILS,
                    result.toString());
        }
            break;
        case CELL: {
            types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_no_coverage };
            values = new double[] { entry.sipper.usageTime, entry.sipper.noCoveragePercent };
        }
            break;
        case WIFI: {
            types = new int[] {
                    R.string.usage_type_wifi_running,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv, };
            values = new double[] { entry.sipper.usageTime, entry.sipper.cpuTime,
                    entry.sipper.cpuFgTime, entry.sipper.wakeLockTime, entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes, };
        }
            break;
        case BLUETOOTH: {
            types = new int[] {
                    R.string.usage_type_on_time,
                    R.string.usage_type_cpu,
                    R.string.usage_type_cpu_foreground,
                    R.string.usage_type_wake_lock,
                    R.string.usage_type_data_send,
                    R.string.usage_type_data_recv, };
            values = new double[] {
                    entry.sipper.usageTime, entry.sipper.cpuTime,
                    entry.sipper.cpuFgTime, entry.sipper.wakeLockTime, entry.sipper.mobileTxBytes,
                    entry.sipper.mobileRxBytes, };
        }
            break;
        default: {
            types = new int[] { R.string.usage_type_on_time };
            values = new double[] { entry.sipper.usageTime };
        }
        }
        
        args.putIntArray(PowerUsageDetail.EXTRA_DETAIL_TYPES, types);
        args.putDoubleArray(PowerUsageDetail.EXTRA_DETAIL_VALUES, values);
        // 20131113 yonguk.kim Apply UsageManager for ATT
        if (getActivity() instanceof PreferenceActivity) {
            PreferenceActivity pa = (PreferenceActivity)getActivity();
            pa.startPreferencePanel(PowerUsageDetail.class.getName(), args,
                    R.string.details_title, null, null, 0);
        } else {
            Intent intent = Utils.buildStartFragmentIntent(PowerUsageDetail.class.getName(), args,
                    R.string.details_title, 0, getActivity(), R.drawable.shortcut_battery);
            startActivity(intent);
        }
    }

}
