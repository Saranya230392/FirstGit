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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.app.ActionBar;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;
import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.deviceinfo.Memory;
import com.lge.constants.SettingsConstants;
//2013-05-31 taesu.jung@lge.com 3LM SD Encryption Solution
import android.provider.Settings;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import com.android.internal.widget.LockPatternUtils;
import java.lang.Integer;
import android.widget.Toast;

/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormat extends Activity {

    private static final int KEYGUARD_REQUEST = 55;

    private LayoutInflater mInflater;

    private View mInitialView;
    private Button mInitiateButton;

    private View mFinalView;
    private Button mFinalButton;

    private TextView mInitialTextView;
    private TextView mFinalTextView;
    private StorageVolume mStorageVolume;
    private static final boolean MORE_THAN_UI_4_2 = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;
    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Mount Service to format the SD card.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
        public void onClick(View v) {

            if (Utils.isMonkeyRunning()) {
                return;
            }
            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            //otg_issue: for to specify whether it is 3LM encryption or user format
            intent.putExtra("3LM", getIntent().getIntExtra("3LM", -1));
            // Transfer the storage volume to the new intent
            final StorageVolume storageVolume = getIntent().getParcelableExtra(
                    StorageVolume.EXTRA_STORAGE_VOLUME);
            intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, storageVolume);
            //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[start]
            int sd_encryption = getIntent().getIntExtra("sd_encryption", -1);
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM
                    && sd_encryption != -1) {
                Settings.Global.putInt(getContentResolver(), "sd_encryption",
                        sd_encryption);
            }
            //2013-08-19 taesu.jung@lge.com 3LM SD Encryption Solution[end]
            startService(intent);
            finish();
        }
    };

    /**
     *  Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     */
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this)
                .launchConfirmationActivity(request,
                        getText(R.string.media_format_gesture_prompt),
                        getText(R.string.media_format_gesture_explanation));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            establishFinalConfirmationState();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else {
            establishInitialState();
        }
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // 3LM_MDM L taewon.jang@lge.com
            //  If we're about to encrypt, check if password quality meets our requirements.
            int sd_encryption = getIntent().getIntExtra("sd_encryption", -1);
            int quality = new LockPatternUtils(v.getContext()).getActivePasswordQuality();
            if (com.android.settings.lgesetting.Config.Config.THREELM_MDM &&
                    (sd_encryption == 1) && (quality < DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) ) {
                showPinDialog();
            }
            // 3LM_MDM L END
            else if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                establishFinalConfirmationState();
            }
        }
    };

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        if (mFinalView == null) {
            mFinalView = mInflater.inflate(R.layout.media_format_final, null);
            mFinalButton =
                    (Button)mFinalView.findViewById(R.id.execute_media_format);
            mFinalButton.setOnClickListener(mFinalClickListener);
        }
        // [USB OTG]
        if (mStorageVolume != null
                && mStorageVolume.getPath().startsWith(Memory.PATH_OF_USBSTORAGE)) {
            //mFinalButton.setText(R.string.usbstorage_erase_button);
            mFinalTextView = (TextView)mFinalView.findViewById(R.id.execute_media_format_desc);
            if (mFinalTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mFinalButton.setText(R.string.usbstorage_erase_button);
                    mFinalTextView
                            .setText(R.string.usbstorage_final_contents_ex);
                } else {
                    mFinalButton.setText(R.string.media_format_final_button_text);
                    mFinalTextView.setText(R.string.usbstorage_final_contents);
                }
            }
        } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
            Log.i("hsmodel", "mediaformat, internal");
            mFinalTextView = (TextView)mFinalView.findViewById(R.id.execute_media_format_desc);
            if (mFinalTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mFinalButton.setText(R.string.sp_erase_internal_memory_title_NORMAL);
                    mFinalTextView
                            .setText(R.string.sp_erase_internal_final_contents_NORMAL_ex);
                } else {
                    mFinalButton.setText(R.string.media_format_final_button_text);
                    mFinalTextView
                            .setText(R.string.sp_erase_internal_final_contents_NORMAL);
                }
            }
        } else {
            Log.i("hsmodel", "mediaformat, sdcard");
            mFinalTextView = (TextView)mFinalView.findViewById(R.id.execute_media_format_desc);
            if (mFinalTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mFinalButton.setText(R.string.media_format_button_text);
                    mFinalTextView
                            .setText(R.string.sp_erase_sdcard_final_contents_NORMAL_ex);
                } else {
                    mFinalButton.setText(R.string.media_format_final_button_text);
                    mFinalTextView
                            .setText(R.string.sp_erase_sdcard_final_contents_NORMAL);
                }
            }
        }

        setContentView(mFinalView);
    }

    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        if (mInitialView == null) {
            mInitialView = mInflater.inflate(R.layout.media_format_primary, null);
            mInitiateButton =
                    (Button)mInitialView.findViewById(R.id.initiate_media_format);
            mInitiateButton.setOnClickListener(mInitiateListener);
        }
        // [USB OTG]
        if (mStorageVolume != null
                && mStorageVolume.getPath().startsWith(Memory.PATH_OF_USBSTORAGE)) {
            mInitiateButton.setText(R.string.usbstorage_erase_button);
            mInitialTextView = (TextView)mInitialView.findViewById(R.id.initiate_media_format_desc);
            if (mInitialTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mInitialTextView.setText(R.string.usbstorage_init_contents_ex);
                } else {
                    mInitialTextView.setText(R.string.usbstorage_init_contents);
                }
            }
        } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
            Log.i("hsmodel", "mediaformat, internal");
            this.setTitle(R.string.sp_erase_internal_memory_title_NORMAL);
            mInitiateButton.setText(R.string.sp_erase_internal_memory_title_NORMAL);
            mInitialTextView = (TextView)mInitialView.findViewById(R.id.initiate_media_format_desc);
            if (mInitialTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mInitialTextView
                            .setText(R.string.sp_erase_internal_init_contents_NORMAL_ex);
                } else {
                    mInitialTextView
                            .setText(R.string.sp_erase_internal_init_contents_NORMAL);
                }
            }
        } else {
            Log.i("hsmodel", "mediaformat, sdcard");
            mInitiateButton.setText(R.string.media_format_button_text);
            mInitialTextView = (TextView)mInitialView.findViewById(R.id.initiate_media_format_desc);
            if (mInitialTextView != null) {
                if (MORE_THAN_UI_4_2) {
                    mInitialTextView
                            .setText(R.string.sp_erase_sdcard_init_contents_NORMAL_ex);
                } else {
                    mInitialTextView
                            .setText(R.string.sp_erase_sdcard_init_contents_NORMAL);
                }

            }
        }
        setContentView(mInitialView);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mStorageVolume = null;
        Intent intent = getIntent();
        if (intent != null) {
            mStorageVolume = (StorageVolume)intent.getExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
        }
        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(this);

        establishInitialState();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);

        if (mStorageVolume != null && mStorageVolume.getPath().
                startsWith(Memory.PATH_OF_USBSTORAGE)) {
            actionBar.setTitle(R.string.usbstorage_erase_button);

        } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
            actionBar.setTitle(R.string.sp_erase_internal_memory_title_NORMAL);

        } else {
            actionBar.setTitle(R.string.media_format_button_text);
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();

        if (!isFinishing()) {
            establishInitialState();
        }
    }

    @Override
    public void onDestroy() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "MediaFormat : mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
    // Factory reset
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (MDMSettingsAdapter.getInstance().receiveWipeDateChangeIntent(intent)) {
                    finish();
                }
            }
        }
    };

    // LGMDM_END

    // 3LM_MDM L for 3LM sd encryption
   private void showPinDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.crypt_keeper_dialog_need_password_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.crypt_keeper_dialog_need_password_message)
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .show();
    }
    // 3LM_MDM END
}
