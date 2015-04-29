package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BluetoothShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        finish();
        
        if (Utils.checkPackage(this, "com.lge.bluetoothsetting")) {
            if (Utils.supportSplitView(this)) {
                Intent intent = new Intent("android.settings.BLUETOOTH_SETTINGS_SPLITVIEW");
                startActivity(intent);
            } else {
                Intent intent = new Intent("android.settings.BLUETOOTH_SETTINGS");
                startActivity(intent);           
            }
        }
        
    }
    
}