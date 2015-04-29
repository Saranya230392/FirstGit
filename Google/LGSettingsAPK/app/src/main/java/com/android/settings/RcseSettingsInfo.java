/*
 * 
 */

package com.android.settings;

import android.os.SystemProperties;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import android.app.Activity;
import android.preference.PreferenceGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settings.R;
import android.database.sqlite.SQLiteException;
import com.android.settings.lgesetting.Config.Config;
import java.io.File;
import java.io.FileReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.provider.Settings;

public class RcseSettingsInfo {
    private static final String TAG = "RcseSettingsInfo";
    private static final String RCS_PROFILE = "rcs_profile";
    private static String PREFERENCE_NAME = "gsma.joyn.preferences";
    private static String PREFERENCE_NAME_SETTING = "com.android.settings.gsma.joyn.preferences";
    private static String KEY_NAME = "gsma.joyn.enabled";
    public static final String ACTION_SETTINGS_CHANGED = "com.lge.ims.rcs.SETTINGS_CHANGED";
    public static final String ACTIONSETTINGSCHANGEDRCSE = "com.lge.ims.action.SETTINGS_CHANGED";
    static final String CONTENT_URI = "content://com.lge.ims.provisioning/settings";
    static final String CONTENT_URI_BB = "content://com.lge.ims.rcs/user";
    static final String CONTENT_URI_PROFILE = "content://com.lge.ims.rcs/ac";
    private static SharedPreferences joyn_sharedpreference = null;
    private static SharedPreferences setting_joyn_sharedpreference = null;
    private static Editor joyn_editor = null;
    private static Editor setting_joyn_editor = null;
    private static String mIMSIvalue = null;
    private static String mEnabledPackageName = null;

    //[S]2014.0206. hakgyu98.kim
    //to test without sim card
    //you must set false below 2 values if you finish test.
    public final static boolean isTest = false;
    public final static boolean isBB = false;

    //[E]2014.0206. hakgyu98.kim

    private static void writeDB(Context context, String string, int value) {
        Log.d(TAG, "[wirteDB] string = " + string + " value = " + value);
        ContentValues row = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();
        Intent intent = new Intent();
        row.put(string, value);
        if (readIMSI(context)) {
            try {
                if (!checkProfile(context)) { //rcse
                    Log.d(TAG, "[writeDB] checkprofile false");
                    contentResolver.update(Uri.parse(
                            CONTENT_URI + "/" + mIMSIvalue), row, null, null);

                    intent.setAction(ACTIONSETTINGSCHANGEDRCSE);
                } else { //blackbird
                    Log.d(TAG, "[writeDB] checkprofile true");
                    contentResolver.update(Uri.parse(
                            CONTENT_URI_BB), row, null, null);

                    intent.setAction(ACTION_SETTINGS_CHANGED);
                }

                context.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "[writeDB] exception = " + e.toString());
            }
        }
    }

    public static boolean readIMSI(Context context) {
        //boolean result = false;
        TelephonyManager mTelephonyManager = (TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE);
        mIMSIvalue = mTelephonyManager.getSubscriberId();

        if ((mIMSIvalue == null) || (mIMSIvalue.isEmpty() == true)) {
            mIMSIvalue = "450000000000000";
            Log.d(TAG, "[readIMSI]mIMSIvalue = " + mIMSIvalue);
            return false;
        } else {
            mIMSIvalue = mIMSIvalue.replace(":", "0");
            Log.d(TAG, "[readIMSI]mIMSIvalue = " + mIMSIvalue);
            return true;
        }

        /*    if(IMSI != null){
                result = true;
                Log.e("JJJJJ", IMSI +"");
            }
            else{
                 IMSI = null;    
            }*/
        //return result;
    }

    public static String getIMSIvalue() {
        Log.d(TAG, "[getIMSIvalue] " + mIMSIvalue);
        return mIMSIvalue;
    }

    public static void editPref(Context context, Boolean value) {
        Log.d(TAG, "[editPref]");
        joyn_editor = null;
        setting_joyn_editor = null;

        joyn_sharedpreference = null;
        setting_joyn_sharedpreference = null;

        joyn_sharedpreference = context.getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                        | Context.MODE_MULTI_PROCESS);
        setting_joyn_sharedpreference = context.getSharedPreferences(
                PREFERENCE_NAME_SETTING,
                Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
                        | Context.MODE_MULTI_PROCESS);
        if (!checkProfile(context)) { //rcse
            joyn_editor = joyn_sharedpreference.edit().putBoolean(KEY_NAME, value);
            Log.d(TAG, "[editpref] rcse value = " + value);
        } else { //blackbird
            joyn_editor = joyn_sharedpreference.edit().putBoolean(KEY_NAME, value);
            setting_joyn_editor = setting_joyn_sharedpreference.edit()
                    .putBoolean(KEY_NAME, value);
            setting_joyn_editor.commit();
            Log.d(TAG, "[editpref] blackbird value = " + value);
        }
        Log.d(TAG, "[editPref]joyn_editor = " + joyn_editor);
        Log.d(TAG, "[editPref]setting_joyn_editor = " + setting_joyn_editor);
        joyn_editor.commit();
    }

    public static boolean getJoynEnabled2(Context context, String packageName) {
        Log.d(TAG, "[getJoynEnabled2]");
        boolean result = false;
        String filePath = null;
        FileReader mFreader = null;
        try {
            filePath = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.dataDir;
            filePath += "/shared_prefs/";
            Log.d(TAG, "[getJoynEnabled2]FILE NAME" + packageName + "." + PREFERENCE_NAME_SETTING
                    + ".xml");
            File file = new File(filePath, PREFERENCE_NAME_SETTING + ".xml");
            if (file == null || file.exists() == false) {
                Log.d(TAG, "[getJoynEnabled2]filePath:" + filePath + " Not exists");
                return false;
            }

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            mFreader = new FileReader(file);
            parser.setInput(mFreader);
            int parseEvent = parser.getEventType();
            while (parseEvent != XmlPullParser.END_DOCUMENT) {
                if (parseEvent == XmlPullParser.START_TAG) {
                    String name = parser.getAttributeValue(null, "name");
                    if (KEY_NAME.equals(name)) {
                        String value = parser.getAttributeValue(null, "value");
                        if ("true".equals(value)) {
                            result = true;
                            break;
                        }
                    }
                }
                parseEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            try {
                if (mFreader != null) {
                    mFreader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean getJoynEnabled(Context context, String packageName) {
        Log.d(TAG, "[getJoynEnabled]");
        boolean result = false;
        String filePath = null;
        FileReader mFreader = null;
        try {
            filePath = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.dataDir;
            filePath += "/shared_prefs/";
            File file = new File(filePath, PREFERENCE_NAME + ".xml");
            if (file == null || file.exists() == false) {
                Log.d(TAG, "[getJoynEnabled]filePath:" + filePath + " Not exists");
                return false;
            }

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            mFreader = new FileReader(file);
            parser.setInput(mFreader);
            int parseEvent = parser.getEventType();
            while (parseEvent != XmlPullParser.END_DOCUMENT) {
                if (parseEvent == XmlPullParser.START_TAG) {
                    String name = parser.getAttributeValue(null, "name");
                    if (KEY_NAME.equals(name)) {
                        String value = parser.getAttributeValue(null, "value");
                        if ("true".equals(value)) {
                            result = true;
                            break;
                        }
                    }
                }
                parseEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            try {
                if (mFreader != null) {
                    mFreader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void setEnabledPackageName(String value) {
        mEnabledPackageName = value;
    }

    public static String getEnabledPackageName() {
        return mEnabledPackageName;
    }

    public static boolean checkMultiClientEnabled(Context context) {
        Log.d(TAG, "[checkMultiClient]");
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> infos = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        boolean enabled = false;
        for (ApplicationInfo appInfo : infos) {
            if (appInfo.metaData == null) {
                continue;
            }
            if (appInfo.metaData.containsKey("gsma.joyn.client")) {
                if (appInfo.metaData.containsKey("gsma.joyn.preferences")) {
                    Log.d(TAG, "[checkMultiClient]gsma.joyn.preferences");
                    Log.d(TAG, "[checkMultiClient]joyn package : " + appInfo.packageName);
                    if (appInfo.packageName.equals(context.getApplicationInfo().packageName)) {
                        continue;
                    }

                    enabled |= getJoynEnabled2(context, appInfo.packageName);

                    Log.d(TAG, "[checkMultiClient]joyn package : " + appInfo.packageName
                            + " enabled=" + enabled);

                    if (appInfo.metaData.containsKey("gsma.joyn.settings.activity")) {
                        String temp = null;
                        temp = appInfo.metaData.getString("gsma.joyn.settings.activity");
                        Log.d(TAG, "[checkMultiClient]intentinfo = " + temp);
                        setEnabledPackageName(temp);
                    }
                } else {
                    Log.d(TAG, "[checkMultiClient]joyn package : " + appInfo.packageName);
                    if (appInfo.packageName.equals(context.getApplicationInfo().packageName)) {
                        continue;
                    }
                    enabled |= getJoynEnabled(context, appInfo.packageName);

                    Log.d(TAG, "[checkMultiClient]joyn package : " + appInfo.packageName
                            + " enabled=" + enabled);

                    if (appInfo.metaData.containsKey("gsma.joyn.settings.activity")) {
                        String temp = null;
                        temp = appInfo.metaData.getString("gsma.joyn.settings.activity");
                        Log.d(TAG, "[checkMultiClient]intentinfo = " + temp);
                        setEnabledPackageName(temp);
                    }
                }
            }
        }
        return enabled;
    }

    public static void checkValueChangedandBroadcast(Context context, String string, boolean enabled) {
        Log.d(TAG, "[checkValueChangedandBroadcast]");
        if (string.equals("service_onoff") || string.equals("rcs_e_service")) {
            editPref(context, enabled);
        }
        writeDB(context, string, enabled ? 1 : 0);
    }

    public static boolean checkProfile(Context context) {
        boolean result = true;
        Cursor objCursor = null;
        if (!isTest) {
            try {
                Log.d(TAG, "[checkProfile]CONTENT_URI_PROFILE" + CONTENT_URI_PROFILE);
                objCursor = context.getContentResolver().query(
                        Uri.parse(CONTENT_URI_PROFILE), null, null, null, null);

                if ( objCursor == null) {
                    Log.d(TAG, "[checkProfile]profile : objCursor==null");
                    return true;
                }

                if ( !objCursor.moveToNext()) {
                    Log.d(TAG, "[checkProfile]profile : objCursor.moveToNext()==null");
                    objCursor.close();
                    return true;
                }

                String value = objCursor.getString(objCursor.getColumnIndex(RCS_PROFILE));
                if ( value.equals("joyn_blackbird")) {
                    Log.d(TAG, "[checkProfile]DEFINE VERSION : " + value);
                    objCursor.close();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "[checkProfile]profile : Exception occured - change Default to BB");
                result = true;
            }
        }
        return result | isBB;
    }

    public static String getDateTimeFormatForRCS(Context context) {
        String targetCountry = Config.getCountry();
        Date date = new Date(System.currentTimeMillis());
        String mDateTime = null;
        Log.d(TAG, "Device targetCountry" + targetCountry);
        try {
            SimpleDateFormat sdfNow;
            if ("CN".equals(targetCountry) || "BS".equals(targetCountry)
              || "HU".equals(targetCountry) || "JP".equals(targetCountry)
              || "KR".equals(targetCountry) || "LT".equals(targetCountry)
              || "NP".equals(targetCountry) || "MN".equals(targetCountry)
              || "TW".equals(targetCountry)) {
                 sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            } else if ("BZ".equals(targetCountry) || "PW".equals(targetCountry)
                   || "FM".equals(targetCountry) || "US".equals(targetCountry)) {
                sdfNow = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");
            } else if ("DE".equals(targetCountry)) {
               sdfNow = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");
            } else {
               sdfNow = new SimpleDateFormat("yyyy/dd/MM HH:mm:ss");
            }
               mDateTime = sdfNow.format(date);
        } catch (Exception e) {
               e.printStackTrace();
        }
       return mDateTime;
    }

    public static void setRCSSupportStatus(Context context, boolean value) {
        Settings.System.putString(context.getContentResolver(), "rcs_working", value ? "1" : "0");
        Log.d(TAG, "rcs_working check" +
                Settings.System.getString(context.getContentResolver(), "rcs_working"));
    }
}
