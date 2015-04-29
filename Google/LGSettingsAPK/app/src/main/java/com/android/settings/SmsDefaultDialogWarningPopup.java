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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony.Sms.Intents;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settings.R;

import java.text.Format;
import java.text.Normalizer.Form;

public final class SmsDefaultDialogWarningPopup extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "SmsDefaultDialogWarningPopup";

    String packageName;
    SmsApplicationData oldSmsApplicationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        packageName = intent.getStringExtra(Intents.EXTRA_PACKAGE_NAME);

        oldSmsApplicationData =
                SmsApplication.getSmsApplicationData(packageName, this);

        if (oldSmsApplicationData == null) {
            Log.e(TAG, "oldSmsApplicationData is null!!! return!!!");
            return;
        }

        setResult(RESULT_CANCELED);
        if (!buildDialog()) {
            finish();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case BUTTON_POSITIVE:
            SmsApplication.setDefaultApplication(packageName, this);
            setResult(RESULT_OK);
            break;
        case BUTTON_NEGATIVE:
            break;
        default:
            break;
        }
    }

    private boolean buildDialog() {
        // Compose dialog; get
        final AlertController.AlertParams p = mAlertParams;

        p.mTitle = getString(R.string.sp_dlg_note_NORMAL);
        p.mMessage = getString(R.string.sms_change_default_app_warning2
                , oldSmsApplicationData.mApplicationName);
        p.mPositiveButtonText = getString(R.string.def_yes_btn_caption);
        p.mNegativeButtonText = getString(R.string.def_no_btn_caption);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;
        setupAlert();
        return true;
    }

}