package com.android.settings.bootcompleted;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

public class AntiFlickerBoot {

    public static void onReceive(Context context) {
        String mCountry_mcc = null;
        mCountry_mcc = Settings.System.getString(context.getContentResolver(), "wifi_country_mcc");
        SystemProperties.set("persist.sys.wificountrymcc", mCountry_mcc);
    }
}