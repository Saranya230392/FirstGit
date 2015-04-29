package com.android.settings;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.wifi.wifiscreen.WifiScreenEnabler;

public class MiracastSwitchPreference extends SwitchPreference {

    private boolean mOnDivider = true;
    private int mWifiScreenState = WFD_STATE_DISABLED;
    //private final Listener mListener = new Listener();
    private Switch mSwitch;
    private final WifiScreenEnabler mWifiScreenEnabler;
    private final IntentFilter mIntentFilter;
    private final Context mContext;

    private static final String WFD_STATE_CHANGED_ACTION =
            "com.lge.systemservice.core.wfdmanager.WFD_STATE_CHANGED";
    private static final String EXTRA_WFD_STATE = "wfd_state";
    private static final int WFD_STATE_DISABLED = 0; // Wi-Fi Direct is turned off
    //private static final int WFD_STATE_ENABLING = 1; // Initializing JNI and RTSP server
    //private static final int WFD_STATE_NOT_CONNECTED = 2; // No connection found
    //private static final int WFD_STATE_CONNECTING = 3; // Link connection in progress
    //private static final int WFD_STATE_LINK_CONNECTED = 4; // (L2 connection established) WFD paring in progress (M1-M4)
    //private static final int WFD_STATE_WFD_PAIRED = 5; // WFD Paired: RTSP Running
    //private static final int WFD_STATE_DISCONNECTING = 6; // State will be changed to WFD_STATE_NOT_CONNECTED
    //private static final int WFD_STATE_DISABLING = 7; // Disabling JNI interface
    //private static final int WFD_STATE_UNKNOWN = 8;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //int value = isChecked ? 1 : 0;
            if (isChecked != mSwitch.isChecked()) {
                mSwitch.setChecked(isChecked);
            }
        }
    }

    public MiracastSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mWifiScreenEnabler = new WifiScreenEnabler(context, new Switch(context));
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WFD_STATE_CHANGED_ACTION);
    }

    public MiracastSwitchPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WFD_STATE_CHANGED_ACTION.equals(action)) {
                mWifiScreenState = intent.getIntExtra(EXTRA_WFD_STATE, WFD_STATE_DISABLED);
            }
        }
    };

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mWifiScreenEnabler.setSwitch(mSwitch, mWifiScreenState);

        //mSwitch.setChecked(true);    //set default switch value
        //mSwitch.setOnCheckedChangeListener(mListener);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        if (mWifiScreenEnabler != null) {
            mWifiScreenEnabler.resume();
        }
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        if (mWifiScreenEnabler != null) {
            mWifiScreenEnabler.pause();
        }
    }
}
