package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TetheringShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        if (Utils.checkPackage(this, "com.lge.wifisettings")) {
            /*if (Utils.supportSplitView(this)) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS_SPLITVIEW");
                startActivity(intent);
            } else {*/
                Intent intent = new Intent("android.settings.TETHER_SETTINGS_EXTERNAL");
                startActivity(intent);
            //}
        }        
    }
    
}