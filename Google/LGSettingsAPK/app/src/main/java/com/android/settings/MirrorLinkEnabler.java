package com.android.settings;

import android.os.IBinder;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MirrorLinkEnabler implements CompoundButton.OnCheckedChangeListener{

    private String TAG = "MirrorLinkEnabler";
    private final Context mContext;
    private Switch mSwitch;

    private IBinder mMlsBinder = null;

    private final ServiceConnection mMlsConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected with CommonAPIService");
            mMlsBinder = service;
            if ( mMlsBinder != null ) {
                mSwitch.setChecked(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected with CommonAPIService");
            mMlsBinder = null;

            mSwitch.setChecked(false);
        }

    };

    public MirrorLinkEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;

    }


    public void resume() {
        Intent intent2 = new Intent("com.mirrorlink.android.service.BIND");
        intent2.setClassName("com.lge.mirrorlink", "com.lge.mirrorlink.commonapi.CommonAPIService");
        if ( mContext.bindService(intent2, mMlsConn, 0) == false ) {
            mSwitch.setChecked(false);
        }
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
        if ( mMlsBinder != null ) {
            mContext.unbindService(mMlsConn);
        }
    }

    public void setSwitch(Switch switch_, boolean wireless_state) {
        if (mSwitch == switch_) {
            return;
        }
        Log.i(TAG, "MirrorLinkEnabler - setSwitch() - isConnected ");
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

        //this.isConnected = wireless_state;
        //mSwitch.setChecked(wireless_state);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "MirrorLinkEnabler - onCheckedChanged() isChecked: "+isChecked);
        if (isChecked) {
            Intent intent_start = new Intent("com.mirrorlink.android.app.LAUNCH");
            mContext.startService(intent_start);
        } else {
            Intent intent_stop = new Intent("com.mirrorlink.android.app.TERMINATE");
            mContext.stopService(intent_stop);
        }
    }

}
