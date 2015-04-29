/*
 * A2 lab 4 team, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2011 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */

package com.android.settings.nfc.trigger;

import android.content.Context;
import android.widget.Toast;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.graphics.Typeface;

import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.nfc.*;
import com.android.settings.MDMSettingsAdapter;

import com.lge.nfcaddon.NfcAdapterAddon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.app.Activity;

import android.util.Log;
import com.android.settings.nfc.preference.NfcSwitchPreference;

public class NfcCardTrigger implements NfcTriggerIf {

    static final String TAG = "NFC_CardTrigger";

    private static boolean sIsUseNfcOffDlg = false;

    private Context mCtx;
    private NfcAdapterAddon mNfcAdapterAddon;
//[START][PORTING_FOR_NFCSETTING]
    //protected NfcSwitchPreference mMainSwitchPreference;

    public NfcCardTrigger(Context ctx) {
        mCtx = ctx;
        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
        //mMainSwitchPreference = null; //[PORTING_FOR_NFCSETTING]
    }
/*
    // [MLM][NFC-812][START]
    public NfcCardTrigger(Context ctx, NfcSwitchPreference mainSwitchPref) {
        this(ctx);
        mMainSwitchPreference = mainSwitchPref;
    }
    // [MLM][NFC-812][END]
*/
//[END][PORTING_FOR_NFCSETTING]

    @Override
    public boolean trigger(boolean isEnable) {

        if (isEnable) {
            if (mNfcAdapterAddon.isWirelessChargingModeOn() == true) {
                Toast.makeText(mCtx, R.string.nfc_toast_cannot_turnon_during_charging,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                    && MDMSettingsAdapter.getInstance().checkDisallowNfc(
                            MDMSettingsAdapter.LGMDM_NFC_SYS)) {
                Log.i(TAG, "NfcCardTrigger MDM Blcok");
                return false;
            }
            // LGMDM_END
            // rebestm - WBT
            //[START][PORTING_FOR_NFCSETTING]
            /*
                        int nfcPowerStatus = NfcAdapterAddon.NFC_CARD_ON;
                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                                && MDMSettingsAdapter.getInstance().checkDisallowNfc(
                                        MDMSettingsAdapter.LGMDM_NFC_CARD_MODE) == false) {
                            nfcPowerStatus |= NfcAdapterAddon.NFC_DISCOVERY_ON;
                        }

                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                              && MDMSettingsAdapter.getInstance().checkDisallowNfc(
                                      MDMSettingsAdapter.LGMDM_NFC_ANDROID_BEAM) == false) {
                            nfcPowerStatus |= NfcAdapterAddon.NFC_P2P_ON;
                        }
                        result = mNfcAdapterAddon.setNfcPowerStatus(nfcPowerStatus);
                        Log.d(TAG, "mNfcAdapterAddon.setNfcPowerStatus(" + nfcPowerStatus + ") - " + result);
                        return result;
                */
            //NfcSettingAdapter.ExtRequestConnection.sendBroadcastForTrigger(mCtx,
            //    NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_CARD_TRIGGER, isEnable);

            NfcSettingAdapter.ExtRequestConnection.startActivityForTrigger(mCtx,
                NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_CARD_TRIGGER, isEnable);
            //[END][PORTING_FOR_NFCSETTING]
            return true;

        } else {
            //[START][PORTING_FOR_NFCSETTING]
            //showNfcOffDlg();
            NfcSettingAdapter.NfcUtils.setShowNfcOffDialog(true);
            NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_NFC_OFF);
            //[END][PORTING_FOR_NFCSETTING]
            return false;
        }
    }

    //[START][PORTING_FOR_NFCSETTING]
/*
    public static boolean isShowNfcOffDialog() {
        return sIsUseNfcOffDlg;
    }

    private void showNfcOffDlg() {
        // Create Alert Dialog
        Builder alertDlg = new AlertDialog.Builder(mCtx);
        alertDlg.setTitle(R.string.nfc_dlg_title_note);
        //AlertDlg.setMessage(R.string.sp_attention_disable_nfc_NORMAL);
        LayoutInflater inflater = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ScrollView dialogView = (ScrollView)inflater.inflate(R.layout.nfc_off_dlg, null);
        
        if (!Utils.isUI_4_1_model(mCtx)) {
            alertDlg.setIconAttribute(android.R.attr.alertDialogIcon);
            TextView textView = (TextView)dialogView.findViewById(R.id.nfc_off_text);
            textView.setTypeface(null, Typeface.BOLD);
        }
        alertDlg.setView(dialogView);
        alertDlg.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "[showNfcOffDlg] Click Positive Button.");
                mNfcAdapterAddon.setNfcPowerStatus(NfcAdapterAddon.NFC_CARD_OFF
                                                | NfcAdapterAddon.NFC_DISCOVERY_OFF
                                                | NfcAdapterAddon.NFC_P2P_OFF);
                sIsUseNfcOffDlg = false;
            }
        }).setNegativeButton(R.string.sp_cancel_NORMAL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "[showNfcOffDlg] Click Negative Button.");
                dialog.cancel();

            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "[showNfcOffDlg] Cancel");
                sIsUseNfcOffDlg = false;
                
                if (mMainSwitchPreference != null) {
                    // [MLN][NFC-1245]
                    if (mNfcAdapterAddon.isNfcSystemEnabled()) {
                        // [MLM][NFC-812][START]
                        mMainSwitchPreference.setChecked(true);
                        // [MLM][NFC-812][END]
                    }
                } else {
                    Log.e(TAG, "[showNfcOffDlg] mMainSwitchPreference is null...");
                }
            }
        }).show();

        sIsUseNfcOffDlg = true;
    }
    */
  //[END][PORTING_FOR_NFCSETTING]
    
}
