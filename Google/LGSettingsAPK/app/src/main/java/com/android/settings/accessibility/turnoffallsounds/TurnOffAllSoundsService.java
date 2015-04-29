package com.android.settings.accessibility.turnoffallsounds;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.util.Log;

public class TurnOffAllSoundsService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("resumet", "Settings Package : onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("resumet", "Settings Package : onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("resumet", "Settings Package : onDestroy)");
        super.onDestroy();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("resumet", "Settings Package : onServiceConnected)");
    }
}