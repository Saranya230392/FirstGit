/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.TwoStatePreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.lgesetting.Config.Config;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
import java.util.Timer;
import java.util.TimerTask;

//shlee1219.lee START
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.internal.telephony.ITelephony;
//shlee1219.lee END
import com.android.internal.telephony.PhoneFactory;

public class AirplaneModeEnabler implements Preference.OnPreferenceChangeListener {

    private final Context mContext;

    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private static boolean airplanemode_on_off = false;
    private final TwoStatePreference mTwoStatePref;
    private static boolean global_airplane;
    ServiceState serviceState = new ServiceState();
    private static final int EVENT_SERVICE_STATE_CHANGED = 3;
    private final int EVENT_AIRPLANE_MODE_UI_BLOCK_TIMER = 4;

    // kerry start
    protected TelephonyManager mPhone;
    protected PhoneStateListener mPhoneStateListener; // kerry - for dcm req
    boolean csActive = false;
    boolean fromSettings = false;
    PreferenceScreen wdPref = null;
    boolean backup_check_value;
    // kerry end

/* LGE_CHANGE_S : FIX_AIRPLANE_MODE_DEFECT
** 2012.03.24, kyemyung.an@lge.com
*/
    static final String TAG = "Airplanemodeenabler";
/* LGE_CHANGE_E : FIX_AIRPLANE_MODE_DEFECT */

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_SERVICE_STATE_CHANGED:
                Log.d (TAG, "----------------EVENT_SERVICE_STATE_CHANGED------------");
                 Timer timer = new Timer();
                 TimerTask task = new TimerTask() {
                     public void run() {
                         Message m = obtainMessage(EVENT_AIRPLANE_MODE_UI_BLOCK_TIMER);
                         m.sendToTarget();
                     }
                 };
                 timer.schedule(task, 3000);
                 break;
            case EVENT_AIRPLANE_MODE_UI_BLOCK_TIMER:
                Log.d (TAG, "----------------EVENT_AIRPLANE_MODE_UI_BLOCK_TIMER----------------");
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {
                    if (adapter.getState() == BluetoothAdapter.STATE_TURNING_ON
                        || adapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                        mIsStateChanging = true;
                        Log.d (TAG, "BT mIsStateChanging is true ---- return ");
                        return;
                    }
                }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
                serviceState = mPhoneStateReceiver.getServiceState();
                Log.i ("Airplanemodeenabler", "serviceState.getState() = " + serviceState.getState());
                if (global_airplane == true) {
                    Log.d (TAG, "if global_airplane = true");                    
                    if ((serviceState.getState() == ServiceState.STATE_POWER_OFF) || (Utils.isWifiOnly(mContext)) ) {
                       Log.d (TAG, "global_airplane = true , STATE_POWER_OFF");
                       onAirplaneModeChanged();
                       airplanemode_on_off = false;
                    }
                } else if (global_airplane == false) {
                    Log.d (TAG, "else if global_airplane = false");
                    if ((serviceState.getState() == ServiceState.STATE_IN_SERVICE)
                            || (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE)
                            || (serviceState.getState() == ServiceState.STATE_EMERGENCY_ONLY)) {
                        Log.d (TAG, "ServiceState = " + serviceState.getState() );
                        onAirplaneModeChanged();
                        airplanemode_on_off = false;
                    }
                }
                mIsStateChanging = false;
                break;

                default:
                    break;
            }
        }
    };

    private ContentObserver mAirplaneModeObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean airplaneModeEnabled = isAirplaneModeOn(mContext);

            if ( "KDDI".equals(Config.getOperator()) && Utils.isUI_4_1_model(mContext) ) {
                mTwoStatePref.setEnabled(false);
                mTwoStatePref.setSummary(airplaneModeEnabled ?
                        R.string.sp_airplane_mode_on_summary_NORMAL 
                        : R.string.sp_airplane_mode_off_summary_NORMAL);
            }
            mTwoStatePref.setChecked(airplaneModeEnabled);
            Log.d (TAG, "mAirplaneModeObserver - fromSettings = " + fromSettings);
            
            if (Utils.isWifiOnly(mContext) && fromSettings) {
                mPhoneStateReceiver.registerIntent();
            }

            if ( "KDDI".equals(Config.getOperator()) && Utils.isUI_4_1_model(mContext) ) {
                global_airplane = airplaneModeEnabled;
                airplanemode_on_off = true;
                
            } else {
                if (false == fromSettings) {
                    onAirplaneModeChanged();
                    airplanemode_on_off = false;
                } else {
                    fromSettings = false;
                }
            }
            Log.d (TAG, "mAirplaneModeObserver - airplanemode_on_off =" + airplanemode_on_off);
        }
    };

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
    private boolean mIsStateChanging = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Log.d (TAG, "BluetoothAdapter.ACTION_STATE_CHANGED");
                if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                    Log.d ("Airplanemodeenabler", "BT status is STATE_ON/STATE_OFF");

                    mIsStateChanging = false;
                    // rebestm
                    mPhoneStateReceiver.registerIntent();
                    global_airplane = isAirplaneModeOn(mContext);
                } else if (state == BluetoothAdapter.STATE_TURNING_ON || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    Log.d ("Airplanemodeenabler", "BT status is STATE_TURNING_ON/STATE_TURNING_OFF");
                    Log.d ("Airplanemodeenabler", "fromSettings = " + fromSettings);
                    if (fromSettings) {
                        mIsStateChanging = true;
                        mTwoStatePref.setEnabled(false);

                        if (null != wdPref) {
                            wdPref.setEnabled(false);
                        }
                    }
                }
            }
        }
    };
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

    private void AirplaneModeEnableInit(PreferenceScreen pref) {
        mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);

        wdPref = pref;

        // kerry - for dcm req start
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if ((state != TelephonyManager.CALL_STATE_IDLE)  || getAirplaneModeVoLTE() || isVTCalling()) { //shlee1219.lee
                    csActive = true;
                } else {
                    csActive = false;
                }
            }

            @Override
            public void onDataActivity(int direction) {
            }
        };

        mPhone = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        // kerry - for dcm req end
    }

    public AirplaneModeEnabler(Context context, CheckBoxPreference airplaneModeCheckBoxPreference, PreferenceScreen pref) {
        mContext = context;
        mTwoStatePref = (CheckBoxPreference)airplaneModeCheckBoxPreference;
        airplaneModeCheckBoxPreference.setPersistent(false);
        AirplaneModeEnableInit(pref);
    }

    public AirplaneModeEnabler(Context context, SettingsSwitchPreference airplaneModeSwitchPreference, PreferenceScreen pref) {
        mContext = context;
        mTwoStatePref = (SettingsSwitchPreference)airplaneModeSwitchPreference;
        airplaneModeSwitchPreference.setPersistent(false);
        AirplaneModeEnableInit(pref);
    }

    public void resume() {
        Log.d (TAG, "Airplane resume -->");

//ask130402 :: +
        Utils.set_TelephonyListener(mPhone, mPhoneStateListener);
//ask130402 :: -

/* LGE_CHANGE_S : FIX_AIRPLANE_MODE_DEFECT
** 2012.03.24, kyemyung.an@lge.com
** OFFICIAL_EVENT/Mobile_12/LGP705g_RGS_CANADA_ROGERS_QE/ Defect TD ID # 31697
** Airplane mode check is possible, but not activation function
** 3. Tester's Action : APPs - Settings - More... - Ariplane mode - On - Attention Pop-up - Home key - Indicartor down - Airplane off - Indicartor up
** - Apps - Settings - APPs - Settings - More... - Ariplane mode - On - Mobile net works - Home key - APPs - Settings - More...
** - Ariplane mode - On
** 4. Detailed Symptom (ENG.) : Airplane mode check is possible, but not activation function
** Repository : None */
        boolean bIsAirplaneModeOn = isAirplaneModeOn(mContext);

        Log.d(TAG, "resume() bIsAirplaneModeOn=" + bIsAirplaneModeOn + ", global_airplane="
            + global_airplane + ", airplanemode_on_off=" + airplanemode_on_off);

// [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-02, QE#108373 : Fixed Airplane mode check/uncheck with BT on/off issue.
        if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON ||
                BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_OFF) {
            mIsStateChanging = false;
        }
// [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-02, QE#108373 : Fixed Airplane mode check/uncheck with BT on/off issue.

        if (bIsAirplaneModeOn != global_airplane)
        {
            global_airplane = bIsAirplaneModeOn;
            airplanemode_on_off = false;
        }
/* LGE_CHANGE_E : FIX_AIRPLANE_MODE_DEFECT */

        if (false == Config.isVZWAdminDisabled(mContext)) {
            mTwoStatePref.setEnabled(true);
        }

        if (null != wdPref) {
            wdPref.setEnabled(true);
        }

        mTwoStatePref.setChecked(bIsAirplaneModeOn);

        mPhoneStateReceiver.registerIntent();

        if (Utils.isSupportedVolte(mContext)) {
            Utils.setIsInUseVoLTE(isUseInVoLTE());
            Utils.registerUCStateObserve(mContext);
        }

        mTwoStatePref.setOnPreferenceChangeListener(this);
        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),
                true, mAirplaneModeObserver);
        if ((mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE ) || getAirplaneModeVoLTE() || isVTCalling()) { //shlee1219.lee

            csActive = true;
        } else {
            csActive = false;
        } // kerry - qm req

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, intentFilter);
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary(null, mContext, mTwoStatePref);
        }
        // LGMDM_END
        
        if ("KDDI".equals(Config.getOperator()) && airplanemode_on_off) {
            Log.d (TAG, "Update UI");
            mTwoStatePref.setChecked(global_airplane);
            mTwoStatePref.setSummary(global_airplane ? R.string.sp_airplane_mode_on_summary_NORMAL
                    : R.string.sp_airplane_mode_off_summary_NORMAL);
            mTwoStatePref.setEnabled(false);
        } else {
            Log.d (TAG, "Not Update UI");
        }
        Log.d (TAG, "<-- Airplane resume");
    }

    public void pause() {
//ask130402 :: +
        Utils.release_TelephonyListener(mPhone, mPhoneStateListener);
//ask130402 :: -

        try {
            mPhoneStateReceiver.unregisterIntent();
        } catch (IllegalArgumentException e) {
            Log.e (TAG, "IllegalArgumentException = " + e);
        }
        mTwoStatePref.setOnPreferenceChangeListener(null);
        mContext.getContentResolver().unregisterContentObserver(
                mAirplaneModeObserver);

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {}
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
        if (Utils.isSupportedVolte(mContext)) {
            Utils.unregisterObserver(mContext);
    	}
    }

    public static boolean isAirplaneModeOn(Context context) {
        int bReturn = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0);

        if ( bReturn == 1 ) {
            Log.d ("Airplanemodeenabler", "isAirplaneMode -> On");
            return true;
        } else {
            Log.d ("Airplanemodeenabler", "isAirplaneMode -> off");
            return false;
        }
    }

    private void setAirplaneModeOn(boolean enabling) {
        boolean putExtraIsPopup = true;
        // Change the system setting
        Log.d (TAG, "setAirplaneModeOn --> ");
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary(null, mContext, mTwoStatePref)) {
                Log.d ("AirplaneModeEnabler", "setAirplaneModeOn() is blocked by MDM");
                return;
            }
        }
        // LGMDM_END
        Log.d (TAG, "enabling: " + enabling); //shlee1219.lee
        mTwoStatePref.setEnabled(false);

        if(null != wdPref)
            wdPref.setEnabled(false);

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        if(enabling && BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON) {
            mIsStateChanging = true;
        }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

        // DGMS MC-05416-2: Can't make a call while HDR or Airplane mode in VS740 START
        //myoungsuk.kim@lge.com[START]
        // kiseok.son@lge.com 20130128 VZW ODR issue - start
        String airplaneText = "";

        if ("VZW".equals(Config.getOperator())) {
            airplaneText = mContext
                    .getString(R.string.sp_airplane_mode_on_common_JB_VZW_NORMAL);
        } else if ("LGU".equals(Config.getOperator())) {
            airplaneText = mContext
                    .getString(R.string.sp_airplane_mode_on_LGU_NORMAL);
        } else {
            airplaneText = mContext
                    .getString(R.string.settings_airplane_mode_popup);
        }
        // kiseok.son@lge.com 20130128 VZW ODR issue - end

        Log.d (TAG, "ro.airplane.phoneapp -> "
                            + SystemProperties.getInt("ro.airplane.phoneapp", 0));

        if ((SystemProperties.getInt("ro.airplane.phoneapp", 0) != 1)
                && !(Utils.isWifiOnly(mContext))) {
            if (enabling && (Utils.getAirplaneFlow(mContext) == 1)) {
                //ask130206 [F240K] Fixed td 196024
                AlertDialog mAirplaneModeDialog;
                mAirplaneModeDialog = new AlertDialog.Builder(mContext)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(airplaneText)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, null).create();
                mAirplaneModeDialog.show();
                Log.i (TAG, "Settings show airplane mode popup!!! - enabling = " + enabling );
            }
        } else {
            Log.i (TAG, "ro.airplane.phoneapp is TRUE!!! - enabling = " + enabling );
        }
        //myoungsuk.kim@lge.com[END]
        // DGMS MC-05416-2: Can't make a call while HDR or Airplane mode in VS740 END

        mTwoStatePref.setEnabled(false);

        if(null != wdPref)
            wdPref.setEnabled(false);

        mTwoStatePref.setSummary(enabling ? R.string.sp_airplane_mode_on_summary_NORMAL
                : R.string.sp_airplane_mode_off_summary_NORMAL);

        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, enabling ? 1 : 0);

        // Update the UI to reflect system setting
        mTwoStatePref.setChecked(enabling);

        if (Utils.getAirplaneFlow(mContext) == 2) {
            putExtraIsPopup = false;
            Log.i (TAG, "UI_4_1 is putExtraIsPopup = false!!!");
        }
                
        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        intent.putExtra("ispopup", putExtraIsPopup);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d (TAG, "<-- setAirplaneModeOn");
    }

    /**
     * Called when we've received confirmation that the airplane mode was set.
     * TODO: We update the checkbox summary when we get notified that mobile
     * radio is powered up/down. We should not have dependency on one radio
     * alone. We need to do the following: - handle the case of wifi/bluetooth
     * failures - mobile does not send failure notification, fail on timeout.
     */
    private void onAirplaneModeChanged() {
        Log.d (TAG, "onAirplaneModeChanged -->");
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);

        mTwoStatePref.setChecked(airplaneModeEnabled);
        if (Utils.isWifiOnly(mContext) && !Utils.isTablet()) {
            mTwoStatePref.setSummary(airplaneModeEnabled ? null : Utils.getResources().getString(R.string.sp_airplane_mode_summary_NORMAL_jb_plus_wifi_only));
        } else {
            mTwoStatePref.setSummary(airplaneModeEnabled ? null : Utils.getResources().getString(R.string.sp_airplane_mode_summary_NORMAL_jb_plus));
        }

        if (false == Config.isVZWAdminDisabled(mContext))
            mTwoStatePref.setEnabled(true);

        if (null != wdPref) {
            wdPref.setEnabled(true);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
           MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary(null, mContext, mTwoStatePref);
        }
        // LGMDM_END
        Log.d (TAG, "UI update Complete");
        Log.d (TAG, "airplanemode_on_off = " + airplanemode_on_off);
        Log.d (TAG, "<-- onAirplaneModeChanged");
    }

    public void onAirplaneWarningPopup(boolean newValue) {
        backup_check_value = newValue;
        Builder AlertDlg = new AlertDialog.Builder(mContext);

        if (Utils.isWifiOnly(mContext) || Utils.isTablet()) {
            AlertDlg.setTitle(mContext.getResources()
                    .getString(R.string.settings_airplane_warning_popup_title));
            AlertDlg.setMessage(mContext
                    .getString(R.string.settings_airplane_popup_vzw_tablet));
        } else {
            AlertDlg.setTitle(mContext.getResources()
                    .getString(R.string.settings_airplane_warning_popup_title));
            AlertDlg.setMessage(mContext
                    .getString(R.string.settings_airplane_warning_popup_summary));
        }

        AlertDlg.setPositiveButton(android.R.string.ok
                , new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               onAirplaneWarningOK(backup_check_value);
           }
       }).setNegativeButton(android.R.string.cancel
               , new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
               dialog.dismiss();
           }
       }).setOnCancelListener(new DialogInterface.OnCancelListener() {
           public void onCancel(DialogInterface dialog) {
           }
       }).show();
   }
    
    public void onAirplaneWarningOK(boolean newValue) {
        Log.d (TAG, "onAirplaneWarningOK --> newValue = " + newValue);

        if (!airplanemode_on_off) {
            global_airplane = (Boolean) newValue;
            Log.d ("Airplanemodeenabler", "airplanemode_on_off = " + airplanemode_on_off);
            Log.d ("Airplanemodeenabler", "global_airplane = " + global_airplane);
            setAirplaneModeOn((Boolean)newValue);
            airplanemode_on_off = true;
            mTwoStatePref.setChecked((Boolean)newValue);
        }
        
        Log.d (TAG, "<- onAirplaneWarningOK");
    }
    /**
     * Called when someone clicks on the checkbox preference.
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d (TAG, "onPreferenceChange -->");

        fromSettings = true;
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary(null, mContext, mTwoStatePref)) {
            Log.d("AirplaneModeEnabler", "LGMDM Block");
            return false;
        }
        // LGMDM_END
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        if(mIsStateChanging) {
            Log.d("AirplaneModeEnabler", "[BTUI] state is changing... return for a while");
            return false;
        }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
        mPhoneStateReceiver.getServiceState();
        if (Boolean.parseBoolean(SystemProperties
                .get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
            Log.d (TAG, "In ECM mode, do not update database at this point");
        } else {
                 Log.d (TAG, "csActive =" + csActive);
            if(true == csActive) {

                 if (false == Config.isVZWAdminDisabled(mContext))
                     mTwoStatePref.setEnabled(true);

                 if(null != wdPref)
                     wdPref.setEnabled(true);

                mTwoStatePref.setChecked(false);

                if (SystemProperties.getInt("ro.airplane.phoneapp",0) != 1) {
                    Log.d(TAG, "onPreferenceChange() setting app toast." ); //shlee1219.lee
                    Toast.makeText(mContext, R.string.sp_not_available_during_a_call_NORMAL, Toast.LENGTH_SHORT).show();
                }else{
                      Log.d(TAG, "onPreferenceChange() phone app toast." ); //shlee1219.lee
                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, false ? 1 : 0);

                    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    intent.putExtra("state", false);
                    mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }

                return true;
            } // kerry - qm req

            if ( Utils.getAirplaneFlow(mContext) == 1 || !(Boolean)newValue) {
                if (!airplanemode_on_off) {
                    global_airplane = (Boolean) newValue;
                    Log.d ("Airplanemodeenabler", "airplanemode_on_off = " + airplanemode_on_off);
                    Log.d ("Airplanemodeenabler", "global_airplane = " + global_airplane);
                    setAirplaneModeOn((Boolean) newValue);
                    airplanemode_on_off = true;
                }
            } else if ( Utils.getAirplaneFlow(mContext) == 2) {
                onAirplaneWarningPopup((Boolean) newValue);
                Log.d (TAG, "popup <- onPreferenceChange");
                return false;
            }
        }
        Log.d (TAG, "<- onPreferenceChange");
        return true;
    }

    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            // update database based on the current checkbox state
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            // update summary
            onAirplaneModeChanged();
        }
    }

    public void updatePreferences() {
        if (Utils.isMonkeyRunning()) {
            mTwoStatePref.setEnabled(false);

            if(null != wdPref)
                wdPref.setEnabled(false);

        } else {
            if (false == Config.isVZWAdminDisabled(mContext))
                mTwoStatePref.setEnabled(true);

            if(null != wdPref)
                wdPref.setEnabled(true);

            mTwoStatePref.setChecked(isAirplaneModeOn(mContext));
        }
    }

    public boolean isVTCalling() { //shlee1219.lee
        boolean result = false;
        int vtCallState = 0;
        ITelephony telephonyService = ITelephony.Stub.asInterface(
                        ServiceManager.checkService(Context.TELEPHONY_SERVICE));
        if (telephonyService != null) {
            try {
                vtCallState = telephonyService.getCallState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (vtCallState >= 100) {
            result = true;
        } else {
            result = false;
        }
        Log.d (TAG, "isVTCalling return value = " + result);
        return result;
    }

	public boolean getAirplaneModeVoLTE() {
       if (!Utils.isSupportedVolte(mContext)) {
         return false;
       } else {
         if (Utils.getIsInUseVoLTE()) {
           return true;
         } else {
           return false;
         }
       }
    }

	private boolean isUseInVoLTE() {
        if (!Utils.isSupportedVolte(mContext)) {
           return false;
	    } else {
           if (Utils.getCurrentVoLTEStatus(mContext)) {
              return true;
           } else {
              return false;
           }
	    }
	}
}
