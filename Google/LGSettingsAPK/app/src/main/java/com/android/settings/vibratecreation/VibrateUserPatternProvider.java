package com.android.settings.vibratecreation;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.database.sqlite.SqliteWrapper;
import android.util.Log;

public class VibrateUserPatternProvider extends ContentProvider {
    private static final String TAG = "VibrateUserPatternProvider";

    public static final String AUTHORITY = "com.android.settings.vibratecreation.VibrateUserPatternProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/vibrateuserpattern");

    public static final String _ID = "_id";
    public static final String VIBRATE_NAME = "name";
    public static final String VIBRATE_PATTERN = "pattern";
    public static final String IS_CALL = "is_call";
    public static final String IS_CALL2 = "is_call2";
    public static final String IS_CALL3 = "is_call3";
    public static final String IS_MESSAGE = "is_message";
    public static final String IS_EMAIL = "is_email";
    public static final String IS_ALARM = "is_alarm";
    public static final String IS_CALENDAR = "is_calendar";

    private static final int ALLOWED = 1;
    private static final int ALLOWED_ID = 2;

    public static final String DEFAULT_SORT_ORDER = VIBRATE_NAME + " ASC";

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "vibrateuserpattern", ALLOWED);
        uriMatcher.addURI(AUTHORITY, "vibrateuserpattern/#", ALLOWED_ID);
    }

    private SQLiteDatabase vibrateuserpatternDB;
    private static final String DATABASE_NAME = "vibrateuserpattern.db";
    private static final String DATABASE_TABLE = "vibrateuserpattern";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_CREATE =
            "    create table " + DATABASE_TABLE +
                    "   (                                       " +
                    _ID + " integer primary key ,    " +
                    VIBRATE_NAME + " text ,           " +
                    VIBRATE_PATTERN + " text ,        " +
                    IS_CALL + " integer default 0,     " +
                    IS_CALL2 + " integer default 0,     " +
                    IS_CALL3 + " integer default 0,     " +
                    IS_MESSAGE + " integer default 0,     " +
                    IS_EMAIL + " integer default 0,     " +
                    IS_ALARM + " integer default 0,     " +
                    IS_CALENDAR + " integer default 0     " +
                    /*MATCH_CONDITION + " integer default 0       " +*/

                    "    )                                              ";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Content provider database", "Upgrading database from version " + oldVersion
                    + " to " + newVersion
                    + ", which will destroy all old data");

            if (oldVersion == 1) {
                db.beginTransaction();
                try {
                    upgradeToVersion2(db);
                    db.setTransactionSuccessful();
                    oldVersion++;
                } catch (Throwable ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
            } else {
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
                onCreate(db);
            }
        }

        private void upgradeToVersion2(SQLiteDatabase db) {
            Log.w("TAG", "upgradeToVersion2");
            //// [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-05-24, Change the special character to empty string to make same format on ICS version.
            //            db.execSQL("UPDATE " + DATABASE_TABLE + " SET " + NUMBER + " = REPLACE(" + NUMBER + ", '-', '')");
            //            db.execSQL("UPDATE " + DATABASE_TABLE + " SET " + NUMBER + " = REPLACE(" + NUMBER + ", ' ', '')");
            //            db.execSQL("UPDATE " + DATABASE_TABLE + " SET " + NUMBER + " = REPLACE(" + NUMBER + ", '(', '')");
            //            db.execSQL("UPDATE " + DATABASE_TABLE + " SET " + NUMBER + " = REPLACE(" + NUMBER + ", ')', '')");
            //// [END_LGE_VOICECALL], MOD, yeobong.yoon, 2012-05-24, Change the special character to empty string to make same format on ICS version.
            //            db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + MATCH_CONDITION + " INTEGER NOT NULL DEFAULT 0");
        }
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // arg0 = uri
        // arg1 = selection
        // arg2 = selectionArgs
        int count = 0;
        switch (uriMatcher.match(arg0)) {
        // [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling
        case ALLOWED:
            Log.d(TAG, "ALLOWED");
            try {
                count = vibrateuserpatternDB.delete(DATABASE_TABLE, arg1, arg2);
            } catch (SQLiteException e) {
                SqliteWrapper.checkSQLiteException(getContext(), e);
            }
            break;

        case ALLOWED_ID:
            String id = arg0.getPathSegments().get(1);
            Log.d(TAG, "ALLOWED_ID=" + id);
            try {
                count = vibrateuserpatternDB.delete(DATABASE_TABLE,
                        _ID + " = " + id + (!TextUtils.isEmpty(arg1) ? " AND (" + arg1 + ')' : ""),
                        arg2);
            } catch (SQLiteException e) {
                SqliteWrapper.checkSQLiteException(getContext(), e);
            }
            break;
        // [END_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling

        default:
            throw new IllegalArgumentException("Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(DATABASE_CREATE);
    }

    /*
    public static String getMatchedItemID(Context context, String start_time, String end_time, String package_name){
        //scheduleEventDB.execSQL("SELECT * FROM system WHERE "+ str);
        String retStr=null;
        Cursor cursor = context.getContentResolver().query(Uri.parse(CONTENT_URI + "/start_time"), null, null, null, null);
        cursor.moveToFirst();


        while(cursor.moveToNext()){
             String tempEndTime = cursor.getString(2);
             Log.d(TAG, "cursor.getString(0)="+cursor.getString(0));
             Log.d(TAG, "cursor.getString(1)="+cursor.getString(1));
             Log.d(TAG, "cursor.getString(2)="+cursor.getString(2));
             String tempPackageName = cursor.getString(3);
             if(end_time.equals(cursor.getString(2)) &&
                     package_name.equals(cursor.getString(3))) {
                      //Toast.makeText(MainActivity.this, "Allowed number!!!", Toast.LENGTH_SHORT).show();
                 Log.d(TAG, "matched sTime="+start_time+" eTime="+end_time+" package="+package_name);
                 retStr = cursor.getString(0);
                 break;
             }
        }
        if(cursor != null)
            cursor.close();
        return retStr;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {

        int count = 0;
        if(uriMatcher.match(arg0) && uriMatcher.match(arg2[0]) && uriMatcher.match(arg2[1]))
        {
                try {
                    count = scheduleEventDB.delete(DATABASE_TABLE, arg1,
                            null);
                } catch (SQLiteException e) {
                    SqliteWrapper.checkSQLiteException(getContext(), e);
                }
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }
    */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case ALLOWED:
            return "vnd.android.cursor.dir/vnd.android.vibrateuserpattern";

        case ALLOWED_ID:
            return "vnd.android.cursor.item/vnd.android.vibrateuserpattern";

        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // ---add a new ---
        // [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling
        long rowID = 0;
        try {
            rowID = vibrateuserpatternDB.insert(DATABASE_TABLE, "", values);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }
        // [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling

        // ---if added successfully---
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        // [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling
        try {
            vibrateuserpatternDB = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
            return false;
        }
        // [END_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling
        return (vibrateuserpatternDB == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);

        if (uriMatcher.match(uri) == ALLOWED_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }

        // [START_LGE_VOICECALL] , ADD, sungjoo.kim , 2012-01-03, side effect fixed
        // [START_LGE_VOICECALL], ADD, daewook.kwon , 2012-01-30 , WBT Test of M4 [START]
        if (sortOrder instanceof String) {
            // [START_LGE_VOICECALL], ADD, daewook.kwon , 2012-01-30 , WBT Test of M4 [END]
            sortOrder = DEFAULT_SORT_ORDER;
        } else {
            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = DEFAULT_SORT_ORDER;
            } // MOD, 2012.01.02, mckihoon.kim, WBT String Null check
        }
        // [END_LGE_VOICECALL] , ADD, sungjoo.kim , 2012-01-03, side effect fixed

        Cursor c = null;
        try {
            c = sqlBuilder.query(vibrateuserpatternDB, projection, selection, selectionArgs, null,
                    null, sortOrder);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }

        // ---register to watch a content URI for changes---
        if (c != null) { // ADD, 2011.12.26, jerome.kim, WBT Null pointer return
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
        case ALLOWED:
            // [START_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling
            try {
                count = vibrateuserpatternDB.update(DATABASE_TABLE, values, selection,
                        selectionArgs);
            } catch (SQLiteException e) {
                SqliteWrapper.checkSQLiteException(getContext(), e);
                return -1;
            }

            break;

        case ALLOWED_ID:
            try {
                count = vibrateuserpatternDB
                        .update(DATABASE_TABLE,
                                values,
                                _ID
                                        + " = "
                                        + uri.getPathSegments().get(1)
                                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                                                + ')' : ""),
                                selectionArgs);
            } catch (SQLiteException e) {
                SqliteWrapper.checkSQLiteException(getContext(), e);
                return -1;
            }
            break;
        // [END_LGE_VOICECALL], MOD, yeobong.yoon, 2012-03-29, Storage full exception handling

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
