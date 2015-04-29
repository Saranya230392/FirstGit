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
import android.util.Log;
import android.widget.EditText;
import android.text.InputType;
import android.widget.Toast;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Paint;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * Dialog to show WPS progress.
 */
public class WpsApDialog extends AlertDialog {

    private final static String TAG = "WpsApDialog";

    private View mView;
    private TextView mTextView;
    private EditText mEditView;
    private ImageView mImageView;
    private ProgressBar mTimeoutBar;
    private ProgressBar mProgressBar;
    private Button mButton;
    private Timer mTimer;

    private static final int WPS_TIMEOUT_S = 120;

    private int mWpsSetup;
    private String mWpspin;

    private final IntentFilter mFilter;
    private BroadcastReceiver mReceiver;

    private Context mContext;
    private Handler mHandler = new Handler();

    private final DialogInterface.OnClickListener mListener;
//    private final DialogInterface.OnClickListener mPinListener;

    private enum DialogState {
        WPS_INIT,
        WPS_START,
        WPS_COMPLETE,
        CONNECTED, //WPS + IP config is done
        WPS_FAILED
    }
    DialogState mDialogState = DialogState.WPS_INIT;

    private static final int WPS_AP_PBC = 0;
    private static final int WPS_AP_DISPLAY = 1; // WPA-PIN
    private static final int WPS_AP_NFC = 2; // WPA-NFC

    public WpsApDialog(Context context, int wpsSetup ) {
        super(context);
        mContext = context;
        mWpsSetup = wpsSetup;

/*
        class WpsListener implements WifiManager.WpsListener {
            public void onStartSuccess(String pin) {
                if (pin != null) {
                    updateDialog(DialogState.WPS_START, String.format(
                            mContext.getString(R.string.wifi_wps_ap_onstart_pin), pin));
                } else {
                    updateDialog(DialogState.WPS_START, mContext.getString(
                            R.string.wifi_wps_onstart_pbc));
                }
            }
            public void onCompletion() {
                updateDialog(DialogState.WPS_COMPLETE,
                        mContext.getString(R.string.wifi_wps_complete));
            }

            public void onFailure(int reason) {
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
                        msg = mContext.getString(R.string.wifi_wps_failed_generic);
                        break;
                }
                updateDialog(DialogState.WPS_FAILED, msg);
            }
        }
*/

        mFilter = new IntentFilter();
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_PBC_ACTIVE");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_DISABLE");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_TIMEOUT");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_SUCCESS");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_FAIL");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_REG_SUCCES");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_STATION_ASSOC");
        mFilter.addAction("com.lge.wifi.sap.WIFI_SAP_STATION_DISASSOC");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
/*
        mPinListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                    String PinNum = mEditView.getText().toString();
                    int StringLen = mEditView.length();

                    if (PinNum == null)  {
                        Toast.makeText(getContext().getApplicationContext(),
                                "PIN empty!! Must input PIN for the WPS PIN",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    } else if (StringLen <8) {
                        Toast.makeText(getContext().getApplicationContext(),
                                "PIN Length Error! Must input 8 Length",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mWpspin = PinNum;
                        Log.d(TAG, "WPS: WPS_PIN =  " + mWpspin);
                        if (mWpspin != null) {
                            updateDialog(DialogState.WPS_START, String.format(
                                    mContext.getString(R.string.wifi_wps_ap_onstart_pin), mWpspin));
                        }
                        com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().setWpsPin(mWpspin,0);
                        Log.d(TAG, "WPS :  WPS_AP_PIN onClick");
                        wps_pin_run();
                        TimerRun();
                   }
            }
        };
*/


        mListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        };
    }
    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_wps_dialog, null);

        /* Title */
        setTitle(R.string.wifi_wps_setup_title);

        mTextView = (TextView) mView.findViewById(R.id.wps_dialog_txt);
        mTextView.setText(R.string.wifi_wps_setup_msg);
        mEditView = (EditText) mView.findViewById(R.id.wps_dialog_edit);
        mImageView = (ImageView) mView.findViewById(R.id.wps_icon);

      if (mWpsSetup == WPS_AP_DISPLAY) {
          Paint Paint = new Paint();

          mTextView.setText(R.string.wifi_wps_pin_input_msg);
          mEditView.setEnabled(true);
          mEditView.setVisibility(View.VISIBLE);
          mEditView.setInputType(InputType.TYPE_CLASS_NUMBER);
          mEditView.requestFocus();
          mImageView.setClickable(true);
          mImageView.setOnClickListener(new View.OnClickListener() {
              @Override
                  public void onClick(View v) {
                        String PinNum = mEditView.getText().toString();
                        int StringLen = mEditView.length();

                        if (PinNum == null)  {
                            Toast.makeText(getContext().getApplicationContext(),
                                    "PIN empty!! Must input PIN for the WPS PIN",
                                    Toast.LENGTH_SHORT).show();
                        } else if (StringLen <8) {
                            Toast.makeText(getContext().getApplicationContext(),
                                    "PIN Length Error! Must input 8 Length",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mWpspin = PinNum;
                            Log.d(TAG, "WPS: WPS_PIN =  " + mWpspin);
                            if (mWpspin != null) {
                                updateDialog(DialogState.WPS_START, String.format(
                                        mContext.getString(R.string.wifi_wps_ap_onstart_pin), mWpspin));
                            }
                            com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().setWpsPin(mWpspin,0);
                            Log.d(TAG, "WPS :  WPS_AP_PIN onClick");
                            wps_pin_run();
                            TimerRun();
                       }
                }
            });
          //mTextView.setPaintFlags(mTextView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

              Toast.makeText(getContext().getApplicationContext(),
                      "Click the WPS Icon after Entering PIN number to the next STEP",
                      Toast.LENGTH_SHORT).show();
      } else {
           mEditView.setEnabled(false);
           mEditView.setVisibility(View.GONE);
           mImageView.setClickable(false);
      }

        if (mWpsSetup == WPS_AP_DISPLAY) {
            mTimeoutBar = ((ProgressBar) mView.findViewById(R.id.wps_timeout_bar));
            mTimeoutBar.setMax(WPS_TIMEOUT_S);
            mTimeoutBar.setProgress(0);
            mTimeoutBar.setVisibility(View.GONE);
        } else {
            mTimeoutBar = ((ProgressBar) mView.findViewById(R.id.wps_timeout_bar));
            mTimeoutBar.setMax(WPS_TIMEOUT_S);
            mTimeoutBar.setProgress(0);
        }

        mProgressBar = ((ProgressBar) mView.findViewById(R.id.wps_progress_bar));
        mProgressBar.setVisibility(View.GONE);

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
        setView(mView);
        super.onCreate(savedInstanceState);
    }

    protected void wps_pin_run() {
       mEditView.setVisibility(View.GONE);
       mEditView.setEnabled(false);
       mTimeoutBar.setVisibility(View.VISIBLE);
       mImageView.setClickable(false);
    }

    protected void TimerRun() {
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
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected void onStart() {

        mContext.registerReceiver(mReceiver, mFilter);
        if (mWpsSetup == WPS_AP_DISPLAY) {
            Log.d(TAG, "WPS :  WPS_AP_PIN onStart");
        } else if (mWpsSetup == WPS_AP_PBC) {
            TimerRun();
            com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().setWpsPbc();
            updateDialog(DialogState.WPS_START, mContext.getString(
                    R.string.wifi_wps_onstart_pbc_ver2));
            Log.d(TAG, "WPS: WPS_AP_PBC on running");
        }  else {  // WPS_AP_NFC
            Log.d(TAG, "WPS :  WPS_AP_NFC on running");
        }
    }

    @Override
    protected void onStop() {
        if (mDialogState != DialogState.WPS_COMPLETE) {
            Log.d(TAG, " [onStop] WPS : CANCEL WPS");
            com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().setWpsCancel();
        }

        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void updateDialog(final DialogState state, final String msg) {
        if (mDialogState.ordinal() >= state.ordinal()) {
            //ignore.
            return;
        }
        mDialogState = state;

        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch(state) {
                        case WPS_COMPLETE:
                            mTimeoutBar.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.VISIBLE);
                            break;
                        case CONNECTED:
                        case WPS_FAILED:
                            //mButton.setText(mContext.getString(R.string.dlg_ok));
                            if (getButton(DialogInterface.BUTTON_POSITIVE) != null) { //WBT 540522 2013.12.24 eunjungjudy.kim
                                getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.dlg_ok);
                            }
                            mTimeoutBar.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            if (mReceiver != null) {
                                mContext.unregisterReceiver(mReceiver);
                                mReceiver = null;
                            }
                            break;
                    }
                    Paint Paint = new Paint();
                    mTextView.setText(msg);
                    //mTextView.setPaintFlags(mTextView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }
            });
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("com.lge.wifi.sap.WIFI_SAP_WPS_PBC_ACTIVE")) {
            updateDialog(DialogState.WPS_START, mContext.getString(R.string.wifi_wps_onstart_pbc_ver2));
        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_TIMEOUT")) {

        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_SUCCESS")) {
            updateDialog(DialogState.WPS_COMPLETE,  mContext.getString(R.string.wifi_wps_complete));
        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_FAIL")) {
            updateDialog(DialogState.WPS_FAILED, mContext.getString(R.string.wifi_wps_failed_generic_ver2));
        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_WPS_EVENT_REG_SUCCES")) {

        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_STATION_ASSOC")) {
            String msg = String.format(mContext.getString(
                    R.string.wifi_wps_connected), "TEST SSID : Station");
            updateDialog(DialogState.CONNECTED, msg);
        } else if (action.equals("com.lge.wifi.sap.WIFI_SAP_STATION_DISASSOC")) {

        }
/*
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            final NetworkInfo.DetailedState state = info.getDetailedState();
            if (state == DetailedState.CONNECTED &&
                    mDialogState == DialogState.WPS_COMPLETE) {
                WifiInfo wifiInfo = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if (wifiInfo != null) {
                    String msg = String.format(mContext.getString(
                            R.string.wifi_wps_connected), wifiInfo.getSSID());
                    updateDialog(DialogState.CONNECTED, msg);
                }
            }
        }
*/
    }

}
