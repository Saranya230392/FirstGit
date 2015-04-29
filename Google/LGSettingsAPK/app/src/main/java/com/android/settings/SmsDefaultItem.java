/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.provider.Telephony.Sms.Intents;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settings.lgesetting.Config.Config;

public class SmsDefaultItem {
    public static final String TAG = "SmsDefaultItem";
    private static final String LGSMS_PACKAGENAME = "com.android.mms";
    private static final String HANGOUT_PACKAGENAME = "com.google.android.talk";
    private final Context mContext;

    public static class SmsAppInfo {
        String mAppName;
        String mAPKName;
        Drawable mAppImages;
        Boolean isDefault;
    }

    public SmsDefaultItem(Context context) {
        mContext = context;
    }

    public List<SmsAppInfo> getSmsAppInfo() {
        Log.d(TAG, "getSmsAppInfo");
        Collection<SmsApplicationData> smsApplications =
                SmsApplication.getApplicationCollection(mContext);

        // If the list is empty the dialog will be empty, but we will not crash.
        //int count = smsApplications.size();

        List<SmsAppInfo> appInfos = new ArrayList<SmsAppInfo>();

        PackageManager packageManager = mContext.getPackageManager();

        for (SmsApplicationData smsApplicationData : smsApplications) {
            SmsAppInfo appInfo = new SmsAppInfo();
            CharSequence LgSMS = smsApplicationData.mPackageName;
            Log.d(TAG, "smsApplicationData.mApplicationName111 = "
                    + smsApplicationData.mApplicationName);

            if (LgSMS.equals(LGSMS_PACKAGENAME)) {
                appInfo.isDefault = LgSMS.equals(getDefaultSMSApp(mContext)) ? true : false;
                appInfo.mAppName = smsApplicationData.mApplicationName;
                appInfo.mAPKName = smsApplicationData.mPackageName;
                try {
                    appInfo.mAppImages = packageManager
                            .getApplicationIcon(smsApplicationData.mPackageName);
                } catch (NameNotFoundException e) {
                    appInfo.mAppImages = packageManager.getDefaultActivityIcon();
                }
                appInfos.add(appInfo);
                break;
            }
        }
        for (SmsApplicationData smsApplicationData : smsApplications) {
            SmsAppInfo appInfo = new SmsAppInfo();
            CharSequence LgSMS = smsApplicationData.mPackageName;
            Log.d(TAG, "smsApplicationData.mApplicationName222 = "
                    + smsApplicationData.mApplicationName);

            if (!LgSMS.equals(LGSMS_PACKAGENAME)) {
                appInfo.mAppName = smsApplicationData.mApplicationName;
                appInfo.mAPKName = smsApplicationData.mPackageName;
                appInfo.isDefault = appInfo.mAPKName.equals(getDefaultSMSApp(mContext)) ? true
                        : false;
                try {
                    appInfo.mAppImages = packageManager
                            .getApplicationIcon(smsApplicationData.mPackageName);
                } catch (NameNotFoundException e) {
                    appInfo.mAppImages = packageManager.getDefaultActivityIcon();
                }
                if (appInfo.mAPKName.equalsIgnoreCase(HANGOUT_PACKAGENAME)) {
                    boolean voice_capable = Config.getFWConfigBool(mContext, com.android.internal.R.bool.config_voice_capable,
                        "com.android.internal.R.bool.config_voice_capable");
                    if (voice_capable == true) {
                        Log.i (TAG, "config_voice_capable is true");
                        appInfos.add(appInfo);
                    } else {
                        Log.i (TAG, "config_voice_capable is false");
                        Log.i (TAG, "Not supported Hangout");
                    }
                } else {
                    appInfos.add(appInfo);
                }
            }
        }
        return appInfos;
    }

    public String getDefaultSMSApp(Context context) {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(context, true);
        String packageName = "null";
        if (appName != null) {
            packageName = appName.getPackageName();
        }
        Log.d(TAG, "getDefaultSMSApp = " + packageName);
        return packageName;
    }

    public void setDefaultSMSApp(Context context, String packageName) {
        SmsApplication.setDefaultApplication(packageName, context);
    }

}