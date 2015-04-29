/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.WirelessSettings;
import com.android.settings.lgesetting.Config.Config;

import java.util.concurrent.atomic.AtomicBoolean;
// 3LM_MDM_DCM jihun.im@lge.com 20121015
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;

// LGE_WLAN_HS20_EXT_CFG
// soonhyuk.choi@lge.com start..
import java.io.File;
import android.os.Environment;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
// soonhyuk.choi@lge.com end..

import android.net.ConnectivityManager;

import com.lge.wifi.impl.mobilehotspot.MHPProxy;


public class WifiEnabler implements CompoundButton.OnCheckedChangeListener  {
    private static final String TAG = "WifiSettings";

    private final Context mContext;
    private Switch mSwitch;
    private TextView mSummary_VZW;
    private NetworkInfo mNetworkInfo;
    private final AtomicBoolean mConnected = new AtomicBoolean(false);

    private final WifiManager mWifiManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mNetworkInfo = info;
                if (info != null) {
                    mConnected.set(info.isConnected());
                    handleStateChanged(info.getDetailedState());
                }
            }
        }
    };

    public WifiEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if(com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedByMdm()) {
                setSwitchChecked(false);
                mSwitch.setEnabled(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //[jaewoong87.lee@lge.com] 2013.04.25 if Airplane mode on, wifi should not be turned on by CMCC requirements        
        //mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED); // CMCC requirements change
    }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        
        handleWifiStateChanged(mWifiManager.getWifiState());
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWiFiEnableMenu(
                    mSwitch);
        }
        // LGMDM_END
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121029
        if(com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedByMdm()) {
                setSwitchChecked(false);
                mSwitch.setEnabled(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121029
    }

    public void pause() {
        
        //[Start]Hoon2.lim@lge.com  to avaoid SorryPopup "Receiver not registered"
        //mContext.unregisterReceiver(mReceiver);
        try{
            mContext.unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){}
        //[End]Hoon2.lim@lge.com      to avaoid SorryPopup "Receiver not registered"     
  
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_) {
        if (Config.getOperator().equals(Config.VZW)) {
            return;
        }
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

        final int wifiState = mWifiManager.getWifiState();

        //wifi switch for WFC
        if ( wifiState == WifiManager.WIFI_STATE_ENABLING ) {
            //eunjungjudy.kim@lge.com 2014.02.21 Select to Wi-Fi ON -> Wi-Fi OFF -> Wi-Fi ON
            setSwitchChecked(true);
            mSwitch.setEnabled(false);
            return;
        }

        boolean isEnabled = wifiState == WifiManager.WIFI_STATE_ENABLED;
        boolean isDisabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
        //mSwitch.setChecked(isEnabled);
        setSwitchChecked(isEnabled);
        mSwitch.setEnabled(isEnabled || isDisabled);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWiFiEnableMenu(mSwitch);
        }
        // LGMDM_END
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if(com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedByMdm()) {
                mSwitch.setEnabled(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
    }

    public void setSwitch(Switch switch_, TextView summary) {
        // Dummy
        if (!Config.getOperator().equals(Config.VZW)) {
            return;
        }

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

        final int wifiState = mWifiManager.getWifiState();
        boolean isEnabled = wifiState == WifiManager.WIFI_STATE_ENABLED;
        boolean isDisabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
        mSwitch.setChecked(isEnabled);
        mSwitch.setEnabled(isEnabled || isDisabled);

        mSummary_VZW = summary;
        if (mSummary_VZW != null) {
            setSummary(wifiState);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWiFiEnableMenu(mSwitch);
        }
        // LGMDM_END
    }

// soonhyuk.choi@lge.com
// LGE_WLAN_HS20_EXT_CFG
    public void setPasspointConfiguration( boolean isChecked) {
        // check sungyeon.kim not supported??
        if (Config.getOperator().equals(Config.VZW)) {
            return;
        }

        if ((SystemProperties.getBoolean("wlan.lge.passpoint_demo", false) == true) &&
            (SystemProperties.getBoolean("wlan.lge.passpoint_setting", false) == true)) {
            if (isChecked == true) {
                String mFileName = "lge_cred.conf";

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String root = Environment.getExternalStorageDirectory().getPath();
                    String srcFilePath = root + "/Download/" + mFileName;
                    StringBuffer stringBuffer = new StringBuffer();

                    File mSrcCredfile = new File(srcFilePath);

                    if (mSrcCredfile.exists()) {
                        try {
                            String line;
                            BufferedReader bufferedReader =
                                new BufferedReader(new FileReader(srcFilePath));
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuffer.append(line).append("\n");
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            
                            String destFilePath = "/data/misc/wifi/" + mFileName;
                            File mDestCredfile = new File(destFilePath);
                            BufferedWriter bufferedWriter = null;

                            try {
                                if (mDestCredfile.createNewFile() == false) {
                                    mDestCredfile.delete();
                                    mDestCredfile.createNewFile();
                                }
                                bufferedWriter = new BufferedWriter(new FileWriter(mDestCredfile));
                                bufferedWriter.write(stringBuffer.toString());
                                bufferedWriter.close();
                                mDestCredfile.setExecutable(true, false);
                                mDestCredfile.setReadable(true, false);
                                mDestCredfile.setWritable(true, false);
                                } catch (IOException e) {
                                    return;
                                }
                        } catch (Exception e) {
                            return;
                        }
                    }
                    else {
                        String destFilePath = "/data/misc/wifi/" + mFileName;
                        File mDestCredfile = new File(destFilePath);
//                        BufferedWriter bufferedWriter = null;
                        if (mDestCredfile.exists() ) {
                            mDestCredfile.delete();
                        }
                    }
                 }
             }
        }
// soonhyuk.choi@lge.com end..
    }
// soonhyuk.choi@lge.com end..

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }

        if (mWifiManager ==  null) { //wbt 5101
            return;
        }

        //[jaewoong87.lee@lge.com] 2013.04.25 if Airplane mode on, wifi should not be turned on by CMCC requirements[S]
        /* if (Config.getOperator().equals("CMCC")) {
            //ljw
            boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                            Settings.Global.AIRPLANE_MODE_ON, 0) != 0; 

            if (isAirplaneMode) {
                mSwitch.setChecked(false);
                Toast.makeText(mContext, R.string.sp_wifi_notify_deactivate_airplane_mode_NORMAL, Toast.LENGTH_SHORT).show();  
                return;
            }
        } */ // CMCC requirements change
        //[jaewoong87.lee@lge.com] 2013.04.25 if Airplane mode on, wifi should not be turned on by CMCC requirements[E]

        // Show toast message if Wi-Fi is not allowed in airplane mode


        // LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [S]
        if (Config.getOperator().equals(Config.VZW) &&
            SystemProperties.get("service.btui.ampstate", "0").equals("1")) {
            Intent intent = new Intent("android.bluetooth.intent.action.WIFI_BT_HS_COEX");
            mContext.sendBroadcast(intent);
            mHandler.sendEmptyMessage(mMSG_AMP_BUSY);
            return;
        }
        // LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [E]

        if (isChecked && !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_WIFI)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off. No infinite check/listenenr loop.
            buttonView.setChecked(false);
            return;
        }

        if (Config.getOperator().equals(Config.VZW)) {
            boolean enabled = false;
            if (null != mWifiManager) {
                Log.i(TAG, "mWifiManager is not null calling setWifiEnabled");
                if (Utils.isLUpgradeModelForMHP()) {
                    MHPProxy mMHPProxy = MHPProxy.getMobileHotspotServiceProxy();
                    enabled = mMHPProxy.isMHPEnabled();
                } else {
                    enabled = mWifiManager.isWifiApEnabled();
                }
                if (enabled == false) {
                    Log.e(TAG, "isVZWMobileHotspotEnabled == false");
                }
            }

            if (isChecked && enabled)
            {
                Log.d("WifiEnabler", "MHP is STATE_ON! => Dialog ON/OFF! ");
                        setSwitchChecked(false);
                        mSwitch.setEnabled(true);
                        
                Class<?> mWifiVZWInteractiveDialog = null;
                try {
                    // intent
                    Intent di = new Intent();
                    
                    mWifiVZWInteractiveDialog = Class.forName("com.lge.wifisettings.WifiVZWInteractiveDialog");
//                    di.setClass(mContext, WifiVZWInteractiveDialog.class);
                    di.setClass(mContext, mWifiVZWInteractiveDialog);
                    di.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mContext.startActivity(di);
                } catch (ClassNotFoundException e1) {
                    Log.e(TAG, "Please check operator name");
                } catch (Exception e) {
                    Log.e(TAG, "error startActivity :(WifiVZWInteractiveDialog) : ", e);
                }
                // intent

                return;
            }
            // LGE_VERIZON_WIFI_E,[kyubyoung.lee@lge.com], 2011.09.03, for VZW
            // Wi-Fi UI req.
            // LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu
            // disable when USB Tether
            else if (isChecked && getTetheredUsb())
            {
                Log.d("WifiEnabler", "USB Tether is ON! ");

                 setSwitchChecked(false);
                 mSwitch.setEnabled(true);
                 
                 Class<?> mWifiVZWErrorDialogClz = null;
                // intent
                try {
                    mWifiVZWErrorDialogClz = Class.forName("com.lge.wifisettings.WifiVZWErrorDialog");

                    Intent di = new Intent();
                    //di.setClass(mContext, WifiVZWErrorDialog.class);
                    di.setClass(mContext, mWifiVZWErrorDialogClz);
                    di.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mContext.startActivity(di);
                } catch (ClassNotFoundException e1) {
                    Log.e(TAG, "Please check operator name");
                } catch (Exception e) {
                    Log.e(TAG, "error startActivity :(WifiVZWInteractiveDialog) : ", e);
                }
                // intent

                return;
            }
            // LGE_CHANGE_E, [jeongah.min@lge.com], 2011-10-09, WiFi menu
            // disable when USB Tether
        }
        else {
        if (!isChecked &&
            (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED ||
             mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLING)) {
            return;
        }

        // Disable tethering if enabling Wifi
        int wifiApState = mWifiManager.getWifiApState();
        if (isChecked && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {

            // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
            if (Utils.isUI_4_1_model(mContext) &&
                Config.getOperator().equals(Config.KT)) {
                createHotspotWarningPopupForKT();
                return;
            }

            mWifiManager.setWifiApEnabled(null, false);
        }
        }
        mSwitch.setEnabled(false);

        /* soonhyuk.choi@lge.com for the passpoint demostration.
      When Enable LGE_WLAN_HS20_EXT_CFG feature in the supplicant.
      have to define below function to set the lge_cred.conf file. */
        // LGE_WLAN_HS20_EXT_CFG
        //setPasspointConfiguration(isChecked);
        if (!mWifiManager.setWifiEnabled(isChecked)) {
            // Error
            mSwitch.setEnabled(true);
            //Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
        }

        if (Config.getOperator().equals(Config.VZW) && mSummary_VZW != null) {
            setSummary(state);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMWifiSettingsAdapter.getInstance().setWiFiEnableMenu(mSwitch);
        }
        // LGMDM_END
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        if(com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedByMdm()) {
                setSwitchChecked(false);
                mSwitch.setEnabled(false);
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
    }

    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    //private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a CheckBoxPreference to a Switch, this method is useless since
        // there is nowhere to display a summary.
        // This code is kept in case a future change re-introduces an associated text.
        /*
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the switch as an optimization.
        if (state != null && mSwitch.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //setSummary(Summary.get(mContext, info.getSSID(), state));
            }
        }
        */
    //}
    private void handleStateChanged(NetworkInfo.DetailedState state) {
        if (!Config.getOperator().equals(Config.VZW)) {
            return;
        }

        if (state != null /*&& mSwitch.isChecked()*/) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //String format = mContext.getResources().getString((state == NetworkInfo.DetailedState.CONNECTED)
                //    ? R.string.wifi_settings_summary_wifi_connected : R.string.wifi_error);

                if (state == NetworkInfo.DetailedState.CONNECTED) {
                    if (mSummary_VZW != null) {
                        mSummary_VZW.setVisibility(View.VISIBLE);
                        mSummary_VZW.setText(String.format(mContext.getResources().getString(R.string.wifi_settings_summary_wifi_connected), info.getSSID()));
                    }
                } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                    if (mSummary_VZW != null && mWifiManager.isWifiEnabled()) {
                        if (Config.getOperator().equals(Config.VZW)) {
                            mSummary_VZW.setText(R.string.sp_wifi_select_network);
                        } else {
                            mSummary_VZW.setText(R.string.wifi_settings_summary_wifi_on);
                        }
                    }
                } else if (state == NetworkInfo.DetailedState.FAILED) {
                    if (mSummary_VZW != null) {
                        mSummary_VZW.setVisibility(View.VISIBLE);
                        mSummary_VZW.setText(R.string.wifi_error);
                    }
                }
            }
        }
    }

    // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
    /**
     * Check if WiFi is blocked by 3LM service.
     * @return
     */
    private boolean isBlockedByMdm() {
        return SystemProperties.getInt("persist.security.wifi.lockout", 1) == 0;
    }
    // 3LM_MDM_DCM_END jihun.im@lge.com 20121015

    // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
    private void createHotspotWarningPopupForKT() {

        if (mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_ENABLED) {
            mSwitch.setEnabled(false);
            
            if (!mWifiManager.setWifiEnabled(true)) {
                // Error
                mSwitch.setEnabled(true);
                Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        AlertDialog mKT_Hotspot_warning_dialog = new AlertDialog.Builder(mContext)
            .setTitle(R.string.sp_note_normal)
            //.setIconAttribute(android.R.attr.alertDialogIcon) //Not Used
            .setMessage(R.string.sp_hotspot_incompatible_wifi_wanning_NORMAL)
            .setCancelable(true)    //TD2422120984
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(true);
                    dialog.dismiss();
                    return;
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        setSwitchChecked(false);
                        mSwitch.setEnabled(true);
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setSwitchChecked(false);
                    mSwitch.setEnabled(true);
                    dialog.dismiss();
                    return;
                }
            })
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Disable tethering if enabling Wifi
                    if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLING ||
                        mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                        mWifiManager.setWifiApEnabled(null, false);
                    }

                    mSwitch.setEnabled(false);

                    if (!mWifiManager.setWifiEnabled(true)) {
                        // Error
                        mSwitch.setEnabled(true);
                        Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    return;
                }
            }).create();
        mKT_Hotspot_warning_dialog.show();
    }

    private void setSummary(int wifiState) {
        if (mSummary_VZW != null) {
            mSummary_VZW.setVisibility(View.VISIBLE);
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMWifiSettingsAdapter.getInstance().setWiFiEnableSummary(mSummary_VZW)) {
                    Log.i(TAG, "LGMDM set text Wifi disallow Summary ");
                    return;
                }
            }
            // LGMDM_END

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLING:
                    String text = mContext.getString(R.string.wifi_starting);
                    mSummary_VZW.setText(text);
//                    mSummary.setText(R.string.wifi_starting);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    if (mNetworkInfo == null) {
                        if (Config.getOperator().equals(Config.VZW)) {
                            mSummary_VZW.setText(R.string.sp_wifi_select_network);
                        } else {
                            mSummary_VZW.setText(R.string.wifi_settings_summary_wifi_on);
                        }
                    } else if (mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
                        if (Config.getOperator().equals(Config.VZW)) {
                            mSummary_VZW.setText(R.string.sp_wifi_select_network);
                        } else {
                            mSummary_VZW.setText(R.string.wifi_settings_summary_wifi_on);
                        }
                    } else {
                        handleStateChanged(mNetworkInfo.getDetailedState());
                    }
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    mSummary_VZW.setText(R.string.wifi_stopping);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    if (Config.getOperator().equals(Config.VZW)) {
                        mSummary_VZW.setText("");
                        mSummary_VZW.setVisibility(View.GONE);
                    } else {
                        mSummary_VZW.setText(R.string.wifi_settings_summary_wifi_off_Jb_plus);

                   }
                    break;
                default:
                    if (Config.getOperator().equals(Config.VZW)) {
                        mSummary_VZW.setText("");
                        mSummary_VZW.setVisibility(View.GONE);
                    } else {
                        mSummary_VZW.setText(R.string.wifi_settings_summary_wifi_off_Jb_plus);

                   }
                    break;
            }
        }
    }

    // LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [S]
    private final int mMSG_AMP_BUSY = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case mMSG_AMP_BUSY:
                    mSwitch.setChecked(false);
                    break;
                default:
                    break;
            }
        }
    };

    // LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [E]

    /*
     * LGE_CHANGE_S, [jeongah.min@lge.com], 2011-10-09, WiFi menu disable when
     * USB Tether
     */
    public boolean getTetheredUsb() {

        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        // String[] available = cm.getTetherableIfaces();
        String[] tethered = cm.getTetheredIfaces();
        String[] usbtetherable = cm.getTetherableUsbRegexs();
//        String[] Wifitetherable = cm.getTetherableWifiRegexs();

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : usbtetherable) {
                if (s.matches(regex)) {
                    Log.e("WifiEnabler", "[Nezzi-Sebong] USB Tethered ");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRadioAllowed(Context context, String type) {
         if (!isAirplaneModeOn(context)) {
             return true;
         }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.Global.getString(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    } 
    private boolean isAirplaneModeOn(Context context) {
        int bReturn = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        if ( bReturn == 1 ) {
            return true;
        } else {
            return false;
        }
    }
}
