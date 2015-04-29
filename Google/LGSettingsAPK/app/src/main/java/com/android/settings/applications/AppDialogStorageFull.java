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

package com.android.settings.applications;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;

import java.text.Format;
import java.text.Normalizer.Form;

public final class AppDialogStorageFull extends AlertActivity implements
        DialogInterface.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        if (!buildDialog()) {
            finish();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case BUTTON_POSITIVE:
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

        p.mIconAttrId = android.R.attr.alertDialogIcon;
        p.mTitle = getString(R.string.settings_apps_recover_memoryfull_title);
        p.mMessage = getString(R.string.settings_apps_recover_memoryfull);
        p.mPositiveButtonText = getString(android.R.string.ok); //getString(R.string.def_yes_btn_caption);
        //        p.mNegativeButtonText = getString(R.string.def_no_btn_caption);
        p.mPositiveButtonListener = this;

        //        p.mNegativeButtonListener = this;
        setupAlert();
        return true;
    }

}