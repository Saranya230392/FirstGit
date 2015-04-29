package com.android.settings.lge;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;

//import com.android.internal.telephony.ModemItem;
import com.lge.internal.telephony.ModemItem;
import com.android.settings.lgesetting.Config.Config;

public class NVItemClear extends Activity {
    private static final String TAG = "NVItemClear";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Log.d(TAG, "onCreate()");

        if (Config.getCountry().equals("KR")) {
            Phone phone = PhoneFactory.getDefaultPhone();
            if (phone != null) {
                phone.setModemIntegerItem(ModemItem.W_BASE_INFO + 38 , 0,  null);
                Log.d(TAG, "onCreate() : Success ModemItem Clear");
            }
        } else if ("1".equals(SystemProperties.get("ro.lge_radio_gpri"))) {
            Phone phone = PhoneFactory.getDefaultPhone();
            if (phone != null) { 
                phone.setModemIntegerItem(ModemItem.C_RP.LGE_MODEM_RP_FACTORY_RESET_LTE, 0, null);
                Log.d(TAG, "onCreate() : Success ModemItem Clear");
            }
        }
        finish();
    }
}
