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

import android.app.Activity;
import android.app.Fragment;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.preference.ListPreference;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceFrameLayout;

import com.android.settings.lge.RadioButtonPreference;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGDevEncManager;
import com.lge.view.ViewUtil;
import com.lge.constants.NavigationButtonConstants;
import com.lge.constants.ViewConstants;
import com.lge.constants.StatusBarManagerConstants;

import android.media.AudioManager;
import android.media.AudioManagerEx;
import android.widget.Toast;

public class CryptKeeperConfirm extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {
    //[S][2012.02.22][jin850607.hong@lge.com] battery & usb connection check
    private static final int MIN_BATTERY_LEVEL = 80;
    private IntentFilter mIntentFilter;

    private RadioButtonPreference mQuickEncryption;
    private RadioButtonPreference mFullEncryption;
    //private ConfirmEncryptionSummaryDescriptionPreference mConfirmEncryptionSummaryDescriptionPreference;

    private LGContext mServiceContext = null;
    private static LGDevEncManager mLGDevEncManager = null;
    private AudioManager mAudioManager;

    private static final int QUICK_CHECKED = 1;
    private static final int FULL_CHECKED = 2;

    private static final String KEY_QUICK_ENCRYPTION =
            "quick_encryption_radio_button";
    private static final String KEY_FULL_ENCRYPTION =
            "full_encryption_radio_button";
    private CustomDialogToast cdt = null;
	private ListView mListView;
    /*
    private static final String KEY_CONFIRM_ENCRYPTION =
            "confirm_encryption_summary_description";
    */

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                int invalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                boolean levelOk = level >= MIN_BATTERY_LEVEL;
                boolean pluggedOk =
                        ((plugged & BatteryManager.BATTERY_PLUGGED_ANY) != 0) &&
                                invalidCharger == 0;

                // Update UI elements based on power/battery status
                mApplyButton.setEnabled(levelOk && pluggedOk);
            }
        }
    };

    //[E][2012.02.22][jin850607.hong@lge.com] battery & usb connection check
    public static class Blank extends Activity {
        private Handler mHandler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.crypt_keeper_blank);

            View layoutRoot = findViewById(R.id.layout_root);
            if (layoutRoot != null) {
                layoutRoot.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                                View.SYSTEM_UI_FLAG_IMMERSIVE |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                
                // yonguk.kim Apply changed Navigation API
                ViewUtil.setLGSystemUiVisibility(layoutRoot, 
                		NavigationButtonConstants.NAVIGATION_BUTTON_DISABLE_DUAL_WINDOW);
            }
            if (Utils.isMonkeyRunning()) {
                finish();
            }
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                    getWindow().addFlags(
                            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                }
            }
            // LGMDM_END

            StatusBarManager sbm = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
            sbm.disable(StatusBarManager.DISABLE_EXPAND
                    | StatusBarManager.DISABLE_NOTIFICATION_ICONS
                    | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                    | StatusBarManager.DISABLE_SYSTEM_INFO
                    | StatusBarManager.DISABLE_HOME
                    | StatusBarManagerConstants.DISABLE_QMEMO
                    | StatusBarManagerConstants.DISABLE_NOTIFICATION
                    | StatusBarManagerConstants.DISABLE_SIM_SWITCH
                    | StatusBarManagerConstants.DISABLE_DUAL_WINDOW
                    | StatusBarManager.DISABLE_SEARCH
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
                            if (args.getInt("checkedValue") == QUICK_CHECKED) {
                                /*mLGDevEncManager.quicklyencryptStorage
                                        (args.getString("password")
                                                .replaceAll("\\\\", "\\\\\\\\")
                                                .replaceAll("\\\"", "\\\\\\\""));*/

                              mountService.encryptStorage(args.getInt("type", -1), args.getString("password")
                                        .replaceAll("\\\\", "\\\\\\\\")
                                        .replaceAll("\\\"", "\\\\\\\""));
                            } else if (args.getInt("checkedValue") ==
                            FULL_CHECKED) {

                                mLGDevEncManager.quicklyencryptStorage
                                    (args.getInt("type", -1), args.getString("password")
                                     .replaceAll("\\\\", "\\\\\\\\")
                                     .replaceAll("\\\"", "\\\\\\\""));
                            }
                            //mountService.encryptStorage(args.getString("password"));
                        } else {
                            Log.d("hkk", "encryptStorage no arg");
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

    private View mContentView;
    private Button mBackButton;
    private Button mApplyButton;
    private Button.OnClickListener mBackClickListener =
            new Button.OnClickListener() {
                public void onClick(View v) {
                    if (Utils.isMonkeyRunning()) {
                        return;
                    }
                    getActivity().onBackPressed();
                }
            };

    private Button.OnClickListener mApplyClickListener =
            new Button.OnClickListener() {
                public void onClick(View v) {
                    if (Utils.isMonkeyRunning()) {
                        return;
                    }

                    if (mAudioManager.getMode() == AudioManagerEx.MODE_IN_CALL ||
                            mAudioManager.getMode() == AudioManagerEx.MODE_IN_COMMUNICATION) {
                        if (cdt == null) {
                            cdt = new CustomDialogToast(getActivity().getApplicationContext(),
                                  getResources().getString(R.string.sp_encryption_not_available_during_call), Toast.LENGTH_SHORT);
                        }
                        cdt.show();
                        return;
                    }
                    Intent intent = new Intent(getActivity(), Blank.class);
                    intent.putExtras(getArguments());
                    intent.putExtra("checkedValue", getCheckedPreference());
                    startActivity(intent);
                }
            };

    private void establishFinalConfirmationState() {
        mBackButton = (Button)mContentView.
                findViewById(R.id.encryption_settings_confirm_preference_back);
        mApplyButton = (Button)mContentView.
                findViewById(R.id.encryption_settings_confirm_preference_apply);
        mBackButton.setOnClickListener(mBackClickListener);
        mApplyButton.setOnClickListener(mApplyClickListener);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.encryption_settings_confirm_preference);
        //mConfirmEncryptionSummaryDescriptionPreference =
        //        (ConfirmEncryptionSummaryDescriptionPreference)
        //        findPreference(KEY_CONFIRM_ENCRYPTION);
        mQuickEncryption = (RadioButtonPreference)
                findPreference(KEY_QUICK_ENCRYPTION);
        mQuickEncryption.setOnPreferenceChangeListener(this);
        mQuickEncryption.setChecked(true);
        setCheckedPreference(QUICK_CHECKED);
        mFullEncryption = (RadioButtonPreference)
                findPreference(KEY_FULL_ENCRYPTION);
        mFullEncryption.setOnPreferenceChangeListener(this);

        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (getActivity() != null && !Utils.supportSplitView(getActivity())) {
            getActivity().getActionBar().setIcon(
                    getResources().getDrawable(R.drawable.shortcut_security));
        }
        if (LGDevEncManager.getDevEncSupportStatus(getActivity().getApplicationContext())) {
            mServiceContext = new LGContext(getActivity().getApplicationContext());
            mLGDevEncManager = (LGDevEncManager)mServiceContext.
                    getLGSystemService(LGContext.LGDEVENC_SERVICE);
        }

        //[S][2012.02.22][jin850607.hong@lge.com] battery & usb connection check
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        //[E][2012.02.22][jin850607.hong@lge.com] battery & usb connection check

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                getActivity().getWindow().addFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        }
        // LGMDM_END
        mContentView = inflater.inflate
                (R.layout.encryption_settings_confirm_preference_layout, container, false);
        if (mContentView != null) {
            mListView = (ListView)mContentView.findViewById(android.R.id.list);
		}
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)mContentView.getLayoutParams()).removeBorders = true;
        }
        establishFinalConfirmationState();

        if (mListView != null) {
             mListView.setFooterDividersEnabled(false);
             mListView.setItemsCanFocus(true);
       }
        return mContentView;
    }

    //[S][2012.02.22][jin850607.hong@lge.com] battery & usb connection check
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
    }

    //[E][2012.02.22][jin850607.hong@lge.com] battery & usb connection check

    private void initToggles() {
        mQuickEncryption.setChecked(false);
        mFullEncryption.setChecked(false);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        initToggles();
        if (preference == mQuickEncryption) {
            mQuickEncryption.setChecked(true);
            setCheckedPreference(QUICK_CHECKED);
            return true;
        } else if (preference == mFullEncryption) {
            mFullEncryption.setChecked(true);
            setCheckedPreference(FULL_CHECKED);
            return true;
        } else {
        }
        return true;
    }

    public int mChecked;

    public void setCheckedPreference(int mButton) {
        mChecked = mButton;
    }

    public int getCheckedPreference() {
        return mChecked;
    }
}
