/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserManager;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemProperties;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageMeasurement.MeasurementDetails;
import com.android.settings.deviceinfo.StorageMeasurement.MeasurementReceiver;
import com.android.settings.lgesetting.Config.Config;
import com.google.android.collect.Lists;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import com.lge.constants.SettingsConstants;

//2013-05-31 taesu.jung@lge.com 3LM SD Encryption Solution[start]
import android.os.UserHandle;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.provider.Settings;
import android.content.DialogInterface;
import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;

//2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
public class StorageVolumePreferenceCategory extends PreferenceCategory {
    private static final String LOG_TAG = "StorageVolumePreferenceCategory";
    public static final String KEY_CACHE = "cache";

    private static final int ORDER_STORAGE_LOW = -3;
    private static final int ORDER_TOTAL_MEMORY_VZW = -2;
    private static final int ORDER_USAGE_BAR = -1;

    /** Physical volume being measured, or {@code null} for internal. */
    private final StorageVolume mVolume;
    private final StorageMeasurement mMeasure;

    private final Resources mResources;
    private final StorageManager mStorageManager;
    private final UserManager mUserManager;

    private UsageBarPreference mUsageBarPreference;
    private Preference mMountTogglePreference;
    private Preference mFormatPreference;
    private Preference mStorageLow;

    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution
    private Preference mEncryptPreference;
    private StorageItemPreference mItemTotal;
    private StorageItemPreference mItemAvailable;
    private StorageItemPreference mItemApps;
    private StorageItemPreference mItemDcim;
    private StorageItemPreference mItemMusic;
    private StorageItemPreference mItemDownloads;
    // [seungyeop.yeom][2014-03-21] display capacity of TV app
    private StorageItemPreference mItemTVapps;
    private StorageItemPreference mItemCache;
    private StorageItemPreference mItemMisc;
    private List<StorageItemPreference> mItemUsers = Lists.newArrayList();
    private StorageVolume mStorageVolume;

    // [SD CARD][seungyeop.yeom][2014-02-05] Save to sd card in Camera
    private CheckBoxPreference mSaveSDcardCamera;

    // [BUA+] Subcribe or Upgrade
    private StorageItemPreference mItemUserOption;

    // [VZW] H/W memory information
    private StorageItemPreference mItemHWTotal;
    protected boolean isExistedBUACategory = false;
    protected boolean isExistedHWTotalMenu = false;
    private static long mVZWTotalSpace = 0;
    protected static long miscForSystemData = 0;

    private boolean mUsbConnected;
    private String mUsbFunction;
    private String mTotalSpaceSummary;

    // [OTG]
    private int indexOfUSBStorage = 0;
    private String mount_usb_storage_title;
    private String mount_usb_storage_summary;
    private String unmount_usb_storage_title;
    private String unmount_usb_storage_summary;
    private String format_usb_storage_title;
    private String format_usb_storage_summary;
    private String thisVolumePath = null;
    private long mTotalSize;
    protected boolean isNeededToCheckUSBRefresh = false;

    private static final int MSG_UI_UPDATE_APPROXIMATE = 1;
    private static final int MSG_UI_UPDATE_DETAILS = 2;

    private Context mContext;

    // [2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
    private boolean mDisplayUnit = true;
    private static final boolean MORE_THAN_UI_4_2 = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;

    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UI_UPDATE_APPROXIMATE: {
                final long[] size = (long[])msg.obj;
                updateApproximate(size[0], size[1], size[2]);
                break;
            }
            case MSG_UI_UPDATE_DETAILS: {
                final MeasurementDetails details = (MeasurementDetails)msg.obj;
                if (!Utils.isUI_4_1_model(mContext)) {
                    if (Memory.isVZWOperator) {
                        updateDetails_vzw(details);
                    } else {
                        updateDetails(details);
                    }
                } else {
                    updateDetails_vzw(details);
                }
                break;
            }
            default:
                break;
            }
        }
    };

    /**
     * Build category to summarize internal storage, including any emulated
     * {@link StorageVolume}.
     */
    public static StorageVolumePreferenceCategory buildForInternal(Context context) {
        return new StorageVolumePreferenceCategory(context, null, 0, null);
    }

    /**
     * Build category to summarize specific physical {@link StorageVolume}.
     */
    public static StorageVolumePreferenceCategory buildForPhysical(
            Context context, StorageVolume volume) {
        return new StorageVolumePreferenceCategory(context, volume, 0, null);
    }

    /**
     * Build category to summarize BUA+ media information
     */
    public static StorageVolumePreferenceCategory buildForBUA(Context context) {
        return new StorageVolumePreferenceCategory(context, null, 0, null);
    }

    /**
     * Build category to summarize H/W Memory information
     */
    public static StorageVolumePreferenceCategory buildForTotalMemory(Context context) {
        return new StorageVolumePreferenceCategory(context, null, 0, null);
    }

    /**
     * Build category to summarize USB OTG information
     */
    public static StorageVolumePreferenceCategory buildForOTG(Context context,
            StorageVolume volume, int mIndexOfUSBStorage, String thePathOfUSB) {
        return new StorageVolumePreferenceCategory(context, volume, mIndexOfUSBStorage,
                thePathOfUSB);
    }

    private StorageVolumePreferenceCategory(Context context, StorageVolume volume,
            int mIndexOfUSBStorage, String thePathOfUSB) {
        super(context);
        mContext = context;
        indexOfUSBStorage = mIndexOfUSBStorage;
        mStorageVolume = volume;
        mVolume = volume;
        mMeasure = StorageMeasurement.getInstance(context, volume);
        setVolumePath(thePathOfUSB);
        mResources = context.getResources();
        mStorageManager = StorageManager.from(context);
        mUserManager = (UserManager)context.getSystemService(Context.USER_SERVICE);

        setTitle(volume != null ? volume.getDescription(context)
                : context.getText(R.string.internal_storage));
    }

    private StorageItemPreference buildItem(int titleRes, int colorRes) {
        return new StorageItemPreference(getContext(), titleRes, colorRes);
    }

    public void init() {
        final Context context = getContext();

        removeAll();

        // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        if (Settings.System.getInt(mContext.getContentResolver(), "display_unit", 1) == 1) {
            mDisplayUnit = true;
        } else {
            mDisplayUnit = false;
        }
        // [END]

        final UserInfo currentUser;
        try {
            currentUser = ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get current user");
        }

        final List<UserInfo> otherUsers = getUsersExcluding(currentUser);
        final boolean showUsers = mVolume == null && otherUsers.size() > 0;

        mUsageBarPreference = new UsageBarPreference(context);
        mUsageBarPreference.setOrder(ORDER_USAGE_BAR);
        addPreference(mUsageBarPreference);

        mItemTotal = buildItem(R.string.memory_size, 0);
        mItemAvailable = buildItem(R.string.memory_available, R.color.memory_avail);
        // [VZW] This is for Total Memory Menu
        // Compared to others, VZW requires 1)H/W spec memory and 2) the location of Total menu on the top
        // It belongs to D1L, CAYMAN and 325. not i_proj.

        if (!Utils.isUI_4_1_model(mContext)) {
            if (Memory.isVZWOperator && (mVolume == null)) {
                initForTotalMemory();
            } else {
                addPreference(mItemTotal);
            }
        } else {
            if (mVolume == null) {
                initForTotalMemory();
            } else {
                addPreference(mItemTotal);
            }
        }

        addPreference(mItemAvailable);

        mItemApps = buildItem(R.string.memory_apps_usage, R.color.memory_apps_usage);
        mItemDcim = buildItem(R.string.memory_dcim_usage, R.color.memory_dcim);
        mItemMusic = buildItem(R.string.memory_music_usage, R.color.memory_music);
        mItemDownloads = buildItem(R.string.memory_downloads_usage, R.color.memory_downloads);
        // [seungyeop.yeom][2014-03-21] display capacity of TV app
        mItemTVapps = buildItem(R.string.tv_box_title, R.color.memory_tv_box);
        mItemCache = buildItem(R.string.memory_media_cache_usage, R.color.memory_cache);
        mItemMisc = buildItem(R.string.memory_media_misc_usage, R.color.memory_misc);

        mItemCache.setKey(KEY_CACHE);

        final boolean showDetails = mVolume == null || mVolume.isPrimary();
        if (showDetails) {
            if (showUsers) {
                addPreference(new PreferenceHeader(context, currentUser.name));
            }

            addPreference(mItemApps);
            addPreference(mItemDcim);
            addPreference(mItemMusic);
            addPreference(mItemDownloads);
            // [seungyeop.yeom][2014-03-21] display capacity of TV app
            if (Utils.checkPackage(mContext, "com.lge.oneseg")) {
                addPreference(mItemTVapps);
            }
            addPreference(mItemCache);
            addPreference(mItemMisc);

            if (showUsers) {
                addPreference(new PreferenceHeader(context, R.string.storage_other_users));

                int count = 0;
                for (UserInfo info : otherUsers) {
                    final int colorRes = count++ % 2 == 0 ? R.color.memory_user_light
                            : R.color.memory_user_dark;
                    final StorageItemPreference userPref = new StorageItemPreference(
                            getContext(), info.name, colorRes, info.id);
                    mItemUsers.add(userPref);
                    addPreference(userPref);
                }
            }
        }

        final boolean isRemovable = mVolume != null ? mVolume.isRemovable() : false;
        // Always create the preference since many code rely on it existing
        mMountTogglePreference = new Preference(context);

        // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
        mSaveSDcardCamera = new CheckBoxPreference(mContext);
        if (isRemovable) {
            mMountTogglePreference.setTitle(R.string.sd_eject);
            mMountTogglePreference.setSummary(R.string.sp_sd_eject_summary_NORMAL);
            addPreference(mMountTogglePreference);
        }

        // Only allow formatting of primary physical storage
        // TODO: enable for non-primary volumes once MTP is fixed
        //final boolean allowFormat = mVolume != null ? mVolume.isPrimary() : false;
        final boolean allowFormat = mVolume != null && !mVolume.isEmulated();
        if (allowFormat) {
            mFormatPreference = new Preference(context);
            mFormatPreference.setTitle(R.string.media_format_button_text);
            if (MORE_THAN_UI_4_2) {
                mFormatPreference.setSummary(R.string.sp_sd_format_summary_NORMAL_ex3);    
            } else {
                mFormatPreference.setSummary(R.string.sp_sd_format_summary_NORMAL);
            }
            addPreference(mFormatPreference);

            // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
            if (Utils.checkPackage(mContext, "com.lge.camera")) {
                if (Utils.isSoftwareVersionCheck(mContext, "com.lge.camera", 420000001)) {
                    mSaveSDcardCamera.setTitle(mResources
                            .getString(R.string.storage_save_sd_card_camera_title));
                    // addPreference(mSaveSDcardCamera);
                }
            }

            boolean isSaveSDcardCamera = (Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.SAVE_CAMERA_IN_SD_CARD, 1) != 0);

            mSaveSDcardCamera.setChecked(isSaveSDcardCamera);

            Log.d("YSY", "mSaveSDcardCamera check DB stats : " + isSaveSDcardCamera);
        }
        // [USB OTG] Tokens
        unmount_usb_storage_title = mResources.getString(R.string.usbstorage_unmount_title);
        unmount_usb_storage_summary = mResources.getString(R.string.usbstorage_unmount_summary);
        format_usb_storage_title = mResources.getString(R.string.usbstorage_format_title);
        format_usb_storage_summary = mResources.getString(R.string.usbstorage_format_summary);

        if (mVolume != null && mVolume.getPath().startsWith(Memory.PATH_OF_USBSTORAGE)) {
            if (indexOfUSBStorage == 1) {
                mMountTogglePreference.setTitle(String.format(unmount_usb_storage_title, ""));
                mMountTogglePreference.setSummary(String.format(unmount_usb_storage_summary, ""));
                mFormatPreference.setTitle(String.format(format_usb_storage_title, ""));
                mFormatPreference.setSummary(String.format(format_usb_storage_summary, ""));

                // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                removePreference(mSaveSDcardCamera);
            } else {
                mMountTogglePreference.setTitle(String.format(unmount_usb_storage_title,
                        indexOfUSBStorage + ""));
                mMountTogglePreference.setSummary(String.format(unmount_usb_storage_summary,
                        indexOfUSBStorage + ""));
                mFormatPreference.setTitle(String.format(format_usb_storage_title,
                        indexOfUSBStorage + ""));
                mFormatPreference.setSummary(String.format(format_usb_storage_summary,
                        indexOfUSBStorage + ""));

                // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                removePreference(mSaveSDcardCamera);
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-286]
            // USB Host Storage
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                com.android.settings.MDMSettingsAdapter.getInstance().setUSBHostStorageEnableMenu(
                        mMountTogglePreference);
                if (mUsageBarPreference != null) {
                    removePreference(mUsageBarPreference);
                }
                if (mItemTotal != null) {
                    removePreference(mItemTotal);
                }
                if (mItemAvailable != null) {
                    removePreference(mItemAvailable);
                }
            }
            // LGMDM_END
        } else {
        }

        //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
        // Only allow 3LM SD encryption for non-emulated devices that are removable,
        // otherwise OEMs should use stock Android encryption.
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM
                && mVolume != null && !mVolume.isEmulated() &&
                mVolume.isRemovable()) {
            mEncryptPreference = new Preference(context);
            mEncryptPreference.setTitle(R.string.sd_encrypt_title);
            addPreference(mEncryptPreference);
            // addPreference(mEncryptPreference); // this is in SecureOS4.4
        }
        //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
        if (mStorageVolume == null) {
            final IPackageManager pm = ActivityThread.getPackageManager();
            try {
                if (pm.isStorageLow()) {
                    mStorageLow = new Preference(context);
                    mStorageLow.setOrder(ORDER_STORAGE_LOW);
                    mStorageLow.setTitle(R.string.storage_low_title_ex);
                    mStorageLow.setSummary(R.string.storage_low_summary_ex);
                    addPreference(mStorageLow);
                } else if (mStorageLow != null) {
                    removePreference(mStorageLow);
                    mStorageLow = null;
                }
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "RemoteException : " + e);
            }
        }
    }

    // [BUA+] Initiate BUA+ information
    protected void initForBUA() {
        isExistedBUACategory = true;

        mUsageBarPreference = new UsageBarPreference(getContext());
        mUsageBarPreference.setOrder(ORDER_USAGE_BAR);
        addPreference(mUsageBarPreference);

        setTitle(R.string.sp_bua_plus_storage_NORMAL);

        // To make storage item
        mItemTotal = buildItem(R.string.memory_size, 0);
        mItemAvailable = buildItem(R.string.memory_available, R.color.vzw_storage_avail);
        addPreference(mItemTotal);
        addPreference(mItemAvailable);

        mItemDcim = buildItem(R.string.memory_dcim_usage, R.color.vzw_storage_pictures);
        mItemMusic = buildItem(R.string.memory_music_usage, R.color.vzw_storage_audio);
        mItemMisc = buildItem(R.string.memory_media_misc_usage, R.color.vzw_storage_misc);
        mItemUserOption = buildItem(R.string.sp_subscribe_now_NORMAL, 0);

        addPreference(mItemDcim);
        addPreference(mItemMusic);
        addPreference(mItemMisc);
        addPreference(mItemUserOption);

    }

    // [VZW] This is an additional preference which displays 'Total Menu' with H/W spec
    public void initForTotalMemory() {
        //isExistedHWTotalMenu = true;
        // setTitle(mResources.getString(R.string.sp_total_memory_category_NORMAL));
        mTotalSpaceSummary = mResources.getString(R.string.sp_total_category_storage_NORMAL);

        addTotalMenu();
        //sp_total_category_storage_NORMAL
    }

    public void addTotalMenu() {

        mItemHWTotal = buildItem(R.string.memory_size, 0);
        mItemHWTotal.setOrder(ORDER_TOTAL_MEMORY_VZW);
        addPreference(mItemHWTotal);
        //mPreferencesTotal.setTitle(mResources.getString(R.string.memory_size));

        String systemMemoryToken = SystemProperties.get("ro.device.memory.system");
        String internalMemoryToken = SystemProperties.get("ro.device.memory.internal");
        float systemMemory = 0;
        float internalMemory = 0;

        boolean flagSystemMemory = false;
        boolean flagInternalMemoryFormat = false;

        if (systemMemoryToken != null && !systemMemoryToken.equals("")) {
            systemMemory = Float.parseFloat(systemMemoryToken);
            if ((int)systemMemory != 0/* && ((int)systemMemory == systemMemory)*/) {
                flagSystemMemory = true;
            }
        }
        if (internalMemoryToken != null && !internalMemoryToken.equals("")) {
            internalMemory = Float.parseFloat(internalMemoryToken);
            if ((int)internalMemory != internalMemory) {
                flagInternalMemoryFormat = true;
            }
        }

        if (flagSystemMemory) {
            // ( System memory + Internal Memory + SD card ) like Cayman
            Log.d("yeom", "flagSystemMemory");
            mItemHWTotal.setSummary(String.format(mTotalSpaceSummary, ""
                    + ((int)systemMemory + (int)internalMemory),
                    Integer.toString((int)systemMemory), Integer.toString((int)internalMemory)));
        } else if (!flagSystemMemory && flagInternalMemoryFormat) {
            // ( Internal Storage + SD card but 'Float' ) like Batman
            Log.d("yeom", "!flagSystemMemory && flagInternalMemoryFormat");
            mItemHWTotal.setSummary(String.format(mTotalSpaceSummary, ""
                    + (int)(systemMemory + internalMemory), Float.toString(systemMemory),
                    Float.toString(internalMemory)));
        } else {
            Log.d("yeom", "flagSystemMemory, else");
            // ( Internal Storage + SD card ) like D1L
            String hwTotalSize;
            if (mDisplayUnit) {
                hwTotalSize = String.format("%d", (int)(systemMemory + internalMemory))
                        + mResources.getString(com.android.internal.R.string.gigabyteShort);
                Log.d("YSY", "formatSize : " + hwTotalSize);

            } else {
                hwTotalSize = String.format("%d", ((int)(systemMemory + internalMemory) * 1024))
                        + mResources.getString(com.android.internal.R.string.megabyteShort);
                Log.d("YSY", "formatSize : " + hwTotalSize);
            }

            // [START][seungyeop.yeom][2014-05-02] modify issue of volume for HEBREW lang.
            mItemHWTotal.setSummary(hwTotalSize);
            if ("iw".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
                Log.d("YSY", "formatSize (iw), 202A : " + hwTotalSize);
                mItemHWTotal.setSummary("\u202A" + hwTotalSize + "\u202C");
            }
            // [END]
        }
        mVZWTotalSpace = (long)(systemMemory + internalMemory);
        //mVZWTotalSpace = (long)8;
    }

    public StorageVolume getStorageVolume() {
        return mVolume;
    }

    private void resetPreferences() {

        //int numberOfCategories = 0;
        /*if( Memory.isVZWOperator && ( mStorageVolume == null || !mStorageVolume.isRemovable() ) ) {
            numberOfCategories = sCategoryInfos_WithOutTotal_VZW.length;
            removePreference(mPreferencesTotal);
        } else {
            numberOfCategories = sCategoryInfos.length;
        } */
        removePreference(mUsageBarPreference);
        /*for (int i = 0; i < numberOfCategories; i++) {
            removePreference(mPreferences[i]);
        }*/
        removePreference(mMountTogglePreference);
        if (mFormatPreference != null) {
            removePreference(mFormatPreference);
        }

        // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
        if (mSaveSDcardCamera != null) {
            removePreference(mSaveSDcardCamera);
        }

        /*if ( mPreferences.length < 2 ) {
        } else if ( mPreferences.length == sCategoryInfos.length ) {
        } else if ( Memory.isVZWOperator && mPreferences.length == sCategoryInfos_WithOutTotal_VZW.length && !flagForEntireTotalMemory) {
            addTotalMenu();
        }*/
        if (mUsageBarPreference != null) {
            addPreference(mUsageBarPreference);
        }
        if (mItemTotal != null) {
            addPreference(mItemTotal);
        }
        if (mItemAvailable != null) {
            addPreference(mItemAvailable);
        }
        if (mStorageLow != null) {
            addPreference(mStorageLow);
        }
        /*for (int i = 0; i < numberOfCategories; i++) {
            addPreference(mPreferences[i]);
        }*/
        if (mMountTogglePreference != null) {
            addPreference(mMountTogglePreference);
            mMountTogglePreference.setEnabled(true);
        }
        if (mFormatPreference != null) {
            addPreference(mFormatPreference);
        }
        // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
        if (mSaveSDcardCamera != null) {

            // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
            if (Utils.checkPackage(mContext, "com.lge.camera")) {
                if (Utils.isSoftwareVersionCheck(mContext, "com.lge.camera", 420000001)) {
                    mSaveSDcardCamera.setTitle(mResources
                            .getString(R.string.storage_save_sd_card_camera_title));
                    // addPreference(mSaveSDcardCamera);
                }
            }
            boolean isSaveSDcardCamera = (Settings.System.getInt(mContext.getContentResolver(),
                    SettingsConstants.System.SAVE_CAMERA_IN_SD_CARD, 1) != 0);

            mSaveSDcardCamera.setChecked(isSaveSDcardCamera);
        }
    }

    private void updatePreferencesFromState() {
        // Only update for physical volumes
        if (mVolume == null) {
            return;
        }
        resetPreferences();
        mMountTogglePreference.setEnabled(true);
        boolean isUSBStorage = false;
        //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
        // 3LM SD card encryption preferences
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM
                && mEncryptPreference != null) {
            int sd_encryption = Settings.Global.getInt(getContext().getContentResolver(),
                    "sd_encryption", 0);
            mEncryptPreference.setEnabled(sd_encryption == 0);
        }
        //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]

        final String state = mStorageManager.getVolumeState(mVolume.getPath());

        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mItemAvailable.setSummary(R.string.memory_available_read_only);
            if (mFormatPreference != null) {
                removePreference(mFormatPreference);
            }
        } else {
            //mItemAvailable.setSummary(R.string.memory_available);
        }

        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mMountTogglePreference.setEnabled(true);
            mMountTogglePreference.setTitle(mResources.getString(R.string.sd_eject));
            mMountTogglePreference.setSummary(mResources
                    .getString(R.string.sp_sd_eject_summary_NORMAL));
            addPreference(mUsageBarPreference);
            addPreference(mItemTotal);
            addPreference(mItemAvailable);
            if (mFormatPreference != null) {
                mFormatPreference.setEnabled(true);
            }

            // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
            if (mSaveSDcardCamera != null) {
                mSaveSDcardCamera.setEnabled(true);
            }

            // [USB OTG] Tokens
            if (mVolume != null && mVolume.getPath().startsWith(Memory.PATH_OF_USBSTORAGE)) {
                isUSBStorage = true;
                if (indexOfUSBStorage == 1) {
                    mMountTogglePreference.setTitle(String.format(unmount_usb_storage_title, ""));
                    mMountTogglePreference.setSummary(String
                            .format(unmount_usb_storage_summary, ""));
                    if (mFormatPreference != null) {
                        mFormatPreference.setTitle(String.format(format_usb_storage_title, ""));
                        mFormatPreference.setSummary(String.format(format_usb_storage_summary, ""));
                    }

                    // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                    removePreference(mSaveSDcardCamera);
                } else {
                    mMountTogglePreference.setTitle(String.format(unmount_usb_storage_title,
                            indexOfUSBStorage + ""));
                    mMountTogglePreference.setSummary(String.format(unmount_usb_storage_summary,
                            indexOfUSBStorage + ""));
                    if (mFormatPreference != null) {
                        mFormatPreference.setTitle(String.format(format_usb_storage_title,
                                indexOfUSBStorage + ""));
                        mFormatPreference.setSummary(String.format(format_usb_storage_summary,
                                indexOfUSBStorage + ""));
                    }

                    // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                    removePreference(mSaveSDcardCamera);
                }
            } else {
            }
        } else {
            if (Environment.MEDIA_UNMOUNTED.equals(state) || Environment.MEDIA_NOFS.equals(state)
                    || Environment.MEDIA_UNMOUNTABLE.equals(state)) {
                // 3LM_MDM L
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                    String enabled = SystemProperties.get("persist.security.3lm.storage", "1");
                    mMountTogglePreference.setEnabled(enabled.equals("1") ? true : false);
                } else {
                    mMountTogglePreference.setEnabled(true);
                }
                mMountTogglePreference.setTitle(mResources.getString(R.string.sd_mount));
                mMountTogglePreference.setSummary(mResources
                        .getString(R.string.sp_sd_mount_summary_NORMAL));
                if (mFormatPreference != null) {
                    mFormatPreference.setEnabled(true);
                }

                // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                if (mSaveSDcardCamera != null) {
                    mSaveSDcardCamera.setEnabled(true);
                }

                // [USB OTG] Tokens
                mount_usb_storage_title = mResources.getString(R.string.usbstorage_mount_title);
                mount_usb_storage_summary = mResources.getString(R.string.usbstorage_mount_summary);
                if (mVolume != null && mVolume.getPath().startsWith(Memory.PATH_OF_USBSTORAGE)) {
                    isUSBStorage = true;
                    if (indexOfUSBStorage == 1) {
                        mMountTogglePreference.setTitle(String.format(mount_usb_storage_title, ""));
                        mMountTogglePreference.setSummary(String.format(mount_usb_storage_summary,
                                ""));
                        if (mFormatPreference != null) {
                            mFormatPreference.setTitle(String.format(format_usb_storage_title, ""));
                            mFormatPreference.setSummary(String.format(format_usb_storage_summary,
                                    ""));
                        }

                        // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                        removePreference(mSaveSDcardCamera);
                    } else {
                        mMountTogglePreference.setTitle(String.format(mount_usb_storage_title,
                                indexOfUSBStorage + ""));
                        mMountTogglePreference.setSummary(String.format(mount_usb_storage_summary,
                                indexOfUSBStorage + ""));
                        if (mFormatPreference != null) {
                            mFormatPreference.setTitle(String.format(format_usb_storage_title,
                                    indexOfUSBStorage + ""));
                            mFormatPreference.setSummary(String.format(format_usb_storage_summary,
                                    indexOfUSBStorage + ""));
                        }

                        // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                        removePreference(mSaveSDcardCamera);
                    }
                } else {
                }
            } else {
                if (mFormatPreference != null) {
                    mFormatPreference.setEnabled(false);
                }

                // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
                if (mSaveSDcardCamera != null) {
                    mSaveSDcardCamera.setEnabled(false);
                }

                mMountTogglePreference.setEnabled(false);
                mMountTogglePreference.setTitle(mResources.getString(R.string.sd_mount));
                mMountTogglePreference.setSummary(mResources
                        .getString(R.string.sp_sd_insert_summary_NORMAL));
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-45]
            // External Storage
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mMountTogglePreference != null
                    && isUSBStorage == false) {
                com.android.settings.MDMSettingsAdapter.getInstance().setExternalMemoryEnableMenu(
                        null, getContext(), "mMountSDcard", mMountTogglePreference);
            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-286]
            // USB Host Storage
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM && isUSBStorage) {
                com.android.settings.MDMSettingsAdapter.getInstance().setUSBHostStorageEnableMenu(
                        mMountTogglePreference);
            }
            // LGMDM_END

            removePreference(mUsageBarPreference);
            removePreference(mItemTotal);
            removePreference(mItemAvailable);
            /*if (mFormatPreference != null) {
                removePreference(mFormatPreference);
            }*/
        }

        if (mMountTogglePreference != null && mUsbConnected
                && (UsbManager.USB_FUNCTION_MTP.equals(mUsbFunction) ||
                UsbManager.USB_FUNCTION_PTP.equals(mUsbFunction))) {

            mMountTogglePreference.setEnabled(false);

            if (Environment.MEDIA_MOUNTED.equals(state)
                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                mMountTogglePreference.setSummary(
                        mResources.getString(R.string.mtp_ptp_mode_summary));
            }

            if (mFormatPreference != null) {
                mFormatPreference.setEnabled(false);
                mFormatPreference.setSummary(mResources.getString(R.string.mtp_ptp_mode_summary));
            }
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-45][ID-MDM-169]
        // Factory reset, sd card
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM && mFormatPreference != null
                && isUSBStorage == false) {
            com.android.settings.MDMSettingsAdapter.getInstance().setEraseSdMenu(null,
                    mFormatPreference);
        }
        // LGMDM_END

        // 3LM_MDM L
        // gray-out 3LM "Encrypt SD card" in case SD card unplugged.
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM && mEncryptPreference != null) {
            int sd_encryption = Settings.Global.getInt(getContext().getContentResolver(),
                    "sd_encryption", 0);
            mEncryptPreference.setEnabled(mMountTogglePreference.isEnabled() && sd_encryption == 0);
        }
        // 3LM_MDM L END
    }

    public void updateApproximate(long totalSize, long availSize, long reservedSize) {
        mItemTotal.setSummary(formatSize(totalSize));
        mItemAvailable.setSummary(formatSize(availSize));

        mTotalSize = totalSize - reservedSize;

        final long usedSize = mTotalSize - availSize;

        mUsageBarPreference.clear();
        mUsageBarPreference.addEntry(0, usedSize / (float)mTotalSize, Utils.getResources()
                .getColor(R.color.memory_gray));
        mUsageBarPreference.commit();

        updatePreferencesFromState();
    }

    private static long totalValues(HashMap<String, Long> map, String... keys) {
        long total = 0;
        for (String key : keys) {
            if (map.containsKey(key)) {
                total += map.get(key);
            }
        }
        return total;
    }

    public void updateDetails(MeasurementDetails details) {
        Log.d("yeom", "updateDetails");
        final boolean showDetails = mVolume == null || mVolume.isPrimary();
        if (!showDetails) {
            return;
        }
        // Count caches as available space, since system manages them
        mItemTotal.setSummary(formatSize(details.totalSize));
        mItemAvailable.setSummary(formatSize(details.availSize));

        mUsageBarPreference.clear();

        updatePreference(mItemApps, details.appsSize);

        final long dcimSize = totalValues(details.mediaSize, Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES);
        updatePreference(mItemDcim, dcimSize);

        final long musicSize = totalValues(details.mediaSize, Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_PODCASTS);
        updatePreference(mItemMusic, musicSize);

        final long downloadsSize = totalValues(details.mediaSize, Environment.DIRECTORY_DOWNLOADS);
        updatePreference(mItemDownloads, downloadsSize);

        // [seungyeop.yeom][2014-03-21] display capacity of TV app
        updatePreference(mItemTVapps, details.tvAppsSize);
        updatePreference(mItemCache, details.cacheSize);
        updatePreference(mItemMisc, details.miscSize);

        for (StorageItemPreference userPref : mItemUsers) {
            final long userSize = details.usersSize.get(userPref.userHandle);
            updatePreference(userPref, userSize);
        }

        mUsageBarPreference.commit();
    }

    public void updateDetails_vzw(MeasurementDetails details) {
        Log.d("yeom", "updateDetails_vzw");
        final boolean showDetails = mVolume == null || mVolume.isPrimary();
        if (!showDetails) {
            return;
        }

        long totalSize = mVZWTotalSpace * 1024 * 1024 * 1024;
        mTotalSize = totalSize;

        // Count caches as available space, since system manages them
        mItemTotal.setSummary(formatSize(totalSize));
        mItemAvailable.setSummary(formatSize(details.availSize));

        mUsageBarPreference.clear();
        updatePreference(mItemApps, details.appsSize);

        final long dcimSize = totalValues(details.mediaSize, Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES);
        updatePreference(mItemDcim, dcimSize);

        final long musicSize = totalValues(details.mediaSize, Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_PODCASTS);
        updatePreference(mItemMusic, musicSize);

        final long downloadsSize = totalValues(details.mediaSize, Environment.DIRECTORY_DOWNLOADS);
        updatePreference(mItemDownloads, downloadsSize);

        // [seungyeop.yeom][2014-09-15] sum capacity of TV app for misc capacity
        long newMisc = totalSize
                - (details.availSize + details.appsSize + dcimSize + musicSize + downloadsSize
                        + details.cacheSize + details.tvAppsSize + details.miscSize)
                + details.miscSize;
        miscForSystemData = newMisc - details.miscSize;

        // [seungyeop.yeom][2014-03-21] display capacity of TV app
        updatePreference(mItemTVapps, details.tvAppsSize);
        updatePreference(mItemCache, details.cacheSize);
        updatePreference(mItemMisc, newMisc);

        for (StorageItemPreference userPref : mItemUsers) {
            final long userSize = details.usersSize.get(userPref.userHandle);
            updatePreference(userPref, userSize);
        }
        mUsageBarPreference.commit();
    }

    private void updatePreference(StorageItemPreference pref, long size) {
        if (size > 0) {
            pref.setSummary(formatSize(size));
            final int order = pref.getOrder();
            mUsageBarPreference.addEntry(order, size / (float)mTotalSize, pref.color);
        } else {
            removePreference(pref);
        }
    }

    private void measure() {
        mMeasure.invalidate();
        mMeasure.measure();
    }

    public void onResume() {
        mMeasure.setReceiver(mReceiver);
        measure();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-45]
        // External Storage
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addExternalStoragePolicyChangeIntentFilter(filterLGMDM);
            getContext().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }

    public void onStorageStateChanged() {
        if (isNeededToCheckUSBRefresh) {
            init();
            measure();
        }
    }

    public void onUsbStateChanged(boolean isUsbConnected, String usbFunction) {
        mUsbConnected = isUsbConnected;
        mUsbFunction = usbFunction;
        measure();
    }

    public void onMediaScannerFinished() {
        measure();
    }

    public void onCacheCleared() {
        measure();
    }

    public void onPause() {
        mMeasure.cleanUp();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-45]
        // External Storage
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getContext().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-45]
    // External Storage
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveExternalStoragePolicyChangeIntent(intent)) {
                    updatePreferencesFromState();
                }
            }
        }
    };

    // LGMDM_END

    private String formatSize(long size) {
        // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        if (Memory.isVZWOperator) {
            return Utils.formatFileSize_vzw(getContext(), size, mDisplayUnit);
            //Lavanya added for GB reversal display 19785
        } else if ("iw".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            String formattedFileSize = Formatter.formatFileSize(getContext(), size);
            return "\u202A" + formattedFileSize + "\u202C";
        } else {
            return Formatter.formatFileSize(getContext(), size);
        }
        //Lavanya added for GB reversal display 19785
        // [END]
    }

    private MeasurementReceiver mReceiver = new MeasurementReceiver() {
        @Override
        public void updateApproximate(StorageMeasurement meas, long totalSize, long availSize,
                long reservedSize) {
            mUpdateHandler.obtainMessage(MSG_UI_UPDATE_APPROXIMATE, new long[] {
                    totalSize, availSize, reservedSize }).sendToTarget();
        }

        @Override
        public void updateDetails(StorageMeasurement meas, MeasurementDetails details) {
            mUpdateHandler.obtainMessage(MSG_UI_UPDATE_DETAILS, details).sendToTarget();
        }
    };

    public boolean mountToggleClicked(Preference preference) {
        // 3LM_MDM L
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            boolean ret = false;
            if (preference == mMountTogglePreference) {
                String enabled = SystemProperties.get("persist.security.3lm.storage", "1");
                if (enabled.equals("1")) {
                    ret = true;
                } else {
                    updatePreferencesFromState();
                }
            }
            return ret;
        } else {
            return preference == mMountTogglePreference;
        }
    }

    public Preference getmFormatPreference() {
        return mFormatPreference;
    }
    
    // [2014-10-28][seungyeop.yeom] add method for object apps preference
    public Preference getmItemAppsPreference() {
        return mItemApps;
    }

    public StorageVolume getVOLUME() {
        return mVolume;
    }

    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
    private void showNonOwnerDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.crypt_keeper_dialog_need_owner_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.crypt_keeper_dialog_need_owner_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    private void showPinDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.crypt_keeper_dialog_need_password_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.crypt_keeper_dialog_need_password_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
    public Intent intentForClick(Preference pref) {
        Intent intent = null;

        // TODO The current "delete" story is not fully handled by the respective applications.
        // When it is done, make sure the intent types below are correct.
        // If that cannot be done, remove these intents.

        if (pref == mFormatPreference) {
            //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
            int quality = new LockPatternUtils(getContext()).getActivePasswordQuality();
            int sd_encryption =
                    Settings.Global.getInt(getContext().getContentResolver(),
                            "sd_encryption", 0);
            //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
            if (!Memory.isUnmountable()) {
                if (MORE_THAN_UI_4_2) {
                    Toast.makeText(mContext,
                            R.string.impossible_erase_sd_card_ex,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, R.string.impossible_erase_sd_card,
                            Toast.LENGTH_SHORT).show();
                }
                //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
            } else if (com.android.settings.lgesetting.Config.Config.THREELM_MDM &&
                    sd_encryption == 1 &&
                    UserHandle.myUserId() != UserHandle.USER_OWNER) {
                showNonOwnerDialog();
            } else if (com.android.settings.lgesetting.Config.Config.THREELM_MDM &&
                    sd_encryption == 1 &&
                    quality < DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
                showPinDialog();
                //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(getContext(), com.android.settings.MediaFormat.class);
                intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
            }
        } else if (pref == mItemApps) {
            intent = new Intent(Intent.ACTION_MANAGE_PACKAGE_STORAGE);
            intent.setClass(getContext(),
                    com.android.settings.Settings.ManageApplicationsActivity.class);
        } else if (pref == mItemDownloads) {
            intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).putExtra(
                    DownloadManager.INTENT_EXTRAS_SORT_BY_SIZE, true);
        } else if (pref == mItemMusic) {
            if (Utils.checkPackage(getContext(), "com.lge.music")) {
                intent = new Intent();
                intent.setComponent(new ComponentName("com.lge.music",
                        "com.lge.music.MusicBrowserActivity"));
                intent.putExtra("component", "com.lge.music");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                //intent.setComponent(new ComponentName("com.google.android.music",
                //  "com.android.music.activitymanagement.TopLevelActivity"));
                //intent.putExtra("component", "com.google.android.music");
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/mp3");
            }
            /*intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/mp3");*/
        } else if (pref == mItemDcim) {
            if (Utils.checkPackage(getContext(), "com.android.gallery3d")) {
                intent = new Intent();
                intent.setComponent(new ComponentName("com.android.gallery3d",
                        "com.android.gallery3d.app.Gallery"));
                intent.setAction("android.intent.action.MAIN");
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setComponent(new ComponentName("com.google.android.apps.plus",
                        "com.google.android.apps.plus.phone.ConversationListActivity"));
                intent.setAction("android.intent.action.MAIN");
            }
            //intent = new Intent(Intent.ACTION_VIEW);
            //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            // TODO Create a Videos category, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            //intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else if (pref == mItemMisc) {
            Context context = getContext().getApplicationContext();
            intent = new Intent(context, MiscFilesHandler.class);
            intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
        } else if (pref == mItemTVapps) {
            // [START][seungyeop.yeom][2014-03-21] display capacity of TV app
            intent = new Intent();
            intent.setComponent(new ComponentName("com.lge.oneseg",
                    "com.lge.oneseg.list.TdmbListActivity"));
            intent.setAction("android.intent.action.MAIN");
            // [END][seungyeop.yeom][2014-03-21] display capacity of TV app
        //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
        } else if (com.android.settings.lgesetting.Config.Config.THREELM_MDM
                && pref == mEncryptPreference) {
            // Check if password quality meets our requirements
            int quality = new LockPatternUtils(getContext()).getActivePasswordQuality();
            String enabled = SystemProperties.get("persist.security.3lm.storage", "1");
            if (enabled.equals("0")) {
                //  Check if external storage is allowed.  If not, disable preference
                updatePreferencesFromState();
            } else if (quality < DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
                // Check if password quality meets our requirements
                showPinDialog();
            } else if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                showNonOwnerDialog();
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(getContext(), com.android.settings.MediaFormat.class);
                intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
                intent.putExtra("sd_encryption", 1);
            }
            //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
        } else if (pref == mSaveSDcardCamera) {
            // [seungyeop.yeom][2014-02-05] Save to sd card in Camera
            if (mSaveSDcardCamera.isChecked()) {
                Log.d("YSY", "mSaveSDcardCamera check");
                Settings.System.putInt(mContext.getContentResolver(),
                        SettingsConstants.System.SAVE_CAMERA_IN_SD_CARD, 1);
            } else {
                Log.d("YSY", "mSaveSDcardCamera uncheck");
                Settings.System.putInt(mContext.getContentResolver(),
                        SettingsConstants.System.SAVE_CAMERA_IN_SD_CARD, 0);
            }
        }

        return intent;
    }

    public static class PreferenceHeader extends Preference {
        public PreferenceHeader(Context context, int titleRes) {
            super(context, null, com.android.internal.R.attr.preferenceCategoryStyle);
            setTitle(titleRes);
        }

        public PreferenceHeader(Context context, CharSequence title) {
            super(context, null, com.android.internal.R.attr.preferenceCategoryStyle);
            setTitle(title);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    /**
     * Return list of other users, excluding the current user.
     */
    private List<UserInfo> getUsersExcluding(UserInfo excluding) {
        final List<UserInfo> users = mUserManager.getUsers();
        final Iterator<UserInfo> i = users.iterator();
        while (i.hasNext()) {
            if (i.next().id == excluding.id) {
                i.remove();
            }
        }
        return users;
    }

    // [USB OTG] To set/get volume path
    protected void setVolumePath(String thePathOfUSB) {
        this.thisVolumePath = thePathOfUSB;
    }

    protected String getVolumePath() {
        return this.thisVolumePath;
    }
}
