/*
 * Lab A, Mobile Communication Company, LG ELECTRONICS INC., SEOUL, KOREA
 * Copyright(c) 2013 by LG Electronics Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LG Electronics Inc.
 */
package com.android.settings;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SmartShareBeamSwitchPreference extends SwitchPreference implements
        OnCheckedChangeListener {
    private static final String TAG = "SmartShareBeamEnabler";
    private static final String SERVICE_NAME = "com.lge.smartsharebeam.service.ReceiveService";

    private Context mContext = null;
    private Switch mSwitch = null;
    private boolean mOnDivider = true;
    private boolean mSwitchButtonChangedFromUser = true;

    public SmartShareBeamSwitchPreference(Context context) {
        this(context, null);
    }

    public SmartShareBeamSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mContext = context;
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    public void setListener() {
        // Do nothing
    }

    public void resume() {
        Log.d(TAG, "resume");
        if (mSwitch != null) {
            boolean isOn = isSmartShareBeamRunning();
            setState(isOn);
        }
    }

    public void pause() {
        Log.d(TAG, "pause");
        // Do nothing
    }

    @Override
    protected void onBindView(View view) {
        Log.d(TAG, "onBindView start");
        super.onBindView(view);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onBindView - onClick");
           }
        });

        boolean isOn = isSmartShareBeamRunning();
        Log.d(TAG, "onBindView isSmartShareBeamRunning :" + isOn);
        setState(isOn);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged-mSwitchButtonChanged: " + mSwitchButtonChangedFromUser);
        if (mSwitchButtonChangedFromUser == false) {
            mSwitchButtonChangedFromUser = true;
            return;
        }
        setEnabled(false);
        if (isChecked) {
            startReceiverService();
        } else {
            stopReceiverService();
        }
    }

    private void startReceiverService() {
        Log.d(TAG, "call start smartshare beam service");
        Intent intent = new Intent("com.lge.smartsharebeam.on");
        ComponentName component = new ComponentName("com.lge.smartsharepush",
                "com.lge.smartsharebeam.LGSmartShareBeam");
        intent.setComponent(component);
        intent.setPackage("com.android.settings");
        mContext.startService(intent);
    }

    private void stopReceiverService() {
        Log.d(TAG, "call stop smartshare beam service");
        mContext.sendBroadcast(new Intent("com.lge.smartsharebeam.off"));
    }

    private boolean isSmartShareBeamRunning() {
        ActivityManager manager = (ActivityManager)mContext
            .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo runningInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_NAME.equals(runningInfo.service.getClassName())) {
                int serviceUid = runningInfo.uid / UserHandle.PER_USER_RANGE
                        - (UserHandle.myUserId() == UserHandle.USER_OWNER ? 1 : 0);
                Log.d(TAG, "calculated serviceUid : " + serviceUid);
                serviceUid = serviceUid < 0 ? UserHandle.USER_OWNER : serviceUid;
                Log.d(TAG, "interpolated serviceUid : " + serviceUid);
                Log.d(TAG, "UserHandle.myUserId() : " + UserHandle.myUserId());
                if (serviceUid == UserHandle.myUserId()) {
                    Log.d(TAG, "smartshare beam service running.");
                    return true;
                }
            }
        }
        Log.d(TAG, "smartshare beam service not running.");
        return false;
    }

    public void setState(boolean isOn) {
        Log.d(TAG, "setState start");
        if (mSwitch != null && mSwitch.isChecked() != isOn) {
            Log.d(TAG, "mSwitch set-mSwitch.isChecked() : " + mSwitch.isChecked());
            mSwitchButtonChangedFromUser = false;
            mSwitch.setChecked(isOn);
        }
    }
}
