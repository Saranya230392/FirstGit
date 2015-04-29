package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class WirelessStorageSwitchPreference extends SwitchPreference {

    private boolean mOnDivider = true;
    private Switch mSwitch ;
    private WirelessStorageEnabler mWirelessStorageEnabler;
    private boolean isConnected = false;
    private Context mContext;
    private IntentFilter mIntentFilter;
    private String TAG = "WirelessStorage";

    public WirelessStorageSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mWirelessStorageEnabler = new WirelessStorageEnabler(context, new Switch(context));
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_ON");
        mIntentFilter.addAction("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_OFF");
        Log.i(TAG, "WirelessStorageSwitchPreference - WirelessStorageSwitchPreference() - isConnected :"+isConnected);
    }

    public WirelessStorageSwitchPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    public void setListener() {
    }
    
    public boolean getConnectState() {
    	boolean connectState = false;
    	int settingFlag = 0;
    	try {
			settingFlag = Settings.System.getInt(mContext.getContentResolver(), "Wireless_storage_TurnOn");
			if (settingFlag == 1) {
				connectState = true;
			}
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
    	return connectState;
    }
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "WirelessStorageSwitchPreference - onReceive() : "+action);

            if ("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_ON".equals(action)) {
                isConnected = true;
            } else if ("com.lge.wirelessstorage.action.QUICK_SETTING_TURN_OFF".equals(action)){
                isConnected = false;
            }
        }
    };

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Log.i(TAG, "WirelessStorageSwitchPreference - onBindView() is called");
        Log.i(TAG, "WirelessStorageSwitchPreference - isConnected : "+isConnected);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mWirelessStorageEnabler.setSwitch(mSwitch, isConnected);

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        isConnected = getConnectState();
        notifyChanged();
        if (mWirelessStorageEnabler != null) {
            Log.i(TAG, "resume() called");
            mWirelessStorageEnabler.resume();
        }
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        if (mWirelessStorageEnabler != null) {
            Log.i(TAG, "pause() called");
            mWirelessStorageEnabler.pause();
        }
    }
}
