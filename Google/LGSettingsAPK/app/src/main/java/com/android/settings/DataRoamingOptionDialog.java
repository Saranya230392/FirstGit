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
import android.widget.*;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
import com.android.settings.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

//import com.android.internal.telephony.TelephonyIntents;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import android.content.res.Resources;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.lge.constants.SettingsConstants;
import android.provider.Settings;

//import com.android.internal.telephony.DataCallConfig; // for  DataCallConfig
/**
 * DataRoamingOptionDialog asks the user to confirm that they approve automatic
 * connecting roaming network.
 */

public class DataRoamingOptionDialog extends AlertActivity implements
        DialogInterface.OnClickListener {

    private static final String TAG = "LGE_data";
    private boolean DEBUG = true;

    private int lastRoamingOption = 0;
    private ListenForCancel mNetworkReceiver;
    private IntentFilter mIntentFilter;

    private String ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_REQUEST";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL = "android.intent.action.MOBILE_DATA_ROAMING_OPTION_CANCEL";
    private String ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN = "android.intent.action.ENABLE_DATA_IN_HPLMN";
    private String REQUEST_ROAMING_OPTION = "requestRoamingOption";

    private String ACTION_MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST = "android.intent.action.MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST";
    /* [S][2012-08-31][jisu.kim@lge.com] , ADD, Data Roaming popup finish reponse*/
    private String ACTION_MOBILE_DATA_IS_ON_RESPONSE = "android.intent.action.MOBILE_DATA_IS_ON_RESPONSE";
    /* [E][2012-08-31][jisu.kim@lge.com] , ADD, Data Roaming popup finish reponse*/
    private String REQUEST_STATE = "requestState";

    private Intent mIntent;

    private static int disableRoamingPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) {
            Log.v(TAG, "onCreate started");
        }

        mIntent = getIntent();

        mNetworkReceiver = new ListenForCancel();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL);
        this.getBaseContext().registerReceiver(mNetworkReceiver, mIntentFilter);

        final AlertController.AlertParams p = mAlertParams;

        if (p != null) {

            if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST)) {
                Log.v(TAG, "DataRoamingOptionDialog : ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST");

                lastRoamingOption = mIntent.getIntExtra(REQUEST_ROAMING_OPTION, 0);

                p.mTitle = getString(R.string.roaming);
                View view = getLayoutInflater().inflate(R.layout.mobilenetwork_authorize_request,
                        null);
                TextView messageView = (TextView)view.findViewById(R.id.message1);
                messageView.setText(getString(R.string.sp_data_popup_window_show_roaming));

                p.mView = view;
                p.mPositiveButtonText = getString(R.string.yes);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonText = getString(R.string.no);
                p.mNegativeButtonListener = this;

            } else if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN)) {
                Log.v(TAG, "DataRoamingOptionDialog : ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN");

                lastRoamingOption = mIntent.getIntExtra(REQUEST_ROAMING_OPTION, 0);

                //p.mTitle = getString(R.string.sp_data_on_HPLMN_popup);
                View view = getLayoutInflater().inflate(R.layout.mobilenetwork_authorize_request,
                        null);
                TextView messageView = (TextView)view.findViewById(R.id.message2);
                CheckBox cb = (CheckBox)view.findViewById(R.id.disable_roaming_popup);
                cb.setVisibility(cb.VISIBLE);
                disableRoamingPopup = 0;

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if (isChecked) {
                            disableRoamingPopup = 1;
                        } else {
                            disableRoamingPopup = 0;
                        }
                    }
                });

                messageView.setText(getString(R.string.sp_data_popup_hplmn));

                p.mView = view;
                p.mPositiveButtonText = getString(R.string.dlg_ok);
                p.mPositiveButtonListener = this;

            }

            setupAlert();
        }
        if (DEBUG) {
            Log.v(TAG, "onCreate finished");
        }
    }

    private void onAuthorize() {
        if (DEBUG) {
            Log.v(TAG, "onAuthorize called");
        }

        if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST)) {
            if (DEBUG) {
                Log.v(TAG, "onAuthorize called : ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST");
            }
            onConnectNetworkSelect(ACTION_MOBILE_DATA_ROAMING_OPTION_REQUEST, lastRoamingOption);

            Intent dataIntent = new Intent(ACTION_MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST);
            dataIntent.putExtra(REQUEST_STATE, 1);
            sendBroadcast(dataIntent);
            /* [S][2012-08-31][jisu.kim@lge.com] , ADD, Data Roaming popup finish reponse*/
        } else if (mIntent.getAction().equals(ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN)) {
            if (DEBUG) {
                Log.v(TAG, "getAction: ACTION_MOBILE_DATA_ROAMING_OPTION_HPLMN");
            }
            Intent dataIntent = new Intent(ACTION_MOBILE_DATA_IS_ON_RESPONSE);
            sendBroadcast(dataIntent);
        }
        /* [E][2012-08-31][jisu.kim@lge.com] , ADD, Data Roaming popup finish reponse*/
    }

    private void onDecline() {
        if (DEBUG) {
            Log.v(TAG, "onDecline called : ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL");
        }

        onConnectNetworkSelect(ACTION_MOBILE_DATA_ROAMING_OPTION_CANCEL, lastRoamingOption);

        Intent dataIntent = new Intent(ACTION_MOBILE_DATA_ROAMING_STATE_CHANGE_REQUEST);
        dataIntent.putExtra(REQUEST_STATE, 0);
        sendBroadcast(dataIntent);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (DEBUG) {
            Log.v(TAG, "onClick called");
        }
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            Settings.Secure.putInt(getApplicationContext().getContentResolver(),
                    SettingsConstants.Secure.DATA_AUTO_ON_POPUP_HPLMN,
                    disableRoamingPopup);

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

    public void onConnectNetworkSelect(String action, int roamingoption) {

        Intent authIntent = new Intent();
        authIntent.setClass(this, DataRoamingOptionDialogResult.class);
        authIntent.setAction(action);
        authIntent.putExtra(REQUEST_ROAMING_OPTION, roamingoption);
        authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(authIntent);
    }

}
