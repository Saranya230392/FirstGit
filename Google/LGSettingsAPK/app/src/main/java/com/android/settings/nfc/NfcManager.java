/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.TwoStatePreference;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.settings.AirplaneModeEnabler;
import com.android.settings.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.app.Activity;

import com.android.settings.lgesetting.Config.Config;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.widget.Toast;
import android.widget.TextView;
import java.util.ArrayList;

import com.android.settings.Utils;
import com.android.settings.nfc.NfcStateListener;


import com.lge.nfcaddon.NfcAdapterAddon;

class NfcManager {
    private class NfcListenerInfo {
        int states;
        NfcStateListener listener;
    };

    static final String TAG = "NfcManager";

    int nfcState;
    int rwState;
    int p2pState;

    int cardState;
    private final Context mContext;

    public NfcManager(Context context) {
        mContext = context;
        mIntentFilter = new IntentFilter(NfcAdapterAddon.ACTION_ADAPTER_STATE_CHANGED);
        resetNfcState();
//[START] wonjong77.lee - fix to changing NFC Enabler Switch
        mContext.registerReceiver(mReceiver, mIntentFilter);
//[END] wonjong77.lee - fix to changing NFC Enabler Switch
    }

    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NfcAdapterAddon.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                int systemExtra = intent.getIntExtra(NfcAdapterAddon.EXTRA_ADAPTER_SYSTEM_STATE,
                        NfcAdapterAddon.STATE_OFF);
                int cardExtra = intent.getIntExtra(NfcAdapterAddon.EXTRA_ADAPTER_CARD_STATE,
                        NfcAdapterAddon.STATE_CARD_OFF);
                int rwExtra = intent.getIntExtra(NfcAdapterAddon.EXTRA_ADAPTER_DISCOVERY_STATE,
                        NfcAdapterAddon.STATE_DISCOVERY_OFF);
                int p2pExtra = intent.getIntExtra(NfcAdapterAddon.EXTRA_ADAPTER_P2P_STATE,
                        NfcAdapterAddon.STATE_P2P_OFF);

                // 20131015_rebestm_add_cardState
                handleNfcStateChanged(systemExtra, rwExtra, p2pExtra, cardExtra);

                Log.d(TAG, "onReceive sys =" + systemExtra + ", p2p =" + p2pExtra);
                Log.d(TAG, "onReceive cardExtra =" + cardExtra);
            }
        }
    };

    ArrayList<NfcListenerInfo> mChangeListener = new ArrayList<NfcListenerInfo>();

    public void resetNfcState() {
        nfcState = -1;
        rwState = -1;
        p2pState = -1;
        cardState = -1;
    }

    public void addNfcStateListener(NfcStateListener listener, int states) {
        NfcListenerInfo info = new NfcListenerInfo();
        info.listener = listener;
        info.states = states;
        mChangeListener.add(info);
    }

    private void handleNfcStateChanged() {
        NfcAdapterAddon nfc = NfcAdapterAddon.getNfcAdapterAddon();

        int sysState = nfc.getAdapterSysState();
        int rwState = nfc.getAdapterDiscoveryState();
        int p2pState = nfc.getAdapterP2pState();
        int cardState = nfc.getAdapterCardState();

        handleNfcStateChanged(sysState, rwState, p2pState, cardState);
    }

    private void handleNfcStateChanged(int sysState, int rwState, int p2pState, int cardState) {
        Log.d(TAG, "current:" + "nfc = " + this.nfcState + ",rw = "
                + this.rwState + ",p2p = " + this.p2pState);
        Log.d(TAG, "current:" + "card = " + this.cardState);
        Log.d(TAG, "next:" + "nfc = " + sysState + ",rw = " + rwState + ",p2p = " + p2pState);
        Log.d(TAG, "next:" + "card = " + cardState);

        for (int i = 0; i < mChangeListener.size(); i++) {
            NfcListenerInfo listener = mChangeListener.get(i);
            notifyNfcStateChanged(listener, sysState, rwState, p2pState, cardState);
        }

        this.nfcState = sysState;
        this.rwState = rwState;
        // Added for invoke beam scenario. [NFC-1713][START]
        if (rwState == NfcAdapterAddon.STATE_DISCOVERY_OFF) {
            Log.d(TAG, "STATE_DISCOVERY_OFF");
            this.p2pState = NfcAdapterAddon.STATE_P2P_OFF;
        } else {
            this.p2pState = p2pState;
        }
        // Added for invoke beam scenario. [NFC-1713][END]

        this.cardState = cardState;
    }

    private void notifyNfcStateChanged(NfcListenerInfo info, int sysState,
         int rwState, int p2pState, int cardState) {

        if ((info.states & NfcStateListener.SYSTEM_STATE) != 0) {
            if (this.nfcState != sysState || this.cardState != cardState) {
                info.listener.handleNfcStateChanged(sysState, cardState);
            }
        }

        if ((info.states & NfcStateListener.RW_STATE) != 0) {
            if (this.rwState != rwState) {
                info.listener.handleRwStateChanged(rwState);
            }
        }

        // Added for invoke beam scenario. [NFC-1713][START]
        if (rwState == NfcAdapterAddon.STATE_DISCOVERY_OFF) {
            Log.d(TAG, "STATE_DISCOVERY_OFF");
            p2pState = NfcAdapterAddon.STATE_P2P_OFF;
        }
        Log.d(TAG, "this.p2pState : " + this.p2pState + "p2pState : " +p2pState);
        // Added for invoke beam scenario. [NFC-1713][END]
        if ((info.states & NfcStateListener.P2P_STATE) != 0) {
            if (this.p2pState != p2pState) {
                info.listener.handleP2pStateChanged(p2pState);
            }
        }
    }

    public void resume() {
        //[START]wonjong77.lee - fix to changing NFC Enabler Switch
        //mContext.registerReceiver(mReceiver, mIntentFilter);
        for (int i = 0; i < mChangeListener.size(); i++) {
            NfcListenerInfo info = mChangeListener.get(i);
            info.listener.resume();
        }

        if (NfcSettingAdapter.ExtRequestConnection.getTriggerActivityStatus()) {
            NfcSettingAdapter.ExtRequestConnection.setTriggerActivityStatus(false);
        } else {
           resetNfcState();
           handleNfcStateChanged();
        }
        //[END]wonjong77.lee - fix to changing NFC Enabler Switch
    }

    public void pause() {
        //[START]wonjong77.lee - fix to changing NFC Enabler Switch
        //mContext.unregisterReceiver(mReceiver);
       for (int i = 0; i < mChangeListener.size(); i++) {
            NfcListenerInfo info = mChangeListener.get(i);
            info.listener.pause();
       }
    }

    //[START]wonjong77.lee - fix to changing NFC Enabler Switch
    public void destroy() {
        mContext.unregisterReceiver(mReceiver);
    }
    //[END]wonjong77.lee - fix to changing NFC Enabler Switch
};