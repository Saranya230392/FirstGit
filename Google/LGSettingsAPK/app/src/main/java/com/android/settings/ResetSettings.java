package com.android.settings;

import static android.net.NetworkPolicy.LIMIT_DISABLED;
import static android.net.NetworkPolicy.WARNING_DISABLED;
import static android.net.NetworkPolicyManager.POLICY_NONE;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.INotificationManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.app.backup.IBackupManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import java.io.File;
import android.location.LocationManager;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.UserDictionary;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
//ResetSettings - Desktop password
import android.app.Activity;
import android.widget.TextView;
import android.widget.EditText;
import android.app.backup.IBackupManager;

import android.app.AppOpsManager;
/* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
import android.net.wifi.WifiManager;
/* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
import com.android.internal.telephony.RILConstants;
import com.android.internal.view.RotationPolicy;
import com.android.internal.widget.LockPatternUtilsEx;
//LG_BTUI : RESET_SETTING - reset bluetooth setting values [s] , 20130821
import android.bluetooth.BluetoothAdapter;
//LG_BTUI : RESET_SETTING - reset bluetooth setting values [e] , 20130821
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lge.NightModeInfo;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.net.NetworkPolicyEditor;
import com.android.settings.powersave.PowerSave;
import com.lge.constants.SettingsConstants;
import com.lge.constants.SettingsConstants;
import com.android.settings.powersave.PowerSaveSettings;
import com.lge.constants.UsbManagerConstants;
import android.service.dreams.IDreamManager;
import android.service.dreams.DreamService;
// rebestm - NfcReset
import com.lge.nfcaddon.NfcAdapterAddon;
import com.android.settings.nfc.NfcSettingsFragment;
import com.android.internal.telephony.SmsApplication;
import android.os.UserHandle;

import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
/* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [START] */
import android.net.ConnectivityManager;

/* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [END] */

public class ResetSettings extends Fragment {
    private static final String TAG = "ResetSettings";
    private static final String PREF_FILE = "data_usage";
    private static final String PREF_SHOW_WIFI = "show_wifi";
    private static final String PREF_SHOW_ETHERNET = "show_ethernet";
    private static final String PREF_DEFAULT_UNIT = "default_unit";
    private static final String RESET_SETTINGS_INTENT = "lge.settings.intent.action.RESET_SETTING";
    private View mContentView;
    private Button mResetSetting;
    AlertDialog mConfirmDlg = null;
    private boolean mIsShowDlg = false;
    private ContentResolver mContentResolver;
    private final Configuration mCurConfig = new Configuration();
    private static final float NORMAL_FONT_SCALE = 1.0f;

    private static final int SYSTEM_PROPERTY_EMPTY = 0;
    private static final int DB_SET_OFF = 0;
    private static final int DB_SET_ON = 1;

    //private ProgressDialog mProgressDlg = null;
    //private Handler mProgressHandler = null;

    private static final int DATETIME = 1;
    //ResetSettings - Desktop password
    //private IBackupManager mBackupManager;
    static final int REQUEST_CODE = 0x1234;
    private File mPasswordHashFile;
    File mBaseStateDir;

    /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
    private WifiManager mWifiManager;
    /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
    /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [START] */
    private ConnectivityManager mCm;
	private TelephonyManager mTM;

    /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [END] */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.reset_settings, null);

        /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
        mWifiManager = (WifiManager)getActivity().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */

        /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [START] */
        mCm = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [END] */

	mTM = (TelephonyManager)getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);


        if (getActivity() != null) {
            getActivity().setTitle(R.string.reset_settings_title);
        }

        mContentResolver = mContentView.getContext().getContentResolver();

        establishInitialState();
        return mContentView;
    }

    // 20120927 dongseok.lee : reset font index [START]
    public static final int DEFAULT_FONT_INDEX = 0;
    private boolean mFontServerConnected = false;
    private FontServerConnection mRemoteFontServer = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case FontServerConnection.FONT_SERVER_CONNECTED:
                mFontServerConnected = true;
                break;

            default:
                break;
            }
        }
    };

    private void resetFontData() {
        if (mFontServerConnected == false) {
            Log.e(TAG, "Failure : not yet connected");
            return;
        }
        if (mRemoteFontServer == null) {
            Log.e(TAG, "Failure : mRemoteFontServer is null");
            return;
        }

        if (mRemoteFontServer != null) {
            mRemoteFontServer.selectDefaultTypeface(DEFAULT_FONT_INDEX);
        }

    }

    // 20120927 dongseok.lee : reset font index [END]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 20120927 dongseok.lee : reset font index [START]
        // FontServer connected.
        mRemoteFontServer = new FontServerConnection(getActivity(), mHandler);
        mRemoteFontServer.connectFontServerService();
        // 20120927 dongseok.lee : reset font index [END]

        //ResetSettings - Desktop password
        //mBackupManager = IBackupManager.Stub.asInterface(
        //        ServiceManager.getService(Context.BACKUP_SERVICE));
    }
/*
    private Runnable mRunnable = new Runnable() {
        public void run() {
            try {
                if (mProgressDlg != null && mProgressDlg.isShowing()) {
                    mProgressDlg.dismiss();
                    mProgressDlg = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
*/
    private void establishInitialState() {
        mResetSetting = (Button)mContentView.findViewById(R.id.reset_settings);

        mBaseStateDir = new File(Environment.getSecureDataDirectory(), "backup");
        mPasswordHashFile = new File(mBaseStateDir, "pwhash");
        Log.d(TAG, "mPasswordHashFile = " + mPasswordHashFile);

        mResetSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowDlg) {
                    Log.d(TAG, "onClick : mIsShowDlg = " + mIsShowDlg);
                    return;
                }

                mConfirmDlg = new AlertDialog.Builder(mContentView.getContext())
                        .setTitle(R.string.sp_dlg_note_NORMAL)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setMessage(R.string.reset_settings_popup_kk)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        /*                                mProgressHandler = new Handler();

                                                                        if (mProgressDlg == null) {
                                                                            mProgressDlg = new ProgressDialog(mContentView.getContext());
                                                                            mProgressDlg.setMessage(getActivity().getResources().getText(R.string.reset_setting_progress_msg));
                                                                            mProgressDlg.setCancelable(false);
                                                                            mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                                                            mProgressDlg.show();

                                                                            mProgressHandler.postDelayed(mRunnable, 5000);
                                                                        }
                                        */
                                        //ResetSettings - Desktop password
                                        //                                try {
                                        if (mPasswordHashFile.exists()) {
                                            Log.d(TAG, "Desktop password exist");
                                            Intent intent = new Intent(getActivity(),
                                                    ResetSetFullBackupPassword.class);
                                            startActivityForResult(intent, REQUEST_CODE);
                                        } else {
                                            Log.d(TAG, "Desktop password not exist");
                                            getActivity().sendBroadcast(
                                                    new Intent(RESET_SETTINGS_INTENT));
                                        }
                                        //                                } catch (RemoteException e) {
                                        // Not much we can do here
                                        //                                    Log.d(TAG, "Desktop password RemoteException");
                                        //                                }
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .create();

                if (mConfirmDlg != null) {
                    mIsShowDlg = true;
                    mConfirmDlg.show();
                    mConfirmDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface arg0) {
                            mIsShowDlg = false;
                        }
                    });
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(RESET_SETTINGS_INTENT);
        getActivity().registerReceiver(mReceiver, filter);
    }

    //ResetSettings - Desktop password
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "onActivityResult()");

        switch (requestCode) {
        case REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult() : RESULT_OK");
                getActivity().sendBroadcast(new Intent(RESET_SETTINGS_INTENT));
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult() : RESULT_CANCELED");
            }
            break;

        default:
            break;
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);

        // 20120927 dongseok.lee : reset font index [START]
        // FontServer disconnect.
        if (mRemoteFontServer != null) {
            mRemoteFontServer.disconnectFontServerService();
            mRemoteFontServer = null;
        }
        // 20120927 dongseok.lee : reset font index [END]

        super.onDestroy();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RESET_SETTINGS_INTENT.equals(action)) {
                updateSettingsDB();
            }
        }
    };

    private void updateSettingsDB() {
        //        Toast.makeText(getActivity(), "receive RESET_SETTINGS_INTENT", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Default SettingsDB start");
        resetDefaultMessageApp();
        /* MobileData */
        resetMobileDataDB();
        /* Connectivity(=More) */
        /* LGE_CHANGE_S, yeonho.park, 2012-09-20, To set setting's values to default related to WiFi */
        SetWiFiSettingsDBToDefault();
        /* LGE_CHANGE_E */

        /* Mode Change */
        resetModeChangeDB();

        /* Sound */
        resetSoundDB();
        /* Display */
        resetDisplayDB();

        // font
        resetFontData();

        /* Home screen */
        /* Location services */
        locationServicesReset();

        /* Gestures */
        GestureSettingsResetDB();
        /* One hand operation */
        OneHandOperationSettingsResetDB();
        /* Cover operation */
        CoverSettingsResetDB();
        /* Power saver */
        setPowersaverResetDB();

        /* Quad core control */

        /* Backup & reset */
        resetBackupAndResetDB();

        /* Date & time */
        resetDateAndTimeDB();

        /* Language & Input */
        resetLanguageAndInputDB();

        /* Accessibility */
        setAccessibilityResetDB();

        /* PC connection */
        resetPcConnectionDB();

        /* BT */
        resetBtDB();

        /* Developer options */

        /* rebestm - NfcReset */
        resetNFC();
        /* Plug&Pop */
        resetTangibleDB();
        resetApps();
        /* Wireless Storage */
        resetWirelessStorage();
        /* SmartShare Beam */
        resetSmartShareBeam();

        /* ShortcustKeyStatus */
        resetShortcutKeyStatus();
        Log.d(TAG, "Default SettingsDB end");
    }

    /* LGE_CHANGE_S, yeonho.park, 2012-09-20, To set setting's values to default related to WiFi */
    private void SetWiFiSettingsDBToDefault() {
        Log.d("[ResetSetting][Wi-fi]", "start");
        /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        /* LGE_CHANGE_S, junho.lim, 2012-11-09, Whe Wi-Fi is off, Wi-Fi off */

        // Show Wi-Fi pop-up
        Settings.System.putInt(mContentView.getContext().getContentResolver(),
                SettingsConstants.System.ACTION_OFFLOADING_NOTIFY_ME, 1);

        // Wi-Fi Notifications
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                SettingsConstants.System.WIFI_WI_FI_NOTIFICATIONS, 1);

        // Internet unavailable
        Settings.System.putInt(mContentView.getContext().getContentResolver(),
                SettingsConstants.System.WIFI_INTERNET_UNAVAILABLE, 0);

        // Sort list by
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                SettingsConstants.Global.WIFI_LIST_SORTING, 0);

        // WIFI_SLEEP_POLICY
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                Settings.Global.WIFI_SLEEP_POLICY, 2);

        // Scanning always available
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0);

        // Wi-Fi signal weak
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0);

        // Battery saving for Wi-Fi
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1);

        /*        // WIFI_NETWORKS_AVAILABLE_AUTO_CONNECT
                Settings.System.putInt(mContentView.getContext().getContentResolver(),
                                        SettingsConstants.System.WIFI_NETWORKS_AVAILABLE_AUTO_CONNECT, 0);

                // WIFI_LARGE_FILE_TRANSFER_BROWSER
                Settings.System.putInt(mContentView.getContext().getContentResolver(),
                                        SettingsConstants.System.WIFI_LARGE_FILE_TRANSFER_BROWSER, 1);

                // WIFI_AUTO_CONNECT_VZW_AP
                Settings.System.putInt(mContentView.getContext().getContentResolver(),
                                        SettingsConstants.System.WIFI_AUTO_CONNECT_VZW_AP, 1);

                // WIFI_AGGREGATION_AUDIO
                Settings.System.putInt(mContentView.getContext().getContentResolver(),
                                        SettingsConstants.System.WIFI_AGGREGATION_AUDIO, 1);

                // WIFI_AGGREGATION_VIBRATION
                Settings.System.putInt(mContentView.getContext().getContentResolver(),
                                        SettingsConstants.System.WIFI_AGGREGATION_VIBRATION, 1);
        */

        // Miracast switch off
        Settings.Global.putInt(mContentView.getContext().getContentResolver(),
                Settings.Global.WIFI_DISPLAY_ON, 0);

        Log.d("[ResetSetting][Wi-fi]", "end");
    }

    /* LGE_CHANGE_E */

    private void resetDisplayDB() {
        //int brightness = SystemProperties.getInt("ro.lge.lcd_default_brightness",
        //        SYSTEM_PROPERTY_EMPTY);

        Log.d(TAG, "[Display] start");

        // Display > Brightness
        Settings.System.putInt(mContentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                SystemProperties.getInt("ro.lge.lcd_default_brightness", SYSTEM_PROPERTY_EMPTY));

        //try {
        //IPowerManager power = IPowerManager.Stub.asInterface(
        //ServiceManager.getService("power"));
        //power.setBacklightBrightness(brightness);
        //} catch (RemoteException doe) {

        //}
        // Display > Custom Brightness
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.SCREEN_BRIGHTNESS_CUSTOM,
                SystemProperties.getInt("ro.lge.lcd_default_brightness", SYSTEM_PROPERTY_EMPTY));

        // Display > Brightness Auto Mode
        int sp_def_brightness_Mode = SystemProperties.getBoolean("ro.lge.lcd_auto_brightness_mode",
                false) == true ? 1 : 0;
        Settings.System.putInt(mContentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                sp_def_brightness_Mode);
        // Display > NightBrightness
        Context context = getActivity().getApplicationContext();
        NightModeInfo mNightModeInfo = new NightModeInfo(context);
        mNightModeInfo.setNightDB(0);
        mNightModeInfo.cancelAlarmEnd(context);
        mNightModeInfo.setAlarmNextEnd(context);
        mNightModeInfo.endNotification();

        // Display > Brightness Auto Custom Mode
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.SCREEN_BRIGHTNESS_MODE_CUSTOM,
                sp_def_brightness_Mode);

        // Display > Brightness Custom on/off
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.CUSTOM_SCREEN_BRIGHTNESS,
                1);

        // Display > Screen Timeout
        final DevicePolicyManager dpm =
                (DevicePolicyManager)getActivity().getSystemService(
                        Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;

        if (maxTimeout == 0 || maxTimeout >= 30000) {
            Settings.System.putInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000);
        }

        // Display > Screen off effect
        Settings.System.putInt(mContentResolver, SettingsConstants.System.SCREEN_OFF_EFFECT_SET, 2);

        // Display > DayDream
        boolean mDreamsEnabledByDefault = Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_dreamsEnabledByDefault, 
                           "com.android.internal.R.bool.config_dreamsEnabledByDefault");
        boolean mDreamsActivatedOnSleepByDefault = Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_dreamsActivatedOnSleepByDefault, 
                           "com.android.internal.R.bool.config_dreamsActivatedOnSleepByDefault");
        boolean mDreamsActivatedOnDockByDefault = Config.getFWConfigBool(getActivity(), com.android.internal.R.bool.config_dreamsActivatedOnDockByDefault, 
                           "com.android.internal.R.bool.config_dreamsActivatedOnDockByDefault");

        Settings.Secure.putInt(mContentResolver,
                Settings.Secure.SCREENSAVER_ENABLED, mDreamsEnabledByDefault ? 1 : 0);
        Settings.Secure.putInt(mContentResolver,
                Settings.Secure.SCREENSAVER_ACTIVATE_ON_SLEEP,
                mDreamsActivatedOnSleepByDefault ? 1 : 0);
        Settings.Secure.putInt(mContentResolver,
                Settings.Secure.SCREENSAVER_ACTIVATE_ON_DOCK, mDreamsActivatedOnDockByDefault ? 1
                        : 0);

        IDreamManager mDreamManager;
        mDreamManager = IDreamManager.Stub.asInterface(
                ServiceManager.getService(DreamService.DREAM_SERVICE));

        ComponentName mDreamsDefaultComponent = null;
        if (mDreamManager != null) {
            try {
                mDreamsDefaultComponent = mDreamManager.getDefaultDreamComponent();
                ComponentName[] mDreams = { mDreamsDefaultComponent };
                mDreamManager.setDreamComponents(mDreams);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to get active dream", e);
            }
        }

        // Display > Auto Rotate screen
        Settings.System.putInt(mContentResolver, Settings.System.ACCELEROMETER_ROTATION, 1);

        // Display > smart screen
        Settings.System.putInt(mContentResolver, SettingsConstants.System.KEEP_SCREEN_ON, 0);

        // Display > smart screen popup do not show
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.KEEP_SCREEN_ON_DO_NOT_SHOW, 0);

        // Display > smart video
        Settings.System.putInt(mContentResolver, SettingsConstants.System.KEEP_VIDEO_ON, 0);

        // Display > smart video popup do not show
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.KEEP_VIDEO_ON_DO_NOT_SHOW, 0);

        // Display > Frontkeylight Timeout
        Settings.Secure.putInt(mContentResolver, SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT,
                1500);

        // Display > Frontkeylight Prevalue
        Settings.System.putInt(mContentResolver, SettingsConstants.System.FRONT_KEY_LIGHT_PREVALUE,
                1500);

        //Display Notification LED        
        if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") != 0) {
            //Display LED Brightness 
            int sp_led_def_brightness = SystemProperties.getInt(
                    "ro.lge.led_default_brightness", -1);
            if (sp_led_def_brightness == -1) {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.LED_BRIGHTNESS, 255);
            } else {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.LED_BRIGHTNESS, sp_led_def_brightness);
            }

            // Display > Notification on/off
            Settings.System.putInt(mContentResolver,
                    "lge_notification_light_pulse", 1);

            // Display > Notification Incomming call
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_INCOMING_CALL, 1);

            // Display > Notification Missed call
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_MISSED_CALL, 1);

            // Display > Notification Missed Message
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_MSG, 1);

            // Display > Notification Missed LG Emais
            if (!Utils.isNotUseEmailLED(getActivity())) {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_REMINDER_EMAIL, 1);
            }

            // Display > Notification voice mail
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_REMINDER_VOICEMAIL, 1);

            // Display > Notification Alarm
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_ALARM, 1);

            // Display > Notification Calendar
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_CALENDAR, 1);

            // Display > Notification Voice recording
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_VOICE_RECORDER, 1);

            // Display > Notification Battery charging
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_BATTERY_CHARGING, 1);

            // Display > Notification Downloaded apps
            Settings.System.putInt(mContentResolver,
                    Settings.System.NOTIFICATION_LIGHT_PULSE, 1);

            // Display > Notification Missed calls & message
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.EMOTIONAL_LED_MISSED_CALL_MSG, 1);

            if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasBackLed,
                           "com.lge.R.bool.config_hasBackLed")) {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL, 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM, 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL, 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES, 1);

                if (!Utils.isNotUseEmailLED(getActivity())) {
                    Settings.System.putInt(mContentResolver,
                            SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_EMAILS, 1);
                }

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_CALENDAR_NOTI, 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_VOICE_RECORDING, 1);

                Settings.System.putInt(mContentResolver,
                        "emotional_led_back_camera_timer_noti", 1);

                Settings.System.putInt(mContentResolver,
                        "emotional_led_back_camera_face_detecting_noti", 1);

                Settings.System.putInt(mContentResolver,
                        "notification_light_pulse_back", 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL_MSG, 1);

                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_VOICE_MAIL, 1);
            }
        }

        if (Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_powerLight_available,
                           "com.lge.R.bool.config_powerLight_available")) {
            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_ALL, 1);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_MISSED_CALL, 1);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_MESSAGING, 1);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_VOICE_MAIL, 1);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_FELICA, 1);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_ALARM, 0);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_CALENDAR_REMINDER, 0);

            if (Utils.isUI_4_1_model(getActivity())) {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.FRONT_KEY_EMAIL, 1);
            } else {
                Settings.System.putInt(mContentResolver,
                        SettingsConstants.System.FRONT_KEY_EMAIL, 0);
            }

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_GPS, 0);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_SOCIAL_EVENT, 0);

            Settings.System.putInt(mContentResolver,
                    SettingsConstants.System.FRONT_KEY_INDICATOR, 1);
        }

        // Display > Font size
        try {
            Configuration conf = getResources().getConfiguration();
            if (conf != null) {
                Log.d(TAG, String.format("orientation config  old:%d new:%d ",
                        mCurConfig.orientation, conf.orientation));
                mCurConfig.orientation = conf.orientation;
            }
            Settings.Global
                      .putInt(mContentResolver, "sync_large_text", 0);

            mCurConfig.fontScale = NORMAL_FONT_SCALE;
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }

        //Home touch buttons (Front touch buttons), Screen capture area
        IWindowManager mWindowManagerService;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (showNav) {
                //Navigation bar 
                String mDefaultNavi = "Back|Home|Menu|";
                if (Utils.isUI_4_1_model(getActivity())) {
                    if (Utils.supportSplitView(getActivity())) {
                        mDefaultNavi = "Back|Home|RecentApps|DualWindow|";
                    } else {
                        mDefaultNavi = "Back|Home|RecentApps|";
                    }
                }
                Settings.System.putString(mContentResolver,
                        "button_combination", mDefaultNavi);

                //Navigation theme (color)
                String mDefaultTheme = SystemProperties.get("sys.ftm.theme");
                if (mDefaultTheme == null) {
                    mDefaultTheme = "com.lge.systemui.theme.white";
                    Log.d(TAG, "[Display] Theme property is null");
                }
                Settings.System.putString(mContentResolver,
                        "navigation_bar_theme", mDefaultTheme);

                //Transparent background
                Settings.System.putInt(mContentResolver,
                        "navigation_bar_option", 1);

                //Screen Capture area
                Settings.System.putInt(mContentResolver,
                        "screen_capture_area", 0);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException front key");
        }

        Log.d(TAG, "[Display] end");
    }

    private void resetBackupAndResetDB() {
        Log.d(TAG, "[Backup & reset] start");

        // Backup & reset > Backup my data
        IBackupManager backupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        try {
            backupManager.setBackupEnabled(false);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Backup & reset > Automatic restore
        Settings.Secure.putInt(mContentResolver,
                Settings.Secure.BACKUP_AUTO_RESTORE, 1);

        Log.d(TAG, "[Backup & reset] end");
    }

    private void resetPcConnectionDB() {
        Log.d(TAG, "[PC connection] start");

        // PC connection > USB connection type
        Context context = getActivity().getApplicationContext();
        UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);

        if (UsbSettingsControl.isMassStorageSupport(context)) {
            if (UsbSettingsControl.getUsbConnected(context)) {
                usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE, true);
                UsbSettingsControl.setMassStorage(context, true);
            } else {
                usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE, true);
                doSleep(20);
                UsbSettingsControl
                        .changeAutorunMode(context, UsbSettingsControl.USBMODE_UMS, false);
            }
        } else if (UsbSettingsControl.isMtpSupport(context)) {
            if (UsbSettingsControl.getUsbConnected(context)) {
                usbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_AUTO_CONF, true);
            } else {
                usbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_AUTO_CONF, true);
                doSleep(20);
                UsbSettingsControl
                    .changeAutorunMode(context, UsbSettingsControl.USBMODE_AUTOCONF, false);
            }
        }

        // PC connection > Ask on connection
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.USB_ASK_ON_CONNECTION, 1);

        // PC connection > PC Suite
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.LINKCLOUD_WIFI_CONNECTION, 0);

        // PC connection > On-Screen Phone
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.OSP_WIFI_CONNECTION, 0);
        /*
        // PC connection > Car Home(VZW)
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.CAR_HOME_AUTO_LAUNCH, 1);

        // PC connection > Desk Home(VZW)
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.DESK_HOME_AUTO_LAUNCH, 1);

        // PC connection > Media Home(VZW)
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.MEDIA_HOME_AUTO_LAUNCH, 1);

        // PC connection > Pouch Mode(VZW)
        Settings.System.putInt(mContentResolver,
                SettingsConstants.System.POUCH_MODE_AUTO_LAUNCH, 1);
        */
        Log.d(TAG, "[PC connection] end");
    }

    public static final String RESET_BLUETOOTH_SETTING_VALUE = "com.android.settings.bluetooth.RESET_BLUETOOTH_SETTING_VALUE";
    
    private void resetBtDB() {

        //LG_BTUI : RESET_SETTING - reset bluetooth setting values [s] , 20130821
        Log.d(TAG, "[Bt Reset DB] start");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "mBluetoothAdapter is null");
        }
        else {
            int state = mBluetoothAdapter.getState();
            Log.d(TAG, "state : " + state + "[off:10/turning on:11/on:12/turning off:13]");
        }

        Intent intent = new Intent(RESET_BLUETOOTH_SETTING_VALUE);
        getActivity().sendBroadcast(intent);

        Log.d(TAG, "[Bt Reset DB] end");
        //LG_BTUI : RESET_SETTING - reset bluetooth setting values [e] , 20130821
        /*
        Log.d(TAG, "[Bt Reset DB] start");

        String KEY_DISCOVERABLE_TIMEOUT = "com.android.settings_preferences";
        String KEY_SEARCH_FILTER = "BtUiSearchFilter";

        Log.d(TAG, "[Bt Reset DB] make Discoverable time as 2min");
        SharedPreferences mSharedPreferences = mContentView.getContext().getSharedPreferences(KEY_DISCOVERABLE_TIMEOUT, Context.MODE_PRIVATE);
        mSharedPreferences.edit().putString("bt_discoverable_timeout","twomin").commit();

        Log.d(TAG, "[Bt Reset DB] end");
        */
    }

    private void setAccessibilityResetDB() {
        Log.d("[ResetSetting][Accessibility]", "start");
        Context c = getActivity();
        Resources res = c.getResources();

        Intent accessibilityResetIntent = new Intent(
                "com.lge.android.intent.action.ACCESSIBILITY_RESET_SETTING");
        getActivity().sendBroadcast(accessibilityResetIntent);

        ContentResolver cr = getActivity().getContentResolver();

        // Services
        Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "");
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 0);

        // Large Text is resetted in resetDisplayDB()
        // Invert color & color adjustment
        
        // Invert color
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0);

        // Color adjustment
        int id = res.getIdentifier("config_invert_color_support", "bool", "com.lge");
        if (res.getBoolean(id)) {
            // Color adjustment
            Settings.Secure.putInt(cr, SettingsConstants.Secure.ACCESSIBILITY_COLOR_HUE, 0);
            Settings.Secure.putInt(cr, SettingsConstants.Secure.ACCESSIBILITY_COLOR_INTENSITY, 0);
            Settings.Secure.putFloat(cr, SettingsConstants.Secure.ACCESSIBILITY_COLOR_SAT, 0.0f);
            Settings.Secure.putFloat(
                    cr, SettingsConstants.Secure.ACCESSIBILITY_COLOR_CONTRAST, 0.0f);
            Settings.Secure.putInt(cr, SettingsConstants.Secure.ACCESSIBILITY_COLOR_CONFIG_MODE, 0);
            // TODO dialog check off
        }

        // Touch zoom
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0);

        // Voice notification is reseted in resetSoundDB()
        Settings.System.putInt(cr, SettingsConstants.System.HANDS_FREE_MODE_STATUS, 0);

        // Screen shades is reseted in resetDisplayDB()
        // Accessibility shortcut
        Settings.Global.putInt(cr, Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0);

        // Persistent alerts
        Settings.Secure.putInt(cr, "periodic_alerts_enable", 0);
        Settings.Secure.putString(cr, "periodic_alerts_package_names", "");
        Settings.Secure.putInt(cr, "key_periodic_alerts_interval", 0);
        // TODO change sharedpreference

        // Autio type
        Settings.System.putInt(cr, SettingsConstants.System.MONO_AUDIO_SETTINGS, 0);

        // Sound balance (0, 31, 62)
        Settings.System.putInt(cr, SettingsConstants.System.BALANCE_CHANGE_VALUE, 31);

        // Flash alert (service)
        // Call reject message
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_CALL_REJECT_MESSAGE_ENABLE, 0);

        // Turn off all sounds (service)
        // Captions
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED, 0);
        // Language(Locale)
        Settings.Secure.putString(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_LOCALE, "");
        // Text size
        Settings.Secure.putFloat(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE, 1.0f);
        // Caption style(Preset)
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_PRESET, 0);
        // Custom option
        // Font family
        Settings.Secure.putString(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_TYPEFACE, "");
        // Text color(Foreground color)
        Settings.Secure.putInt(
                cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR, Color.WHITE);
        // Edge type
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_TYPE, 0);
        // Edge color -16777216
        Settings.Secure.putInt(
                cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_COLOR, Color.BLACK);
        // Background color & opacity
        Settings.Secure.putInt(
                cr, Settings.Secure.ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR, Color.BLACK);
        // Character opacity
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_CAPTIONING_CHARACTER_OPACITY,
                0xFFFFFFFF);
        // Caption window color
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_CAPTIONING_WINDOW_COLOR, 0);
        // Caption window opacity 100%
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_CAPTIONING_WINDOW_OPACITY, 0xFFFFFFFF);

        // Touch feedback time
        Settings.Secure.putInt(cr, Settings.Secure.LONG_PRESS_TIMEOUT, Integer.parseInt("500"));

        // Universal touch (service)
        // TODO change coordinate

        // Screen timeout is reseted in resetDisplayDB()
        // KnockON is reseted in GestureSettingsResetDB()
        // Touch control areas
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_TOUCH_CONTROL_AREAS_ENABLE, 0);

        // Auto-rotate screen
        // false is checked value!!
        RotationPolicy.setRotationLockForAccessibility(getActivity(), false);
        Intent rotationIntent = new Intent("com.android.settings.rotation.CHANGED");
        getActivity().sendBroadcast(rotationIntent);

        // Read passwords
        Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0);

        // Power key ends call
        Settings.Secure.putInt(cr, Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF);

        // Accessibility settings shortcut
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_EASY_ACCESS_ENABLED_CATEGORY, 0);

        // One-touch input
        Settings.Secure.putInt(
                cr, SettingsConstants.Secure.ACCESSIBILITY_ONE_TOUCH_INPUT_SERVICE_ENABLE, 1);

        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SCRIPT_INJECTION, 0);

        Log.d("[ResetSetting][Accessibility]", "end");
    }

    private void setPowersaverResetDB() {
        Log.d("[ResetSetting][Powersave]", "start");

        if (Settings.System.getInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_ENABLED,
                PowerSave.DEFAULT_POWER_SAVE_ENABLED) != PowerSave.DEFAULT_POWER_SAVE_ENABLED) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.powersave.PowerSaveService");
            getActivity().stopService(intent);

            Settings.System.putInt(
                    getActivity().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_ENABLED,
                    PowerSave.DEFAULT_POWER_SAVE_ENABLED);
        }
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_MODE,
                PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);

        if ("VZW".equals(Config.getOperator())) {
            Settings.System.putInt(
                    getActivity().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BATTERY_INDICATOR, 1);
        } else {
            Settings.System.putInt(
                    getActivity().getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_BATTERY_INDICATOR, 0);
        }
        Intent intent = new Intent(PowerSave.ACTION_POWERSAVE_BATTERY_INDICATOR_CHANGED);
        getActivity().sendBroadcast(intent);

        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_WIFI, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_SYNC, 0);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_BT, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_NFC, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_TOUCH, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_CPU_ADJUST,
                1);
        Settings.System.putInt(
                getActivity().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_BRIGHTNESS_ADJUST, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_BRIGHTNESS,
                PowerSave.DEFAULT_BRIGHTNESS);
        Settings.System.putInt(
                getActivity().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT_ADJUST, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_SCREEN_TIMEOUT,
                PowerSave.DEFAULT_SCREEN_TIMEOUT);
        Settings.System.putInt(
                getActivity().getContentResolver(),
                SettingsConstants.System.POWER_SAVE_FRONT_LED_ADJUST, 1);
        Settings.System.putInt(
                getActivity().getContentResolver(), SettingsConstants.System.POWER_SAVE_FRONT_LED,
                PowerSave.DEFAULT_FRONT_LED);
        Settings.System.putInt(getActivity().getContentResolver(),
                PowerSaveSettings.POWER_SAVE_EMOTIONAL_LED, 0);

        Log.d("[ResetSetting][Powersave]", "end");
    }

    private void resetDateAndTimeDB() {
        Log.d(TAG, "[Date & time] start");

        // Auto date and time
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.AUTO_TIME, DATETIME);

        // Auto time zone
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.AUTO_TIME_ZONE, DATETIME);

        // Time format
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.TIME_12_24, "12");
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        getActivity().sendBroadcast(timeChanged);

        // Date format
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.DATE_FORMAT, "");
        Intent timeChangedFormat = new Intent(Intent.ACTION_TIME_CHANGED);
        getActivity().sendBroadcast(timeChangedFormat);

        Log.d(TAG, "[Date & time] end");
    }

    private void resetLanguageAndInputDB() {
        Log.d(TAG, "[Language & input] start");

        //pointer speed
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.POINTER_SPEED, 0);
        //speech rate
        Settings.Secure.putInt(getActivity().getContentResolver(),
                Settings.Secure.TTS_DEFAULT_RATE, 100);
        //default keyboard
        Settings.Secure.putString(getActivity().getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD, "com.lge.ime/.LgeImeImpl");

        //personal dictionary
        mContentResolver.delete(UserDictionary.Words.CONTENT_URI, null, null);

        Log.d(TAG, "[Language & input] end");
    }

    private void resetSoundDB() {
        Log.d(TAG, "[SoundData] start");
        SoundSettings mSoundSettings = new SoundSettings();
        mSoundSettings.reSetSoundSetting(getActivity());

        Log.d(TAG, "[SoundData] end");
    }

    private void locationServicesReset() {
        /*      //Location access (only JB)
                Settings.Secure.setLocationProviderEnabled(
                    getActivity().getContentResolver(), "vzw_lbs", false);
                Settings.Secure.setLocationProviderEnabled(
                     getActivity().getContentResolver(), LocationManager.GPS_PROVIDER, false);
                Settings.Secure.setLocationProviderEnabled(getActivity().getContentResolver(),
                                LocationManager.NETWORK_PROVIDER, false);
        */
        //Location access (add KK)
        //(Settings.Secure.LOCATION_MODE_OFF / Settings.Secure.LOCATION_MODE_HIGH_ACCURACY / Settings.Secure.LOCATION_MODE_BATTERY_SAVING / Settings.Secure.LOCATION_MODE_SENSORS_ONLY)
        Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
    }

    private void resetMobileDataDB() {
        Log.d(TAG, "[MobileData] start");

        final int mDefaultOn = 1;

        // Data usage limit value
        resetMobileDataUsageSummaryValue();

        //AIRPLANE_MODE_ON
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", false);
        getActivity().sendBroadcast(intent);
        // do not show check box
        if ( Utils.isUI_4_1_model(getActivity()) ) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    "airplane_mode_dialog_do_not_show_this_again", 0);
        }

        //MOBILE_DATA
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.MOBILE_DATA + OverlayUtils.getDefaultPhoneID(getActivity()), mDefaultOn);

        //DATA_ROAMING, GLOBAL_ROAMING_DATA_ALLOW_ACCESS_VALUES
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.DATA_ROAMING + OverlayUtils.getDefaultPhoneID(getActivity()), 0);
        Settings.System.putInt(getActivity().getContentResolver(),
                SettingsConstants.System.GLOBAL_ROAMING_DATA_ALLOW_ACCESS_VALUES, mDefaultOn);

        //PREFERRED_NETWORK_MODE
        int type;
        type = SystemProperties.getInt("ro.telephony.default_network",
                RILConstants.PREFERRED_NETWORK_MODE);
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE, type);
        Intent intent_modechange = new Intent();
        intent_modechange.setAction("com.lge.settings.action.CHANGE_NETWORK_MODE");
        getActivity().sendBroadcast(intent_modechange);

        /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [START] */
        //MOBILE_DATA Enable after resetMobileDataDB
        if (mCm != null && mCm.getMobileDataEnabled()) {
            Log.d(TAG, "setMobileDataEnabled(mDefaultOn)");
            mTM.setDataEnabled(true);
        }
        /* 2014-04-15 taegil.kim@lge.com LGP_DATA_MOBILE_DATA_ENABLE_FOR_RESET_SETTING_VZW [END] */

        Log.d(TAG, "[MobileData] end");
    }

    private void resetModeChangeDB() {
        Log.d("[ResetSetting][ModeChange]", "start");
        Settings.System.putInt(getActivity().getContentResolver(), "ui_type_settings", 0);
        Log.d("[ResetSetting][ModeChange]", "end");
    }

    private void GestureSettingsResetDB() {

        if (Utils.isUI_4_1_model(getActivity()) && !Utils.supportSplitView(getActivity())) {
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_voice_call",
                    DB_SET_ON);
            Settings.System.putInt(
                    getActivity().
                            getContentResolver(),
                    "gesture_fadeout_ringtone",
                    DB_SET_ON);
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_video_player",
                    DB_SET_ON);
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_alarm", DB_SET_ON);
        } else {
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_voice_call", 0);
            Settings.System.putInt(
                    getActivity().
                            getContentResolver(),
                    "gesture_fadeout_ringtone",
                    DB_SET_ON);
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_video_player", 0);
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_alarm", 0);
        }
        Settings.System.putInt(getActivity().getContentResolver(), "gesture_home_rearrange", 0);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "gesture_answering",
                DB_SET_OFF);

        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "hide_display",
                DB_SET_OFF);
        Context context = getActivity().getApplicationContext();
        if (!ConfigHelper.isSupportScreenOnOff(context)
                && !ConfigHelper.isSupportScreenOff(context)) {
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_trun_screen_on",
                    DB_SET_OFF);
        } else {
            Settings.System.putInt(getActivity().getContentResolver(), "gesture_trun_screen_on",
                    DB_SET_ON);
        }
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "multitasking_slide_aside",
                DB_SET_ON);
    }

    private void OneHandOperationSettingsResetDB() {
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_dial_keypad",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_lg_keyboard",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_keyboard_gesture",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_lock_screen",
                DB_SET_OFF);

        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_front_touch_button",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_navigation_button",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_pull_down_screen",
                DB_SET_OFF);
        Settings.System.putInt(
                getActivity().
                        getContentResolver(),
                "one_hand_mini_view",
                DB_SET_ON);
    }

    private void CoverSettingsResetDB() {
        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == true
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == true) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 1);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 3);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 1);
        } else if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(getActivity(),
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 5);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        } else {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_COVER_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.QUICK_VIEW_ENABLE, 0);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.COVER_TYPE, 5);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.WINDOW_AUTO_UNLOCK_ENABLE, 0);
        }
    }

    private void doSleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.d(TAG, "[AUTORUN] waiting exception");
        }
    }

    private void resetNFC() {
        Log.d(TAG, "resetNFC start");

        NfcAdapterAddon mNfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
        if (mNfcAdapterAddon == null) {
            return;
        }

        Settings.Global.putInt(getActivity().getContentResolver(),
                SettingsConstants.Global.NFC_FIRST_ON_CHECK, 0);

        SharedPreferences preferences = getActivity().getSharedPreferences(
                NfcSettingsFragment.PREFS_NAME, Context.MODE_PRIVATE);

        preferences.edit()
                .putBoolean(NfcSettingsFragment.KEY_NFC_REMIDER, true).commit();
        preferences.edit()
                .putBoolean(NfcSettingsFragment.KEY_SHOW_NFC_EVERY, false)
                .commit();

        mNfcAdapterAddon.setNfcAddonPreference(
                NfcSettingsFragment.NUMBER_TAG_DEFAULT_CHECKBOX, true);
        mNfcAdapterAddon.setNfcAddonPreference(
                NfcSettingsFragment.NUMBER_HANDOVER_CHECKBOX, true);
        mNfcAdapterAddon.setNfcAddonPreference(
                NfcSettingsFragment.NUMBER_CALLING_NFCSOUND, false);

        mNfcAdapterAddon.disableNfcDiscovery();
        mNfcAdapterAddon.disableNfcCard();
        mNfcAdapterAddon.deinitNfcSystem();
        mNfcAdapterAddon.disableNfcP2p(); // androidbeam off

        Log.d(TAG, "resetNFC end");
    }

    private void resetTangibleDB() {
        Log.d("[ResetSetting][resetTangibleDB]", "start");
        if (SystemProperties.get("ro.product.name").contains("vu3")
                || (SystemProperties.get("ro.product.name").contains("g2") &&
                !Utils.isUI_4_1_model(getActivity().getApplicationContext()))) {
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_PEN_PANEL_ENABLE, 2);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE, 2);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE, 2);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE, 2);
        } else {
            if ("VZW".equals(Config.getOperator())) {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_PEN_PANEL_ENABLE, 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE, 0);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE, 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE, 0);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_PEN_PANEL_ENABLE, 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_EARPHONE_PANEL_ENABLE, 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_DOCK_PANEL_ENABLE, 1);
                Settings.Global.putInt(getActivity().getContentResolver(),
                        SettingsConstants.Global.TANGIBLE_USBSTORAGE_PANEL_ENABLE, 1);
            }
        }
        Log.d("[ResetSetting][resetTangibleDB]", "end");
    }

    public void resetDefaultMessageApp() {
        Resources r = getActivity().getResources();
        String defaultPackage = r.getString(com.android.internal.R.string.default_sms_application);
        Log.d(TAG, "defaultPackage = " + defaultPackage);
        SmsApplication.setDefaultApplication(defaultPackage, getActivity());
    }

    public void resetApps() {
        Log.d(TAG, "resetApps start");
        if (true) {
            final PackageManager pm = getActivity().getPackageManager();
            //rebestm - kk migration
            //final IPackageManager mIPm = IPackageManager.Stub.asInterface(
            //        ServiceManager.getService("package"));
            final INotificationManager nm = INotificationManager.Stub.asInterface(
                    ServiceManager.getService(Context.NOTIFICATION_SERVICE));
            final NetworkPolicyManager npm = NetworkPolicyManager.from(getActivity());
            //rebestm - kk migration
            final AppOpsManager aom = (AppOpsManager)getActivity().getSystemService(
                    Context.APP_OPS_SERVICE);
            //final Handler handler = new Handler(getActivity().getMainLooper());
            (new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    List<ApplicationInfo> apps;
                    apps = pm.getInstalledApplications(PackageManager.GET_DISABLED_COMPONENTS);

                    for (int i = 0; i < apps.size(); i++) {
                        ApplicationInfo app = apps.get(i);
                        //rebestm - kk migration
                        try {
                            nm.setNotificationsEnabledForPackage(app.packageName, app.uid, true);
                        } catch (android.os.RemoteException ex) {
                            Log.w(TAG, "android.os.RemoteException");
                        }

                        if ("com.android.settings".equals(app.packageName)
                                || "com.lge.settings.easy".equals(app.packageName)) {
                            Log.w(TAG, "com.android.settings || com.lge.settings.easy, 1");
                        } else {
                            pm.clearPackagePreferredActivities(app.packageName);
                        }
                        if (!app.enabled) {
                            if (pm.getApplicationEnabledSetting(app.packageName)
                            == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                                pm.setApplicationEnabledSetting(app.packageName,
                                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                        PackageManager.DONT_KILL_APP);
                            }
                        }
                    }

                    ArrayList<ComponentName> prefActivities;
                    prefActivities = new ArrayList<ComponentName>();

                    ArrayList<IntentFilter> filters;
                    filters = new ArrayList<IntentFilter>();

                    pm.getPreferredActivities(filters, prefActivities, null);

                    for (int i = 0; i < prefActivities.size(); i++) {
                        if ("com.android.settings".equals(prefActivities.get(i).getPackageName())
                                || "com.lge.settings.easy".equals(prefActivities.get(i)
                                        .getPackageName())) {
                            Log.w(TAG, "com.android.settings || com.lge.settings.easy, 2");
                        } else {
                            pm.clearPackagePreferredActivities(prefActivities.get(i)
                                    .getPackageName());
                        }
                    }

                    aom.resetAllModes();
                    final int currentUserId = ActivityManager.getCurrentUser();
                    final int[] restrictedUids;
                    restrictedUids = npm.getUidsWithPolicy(POLICY_REJECT_METERED_BACKGROUND);
                    for (int uid : restrictedUids) {
                        // Only reset for current user
                        if (UserHandle.getUserId(uid) == currentUserId) {
                            npm.setUidPolicy(uid, POLICY_NONE);
                        }
                    }
                    return null;
                }
            }).execute();
        }
        Log.d(TAG, "resetApps END");
    }

    private void resetMobileDataUsageSummaryValue() {
        SharedPreferences mPrefs;
        Context context = getActivity().getApplicationContext();
        NetworkPolicyManager mPolicyManager;
        NetworkTemplate mTemplate;
        NetworkPolicyEditor mPolicyEditor;
        mPolicyManager = NetworkPolicyManager.from(context);
        mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
        mPolicyEditor.read();
        mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context));
        if (null != mTemplate
                && null != mPolicyEditor) {
            mPolicyEditor.setPolicyLimitBytes(mTemplate, LIMIT_DISABLED);
            mPolicyEditor.setPolicyWarningBytes(mTemplate, WARNING_DISABLED);
            Log.d(TAG, "resetMobileDataLimitedValue = LIMIT_DISABLED");
        }

        //reset the background restrict
        Settings.Secure.putInt(context.getContentResolver(),
                SettingsConstants.Secure.DATA_NETWORK_USER_BACKGROUND_SETTING_DATA, 0);
        mPolicyManager.setRestrictBackground(false);

        //reset the option menu, display unit, wifi, ethernet.
        mPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        mPrefs.edit().putBoolean(PREF_SHOW_WIFI, false).apply();
        mPrefs.edit().putBoolean(PREF_SHOW_ETHERNET, false).apply();
        mPrefs.edit().putBoolean(PREF_DEFAULT_UNIT, true).apply();
        ContentResolver.setMasterSyncAutomatically(true);

    }

    private void resetWirelessStorage() {
        Intent stopServer = new Intent("com.lge.wirelessstorage.action.RESET_SETTINGS");
        getActivity().sendBroadcast(stopServer);
    }

    private String getActiveSubscriberId(Context context) {
        final String mTEST_SUBSCRIBER_PROP = "test.subscriberid";
        final TelephonyManager tele = TelephonyManager.from(context);
        final String actualSubscriberId = tele.getSubscriberId();
        Log.d(TAG, "subscriberId = "
                + SystemProperties.get(mTEST_SUBSCRIBER_PROP, actualSubscriberId));
        return SystemProperties.get(mTEST_SUBSCRIBER_PROP, actualSubscriberId);
    }

    private void resetSmartShareBeam() {
        Intent stop = new Intent();
        stop.setAction("com.lge.smartsharebeam.settingreset");
        stop.setPackage("com.android.settings");
        getActivity().startService(stop);
    }

    private void resetShortcutKeyStatus() {
        Settings.System.putInt(mContentResolver, "shortcut_key_status", DB_SET_ON);
    }
}
