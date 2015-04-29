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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import com.android.settings.search.Index;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManagerGlobal;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.util.ArrayUtils;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.applications.AccessLockDetails;
import com.android.settings.applications.AccessLockSummary;
import com.android.settings.applications.AppOpsSummary;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.applications.ManageApplications;
import com.android.settings.applications.ProcessStatsUi;
import android.content.res.TypedArray;
import com.android.settings.bluetooth.BluetoothEnabler;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.deviceinfo.UsbSettings;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.inputmethod.KeyboardLayoutPickerFragment;
import com.android.settings.inputmethod.SpellCheckersSettings;
import com.android.settings.inputmethod.UserDictionaryList;
import com.android.settings.lge.DeviceInfoLgeDSim;
import com.android.settings.lge.EmotionalLEDEffectTabFront;
import com.android.settings.lge.DeviceInfoLgeTSim;
import com.android.settings.lge.QuickCoverView;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.location.LocationSettings;

import com.android.settings.nfc.AndroidBeam;
//import com.android.settings.nfc.PaymentSettings;
import com.android.settings.notification.NotificationAppList;
import com.android.settings.notification.RedactionInterstitial.RedactionInterstitialFragment;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.powersave.BatterySaverTips;
import com.android.settings.print.PrintJobSettingsFragment;
import com.android.settings.print.PrintServiceSettingsFragment;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.remote.LGPreferenceActivity;
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.remote.RemoteFragmentManager.RemoteActionInfo;
import com.android.settings.remote.RemoteLayoutInflater;
import com.android.settings.remote.SettingsMenu;
import com.android.settings.tts.TextToSpeechSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.vpn2.VpnSettings;
import com.android.settings.wifi.WifiCallingEnabler;
import com.android.settings.wifi.WifiEnabler;
import com.android.settings.wifi.wifiscreen.WifiScreenEnabler;
import com.lge.constants.SettingsConstants;
import com.lge.nfcaddon.NfcAdapterAddon;

/**
 * Top-level settings activity to handle single pane and double pane UI layout.
 */
public class Settings extends LGPreferenceActivity implements ButtonBarHandler,
		OnAccountsUpdateListener {

    private static final String LOG_TAG = "Settings";

    // show Back and Next buttons? takes boolean parameter
    // Back will then return RESULT_CANCELED and Next RESULT_OK
    protected static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";

    // specify custom text for the Back or Next buttons, or cause a button to not appear
    // at all by setting it to null
    protected static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    protected static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";

    static final String CONTENT_URI_SEARCH = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";
    private static final String META_DATA_KEY_HEADER_ID =
            "com.android.settings.TOP_LEVEL_HEADER_ID";
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
            "com.android.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE =
            "com.android.settings.PARENT_FRAGMENT_TITLE";
    private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
            "com.android.settings.PARENT_FRAGMENT_CLASS";

    private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";

    private static final String SAVE_KEY_CURRENT_HEADER = "com.android.settings.CURRENT_HEADER";
    private static final String SAVE_KEY_PARENT_HEADER = "com.android.settings.PARENT_HEADER";
    private static final String DOCOMO_SERVICE = "com.nttdocomo.android.docomoset";
    private static final String MULTITASKING_CLASSNAME =
            "com.android.settings.Settings$MultiTaskingSettingsActivity";
    private static final String CONTENT_URI_BB = "content://com.lge.ims.rcs/device";
    private static final String CONTENT_URI = "content://com.lge.ims.provisioning/workings";
    private static final String SHOW_RCS = "show_settings";
    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;
    private boolean mBreadCrumbTitleDisplayedFlag;
    private static boolean mIsWifiOnly = false; //[chris.won@lge.com][2013-05-30] Set boolean code for access in static class
    private static boolean sIsSubUserOnly;
    
    private Dialog mInitGuideDialog;
    private CheckBox mInitChecked;
    private boolean mIsDialogCheck = false;
    
    // Wine Flip Close
    AlertDialog mFlipSettingDlg;

    public static final int SETTING_STYLE_ITEM_ID = 0x1234; // This should not be zero
    // Show only these settings for restricted users
    private int[] SETTINGS_FOR_RESTRICTED = {
            R.id.au_settings_section,
            R.id.au_settings,
            R.id.wireless_section,
            R.id.airplane_mode_settings,
            R.id.wifi_settings,
            R.id.bluetooth_settings,
            R.id.data_usage_settings,
            R.id.use_4g_network,
            R.id.wireless_settings,
            R.id.tether_network_settings,
            R.id.wireless_more_settings,
            R.id.share_connect,
            R.id.device_section,
            R.id.sound_settings,
            R.id.display_settings,
            R.id.settings_multitasking,
            R.id.storage_settings,
            R.id.application_settings,
            R.id.battery_settings,
            R.id.sms_default_settings,
            R.id.personal_section,
            R.id.location_settings,
            R.id.security_settings,
            R.id.block_mode_settings,
            R.id.language_settings,
            R.id.user_settings,
            R.id.account_settings,
            R.id.account_add,
            R.id.account_main_settings,
            R.id.system_section,
            R.id.date_time_settings,
            R.id.connectivity_settings,
            R.id.usb_settings,
            R.id.about_settings,
            R.id.about_settings_lge,
            R.id.accessibility_settings,
            R.id.accessibility_settings_lge,
            R.id.ifttt_settings,
            R.id.accessory_settings,
            R.id.cloud,
            R.id.menu_settings,
            R.id.home_settings,
            R.id.lock_settings,
            R.id.cover_settings,
            R.id.nfc_payment_settings // rebestm - kk migration
    };

    private SharedPreferences mDevelopmentPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();

    private AuthenticatorHelper mAuthenticatorHelper;
    private Header mLastHeader;
    private boolean mListeningToAccountUpdates;

    public static final String GESTURE_ALL_EDITOR = "gesture_all_editor";
    private static final int TYPE_SENSOR_LGE_GESTURE_FACING = 23;
    private static final int SETTINGS_SEARCH_ID = 77;
    private static final int ORIENTATION = 3;
    private static final int TYPE_ACCELEROMETER = 1;
    private boolean HASFACING_SENSOR = false;
    private boolean hasOrient_SENSOR = false;
    private boolean hasAccelerometer_SENSOR = false;

    IntentFilter mNFCIntentFilter; // rebestm - tap&pay
    TelephonyManager mPhone;

    private static Header mRCSHeader = null; // yonguk.kim 20121109 Add RCS menu dynamically
    ListView customList;

    @Override
    public void onBackPressed() {
        try {
            if (Utils.isTopActivity("com.android.settings.Settings$ApnSettingsActivity")) {
                // task stack is not settings stack,
                // so it should be to call the mobile network setting.
                SLog.i("ApnSettings onBackPressed call previous state");
                Intent mIntent = new Intent();
                if ("SPR".equals(Config.getOperator())) {
                    mIntent.setClassName("com.android.phone",
                        "com.android.phone.CallFeatureSettingRoaming");
                } else if ("DCM".equals(Config.getOperator())) {
                    finish();
                    return;
                } else {
                    mIntent.setClassName("com.lge.networksettings",
                        "com.lge.networksettings.MobileNetworkSettings");
                }
                startActivity(mIntent);
                finish();
                return;
            }
            super.onBackPressed();
        } catch (IllegalStateException e) {

        }
    }

    private BroadcastReceiver mFinshMainUIReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.lge.FINISH_MAIN_UI".equals(action)) {
                Log.d("Settings", "com.lge.FINISH_MAIN_UI Got it~~!!");
                Settings.this.finish();
            }
        }

    };
    // rebestm - tap&pay
    private final BroadcastReceiver mNFCReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NfcAdapterAddon.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                if (Settings.this.hasHeaders()) {
                    Settings.this.invalidateHeaders();
                }
            }
        }
    };
    
    private static final int REQUEST_CODE_EXIT_ECM = 1;
    private BroadcastReceiver mAirplaneECMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM action :" + action);
            if ("AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM".equals(action)) {
                Log.d(LOG_TAG, "AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM Received");
                startActivityForResult(new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null), REQUEST_CODE_EXIT_ECM);
            }
        }
    };    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult-requestCode : " + requestCode);
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            boolean isInEcmValue = Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE));
            Log.d(LOG_TAG, "isInEcmValue : " + isInEcmValue);
            if (isInEcmValue == false) {
                ListAdapter listAdapter = getListAdapter();
                if ((listAdapter != null) && (listAdapter instanceof HeaderAdapter)) {
                    ((HeaderAdapter)listAdapter).updateAirplaneModeEnablerInECM();
                }
            }
        }
    }
    
    // yonguk.kim 20130409 Slide aside featuring
    public static final boolean SUPPORT_SLIDEASIDE = SystemProperties.getBoolean("ro.lge.capp_slideAside", false);
    public static final boolean SUPPORT_DUAL_WINDOW =
            SystemProperties.getBoolean("ro.lge.capp_splitwindow", false);
    private static final boolean CAPP_SLIDEASIDE = false;
    SettingsBreadCrumb mBreadCrumb;

    // for remote fragment loading
    public static Resources mMyOwnResources;
    private UserManager mUm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPhone = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
		setPerformOnCreate(true);

		/* ensure own resource instance */
        mMyOwnResources = super.getResources();
        Config.sContext = this;

		setupRemoteFragmentInfo(getIntent());

        if (Utils.supportSplitView(getApplicationContext())) {
            if (getActionBar() != null) {
                getActionBar().setTitle(R.string.settings_label);
                getActionBar().setIcon(R.mipmap.ic_launcher_settings);
            }
        }

        if (getIntent().getBooleanExtra(EXTRA_CLEAR_UI_OPTIONS, false)) {
            getWindow().setUiOptions(0);
        }


        //[s][chris.won@lge.com][2013-05-30] Set boolean code for access in static class
        mIsWifiOnly = Utils.isWifiOnly(this);
        //[e][chris.won@lge.com][2013-05-30] Set boolean code for access in static class
        mUm = (UserManager)getSystemService(Context.USER_SERVICE);
        UserInfo userInfo = mUm.getUserInfo(UserHandle.myUserId());
        sIsSubUserOnly = UserHandle.myUserId() != UserHandle.USER_OWNER;
        mAuthenticatorHelper = new AuthenticatorHelper(this, userInfo.getUserHandle(), mUm);
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, null);

        mDevelopmentPreferences = getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE);

        if (mRemoteManager == null) {
            mRemoteManager = new RemoteFragmentManager(this);
            mRemoteManager.checkInstalledPackagesForRemoteLoading(this,
                    super.getClassLoader());
        }
        
        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        customList = (ListView)findViewById(android.R.id.list);
        customList.setPaddingRelative(0, 0, 0, 0);

        mBreadCrumb = new SettingsBreadCrumb(this); 
        if (onIsMultiPane() && needBreadCrumb()) {
            mBreadCrumb.attach();
            if (mFragmentClass != null) {
                showBreadCrumbs(getTitle(), null);
                mBreadCrumbTitleDisplayedFlag = true;
            }
        }

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader(mTopLevelHeaderId);
            // Force the title so that it doesn't get overridden by a direct launch of
            // a specific settings screen.
            setTitle(R.string.settings_label);
            if (getActionBar() != null) {
                getActionBar().setIcon(R.mipmap.ic_launcher_settings);
            }
        }

        // Retrieve any saved state
        if (savedInstanceState != null) {
            mCurrentHeader = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_HEADER);
            mParentHeader = savedInstanceState.getParcelable(SAVE_KEY_PARENT_HEADER);
        }

        // If the current header was saved, switch to it
        if (savedInstanceState != null && mCurrentHeader != null) {
            //switchToHeaderLocal(mCurrentHeader);
            showBreadCrumbs(mCurrentHeader.title, null);
        }
        mNFCIntentFilter = new IntentFilter(NfcAdapterAddon.ACTION_ADAPTER_STATE_CHANGED); // rebestm - tap&pay
        if (mParentHeader != null) {
            setParentTitle(mParentHeader.title, null, new OnClickListener() {
                public void onClick(View v) {
                    switchToParent(mParentHeader.fragment);
                }
            });
        }

        // 2013-08-19 Fix upkey action of Home Settings in split view environment [START]
        if (onIsMultiPane() == true) {
            String shortcutExtra = (String)getIntent().getExtra(Intent.EXTRA_TEXT);

            if (shortcutExtra != null && "display".equalsIgnoreCase(shortcutExtra)) {
                Log.d("kimyow", "shortcutExtra:" + shortcutExtra);
                List<Header> headers = getHeaders();
                for (Header header : headers) {
                    if (header.id == R.id.display_settings) {
                        switchToHeader(header);
                        break;
                    }
                }
            }
        }
        // 2013-08-19 Fix upkey action of Home Settings in split view environment [END]

        // [2012.1.19][changyu0218.lee@lge.com][U0][123621] fix accessibility pop up issue
        android.provider.Settings.System.putInt(getContentResolver(), "LargeText_Flag", 0);

        if (onIsMultiPane()) {
            if (getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setHomeButtonEnabled(false);
            }
        }

        if (savedInstanceState == null && ("VZW".equals(Config.getOperator())
                && Utils.supportEasySettings(this))) {
            if (!Utils.isUI_4_1_model(this)) {
                showSettingsTipEasy();
            }
        }

        //yonguk.kim 2013-12-18 Apply split view divider for Tablet model(V500)
        if (onIsMultiPane()) {
            setSplitViewDivider();
        }


        // rebestm - WFC
        if( "1".equals(SystemProperties.get("ro.chameleon.sprintwificall", "0")) ) { //ro property
            SharedPreferences preferences = this.getSharedPreferences("WFC_toast", 0);
            if ( preferences.getInt("WFC_toast", 0) == 0 ) {
                preferences.edit().putInt("WFC_toast", 111).commit(); // first
            }
        }

        setPerformOnCreate(false);
        setRegisterReceiverSettings();
    }
    
    private void setRegisterReceiverSettings() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lge.FINISH_MAIN_UI");
        registerReceiver(mFinshMainUIReceiver, filter);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195][ID-MDM-35][ID-MDM-191][ID-MDM-167]
        // [ID-MDM-198][ID-MDM-52][ID-MDM-280][ID-MDM-342][ID-MDM-343][ID-MDM-344]
        // [ID-MDM-345][ID-MDM-346][ID-MDM-347][ID-MDM-424]

        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
            MDMSettingsAdapter.getInstance().addMainSettingsPolicyChangeIntentFilter(
                    filterLGMDM);
            registerReceiver(mLGMDMReceiver, filterLGMDM);
        }

        // LGMDM_END
        if (Config.supportAirplaneListMenu() && !Utils.isTablet()) {
            Log.d(LOG_TAG, "AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM Receiver registerReceiver");
            IntentFilter airplaneModeFilter = new IntentFilter("AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM");
            registerReceiver(mAirplaneECMReceiver, airplaneModeFilter);
        }
    }    

	@Override
	public boolean onIsMultiPane() {
		return (super.onIsMultiPane() || mMyOwnResources
				.getBoolean(R.bool.lg_preferences_prefer_dual_pane));
	}

    // yonguk.kim 2013-12-18 Apply split view divider for Tablet model(V500) [START]
    private void setSplitViewDivider() {
        if (findViewById(com.android.internal.R.id.headers).getVisibility() == View.GONE) {
            return ;
        }
        
        ViewGroup prefs_frame = (ViewGroup)findViewById(com.android.internal.R.id.prefs_frame);
        LinearLayout header_layout = (LinearLayout)findViewById(com.android.internal.R.id.headers);
        if (prefs_frame != null && header_layout != null) {
            ViewGroup parent = (ViewGroup)prefs_frame.getParent();
            if (parent != null) {
                android.widget.LinearLayout divider = new android.widget.LinearLayout(this);
                divider.setBackgroundResource(R.drawable.list_divider_bg_normal);
                android.widget.LinearLayout.LayoutParams params =
                        new android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

                parent.addView(divider, 1, params);
            }
            android.widget.LinearLayout.LayoutParams header_params =
                    (LayoutParams)header_layout.getLayoutParams();
            header_params.rightMargin = convertDpToPixel(-6);
            header_layout.setLayoutParams(header_params);
        }

        // yonguk.kim 20140224 Remove native breadcrumb section
        View bcSection = findViewById(com.android.internal.R.id.breadcrumb_section);
        if (bcSection != null) {
            bcSection.setVisibility(View.GONE);
        }
    }

    private int convertDpToPixel(double dp) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = (float)dp * (metrics.densityDpi / 160f);
        return (int)px;
    }
    // yonguk.kim 2013-12-18 Apply split view divider for Tablet model(V500) [END]
    public SettingsBreadCrumb getBreadCrumb() {
        return mBreadCrumb;
    }

    @Override
    public void showBreadCrumbs(CharSequence title, CharSequence shortTitle) {
        //if (mBreadCrumbTitleDisplayedFlag == true) {
        //    return;
        //}
        if (mBreadCrumb != null) {
            mBreadCrumb.showBreadCrumb(title, shortTitle);
        } else {
            if (Utils.supportRemoteFragment(this) == false) {
                super.showBreadCrumbs(title, shortTitle);
            }
        }
    }

    // [S][2013.08.01][eunjungjudy.kim@lge.com] check need BreadCrumb or not
    public boolean needBreadCrumb() {
        Intent intent = getIntent();
        if ("com.android.settings.Settings$WifiSettingsDialogActivity".equals(intent.getComponent().getClassName())
                || "com.android.settings.Settings$WifiScreenSettingsDialogActivity".equals(intent.getComponent().getClassName())) {
            return false;
        }
        
        if (findViewById(com.android.internal.R.id.headers).getVisibility() == View.GONE) {
            return false;
        }
        
        return true;
    }
    // [E][2013.08.01][eunjungjudy.kim@lge.com] check need BreadCrumb or not

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current fragment, if it is the same as originally launched
        if (mCurrentHeader != null) {
            outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
        }
        if (mParentHeader != null) {
            outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mNFCReceiver != null && mNFCIntentFilter != null) {
            try {
                this.registerReceiver(mNFCReceiver, mNFCIntentFilter); // rebestm - tap&pay
                Log.d ("kjo", "registerReceiver");
            } catch (IllegalArgumentException e) {
                Log.d ("kjo", "IllegalArgumentException " + e );
            }
        }
        mDevelopmentPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                invalidateHeaders();
            }
        };
        mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            // rebestm - WFC
            if( "1".equals(SystemProperties.get("ro.chameleon.sprintwificall", "0")) ) { //ro property
                SharedPreferences preferences = this.getSharedPreferences("WFC_toast", 0);
                if ( preferences.getInt("WFC_toast", 0) == 111 ) {
                    onShowCustomToast();
                    preferences.edit().putInt("WFC_toast", 222).commit(); //first entrance end
                }
            }

            ((HeaderAdapter)listAdapter).resume();
            // yonguk.kim 20121109 Add RCS menu dynamically
            if (mRCSHeader != null && checkRcsDB() == true) {
                Log.d("Settings", "checkRCS");
                HeaderAdapter ha = (HeaderAdapter)listAdapter;
                int headers_size = ha.getCount();
                boolean found = false;
                int insertIndex = 0;
                for (int i = 0; i < headers_size; i++) {
                    if (ha.getItem(i).id == mRCSHeader.id) {
                        found = true;
                    }
                    if (ha.getItem(i).id == R.id.wireless_settings) {
                        insertIndex = i;
                    }
                }
                if (found == false) {
                    Log.d("Settings", "insert RCS Header");
                    ha.insert(mRCSHeader, insertIndex);
                }
            }
        }
        invalidateHeaders();
        updateSecuritySearchDB();
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	if (mInitGuideDialog != null && mInitGuideDialog.isShowing()) {
    		mIsDialogCheck = mInitChecked.isChecked();
        	setUpGuideDialogContent();
        }
		super.onConfigurationChanged(newConfig);
	}


    @Override
    public void onPause() {
        super.onPause();
        if ( mNFCReceiver != null ) {
            try {
                this.unregisterReceiver(mNFCReceiver); // rebestm - tap&pay
                Log.d ("kjo", "unregisterReceiver");
            } catch (IllegalArgumentException e) {
                Log.d ("kjo", "IllegalArgumentException " + e );
            }
        }
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter)listAdapter).pause();
        }

        mDevelopmentPreferences.unregisterOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);
        mDevelopmentPreferencesListener = null;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // [Account] Back to native
        if (Utils.isSupportAccountMenu_VZW() && mListeningToAccountUpdates) {
            AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        }
        unregisterReceiver(mFinshMainUIReceiver);


        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195][ID-MDM-35][ID-MDM-191][ID-MDM-167]
        // [ID-MDM-198][ID-MDM-52][ID-MDM-280][ID-MDM-342][ID-MDM-343][ID-MDM-344][ID-MDM-345]
        // [ID-MDM-346][ID-MDM-347][ID-MDM-413][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END

        if (Config.supportAirplaneListMenu() && !Utils.isTablet()) {
            if (mAirplaneECMReceiver != null) {
                unregisterReceiver(mAirplaneECMReceiver);
            }
        }
        
        if (mBreadCrumb != null) {
            mBreadCrumb.clean();
        }
        //        mSelectedHeader = null; // for remote fragment loading
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    private void initBreadCrumb() {
        mBreadCrumbTitleDisplayedFlag = false;
        if (mBreadCrumb != null) {
            mBreadCrumb.removeSwitch();
            mBreadCrumb.removeImageButton();
            mBreadCrumb.removeView(null);
        }
    }

    @Override
    public void switchToHeader(Header header) {
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }

		if (header.fragment != null && getCurrentHeader() != null
				&& getCurrentHeader().fragment != null) {
			if (header.fragment.equals(getCurrentHeader().fragment) == false) {
				initBreadCrumb();
			}
		} else if (getCurrentHeader() != null
				&& getCurrentHeader().id != header.id) {
			initBreadCrumb();
		}

        super.switchToHeader(header);
    }

    /**
     * Switch to parent fragment and store the grand parent's info
     * @param className name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
                highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment
                = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, "Could not find parent activity : " + className);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void highlightHeader(int id) {
        if (id != 0) {
            //if (mSelectedHeader != null) {
            //	mSelectedHeader.id = id;
            //}

            Integer index = mHeaderIndexMap.get(id);
            if (index != null) {
                getListView().setItemChecked(index, true);
                if (isMultiPane()) {
                    getListView().smoothScrollToPosition(index);
                }
            }
        }
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        } else if (startingFragment != null && onIsMultiPane() && ("com.android.settings.Settings$WifiSettingsDialogActivity".equals(superIntent.getComponent().getClassName())
                || "com.android.settings.Settings$WifiScreenSettingsDialogActivity".equals(superIntent.getComponent().getClassName()))) {

            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            modIntent.putExtra(EXTRA_NO_HEADERS, true);
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and
     * returns the class name to load as a fragment.
     */
    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) { return mFragmentClass; }

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) { return null; }

        if ("com.android.settings.ManageApplications".equals(intentClass)
                || "com.android.settings.RunningServices".equals(intentClass)
                || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            // Old names of manage apps.
            intentClass = com.android.settings.applications.ManageApplications.class.getName();
        }

        return intentClass;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be launched
     * for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();

            if (Utils.supportRemoteFragment(this)
                    && fragmentClass.startsWith("com.android.settings.") == false
                    && RemoteFragmentManager.getPackageName(fragmentClass) != null) {
                if (mRemoteManager != null) {
                    mRemoteManager.buildRemoteHeader(header, fragmentClass);
                }
            }

            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
            int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args,
                titleRes, shortTitleRes);

        Log.d(LOG_TAG, "fragmentName = " + fragmentName);
        // some fragments want to avoid split actionbar
        if (DataUsageSummary.class.getName().equals(fragmentName) ||
                PowerUsageSummary.class.getName().equals(fragmentName) ||
                AccountSyncSettings.class.getName().equals(fragmentName) ||
                UserDictionarySettings.class.getName().equals(fragmentName) ||
                UserDictionaryList.class.getName().equals(fragmentName) ||
                Memory.class.getName().equals(fragmentName) ||
                ManageApplications.class.getName().equals(fragmentName) ||
                WirelessSettings.class.getName().equals(fragmentName) ||
                DreamSettings.class.getName().equals(fragmentName) ||
                SoundSettings.class.getName().equals(fragmentName) ||
                RingtonePicker.class.getName().equals(fragmentName) ||
                RingtonePickerActivity.class.getName().equals(fragmentName) ||
                FontSettings.class.getName().equals(fragmentName) ||
                FontSettingsActivity.class.getName().equals(fragmentName) ||
                TouchFeedbackAndSystemPreference.class.getName().equals(fragmentName) ||
                PrivacySettings.class.getName().equals(fragmentName) ||
                ManageAccountsSettings.class.getName().equals(fragmentName) ||
                VpnSettings.class.getName().equals(fragmentName) ||
                SecuritySettings.class.getName().equals(fragmentName) ||
                TrustedCredentialsSettingsActivity.class.getName().equals(fragmentName) ||
                InstalledAppDetails.class.getName().equals(fragmentName) ||
                SKTVerifyAppsPreference.class.getName().equals(fragmentName) ||
                DeviceInfoLgeDSim.class.getName().equals(fragmentName) ||
                PrintSettingsFragment.class.getName().equals(fragmentName) ||
                PrintServiceSettingsFragment.class.getName().equals(fragmentName) ||
                DeviceInfoLgeTSim.class.getName().equals(fragmentName) ||
                HotkeySettingsActivity.class.getName().equals(fragmentName) ||
                Hotkey4thSettingsActivity.class.getName().equals(fragmentName) ||
                ShortcutkeySettingsActivity.class.getName().equals(fragmentName) ||
                ZenModeSettingsActivity.class.getName().equals(fragmentName) ||
                NotificationAppListActivity.class.getName().equals(fragmentName) ||
                DataUsageSummaryMultiSIM.class.getName().equals(fragmentName) ||
                AppNotificationSettingsActivity.class.getName().equals(fragmentName) ||
                AdvancedCalling.class.getName().equals(fragmentName) ||
                SoundSettingsMoreActivity.class.getName().equals(fragmentName) ||
                ACRSettingsActivity.class.getName().equals(fragmentName) ||
				WirelessMoreSettings.class.getName().equals(fragmentName)) {
                intent.putExtra(EXTRA_CLEAR_UI_OPTIONS, true);
        }

        intent.setClass(this, SubSettings.class);
        setCurrentHeader(null);
        return intent;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {
        loadHeadersFromResource(R.xml.settings_headers, headers);
        // for remote fragment loading
        if (Utils.supportRemoteFragment(this) && mRemoteManager != null) {
            mRemoteManager.buildHeaders(headers, getApplicationContext());
        }
        updateHeaderList(headers);
    }

    private void updateHeaderList(List<Header> target) {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-35][ID-MDM-280][ID-MDM-52][ID-MDM-191]
        // [ID-MDM-167][ID-MDM-198][ID-MDM-223][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && Utils.supportSplitView(this) == false) {
            ListAdapter listAdapter = getListAdapter();
            if (listAdapter instanceof HeaderAdapter) {
                MDMSettingsAdapter.getInstance().setSettingsMenu(getResources(), listAdapter);
            }
        }
        // LGMDM_END
        final boolean showDev = mDevelopmentPreferences.getBoolean(
                DevelopmentSettings.PREF_SHOW,
                android.os.Build.TYPE.equals("eng"));
        
        final boolean mHideDev = mDevelopmentPreferences.getBoolean(
                DevelopmentSettings.PREF_HIDE,
                android.os.Build.TYPE.equals("eng"));        

        final UserManager um = (UserManager)getSystemService(Context.USER_SERVICE);

        int i = 0;
        checkSensor();
        mHeaderIndexMap.clear();
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int)header.id;
            if (id == R.id.operator_settings || id == R.id.manufacturer_settings) {
                Utils.updateHeaderToSpecificActivityFromMetaDataOrRemove(this, target, header);
            } else if (id == R.id.wifi_settings) {
                // Remove WiFi Settings if WiFi service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                    target.remove(i);
                }
                if ("VZW".equals(Config.getOperator())
                        && (android.provider.Settings.System.getInt(this.getContentResolver(),
                                SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0)) == 1) {
                    Log.d(LOG_TAG, "##feature remove Settings->Wi-Fi");
                    target.remove(header);
                }
                // rebestm - WFC
            } else if (id == R.id.wifi_calling_settings) {
                // rebestm - Remove WiFi calling Settings if WiFi service is not available.
                //ro property for Sprint MVNO. ro property is activated to "1" by Sprint HFA.
                if( "0".equals(SystemProperties.get("ro.chameleon.sprintwificall", "0")) ) {
                    target.remove(i);
                }
            } else if (id == R.id.bluetooth_settings) {
                // Remove Bluetooth Settings if Bluetooth service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    target.remove(i);
                }
                // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            } else if (id == R.id.airplane_mode_settings) {
                if (!Config.supportAirplaneListMenu()) {
                    target.remove(header);
                }
                // [END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            } else if (id == R.id.home_settings) {
                if (onIsMultiPane() == true
                    && (!"VZW".equals(Config.getOperator())
                            && !"ATT".equals(Config.getOperator()))) {
                    target.remove(header);
                }
            } else if (id == R.id.lock_settings) {
                if (onIsMultiPane() == true
                    && (!"VZW".equals(Config.getOperator())
                            && !"ATT".equals(Config.getOperator()))) {
                    target.remove(header);
                }
            } else if (id == R.id.data_usage_settings) {
                // Remove data usage when kernel module not enabled
                final INetworkManagementService netManager = INetworkManagementService.Stub
                        .asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
                try {
                    if (!netManager.isBandwidthControlEnabled()) {
                        target.remove(i);
                    }
                } catch (RemoteException e) {
                    // ignored
                }

                if (Utils.isWifiOnly(this)) {
                    header.titleRes = R.string.data_usage_summary_title;
                } else if ("SKT".equals(Config.getOperator()) ||
                        "KT".equals(Config.getOperator()) /* ask130613 ||
                         "LGU".equals(Config.getOperator())*/) {
                    header.titleRes = R.string.data_network_settings_title;
                } else if ( "ATT".equals(Config.getOperator()) ) {
                    header.titleRes = R.string.shortcut_datausage_att;
                } else if ( "VZW".equals(Config.getOperator()) ) {
                    header.titleRes = R.string.data_usage_summary_title;
                }

                //      else if (Utils.isMultiSimEnabled() || mtk_dual || mtk_triple) {
                //                  header.titleRes = R.string.data_usage_summary_title;
                //              }
            } else if (id == R.id.use_4g_network) {
                if (false == "CTC".equals(Config.getOperator())
                        && false == "CTO".equals(Config.getOperator())) {
                    SLog.i("not supported the use 4g network");
                    target.remove(header);
                }
            } else if (id == R.id.account_settings) {
                if (Utils.isSupportAccountMenu_VZW()) {
                    int headerIndex = i + 1;
                    i = insertAccountsHeaders(target, headerIndex);
                } else {
                    target.remove(header);
                }
            } else if (id == R.id.account_main_settings) {
                if (Utils.isSupportAccountMenu_VZW()) {
                    target.remove(header);
                } else {
                    if (Config.VZW.equals(Config.getOperator())) {
                        header.titleRes = R.string.account_settings;
                    }
                }
            } else if (id == R.id.account_add) {
                if (Utils.isSupportAccountMenu_VZW()) {
                } else {
                    target.remove(header);
                }
            } else if (id == R.id.safety_care_settings) {
                //                if (!appIsEnabled("com.lge.cloudhub")) {
                //                    target.remove(header);
                //                }
                target.remove(header);
            } else if (id == R.id.user_settings) {
                boolean hasMultipleUsers =
                        ((UserManager)getSystemService(Context.USER_SERVICE))
                                .getUserCount() > 1;

                if (!UserHandle.MU_ENABLED
                        || (!UserManager.supportsMultipleUsers()
                        && !hasMultipleUsers)
                        || Utils.isMonkeyRunning()) {
                    target.remove(i);
                }
                // rebestm - kk migration
            } else if (id == R.id.nfc_payment_settings) {
                if (!Utils.isTapPay(this) || Utils.isUI_4_1_model(this)) {
                    target.remove(i);
                    Log.i (LOG_TAG, "Remove tap&pay");
                }
            } else if (id == R.id.application_settings) {
                header.titleRes = R.string.applications_settings;

                if ("VZW".equals(Config.getOperator())) {
                    header.titleRes = R.string.applications_settings_title;
                } else if ("LRA".equals(Config.getOperator())) {
                    header.titleRes = R.string.applications_settings;
                }
            } else if ( id == R.id.sms_default_settings ) {
            

                boolean isSMSsupported = false;
                if ((mPhone.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) || (mPhone.isSmsCapable())) {
                    isSMSsupported = true;
                } else {
                    isSMSsupported = false;
                }
                Log.d(LOG_TAG, "mPhone.getPhoneType() = " + mPhone.getPhoneType());
                Log.d(LOG_TAG, "mPhone.isSmsCapable() = " + mPhone.isSmsCapable());
                Log.d(LOG_TAG, "isSMSsupported = " + isSMSsupported);

                if (!Utils.isUI_4_1_model(this) || !isSMSsupported
                        || "VZW".equals(Config.getOperator())) {
                    target.remove(i);
                    Log.d(LOG_TAG, "remove Default message app");
                }
            } else if (id == R.id.development_settings) {
                Log.d ("development", "mHideDev = " + mHideDev);
                Log.d ("development", "showDev = " + showDev);
                if (!showDev || mHideDev
                        || um.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES)) {
                    target.remove(i);
                }
            } else if (id == R.id.share_connect) {
                /*UI 4.2 Version has Printer, and Printer does not remove all model except CN */
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    if (("CN".equals(Config.getCountry()) || Utils.isFolderModel(this) || sIsSubUserOnly) && !Utils.supportShareConnect(this)) {
                        target.remove(header);
                    }
                } else {
                    if (!Utils.supportShareConnect(this) ) {
                        target.remove(header);
                    }
                }
            } else if (id == R.id.network_storage_settings) {
                target.remove(header);
            } else if (id == R.id.wireless_settings) {
                if (!"DCM".equals(Config.getOperator()) && !Utils.isWifiOnly(this)) {
                    target.remove(header);
                }

            } else if (id == R.id.tether_network_settings) {
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2
                    && !"VZW".equals(Config.getOperator())) {
                   if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(this) ) {
                      target.remove(header);
				   } else if (sIsSubUserOnly || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING) || !Utils.isSupportTethering(this)) {
                      target.remove(header);
                   } else {
                      header.breadCrumbTitleRes = R.string.wireless_tethering_settings_title;
                      header.titleRes = R.string.wireless_tethering_settings_title;
                   }
                } else {
                    if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(this) ) {
                        Log.d (LOG_TAG, "Delete tether_network_settings");
                        target.remove(header);
                    } else if (Config.supportAirplaneListMenu() && sIsSubUserOnly) {
                        if ("VZW".equals(Config.getOperator())) {
							if (!mPhone.isSmsCapable()) {
                                Log.d (LOG_TAG, "Delete tether_network_settings");
                                target.remove(header);
                            } else {
                                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                                    header.breadCrumbTitleRes = R.string.wireless_more_settings_title;
                                    header.iconRes = R.drawable.empty_icon;
                                    header.titleRes = R.string.wireless_more_settings_title;
                                } else {
                                    header.breadCrumbTitleRes = R.string.wireless_networks_settings_title;
                                    header.iconRes = R.drawable.empty_icon;
                                    header.titleRes = R.string.radio_controls_title;
                                }
                            }
                        } else {
                            Log.d (LOG_TAG, "Delete tether_network_settings");
                            target.remove(header);
                        }
                    } else if ("SPR".equals(Config.getOperator())
                            || "TRF".equals(Config.getOperator())
                            || "CTC".equals(Config.getOperator())
                            || "CTO".equals(Config.getOperator())
                            || ("ATT".equals(Config.getOperator()) && Utils.isTablet())
                            || "AIO".equals(Config.getOperator())
                            || "CRK".equals(SystemProperties.get("ro.build.target_operator"))
                            || "VZW".equals(Config.getOperator())
                            || "vfp".equals(Build.DEVICE)
                            || "vfpv".equals(Build.DEVICE)
                            || Utils.isSBMUserBuild()) {
                        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                            header.breadCrumbTitleRes = R.string.wireless_more_settings_title;
                            header.iconRes = R.drawable.empty_icon;
                            header.titleRes = R.string.wireless_more_settings_title;
                        } else {
                            header.breadCrumbTitleRes = R.string.wireless_networks_settings_title;
                            header.iconRes = R.drawable.empty_icon;
                            header.titleRes = R.string.radio_controls_title;
                        }
                    }
               }
            } else if (id == R.id.wireless_more_settings) {
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2
                    && !"VZW".equals(Config.getOperator())) {
                   if (Utils.isWifiOnly(this) || "DCM".equals(Config.getOperator())) {
                       target.remove(header);
				   } else if ((Config.supportAirplaneListMenu() && sIsSubUserOnly)) {
				   	   if ("VZW".equals(Config.getOperator())) {
                          if (!mPhone.isSmsCapable()) {
                              Log.d (LOG_TAG, "Delete tether_network_settings");
                              target.remove(header);
                          }
				   	   } else {
					       target.remove(header);
                       }
				   }
                } else {
                   target.remove(header);
                }
            } else if (id == R.id.docomo_settings) {
                if (false == "DCM".equals(Config.getOperator())) {
                    target.remove(header); // dcm service menu
                }
            }
            else if (id == R.id.activate_device_settings) {
                // [LGE_S] Remove when Operator is not Sprint / sangwouk.han@lge.com / 2012.04.20
                if (!isCallable(new Intent().setClassName("com.sprint.dsa", "com.sprint.dsa.DsaClient"))) {
                    target.remove(header);
                }
            } else if (id == R.id.qremote_settings) {
                if (!isCallable(new Intent().setClassName("com.lge.ir.remote.settings",
                        "com.lge.ir.remote.settings.MainSettingsPrefAct"))) {
                    target.remove(header);
                }
            } else if (id == R.id.cloud) {
                if (!appIsEnabled("com.lge.cloudhub") || "VZW".equals(Config.getOperator())) {
                    target.remove(header);
                }
            } else if (id == R.id.account_cloud) {
                if (!appIsEnabled("com.lge.cloudhub") || !"VZW".equals(Config.getOperator())) {
                    target.remove(header);
                }
            } else if (id == R.id.account_vzw_cloud) {
                target.remove(header);
            } else if (id == R.id.home_mode_change) {
                if (!tModeAppIsEnabled()
                        || !Utils.is_G_model_Device(Build.DEVICE)) {
                    target.remove(header);
                }
            } else if (id == R.id.browser_bar_settings) {
                if (false == "ATT".equals(Config.getOperator())
                        || !appIsEnabled("com.skyfire.browser.toolbar.att")) {
                    target.remove(header);
                }
            } else if (id == R.id.guest_mode) {
                if (!isCallable(new Intent().setClassName("com.lge.launcher2", "com.lge.launcher2.plushome.setting.LGPlusHomeSetting"))) {
                    target.remove(header);
                } else {
                    if ("kids".equals(SystemProperties.get("service.plushome.currenthome"))) {
                        target.remove(header);
                    }

                    if (true == UserManager.supportsMultipleUsers()) {
                        target.remove(header);
                    }
                }
                //[Gpro Temporarily model featuring][jongwon007.kim]
                if (Utils.is_G_model_Device(Build.DEVICE)) {
                    target.remove(header);
                }

                if (!Utils.getGuestModeFeature(this)) {
                    target.remove(header);
                }
            } else if (id == R.id.dualsim_settings) {
                if (!Utils.isMultiSimEnabled() || UserHandle.myUserId() != UserHandle.USER_OWNER) {
                    target.remove(header);
                } else {
                    if ("CTC".equalsIgnoreCase(Config.getOperator()) ||
                            "CTO".equalsIgnoreCase(Config.getOperator())) {
                        header.titleRes = R.string.sp_uim_sim_card_actionbar_CT_NORMAL;
                    }
                }
            } else if (id == R.id.dualsim_settings_triple_qct) {
                if (!Utils.isTripleSimEnabled() || UserHandle.myUserId() != UserHandle.USER_OWNER) {
                    target.remove(header);
                }
            } else if (id == R.id.au_customer_support) {
                if (!"KDDI".equals(Config.getOperator())) {
                    target.remove(header);
                }
            } else if (id == R.id.vzw_advance_calling_settings) {
                if ("VZW".equals(Config.getOperator())) {
                    if (!Utils.isSupportedVolte(this)) {
                      target.remove(header);
                    } else {
                      Log.d("VZW", "Advanced menu exist");
                    }
                } else {
                    target.remove(header);
                }
            }

            //[S][2013.04.01][jin850607.hong] Accessory setting
            if (id == R.id.accessory_settings) {
                if (Utils.isCarHomeSupport(this) == false) {
                    if ((Utils.supportTangibleSettings(this) == false
                            && Utils.supportCoverSettings(this) == false)
                            || Utils.isUI_4_1_model(this) == true) {
                        target.remove(header);
                    }    
                }
                
            }
            //[E][2013.04.01][jin850607.hong] Accessory setting

            if (id == R.id.ifttt_settings) {
                if (com.lge.os.Build.LGUI_VERSION.RELEASE < com.lge.os.Build.LGUI_VERSION_NAMES.V4_2 ||
                    !Utils.checkPackage(this, "com.lge.iftttmanager")) {
                    target.remove(header);
                } else {
                    ComponentName c;
                    c = new ComponentName("com.lge.iftttmanager",
                        "com.lge.iftttmanager.ui.RecipeListActivity");
                     header.intent = new Intent(Intent.ACTION_MAIN);
                     header.intent.setComponent(c);
                }
            }
            //[START][seungyeop.yeom][2014-04-23] Item of Cover for GUI 4.1 model
            if (id == R.id.cover_settings) {
                if (Utils.supportTangibleSettings(this) == true
                        || Utils.supportCoverSettings(this) == false
                        || Utils.isUI_4_1_model(this) == false
                        || Utils.isCarHomeSupport(this) == true) {
                    target.remove(header);
                } else {
                    if (Config.getFWConfigBool(this,
                            com.lge.R.bool.config_smart_cover,
                            "com.lge.R.bool.config_smart_cover") == true) {
                        header.iconRes = R.drawable.ic_smartcover;
                        header.titleRes = R.string.quickcover_title;
                        header.fragment = QuickCoverView.class.getName();
                    } else if (Config.getFWConfigBool(this,
                                    com.lge.R.bool.config_using_window_cover,
                                    "com.lge.R.bool.config_using_window_cover") == true) {
                        header.iconRes = R.drawable.ic_quickcover_general;
                        header.titleRes = R.string.display_quick_cover_window_title;

                        ComponentName c;
                        c = new ComponentName("com.android.settings",
                                "com.android.settings.lge.QuickWindowCase");
                        header.intent = new Intent(Intent.ACTION_MAIN);
                        header.intent.setComponent(c);
                    } else if (Config.getFWConfigBool(this,
                                    com.lge.R.bool.config_using_circle_cover,
                                    "com.lge.R.bool.config_using_circle_cover") == true) {
                        header.iconRes = R.drawable.ic_quickcover_circle;
                        header.titleRes = R.string.quick_circle_case_title;

                        ComponentName c;
                        c = new ComponentName("com.android.settings",
                                "com.android.settings.lge.QuickWindowCase");
                        header.intent = new Intent(Intent.ACTION_MAIN);
                        header.intent.setComponent(c);
                    } else if (Config.getFWConfigBool(this,
                            com.lge.R.bool.config_using_disney_cover,
                            "com.lge.R.bool.config_using_disney_cover") == true) {
                        header.iconRes = R.drawable.ic_quickcover_circle;
                        header.titleRes = R.string.disney_case_title_ex;

                        ComponentName c;
                        c = new ComponentName("com.android.settings",
                                "com.android.settings.lge.QuickWindowCase");
                        header.intent = new Intent(Intent.ACTION_MAIN);
                        header.intent.setComponent(c);
                    } else {
                        Log.d("YSY", "Quick Cover config : " + Config.getFWConfigBool(this,
                                com.lge.R.bool.config_smart_cover,
                                "com.lge.R.bool.config_smart_cover"));
                        Log.d("YSY", "QuickWindow case config : " + Config.getFWConfigBool(this,
                                com.lge.R.bool.config_using_window_cover,
                                "com.lge.R.bool.config_using_window_cover"));
                        Log.d("YSY", "QuickCircle case config : " + Config.getFWConfigBool(this,
                                com.lge.R.bool.config_using_circle_cover,
                                "com.lge.R.bool.config_using_circle_cover"));
                        Log.d("YSY", "Disney case config : " + Config.getFWConfigBool(this,
                                    com.lge.R.bool.config_using_disney_cover,
                                    "com.lge.R.bool.config_using_disney_cover"));
                        target.remove(header);
                    }
                }
            }
            //[END]

            //[S][2012.03.30][kyochun.shin] Call Settings for Dual Sim
            if (id == R.id.call_settings) {
                boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
                if ( "ATT".equals(Config.getOperator()) ) {
                    if (Utils.isUI_4_1_model(this)) {
                        header.iconRes = R.drawable.ic_settings_call_settings;
                    } else {
                        header.iconRes = R.drawable.ic_call_att;
                    }
                }
                if ( "VZW".equals(Config.getOperator()) ) {
                    target.remove(header);
                }
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled() || isSecondaryUser
                        || Utils.isWifiOnly(this)
                        || Utils.isTablet())
                {
                    //for v410
                    target.remove(header);
                }
            }
            if (id == R.id.dualsim_call_settings) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (!Utils.isMultiSimEnabled() && !Utils.isTripleSimEnabled() || Utils.isWifiOnly(this))
                {
                    target.remove(header);
                }
            }
            //[E][2012.03.30][kyochun.shin] Call Settings for Dual Sim

            // 111209 kerry START
            if (id == R.id.roaming_lgt) {
                boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
                if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                    header.titleRes = R.string.chinatelecomroaming;
                }

                if ("SPR".equals(Config.getOperator()) || "SPRINT".equals(Config.getOperator())) {
                    // [START_LGE_VOICECALL],ADD,hyechan.lee
                    // 2013-03-19,[voicecall][SPCS][Chamelion] Control roaming menu
                    // according to chaemloin node
                    int Chamelion_display = 1;
                    boolean bHAF_activation = false;

                    String phoneActivated = SystemProperties.get("ro.sprint.hfa.flag");
                    if (phoneActivated.equalsIgnoreCase("activationOK")) { // HFA Yes
                        Log.d(LOG_TAG, "HFA yes");
                        bHAF_activation = true;
                    } else { // HFA No
                        Log.d(LOG_TAG, "HFA  No");
                        bHAF_activation = false;
                    }

                    if (bHAF_activation) {
                        Chamelion_display = android.provider.Settings.System.getInt(this.getContentResolver(),
                                SettingsConstants.System.ROAMPREFERENCE_MENUDISPLAY, 1);
                    } else {
                        // Out of Box Chameleon state (roaming menu displayed)
                        if (SystemProperties.getInt("sys.roamingmenu.enable", 0) == 1) {
                            Log.d(LOG_TAG, " - Set 1 sys.roamingmenu.enable ");
                            Chamelion_display = 1;
                        } else {
                            Chamelion_display = 0;
                        }
                    }

                    if (Chamelion_display == 0) {
                        Log.d(LOG_TAG, "remove roaming menu");
                        target.remove(header);
                    }
                }
                // [END_LGE_VOICECALL],ADD,hyechan.lee
                // 2013-03-19,[voicecall][SPCS][Chamelion] Control roaming menu
                // according to chaemloin node
                //[S][2012.04.18][youngmin.jeon][LG730] Reuse existing roaming menu for LG730(SPRINT, BM)
                //shlee1219 20120531 SKT Roaming menu add
                if (false == "LGU".equals(Config.getOperator())    && false == "KT".equals(Config.getOperator())  &&
                    false == "SPRINT".equals(Config.getOperator()) && false == "BM".equals(Config.getOperator())  &&
                    false == "SPR".equals(Config.getOperator())    && false == "SKT".equals(Config.getOperator()) &&
                    false == "CTC".equals(Config.getOperator()) && false == "CTO".equals(Config.getOperator())) {
                    // [2012.04.30][youngmin.jeon] Add SPR operator for Roaming menu.
                    target.remove(header); // TODO: add LGT featuring
                } else if (Utils.isWifiOnly(this)) {
                    target.remove(header);
                } else if (isSecondaryUser) {
                    // [2014-10-14][seungyeop.yeom] delete roaming menu for other users (not owner)
                    target.remove(header);
                }
                //[E][2012.04.18][youngmin.jeon][LG730] Reuse existing roaming menu for LG730(SPRINT, BM)
            }
            // 111209 kerry END
            //[S][jongtak0920.kim][common] Delete One-Handed Operation menu
            if (id == R.id.one_hand_settings) {
                if (!Utils.supportOneHandOperation(this)) {
                    target.remove(header);
                }
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    target.remove(header);
                }
            }
            //[E][jongtak0920.kim][common] Delete One-Handed Operation menu

            if (id == R.id.settings_multitasking) {
                if (SUPPORT_DUAL_WINDOW && !appIsEnabled("com.lge.slideaside")
                        && Utils.isUI_4_1_model(getApplicationContext())) {
                    header.titleRes = R.string.app_split_view_dual_window;
                    header.iconRes = R.drawable.ic_split_view;
                    header.fragment = DualWindowSettings.class.getName();
                } else {
                    if (!appIsEnabled("com.lge.slideaside") ||
                            (Config.getOperator().equals(Config.VZW)
                                    && "g2".equals(Build.DEVICE))) {
                        target.remove(header);
                    } else if (SUPPORT_DUAL_WINDOW) {
                        header.titleRes = R.string.sp_settings_multitasking;
                        header.iconRes = R.drawable.ic_settings_multitasking_splitview_list;
                    }
                }
                if (SUPPORT_DUAL_WINDOW == false &&
                        Utils.isUI_4_1_model(getApplicationContext())) {
                    target.remove(header);
                }
            }

            // [S][2012.10.5][changyu0218.lee] Add lge accessibility
            if ( id == R.id.accessibility_settings ) {
                if (Utils.checkPackage(this, "com.android.settingsaccessibility")) {
                    Log.i(LOG_TAG, "Remove native accessibility");
                    target.remove(header);
                }
            }

            if ( id == R.id.accessibility_settings_lge ) {
                if (!Utils.checkPackage(this, "com.android.settingsaccessibility")) {
                    Log.i(LOG_TAG, "Remove lge accessibility");
                    target.remove(header);
                }
            }
            // [E][2012.10.5][changyu0218.lee] Add lge accessibility
            // [S][2012.09.20][changyu0218.lee] Easy UI menu
            if (id == R.id.easy_to_use) {
                if (!Utils.isEnableEasyUI()) {
                    target.remove(header);
                }
            }

            if (id == R.id.sound_settings) {
                if (Config.getOperator().equals(Config.VZW)) {
                    header.titleRes = R.string.notification_settings;
                }
            }
            // [E][2012.09.20][changyu0218.lee] Easy UI menu

            if (id == R.id.flip_close_settings) {
                if (!Utils.isFolderModel(this)) {
                    target.remove(header);
                }

            }

            // [START][2014-10-15][seungyeop.yeom] add category of au settings for only KDDI
            if (id == R.id.au_settings_section) {
                if (Utils.checkPackage(this, "com.kddi.android.au_setting_menu") == false) {
                    Log.i(LOG_TAG, "Remove au settings category");
                    target.remove(header);
                }
            }

            if (id == R.id.au_settings) {
                if (Utils.checkPackage(this, "com.kddi.android.au_setting_menu") == false) {
                    Log.i(LOG_TAG, "Remove au settings menj");
                    target.remove(header);
                }
            }
            // [END][2014-10-15][seungyeop.yeom] add category of au settings for only KDDI

            if (id == R.id.t_action) {
                if (Utils.checkPackage(this, "com.skt.taction") == false) {
                    target.remove(header);
                }
            }
            if (id == R.id.safety_cleaner) {
                if (Utils.checkPackage(this, "com.skt.t_smart_charge") == false) {
                    target.remove(header);
                }
            }

            //[E][jaeyoon.hyun][common] Delete Gesture menu when sensor no supply
            //[S][jaeyoon.hyun][common] RCS setting del when it's not exist
            if (id == R.id.rcs_setting_bb) {
                mRCSHeader = header;
                if (checkRcsDB() == false) {
                    target.remove(header);
                }
            }
            if (id == R.id.rcs_setting_mpcs) {
                if (false == isCallable(new Intent().setAction("com.summit.beam.SHOW_SETTINGS"))) {
                    target.remove(header);
                }
            }
            // [S][2012.02.15][jaym123] About phone menu
            if (id == R.id.about_settings) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isSelectOldVersion() == false || Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    Log.d("aboutphone", "about_settings, removed !" );
                    target.remove(header);
                }
            }

            if (id == R.id.about_settings_lge) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (Utils.isSelectOldVersion() == true || Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    Log.d("aboutphone", "about_settings_lge, removed !" );
                    target.remove(header);
                }
            }

            if (id == R.id.system_update_settings) {
                if (!"VZW".equals(Config.getOperator()) && !"LRA".equals(Config.getOperator())) {
                    Log.d("aboutphone", "system_update_settings, removed !" );
                    target.remove(header);
                }
                if (!Utils.checkPackage(this, "com.innopath.activecare")) {
                    Log.d("aboutphone", "com.innopath.activecare checkPackage = false, removed !" );
                    target.remove(header);
                }
                if (Utils.isUI_4_1_model(getApplicationContext())) {
                    Log.d("aboutphone", "isUI_4_1_model = true, removed !" );
                    target.remove(header);
                }
            }

            if (id == R.id.system_update_settings_dms) {
                if (!"VZW".equals(Config.getOperator()) && !"LRA".equals(Config.getOperator())) {
                    Log.d("aboutphone", "system_update_settings, removed !" );
                    target.remove(header);
                }
                if (!Utils.checkPackage(this, "com.lge.lgdmsclient")) {
                    Log.d("aboutphone", "com.lge.lgdmsclient checkPackage = false, removed !" );
                    target.remove(header);
                }
                if (!Utils.isUI_4_1_model(getApplicationContext())) {
                    Log.d("aboutphone", "isUI_4_1_model = false, removed !" );
                    target.remove(header);
                }

                int otadmSetting = android.provider.Settings.System.getInt(
                        this.getContentResolver(),
                        SettingsConstants.System.VZW_HIDDEN_FEATURE_OTADM, 1);

                if (otadmSetting == 0) {
                    Log.d("aboutphone", "##feature false, removed !" );
                    target.remove(header);
                }
            }

            if (id == R.id.about_settings_lge_dsim) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (!Utils.isMultiSimEnabled()) {
                    target.remove(header);
                }
            }

            if (id == R.id.about_settings_lge_tsim) {
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (!Utils.isTripleSimEnabled()) {
                    target.remove(header);
                }
            }
            // [E][2012.02.15][jaym123] About phone menu

            // [S][2012.12.07][jongtak0920.kim] Add Hotkey customization
            if (id == R.id.hotkey_settings) {
                header.titleRes = R.string.sp_hotkey_NORMAL;

                if ("KR".equals(Config.getCountry2())
                        || "JP".equals(Config.getCountry2())) {
                    header.titleRes = R.string.sp_q_button_NORMAL;
                }

                if (!Utils.supportHotkey(this)
                        || Utils.isSharingBetweenSimHotkey(this)) {
                    target.remove(header);
                }
            }
            // [E][2012.12.07][jongtak0920.kim] Add Hotkey customization
            if (id == R.id.hotkey_settings_4th) {
                header.titleRes = R.string.sp_hotkey_NORMAL;

                if ("KR".equals(Config.getCountry2())
                        || "JP".equals(Config.getCountry2())) {
                    header.titleRes = R.string.sp_q_button_NORMAL;
                }

                if (!Utils.supportHotkey(this)
                        || !Utils.isSharingBetweenSimHotkey(this)) {
                    target.remove(header);
                }
            }
            if (id == R.id.shortcutkey_settings) {
                if (!Utils.isUI_4_1_model(this) || Utils.isFolderModel(this)) {
                  target.remove(header);
                }
            }
            if (id == R.id.system_update_spr) {
                if (!("SPR".equals(Config.getOperator()))) {
                    target.remove(header);
                }
            }
            //[S][2013.5.21][jm1.yoo@lge.com] Changed the menu of backup and reset for CMCC
            /*
            if (id == R.id.privacy_settings) {
                if ("CMCC".equals(Config.getOperator())) {
                    target.remove(header);
                }
            }

            if (id == R.id.privacy_settings_cmcc) {
                if (!"CMCC".equals(Config.getOperator())) {
                    target.remove(header);
                }
            }
            */
            //[E][2013.5.21][jm1.yoo@lge.com] Changed the menu of backup and reset for CMCC
            //[S][2014.1.25][weiyt.jiang@lge.com] remove printing for CN
            if (id == R.id.print_settings) {
                if ("CN".equals(Config.getCountry()) || Utils.isFolderModel(this) || com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    target.remove(header);
                }
            }
            //[E][2013.1.25][weiyt.jiang@lge.com] remove printing for CN
            //[jongwon007.kim] SKT Phone Mode
            if (id == R.id.tphone_mode) {
                if (!IsTPhoneMode()) {
                    target.remove(header);
                }
            }

            if (id == R.id.connectivity_settings) {
                target.remove(header);
            }
            
            if (id == R.id.usb_settings) {
                target.remove(header);
            }
            
            if (id == R.id.springcleaning) {
            	boolean hasClipTray = false;
            	PackageManager pm = getPackageManager();
            	
            	if (pm != null) {
            		hasClipTray = pm.hasSystemFeature("com.lge.software.cliptray");
            	}
            	
                if (Utils.checkPackage(this, "com.lge.springcleaning") == false
                		|| hasClipTray == false) {
                    target.remove(header);
                }
            }
            if (id == R.id.yahoo_box_settings) {
                if (Utils.checkPackage(this, "jp.co.yahoo.android.ybox") == false) {
                    target.remove(header);
                }

            }

            if (id == R.id.block_mode_settings) {
                if (com.lge.os.Build.LGUI_VERSION.RELEASE < com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    target.remove(header);
                } else if (!"CMCC".equalsIgnoreCase(Config.getOperator())
                        && !"CTC".equalsIgnoreCase(Config.getOperator())
                        && !"CMO".equalsIgnoreCase(Config.getOperator())
                        && !"CTO".equalsIgnoreCase(Config.getOperator())) {
                    target.remove(header);
                }
            }

            if (id == R.id.menu_settings) {
                boolean naviBoolean = false;
                IWindowManager mWindowManagerService;
                mWindowManagerService = WindowManagerGlobal
                        .getWindowManagerService();
                try {
                    naviBoolean = mWindowManagerService.hasNavigationBar();
                } catch (RemoteException e) {
                    Log.d(LOG_TAG, "RemoteException hasNavigationBar()");
                }
                if (naviBoolean
                        || !Config.getFWConfigBool(this, com.lge.R.bool.config_front_menu_or_recent_key,
                              "com.lge.R.bool.config_front_menu_or_recent_key")) {
                    target.remove(header);
                }
            }

            if (id == R.id.date_time_settings) {
                if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                    target.remove(header);
                }
            }

            if (target.size() > i && target.get(i) == header
                    && UserHandle.MU_ENABLED && UserHandle.myUserId() != 0
                    && !ArrayUtils.contains(SETTINGS_FOR_RESTRICTED, id)) {
                target.remove(i);
            }

            // Increment if the current one wasn't removed by the Utils code.
            if (target.size() > i && target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the top-level
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }
    // yonguk.kim 20121109 Add RCS menu dynamically
    public boolean checkRcsDB() {
        boolean result = true;
        boolean rcs_scenario = false;
        Cursor objCursor = null;
        //VDF : ES, DE, TLF : ES, ORG : ES
        Log.d("hkk", "[checkRcsDB] operator : " + Config.getOperator());
        Log.d("hkk", "[checkRcsDB] country : " + Config.getCountry());
        String rcs_working = android.provider.Settings.System.getString(this.getContentResolver(), "rcs_working");
        Log.d("hkk", "[checkRcsDB] rcs_working : " + rcs_working);
            if (rcs_working != null) {
                if (!rcs_working.equals("1")) {
                    result = false;
                }
            }
        try {
            objCursor = this.getContentResolver().query(
                    Uri.parse(CONTENT_URI), null, null, null, null);
            Log.d("kimyow", "CONTENT_URI" + CONTENT_URI);
            if (objCursor == null) {
                objCursor = this.getContentResolver().query(
                        Uri.parse(CONTENT_URI_BB), null, null, null, null);
                Log.d("kimyow", "CONTENT_URI_BB" + CONTENT_URI_BB);
            }
            if ( objCursor == null) {
                Log.d("kimyow", "R.id.rcs_setting : objCursor==null");
                return false;
            }
            if ( !objCursor.moveToNext()) {
                Log.d("kimyow", "R.id.rcs_setting : objCursor.moveToNext()==null");
                objCursor.close();
                return false;
            }
            int show_status = objCursor.getInt(objCursor.getColumnIndex(SHOW_RCS));
            if ( show_status < 1 ) {
                Log.d("kimyow", "R.id.rcs_setting:status:" + show_status);
                objCursor.close();
                return false;
            }
			if (show_status == 1) {
                objCursor.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("kimyow", "Exception occured");
            result = false;
        }
        return result;
    }
    //[S][jaeyoon.hyun][common] Delete Gesture menu when sensor no supply
    public void checkSensor() {
        SensorManager sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        // get all sensors of all types
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < sensorList.size(); i++) {
            if (sensorList.get(i).getType() == TYPE_SENSOR_LGE_GESTURE_FACING) {
                HASFACING_SENSOR = true;
            } else if (sensorList.get(i).getType() == ORIENTATION) {
                hasOrient_SENSOR = true;
            } else if (sensorList.get(i).getType() == TYPE_ACCELEROMETER) {
                hasAccelerometer_SENSOR = true;
            }
        }
    }
    //[E][jaeyoon.hyun][common] Delete Gesture menu when sensor no supply
    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    private int insertAccountsHeaders(List<Header> target, int headerIndex) {
        String[] accountTypes = mAuthenticatorHelper.getEnabledAccountTypes();
        List<Header> accountHeaders = new ArrayList<Header>(accountTypes.length);
        for (String accountType : accountTypes) {
            CharSequence label = mAuthenticatorHelper.getLabelForType(this, accountType);
            if (label == null) {
                continue;
            }
            if (!isVisibleAccounts(accountType)) {
                continue;
            }
            Account[] accounts = AccountManager.get(this).getAccountsByType(accountType);
            boolean skipToAccount = accounts.length == 1
                    && !mAuthenticatorHelper.hasAccountPreferences(accountType);
            Header accHeader = new Header();
            accHeader.title = label;
            if (accHeader.extras == null) {
                accHeader.extras = new Bundle();
            }
            if (skipToAccount) {
                accHeader.breadCrumbTitleRes = R.string.account_sync_settings_title;
                accHeader.breadCrumbShortTitleRes = R.string.account_sync_settings_title;
                accHeader.fragment = AccountSyncSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                // Need this for the icon
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.extras.putParcelable(AccountSyncSettings.ACCOUNT_KEY, accounts[0]);
                //[A_VZW][jongwon007.kim] IsSkip
                accHeader.fragmentArguments.putBoolean("IsSkip", true);
                accHeader.fragmentArguments.putParcelable(AccountSyncSettings.ACCOUNT_KEY,
                        accounts[0]);
            } else {
                accHeader.breadCrumbTitle = label;
                accHeader.breadCrumbShortTitle = label;
                accHeader.fragment = ManageAccountsSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE,
                        accountType);
                if (!isMultiPane()) {
                    accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL,
                            label.toString());
                }
            }
            accountHeaders.add(accHeader);
        }

        // Sort by label
        Collections.sort(accountHeaders, new Comparator<Header>() {
            @Override
            public int compare(Header h1, Header h2) {
                String accountType = h2.extras.getString(ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                if (accountType != null && accountType.equals("com.fusionone.account")) {
                    return 1;
                }
                return h1.title.toString().compareTo(h2.title.toString());
            }
        });

        for (Header header : accountHeaders) {
            target.add(headerIndex++, header);
        }
        if (!mListeningToAccountUpdates) {
            AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
        return headerIndex;
    }

    protected boolean cloudEnable() {
        boolean result = false;
        PackageManager pm = getPackageManager();
        if (pm != null) {
            Log.d("BUA+", "pm.hasSystemFeature(com.lge.cloudservice.enabled); : " + pm.hasSystemFeature("com.lge.cloudservice.enabled"));
            result = pm.hasSystemFeature("com.lge.cloudservice.enabled");
        }
        return result;
    }
    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);

            if (Utils.supportRemoteFragment(this)) {
                if (getIntent() != null && getIntent().getAction() != null) {
                    RemoteActionInfo actionInfo = RemoteFragmentManager.checkRemoteAction(getIntent().getAction());
                    if (actionInfo != null) {
                        mTopLevelHeaderId = actionInfo.headerId;
                        mFragmentClass = actionInfo.fragmentClassName;
                        setTitle(actionInfo.breadcrumbTitle);
                        return;
                    }
                }
            }

            if (ai == null || ai.metaData == null) {
                // [START][seungyeop.yeom][2014-02-28][User Help Guide]
                if (getIntent().hasExtra("help_guide_function")) {
                    if (getIntent().getStringExtra("help_guide_function").equals(
                            "one_hand_operation")) {
                        Log.d("YSY", "one_hand_operation, getMetaData()");
                        mTopLevelHeaderId = R.id.one_hand_settings;
                        mFragmentClass = OneHandOperationHelp.class.getName();
                    } else if (getIntent().getStringExtra("help_guide_function").equals(
                            "dual_window")) {
                        Log.d("YSY", "dual window, getMetaData()");
                        mTopLevelHeaderId = R.id.settings_multitasking;
                        mFragmentClass = DualWindowHelp.class.getName();
                    } else if (getIntent().getStringExtra("help_guide_function").equals(
                            "battery")) {
                        Log.d("YSY", "battery, getMetaData()");
                        mTopLevelHeaderId = R.id.battery_settings;
                        mFragmentClass = BatterySaverTips.class.getName();
                    }
                    // [END][seungyeop.yeom][2014-02-28][User Help Guide]
                }

                return;
            } else {
                mTopLevelHeaderId = ai.metaData.getInt(META_DATA_KEY_HEADER_ID);
                mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                if (mFragmentClass != null && mFragmentClass.equals("com.android.settings.wifi.wifiscreen.WifiScreenSettings") && !Utils.isWifiOnly(this)) {
                    mTopLevelHeaderId = R.id.share_connect;
                }
            }

            // Check if it has a parent specified and create a Header object
            final int parentHeaderTitleRes = ai.metaData.getInt(META_DATA_KEY_PARENT_TITLE);
            String parentFragmentClass = ai.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
            if (parentFragmentClass != null) {
                mParentHeader = new Header();
                mParentHeader.fragment = parentFragmentClass;
                if (parentHeaderTitleRes != 0) {
                    mParentHeader.title = getResources().getString(parentHeaderTitleRes);
                }
            }
        } catch (NameNotFoundException nnfe) {
            // No recovery
        }
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().checkDeviceEncryptionForSplitUI(
                    getApplicationContext())) {
                return;
            }
        }
        super.onListItemClick(l, v, position, id);
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        private final WifiEnabler mWifiEnabler;
        private final WifiCallingEnabler mWifiCallingEnabler; // rebestm - WFC
        private final BluetoothEnabler mBluetoothEnabler;
        private AuthenticatorHelper mAuthHelper;
        // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
        private final AirplaneModeEnabler_VZW mAirplaneModeEnabler_VZW;
        //[END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
        private final DataUsageEnabler mDataUsageEnabler;
        private final Use4GNetworkEnabler mUse4GNetworkEnabler;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            View divider_; // ksson
            Switch switch_;
            ImageView arrow;
            LinearLayout mFolderLayout;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
                if (header.id == R.id.cloud || header.id == R.id.account_cloud
                        || header.id == R.id.guest_mode
                        || header.id == R.id.account_add
                        || header.id == R.id.tphone_mode
                        || header.id == R.id.au_settings
                        || header.id == R.id.cover_settings
                        || header.id == R.id.flip_close_settings
                        || header.id == R.id.block_mode_settings) {
                    return HEADER_TYPE_NORMAL;
                } else if (header.id == R.id.use_4g_network) {
                    return HEADER_TYPE_SWITCH;
                } else {
                    // [END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
                    return HEADER_TYPE_CATEGORY;
                }
            } else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings
                    || header.id == R.id.wifi_calling_settings
                    || header.id == R.id.use_4g_network
                    || ((!Utils.isUpgradeModel() && !Utils.isVeeModel()
                            && !mIsWifiOnly
                            && !sIsSubUserOnly
                            && !"VZW".equals(Config.getOperator())
                            && !"ATT".equals(Config.getOperator()))
                            && (header.id == R.id.data_usage_settings))) {
                if (mIsWifiOnly) {
                    if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
                        return HEADER_TYPE_NORMAL;
                    }
                } else if ( Utils.isTablet() ) {
                    if (header.id == R.id.wifi_settings
                            || header.id == R.id.airplane_mode_settings
                            || header.id == R.id.bluetooth_settings
                            || header.id == R.id.data_usage_settings) {
                        return HEADER_TYPE_NORMAL;
                    }
                } else if (sIsSubUserOnly) {
                    if (header.id == R.id.data_usage_settings) {
                        return HEADER_TYPE_NORMAL;
                    }
                }
                return HEADER_TYPE_SWITCH;
            } else if ( header.id == R.id.airplane_mode_settings  ) {
                if ( Utils.isTablet() && "US".equals(Config.getCountry())) {
                    return HEADER_TYPE_NORMAL;
                } else {
                    return HEADER_TYPE_SWITCH;
                }
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }
        // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
        public void updateAirplaneModeEnabler(boolean isChecked) {
            Log.d(LOG_TAG, "updateAirplaneModeEnabler(isChecked) =  " + isChecked);
            if (mAirplaneModeEnabler_VZW != null) {
                mAirplaneModeEnabler_VZW.setSwitchChecked(isChecked);
            }
        }
        //[END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
        
        public void updateAirplaneModeEnablerInECM() {
            if (mAirplaneModeEnabler_VZW != null) {
                mAirplaneModeEnabler_VZW.setAirplaneModeInECM(true, true);
            }
        }
        
        public void updateUse4gNetworkEnabler(boolean isChecked) {
            Log.d(LOG_TAG, "updateUse4gNetworkEnabler() =  " + isChecked);
            if (mUse4GNetworkEnabler != null) {
                mUse4GNetworkEnabler.setSwitchChecked(isChecked);
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects,
                AuthenticatorHelper authenticatorHelper) {
            super(context, 0, objects);

            mAuthHelper = authenticatorHelper;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (Utils.supportRemoteFragment(getContext()) == true) {
                mInflater = new RemoteLayoutInflater(mInflater, getContext());
				((RemoteLayoutInflater) mInflater)
						.setResources(mMyOwnResources);
            }
            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            // rebestm - exception Airplane mode for Atable_VZW
            if (Config.supportAirplaneListMenu() && !Utils.isTablet()) {
                mAirplaneModeEnabler_VZW = new AirplaneModeEnabler_VZW(context, new Switch(context), null);
            } else {
                mAirplaneModeEnabler_VZW = null;
            }
            // [END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            if (!Utils.isUpgradeModel() && !Utils.isVeeModel(context)
                && !mIsWifiOnly && !sIsSubUserOnly && !"ATT".equals(Config.getOperator()) && !"VZW".equals(Config.getOperator())) {
                mDataUsageEnabler = new DataUsageEnabler(context, new Switch(context), null);
            } else {
                mDataUsageEnabler = null;
            }

            if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                mUse4GNetworkEnabler = new Use4GNetworkEnabler(context, new Switch(context), null);
            } else {
                mUse4GNetworkEnabler = null;
            }
            mWifiEnabler = new WifiEnabler(context, new Switch(context));
            mBluetoothEnabler = new BluetoothEnabler(context, new Switch(context));
            mWifiCallingEnabler = new WifiCallingEnabler(context, new Switch(context)); // rebestm - WFC
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            // for remote fragment loading [START]
			Resources resources = mMyOwnResources;
            LayoutInflater inflater = mInflater;
            /* If header is builded from remote context, the resources also should be from remote context. */
            if (Utils.supportRemoteFragment(getContext()) == true) {
                boolean flag = false;
                if (header.extras != null) {
                    String packageName = header.extras.getString(RemoteFragmentManager.EXTRA_HEADER_DATA_PACKAGE_NAME);
                    boolean replace_flag = header.extras.getBoolean(RemoteFragmentManager.EXTRA_HEADER_DATA_REPLACE_FLAG);
                    Context remote_context =
                            RemoteFragmentManager.getRemoteContext(packageName);
                    if (remote_context != null && replace_flag == false) {
                        resources = remote_context.getResources();
                        flag = true;
                    }
                }
                //                String header_title = header.getTitle(resources).toString();
                //Log.d("kimyow", "getView(), "+header_title+", This is using resources from package. isRemote? ="+flag);
                // In getView(), the layoutInflater should be local inflater always. (Not remote context's inflater)
                //                inflater = new RemoteLayoutInflater(mInflater, getContext());
                //            	((RemoteLayoutInflater)inflater).setResources(getMyOwnResources());
            }

            // for remote fragment loading [END]

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    // [START][2014-10-15][seungyeop.yeom] modify style of category (able Lower case)
                    if (header.id == R.id.au_settings_section) {
                        TextView mTextView = null;
                        mTextView = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        mTextView.setAllCaps(false);
                        view = mTextView;
                        holder.title = (TextView)view;
                    } else {
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView)view;
                    }
                    // [END][2104-10-15][seungyeop.yeom] modify style of category (able Lower case)
                    break;

                case HEADER_TYPE_SWITCH:
                    view = inflater.inflate(R.layout.preference_header_switch_item, parent,
                            false);
                    holder.icon = (ImageView) view.findViewById(R.id.icon);
                    holder.title = (TextView)
                            view.findViewById(com.android.internal.R.id.title);
                    holder.summary = (TextView)
                            view.findViewById(com.android.internal.R.id.summary);
                    holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);
                    // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
                    if (Config.supportAirplaneListMenu()) {
                        holder.divider_ = (View)view.findViewById(R.id.switchDivider);
                        if (header.id == R.id.airplane_mode_settings) {
                            holder.divider_.setVisibility(View.GONE);
                        }
                    }
                    if (header.id == R.id.use_4g_network) {
                        holder.divider_ = (View)view.findViewById(R.id.switchDivider);
                        holder.divider_.setVisibility(View.GONE);
                    }
                    // [END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.

                    // [START][2014-08-11][seungyeop.yeom] add condition of activity component for Folder model
                    if (Utils.isFolderModel(getContext())) {
                        final long headerID = header.id;
                        holder.mFolderLayout = (LinearLayout)view.findViewById(R.id.list_header);
                        holder.mFolderLayout.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setFolderComponent(v, headerID);
                            }
                        });
                    }
                    // [END]
                    break;

                case HEADER_TYPE_NORMAL:
                    view = inflater.inflate(
                            R.layout.preference_header_item, parent,
                            false);
                    holder.icon = (ImageView) view.findViewById(R.id.icon);
                    holder.title = (TextView)
                            view.findViewById(R.id.title);
                    holder.summary = (TextView)
                            view.findViewById(R.id.summary);
                    holder.arrow = (ImageView)view.findViewById(R.id.header_arrow);
                    break;
                default:
                    break;
                }
                view.setTag(holder);
				if (Utils.supportSplitView(getContext())
						&& headerType != HEADER_TYPE_CATEGORY) {
					Drawable background = mMyOwnResources
							.getDrawable(R.drawable.list_selector_holo_light);
                    view.setBackground(background);
                }

            } else {
                view = convertView;
                holder = (HeaderViewHolder)view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
            case HEADER_TYPE_CATEGORY:
                holder.title.setText(header.getTitle(/*getContext().getResources()*/resources));
                break;

            case HEADER_TYPE_SWITCH:
                // Would need a different treatment if the main menu had more switches
                if (header.id == R.id.wifi_settings) {
                    if ( "VZW".equals(Config.getOperator()) ) {
                        mWifiEnabler.setSwitch(holder.switch_, holder.summary);
                    } else {
                        mWifiEnabler.setSwitch(holder.switch_);
                    }
                    // rebestm - WFC
                } else if (header.id == R.id.wifi_calling_settings) {
                    if ( mWifiCallingEnabler != null &&  holder != null ) {
                    mWifiCallingEnabler.setSwitch(holder.switch_);
                    }
                } else if (header.id == R.id.bluetooth_settings) {
                    if ( "VZW".equals(Config.getOperator()) ) {
                        mBluetoothEnabler.setSwitch(holder.switch_, holder.summary);
                    } else {
                        mBluetoothEnabler.setSwitch(holder.switch_);
                    }
                    // [START_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
                    // rebestm - exception Airplane mode for Atable_VZW
                } else if (Config.supportAirplaneListMenu() && header.id == R.id.airplane_mode_settings && !Utils.isTablet()) {
                    if (holder.divider_ != null) {
                        holder.divider_.setVisibility(View.GONE);
                    }
                    mAirplaneModeEnabler_VZW.setSwitch(holder.switch_);
                    // [END_LGE_SETTINGS], ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
                } else if ((!Utils.isUpgradeModel()
                            && !Utils.isVeeModel(getContext())
                            && !mIsWifiOnly
                            && !sIsSubUserOnly
                            && !"VZW".equals(Config.getOperator())
                            && !"ATT".equals(Config.getOperator()))
                            && (header.id == R.id.data_usage_settings)) {
                    if (mDataUsageEnabler != null) {
                        mDataUsageEnabler.setSwitch(holder.switch_);
                    }
                } else if (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                            && (null != mUse4GNetworkEnabler)
                            && (header.id == R.id.use_4g_network)) {
                    mUse4GNetworkEnabler.setSwitch(holder.switch_);
                } else {

                }
                // No break, fall through on purpose to update common fields

                //$FALL-THROUGH$
            case HEADER_TYPE_NORMAL:
                if (header.extras != null
                && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                    String accType = header.extras.getString(
                            ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                    ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
//                    lp.width = /*getContext().getResources()*/resources.getDimensionPixelSize(
//                            R.dimen.header_icon_width);
                    Drawable drawable = resources.getDrawable(R.drawable.ic_settings_battery);
                    lp.width = drawable.getIntrinsicWidth();
                    lp.height = lp.width;
                    holder.icon.setLayoutParams(lp);
                    Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                    holder.icon.setImageDrawable(icon);
                } else {
                    // for remote fragment loading
                    if (header.iconRes > 0) {
                        Drawable drawable = resources.getDrawable(header.iconRes);
                        if (holder != null && holder.icon != null) {
                            holder.icon.setImageDrawable(drawable);
                        }
                    } else {
                        holder.icon.setImageDrawable(null);
                    }
                }
                holder.title.setText(header.getTitle(/*getContext().getResources()*/resources));
                CharSequence summary = header.getSummary(/*getContext().getResources()*/resources);

                // rebestm - 20130613 add summary
                //[s][chris.won@lge.com][2013-06-12] Do not set summary if holder.summary exists

                if (holder.summary != null) {
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else if (header.id == R.id.wifi_settings ||
                            header.id == R.id.bluetooth_settings) {
                        if (!TextUtils.isEmpty(holder.summary.getText())) {
                            holder.summary.setVisibility(View.VISIBLE);
                        } else {
                            holder.summary.setVisibility(View.GONE);
                        }
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                }

                //[s][chris.won@lge.com][2013-06-12] Do not set summary if holder.summary exists

                //[S][2012.04.18][youngmin.jeon][LG730] Replace menu title for LG730("Global Roaming" -> "Roaming")
                if (header.id == R.id.roaming_lgt) {
                    if (true == "SPRINT".equals(Config.getOperator()) || true == "BM".equals(Config.getOperator())
                            || true == "SPR".equals(Config.getOperator())) // [2012.04.30][youngmin.jeon] Add SPR operator for Roaming menu.
                    {
                        if (holder.title != null) {
                            holder.title.setText(resources.getString(R.string.status_roaming));
                        }
                    }
                    else if (true == "SKT".equals(Config.getOperator())) { //shlee1219 20120531 SKT Roaming menu add
                        if (holder.icon != null) {
                            holder.icon.setImageDrawable(resources.getDrawable(R.drawable.ic_settings_roaming_skt));
                        } // kiseok.son 20120810 Changed SKT Roaming menu icon.
                        if (holder.title != null) {
                            holder.title.setText(resources.getString(R.string.sp_troaming_NORMAL));
                        }
                    }
                }
                //[E][2012.04.18][youngmin.jeon][LG730] Replace menu title for LG730("Global Roaming" -> "Roaming")
                if (header.id == R.id.call_settings) { //shlee1219 20120612 Add to DCM call setting menu
                    if (true == "DCM".equals(Config.getOperator()))
                    {
                        if (holder.title != null) {
                            holder.title.setText(resources.getString(R.string.call_settings_title));
                        }
                    }
                }
                break;
            default:
                break;
            }

            if (Utils.supportSplitView(getContext()) && headerType != HEADER_TYPE_CATEGORY) {
                if (holder != null && holder.title != null) {
					holder.title.setTextColor(mMyOwnResources
							.getColorStateList(R.color.header_text_color));
                }

                if (holder != null && holder.summary != null) {
					holder.summary.setTextColor(mMyOwnResources
							.getColorStateList(R.color.header_text_color));
                }

                if (holder != null && holder.arrow != null) {
                	if (Utils.isRTLLanguage() || Utils.isEnglishDigitRTLLanguage()) {
                		holder.arrow.setImageDrawable(resources.getDrawable(R.drawable.ic_list_selected_arrow_light_rtl));
                	}
                }
                /*if (holder != null && holder.arrow != null) {
                	if (header.fragment != null && mSelectedHeader != null
                			&& mSelectedHeader.fragment != null
                			&& mSelectedHeader.fragment.equals(header.fragment)) {
                		holder.arrow.setVisibility(View.VISIBLE);
                	} else if (mSelectedHeader != null && header.id == mSelectedHeader.id) {
                        holder.arrow.setVisibility(View.VISIBLE);
                    } else {
                        holder.arrow.setVisibility(View.GONE);
                    }
                }*/
            } else {
                if (holder != null && holder.arrow != null) {
                    holder.arrow.setVisibility(View.GONE);
                }
            }
            return view;
        }

        // [START][2014-08-11][seungyeop.yeom] add condition of activity component for Folder model
        private void setFolderComponent(View v, long id) {
            ComponentName c;
            if (id == R.id.wifi_settings) {
                Intent i = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                v.getContext().startActivity(i);
            } else if (id == R.id.bluetooth_settings) {
                Intent i = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                v.getContext().startActivity(i);
            } else if (id == R.id.data_usage_settings) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                c = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity");
                i.setComponent(c);
                v.getContext().startActivity(i);
            }
        }
        // [END]

        public void resume() {
            // rebestm - exception Airplane mode for Atable_VZW
            if (Config.supportAirplaneListMenu() && !Utils.isTablet()
                    && (mAirplaneModeEnabler_VZW != null)) {
                mAirplaneModeEnabler_VZW.resume(); // ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            }
            if (!Utils.isUpgradeModel() && !Utils.isVeeModel(getContext())
                && !mIsWifiOnly && !sIsSubUserOnly && !"ATT".equals(Config.getOperator()) && !"VZW".equals(Config.getOperator())) {
                if (mDataUsageEnabler != null) {
                    mDataUsageEnabler.resume();
                }
            }
            if (("CTC".equals(Config.getOperator()) ||
                    "CTO".equals(Config.getOperator()))
                && null != mUse4GNetworkEnabler) {
                mUse4GNetworkEnabler.resume();
            }
            mWifiEnabler.resume();
            mWifiCallingEnabler.resume();
            mBluetoothEnabler.resume();
        }

        public void pause() {
            // rebestm - exception Airplane mode for Atable_VZW
            if (Config.supportAirplaneListMenu()&& !Utils.isTablet()
                    && (mAirplaneModeEnabler_VZW != null)) {
                mAirplaneModeEnabler_VZW.pause(); // ADD, kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
            }
            if (!Utils.isUpgradeModel() && !Utils.isVeeModel(getContext())
                && !mIsWifiOnly && !sIsSubUserOnly && !"ATT".equals(Config.getOperator()) && !"VZW".equals(Config.getOperator())) {
                if (mDataUsageEnabler != null) {
                    mDataUsageEnabler.pause();
                }
            }
            if (("CTC".equals(Config.getOperator()) ||
                    "CTO".equals(Config.getOperator()))
                && null != mUse4GNetworkEnabler) {
                mUse4GNetworkEnabler.pause();
            }
            mWifiEnabler.pause();
            mWifiCallingEnabler.pause();
            mBluetoothEnabler.pause();
        }
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        boolean revert = false;
        if (header.id == R.id.account_add) {
            revert = true;
            if (SettingsBreadCrumb.isAttached(this)) {
                header.intent = null;
                revert = false;
                Log.d("jw", "AddAccountSettingsFragment_A_vzw");
            } else {
                header.fragment = null;
            }
            super.onHeaderClick(header, position);
        } else if (header.id == R.id.docomo_settings) {
            if (appIsEnabled(DOCOMO_SERVICE)) {
                super.onHeaderClick(header, position);
            } else {
                confirmDialog(DOCOMO_SERVICE);
            }
        } else if (header.id == R.id.yahoo_box_settings) {
            if (appIsEnabled("jp.co.yahoo.android.ybox")) {
                super.onHeaderClick(header, position);
            } else {
                confirmDialog("jp.co.yahoo.android.ybox");
            }
        } else if (Config.supportAirplaneListMenu() && header.id == R.id.airplane_mode_settings) {
            if (!Utils.isTablet()) {
                Log.d(LOG_TAG, "airplane switch click");
                boolean isChecked = android.provider.Settings.Global.getInt(getContentResolver(),
                        android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                ListAdapter listAdapter = getListAdapter();
                if ((listAdapter != null) && (listAdapter instanceof HeaderAdapter)) {
                    ((HeaderAdapter)listAdapter).updateAirplaneModeEnabler(isChecked);
                }
            } else {
                super.onHeaderClick(header, position);
            }
        } else if (header.id == R.id.use_4g_network) {
            Log.d(LOG_TAG, "use_4g_network click");
            boolean isChecked = android.provider.Settings.Global.getInt(getContentResolver(),
                    "use_4g_network_onoff", 1) != 0;
            ListAdapter listAdapter = getListAdapter();
            if ((listAdapter != null) && (listAdapter instanceof HeaderAdapter)) {
                ((HeaderAdapter)listAdapter).updateUse4gNetworkEnabler(isChecked);
            }
        } else if (header.id == R.id.au_settings) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.kddi.android.au_setting_menu",
                    "com.kddi.android.au_setting_menu.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("mode", "Start");
            startActivity(intent);
        } else if (header.id == R.id.activate_device_settings) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setAction("com.sprint.dsa.DSA_ACTIVITY");
            intent.setType("vnd.sprint.dsa/vnd.sprint.dsa.main");
            intent.putExtra("com.sprint.dsa.source", "menu");
            startActivity(intent);
            // This is ONLY for tablet (guwan.jung, 2013.10.28)
        } else if ((Utils.isTablet() == false && header.id == R.id.cloud) || header.id == R.id.account_cloud) {
            Intent intent = new Intent("com.lge.cloudhub.action.ACCOUNT_LIST");
            Log.i("hsmodel", "packageName : " + getPackageName());
            intent.putExtra("package.name", getPackageName());
            startActivity(intent);
        } else if (header.id == R.id.guest_mode) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.lge.launcher2", "com.lge.launcher2.plushome.setting.LGPlusHomeSetting");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (header.id == R.id.flip_close_settings) {
            showFlipCloseDialog(this);
        } else if (header.id == R.id.dualsim_settings) {
            if (isAirplaneModeOn(this)) {
                Toast.makeText(this,
                        getString(R.string.sp_unavailable_dualsim_airplane_mode),
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (isInCall()) {
                Toast.makeText(this,
                        getString(R.string.sp_calling_pass_dualsim_activity_NORMAL),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                super.onHeaderClick(header, position);
            }
        } else if (header.id == R.id.dualsim_settings_triple_qct) {
            if (isAirplaneModeOn(this)) {
                Toast.makeText(this,
                        getString(R.string.sp_unavailable_triplesim_airplane_mode),
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (isInCall()) {
                Toast.makeText(this,
                        getString(R.string.sp_calling_pass_dualsim_activity_NORMAL),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                super.onHeaderClick(header, position);
            }
        } else if (header.id == R.id.tphone_mode) {
            ComponentName c;
            c = new ComponentName("com.android.settings",
                    "com.android.settings.lge.SKTPhoneMode");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(c);
            startActivity(i);
        } else if (header.id == R.id.usb_settings) {
        	Bundle mBundle = new Bundle();
        	mBundle.putBoolean(UsbSettingsControl.EXTRA_USB_LAUNCHER, false);
        	header.fragmentArguments = mBundle;
        	super.onHeaderClick(header, position);
        } else if (header.id == R.id.block_mode_settings) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction("com.lge.intent.action.privacymanager");
            intent.setPackage("com.android.mms");
            startActivity(intent);
        } else {
            // [Account] This is for BUA account
			//[A_VZW][jongwon007.kim]
            if (Utils.supportSplitView(this) && "VZW".equals(Config.getOperator())) {
                mBreadCrumbTitleDisplayedFlag = false;
            }
            String accountType = null;
            if (header != null && header.extras != null) {
                accountType = header.extras.getString(ManageAccountsSettings.KEY_ACCOUNT_TYPE);
            }

            if (accountType != null && accountType.equals("com.fusionone.account")) {
                if (!cloudEnable()) {
                    Intent intent = new Intent("com.lge.vzw.bua.intent.action.SUBSCRIBE");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName("com.lge.cloudvmm", "com.lge.cloudvmm.ApplicationGateActivity"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
            try {
                super.onHeaderClick(header, position);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Activity is not found", Toast.LENGTH_SHORT).show();
            }
        }

        if (revert && mLastHeader != null) {
            highlightHeader((int)mLastHeader.id);
        } else {
            mLastHeader = header;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("kimyow", "onCreateOptionsMenu " + menu.size());
        if (Utils.supportEasySettings(this) && Utils.isUI_4_1_model(this)) {
            if (menu.findItem(SETTING_STYLE_ITEM_ID) == null
                    && (findViewById(com.android.internal.R.id.headers).getVisibility() != View.GONE)) {
                menu.add(0, SETTING_STYLE_ITEM_ID, 0, mMyOwnResources.getString(R.string.sp_tab_view_NORMAL));
            }
        }       
        if (mMyOwnResources.getBoolean(R.bool.config_settings_search_enable)) {
            MenuItem mi = menu.add(0, SETTINGS_SEARCH_ID, 0, mMyOwnResources.getString(R.string.settings_search_hint));
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            int[] attrs = new int[]{android.R.attr.actionModeWebSearchDrawable};
            TypedArray ta = obtainStyledAttributes(attrs);
            Drawable d = ta.getDrawable(0);
            mi.setIcon(d);
            ta.recycle();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = false;
        Log.d("kimyow", "onPrepareOptionsMenu " + menu.size());
        if (Utils.supportEasySettings(this) && checkActivity()) {
            if (menu.findItem(SETTING_STYLE_ITEM_ID) == null) {
                menu.add(0, SETTING_STYLE_ITEM_ID, 0, mMyOwnResources.getString(R.string.sp_tab_view_NORMAL));
            }
            result = true;
        }

        if (Utils.isUI_4_1_model(this) == true) {
            result = true;
        }

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Utils.supportEasySettings(this) && item.getItemId() == SETTING_STYLE_ITEM_ID) {
            switchToTab();
            finish();
        } else if (item.getItemId() == SETTINGS_SEARCH_ID) {
            switchToSearchResults();
        } else if (item.getItemId() == android.R.id.home) {
            int settingStyle = android.provider.Settings.System.getInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                String parentActivityName = Utils.getParentActivityName(this, topActivity);
                Log.d("kimyow", "top=" + topActivityClassName + "  base=" + baseActivityClassName);
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if (("com.android.settings." + getResources().getString(R.string.parent_activity_hotspot)).equals(parentActivityName)) {
                    Intent parentIntent = new Intent();
                    Log.d("CHRISWON", "CHRISWON : Doing nothing! + " + parentActivityName);
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    parentIntent.setClassName("com.android.settings", parentActivityName);
                    startActivity(parentIntent);
                    finish();
                    return true;
                } else if (("com.android.settings." + getResources().getString(R.string.parent_activity_hotspot)).equals(baseActivityClassName)
                        && !baseActivityClassName.equals(topActivityClassName)) {
                    if ( "com.android.settings.SubSettings".equals(topActivityClassName) &&
                        "com.android.settings.Settings$TetherNetworkSettingsActivity".equals(baseActivityClassName)) {
                        Log.d ("CHRISWON", "kjo - onBackPressed");
                        onBackPressed();
                        return true;
                    } else {
                    Intent baseIntent = new Intent();
                    Log.d("CHRISWON", "CHRISWON : Doing nothing! + " + baseActivityClassName);
                    baseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    baseIntent.setClassName("com.android.settings", baseActivityClassName);
                    startActivity(baseIntent);
                    finish();
                    return true;
                    }
                } else if (parentActivityName != null
                        && parentActivityName.equals("com.android.settings.Settings$WifiSettingsActivity")) {
                    Intent parentIntent = new Intent();
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    parentIntent.setClassName("com.android.settings", parentActivityName);
                    startActivity(parentIntent);
                    finish();
                    return true;
                } else if (("com.android.settings.Settings$DataUsageSummaryActivity".equals(baseActivityClassName) ||
                        "com.android.settings.Settings$ShareConnectionActivity".equals(baseActivityClassName))
                        && !baseActivityClassName.equals(topActivityClassName)) {

                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings", baseActivityClassName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                    finish();
                    return true;
                    //jangkeun.jeong@lge.com 20121227, operation up button for WifiP2pSettings
                } else if ( "com.android.settings.Settings$WifiSettingsActivity".equals(baseActivityClassName) && !topActivityClassName.equals(baseActivityClassName) ) {
                    //Intent intent = new Intent();
                    Log.d("JKJK", "baseActivityClassName: " + baseActivityClassName + " | topActivityClassName: " + topActivityClassName);
                    /*
                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                       Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                       Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                       intent.setClassName("com.android.settings", baseActivityClassName);
                       startActivity(intent);
                       finish();
                     */
                    onBackPressed();
                    return true;
                    //jangkeun.jeong@lge.com 20121227, operation up button for WifiP2pSettings
                    //added by jangkeun.jeong
                } else if ( "com.android.settings.Settings$WifiP2pSettingsActivity".equals(baseActivityClassName) && !topActivityClassName.equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                    //added by jangkeun.jeong
                } else if ("com.android.settings.SubSettings".equals(topActivityClassName) &&
                           "com.android.browser.BrowserActivity".equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                    //BT  Paired Device Settings , operation up button ,  20130118
                } else if ( "com.android.settings.SubSettings".equals(topActivityClassName) &&
                        "com.android.settings.Settings$TetherNetworkSettingsActivity".equals(baseActivityClassName)) {
                    Log.d("TEST", "It from SubSettings");
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.setClassName("com.android.settings", baseActivityClassName);
                    startActivity(intent);
                    finish();
                    return true;
                //[TD#20935] ActionBar up button working onBackPressed(), 2014-04-04
                } else if ("com.android.settings.SubSettings".equals(topActivityClassName) &&
                           "com.android.gallery3d.app.Gallery".equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$PowerSaveBatteryDetailActivity".equals(baseActivityClassName) && !topActivityClassName.equals(baseActivityClassName) ) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$AccountsGroupSettingsActivity"
                        .equals(baseActivityClassName)
                        && !topActivityClassName.equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$BatterySettingsActivity"
                        .equals(baseActivityClassName)
                        && "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if (
                        MULTITASKING_CLASSNAME
                        .equals(baseActivityClassName)
                        && !topActivityClassName
                        .equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$AccessibilitySettingsActivity"
                            .equals(baseActivityClassName) &&
                            "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    Log.i("Accessibility", "short cut init case");
                    onBackPressed();
                } else if ("com.android.LGSetupWizard.SetupFlowController"
                        .equals(baseActivityClassName) &&
                        "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    Log.d("Accessibility", "setup wizard init case");
                    onBackPressed();
                } else if ("com.android.settings.SubSettings".equals(topActivityClassName) &&
                        "com.android.settings.Settings$StorageSettingsActivity".equals(baseActivityClassName)) { 
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$LockSettingsActivity"
                        .equals(baseActivityClassName)
                        && "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$UserSettingsActivity"
                        .equals(baseActivityClassName)
                        && "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$LocationSettingsActivity"
                        .equals(baseActivityClassName)
                        && "com.android.settings.SubSettings".equals(topActivityClassName)) {
                    onBackPressed();
                    return true;
                } else if ("com.android.settings.Settings$NfcSettingsFragmentActivity".equals(baseActivityClassName)) {
                    onBackPressed();
                    return true;
                } else {
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                    Log.d("kimyow", "tabIndex=" + tabIndex);
                    settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                    startActivity(settings);
                    finish();
                    return true;
                }
            } else {
                String topActivityName = getTopActivityName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();

                if ((Config.DCM).equals(Config.getOperator())) {
                    boolean epsmodeOff = SystemProperties.get("persist.sys.epsmodestate", "off").equals("off");
                    boolean isDocomo = SystemProperties.get("ro.build.target_operator").equalsIgnoreCase("DCM");

                    if (isDocomo && !epsmodeOff) {
                        finish();
                        return true;
                    }
                }

                Log.d("kimyow_upkey", "TopActivityName : " + topActivityName);
                Log.d("kimyow_upkey", "baseActivityName : " + baseActivityClassName);
                if ("com.android.settings.SubSettings".equals(topActivityName) != true) {
                    if ("com.android.settings.Settings$AccessLockSummaryActivity"
                            .equals(topActivityName)) {
                        onBackPressed();
                    } else if ("com.android.settings.Settings$AppNotificationSettingsActivity"
                            .equals(topActivityName)) {
                        onBackPressed();
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                          Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        startActivity(settings);
                        finish();
                    }
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String getTopActivityName() {
        try {
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            ComponentName topActivity = info.get(0).topActivity;
            String topActivityClassName = topActivity.getClassName();
            Log.d("kimyow", "topActivity:" + topActivityClassName);
            return topActivityClassName;
        } catch (NullPointerException npe) {
            Log.d("kimyow", "topActivity: NullPointerException");
            return "";
        } // fix WBT issue
    }

    private boolean checkActivity(String partialName) {
        try {
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            ComponentName topActivity = info.get(0).topActivity;
            String topActivityClassName = topActivity.getClassName();
            Log.d("kimyow", "checkActivity:" + topActivityClassName);
            if (partialName != null) {
                return topActivityClassName.contains(partialName);
            } else {
                return ("com.android.settings.Settings".equals(topActivityClassName) /*|| "com.android.settings.SubSettings".equals(topActivityClassName)*/);
            }
        } catch (NullPointerException npe) {
            Log.d("kimyow", "checkActivity: NullPointerException");
            return false;
        }
    }

    private boolean checkActivity() {
        return checkActivity(null);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        // Override the fragment title for Wallpaper settings
        int titleRes = pref.getTitleRes();
        if (pref.getFragment().equals(WallpaperTypeSettings.class.getName())) {
            titleRes = R.string.wallpaper_settings_fragment_title;
        } else if (pref.getFragment().equals(UserDictionarySettings.class.getName())) {
            titleRes = R.string.user_dict_single_settings_title;
        }
        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(),
                null, 0);
        return true;
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, Settings.class));
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // TODO: watch for package upgrades to invalidate cache; see 7206643
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, accounts);
        invalidateHeaders();
    }
    private boolean isVisibleAccounts(String accountType) {
        // TODO Auto-generated method stub

        if (accountType.equals("com.mobileleader.sync")) {
            return false;
        }
        if (accountType.equals("com.lge.sync")) {
            return false;
        }
        if (accountType.equals("com.att.aab") && !Config.getOperator().equals(Config.ATT)) {
            return false;
        }
        if (accountType.equals("com.lge.vmemo")) {
            return false;
        }
        if (accountType.equals("com.lge.android.weather.sync")) {
            return false;
        }
        if (accountType.equals("com.verizon.phone")) {
            return false;
        }
        if (accountType.equals("com.android.sim")) {
            return false;
        }
        if (accountType.equals("com.verizon.tablet")) {
            return false;
        }
        if (accountType.equals("com.lge.lifetracker")) {
            return false;
        }
        if (accountType.equals("com.lge.bioitplatform.sdservice")) {
            return false;
        }
        return true;
    }

    /*
     * Settings subclasses for launching independently.
     */
    //[S][jaeyoon.hyun@lge.com] Add Gesture setting on Settings one depth
    public static class GestureSettingsActivity extends Settings { /* empty */ }
    public static class GestureSettingsVerizonActivity extends Settings { /* empty */ }
    public static class RcseSettingsActivity extends Settings { /* empty */ }
    public static class WirelessSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(getApplicationContext())) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }
    }

    public static class AdvancedCallingActivity extends Settings { /* empty */ }
    public static class ShareConnectionActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActionBar().setIcon(R.drawable.shortcut_share_connect);
        }
    }

    public static class WirelessMoreSettingsActivity extends Settings { /* empty */ }
    public static class TetherNetworkSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
                Log.d("Settings", "[Settings] settings icon");
            } else {
                getActionBar().setIcon(R.drawable.shortcut_networks_setting);
            }
        }
    }
    public static class TetherSettingsActivity extends Settings {
        //[START][TD#201799] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            if ("ATT".equals(Config.getOperator())) {
                setTitle(getResources().getString(R.string.sp_mobile_hotspot_NORMAL));  //2014.05.08 change the breadcrumb using the shortcut icon, eunjungjudy.kim@lge.com
            }        
              
            super.onCreate(savedInstanceState);

            ActionBar actionBar = getActionBar();
            if (Utils.isTablet()) {
               if (actionBar != null) {
                   actionBar.setDisplayHomeAsUpEnabled(false);
               }
            } else {
               if (actionBar != null) {
                   actionBar.setDisplayHomeAsUpEnabled(true);
               }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                // Back button in action bar for DCM, 20130329, hyeondug.yeo@lge.com [S]
                if ("DCM".equals(Config.getOperator())) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.setClassName("com.android.settings",
                            "com.android.settings.Settings$WirelessSettingsActivity");
                    startActivity(i);
                    finish();
                    return true;
                }
                // Back button in action bar for DCM, 20130329, hyeondug.yeo@lge.com [E]
                Intent i = new Intent(Intent.ACTION_VIEW);
                //[TD#21081] addFlags CLEAR and SINGLE_TOP to fix ActionBar bug. 2013-05-02, ilyong.oh@lge.com
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$TetherNetworkSettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        //[E N D][TD#201799] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
    }
    public static class VpnSettingsActivity extends Settings {

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(this)) {
                    i.setClassName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
                } else {
                    if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2
                        && !"VZW".equals(Config.getOperator())) {
                        i.setClassName("com.android.settings", "com.android.settings.Settings$WirelessMoreSettingsActivity");
                    } else {
                        i.setClassName("com.android.settings", "com.android.settings.Settings$TetherNetworkSettingsActivity");
                    }
                }
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        } }
    // [shpark82.park] VPN shortcut [S]
    public static class VpnSelectorActivity extends Settings {

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Utils.isWifiOnly(this)){
                    i.setClassName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
                } else {
                    if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2
                        && !"VZW".equals(Config.getOperator())) {
				        i.setClassName("com.android.settings", "com.android.settings.Settings$WirelessMoreSettingsActivity");
                    } else {
                        i.setClassName("com.android.settings", "com.android.settings.Settings$TetherNetworkSettingsActivity");
                    }
                }
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    // [shpark82.park] VPN shortcut [E]

    public static class AuCustomerSupportActivity extends Settings { /* empty */ }
    public static class DateTimeSettingsActivity extends Settings { /* empty */ }
    public static class StorageSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcut_storage);
            }

        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class WifiCallingSettingsActivity extends Settings { /* empty */ }
    public static class WFCMapActivity extends Settings { /* empty */ }
    public static class WifiSettingsDialogActivity extends Settings { /* empty */ }
    public static class WifiSettingsActivity extends Settings {
        //[START][TD#201799] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ActionBar actionBar = getActionBar();
            if (Utils.isTablet()) {
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            } else {
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.setClassName("com.android.settings", "com.android.settings.Settings");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
        //[E N D][TD#201799] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
    }
    public static class WifiP2pSettingsActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // TODO Auto-generated method stub
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();

                    if (topActivityClassName != null &&
                        topActivityClassName.equals(baseActivityClassName)) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                 | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.setClassName("com.android.settings",
                                       "com.android.settings.Settings$AdvancedWifiSettingsActivity");
                        startActivity(i);
                    }
                    finish();
                    return true;
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                             | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.setClassName("com.android.settings",
                                   "com.android.settings.Settings$AdvancedWifiSettingsActivity");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    //[S][2012.03.07][andrew75.kim@lge.com] Add Wi-Fi Screen Share for QuickSetting
    public static class WifiScreenSettingsActivity extends Settings {
        //[START][TD#201802] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                if ("DCM".equals(Config.getOperator())) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setClassName("com.android.settings",
                            "com.android.settings.Settings$WirelessSettingsActivity");
                    startActivity(i);
                    finish();
                    return true;
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                //[TD#21081] addFlags CLEAR and SINGLE_TOP to fix ActionBar bug. 2013-05-02, ilyong.oh@lge.com
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$ShareConnectionActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        //[E N D][TD#201802] Back button add to action bar, 2013-03-20, ilyong.oh@lge.com
    }
    public static class WifiScreenSettingsDialogActivity extends Settings { /* empty */ }
    //[E][2012.03.07][andrew75.kim@lge.com] Add Wi-Fi Screen Share for QuickSetting
    public static class InputMethodAndLanguageSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
             if (!Utils.supportSplitView(this)) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    //if (isAppliedFunctionIcon) {
                    actionBar.setIcon(R.drawable.shortcut_language);
                    //}
                }
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // TODO Auto-generated method stub
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    //L OS
    public static class ApnSettingsActivity extends Settings { 
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            SLog.i("ApnSettings onOptionsItemSelected === ");
            if (itemId == android.R.id.home) {
                // task stack is not settings stack,
                // so it should be to call the mobile network setting.
                SLog.i("ApnSettings call previous state");
                Intent mIntent = new Intent();
                if ("SPR".equals(Config.getOperator())) {
                    mIntent.setClassName("com.android.phone",
                        "com.android.phone.CallFeatureSettingRoaming");
                } else if ("DCM".equals(Config.getOperator())) {
                    finish();
                    return true;
                } else {
                    mIntent.setClassName("com.lge.networksettings",
                        "com.lge.networksettings.MobileNetworkSettings");
                }
                startActivity(mIntent);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class SoundSettingsMoreActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(
                        getContentResolver(), "settings_style", 0);
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);

                String baseActivityClassName = info.get(0).baseActivity
                        .getClassName();
                Log.d("starmotor", "SoundSettingsMore settingStyle : "
                        + settingStyle);
                Log.d("starmotor",
                        "SoundSettingsMore Utils.supportEasySettings(this) : "
                                + Utils.supportEasySettings(this));
                Log.d("starmotor", "baseActivityClassName : "
                        + baseActivityClassName);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.setAction("com.android.settings.SOUND_SETTINGS");
                startActivity(i);
                finish();
                return true;

            }

            return super.onOptionsItemSelected(item);
        }
    }
    public static class NotificationAppListActivity extends Settings { /* empty */ }

    public static class AppNotificationSettingsActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {

                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity
                        .getClassName();
                Log.d("jw", "top=" + topActivityClassName + "  base="
                        + baseActivityClassName);

                if (baseActivityClassName
                        .equals("com.android.settings.Settings$AppNotificationSettingsActivity")) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setClassName("com.android.settings",
                            "com.android.settings.Settings$NotificationAppListActivity");
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class KeyboardLayoutPickerActivity extends Settings { /* empty */ }
    public static class InputMethodAndSubtypeEnablerActivity extends Settings { /* empty */ }
    public static class SpellCheckersSettingsActivity extends Settings { /* empty */ }
    public static class TextLinkSettingsActivity extends Settings { /* empty */ }
    public static class TextlinkHelperPopup extends Settings { /* empty */ }
    public static class LocalePickerActivity extends Settings { /* empty */ }
    public static class UserDictionarySettingsActivity extends Settings { /* empty */ }
    public static class TrustedCredentialsSettingsActivity extends Settings { /* empty */ }
    public static class ZenModeSettingsActivity extends Settings {
          @Override
          public boolean onOptionsItemSelected(MenuItem item) {
              final int itemId = item.getItemId();
              if (itemId == android.R.id.home) {
                  int settingStyle = android.provider.Settings.System.getInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
                  if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                      Intent i = new Intent(android.provider.Settings.ACTION_SETTINGS);
                      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                      i.putExtra(Intent.EXTRA_TEXT, "sound");
                      startActivity(i);
                      finish();
                      return true;
                  } else {
                      Intent i = new Intent(Intent.ACTION_VIEW);
                      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      i.setClassName("com.android.settings", "com.android.settings.Settings$SoundSettingsActivity");
                      startActivity(i);
                      finish();
                      return true;
                  }
              }
              return super.onOptionsItemSelected(item);
         }
    }
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
    public static class FontSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setIcon(R.drawable.font_type);
            }
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
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]

    public static class ACRSettingsActivity extends Settings { /* empty */ }
    public static class RingtonePickerActivity extends Settings {

        public static final int TYPE_RINGTONE = 1;
        public static final int TYPE_NOTIFICATION = 2;
        public static final int TYPE_ALARM = 4;
        public static final int TYPE_RINGTONE_SIM2 = 8;
        public static final int TYPE_NOTIFICATION_SIM2 = 16;
        public static final int TYPE_RINGTONE_VC = 32;
        public static final int TYPE_RINGTONE_SIM3 = 64;
        public static final int TYPE_NOTIFICATION_SIM3 = 128;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            int parent = super.getIntent().getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
            switch(parent) {
            case TYPE_RINGTONE:
                this.setTitle(R.string.ringtone_title);
                break;
            case TYPE_RINGTONE_SIM2:
                this.setTitle(R.string.sp_sub_sim2_ringtone_title_NORMAL);
                break;
            case TYPE_RINGTONE_SIM3:
                this.setTitle(R.string.sp_sub_sim3_ringtone_title_NORMAL);
                break;
            case TYPE_NOTIFICATION:
                this.setTitle(R.string.sp_sound_noti_NORMAL);
                break;
            case TYPE_NOTIFICATION_SIM2:
                this.setTitle(R.string.sp_sub_sim2_notification_sound_title_NORMAL);
                break;
            case TYPE_NOTIFICATION_SIM3:
                this.setTitle(R.string.sp_sub_sim3_notification_sound_title_NORMAL);
                break;
            case TYPE_RINGTONE_VC:
                this.setTitle(R.string.sp_vc_ringtone_title_NORMAL);
                break;
            default:
                break;
            }

        }

    }
    public static class TouchFeedbackAndSystemPreferenceActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setTitle(R.string.sp_sound_category_feedback_title_NORMAL);
        }
    }
    public static class SoundSettingsActivity extends Settings {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            if (Config.getOperator().equals(Config.VZW)) {
                getActionBar().setTitle(R.string.notification_settings);
            }
            if (Utils.supportSplitView(getApplicationContext())) {
                //getActionBar().setTitle(R.string.settings_label);
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class DisplaySettingsActivity extends Settings {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

    }

    public static class DisplayMoreSettingsActivity extends Settings { /* empty */ }
    /**
     * @date        2013/06/26
     * @author         seungyeop.yeom
     * @brief        The class is declared for EmotionalELD fragment
     */
    public static class EmotionalLEDEffectActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        settings.putExtra(Intent.EXTRA_TEXT, "sound");
                        startActivity(settings);
                        finish();
                        return true;
                    }
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    i.setAction("com.android.settings.SOUND_SETTINGS");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class EmotionalLEDEffectTabActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    settings.putExtra(Intent.EXTRA_TEXT, "sound");
                    startActivity(settings);
                    finish();
                    return true;
                    //}
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    i.setAction("com.android.settings.SOUND_SETTINGS");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class ScreenCaptureAreaActivity extends Settings {
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

    public static class AccessorySettingsActivity extends Settings { /* empty */
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
    // [seungyeop.yeom][2013-12-17][START]
    public static class QuickCaseActivity extends Settings { /* empty */
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

    // [seungyeop.yeom][2014-01-27][START]
    public static class QuickCoverActivity extends Settings { /* empty */
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

    public static class QuickCaseGuideActivity extends Settings { /* empty */
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
    // [seungyeop.yeom][2013-12-17][END]

    // 2013-06-10
    // A 4Team 2Part Yeom Seungyeop Y
    public static class QuickCaseLollipopActivity extends Settings { /* empty */
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

    // [seungyeop.yeom][2014-02-28][START]
    public static class UserHelpGuideActivity extends Settings { /* empty */
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
    // [START]

    public static class SKTVerifyAppsPreferenceActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            this.setTitle(R.string.verify_applications_title_skt);
        }
    }

    public static class MasterClearActivity extends Settings { /* empty */ }
    public static class LlkDemoModeActivity extends Settings { /* empty */ }
    public static class ResetSettingsActivity extends Settings { /* empty */ }
    public static class DeviceInfoSettingsActivity extends Settings { /* empty */ }
    public static class ApplicationSettingsActivity extends Settings { /* empty */ }
    public static class ManageApplicationsActivity extends Settings { /* empty */ }
    public static class AppOpsSummaryActivity extends Settings { /* empty */ }    // rebestm - kk migration
    public static class AccessLockSummaryActivity extends Settings { /* empty */ }
    public static class AccessLockDetailsActivity extends Settings { /* empty */ }
    public static class StorageUseActivity extends Settings { /* empty */ }
    public static class DevelopmentSettingsActivity extends Settings { /* empty */ }
    public static class AccessibilitySettingsActivity extends Settings { /* empty */ }
    public static class LGAccessibilityShortcut extends Settings { /* empty */ }
    public static class SecuritySettingsActivity extends Settings { /* empty */ }
    public static class LocationSettingsActivity extends Settings { /* empty */ }
    public static class PrivacySettingsActivity extends Settings { /* empty */ }
    public static class DreamSettingsActivity extends Settings { /* empty */ }
    public static class KnockONSettingsActivity extends Settings { /* empty */ }
    public static class KnockONSettingsSwitchActivity extends Settings { /* empty */ }
    public static class FrontTouchKeyActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    //if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    //    onBackPressed();
                    //    return true;
                    //} else {
                    Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    settings.putExtra(Intent.EXTRA_TEXT, "display");
                    startActivity(settings);
                    finish();
                    return true;
                    //}
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    i.setClassName("com.android.settings",
                            "com.android.settings.Settings$DisplaySettingsActivity");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class ButtonCombinationDragActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
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
    }
    public static class FrontTouchKeyThemeActivity extends Settings { /* empty */ }
    public static class HideNavigationAppSelectActivity extends Settings { /* empty */ }
    public static class MotionSensorCalibrationActivity extends Settings { /* empty */ }
    public static class RunningServicesActivity extends Settings { /* empty */ }
    public static class ManageAccountsSettingsActivity extends Settings {

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
    public static class ZonePickerActivity extends Settings { /* empty */ }
    public static class ZonePickerCTActivity extends Settings { /* empty */ }
    public static class PowerUsageSummaryActivity extends Settings { /* empty */ }
    public static class SmsDefaultSettingsActivity extends Settings {
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            // TODO Auto-generated method stub
//            super.onCreate(savedInstanceState);
//        }
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            final int itemId = item.getItemId();
//            if (itemId == android.R.id.home) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                i.setClassName("com.android.settings", "com.android.settings.Settings$ShareConnectionActivity");
//                startActivity(i);
//                finish();
//                return true;
//
//            }
//            return super.onOptionsItemSelected(item);
//        }
    } 

    public static class CaptioningSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Intent i = null;
            
            if (Utils.isTablet()) {
            	if (Utils.checkPackage(this, "com.android.settingsaccessibility")) {
            		Log.d("Settings", "launch normal caption setting");
	                i = new Intent("com.lge.accessibility.CAPTIONING_SETTINGS");            	
            	} else {
                	Log.d("Settings", "launch tablet caption setting");
                	i = new Intent("com.lge.accessibility.CAPTIONING_SETTINGS_TABLET");
                }
            } else {
                if (!Utils.checkPackage(this, "com.android.settingsaccessibility")) {
                    return;
                }
                Log.d("Settings", "launch normal caption setting");
                i = new Intent("com.lge.accessibility.CAPTIONING_SETTINGS");
            }

            startActivity(i);
            finish();
        }
    }
    
    public static class AccountSyncSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcut_account);
            }
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
    public static class AccountSyncSettingsInAddAccountActivity extends Settings { /* empty */ }
    public static class CryptKeeperSettingsActivity extends Settings { /* empty */ }
    public static class UnCryptKeeperSettingsActivity extends Settings { /* empty */ }
    public static class DeviceAdminSettingsActivity extends Settings { /* empty */ }
    public static class CertificateManagementSettingsActivity extends Settings { /* empty */ }
    public static class ScreenPinningSettingsActivity extends Settings { /* empty */ }
    public static class TrustAgentSettingsActivity extends Settings { /* empty */ }

    public static class DataUsageSummaryMultiSIMActivity extends Settings { /* empty */ }
    public static class DataUsageSummaryActivity extends Settings {
        //[S][2013.03.27][jaeyoon.hyun@lge.com][ATT]] Set DataUsageSummaryActivity Icon
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                Log.d("YSY", "Settings, Utils.supportSplitView(this)");
                getActionBar().setTitle(R.string.settings_label);
            } else if (mIsWifiOnly) {
                // A-WIFI setting-data usage update fix
                Log.d("YSY", "Settings, Utils.isWifiOnly(context)");
                getActionBar().setTitle(R.string.data_usage_summary_title);
            } else {
                Log.d("YSY", "DataUsageSummary, else");
                setTitle(R.string.data_usage_enable_mobile);

                if ("SKT".equals(Config.getOperator())
                        || "KT".equals(Config.getOperator())) {
                    Log.d("YSY", "Settings, SKT || KT");
                    setTitle(R.string.data_network_settings_title);
                } else if ("ATT".equals(Config.getOperator())) {
                    Log.d("YSY", "Settings, ATT");
                    setTitle(R.string.shortcut_datausage_att);
                }
            }
        }
    }
    //[E][2013.03.27][jaeyoon.hyun@lge.com][ATT]] Set DataUsageSummaryActivity Icon
    public static class AdvancedWifiSettingsActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // TODO Auto-generated method stub
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                         | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.setClassName("com.android.settings",
                               "com.android.settings.Settings$WifiSettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    // [S][2012.12.06][andrew75.kim@lge.com] Manage networks for VZW
    public static class ManageNetworksActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //if (isAppliedFunctionIcon) {
            getActionBar().setIcon(R.drawable.shortcut_wifi);
            //}
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
    // [E][2012.12.06][andrew75.kim@lge.com] Manage networks for VZW
    public static class TextToSpeechSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                if (android.provider.Settings.Global.getInt(getContentResolver(),
                        android.provider.Settings.Global.DEVICE_PROVISIONED, 0) == 0) {
                    onBackPressed();
                    return true;
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setClassName("com.android.settings",
                            "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        } }
    public static class AndroidBeamSettingsActivity extends Settings { /* empty */ }
    public static class NfcSettingsFragmentActivity extends Settings {  //jw_skt_modify_menu
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);

            if (mIsWifiOnly || Utils.isTablet()) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.setClassName("com.android.settings", "com.android.settings.Settings$ShareConnectionActivity");
                startActivity(i);
                finish();
                return true;

            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class NfcSettingsActivity extends Settings { // rebestm - fix shortcut actionbar
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                if (mIsWifiOnly || Utils.isTablet()) {
                    //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                    //getActionBar().setTitle(R.string.settings_label);
                }
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                SharedPreferences preferences 
                    = getApplicationContext().getSharedPreferences("NfcUIMinit", 0);
                Boolean bFromJump = preferences.getBoolean("NFC_FromJump", false);
                Log.d ("kjo", "[onOptionsItemSelected] bFromJump = " + bFromJump);
                if ( !bFromJump ) {
                    onBackPressed();
                    return true;
                } else {
                    if ("DCM".equals(Config.getOperator())) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.setClassName("com.android.settings",
                                "com.android.settings.Settings$WirelessSettingsActivity");
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.setClassName("com.android.settings"
                                , "com.android.settings.Settings$ShareConnectionActivity");
                        startActivity(i);
                        finish();
                    }
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class ConnectivitySettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class ConnectivityHelperPopupActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
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

    public static class OneHandOperationSettingsActivity extends Settings { /* empty */ }
    public static class HotkeySettingsActivity extends Settings { /* empty */ }
    public static class Hotkey4thSettingsActivity extends Settings { /* empty */ }
    public static class ShortcutkeySettingsActivity extends Settings { /* empty */ }
    public static class UsageAccessSettingsActivity extends Settings { /* empty */ }
    /* LGE_CHANGE_S, Power save */
    public static class PowerSaveSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcut_powersave);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$BatterySettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    /* LGE_CHANGE_E, Power save */

    public static class BatterySaverSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
            } else {
                getActionBar().setIcon(R.drawable.shortcut_powersave);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$BatterySettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class WifiDisplaySettingsActivity extends Settings { /* empty */ }
    public static class UsbSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                boolean usbLauncher = getIntent().getBooleanExtra(UsbSettingsControl.EXTRA_USB_LAUNCHER, true);
                if (Utils.isUI_4_1_model(getApplicationContext())) {
                	if ("VZW".equals(Config.getOperator())
                			|| "SPR".equals(Config.getOperator())
                			|| Utils.supportSplitView(getApplicationContext())) {
                        if (usbLauncher) {
                		    Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClassName("com.android.settings", "com.android.settings.Settings$StorageSettingsActivity");
                            startActivity(intent);
                            finish();
                        } else {
                        	finish();
                        }
                	} else {
                		finish();
                	}
                }
                else if (usbLauncher) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$ConnectivitySettingsActivity");
                    startActivity(intent);
                }
                else {
                    finish();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
    public static class UsbTetheringActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            }
        }
    }
    public static class DeviceInfoLgeSoftwareInformationActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeDSimActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeTSimActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeBatteryActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeLegalActivity extends Settings { /* empty */ }
    public static class DeviceInfoLgeSerialActivity extends Settings { /* empty */ }
    public static class SDEncryptionHelp_Extension extends Settings { /* empty */ }
    public static class SDEncryptionSettings_Extension extends Settings { /* empty */ }

    public static class BatterySaverTipsActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$BatterySettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class PowerSaveBatteryDetailActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcut_battery);
            }
        }

        //[Start] 2013_10_14_jongwon007.kim Settings isValidFragment Overrid
        @Override
        protected boolean isValidFragment(String fragmentName) {
            // Almost all fragments are wrapped in this,
            // except for a few that have their own activities.
            return true;
        }
        //[Start] 2013_10_14_jongwon007.kim Settings isValidFragment Overrid

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClassName("com.android.settings",
                        "com.android.settings.Settings$BatterySettingsActivity");
                startActivity(i);
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    } // yonguk.kim 20120514 Shortcut
    public static class AccountsGroupSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcut_account);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }

    }

    public static class UserSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.shortcuts_multi_user);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }

    }

    //kerry
    public static class LgtRoamingSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActionBar().setIcon(R.drawable.ic_settings_roaming_lgt);
        }
    }

    public static class PaymentSettingsActivity extends Settings { /* empty */ } // rebestm - kk migration
    public static class PrintSettingsActivity extends Settings { /* empty */ }
    public static class PrintJobSettingsActivity extends Settings { /* empty */ }

    public static class BatterySettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActionBar().setIcon(R.drawable.shortcut_battery);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    // [S][2013.07.24][resumet.kwon][Accessibility] Accessibility Porting
    public static class AssistiveTouchActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.ic_access_assistive_touch);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class EasyAccessActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.ic_access_easy_access);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    public static class NegativeColorActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (Utils.supportSplitView(this)) {
                //getActionBar().setIcon(R.mipmap.ic_launcher_settings);
                //getActionBar().setTitle(R.string.settings_label);
            } else {
                getActionBar().setIcon(R.drawable.ic_access_negative_colors);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }
    // [E][2013.07.24][resumet.kwon][Accessibility] Accessibility Porting
    public static class AirplaneModeFragment extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ActionBar actionBar = getActionBar();
            if (Utils.isTablet()) {
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            } else {
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }
    }

    public void confirmDialog(String packageName) {
        final String appPackageName = packageName;
        AlertDialog dialog = new AlertDialog.Builder(Settings.this)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle(getText(R.string.sp_dlg_note_NORMAL))
        .setPositiveButton(getText(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog,
                    int which) {
                final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + appPackageName));
                startActivity(intent);

            }
        })
        .setNegativeButton(getText(R.string.dlg_cancel),
                new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog,
                    int which) {

            }
        }).setMessage(getText(R.string.sp_app_enable_confirm_message_NORMAL)).create();
        dialog.show();
    }
    public boolean appIsEnabled(String packageName) {

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return info.enabled;

    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47][ID-MDM-195][ID-MDM-35][ID-MDM-191][ID-MDM-167]
    // [ID-MDM-198][ID-MDM-52][ID-MDM-280][ID-MDM-342][ID-MDM-343][ID-MDM-344][ID-MDM-345]
    // [ID-MDM-346][ID-MDM-347][ID-MDM-413][ID-MDM-424]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content
            .BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveMainSettingsPolicyChangeIntent(intent,
                        Settings.this)) {
                    finish();
                }
            }
        }
    };
    // LGMDM_END
    private void switchToTab() {
        IntentFilter filter = new IntentFilter("android.settings.SETTINGS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = new Intent("android.settings.SETTINGS");
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY
                | PackageManager.GET_RESOLVED_FILTER);

        final int N = resolveInfoList.size();
        ComponentName[] set = new ComponentName[N];
        int bestMatch = 0;
        for (int i = 0; i < N; i++) {
            ResolveInfo r = resolveInfoList.get(i);
            set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
            if (r.match > bestMatch) {
                bestMatch = r.match;
            }
        }
        getPackageManager().replacePreferredActivity(filter, bestMatch, set, new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings"));

        changeSoundPreferActivity(bestMatch);
        changeDisplayPreferActivity(bestMatch);
        changeAirplanePreferActivity(bestMatch);

        android.provider.Settings.System.putInt(getContentResolver(), SettingsConstants.System.SETTING_STYLE, 1); // List Style : 0, Tab Style: 1

        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        intent.setComponent(new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //[S][Changyu0218.lee]Add Tips VS870(L1V ODR issue)
        intent.putExtra("Change_tab", true);
        //[E][Changyu0218.lee]Add Tips VS870(L1V ODR issue)
        startActivity(intent);
        if (mMyOwnResources.getBoolean(R.bool.config_settings_search_enable)) {
            updateSearchDB();
        }
    }

    private void changeDisplayPreferActivity(int match) {
        IntentFilter displayFilter1 = new IntentFilter("android.settings.DISPLAY_SETTINGS");
        IntentFilter displayFilter2 = new IntentFilter("com.android.settings.DISPLAY_SETTINGS");

        displayFilter1.addCategory(Intent.CATEGORY_DEFAULT);
        displayFilter2.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] display_set = new ComponentName[2];
        display_set[0] = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        display_set[1] = new ComponentName("com.android.settings", "com.android.settings.Settings$DisplaySettingsActivity");

        ComponentName list_view_display = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        getPackageManager().replacePreferredActivity(displayFilter1, match, display_set, list_view_display);
        getPackageManager().replacePreferredActivity(displayFilter2, match, display_set, list_view_display);
    }

    private void changeSoundPreferActivity(int match) {
        IntentFilter soundFilter1 = new IntentFilter("android.settings.SOUND_SETTINGS");
        IntentFilter soundFilter2 = new IntentFilter("com.android.settings.SOUND_SETTINGS");

        soundFilter1.addCategory(Intent.CATEGORY_DEFAULT);
        soundFilter2.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] sound_set = new ComponentName[2];
        sound_set[0] = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        sound_set[1] = new ComponentName("com.android.settings", "com.android.settings.Settings$SoundSettingsActivity");

        ComponentName list_view_sound = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        getPackageManager().replacePreferredActivity(soundFilter1, match, sound_set, list_view_sound);
        getPackageManager().replacePreferredActivity(soundFilter2, match, sound_set, list_view_sound);
    }

    private void changeAirplanePreferActivity(int match) {
        IntentFilter airplaneFilter1 = new IntentFilter("android.settings.AIRPLANE_MODE_SETTINGS");

        airplaneFilter1.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] airplane_set = new ComponentName[2];
        airplane_set[0] = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        airplane_set[1] = new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");

        ComponentName list_view_sound = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
        getPackageManager().replacePreferredActivity(airplaneFilter1, match, airplane_set, list_view_sound);
    }

    public static class DualWindowSettingsActivity extends Settings { /* empty */ }
    public static class MultiTaskingSettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActionBar().setIcon(R.drawable.shortcut_multitasking);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                int settingStyle = android.provider.Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.SETTING_STYLE, 0);
                if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningTaskInfo> info = am.getRunningTasks(1);
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        //String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        String tabIndex = "general";
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public void showSettingsTipEasy() {

        int checkSettingTipEnable = android.provider.Settings.System.getInt(getContentResolver(), "help_settings_settings_tips", 1);

        int checkSettingCount = android.provider.Settings.System.getInt(getContentResolver(), "settings_tip_count", 0);

        if (checkActivity()) {
            if ((checkSettingTipEnable == 1) && (checkSettingCount >= 5)) {
                checkSettingCount = 4;
            }

            if (checkSettingCount <= 5) {
                checkSettingCount++;
                android.provider.Settings.System.putInt(getContentResolver(), "settings_tip_count", checkSettingCount);
                Log.d (LOG_TAG,"Tips count(Settings) : " + checkSettingCount);
            }
        }

        if (checkActivity() && (checkSettingTipEnable == 1) && (checkSettingCount == 5) ) {
        	if (Utils.isUI_4_1_model(this)) {
        		mIsDialogCheck = true;
                showInitialGuidePopup();
        	} else {
        		Intent intent_easy_tip = new Intent(Settings.this, SettingsTipActivity.class);
                startActivity(intent_easy_tip);
        	}
        }
    }
    
    private void showInitialGuidePopup() {
        if (mInitGuideDialog == null) {
            createInitialGuidePopup();
        }
         
        setUpGuideDialogContent();
        mInitGuideDialog.show();
    }
    
    private void createInitialGuidePopup() {
        int dialog_theme_res = getResources().getIdentifier("Theme.LGE.White.Dialog.MinWidth", "style", "com.lge");
         
        mInitGuideDialog = new Dialog(this, dialog_theme_res);
        mInitGuideDialog.setCanceledOnTouchOutside(false);
         
        Window dialogWindow = mInitGuideDialog.getWindow();
        dialogWindow.requestFeature(Window.FEATURE_NO_TITLE);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    
    private void setUpGuideDialogContent() {
        int init_guide = getResources().getIdentifier("dialog_init_guide", "layout", "com.lge");
        mInitGuideDialog.setContentView(init_guide);
     
        TextView title = (TextView)mInitGuideDialog.findViewById(android.R.id.title);
        ImageView image = (ImageView)mInitGuideDialog.findViewById(android.R.id.icon);
        TextView text = (TextView)mInitGuideDialog.findViewById(android.R.id.message);
        mInitChecked = (CheckBox)mInitGuideDialog.findViewById(android.R.id.checkbox);
        Button button = (Button)mInitGuideDialog.findViewById(android.R.id.button1); 
        
        title.setText(R.string.sp_tip_NORMAL);
        image.setImageResource(R.drawable.help_settings_tip_soft_key);
        
        String msg = getString(R.string.settings_tip_message_layout) + "\n\n"
                + getString(R.string.settings_tip_message);
        text.setText(msg);
         
        mInitChecked.setText(R.string.develop_do_not_show);
        mInitChecked.setChecked(mIsDialogCheck);
        mInitChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            }
        });
         
        button.setText(R.string.dlg_ok);
        button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mInitChecked.isChecked()) {
					android.provider.Settings.System.putInt(getContentResolver(), 
            				"help_settings_settings_tips", 0);
            	} else {
            		android.provider.Settings.System.putInt(getContentResolver(), 
            				"help_settings_settings_tips", 1);
            	}
            	mInitGuideDialog.dismiss();
			}
		});
    }
    
    private final boolean isAirplaneModeOn(Context c) {
        return android.provider.Settings.Global.getInt(c.getContentResolver(),
                android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    private final boolean isInCall() {
        TelephonyManager mPhone = TelephonyManager.getDefault();
        Log.i(LOG_TAG, "Call State = " + mPhone.getCallState());
        if (mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            return true;
        } else {
            return false;
        }
    }

    public void onShowCustomToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_popup));

        layout.setBackgroundResource(R.drawable.img_spint_wfc_toast_frame_initial_holo); //toast_frame_holo);


        TextView text = (TextView) layout.findViewById(R.id.textView);
        text.setText(R.string.settings_wfc_toast_popup);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int y = (int)(height*0.2);
        int mToastheight = 0;


        Configuration conf = this.getResources().getConfiguration();

        if ((conf.orientation == Configuration.ORIENTATION_PORTRAIT)
                || (conf.orientation == Configuration.ORIENTATION_UNDEFINED)) {
            mToastheight = (int)(height*0.37);
            Log.d ("kjo", "ORIENTATION_PORTRAIT = " + mToastheight);
        } else {
            mToastheight = (int)(height*0.45);
            Log.d ("kjo", "ORIENTATION_LANDSCAPE = " + mToastheight);
        }


        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.END, 0, (int)(mToastheight));
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        //        new CountDownTimer(4000, 1000) {
        //            public void onTick(long millisUntilFinished) {toast.show();}
        //            public void onFinish() {toast.show();}
        //          }.start();
    }


    public static class NotificationAccessSettingsActivity extends Settings { /* Empty */ }
    public static class AppCleanupActivity extends Settings { /* empty */ }
    public static class DualWindowHelpActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }    

    public static class MenuButtonSettingsActivity extends Settings {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }   

    //T-PhoneMode Roamming Pop-up
    public void RoammingDialog() {

        AlertDialog dialog = new AlertDialog.Builder(Settings.this)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setMessage(getText(R.string.sp_phone_mode_roam_not))
        .setTitle(getText(R.string.sp_phone_mode_title))
        .setPositiveButton(getText(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {

            }
        }).create();
        dialog.show();
    }

    //T-Phone Mode Feature
    public boolean IsTPhoneMode() {
        if (appIsEnabled("com.skt.prod.phone") && appIsEnabled("com.skt.prod.dialer")) {
            if ("false".equals(ModelFeatureUtils.getFeature(this,
                    "tphonemodesupport"))) {
                return false;
            }
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                return false;
            }

            if (sIsSubUserOnly) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean tModeAppIsEnabled() {

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo("com.skt.tmode", PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return info.enabled;

    }

    public static class RemoteFragmentActivity extends Settings { /* empty */ }
    public static class ISAISettingsActivity extends Settings {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getActionBar().setTitle(R.string.sp_gesture_isai);

        }
    }
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        SettingsMenu tempMenu = new SettingsMenu(this, menu);
        return super.onCreatePanelMenu(featureId, tempMenu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        SettingsMenu tempMenu = new SettingsMenu(this, menu);
        return super.onPreparePanel(featureId, view, tempMenu);
    }

	@Override
	public void showBreadCrumbs(Header header) {
		if (header != null) {
			if (header.id == R.id.data_usage_settings) {
				if (Utils.isWifiOnly(this)) {
					mBreadCrumbTitleDisplayedFlag = false;
				}
			}
		}
		super.showBreadCrumbs(header);
	}
	
	private void setupRemoteFragmentInfo(Intent intent) {
		if (getRemoteFragmentInfoMap() != null) {
			return;
		}
		
		addRemoteFragmentInfo("com.android.settings.TTS_SETTINGS", new RemoteFragmentInfo("com.android.settings.tts.TextToSpeechSettings", null,
				R.string.tts_settings_title, null, null, 0));
		
		addRemoteFragmentInfo("com.lge.settings.EMOTIONAL_LED", new RemoteFragmentInfo("com.android.settings.lge.EmotionalLEDEffectTab", null,
				R.string.notification_led, null, null, 0));

		Bundle args = new Bundle();
		if (intent != null) {
			args.putString("key_action_name", intent.getAction());
		}
		addRemoteFragmentInfo("com.lge.springcleaning.idle_apps", new RemoteFragmentInfo("com.lge.springcleaning.ui.SpringCleaningSubFragment",
        args, R.string.unused_apps, null, null, 0));
		addRemoteFragmentInfo("com.lge.springcleaning.temp_files", new RemoteFragmentInfo("com.lge.springcleaning.ui.SpringCleaningSubFragment",
        args, R.string.temporary_files, null, null, 0));
		addRemoteFragmentInfo("com.lge.springcleaning.download_folder", new RemoteFragmentInfo("com.lge.springcleaning.ui.SpringCleaningSubFragment",
        args, R.string.download_folder, null, null, 0));
		addRemoteFragmentInfo("com.lge.springcleaning.settings", new RemoteFragmentInfo("com.lge.springcleaning.ui.settings.SpringCleaningSettingsFragment",
        args, R.string.settings_label, null, null, 0));
    }

    private void showFlipCloseDialog(Context c) {
        LayoutInflater inflater = LayoutInflater.from(c);
        String[] items = { getString(R.string.sp_stop_app),
                getString(R.string.sp_keep_app) };

        // Parent Layout
        LinearLayout ll = (LinearLayout)inflater.inflate(
                com.lge.R.layout.dialog_c_frame, null);
        ll.setPadding(0, 0, 0, 0);

        // ListView
        ListView c_6 = createListView_C6(c, items);

        ll.addView(c_6);

        // Show dialog
        mFlipSettingDlg = new AlertDialog.Builder(this)
                .setTitle(R.string.sp_flip_settings_title).setView(ll)
                .setNegativeButton(R.string.cancel, null).show();
    }

    private ListView createListView_C6(Context c, String[] items) {
        Context dialogContext = getDialogThemedContext(c);
        final ListView listView = createDialogListView(dialogContext);
        int mItemSelect = android.provider.Settings.System.getInt(
                this.getContentResolver(), "wine_flip_setting", 0);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(new ArrayAdapter<String>(dialogContext,
                com.lge.R.layout.dialog_c_6, android.R.id.text1, items));

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // TODO Auto-generated method stub
                Log.d("jw", "position : " + position);
                setFlipSettingDB(position);
                listView.setItemChecked(position, true);
                listView.setSelection(position);
                mFlipSettingDlg.dismiss();
            }

        });

        listView.setItemChecked(mItemSelect, true);
        listView.setSelection(mItemSelect);

        return listView;
    }

    private void setFlipSettingDB(int position) {
        if (position == 0) {
            Log.d("jw", "position : " + position);
            android.provider.Settings.System.putInt(this.getContentResolver(),
                    "wine_flip_setting", 0);

        } else {
            Log.d("jw", "position : " + position);
            android.provider.Settings.System.putInt(this.getContentResolver(),
                    "wine_flip_setting", 1);
        }
    }

    private ListView createDialogListView(Context c) {
        ListView listView = new ListView(c);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        listView.setLayoutParams(lp);

        return listView;
    }

    private Context getDialogThemedContext(Context c) {
        TypedValue outValue = new TypedValue();

        c.getTheme().resolveAttribute(android.R.attr.alertDialogTheme,
                outValue, true);

        return new ContextThemeWrapper(c, outValue.resourceId);
    }
    
    private void switchToSearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }
    
    private void updateSearchDB() {
        ContentResolver cr = this.getContentResolver();
        ContentValues row = new ContentValues();
        row.put("visible", 1);
        String where = "data_key_reference = ? AND class_name = ?";
        cr.update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                new String[] { "easy_display_more_settings",
                        DisplaySettings.class.getName() });
    }

    private void updateSecuritySearchDB() {
        ContentValues row = new ContentValues();
        String where = "data_key_reference = ? AND class_name = ?";
        boolean isEncrypted = SystemProperties.get("ro.crypto.state", "0").equals("encrypted") ? true : false;
        int encryptionVisibleItem = 1;
        if (isEncrypted) {
            encryptionVisibleItem = 0;
        }

        row.clear();
        row.put("visible", encryptionVisibleItem);
        row.put("intent_action", "android.app.action.START_ENCRYPTION");
        row.put("intent_target_class", "com.android.settings.Settings$CryptKeeperSettingsActivity");
        row.put("intent_target_package", "com.android.settings");
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                new String[] { "security_encryption",
                        SecuritySettings.class.getName() });
        row.clear();

        int decryptionVisibleItem = 1;
        if (!isEncrypted) {
             decryptionVisibleItem = 0;
        }
        row.put("visible", decryptionVisibleItem);
        row.put("intent_action", "com.lge.settings.START_UNENCRYPTION");
        row.put("intent_target_class", "com.android.settings.Settings$UnCryptKeeperSettingsActivity");
        row.put("intent_target_package", "com.android.settings");
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                new String[] { "encryption",
                        SecuritySettings.class.getName() });
        row.clear();
        final int getNotificationAccessCount =
                NotificationAccessSettings.getListenersCount(getApplicationContext().getPackageManager());
        if (getNotificationAccessCount > 0) {
            row.put("visible", 1);
        } else {
            row.put("visible", 0);
        }
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                    new String[] { "manage_notification_access",
                        SecuritySettings.class.getName() });
        row.clear();
        if (Utils.isSetLockSecured(getApplicationContext())) {
            row.put("intent_action", "com.android.settings.TRUST_AGENT");
            row.put("intent_target_package", "com.android.settings");
            row.put("intent_target_class", "com.android.settings.Settings$TrustAgentSettingsActivity");
        } else {
            row.put("intent_action", "android.settings.SECURITY_SETTINGS");
            row.putNull("intent_target_package");
            row.putNull("intent_target_class");
        }
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                    new String[] { "manage_trust_agents",
                    SecuritySettings.class.getName() });
    }
}
