/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.IntentFilter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.preference.PreferenceCategory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.widget.LockPatternUtils;
//[S][2011.12.30][jaeyoon.hyun] Change Config.java location
import com.android.settings.lgesetting.Config.Config;
//[E][2011.12.30][jaeyoon.hyun] Change Config.java location

import com.lge.mdm.LGMDMManager;
import java.util.ArrayList;
import java.util.List;

//[swgi.kim@lge.com] added always ask checking
import android.provider.Settings.SettingNotFoundException;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import com.android.settings.sdencryption.SDEncryption;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import android.app.admin.DevicePolicyManager;
import com.android.internal.widget.LockPatternUtilsEx;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;
import com.lge.constants.SettingsConstants;
import com.android.settings.lgesetting.Config.Config;

import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGDevEncManager;
/**
 * Gesture lock pattern settings.
 */
public class SecuritySettings extends RestrictedSettingsFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "SecuritySettings";

    private Uri mPackageURI = null;
    private String mPackageType = null;

    private static final String SD_CRYPTO = "persist.security.sdc.enabled";

    //private static final String KEY_SECURITY_CATEGORY = "security_category";
    private static final String KEY_DEVICE_ADMIN_CATEGORY = "device_admin_category";
    private static final String KEY_ADVENCED_CATEGORY = "advanced_security";
    private static final String KEY_OWNER_INFO_SETTINGS = "owner_info_settings";

    //encryption category
    private static final String KEY_ENCRYPTION_CATEGORY = "security_category";
    private static final String KEY_ENCRYPT_PHONE = "security_encryption";
    private static final String KEY_UNENCRYPT_PHONE = "encryption";
    private static final String KEY_ENCRYPT_SDCARD_STORAGE = "sd_card_encryption";

    // phone lock : sungsuplus.kim
    private static final String KEY_PHONE_LOCK = "security_phone_lock";
    private static final int PHONE_LOCK_REQ_CODE = 1;
    private static final int PHONE_LOCK_CONFIRM_REQ_CODE = 2;

    // Misc Settings
    private static final String KEY_SIM_LOCK = "sim_lock";

    private static final String KEY_SHOW_PASSWORD = "show_password";

    private static final String KEY_CREDENTIAL_STORAGE_CATEGORY = "credential_storage_category";
    private static final String KEY_CREDENTIAL_STORAGE_TYPE = "credential_storage_type";
    private static final String KEY_CERTIFICATE_MANAGEMENT = "certificate_management";
    private static final String KEY_TRUSTED_CREDENTIALS = "trusted_credentials";
    private static final String KEY_CERTIFICATION_INSTALLER = "certification_installer";
    private static final String KEY_CLEAR_CREDENTIALS = "clear_credentials";

    private static final String KEY_MANAGE_DEVICE_ADMIN = "manage_device_admin";
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_TOGGLE_VERIFY_APPLICATIONS = "toggle_verify_applications";
    private static final String KEY_SKT_TOGGLE_VERIFY_APPLICATIONS = "toggle_skt_verify_applications";
    private static final String KEY_ACCESS_LOCK = "access_lock";
    private static final String KEY_SIM_LOCK_MENU = "sim_lock_menu";
    private static final String KEY_NOTIFICATION_ACCESS = "manage_notification_access";
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    //[swgi.kim@lge.com] added always ask checking
    private static final String KEY_LIST_TOGGLE_INSTALL_APPLICATIONS = "list_toggle_install_applications";
    private static final String KEY_TRUST_AGENT = "manage_trust_agents";

    private boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;

    // content lock
    private static final String KEY_CONTENT_LOCK = "content_lock";

    private String unknownCheck;
    private String[] mUnknownlist;

    private PackageManager mPM;
    private DevicePolicyManager mDPM;

    private LockPatternUtils mLockPatternUtils;

    private static CheckBox sCB_always;

    private final static int UNKNOWN_LIST_DISABLE = 0;
    private final static int UNKNOWN_LIST_ASK = 1;
    private final static int UNKNOWN_LIST_ENABLE = 2;

    //private final static int UNKNOWN_LIST_DISABLE_SKT = 0;
    private final static int UNKNOWN_LIST_ENABLE_SKT = 1;

    private static final int UNKNOWN_DB_DISABLE = 0;
    private static final int UNKNOWN_DB_ENABLE = 1;
    private static final int UNKNOWN_DB_ALWAYSASK = 2;
    private static final int UNKNOWN_DB_JUSTONE = 2;

    private DoubleTitleListPreference mListToggleAppInstallation;

    public static final int SHOW_PASSWORD_ON = 1;
    public static final int SHOW_PASSWORD_OFF = 0;

    public static final int REQUEST_CODE = -1;

    /**
     * Indicates the encrypt nothing
     */
    public static final int LGMDMENCRYPTION_DISABLED = 0;

    /**
     * Indicates the encrypt only device
     */
    public static final int LGMDMENCRYPTION_DEVICE = 1;

    /**
     * Indicates the encrypt only storage
     */
    public static final int LGMDMENCRYPTION_STORAGE = 2;

    /**
     * Indicates the encrypt device and storage
     */
    public static final int LGMDMENCRYPTION_DEVICE_AND_STORAGE = 3;

    /**
     * Indicates the not use encryption policy
     */
    public static final int LGMDMENCRYPTION_NONE = 4;

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
    private static final int ENCRYPT = 0;
    private static final int DECRYPT = 1;
    // LGMDM_END

    private CheckBoxPreference mShowPassword;

    private KeyStore mKeyStore;

    private PreferenceCategory mCredentialStorageCategory;
    private Preference mCredentialStorageType;
    private Preference mCertificateManagement;
    private Preference mTrustedCredentials;
    private Preference mCertificationInstaller;
    private Preference mClearCredentials;
    private Preference mTrustAgents;
    private Preference mScreenPinning;

    //LGE_UICC_S
    private Preference mSimLockPreference;
    //LGE_UICC_E
    private Preference mDeviceAdmin;
    private CheckBoxPreference mToggleAppInstallation;
    //private DialogInterface mWarnInstallApps;
    private AlertDialog.Builder mWarnInstallApps;
    private CheckBoxPreference mToggleVerifyApps;
    private Preference mSKTVerifyApps;
    private Preference mAccessLock;
    private PreferenceCategory mDeviceAdminCategory;

    // Phone Lock : sungsuplus.kim
    private CheckBoxPreference  mTogglePhoneLock;
    private AlertDialog.Builder mNotePhoneLock;

    /*[S] [2012.2.17] kyochun.shin dual sim menu*/
    private PreferenceScreen mIccLockPreferences = null;
    /*[E] [2012.2.17] kyochun.shin dual sim menu*/
    /*[S] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
    private static int mCheckFullMediaException;
    public static final int FULL_MEDIA_CASE = 0;
    public static final int FULL_CASE = 1;
    public static final int MEDIA_CASE = 2;
    public static final int NORMAL_CASE = 3;
    /*[E] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
    private Preference mNotificationAccess;

    private PreferenceCategory mEncryptionCategory;
    private Preference mEncryptPhone;
    private Preference mUnEncryptPhone;
    private Preference mEncryptSDcardStorage;

    private boolean mIsPrimary;
    private boolean mIsDataEncrypt;
    private boolean mIsSdcardEncrypt;

    private Context mContext;

    private UserManager um;

    //Advanced Menu
    private static final String KEY_SCREEN_PINNING = "screen_pinning_settings";

    private static final int PACKAGE_MANAGER = 0;

    private static final String CONTENT_URI_SEARCH = "content://com.android.settings.search.SearchDBupdateProvider/prefs_index";

    private LGContext mServiceContext = null;
    private LGDevEncManager mLGDevEncManager = null;

    //[S][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "enter Receiver");
                // [2012.12.31][munjohn.kang] added a triple SIM condition
                if (findPreference(KEY_SIM_LOCK) != null && !Utils.isMultiSimEnabled()
                        && !Utils.isTripleSimEnabled()) {
                    updateSimLockEnable();
                }
            }
        }
    };

    //[E][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue

    public SecuritySettings() {
        super(null /* Don't ask for restrictions pin on creation. */);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Credential storage
        um = (UserManager)getActivity().getSystemService(
              getActivity().getApplicationContext().USER_SERVICE);

        mContext = getActivity().getApplicationContext();

        mLockPatternUtils = new LockPatternUtils(getActivity());
        final Intent intent = getActivity().getIntent();
        mPackageURI = intent.getParcelableExtra("packageURI");
        mPackageType = intent.getStringExtra("packageType");

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-162][ID-MDM-308][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addSecuritySettingChangeIntentFilter(filterLGMDM);
            MDMSettingsAdapter.getInstance().addEncryptionSettingChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        mServiceContext = new LGContext(getActivity().getApplicationContext());

        if (LGDevEncManager.getDevEncSupportStatus(getActivity().getApplicationContext())) {
            mLGDevEncManager = (LGDevEncManager)mServiceContext.getLGSystemService(LGContext.LGDEVENC_SERVICE);
        }
        mIsFirst = true;
    }

    private void do_setSDEncyrptionMenu(PreferenceScreen root) {
        SDEncryption sdencryption = new SDEncryption();
        boolean mEncryptionState = false;
 
        if (SystemProperties.get("ro.crypto.state", "0").equals("encrypted")) {
            mEncryptionState = true;
        }

        if (mIsPrimary) {
            if (mDPM != null) {
                if (mEncryptionState) {
                    // The device is currently encrypted.
                    if (mIsDataEncrypt == true) {
                        if (mIsSdcardEncrypt == true) {
                            addPreferencesFromResource(sdencryption.getSDcardEncryptedMenuId());
                            mEncryptionCategory = (PreferenceCategory)root.findPreference(KEY_ENCRYPTION_CATEGORY);
                            mEncryptSDcardStorage = root.findPreference(KEY_ENCRYPT_SDCARD_STORAGE);
                            mEncryptSDcardStorage.setEnabled(true);
                            mEncryptPhone = root.findPreference(KEY_ENCRYPT_PHONE);
                            if (SystemProperties.get(SD_CRYPTO, "0").equals("1")) {
                                mEncryptSDcardStorage.setSummary(
                                        getString(R.string.crypt_keeper_encrypted_summary));
                            } else {
                                mEncryptSDcardStorage.setSummary(
                                        getString(R.string.sp_sdcard_encryption_summary_NORMAL_new));
                            }
                        } else {
                            addPreferencesFromResource(R.xml.security_settings_encrypted);
                            mEncryptionCategory = (PreferenceCategory)root.findPreference(KEY_ENCRYPTION_CATEGORY);
                            mEncryptPhone = root.findPreference(KEY_ENCRYPT_PHONE);
                        }
                        //remove encrypt phone for default encryption
                        if (sdencryption.defaultEncryptionSupport
                               (getActivity().getApplicationContext()) &&
                                mEncryptPhone != null) {
                                if (mIsSdcardEncrypt) {
                                    mEncryptionCategory.removePreference(mEncryptPhone);
                                } else {
                                    getPreferenceScreen().removePreference(mEncryptionCategory);
                                    mEncryptionCategory.removeAll();
                                }
                        }
                        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                            MDMSettingsAdapter.getInstance().setEncryptionSummary(
                                    mIsSdcardEncrypt, ENCRYPT,
                                    getActivity().getApplicationContext(),
                                    root.findPreference(KEY_ENCRYPT_PHONE),
                                    root.findPreference(KEY_ENCRYPT_SDCARD_STORAGE));

                        }
                        // LGMDM_END
                    }
                 } else {
                    // This device supports encryption but isn't encrypted.
                    if (mIsDataEncrypt == true) {
                        if (mIsSdcardEncrypt == true) {
                            addPreferencesFromResource(sdencryption.getSDcardUnencryptedMenuId());
                            mEncryptionCategory = (PreferenceCategory)root.findPreference(KEY_ENCRYPTION_CATEGORY);
                            mEncryptSDcardStorage = root.findPreference(KEY_ENCRYPT_SDCARD_STORAGE);
                            mEncryptSDcardStorage.setEnabled(true);
                            mUnEncryptPhone = root.findPreference(KEY_UNENCRYPT_PHONE);
                            if (Utils.isSupportUIV4_2()) {
                                mUnEncryptPhone.setSummary(R.string.sp_security_encrypt_phone_summary_NORMAL_changed_version4_2);
                            }
                            if (SystemProperties.get(SD_CRYPTO, "0").equals("1")) {
                                mEncryptSDcardStorage.setSummary(
                                        getString(R.string.crypt_keeper_encrypted_summary));
                            } else {
                                mEncryptSDcardStorage.setSummary(
                                        getString(R.string.sp_sdcard_encryption_summary_NORMAL_new));
                            }
                        } else {
                            addPreferencesFromResource(R.xml.security_settings_unencrypted);
                            mEncryptionCategory = (PreferenceCategory)root.findPreference(KEY_ENCRYPTION_CATEGORY);
                            mUnEncryptPhone = root.findPreference(KEY_UNENCRYPT_PHONE);
                        }
                        //remove encrypt phone for default encryption
                        if (sdencryption.defaultEncryptionSupport
                               (getActivity().getApplicationContext()) &&
                                mUnEncryptPhone != null) {
                              if (mIsSdcardEncrypt) {
                                    mEncryptionCategory.removePreference(mUnEncryptPhone);
                              } else {
                                    getPreferenceScreen().removePreference(mEncryptionCategory);
                                    mEncryptionCategory.removeAll();
                              }
                        }
                        // CAPP_MDM [a1-mdm-dev@lge.com] Require Device Encryption
                        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                            MDMSettingsAdapter.getInstance().setEncryptionSummary(
                                    mIsSdcardEncrypt, DECRYPT,
                                    getActivity().getApplicationContext(),
                                    root.findPreference(KEY_UNENCRYPT_PHONE),
                                    root.findPreference(KEY_ENCRYPT_SDCARD_STORAGE));
                        }
                        // LGMDM_END
                    }
                }
            }
            /*[S] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
            if (CheckFullMediaExceptionService() != NORMAL_CASE && mIsSdcardEncrypt
                    && mIsDataEncrypt) {
                if (SDEncryption_checkEnableProperty() == false) { //enable
                    root.findPreference("sd_card_encryption").setFragment(
                            "com.android.settings.sdencryption.SDEncryptionHelp_Extension");
                    Log.i(TAG, "SDEncryptionHelp_Extension");
                } else { //disable
                    root.findPreference("sd_card_encryption").setFragment(
                            "com.android.settings.sdencryption.SDEncryptionSettings_Extension");
                    Log.i(TAG, "SDEncryptionSettings_Extension");
                }
            }
            /*[E] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
        }
    }

    private void do_setupSIMwithIccLock(PreferenceScreen root) {
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            boolean mIs_Ctc_Cn = "CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator());
            mIccLockPreferences = getPreferenceManager()
                    .createPreferenceScreen(getActivity());
            if (true == mIs_Ctc_Cn) {
                mIccLockPreferences.setTitle(R.string.uim_sim_lock_settings_category);
            } else {
                mIccLockPreferences.setTitle(R.string.sim_lock_settings_category);
            }
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.setClassName("com.lge.networksettings", "com.lge.networksettings.msim.MSimSetupSimCardLockSettings");
            intent.putExtra("PACKAGE", "com.android.phone");
            intent.putExtra("TARGET_CLASS", "com.android.phone.IccLockSettings");
            mIccLockPreferences.setIntent(intent);
            PreferenceCategory iccLockCat = new PreferenceCategory(getActivity());
            if (true == mIs_Ctc_Cn) {
                iccLockCat.setTitle(R.string.uim_sim_lock_settings_title);
            } else {
                iccLockCat.setTitle(R.string.sim_lock_settings_title);
            }
            root.addPreference(iccLockCat);
            iccLockCat.addPreference(mIccLockPreferences);
            //root.removePreference(root.findPreference(KEY_SIM_LOCK));
        }
    }

    private void do_setSIMLockMenu(PreferenceScreen root) {
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
            updateSimLockEnable();
        }
        // [2012.12.31][munjohn.kang] added a triple SIM condition
        if (!Utils.isMultiSimEnabled() && !Utils.isTripleSimEnabled()) {
            // [S][2011.11.25][jin850607.hong@lge.com][Kor] change menu string
            if ((Config.LGU).equals(Config.getOperator())
                    || (Config.KT).equals(Config.getOperator())
                    || (Config.SKT).equals(Config.getOperator())) {
                root.findPreference(KEY_SIM_LOCK).setTitle(getResources()
                        .getString(R.string.sp_usim_lock_settings_category_NORMAL));
                root.findPreference(KEY_SIM_LOCK_MENU).setTitle(getResources()
                        .getString(R.string.sp_usim_lock_settings_title_NORMAL));
            }
            //LGE_UICC_S
            mSimLockPreference = root.findPreference(KEY_SIM_LOCK_MENU);
            //LGE_UICC_E
            // [E][2011.11.25][jin850607.hong@lge.com][Kor] change menu string

            // Do not display SIM lock for devices without an Icc card
            TelephonyManager tm = TelephonyManager.getDefault();
            if (!mIsPrimary || !tm.hasIccCard()) {
                root.removePreference(root.findPreference(KEY_SIM_LOCK));
            } else {
                // Disable SIM lock if sim card is missing or unknown
                if ((TelephonyManager.getDefault().getSimState() ==
                        TelephonyManager.SIM_STATE_ABSENT) ||
                        (TelephonyManager.getDefault().getSimState() ==
                        TelephonyManager.SIM_STATE_UNKNOWN)) {
                    root.findPreference(KEY_SIM_LOCK).setEnabled(false);
                }

                if (Config.SPR.equals(Config.getOperator())) {
                    root.removePreference(root.findPreference(KEY_SIM_LOCK));
                }

                if ("USC".equals(Config.getOperator())) {
                    if (SystemProperties.get("ril.card_operator", "").equals("USC4G") ||
                        SystemProperties.get("ril.card_operator", "").equals("USC3G")) {
                        root.removePreference(root.findPreference(KEY_SIM_LOCK));
                    }
                }
            }

        } else {
            root.removePreference(root.findPreference(KEY_SIM_LOCK));
        }
    }

    private void do_setCredentialStorageMenu(PreferenceGroup credentialStorageCategory) {
        mCertificateManagement = (Preference)findPreference(KEY_CERTIFICATE_MANAGEMENT);
        mTrustedCredentials = (Preference)findPreference(KEY_TRUSTED_CREDENTIALS);
        mCertificationInstaller = (Preference)findPreference(KEY_CERTIFICATION_INSTALLER);
        mClearCredentials = (Preference)findPreference(KEY_CLEAR_CREDENTIALS);
        if (Utils.isSupportUIV4_2()) {
            credentialStorageCategory.removePreference(mTrustedCredentials);
            credentialStorageCategory.removePreference(mCertificationInstaller);
            credentialStorageCategory.removePreference(mClearCredentials);
        } else {
            credentialStorageCategory.removePreference(mCertificateManagement);
        }

        mKeyStore = KeyStore.getInstance(); // needs to be initialized for onResume()
        mCredentialStorageType = (Preference)findPreference(KEY_CREDENTIAL_STORAGE_TYPE);
        if (mIsOwner && !um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
            credentialStorageCategory.addPreference(mCredentialStorageType);

            final int storageSummaryRes =
                    mKeyStore.isHardwareBacked() ? R.string.credential_storage_type_hardware
                            : R.string.credential_storage_type_software;
            mCredentialStorageType.setSummary(storageSummaryRes);
        } else {
            getPreferenceScreen().removePreference(credentialStorageCategory);
        }
    }

    private void do_setShowPasswordMenu(PreferenceScreen root) {
        mShowPassword = (CheckBoxPreference)root.findPreference(KEY_SHOW_PASSWORD);
        if (mShowPassword != null) {
            mShowPassword.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.TEXT_SHOW_PASSWORD, SHOW_PASSWORD_ON) != 0);
        }
    }

    private void do_setInstallAppMenu(PreferenceGroup deviceAdminCategory) {
        mDeviceAdmin = (Preference)findPreference(KEY_MANAGE_DEVICE_ADMIN);
        deviceAdminCategory.addPreference(mDeviceAdmin);
        mToggleAppInstallation = (CheckBoxPreference)
                findPreference(KEY_TOGGLE_INSTALL_APPLICATIONS);
        deviceAdminCategory.addPreference(mToggleAppInstallation);
        mListToggleAppInstallation = (DoubleTitleListPreference)
                findPreference(KEY_LIST_TOGGLE_INSTALL_APPLICATIONS);
        deviceAdminCategory.addPreference(mListToggleAppInstallation);
        if ("KR".equals(Config.getCountry())) {
            mListToggleAppInstallation.setTitle(R.string.install_applications_kr);
            mListToggleAppInstallation.setMainTitle(getString(R.string.install_applications_kr));
            mListToggleAppInstallation.setSubTitle(
                    getString(
                    R.string.sp_install_unknown_applications_NORMAL));

            mListToggleAppInstallation.setOnPreferenceChangeListener(this);

            String[] unkownvalue = null;

            unkownvalue = getResources().getStringArray(R.array.unkown_source_value_for_SKT);

            int length = unkownvalue.length;
            mUnknownlist = new String[length];

            for (int i = 0; i < length; i++) {
                //getResources().getString(R.string.unknown_sources_disable + i);
                mUnknownlist[i] = unkownvalue[i];
                Log.i(TAG, "mListToggleAppInstallation " + mUnknownlist[i]);
            }

            mListToggleAppInstallation.setEntries(mUnknownlist);
            mListToggleAppInstallation.setEntryValues(mUnknownlist);

            if (UNKNOWN_DB_ENABLE == isNonMarketAppsAllowedValue()) {
                    mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ENABLE_SKT);
                    mListToggleAppInstallation.setSummary(mUnknownlist[UNKNOWN_LIST_ENABLE_SKT]);
            } else if (UNKNOWN_DB_ALWAYSASK == isNonMarketAppsAllowedValue()) {
                    mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ASK);
                    mListToggleAppInstallation.setSummary(mUnknownlist[UNKNOWN_LIST_ASK]);
            } else {
                mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_DISABLE);
                mListToggleAppInstallation.setSummary(mUnknownlist[UNKNOWN_LIST_DISABLE]);
            }

            try {
                unknownCheck = mUnknownlist[isNonMarketAppsAllowedValue()];
            } catch (IndexOutOfBoundsException e) {
                Log.i(TAG, "IndexOutOfBoundsException - mUnknownlist[]");
                unknownCheck = mUnknownlist[UNKNOWN_DB_DISABLE];
            }
        }
        checkSupportInstallAppMenu(deviceAdminCategory);
    }

    private void checkSupportInstallAppMenu(PreferenceGroup deviceAdminCategory) {
        if ("KR".equals(Config.getCountry())) {
            deviceAdminCategory.removePreference(mToggleAppInstallation);
            if (!mIsPrimary) {
                deviceAdminCategory.removePreference(mListToggleAppInstallation);
            } else {
                if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
                || um.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS)) {
                    mListToggleAppInstallation.setEnabled(false);
                }
            }
        } else {
            deviceAdminCategory.removePreference(mListToggleAppInstallation);
            if (!mIsPrimary) {
                deviceAdminCategory.removePreference(mToggleAppInstallation);
            } else {
                if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
                || um.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS)) {
                    mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
                    mToggleAppInstallation.setEnabled(false);
                } else {
                   mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
                }
            }
        }
   }

    private void do_setVerifyAppsMenu(PreferenceGroup deviceAdminCategory) {
        // Package verification, only visible to primary user and if enabled
        mToggleVerifyApps = (CheckBoxPreference)findPreference(KEY_TOGGLE_VERIFY_APPLICATIONS);
        mDeviceAdminCategory.addPreference(mToggleVerifyApps);
        mSKTVerifyApps = (Preference)findPreference(KEY_SKT_TOGGLE_VERIFY_APPLICATIONS);
        deviceAdminCategory.addPreference(mSKTVerifyApps);
        if (showVerifierSetting(mContext)) {
            if (isVerifierInstalled(mContext)) {
                if (Utils.isSupportSKTVerifyApps()) {
                    mDeviceAdminCategory.removePreference(mToggleVerifyApps);
                } else {
                    mToggleVerifyApps.setChecked(isVerifyAppsEnabled());
                    mDeviceAdminCategory.removePreference(mSKTVerifyApps);
                }
            } else {
                if (Utils.isSupportSKTVerifyApps()) {
                    mDeviceAdminCategory.removePreference(mToggleVerifyApps);
                    mSKTVerifyApps.setEnabled(false);
                } else {
                    mDeviceAdminCategory.removePreference(mSKTVerifyApps);
                    mToggleVerifyApps.setChecked(false);
                    mToggleVerifyApps.setEnabled(false);
                }
            }
        } else {
            mDeviceAdminCategory.removePreference(mToggleVerifyApps);
            mDeviceAdminCategory.removePreference(mSKTVerifyApps);
        }
    }

    private void do_setAccessLockMenu(PreferenceGroup deviceAdminCategory) {
        mAccessLock = (Preference)findPreference(KEY_ACCESS_LOCK);
        mDeviceAdminCategory.addPreference(mAccessLock);
        if (!Utils.isChinaOperator()) {
            if (mDeviceAdminCategory != null) {
                mDeviceAdminCategory.removePreference(mAccessLock);
            } else {
                getPreferenceScreen().removePreference(mAccessLock);
            }
        }
    }

    private void do_setNotificationAccessMenu(PreferenceGroup deviceAdminCategory) {
        mNotificationAccess = findPreference(KEY_NOTIFICATION_ACCESS);
        if (mNotificationAccess != null) {
            final int total = NotificationAccessSettings.getListenersCount(mPM);
            if (total == 0) {
                if (mDeviceAdminCategory != null) {
                    mDeviceAdminCategory.removePreference(mNotificationAccess);
                }
            } else {
                final int n = getNumEnabledNotificationListeners();
                if (n == 0) {
                    mNotificationAccess.setSummary(getResources().getString(
                            R.string.manage_notification_access_summary_zero));
                } else {
                    mNotificationAccess.setSummary(String.format(getResources().getQuantityString(
                            R.plurals.manage_notification_access_summary_nonzero,
                            n, n)));
                }
            }
        }
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.security_settings);
        root = getPreferenceScreen();

        // Add options for device encryption
        mPM = getActivity().getPackageManager();
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;

        if (!mIsPrimary) {
            // Rename owner info settings
            Preference ownerInfoPref = findPreference(KEY_OWNER_INFO_SETTINGS);
            if (ownerInfoPref != null) {
                ownerInfoPref.setTitle(R.string.user_info_settings_title);
            }
        }

        if ("ATT".equals(Config.getOperator()) &&
                Utils.isUI_4_1_model(getActivity())) {
            addPreferencesFromResource(R.xml.security_settings_lock_screen_att);
        }

        if (SystemProperties.getBoolean("ro.kddi.privacyguard", false) == true) {
            addPreferencesFromResource(R.xml.security_settings_privacy);
        }


        initPrivacylockMenu();

        SDEncryption sdencryption = new SDEncryption();
        do_setSDEncyrptionMenu(root);
        if (sdencryption.defaultEncryptionSupport(getActivity().getApplicationContext())) {
            addPreferencesFromResource(R.xml.security_settings_phone_lock);
            mTogglePhoneLock = (CheckBoxPreference)findPreference(KEY_PHONE_LOCK);
            mTogglePhoneLock.setChecked(isPhoneLockEnabled());
        }

        do_setupSIMwithIccLock(root);

        // Append the rest of the settings
        addPreferencesFromResource(R.xml.security_settings_misc);
        do_setSIMLockMenu(root);
        // Application install
        mDeviceAdminCategory = (PreferenceCategory)
                root.findPreference(KEY_DEVICE_ADMIN_CATEGORY);
        if ("KR".equals(Config.getCountry())) {
            mDeviceAdminCategory.setTitle(R.string.device_admin_title_kr);
        }
        do_setShowPasswordMenu(root);
        do_setInstallAppMenu(mDeviceAdminCategory);
        do_setVerifyAppsMenu(mDeviceAdminCategory);
        mCredentialStorageCategory = (PreferenceCategory)
                root.findPreference(KEY_CREDENTIAL_STORAGE_CATEGORY);
        do_setCredentialStorageMenu(mCredentialStorageCategory);
        do_setAdvancedMenu(root);

        do_setAccessLockMenu(mDeviceAdminCategory);
        if (Utils.isUI_4_1_model(getActivity())) {
            do_setNotificationAccessMenu(mDeviceAdminCategory);
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-162][ID-MDM-308]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setUnknownSourceEnableMenu(mToggleAppInstallation);
            MDMSettingsAdapter.getInstance().setPasswordTypingVisibleMenu(mShowPassword);
            MDMSettingsAdapter.getInstance().setUnknownSourceEnableMenu(mListToggleAppInstallation);
        }
        // LGMDM_END

        //2013-05-31 [swgi.kim@lge.com] [GK/CMCC] change Unknown source summary string [START]

        if (("CN".equals(Config.getCountry()))
                && mToggleAppInstallation != null) {
            mToggleAppInstallation.setSummary(R.string.sp_install_unkown_applications_NORMAL);
            mToggleAppInstallation.setSummaryOff(R.string.sp_install_unkown_applications_NORMAL);
            mToggleAppInstallation.setSummaryOn(R.string.sp_install_unkown_applications_NORMAL);
        }
        //2013-05-31 [swgi.kim@lge.com] [GK/CMCC] change Unknown source summary string [END]
        do_setTrustAgentsMenu(root);

        return root;
    }

    private void initPrivacylockMenu() {
        if (!PrivacyLockHelper.isPrivacyLockSupport(getActivity())) {
            return;
        }

        String formattedSummary = PrivacyLockHelper.getFormattedSummary(getActivity());

        if (!TextUtils.isEmpty(formattedSummary)) {
            addPreferencesFromResource(R.xml.security_settings_privacy_lock);
            Preference contentLockPref = findPreference(KEY_CONTENT_LOCK);

            if (contentLockPref != null) {
                contentLockPref.setSummary(formattedSummary);
            }
        }
    }

    private void do_setTrustAgentsMenu(PreferenceScreen root) {
        // Trust Agent preferences
        PreferenceGroup securityCategory = (PreferenceGroup)
                root.findPreference(KEY_ADVENCED_CATEGORY);
        if (securityCategory != null) {
            final boolean hasSecurity = mLockPatternUtils.isSecure();
            mTrustAgents = root.findPreference(KEY_TRUST_AGENT);
            if (mTrustAgents != null && !hasSecurity) {
                mTrustAgents.setEnabled(false);
                mTrustAgents.setSummary(R.string.disabled_because_no_backup_security);
            }
        }
    }

    private void do_setAdvancedMenu(PreferenceScreen root) {
        mScreenPinning = root.findPreference(KEY_SCREEN_PINNING);
        if (Settings.System.getInt(getContentResolver(),
                Settings.System.LOCK_TO_APP_ENABLED, 0) != 0) {
            mScreenPinning.setSummary(
                    getResources().getString(R.string.sp_on_NORMAL));
        }
    }

    private boolean isSDcardSupport() {
        if (SystemProperties.get("ro.build.characteristics").contains("nosdcard") ||
        !Config.getFWConfigBool(getActivity().getApplicationContext(), com.lge.R.bool.config_sd_encrypt,
        "com.lge.R.bool.config_sd_encrypt")) {
              return false;
        }
        return true;
    }

    // sungsuplus.kim phone lock
    private boolean isPhoneLockEnabled() {
       return Settings.Global.getInt(getContentResolver(),
          SettingsConstants.Global.SECURITY_PHONE_LOCK, 1) == 1 ? true : false;
    }

    private boolean isNonMarketAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    //[swgi.kim@lge.com] added always ask checking
    private int isNonMarketAppsAllowedValue() {
        int value;

        try {
            value = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INSTALL_NON_MARKET_APPS);
        } catch (SettingNotFoundException e) {
            value = 0;
        }

        Log.d(TAG, "isNonMarketAppsAllowedList value = " + value);

        return value;
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        if (um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)) {
            return;
        }
        // Change the system setting
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS,
                enabled ? 1 : 0);
    }
    private void setPhoneLockEnabled(boolean enabled) {
        // Change the Phone lock setting : sungsuplus.kim
        Settings.Global.putInt(getContentResolver(),
              SettingsConstants.Global.SECURITY_PHONE_LOCK, enabled == true ? 1 : 0);
    }

    //[swgi.kim@lge.com] added always ask checking
    private void setNonMarketAppsAllowed(int allowed) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, allowed);
    }

    private boolean isVerifyAppsEnabled() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_ENABLE, 1) > 0;
    }

    private static boolean isVerifierInstalled(Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent verification = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
        verification.setType(PACKAGE_MIME_TYPE);
        verification.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final List<ResolveInfo> receivers = pm.queryBroadcastReceivers(verification, 0);
        return (receivers.size() > 0) ? true : false;
    }

    private static boolean showVerifierSetting(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.PACKAGE_VERIFIER_SETTING_VISIBLE, 1) > 0;
    }

// phone lock dialog sungsuplus
    private void notePhoneLock() {
        // TODO: DialogFragment?
        //[swgi.kim@lge.com] added always ask checking [START]
        View dialogView = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
            mNotePhoneLock = new AlertDialog.Builder(getActivity());
            dialogView = inflater.inflate(R.layout.security_phonelock_noti_dialog, null);
            mNotePhoneLock.setView(dialogView);

            mNotePhoneLock.setNegativeButton(getResources().getString(R.string.cancel),
                       new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           mTogglePhoneLock.setChecked(false);
                           setPhoneLockEnabled(false);
                       }});
            mNotePhoneLock.setTitle(getResources().getString(R.string.sp_security_phone_lock_popup_title));
            mNotePhoneLock.setPositiveButton(getResources().getString(R.string.dlg_ok),
                      new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                               if (!runKeyguardConfirmation(PHONE_LOCK_CONFIRM_REQ_CODE)) {
                                    Intent intent = new Intent(getActivity(),
                                                ChooseLockGeneric.class);
                                    startActivityForResult(intent, PHONE_LOCK_REQ_CODE);
                                    dialog.dismiss();
                               } else {
                                   mTogglePhoneLock.setChecked(true);
                                   setPhoneLockEnabled(true);
                               }
                        }});
        mNotePhoneLock.setCancelable(true);
        mNotePhoneLock.show();
    }
    private void warnAppInstallation() {
        // TODO: DialogFragment?
        //[swgi.kim@lge.com] added always ask checking [START]
        View dialogView = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mWarnInstallApps = new AlertDialog.Builder(getActivity());
        dialogView = inflater.inflate(R.layout.security_unknownapp_noti_dialog, null);
        mWarnInstallApps.setView(dialogView);

        TextView message = (TextView)dialogView.findViewById(R.id.message);
        sCB_always = (CheckBox)dialogView.findViewById(R.id.cb_always);

        sCB_always.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Click sound effect
            }
        });

        if ((Config.LGU).equals(Config.getOperator())) {
            sCB_always.setChecked(true);
        }
        else {
            sCB_always.setChecked(false);
        }

        if ("KR".equals(Config.getCountry())) {
        	mWarnInstallApps.setTitle(getResources().getString(R.string.install_applications_kr));
            mWarnInstallApps.setPositiveButton(getResources()
                .getString(R.string.dlg_ok), mOnClickListener);
        } else {
            mWarnInstallApps.setNegativeButton(getResources().getString(R.string.cancel), null);
            mWarnInstallApps.setTitle(getResources().getString(R.string.install_applications));
            mWarnInstallApps.setPositiveButton(getResources()
                    .getString(R.string.dlg_ok), mOnClickListener);
            sCB_always.setVisibility(View.GONE);
            Log.i(TAG, "message : " + getResources().getString(R.string.sp_unkown_popup_NORMAL));
        }
        message.setText(getResources().getString(R.string.install_all_warning));
        mWarnInstallApps.setIconAttribute(android.R.attr.alertDialogIcon);
        mWarnInstallApps.setOnCancelListener(mOnCancelListener);
        mWarnInstallApps.setCancelable(true);
        mWarnInstallApps.show();
    }

    DialogInterface.OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            Log.i(TAG, "secu = onClick");
            if ("KR".equals(Config.getCountry())) {
                Log.i(TAG, "mWarnInstallApps");
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mListToggleAppInstallation != null) {
                            Log.i(TAG, "checkbox :" + sCB_always.isChecked());
							mListToggleAppInstallation.setTitle(R.string.install_applications_kr);
                            mListToggleAppInstallation.setSummary(R.string.unknown_sources_enable_kr);
                            mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ENABLE_SKT);
                            if (null != sCB_always && sCB_always.isChecked()) {
                                Log.i(TAG, "checkbox - check");
                                setNonMarketAppsAllowed(UNKNOWN_DB_JUSTONE);
                            }
                            else {
                                Log.i(TAG, "checkbox - uncheck");
                                setNonMarketAppsAllowed(UNKNOWN_DB_ENABLE);
                            }
                    }
                }
            } else {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    setNonMarketAppsAllowed(true);
                    if (mToggleAppInstallation != null) {
                        mToggleAppInstallation.setChecked(true);
                    }
                }
            }

            if (mPackageType != null) {
                Intent launchPackageInstallerIntent = new Intent(android.content.Intent.ACTION_VIEW);
                launchPackageInstallerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchPackageInstallerIntent.setDataAndType(mPackageURI, mPackageType);
                startActivityForResult(launchPackageInstallerIntent, PACKAGE_MANAGER);
            }
        }
    };

    DialogInterface.OnCancelListener mOnCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            if (UNKNOWN_DB_DISABLE == isNonMarketAppsAllowedValue()) {
                mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_DISABLE);
            }
            else if (UNKNOWN_DB_ENABLE == isNonMarketAppsAllowedValue()) {
                if ("KR".equals(Config.getCountry())) {
                    mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ENABLE_SKT);
                }
                else {
                    mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ENABLE);
                }

            }
            else {
                mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ASK);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.create().dismiss();
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-162][ID-MDM-308][ID-MDM-28][ID-MDM-29]
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
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        //[S][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue
        final IntentFilter filter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        getActivity().registerReceiver(mSimStateReceiver, filter);
        //[E][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue

        mIsDataEncrypt = Config.getFWConfigBool(getActivity().getApplicationContext(), com.lge.R.bool.config_data_encrypt,
                                                "com.lge.R.bool.config_data_encrypt");
        
        mIsDataEncrypt |= Utils.getDefaultBooleanValue(getActivity(),
                "com.lge.internal",
                "config_data_encrypt");
        mIsSdcardEncrypt = isSDcardSupport();
        //getResources().getBoolean(com.lge.R.bool.config_sd_encrypt);

        Log.d(TAG, "onResume data_encrypt =" + mIsDataEncrypt + "  sd_encrypt = "
                + mIsSdcardEncrypt);

        createPreferenceHierarchy();

        KeyStore.State state = KeyStore.getInstance().state();
        if (mClearCredentials != null) {
            mClearCredentials.setEnabled(state != KeyStore.State.UNINITIALIZED);
        }

        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent().getBooleanExtra("perform",
                false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            startResult();
            mIsFirst = false;
        }
        updateSecuritySearchDB();
    }

    //[swgi.kim@lge.com] added always ask checking
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Log.d(TAG, "onPreferenceChange = newValue" + newValue);

        if (preference == mListToggleAppInstallation) {
            unknownCheck = (String)newValue;

            //            if ( "Disable".equals(newValue)) {
            if (true == newValue.equals(getString(R.string.unknown_sources_disable))) {
                setNonMarketAppsAllowed(UNKNOWN_DB_DISABLE);
                mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_DISABLE);
                mListToggleAppInstallation.setSummary(R.string.unknown_sources_disable);
            } else {
                if (true == unknownCheck.equals(getString(R.string.unknown_sources_enable))) {
                    if ("KR".equals(Config.getCountry())) {
                        mListToggleAppInstallation.setTitle(R.string.install_applications_kr);
                    } else {
                        mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ENABLE);
                        setNonMarketAppsAllowed(UNKNOWN_DB_ENABLE);
                        mListToggleAppInstallation.setSummary(R.string.unknown_sources_enable);
                    }
                } else {
                    if ("KR".equals(Config.getCountry())) {
                       mListToggleAppInstallation.setTitle(R.string.install_applications_kr);
                    } else {
                        mListToggleAppInstallation.setValueIndex(UNKNOWN_LIST_ASK);
                        setNonMarketAppsAllowed(UNKNOWN_DB_ALWAYSASK);
                        mListToggleAppInstallation.setSummary(R.string.unknown_sources_always_ask);
                    }
                }
                warnAppInstallation();
            }
            //mListToggleAppInstallation.setValueIndex(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if (preference == mTogglePhoneLock)
        {
                 if (mTogglePhoneLock.isChecked()) {
                 mTogglePhoneLock.setChecked(false);
                 notePhoneLock();
             } else {
                 mTogglePhoneLock.setChecked(false);
                 setPhoneLockEnabled(false);
                 mLockPatternUtils.clearEncryptionPassword();
             }
        }
        else if (preference == mShowPassword)
        {
            Settings.System.putInt(getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD,
               mShowPassword.isChecked() ? 1 : 0);
        }
        else if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
        } else if (KEY_TOGGLE_VERIFY_APPLICATIONS.equals(key)) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.PACKAGE_VERIFIER_ENABLE,
                    mToggleVerifyApps.isChecked() ? 1 : 0);
        }
        //[swgi.kim@lge.com] added always ask checking
        else if (preference == mListToggleAppInstallation) {
            //warnAppInstallation();
        } else if (preference == mSimLockPreference) {
            //LGE_UICC_S
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            if ("KR".equals(Config.getCountry())) {
                intent.setClassName("com.android.settings",
                        "com.android.settings.IccLockSettings_kr");
            } else {
                intent.setClassName("com.android.settings", "com.android.settings.IccLockSettings");
            }
            startActivity(intent);
            //LGE_UICC_E
        } else if (preference.equals(mAccessLock)) {
            startActivity(new Intent("lge.settings.ACCESS_LOCK_SETTINGS"));
        } else if (preference.equals(mSKTVerifyApps)) {
            startFragment(this,
                    "com.android.settings.SKTVerifyAppsPreference",
                    REQUEST_CODE,
                    null,
                    R.string.verify_applications_title_skt);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    //[S][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mSimStateReceiver);
    }

    //[E][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue

    private boolean isToggled(Preference pref) {
        return ((CheckBoxPreference)pref).isChecked();
    }

    /**
     * see confirmPatternThenDisableAndClear
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        createPreferenceHierarchy();
        switch (requestCode) {
            case PACKAGE_MANAGER:
                finish();
                break;
            case PHONE_LOCK_REQ_CODE:
                        if (resultCode == Activity.RESULT_OK) {
                        LockPatternUtilsEx lockPatternUtils = new LockPatternUtilsEx(getActivity());
                        int quality = lockPatternUtils.getKeyguardStoredPasswordQuality();

                        if (quality < DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
                            mTogglePhoneLock.setChecked(false);
                            setPhoneLockEnabled(false);
                        } else {
                            mTogglePhoneLock.setChecked(true);
                            setPhoneLockEnabled(true);
                        }
                     } else if (resultCode == Activity.RESULT_CANCELED) {
                       mTogglePhoneLock.setChecked(false);
                       setPhoneLockEnabled(false);
                     }
                break;
            case PHONE_LOCK_CONFIRM_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    int type = data.getIntExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE, -1);
                    String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                    mLGDevEncManager.activatePhonelock(type , password);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                       mTogglePhoneLock.setChecked(false);
                       setPhoneLockEnabled(false);
                     }
                break;
            default:
                break;
        }
    }

    //[S][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue
    public void updateSimLockEnable() {
        int currentSimStatus = TelephonyManager.getDefault().getSimState();
        if (currentSimStatus != TelephonyManager.SIM_STATE_READY) {
            if (Config.DCM.equals(Config.getOperator())) {
                if (!"PERM_DISABLED".equals(SystemProperties.get("gsm.sim.state"))) {
                    Log.d("hong", "PERM_ENABLED");
                    findPreference(KEY_SIM_LOCK).setEnabled(false);
                } else {
                    Log.d("hong", "PERM_DISABLED");
                    findPreference(KEY_SIM_LOCK).setEnabled(true);
                }
            } else {
                findPreference(KEY_SIM_LOCK).setEnabled(false);
                Log.i(TAG, "currentSimStatus : " + currentSimStatus);
                Log.i(TAG, "TelephonyManager.SIM_STATE_READY : " + TelephonyManager.SIM_STATE_READY);
                Log.i(TAG, "KEY_SIM_LOCK : false");
            }
        } else {
            findPreference(KEY_SIM_LOCK).setEnabled(true);
            Log.d(TAG, "KEY_SIM_LOCK : true");
        }
    }

    //[E][2012.8.3][changyu0218.lee] Fix Sim card lock disable issue
    @Override
    protected int getHelpResource() {
        return R.string.help_url_security;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-162][ID-MDM-308][ID-MDM-28][ID-MDM-29]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveSecuritySettingChangeIntent(intent)) {
                    getActivity().finish();
                }
                if (MDMSettingsAdapter.getInstance()
                        .receiveSecurityEncryptionChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };

    // LGMDM_END

    /*[S] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/
    public int CheckFullMediaExceptionService() {
        mCheckFullMediaException = NORMAL_CASE;
        //SystemProperties.getBoolean("ro.lge.capp_sdencryption_full", false);
        //SystemProperties.getBoolean("ro.lge.capp_sdencryption_media", false);
        Boolean mFull_enable = false;
        Boolean mMedia_enable = false;

        if (isSDcardSupport()) {
            mFull_enable = true;
            mMedia_enable = true;
        }
        if ((mFull_enable == true) && (mMedia_enable == true)) {
            mCheckFullMediaException = FULL_MEDIA_CASE;
        } else if (mFull_enable == true) {
            mCheckFullMediaException = FULL_CASE;
        } else if (mMedia_enable == true) {
            mCheckFullMediaException = MEDIA_CASE;
        } else {
            mCheckFullMediaException = NORMAL_CASE;
        }
        Log.i(TAG, "CheckFullMediaExceptionService" + mCheckFullMediaException);
        return mCheckFullMediaException;
    }

    public static int getFullMediaExceptionOption() {
        return mCheckFullMediaException;
    }

    private int getNumEnabledNotificationListeners() {
        final String flat = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
        if (flat == null || "".equals(flat)) {
            return 0;
        }
        final String[] components = flat.split(":");
        int count = components.length;
        for (int i = 0; i < components.length; i++) {
            Log.i(TAG, "components :" + components[i]);
            String[] name = components[i].split("/");
            Log.i(TAG, "name :" + name[1]);
            for (int j = 0; j < NotificationAccessSettings.HIDE_ITEM.length; j++) {
                if (NotificationAccessSettings.HIDE_ITEM[j].equals(name[1])) {
                    count--;
                }
            }
        }
        return count;
    }

    public static boolean SDEncryption_checkEnableProperty() {
        boolean status = false;
        if (SystemProperties.get(SD_CRYPTO, "0").equals("1")) {
            status = true;
        }
        return status;
    }

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        // 1.  Confirm that we have a sufficient PIN/Password to continue

        if (request == PHONE_LOCK_CONFIRM_REQ_CODE) {
            setPhoneLockEnabled(true);
        }
        int quality = new LockPatternUtils(getActivity()).getActivePasswordQuality();
        if (quality < MIN_PASSWORD_QUALITY) {
            return false;
        }
        // 2.  Ask the user to confirm the current PIN/Password
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this)
                .launchConfirmationActivity(request,
                        res.getText(R.string.lockpattern_need_to_unlock),
                        null);
    }

    public void onBackPressed() {
        if (getActivity() != null) {
            if (!Utils.supportSplitView(getActivity())) {
                getActivity().finish();
            } else {
                getActivity().onBackPressed();
            }
        }
    }
    /*[E] [2012.11.23] seunggu.yang SDCARD FULL & MEDIAE EXCEPTION ENCRYPTION*/


    private void startResult() {
        if (mSearch_result.equals(KEY_SHOW_PASSWORD)) {
            mShowPassword.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_TOGGLE_INSTALL_APPLICATIONS)) {
            mToggleAppInstallation.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_LIST_TOGGLE_INSTALL_APPLICATIONS)) {
            mListToggleAppInstallation.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_TOGGLE_VERIFY_APPLICATIONS)) {
            mToggleVerifyApps.performClick(getPreferenceScreen());
        } else if (mSearch_result.equals(KEY_SKT_TOGGLE_VERIFY_APPLICATIONS)) {
            mSKTVerifyApps.performClick(getPreferenceScreen());
        }else if (mSearch_result.equals(KEY_TRUST_AGENT)) {
            mTrustAgents.performClick(getPreferenceScreen());
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

        setSearchIndexData(context, "security_settings", context.getString(R.string.security_settings_title),
                          "main", null, null, "android.settings.SECURITY_SETTINGS", null, null, 1,
                          null, null, null, 1, 0);

        boolean mIsDataEncryptSearch = Config.getFWConfigBool(context, com.lge.R.bool.config_data_encrypt,
                                                "com.lge.R.bool.config_data_encrypt");
                mIsDataEncryptSearch |= Utils.getDefaultBooleanValue(context,
                                        "com.lge.internal",
                                        "config_data_encrypt");

        boolean isEncrypted = SystemProperties.get("ro.crypto.state", "0").equals("encrypted") ? true : false;
        if (mIsDataEncryptSearch) {
            int encryptionVisibleItem = 1;
            if (isEncrypted) {
                encryptionVisibleItem = 0;
            }

            setSearchIndexData(context, KEY_ENCRYPT_PHONE,
                                   context.getString(R.string.crypt_keeper_encrypt_title),
                                   context.getString(R.string.security_settings_title),
                                   context.getString(R.string.sp_security_encrypt_phone_summary_NORMAL_changed_version4_2),
                                   null, "android.app.action.START_ENCRYPTION",
                                   "com.android.settings.Settings$CryptKeeperSettingsActivity",
                                   "com.android.settings", 1,
                                   null, null, null, encryptionVisibleItem, 0);

            int decryptionVisibleItem = 1;
            if (!isEncrypted) {
                 decryptionVisibleItem = 0;
            }
            setSearchIndexData(context, KEY_UNENCRYPT_PHONE,
                                   context.getString(R.string.crypt_keeper_encrypt_title),
                                   context.getString(R.string.security_settings_title),
                                   context.getString(R.string.crypt_keeper_encrypted_summary),
                                   null, "com.lge.settings.START_UNENCRYPTION",
                                   "com.android.settings.Settings$UnCryptKeeperSettingsActivity",
                                   "com.android.settings", 1,
                                    null, null, null, decryptionVisibleItem, 0);
        }

        if (!SystemProperties.get("ro.build.characteristics").contains("nosdcard") ||
           Config.getFWConfigBool(context, com.lge.R.bool.config_sd_encrypt,
            "com.lge.R.bool.config_sd_encrypt")) {
                setSearchIndexData(context, KEY_ENCRYPT_SDCARD_STORAGE,
                               context.getString(R.string.sp_storage_encryption_sdcard_title_NORMAL),
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.sp_sdcard_encryption_summary_NORMAL_new), 
                               null, "com.lge.settings.SD_ENCRYPTION_HELP_EXTENSION",
                               "com.android.settings.Settings$SDEncryptionHelp_Extension",
                               "com.android.settings", 1,
                               null, null, null, 1, 0);
        }

            setSearchIndexData(context, KEY_SHOW_PASSWORD,
                               context.getString(R.string.sp_password_visible_NORMAL),
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.sp_password_visible_summary_NORMAL),
                               null, "android.settings.SECURITY_SETTINGS",
                               null, null, 1,
                               "CheckBox", "System", "show_password", 1, 0);

            setSearchIndexData(context, KEY_MANAGE_DEVICE_ADMIN,
                               context.getString(R.string.manage_device_admin),
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.manage_device_admin_summary),
                               null, "android.intent.action.MAIN",
                               "com.android.settings.DeviceAdminSettings",
                               "com.android.settings", 1,
                               null, null, null, 1, 0);

            if ("KR".equals(Config.getCountry())) {
                setSearchIndexData(context, KEY_LIST_TOGGLE_INSTALL_APPLICATIONS,
                               context.getString(R.string.install_applications),
                               context.getString(R.string.security_settings_title),
                               null, null,
                              "android.settings.SECURITY_SETTINGS", null, null, 1, null,
                               null, null, 1, 0);
            } else {
                setSearchIndexData(context, KEY_TOGGLE_INSTALL_APPLICATIONS,
                               context.getString(R.string.install_applications),
                               context.getString(R.string.security_settings_title),
                               null, null,
                               "android.settings.SECURITY_SETTINGS",
                               null, null, 1, "CheckBox",
                               "Secure", "install_non_market_apps", 1, 0);
            }

            if (showVerifierSetting(context) && isVerifierInstalled(context)) {
                if (Utils.isSupportSKTVerifyApps()) {
                    setSearchIndexData(context, KEY_SKT_TOGGLE_VERIFY_APPLICATIONS,
                               context.getString(R.string.verify_applications_title_skt), 
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.verify_applications_summary), null,
                               "com.android.settings.SKT_VERIFY_APPS",
                               "com.android.settings.Settings$SKTVerifyAppsPreferenceActivity",
                               "com.android.settings", 1,
                               null, null, null, 1, 0);
                } else {
                    setSearchIndexData(context, KEY_TOGGLE_VERIFY_APPLICATIONS,
                               context.getString(R.string.verify_applications),
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.verify_applications_summary), null,
                               "android.settings.SECURITY_SETTINGS", null, null, 1, "CheckBox",
                               "Global", "package_verifier_enable", 1, 0);
                }
           }

            if (Utils.isChinaOperator()) {
                setSearchIndexData(context, KEY_ACCESS_LOCK,
                                   context.getString(R.string.access_lock_title),
                                   context.getString(R.string.security_settings_title),
                                   context.getString(R.string.access_lock_summary),
                                   null, "lge.settings.ACCESS_LOCK_SETTINGS",
                                   null,
                                   null, 1,
                                   null, null, null, 1, 0);
            }

            final int getNotificationAccessCount =
                NotificationAccessSettings.getListenersCount(context.getPackageManager());
            int visibleNotificationAccess = 0;
            if (getNotificationAccessCount > 0) {
                visibleNotificationAccess = 1;
            }
            setSearchIndexData(context, KEY_NOTIFICATION_ACCESS,
                               context.getString(R.string.manage_notification_access),
                               context.getString(R.string.security_settings_title),
                               null,
                               null, "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS",
                               "com.android.settings.Settings$NotificationAccessSettingsActivity",
                               "com.android.settings", 1,
                               null, null, null, visibleNotificationAccess, 0);

            if (Utils.isSupportCredentialStorageType(context) != 0) {
                setSearchIndexData(context, KEY_CREDENTIAL_STORAGE_TYPE, 
                               context.getString(R.string.credential_storage_type),
                               context.getString(R.string.security_settings_title),
                               context.getString(Utils.isSupportCredentialStorageType(context)), null,
                               "android.settings.SECURITY_SETTINGS",
                               null,
                               null, 1,
                               null, null, null, 1, 0);
            }

            if (Utils.isSupportUIV4_2()) {
                setSearchIndexData(context, KEY_CERTIFICATE_MANAGEMENT,
                               context.getString(R.string.certificat_management),
                               context.getString(R.string.security_settings_title),
                               null,
                               null, "android.app.action.CERTIFICATE_MANAGEMENT",
                               "com.android.settings.Settings$CertificateManagementSettingsActivity", 
                               "com.android.settings", 1,
                               null, null, null, 1, 0);
            } else {
                setSearchIndexData(context, KEY_TRUSTED_CREDENTIALS,
                               context.getString(R.string.trusted_credentials),
                               context.getString(R.string.security_settings_title),
                               context.getString(R.string.trusted_credentials_summary),
                               null, "com.android.settings.TRUSTED_CREDENTIALS",
                               "com.android.settings.TrustedCredentialsSettings",
                               "com.android.settings", 1,
                               null, null, null, 1, 0);

                setSearchIndexData(context, KEY_CERTIFICATION_INSTALLER, 
                                   context.getString(R.string.sp_credentials_install_NORMAL),
                                   context.getString(R.string.security_settings_title),
                                   context.getString(R.string.sp_credentials_install_summary_NORMAL),
                                   null, "com.android.credentials.INSTALL",
                                   "com.android.certinstaller.CertInstallerMain",
                                   "com.android.certinstaller", 1,
                                   null, null, null, 1, 0);

               KeyStore.State state = KeyStore.getInstance().state();
               int enableState = 1;
               if (state == KeyStore.State.UNINITIALIZED) {
                   enableState = 0;
               }

                setSearchIndexData(context, KEY_CLEAR_CREDENTIALS,
                                   context.getString(R.string.credentials_reset),
                                   context.getString(R.string.security_settings_title),
                                   context.getString(R.string.credentials_reset_summary),
                                   null, "com.android.credentials.RESET",
                                   "com.android.settings.CredentialStorage",
                                   "com.android.settings", enableState,
                                   null, null, null, 1, 0);
            }

            if (Utils.isSetLockSecured(context)) {
                setSearchIndexData(context, KEY_TRUST_AGENT,
                                   context.getString(R.string.manage_trust_agents),
                                   context.getString(R.string.security_settings_title),
                                   null,
                                   null, "com.android.settings.TRUST_AGENT",
                                   "com.android.settings.Settings$TrustAgentSettingsActivity",
                                   "com.android.settings",
                                   1, null, null, null, 1, 0);
            } else {
                setSearchIndexData(context, KEY_TRUST_AGENT,
                                   context.getString(R.string.manage_trust_agents),
                                   context.getString(R.string.security_settings_title),
                                   null,
                                   null, "android.settings.SECURITY_SETTINGS", null, null,
                                   1, null, null, null, 1, 0);
            }

                setSearchIndexData(context, KEY_SCREEN_PINNING,
                                   context.getString(R.string.screen_pin_title),
                                   context.getString(R.string.security_settings_title),
                                   null,
                                   null, "android.settings.SCREEN_PINNING",
                                   "com.android.settings.Settings$ScreenPinningSettingsActivity",
                                   "com.android.settings", 1,
                                   null, null, null, 1, 0);

                setSearchIndexData(context, "usage_access",
                               context.getString(R.string.apps_with_usage_access_title),
                               context.getString(R.string.security_settings_title),
                               null,
                               null, "android.settings.USAGE_ACCESS_SETTINGS",
                               "com.android.settings.Settings$UsageAccessSettingsActivity",
                               "com.android.settings", 1,
                               null, null, null, 1, 0);
            return mResult;
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

    private void updateSecuritySearchDB() {
        ContentValues row = new ContentValues();
        String where = "data_key_reference = ? AND class_name = ?";
        boolean isEncrypted = SystemProperties.get("ro.crypto.state", "0").equals("encrypted") ? true : false;
        row.clear();
        int encryptionVisibleItem = 1;
        if (isEncrypted) {
            encryptionVisibleItem = 0;
        }
        row.put("visible", encryptionVisibleItem);
        row.put("intent_action", "android.app.action.START_ENCRYPTION");
        row.put("intent_target_class", "com.android.settings.Settings$CryptKeeperSettingsActivity");
        row.put("intent_target_package", "com.android.settings");
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                new String[] { "security_encryption",
                        SecuritySettings.class.getName() });
        row.clear();
        int decryptionVisibleItem = 1;
        if (!isEncrypted) {
             decryptionVisibleItem = 0;
         }
            row.put("visible", decryptionVisibleItem);
            row.put("intent_action", "com.lge.settings.START_UNENCRYPTION");
            row.put("intent_target_class", "com.android.settings.Settings$UnCryptKeeperSettingsActivity");
            row.put("intent_target_package", "com.android.settings");
            this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                new String[] { "encryption",
                        SecuritySettings.class.getName() });
        row.clear();
        final int getNotificationAccessCount =
                NotificationAccessSettings.getListenersCount(this.getPackageManager());
        if (getNotificationAccessCount > 0) {
            row.put("visible", 1);
        } else {
            row.put("visible", 0);
        }

         this.getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
                    new String[] { "manage_notification_access",
                        SecuritySettings.class.getName() });
        row.clear();
        if (Utils.isSetLockSecured(mContext)) {
            row.put("intent_action", "com.android.settings.TRUST_AGENT");
            row.put("intent_target_package", "com.android.settings");
            row.put("intent_target_class", "com.android.settings.Settings$TrustAgentSettingsActivity");
        } else {
            row.put("intent_action", "android.settings.SECURITY_SETTINGS");
            row.putNull("intent_target_package");
            row.putNull("intent_target_class");
        }
        getActivity().getApplicationContext()
        .getContentResolver().update(Uri.parse(CONTENT_URI_SEARCH), row, where,
         new String[] { "manage_trust_agents",
                SecuritySettings.class.getName()});
    }
}
