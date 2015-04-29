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
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.android.internal.util.Preconditions.checkNotNull;
import static com.android.settings.Utils.prepareCustomPreferencesList;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsServiceEx;
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
import android.os.Handler;
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
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import android.telephony.SubscriptionManager;

import com.android.settings.AppViewHolder; // kiseok.son 20130109 List menu speed up.
import com.android.settings.Utils;
import com.android.settings.drawable.InsetBoundsDrawable;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgmdm.MDMDataUsageAdapter;
import com.android.settings.net.ChartData;
import com.android.settings.net.ChartDataLoaderMultiSIM;
import com.android.settings.net.DataUsageMeteredSettings;
import com.android.settings.net.NetworkPolicyEditor;
import com.android.settings.net.SummaryForAllUidLoader;
import com.android.settings.net.UidDetail;
import com.android.settings.net.UidDetailProvider;
import com.android.settings.widget.ChartDataUsageView;
import com.android.settings.widget.ChartDataUsageView.DataUsageChartListener;
import com.android.settings.widget.PieChartView;
import com.google.android.collect.Lists;
import android.content.ComponentName;
import com.android.settings.utils.LGSubscriptionManager;

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

// LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
import com.lge.constants.SettingsConstants;

// LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

/**
 * Panel showing data usage history across various networks, including options
 * to inspect based on usage cycle and control through {@link NetworkPolicy}.
 */
public class DataUsageSummaryMultiSIM extends Fragment {
    private static final String TAG = "DataUsageSummaryMultiSIM";
    private static final boolean LOGD = true;

    // TODO: remove this testing code
    private static final boolean TEST_ANIM = false;
    private static final boolean TEST_RADIOS = false;

    //private MessageAndListViewHolder mMsgAndListView; //DataSwitcher pop-up for TLF_ES
    private static final String TEST_RADIOS_PROP = "test.radios";
    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";

    private static final String TAB_3G = "3g";
    private static final String TAB_4G = "4g";
    private static final String TAB_MOBILE = "mobile";
    private static final String TAB_WIFI = "wifi";
    private static final String TAB_ETHERNET = "ethernet";
    private static final String TAB_SIM_1 = "sim1";
    private static final String TAB_SIM_2 = "sim2";
    private static final String TAB_SIM_3 = "sim3";

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
    private static final String TAG_CONFIRM_DATA_USAGE_RESET = "confirmDataUsageReset";

    private static final int LOADER_CHART_DATA = 2;
    private static final int LOADER_SUMMARY = 3;
    //private static final int R.id.data_usage_reset_menu = 1234; 

    private INetworkManagementService mNetworkService;
    private INetworkStatsService mStatsService;
    private INetworkStatsServiceEx mStatsServiceEx;
    private NetworkPolicyManager mPolicyManager;
    private TelephonyManager mTelephonyManager;

    private INetworkStatsSession mStatsSession;

    private static final String PREF_FILE = "data_usage";
    private static final String PREF_SHOW_WIFI = "show_wifi";
    private static final String PREF_SHOW_ETHERNET = "show_ethernet";
    private static final String PREF_DEFAULT_UNIT = "default_unit";

    private SharedPreferences mPrefs;

    private TabHost mTabHost;
    private ViewGroup mTabsContainer;
    private TabWidget mTabWidget;
    private ListView mListView;
    private ImageView mImg_view;
    private LinearLayout mLayout_MsimInfoLL;
    private RelativeLayout mLayout_MsimInfo;
    private TextView mTextView_sim;
    private DataUsageAdapter mAdapter;

    /** Distance to inset content from sides, when needed. */
    private int mInsetSide = 0;

    private ViewGroup mHeader;

    private ViewGroup mNetworkSwitchesContainer;
    private LinearLayout mNetworkSwitches;
    private Switch mDataEnabled;
    private View mDataEnabledView;
    private CheckBox mDisableAtLimit;
    private View mDisableAtLimitView;

    private View mCycleView;
    private Spinner mCycleSpinner;
    private CycleAdapter mCycleAdapter;
    private TextView mCycleSummary;

    private ChartDataUsageView mChart;
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

    private NetworkTemplate mTemplate;

    private ChartData mChartData;

    private AppItem mCurrentApp = null;

    private Intent mAppSettingsIntent;

    private NetworkPolicyEditor mPolicyEditor;

    private String mCurrentTab = null;
    private String mIntentTab = null;

    private MenuItem mMenuRestrictBackground;
    private MenuItem mMenuCellularNetworks;
    private MenuItem mMenuDataUsageReset;

    /** Flag used to ignore listeners during binding. */
    private boolean mBinding;
    private boolean mIsChartDataBinding;
    private boolean mIsSummaryUidLoaderBinding;

    private UidDetailProvider mUidDetailProvider;

    private TelephonyManager mMultiSimTM; //chriswon 20121011 V3 Multi SIM
    private static boolean mPopupdisable = false;

    private static int mTitleStyle = 0;
    private static ContentQueryMap mContentQueryMap;
    // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
    private static boolean mDataEnabledChecked = false;
    // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
    private static Observer mSettingsObserver;
    Cursor settingsCursor;

    // td155964: fixed the IllegalStateException by calling the dialog fragment after backg.
    // check the datausage fragement state.
    private static boolean sIsForg = false;
    static boolean aboidBlink = false;
    static Timer timer = new Timer();
    static TimerTask task = new TimerTask() {
        public void run() {
            aboidBlink = false;
        }
    };

    /* Dual SIM global parameters */
    private PhoneStateListener mPSListener1;
    private PhoneStateListener mPSListener2;
    private static int SIM_SLOT_1;
    private static int SIM_SLOT_2;
    private static int SIM_SLOT_3;
    private static int SIM_SUBSCRIBE_1;
    private static int SIM_SUBSCRIBE_2;
    private static int SIM_SUBSCRIBE_3;
    private static final int MAX_SUBSCRIPTIONS = 3;
    private int mSim1Stat = -1;
    private int mSim2Stat = -1;
    private int mSim3Stat = -1;
    private String mSavedCurrentTab = null;
    private int mLastSimEnabled = 0;
    private static boolean mIsSwitching = false;

    private boolean mHaveSim1Tab = false;
    private boolean mHaveSim2Tab = false;
    private boolean mHaveSim3Tab = false;
    private boolean mIsRadio1Off = false;
    private boolean mIsRadio2Off = false;
    private boolean mIsRadio3Off = false;
    private boolean mIsUpdateCycle = false;
    private IntentFilter mIntentFilter;
    private static final int EVENT_DETACH_TIME_OUT = 2000;
    private static final int EVENT_ATTACH_TIME_OUT = 2001;
    private static final int DETACH_TIME_OUT_LENGTH = 10000;
    private static final int ATTACH_TIME_OUT_LENGTH = 10000;
    private static Context mContext;

    private static final boolean mIs_Ctc_Cn = "CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator());

    private static long sSim1SubId;
    private static final int SIM_SLOT_1_SEL = 0;

    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            boolean needUpdate = false;

            if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                updateSimStatus();
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                String reason = intent.getStringExtra(PhoneConstants.STATE_CHANGE_REASON_KEY);
                String apnTypeList = intent.getStringExtra(PhoneConstants.DATA_APN_TYPE_KEY);
                PhoneConstants.DataState state;
                String str = intent.getStringExtra(PhoneConstants.STATE_KEY);

                if (str != null) {
                    state = Enum.valueOf(PhoneConstants.DataState.class, str);
                } else {
                    state = PhoneConstants.DataState.DISCONNECTED;
                }

                Log.i(TAG, "DataConnectionReceiver phone state : " + str);
                if (reason == null || (!PhoneConstants.APN_TYPE_DEFAULT.equals(apnTypeList))) {
                    return;
                }
                if ((reason.equals(Phone.REASON_DATA_ENABLED) || reason
                        .equals(Phone.REASON_PS_RESTRICT_DISABLED))
                        && (state == PhoneConstants.DataState.DISCONNECTED)
                        && (mIsSwitching == true)) {
                    timerHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
                    mIsSwitching = false;
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                mSavedCurrentTab = mTabHost.getCurrentTabTag();
                updateSimStatus();
                updateBody();
            }
        }
    };

    private ContentObserver mGprsDefaultSIMObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mSavedCurrentTab = mTabHost.getCurrentTabTag();
            updateBody();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();

        mContext = getActivity();
        mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        mStatsService = INetworkStatsService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mStatsServiceEx = INetworkStatsServiceEx.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mPolicyManager = NetworkPolicyManager.from(context);
        mTelephonyManager = TelephonyManager.from(context);
        mMultiSimTM = TelephonyManager.getDefault();

        mPrefs = getActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mIntentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
        mPolicyEditor.read();

        mShowWifi = mPrefs.getBoolean(PREF_SHOW_WIFI, false);
        mShowEthernet = mPrefs.getBoolean(PREF_SHOW_ETHERNET, false);
        sDisplayUnit = mPrefs.getBoolean(PREF_DEFAULT_UNIT, true);
        if (false == "VZW".equals(Config.getOperator())) {
            sDisplayUnit = false;
        }

        SIM_SLOT_1 = SIM_SUBSCRIBE_1 = 0;
        SIM_SLOT_2 = SIM_SUBSCRIBE_2 = 1;
        SIM_SLOT_3 = SIM_SUBSCRIBE_3 = 2;
        updateSimStatus();

        // override preferences when no mobile radio
        if (!hasReadyMobileRadio(context)) {
            mShowWifi = hasWifiRadio(context);
            mShowEthernet = hasEthernet(context);
        }
        setHasOptionsMenu(true);
        initIntentFilter();
        final int currentDds =  LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(context);
        final long currentDds1 =  SubscriptionManager.getDefaultDataSubId();
        Log.d(TAG, "dds : getDefaultDataPhoneId = " + currentDds);
        Log.d(TAG, "dds : getDefaultDataSubId = " + currentDds1);

        settingsCursor = getActivity().getContentResolver().query(Global.CONTENT_URI, null,
                "(" + android.provider.Settings.System.NAME + "=?)",
                new String[] { Global.MOBILE_DATA }, null);
        if (null != settingsCursor) {
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    android.provider.Settings.System.NAME, true, null);
        }

        setSettingObserver();

        sSim1SubId = LGSubscriptionManager.getSubIdBySlotId(SIM_SLOT_1_SEL);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
        MDMDataUsageAdapter.getInstance().initMdmPolicyMonitor(context, mLGMDMReceiver);
        // LGMDM_END
    }

    private void setSettingObserver() {
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
    }


    private void initIntentFilter() {
        // kerry - to sync with quick settings start
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        getActivity().registerReceiver(mReceiver, filter);
        // kerry - to sync with quick settings end
    }


    private void updateSimStatus() {
        mSim1Stat = TelephonyManager.getDefault().getSimState(SIM_SUBSCRIBE_1);
        mSim2Stat = TelephonyManager.getDefault().getSimState(SIM_SUBSCRIBE_2);
        if (Utils.isTripleSimEnabled()) {
            mSim3Stat = TelephonyManager.getDefault().getSimState(SIM_SUBSCRIBE_3);
        }

    }


    private void setRememberOptionChecked(boolean mValue) {
        android.provider.Settings.System.putInt(
                mContext.getContentResolver(),
                SettingsConstants.System.DATA_CONNECTIVITY_POPUP_REMEMBER,
                mValue ? 1 : 0);
    }

    //DataSwitcher pop-up for TLF_ES START
    private boolean isRememberOptionChecked() {
        return (android.provider.Settings.System.getInt(
                mContext.getContentResolver(),
                SettingsConstants.System.DATA_CONNECTIVITY_POPUP_REMEMBER, 0) == 1);
    }

    //DataSwitcher pop-up for TLF_ES END
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final Context context = inflater.getContext();
        View view;
        if (Utils.isUI_4_1_model(getActivity())) {
            view = inflater.inflate(R.layout.data_usage_summary_msim, container, false);
        } else {
            view = inflater.inflate(R.layout.data_usage_summary, container, false);
        }
        //final View view = inflater.inflate(R.layout.data_usage_summary, container, false);

        mTitleStyle = getResources().getInteger(R.integer.config_font_name); //[chris.won@lge.com][2012-11-30] Applying bold text

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

        if (Utils.isUI_4_1_model(getActivity())) {
            mLayout_MsimInfoLL = (LinearLayout)view.findViewById(R.id.layout_sim_info);
        } else {
            mLayout_MsimInfo = (RelativeLayout)view.findViewById(R.id.layout_sim_info);
        }
        mTextView_sim = (TextView)view.findViewById(R.id.empt_sim);
        mImg_view = (ImageView)view.findViewById(R.id.ic_simcard);
        mImg_view.setImageResource(R.drawable.ic_simcard);
        mTextView_sim.setTextColor(Color.BLACK);

        // decide if we need to manually inset our content, or if we should rely
        // on parent container for inset.
        final boolean shouldInset = mListView.getScrollBarStyle()
                == View.SCROLLBARS_OUTSIDE_OVERLAY;

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(mTabListener);
        mHeader = (ViewGroup)inflater.inflate(R.layout.data_usage_header,
                mListView, false);
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
            mNetworkSwitchesContainer = (ViewGroup)mHeader
                    .findViewById(R.id.network_switches_container);
            mNetworkSwitches = (LinearLayout)mHeader.findViewById(R.id.network_switches);

            mDataEnabled = new Switch(inflater.getContext());
            mDataEnabledView = inflatePreference(inflater, mNetworkSwitches, mDataEnabled);
            mDataEnabled.setOnCheckedChangeListener(mDataEnabledListener);
            // [SWITCH_SOUND]
            mDataEnabled.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
            mDataEnabledView.setOnClickListener(mDataEnabledViewListener);
            mDataEnabledView.setFocusable(false);
            mDataEnabledView.setClickable(true);
            mNetworkSwitches.addView(mDataEnabledView);

            mDisableAtLimit = new CheckBox(inflater.getContext());
            mDisableAtLimit.setClickable(false);
            mDisableAtLimit.setFocusable(false);
            mDisableAtLimitView = inflatePreference(inflater, mNetworkSwitches, mDisableAtLimit);
            mDisableAtLimitView.setClickable(true);
            mDisableAtLimitView.setFocusable(true);
            mDisableAtLimitView.setOnClickListener(mDisableAtLimitListener);
            mNetworkSwitches.addView(mDisableAtLimitView);

            if (Config.SKT.equals(Config.getOperator())
                    || Config.LGU.equals(Config.getOperator())) {
                //shlee1219 20120413 G1 SKT LGU+ Roaming - Mobile Data disable
                if (isAirplaneModeOn(inflater.getContext()) == true ||
                        (Config.isDataRoaming() == true && Config.SKT.equals(Config.getOperator()))) {
                    mDataEnabledView.setVisibility(View.VISIBLE);
                    mDataEnabled.setEnabled(false);

                    mDataEnabledView.setEnabled(false);
                    TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                    ev.setTextColor(Color.GRAY);

                    mDisableAtLimit.setEnabled(false);

                    mDisableAtLimitView.setEnabled(false);
                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.GRAY);

                    mNetworkSwitches.setEnabled(false);
                }
            } else if (Config.LGU.equals(Config.getOperator()) && Config.isDataRoaming() == true) {

                mDataEnabled.setEnabled(false);

                mDataEnabledView.setEnabled(false);
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.GRAY);

                mDisableAtLimit.setEnabled(false);

                mDisableAtLimitView.setEnabled(false);
                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.GRAY);

                mNetworkSwitches.setVisibility(View.GONE);
                mDataEnabledView.setVisibility(View.GONE);
            }
            else
            {
                if (isAirplaneModeOn(inflater.getContext()) == true || mIsSwitching) {
                    mDataEnabledView.setVisibility(View.VISIBLE);
                    mDataEnabled.setEnabled(false);

                    mDataEnabledView.setEnabled(false);
                    TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                    ev.setTextColor(Color.GRAY);

                    mDisableAtLimit.setEnabled(false);

                    mDisableAtLimitView.setEnabled(false);
                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.GRAY);

                    mNetworkSwitches.setEnabled(false);
                } // kerry
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
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM == true) {
            mCycleSpinner.setOnTouchListener(mCycleOnTouch);
        }
        // LGMDM_END
        mChart = (ChartDataUsageView)mHeader.findViewById(R.id.chart);
        mChart.setListener(mChartListener);
        mChart.bindNetworkPolicy(null);

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

        mEmpty = (TextView)mHeader.findViewById(android.R.id.empty);
        mStupidPadding = mHeader.findViewById(R.id.stupid_padding);

        final UserManager um = (UserManager)context.getSystemService(Context.USER_SERVICE);
        mAdapter = new DataUsageAdapter(um, mUidDetailProvider, mInsetSide);
        mListView.setOnItemClickListener(mListListener);
        mListView.setAdapter(mAdapter);

        //[s][chris.won@lge.com][2013-05-13] In case of Chart Data arrives lately.
        mAppDetail.setVisibility(View.GONE);
        mCycleAdapter.setChangeVisible(false);
        mChart.bindDetailNetworkStats(null);
        //[e][chris.won@lge.com][2013-05-13] In case of Chart Data arrives lately.

        // this kicks off chain reaction which creates tabs, binds the body to
        // selected network, and binds chart, cycles and detail list.
        updateTabs();

        return view;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // check the datausage fragement state.
        sIsForg = false;
        SLog.i("onPause");

        //if (null != mContentQueryMap)
        //    mContentQueryMap.deleteObserver(mSettingsObserver);
        //TelephonyManager.getDefault().listen(mPSListener1, PhoneStateListener.LISTEN_NONE);
        //TelephonyManager.getDefault().listen(mPSListener2, PhoneStateListener.LISTEN_NONE);

        if (mMultiSimTM.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            getActivity().getContentResolver().unregisterContentObserver(mGprsDefaultSIMObserver);
            mSavedCurrentTab = mTabHost.getCurrentTabTag();
            getActivity().unregisterReceiver(mSimReceiver);
        }

        //if (null != settingsCursor)
        //    settingsCursor.close();

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
    public void onResume() {
        super.onResume();
        SLog.i("onResume");

        mIsRadio1Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_1);
        mIsRadio2Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_2);
        if (Utils.isTripleSimEnabled()) {
            mIsRadio3Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_3);
        } else {
            mIsRadio3Off = true;
        }

        // pick default tab based on incoming intent
        final Intent intent = getActivity().getIntent();
        mIntentTab = computeTabFromIntent(intent);

        if (null != mIntentTab || null != mSavedCurrentTab) {
            updateTabs();
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
                } catch (RemoteException e) {
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

        if (mMultiSimTM.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            getActivity().getContentResolver().registerContentObserver(
                    Settings.System.getUriFor("default_subscription"), false,
                    mGprsDefaultSIMObserver);
            getActivity().registerReceiver(mSimReceiver, mIntentFilter);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final Context context = getActivity();
        final boolean appDetailMode = isAppDetailMode();
        final boolean isOwner = ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;

        mMenuRestrictBackground = menu.findItem(R.id.data_usage_menu_restrict_background);
        if ("LGU".equals(Config.getOperator()) && Config.isDataRoaming()) {
            mMenuRestrictBackground.setVisible(false);
        } else {
            mMenuRestrictBackground.setVisible(hasReadyMobileRadio(context) && !appDetailMode
                    && isOwner);
            mMenuRestrictBackground.setChecked(mPolicyManager.getRestrictBackground());
        }

        final MenuItem showWifi = menu.findItem(R.id.data_usage_menu_show_wifi);
        if (hasWifiRadio(context) && hasReadyMobileRadio(context)) {
            showWifi.setVisible(!appDetailMode);
            showWifi.setChecked(mShowWifi);
        } else {
            showWifi.setVisible(false);
            mShowWifi = true;
        }

        final MenuItem showEthernet = menu.findItem(R.id.data_usage_menu_show_ethernet);
        if (hasEthernet(context)
                && (hasReadyMobileRadio(context) || (mHaveSim1Tab && mHaveSim2Tab))) {
            showEthernet.setVisible(!appDetailMode);
            showEthernet.setChecked(mShowEthernet);
        } else {
            showEthernet.setVisible(false);
            mShowEthernet = true;
        }

        final MenuItem metered = menu.findItem(R.id.data_usage_menu_metered);
        if ((hasReadyMobileRadio(context) || hasWifiRadio(context))
                && (false == "TRF".equals(Config.getOperator()))) {
            // It should be not displayed the mobile hotspot menu as TRF requirement.
            metered.setVisible(!appDetailMode);
        } else {
            metered.setVisible(false);
        }

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
        if (!TextUtils.isEmpty(helpUrl = getResources().getString(R.string.help_url_data_usage))) {
            // applied the help app.
            if ("true".equals(helpUrl)) {
                Intent helpIntent = new Intent();
                helpIntent.setAction("com.lge.help.intent.action.SEARCH_FROM_APPS");
                helpIntent.putExtra("app-name", "Data");
                help.setIntent(helpIntent);
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

        prepareOptionResetMenu(menu, appDetailMode);
    }

    private void prepareOptionResetMenu(Menu menu, boolean appDetailMode) {
        mMenuDataUsageReset = menu.findItem(R.id.data_usage_reset_menu);
        //mMenuDataUsageReset.setVisible(mValue && !appDetailMode);
        if (mPolicyEditor == null
                || TAB_WIFI.equals(mCurrentTab)
                || mPolicyEditor.getPolicy(mTemplate) == null) {
            SLog.i("not applied the usage reset " + mCurrentTab);
            //mMenuDataUsageReset.setVisible(!appDetailMode);
            //mMenuDataUsageReset.setEnabled(false);
            mMenuDataUsageReset.setVisible(false);
        } else {
            NetworkPolicy policy = mPolicyEditor.getPolicy(mTemplate);
            boolean mValue = isNetworkPolicyModifiable(policy);
            SLog.i("Data reset visible = " + mValue);
            mMenuDataUsageReset.setVisible(mValue && !appDetailMode);
            //mMenuDataUsageReset.setEnabled(mValue);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_usage_qct, menu);
    }

    @Override
    public void onDestroy() {
        if (null != mContentQueryMap) {
            mContentQueryMap.deleteObserver(mSettingsObserver);
        }
        if (null != settingsCursor) {
            settingsCursor.close();
        }
        mDataEnabledView = null;
        mDisableAtLimitView = null;

        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;

        TrafficStats.closeQuietly(mStatsSession);
        getActivity().unregisterReceiver(mReceiver); // kerry

        if (this.isRemoving()) {
            getFragmentManager()
                    .popBackStack(TAG_APP_DETAILS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
        MDMDataUsageAdapter.getInstance().finalizeMdmPolicyMonitor(getActivity(), mLGMDMReceiver);
        // LGMDM_END

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.data_usage_menu_restrict_background: {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-236]
            if (MDMDataUsageAdapter.getInstance().isShowEnforceBackgroundDataRestrictedToastIfNeed(getActivity())) {
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
                        // no confirmation to drop restriction
                        setRestrictBackground(false);
                    }
                }
                else {
                    // no confirmation to drop restriction
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
        case R.id.data_usage_menu_cellular_networks: {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.android.phone",
                    "com.android.phone.MobileNetworkSettings"));
            startActivity(intent);
            return true;
        }
        case R.id.data_usage_menu_metered: {
            final PreferenceActivity activity = (PreferenceActivity)getActivity();
            activity.startPreferencePanel(DataUsageMeteredSettings.class.getCanonicalName(), null,
                    R.string.data_usage_metered_title, null, this, 0);
            return true;
        }

        case R.id.data_usage_reset_menu: {
            if (ActivityManager.isUserAMonkey()) {
                SLog.d("ignoring monkey's attempt to reset the data usage record");
            } else {
                ConfirmDataUsageResetFragment.show(this);
            }
            return true;
        }
        }
        return false;
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
        } else if (TelephonyManager.getDefault().isMultiSimEnabled()
                || Utils.isTripleSimEnabled()) {
            mTabHost.addTab(buildTabSpec(TAB_SIM_1,
                    OverlayUtils.getMSimTabName(context,
                            SettingsConstants.System.SIM1_NAME)));
            mHaveSim1Tab = true;
            mTabHost.addTab(buildTabSpec(TAB_SIM_2,
                    OverlayUtils.getMSimTabName(context,
                            SettingsConstants.System.SIM2_NAME)));
            mHaveSim2Tab = true;
            if (Utils.isTripleSimEnabled()) {
                mTabHost.addTab(buildTabSpec(TAB_SIM_3,
                        OverlayUtils.getMSimTabName(context,
                                SettingsConstants.System.SIM3_NAME)));
                mHaveSim3Tab = true;
            }
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
        } else if (mSavedCurrentTab != null) {
            if (Objects.equal(mSavedCurrentTab, mTabHost.getCurrentTabTag())) {
                updateBody();
            } else {
                mTabHost.setCurrentTabByTag(mSavedCurrentTab);
            }
            mSavedCurrentTab = null;

        } else if (noTabs) {
            // no usable tabs, so hide body
            updateBody();
        } else {
            // already hit updateBody() when added; ignore
        }
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
     * Build {@link TabSpec} with thin indicator, and empty content.
     */
    private TabSpec buildTabSpec(String tag, int titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(getText(titleRes)).setContent(
                mEmptyTabContent);
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

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            // user changed tab; update body
            if (mIntentTab != null) {
                if (false == Objects.equal(mIntentTab, mTabHost.getCurrentTabTag())) {
                    SLog.i("test ===== OnTabChangeListener skip update :: " + mIntentTab);
                    return;
                }
            } else if (mSavedCurrentTab != null) {
                if (false == Objects.equal(mSavedCurrentTab, mTabHost.getCurrentTabTag())) {
                    SLog.i("test ===== OnTabChangeListener skip update :: " + mSavedCurrentTab);
                    return;
                }
            }

            SLog.i("onTabChanged getCurrentTabTag()== " + mTabHost.getCurrentTabTag());

            if (getPopupdisable()) {
                mTabHost.setCurrentTabByTag(mCurrentTab);
                SLog.i("OnTabChangeListener skip update :: " + mCurrentTab);
                return;
            }
            updateBody();
        }
    };

    private TabSpec buildTabSpec(String tag, String titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(titleRes).setContent(
                mEmptyTabContent);
    }


    private boolean isAppDetailMode() {
        return mCurrentApp != null;
    }

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
            //131205 ::   mListView.setVisibility(View.VISIBLE);
        }

        final boolean tabChanged = !currentTab.equals(mCurrentTab);
        mCurrentTab = currentTab;

        if (LOGD) {
            Log.d(TAG, "updateBody() with currentTab=" + currentTab);
        }

        mDataEnabledView.setVisibility(View.VISIBLE);

        mIsRadio1Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_1);
        mIsRadio2Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_2);
        if (Utils.isTripleSimEnabled()) {
            mIsRadio3Off = !hasReadyMobileRadio(getActivity(), SIM_SUBSCRIBE_3);
        } else {
            mIsRadio3Off = true;
        }

        int mCurrentSimID = 0;

        // TODO: remove mobile tabs when SIM isn't ready
        final TelephonyManager tele = TelephonyManager.from(context);
        if (TAB_SIM_1.equals(currentTab)) {
            boolean isRadioOff = false;
            if (mIsRadio1Off || isAirplaneModeOn(mContext)) {
                isRadioOff = true;
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(false);
                }
                Log.d(TAG, "disable sim 1 enable because radio off");
            } else {
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(true);
                }
            }
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            mDataEnabled.setEnabled(!mIsSwitching && !isRadioOff);
            mDataEnabledView.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimit.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimitView.setEnabled(!mIsSwitching && !isRadioOff);
            if (true == mIsSwitching || isRadioOff) {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.GRAY);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.GRAY);
            } else {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.BLACK);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.BLACK);
            }
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context, SIM_SLOT_1));
            SLog.i("mTemplate = " + mTemplate);
            mCurrentSimID = SIM_SUBSCRIBE_1;
        } else if (TAB_SIM_2.equals(currentTab)) {
            boolean isRadioOff = false;
            if (mIsRadio2Off || isAirplaneModeOn(mContext)) {
                //Sim radio off , cannot set data connection for it.
                isRadioOff = true;
                Log.d(TAG, "disable sim 2 enable because radio off");
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(false);
                }
            } else {
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(true);
                }
            }
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            mDataEnabled.setEnabled(!mIsSwitching && !isRadioOff);
            mDataEnabledView.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimit.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimitView.setEnabled(!mIsSwitching && !isRadioOff);
            if (true == mIsSwitching || isRadioOff) {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.GRAY);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.GRAY);
            } else {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.BLACK);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.BLACK);
            }
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context, SIM_SLOT_2));
            mCurrentSimID = SIM_SUBSCRIBE_2;
        } else if (TAB_SIM_3.equals(currentTab)) {
            boolean isRadioOff = false;
            if (mIsRadio3Off || isAirplaneModeOn(mContext)) {
                //Sim radio off , cannot set data connection for it.
                isRadioOff = true;
                Log.d(TAG, "disable sim 3 enable because radio off");
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(false);
                }
            } else {
                if (null != mNetworkSwitches) {
                    mNetworkSwitches.setEnabled(true);
                }
            }
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            mDataEnabled.setEnabled(!mIsSwitching && !isRadioOff);
            mDataEnabledView.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimit.setEnabled(!mIsSwitching && !isRadioOff);
            mDisableAtLimitView.setEnabled(!mIsSwitching && !isRadioOff);
            if (true == mIsSwitching || isRadioOff) {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.GRAY);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.GRAY);
            } else {
                TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                ev.setTextColor(Color.BLACK);

                TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                lv.setTextColor(Color.BLACK);
            }
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context, SIM_SLOT_3));
            mCurrentSimID = SIM_SUBSCRIBE_3;
        } else if (TAB_MOBILE.equals(currentTab)) {
            if (true == "SKT".equals(Config.getOperator())) {
                setPreferenceTitle(mDataEnabledView, R.string.data_network_settings_title);
            } else {
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
            }
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);

            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context));

        } else if (TAB_3G.equals(currentTab)) {
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_3g);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
            // TODO: bind mDataEnabled to 3G radio state
            mTemplate = buildTemplateMobile3gLower(getActiveSubscriberId(context));

        } else if (TAB_4G.equals(currentTab)) {
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_4g);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_4g_limit);
            // TODO: bind mDataEnabled to 4G radio state
            mTemplate = buildTemplateMobile4g(getActiveSubscriberId(context));

        } else if (TAB_WIFI.equals(currentTab)) {
            // wifi doesn't have any controls
            mDataEnabledView.setVisibility(View.GONE);
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateWifiWildcard();

        } else if (TAB_ETHERNET.equals(currentTab)) {
            // ethernet doesn't have any controls
            mDataEnabledView.setVisibility(View.GONE);
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateEthernet();

        } else {
            throw new IllegalStateException("unknown tab: " + currentTab);
        }

        boolean mIsNormalSimState = true;
        if (TAB_SIM_1.equals(currentTab)
                || TAB_SIM_2.equals(currentTab)
                || TAB_SIM_3.equals(currentTab)) {
            SLog.i("updateBody :: mCurrentSimID = " + mCurrentSimID);

            if (mIs_Ctc_Cn) {
                if (TAB_SIM_2.equals(currentTab)) {
                    mTextView_sim.setText(getString(R.string.sp_no_sim));
                } else {
                    mTextView_sim.setText(getString(R.string.sp_icc_uim_absent_NORMAL));
                }
            } else {
                mTextView_sim.setText(getString(R.string.sp_no_sim));
            }

            if (OverlayUtils.check_SIM_inserted(mCurrentSimID)) {
                if (OverlayUtils.check_sim_lock_state(
                        getActivity().getApplicationContext(),
                        mCurrentSimID)) {
                    String m_sim_state = OverlayUtils.
                            get_SIM_state(getActivity().getApplicationContext(),
                                    mCurrentSimID);
                    mIsNormalSimState = false;
                    if (m_sim_state.equals("pin_lock")) {
                        if (mIs_Ctc_Cn) {
                            if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_simpinlocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_uimpinlocked_new_NORMAL));
                            }
                        } else {
                            if (TAB_SIM_1.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim1pinlocked_new_NORMAL));
                            } else if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim2pinlocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim3pinlocked_new_NORMAL));
                            }
                        }
                    } else if (m_sim_state.equals("puk_lock")) {
                        if (mIs_Ctc_Cn) {
                            if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_simpinlocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_uimpinlocked_new_NORMAL));
                            }
                        } else {
                            if (TAB_SIM_1.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim1puklocked_new_NORMAL));
                            } else if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim2puklocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim3puklocked_new_NORMAL));
                            }
                        }
                    } else {
                        if (mIs_Ctc_Cn) {
                            if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_simpinlocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_uimpinlocked_new_NORMAL));
                            }
                        } else {
                            if (TAB_SIM_1.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim1permlocked_new_NORMAL));
                            } else if (TAB_SIM_2.equals(currentTab)) {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim2permlocked_new_NORMAL));
                            } else {
                                mTextView_sim
                                        .setText(getString(R.string.sp_sim3permlocked_new_NORMAL));
                            }
                        }
                    }
                }
            } else {
                mIsNormalSimState = false;
            }
        }
        SLog.i("updateBody :: mIsNormalSimState  = " + mIsNormalSimState);
        if (mIsNormalSimState) {
            if (Utils.isUI_4_1_model(getActivity())) {
                mLayout_MsimInfoLL.setVisibility(View.GONE);
            } else {
                mLayout_MsimInfo.setVisibility(View.GONE);
            }
            mListView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.GONE);
            if (Utils.isUI_4_1_model(getActivity())) {
                mLayout_MsimInfoLL.setVisibility(View.VISIBLE);
            } else {
                mLayout_MsimInfo.setVisibility(View.VISIBLE);
            }

        }

        SLog.i("updateBody ::restartLoader mTemplate = " + mTemplate);
        mCycleSpinner.setAdapter(mCycleAdapter);
        // kick off loader for network history
        // TODO: consider chaining two loaders together instead of reloading
        // network history when showing app detail.
        SLog.i("updateBody() ::mChartDataCallbacks mIsChartDataBinding = " + mIsChartDataBinding);
        if (!mIsChartDataBinding) {
            SLog.i("updateBody() :: start chart loader ");
            getLoaderManager().restartLoader(LOADER_CHART_DATA,
                    ChartDataLoaderMultiSIM.buildArgs(mTemplate, mCurrentApp), mChartDataCallbacks);
        } else {
            SLog.e("updateBody() :: skip chart loader");
        }
        // detail mode can change visible menus, invalidate
        getActivity().invalidateOptionsMenu();

        mBinding = false;
    }

    private void setPolicyWarningBytes(long warningBytes) {
        if (LOGD) {
            Log.d(TAG, "setPolicyWarningBytes() = " + warningBytes);
        }
        mPolicyEditor.setPolicyWarningBytes(mTemplate, warningBytes);
        updatePolicy(false);
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

            if (hasLimitedNetworks()) {
                setPreferenceSummary(mAppRestrictView,
                        getString(R.string.data_usage_app_restrict_background_summary));
            } else {
                setPreferenceSummary(mAppRestrictView,
                        getString(R.string.data_usage_app_restrict_background_summary_disabled));
            }

            mAppRestrictView.setVisibility(View.VISIBLE);
            mAppRestrict.setChecked(getAppRestrictBackground());

        } else {
            mAppRestrictView.setVisibility(View.GONE);
        }
    }

    private void setPolicyLimitBytes(long limitBytes) {
        if (LOGD) {
            Log.d(TAG, "setPolicyLimitBytes() = " + limitBytes);
        }
        mPolicyEditor.setPolicyLimitBytes(mTemplate, limitBytes);
        updatePolicy(false);
    }

    private boolean isMobileDataEnabled(int slotId) {
        int subId = -1;
        updateSimStatus();
        if (slotId == SIM_SLOT_1) {
            subId = SIM_SUBSCRIBE_1;
        } else if (slotId == SIM_SLOT_2) {
            subId = SIM_SUBSCRIBE_2;
        } else if (slotId == SIM_SLOT_3) {
            subId = SIM_SUBSCRIBE_3;
        }
        if (isConfirmDialogShowed() == true) {
            SLog.i("isMobileDataEnabled[" + slotId + "] = false");
            return false;
        }
        if (mMobileDataEnabled != null) {
            SLog.i("isMobileDataEnabled[" + slotId + "] = false");
            return mMobileDataEnabled && (subId == Utils.getCurrentDDS(mContext))
                    && TelephonyManager.getDefault().getSimState(subId) == SIM_STATE_READY;
        } else {
            SLog.i("isMobileDataEnabled[" + slotId + "] = false");
            return mTelephonyManager.getDataEnabled()
                    && (subId == Utils.getCurrentDDS(mContext))
                    && TelephonyManager.getDefault().getSimState(subId) == SIM_STATE_READY;
        }
    }

    /**
     * Local cache of value, used to work around delay when
     * {@link ConnectivityManager#setMobileDataEnabled(boolean)} is async.
     */
    private Boolean mMobileDataEnabled;

    private boolean isMobileDataEnabled() {
        if (isConfirmDialogShowed() == true) {
            return false;
        }
        if (mMobileDataEnabled != null) {
            // TODO: deprecate and remove this once enabled flag is on policy
            return mMobileDataEnabled;
        } else {
            return mTelephonyManager.getDataEnabled();
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        if (LOGD) {
            Log.d(TAG, "setMobileDataEnabled()");
        }
        
        mTelephonyManager.setDataEnabled(enabled);
        sendBroadcastDdsForMMS(enabled);

        mMobileDataEnabled = enabled;
        updatePolicy(false);
    }

    private void setMobileDataEnabledChangedDefaultSIM(boolean enabled, int slotId, boolean offToOn) {

        long[] subId =  LGSubscriptionManager.getSubId(slotId);

        if (enabled) {
            if (mTelephonyManager.getDataEnabled() == false) {
                Utils.setMobileDataEnabledDB(mContext, slotId, true);
                 mTelephonyManager.setDataEnabledUsingSubId(subId[0], true);
                offToOn = true;
            }

             LGSubscriptionManager.changedParameterTypeLongToIntReturnVoid(subId[0], "setDefaultDataSubId");

            if (!offToOn) {
                mIsSwitching = true;
                timerHandler.sendEmptyMessageDelayed(
                        EVENT_ATTACH_TIME_OUT, DETACH_TIME_OUT_LENGTH);
            }

        } else {
            if (mTelephonyManager.getDataEnabled() == true) {
                Utils.setMobileDataEnabledDB(mContext, slotId, false);
                mTelephonyManager.setDataEnabledUsingSubId(subId[0], false);
            }
        }
    }

    private void setMobileDataEnabled(boolean enabled, int slotId) {
        int defaultSlotId = Utils.getCurrentDDS(mContext);
        boolean offToOn = false;
        Log.d(TAG, "defaultSlotId is " + Integer.toString(defaultSlotId));
        Log.d(TAG, "defaultSlotId is slotId " + slotId);
        mMobileDataEnabled = enabled;

        long[] subId = LGSubscriptionManager.getSubId(slotId);
        Log.d(TAG, "defaultSlotId is subId " + subId);
        Log.d(TAG, "defaultSlotId is subId[0] " + subId[0]);

        if (defaultSlotId == -1000) {
            Utils.setMobileDataEnabledDB(mContext, slotId, false);
            mTelephonyManager.setDataEnabledUsingSubId(subId[0], false);
        } else if (slotId == defaultSlotId) {
            if (enabled) {
                if (mTelephonyManager.getDataEnabled() == false) {
                    Utils.setMobileDataEnabledDB(mContext, slotId, true);
                    mTelephonyManager.setDataEnabledUsingSubId(subId[0], true);
                }
            } else {
                Utils.setMobileDataEnabledDB(mContext, slotId, false);
                mTelephonyManager.setDataEnabledUsingSubId(subId[0], false);
            }
        } else {
            setMobileDataEnabledChangedDefaultSIM(enabled, slotId, offToOn);
        }
        sendBroadcastDdsForMMS(enabled);
        updatePolicy(false);

    }

    // KERRY START
    private static boolean confirmDialogShowed = false;
    private static boolean positiveResult = false;

    private static void setPositiveResult(boolean result) {
        positiveResult = result;
    }

    private static void setConfirmDialogShowed(boolean showed) {
        confirmDialogShowed = showed;
    }

    private static boolean isConfirmDialogShowed() {
        return confirmDialogShowed;
    }

    private static boolean isPositiveResult() {
        return positiveResult;
    }

    // KERRY END
    private boolean isNetworkPolicyModifiable(NetworkPolicy policy) {
        boolean sim_ready = true;
        if (null == mDataEnabled) {
            return false;
        }
        else if (mMultiSimTM.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            if (TAB_SIM_1.equals(mCurrentTab)) {
                sim_ready = TelephonyManager.
                        getDefault().getSimState(SIM_SUBSCRIBE_1) == SIM_STATE_READY;
            }
            if (TAB_SIM_2.equals(mCurrentTab)) {
                sim_ready = TelephonyManager.
                        getDefault().getSimState(SIM_SUBSCRIBE_2) == SIM_STATE_READY;
            }
            if (Utils.isTripleSimEnabled() && TAB_SIM_3.equals(mCurrentTab)) {
                sim_ready = TelephonyManager.
                        getDefault().getSimState(SIM_SUBSCRIBE_3) == SIM_STATE_READY;
            }
        }
        Log.d(TAG, "Current Tab is " + mCurrentTab);
        Log.d(TAG, "Policy is null" + (policy == null ? "true" : "false"));
        Log.d(TAG, "Sim is " + (sim_ready ? "ready" : "not ready"));
        Log.d(TAG, "Bandwidth Control is " + (isBandwidthControlEnabled() ? "enabled" : "disabled"));
        Log.d(TAG, "Data switch is " + (mDataEnabled.isChecked() ? "enabled" : "disabled"));
        if (mMultiSimTM.isMultiSimEnabled()) {
            return policy != null && sim_ready && isBandwidthControlEnabled()
                    && mDataEnabled.isChecked();
        } else {
            return policy != null && isBandwidthControlEnabled() && mDataEnabled.isChecked()
                    && ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;
        }
    }

    private boolean getDataRoaming() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Global.getInt(resolver, Settings.Global.DATA_ROAMING + Utils.getCurrentDDS(mContext), 0) != 0;
    }

    private boolean isBandwidthControlEnabled() {
        try {
            return mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with INetworkManagementService: " + e);
            return false;
        }
    }

    private boolean getAppRestrictBackground() {
        final int uid = mCurrentApp.key;
        final int uidPolicy = mPolicyManager.getUidPolicy(uid);
        return (uidPolicy & POLICY_REJECT_METERED_BACKGROUND) != 0;
    }


    public void setRestrictBackground(boolean restrictBackground) {
        if (LOGD) {
            Log.d(TAG, "setRestrictBackground()");
        }
        //        try {
        // LGE_CHANGE_S, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05
        Settings.Secure.putInt(getActivity().getContentResolver(),
                SettingsConstants.Secure.DATA_NETWORK_USER_BACKGROUND_SETTING_DATA,
                restrictBackground ? 1 : 0);
        // LGE_CHANGE_E, [LGE_DATA][LIMIT_DATA_USAGE], hyoseab.song@lge.com, 2011-08-05

        mPolicyManager.setRestrictBackground(restrictBackground);
        mMenuRestrictBackground.setChecked(restrictBackground);
        //        } catch (RemoteException e) {
        //            Log.w(TAG, "problem talking with policy service: " + e);
        //        }
    }

    private void setAppRestrictBackground(boolean restrictBackground) {
        if (LOGD) {
            Log.d(TAG, "setAppRestrictBackground()");
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
        if (TAB_SIM_1.equals(mCurrentTab)) {
            mBinding = true;
            if (!isAirplaneModeOn(getActivity())) {
                mDataEnabled.setChecked(isMobileDataEnabled(SIM_SLOT_1));
                mDataEnabledChecked = mDataEnabled.isChecked();
            } else {
                mDataEnabled.setChecked(false);
            }
            mBinding = false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            MDMDataUsageAdapter.getInstance().setDataUsageSwitchMultiSIM(
                    mDataEnabled, mDataEnabledListener, isAirplaneModeOn(getActivity()));
            // LGMDM_END
        } else if (TAB_SIM_2.equals(mCurrentTab)) {
            mBinding = true;
            if (!isAirplaneModeOn(getActivity())) {
                mDataEnabled.setChecked(isMobileDataEnabled(SIM_SLOT_2));
                mDataEnabledChecked = mDataEnabled.isChecked();
            } else {
                mDataEnabled.setChecked(false);
            }
            mBinding = false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            MDMDataUsageAdapter.getInstance().setDataUsageSwitchMultiSIM(
                    mDataEnabled, mDataEnabledListener, isAirplaneModeOn(getActivity()));
            // LGMDM_END
        } else if (TAB_SIM_3.equals(mCurrentTab)) {
            mBinding = true;
            if (!isAirplaneModeOn(getActivity())) {
                mDataEnabled.setChecked(isMobileDataEnabled(SIM_SLOT_3));
                mDataEnabledChecked = mDataEnabled.isChecked();
            } else {
                mDataEnabled.setChecked(false);
            }
            mBinding = false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            MDMDataUsageAdapter.getInstance().setDataUsageSwitchMultiSIM(
                    mDataEnabled, mDataEnabledListener, isAirplaneModeOn(getActivity()));
            // LGMDM_END
        } else if (TAB_MOBILE.equals(mCurrentTab)) {
            mBinding = true;
            if (!isAirplaneModeOn(getActivity())) {
                mDataEnabled.setChecked(isMobileDataEnabled());
                mDataEnabledChecked = mDataEnabled.isChecked();
            } else {
                mDataEnabled.setChecked(false);
            }
            mBinding = false;
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            MDMDataUsageAdapter.getInstance().setDataUsageSwitchMultiSIM(
                    mDataEnabled, mDataEnabledListener, isAirplaneModeOn(getActivity()));
            // LGMDM_END
        }

        final NetworkPolicy policy = mPolicyEditor.getPolicy(mTemplate);
        if (isNetworkPolicyModifiable(policy)) {
            if (null != mDisableAtLimitView) {
                mDisableAtLimitView.setVisibility(View.VISIBLE);
            }
            if (null != mDisableAtLimit) {
                mDisableAtLimit.setChecked(policy != null && policy.limitBytes != LIMIT_DISABLED);
            }
            // [START_LGT_SETTING] mod, 2012-07-03 : Fixed WBT ISSUE #374862.
            if (true == "VZW".equals(Config.getOperator()) && (null != mDisableAtLimit)
                    && (false == mDisableAtLimit.isChecked())) {

                if (null != policy) {
                    policy.warningBytes = WARNING_DISABLED;
                }
            }
            // [END_LGT_SETTING] mod, 2012-07-03 : Fixed WBT ISSUE #374863.
            if (!isAppDetailMode()) {
                if (null != mChart) {
                    mChart.bindNetworkPolicy(policy);
                }
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-344][ID-MDM-346]
            MDMDataUsageAdapter.getInstance().setDataUsageLimitMenu(mDisableAtLimit, mDisableAtLimitView);
            // LGMDM_END

        } else {
            // controls are disabled; don't bind warning/limit sweeps
            if (null != mDisableAtLimitView) {
                mDisableAtLimitView.setVisibility(View.GONE);
            }
            if (null != mChart) {
                mChart.bindNetworkPolicy(null);
            }
        }

        if (refreshCycle) {
            // generate cycle list based on policy and available history
            updateCycleList(policy);
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

    private OnCheckedChangeListener mDataEnabledListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SLog.i("mDataEnabledListener :: onCheckedChanged = " + mBinding);

            if (mBinding) {
                return;
            }

            if (false == sIsForg) {
                SLog.i("mDataEnabledListener not forg, so skip...");
                return;
            }
            final boolean dataEnabled = isChecked;
            // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
            if (!isAirplaneModeOn(getActivity())) {
                mDataEnabledChecked = dataEnabled;
            }
            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            if (MDMDataUsageAdapter.getInstance().isMobileNetworkDisallowed()) {
                Log.i("MDM", "[LGMDM] Deactivate MobileDataMenu");
                return;
            }
            // LGMDM_END

            SLog.i("mDataEnabledListener :: currentTab = " + mCurrentTab);
            final String currentTab = mCurrentTab;
            if (TAB_SIM_1.equals(currentTab)) {
                if (dataEnabled == true) {
                    mLastSimEnabled = 1;
                }
                if (mMultiSimTM.isMultiSimEnabled() /* && Utils.isTripleSimEnabled() */) {
                    mDataEnabledChecked = dataEnabled
                            || (!dataEnabled && (isMobileDataEnabled(SIM_SLOT_2)));
                    onDataEnableChange(dataEnabled, SIM_SLOT_1);
                    SLog.i("mDataEnabledListener :: currentTab 1 ");
                } else if (Utils.isTripleSimEnabled()) {
                    mDataEnabledChecked = dataEnabled
                            || (!dataEnabled
                            && (isMobileDataEnabled(SIM_SLOT_2)
                            || isMobileDataEnabled(SIM_SLOT_3)));
                    onDataEnableChange(dataEnabled, SIM_SLOT_1);
                    SLog.i("mDataEnabledListener :: currentTab 2 ");
                }
            } else if (TAB_SIM_2.equals(currentTab)) {
                if (dataEnabled == true) {
                    mLastSimEnabled = 2;
                }
                if (mMultiSimTM.isMultiSimEnabled() /* && !Utils.isTripleSimEnabled() */) {
                    mDataEnabledChecked = dataEnabled
                            || (!dataEnabled && (isMobileDataEnabled(SIM_SLOT_1)));
                    onDataEnableChange(dataEnabled, SIM_SLOT_2);
                } else if (Utils.isTripleSimEnabled()) {
                    mDataEnabledChecked = dataEnabled
                            || (!dataEnabled
                            && (isMobileDataEnabled(SIM_SLOT_1)
                            || isMobileDataEnabled(SIM_SLOT_3)));
                    onDataEnableChange(dataEnabled, SIM_SLOT_2);
                }
            } else if (TAB_SIM_3.equals(currentTab)) {
                if (dataEnabled == true) {
                    mLastSimEnabled = 3;
                }
                if (mMultiSimTM.isMultiSimEnabled() /* || Utils.isTripleSimEnabled() */) {
                    mDataEnabledChecked = dataEnabled
                            || (!dataEnabled && (isMobileDataEnabled(SIM_SLOT_1)
                            || isMobileDataEnabled(SIM_SLOT_2)));
                    onDataEnableChange(dataEnabled, SIM_SLOT_3);
                }
            } else if (TAB_MOBILE.equals(currentTab)) {
                if (dataEnabled) {
                    if (true == "SKT".equals(Config.getOperator())) {
                        ConfirmDataDisableFragment.show(DataUsageSummaryMultiSIM.this);
                    }
                    else {
                        setMobileDataEnabled(true);
                    }
                } else {
                    // disabling data; show confirmation dialog which eventually
                    // calls setMobileDataEnabled() once user confirms.
                    ConfirmDataDisableFragment.show(DataUsageSummaryMultiSIM.this);
                    setConfirmDialogShowed(true); // KERRY
                }

                if (false == aboidBlink) {
                    aboidBlink = true;
                    task.cancel();
                    timer.purge();
                    timer = new Timer();
                    task = new TimerTask() {
                        public void run() {
                            aboidBlink = false;
                        }
                    };
                    timer.schedule(task, 2000);
                }
                else {
                    task.cancel();
                    timer.purge();
                    timer = new Timer();
                    task = new TimerTask() {
                        public void run() {
                            aboidBlink = false;
                        }
                    };
                    timer.schedule(task, 2000);
                }
            }
            if (!isConfirmDialogShowed()) {

                updatePolicy(true);
            }
        }
    };

    private void onDataEnableChange(boolean dataEnabled, int slotId) {
        SLog.i("onDataEnableChange = " + dataEnabled);
        if (isMobileDataEnabled(slotId) == dataEnabled) {
            SLog.i("onDataEnableChange return ");
            return;
        }
        updateSimStatus();
        if (dataEnabled) {
            SLog.i("onDataEnableChange = " +
                    (getSimStatus(slotId) == TelephonyManager.SIM_STATE_NETWORK_LOCKED));
            if (getSimStatus(slotId) == TelephonyManager.SIM_STATE_NETWORK_LOCKED) {
                mDataEnabled.setChecked(false);
            } else {
                if (mIs_Ctc_Cn) {
                    if (Config.isDataRoaming(sSim1SubId)) {
                        SLog.i("onDataEnableChange: set mobile data in roaming.");
                        if (isRememberOptionChecked()) {
                            setMobileDataEnabled(true, slotId);
                        } else {
                            setRememberOptionChecked(true);
                            ConfirmDataEnableFragment.show(DataUsageSummaryMultiSIM.this, slotId);
                        }
                    } else {
                        setRememberOptionChecked(false);
                        setMobileDataEnabled(true, slotId);
                    }
                } else {
                    setMobileDataEnabled(true, slotId);
                }
            }
        } else {
            SLog.i("onDataEnableChange ConfirmDataDisableFragment.show()");
            if (mIs_Ctc_Cn /* && Config.isDataRoaming() */) {
                setMobileDataEnabled(false, slotId);
            } else {
                ConfirmDataDisableFragment.show(DataUsageSummaryMultiSIM.this, slotId);
                setConfirmDialogShowed(true);
            }
        }
    }

    private View.OnClickListener mDataEnabledViewListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-167]
            if (MDMDataUsageAdapter.getInstance().isMobileNetworkDisallowed()) {
                Log.i("MDM", "[LGMDM] Deactivate MobileDataMenu");
                return;
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
                ConfirmAppRestrictFragment.show(DataUsageSummaryMultiSIM.this);
            } else {
                setAppRestrictBackground(false);
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
            if (MDMDataUsageAdapter.getInstance().onClickDataUsageLimitMenu()) {
                return;
            }
            // LGMDM_END
            if (Utils.isMonkeyRunning() || mPolicyEditor.getPolicy(mTemplate) == null) {
                return;
            }

            final boolean disableAtLimit = !mDisableAtLimit.isChecked();
            if (disableAtLimit) {
                // enabling limit; show confirmation dialog which eventually
                // calls setPolicyLimitBytes() once user confirms.
                setPopupdisable(true);
                ConfirmLimitFragment.show(DataUsageSummaryMultiSIM.this);
            } else {
                setPolicyLimitBytes(LIMIT_DISABLED);

                if (true == "VZW".equals(Config.getOperator())) {
                    setPolicyWarningBytes(WARNING_DISABLED);
                }
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

    private View.OnTouchListener mCycleOnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-342][ID-MDM-343]
                if (MDMDataUsageAdapter.getInstance().isDataUsageCycleDisallowed(
                            getActivity(), mTabHost.getCurrentTabTag())) {
                    return true;
                }
                // LGMDM_END
            }
            return false;
        }
    };

    private OnItemClickListener mListListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Context context = view.getContext();
            final AppItem app = (AppItem)parent.getItemAtPosition(position);

            // TODO: sigh, remove this hack once we understand 6450986
            if (mUidDetailProvider == null || app == null) {
                return;
            }

            final UidDetail detail = mUidDetailProvider.getUidDetail(app.key, true);
            AppDetailsFragment.show(DataUsageSummaryMultiSIM.this, app, detail.label);
        }
    };

    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final CycleItem cycle = (CycleItem)parent.getItemAtPosition(position);
            if (cycle instanceof CycleChangeItem) {
                // show cycle editor; will eventually call setPolicyCycleDay()
                // when user finishes editing.
                CycleEditorFragment.show(DataUsageSummaryMultiSIM.this);

                // reset spinner to something other than "change cycle..."
                mCycleSpinner.setSelection(0);

            } else {
                if (LOGD) {
                    if (null != cycle) {
                        Log.d(TAG, "showing cycle " + cycle + ", start=" + cycle.start + ", end="
                                + cycle.end + "]");
                    } // 120103 WBT
                }

                // update chart to show selected cycle, and update detail data
                // to match updated sweep bounds.
                if (null != cycle) {
                    mChart.setVisibleRange(cycle.start, cycle.end);
                } // 120103 WBT

                if (mCycleSpinner.isPressed() || mIsUpdateCycle) {
                    updateDetailData();
                    mIsUpdateCycle = false;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // ignored
        }
    };

    private void setAppTotalText(final Context context, final long totalBytes) {
        if (mAppTotal != null) {
            mAppTotal.setText(formatFileSizeForUnit(context, totalBytes, sDisplayUnit));
        }
    }


    /**
     * Update details based on {@link #mChart} inspection range depending on
     * current mode. In network mode, updates {@link #mAdapter} with sorted list
     * of applications data usage, and when {@link #isAppDetailMode()} update
     * app details.
     */
    private void updateDetailData() {
        if (LOGD) {
            Log.d(TAG, "updateDetailData()");
        }

        if (null == mChart) {
            SLog.i("updateDetailData mChar null, return...");
            return;
        }

        //final long start = mChart.getInspectStart();
        //final long end = mChart.getInspectEnd();
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

            // Dn not request to restart loader if it's already restarted.
            if (!mIsSummaryUidLoaderBinding) {
                // kick off loader for detailed stats
                getLoaderManager().restartLoader(LOADER_SUMMARY,
                        SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
            }
        }

        final long totalBytes = entry != null ? entry.rxBytes + entry.txBytes : 0;
        final String totalPhrase = formatFileSizeForUnit(context, totalBytes, sDisplayUnit);
        mCycleSummary.setText(totalPhrase);

        // initial layout is finished above, ensure we have transitions
        ensureLayoutTransitions();
    }

    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<
            ChartData>() {
                @Override
                public Loader<ChartData> onCreateLoader(int id, Bundle args) {
                    mAppSettings.setEnabled(false);
                    mIsChartDataBinding = true;
                    SLog.i("mChartDataCallbacks::onCreateLoader() mIsChartDataBinding = true");
                    return new ChartDataLoaderMultiSIM(getActivity(), mStatsSession, args);
                }

                @Override
                public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
                    mChartData = data;
                    mChart.bindNetworkStats(mChartData.network);
                    mChart.bindDetailNetworkStats(mChartData.detail);
                    SLog.i("onLoadFinished");
                    mAppSettings.setEnabled(true);
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

    @Deprecated
    private boolean isMobilePolicySplit() {
        final Context context = getActivity();
        if (hasReadyMobileRadio(context)) {
            final TelephonyManager tele = TelephonyManager.from(context);
            return mPolicyEditor.isMobilePolicySplit(getActiveSubscriberId(context));
        } else {
            return false;
        }
    }

    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<
            NetworkStats>() {
                @Override
                public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
                    mIsSummaryUidLoaderBinding = true;
                    return new SummaryForAllUidLoader(getActivity(), mStatsSession, args);
                }

                @Override
                public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
                    mIsSummaryUidLoaderBinding = false;
                    final int[] restrictedUids = mPolicyManager.getUidsWithPolicy(
                            POLICY_REJECT_METERED_BACKGROUND);
                    mAdapter.bindStats(data, restrictedUids);
                    updateEmptyVisible();
                }

                @Override
                public void onLoaderReset(Loader<NetworkStats> loader) {
                    mIsSummaryUidLoaderBinding = false;
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
    private void setMobilePolicySplit(boolean split) {
        final Context context = getActivity();
        if (hasReadyMobileRadio(context)) {
            final TelephonyManager tele = TelephonyManager.from(context);
            mPolicyEditor.setMobilePolicySplit(getActiveSubscriberId(context), split);
        }
    }

    private static String getActiveSubscriberId(Context context, int slotId) {
        int subscription = -1;
        if (slotId == SIM_SLOT_1) {
            subscription = SIM_SUBSCRIBE_1;
        } else if (slotId == SIM_SLOT_2) {
            subscription = SIM_SUBSCRIBE_2;
        } else if (slotId == SIM_SLOT_3) {
            subscription = SIM_SUBSCRIBE_3;
        }
        return SystemProperties.get(TEST_SUBSCRIBER_PROP, Utils.getSubscriberId(subscription));
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
            if (MDMDataUsageAdapter.getInstance().setDataUsageWarnToast(getActivity())) {
                return;
            }
            // LGMDM_END
            if (!getPopupdisable()) {
                setPopupdisable(true);
                WarningEditorFragment.show(DataUsageSummaryMultiSIM.this);
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
            if (MDMDataUsageAdapter.getInstance().setDataUsageLimitToast(getActivity())) {
                return;
            }
            // LGMDM_END
            if (!getPopupdisable()) {
                setPopupdisable(true);
                LimitEditorFragment.show(DataUsageSummaryMultiSIM.this);
            }
        }
    };

    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static final java.util.Formatter sFormatter = new java.util.Formatter(sBuilder,
            Locale.getDefault());

    public static String formatDateRange(Context context, long start, long end) {
        final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH; // KERRY

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

    public static class CycleChangeItem extends CycleItem {
        public CycleChangeItem(Context context) {
            super(context.getString(R.string.data_usage_change_cycle));
        }
    }

    /**
     * Special-case data usage cycle that triggers dialog to change
     * {@link NetworkPolicy#cycleDay}.
     */
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
     * Empty {@link Fragment} that controls display of UID details in
     * {@link DataUsageSummaryMultiSIM}.
     */
    public static class AppDetailsFragment extends Fragment {
        private static final String EXTRA_APP = "app";

        public static void show(DataUsageSummaryMultiSIM parent, AppItem app, CharSequence label) {
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
        }

        @Override
        public void onStart() {
            super.onStart();
            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
            target.mCurrentApp = getArguments().getParcelable(EXTRA_APP);
            target.updateBody();
        }

        @Override
        public void onStop() {
            super.onStop();
            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
            target.mCurrentApp = null;
            target.updateBody();
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#cycleDay}.
     */
    public static class CycleEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummaryMultiSIM parent) {
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
            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_cycle_editor, null, false);
            final NumberPicker cycleDayPicker = (NumberPicker)view.findViewById(R.id.cycle_day);
            TextView megaShortView = (TextView)view.findViewById(R.id.megabyteShort);
            if (megaShortView != null) {
                megaShortView.setText(com.android.internal.R.string.megabyteShort);
            }

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
     * Dialog to request user confirmation before setting
     * {@link NetworkPolicy#limitBytes}.
     */
    public static class ConfirmLimitFragment extends DialogFragment {
        private static final String EXTRA_MESSAGE = "message";
        private static final String EXTRA_LIMIT_BYTES = "limitBytes";

        public static void show(DataUsageSummaryMultiSIM parent) {
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
            final long minLimitBytes = (long)(policy.warningBytes * 1.2f);
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
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
            } else if (TAB_SIM_1.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
            } else if (TAB_SIM_2.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
            } else if (Utils.isTripleSimEnabled() && TAB_SIM_3.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_limit_dialog_mobile);
                limitBytes = Math.max(5 * GB_IN_BYTES, minLimitBytes);
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
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                    if (target != null) {

                        if (target.mPolicyEditor == null
                                || TAB_WIFI.equals(target.mCurrentTab)
                                || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                            SLog.i("ConfirmLimitFragment onclick " + target.mCurrentTab);
                            return;
                        }

                        final NetworkPolicyEditor editor = target.mPolicyEditor;
                        final NetworkTemplate template = target.mTemplate;
                        long warningBytes = editor.getPolicyWarningBytes(template);

                        if (true == "VZW".equals(Config.getOperator())
                                && WARNING_DISABLED == warningBytes) {
                            warningBytes = 2 * GB_IN_BYTES;
                        }
                        //long newValue = warningBytes + (warningBytes / 10);

                        target.setPolicyLimitBytes(limitBytes); //limitBytes --> newValue

                        if (true == "VZW".equals(Config.getOperator())) {
                            target.setPolicyWarningBytes(warningBytes);
                        }
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
     * Dialog to request user confirmation before disabling data.
     */
    public static class ConfirmDataDisableFragment extends DialogFragment {

        public static void show(DataUsageSummaryMultiSIM parent) {
            show(parent, 0);
        }

        public static void show(DataUsageSummaryMultiSIM parent, int slotId) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmDataDisableFragment dialog = new ConfirmDataDisableFragment();
            dialog.setTargetFragment(parent, (int)slotId);
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

            //[s][chris.won@lge.com][20121130] Sync popup_msg with Phone3
            if (true == "SKT".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_networks_disable_SKT_NORMAL);
                // [START_LGE_SETTINGS], MOD, kiseok.son 20121024 : TD#204630 Data network popup description for LGU+.
            } else if (true == "LGU".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_network_mode_disabled_desc_lgt_NORMAL);
                // [END_LGE_SETTINGS], MOD, kiseok.son 20121024 : TD#204630 Data network popup description for LGU+.
            } else if (true == "KT".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_networks_disabled_KT_NORMAL);
                //[e][chris.won@lge.com][20121130] Sync popup_msg with Phone3
            } else if (true == "DCM".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_enabled_uncheck_NORMAL);
            } else if (true == "VZW".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_mobile_data_off_VZW_NORMAL);
            } else {
                    strOk = R.string.yes;
                    strCancel = R.string.no;
                builder.setMessage(R.string.data_usage_disable_mobile);
            }

            builder.setPositiveButton(strOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                    int slotId = (int)getTargetRequestCode();
                    if (target != null) {
                        SLog.i("ConfirmDataDisableFragment::setPositiveButton");
                        if (slotId != 0) {
                            target.setMobileDataEnabled(false, slotId);
                        } else {
                            target.setMobileDataEnabled(false);
                        }
                        setPositiveResult(true);
                    }
                }
            });
            builder.setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                // KERRY START
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                    if (target != null) {
                        SLog.i("ConfirmDataDisableFragment::setNegativeButton");
                        setConfirmDialogShowed(false);
                        setPositiveResult(false);
                    }
                }
                // KERRY END
            });
            builder.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                        if (target != null) {
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

            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
            int slotId = (int)getTargetRequestCode();
            if (target != null) {
                // TODO: extend to modify policy enabled flag.

                aboidBlink = false;
                SLog.i("isPositiveResult()= " + isPositiveResult());

                setConfirmDialogShowed(false);
                setPositiveResult(false);
            }

            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            // [Settings] WBT issue fixed 0517
            if (target != null)
                target.updateBody();
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#warningBytes}.
     */
    public static class WarningEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
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
            final long limitBytes = editor.getPolicyLimitBytes(template);
            final long warningBytes = editor.getPolicyWarningBytes(template);

            int value = 0;
            bytesPicker.setMinValue(0);
            if (limitBytes != LIMIT_DISABLED) {
                value = (int)(limitBytes / MB_IN_BYTES) - 1;
            } else {
                value = 1024000;    //1000GB
            }
            
            if (0 > value) {
                value = 0;
            }
            
            bytesPicker.setValue((int)(warningBytes / MB_IN_BYTES));
            bytesPicker.setMaxValue(value);
            bytesPicker.setWrapSelectorWheel(false);
            builder.setTitle(R.string.data_usage_warning_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                            if (target != null) {
                                if (target.mPolicyEditor == null
                                        || TAB_WIFI.equals(target.mCurrentTab)
                                        || target.mPolicyEditor.getPolicy(target.mTemplate) == null) {
                                    SLog.i("WarningEditorFragment onclick " + target.mCurrentTab);
                                    return;
                                }
                            }
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

        public static void show(DataUsageSummaryMultiSIM parent) {
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
        public void onSaveInstanceState(Bundle outState) {

        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            setPopupdisable(false);
        }
    }

    /**
     * Dialog to request user confirmation before disabling data.
     */
    public static class ConfirmDataEnableFragment extends DialogFragment {

        public static void show(DataUsageSummaryMultiSIM parent) {
            show(parent, 0);
        }

        public static void show(DataUsageSummaryMultiSIM parent, int slotId) {

            if (!parent.isAdded()) {
                return;
            }

            final ConfirmDataEnableFragment dialog = new ConfirmDataEnableFragment();
            dialog.setTargetFragment(parent, (int)slotId);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_ENABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            int strOk = android.R.string.ok;
            int strCancel = android.R.string.cancel;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
                builder.setTitle(R.string.roaming_reenable_title);
            } else {
                builder.setTitle(R.string.data_usage_enable_mobile);
            }

            //[s][chris.won@lge.com][20121130] Sync popup_msg with Phone3
            if (true == "SKT".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_networks_enable_SKT_NORMAL);
                // [START_LGE_SETTINGS], MOD, kiseok.son 20121024 : TD#204630 Data network popup description for LGU+.
            } else if (true == "LGU".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_network_mode_enabled_desc_lgt_NORMAL);
                strOk = R.string.yes;
                strCancel = R.string.no;
                // [END_LGE_SETTINGS], MOD, kiseok.son 20121024 : TD#204630 Data network popup description for LGU+.
            } else if (true == "KT".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_networks_enabled_KT_NORMAL);
                //[e][chris.won@lge.com][20121130] Sync popup_msg with Phone3
            } else if (mIs_Ctc_Cn) {
                if (Config.isDataRoaming(sSim1SubId)) {
                    builder.setTitle(R.string.sp_dlg_note_NORMAL);
                    builder.setMessage(R.string.sp_mobile_data_enable_ct_roaming_normal);
                } else {
                    builder.setMessage(R.string.sp_mobile_network_data_connection_charges_NORMAL);
                }
            } else if (true == "DCM".equals(Config.getOperator())) {
                builder.setMessage(R.string.sp_data_enabled_check_NORMAL);
            }

            builder.setPositiveButton(strOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                    int slotId = (int)getTargetRequestCode();
                    if (target != null) {
                        // TODO: extend to modify policy enabled flag.
                        target.setMobileDataEnabled(true, slotId);
                        SLog.i("setMobileDataEnabled = " + slotId);
                        setPositiveResult(true);
                    }
                }
            });
            if (!(mIs_Ctc_Cn && Config.isDataRoaming(sSim1SubId))) {
                builder.setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    // KERRY START
                    public void onClick(DialogInterface dialog, int which) {
                        final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                        if (target != null) {
                            // TODO: extend to modify policy enabled flag.
                            setConfirmDialogShowed(false);
                            setPositiveResult(false);
                        }
                    }
                    // KERRY END
                });
            }
            builder.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                        if (target != null) {
                            // TODO: extend to modify policy enabled flag.
                            setConfirmDialogShowed(false);
                            setPositiveResult(false);
                        }
                    }
                    return false;
                }
            });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
            int slotId = (int)getTargetRequestCode();
            if (target != null) {
                // TODO: extend to modify policy enabled flag.

                aboidBlink = false;
                if (mIs_Ctc_Cn && Config.isDataRoaming(sSim1SubId)) {
                    target.setMobileDataEnabled(true, slotId);
                    SLog.i("setMobileDataEnabled = " + slotId);
                } else if (false == isPositiveResult()) {
                    target.setMobileDataEnabled(false);
                } else {
                    target.setMobileDataEnabled(true, slotId);
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
     * Dialog to inform user that {@link #POLICY_REJECT_METERED_BACKGROUND}
     * change has been denied, usually based on
     * {@link DataUsageSummaryMultiSIM#hasLimitedNetworks()}.
     */
    public static class DeniedRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
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
     * {@link INetworkPolicyManager#setRestrictBackground(boolean)}.
     */
    public static class ConfirmRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
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
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
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
     * Dialog to request user confirmation before setting
     * {@link #POLICY_REJECT_METERED_BACKGROUND}.
     */
    public static class ConfirmAppRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
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
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
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
     * Background task that loads {@link UidDetail}, binding to
     * {@link DataUsageAdapter} row item when finished.
     */
    private static class UidDetailTask extends AsyncTask<Void, Void, UidDetail> {
        private final UidDetailProvider mProvider;
        private final AppItem mItem;
        private final View mTarget;

        private UidDetailTask(UidDetailProvider provider, AppItem item, View target) {
            mProvider = checkNotNull(provider);
            mItem = checkNotNull(item);
            mTarget = checkNotNull(target);
        }

        public static void bindView(
                UidDetailProvider provider, AppItem item, View target) {
            final UidDetailTask existing = (UidDetailTask)target.getTag();
            if (existing != null) {
                existing.cancel(false);
            }

            final UidDetail cachedDetail = provider.getUidDetail(item.key, false);
            if (cachedDetail != null) {
                bindView(cachedDetail, target);
            } else {
                target.setTag(new UidDetailTask(provider, item, target).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR));
            }
        }

        private static void bindView(UidDetail detail, View target) {
            final ImageView icon = (ImageView)target.findViewById(android.R.id.icon);
            final TextView title = (TextView)target.findViewById(android.R.id.title);

            if (detail != null) {
                icon.setImageDrawable(detail.icon);
                title.setText(detail.label);
            } else {
                icon.setImageDrawable(null);
                title.setText(null);
            }
        }

        @Override
        protected void onPreExecute() {
            bindView(null, mTarget);
        }

        @Override
        protected UidDetail doInBackground(Void... params) {
            return mProvider.getUidDetail(mItem.key, true);
        }

        @Override
        protected void onPostExecute(UidDetail result) {
            bindView(result, mTarget);
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
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                String subscriber = template.getSubscriberId();
                if (subscriber == null) {
                    Log.e(TAG, "the subscriber error , null!");
                    return TAB_SIM_1;
                }
                if (subscriber.equals(getActiveSubscriberId(mContext,
                        SIM_SLOT_1))) {
                    return TAB_SIM_1;
                } else if (subscriber.equals(getActiveSubscriberId(mContext,
                        SIM_SLOT_2))) {
                    return TAB_SIM_2;
                } else if (Utils.isTripleSimEnabled()
                        && subscriber.equals(getActiveSubscriberId(mContext,
                                SIM_SLOT_3))) {
                    return TAB_SIM_3;
                } else {
                    Log.e(TAG, "the subscriber error , no mataching!");
                    return TAB_SIM_1;
                }
            } else {
                return TAB_MOBILE;
            }
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
        final TelephonyManager tele = TelephonyManager.getDefault();

        // require both supported network and ready SIM
        if (!Utils.isTripleSimEnabled()) {
            return conn.isNetworkSupported(TYPE_MOBILE)
                    &&
                    (tele.getSimState(SIM_SLOT_1) == SIM_STATE_READY || tele
                            .getSimState(SIM_SLOT_2) == SIM_STATE_READY);
        } else {
            return conn.isNetworkSupported(TYPE_MOBILE) &&
                    (tele.getSimState(SIM_SLOT_1) == SIM_STATE_READY ||
                            tele.getSimState(SIM_SLOT_2) == SIM_STATE_READY ||
                    tele.getSimState(SIM_SLOT_3) == SIM_STATE_READY);
        }
    }

    /**
     * Test if device has two or more mobile data radios with SIM in ready state.
     */
    public static boolean hasReadyMobileRadio(Context context, int slotId) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("mobile");
        }
        final ConnectivityManager conn = ConnectivityManager.from(context);
        final TelephonyManager simtel = TelephonyManager.getDefault();

        Log.d(TAG, "SIM STATE for slot " + Integer.toString(slotId) + " is "
                + Integer.toString(simtel.getSimState(slotId)));
        return conn.isNetworkSupported(TYPE_MOBILE)
                && simtel.getSimState(slotId) == SIM_STATE_READY;
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
     * Inflate a {@link Preference} style layout, adding the given {@link View}
     * widget into {@link android.R.id#widget_frame}.
     */
    private static View inflatePreference(LayoutInflater inflater, ViewGroup root, View widget) {
        final View view = inflater.inflate(R.layout.preference, root, false);
        final LinearLayout widgetFrame = (LinearLayout)view.findViewById(android.R.id.widget_frame);
        widgetFrame.addView(widget, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return view;
    }

    private static View inflateAppTitle(
            LayoutInflater inflater, ViewGroup root, CharSequence label) {
        final TextView view = (TextView)inflater
                .inflate(R.layout.data_usage_app_title, root, false);
        view.setText(label);
        return view;
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
     * Test if any networks are currently limited.
     */
    private boolean hasLimitedNetworks() {
        return !buildLimitedNetworksList().isEmpty();
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
        final String currentTab = mTabHost.getCurrentTabTag();

        final TelephonyManager tele = TelephonyManager.from(context);
        if (tele.getSimState() == SIM_STATE_READY) {
            final String subscriberId = getActiveSubscriberId(context);
            if (mMultiSimTM.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                //
                if (mHaveSim1Tab
                        && mPolicyEditor
                                .hasLimitedPolicy(buildTemplateMobileAll(getActiveSubscriberId(
                                        context, SIM_SLOT_1)))) {
                    limited.add(getText(R.string.data_usage_list_mobile));
                }
                if (mHaveSim2Tab
                        && mPolicyEditor
                                .hasLimitedPolicy(buildTemplateMobileAll(getActiveSubscriberId(
                                        context, SIM_SLOT_2)))) {
                    limited.add(getText(R.string.data_usage_list_mobile));
                }
                if (Utils.isTripleSimEnabled()
                        && mHaveSim3Tab
                        &&
                        mPolicyEditor
                                .hasLimitedPolicy(buildTemplateMobileAll(getActiveSubscriberId(
                                        context, SIM_SLOT_3)))) {
                    limited.add(getText(R.string.data_usage_list_mobile));
                }
                //
            } else if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobileAll(subscriberId))) {
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

    private static boolean contains(int[] haystack, int needle) {
        for (int value : haystack) {
            if (value == needle) {
                return true;
            }
        }
        return false;
    }

    // kerry - to sync with quick settings start
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            /*            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                            ConnectivityManager cm =
                                    (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                            // TODO: extend to modify policy enabled flag.
                            boolean enabled = cm.getMobileDataEnabled();

            // [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
                            if(enabled == true){
                                if(!mDataEnabled.isChecked())
                                    mDataEnabledChecked = true;
                            }else{
                                if(mDataEnabled.isChecked())
                                    mDataEnabledChecked = false;
                            }
            // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.

                            tm.setDataEnabled(enabled);
                            mMobileDataEnabled = enabled;
                            updatePolicy(false);

                            setConfirmDialogShowed(false);
                        }
                    else*/

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                boolean enabled = intent.getBooleanExtra("state", false);
                SLog.i("ACTION_AIRPLANE_MODE_CHANGED enabled= " + enabled);
                // [START_LGE_SETTINGS], MOD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.
                if (enabled == true) {
                    mDataEnabled.setEnabled(!enabled);
                    mDataEnabledView.setEnabled(!enabled);
                    mDisableAtLimit.setEnabled(!enabled);
                    mDisableAtLimitView.setEnabled(!enabled);
                    mNetworkSwitches.setEnabled(!enabled);
                } else {
                    mDataEnabled.setEnabled(!enabled);
                    mDataEnabledView.setEnabled(!enabled);
                    mDisableAtLimit.setEnabled(!enabled);
                    mDisableAtLimitView.setEnabled(!enabled);
                    mNetworkSwitches.setEnabled(!enabled);
                }
                // [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-16, LU6800 TD#118124 : Airplane mode & data connection display error.

                if (true == enabled) {
                    TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                    ev.setTextColor(Color.GRAY);

                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.GRAY);
                }
                else {
                    TextView ev = (TextView)mDataEnabledView.findViewById(android.R.id.title);
                    ev.setTextColor(Color.BLACK);

                    TextView lv = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
                    lv.setTextColor(Color.BLACK);
                } // kerry

                updatePolicy(false);

                setConfirmDialogShowed(false);
            }
            //If in callings, DataEnabled preperence is dimmed
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                if ("DCM".equals(Config.getOperator())) {
                    SLog.i("ACTION_PHONE_STATE_CHANGED :: DCM ");
                    if ((mTelephonyManager != null)
                            && false == isAirplaneModeOn(getActivity())
                            && (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE)) {
                        SLog.i("ACTION_PHONE_STATE_CHANGED :: tm.getCallState() = "
                                + mTelephonyManager.getCallState());
                        if (mDataEnabled != null && mDataEnabledView != null) {
                            mDataEnabled.setEnabled(true);
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
    };

    // kerry
    public static boolean isAirplaneModeOn(Context context) {
        if (null == context) {
            return false;
        }
        Log.d("Airplanemodeenabler", "isAirplaneModeOn");

        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    // kerry - to sync with quick settings end
    private static void setPopupdisable(boolean popupdisable) {
        mPopupdisable = popupdisable;
    }

    private int getSimStatus(int slotId) {
        if (slotId == SIM_SLOT_1) {
            return mSim1Stat;
        } else if (slotId == SIM_SLOT_2) {
            return mSim2Stat;
        } else if (Utils.isTripleSimEnabled() && slotId == SIM_SLOT_3) {
            return mSim3Stat;
        } else {
            return 0;
        }
    }

    private static boolean getPopupdisable() {
        return mPopupdisable;
    }

    private Handler timerHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_DETACH_TIME_OUT:
            case EVENT_ATTACH_TIME_OUT:
                //mDataEnabled.setEnabled(true);
                mIsSwitching = false;
                updateBody();
                //updatePolicy(true);
                break;
            default:
                break;
            }
        }
    };

    public static class ConfirmLGURestrictDisableFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
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
                                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();

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
                                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();

                                    target.setRestrictBackground(true);
                                }
                            });
            if (false == Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            return builder.create();
        }
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-167][ID-MDM-198][ID-MDM-164][ID-MDM-236]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM == false) {
                return;
            }

            if (MDMDataUsageAdapter.getInstance().isDataUsageSettingPolicyChanged(intent) == false) {
                return;
            }

            Activity activity = getActivity();

            if (activity == null) {
                return;
            }

            activity.finish();
        }
    };
    // LGMDM_END

    public static class ConfirmLGURestrictEnableFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
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
                                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();

                                    target.setRestrictBackground(false);
                                }
                            })
                    .setPositiveButton(
                            R.string.sp_data_roaming_background_data_popup_button_text_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();

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
    //[e][chris.won@lge.com][2013-03-18] Chamelion

    private void setDataUsageReset() {
        SLog.i("setDataUsageReset :: reset the mobile data record.");
        try {
            if (true == mStatsServiceEx.clearUsage(mTemplate)) {
                Log.d(TAG, "clearUsage success");
            } else {
                Log.d(TAG, "clearUsage failed");
            }                        
        } catch (Exception e) {
            SLog.d("ignoring monkey's attempt to reset the data usage record");
        }
        updateBody();
    }

    //[s][chris.won@lge.com][2013-03-18] Chamelion
    private boolean checkChamelionRoamingDisable() {
        int Chamelion_display = 1;
        // [START_LGE_VOICECALL] , MOD, euijoong.kim , 2012-08-26 , chameleon full dummy 123456 case add.
        final String Operation_Numeric_Sprint = "310120";
        final String Operation_Numeric_Boost = "311870";
        final String Operation_Numeric_Virgin = "311490";
        final String Operation_Numeric_Wholesale = "000000";
        final String Operation_Numeric_Full_Dummy = "123456";

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
                    Chamelion_display = Settings.System.getInt(mContext.getContentResolver(),
                            SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 0);
                    Log.d(TAG, "BM VG Chamelion_display = " + Chamelion_display);
                } else if (Operation_Alpha_Sprint.equalsIgnoreCase(operator_alpha)) {
                    Chamelion_display = Settings.System.getInt(mContext.getContentResolver(),
                            SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 1);
                    Log.d(TAG, "SPR Chamelion_display = " + Chamelion_display);
                } else {
                    Chamelion_display = Settings.System.getInt(mContext.getContentResolver(),
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

    public static class ConfirmDataUsageResetFragment extends DialogFragment {
        public static void show(DataUsageSummaryMultiSIM parent) {
            show(parent, 0);
        }

        public static void show(DataUsageSummaryMultiSIM parent, int slotId) {
            if (!parent.isAdded()) {
                return;
            }
            final ConfirmDataUsageResetFragment dialog = new ConfirmDataUsageResetFragment();
            dialog.setTargetFragment(parent, slotId);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_USAGE_RESET);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.sp_reset_datausage_title);
            builder.setMessage(R.string.sp_reset_data_usage_msg);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
                    if (target != null) {
                        target.setDataUsageReset();
                    }
                }
            });
            builder.setNegativeButton(R.string.no, null);
            return builder.create();
        }
    }

    private boolean isCurrentWiFiTab() {
        SLog.i("isCurrentWiFiTab mCurrentTab =  " + mCurrentTab);
        return mPolicyEditor == null || TAB_WIFI.equals(mCurrentTab) || mPolicyEditor.getPolicy(mTemplate) == null;
    }

    private void sendBroadcastDdsForMMS(boolean mValue) {
        Intent intent = new Intent();
        intent.setAction("com.lge.mms.intent.action.switch_dds");
        intent.putExtra("data_status", mValue);
        SLog.d("sendBroadcastDdsForMMS mValue " + mValue);
        getActivity().sendBroadcast(intent);
    }

    static float sResult;
    static boolean sIsSmallValue = false;
    static int suffix = com.android.internal.R.string.byteShort;

    private static String formatFileSizeForUnit(Context context, long number, boolean unit_type) {
        sIsSmallValue = false;
        suffix = com.android.internal.R.string.byteShort;

        if (context == null) {
            return "";
        }
        final int mDISPLAY_UNIT_KB_CNT = 0x1;
        final int mDISPLAY_UNIT_MB_CNT = 0x2;
        final int mDISPLAY_UNIT_GB_CNT = 0x3;
        int mUnitCount = 0;
        sResult = number;
        if (sResult > 900) {
            suffix = com.android.internal.R.string.kilobyteShort;
            sResult = sResult / 1024;
            mUnitCount++;
        }
        if (sResult > 900) {
            suffix = com.android.internal.R.string.megabyteShort;
            sResult = sResult / 1024;
            mUnitCount++;
        }
        if (sResult > 900
                && true == unit_type) {
            suffix = com.android.internal.R.string.gigabyteShort;
            sResult = sResult / 1024;
            mUnitCount++;
        }
        formatFileSizeForUnitUnitType(unit_type, mDISPLAY_UNIT_KB_CNT, mDISPLAY_UNIT_MB_CNT,
                mDISPLAY_UNIT_GB_CNT, mUnitCount);
        String value;
        if (sIsSmallValue) {
            float temp = 0.01f;
            value = "< " + String.format("%.2f", temp);
        } else if (sResult == 0) {
            int temp = 0;
            value = String.format("%d", temp);
        } else if (sResult < 1) {
            value = String.format("%.2f", sResult);
        } else if (sResult < 10) {
            value = String.format("%.2f", sResult);
        } else if (sResult < 100) {
            value = String.format("%.2f", sResult);
        } else {
            value = String.format("%.0f", sResult);
        }

        return context.getResources().
                getString(com.android.internal.R.string.fileSizeSuffix,
                        value, context.getString(suffix));
    }

    private static void formatFileSizeForUnitUnitTypeGB(
            final int mDISPLAY_UNIT_MB_CNT, final int mDISPLAY_UNIT_GB_CNT,
            int mUnitCount) {
        if (mUnitCount < mDISPLAY_UNIT_GB_CNT) {
            suffix = com.android.internal.R.string.gigabyteShort;
            if (mUnitCount <= mDISPLAY_UNIT_MB_CNT) {
                if (mUnitCount == mDISPLAY_UNIT_MB_CNT) {
                    sResult = sResult / 1024;
                    if (0 < sResult
                            && sResult < 0.01) {
                        sIsSmallValue = true;
                    }
                } else {
                    if (0 < sResult) {
                        sIsSmallValue = true;
                    }
                }
            }
        }
    }

    private static void formatFileSizeForUnitUnitType(boolean unit_type,
            final int mDISPLAY_UNIT_KB_CNT, final int mDISPLAY_UNIT_MB_CNT,
            final int mDISPLAY_UNIT_GB_CNT, int mUnitCount) {
        if (unit_type) {
            formatFileSizeForUnitUnitTypeGB(mDISPLAY_UNIT_MB_CNT,
                    mDISPLAY_UNIT_GB_CNT, mUnitCount);
        } else {
            formatFileSizeForUnitUnitTypeMB(mDISPLAY_UNIT_KB_CNT,
                    mDISPLAY_UNIT_MB_CNT, mUnitCount);
        }
    }

    private static void formatFileSizeForUnitUnitTypeMB(
            final int mDISPLAY_UNIT_KB_CNT, final int mDISPLAY_UNIT_MB_CNT,
            int mUnitCount) {
        if (mUnitCount < mDISPLAY_UNIT_MB_CNT) {
            suffix = com.android.internal.R.string.megabyteShort;
            if (mUnitCount <= mDISPLAY_UNIT_KB_CNT) {
                if (mUnitCount == mDISPLAY_UNIT_KB_CNT) {
                    sResult = sResult / 1024;
                    if (0 < sResult
                            && sResult < 0.01) {
                        sIsSmallValue = true;
                    }
                } else {
                    if (0 < sResult) {
                        sIsSmallValue = true;
                    }
                }
            }
        }
    }

    /**
     * Update UID details panels to match {@link #mCurrentApp}, showing or
     * hiding them depending on {@link #isAppDetailMode()}.
     */
    private void updateAppDetail() {
        final Context context = getActivity();
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final PackageManager pm = context.getPackageManager();

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
                TextView appTitle = (TextView)title.findViewById(R.id.app_title);
                appTitle.setText(label);
                appTitle.setContentDescription(contentDescription);
                mAppTitles.addView(title);
            }
        } else {
            title = inflater.inflate(R.layout.data_usage_app_title, mAppTitles, false);
            TextView appTitle = (TextView)title.findViewById(R.id.app_title);
            appTitle.setText(detail.label);
            appTitle.setContentDescription(detail.contentDescription);
            mAppTitles.addView(title);
        }

        // Remember last slot for summary
        if (title != null) {
            mAppTotal = (TextView)title.findViewById(R.id.app_summary);
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

    /**
     * Rebuild {@link #mCycleAdapter} based on {@link NetworkPolicy#cycleDay}
     * and available {@link NetworkStatsHistory} data. Always selects the newest
     * item, updating the inspection range on {@link #mChart}.
     */
    long historyEnd = Long.MIN_VALUE;
    long historyStart = Long.MAX_VALUE;
    private void updateCycleList(NetworkPolicy policy) {
        // stash away currently selected cycle to try restoring below
        final CycleItem previousItem = (CycleItem)mCycleSpinner.getSelectedItem();
        final Context context = mCycleSpinner.getContext();
        mIsUpdateCycle = true; //[chris.won@lge.com][2013-01-07] Cycle item ghostly selected issue.

        mCycleAdapter.clear();

        historyEnd = Long.MIN_VALUE;
        historyStart = Long.MAX_VALUE;
        if (mChartData != null) {
            historyStart = mChartData.network.getStart();
            historyEnd = mChartData.network.getEnd();
        }
        Log.d(TAG, "Usage Clycle List historyEnd = " + historyEnd);
        Log.d(TAG, "Usage Clycle List historyStart = " + historyStart);

        initCycleHostory();
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


    private void initCycleHostory() {
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

        public DataUsageAdapter(final UserManager userManager,
                UidDetailProvider provider, int insetSide) {
            mProvider = checkNotNull(provider);
            mInsetSide = insetSide;
            mUm = userManager;
        }

        /**
         * Accumulate data usage of a network stats entry for the item mapped by
         * the collapse key. Creates the item if needed.
         *
         * @param collapseKey
         *            the collapse key used to map the item.
         * @param knownItems
         *            collection of known (already existing) items.
         * @param entry
         *            the network stats entry to extract data usage from.
         * @param itemCategory
         *            the item is categorized on the list view by this category.
         *            Must be either AppItem.APP_ITEM_CATEGORY or
         *            AppItem.MANAGED_USER_ITEM_CATEGORY
         */
        private void accumulate(int collapseKey,
                final SparseArray<AppItem> knownItems,
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

        /**
         * Bind the given {@link NetworkStats}, or {@code null} to clear list.
         */
        public void bindStats(NetworkStats stats, int[] restrictedUids) {
            mLargest = 0;
            mItems.clear();

            final List<UserHandle> profiles = mUm.getUserProfiles();
            final int currentUserId = ActivityManager.getCurrentUser();
            final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();

            NetworkStats.Entry entry = null;
            final int size = stats != null ? stats.size() : 0;
            for (int i = 0; i < size; i++) {
                entry = stats.getValues(i, entry);

                // Decide how to collapse items together
                final int collapseKey;
                final int uid = entry.uid;
                final int userId = UserHandle.getUserId(uid);
                final int category;
                if (UserHandle.isApp(uid)) {
                    if (profiles.contains(new UserHandle(userId))) {
                        if (userId != currentUserId) {
                            // Add to a managed user item.
                            final int managedKey = UidDetailProvider
                                    .buildKeyForUser(userId);
                            accumulate(managedKey, knownItems, entry,
                                    AppItem.CATEGORY_USER);
                        }
                        // Add to app item.
                        category = AppItem.CATEGORY_APP;
                        collapseKey = uid;
                    } else {
                        // Add to other user item.
                        collapseKey = UidDetailProvider.buildKeyForUser(userId);
                        category = AppItem.CATEGORY_USER;
                    }
                } else if (uid == UID_REMOVED || uid == UID_TETHERING) {
                    category = AppItem.CATEGORY_APP;
                    collapseKey = uid;
                } else {
                    category = AppItem.CATEGORY_APP;
                    collapseKey = android.os.Process.SYSTEM_UID;
                }
                accumulate(collapseKey, knownItems, entry, category);
            }

            final int restrictedUidsMax = restrictedUids.length;
            for (int i = 0; i < restrictedUidsMax; ++i) {
                final int uid = restrictedUids[i];
                // Only splice in restricted state for current user or managed
                // users
                if (!profiles
                        .contains(new UserHandle(UserHandle.getUserId(uid)))) {
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

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        /**
         * See {@link #getItemViewType} for the view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).key;
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
        public boolean isEnabled(int position) {
            if (position > mItems.size()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return getItemViewType(position) == 0;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppItem item = mItems.get(position);
            if (getItemViewType(position) != AppItem.CATEGORY_APP_TITLE) {
                AppViewHolder holder = AppViewHolder.createOrRecycle(
                        LayoutInflater.from(parent.getContext()), convertView);
                convertView = holder.rootView;

                if (mInsetSide > 0) {
                    convertView
                            .setPaddingRelative(mInsetSide, 0, mInsetSide, 0);
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
                    holder.text1.setText(formatFileSizeForUnit(
                            parent.getContext(), item.total, sDisplayUnit));
                    holder.progress.setVisibility(View.VISIBLE);
                }

                final int percentTotal = mLargest != 0 ? (int)(item.total * 100 / mLargest)
                        : 0;
                holder.progress.setProgress(percentTotal);
            }

            return convertView;
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#limitBytes}.
     */
    public static class LimitEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummaryMultiSIM parent) {
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
            dialog.setTargetFragment(parent, 0);
            dialog.setArguments(args);
            dialog.show(parent.getFragmentManager(), TAG_LIMIT_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
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
            bytesPicker.setWrapSelectorWheel(false);
            bytesPicker.setValue((int)(limitBytes / MB_IN_BYTES));
            builder.setTitle(R.string.data_usage_limit_editor_title);
            builder.setView(view);
            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final DataUsageSummaryMultiSIM target = (DataUsageSummaryMultiSIM)getTargetFragment();
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

        //[s][chris.won@lge.com][2012-12-04] Due to Android Bug
        @Override
        public void onSaveInstanceState(Bundle outState) {

        }
        //[e][chris.won@lge.com][2012-12-04] Due to Android Bug
    }
}
