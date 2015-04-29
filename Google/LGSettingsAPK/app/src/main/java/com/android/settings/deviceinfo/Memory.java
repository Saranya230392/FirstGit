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

package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.hardware.usb.UsbManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import java.io.File;
import android.os.FileUtils;
import android.os.SystemProperties;
import java.io.IOException;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.lge.constants.SettingsConstants;
//2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
//2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution
import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.view.RotationPolicy;
import com.android.settings.MediaFormatFragment;
import com.android.settings.applications.ManageApplications;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.google.android.collect.Lists;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.List;

import com.lge.constants.UsbManagerConstants;

/**
 * Panel showing storage usage on disk for known {@link StorageVolume} returned
 * by {@link StorageManager}. Calculates and displays usage of data types.
 */
public class Memory extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "MemorySettings";

    // [START][2015-02-16][seungyeop.yeom] Create value for Search function
    private static final String KEY_TOTAL_SPACE = "key_total_space";
    private static final String KEY_AVAILABLE = "key_available";
    private static final String KEY_APPS = "key_apps";
    private static final String KEY_PICTURES_VIDEOS = "key_pictures_videos";
    private static final String KEY_AUDIO = "key_audio";
    private static final String KEY_DOWNLOADS = "key_downloads";
    private static final String KEY_CACHED_DATA = "key_cached_data";
    private static final String KEY_MISC = "key_misc";
    private static final String KEY_TV_APPS = "key_tv_apps";

    private boolean mIsFirst = false;
    private String mSearch_result;
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    // [END][2015-02-16][seungyeop.yeom] Create value for Search function

    private static final String TAG_CONFIRM_CLEAR_CACHE = "confirmClearCache";

    private static final int DLG_CONFIRM_UNMOUNT = 1;
    private static final int DLG_ERROR_UNMOUNT = 2;

    private Resources mResources;
    // The mountToggle Preference that has last been clicked.
    // Assumes no two successive unmount event on 2 different volumes are performed before the first
    // one's preference is disabled
    private static Preference sLastClickedMountToggle;
    private static String sClickedMountPoint;

    // Access using getMountService()
    private IMountService mMountService;
    private StorageManager mStorageManager;
    private UsbManager mUsbManager;

    private ArrayList<StorageVolumePreferenceCategory> mCategories = Lists.newArrayList();
    protected static boolean isVZWOperator = false;
    protected static boolean isCloudBUA = false;

    // [USB OTG] Variables PATH_OF_USBSTORAGE
    public static final String PATH_OF_USBSTORAGE = "/storage/USBstorage";
    private int theCountOfUSBStorageVolume = 1;
    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution
    // Used for 3LM SD encryption
    private Intent mIntent;

    // [2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
    private boolean mDisplayUnit = true;
    private static final boolean MORE_THAN_UI_4_2 = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Context context = getActivity();

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        mStorageManager = StorageManager.from(context);
        mStorageManager.registerListener(mStorageListener);

        // [2015-02-16][seungyeop.yeom] support perform click for Search
        mIsFirst = true;

        // check whether VZW or not
        isVZWOperator();

        addPreferencesFromResource(R.xml.device_info_memory);

        // [VZW][BUA+] This is for BUA+ category
        if (isVZWOperator && cloudEnable()) {
            isCloudBUA = true;
            addCategoryForBUA(StorageVolumePreferenceCategory.buildForBUA(context));
        }

        mResources = getResources();
        // Essential storage such as 'Internal storage'
        addCategory(StorageVolumePreferenceCategory.buildForInternal(context));

        final StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        for (StorageVolume volume : storageVolumes) {
            if (!volume.isEmulated()) {
                /*
                 *
                 * 1. if( USB OTG )
                 * 1.1. To check the status of the USB storage
                 * 1.2. if( Connected : Mount, Unmount )
                 * 1.2.1. To add the preference
                 * 1.3. else if ( Not Connected )
                 * 1.3.1. Just skip
                 * 2. else if ( Normal storage )
                 * 2.1. To add the preference
                 *
                 */
                if (volume.getPath().startsWith(PATH_OF_USBSTORAGE)) {
                    StorageVolumePreferenceCategory category = StorageVolumePreferenceCategory
                            .buildForOTG(context, volume, theCountOfUSBStorageVolume,
                                    volume.getPath());
                    theCountOfUSBStorageVolume++;
                    boolean isConnected = isConnectedToUSBStorage(volume.getPath());
                    if (isConnected) {
                        Log.d("YSY", "connected USB : " + PATH_OF_USBSTORAGE);
                        //volume.setVolumePath(volume.getPath());
                        //StorageVolumePreferenceCategory category = StorageVolumePreferenceCategory.buildForOTG(context, volume, theCountOfUSBStorageVolume, volume.getPath());
                        category.setVolumePath(volume.getPath());
                        // category.isNeededToCheckUSBRefresh = true;
                        addCategory(category);
                        //theCountOfUSBStorageVolume++;
                        //getPreferenceScreen().addPreference(mStorageVolumePreferenceCategories[i]);
                    } else {
                        Log.d("YSY", "unconnected USB : " + PATH_OF_USBSTORAGE);
                        mCategories.add(category);
                        category.isNeededToCheckUSBRefresh = false;
                        category.setVolumePath(null);
                    }
                } else {
                    // It can be 'SD card' or 'System memory'
                    Log.d("YSY", "sd card : " + PATH_OF_USBSTORAGE);
                    // [2014-11-05][seungyeop.yeom] add condition of category for SD card (SD card not support)
                    if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
                        addCategory(StorageVolumePreferenceCategory.buildForPhysical(context,
                                volume));
                    }
                }
            }
        }

        // only show options menu if we are not using the legacy USB mass storage support
        // setHasOptionsMenu(!massStorageEnabled);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        if (Settings.System.getInt(getContentResolver(), "display_unit", 1) == 1) {
            mDisplayUnit = true;
        } else {
            mDisplayUnit = false;
        }

        if (isVZWOperator
                || (Utils.isUI_4_1_model(getActivity())
                && ("SPR".equals(Config.getOperator())
                || Utils.supportSplitView(getActivity())))) {
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);
        }
        // [END][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
    }

    private void addCategory(StorageVolumePreferenceCategory category) {
        mCategories.add(category);
        category.isNeededToCheckUSBRefresh = true;
        getPreferenceScreen().addPreference(category);
        category.init();
    }

    // [BUA+] BUA+ media information
    private void addCategoryForBUA(StorageVolumePreferenceCategory category) {
        mCategories.add(category);
        getPreferenceScreen().addPreference(category);
        category.initForBUA();
    }

    // [VZW] H/W storage information
    private void addCategoryForTotalMemory(StorageVolumePreferenceCategory category) {
        mCategories.add(category);
        getPreferenceScreen().addPreference(category);
        category.initForTotalMemory();
    }

    private boolean isMassStorageEnabled() {
        // Mass storage is enabled if primary volume supports it
        final StorageVolume[] volumes = mStorageManager.getVolumeList();
        final StorageVolume primary = StorageManager.getPrimaryVolume(volumes);
        return primary != null && primary.allowMassStorage();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        getActivity().registerReceiver(mMediaScannerReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_STATE);
        getActivity().registerReceiver(mMediaScannerReceiver, intentFilter);

        // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        if (Settings.System.getInt(getContentResolver(), "display_unit", 1) == 1) {
            mDisplayUnit = true;
        } else {
            mDisplayUnit = false;
        }
        // [END]

        for (StorageVolumePreferenceCategory category : mCategories) {
            // [BUA+][VZW] Do not run onReumse() When a category belongs to 'BUA' or 'HW Total menu'
            if (!category.isExistedHWTotalMenu && !category.isExistedBUACategory) {
                if (!category.isNeededToCheckUSBRefresh) {
                    // nothing
                } else {
                    category.onResume();
                }
            }
        }

        // [START][2015-02-16][seungyeop.yeom] support perform click for Search function
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra("perform",
                false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
        // [END][2015-02-16][seungyeop.yeom] support perform click for Search function
    }

    /*
     * Author : seungyeop.yeom
     * Type : startResult() method
     * Date : 2015-02-16
     * Brief : perform click for search function
     */
    private void startResult() {
        if (mSearch_result.equals(StorageVolumePreferenceCategory.KEY_CACHE)) {
            ConfirmClearCacheFragment.show(this);
        }
    }

    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i("hsmodel", "Received storage state changed notification that " + path +
                    " changed state from " + oldState + " to " + newState);

            for (StorageVolumePreferenceCategory category : mCategories) {
                //for (StorageVolume volume : storageVolumes ) {
                //final StorageVolume volume = category.getStorageVolume();
                if (path.startsWith(PATH_OF_USBSTORAGE)) {
                    String theVolumePath = category.getVolumePath();
                    String theKey = category.getKey();
                    if (theVolumePath != null && theVolumePath.equals(path)) {
                        if (isConnectedToUSBStorage(path)) {
                            // update
                            Log.i("hsmodel", "To update : " + path);
                            category.isNeededToCheckUSBRefresh = true;
                            category.setVolumePath(path);
                            category.setKey("already_defined");
                            category.onStorageStateChanged();
                            break;
                        } else {
                            // remove
                            Log.i("hsmodel", "To remove : " + path);
                            category.isNeededToCheckUSBRefresh = false;
                            category.setVolumePath(null);
                            category.setKey("already_defined");
                            //mCategories.remove(category);
                            getPreferenceScreen().removePreference(category);
                            break;
                        }
                    } else if ((theVolumePath == null || (theKey != null && theKey
                            .equals("already_defined")))
                            && category.getStorageVolume() != null
                            && category.getStorageVolume().getPath().startsWith(PATH_OF_USBSTORAGE)) {
                        if (!isConnectedToUSBStorage(path)) {
                            Log.i("hsmodel", "skip");
                            break;
                        }
                        Log.i("hsmodel", "To add : " + path);
                        category.isNeededToCheckUSBRefresh = true;
                        category.setVolumePath(path);
                        getPreferenceScreen().addPreference(category);
                        if (category.getKey() != null
                                && category.getKey().equals("already_defined")
                                && path.equals(category.getStorageVolume().getPath())) {
                            category.onStorageStateChanged();
                            break;
                        } else if (theKey == null) {
                            category.init();
                            category.onResume();
                            break;
                        }
                    }
                } else {
                    final StorageVolume volume = category.getStorageVolume();
                    if (volume != null && path.equals(volume.getPath())) {
                        category.isNeededToCheckUSBRefresh = true;
                        category.onStorageStateChanged();
                        break;
                    }
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mMediaScannerReceiver);
        for (StorageVolumePreferenceCategory category : mCategories) {
            // [BUA+][VZW] Do not run onReumse() When a category belongs to 'BUA' or 'HW Total menu'
            if (!category.isExistedHWTotalMenu && !category.isExistedBUACategory) {
                if (!category.isNeededToCheckUSBRefresh) {
                    // nothing
                } else {
                    category.onPause();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "onDestroy : mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.storage, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        final MenuItem sDisplayUnitMB = menu.findItem(R.id.display_unit_mb);
        final MenuItem sDisplayUnitGB = menu.findItem(R.id.display_unit_gb);

        if (sDisplayUnitMB != null && sDisplayUnitGB != null) {
            if (mDisplayUnit) {
                sDisplayUnitGB.setVisible(false);
                sDisplayUnitMB.setVisible(true);
            } else {
                sDisplayUnitMB.setVisible(false);
                sDisplayUnitGB.setVisible(true);
            }
        }
        // [END][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        final MenuItem usb = menu.findItem(R.id.storage_usb);
        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        boolean usbItemVisible = !isMassStorageEnabled()
                && !um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER);
        usb.setVisible(usbItemVisible);
        //usb.setVisible(false);

        if (!isVZWOperator) {
            if (sDisplayUnitMB != null && sDisplayUnitGB != null) {
                sDisplayUnitGB.setVisible(false);
                sDisplayUnitMB.setVisible(false);
            }
        }

        if (!Utils.isUI_4_1_model(getActivity())) {
            usb.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.storage_usb:
            Bundle bundle = new Bundle();
            bundle.putBoolean(UsbSettingsControl.EXTRA_USB_LAUNCHER, false);
            if (getActivity() instanceof PreferenceActivity) {
                ((PreferenceActivity)getActivity()).startPreferencePanel(
                        UsbSettings.class.getCanonicalName(),
                        bundle,
                        R.string.usb_pc_connection_type, null,
                        this, 0);
            } else {
                startFragment(this, UsbSettings.class.getCanonicalName(), -1, bundle,
                        R.string.usb_pc_connection_type);
            }
            return true;

            // [START][2014-03-17][seungyeop.yeom] add menu for Display in GB, MB
        case R.id.display_unit_gb: {
            Log.d("YSY", "DISPLAY_UNIT_GB on");
            mDisplayUnit = true;
            Settings.System.putInt(getContentResolver(),
                    "display_unit", 1);
            for (StorageVolumePreferenceCategory category : mCategories) {
                category.onStorageStateChanged();
            }
            return true;
        }
        case R.id.display_unit_mb: {
            Log.d("YSY", "DISPLAY_UNIT_MB on");
            mDisplayUnit = false;
            Settings.System.putInt(getContentResolver(),
                    "display_unit", 0);
            for (StorageVolumePreferenceCategory category : mCategories) {
                category.onStorageStateChanged();
            }
            return true;
        }
        // [END]
        default:
            break; // yonguk.kim JB+ migration 20130130 build error fix
        }
        return super.onOptionsItemSelected(item);
    }

    private synchronized IMountService getMountService() {
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }
        return mMountService;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (StorageVolumePreferenceCategory.KEY_CACHE.equals(preference.getKey())) {
            ConfirmClearCacheFragment.show(this);
            return true;
        }

        for (StorageVolumePreferenceCategory category : mCategories) {
            Intent intent = category.intentForClick(preference);
            Log.d("YB", "intentForClick");
            if (intent != null) {
                //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM
                        &&
                        intent.getIntExtra("sd_encryption", 0) == 1) {
                    Log.d("YB", "3LM SD Encryption");
                    mIntent = intent;
                    new AlertDialog.Builder(getActivity())
                            .setTitle(android.R.string.dialog_alert_title)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(R.string.sd_encrypt_dialog)
                            .setPositiveButton(android.R.string.ok, mHandler)
                            .setNegativeButton(android.R.string.cancel, mHandler)
                            .create()
                            .show();
                    return true;
                }
                //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]

                /** @date  : 2013-11-27
                 *  @name  : seungyeop.yeom
                 *  @brief : If you select the App in the stopped state, and guides you to be able to turn on.
                 */
                String intentComponentName = intent.getStringExtra("component");
                if (intentComponentName != null) {
                    if (appIsEnabled(intentComponentName)) {
                        // Don't go across app boundary if monkey is running
                        if (!Utils.isMonkeyRunning()) {
                            /** date  : 2013-08-15
                             *  name  : seungyeop.yeom
                             *  brief : Modification operations is performed. (split view)
                             */
                            try {
                                if (intent.getComponent() != null) {
                                    if (preference == category.getmItemAppsPreference()) {
                                        startFragment(
                                                this,
                                                ManageApplications.class.getCanonicalName(),
                                                -1, null, R.string.memory_apps_usage);
                                        Log.d("YSY", "enter fragment appIsEnabled");
                                    } else if (preference == category.getmFormatPreference()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable(
                                                StorageVolume.EXTRA_STORAGE_VOLUME,
                                                category.getVOLUME());
                                        startFragment(this,
                                                MediaFormatFragment.class.getCanonicalName(),
                                                -1, bundle, R.string.media_format_title);
                                    } else {
                                        Log.d("YSY", "activity appIsEnabled : "
                                                + intent.getComponent().getShortClassName());
                                        startActivity(intent);
                                    }
                                } else {
                                    startActivity(intent);
                                }

                            } catch (ActivityNotFoundException anfe) {
                                Log.w(TAG, "No activity found for intent " + intent);
                            } catch (NullPointerException npe) {
                                Log.w(TAG, "null pointer exception for intent " + intent);
                            } catch (Exception e) {
                                Log.w(TAG, "all exception for intent " + intent);
                            }
                        }
                    } else {
                        confirmDialog(intentComponentName);
                    }
                } else {
                    // Don't go across app boundary if monkey is running
                    if (!Utils.isMonkeyRunning()) {
                        /** date  : 2013-08-15
                         *  name  : seungyeop.yeom
                         *  brief : Modification operations is performed. (split view)
                         */
                        try {
                            if (intent.getComponent() != null) {
                                if (preference == category.getmItemAppsPreference()) {
                                    startFragment(
                                            this,
                                            ManageApplications.class.getCanonicalName(),
                                            -1, null, R.string.memory_apps_usage);
                                    Log.d("YSY", "enter fragment");
                                } else if (preference == category.getmFormatPreference()) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(StorageVolume.EXTRA_STORAGE_VOLUME,
                                            category.getVOLUME());
                                    startFragment(this,
                                            MediaFormatFragment.class.getCanonicalName(), -1,
                                            bundle, R.string.media_format_title);
                                } else {
                                    Log.d("YSY", "activity : "
                                            + intent.getComponent().getShortClassName());
                                    startActivity(intent);
                                }
                            } else {
                                startActivity(intent);
                            }
                        } catch (ActivityNotFoundException anfe) {
                            Log.w(TAG, "No activity found for intent " + intent);
                        } catch (NullPointerException npe) {
                            Log.w(TAG, "null pointer exception for intent " + intent);
                        } catch (Exception e) {
                            Log.w(TAG, "all exception for intent " + intent);
                        }
                    }
                }
                return true;
            }

            final StorageVolume volume = category.getStorageVolume();
            if (volume != null && category.mountToggleClicked(preference)) {
                sLastClickedMountToggle = preference;
                sClickedMountPoint = volume.getPath();
                String state = mStorageManager.getVolumeState(volume.getPath());
                if (Environment.MEDIA_MOUNTED.equals(state) ||
                        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    unmount();
                } else {
                    mount();
                }
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver mMediaScannerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean isUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                String usbFunction = mUsbManager.getDefaultFunction();
                for (StorageVolumePreferenceCategory category : mCategories) {
                    if (!category.isNeededToCheckUSBRefresh) {
                        continue;
                    }
                    category.onUsbStateChanged(isUsbConnected, usbFunction);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                for (StorageVolumePreferenceCategory category : mCategories) {
                    if (!category.isNeededToCheckUSBRefresh) {
                        continue;
                    }
                    category.onMediaScannerFinished();
                }
            }
        }
    };

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DLG_CONFIRM_UNMOUNT:
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_confirm_unmount_title)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // [S][2012.06.18][hsmodel.jeon][TD_95708] To provide proper label by SD card state.
                            String state = mStorageManager.getVolumeState(sClickedMountPoint);
                            if (Environment.MEDIA_MOUNTED.equals(state) ||
                                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                                doUnmount();
                            } else {
                                if (Environment.MEDIA_UNMOUNTED.equals(state)
                                        || Environment.MEDIA_NOFS.equals(state)
                                        || Environment.MEDIA_UNMOUNTABLE.equals(state)) {
                                    sLastClickedMountToggle.setEnabled(false);
                                    sLastClickedMountToggle.setTitle(mResources
                                            .getString(R.string.sd_mount));
                                    sLastClickedMountToggle.setSummary(mResources
                                            .getString(R.string.sp_sd_mount_summary_NORMAL));
                                } else {
                                    sLastClickedMountToggle.setEnabled(false);
                                    sLastClickedMountToggle.setTitle(mResources
                                            .getString(R.string.sd_mount));
                                    sLastClickedMountToggle.setSummary(mResources
                                            .getString(R.string.sp_sd_insert_summary_NORMAL));

                                }
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_unmount_text)
                    .create();
            // [E][2012.06.18][hsmodel.jeon][TD_95708] To provide proper label by SD card state.
        case DLG_ERROR_UNMOUNT:
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_error_unmount_title)
                    .setNeutralButton(R.string.dlg_ok, null)
                    .setMessage(R.string.dlg_error_unmount_text)
                    .create();
        default:
            break; // yonguk.kim JB+ migration 20130130 build error fix
        }
        return null;
    }

    private void doUnmount() {
        IMountService mountService = getMountService();
        try {
            sLastClickedMountToggle.setEnabled(false);
            sLastClickedMountToggle.setTitle(getString(R.string.sd_ejecting_title));
            sLastClickedMountToggle.setSummary(getString(R.string.sd_ejecting_summary));
            mountService.unmountVolume(sClickedMountPoint, true, false);
        } catch (RemoteException e) {
            // Informative dialog to user that unmount failed.
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    private boolean hasAppsAccessingStorage() throws RemoteException {
        IMountService mountService = getMountService();
        int stUsers[] = mountService.getStorageUsers(sClickedMountPoint);
        if (stUsers != null && stUsers.length > 0) {
            return true;
        }
        // TODO FIXME Parameterize with mountPoint and uncomment.
        // On HC-MR2, no apps can be installed on sd and the emulated internal storage is not
        // removable: application cannot interfere with unmount
        /*
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ApplicationInfo> list = am.getRunningExternalApplications();
        if (list != null && list.size() > 0) {
            return true;
        }
        */
        // Better safe than sorry. Assume the storage is used to ask for confirmation.
        return true;
    }

    protected static boolean isUnmountable() {
        boolean isUnmountable = true;
        //START. Check MTP connection
        final String STATE_PATH = "/sys/class/android_usb/android0/state";
        String state = null;
        try {
            state = FileUtils.readTextFile(new File(STATE_PATH), 0, null).trim();
        } catch (IOException e) {
            Log.w(TAG, "IOException " + e);
        }

        final String currentUsbMode = SystemProperties.get("sys.usb.config", "none");

        /*if(!state.equals("DISCONNECTED") && (currentUsbMode.equals("mtp_only,adb") || currentUsbMode.equals("mtp_only")
            || currentUsbMode.equals("pc_suite,adb") || currentUsbMode.equals("pc_suite")
            || currentUsbMode.equals("ptp_only,adb") || currentUsbMode.equals("ptp_only")
            || currentUsbMode.equals("usb_enable_mtp,adb") || currentUsbMode.equals("usb_enable_mtp")
        ))*/

        // [seungyeop.yeom][2013-11-28][STRAT] static analysis
        if (state != null) {
            if (!state.equals("DISCONNECTED")
                    &&
                    (currentUsbMode.startsWith(UsbManagerConstants.USB_FUNCTION_PC_SUITE)
                            || currentUsbMode.startsWith(UsbManagerConstants.USB_FUNCTION_MTP_ONLY)
                            || currentUsbMode.startsWith(UsbManagerConstants.USB_FUNCTION_PTP_ONLY)
                            || currentUsbMode
                                    .startsWith(UsbManagerConstants.USB_FUNCTION_AUTO_CONF)
                            || currentUsbMode.equals("usb_enable_mtp"))) {
                Log.i("hsmodel", "Can't unmount SD card");
                isUnmountable = false;
                //return;
            } else {
                Log.i("hsmodel", "Can unmount SD card");
            }
            //END.
        }
        // [seungyeop.yeom][END]
        return isUnmountable;
    }

    private void unmount() {
        // Check if external media is in use.
        //try {
        Log.i("hsmodel", "mClickedMountPoint : " + sClickedMountPoint);
        if (sClickedMountPoint.startsWith(PATH_OF_USBSTORAGE)) {
            // [seungyeop.yeom][2013-08-19] add toast for usb unmount
            if (MORE_THAN_UI_4_2) {
                Toast.makeText(getActivity(), R.string.usb_unmount_inform_text_ex,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.usb_unmount_inform_text,
                        Toast.LENGTH_SHORT).show();
            }
            doUnmount();
        } else if (!isUnmountable()/*hasAppsAccessingStorage()*/) {
            // Present dialog to user
            //showDialogInner(DLG_CONFIRM_UNMOUNT);
            if (MORE_THAN_UI_4_2) {
                Toast.makeText(getActivity(),
                        R.string.impossible_unmount_sd_card_ex,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),
                        R.string.impossible_unmount_sd_card, Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            // [seungyeop.yeom][2013-08-19] add toast for sd card unmount
            if (MORE_THAN_UI_4_2) {
                Toast.makeText(getActivity(), R.string.sd_unmount_inform_text_ex,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.sd_unmount_inform_text,
                        Toast.LENGTH_SHORT).show();
            }
            doUnmount();
        }
        //} catch (RemoteException e) {
        // Very unlikely. But present an error dialog anyway
        //    Log.e(TAG, "Is MountService running?");
        //    showDialogInner(DLG_ERROR_UNMOUNT);
        //}
    }

    private void mount() {
        IMountService mountService = getMountService();
        try {
            if (mountService != null) {
                mountService.mountVolume(sClickedMountPoint);
            } else {
                Log.e(TAG, "Mount service is null, can't mount");
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException " + ex);
        }
    }

    private void onCacheCleared() {
        for (StorageVolumePreferenceCategory category : mCategories) {
            category.onCacheCleared();
        }
    }

    private static class ClearCacheObserver extends IPackageDataObserver.Stub {
        private final Memory mTarget;
        private int mRemaining;

        public ClearCacheObserver(Memory target, int remaining) {
            mTarget = target;
            mRemaining = remaining;
        }

        @Override
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            synchronized (this) {
                if (--mRemaining == 0) {
                    mTarget.onCacheCleared();
                }
            }
        }
    }

    /**
     * Dialog to request user confirmation before clearing all cache data.
     */
    public static class ConfirmClearCacheFragment extends DialogFragment {
        public static void show(Memory parent) {
            if (!parent.isAdded()) {
                return;
            }

            final ConfirmClearCacheFragment dialog = new ConfirmClearCacheFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_CLEAR_CACHE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);

            if (MORE_THAN_UI_4_2) {
                builder.setTitle(R.string.memory_clear_cache_title_ex2);
                builder.setMessage(getString(R.string.memory_clear_cache_message_ex2));
            } else {
                builder.setTitle(R.string.memory_clear_cache_title_ex);
                builder.setMessage(getString(R.string.memory_clear_cache_message_ex));
            }

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Memory target = (Memory)getTargetFragment();
                    final PackageManager pm = context.getPackageManager();
                    final List<PackageInfo> infos = pm.getInstalledPackages(0);
                    final ClearCacheObserver observer = new ClearCacheObserver(
                            target, infos.size());
                    for (PackageInfo info : infos) {
                        pm.deleteApplicationCacheFiles(info.packageName, observer);
                    }
                }
            });
            builder.setNegativeButton(R.string.no, null);

            return builder.create();
        }
    }

    // [VZW] check whether VZW or not
    protected void isVZWOperator() {
        if (Config.getOperator().equals(Config.VZW)) {
            isVZWOperator = true;
        } else {
            isVZWOperator = false;
        }
    }

    // [Common] Check whether enabled app or not
    public void confirmDialog(final String intentComponentName) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(getText(R.string.sp_dlg_note_NORMAL))
                .setPositiveButton(getText(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                final Intent intent = new Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + intentComponentName));
                                startActivity(intent);

                            }
                        })
                .setNegativeButton(getText(R.string.dlg_cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {

                            }
                        }).setMessage(getText(R.string.sp_app_enable_confirm_message_NORMAL))
                .create();
        dialog.show();
    }

    // [Common] Check whether enabled app or not
    public boolean appIsEnabled(String intentComponentName) {

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo(intentComponentName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return info.enabled;

    }

    // [VZW][BUA+] Check whether enabled BUA+ or not
    protected boolean cloudEnable() {

        boolean result = false;
        PackageManager mPackageManager = getPackageManager();
        if (mPackageManager != null) {
            result = mPackageManager.hasSystemFeature("com.lge.cloudservice.enabled");
        }
        return result;
    }

    // [USB OTG] To check the status of USB storage
    protected boolean isConnectedToUSBStorage(String pathOfStorageVolume) {
        String state = pathOfStorageVolume != null ? mStorageManager
                .getVolumeState(pathOfStorageVolume) : Environment.MEDIA_MOUNTED;
        boolean sd_encryption = Settings.Global.getInt(getActivity().getContentResolver(),
                "sd_encryption", 0) == 1;
        Log.i("hsmodel", "state : " + state);
        Log.i("YSY", "sd_encryption : " + sd_encryption);

        if (sd_encryption) {
            return false;
        }

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            if (Environment.MEDIA_UNMOUNTED.equals(state)
                    || Environment.MEDIA_NOFS.equals(state)
                    || Environment.MEDIA_UNMOUNTABLE.equals(state)) {
                return true;
            } else {
                return false;
            }
        }
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
    // Factory reset
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveWipeDateChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
    private DialogInterface.OnClickListener mHandler = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                startActivity(mIntent);
                mIntent = null;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mIntent = null;
                break;
            default:
                break;
            }
        }
    };
    //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]

    /*
     * Author : seungyeop.yeom
     * Type : Search object
     * Date : 2015-02-16
     * Brief : Create of Storage search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            // Search Storage Main
            setSearchIndexData(context, "storage_settings",
                    context.getString(R.string.storage_settings), "main", null,
                    null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$StorageSettingsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);

            // Search Total space
            setSearchIndexData(context, KEY_TOTAL_SPACE,
                    context.getString(R.string.memory_size),
                    context.getString(R.string.storage_settings), null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$StorageSettingsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);

            // Search Available
            setSearchIndexData(context, KEY_AVAILABLE,
                    context.getString(R.string.memory_available),
                    context.getString(R.string.storage_settings), null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$StorageSettingsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);

            // Search Apps
            setSearchIndexData(context, KEY_APPS,
                    context.getString(R.string.memory_apps_usage),
                    context.getString(R.string.storage_settings), null, null,
                    "android.settings.APPLICATION_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);

            // Search Pictures, videos
            setSearchPicturesVideos(context);

            // Search Video
            setSearchAudio(context);

            setSearchIndexData(context, KEY_DOWNLOADS,
                    context.getString(R.string.memory_downloads_usage),
                    context.getString(R.string.storage_settings), null, null,
                    DownloadManager.ACTION_VIEW_DOWNLOADS, null, null, 1, null,
                    null, null, 1, 0);
            setSearchIndexData(context,
                    StorageVolumePreferenceCategory.KEY_CACHE,
                    context.getString(R.string.memory_media_cache_usage),
                    context.getString(R.string.storage_settings), null, null,
                    "android.intent.action.MAIN",
                    "com.android.settings.Settings$StorageSettingsActivity",
                    "com.android.settings", 1, null, null, null, 1, 0);
            setSearchIndexData(context, KEY_MISC,
                    context.getString(R.string.memory_media_misc_usage),
                    context.getString(R.string.storage_settings), null, null,
                    null, "com.android.settings.deviceinfo.MiscFilesHandler",
                    "com.android.settings", 1, null, null, null, 1, 0);

            // Search Tv apps
            setSearchTvApps(context);
            return mResult;
        }

        private void setSearchPicturesVideos(Context context) {
            String mDcimIntentAction;
            String mDcimIntentClass;
            String mDcimIntentPackage;

            if (Utils.checkPackage(context, "com.android.gallery3d")) {
                mDcimIntentAction = "android.intent.action.MAIN";
                mDcimIntentClass = "com.android.gallery3d.app.Gallery";
                mDcimIntentPackage = "com.android.gallery3d";
            } else {
                mDcimIntentAction = "android.intent.action.MAIN";
                mDcimIntentClass = "com.google.android.apps.plus.phone.ConversationListActivity";
                mDcimIntentPackage = "com.google.android.apps.plus";
            }

            setSearchIndexData(context, KEY_PICTURES_VIDEOS,
                    context.getString(R.string.memory_dcim_usage),
                    context.getString(R.string.storage_settings), null, null,
                    mDcimIntentAction, mDcimIntentClass, mDcimIntentPackage, 1,
                    null, null, null, 1, 0);
        }

        private void setSearchAudio(Context context) {
            String mMusicIntentAction;
            String mMusicIntentClass;
            String mMusicIntentPackage;

            if (Utils.checkPackage(context, "com.lge.music")) {
                mMusicIntentAction = null;
                mMusicIntentClass = "com.lge.music.MusicBrowserActivity";
                mMusicIntentPackage = "com.lge.music";
            } else {
                mMusicIntentAction = Intent.ACTION_GET_CONTENT;
                mMusicIntentClass = null;
                mMusicIntentPackage = null;
            }

            setSearchIndexData(context, KEY_AUDIO,
                    context.getString(R.string.memory_music_usage),
                    context.getString(R.string.storage_settings), null, null,
                    mMusicIntentAction, mMusicIntentClass, mMusicIntentPackage,
                    1, null, null, null, 1, 0);
        }

        private void setSearchTvApps(Context context) {
            int isTVApps = 0;

            if (Utils.checkPackage(context, "com.lge.oneseg")
                    && "JP".equals(Config.getCountry())) {
                isTVApps = 1;
            }

            setSearchIndexData(context, KEY_TV_APPS,
                    context.getString(R.string.tv_box_title),
                    context.getString(R.string.storage_settings), null, null,
                    "android.intent.action.MAIN",
                    "com.lge.oneseg.list.TdmbListActivity", "com.lge.oneseg",
                    1, null, null, null, isTVApps, 0);
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
