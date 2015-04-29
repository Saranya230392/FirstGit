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

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.nfc.*;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.MDMSettingsAdapter;
import android.view.View;

import android.util.Log;

import com.android.settings.nfc.trigger.*;
import com.android.settings.lgesetting.Config.Config;
import android.widget.Switch;

import com.lge.nfcaddon.NfcAdapterAddon;
import com.lge.os.Build;

public class NfcMainSwitchPref extends NfcStateListener implements Preference.OnPreferenceChangeListener,
        NfcStateListener.NfcSwitchListener {

    private static final String TAG = "NFC_MainSwitchPref";
            
    private final NfcSwitchPreference mMainSwitchPrefence;
    private final NfcSwitchPreference mBeamSwitchPrefence;

    //[START][PORTING_FOR_NFCSETTING]
    //private NfcPreferenceDialog mDialog;
    //[END][PORTING_FOR_NFCSETTING]
    private NfcAdapterAddon mNfcAdapterAddon;

    private Context mCtx;
    private NfcTriggerIf mTriggerIf;

    private int mOldState;
    // 20131015_rebestm_add_cardState
    private int mOldCardState;
    private boolean mIsfromButton;

    public NfcMainSwitchPref (Context ctx, NfcSwitchPreference mainSwitchPref, NfcSwitchPreference beamSwitchPref) {
        mCtx = ctx;
        mMainSwitchPrefence = mainSwitchPref;
        mBeamSwitchPrefence = beamSwitchPref;

        mOldState = -1;
        mOldCardState = -1;
        mIsfromButton = false;
//[CHECK][START][PORTING_NFC]
        //[START][PORTING_FOR_NFCSETTING]
        //if (NfcSettingAdapter.NfcUtils.hasInner()) {
        //    mTriggerIf = new NfcMainTriggerWithCardTrigger(mCtx, mMainSwitchPrefence);
        //} else {
        //    mTriggerIf = new NfcMainTrigger(mCtx);
        //}
        if (NfcSettingAdapter.NfcUtils.hasInner()) {
            mTriggerIf = new NfcCardTrigger(mCtx);
        } else {
            mTriggerIf = new NfcMainTrigger(mCtx);
        }
        //[END][PORTING_FOR_NFCSETTING]
//[CHECK][END][PORTING_NFC]
        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();

        mMainSwitchPrefence.setNfcSwitchListener(this);
        mMainSwitchPrefence.setOnPreferenceChangeListener(this);

        //[START][PORTING_FOR_NFCSETTING]
        //mDialog = new NfcPreferenceDialog(mCtx, mMainSwitchPrefence);
        //[END][PORTING_FOR_NFCSETTING]

        if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == false
                && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() == false
                && !Utils.isWifiOnly(mCtx)
                && !Utils.isUI_4_1_model(mCtx)) {
            mMainSwitchPrefence.setFragment(null);
            mMainSwitchPrefence.setDivider(false);
        }
//[CHECK][START][PORTING_NFC]
        if (!(Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2)) {
            if (NfcSettingAdapter.NfcUtils.hasOperator(NfcSettingAdapter.NfcUtils.koreaOperator)) {
                mMainSwitchPrefence.setTitle(R.string.nfc_quick_toggle_title_jb_plus);
            }
        }
//[CHECK][END][PORTING_NFC]
        //2013_07_09
        //airNfcSwitch = mMainSwitchPref;  // wonjong77.lee [2014-08-23] : need to check about it. it was used by checking status of beam switch pref
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        mMainSwitchPrefence.setOnPreferenceChangeListener(this);
        mMainSwitchPrefence.setNfcSwitchListener(this);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        mMainSwitchPrefence.setOnPreferenceChangeListener(null);
        mMainSwitchPrefence.setNfcSwitchListener(null);
        mIsfromButton = false;
    }

    @Override
    public void onSwitchChange(View view) {
    //NfcSwitchListener.onSwitchChange
        // TODO Auto-generated method stub
        boolean bValue = false;
        Switch switchView = null;
        View checkableView = view.findViewById(R.id.switchWidget);

        mIsfromButton = true;

        if (checkableView == null) {
            Log.d (TAG, "[NfcSwitchPref] checkableView is null");
            return;
        }

        switchView = (Switch)checkableView;
        bValue = switchView.isChecked();

        Log.d(TAG, "[NfcSwitchPref] onSwitchChange , bValue = " + bValue);

        if (mBeamSwitchPrefence != null) {
            mBeamSwitchPrefence.setEnabled(false);
        }

        // [CHANGE_UTILS]
        if (bValue && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() 
                && !Utils.isWifiOnly(mCtx)) {
            if ((NfcSettingAdapter.isUnchecked(mCtx, NfcSettingAdapter.NUMBER_TAG_DEFAULT_FIRSTCONNCET) == 0)
                    && (!Utils.isWifiOnly(mCtx))) {
                if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx)) {
                    // [AIRPLANE_MODE] mNfcSettings.setEnabled(false);
                    Log.d(TAG, "update nfc setting : false : airplane on");
                    //[START][PORTING_FOR_NFCSETTING]
                    NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_AIRPLANE);
                    //mDialog.airplaneNfcDlg();
                    //[END][PORTING_FOR_NFCSETTING]
                } else {
                    //[START][PORTING_FOR_NFCSETTING]
                    NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF);
                    //mDialog.showNfcFirstConnectOffDlg();
                    //[END][PORTING_FOR_NFCSETTING]
                }
            } else {
                if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx) && mBeamSwitchPrefence.isChecked() == false) {
                    //[START][PORTING_FOR_NFCSETTING]
                    NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_AIRPLANE);
                    //mDialog.airplaneNfcDlg();
                    //[END][PORTING_FOR_NFCSETTING]
                } else {
                    mMainSwitchPrefence.setEnabled(false);
                    if (false == mTriggerIf.trigger(bValue)) {
                        mMainSwitchPrefence.setEnabled(true);
                        switchView.setChecked(false);
                    }
                }
            }
        } else {

            mMainSwitchPrefence.setEnabled(false);
            if (false == mTriggerIf.trigger(bValue)) {

                mMainSwitchPrefence.setEnabled(true);
                switchView.setChecked(true);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    // Preference.OnPreferenceChangeListener
        if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == false
                // [CHANGE_UTILS]
                && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() == false
                && !Utils.isWifiOnly(mCtx)
                && !Utils.isUI_4_1_model(mCtx)) {
                
            boolean bValue = (Boolean)newValue;

            bValue = !mNfcAdapterAddon.isNfcSystemEnabled();
            Log.d(TAG, "[NfcSwitchPref] onPreferenceChange = " + bValue);

            // [CHANGE_UTILS]
            if (bValue && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() && !Utils.isWifiOnly(mCtx)) {
                if ((NfcSettingAdapter.isUnchecked(mCtx, NfcSettingAdapter.NUMBER_TAG_DEFAULT_FIRSTCONNCET) == 0)
                        && (!Utils.isWifiOnly(mCtx))) {
                    if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx)) {
                        //[AIRPLANE_MODE] mNfcSettings.setEnabled(false);
                        Log.d(TAG, "update nfc setting : false : airplane on");
                        //[START][PORTING_FOR_NFCSETTING]
                         NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_AIRPLANE);
                        //mDialog.airplaneNfcDlg();
                        //[END][PORTING_FOR_NFCSETTING]
                    } else {
                        //[START][PORTING_FOR_NFCSETTING]
                        NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_FIRST_CONNECT_OFF);
                        //mDialog.showNfcFirstConnectOffDlg();
                        //[END][PORTING_FOR_NFCSETTING]
                    }

                } else {
                    if (true == AirplaneModeEnabler.isAirplaneModeOn(mCtx) && mBeamSwitchPrefence.isChecked() == false) {
                        //[START][PORTING_FOR_NFCSETTING]
                        NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                            NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_AIRPLANE);
                        //mDialog.airplaneNfcDlg();
                        //[END][PORTING_FOR_NFCSETTING]
                    } else {
                        //[START][MD]
                        if (false == mTriggerIf.trigger(bValue)) {
                            return false;
                        }
                        //[END]
                    }
                }
            } else {
                //[START][MD]
                if (false == mTriggerIf.trigger(bValue)) {
                    return false;
                }
                //[END]
            }
            return true;
        } else {
            Log.d(TAG, "[NfcSwitchPref] KOR not used , newValue = " + newValue);
            return false;
        }
    }

    // rebestm - summary        
    public void handleRwStateChanged(int newState) {

        Log.d(TAG, "[NfcSwitchPref] handleRwStateChanged = " + newState);

        int p2pState = newState;
        
        switch (p2pState) {
            case NfcAdapterAddon.STATE_DISCOVERY_OFF:
                if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() && !mNfcAdapterAddon.isNfcRwModeEnabled() && mNfcAdapterAddon.isNfcSystemEnabled()) {
                    if (!mIsfromButton) {
                        mMainSwitchPrefence.setSummary(R.string.settings_shareconnect_nfc_summary_only_cardon2_2);
                    } else {
                         mIsfromButton = false;
                    }
                }
                break;
            case NfcAdapterAddon.STATE_DISCOVERY_ON:
                if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() && mNfcAdapterAddon.isNfcSystemEnabled()) {
                    mMainSwitchPrefence.setSummary(R.string.settings_shareconnect_nfc_summary_on_2);
                }
                break;
            default:
                break;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setNfcEnablerMenu(mCtx, mMainSwitchPrefence, null, mBeamSwitchPrefence);
        }
        // LGMDM_END
    }

    // 20131015_rebestm_add_cardState 
    public void handleNfcStateChanged(int nfcState, int cardState) {

        Log.d(TAG, "[NfcSwitchPref] handleNfcStateChanged  " + "OldNfcstate = "
                + mOldState + ", new = " + nfcState);
        Log.d(TAG, "[NfcSwitchPref] handleNfcStateChanged  " + "mOldCardState = "
                + mOldCardState + ", new = " + cardState);

        if (mOldCardState == cardState && mOldState == nfcState) {
            Log.i(TAG, "[NfcSwitchPref] All status same return!!!");
            return;
        }

        switch (nfcState) {
        case NfcAdapterAddon.STATE_OFF:
            mMainSwitchPrefence.setChecked(false);
            mMainSwitchPrefence.setEnabled(true);
            if (mBeamSwitchPrefence != null) {
                mBeamSwitchPrefence.setEnabled(true);
            }
            //mSwitch.setSummaryOff(R.string.nfc_quick_toggle_summary); jalran boss

            if (Utils.isFrench()) {
                mMainSwitchPrefence.setSummaryOff(R.string.settings_share_nfc_summary_fr);
            } else if ( Utils.isWifiOnly(mCtx) || Utils.isTablet()) {
                mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_tablet);
            } else if (!NfcSettingAdapter.NfcUtils.hasCardEmulation()) {
                mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_unsupported_ce);
            } else if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
                mMainSwitchPrefence.setSummary(R.string.settings_shareconnect_nfc_summary_off_2);
            } else if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_dualsim);
            } else {
                mMainSwitchPrefence.setSummaryOff(R.string.nfc_quick_toggle_summary_easily);
            }
            Log.d(TAG, "[NfcSwitchPref] mIsfromButton = " + mIsfromButton);
            mIsfromButton = false;
            break;
        case NfcAdapterAddon.STATE_ON:
            if (cardState == NfcAdapterAddon.STATE_CARD_ON) {
                mMainSwitchPrefence.setChecked(true);
                mMainSwitchPrefence.setEnabled(true);
                if (mBeamSwitchPrefence != null) {
                    mBeamSwitchPrefence.setEnabled(true);
                }

                //mSwitch.setSummaryOn(R.string.nfc_quick_toggle_summary); // "NFC enabled" jalran boss
                if (Utils.isFrench()) {
                    mMainSwitchPrefence.setSummaryOff(R.string.settings_share_nfc_summary_fr);
                } else if ( Utils.isWifiOnly(mCtx) || Utils.isTablet()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_tablet);
                } else if (!NfcSettingAdapter.NfcUtils.hasCardEmulation()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_unsupported_ce);
                } else if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
                    if (!mNfcAdapterAddon.isNfcRwModeEnabled() && mNfcAdapterAddon.isNfcSystemEnabled()) {
                        //if (!mIsfromButton) {
                         //[START][PORTING_FOR_NFCSETTING]
                         //if (mIsfromButton == false || NfcCardTrigger.isShowNfcOffDialog()) { // when dialog for disable nfc is activated, you should change summay words about card-mode.
                         if (mIsfromButton == false || NfcSettingAdapter.NfcUtils.isShowNfcOffDialog()) {
                         //[END][PORTING_FOR_NFCSETTING]
                            mMainSwitchPrefence.setSummary(R.string.settings_shareconnect_nfc_summary_only_cardon2_2);
                        } else {
                             mIsfromButton = false;
                        }
                    }
                } else if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_dualsim);
                } else {
                    mMainSwitchPrefence.setSummaryOff(R.string.nfc_quick_toggle_summary_easily);
                }
            } else if ((cardState == NfcAdapterAddon.STATE_CARD_OFF) && (mOldCardState != cardState)) {
                mMainSwitchPrefence.setChecked(false);
                mMainSwitchPrefence.setEnabled(true);
                if (mBeamSwitchPrefence != null) {
                    mBeamSwitchPrefence.setEnabled(true);
                }
                //mSwitch.setSummaryOff(R.string.nfc_quick_toggle_summary); jalran boss
                if (Utils.isFrench()) {
                    mMainSwitchPrefence.setSummaryOff(R.string.settings_share_nfc_summary_fr);
                } else if ( Utils.isWifiOnly(mCtx) || Utils.isTablet()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_tablet);
                } else if (!NfcSettingAdapter.NfcUtils.hasCardEmulation()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_unsupported_ce);
                } else if (NfcSettingAdapter.NfcUtils.hasBeamInP2p()) {
                    //mSwitch.setSummary(R.string.settings_shareconnect_nfc_summary_off_2);
                } else if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                    mMainSwitchPrefence.setSummary(R.string.nfc_quick_toggle_summary_easily_dualsim);
                } else {
                    mMainSwitchPrefence.setSummaryOff(R.string.nfc_quick_toggle_summary_easily);
                }
            }
            break;
        case NfcAdapterAddon.STATE_TURNING_ON:
            //                mSwitch.setChecked(true);
            mMainSwitchPrefence.setEnabled(false);
            //jw 2013_06_18 Start prevent successive switch event
            if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == false
                // [CHANGE_UTILS]
                && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() == false) {
                mMainSwitchPrefence.setEnabled(false);
            }
            //jw 2013_06_18 END
            break;
        case NfcAdapterAddon.STATE_TURNING_OFF:
            //                mSwitch.setChecked(false);
            mMainSwitchPrefence.setEnabled(false);
            //jw 2013_06_18 Start prevent successive switch event 
            if (NfcSettingAdapter.NfcUtils.hasBeamInP2p() == false
                // [CHANGE_UTILS]
                && NfcSettingAdapter.NfcUtils.hasNfcDisplaySettings() == false) {
                mMainSwitchPrefence.setEnabled(false);
            }
            //jw 2013_06_18 END
            break;
        default:
            break;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setNfcEnablerMenu(mCtx, mMainSwitchPrefence, null, mBeamSwitchPrefence);
        }
        // LGMDM_END
        mOldState = nfcState;
        mOldCardState = cardState;
    }

    public NfcSwitchPreference getSwitchPreference() {
        return mMainSwitchPrefence;
    }
}

