/*
 * Copyright (C) 2010 LG Electronics, Inc.  All Rights Reserved.
 * Copyright (C) 2009 The Android Open Source Project
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
/**
 * @author Agathon.Jung@lge.com
 * @version $Revision$
 */

package com.android.settings;

import com.android.settings.DataUsageSummary;
import com.android.settings.R;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.TelephonyIntents;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.lge.constants.SettingsConstants;
import android.provider.Settings;

/**
 * DataRoamingOptionRequest is a receiver for any Mobile network request. It
 * checks if the mobile network settings is currently visible and brings up the
 * confirmation dialog. Otherwise it puts a Notification in the status bar, which can
 * be clicked to bring up the confirmation dialog.
 */
public class DataRoamingOptionRequest extends BroadcastReceiver {

    private static final String TAG = "LGE_data";
    private boolean LogEnable = true;

    private String ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_REQUEST";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_CANCEL";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN = "android.intent.action.ENABLE_DATA_IN_HPLMN";
    private String REQUEST_ROAMING_OPTION = "requestRoamingOption";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        int isPopupChecked = Settings.Secure.getInt(context.getContentResolver(),
                SettingsConstants.Secure.DATA_AUTO_ON_POPUP_HPLMN,
                0);

        if (action.equals(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST)) {

            if (LogEnable) {
                Log.e(TAG,
                        "DataRoamingOptionRequest:onReceive(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST)");
            }
            int lastRoamingOption = intent.getIntExtra(REQUEST_ROAMING_OPTION, 0);

            Intent authIntent = new Intent();
            authIntent.setClass(context, DataRoamingOptionDialog.class);
            authIntent.setAction(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST);
            authIntent.putExtra(REQUEST_ROAMING_OPTION, lastRoamingOption);
            authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(authIntent);

        } else if (action.equals(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN)) {

            if (LogEnable) {
                Log.e(TAG,
                        "DataRoamingOptionRequest:onReceive(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN)");
            }
            int lastRoamingOption = intent.getIntExtra(REQUEST_ROAMING_OPTION, 0);

            if (isPopupChecked == 0) {
                Intent authIntent = new Intent();
                authIntent.setClass(context, DataRoamingOptionDialog.class);
                authIntent.setAction(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN);
                authIntent.putExtra(REQUEST_ROAMING_OPTION, lastRoamingOption);
                authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(authIntent);
            }

        } else if (action.equals(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL)) {

            if (LogEnable) {
                Log.e(TAG,
                        "DataRoamingOptionRequest:onReceive(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL)");
            }
        }
    }

}
