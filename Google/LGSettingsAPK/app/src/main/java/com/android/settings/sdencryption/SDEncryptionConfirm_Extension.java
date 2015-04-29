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

package com.android.settings.sdencryption;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
import android.app.admin.DevicePolicyManager;
import android.widget.Toast;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.view.WindowManager;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGSDEncManager;
import android.preference.PreferenceFrameLayout;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import android.os.SystemClock;
import com.android.settings.Utils;
import com.android.settings.R;
import com.android.settings.SecuritySettings;
import com.android.settings.SdcardFullDialogActivity;
import com.android.settings.lgesetting.Config.Config;
import com.lge.mdm.LGMDMManager;
//FEATURE_SDCARD_PERCENT[S]
import android.text.TextUtils;
//FEATURE_SDCARD_PERCENT[E]
import com.lge.constants.KeyExceptionConstants;
public class SDEncryptionConfirm_Extension extends Fragment {

    private static final String TAG = "SDEncryptionConfirm_Extension";

    //CRYPT_CMD_PATH
    private static final String SET_ENCRYPTION_REQUEST = "encryption_confirm";
    private static final int SPACES_ERROR_CODE = 3;
    private static final int MOVE_ERROR_CODE = 4;

    private boolean mProperty;
    private int mCryptProperty;
    private int rc = 0;
    private LGContext mServiceContext = null;
    private LGSDEncManager mLGSDEncManager = null;

    private View mContentView;
    private Button mApplyButton;
    private Button mBackButton;
    private TextView mFinalText;
    private TextView mFinalText_askPIN;
//FEATURE_SDCARD_PERCENT[S]
    private static final int PROGRESS_UPDATE_MSG = 1;
    private static final int PROGRESS_END_MSG = 2;
    public  CharSequence mState = null;
    ProgressDialog mProgDialog;
    private static final String PERCENT_PROPERTY_VALUE = "persist.security.sdfullpercent";
//FEATURE_SDCARD_PERCENT[E]

    private Button.OnClickListener mApplyListener = new Button.OnClickListener() {
        private Handler confirmHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//FEATURE_SDCARD_PERCENT[S]
                switch(msg.what){
                    case PROGRESS_END_MSG:
                        if (getActivity() == null) {
                            break;
                        }
                        mCryptProperty = Encryption_Data.SDEncryption_getCryptMsg();
                        confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);

                        Encryption_Data.setSDEncryptionConfirm(true);

                        try {
                            mProgDialog.dismiss();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        int errorcode = Encryption_Data.getSDEncryptionError();
                        if (errorcode == Encryption_Data.ERROR_MOVE_FAIL) {
                            Intent intent = new Intent(getActivity(), SdcardFullDialogActivity.class);
                            intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
                            startActivityForResult(intent, MOVE_ERROR_CODE);
                        } else if (errorcode == Encryption_Data.ERROR_SPACE_LOW) {
                            Encryption_Data.setSDEncryptionConfirm(false);
                            Encryption_Data.setSDEncryptionSpacesError(true);
                            Intent intent = new Intent(getActivity(), SdcardFullDialogActivity.class);
                            intent.putExtra(SET_ENCRYPTION_REQUEST, "encryption");
                            startActivityForResult(intent, SPACES_ERROR_CODE);
                        }else {
                            onBackPressed();
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
                            }
                            Toast.makeText(getActivity(), getString(mToastMessage), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case PROGRESS_UPDATE_MSG:
                        if (getActivity() == null) {
                            break;
                        }
                        if (mCryptProperty == Encryption_Data.ENABLE_FULL_CASE) {
                            if (Utils.isSupportUIV4_2()) {
                                mState = getString(R.string.sp_full_encrypt_disabling_NORMAL_version4_2);
                            } else {
                                mState = getString(R.string.sp_full_encrypt_disabling_NORMAL);
                            }
                        } else {
                            if (Utils.isSupportUIV4_2()) {
                                mState = getString(R.string.sp_full_encrypt_enabling_NORMAL_version4_2);
                            } else {
                                mState = getString(R.string.sp_full_encrypt_enabling_NORMAL);
                            }
                        }
                        mProgDialog.setMessage(TextUtils.expandTemplate(mState, SystemProperties.get(PERCENT_PROPERTY_VALUE)));
                        if (!Encryption_Data.isProgressing()) {
                            confirmHandler.sendMessage(Message.obtain(confirmHandler, PROGRESS_END_MSG));
                            break;
                        }
                        confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                        confirmHandler.sendEmptyMessageDelayed(PROGRESS_UPDATE_MSG, 300);
                        mProgDialog.show();
                        break;
                 }
//FEATURE_SDCARD_PERCENT[E]
            }
        };

        public void onClick(View v) {
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
//FEATURE_SDCARD_PERCENT[E]
        mProgDialog.show();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                String mountPath;
                try {
                    Bundle args = getArguments();

                    if (mLGSDEncManager == null) {
                        confirmHandler.sendMessage(Message.obtain(confirmHandler, PROGRESS_END_MSG));
                        return;
                    }
                    checkMediaEnabled();

                    Encryption_Data.setProgressing(true);
                    Encryption_Data.setSDEncryptionError(0);
                    if (Encryption_Data.getFullOption() == true) {
                        confirmHandler.removeMessages(PROGRESS_UPDATE_MSG);
                        confirmHandler.sendMessage(Message.obtain(confirmHandler, PROGRESS_UPDATE_MSG));
                            rc = mLGSDEncManager.externalSDCardCheckMemory();
                        if (rc < 0) {
                            Encryption_Data.setSDEncryptionError(Encryption_Data.ERROR_SPACE_LOW);
                            Encryption_Data.setProgressing(false);
                            return;
                        }
                            Encryption_Data.setDISASDcardValue(getActivity().getApplicationContext());
                    }

                    if (mProperty == true) {
                        rc = mLGSDEncManager.externalSDCardDisableEncryption("externalStorage");
                    } else {
                        if (Config.getFWConfigBool(getActivity().getApplicationContext(),
                            com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
                            "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")) {
                            rc = mLGSDEncManager.externalSDCardEnableEncryptionUserPassword("externalStorage", args.getString("password"));
                        } else {
                            rc = mLGSDEncManager.externalSDCardEnableEncryption("externalStorage");
                        }
                    } //encryption
                    if (rc == -1) {
                        Encryption_Data.setProgressing(false);
                        return;
                    }
                    if (Encryption_Data.getFullOption() == true) {
                        if (mProperty == true) {
                            rc = mLGSDEncManager.externalSDCardFullDisableEncryption(Encryption_Data.getExternalSDCardPath());
                        } else {
                            rc = mLGSDEncManager.externalSDCardFullEnableEncryption(Encryption_Data.getExternalSDCardPath());
                        }
                        if (rc < 0) {
                            Encryption_Data.setSDEncryptionError(Encryption_Data.ERROR_MOVE_FAIL);
                            Encryption_Data.setProgressing(false);
                            return;
                        }
                    } //Full option
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Encryption_Data.setProgressing(false);
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
    };

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

    private Button.OnClickListener mBackListener = new Button.OnClickListener() {

        public void onClick(View v) {
            onBackPressed();
        }
    };
    
    public void onBackPressed() {
        if (getActivity() != null) {
            if (!Utils.supportSplitView(getActivity())) {
                getActivity().finish();
            } else {
                getActivity().onBackPressed();
            }
        }
    }

    private void initExternalSDCardPath() {
        String externalPath  = null;

        try{
            if (mLGSDEncManager != null)
                externalPath = mLGSDEncManager.getExternalSDCardMountPath();
        }catch (Exception e) {
            Log.e(TAG, "err : initExternalSDCardPath :" + e.toString());
        }

        if (externalPath == null) {
            Log.e(TAG, "err : initExternalSDCardPath :" + externalPath);
        } else {
           Encryption_Data.setExternalSDCardPath(externalPath);
        }
    }

    private void establishFinalConfirmationState() {
        mProperty = SecuritySettings.SDEncryption_checkEnableProperty();

        mFinalText = (TextView) mContentView.findViewById(R.id.encrypt_desc_confirm_Extension);
        mFinalText_askPIN = (TextView)mContentView.
                            findViewById(R.id.encrypt_desc_confirm_ask_pin_Extension);

        mApplyButton = (Button) mContentView.findViewById(R.id.sdencrypt_confirm_apply);
        mApplyButton.setOnClickListener(mApplyListener);

        mBackButton = (Button) mContentView.findViewById(R.id.sdencrypt_confirm_back);
        mBackButton.setOnClickListener(mBackListener);
        
        if (Utils.isUI_4_1_model(getActivity())) {
            mApplyButton.setText(getString(R.string.sp_security_encryption_button_label));
        }

        if (mProperty == false) { //enable
            mApplyButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mFinalText.setText(getString(R.string.sp_storage_sd_card_full_confirm_NORMAL));
            if (Config.getFWConfigBool(getActivity().getApplicationContext(),
                 com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY,
                "com.lge.config.ConfigBuildFlags.CAPP_SDENCRYPTION_USERKEY")) {
                mFinalText_askPIN.setVisibility(View.VISIBLE);
            } else {
                mFinalText_askPIN.setVisibility(View.GONE);
            }
        } else { //disable
            mApplyButton.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.GONE);
            mFinalText.setText(getString(R.string.sp_sd_card_encryption_dis_final_desc_NORMAL));
            mFinalText_askPIN.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_security);
            getActivity().setTitle(getResources().getString(R.string.sp_storage_encryption_full_title_NORMAL));
        }

        mContentView = inflater.inflate(R.layout.sd_encryption_confirm_extension, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams) mContentView.getLayoutParams()).removeBorders = true;
        }
        Log.i(TAG, "SDEncryptionConfirm_Extension::onCreateView");

        establishFinalConfirmationState();
        Encryption_Data.setSDEncryptionConfirm(false);
        Encryption_Data.setSDEncryptionSpacesError(false);

        if (LGSDEncManager.getSDEncSupportStatus(getActivity().getApplicationContext())) {
            mServiceContext = new LGContext(getActivity().getApplicationContext());
            mLGSDEncManager = (LGSDEncManager)mServiceContext.getLGSystemService(LGContext.LGSDENC_SERVICE);
            initExternalSDCardPath();
        }

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != SPACES_ERROR_CODE &&  requestCode != MOVE_ERROR_CODE) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.

        if (requestCode == SPACES_ERROR_CODE) {
            Log.i(TAG, "Encryption is failed. Please checked spaces");
            Encryption_Data.setSDEncryptionSpacesError(false);
//          getActivity().finish();
        } else if (requestCode == MOVE_ERROR_CODE) {
            Log.i(TAG, "Encryption is failed. Please checked move files");
            onBackPressed();
            Toast.makeText(getActivity(), R.string.sp_full_encryption_fail_toast_NORMAL,
            Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
           if (mProgDialog != null && mProgDialog.isShowing()) {
                mProgDialog.dismiss();
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
