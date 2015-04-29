package com.android.settings.accessibility.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import com.android.settings.lgesetting.Config.Config;

public class ModelManager {
    private final static String MINIVIEW_APP_NAME = "com.lge.onehandcontroller";
    private static ModelManager sUniqueInstance;
    private String mTargetModelName;
    private String mTargetDevice;
    private String mTargetOperator;
    private String mTargetCountry;
    private boolean mIsNativeCaptionsModel;
    private boolean mIsSprintModel;

    private ModelManager() {
        mTargetModelName = SystemProperties.get("ro.product.model", "COMMON");
        mTargetDevice = SystemProperties.get("ro.product.device", "COMMON");
        mTargetOperator = SystemProperties.get("ro.build.target_operator", "COMMON");
        mTargetCountry = Config.getCountry();

        mIsNativeCaptionsModel = isEqualDeviceNameWith("b1");
        mIsSprintModel = isSprintModel();
    }

    public static ModelManager getInstance() {
        if (sUniqueInstance == null) {
            sUniqueInstance = new ModelManager();
        }
        return sUniqueInstance;
    }

    public boolean isEqualOperatorWith(String operator) {
        if (operator == null) {
            return false;
        }

        return operator.equals(mTargetOperator);
    }

    public boolean isEqualModelNameWith(String modelName) {
        if (modelName == null) {
            return false;
        }

        return modelName.equals(mTargetModelName);
    }

    public boolean isEqualDeviceNameWith(String deviceName) {
        if (deviceName == null) {
            return false;
        }

        return deviceName.equals(mTargetDevice);
    }

    public boolean isEqualCountryWith(String countryName) {
        if (countryName == null) {
            return false;
        }

        return countryName.equals(mTargetCountry);
    }

    public boolean isEqualDeviceNameStartWith(String deviceName) {
        if (deviceName == null) {
            return false;
        }

        int length = deviceName.length();
        String targetDevice2Digit = null;

        try {
            targetDevice2Digit = mTargetDevice.substring(0, length);
            return deviceName.equals(targetDevice2Digit);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private boolean isSprintModel() {
        return isEqualOperatorWith("SPR");
    }

    public boolean isNativeCaptionsModel() {
        return mIsNativeCaptionsModel;
    }

    // The G2 Sprint OSU model does not support Persistent alerts feature.
    public boolean isPersistentAlertsSupported() {
        return mIsSprintModel && !isEqualModelNameWith("LG-LS980");
    }

    public boolean isMiniViewSupported(Context c) {
        PackageManager pm = c.getPackageManager();
        try {
            pm.getApplicationInfo(MINIVIEW_APP_NAME, PackageManager.GET_META_DATA);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
