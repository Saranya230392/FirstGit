package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LockScreenShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        finish();
        
        if (Utils.checkPackage(this, "com.lge.lockscreensettings")) {
            if (Utils.supportSplitView(this)) {
                Intent intent = new Intent("android.settings.LOCK_SETTINGS_SPLITVIEW");
                startActivity(intent);
            } else {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.lge.lockscreensettings", "com.lge.lockscreensettings.lockscreen.LockSettings");
                startActivity(intent);           
            }
        }
        
    }
    
}