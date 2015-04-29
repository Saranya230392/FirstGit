/*========================================================================
Copyright (c) 2011 by LG MDM.  All Rights Reserved.

            EDIT HISTORY FOR MODULE

This section contains comments describing changes made to the module.
Notice that changes are listed in reverse chronological order.

when        who                 what, where, why
----------  --------------      ---------------------------------------------------

=========================================================================*/

package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.accounts.Account;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageVolume;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.TypedValue;
import android.view.Gravity;
import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.app.AlertController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants; // yonguk.kim JB+ Migration 20130125
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.lgesetting.Config.Config;
import com.lge.cappuccino.IMdm;
import com.lge.cappuccino.Mdm;
import com.lge.mdm.LGMDMManager;
import com.lge.mdm.LGMDMManagerInternal;
import com.lge.mdm.manager.LGMDMDeviceAdminInfo;
import com.lge.mdm.manager.LGMDMDevicePolicyManager;
import com.lge.nfcaddon.NfcAdapterAddon;
import com.lge.constants.SettingsConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.android.settings.lge.OverlayUtils;
import com.android.settings.widget.ChartDataUsageView.DataUsageChartListener;
import com.android.settings.widget.ChartSweepView;
import com.android.settings.nfc.LGNfcEnabler;
import com.android.settings.EncryptWarningDialogActivity;
import com.android.settings.R;

import com.lge.constants.UsbManagerConstants;

public class MDMSettingsAdapter {
    private static final String TAG = "MDM";

    public static String LGMDM_ADAPTER_GPS = "LGMDMGPSUIAdpater";
    public static String LGMDM_ADAPTER_WIFI = "LGMDMWifiUIAdapter";
    public static String LGMDM_ADAPTER_EMAIL = "LGMDMEmailUIAdapter";
    public static String LGMDM_ADAPTER_USB = "LGMDMUsbUIAdapter";
    public static String LGMDM_ADAPTER_BLUETOOTH = "LGMDMBluetoothAdapter";
    public static String sLGMDM_ADAPTER_BLUETOOTH_AUDIOONLY = "LGMDMBluetoothAdapterAudioOnly";
    public static String LGMDM_ADAPTER_TETHERING = "LGMDMTethringAdapter";
    public static final String ACTION_EXPIRATION_PASSWORD_CHANGE = "com.lge.mdm.intent.action.EXPIRATION_PASSWORD_CHANGE";
    public static String LGMDM_ADAPTER_ENCRYPTION = "LGMDMEncryptionUIAdapter";
    public static final String LGMDM_ADAPTER_AIRPLANE_MODE = "LGMDMAirplaneModeUIAdpater";
    public static final String LGMDM_NFC_SYS = "nfcSystem";
    public static final String LGMDM_NFC_CARD_MODE = "nfcCardMode";
    public static final String LGMDM_NFC_ANDROID_BEAM = "nfcAndroidBeam";

    // make sure LGMDMIntentInternal.java
    private static final String ACTION_DEVICE_ADMIN_DEACTIVATE =
            "com.lge.mdm.intent.action.DEVICE_ADMIN_DEACTIVATE";
    private static final String ACTION_EXTERNAL_STORAGE_POLICY_CHANGE = "com.lge.mdm.intent.action.EXTERNAL_STORAGE_POLICY_CHANGE";
    private static final String ACTION_WIPE_DATA_POLICY_CHANGE = "com.lge.mdm.intent.action.WIPE_DATA_POLICY_CHANGE";
    private static final String ACTION_USB_POLICY_CHANGE = "com.lge.mdm.intent.action.USB_POLICY_CHANGE";
    private static final String ACTION_TETHER_POLICY_CHANGE = "com.lge.mdm.intent.action.TETHER_POLICY_CHANGE";
    private static final String ACTION_MANUAL_SYNC_POLICY_CHANGE = "com.lge.mdm.intent.action.MANUAL_SYNC_POLICY_CHANGE";
    private static final String ACTION_MOBILE_NETWORK_POLICY_CHANGE = "com.lge.mdm.intent.action.MOBILE_NETWORK_POLICY_CHANGE";
    private static final String ACTION_SCREEN_CAPTURE_POLICY_CHANGE = "com.lge.mdm.intent.action.SCREEN_CAPTURE_POLICY_CHANGE";
    private static final String ACTION_REMOVE_ADMIN_POLICY_CHANGE = "com.lge.mdm.intent.action.REMOVE_ADMIN_POLICY_CHANGE";
    private static final String ACTION_LOCATION_POLICY_CHANGE = "com.lge.mdm.intent.action.LOCATION_POLICY_CHANGE";
    private static final String ACTION_WIFI_POLICY_CHANGE = "com.lge.mdm.intent.action.WIFI_POLICY_CHANGE";
    private static final String ACTION_BLUETOOTH_POLICY_CHANGE = "com.lge.mdm.intent.action.BLUETOOTH_POLICY_CHANGE";
    private static final String ACTION_DATA_ROAMING_POLICY_CHANGE = "com.lge.mdm.intent.action.DATA_ROAMING_POLICY_CHANGE";
    private static final String ACTION_AIRPLANE_MODE_ON_POLICY_CHANGE = "com.lge.mdm.intent.action.AIRPLANE_MODE_ON_POLICY_CHANGE";
    private static final String ACTION_UNKNOWN_SOURCE_POLICY_CHANGE = "com.lge.mdm.intent.action.ACTION_UNKNOWN_SOURCE_POLICY_CHANGE";
    private static final String ACTION_WIRELESS_STORAGE_DISABLE = "com.lge.mdm.intent.action.ACTION_WIRELESS_STORAGE_DISALLOW";
    private static final String ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE = "com.lge.mdm.intent.action.BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE";
    private static final String ACTION_GOOGLE_BACKUP_POLICY_CHANGE = "com.lge.mdm.intent.action.GOOGLE_BACKUP_POLICY_CHANGE";
    private static final String ACTION_AUTO_RESTORE_POLICY_CHANGE = "com.lge.mdm.intent.action.AUTO_RESTORE_POLICY_CHANGE";
    private static final String ACTION_SECURITY_SETTING_POLICY_CHANGE = "com.lge.mdm.intent.action.SECURITY_SETTING_POLICY_CHANGE";
    private static final String ACTION_MOCK_LOCATION_POLICY_CHANGE = "com.lge.mdm.intent.action.MOCK_LOCATION_POLICY_CHANGE";
    private static final String ACTION_VPN_POLICY_CHANGE = "com.lge.mdm.intent.action.VPN_POLICY_CHANGE";
    private static final String ACTION_QUICKCIRCLE_POLICY_CHANGED =
            "com.lge.mdm.intent.action.ACTION_QUICKCIRCLE_POLICY_CHANGED";
    private static final String ACTION_SYSTEM_TIME_POLICY_CHANGE =
            "com.lge.mdm.intent.action.ACTION_SYSTEM_TIME_POLICY_CHANGE";
    private static final String ACTION_MULTI_USER_POLICY_CHANGE = "com.lge.mdm.intent.action.ACTION_MULTI_USER_POLICY_CHANGE";
    private static final String ACTION_NFC_POLICY_CHANGE = "com.lge.mdm.intent.action.NFC_POLICY_CHANGE";
    private static final String ACTION_PROCESS_LIMIT_POLICY_CHANGE =
            "com.lge.mdm.intent.action.ACTION_PROCESS_LIMIT_POLICY_CHANGE";
    private static final String ACTION_ENCRYPTION = "com.lge.mdm.intent.action.ACTION_ENCRYPTION";
    private static final String ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE =
            "com.lge.mdm.intent.action.CHANGING_MOBILE_DATAUSAGE_CYCLE";
    private static final String ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT =
            "com.lge.mdm.intent.action.ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT";
    private static final String ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN =
            "com.lge.mdm.intent.action.ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN";
    private static final String ACTION_DEV_MODE_POLICY_CHANGE =
            "com.lge.mdm.intent.action.ACTION_DEVELOPER_MODE_POLICY_CHANGE";
    private static final String ACTION_DUAL_WINDOW_POLICY_CHANGE =
            "com.lge.mdm.intent.action.ACTION_DUAL_WINDOW_POLICY_CHANGE";

    private static final String EASY_SETTING_PACKAGE = "com.lge.settings.easy";

    // ID-MDM-169
    private static final String LGMDM_CHECK_PATH = "persist-lg/lgmdm/check_for_policy.txt";

    private static final int ENCRYPT = 0;
    private static final int DECRYPT = 1;
    private long mToastTime = 0;

    /**
     * Indicates the decrypt only storage.
     */
    public static final int LGMDMENCRYPTION_DISABLED = 0;

    /**
     * Indicates the encrypt only device
     */
    public static final int LGMDMENCRYPTION_DEVICE = 1;

    /**
     * Indicates the encrypt only storage.
     * Nothing all devices support Storage Encryption.
     */
    public static final int LGMDMENCRYPTION_STORAGE = 2;

    /**
     * Indicates the encrypt device and storage
     * If the device does not support storage encryption,
     * the device encryption only will execute.
     */
    public static final int LGMDMENCRYPTION_DEVICE_AND_STORAGE = 3;

    /**
     * Indicates the encryption policy
     */
    public static final int LGMDMENCRYPTION_NONE = 4;

    /**
     * Indicates the decrypt only device.
     */
    public static final int LGMDMENCRYPTION_DEVICE_DISABLED = 5;

    /**
     * Indicates the decrypt device and storage.
     */
    public static final int LGMDMENCRYPTION_DEVICE_AND_STORAGE_DISABLED = 6;

    private LGMDMDeviceAdminInfo mLGMDMDeviceAdmin;

    private LGMDMManager mLGMDM = LGMDMManager.getInstance();

    private static MDMSettingsAdapter mInstance;

    public static MDMSettingsAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new MDMSettingsAdapter();
        }
        return mInstance;
    }

    private MDMSettingsAdapter() {
    }

    public boolean checkDisabled(ComponentName who, String mCurrentModuleName) {
        if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_GPS)) {
            if (LGMDMManager.getInstance().getAllowGPSLocation(who)) {
                return false;
            }

            // LGMDMManager.getInstance().sendLockedNotification();
            return true;
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_WIFI)) {
            if (LGMDMManager.getInstance().getAllowWifi(who)) {
                return false;
            }

            // LGMDMManager.getInstance().sendLockedNotification();
            return true;
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_EMAIL)) {
            if (LGMDMManager.getInstance().isManualSyncCurrent()) {
                return true;
            }
            return false;
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_BLUETOOTH)) {
            if (LGMDMManager.getInstance().getAllowBluetooth(who) != LGMDMManager.LGMDMBluetooth_DISALLOW) {
                return false;
            }
            return true;
        } else if (mCurrentModuleName.equalsIgnoreCase(sLGMDM_ADAPTER_BLUETOOTH_AUDIOONLY)) {
            if (LGMDMManager.getInstance().getAllowBluetooth(who) != LGMDMManager.LGMDMBluetooth_ALLOW_AUDIOONLY) {
                return false;
            }
            return true;
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_USB)) {
            return LGMDMManagerInternal.getInstance().getDisallowUSBType(IMdm.USB_ALL);
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_TETHERING)) {
            return LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_USB);
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_ENCRYPTION)) {
            if (LGMDMManager.getInstance().getEncryptionPolicy(who) == LGMDMManager.LGMDMENCRYPTION_DEVICE
                    || LGMDMManager.getInstance().getEncryptionPolicy(who) == LGMDMManager.LGMDMENCRYPTION_DEVICE_AND_STORAGE) {
                return true;
            }
            return false;
        } else if (mCurrentModuleName.equalsIgnoreCase(LGMDM_ADAPTER_AIRPLANE_MODE)) {
            if (LGMDMManager.getInstance().getAllowAirplaneModeOn(who) == false ||
                    LGMDMManager.getInstance().getEnforceAirplaneMode(who) == true) {
                return true;
            }
            return false;
        } else if (mCurrentModuleName.equalsIgnoreCase(ACTION_MOBILE_NETWORK_POLICY_CHANGE)) {
            if ((LGMDMManager.getInstance().getAllowMobileNetwork(who))
                    && (!LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(who))) {
                return false;
            }
            return true;
        }

        return false;
    }

    public boolean setLocationEnableMenu(Context context, String menuString, TwoStatePreference menu) {
        if (menu == null) {
            return false;
        }
        boolean disallowGps = (LGMDMManager.getInstance().getAllowGPSLocation(null) == false);
        boolean enforceGps = LGMDMManager.getInstance().getEnforceGPSLocationEnabled(null);
        boolean disallowLocation = (LGMDMManager.getInstance().getAllowWirelessLocation(null) == false);

        if ("mGps".equalsIgnoreCase(menuString)) {
            if (disallowGps) {
                Log.i(TAG, "[LGMDM] mGps disabllow mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_block_gps_NORMAL);
                if ((Config.SKT).equals(Config.getOperator())
                        || (Config.KT).equals(Config.getOperator())
                        || (Config.LGU).equals(Config.getOperator())) {
                    menu.setSummaryOff(R.string.sp_lgmdm_block_gps_NORMAL);
                }
                return true; // mdm policy set
            } else if (enforceGps) {
                Log.i(TAG, "[LGMDM] mGps Enforce mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_enable_gps_NORMAL);
                if ((Config.SKT).equals(Config.getOperator())
                        || (Config.KT).equals(Config.getOperator())
                        || (Config.LGU).equals(Config.getOperator())) {
                    menu.setSummaryOn(R.string.sp_lgmdm_enable_gps_NORMAL);
                }
                return true; // mdm policy set
            }
        } else if ("mGpsVzw".equalsIgnoreCase(menuString)) {
            if (disallowGps) {
                Log.i(TAG, "[LGMDM] mGpsVzw disabllow mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_block_gps_NORMAL);
                return true; // mdm policy set
            } else if (enforceGps) {
                Log.i(TAG, "[LGMDM] mGpsVzw Enforce mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_enable_gps_NORMAL);
                return true; // mdm policy set
            }
        } else if ("mNetwork".equalsIgnoreCase(menuString)) {
            if (disallowLocation) {
                Log.i(TAG, "[LGMDM] mNetwork disabllow mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_block_loaction_NORMAL);
                if ((Config.SKT).equals(Config.getOperator())
                        || (Config.KT).equals(Config.getOperator())
                        || (Config.LGU).equals(Config.getOperator())) {
                    menu.setSummaryOff(R.string.sp_lgmdm_block_loaction_NORMAL);
                }
                return true; // mdm policy set
            }
        } else if ("mVzwLbs".equalsIgnoreCase(menuString)) {
            if (Config.isSupportVZWLocationAccessScenario4_0()) {
                return false;
            }
            if (LGMDMManager.getInstance().getAllowVerizonLocation(null) == false) {
                Log.i(TAG, "[LGMDM] mVzwLbs disabllow mode");

                menu.setEnabled(false);
                menu.setSummary(R.string.sp_lgmdm_block_loaction_NORMAL);
                return true; // mdm policy set
            }
        } else if ("mLocationAccess".equalsIgnoreCase(menuString)) {
            boolean restrictLocationAccess = ((enforceGps && !disallowGps) || (disallowGps && disallowLocation));
            if (Config.VZW.equals(Config.getOperator())
                    && Config.isSupportVZWLocationAccessScenario4_0() == false) {
                boolean disallwVzwLoc = !(LGMDMManager.getInstance().getAllowVerizonLocation(null));
                restrictLocationAccess = ((enforceGps && !disallowGps) || (restrictLocationAccess && disallwVzwLoc));
            }
            if (restrictLocationAccess) {
                Log.i(TAG, "[LGMDM] mLocationAccess restrict mode");
                List<Integer> toastMsg = new ArrayList<Integer>();
                if (disallowGps) {
                    menu.setChecked(false);
                    toastMsg.add(R.string.sp_lgmdm_block_gps_point);
                } else if (enforceGps) {
                    menu.setChecked(true);
                    toastMsg.add(R.string.sp_lgmdm_cannot_gps_NORMAL);
                }
                if (disallowLocation) {
                    toastMsg.add(R.string.sp_lgmdm_block_loaction_point);
                }
                for (int id : toastMsg) {
                    Toast.makeText(context, Utils.getString(id), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        return false;
    }

    public void setLocationEnableMenuKK(PreferenceScreen root) {
        if (root == null) {
            return;
        }
        CheckBoxPreference mHighAccuracy = (CheckBoxPreference)root.findPreference("high_accuracy");
        CheckBoxPreference mBatterySaving = (CheckBoxPreference)root
                .findPreference("battery_saving");
        CheckBoxPreference mSensorsOnly = (CheckBoxPreference)root.findPreference("sensors_only");

        boolean disallowGps = (LGMDMManager.getInstance().getAllowGPSLocation(null) == false);
        boolean enforceGps = LGMDMManager.getInstance().getEnforceGPSLocationEnabled(null);
        boolean disallowLocation =
                (LGMDMManager.getInstance().getAllowWirelessLocation(null) == false);

        if (disallowGps && enforceGps) {
            enforceGps = false;
        }
        if (mSensorsOnly != null) {
            if (disallowGps) {
                mSensorsOnly.setSummary(R.string.sp_lgmdm_block_gps_only_mode);
                mSensorsOnly.setEnabled(false);
            }
        }
        if (mBatterySaving != null) {
            if (disallowLocation) {
                mBatterySaving.setSummary(R.string.sp_lgmdm_block_network_only_mode);
                mBatterySaving.setEnabled(false);
            }
            if (enforceGps && !disallowLocation) {
                mBatterySaving.setSummary(R.string.sp_lgmdm_block_network_only_mode);
                mBatterySaving.setEnabled(false);
            }
        }
        if (mHighAccuracy != null) {
            if (disallowGps || disallowLocation) {
                mHighAccuracy.setSummary(R.string.sp_lgmdm_block_network_gps_mode);
                mHighAccuracy.setEnabled(false);
            }
            if (enforceGps && disallowLocation) {
                mHighAccuracy.setSummary(R.string.sp_lgmdm_block_network_gps_mode);
                mHighAccuracy.setEnabled(false);
            }
        }
    }

    public void setExternalMemoryEnableMenu(ComponentName who, Context resContext,
            String menuString, Preference externalmenu) {
        if (externalmenu == null || menuString == null) {
            return;
        }

        if (menuString.equalsIgnoreCase("mMountSDcard")) {
            if (!LGMDMManager.getInstance().getAllowExternalMemorySlot(who)
                    || !checkCIDMountServiceWhitelist()) {
                Log.i(TAG, "[LGMDM] disallow MountSDcard menu");

                externalmenu.setEnabled(false);
                externalmenu.setTitle(R.string.sd_mount);
                externalmenu.setSummary(R.string.sp_lgmdm_block_sdcard_NORMAL);
            }
        }
    }

    private boolean checkCIDMountServiceWhitelist() {
        if (LGMDMManager.getInstance().getAllowCIDWithWhitelist(null)) {
            List<String> mCIDlist = null;
            mCIDlist = LGMDMManager.getInstance().getCIDWithWhitelist(null);
            String currentCID = getSDCARDiD();
            if (currentCID == null) {
                return true;
            }
            if (currentCID.length() < 3) {
                Log.w(TAG,
                        "checkCIDMountServiceWhitelist() : currentCID.length() is not reasonable");
                return true;
            }
            // CID`s full length is 32, and last 2 length is checksum.
            String reasonableCID = currentCID.substring(0, currentCID.length() - 2);
            boolean iswhitelist = false;
            if (mCIDlist != null) {
                for (String externalmemoryCID : mCIDlist) {
                    iswhitelist = externalmemoryCID.contains(reasonableCID);
                    if (iswhitelist) {
                        break;
                    }
                }
            }
            if (!iswhitelist) {
                Log.i(TAG, "[LGMDM] disallow sdcard : CID = " + reasonableCID);
            }
            return iswhitelist;
        }
        return true;
    }

    private String getSDCARDiD() {
        String sd_cid = "-1";
        Process cmd = null;
        BufferedReader br = null;
        try {
            File file = new File("/sys/block/mmcblk1");
            if (file.exists() && file.isDirectory()) {
                cmd = Runtime.getRuntime().exec("cat /sys/block/mmcblk1/device/cid");
                br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
                String temp = br.readLine();
                if (temp != null) {
                    sd_cid = temp;
                    return sd_cid;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "MDMSettingsAdapter : IOException is ", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.w(TAG, "MDMSettingsAdapter : br.close() : IOException is ", e);
                }
            }
            if (cmd != null) {
                cmd.destroy();
            }
        }
        return sd_cid;
    }

    public boolean setEraseSdMenu(ComponentName who, Preference mFormatPreference) {
        if (mFormatPreference == null) {
            return false;
        }

        if (!LGMDMManager.getInstance().getAllowWipeData(who)) {
            mFormatPreference.setEnabled(false);
            mFormatPreference.setSummary(R.string.sp_lgmdm_erase_sdcard_NORMAL);
            Log.i(TAG, "setEraseSdMenu: MDM Locked menu");
            return true;
        }
        return false;
    }

    /**
     * check LGMDM Enforce minimum password change digit policy is allow or disallow
     * this API must required two argument(old password and new password)
     *
     * @param old_password old password(must required)
     * @param new_password new password(must required)
     * @return return true, if password meets the current requirements, else false
     */
    public boolean isAllowMinimumPasswordChangeDigit(String old_password, String new_password) {
        int curPasswordMinimumChange = LGMDMManager.getInstance().getPasswordMinimumChange(null);
        if (curPasswordMinimumChange <= 0) {
            // no need to check
            return true;
        }
        if (TextUtils.isEmpty(old_password)) {
            return true;
        }
        if (TextUtils.isEmpty(new_password)) {
            return true;
        }
        int changeDigit = LGMDMManagerInternal.getInstance().computeEditDistance(old_password,
                new_password);
        if (changeDigit < curPasswordMinimumChange) {
            Log.i(TAG, "minimum password change is not requirements. not allowed");
            return false;
        }
        return true; // allow
    }

    /**
     * Retrieve the current enforce minimum password change digit for all admins
     *
     * @return The number of character changes in the new password
     */
    public int getPasswordMinimumChange() {
        return LGMDMManager.getInstance().getPasswordMinimumChange(null);
    }

    public boolean setAutoMasterSynceEnableMenu(ComponentName who, CheckBoxPreference autoSyncData) {
        if (autoSyncData == null) {
            return false;
        }

        if (LGMDMManager.getInstance().isManualSyncCurrent()) {
            autoSyncData.setEnabled(false);
            autoSyncData.setSummary(R.string.sp_lgmdm_control_autosync_NORMAL);
            Log.i(TAG, "[LGMDM] master auto synce is false");
        }

        return true;
    }

    public void setAdbEnableMenu(CheckBoxPreference pref) {
        if (pref == null) {
            return;
        }

        if (LGMDMManagerInternal.getInstance().getDisallowUSBType(IMdm.USB_ADB)) {
            Log.i(TAG, "setAdbEnableMenu : LGMDM disallow Usb Debugging");
            pref.setSummary(R.string.sp_block_usb_debugging_NORMAL);
            pref.setEnabled(false);
        }
    }

    public void setProcessLimitMenu(ListPreference pref) {
        if (pref == null) {
            Log.i(TAG, "setProcessLimitMenu : pref is null");
            return;
        }

        if (LGMDMManager.getInstance().getAllowBackgroundProcessLimit(null) == false) {
            pref.setSummary(R.string.sp_lgmdm_cannot_set_background_process_limit);
            pref.setEnabled(false);
        }
    }

    public boolean checkAdbDisallow() {
        return LGMDMManagerInternal.getInstance().getDisallowUSBType(IMdm.USB_ADB);

    }

    private boolean isUsbConnected() {
        String STATE_PATH = "/sys/class/android_usb/android0/state";
        String state = null;
        try {
            state = FileUtils.readTextFile(new File(STATE_PATH), 0, null).trim();
        } catch (IOException e) {
            Log.w(TAG, "isUsbConnected : IOException : " + e);
        }
        // WBT Defect ID : 316782
        if (state != null) {
            if (state.equals("DISCONNECTED")) {
                return false;
            }
        }
        return true;
    }

    public void setUnknownSourceEnableMenu(CheckBoxPreference pref) {
        if (pref == null) {
            return;
        }
        if (LGMDMManager.getInstance().getAllowUnknownSourceInstallation(null) == false) {
            Log.i(TAG, "LGMDM disallow unknown source installtion menu");
            pref.setSummary(R.string.sp_block_unknownsource_NORMAL);
            pref.setSummaryOff(R.string.sp_block_unknownsource_NORMAL);
            pref.setSummaryOn(R.string.sp_block_unknownsource_NORMAL);
            pref.setEnabled(false);
        }
    }

    public void setUnknownSourceEnableMenu(
            com.android.settings.DoubleTitleListPreference mListToggleAppInstallation) {
        if (mListToggleAppInstallation == null) {
            return;
        }
        if (LGMDMManager.getInstance().getAllowUnknownSourceInstallation(null) == false) {
            Log.i(TAG, "LGMDM disallow unknown source installation menu");
            mListToggleAppInstallation.setValueIndex(0);
            mListToggleAppInstallation.setSummary(R.string.sp_block_unknownsource_NORMAL);
            mListToggleAppInstallation.setEnabled(false);
        }
    }

    public void setAdminListEnableDisplayMenu(Context context, DeviceAdminInfo item,
            ImageView icon, TextView name, CheckBox checkbox, TextView description) {
        DevicePolicyManager mDPM;
        mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final HashSet<ComponentName> mActiveAdmins = new HashSet<ComponentName>();
        List<ComponentName> cur = mDPM.getActiveAdmins();
        if (cur != null) {
            for (int i = 0; i < cur.size(); i++) {
                mActiveAdmins.add(cur.get(i));
            }
        }
        if (!LGMDMManager.getInstance().getAllowRemoveDeviceAdmin(null,
                item.getPackageName())
                && mActiveAdmins.contains(item.getComponent())) {
            icon.setEnabled(false);
            name.setEnabled(false);
            checkbox.setEnabled(false);
            description.setEnabled(false);
            description.setText(R.string.sp_remove_device_block_NORMAL);
        }
    }

    public boolean setAdminListEnableClickMenu(Context context, DeviceAdminInfo item) {
        DevicePolicyManager mDPM;
        mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final HashSet<ComponentName> mActiveAdmins = new HashSet<ComponentName>();
        List<ComponentName> cur = mDPM.getActiveAdmins();
        if (cur != null) {
            for (int i = 0; i < cur.size(); i++) {
                mActiveAdmins.add(cur.get(i));
            }
        }
        if (!LGMDMManager.getInstance().getAllowRemoveDeviceAdmin(null, item.getPackageName())
                && mActiveAdmins.contains(item.getComponent())) {
            Log.i(TAG, "setAdminListEnableClickMenu : allowed package can't remove ");
            return true;
        }
        return false;
    }

    public void setFactoryResetMenu(ComponentName who, Context resContext,
            PreferenceScreen mBackupReset) {
        boolean allowFactoryRest = LGMDMManager.getInstance().getAllowWipeData(who);
        if (mBackupReset == null) {
            return;
        }

        if (!allowFactoryRest) {
            Log.i(TAG, "LGMDM menu : Disallow FactoryRest");
            mBackupReset.setSummary(R.string.sp_lgmdm_block_factoryreset_NORMAL);
            mBackupReset.setEnabled(false);
        }
    }

    public void setGoogleBackupMenu(ComponentName who, Context resContext,
            CheckBoxPreference mBackup) {
        boolean allowGoogleBackup = LGMDMManager.getInstance().getAllowGoogleBackup(who);
        if (mBackup == null) {
            return;
        }

        if (!allowGoogleBackup) {
            Log.i(TAG, "LGMDM menu : Disallow GoogleBackup");
            mBackup.setChecked(false);
            mBackup.setSummary(R.string.sp_lgmdm_block_backup_my_data_NORMAL);
            mBackup.setEnabled(false);
        }
    }

    public void setAutoRestoreMenu(ComponentName who, Context resContext,
            CheckBoxPreference mAutoRestore) {
        boolean allowAutoRestore = LGMDMManager.getInstance().getAllowAutoRestore(who);
        if (mAutoRestore == null) {
            return;
        }

        if (!allowAutoRestore) {
            Log.i(TAG, "LGMDM menu : Disallow AutoRestore");
            mAutoRestore.setChecked(false);
            mAutoRestore.setSummary(R.string.sp_lgmdm_block_automatic_restore_NORMAL);
            mAutoRestore.setEnabled(false);
        }
    }

    public void setFactoryResetButton(ComponentName who, Context resContext, Button mInitiateButton) {
        boolean allowFactoryRest = LGMDMManager.getInstance().getAllowWipeData(who);
        if (mInitiateButton == null) {
            return;
        }

        if (!allowFactoryRest) {
            Log.i(TAG, "LGMDM menu : Disallow FactoryRest Button");
            mInitiateButton.setEnabled(false);
        }
    }

    public void setFactoryResetConfirmButton(ComponentName who, Context resContext,
            Button mFinalButton) {
        boolean allowFactoryRest = LGMDMManager.getInstance().getAllowWipeData(who);
        if (mFinalButton == null) {
            return;
        }

        if (!allowFactoryRest) {
            Log.i(TAG, "LGMDM menu : Disallow FactoryResetConfirmButton");
            mFinalButton.setEnabled(false);
        }
    }

    public void setDataEnablerSwitch(Switch mSwitch) {
        if (mSwitch == null) {
            return;
        }
        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mSwitch.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mSwitch.setEnabled(false);
        }
    }

    public boolean setDataUsageSwitch(Switch mDataEnabled, OnCheckedChangeListener listener) {
        if (mDataEnabled == null) {
            return false;
        }
        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mDataEnabled.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mDataEnabled.setOnCheckedChangeListener(null);
            mDataEnabled.setChecked(false); // for Settings UI update
            mDataEnabled.setEnabled(false);
            mDataEnabled.setOnCheckedChangeListener(listener);
        }
        return true;
    }

    public void setDataUsageSwitchSplitUI(Switch mDataEnabled) {
        if (mDataEnabled == null) {
            return;
        }
        if (mLGMDM.getEnforceMobileNetworkEnabled(null)) {
            mDataEnabled.setClickable(false);
        } else if (!mLGMDM.getAllowMobileNetwork(null)) {
            mDataEnabled.setChecked(false);
            mDataEnabled.setEnabled(false);
        }
    }


    public void setDataUsageSummarySplitUI(TextView mMDMSummaryView, Context context,
            boolean isWifiTab) {
        if (mMDMSummaryView == null || context == null) {
            return;
        }
        if (isWifiTab) {
            mMDMSummaryView.setVisibility(View.GONE);
        } else {
            final int sPadding = context.getResources().getDimensionPixelSize(
                    R.dimen.help_guide_padding_size);
            final int ePadding = context.getResources().getDimensionPixelSize(
                    R.dimen.help_guide_padding_size);
            final int tPadding = context.getResources().getDimensionPixelSize(
                    R.dimen.settings_density_size_12);
            final int bPadding = context.getResources().getDimensionPixelSize(
                    R.dimen.settings_density_size_11_5);

            mMDMSummaryView.setPadding(sPadding, tPadding, ePadding, bPadding);
            if (mLGMDM.getEnforceMobileNetworkEnabled(null)) {
                mMDMSummaryView.setText(R.string.sp_lgmdm_force_data_NORMAL);
                mMDMSummaryView.setVisibility(View.VISIBLE);
            } else if (!mLGMDM.getAllowMobileNetwork(null)) {
                mMDMSummaryView.setText(R.string.sp_lgmdm_block_data_NORMAL);
                mMDMSummaryView.setVisibility(View.VISIBLE);
            } else {
                mMDMSummaryView.setVisibility(View.GONE);
            }
        }
    }

    public boolean setDataUsageSwitchMultiSIM(Switch mDataEnabled,
            OnCheckedChangeListener listener, boolean airPlaneMode) {
        if (mDataEnabled == null) {
            return false;
        }
        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mDataEnabled.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mDataEnabled.setOnCheckedChangeListener(null);
            mDataEnabled.setChecked(false); // for Settings UI update
            mDataEnabled.setEnabled(false);
            mDataEnabled.setOnCheckedChangeListener(listener);
        }
        return true;
    }

    public boolean setDataUsageRoamingMenu(MenuItem mMenuDataRoaming, boolean isDataRoaming) {
        if (mMenuDataRoaming == null) {
            return false;
        }

        if (!LGMDMManager.getInstance().getAllowDataRoaming(null)) {
            mMenuDataRoaming.setChecked(false);
            mMenuDataRoaming.setEnabled(false);
        } else {
            mMenuDataRoaming.setEnabled(true); // for Settings UI update
            mMenuDataRoaming.setChecked(isDataRoaming); // for Settings UI update
        }
        return true;
    }

    public boolean isShowDataUsageRoamingToastIfNeed(Context context) {
        if (context != null
                && (com.lge.mdm.LGMDMManager.getInstance().getAllowDataRoaming(null) == false)) {
            Toast.makeText(context, Utils.getString(R.string.sp_block_data_roaming_point),
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "send toast disallow data roaming");
            return true;
        }
        return false;
    }

    public boolean isShowDataUsageAutoSyncToastIfNeed(Context context) {
        if (context != null
                && (com.lge.mdm.LGMDMManager.getInstance().isManualSyncCurrent() == true)) {
            Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_control_autosync_NORMAL),
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "send toast disallow auto sync");
            return true;
        }
        return false;
    }

    /**
     * @return true : force charge only
     */
    public boolean setUsbMenu(RadioButtonPreference charger, RadioButtonPreference mediasync,
            RadioButtonPreference massStorage, RadioButtonPreference pcsuite,
            RadioButtonPreference tether, RadioButtonPreference ptp,
            RadioButtonPreference internet, int internetConnection, String currentFunction) {
        boolean forceChargeOnly = false;
        LGMDMManagerInternal mdm = LGMDMManagerInternal.getInstance();
        if (mdm == null) {
            Log.w(TAG, "setUsbMenu : mdm is null");
            return forceChargeOnly;
        }
        boolean allDisallow = mdm.getDisallowUSBType(IMdm.USB_ALL);
        boolean tetherDisallow = mdm.getDisallowUSBType(IMdm.USB_TETHER);
        boolean mtpDisallow = mdm.getDisallowUSBType(IMdm.USB_MTP);
        boolean ptpDisallow = mdm.getDisallowUSBType(IMdm.USB_PTP);
        boolean storageDisallow = mdm.getDisallowUSBType(IMdm.USB_MASS_STORAGE);
        boolean pcsuiteDisallow = mdm.getDisallowUSBType(IMdm.USB_SOFTWARE);
        // [S][changyu0218.lee]Add Sprint USBallowPort
        boolean allowUsbPort = OverlayUtils.getAllowUsbPort(null);
        if (allowUsbPort == false) {
            mtpDisallow = true;
            ptpDisallow = true;
            storageDisallow = true;
            pcsuiteDisallow = true;
        }
        // [E][changyu0218.lee]Add Sprint USBallowPort
        if (mediasync != null) {
            if (mtpDisallow) {
                // disallow mode
                mediasync.setSummary(R.string.sp_lgmdm_block_media_sync_NORMAL);
                mediasync.setChecked(false);
                mediasync.setEnabled(false);
                Log.i(TAG, "setUsbMenu : LGMDM Block mediasync");
            }
        }

        if (massStorage != null) {
            if (storageDisallow) {
                // disallow mode
                massStorage.setSummary(R.string.sp_block_usb_mass_storage_NORMAL);
                massStorage.setChecked(false);
                massStorage.setEnabled(false);
                Log.i(TAG, "setUsbMenu : LGMDM Block massStorage");
            }
        }

        if (pcsuite != null) {
            if (pcsuiteDisallow) {
                // disallow mode
                pcsuite.setSummary(R.string.sp_lgmdm_block_lg_software_NORMAL);
                pcsuite.setChecked(false);
                pcsuite.setEnabled(false);
                Log.i(TAG, "setUsbMenu : LGMDM Block pcsuite");
            }
        }

        if (tether != null) {
            if (tetherDisallow) {
                // disallow mode
                tether.setSummary(R.string.sp_block_usb_thering_NORMAL);
                tether.setChecked(false);
                tether.setEnabled(false);
                Log.i(TAG, "setUsbMenu : LGMDM Block tether");
            }
        }

        if (ptp != null) {
            if (ptpDisallow) {
                // disallow mode
                ptp.setSummary(R.string.sp_lgmdm_block_camera_ptp_NORMAL);
                ptp.setChecked(false);
                ptp.setEnabled(false);
                Log.i(TAG, "setUsbMenu : LGMDM Block ptp");
            }
        }

        if (internet != null) {
            if (Config.VZW.equals(Config.getOperator())) {
                if (tetherDisallow) {
                    // disallow mode
                    internet.setSummary(R.string.sp_block_usb_thering_NORMAL);
                    internet.setChecked(false);
                    internet.setEnabled(false);
                    Log.i(TAG, "setUsbMenu : LGMDM Block internet");
                }
            }
        }

        if (charger != null) {
            if (allDisallow) {
                forceChargeOnly = true;
            } else if (tetherDisallow) {
                if (Config.VZW.equals(Config.getOperator())) {
                    if (UsbManagerConstants.USB_FUNCTION_TETHER.equals(currentFunction)
                            || UsbManagerConstants.USB_FUNCTION_PC_SUITE.equals(currentFunction)) {
                        forceChargeOnly = true;
                    }
                } else {
                    if (UsbManagerConstants.USB_FUNCTION_TETHER.equals(currentFunction)) {
                        forceChargeOnly = true;
                    }
                }
            } else if (mtpDisallow) {
                if (/* KLP UsbManagerConstants.USB_FUNCTION_MTP.equals(currentFunction)
                     * || */UsbManagerConstants.USB_FUNCTION_MTP_ONLY.equals(currentFunction)
                        || "usb_enable_mtp".equals(currentFunction)) {
                    forceChargeOnly = true;
                }
            } else if (storageDisallow) {
                if (true /* KLP UsbManagerConstants.USB_FUNCTION_MASS_STORAGE.
                        equals(currentFunction) */) {
                    forceChargeOnly = true;
                }
            } else if (ptpDisallow) {
                if (/* KLP UsbManagerConstants.USB_FUNCTION_PTP.equals(currentFunction)
                     * || */UsbManagerConstants.USB_FUNCTION_PTP_ONLY.equals(currentFunction)
                        || "usb_enable_mtp".equals(currentFunction)) {
                    forceChargeOnly = true;
                }
            }

            if (forceChargeOnly) {
                charger.setChecked(true);
                setUsbSettingsControlChargeOnly();
            }
        }

        return forceChargeOnly;
    }

    public void setSettingsMenu(Resources res, ListAdapter listAdapter) {

        if (listAdapter == null) {
            return;
        }

        for (int i = 0; i < listAdapter.getCount(); i++) {
            Header header = (Header)listAdapter.getItem(i);
            int id = (int)header.id;

            if (id == R.id.wifi_settings) {
                if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.LGMDM_ADAPTER_WIFI)) {
                    Log.i(TAG, "MDM wifi change summary");
                    header.summary = res.getString(R.string.sp_lgmdm_blockwifi_NORMAL);
                }
            } else if (id == R.id.bluetooth_settings) {
                if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.LGMDM_ADAPTER_BLUETOOTH)) {
                    Log.i(TAG, "MDM bt change summary");
                    header.summary = res.getString(R.string.sp_lgmdm_block_bluetooth_NORMAL);
                } else if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.sLGMDM_ADAPTER_BLUETOOTH_AUDIOONLY)) {
                    Log.i(TAG, "MDM bt change summary audio only");
                    header.summary = res.getString(R.string.sp_lgmdm_only_audio_bluetooth_NORMAL);
                }
            } else if (id == R.id.account_main_settings) {
                if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.LGMDM_ADAPTER_EMAIL)) {
                    Log.i(TAG, "MDM sync change summary");
                    header.summary = res.getString(R.string.sp_lgmdm_control_autosync_NORMAL);
                }
            } else if (id == R.id.airplane_mode_settings) {
                if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.LGMDM_ADAPTER_AIRPLANE_MODE)) {
                    if (LGMDMManager.getInstance().getAllowAirplaneModeOn(null) == false) {
                        Log.i(TAG, "MDM disallow airplane mode change summary");
                        header.summary = res.getString(R.string.sp_lgmdm_block_airplane_NORMAL);
                    } else if (LGMDMManager.getInstance().getEnforceAirplaneMode(null) == true) {
                        Log.i(TAG, "MDM enforce airplane mode change summary");
                        header.summary = res.getString(R.string.sp_lgmdm_enforce_airplane_NORMAL);
                    }
                }
            } else if (id == R.id.data_usage_settings) {
                if (MDMSettingsAdapter.getInstance().checkDisabled(null,
                        MDMSettingsAdapter.ACTION_MOBILE_NETWORK_POLICY_CHANGE)) {
                    if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
                        Log.i(TAG, "MDM EnforceMobileNetwork summary");
                        header.summary = res.getString(R.string.sp_lgmdm_force_data_NORMAL);
                    } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
                        Log.i(TAG, "MDM AllowMobileNetwork summary");
                        header.summary = res.getString(R.string.sp_lgmdm_block_data_NORMAL);
                    }
                }
            }
        }
    }

    private void setUsbSettingsControlChargeOnly() {
        if (Config.VZW.equals(Config.getOperator())) {
            UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE,
                    UsbSettingsControl.USBMODE_CHARGE);
        }
    }

    public void setSimSettingsMenuMTK(ComponentName who, boolean mIsSlot1Insert,
            boolean mIsSlot2Insert, boolean mIsSlot3Insert, Context resContext,
            CharSequence summary, Preference mButtonSimSettingsEnabled) {
        if (mButtonSimSettingsEnabled == null) {
            return;
        }

        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(who)) {
            mButtonSimSettingsEnabled.setSummary(R.string.sp_lgmdm_force_data_NORMAL);
            mButtonSimSettingsEnabled.setEnabled(false);
            Log.i(TAG, "LGMDM Enforce Mobile Network");
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(who)) {
            mButtonSimSettingsEnabled.setSummary(R.string.sp_lgmdm_block_data_NORMAL);
            mButtonSimSettingsEnabled.setEnabled(false);
            Log.i(TAG, "LGMDM Disallow Mobile Network");
        }
    }

    public void setDataRoamingMenuMTK(ComponentName who, boolean mIsSlot1Insert,
            boolean mIsSlot2Insert, Context resContext, CheckBoxPreference mButtonDataRoam) {
        if (!LGMDMManager.getInstance().getAllowDataRoaming(who)) {
            mButtonDataRoam.setChecked(false);
            mButtonDataRoam.setSummaryOff(R.string.sp_block_data_roaming_NORMAL);
            mButtonDataRoam.setEnabled(false);
            Log.i(TAG, "LGMDM Disallow data Roaming");
        }
    }

    public void setMobileNetworkMenuMTK(ComponentName who, boolean mIsSlot1Insert,
            boolean mIsSlot2Insert, Context resContext, CheckBoxPreference mButtonDataEnabled) {
        if (mButtonDataEnabled == null) {
            return;
        }

        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(who)) {
            mButtonDataEnabled.setChecked(true);
            mButtonDataEnabled.setSummary(R.string.sp_lgmdm_force_data_NORMAL);
            mButtonDataEnabled.setEnabled(false);
            Log.i(TAG, "LGMDM Enforce Mobile Network");
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(who)) {
            mButtonDataEnabled.setChecked(false);
            mButtonDataEnabled.setSummaryOff(R.string.sp_lgmdm_block_data_NORMAL);
            mButtonDataEnabled.setEnabled(false);
            Log.i(TAG, "LGMDM Disallow Mobile Network");
        }
    }

    public void setMobileNetworkMenu(PreferenceScreen mButtonDataEnabled) {
        if (mButtonDataEnabled == null) {
            return;
        }
        if (Config.SKT.equals(Config.getOperator()) == false) {
            return;
        }

        if (LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)) {
            mButtonDataEnabled.setSummary(R.string.sp_lgmdm_force_data_NORMAL);
            Log.i(TAG, "LGMDM Enforce Mobile Network enable");
            mButtonDataEnabled.setEnabled(false);
        } else if (!LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
            mButtonDataEnabled.setSummary(R.string.sp_lgmdm_block_data_NORMAL);
            Log.i(TAG, "LGMDM Disallow Mobile Network disable");
            mButtonDataEnabled.setEnabled(false);
        }
    }

    public boolean checkDeviceEncryptionForSplitUI(Context mContext) {
        int encryption = LGMDMManager.getInstance().getEncryptionPolicy(null);

        if (Utils.supportSplitView(mContext)
                && mContext.getResources().getBoolean(com.lge.R.bool.config_data_encrypt)) {
            DevicePolicyManager dpm = (DevicePolicyManager)mContext
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm == null) {
                return false;
            }
            if ((encryption == LGMDMManager.LGMDMENCRYPTION_DEVICE_AND_STORAGE || encryption == LGMDMManager.LGMDMENCRYPTION_DEVICE)
                    && (dpm.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE)) {
                Toast.makeText(mContext,
                        Utils.getString(R.string.sp_lgmdm_prevent_entering_menu_during_encryption),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    public boolean checkDeviceEncryption() {
        int encryption = LGMDMManager.getInstance().getEncryptionPolicy(null);
        if (encryption == LGMDMManager.LGMDMENCRYPTION_DEVICE_AND_STORAGE
                || encryption == LGMDMManager.LGMDMENCRYPTION_DEVICE) {
            return true;
        }
        return false;
    }

    public void setEncryptionSummary(boolean isEnable, int type, Context resContext,
            Preference mPhoneEncryption, Preference mSdCardEncryption) {
        int policyType = LGMDMManager.getInstance().getEncryptionPolicy(null);
        int easPolicyType = LGMDMManager.getInstance().getEncryptionPolicyForEas(null);

        switch (type) {
        case ENCRYPT:
            if (isEnable) {
                if (policyType == LGMDMENCRYPTION_DEVICE || easPolicyType == LGMDMENCRYPTION_DEVICE) {
                    Log.i(TAG, "setEncryptionSummary : [ENCRYPT][ENABLE][phone] : policyType = "
                            + policyType + ", easPolicyType = " + easPolicyType);
                    if (mPhoneEncryption != null) {
                        mPhoneEncryption.setSummary(R.string.sp_lgmdm_phone_encryption_done);
                        mPhoneEncryption.setEnabled(false);
                    }
                } else if (policyType == LGMDMENCRYPTION_STORAGE
                        || easPolicyType == LGMDMENCRYPTION_STORAGE) {
                    Log.i(TAG, "setEncryptionSummary : [ENCRYPT][storage] : policyType = "
                            + policyType + ", easPolicyType = " + easPolicyType);
                    if (mSdCardEncryption != null) {
                        mSdCardEncryption.setSummary(R.string.sp_lgmdm_storage_encryption_done);
                        mSdCardEncryption.setEnabled(false);
                    }
                } else if (policyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE
                        || easPolicyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE) {
                    Log.i(TAG, "setEncryptionSummary : [ENCRYPT][phone][storage] : policyType = "
                            + policyType + ", easPolicyType = " + easPolicyType);
                    if (mPhoneEncryption != null) {
                        mPhoneEncryption.setSummary(R.string.sp_lgmdm_phone_encryption_done);
                        mPhoneEncryption.setEnabled(false);
                    }
                    if (mSdCardEncryption != null) {
                        mSdCardEncryption.setSummary(R.string.sp_lgmdm_storage_encryption_done);
                        mSdCardEncryption.setEnabled(false);
                    }
                }
            } else {
                if (mPhoneEncryption == null) {
                    return;
                }
                if (policyType == LGMDMENCRYPTION_DEVICE
                        || policyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE
                        || easPolicyType == LGMDMENCRYPTION_DEVICE
                        || easPolicyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE) {
                    Log.i(TAG, "setEncryptionSummary : [ENCRYPT][DISABLE][phone] : policyType = "
                            + policyType + ", easPolicyType = " + easPolicyType);
                    mPhoneEncryption.setSummary(R.string.sp_lgmdm_phone_encryption_done);
                    mPhoneEncryption.setEnabled(false);
                }
            }
            break;
        case DECRYPT:
            if (mSdCardEncryption == null) {
                return;
            }
            if (isEnable) {
                if (policyType == LGMDMENCRYPTION_STORAGE
                        || policyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE
                        || easPolicyType == LGMDMENCRYPTION_STORAGE
                        || easPolicyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE) {
                    Log.i(TAG, "setEncryptionSummary : [DECRYPT][storage] : policyType = "
                            + policyType + ", easPolicyType = " + easPolicyType);
                    mSdCardEncryption.setSummary(R.string.sp_lgmdm_storage_encryption_done);
                    mSdCardEncryption.setEnabled(false);
                }
            }
            break;
        default:
            break;
        }
    }

    public boolean setOKEncryptionInsetSD(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (checkSDEncryption() == true) {
            activity.finish();
            return true;
        }
        return false;
    }

    public boolean checkSDEncryption() {
        int policyType = LGMDMManager.getInstance().getEncryptionPolicy(null);
        int easPolicyType = LGMDMManager.getInstance().getEncryptionPolicyForEas(null);
        if (policyType == LGMDMENCRYPTION_STORAGE
                || policyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE
                || easPolicyType == LGMDMENCRYPTION_STORAGE
                || easPolicyType == LGMDMENCRYPTION_DEVICE_AND_STORAGE) {
            return true;
        }
        return false;
    }

    public boolean setEncryptionInsetSD(AlertController.AlertParams p, Activity activity,
            EncryptWarningDialogActivity dialogActivity) {
        if (p == null || activity == null) {
            return false;
        }
        if (checkSDEncryption() == true) {
            p.mView = activity.getLayoutInflater().inflate(
                    R.layout.mdm_encrypt_warning_dialog_activity, null);
            p.mPositiveButtonText = activity.getResources().getString(R.string.dlg_ok);
            p.mPositiveButtonListener = dialogActivity;
            return true;
        }
        return false;
    }

    public void setMockLocationMenu(CheckBoxPreference pref) {
        if (pref == null) {
            return;
        }

        if (LGMDMManager.getInstance().getAllowMockLocation(null) == false) {
            Log.i(TAG, "setMockLocationMenu : LGMDM disallow MockLocation");
            pref.setSummary(R.string.sp_lgmdm_block_mock_location_NORMAL);
            pref.setEnabled(false);
            pref.setChecked(false);
        }
    }

    public boolean isShowManualSyncToastIfNeed(Context context) {
        if (context != null && checkDisabled(null, LGMDM_ADAPTER_EMAIL)) {
            Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_control_autosync_NORMAL),
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "send toast disallow manual sync");
            return true;
        }
        return false;
    }

    public void setDisallowAirplaneModeSummary_Tablet(Context mContext, TextView mSummary) {
        if (mSummary == null) {
            return;
        }
        if (Utils.supportSplitView(mContext)) {
            boolean isAllowAirplaneMode = LGMDMManager.getInstance().getAllowAirplaneModeOn(null);
            boolean isEnforceAirplaneMode = LGMDMManager.getInstance().getEnforceAirplaneMode(null);
            if (isAllowAirplaneMode == false || isEnforceAirplaneMode == true) {
                mSummary.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                mSummary.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                mSummary.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, mContext
                        .getResources().getDisplayMetrics()));
                mSummary.setTypeface(null, Typeface.BOLD);
            }
            if (isAllowAirplaneMode == false) {
                Log.i(TAG, "setDisallowAirplaneModeSummary_Tablet, disallow airplane mode");
                mSummary.setText(R.string.sp_lgmdm_block_airplane_NORMAL);
            } else if (isEnforceAirplaneMode == true) {
                Log.i(TAG, "setDisallowAirplaneModeSummary_Tablet, enforce airplane mode");
                mSummary.setText(R.string.sp_lgmdm_enforce_airplane_NORMAL);
            }
        }
    }

    public boolean setDisallowAirplaneModeSummary_VZW(ComponentName who, Context context,
            Switch mDisallowAirplaneModeCBPref) {
        if (mDisallowAirplaneModeCBPref == null) {
            return false;
        }
        if (LGMDMManager.getInstance().getAllowAirplaneModeOn(who) == false) {
            Log.i(TAG, "setDisallowAirplaneModeSummary_VZW : disallow AirplaneMode");
            mDisallowAirplaneModeCBPref.setEnabled(false);
            return true;
        }
        if (LGMDMManager.getInstance().getEnforceAirplaneMode(who) == true) {
            Log.i(TAG, "setDisallowAirplaneModeSummary_VZW : enforce AirplaneMode");
            mDisallowAirplaneModeCBPref.setEnabled(false);
            return true;
        }
        return false;
    }

    public boolean setDisallowAirplaneModeSummary(ComponentName who, Context context,
            TwoStatePreference mDisallowAirplaneModeCBPref) {
        if (mDisallowAirplaneModeCBPref == null) {
            return false;
        }
        if (LGMDMManager.getInstance().getAllowAirplaneModeOn(who) == false) {
            Log.i(TAG, "setDisallowAirplaneModeSummary : disallow AirplaneMode");
            mDisallowAirplaneModeCBPref.setChecked(false);
            mDisallowAirplaneModeCBPref.setSummary(R.string.sp_lgmdm_block_airplane_NORMAL);
            // mDisallowAirplaneModeCBPref.setEnabled(false);
            return true;
        }
        if (LGMDMManager.getInstance().getEnforceAirplaneMode(who) == true) {
            Log.i(TAG, "setDisallowAirplaneModeSummary : enforce AirplaneMode");
            mDisallowAirplaneModeCBPref.setChecked(false);
            mDisallowAirplaneModeCBPref.setSummary(R.string.sp_lgmdm_enforce_airplane_NORMAL);
            // mDisallowAirplaneModeCBPref.setEnabled(false);
            return true;
        }
        return false;
    }

    public boolean setUsbTetherMenu(CheckBoxPreference usbTether) {
        if (usbTether == null) {
            return false;
        }
        if (LGMDMManagerInternal.getInstance().getDisallowUSBType(IMdm.USB_TETHER)) {
            usbTether.setChecked(false);
            usbTether.setSummary(R.string.sp_block_usb_thering_NORMAL);
            usbTether.setEnabled(false);
            return true;
        }
        return false;
    }

    public void addExternalStoragePolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_EXTERNAL_STORAGE_POLICY_CHANGE);
    }

    public void addWipeDatePolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_WIPE_DATA_POLICY_CHANGE);
    }

    public void addBackupRestorePolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_GOOGLE_BACKUP_POLICY_CHANGE);
        filter.addAction(ACTION_AUTO_RESTORE_POLICY_CHANGE);
    }

    public void addUsbPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_USB_POLICY_CHANGE);
        filter.addAction(ACTION_TETHER_POLICY_CHANGE);
    }

    public void addManualSyncPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_MANUAL_SYNC_POLICY_CHANGE);
    }

    public void addDataUsageSettingPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_MOBILE_NETWORK_POLICY_CHANGE);
        filter.addAction(ACTION_DATA_ROAMING_POLICY_CHANGE);
        filter.addAction(ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE);
        filter.addAction(ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE);
        filter.addAction(ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN);
        filter.addAction(ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT);
    }

    public void addDevelopmentPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_USB_POLICY_CHANGE);
        filter.addAction(ACTION_PROCESS_LIMIT_POLICY_CHANGE);
        filter.addAction(ACTION_SCREEN_CAPTURE_POLICY_CHANGE);
        filter.addAction(ACTION_MOCK_LOCATION_POLICY_CHANGE);
        filter.addAction(ACTION_DEV_MODE_POLICY_CHANGE);
    }

    public void addRemoveAdminPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_REMOVE_ADMIN_POLICY_CHANGE);
    }

    public void addLocationPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_LOCATION_POLICY_CHANGE);
    }

    public void addMainSettingsPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_WIFI_POLICY_CHANGE);
        filter.addAction(ACTION_BLUETOOTH_POLICY_CHANGE);
        filter.addAction(ACTION_AIRPLANE_MODE_ON_POLICY_CHANGE);
        filter.addAction(ACTION_DUAL_WINDOW_POLICY_CHANGE);
        filter.addAction(ACTION_QUICKCIRCLE_POLICY_CHANGED);
        filter.addAction(ACTION_MOBILE_NETWORK_POLICY_CHANGE);
        filter.addAction(ACTION_DEVICE_ADMIN_DEACTIVATE);
        filter.addAction(ACTION_MANUAL_SYNC_POLICY_CHANGE);
        filter.addAction(ACTION_DEV_MODE_POLICY_CHANGE);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
    public void addQuickWindowCasePolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_QUICKCIRCLE_POLICY_CHANGED);
    }

    public void addWirelessSettingsPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_BLUETOOTH_POLICY_CHANGE);
        filter.addAction(ACTION_MOBILE_NETWORK_POLICY_CHANGE);
        filter.addAction(ACTION_TETHER_POLICY_CHANGE);
        filter.addAction(ACTION_WIFI_POLICY_CHANGE);
        filter.addAction(ACTION_USB_POLICY_CHANGE);
        filter.addAction(ACTION_VPN_POLICY_CHANGE);
        filter.addAction(ACTION_NFC_POLICY_CHANGE);
        filter.addAction(ACTION_WIRELESS_STORAGE_DISABLE);
    }

    public void addTetherPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_TETHER_POLICY_CHANGE);
        filter.addAction(ACTION_BLUETOOTH_POLICY_CHANGE);
        filter.addAction(ACTION_VPN_POLICY_CHANGE);
        filter.addAction(ACTION_USB_POLICY_CHANGE);
    }

    public void addMTKSimSettingChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_MOBILE_NETWORK_POLICY_CHANGE);
        filter.addAction(ACTION_DATA_ROAMING_POLICY_CHANGE);
    }

    public void addSecuritySettingChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_UNKNOWN_SOURCE_POLICY_CHANGE);
        filter.addAction(ACTION_SECURITY_SETTING_POLICY_CHANGE);
    }

    public void addEncryptionSettingChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_ENCRYPTION);
    }

    public void addDateTimeSettingChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_SYSTEM_TIME_POLICY_CHANGE);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
    public void addDualWindowSettingChangeIntentFilter(IntentFilter filter) {
        Log.i(TAG, "addDualWindowSettingChangeIntentFilter");
        filter.addAction(ACTION_DUAL_WINDOW_POLICY_CHANGE);
    }

    public boolean receiveExternalStoragePolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveExternalStoragePolicyChangeIntent action : " + intent.getAction());
        if (ACTION_EXTERNAL_STORAGE_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveWipeDateChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveWipeDateChangeIntent action : " + intent.getAction());
        if (ACTION_WIPE_DATA_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveBackupRestoreChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveBackupRestoreChangeIntent action : " + intent.getAction());
        if (ACTION_GOOGLE_BACKUP_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_AUTO_RESTORE_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveUsbPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveUsbPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_TETHER_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_USB_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveManualSyncPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveManualSyncPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_MANUAL_SYNC_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveDataUsageSettingPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveDataUsageSettingPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_MOBILE_NETWORK_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_BACKGROUND_DATA_RESTRICTED_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_CHANGING_MOBILE_DATAUSAGE_CYCLE.equals(intent.getAction())
                || ACTION_ENFORCE_MOBILE_DATAUSAGE_WARN.equals(intent.getAction())
                || ACTION_ENFORCE_MOBILE_DATAUSAGE_LIMIT.equals(intent.getAction())
                || ACTION_DATA_ROAMING_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveDevelopmentPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveDevelopmentPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_USB_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_MOCK_LOCATION_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_PROCESS_LIMIT_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_DEV_MODE_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_SCREEN_CAPTURE_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveRemoveAdminPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveRemoveAdminPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_REMOVE_ADMIN_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveLocationPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveLocationPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_LOCATION_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-432]
    public boolean receiveQuickWindowCaseChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveQuickWindowCaseChangeIntent action : " + intent.getAction());
        if (ACTION_QUICKCIRCLE_POLICY_CHANGED.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveMainSettingsPolicyChangeIntent(Intent intent, Context context) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveMainSettingsPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_WIFI_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_BLUETOOTH_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_AIRPLANE_MODE_ON_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_DUAL_WINDOW_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_QUICKCIRCLE_POLICY_CHANGED.equals(intent.getAction())
                || ACTION_MOBILE_NETWORK_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_DEVICE_ADMIN_DEACTIVATE.equals(intent.getAction())
                || ACTION_DEV_MODE_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_MANUAL_SYNC_POLICY_CHANGE.equals(intent.getAction())) {
            if (ACTION_DEVICE_ADMIN_DEACTIVATE.equals(intent.getAction()) && context != null) {
                if (checkEasySettingAlive(context) == false) {
                    if (mToastTime == 0 || (System.currentTimeMillis() - mToastTime) > 1000) {
                        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_dis_deactivated),
                                Toast.LENGTH_SHORT).show();
                    }
                    mToastTime = System.currentTimeMillis();
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkEasySettingAlive(Context context) {
        if (context == null) {
            return false;
        }

        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppList = null;
        if (am != null) {
            runningAppList = am.getRunningAppProcesses();
        }

        if (runningAppList == null) {
            return false;
        }

        for (RunningAppProcessInfo rapi : runningAppList) {
            String[] runningPackageList = rapi.pkgList;
            for (String pkgName : runningPackageList) {
                if (pkgName.equals(EASY_SETTING_PACKAGE)) {
                    Log.i(TAG, "[checkEasySettingAlive] EasySetting Alive");
                    return true;
                }
            }
        }
        Log.i(TAG, "[checkEasySettingAlive] EasySetting not Alive");
        return false;
    }

    public boolean receiveWirelessSettingsPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveWirelessSettingsPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_TETHER_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_MOBILE_NETWORK_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_WIFI_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_BLUETOOTH_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_USB_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_VPN_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_NFC_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_WIRELESS_STORAGE_DISABLE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveTetherPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveTetherPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_TETHER_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_BLUETOOTH_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_VPN_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_USB_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveMTKSimSettingChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveMTKSimSettingChangeIntent action : " + intent.getAction());
        if (ACTION_MOBILE_NETWORK_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_DATA_ROAMING_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveSecuritySettingChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveSecuritySettingChangeIntent action : " + intent.getAction());
        if (ACTION_SECURITY_SETTING_POLICY_CHANGE.equals(intent.getAction())
                || ACTION_UNKNOWN_SOURCE_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveSecurityEncryptionChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveSecurityEncryptionChangeIntent action : " + intent.getAction());
        if (ACTION_ENCRYPTION.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean receiveDateTimeSettingChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveDateTimeSettingChangeIntent action : " + intent.getAction());
        if (ACTION_SYSTEM_TIME_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
    public boolean receiveDualWindowPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveDualWindowPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_DUAL_WINDOW_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }


    /**
     * @return true-can't remove account. false-remove account.
     */
    public boolean isShowRemoveAccountToastIfNeed(Context context, Account account) {
        if (isAccountByMDM(context, account) == true || isAccountByGoogle(context, account) == true) {
            return true;
        }
        return false;
    }

    /**
     * @return true-can't remove account. false-remove account.
     */
    private boolean isAccountByGoogle(Context context, Account account) {
        if ("com.google".equals(account.type)) {
            if (LGMDMManager.getInstance().getAllowRemoveGoogleAccount(null) == false) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_deleted_google_account_NORMAL),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    /**
     * @return true-can't remove account. false-remove account.
     */
    private boolean isAccountByMDM(Context context, Account account) {
        boolean isMdmAccount = false;

        if (context == null || account == null
                || account.type == null || account.name == null) {
            return false;
        }

        isMdmAccount = LGMDMManagerInternal.getInstance().isAccountByMDM(context, account);
        if (isMdmAccount) {
            Toast.makeText(context,
                    Utils.getString(R.string.sp_lgmdm_block_remove_account_NORMAL),
                    Toast.LENGTH_SHORT).show();
        }

        return isMdmAccount;
    }

    public boolean setUsbTetheringMenu(TwoStatePreference usbMenu) {
        if (usbMenu == null) {
            return false;
        }

        if (LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_USB)) {
            usbMenu.setEnabled(false);
            usbMenu.setChecked(false);
            usbMenu.setSummary(R.string.sp_block_usb_thering_NORMAL);
            return true; // disallow
        }
        return false; // allow
    }

    public boolean setBluetoothTetheringMenu(TwoStatePreference btMenu) {
        if (btMenu == null) {
            return false;
        }

        if (LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_BLUETOOTH)) {
            btMenu.setEnabled(false);
            btMenu.setChecked(false);
            btMenu.setSummary(R.string.sp_block_bt_tethering_NORMAL);
            return true; // disallow
        }
        return false; // allow
    }

    public boolean checkDisabledUsbType(int type) {
        if (type > IMdm.USB_TYPE_MAX) {
            Log.w(TAG, "checkDisabledUsbType : unknown type :" + type);
            return false;
        }
        return LGMDMManagerInternal.getInstance().getDisallowUSBType(type);
    }

    private final boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, -1) == 1;
    }

    public void createLGMDMDeviceAdminInfo(Context context, ResolveInfo ri) {
        try {
            mLGMDMDeviceAdmin = new LGMDMDeviceAdminInfo(context, ri);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Unable to retrieve LGMDM device policy ", e);
        } catch (IOException e) {
            Log.w(TAG, "Unable to retrieve LGMDM device policy ", e);
        }
    }

    public void addLGMDMDeivceAdminPolicyInfo(ArrayList<DeviceAdminInfo.PolicyInfo> policies) {
        if (mLGMDMDeviceAdmin == null) {
            return;
        }
        ArrayList<DeviceAdminInfo.PolicyInfo> mdmPolicies = mLGMDMDeviceAdmin.getUsedPolicies();
        if (mdmPolicies != null && mdmPolicies.size() > 0) {
            policies.addAll(mdmPolicies);

            DeviceAdminInfo.PolicyInfo multiUserPolicy = null;
            int index = -1;
            for (int i = 0; i < policies.size(); i++) {
                DeviceAdminInfo.PolicyInfo mdmPolicy = policies.get(i);
                if (mdmPolicy.tag.equals("lg-disable-multiuser")) {
                    multiUserPolicy = mdmPolicy;
                    index = i;
                }
            }
            if (multiUserPolicy != null && index != -1) {
                 policies.remove(index);
                 policies.add(0, multiUserPolicy);
            }
        }
    }

    public boolean checkLGMDMUsePolicies(ComponentName cn) {
        if (mLGMDMDeviceAdmin == null) {
            return false;
        }

        ArrayList<DeviceAdminInfo.PolicyInfo> newPolicies = mLGMDMDeviceAdmin.getUsedPolicies();
        for (int i = 0; i < newPolicies.size(); i++) {
            DeviceAdminInfo.PolicyInfo pi = newPolicies.get(i);
            if (!LGMDMManagerInternal.getInstance().hasGrantedPolicy(cn, pi.ident)) {
                return true;
            }
        }
        return false;
    }

    public boolean setWifiApEnablerMenu(Preference pref) {
        if (pref == null) {
            return false;
        }

        if (LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_HOTSPOT)) {
            pref.setEnabled(false);
            pref.setSummary(R.string.sp_block_wifi_hotspot_NORMAL);
            Log.i(TAG, "setWifiApEnablerMenu : LGMDM Block Hotspot menu");
            return true;
        }
        return false;
    }

    public boolean checkDisallowHotspotWithToast(Context context) {
        if (context == null) {
            return false;
        }

        if (LGMDMManagerInternal.getInstance().getDisallowTetheringType(IMdm.TETHER_HOTSPOT)) {
            Log.i(TAG, "setWifiApEnablerMenu : LGMDM Block Hotspot menu");
            Toast.makeText(context, Utils.getString(R.string.sp_block_wifi_hotspot_NORMAL),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public boolean isShowEnforceBackgroundDataRestrictedToastIfNeed(Context context) {
        if (LGMDMManager.getInstance().getEnforceBackgroundDataRestricted(null) == false) {
            Toast.makeText(context,
                    Utils.getString(R.string.sp_lgmdm_block_restrict_background_data_NORMAL),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void checkEnforceBackgroundDataRestrictedPolicy(MenuItem mMenuRestrictBackground,
            CheckBox mBackgroundSwitdh) {
        if (mMenuRestrictBackground == null && mBackgroundSwitdh == null) {
            return;
        }
        if (LGMDMManager.getInstance().getEnforceBackgroundDataRestricted(null) == false) {
            if (mMenuRestrictBackground != null) {
                mMenuRestrictBackground.setChecked(true);
            } else if (mBackgroundSwitdh != null) {
                mBackgroundSwitdh.setEnabled(true);
                mBackgroundSwitdh.setChecked(true);
            }
        }
    }

    public void setWirelessStorageMenu(Preference preference) {
        if (preference == null) {
            return;
        }
        int policy = LGMDMManager.getInstance().getAllowWirelessStorage(null);
        if (policy == LGMDMManager.WIRELESS_STORAGE_DISALLOW) {
            Log.i(TAG, "disallow wireless storage");
            preference.setEnabled(false);
            preference.setSummary(R.string.sp_lgmdm_wireless_storage_disable_NORMAL);
        } else {
            preference.setEnabled(true);
            preference.setSummary(R.string.wireless_storage_settings_info);
        }
    }

    public void setPasswordTypingVisibleMenu(CheckBoxPreference pref) {
        if (pref == null) {
            return;
        }
        if (LGMDMManager.getInstance().getAllowPasswordTypingVisible(null) == false) {
            Log.i(TAG, "LGMDM disallow PasswordTypingVisible menu");
            pref.setSummary(R.string.sp_lgmdm_block_password_typing_visible_NORMAL);
            pref.setEnabled(false);
        }
    }

    public void setSmartShareBeamMenu(Preference preference) {
        boolean disallow = checkDisallowSmartShareBeam(null, false);
        if (disallow) {
            Log.i(TAG, "setSmartShareBeamMenu : LGMDM Block SmartShareBeam menu");
            if (preference != null) {
                preference.setEnabled(false);
                preference.setSummary(R.string.sp_lgmdm_block_smartshare_beam_NORMAL);
            }
        }
    }

    public boolean checkDisallowSmartShareBeam(Context context, boolean needToast) {
        boolean wifiPolicy = LGMDMManager.getInstance().getAllowWifi(null);
        boolean wifiDirectPolicy = LGMDMManager.getInstance().getAllowWifiDirect(null);
        if (wifiPolicy == false || wifiDirectPolicy == false) {
            Log.i(TAG, "checkDisallowSmartShareBeam : LGMDM Block SmartShareBeam");
            if (needToast && context != null) {
                Toast.makeText(context,
                        Utils.getString(R.string.sp_lgmdm_block_smartshare_beam_point),
                        Toast.LENGTH_SHORT);
            }
            return true; // disallow
        }
        return false; // allow
    }

    public boolean setUSBHostStorageEnableMenu(Preference externalmenu) {
        if (externalmenu == null) {
            return false;
        }

        if (LGMDMManager.getInstance().getAllowUSBHostStorage(null) == false) {
            externalmenu.setEnabled(false);
            externalmenu.setSummary(R.string.sp_lgmdm_block_usb_storage_NORMAL);

            Log.i(TAG, "LGMDM Block USBHostStorageEnableMenu");
            return true;
        }

        return false;
    }

    public boolean setMiracastPreference(Preference pref) {
        if (pref == null) {
            return false;
        }

        if (LGMDMManager.getInstance().getAllowWifi(null) == false
                || LGMDMManager.getInstance().getAllowWifiDirect(null) == false
                || LGMDMManager.getInstance().getAllowMiracast(null) == false) {
            pref.setEnabled(false);
            pref.setSummary(R.string.sp_lgmdm_block_miracast_NORMAL);
            Log.i(TAG, "setMiracastPreference : LGMDM Block Miracast menu");
            return true;
        }
        return false;
    }

    // true : Allow multi user, false: Disallow multi user
    public boolean checkAllowMultiUser() {
        return LGMDMManager.getInstance().getAllowMultiUser(null);
    }

    public void addMultiUserPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_MULTI_USER_POLICY_CHANGE);
    }

    public boolean receiveMultiUserPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveMultiUserPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_MULTI_USER_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean checkDisallowNfc(String type) {
        boolean ret = false;
        int nfcPolicy = LGMDMManager.getInstance().getAllowNfc(null);
        boolean androidBeamPolicy = LGMDMManager.getInstance().getAllowAndroidBeam(null);
        if (LGMDM_NFC_SYS.equals(type)) {
            ret = (nfcPolicy == LGMDMManager.NFC_DISALLOW);
        } else if (LGMDM_NFC_CARD_MODE.equals(type)) {
            ret = (nfcPolicy != LGMDMManager.NFC_ALLOW);
        } else if (LGMDM_NFC_ANDROID_BEAM.equals(type)) {
            ret = (nfcPolicy != LGMDMManager.NFC_ALLOW || androidBeamPolicy == false);
        }
        return ret;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-426]
    public boolean checkDisallowDualWindow(Context mContext) {
        boolean dwPolicy = LGMDMManager.getInstance().getAllowDualWindow(null);
        if (dwPolicy != true) {
            Toast.makeText(mContext,
                    Utils.getString(R.string.sp_lgmdm_block_dualwindow),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void setAndroidBeamMenu(Switch androidBeamSwitch, TextView tv) {
        if (checkDisallowNfc(LGMDM_NFC_ANDROID_BEAM)) {
            if (androidBeamSwitch != null) {
                androidBeamSwitch.setEnabled(false);
            }
            if (tv != null) {
                tv.setText(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        }
    }

    public void setNfcEnablerMenu(Context context, TwoStatePreference nfcOnOff,
            PreferenceScreen beamPref, TwoStatePreference beamSwitch) {
        if (checkDisallowNfc(LGMDM_NFC_SYS)) {
            if (nfcOnOff != null) {
                nfcOnOff.setEnabled(false);
                if (context != null
                        && (Utils.hasFeatureNfcP2P() || Utils.isWifiOnly(context)
                                || Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())) {
                    nfcOnOff.setSummary(R.string.sp_lgmdm_block_nfc_NORMAL);
                } else {
                    nfcOnOff.setSummaryOff(R.string.sp_lgmdm_block_nfc_NORMAL);
                }
            }
            if (beamPref != null) {
                beamPref.setEnabled(false);
                beamPref.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
            if (beamSwitch != null) {
                beamSwitch.setEnabled(false);
                beamSwitch.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        } else if (checkDisallowNfc(LGMDM_NFC_CARD_MODE)) {
            if (nfcOnOff != null) {
                NfcAdapterAddon nfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
                boolean nfcOn = false;
                if (nfcAdapterAddon != null) {
                    nfcOn = nfcAdapterAddon.isNfcSystemEnabled();
                }
                if (nfcOn
                        && LGMDMManager.getInstance().getAllowNfc(null) == LGMDMManager.NFC_ALLOW_CARDMODE) {
                    if (context != null
                            && (Utils.hasFeatureNfcP2P() || Utils.isWifiOnly(context)
                                    || Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled())) {
                        nfcOnOff.setSummary(R.string.sp_lgmdm_block_nfc_cardmode);
                    } else {
                        nfcOnOff.setSummaryOff(R.string.sp_lgmdm_block_nfc_cardmode);
                    }
                }
            }
            if (beamPref != null) {
                beamPref.setEnabled(false);
                beamPref.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
            if (beamSwitch != null) {
                beamSwitch.setEnabled(false);
                beamSwitch.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        } else if (checkDisallowNfc(LGMDM_NFC_ANDROID_BEAM)) {
            if (beamPref != null) {
                beamPref.setEnabled(false);
                beamPref.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
            if (beamSwitch != null) {
                beamSwitch.setEnabled(false);
                beamSwitch.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        }
    }

    public void setNfcSettingsMenu(Context context, Switch nfcSwitch, TwoStatePreference p2pOnOff,
            TwoStatePreference androidBeam) {
        if (checkDisallowNfc(LGMDM_NFC_SYS)) {
            if (nfcSwitch != null) {
                nfcSwitch.setEnabled(false);
            }
            if (p2pOnOff != null) {
                p2pOnOff.setEnabled(false);
                p2pOnOff.setSummary(R.string.sp_lgmdm_block_readwrite_p2p_NORMAL);
            }
            if (androidBeam != null) {
                if (context != null && (Utils.hasFeatureNfcInner() || Utils.isWifiOnly(context))) {
                    androidBeam.setEnabled(false);
                    androidBeam.setChecked(false);
                }

                androidBeam.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        } else if (checkDisallowNfc(LGMDM_NFC_CARD_MODE)) {
            if (p2pOnOff != null) {
                p2pOnOff.setEnabled(false);
                p2pOnOff.setSummary(R.string.sp_lgmdm_block_readwrite_p2p_NORMAL);
            }
            if (androidBeam != null) {
                if (context != null && (Utils.hasFeatureNfcInner() || Utils.isWifiOnly(context))) {
                    androidBeam.setEnabled(false);
                    androidBeam.setChecked(false);
                }
                androidBeam.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        } else if (checkDisallowNfc(LGMDM_NFC_ANDROID_BEAM)) {
            if (androidBeam != null) {
                if (context != null && (Utils.hasFeatureNfcInner() || Utils.isWifiOnly(context))) {
                    androidBeam.setEnabled(false);
                    androidBeam.setChecked(false);
                }
                androidBeam.setSummary(R.string.sp_lgmdm_block_androidbeam_NORMAL);
            }
        }
    }

    public void addNfcPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_NFC_POLICY_CHANGE);
    }

    public boolean receiveNfcChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveNfcChangeIntent action : " + intent.getAction());
        if (ACTION_NFC_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public void addVpnPolicyChangeIntentFilter(IntentFilter filter) {
        filter.addAction(ACTION_VPN_POLICY_CHANGE);
    }

    public boolean receiveVpnPolicyChangeIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Log.i(TAG, "receiveVpnPolicyChangeIntent action : " + intent.getAction());
        if (ACTION_VPN_POLICY_CHANGE.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public boolean checkDisabledVpnConnect(Context context) {
        if (LGMDMManager.getInstance().getAllowVpn(null)) {
            return false; // allow
        }
        Log.i(TAG, "checkDisabledVpnConnect : disallow vpn");
        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_vpn_connection_point),
                Toast.LENGTH_SHORT).show();
        return true; // disallow
    }

    public void setVpnMenu(Preference vpnPreference) {
        if (vpnPreference == null) {
            return;
        }

        if (LGMDMManager.getInstance().getAllowVpn(null) == false) {
            vpnPreference.setSummary(R.string.sp_lgmdm_block_vpn_connection_NORMAL);
            Log.i(TAG, "LGMDM Block VPN menu");
            vpnPreference.setEnabled(false);
        }
    }

    public void setVpnSelectorMenu(Preference vpnSettings, Preference lgVpn) {
        if (LGMDMManager.getInstance().getAllowVpn(null)) {
            return;
        }

        Log.i(TAG, "setVpnSelectorMenu : LGMDM Block VpnSelector menu");
        if (vpnSettings != null) {
            vpnSettings.setEnabled(false);
            vpnSettings.setSummary(R.string.sp_lgmdm_block_vpn_connection_NORMAL);
        }
        if (lgVpn != null) {
            lgVpn.setEnabled(false);
            lgVpn.setSummary(R.string.sp_lgmdm_block_vpn_connection_NORMAL);
        }
    }

    public boolean googleErrorReportDisallowed(Context context) {
        if (context == null) {
            return false;
        }
        int policy = LGMDMManager.getInstance().getAllowGoogleErrorReport(null);
        if (policy > LGMDMManager.GOOGLE_ERROR_REPORT_ALLOW) {
            Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_cannot_google_crash_report),
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "googleErrorReportDisallowed : LGMDM Block Google Error Report");
            return true;
        }
        return false;
    }

    public void setDateAndTimeMenu(CheckBoxPreference autoTimePref, Preference datePref,
            Preference timePref) {
        if (LGMDMManager.getInstance().getAllowChangeDateAndTime(null)) {
            return;
        }

        Log.i(TAG, "setDateAndTimeMenu : LGMDM block date and time menu");
        if (autoTimePref != null) {
            autoTimePref.setSummary(R.string.sp_lgmdm_enable_date_time_NORMAL);
            autoTimePref.setSummaryOff(R.string.sp_lgmdm_enable_date_time_NORMAL);
            autoTimePref.setSummaryOn(R.string.sp_lgmdm_enable_date_time_NORMAL);
            autoTimePref.setEnabled(false);
        }
        if (autoTimePref != null) {
            datePref.setEnabled(false);
        }
        if (autoTimePref != null) {
            timePref.setEnabled(false);
        }
    }

    public void setTimeZoneMenu(CheckBoxPreference autoTimeZonePref, Preference timeZonePref) {
        if (LGMDMManager.getInstance().getAllowChangeTimezone(null)) {
            return;
        }

        Log.i(TAG, "setTimeZoneMenu : LGMDM block time zone menu");
        if (autoTimeZonePref != null) {
            autoTimeZonePref.setSummary(R.string.sp_lgmdm_enable_timezone_NORMAL);
            autoTimeZonePref.setSummaryOff(R.string.sp_lgmdm_enable_timezone_NORMAL);
            autoTimeZonePref.setSummaryOn(R.string.sp_lgmdm_enable_timezone_NORMAL);
            autoTimeZonePref.setEnabled(false);
        }
        if (timeZonePref != null) {
            timeZonePref.setEnabled(false);
        }
    }

    // ID-MDM-169 check policy when cryptkeeper case.
    protected boolean getDiallowedFactoryReset() {
        File file = new File(LGMDM_CHECK_PATH);
        if (file.exists()) {
            Log.w(TAG, "getDiallowedFactoryReset() exists");
            return true;
        } else {
            Log.w(TAG, "getDiallowedFactoryReset() not exists");
            return false;
        }
    }

    public boolean setDataUsageCycle(Context context, String tabInfo) {
        if ((tabInfo == null) || "wifi".equals(tabInfo) || "ethernet".equals(tabInfo)
                || (LGMDMManager.getInstance().getAllowChangingMobileDataUsageCycle(null) == true)) {
            return false;
        }
        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_period),
                Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean onClickDataUsageLimitMenu() {
        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return false;
        }
        return true;
    }

    public void setDataUsageLimitMenu(CheckBox mDisableAtLimit, View mDisableAtLimitView) {
        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return;
        }
        if (mDisableAtLimit == null) {
            return;
        }
        mDisableAtLimit.setChecked(true);
        mDisableAtLimit.setEnabled(false);
        if (mDisableAtLimitView != null) {
            final TextView summary = (TextView)mDisableAtLimitView
                    .findViewById(android.R.id.summary);
            summary.setVisibility(View.VISIBLE);
            summary.setText(R.string.sp_lgmdm_block_limit_mobile_data);
            summary.setTextColor(Color.GRAY);

            final TextView title = (TextView)mDisableAtLimitView.findViewById(android.R.id.title);
            title.setTextColor(Color.GRAY);
        }
    }

    public boolean setDataUsageLimitToast(Context context) {
        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return false;
        }
        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_data),
                Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean setDataUsageLimitChart(ChartSweepView sweep, ChartSweepView mSweepLimit,
            DataUsageChartListener mListener) {
        boolean ret = false;
        if (LGMDMManager.getInstance().getEnforceLimitMobileDataUsage(null) == false) {
            return ret;
        }
        if (sweep == mSweepLimit && mListener != null) {
            ret = true;
        }
        return ret;
    }

    public boolean onClickDataUsageWarnMenu() {
        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return false;
        }
        return true;
    }

    public void setDataUsageWarnMenu(CheckBox mDisableAtWarn, View mDisableAtWarnView) {
        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return;
        }
        if (mDisableAtWarn == null) {
            return;
        }
        mDisableAtWarn.setChecked(true);
        mDisableAtWarn.setEnabled(false);
        if (mDisableAtWarnView != null) {
            final TextView summary = (TextView)mDisableAtWarnView
                    .findViewById(android.R.id.summary);
            summary.setVisibility(View.VISIBLE);
            summary.setText(R.string.sp_lgmdm_warning_limit_mobile_data);
            summary.setTextColor(Color.GRAY);

            final TextView title = (TextView)mDisableAtWarnView.findViewById(android.R.id.title);
            title.setTextColor(Color.GRAY);
        }
    }

    public boolean setDataUsageWarnToast(Context context) {
        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return false;
        }
        Toast.makeText(context, Utils.getString(R.string.sp_lgmdm_block_change_mobile_data),
                Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean setDataUsageWarnChart(ChartSweepView sweep, ChartSweepView mSweepWarn,
            DataUsageChartListener mListener) {
        boolean ret = false;
        if (LGMDMManager.getInstance().getEnforceAlertMobileDataUsage(null) == false) {
            return ret;
        }
        if (sweep == mSweepWarn && mListener != null) {
            ret = true;
        }
        return ret;
    }
}
