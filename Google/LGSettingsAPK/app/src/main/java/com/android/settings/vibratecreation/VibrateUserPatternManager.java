/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings.vibratecreation;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlarmManager;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * This service is used to handle calendar event reminders.
 */
public class VibrateUserPatternManager {
    static final boolean DEBUG = true;
    private static final String TAG = "VibrateUserPatternManager";

    private static Context mContext = null;

    static final String[] VIBRATE_USERPATTERN_PROJECTION = new String[] {
            "_id", // 0
            "name", // 1
            "pattern", // 2
    };

    public static final String AUTHORITY = "com.android.settings.vibratecreation.VibrateUserPatternProvider";
    public static final String VIBRATE_USERPATTERN_CONTENT = "content://" + AUTHORITY
            + "/vibrateuserpattern";
    public static final Uri VIBRATE_USERPATTERN_URI = Uri.parse(VIBRATE_USERPATTERN_CONTENT);

    public VibrateUserPatternManager(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    //insert
    public static boolean insertVibrateUserPattern(String name, String pattern) {
        ContentValues insertValue = new ContentValues();
        insertValue.put("name", name);
        insertValue.put("pattern", pattern);
        mContext.getContentResolver().insert(VIBRATE_USERPATTERN_URI, insertValue);
        return true;
    }

    //delete
    public static boolean deleteVibrateUserPattern(String name) {
        Log.d(TAG, "deleteUserPattern");
        String where = "name=?";
        String[] args = new String[] { name };
        mContext.getContentResolver().delete(VIBRATE_USERPATTERN_URI, where, args);
        return true;
    }

    //modified
    public static boolean updateVibrateUserPatternName(String old_name, String new_name) {
        Log.d("hkk", "updateVibrateUserPattern : " + old_name + " " + new_name);
        String _uID = queryUserPatternUniqueID(old_name);
        ContentValues values = new ContentValues();
        values.put("name", new_name);

        String where = "_id=?";
        String[] args = new String[] { _uID };

        mContext.getContentResolver().update(VIBRATE_USERPATTERN_URI, values, where, args);
        return true;
    }

    public static boolean updateVibrateUserPatternDefault(String name, int parent_type,
            int def_value) {

        updateVibrateUserPatternResetDefault(parent_type);
        String _uID = queryUserPatternUniqueID(name);

        Log.d(TAG, "updateVibrateUserPatternDefault" + " _id=" + _uID);

        ContentValues values = new ContentValues();
        values.put(parentDefault_name(parent_type), Integer.toString(def_value));

        String where = "_id=?";
        String[] args = new String[] { _uID };

        mContext.getContentResolver().update(VIBRATE_USERPATTERN_URI, values, where, args);
        return true;
    }

    public static boolean updateVibrateUserPatternResetDefault(int parent_type) {

        Log.d(TAG, "updateVibrateUserPatternResetDefault");

        ContentValues values = new ContentValues();
        values.put(parentDefault_name(parent_type), "0");

        mContext.getContentResolver().update(VIBRATE_USERPATTERN_URI, values, null, null);
        return true;
    }

    private static String parentDefault_name(int parent_type) {
        switch (parent_type)
        {
        case 0:
            return "is_call";
        case 1:
            return "is_call2";
        case 2:
            return "is_message";
        case 3:
            return "is_email";
        case 4:
            return "is_alarm";
        case 5:
            return "is_calendar";
        case 6:
            return "is_call3";
        default:
            break;
        }
        return null;
    }

    //search
    private static String queryUserPatternUniqueID(String name) {
        Log.d(TAG, "queryUserPatternUniqueID");
        String where = "name=?";
        String[] args = new String[] { name };
        String uID = null;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(VIBRATE_USERPATTERN_URI, null, where, args,
                    null);
            Log.d(TAG, "[queryUserPatternUniqueID] getCount :" + c.getCount());
            if (null != c) {
                c.moveToFirst();
                Log.d(TAG, "[queryUserPatternUniqueID] c.getString(0)=" + c.getString(0));
                Log.d(TAG, "[queryUserPatternUniqueID] c.getString(1)=" + c.getString(1));
                Log.d(TAG, "[queryUserPatternUniqueID] c.getString(2)=" + c.getString(2));
                Log.e(TAG, "return uID : " + c.getString(0));
                uID = c.getString(0);
                if (null != c) {
                    c.close();
                }
                return uID != null ? uID : null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[queryUserPatternUniqueID] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[queryUserPatternUniqueID] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        }
        if (null != c) {
            c.close();
        }
        return null;
    }

    public static String queryUserPatternDefaultName(int parent_type) {
        Log.d(TAG, "queryUserPatternDefaultName");

        String where = parentDefault_name(parent_type) + "=?";
        String[] args = new String[] { "1" };
        String uID = null;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(VIBRATE_USERPATTERN_URI, null, where, args,
                    null);
            if (null != c) {
                c.moveToFirst();
                Log.d(TAG, "[queryUserPatternDefault] c.getString(1)=" + c.getString(1));
                Log.e(TAG, "return name : " + c.getString(1));
                uID = c.getString(1);
                if (null != c) {
                    c.close();
                }
                return uID != null ? uID : null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[queryUserPatternDefault] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[queryUserPatternDefault] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        }
        if (null != c) {
            c.close();
        }
        return null;
    }

    public static String queryUserPatternDefaultPattern(int parent_type) {
        Log.d(TAG, "queryUserPatternDefaultPattern");

        String where = parentDefault_name(parent_type) + "=?";
        String[] args = new String[] { "1" };
        String uID = null;
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(VIBRATE_USERPATTERN_URI, null, where, args,
                    null);
            if (null != c) {
                c.moveToFirst();
                Log.d(TAG, "[queryUserPatternDefaultPattern] c.getString(1)=" + c.getString(2));
                Log.e(TAG, "return name : " + c.getString(2));
                uID = c.getString(2);
                if (null != c) {
                    c.close();
                }
                return uID != null ? uID : null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[queryUserPatternDefaultPattern] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[queryUserPatternDefaultPattern] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        }
        if (null != c) {
            c.close();
        }
        return null;
    }

    private String queryUserPattern(String name) {
        Log.d(TAG, "queryUserPattern");
        String pattern = null;
        String where = "name=?";
        String[] args = new String[] { name };
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(VIBRATE_USERPATTERN_URI, null, where, args,
                    null);
            Log.d(TAG, "[queryUserPattern] getCount :" + c.getCount());
            if (null != c) {
                c.moveToFirst();
                Log.d(TAG, "[queryUserPattern] c.getString(0)=" + c.getString(0));
                Log.d(TAG, "[queryUserPattern] c.getString(1)=" + c.getString(1));
                Log.d(TAG, "[queryUserPattern] c.getString(2)=" + c.getString(2));
                Log.e(TAG, "return Name : " + c.getString(2));
                pattern = c.getString(2);
                if (null != c) {
                    c.close();
                }
                return pattern != null ? pattern : null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[queryUserPattern] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[queryUserPattern] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        }
        if (null != c) {
            c.close();
        }
        return null;
    }

    public int getUserPatternCount() {
        Log.d(TAG, "getUserPatternCount");

        Cursor c = null;
        try {
            c = mContext.getContentResolver()
                    .query(VIBRATE_USERPATTERN_URI, null, null, null, null);
            Log.d(TAG, "[getUserPatternCount] getCount :" + c.getCount());
            return c.getCount();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[getUserPatternCount] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return 0;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[getUserPatternCount] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return 0;
        }
    }

    public ArrayList<String> getAllUserPatternName() {
        ArrayList<String> patternNameList_User = new ArrayList<String>();

        Cursor c = null;
        try {
            c = mContext.getContentResolver()
                    .query(VIBRATE_USERPATTERN_URI, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    patternNameList_User.add(c.getString(1));
                    Log.d("hkk", "getAllUserPatternName : " + c.getString(1));
                    c.moveToNext();
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[getUserPatternCount] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[getUserPatternCount] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
        }

        return patternNameList_User;
    }

    public ArrayList<String> getAllUserPattern() {
        ArrayList<String> patternList_User = new ArrayList<String>();

        Cursor c = null;
        try {
            c = mContext.getContentResolver()
                    .query(VIBRATE_USERPATTERN_URI, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    patternList_User.add(c.getString(2));
                    c.moveToNext();
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "[getUserPatternCount] IllegalArgumentException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "[getUserPatternCount] CursorIndexOutOfBoundsException!!!");
            if (null != c) {
                c.close();
            }
            return null;
        }

        return patternList_User;
    }
}
