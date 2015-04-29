package com.android.settings.lgesetting.wireless;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

public class DataNetworkModeRoamingQueryPopupLGT extends Activity {

    private static final String TAG = "LGE_DATA_ROAMINGPOPUP_LGT";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check qem
        boolean factory_qem = SystemProperties.getBoolean("sys.factory.qem", false);
        if (factory_qem == true) { // if it's mode is QEM, do not show charging popup
            Log.d(TAG, "[LGE_DATA] QEM mode, blocking data call and don't show charging popup");
            return;
        }
        Log.d(TAG, "jump the DataRoamingSettingsLGU after JB MR");
        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
                "com.android.settings.lgesetting.wireless.DataRoamingSettingsLGU");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
