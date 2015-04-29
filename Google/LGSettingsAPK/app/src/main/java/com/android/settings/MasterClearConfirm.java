/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.Context;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.os.SystemProperties;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.UserHandle;
import android.service.persistentdata.PersistentDataBlockManager;
//[S] insook.kim@lge.com 2012.03.15: support LG_SYS_CDMA_FACTORY_RESET feature
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import android.os.Build;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
//[E] insook.kim@lge.com 2012.03.15 : support LG_SYS_CDMA_FACTORY_RESET feature

import java.io.File;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import com.android.settings.Utils;
import com.lge.config.ConfigBuildFlags;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGSDEncManager;
/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the confirmation screen.
 */
public class MasterClearConfirm extends Fragment {
    private static final String TAG = "MasterClearConfirm";


    private View mContentView;
    private boolean mEraseExternal;
    private Button mFinalButton;
    private StorageManager mStorageManager = null;
    private StorageVolume mExternalVolume = null;
    private IMountService mMountService;
    private boolean mErasingSdCard = false;

    private TextView mWarnBatteryView;
    private boolean mIsEnableReset = true;
    private int mBatteryLevel;

    AlertDialog mConfirmDlg = null;
    private boolean mIsShowDlg = false;
    private boolean mIsSelectOK = false;
    private static final String FACTORY_RESET_DATE = "factroy_reset_date";
    private LGContext mServiceContext = null;
    private LGSDEncManager mLGSDEncManager = null;

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Checkin Service to reset the device to its factory-default
     * state (rebooting in the process).
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }

            //[Start] L OS Add source

            final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                    getActivity().getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);

            if (pdbManager != null && !pdbManager.getOemUnlockEnabled()) {
                // if OEM unlock is enabled, this will be wiped during FR
                // process.
                final ProgressDialog progressDialog = getProgressDialog();
                progressDialog.show();

                // need to prevent orientation changes as we're about to go into
                // a long IO request, so we won't be able to access inflate
                // resources on flash
                final int oldOrientation = getActivity()
                        .getRequestedOrientation();
                getActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        pdbManager.wipe();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        progressDialog.hide();
                        getActivity().setRequestedOrientation(oldOrientation);
                        doMasterClear();
                    }
                }.execute();
            } else {
                doMasterClear();
            }
        }

        private ProgressDialog getProgressDialog() {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(
                    getActivity().getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(
                    getActivity().getString(R.string.master_clear_progress_text));
            return progressDialog;
        }
    };

            //[END] : L OS Add source

    private void doMasterClear() {

        // 3LM_MDM_START KK
        // [COMMAND-WIPE-DATA]
        // Set SD card encryption disabled now.
        // Android will format SD card first and then intenral storage. So we need to disable
        // SD card encryption so that the MountService
        // won't get wrong configuration when formatting
        // SD card.
        Settings.Global.putInt(getActivity().getContentResolver(), "sd_encryption", 0);
        // 3LM_MDM_END

        if (mIsShowDlg/* || mIsSelectOK*/) {
            Log.d(TAG, "onClick : mIsShowDlg = " + mIsShowDlg + "mIsSelectOK = " + mIsSelectOK);
            return;
        }

        //[nara.park@lge.com] 2012. 6. 28. check integral battery
        String strText = "";
        if (getActivity().getResources().getBoolean(com.lge.R.bool.config_default_encrypt)) {
            strText = getString(R.string.factory_reset_warning_new) + " " + getString(R.string.factory_reset_reboot_new);
        } else {
            strText = getString(R.string.factory_reset_warning_reboots);
        }

        if (checkIntegralBattery()) {
            if (getActivity().getResources().getBoolean(com.lge.R.bool.config_default_encrypt)) {
                strText = getString(R.string.factory_reset_warning_new);
            } else {
                strText = getString(R.string.factory_reset_warning_reboots_integrated_battery);
            }
        }
        //[nara.park@lge.com] end.

        mConfirmDlg = new AlertDialog.Builder(mContentView.getContext())
            .setTitle(R.string.sp_dlg_note_NORMAL)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setMessage(strText)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doPositiveButton();
                        }
            })
            .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                //setCurrentTimeToDB(); // Test
                            dialog.cancel();
                        }
            })
            .create();

        if (mConfirmDlg != null) {
            mIsShowDlg = true;
            mConfirmDlg.show();
            mConfirmDlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
                @Override
                public void onDismiss(DialogInterface arg0) {
                    mIsShowDlg = false;
                }
            });
        }
    }

    private void doPositiveButton() {

        PreProcess pp = new PreProcess();
        pp.execute();
        if (Config.getOperator().equals("LRA")
                || Config.getOperator().equals("ACG")
                || Config.getOperator().equals("VZW")
                || Config.getOperator().equals("TRF")
                || Config.getOperator().equals("SPR")
                || Config.getOperator().equals("CTC")
                || Config.getOperator().equals("CTO")) {
            sendCDMAFactoryRequest();
        }

        OverlayUtils.initNV(mContentView.getContext());
        if (mEraseExternal) {
            nativeCodeFormatSdCard();

            mErasingSdCard = true;

        } else if (Build.DEVICE.equals("altev")) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MASTER_CLEAR");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            getActivity().sendBroadcast(intent);
        } else {
            Intent masterClearIntent = new Intent(
                    "android.intent.action.MASTER_CLEAR");
            masterClearIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            getActivity().sendBroadcast(masterClearIntent);
            // Intent handling is asynchronous -- assume it will happen soon.
        }

        // [S][2012. 8. 3.][nara.park] add flag to prevent duplicated selection
        mIsSelectOK = true;
        Log.d(TAG, "onClick() : mIsSelectOK =" + mIsSelectOK);
        // [E][2012. 8. 3.][nara.park]

    }

        private void setCurrentTimeToDB() {
            long firstboot_time = Long.parseLong(SystemProperties.get("ro.runtime.firstboot"));
            long current_time = getCurrentTimeMillis();
            long lastFactoryReset = android.provider.Settings.Secure.getLong(getActivity().getContentResolver(), FACTORY_RESET_DATE, 0);

            Log.d(TAG, "firstboottime =" + firstboot_time);
            Log.d(TAG, "current time  =" + current_time);

            if ( current_time >= lastFactoryReset ) {
                setDBFactoryResetDate(current_time);
            } else {
                setDBFactoryResetDate(firstboot_time);
            }

            long temp = android.provider.Settings.Secure.getLong(getActivity().getContentResolver(), FACTORY_RESET_DATE, 0);
            Log.d(TAG, "factroy_reset_date befor =" + temp);
            long temp1  = getDBStartTime();
            Log.d(TAG, "factroy_reset_date after =" + temp1);
        }

        private long getCurrentTimeMillis() {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());

            return c.getTimeInMillis();
        }

        private long getCalendarTimeMillis(long time) {
            java.util.Calendar dummy = java.util.Calendar.getInstance();
            dummy.setTimeInMillis(time);

            int hourOfDay = dummy.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = dummy.get(java.util.Calendar.MINUTE);
            int seconds = dummy.get(java.util.Calendar.SECOND);

            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(java.util.Calendar.AM_PM, 0);
            c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(java.util.Calendar.MINUTE, minute);
            c.set(java.util.Calendar.SECOND, seconds);

            return c.getTimeInMillis();
        }

        private void setDBFactoryResetDate(long startTime) {
            //Settings.System.putLong(context.getContentResolver(), QUIET_TIME_START_TIME, startTime);
            android.provider.Settings.Secure.putLong(getActivity().getContentResolver(), FACTORY_RESET_DATE, startTime);
        }

        private long getDBStartTime() {
            try {
                Log.d(TAG, "getDBStartTime() - start time string : " + getTimeString(android.provider.Settings.Secure.getLong(getActivity().getContentResolver(), FACTORY_RESET_DATE)));

                return getCalendarTimeMillis(android.provider.Settings.Secure.getLong(getActivity().getContentResolver(), FACTORY_RESET_DATE));
            } catch (SettingNotFoundException e) {
                Log.e(TAG, "SettingNotFoundException - getDBStartTime()");
                //Log.d(TAG,"getDBStartTime() - Dummy start time string : " + getTimeString(getDummyTime(false)));
                //setDBStartTime(getDummyTime(false));
                return getDummyTime(false);
            }
        }

        private long getDummyTime(boolean isAmPm) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            Date date = new Date();
            if (isAmPm == false) {
                date.setHours(22);
                date.setMinutes(00);
                date.setSeconds(00);
            } else {
                date.setHours(06);
                date.setMinutes(00);
                date.setSeconds(00);
            }

            c.setTime(date);
            int hourOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = c.get(java.util.Calendar.MINUTE);
            int seconds = c.get(java.util.Calendar.SECOND);
            c.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(java.util.Calendar.MINUTE, minute);
            c.set(java.util.Calendar.SECOND, seconds);
            c.add(java.util.Calendar.DAY_OF_YEAR, +1);

            return c.getTimeInMillis();
        }

        public String getTimeString(Long time) {
            return DateFormat.getTimeFormat(getActivity()).format(time);
        }

        public String getDateString(Long time) {
            return DateFormat.getLongDateFormat(getActivity()).format(time);
        }

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        mFinalButton = (Button)mContentView.findViewById(R.id.execute_master_clear);
        mFinalButton.setOnClickListener(mFinalClickListener);
        mWarnBatteryView = (TextView)mContentView.findViewById(R.id.warning_low_battery);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (getActivity() != null && !Utils.supportSplitView(getActivity().getApplicationContext())) {
            getActivity().getActionBar().setTitle(R.string.master_clear_title_ui42);
        }

        //[Start] L OS Add source
        if (UserManager.get(getActivity()).hasUserRestriction(
            UserManager.DISALLOW_FACTORY_RESET)) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        }
        //[End] L OS Add source

        if (Utils.supportSplitView(getActivity().getApplicationContext())) {
            mContentView = inflater.inflate(R.layout.master_clear_confirm_tablet, null);
    } else {
            mContentView = inflater.inflate(R.layout.master_clear_confirm, null);
    }
        establishFinalConfirmationState();
        return mContentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorageManager = (StorageManager)getActivity().getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mStorageListener);

        Bundle args = getArguments();
        mEraseExternal = args != null && args.getBoolean(MasterClear.ERASE_EXTERNAL_EXTRA);
        if (mEraseExternal) {
            mExternalVolume = getExternalVolume();
          }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(mBatteryReceiver, filter);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }


    @Override
    public void onResume() {
        if (mIsEnableReset) {

        }
        super.onResume();
    }


    private StorageVolume getExternalVolume() {
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        if (storageVolumes == null) {
            return null;
        }

        StorageVolume PrimaryVolume = null;
        StorageVolume SecondaryVolume = null;

        int length = storageVolumes.length;
        for (int i = 0; i < length; i++) {
            StorageVolume storageVolume = storageVolumes[i];
            if (storageVolume.getStorageId() == 0x00010001) {
                PrimaryVolume = storageVolume;
            } else {
                SecondaryVolume = storageVolume;
                break;
            }
        }
            if (PrimaryVolume != null && PrimaryVolume.allowMassStorage()) {
                // ums(mass storage) is supported
                return PrimaryVolume;
            } else {
                if (SecondaryVolume == null) {
                    return null;
                } else {
                    return SecondaryVolume;
                }
            }
    }

    //[S] insook.kim@lge.com 2012.03.15
    private void nativeCodeFormatSdCard() {
        Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
        intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
        intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mExternalVolume);

        getActivity().startService(intent);
    }
    //[E] insook.kim@lge.com 2012.03.15 :

/*============================================================
    The following is used for verizon's model.
============================================================*/
    // BEGIN: 0009214 sehyuny.kim@lge.com 2010-09-03
    // MOD 0009214: [DIAG] LG Diag feature added in side of android
    // LG_SYS_CDMA_FACTORY_RESET
    static final String PHONE_CDMA_REQUEST_FACTORY_RESET_COMPLETED = "phoneCdmaRequestFactoryResetCompleted";
    // LG_SYS_CDMA_FACTORY_RESET
    // END: 0009214 sehyuny.kim@lge.com 2010-09-03
    // LGE_MERGE_E

    // BEGIN: 0009214 sehyuny.kim@lge.com 2010-09-03
    // MOD 0009214: [DIAG] LG Diag feature added in side of android
    // LG_SYS_CDMA_FACTORY_RESET
    private static final String ACTION_CDMA_REQUEST_FACTORY_RESET
            = "android.intent.action.ACTION_CDMA_REQUEST_FACTORY_RESET";

    private static final String ACTION_CDMA_REQUEST_FACTORY_RESET_COMPLETED
            = "android.intent.action.ACTION_CDMA_REQUEST_FACTORY_RESET_COMPLETED";
    // LG_SYS_CDMA_FACTORY_RESET

    // BEGIN: 0009484 sehyuny.kim@lge.com 2010-09-24
    // MOD 0009484: [FactoryReset] Enable FactoryReset
    // LGE_MERGE_S
    // LG_SYS_CDMA_FACTORY_RESET
        void sendCDMAFactoryRequest() {
            Intent intent = new Intent(ACTION_CDMA_REQUEST_FACTORY_RESET);
            Log.d(TAG, "sendCDMAFactoryRequest() is called.");
            ActivityManagerNative.broadcastStickyIntent(intent,null, UserHandle.USER_ALL);
        }

        private class MasterCDMAFactoryResetReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION_CDMA_REQUEST_FACTORY_RESET_COMPLETED)) {
                    MasterCDMAFactoryResetCompleted(intent);
                }
            }
        }

        private void MasterCDMAFactoryResetCompleted(Intent intent) {
            Log.d(TAG, "MasterCDMAFactoryResetCompleted() is called.");

            int cdma_factory_reset_completed  = (int)intent.getIntExtra(PHONE_CDMA_REQUEST_FACTORY_RESET_COMPLETED , -1);
            if (mEraseExternal) {
                nativeCodeFormatSdCard();
             } else if (cdma_factory_reset_completed != 0) {
				if (Build.DEVICE.equals("altev")) {
				   Intent masterIntent = new Intent("android.intent.action.MASTER_CLEAR");
				   masterIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
				   getActivity().sendBroadcast(masterIntent);
				} else {
				   Intent masterIntent = new Intent("android.intent.action.MASTER_CLEAR");
				   getActivity().sendBroadcast(masterIntent);
				}

            // Intent handling is asynchronous -- assume it will happen soon.

                Log.d(TAG, "send MASTER_CLEAR intent.");
            }


        // LG_FW_GPS_FACTORY_RESET start
            //GPS_default_set();
        // LG_FW_GPS_FACTORY_RESET end
         }
    // LG_SYS_CDMA_FACTORY_RESET
    // LGE_MERGE_E
    // END: 0009484 sehyuny.kim@lge.com 2010-09-24

    @Override
    public void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }

           getActivity().unregisterReceiver(mBatteryReceiver);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
        super.onDestroy();
    }

// ============================================================

    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            if (!mErasingSdCard) {
                getActivity().finish();
            }
        }
    };

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);

                int battery = level * 100 / scale;
                Log.d(TAG, "battery = " + battery + "%");

                mBatteryLevel = battery;
                checkBatteryLevel();
            }
        }
    };

    private void checkBatteryLevel() {
        if (mBatteryLevel < MasterClear.BATTERY_LEVEL) {
            if (mConfirmDlg != null) {
                mConfirmDlg.dismiss();
                mConfirmDlg = null;
            }
            mIsEnableReset = false;
            mWarnBatteryView.setText(getString(R.string.backup_reset_encrypt_battery_low, mBatteryLevel + "%", MasterClear.BATTERY_LEVEL + "%"));
            mWarnBatteryView.setVisibility(View.VISIBLE);
            mFinalButton.setEnabled(false);
        } else if (mBatteryLevel >= MasterClear.BATTERY_LEVEL) {
            mIsEnableReset = true;
            mFinalButton.setEnabled(true);
            if (ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY
                    && LGSDEncManager.getSDEncSupportStatus(getActivity())) {
                mServiceContext = new LGContext(getActivity());
                mLGSDEncManager = (LGSDEncManager)mServiceContext
                        .getLGSystemService(LGContext.LGSDENC_SERVICE);
                boolean isSDEncMetaFile = mLGSDEncManager
                        .isExistSDEncMetaFile();
                if (isSDEncMetaFile) {
                    mWarnBatteryView.setVisibility(View.VISIBLE);
                    mWarnBatteryView
                            .setText(getString(R.string.reset_sd_encrypt_desc));
                } else {
                    mWarnBatteryView.setVisibility(View.GONE);
                }
            } else {
                mWarnBatteryView.setVisibility(View.GONE);
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                com.android.settings.MDMSettingsAdapter.getInstance().setFactoryResetConfirmButton(
                        null, getActivity().getApplicationContext(), mFinalButton);
            }
            // LGMDM_END
        }
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

    private boolean checkIntegralBattery() {
        if (Build.DEVICE.equals("geeb") || Build.DEVICE.equals("geeb3g") || Build.DEVICE.equals("geefhd") ||
                Build.DEVICE.equals("geehrc") || Build.DEVICE.equals("geehrc4g") ||
                Build.DEVICE.equals("geevl04e") || Build.DEVICE.equals("gvfhd") 
                || Utils.isEmbededBattery(getActivity().getApplicationContext())
                || Utils.isWifiOnly(getActivity())) {
            return true;
        }

        return false;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveWipeDateChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END

    @Override
    public void onPause() {
        super.onPause();

    }
}
