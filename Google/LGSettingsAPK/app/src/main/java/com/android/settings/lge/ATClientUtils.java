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
import com.lge.android.atservice.client.LGATCMDClient;
import android.os.Handler;
import android.os.SystemProperties;
import android.app.Activity;
import android.preference.PreferenceGroup;
import android.util.Log;
import com.android.settings.deviceinfo.Status;
import android.widget.Toast;
import android.widget.ImageView;
//[E][2012.07.20][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

import com.android.settings.R;

public class ATClientUtils {
    public static final String TAG = "ATClientUtils";
    public static final String LOG_TAG = "LGAtcmdClient , aboutphone , ATClientUtils";
    public static LGATCMDClient sATClient;
    public static boolean sBind;

    public static String atClient_readValue(int command, Context context, String option) {
        LGATCMDClient.Response response = null;
        String strResponseData = "";
        char[] tempHwRev = new char[3];
        response = sATClient.request(command, "".getBytes());

        if (response == null) {
            Log.e(TAG, "response is null, retry~");
            response = sATClient.request(command, "".getBytes());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        if (response == null) {
            Log.e(TAG, "response is null, return not_available");
            return context.getString(R.string.device_info_not_available);
        }

        if (response.data == null) {
            Log.e(TAG, "response.data is null, return not_available");
            return context.getString(R.string.device_info_not_available);
        }

        Log.i(LOG_TAG, "response result:" + response.result);
        Log.i(LOG_TAG, "response length:" + response.data.length);

        if (option.equals("hw_version")) {
            for (int i = 0; i < 3; i++) {
                Log.e(TAG, "[" + i + "]:" + response.data[i]);
                tempHwRev[i] = Character.toUpperCase((char)response.data[i]); // inyeop.woo, 20121210, make rev. to be upper case
            }

            if (tempHwRev[0] == '1') {
                strResponseData = ("Rev." + tempHwRev[0] + tempHwRev[1] + tempHwRev[2]);
            } else if (tempHwRev[0] == '0') {
                strResponseData = ("Rev." + tempHwRev[2]);
            } else {
                strResponseData = ("Rev." + tempHwRev[0]);
            }

            return strResponseData;
        } else {
            strResponseData = new String(response.data);

            return strResponseData;
        }
    }

    public static void atClient_BindService(Context context) {
        sATClient = new LGATCMDClient(context);

        if (sATClient != null) {
            Log.i(LOG_TAG, "mATClient != null... ok !!, start bindService !");
            sATClient.bindService();
            sBind = true;
        } else {
            Log.e(LOG_TAG, "mATClient == null... fail !!");
        }
    }

    public static void atClient_unBindService() {
        if (sATClient != null && sBind == true) {
            Log.i(LOG_TAG, "atClient_unBindService");
            sATClient.unbindService();
            sBind = false;
        }
    }
}