/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.TwoStatePreference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.nfc.trigger.*;
import com.android.settings.nfc.preference.*;

import com.lge.nfcaddon.NfcAdapterAddon;
import com.lge.nfcconfig.NfcConfigure;
/**
 * LGNfcEnabler is a helper to manage the Nfc on/off preference. It is turns
 * on/off Nfc and ensures the summary of the preference reflects the current
 * state. This class was modified by LGE to support multiple NFC setting UI.
 */

public class LGNfcEnabler {

    static final String TAG = "LGNfcEnabler";

    public static AlertDialog dialog;

    private NfcManager mNfcManager;

    private final NfcAdapter mNfcAdapter;
    private final NfcAdapterAddon mNfcAdapterAddon;

// [START] wonjong77.lee
    private NfcMainSwitchPref       mMainSwitchPref = null;
    private NfcBeamSwitchPref       mBeamSwitchPref = null;
    private NfcSettingScreenPref    mSettingScreenPref = null;
// [END]

    //[START][PORTING_FOR_NFCSETTING]
    private NfcSettingAdapter.ExtResponseProcess mResponseProc = null;
    //[END][PORTING_FOR_NFCSETTING]
    // call this constructor directly.
    public LGNfcEnabler(Context context) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();

        if (mNfcAdapter == null || mNfcAdapterAddon == null) {
            // NFC is not supported
            Log.e(TAG, "mNfcAdapter is null in LGNfcEnabler()");
            return;
        }
        mNfcManager = new NfcManager(context);
    }

    public LGNfcEnabler(Context context, PreferenceScreen nfcSettings) {
        this(context);
        // rebestm - WBT
        if (mNfcManager != null) {
            mSettingScreenPref = new NfcSettingScreenPref(context, nfcSettings);
            mNfcManager.addNfcStateListener(mSettingScreenPref, NfcStateListener.NONE_STATE);
        }
        
        Log.d(TAG, "LGNfcEnabler created  NFC preference screen for kddi");
    }

    public LGNfcEnabler(Context context, NfcSwitchPreference switchNfc, NfcSwitchPreference androidBeam) {
        this(context);
        // rebestm - WBT
        if (mNfcManager != null) {

            mMainSwitchPref = new NfcMainSwitchPref(context, (NfcSwitchPreference)switchNfc, (NfcSwitchPreference)androidBeam);
            mBeamSwitchPref = new NfcBeamSwitchPref(context, (NfcSwitchPreference)switchNfc, (NfcSwitchPreference)androidBeam);

            //[START][PORTING_FOR_NFCSETTING]
            mResponseProc = new NfcSettingAdapter.ExtResponseProcess(context, switchNfc, androidBeam);
            mResponseProc.registerBroadcastReceiver();
            //[END][PORTING_FOR_NFCSETTING]

            mNfcManager.addNfcStateListener(mMainSwitchPref, NfcStateListener.SYSTEM_STATE);
            mNfcManager.addNfcStateListener(mBeamSwitchPref, NfcStateListener.P2P_STATE | NfcStateListener.SYSTEM_STATE);
        }
        
        Log.d(TAG, "LGNfcEnabler created nfc switch + beam switch");
    }

    //jw_skt_modify_menu
    public LGNfcEnabler(Context context, NfcSwitchPreference switchNfc) {
        this(context);
        // rebestm - WBT
        if (mNfcManager != null) {

            mMainSwitchPref = new NfcMainSwitchPref(context, (NfcSwitchPreference)switchNfc, null);
            mBeamSwitchPref = null;
            
            //[START][PORTING_FOR_NFCSETTING]
            mResponseProc = new NfcSettingAdapter.ExtResponseProcess(context, switchNfc, null);
            mResponseProc.registerBroadcastReceiver();
            //[END][PORTING_FOR_NFCSETTING]

            mNfcManager.addNfcStateListener(mMainSwitchPref, NfcStateListener.SYSTEM_STATE | NfcStateListener.RW_STATE);
        }
        
        Log.d(TAG, "LGNfcEnabler created nfc switch");
    }

    public void resume() {
        Log.d(TAG, "LGNfcEnabler resume");

        if (mNfcAdapter == null || mNfcManager == null) {
            Log.e(TAG, "mNfcAdapter || mNfcManager is null!!! return");
            return;
        }

        // dead object referencing.
        //mNfcAdapter.isEnabled();
        mNfcManager.resume();
    }

    public void pause() {

        Log.d(TAG, "LGNfcEnabler pause");

        if (mNfcManager == null) {
            Log.e(TAG, "mNfcManager is null!!! return");
            return;
        }

        mNfcManager.pause();
    }
    //[START][PORTING_FOR_NFCSETTING]
    public void destroy() {
        
        Log.d(TAG, "LGNfcEnabler destroy");

        //[START]wonjong77.lee - fix to changing NFC Enabler Switch
        if (mNfcManager != null) {
            mNfcManager.destroy();
        }
        //[END]wonjong77.lee - fix to changing NFC Enabler Switch

        if (mResponseProc != null) {
            mResponseProc.unregisterBroadcastReceiver();
        }
    }
    //[END][PORTING_FOR_NFCSETTING]
    public void onConfigChange() {
        Log.d(TAG, "LGNfcEnabler onConfigChange");
        //[START][PORTING_FOR_NFCSETTING]
        //if (NfcCardTrigger.isShowNfcOffDialog()) {
        if (NfcSettingAdapter.NfcUtils.isShowNfcOffDialog()) {
        //[END][PORTING_FOR_NFCSETTING]
            NfcSwitchPreference pref = mMainSwitchPref.getSwitchPreference();
            Log.i (TAG, "Button true check");

            if (pref != null && mNfcAdapterAddon.isNfcSystemEnabled()) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
        } else {
            //[START][PORTING_FOR_NFCSETTING]
            Log.i (TAG, "return, isShowNfcOffDlg = " + NfcSettingAdapter.NfcUtils.isShowNfcOffDialog());
            //[END][PORTING_FOR_NFCSETTING]
        }
    }
}
