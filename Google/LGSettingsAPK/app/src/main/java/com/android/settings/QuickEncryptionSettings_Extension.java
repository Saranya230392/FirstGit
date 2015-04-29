/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Environment;
import java.io.File;
import android.widget.ImageView;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageResultCode;
import android.os.IBinder;

import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;
import java.io.FileWriter;
import java.io.BufferedWriter;
import android.app.ProgressDialog;
import android.os.Handler;
import android.widget.Toast;
import android.view.WindowManager;

import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGDevEncManager;

import android.os.Message;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
//FEATURE_SDCARD_PERCENT[S]
import android.text.TextUtils;
//FEATURE_SDCARD_PERCENT[E]
import android.content.res.Configuration;
import android.widget.LinearLayout;
import android.os.storage.StorageEventListener;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.app.StatusBarManager;

public class QuickEncryptionSettings_Extension extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String QUICK_ENCRYPT = "quick_radio_button";
    private static final String FULL_ENCRYPT = "full_radio_button";

    // This is the minimum acceptable password quality.  If the current password quality is
    // lower than this, encryption should not be activated.
    static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;

    // Minimum battery charge level (in percent) to launch encryption.  If the battery charge is
    // lower than this, encryption should not be activated.
    private static final int MIN_BATTERY_LEVEL = 40;

    private View mContentView;
    private ImageView mLineImage;
    private Button mApplyButton;
    private Button mBackButton;
    private IntentFilter mIntentFilter;
    private TextView encryptionText2;
    private View mPowerWarning;
    private View mBatteryWarning;
    //private PreferenceCategory settings1Category;
    //private PreferenceCategory settings2Category;

    private RadioButtonPreference mQuickEncrypt;
    private RadioButtonPreference mFullEncrypt;
    private static LGContext mServiceContext = null;
    private static LGDevEncManager mLGDevEncManager = null;

    IMountService mMountService;

    private boolean levelOk;
    private boolean pluggedOk;
    private LayoutInflater minflater;

    private static boolean sQuickChecked = true;
    private static boolean sFullChecked = false;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                int invalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                levelOk = level >= MIN_BATTERY_LEVEL;
                pluggedOk =
                        (plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                                plugged == BatteryManager.BATTERY_PLUGGED_USB) &&
                                invalidCharger == 0;

                mApplyButton.setEnabled(levelOk && pluggedOk);
                mPowerWarning.setVisibility(pluggedOk ? View.GONE : View.VISIBLE);
                mBatteryWarning.setVisibility(levelOk ? View.GONE : View.VISIBLE);

            }
        }
    };

    public static class Blank extends Activity {
        private Handler mHandler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.uncrypt_keeper_blank);

            if (Utils.isMonkeyRunning()) {
                finish();
            }

            //CAPP_MDM [a1-mdm-dev@lge.com] add encryption API related to get()
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                    getWindow().addFlags(
                            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                }
            }
            //CAPP_MDM_END

            StatusBarManager sbm = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
            sbm.disable(StatusBarManager.DISABLE_EXPAND
                    | StatusBarManager.DISABLE_NOTIFICATION_ICONS
                    | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                    | StatusBarManager.DISABLE_SYSTEM_INFO
                    | StatusBarManager.DISABLE_HOME
                    | StatusBarManager.DISABLE_RECENT
                    | StatusBarManager.DISABLE_BACK);

            // Post a delayed message in 700 milliseconds to enable encryption.
            // NOTE: The animation on this activity is set for 500 milliseconds
            // I am giving it a little extra time to complete.
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    IBinder service = ServiceManager.getService("mount");
                    if (service == null) {
                        Log.e("CryptKeeper", "Failed to find the mount service");
                        finish();
                        return;
                    }

                    IMountService mountService = IMountService.Stub.asInterface(service);
                    try {
                        Bundle args = getIntent().getExtras();
                        if (args != null) {
                            if (sQuickChecked) {
                                mountService.encryptStorage(args.getInt("type", -1), args.getString("password")
                                        .replaceAll("\\\\", "\\\\\\\\")
                                        .replaceAll("\\\"", "\\\\\\\""));
                            } else  {
                                mLGDevEncManager.quicklyencryptStorage
                                    (args.getInt("type", -1), args.getString("password")
                                     .replaceAll("\\\\", "\\\\\\\\")
                                     .replaceAll("\\\"", "\\\\\\\""));
                            }
                        } else {
                            Log.e("UnCryptKeeper", "There are no args");
                            finish();
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("CryptKeeper", "Error while encrypting...", e);
                        finish();
                    }
                }
            }, 700);
        }
    }

    private Button.OnClickListener mApplyListener = new Button.OnClickListener() {

        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), Blank.class);
            intent.putExtras(getArguments());

            startActivity(intent);
        }
    };

    private Button.OnClickListener mBackListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Utils.isWifiOnly(getActivity())) {
                new Thread(new Runnable() {
                    public void run() {
                        new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                    }
                }).start();
            } else {
                getActivity().finish();
            }
        }
    };

    private synchronized IMountService getMountService() {
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            }
        }
        return mMountService;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (getActivity() != null) {
            getActivity().setTitle(
                    getResources().getString(R.string.sp_storage_encryption_sdcard_title_NORMAL));
        }
    }

    private void initializeAllPreferences(boolean mCreateRequest) {
        addPreferencesFromResource(R.layout.quick_encryption_settings_extension_preference);

        //settings1Category = (PreferenceCategory)findPreference(KEY_SETTINGS1_INFO_CATEGORY);
        mQuickEncrypt = (RadioButtonPreference)findPreference(QUICK_ENCRYPT);
        mQuickEncrypt.setOnPreferenceChangeListener(this);
        mFullEncrypt = (RadioButtonPreference)findPreference(FULL_ENCRYPT);
        mFullEncrypt.setOnPreferenceChangeListener(this);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        if (LGDevEncManager.getDevEncSupportStatus(getActivity().getApplicationContext())) {
            mServiceContext = new LGContext(getActivity().getApplicationContext());
            mLGDevEncManager = (LGDevEncManager)mServiceContext.
                    getLGSystemService(LGContext.LGDEVENC_SERVICE);
        }

        if (getActivity() != null) {
            if (!Utils.supportSplitView(getActivity())) {
                getActivity().getActionBar().setIcon(R.drawable.shortcut_security);
            }
            getActivity().setTitle(
                    getResources().getString(R.string.sp_storage_encryption_sdcard_title_NORMAL));
        }

        minflater = inflater;

        mContentView = inflater.inflate(R.layout.quick_encryption_settings_extension_linear, null);

        setLayoutParam(minflater);

        mApplyButton.setEnabled(false);
        return mContentView;
    }

    private void createPreferenceHierarchy(boolean mCreateRequest, boolean mQuickenable,
            boolean mFullenable) {
        if (mCreateRequest == false) {
            getPreferenceScreen().removeAll();
            initializeAllPreferences(mCreateRequest);
        }
        mQuickEncrypt.setChecked(mQuickenable);
        mFullEncrypt.setChecked(mFullenable);

    }

    private void setLayoutParam(LayoutInflater inflater) {
        initializeAllPreferences(true);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        mLineImage = (ImageView)mContentView.findViewById(R.id.encryption_settingLine_Extension);
        encryptionText2 = (TextView)mContentView
                .findViewById(R.id.encryption_settingText2_Extension);

        mApplyButton = (Button)mContentView.findViewById(R.id.sdencrypt_settings_apply);
        mApplyButton.setOnClickListener(mApplyListener);

        mBackButton = (Button)mContentView.findViewById(R.id.sdencrypt_settings_back);
        mBackButton.setOnClickListener(mBackListener);

        mLineImage.setVisibility(View.VISIBLE);
        encryptionText2.setVisibility(View.VISIBLE);

        mApplyButton.setVisibility(View.VISIBLE);
        mBackButton.setVisibility(View.VISIBLE);

        mPowerWarning = mContentView.findViewById(R.id.warning_unplugged);
        mBatteryWarning = mContentView.findViewById(R.id.warning_low_charge);

        mQuickEncrypt.setChecked(true);

    }

    //[E][2013.01.31][TD:281676][swgi.kim@lge.com] displaying error when opientation landsacape		

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (SecuritySettings.getFullMediaExceptionOption() == SecuritySettings.MEDIA_CASE) {
            return true;
        }

        if (preference == mQuickEncrypt) {
            sQuickChecked = true;
            sFullChecked = false;
            createPreferenceHierarchy(false, true, false);
        } else {
            sFullChecked = true;
            sQuickChecked = false;
            createPreferenceHierarchy(false, false, true);
        }

        mApplyButton.setEnabled(levelOk && pluggedOk);
        mPowerWarning.setVisibility(pluggedOk ? View.GONE : View.VISIBLE);
        mBatteryWarning.setVisibility(levelOk ? View.GONE : View.VISIBLE);

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        createPreferenceHierarchy(false, sQuickChecked, sFullChecked);
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
    }

    /**
     * If encryption is already started, and this launched via a "start encryption" intent,
     * then exit immediately - it's already up and running, so there's no point in "starting" it.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        if (DevicePolicyManager.ACTION_START_ENCRYPTION.equals(intent.getAction())) {
            DevicePolicyManager dpm = (DevicePolicyManager)
                    activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                int status = dpm.getStorageEncryptionStatus();
                if (status != DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE) {
                    // There is nothing to do here, so simply finish() (which returns to caller)
                    activity.finish();
                }
            }
        }
    }

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        // 1.  Confirm that we have a sufficient PIN/Password to continue
        int quality = new LockPatternUtils(getActivity()).getActivePasswordQuality();
        if (quality < MIN_PASSWORD_QUALITY) {
            return false;
        }
        // 2.  Ask the user to confirm the current PIN/Password
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this)
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
