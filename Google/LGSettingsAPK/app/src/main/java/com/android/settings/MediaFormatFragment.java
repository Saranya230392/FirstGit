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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.settings.lockscreen.ChooseLockSettingsHelper;
import com.android.settings.deviceinfo.Memory;

/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormatFragment extends SettingsPreferenceFragment {

    private static final int KEYGUARD_REQUEST = 55;
    private LayoutInflater mInflater;

    private View mInitialView;
    private Button mInitiateButton;

    private TextView mInitialTextView;
    private StorageVolume mStorageVolume;
    private static final int FRAGMENT_FLAG = 1;

    //jw
    private static final String MEDIA_RECEIVER = "com.android.settings.MediaFragment";
    private BroadcastReceiver mMediaFragmetReceiver;
    private SettingsBreadCrumb mBreadCrumb;
    private String mTitle;
    private static final boolean MORE_THAN_UI_4_2 = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("JW", "onResume");
        mMediaFragmetReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context mContext, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                if (MEDIA_RECEIVER.equals(action)) {
                    finish();
                }
            }

        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.settings.MediaFragment");
        getActivity().registerReceiver(mMediaFragmetReceiver, filter);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.setTitle(mTitle);
            }
        }
    }

    /**
     *  Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     */
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), MediaFormatFragment.this)
                .launchConfirmationActivity(request,
                        getText(R.string.media_format_gesture_prompt),
                        getText(R.string.media_format_gesture_explanation));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == getActivity().RESULT_OK) {

            Bundle bundle = new Bundle();
            bundle.putParcelable(StorageVolume.EXTRA_STORAGE_VOLUME, mStorageVolume);

            if (mStorageVolume != null && mStorageVolume.getPath().
                    startsWith(Memory.PATH_OF_USBSTORAGE)) {
                Log.d("yeom", "set fragment title : USB");
                startFragment(MediaFormatFragment.this,
                        MediaFormatFragmentFinal.class.getCanonicalName(),
                        FRAGMENT_FLAG, bundle, R.string.usbstorage_erase_button);

            } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
                Log.d("yeom", "set fragment title : internal memory");
                startFragment(MediaFormatFragment.this,
                        MediaFormatFragmentFinal.class.getCanonicalName(),
                        FRAGMENT_FLAG, bundle, R.string.sp_erase_internal_memory_title_NORMAL);

            } else {
                Log.d("yeom", "set fragment title : sd card");
                startFragment(MediaFormatFragment.this,
                        MediaFormatFragmentFinal.class.getCanonicalName(),
                        FRAGMENT_FLAG, bundle, R.string.media_format_button_text);
            }

        } else if (resultCode == getActivity().RESULT_CANCELED) {
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
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                //establishFinalConfirmationState();
                Bundle bundle = new Bundle();
                bundle.putParcelable(StorageVolume.EXTRA_STORAGE_VOLUME, mStorageVolume);
                if (mStorageVolume != null && mStorageVolume.getPath().
                        startsWith(Memory.PATH_OF_USBSTORAGE)) {
                    Log.d("yeom", "set fragment title : USB");
                    startFragment(MediaFormatFragment.this,
                            MediaFormatFragmentFinal.class.getCanonicalName(),
                            FRAGMENT_FLAG, bundle, R.string.usbstorage_erase_button);

                } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
                    Log.d("yeom", "set fragment title : internal memory");
                    startFragment(MediaFormatFragment.this,
                            MediaFormatFragmentFinal.class.getCanonicalName(),
                            FRAGMENT_FLAG, bundle, R.string.sp_erase_internal_memory_title_NORMAL);

                } else {
                    Log.d("yeom", "set fragment title : sd card");
                    startFragment(MediaFormatFragment.this,
                            MediaFormatFragmentFinal.class.getCanonicalName(),
                            FRAGMENT_FLAG, bundle, R.string.media_format_button_text);
                }
            }
        }
    };

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
            Log.d("yeom", "mediaformat, usb");
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
            Log.d("yeom", "mediaformat, internal");
            getActivity().setTitle(R.string.sp_erase_internal_memory_title_NORMAL);
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
            Log.d("yeom", "mediaformat, sdcard");
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

    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d("yeom", "MediaFormat Fragment");
        Activity activity = getActivity();
        mStorageVolume = null;

        Bundle bundle = new Bundle();
        bundle = getArguments();
        if (bundle != null) {
            Log.d("jw", "Bundle is not null");
            mStorageVolume = (StorageVolume)bundle
                    .getParcelable(StorageVolume.EXTRA_STORAGE_VOLUME);
        }

        mInitialView = null;
        mInflater = LayoutInflater.from(getActivity());

        establishInitialState();

        if (mStorageVolume != null && mStorageVolume.getPath().
                startsWith(Memory.PATH_OF_USBSTORAGE)) {
            mTitle = Utils.getResources().getString(R.string.usbstorage_erase_button);

        } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
            mTitle = Utils.getResources().getString(R.string.sp_erase_internal_memory_title_NORMAL);

        } else {
            mTitle = Utils.getResources().getString(R.string.media_format_button_text);
        }

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            Log.d("yeom", "SettingsBreadCrumb");
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else {
            Log.d("yeom", "not provider SettingsBreadCrumb");
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);

            if (mStorageVolume != null && mStorageVolume.getPath().
                    startsWith(Memory.PATH_OF_USBSTORAGE)) {
                Log.d("yeom", "action bar title : USB");
                activity.getActionBar().setTitle(R.string.usbstorage_erase_button);

            } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
                Log.d("yeom", "action bar title : internal memory");
                activity.getActionBar().setTitle(R.string.sp_erase_internal_memory_title_NORMAL);

            } else {
                Log.d("yeom", "action bar title : sd card");
                activity.getActionBar().setTitle(R.string.media_format_button_text);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            MDMSettingsAdapter.getInstance().addWipeDatePolicyChangeIntentFilter(filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return mInitialView;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Activity activity = getActivity();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-169]
        // Factory reset
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "MediaFormat : mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END

        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
        }
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

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mMediaFragmetReceiver);
        if (!getActivity().isFinishing()) {
            establishInitialState();
        }
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
}
