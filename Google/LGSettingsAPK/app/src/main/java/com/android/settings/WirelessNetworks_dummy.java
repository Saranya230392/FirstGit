package com.android.settings;

import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import android.util.Log;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;
import android.provider.Telephony.Sms.Intents;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;

public class WirelessNetworks_dummy extends Activity {
    private static final String TAG = "WirelessNetworks_dummy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        ComponentName component = null;
        String mPackageName;
        boolean isSMSapp = false;

        int style = Settings.System.getInt
                (this.getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);

        Log.d(TAG, " style = " + style);
        // 0 : list settings
        // 1 : easy settings

        if (!Utils.isUI_4_1_model(getApplicationContext())) {
            onUIFeatue4_0();
            return;
        }

        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> info = am.getRunningTasks(2);
        mPackageName = info.get(0).baseActivity.getPackageName();

        ComponentName topActivity = null;

        //        String topActivityClassName = topActivity.getClassName();
        //        String baseActivityClassName = info.get(0).baseActivity.getPackageName();// getClassName();
        //        String parentActivityName = Utils.getParentActivityName(getApplicationContext(), topActivity);
        //        ComponentName topActivity = info.get(1).topActivity; // 1->0 change
        //          Log.e ("kjo", "topActivity = " + topActivity);
        //          Log.e ("kjo", "topActivityClassName = " + topActivityClassName);
        //          Log.e ("kjo", "baseActivityClassName = " + baseActivityClassName);
        //          Log.e ("kjo", "parentActivityName = " + parentActivityName);

        Log.i("kjo", "PackageName = " + mPackageName);

        Collection<SmsApplicationData> smsApplications =
                SmsApplication.getApplicationCollection(getApplicationContext());

        for (SmsApplicationData smsApplicationData : smsApplications) {
            if (mPackageName.equals(smsApplicationData.mPackageName)) {
                component = new ComponentName("com.android.settings"
                        , "com.android.settings.Settings$SmsDefaultSettingsActivity");
                isSMSapp = true;
                break;
            }
        }

        if (isSMSapp) {
            if ("VZW".equals(Config.getOperator())) {
                component = new ComponentName("com.android.settings"
                        , "com.android.settings.Settings$TetherNetworkSettingsActivity");
            } else {
                Log.i("kjo", "not other OP");
                info = am.getRunningTasks(1);
                topActivity = info.get(0).topActivity;
                //String topActivityClassName = topActivity.getClassName();
                String baseActivityClassName = info.get(0).baseActivity.getClassName();
                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                    component = new ComponentName("com.android.settings"
                            , "com.android.settings.Settings$TetherNetworkSettingsActivity");
                }
            }
        } else {
            Log.i("kjo", "not Default message app");
            if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(getApplicationContext())) {
                component = new ComponentName("com.android.settings"
                        , "com.android.settings.Settings$WirelessSettingsActivity");
            } else {
                component = new ComponentName("com.android.settings"
                        , "com.android.settings.Settings$TetherNetworkSettingsActivity");
            }
        }

        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void onUIFeatue4_0() {
        Intent intent = new Intent();
        ComponentName component;

        if (Utils.isWifiOnly(getApplicationContext())) {
            component = new ComponentName("com.android.settings"
                    , "com.android.settings.Settings$WirelessSettingsActivity");
        } else {
            component = new ComponentName("com.android.settings"
                    , "com.android.settings.Settings$TetherNetworkSettingsActivity");
        }
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Log.i(TAG, "UI ver 4.0");
    }

}
