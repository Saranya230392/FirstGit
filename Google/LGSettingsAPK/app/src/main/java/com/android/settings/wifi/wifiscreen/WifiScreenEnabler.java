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

package com.android.settings.wifi.wifiscreen;

import com.android.settings.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Message;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.view.View;

//WfdManager
//import com.lge.systemservice.core.LGContextImpl;
//import com.lge.systemservice.core.LGContext;
//import com.lge.systemservice.core.wfdmanager.WfdManager;

import android.util.Log;

//LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [S]
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
//LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [E]

import android.preference.PreferenceCategory;


/**
 * WifiDisplayEnabler is a helper to manage the Wifi display on/off
 */
public class WifiScreenEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "WifiScreenEnabler";

    private final Context mContext;
    private Switch mSwitch;

    private final IntentFilter mIntentFilter;
//    private WfdManager mWfdManager = null;
    private boolean mStateMachineEvent;
    private static boolean mWifiScreenState = false;

    private static final String WFD_STATE_CHANGED_ACTION =
        "com.lge.systemservice.core.wfdmanager.WFD_STATE_CHANGED";
    private static final String EXTRA_WFD_STATE = "wfd_state";
    private static final int WFD_STATE_DISABLED = 0;	// Wi-Fi Direct is turned off
    private static final int WFD_STATE_ENABLING = 1;	// Initializing JNI and RTSP server
    private static final int WFD_STATE_NOT_CONNECTED = 2;	// No connection found
    private static final int WFD_STATE_CONNECTING = 3;	// Link connection in progress
    private static final int WFD_STATE_LINK_CONNECTED = 4;	// (L2 connection established) WFD paring in progress (M1-M4)
    private static final int WFD_STATE_WFD_PAIRED = 5;	// WFD Paired: RTSP Running
    private static final int WFD_STATE_DISCONNECTING = 6; // State will be changed to WFD_STATE_NOT_CONNECTED
    private static final int WFD_STATE_DISABLING = 7;	// Disabling JNI interface
    private static final int WFD_STATE_UNKNOWN = 8;

    private static int mWfdState = WFD_STATE_UNKNOWN;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WFD_STATE_CHANGED_ACTION.equals(action)) {
                handleScreenStateChanged(intent.getIntExtra(
                        EXTRA_WFD_STATE, WFD_STATE_DISABLED));
            }
        }
    };

    public static final String getWifiScreenSettingsFragmentName() {
        return "com.android.settings.wifi.wifiscreen.WifiScreenSettings";
    }

    public WifiScreenEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WFD_STATE_CHANGED_ACTION);
    }

    private PreferenceCategory mDeviceCategory; 

    public WifiScreenEnabler(Context context, Switch switch_, PreferenceCategory mDevice) {
        mContext = context;
        mSwitch = switch_;
        mDeviceCategory = mDevice;

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WFD_STATE_CHANGED_ACTION);
    }

    public void resume() {
        if (Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.WIFI_DISPLAY_ON, 0) != 0) {
            handleScreenStateChanged(WFD_STATE_NOT_CONNECTED);
        } else {
            handleScreenStateChanged(WFD_STATE_DISABLED);
        }
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.wifi.MDMWifiSettingsAdapter.getInstance()
                    .setWiFiScreenEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void pause() {
        //if (mWfdManager == null) return;
        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
        /*
        final int WfdState = mWfdManager.getWfdState();
        boolean isEnabled = WfdState == WfdManager.WFD_STATE_NOT_CONNECTED;
        boolean isDisabled = WfdState == WfdManager.WFD_STATE_DISABLED;
        mSwitch.setChecked(isEnabled);
        mSwitch.setEnabled(isEnabled || isDisabled);
        */
        mSwitch.setChecked(mWifiScreenState); // temp code
        mSwitch.setEnabled(true); // temp code
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.wifi.MDMWifiSettingsAdapter.getInstance()
                    .setWiFiScreenEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void setSwitch(Switch switch_, int wfd_state) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
        handleScreenStateChanged(wfd_state);
        /*
        final int WfdState = mWfdManager.getWfdState();
        boolean isEnabled = WfdState == WfdManager.WFD_STATE_NOT_CONNECTED;
        boolean isDisabled = WfdState == WfdManager.WFD_STATE_DISABLED;
        mSwitch.setChecked(isEnabled);
        mSwitch.setEnabled(isEnabled || isDisabled);
        */
        //mSwitch.setChecked(mWifiScreenState);
        //mSwitch.setEnabled(true);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.wifi.MDMWifiSettingsAdapter.getInstance()
                    .setWiFiScreenEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }

        //LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [S]
        if (SystemProperties.get("service.btui.ampstate", "0").equals("1")) {
            Intent intent = new Intent("android.bluetooth.intent.action.WIFI_BT_HS_COEX");
            mContext.sendBroadcast(intent);
            mHandler.sendEmptyMessage(MSG_AMP_BUSY);
            return;
        }
        //LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [E]

        //WfdManager lWfdManager = null;

        //lWfdManager = getWfdManager();
        //if (lWfdManager == null) return;

        if (setWifiDisplayEnabled(isChecked)) {
            if (isChecked) {
                if(!mWifiScreenState) {
                    mSwitch.setEnabled(false);
                }
            } else {
                if(mWifiScreenState) {
                    mSwitch.setEnabled(false);
                }
            }
        } else {
            mHandler.sendEmptyMessage(MSG_AMP_BUSY);
        }
    }

/*
    public boolean onPreferenceChange(Preference preference, Object value) {
        mCheckBox.setEnabled(false);

    }
*/

    private boolean setWifiDisplayEnabled(boolean enable) {
        Intent intent;
        if (enable) {
            intent = new Intent("com.lge.systemservice.core.wfdmanager.WFD_ENABLE");
        } else {
            intent = new Intent("com.lge.systemservice.core.wfdmanager.WFD_DISABLE");
        }
        mContext.sendBroadcast(intent);
        return true;
    }

    /*private WfdManager getWfdManager() {
        if(mWfdManager == null) {
            LGContext mServiceContext = new LGContextImpl(mContext);
            mWfdManager = (WfdManager) mServiceContext.getLegacyService(LGContext.ServiceList.LGEWIFIDISPLAY_SERVICE);
        }
        return mWfdManager;
    }*/

    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    private void handleScreenStateChanged(int state) {
       Log.v(TAG, "handleScreenStateChanged: " + state + " set: " + mSwitch.isChecked() + " enabled: " + mSwitch.isEnabled());
       mWfdState = state;
       switch (state) {
        case WFD_STATE_DISABLED:
            mWifiScreenState = false;
            mSwitch.setEnabled(true);
            setSwitchChecked(false);

            if (mDeviceCategory != null) {
                mDeviceCategory.setTitle(R.string.wifi_display_available_devices);
            }
            break;
        case WFD_STATE_ENABLING:
            mWifiScreenState = false;
            mSwitch.setEnabled(false);
            setSwitchChecked(true);
            break;
        case WFD_STATE_NOT_CONNECTED:
        case WFD_STATE_CONNECTING:
        case WFD_STATE_LINK_CONNECTED:
        case WFD_STATE_WFD_PAIRED:
        case WFD_STATE_DISCONNECTING:
            mWifiScreenState = true;
            mSwitch.setEnabled(true);
            setSwitchChecked(true);
            break;
        case WFD_STATE_DISABLING:
            mWifiScreenState = false;
            mSwitch.setEnabled(false);
            setSwitchChecked(false);
            break;
        default:
            Log.e(TAG,"Unhandled wifi state " + state);
            break;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-47]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.wifi.MDMWifiSettingsAdapter.getInstance()
                    .setWiFiScreenEnablerMenu(mSwitch);
        }
        // LGMDM_END
    }

//LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [S]
    private final int MSG_AMP_BUSY = 0;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MSG_AMP_BUSY:
                    mSwitch.setChecked(false);
                    break;
            }
        }
    };
//LG_BTUI_HS : BT 3.0 High Speed - kukdong.bae@lge.com [E]
}
