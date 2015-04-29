package com.android.settings.lge;

import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.lge.constants.LGIntent;

/**
@class       LollipopReceiver
@date        2013/06/18
@author      seungyeop.yeom@lge.com
@brief        Once operation for lollipop Cover (only lollipop)
@warning
*/
public class LollipopReceiver extends BroadcastReceiver {

    private static final int COVER_OPEN_STATE = 0;
    private static final int COVER_CLOSE_STATE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        Log.d("YSY", "cover receiver start");
        if (intent.getAction().equals(LGIntent.ACTION_ACCESSORY_COVER_EVENT)) {
            int coverState = intent.getIntExtra(LGIntent.EXTRA_ACCESSORY_COVER_STATE, 0);
            Log.d("YSY", "lollipop cover intent start");
            int beforeCoverState = Settings.Global.getInt(context.getContentResolver(),
                    SettingsConstants.Global.BEFORE_COVER_STATE, COVER_OPEN_STATE);

            if (coverState == LGIntent.EXTRA_ACCESSORY_COVER_OPENED) {

                if (beforeCoverState == COVER_CLOSE_STATE) {
                    // This receiver is operated only lollipop
                    if (Config.getFWConfigBool(context,
                            com.lge.R.bool.config_smart_cover,
                            "com.lge.R.bool.config_smart_cover") == true
                            && Config.getFWConfigBool(context,
                                    com.lge.R.bool.config_using_window_cover,
                                    "com.lge.R.bool.config_using_window_cover") == false
                            && Config.getFWConfigBool(context,
                                    com.lge.R.bool.config_using_lollipop_cover,
                                    "com.lge.R.bool.config_using_lollipop_cover") == true
                            && Config.getFWConfigBool(context,
                                    com.lge.R.bool.config_using_circle_cover,
                                    "com.lge.R.bool.config_using_circle_cover") == false) {

                        int coverMount = Settings.Global.getInt(context.getContentResolver(),
                                SettingsConstants.Global.QUICK_POP_MODE_SET, 0);
                        Log.d("YSY", "lollipop Cover opened");

                        if (coverMount == 0) {
                            Intent lollipopCaseIntent = new Intent(context, QuickCaseLollipop.class);
                            lollipopCaseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Settings.Global.putInt(context.getContentResolver(),
                                    SettingsConstants.Global.QUICK_POP_MODE_SET, 1);
                            context.startActivity(lollipopCaseIntent);
                        }
                    }

                    Settings.Global.putInt(context.getContentResolver(),
                            SettingsConstants.Global.BEFORE_COVER_STATE,
                            COVER_OPEN_STATE);
                }
            } else if (coverState == LGIntent.EXTRA_ACCESSORY_COVER_CLOSED) {

                if (beforeCoverState == COVER_OPEN_STATE) {
                    Log.d("YSY", "lollipop Cover closed");
                    Settings.Global.putInt(context.getContentResolver(),
                            SettingsConstants.Global.BEFORE_COVER_STATE,
                            COVER_CLOSE_STATE);
                }
            }
        }
    }
}
