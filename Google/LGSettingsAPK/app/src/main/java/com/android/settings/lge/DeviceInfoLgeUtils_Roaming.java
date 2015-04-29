package com.android.settings.lge;

import android.telephony.ServiceState;
import android.util.Log;

public class DeviceInfoLgeUtils_Roaming {
    public static final String LOG_TAG = "aboutphone # src/DeviceInfoLgeRoamingUtils";

    public static String getVoiceRoaming(ServiceState serviceState) {
        Log.d(LOG_TAG, "getVoiceRoaming() : N/A");
        return "N/A";
    }

    public static String getDataRoaming(ServiceState serviceState) {
        Log.d(LOG_TAG, "getDataRoaming() : N/A");
        return "N/A";
    }
}