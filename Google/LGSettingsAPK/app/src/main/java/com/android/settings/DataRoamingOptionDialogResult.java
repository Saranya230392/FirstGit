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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

//import com.android.internal.telephony.TelephonyIntents;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import android.content.res.Resources;

//import com.android.internal.telephony.DataCallConfig; // for  DataCallConfig
/**
 * DataRoamingOptionDialog asks the user to confirm that they approve automatic
 * connecting roaming network.
 */

public class DataRoamingOptionDialogResult extends AlertActivity implements
        DialogInterface.OnClickListener {

    private static final String TAG = "LGE_data";
    private boolean DEBUG = true;

    //private int lastRoamingOption = 0;
    private ListenForCancel mNetworkReceiver;
    private IntentFilter mIntentFilter;

    private String ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_REQUEST";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_CANCEL";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN = "android.intent.action.ENABLE_DATA_IN_HPLMN";
    //private String REQUEST_ROAMING_OPTION = "requestRoamingOption";

    private String ACTION_MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST = "android.intent.action.MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST";
    private String REQUEST_STATE = "requestState";

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) {
            Log.v(TAG, "DataRoamingOptionDialogResult : onCreate started");
        }
        mIntent = getIntent();

        mNetworkReceiver = new ListenForCancel();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL);
        this.getBaseContext().registerReceiver(mNetworkReceiver, mIntentFilter);

        //lastRoamingOption = mIntent.getIntExtra(REQUEST_ROAMING_OPTION, 0);

        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.roaming);
        View view = getLayoutInflater().inflate(R.layout.mobilenetwork_authorize_request, null);
        TextView messageView = (TextView)view.findViewById(R.id.message1);

        if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST)) {
            messageView.setText(getString(R.string.sp_data_popup_choose_data_on));
        } else if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL)) {
            messageView.setText(getString(R.string.sp_data_popup_choose_data_off));
        } else if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN)) {
            messageView.setText(getString(R.string.sp_data_popup_hplmn));
        }

        p.mView = view;
        p.mPositiveButtonText = getString(R.string.dlg_ok);
        p.mPositiveButtonListener = this;

        setupAlert();
        if (DEBUG) {
            Log.v(TAG, "onCreate finished");
        }
    }

    private void onAuthorize() {
        if (DEBUG) {
            Log.v(TAG,
                    "DataRoamingOptionDialogResult onAuthorize() : ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST");
        }
        // update DataRoaming flag as user selection
        /*Intent dataIntent = new Intent("android.intent.action.ACTION_MOBILE_DATA_EXTFUNC_REQUEST");
        dataIntent.putExtra("reqFuncId", 326);       //FUNC_MMI_REQ_ROAMING_ON
        dataIntent.putExtra("reqIntParam", 1);        // bPSEnabled = 1
        dataIntent.putExtra("reqStringParam", (String)null);
        dataIntent.putExtra("reqAppId", 0);
        sendBroadcast(dataIntent);*/

    }

    private void onDecline() {
        if (DEBUG) {
            Log.v(TAG,
                    "DataRoamingOptionDialogResult onDecline() : ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST");
        }
        // update DataRoaming flag as user selection
        /*        Intent dataIntent = new Intent("android.intent.action.ACTION_MOBILE_DATA_EXTFUNC_REQUEST");
                dataIntent.putExtra("reqFuncId", 326);       //FUNC_MMI_REQ_ROAMING_ON
                dataIntent.putExtra("reqIntParam", 0);        // bPSEnabled = 0
                dataIntent.putExtra("reqStringParam", (String)null);
                dataIntent.putExtra("reqAppId", 0);
                sendBroadcast(dataIntent);*/

        Intent dataIntent = new Intent(ACTION_MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST);
        dataIntent.putExtra(REQUEST_STATE, 0);
        sendBroadcast(dataIntent);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (DEBUG) {
            Log.v(TAG, "DataRoamingOptionDialogResult : onClick called");
        }
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            onAuthorize();
            return;

        case DialogInterface.BUTTON_NEGATIVE:
            onDecline();
            return;
        default:
            return;
        }
    }

    protected void onDestroy() {
        this.getBaseContext().unregisterReceiver(mNetworkReceiver);
        super.onDestroy();
    }

    private void quitActivity()
    {
        finish();
    }

    private class ListenForCancel extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            quitActivity();
        }
    }

}
