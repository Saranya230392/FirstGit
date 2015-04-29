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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFrameLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;

public class UnCryptKeeperSettings extends Fragment {

    private static final int KEYGUARD_REQUEST = 55;
    //[S][2011.02.02][jin850607.hong@lge.com] disable menu because encryption
    private static final int REQ_CODE = 1;
    private static final String SET_ENCRYPTION_REQUEST = "encryption_confirm";
    //[E][2011.02.02][jin850607.hong@lge.com] disable menu because encryption
    // This is the minimum acceptable password quality.  If the current password quality is
    // lower than this, encryption should not be activated.
    public static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;

    // Minimum battery charge level (in percent) to launch encryption.  If the battery charge is
    // lower than this, encryption should not be activated.
    private static final int MIN_BATTERY_LEVEL = 80;

    private View mContentView;
    private Button mInitiateButton;
    private TextView mPowerWarning;
    private TextView mBatteryWarning;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                final int invalidCharger = intent.getIntExtra(
                        BatteryManager.EXTRA_INVALID_CHARGER, 0);

                final boolean levelOk = level >= MIN_BATTERY_LEVEL;
                final boolean pluggedOk =
                        ((plugged & BatteryManager.BATTERY_PLUGGED_ANY) != 0) &&
                                invalidCharger == 0;

                // Update UI elements based on power/battery status
                mInitiateButton.setEnabled(levelOk && pluggedOk);
                mPowerWarning.setVisibility(pluggedOk ? View.GONE : View.VISIBLE);
                mBatteryWarning.setVisibility(levelOk ? View.GONE : View.VISIBLE);

                mBatteryWarning.setText(getString(R.string.sp_unencryption_low_charge_text_change,
                        level));
                mPowerWarning.setText(R.string.battery_power_decrypt_change);

                if (Config.VZW.equals(Config.getOperator())) {
                    if (!pluggedOk && !levelOk) {
                        mPowerWarning
                                .setText(getString(R.string.decryption_not_battery_plug, level));
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                } else {
                    if (!pluggedOk && levelOk) {
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                    else if (!pluggedOk && !levelOk) {
                        mPowerWarning
                                .setText(getString(R.string.decryption_not_battery_plug, level));
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                    else {
                        mPowerWarning.setText(R.string.battery_power_decrypt_change);
                    }
                }
            }
        }
    };

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we prompt the user to set a password.
     */
    private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {

        //[S][2011.02.02][jin850607.hong@lge.com] disable menu because encryption
        public void onClick(View v) {
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                // TODO replace (or follow) this dialog with an explicit launch into password UI
            	AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.crypt_keeper_dialog_need_password_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setMessage(R.string.crypt_keeper_dialog_need_password_message)
                        .setPositiveButton(getResources().getString(R.string.dlg_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub

                                        Intent intent = new Intent(getActivity(),
                                                ChooseLockGeneric.class);
                                        intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
                                        startActivityForResult(intent, REQ_CODE);
                                        /*startFragment("com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment",
                                        SET_OR_CHANGE_LOCK_METHOD_REQUEST, null);*/
                                        dialog.dismiss();
                                    }
                        })
                       .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        dialog.dismiss();
                                        //getActivity().finish();
                                    }
                        }).create();
                         if (!Utils.isUI_4_1_model(getActivity())) {
                        	 dialog.setIconAttribute(android.R.attr.alertDialogIcon);
                         }
                        dialog.show();
            }
        }
    };

    //[E][2011.02.02][jin850607.hong@lge.com] disable menu because encryption

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        if (getActivity() != null) {
            if (!Utils.supportSplitView(getActivity())) {
                getActivity().getActionBar().setIcon(R.drawable.shortcut_security);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                getActivity().getWindow()
                        .addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addEncryptionSettingChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
        mContentView = inflater.inflate(R.layout.uncrypt_keeper_settings, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)mContentView.getLayoutParams()).removeBorders = true;
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        mInitiateButton = (Button)mContentView.findViewById(R.id.initiate_encrypt);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mInitiateButton.setEnabled(false);

        mPowerWarning = (TextView)mContentView.findViewById(R.id.warning_unplugged);
        mBatteryWarning = (TextView)mContentView.findViewById(R.id.warning_low_charge);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                android.util.Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
    }

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
        LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
        int quality = lockPatternUtils.getActivePasswordQuality();
        if (quality == DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK
                && lockPatternUtils.isLockPasswordEnabled()) {
            // Use the alternate as the quality. We expect this to be
            // PASSWORD_QUALITY_SOMETHING(pattern) or PASSWORD_QUALITY_NUMERIC(PIN).
            quality = lockPatternUtils.getKeyguardStoredPasswordQuality();
        }
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

        if (requestCode != KEYGUARD_REQUEST && requestCode != REQ_CODE) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK && data != null) {
            String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            if (!TextUtils.isEmpty(password)) {
                showFinalConfirmation(password);
            } else {
                getActivity().finish();
            }
        }
    }

    private void showFinalConfirmation(String password) {
        Preference preference = new Preference(getActivity());
        preference.setFragment(UnCryptKeeperConfirm.class.getName());
        preference.setTitle(R.string.sp_security_unencrypt_button_NORMAL);
        preference.getExtras().putString("password", password);
        ((PreferenceActivity)getActivity()).onPreferenceStartFragment(null, preference);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance()
                        .receiveSecurityEncryptionChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };
    // LGMDM_END
}
