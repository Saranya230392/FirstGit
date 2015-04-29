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

package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.TextView;
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
import android.text.TextUtils;
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION

import com.android.settings.R;
import com.android.settings.WirelessSettings;

//LG_BTUI : SWITCH_FEEDBACK_SOUND [s]
import android.view.View;
import android.view.View.OnClickListener;
//LG_BTUI : SWITCH_FEEDBACK_SOUND [e]

// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION 2013-06-27 koh.changseok@lge.com show device connection status on settings UI
import android.util.Log;
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
// 3LM_MDM L taewon.jang@lge.com [CONBT-1143]
import android.os.IDeviceManager3LM;
import android.os.RemoteException;
import android.os.ServiceManager;
//3LM_MDM L END [CONBT-1143]

import android.content.SharedPreferences;

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
public final class BluetoothEnabler implements CompoundButton.OnCheckedChangeListener {
    private final Context mContext;
    private Switch mSwitch;
    private static final String TAG = "BluetoothEnabler";
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
    private TextView mSummaryTextView;
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
    private boolean mValidListener;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;

    // BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION, modified jeongwoo.hwang@lge.com 20130816
    private static String connectedDeviceName;
    private static int connectedDeviceNum;
    // BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION, modified jeongwoo.hwang@lge.com 20130816

    public static final String BT_ENABLER_UPDATE_BROADCAST = "com.lge.bluetooth.action.BT_ENABLER_UPDATE_BROADCAST";
    private static final String BT_CONNECTED_DEVICE_PREF_NAME = "BT_CONNECTED_DEVICE_PREF_NAME";
    private static final String BT_CONNECTED_NAME_PREF_KEY = "CONNECTED_NAME";
    private static final String BT_CONNECTED_NUM_PREF_KEY = "CONNECTED_NUM";
    public static final int NOTSUPPORTED = 0;    
    public static final int NO_DEVICE_CONNECTED = 1;
    public static final int A_DEVICE_CONNECTED = 2;
    public static final int DEVICES_CONNECTED = 3;
        
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast receiver is always running on the UI thread here,
            // so we don't need consider thread synchronization.
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION 2013-06-27 koh.changseok@lge.com show device connection status on settings UI
            String action = intent.getAction();

            if (BT_ENABLER_UPDATE_BROADCAST.equals(action)) {
                Log.d("BluetoothEnabler", "onReceive(), action : " + action);
//BT_S: [PSIX-5105] token is changed for VZW Req.
                //setSummary(BluetoothAdapter.STATE_ON);
                if (mLocalAdapter != null) {
                    setSummary(mLocalAdapter.getBluetoothState());
                }
//BT_E: [PSIX-5105] token is changed for VZW Req.
            } else {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                handleStateChanged(state);
            }
/* original
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            handleStateChanged(state);
*/
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
        }
    };

// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION 2013-06-27 koh.changseok@lge.com show device connection status on settings UI
    public BluetoothEnabler(Context context, Switch switch_, TextView summary_) {
        this(context, switch_);
        mSummaryTextView = summary_;
    }
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
    public BluetoothEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mValidListener = false;

        LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context);
        if (manager == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
            mSwitch.setEnabled(false);
        } else {
            mLocalAdapter = manager.getBluetoothAdapter();
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION 2013-06-27 koh.changseok@lge.com show device connection status on settings UI
        mIntentFilter.addAction(BT_ENABLER_UPDATE_BROADCAST);
        mSummaryTextView = null;
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
    }

    public void resume() {
        if (mLocalAdapter == null) {
            mSwitch.setEnabled(false);
            return;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (mSwitch != null) {
                com.android.settings.bluetooth.MDMBluetoothSettingsAdapter.getInstance()
                        .setBluetoothEnableMenu(mSwitch);
            }
        }
        // LGMDM_END

        // Bluetooth state is not sticky, so set it manually
        handleStateChanged(mLocalAdapter.getBluetoothState());

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);
        mValidListener = true;
//LG_BTUI : SWITCH_FEEDBACK_SOUND [s]
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });
//LG_BTUI : SWITCH_FEEDBACK_SOUND [e]

        // 3LM_MDM L jihun.im@lge.com
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedMdm()) {
                setChecked(false);
                mSwitch.setEnabled(false);
              //  mSwitchBar.setEnabled(false);
            }
        }
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }
// BT_S : [CONBT-156] IllegalArgumentException

//        mContext.unregisterReceiver(mReceiver);
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        }
// BT_E : [CONBT-156] IllegalArgumentException
        mSwitch.setOnCheckedChangeListener(null);
        mValidListener = false;
//LG_BTUI : SWITCH_FEEDBACK_SOUND [s]
        mSwitch.setOnClickListener(null);
//LG_BTUI : SWITCH_FEEDBACK_SOUND [e]
    }

// F3Q_BTUI_CONNECTIONCHECK_SETTINGS[s]
    public void setSwitch(Switch switch_, TextView summary_) {
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
        mSummaryTextView = summary_;
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
        setSwitch(switch_);
    }
// F3Q_BTUI_CONNECTIONCHECK_SETTINGS[e]

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(mValidListener ? this : null);

        int bluetoothState = BluetoothAdapter.STATE_OFF;
        if (mLocalAdapter != null) {
            bluetoothState = mLocalAdapter.getBluetoothState();
        }
        boolean isOn = bluetoothState == BluetoothAdapter.STATE_ON;
        boolean isOff = bluetoothState == BluetoothAdapter.STATE_OFF;
        setChecked(isOn);
        mSwitch.setEnabled(isOn || isOff);
//LG_BTUI : SWITCH_FEEDBACK_SOUND [s]
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });
//LG_BTUI : SWITCH_FEEDBACK_SOUND [e]

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mSwitch != null) {
            com.android.settings.bluetooth.MDMBluetoothSettingsAdapter.getInstance()
                    .setBluetoothEnableMenu(mSwitch);
        }
        // LGMDM_END

// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
        setSummary(bluetoothState);
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
    }
//BT_S : [PSIX-6121] BT On/Off switch on breadcrumb
    public Switch getSwitch() {
        return mSwitch;
    }
//BT_E : [PSIX-6121] BT On/Off switch on breadcrumb
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode
        if (isChecked &&
                !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off
            buttonView.setChecked(false);
        }

        if (mLocalAdapter != null) {
            mLocalAdapter.setBluetoothEnabled(isChecked);
        }
        mSwitch.setEnabled(false);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mSwitch != null) {
            com.android.settings.bluetooth.MDMBluetoothSettingsAdapter.getInstance()
                    .setBluetoothEnableMenu(mSwitch);
        }
        // LGMDM_END
    }

    void handleStateChanged(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                setChecked(true);
                mSwitch.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                setChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                setChecked(false);
                mSwitch.setEnabled(true);
        }
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION
        setSummary(state);
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-35]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mSwitch != null) {
            com.android.settings.bluetooth.MDMBluetoothSettingsAdapter.getInstance()
                    .setBluetoothEnableMenu(mSwitch);
        }
        // LGMDM_END
        // 3LM_MDM L taewon.jang@lge.com [CONBT-1143]
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (isBlockedMdm()) {
                setChecked(false);
                mSwitch.setEnabled(false);
              //  mSwitchBar.setEnabled(false);
            }
        }
    }
    /**
     *  Check BT is blocked by 3LM service.
     * @return
     */
    private boolean isBlockedMdm() {
        IDeviceManager3LM dm = IDeviceManager3LM.Stub.asInterface(
                ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
        try {
            return !dm.getBluetoothEnabled();
        } catch (RemoteException e) {
            // System service, should never happen
            Log.e("BluetoothEnabler", "3LM Exception");
        }
        return false;
    }
        // 3LM_MDM L END [CONBT-1143]

    private void setChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            // set listener to null, so onCheckedChanged won't be called
            // if the checked status on Switch isn't changed by user click
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(null);
            }
            mSwitch.setChecked(isChecked);
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(this);
            }
        }
    }
// BT_S : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION 2013-06-27 koh.changseok@lge.com show device connection status on settings UI
    private void setSummary(int state) {

        Log.d("BluetoothEnabler", "setSummary(), state : " + state);

        if (mSummaryTextView == null) {
            return;
        }

        if (state == BluetoothAdapter.STATE_ON) {
            // +s modify LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION, jeongwoo.hwang@lge.com 20130816
            connectedDeviceNum = getConnectedDeviceNum(mContext);
            connectedDeviceName = getConnectedDeviceName(mContext);

            Log.d("BluetoothEnabler", "setSummary(), connectedDeviceName : " + connectedDeviceName + ", connectedDeviceNum : " + connectedDeviceNum);

            if (connectedDeviceNum == A_DEVICE_CONNECTED) {
                String summary = String.format(mContext.getResources().getString(R.string.wifi_settings_summary_wifi_connected),
                                                   connectedDeviceName );
            // +e modify LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION, jeongwoo.hwang@lge.com 20130816
                mSummaryTextView.setText(summary);
                Log.d("BluetoothEnabler", summary);
            } else if (connectedDeviceNum == DEVICES_CONNECTED) {
                    mSummaryTextView.setText(R.string.sp_bt_connected_to_multiple);
                    //Log.d("BluetoothEnabler", "Connected to multiple devices");
            } else {
//BT_S: [PSIX-5105] token is changed for VZW Req.
                //mSummaryTextView.setText(R.string.sp_easysettings_bt_off_NORMAL_jb_plus);
                mSummaryTextView.setText(R.string.sp_bt_pair_or_connect);
                //Log.d("BluetoothEnabler","Pair with a device");
//BT_E: [PSIX-5105] token is changed for VZW Req.
            }

        } else if (state == BluetoothAdapter.STATE_TURNING_ON) {
            mSummaryTextView.setText(R.string.sp_easysettings_bt_turning_on_NORMAL);
            //Log.d("BluetoothEnabler", "Turning Bluetooth on...");
        } else {
//BT_S: [PSIX-5105] token is changed for VZW Req.
            //mSummaryTextView.setText(R.string.sp_easysettings_bt_off_NORMAL_jb_plus);
            mSummaryTextView.setText(null);
            //Log.d("BluetoothEnabler", "setText to null");
//BT_E: [PSIX-5105] token is changed for VZW Req.
        }

//BT_S: [PSIX-5105] token is changed for VZW Req.
        if (!TextUtils.isEmpty(mSummaryTextView.getText())) {
            mSummaryTextView.setVisibility(View.VISIBLE);
        } else {
            mSummaryTextView.setVisibility(View.GONE);
        }
//BT_E: [PSIX-5105] token is changed for VZW Req.
    }
// BT_E : [PSIX-5105] LGBT_COMMON_SCENARIO_SHOW_DEVICECONNECTION

    public String getConnectedDeviceName(Context context) {

        SharedPreferences connectedDeviceNamePref;
        String deviceName;

        try {
            connectedDeviceNamePref = context.getSharedPreferences(BT_CONNECTED_DEVICE_PREF_NAME, Context.MODE_PRIVATE);
            deviceName = connectedDeviceNamePref.getString(BT_CONNECTED_NAME_PREF_KEY, null);

            Log.d(TAG, "getConnectedDeviceName(), deviceName : " + deviceName );

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception happened, set deviceName to null");
            deviceName = null;
        }

        return deviceName;
    }

    public int getConnectedDeviceNum(Context context) {

        SharedPreferences connectedDeviceNumPref;
        int deviceNum;

        try {
            connectedDeviceNumPref = context.getSharedPreferences(BT_CONNECTED_DEVICE_PREF_NAME, Context.MODE_PRIVATE);
            deviceNum = connectedDeviceNumPref.getInt(BT_CONNECTED_NUM_PREF_KEY, NOTSUPPORTED);

            Log.d(TAG, "getConnectedDeviceNum(), deviceNum : " + deviceNum );

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception happened, set deviceNum to NOTSUPPORTED");
            deviceNum = NOTSUPPORTED;
        }

        return deviceNum;

    }

}
