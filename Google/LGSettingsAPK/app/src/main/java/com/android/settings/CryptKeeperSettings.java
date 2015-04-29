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
//[S][2011.02.02][jin850607.hong@lge.com] disable menu because encryption
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//[E][2011.02.02][jin850607.hong@lge.com] disable menu because encryption
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtilsEx;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;
import android.preference.PreferenceFrameLayout;

import com.android.settings.lgesetting.Config.Config;
import android.util.Log;

public class CryptKeeperSettings extends Fragment {

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
    private ImageView mImageView;
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

                mBatteryWarning.setText(getString(R.string.sp_encryption_low_charge_text_change,
                        level));
                mPowerWarning.setText(R.string.battery_power_encrypt_change_new);

                if (Config.VZW.equals(Config.getOperator())) {
                    if (!pluggedOk && !levelOk) {
                        mPowerWarning
                                .setText(getString(R.string.encryption_not_battery_plug_changed, level));
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                } else {
                    if (!pluggedOk && levelOk) {
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                    else if (!pluggedOk && !levelOk) {
                        mPowerWarning
                                .setText(getString(R.string.encryption_not_battery_plug_changed, level));
                        mBatteryWarning.setVisibility(View.GONE);
                    }
                    else {
                        mPowerWarning.setText(R.string.battery_power_encrypt_change_new);
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
                int icon_res = android.R.attr.alertDialogIcon;
                if (Utils.isUI_4_1_model(getActivity())) {
                    icon_res = R.drawable.no_icon;
                    Log.d("hkk", "UI4.1 model");
                } else {
                    Log.d("hkk", "UI4.0 model");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.crypt_keeper_dialog_need_password_title)
                        .setIconAttribute(icon_res)
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
                                })
                        .create()
                        .show();
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

        // CAPP_MDM [a1-mdm-dev@lge.com] add encryption API related to get()
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                getActivity().getWindow()
                        .addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        }
        // LGMDM_END
        mContentView = inflater.inflate(R.layout.crypt_keeper_settings, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)mContentView.getLayoutParams()).removeBorders = true;
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        mImageView = (ImageView)mContentView.findViewById(R.id.encryptionView);
        if (null != mImageView) {
            if (Utils.isTablet()) {
                mImageView.setVisibility(View.VISIBLE);
            } else {
                mImageView.setVisibility(View.GONE);
            }
        }


        mInitiateButton = (Button)mContentView.findViewById(R.id.initiate_encrypt);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mInitiateButton.setEnabled(false);

        mPowerWarning = (TextView)mContentView.findViewById(R.id.warning_unplugged);
        mBatteryWarning = (TextView)mContentView.findViewById(R.id.warning_low_charge);

        return mContentView;
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
        LockPatternUtilsEx lockPatternUtils = new LockPatternUtilsEx(getActivity());
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
                if (Utils.isUI_4_1_model(getActivity().getApplicationContext())) {
                    showConfirmDialog(password);
                } else {
                    showFinalConfirmation(password);
                }
            } else {
                getActivity().finish();
            }
        }
    }

    private void showConfirmDialog(final String finalPassword) {
        AlertDialog mEncryptionAlertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(getText(R.string.sp_security_warning_message_before_encrypting_new))
                .setTitle(getText(R.string.sp_dlg_note_NORMAL))
                .setPositiveButton(getText(R.string.dlg_ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                showFinalConfirmation(finalPassword);
                            }
                        })
                .setNegativeButton(getText(R.string.dlg_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).create();
        // LGMDM [a1-mdm-dev@lge.com] add encryption API related to get()
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (MDMSettingsAdapter.getInstance().checkDeviceEncryption()) {
                mEncryptionAlertDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            }
        }
        // LGMDM_END
        if (!Utils.isUI_4_1_model(getActivity())) {
            mEncryptionAlertDialog.setIconAttribute(android.R.attr.alertDialogIcon);
         }
        mEncryptionAlertDialog.show();
    }

    private void showFinalConfirmation(String password) {
        LockPatternUtilsEx lockPatternUtils = new LockPatternUtilsEx(getActivity());
        int quality = lockPatternUtils.getActivePasswordQuality();

        Preference preference = new Preference(getActivity());

        preference.setFragment(CryptKeeperConfirm.class.getName());
        if (!Utils.supportSplitView(getActivity())) {
            preference.setTitle(R.string.crypt_keeper_confirm_title);
        } else { 
            preference.setTitle(R.string.sp_encryption_settings_enable_NORMAL);
        }
        preference.getExtras().putString("password", password);

        preference.getExtras().putInt("type", changeQualityToType(quality));
        ((PreferenceActivity)getActivity()).onPreferenceStartFragment(null, preference);
    }

    private int changeQualityToType(int type) {

        switch(type) {
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC: //pin
                type = 3;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING: // pattern
                type = 2;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC: // pwd
                type = 0;
                break;
            default :
                type = 0;
                break;
        }

        return type;
    }
}
