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

public class NfcP2pBeamTrigger implements NfcTriggerIf {

    static final String TAG = "NfcCardTrigger";

    private Context mCtx;
    private NfcAdapterAddon mNfcAdapterAddon;
    private boolean mIsBeamInP2p;

    public NfcP2pBeamTrigger(Context ctx, boolean isBeamInP2p) {
        mCtx = ctx;
        mIsBeamInP2p = isBeamInP2p;
        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
    }

    @Override
    //[START][PORTING_FOR_NFCSETTING]
    public boolean trigger(boolean isEnable) {
        // TODO :: need to check whether mNfcAdapterAddon is null or not.
        if (mIsBeamInP2p) {
            if (mNfcAdapterAddon.isNfcP2pModeEnabled() && isEnable == false) {
                //[START][PORTING_FOR_NFCSETTING]
                NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                    NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_BEAM_OFF);
                //showBeamOffDlg();
                //[END][PORTING_FOR_NFCSETTING]
                return false;
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76][ID-MDM-307]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().checkDisallowNfc(MDMSettingsAdapter.LGMDM_NFC_ANDROID_BEAM)) {
            Log.i(TAG, "NfcCardTrigger MDM Blcok Android beam");
            return false;
        }
        // LGMDM_END

        //NfcSettingAdapter.ExtRequestConnection.sendBroadcastForTrigger(mCtx,
        //        NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_P2P_BEAM_TRIGGER, isEnable);
        NfcSettingAdapter.ExtRequestConnection.startActivityForTrigger(mCtx,
                NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_P2P_BEAM_TRIGGER, isEnable);
       return true;
    }

/*
    public boolean trigger(boolean isEnable) {
        boolean result = false;

        // TODO :: need to check whether mNfcAdapterAddon is null or not.
        if (mIsBeamInP2p) {
            if (mNfcAdapterAddon.isNfcP2pModeEnabled() && isEnable == false) {
                //[START][PORTING_FOR_NFCSETTING]
                NfcSettingAdapter.ExtRequestConnection.startActivityForPopup(mCtx,
                    NfcSettingAdapter.ExtRequestConnection.REQUEST_POPUP_NFC_DLG_IN_BEAM_OFF);
                //showBeamOffDlg();
                //[END][PORTING_FOR_NFCSETTING]
                return false;
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-76][ID-MDM-307]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && MDMSettingsAdapter.getInstance().checkDisallowNfc(MDMSettingsAdapter.LGMDM_NFC_ANDROID_BEAM)) {
            Log.i(TAG, "NfcCardTrigger MDM Blcok Android beam");
            return false;
        }
        // LGMDM_END


        //[START][PORTING_FOR_NFCSETTING]
        NfcSettingAdapter.ExtRequestConnection.sendBroadcastForTrigger(mCtx,
                NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_P2P_BEAM_TRIGGER, isEnable);
        //if (isEnable) {
        //    result = mNfcAdapterAddon.setNfcPowerStatus(NfcAdapterAddon.NFC_P2P_ON);
        //    Log.d(TAG, "mNfcAdapter.enableNdefPush() = " + result);
        //} else {
        //    result = mNfcAdapterAddon.setNfcPowerStatus(NfcAdapterAddon.NFC_P2P_OFF);
        //    Log.d(TAG, "mNfcAdapter.disableNdefPush() = " + result);
        //}        
        //[END][PORTING_FOR_NFCSETTING]
       return result;
    }
*/
  //[END][PORTING_FOR_NFCSETTING]


//[START][PORTING_FOR_NFCSETTING]
/*
    private void showBeamOffDlg() {
        // Create Alert Dialog
        Builder alertDlg = new AlertDialog.Builder(mCtx);
        alertDlg.setTitle(R.string.nfc_dlg_title_note);
        
        if (!Utils.isUI_4_1_model(mCtx)) {
            alertDlg.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        
        // wonjong77.lee [2014.08.23] no longer use directbeam...
        alertDlg.setMessage(R.string.sp_toast_body_beam_off_NORMAL);
        
        alertDlg.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mNfcAdapterAddon.setNfcPowerStatus(NfcAdapterAddon.NFC_P2P_OFF);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.sp_cancel_NORMAL, null)
                //       .setIconAttribute(android.R.attr.alertDialogIcon)
                .show();
    }
*/
//[END][PORTING_FOR_NFCSETTING]
}
