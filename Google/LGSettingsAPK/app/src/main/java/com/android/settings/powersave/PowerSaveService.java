package com.android.settings.powersave;

import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;

import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

import com.android.settings.R;
import android.content.res.Configuration;
import com.android.settings.lgesetting.Config.Config;

public class PowerSaveService extends Service {

    private static final String TAG = "PowerSaveService";

    /** Indicate if phone is in emergency callback mode */
    private static final String PROPERTY_INECM_MODE = "ril.cdma.inecmmode";

    private final IBinder mBinder = new PowerSaveServiceLocalBinder();

    private boolean mPowerSaveStarted = false;
    private int mCurrentBatteryLevel = -1;
    private int mPlugType = 0;

    public static final int NOTIFICATION_ID = R.drawable.notify_powersaver_activation;

    private Notification mNotification;
    private NotificationManager mNotificationManager = null;

    private PowerSave mPowerSave;

    private boolean initCheck = false;

    //jw 2013_08_23
    private Context mContext;
    private final int mImmediatelyValue = 100;
    private final int mLogDefaultValue = 2;
    private BroadcastReceiver mPowerSaveReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mContext = context;
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                //Log.i(TAG, "onReceive, ACTION_BATTERY_CHANGED");

                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);

                mCurrentBatteryLevel = level * 100 / scale;
                int powerSaveModeValue = getPowerSaveModeValue(context);

                mPlugType = intent.getIntExtra("plugged", 0);

                Log.i(TAG, "mPowerSaveStarted : " + (mPowerSaveStarted ? "true, " : "false, ")
                        + "mCurrentBatteryLevel : " + mCurrentBatteryLevel
                        + ", powerSaveModeValue : " + powerSaveModeValue
                        + ", plugType : " + mPlugType);
                Log.i(TAG, "power_save_mode_activated: " +
                        Settings.System.getInt
                                (getContentResolver(), SettingsConstants.System.
                                        POWER_SAVE_MODE_ACTIVATED, mLogDefaultValue));

                if (mPowerSaveStarted && mPlugType > 0) {
                    //                    endNotification();
                    startNotification(PowerSave.NOTIFICATION_ON_CHARGING);

                    /* LGE_CHANGE_S, Power save restore */
                    if (getPowerSaveStarted() == PowerSave.NOTIFICATION_ACTIVATED) {
                        mPowerSave.doRestore();
                    }
                    /* LGE_CHANGE_E, Power save restore */

                    // Power Control update - Deactivation State
                    mPowerSave.updatePowerControl();

                    mPowerSaveStarted = false;
                    setActivationState(false);
                    setPowerSaveStarted(-1);
                } else if (mPlugType > 0) {
                    startNotification(PowerSave.NOTIFICATION_ON_CHARGING);
                    setActivationState(false);
                } else if (mPlugType == 0) {
                    //if( (powerSaveModeValue == -1) || mCurrentBatteryLevel <= powerSaveModeValue) {
                    if (mCurrentBatteryLevel <= powerSaveModeValue) {
                        if (!mPowerSaveStarted && checkCondition()) {
                            Intent i = new Intent(context, PowerSaveWarningPopupActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            mPowerSaveStarted = true;
                            Log.i(TAG, "PowerSaveWarning" +
                                    "PopupActivity " + "start ACTION_BATTERY_CHANGED");
                        }
                    } else {
                        startNotification(PowerSave.NOTIFICATION_WAIT_BATTERY_LEVEL);
                        setActivationState(false);
                        if (mPowerSaveStarted && mCurrentBatteryLevel > powerSaveModeValue) {
                            //                            endNotification();

                            /* LGE_CHANGE_S, Power save restore */
                            if (getPowerSaveStarted() == PowerSave.NOTIFICATION_ACTIVATED) {
                                mPowerSave.doRestore();
                            }
                            /* LGE_CHANGE_E, Power save restore */

                            mPowerSaveStarted = false;

                            setActivationState(false);
                            setPowerSaveStarted(-1);

                            // Power Control update - Deactivation State
                            mPowerSave.updatePowerControl();
                        }
                    }
                }
            } else if (PowerSave.ACTION_POWERSAVE_MODE_CHANGED.equals(action)) {
                Log.i(TAG, "PowerSave.ACTION_POWERSAVE_MODE_CHANGED received");
                int powerSaveModeValue = getPowerSaveModeValue(context);

                if (mCurrentBatteryLevel > -1 && mPlugType == 0) { // No first action
                    if (mCurrentBatteryLevel <= powerSaveModeValue) {
                        if (!mPowerSaveStarted && checkCondition()) {
                            Intent i = new Intent(context, PowerSaveWarningPopupActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            Log.i(TAG, "PowerSaveWarningPopupActivity " +
                                    "start ACTION_POWERSAVE_MODE_CHANGED");
                            mPowerSaveStarted = true;
                        }
                    } else {
                        startNotification(PowerSave.NOTIFICATION_WAIT_BATTERY_LEVEL);
                        setActivationState(false);
                        if (mPowerSaveStarted && mCurrentBatteryLevel > powerSaveModeValue) {
                            //                            endNotification();

                            /* LGE_CHANGE_S, Power save restore */
                            if (getPowerSaveStarted() == PowerSave.NOTIFICATION_ACTIVATED) {
                                mPowerSave.doRestore();
                            }
                            /* LGE_CHANGE_E, Power save restore */

                            mPowerSaveStarted = false;

                            setActivationState(false);
                            setPowerSaveStarted(-1);

                            // Power Control update - Deactivation State
                            mPowerSave.updatePowerControl();
                        }
                    }
                }
            }
            else if (PowerSave.ACTION_POWERSAVE_ACTIVATION.equals(action)) {
                int doActivation = intent.getIntExtra(PowerSave.EXTRA_POWERSAVE_ACTIVATION, 0);
                if (doActivation == 1) {
                    mPowerSave.doPowerSave();
                    //changeNotificationIcon();
                    //mPowerSaveStarted = true;
                    endNotification();
                    startNotification(PowerSave.NOTIFICATION_ACTIVATED);
                    setPowerSaveStarted(PowerSave.NOTIFICATION_ACTIVATED);

                    setActivationState(true);

                    // Power Control update - Activation State
                    mPowerSave.updatePowerControl();
                } else {
                    endNotification();
                    // yonguk.kim 20120424 Immediately start
                    int mode_value = Settings.System.getInt(getContentResolver(),
                            SettingsConstants.System.POWER_SAVE_MODE,
                            PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
                    if (mode_value == PowerSave.POWER_SAVE_MODE_VALUE_IMMEDIATLY) {
                        Settings.System.putInt(getContentResolver(),
                                SettingsConstants.System.POWER_SAVE_ENABLED,
                                PowerSave.DEFAULT_POWER_SAVE_ENABLED);
                        stopSelf();
                    } else {
                        startNotification(PowerSave.NOTIFICATION_SELECT);
                        setPowerSaveStarted(PowerSave.NOTIFICATION_SELECT);

                        setActivationState(false);
                    }
                }
            }
            else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                /* Notification language update */
                int powerSaveState = getPowerSaveStarted();
                if (powerSaveState > -1) {
                    Log.i(TAG, "Intent.ACTION_LOCALE_CHANGED receive, POWER_SAVE_STARTED : "
                            + powerSaveState);
                    startNotification(powerSaveState);
                }

            }
            /* Start, test code */
            else if ("com.android.settings.powersave.POWERSAVE_GO".equals(action)) {
                if (checkCondition()) {
                    Intent i = new Intent(context, PowerSaveWarningPopupActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                    mPowerSaveStarted = true;
                }
            }
            /* End, test code */
        }
    };

    public class PowerSaveServiceLocalBinder extends Binder {
        PowerSaveService getService() {
            Log.i(TAG, "getService");
            return PowerSaveService.this;
        }
    }

    private void init() {
        Log.i(TAG, "initialize");

        mContext = getApplicationContext();
        mPowerSave = new PowerSave(getApplicationContext());

        //Battery on_off Userlog
        mPowerSave.onBatterySaverEnabled(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, 0) > 0);

        // Register receiver for intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(PowerSave.ACTION_POWERSAVE_ACTIVATION);
        filter.addAction(PowerSave.ACTION_POWERSAVE_MODE_CHANGED);
        filter.addAction("com.android.settings.powersave.POWERSAVE_GO");
        registerReceiver(mPowerSaveReceiver, filter);

        // Power Control update - On State
        mPowerSave.updatePowerControl();

        initCheck = true;
    }

    private void checkPowerSaveStarted() {
        int powerSaveState = getPowerSaveStarted();
        if (powerSaveState > -1) {
            Log.i(TAG, "checkPowerSaveStarted, POWER_SAVE_STARTED > -1");
            mPowerSaveStarted = true;
            startNotification(powerSaveState);
        }
        else {
            Log.i(TAG, "checkPowerSaveStarted, POWER_SAVE_STARTED : -1");
            mPowerSaveStarted = false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate, service create");

        mContext = getApplicationContext();
        checkPowerSaveStarted();

        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand, service start");

        if ((flags & START_FLAG_RETRY) == 0) {
            Log.i(TAG, "onStartCommand, service re-start");
        }

        if (!initCheck) {
            init();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy, service stop");

        unregisterReceiver(mPowerSaveReceiver);

        //Battery on_off Userlog
        mPowerSave.onBatterySaverEnabled(Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_ENABLED, 0) > 0);

        // Notification
        //endNotification();

        /* LGE_CHANGE_S, Power save restore */
        if (getPowerSaveStarted() == PowerSave.NOTIFICATION_ACTIVATED) {
            mPowerSave.doRestore();
        }
        /* LGE_CHANGE_E, Power save restore */

        //if(mPowerSaveStarted) {
        endNotification();
        mPowerSaveStarted = false;

        setActivationState(false);
        setPowerSaveStarted(-1);
        //}

        // Power Control update - Off State
        mPowerSave.updatePowerControl();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        return mBinder;
    }

    private int getPowerSaveModeValue(Context context) {
        return Settings.System.getInt(
                context.getContentResolver(),
                SettingsConstants.System.POWER_SAVE_MODE, PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
    }

    //    private void setPowerSaveModeAllowed(int enabled) {
    //        Settings.System.putInt(getContentResolver(), Settings.System.POWER_SAVE_MODE, enabled);
    //    }

    private boolean onlyActivateNotification() {
        if ("LGU".equals(Config.getOperator()) || "ATT".equals(Config.getOperator())) {
            return true;
        } else {
            return false;
        }
    }

    /* Notification */
    private void startNotification(int mode) {
        if (mode != PowerSave.NOTIFICATION_ACTIVATED && onlyActivateNotification()) {
            if (mNotificationManager != null) {
                endNotification();
            }
            return;
        }
        int icon;
        String tickerText;
        String text;
        String title = getString(R.string.sp_power_saver_NORMAL);
        Intent intent = new Intent(mContext, PowerSaveSettings.class);
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mode == PowerSave.NOTIFICATION_ACTIVATED) {
            icon = R.drawable.notify_powersaver_activation;
            tickerText = getString(R.string.sp_power_saver_noti_ticker_activated_NORMAL);
            text = getString(R.string.sp_power_save_notification_activated_NORMAL);
            intent = new Intent("com.lge.settings.POWER_SAVER_SETTINGS");
        } else if (mode == PowerSave.NOTIFICATION_SELECT) {
            icon = R.drawable.notify_powersaver_stand_by;
            tickerText = getString(R.string.sp_power_saver_noti_ticker_select_NORMAL);
            text = getString(R.string.sp_power_save_notification_select_NORMAL);
            intent = new Intent(this, PowerSaveWarningPopupActivity.class);
        } else if (mode == PowerSave.NOTIFICATION_WAIT_BATTERY_LEVEL) {
            icon = R.drawable.notify_powersaver_stand_by;
            int level = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.POWER_SAVE_MODE,
                    PowerSave.FALLBACK_POWER_SAVE_MODE_VALUE);
            tickerText = String.format(
                    getString(R.string.sp_power_save_waiting_battery_level_NORMAL),
                    String.valueOf(level) + "%");
            text = String.format(getString(R.string.sp_power_save_waiting_battery_level_NORMAL),
                    String.valueOf(level) + "%");
            intent = new Intent("com.lge.settings.POWER_SAVER_SETTINGS");
        } else {
            icon = R.drawable.notify_powersaver_stand_by;
            tickerText = getString(R.string.sp_power_save_on_charging_NORMAL);
            text = getString(R.string.sp_power_save_on_charging_NORMAL);
            intent = new Intent("com.lge.settings.POWER_SAVER_SETTINGS");
        }

        mNotification = new Notification(icon, tickerText, 0);
        mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;

        if (Utils.supportSplitView(getApplicationContext())) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        mNotification.setLatestEventInfo(mContext, title, text, contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private void endNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void setActivationState(boolean enabled) {
        Settings.System.putInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_MODE_ACTIVATED, enabled ? 1 : 0);
    }

    private void setPowerSaveStarted(int enabled) {
        Log.i(TAG, "setPowerSaveStarted, POWER_SAVE_STARTED : " + enabled);
        Settings.System.putInt(getContentResolver(), SettingsConstants.System.POWER_SAVE_STARTED,
                enabled);
    }

    private int getPowerSaveStarted() {
        return Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.POWER_SAVE_STARTED, -1);
    }

    /*
     * Auto Power Save will not be activated on customized battery level and
     * it will  wait for finishing those activity, if one of following condition meets,
     * - When Camera or Voice recorder is currently running
     * - In call
     */
    private boolean checkCondition() {
        boolean canDo = true;

        /* Check the factory mode */
        //if (PowerSaveFactoryMode.checkFactoryMode()) {
        //    return false;
        //}

        // jongtak0920.kim 121029 Check ECM mode
        try {
            canDo = !(Boolean.parseBoolean(SystemProperties.get(PROPERTY_INECM_MODE)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> Info = am.getRunningTasks(1);

        if (Info == null) {
            return canDo;
        }

        ComponentName topActivity = Info.get(0).topActivity;
        String topActivityName = topActivity.getPackageName();
        //String topActivityName = topActivity.getClassName();

        if (PowerSave.PACKAGENAME_CAMERA.equals(topActivityName)) {
            canDo = false;
            int powerSaveModeValue = getPowerSaveModeValue(mContext);
            if (powerSaveModeValue == mImmediatelyValue) {
                canDo = true;
                Log.d("jw", "Video Execption powerSaveModeValue = 100");
            }
        }
        if (PowerSave.PACKAGENAME_VOICERECORDER.equals(topActivityName)) {
            canDo = false;
        }

        // Hidden menu check, Start
        String topActivityClassName = topActivity.getClassName();
        if ("com.lge.hiddenmenu.wcdma_only.module_test.ChargingTest".equals(topActivityClassName)) {
            canDo = false;
        } else if ("com.lge.LgHiddenMenu.LgSrvChargingTest".equals(topActivityClassName)) {
            canDo = false;
        }
        // Hidden menu check, End

        /* Call activity check Start */
        if ("com.android.incallui.InCallActivity".
                equals(topActivityClassName)) {
            canDo = false;
        }
        if ("com.lge.ltecall.videocall.ui.VgaVideoCallActivity".
                equals(topActivityClassName)) {
            canDo = false;
        }
        if ("com.lge.ltecall.videocall.ui.QcifVideoCallActivity".
                equals(topActivityClassName)) {
            canDo = false;
        }
        if ("com.lge.ltecall.videocall.ui.GroupVideoCallActivity".
                equals(topActivityClassName)) {
            canDo = false;
        }
        if ("com.lge.vt.ui.QcifGroupVideoCallActivity".equals(topActivityClassName)) {
            canDo = false;
        }
        /* Call activity check End */

        // Call state check
        CallManager mCM = CallManager.getInstance();
        PhoneConstants.State phoneState = mCM.getState();
        if (phoneState == PhoneConstants.State.OFFHOOK
                || phoneState == PhoneConstants.State.RINGING) {
            Log.i(TAG, "CallManager CallState : " + phoneState);
            canDo = false;
        }

        // jongtak0920.kim 130513 Call state check
        try {
            ITelephony telephonyService = ITelephony.Stub.asInterface(ServiceManager
                    .checkService(Context.TELEPHONY_SERVICE));

            if (telephonyService != null) {
                if (telephonyService.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK
                        || telephonyService.getCallState() == TelephonyManager.CALL_STATE_RINGING
                        || telephonyService.getCallState() >= 100) { // jongtak0920.kim 130620 In video call
                    Log.i(TAG, "TelephonyManager CallState : " + telephonyService.getCallState());
                    canDo = false;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }

        return canDo;
    }
}
