/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc.preference;

import android.nfc.NfcAdapter;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.content.Context;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.nfc.*;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.MDMSettingsAdapter;

import android.content.ServiceConnection;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.lge.nfclock.service.INfcSettingRemoteIF;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.widget.Toast;

import android.util.Log;
import com.android.settings.lgesetting.Config.Config;

import com.lge.nfcaddon.NfcAdapterAddon;

import com.lge.nfclock.service.common.UserLockStatusType;
import com.lge.nfclock.service.common.RemoteLockStatusType;
import com.lge.nfclock.service.common.LockResultType;


public class NfcSettingScreenPref extends NfcStateListener implements OnPreferenceClickListener {
    private static final String TAG = "NFC_SettingScrPref";
    
    private final       PreferenceScreen mNfcSettings;
    private INfcSettingRemoteIF    mFelicaNfcLock;
    private Context     mCtx;

    
    ServiceConnection srvConn = new ServiceConnection() {
    
        public void onServiceDisconnected(ComponentName name) {
            mFelicaNfcLock = null;
            Log.d(TAG, "INfcSettingRemoteIF: onServiceDisconnected");
        }
    
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFelicaNfcLock = com.lge.nfclock.service.INfcSettingRemoteIF.Stub.asInterface(service);
    
            Log.d(TAG, "INfcSettingRemoteIF: onServiceConnected");
            updateNfcSettings();
        }
    };
    
    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNfcSettings();
            Log.d(TAG, "on airplane mode receiver");
        }
    };
    
    public NfcSettingScreenPref(Context ctx, PreferenceScreen prep) {
        mCtx = ctx;
        mNfcSettings = prep;
    }
    
    public void handleNfcStateChanged(int newState) {
    
        switch (newState) {
        case NfcAdapterAddon.STATE_OFF:
            break;
        case NfcAdapterAddon.STATE_ON:
            break;
        case NfcAdapterAddon.STATE_TURNING_ON:
            break;
        case NfcAdapterAddon.STATE_TURNING_OFF:
            break;
        default:
            break;
        }
    
    }

    private RemoteLockStatusType getNfcRemoteLockStatus() {
        Log.d(TAG, "getNfcRemoteLockStatus()");
        if (null == mFelicaNfcLock) {
            Log.d(TAG, "mINfcLock == null in getNfcRemoteLockStatus ");
            return RemoteLockStatusType.REMOTE_ERROR;
        }
    
        try {
            return mFelicaNfcLock.getRemoteLockStatus(false, false, false);
        } catch (RemoteException e) {
            e.printStackTrace();
            return RemoteLockStatusType.REMOTE_ERROR;
        }
    }

    private boolean isNfcRemoteLocked() {
        RemoteLockStatusType remoteLockStatus = getNfcRemoteLockStatus ();
        boolean result = false;
    
        Log.d(TAG, "[isNfcRemoteLocked] current status : " + remoteLockStatus);
    
        switch (remoteLockStatus) {
            case REMOTE_CLF_LOCK:
            case REMOTE_UIM_LOCK:
            case REMOTE_MDM_LOCK:
            case REMOTE_CLF_UIM_LOCK:
            case REMOTE_CLF_MDM_LOCK:
            case REMOTE_UIM_MDM_LOCK:
            case REMOTE_CLF_LOCK_UNAVAILABLE_UIM:
            case REMOTE_ALL_LOCK:
                Log.d(TAG, "isNfcRemoteLocked() = true");     
                result = true;
                break;
                
            default:
                Log.d(TAG, "isNfcRemoteLocked() = false");
                break;
        }
    
        return result;
    }

    
    private void updateNfcSettings() {
       // try {
            if (null == mFelicaNfcLock) {
                mNfcSettings.setEnabled(false);
                //mNfcSettings.setEnabled(true); // jongwon
                Log.d(TAG, "update nfc setting : false : mNfcLock is null");
            } else if (true == isNfcRemoteLocked()) {
                //mNfcSettings.setEnabled(false);
                mNfcSettings.setEnabled(true); // jongwon
                Log.d(TAG, "update nfc setting : false : remote locked");
            }
            // [AIRPLANE_MODE][START]
            /*
             * else if ( true ==
             * AirplaneModeEnabler.isAirplaneModeOn(mContext) ) {
             * //[AIRPLANE_MODE] mNfcSettings.setEnabled(false); Log.d(TAG,
             * "update nfc setting : false : airplane on"); }
             */
            // [AIRPLANE_MODE][END]
    
            else {
                mNfcSettings.setEnabled(true);
                Log.d(TAG, "update nfc setting : true");
            }
            // rebestm - for KDDI
            if (Utils.hasFeatureNfcLockForKDDI() && isNfcRemoteLocked()) {
                mNfcSettings.setEnabled(false);
                // Post the intent
                Intent intent = new Intent("com.lge.nfclock.LOCK_STATE_CHANGED");
                mCtx.sendBroadcast(intent); // sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        //} catch (RemoteException e) {
        //    e.printStackTrace();
        //    mNfcSettings.setEnabled(false);
        //}
    }
    
    @Override
    public void resume() {
        mNfcSettings.setEnabled(false);
    
        mFelicaNfcLock = null;
        Intent intent = new Intent();
        intent.setAction("com.lge.nfclock.service.INfcSettingRemoteIF");
        intent.setPackage("com.lge.nfclock");
        mCtx.bindService(intent, srvConn, Context.BIND_AUTO_CREATE);
    
        mNfcSettings.setOnPreferenceClickListener(this);
        mCtx.registerReceiver(mAirplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    }
    
    @Override
    public void pause() {
        mFelicaNfcLock = null;
        mCtx.unbindService(srvConn);
        mNfcSettings.setOnPreferenceClickListener(null);
        mCtx.unregisterReceiver(mAirplaneModeReceiver);
    }
    
    @Override
    public boolean onPreferenceClick(Preference arg0) {
        // TODO Auto-generated method stub
        // jalran
        // rebestm - A1_KDDI
        //            if ((TelephonyManager.SIM_STATE_READY != TelephonyManager.getDefault().getSimState()) &&
        //                Utils.hasFeatureNfcLockForKDDI()) {
        //                Log.d(TAG, "[LGNfcEnabler] Preference onClick - Sim satus is Ready");
        //                Toast.makeText(mContext, R.string.nfc_toast_no_uim, Toast.LENGTH_LONG).show();
        //                return false;
        //            } else
    
        if ((TelephonyManager.SIM_STATE_UNKNOWN == TelephonyManager.getDefault().getSimState())
                && "DCM".equals(Config.getOperator())) {
            Log.d(TAG, "Preference onClick - Sim satus is UnKown");
            Toast.makeText(mCtx,
                    R.string.nfc_toast_sim_state_unknown_docomo,
                    Toast.LENGTH_LONG).show();
            return false;
        }
    
        if (null == mFelicaNfcLock) {
            return false;
        }
        // rebestm - A1_KDDI
        //            try {
        //                if ( mFelicaNfcLock.isSimForNFC() == false ) {
        //                    if (Utils.hasFeatureNfcLockForKDDI()) {
        //                        Toast.makeText(mContext, R.string.nfc_toast_no_uim_for_nfc, Toast.LENGTH_LONG).show();
        //                    }
        //                }
        //            } catch (RemoteException e) {
        //                e.printStackTrace();
        //            }
    
        //isToastForDCM = true;
        return false;
    }

}

