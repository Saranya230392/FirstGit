package com.android.settings.sdencryption;

import java.io.File;
import android.os.SystemProperties;
import android.util.Log;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.IMountService;
import android.os.IBinder;
import android.os.ServiceManager;
import android.content.Context;


import com.android.settings.SecuritySettings;

import com.android.settings.lgesetting.Config.Config;

public class Encryption_Data {
    private static final String TAG = "Encryption_Data";
    //private static final String TEMP_DIRECTORY = "/.ecryptfs_tmp"; // not used
    private static boolean mMediaOption;
    private static boolean mFullOption;
    private static boolean mSDEncryptionConfirm;
    private static boolean mSpacesError;
    public static final int ENABLE_FULL_CASE = 0;
    public static final int ENABLE_NORMAL_CASE = 1;
    public static final int DISABLE_FULL_CASE = 2;
    public static final int DISABLE_NORMAL_CASE = 3;
    private static boolean mCheckStorageVolume;

    private static final String MEDIA_PROPERTY_VALUE = "persist.security.sdmediacrypto";
    private static final String FULL_PROPERTY_VALUE = "persist.security.sdfullcrypto";
    private static long mMaxFileSize;
    private static long mTotalMemory;
    private static long availableSize;
    private static String externalPath;
    private static boolean mPossibleEncryption;

    private static boolean mLevelOK_prev;
    private static boolean mPluggedOK_prev;
    public static int mBatteryLevel = 0;
    private static boolean mIsProgressing = false;
    private static int mPropertyType = 0;

    private static int mErrorCode = 0;
    public static final int ERROR_SPACE_LOW = 1;
    public static final int ERROR_MOVE_FAIL = 2;

    public static IMountService sMountService;


    public static void setProgressing(boolean isProgressing) {
        mIsProgressing = isProgressing;
    }

    public static boolean isProgressing() {
        return mIsProgressing;
    }

    public static void setMediaOption(boolean mediaenable) {
        if (mIsProgressing) {
            return;
        }
        mMediaOption = mediaenable;
    }

    public static boolean getMediaOption() {
        return mMediaOption;
    }

    public static void setFullOption(boolean fullenable) {
        if (mIsProgressing) {
            return;
        }
        mFullOption = fullenable;
    }

    public static boolean getFullOption() {
        return mFullOption;
    }

    public static void setSDEncryptionConfirm(boolean SDEncryptionConfirm) {
        mSDEncryptionConfirm = SDEncryptionConfirm;
    }

    public static boolean getSDEncryptionConfirm() {
        return mSDEncryptionConfirm;
    }

    public static void setSDEncryptionSpacesError(boolean SpacesError) {
        mSpacesError = SpacesError;
    }

    public static boolean getSDEncryptionSpacesError() {
        return mSpacesError;
    }

    public static void setSDEncryptionError(int error) {
        mErrorCode = error;
    }

    public static int getSDEncryptionError() {
        return mErrorCode;
    }

    public static void setCheckStorageVolume(boolean mStorageVolume) {
        if (mIsProgressing) {
            return;
        }
        mCheckStorageVolume = mStorageVolume;
    }

    public static boolean getCheckStorageVolume() {
        return mCheckStorageVolume;
    }

    public static long getCheckSDcardTotalMemory() {
        return mTotalMemory;
    }

    public static void setExternalSDCardPath(String mExternalPath) {
        externalPath = mExternalPath;
    }

    public static String getExternalSDCardPath() {
        return externalPath;
    }

    public static boolean getPossibleEncryption() {
        return mPossibleEncryption;
    }

    public static void SDEncryption_setOption() {
        if (SystemProperties.get(FULL_PROPERTY_VALUE, "0").equals("1")) {
            setFullOption(true);
        }else {
            setFullOption(false);
        }

        if (SystemProperties.get(MEDIA_PROPERTY_VALUE, "0").equals("1")) {
            setMediaOption(true);
        }else {
            setMediaOption(false);
        }
    }

    public static int SDEncryption_getCryptMsg() {
        if (mIsProgressing) {
            return mPropertyType;
        }

        if (SecuritySettings.SDEncryption_checkEnableProperty() == false) {
            if ((getFullOption() == true) && (mCheckStorageVolume == true)) {
                mPropertyType = DISABLE_FULL_CASE;
            }else {
                mPropertyType = DISABLE_NORMAL_CASE;
            }
        }else {
            if ((getFullOption() == true) && (mCheckStorageVolume == true)) {
                mPropertyType = ENABLE_FULL_CASE;
            }else {
                mPropertyType = ENABLE_NORMAL_CASE;
            }
        }

        Log.d(TAG, "getCryptMsg is = " + mPropertyType);

        return mPropertyType;
    }

    public static void checkPossibleEncryption() {
        if (externalPath == null) {
            externalPath = "/storage/sdcard1";
        }
        mPossibleEncryption = true;
        mTotalMemory = 0;
        mMaxFileSize = 0;
        File fileList = new File(externalPath);
        availableSize = fileList.getFreeSpace();
        GetMaxFileSize(externalPath);

        Log.d(TAG, "mTotalMemory : " + mTotalMemory + ",mMaxFileSize : " + mMaxFileSize + ",availableSize : " + availableSize);
    }

    public static void GetMaxFileSize(String path) {
        File fileList = new File(path);
        File[] files = fileList.listFiles();

        if ((files != null) && (mPossibleEncryption == true)) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    if (!files[i].getName().equals("LOST.DIR")) {
                        GetMaxFileSize(files[i].getPath());
                    }
                } else {
                    if (mMaxFileSize < files[i].length()) {
                        mMaxFileSize = files[i].length();
                        if (mMaxFileSize > availableSize) {
                            mPossibleEncryption = false;
                        }
                    }
                    mTotalMemory += files[i].length();
                }
            }
        }
    }

    public static void setBatteryStatus(boolean levelOK, boolean pluggedOK) {
        mLevelOK_prev = levelOK;
        mPluggedOK_prev = pluggedOK;
    }

    public static boolean isBatteryStatusChanged(boolean levelOK, boolean pluggedOK) {
        if ((mLevelOK_prev != levelOK) || (mPluggedOK_prev != pluggedOK)) {
            return true;
        }
        return false;
    }

    public static void setBatteryLevel(int level) {
        mBatteryLevel = level;
    }

    public static int getBatteryLevel() {
        return mBatteryLevel;
    }

    public static void setDISASDcardValue(Context context) {
    if (Config.getFWConfigBool(context,
        com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
        "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")) {
             SystemProperties.set("persist.security.sdc.enabled", "2");
         }
    }

    public static String getSDCardStorageState(Context context, 
                                              StorageManager mStorageManager) {
        if (mStorageManager == null) {
            mStorageManager = (StorageManager)context.getSystemService(context.STORAGE_SERVICE);
            if (mStorageManager == null) {
                Log.w(TAG, "Failed to get StorageManager");
            }
        }
        try {
            return mStorageManager.getVolumeState(getExternalSDCardPath());
        } catch (Exception e) {
            Log.w(TAG, "Failed to read SDCard storage state; assuming REMOVED: " + e);
            return Environment.MEDIA_REMOVED;
        }
    }

    public static synchronized IMountService getMountService() {
       if (sMountService == null) {
           IBinder service = ServiceManager.getService("mount");
           if (service != null) {
               sMountService = IMountService.Stub.asInterface(service);
           }
       }
       return sMountService;
    }

    public static void checkStorageVolume(Context context,
                                          StorageManager mStorageManager) {
        String mStateSDCard = "";
        setCheckStorageVolume(false);
        if (mStorageManager == null) {
            Log.w(TAG, "Failed to get StorageManager");
            return;
        }

        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        if (storageVolumes == null) {
            Log.w(TAG, "Failed to get StorageVolumes");
            return;
        }

        mStateSDCard =  getSDCardStorageState(context, mStorageManager);
        Log.d(TAG, "SD Card state :" + mStateSDCard);

        if (!Environment.MEDIA_MOUNTED.equals(mStateSDCard)) {
            setCheckStorageVolume(false);
        }else {
            setCheckStorageVolume(true);
        }
    }
}
