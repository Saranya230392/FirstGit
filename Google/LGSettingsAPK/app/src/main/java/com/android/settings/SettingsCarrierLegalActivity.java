/*
 * Copyright (C) 2009 The Android Open Source Project
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

//[2012.06.06][munjohn.kang]added Chameleon Carrier Legal Information

package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemProperties; //[2012.06.23][munjohn.kang] added a code to change the Chameleon legal verbiage by Networkcode
import android.util.Log;

import com.android.settings.lgesetting.Config.Config;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;
import android.text.TextUtils;

import android.net.Uri;

/**
 * The "dialog" that shows from "Carrier Legal Information" in the Settings app.
 */

public class SettingsCarrierLegalActivity extends Activity implements
        DialogInterface.OnCancelListener, DialogInterface.OnClickListener {

    private static final String TAG = "SettingsCarrierLegalActivity";

    private static final String DB_NAME = "legal.db";
    private static final String DB_TABLE = "carrierLegal";
    //private static final String CARRIER_DB = "/carrier/legal.db";

    private AlertDialog mPrivacyAlert = null;
    private AlertDialog mPrivacyDetail = null;

    SQLiteDatabase myDatabase;
    private String mTitle = null;
    //private String mAlert = null;
    private String mAlert1 = null;
    private String mAlert2 = null;
    //private String mContent1 = null;
    //private String mContent2 = null;
    //private String mContent3 = null;

    private String mHomePageApply;
    private String mBrandAlpha;
    private String mDefautlHomePage = "homepage";
    private String mDefautlCarrier = "Your carrier";
    //private boolean mCarrier = false;
    private static final Uri NODE_URI = Uri.parse("content://com.lge.node/omadm_node");

    private static final String[] PROJECTION = {
            "key",
            "value",
    };
    private static final String SELECTION = "key=?";
    private static final String CARRIER_NODE = "Carrier_legal";
    private static final String BRANDALPHA_NODE = "Brandalpha_legal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        boolean bcheck_HFA_Status = check_HFA_status();
        boolean bcheck_Payload = check_Payload();
        //boolean b_setPreloaded_Data = false;

        // Test
        //bcheck_HFA_Status = true;
        //bcheck_Payload = true;

        mHomePageApply = readCarrierFile(CARRIER_NODE); //Settings.System.getString(getContentResolver(), "home_page");
        mBrandAlpha = readCarrierFile(BRANDALPHA_NODE); //Settings.System.getString(getContentResolver(), "brand_alpha");

        Log.i("starmotor", "mHomePageApply DB = " + mHomePageApply);
        Log.i("starmotor", "mBrandAlpha DB = " + mBrandAlpha);

        //        Settings.System.putString(getContentResolver(), "home_page", mHomePageApply);
        //        Settings.System.putString(getContentResolver(), "brand_alpha", mBrandAlpha);

        deleteDBFile(this); //[2012.08.09][munjohn.kang] delete legal.db file

        Log.i(TAG, "bcheck_HFA_Status = " + bcheck_HFA_Status + ", bcheck_Payload = "
                + bcheck_Payload);
        PrivacyAlertDialog();
        /*
        if (bcheck_HFA_Status) {
        //            if (bcheck_Payload) {
                if (set_value_from_db(CARRIER_DB)) {
                    Log.i(TAG, "success get db:" + CARRIER_DB);
                } else {
                    Log.i(TAG, "failed get db:" + CARRIER_DB);

                    //[S][2012.08.10][munjohn.kang] If DB data is null in carrier patition brings generic DB data.
                    Log.i(TAG, "If DB data is null in carrier patition brings generic DB data.");
                    b_setPreloaded_Data = false; // generic Carrier Legal database create

                    create_database(b_setPreloaded_Data);

                    if (set_value_from_db(DB_NAME)) {
                        Log.i(TAG, "success get db:" + DB_NAME);
                    } else {
                        Log.i(TAG, "failed get db:" + CARRIER_DB);
                        finish();
                    }
                    //[E][munjohn.kang]
                }
            } else {
                b_setPreloaded_Data = true; // BM or SPR Carrier Legal database create
                Log.i(TAG, "not found " + CARRIER_DB + " and create Legal db.");

                create_database(b_setPreloaded_Data);

                if (set_value_from_db(DB_NAME)) {
                    Log.i(TAG, "success get db:" + DB_NAME);
                } else {
                    Log.i(TAG, "failed get db:" + CARRIER_DB);
                    finish();
                }
            }
            
        } else {
            b_setPreloaded_Data = false; // generic Carrier Legal database create

            create_database(b_setPreloaded_Data);

            if (set_value_from_db(DB_NAME)) {
                Log.i(TAG, "success get db:" + DB_NAME);
            } else {
                Log.i(TAG, "failed get db:" + CARRIER_DB);
                finish();
            }
        }
        // display alert dialog
        PrivacyAlertDialog();
        if (mTitle != null && mAlert != null && mContent1 != null
                && mContent2 != null && mContent3 != null) {
            PrivacyAlertDialog();
        } else {
            Log.e(TAG, "create AlertDialog failed");
        }
        */
    }

    private String readCarrierFile(String nodeKey) {
        String result = null;
        Cursor cursor = null;
        //        ContentResolver mResolver = mContext.getContentResolver();
        try {
            cursor = getContentResolver().query(NODE_URI, PROJECTION, SELECTION,
                    new String[] { nodeKey }, null);

            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(1); // 1 : value.
                Log.e(TAG, "result : " + result);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume ()");
    }

    private boolean check_HFA_status() {
        String phoneActivated = SystemProperties.get("ro.sprint.hfa.flag");

        //[2012.08.10][munjohn.kang] Fix WBT
        if (phoneActivated != null)
        {
            Log.i(TAG, "phoneActivated = " + phoneActivated);
            return (phoneActivated.equals("activationOK"));
        }
        else
        {
            return false;
        }
    }

    private boolean check_Payload() {
        String payloadReceived = SystemProperties.get("ro.sprint.chameleon.flag");

        //[2012.08.10][munjohn.kang] Fix WBT
        if (payloadReceived != null)
        {
            Log.i(TAG, "payloadReceived = " + payloadReceived);
            return (payloadReceived.equals("payloadOK"));
        }
        else
        {
            return false;
        }
    }

    //carrier regal table type
    private static final String TABLE_CREATE =
            "create table " + DB_TABLE +
                    "(ID integer primary key autoincrement," +
                    "TITLE text," +
                    "ALERT text," +
                    "LANGUAGE text," +
                    "CONTENT_1 text," +
                    "CONTENT_2 text," +
                    "CONTENT_3 text" +
                    ")";

    //              added a code to change the Chameleon legal verbiage by Networkcode
    //              String networkCode = SystemProperties.get("ro.cdma.home.operator.numeric", "111111");
    //              Log.i(TAG, "networkCode = "+ networkCode);
    //              if(networkCode.equals("310120"))//Sprint if NetworkCode is 310120
    //                  myDatabase.insert(DB_TABLE, null, insert_db_item(getResources().getStringArray(R.array.sp_carrier_legal_spr_NORMAL)));
    //              else if(networkCode.equals("311870"))//Boost Mobile if NetworkCode is 311870
    //                  myDatabase.insert(DB_TABLE, null, insert_db_item(getResources().getStringArray(R.array.sp_carrier_legal_bm_NORMAL)));
    //              else if(networkCode.equals("311490"))//Virgin Mobile if NetworkCode is 311490 (when in scope)
    //                  myDatabase.insert(DB_TABLE, null, insert_db_item(getResources().getStringArray(R.array.sp_carrier_legal_virgin_NORMAL)));

    private void create_database(boolean bPayload_Data) {
        //final Configuration configuration = getResources().getConfiguration();
        myDatabase = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        try {
            myDatabase.query(DB_TABLE, null, null, null, null, null, null);
        } catch (Exception e) {
            // Log.i(TAG, "createDataBase Exception e:" + e.getMessage());
            myDatabase.execSQL(TABLE_CREATE);

            if (myDatabase != null && myDatabase.isOpen()) {
                if (bPayload_Data) { // Boost Mobile Carrier Legal database create
                    if ("http://sprint.com".equals(mHomePageApply)) {
                        myDatabase.insert(DB_TABLE, null, insert_db_item(getResources()
                                .getStringArray(R.array.sp_carrier_legal_spr_NORMAL)));
                    } else if ("http://boostmobile.com".equals(mHomePageApply)) {
                        myDatabase.insert(DB_TABLE, null, insert_db_item(getResources()
                                .getStringArray(R.array.sp_carrier_legal_bm_NORMAL)));
                    } else if ("http://virginmobileusa.com".equals(mHomePageApply)) {
                        myDatabase.insert(DB_TABLE, null, insert_db_item(getResources()
                                .getStringArray(R.array.sp_carrier_legal_virgin_NORMAL)));
                    } else {
                        myDatabase.insert(DB_TABLE, null, insert_db_item(getResources()
                                .getStringArray(R.array.sp_carrier_legal_NORMAL)));
                    }
                } else { // generic Carrier Legal database create
                    myDatabase.insert(
                            DB_TABLE,
                            null,
                            insert_db_item(getResources().getStringArray(
                                    R.array.sp_carrier_legal_NORMAL)));
                }
            } else {
                Log.e(TAG,
                        "myDatabase == null or createDataBase isOpen status :"
                                + myDatabase.isOpen());
            }
        } finally {
            if (myDatabase != null) {
                myDatabase.close();
            }
        }
    }

    private ContentValues insert_db_item(String[] presetStrings) {
        if (presetStrings != null) {
            ContentValues cVals = new ContentValues();

            cVals.put("TITLE", presetStrings[0]);
            cVals.put("ALERT", presetStrings[1]);
            //[S][2012.06.07][munjohn.kang] fix TMS Sync error
            //cVals.put("LANGUAGE", presetStrings[2]);
            cVals.put("LANGUAGE", "EN");
            cVals.put("CONTENT_1", presetStrings[2]);
            cVals.put("CONTENT_2", presetStrings[3]);
            cVals.put("CONTENT_3", presetStrings[4]);
            //[E][munjohn.kang]
            return cVals;
        }
        return null;
    }

    /*
    private boolean set_value_from_db(String db_name) {
        Log.i(TAG, "set_value_from_db : " + db_name);

        final Configuration configuration = getResources().getConfiguration();
        final String language = configuration.locale.getLanguage();

        Cursor cursor = null;

        try {
            Log.i(TAG, "openOrCreateDatabase1 :" + db_name);

            if (db_name.equals(CARRIER_DB))
            {
                // [2012.11.26][munjohn.kang] add to permission Carrier_Legal DB for carrier partition in JellyBean. add SQLiteDatabase.OPEN_READONLY.
                myDatabase = SQLiteDatabase.openDatabase(db_name, null,
                        SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }
            else
            {
                myDatabase = openOrCreateDatabase(db_name, SQLiteDatabase.OPEN_READONLY, null);
            }

            cursor = myDatabase.query(DB_TABLE, null, null, null, null, null, null);

            cursor.moveToFirst();

            //[S][2012.07.18][munjohn.kang] add to control for carrier partition
            if (db_name.equals(CARRIER_DB) &&
                    (language.equalsIgnoreCase("ES") || language.equalsIgnoreCase("EN")))
            {
                Log.i(TAG, "cursor.getString(3) " + cursor.getString(3));
                while (cursor.isAfterLast() == false) {
                    if (cursor.getString(3).equalsIgnoreCase(language)) {
                        Log.i(TAG, "load data from " + db_name);
                        mTitle = cursor.getString(1);
                        //mAlert = cursor.getString(2);
                        //mContent1 = cursor.getString(4);
                        //mContent2 = cursor.getString(5);
                        //mContent3 = cursor.getString(6);
                    }
                    cursor.moveToNext();
                }
            }
            else
            //[E][munjohn.kang]
            {
                // set for default language
                Log.i(TAG, "cursor.getString(3)default " + cursor.getString(3));
                mTitle = cursor.getString(1);
                //mAlert = cursor.getString(2);
                //mContent1 = cursor.getString(4);
                //mContent2 = cursor.getString(5);
                //mContent3 = cursor.getString(6);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "openOrCreateDatabase SQLiteException e = " + e);
            // [2012.06.27][munjohn.kang]WBT
            Log.e(TAG, "db_name = " + db_name);
            //if (db_name.equals(CARRIER_DB))
            //    Toast.makeText(this, "DB open Error!", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            if (cursor != null)
            {
                cursor.close();
            }

            if (myDatabase != null)
            {
                myDatabase.close();
            }
        }
        return true;
    }
    */
    private void PrivacyAlertDialog() {
        Log.i(TAG, "PrivacyAlertDialog");
        Log.i("starmotor", "mHomePageApply  = " + mHomePageApply);
        Log.i("starmotor", "mHomePageApply  = " + mBrandAlpha);

        if (TextUtils.isEmpty(mBrandAlpha)
                || ("SPR".equals(Config.getOperator())
                        && ("LG".equals(mBrandAlpha) || "Chameleon".equals(mBrandAlpha)))) {
            Log.i("starmotor", "mBrandAlpha empty ");
            mTitle = getString(R.string.sp_carrier_legal_title_default_NORMAL);
            mAlert1 = getString(R.string.sp_carrier_legal_title_detail_NORMAL, mDefautlCarrier);
        } else {
            mTitle = getString(R.string.sp_carrier_legal_title_NORMAL, mBrandAlpha);
            mAlert1 = getString(R.string.sp_carrier_legal_title_detail_NORMAL, mBrandAlpha);

        }
        if (TextUtils.isEmpty(mHomePageApply)) {
            Log.i("starmotor", "mHomePageApply empty ");
            mAlert2 = getString(R.string.sp_carrier_legal_detail_NORMAL, mDefautlCarrier,
                    "my carrier", mDefautlCarrier, mDefautlCarrier, mDefautlHomePage);
        } else {
            mAlert2 = getString(R.string.sp_carrier_legal_detail_NORMAL, mBrandAlpha, mBrandAlpha,
                    mBrandAlpha, mBrandAlpha, mHomePageApply);
        }
        mPrivacyAlert = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setOnCancelListener(this)
                .setNegativeButton(android.R.string.cancel, this)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                PrivacyAlert_Detail();
                            }
                        }).create();
        mPrivacyAlert.setTitle(mTitle);
        mPrivacyAlert.setMessage(mAlert1);
        mPrivacyAlert.show();
    }

    private void PrivacyAlert_Detail() {
        Log.i(TAG, "PrivacyAlert_Detail");
        if (mPrivacyAlert != null) {
            mPrivacyAlert.dismiss();
            mPrivacyAlert = null;
        }

        mPrivacyDetail = new AlertDialog.Builder(this)
                .setMessage(mAlert2)
                .setCancelable(true).create();
        mPrivacyDetail.show();
        mPrivacyDetail.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                // TODO Auto-generated method stub
                SettingsCarrierLegalActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        // TODO Auto-generated method stub

        super.onDestroy();

        // [2012.06.11][munjohn.kang]added code to delete the DB file
        deleteDBFile(this);

        if (mPrivacyAlert != null) {
            mPrivacyAlert.dismiss();
            mPrivacyAlert = null;
        }

        if (mPrivacyDetail != null) {
            mPrivacyDetail.dismiss();
            mPrivacyDetail = null;
        }

        // [S][2012.06.27][munjohn.kang]WBT
        if (myDatabase != null)
        {
            myDatabase.close();
        }
        // [E][munjohn.kang]
    }

    // [S][2012.06.11][munjohn.kang]added code to delete the DB file
    private void deleteDBFile(Context ctx) {
        try {
            ctx.deleteDatabase(DB_NAME);
            ctx.deleteDatabase(DB_NAME + "-journal");
            Log.i(TAG, "deleteDBFile ok");
        } catch (Exception e) {
            Log.i(TAG, "deleteDBFile failed. Exception e = " + e);
        }
    }

    // [E][munjohn.kang]

    @Override
    public void onClick(DialogInterface arg0, int arg1) {
        Log.i(TAG, "onClick");
        // TODO Auto-generated method stub
        finish();
    }

    @Override
    public void onCancel(DialogInterface arg0) {
        Log.i(TAG, "onCancel");
        // TODO Auto-generated method stub
        finish();
    }
}
