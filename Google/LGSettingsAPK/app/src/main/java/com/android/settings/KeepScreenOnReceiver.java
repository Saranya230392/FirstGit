package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;  //[12.03.22][common][susin.park] Receiver on/off
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager; //[12.03.22][common][susin.park] Receiver on/off
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.os.Build;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

//LGE_CHANGE_S [donghan07.lee@lge.com 2012.02.13] Change default value for KR
import android.os.SystemProperties;

//[S] insook.kim@lge.com 2012.03.01: refer to operator
import com.android.settings.lgesetting.Config.Config;
//[E]insook.kim@lge.com 2012.03.01: refer to operator

public class KeepScreenOnReceiver extends BroadcastReceiver {
    Context context;
    private static final String TAG = "KeepScreenOnReceiver";

    @Override
    public void onReceive(Context _context, Intent intent) {

        String action = intent.getAction();
        Log.d(TAG,"intent.getAction() : " + intent.getAction());
        context = _context;
        long maxTimeout;
        long currentTimeout;
        if (action.equals("com.lge.settings.SMART_SCREEN"))
        {
            int iKeepScreenOn = Settings.System.getInt(
                    context.getContentResolver(),
                    SettingsConstants.System.KEEP_SCREEN_ON, 0);
            int iKeepVideoOn = Settings.System.getInt(
                    context.getContentResolver(),
                    SettingsConstants.System.KEEP_VIDEO_ON, 0);
            Log.d(TAG,"go to startService(): "+iKeepScreenOn);
            Log.d(TAG, "go to startService(): " + iKeepVideoOn);
            if (iKeepScreenOn > 0 || iKeepVideoOn > 0) {
                Intent intentKeepScreenOn = new Intent();
                intentKeepScreenOn.setClassName("com.lge.keepscreenon", "com.lge.keepscreenon.KeepScreenOnService");
                context.startService(intentKeepScreenOn);
            }else{
                Intent intentKeepScreenOn = new Intent();
                intentKeepScreenOn.setClassName("com.lge.keepscreenon", "com.lge.keepscreenon.KeepScreenOnService");
                context.stopService(intentKeepScreenOn);
                //ReceiverDisable();
            }
        }

        if(action.equals("com.lge.mdm.ACTION_MAMXIMUM_TIME_TOLOCK"))
        {
            Log.d(TAG,"ACTION_MAMXIMUM_TIME_TOLOK() : " + intent.getAction());
            maxTimeout = intent.getLongExtra("MaximumTime", 30000);
            currentTimeout = Settings.System.getInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 30000);
            Log.d(TAG, "ACTION_MAMXIMUM_TIME_TOLOCK() : max = " + maxTimeout + "cur = " + currentTimeout);

            if(maxTimeout < currentTimeout)
            {
                if(maxTimeout >= 15000) //15s
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 15000);
                }
                if(maxTimeout >= 30000) //30s
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 30000);
                }
                if(maxTimeout >= 60000) //1min
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 60000);
                }
                if(maxTimeout >= 120000) //2min
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 120000);
                }
                if(maxTimeout >= 300000) //5min
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 300000);
                }
                if(maxTimeout >= 600000) //10min
                {
                    Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 600000);
                }
                if (!Config.getFWConfigBool(context, com.lge.R.bool.config_lcd_oled,
                        "com.lge.R.bool.config_lcd_oled")) {
                    if (maxTimeout >= 900000) //15min
                    {
                        Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 900000);
                    }
                    if ("VZW".equals(Config.getOperator())) {
                        if (maxTimeout >= 1800000) //30min
                        {
                            Settings.System.putInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, 1800000);
                        }
                    }
                }
            }

        }

    }
    private void ReceiverDisable(){
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, StoreModeReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG,"KeepScreenOnReceiver disenabled!");
    }
}
