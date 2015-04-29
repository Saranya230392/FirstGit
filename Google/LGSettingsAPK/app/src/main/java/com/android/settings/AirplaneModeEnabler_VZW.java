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

// add the file : kiseok.son 20121025: Add the airplane mode menu in setting screen for VZW.
package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;

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
import android.widget.CompoundButton;
import android.provider.Settings;

//shlee1219.lee START
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.internal.telephony.ITelephony;
//shlee1219.lee END
//ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
import com.android.settings.lgesetting.Config.Config;
import android.provider.Telephony;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
public class AirplaneModeEnabler_VZW implements CompoundButton.OnCheckedChangeListener {

    private final Context mContext;

    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private static boolean airplanemode_on_off = false;
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
    private Switch mSwitch;
    boolean fromTouch = false;
    // kerry end
    
    boolean backup_check_value;
    int mDoNotShowDataOnPopupChecked;
    
    private static final int BLUETOOTH_HANDLER_OFF = 0;
    private static final int BLUETOOTH_HANDLER_ON = 1;
    private static final int BLUETOOTH_HANDLER_PENDING = 2;
    public static final String ACTION_UPDATE_MOBILE_NETWORKS = "ACTION_UPDATE_MOBILE_NETWORKS";
    public static final Uri IMS_UCS_URI = Uri.parse("content://com.lge.ims.provider.uc/ucstate");
/* LGE_CHANGE_S : FIX_AIRPLANE_MODE_DEFECT
** 2012.03.24, kyemyung.an@lge.com
*/
    static final String TAG = "Airplanemodeenabler_other";
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
                if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_TURNING_ON
                    || BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    mIsStateChanging = true;
                    Log.d (TAG, "BT mIsStateChanging is true ---- return ");
                    return;
                }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
                serviceState = mPhoneStateReceiver.getServiceState();
                Log.i (TAG, "serviceState.getState = " + serviceState.getState());
                Log.d (TAG, "airplanemode_on_off = " + airplanemode_on_off);
                
                if (global_airplane == true) {
                   if ((serviceState.getState() == ServiceState.STATE_POWER_OFF) || (Utils.isWifiOnly(mContext)) ) {
                        Log.i (TAG, "global_airplane = true , ServiceState = STATE_POWER_OFF");
                        onAirplaneModeChanged();
                        airplanemode_on_off = false;
                   }
                } else if (global_airplane == false) {
                    if ((serviceState.getState() == ServiceState.STATE_IN_SERVICE)
                            || (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE)
                            || (serviceState.getState() == ServiceState.STATE_POWER_OFF) 
                            || (serviceState.getState() == ServiceState.STATE_EMERGENCY_ONLY)) {
                        Log.i (TAG, "global_airplane = false, ServiceState = " + serviceState.getState());
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

            mSwitch.setChecked(airplaneModeEnabled);
            Log.d(TAG, "mAirplaneModeObserver - fromSettings = " + fromSettings);

            if ("TMO".equals(Config.getOperator()) && "US".equals(Config.getCountry())) {
                mPhoneStateReceiver.registerIntent();
            }

            if (false == fromSettings) {
                onAirplaneModeChanged();
                airplanemode_on_off = false;
            } else {
                fromSettings = false;
            }

            Log.d (TAG, "mAirplaneModeObserver - airplanemode_on_off = "
                    + airplanemode_on_off);
        }
    };

    //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
    private ContentObserver mApnChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {

            boolean value = Config.isVZWAdminDisabled(mContext);
            if (value) {
                mSwitch.setEnabled(!value);
                Log.d (TAG, "mApnChangeObserver - mSwitch.setEnabled(" + !value
                        + ")");
            }


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
                    mPhoneStateReceiver.registerIntent();
                    global_airplane = isAirplaneModeOn(mContext);
                } else if (state == BluetoothAdapter.STATE_TURNING_ON || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    mIsStateChanging = true;
                }
            }
        }
    };
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

    public AirplaneModeEnabler_VZW(Context context, Switch switch_, PreferenceScreen pref) {
        mContext = context;
        mSwitch = switch_;

        mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);

        wdPref = pref;
        // kerry - for dcm req start
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if ((state != TelephonyManager.CALL_STATE_IDLE) || isVTCalling()) { //shlee1219.lee
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
// rebestm - exception Airplane mode for Atable_VZW	        
        if ( Utils.isTablet()) {
            mSwitch.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                        if (true == csActive) {
                            Toast.makeText(mContext, R.string.sp_not_available_during_a_call_NORMAL
                                        , Toast.LENGTH_SHORT).show();
                        }   
                    }
                    
                    if (arg1.getAction() == MotionEvent.ACTION_UP) {
                        fromTouch = true;
                    }
                    Log.d(TAG, "setOnTouchListener , fromTouch = " + fromTouch);
                    // TODO Auto-generated method stub
                    return false;
                }
            });
        }
    }

    public void setSwitch(Switch switch_) {
        Log.d (TAG, "setSwitch -->" );
// rebestm - exception Airplane mode for Atable_VZW
        if ( (mSwitch == switch_) && !Utils.isTablet() ) {
            Log.d(TAG, " setSwitch end return" );
            return;
        }
        if ( !Utils.isTablet()) {
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch = switch_;
            mSwitch.setOnCheckedChangeListener(this);
        }
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "setOnClickListener");
            }
        });

        mSwitch.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (true == csActive) {
                        Toast.makeText(mContext, R.string.sp_not_available_during_a_call_NORMAL, Toast.LENGTH_SHORT).show();
                    }
                }

                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    fromTouch = true;
                }
                Log.d(TAG, "setOnTouchListener , fromTouch = " + fromTouch);
                // TODO Auto-generated method stub
                return false;
            }
        });

        //ask130206 :: if it was changing the airplane mode, set to disable the switch.
        if (airplanemode_on_off) {
            mSwitch.setEnabled(false);
            Log.d(TAG, " setSwitch airplanemode_on_off = true, mSwitch.setEnabled = false" );
        }

        int isEnabled = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);

        if (1 == isEnabled) {
            mSwitch.setChecked(true);
        }
        else if (0 == isEnabled) {
            mSwitch.setChecked(false);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_VZW(null, mContext, mSwitch)) {
                Log.d (TAG, "MDM Block");
            }
        }
        // LGMDM_END

        //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
        boolean value = Config.isVZWAdminDisabled(mContext);
        if (value) {
            mSwitch.setEnabled(!value);
        }
        Log.d (TAG, "<-- setSwitch");

    }

    public void setSwitchChecked(boolean checked) {
        Log.d (TAG, "setSwitchChecked(checked) = " + checked);
        if (mSwitch.isEnabled() == true) {
            fromTouch = true;
            mSwitch.setChecked(!checked);
        } else {
            fromTouch = false;
        }
    }

    public void resume() {
        Log.d (TAG, "Airplane reusme -->");
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
            + global_airplane + ", airplanemode_on_off=" + airplanemode_on_off + ", fromTouch = "+fromTouch);

// [START_LGE_SETTINGS], ADD, kiseok.son, 2012-07-02, QE#108373 : Fixed Airplane mode check/uncheck with BT on/off issue.
        if ((BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON) ||
            (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_OFF)) {
            mIsStateChanging = false;
        }
// [END_LGE_SETTINGS], ADD, kiseok.son, 2012-07-02, QE#108373 : Fixed Airplane mode check/uncheck with BT on/off issue.

        if (bIsAirplaneModeOn != global_airplane)
        {
            global_airplane = bIsAirplaneModeOn;
            airplanemode_on_off = false;
        }
/* LGE_CHANGE_E : FIX_AIRPLANE_MODE_DEFECT */

        //ask130204 :: && airplanemode_on_off == false
        if (false == csActive && airplanemode_on_off == false ) {
            mSwitch.setEnabled(true);
            Log.d(TAG, " resume mSwitch.setEnabled = true" );
        }

        if (null != wdPref) {
            wdPref.setEnabled(true);
        }

        if (false == fromTouch) {
            mSwitch.setChecked(isAirplaneModeOn(mContext));
        } else {
            fromTouch = false;
        }

        mPhoneStateReceiver.registerIntent();
        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),
                true, mAirplaneModeObserver);

        //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
        mContext.getContentResolver().registerContentObserver(
                Telephony.Carriers.CONTENT_URI,
                true, mApnChangeObserver);

        if ((mPhone.getCallState() != TelephonyManager.CALL_STATE_IDLE) || isVTCalling()) { //shlee1219.lee
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
            MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_VZW(null, mContext, mSwitch);
        }
        // LGMDM_END

        //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
        boolean value = Config.isVZWAdminDisabled(mContext);
        if (value) {
            mSwitch.setEnabled(!value);
        }
// rebestm - exception Airplane mode for Atable_VZW			
        if (Utils.isTablet()) {
           mSwitch.setOnCheckedChangeListener(this);
        }
        Log.d (TAG, "<-- Airplane reusme");

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

        mContext.getContentResolver().unregisterContentObserver(
                mAirplaneModeObserver);

        //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
        mContext.getContentResolver().unregisterContentObserver(
                mApnChangeObserver);


//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            ;
        }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

    }

    public static boolean isAirplaneModeOn(Context context) {
        int bReturn = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0);

        if ( bReturn == 1 ) {
            Log.d (TAG, "isAirplaneMode On");
            return true;
        } else {
            Log.d (TAG, "isAirplaneMode off");
            return false;
        }
    }

    private void setAirplaneModeOn(boolean enabling) {
        // Change the system setting
        Log.d (TAG, "setAirplaneModeOn --> ");
        // LGMDM [a1-mdm-dev@lge.com][ID_MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_VZW(null, mContext, mSwitch)) {
                Log.d("MDM", "setAirplaneModeOn() is blocked by MDM");
                return;
            }
        }
        // LGMDM_END
        Log.d(TAG, "enabling = " + enabling); //shlee1219.lee
        mSwitch.setEnabled(false);

        if (null != wdPref) {
            wdPref.setEnabled(false);
        }

//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        if (enabling && (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON)) {
            mIsStateChanging = true;
        }
//LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]

        // DGMS MC-05416-2: Can't make a call while HDR or Airplane mode in VS740 START
        //myoungsuk.kim@lge.com[START]
        // kiseok.son@lge.com 20130128 VZW ODR issue - start
        String airplaneText = "";
        if ("VZW".equals(Config.getOperator())) {
            if ( Utils.isTablet()) {
                airplaneText = mContext.getString(
                        R.string.settings_airplane_popup_vzw_tablet);
            } else {
                airplaneText = mContext.getString(
                        R.string.sp_airplane_mode_on_common_JB_VZW_NORMAL);
            }
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
                //fixed [F240K] td #196024
                AlertDialog mAirplaneModeDialog;
                mAirplaneModeDialog = new AlertDialog.Builder(mContext)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(airplaneText)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, null).create();
                if (Utils.isUI_4_1_model(mContext)) {
                    mAirplaneModeDialog
                        .setTitle(R.string.settings_airplane_warning_popup_title);
                } else {
                    mAirplaneModeDialog.setTitle(android.R.string.dialog_alert_title);
                }
                mAirplaneModeDialog.show();
                Log.i (TAG, "Settings show airplane mode popup!!! - enabling = " + enabling );
            }
        } else {
            Log.i (TAG, "ro.airplane.phoneapp is TRUE!!! - enabling = " + enabling );
        }
        //myoungsuk.kim@lge.com[END]
        // DGMS MC-05416-2: Can't make a call while HDR or Airplane mode in VS740 END

        mSwitch.setEnabled(false);

        if (null != wdPref) {
            wdPref.setEnabled(false);
        }

        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, enabling ? 1 : 0);

        // Update the UI to reflect system setting
        mSwitch.setChecked(enabling);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
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
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);
        Log.d (TAG, "onAirplaneModeChanged -->");
        mSwitch.setChecked(airplaneModeEnabled);

        if (false == csActive) {
            mSwitch.setEnabled(true);
        }

        //ask130111 :: if it was to disable the apn2, disbled the airplane mode menu item.
        boolean value = Config.isVZWAdminDisabled(mContext);
        if (value) {
            mSwitch.setEnabled(!value);
        }



        if (null != wdPref) {
            wdPref.setEnabled(true);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID_MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_VZW(null, mContext, mSwitch);
        }
        // LGMDM_END
        if ( Utils.isTablet()) {
            Intent intent = new Intent(ACTION_UPDATE_MOBILE_NETWORKS);
            mContext.sendBroadcast(intent);
        }
        Log.d (TAG, "UI update Complete");
        Log.d (TAG, "airplanemode_on_off = " + airplanemode_on_off);
        Log.d (TAG, "<-- onAirplaneModeChanged");
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
        
        if ("ATT".equals(Config.getOperator())) {
            if (Utils.isTablet()) {
                AlertDlg.setTitle(mContext.getResources()
                    .getString(R.string.settings_airplane_warning_popup_title_att3));
            } else {
                AlertDlg.setTitle(mContext.getResources()
                        .getString(R.string.settings_airplane_warning_popup_title_att2));
            }
            AlertDlg.setMessage(mContext
                    .getString(R.string.settings_airplnae_popup_att));
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
               mSwitch.setChecked(!backup_check_value);
           }
       }).setOnCancelListener(new DialogInterface.OnCancelListener() {
           public void onCancel(DialogInterface dialog) {
               mSwitch.setChecked(!backup_check_value);
           }
       }).show();
   }
    
    private void onAirplaneWarningPopup_VZW(boolean newValue) {
        backup_check_value = newValue;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                com.lge.R.style.Theme_LGE_White_Dialog_Alert);

        builder.setTitle(mContext.getResources()
                .getString(R.string.settings_airplane_warning_popup_title));

        LayoutInflater factory = LayoutInflater.from(mContext);
        View inpuView = factory.inflate(R.layout.airplanemode_do_not_show, null);
        CheckBox mDataCheckbox = (CheckBox)inpuView.findViewById(R.id.do_not_show_check);
        TextView  txtPopup = (TextView)inpuView.findViewById(R.id.popup_text);
        
        
        if (!isVoLTESupported()) {
            txtPopup.setText(R.string.settings_airplnae_popup_com_vzw);
        } else {
            txtPopup.setText(R.string.settings_airplnae_popup_volte_vzw);
        }
        
        mDataCheckbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });
        mDataCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        if (isChecked) {
                            mDoNotShowDataOnPopupChecked = 1;
                        } else {
                            mDoNotShowDataOnPopupChecked = 0;
                        }
                    }
                });
        builder.setPositiveButton(android.R.string.ok,
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setDoNotShowCheckBox(mDoNotShowDataOnPopupChecked);
                        onAirplaneWarningOK(backup_check_value);
                    }
                });
        
        builder.setNegativeButton(android.R.string.cancel,
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSwitch.setChecked(!backup_check_value);
                        dialog.dismiss();
                        mDoNotShowDataOnPopupChecked = 0;
                    }
                });
        builder.setOnCancelListener(
                new Dialog.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mSwitch.setChecked(!backup_check_value);
                        mDoNotShowDataOnPopupChecked = 0;
                    }
                });
        
        builder.setView(inpuView);

        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    public void setDoNotShowCheckBox(int enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                "airplane_mode_dialog_do_not_show_this_again", enabled);
    }
    
    public int getDoNotShowCheckBox() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                "airplane_mode_dialog_do_not_show_this_again", 0);
    }
        
    public boolean isVoLTESupported() {
        boolean value = false;
        
        Cursor cursor = mContext.getContentResolver().query(IMS_UCS_URI, null, null, null, null);
            
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int provision_index = cursor.getColumnIndex("voip_enabled");
                String provision_data = cursor.getString(provision_index);
                if ("1".equals(provision_data)) {
                    value = true;
                } else if ("2".equals(provision_data)) {
                    value = false;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d (TAG, "isVoLTESupported() value = " + value);

        return value;
    }

    public void onAirplaneWarningOK(boolean newValue) {
        Log.d (TAG, "onAirplaneWarningOK --> newValue = " + newValue);

        if (!airplanemode_on_off) {
            global_airplane = (Boolean) newValue;
            Log.d ("Airplanemodeenabler", "airplanemode_on_off = " + airplanemode_on_off);
            Log.d ("Airplanemodeenabler", "global_airplane = " + global_airplane);
            setAirplaneModeOn((Boolean) newValue);
            airplanemode_on_off = true;
            mSwitch.setChecked((Boolean) newValue);
        }
        
        Log.d (TAG, "<- onAirplaneWarningOK");
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        Log.d (TAG, "onCheckedChanged -->" );
        Log.d (TAG, "fromTouch = " + fromTouch );
        // LGMDM [a1-mdm-dev@lge.com][ID_MDM-191][ID-MDM-424]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_VZW(null, mContext, mSwitch)) {
                Log.d(TAG, "onCheckedChanged LGMDM Block");
                return;
            }
        }
        // LGMDM_END

        if (false == fromTouch) {
            mSwitch.setChecked(isAirplaneModeOn(mContext));
            return;
        } else {
            fromTouch = false;
        }

        // TODO Auto-generated method stub
        fromSettings = true;

        //LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [s]
        if (mIsStateChanging || (getBTButtonStatus() == BLUETOOTH_HANDLER_PENDING)) {
            Log.d(TAG, "[BTUI] state is changing... return for a while");
            Toast.makeText(mContext, mContext.getString(R.string.sp_quicksettings_airplane_suspend_bt_NORMAL), Toast.LENGTH_SHORT).show();

            return;
        }
        //LG_BTUI [kukdong.bae@lge.com]: Block Airplane mode during BT state transition [e]
        mPhoneStateReceiver.getServiceState();
        if (Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
            Log.d(TAG, "Send intent mContext.startActivity(new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null));");
            mSwitch.setChecked(!(Boolean)arg1);
            Intent intent = new Intent("AIRPALNE_MODE_ENABLER_CHANGED_IN_ECM");
            mContext.sendBroadcast(intent);            
        } else {
            if (true == csActive) {
                if (null != wdPref) {
                    wdPref.setEnabled(true);
                }

                mSwitch.setChecked(false);
                Log.d(TAG, "onPreferenceChange() setting app toast." ); //shlee1219.lee
                Toast.makeText(mContext, R.string.sp_not_available_during_a_call_NORMAL, Toast.LENGTH_SHORT).show();

                return;
            } // kerry - qm req
            
            if ( Utils.getAirplaneFlow(mContext) == 1 || !(Boolean)arg1) { // When airplane off
                if (!airplanemode_on_off) {
                    global_airplane = (Boolean)arg1;
                    Log.d(TAG, "airplanemode_on_off = " + airplanemode_on_off);
                    Log.d(TAG, "global_airplane = " + global_airplane);
                    setAirplaneModeOn((Boolean)arg1);
                    airplanemode_on_off = true;
                }
            } else if ( Utils.getAirplaneFlow(mContext) == 2) {
                mSwitch.setChecked(!(Boolean)arg1);
                if ("VZW".equals(Config.getOperator()))  {
                    Log.i (TAG, "getDoNotShowCheckBox() = " + getDoNotShowCheckBox());
                    if ( getDoNotShowCheckBox() == 1) {
                        backup_check_value = (Boolean)arg1;
                        onAirplaneWarningOK(backup_check_value);
                    } else {
                        onAirplaneWarningPopup_VZW((Boolean)arg1);
                    }
                } else {
                    onAirplaneWarningPopup((Boolean)arg1);
                }

                Log.d (TAG, "popup <- onCheckedChanged");
                return;
            }
        }
        Log.d (TAG, "<-- onCheckedChanged" );
        return;
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
        Log.d(TAG, "isVTCalling() return: " + result);
        return result;
    }

    private int getBTButtonStatus() {
        int status = BLUETOOTH_HANDLER_OFF;
        int getBTstate = BluetoothAdapter.getDefaultAdapter().getState();

        switch (getBTstate) {
        case BluetoothAdapter.STATE_ON:
            status = BLUETOOTH_HANDLER_ON;
            break;
        case BluetoothAdapter.STATE_TURNING_ON:
        case BluetoothAdapter.STATE_TURNING_OFF:
            status = BLUETOOTH_HANDLER_PENDING;
            break;
        case BluetoothAdapter.STATE_OFF:
        default:
            status = BLUETOOTH_HANDLER_OFF;
            break;
        }

        return status;
    }

}
