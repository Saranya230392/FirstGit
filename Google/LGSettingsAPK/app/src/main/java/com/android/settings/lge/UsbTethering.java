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

package com.android.settings.lge;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.wifi.WifiApEnabler;
import com.android.settings.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings.System;
import android.widget.Toast;
import com.android.settings.lgesetting.Config.Config;

import com.lge.constants.UsbManagerConstants;

public class UsbTethering extends SettingsPreferenceFragment {
    private static final String TAG = "TetherSettings";
    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    //private static final String TETHERING_HELP = "tethering_help";

    private static final int DIALOG_TETHER_HELP = 1;
    //private static final int DIALOG_TETHER_ALERT = 3;
    /* LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/
    private static final int DIALOG_WIFI_ON = 4;
    /* LGE_CHANGE_E, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/

    //private static final int USB_TETHER_UNAVAILABLE = 0;
    //private static final int USB_TETHER_AVAILABLE = 1;
    //private static final int USB_TETHER_DISABLE = 2;
    //private static final int USB_TETHER_ACTIVE = 3;

    private static final boolean DBG = false;

    private CheckBoxPreference mUsbTether;

    private boolean mUsbConnection;
    private boolean mMassStorageActive;
    private boolean mCdromStorage = false;

    private String[] mUsbRegexs;
    /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */
    private boolean mIsProvisioned;
    private final boolean PROVISION = true;
    /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                try {
                    ArrayList<String> available = intent.getStringArrayListExtra(
                            ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                    ArrayList<String> active = intent.getStringArrayListExtra(
                            ConnectivityManager.EXTRA_ACTIVE_TETHER);
                    ArrayList<String> errored = intent.getStringArrayListExtra(
                            ConnectivityManager.EXTRA_ERRORED_TETHER);
                    if (available != null && active != null && errored != null) {
                        updateState(available.toArray(), active.toArray(), errored.toArray());
                    }
                } catch (NullPointerException e) {
                    Log.w(TAG, "information of tether status is null=" + e);
                }
            } else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                boolean usbConfigured = intent.getBooleanExtra(UsbManager.USB_CONFIGURED, false);
                mCdromStorage = intent.getBooleanExtra(
                        UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false);

                if (usbConnected) {
                    if (usbConfigured) {
                        mUsbConnection = true;
                        updateState();
                    }
                } else {
                    mUsbConnection = false;
                    updateState();
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mMassStorageActive = true;
                updateState();
            } else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mMassStorageActive = false;
                updateState();
            }
            // LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check
            // BEGIN: 0018445 byungkook.son@lge.com 2010-03-24
            // ADD 0018445 : Apply hotspot provisioning according to VZW requirement
            else if (PROVISION
                    && intent.getAction().equals("com.lge.hotspotprovision.STATE_CHANGED")) {
                getActivity().removeStickyBroadcast(intent);
                if (DBG) {
                    Log.d("USB_TETHER",
                            "[MHP] hotspotprovision.STATE_CHANGED : "
                                    + intent.getIntExtra("result", 0));
                }
                if (intent.getIntExtra("result", 0) == 1)
                {
                    mUsbTether.setChecked(true);
                    mIsProvisioned = true;
                    processTurnOnOff();
                } else {
                    mUsbTether.setChecked(false);
                    mIsProvisioned = false;
                }
            }
            // END: 0018445 byungkook.son@lge.com 2010-03-24
            //LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.usb_tethering);

        mUsbTether = (CheckBoxPreference)findPreference(USB_TETHER_SETTINGS);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            finish();
            return;
        }

        mMassStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());

        mUsbRegexs = cm.getTetherableUsbRegexs();
        if (mUsbRegexs.length == 0 || Utils.isMonkeyRunning()) {
            PreferenceScreen ps = getPreferenceScreen();
            if (ps != null) {
                ps.removePreference(mUsbTether);
            }
            //getPreferenceScreen().removePreference(mUsbTether);
        }

        //UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        //boolean enable = usbManager.isFunctionEnabled(UsbManagerConstants.USB_FUNCTION_TETHER);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addTetherPolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_TETHER_HELP) {
        }
        /* LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/
        else if (id == DIALOG_WIFI_ON) {
            mUsbTether.setChecked(false);
            AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(getActivity());
            alertDlgBuilder.setCancelable(true);
            alertDlgBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
            alertDlgBuilder.setTitle(R.string.sp_notify_wifi_to_usbtethering_title_NORMAL);
            alertDlgBuilder.setMessage(R.string.sp_notify_wifi_to_usbtethering_message_NORMAL);
            alertDlgBuilder.setPositiveButton(R.string.def_yes_btn_caption,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            disableWifi();
                            mUsbTether.setChecked(true);
                            onPreferenceTreeClick(null, mUsbTether);
                            dialog.dismiss(); //disappear dialog
                        }
                    });
            alertDlgBuilder.setNegativeButton(R.string.def_no_btn_caption,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss(); //disappear dialog
                        }
                    });
            return alertDlgBuilder.create();
        }
        /* LGE_CHANGE_E, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/

        return null;
    }

    /* LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/
    public void disableWifi() {
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            //boolean isWifiEnabled = mWifiManager.isWifiEnabled();
            //            if (DBG) {
            //                Log.e(TAG, isWifiEnabled ? "WiFi WifiOffloading changed to False": "WiFi WifiOffloading changed to True");
            //            }
            mWifiManager.setWifiEnabled(false);
        }
    }

    /* LGE_CHANGE_E, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);

        intentFilter.addAction(UsbManager.ACTION_USB_STATE);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNSHARED);

        //LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check
        // BEGIN: 0018445 byungkook.son@lge.com 2010-03-24
        // ADD 0018445 : Apply hotspot provisioning according to VZW requirement
        if (PROVISION) {
            intentFilter.addAction("com.lge.hotspotprovision.STATE_CHANGED");
        }
        // END: 0018445 byungkook.son@lge.com 2010-03-24
        //LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check

        getActivity().registerReceiver(mStateReceiver, intentFilter);
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
        //LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check
        // BEGIN: 0018445 byungkook.son@lge.com 2010-03-24
        // ADD 0018445 : Apply hotspot provisioning according to VZW requirement
        if (PROVISION) {
            mIsProvisioned = false;
        }
        // END: 0018445 byungkook.son@lge.com 2010-03-24
        //LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
        // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    private void updateState() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return;
        }

        String[] available = cm.getTetherableIfaces();
        String[] tethered = cm.getTetheredIfaces();
        String[] errored = cm.getTetheringErroredIfaces();
        updateState(available, tethered, errored);
    }

    private void updateState(Object[] available, Object[] tethered,
            Object[] errored) {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-242][ID-MDM-75]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && com.android.settings.MDMSettingsAdapter.getInstance().setUsbTetheringMenu(
                        mUsbTether)) {
            return;
        }
        // LGMDM_END
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean usbTethered = false;
        boolean usbAvailable = false;
        int usbError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        boolean usbErrored = false;

        for (Object o : available) {
            String s = (String)o;
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    usbAvailable = true;
                    if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        usbError = cm.getLastTetherError(s);
                    }
                }
            }
        }
        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    usbTethered = true;
                }
            }
        }
        for (Object o : errored) {
            String s = (String)o;
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    usbErrored = true;
                }
            }
        }

        if (mUsbTether == null) {
            return;
        }

        /*if (usbTethered) {
            mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
            mUsbTether.setEnabled(true);
            mUsbTether.setChecked(true);
        } else if (usbAvailable) {
            if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
            } else {
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            }
            mUsbTether.setEnabled(true);
            mUsbTether.setChecked(false);
        } else if (usbErrored) {
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else if (mMassStorageActive) {
            mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else {
            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        }*/

        Log.d("USB_TETHER", "[MHP_GOOKY] usbTethered: " + usbTethered + ",usbAvailable: "
                + usbAvailable + ", usbErrored: " + usbErrored);

        if (usbTethered) {
            mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
            mUsbTether.setEnabled(true);
            mUsbTether.setChecked(true);
            /*} else if (usbAvailable || mUsbConnection) {
                if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
                } else {
                    mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                }
                mUsbTether.setEnabled(true);
                mUsbTether.setChecked(false); */
        } else if (usbErrored) {
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else if (mMassStorageActive) {
            mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else {
            if (mUsbConnection) {
                if (mCdromStorage) {
                    mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                    mUsbTether.setEnabled(false);
                    mUsbTether.setChecked(false);
                } else {
                    UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
                    String defaultFuction = usbManager.getDefaultFunction();
                    if (defaultFuction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                        mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
                        mUsbTether.setEnabled(true);
                        mUsbTether.setChecked(false);
                    } else {
                        mUsbTether.setSummary(R.string.sp_usb_tether_active_summary_NORMAL);
                        mUsbTether.setEnabled(false);
                        mUsbTether.setChecked(false);
                    }
                }
            } else {
                mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
                mUsbTether.setEnabled(false);
                mUsbTether.setChecked(false);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (preference == mUsbTether) {
            boolean newState = mUsbTether.isChecked();

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-242][ID-MDM-75]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && com.android.settings.MDMSettingsAdapter.getInstance().setUsbTetheringMenu(
                            mUsbTether)) {
                return true;
            }
            // LGMDM_END

            if (mUsbTether == null) {
                return true;
            }

            // LGE_CHANGE_S, yeonho.park@lge.com, 2012-05-02, Before start Hotspot Provison, do not use USB tethering
            /*
                    if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        mUsbTether.setChecked(false);
                        mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                        return true;
                    }
                    mUsbTether.setSummary("");
            */
            // LGE_CHANGE_S, yeonho.park@lge.com, 2012-05-02, Before start Hotspot Provison, do not use USB tethering

            /* LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/
            WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            if (mWifiManager != null) {
                boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                if (isWifiEnabled == true) {
                    Log.d("Tethersettings", "WiFi is ON! ");
                    mUsbTether.setChecked(false);
                    showDialog(DIALOG_WIFI_ON);
                    return true;
                }
            }
            /* LGE_CHANGE_E, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when USB Tether*/

            //LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check

            if (PROVISION)
            {
                //                    if (DBG) {
                //                        Log.d("USB_TETHER", "[MHP_GOOKY] onPreferenceTreeClick(), PROVISION :1");
                //                    }
                if (newState)
                {
                    //              if (DBG) {
                    //                  Log.d("USB_TETHER", "[MHP_GOOKY] onPreferenceTreeClick(), PROVISION :1,onPreferenceTreeClick:1");
                    //              }
                    // BEGIN: 0018477 byungkook.son@lge.com 2010-03-24
                    // ADD 0018477 : Change check flow when Mobile Broadband Connection enable
                    /*
                    TelephonyManager tm=TelephonyManager.getDefault();
                    if (!tm.hasIccCard())
                    {
                        //   Toast.makeText(mContext, R.string.mobilehotspotlimitnoti, Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this);
                        alertDlgBuilder.setMessage("No SIM card detected.\nMobile Hotspot/Mobile Broadband Connect service needs a Verizon SIM card in order to work.");//PARKJAEWOO 20110321 change the string for NO SIM
                        alertDlgBuilder.setPositiveButton("OK",null);
                        alertDlgBuilder.show();
                        mUsbTether.setChecked(false);
                        return false;
                    }
                    */

                    if (mIsProvisioned) {
                        //                            if (DBG) {
                        //                                Log.d("USB_TETHER", "[MHP_GOOKY] mIsProvisioned: 1, PROVISION :1,    newstate: 0");
                        //                            }
                        processTurnOnOff();
                    } else {
                        //                            if (DBG) {
                        //                                Log.d("USB_TETHER", "[MHP_GOOKY] mIsProvisioned: 0, PROVISION :1, start intent ");
                        //                            }
                        Intent proIntent = new Intent("com.lge.hotspot.provision_start");
                        startActivity(proIntent);
                        return true;
                    }
                } else {
                    //                        if (DBG) {
                    //                            Log.d("USB_TETHER", "[MHP_GOOKY] PROVISION :1,  newstate: 0");
                    //                        }
                    processTurnOnOff();
                }
            }
            // Not Apply hotspot provision
            else {
                //LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com], 2011.09.05, for SPG check

                if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    Log.d("USB_TETHER",
                            "[MHP_GOOKY] setUsbTethering: STATE mUsbTether.setChecked(false)");
                    mUsbTether.setChecked(false);
                    mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                    return true;
                }
                mUsbTether.setSummary("");
            }
            //LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2012.04.23, for SPG check
        }
        //LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com], 2011.04.23, for SPG check

        /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */

        return false;
    }

    private String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    /* LGE_VERIZON_WIFI_S, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */
    private boolean processTurnOnOff()
    {
        boolean newState = mUsbTether.isChecked();
        Log.d("USB_TETHER", "[MHP_GOOKY] processTurnOnOff : STATE : " + newState);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return true;
        }

        if (newState) {
            if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                mUsbTether.setChecked(false);
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                return true;
            }
            //                mUsbTether.setSummary("");
        } else {

            if (cm.setUsbTethering(newState) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                mUsbTether.setChecked(false);
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
                return true;
            }

            mUsbTether.setSummary("");
        }
        return true;
    }

    /* LGE_VERIZON_WIFI_E, [jeongah.min@lge.com], 2011-04-12, <MobileHotSpot Provisioning> */

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235][ID-MDM-35]
    // [ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveTetherPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
}
