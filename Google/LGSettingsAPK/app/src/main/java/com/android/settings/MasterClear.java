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

package com.android.settings;

import com.android.settings.lgesetting.Config.Config;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import android.preference.PreferenceFrameLayout;

import android.content.pm.ApplicationInfo;
import java.util.List;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.settings.Utils;
/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the initial screen.
 */
public class MasterClear extends Fragment {
    private static final String TAG = "MasterClear";

    private static final int KEYGUARD_REQUEST = 55;
    private static final int PIN_REQUEST = 56;

    static final String ERASE_EXTERNAL_EXTRA = "erase_sd";

    private View mContentView;
    private Button mInitiateButton;
    private View mExternalStorageContainer;
    private CheckBox mExternalStorage;
    private boolean mPinConfirmed;
    private boolean mountExternal = false;
    private TextView mEraseExternalText1;
    private TextView mEraseExternalText2;
    private TextView mMasterClearDescText;

    private TextView mDescriptionMasterClear;

    private StorageManager mStorageManager = null;
    private StorageVolume mPrimaryVolume = null;
    private StorageVolume mSecondaryVolume = null;

    private TextView mWarnBatteryView;
    private boolean mIsEnableReset = true;
    private int mBatteryLevel;
    static final int BATTERY_LEVEL = 30;
    private IMountService mMountService;

public static final String PATH_OF_USBSTORAGE = "/storage/USBstorage";

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this)
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.sp_master_clear_gesture_comment_NORMAL));
    }

    private boolean runRestrictionsChallenge() {
        if (UserManager.get(getActivity()).hasRestrictionsChallenge()) {
            startActivityForResult(
                    new Intent(Intent.ACTION_RESTRICTIONS_CHALLENGE), PIN_REQUEST);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mPinConfirmed = true;
            }
            return;
        } else if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            showFinalConfirmation();
        } else {
            establishInitialState();
        }
    }

    private void showFinalConfirmation() {
        Preference preference = new Preference(getActivity());
        preference.setFragment(MasterClearConfirm.class.getName());
        preference.setTitle(R.string.master_clear_confirm_title);
        if (mExternalStorage.isEnabled()) {
                preference.getExtras().putBoolean(ERASE_EXTERNAL_EXTRA, mExternalStorage.isChecked());
            }
        else {
                preference.getExtras().putBoolean(ERASE_EXTERNAL_EXTRA, false);
            }
        ((PreferenceActivity) getActivity()).onPreferenceStartFragment(null, preference);
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private final Button.OnClickListener mInitiateListener = new Button.OnClickListener() {

        public void onClick(View v) {
            mPinConfirmed = false;
            if (runRestrictionsChallenge()) {
                return;
            }
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                showFinalConfirmation();
            }
        }
    };

    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        mInitiateButton = (Button)mContentView.findViewById(R.id.initiate_master_clear);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mExternalStorageContainer = mContentView.findViewById(R.id.erase_external_container);
        mExternalStorage = (CheckBox)mContentView.findViewById(R.id.erase_external);
        mWarnBatteryView = (TextView)mContentView.findViewById(R.id.warning_low_battery);
        mMasterClearDescText = (TextView)mContentView.findViewById(R.id.master_clear_desc);
        mEraseExternalText1 = (TextView)mContentView.findViewById(R.id.erase_external_text1);
        mEraseExternalText2 = (TextView)mContentView.findViewById(R.id.erase_external_text2);
        //[S][2012. 12. 10.][nara.park] [TD#250612] setLayoutType for bullet icon
        //mMasterClearDescText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //[E][2012. 12. 10.][nara.park]

        mDescriptionMasterClear = (TextView)mContentView.findViewById(R.id.master_clear_desc);
        Log.d("starmotor" , "notSupportPreloadlist() : " + notSupportPreloadlist());
        if (notSupportPreloadlist()) {
            mMasterClearDescText.setText(R.string.master_clear_description_settings_ui42);
        }

        if ("SPR".equals(Config.getOperator())) {
            mDescriptionMasterClear.setText(R.string.master_clear_description_Credentials_ui42);
        } else if ("CN".equals(Config.getCountry())) {
            mDescriptionMasterClear.setText(R.string.master_clear_description_cmcc_ui42);
        }

        /*
         * If the external storage is emulated, it will be erased with a factory
         * reset at any rate. There is no need to have a separate option until
         * we have a factory reset that only erases some directories and not
         * others. Likewise, if it's non-removable storage, it could potentially have been
         * encrypted, and will also need to be wiped.
         */
//        boolean isExtStorageEmulated = Environment.isExternalStorageEmulated();
//        if (isExtStorageEmulated
//                || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
//            mExternalStorageContainer.setVisibility(View.GONE);
//
//            // If it's not emulated, it is on a separate partition but it means we're doing
//            // a force wipe due to encryption.
//            mExternalStorage.setChecked(!isExtStorageEmulated);
//        }

        mStorageManager = (StorageManager) getActivity().getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mStorageListener);
        updateExternalView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);

        loadAccountList();
        preloadedAppsList();
    }

    private boolean notSupportPreloadlist() {
        return  (Build.DEVICE.equals("geehrc4g") || Build.DEVICE.equals("geehrc"));
    }

    private boolean isExtStorageEncrypted() {
        String state = SystemProperties.get("vold.decrypt");
        return !"".equals(state);
    }

    private void loadAccountList() {
        View accountsLabel = mContentView.findViewById(R.id.accounts_label);
        LinearLayout contents = (LinearLayout)mContentView.findViewById(R.id.accounts);
        contents.removeAllViews();

        Context context = getActivity();

        AccountManager mgr = AccountManager.get(context);
        Account[] accounts = mgr.getAccounts();
        final int N = accounts.length;
        if (N == 0) {
            accountsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        AuthenticatorDescription[] descs = AccountManager.get(context).getAuthenticatorTypes();
        final int M = descs.length;

        for (int i=0; i<N; i++) {
            Account account = accounts[i];
            if (!isVisibleAccount(account.type)) {
                    continue;
                }

            AuthenticatorDescription desc = null;
            for (int j=0; j<M; j++) {
                if (account.type.equals(descs[j].type)) {
                    desc = descs[j];
                    break;
                }
            }
            if (desc == null) {
                Log.w(TAG, "No descriptor for account name=" + account.name
                        + " type=" + account.type);
                continue;
            }
            Drawable icon = null;
            try {
                Context authContext = context.createPackageContext(desc.packageName, 0);
                if (authContext != null && desc.iconId != 0)
                    icon = authContext.getResources().getDrawable(desc.iconId);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No icon for account type " + desc.type);
            }

//            TextView child = (TextView)inflater.inflate(R.layout.master_clear_account,
//                    contents, false);
//            child.setText(account.name);
//            if (icon != null) {
//                child.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
//            }
            LinearLayout child = (LinearLayout)inflater.inflate(R.layout.master_clear_account,
                    contents, false);
            TextView tv = (TextView)child.findViewById(R.id.text);
            tv.setText(account.name);

            ImageView iv = (ImageView)child.findViewById(R.id.icon);
            if (icon != null) {
                iv.setImageDrawable(icon);
            }
            contents.addView(child);
        }

        if (contents.getChildCount() == 0) {
            accountsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
        } else {
            accountsLabel.setVisibility(View.VISIBLE);
            contents.setVisibility(View.VISIBLE);
        }
    }

    private void preloadedAppsList() {
        View mPreloadAppsLabel = mContentView.findViewById(R.id.preloadapp_label);
        LinearLayout contents = (LinearLayout)mContentView.findViewById(R.id.preloadapp);
        contents.removeAllViews();

        Context context = getActivity();
        String filePath = null;
        PackageManager mPm = getActivity().getPackageManager();
        String installedPackageName = "";
        List<ApplicationInfo> appList = mPm.getInstalledApplications(0);     
        PackageInfo mPackageInfo = null;
        Log.d("starmotor", "appList.size() : " + appList.size());
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int mPreloadListNum = 0;
        for (int i = 0; i < appList.size(); i++ ) {
            ApplicationInfo info = appList.get(i);
            installedPackageName = info.packageName;
            try {
                PackageInfo pi = mPm.getPackageInfo(installedPackageName, 0);
                filePath = mPm.getPackageInfo(installedPackageName, 0).applicationInfo.sourceDir;
                Log.d("starmotor", "(pi.applicationInfo.dataDir) : " + pi.applicationInfo.sourceDir);
                Log.d("starmotor", "filePath : " + filePath);

                if (filePath.startsWith("/data/preload")) {
                    try {
                        mPackageInfo = mPm.getPackageInfo(installedPackageName,
                        PackageManager.GET_DISABLED_COMPONENTS |
                        PackageManager.GET_UNINSTALLED_PACKAGES);
                    } catch (NameNotFoundException e) {
                        Log.e("starmotor", "Exception when retrieving package:" + installedPackageName, e);
                        mPackageInfo = null;
                    }
                    LinearLayout child = (LinearLayout)inflater.inflate(R.layout.master_clear_preloadapp, contents, false);
                    TextView tv = (TextView)child.findViewById(R.id.text);
                    ImageView iv = (ImageView)child.findViewById(R.id.icon);
                    if (mPackageInfo != null) {
                    tv.setText(mPm.getApplicationLabel(mPackageInfo.applicationInfo));
                    iv.setImageDrawable(mPm.getApplicationIcon(mPackageInfo.applicationInfo));
                    contents.addView(child);     
                    }
                    mPreloadListNum++;
                }
            } catch (NameNotFoundException e) {
                Log.d("starmotor", "namenotfoundexception");
            }
        }

        Log.d("starmotor", "mPreloadListNum : " + mPreloadListNum + "\n" 
            + "contents.getChildCount() : " + contents.getChildCount());
//        if (mPreloadListNum == 0) {
            mPreloadAppsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
//            return;
//        }
        /*
        if (contents.getChildCount() == 0) {
            mPreloadAppsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
        } else {
            mPreloadAppsLabel.setVisibility(View.VISIBLE);
            contents.setVisibility(View.VISIBLE);
        }
        */

    }    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //[Start] L OS Add source
        if (UserManager.get(getActivity()).hasUserRestriction(
            UserManager.DISALLOW_FACTORY_RESET)) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        }
        //[End] L OS Add source

        View view = mContentView = inflater.inflate(R.layout.master_clear, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams) view.getLayoutParams()).removeBorders = true;
        }
        if (getActivity() != null) {
            getActivity().setTitle(R.string.master_clear_title_ui42);
        }

        establishInitialState();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        return mContentView;
    }

    @Override
    public void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }

        try {
               getActivity().unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        Log.d(TAG, "unregisterReceiver() : Receiver not registered");
        }

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


    @Override
    public void onResume() {
         if (mIsEnableReset) {

         }

        super.onResume();
        // If this is the second step after restrictions pin challenge
        if (mPinConfirmed) {
            mPinConfirmed = false;
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                showFinalConfirmation();
            }
        }
        
        if (mountExternal == false) {
            mExternalStorage.setChecked(false);
        }

    }

    private void updateExternalView() {
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        if (storageVolumes == null) {
            return;
        }

        int length = storageVolumes.length;
        for (int i = 0; i < length; i++) {
            StorageVolume storageVolume = storageVolumes[i];
            if (storageVolume.getStorageId() == 0x00010001) {
                mPrimaryVolume = storageVolume;
            } else if (!storageVolume.getPath().startsWith(PATH_OF_USBSTORAGE)) {
                mSecondaryVolume = storageVolume;
                break;
            }
        }

        String state = "";
            if (mPrimaryVolume != null && mPrimaryVolume.allowMassStorage()) {
                // ums(mass storage) is supported
                mMasterClearDescText.setText(R.string.master_clear_description_upgrade_ui42);

                if (mPrimaryVolume.isRemovable()) {
                    // SD card
                    mExternalStorageContainer.setVisibility(View.VISIBLE);
                } else {
                    mExternalStorageContainer.setVisibility(View.VISIBLE);
                    mEraseExternalText1.setText(R.string.sp_erase_internal_memory_title_NORMAL);
                    mEraseExternalText2.setText(R.string.sp_erase_internal_memory_summary_NORMAL);
                }
                state = mStorageManager.getVolumeState(mPrimaryVolume.getPath());
            } else {
                if (mSecondaryVolume == null) {
                    mExternalStorageContainer.setVisibility(View.GONE);
                } else {
                    if (mSecondaryVolume.allowMassStorage()) {
                        mMasterClearDescText.setText(R.string.master_clear_description_upgrade_ui42);
                    }
                    if (mSecondaryVolume.isRemovable()) {
                        // SD card
                        mExternalStorageContainer.setVisibility(View.VISIBLE);
                    } else {
                        mExternalStorageContainer.setVisibility(View.VISIBLE);
                        mEraseExternalText1.setText(R.string.sp_erase_internal_memory_title_NORMAL);
                        mEraseExternalText2.setText(R.string.sp_erase_internal_memory_summary_NORMAL);
                    }
                    state = mStorageManager.getVolumeState(mSecondaryVolume.getPath());
                }
            }

        if (mExternalStorageContainer.getVisibility() == View.VISIBLE) {
            mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExternalStorage.toggle();
                }
            });
        }


        if (!Environment.MEDIA_MOUNTED.equals(state)) {
                mountExternal = false;
            }
        else {
                mountExternal = true;
            }

        mExternalStorageContainer.setEnabled(mountExternal);
        mExternalStorage.setEnabled(mountExternal);
        mEraseExternalText1.setEnabled(mountExternal);
        mEraseExternalText2.setEnabled(mountExternal);

        //[S][2012. 10. 31.][nara.park][LAB1_11][TD#12048] update checkbox status when unmount SD card
        if (mountExternal == false) {
            mExternalStorage.setChecked(false);
        }
        //[E][2012. 10. 31.][nara.park]
    }

    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " + path +
                    " changed state from " + oldState + " to " + newState);

            updateExternalView();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

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
        if (mBatteryLevel < BATTERY_LEVEL) {
            mIsEnableReset = false;
            mWarnBatteryView.setText(getString(R.string.backup_reset_encrypt_battery_low, String.format("%d", mBatteryLevel) + "%", String.format("%d", BATTERY_LEVEL) + "%"));
            mWarnBatteryView.setVisibility(View.VISIBLE);
            mInitiateButton.setEnabled(false);
        } else if (mBatteryLevel >= BATTERY_LEVEL) {
            mIsEnableReset = true;
            mWarnBatteryView.setVisibility(View.GONE);
            mInitiateButton.setEnabled(true);

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                com.android.settings.MDMSettingsAdapter.getInstance().setFactoryResetButton(null,
                        getActivity().getApplicationContext(), mInitiateButton);
            }
            // LGMDM_END
        }
    }

    private boolean isVisibleAccount(String accountType) {

        Log.i(TAG, "isVisibleAccount : " + accountType);

        if ( accountType.equals("com.mobileleader.sync")) {
            return false;
          }
        if ( accountType.equals("com.lge.sync")) {
            return false;
          }
        if ( accountType.equals("com.verizon.phone")) {
            return false;
          }
        if ( accountType.equals("com.fusionone.account") ) {
            return false;
          }
        if ( accountType.equals("com.lge.myphonebook") ) {
            return false;
          }
        if ( accountType.equals("com.lge.android.finance.sync") ) {
            return false;
          }
        if ( accountType.equals("com.lge.android.weather.sync") ) {
            return false;
          }
         if ( accountType.equals("com.lge.android.news.sync") ) {
            return false;
          }

        if ( accountType.equals("com.lge.android.todayplus.sync") ) {
            return false;
          }


        return true;

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
}
