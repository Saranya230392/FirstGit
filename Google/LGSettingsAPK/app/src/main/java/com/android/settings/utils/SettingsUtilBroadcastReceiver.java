package com.android.settings.utils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.provider.Telephony;
//import android.provider.Telephony.Intents.*;
//import static android.provider.Telephony.Intents.SECRET_CODE_ACTION;
import android.os.Build;

public class SettingsUtilBroadcastReceiver extends BroadcastReceiver {
    /**
     * @see android.content.BroadcastReceiver#onReceive(Context,Intent)
     */
     private static String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Put your code here
         if (("userdebug".equals(Build.TYPE)) && intent.getAction().equals(SECRET_CODE_ACTION) ) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClass(context, SettingsUtil.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
    }
}
