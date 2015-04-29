/*
 * Copyright (C) 2011 The Android Open Source Project
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


import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings.Global;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.settings.AppViewHolder;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;
import com.android.settings.drawable.InsetBoundsDrawable;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.net.ChartData;
import com.android.settings.net.ChartDataLoader;
import com.android.settings.net.DataUsageMeteredSettings;
import com.android.settings.net.NetworkPolicyEditor;
import com.android.settings.net.SummaryForAllUidLoader;
import com.android.settings.net.UidDetail;
import com.android.settings.net.UidDetailProvider;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.ChartDataUsageView.DataUsageChartListener;
import com.android.settings.widget.ChartDataUsageView;
import com.android.settings.widget.PieChartView;
import com.google.android.collect.Lists;
import com.lge.constants.SettingsConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import libcore.util.Objects;

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static android.net.NetworkPolicy.LIMIT_DISABLED;
import static android.net.NetworkPolicy.WARNING_DISABLED;
import static android.net.NetworkPolicyManager.EXTRA_NETWORK_TEMPLATE;
import static android.net.NetworkPolicyManager.POLICY_NONE;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkPolicyManager.computeLastCycleBoundary;
import static android.net.NetworkPolicyManager.computeNextCycleBoundary;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;
import static android.net.NetworkTemplate.MATCH_MOBILE_3G_LOWER;
import static android.net.NetworkTemplate.MATCH_MOBILE_4G;
import static android.net.NetworkTemplate.MATCH_MOBILE_ALL;
import static android.net.NetworkTemplate.MATCH_WIFI;
import static android.net.NetworkTemplate.buildTemplateEthernet;
import static android.net.NetworkTemplate.buildTemplateMobile3gLower;
import static android.net.NetworkTemplate.buildTemplateMobile4g;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;
import static android.net.TrafficStats.GB_IN_BYTES;
import static android.net.TrafficStats.MB_IN_BYTES;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.Time.TIMEZONE_UTC;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.android.internal.util.Preconditions.checkNotNull;
import static com.android.settings.Utils.prepareCustomPreferencesList;

import android.graphics.Typeface;

/**
 * Panel showing data usage history across various networks, including options
 * to inspect based on usage cycle and control through {@link NetworkPolicy}.
 */
public class DataUsageSummary extends Fragment implements Indexable {
    private static final String TAG = "DataUsageSummary";

    // TODO: remove this testing code
    private static final boolean TEST_ANIM = false;
    private static final boolean TEST_RADIOS = false;

    private static final String TEST_RADIOS_PROP = "test.radios";
    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";

    private static final String TAB_3G = "3g";
    private static final String TAB_4G = "4g";
    private static final String TAB_MOBILE = "mobile";
    private static final String TAB_WIFI = "wifi";
    private static final String TAB_ETHERNET = "ethernet";

    private static final String TAG_CONFIRM_DATA_DISABLE = "confirmDataDisable";
    private static final String TAG_CONFIRM_DATA_ENABLE = "confirmDataEnable";
    private static final String TAG_CONFIRM_LIMIT = "confirmLimit";
    private static final String TAG_CYCLE_EDITOR = "cycleEditor";
    private static final String TAG_WARNING_EDITOR = "warningEditor";
    private static final String TAG_LIMIT_EDITOR = "limitEditor";
    private static final String TAG_CONFIRM_RESTRICT = "confirmRestrict";
    private static final String TAG_DENIED_RESTRICT = "deniedRestrict";
    private static final String TAG_CONFIRM_APP_RESTRICT = "confirmAppRestrict";
    private static final String TAG_APP_DETAILS = "appDetails";

    private static final String TAG_CONFIRM_RESTRICT_LGU_ENABLE = "confirmRestrictlguenable";
    private static final String TAG_CONFIRM_RESTRICT_LGU_DISABLE = "confirmRestrictlgudisable";
    private static final String TAG_CONFIRM_TRAFFIC_STATE_ENABLE = "confirmTrafficStateEnable";
    private static final String TAG_CONFIRM_TRAFFIC_STATE_DISABLE = "confirmTrafficStateDisable";

    private static final String TAG_CONFIRM_DATA_ROAMING = "confirmDataRoaming";

    private static String sSktUserBackgSettingIntent =
            "com.lge.skt.intent.action.USER_BACKG_SETTING";
    private static String sSktUserBackgSettingIntentOld =
            "android.skt.intent.action.USER_BACKG_SETTING";
    private static final int LOADER_CHART_DATA = 2;
    private static final int LOADER_SUMMARY = 3;

    private INetworkManagementService mNetworkService;
    private INetworkStatsService mStatsService;
    private NetworkPolicyManager mPolicyManager;
    private TelephonyManager mTelephonyManager;

    private INetworkStatsSession mStatsSession;

    private static final String PREF_FILE = "data_usage";
    private static final String PREF_SHOW_WIFI = "show_wifi";
    private static final String PREF_SHOW_ETHERNET = "show_ethernet";
    private static final String PREF_DEFAULT_UNIT = "default_unit";

    private static final String TRAFFIC_SERVICE_STATE = "bg_traffic_service_state";
    private static final String TRAFFIC_SERVICE_STATE_SUB = "bg_auto_optimization_setup";
    private static final int TRAFFIC_STATUS_DISABLING = 0;
    private static final int TRAFFIC_STATUS_DISABLED = 1;
    private static final int TRAFFIC_STATUS_ENABLING = 2;
    private static final int TRAFFIC_STATUS_ENABLED = 3;
    private static final String ACTION_TRAFFIC_SERVICE_STATE_CHANGED = "com.skt.apra.action.SERVICE_STATE_CHANGED";
    private static final String ACTION_TRAFFIC_SERVICE_STATE_CHANGING = "com.skt.apra.action.SERVICE_STATE_CHANGING";

    private SharedPreferences mPrefs;

    private TabHost mTabHost;
    private ViewGroup mTabsContainer;
    private TabWidget mTabWidget;
    private ListView mListView;
    private DataUsageAdapter mAdapter;

    /** Distance to inset content from sides, when needed. */
    private int mInsetSide = 0;

    private ViewGroup mHeader;
    private ViewGroup mNetworkSwitchesContainer;
    private LinearLayout mNetworkSwitches;
    private Switch mDataEnabled;
    private View mDataEnabledView;
    private static CheckBox sDisableAtLimit;
    private View mDisableAtLimitView;

    private static CheckBox sAlertAtWarning;
    private View mAlertAtWarningView;
    private static CheckBox sTrafiicOptimizeCheckBox;
    private TextView mTrafficOptimize = null;
    private View mTrafficOptimizeView = null;
    private Switch mDataSleepEnabled;
    private View mDataSleepEnabledView;

    private View mCycleView;
    private Spinner mCycleSpinner;
    private CycleAdapter mCycleAdapter;
    private TextView mCycleSummary;

    private ChartDataUsageView mChart;
    private View mDisclaimer;
    private TextView mEmpty;
    private View mStupidPadding;

    private View mAppDetail;
    private ImageView mAppIcon;
    private ViewGroup mAppTitles;
    private TextView mAppTotal;
    private TextView mAppForeground;
    private TextView mAppBackground;
    private Button mAppSettings;

    private LinearLayout mAppSwitches;
    private CheckBox mAppRestrict;
    private View mAppRestrictView;

    private boolean mShowWifi = false;
    private boolean mShowEthernet = false;
    private static boolean sDisplayUnit = true;
    SettingsBreadCrumb mBreadCrumb;
    private DataSleepInfo mDataSleepInfo;

    private NetworkTemplate mTemplate;
    private NetworkTemplate mTemplate_mobile;
    private ChartData mChartData;

    private AppItem mCurrentApp = null;

    private Intent mAppSettingsIntent;

    private NetworkPolicyEditor mPolicyEditor;

    private String mCurrentTab = null;
    private String mIntentTab = null;

    private MenuItem mMenuRestrictBackground;
    private MenuItem mMenuShowWifi;
    private MenuItem mMenuShowEthernet;
    private MenuItem mMenuSimCards;
    private MenuItem mMenuCellularNetworks;
    private MenuItem mMenuDataRoaming;

    /** Flag used to ignore listeners during binding. */
    private boolean mBinding;
    private boolean mIsChartDataBinding;

    private UidDetailProvider mUidDetailProvider;

    private static boolean mPopupdisable = false;
    protected static Context sContext; //DataSwitcher pop-up for TLF_ES

    private static int mTitleStyle = 0;
    private static ContentQueryMap mContentQueryMap;
    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
    //private static boolean mDataEnabledChecked = false;
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
    private static Observer mSettingsObserver;
    Cursor settingsCursor;

    private static final boolean mIs_Ctc_Cn = "CTC".equals(Config.getOperator()) ||  "CTO".equals(Config.getOperator());

    //private int mDoNotShowDataOffPopupChecked = 0;
    //private static final int OPTION_UNIT_ITEM = 0x4885;
    // LGMDM [a1-mdm-dev@lge.com] [ID-MDM-167][ID-MDM-198]
    private TextView mMDMSummaryView;
    // LGMDM_END

    // td155964: fixed the IllegalStateException by calling the dialog fragment after backg.
    // check the datausage fragement state.
    private static boolean sIsForg = false;

    static boolean aboidBlink = false;
    static Timer sTimer = new Timer();
    static TimerTask task = new TimerTask() {
        public void run() {
            aboidBlink = false;
        }
    };

    //private AlertDialog mDialog = null;
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.Settings$DataUsageSummaryMultiSIMActivity"));
            startActivity(intent);
            getActivity().finish();
        }
        final Context context = getActivity();

        if (Utils.supportSplitView(context)) {
            Log.d("YSY", "DataUsageSummary, Utils.supportSplitView(this)");
            SLog.i(" skip data usage title");
            getActivity().getActionBar().setTitle(R.string.settings_label);
        } else if (Utils.isWifiOnly(context)) {
            // A-WIFI setting-data usage update fix
            Log.d("YSY", "DataUsageSummary, Utils.isWifiOnly(context)");
            getActivity().getActionBar().setTitle(
                    R.string.data_usage_summary_title);
        } else {
            Log.d("YSY", "DataUsageSummary, else");
            getActivity().setTitle(R.string.data_usage_enable_mobile);

            if ("SKT".equals(Config.getOperator())
                    || "KT".equals(Config.getOperator())) {
                Log.d("YSY", "DataUsageSummary, SKT || KT");
                getActivity().setTitle(R.string.data_network_settings_title);
            } else if ("ATT".equals(Config.getOperator())) {
                Log.d("YSY", "DataUsageSummary, ATT");
                getActivity().setTitle(R.string.shortcut_datausage_att);
            } else if ("VZW".equals(Config.getOperator())) {
                getActivity().setTitle(R.string.data_usage_summary_title);
            }
        }

        sContext = context;
        mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        mStatsService = INetworkStatsService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mPolicyManager = NetworkPolicyManager.from(context);
        mTelephonyManager = TelephonyManager.from(context);

        mPrefs = getActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
        mPolicyEditor.read();

        try {
            if (!mNetworkService.isBandwidthControlEnabled()) {
                Log.w(TAG, "No bandwidth control; leaving");
                getActivity().finish();
            }
        } catch (RemoteException e) {
            Log.w(TAG, "No bandwidth control; leaving");
            getActivity().finish();
        }

        mShowWifi = mPrefs.getBoolean(PREF_SHOW_WIFI, false);
        mShowEthernet = mPrefs.getBoolean(PREF_SHOW_ETHERNET, false);
        sDisplayUnit = mPrefs.getBoolean(PREF_DEFAULT_UNIT, true);
        if (false == "VZW".equals(Config.getOperator())) {
            sDisplayUnit = false;
        }
        // override preferences when no mobile radio
        if (!hasReadyMobileRadio(context)) {
            mShowWifi = hasWifiRadio(context);
            mShowEthernet = hasEthernet(context);
        }

        setHasOptionsMenu(true);

        // kerry - to sync with quick settings start
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        // LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
        filter.addAction(sSktUserBackgSettingIntent);
        filter.addAction(sSktUserBackgSettingIntentOld);
        // LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

        //If in callings, DataEnabled preperence is dimmed
        if ("DCM".equals(Config.getOperator()) || "VZW".equals(Config.getOperator())) {
            SLog.i("register the ACTION_PHONE_STATE_CHANGED for DCM");
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        }

        if (isSupportTrafficOptimezeCheckBox()) {
            filter.addAction(ACTION_TRAFFIC_SERVICE_STATE_CHANGED);
        }
        getActivity().registerReceiver(mReceiver, filter);
        // kerry - to sync with quick settings end
        settingsCursor = getActivity().getContentResolver().query(Global.CONTENT_URI, null,
                "(" + android.provider.Settings.System.NAME + "=?)",
                new String[] { Global.MOBILE_DATA }, null);
        if (null != settingsCursor) {
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    android.provider.Settings.System.NAME, true, null);
        }
        mSettingsObserver = new Observer() {
            public void update(Observable o, Object arg) {

                if (null == getActivity() || true == aboidBlink) {
                    return;
                }
                boolean enabled = mTelephonyManager.getDataEnabled();
                mMobileDataEnabled = enabled;

                updatePolicy(false);

                setConfirmDialogShowed(false);
            }
        };
        if (null != mContentQueryMap) {
            mContentQueryMap.addObserver(mSettingsObserver);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addDataUsageSettingPolicyChangeIntentFilter(
                    filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        mIsFirst = true;
    }

    //DataSwitcher pop-up for TLF_ES START
    private boolean isRememberOptionChecked() {
        return (android.provider.Settings.System.getInt(
                sContext.getContentResolver(),
                SettingsConstants.System.DATA_CONNECTIVITY_POPUP_REMEMBER, 0) == 1);
    }
    //DataSwitcher pop-up for TLF_ES END

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final Context context = inflater.getContext();
        final View view = inflater.inflate(R.layout.data_usage_summary, container, false);

        mTitleStyle = getResources().getInteger(R.integer.config_font_name);

        if (true == "KR".equals(Config.getCountry())) {
            Activity activity = getActivity();
            mDataEnabled = new Switch(activity);
            setPaddingSwitch(activity,
                    mDataEnabled,
                    activity.getResources().getConfiguration().orientation);
        }

        mUidDetailProvider = new UidDetailProvider(context);
        try {
            mStatsSession = mStatsService.openSession();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
        mTabsContainer = (ViewGroup)view.findViewById(R.id.tabs_container);
        mTabWidget = (TabWidget)view.findViewById(android.R.id.tabs);
        mListView = (ListView)view.findViewById(android.R.id.list);

        // decide if we need to manually inset our content, or if we should rely
        // on parent container for inset.
        //final boolean shouldInset = mListView.getScrollBarStyle()
        //        == View.SCROLLBARS_OUTSIDE_OVERLAY;
        mInsetSide = 0;

        // adjust padding around tabwidget as needed
        //if (null != mListView) {
        //    prepareCustomPreferencesList(container, view, mListView, false);
        //    prepareCustomPreferencesList(container, view, mListView, true); kerry - remove for margin guide
        //} // 120103 WBT

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(mTabListener);

        mHeader = (ViewGroup)inflater.inflate(R.layout.data_usage_header, mListView, false);
        mHeader.setClickable(true);

        mListView.addHeaderView(mHeader, null, true);
        mListView.setItemsCanFocus(true);

        if (mInsetSide > 0) {
            // inset selector and divider drawables
            insetListViewDrawables(mListView, mInsetSide);
            mHeader.setPaddingRelative(mInsetSide, 0, mInsetSide, 0);
        }

        {
            // bind network switches
            mNetworkSwitchesContainer = (ViewGroup)mHeader.findViewById(
                    R.id.network_switches_container);
            mNetworkSwitches = (LinearLayout)mHeader.findViewById(R.id.network_switches);

            if (false == "KR".equals(Config.getCountry())) {
                mDataEnabled = new Switch(inflater.getContext());
                mDataEnabledView = inflatePreference(inflater, mNetworkSwitches, mDataEnabled);
                mDataEnabledView.setOnClickListener(mDataEnabledViewListener);
                mDataEnabledView.setFocusable(false);
                mDataEnabledView.setClickable(true);
                mNetworkSwitches.addView(mDataEnabledView);
            }

            mDataEnabled.setOnCheckedChangeListener(mDataEnabledListener);
            mDataEnabled.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // [SWITCH_SOUND]
                }
            });
            sDisableAtLimit = new CheckBox(inflater.getContext());
            sDisableAtLimit.setClickable(false);
            sDisableAtLimit.setFocusable(false);
            mDisableAtLimitView = inflatePreference(inflater, mNetworkSwitches, sDisableAtLimit);
            mDisableAtLimitView.setClickable(true);
            mDisableAtLimitView.setFocusable(true);
            mDisableAtLimitView.setOnClickListener(mDisableAtLimitListener);
            mNetworkSwitches.addView(mDisableAtLimitView);

            sAlertAtWarning = new CheckBox(inflater.getContext());
            sAlertAtWarning.setClickable(false);
            sAlertAtWarning.setFocusable(false);
            mAlertAtWarningView = inflatePreference(inflater, mNetworkSwitches, sAlertAtWarning);
            mAlertAtWarningView.setClickable(true);
            mAlertAtWarningView.setFocusable(true);
            mAlertAtWarningView.setOnClickListener(mAlertAtWarningListener);
            mNetworkSwitches.addView(mAlertAtWarningView);

            if (true == "KR".equals(Config.getCountry())
                    || true == "VZW".equals(Config.getOperator())) {
                initTrafficOptimizeViewSKTOnlay(inflater);
            } else {
                mAlertAtWarningView.setVisibility(View.GONE);
            }
            Log.i(TAG, "onCreateView isAirplaneModeOn() = " + isAirplaneModeOn(inflater.getContext()));
            Log.i(TAG, "onCreateView isDataRoaming() = " + Config.isDataRoaming());

            mDataSleepInfo = new DataSleepInfo(context);
            mDataSleepEnabled = new Switch(inflater.getContext());
            mDataSleepEnabled.setOnCheckedChangeListener(mDataSleepEnabledListener);
            mDataSleepEnabled.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // [SWITCH_SOUND]
                }
            });
            mDataSleepEnabledView = inflatePreference_data_sleep_switch(inflater, mNetworkSwitches,
                    mDataSleepEnabled);
            mDataSleepEnabledView.setOnClickListener(mDataSleepEnabledViewListener);
            setPreferenceSummary(mDataSleepEnabledView, getDataSleepSummaryString());
            mDataSleepEnabledView.setFocusable(false);
            mDataSleepEnabledView.setClickable(true);
            mNetworkSwitches.addView(mDataSleepEnabledView);
            if (false == "VZW".equals(Config.getOperator())) {
                mDataSleepEnabledView.setVisibility(View.GONE);
            }

            if (Config.SKT.equals(Config.getOperator())
                    || Config.LGU.equals(Config.getOperator())
                    || Config.VZW.equals(Config.getOperator())
                    || Config.KT.equals(Config.getOperator())) {
                if (isAirplaneModeOn(inflater.getContext()) == true
                        || (Config.isDataRoaming() == true
                        && (Config.SKT.equals(Config.getOperator())
                        || Config.KT.equals(Config.getOperator())))) {
                    mDataEnabled.setVisibility(View.VISIBLE);
                    mDataEnabled.setEnabled(false);
                    mDataSleepEnabled.setEnabled(false);
                    sDisableAtLimit.setEnabled(false);
                    sAlertAtWarning.setEnabled(false);
                    mDisableAtLimitView.setEnabled(false);
                    mAlertAtWarningView.setEnabled(false);
                    if (null != mTrafficOptimizeView) {
                        mTrafficOptimizeView.setEnabled(false);
                    }
                    if (sTrafiicOptimizeCheckBox != null) {
                        sTrafiicOptimizeCheckBox.setEnabled(false);
                    }

                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.GRAY);
                    TextView lvw = (TextView)mAlertAtWarningView.findViewById(android.R.id.title);
                    lvw.setTextColor(Color.GRAY);
                    TextView mTitle = (TextView)mDataSleepEnabledView
                            .findViewById(android.R.id.title);
                    mTitle.setTextColor(Color.GRAY);
                    mNetworkSwitches.setEnabled(false);
                }
            } else {
                if (isAirplaneModeOn(inflater.getContext()) == true) {
                    if (true == "KR".equals(Config.getCountry())) {
                        mDataEnabled.setVisibility(View.VISIBLE);
                        mDataEnabled.setEnabled(false);
                        sAlertAtWarning.setEnabled(false);
                        mAlertAtWarningView.setEnabled(false);
                        TextView lvw = (TextView)mAlertAtWarningView
                                .findViewById(android.R.id.title);
                        lvw.setTextColor(Color.GRAY);
                    } else {
                        mDataEnabledView.setVisibility(View.VISIBLE);
                        mDataEnabled.setEnabled(false);
                        mDataEnabledView.setEnabled(false);
                        TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                        ev.setTextColor(Color.GRAY);
                    }
                    sDisableAtLimit.setEnabled(false);
                    mDisableAtLimitView.setEnabled(false);
                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.GRAY);
                    mNetworkSwitches.setEnabled(false);
                }
            }
        }

        // bind cycle dropdown
        mCycleView = inflater.inflate(R.layout.data_usage_cycles, mNetworkSwitches, false);
        mCycleSpinner = (Spinner)mCycleView.findViewById(R.id.cycles_spinner);
        mCycleAdapter = new CycleAdapter(context);
        mCycleSpinner.setAdapter(mCycleAdapter);
        mCycleSpinner.setOnItemSelectedListener(mCycleListener);
        mCycleSummary = (TextView)mCycleView.findViewById(R.id.cycle_summary);
        mNetworkSwitches.addView(mCycleView);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-342][ID-MDM-343]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            mCycleSpinner.setOnTouchListener(mCycleOnTouch);
        }
        // LGMDM_END
        mChart = (ChartDataUsageView)mHeader.findViewById(R.id.chart);
        mChart.setListener(mChartListener);
        mChart.bindNetworkPolicy(null);

        mChart.setDisplayUnit(sDisplayUnit);

        {
            // bind app detail controls
            mAppDetail = mHeader.findViewById(R.id.app_detail);
            mAppIcon = (ImageView)mAppDetail.findViewById(R.id.app_icon);
            mAppTitles = (ViewGroup)mAppDetail.findViewById(R.id.app_titles);
            mAppForeground = (TextView)mAppDetail.findViewById(R.id.app_foreground);
            mAppBackground = (TextView)mAppDetail.findViewById(R.id.app_background);
            mAppSwitches = (LinearLayout)mAppDetail.findViewById(R.id.app_switches);

            mAppSettings = (Button)mAppDetail.findViewById(R.id.app_settings);
            mAppSettings.setOnClickListener(mAppSettingsListener);

            mAppRestrict = new CheckBox(inflater.getContext());
            mAppRestrict.setClickable(false);
            mAppRestrict.setFocusable(false);
            mAppRestrictView = inflatePreference(inflater, mAppSwitches, mAppRestrict);
            mAppRestrictView.setClickable(true);
            mAppRestrictView.setFocusable(true);
            mAppRestrictView.setOnClickListener(mAppRestrictListener);
            mAppSwitches.addView(mAppRestrictView);
        }

        mDisclaimer = mHeader.findViewById(R.id.disclaimer);

        mEmpty = (TextView)mHeader.findViewById(android.R.id.empty);
        mStupidPadding = mHeader.findViewById(R.id.stupid_padding);

        final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        mAdapter = new DataUsageAdapter(um, mUidDetailProvider, mInsetSide);
        mListView.setOnItemClickListener(mListListener);
        mListView.setAdapter(mAdapter);

        //[s][chris.won@lge.com][2013-05-13] In case of Chart Data arrives lately.
        mAppDetail.setVisibility(View.GONE);
        mCycleAdapter.setChangeVisible(false);
        mChart.bindDetailNetworkStats(null);
        //[e][chris.won@lge.com][2013-05-13] In case of Chart Data arrives lately.

        // LGMDM [a1-mdm-dev@lge.com] [ID-MDM-167][ID-MDM-198]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.android.settings.Utils.supportSplitView(getActivity())) {
            if (Utils.isWifiOnly(context) == false) {
                mMDMSummaryView = (TextView)mTabHost.findViewById(R.id.mdm_summary);
            }
        }
        // LGMDM_END

        SLog.i("onCreateView");
        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // pick default tab based on incoming intent
        final Intent intent = getActivity().getIntent();
        mIntentTab = computeTabFromIntent(intent);

        // this kicks off chain reaction which creates tabs, binds the body to
        // selected network, and binds chart, cycles and detail list.
        updateTabs();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mBreadCrumb != null) {
            mBreadCrumb.removeSwitch();
        }
        // check the datausage fragement state.
        sIsForg = false;
        SLog.i("onPause");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!"VZW".equals(Config.getOperator())) {
            addSwitchTablet();
        }

        //If in callings, DataEnabled preperence is dimmed
        if ("DCM".equals(Config.getOperator())
                && false == isAirplaneModeOn(getActivity())) {
            if ((mTelephonyManager != null) && (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE)) {
                mDataEnabled.setEnabled(false);
                mDataEnabledView.setEnabled(false);
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.GRAY);
                setMobileDataEnabled(isMobileDataEnabled());
                updatePolicy(false);
            } else {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.BLACK);
            }
        }

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null && !"VZW".equals(Config.getOperator())) {
                mBreadCrumb.addSwitch(mDataEnabled);
            }
        }

        //DataSwitcher pop-up for TLF_ES START
        /* [S][2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
        if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(sContext)) {
            setMobileDataEnabled(isMobileDataEnabled());
        }
        //DataSwitcher pop-up for TLF_ES END

        SLog.i("onResume :: ");

        //If in callings, DataEnabled preperence is dimmed
        if ("VZW".equals(Config.getOperator())
                && false == isAirplaneModeOn(getActivity())) {
            if (Utils.isDimmingMobileDataForVolte(getActivity())) {
                mDataEnabled.setEnabled(false);
            }
        }

        if ("LGU".equals(Config.getOperator()) && Utils.isInVideoCall()) {
            mDataEnabled.setEnabled(false);
        }

        // kick off background task to update stats
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // wait a few seconds before kicking off
                    Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
                    mStatsService.forceUpdate();
                } catch (InterruptedException e) {
                    Log.w(TAG, "InterruptedException");
                } catch (RemoteException e) {
                    Log.w(TAG, "InterruptedException");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                SLog.i("onResume :: sIsForg " + sIsForg);
                // check the datausage fragement state.
                if (isAdded() && sIsForg) {
                    SLog.i("onResume :: onPostExecute updateBody");
                    updateBody();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // check the datausage fragement state.
        sIsForg = true;
        // LGMDM [a1-mdm-dev@lge.com] [ID-MDM-167][ID-MDM-198]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.android.settings.Utils.supportSplitView(getActivity())) {
            if (hasReadyMobileRadio(getActivity())) {
                boolean isWIFITab = false;
                if (TAB_WIFI.equals(mTabHost.getCurrentTabTag())) {
                    isWIFITab = true;
                }
                com.android.settings.MDMSettingsAdapter.getInstance().setDataUsageSummarySplitUI(
                        mMDMSummaryView, getActivity(), isWIFITab);
            }
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_usage, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final Context context = getActivity();
        final boolean appDetailMode = isAppDetailMode();
        final boolean isOwner = ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;

        mMenuDataRoaming = menu.findItem(R.id.data_usage_menu_roaming);
        if ("TRF".equals(Config.getOperator())) {
            mMenuDataRoaming.setVisible(hasReadyMobileRadio(context) && !appDetailMode);
            mMenuDataRoaming.setChecked(getDataRoaming());
        } else {
            mMenuDataRoaming.setVisible(false);
        }

        final MenuItem sDisplayUnitMB = menu.findItem(R.id.display_unit_mb);
        final MenuItem sDisplayUnitGB = menu.findItem(R.id.display_unit_gb);
        if (false == "VZW".equals(Config.getOperator())) {
            sDisplayUnit = false;
            sDisplayUnitGB.setVisible(false);
            sDisplayUnitMB.setVisible(false);
        } else if (sDisplayUnit) {
            sDisplayUnitGB.setVisible(false);
            sDisplayUnitMB.setVisible(true);
        } else {
            sDisplayUnitMB.setVisible(false);
            sDisplayUnitGB.setVisible(true);
        }

        mMenuShowWifi = menu.findItem(R.id.data_usage_menu_show_wifi);
        if (hasWifiRadio(context) && hasReadyMobileRadio(context)) {
            mMenuShowWifi.setVisible(!appDetailMode);
            mMenuShowWifi.setChecked(mShowWifi);
        } else {
            mMenuShowWifi.setVisible(false);
        }

        mMenuShowEthernet = menu.findItem(R.id.data_usage_menu_show_ethernet);
        if (hasEthernet(context) && hasReadyMobileRadio(context)) {
            mMenuShowEthernet.setVisible(!appDetailMode);
            mMenuShowEthernet.setChecked(mShowEthernet);
        } else {
            mMenuShowEthernet.setVisible(false);
        }

        mMenuRestrictBackground = menu.findItem(R.id.data_usage_menu_restrict_background);
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);
        if ("LGU".equals(Config.getOperator()) && Config.isDataRoaming()) {
            mMenuRestrictBackground.setVisible(false);
        } else if ("VZW".equals(Config.getOperator()) && Utils.isTablet() && pco_internet == 3) {
            mMenuRestrictBackground.setVisible(false);
        } else {
            mMenuRestrictBackground.setVisible(hasReadyMobileRadio(context) && !appDetailMode
                    && isOwner);
            mMenuRestrictBackground.setChecked(mPolicyManager.getRestrictBackground());
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .checkEnforceBackgroundDataRestrictedPolicy(mMenuRestrictBackground, null);
        }
        // LGMDM_END
        //[E][chris.won@lge.com][2013-03-29] Apply LGU roaming scenario

        final MenuItem metered = menu.findItem(R.id.data_usage_menu_metered);
        if ((hasReadyMobileRadio(context) || hasWifiRadio(context))
                && (false == "TRF".equals(Config.getOperator())
                && false == "AIO".equals(Config.getOperator2())
                && false == "CRK".equals(Config.getOperator2()))) {
            // It should be not displayed the mobile hotspot menu as TRF/AIO requirement.
            metered.setVisible(!appDetailMode);
        } else {
            metered.setVisible(false);
        }

        // TODO: show when multiple sims available
        mMenuSimCards = menu.findItem(R.id.data_usage_menu_sim_cards);
        mMenuSimCards.setVisible(false);

        mMenuCellularNetworks = menu.findItem(R.id.data_usage_menu_cellular_networks);
        if (ActivityManager.getCurrentUser() != UserHandle.USER_OWNER) {
            mMenuCellularNetworks.setVisible(false);
        } else if (isAirplaneModeOn(context)) {
            mMenuCellularNetworks.setVisible(false);
        } else if (hasReadyMobileRadio(context)) {
            mMenuCellularNetworks.setVisible(!appDetailMode);
        } else {
            mMenuCellularNetworks.setVisible(false);
        }

        final MenuItem help = menu.findItem(R.id.data_usage_menu_help);
        String helpUrl;
        if (false == "VZW".equals(Config.getOperator())) {
            help.setVisible(false);
        } else if (!TextUtils.isEmpty(helpUrl = getResources().getString(
                R.string.help_url_data_usage))) {
            // applied the help app.
            if ("true".equals(helpUrl)) {
                // applied the help app.
                final String ON_OVERFLOW_VALUE = "help_settings_on_overflowmenu";
                int mIsApply = Settings.System.getInt(
                        getActivity().getContentResolver(),
                        ON_OVERFLOW_VALUE, 0);
                SLog.e("mIsApply = " + mIsApply);
                if (mIsApply != 0) {
                    Intent helpIntent = new Intent();
                    helpIntent.setAction("com.lge.help.intent.action.SEARCH_FROM_APPS");
                    helpIntent.putExtra("app-name", "Data");
                    help.setIntent(helpIntent);
                } else {
                    help.setVisible(false);
                }
            } else {
                Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
                helpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                help.setIntent(helpIntent);
                help.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        } else {
            help.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.data_usage_menu_roaming: {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-164]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && (com.android.settings.MDMSettingsAdapter.getInstance()
                            .isShowDataUsageRoamingToastIfNeed(getActivity()))) {
                return true;
            }
            // LGMDM_END
            final boolean dataRoaming = !item.isChecked();
            if (dataRoaming) {
                ConfirmDataRoamingFragment.show(this);
            } else {
                // no confirmation to disable roaming
                setDataRoaming(false);
                //myeonghwan.kim@lge.com Add toast popup [S]
                Toast.makeText(getActivity(), R.string.dataroaming_off_result,
                        Toast.LENGTH_SHORT).show();
                //myeonghwan.kim@lge.com Add toast popup [E]
            }
            return true;
        }
        case R.id.data_usage_menu_restrict_background: {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && (com.android.settings.MDMSettingsAdapter.getInstance()
                            .isShowEnforceBackgroundDataRestrictedToastIfNeed(getActivity()))) {
                return true;
            }
            // LGMDM_END

            final boolean restrictBackground = !item.isChecked();
            if (restrictBackground) {
                if ("LGU".equals(Config.getOperator())) {
                    if (Config.isDataRoaming()) {
                        ConfirmLGURestrictEnableFragment.show(this);
                    }
                    else {
                        ConfirmRestrictFragment.show(this);
                    }
                }
                else {
                    ConfirmRestrictFragment.show(this);
                }
            } else {
                if ("LGU".equals(Config.getOperator())) {
                    if (Config.isDataRoaming()) {
                        ConfirmLGURestrictDisableFragment.show(this);
                    }
                    else {
                        setRestrictBackground(false);
                    }
                }
                else {
                    setRestrictBackground(false);
                }
            }
            return true;
        }
        case R.id.data_usage_menu_show_wifi: {
            mShowWifi = !item.isChecked();
            mPrefs.edit().putBoolean(PREF_SHOW_WIFI, mShowWifi).apply();
            item.setChecked(mShowWifi);
            updateTabs();
            return true;
        }
        case R.id.data_usage_menu_show_ethernet: {
            mShowEthernet = !item.isChecked();
            mPrefs.edit().putBoolean(PREF_SHOW_ETHERNET, mShowEthernet).apply();
            item.setChecked(mShowEthernet);
            updateTabs();
            return true;
        }
            case R.id.data_usage_menu_sim_cards: {
                // TODO: hook up to sim cards
                return true;
            }
            case R.id.data_usage_menu_cellular_networks: {
                final Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.phone",
                        "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
            }
        case R.id.data_usage_menu_metered: {
            // Apply UsageManager for ATT
            if (getActivity() instanceof PreferenceActivity) {
                final PreferenceActivity activity = (PreferenceActivity)getActivity();
                activity.startPreferencePanel(
                        DataUsageMeteredSettings.class.getCanonicalName(),
                        null,
                        R.string.data_usage_metered_title,
                        null, this, 0);
                return true;
            } else {
                Intent intent = Utils.buildStartFragmentIntent(
                        DataUsageMeteredSettings.class.getName(),
                        null,
                        R.string.data_usage_metered_title, 0,
                        getActivity(),
                        R.drawable.shortcut_data_usage);
                startActivity(intent);
                return true;
            }
        }
        case R.id.display_unit_gb: {
            SLog.e("DISPLAY_UNIT_GB on");
            sDisplayUnit = true;
            mChart.setDisplayUnit(sDisplayUnit);
            mPrefs.edit().putBoolean(PREF_DEFAULT_UNIT, true).apply();
            updateTabs();
            return true;
        }
        case R.id.display_unit_mb: {
            SLog.e("DISPLAY_UNIT_MB on");
            sDisplayUnit = false;
            mChart.setDisplayUnit(sDisplayUnit);
            mPrefs.edit().putBoolean(PREF_DEFAULT_UNIT, false).apply();
            updateTabs();
            return true;
        }
        default:
            break;
        }
        return false;
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link android.provider.Settings.Global#DATA_ROAMING}.
     */
    public static class ConfirmDataRoamingFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmDataRoamingFragment dialog = new ConfirmDataRoamingFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_ROAMING);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            //myeonghwan.kim@lge.com 2013.01.29 Change popup string for LGT [S]
            String strOk;
            String strCancel;

            builder.setTitle(R.string.roaming_reenable_title);
            strOk = getString(android.R.string.ok);
            strCancel = getString(android.R.string.cancel);
            if (Utils.hasMultipleUsers(context)) {
                builder.setMessage(R.string.roaming_warning_multiuser);
            } else {
                if (Utils.isUI_4_1_model(getActivity())) {
                    builder.setTitle(R.string.sp_dlg_note_NORMAL);
                    builder.setMessage(R.string.sp_data_roaming_warning_msg_ux4);
                    strOk = getString(R.string.yes);
                    strCancel = getString(R.string.no);
                } else {
                    builder.setMessage(R.string.roamingquery_for_lgt_disable);
                    builder.setIconAttribute(android.R.attr.alertDialogIcon);
                }
            }
            builder.setPositiveButton(strOk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                    if (target != null) {
                        target.setDataRoaming(true);
                        //myeonghwan.kim@lge.com Add toast popup [S]
                        Toast.makeText(context, R.string.dataroaming_on_result,
                                Toast.LENGTH_SHORT).show();
                        //myeonghwan.kim@lge.com Add toast popup [E]
                    }
                }
            });
            builder.setNegativeButton(strCancel, null);

            return builder.create();
        }
    }

    @Override
    public void onDestroy() {
        if (null != mContentQueryMap) {
            mContentQueryMap.deleteObserver(mSettingsObserver);
        }
        if (null != settingsCursor) {
            settingsCursor.close();
        }
        if (null != mDataSleepEnabledView) {
            mDataSleepEnabledView = null;
        }
        if (null != mDataEnabledView) {
            mDataEnabledView = null;
        }
        if (null != mDisableAtLimitView) {
            mDisableAtLimitView = null;
        }
        if (null != mAlertAtWarningView) {
            mAlertAtWarningView = null;
        }
        if (null != mTrafficOptimizeView) {
            mTrafficOptimizeView = null;
        }

        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;

        TrafficStats.closeQuietly(mStatsSession);
        getActivity().unregisterReceiver(mReceiver); // kerry

        removeAppDetails();

        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    private void removeAppDetails() {
        if (this.isRemoving()) {
            getFragmentManager()
               .popBackStack(TAG_APP_DETAILS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Build and assign {@link LayoutTransition} to various containers. Should
     * only be assigned after initial layout is complete.
     */
    private void ensureLayoutTransitions() {
        // skip when already setup
        if (mChart.getLayoutTransition() != null) {
            return;
        }

        mTabsContainer.setLayoutTransition(buildLayoutTransition());
        mHeader.setLayoutTransition(buildLayoutTransition());
        mNetworkSwitchesContainer.setLayoutTransition(buildLayoutTransition());

        final LayoutTransition chartTransition = buildLayoutTransition();
        chartTransition.disableTransitionType(LayoutTransition.APPEARING);
        chartTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        mChart.setLayoutTransition(chartTransition);
    }

    private static LayoutTransition buildLayoutTransition() {
        final LayoutTransition transition = new LayoutTransition();
        if (TEST_ANIM) {
            transition.setDuration(1500);
        }
        transition.setAnimateParentHierarchy(false);
        return transition;
    }

    /**
     * Rebuild all tabs based on {@link NetworkPolicyEditor} and
     * {@link #mShowWifi}, hiding the tabs entirely when applicable. Selects
     * first tab, and kicks off a full rebind of body contents.
     */
    private void updateTabs() {
        final Context context = getActivity();
        mTabHost.clearAllTabs();

        final boolean mobileSplit = isMobilePolicySplit();
        if (mobileSplit && hasReadyMobile4gRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_3G, R.string.data_usage_tab_3g));
            mTabHost.addTab(buildTabSpec(TAB_4G, R.string.data_usage_tab_4g));
        } else if (hasReadyMobileRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_MOBILE, R.string.data_usage_tab_mobile));
        }
        if (mShowWifi && hasWifiRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_WIFI, R.string.data_usage_tab_wifi));
        }
        if (mShowEthernet && hasEthernet(context)) {
            mTabHost.addTab(buildTabSpec(TAB_ETHERNET, R.string.data_usage_tab_ethernet));
        }

        final boolean noTabs = mTabWidget.getTabCount() == 0;
        final boolean multipleTabs = mTabWidget.getTabCount() > 1;
        mTabWidget.setVisibility(multipleTabs ? View.VISIBLE : View.GONE);
        if (mIntentTab != null) {
            if (Objects.equal(mIntentTab, mTabHost.getCurrentTabTag())) {
                // already hit updateBody() when added; ignore
                updateBody();
            } else {
                mTabHost.setCurrentTabByTag(mIntentTab);
            }
            mIntentTab = null;
        } else if (noTabs) {
            // no usable tabs, so hide body
            updateBody();
        } else {
            // already hit updateBody() when added; ignore
        }
    }

    /**
     * Factory that provide empty {@link View} to make {@link TabHost} happy.
     */
    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            return new View(mTabHost.getContext());
        }
    };

    /**
     * Build {@link TabSpec} with thin indicator, and empty content.
     */
    private TabSpec buildTabSpec(String tag, int titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(getText(titleRes)).setContent(
                mEmptyTabContent);
    }

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            // user changed tab; update body
            SLog.i("onTabChanged getCurrentTabTag()== " + mTabHost.getCurrentTabTag());

            if (getPopupdisable()) {
                mTabHost.setCurrentTabByTag(mCurrentTab);
                SLog.i("OnTabChangeListener skip update :: " + mCurrentTab);
                return;
            }
            SLog.i("mTabListener :: updateBody");
            updateBody();
        }
    };

    /**
     * Update body content based on current tab. Loads
     * {@link NetworkStatsHistory} and {@link NetworkPolicy} from system, and
     * binds them to visible controls.
     */
    private void updateBody() {
        mBinding = true;
        if (!isAdded()) {
            return;
        }

        final Context context = getActivity();
        final String currentTab = mTabHost.getCurrentTabTag();
        final boolean isOwner = ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;

        if (currentTab == null) {
            Log.w(TAG, "no tab selected; hiding body");
            mListView.setVisibility(View.GONE);
            return;
        } else {
            mListView.setVisibility(View.VISIBLE);
        }

        mCurrentTab = currentTab;

        Log.d(TAG, "updateBody() with currentTab=" + currentTab);
        if (true == "KR".equals(Config.getCountry())) {
            if ("LGU".equals(Config.getOperator()) && Config.isDataRoaming()) {
                mDataEnabled.setVisibility(View.GONE);
            } else {
                mDataEnabled.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            }
        } else {
            mDataEnabledView.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        }

        if (TAB_MOBILE.equals(currentTab)) {
            if (true == "KR".equals(Config.getCountry())) {
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit_KR);
                setPreferenceSummary(mDisableAtLimitView,
                        getString(R.string.data_usage_disable_mobile_limit_summary));
                setPreferenceTitle(mAlertAtWarningView, R.string.title_data_usage_alert);
                setPreferenceSummary(mAlertAtWarningView,
                        getString(R.string.data_usage_alert_summary));
                if (null != mTrafficOptimizeView) {
                    setPreferenceTitle(mTrafficOptimizeView,
                            R.string.sp_data_usage_background_optimization);
                }
                //mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context));
            } else if (true == "VZW".equals(Config.getOperator())) {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
                setPreferenceTitle(mDataSleepEnabledView, R.string.sp_data_sleep_title_normal);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
                setPreferenceTitle(mAlertAtWarningView, R.string.item_alert_me);
            } else {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            }
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context));
        } else if (TAB_3G.equals(currentTab)) {
            if (true == "KR".equals(Config.getCountry())) {
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
                if (null != mTrafficOptimizeView) {
                    setPreferenceTitle(mTrafficOptimizeView,
                            R.string.sp_data_usage_background_optimization);
                }
            } else if (true == "VZW".equals(Config.getOperator())) {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_3g);
                setPreferenceTitle(mDataSleepEnabledView, R.string.sp_data_sleep_title_normal);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
            } else {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_3g);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
            }
            mTemplate = buildTemplateMobile3gLower(getActiveSubscriberId(context));
        } else if (TAB_4G.equals(currentTab)) {
            if (true == "KR".equals(Config.getCountry())) {
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_4g_limit);
                if (null != mTrafficOptimizeView) {
                    setPreferenceTitle(mTrafficOptimizeView,
                            R.string.sp_data_usage_background_optimization);
                }
            } else if (true == "VZW".equals(Config.getOperator())) {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_4g);
                setPreferenceTitle(mDataSleepEnabledView, R.string.sp_data_sleep_title_normal);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_4g_limit);
            } else {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_4g);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_4g_limit);
            }
            mTemplate = buildTemplateMobile4g(getActiveSubscriberId(context));
        } else if (TAB_WIFI.equals(currentTab)) {
            if (null != mDataEnabled
                    && ("KR".equals(Config.getCountry()))) {
                mDataEnabled.setVisibility(View.GONE);
            }
            if (null != mDataEnabledView) {
                mDataEnabledView.setVisibility(View.GONE);
            }
            if (null != mAlertAtWarningView) {
                mAlertAtWarningView.setVisibility(View.GONE);
            }
            if (null != mTrafficOptimizeView) {
                mTrafficOptimizeView.setVisibility(View.GONE);
            }
            if (null != mDataSleepEnabledView) {
                mDataSleepEnabledView.setVisibility(View.GONE);
            }
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateWifiWildcard();

            mTemplate_mobile = buildTemplateMobileAll(getActiveSubscriberId(context));
            SLog.i("mTemplate_mobile = " + mTemplate_mobile);
        } else if (TAB_ETHERNET.equals(currentTab)) {
            if (null != mDataEnabled
                    && ("KR".equals(Config.getCountry()))) {
                mDataEnabled.setVisibility(View.GONE);
            }
            if (null != mDataEnabledView) {
                mDataEnabledView.setVisibility(View.GONE);
            }
            if (null != mAlertAtWarningView) {
                mAlertAtWarningView.setVisibility(View.GONE);
            }
            if (null != mTrafficOptimizeView) {
                mTrafficOptimizeView.setVisibility(View.GONE);
            }
            if (null != mDataSleepEnabledView) {
                mDataSleepEnabledView.setVisibility(View.GONE);
            }
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateEthernet();
        } else {
            throw new IllegalStateException("unknown tab: " + currentTab);
        }
        // LGMDM [a1-mdm-dev@lge.com] [ID-MDM-167][ID-MDM-198]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.android.settings.Utils.supportSplitView(getActivity())) {
            if (hasReadyMobileRadio(context)) {
                boolean isWIFITab = false;
                if (TAB_WIFI.equals(mTabHost.getCurrentTabTag())) {
                    isWIFITab = true;
                }
                com.android.settings.MDMSettingsAdapter.getInstance().setDataUsageSummarySplitUI(
                        mMDMSummaryView, getActivity(), isWIFITab);
            }
        }
        // LGMDM_END
        // kick off loader for network history
        // TODO: consider chaining two loaders together instead of reloading
        // network history when showing app detail.
        SLog.i("updateBody() ::mChartDataCallbacks mIsChartDataBinding = " + mIsChartDataBinding);
        if (!mIsChartDataBinding) {
            SLog.i("updateBody() :: start chart loader ");
            getLoaderManager().restartLoader(LOADER_CHART_DATA,
                    ChartDataLoader.buildArgs(mTemplate, mCurrentApp), mChartDataCallbacks);
        } else {
            SLog.e("updateBody() :: skip chart loader");
        }
        // detail mode can change visible menus, invalidate
        getActivity().invalidateOptionsMenu();

        mBinding = false;
    }

    private boolean isSupportTrafficOptimezeCheckBox() {
        boolean result = false;
        if (Config.SKT.equals(Config.getOperator())
            && "true".equals(SystemProperties.get("ro.lge.skt_bg_data_opt_setting"))) {
            result = true;
        }
        Log.d(TAG, "isSupportTrafficOptimezeCheckBox = " + result);
        return result;
    }

    private boolean isAppDetailMode() {
        return mCurrentApp != null;
    }

    /**
     * Update UID details panels to match {@link #mCurrentApp}, showing or
     * hiding them depending on {@link #isAppDetailMode()}.
     */
    private void updateAppDetail() {
        final Context context = getActivity();
        final PackageManager pm = context.getPackageManager();
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        if (isAppDetailMode()) {
            mAppDetail.setVisibility(View.VISIBLE);
            mCycleAdapter.setChangeVisible(false);
        } else {
            mAppDetail.setVisibility(View.GONE);
            mCycleAdapter.setChangeVisible(true);

            // hide detail stats when not in detail mode
            mChart.bindDetailNetworkStats(null);
            return;
        }

        // remove warning/limit sweeps while in detail mode
        mChart.bindNetworkPolicy(null);

        // show icon and all labels appearing under this app
        final int uid = mCurrentApp.key;
        final UidDetail detail = mUidDetailProvider.getUidDetail(uid, true);
        mAppIcon.setImageDrawable(detail.icon);

        mAppTitles.removeAllViews();
        View title = null;
        if (detail.detailLabels != null) {
            final int n = detail.detailLabels.length;
            for (int i = 0; i < n; ++i) {
                CharSequence label = detail.detailLabels[i];
                CharSequence contentDescription = detail.detailContentDescriptions[i];
                title = inflater.inflate(R.layout.data_usage_app_title, mAppTitles, false);
                TextView appTitle = (TextView) title.findViewById(R.id.app_title);
                appTitle.setText(label);
                appTitle.setContentDescription(contentDescription);
                appTitle.setTypeface(Typeface.DEFAULT);
                mAppTitles.addView(title);
            }
        } else {
            title = inflater.inflate(R.layout.data_usage_app_title, mAppTitles, false);
            TextView appTitle = (TextView) title.findViewById(R.id.app_title);
            appTitle.setText(detail.label);
            appTitle.setContentDescription(detail.contentDescription);
            appTitle.setTypeface(Typeface.DEFAULT);
            mAppTitles.addView(title);
        }

        // Remember last slot for summary
        if (title != null) {
            mAppTotal = (TextView) title.findViewById(R.id.app_summary);
        } else {
            mAppTotal = null;
        }

        setAppDetailEnableSettingsButton(pm, uid, context);

        updateDetailData();

        if (UserHandle.isApp(uid) && !mPolicyManager.getRestrictBackground()
                && isBandwidthControlEnabled() && hasReadyMobileRadio(context)) {
            setPreferenceTitle(mAppRestrictView, R.string.data_usage_app_restrict_background);
            setPreferenceSummary(mAppRestrictView,
                    getString(R.string.data_usage_app_restrict_background_summary));

            mAppRestrictView.setVisibility(View.VISIBLE);
            mAppRestrict.setChecked(getAppRestrictBackground());

        } else {
            mAppRestrictView.setVisibility(View.GONE);
        }
    }

    private void setAppDetailEnableSettingsButton(final PackageManager pm, final int uid, final Context context) {
        // enable settings button when package provides it
        final String[] packageNames = pm.getPackagesForUid(uid);
        if (packageNames != null && packageNames.length > 0) {
            mAppSettingsIntent = new Intent(Intent.ACTION_MANAGE_NETWORK_USAGE);
            mAppSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);

            // Search for match across all packages
            boolean matchFound = false;
            for (String packageName : packageNames) {
                mAppSettingsIntent.setPackage(packageName);
                if (pm.resolveActivity(mAppSettingsIntent, 0) != null) {
                    matchFound = true;
                    break;
                }
            }

            mAppSettings.setEnabled(matchFound);
            mAppSettings.setVisibility(View.VISIBLE);

        } else {
            mAppSettingsIntent = null;
            mAppSettings.setVisibility(View.GONE);
        }

        updateDetailData();

        if (UserHandle.isApp(uid) && !mPolicyManager.getRestrictBackground()
                && isBandwidthControlEnabled() && hasReadyMobileRadio(context)) {
            setPreferenceTitle(mAppRestrictView, R.string.data_usage_app_restrict_background);
            setPreferenceSummary(mAppRestrictView,
                    getString(R.string.data_usage_app_restrict_background_summary));

            mAppRestrictView.setVisibility(View.VISIBLE);
            mAppRestrict.setChecked(getAppRestrictBackground());
            SLog.i("updateAppDetail :: mAppRestrictView background visible");
        } else {
            mAppRestrictView.setVisibility(View.GONE);
            SLog.i("updateAppDetail :: mAppRestrictView background gone");
        }
    }

    private long getPolicyWarningBytes() {
        Log.d(TAG, "getPolicyWarningBytes()");
        return mPolicyEditor.getPolicyWarningBytes(mTemplate);
    }

    private void setPolicyWarningBytes(long warningBytes) {
        Log.d(TAG, "setPolicyWarningBytes() = " + warningBytes);
        if (!isCurrentWiFiTab()) {
            mPolicyEditor.setPolicyWarningBytes(mTemplate, warningBytes);
            updatePolicy(false);
        }
    }

    private long getPolicyLimitBytes() {
        Log.d(TAG, "getPolicyLimitBytes()");
        return mPolicyEditor.getPolicyLimitBytes(mTemplate);
    }

    private void setPolicyLimitBytes(long limitBytes) {
        Log.d(TAG, "setPolicyLimitBytes() = " + limitBytes);
        if (!isCurrentWiFiTab()) {
            mPolicyEditor.setPolicyLimitBytes(mTemplate, limitBytes);
            updatePolicy(false);
        }
    }

    /**
     * Local cache of value, used to work around delay when
     * {@link ConnectivityManager#setMobileDataEnabled(boolean)} is async.
     */
    private Boolean mMobileDataEnabled;

    private boolean isMobileDataEnabled() {
        if ("ATT".equals(Config.getOperator()) && Config.isDataRoaming()) {
            return getDomesticMobileData();
        }
        if (mMobileDataEnabled != null) {
            // TODO: deprecate and remove this once enabled flag is on policy
            return mMobileDataEnabled;
        } else {
            return mTelephonyManager.getDataEnabled();
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        boolean isDataEnabled = isMobileDataEnabled();
        if (isDataEnabled != enabled) {
            Log.d(TAG, "setMobileDataEnabled()");
            if ("KDDI".equals(Config.getOperator())) {
                Utils.restoreLteModeForKddi(getActivity(), enabled);
            }

            if ("ATT".equals(Config.getOperator()) && Config.isDataRoaming()) {
                setDomesticMobileData(enabled);
            } else {
                mTelephonyManager.setDataEnabled(enabled);
            }

            if (!enabled && "KDDI".equals(Config.getOperator()) && Utils.isSupportedVolte(sContext)) {
                Intent intent = new Intent();
                intent.putExtra("is_roaming_onoff", false);
                intent.putExtra("change_networkmode", -1);
                intent.setAction("com.lge.settings.action.CHANGE_ROAMING_MODE");
                sContext.sendBroadcast(intent);
            }
            mMobileDataEnabled = enabled;
        }

        if (sIsForg) {
            updatePolicy(false);
        }
    }

    // KERRY START
    private static boolean confirmDialogShowed = false;
    private static boolean positiveResult = false;

    private static boolean isConfirmDialogShowed() {
        return confirmDialogShowed;
    }

    private static void setConfirmDialogShowed(boolean showed) {
        confirmDialogShowed = showed;
    }

    private static boolean isPositiveResult() {
        return positiveResult;
    }

    private static void setPositiveResult(boolean result) {
        positiveResult = result;
    }

    // KERRY END

    private boolean isNetworkPolicyModifiable(NetworkPolicy policy) {
        if (null == mDataEnabled) {
            SLog.i("isNetworkPolicyModifiable false");
            return false;
        } else {
            if ("VZW".equals(Config.getOperator())) {
                //add the wifi usage cycle.
                if (TAB_WIFI.equals(mCurrentTab)) {
                    SLog.i("isNetworkPolicyModifiable false");
                    return false;
                } else {
                    SLog.i("policy = " + policy);
                    SLog.i("isBandwidthControlEnabled() = " + isBandwidthControlEnabled());
                    return policy != null && isBandwidthControlEnabled()
                            && mDataEnabled.isChecked()
                            && ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;
                }
            } else {
                return policy != null && isBandwidthControlEnabled() && mDataEnabled.isChecked()
                        && ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;
            }
        }
    }

    private boolean isBandwidthControlEnabled() {
        try {
            return mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with INetworkManagementService: " + e);
            return false;
        }
    }

    private boolean getDataRoaming() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Global.getInt(resolver, Settings.Global.DATA_ROAMING + Utils.getCurrentDDS(sContext), 0) != 0;
    }

    private void setDataRoaming(boolean enabled) {
        final ContentResolver resolver = getActivity().getContentResolver();
        Settings.Global.putInt(resolver, Settings.Global.DATA_ROAMING + Utils.getCurrentDDS(sContext), enabled ? 1 : 0);
        if (mMenuDataRoaming != null) {
            mMenuDataRoaming.setChecked(enabled);
        }
    }

    public void setRestrictBackground(boolean restrictBackground) {
        Log.d(TAG, "setRestrictBackground()");

        // LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
        Settings.Secure.putInt(getActivity().getContentResolver(),
                SettingsConstants.Secure.DATA_NETWORK_USER_BACKGROUND_SETTING_DATA,
                restrictBackground ? 1 : 0);
        // LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

        if ("LGU".equals(Config.getOperator())) {
            if (!Config.isDataRoaming()) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Secure.DATA_NETWORK_BACKGROUND_DATA_ROMAING_BACKUP,
                        restrictBackground ? 1 : 0);
            }
        }

        mPolicyManager.setRestrictBackground(restrictBackground);
        mMenuRestrictBackground.setChecked(restrictBackground);
    }

    // LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
    public void handleSKT_setRestrictBackground(boolean restrictBackground) {
        Log.d(TAG, "handleSKT_setRestrictBackground() : " + restrictBackground);
        mPolicyManager.setRestrictBackground(restrictBackground);
        mMenuRestrictBackground.setChecked(restrictBackground);
    }

    // LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

    private boolean getAppRestrictBackground() {
        final int uid = mCurrentApp.key;
        final int uidPolicy = mPolicyManager.getUidPolicy(uid);
        return (uidPolicy & POLICY_REJECT_METERED_BACKGROUND) != 0;
    }

    private void setAppRestrictBackground(boolean restrictBackground) {
        Log.d(TAG, "setAppRestrictBackground()");
        if (false == isAppDetailMode()) {
            updateBody();
            SLog.i("setAppRestrictBackground() :: mCurrentApp == null");
            return;
        }
        final int uid = mCurrentApp.key;
        mPolicyManager.setUidPolicy(
                uid, restrictBackground ? POLICY_REJECT_METERED_BACKGROUND : POLICY_NONE);
        mAppRestrict.setChecked(restrictBackground);
    }

    /**
     * Update chart sweeps and cycle list to reflect {@link NetworkPolicy} for
     * current {@link #mTemplate}.
     */
    private void updatePolicy(boolean refreshCycle) {
        if (isAppDetailMode()) {
            if (null != mNetworkSwitches) {
                mNetworkSwitches.setVisibility(View.GONE);
            }
        } else {
            if (null != mNetworkSwitches) {
                mNetworkSwitches.setVisibility(View.VISIBLE);
            }
        }

        // TODO: move enabled state directly into policy
        if (TAB_MOBILE.equals(mCurrentTab)) {
            mBinding = true;
            if (!isAirplaneModeOn(getActivity())) {
                if (!isConfirmDialogShowed()) {
                    mDataEnabled.setChecked(isMobileDataEnabled());
                }
                //check the mDataSleepEnabledView
                mDataSleepEnabled.setChecked(mDataSleepInfo.isDataSleepEnabled());
            } else {
                mDataEnabled.setChecked(false);
            }
            //check the mDataSleepEnabledView
            setPreferenceSummary(mDataSleepEnabledView, getDataSleepSummaryString());
            mBinding = false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mDataEnabled != null) {
                if (com.android.settings.Utils.supportSplitView(getActivity())
                        && hasReadyMobileRadio(getActivity())) {
                    com.android.settings.MDMSettingsAdapter.getInstance()
                            .setDataUsageSwitchSplitUI(mDataEnabled);
                } else {
                    com.android.settings.MDMSettingsAdapter.getInstance().setDataUsageSwitch(
                            mDataEnabled, mDataEnabledListener);
                }
            }
            // LGMDM_END
        }

        final NetworkPolicy policy = mPolicyEditor.getPolicy(mTemplate);
        if (isNetworkPolicyModifiable(policy)
                && !("LGU".equals(Config.getOperator())
                && Config.isDataRoaming())) {
            if (null != mDisableAtLimitView) {
                mDisableAtLimitView.setVisibility(View.VISIBLE);
            }
            if (null != sDisableAtLimit) {
                sDisableAtLimit.setChecked(policy != null
                        && policy.limitBytes != LIMIT_DISABLED);
            }

            boolean isKR = "KR".equals(Config.getCountry());
            boolean isDispAlertAtWarningView = ("KR".equals(Config.getCountry()) || "VZW"
                    .equals(Config.getOperator()));
            if (null != mAlertAtWarningView) {
                mAlertAtWarningView.setVisibility(isDispAlertAtWarningView ? View.VISIBLE
                        : View.GONE);
            }
            if (null != sAlertAtWarning) {
                sAlertAtWarning.setChecked(policy != null
                        && policy.warningBytes != WARNING_DISABLED);
            }
            if (null != mTrafficOptimizeView) {
                mTrafficOptimizeView.setVisibility(isKR ? View.VISIBLE : View.GONE);
            }
            if (sTrafiicOptimizeCheckBox != null) {
                setCheckedTrafficServiceState();
            }
            // [START_LGT_SETTING] mod, 2012-07-03 : Fixed WBT ISSUE #374862.
            /*
            if (true == "VZW".equals(Config.getOperator())
                    && (null != sDisableAtLimit)
                    && (false == sDisableAtLimit.isChecked())) {
                if (null != policy) {
                    policy.warningBytes = WARNING_DISABLED;
                }
            }*/
            // [END_LGT_SETTING] mod, 2012-07-03 : Fixed WBT ISSUE #374863.
            if (!isAppDetailMode()) {
                if (null != mChart) {
                    mChart.bindNetworkPolicy(policy);
                }
            }

            if (null != mDataSleepEnabledView) {
                // temp gone
                //mDataSleepEnabledView.setVisibility(View.VISIBLE);
                mDataSleepEnabledView.setVisibility(View.GONE);
            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-344][ID-MDM-346]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                MDMSettingsAdapter.getInstance().setDataUsageLimitMenu(sDisableAtLimit,
                        mDisableAtLimitView);
                MDMSettingsAdapter.getInstance().setDataUsageWarnMenu(sAlertAtWarning,
                        mAlertAtWarningView);
            }
            // LGMDM_END
        } else {
            // controls are disabled; don't bind warning/limit sweeps
            if (null != mDisableAtLimitView) {
                mDisableAtLimitView.setVisibility(View.GONE);
            }
            if (null != mAlertAtWarningView) {
                mAlertAtWarningView.setVisibility(View.GONE);
            }
            if (null != mTrafficOptimizeView) {
                mTrafficOptimizeView.setVisibility(View.GONE);
            }
            if (null != mChart) {
                mChart.bindNetworkPolicy(null);
            }
            if (null != mDataSleepEnabledView) {
                mDataSleepEnabledView.setVisibility(View.GONE);
            }
        }

        if (refreshCycle) {
            // generate cycle list based on policy and available history
            if (true == "VZW".equals(Config.getOperator())
                    && true == TAB_WIFI.equals(mCurrentTab)) {
                updateWifiCycleList(policy);
            } else {
                updateCycleList(policy);
            }
        }
    }

    /**
     * Rebuild {@link #mCycleAdapter} based on {@link NetworkPolicy#cycleDay}
     * and available {@link NetworkStatsHistory} data. Always selects the newest
     * item, updating the inspection range on {@link #mChart}.
     */
    private void updateCycleList(NetworkPolicy policy) {
        // stash away currently selected cycle to try restoring below
        final CycleItem previousItem = (CycleItem)mCycleSpinner.getSelectedItem();
        mCycleAdapter.clear();

        final Context context = mCycleSpinner.getContext();

        long historyStart = Long.MAX_VALUE;
        long historyEnd = Long.MIN_VALUE;
        if (mChartData != null) {
            historyStart = mChartData.network.getStart();
            historyEnd = mChartData.network.getEnd();
        }
        Log.d(TAG, "Usage Clycle List historyStart = " + historyStart);
        Log.d(TAG, "Usage Clycle List historyEnd = " + historyEnd);

        final long now = System.currentTimeMillis();
        if (historyStart == Long.MAX_VALUE) {
            historyStart = now;
        }
        if (historyEnd == Long.MIN_VALUE) {
            historyEnd = now + 1;
        }

        if ((isCurrentWiFiTab()) && (historyStart < (historyEnd - DateUtils.YEAR_IN_MILLIS))) {
            Log.d(TAG, "Data Usage Clycle List YEAR_IN_MILLIS IN");
            if (historyEnd >= (now + DateUtils.DAY_IN_MILLIS)) {
                historyEnd = now + 1;
            }
            historyStart = historyEnd - DateUtils.YEAR_IN_MILLIS;
            Log.d(TAG, "Usage Clycle List historyStart Changed = " + historyStart);
            Log.d(TAG, "Usage Clycle List historyEnd Changed = " + historyEnd);
        }

        boolean hasCycles = computeCycleListBoundary(policy, context, historyStart, historyEnd);

        if (!hasCycles) {
            // no policy defined cycles; show entry for each four-week period
            long cycleEnd = historyEnd;
            while (cycleEnd > historyStart) {
                final long cycleStart = cycleEnd - (DateUtils.WEEK_IN_MILLIS * 4);
                mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                cycleEnd = cycleStart;
            }

            mCycleAdapter.setChangePossible(false);
        }

        // force pick the current cycle (first item)
        if (mCycleAdapter.getCount() > 0) {
            final int position = mCycleAdapter.findNearestPosition(previousItem);
            mCycleSpinner.setSelection(position);

            // only force-update cycle when changed; skipping preserves any
            // user-defined inspection region.
            final CycleItem selectedItem = mCycleAdapter.getItem(position);
            if (!Objects.equal(selectedItem, previousItem)) {
                mCycleListener.onItemSelected(mCycleSpinner, null, position, 0);
            } else {
                // but still kick off loader for detailed list
                updateDetailData();
            }
        } else {
            updateDetailData();
        }
    }

    private boolean computeCycleListBoundary(NetworkPolicy policy, final Context context, long historyStart, long historyEnd) {
        boolean hasCycles = false;
        if (policy != null) {
            // find the next cycle boundary
            long cycleEnd = computeNextCycleBoundary(historyEnd, policy);

            // walk backwards, generating all valid cycle ranges
            while (cycleEnd > historyStart) {
                final long cycleStart = computeLastCycleBoundary(cycleEnd, policy);
                Log.d(TAG, "generating cs=" + cycleStart + " to ce=" + cycleEnd
                        + " waiting for hs="
                        + historyStart);
                mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                cycleEnd = cycleStart;
                hasCycles = true;
            }

            // one last cycle entry to modify policy cycle day
            mCycleAdapter.setChangePossible(isNetworkPolicyModifiable(policy));
        }
        return hasCycles;
    }

    private void updateWifiCycleList(NetworkPolicy policy) {
        // stash away currently selected cycle to try restoring below
        final CycleItem previousItem = (CycleItem)mCycleSpinner.getSelectedItem();
        mCycleAdapter.clear();

        final Context context = mCycleSpinner.getContext();

        long historyStart = Long.MAX_VALUE;
        long historyEnd = Long.MIN_VALUE;
        if (mChartData != null) {
            historyStart = mChartData.network.getStart();
            historyEnd = mChartData.network.getEnd();
        }

        final long now = System.currentTimeMillis();
        if (historyStart == Long.MAX_VALUE) {
            historyStart = now;
        }
        if (historyEnd == Long.MIN_VALUE) {
            historyEnd = now + 1;
        }

        SLog.i("updateWifiCycleList :: mTemplate_mobile = " + mTemplate_mobile);
        SLog.i("updateWifiCycleList :: hasReadyMobileRadio(context) = "
                + hasReadyMobileRadio(context));
        boolean hasCycles = false;
        if (null != mTemplate_mobile && hasReadyMobileRadio(context)) {
            try {
                final NetworkStatsHistory network_mobile =
                        mStatsSession.getHistoryForNetwork(mTemplate_mobile,
                                FIELD_RX_BYTES | FIELD_TX_BYTES);
                final NetworkPolicy policy_mobile = mPolicyEditor.getPolicy(mTemplate_mobile);
                SLog.i("If the device is ready with the mobile data, display the wifi cycle day");
                if (network_mobile != null) {
                    historyStart = network_mobile.getStart();
                    historyEnd = network_mobile.getEnd();
                }
                if (historyStart == Long.MAX_VALUE) {
                    historyStart = now;
                }
                if (historyEnd == Long.MIN_VALUE) {
                    historyEnd = now + 1;
                }

                SLog.i("updateWifiCycleList :: historyStart = " + historyStart);
                SLog.i("updateWifiCycleList :: historyEnd = " + historyEnd);
                if (policy_mobile != null) {
                    // find the next cycle boundary
                    long cycleEnd = computeNextCycleBoundary(historyEnd, policy_mobile);
                    SLog.i("historyEnd = " + historyEnd);

                    // walk backwards, generating all valid cycle ranges
                    while (cycleEnd > historyStart) {
                        final long cycleStart =
                                computeLastCycleBoundary(cycleEnd, policy_mobile);
                        SLog.d(TAG, "updateWifiCycleList :: generating cs=" + cycleStart
                                + " to ce=" + cycleEnd + " waiting for hs=" + historyStart);
                        mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                        cycleEnd = cycleStart;
                        hasCycles = true;
                    }
                }
            } catch (RemoteException e) {
                SLog.i("error = " + e);
            }
        }

        if (!hasCycles)
        {
            // no policy defined cycles; show entry for each four-week period
            long cycleEnd = historyEnd;
            while (cycleEnd > historyStart) {
                final long cycleStart = cycleEnd - (DateUtils.WEEK_IN_MILLIS * 4);
                mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                cycleEnd = cycleStart;
                SLog.d(TAG, "updateWifiCycleList :: set the default wifi cycle ======== ");
            }
        }
        mCycleAdapter.setChangePossible(false);

        // force pick the current cycle (first item)
        if (mCycleAdapter.getCount() > 0) {
            final int position = mCycleAdapter.findNearestPosition(previousItem);
            mCycleSpinner.setSelection(position);
            // only force-update cycle when changed; skipping preserves any
            // user-defined inspection region.
            final CycleItem selectedItem = mCycleAdapter.getItem(position);
            if (!Objects.equal(selectedItem, previousItem)) {
                mCycleListener.onItemSelected(mCycleSpinner, null, position, 0);
            } else {
                // but still kick off loader for detailed list
                updateDetailData();
            }
        } else {
            updateDetailData();
        }
    }

    private OnCheckedChangeListener mDataSleepEnabledListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mBinding) {
                return;
            }
            if (false == sIsForg) {
                SLog.i("mDataSleepEnabledListener not forg, so skip...");
                return;
            }
            final String currentTab = mCurrentTab;
            if (TAB_MOBILE.equals(currentTab)) {
                if (!isAirplaneModeOn(getActivity())) {
                    SLog.i("mDataSleepEnabledListener set data sleep");
                    mDataSleepInfo.setDBDataSleepEnabled(isChecked);
                }
            }
            updatePolicy(true);
        }
    };

    private View.OnClickListener mDataSleepEnabledViewListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mDataSleepEnabled != null) {
                mDataSleepEnabled.setSoundEffectsEnabled(false);
                //mDataSleepEnabled.performClick();
                //start activity
                Intent baseIntent = new Intent();
                ComponentName component;
                component = new ComponentName("com.android.settings",
                        "com.android.settings.DataSleepPreferenceActivity");
                baseIntent.setComponent(component);
                baseIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(baseIntent);
                mDataSleepEnabled.setSoundEffectsEnabled(true);
            }
        }
    };

    private OnCheckedChangeListener mDataEnabledListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mBinding) {
                return;
            }
            if (false == sIsForg) {
                SLog.i("mDataEnabledListener not forg, so skip...");
                return;
            }
            final boolean dataEnabled = isChecked;

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)
                        || !com.lge.mdm.LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
                    return;
                }
            }
            // LGMDM_END

            final String currentTab = mCurrentTab;
            if (TAB_MOBILE.equals(currentTab)) {
                if (dataEnabled) {
                    if (true == "SKT".equals(Config.getOperator()) ||
                            true == "KDDI".equals(Config.getOperator()) ||
                            true == "KT".equals(Config.getOperator()) ||
                            true == "LGU".equals(Config.getOperator()) ||
                            true == (mIs_Ctc_Cn && Config.isDataRoaming()) ||
                            true == "DCM".equals(Config.getOperator())) {
                        setConfirmDialogShowed(true);
                        ConfirmDataEnableFragment.show(DataUsageSummary.this);
                    } else if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(sContext)) {
                        //DataSwitcher pop-up for TLF_ES START
                        /* [S][2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
                        if (isRememberOptionChecked()) {
                            setMobileDataEnabled(true);
                            android.provider.Settings.Secure.putInt(sContext.getContentResolver(),
                                    SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE,
                                    (isMobileDataEnabled() ? 1 : 0));
                        } else {
                            //Create intent to DialogActivity java from ConnectionManagerWidget
                            Intent connectionDialogIntent = new Intent();
                            connectionDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            connectionDialogIntent.setClassName(
                                    "com.lge.android.connectionmanager.widget",
                                    "com.lge.android.connectionmanager.widget.DialogActivity");
                            try
                            {
                                sContext.startActivity(connectionDialogIntent);
                            }
                            catch (ActivityNotFoundException e) {
                                //There is no widget installed
                                setMobileDataEnabled(true);
                            }
                        }
                    } else if ("VZW".equals(Config.getOperator()) && Utils.isTablet() && IsSupportPCO()) {
                        showSupportPCO_Dialog();
                    } else {
                        setMobileDataEnabled(true);
                    }
                } else {
                    if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(sContext)) {
                        // disabling data; show confirmation dialog which eventually
                        // calls setMobileDataEnabled() once user confirms.
                        //DataSwitcher pop-up for TLF_ES START
                        /* [2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
                        setMobileDataEnabled(false);
                    } else if ("VZW".equals(Config.getOperator())) {
                        if (Settings.Secure.getInt(getActivity().getContentResolver(),
                                "vzw_data_off_enabled", 0) == 0) {
                            ConfirmDataDisableFragment.show(DataUsageSummary.this);
                            setConfirmDialogShowed(true); // KERRY
                        } else {
                            setMobileDataEnabled(false);
                            setPositiveResult(true);
                        }
                    } else {
                        setConfirmDialogShowed(true); // KERRY
                        ConfirmDataDisableFragment.show(DataUsageSummary.this);
                    }
                }

                if (false == aboidBlink) {
                    aboidBlink = true;
                    task.cancel();
                    sTimer.purge();
                    sTimer = new Timer();
                    task = new TimerTask() {
                        public void run() {
                            aboidBlink = false;
                        }
                    };
                    sTimer.schedule(task, 2000);
                }
                else {
                    task.cancel();
                    sTimer.purge();
                    sTimer = new Timer();
                    task = new TimerTask() {
                        public void run() {
                            aboidBlink = false;
                        }
                    };
                    sTimer.schedule(task, 2000);
                }
                
                if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(sContext)) {
                    /* [2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
                    aboidBlink = false;
                }
            }
            if (!isConfirmDialogShowed()) {

                updatePolicy(true);
            }
        }
    };

    private View.OnClickListener mDataEnabledViewListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)
                        || !com.lge.mdm.LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
                    return;
                }
            }
            // LGMDM_END
            if (mDataEnabled != null) {
                mDataEnabled.setSoundEffectsEnabled(false);
                mDataEnabled.performClick();
                mDataEnabled.setSoundEffectsEnabled(true);
                // TODO Auto-generated method stub
            }
        }
    };

    private View.OnClickListener mDisableAtLimitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (false == sIsForg) {
                SLog.i("mDisableAtLimitListener not forg, so skip...");
                return;
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-344][ID-MDM-346]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().onClickDataUsageLimitMenu()) {
                    return;
                }
            }
            // LGMDM_END
            if (Utils.isMonkeyRunning() || mPolicyEditor.getPolicy(mTemplate) == null) {
                return;
            }

            final boolean disableAtLimit = !sDisableAtLimit.isChecked();
            if (disableAtLimit) {
                // enabling limit; show confirmation dialog which eventually
                // calls setPolicyLimitBytes() once user confirms.
                setPopupdisable(true);
                ConfirmLimitFragment.show(DataUsageSummary.this);
            } else {
                setPolicyLimitBytes(LIMIT_DISABLED);

                //if (true == "VZW".equals(Config.getOperator())) {
                //    setPolicyWarningBytes(WARNING_DISABLED);
                //}
            }
        }
    };

    private View.OnClickListener mAlertAtWarningListener = new View.OnClickListener() {
        /** {@inheritDoc} */
        public void onClick(View v) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-345][ID-MDM-347]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().onClickDataUsageWarnMenu()) {
                    return;
                }
            }
            // LGMDM_END
            if (Utils.isMonkeyRunning() || mPolicyEditor.getPolicy(mTemplate) == null) {
                return;
            }

            final boolean disableAtWarning = !sAlertAtWarning.isChecked();
            if (disableAtWarning) {
                // enabling limit; show confirmation dialog which eventually
                // calls setPolicyLimitBytes() once user confirms.
                setPopupdisable(true);
                ConfirmWarningFragment.show(DataUsageSummary.this);
            } else {
                setPolicyWarningBytes(WARNING_DISABLED);
            }
        }
    };

    private View.OnClickListener mTrafficOptimizeSupportCheckBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sTrafiicOptimizeCheckBox.isChecked()) {
                ConfirmTrafficServiceDisableFragment.show(DataUsageSummary.this);
            } else {
                ConfirmTrafficServiceEnableFragment.show(DataUsageSummary.this);
            }
        }
    };

    private View.OnClickListener mTrafficOptimizationListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (Utils.checkPackage(sContext, "com.skt.apra")) {
                //skt t-baseplatform v0.9.6
                Intent intent = new Intent("com.skt.apra.action.START_ACTIVITY");
                startActivity(intent);
            } else {
                Log.e(TAG, "not found package : background traffic automatic optimization");
            }
        }
    };

    private View.OnClickListener mAppRestrictListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }

            final boolean restrictBackground = !mAppRestrict.isChecked();

            if (restrictBackground) {
                // enabling restriction; show confirmation dialog which
                // eventually calls setRestrictBackground() once user
                // confirms.
                ConfirmAppRestrictFragment.show(DataUsageSummary.this);
            } else {
                setAppRestrictBackground(false);
            }
        }
    };

    private OnClickListener mAppSettingsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }

            if (!isAdded()) {
                return;
            }
            // TODO: target torwards entire UID instead of just first package
            startActivity(mAppSettingsIntent);
        }
    };

    private OnItemClickListener mListListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AppItem app = (AppItem)parent.getItemAtPosition(position);

            // TODO: sigh, remove this hack once we understand 6450986
            if (mUidDetailProvider == null || app == null) {
                return;
            }
            final UidDetail detail = mUidDetailProvider.getUidDetail(app.key, true);
            AppDetailsFragment.show(DataUsageSummary.this, app, detail.label);
        }
    };

    private View.OnTouchListener mCycleOnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-342][ID-MDM-343]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    if (MDMSettingsAdapter.getInstance().setDataUsageCycle(getActivity(),
                            mTabHost.getCurrentTabTag())) {
                        return true;
                    }
                }
                // LGMDM_END
            }
            return false;
        }
    };

    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final CycleItem cycle = (CycleItem)parent.getItemAtPosition(position);
            if (cycle instanceof CycleChangeItem) {
                // show cycle editor; will eventually call setPolicyCycleDay()
                // when user finishes editing.
                CycleEditorFragment.show(DataUsageSummary.this);

                // reset spinner to something other than "change cycle..."
                mCycleSpinner.setSelection(0);

            } else {

                if (null != cycle) {
                    Log.d(TAG, "showing cycle " + cycle + ", start=" + cycle.start + ", end="
                            + cycle.end + "]");
                } // 120103 WBT


                // update chart to show selected cycle, and update detail data
                // to match updated sweep bounds.
                if (null != cycle) {
                    mChart.setVisibleRange(cycle.start, cycle.end);
                } // 120103 WBT

                updateDetailData();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // ignored
        }
    };

    /**
     * Update details based on {@link #mChart} inspection range depending on
     * current mode. In network mode, updates {@link #mAdapter} with sorted list
     * of applications data usage, and when {@link #isAppDetailMode()} update
     * app details.
     */
    private void updateDetailData() {
        Log.d(TAG, "updateDetailData()");
        if (null == mChart) {
            SLog.i("updateDetailData mChar null, return...");
            return;
        }

        long start = mChart.getInspectStart();
        long end = mChart.getInspectEnd();
        final long now = System.currentTimeMillis();
        final Context context = getActivity();
        final CycleItem mCurCycleItem = (CycleItem)mCycleSpinner.getSelectedItem();
        final long mLimited_start = mCurCycleItem.start;
        final long mLimited_end = mCurCycleItem.end;

        if (start < mLimited_start
                || end > mLimited_end) {
            SLog.i("mCurCycleItem = " + mCurCycleItem);
            String mCurDateRange = formatDateRange(context, start, end);
            SLog.i("before mCurDateRange = " + mCurDateRange);
            start = start < mLimited_start ? mLimited_start : start;
            end = end > mLimited_end ? mLimited_end : end;
            mCurDateRange = formatDateRange(context, start, end);
            SLog.i("after mCurDateRange = " + mCurDateRange);
        }

        NetworkStatsHistory.Entry entry = null;
        if (isAppDetailMode() && mChartData != null && mChartData.detail != null) {
            // bind foreground/background to piechart and labels
            entry = mChartData.detailDefault.getValues(start, end, now, entry);
            final long defaultBytes = entry.rxBytes + entry.txBytes;
            entry = mChartData.detailForeground.getValues(start, end, now, entry);
            final long foregroundBytes = entry.rxBytes + entry.txBytes;
            final long totalBytes = defaultBytes + foregroundBytes;

            setAppTotalText(context, totalBytes);
            //mAppBackground.setText(Formatter.formatFileSize(context, defaultBytes));
            //mAppForeground.setText(Formatter.formatFileSize(context, foregroundBytes));
            mAppBackground.setText(formatFileSizeForUnit(context, defaultBytes, sDisplayUnit));
            mAppForeground.setText(formatFileSizeForUnit(context, foregroundBytes, sDisplayUnit));

            // and finally leave with summary data for label below
            entry = mChartData.detail.getValues(start, end, now, null);
            getLoaderManager().destroyLoader(LOADER_SUMMARY);

            mCycleSummary.setVisibility(View.GONE);
        } else {
            if (mChartData != null) {
                entry = mChartData.network.getValues(start, end, now, null);
            }

            mCycleSummary.setVisibility(View.VISIBLE);

            // kick off loader for detailed stats
            getLoaderManager().restartLoader(LOADER_SUMMARY,
                    SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
        }

        final long totalBytes = entry != null ? entry.rxBytes + entry.txBytes : 0;
        //final String totalPhrase = Formatter.formatFileSize(context, totalBytes);
        final String totalPhrase = formatFileSizeForUnit(context, totalBytes, sDisplayUnit);
        mCycleSummary.setText(totalPhrase);

        if (TAB_MOBILE.equals(mCurrentTab) || TAB_3G.equals(mCurrentTab)
                || TAB_4G.equals(mCurrentTab)) {
            if (isAppDetailMode()) {
                mDisclaimer.setVisibility(View.GONE);
            } else {
                mDisclaimer.setVisibility(View.VISIBLE);
            }
        } else {
            mDisclaimer.setVisibility(View.GONE);
        }

        // initial layout is finished above, ensure we have transitions
        ensureLayoutTransitions();
    }

    private void setAppTotalText(final Context context, final long totalBytes) {
        if (mAppTotal != null) {
            mAppTotal.setText(formatFileSizeForUnit(context, totalBytes, sDisplayUnit));
        }
    }

    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<
            ChartData>() {
                @Override
                public Loader<ChartData> onCreateLoader(int id, Bundle args) {
                    mIsChartDataBinding = true;
                    SLog.i("mChartDataCallbacks::onCreateLoader() mIsChartDataBinding = true");
                    return new ChartDataLoader(getActivity(), mStatsSession, args);
                }

                @Override
                public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
                    mChartData = data;
                    mChart.bindNetworkStats(mChartData.network);
                    mChart.bindDetailNetworkStats(mChartData.detail);
                    SLog.i("onLoadFinished");
                    // calcuate policy cycles based on available data
                    updatePolicy(true);
                    updateAppDetail();

                    // force scroll to top of body when showing detail
                    if (mChartData.detail != null) {
                        // TD170328 : Fixed to display the update screen with position 0.
                        //mListView.smoothScrollToPosition(0);
                        mListView.smoothScrollToPositionFromTop(0, 1);
                    }
                    mIsChartDataBinding = false;
                    SLog.i("mChartDataCallbacks:: mIsChartDataBinding = false");
                }

                @Override
                public void onLoaderReset(Loader<ChartData> loader) {
                    SLog.i("mChartDataCallbacks::onLoaderReset() mIsChartDataBinding = false");
                    mIsChartDataBinding = false;
                    mChartData = null;
                    mChart.bindNetworkStats(null);
                    mChart.bindDetailNetworkStats(null);
                }
            };

    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<
            NetworkStats>() {
                @Override
                public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
                    return new SummaryForAllUidLoader(getActivity(), mStatsSession, args);
                }

                @Override
                public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
                    final int[] restrictedUids = mPolicyManager.getUidsWithPolicy(
                            POLICY_REJECT_METERED_BACKGROUND);
                    mAdapter.bindStats(data, restrictedUids);
                    updateEmptyVisible();
                }

                @Override
                public void onLoaderReset(Loader<NetworkStats> loader) {
                    mAdapter.bindStats(null, new int[0]);
                    updateEmptyVisible();
                }

                private void updateEmptyVisible() {
                    final boolean isEmpty = mAdapter.isEmpty() && !isAppDetailMode();
                    mEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    mStupidPadding.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                }
            };

    @Deprecated
    private boolean isMobilePolicySplit() {
        final Context context = getActivity();
        if (hasReadyMobileRadio(context)) {
            return mPolicyEditor.isMobilePolicySplit(getActiveSubscriberId(context));
        } else {
            return false;
        }
    }

    @Deprecated
    private void setMobilePolicySplit(boolean split) {
        final Context context = getActivity();
        if (hasReadyMobileRadio(context)) {
            mPolicyEditor.setMobilePolicySplit(getActiveSubscriberId(context), split);
        }
    }

    private static String getActiveSubscriberId(Context context) {
        final TelephonyManager tele = TelephonyManager.from(context);
        final String actualSubscriberId = tele.getSubscriberId();
        return SystemProperties.get(TEST_SUBSCRIBER_PROP, actualSubscriberId);
    }

    private DataUsageChartListener mChartListener = new DataUsageChartListener() {
        @Override
        public void onWarningChanged() {
            setPolicyWarningBytes(mChart.getWarningBytes());
        }

        @Override
        public void onLimitChanged() {
            setPolicyLimitBytes(mChart.getLimitBytes());
        }

        @Override
        public void requestWarningEdit() {
            // check the datausage fragement state.
            if (false == sIsForg) {
                SLog.i("requestWarningEdit not forg, so skip...");
                return;
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-345][ID-MDM-347]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().setDataUsageWarnToast(getActivity())) {
                    return;
                }
            }
            // LGMDM_END
            if (!getPopupdisable()) {
                setPopupdisable(true);
                WarningEditorFragment.show(DataUsageSummary.this);
            }
        }

        @Override
        public void requestLimitEdit() {
            // check the datausage fragement state.
            if (false == sIsForg) {
                SLog.i("requestLimitEdit not forg, so skip...");
                return;
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-344][ID-MDM-346]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().setDataUsageLimitToast(getActivity())) {
                    return;
                }
            }
            // LGMDM_END
            if (!getPopupdisable()) {
                setPopupdisable(true);
                LimitEditorFragment.show(DataUsageSummary.this);
            }
        }
    };

    /**
     * List item that reflects a specific data usage cycle.
     */
    public static class CycleItem implements Comparable<CycleItem> {
        public CharSequence label;
        public long start;
        public long end;

        CycleItem(CharSequence label) {
            this.label = label;
        }

        public CycleItem(Context context, long start, long end) {
            this.label = formatDateRange(context, start, end);
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return label.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CycleItem) {
                final CycleItem another = (CycleItem)o;
                return start == another.start && end == another.end;
            }
            return false;
        }

        @Override
        public int compareTo(CycleItem another) {
            return Long.compare(start, another.start);
        }
    }

    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static final java.util.Formatter sFormatter = new java.util.Formatter(
            sBuilder, Locale.getDefault());

    public static String formatDateRange(Context context, long start, long end) {
        final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH;

        synchronized (sBuilder) {
            sBuilder.setLength(0);
            /* LGSI_CHANGE_S :Data Usage Date Format
             * 2012-04-23, mohammed.miran@lge.com,
             * Data Usage Date Format change due to string cut
             */
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ar")) {
                return convertArabNum(DateUtils
                        .formatDateRange(context, sFormatter, start, end, flags, null).toString(),
                        false);
            } else {
                return DateUtils.formatDateRange(context, sFormatter, start, end, flags, null)
                        .toString();
            }
            /* LGSI_CHANGE_E :Data Usage Date Format */
        }
    }

    /* LGSI_CHANGE_S :Data Usage Date Format
     * 2012-04-23, mohammed.miran@lge.com,
     * Data Usage Date Format change due to string cut
     */

    public static String convertArabNum(String ActualStr, boolean Trim) {
        int check = 0;
        char[] arabfarsi_str = new char[ActualStr.length()];
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ar"))
        {
            for (int i = 0; i < ActualStr.length(); i++) {
                check = Character.digit(ActualStr.charAt(i), 10);
                if (check >= 0 && check <= 9) {
                    arabfarsi_str[i] = new Character((char)(check + 1632));
                } else {
                    arabfarsi_str[i] = new Character(ActualStr.charAt(i));
                }
            }
        }
        String ArabFarsi_Str = new String(arabfarsi_str);
        if (Trim) {
            return ArabFarsi_Str;
        } else {
            return ArabFarsi_Str.trim();
        }
    }

    /* LGSI_CHANGE_E :Data Usage Date Format */

    /**
     * Special-case data usage cycle that triggers dialog to change
     * {@link NetworkPolicy#cycleDay}.
     */
    public static class CycleChangeItem extends CycleItem {
        public CycleChangeItem(Context context) {
            super(context.getString(R.string.data_usage_change_cycle));
        }
    }

    public static class CycleAdapter extends ArrayAdapter<CycleItem> {
        private boolean mChangePossible = false;
        private boolean mChangeVisible = false;

        private final CycleChangeItem mChangeItem;

        public CycleAdapter(Context context) {
            super(context, R.layout.data_usage_cycle_item);
            setDropDownViewResource(R.layout.data_usage_cycle_item_dropdown);
            mChangeItem = new CycleChangeItem(context);
        }

        public void setChangePossible(boolean possible) {
            mChangePossible = possible;
            updateChange();
        }

        public void setChangeVisible(boolean visible) {
            mChangeVisible = visible;
            updateChange();
        }

        private void updateChange() {
            remove(mChangeItem);
            if (mChangePossible && mChangeVisible) {
                add(mChangeItem);
            }
        }

        /**
         * Find position of {@link CycleItem} in this adapter which is nearest
         * the given {@link CycleItem}.
         */
        public int findNearestPosition(CycleItem target) {
            if (target != null) {
                final int count = getCount();
                for (int i = count - 1; i >= 0; i--) {
                    final CycleItem item = getItem(i);
                    if (item instanceof CycleChangeItem) {
                        continue;
                    } else if (item.compareTo(target) >= 0) {
                        return i;
                    }
                }
            }
            return 0;
        }
    }

    public static class AppItem implements Comparable<AppItem>, Parcelable {
        public static final int CATEGORY_USER = 0;
        public static final int CATEGORY_APP_TITLE = 1;
        public static final int CATEGORY_APP = 2;

        public final int key;
        public boolean restricted;
        public int category;

        public SparseBooleanArray uids = new SparseBooleanArray();
        public long total;

        public AppItem() {
            this.key = 0;
        }

        public AppItem(int key) {
            this.key = key;
        }

        public AppItem(Parcel parcel) {
            key = parcel.readInt();
            uids = parcel.readSparseBooleanArray();
            total = parcel.readLong();
        }

        public void addUid(int uid) {
            uids.put(uid, true);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(key);
            dest.writeSparseBooleanArray(uids);
            dest.writeLong(total);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public int compareTo(AppItem another) {
            int comparison = Integer.compare(category, another.category);
            if (comparison == 0) {
                comparison = Long.compare(another.total, total);
            }
            return comparison;
        }

        public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
            @Override
            public AppItem createFromParcel(Parcel in) {
                return new AppItem(in);
            }

            @Override
            public AppItem[] newArray(int size) {
                return new AppItem[size];
            }
        };
    }

    /**
     * Adapter of applications, sorted by total usage descending.
     */
    public static class DataUsageAdapter extends BaseAdapter {
        private final UidDetailProvider mProvider;
        private final int mInsetSide;
        private final UserManager mUm;

        private ArrayList<AppItem> mItems = Lists.newArrayList();
        private long mLargest;

        public DataUsageAdapter(final UserManager userManager, UidDetailProvider provider, int insetSide) {
            mProvider = checkNotNull(provider);
            mInsetSide = insetSide;
            mUm = userManager;
        }

        /**
         * Bind the given {@link NetworkStats}, or {@code null} to clear list.
         */
        public void bindStats(NetworkStats stats, int[] restrictedUids) {
            mItems.clear();
            mLargest = 0;

            final int currentUserId = ActivityManager.getCurrentUser();
            final List<UserHandle> profiles = mUm.getUserProfiles();
            final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();

            NetworkStats.Entry entry = null;
            final int size = stats != null ? stats.size() : 0;
            for (int i = 0; i < size; i++) {
                entry = stats.getValues(i, entry);

                // Decide how to collapse items together
                final int uid = entry.uid;
                final int collapseKey;
                final int category;
                final int userId = UserHandle.getUserId(uid);

                if (UserHandle.isApp(uid)) {
                    if (profiles.contains(new UserHandle(userId))) {
                        if (userId != currentUserId) {
                            // Add to a managed user item.
                            final int managedKey = UidDetailProvider.buildKeyForUser(userId);
                            accumulate(managedKey, knownItems, entry,
                                    AppItem.CATEGORY_USER);
                        }
                        // Add to app item.
                        collapseKey = uid;
                        category = AppItem.CATEGORY_APP;
                    } else {
                        // Add to other user item.
                        collapseKey = UidDetailProvider.buildKeyForUser(userId);
                        category = AppItem.CATEGORY_USER;
                    }
                } else if (uid == UID_REMOVED || uid == UID_TETHERING || uid == 1050) {
                    collapseKey = uid;
                    category = AppItem.CATEGORY_APP;
                } else {
                    collapseKey = android.os.Process.SYSTEM_UID;
                    category = AppItem.CATEGORY_APP;
                }
                accumulate(collapseKey, knownItems, entry, category);
            }

            final int restrictedUidsMax = restrictedUids.length;
            for (int i = 0; i < restrictedUidsMax; ++i) {
                final int uid = restrictedUids[i];
                // Only splice in restricted state for current user or managed users
                if (!profiles.contains(new UserHandle(UserHandle.getUserId(uid)))) {
                    continue;
                }

                AppItem item = knownItems.get(uid);
                if (item == null) {
                    item = new AppItem(uid);
                    item.total = -1;
                    mItems.add(item);
                    knownItems.put(item.key, item);
                }
                item.restricted = true;
            }

            Collections.sort(mItems);
            notifyDataSetChanged();
        }

        /**
         * Accumulate data usage of a network stats entry for the item mapped by the collapse key.
         * Creates the item if needed.
         *
         * @param collapseKey the collapse key used to map the item.
         * @param knownItems collection of known (already existing) items.
         * @param entry the network stats entry to extract data usage from.
         * @param itemCategory the item is categorized on the list view by this category. Must be
         *            either AppItem.APP_ITEM_CATEGORY or AppItem.MANAGED_USER_ITEM_CATEGORY
         */
        private void accumulate(int collapseKey, final SparseArray<AppItem> knownItems,
                NetworkStats.Entry entry, int itemCategory) {
            final int uid = entry.uid;
            AppItem item = knownItems.get(collapseKey);
            if (item == null) {
                item = new AppItem(collapseKey);
                item.category = itemCategory;
                mItems.add(item);
                knownItems.put(item.key, item);
            }
            item.addUid(uid);
            item.total += entry.rxBytes + entry.txBytes;
            if (mLargest < item.total) {
                mLargest = item.total;
            }
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).key;
        }

        /**
         * See {@link #getItemViewType} for the view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * Returns 1 for separator items and 0 for anything else.
         */
        @Override
        public int getItemViewType(int position) {
            final AppItem item = mItems.get(position);
            if (item.category == AppItem.CATEGORY_APP_TITLE) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position > mItems.size()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return getItemViewType(position) == 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppItem item = mItems.get(position);
            if (getItemViewType(position) != AppItem.CATEGORY_APP_TITLE) {
                AppViewHolder holder = AppViewHolder.createOrRecycle(
                        LayoutInflater.from(parent.getContext()), convertView);
                convertView = holder.rootView;

                if (mInsetSide > 0) {
                    convertView.setPaddingRelative(mInsetSide, 0, mInsetSide, 0);
                }

                // kick off async load of app details
                final UidDetail detail = mProvider.getUidDetail(item.key, true);
                if (detail.icon != null) {
                    holder.appIcon.setImageDrawable(detail.icon);
                }

                if (detail.label != null) {
                    holder.title.setText(detail.label);
                }

                if (item.restricted && item.total <= 0) {
                    holder.text1.setText(R.string.data_usage_app_restricted);
                    holder.progress.setVisibility(View.GONE);
                } else {
                    //holder.text1.setText(Formatter.formatFileSize(parent.getContext(), item.total));
                    holder.text1.setText(formatFileSizeForUnit(parent.getContext(),
                            item.total,
                            sDisplayUnit));
                    holder.progress.setVisibility(View.VISIBLE);
                }

                final int percentTotal = mLargest != 0 ? (int)(item.total * 100 / mLargest) : 0;
                holder.progress.setProgress(percentTotal);    
            }

            return convertView;
        }
    }

    /**
     * Empty {@link Fragment} that controls display of UID details in
     * {@link DataUsageSummary}.
     */
    public static class AppDetailsFragment extends Fragment {
        private static final String EXTRA_APP = "app";

        public static void show(DataUsageSummary parent, AppItem app, CharSequence label) {
            if (!parent.isAdded()) {
                return;
            }
            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_APP, app);

            final AppDetailsFragment fragment = new AppDetailsFragment();
            fragment.setArguments(args);
            fragment.setTargetFragment(parent, 0);
            final FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
            ft.add(fragment, TAG_APP_DETAILS);
            ft.addToBackStack(TAG_APP_DETAILS);
            ft.setBreadCrumbTitle(
                    parent.getResources().getString(R.string.data_usage_app_summary_title));
            ft.commitAllowingStateLoss();
            SLog.i("AppDetailsFragment :: show");
        }

        @Override
        public void onStart() {
            super.onStart();
            SLog.i("AppDetailsFragment :: onStart");
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            target.mCurrentApp = getArguments().getParcelable(EXTRA_APP);
            target.updateBody();
        }

        @Override
        public void onStop() {
            super.onStop();
            SLog.i("AppDetailsFragment :: onStop");
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            target.mCurrentApp = null;
            if (sIsForg) {
                target.updateBody();
            }
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link NetworkPolicy#limitBytes}.
     */
    public static class ConfirmLimitFragment extends DialogFragment {
        private static final String EXTRA_MESSAGE = "message";
        private static final String EXTRA_LIMIT_BYTES = "limitBytes";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            
            final NetworkPolicy policy = parent.mPolicyEditor.getPolicy(parent.mTemplate);
            if (policy == null) {
                return;
            }
            
            if (parent.mPolicyEditor == null
                    || TAB_WIFI.equals(parent.mCurrentTab)
                    || parent.mPolicyEditor.getPolicy(parent.mTemplate) == null) {
                SLog.i("ConfirmLimitFragment " + parent.mCurrentTab);
                setPopupdisable(false);
                return;
            }

            final Resources res = parent.getResources();
            final CharSequence message;
            final long minLimitBytes = (long) (policy.warningBytes * 1.2f);
            final long limitBytes;

            // TODO: customize default limits based on network template
            final String currentTab = parent.mCurrentTab;
            if (TAB_3G.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
            } else if (TAB_4G.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
            } else if (TAB_MOBILE.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                if ((Config.TMO.equals(Config.getOperator())
                        || Config.MPCS.equals(Config.getOperator()))
                        && "US".equals(Config.getCountry())) {
                    limitBytes = Math.max(100 * GB_IN_BYTES, minLimitBytes);
                } else if (Config.KT.equals(Config.getOperator())) {
                    limitBytes = Math.max(8 * GB_IN_BYTES, minLimitBytes);
                } else if (Config.SKT.equals(Config.getOperator())) {
                    limitBytes = Math.max((long)(7.2 * GB_IN_BYTES), minLimitBytes);
                } else {
                    limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
                }
            } else {
                throw new IllegalArgumentException("unknown current tab: " + currentTab);
            }

            final Bundle args = new Bundle();
            args.putCharSequence(EXTRA_MESSAGE, message);
            args.putLong(EXTRA_LIMIT_BYTES, limitBytes);

            final ConfirmLimitFragment dialog = new ConfirmLimitFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_LIMIT);
        }

        private static CharSequence buildDialogMessage(Resources res, int networkResId) {
            // Apply the VZW ODR.
            if ("VZW".equals(Config.getOperator())) {
                return res.getString(R.string.body_your_mobile_data_turn_off);
            } else if (networkResId != 0) {
                return res.getString(networkResId);
            } else {
                return res.getString(R.string.data_usage_limit_dialog_mobile);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final CharSequence message = getArguments().getCharSequence(EXTRA_MESSAGE);
            final long limitBytes = getArguments().getLong(EXTRA_LIMIT_BYTES);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if ("VZW".equals(Config.getOperator())) {
                builder.setTitle(R.string.sp_data_usage_limit_mobile_VZW);
            } else {
                builder.setTitle(R.string.data_usage_disable_mobile_limit_KR);
            }
            builder.setMessage(message);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                    if (target != null) {
                        if (target.mPolicyEditor == null
                                || TAB_WIFI.equals(target.mCurrentTab)
                                || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                            SLog.i("ConfirmLimitFragment onclick " + target.mCurrentTab);
                            return;
                        }
                        if ("VZW".equals(Config.getOperator())) {
                            long warningBytes = 2 * GB_IN_BYTES;
                            long newValue = 5 * GB_IN_BYTES;
                            if (true == sAlertAtWarning.isChecked()) {
                                warningBytes = target.getPolicyWarningBytes();
                                if (warningBytes >= 5 * GB_IN_BYTES) {
                                    newValue = warningBytes + warningBytes / 10;
                                } else {
                                    newValue = 5 * GB_IN_BYTES;
                                }
                            } else {
                                SLog.i("ConfirmLimitFragment onclick setPolicyWarningBytes(WARNING_DISABLED)");
                                target.setPolicyWarningBytes(WARNING_DISABLED);
                            }
                            SLog.i("ConfirmLimitFragment onclick setPolicyWarningBytes(WARNING_DISABLED)");
                            target.setPolicyLimitBytes(newValue);
                        } else {
                            target.setPolicyLimitBytes(limitBytes);
                        }
                        //target.setPolicyLimitBytes(limitBytes);
                    }
                }
            });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            setPopupdisable(false);
        }

    }

    /**
     * Dialog to request user confirmation before setting
     * {@link NetworkPolicy#warningBytes}.
     */
    public static class ConfirmWarningFragment extends DialogFragment {
        private static final String EXTRA_MESSAGE = "message";
        private static final String EXTRA_WARNING_BYTES = "warningBytes";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }

            if (parent.mPolicyEditor == null
                    || TAB_WIFI.equals(parent.mCurrentTab)
                    || parent.mPolicyEditor.getPolicy(parent.mTemplate) == null) {
                SLog.i("ConfirmWarningFragment " + parent.mCurrentTab);
                setPopupdisable(false);
                return;
            }

            final Resources res = parent.getResources();
            final CharSequence message;
            final long warningBytes;
            final long maxWarningBytes;
            if (parent.mPolicyEditor.getPolicyLimitBytes(parent.mTemplate) != LIMIT_DISABLED) {
                maxWarningBytes = (long)(
                        parent.mPolicyEditor.getPolicy(parent.mTemplate).limitBytes / 1.2f);
            } else {
                if (Config.KT.equals(Config.getOperator())
                        || Config.SKT.equals(Config.getOperator())) {
                    maxWarningBytes = 6 * GB_IN_BYTES;
                } else {
                    maxWarningBytes = 2 * GB_IN_BYTES;
                }

            }

            // TODO: customize default limits based on network template
            final String currentTab = parent.mCurrentTab;

            if (TAB_3G.equals(currentTab)) {
                message = res.getString(R.string.body_your_phone_will_alert);
                warningBytes = Math.min(2 * GB_IN_BYTES, maxWarningBytes);
            } else if (TAB_4G.equals(currentTab)) {
                message = res.getString(R.string.body_your_phone_will_alert);
                warningBytes = Math.min(2 * GB_IN_BYTES, maxWarningBytes);
            } else if (TAB_MOBILE.equals(currentTab)) {
                message = res.getString(R.string.body_your_phone_will_alert);
                if (Config.KT.equals(Config.getOperator())
                        || Config.SKT.equals(Config.getOperator())) {
                    warningBytes = Math.min(6 * GB_IN_BYTES, maxWarningBytes);
                } else {
                    warningBytes = Math.min(2 * GB_IN_BYTES, maxWarningBytes);
                }

            } else {
                throw new IllegalArgumentException("unknown current tab: " + currentTab);
            }

            final Bundle args = new Bundle();
            args.putCharSequence(EXTRA_MESSAGE, message);
            args.putLong(EXTRA_WARNING_BYTES, warningBytes);

            final ConfirmWarningFragment dialog = new ConfirmWarningFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_LIMIT);
        }

        //private static CharSequence buildDialogMessage(Resources res, int networkResId) {
        //    return res.getString(R.string.body_your_phone_will_alert, res.getString(networkResId));
        //}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final CharSequence message = getArguments().getCharSequence(EXTRA_MESSAGE);
            final long warningBytes = getArguments().getLong(EXTRA_WARNING_BYTES);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.title_data_usage_alert);
            builder.setMessage(message);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

                    if (target != null) {
                        if (target.mPolicyEditor == null
                                || TAB_WIFI.equals(target.mCurrentTab)
                                || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                            SLog.i("ConfirmWarningFragment onclick " + target.mCurrentTab);
                            return;
                        }
                        if ("VZW".equals(Config.getOperator())) {
                            long limitBytes = 5 * GB_IN_BYTES;
                            long newValue = 2 * GB_IN_BYTES;
                            if (true == target.sDisableAtLimit.isChecked()) {
                                limitBytes = target.getPolicyLimitBytes();
                                if (5 * GB_IN_BYTES >= limitBytes &&
                                        2 * GB_IN_BYTES < limitBytes) {
                                    newValue = 2 * GB_IN_BYTES;
                                } else {
                                    newValue = limitBytes - limitBytes / 10;
                                }
                            }
                            else {
                                SLog.i("ConfirmWarningFragment onclick setPolicyLimitBytes(LIMIT_DISABLED)");
                                target.setPolicyLimitBytes(LIMIT_DISABLED);
                            }
                            target.setPolicyWarningBytes(newValue);
                        } else {
                            target.setPolicyWarningBytes(warningBytes);
                        }
                        //target.setPolicyWarningBytes(warningBytes);
                    }
                }
            });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            setPopupdisable(false);
        }

    }

    /**
     * Dialog to edit {@link NetworkPolicy#cycleDay}.
     */
    public static class CycleEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final CycleEditorFragment dialog = new CycleEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CYCLE_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_cycle_editor, null, false);
            final NumberPicker cycleDayPicker = (NumberPicker)view.findViewById(R.id.cycle_day);

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final int cycleDay = editor.getPolicyCycleDay(template);

            cycleDayPicker.setMinValue(1);
            cycleDayPicker.setMaxValue(31);
            cycleDayPicker.setValue(cycleDay);
            cycleDayPicker.setWrapSelectorWheel(true);

            builder.setTitle(R.string.data_usage_cycle_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // clear focus to finish pending text edits
                            cycleDayPicker.clearFocus();
                            final int cycleDay = cycleDayPicker.getValue();
                            final String cycleTimezone = new Time().timezone;
                            if (0 != cycleDay) {
                                editor.setPolicyCycleDay(template, cycleDay, cycleTimezone);
                                target.updatePolicy(true);
                            }
                        }
                    });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
        }

    }

    /**
     * Dialog to edit {@link NetworkPolicy#warningBytes}.
     */
    public static class WarningEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            if (parent.mPolicyEditor == null
                    || TAB_WIFI.equals(parent.mCurrentTab)
                    || parent.mPolicyEditor.getPolicy(parent.mTemplate) == null) {
                SLog.i("WarningEditorFragment " + parent.mCurrentTab);
                setPopupdisable(false);
                return;
            }

            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final WarningEditorFragment dialog = new WarningEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_WARNING_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_bytes_editor, null, false);
            final NumberPicker bytesPicker = (NumberPicker)view.findViewById(R.id.bytes);
            TextView megaShortView = (TextView)view.findViewById(R.id.megabyteShort);
            if (megaShortView != null) {
            	megaShortView.setText(com.android.internal.R.string.megabyteShort);
            }
            

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final long warningBytes = editor.getPolicyWarningBytes(template);
            final long limitBytes = editor.getPolicyLimitBytes(template);

            bytesPicker.setMinValue(0);
            int value = 0;
            if (limitBytes != LIMIT_DISABLED) {
                value = (int)(limitBytes / MB_IN_BYTES) - 1;
            } else {
                value = 1024000; //1000GB
            }

            if (0 > value) {
                value = 0; // monkey
            }
            bytesPicker.setMaxValue(value);

            bytesPicker.setValue((int)(warningBytes / MB_IN_BYTES));
            bytesPicker.setWrapSelectorWheel(false);

            builder.setTitle(R.string.data_usage_warning_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                if (target.mPolicyEditor == null
                                        || TAB_WIFI.equals(target.mCurrentTab)
                                        || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                                    SLog.i("WarningEditorFragment onclick " + target.mCurrentTab);
                                    return;
                                }
                            }

                            // clear focus to finish pending text edits
                            bytesPicker.clearFocus();

                            final long bytes = bytesPicker.getValue() * MB_IN_BYTES;
                            editor.setPolicyWarningBytes(template, bytes);
                            if (target != null) {
                                target.updatePolicy(false);
                            }
                        }
                    });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            setPopupdisable(false);
        }

        // td140788 : Due to Android Bug
        @Override
        public void onSaveInstanceState(Bundle outState) {

        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#limitBytes}.
     */
    public static class LimitEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            if (parent.mPolicyEditor == null
                    || TAB_WIFI.equals(parent.mCurrentTab)
                    || parent.mPolicyEditor.getPolicy(parent.mTemplate) == null) {
                SLog.i("LimitEditorFragment " + parent.mCurrentTab);
                setPopupdisable(false);
                return;
            }

            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final LimitEditorFragment dialog = new LimitEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_LIMIT_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_bytes_editor, null, false);
            final NumberPicker bytesPicker = (NumberPicker)view.findViewById(R.id.bytes);
            TextView megaShortView = (TextView)view.findViewById(R.id.megabyteShort);
            if (megaShortView != null) {
            	megaShortView.setText(com.android.internal.R.string.megabyteShort);
            }
            

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final long warningBytes = editor.getPolicyWarningBytes(template);
            final long limitBytes = editor.getPolicyLimitBytes(template);

            bytesPicker.setMaxValue(1024000); //1000GB
            if (warningBytes != WARNING_DISABLED && limitBytes > 0) {
                bytesPicker.setMinValue((int)(warningBytes / MB_IN_BYTES) + 1);
            } else {
                if (true == "SKT".equals(Config.getOperator())) {
                    bytesPicker.setMinValue(0);
                } else {
                    bytesPicker.setMinValue(1);
                }
            }
            bytesPicker.setValue((int)(limitBytes / MB_IN_BYTES));
            bytesPicker.setWrapSelectorWheel(false);

            builder.setTitle(R.string.data_usage_limit_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                if (target.mPolicyEditor == null
                                        || TAB_WIFI.equals(target.mCurrentTab)
                                        || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                                    SLog.i("LimitEditorFragment onclick " + target.mCurrentTab);
                                    return;
                                }
                            }

                            // clear focus to finish pending text edits
                            bytesPicker.clearFocus();

                            final long bytes = bytesPicker.getValue() * MB_IN_BYTES;
                            editor.setPolicyLimitBytes(template, bytes);
                            if (target != null) {
                                target.updatePolicy(false);
                            }
                        }
                    });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            setPopupdisable(false);
        }

        // td134866 : Due to Android Bug
        @Override
        public void onSaveInstanceState(Bundle outState) {

        }
    }

    /**
     * Dialog to request user confirmation before disabling data.
     */
    public static class ConfirmDataDisableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmDataDisableFragment dialog = new ConfirmDataDisableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_DISABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
                builder.setTitle(R.string.roaming_reenable_title);
            } else {
                builder.setTitle(R.string.data_usage_enable_mobile);
            }

            int strOk = android.R.string.ok;
            int strCancel = android.R.string.cancel;

            if (true == "SKT".equals(Config.getOperator())) {
                builder.setTitle(R.string.data_network_settings_title);
                builder.setMessage(R.string.sp_data_networks_disable_SKT_NORMAL);
            } else if (true == "LGU".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_network_mode_disabled_desc_lgt_NORMAL);
                strOk = R.string.yes;
                strCancel = R.string.no;
            } else if (true == "KDDI".equals(Config.getOperator())) {
                strOk = R.string.yes;
                strCancel = R.string.no;
                builder.setMessage(R.string.sp_data_enabled_uncheck_NORMAL);
                SLog.i("strCancel = " + strCancel);
            } else if (true == "KT".equals(Config.getOperator())) {
                builder.setTitle(R.string.data_network_settings_title);
                //add the OMA MMS Service spec 1.1.3.
                if (Config.isDataRoaming() == true) {
                    builder.setMessage(R.string.sp_data_disable_msg_mms_disabled_kt);
                } else {
                    builder.setMessage(R.string.sp_data_disable_msg_mms_enabled_kt);
                }
            } else if (true == "DCM".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_DataDisabledPopup_NORMAL);
            } else if (true == "VZW".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_mobile_data_off_VZW_NORMAL);
            } else {
                strOk = R.string.yes;
                strCancel = R.string.no;
                builder.setMessage(R.string.data_usage_disable_mobile);
            }

            builder.setPositiveButton(
                    strOk,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                // TODO: extend to modify policy enabled flag.
                                target.setMobileDataEnabled(false);
                                setPositiveResult(true);
                            }
                        }
                    });
            builder.setNegativeButton(
                    strCancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                // TODO: extend to modify policy enabled flag.
                                setConfirmDialogShowed(false);
                                setPositiveResult(false);
                            }
                        }
                    });
            builder.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                        if (target != null) {
                            // TODO: extend to modify policy enabled flag.
                            setConfirmDialogShowed(false);
                            setPositiveResult(false);
                        }
                    }
                    return false;
                }
            });
            builder.setNegativeButton(strCancel, null);

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            if (target != null) {
                // TODO: extend to modify policy enabled flag.

                aboidBlink = false;

                if (false == isPositiveResult()) {
                    target.setMobileDataEnabled(true);
                } else {
                    target.setMobileDataEnabled(false);
                }
                setConfirmDialogShowed(false);
                setPositiveResult(false);
            }

            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            // [Settings] WBT issue fixed 0517
            if (target != null) {
                target.updateBody();
            }
        }

        //[s][chris.won@lge.com][2012-12-04] Due to Android Bug
        @Override
        public void onSaveInstanceState(Bundle outState) {

        }
        //[e][chris.won@lge.com][2012-12-04] Due to Android Bug

    }

    /**
     * Dialog to request user confirmation before disabling data.
     */
    public static class ConfirmDataEnableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }

            final ConfirmDataEnableFragment dialog = new ConfirmDataEnableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_ENABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
                builder.setTitle(R.string.roaming_reenable_title);
            } else {
                builder.setTitle(R.string.data_usage_enable_mobile);
            }

            int strOk = android.R.string.ok;
            int strCancel = android.R.string.cancel;

            if (true == "SKT".equals(Config.getOperator())) {
                if (Utils.isUI_4_1_model(getActivity())) {
                    builder.setTitle(R.string.data_network_settings_title);
                }
                builder.setMessage(R.string.sp_data_networks_enable_SKT_NORMAL);
            } else if (true == "LGU".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_network_mode_enabled_desc_lgt_NORMAL);
                if (Utils.isUI_4_1_model(getActivity())) {
                    strOk = R.string.yes;
                    strCancel = R.string.no;
                }
            } else if (true == "KDDI".equals(Config.getOperator())) {
                strOk = R.string.yes;
                strCancel = R.string.no;
                SLog.i("strCancel = " + strCancel);
                builder.setMessage(R.string.sp_data_enabled_check_NORMAL);
            } else if (true == "KT".equals(Config.getOperator())) {
                if (Utils.isUI_4_1_model(getActivity())) {
                    builder.setTitle(R.string.data_network_settings_title);
                }
                builder.setMessage(R.string.sp_data_networks_enabled_KT_NORMAL);
            } else if (mIs_Ctc_Cn) {
                builder.setTitle(R.string.roaming);
                builder.setMessage(R.string.sp_mobile_data_enable_ct_roaming_normal);
                strOk = R.string.yes;
                strCancel = R.string.no;
            } else if (true == "DCM".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_DataEnabledPopup_NORMAL);
            }
            builder.setPositiveButton(
                    strOk /*android.R.string.ok*/,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                // TODO: extend to modify policy enabled flag.
                                target.setMobileDataEnabled(true);
                                setPositiveResult(true);
                            }
                        }
                    });
            builder.setNegativeButton(
                    strCancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                            if (target != null) {
                                // TODO: extend to modify policy enabled flag.
                                setConfirmDialogShowed(false);
                                setPositiveResult(false);
                            }
                        }
                    });
            builder.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                        if (target != null) {
                            // TODO: extend to modify policy enabled flag.
                            setConfirmDialogShowed(false);
                            setPositiveResult(false);
                        }
                    }
                    return false;
                }
            });
            builder.setNegativeButton(strCancel, null);

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
            if (target != null) {
                // TODO: extend to modify policy enabled flag.
                aboidBlink = false;
                if (false == isPositiveResult()) {
                    target.setMobileDataEnabled(false);
                } else {
                    target.setMobileDataEnabled(true);
                }
                setConfirmDialogShowed(false);
                setPositiveResult(false);
            }

            super.onDismiss(dialog);
            // [Settings] WBT issue fixed 0517
            if (target != null) {
                target.updateBody();
            }
        }
    }


    /**
     * Dialog to request user confirmation before setting
     * {@link INetworkPolicyManager#setRestrictBackground(boolean)}.
     */
    public static class ConfirmRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmRestrictFragment dialog = new ConfirmRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_restrict_background_title);
            if (Utils.hasMultipleUsers(context)) {
                builder.setMessage(R.string.data_usage_restrict_background_multiuser);
            } else {
                if (false == Utils.isUI_4_1_model(getActivity())) {
                    builder.setMessage(R.string.data_usage_restrict_background);
                } else {
                    builder.setMessage(R.string.data_usage_restrict_background_ui4_1);
                }
            }
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                    if (target != null) {
                        target.setRestrictBackground(true);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }

            return builder.create();
        }
    }

    /**
     * Dialog to inform user that {@link #POLICY_REJECT_METERED_BACKGROUND}
     * change has been denied, usually based on
     * {@link DataUsageSummary#hasLimitedNetworks()}.
     */
    public static class DeniedRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final DeniedRestrictFragment dialog = new DeniedRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_DENIED_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_app_restrict_background);
            builder.setMessage(R.string.data_usage_restrict_denied_dialog);
            builder.setPositiveButton(android.R.string.ok, null);
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }

            return builder.create();
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link #POLICY_REJECT_METERED_BACKGROUND}.
     */
    public static class ConfirmAppRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmAppRestrictFragment dialog = new ConfirmAppRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_APP_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_app_restrict_dialog_title);
            builder.setMessage(R.string.data_usage_app_restrict_dialog);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();
                    if (target != null) {
                        target.setAppRestrictBackground(true);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }


    /**
     * Compute default tab that should be selected, based on
     * {@link NetworkPolicyManager#EXTRA_NETWORK_TEMPLATE} extra.
     */
    private static String computeTabFromIntent(Intent intent) {
        final NetworkTemplate template = intent.getParcelableExtra(EXTRA_NETWORK_TEMPLATE);
        if (template == null) {
            return null;
        }
        switch (template.getMatchRule()) {
        case MATCH_MOBILE_3G_LOWER:
            return TAB_3G;
        case MATCH_MOBILE_4G:
            return TAB_4G;
        case MATCH_MOBILE_ALL:
            return TAB_MOBILE;
        case MATCH_WIFI:
            return TAB_WIFI;
        default:
            return null;
        }
    }

    /**
     * Test if device has a mobile data radio with SIM in ready state.
     */
    public static boolean hasReadyMobileRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("mobile");
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        final TelephonyManager tele = TelephonyManager.from(context);

        // require both supported network and ready SIM
        //[chris.won@lge.com][2012-12-18] get Radio state for CDMA models
        if ("SPR".equals(Config.getOperator()) || Config.MPCS.equals(Config.getOperator())) {
            return conn.isNetworkSupported(TYPE_MOBILE);
        } else if (tele.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
                && tele.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_FALSE) {
            return conn.isNetworkSupported(TYPE_MOBILE);
        } else {
            return conn.isNetworkSupported(TYPE_MOBILE) && tele.getSimState() == SIM_STATE_READY;
        }
    }

    /**
     * Test if device has a mobile 4G data radio.
     */
    public static boolean hasReadyMobile4gRadio(Context context) {
        if (!NetworkPolicyEditor.ENABLE_SPLIT_POLICIES) {
            return false;
        }
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("4g");
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        final TelephonyManager tele = TelephonyManager.from(context);

        final boolean hasWimax = conn.isNetworkSupported(TYPE_WIMAX);
        final boolean hasLte = (tele.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE)
                && hasReadyMobileRadio(context);
        return hasWimax || hasLte;
    }

    /**
     * Test if device has a Wi-Fi data radio.
     */
    public static boolean hasWifiRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("wifi");
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        return conn.isNetworkSupported(TYPE_WIFI);
    }

    /**
     * Test if device has an ethernet network connection.
     */
    public boolean hasEthernet(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("ethernet");
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        final boolean hasEthernet = conn.isNetworkSupported(TYPE_ETHERNET);

        final long ethernetBytes;
        if (mStatsSession != null) {
            try {
                ethernetBytes = mStatsSession.getSummaryForNetwork(
                        NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE)
                        .getTotalBytes();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            ethernetBytes = 0;
        }

        // only show ethernet when both hardware present and traffic has occurred
        return hasEthernet && ethernetBytes > 0;
    }

    /**
     * Inflate a {@link Preference} style layout, adding the given {@link View}
     * widget into {@link android.R.id#widget_frame}.
     */
    private static View inflatePreference(LayoutInflater inflater, ViewGroup root, View widget) {
        final View view = inflater.inflate(R.layout.preference, root, false);
        final LinearLayout widgetFrame = (LinearLayout)view.findViewById(
                android.R.id.widget_frame);
        widgetFrame.addView(widget, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return view;
    }

    private static View inflatePreference_data_sleep_switch(LayoutInflater inflater,
            ViewGroup root, View widget) {
        final View view = inflater.inflate(R.layout.preference_datasleep_switch, root, false);
        final LinearLayout widgetFrame = (LinearLayout)view.findViewById(
                android.R.id.widget_frame);
        widgetFrame.addView(widget, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return view;
    }

    /**
     * Test if any networks are currently limited.
     */
    private boolean hasLimitedNetworks() {
        return !buildLimitedNetworksList().isEmpty();
    }

    /**
     * Build string describing currently limited networks, which defines when
     * background data is restricted.
     */
    @Deprecated
    private CharSequence buildLimitedNetworksString() {
        final List<CharSequence> limited = buildLimitedNetworksList();

        // handle case where no networks limited
        if (limited.isEmpty()) {
            limited.add(getText(R.string.data_usage_list_none));
        }

        return TextUtils.join(limited);
    }

    /**
     * Build list of currently limited networks, which defines when background
     * data is restricted.
     */
    @Deprecated
    private List<CharSequence> buildLimitedNetworksList() {
        final Context context = getActivity();

        // build combined list of all limited networks
        final ArrayList<CharSequence> limited = Lists.newArrayList();

        final TelephonyManager tele = TelephonyManager.from(context);
        if (tele.getSimState() == SIM_STATE_READY) {
            final String subscriberId = getActiveSubscriberId(context);
            if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobileAll(subscriberId))) {
                limited.add(getText(R.string.data_usage_list_mobile));
            }
            if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobile3gLower(subscriberId))) {
                limited.add(getText(R.string.data_usage_tab_3g));
            }
            if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobile4g(subscriberId))) {
                limited.add(getText(R.string.data_usage_tab_4g));
            }
        }

        if (mPolicyEditor.hasLimitedPolicy(buildTemplateWifiWildcard())) {
            limited.add(getText(R.string.data_usage_tab_wifi));
        }
        if (mPolicyEditor.hasLimitedPolicy(buildTemplateEthernet())) {
            limited.add(getText(R.string.data_usage_tab_ethernet));
        }

        return limited;
    }

    /**
     * Inset both selector and divider {@link Drawable} on the given
     * {@link ListView} by the requested dimensions.
     */
    private static void insetListViewDrawables(ListView view, int insetSide) {
        final Drawable selector = view.getSelector();
        final Drawable divider = view.getDivider();

        // fully unregister these drawables so callbacks can be maintained after
        // wrapping below.
        final Drawable stub = new ColorDrawable(Color.TRANSPARENT);
        view.setSelector(stub);
        view.setDivider(stub);

        view.setSelector(new InsetBoundsDrawable(selector, insetSide));
        view.setDivider(new InsetBoundsDrawable(divider, insetSide));
    }

    /**
     * Set {@link android.R.id#title} for a preference view inflated with
     * {@link #inflatePreference(LayoutInflater, ViewGroup, View)}.
     */
    private static void setPreferenceTitle(View parent, int resId) {
        if (null == parent) {
            return;
        }

        final TextView title = (TextView)parent.findViewById(android.R.id.title);
        //[s][chris.won@lge.com][2012-11-30] Applying bold text
        title.setTypeface(title.getTypeface(), mTitleStyle);
        //[e][chris.won@lge.com][2012-11-30] Applying bold text

        title.setText(resId);
    }

    /**
     * Set {@link android.R.id#summary} for a preference view inflated with
     * {@link #inflatePreference(LayoutInflater, ViewGroup, View)}.
     */
    private static void setPreferenceSummary(View parent, CharSequence string) {
        if (null == parent) {
            return;
        }
        final TextView summary = (TextView)parent.findViewById(android.R.id.summary);
        summary.setVisibility(View.VISIBLE);
        summary.setText(string);
    }

    private static boolean contains(int[] haystack, int needle) {
        for (int value : haystack) {
            if (value == needle) {
                return true;
            }
        }
        return false;
    }

    // kerry
    public static boolean isAirplaneModeOn(Context context) {
        if (null == context) {
            return false;
        }
        Log.d("Airplanemodeenabler", "isAirplaneModeOn");

        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    // kerry - to sync with quick settings start
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            // LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
            // if (action.equals("android.skt.intent.action.USER_BACKG_SETTING")) 
            if (action.equals(sSktUserBackgSettingIntent)
                    || action.equals(sSktUserBackgSettingIntentOld))
            {
                boolean enabled = intent.getBooleanExtra("on_off", true);
                boolean getBackground = mPolicyManager.getRestrictBackground();

                Log.d(TAG, " [LGE_DATA] USER_BACKG_SETTING :: " + enabled);
                Log.d(TAG, " [LGE_DATA] getBackground :: " + getBackground);

                if (getBackground != enabled) {
                    handleSKT_setRestrictBackground(enabled);
                }
            }
            // LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                boolean enabled = intent.getBooleanExtra("state", false);
                Log.i(TAG, "mReceiver ACTION_AIRPLANE_MODE_CHANGED  state = " + enabled);
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-16, LU6800 TD#118124 
                if (true == "KR".equals(Config.getCountry())
                        || true == "VZW".equals(Config.getOperator())) {
                    mDataEnabled.setEnabled(!enabled);
                    if (true == "VZW".equals(Config.getOperator())) {
                        mDataEnabledView.setEnabled(!enabled);
                    }
                    sDisableAtLimit.setEnabled(!enabled);
                    mDisableAtLimitView.setEnabled(!enabled);
                    sAlertAtWarning.setEnabled(!enabled);
                    mAlertAtWarningView.setEnabled(!enabled);
                    if (null != mTrafficOptimizeView) {
                        mTrafficOptimizeView.setEnabled(!enabled);
                    }
                    if (sTrafiicOptimizeCheckBox != null) {
                        sTrafiicOptimizeCheckBox.setEnabled(!enabled);
                    }
                    mNetworkSwitches.setEnabled(!enabled);
                } else {
                    mDataEnabled.setEnabled(!enabled);
                    mDataEnabledView.setEnabled(!enabled);
                    sDisableAtLimit.setEnabled(!enabled);
                    mDisableAtLimitView.setEnabled(!enabled);
                    mNetworkSwitches.setEnabled(!enabled);
                }
                if (null != mDataSleepEnabled && null != mDataSleepEnabledView) {
                    mDataSleepEnabled.setEnabled(!enabled);
                    mDataSleepEnabledView.setEnabled(!enabled);
                }
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 
                if (true == "KR".equals(Config.getCountry())
                        || true == "VZW".equals(Config.getOperator())) {
                    if (true == enabled) {
                        TextView ev = (TextView)mDataSleepEnabledView
                                .findViewById(android.R.id.title);
                        ev.setTextColor(Color.GRAY);
                        TextView lv = (TextView)mDisableAtLimitView
                                .findViewById(android.R.id.title);
                        lv.setTextColor(Color.GRAY);
                        TextView lvw = (TextView)mAlertAtWarningView
                                .findViewById(android.R.id.title);
                        lvw.setTextColor(Color.GRAY);
                        if (true == "VZW".equals(Config.getOperator())) {
                            TextView ev2 = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                            ev2.setTextColor(Color.GRAY);
                        }
                    } else {
                        final Resources res = getResources();
                        TextView ev = (TextView)mDataSleepEnabledView
                                .findViewById(android.R.id.title);
                        ev.setTextColor(res.getColor(R.color.graph_text_color));
                        TextView lv = (TextView)mDisableAtLimitView
                                .findViewById(android.R.id.title);
                        lv.setTextColor(res.getColor(R.color.graph_text_color));
                        TextView lvw = (TextView)mAlertAtWarningView
                                .findViewById(android.R.id.title);
                        lvw.setTextColor(res.getColor(R.color.graph_text_color));
                        if (true == "VZW".equals(Config.getOperator())) {
                            TextView ev2 = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                            ev2.setTextColor(res.getColor(R.color.graph_text_color));
                        }
                    }
                } else {
                    if (true == enabled) {
                        TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                        ev.setTextColor(Color.GRAY);
                        TextView lv = (TextView)mDisableAtLimitView
                                .findViewById(android.R.id.title);
                        lv.setTextColor(Color.GRAY);
                    } else {
                        final Resources res = getResources();
                        TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                        ev.setTextColor(res.getColor(R.color.graph_text_color));
                        TextView lv = (TextView)mDisableAtLimitView
                                .findViewById(android.R.id.title);
                        lv.setTextColor(res.getColor(R.color.graph_text_color));
                    }
                }

                updatePolicy(false);

                setConfirmDialogShowed(false);
            }
            //If in callings, DataEnabled preperence is dimmed
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                if ("DCM".equals(Config.getOperator()) || "VZW".equals(Config.getOperator())) {
                    SLog.i("ACTION_PHONE_STATE_CHANGED :: DCM / VZW ");
                    if ((mTelephonyManager != null)
                            && false == isAirplaneModeOn(getActivity())
                            && (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE)) {
                        SLog.i("ACTION_PHONE_STATE_CHANGED :: tm.getCallState() = "
                                + mTelephonyManager.getCallState());
                        if (mDataEnabled != null && mDataEnabledView != null) {
                            mDataEnabled.setEnabled(true);
                            if ("DCM".equals(Config.getOperator())) {
                                mDataEnabledView.setEnabled(true);
                                TextView ev = (TextView)mDataEnabledView
                                        .findViewById(android.R.id.title);
                                ev.setTextColor(Color.BLACK);
                                updatePolicy(false);
                            }
                        }
                    }
                }
            }
            if (ACTION_TRAFFIC_SERVICE_STATE_CHANGED.equals(action) && Config.SKT.equals(Config.getOperator())) {
                Log.i(TAG, "ACTION_TRAFFIC_SERVICE_STATE_CHANGED IN");
                setCheckedTrafficServiceState();
            }
        }
    };

    // kerry - to sync with quick settings end
    private static void setPopupdisable(boolean popupdisable) {
        mPopupdisable = popupdisable;
    }

    private static boolean getPopupdisable() {
        return mPopupdisable;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveDataUsageSettingPolicyChangeIntent(
                        intent)) {
                    updatePolicy(false);
                    if (mMenuDataRoaming != null || mDataEnabled != null) {
                        getActivity().finish();
                    }
                }
            }
        }
    };

    // LGMDM_END

    public static class ConfirmLGURestrictDisableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }

            final ConfirmLGURestrictDisableFragment dialog = new ConfirmLGURestrictDisableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_RESTRICT_LGU_ENABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_menu_restrict_background)
                    .setMessage(R.string.sp_data_roaming_background_restrict_uncheck_lgt_popup)
                    .setPositiveButton(
                            R.string.sp_data_roaming_background_data_popup_button_text_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

                                    Toast.makeText(
                                            getActivity(),
                                            R.string.sp_data_roaming_background_restrict_uncheck_lgt_Toast_popup,
                                            Toast.LENGTH_LONG).show();
                                    target.setRestrictBackground(false);
                                }
                            })
                    .setNegativeButton(
                            R.string.sp_data_roaming_background_data_popup_button_text_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

                                    target.setRestrictBackground(true);
                                }
                            });
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return builder.create();
        }
    }

    public static class ConfirmLGURestrictEnableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmLGURestrictEnableFragment dialog = new ConfirmLGURestrictEnableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_RESTRICT_LGU_DISABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_menu_restrict_background)
                    .setMessage(R.string.sp_data_roaming_background_restrict_check_lgt_popup)
                    .setNegativeButton(
                            R.string.sp_data_roaming_background_data_popup_button_text_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

                                    target.setRestrictBackground(false);
                                }
                            })
                    .setPositiveButton(
                            R.string.sp_data_roaming_background_data_popup_button_text_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

                                    Toast.makeText(
                                            getActivity(),
                                            R.string.sp_data_roaming_background_data_limit_lgt_Toast_popup,
                                            Toast.LENGTH_LONG).show();
                                    target.setRestrictBackground(true);
                                }
                            });
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return builder.create();
        }
    }

    private static class ConfirmTrafficServiceEnableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmTrafficServiceEnableFragment dialog = new ConfirmTrafficServiceEnableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_TRAFFIC_STATE_ENABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.sp_data_usage_background_optimization)
            .setMessage(R.string.sp_data_usage_background_optimization_on_popup)
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (target != null) {
                        target.setCheckedTrafficServiceState();
                    }
                }
            }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (target != null) {
                        target.setCheckedTrafficServiceState();
                        target.sendSendBroadcastTrafficServiceStateSave(true);
                    }
                }
            });
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return builder.create();
        }
    }

    private static class ConfirmTrafficServiceDisableFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmTrafficServiceDisableFragment dialog = new ConfirmTrafficServiceDisableFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_TRAFFIC_STATE_DISABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary)getTargetFragment();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.sp_data_usage_background_optimization)
            .setMessage(R.string.sp_data_usage_background_optimization_off_popup)
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (target != null) {
                        target.setCheckedTrafficServiceState();
                    }
                }
            }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (target != null) {
                        target.setCheckedTrafficServiceState();
                        target.sendSendBroadcastTrafficServiceStateSave(false);
                    }
                }
            });
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return builder.create();
        }
    }

    //[s][chris.won@lge.com][2013-03-18] Chamelion
    private boolean checkChamelionRoamingDisable() {
        int Chamelion_display = 1;
        final String Operation_Alpha_Sprint = "Sprint";
        final String Operation_Alpha_Boost = "Boost Mobile";
        final String Operation_Alpha_Virgin = "Virgin Mobile";

        boolean bHAF_activation = false;
        boolean bPayload_data = false;
        String operator_numeric = SystemProperties.get("ro.cdma.home.operator.numeric");
        String operator_alpha = SystemProperties.get("ro.cdma.home.operator.alpha");
        Log.d(TAG, "operator_numeric = " + operator_numeric);
        Log.d(TAG, "operator_alpha = " + operator_alpha);
        String phoneActivated = SystemProperties.get("ro.sprint.hfa.flag");
        if (phoneActivated.equals("activationOK")) { // HFA Yes
            Log.d(TAG, "HFA yes");
            bHAF_activation = true;
            String payloadReceived = SystemProperties.get("ro.sprint.chameleon.flag");
            if (payloadReceived.equals("payloadOK")) { // Payload Data yes //Fix
                Log.d(TAG, "Payload Data yes");
                bPayload_data = true;
            } else { // Payload Data NO
                Log.d(TAG, "Payload Data No");
                bPayload_data = false;
            }

        } else { // HFA No
            Log.d(TAG, "HFA  No");
            bHAF_activation = false;
        }
        Log.d(TAG, "bHAF_activation = " + bHAF_activation);
        Log.d(TAG, "bPayload_data = " + bPayload_data);

        if (bHAF_activation) {
            if (bPayload_data) {
                if (Operation_Alpha_Boost.equalsIgnoreCase(operator_alpha)
                        || Operation_Alpha_Virgin.equalsIgnoreCase(operator_alpha)) {
                    Chamelion_display = Settings.System.getInt(sContext.getContentResolver(),
                            SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 0);
                    Log.d(TAG, "BM VG Chamelion_display = " + Chamelion_display);
                } else if (Operation_Alpha_Sprint.equalsIgnoreCase(operator_alpha)) {
                    Chamelion_display = Settings.System.getInt(sContext.getContentResolver(),
                            SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 1);
                    Log.d(TAG, "SPR Chamelion_display = " + Chamelion_display);
                } else {
                    Chamelion_display = Settings.System.getInt(sContext.getContentResolver(),
                            SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 1); // Fix
                    Log.d(TAG, "Chamelion_display : 1 (other)");
                }

            } else {
                if (Operation_Alpha_Boost.equalsIgnoreCase(operator_alpha)
                        || Operation_Alpha_Virgin.equalsIgnoreCase(operator_alpha)) {
                    Chamelion_display = 0;
                    Log.d(TAG, "BM VG Chamelion_display = " + Chamelion_display);
                } else if (Operation_Alpha_Sprint.equalsIgnoreCase(operator_alpha)) {
                    Chamelion_display = 1;
                    Log.d(TAG, "SPR Chamelion_display = " + Chamelion_display);
                } else {
                    Chamelion_display = 1;
                    Log.d(TAG, "Chamelion_display : 1 (other)");
                }
            }
        } else {
            Chamelion_display = 1;
        }

        return Chamelion_display == 0 ? true : false;
    }

    //[e][chris.won@lge.com][2013-03-18] Chamelion

    // add the "KT" Requirement.
    private void setDB_LteRoaming(boolean lte_roaming) {
        Log.d(TAG, "[Set lte_roaming DB]lte_roaming = " + lte_roaming);
        Settings.Secure.putInt(sContext.getContentResolver(), "lte_roaming", lte_roaming ? 1 : 0);
    }

    public void changePreferrredNetworkMode(boolean enabled) {
        int m_NT_MODE_INVALID = -1;
        int newPreferMode = m_NT_MODE_INVALID;
        int curPreferMode = Settings.Global.getInt(sContext.getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE,
                0);

        Log.d(TAG, "[changePreferrredNetworkMode] enabled = " + enabled);
        switch (curPreferMode) {
        case Phone.NT_MODE_WCDMA_PREF:
        case Phone.NT_MODE_GSM_UMTS:
        case Phone.NT_MODE_LTE_GSM_WCDMA:
            newPreferMode = enabled ? Phone.NT_MODE_LTE_GSM_WCDMA : Phone.NT_MODE_WCDMA_PREF;
            break;
        case Phone.NT_MODE_WCDMA_ONLY:
            break;
        case Phone.NT_MODE_LTE_WCDMA:
            newPreferMode = enabled ? Phone.NT_MODE_LTE_WCDMA : Phone.NT_MODE_WCDMA_ONLY;
            break;
        default:
            break;
        }
        Log.d(TAG, "[changePreferrredNetworkMode] curPreferMode = " + curPreferMode);
        Log.d(TAG, "[changePreferrredNetworkMode] newPreferMode = " + newPreferMode);

        if ((newPreferMode != m_NT_MODE_INVALID) && (newPreferMode != curPreferMode)) {
            Settings.Global.putInt(sContext.getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE,
                    newPreferMode);
        }
        Intent intent = new Intent();
        intent.putExtra("is_roaming_onoff", enabled);
        intent.putExtra("change_networkmode", newPreferMode);
        intent.setAction("com.lge.settings.action.CHANGE_ROAMING_MODE");
        sContext.sendBroadcast(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        setPaddingSwitch(getActivity(), mDataEnabled, newConfig.orientation);
    }

    public void setPaddingSwitch(Activity mActivity, Switch mBtn, int orientation) {
        if (null == mBtn || null == mBtn || null == mActivity
             || true != ("KR".equals(Config.getCountry()))) {
            return;
        }
        if (mActivity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity)mActivity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                int mGravity = Gravity.CENTER_VERTICAL | Gravity.END;
                if ("true".equals(
                        ModelFeatureUtils.getFeature(getActivity(), "actionbardatausagepadding"))) {
                    SLog.i("ModelFeatureUtils setPaddingSwitch " + orientation);
                    boolean mLand = (orientation == Configuration.ORIENTATION_LANDSCAPE);
                    int mDimension =
                            mLand ?
                                    R.dimen.action_bar_datausage_padding_land :
                                    R.dimen.action_bar_datausage_padding_port;
                    int mPadding = mActivity.getResources().getDimensionPixelSize(mDimension);
                    SLog.i("ModelFeatureUtils mPadding " + mPadding);
                    mBtn.setPaddingRelative(mPadding, 0, 0, 0);
                    mGravity = Gravity.CENTER_VERTICAL | Gravity.START;
                } else {
                    final int padding = mActivity.getResources().getDimensionPixelSize(
                            R.dimen.action_bar_switch_padding);
                    mBtn.setPaddingRelative(0, 0, padding, 0);
                }
                SLog.i("insert the data enable btn in actionbar");
                mActivity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                mActivity.getActionBar().setCustomView(mBtn,
                        new ActionBar.LayoutParams(
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                mGravity));
            } else if (SettingsBreadCrumb.isAttached(getActivity()) == false
                    && "VZW".equals(Config.getOperator())) {
                SLog.i("mDataEnabled = preferenceActivity");
                if (Utils.isTablet()) {
                    mActivity.getActionBar().setTitle(R.string.data_usage_summary_title);
                } else {
                    mActivity.getActionBar().setTitle(R.string.data_usage_enable_mobile);
                }
            }
        }
    }

    private boolean isCurrentWiFiTab() {
        SLog.i("isCurrentWiFiTab mCurrentTab =  " + mCurrentTab);
        return mPolicyEditor == null || TAB_WIFI.equals(mCurrentTab) || mPolicyEditor.getPolicy(mTemplate) == null;
    }

    private static String formatFileSizeForUnit(Context context, long number, boolean unit_type) {
        if (context == null) {
            return "";
        }
        final int mDISPLAY_UNIT_KB_CNT = 0x1;
        final int mDISPLAY_UNIT_MB_CNT = 0x2;
        final int mDISPLAY_UNIT_GB_CNT = 0x3;
        float result = number;
        int mUnitCount = 0;
        boolean mIsSmallValue = false;
        int suffix = com.android.internal.R.string.byteShort;
        if (result > 900) {
            suffix = com.android.internal.R.string.kilobyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (result > 900) {
            suffix = com.android.internal.R.string.megabyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (result > 900
                && true == unit_type) {
            suffix = com.android.internal.R.string.gigabyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (unit_type) {
            // unit_type = true  : GB
            if (mUnitCount < mDISPLAY_UNIT_GB_CNT) {
                suffix = com.android.internal.R.string.gigabyteShort;
                if (mUnitCount <= mDISPLAY_UNIT_MB_CNT) {
                    if (mUnitCount == mDISPLAY_UNIT_MB_CNT) {
                        result = result / 1024;
                        if (0 < result
                                && result < 0.01) {
                            mIsSmallValue = true;
                        }
                    } else {
                        if (0 < result) {
                            mIsSmallValue = true;
                        }
                    }
                }
            }
        } else {
            // unit_type = false : MB
            if (mUnitCount < mDISPLAY_UNIT_MB_CNT) {
                suffix = com.android.internal.R.string.megabyteShort;
                if (mUnitCount <= mDISPLAY_UNIT_KB_CNT) {
                    if (mUnitCount == mDISPLAY_UNIT_KB_CNT) {
                        result = result / 1024;
                        if (0 < result
                                && result < 0.01) {
                            mIsSmallValue = true;
                        }
                    } else {
                        if (0 < result) {
                            mIsSmallValue = true;
                        }
                    }
                }
            }
        }
        String value;
        if (mIsSmallValue) {
            float temp = 0.01f;
            value = "< " + String.format("%.2f", temp);
        } else if (result == 0) {
            int temp = 0;
            value = String.format("%d", temp);
        } else if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.2f", result);
        } else if (result < 100) {
            value = String.format("%.2f", result);
        } else {
            value = String.format("%.0f", result);
        }

        return context.getResources().
                getString(com.android.internal.R.string.fileSizeSuffix,
                        value, context.getString(suffix));
    }

    String getDataSleepSummaryString() {
        if (false == sIsForg) {
            SLog.i("getDataSleepSummaryString skip..");
            return " ";
        }
        mDataSleepInfo.initDataSleepTime();
        //String mDaysValue = mDataSleepInfo.getDBDataSleepDays();
        //String mDaysSummary = mDataSleepInfo.getDataSleepSummaryText(getActivity(), mDaysValue);
        //String mDaysFormatter = mDataSleepInfo.getDataSleepOnSummary(getActivity(), mDaysSummary);
        String mDaysFormatter = mDataSleepInfo.getDataSleepOnSummary(getActivity());
        //SLog.i("DataSleepSummaryString mDaysValue = " + mDaysValue);
        //SLog.i("DataSleepSummaryString mDaysSummary = " + mDaysSummary);
        SLog.i("DataSleepSummaryString mDaysFormatter = " + mDaysFormatter);
        return mDaysFormatter;
    }

    private void setTabWidgetSelectedBoldType() {
        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            TextView tv = (TextView)mTabHost.getTabWidget()
                        .getChildAt(i).findViewById(android.R.id.title);
            if (i == mTabHost.getCurrentTab()) {
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void addSwitchTablet() {
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mDataEnabled);
            }
        }
    }
    private void initTrafficOptimizeViewSKTOnlay(LayoutInflater inflater) {
        if (Config.SKT.equals(Config.getOperator())
                && SystemProperties.get("ro.lge.skt_background_call_opt").equals("true")) {
            if (isSupportTrafficOptimezeCheckBox()) {
                sTrafiicOptimizeCheckBox = new CheckBox(inflater.getContext());
                sTrafiicOptimizeCheckBox.setClickable(false);
                sTrafiicOptimizeCheckBox.setFocusable(false);
                mTrafficOptimizeView = inflatePreference(inflater, mNetworkSwitches, sTrafiicOptimizeCheckBox);
                mTrafficOptimizeView.setFocusable(true);
                mTrafficOptimizeView.setClickable(true);
                mTrafficOptimizeView.setOnClickListener(mTrafficOptimizeSupportCheckBoxListener);
                mNetworkSwitches.addView(mTrafficOptimizeView);
            } else {
                mTrafficOptimize = new TextView(inflater.getContext());
                mTrafficOptimizeView = inflatePreference(inflater,
                        mNetworkSwitches,
                        mTrafficOptimize);
                mTrafficOptimizeView.setFocusable(true);
                mTrafficOptimizeView.setClickable(true);
                mTrafficOptimizeView.setOnClickListener(mTrafficOptimizationListener);
                mNetworkSwitches.addView(mTrafficOptimizeView);
            }
        }
    }

    private int getTrafficServiceState() {
        Log.i(TAG, "getTrafficServiceState IN");
        ContentResolver resolver = sContext.getContentResolver();
        int serviceState = TRAFFIC_STATUS_ENABLED;
        try {
            serviceState = Settings.System.getInt(resolver, TRAFFIC_SERVICE_STATE);
        } catch (SettingNotFoundException e) {
            int setupWizardResult = Settings.System.getInt(resolver, TRAFFIC_SERVICE_STATE_SUB, TRAFFIC_STATUS_DISABLED);
            Log.i(TAG, "getTrafficServiceState setupWizardResult = " + setupWizardResult);
            if (setupWizardResult == TRAFFIC_STATUS_DISABLED) {
                serviceState = TRAFFIC_STATUS_ENABLED;
            } else {
                serviceState = TRAFFIC_STATUS_DISABLED;
            }
        }
        Log.i(TAG, "getTrafficServiceState = " + serviceState);

        return serviceState;
    }

    private void sendSendBroadcastTrafficServiceStateSave(boolean onoff) {
        Log.i(TAG, "sendSendBroadcastTrafficServiceStateSave IN onoff = " + onoff);
        Intent intent = new Intent(ACTION_TRAFFIC_SERVICE_STATE_CHANGING);
        intent.putExtra("extra.IS_SERVICE_ON", onoff);
        sContext.sendBroadcast(intent);
    }

    private void setCheckedTrafficServiceState() {
        Log.i(TAG, "setCheckedTrafficServiceState IN");
        int serviceState = getTrafficServiceState();
        TextView lv = (TextView)mTrafficOptimizeView.findViewById(android.R.id.title);

        switch (serviceState) {
        case TRAFFIC_STATUS_DISABLING:
        case TRAFFIC_STATUS_ENABLING:
            sTrafiicOptimizeCheckBox.setEnabled(false);
            mTrafficOptimizeView.setEnabled(false);
            lv.setTextColor(Color.GRAY);
            break;
        case TRAFFIC_STATUS_DISABLED:
            sTrafiicOptimizeCheckBox.setEnabled(true);
            sTrafiicOptimizeCheckBox.setChecked(false);
            mTrafficOptimizeView.setEnabled(true);
            lv.setTextColor(Color.BLACK);
            break;
        case TRAFFIC_STATUS_ENABLED:
            sTrafiicOptimizeCheckBox.setEnabled(true);
            sTrafiicOptimizeCheckBox.setChecked(true);
            mTrafficOptimizeView.setEnabled(true);
            lv.setTextColor(Color.BLACK);
            break;
        default:
            mTrafficOptimizeView.setEnabled(true);
            break;
        }
    }

    private boolean getDomesticMobileData() {
        boolean enabled = Settings.Global.getInt(sContext.getContentResolver(), "domestic_mobile_data", 1) == 1;
        SLog.i("Data Usage getDomesticMobileData = " + enabled);
        return enabled;
    }

    private void setDomesticMobileData(boolean isEnabled) {
        SLog.i("Data Usage setDomesticMobileData = " + isEnabled);
        Settings.Global.putInt(sContext.getContentResolver(), "domestic_mobile_data", isEnabled ? 1 : 0);
    }

    private void showSupportPCO_Dialog() {
        final Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context,
                com.lge.R.style.Theme_LGE_White_Dialog_Alert);

        builder.setTitle(R.string.pco_dialog_title);

        // C-1 content
        builder.setMessage(R.string.pco_dialog_message);

        // B-3 button
        builder.setPositiveButton(R.string.def_yes_btn_caption,
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setMobileDataEnabled(true);
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings",
                                "com.android.settings.VerizonOnlinePortal");
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton(R.string.user_setup_button_setup_later,
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "pressed setNegativeButton");
                        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);
                        if (pco_ims == 5) {
                            mTelephonyManager.setRadioPower(false);
                            Settings.Secure.putInt(sContext.getContentResolver(),
                                        "radio_off_by_pco5", 1);
                        }
                    }
                });
        builder.setOnCancelListener(new Dialog.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "pressed cancel");
            }
        });

        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    private boolean IsSupportPCO() {
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);
        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);

        if (pco_internet == 3 || pco_ims == 5) {
            return true;
        } else {
            return false;
        }
    }
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            String title = "";
            if ("SKT".equals(Config.getOperator())
                    || "KT".equals(Config.getOperator())) {
                title = context.getString(R.string.data_network_settings_title);
            } else if ("ATT".equals(Config.getOperator())) {
                 title = context.getString(R.string.shortcut_datausage_att);
            } else if ("VZW".equals(Config.getOperator())) {
                title = context.getString(R.string.data_usage_summary_title);
            } else {
                title = context.getString(R.string.data_usage_enable_mobile);
            }
            
            setSearchIndexData(context, "datausage_settings",
                    title, "main",null, 
                    null, "com.lge.helpcenter.action.DATA_USAGE_SETTINGS", null, 
                    null, 1, 
                    null, null, 
                    null, 1, 0);
            
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
