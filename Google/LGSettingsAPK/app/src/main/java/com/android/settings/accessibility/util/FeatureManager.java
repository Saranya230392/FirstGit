package com.android.settings.accessibility.util;

import android.content.Context;
import android.os.SystemProperties;

import com.android.internal.telephony.TelephonyProperties;

public class FeatureManager {
    public static ModelManager getManager() {
        return ModelManager.getInstance();
    }

    public static boolean isEqualOperatorWith(String operator) {
        return getManager().isEqualOperatorWith(operator);
    }

    public static boolean isEqualModelNameWith(String modelName) {
        return getManager().isEqualModelNameWith(modelName);
    }

    public static boolean isEqualDeviceNameWith(String deviceName) {
        return getManager().isEqualDeviceNameWith(deviceName);
    }

    public static boolean isEqualDeviceNameStartWith(String deviceName) {
        return getManager().isEqualDeviceNameStartWith(deviceName);
    }

    public static boolean isEqualCountryWith(String countryName) {
        return getManager().isEqualCountryWith(countryName);
    }

    public static boolean isInvertColorSupported(Context c) {
        return InternalPackageConfigManager.getBoolean(c, "config_invert_color_support", false);
    }

    public static boolean isNativeCaptions() {
        return getManager().isNativeCaptionsModel();
    }

    public static boolean isCameraFlashSupported() {
        boolean support = true;
        if (FeatureManager.isEqualDeviceNameStartWith("w3")) {
            support = false;
        }

        return support;
    }

    public static boolean isPersistentAlertsSupported() {
        return getManager().isPersistentAlertsSupported();
    }

    // if the model has MiniView feature
    public static boolean isMiniViewSupported(Context c) {
        return getManager().isMiniViewSupported(c);
    }

    public static boolean isVZWEmergencyCallBackMode() {
        if (!getManager().isEqualOperatorWith("VZW")) {
            return false;
        }

        String ecmMode = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        return ecmMode.equals("true");
    }
}
