/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.settings.lgesetting.Config.Config;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
/* MSE-ADD-S, IC Data Transfer for DCM, daehwan.kim@lge.com, 2013/02/18 */
import android.app.ActivityManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.hardware.usb.UsbManager;
import android.os.SystemProperties;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import android.content.IntentFilter;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/* MSE-ADD-E, IC Data Transfer for DCM, daehwan.kim@lge.com, 2013/02/18 */

/**
 * Gesture lock pattern settings.
 */
public class PrivacySettings extends SettingsPreferenceFragment implements
        DialogInterface.OnClickListener, Indexable {

    // Vendor specific
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String BACKUP_CATEGORY = "backup_category";
    private static final String BACKUP_DATA = "backup_data";
    private static final String AUTO_RESTORE = "auto_restore";
    private static final String CONFIGURE_ACCOUNT = "configure_account";
    private static final String SKT_BACKUP = "skt_backup_service";
    private static final String PERSONAL_DATA_CATEGORY = "personal_data_category";
    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
    private static final String BACKUP_RESET = "backup_reset";
    // LGMDM_END
    private static final String BACKUP_RESET_VZW = "backup_reset_vzw";
    private static final String LG_BACKUP_CATEGORY = "lg_backup_category";
    private static final String LG_BACKUP_SERVICE = "lg_backup_service";
    private static final String RESET_SETTINGS = "reset_settings";
    
    private IBackupManager mBackupManager;
    private PreferenceCategory mPersonalDataCategory;
    private CheckBoxPreference mBackup;
    private CheckBoxPreference mAutoRestore;
    //private Dialog mConfirmDialog;
    private PreferenceScreen mConfigure;
    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
    private PreferenceScreen mBackupReset;
    private PreferenceScreen mBackupReset_vzw;
    // LGMDM_END
    private static final int DIALOG_ERASE_BACKUP = 2;
    private int mDialogType;

    private PreferenceCategory lgBackupCategory;
    private PreferenceCategory mSKTBackupGategory;
    private PreferenceScreen mlgBackupScreen;
    private PreferenceScreen mSKTbackup;
    private PreferenceScreen mCiqTMO;
    private PreferenceScreen mCiqMPCS;
    
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;
    static final String CONTENT_URI = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.privacy_settings);
        //[S][2013.5.21][jm1.yoo@lge.com] Changed the menu of backup and reset for CMCC
        /*
        if (getActivity() != null && "CMCC".equals(Config.getOperator())) {
            getActivity().setTitle(R.string.call_meter_reset);
        }
        */
        //[E][2013.5.21][jm1.yoo@lge.com] Changed the menu of backup and reset for CMCC
        final PreferenceScreen screen = getPreferenceScreen();

        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));

        mBackup = (CheckBoxPreference)screen.findPreference(BACKUP_DATA);
        mAutoRestore = (CheckBoxPreference)screen.findPreference(AUTO_RESTORE);
        mConfigure = (PreferenceScreen)screen.findPreference(CONFIGURE_ACCOUNT);
        mPersonalDataCategory = (PreferenceCategory)screen.findPreference(PERSONAL_DATA_CATEGORY);
        mCiqTMO = (PreferenceScreen)screen.findPreference("ciq_toggle_tmo");
        mCiqMPCS = (PreferenceScreen)screen.findPreference("ciq_toggle_mpcs");
        mSKTBackupGategory = (PreferenceCategory)findPreference("skt_backcup");
        mSKTbackup = (PreferenceScreen)screen.findPreference(SKT_BACKUP);

        //[Start] L OS Add source
        if (UserManager.get(getActivity()).hasUserRestriction(
                UserManager.DISALLOW_FACTORY_RESET)) {
            if (mPersonalDataCategory != null) {
                mPersonalDataCategory.removePreference(findPreference(BACKUP_RESET));
                mPersonalDataCategory.removePreference(findPreference(BACKUP_RESET_VZW));
            }
        }
        //[End] L OS Add source

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (mPersonalDataCategory != null) {
                mBackupReset = (PreferenceScreen)mPersonalDataCategory.findPreference(BACKUP_RESET);
                mBackupReset_vzw = (PreferenceScreen)mPersonalDataCategory.findPreference(BACKUP_RESET_VZW);
            }
        }
        // LGMDM_END
        if ("VZW".equals(Config.getOperator())) {
            if (mPersonalDataCategory != null) {
                mPersonalDataCategory.removePreference(findPreference(BACKUP_RESET));
            }
        } else {
            if (mPersonalDataCategory != null) {
                mPersonalDataCategory.removePreference(findPreference(BACKUP_RESET_VZW));
            }
        }

        // LGE_CHANGE_S : If apn2 is disabled, the reset menu disable. on 120601, by infomagic
        if (mBackupReset == null) {
            if (mPersonalDataCategory != null) {
                mBackupReset = (PreferenceScreen)mPersonalDataCategory.findPreference(BACKUP_RESET);
            }
        }
        if (mBackupReset_vzw == null) {
            if (mPersonalDataCategory != null) {
                mBackupReset_vzw = (PreferenceScreen)mPersonalDataCategory.findPreference(BACKUP_RESET_VZW);
            }
        }        
        // LGE_CHANGE_E : If apn2 is disabled, the reset menu disable. on 120601, by infomagic

        //[nara.park@lge.com] 2012. 7. 11. [U2][TMUS] CIQ requirement

        if (getActivity() != null &&
                !Utils.checkPackage(getActivity(), "com.carrieriq.tmobile.IQToggle")) {
            if (mPersonalDataCategory != null && mCiqMPCS != null) {
                mPersonalDataCategory.removePreference(mCiqMPCS);
            }
        }

        if (getActivity() != null &&
                !Utils.checkPackage(getActivity(), "com.tmobile.pr.mytmobile")) {
            if (mPersonalDataCategory != null && mCiqTMO != null) {
                mPersonalDataCategory.removePreference(mCiqTMO);
            }
        }

        if ("TMO".equals(Config.getOperator())
            && "US".equals(Config.getCountry())) {
            if (mPersonalDataCategory != null && mCiqMPCS != null) {
                mPersonalDataCategory.removePreference(mCiqMPCS);
            }
        } else if ("MPCS".equals(Config.getOperator())) {
            if (mPersonalDataCategory != null && mCiqTMO != null) {
                mPersonalDataCategory.removePreference(mCiqTMO);
            }
        }
        //[nara.park@lge.com] end.
        
        // Vendor specific
        if (getActivity().getPackageManager().
                resolveContentProvider(GSETTINGS_PROVIDER, 0) == null) {
            screen.removePreference(findPreference(BACKUP_CATEGORY));
        }
        updateToggles();
        
        //PreferenceScreen resetsetting = (PreferenceScreen)findPreference("reset_settings");
        if (!Config.getOperator().equals(Config.VZW)
                || !Utils.isUI_4_1_model(getActivity()) && Build.DEVICE.equals("g2")) {
            if (mPersonalDataCategory != null) {
                mPersonalDataCategory.removePreference(findPreference(RESET_SETTINGS));
            }
        }
        if ("altev".equals(Build.DEVICE)) {
            if (mPersonalDataCategory != null) {
                mPersonalDataCategory.removePreference(findPreference(RESET_SETTINGS));
            }
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169][ID-MDM-251][ID-MDM-278]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            MDMSettingsAdapter.getInstance().addBackupRestorePolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        lgBackupCategory = (PreferenceCategory)findPreference(LG_BACKUP_CATEGORY);
        
        if (getActivity() != null && !Utils.checkPackage(getActivity(), "com.lge.bnr")) {
            if (lgBackupCategory != null) {
                screen.removePreference(lgBackupCategory);
            }
        } else {
            //[youngju.do] from LGBackup4.30.0, target class name is changed to support wifi transfer.
            mlgBackupScreen = (PreferenceScreen)findPreference(LG_BACKUP_SERVICE);
            if (null != mlgBackupScreen) {
                mlgBackupScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        Intent intent = new Intent();
                        intent.setPackage("com.lge.bnr");

                        String targetClass = "com.lge.bnr.activity.BNRMainActivity";
                        if (isLGBackupSupportWifiTransfer(getActivity())) {
                            targetClass = "com.lge.bnr.activity.BNRViaWifiMainActivity";
                        }

                        intent.setClassName("com.lge.bnr", targetClass);
                        intent.setAction("android.intent.action.MAIN");
                        startActivity(intent);
                        return true;
                    }
                });
            }
        }

        //        if (getActivity() != null && !Utils.checkPackage(getActivity(), "com.skt.tbagplus")) {
        if (!"SKT".equals(Config.getOperator())) {
            if (mSKTBackupGategory != null && mSKTbackup != null) {
                mSKTBackupGategory.removePreference(mSKTbackup);
                screen.removePreference(findPreference("skt_backcup"));
            }
        }
        //        }
        if (mPersonalDataCategory != null &&
                mPersonalDataCategory.getPreferenceCount() == 0) {
            screen.removePreference(mPersonalDataCategory);
        }
        mIsFirst = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            inflater.inflate(R.menu.settings_search, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            switchTosearchResults();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void switchTosearchResults() {
        Intent intent = new Intent();
        intent.setAction("com.lge.settings.SETTINGS_SEARCH");
        intent.putExtra("search", true);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh UI
        updateToggles();
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            Log.d("skku", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void startResult(boolean newValue) {
        if (mSearch_result.equals(SKT_BACKUP)
                || mSearch_result.equals(BACKUP_RESET)
                || mSearch_result.equals(BACKUP_RESET_VZW)
                || mSearch_result.equals(RESET_SETTINGS)
                || mSearch_result.equals("ciq_toggle_mpcs")
                || mSearch_result.equals("ciq_toggle_tmo")) {
            getActivity().finish();
        } else if (mSearch_result.equals(BACKUP_DATA)) {
            mBackup.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(CONFIGURE_ACCOUNT)) {
            mConfigure.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(AUTO_RESTORE)) {
            mAutoRestore.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(LG_BACKUP_SERVICE)) {
            getActivity().finish();
        }
    }

    private void searchDBUpdate(boolean newValue, String key, String field) {
        Log.d("skku", "searchDBUpdate newValue : " + newValue + " key : " + key);
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues row = new ContentValues();
        row.put(field, newValue ? 1 : 0);
        String where = "data_key_reference = ? AND class_name = ?";
        Log.d("skku", "searchDBUpdate where : " + where);
        cr.update(Uri.parse(CONTENT_URI), row, where, new String[] { key,
                "com.android.settings.PrivacySettings" });
    }

    @Override
    public void onStop() {
        // LGE_CHANGE_S : When onStop, do not destroy popup
        /*if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
            mConfirmDialog.dismiss();
        }
        mConfirmDialog = null;
        mDialogType = 0;*/
        super.onStop();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169][ID-MDM-251][ID-MDM-278]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        final PreferenceActivity activity = (PreferenceActivity)getActivity();
        if (preference == mBackup) {
            if (!mBackup.isChecked()) {
                showEraseBackupDialog();
            } else {
                setBackupEnabled(true);
                searchDBUpdate(true, BACKUP_DATA, "check_value");
                searchDBUpdate(true, CONFIGURE_ACCOUNT, "current_enable");
                searchDBUpdate(true, AUTO_RESTORE, "current_enable");
            }
        } else if (preference == mAutoRestore) {
            boolean curState = mAutoRestore.isChecked();
            try {
                mBackupManager.setAutoRestore(curState);
                searchDBUpdate(curState, AUTO_RESTORE, "check_value");
            } catch (RemoteException e) {
                mAutoRestore.setChecked(!curState);
                searchDBUpdate(!curState, AUTO_RESTORE, "check_value");
            }
        } else if (preference == mSKTbackup) {
            if (getActivity() != null && !Utils.checkPackage(getActivity(), "com.skt.tbagplus")) {
                Uri uri = Uri.parse("http://tsto.re/0000179401");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } else {
                Intent mIntent = new Intent("com.skt.tbagplus.ACTION_EVOKE_TCLOUD");
                int mReq_func = 8;
                mIntent.putExtra("caller", "com.android.settings");
                mIntent.putExtra("service_function", mReq_func);
                mIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                getActivity().sendBroadcast(mIntent);
            }
        } else if (mBackupReset_vzw != null && preference == mBackupReset_vzw) {
//            String property = SystemProperties.get("persist.verizon.llkagent");
            if (Settings.Secure.getInt(getContentResolver(),
                "verizonwireless_store_demo_mode", 0) == 1) {
                activity.startPreferencePanel(
                        LlkDemoMode.class.getName(), null,
                        R.string.master_clear_title_ui42, null, PrivacySettings.this,
                        0);
            } else {
                activity.startPreferencePanel(
                        MasterClear.class.getName(), null,
                        R.string.master_clear_title_ui42, null, PrivacySettings.this,
                        0);
            }

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showEraseBackupDialog() {
        mBackup.setChecked(true);

        mDialogType = DIALOG_ERASE_BACKUP;
        CharSequence msg = getResources().getText(R.string.backup_erase_dialog_message);
        // TODO: DialogFragment?
        if (Config.getOperator().equals(Config.VZW)) {
            /*mConfirmDialog = */ new AlertDialog.Builder(getActivity()).setMessage(msg)
                    .setTitle(R.string.backup_erase_dialog_title)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this)
                    .show();
        } else {
            /*mConfirmDialog = */ new AlertDialog.Builder(getActivity()).setMessage(msg)
                    .setTitle(R.string.backup_erase_dialog_title)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this)
                    .show();
        }
    }

    /*
     * Creates toggles for each available location provider
     */
    private void updateToggles() {
        ContentResolver res = getContentResolver();

        boolean backupEnabled = false;
        Intent configIntent = null;
        String configSummary = null;
        try {
            if (mBackupManager != null) {
                backupEnabled = mBackupManager.isBackupEnabled();
                String transport = mBackupManager.getCurrentTransport();
                configIntent = mBackupManager.getConfigurationIntent(transport);
                configSummary = mBackupManager.getDestinationString(transport);
                if (configSummary != null) {
                    if (configSummary.equals("Need to set the backup account")
                            && Config.getOperator().equals(Config.VZW)) {
                        configSummary = getResources().getString(
                                R.string.backup_configure_account_default_summary_no_account);
                    }
                } else {
                    Log.d("PrivacySettings", "PrivacySettings : configSummary is null");
                }
            }
        } catch (RemoteException e) {
            // leave it 'false' and disable the UI; there's no backup manager
            mBackup.setEnabled(false);
        }
        mBackup.setChecked(backupEnabled);

        mAutoRestore.setChecked(Settings.Secure.getInt(res,
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1);
        mAutoRestore.setEnabled(backupEnabled);

        final boolean configureEnabled = (configIntent != null) && backupEnabled;
        mConfigure.setEnabled(configureEnabled);
        mConfigure.setIntent(configIntent);
        setConfigureSummary(configSummary);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169][ID-MDM-251][ID-MDM-278]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (mBackupReset != null) {
                com.android.settings.MDMSettingsAdapter.getInstance().setFactoryResetMenu(null,
                        getActivity().getApplicationContext(), mBackupReset);
            }
            if (mBackup != null) {
                com.android.settings.MDMSettingsAdapter.getInstance().setGoogleBackupMenu(null,
                        getActivity().getApplicationContext(), mBackup);
            }
            if (mAutoRestore != null) {
                com.android.settings.MDMSettingsAdapter.getInstance().setAutoRestoreMenu(null,
                        getActivity().getApplicationContext(), mAutoRestore);
            }
            if (mBackupReset_vzw != null) {
                com.android.settings.MDMSettingsAdapter.getInstance().setFactoryResetMenu(null,
                        getActivity().getApplicationContext(), mBackupReset_vzw);
            }            
        }
        // LGMDM_END
    }

    private void setConfigureSummary(String summary) {
        if (summary != null) {
            if (summary.equals("Need to set the backup account")
                    && Config.getOperator().equals(Config.VZW)) {
                summary = getResources().getString(
                        R.string.backup_configure_account_default_summary_no_account);
            }
            mConfigure.setSummary(summary);
        } else {
            mConfigure.setSummary(R.string.backup_configure_account_default_summary);
        }
    }

    private void updateConfigureSummary() {
        try {
            String transport = mBackupManager.getCurrentTransport();
            String summary = mBackupManager.getDestinationString(transport);
            setConfigureSummary(summary);
        } catch (RemoteException e) {
            Log.w("PrivacySettings", "RemoteException");
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        // Dialog is triggered before Switch status change, that means marking the Switch to
        // true in showEraseBackupDialog() method will be override by following status change.
        // So we do manual switching here due to users' response.
        if (mDialogType == DIALOG_ERASE_BACKUP) {
            // Accept turning off backup
            if (which == DialogInterface.BUTTON_POSITIVE) {
                setBackupEnabled(false);
                searchDBUpdate(false, BACKUP_DATA, "check_value");
                searchDBUpdate(false, CONFIGURE_ACCOUNT, "current_enable");
                searchDBUpdate(false, AUTO_RESTORE, "current_enable");
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                // Reject turning off backup
                setBackupEnabled(true);
                searchDBUpdate(true, BACKUP_DATA, "check_value");
                searchDBUpdate(true, CONFIGURE_ACCOUNT, "current_enable");
                searchDBUpdate(true, AUTO_RESTORE, "current_enable");
            }
            updateConfigureSummary();
        }
        mDialogType = 0;
    }

    /**
     * Informs the BackupManager of a change in backup state - if backup is disabled,
     * the data on the server will be erased.
     * @param enable whether to enable backup
     */
    private void setBackupEnabled(boolean enable) {
        if (mBackupManager != null) {
            try {
                mBackupManager.setBackupEnabled(enable);
            } catch (RemoteException e) {
                mBackup.setChecked(!enable);
                mAutoRestore.setEnabled(!enable);
                return;
            }
        }
        mBackup.setChecked(enable);
        mAutoRestore.setEnabled(enable);

        boolean backupEnabled = false;
        Intent configIntent = null;
        String configSummary = null;

        if (mBackupManager != null) {
            try {
                backupEnabled = mBackupManager.isBackupEnabled();
                String transport = mBackupManager.getCurrentTransport();
                configIntent = mBackupManager.getConfigurationIntent(transport);
                configSummary = mBackupManager.getDestinationString(transport);
            } catch (RemoteException e) {
                // leave it 'false' and disable the UI; there's no backup manager
                mBackup.setEnabled(false);
            }
        }

        final boolean configureEnabled = (configIntent != null) && backupEnabled;
        mConfigure.setEnabled(configureEnabled);
        mConfigure.setIntent(configIntent);
        setConfigureSummary(configSummary);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-278]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (mAutoRestore != null) {
                com.android.settings.MDMSettingsAdapter.getInstance().setAutoRestoreMenu(null,
                        getActivity().getApplicationContext(), mAutoRestore);
            }
        }
        // LGMDM_END
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_backup_reset;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169][ID-MDM-251][ID-MDM-278]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveWipeDateChangeIntent(intent)
                        || MDMSettingsAdapter.getInstance()
                                .receiveBackupRestoreChangeIntent(intent)) {
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

    //[youngju.do] from LGBackup4.30.0, target class name is changed to support wifi transfer.
    private static boolean isLGBackupSupportWifiTransfer(Context context) {

        PackageManager pm = context.getPackageManager();
        if (null != pm) {
            try {
                PackageInfo pkgInfo = pm.getPackageInfo("com.lge.bnr", 0);
                if (null != pkgInfo && (pkgInfo.versionCode >= 43000000)) {
                    return true;
                } else {
                    Log.e("YJDO", "com.lge.bnr versionCode is lower than 4.30.0");
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("YJDO", "NameNotFoundException com.lge.bnr");
                return false;
            }
        }
        return false;
    }

    public static boolean isNotUseNewLGBackup() {
        if ("CMCC".equals(Config.getOperator())
            || "CTC".equals(Config.getOperator())
            || "CUCC".equals(Config.getOperator())
            || "CMO".equals(Config.getOperator())
            || "CTO".equals(Config.getOperator())
            || ("CN".equals(Config.getCountry()) 
                 && "OPEN".equals(Config.getOperator()))) {
            return true;
        }
        return false;
    }
    
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        
        private Map<String, Integer> mDisplayCheck = new HashMap<String, Integer>();
        private IBackupManager mBackupManager;
        int isBackupEnabled = 0;
        int isAutoRestore = 0;
        
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            String targetClass = "";
            String intentaction = "";

            
            mBackupManager = IBackupManager.Stub.asInterface(
                    ServiceManager.getService(Context.BACKUP_SERVICE));
            
            if (mBackupManager != null) {
                try {
                    isBackupEnabled = mBackupManager.isBackupEnabled() ? 1 : 0;
                } catch (RemoteException e) {
                    isBackupEnabled = 0;
                }
            }
            isAutoRestore = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.BACKUP_AUTO_RESTORE, 1);
            
            Log.d("skku", "isBackupEnabled : " + isBackupEnabled + " isBackupEnabled : " + isAutoRestore);
            
            setRemoveVisible(context);

            setSearchIndexData(context, "privacy_settings_main",
                    context.getString(R.string.privacy_settings_title), "main", null,
                    null, "android.settings.PRIVACY_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            
            setCheckBoxType(context);
            
            if (mDisplayCheck.get(CONFIGURE_ACCOUNT) == null) {
                setSearchIndexData(context, CONFIGURE_ACCOUNT,
                        context.getString(R.string.backup_configure_account_title),
                        context.getString(R.string.privacy_settings_title),
                        null, null, 
                        "android.settings.PRIVACY_SETTINGS", null, null, isBackupEnabled,
                        null, null, null, 1, 0);
                Log.d("skku", " configure_account : create");
            }

            if (mDisplayCheck.get(LG_BACKUP_SERVICE) == null) {
                targetClass = "com.lge.bnr.activity.BNRMainActivity";
                if (isLGBackupSupportWifiTransfer(context)) {
                    targetClass = "com.lge.bnr.activity.BNRViaWifiMainActivity";
                }
                
                setSearchIndexData(context, LG_BACKUP_SERVICE,
                        context.getString(R.string.backup_lg_service_title),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.lg_backup_service_description), 
                        null, "android.intent.action.MAIN", targetClass, "com.lge.bnr", 1,
                        null, null, null, 1, 0);
                Log.d("skku", " lg_backup_service : create");
            }
            if (mDisplayCheck.get(SKT_BACKUP) == null) {
                setSearchIndexData(context, SKT_BACKUP,
                        context.getString(R.string.sp_skt_backup_service),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.sp_skt_backup_description), 
                        null, "android.settings.PRIVACY_SETTINGS",
                        null, null, 1, null, null, null, 1, 0);
                Log.d("skku", " skt_backup_service : create");
            }


            if (mDisplayCheck.get(BACKUP_RESET) == null) {
                setSearchIndexData(context, BACKUP_RESET,
                        context.getString(R.string.master_clear_title_ui42),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.master_clear_summary), 
                        null, "android.settings.MASTERCLEAR_SETTINGS",
                        null, null, 1,
                        null, null, null, 1, 0);
                Log.d("skku", "backup_reset : create");
            }
            if (mDisplayCheck.get(BACKUP_RESET_VZW) == null) {

                if (Settings.Secure.getInt(context.getContentResolver(),
                        "verizonwireless_store_demo_mode", 0) == 1) {
                    intentaction = "android.settings.LLKDEMOMODE_SETTINGS";
                } else {
                    intentaction = "android.settings.MASTERCLEAR_SETTINGS";
                }

                setSearchIndexData(context, BACKUP_RESET_VZW,
                        context.getString(R.string.master_clear_title_ui42),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.master_clear_summary), 
                        null, intentaction,
                        null, null, 1,
                        null, null, null, 1, 0);
                Log.d("skku", " backup_reset_vzw : create");
            }
            if (mDisplayCheck.get(RESET_SETTINGS) == null) {
                setSearchIndexData(context, RESET_SETTINGS,
                        context.getString(R.string.reset_settings_title),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.reset_settings_summary), 
                        null, "android.settings.RESET_SETTINGS", 
                        null, null, 1,
                        null, null, null, 1, 0);
                Log.d("skku", "reset_settings : create");
            }
            
            if (mDisplayCheck.get("ciq_toggle_mpcs") == null) {
                setSearchIndexData(context, "ciq_toggle_mpcs",
                        context.getString(R.string.backup_reset_collect_diagnostics),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.backup_reset_collect_diagnostics_summary), 
                        context.getString(R.string.backup_reset_collect_diagnostics_summary), 
                        "android.intent.action.MAIN", "com.carrieriq.tmobile.IQToggle.ui",
                        "com.carrieriq.tmobile.IQToggle", 1,
                        null, null, null, 1, 0);
                Log.d("skku", "ciq_toggle_mpcs : create");
            }
            if (mDisplayCheck.get("ciq_toggle_tmo") == null) {
                setSearchIndexData(context, "ciq_toggle_tmo",
                        context.getString(R.string.backup_reset_collect_diagnostics),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.backup_reset_collect_diagnostics_summary), 
                        context.getString(R.string.backup_reset_collect_diagnostics_summary),
                        "com.carrieriq.tmobile.SUMMARY", "com.tmobile.pr.mytmobile.iqtoggle.ui.OptInSummary",
                        "com.tmobile.pr.mytmobile", 1,
                        null, null, null, 1, 0);
                Log.d("skku", "ciq_toggle_tmo : create");
            }
            return mResult;
        }

        private void setCheckBoxType(Context context) {
            if (mDisplayCheck.get(BACKUP_DATA) == null) {
                setSearchIndexData(context, BACKUP_DATA,
                        context.getString(R.string.backup_data_title),
                        context.getString(R.string.privacy_settings_title),
                        context.getString(R.string.backup_data_summary),
                        context.getString(R.string.backup_data_summary),
                        "android.settings.PRIVACY_SETTINGS", null, null, 1,
                        "CheckBox", null, null, 1, isBackupEnabled);
                Log.d("skku", " backup_data : create");
            }
            if (mDisplayCheck.get(AUTO_RESTORE) == null) {
                setSearchIndexData(context, AUTO_RESTORE,
                        context.getString(R.string.auto_restore_title),
                        context.getString(R.string.privacy_settings_title),
                        null, null,
                        "android.settings.PRIVACY_SETTINGS", null, null, isBackupEnabled,
                        "CheckBox", null, null, 1, isAutoRestore);
                Log.d("skku", " auto_restore : create");
            }
        }

        public void setRemoveVisible(Context context) {
            
            setRemoveCategory(context);
            
            if (!"SKT".equals(Config.getOperator())) {
                mDisplayCheck.put(SKT_BACKUP, 0);
                Log.d("skku", "5remove skt_backcup");
            }
            
            if (UserManager.get(context).hasUserRestriction(
                    UserManager.DISALLOW_FACTORY_RESET)) {
                mDisplayCheck.put(BACKUP_RESET, 0);
                mDisplayCheck.put(BACKUP_RESET_VZW, 0);
                Log.d("skku", "6remove backup_reset");
                Log.d("skku", "7remove backup_reset_vzw");
            }
            if ("VZW".equals(Config.getOperator())) {
                mDisplayCheck.put(BACKUP_RESET, 0);
                Log.d("skku", "8remove backup_reset");
            } else {
                mDisplayCheck.put(BACKUP_RESET_VZW, 0);
                Log.d("skku", "9remove backup_reset_vzw");
            }
            
            if (!Config.getOperator().equals(Config.VZW)
                    || !Utils.isUI_4_1_model(context) && Build.DEVICE.equals("g2")) {
                mDisplayCheck.put(RESET_SETTINGS, 0);
                Log.d("skku", "10remove reset_settings");
            }
            if ("altev".equals(Build.DEVICE)) {
                mDisplayCheck.put(RESET_SETTINGS, 0);
                Log.d("skku", "11remove reset_settings");
            }
            
            if (!Utils.checkPackage(context,
                            "com.carrieriq.tmobile.IQToggle")) {
                mDisplayCheck.put("ciq_toggle_mpcs", 0);
                Log.d("skku", "12remove ciq_toggle_mpcs");
            }
            if (!Utils.checkPackage(context,
                            "com.tmobile.pr.mytmobile")) {
                mDisplayCheck.put("ciq_toggle_tmo", 0);
                Log.d("skku", "13remove ciq_toggle_tmo");
            }
            if ("TMO".equals(Config.getOperator())
                    && "US".equals(Config.getCountry())) {
                mDisplayCheck.put("ciq_toggle_mpcs", 0);
                Log.d("skku", "14remove ciq_toggle_mpcs");
            } else if ("MPCS".equals(Config.getOperator())) {
                mDisplayCheck.put("ciq_toggle_tmo", 0);
                Log.d("skku", "15remove ciq_toggle_tmo");
            }
        }

        private void setRemoveCategory(Context context) {
            
            if (!isNotUseNewLGBackup()) {
                mDisplayCheck.put(LG_BACKUP_SERVICE, 0);
                Log.d("skku", "1remove lg_backup_category");
            }
            
            if (context.getPackageManager().resolveContentProvider(
                    GSETTINGS_PROVIDER, 0) == null) {
                mDisplayCheck.put(BACKUP_DATA, 0);
                mDisplayCheck.put(CONFIGURE_ACCOUNT, 0);
                mDisplayCheck.put(AUTO_RESTORE, 0);
                mDisplayCheck.put("ciq_toggle_mpcs", 0);
                mDisplayCheck.put("ciq_toggle_tmo", 0);
                Log.d("skku", "4remove backup_category");
            }
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
