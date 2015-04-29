package com.android.settings.lockscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class LockSettingsFeatureConfig {

    private static final String TAG = "LockSettingsFeatureConfig";
    private static final String LOCKSCREENSETTINGS_PACKAGE = "com.lge.lockscreensettings";
    private static boolean sEnableDebug = "userdebug".equals(Build.TYPE)
            || "eng".equals(Build.TYPE);

    public static boolean isEnabled(Context c, String featureName) {
        PackageManager pm = c.getPackageManager();
        Resources r = null;
        boolean result = false;

        try {
            // support a function about feature_onoff for developer
            if (sEnableDebug && Settings.Secure.getInt(c.getContentResolver(),
                    "dev_feature_onoff", 0) == 1) {
                Context other = null;
                SharedPreferences pref = null;
                try {
                    other = c.createPackageContext("com.lge.lockscreensettings", 0);
                    pref = other.getSharedPreferences("feature_list_prefs",
                        other.MODE_WORLD_READABLE | other.MODE_MULTI_PROCESS);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (pref != null) {
                    return pref.getBoolean(featureName, false);
                }
            } else {
                r = pm.getResourcesForApplication(LOCKSCREENSETTINGS_PACKAGE);
                int resId = r.getIdentifier(featureName, "bool", LOCKSCREENSETTINGS_PACKAGE);
                result = r.getBoolean(resId);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "LockScreenSettings Package not found." + e);
        } catch (NotFoundException e) {
            Log.e(TAG, "Feature not found : feature name = " + featureName);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to get resources : pm = " + pm + ", r = " + r);
        }

        return result;
    }

    public static int getInteger(Context c, String valueName) {
        PackageManager pm = c.getPackageManager();
        Resources r = null;
        int result = 0;

        try {
            r = pm.getResourcesForApplication(LOCKSCREENSETTINGS_PACKAGE);
            int resId = r.getIdentifier(valueName, "integer", LOCKSCREENSETTINGS_PACKAGE);
            result = r.getInteger(resId);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "LockScreenSettings Package not found." + e);
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource not found : integer name = " + valueName);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to get resources : pm = " + pm + ", r = " + r);
        }

        return result;
    }

    public static int[] getIntArray(Context c, String arrayName) {
        PackageManager pm = c.getPackageManager();
        Resources r = null;
        int[] result = null;

        try {
            r = pm.getResourcesForApplication(LOCKSCREENSETTINGS_PACKAGE);
            int resId = r.getIdentifier(arrayName, "array", LOCKSCREENSETTINGS_PACKAGE);
            result = r.getIntArray(resId);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "LockScreenSettings Package not found." + e);
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource not found : array name = " + arrayName);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to get resources : pm = " + pm + ", r = " + r);
        }

        return result;
    }

    public static String getString(Context c, String stringName) {
        PackageManager pm = c.getPackageManager();
        Resources r = null;
        String result = null;

        try {
            r = pm.getResourcesForApplication(LOCKSCREENSETTINGS_PACKAGE);
            int resId = r.getIdentifier(stringName, "string", LOCKSCREENSETTINGS_PACKAGE);
            result = r.getString(resId);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "LockScreenSettings Package not found." + e);
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource not found : string name = " + stringName);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to get resources : pm = " + pm + ", r = " + r);
        }

        return result;
    }

    public static String[] getStringArray(Context c, String arrayName) {
        PackageManager pm = c.getPackageManager();
        Resources r = null;
        String[] result = null;

        try {
            r = pm.getResourcesForApplication(LOCKSCREENSETTINGS_PACKAGE);
            int resId = r.getIdentifier(arrayName, "array", LOCKSCREENSETTINGS_PACKAGE);
            result = r.getStringArray(resId);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "LockScreenSettings Package not found." + e);
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource not found : array name = " + arrayName);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to get resources : pm = " + pm + ", r = " + r);
        }

        return result;
    }

}
