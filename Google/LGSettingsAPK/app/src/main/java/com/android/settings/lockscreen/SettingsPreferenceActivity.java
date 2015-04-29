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

package com.android.settings.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.WindowManager;
import android.view.Display;
import com.android.settings.R;
import com.android.settings.Utils;
import android.util.Log;

public class SettingsPreferenceActivity extends PreferenceActivity {
    private int mWidthResId = -1;
    private int mHeightResId = -1;
    private boolean mDisplayAsDialog = false;
    boolean mActDisplayingDialog = false;
    private static final String  TAG = "SettingsPreferenceActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDisplayAsDialog = Utils.isTablet();
        if (!mDisplayAsDialog) {
            setTheme(com.lge.R.style.Theme_LGE_White);
        }

        super.onCreate(savedInstanceState);

        if (mDisplayAsDialog) {
            resizeActivity();
            if (savedInstanceState != null) {
                mActDisplayingDialog = savedInstanceState.getBoolean("activity_displaying_dialog",
                        false);
                Log.i(TAG, "mActDisplayingDialog .. = " + mActDisplayingDialog);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDisplayAsDialog) {
            outState.putBoolean("activity_displaying_dialog", mActDisplayingDialog);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged .. ");
        if (mDisplayAsDialog) {
            resizeActivity();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause .. ");
        changeActTrasparency(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume .. ");
        if (mActDisplayingDialog) {
            return;
        }
        changeActTrasparency(false);
    }

    public void setActivityDimention(int widthResId, int heightResId) {
        mWidthResId = widthResId;
        mHeightResId = heightResId;
    }

    private void resizeActivity() {
        int widthResId = mWidthResId;
        int heightResId = mHeightResId;
        if (mWidthResId < 0) {
            widthResId = R.dimen.lock_settings_center_dialog_default_width;
        }

        if (mHeightResId < 0) {
            heightResId = R.dimen.lock_settings_center_dialog_default_height;
        }

        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.width = getResources().getDimensionPixelSize(widthResId);
        wmlp.height = getResources().getDimensionPixelSize(heightResId);
        Log.i(TAG, "Final with and height = " + wmlp.width + "...." + wmlp.height);
        getWindow().setAttributes(wmlp);
    }

    private void changeActTrasparency(boolean isTransparent) {
        if (mDisplayAsDialog) {
            WindowManager.LayoutParams wmlp = getWindow().getAttributes();
            if (isTransparent) {
                wmlp.alpha = 0.0f;
            } else {
                wmlp.alpha = 1.0f;
            }
            getWindow().setAttributes(wmlp);
        }
    }

    public static void onShowDialog(Activity act) {
        if (act instanceof SettingsPreferenceActivity) {
            SettingsPreferenceActivity activity = (SettingsPreferenceActivity)act;
            activity.changeActTrasparency(true);
            activity.mActDisplayingDialog = true;
        }
    }

    public static void onDismissDialog(Activity act) {
        if (act instanceof SettingsPreferenceActivity) {
            SettingsPreferenceActivity activity = (SettingsPreferenceActivity)act;
            activity.changeActTrasparency(false);
            activity.mActDisplayingDialog = false;
        }
    }
}
