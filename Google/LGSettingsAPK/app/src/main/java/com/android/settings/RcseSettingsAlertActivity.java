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
import android.view.WindowManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.BroadcastReceiver;

import com.android.settings.R;

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

public class RcseSettingsAlertActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "RCSeAlertActivity";
    private String mTemp = null;

    private static String PREFERENCE_NAME = "gsma.joyn.preferences";
    private static String KEY_NAME = "gsma.joyn.enabled";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");

        final AlertController.AlertParams p = mAlertParams;
        // p.mOnClickListener = mQmodeClickListener;
        p.mPositiveButtonText = getString(R.string.yes);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.no);
        p.mNegativeButtonListener = this;
        p.mMessage = getString(R.string.rcse_multi_clent_dexcription);
        p.mIcon = getResources().getDrawable(com.lge.R.drawable.ic_dialog_alert_holo);
        p.mTitle = getString(R.string.sp_rcs_e_service_NORMAL);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        setupAlert();
        Log.d(TAG, "onCreate++++++++");

    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {

            checkMultiClient();
            try {
                Log.d(TAG, "afercheckintentinfo = " + mTemp);
                Intent intent = new Intent(mTemp);
                startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, "There is no RCS app except LG RCS app");
            }
        } else if (which == AlertDialog.BUTTON_NEGATIVE) {

        }

        finish();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy++++++++");

        super.onDestroy();

    }

    private boolean getJoynEnabled(String packageName) {
        boolean result = false;
        String filePath = null;
        try {
            filePath = getPackageManager().getPackageInfo(packageName, 0).applicationInfo.dataDir;
            filePath += "/shared_prefs/";
            File file = new File(filePath, PREFERENCE_NAME + ".xml");
            if (file == null || file.exists() == false) {
                Log.d(TAG, "filePath:" + filePath + " Not exists");
                return false;
            }

            XmlPullParserFactory parserCreator = XmlPullParserFactory
                    .newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            FileReader fr = new FileReader(file);
            parser.setInput(fr);
            int parseEvent = parser.getEventType();
            while (parseEvent != XmlPullParser.END_DOCUMENT) {
                if (parseEvent == XmlPullParser.START_TAG) {
                    String name = parser.getAttributeValue(null, "name");
                    if (KEY_NAME.equals(name)) {
                        String value = parser.getAttributeValue(null, "value");
                        if ("true".equals(value)) {
                            result = true;
                            break;
                        }
                    }
                }
                parseEvent = parser.next();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void checkMultiClient() {

        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> infos = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : infos) {
            if (appInfo.metaData == null) {
                continue;
            }
            if (appInfo.metaData.containsKey("gsma.joyn.client")) {
                Log.d(TAG, "joyn package : " + appInfo.packageName);
                if (appInfo.packageName.equals(getBaseContext()
                        .getApplicationInfo().packageName)) {
                    continue;
                }
                boolean enabled = getJoynEnabled(appInfo.packageName);

                Log.d(TAG, "joyn package : " + appInfo.packageName
                        + " enabled=" + enabled);

                if (appInfo.metaData.containsKey("gsma.joyn.settings.activity")) {

                    mTemp = appInfo.metaData
                            .getString("gsma.joyn.settings.activity");
                    Log.d(TAG, "intentinfo = " + mTemp);
                }
                if (enabled) {
                    break;
                } else {
                    // do nothing
                }
            }
        }
    }
}
