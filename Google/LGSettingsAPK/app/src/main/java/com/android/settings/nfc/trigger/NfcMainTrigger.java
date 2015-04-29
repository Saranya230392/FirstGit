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
import android.view.LayoutInflater;

import com.android.settings.Utils;
import com.android.settings.nfc.*;
import com.android.settings.R;
import com.android.settings.MDMSettingsAdapter;

import com.lge.nfcaddon.NfcAdapterAddon;
import android.util.Log;

public class NfcMainTrigger implements NfcTriggerIf {

    static final String TAG = "NfcMainTrigger";

    private Context mCtx;
    private NfcAdapterAddon mNfcAdapterAddon;

    public NfcMainTrigger(Context ctx) {
        mCtx = ctx;
        mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
    }

    @Override
    //[START][PORTING_FOR_NFCSETTING]
    public boolean trigger(boolean isEnable) {

        if (isEnable) {
            if (mNfcAdapterAddon.isWirelessChargingModeOn() == true) {
                Toast.makeText(mCtx, R.string.nfc_toast_cannot_turnon_during_charging,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        //NfcSettingAdapter.ExtRequestConnection.sendBroadcastForTrigger(mCtx,
        //        NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_MAIN_TRIGGER, isEnable);
        NfcSettingAdapter.ExtRequestConnection.startActivityForTrigger(mCtx,
                        NfcSettingAdapter.ExtRequestConnection.SEND_BROADCAST_TYPE_FOR_MAIN_TRIGGER, isEnable);

        return true;
    }

/*
    public boolean trigger(boolean isEnable) {
        boolean result = false;

        if (isEnable) {
            if (mNfcAdapterAddon.isWirelessChargingModeOn() == true) {
                Toast.makeText(mCtx, R.string.nfc_toast_cannot_turnon_during_charging,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            
            int nfcPowerStatus = NfcAdapterAddon.NFC_CARD_ON | NfcAdapterAddon.NFC_DISCOVERY_ON;
            if (Utils.isWifiOnly(mCtx)) {
                nfcPowerStatus |= NfcAdapterAddon.NFC_P2P_ON;
            }
            result = mNfcAdapterAddon.setNfcPowerStatus(nfcPowerStatus);
            Log.d(TAG, "mNfcAdapterAddon.setNfcPowerStatus(" + nfcPowerStatus + ") - " + result);
         } else { 

            int nfcPowerStatus = NfcAdapterAddon.NFC_CARD_OFF | NfcAdapterAddon.NFC_DISCOVERY_OFF;
            if (Utils.isWifiOnly(mCtx)) {
                nfcPowerStatus |= NfcAdapterAddon.NFC_P2P_OFF;
            }
            result = mNfcAdapterAddon.setNfcPowerStatus(nfcPowerStatus);
            Log.d(TAG, "mNfcAdapterAddon.setNfcPowerStatus(" + nfcPowerStatus + ") - " + result);
        }
        
        return result;
    }
*/
  //[END][PORTING_FOR_NFCSETTING]

}
