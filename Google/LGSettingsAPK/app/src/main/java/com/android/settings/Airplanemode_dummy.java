package com.android.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import com.lge.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.Utils;

public class Airplanemode_dummy extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        ComponentName component;

        int style = Settings.System.getInt(this.getContentResolver(), SettingsConstants.System.SETTING_STYLE, 0);
        if( "TRF".equals(Config.getOperator()) 
                || Utils.isWifiOnly(this) || Utils.isTablet() ) {
            Log.d ("Airplanemode_dummy", "Only List settings supported" );
            style = 0;
        }

        Log.d ("Airplanemode_dummy", " style = " + style );
        // 0 : list settings
        // 1 : easy settings

        if(style==0) {
            component = getListMenuComponetName();
        } else {
            component = getTabMenuComponetName();
        }

        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private ComponentName getListMenuComponetName() {
        ComponentName component;
        if (Config.supportAirplaneListMenu()) {
            if ( Utils.isWifiOnly(this) || Utils.isTablet() ) {
                component = new ComponentName("com.android.settings", "com.android.settings.Settings$AirplaneModeFragment");
            } else {
                component = new ComponentName("com.android.settings", "com.android.settings.Settings");
            }
        } else {
            if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
                component = new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessMoreSettingsActivity");
            } else {
                if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(this)) {
                    component = new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
                } else {
                    component = new ComponentName("com.android.settings", "com.android.settings.Settings$TetherNetworkSettingsActivity");
                }
            }
        }
        return component;
    }

    private ComponentName getTabMenuComponetName() {
        ComponentName component;
        if (Config.supportAirplaneListMenu()) {
            if ( Utils.isWifiOnly(this) || Utils.isTablet() ) {
                component = new ComponentName("com.android.settings", "com.android.settings.Settings$AirplaneModeFragment");
            } else {
                if (Utils.supportEasySettings(this)) {
                    component = new ComponentName("com.lge.settings.easy", "com.lge.settings.easy.EasySettings");
                } else {
                    component = new ComponentName("com.android.settings", "com.android.settings.Settings");
                }
            }
        } else {
            if (Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2) {
                component = new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessMoreSettingsActivity");
            } else {
                if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(this)) {
                    component = new ComponentName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
                } else {
                    component = new ComponentName("com.android.settings", "com.android.settings.Settings$TetherNetworkSettingsActivity");
                }
            }
        }
	    return component;
   }

}
