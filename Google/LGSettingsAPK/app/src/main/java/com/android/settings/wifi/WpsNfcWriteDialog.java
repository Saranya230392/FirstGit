/*==========================================================================================================
CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com
WpsNfcWriteDialog.java : This file is for making WPS_NFC Dialog.
===========================================================================================================*/
/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Paint;
import com.android.settings.R;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * Dialog to show WPS progress.
 */
public class WpsNfcWriteDialog extends AlertDialog {

    private final static String TAG = "WpsNfcWriteDialog";

    private View mView;
    private TextView mTextView;
    private ProgressBar mTimeoutBar;
    //private ProgressBar mProgressBar;
    private Button mButton;
    private Timer mTimer;

    private static final int WPS_TIMEOUT_S = 60;
    private int mCount = 0;

    private WifiManager mWifiManager;
    private WifiManager.WpsCallback mWpsListener; // WpsListener -> WpsCallback
    private int mWpsSetup;

    //private final IntentFilter mFilter;
    //private BroadcastReceiver mReceiver;

    private Context mContext;
    private Handler mHandler = new Handler();

    private final DialogInterface.OnClickListener mListener;

    public WpsNfcWriteDialog(Context context, int wpsSetup) {
//        super(context);
        super(context, R.style.Theme_Holo_WifiDialog);
        mContext = context;
        mWpsSetup = wpsSetup;

        class WpsListener extends WifiManager.WpsCallback {
            public void onStarted(String pin) {
                if (pin != null) {
//                    updateDialog(DialogState.WPS_START, String.format(
//                            mContext.getString(R.string.wifi_wps_onstart_pin_ver2), pin));
                } else {
//                    updateDialog(DialogState.WPS_START, mContext.getString(
//                            R.string.wifi_wps_onstart_pbc_ver2));
                }
            }

            public void onSucceeded() {
                //updateDialog(DialogState.WPS_COMPLETE,
                //        mContext.getString(R.string.wifi_wps_complete));
            }

            public void onFailed(int reason) {
                String msg;
                switch (reason) {
                    case WifiManager.WPS_OVERLAP_ERROR:
                        msg = mContext.getString(R.string.wifi_wps_failed_overlap);
                        break;
                    case WifiManager.WPS_WEP_PROHIBITED:
                        msg = mContext.getString(R.string.wifi_wps_failed_wep);
                        break;
                    case WifiManager.WPS_TKIP_ONLY_PROHIBITED:
                        msg = mContext.getString(R.string.wifi_wps_failed_tkip);
                        break;
                    case WifiManager.IN_PROGRESS:
                        msg = mContext.getString(R.string.wifi_wps_in_progress);
                        break;
                    default:
                        msg = mContext.getString(R.string.wifi_wps_failed_generic_ver2);
                        break;
                }
                //updateDialog(DialogState.WPS_FAILED, msg);
            }
        }

        mWpsListener = new WpsListener();


        /*mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };*/

        mListener = new DialogInterface.OnClickListener() {
        
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        };
    }

    //[jaewoong87.lee@lge.com] 2013.06.03 Wps dialog is canceled only when touch cancel button[S]
    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(false);
    }
    //[jaewoong87.lee@lge.com] 2013.06.03 Wps dialog is canceled only when touch cancel button[E]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_wps_nfc_dialog, null);

        mTextView = (TextView) mView.findViewById(R.id.wps_dialog_txt);

        if (mWpsSetup == WpsNfcDialog.PWD_TOKEN_DIALOG_TYPE) {
            setTitle(R.string.wifi_wps_setup_title);
            mTextView.setText(R.string.wifi_wps_setup_msg);
        } else if (mWpsSetup == WpsNfcDialog.HANDOVER_DIALOG_TYPE) {
            setTitle("WPS NFC HANDOVER");            
            mTextView.setText("Trying Handover...");
        } else if (mWpsSetup == WpsNfcDialog.CONF_TOKEN_DIALOG_TYPE) {
            setTitle("WPS NFC WPS NFC CONFIG TOKEN");            
            mTextView.setText("Please tag with valid nfc tag.");
        }
        mTimeoutBar = ((ProgressBar) mView.findViewById(R.id.wps_timeout_bar));
        mTimeoutBar.setMax(WPS_TIMEOUT_S);
        mTimeoutBar.setProgress(0);

        mButton = ((Button) mView.findViewById(R.id.wps_dialog_btn));
        mButton.setText(R.string.wifi_cancel);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mButton.setVisibility(View.GONE);
        setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(R.string.wifi_cancel), mListener);

        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        setView(mView);
//        mContext.setTheme(R.style.Theme_Settings);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        /*
         * increment timeout bar per second.
         */
        mTimer = new Timer(false);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mTimeoutBar.incrementProgressBy(1);
                        mCount++;
                        if (mCount >= WPS_TIMEOUT_S) {
                            mTimer.cancel();
                            dismiss();
                        }
                    }
                });
            }
        }, 1000, 1000);

        //mContext.registerReceiver(mReceiver, mFilter);
        
        WpsInfo wpsConfig = new WpsInfo();
        wpsConfig.setup = mWpsSetup;
        mWifiManager.startWps(wpsConfig, mWpsListener);
    }

    @Override
    protected void onStop() {
        //if (mDialogState != DialogState.WPS_COMPLETE) {
        //    mWifiManager.cancelWps(null);
        //}

        /*if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }*/

        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();

    }

}
