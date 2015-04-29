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

import com.android.settings.Utils;
import com.android.settings.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.IBinder;
import android.widget.ListView;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.lgesetting.Config.Config;

public class SDEncryptionHelp_Extension extends SettingsPreferenceFragment {
    private static final String TAG = "SDEncryptionHelp_Extension";

    private View mContentView;
    private Button mSdcardButton;
    private ImageView mImageView;
    private ListView mheaderTitle;
    IMountService mMountService;

    private Button.OnClickListener mSdcardListener = new Button.OnClickListener() {

        public void onClick(View v) {
            Preference preference = new Preference(getActivity());
            preference.setFragment(SDEncryptionSettings_Extension.class.getName());
            preference.setTitle(R.string.sp_storage_encryption_sdcard_title_NORMAL);
            ((PreferenceActivity) getActivity()).onPreferenceStartFragment(null, preference);
            Log.i(TAG,"showFinalSdcardConfirm");
        }
    };

    private synchronized IMountService getMountService() {
       if (mMountService == null) {
           IBinder service = ServiceManager.getService("mount");
           if (service != null) {
               mMountService = IMountService.Stub.asInterface(service);
           }else {
               Log.e(TAG, "Can't get mount service");
           }
       }
       return mMountService;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        //[S][2011.12.07][jin850607.hong@lge.com][G1-D1L][TD103664] Set activity title
        if (getActivity() != null && !Utils.supportSplitView(getActivity())) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_security);
            getActivity().setTitle(getResources().getString(R.string.sp_storage_encryption_sdcard_title_NORMAL));
        }
        //[E][2011.12.07][jin850607.hong@lge.com][G1-D1L][TD103664] Set activity title


        mContentView = inflater.inflate(R.layout.sd_encryption_help_extension, null);

        mSdcardButton = (Button) mContentView.findViewById(R.id.sdcard_encrypthelp_Extension);
        mSdcardButton.setText(getString(R.string.sp_encryption_continue_NORMAL));
        mSdcardButton.setOnClickListener(mSdcardListener);
        mImageView = (ImageView)mContentView.findViewById(R.id.encryption_helpView_Extension);

        if(null != mImageView) {
            mImageView.setVisibility(View.GONE);
        }

        mheaderTitle = (ListView)mContentView.findViewById(android.R.id.list);
        if (null != mheaderTitle) {
            mheaderTitle.setVisibility(View.VISIBLE);
        }

        mMountService = getMountService();
        if (mMountService == null) {
            Log.e(TAG,"Fail get MountService");
            return mContentView;
        }

        Encryption_Data.setSDEncryptionConfirm(false);
        return mContentView;
    }

    @Override
    public void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();

        if (Encryption_Data.getSDEncryptionConfirm() == true) {
            Encryption_Data.setSDEncryptionConfirm(false);
            Log.i(TAG,"finish");
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
}
