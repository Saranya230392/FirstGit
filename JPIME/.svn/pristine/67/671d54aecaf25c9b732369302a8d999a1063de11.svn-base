/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/***********************************************************************
*    DecoEmojiListener
*
* [File]
*     DecoEmojiListener.java
*
*     OMRON SOFTWARE Co., Ltd.
*     All rights reserved.
*
* [Description]
*     XXXXXX
************************************************************************/
package jp.co.omronsoft.iwnnime.ml;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiAttrInfo;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiConstant;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiManager;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiOperationQueue;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

import java.util.List;

/**
 * OnReceiveLock class.
 */
class OnReceiveLock {
    static Object lock = new Object();
}

/**
 * DecoEmojiListener class.
 */
public class DecoEmojiListener extends BroadcastReceiver {

    /** Tag name for debug log */
    private static final String TAG = "DecoEmojiListener";
    /** Debug flag */
    private static boolean DEBUG = false;
    /** SharedPreferences access key */
    public static final String PREF_KEY = "preferenceId";
    /** SharedPreferences */
    private SharedPreferences mPref = null;
    /** Maximum number of DecoEmoji attribute information to be processed */
    private static final int ATTRINFO_MAX_OPERATE_CNT = 100;

    /**
     * onReceive
     *
     * @param  context    : The Context in which the receiver is running.
     * @param  intent     : The Intent being received.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG) {Log.d(TAG, "onReceive() Start");}

        EmojiAssist assist = EmojiAssist.getInstance();
        if (assist != null) {
            int functype = assist.getEmojiFunctionType();
            if (functype == 0) {
                return;
            }
        }


        synchronized (OnReceiveLock.lock) {

            // Get the maximum serial value from SharedPreferences.
            mPref = context.getSharedPreferences(OpenWnn.FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);

            // Get the intent identifier.
            String action = intent.getAction();

            // If the intent identifier is equal to Package name of DecoEmojiManager or Package name of DecoEmojiListener, execute process.
            if (action.equals(IDecoEmojiConstant.ACTION_DECOEMOJI_RESULT)
                    || action.equals(this.getClass().getPackage().getName())) {

                // Create a bundle for receiving the included Emoji information.
                Bundle bundle = intent.getExtras();

                if (bundle != null) {

                    // Receive the type of instruction.
                    int type = bundle.getInt(IDecoEmojiConstant.BROADCAST_TYPE_TAG);

                    // If the type of instruction is "initialization", get all the Emoji information.
                    if (type == IDecoEmojiConstant.FLAG_SET_TO_INITIALIZING) {
                        DecoEmojiOperationQueue.getInstance().clearOperation();
                        reciveOperation(null, type, context);

                        // Initialize the maximum serial which processed the dictionary information.
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putInt(PREF_KEY, -1);
                        editor.commit();
                    }
                    // If the type of instruction is not "Exit", receive the included Emoji information.
                    else if (type != IDecoEmojiConstant.FLAG_FINISH) {

                        // Receive the included Emoji information.
                        List<DecoEmojiAttrInfo> receivedatalist = bundle.getParcelableArrayList(IDecoEmojiConstant.BROADCAST_DATA_TAG);

                        // If the received included Emoji information is not empty or null, execute process.
                        if (receivedatalist != null && !receivedatalist.isEmpty()) {

                            // Get the number of the included Emoji information.
                            int len = receivedatalist.size();

                            // Generate a object for storing additional iWnn's dictionary information.
                            DecoEmojiAttrInfo[] decoemojiattrinfo = new DecoEmojiAttrInfo[len];

                            // Store the received included Emoji information in the object for storing additional iWnn's dictionary information.
                            receivedatalist.toArray(decoemojiattrinfo);

                            // If the number of the included Emoji information exceeds 100, don't process.
                            if (decoemojiattrinfo.length <= ATTRINFO_MAX_OPERATE_CNT) {
                                reciveOperation(decoemojiattrinfo, type, context);

                                if (type == IDecoEmojiConstant.FLAG_INSERT) {
                                    boolean isSerialConfirmFlag = false;
                                    int prefInt = OpenWnn.getIntFromNotResetSettingsPreference(context, PREF_KEY, 0);

                                    // Get the maximum serial value from the received included Emoji information.
                                    for (int i = 0; i < decoemojiattrinfo.length; i++) {

                                        int newSerial = decoemojiattrinfo[i].getId();

                                        if (prefInt < newSerial) {
                                            prefInt = newSerial;
                                            isSerialConfirmFlag = true;
                                        }
                                    }
                                    // If the maximum serial value was updated, check for updates to DecoEmojiManager.
                                    if (isSerialConfirmFlag) {
                                        updateConfirm(context, prefInt);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (DEBUG) {Log.d(TAG, "onReceive() End");}
    }

    /**
     * updateConfirm
     *
     * @param  context          : The Context in which the receiver is running.
     * @param  updatePrefInt    : IME Preference.
     * @return none
     */
    private void updateConfirm(Context context, int updatePrefInt) {
        if (DEBUG) {Log.d(TAG, "updateConfirm() Start");}

        // Check connection to Service(DecoEmojiManager).
        Intent confirmIntent = new Intent(IDecoEmojiManager.class.getName());
        try {

            ComponentName retService = context.startService(confirmIntent);

            // If the service(DecoEmojiManager) does not exist, output an alert.
            if (retService == null) {
                Log.w(TAG, "(Warning) Service does not exist!");
                return;
            }

        } catch (SecurityException se) {
            Log.e(TAG, "(Exception) startService Error!");
            se.printStackTrace();
            return;
        }


        if (DEBUG) {Log.d(TAG, "updateConfirm() End");}
    }

    /**
     * Receive the DecoEmoji operation.
     */
    private void reciveOperation(DecoEmojiAttrInfo[] decoemojiattrinfo, int type, Context context) {
        DecoEmojiOperationQueue.getInstance().enqueueOperation(decoemojiattrinfo, type, context);

        OpenWnn wnn = OpenWnn.getCurrentIme();
        if (wnn != null) {
            wnn.onEvent(new OpenWnnEvent(OpenWnnEvent.RECEIVE_DECOEMOJI));
        }
    }
}
