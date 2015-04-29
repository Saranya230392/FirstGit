package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.SettingsCarrierLegalActivity;



public class SettingsCarrierLegalReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        mContext = context;

        Log.d("starmotor" , "action = " + action);

        if (action.equals("com.lge.lgdmsclient.dmclient.app.data.CARRIER_LEGAL")) {
            String mHomePage = intent.getStringExtra("node_index");
            Settings.System.putString(mContext.getContentResolver(), "home_page", mHomePage);
//            String mHome = Settings.System.getString(mContext.getContentResolver(), "home_page");
            Log.d("starmotor" , "mHomePage1 = " + mHomePage);
//            if (mHome == null) {
//                SettingsCarrierLegalActivity.mHomePageApply =  mHomePage;
//            }
        }
        if (action.equals("com.lge.lgdmsclient.dmclient.app.data.BRAND_ALPHA")) {
            String brnad_alpha = intent.getStringExtra("brnad_alpha"); 
            Settings.System.putString(mContext.getContentResolver(), "brand_alpha", brnad_alpha);
//            String mBran = Settings.System.getString(mContext.getContentResolver(), "brand_alpha");
            Log.d("starmotor" , "brnad_alpha1 = " + brnad_alpha);
//            if (mBran == null) {
//                SettingsCarrierLegalActivity.mBrandAlpha =  brnad_alpha;
//            }
        }
    }
}
