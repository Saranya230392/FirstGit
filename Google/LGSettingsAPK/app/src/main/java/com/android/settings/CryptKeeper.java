/*
 * Copyright (C) 2011 The Android Open Source Project
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

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.settings.lockscreen.widget.LgeLockPatternViewBase;
import com.android.settings.lockscreen.widget.OnPatternListener;

import android.app.Activity;
import android.app.StatusBarManager;
import android.app.StatusBarManagerEx;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.SystemProperties;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import android.telephony.ServiceState;
import android.telephony.PhoneStateListener;
import com.android.settings.hotkey.HotkeyInfo;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.MDMSettingsAdapter;

import java.util.List;

import com.lge.mdm.LGMDMManager;

import com.lge.view.ViewUtil;

import android.view.Menu;
import android.view.MenuItem;
import android.content.res.Configuration;

import com.lge.constants.ViewConstants;
import com.lge.constants.StatusBarManagerConstants;
import com.lge.constants.NavigationButtonConstants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.FileReader;
import com.lge.constants.SettingsConstants;
/**
 * Settings screens to show the UI flows for encrypting/decrypting the device.
 *
 * This may be started via adb for debugging the UI layout, without having to go through
 * encryption flows everytime. It should be noted that starting the activity in this manner
 * is only useful for verifying UI-correctness - the behavior will not be identical.
 * <pre>
 * $ adb shell pm enable com.android.settings/.CryptKeeper
 * $ adb shell am start \
 *     -e "com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW" "progress" \
 *     -n com.android.settings/.CryptKeeper
 * </pre>
 */
public class CryptKeeper extends Activity implements TextView.OnEditorActionListener,
        OnKeyListener, OnTouchListener, TextWatcher {
    private static final String TAG = "CryptKeeper";

    private static final String DECRYPT_STATE = "trigger_restart_framework";
    /** Message sent to us to indicate encryption update progress. */
    private static final int MESSAGE_UPDATE_PROGRESS = 1;
    /** Message sent to us to cool-down (waste user's time between password attempts) */
    private static final int MESSAGE_COOLDOWN = 2;
    /** Message sent to us to indicate alerting the user that we are waiting for password entry */
    private static final int MESSAGE_NOTIFY = 3;

    // Constants used to control policy.
    private static final int MAX_FAILED_ATTEMPTS = 30;
    private static final int MAX_FAILED_ATTEMPTS_VZW = 10;
    private static final int COOL_DOWN_ATTEMPTS = 10;
    private static final int COOL_DOWN_INTERVAL = 30; // 30 seconds
    private static final int WAKE_LOCK_SECONDS = 180;

    // Intent action for launching the Emergency Dialer activity.
    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";

    // Debug Intent extras so that this Activity may be started via adb for debugging UI layouts
    private static final String EXTRA_FORCE_VIEW =
            "com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW";
    private static final String FORCE_VIEW_PROGRESS = "progress";
    private static final String FORCE_VIEW_ERROR = "error";
    private static final String FORCE_VIEW_PASSWORD = "password";
    private static final String CRYPT_INFO_PATH = "persist-lg/encryption/info.txt";
    private static final String CRYPT_INFO_SELFTESTFAIL = "F: selftest fail factory reset!\n\r";

    /** When encryption is detected, this flag indicates whether or not we've checked for errors. */
    private boolean mValidationComplete;
    private boolean mValidationRequested;
    /** A flag to indicate that the volume is in a bad state (e.g. partially encrypted). */
    private boolean mEncryptionGoneBad;
    /** A flag to indicate when the back event should be ignored */
    private boolean mIgnoreBack = false;
    private int mCooldown = 0;
    PowerManager.WakeLock mWakeLock;
    private EditText mPasswordEntry;
    private LgeLockPatternViewBase mLockPatternView;
    /** Number of calls to {@link #notifyUser()} to ignore before notifying. */
    private int mNotificationCountdown = 0;
    private int mStatusString = R.string.enter_password;
    private String mPasswordString;
    private boolean isVZW = Utils.istargetOperator("VZW");

    private HotkeyInfo mHotkeyInfo;
    private ServiceState mServiceState;
    private RandomAccessFile mFile;
    private String mInfo;
    // how long we wait to clear a wrong pattern
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 200;

    private static final int RIGHT_PATTERN_CLEAR_TIMEOUT_MS = 500;

    private static final int DEFAULT_PASSWORD_TYPE = 0;
    private static final int PATTERN_PASSWORD_TYPE = 1;
    private static final int PIN_PASSWORD_TYPE = 2;
    private static final int CHAR_PASSWORD_TYPE = 3;

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    /**
     * Used to propagate state through configuration changes (e.g. screen rotation)
     */
    private static class NonConfigurationInstanceState {
        final PowerManager.WakeLock wakelock;

        NonConfigurationInstanceState(PowerManager.WakeLock _wakelock) {
            wakelock = _wakelock;
        }
    }

    /**
     * Activity used to fade the screen to black after the password is entered.
     */
    public static class FadeToBlack extends Activity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.crypt_keeper_blank);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LOW_PROFILE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            ViewUtil.setLGSystemUiVisibility(decorView, 
                    NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_DUAL_WINDOW |
                    NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_SIM_SWITCH);
        }

        /** Ignore all back events. */
        @Override
        public void onBackPressed() {
            return;
        }
    }

    private class DecryptTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            final IMountService service = getMountService();
            if (service == null) {
                return -1;
            }
            try {
                //return service.decryptStorage(params[0]);
                return service.decryptStorage(params[0].replaceAll("\\\\", "\\\\\\\\").replaceAll(
                        "\\\"", "\\\\\\\""));
            } catch (Exception e) {
                Log.e(TAG, "Error while decrypting...", e);
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer failedAttempts) {
            if (failedAttempts == 0) {
                // The password was entered successfully. Start the Blank activity
                // so this activity animates to black before the devices starts. Note
                // It has 1 second to complete the animation or it will be frozen
                // until the boot animation comes back up.
                checkLockPatternView(RIGHT_PATTERN_CLEAR_TIMEOUT_MS);
                mHotkeyInfo.hasActionReceive(true);
                mHotkeyInfo.revertQButtonClear();
                Intent intent = new Intent(CryptKeeper.this, FadeToBlack.class);
                finish();
                startActivity(intent);
            } else if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                doMaxFailedActions();
            } else {
                checkLockPatternView(WRONG_PATTERN_CLEAR_TIMEOUT_MS);
                if ((failedAttempts % COOL_DOWN_ATTEMPTS) == 0) {
                    do_showAttempts_VZW(Math.abs(failedAttempts));
                    if (isVZW) { //if this is vzw
                        doMaxFailedActionsVZW();
                    } else {
                        mCooldown = COOL_DOWN_INTERVAL;
                        cooldown();
                    }
                } else {
                    if (Math.abs(failedAttempts) == MAX_FAILED_ATTEMPTS_VZW - 1) {
                        if (isVZW) { //if this is vzw
                            do_showLimitPopup_VZW();
                        }
                    } else if (Math.abs(failedAttempts) == MAX_FAILED_ATTEMPTS - 1) {
                        do_showLimitPopup_VZW();
                    }
                    final TextView status = (TextView)findViewById(R.id.status);
                    status.setText(R.string.try_again);
                    do_showAttempts_VZW(Math.abs(failedAttempts));
                    enableInputScreen();
                }
            }
        }
    }

   private void checkLockPatternView(int delayedTime) {
       if (mLockPatternView != null) {
           mLockPatternView.removeCallbacks(mClearPatternRunnable);
           mLockPatternView.postDelayed(mClearPatternRunnable, delayedTime);
       }
   }

   private void enableInputScreen() {
       if (mLockPatternView != null) {
            mLockPatternView.setEnabled(true);
            setBackFunctionality(true);
       }
       if (mPasswordEntry != null) {
            mPasswordEntry.setEnabled(true);
            setBackFunctionality(true);
        }
   }


    private class ValidationTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            final IMountService service = getMountService();
            if (service == null) {
                return true;
            }
            try {
                Log.d(TAG, "Validating encryption state.");
                int state = service.getEncryptionState();
                if (state == IMountService.ENCRYPTION_STATE_NONE) {
                    Log.w(TAG, "Unexpectedly in CryptKeeper even though there is no encryption.");
                    return true; // Unexpected, but fine, I guess...
                }
                return state == IMountService.ENCRYPTION_STATE_OK;
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to get encryption state properly");
                return true;
            } catch (NullPointerException npe) {
                Log.w(TAG, "NullPointerException");
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mValidationComplete = true;
            if (Boolean.FALSE.equals(result)) {
                Log.w(TAG, "Incomplete, or corrupted encryption detected. Prompting user to wipe.");
                mEncryptionGoneBad = true;
            } else {
                Log.d(TAG, "Encryption state validated. Proceeding to configure UI");
            }
            setupUi();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_UPDATE_PROGRESS:
                updateProgress();
                break;

            case MESSAGE_COOLDOWN:
                cooldown();
                break;

            case MESSAGE_NOTIFY:
                notifyUser();
                break;
            default:
                break;
            }
        }
    };
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState state) {
            Log.d(TAG, "onServiceStateChanged state=" + state.getState());
            mServiceState = state;
            updateEmergencyCallButtonState();
        }
    };
    private AudioManager mAudioManager;
    /** The status bar where back/home/recent buttons are shown. */
    private static StatusBarManagerEx sStatusBar;

    /** All the widgets to disable in the status bar */
    final private static int sWidgetsToDisable = StatusBarManager.DISABLE_EXPAND
            | StatusBarManager.DISABLE_NOTIFICATION_ICONS
            | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
            | StatusBarManager.DISABLE_SYSTEM_INFO
            | StatusBarManager.DISABLE_HOME
            | StatusBarManagerConstants.DISABLE_QMEMO
            | StatusBarManagerConstants.DISABLE_NOTIFICATION
            | StatusBarManagerConstants.DISABLE_SIM_SWITCH
            | StatusBarManagerConstants.DISABLE_DUAL_WINDOW
            | StatusBarManager.DISABLE_SEARCH
            | StatusBarManager.DISABLE_RECENT;

    final private static int sNavigationBarDisable =
                             NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_NOTIFICATION
                             | NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_QMEMO
                             | NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_SIM_SWITCH
                             | NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_QSLIDE
                             | NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_DUAL_WINDOW;


    /** @return whether or not this Activity was started for debugging the UI only. */
    private boolean isDebugView() {
        return getIntent().hasExtra(EXTRA_FORCE_VIEW);
    }

    /** @return whether or not this Activity was started for debugging the specific UI view only. */
    private boolean isDebugView(String viewType /* non-nullable */) {
        return viewType.equals(getIntent().getStringExtra(EXTRA_FORCE_VIEW));
    }

    /**
     * Notify the user that we are awaiting input. Currently this sends an audio alert.
     */
    private void notifyUser() {
        if (mNotificationCountdown > 0) {
            --mNotificationCountdown;
        } else if (mAudioManager != null) {
            try {
                // Play the standard keypress sound at full volume. This should be available on
                // every device. We cannot play a ringtone here because media services aren't
                // available yet. A DTMF-style tone is too soft to be noticed, and might not exist
                // on tablet devices. The idea is to alert the user that something is needed: this
                // does not have to be pleasing.
                mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 100);
            } catch (Exception e) {
                Log.w(TAG, "notifyUser: Exception while playing sound: " + e);
            }
        }
        // Notify the user again in 5 seconds.
        mHandler.removeMessages(MESSAGE_NOTIFY);
        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY, 5 * 1000);
    }

    /**
     * Ignore back events after the user has entered the decrypt screen and while the device is
     * encrypting.
     */
    @Override
    public void onBackPressed() {
        // In the rare case that something pressed back even though we were disabled.
        if (mIgnoreBack) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // If we are not encrypted or encrypting, get out quickly.
        getTelephonyManager().listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE);
        final String state = SystemProperties.get("vold.decrypt");
        if (!isDebugView() && ("".equals(state) || DECRYPT_STATE.equals(state))) {
            // Disable the crypt keeper.
            PackageManager pm = getPackageManager();
            ComponentName name = new ComponentName(this, CryptKeeper.class);
            pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            // Typically CryptKeeper is launched as the home app.  We didn't
            // want to be running, so need to finish this activity.  We can count
            // on the activity manager re-launching the new home app upon finishing
            // this one, since this will leave the activity stack empty.
            // NOTE: This is really grungy.  I think it would be better for the
            // activity manager to explicitly launch the crypt keeper instead of
            // home in the situation where we need to decrypt the device
            Log.d(TAG, "onCreate finish");
            finish();
            return;
        }

        setStatusBar(getApplicationContext());

        //setAirplaneModeIfNecessary();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // Check for (and recover) retained instance data
        final Object lastInstance = getLastNonConfigurationInstance();
        if (lastInstance instanceof NonConfigurationInstanceState) {
            NonConfigurationInstanceState retained = (NonConfigurationInstanceState)lastInstance;
            mWakeLock = retained.wakelock;
            Log.d(TAG, "Restoring wakelock from NonConfigurationInstanceState");
        }
        mHotkeyInfo = new HotkeyInfo(getApplicationContext());
    }

    private static void setStatusBar(Context context) {
        // Disable the status bar, but do NOT disable back because the user needs a way to go
        // from keyboard settings and back to the password screen.
        if (sStatusBar == null) {
            sStatusBar = (StatusBarManagerEx)context.getSystemService(Context.STATUS_BAR_SERVICE);
            sStatusBar.disable(sWidgetsToDisable, sNavigationBarDisable);
        }
    }

    /**
     * Note, we defer the state check and screen setup to onStart() because this will be
     * re-run if the user clicks the power button (sleeping/waking the screen), and this is
     * especially important if we were to lose the wakelock for any reason.
     */
    @Override
    public void onStart() {
        super.onStart();
        setupUi();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    private boolean isSelfTestError() {
        boolean fips_error = false;
        BufferedReader in = null;
        String s = "";
        try {
            in = new BufferedReader(new FileReader("/proc/sys/crypto/fips_error"));
            s = in.readLine();
            fips_error |= s.equals("1");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fips_error |= SystemProperties.get("ro.sys.bc.selftest.status").equals("failed");
        fips_error |= SystemProperties.get("ro.sys.openssl.selftest.status").equals("failed");

        return fips_error;
    }

    /**
     * Initializes the UI based on the current state of encryption.
     * This is idempotent - calling repeatedly will simply re-initialize the UI.
     */
    private void setupUi() {
        if (mEncryptionGoneBad || isDebugView(FORCE_VIEW_ERROR)) {
            setContentView(R.layout.crypt_keeper_progress);
            showFactoryReset();
            return;
        }

        if (com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY) {
            if (isSelfTestError()) {
                setContentView(R.layout.disa_selftest_error);
                final Button button = (Button)findViewById(R.id.bt_reset);
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Factory reset the device.
                    mInfo = CRYPT_INFO_SELFTESTFAIL;
                    try {
                        mFile = new RandomAccessFile(CRYPT_INFO_PATH, "rw");
                        long len = mFile.length();
                        mFile.seek(len);
                        mFile.writeBytes(mInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (mFile != null) {
                                mFile.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                }
                });
                return;
               }
            }

        final String progress = SystemProperties.get("vold.encrypt_progress");
       if (!"".equals(progress) || isDebugView(FORCE_VIEW_PROGRESS)) {
            setContentView(R.layout.crypt_keeper_progress);
               if (!Utils.isWifiOnly(CryptKeeper.this)) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                ViewUtil.setLGSystemUiVisibility(decorView,
                    NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_DUAL_WINDOW |
                    NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_SIM_SWITCH);
            }
            encryptionProgressInit();
        } else if (mValidationComplete || isDebugView(FORCE_VIEW_PASSWORD)) {
            setLayoutByScreenType();
        } else if (!mValidationRequested) {
            new ValidationTask().execute((Void[])null);
            mValidationRequested = true;
        }
    }

    public void setLayoutByScreenType() {
            new AsyncTask<Void, Void, Void>() {
                int type = StorageManager.CRYPT_TYPE_PASSWORD;
                String owner_info;
                boolean pattern_visible;
                @Override
                public Void doInBackground(Void... v) {
                    try {
                        final IMountService service = getMountService();
                        type = service.getPasswordType();
                        owner_info = service.getField("OwnerInfo");
                        pattern_visible = !("0".equals(service.getField("PatternVisible")));
                    } catch (Exception e) {
                        Log.e(TAG, "Error calling mount service " + e);
                    }
                    return null;
                }

                @Override
                public void onPostExecute(java.lang.Void v) {
                    if (type == StorageManager.CRYPT_TYPE_PIN) {
                        setContentView(R.layout.crypt_keeper_pin_entry);
                        mStatusString = R.string.enter_pin;
                    } else if (type == StorageManager.CRYPT_TYPE_PATTERN) {
                        setContentView(R.layout.crypt_keeper_pattern_entry);
                        mStatusString = R.string.lockpattern_need_to_unlock;
                    } else {
                        setContentView(R.layout.crypt_keeper_password_entry);
                        mStatusString = R.string.enter_password;
                    }
                    final TextView status = (TextView)findViewById(R.id.status);
                    status.setText(mStatusString);
                    lockScreenEntryInit();
                    updateEmergencyCallButtonState();
                    if (mLockPatternView != null) {
                        mLockPatternView.setInStealthMode(!pattern_visible);
                    }
                    if (mCooldown > 0) {
                        setBackFunctionality(false);
                        cooldown(); // in case we are cooling down and coming back from emergency dialler
                    }
                }
            }.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPasswordEntry != null) {
            mPasswordString = mPasswordEntry.getText().toString();
        }
        mHandler.removeMessages(MESSAGE_COOLDOWN);
        mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
        mHandler.removeMessages(MESSAGE_NOTIFY);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (mCooldown > 0) {
            final TextView status = (TextView)findViewById(R.id.status);
            CharSequence template = getText(R.string.crypt_keeper_cooldown);
            status.setText(TextUtils.expandTemplate(template, Integer.toString(mCooldown)));

            mCooldown--;
            mHandler.removeMessages(MESSAGE_COOLDOWN);
            Log.d(TAG, "[cooldown()] cooldown");
            mHandler.sendEmptyMessageDelayed(MESSAGE_COOLDOWN, 1000); // Tick every second    
        }
        mHotkeyInfo.backupQButtonInfo();
        mHotkeyInfo.deleteQButtonInfo();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mHotkeyInfo.revertQButtonClear();
    }

    /**
     * Reconfiguring, so propagate the wakelock to the next instance.  This runs between onStop()
     * and onDestroy() and only if we are changing configuration (e.g. rotation).  Also clears
     * mWakeLock so the subsequent call to onDestroy does not release it.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        NonConfigurationInstanceState state = new NonConfigurationInstanceState(mWakeLock);
        Log.d(TAG, "Handing wakelock off to NonConfigurationInstanceState");
        mWakeLock = null;
        return state;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        getTelephonyManager().listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        if (mWakeLock != null) {
            Log.d(TAG, "Releasing and destroying wakelock");
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * Start encrypting the device.
     */
    private void encryptionProgressInit() {
        // Accquire a partial wakelock to prevent the device from sleeping. Note
        // we never release this wakelock as we will be restarted after the device
        // is encrypted.
        Log.d(TAG, "Encryption progress screen initializing.");
        if (mWakeLock == null) {
            Log.d(TAG, "Acquiring wakelock.");
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }

        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(true);
        // Ignore all back presses from now, both hard and soft keys.
        setBackFunctionality(false);
        // Start the first run of progress manually. This method sets up messages to occur at
        // repeated intervals.
        if ("encrypted".equals(SystemProperties.get("ro.crypto.state")) &&
            !isSupportDefaultEncryption()) {
            ((TextView)findViewById(R.id.title)).setText(R.string.crypt_keeper_setup_title_un);
        }
        updateProgress();
    }

    private void showFactoryReset() {
        // Hide the encryption-bot to make room for the "factory reset" button
        findViewById(R.id.encroid).setVisibility(View.GONE);

        // Show the reset button, failure text, and a divider
        final Button button = (Button)findViewById(R.id.factory_reset);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Factory reset the device.
                Intent factoryReset = new Intent("android.intent.action.MASTER_CLEAR");
                factoryReset.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                factoryReset.putExtra("Settings", "CryptKeeper");
                sendBroadcast(factoryReset);
            }
        });

        // Alert the user of the failure.

        final IMountService service = getMountService();
        if (service != null) {
            try {
                int state = service.getEncryptionState();
                if (state == -100) {
                    ((TextView)findViewById(R.id.title))
                            .setText(R.string.uncrypt_keeper_failed_title);
                    if (Utils.isChinaOperator()) {
                        ((TextView)findViewById(R.id.status))
                                .setText(R.string.uncrypt_keeper_failed_summary_new_cn);
                    } else {
                        ((TextView)findViewById(R.id.status))
                                .setText(R.string.uncrypt_keeper_failed_summary_new);
                    }
                } else if (state == -2 || state == -3) {
                    ((TextView)findViewById(R.id.title))
                            .setText(R.string.crypt_keeper_failed_title);
                    if (Utils.isChinaOperator()) {
                        ((TextView)findViewById(R.id.status))
                            .setText(R.string.crypt_keeper_failed_summary_cn);
                    } else {
                        ((TextView)findViewById(R.id.status))
                            .setText(R.string.crypt_keeper_failed_summary);
                    }
                }
            } catch (RemoteException e) {
                return;
            }
        }
        final View view = findViewById(R.id.bottom_divider);
        // TODO(viki): Why would the bottom divider be missing in certain layouts? Investigate.
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgress() {
        final String state = SystemProperties.get("vold.encrypt_progress");

        if ("error_partially_encrypted".equals(state)) {
            showFactoryReset();
            return;
        }

        int progress = 0;
        try {
            // Force a 50% progress state when debugging the view.
            progress = isDebugView() ? 50 : Integer.parseInt(state);
        } catch (Exception e) {
            Log.w(TAG, "Error parsing progress: " + e.toString());
        }

        //final CharSequence status = getText(R.string.crypt_keeper_setup_description);
        //[S] decrypt token
        CharSequence temp = null;
        if ("encrypted".equals(SystemProperties.get("ro.crypto.state")) 
          && !isSupportDefaultEncryption())
        {
            temp = getText(R.string.crypt_keeper_setup_description_un);
        } else {
            temp = getText(R.string.crypt_keeper_setup_description);
        }

        final CharSequence status = temp;

        Log.v(TAG, "Encryption progress: " + progress);
        final TextView tv = (TextView)findViewById(R.id.status);
        if (tv != null) {
            tv.setText(TextUtils.expandTemplate(status, Integer.toString(progress)));
        }
        // Check the progress every 5 seconds
        mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
    }

    /** Disable password input for a while to force the user to waste time between retries */
    private void cooldown() {
        final TextView status = (TextView)findViewById(R.id.status);

        if (mCooldown <= 0) {
            // Re-enable the password entry and back presses.
            enableInputScreen();
            status.setText(mStatusString);
            lockScreenEntryInit();
        } else {
            if (mPasswordEntry != null) {
                mPasswordEntry.setEnabled(false);
            }

            if (mLockPatternView != null) {
                mLockPatternView.setEnabled(false);
            }

            CharSequence template = getText(R.string.crypt_keeper_cooldown);
            status.setText(TextUtils.expandTemplate(template, Integer.toString(mCooldown)));

            mCooldown--;
            mHandler.removeMessages(MESSAGE_COOLDOWN);
            mHandler.sendEmptyMessageDelayed(MESSAGE_COOLDOWN, 1000); // Tick every second
        }
    }

    /**
     * Sets the back status: enabled or disabled according to the parameter.
     * @param isEnabled true if back is enabled, false otherwise.
     */
    private final void setBackFunctionality(boolean isEnabled) {
        mIgnoreBack = !isEnabled;
        if (isEnabled) {
            sStatusBar.disable(sWidgetsToDisable);
        } else {
            sStatusBar.disable(sWidgetsToDisable | StatusBarManager.DISABLE_BACK);
        }
    }

    protected OnPatternListener mChooseNewLockPatternListener = new OnPatternListener() {

        public void onPatternStart() {
            delayAudioNotification();
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

       public void onPatternCleared() {
           mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        @Override
        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            mLockPatternView.setEnabled(false);
            new DecryptTask().execute(LockPatternUtils.patternToString(pattern));
        }

        @Override
        public void onPatternCellAdded(List<Cell> pattern) {
        }
     };

    private void lockScreenEntryInit() {
        // pin & password case
        mPasswordEntry = (EditText)findViewById(R.id.passwordEntry);
        if (mPasswordEntry != null) {
            if (mCooldown > 0 && mCooldown < COOL_DOWN_INTERVAL) {
                mPasswordEntry.setEnabled(false);
            } else {
                mPasswordEntry.setOnEditorActionListener(this);
                mPasswordEntry.requestFocus();
                mPasswordEntry.setOnKeyListener(this);
                mPasswordEntry.setOnTouchListener(this);
                mPasswordEntry.addTextChangedListener(this);
                if (mPasswordString != null) {
                    mPasswordEntry.setText(mPasswordString);
                    mPasswordEntry.setSelection(mPasswordString.length());
                }
            }
            if (StorageManager.CRYPT_TYPE_PASSWORD == StorageManager.CRYPT_TYPE_PIN) {
                mPasswordEntry.setRawInputType(InputType.TYPE_CLASS_NUMBER);
             }
           }
        // Pattern case
        if (isSupportDefaultEncryption()) {
            mLockPatternView = (LgeLockPatternViewBase)findViewById(R.id.lockPattern);
            if (mLockPatternView != null) {
                mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
            }
        }
         keepScreenOnWhileInput();
            // Asynchronously throw up the IME, since there are issues with requesting it to be shown
            // immediately.
        if (getLockScreenType() != PATTERN_PASSWORD_TYPE) {
            final View imeSwitcher = findViewById(R.id.switch_ime_button);
            final InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imeSwitcher != null && hasMultipleEnabledIMEsOrSubtypes(imm, false)) {
                imeSwitcher.setVisibility(View.VISIBLE);
                imeSwitcher.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imm.showInputMethodPicker();
                    }
                });
            }
            if (mCooldown <= 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override public void run() {
                        imm.showSoftInputUnchecked(0, null);
                    }
                }, 0);
            }
        }
        checkshowCountInAttempt();
        // Notify the user in 120 seconds that we are waiting for him to enter the password.
        mHandler.removeMessages(MESSAGE_NOTIFY);
        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY, 120 * 1000);
    }

    private void keepScreenOnWhileInput() {
         // We want to keep the screen on while waiting for input. In minimal boot mode, the device
         // is completely non-functional, and we want the user to notice the device and enter a
         // password.
        if (mWakeLock == null) {
            Log.d(TAG, "Acquiring wakelock.");
            final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                if (isVZW) {
                    mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
                    mWakeLock.acquire(WAKE_LOCK_SECONDS * 1000);
                } else {
                    if (mWakeLock == null) {
                        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
                        mWakeLock.acquire();
                    }
                }
            }
        }
    }

    private void checkshowCountInAttempt() {       
        final String failed_count = SystemProperties.get("vold.failed_decrypt_count");
        if (failed_count == null || failed_count.equals("") || failed_count.equals("0")) {
            do_showAttempts_VZW(0);
        } else {
            do_showAttempts_VZW(Integer.parseInt(failed_count));
        }
    }

    private void do_showAttempts_VZW(int failed) {
        final TextView mAttemptsDisplay = (TextView)findViewById(R.id.attempts);
        if (isVZW) {
            mAttemptsDisplay.setText(getResources().getString(R.string.sp_remained_attempts_NORMAL,
                    MAX_FAILED_ATTEMPTS_VZW - failed, MAX_FAILED_ATTEMPTS_VZW));
        } else {
            mAttemptsDisplay.setText(getResources().getString(R.string.sp_remained_attempts_NORMAL,
                    MAX_FAILED_ATTEMPTS - failed, MAX_FAILED_ATTEMPTS));
        }
    }

    /**
     * Method adapted from com.android.inputmethod.latin.Utils
     *
     * @param imm The input method manager
     * @param shouldIncludeAuxiliarySubtypes
     * @return true if we have multiple IMEs to choose from
     */
    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm,
            final boolean shouldIncludeAuxiliarySubtypes) {
        final List<InputMethodInfo> enabledImis = imm.getEnabledInputMethodList();

        // Number of the filtered IMEs
        int filteredImisCount = 0;

        for (InputMethodInfo imi : enabledImis) {
            // We can return true immediately after we find two or more filtered IMEs.
            if (filteredImisCount > 1) {
                return true;
            }
            final List<InputMethodSubtype> subtypes =
                    imm.getEnabledInputMethodSubtypeList(imi, true);
            // IMEs that have no subtypes should be counted.
            if (subtypes.isEmpty()) {
                ++filteredImisCount;
                continue;
            }

            int auxCount = 0;
            for (InputMethodSubtype subtype : subtypes) {
                if (subtype.isAuxiliary()) {
                    ++auxCount;
                }
            }
            final int nonAuxCount = subtypes.size() - auxCount;

            // IMEs that have one or more non-auxiliary subtypes should be counted.
            // If shouldIncludeAuxiliarySubtypes is true, IMEs that have two or more auxiliary
            // subtypes should be counted as well.
            if (nonAuxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                ++filteredImisCount;
                continue;
            }
        }

        return filteredImisCount > 1
                // imm.getEnabledInputMethodSubtypeList(null, false) will return the current IME's enabled
                // input method subtype (The current IME should be LatinIME.)
                || imm.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    private IMountService getMountService() {
        final IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
            // Get the password
            final String password = v.getText().toString();

            if (TextUtils.isEmpty(password)) {
                return true;
            }

            // Now that we have the password clear the password field.
            v.setText(null);

            // Disable the password entry and back keypress while checking the password. These
            // we either be re-enabled if the password was wrong or after the cooldown period.
            mPasswordEntry.setEnabled(false);
            setBackFunctionality(false);

            Log.d(TAG, "Attempting to send command to decrypt");
            new DecryptTask().execute(password);

            return true;
        }
        return false;
    }

    /**
     * Set airplane mode on the device if it isn't an LTE device.
     * Full story: In minimal boot mode, we cannot save any state. In particular, we cannot save
     * any incoming SMS's. So SMSs that are received here will be silently dropped to the floor.
     * That is bad. Also, we cannot receive any telephone calls in this state. So to avoid
     * both these problems, we turn the radio off. However, on certain networks turning on and
     * off the radio takes a long time. In such cases, we are better off leaving the radio
     * running so the latency of an E911 call is short.
     * The behavior after this is:
     * 1. Emergency dialing: the emergency dialer has logic to force the device out of
     *    airplane mode and restart the radio.
     * 2. Full boot: we read the persistent settings from the previous boot and restore the
     *    radio to whatever it was before it restarted. This also happens when rebooting a
     *    phone that has no encryption.
     */
    /*
    private final void setAirplaneModeIfNecessary() {
        final boolean isLteDevice =
                TelephonyManager.getDefault().getLteOnCdmaMode() == Phone.LTE_ON_CDMA_TRUE;
        if (!isLteDevice) {
            Log.d(TAG, "Going into airplane mode.");
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            final Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", true);
            sendBroadcast(intent);
        }
    }
    */

    /**
     * Code to update the state of, and handle clicks from, the "Emergency call" button.
     *
     * This code is mostly duplicated from the corresponding code in
     * LockPatternUtils and LockPatternKeyguardView under frameworks/base.
     */
    private void updateEmergencyCallButtonState() {
        final Button emergencyCall = (Button)findViewById(R.id.emergencyCallButton);
        // The button isn't present at all in some configurations.
        if (emergencyCall == null) {
            return;
        }
        if (mServiceState == null) {
            return;
        }
        if ("SBM".equals(Config.getOperator())) {
            emergencyCall.setVisibility(View.GONE);
            return;
        }

        if (isEmergencyCallCapable()) {
            if ("AU".equals(Config.getCountry())
                    && mServiceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                emergencyCall.setText(R.string.sp_network_noservice_NORMAL);
                return;
            }
            if (getTelecomManager().isInCall()) {
                emergencyCall.setText(R.string.cryptkeeper_return_to_call);
            }
            emergencyCall.setVisibility(View.VISIBLE);
            emergencyCall.setEnabled(true);
            emergencyCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeEmergencyCallAction();
                }
            });
        } else {
            emergencyCall.setVisibility(View.GONE);
            return;
        }
    }

    private boolean isEmergencyCallCapable() {
        return getTelephonyManager().isVoiceCapable();
    }

    private void takeEmergencyCallAction() {
        TelecomManager telecomManager = getTelecomManager();
        if (telecomManager.isInCall()) {
            telecomManager.showInCallScreen(false /* showDialpad */);
        } else {
            launchEmergencyDialer();
        }
    }


    private void launchEmergencyDialer() {
        final Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        setBackFunctionality(true);
        startActivity(intent);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    }

    private TelecomManager getTelecomManager() {
        return (TelecomManager)getSystemService(Context.TELECOM_SERVICE);
    }

    /**
     * Listen to key events so we can disable sounds when we get a keyinput in EditText.
     */
    private void delayAudioNotification() {
        mNotificationCountdown = 20;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        delayAudioNotification();
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        delayAudioNotification();
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        return;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        delayAudioNotification();
    }

    @Override
    public void afterTextChanged(Editable s) {
        return;
    }

    private void do_showLimitPopup_VZW() {
    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169] Disallow Wipe Data(SW Factory Reset)
        if (MDMSettingsAdapter.getInstance().getDiallowedFactoryReset()) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cryptkeeper_final_attempts_NORMAL)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.cryptkeeper_final_attempts_msg_lgmdm_NORMAL)
                .setPositiveButton(getResources().getString(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        })
                .create()
                .show();
        } else {
        int mPopupString = R.string.cryptkeeper_final_attempts_msg_NORMAL;
        if (getLockScreenType() == PATTERN_PASSWORD_TYPE) {
            mPopupString = R.string.cryptkeeper_final_attempts_msg_pattern;
        } else if (getLockScreenType() == PIN_PASSWORD_TYPE) {
            mPopupString = R.string.cryptkeeper_final_attempts_msg_pin_NORMAL;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.cryptkeeper_final_attempts_NORMAL)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(mPopupString)
                .setPositiveButton(getResources().getString(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        })
                .create()
                .show();
        }
    }

    private void doMaxFailedActions() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169] Disallow Wipe Data(SW Factory Reset)
        boolean lgmdmDisAllowWipeData =
                    MDMSettingsAdapter.getInstance().getDiallowedFactoryReset();
            if (lgmdmDisAllowWipeData) {
                Log.w(TAG, "LGMDM do reboot system");
                PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                try {
                    pm.reboot("boot");
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
            // LGMDM_END
            // Factory reset the device.
                Intent resetMaxFailed = new Intent("android.intent.action.MASTER_CLEAR");
                resetMaxFailed.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                sendBroadcast(resetMaxFailed);
            }
    }

    private void doMaxFailedActionsVZW() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169] Disallow Wipe Data(SW Factory Reset)
        boolean lgmdmDisAllowWipeData =
                    MDMSettingsAdapter.getInstance().getDiallowedFactoryReset();
        if (lgmdmDisAllowWipeData) {
            Log.w(TAG, "LGMDM do reboot system");
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            try {
                pm.reboot("boot");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // LGMDM_END
            LGMDMManager.getInstance().wipeData(2);
        }
    }

    private boolean isSupportDefaultEncryption() {
        return Config.getFWConfigBool(getApplicationContext(), com.lge.R.bool.config_default_encrypt,
                                   "com.lge.R.bool.config_default_encrypt");
    }

    private boolean isSupportPhoneLock() {
        boolean isPhoneLockEnable = Settings.Global.getInt(getApplicationContext().getContentResolver(),
            SettingsConstants.Global.SECURITY_PHONE_LOCK, 0) == 1 ? true : false;
        return isPhoneLockEnable;
    }

    private int getLockScreenType() {
        try {
            final IMountService service = getMountService();
            switch(service.getPasswordType()) {
                case StorageManager.CRYPT_TYPE_PATTERN:
                    return PATTERN_PASSWORD_TYPE;
                case StorageManager.CRYPT_TYPE_PIN:
                    return PIN_PASSWORD_TYPE;
                case StorageManager.CRYPT_TYPE_PASSWORD:
                    return CHAR_PASSWORD_TYPE;
                default:
                    return DEFAULT_PASSWORD_TYPE;
            }
         } catch (RemoteException e) {
             Log.w(TAG, "failed taking mount service");
             return DEFAULT_PASSWORD_TYPE;
         }
    }
}
