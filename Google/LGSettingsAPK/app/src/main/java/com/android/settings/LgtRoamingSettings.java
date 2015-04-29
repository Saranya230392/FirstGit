/*
 *
 */

package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
//import android.os.SystemProperties;
//import android.telephony.TelephonyManager;

//import com.android.internal.telephony.PhoneConstants;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.lgesetting.Config.Config;

public class LgtRoamingSettings extends SettingsPreferenceFragment {

    private static boolean bLaunched = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mIntent = new Intent();
        if (true == "LGU".equals(Config.getOperator())) {
            //if (TelephonyManager.getDefault().getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
            //mIntent.setClassName("com.lge.roamingsettings", "com.lge.roamingsettings.uplusroaming.wcdmaroaming.Roaming");
            //} else if (SystemProperties.get("ro.product.model").equals("LG-F320L") || SystemProperties.get("ro.product.model").equals("LG-F300L")) {
            mIntent.setClassName("com.lge.roamingsettings",
                    "com.lge.roamingsettings.uplusroaming.wcdmaroaming.RoamingonLTE");
            //} else {
            //mIntent.setClassName("com.lge.roamingsettings", "com.lge.roamingsettings.uplusroaming.cdmaroaming.UPlusRoamingSettings");
        }

        if (true == "KT".equals(Config.getOperator())) {
            mIntent.setClassName("com.lge.roamingsettings",
                    "com.lge.roamingsettings.ktroaming.KTRoaming");
        }
        //[S][2012.04.18][youngmin.jeon][LG730] Add package and class name for LG730
        if (true == "SPRINT".equals(Config.getOperator())
                || true == "BM".equals(Config.getOperator())
                || true == "SPR".equals(Config.getOperator())) // [2012.04.30][youngmin.jeon] Add SPR operator for Roaming menu.
        {
            mIntent.setClassName("com.android.phone", "com.android.phone.CallFeatureSettingRoaming");
        }
        // [E][2012.04.18][youngmin.jeon][LG730] Add package and class name for LG730
        if (true == "SKT".equals(Config.getOperator())) {
            mIntent.setClassName("com.lge.roamingsettings",
                    "com.lge.roamingsettings.troaming.TRoamingFGK");
        }

        if (true == "CTC".equals(Config.getOperator()) ||
                true == "CTO".equals(Config.getOperator())) {
            if (Utils.isMultiSimEnabled()) {
                mIntent.setClassName("com.lge.roamingsettings",
                        "com.lge.roamingsettings.chinaroaming.MSimChinaTelecomRoamingSetting");
            } else {
                mIntent.setClassName("com.lge.roamingsettings",
                        "com.lge.roamingsettings.chinaroaming.ChinaTelecomRoamingSetting");
            }
        }

        startActivity(mIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (false == bLaunched) {
            bLaunched = true;
        }
        else {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
}
