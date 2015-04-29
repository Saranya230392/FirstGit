/*========================================================================
Copyright (c) 2011 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------

=========================================================================*/

package com.android.settings.bluetooth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.lge.mdm.LGMDMManager;

public class MDMBluetoothSettingsAdapter {
    private static final boolean DEBUG = Utils.D;
    private static String TAG = "MDMBluetoothSettingsAdapter";

    // make sure LGMDMIntentInternal.java
    private static final String ACTION_BLUETOOTH_POLICY_CHANGE = "com.lge.mdm.intent.action.BLUETOOTH_POLICY_CHANGE";
    private static final int DISCOVERABLE_TIMEOUT_TWO_MINUTES = 120;
    private static final int DISCOVERABLE_TIMEOUT_FIVE_MINUTES = 300;
    private static final int DISCOVERABLE_TIMEOUT_ONE_HOUR = 3600;
    static final int DISCOVERABLE_TIMEOUT_NEVER = 0;

    private static MDMBluetoothSettingsAdapter mInstance;

    public static MDMBluetoothSettingsAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new MDMBluetoothSettingsAdapter();
        }
        return mInstance;
    }

    private MDMBluetoothSettingsAdapter() {
    }

    public boolean setTextViewDisabledString(TextView textView) {
        if (textView == null) {
            if (DEBUG) {
                Log.i(TAG, "setTextViewDisabledString");
            }
            return false;
        }

        if (LGMDMManager.getInstance().getAllowBluetooth(null) == LGMDMManager.LGMDMBluetooth_DISALLOW) {
            textView.setText(R.string.sp_lgmdm_block_bluetooth_NORMAL);
        } else if (LGMDMManager.getInstance().getAllowBluetooth(null) == LGMDMManager.LGMDMBluetooth_ALLOW_AUDIOONLY) {
            textView.setText(R.string.sp_lgmdm_only_audio_bluetooth_NORMAL);
        }
        return true;
    }

    public boolean setBluetoothEnableMenu(Switch menu) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (menu == null) {
            if (DEBUG) {
                Log.i(TAG, "setBluetoothEnableMenu()");
            }
            return false;
        }

        if (LGMDMManager.getInstance().getAllowBluetooth(null) == LGMDMManager.LGMDMBluetooth_DISALLOW) {
            menu.setEnabled(false);
        }

        return true;
    }

    public void setBluetoothDiscoverableEnableMenu(Context resContext,
            boolean discoverable, Preference discoveryPreference) {
        int btPolicy = LGMDMManager.getInstance().getAllowBluetooth(null);
        boolean visiblePolicy = LGMDMManager.getInstance().getAllowBluetoothVisible(null);
        if (btPolicy == LGMDMManager.LGMDMBluetooth_ALLOW_AUDIOONLY || visiblePolicy == false) {
            discoverable = false;
            if (discoveryPreference instanceof CheckBoxPreference) {
                CheckBoxPreference preference = (CheckBoxPreference)discoveryPreference;
                preference.setChecked(false);
            }
            discoveryPreference.setEnabled(false);
            String str = (String)resContext.getResources().getString(
                    R.string.sp_bt_visible_lbl_SHOR_new);//R.string.sp_bt_visible_lbl_SHORT);
            String strSummary = (String)resContext.getResources().getString(
//BT_S : [CONBT-1186] Request to change string resource from application team. , [START]
//                    com.lge.internal.R.string.sp_lgmdm_block_common_NORMAL, str);
                    com.lge.R.string.sp_lgmdm_block_common_NORMAL, str);
//BT_S : [CONBT-1186] Request to change string resource from application team. , [END]
            discoveryPreference.setSummary(strSummary);
        }
    }

    public void setBluetoothDiscoverableMenu(Preference discoveryPreference) {
        boolean visiblePolicy = LGMDMManager.getInstance().getAllowBluetoothVisible(null);
        int btPolicy = LGMDMManager.getInstance().getAllowBluetooth(null);
        if (btPolicy == LGMDMManager.LGMDMBluetooth_ALLOW_AUDIOONLY || visiblePolicy == false) {
            String str = (String)com.android.settings.Utils.getResources().getString(
                    R.string.sp_lgmdm_block_bt_visible);
            discoveryPreference.setSummary(str);
        }
    }

    public int getBluetoothDiscoverableTimeout(int timeout) {

        int maxVisiblityTimeOut = LGMDMManager.getInstance().getBluetoothMaxVisiblityTimeOut(null);
        if (maxVisiblityTimeOut == 0) {
            return timeout;
        }
        if (timeout > maxVisiblityTimeOut || timeout == 0) {

            if (DISCOVERABLE_TIMEOUT_NEVER < maxVisiblityTimeOut
                    && maxVisiblityTimeOut < DISCOVERABLE_TIMEOUT_FIVE_MINUTES) {
                return DISCOVERABLE_TIMEOUT_TWO_MINUTES;

            } else if (DISCOVERABLE_TIMEOUT_FIVE_MINUTES <= maxVisiblityTimeOut
                    && maxVisiblityTimeOut < DISCOVERABLE_TIMEOUT_ONE_HOUR) {
                return DISCOVERABLE_TIMEOUT_FIVE_MINUTES;

            } else if (DISCOVERABLE_TIMEOUT_ONE_HOUR <= maxVisiblityTimeOut) {
                return DISCOVERABLE_TIMEOUT_ONE_HOUR;
            } else {
                return timeout;
            }
        }
        return timeout;
    }

    public boolean checkBluetoothAudioOnly(String address) {
        boolean isAudioOnlyPolicy = (LGMDMManager.getInstance().getAllowBluetooth(null) == LGMDMManager.LGMDMBluetooth_ALLOW_AUDIOONLY);
        if (isAudioOnlyPolicy) {
            if (address == null) {
                if (DEBUG) {
                    Log.d(TAG, "address is null");
                }
                return true;
            }
            // adddress is not null, BT class check
            BluetoothDevice btDevice = BluetoothAdapter.getDefaultAdapter()
                    .getRemoteDevice(address);
            BluetoothClass btClass = btDevice.getBluetoothClass();
            if (btClass == null) {
                if (DEBUG) {
                    Log.d(TAG, "btClass is null");
                }
                return  true;
            }

            if (btClass.getMajorDeviceClass() == 0) { // not set
                if (DEBUG) {
                    Log.d(TAG, "btClass.getMajorDeviceClass() is zero, not set....");
                }
                return false;
            }

            boolean isAudioBTClass = (btClass.getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO);
            if (isAudioBTClass == false) {
                if (DEBUG) {
                    Log.d(TAG, "checkBluetoothAudioOnly block bluetooth except audio ");
                }
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public boolean checkBluetoothVisible(ComponentName who) {
        boolean ret = LGMDMManager.getInstance().getAllowBluetoothVisible(who);
        if (ret == true) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkBluetoothSearchType(ComponentName who,
            CachedBluetoothDevice bluetoothCashedDevice) {
        BluetoothDevice bluetoothDevice = bluetoothCashedDevice.getDevice();
        if (bluetoothCashedDevice.isUserInitiatedPairing()) {
            return false;
        }

        int nBluetoothSearhType = LGMDMManager.getInstance().getBluetoothSearchedDeviceType(who);
        BluetoothClass btClass = bluetoothDevice.getBluetoothClass();

        if (btClass == null) { // WBT 53641
            if (DEBUG) {
                Log.d(TAG, "btClass is null");
            }
            return true;
        }

        int nBluetoothClass = btClass.getMajorDeviceClass();

        switch (nBluetoothClass) {
        case BluetoothClass.Device.Major.AUDIO_VIDEO:
            nBluetoothClass = LGMDMManager.LGMDMBLUETOOTH_DEVICE_HEADSET;
            break;
        case BluetoothClass.Device.Major.COMPUTER:
            nBluetoothClass = LGMDMManager.LGMDMBLUETOOTH_DEVICE_PC;
            break;
        case BluetoothClass.Device.Major.PHONE:
            nBluetoothClass = LGMDMManager.LGMDMBLUETOOTH_DEVICE_MOBILEPHONE;
            break;
        case BluetoothClass.Device.Major.PERIPHERAL:
            nBluetoothClass = LGMDMManager.LGMDMBLUETOOTH_DEVICE_HID;
            break;
        default:
            nBluetoothClass = LGMDMManager.LGMDMBLUETOOTH_DEVICE_ETC;
            break;
        }

        if ((nBluetoothSearhType & nBluetoothClass) != nBluetoothClass) {
            if (DEBUG) {
                Log.d(TAG, "checkBluetoothSearchType block bluetooth except device ");
            }
            return true;
        }

        return false;
    }

    public void addBluetoothChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_BLUETOOTH_POLICY_CHANGE);
    }

    public boolean receiveBluetoothChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        if (ACTION_BLUETOOTH_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public int getSearchFilterPos(int pos) {
        if (pos == -1) {
            if (DEBUG) {
                Log.i(TAG, "getSearchFilterPos pos : " + pos);
            }
            return 0;
        } else {
            return pos;
        }
    }

    public boolean checkBluetoothPairingAndToast(Context context) {
        boolean btPolicy = LGMDMManager.getInstance().getAllowBluetoothPairing(null);
        if (context != null && btPolicy == false) {
            String str1 = context.getResources().getString(
//BT_S : [CONBT-1186] Request to change string resource from application team. , [START]
//                    com.lge.internal.R.string.sp_lgmdm_bluetooth_pairing_SHORT);
                    com.lge.R.string.sp_lgmdm_bluetooth_pairing_SHORT);
//BT_E : [CONBT-1186] Request to change string resource from application team. [END]
            Toast.makeText(
                    context,
                    context.getResources().getString(
//BT_S : [CONBT-1186] Request to change string resource from application team. , [START]
//                            com.lge.internal.R.string.sp_lgmdm_block_common_point, str1),
                         com.lge.R.string.sp_lgmdm_block_common_point, str1),
//BT_E : [CONBT-1186] Request to change string resource from application team. [END]
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public String[] getVisibilityTimeoutMenu(ComponentName who, Context context) {
        int maxVisiblityTimeOut = LGMDMManager.getInstance().getBluetoothMaxVisiblityTimeOut(who);
        ArrayList vTimeList = new ArrayList(Arrays.asList((String[])context.getResources()
                .getStringArray(R.array.bluetooth_visibility_timeout_entries)));

        if (DISCOVERABLE_TIMEOUT_NEVER < maxVisiblityTimeOut
                && maxVisiblityTimeOut < DISCOVERABLE_TIMEOUT_FIVE_MINUTES) {
            vTimeList.remove(3);
            vTimeList.remove(2);
            vTimeList.remove(1);
        } else if (DISCOVERABLE_TIMEOUT_FIVE_MINUTES <= maxVisiblityTimeOut
                && maxVisiblityTimeOut < DISCOVERABLE_TIMEOUT_ONE_HOUR) {
            vTimeList.remove(3);
            vTimeList.remove(2);
        } else if (DISCOVERABLE_TIMEOUT_ONE_HOUR <= maxVisiblityTimeOut) {
            vTimeList.remove(3);
        }
        return (String[])vTimeList.toArray(new String[vTimeList.size()]);
    }

    public boolean checkBluetoothVisibilityTimeOut(ComponentName who) {
        int maxVisiblityTimeOut = LGMDMManager.getInstance().getBluetoothMaxVisiblityTimeOut(who);
        if (maxVisiblityTimeOut == 0) {
            return false;
        } else {
            return true;
        }
    }

}
