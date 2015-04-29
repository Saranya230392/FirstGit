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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.WirelessSettings;

import com.lge.constants.SettingsConstants;
import android.net.Uri;
import android.database.Cursor;

import android.app.AlertDialog;
import android.view.WindowManager;
//wfc switch sound
import android.view.View;
import android.view.View.OnClickListener;
import android.content.DialogInterface;

import android.app.ActivityManagerNative;
import android.net.wifi.WifiManager;


public class WifiCallingEnabler implements CompoundButton.OnCheckedChangeListener  {
    private final Context mContext;
    private final ContentResolver mResolver;
    private Switch mSwitch;
    private boolean mStateMachineEvent;
    private static final String TAG = "WifiCallingEnabler";

    private final String CFI_WFC_ENABLE_REQUEST = "com.oem.smartwifisupport.WFC_ENABLE_REQUEST";
    private final String CFI_WFC_ENABLE_COMPLETED = "com.kineto.smartwifi.WFC_ENABLE_COMPLETED";
    private final String CFI_WFC_STATE = "com.kineto.smartwifi.WFCState";

    private final int WIFICALLING_STATE_UNKNOWN = 0;
    private final int WIFICALLING_STATE_ENABLING =1;
    private final int WIFICALLING_STATE_ENABLED = 2;
    private final int WIFICALLING_STATE_DISABLING = 3;
    private final int WIFICALLING_STATE_DISABLED = 4;

    private final int VOWIFI_DEREGISTERED = 0;
    private final int VOWIFI_REGISTERED = 1;
    private int mVoWiFiRegStatus = VOWIFI_DEREGISTERED;

    private AlertDialog mEntryDialogForDss = null;
    private AlertDialog mAirplaneModeDialog = null;

    private static final int AIRPLANE_MODE_OFF = 0;
    private static final int AIRPLANE_MODE_ON = 1;

    public static final int WIFI_CALLING_ACTIVATED = 0;
    public static final int WIFI_CALLING_NOT_ACTIVATED = 1;
    public static final int WIFI_CALLING_911_DISABLED = 2;
    public static final int WIFI_CALLING_UNKNOWN = -1;

    /**** same DB Names with those of CarrierDBSystemPropertyProvider.java and NodeHandlerVoWifi.java ****/
    /*public static final int SYSPROP_VOWIFI_ENABLED = 535; //VoWiFiEnable*/
    private static final String VOWIFI_ENABLED_DB = "VoWifi_Enabled";
    /*public static final int SYSPROP_VOWIFI_CONSENT = 536; //911Consent*/
    private static final String VOWIFI_CONSENT_DB = "VoWifi_Consent";

    public final String AUTHORITY = "com.lge.node";
    public final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NodeData.TABLE_NAME); //"content://com.lge.node/omadm_node"
    public final class NodeData {
        public static final String TABLE_NAME = "omadm_node";
        public static final String NODE_KEY = "key";
        public static final String NODE_VALUE = "value";
    }

    private static final String[] PROJECTION = {
        NodeData.NODE_KEY,     //0
        NodeData.NODE_VALUE,   //1
    };
    private static final String SELECTION = NodeData.NODE_KEY + "=?";

    private WifiManager mWifiManager = null;
    private int mWifiState = -1;


    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*** CallBack intent( wfcEnableResp)  of CFI_WFC_ENABLE_REQUEST intent ***
            //    The OEM should not allow the button to change WFC service state
            //    until there is a response from the Smart Wi-Fi client through an API wfcEnableResp().
            //    wfcEnableResp() broadcast "com.kineto.smartwifi.WFC_ENABLE_COMPLETED" intent.
            if (intent.getAction().equals(CFI_WFC_ENABLE_COMPLETED)) {
                boolean state = intent.getBooleanExtra("state", false);
                Log.d(TAG, "receive WFC_ENABLE_COMPLETED  state="+state);
                if (state == true) {
                    handleWifiCallingStateChanged(WIFICALLING_STATE_ENABLED);
                    //UX Flow 062314 : page31 - International Wi-Fi Calling - Settings Screen Entry Point - New Toggle Behavior
                    //UI Update: Wi-Fi toggled ON when WFC toggle is turned ON.
                    mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    mWifiState = mWifiManager.getWifiState();
                    if ( mWifiState == WifiManager.WIFI_STATE_DISABLED || mWifiState == WifiManager.WIFI_STATE_DISABLING ) {
                        mWifiManager.setWifiEnabled(true);
                    }
                } else /*if (state == false))*/ {
                    handleWifiCallingStateChanged(WIFICALLING_STATE_DISABLED);
                }
            }
            else if (intent.getAction().equals(CFI_WFC_STATE)) {
                String Event = intent.getStringExtra("Event");
                if ( Event != null ) {
                    Log.d(TAG, "receive WFCState  Event="+Event);
                    if (Event.equals("WFC_REGISTERED")) {
                        mVoWiFiRegStatus = VOWIFI_REGISTERED;
                    } else if (Event.equals("WFC_DEREGISTERED")) {
                        mVoWiFiRegStatus = VOWIFI_DEREGISTERED;
                    }
                }
            }
        }
    };

    public WifiCallingEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mResolver = mContext.getContentResolver();

        //TODO...
        //WIFICALLING_STATE_ENABLING, WIFICALLING_STATE_DISABLING
        if (mSwitch.isEnabled() == false)
        {
            Log.d(TAG, "WifiCallingEnabler - Keep Pending??");
            //return;
        }
        if (isWFCStateON()) {
            Log.d(TAG, "WifiCallingEnabler - ON");
            mSwitch.setChecked(true);
            mSwitch.setEnabled(true);
        }
        else {
            Log.d(TAG, "WifiCallingEnabler - OFF");
            mSwitch.setChecked(false);
            mSwitch.setEnabled(true);
        }

        mIntentFilter = new IntentFilter(CFI_WFC_ENABLE_COMPLETED);
        mIntentFilter.addAction(CFI_WFC_STATE);
    }

    public void resume() {
        Log.d(TAG, "resume");
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);

        //TODO...
        //WIFICALLING_STATE_ENABLING, WIFICALLING_STATE_DISABLING
        if (mSwitch.isEnabled() == false)
        {
            Log.d(TAG, "resume - Keep Pending??");
            //return;
        }
        if (isWFCStateON()) {
            Log.d(TAG, "resume - ON");
            //mSwitch.setChecked(true);
            setSwitchChecked(true);
            mSwitch.setEnabled(true);
        }
        else {
            Log.d(TAG, "resume - OFF");
            //mSwitch.setChecked(false);
            setSwitchChecked(false);
            mSwitch.setEnabled(true);
        }
    }

    public void pause() {
        Log.d(TAG, "pause");
        try {
            mContext.unregisterReceiver(mReceiver);
            mSwitch.setOnCheckedChangeListener(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setSwitch(Switch switch_) {
        Log.d(TAG, "setSwitch");

        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        //wfc switch sound
        mSwitch.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });

        //TODO...
        //WIFICALLING_STATE_ENABLING, WIFICALLING_STATE_DISABLING
        if (mSwitch.isEnabled() == false)
        {
            Log.d(TAG, "setSwitch - Keep Pending??");
            //return;
        }
        if (isWFCStateON()) {
            Log.d(TAG, "setSwitch - ON");
            //mSwitch.setChecked(true);
            setSwitchChecked(true);
            mSwitch.setEnabled(true);
        }
        else {
            Log.d(TAG, "setSwitch - OFF");
            //mSwitch.setChecked(false);
            setSwitchChecked(false);
            mSwitch.setEnabled(true);
        }

    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        //Duplicate check
        if (mStateMachineEvent) {
            return;
        }
        Intent intent;

        //Switch(toggle) Off
        if (isChecked == false) {
            Log.d(TAG, "onCheckedChanged - Switch ON->OFF. broadcast intent");

            intent = new Intent(CFI_WFC_ENABLE_REQUEST);
            intent.putExtra("state", false);
            //mContext.sendBroadcast(intent);
            ActivityManagerNative.broadcastStickyIntent(intent, null, UserHandle.USER_ALL);

            mSwitch.setEnabled(false);
        }
        //Switch(toggle) On
        else /*if(isChecked == true)*/ {
            int dssResult = isDeviceProvisioned();
            if (isAirplaneModeOn()) {
                setSwitchChecked(false);
                airplaneModeTurnOffDialog(mContext);
            } else {
                if (!isFirstUseCase() && dssResult == WIFI_CALLING_ACTIVATED) {
                    Log.d(TAG, "onCheckedChangedSwitch OFF->ON. broadcast WFC_ENABLE_REQUEST intent");
                    Intent mIntent = new Intent(CFI_WFC_ENABLE_REQUEST);
                    mIntent.putExtra("state", true);
                    ActivityManagerNative.broadcastStickyIntent(mIntent, null, UserHandle.USER_ALL);
                    mSwitch.setEnabled(false);
                } else {
                    Intent mintent = new Intent("com.lge.settings.WIFI_CALLING_LOCATIONSETTING");
                    mintent.putExtra("FIRSTUSE", true);  //Because first use address DB does not exist..
                    mintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivityAsUser(mintent, UserHandle.CURRENT);
                }
            }
        }
    }

    //Check whether defaultAddr DB exists or not.
    //- "NOT" means that user has not set the first use address at the vowifi initialization time or this is the first time to use Wi-Fi Calling.
    private boolean isFirstUseCase() {
        String mWFC_AUTHORITY = "com.android.settings.wifi.WifiCallingDefaultAddrDBHelperProvider";
        Uri mWFC_CONTENT_URI = Uri.parse("content://" + mWFC_AUTHORITY + "/" + "defaultAddr");
        //  //data/data/com.android.settings/databases/wificalling_defaultAddr.db
        String m_ID = "id";
        String m_AP_NM = "ap_name"; // 1
        String m_WPS = "wps"; //InternationalWFC.. Using this for Country Code
        String m_ADDRESS = "address";
        String m_MAC_ADDRESS = "mac_address"; // 4
        String m_IS_SCANNED_AP = "is_scanned_ap";
        String m_IS_CHECKED_AP = "is_checked_ap";
        String m_QUERY_TIME = "query_time";
        String m_VALIDATE = "validate";
        String m_MSCID = "mscid"; // 9
        String m_CELLID = "cellid";
        String m_SECTORID = "sectorid";
        String m_LATITUDE = "latitude";
        String m_LONGITUDE = "longitude";
        String m_URADIUS = "uradius";
        String m_ADDR911TTL = "Addr911ttl";
        String m_INFOOTPRINT = "infootprintind";
        String m_IS_AP_ENABLED = "is_ap_enabled";
        String[] mWIFICALLING_PROJECTION = new String[] {m_ID, m_AP_NM, m_WPS, m_ADDRESS, m_MAC_ADDRESS, m_IS_SCANNED_AP, m_IS_CHECKED_AP,
                    m_QUERY_TIME, m_VALIDATE, m_MSCID, m_CELLID, m_SECTORID, m_LATITUDE, m_LONGITUDE,
                    m_URADIUS, m_ADDR911TTL, m_INFOOTPRINT, m_IS_AP_ENABLED };
        Cursor c = mContext.getContentResolver().query(mWFC_CONTENT_URI, mWIFICALLING_PROJECTION, null, null, null);
        if (c != null) {
            return c.getCount() == 0;
        } else {
            Log.d(TAG, "cannot find Default Addr DB, cursor is null!");
            return true;
        }
    }

    private boolean isAirplaneModeOn() {
        return (Settings.System.getInt(mResolver, Settings.System.AIRPLANE_MODE_ON, 0) == AIRPLANE_MODE_ON);
    }

    private void setAirplaneMode(boolean status) {
        Settings.Global.putInt(mResolver, Settings.System.AIRPLANE_MODE_ON, status ? AIRPLANE_MODE_ON : AIRPLANE_MODE_OFF);

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("state", status);
        mContext.sendBroadcast(intent);
    }

    private void airplaneModeTurnOffDialog(Context context) {
        Log.d(TAG, "airplaneModeTurnOffDialog");

        String strTitle;
        String strMsg;
        String strOkButton;
        String strCancelButton;

        if (mAirplaneModeDialog != null) {
            mAirplaneModeDialog.dismiss();
            mAirplaneModeDialog = null;
        }

        strTitle = context.getString(R.string.vowifi_setting_airplane_mode_off_title);
        strMsg = context.getString(R.string.vowifi_setting_airplane_mode_off_msg);
        strOkButton = context.getString(R.string.dlg_ok);
        strCancelButton = context.getString(R.string.dlg_cancel);

        AlertDialog.Builder alertDialogMsg = new AlertDialog.Builder(context);
        alertDialogMsg.setTitle(strTitle).setMessage(strMsg)
        .setNegativeButton(strCancelButton, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        })
        .setPositiveButton(strOkButton, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Turn Off Airplane mode");
                setAirplaneMode(false);
                dialog.dismiss();

                int dssResult = isDeviceProvisioned();
                if (!isFirstUseCase() && dssResult == WIFI_CALLING_ACTIVATED) {
                    Intent mIntent = new Intent(CFI_WFC_ENABLE_REQUEST);
                    mIntent.putExtra("state", true);
                    ActivityManagerNative.broadcastStickyIntent(mIntent, null, UserHandle.USER_ALL);
                    mSwitch.setEnabled(false);
                } else {
                    Intent mintent = new Intent("com.lge.settings.WIFI_CALLING_LOCATIONSETTING");
                    mintent.putExtra("FIRSTUSE", true);  //Because first use address DB does not exist..
                    mintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivityAsUser(mintent, UserHandle.CURRENT);
                }
            }

        });

        AlertDialog alert = alertDialogMsg.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
        mAirplaneModeDialog = alert;

    }


    public boolean getWFCState() {
        return isWFCStateON();
    }

    private void handleWifiCallingStateChanged(int state) {
        switch (state) {
            case WIFICALLING_STATE_ENABLING:
                mSwitch.setEnabled(false);
                break;
            case WIFICALLING_STATE_ENABLED:
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WIFICALLING_STATE_DISABLING:
                mSwitch.setEnabled(false);
                break;
            case WIFICALLING_STATE_DISABLED:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
        }
    }

    private void setSwitchChecked(boolean checked) {
        Log.d(TAG, "setSwitchChecked checked="+checked);
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    private boolean isWFCStateON() {
        boolean ret = false;
        try {
            int result = Settings.System.getInt(mResolver, SettingsConstants.System.VOWIFI_ONOFF, 0); //Check WFCSettings Toggle on/off
            if(result  == 1) {
                Log.i(TAG, "isWFCStateON. TRUE");
                ret = true;
            }
            else {
                Log.i(TAG, "isWFCStateON. FALSE");
                ret = false;
            }
        } catch(Exception e) {
            Log.e(TAG, "isWFCStateON is failed");
            e.printStackTrace();
        }
        return ret;
    }

    private void VoWiFiEntryDialogForDSS() {

        if (mEntryDialogForDss != null) {
            mEntryDialogForDss.dismiss();
            mEntryDialogForDss = null;
        }
        Log.d(TAG, "VoWiFiEntryDialogForDSS() <-Wi-Fi App First Time Entry Dialog.");

        //SPRINT_LGE_WFC_FROM_KK, Change String value to String token
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, com.lge.R.style.Theme_LGE_White_Dialog_Alert);
        builder.setTitle(R.string.sp_vowifi_on_title);
        builder.setMessage(R.string.sp_vowifi_on_NORMAL);

        //PROCEED
        builder.setPositiveButton(R.string.sp_vowifi_on_proceed,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int result = isDeviceProvisioned();
                    if ( result == WIFI_CALLING_NOT_ACTIVATED) { //SYSPROP_VOWIFI_ENABLED == false
                        Log.d(TAG, "VoWiFiEntryDialogForDSS(). Start Sprint DSS with vowifi");
                        Intent myintent = new Intent();
                        myintent.setAction("com.sprint.dsa.DSA_ACTIVITY");
                        myintent.setType("vnd.sprint.dsa/vnd.sprint.dsa.main");
                        myintent.putExtra("com.sprint.dsa.source", "vowifi");
                        myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(myintent);
                    }
                    else if ( result == WIFI_CALLING_911_DISABLED) { //SYSPROP_VOWIFI_ENABLED ==true && SYSPROP_VOWIFI_CONSENT == false
                        Log.d(TAG, " VoWiFiEntryDialogForDSS. Start Sprint DSS with vowifi911");
                        Intent myintent = new Intent();
                        myintent.setAction("com.sprint.dsa.DSA_ACTIVITY");
                        myintent.setType("vnd.sprint.dsa/vnd.sprint.dsa.main");
                        myintent.putExtra("com.sprint.dsa.source", "vowifi911");
                        myintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(myintent);
                    }
                    else {
                        Log.d(TAG, "VoWiFiEntryDialogForDSS(). Do nothing.");
                    }
                    dialog.dismiss();
                }
            }
        );

        //NOT NOW
        builder.setNegativeButton(R.string.sp_vowifi_on_notnow,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }
        );

        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
        mEntryDialogForDss = alert;
    }

    private String getProvisioningInfoFromDB(String selectionArg) {
        Cursor cursor = null;
        String result = null;

        if (mResolver == null || selectionArg == null) {
            return null;
        }

        cursor = mResolver.query(CONTENT_URI, PROJECTION, SELECTION, new String[]{selectionArg}, null);
        if (cursor != null && cursor.moveToFirst())
        {
            result = cursor.getString(1);
        }
        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    private int isDeviceProvisioned() {
        String SysVoWiFiEnable = getProvisioningInfoFromDB(VOWIFI_ENABLED_DB); //SYSPROP_VOWIFI_ENABLED = 535
        String Sys911Consent = getProvisioningInfoFromDB(VOWIFI_CONSENT_DB); //SYSPROP_VOWIFI_CONSENT = 536

        if (SysVoWiFiEnable == null) {
            SysVoWiFiEnable = "0";
        }
        if (Sys911Consent == null) {
            Sys911Consent = "0";
        }

        Log.d(TAG, "isDeviceProvisioned() SysVoWiFiEnable=" + SysVoWiFiEnable);
        Log.d(TAG, "isDeviceProvisioned() Sys911Consent=" + Sys911Consent);

        if (SysVoWiFiEnable.equals("true") && Sys911Consent.equals("true")) {
            return WIFI_CALLING_ACTIVATED;
        }
        else if (SysVoWiFiEnable.equals("1") && Sys911Consent.equals("1")) {
            return WIFI_CALLING_ACTIVATED;
        }
        else if (SysVoWiFiEnable.equals("false")) {
            return WIFI_CALLING_NOT_ACTIVATED;
        }
        else if (SysVoWiFiEnable.equals("0")) {
            return WIFI_CALLING_NOT_ACTIVATED;
        }
        else if(SysVoWiFiEnable.equals("true") && Sys911Consent.equals("false")) {
            return WIFI_CALLING_911_DISABLED;
        }
        else if(SysVoWiFiEnable.equals("1") && Sys911Consent.equals("0")) {
            return WIFI_CALLING_911_DISABLED;
        }
        else {
            return WIFI_CALLING_UNKNOWN;
        }
    }

}
