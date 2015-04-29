package com.android.settings;

import android.provider.Telephony;
import static com.android.internal.telephony.TelephonyIntents.SECRET_CODE_ACTION;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.view.KeyEvent;
import com.android.settings.lgesetting.Config.Config;


public class TestingSettingsBroadcastReceiver extends BroadcastReceiver {

    public TestingSettingsBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
        // 20131029 yonguk.kim This testing code should not apply at Korea or KDDI(JP) version requested by Dialer and Z KDDI model team
        if ("KR".equals(Config.getCountry()) || "KDDI".equals(Config.getOperator())) {
            return;
        }
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClass(context, TestingSettings.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
