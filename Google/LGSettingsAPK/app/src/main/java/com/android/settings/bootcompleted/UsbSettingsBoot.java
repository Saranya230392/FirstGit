package com.android.settings.bootcompleted;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.deviceinfo.UsbSettingsReceiver;
import com.android.settings.lgesetting.Config.Config;

public class UsbSettingsBoot {
    private static final String TAG = "SettingBootReceiver";

    public static void onReceive(Context context) {
        if (UserHandle.getUserId(context.getApplicationInfo().uid) != ActivityManager
                .getCurrentUser()) {
            return;
        }

        if (Utils.isMonkeyRunning()) {
            return;
        }

        UsbSettingsControl.setTetherStatus(context, false);
        UsbSettingsControl.mTetherChanged = false;
        if (Config.getOperator().equals(Config.VZW)
                || UsbSettingsControl.isDirectAutorunModel()) {
            UsbSettingsControl.mDirectAutorun = true;
        } else {
            UsbSettingsControl.mDirectAutorun = false;
        }

        Log.d(TAG, "[AUTORUN] onReceive() : android.intent.action.BOOT_COMPLETED");

        Log.d(TAG, "[AUTORUN] sys.usb.config = " + SystemProperties.get("sys.usb.config", ""));
        Log.d(TAG, "[AUTORUN] sys.usb.state = " + SystemProperties.get("sys.usb.state", ""));
        Log.d(TAG,
                "[AUTORUN] persist.sys.usb.config = "
                        + SystemProperties.get("persist.sys.usb.config", ""));

        UsbSettingsReceiver.sprintUsbTetheringON(context);
        UsbSettingsReceiver.ctcUsbTetheringON(context);
        UsbSettingsReceiver.sprintusbtethering = true;
        UsbSettingsReceiver.sCTCusbtethering = true;
    }
}
