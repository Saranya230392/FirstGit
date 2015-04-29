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
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.settings.deviceinfo.Memory;

/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormatFragmentFinal extends SettingsPreferenceFragment {

    private LayoutInflater mInflater;

    private View mFinalView;
    private Button mFinalButton;

    private TextView mFinalTextView;
    private StorageVolume mStorageVolume;
    private boolean isSend = false;
    private SettingsBreadCrumb mBreadCrumb;
    private String mTitle;
    private static final boolean MORE_THAN_UI_4_2 = com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2;
    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Mount Service to format the SD card.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.d("JW", "mFinalClickListener");
            isSend = true;
            if (Utils.isMonkeyRunning()) {
                return;
            }
            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);

            Bundle bundle = new Bundle();
            bundle = getArguments();
            final StorageVolume storageVolume = (StorageVolume)bundle
                    .getParcelable(StorageVolume.EXTRA_STORAGE_VOLUME);
            intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, storageVolume);
            getActivity().startService(intent);
            finish();
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
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Activity activity = getActivity();
        mStorageVolume = null;

        Bundle bundle = new Bundle();
        bundle = getArguments();
        if (bundle != null) {
            Log.d("jw", "Bundle is not null");
            mStorageVolume = (StorageVolume)bundle
                    .getParcelable(StorageVolume.EXTRA_STORAGE_VOLUME);
        }

        mFinalView = null;
        mInflater = LayoutInflater.from(getActivity());

        establishFinalConfirmationState();

        if (mStorageVolume != null && mStorageVolume.getPath().
                startsWith(Memory.PATH_OF_USBSTORAGE)) {
            mTitle = Utils.getResources().getString(R.string.usbstorage_erase_button);

        } else if (mStorageVolume != null && !mStorageVolume.isRemovable()) {
            mTitle = Utils.getResources().getString(R.string.sp_erase_internal_memory_title_NORMAL);

        } else {
            mTitle = Utils.getResources().getString(R.string.media_format_button_text);
        }

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else {
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.setTitle(mTitle);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return mFinalView;
    }

    /** Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        final Activity activity = getActivity();
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isSend) {
            Intent mediaIntent = new Intent("com.android.settings.MediaFragment");
            getActivity().sendBroadcast(mediaIntent);
        }

        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            View view = activity.getActionBar().getCustomView();
            if (view != null) {
                view.destroyDrawingCache();
            }
            activity.getActionBar().setCustomView(null);
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
