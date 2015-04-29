package com.android.settings.dragndrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.dragndrop.ButtonConfiguration.ButtonInfo;
import com.android.settings.R;

import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;

public class DBInterface {
    private static final String TAG = "ButtonCombinationDBInterface";
    public static String DB_BUTTON_COMBINATION = "button_combination";

    private static final long ACTION_FEATURE_USAGE = 0x02000001;
    private Context mContext = null;
  
    public DBInterface(Context context) {
    	mContext =  context;
    }
    
    public static void sendUserLog(Context context) {
        Bundle log = new Bundle();
        log.putLong("action", ACTION_FEATURE_USAGE);
        log.putString("feature_name", "ugc_navigationbar");

        Intent i = new Intent("com.lge.mrg.service.intent.action.APPEND_USER_LOG");
	i.setPackage("com.lge.mrg.service");		
        i.putExtras(log);
        context.startService(i);
    }

    public String readDB() {
        return Settings.System.getString(mContext.getContentResolver(), DB_BUTTON_COMBINATION);
    }

    public void writeDB(String str) {
        Settings.System.putString(mContext.getContentResolver(), DB_BUTTON_COMBINATION, str);
        Log.d(TAG,
                "write Button Combination DB : "
                        + Settings.System.getString(mContext.getContentResolver(),
                                DB_BUTTON_COMBINATION));
        sendUserLog(mContext);
    }
}