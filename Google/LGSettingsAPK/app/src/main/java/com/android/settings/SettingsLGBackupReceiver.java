package com.android.settings;

import java.io.File;
import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.settings.SettingsLGBackupAgent;
import com.lge.bnr.framework.LGBackupConstant;

public class SettingsLGBackupReceiver extends BroadcastReceiver {

    private static final String TAG = "SettingsLGBackupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.e(TAG, "receiver ~~~~~~~~~~~~~~~~");

        String mAction = intent.getAction();
        long total_size = 0;
        long size = 0;

        // Settings size
        File file = new File(new String(
                "/data/data/com.android.providers.settings/databases/settings.db"));
        size = file.length();
        total_size = total_size + size;
        Log.d(TAG, "onReceive() Settings size : " + size + ", total_size : " + total_size);

        // Wi-Fi size
        //--------------------
        File wifiFile1 = new File(new String(
                "/data/misc/wifi/wpa_supplicant_copy.conf"));

        if (wifiFile1 != null) {
            size = wifiFile1.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() wifiFile1 size : " + size + ", total_size : " + total_size);
        }

        File wifiFile2 = new File(new String(
                "/data/misc/wifi/ipconfig_copy.txt"));

        if (wifiFile2 != null) {
            size = wifiFile2.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() wifiFile2 size : " + size + ", total_size : " + total_size);
        }
        //--------------------
        Log.d(TAG, "onReceive() Wi-Fi size : " + size + ", total_size : " + total_size);

        // Call size
        //--------------------
        File callFile1 = new File(new String(
                "/data/data/com.android.phone/databases/callreject.db"));

        if (callFile1 != null) {
            size = callFile1.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile1 size : " + size + ", total_size : " + total_size);
        }

        File callFile2 = new File(new String(
                "/data/data/com.android.phone/databases/callsettings.db"));

        if (callFile2 != null) {
            size = callFile2.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile2 size : " + size + ", total_size : " + total_size);
        }

        File callFile3 = new File(new String(
                "/data/data/com.android.phone/databases/quickmessage.db"));

        if (callFile3 != null) {
            size = callFile3.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile3 size : " + size + ", total_size : " + total_size);
        }

        File callFile4 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/PreCallDuration.xml"));      

        if (callFile4 != null) {
            size = callFile4.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile4 size : " + size + ", total_size : " + total_size);
        }

        File callFile5 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_date.xml"));

        if (callFile5 != null) {
            size = callFile5.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile5 size : " + size + ", total_size : " + total_size);
        }

        File callFile6 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_number.xml"));

        if (callFile6 != null) {
            size = callFile6.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile6 size : " + size + ", total_size : " + total_size);
        }

        File callFile7 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/cdma_call_forwarding_token.xml"));

        if (callFile7 != null) {
            size = callFile7.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile7 size : " + size + ", total_size : " + total_size);
        }

        File callFile8 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml"));

        if (callFile8 != null) {
            size = callFile8.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile8 size : " + size + ", total_size : " + total_size);
        }

        File callFile9 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/customized_number.xml"));

        if (callFile9 != null) {
            size = callFile9.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile9 size : " + size + ", total_size : " + total_size);
        }

        File callFile10 = new File(new String(
                "/data/data/com.android.phone/shared_prefs/enable_call_forwarding_key.xml"));

        if (callFile10 != null) {
            size = callFile10.length();
            total_size = total_size + size;
            Log.d(TAG, "onReceive() callFile9 size : " + size + ", total_size : " + total_size);
        }

        //--------------------

        Log.d(TAG, "onReceive() Action : " + intent.getAction());
        if (intent.getAction().equals(LGBackupConstant.ACTION_BNR_QUERY)) {
            int  mBnRMode = intent.getExtras().getInt(LGBackupConstant.INTENT_KEY_BNR_MODE);
            Intent i = new Intent();
            i.setAction(LGBackupConstant.ACTION_BNR_REPONSE);
            i.putExtra(LGBackupConstant.INTENT_KEY_REPONSE_PACKAGENAME, context.getPackageName());
            if (mBnRMode == LGBackupConstant.BACKUP_MODE) {
                i.putExtra(LGBackupConstant.INTENT_KEY_REPONSE_BACKUP_SIZE, total_size);
            }
            context.sendBroadcast(i);
        }

        else if (LGBackupConstant.ACTION_REQUEST_SETTINGS.equals(mAction)) {

            int BnRMode = intent.getExtras().getInt(LGBackupConstant.INTENT_KEY_BNR_MODE);

            if (BnRMode == LGBackupConstant.BACKUP_MODE) {
                new Thread(new BackupThread(context.getApplicationContext(), intent)).start();
            } else if (BnRMode == LGBackupConstant.RESTROE_MODE
                    || BnRMode == LGBackupConstant.RESTROE_MODE_OLD) {
                new Thread(new RestoreThread(context.getApplicationContext(), intent)).start();
            }
        }
    }

    class BackupThread implements Runnable {

        private Context mContext = null;
        private SettingsLGBackupAgent bnr;
        private Intent mIntent;

        public BackupThread(Context context, Intent intent) {
            // TODO Auto-generated constructor stub
            mContext = context;
            mIntent = intent;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            bnr = new SettingsLGBackupAgent(mContext, mIntent);

            bnr.startBackup();
        }
    }

    class RestoreThread implements Runnable {

        private Context mContext = null;
        private SettingsLGBackupAgent bnr;
        private Intent mIntent;

        public RestoreThread(Context context, Intent intent) {
            // TODO Auto-generated constructor stub
            mContext = context;
            mIntent = intent;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int mode;
            ArrayList<String> filePathList;
            bnr = new SettingsLGBackupAgent(mContext, mIntent);

            mode = mIntent.getIntExtra(LGBackupConstant.INTENT_KEY_BNR_MODE,
                    LGBackupConstant.RESTROE_MODE);
            filePathList = mIntent
                    .getStringArrayListExtra(LGBackupConstant.INTENT_KEY_OLD_FILELIST);
            if (LGBackupConstant.RESTROE_MODE == mode) {
                bnr.startRestore();
            } else {
                bnr.startRestoreOld(filePathList);
            }

        }
    }

}
