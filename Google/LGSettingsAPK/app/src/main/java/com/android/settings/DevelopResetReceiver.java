package com.android.settings;

import com.android.settings.DevelopmentSettings.SystemPropPoker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.IWindowManager;
import android.app.backup.IBackupManager;
import android.app.admin.DevicePolicyManager;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.PowerManager;
import android.app.ActivityManagerNative;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.StrictMode;
import android.view.HardwareRenderer;
import android.app.ActivityThread;
import android.os.AsyncTask;
import android.view.View;
import android.util.Log;
import android.webkit.WebViewFactory;

public class DevelopResetReceiver extends BroadcastReceiver {

    private static final String TAG = "ResetSettings";
    //Reset complete intent action
    private static final String RESET_SETTINGS_COMPLETE_INTENT = "lge.settings.intent.action.RESET_SETTING_COMPLETE";
    //Reset complete intent extra name
    private static final String RESET_SETTINGS_COMPLETE_MODULE = "RESET_SETTING_COMPLETE_MODULE";
    //Reset complete intent extra value
    private static final int RESET_BT_COMPLETE = 0;
    private static final int RESET_LOCKSCREEN_COMPLETE = 1;
    private static final int RESET_CALL_COMPLETE = 2;
    private static final int RESET_COMPLETE_MAX = 3;
    private static int sResetCompleteCount = 0;

    private static final String RESET_SETTINGS_INTENT = "lge.settings.intent.action.RESET_SETTING";
    //MDM - Development reset
    private static final String MDM_RESET_SETTINGS_INTENT = "com.lge.mdm.intent.action.ACTION_RESET_DEVELOPER_OPTIONS";
    private IWindowManager mWindowManager;
    //private IBackupManager mBackupManager;
    //private DevicePolicyManager mDpm;

    private static final String HARDWARE_UI_PROPERTY = "persist.sys.ui.hw";
    private static final String HDCP_CHECKING_PROPERTY = "persist.sys.hdcp_checking";
    private static final String MSAA_PROPERTY = "debug.egl.force_msaa";
    private static final String OPENGL_TRACES_PROPERTY = "debug.egl.trace";

    private boolean mDontPokeProperties;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.d("[ResetSetting][DevelopmentSettings]", "start");
        mContext = context;

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        //mBackupManager = IBackupManager.Stub.asInterface(ServiceManager
        //        .getService(Context.BACKUP_SERVICE));
        //mDpm = (DevicePolicyManager)mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);

        String action = intent.getAction();
        Log.d(TAG, "onReceive : action = " + action);
		int nResetCompleteModule = 0;
        if (action.equals(RESET_SETTINGS_INTENT)) {
            Bundle bd = intent.getExtras();
            if (bd != null) {
                nResetCompleteModule = bd.getInt(RESET_SETTINGS_COMPLETE_MODULE, 0);
            }
	        Log.d(TAG, "onReceive : nResetCompleteModule = " + nResetCompleteModule);
		}
        if (action.equals(RESET_SETTINGS_INTENT)
            || action.equals(MDM_RESET_SETTINGS_INTENT)) {

            mDontPokeProperties = true;

            //Switch
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);

            //Warning popup - Don't show this again
            Settings.System.putInt(mContext.getContentResolver(),
                    "DEVELOPER_DO_NOT_SHOW", 0);

            //USB debugging
            if (action.equals(MDM_RESET_SETTINGS_INTENT)) {
                Settings.Global.putInt(mContext.getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0);
            } else {
                Settings.Global.putInt(mContext.getContentResolver(),
                        Settings.Global.ADB_ENABLED, 1);
            }

            //Power menu bug reports
            if (action.equals(MDM_RESET_SETTINGS_INTENT)) {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.BUGREPORT_IN_POWER_MENU, 0);
            } else {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.BUGREPORT_IN_POWER_MENU, 1);
            }

            //HDCP check
            SystemProperties.set(HDCP_CHECKING_PROPERTY, "drm-only");

            //Stay awake
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);

            //Enable Bluetooth HCI snoop log (add KK)
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.BLUETOOTH_HCI_LOG, 0);

            /*          //Protect USB storage (only JB)
                        try {
                            ActivityThread.getPackageManager().setPermissionEnforced(
                                    READ_EXTERNAL_STORAGE, false);
                        } catch (RemoteException e) {
                            throw new RuntimeException("Problem talking with PackageManager", e);
                        }
            */
            //Allow mock locations
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 0);

            //Select debug app
            try {
                ActivityManagerNative.getDefault().setDebugApp(
                        Settings.System.getString(mContext.getContentResolver(),
                                Settings.System.DEBUG_APP), false, true);
            } catch (RemoteException ex) {
                Log.w(TAG, "RemoteException");
            }

            try {
                mWindowManager.setStrictModeVisualIndicatorPreference("");
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException");
            }

            //Verify apps over USB
            if (action.equals(MDM_RESET_SETTINGS_INTENT)) {
                Settings.Global.putInt(mContext.getContentResolver(),
                        Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 0);
            } else {
                Settings.Global.putInt(mContext.getContentResolver(),
                        Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 1);
            }

            //Wireless display certification (add KK)
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON, 0);

            //Show touch data
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.POINTER_LOCATION, 0);
            //Show touches
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SHOW_TOUCHES, 0);

            //Show screen updates
            writeShowUpdatesOption();

            //Force RTL layout direction (add KK)
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.DEVELOPMENT_FORCE_RTL, 0);
            SystemProperties.set(Settings.Global.DEVELOPMENT_FORCE_RTL, "0");
            LocalePicker.updateLocale(mContext.getResources().getConfiguration().locale);

            //Disable HW overlays
            writeDisableOverlaysOption();

            //Show CPU usage
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.SHOW_PROCESSES, 0);
            Intent service = (new Intent()).setClassName(
                    "com.android.systemui",
                    "com.android.systemui.LoadAverageService");
            mContext.stopService(service);

            //Don't keep activities
            try {
                ActivityManagerNative.getDefault().setAlwaysFinish(false);

            } catch (RemoteException ex) {
                Log.w(TAG, "RemoteException");
            }

            //Show all ANRs
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.ANR_SHOW_BACKGROUND, 0);

            //Force GPU rendering
            SystemProperties.set(HARDWARE_UI_PROPERTY, "false");

            //Enable strict mode
            SystemProperties.set(StrictMode.VISUAL_PROPERTY, "false");

            /*          //Profile GPU rendering (only JB)
                        SystemProperties.set(HardwareRenderer.PROFILE_PROPERTY, "false");
            */
            //Profile GPU rendering (add KK)
            SystemProperties.set(HardwareRenderer.PROFILE_PROPERTY, "");

            //Show GPU view updates
            SystemProperties.set(HardwareRenderer.DEBUG_DIRTY_REGIONS_PROPERTY,
                    "false");

            //Show layout bounds
            SystemProperties.set(View.DEBUG_LAYOUT_PROPERTY, "false");

            //Show hardware layers updates
            SystemProperties.set(HardwareRenderer.DEBUG_SHOW_LAYERS_UPDATES_PROPERTY, "false");

            //Debug GPU overdraw (add KK)
            SystemProperties.set(HardwareRenderer.DEBUG_OVERDRAW_PROPERTY, "");

            //Debug non-rectangular clip operations (add KK)
            SystemProperties.set(HardwareRenderer.DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY, "");

            /*          //Show GPU overdraw (only JB)
                        SystemProperties.set(HardwareRenderer.DEBUG_SHOW_OVERDRAW_PROPERTY, "false");
            */
            //Force 4x MSAA
            SystemProperties.set(MSAA_PROPERTY, "false");

            //Enable OpenGL trances
            SystemProperties.set(OPENGL_TRACES_PROPERTY, "");

            //Simulate secondary displays
            Settings.Global.putString(mContext.getContentResolver(),
                    Settings.Global.OVERLAY_DISPLAY_DEVICES, "");

            //Wait for debugger
            try {
                ActivityManagerNative.getDefault().setDebugApp(null, false, true);
            } catch (RemoteException ex) {
                Log.w(TAG, "RemoteException");
            }

            //Window animation scale / Transition animation scale / Animator duration scale
            try {
                mWindowManager.setAnimationScale(0, 1);
                mWindowManager.setAnimationScale(1, 1);
                mWindowManager.setAnimationScale(2, 1);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException");
            }

            /*          //Enable trances (only JB)
                        try {
                            int limit = -1;
                            ActivityManagerNative.getDefault().setProcessLimit(limit);
                        } catch (RemoteException e) {
                        }
                        SystemProperties.set(Trace.PROPERTY_TRACE_TAG_ENABLEFLAGS,
                                "0x" + Long.toString(0, 16));
            */

            //Background process limit
            try {
                ActivityManagerNative.getDefault().setProcessLimit(-1);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException");
            }

            mDontPokeProperties = false;
            pokeSystemProperties();

        }
        else if (RESET_SETTINGS_COMPLETE_INTENT.equals(action)) {
            if ((RESET_BT_COMPLETE == nResetCompleteModule)
                    || (RESET_LOCKSCREEN_COMPLETE == nResetCompleteModule)
                    || (RESET_CALL_COMPLETE == nResetCompleteModule)) {
                Log.d(TAG, "RESET_SETTINGS_COMPLETE_INTENT : nResetCompleteModule = "
                        + nResetCompleteModule + "sResetCompleteCount = " + sResetCompleteCount);
                sResetCompleteCount++;
                if (RESET_COMPLETE_MAX == sResetCompleteCount) {
                    //reboot code
                    /*
                    PowerManager pm = (PowerManager)
                            context.getSystemService(Context.POWER_SERVICE);
                     */
                    Log.d(TAG, "Reboot!!");
                    sResetCompleteCount = 0;
                    //pm.reboot(null);
                }
            }
        }
    }

    void pokeSystemProperties() {
        if (!mDontPokeProperties) {
            (new SystemPropPoker()).execute();
        }
    }

    static class SystemPropPoker extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String[] services;
            try {
                services = ServiceManager.listServices();
            } catch (RemoteException e) {
                return null;
            } catch (NullPointerException npe) {
                return null;
            }
            for (String service : services) {
                IBinder obj = ServiceManager.checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(IBinder.SYSPROPS_TRANSACTION, data, null, 0);
                    } catch (RemoteException e) {
                        Log.w(TAG, "RemoteException");
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }

    private void writeShowUpdatesOption() {
        try {
            if (Settings.System.getInt(mContext.getContentResolver(), "Screen_Update_Flag", 0) == 1) {
                IBinder flinger = ServiceManager.getService("SurfaceFlinger");
                if (flinger != null) {
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken("android.ui.ISurfaceComposer");
                    final int showUpdates = 0;
                    data.writeInt(showUpdates);
                    flinger.transact(1002, data, null, 0);
                    data.recycle();
                    updateFlingerOptions();
                    Settings.System.putInt(mContext.getContentResolver(), "Screen_Update_Flag", 0);
                }
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void writeDisableOverlaysOption() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                final int disableOverlays = 0;
                data.writeInt(disableOverlays);
                flinger.transact(1008, data, null, 0);
                data.recycle();
                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }

    private void updateFlingerOptions() {
        // magic communication with surface flinger.
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(1010, data, reply, 0);
                @SuppressWarnings("unused")
                int showCpu = reply.readInt();
                @SuppressWarnings("unused")
                int enableGL = reply.readInt();
                int showUpdates = reply.readInt();
                @SuppressWarnings("unused")
                int showBackground = reply.readInt();
                int disableOverlays = reply.readInt();
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "RemoteException");
        }
    }
}
