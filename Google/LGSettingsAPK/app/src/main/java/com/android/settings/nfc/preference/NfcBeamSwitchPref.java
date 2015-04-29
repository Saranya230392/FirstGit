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
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.Context;
import android.os.UserManager;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.nfc.*;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.MDMSettingsAdapter;
import android.view.View;

import android.util.Log;

import com.android.settings.nfc.trigger.*;
import com.android.settings.lgesetting.Config.Config;

import android.widget.Toast;
import com.lge.nfcaddon.NfcAdapterAddon;
import android.widget.Switch;

public class NfcBeamSwitchPref extends NfcStateListener implements
        Preference.OnPreferenceChangeListener, NfcStateListener.NfcSwitchListener {

    private static final String TAG = "NFC_BeamSwitchPref";

    private Context mCtx;

    //[START][PORTING_FOR_NFCSETTING]
    //private NfcPreferenceDialog mDialog;
    //[END][PORTING_FOR_NFCSETTING]

    private NfcSwitchPreference mMainSwitchPrefence;
    private NfcSwitchPreference mBeamSwitchPrefence;

    private NfcTriggerIf mCardTriggerIf;
    private NfcTriggerIf mBeamTriggerIf;

    private NfcAdapterAddon mNfcAdapterAddon;

    private static boolean sFirstBeam = false;   // wonjong77.lee - There is that we will need to check a name of this variable

    private Switch mSwitch = null;
    //luri.lee@lge.com [Outgoing Beam][START]
    private boolean mBeamDisallowed;
    //luri.lee@lge.com [Outgoing Beam][END]

    public NfcBeamSwitchPref(Context ctx, NfcSwitchPreference mainSwitchPref, NfcSwitchPreference beamSwitchPref) {

        mCtx = ctx;
        
        mMainSwitchPrefence = mainSwitchPref;
        mBeamSwitchPrefence = beamSwitchPref;

        mCardTriggerIf = new NfcCardTrigger(mCtx);
        mBeamTriggerIf = new NfcP2pBeamTrigger(mCtx, NfcSettingAdapter.NfcUtils.hasBeamInP2p());

        mBeamSwitchPrefence.setNfcSwitchListener(this);
        // jalran : This logic don't care Docomo/KDDI model
        if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
            mBeamSwitchPrefence.setTitle(R.string.nfc_direct_beam_tm_ea_title);
            mBeamSwitchPrefence.setSummary(R.string.nfc_preference_direct_beam_summary_easily);
        } else {
            mBeamSwitchPrefence.setTitle(R.string.android_beam_settings_title);
            mBeamSwitchPrefence.setSummary(R.string.nfc_preference_direct_beam_summary_easily);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76][ID-MDM-307]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setNfcEnablerMenu(mCtx, mMainSwitchPrefence, null, mBeamSwitchPrefence);
        }
        //[START][PORTING_FOR_NFCSETTING]
        //mDialog = new NfcPreferenceDialog(mCtx, mBeamSwitchPrefence);
        //[END][PORTING_FOR_NFCSETTING]

        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();

        //luri.lee@lge.com [Outgoing Beam][START]
        mBeamDisallowed = ((UserManager)mCtx.getSystemService(Context.USER_SERVICE))
                .hasUserRestriction(UserManager.DISALLOW_OUTGOING_BEAM);
        Log.i(TAG, "mBeamDisallowed = " + mBeamDisallowed);

        if (mBeamDisallowed) {
            mBeamSwitchPrefence.setEnabled(false);
        }
        //luri.lee@lge.com [Outgoing Beam][END]

        // LGMDM_END
        //2013_07_09
        //airBeamSwitch = mSwitch;
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        mBeamSwitchPrefence.setOnPreferenceChangeListener(this);
        mBeamSwitchPrefence.setNfcSwitchListener(this);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        mBeamSwitchPrefence.setOnPreferenceChangeListener(null);
        mBeamSwitchPrefence.setNfcSwitchListener(null);
    }

    @Override
    public void onSwitchChange(View view) {

        int resId;
        boolean bValue = false;
        Switch switchView = null;
        View checkableView = view.findViewById(R.id.switchWidget);
        if (mNfcAdapterAddon.getAdapterSysState() == NfcAdapterAddon.STATE_OFF) {
            mBeamSwitchPrefence.setEnabled(false);
        }
        if (checkableView != null) {
            switchView = (Switch)checkableView;
            bValue = switchView.isChecked();

            mSwitch = switchView;
            Log.d(TAG, "beam switch set Disabled while turning on/turning off");
            mSwitch.setEnabled(false);
        } else {
            Log.d(TAG, "[BeamSwitchPref] checkableView is null");
        }
        Log.d(TAG, "[BeamSwitchPref] onSwitchChange , bValue = " + bValue);

        if (bValue && mNfcAdapterAddon.isNfcSystemEnabled() == false) {
            // 2013_07_03_VZW
            // [CHANGE_UTILS]
            if (bValue && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings()) {
                if (NfcSettingAdapter.isUnchecked(mCtx, NfcSettingAdapter.NUMBER_TAG_DEFAULT_FIRSTCONNCET) == 0) {
                    //firstBeam = true;
                    sFirstBeam = true;
                    //[START][PORTING_FOR_NFCSETTING]
                    if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx)) {
                        // [AIRPLANE_MODE] mNfcSettings.setEnabled(false);
                        Log.d(TAG, "[BeamSwitchPref] update nfc setting : false : airplane on");
                        NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_BEAM_DLG_IN_AIRPLANE);
                    } else {
                        //[START][PORTING_FOR_NFCSETTING]
                        NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF);
                        //mDialog.showNfcFirstConnectOffDlg();
                        //[END][PORTING_FOR_NFCSETTING]
                    }
                    //if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx)) {
                        // [AIRPLANE_MODE] mNfcSettings.setEnabled(false);
                    //    Log.d(TAG, "[BeamSwitchPref] update nfc setting : false : airplane on");
                    //    mDialog.airplaneBeamDlg();
                    //} else {
                    //    mDialog.showNfcFirstConnectOffDlg();
                    //}
                    //[END][PORTING_FOR_NFCSETTING]
                } else {
                    if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx) && !mBeamSwitchPrefence.isChecked()) {
                        //[START][PORTING_FOR_NFCSETTING]
                        NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_BEAM_DLG_IN_AIRPLANE);
                        //mDialog.airplaneBeamDlg();
                        //[END][PORTING_FOR_NFCSETTING]
                    } else {
                        resId = (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == true) ? R.string.nfc_toast_trun_on_nfc_rw : R.string.nfc_toast_turn_on_nfc;
                        Toast.makeText(mCtx, resId, Toast.LENGTH_SHORT).show();
                        mCardTriggerIf.trigger(bValue);
                    }
                }
            } else {
                resId = (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == true) ? R.string.nfc_toast_trun_on_nfc_rw : R.string.nfc_toast_turn_on_nfc;
                Toast.makeText(mCtx, resId, Toast.LENGTH_SHORT).show();
                mCardTriggerIf.trigger(bValue);
            }
            // 2013_07_03_VZW
        } else {
            if (mNfcAdapterAddon.isNfcRwModeEnabled() == false) {
                mNfcAdapterAddon.enableNfcDiscovery();
            }
            mBeamTriggerIf.trigger(bValue);
        }
    }

    public static boolean getFirstBeamStatusStatus() {
        Log.d(TAG, "[getFirstBeamStatusStatus] sFirstBeam : " + sFirstBeam);
        return sFirstBeam;
    }

    public static void setFirstBeamStatusStatus(boolean setStatus) {
        Log.d(TAG, "[setFirstBeamStatusStatus] set sFirstBeam : " + setStatus);
        sFirstBeam = setStatus;
    }

    public void handleNfcStateChanged(int nfcState, int cardState) {
        Log.d(TAG, "[BeamSwitchPref] handleNfcStateChanged start --> ");
        Log.d(TAG, "[BeamSwitchPref] nfcState = " + nfcState);

        switch (nfcState) {
        case NfcAdapterAddon.STATE_OFF:
            if (mBeamSwitchPrefence != null) {
                //luri.lee@lge.com [Outgoing Beam][START]
                mBeamSwitchPrefence.setEnabled(!mBeamDisallowed);
                //luri.lee@lge.com [Outgoing Beam][END]
            }
            break;
        case NfcAdapterAddon.STATE_ON:
        case NfcAdapterAddon.STATE_TURNING_ON:
        case NfcAdapterAddon.STATE_TURNING_OFF:
        default:
            break;
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76][ID-MDM-307]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setNfcEnablerMenu(mCtx, null, null, mBeamSwitchPrefence);
        }
        // LGMDM_END
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[BeamSwitchPref] Not used = " + newValue);
        return false;
    }

    public void handleP2pStateChanged(int newState) {
        Log.d(TAG, "[BeamSwitchPref] handleP2pStateChanged start --> ");
        Log.d(TAG, "[BeamSwitchPref] P2pState = " + newState);

        int p2pState = newState;
        switch (p2pState) {
        case NfcAdapterAddon.STATE_P2P_OFF:
            //luri.lee@lge.com [Outgoing Beam][START]
            mBeamSwitchPrefence.setEnabled(!mBeamDisallowed);
            if (mSwitch != null) {
                Log.d(TAG, "beam switch set Enabled");
                mSwitch.setEnabled(true);
            }
            //luri.lee@lge.com [Outgoing Beam][END]
            mBeamSwitchPrefence.setChecked(false);
            mBeamSwitchPrefence.setSummary(R.string.nfc_preference_direct_beam_summary_easily);
            break;
        case NfcAdapterAddon.STATE_P2P_ON:
            //luri.lee@lge.com [Outgoing Beam][START]
            mBeamSwitchPrefence.setEnabled(!mBeamDisallowed);
            if (mSwitch != null) {
                Log.d(TAG, "beam switch set Enabled");
                mSwitch.setEnabled(true);
            }
            //luri.lee@lge.com [Outgoing Beam][END]
            mBeamSwitchPrefence.setChecked(true);
            mBeamSwitchPrefence.setSummary(R.string.nfc_preference_direct_beam_summary_easily);
            break;

        case NfcAdapterAddon.STATE_TURNING_P2P_ON:
            mBeamSwitchPrefence.setEnabled(false);
            break;
        case NfcAdapterAddon.STATE_TURNING_P2P_OFF:
            mBeamSwitchPrefence.setEnabled(false);
            break;
        default:
            break;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76][ID-MDM-307]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setNfcEnablerMenu(mCtx, mMainSwitchPrefence, null, mBeamSwitchPrefence);
        }
        // LGMDM_END
        Log.d(TAG, "[BeamSwitchPref] handleP2pStateChanged = " + newState);
    }
}
