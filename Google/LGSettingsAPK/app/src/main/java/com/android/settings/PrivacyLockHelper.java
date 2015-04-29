package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.lge.os.Build;

import java.util.ArrayList;
import java.util.List;

public class PrivacyLockHelper {
    private static final String TAG = PrivacyLockHelper.class.getSimpleName();
    private static final String LOCK_CONDITION = "com.lge.privacylock.condition";

    private static final String PKG_GALLERY = "com.android.gallery3d";
    private static final ArrayList<String> PREDEFINED_PKGS = new ArrayList<String>();

    private static final String MODEL_CODE_HDB = "HDB";

    static {
        PREDEFINED_PKGS.add(PKG_GALLERY);
        PREDEFINED_PKGS.add("com.lge.qmemoplus"); // QMemo+
        PREDEFINED_PKGS.add("com.lge.app.richnote"); // Memo (for backward compatibility)
        PREDEFINED_PKGS.add("com.lge.Notebook"); // Notebook (for backward compatibility)
    }

    enum TIERS {
        TIER_NOT_DEF("NOT_DEF"), // default value
        TIER_NONE("NONE"), // none
        TIER_LOW("LOW"), // less than 1GB RAM
        TIER_MID_LOW("MID_LOW"), // 1GB RAM
        TIER_MID("MID"), // 1.5GB RAM
        TIER_MID_HIGH("MID_HIGH"), // 2GB RAM
        TIER_HIGH("HIGH"); // more than 2GB RAM

        private String mStrTier;

        TIERS(String strTier) {
            mStrTier = strTier;
        }

        public String getText() {
            return mStrTier;
        }

        public static TIERS fromString(String strTier) {
            if (TextUtils.isEmpty(strTier)) {
                return null;
            }

            for (TIERS tier : TIERS.values()) {
                if (strTier.equalsIgnoreCase(tier.mStrTier)) {
                    return tier;
                }
            }

            return null;
        }
    }

    public static boolean isPrivacyLockSupport(Context context) {
        if (context == null) {
            Log.w(TAG, "isPrivacyLockSupport: The context is null.");
            return false;
        }

        if (isSupportSecretMode()) {
            Log.v(TAG,
                    "isPrivacyLockSupport: This is HDB model. So we do not support the PrivacyLock's memu.");
            return false;
        }

        Intent intent = new Intent();
        intent.setClassName("com.lge.privacylock", "com.lge.privacylock.PrivacyLockActivity");
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
        boolean isSupport = (infos != null) && (infos.size() == 1);

        Log.v(TAG, "isPrivacyLockSupport: " + isSupport);

        return isSupport;
    }

    private static boolean isSupportSecretMode() {
        String modelCode = SystemProperties.get("ro.product.model").toLowerCase();
        return MODEL_CODE_HDB.equalsIgnoreCase(modelCode);
    }

    public static String getFormattedSummary(Context context) {
        List<String> lockFeatureApps = new ArrayList<String>();
        String appName = null;

        for (String pkg : PREDEFINED_PKGS) {
            appName = getAppNameIfSupportLockFeature(context, pkg);

            if (!TextUtils.isEmpty(appName) && !lockFeatureApps.contains(appName)) {
                lockFeatureApps.add(appName);
            }
        }

        return makeFormattedSummary(context, lockFeatureApps);
    }

    private static String makeFormattedSummary(Context context, List<String> apps) {
        if (context == null || apps == null || apps.size() <= 0) {
            Log.w(TAG, "makeFormattedSummary: The context or apps list is null.");
            return null;
        }

        final int size = apps.size();

        if (size == 1) {
            return context.getString(R.string.content_lock_desc_single, apps.get(0));
        } else {
            CharSequence rearText = apps.get(size - 1);
            StringBuilder frontText = new StringBuilder();

            for (int i = 0; i < size - 1; i++) {
                if (frontText.length() > 0) {
                    frontText.append(", ");
                }

                frontText.append(apps.get(i));
            }

            return context.getString(R.string.content_lock_desc_multi, new Object[] {
                    frontText, rearText
            });
        }
    }

    private static String getAppNameIfSupportLockFeature(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.w(TAG, "loadAppsDefiedMetadata: The context or pakcage name is null.");
            return null;
        }

        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = null;
        TIERS deviceTierInfo = TIERS.fromString(getTierType(context));
        TIERS requireTierInfo = null;

        try {
            ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException nnfe) {
            return null;
        }

        String metadataValue =
                (ai.metaData == null || !ai.metaData.containsKey(LOCK_CONDITION)) ? null
                        : ai.metaData.getString(LOCK_CONDITION);
        requireTierInfo = TIERS.fromString(metadataValue);

        // assume that if the config_app_tier is TIERS.TIER_NOT_DEF then this is L-OSU model.
        if (deviceTierInfo == TIERS.TIER_NOT_DEF
                && (PKG_GALLERY.equals(packageName) || requireTierInfo != null)) {
            requireTierInfo = TIERS.TIER_NONE;
        } else if (isLessThanLGUI4_2() && PKG_GALLERY.equals(packageName)) {
            requireTierInfo = TIERS.TIER_MID_HIGH;
        }

        Log.v(TAG, "getAppNameIfSupportLockFeature: " + ai.packageName + " requireTier ("
                + ((requireTierInfo == null) ? "null" : requireTierInfo.getText() + ")"));

        return isSupportCondition(deviceTierInfo, requireTierInfo) ? ai.loadLabel(pm).toString()
                : null;
    }

    private static boolean isSupportCondition(TIERS deviceTierInfo, TIERS requireTierInfo) {
        if (requireTierInfo == null) {
            Log.w(TAG, "isSupportCondition: The requireTierInfo is null.");
            return false;
        }

        if (requireTierInfo == TIERS.TIER_NONE) {
            return true;
        }

        deviceTierInfo = (deviceTierInfo == null) ? TIERS.TIER_LOW : deviceTierInfo;

        return (deviceTierInfo.ordinal() >= requireTierInfo.ordinal());
    }

    private static String getTierType(Context context) {
        if (context == null) {
            return null;
        }

        String deviceTier = null;

        try {
            deviceTier = context.getResources().getString(com.lge.R.string.config_app_tier);
            Log.v(TAG, "getTierType: deviceTier is " + deviceTier);
        } catch (NotFoundException nfe) {
            nfe.printStackTrace();
            deviceTier = TIERS.TIER_NOT_DEF.getText();
        }

        return deviceTier;
    }

    private static boolean isLessThanLGUI4_2() {
        return Build.LGUI_VERSION.RELEASE < Build.LGUI_VERSION_NAMES.V4_2;
    }
}
