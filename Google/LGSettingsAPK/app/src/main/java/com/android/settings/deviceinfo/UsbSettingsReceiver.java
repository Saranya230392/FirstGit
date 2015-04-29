package com.android.settings.deviceinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;
import android.os.SystemProperties;
//[Start][22/05/2014][vinodh.kumara@lge.com]Added for USB tethering deactivation when sim subcribtion change for Spain_TLF
import com.android.internal.telephony.IccCardConstants;
//[End][22/05/2014][vinodh.kumara@lge.com]Added for USB tethering deactivation when sim subcribtion change for Spain_TLF
import android.content.pm.PackageManager;

import com.lge.constants.UsbManagerConstants;

public class UsbSettingsReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbSettingsReceiver";

    private static Context mContext = null;
    private static Intent mIntent;
    public static boolean sprintusbtethering = false;
    public static boolean sCTCusbtethering = false;

    private static ConnectivityManager mConnectivityManager = null;
    private static UsbManager mUsbManager = null;
    private static String mDefaultFunction = "";

    private static String[] mRejectPackageList = {
            "com.google.android.setupwizard", "com.android.LGSetupWizard" };

    private static String[] mRejectClassList = {
            "com.google.android.gsf.login.LoginActivityTask",
            "com.android.phone.InCallScreen",
            "com.lge.vt.ui.VgaVideoCallActivity",
            "com.lge.vt.ui.QcifVideoCallActivity",
            "com.lge.shutdownmonitor.ShutdownMonitorActivity",
            "com.android.phone.InVideoCallScreen",
            "com.lge.vt.ui.GroupVideoCallActivity",
            "com.lge.vt.ui.IncomingCallActivity",
            "com.lge.vt.ui.OutgoingCallActivity",
            "com.android.phone.DualSimSetupWizard",
            "com.android.phone.EmergencyCallbackModeActivity",
            "com.lge.ota.SKTUsimDownloadActivity",
            "com.lge.ota.LGTNoUSIMActivity",
            "com.lge.ota.LGTNoUSIMActivityForLockScreen",
            "com.lge.ota.LGTUsimDownloadActivity",
            "com.android.settings.lgesetting.wireless.DataEnabledSettingBootableSKT",
            "com.android.settings.lgesetting.wireless.DataNetworkModeSetting",
            "com.android.settings.lgesetting.wireless.DataNetworkModePayPopupLGT",
            "com.android.settings.lgesetting.wireless.DataNetworkModePayPopupKT",
            "com.android.settings.lgesetting.wireless.DataRoamingSettingsBootableSKT",
            "com.android.settings.lgesetting.wireless.DataRoamingSettingsSKT",
            "com.android.settings.lgesetting.wireless.DataRoamingSettingsLGU",
            "com.android.settings.lgesetting.wireless.DataRoamingSettingsKT",
            "com.lge.ota.KTRegiActivity",
            "com.lge.ota.KTNoUSIMActivityForLockScreen",
            "com.lge.ota.KTResultActivity",
            "com.android.phone.LGOtaCallActivity",
            "com.android.phone.OtaTemporaryActivity" };

    public static void sprintUsbTetheringON(Context context) {
        if (Config.getOperator().equals(Config.SPRINT)) {
            if (context != null) {
                String userdebugmode = SystemProperties.get(
                        "persist.service.usb_ther", "");
                if (userdebugmode.contains("true")) {
                    Log.d(TAG, "[AUTORUN] onReceive() : SystemProperties");

                    String usbtetheron = SystemProperties.get(
                            "persist.sys.usb.state", "");
                    if (usbtetheron.contains("on")) {
                        ConnectivityManager cm = (ConnectivityManager)context
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (cm != null) {
                            Log.d(TAG,
                                    "[AUTORUN] onReceive() : ConnectivityManager");
                            if (cm.setUsbTethering(true) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                                Log.d(TAG,
                                        "[AUTORUN] ============ USB Tethering ERROR !! ============");
                                return;
                            }
                            UsbSettingsControl.setTetherStatus(context, true);
                            UsbSettingsControl.mTetherChanged = true;

                            sprintusbtethering = true;
                        }
                    }
                }
            }
        }
    }

    public static void ctcUsbTetheringON(Context context) {
        if (("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                && Utils.isSupportUSBMultipleConfig(context)) {
            if (!UsbSettingsControl.getUsbConnected(context)) {
                return;
            }

            boolean mCheckCTHidden = SystemProperties.getBoolean(
                    "persist.service.usb_ther_always", false);
            if (mCheckCTHidden) {
                ConnectivityManager cm = (ConnectivityManager)context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    if (cm.setUsbTethering(true) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        Log.d(TAG,
                                "[AUTORUN] ============ USB Tethering ERROR (CTC, CTO Set) !! ============");
                        return;
                    }
                    UsbSettingsControl.setTetherStatus(context, true);
                    UsbSettingsControl.mTetherChanged = true;
                    sCTCusbtethering = true;
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;
        mContext = context;
        String action = mIntent.getAction();

        Log.d(TAG, "[AUTORUN] onReceive() : action = " + action);

        Log.d(TAG,
                "[AUTORUN] sys.usb.config = "
                        + SystemProperties.get("sys.usb.config", ""));
        Log.d(TAG,
                "[AUTORUN] sys.usb.state = "
                        + SystemProperties.get("sys.usb.state", ""));
        Log.d(TAG,
                "[AUTORUN] persist.sys.usb.config = "
                        + SystemProperties.get("persist.sys.usb.config", ""));

        Log.d(TAG,
                "[AUTORUN] onReceive() : app userid = "
                        + UserHandle.getUserId(context.getApplicationInfo().uid)
                        + ", current userid = "
                        + ActivityManager.getCurrentUser());

        if (UserHandle.getUserId(context.getApplicationInfo().uid) != ActivityManager
                .getCurrentUser()) {
            return;
        }

        if (Utils.isMonkeyRunning()) {
            return;
        }

        if (mUsbManager == null) {
            mUsbManager = (UsbManager)mContext
                    .getSystemService(mContext.USB_SERVICE);
        }
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager)mContext
                    .getSystemService(mContext.CONNECTIVITY_SERVICE);
        }

        if (UsbSettingsControl.isLCDCrackATT()) {
            changeSettingsLCDCrack();
        }

        // [Start][22/05/2014][vinodh.kumara@lge.com]Added for USB tethering
        // deactivation when sim subcribtion change for Spain_TLF
        if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            Log.d(TAG, "android.intent.action.SIM_STATE_CHANGED");
            Log.d(TAG, "Target Operator" + Config.getOperator());
            Log.d(TAG, "Target Country" + Config.getCountry());
            Log.d(TAG,
                    "stateExtra"
                            + intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE));
            Log.d(TAG, "INTENT_VALUE_ICC_IMSI"
                    + IccCardConstants.INTENT_VALUE_ICC_IMSI);

            if ((Config.getOperator().equals("TLF") && Config.getCountry()
                    .equals("ES"))
                    || (Config.getOperator().equals("TLF") && Config
                            .getCountry().equals("COM"))) {
                final String stateExtra = intent
                        .getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                if (stateExtra != null) {
                    if (IccCardConstants.INTENT_VALUE_ICC_LOADED
                            .equals(stateExtra)) {
                        if (UsbSettingsControl.getTetherStatus(mContext)) {
                            Log.d(TAG,
                                    "IMSI changes and tether status true and disabling the USB tethering");
                            ConnectivityManager cm = (ConnectivityManager)mContext
                                    .getSystemService(Context.CONNECTIVITY_SERVICE);
                            mUsbManager
                                    .setCurrentFunction(
                                            UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY,
                                            true);
                            cm.setUsbTethering(false); // Added for LOS
                        }
                    }
                }
            }
        }
        // [End][22/05/2014][vinodh.kumara@lge.com]Added for USB tethering
        // deactivation when sim subcribtion change for Spain_TLF

        if (action.equals(UsbManager.ACTION_USB_STATE)) {
            mDefaultFunction = mUsbManager.getDefaultFunction();

            boolean usbConnected = intent.getBooleanExtra(
                    UsbManager.USB_CONNECTED, false);
            boolean usbConfigured = intent.getBooleanExtra(
                    UsbManager.USB_CONFIGURED, false);

            Log.d(TAG, "[AUTORUN] onReceive() : getDefaultFunction = "
                    + mDefaultFunction);
            Log.d(TAG,
                    "[AUTORUN] onReceive() : USB_FUNCTION_CDROM_STORAGE = "
                            + intent.getBooleanExtra(
                                    UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE,
                                    false));
            Log.d(TAG, "[AUTORUN] onReceive() : mDirectAutorun = "
                    + UsbSettingsControl.mDirectAutorun);
            Log.d(TAG, "[AUTORUN] onReceive() : mActivityUsbModeChange = "
                    + UsbSettingsControl.mActivityUsbModeChange);
            Log.d(TAG, "[AUTORUN] onReceive() : mActivityFinish = "
                    + UsbSettingsControl.mActivityFinish);

            if (usbConnected == true && usbConfigured == false) {
                Log.d(TAG, "[AUTORUN] onReceive() : ===== USB Connected =====");

                UsbSettingsControl.setUsbConnected(context, true);
            } else if (usbConnected == true && usbConfigured == true) {
                Log.d(TAG, "[AUTORUN] onReceive() : ===== USB Configured =====");

                UsbSettingsControl.setUsbConnected(context, true);

                boolean isQMICM = false;
                String checkQmicm = SystemProperties.get("sys.usb.config", "");
                Log.d(TAG, "[AUTORUN] onReceive() : checkQmicm = " + checkQmicm);
                if (checkQmicm.contains("qmicm")) {
                    isQMICM = true;
                }

                if (intent.getBooleanExtra(
                        UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false)
                        && UsbSettingsControl.mDirectAutorun
                        && !UsbSettingsControl.mActivityUsbModeChange
                        && !UsbSettingsControl.isDisconnectBugModel()
                        && !isQMICM) {
                    UsbSettingsControl.startAutorunTimer(mContext);
                    UsbSettingsControl.mActivityFinish = false;
                    setUsbConnect();
                } else {
                    setUsbConnect();
                }

                if (sprintusbtethering) {
                    sprintUsbTetheringON(mContext);
                }

                if (sCTCusbtethering) {
                    ctcUsbTetheringON(mContext);
                }
            } else if (usbConnected == false && usbConfigured == false) {
                Log.d(TAG,
                        "[AUTORUN] onReceive() : ===== USB Disconnected =====");

                UsbSettingsControl.setUsbConnected(context, false);

                UsbSettingsControl.mTetherChanged = false;

                setUsbDisconnect();
            } else {
                Log.d(TAG,
                        "[AUTORUN] onReceive() : ===== USB Unknown Connected ====");

                UsbSettingsControl.setUsbConnected(context, false);
            }
        } else if (action.equals("com.lge.setup_wizard.AUTORUNON")) {
            if (UsbSettingsControl.getUsbConnected(context)) {
                setUsbConnect();
            }
        } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_ACK)) {
            if ("VZW".equals(Config.getOperator())) {
                return;
            }
        } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_CHANGE_MODE)) {
            if ("VZW".equals(Config.getOperator())) {
                return;
            }
            UsbSettingsControl.cancelAutorunTimer();

            String value = UsbSettingsControl
                    .readToFile(UsbSettingsControl.AUTORUN_USBMODE);
            if (value != null) {
                if (value.equals(UsbSettingsControl.USBMODE_MTP)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_MTP_ONLY, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_TETHER)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_TETHER, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_PCSUITE)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_PC_SUITE, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_AUTOCONF)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_AUTO_CONF, true);
                }
            }
        } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_TIMEOUT)) {
            String value = UsbSettingsControl
                    .readToFile(UsbSettingsControl.AUTORUN_USBMODE);
            if (value != null) {
                if (value.equals(UsbSettingsControl.USBMODE_MTP)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_MTP_ONLY, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_TETHER)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_TETHER, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_PCSUITE)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_PC_SUITE, true);
                } else if (value.equals(UsbSettingsControl.USBMODE_AUTOCONF)) {
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_AUTO_CONF, true);
                }
            }
        } else if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
            Log.d(TAG,
                    "[AUTORUN] onReceive() : android.intent.action.MEDIA_MOUNTED");

            Intent i = new Intent(UsbSettingsControl.ACTION_MASS_STATE_CHANGE);
            mContext.sendBroadcast(i);
        } else if (action.equals("android.intent.action.MEDIA_REMOVED")
                || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {

            Intent i = new Intent(UsbSettingsControl.ACTION_MASS_STATE_CHANGE);
            if (UsbSettingsControl.getUsbConnected(context)
                    && mDefaultFunction
                            .equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
                if (UsbSettingsControl.isMassSeperatedModel()) {
                    if (Settings.System.getInt(mContext.getContentResolver(),
                            "ums_selected_storage", 0) == 1) {
                        mUsbManager.setCurrentFunction(
                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY,
                                true);
                        i.putExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE,
                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY);
                    }
                } else {
                    if (UsbSettingsControl.mMountInternalMemory == false) {
                        mUsbManager.setCurrentFunction(
                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY,
                                true);
                        i.putExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE,
                                UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY);
                    }
                }
            }
            mContext.sendBroadcast(i);
        } else if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
            if (UsbSettingsControl.getUsbConnected(context)) {
                ArrayList<String> availableList = new ArrayList<String>();
                ArrayList<String> activeList = new ArrayList<String>();
                ArrayList<String> errorList = new ArrayList<String>();
                activeList = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER);
                availableList = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                errorList = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER);

                Log.d(TAG, "[AUTORUN] onReceive() : availableList="
                        + availableList);
                Log.d(TAG, "[AUTORUN] onReceive() : activeList=" + activeList);
                Log.d(TAG, "[AUTORUN] onReceive() : errorList=" + errorList);

                if (UsbSettingsControl.isAutorunTimer()) {
                    return;
                }

                boolean activeTether = false;
                boolean availableTether = false;
                if (availableList != null) {
                    for (String usbTetherState : availableList) {
                        Log.d(TAG,
                                "[AUTORUN] onReceive() : TetherState Changed="
                                        + usbTetherState);
                        if (usbTetherState.equals("usb0")
                                || usbTetherState.equals("ecm0")
                                || isSpainTLFInterface(usbTetherState)) {
                            availableTether = true;
                        }
                    }
                }
                if (activeList != null) {
                    for (String usbTetherState : activeList) {
                        Log.d(TAG,
                                "[AUTORUN] onReceive() : TetherState Changed="
                                        + usbTetherState);
                        if (usbTetherState.equals("usb0")
                                || usbTetherState.equals("ecm0")
                                || isSpainTLFInterface(usbTetherState)) {
                            activeTether = true;
                        }
                    }
                }

                if (!availableTether && activeTether) {
                    UsbSettingsControl.setTetherStatus(mContext, true);
                } else {
                    UsbSettingsControl.setTetherStatus(mContext, false);
                }
            }
        }
    }

    // [Start][20/11/2014][vinodh.kumara@lge.com]Added for USB tethering
    // deactivation when sim subcribtion change for Spain_TLF
    private boolean isSpainTLFInterface(String interfaceName) {
        if ((Config.getOperator().equals("TLF") && Config.getCountry().equals(
                "ES"))
                || (Config.getOperator().equals("TLF") && Config.getCountry()
                        .equals("COM"))) {
            Log.d(TAG, "isSpainTLFInterface : " + interfaceName);
            if ("rndis0".equals(interfaceName)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // [End][20/11/2014][vinodh.kumara@lge.com]Added for USB tethering
    // deactivation when sim subcribtion change for Spain_TLF

    private void changeSettingsLCDCrack() {
        Log.d(TAG, "[AUTORUN] Change Settings LCD Crack in ATT");
        if (Utils.isSupportUSBMultipleConfig(mContext)) {
            mUsbManager.setCurrentFunction(
                    UsbManagerConstants.USB_FUNCTION_AUTO_CONF, true);
            UsbSettingsControl.setAutorunDialogDoNotShow(mContext, true);
        } else {
            mUsbManager.setCurrentFunction(
                    UsbManagerConstants.USB_FUNCTION_PC_SUITE, true);
            Settings.System.putInt(mContext.getContentResolver(),
                    SettingsConstants.System.USB_ASK_ON_CONNECTION, 1);
            UsbSettingsControl.setAutorunDialogDoNotShow(mContext, true);
        }

    }

    private void setUsbConnect() {
        if (!isLaunchEnable()) {
            return;
        }

        Log.d(TAG, "[AUTORUN] setUsbConnect() : mUsbSettingsRun = "
                + UsbSettingsControl.mUsbSettingsRun);
        /*
         * MSE-ADD-S 2012/06/14 - If Data transfer mode is ON, USB autorun is
         * disabled.
         */
        if (!UsbSettingsControl.mActivityUsbModeChange
                && !UsbSettingsControl.mUsbSettingsRun
                && !mUsbManager.isFunctionEnabled("dtf")) {
            startUsbSettings();
            /* MSE-ADD-E 2012/06/14 */
        }
    }

    private void setUsbDisconnect() {
        int deviceProvisioned = android.provider.Settings.Global.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.Global.DEVICE_PROVISIONED, 0);
        Log.d(TAG, "[AUTORUN] setUsbDisconnect() : deviceProvisioned="
                + deviceProvisioned);

        if (deviceProvisioned == 0) {
            return;
        }

        if (SystemProperties.getInt("dev.bootcomplete", 0) == 0) {
            return;
        }

        if (!UsbSettingsControl.isDisconnectBugModel()) {
            if (Config.getOperator().equals(Config.VZW)
                    || UsbSettingsControl.isDirectAutorunModel()) {
                UsbSettingsControl.mDirectAutorun = true;
            } else {
                UsbSettingsControl.mDirectAutorun = false;
            }
            UsbSettingsControl.mActivityUsbModeChange = false;
            UsbSettingsControl.mActivityFinish = false;
        }

        Intent i1 = new Intent(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE);
        mContext.removeStickyBroadcast(i1);
        Intent i2 = new Intent(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
        mContext.removeStickyBroadcast(i2);

        if ((Config.getCountry().equals("US") && Config.getOperator().equals(
                "TMO"))
                || Config.getOperator().equals("DCM")) {
            if (UsbSettingsControl.getTetherStatus(mContext)
                    && !UsbSettingsControl.isDisconnectBugModel()) {
                UsbSettingsControl.connectUsbTether(mContext, false);
            }
        }

        if (mDefaultFunction.equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            UsbSettingsControl.setMassStorage(mContext, false);
        } else if (mDefaultFunction
                .equals(UsbManagerConstants.USB_FUNCTION_MTP_ONLY)) {
            if (!UsbSettingsControl.isDisconnectBugModel()
                    && !Config.getOperator().equals(Config.VZW)
                    && UsbSettingsControl.isDirectAutorunModel()) {
                UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_MTP, false);
            }
        } else if (mDefaultFunction
                .equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            if (UsbSettingsControl.getTetherStatus(mContext)) {
                UsbSettingsControl.connectUsbTether(mContext, false);
            }
            if (!UsbSettingsControl.isDisconnectBugModel()
                    && !Config.getOperator().equals(Config.VZW)
                    && UsbSettingsControl.isDirectAutorunModel()) {
                UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_TETHER, false);
            }
        } else if (mDefaultFunction
                .equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
            if (!UsbSettingsControl.isDisconnectBugModel()
                    && !Config.getOperator().equals(Config.VZW)
                    && UsbSettingsControl.isDirectAutorunModel()) {
                UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_PCSUITE, false);
            }
        }
    }

    private void startUsbSettings() {
        int deviceProvisioned = android.provider.Settings.Global.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.Global.DEVICE_PROVISIONED, 0);
        Log.d(TAG, "[AUTORUN] startUsbSettings() : deviceProvisioned="
                + deviceProvisioned);

        if (deviceProvisioned == 0) {
            return;
        }

        if (SystemProperties.getInt("dev.bootcomplete", 0) == 0) {
            return;
        }

        if (Utils.isUI_4_1_model(mContext)) {
            return;
        }

        if (UsbSettingsControl.getAskOnConnection(mContext)
                && !"kids".equals(SystemProperties
                        .get("service.plushome.currenthome"))) {
            Intent intent = new Intent("android.intent.action.MAIN");
            ComponentName cmp = new ComponentName("com.android.settings",
                    "com.android.settings.UsbSettings");
            intent.setComponent(cmp);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);

            intent.putExtra(UsbSettingsControl.EXTRA_USB_LAUNCHER, true);
            mContext.startActivity(intent);
            // UsbSettingsControl.mUsbSettingsRun = true;
        } else {
            if (mDefaultFunction.equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
                if (UsbSettingsControl.isMassSeperatedModel()) {
                    startUmsSelection();
                } else {
                    UsbSettingsControl.setMassStorage(mContext, true);
                }
            } else if (!mIntent.getBooleanExtra(
                    UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false)
                    && mDefaultFunction
                            .equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                boolean mCheckCTHidden = SystemProperties.getBoolean(
                        "persist.service.usb_ther_always", false);
                if (("CTC".equals(Config.getOperator())|| "CTO".equals(Config.getOperator()))
                        && mCheckCTHidden) {
                    if (!UsbSettingsControl.getUsbConnected(mContext)) {
                        return;
                    }
                    if (UsbSettingsControl.connectUsbTether(mContext, true) == -1) {
                        return;
                    }
                    mUsbManager.setCurrentFunction(
                            UsbManagerConstants.USB_FUNCTION_TETHER, true);
                    UsbSettingsControl.connectUsbTether(mContext, true);
                } else {
                    UsbSettingsControl.callTetherPopup(mContext);
                }
            }
        }
    }

    private String getCurrentFunction() {
        String functions = SystemProperties.get("sys.usb.config", "");
        int commaIndex = functions.indexOf(',');
        if (commaIndex > 0) {
            return functions.substring(0, commaIndex);
        } else {
            return functions;
        }
    }

    private boolean isRejectPopup(String packageName, String className) {
        boolean isPackageName = false;
        boolean isClassName = false;
        int i;
        if (mRejectPackageList != null) {
            for (i = 0; i < mRejectPackageList.length; i++) {
                if (mRejectPackageList[i].equals(packageName)) {
                    isPackageName = true;
                    break;
                }
            }
        }
        if (mRejectClassList != null) {
            for (i = 0; i < mRejectClassList.length; i++) {
                if (mRejectClassList[i].equals(className)) {
                    isClassName = true;
                    break;
                }
            }
        }

        if (isPackageName || isClassName) {
            return true;
        } else {
            return false;
        }
    }

    private void startUmsSelection() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.putExtra("DirectUMS", true);
        send.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        send.setClassName("com.android.settings",
                "com.android.settings.lge.UmsSelection");
        mContext.startActivity(send);
    }

    private boolean isLaunchEnable() {
        boolean launch = true;

        // final String IS_FACTORY_PATH =
        // "/sys/module/lge_emmc_direct_access/parameters/is_factory";
        // /* LGE_CHANGE_S [START] 2012.6.2 jaeho.cho@lge.com adjust inode file
        // for u0 cdma project */
        // final String U0_CDMA_FACTORY_PATH =
        // "/sys/class/android_usb/android0/idProduct";
        // /* LGE_CHANGE_S [END] 2012.6.2 jaeho.cho@lge.com adjust inode file
        // for u0 cdma project */
        // final String U0_FACTORY_PATH =
        // "/sys/class/android_usb/android0/factory_cable";
        final String G1_FACTORY_PROPERTY = "ro.factorytest";
        final String G1_BOOTMODE_PROPERTY = "ro.bootmode";
        final String AAT_PROPERTY = "sys.allautotest.run";

        final String FACTORY_PATH = "/sys/class/android_usb/android0/idProduct";
        final String FACTORY_PATH2 = "/sys/class/android_usb/android0/factory_cable";
        final String FACTORY_PATH3 = "/sys/module/lge_emmc_direct_access/parameters/is_factory";

        final String ota_property = "gsm.lge.ota_ignoreKey";

        if (UsbSettingsControl.isDisconnectBugModel()) {
            if (UsbSettingsControl.mActivityUsbModeChange == true) {
                UsbSettingsControl.mActivityUsbModeChange = false;
                launch = false;
            }
        }

        String factoryTestStr = SystemProperties.get(G1_FACTORY_PROPERTY);
        String bootmodeTestStr = SystemProperties.get(G1_BOOTMODE_PROPERTY);
        if ((factoryTestStr != null && "2".equals(factoryTestStr))
                || (bootmodeTestStr != null && "pifboot"
                        .equals(bootmodeTestStr))) {
            launch = false;
        }

        String aatTestStr = SystemProperties.get(AAT_PROPERTY);
        if (aatTestStr != null && "true".equals(aatTestStr)) {
            launch = false;
        }

        String otaRunning = SystemProperties.get(ota_property);
        if (otaRunning != null && "true".equals(otaRunning)) {
            launch = false;
        }

        BufferedReader in = null;
        int is_factory = 0;

        if (new File(FACTORY_PATH).exists()) {
            try {
                in = new BufferedReader(new FileReader(FACTORY_PATH));
                String _is_factory_pid = (in != null) ? in.readLine() : null;
                if (in != null) {
                    in.close();
                }

                if (_is_factory_pid != null && _is_factory_pid.equals("6000")) {
                    Log.d(TAG, "is_factory_pid : 6000");
                    is_factory = 1;
                }

            } catch (IOException e) {
                Log.d(TAG, "unable to FACTORY_PATH : " + e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Exception : " + e);
                }
            }
        }
        if (new File(FACTORY_PATH2).exists()) {
            try {
                in = new BufferedReader(new FileReader(FACTORY_PATH2));
                String _is_factory_pid = (in != null) ? in.readLine() : null;
                if (in != null) {
                    in.close();
                }

                if (_is_factory_pid != null && _is_factory_pid.equals("6000")) {
                    Log.d(TAG, "is_factory_pid : 6000");
                    is_factory = 1;
                }

            } catch (IOException e) {
                Log.d(TAG, "unable to FACTORY_PATH : " + e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Exception : " + e);
                }
            }
        }
        if (new File(FACTORY_PATH3).exists()) {
            try {
                in = new BufferedReader(new FileReader(FACTORY_PATH3));
                String _is_factory = (in != null) ? in.readLine() : null;
                if (in != null) {
                    in.close();
                }

                if (_is_factory != null && _is_factory.equals("yes")) {
                    Log.d(TAG, "is_factory : yes");
                    is_factory = 1;
                }

            } catch (IOException e) {
                Log.d(TAG, "unable to FACTORY_PATH : " + e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Exception : " + e);
                }
            }
        }

        if (is_factory == 1) {
            UsbSettingsControl.cancelAutorunTimer();
            launch = false;
        }

        if (getCurrentFunction().equals(UsbManager.USB_FUNCTION_ACCESSORY)) {
            launch = false;
        }
        /*
         * LGE_CHANGE_S [START] 2012.12.2 jahoon.ku@lge.com blocking audio
         * accessory
         */
        if (getCurrentFunction().equals(UsbManager.USB_FUNCTION_AUDIO_SOURCE)) {
            launch = false;
        }
        /*
         * LGE_CHANGE_E [END] 2012.12.2 jahoon.ku@lge.com blocking audio
         * accessory
         */

        /*
         * LGE_CHANGE_S [START] 2013.4.29 enigmah2k.kim@lge.com blocking ncm
         * mirrolink accessory
         */
        if (getCurrentFunction().equals("ncm,adb")
                || getCurrentFunction().equals("ncm")) {
            launch = false;
        }
        /*
         * LGE_CHANGE_E [END] 2013.4.29 enigmah2k.kim@lge.com blocking ncm
         * mirrolink accessory
         */

        ActivityManager am = (ActivityManager)mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> Info = am.getRunningTasks(1);
        if (Info != null && Info.size() > 0) {
            ComponentName topActivity = Info.get(0).topActivity;
            String topPackageName = topActivity.getPackageName();
            String topClassName = topActivity.getClassName();
            Log.d(TAG, "[AUTORUN] : topPackageName=" + topPackageName);
            Log.d(TAG, "[AUTORUN] : topClassName=" + topClassName);

            if (isRejectPopup(topPackageName, topClassName)) {
                launch = false;
            }
        } else {
            Log.d(TAG,
                    "[AUTORUN] : do not check a top activity because do not exist activity list");
        }

        if ("VZW".equals(Config.getOperator())) {
            PackageManager pm = mContext.getPackageManager();
            String setupWizard = "com.android.LGSetupWizard";
            try {
                if (pm.getApplicationEnabledSetting(setupWizard) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    launch = false;
                    Log.d(TAG, "[AUTORUN] VZW LGSetupWizard running");
                }
            } catch (Exception e) {
                Log.d(TAG, "[AUTORUN] Not exist com.android.LGSetupWizard");
            }
        } else if ("SPR".equals(Config.getOperator())) {
            PackageManager pm = mContext.getPackageManager();
            String setupWizard = "com.android.LGSetupWizard.flow.spr.SetupAccessibilitySprint";
            try {
                if (pm.getApplicationEnabledSetting(setupWizard) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    launch = false;
                    Log.d(TAG, "[AUTORUN] SPR LGSetupWizard running");
                }
            } catch (Exception e) {
                Log.d(TAG, "[AUTORUN] Not exist com.android.LGSetupWizard");
            }
        } else {
            PackageManager pm = mContext.getPackageManager();
            String setupWizard = "com.android.LGSetupWizard.SetupFlowController";
            try {
                if (pm.getApplicationEnabledSetting(setupWizard) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    launch = false;
                    Log.d(TAG, "[AUTORUN] LGSetupWizard running");
                }
            } catch (Exception e) {
                Log.d(TAG, "[AUTORUN] Not exist com.android.LGSetupWizard");
            }
        }

        // if (Config.getOperator().equals(Config.VZW)) {
        // if (mIntent.getBooleanExtra(UsbManager.USB_FUNCTION_CDROM_STORAGE,
        // false))
        // launch = false;
        // }

        if (Config.getOperator().equals(Config.DCM)
                && mUsbManager.isFunctionEnabled("dtf") == true) {
            launch = false;
        }

        if (mUsbManager.getDefaultFunction().equals("qmicm")) {
            launch = false;
        }

        String checkQmicm = SystemProperties.get("sys.usb.config", "");
        if (checkQmicm.contains("qmicm")) {
            launch = false;
        }

        if (Config.getOperator().equals(Config.SPRINT)
                || Config.getOperator().equals(Config.BM)
                || (Config.getCountry().equals("US") && Config.getOperator()
                        .equals("TMO")) || Config.getOperator().equals("DCM")) {
            if (UsbSettingsControl.mTetherChanged) {
                launch = false;
                // UsbSettingsControl.mTetherChanged = false;
            }
        }

        // sprint userdebug check
        if (Config.getOperator().equals(Config.SPRINT)) {
            String userdebugmode = SystemProperties.get(
                    "persist.service.usb_ther", "");
            if (userdebugmode.contains("true")) {
                String usbtetheron = SystemProperties.get(
                        "persist.sys.usb.state", "");
                if (usbtetheron.contains("on")) {
                    launch = false;
                }
            }
        }

        return launch;
    }
}
