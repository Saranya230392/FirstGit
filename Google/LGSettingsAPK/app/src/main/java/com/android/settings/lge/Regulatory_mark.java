
package com.android.settings.lge;

import android.content.ComponentName;
import android.content.Context;
import android.preference.PreferenceScreen;
import android.preference.Preference;
/*[START]jun02.kim@lge.com Added MTK Dual SIM Lib*/
import com.android.internal.telephony.Phone;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.provider.Settings;
/*[END]jun02.kim@lge.com Added MTK Dual SIM Lib*/

//[S][2012.07.20][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
import android.os.Handler;
import android.os.SystemProperties;
import android.app.Activity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.deviceinfo.Status;
import android.widget.Toast;
import android.widget.ImageView;
//[E][2012.07.20][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

import com.android.settings.R;

public class Regulatory_mark {
    public static void setRes_Regulatory(ImageView imgView) {
        String mCupssDir = SystemProperties.get("ro.lge.capp_cupss.rootdir");
        if (mCupssDir.contains("/cust")) { // In case of CUPSS model
            imgView.setImageResource(R.drawable.regulatory_mark);
        } else {
            imgView.setImageResource(com.lge.R.drawable.regulatory_mark);
        }
    }
}