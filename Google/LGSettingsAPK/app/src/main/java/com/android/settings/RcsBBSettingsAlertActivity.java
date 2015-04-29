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
package com.android.settings;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.DialogInterface.OnClickListener;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.BroadcastReceiver;

import com.android.settings.R;

import com.android.settings.RcseSettingsInfo;

/**
 * If the attached USB accessory has a URL associated with it, and that URL is valid,
 * show this dialog to the user to allow them to optionally visit that URL for more
 * information or software downloads.
 * Otherwise (no valid URL) this activity does nothing at all, finishing immediately.
 */

/**
 * This is common dialog for application that can't make own dialogs. This
 * support for blew. 1. Title 2. Title Icon 3. Message 4. Positive Button & Text
 * 5. Negative Button & Text 6. Button Actions
 */

public class RcsBBSettingsAlertActivity extends AlertActivity implements OnClickListener {
    private static final String TAG = "RcsBBSettingsAlertActivity";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        	AlertDialog dialog = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(getText(R.string.sp_rich_communication_enable_NORMAL))
                .setPositiveButton(getText(R.string.yes), this)
                .setNegativeButton(getText(R.string.no), this)
                .setMessage(getText(R.string.rcse_multi_clent_dexcription_rich)).create();
            dialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface arg0) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "Dialog dismiss");
                    RcsBBSettingsAlertActivity.this.finish();
                }
            });
            dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            if (RcseSettingsInfo.getEnabledPackageName() != null) {
                Log.d(TAG, "intentinfo in dialog = " + RcseSettingsInfo.getEnabledPackageName());
                Intent i = new Intent(RcseSettingsInfo.getEnabledPackageName());
                startActivity(i);
            }
            Intent intent = new Intent();
            intent.setAction("com.lge.ims.rcsservice.action.READY_FOR_RCS_ACTIVATE");
            Log.d(TAG, "send broadcast com.lge.ims.rcsservice.action.READY_FOR_RCS_ACTIVATE");
            sendBroadcast(intent);
            finish();
        } else if (which == AlertDialog.BUTTON_NEGATIVE) {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        super.onDestroy();
        if (RcseSettingsInfo.checkProfile(this)) {
            RcseSettingsInfo.checkValueChangedandBroadcast(this, "service_onoff", false);
        } else {
            RcseSettingsInfo.checkValueChangedandBroadcast(this, "rcs_e_service", false);
        }
    }
}
