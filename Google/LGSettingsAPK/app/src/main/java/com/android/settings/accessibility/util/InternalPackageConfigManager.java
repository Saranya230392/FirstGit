package com.android.settings.accessibility.util;

import android.content.Context;
import android.util.Log;

public class InternalPackageConfigManager {
    public static final String TAG = InternalPackageConfigManager.class.getName();
    private final static String RESOURCE_TYPE_BOOLEAN = "bool";
    private final static String RESOURCE_TYPE_INTEGER = "integer";
    private final static String LG_INTERNAL_PACKAGE_NAME = "com.lge.internal";

    public static boolean getBoolean(Context c, String resName, boolean defaultValue) {
        String id = getResourceId(c, LG_INTERNAL_PACKAGE_NAME, resName, RESOURCE_TYPE_BOOLEAN);

        if (id == null) {
            return defaultValue;
        }

        Boolean b = Boolean.parseBoolean(id);

        if (b == null) {
            Log.d(TAG, "Cant find " + resName + " in package " + LG_INTERNAL_PACKAGE_NAME);
            return false;
        }

        return b.booleanValue();
    }

    public static int getInteger(Context c, String resName, int defaultValue) {
        String id = getResourceId(c, LG_INTERNAL_PACKAGE_NAME, resName, RESOURCE_TYPE_INTEGER);

        if (id == null) {
            return defaultValue;
        }

        Integer i = Integer.valueOf(id);

        if (i == null) {
            Log.d(TAG, "Cant find " + resName + " in package " + LG_INTERNAL_PACKAGE_NAME);
            return -1;
        }

        return i.intValue();
    }

    private static String getResourceId(
            Context c, String packageName, String resName, String type) {
        try {
            int id = c.getResources().getIdentifier(resName, type, packageName);
            return c.getText(id).toString();
        } catch (NullPointerException e) {
            Log.d(TAG, "NullPointerException : " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "resourceException : " + e.getMessage());
        }
        return null;
    }
}
