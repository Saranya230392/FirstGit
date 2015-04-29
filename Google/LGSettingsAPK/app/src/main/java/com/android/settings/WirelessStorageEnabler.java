package com.android.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.Switch;

public class WirelessStorageEnabler implements CompoundButton.OnCheckedChangeListener {

    private final Context mContext;
    private Switch mSwitch;
    private boolean isConnected = false;
    private boolean hotSpotConnected = false;
    private final IntentFilter mIntentFilter;
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo mNetworkInfo;
    private String TAG = "WirelessStorage";

    private boolean mInternalChecked = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            try {
                if (((String)WifiManager.class.getField("WIFI_AP_STATE_CHANGED_ACTION").get(null))
                        .equals(action)) {
                    int state = intent.getIntExtra(
                            (String)WifiManager.class.getField("EXTRA_WIFI_AP_STATE").get(null), 0);
                    if (state == WifiManager.class.getField("WIFI_AP_STATE_ENABLED").getInt(
                            WifiManager.class)) {
                        hotSpotConnected = true;
                    } else if (state == WifiManager.class.getField("WIFI_AP_STATE_DISABLED")
                            .getInt(WifiManager.class)) {
                        hotSpotConnected = false;
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            handleWirelessStateChanged(action);
            Log.i(TAG, "WirelessStorageEnabler - onReceive() : " + action);
        }
    };

    public WirelessStorageEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;

        mConnectivityManager = (ConnectivityManager)mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        mIntentFilter = new IntentFilter("com.lge.wirelessstorage.action.START_SERVER");
        mIntentFilter.addAction("com.lge.wirelessstorage.action.STOP_SERVER");
        mIntentFilter.addAction("com.lge.app.wirelessstorage.action.EXEPTIONAL_STOP_SERVER");
        mIntentFilter.addAction("com.lge.wirelessstorage.action.MEDIA_EJECT");
        mIntentFilter.addAction("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_ON");
        mIntentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        Log.i(TAG, "WirelessStorageEnabler - WirelessStorageEnabler() - isConnected :"
                + isConnected);
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_, boolean wireless_state) {
        if (mSwitch == switch_) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        // [SWITCH_SOUND]
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            }
        });

        mSwitch.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                mInternalChecked = false;
                return false;
            }
        });

        this.isConnected = wireless_state;
        mSwitch.setChecked(isConnected);
        Log.i(TAG, "WirelessStorageEnabler - setSwitch() - isConnected : " + isConnected);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "WirelessStorageEnabler - onCheckedChanged() isChecked: " + isChecked);
        if (isChecked) {
            mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            Log.i(TAG, "WirelessStorageEnabler - onCheckedChanged() - hotSpotConnected : "
                    + hotSpotConnected);
            Log.i(TAG,
                    "WirelessStorageEnabler - onCheckedChanged() - mNetworkInfo.isConnected() : "
                            + mNetworkInfo.isConnected());
            if (!hotSpotConnected && !mNetworkInfo.isConnected() && !isConnected) {
                mSwitch.setChecked(false);
                mInternalChecked = true;
            }
            Intent startServer = new Intent("com.lge.wirelessstorage.action.START_SERVER");
            mContext.sendBroadcast(startServer);
        } else {
            if (!mInternalChecked) {
                mSwitch.setChecked(true);
                mInternalChecked = true;
                new AlertDialog.Builder(mContext)
                        //NFS : yunha.gyeong, dismiss AlertDialog by network disconnection
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(
                                mContext.getResources().getString(
                                        R.string.sp_disconnect_attention_title_NORMAL))
                        .setMessage(
                                mContext.getResources().getString(
                                        R.string.sp_disconnect_attention_WS_NORMAL))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent stopServer = new Intent(
                                        "com.lge.wirelessstorage.action.STOP_SERVER");
                                mContext.sendBroadcast(stopServer);
                                mSwitch.setChecked(false);
                                mInternalChecked = true;
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                                mSwitch.setChecked(true);
                                mInternalChecked = true;
                            }
                        })
                        .show();
            }
        }
    }

    private void handleWirelessStateChanged(String action) {
        Log.i(TAG, "WirelessStorageEnabler - handleWirelessStateChanged() - action : " + action);
        Log.i(TAG, "WirelessStorageEnabler - handleWirelessStateChanged() - isConnected : "
                + isConnected);
        Log.i(TAG, "WirelessStorageEnabler - handleWirelessStateChanged() - hotSpotConnected : "
                + hotSpotConnected);
        if ("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_ON".equals(action)) {
            isConnected = true;
            mSwitch.setChecked(true);
        } else if ("com.lge.wirelessstorage.action.STOP_SERVER".equals(action)
                || "com.lge.app.wirelessstorage.action.EXEPTIONAL_STOP_SERVER".equals(action)
                || "com.lge.wirelessstorage.action.MEDIA_EJECT".equals(action)) {
            isConnected = false;
            mSwitch.setChecked(false);
        }
    }
}
