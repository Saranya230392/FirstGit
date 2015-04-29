/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DualIMSIApnProfileReceiver extends BroadcastReceiver{

    private final static String TAG = "DualIMSIApnProfileReceiver";
    private static String IMSIprof1 = null, IMSIprof2 = null;
    private static int ApnIDprof1 = 0, ApnIDprof2 = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentName = intent.getAction();

        if (intentName != null) {

            // Get the app's shared preferences
            SharedPreferences app_preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            IMSIprof1 = app_preferences.getString("IMSIprof1", null);
            IMSIprof2 = app_preferences.getString("IMSIprof2", null);
            ApnIDprof1 = app_preferences.getInt("ApnIDprof1", -1);
            ApnIDprof2 = app_preferences.getInt("ApnIDprof2", -1);

            //IMSIprof1 = "450001022119977"; //TEST

            SharedPreferences.Editor editor = app_preferences.edit();
            Log.i(TAG, "intentName == " + intentName);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intentName) 
                || "com.lge.dualsimTLF.apnTested".equals(intentName)) {

                TelephonyManager mTelephonyMgr = (TelephonyManager)context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String imsi = null;
                if (mTelephonyMgr != null) {
                    imsi = mTelephonyMgr.getSubscriberId();
                    //Log.i(TAG, "IMSI name is --->>> " + imsi);
                    if (imsi == null 
                        //|| !imsi.substring(0, 5).equals("21407")
                        || !isTlfSpainSim(context)
                        || imsi.length() < 5) {
                        Log.i(TAG, "IMSI not available :: return ");
                        return;
                    }
                }

                Bundle bundle2 = null;
                String ss = null;

                if (intent.hasExtra("ss")) {
                    Log.i(TAG, "has extra ss");

                    bundle2 = intent.getExtras();
                    if (bundle2 != null) {
                        ss = bundle2.getString("ss");
                        Log.i(TAG, "has extra ss result " + ss);
                    }
                }

                if (ss != null && ss.equals("LOADED")) {

                    if (imsi != null && imsi.length() > 5) {

                        if (imsi.equals(IMSIprof1)) {
                            Log.i(TAG, "SIM_CHANGE set apn for profile 1 = " + ApnIDprof1);
                            SetDefaultAPN(ApnIDprof1, context);
                        } else if (imsi.equals(IMSIprof2)) {
                            Log.i(TAG, "SIM_CHANGE set apn for profile 2 = " + ApnIDprof2);
                            SetDefaultAPN(ApnIDprof2, context);
                        } else if ((IMSIprof1 != null) 
                                    && (IMSIprof2 == null) 
                                    && !imsi.equals(IMSIprof1)) {
                            // here set default APN if profiles are switched for the first time
                            int apnDef = -1;
                            SetDefaultAPN(apnDef , context);
                            Log.i(TAG, "SIM_CHANGE delete default APN during first switch");
                        } else if (IMSIprof1 != null && IMSIprof2 != null
                                && !imsi.equals(IMSIprof1) && !imsi.equals(IMSIprof2)) {
                            Log.i(TAG, "SIM_CHANGE reset values");
                            DualIMSIApnProfileReceiver.reset();

                        } else {
                            Log.i(TAG, "SIM_CHANGE just set current values");

                        }
                    }

                }

            }

            if ("com.lge.dualsimTLF.apnChanged".equals(intentName)) {

                TelephonyManager mTelephonyMgr = (TelephonyManager)context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String imsi = null;

                if (mTelephonyMgr != null) {
                    imsi = mTelephonyMgr.getSubscriberId();
                    //Log.i(TAG, "IMSI name is --->>> " + imsi);
                    if (imsi == null 
                        //|| !imsi.substring(0, 5).equals("21407")
                        || !isTlfSpainSim(context)
                        || imsi.length() < 5) {
                        Log.i(TAG, "IMSI not available :: return ");
                        return;
                    }
                }

                if (imsi != null) {

                    if (IMSIprof1 == null && IMSIprof2 == null) {

                        IMSIprof1 = imsi;
                        ApnIDprof1 = ReadDefaultAPNid(
                                ReadDefaultAPN(context), context);
                        Log.i(TAG, "ANY_DATA prof1 null and prof2 null, apn id prof1 set to "
                                + ApnIDprof1);

                    } else if (IMSIprof1 != null && IMSIprof2 == null && !imsi.equals(IMSIprof1)) {

                        IMSIprof2 = imsi;
                        ApnIDprof2 = ReadDefaultAPNid(
                                ReadDefaultAPN(context), context);
                        Log.i(TAG, "ANY_DATA prof1 NOTnull and prof2 null apnID prof2 set to "
                                + ApnIDprof2);

                    } else if (imsi.equals(IMSIprof1)) {

                        ApnIDprof1 = ReadDefaultAPNid(
                                ReadDefaultAPN(context), context);
                        Log.i(TAG, "ANY_DATA apnID prof1 set to " + ApnIDprof1);

                    } else if (imsi.equals(IMSIprof2)) {

                        ApnIDprof2 = ReadDefaultAPNid(
                                ReadDefaultAPN(context), context);
                        Log.i(TAG, "ANY_DATA apnID prof2 set to " + ApnIDprof2);

                    } else {
                        Log.i(TAG, "ANY_DATA any profile, reset variables");
                        IMSIprof1 = null;
                        IMSIprof2 = null;

                        ApnIDprof1 = 0;
                        ApnIDprof2 = 0;

                    }

                }

            }

            editor.putString("IMSIprof1", IMSIprof1);
            editor.putString("IMSIprof2", IMSIprof2);
            editor.putInt("ApnIDprof1", ApnIDprof1);
            editor.putInt("ApnIDprof2", ApnIDprof2);
            editor.commit();

        }

    }

    public static void reset() {
        Log.i(TAG, "Button Reset, reset variables");
        IMSIprof1 = null;
        IMSIprof2 = null;

        ApnIDprof1 = 0;
        ApnIDprof2 = 0;

    }

    public final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    public final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    public boolean SetDefaultAPN(final int id, Context context) {
        Log.d(TAG, "SetDefaultAPN start");
        boolean res = false;
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        Log.d(TAG, "SetDefaultAPN apn_id = " + id);

        if (id == -1) {
            Log.e("LGEPL", "Delete default apn");
            resolver.delete(PREFERRED_APN_URI, null, null);
            return true;
        }

        values.put("apn_id", id);
        try {
            resolver.update(PREFERRED_APN_URI, values, null, null);
            Cursor c = resolver.query(PREFERRED_APN_URI, new String[]{"name", "apn"}, "_id=" + id,
                    null, null);
            if (c != null) {
                Log.d(TAG, "SetDefaultAPN succes, added ID ->" + id);
                res = true;
                c.close();
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return res;
    }

    public String ReadDefaultAPN(Context context) {
        Log.d(TAG, "Read default APN");
        Cursor mCursor = context.getContentResolver().query(
                Uri.parse("content://telephony/carriers/preferapn"), 
                new String[]{"name"}, null,
                null, null);
        if (mCursor != null) {
            try {
                if (mCursor.moveToFirst()) {

                    String name0 = mCursor.getString(0);

                    Log.i(TAG, "Default APN NAME is --->>> " + name0);

                    if (mCursor != null) {

                        mCursor.close();
                    }

                    return name0;
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in ReadDefaultAPN " + e.toString());
                e.printStackTrace();
            }

        }
        return null;
    }

    public int ReadDefaultAPNid(final String apnName, Context context) {
        Log.d(TAG, "Read default APN ID INT");

        if (apnName == null || apnName.length() < 1) {
            Log.d(TAG, "Read -1");
            return -1;
        }
        
        String where = "name=\"" + apnName + "\"";
        try {
            Cursor c = context.getContentResolver().query(
                APN_TABLE_URI, 
                null, 
                where, null, null);
            if (c != null) {

                c.moveToFirst();

                String s = c.getString(0);
                Log.d(TAG, "Default APN ID is " + s);
                int resultID = Integer.parseInt(s);

                if (c != null) {
                    c.close();
                }

                return resultID;

            } else {
                Log.e(TAG, "Cursor null");

            }

        } catch (SQLException e) {
            Log.e(TAG, "Exception in ReadDefaultAPNid " + e.toString());
            Log.e(TAG, e.getMessage());
        }

        return -1;
    }

    private boolean isTlfSpainSim(Context context) {
        String mccmnc = ((TelephonyManager)context.getSystemService(
            Context.TELEPHONY_SERVICE)).getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            Log.d(TAG, "isTlfSpainSim : false, mccmnc null ");
            return false;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        //Log.i(TAG, "[Step1] MCC: " + mcc + "/ MNC: " + mnc);
        if (!("214".equals(mcc) && (/* "05".equals(mnc) ||*/ "07".equals(mnc)))) {
            Log.d(TAG, "isTlfSpainSim : false");
            return false;
        }
        Log.d(TAG, "isTlfSpainSim : true");
        return true;
    }

}