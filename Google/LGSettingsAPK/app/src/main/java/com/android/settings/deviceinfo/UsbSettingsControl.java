package com.android.settings.deviceinfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import com.lge.constants.SettingsConstants;
import android.os.UserHandle;

import com.lge.constants.UsbManagerConstants;

public class UsbSettingsControl {

    private static final String TAG = "UsbSettingsControl";
    private static final String SHARED_TETHER_STATUS = "shared_tether_status";
    private static final String SHARED_USB_CONNECTED = "shared_usb_connected";
    private static final String SHARED_DIALOG_DO_NOT_SHOW = "shared_dialog_do_not_show";

    public final static String ACTION_AUTORUN_TIMEOUT = "com.lge.intent.action.autorun_timeout";
    public final static String ACTION_AUTORUN_FINISH = "com.lge.intent.action.autorun_finish";
    public final static String ACTION_AUTORUN_ACK = "com.lge.android.intent.action.AUTORUN_ACK";
    public final static String ACTION_AUTORUN_CHANGE_MODE = "com.lge.android.intent.action.AUTORUN_CHANGE_MODE";
    public final static String ACTION_MASS_STATE_CHANGE = "com.lge.intent.action.mass_state_change";
    public final static String ACTION_TETHER_STATE_CHANGE = "com.lge.intent.action.tether_state_change";
    public final static String ACTION_ACTIVITY_FINISH = "com.lge.intent.action.activity_finish";

    public static final String EXTRA_USB_LAUNCHER = "extra_usb_launcher";
    public static final String EXTRA_DIRECT_AUTORUN = "extra_direct_autorun";
    public static final String EXTRA_USB_DEFAULT_MODE = "extra_usb_default_mode";
    public static final String EXTRA_TETHER_POPUP_FORCE = "extra_tether_popup_force";

    public static final String AUTORUN_ISO_PATH = "/system/usbautorun.iso";
    public static final String AUTORUN_USBMODE =
            "/sys/class/android_usb/android0/f_cdrom_storage/lun/cdrom_usbmode";
    public static final String USBMODE_PCSUITE = "0";
    public static final String USBMODE_MTP = "1";
    public static final String USBMODE_UMS = "2";
    public static final String USBMODE_ASK = "3";
    public static final String USBMODE_CHARGE = "4";
    public static final String USBMODE_TETHER = "5";
    public static final String USBMODE_PTP = "6";
    public static final String USBMODE_AUTOCONF = "7";

    public static final String TETHER_URL = "file:///android_asset/html/%y%z/tether_attention_%x.html";
    public static final String TETHER_PATH = "html/%y%z/tether_attention_%x.html";

    public static final int DIALOG_TETHERING_ALERT = 1;
    public static final int DIALOG_TETHERING_DISCONNECTION = 2;
    public static final int DIALOG_PROGRESS = 3;
    public static final int DIALOG_INTERNET_CONNECTION = 4;
    public static final int DIALOG_DCM_TETHER_ALERT = 5;
    public static final int DIALOG_FIRST_AUTORUN = 6;
    public static final int DIALOG_INSTALL_AUTORUN = 7;

    private static final int ATT_DEFAULT = 0;
    private static final int ATT_TABLET_DEFAULT = 1;

    private static Timer mAutorunTimer = null;
    private static boolean mAutorunChanging = false;
    private final static int AUTORUN_DELAY_TIME = 30000;
    private static Context mTimerContext = null;

    public static boolean mUsbConnected = false;
    public static boolean mDirectAutorun = true;
    public static boolean mUsbSettingsRun = false;
    public static boolean mActivityUsbModeChange = false;
    public static boolean mActivityFinish = false;

    public static boolean mOldversion = false;
    public static String mActiveCurrentFunction = "";

    public static boolean mMountSDCard = false;
    public static String mStateSDCard = "";
    public static boolean mMountInternalMemory = false;
    public static String mStateInternalMemory = "";
    public static int mStorageVolumeNum = 0;

    public static boolean mTetherChanged = false;

    public static void setTetherStatus(Context context, boolean status) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean(SHARED_TETHER_STATUS, status).commit();

        Log.d(TAG, "[AUTORUN] setTetherStatus() : status=" + status);
    }

    public static boolean getTetherStatus(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        boolean ret = preferences.getBoolean(SHARED_TETHER_STATUS, false);
        Log.d(TAG, "[AUTORUN] getTetherStatus() : status=" + ret);
        return ret;
    }

    public static boolean writeToFile(String path, String value) {
        boolean writeSuccess = false;

        File file = new File(path);
        BufferedWriter out = null;

        if (file.exists() && file.canWrite()) {
            try {
                out = new BufferedWriter(new FileWriter(file), 128);
                out.write(value);
                writeSuccess = true;
            } catch (IOException e) {
                writeSuccess = false;
            } finally {
                Log.d(TAG, "[AUTORUN] writeToFile() : Success write path = " + path + ", value = "
                        + value);
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    writeSuccess = false;
                }
            }
        } else {
            Log.w(TAG, "[AUTORUN] writeToFile() : Fail write path = " + path + ", value = " + value);
        }

        return writeSuccess;
    }

    public static String readToFile(String path) {
        String value = "";

        File file = new File(path);
        BufferedReader in = null;

        if (file.exists() && file.canRead()) {
            try {
                in = new BufferedReader(new FileReader(file), 128);
                value = in.readLine();
            } catch (IOException e) {
            	Log.w(TAG, "IOException : " + e);
            } finally {
                Log.d(TAG, "[AUTORUN] readToFile() : Success read path = " + path + ", value = "
                        + value);
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                	Log.w(TAG, "IOException : " + e);
                }
            }
        } else {
            Log.w(TAG, "[AUTORUN] readToFile() : Fail read path = " + path + ", value =" + value);
        }

        return value;
    }

    public static class autorunTimerTask extends TimerTask {
        public void run() {

            if (mTimerContext != null) {
                Intent intent = new Intent(ACTION_AUTORUN_TIMEOUT);
                mTimerContext.sendBroadcast(intent);
            }

            cancelAutorunTimer();
        }
    }

    public static void cancelAutorunTimer() {
        if (Config.getOperator().equals(Config.VZW)) {
            return;
        }
        if (mAutorunTimer != null) {
            mAutorunTimer.cancel();
            mAutorunTimer = null;

            Log.d(TAG, "[AUTORUN] cancelAutorunTimer()");
        }

        mAutorunChanging = false;
        mTimerContext = null;
    }

    public static void startAutorunTimer(Context context) {
        if (Config.getOperator().equals(Config.VZW)) {
            return;
        }
        if (mAutorunTimer != null) {
            mAutorunTimer.cancel();
            mAutorunTimer = null;
        }

        mTimerContext = context;

        mAutorunTimer = new Timer();
        mAutorunTimer.schedule(new autorunTimerTask(), AUTORUN_DELAY_TIME);
        mAutorunChanging = true;
        Log.d(TAG, "[AUTORUN] startAutorunTimer() : mAutorunChanging=" + mAutorunChanging);
    }

    public static boolean isAutorunTimer() {
        Log.d(TAG, "[AUTORUN] isAutorunTimer() : mAutorunChanging=" + mAutorunChanging);
        return mAutorunChanging;
    }

    public static void checkStorageVolume(Context context) {
        StorageManager storageManager = (StorageManager)context.getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return;
        }
        String state = "";
        StorageVolume[] storageVolumes = storageManager.getVolumeList();
        if (storageVolumes == null) {
            return;
        }
        int length = storageVolumes.length;
        mStorageVolumeNum = length;
        for (int i = 0; i < length; i++) {
            StorageVolume storageVolume = storageVolumes[i];

            // Fusion2 JB - support Internal_SD
            if (Build.BOARD.equals("batman_lgu_kr") || Build.BOARD.equals("batman_skt_kr")) {
                state = storageManager.getVolumeState(storageVolume.getPath());
                mStateSDCard = state;

                mMountSDCard = false;
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    mMountSDCard = true;
                }
                break;
            }

            if (storageVolume.isRemovable() == true) {
                state = storageManager.getVolumeState(storageVolume.getPath());
                mStateSDCard = state;

                mMountSDCard = false;
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    mMountSDCard = false;
                }
                else {
                    mMountSDCard = true;
                }
            } else {
                mMountInternalMemory = true;
                mStateInternalMemory = storageManager.getVolumeState(storageVolume.getPath());
                /*
                state = storageManager.getVolumeState(storageVolume.getPath());

                mMountInternalMemory = false;
                if (!Environment.MEDIA_MOUNTED.equals(state))
                    mMountInternalMemory = false;
                else
                    mMountInternalMemory = true;
                */
            }
        }

        Log.d(TAG, "[AUTORUN] checkStorageVolume() : mMountSDCard=" + mMountSDCard);
        Log.d(TAG, "[AUTORUN] checkStorageVolume() : mStateSDCard=" + mStateSDCard);
        Log.d(TAG, "[AUTORUN] checkStorageVolume() : mMountInternalMemory=" + mMountInternalMemory);
        Log.d(TAG, "[AUTORUN] checkStorageVolume() : mStateInternalMemory=" + mStateInternalMemory);
    }

    public static boolean isMassStorageEnable() {
        boolean isMassStorageEnable = false;
        if (mStorageVolumeNum <= 0) {
            return isMassStorageEnable;
        }
        if (mStorageVolumeNum == 1) {
            if (Environment.MEDIA_MOUNTED.equals(mStateSDCard)
                    || Environment.MEDIA_SHARED.equals(mStateSDCard)
                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mStateSDCard)) {
                isMassStorageEnable = true;
            }
            else {
                isMassStorageEnable = false;
            }
        } else if (mStorageVolumeNum == 2) {
            if (UsbSettingsControl.isMassSeperatedModel()) {
                isMassStorageEnable = true;
            }
            else {
                if (Environment.MEDIA_MOUNTED.equals(mStateSDCard)
                        || Environment.MEDIA_SHARED.equals(mStateSDCard)
                        || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mStateSDCard)) {
                    isMassStorageEnable = true;
                }
                else if (Environment.MEDIA_MOUNTED.equals(mStateInternalMemory)
                        || Environment.MEDIA_SHARED.equals(mStateInternalMemory)
                        || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mStateInternalMemory)) {
                    isMassStorageEnable = true;
                }
                else {
                    isMassStorageEnable = false;
                }
            }
        } else {
            isMassStorageEnable = false;
        }

        Log.d(TAG, "[AUTORUN] isMassStorageEnable() : isMassStorageEnable=" + isMassStorageEnable);
        return isMassStorageEnable;
    }

    public static boolean setMassStorage(Context context, boolean set) {
        StorageManager storageManager = (StorageManager)context.getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return false;
        }
        if (set) {
            if (!storageManager.isUsbMassStorageEnabled()) {
                Log.d(TAG, "[AUTORUN] enableUsbMassStorage");
                storageManager.enableUsbMassStorage();
            }
        } else {
            if (storageManager.isUsbMassStorageEnabled()) {
                Log.d(TAG, "[AUTORUN] disableUsbMassStorage");
                storageManager.disableUsbMassStorage();
            }
        }

        return true;
    }

    public static boolean changeAutorunMode(Context context, String autorunMode, boolean timer) {
        if (!supportAutorunMode(context)) {
            return false;
        }

        UsbManager usbManager = (UsbManager)context.getSystemService(context.USB_SERVICE);
        if (usbManager == null) {
            return false;
        }
        if (UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE, autorunMode) == false) {
            return false;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Log.d(TAG, "[AUTORUN] waiting exception");
        }
        usbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false);
        if (timer) {
            UsbSettingsControl.startAutorunTimer(context);
        }
        return true;
    }

    public static boolean supportAutorunMode(Context context) {
        if (context == null) {
            return false;
        }

        if ("TRF".equals(Config.getOperator())) {
            return false;
        }

        String use = SystemProperties.get("use_open_autorun");
        if (use.equals("false")) {
            return false;
        }
        boolean autorunEnable = Settings.System.getInt(context.getContentResolver(),
                SettingsConstants.System.AUTORUN_SWITCH, 1) == 1 ? true : false;
        if (autorunEnable == false) {
            return false;
        }
        if (new File(UsbSettingsControl.AUTORUN_ISO_PATH).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getAskOnConnection(Context context) {
        int usbAskOnConnection = Settings.System.getInt(context.getContentResolver(),
                SettingsConstants.System.USB_ASK_ON_CONNECTION, 0);

        return (usbAskOnConnection == 1 ? true : false);
    }

    public static boolean isPCsoftwareTRFModel() {
        boolean pcsoftwareuse = false;
        String productname = SystemProperties.get("ro.product.name");
        String mProductOperator = Config.getOperator2();
        if (productname.equals("fx3_wcdma_trf_us")
                || productname.equals("fx3_cdma_trf_us")
                || productname.equals("w3c_trf_us")
                || productname.equals("x3_trf_us")
                || productname.equals("f70_trf_us")
                || mProductOperator.equals(Config.TRF)) {
            pcsoftwareuse = true;
        }

        return pcsoftwareuse;
    }

    public static boolean isDisconnectBugModel() {
        boolean disconnectBug = false;
        return disconnectBug;
    }

    public static boolean isMassSeperatedModel() {
        boolean massSeperated = false;
        return massSeperated;
    }

    public static boolean isDirectAutorunModel() {
        boolean directAutorun = false;

        if ("TRF".equals(Config.getOperator())) {
            directAutorun = true;
        }

        return directAutorun;
    }

    public static int connectUsbTether(Context context, boolean connection) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return 0;
        }
        if (connection == true) {
            UsbSettingsControl.setTetherStatus(context, false);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            	Log.w(TAG, "InterruptedException : " + e);
            }
            if (cm.setUsbTethering(true) != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                Log.d(TAG, "[AUTORUN] ============ Tethering ERROR !! ============");
                return -1;
            } else {
                Log.d(TAG, "[AUTORUN] ============ Tethering OK !!    ==============");
                return 1;
            }
        } else {
            UsbSettingsControl.setTetherStatus(context, false);
            cm.setUsbTethering(false);
            return 0;
        }
    }

    public static void setUsbConnected(Context context, boolean connected) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean(SHARED_USB_CONNECTED, connected).commit();

        Log.d(TAG, "[AUTORUN] setUsbConnected() : connected=" + connected);
    }

    public static boolean getUsbConnected(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        boolean ret = preferences.getBoolean(SHARED_USB_CONNECTED, false);
        Log.d(TAG, "[AUTORUN] getUsbConnected() : connected=" + ret);
        return ret;
    }

    public static void callTetherPopup(Context context) {
        if (Config.getOperator().equals(Config.VZW)) {
            return;
        }
        if (context == null) {
            return;
        }
        Intent i = new Intent();
        i.setComponent(new ComponentName(
                "com.android.settings",
                "com.android.settings.deviceinfo.UsbSettingsPopup"));
        i.setAction(Intent.ACTION_PICK_ACTIVITY);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public static boolean isMtpSupport(Context context) {
        StorageManager storageManager = (StorageManager)context.getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return false;
        }

        boolean isMtp = false;
        StorageVolume[] storageVolumes = storageManager.getVolumeList();
        if (storageVolumes == null) {
            return false;
        }

        int length = storageVolumes.length;
        mStorageVolumeNum = length;

        for (int i = 0; i < length; i++) {
            StorageVolume storageVolume = storageVolumes[i];
            if (storageVolume.getMtpReserveSpace() > 0) {
                isMtp = true;
                break;
            }
        }

        return isMtp;
    }

    public static boolean isMassStorageSupport(Context context) {
        StorageManager storageManager = (StorageManager)context.getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return false;
        }

        boolean isMassStorage = false;
        StorageVolume[] storageVolumes = storageManager.getVolumeList();
        if (storageVolumes == null) {
            return false;
        }

        int length = storageVolumes.length;
        mStorageVolumeNum = length;

        for (int i = 0; i < length; i++) {
            StorageVolume storageVolume = storageVolumes[i];
            if (storageVolume.allowMassStorage() == true) {
                isMassStorage = true;
                break;
            }
        }

        return isMassStorage;
    }

    public static void setAutorunDialogDoNotShow(Context context, boolean status) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean(SHARED_DIALOG_DO_NOT_SHOW, status).commit();

        Log.d(TAG, "[AUTORUN] setAutorunDialogDoNotShow() : status=" + status);
    }

    public static boolean getAutorunDialogDoNotShow(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences("UsbSettingsControl",
                Context.MODE_PRIVATE);
        boolean ret = preferences.getBoolean(SHARED_DIALOG_DO_NOT_SHOW, false);
        Log.d(TAG, "[AUTORUN] setAutorunDialogDoNotShow() : status=" + ret);
        return ret;
    }

    public static boolean isMultiUser() {
        Log.d(TAG, "[AUTORUN] isMultiUser() : " + UserHandle.myUserId());
        return (UserHandle.myUserId() != 0);
    }

    public static boolean isLCDCrackATT() {
        if ("ATT".equals(Config.getOperator())
                && "1".equals(SystemProperties.get("sys.lge.touchcrack_mode"))) {
            return true;
        }
        return false;
    }

    public static boolean isNotSupplyUSBTethering(Context context) {
        if ("AIO".equals(Config.getOperator2())
                || "TRF".equals(Config.getOperator2())
                || Utils.isWifiOnly(context)
                || ("ATT".equals(Config.getOperator())
                    && (SystemProperties.getInt("wlan.lge.atthotspot", ATT_DEFAULT) == ATT_TABLET_DEFAULT))
                || ("SBM".equals(Config.getOperator())
                    && "user".equals(Build.TYPE))) {
            return true;
        }
        return false;
    }
}
