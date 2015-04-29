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
 * distributed under th439e License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;

import android.util.Log;

public class AndroidBeam extends Fragment {
    private final String TAG = "NFC_SETTINGS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //[START][PORTING_FOR_NFCSETTING] 
        Activity activity = getActivity();

        if (NfcSettingAdapter.NfcUtils.isSupportedRemoteNfcSettings(activity)) {

            Log.d (TAG, "Jump to NfcSettings app.");
            NfcSettingAdapter.ExtRequestConnection.startActivityForNfcSetting(activity,
                                                NfcSettingAdapter.ExtRequestConnection.REQUEST_START_NFC_ANDROID_BEAM);
        }
        //[END][PORTING_FOR_NFCSETTING]
        activity.finish();
        return;
    }

}

