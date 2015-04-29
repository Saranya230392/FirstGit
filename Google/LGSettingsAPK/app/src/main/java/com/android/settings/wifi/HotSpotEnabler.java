/* Copyright (C) 2010 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. */

package com.android.settings.wifi;

import com.android.settings.R;
import java.util.ArrayList;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
//import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.lgesetting.Config.Config;
//import android.telephony.TelephonyManager;
//import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
//import com.lge.settings.HotSpotPreference;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.ITelephony;
import android.os.RemoteException;
import android.os.ServiceManager;
//import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.AsyncTask;
import android.content.DialogInterface.OnCancelListener;
import android.net.NetworkInfo;
//import android.app.Activity;
import android.app.ProgressDialog;

//import java.net.URL;
//import android.net.Uri;
import android.widget.TextView;
//import android.widget.Toast;

public class HotSpotEnabler implements CompoundButton.OnCheckedChangeListener {
    private final Context mContext;
    private Switch mSwitch;
    private final CheckBoxPreference mCheckBox;
    // private final CharSequence mOriginalSummary;
    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
    ConnectivityManager mCm;
    private final String[] mWifiRegexs;
    private static final String TAG = "HotSpotEnabler";
    // private String[] mProvisionApp;
    private WifiConfiguration mWifiConfig;
    private boolean mStateMachineEvent;
    private final ITelephony phone;
    private boolean mIsProvisioned = false;
    private final boolean PROVISION = true;
    public static final int DIALOG_WIFI_ON = 1;
    public static final int DIALOG_MHP_BATTERY = 2;
    public static final int DIALOG_ROAMING = 3;
    public static final int DIALOG_3G_NOT_PROVISIONED = 4;
    public static final String BATTERY_USE_REMINDER = "mhp_bat_use_reminder";
    public static final String MHP_USE_REMINDER = "mhp_use_reminder";
    private boolean mIsWantAutoTurnOnMHP;

    public static final int STATE_MHP_ON = 1;
    public static final int STATE_MHP_OFF = 0;

    public AsyncProvisionAfterCheckMobileNetwork mAsyncProvisionAfterCheckMobileNetwork = null;

    private static final String TARGET_NAME = SystemProperties.get("ro.product.name");

    // LGE_CHANGE_S, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
    public static final boolean NO_SIM_MODEL =
            (SystemProperties.getInt("ro.telephony.default_network", -1) == 4 ? true : false);
    public static final String MHP_3G_PROVISION_REMINDER = "mhp_3g_provision_reminder";
    public static final String MHP_3G_PROVISION_NOTI_DONE = "mhp_3g_provision_noti_done";
    public final static String ON_OFF_SWITCH_RESTORE = "com.lge.mobilehotspot.action.SWITCH_RESTORE";
    // LGE_CHANGE_E, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2011-04-12,
             * <MobileHotSpot Provisioning> */
            /* Log.w(TAG, "[Hotspot] Enabler Received Intent >> " + action); if
             * (PROVISION && intent.getAction().equals(
             * "com.lge.hotspotprovision.STATE_CHANGED")) {
             * mContext.removeStickyBroadcast(intent); Log.d(TAG,
             * "[Hotspot]hotspotprovision.STATE_CHANGED : " +
             * intent.getIntExtra("result", 0)); if
             * (intent.getIntExtra("result", 0) == 1) { Log.d(TAG,
             * "[Hotspot] 4G MOBILE HOTSPOT PROVISION OK."); mIsProvisioned =
             * true; setSwitchChecked(true); mSwitch.setEnabled(true);
             * mWifiManager.setVZWMobileHotspot(true); } else { Log.d(TAG,
             * "[Hotspot] 4G MOBILE HOTSPOT PROVISION FAIL."); mIsProvisioned =
             * false; mSwitch.setChecked(false); mSwitch.setEnabled(true);
             * Intent failIntent = new Intent();
             * failIntent.setAction("android.intent.action.MAIN"); //
             * failIntent.setClassName(mContext,
             * CLASSNAME_MHP_PROVISION_FAILED_DIALOG);
             * ((Activity)mContext).startActivity(failIntent); } } /*
             * LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2011-04-12,
             * <MobileHotSpot Provisioning> */
            if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                int data_state = 0;
                Log.w(TAG, "[Hotspot] Got DATA state changed");
                try {
                    data_state = phone.getDataState();
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException");
                }
                Log.w(TAG, "[Hotspot] phone.getDataState" + data_state);
            } else if (intent.getAction().equals(
                    TelephonyIntents.ACTION_DATA_CONNECTION_FAILED)) {
                // do somthing;
            } else if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
                updateTetherState(available.toArray(), active.toArray(), errored.toArray());
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            } else if (action.equals(ON_OFF_SWITCH_RESTORE)) {
                restoreVZWMobileHotspotSetting();
            }

        }
    };

    // [jaewoong87.lee@lge.com] 2013.05.23 Fixed for jenkins error[S]
    public HotSpotEnabler(Context context, CheckBoxPreference checkBox) {
        mContext = context;
        mCheckBox = checkBox;
        //mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);
        phone = null;
        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

    }

    // [jaewoong87.lee@lge.com] 2013.05.23 Fixed for jenkins error[E]

    // public HotSpotEnabler(Context context, CheckBoxPreference checkBox) {
    public HotSpotEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mCheckBox = null;
        //mOriginalSummary = null;

        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ON_OFF_SWITCH_RESTORE);
        phone = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));

        mContext.registerReceiver(mReceiver, mIntentFilter);
        // witch.setEnabled(true);

    }

    /* public void resume() { Log.d(TAG, "[Hotspot] resume");
     * mContext.registerReceiver(mReceiver, mIntentFilter); //
     * enableWifiCheckBox(); enableWifiSwitch();
     * mSwitch.setOnCheckedChangeListener(this); } public void pause() {
     * Log.d(TAG, "[Hotspot] pause"); mContext.unregisterReceiver(mReceiver);
     * mSwitch.setOnCheckedChangeListener(null); } */

    public void onDestroy() {
        Log.w(TAG, "[Hotspot] Mobile Hotspot Enabler onDestroy");
        mContext.unregisterReceiver(mReceiver);
    }

    public void settingVZWMobileHotspot(int tethervalue) {
        mIsProvisioned = true;
        setSwitchChecked(true);
        mSwitch.setEnabled(true);
        gotoNextStep(true, tethervalue);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
            return;
        }
        // LGMDM_END
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
    }
    public void pause() {
        if (null != mAsyncProvisionAfterCheckMobileNetwork
                && true == mAsyncProvisionAfterCheckMobileNetwork.getAsyncProvisionState()) {
            mAsyncProvisionAfterCheckMobileNetwork.stopAsyncProvision();
        }
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void restoreVZWMobileHotspotSetting() {

        mIsProvisioned = false;
        mSwitch.setChecked(false);
        mSwitch.setEnabled(true);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
            return;
        }
        // LGMDM_END
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
        //String s = mContext.getString(
        //        com.android.internal.R.string.wifi_tether_configure_ssid_default);
        /* mCheckBox.setSummary(String.format(
         * mContext.getString(R.string.wifi_tether_enabled_subtext), (wifiConfig
         * == null) ? s : wifiConfig.SSID)); */
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) {
                    wifiTethered = true;
                }
            }
        }
        for (Object o : errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) {
                    wifiErrored = true;
                }
            }
        }

        if (wifiTethered) {
            WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
            updateConfigSummary(wifiConfig);
        } else if (wifiErrored) {
            // mCheckBox.setSummary(R.string.wifi_error);
        }
    }

    private void handleWifiApStateChanged(int state) {
        Log.d(TAG, "[Hotspot] isAirPlaneModeOn" + state);
        switch (state) {
        case WifiManager.WIFI_AP_STATE_ENABLING:
            mSwitch.setEnabled(false);
            break;
        case WifiManager.WIFI_AP_STATE_ENABLED:
            setSwitchChecked(true);
            mSwitch.setEnabled(true);
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
                return;
            }
            // LGMDM_END
            break;
        case WifiManager.WIFI_AP_STATE_DISABLING:
            mSwitch.setEnabled(false);
            break;
        case WifiManager.WIFI_AP_STATE_DISABLED:
            setSwitchChecked(false);
            enableWifiSwitch();
            break;
        default:
            // mCheckBox.setChecked(false);
            // mCheckBox.setSummary(R.string.wifi_error);
            // enableWifiCheckBox();
            setSwitchChecked(false);
            enableWifiSwitch();
            break;
        }
    }

    // JB+ : Wi-Fi hotspot change to switch [andrew75.kim@lge.com] [S]
    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    private void enableWifiSwitch()
    {
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

        if (!isAirplaneMode)
        {
            mSwitch.setEnabled(true);
        }
        else
        {
            mSwitch.setEnabled(false);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
        }
        // LGMDM_END

    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });

        final int wifiApState = mWifiManager.getWifiApState();
        boolean isEnabled;
        isEnabled = wifiApState == WifiManager.WIFI_AP_STATE_ENABLED;
        boolean isDisabled = wifiApState == WifiManager.WIFI_AP_STATE_DISABLED;
        // mSwitch.setChecked(isEnabled);
        setSwitchChecked(isEnabled);
        mSwitch.setEnabled(true);
        mSwitch.setEnabled(isEnabled || isDisabled);

        // [START][TD#20571] Swith should be disabled when AirplaneMode.
        // 2013-04-30, ilyong.oh@lge.com
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        if (isAirplaneMode)
        {
            mSwitch.setEnabled(false);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
            return;
        }
        // LGMDM_END
        // [E N D][TD#20571] Swith should be disabled when AirplaneMode.
        // 2013-04-30, ilyong.oh@lge.com

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Do nothing if called as a result of a state machine event

        if (mStateMachineEvent) {
            return;
        }
        Log.d(TAG, "[Hotspot] onCheckedChanged " + isChecked);

        // mProvisionApp =
        // mContext.getResources().getStringArray(com.android.internal.R.array.config_mobile_hotspot_provision_app);

        boolean newValue = isChecked;

        if (newValue) {
            if (isAirPlaneModeOn()) {
                Toast.makeText(mContext, R.string.airplanemodenoti,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //ConnectivityManager cm = (ConnectivityManager)mContext
            //        .getSystemService(Context.CONNECTIVITY_SERVICE);
            // String[] mUsbRegexs = cm.getTetherableUsbRegexs();
            // String[] tethered = cm.getTetheredIfaces();

            if (mWifiManager.isWifiEnabled())
            {
                showDialog(DIALOG_WIFI_ON);
            } else {

                if (PROVISION && ((Boolean)newValue == true)) {
                    setSwitchChecked(false);
                    checkProvision();
                } else {
                    gotoNextStep((Boolean)newValue, 0);
                }
            }

        }
        else {
            gotoNextStep(newValue, 0);
        }

    }

    private void gotoNextStep(Boolean value, int tethervalue) {

        Log.d(TAG, " Mobile Hotspot MHPEnabler gotoNextStep tethervalue" + tethervalue);

        if ((isCheckedBatUseReminder() == 0) && value
                && (!(tethervalue == 1) && !(tethervalue == 2))) {
            showDialog(DIALOG_MHP_BATTERY);
        }
        else if (isNetworkRoaming() && value) {
            showDialog(DIALOG_ROAMING);
        }
        else {
            if (mWifiManager.isWifiEnabled()
                    || (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING))
            {
                Log.e(TAG,
                        " Still WiFi is On? go out MHP");
                value = false;
                return;
            }

            // mWifiManager.setVZWMobileHotspot(( Boolean )value);
            setMobileHotspotEnabled(value);
            if (value) {
                // Intent intent = new Intent();
                // intent.setClassName("com.lge.mobilehotspot", "com.lge.mobilehotspot.ui.MHPHelp.class");
                // intent.putExtra("HELP_MODE", STATE_MHP_ON);
                // mContext.startActivity(intent);
            }
        }
    }

    private void showDialog(int id) {
        Log.d(TAG, "[Hotspot] showDialog " + id);
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(mContext);
        switch (id) {

        case DIALOG_WIFI_ON: {
            mSwitch.setEnabled(false);
            // AlertDialog.Builder alertDlgBuilder = new
            // AlertDialog.Builder(mContext);
            // alertDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            //remove by UX alertDlgBuilder
            //        .setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(mContext.getResources().getString(
                    R.string.sp_mobile_hotspot_NORMAL));
            alertDlgBuilder
                    .setMessage(R.string.sp_wifi_mobilehotspot_detail_NORMAL);
            alertDlgBuilder.setOnCancelListener(cancelEvent);
            alertDlgBuilder.setPositiveButton(R.string.yes,
                    doTurnOffWifi);
            alertDlgBuilder.setNegativeButton(R.string.no,
                    doNotTurnOffWifi);
            alertDlgBuilder.create();
            alertDlgBuilder.show();
            break;
        }

        case DIALOG_MHP_BATTERY: {
            LayoutInflater factory = LayoutInflater.from(mContext);

            final View dialogView = factory.inflate(
                    R.layout.mobilehotspot_onoff, null);
            final TextView textView = (TextView)dialogView
                    .findViewById(R.id.ApOnOffNotiMessageTextView);
            final CheckBox reminderCheck = (CheckBox)dialogView
                    .findViewById(R.id.DoNotReminderCheckBox);

            reminderCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reminderCheck.isChecked()) {
                        confirmBatUseReminder(1);
                    } else {
                        confirmBatUseReminder(0);
                    }
                }
            });

            if ("fx3q_vzw".equals(TARGET_NAME)
                    || "altev_vzw".equals(TARGET_NAME)) {
                textView.setTypeface(null, Typeface.BOLD);
                reminderCheck.setTypeface(null, Typeface.BOLD);
            }

            // AlertDialog.Builder alertDlgBuilder = new
            // AlertDialog.Builder(mContext);
            alertDlgBuilder.setView(dialogView);
            // alertDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            alertDlgBuilder
                    .setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(mContext.getResources().getString(
                    R.string.sp_notification_NORMAL));
            // Token Update
            alertDlgBuilder.setOnCancelListener(cancelEvent);
            alertDlgBuilder.setPositiveButton(R.string.dlg_ok,
                    doTurnOnMHP);
            alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
                    doNotTurnOn);

            alertDlgBuilder.create();
            alertDlgBuilder.show();
            break;
        }

        /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2012-08-07, International
         * Roaming */
        case DIALOG_ROAMING: {
            Log.d(TAG, "[MHP_GOOKY]DIALOG_ROAMING");
            // alertDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            alertDlgBuilder
                    .setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(R.string.sp_int_roam_title_NORMAL);
            alertDlgBuilder.setMessage(R.string.sp_int_roam_message_NORMAL);
            alertDlgBuilder.setOnCancelListener(cancelEvent);
            alertDlgBuilder.setPositiveButton(R.string.str_continue,
                    mDoTurnOnMHPAtRoaming);
            alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
                    doNotTurnOn);
            alertDlgBuilder.create();
            alertDlgBuilder.show();
            break;
        }
        /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2012-08-07, International*/

        // LGE_CHANGE_S, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
        case DIALOG_3G_NOT_PROVISIONED: {

            LayoutInflater factory = LayoutInflater.from(mContext);

            final View dialogView = factory.inflate(
                    R.layout.mobilehotspot_3g_provision, null);

            final CheckBox reminderCheck = (CheckBox)dialogView
                    .findViewById(R.id.sign_noti_reminder);

            reminderCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reminderCheck.isChecked()) {
                        confirm3gPrivisionUseReminder(1);
                    } else {
                        confirm3gPrivisionUseReminder(0);
                    }
                }
            });

            alertDlgBuilder.setView(dialogView);
            // alertDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            alertDlgBuilder
                    .setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(mContext.getResources().getString(
                    R.string.sp_notification_NORMAL));
            // Token Update
            alertDlgBuilder.setOnCancelListener(cancelEvent);
            alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
                    doNotTurnOn);
            alertDlgBuilder.setPositiveButton(R.string.dlg_ok,
                    doAccept3gProvision);
            //alertDlgBuilder.setNegativeButton(R.string.sp_cancel_NORMAL,
            //        cancelEvent);

            alertDlgBuilder.create();
            alertDlgBuilder.show();
            break;
        }
        // LGE_CHANGE_E, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

        default:
            break;
        }
    }

    // When user want to turn off Wi-Fi and use mobile hotspot..
    DialogInterface.OnClickListener doTurnOffWifi = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, " doTurnOffWifi onClick");

            mWifiManager.setWifiEnabled(false);
            setSwitchChecked(true);
            enableWifiSwitch();

            // LGE_CHANGE_S, 2012-11-22, yeonho.park, When turning off WiFi, put
            // the delay before Provision
            mAsyncProvisionAfterCheckMobileNetwork = new AsyncProvisionAfterCheckMobileNetwork(
                    mContext);
            mAsyncProvisionAfterCheckMobileNetwork.execute();
            // LGE_CHANGE_S, 2012-11-22, yeonho.park, When turning off WiFi, put

            if (isNeedStepbyStep()) {
                mIsWantAutoTurnOnMHP = false;
            } else {
                mIsWantAutoTurnOnMHP = true;
            }

        }
    };

    // When user don't want to turn off Wi-Fi
    DialogInterface.OnClickListener doNotTurnOffWifi = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, " doNotTurnOffWifi onClick");
            mIsWantAutoTurnOnMHP = false;
            restoreVZWMobileHotspotSetting();
            if (mWifiManager.isWifiEnabled()) {
                Toast.makeText(mContext, R.string.turnOffSuccess,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    DialogInterface.OnClickListener doTurnOnMHP = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, " doTurnOnMHP onClick");
            if (mWifiManager.isWifiEnabled())
            {
                showDialog(DIALOG_WIFI_ON);
            } else {
                if (isNetworkRoaming()) {
                    showDialog(DIALOG_ROAMING);
                } else {
                    setMobileHotspotEnabled(true);
                    if (Settings.System.getInt(mContext.getContentResolver(), "mhs_help_donotshow", 1) == 1) {
                        //Show Mobile hotspot help
                        Intent intent = new Intent();
                        intent.setClassName("com.lge.wifisettings", "com.lge.wifisettings.vzwmhp.MHPHelp");
                        intent.putExtra("HELP", STATE_MHP_ON);
                        mContext.startActivity(intent);
                    }
                }
            }
        }
    };

    DialogInterface.OnClickListener mDoTurnOnMHPAtRoaming = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, " doTurnOnMHPAtRoaming onClick");
            if (mWifiManager.isWifiEnabled())
            {
                showDialog(DIALOG_WIFI_ON);
            } else {
                setMobileHotspotEnabled(true);
                {
                    // Intent intent = new Intent();
                    // intent.setClassName("com.lge.mobilehotspot", "com.lge.mobilehotspot.ui.MHPHelp.class");
                    // intent.putExtra("HELP_MODE", STATE_MHP_ON);
                    // mContext.startActivity(intent);
                }
            }
        }
    };

    DialogInterface.OnClickListener doNotTurnOn = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, " doNotTurnOn onClick");
            restoreVZWMobileHotspotSetting();
        }
    };

    DialogInterface.OnCancelListener cancelEvent = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            Log.w(TAG, "[MHP_GOOKY] Dialog canceled");
            restoreVZWMobileHotspotSetting();
        }
    };

    // LGE_CHANGE_S, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
    DialogInterface.OnClickListener doAccept3gProvision = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.w(TAG, "[MHP] doAccept3gProvision onClick ");
            if (mIsProvisioned) {
                gotoNextStep(true, 0);
            } else {
                Intent proIntent = new Intent("com.lge.hotspot.provision_start");
                proIntent.putExtra("QUICKSETTING", 2);
                ((Activity)mContext).startActivity(proIntent);
            }
        }
    };

    private void checkProvision() {
        Log.w(TAG, "[MHP] check3gProvision  getMobileHotspotState() >> "
                + isApEnabled());

        if (isApEnabled()) {
            Log.i(TAG, "checkProvision() : MobileHotspot is enabled()");
            return;
        }

        if (NO_SIM_MODEL) {
            check3gProvision();
        } else {
            check4gProvision();
        }
    }

    private void check3gProvision() {

        Log.d(TAG, "[MHP] check3gProvision isChecked3gProvisionUseReminder >> "
                + isChecked3gProvisionUseReminder());
        if (isChecked3gProvisionUseReminder() == 0) {
            showDialog(DIALOG_3G_NOT_PROVISIONED);
        } else {
            if (!mIsProvisioned) {
                Intent proIntent = new Intent("com.lge.hotspot.provision_start");
                proIntent.putExtra("QUICKSETTING", 2);
                ((Activity)mContext).startActivity(proIntent);
            } else {
                gotoNextStep(true, 0);
            }
        }

    }

    private void check4gProvision() {
        if (isApEnabled()) {
            Log.i(TAG, "checkProvision() : MobileHotspot is enabled()");
            return;
        }
        Log.d(TAG, "[Hotspot] checkProvision " + mIsProvisioned);
        if (!mIsProvisioned) {
            try {
                Intent proIntent = new Intent("com.lge.hotspot.provision_start");
                proIntent.putExtra("QUICKSETTING", 2);
                ((Activity)mContext).startActivity(proIntent);
            } catch (ActivityNotFoundException activityNotFoundException) {
                activityNotFoundException.printStackTrace();
            }
        } else {
            //setMobileHotspotEnabled(true);
            gotoNextStep(true, 0);
        }
    }

    // LGE_CHANGE_E, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min

    private void setMobileHotspotEnabled(boolean value) {
        Log.d(TAG, "[Hotspot] setMobileHotspotEnabled value isVZWMobileHotspotEnabled()"
                + value + isApEnabled());
        if (value == true) {
            if (!(isApEnabled() == false)) {
                Toast.makeText(mContext, R.string.sim_please_wait, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (value == false) {
            if (!isApEnabled()) {
                Toast.makeText(mContext, R.string.sim_please_wait, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mWifiManager.setWifiApEnabled(mWifiConfig, true);

        if (mWifiManager.setWifiApEnabled(null, value)) {
            /* Disable here, enabled on receiving success broadcast */
            mSwitch.setEnabled(false);
        } else {
            //mCheckBox.setSummary(R.string.wifi_error);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWifiApEnablerMenu(mSwitch);
            return;
        }
        // LGMDM_END

    }

    private final boolean isAirPlaneModeOn() {
        Log.d(TAG, "[Hotspot] isAirPlaneModeOn");
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    public void setSoftapEnabled(boolean enable) {
        final ContentResolver cr = mContext.getContentResolver();
        /** Disable Wifi if enabling tethering */
        Log.w(TAG, "[Hotspot] setSoftapEnabled");
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (mWifiManager.setWifiApEnabled(null, enable)) {
            /* Disable here, enabled on receiving success broadcast */
            mCheckBox.setEnabled(false);
        } else {
            mCheckBox.setSummary(R.string.wifi_error);
        }

        /** If needed, restore Wifi on tether disable */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        } else { // sunghee78.lee@lge.com 2012.08.15 Wi-Fi hotspot enable
                 // message display modify.
            if ("KR".equals(Config.getCountry())) {
                Toast.makeText(mContext, R.string.sp_wifi_hotspot_enable_warning_toast_NORMAL,
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    boolean isApEnabled() {
        boolean isApEnabled = false;
        isApEnabled = mWifiManager.isWifiApEnabled();
        
        return isApEnabled;
    }

    private int isCheckedBatUseReminder() {
        return Settings.System.getInt(mContext.getContentResolver(),
                BATTERY_USE_REMINDER, 0);
    }

    private boolean isNetworkRoaming() {
        return "true".equals(SystemProperties.get("gsm.operator.isroaming"));
    }

    private boolean isNeedStepbyStep() {
        Log.w(TAG, " Mobile Hotspot MHPEnabler isNeedStepbyStep");
        return (((isCheckedMHPUseReminder() == 0) || (isCheckedBatUseReminder() == 0)));
    }

    private boolean confirmBatUseReminder(int check) {
        return Settings.System.putInt(mContext.getContentResolver(),
                BATTERY_USE_REMINDER, check);
    }

    public int isCheckedMHPUseReminder() {
        return Settings.System.getInt(mContext.getContentResolver(),
                MHP_USE_REMINDER, 0);
    }

    private boolean checkMobileNetworkState()
    {
        ConnectivityManager cm = (ConnectivityManager)mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileConnected = false;

        if (null == cm) {
            Log.e(TAG, "Fail to get a CONNECTIVITY_SERVICE!!!");
            return false;
        }

        WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        boolean isWiFiConnected = false;

        if (null == wm) {
            Log.e(TAG, "Fail to get a WIFI_SERVICE!!!");
            return false;
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        isMobileConnected = true;
                        Log.d(TAG, "MOBILE NETOWRK CONNECTED");
                    }
                }
            }
        }
        isWiFiConnected = wm.isWifiEnabled();
        Log.e(TAG, " WiFi status " + wm.getWifiState());

        if ((isMobileConnected == true) && (isWiFiConnected == false)) {
            return true;
        } else if (isWiFiConnected == true) {
            return false;
        } else {
            return false;
        }
    }

    class AsyncProvisionAfterCheckMobileNetwork extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog mProgressDialog = null;
        private boolean mDialogRunning = true;

        public AsyncProvisionAfterCheckMobileNetwork(Context ctx) {
            Log.d(TAG, "AsyncProvisionAfterCheckMobileNetwork()");
            mProgressDialog = new ProgressDialog(ctx);
            mProgressDialog
                    .setTitle(ctx.getResources().getString(R.string.sp_wifi_turn_off_NORMAL));
            mProgressDialog
                    .setMessage(ctx.getResources().getString(R.string.sp_wifi_turning_off_NORMAL));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG,
                            "AsyncProvisionAfterCheckMobileNetwork : Dialog Cancelled, so stop enabling");
                    mDialogRunning = false;
                }
            });
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "AsyncProvisionAfterCheckMobileNetwork : onPostExecute()");

            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Exception occured at mProgressDialog.dismiss(). But it is OK");
            }

            if (true == result) {
                Log.d(TAG,
                        "AsyncProvisionAfterCheckMobileNetwork : onPostExecute(), mDialogRunning : true");
                if (mIsWantAutoTurnOnMHP) {
                    if (PROVISION) {
                        checkProvision();
                    } else {
                        gotoNextStep(true, 0);
                    }
                    mIsWantAutoTurnOnMHP = false;
                } else {
                    if (PROVISION) {
                        checkProvision();
                    } else {
                        gotoNextStep(true, 0);
                    }
                }

            } else {
                Log.d(TAG,
                        "AsyncProvisionAfterCheckMobileNetwork : onPostExecute(), mDialogRunning : false");
                //handleStateChanged(MHPProxy.STATE_MHP_OFF);
                restoreVZWMobileHotspotSetting();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "AsyncProvisionAfterCheckMobileNetwork : doInBackground()");
            if (PROVISION) {
                boolean bIsAvailableMobileNetwork = false;

                for (int count = 0; count < 30; count++) {
                    if (false == mDialogRunning) {
                        Log.d(TAG,
                                "AsyncProvisionAfterCheckMobileNetwork : doInBackground() stoped");
                        return false;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread sleep exception in MHPEnabler.java");
                        e.printStackTrace();
                    }

                    Log.d(TAG, " Waiting handover from WiFi to Data...");
                    if (checkMobileNetworkState()) {
                        bIsAvailableMobileNetwork = true;
                        break;
                    }
                }

                if (false == bIsAvailableMobileNetwork) {
                    Log.d(TAG, " Fail to handover from WiFi to Data!");
                }
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "AsyncProvisionAfterCheckMobileNetwork : onPreExecute()");
            mProgressDialog.show();
            super.onPreExecute();
        }

        public void stopAsyncProvision() {
            mDialogRunning = false;
        }

        public boolean getAsyncProvisionState() {
            return mDialogRunning;
        }
    }

    // LGE_CHANGE_S, yeonho.park, 2014-01-03, 3G Hotspot Provisioning by jeongah.min
    public int isChecked3gProvisionUseReminder() {
        return Settings.System.getInt(mContext.getContentResolver(),
                MHP_3G_PROVISION_REMINDER, 0);
    }

    public int isChecked3gProvisionNotiDone() {
        return Settings.System.getInt(mContext.getContentResolver(),
                MHP_3G_PROVISION_NOTI_DONE, 0);
    }

    public boolean confirm3gPrivisionUseReminder(int check) {
        return Settings.System.putInt(mContext.getContentResolver(),
                MHP_3G_PROVISION_REMINDER, check);
    }

    public boolean confirm3gPrivisionNotiDone(int check) {
        return Settings.System.putInt(mContext.getContentResolver(),
                MHP_3G_PROVISION_NOTI_DONE, check);
    }
}
