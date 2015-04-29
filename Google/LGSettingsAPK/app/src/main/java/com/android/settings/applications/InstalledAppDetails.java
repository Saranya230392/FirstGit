/**
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.settings.applications;

import com.android.internal.telephony.ISms;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.ApplicationsState.AppEntry;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.INotificationManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.IUsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.BulletSpan;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AppSecurityPermissions;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
// 3LM_MDM_DCM jihun.im@lge.com 20121015
import android.os.Build;
import android.preference.PreferenceFrameLayout;
import android.provider.Settings;
// 3LM_MDM L
import android.os.IDeviceManager3LM;
import com.android.settings.lgesetting.Config.Config;

/**
 * Activity to display application information from Settings. This activity presents
 * extended information associated with a package like code, data, total size, permissions
 * used by the application and also the set of default launchable activities.
 * For system applications, an option to clear user data is displayed only if data size is > 0.
 * System applications that do not want clear user data do not have this option.
 * For non-system applications, there is no option to clear data. Instead there is an option to
 * uninstall the application.
 */
public class InstalledAppDetails extends Fragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        ApplicationsState.Callbacks {
    private static final String TAG = "InstalledAppDetails";
    static final boolean SUPPORT_DISABLE_APPS = true;
    private static final boolean localLOGV = false;

    public static final String ARG_PACKAGE_NAME = "package";

    private PackageManager mPm;
    private UserManager mUserManager;
    private IUsbManager mUsbManager;
    private AppWidgetManager mAppWidgetManager;
    private DevicePolicyManager mDpm;
    private ISms mSmsManager;
    private ApplicationsState mState;
    private ApplicationsState.Session mSession;
    private ApplicationsState.AppEntry mAppEntry;
    private boolean mInitialized;
    private boolean mShowUninstalled;
    private PackageInfo mPackageInfo;
    private CanBeOnSdCardChecker mCanBeOnSdCardChecker;
    private View mRootView;
    private Button mUninstallButton;
    private View mMoreControlButtons;
    private Button mSpecialDisableButton;
    private boolean mMoveInProgress = false;
    private boolean mUpdatedSysApp = false;
    private Button mActivitiesButton;
    private View mScreenCompatSection;
    private CheckBox mAskCompatibilityCB;
    private CheckBox mEnableCompatibilityCB;
    private boolean mCanClearData = true;
    private boolean mAppControlRestricted = false;
    private TextView mAppVersion;
    private TextView mTotalSize;
    private TextView mAppSize;
    private TextView mDataSize;
    private TextView mExternalCodeSize;
    private TextView mExternalDataSize;
    private TextView mExternalCodeSizePrefix; // yonguk.kim 20120816 ICS Stuff Porting to JB - Care string length
    private final int TEXTVIEW_MAX_WIDTH = 210; // dp
    private ClearUserDataObserver mClearDataObserver;
    // Views related to cache info
    private TextView mCacheSize;
    private Button mClearCacheButton;
    private ClearCacheObserver mClearCacheObserver;
    private Button mForceStopButton;
    private Button mClearDataButton;
    private Button mMoveAppButton;
    private CompoundButton mNotificationSwitch;

    private PackageMoveObserver mPackageMoveObserver;

    // rebestm - kk migration
    private final HashSet<String> mHomePackages = new HashSet<String>();
    private boolean mDisableAfterUninstall;

    // [S]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
    private boolean notUserChange = false;

    private boolean mHaveSizes = false;
    private long mLastCodeSize = -1;
    private long mLastDataSize = -1;
    private long mLastExternalCodeSize = -1;
    private long mLastExternalDataSize = -1;
    private long mLastCacheSize = -1;
    private long mLastTotalSize = -1;

    //internal constants used in Handler
    private static final int OP_SUCCESSFUL = 1;
    private static final int OP_FAILED = 2;
    private static final int CLEAR_USER_DATA = 1;
    private static final int CLEAR_CACHE = 3;
    private static final int PACKAGE_MOVE = 4;

    // invalid size value used initially and also when size retrieval through PackageManager
    // fails for whatever reason
    private static final int SIZE_INVALID = -1;

    // Resource strings
    private CharSequence mInvalidSizeStr;
    private CharSequence mComputingStr;

    // Dialog identifiers used in showDialog
    private static final int DLG_BASE = 0;
    private static final int DLG_CLEAR_DATA = DLG_BASE + 1;
    private static final int DLG_FACTORY_RESET = DLG_BASE + 2;
    private static final int DLG_APP_NOT_FOUND = DLG_BASE + 3;
    private static final int DLG_CANNOT_CLEAR_DATA = DLG_BASE + 4;
    private static final int DLG_FORCE_STOP = DLG_BASE + 5;
    private static final int DLG_MOVE_FAILED = DLG_BASE + 6;
    private static final int DLG_DISABLE = DLG_BASE + 7;
    private static final int DLG_DISABLE_NOTIFICATIONS = DLG_BASE + 8;
    private static final int DLG_SPECIAL_DISABLE = DLG_BASE + 9;
    private static final int DLG_FACTORY_RESET_VAPP = DLG_BASE + 10;
    private static final int DLG_DISABLE_VAPP = DLG_BASE + 11;
    // Menu identifiers
    public static final int UNINSTALL_ALL_USERS_MENU = 1;

    // Result code identifiers
    public static final int REQUEST_UNINSTALL = 1;
    public static final int REQUEST_MANAGE_SPACE = 2;

    private static final String PACKAGE_SETTINGS = "com.android.settings";
    private static final String PACKAGE_HOMEBOY = "com.lguplus.u070pv507l";
    private static final String PACKAGE_HOMEBOY2 = "com.lguplus.u070fl40l";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // If the fragment is gone, don't process any more messages.
            if (getView() == null) {
                return;
            }
            switch (msg.what) {
            case CLEAR_USER_DATA:
                processClearMsg(msg);
                break;
            case CLEAR_CACHE:
                // Refresh size info
                mState.requestSize(mAppEntry.info.packageName);
                break;
            case PACKAGE_MOVE:
                processMoveMsg(msg);
                break;
            default:
                break;
            }
        }
    };

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_USER_DATA);
            msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
            mHandler.sendMessage(msg);
        }
    }

    class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_CACHE);
            msg.arg1 = succeeded ? OP_SUCCESSFUL : OP_FAILED;
            mHandler.sendMessage(msg);
        }
    }

    class PackageMoveObserver extends IPackageMoveObserver.Stub {
        public void packageMoved(String packageName, int returnCode) throws RemoteException {
            final Message msg = mHandler.obtainMessage(PACKAGE_MOVE);
            msg.arg1 = returnCode;
            MatrixTime(4000);
            mHandler.sendMessage(msg);
        }
    }

    private String getSizeStr(long size) {
        if (size == SIZE_INVALID) {
            return mInvalidSizeStr.toString();
        }
        return Formatter.formatFileSize(getActivity(), size);
    }

    private void initDataButtons() {
        // rebestm - kk migration
        if ((mAppEntry.info.manageSpaceActivityName == null)
                && ((mAppEntry.info.flags & (ApplicationInfo.FLAG_SYSTEM
                        | ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA))
                            == ApplicationInfo.FLAG_SYSTEM
                        || mDpm.packageHasActiveAdmins(mPackageInfo.packageName)
                        || checkPackageNameForClearData(mPackageInfo.packageName))) {
            // rebestm 20131224 requested by bs0201@lgepartner.com
            Log.d(TAG, "Date clear block");
            mClearDataButton.setText(R.string.clear_user_data_text);
            mClearDataButton.setEnabled(false);
            mCanClearData = false;
        } else {
            if (mAppEntry.info.manageSpaceActivityName != null) {
                mClearDataButton.setText(R.string.manage_space_text);
                //                if ( mPackageInfo.packageName.equals(PACKAGE_GOOGLE_GMS) ) 
                {
                    if (mAppEntry.info.enabled == false) {
                        mClearDataButton.setEnabled(false);
                        mCanClearData = false;
                    } else {
                        mClearDataButton.setEnabled(true);
                        mCanClearData = true;
                    }
                }
            } else {
                mClearDataButton.setText(R.string.clear_user_data_text);
            }
            mClearDataButton.setOnClickListener(this);

            if (mAppControlRestricted) {
                mClearDataButton.setEnabled(false);
            }
        }
    }

    private CharSequence getMoveErrMsg(int errCode) {
        switch (errCode) {
        case PackageManager.MOVE_FAILED_INSUFFICIENT_STORAGE:
            return getActivity().getString(R.string.insufficient_storage);
        case PackageManager.MOVE_FAILED_DOESNT_EXIST:
            return getActivity().getString(R.string.does_not_exist);
        case PackageManager.MOVE_FAILED_FORWARD_LOCKED:
            return getActivity().getString(R.string.app_forward_locked);
        case PackageManager.MOVE_FAILED_INVALID_LOCATION:
            return getActivity().getString(R.string.invalid_location);
        case PackageManager.MOVE_FAILED_SYSTEM_PACKAGE:
            return getActivity().getString(R.string.system_package);
        case PackageManager.MOVE_FAILED_INTERNAL_ERROR:
            return "";
        default:
            break;
        }
        return "";
    }

    private void initMoveButton() {
        //myeonghwan.kim@lge.com External memory for V7 [S]
        boolean fUseSDCardText = false;


        if (Config.getFWConfigBool(getActivity().getApplicationContext(), com.lge.config.ConfigBuildFlags.CAPP_MOVE_SDCARD,
            "com.lge.config.ConfigBuildFlags.CAPP_MOVE_SDCARD") &&
            (SystemProperties.get("ro.build.characteristics").contains("nosdcard") == false)) {

            StorageManager storageManager = (StorageManager)getActivity().getSystemService(
                    Context.STORAGE_SERVICE);
            String state = null;
            if (System.getenv("EXTERNAL_ADD_STORAGE") != null) {
                state = storageManager.getVolumeState(System.getenv("EXTERNAL_ADD_STORAGE"));
            }

            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                mMoveAppButton.setVisibility(View.INVISIBLE);
                return;
            }
            else {
                fUseSDCardText = true;
            }
        }
        else {
            if (Environment.isExternalStorageEmulated()) {
                mMoveAppButton.setVisibility(View.INVISIBLE);
                return;
            }
        }
        //myeonghwan.kim@lge.com External memory for V7 [E]

        boolean dataOnly = false;
        dataOnly = (mPackageInfo == null) && (mAppEntry != null);
        boolean moveDisable = true;
        if (dataOnly) {
            mMoveAppButton.setText(R.string.move_app);
            // rebestm - WBT
        } else if ((mAppEntry != null)
                && ((mAppEntry.info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0)) {
            mMoveAppButton.setText(R.string.move_app_to_internal);
            // Always let apps move to internal storage from sdcard.
            moveDisable = false;
        } else {
            mMoveAppButton.setText(R.string.move_app_to_sdcard);
            // yonguk.kim 20120816 ICS Stuff Porting to JB - change button text for Internal memory device
            //myeonghwan.kim@lge.com External memory for V7 [S]
            if (Utils.supportInternalMemory() && !fUseSDCardText) {
                mMoveAppButton.setText(R.string.sp_move_to_internal_memory_NORMAL);
            }
            //myeonghwan.kim@lge.com External memory for V7 [E]

            mCanBeOnSdCardChecker.init();
            if (mAppEntry != null) {
                moveDisable = !mCanBeOnSdCardChecker.check(mAppEntry.info);
            }
        }
        if (moveDisable || mAppControlRestricted) {
            mMoveAppButton.setEnabled(false);
        } else {
            mMoveAppButton.setOnClickListener(this);
            mMoveAppButton.setEnabled(true);
        }
    }

    private boolean handleDisableable(Button button) {
        boolean disableable = false;
        // Try to prevent the user from bricking their phone
        // by not allowing disabling of apps signed with the
        // system cert and any launcher app in the system.
        // rebestm - kk migration
        if (mHomePackages.contains(mAppEntry.info.packageName)
                || Utils.isSystemPackage(mPm, mPackageInfo)) {
            // Disable button for core system applications.
            button.setText(R.string.disable_text);
        } else if (mAppEntry.info.enabled) {
            button.setText(R.string.disable_text);
            disableable = true;
            // 3LM_MDM_DCM jihun.im@lge.com
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                disableable = !mPackageInfo.packageName.startsWith("com.threelm.dm");
            }
        } else {
            button.setText(R.string.enable_text);
            disableable = true;
        }
        if (mPackageInfo != null && checkPackageNameForDisable(mPackageInfo.packageName)) {
            disableable = false;
        }
        return disableable;
    }

    private void initUninstallButtons() {
        mUpdatedSysApp = (mAppEntry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        final boolean isBundled = (mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean enabled = true;
        if (mUpdatedSysApp) {
            mUninstallButton.setText(R.string.app_factory_reset);
            boolean showSpecialDisable = false;
            if (isBundled) {
                showSpecialDisable = handleDisableable(mSpecialDisableButton);
                if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                    // mSpecialDisableButton.setOnClickListener(this);
                } else {
                    mSpecialDisableButton.setOnClickListener(this);
                }
            }
            if (mAppControlRestricted) {
                showSpecialDisable = false;
            }
            mMoreControlButtons.setVisibility(showSpecialDisable ? View.VISIBLE : View.GONE);
        } else {
            mMoreControlButtons.setVisibility(View.GONE);
            if (isBundled) {
                enabled = handleDisableable(mUninstallButton);
            } else if (checkPackageNameForUninstall(mPackageInfo.packageName)) {
                // [2014-11-20] Block uninstall button
                mUninstallButton.setText(R.string.uninstall_text);
                enabled = false;
            } else if ((mPackageInfo.applicationInfo.flags
                    & ApplicationInfo.FLAG_INSTALLED) == 0
                    && mUserManager.getUsers().size() >= 2) {
                // When we have multiple users, there is a separate menu
                // to uninstall for all users.
                Log.d("YSY", "else if ApplicationInfo.FLAG_INSTALLED) == 0");
                mUninstallButton.setText(R.string.uninstall_text);
                enabled = false;
            } else {
                Log.d("YSY", "else ApplicationInfo.FLAG_INSTALLED) == 0");
                mUninstallButton.setText(R.string.uninstall_text);
            }
        }
        // If this is a device admin, it can't be uninstalled or disabled.
        // We do this here so the text of the button is still set correctly.
        if (mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            enabled = false;
        }

        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            // Check if 3LM prevents uninstall / disable
            // If button is set to ENABLE, ignore 3LM uninstall policy. Still check isPackageDisabled.

            boolean ignore3lmUninstallPolicy;
            ignore3lmUninstallPolicy = mUninstallButton.getText().equals(getString(R.string.enable_text));

            IDeviceManager3LM dm = IDeviceManager3LM.Stub.asInterface(
                ServiceManager.getService(Context.DEVICE_MANAGER_3LM_SERVICE));
            try {
                if ((!ignore3lmUninstallPolicy && !dm.checkAppUninstallPolicies(mAppEntry.info.packageName)) ||
                        dm.isPackageDisabled(mAppEntry.info.packageName)) {
                    enabled = false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "3LM Exception");
            }
        }
        // If this is the default (or only) home app, suppress uninstall (even if
        // we still think it should be allowed for other reasons)
        if (enabled && mHomePackages.contains(mPackageInfo.packageName)) {
            if (isBundled) {
                enabled = false;
            } else {
                ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
                ComponentName currentDefaultHome = mPm.getHomeActivities(homeActivities);
                if (currentDefaultHome == null) {
                    // No preferred default, so permit uninstall only when
                    // there is more than one candidate
                    enabled = (mHomePackages.size() > 1);
                } else {
                    // There is an explicit default home app -- forbid uninstall of
                    // that one, but permit it for installed-but-inactive ones.
                    enabled = !mPackageInfo.packageName.equals(currentDefaultHome.getPackageName());
                }
            }
        }

        if (mAppControlRestricted) {
            enabled = false;
        }


        // exception "com.lge.smartshare" , rebestm
        //        if (Utils.checkPackage(getActivity(),"com.lge.smartshare" ))
        if (mPackageInfo.packageName.equals("com.lge.smartshare")) {
            if (isPackageEnable("com.lge.smartshare")) {
                mUninstallButton.setText(R.string.disable_text);
            } else {
                mUninstallButton.setText(R.string.enable_text);
            }
            Log.i(TAG, "Tablet smartshare 'butoon' ENABLE");
            enabled = true;
        }
        if (mPackageInfo.packageName.equals("com.lge.smartshare.dlna")) {
            if (isPackageEnable("com.lge.smartshare.dlna")) {
                mUninstallButton.setText(R.string.disable_text);
            } else {
                mUninstallButton.setText(R.string.enable_text);
            }
            Log.i(TAG, "Tablet smartshare DLNA 'butoon' ENABLE");
            enabled = true;
        }
        if (mPackageInfo.packageName.equals("com.lge.smartshare.provider")) {
            if (isPackageEnable("com.lge.smartshare.provider")) {
                mUninstallButton.setText(R.string.disable_text);
            } else {
                mUninstallButton.setText(R.string.enable_text);
            }
            Log.i(TAG, "Tablet smartshare Provider 'butoon' ENABLE");
            enabled = true;
        }

        if (mPackageInfo.packageName.equals("com.lge.settings.shortcut")) {
            if (isPackageEnable("com.lge.settings.shortcut")) {
                mUninstallButton.setText(R.string.disable_text);
            } else {
                mUninstallButton.setText(R.string.enable_text);
            }
            Log.i(TAG, "Tablet smartshare 'butoon' ENABLE");
            enabled = true;
        }
        if (mPackageInfo.packageName.equals("com.lge.settings.shortcutBattery")) {
            if (isPackageEnable("com.lge.settings.shortcutBattery")) {
                mUninstallButton.setText(R.string.disable_text);
            } else {
                mUninstallButton.setText(R.string.enable_text);
            }
            Log.i(TAG, "Tablet smartshare 'butoon' ENABLE");
            enabled = true;
        }

        mUninstallButton.setEnabled(enabled);
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            mSpecialDisableButton.setEnabled(enabled);
        }
        if (enabled) {
            // Register listener
            mUninstallButton.setOnClickListener(this);
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                mSpecialDisableButton.setOnClickListener(this);
            }
        }
    }

    public boolean isPackageEnable(String packageName)
    {
        PackageManager pm = getActivity().getPackageManager();

        try {
            if (pm.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                Log.d(TAG, "EnableApplication ->   " + pm.getApplicationEnabledSetting(packageName)); // 0
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception isPackageEnable Disable");
            return false;
        }
        Log.e(TAG, "isPackageEnable Disable");
        return false;
    }

    // yonguk.kim 20120227 Disable "Disable" button
    private boolean checkPackageNameForDisable(String packageName) {
        if (packageName == null) {
            return false;
        }

        if ((Config.getOperator().equals(Config.MPCS) && "com.android.browser".equals(packageName))
                || (Utils.isUI_4_1_model(getActivity()) && "com.lge.appbox.client"
                        .equals(packageName))) {
            return true;
        }

        HashSet<String> disable_blocked_apps = Utils.getDisableBlockedApps(getActivity());
        if (disable_blocked_apps == null || disable_blocked_apps.size() == 0) {
            return false;
        }
        return disable_blocked_apps.contains(packageName);
    }

    // [START][2014-11-20][seungyeop.yeom] Block Force stop function of apps
    private boolean checkPackageNameForForceStop(String packageName) {
        if (packageName == null) {
            return false;
        }

        HashSet<String> mForceStopBlockedList = Utils
                .getForceStopBlockedApps(getActivity());
        if (mForceStopBlockedList == null || mForceStopBlockedList.size() == 0) {
            return false;
        }
        return mForceStopBlockedList.contains(packageName);
    }

    // [END][2014-11-20][seungyeop.yeom] Block Force stop function of apps

    // [START][2014-11-20][seungyeop.yeom] Block Uninstall function of apps
    private boolean checkPackageNameForUninstall(String packageName) {
        if (packageName == null) {
            return false;
        }

        HashSet<String> mUninstallBlockedList = Utils
                .getUninstallBlockedApps(getActivity());
        if (mUninstallBlockedList == null || mUninstallBlockedList.size() == 0) {
            return false;
        }
        return mUninstallBlockedList.contains(packageName);
    }

    // [END][2014-11-20][seungyeop.yeom] Block Uninstall function of apps

    // [START][2014-11-20][seungyeop.yeom] Block Clear data function of apps
    private boolean checkPackageNameForClearData(String packageName) {
        if (packageName == null) {
            return false;
        }

        HashSet<String> mClearDataBlockedList = Utils
                .getClearDataBlockedApps(getActivity());
        if (mClearDataBlockedList == null || mClearDataBlockedList.size() == 0) {
            return false;
        }
        return mClearDataBlockedList.contains(packageName);
    }

    // [END][2014-11-20][seungyeop.yeom] Block Clear data function of apps

    private boolean checkPackageNameForNotification(String packageName) {
        if (packageName == null) {
            return false;
        }
        HashSet<String> notification_blocked_apps = Utils.getNotificationBlockedApps(getActivity());
        if (notification_blocked_apps == null || notification_blocked_apps.size() == 0) {
            return false;
        }
        return notification_blocked_apps.contains(packageName);
    }

    private boolean checkPackageNameForVapp(String packageName) {
        if (packageName == null) {
            return false;
        }
        HashSet<String> vapps = Utils.getVApps(getActivity());
        if (vapps == null || vapps.size() == 0) {
            return false;
        }
        return vapps.contains(packageName);
    }

    private void initNotificationButton() {
        INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        boolean enabled = true; // default on
        // rebestm - kk migration
        try {
            enabled = nm.areNotificationsEnabledForPackage(mAppEntry.info.packageName,
                    mAppEntry.info.uid);
        } catch (android.os.RemoteException ex) {
            Log.w(TAG, "android.os.RemoteException");
        }

        mNotificationSwitch.setChecked(enabled);
        if (Utils.isSystemPackage(mPm, mPackageInfo)) {
            mNotificationSwitch.setEnabled(false);
            // exception "com.lge.smartshare" , rebestm
            //          if (Utils.checkPackage(getActivity(),"com.lge.smartshare" ))
            if (mAppEntry.info.packageName.equals("com.lge.smartshare")
                    || mAppEntry.info.packageName.equals("com.lge.smartshare.dlna")
                    || mAppEntry.info.packageName.equals("com.lge.smartshare.provider")
                    || mAppEntry.info.packageName.equals("com.lge.settings.shortcutBattery")
                    || mAppEntry.info.packageName.equals("com.lge.settings.shortcut")) {
                mNotificationSwitch.setEnabled(true);
            }
        } else if (mPackageInfo != null
                && checkPackageNameForNotification(mPackageInfo.packageName)) {
            mNotificationSwitch.setEnabled(false);
        } else {
            // 3LM_MDM_DCM jihun.im@lge.com
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
                if (Utils.isSystemPackage(mPm, mPackageInfo) || 
                        mAppEntry.info.packageName.startsWith("com.threelm.dm")) {
                    mNotificationSwitch.setEnabled(false);
                    return;
                }
            }
            mNotificationSwitch.setEnabled(true);
            mNotificationSwitch.setOnCheckedChangeListener(this);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mState = ApplicationsState.getInstance(getActivity().getApplication());
        mSession = mState.newSession(this);
        mPm = getActivity().getPackageManager();
        mUserManager = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        IBinder b = ServiceManager.getService(Context.USB_SERVICE);
        mUsbManager = IUsbManager.Stub.asInterface(b);
        mAppWidgetManager = AppWidgetManager.getInstance(getActivity());
        mDpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mSmsManager = ISms.Stub.asInterface(ServiceManager.getService("isms"));

        mCanBeOnSdCardChecker = new CanBeOnSdCardChecker();

        // Need to make sure we have loaded applications at this point.
        mSession.resume(false);

        retrieveAppEntry();

        setHasOptionsMenu(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeMaxWidth(newConfig);
    }

    //yonguk.kim 20120523 externalcodesize text width change
    private void changeMaxWidth(Configuration config) {
        int maxpixels = 0;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            maxpixels = (int)(TEXTVIEW_MAX_WIDTH * 2 * getResources().getDisplayMetrics().density);
            mExternalCodeSizePrefix.setMaxWidth(maxpixels);
        } else {
            maxpixels = (int)(TEXTVIEW_MAX_WIDTH * getResources().getDisplayMetrics().density);
            mExternalCodeSizePrefix.setMaxWidth(maxpixels);
        }
        Log.d(TAG, "onConfigurationChanged: " + config.orientation + " maxpixels: " + maxpixels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = mRootView = inflater.inflate(R.layout.installed_app_details, container, false);

        final ViewGroup allDetails = (ViewGroup)view.findViewById(R.id.all_details);
        Utils.forceCustomPadding(allDetails, true /* additive padding */);

        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)view.getLayoutParams()).removeBorders = true;
        }

        mComputingStr = getActivity().getText(R.string.computing_size);

        // Set default values on sizes
        mTotalSize = (TextView)view.findViewById(R.id.total_size_text);
        mAppSize = (TextView)view.findViewById(R.id.application_size_text);
        mDataSize = (TextView)view.findViewById(R.id.data_size_text);
        mExternalCodeSize = (TextView)view.findViewById(R.id.external_code_size_text);
        mExternalDataSize = (TextView)view.findViewById(R.id.external_data_size_text);
        mExternalCodeSizePrefix = (TextView)view.findViewById(R.id.external_code_size_prefix); // yonguk.kim 20120816 ICS Stuff Porting to JB - Care string length

        TextView mExternalDataSizePrefix = (TextView)view
                .findViewById(R.id.external_data_size_prefix);

        mExternalCodeSizePrefix.setText(getString(R.string.settings_apps_installed_appsize_sd));
        mExternalDataSizePrefix.setText(getString(R.string.settings_apps_installed_datasize_sd));

        if (!Utils.isLGExternalInfoSupport(getActivity())) { //if (Environment.isExternalStorageEmulated() && !Utils.iSLGExternalInfoSupport()) {
            ((View)mExternalCodeSize.getParent()).setVisibility(View.GONE);
            ((View)mExternalDataSize.getParent()).setVisibility(View.GONE);
        }

        // Get Control button panel
        View btnPanel = view.findViewById(R.id.control_buttons_panel);
        mForceStopButton = (Button)btnPanel.findViewById(R.id.left_button);
        mForceStopButton.setText(R.string.force_stop);
        mUninstallButton = (Button)btnPanel.findViewById(R.id.right_button);
        mForceStopButton.setEnabled(false);

        // Get More Control button panel
        mMoreControlButtons = view.findViewById(R.id.more_control_buttons_panel);
        mMoreControlButtons.findViewById(R.id.left_button).setVisibility(View.INVISIBLE);
        mSpecialDisableButton = (Button)mMoreControlButtons.findViewById(R.id.right_button);
        mMoreControlButtons.setVisibility(View.GONE);

        // Initialize clear data and move install location buttons
        View data_buttons_panel = view.findViewById(R.id.data_buttons_panel);
        mClearDataButton = (Button)data_buttons_panel.findViewById(R.id.right_button);
        mMoveAppButton = (Button)data_buttons_panel.findViewById(R.id.left_button);

        // Cache section
        mCacheSize = (TextView)view.findViewById(R.id.cache_size_text);
        mClearCacheButton = (Button)view.findViewById(R.id.clear_cache_button);

        mActivitiesButton = (Button)view.findViewById(R.id.clear_activities_button);

        // Screen compatibility control
        mScreenCompatSection = view.findViewById(R.id.screen_compatibility_section);
        mAskCompatibilityCB = (CheckBox)view.findViewById(R.id.ask_compatibility_cb);
        mEnableCompatibilityCB = (CheckBox)view.findViewById(R.id.enable_compatibility_cb);

        mNotificationSwitch = (CompoundButton)view.findViewById(R.id.notification_switch);

        changeMaxWidth(getResources().getConfiguration());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, UNINSTALL_ALL_USERS_MENU, 1, R.string.uninstall_all_users_text)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean showIt = true;
        if (mUpdatedSysApp) {
            showIt = false;
        } else if (mAppEntry == null) {
            showIt = false;
        } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            showIt = false;
        } else if (mPackageInfo == null || mDpm.packageHasActiveAdmins(mPackageInfo.packageName)) {
            showIt = false;
        } else if (UserHandle.myUserId() != 0) {
            showIt = false;
        } else if (mUserManager.getUsers().size() < 2) {
            showIt = false;
        }
        menu.findItem(UNINSTALL_ALL_USERS_MENU).setVisible(showIt);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == UNINSTALL_ALL_USERS_MENU) {
            uninstallPkg(mAppEntry.info.packageName, true, false);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL) {
            if (mDisableAfterUninstall) {
                mDisableAfterUninstall = false;
                try {
                    ApplicationInfo ainfo = getActivity().getPackageManager().getApplicationInfo(
                            mAppEntry.info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES
                                    | PackageManager.GET_DISABLED_COMPONENTS);
                    if ((ainfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                        new DisableChanger(this, mAppEntry.info,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                .execute((Object)null);
                    }
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "NameNotFoundException");
                    return;
                }
            }
            if (!refreshUi()) {
                Log.d(TAG, "!refreshUi()");
                setIntentAndFinish(true, true);
            }
        } else {
            Log.d(TAG, "requestCode = " + requestCode);
        }
    }

    // Utility method to set applicaiton label and icon.
    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        View appSnippet = mRootView.findViewById(R.id.app_snippet);
        ImageView icon = (ImageView)appSnippet.findViewById(R.id.app_icon);
        mState.ensureIcon(mAppEntry);
        icon.setImageDrawable(mAppEntry.icon);
        // Set application name.
        TextView label = (TextView)appSnippet.findViewById(R.id.app_name);
        if (!Utils.isUI_4_1_model(getActivity())) {
            label.setTypeface(null, Typeface.BOLD);
        }
        label.setText(mAppEntry.label);
        // Version number of application
        mAppVersion = (TextView)appSnippet.findViewById(R.id.app_size);

        if (pkgInfo != null && pkgInfo.versionName != null) {
            mAppVersion.setVisibility(View.VISIBLE);
            mAppVersion.setText(getActivity().getString(R.string.version_text,
                    String.valueOf(pkgInfo.versionName)));
        } else {
            mAppVersion.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mAppControlRestricted = mUserManager.hasUserRestriction(UserManager.DISALLOW_APPS_CONTROL);
        mSession.resume(false);
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSession.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSession.release();
    }

    @Override
    public void onAllSizesComputed() {
    }

    @Override
    public void onPackageIconChanged() {
    }

    @Override
    public void onPackageListChanged() {
        refreshUi();
    }

    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps) {
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
        // yonguk.kim 20120816 ICS Stuff Porting to JB - Add Null Check Routine
        if (packageName == null || mAppEntry == null || mAppEntry.info == null
                || mAppEntry.info.packageName == null) {
            return;
        }

        if (packageName.equals(mAppEntry.info.packageName)) {
            refreshSizeInfo();
        }
    }

    @Override
    public void onRunningStateChanged(boolean running) {
    }

    private String retrieveAppEntry() {
        final Bundle args = getArguments();
        String packageName = (args != null) ? args.getString(ARG_PACKAGE_NAME) : null;
        if (packageName == null) {
            Intent intent = (args == null) ?
                    getActivity().getIntent() : (Intent)args.getParcelable("intent");
            if (intent != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        // rebestm - WBT
        if (packageName != null) {
            mAppEntry = mState.getEntry(packageName);
        }
        if (mAppEntry != null) {
            // Get application info again to refresh changed properties of application
            try {
                mPackageInfo = mPm.getPackageInfo(mAppEntry.info.packageName,
                        PackageManager.GET_DISABLED_COMPONENTS |
                                PackageManager.GET_UNINSTALLED_PACKAGES |
                                PackageManager.GET_SIGNATURES);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Exception when retrieving package:" + mAppEntry.info.packageName, e);
            }
        } else {
            Log.w(TAG, "Missing AppEntry; maybe reinstalling?");
            mPackageInfo = null;
        }

        return packageName;
    }

    private boolean signaturesMatch(String pkg1, String pkg2) {
        if (pkg1 != null && pkg2 != null) {
            try {
                final int match = mPm.checkSignatures(pkg1, pkg2);
                if (match >= PackageManager.SIGNATURE_MATCH) {
                    return true;
                }
            } catch (Exception e) {
                // e.g. named alternate package not found during lookup;
                // this is an expected case sometimes
                Log.d(TAG, "signaturesMatch Exception");
                return false;
            }
        }
        return false;
    }

    private boolean refreshUi() {
        if (getActivity() == null) {
            Log.d(TAG, "context is null -> return");
            return false;
        }
        if (mMoveInProgress) {
            return true;
        }
        final String packageName = retrieveAppEntry();

        //        Log.i("kkk" , "packageName = " + packageName);

        if (mAppEntry == null) {
            return false; // onCreate must have failed, make sure to exit
        }

        if (mPackageInfo == null) {
            return false; // onCreate must have failed, make sure to exit
        }

        // Get list of "home" apps and trace through any meta-data references
        List<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
        mPm.getHomeActivities(homeActivities);
        mHomePackages.clear();
        for (int i = 0; i < homeActivities.size(); i++) {
            ResolveInfo ri = homeActivities.get(i);
            final String activityPkg = ri.activityInfo.packageName;
            mHomePackages.add(activityPkg);

            // Also make sure to include anything proxying for the home app
            final Bundle metadata = ri.activityInfo.metaData;
            if (metadata != null) {
                final String metaPkg = metadata.getString(ActivityManager.META_HOME_ALTERNATE);
                if (signaturesMatch(metaPkg, activityPkg)) {
                    mHomePackages.add(metaPkg);
                }
            }
        }

        // Get list of preferred activities
        List<ComponentName> prefActList = new ArrayList<ComponentName>();

        // Intent list cannot be null. so pass empty list
        List<IntentFilter> intentList = new ArrayList<IntentFilter>();
        mPm.getPreferredActivities(intentList, prefActList, packageName);
        if (localLOGV) {
            Log.i(TAG, "Have " + prefActList.size() + " number of activities in preferred list");
        }
        boolean hasUsbDefaults = false;
        try {
            hasUsbDefaults = mUsbManager.hasDefaults(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "mUsbManager.hasDefaults", e);
        }
        boolean hasBindAppWidgetPermission =
                mAppWidgetManager.hasBindAppWidgetPermission(mAppEntry.info.packageName);

        TextView autoLaunchTitleView = (TextView)mRootView.findViewById(R.id.auto_launch_title);
        TextView autoLaunchView = (TextView)mRootView.findViewById(R.id.auto_launch);
        boolean autoLaunchEnabled = prefActList.size() > 0 || hasUsbDefaults;
        if (!autoLaunchEnabled && !hasBindAppWidgetPermission) {
            resetLaunchDefaultsUi(autoLaunchTitleView, autoLaunchView);
        } else {
            boolean useBullets = hasBindAppWidgetPermission && autoLaunchEnabled;

            if (hasBindAppWidgetPermission) {
                autoLaunchTitleView.setText(R.string.auto_launch_label_generic);
            } else {
                autoLaunchTitleView.setText(R.string.auto_launch_label);
            }

            CharSequence text = null;
            int bulletIndent = getResources()
                    .getDimensionPixelSize(R.dimen.installed_app_details_bullet_offset);
            if (autoLaunchEnabled) {
                CharSequence autoLaunchEnableText = getText(R.string.auto_launch_enable_text);
                SpannableString s = new SpannableString(autoLaunchEnableText);
                if (useBullets) {
                    s.setSpan(new BulletSpan(bulletIndent), 0, autoLaunchEnableText.length(), 0);
                }
                // rebestm - WBT
                text = TextUtils.concat(s, "\n");
            }
            if (hasBindAppWidgetPermission) {
                CharSequence alwaysAllowBindAppWidgetsText =
                        getText(R.string.always_allow_bind_appwidgets_text);
                SpannableString s = new SpannableString(alwaysAllowBindAppWidgetsText);
                if (useBullets) {
                    s.setSpan(new BulletSpan(bulletIndent),
                            0, alwaysAllowBindAppWidgetsText.length(), 0);
                }
                text = (text == null) ?
                        TextUtils.concat(s, "\n") : TextUtils.concat(text, "\n", s, "\n");
            }
            autoLaunchView.setText(text);
            if (PACKAGE_SETTINGS.equals(packageName)) {
                mActivitiesButton.setEnabled(false);
            } else {
                mActivitiesButton.setEnabled(true);
                mActivitiesButton.setOnClickListener(this);
            }
        }

        // Screen compatibility section.
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        int compatMode = am.getPackageScreenCompatMode(packageName);
        // For now these are always off; this is the old UI model which we
        // are no longer using.
        if (false && (compatMode == ActivityManager.COMPAT_MODE_DISABLED
        || compatMode == ActivityManager.COMPAT_MODE_ENABLED)) {
            mScreenCompatSection.setVisibility(View.VISIBLE);
            mAskCompatibilityCB.setChecked(am.getPackageAskScreenCompat(packageName));
            mAskCompatibilityCB.setOnCheckedChangeListener(this);
            mEnableCompatibilityCB.setChecked(compatMode == ActivityManager.COMPAT_MODE_ENABLED);
            mEnableCompatibilityCB.setOnCheckedChangeListener(this);
        } else {
            mScreenCompatSection.setVisibility(View.GONE);
        }

        // Security permissions section
        LinearLayout permsView = (LinearLayout)mRootView.findViewById(R.id.permissions_section);
        AppSecurityPermissions asp = new AppSecurityPermissions(getActivity(), packageName);
        int premiumSmsPermission = getPremiumSmsPermission(packageName);
        // Premium SMS permission implies the app also has SEND_SMS permission, so the original
        // application permissions list doesn't have to be shown/hidden separately. The premium
        // SMS subsection should only be visible if the app has tried to send to a premium SMS.
        if (asp.getPermissionCount() > 0
                || premiumSmsPermission != SmsUsageMonitor.PREMIUM_SMS_PERMISSION_UNKNOWN) {
            permsView.setVisibility(View.VISIBLE);
        } else {
            permsView.setVisibility(View.GONE);
        }
        // Premium SMS permission subsection
        TextView securityBillingDesc = (TextView)permsView.findViewById(
                R.id.security_settings_billing_desc);
        LinearLayout securityBillingList = (LinearLayout)permsView.findViewById(
                R.id.security_settings_billing_list);
        if (premiumSmsPermission != SmsUsageMonitor.PREMIUM_SMS_PERMISSION_UNKNOWN) {
            // Show the premium SMS permission selector
            securityBillingDesc.setVisibility(View.VISIBLE);
            securityBillingList.setVisibility(View.VISIBLE);
            Spinner spinner = (Spinner)permsView.findViewById(
                    R.id.security_settings_premium_sms_list);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.security_settings_premium_sms_values,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            // List items are in the same order as SmsUsageMonitor constants, offset by 1.
            spinner.setSelection(premiumSmsPermission - 1);
            spinner.setOnItemSelectedListener(new PremiumSmsSelectionListener(
                    packageName, mSmsManager));
        } else {
            // Hide the premium SMS permission selector
            securityBillingDesc.setVisibility(View.GONE);
            securityBillingList.setVisibility(View.GONE);
        }
        // App permissions subsection
        if (asp.getPermissionCount() > 0) {
            // Make the security sections header visible
            LinearLayout securityList = (LinearLayout)permsView.findViewById(
                    R.id.security_settings_list);
            securityList.removeAllViews();
            securityList.addView(asp.getPermissionsView());
            // If this app is running under a shared user ID with other apps,
            // update the description to explain this.
            String[] packages = mPm.getPackagesForUid(mPackageInfo.applicationInfo.uid);
            if (packages != null && packages.length > 1) {
                ArrayList<CharSequence> pnames = new ArrayList<CharSequence>();
                for (int i = 0; i < packages.length; i++) {
                    String pkg = packages[i];
                    if (mPackageInfo.packageName.equals(pkg)) {
                        continue;
                    }
                    try {
                        ApplicationInfo ainfo = mPm.getApplicationInfo(pkg, 0);
                        pnames.add(ainfo.loadLabel(mPm));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, "PackageManager.NameNotFoundException");
                    }
                }
                final int N = pnames.size();
                if (N > 0) {
                    final Resources res = getActivity().getResources();
                    String appListStr;
                    if (N == 1) {
                        appListStr = pnames.get(0).toString();
                    } else if (N == 2) {
                        appListStr = res.getString(R.string.join_two_items, pnames.get(0),
                                pnames.get(1));
                    } else {
                        appListStr = pnames.get(N - 2).toString();
                        for (int i = N - 3; i >= 0; i--) {
                            appListStr = res.getString(i == 0 ? R.string.join_many_items_first
                                    : R.string.join_many_items_middle, pnames.get(i), appListStr);
                        }
                        appListStr = res.getString(R.string.join_many_items_last,
                                appListStr, pnames.get(N - 1));
                    }
                    TextView descr = (TextView)mRootView.findViewById(
                            R.id.security_settings_desc);
                    descr.setText(res.getString(R.string.security_settings_desc_multi,
                            mPackageInfo.applicationInfo.loadLabel(mPm), appListStr));
                }
            }
        } else {
            permsView.setVisibility(View.GONE);
        }

        checkForceStop();
        setAppLabelAndIcon(mPackageInfo);
        refreshButtons();
        refreshSizeInfo();
        refreshNotiText();

        if (!mInitialized) {
            // First time init: are we displaying an uninstalled app?
            mInitialized = true;
            mShowUninstalled = (mAppEntry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0;
        } else {
            // All other times: if the app no longer exists then we want
            // to go away.
            try {
                ApplicationInfo ainfo = getActivity().getPackageManager().getApplicationInfo(
                        mAppEntry.info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES
                                | PackageManager.GET_DISABLED_COMPONENTS);
                if (!mShowUninstalled) {
                    // If we did not start out with the app uninstalled, then
                    // it transitioning to the uninstalled state for the current
                    // user means we should go away as well.
                    return (ainfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    private static class PremiumSmsSelectionListener implements AdapterView.OnItemSelectedListener {
        private final String mPackageName;
        private final ISms mSmsManager;

        PremiumSmsSelectionListener(String packageName, ISms smsManager) {
            mPackageName = packageName;
            mSmsManager = smsManager;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position,
                long id) {
            if (position >= 0 && position < 3) {
                Log.d(TAG, "Selected premium SMS policy " + position);
                setPremiumSmsPermission(mPackageName, (position + 1));
            } else {
                Log.e(TAG, "Error: unknown premium SMS policy " + position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Ignored
        }

        private void setPremiumSmsPermission(String packageName, int permission) {
            try {
                if (mSmsManager != null) {
                    mSmsManager.setPremiumSmsPermission(packageName, permission);
                }
            } catch (RemoteException ex) {
                Log.w(TAG, "RemoteException");
            }
        }
    }

    private void resetLaunchDefaultsUi(TextView title, TextView autoLaunchView) {
        title.setText(R.string.auto_launch_label);
        autoLaunchView.setText(R.string.auto_launch_disable_text);
        // Disable clear activities button
        mActivitiesButton.setEnabled(false);
    }

    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        if (localLOGV) {
            Log.i(TAG, "appChanged=" + appChanged);
        }
        Intent intent = new Intent();
        intent.putExtra(ManageApplications.APP_CHG, appChanged);
        PreferenceActivity pa = (PreferenceActivity)getActivity();
        pa.finishPreferencePanel(this, Activity.RESULT_OK, intent);
    }

    private void refreshSizeInfo() {
        if (mAppEntry.size == ApplicationsState.SIZE_INVALID
                || mAppEntry.size == ApplicationsState.SIZE_UNKNOWN) {
            mLastCodeSize = mLastDataSize = mLastCacheSize = mLastTotalSize = -1;
            if (!mHaveSizes) {
                mAppSize.setText(Utils.onLGFormatter(mComputingStr.toString()));
                mDataSize.setText(Utils.onLGFormatter(mComputingStr.toString()));
                mCacheSize.setText(Utils.onLGFormatter(mComputingStr.toString()));
                mTotalSize.setText(Utils.onLGFormatter(mComputingStr.toString()));
            }
            //yonguk.kim 20120816 ICS Stuff Porting to JB - Clear Data button twinkle after disalbe/enable
            //mClearDataButton.setEnabled(false);
            //mClearCacheButton.setEnabled(false);
        } else {
            mHaveSizes = true;
            long codeSize = mAppEntry.codeSize;
            long dataSize = mAppEntry.dataSize;
            if (!Utils.isLGExternalInfoSupport(getActivity())) { //if (Environment.isExternalStorageEmulated() && !Utils.iSLGExternalInfoSupport()) {
                codeSize += mAppEntry.externalCodeSize;
                dataSize += mAppEntry.externalDataSize;
            } else {
                if (mLastExternalCodeSize != mAppEntry.externalCodeSize) {
                    mLastExternalCodeSize = mAppEntry.externalCodeSize;
                    mExternalCodeSize
                            .setText(Utils.onLGFormatter(getSizeStr(mAppEntry.externalCodeSize)));
                }
                if (mLastExternalDataSize != mAppEntry.externalDataSize) {
                    mLastExternalDataSize = mAppEntry.externalDataSize;
                    mExternalDataSize
                            .setText(Utils.onLGFormatter(getSizeStr(mAppEntry.externalDataSize)));
                }
            }
            if (mLastCodeSize != codeSize) {
                mLastCodeSize = codeSize;
                mAppSize.setText(Utils.onLGFormatter(getSizeStr(codeSize)));
            }
            if (mLastDataSize != dataSize) {
                mLastDataSize = dataSize;
                mDataSize.setText(Utils.onLGFormatter(getSizeStr(dataSize)));
            }
            long cacheSize = mAppEntry.cacheSize + mAppEntry.externalCacheSize;
            if (mLastCacheSize != cacheSize) {
                mLastCacheSize = cacheSize;
                mCacheSize.setText(Utils.onLGFormatter(getSizeStr(cacheSize)));
            }
            if (mLastTotalSize != mAppEntry.size) {
                mLastTotalSize = mAppEntry.size;
                mTotalSize.setText(Utils.onLGFormatter(getSizeStr(mAppEntry.size)));
            }

            if ((mAppEntry.dataSize + mAppEntry.externalDataSize) <= 0 || !mCanClearData) {
                Log.d(TAG, "Date clear block222");
                mClearDataButton.setEnabled(false);
            } else {
                mClearDataButton.setEnabled(true);
                mClearDataButton.setOnClickListener(this);
            }
            // rebestm 20131011 requested by LGU(Awifi070)
            if (cacheSize <= 0
                    || mPackageInfo.packageName.equals(PACKAGE_HOMEBOY)
                    || mPackageInfo.packageName.equals(PACKAGE_HOMEBOY2)) {
                mClearCacheButton.setEnabled(false);
            } else {
                mClearCacheButton.setEnabled(true);
                mClearCacheButton.setOnClickListener(this);
            }
        }
        if (mAppControlRestricted) {
            mClearCacheButton.setEnabled(false);
            mClearDataButton.setEnabled(false);
        }
    }

    /*
     * Private method to handle clear message notification from observer when
     * the async operation from PackageManager is complete
     */
    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppEntry.info.packageName;
        mClearDataButton.setText(R.string.clear_user_data_text);
        if (result == OP_SUCCESSFUL) {
            Log.i(TAG, "Cleared user data for package : " + packageName);
            mState.requestSize(mAppEntry.info.packageName);
        } else {
            mClearDataButton.setEnabled(true);
        }
        checkForceStop();
        // yonguk.kim 20120816 ICS Stuff Porting to JB - Send broadcast intent(CLEAR_DATA) for basic package.
        if (packageName != null) {
            Intent intent = new Intent("com.android.settings.CLEAR_DATA");
            intent.setPackage(packageName);
            getActivity().sendBroadcast(intent);
        }
    }

    private void refreshButtons() {
        if (!mMoveInProgress) {
            initUninstallButtons();
            initDataButtons();
            initMoveButton();
            initNotificationButton();
        } else {
            mMoveAppButton.setText(R.string.moving);
            mMoveAppButton.setEnabled(false);
            mUninstallButton.setEnabled(false);
            mSpecialDisableButton.setEnabled(false);
        }
    }

    private void processMoveMsg(Message msg) {
        int result = msg.arg1;
        String packageName = mAppEntry.info.packageName;
        // Refresh the button attributes.
        mMoveInProgress = false;

        if (result == PackageManager.MOVE_SUCCEEDED) {
            Log.i(TAG, "Moved resources for " + packageName);
            // Refresh size information again.
            mState.requestSize(mAppEntry.info.packageName);
        } else {
            showDialogInner(DLG_MOVE_FAILED, result);
        }
        refreshUi();
    }

    public void MatrixTime(int delayTime) {
        long saveTime = System.currentTimeMillis();
        long currTime = 0;
        while (currTime - saveTime < delayTime) {
            currTime = System.currentTimeMillis();
        }
    }

    /*
     * Private method to initiate clearing user data when the user clicks the clear data
     * button for a system package
     */
    private void initiateClearUserData() {
        mClearDataButton.setEnabled(false);
        // Invoke uninstall or clear user data based on sysPackage
        String packageName = mAppEntry.info.packageName;
        Log.i(TAG, "Clearing user data for package : " + packageName);
        if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if (!res) {
            // Clearing data failed for some obscure reason. Just log error for now
            Log.i(TAG, "Couldnt clear application user data for package:" + packageName);
            showDialogInner(DLG_CANNOT_CLEAR_DATA, 0);
        } else {
            mClearDataButton.setText(R.string.recompute_size);
        }
    }

    private void showDialogInner(int id, int moveErrorCode) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, moveErrorCode);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id, int moveErrorCode) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putInt("moveError", moveErrorCode);
            frag.setArguments(args);
            return frag;
        }

        InstalledAppDetails getOwner() {
            return (InstalledAppDetails)getTargetFragment();
        }

        // [S]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
        @Override
        public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub
            super.onDismiss(dialog);
            getOwner().notUserChange = false;
        }

        // [E]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            int moveErrorCode = getArguments().getInt("moveError");
            AlertDialog dialog = null;
            switch (id) {
            case DLG_CLEAR_DATA:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.clear_data_dlg_title))
                        .setMessage(getActivity().getText(R.string.clear_data_dlg_text))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Clear user data here
                                        getOwner().initiateClearUserData();
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_FACTORY_RESET:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.app_factory_reset_dlg_title))
                        .setMessage(getActivity().getText(R.string.app_factory_reset_dlg_text))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Clear user data here
                                        getOwner().uninstallPkg(
                                                getOwner().mAppEntry.info.packageName,
                                                false, false);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_FACTORY_RESET_VAPP:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.uninstall_text))
                        .setMessage(
                                getActivity().getText(R.string.ask_uninstall_app_widget_message))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Clear user data here
                                        getOwner().uninstallPkg(
                                                getOwner().mAppEntry.info.packageName,
                                                false, false);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_APP_NOT_FOUND:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.app_not_found_dlg_title))
                        .setMessage(getActivity().getText(R.string.app_not_found_dlg_title))
                        .setNeutralButton(getActivity().getText(R.string.dlg_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //force to recompute changed value
                                        getOwner().setIntentAndFinish(true, true);
                                    }
                                })
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_CANNOT_CLEAR_DATA:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.clear_failed_dlg_title))
                        .setMessage(getActivity().getText(R.string.clear_failed_dlg_text))
                        .setNeutralButton(R.string.dlg_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        getOwner().mClearDataButton.setEnabled(false);
                                        //force to recompute changed value
                                        getOwner().setIntentAndFinish(false, false);
                                    }
                                })
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_FORCE_STOP:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.force_stop_dlg_title))
                        .setMessage(getActivity().getText(R.string.force_stop_dlg_text))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Force stop
                                        getOwner().forceStopPackage(
                                                getOwner().mAppEntry.info.packageName);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_MOVE_FAILED:
                CharSequence msg = getActivity().getString(R.string.move_app_failed_dlg_text,
                        getOwner().getMoveErrMsg(moveErrorCode));
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.move_app_failed_dlg_title))
                        .setMessage(msg)
                        .setNeutralButton(R.string.dlg_ok, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_DISABLE:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.app_disable_dlg_title_ex))
                        .setMessage(getActivity()
                                .getText(R.string.sp_App_Disable_DLG_Text2_NORMAL_public2_ex))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Disable the app
                                        new DisableChanger(
                                                getOwner(),
                                                getOwner().mAppEntry.info,
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                                .execute((Object)null);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_DISABLE_VAPP:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.disable_text))
                        .setMessage(getActivity().getText(R.string.ask_disable_app_message))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Disable the app
                                        new DisableChanger(
                                                getOwner(),
                                                getOwner().mAppEntry.info,
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
                                                .execute((Object)null);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_DISABLE_NOTIFICATIONS:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(
                                getActivity().getText(R.string.app_disable_notifications_dlg_title))
                        .setMessage(
                                getActivity().getText(R.string.app_disable_notifications_dlg_text))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Disable the package's notifications
                                        getOwner().notUserChange = true;
                                        getOwner().setNotificationsEnabled(false);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Re-enable the checkbox
                                        getOwner().mNotificationSwitch.setChecked(true);
                                    }
                                })
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            case DLG_SPECIAL_DISABLE:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getText(R.string.app_special_disable_dlg_title))
                        .setMessage(getActivity().getText(R.string.app_special_disable_dlg_text))
                        .setPositiveButton(R.string.def_yes_btn_caption,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Clear user data here
                                        getOwner().uninstallPkg(
                                                getOwner().mAppEntry.info.packageName,
                                                false, true);
                                    }
                                })
                        .setNegativeButton(R.string.def_no_btn_caption, null)
                        .create();
                if (!Utils.isUI_4_1_model(getActivity())) {
                    dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                }
                return dialog;
            default:
                break;
            }
            throw new IllegalArgumentException("unknown id " + id);
        }
    }

    private void uninstallPkg(String packageName, boolean allUsers, boolean andDisable) {
        // Create new intent to launch Uninstaller activity
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
        uninstallIntent.putExtra(Intent.EXTRA_UNINSTALL_ALL_USERS, allUsers);
        startActivityForResult(uninstallIntent, REQUEST_UNINSTALL);
        mDisableAfterUninstall = andDisable;
    }

    private void forceStopPackage(String pkgName) {
        ActivityManager am = (ActivityManager)getActivity().getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage(pkgName);
        mState.invalidatePackage(pkgName);
        ApplicationsState.AppEntry newEnt = mState.getEntry(pkgName);
        if (newEnt != null) {
            mAppEntry = newEnt;
        }
        // yonguk.kim 20120816 ICS Stuff Porting to JB - Send Intent for Music Stop requested by System UI [START]
        if (pkgName.equals("com.lge.music")) {
            Log.i(TAG, "stop SystemUI Music controller");
            getActivity().sendBroadcast(new Intent("com.lge.music.saveNoDisplay"));
        }
        // yonguk.kim 20120816 ICS Stuff Porting to JB - Send Intent for Music Stop requested by System UI [END]
        checkForceStop();
    }

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateForceStopButton(getResultCode() != Activity.RESULT_CANCELED);
        }
    };

    private void updateForceStopButton(boolean enabled) {
        if (mAppControlRestricted) {
            mForceStopButton.setEnabled(false);
        } else {
            mForceStopButton.setEnabled(enabled);
            mForceStopButton.setOnClickListener(InstalledAppDetails.this);
        }
    }

    private void checkForceStop() {
        if (mDpm.packageHasActiveAdmins(mPackageInfo.packageName)
                || checkPackageNameForForceStop(mPackageInfo.packageName)) {
            // User can't force stop device admin.
            Log.d(TAG, "force stop block");
            updateForceStopButton(false);
        } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
            // If the app isn't explicitly stopped, then always show the
            // force stop button.
            updateForceStopButton(true);
        } else {
            Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                    Uri.fromParts("package", mAppEntry.info.packageName, null));
            intent.putExtra(Intent.EXTRA_PACKAGES, new String[] { mAppEntry.info.packageName });
            intent.putExtra(Intent.EXTRA_UID, mAppEntry.info.uid);
            intent.putExtra(Intent.EXTRA_USER_HANDLE, UserHandle.getUserId(mAppEntry.info.uid));
            // rebestm - kk migration
            getActivity().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null,
                    mCheckKillProcessesReceiver, null, Activity.RESULT_CANCELED, null, null);
        }
    }

    static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final PackageManager mPm;
        final WeakReference<InstalledAppDetails> mActivity;
        final ApplicationInfo mInfo;
        final int mState;

        DisableChanger(InstalledAppDetails activity, ApplicationInfo info, int state) {
            mPm = activity.mPm;
            mActivity = new WeakReference<InstalledAppDetails>(activity);
            mInfo = info;
            mState = state;
        }

        @Override
        protected Object doInBackground(Object... params) {
            InstalledAppDetails details = mActivity.get();
            if (mInfo.packageName.equals("com.lge.music")) {
                if (mState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                    Log.i(TAG, "disable SystemUI Music controller");
                    if (details != null) {
                        details.getActivity().sendBroadcast(
                                new Intent("com.lge.music.saveNoDisplay"));
                    }
                }
                if (details != null
                        && Utils.checkPackage(details.getActivity(),
                                "com.lge.sizechangable.musicwidget.widget")) {
                    mPm.setApplicationEnabledSetting("com.lge.sizechangable.musicwidget.widget",
                            mState, 0);
                }
            } else if (mInfo.packageName.equals("com.lge.fmradio")) {
                if (mState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                    if (details != null) {
                        details.getActivity().sendBroadcast(
                                new Intent("com.lge.fmradio.saveNoDisplay"));
                    }
                }
            }

            if (mInfo.packageName.equals("com.lge.sizechangable.musicwidget.widget")) {
                if (mState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                    mPm.setApplicationEnabledSetting("com.lge.music", mState, 0);
                }
            }
            //[S][2013.06.28][jaeyoon.hyun]App disable both com.lge.lifestream and com.lge.cic.polaris.logmanager request by donghoon.lee
            if (mInfo.packageName.equals("com.lge.lifestream")) {
                if (details != null
                        && Utils.checkPackage(details.getActivity(),
                                "com.lge.cic.polaris.logmanager")) {
                    mPm.setApplicationEnabledSetting("com.lge.cic.polaris.logmanager", mState, 0);
                }
            }

            if (mInfo.packageName.equals("com.lge.cic.polaris.logmanager")) {
                if (details != null
                        && Utils.checkPackage(details.getActivity(), "com.lge.lifestream")) {
                    mPm.setApplicationEnabledSetting("com.lge.lifestream", mState, 0);
                }
            }
            //[E][2013.06.28][jaeyoon.hyun]App disable both com.lge.lifestream and com.lge.cic.polaris.logmanager request by donghoon.lee

            mPm.setApplicationEnabledSetting(mInfo.packageName, mState, 0);
            // yonguk.kim 20121212 DB modification for HotKeyCustomization
            checkHotKeyCustomization(mInfo.packageName);
            return null;
        }

        private void checkHotKeyCustomization(String deletePackageName) {
            final String HOTKEY_SHORT_PACKAGE = "hotkey_short_package";
            final String HOTKEY_SHORT_CLASS = "hotkey_short_class";
            final String HOTKEY_LONG_PACKAGE = "hotkey_long_package";
            final String HOTKEY_LONG_CLASS = "hotkey_long_class";
            final String DEFAULT_PACKAGE = "com.lge.QuickClip";
            final String DEFAULT_CLASS = "com.lge.QuickClip.QuickClipActivity";
            final String HOTKEY_NONE = "none";
            InstalledAppDetails details = mActivity.get();

            if (details == null) {
                Log.d(TAG, "checkHotKeyCustomization, details == null");
                return;
            }

            ContentResolver cr = details.getActivity().getContentResolver();

            if (cr == null) {
                Log.d(TAG, "checkHotKeyCustomization, cr == null");
                return;
            }

            if (!Utils.supportHotkey(details.getActivity())) {
                return;
            }

            String hotkey_short_package = Settings.System.getString(cr, HOTKEY_SHORT_PACKAGE);
            String hotkey_long_package = Settings.System.getString(cr, HOTKEY_LONG_PACKAGE);

            if (hotkey_short_package == null || hotkey_long_package == null) {
                return;
            }

            if (hotkey_short_package.equals(deletePackageName)) {
                Log.d(TAG, "Short key db change to default from " + deletePackageName);
                Settings.System.putString(cr, HOTKEY_SHORT_PACKAGE, DEFAULT_PACKAGE);
                Settings.System.putString(cr, HOTKEY_SHORT_CLASS, DEFAULT_CLASS);
            }

            if (hotkey_long_package.equals(deletePackageName)) {
                Log.d(TAG, "Long key db change to default from" + deletePackageName);
                Settings.System.putString(cr, HOTKEY_LONG_PACKAGE, HOTKEY_NONE);
                Settings.System.putString(cr, HOTKEY_LONG_CLASS, HOTKEY_NONE);
            }
        }
    }

    private void setNotificationsEnabled(boolean enabled) {
        String packageName = mAppEntry.info.packageName;
        // 3LM_MDM_DCM jihun.im@lge.com
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (mAppEntry.info.packageName.startsWith("com.threelm.dm")) {
                mNotificationSwitch.setChecked(true);
                return;
            }
        }
        INotificationManager nm = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        // rebestm - kk migration
        try {
            // [S]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
            mNotificationSwitch.setChecked(enabled);
            //final boolean enable = mNotificationSwitch.isChecked();
            // [E]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
            nm.setNotificationsEnabledForPackage(packageName, mAppEntry.info.uid, enabled);
        } catch (android.os.RemoteException ex) {
            mNotificationSwitch.setChecked(!enabled); // revert
        }

    }

    private int getPremiumSmsPermission(String packageName) {
        try {
            if (mSmsManager != null) {
                return mSmsManager.getPremiumSmsPermission(packageName);
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
        return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_UNKNOWN;
    }

    /*
     * Method implementing functionality of buttons clicked
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        String packageName = mAppEntry.info.packageName;
        if (v == mUninstallButton) {
            if (mUpdatedSysApp) {
                if (Config.getOperator().equals(Config.ATT)
                        && checkPackageNameForVapp(packageName)) {
                    showDialogInner(DLG_FACTORY_RESET_VAPP, 0);
                } else {
                    showDialogInner(DLG_FACTORY_RESET, 0);
                }

            } else {
                if ((mAppEntry.info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    if (mAppEntry.info.enabled) {
                        if (Config.getOperator().equals(Config.ATT)
                                && checkPackageNameForVapp(packageName)) {
                            showDialogInner(DLG_DISABLE_VAPP, 0);
                        } else {
                            showDialogInner(DLG_DISABLE, 0);
                        }
                    } else {
                        new DisableChanger(this, mAppEntry.info,
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                                .execute((Object)null);
                    }
                } else if ((mAppEntry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                    uninstallPkg(packageName, true, false);
                } else {
                    uninstallPkg(packageName, false, false);
                }
            }
        } else if (v == mSpecialDisableButton) {
            showDialogInner(DLG_SPECIAL_DISABLE, 0);
        } else if (v == mActivitiesButton) {
            mPm.clearPackagePreferredActivities(packageName);
            try {
                mUsbManager.clearDefaults(packageName, UserHandle.myUserId());
            } catch (RemoteException e) {
                Log.e(TAG, "mUsbManager.clearDefaults", e);
            }
            mAppWidgetManager.setBindAppWidgetPermission(packageName, false);
            TextView autoLaunchTitleView =
                    (TextView)mRootView.findViewById(R.id.auto_launch_title);
            TextView autoLaunchView = (TextView)mRootView.findViewById(R.id.auto_launch);
            resetLaunchDefaultsUi(autoLaunchTitleView, autoLaunchView);
        } else if (v == mClearDataButton) {
            if (mAppEntry.info.manageSpaceActivityName != null) {
                if (!Utils.isMonkeyRunning()) {
                    Intent intent = new Intent(Intent.ACTION_DEFAULT);
                    intent.setClassName(mAppEntry.info.packageName,
                            mAppEntry.info.manageSpaceActivityName);
                    startActivityForResult(intent, REQUEST_MANAGE_SPACE);
                }
            } else {
                showDialogInner(DLG_CLEAR_DATA, 0);
            }
        } else if (v == mClearCacheButton) {
            // Lazy initialization of observer
            if (mClearCacheObserver == null) {
                mClearCacheObserver = new ClearCacheObserver();
            }
            mPm.deleteApplicationCacheFiles(packageName, mClearCacheObserver);
        } else if (v == mForceStopButton) {
            showDialogInner(DLG_FORCE_STOP, 0);
            //forceStopPackage(mAppInfo.packageName);
        } else if (v == mMoveAppButton) {
            if (mPackageMoveObserver == null) {
                mPackageMoveObserver = new PackageMoveObserver();
            }
            int moveFlags = (mAppEntry.info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0 ?
                    PackageManager.MOVE_INTERNAL : PackageManager.MOVE_EXTERNAL_MEDIA;
            mMoveInProgress = true;
            refreshButtons();
            Toast.makeText(getActivity(), R.string.move_app_to_internal_desc,
                    Toast.LENGTH_LONG).show();
            mPm.movePackage(mAppEntry.info.packageName, mPackageMoveObserver, moveFlags);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String packageName = mAppEntry.info.packageName;
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        // myeonghwan.kim@lge.com 20120928 Add CheckBox sound effect
        if (notUserChange != true) {
            mNotificationSwitch.playSoundEffect(AudioManager.FX_KEY_CLICK);
        }

        if (buttonView == mAskCompatibilityCB) {
            am.setPackageAskScreenCompat(packageName, isChecked);
        } else if (buttonView == mEnableCompatibilityCB) {
            am.setPackageScreenCompatMode(packageName, isChecked ?
                    ActivityManager.COMPAT_MODE_ENABLED : ActivityManager.COMPAT_MODE_DISABLED);
        } else if (buttonView == mNotificationSwitch) {
            if (!isChecked) {
                // [S]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
                if (!notUserChange) {
                    showDialogInner(DLG_DISABLE_NOTIFICATIONS, 0);
                    mNotificationSwitch.setChecked(true);
                    notUserChange = true;
                }
                // [E]myeonghwan.kim@lge.com 20120927 Fix wrong marked at notification check box
            } else {
                setNotificationsEnabled(true);
            }
        }
    }

    void refreshNotiText() {
        if (mNotificationSwitch != null) {
            mNotificationSwitch
                    .setText(R.string.app_notifications_switch_label);
        }
    }
}
