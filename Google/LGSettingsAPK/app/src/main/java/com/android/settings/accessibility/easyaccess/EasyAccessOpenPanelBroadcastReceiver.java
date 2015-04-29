package com.android.settings.accessibility.easyaccess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EasyAccessOpenPanelBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("resumet", "Settings Package : EasyAccessOpenPanelBroadcastReceiver - easy access!");
    }
}
