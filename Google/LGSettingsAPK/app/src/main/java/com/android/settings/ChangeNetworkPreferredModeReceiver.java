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

package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.lgesetting.Config.Config;

public class ChangeNetworkPreferredModeReceiver extends BroadcastReceiver {

    /** known host define here */
    private Phone mPhone;
    static final int PREFERRED_NETWORKMODE = Phone.PREFERRED_NT_MODE;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Bundle extra = intent.getExtras();

        boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (false == mIsOwner) {
            Log.d("ChangeNetworkPreferredModeReceiver", "return under secondary user.");
            return;
        }

        //add the network selection for setupwizard.
        mPhone = PhoneFactory.getDefaultPhone();
        int nwMode = PREFERRED_NETWORKMODE;
        if (intent.getAction().equals("com.lge.settings.action.CHANGE_NETWORK_MODE")) {
            if ("USC".equals(Config.getOperator())) {
                nwMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        PREFERRED_NETWORKMODE);
            }
            
            mPhone.setPreferredNetworkType(nwMode, null);
            
            Log.d("ChangeNetworkPreferredModeReceiver", " setPreferredNetworkType = " + Integer.toString(nwMode));
        } else if (intent.getAction().equals("com.lge.settings.action.CHANGE_ROAMING_MODE")) {
            boolean isRoaming = intent.getBooleanExtra("is_roaming_onoff", false);
            nwMode = intent.getIntExtra("change_networkmode", 0);
            
            if ( nwMode >= 0 ) {
                mPhone.setPreferredNetworkType(nwMode, null);
            }
            
            Log.d("ChangeNetworkPreferredModeReceiver", " setPreferredNetworkType = " + Integer.toString(nwMode));
            Log.d("ChangeNetworkPreferredModeReceiver", " setDataRoamingEnabled = " + isRoaming);
            
            mPhone.setDataRoamingEnabled(isRoaming);
        } else if (intent.getAction().equals("com.lge.settings.action.RADIO_SET")) {
            boolean isSet = true;
            
            if (extra != null) {
                isSet = extra.getBoolean("state");
            }
            
            Log.d("CryptKeeper", " isSet = " + isSet);
            
            mPhone = PhoneFactory.getDefaultPhone();
            mPhone.setRadioPower(isSet);
        }
    }
}
