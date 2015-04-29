package com.android.settings.applications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Utils;

import java.util.Locale;

public class RecoverReceiver extends BroadcastReceiver {
    private static final String TAG = "RecoverReceiver";
    public static final String APPS_PREFS = "apps_prefs";
    public static final String RESULT_KEY = "result";
    public static final String MESSAGE_KEY = "msg";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "BroadcastAppRecovery onReceive()");

        String action = intent.getAction();
        Log.d(TAG, "action = " + action);

        String result = "null";
        String msg = "null";
        int totalCnt = 0;
        int successCnt = 0;

        if (action.equals("com.lge.android.intent.action.MYFOLDER_EXITED")) {
            Log.d("kjo", "MyFoler exit - Hidden apps clear");
            Utils.onHiddenAppsClear();
            return;
        }

        if (action.equals(EnumManager.IntentActionType.RESULT_BACKUP.getValue())) {
            result = intent.getStringExtra(EnumManager.ExtraNameType.EXTRA_RESULT.getValue());
            msg = intent.getStringExtra(EnumManager.ExtraNameType.EXTRA_MESSAGE.getValue());
        } else if (action.equals(EnumManager.IntentActionType.RESULT_RECOVERY.getValue())) {
            result = intent.getStringExtra(EnumManager.ExtraNameType.EXTRA_RESULT.getValue());
            msg = intent.getStringExtra(EnumManager.ExtraNameType.EXTRA_MESSAGE.getValue());
        } else if (action.equals(EnumManager.IntentActionType.RESULT_TOAST.getValue())) {
            Log.d(TAG, "Show result Toast");
            successCnt = intent.getIntExtra
                    (EnumManager.ExtraNameType.EXTRA_PACKAGE_SUCCESS_CNT.getValue(), 0);
            totalCnt = intent.getIntExtra
                    (EnumManager.ExtraNameType.EXTRA_PACKAGE_TOTAL_CNT.getValue(), 0);

            if (successCnt == 0) {
                Toast.makeText(context, R.string.settings_apps_uninstall_allfail
                        , Toast.LENGTH_LONG).show();
            } else {
                String language = Locale.getDefault().getLanguage();
                if (language.equals("ko")) {
                    Toast.makeText(context, context.getString(
                            R.string.settings_apps_multi_unistall_complete
                            , totalCnt, successCnt), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getString(
                            R.string.settings_apps_multi_unistall_complete
                            , successCnt, totalCnt), Toast.LENGTH_LONG).show();
                }
            }
        }

        Log.d(TAG, "result = " + result);
        Log.d(TAG, "msg = " + msg);

        SharedPreferences preferences = context.getSharedPreferences(APPS_PREFS, 0);
        preferences.edit().putString(RESULT_KEY, result).commit();

        preferences = context.getSharedPreferences(APPS_PREFS, 0);
        preferences.edit().putString(MESSAGE_KEY, msg).commit();
    }
}
