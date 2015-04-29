/*========================================================================
Copyright (c) 2011 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------

=========================================================================*/

package com.android.settings.lgmdm;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.lge.mdm.LGMDMManager;

import com.android.settings.R;

public class MDMLockScreenAdapter {
    private static final String TAG = "MDMLockScreenAdapter";
    private static final String DICTIONARY_FILE = "simple_password_dictionary.txt";

    private HashMap<String, String> mDictionary;

    private static MDMLockScreenAdapter sInstance;

    private static String sLockscreenMsg;

    public static MDMLockScreenAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new MDMLockScreenAdapter();
        }
        return sInstance;
    }

    private MDMLockScreenAdapter() {
    }

    public String checkSimplepassword(Activity activity, String password) {
        if ((LGMDMManager.getInstance().getAllowMaximumRepeatedPassword(null) == 0)
                && (LGMDMManager.getInstance()
                        .getAllowMaximumSequentialPassword(null) == 0)) {
            return null;
        }

        if (getRepeatedPassword(activity, password, true) >= 2) {
            if (sLockscreenMsg != null) {
                return sLockscreenMsg;
            }
        }

        if (getSequentialPassword(activity, password, true) >= 2) {
            if (sLockscreenMsg != null) {
                return sLockscreenMsg;
            }
        }

        if (getDictonarySimplePassword(activity, password, true) == true) {
            if (sLockscreenMsg != null) {
                return sLockscreenMsg;
            }
        }

        return null;
    }

    // CHECK REPEATED
    public int getRepeatedPassword(Activity activity, String password,
            boolean isFromMDM) {
        int nRepCount = 1;
        int nReqLimit = 0;
        int nMaxValue = 1;
        char currChar = '\0';
        char prevChar = '\0';
        String sRepeatedString = "";
        Resources rRes = activity.getResources();
        sLockscreenMsg = null;

        if (isFromMDM == true) {
            nReqLimit = LGMDMManager.getInstance()
                    .getAllowMaximumRepeatedPassword(null);
        }

        for (int i = 0; i < password.length(); i++) {
            prevChar = currChar;
            currChar = password.charAt(i);

            if (prevChar == currChar) {
                nRepCount++;
                if (nMaxValue < nRepCount) {
                    nMaxValue = nRepCount;
                }
                sRepeatedString += currChar;
            } else {
                nRepCount = 1;
                sRepeatedString = Character.toString(currChar);
            }

            if ((nRepCount >= nReqLimit) && (isFromMDM == true)) {
                sLockscreenMsg = rRes.getString(
                        R.string.sp_lock_pw_repeated_NORMAL, sRepeatedString);
                return nRepCount;
            }
        }
        return nMaxValue;
    }

    // CHECK SEQUENTIAL FOR ALPHA
    public int getSequentialPassword(Activity activity, String password,
            boolean isFromMDM) {
        String aAlpha = "abcdefghijklmnopqrstuvwxyz";
        String aNumeric = "0123456789012345";
        int curr_Seqlen = 0;
        int nSeqLimit = 0;
        sLockscreenMsg = null;

        if (isFromMDM == true) {
            nSeqLimit = LGMDMManager.getInstance()
                    .getAllowMaximumSequentialPassword(null);

            curr_Seqlen = checkSequentialForDigit(activity, password, aAlpha,
                    nSeqLimit, isFromMDM);

            if (curr_Seqlen > 0) {
                return curr_Seqlen;
            }

            curr_Seqlen = checkSequentialForDigit(activity, password, aNumeric,
                    nSeqLimit, isFromMDM);

            if (curr_Seqlen > 0) {
                return curr_Seqlen;
            }
        } else {
            int backup_Seqlen = 0;
            for (nSeqLimit = 1; nSeqLimit < password.length() + 1; nSeqLimit++) {
                backup_Seqlen = checkSequentialForDigit(activity, password,
                        aAlpha, nSeqLimit, isFromMDM);
                if ((backup_Seqlen > 0) && (backup_Seqlen > curr_Seqlen)) {
                    curr_Seqlen = backup_Seqlen;
                }

                backup_Seqlen = checkSequentialForDigit(activity, password,
                        aNumeric, nSeqLimit, isFromMDM);
                if ((backup_Seqlen > 0) && (backup_Seqlen > curr_Seqlen)) {
                    curr_Seqlen = backup_Seqlen;
                }
            }
        }

        return curr_Seqlen;
    }

    private int checkSequentialForDigit(Activity activity, String password,
            String type_value, int nSeqLimit, boolean isFromMDM) {
        String sSequentialString = "";
        boolean bSeqStatus = false;
        Resources rRes = activity.getResources();

        for (int i = 0; i < type_value.length() - nSeqLimit + 1; i++) {
            String sFwd = type_value.substring(i, nSeqLimit + i);
            String sRev = strReverse(sFwd);

            if (password.toLowerCase().indexOf(sFwd) != -1) {
                bSeqStatus = true;
                sSequentialString = sFwd;
                break;
            }
            if (password.toLowerCase().indexOf(sRev) != -1) {
                bSeqStatus = true;
                sSequentialString = sRev;
                break;
            }
        }

        if (bSeqStatus) {
            if (isFromMDM == true) {
                sLockscreenMsg = rRes.getString(
                        R.string.sp_lock_pw_sequential_NORMAL,
                        sSequentialString);
            }
            return sSequentialString.length();
        }

        return 0;
    }

    // CHECK DICTIONARY SIMPLE PASSWORD
    public boolean getDictonarySimplePassword(Activity activity,
            String password, boolean isFromMDM) {
        Resources rRes = activity.getResources();
        sLockscreenMsg = null;

        if (mDictionary == null) {
            mDictionary = getDictionary(activity);
        }

        if (mDictionary == null) {
            return false;
        }

        if (mDictionary.containsKey(password)) {
            if (isFromMDM == true) {
                sLockscreenMsg = rRes.getString(
                        R.string.sp_lgmdm_common_words_NORMAL, password);
            }
            return true;
        }

        return false;
    }

    private HashMap<String, String> getDictionary(Activity activity) {
        int mWORD_COUNT = 100;

        if (activity == null) {
            return null;
        }

        BufferedReader br = null;
        HashMap<String, String> dictionary = null;

        AssetManager mgr = activity.getAssets();
        if (mgr == null) {
            Log.w(TAG,
                    "mgr is null. SimplePasswordDictionaryAsset's initialization is failed!");
            return null;
        }

        try {
            InputStream in = mgr.open(DICTIONARY_FILE,
                    AssetManager.ACCESS_BUFFER);

            if (in == null) {
                Log.w(TAG, "dictionaryStream is null!");
                return null;
            }

            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String word = br.readLine();
            if (word == null) {
                Log.w(TAG, "word is null. Dictionary is empty!");
                return null;
            }

            dictionary = new HashMap<String, String>(mWORD_COUNT);

            while (word != null) {
                dictionary.put(word, null);
                word = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (dictionary.size() == 0) {
            Log.w(TAG, "Dictionary is empty!");
            return null;
        }

        return dictionary;
    }

    private String strReverse(String str) {
        String s = "";
        for (int i = 0; i < str.length(); i++) {
            s = str.charAt(i) + s;
        }
        return s;
    }
}
