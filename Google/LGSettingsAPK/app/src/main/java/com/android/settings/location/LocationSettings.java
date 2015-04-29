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

package com.android.settings.location;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.SettingInjectorService;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
//2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
//2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]

import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.Build;
//2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
//2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
/**
 * Location access settings.
 */
public class LocationSettings extends LocationSettingsBase
        implements CompoundButton.OnCheckedChangeListener,
        OnPreferenceChangeListener, //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator.
        CompoundButton.OnClickListener , Indexable { //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound

    private static final String TAG = "LocationSettings";

    //2013-10-07 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'E911 Location' menu for VZW operator. [S]
    //    private static final String KEY_E911 = "e911_only";
    private static final String CAMERA_LOCATION = "camera_location";
    private static final String ANTI_FLICKER = "anti_flicker";
    private CheckBoxPreference mCameraLocation;
    private Preference mAntiflicker;
    private CheckBoxPreference mAgpsCtc;
    private static final String KEY_ASSISTED_GPS_FOR_CTC = "assisted_gps_for_ctc";
    AlertDialog mConfirmDlg = null;
    private boolean mAgpsEnabled;
    private static final int ON = 1;
    private static final int OFF = 0;
    //    private static String mOnOff = null;
    private boolean mCameraLocationMode;
    //2013-10-07 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'E911 Location' menu for VZW operator. [E]
    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
    private static final String KEY_ASSISTED_GPS_FOR_CMCC = "assisted_gps_for_cmcc";
    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]
    /** Key for preference screen "Mode" */
    private static final String KEY_LOCATION_MODE = "location_mode";
    /** Key for preference category "Recent location requests" */
    private static final String KEY_RECENT_LOCATION_REQUESTS = "recent_location_requests";
    /** Key for preference category "Location services" */
    private static final String KEY_LOCATION_SERVICES = "location_services";

    //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [S]
    private static final String KEY_MY_PLACES_CATEGORY = "places_category";
    private static final String KEY_MY_PLACES = "myplaces";
    //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [E]

    private static final String KEY_NEW_MYPLACE_CATEGORY = "new_myplace_category";
    private static final String KEY_NEW_MYPLACE         = "new_myplace";

    private PreferenceCategory      mNewPlaceCategory;
    private SwitchPreference        mNewPlaceSwitch;

    private Switch mSwitch;
    private boolean mValidListener;

    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
    private DoubleTitleListPreference mAssistedGpsForCMCC;
    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]

    // [S][2012.10.16][jin850607.hong] dcm gps noti
    private static final String KEY_GPS_SOUND = "dcm_gps_sound";
    private CheckBoxPreference mGpsSound;
    // [E][2012.10.16][jin850607.hong] dcm gps noti

    private Preference mLocationMode;
    private Preference mBatteryMode;
    private PreferenceCategory mCategoryRecentLocationRequests;
    private PreferenceCategory categoryLocationServices;
    /** Receives UPDATE_INTENT  */
    private BroadcastReceiver mReceiver;
    private SettingsInjector injector;

    //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED [S]
    /** Location services are allowed, but may not be turned on. */
    public final static int GPS_ENABLED = 0;
    /** Location services are not allowed and is off. */
    public final static int GPS_DISABLED = 1;
    /** Location services are active and cannot be turned off by the user. */
    public final static int GPS_FORCED = 2;
    //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED [E]
    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
    private static final int ASSISTED_GPS_ENABLED_FOR_CMCC_VALUE = 0;
    //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]
    Dialog mWarnVzwSwitch;

    // Add switch for tablet. [S]
    SettingsBreadCrumb mBreadCrumb;

    // Add switch for tablet. [E]
    private LocationManager manager;
    LocationProvider mGpsCheck;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    public LocationSettings() {
        mValidListener = false;
    }

    private boolean mGpsPopupShown = false;
    private CheckBox mGpsPopupCheckbox;
    private int mGpsPopupCheckboxChecked;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            android.content.IntentFilter filterLGMDM = new android.content.IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addLocationPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
		mIsFirst = true;

        if (Utils.checkPackage(getActivity(), "com.lge.myplace"))  { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
            mNetworkStatusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e("myplace", "onReceive" + intent.getAction());
                    mNewPlaceSwitch.setEnabled(checkOptionForMyPlace());
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            getActivity().registerReceiver(mNetworkStatusReceiver, intentFilter);
        } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
    }

    private static boolean isAirplaneModeOn(Context context) {
        if (context != null) {
            return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwitch = new Switch(getActivity());
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setOnClickListener(this); //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound
        // [S] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
        // [E] Add switch for tablet.
        mValidListener = true;
        createPreferenceHierarchy();
        updateLocationToggles(); //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED
        if (Utils.checkPackage(getActivity(), "com.lge.myplace")) { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
            mNewPlaceSwitch.setChecked(getMyPlaceEnable());
            mNewPlaceSwitch.setEnabled(checkOptionForMyPlace());
            if (!isWiFiScanAvailable()||!isLocationAvailable()) {
                if (getMyPlaceEnable()) {
                    setMyPlace(false);
                }
            }

        } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
		mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue = getActivity().getIntent().getBooleanExtra("newValue", false);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void goMainScreen() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.android.settings",
                "com.android.settings.Settings$LocationSettingsActivity");
        startActivity(i);
        getActivity().finish();
    }

	private void startResult(boolean newValue) {
	    if (Utils.supportSplitView(getActivity())) {
	        goMainScreen();
	        return;
	    }

        if (mSearch_result.equals(KEY_LOCATION_MODE)) {
            getActivity().finish();
            if (mLocationMode.isEnabled()) {
                mLocationMode.performClick(getPreferenceScreen());
                return;
            }
        }
        goMainScreen();
    }

    @Override
    public void onPause() {
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (RuntimeException e) {
            Log.d(TAG, "RuntimeException");
        }
        // [S] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            Log.d("Location", "onpause");
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
        // [E] Add switch for tablet.
        super.onPause();
        mValidListener = false;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch.setOnClickListener(null); //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound

        if (Utils.checkPackage(getActivity(), "com.lge.myplace")) { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
            if (mNewPlaceSwitch.isChecked() != getMyPlaceEnable()) {
                setMyPlaceEnable(mNewPlaceSwitch.isChecked());
                startActivity(new Intent("com.lge.myplace.MyPlaces_On_Off"));
            }
        } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "LocationSettings : mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
        if (Utils.checkPackage(getActivity(), "com.lge.myplace")) { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
            try {
                getActivity().unregisterReceiver(mNetworkStatusReceiver);
            } catch (RuntimeException e) {
                Log.d("myplace", "RuntimeException mMyPlaceReceiver");
            }
        } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
    }

    private void addPreferencesSorted(List<Preference> prefs, PreferenceGroup container) {
        // If there's some items to display, sort the items and add them to the container.
        Collections.sort(prefs, new Comparator<Preference>() {
            @Override
            public int compare(Preference lhs, Preference rhs) {
                return lhs.getTitle().toString().compareTo(rhs.getTitle().toString());
            }
        });
        for (Preference entry : prefs) {
            container.addPreference(entry);
        }
    }

    private PreferenceScreen createPreferenceHierarchy() {
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
        int assisted_gps_for_cmcc;
        CharSequence[] values;
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]
        manager = (LocationManager)getActivity().getApplicationContext().getSystemService(
                Context.LOCATION_SERVICE);
        mGpsCheck = manager.getProvider("gps");

        final PreferenceActivity activity = (PreferenceActivity)getActivity();
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.location_settings);
        root = getPreferenceScreen();

        //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [S]
        if (!isMyPlaceInstalled()) {
            Preference myPlaceCategory = null;
            Preference myPlacePref = null;

            if (root != null) {
                myPlaceCategory = root.findPreference(KEY_MY_PLACES_CATEGORY);
                myPlacePref = root.findPreference(KEY_MY_PLACES);
            }

            if (myPlaceCategory != null) {
                root.removePreference(myPlaceCategory);
            }

            if (myPlacePref != null) {
                root.removePreference(myPlacePref);
            }
        }
        //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [E]
        root = getPreferenceScreen();

        mAgpsCtc = (CheckBoxPreference)root
                .findPreference(KEY_ASSISTED_GPS_FOR_CTC);
        mAgpsEnabled = Settings.System.getInt(getContentResolver(), "agps_ctc", 0) > 0;
        mAgpsCtc.setChecked(mAgpsEnabled);

        if (!(Config.CTC).equals(Config.getOperator()) && !(Config.CTO).equals(Config.getOperator())) {
            root.removePreference(mAgpsCtc);
        }

        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
        mAssistedGpsForCMCC = (DoubleTitleListPreference)root
                .findPreference(KEY_ASSISTED_GPS_FOR_CMCC);
        mAssistedGpsForCMCC.setMainTitle(getString(R.string.assisted_gps));
        mAssistedGpsForCMCC.setSubTitle(getString(R.string.assisted_gps_popup_summary));
        assisted_gps_for_cmcc = Settings.Global.getInt(getContentResolver(),
                SettingsConstants.Global.ASSISTED_GPS_ENABLED_FOR_CMCC,
                ASSISTED_GPS_ENABLED_FOR_CMCC_VALUE);
        mAssistedGpsForCMCC.setValue(String.valueOf(assisted_gps_for_cmcc));
        mAssistedGpsForCMCC.setOnPreferenceChangeListener(this);

        mAssistedGpsForCMCC.setEntries(R.array.assisted_gps_popup_for_cmcc);
        mAssistedGpsForCMCC.setEntryValues(R.array.assisted_gps_popup_value_for_cmcc);

        values = mAssistedGpsForCMCC.getEntries();
        if (0 <= assisted_gps_for_cmcc) {
            mAssistedGpsForCMCC.setSummary(values[assisted_gps_for_cmcc].toString());
        }

        if (!(Config.CMCC).equals(Config.getOperator()) && !(Config.CMO).equals(Config.getOperator())) {
            root.removePreference(mAssistedGpsForCMCC);
        }
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]

        // [S][2012.10.16][jin850607.hong] dcm gps noti
        mGpsSound = (CheckBoxPreference)root.findPreference(KEY_GPS_SOUND);
        if (!(Config.DCM).equals(Config.getOperator()) || Config.isNotSupportGPSNotificationModel()) {
            root.removePreference(mGpsSound);
        }
        // [E][2012.10.16][jin850607.hong] dcm gps noti

        mLocationMode = root.findPreference(KEY_LOCATION_MODE);
        mLocationMode.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        activity.startPreferencePanel(
                                LocationMode.class.getName(), null,
                                R.string.location_mode_screen_title, null, LocationSettings.this,
                                0);
                        return true;
                    }
                });

        mCategoryRecentLocationRequests =
                (PreferenceCategory)root.findPreference(KEY_RECENT_LOCATION_REQUESTS);
        RecentLocationApps recentApps = new RecentLocationApps(activity);
        List<Preference> recentLocationRequests = recentApps.getAppList();
        if (recentLocationRequests.size() > 0) {
            addPreferencesSorted(recentLocationRequests, mCategoryRecentLocationRequests);
        } else {
            // If there's no item to display, add a "No recent apps" item.
            Preference banner = new Preference(activity);
            banner.setLayoutResource(R.layout.location_list_no_item);
            banner.setTitle(R.string.location_no_recent_apps);
            banner.setSelectable(false);
            mCategoryRecentLocationRequests.addPreference(banner);
        }

        addNewMyPlace(activity, root);

        addLocationServices(activity, root);

        // Only show the master switch when we're not in multi-pane mode, and not being used as
        // Setup Wizard.
        if (activity.onIsHidingHeaders() || !activity.onIsMultiPane()) {
            final int padding = activity.getResources().getDimensionPixelSize(
                    R.dimen.action_bar_switch_padding);
            mSwitch.setPaddingRelative(0, 0, padding, 0);
            // [S]Add switch for tablet.
            if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
            }
        }

        setHasOptionsMenu(true);

        Log.d("starmotor", "mGpsCheck 2 : " + mGpsCheck);
        mBatteryMode = root.findPreference("battery_saving_mode");

        if (Utils.isWifiOnly(getActivity())) {
            mBatteryMode.setSummary(getString(R.string.location_mode_battery_saving_summary));
        }

        if (mGpsCheck == null) {
            root.removePreference(mLocationMode);
        } else {
            root.removePreference(mBatteryMode);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance().setLocationEnableMenuKK(root);
        }
        // LGMDM_END
        refreshLocationMode();
        return root;
    }

    private void addNewMyPlace(Context context, PreferenceScreen root) {
        if (Utils.checkPackage(getActivity(), "com.lge.myplace")) {
            mNewPlaceSwitch = (SwitchPreference)findPreference(KEY_NEW_MYPLACE);
            mNewPlaceSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String key = preference.getKey();
                    if (getActivity() == null) {
                        return false;
                    }

                    if (KEY_NEW_MYPLACE.equals(key)) {
                        Log.d(TAG, "new myplace switch : " + (Boolean)newValue);
                        setMyPlace((Boolean)newValue);
                    }
                    return false;
                }
            });
        } else {
            mNewPlaceSwitch = (SwitchPreference)findPreference(KEY_NEW_MYPLACE);
            if (mNewPlaceSwitch != null) {
                root.removePreference(mNewPlaceSwitch);
            }
            mNewPlaceCategory = (PreferenceCategory)findPreference(KEY_NEW_MYPLACE_CATEGORY);
            if (mNewPlaceCategory != null) {
                root.removePreference(mNewPlaceCategory);
            }
        }
    }

    //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [S]
    private boolean isMyPlaceInstalled() {
        Intent intent = new Intent();
        intent.setClassName("com.lge.settings.locationservice",
                "com.lge.settings.locationservice.MyPlaceListActivity");
        List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
        return (infos != null) && (infos.size() == 1);
    }

    //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [E]

    final int MCC_CHINA = 460; //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
    final int MCC_CUBA = 368;
    final int MCC_NORTHKOREA = 467;
    final int MCC_SUDAN = 634;
    final int MCC_SYRIA = 417;

    BroadcastReceiver mNetworkStatusReceiver;
    boolean prevValue = false;

    private boolean isWiFiScanAvailable () {
        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        return wm.isScanAlwaysAvailable();
    }

    private boolean isLocationAvailable() {
    boolean result = false;
    int locationMode = 0;

    try {
        locationMode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
    } catch (SettingNotFoundException e) {
        e.printStackTrace();
    }
    result = locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
    Log.e("myplace", "isLocationEnabled:" + result);
    return result;
    }

    private boolean checkOptionForMyPlace () {
        // country code & sim state
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
            Log.e("myplace", "sim card is Not ready:" + tm.getSimState());
            return false;
        }

        String networkOperator = tm.getNetworkOperator();
        int mcc = 0;
        if (networkOperator != null && networkOperator.length() > 0) {
            try {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
            } catch (NumberFormatException e) {
            }
        }

        if (!((mcc != MCC_CHINA) && (mcc != MCC_CUBA) && (mcc != MCC_NORTHKOREA) && (mcc != MCC_SUDAN) && (mcc != MCC_SYRIA) && (mcc != 0))) {
            Log.e("myplace", "isCountryAvailable:" + false + " mcc: " + mcc);
            return false;
        }
        // air plane mode
        if (Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1) {
            Log.e("myplace", "isAirplaneModeOn");
            return false;
        }
        // network conn
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

    try {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            Log.e("myplace", "isNetworkAvailable:" + false);
            return false;
        }
    } catch (Exception e) {
        Log.e("myplace", "isNetworkAvailable: error");
        return false;
    }

    return true;
    }

    private void setMyPlace(boolean onoff) {
        Log.e("myplace", "setMyPlace: " + onoff);
        if (onoff) {
            if (isWiFiScanAvailable() && isLocationAvailable()) {
                mNewPlaceSwitch.setChecked(true);
            } else {
            startActivity(new Intent("com.lge.myplace.MyPlaces_Option_Check"));
            }
        } else {
            mNewPlaceSwitch.setChecked(false);
        }
    }

    private boolean getMyPlaceEnable() {
        boolean result = Settings.Global.getInt(getContentResolver(), SettingsConstants.Global.MY_PLACE_ENABLED, 1) == 1? true : false;
        Log.e("myplace", "getMyPlaceEnable: " + result);
        return result;
    }

    private void setMyPlaceEnable(boolean onoff) {
        Log.e("myplace", "setMyPlaceEnable: " + onoff);
        Settings.Global.putInt(getContentResolver(), SettingsConstants.Global.MY_PLACE_ENABLED, (onoff) ? 1 : 0);
    } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]

    /**
     * Add the settings injected by external apps into the "App Settings" category. Hides the
     * category if there are no injected settings.
     *
     * Reloads the settings whenever receives
     * {@link SettingInjectorService#ACTION_INJECTED_SETTING_CHANGED}. As a safety measure,
     * also reloads on {@link LocationManager#MODE_CHANGED_ACTION} to ensure the settings are
     * up-to-date after mode changes even if an affected app doesn't send the setting changed
     * broadcast.
     */
    private void addLocationServices(Context context, PreferenceScreen root) {
        int mMode = 0;
        int mFlicker_summary = R.string.sp_anti_flicker_auto;
        final PreferenceActivity activity = (PreferenceActivity)getActivity();
        categoryLocationServices = (PreferenceCategory)root.findPreference(KEY_LOCATION_SERVICES);
        boolean mCameraLoc = Utils.IsVersionCheck(getActivity(), "com.lge.camera");
        //mOnOff = null;
        mCameraLocationMode = Settings.System.getInt(getContentResolver(), "camera_location", 0) != 0;
        //        Preference mE911_Pref = new Preference(context);

        //if (mCameraLocationMode) {
        //    mOnOff = "On";
        //} else {
        //    mOnOff = "Off";
        //}

        mCameraLocation = new CheckBoxPreference(context);
        mCameraLocation.setKey(CAMERA_LOCATION);
        mCameraLocation.setTitle(R.string.lockscreen_camera_widget_text);

        mAntiflicker = new Preference(context);
        mAntiflicker.setKey(ANTI_FLICKER);
        mAntiflicker.setTitle(R.string.sp_anti_flicker);
        try {
            mMode = Settings.System.getInt(getActivity().getContentResolver(), "anti_flicker_on");
        } catch (SettingNotFoundException snfe) {
            Log.d(TAG, "SettingNotFoundException");
        }
        switch (mMode) {
        case 0:
            mFlicker_summary = R.string.sp_anti_flicker_auto;
            break;
        case 1:
            mFlicker_summary = R.string.sp_anti_flicker_50hz;
            break;
        case 2:
            mFlicker_summary = R.string.sp_anti_flicker_60hz;
            break;
        default:
            break;
        }
        mAntiflicker.setSummary(mFlicker_summary);

        mAntiflicker.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        activity.startPreferencePanel(
                                AntiFlicker.class.getName(), null,
                                R.string.sp_anti_flicker, null, LocationSettings.this,
                                0);
                        return true;
                    }
                });

        Preference mE911_Pref = root.findPreference("e911");
        //        mE911_Pref.setKey(KEY_E911);
        //        mE911_Pref.setTitle(R.string.e911_location_only_title);
        //        mE911_Pref.setSummary(R.string.e911_only_summary);
        injector = new SettingsInjector(context);
        List<Preference> locationServices = injector.getInjectedSettings();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Received settings change intent: " + intent);
                }
                injector.reloadStatusMessages();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingInjectorService.ACTION_INJECTED_SETTING_CHANGED);
        context.registerReceiver(mReceiver, filter);
        //        categoryLocationServices.addPreference(mE911_Pref);
        categoryLocationServices.addPreference(mCameraLocation);
        categoryLocationServices.addPreference(mAntiflicker);

        mCameraLocation.setChecked(mCameraLocationMode);
        mCameraLocation.setSummary(R.string.sp_default_location_camera_summary);
        // LGE_CHANGE_S, [E7/E10][COM] , Flicker property set
        if (!"e7wifi".equals(Build.DEVICE) &&
                !"e9wifi".equals(Build.DEVICE) &&
                !"e9wifin".equals(Build.DEVICE) &&
                !"e8wifi".equals(Build.DEVICE)) {
            categoryLocationServices.removePreference(mAntiflicker);
        }
        if (!mCameraLoc || (mGpsCheck == null)) {
            categoryLocationServices.removePreference(mCameraLocation);
        }

        if (!(Config.VZW).equals(Config.getOperator())) {
            if (!(Config.CRK).equals(Config.getOperator())
                    && !(Config.MPCS).equals(Config.getOperator())
                    && !"TRFBASE_PP".equalsIgnoreCase(SystemProperties.get("ro.afwdata.LGfeatureset", "none"))) {
                //                categoryLocationServices.removePreference(mE911_Pref);
                root.removePreference(mE911_Pref);
            }
        }

        if ((Config.VZW).equals(Config.getOperator())) {
            if (locationServices.size() > 0) {
                addPreferencesSorted(locationServices, categoryLocationServices);
            }
        } else {
            if (locationServices.size() > 0) {
                addPreferencesSorted(locationServices, categoryLocationServices);
            } else {
                // If there's no item to display, remove the whole category.
                if (!"CN".equals(Config.getCountry())) {
                    root.removePreference(categoryLocationServices);
                }
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mCameraLocation) {
            Settings.System.putInt(getContentResolver(), "camera_location",
                    mCameraLocation.isChecked() ? ON : OFF);
            //boolean mCameraLocationMode = Settings.System.getInt(getContentResolver(),
            //        "camera_location", 0) != 0;
            //if (mCameraLocationMode) {
            //    mOnOff = "On";
            //} else {
            //    mOnOff = "Off";
            //}
            ////            mCameraLocation.setSummary(mOnOff);
            return true;
        }

        if (preference == mAgpsCtc) {
            if (mAgpsCtc.isChecked()) {
                mConfirmDlg = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.assisted_gps_ctc)
                        .setMessage(R.string.assisted_gps_popup_summary_ctc)
                        .setCancelable(true)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Settings.System.putInt(getContentResolver(), "agps_ctc", 1);
                                        mAgpsCtc.setChecked(true);
                                    }
                                })
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Settings.System.putInt(getContentResolver(), "agps_ctc", 0);
                                        mAgpsCtc.setChecked(false);
                                        dialog.cancel();
                                    }
                                })
                        .create();
                if (mConfirmDlg != null) {
                    mConfirmDlg.show();
                }
            } else {
                Settings.System.putInt(getContentResolver(), "agps_ctc", 0);
            }
            return true;
        }

        if (preference == mGpsSound) {
            Settings.System.putInt(getContentResolver(), "sound_gps_enabled",
                    mGpsSound.isChecked() ? 1 : 0);

            //ry.jeong : [DCM] sendBroadcast GPS Notification Changed (TD:316224) [S]
            Intent intent = new Intent("com.android.settings.gpsnotification.CHANGED");
            getActivity().sendBroadcast(intent);
            //ry.jeong : [DCM] sendBroadcast GPS Notification Changed (TD:316224) [E]

            return true;
        }
        if ((preference == mNewPlaceSwitch)&&(mNewPlaceSwitch.isChecked())) { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
            startActivity(new Intent("com.lge.myplace.MyPlaces_Setting"));
            return true;
        } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_location_access;
    }

    @Override
    public void onModeChanged(int mode, boolean restricted) {
        switch (mode) {
        case android.provider.Settings.Secure.LOCATION_MODE_OFF:
            mLocationMode.setSummary(R.string.location_mode_location_off_title);
            //            Settings.System.putInt(getContentResolver(), "camera_location", OFF);
            //            mCameraLocation.setChecked(false);
            updateItemEnabled(false);
            break;

        case android.provider.Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
            if ("AU".equals(Config.getCountry())
                    || "NZ".equals(Config.getCountry())) {
                mLocationMode.setSummary(R.string.location_mode_sensors_only_add_title_ex);
            } else {
                mLocationMode.setSummary(R.string.location_mode_sensors_only_add_title);
            }
            updateItemEnabled(true);
            break;

        case android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
            mLocationMode.setSummary(R.string.location_mode_battery_saving_add_title);
            updateItemEnabled(true);
            break;

        case android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
            if ("AU".equals(Config.getCountry())
                    || "NZ".equals(Config.getCountry())) {
                mLocationMode.setSummary(R.string.location_mode_high_accuracy_add_title_ex);
            } else {
                mLocationMode.setSummary(R.string.location_mode_high_accuracy_add_title);
            }
            updateItemEnabled(true);
            break;

        default:
            break;
        }

        // Restricted user can't change the location mode, so disable the master switch. But in some
        // corner cases, the location might still be enabled. In such case the master switch should
        // be disabled but checked.
        boolean enabled = (mode != android.provider.Settings.Secure.LOCATION_MODE_OFF);
        mSwitch.setEnabled(!restricted);
        mLocationMode.setEnabled(enabled && !restricted);
        mCategoryRecentLocationRequests.setEnabled(enabled);
        categoryLocationServices.setEnabled(enabled);

        if (enabled != mSwitch.isChecked()) {
            // set listener to null so that that code below doesn't trigger onCheckedChanged()
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(null);
                mSwitch.setOnClickListener(null); //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound
            }
            mSwitch.setChecked(enabled);
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(this);
                mSwitch.setOnClickListener(this); //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound
            }
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance().setLocationEnableMenuKK(
                    getPreferenceScreen());
        }
        // LGMDM_END
        updateLocationToggles(); //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED

        //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [S]
        if (isMyPlaceInstalled()) {
            Preference myPlacePref = findPreference(KEY_MY_PLACES);
            if (myPlacePref != null) {
                myPlacePref.setEnabled(enabled);
            }
        }
        //2014-2-19 (sy.yoon@lge.com) : Add myplace menu for G.Board. [E]
    }

    /**
     * Listens to the state change of the location master switch.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if ((Config.USC).equals(Config.getOperator())) {
                mGpsPopupCheckboxChecked = Settings.Secure.getIntForUser(getActivity()
                        .getContentResolver(),
                        "usc_gps_enabled", 0, UserHandle.USER_CURRENT);
            }
            if (Config.VZW.equals(Config.getOperator())) {
                LayoutInflater factory = LayoutInflater.from(getActivity());
                View inputView = factory.inflate(R.layout.vzw_switch_dialog_4_0, null);
                mWarnVzwSwitch = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.vzw_location_services_switch_title)
                        .setView(inputView)
                        .setPositiveButton(
                                getActivity().getResources().getString(R.string.sp_agree_NORMAL),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog == mWarnVzwSwitch
                                                && which == DialogInterface.BUTTON_POSITIVE) {
                                            setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                                        }
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(R.string.sp_disagree_NORMAL,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog == mWarnVzwSwitch
                                                && which == DialogInterface.BUTTON_NEGATIVE) {
                                            setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
                                        }
                                    }
                                })
                        .show();
                mWarnVzwSwitch.setCanceledOnTouchOutside(true);
                mWarnVzwSwitch.setCancelable(true);
                mWarnVzwSwitch.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
                    }
                });
            } else if ((Config.USC).equals(Config.getOperator())
                    && "US".equals(Config.getCountry()) && (mGpsPopupCheckboxChecked == 0)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater)getActivity()
                        .getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                View inputView = inflater.inflate(R.layout.usc_gps_dialog, null);
                builder.setTitle(R.string.sp_usc_gps_popup_title_NORMAL);
                //                    builder.setIcon(com.lge.internal.R.drawable.ic_dialog_alert);
                builder.setView(inputView);

                mGpsPopupCheckbox = (CheckBox)inputView.findViewById(R.id.gps_check);
                mGpsPopupCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                                    "usc_gps_enabled", 1, UserHandle.USER_CURRENT);
                        } else {
                            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                                    "usc_gps_enabled", 0, UserHandle.USER_CURRENT);
                        }
                        mGpsPopupCheckbox.playSoundEffect(SoundEffectConstants.CLICK);
                    }
                });

                if (!mGpsPopupShown) {
                    mGpsPopupShown = true;
                    builder.setPositiveButton(R.string.sp_agree_NORMAL,
                            new Dialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                                    mGpsPopupShown = false;
                                }
                            });

                    builder.setNegativeButton(R.string.sp_disagree_NORMAL,
                            new Dialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
                                    dialog.dismiss();
                                    mGpsPopupShown = false;
                                }
                            });

                    builder.setOnCancelListener(
                            new Dialog.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) {
                                    setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
                                    mGpsPopupShown = false;
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                }
            } else if ((Config.DCM).equals(Config.getOperator()) || (Config.KDDI).equals(Config.getOperator())) {
                showGpsWarnDialog();
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            }
        } else {
            setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
    private final android.content.BroadcastReceiver mLGMDMReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(android.content.Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveLocationPolicyChangeIntent(intent)) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        }
    };

    // LGMDM_END

    //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound [S]
    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }

    //2013-11-01 RaeYoung-Jeong (ry.jeong@lge.com) : Add Switch sound [E]

    /**
     * Creates toggles for each available location provider
     */
    private void updateLocationToggles() {
        if (Config.SPR.equals(Config.getOperator()) || Config.BM.equals(Config.getOperator())) {
            //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED [S]
            int mGPSstatus = Settings.System.getInt(getContentResolver(),
                    "gps_device_managerment_enabled", 0);
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            Log.d("SPRINT_DM", "GPSstatus: " + mGPSstatus);
            if (mGPSstatus == GPS_ENABLED) {
                Log.d("SPRINT_DM", " DeviceManagement getGpsState :: GPS_ENABLED ");
                if (Settings.Secure.LOCATION_MODE_OFF == mode) {
                    mLocationMode.setEnabled(false);
                    mSwitch.setEnabled(true);
                } else {
                    mLocationMode.setEnabled(true);
                    mSwitch.setEnabled(true);
                }
            } else if (mGPSstatus == GPS_DISABLED) {
                Log.d("SPRINT_DM", " DeviceManagement getGpsState :: GPS_DISABLED ");
                if (Settings.Secure.LOCATION_MODE_OFF != mode) {
                    setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
                }
                mLocationMode.setEnabled(false);
                mSwitch.setEnabled(false);
            } else if (mGPSstatus == GPS_FORCED) {
                Log.d("SPRINT_DM", " DeviceManagement getGpsState :: GPS_FORCED ");
                if (Settings.Secure.LOCATION_MODE_HIGH_ACCURACY != mode) {
                    setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                }
                mLocationMode.setEnabled(false);
                mSwitch.setEnabled(false);
            }
            //2013-11-07 RaeYoung-Jeong (ry.jeong@lge.com) : GPS_FORCED, GPS_DISABLED, GPS_ENABLED [E]
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
        CharSequence[] values;
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]

        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
        if (pref.getKey().equals(KEY_ASSISTED_GPS_FOR_CMCC)) {
            values = mAssistedGpsForCMCC.getEntries();
            if (0 <= Integer.parseInt(newValue.toString())) {
                mAssistedGpsForCMCC.setValue(newValue.toString());
                mAssistedGpsForCMCC.setSummary(values[Integer.parseInt(newValue.toString())]
                        .toString());
                Settings.Global.putInt(getContentResolver(),
                        SettingsConstants.Global.ASSISTED_GPS_ENABLED_FOR_CMCC,
                        Integer.parseInt(newValue.toString()));
            }
        }
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]
        return true;
    }

    private void updateItemEnabled(boolean isEnabled) {

        if (mCameraLocation != null) {
            mCameraLocation.setEnabled(isEnabled);
        }

        if (mAgpsCtc != null) {
            mAgpsCtc.setEnabled(isEnabled);
        }

        if (mBatteryMode != null) {
            mBatteryMode.setEnabled(isEnabled);
        }

        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [START]
        if (mAssistedGpsForCMCC != null) {
            mAssistedGpsForCMCC.setEnabled(isEnabled);
        }
        //2013-05-13 RaeYoung-Jeong (ry.jeong@lge.com) : Add 'Use assisted GPS' menu for CMCC operator. [END]

        if (mGpsSound != null) {
            mGpsSound.setEnabled(isEnabled);
        }

        if (mNewPlaceSwitch != null) {
            if (isEnabled) { //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [S]
                mNewPlaceSwitch.setEnabled(checkOptionForMyPlace());
            } else {
                mNewPlaceSwitch.setEnabled(isEnabled);
            } //2015-2-8 (jiho.yoo@lge.com) : Add myplace start / stop [E]
        }
    }

	public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            String strSearchCategoryTitle = context.getString(R.string.location_settings_title_kk);
            setSearchIndexData(context, "location_settings",
                    context.getString(R.string.location_settings_title_kk), "main",
                    null, null,
                    "android.settings.LOCATION_SOURCE_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);

            setSearchIndexData(context, KEY_LOCATION_MODE,
                    context.getString(R.string.location_mode_title),
                    strSearchCategoryTitle,
                    null, null,
                    "android.settings.LOCATION_SOURCE_SETTINGS",
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
