package com.android.settings.lgesetting.Config;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ConfigHelper {

    private static final String TAG = "ConfigHelper";

    public static boolean getConfigBool(Context context, int resId, String key) {
        try {
            return Config.getFWConfigBool(context, resId, key);
        } catch (NullPointerException e) {
            Log.e(TAG, " NullPointerException : " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, " resourceException : " + e.getMessage());
        }
        return false;
    }

    public static boolean isSupportKnockCode2_0(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_using_knockon_knockcode,
                "com.lge.R.bool.config_using_knockon_knockcode");
    }

    public static boolean isSupportScreenOff(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_knock_off_available,
                "com.lge.R.bool.config_knock_off_available");
    }

    public static boolean isSupportScreenOnOff(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_knockon_available,
                "com.lge.R.bool.config_knockon_available");
    }

    public static boolean isSupportSlideCover(Context context) {

        int id = context.getResources().getIdentifier("config_using_slide_cover", "bool", "com.lge.internal");
        if (id != 0) {
            return context.getResources().getBoolean(id);
        }
        return false;
    }

    public static boolean isSupportRearSideKey(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_rearside_key,
                "com.lge.R.bool.config_rearside_key");
    }

    public static boolean isSupportQuickMemoHotkey(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_quick_memo_hotkey_customizing,
                "com.lge.R.bool.config_quick_memo_hotkey_customizing");
    }

    public static boolean isSupportSharingWithSimHotKey(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_fourth_key_sharing_available,
                "com.lge.R.bool.config_fourth_key_sharing_available");
    }

    public static boolean isSupportShakingGesture(Context context) {
        return getConfigBool(context,
                com.lge.R.bool.config_shaking_gesture,
                "com.lge.R.bool.config_shaking_gesture");
    }

    public static boolean isRemovedFadeoutRington(Context context) {
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            return true;
        }
        return false;
    }
}
