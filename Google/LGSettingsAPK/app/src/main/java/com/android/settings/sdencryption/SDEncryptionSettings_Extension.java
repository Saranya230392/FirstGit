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

package com.android.settings.sdencryption;

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

import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.lockscreen.ChooseLockGeneric;
import java.io.FileWriter;
import java.io.BufferedWriter;
import android.app.ProgressDialog;
import android.os.Handler;
import android.widget.Toast;
import android.view.WindowManager;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGSDEncManager;
import android.os.Message;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.os.storage.StorageManager;
import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;
import com.android.settings.sdencryption.SDEncryptionSummaryPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.SecuritySettings;
import com.android.settings.SdcardFullDialogActivity;
import android.text.TextUtils;
import android.content.res.Configuration;
import android.widget.LinearLayout;
import android.os.storage.StorageEventListener;
import android.view.Window;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import com.lge.constants.KeyExceptionConstants;

public class SDEncryptionSettings_Extension extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SDEncryptionSettings_Extension";

    //CRYPT_CMD_PATH
    private static final String KEY_SETTINGS1_INFO_CATEGORY = "encryption_settings";
    private static final String KEY_SETTINGS2_INFO_CATEGORY = "multimedia_file_settings";
    private static final String KEY_NEWFILE = "new_radio_button";
    private static final String KEY_FULL = "full_radio_button";
    private static final String MULTIMEDIACHECKBOX = "multimedia_checkbox";

    private static final int KEYGUARD_REQUEST = 55;
    private static final int INTERNAL_CODE = 1;
    private static final int SDCARD_CODE = 2;
    private static final int SPACES_ERROR_CODE = 3;
    private static final int MOVE_ERROR_CODE = 4;
    private static final int SKIP_ENCRYPTION_DATA_STATUS = 5;
    // This is the minimum acceptable password quality.  If the current password quality is
    // lower than this, encryption should not be activated.
    static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;

    // Minimum battery charge level (in percent) to launch encryption.  If the battery charge is
    // lower than this, encryption should not be activated.
    private static final int MIN_BATTERY_LEVEL = 80;

    private View mContentView;
    private Button mApplyButton;
    private Button mBackButton;
    private IntentFilter mIntentFilter;

    private int mCryptProperty;
    private PreferenceCategory settings1Category;
    private PreferenceCategory settings2Category;
    private CheckBoxPreference mMediacheck;
    private RadioButtonPreference mNewFile;
    private RadioButtonPreference mFull;
    private SDEncryptionSummaryPreference mDescription_sdencrypt_msg; //sd is enabled
    private SDEncryptionSummaryPreference mDescription_battery_msg; //battery and pluged
    private SDEncryptionSummaryPreference mDescription_plug_msg; //battery and pluged
    private SDEncryptionSummaryPreference mDescription_noplug_nobattery_msg; //battery and pluged
    private LGContext mServiceContext = null;
    private LGSDEncManager mLGSDEncManager = null;
    private static final String SET_ENCRYPTION_REQUEST = "encryption_confirm";
    private boolean mCrypteEnProperty;
    private static final int PROGRESS_UPDATE_MSG = 1;
    private static final int PROGRESS_END_MSG = 2;
    public  CharSequence mState = null;
    ProgressDialog mProgDialog;
    private static final String PERCENT_PROPERTY_VALUE = "persist.security.sdfullpercent";
    private boolean levelOk;
    private boolean pluggedOk;
    private Configuration mConfig = new Configuration();
    private ListView mListView;
    private int rc = 0;
    private boolean mIsPaused = false;
    private boolean mStorageChanged = false;

    private StorageManager mStorageManager = null;

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "mStorageListener newState : " + newState);

            if (!Encryption_Data.isProgressing()) {
                mStorageChanged = true;
                if (SecuritySettings.getFullMediaExceptionOption() != SecuritySettings.MEDIA_CASE) {
                    if (newState.equals(Environment.MEDIA_UNMOUNTED) ||
                        newState.equals(Environment.MEDIA_NOFS) ||
                        newState.equals(Environment.MEDIA_UNMOUNTABLE) ||
                        newState.equals(Environment.MEDIA_BAD_REMOVAL) ||
                        newState.equals(Environment.MEDIA_REMOVED)) {
                        Encryption_Data.setCheckStorageVolume(false);
                        if (!mIsPaused) {
                            do_setCheckNewFile(false);
                        }
                    } else {
                        Encryption_Data.setCheckStorageVolume(true);
                        if (!mIsPaused) {
                            do_setCheckFull(false);
                        }
                    }
                }
            }
        }
    };

    private Handler confirmHandler = new Handler() {
       @Override
        public void handleMessage(Message msg) {
                switch(msg.what){
                    case PROGRESS_END_MSG:
                        if (getActivity() == null) {
                            break;
                         }

                        Encryption_Data.setSDEncryptionConfirm(true);
                        try {
                            confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                           if (mProgDialog != null) {
                                mProgDialog.dismiss();
                           }
                        } catch (IllegalArgumentException e) {
                          Log.e(TAG, "IllegalArgumentException:" + e);
                          e.printStackTrace();
                        } catch (NullPointerException e) {
                          Log.e(TAG, "NullPointerException:" + e);
                          e.printStackTrace();
                        }
                        checkErrorCode();
                    break;

                    case PROGRESS_UPDATE_MSG:
                         if (getActivity() == null) {
                            break;
                         }
                        if (!Encryption_Data.isProgressing()) {
                            confirmHandler.sendMessage(Message.obtain(confirmHandler, PROGRESS_END_MSG));
                            break;
                        }
                        confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                        confirmHandler.sendEmptyMessageDelayed(PROGRESS_UPDATE_MSG, 200);
                        showSDUpdateDialog();
                        break;
                   }
            }
    };

    private void checkErrorCode() {
        int errorcode = Encryption_Data.getSDEncryptionError();
        Intent intent = new Intent(getActivity(), SdcardFullDialogActivity.class);
        intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
        switch (errorcode) {
            case Encryption_Data.ERROR_MOVE_FAIL:
                startActivityForResult(intent, MOVE_ERROR_CODE);
                break;
            case Encryption_Data.ERROR_SPACE_LOW:
                Encryption_Data.setSDEncryptionConfirm(false);
                Encryption_Data.setSDEncryptionSpacesError(true);
                startActivityForResult(intent, SPACES_ERROR_CODE);
                break;
            default:
                onBackPressed();
                intent.removeExtra("encryption");
                setSDCardEndProgressToastMessage();
                break;
        }
    }

    private void setSDCardEndProgressToastMessage() {
         mCryptProperty = Encryption_Data.SDEncryption_getCryptMsg();
         int mToastMessage = R.string.sp_encrypt_enable_NORMAL;
         switch (mCryptProperty) {
            case Encryption_Data.ENABLE_FULL_CASE:
                mToastMessage =  R.string.sp_encrypt_full_enable_NORMAL;
                break;
            case Encryption_Data.ENABLE_NORMAL_CASE:
                if (Utils.isSupportUIV4_2()) {
                    mToastMessage = R.string.sp_encrypt_enable_NORMAL_version4_2;
                } else {
                    mToastMessage = R.string.sp_encrypt_enable_NORMAL;
                }
                break;
            case Encryption_Data.DISABLE_FULL_CASE:
                mToastMessage =  R.string.sp_encrypt_full_disable_changed_NORMAL;
                break;
            case Encryption_Data.DISABLE_NORMAL_CASE:
                if (Utils.isSupportUIV4_2()) {
                    mToastMessage = R.string.sp_encrypt_disable_NORMAL_version4_2;
                } else {
                    mToastMessage = R.string.sp_encrypt_disable_NORMAL;
                }
                break;
            default:
               break;
        }
        showSDEndProgressToast(mToastMessage);
    }

   private void showSDEndProgressToast(int mToastMessage) {
         Toast.makeText(getActivity(), getString(mToastMessage),
         Toast.LENGTH_SHORT).show();
   }

   private void showSDUpdateDialog() {
         mCryptProperty = Encryption_Data.SDEncryption_getCryptMsg();
         switch (mCryptProperty) {
            case Encryption_Data.ENABLE_FULL_CASE:
                if (Utils.isSupportUIV4_2()) {
                    mState = getString(R.string.sp_full_encrypt_disabling_NORMAL_version4_2);
                } else {
                    mState = getString(R.string.sp_full_encrypt_disabling_NORMAL);
                }
                mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
                break;
            case Encryption_Data.ENABLE_NORMAL_CASE:
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL));
                }
                break;
            case Encryption_Data.DISABLE_FULL_CASE:
                if (Utils.isSupportUIV4_2()) {
                    mState = getString(R.string.sp_full_encrypt_enabling_NORMAL_version4_2);
                } else {
                    mState = getString(R.string.sp_full_encrypt_enabling_NORMAL);
                }
                mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
                break;
            default:
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL));
                }
               break;
         }
         mProgDialog.show();
    }

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
                    ((plugged & BatteryManager.BATTERY_PLUGGED_ANY) != 0) &&
                    invalidCharger == 0;
                Encryption_Data.setBatteryLevel(level);
                if (Encryption_Data.isBatteryStatusChanged(levelOk, pluggedOk) == true) {
                    Encryption_Data.setBatteryStatus(levelOk, pluggedOk);
                    Log.d(TAG, "Battery status changed");
                    update_dynamicSummary();
                }
            }
        }
    };

    private void do_setCheckNewFile(boolean byClick) {
        Log.d(TAG, "do_setCheckNewFile " + byClick);
        mFull.setChecked(false);
        mNewFile.setChecked(true);

        if (byClick == false) {
            mFull.setEnabled(false);
        }
        mDescription_sdencrypt_msg.updatedDecryptDesc(false);
        mDescription_sdencrypt_msg.setSelectable(false);
        
        update_dynamicSummary();
    }

    private void do_setCheckFull(boolean byClick) {
        mFull.setEnabled(true);
        if (byClick == true) {
            mNewFile.setChecked(false);
            mFull.setChecked(true);
            mDescription_sdencrypt_msg.updatedDecryptDesc(true);
            mDescription_sdencrypt_msg.setSelectable(false);
        }

        update_dynamicSummary();
    }

    private void update_dynamicSummary() {
        if ((mNewFile.isChecked())
            || (Encryption_Data.getCheckStorageVolume() == false)) {
            update_newfilecheck_status();
        } else {
            update_fullcheck_status();
        }
    }

    private void update_newfilecheck_status() {
        Encryption_Data.setFullOption(false);
        mApplyButton.setEnabled(true);
        if (false == mCrypteEnProperty) {
            mApplyButton.setText(getString(R.string.sp_security_encryption_button_label));
            getPreferenceScreen().removePreference(mDescription_sdencrypt_msg);
        } else {
            mApplyButton.setText(getString(R.string.sp_sd_encryption_dis_button_NORMAL));
        }
        mDescription_sdencrypt_msg.setSelectable(false);
        getPreferenceScreen().removePreference(mDescription_plug_msg);
        getPreferenceScreen().removePreference(mDescription_battery_msg);
        getPreferenceScreen().removePreference(mDescription_noplug_nobattery_msg);
    }

    private void update_fullcheck_status() {
        Log.d(TAG, "pluggedOk = " + pluggedOk + "  levelOk " + levelOk);

        Encryption_Data.setFullOption(true);
        if (mFull.isChecked()) {
            mApplyButton.setEnabled(levelOk && pluggedOk);
        }
        getPreferenceScreen().removePreference(mDescription_noplug_nobattery_msg);
        getPreferenceScreen().removePreference(mDescription_battery_msg);
        getPreferenceScreen().removePreference(mDescription_plug_msg);
        setSDFullPreferenceStatus();
    }

    private void setSDFullPreferenceStatus() {
          if (pluggedOk) {
                if (!levelOk) {
                    mDescription_battery_msg = new SDEncryptionSummaryPreference(getActivity(),
                                          SDEncryptionSummaryPreference.SD_ENCRYPT_BATTERY_MSG,
                                          mCrypteEnProperty);
                    if (!mCrypteEnProperty) {
                         setSDdescriptionMessage(false);
                         mDescription_battery_msg.setTitle(getString(R.string.sp_encryption_sd_card_low_charge_text_change,
                                                      Encryption_Data.getBatteryLevel()));
                    } else {
                         mDescription_battery_msg.setTitle(getString(R.string.sp_decryption_sd_card_low_charge_text,
                         Encryption_Data.getBatteryLevel()));
                    }
                    getPreferenceScreen().addPreference(mDescription_battery_msg);
                    mDescription_battery_msg.setSelectable(false);
                }
            } else {
                if (levelOk) {
                    mDescription_plug_msg = new SDEncryptionSummaryPreference(getActivity(),
                                    SDEncryptionSummaryPreference.SD_ENCRYPT_PLUG_MSG,
                                    mCrypteEnProperty);
                    if (!mCrypteEnProperty) {
                        setSDdescriptionMessage(false);
                        mDescription_plug_msg.setTitle(getString(R.string.battery_power_decrypt_change));
                    } else {
                        mDescription_plug_msg.setTitle(getString(R.string.battery_power_encrypt_change_new));
                    }
                    getPreferenceScreen().addPreference(mDescription_plug_msg);
                    mDescription_plug_msg.setSelectable(false);
                } else {
                    mDescription_noplug_nobattery_msg = new SDEncryptionSummaryPreference(getActivity(),
                       SDEncryptionSummaryPreference.SD_ENCRYPT_NO_PLUG_NO_BATTERY,
                       mCrypteEnProperty);
                    if (!mCrypteEnProperty) {
                        setSDdescriptionMessage(false);
                        mDescription_noplug_nobattery_msg.setTitle(getString(R.string.encryption_sd_card_not_battery_plug,
                                    Encryption_Data.getBatteryLevel()));
                    } else {
                        mDescription_noplug_nobattery_msg.setTitle(getString(R.string.decryption_sd_card_not_battery_plug,
                                    Encryption_Data.getBatteryLevel()));
                    }
                    getPreferenceScreen().addPreference(mDescription_noplug_nobattery_msg);
                    mDescription_noplug_nobattery_msg.setSelectable(false);
                }
           }
           mDescription_sdencrypt_msg.setSelectable(false);
           setApplyButtonMessage();
    }

    private void setSDdescriptionMessage(boolean mDisplay) {
        if (mDisplay) {
           getPreferenceScreen().addPreference(mDescription_sdencrypt_msg);
           mDescription_sdencrypt_msg.setSelectable(false);
        } else {
           getPreferenceScreen().removePreference(mDescription_sdencrypt_msg);
        }
    }

   private void setApplyButtonMessage() {
           if (!mCrypteEnProperty) {
               mApplyButton.setText(getString(R.string.sp_security_encryption_button_label));
           } else {
               mApplyButton.setText(getString(R.string.sp_full_encryption_disable_NORMAL));
           }
    }

    private Button.OnClickListener mApplyListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Config.getFWConfigBool(getActivity().getApplicationContext(),
                com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
                "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")
                && mCrypteEnProperty) {
                warningPopupWhenChangingLockType();
            } else {
                encryptionSDcardStoragePopup();
            }


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
                onBackPressed();
            }
        }
    };

    private void warningPopupWhenChangingLockType() {
        int res_icon = android.R.attr.alertDialogIcon;
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sp_sd_encryption_need_password_title_NORMAL)
                .setIconAttribute(res_icon)
                .setMessage(R.string.sp_storage_sd_card_confirm_dialog_before_decryption_disa)
                .setPositiveButton(getResources().getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        encryptionSDcardStoragePopup();
                    }
                 })
                 .create()
                 .show();
    }

    private void encryptionSDcardStoragePopup() {
        if (!runKeyguardConfirmation(SDCARD_CODE)) {
            int res_icon = android.R.attr.alertDialogIcon;
            if (Utils.isUI_4_1_model(getActivity())) {
                res_icon = R.drawable.no_icon;
            }
            new AlertDialog.Builder(getActivity())
            .setTitle(R.string.sp_sd_encryption_need_password_title_NORMAL)
            .setIconAttribute(res_icon)
            .setMessage(R.string.sp_sd_encryption_need_password_message_NORMAL)
            .setPositiveButton(getResources().getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), ChooseLockGeneric.class);
                intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
                startActivityForResult(intent, SDCARD_CODE);
                dialog.dismiss();
             }
             })
             .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
             })
             .create()
             .show();
             }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (mStorageManager == null) {
            mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
            if (mStorageManager == null) {
                Log.w(TAG, "Failed to get StorageManager");
            } else {
                mStorageManager.registerListener(mStorageListener);
            }
        }
    }

    private void initializeAllPreferences() {
        addPreferencesFromResource(R.layout.sd_encryption_settings_extension_preference);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        settings1Category = (PreferenceCategory)findPreference(KEY_SETTINGS1_INFO_CATEGORY);
        mNewFile = (RadioButtonPreference)findPreference(KEY_NEWFILE);
        mNewFile.setOrder(0);
        mNewFile.setChecked(true);
        mNewFile.setOnPreferenceChangeListener(this);
        mFull = (RadioButtonPreference)findPreference(KEY_FULL);
        mFull.setOnPreferenceChangeListener(this);
        settings2Category = (PreferenceCategory)findPreference(KEY_SETTINGS2_INFO_CATEGORY);
        mMediacheck = (CheckBoxPreference)findPreference(MULTIMEDIACHECKBOX);
        mDescription_sdencrypt_msg = new SDEncryptionSummaryPreference(getActivity(),
                                         SDEncryptionSummaryPreference.SD_ENCRYPT_ENABLED_MSG);
        getPreferenceScreen().addPreference(mDescription_sdencrypt_msg);
        mDescription_sdencrypt_msg.setSelectable(false);
        mDescription_battery_msg = new SDEncryptionSummaryPreference(getActivity(),
                                       SDEncryptionSummaryPreference.SD_ENCRYPT_BATTERY_MSG);
        getPreferenceScreen().addPreference(mDescription_battery_msg);
        mDescription_battery_msg.setSelectable(false);
        mDescription_plug_msg = new SDEncryptionSummaryPreference(getActivity(),
                                    SDEncryptionSummaryPreference.SD_ENCRYPT_PLUG_MSG,
                                    mCrypteEnProperty);
        getPreferenceScreen().addPreference(mDescription_plug_msg);
        mDescription_plug_msg.setSelectable(false);
        mDescription_noplug_nobattery_msg = new SDEncryptionSummaryPreference(getActivity(),
                                    SDEncryptionSummaryPreference.SD_ENCRYPT_NO_PLUG_NO_BATTERY);
        getPreferenceScreen().addPreference(mDescription_noplug_nobattery_msg);
        mDescription_noplug_nobattery_msg.setSelectable(false);
    }

    private void do_setInitialize() {
        do_setSummary();
        do_setButtonInit();
        mDescription_sdencrypt_msg.updatedDecryptDesc(false);
        mDescription_sdencrypt_msg.setSelectable(false);
        if (SecuritySettings.getFullMediaExceptionOption() == SecuritySettings.MEDIA_CASE) {
            getPreferenceScreen().removePreference(mFull);
            mNewFile.setChecked(true);
        } else {
            if (Encryption_Data.getCheckStorageVolume() == true) {
                do_setCheckFull(false);
            } else {
                do_setCheckNewFile(false);
            }
        }
        
        update_dynamicSummary();

        //media option
        if ((SecuritySettings.getFullMediaExceptionOption() == SecuritySettings.FULL_CASE)
            || (mCrypteEnProperty == true)) {
            getPreferenceScreen().removePreference(settings2Category);
            getPreferenceScreen().removePreference(mMediacheck);
        } else {
            if (Encryption_Data.getMediaOption() == true) {
                mMediacheck.setChecked(true);
            } else {
                mMediacheck.setChecked(false);
            }
        }
    }

    private void do_setButtonInit() {
       if (mContentView != null) {
           mApplyButton = (Button)mContentView.findViewById(R.id.sdencrypt_settings_apply);
           mApplyButton.setOnClickListener(mApplyListener);
           mBackButton = (Button)mContentView.findViewById(R.id.sdencrypt_settings_back);
           mBackButton.setOnClickListener(mBackListener);
           mBackButton.setText(getString(R.string.cancel));
            if (Utils.isUI_4_1_model(getActivity())) {
                mApplyButton.setText(getString(R.string.sp_security_encryption_button_label));
            }

            if (mCrypteEnProperty == false) {
                mApplyButton.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                getPreferenceScreen().removePreference(mDescription_sdencrypt_msg);
                getPreferenceScreen().removePreference(mDescription_noplug_nobattery_msg);
                getPreferenceScreen().removePreference(mDescription_battery_msg);
                getPreferenceScreen().removePreference(mDescription_plug_msg);
            } else {
                getPreferenceScreen().removePreference(mDescription_battery_msg);
                getPreferenceScreen().removePreference(mDescription_plug_msg);
                mDescription_sdencrypt_msg.setSelectable(false);
                mApplyButton.setVisibility(View.VISIBLE);
                mApplyButton.setText(getString(R.string.sp_sd_encryption_dis_button_NORMAL));
                mBackButton.setVisibility(View.GONE);
            }
        }
    }

    private void do_setSummary() {
        //full option
        if (mCrypteEnProperty == false) {
            if (Utils.isUI_4_1_model(getActivity())) {
                mNewFile.setTitle(R.string.sp_newfile_encryption_enable_NORMAL_ex);
            } else {
                mNewFile.setTitle(getString(R.string.sp_newfile_encryption_enable_NORMAL));
            }
            if (Utils.isSupportUIV4_2()) {
                mNewFile.setSummary(getString(R.string.sp_newfile_encryption_enable_summary_NORMAL_version4_2));
                mFull.setSummary(getString(R.string.sp_sdcard_encryption_summary_NORMAL_new));
            } else {
                mNewFile.setSummary(getString(R.string.sp_newfile_encryption_enable_summary_NORMAL));
                mFull.setSummary(getString(R.string.sp_storage_sd_card_full_summary_NORMAL));
            }
            mFull.setTitle(getString(R.string.sp_storage_encryption_full_title_NORMAL));
        } else {
            mNewFile.setTitle(getString(R.string.sp_newfile_encryption_disable_NORMAL));
            if (Utils.isSupportUIV4_2()) {
                mNewFile.setSummary(getString(R.string.sp_newfile_encryption_disable_summary_new_NORMAL_version4_2));
            } else {
                mNewFile.setSummary(getString(R.string.sp_newfile_encryption_disable_summary_NORMAL));
            }
            mFull.setTitle(getString(R.string.sp_full_encryption_disable_NORMAL));
            mFull.setSummary(getString(R.string.sp_storage_sd_card_full_un_summary_NORMAL) +
            " " + getString(R.string.sp_storage_sd_card_full_un_summary_second_NORMAL));
           settings1Category.setTitle(R.string.sp_encryption_settings_disable_NORMAL);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        boolean isResumefromTablet = false;
        mCrypteEnProperty = SecuritySettings.SDEncryption_checkEnableProperty();
        //[S][2011.12.07][jin850607.hong@lge.com][G1-D1L][TD103664] Set activity title
        if (getActivity() != null && !Utils.supportSplitView(getActivity())) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_security);
            getActivity().setTitle(getResources().getString(R.string.sp_storage_encryption_sdcard_title_NORMAL));
        }
        if (mContentView != null) {
            isResumefromTablet = true;
        }

        mContentView = inflater.inflate(R.layout.sd_encryption_settings_extension_linear, null);

        if (mContentView != null) {
            mListView = (ListView)mContentView.findViewById(android.R.id.list);
        }

        Encryption_Data.SDEncryption_setOption();

        if (LGSDEncManager.getSDEncSupportStatus(getActivity().getApplicationContext())) {
            mServiceContext = new LGContext(getActivity().getApplicationContext());
            mLGSDEncManager = (LGSDEncManager)mServiceContext.getLGSystemService(LGContext.LGSDENC_SERVICE);
            initExternalSDCardPath();
        }

        Encryption_Data.checkStorageVolume(getActivity().getApplicationContext(), mStorageManager);
        if (!isResumefromTablet) {
            initializeAllPreferences();
        }

        do_setInitialize();

        if (Encryption_Data.getMountService() == null) {
            Log.e(TAG, "Fail get MountService");
            return mContentView;
        }

        Encryption_Data.setSDEncryptionConfirm(false);
        Encryption_Data.setSDEncryptionSpacesError(false);

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance()
                    .addEncryptionSettingChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
		if (mListView != null) {
		     mListView.setFooterDividersEnabled(false);
             mListView.setItemsCanFocus(true);
        }
        return mContentView;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (SecuritySettings.getFullMediaExceptionOption() == SecuritySettings.MEDIA_CASE) {
            return true;
        }

        if ((preference == mNewFile)
            || (Encryption_Data.getCheckStorageVolume() == false)) {
            do_setCheckNewFile(true);
        } else {
            do_setCheckFull(true);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (MULTIMEDIACHECKBOX.equals(preference.getKey())) {
            boolean bChecked = mMediacheck.isChecked();
            mMediacheck.setChecked(bChecked);
            Encryption_Data.setMediaOption(bChecked);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);

        mIsPaused = false;
        if (mStorageChanged) {
            mStorageChanged = false;
            if (Encryption_Data.getCheckStorageVolume() == true) {
                do_setCheckFull(false);
            } else {
                do_setCheckNewFile(false);
            }
        }

        if (Encryption_Data.getSDEncryptionConfirm() == true) {
            Log.i(TAG, "finish");
            onBackPressed();
        }

        if (!Encryption_Data.isProgressing()) {
            mCrypteEnProperty = SecuritySettings.SDEncryption_checkEnableProperty();
        }
        if (Encryption_Data.isProgressing()) {
            showFinalSdcardConfirmDe_keep();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            getActivity().unregisterReceiver(mLGMDMReceiver);
        }
        // LGMDM_END
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);

        mIsPaused = true;
        try {
            if (mProgDialog != null && mProgDialog.isShowing()) {
                mProgDialog.dismiss();
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
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
                    onBackPressed();
                }
            }
        }
    }

    //[S][2013.01.31][TD:281676][swgi.kim@lge.com] displaying error when opientation landsacape
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfig.updateFrom(newConfig);
    }
    //[E][2013.01.31][TD:281676][swgi.kim@lge.com] displaying error when opientation landsacape

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

        if (requestCode != INTERNAL_CODE &&  requestCode != SDCARD_CODE && requestCode != KEYGUARD_REQUEST
            && requestCode != SPACES_ERROR_CODE && requestCode != MOVE_ERROR_CODE) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK && data != null) {
            String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);

            if (!TextUtils.isEmpty(password)) {
                if (requestCode == INTERNAL_CODE) {
                } else if (requestCode == SDCARD_CODE) {
                    if ((Encryption_Data.getFullOption() == true) && (Encryption_Data.getCheckStorageVolume() == true)) {
                        Encryption_Data.checkPossibleEncryption();
                    }

                    if (SecuritySettings.SDEncryption_checkEnableProperty() == false) { //enable case
                        if ((Encryption_Data.getFullOption() == false)
                            || ((Encryption_Data.getFullOption() == true) && (Encryption_Data.getCheckStorageVolume() == false))) {
                            showFinalSdcardConfirmDe(password);
                        }else {
                            showFinalSdcardConfirm(password);
                        }
                    } else { //disable case
                        if (Encryption_Data.getSDEncryptionConfirm() == true) {
                            Log.i(TAG, "finish");
                            onBackPressed();
                        } else {
                            showFinalSdcardConfirmDe(password);
                        }
                    }
                }
            } else {
                onBackPressed();
            }
        } else if (requestCode == SPACES_ERROR_CODE) {
            Log.i(TAG, "Encryption is failed. Please checked spaces");
            Encryption_Data.setSDEncryptionSpacesError(false);
        } else if (requestCode == MOVE_ERROR_CODE) {
            Log.i(TAG, "Encryption is failed. Please checked move files");
            onBackPressed();
            Toast.makeText(getActivity(), R.string.sp_full_encryption_fail_toast_NORMAL,
            Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        if (getActivity() != null) {
            if (!Utils.supportSplitView(getActivity())) {
                getActivity().finish();
            } else {
                getActivity().onBackPressed();
            }
        }
    }

    private void showFinalSdcardConfirm(String password) {
        Preference preference = new Preference(getActivity());
        preference.setFragment(SDEncryptionConfirm_Extension.class.getName());
        preference.setTitle(R.string.sp_sd_card_confirm_title_NORMAL);
        preference.getExtras().putString("password", password);
        ((PreferenceActivity)getActivity()).onPreferenceStartFragment(null, preference);
    }

    private void setWindowAttributesForBypassKeys(Window window, boolean bypass) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        if (bypass) {
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_HOT_KEY;
            attrs.privateFlags |= KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
        } else {
            attrs.privateFlags &= ~KeyExceptionConstants.BYPASS_HOME_KEY;
            attrs.privateFlags &= ~KeyExceptionConstants.BYPASS_HOT_KEY;
            attrs.privateFlags &= ~KeyExceptionConstants.BYPASS_APP_SWITCH_KEY;
        }
        window.setAttributes(attrs);
    }

    private void showFinalSdcardConfirmDe(final String passwd) {
        if (Utils.isMonkeyRunning()) {
            return;
        }

        mCryptProperty = Encryption_Data.SDEncryption_getCryptMsg();

        mProgDialog = new ProgressDialog(getActivity());

        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setWindowAttributesForBypassKeys(mProgDialog.getWindow(), true);
        mProgDialog.setCancelable(false);

        android.os.SystemProperties.set(PERCENT_PROPERTY_VALUE, "0");

        if (mCryptProperty == Encryption_Data.ENABLE_FULL_CASE) {
            if (Utils.isSupportUIV4_2()) {
                mState = getString(R.string.sp_full_encrypt_disabling_NORMAL_version4_2);
            } else {
                mState = getString(R.string.sp_full_encrypt_disabling_NORMAL);
            }
            mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
        } else if (mCryptProperty == Encryption_Data.DISABLE_FULL_CASE) {
            if (Utils.isSupportUIV4_2()) {
                mState = getString(R.string.sp_full_encrypt_enabling_NORMAL_version4_2);
            } else {
                mState = getString(R.string.sp_full_encrypt_enabling_NORMAL);
            }
            mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
        } else {
            if (mCryptProperty == Encryption_Data.ENABLE_NORMAL_CASE) {
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL));
                }
            } else {
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL));
                }
            }
        }

        mProgDialog.show();
        checkSDEncryptionStatus(passwd);
    }

    private void checkSDEncryptionStatus(final String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle args = getArguments();

                    if (mLGSDEncManager == null) {
                        confirmHandler.sendMessage(Message.obtain(confirmHandler, PROGRESS_END_MSG));
                        Log.e(TAG, "mLGSDEncManager fail :: " + mLGSDEncManager);
                        return;
                    }
                    checkMediaEnabled();

                    setEncryptionData(SKIP_ENCRYPTION_DATA_STATUS, true, 0);
                    confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                    confirmHandler.sendMessageDelayed(Message.obtain(confirmHandler, PROGRESS_UPDATE_MSG), 2000);
                    if (Encryption_Data.getFullOption() && Encryption_Data.getCheckStorageVolume()) {
                        rc = mLGSDEncManager.externalSDCardCheckMemory();
                        if (rc < 0) {
                            setEncryptionData(Encryption_Data.ERROR_SPACE_LOW, false, SKIP_ENCRYPTION_DATA_STATUS);
                            return;
                        }
                        Encryption_Data.setDISASDcardValue(getActivity().getApplicationContext());
                    }//full option

                    if (mCrypteEnProperty) {
                        rc = mLGSDEncManager.externalSDCardDisableEncryption("externalStorage");
                    } else {
                       if (Config.getFWConfigBool(getActivity().getApplicationContext(),
                            com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
                            "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")) {
                            rc = mLGSDEncManager.externalSDCardEnableEncryptionUserPassword("externalStorage", password);
                       } else {
                            rc = mLGSDEncManager.externalSDCardEnableEncryption("externalStorage");
                       }
                    }//encryption

                    if (rc == -1) {
                        Encryption_Data.setProgressing(false);
                        return;
                    }

                    if (Encryption_Data.getFullOption() && Encryption_Data.getCheckStorageVolume()) {
                        if (mCrypteEnProperty) {
                            rc = mLGSDEncManager.externalSDCardFullDisableEncryption(
                                   Encryption_Data.getExternalSDCardPath());
                        } else {
                            rc = mLGSDEncManager.externalSDCardFullEnableEncryption(
                                   Encryption_Data.getExternalSDCardPath());
                        }
                        if (rc < 0) {
                            setEncryptionData(Encryption_Data.ERROR_MOVE_FAIL, false, SKIP_ENCRYPTION_DATA_STATUS);
                            return;
                        }
                    }//Full option
                    setEncryptionData(SKIP_ENCRYPTION_DATA_STATUS, false, SKIP_ENCRYPTION_DATA_STATUS);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void setEncryptionData(int status, boolean progressValue, int error) {
         if (status != SKIP_ENCRYPTION_DATA_STATUS) {
             Encryption_Data.setSDEncryptionError(status);
         }
         Encryption_Data.setProgressing(progressValue);
         if (error != SKIP_ENCRYPTION_DATA_STATUS) {
             Encryption_Data.setSDEncryptionError(error);
         }
    }

    private void checkMediaEnabled() {
        try {
            if (Encryption_Data.getMediaOption() == false) {
                mLGSDEncManager.externalSDCardMediaDisableEncryption();
            } else {
                mLGSDEncManager.externalSDCardMediaEnableEncryption();
            }
        } catch (Exception e) {
                Log.e(TAG, "err : checkMediaEnabled :" + e.toString());
        }
    }

    private void initExternalSDCardPath() {
        String externalPath  = null;

        try{
            if (mLGSDEncManager != null) {
                externalPath = mLGSDEncManager.getExternalSDCardMountPath();
            }
        }catch (Exception e) {
            Log.e(TAG, "err : initExternalSDCardPath :" + e.toString());
        }

        if (externalPath == null) {
            Log.e(TAG, "err : externalPath = :" + externalPath);
        } else {
           Encryption_Data.setExternalSDCardPath(externalPath);
        }
    }
    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-28][ID-MDM-29]
    private BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveSecurityEncryptionChangeIntent(intent)) {
                    onBackPressed();
                }
            }
        }
    };
    // LGMDM_END

    private void showFinalSdcardConfirmDe_keep() {
        if (Utils.isMonkeyRunning()) {
            return;
        }

        mCryptProperty = Encryption_Data.SDEncryption_getCryptMsg();

        if (mProgDialog == null) {
            mProgDialog = new ProgressDialog(getActivity());
        }

        if (mCryptProperty == Encryption_Data.ENABLE_FULL_CASE) {
            mState = getString(R.string.sp_full_encrypt_disabling_NORMAL);
            mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
        } else if (mCryptProperty == Encryption_Data.DISABLE_FULL_CASE) {
            mState = getString(R.string.sp_full_encrypt_enabling_NORMAL);
            mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
        } else {
            if (mCryptProperty == Encryption_Data.ENABLE_NORMAL_CASE) {
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_disabling_NORMAL));
                }
            } else {
                if (Utils.isSupportUIV4_2()) {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL_version4_2));
                } else {
                    mProgDialog.setMessage(getString(R.string.sp_encrypt_enabling_NORMAL));
                }
            }
        }
//FEATURE_SDCARD_PERCENT[E]

        mProgDialog.show();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
            // TODO Auto-generated method stub
                confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                confirmHandler.sendMessageDelayed(Message.obtain(confirmHandler, PROGRESS_UPDATE_MSG), 2000);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
