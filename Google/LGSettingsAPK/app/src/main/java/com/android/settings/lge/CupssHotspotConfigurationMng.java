package com.android.settings.lge;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class CupssHotspotConfigurationMng {

    public static WifiConfiguration getHotSpotConfiguration(WifiManager wifiManager,
            TelephonyManager mTelephonyMng, Context context) {

        SharedPreferences pref_ff = context.getSharedPreferences("FIRST_FLAG",
                Activity.MODE_PRIVATE);

        boolean isHotspotSet = pref_ff.getBoolean("cupss_flag", true);

        WifiConfiguration config = wifiManager.getWifiApConfiguration();
        if (isHotspotSet) {
            HotSpotConfigParser hotspotParser = HotSpotConfigParser.getInstance();

            if (hotspotParser.isHotspotAvailable()) {
                String ssid = hotspotParser.getHotspotSSID(mTelephonyMng.getDeviceId());
                String password = hotspotParser.getHotspotPassword(mTelephonyMng.getDeviceId());
                config.SSID = ssid == null ? config.SSID : ssid;

                switch (hotspotParser.getHotspotKEYMGM()) {
                case com.android.settings.lge.HotSpotConfigParser.OPEN_VALUE:
                    config.allowedKeyManagement.set(KeyMgmt.NONE);
                    break;

                case com.android.settings.lge.HotSpotConfigParser.WPA_VALUE:
                    config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                    config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                    config.preSharedKey = password == null ? config.preSharedKey : password;
                    break;

                case com.android.settings.lge.HotSpotConfigParser.WPA2_VALUE:
                    config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                    config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                    config.preSharedKey = password == null ? config.preSharedKey : password;
                    break;

                default:
                    break;
                }
                pref_ff.edit().putBoolean("cupss_flag", false).commit();
                wifiManager.setWifiApConfiguration(config);
            }
        }
        return config;
    }

}
