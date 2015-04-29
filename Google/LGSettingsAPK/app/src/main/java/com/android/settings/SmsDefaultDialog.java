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
import android.content.res.Resources;
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
import com.lge.os.Build;
import com.android.settings.lgesetting.Config.Config;

import java.text.Format;
import java.text.Normalizer.Form;

public final class SmsDefaultDialog extends AlertActivity implements
        DialogInterface.OnClickListener {

    private SmsApplicationData mNewSmsApplicationData;

    SmsApplicationData oldSmsApplicationData;
    ComponentName oldSmsComponent;

    private static final String LGSMS_PACKAGENAME = "com.android.mms";
    //private static final String HANGOUT_PACKAGENAME = "com.google.android.talk";
    private static final String VZWMSG_PACKAGENAME = "com.verizon.messaging.vzmsgs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String packageName = intent.getStringExtra(Intents.EXTRA_PACKAGE_NAME);

        setResult(RESULT_CANCELED);
        if (!buildDialog(packageName)) {
            finish();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case BUTTON_POSITIVE:
            Resources r =  this.getApplicationContext().getResources();
            String defaultPackage = r.getString(com.android.internal.R.string.default_sms_application);
            
            if (oldSmsComponent.getPackageName().toString().equals(LGSMS_PACKAGENAME)
                    && !defaultPackage.equalsIgnoreCase(VZWMSG_PACKAGENAME)) {
                final Intent intent = new Intent(
                        "com.lge.settings.ACTION_CHANGE_DEFAULT_WARMING_POPUP");
                intent.putExtra(Intents.EXTRA_PACKAGE_NAME
                        , mNewSmsApplicationData.mPackageName.toString());
                startActivity(intent);
            } else {
                SmsApplication.setDefaultApplication(mNewSmsApplicationData.mPackageName, this);
                setResult(RESULT_OK);
            }
            break;
        case BUTTON_NEGATIVE:
            break;
        default:
            break;
        }
    }

    private boolean buildDialog(String packageName) {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            if(!tm.isSmsCapable()) {
                return false;
            }
        }
        mNewSmsApplicationData = SmsApplication.getSmsApplicationData(packageName, this);
        if (mNewSmsApplicationData == null) {
            return false;
        }

        oldSmsApplicationData = null;
        oldSmsComponent = SmsApplication.getDefaultSmsApplication(this, true);
        if (oldSmsComponent != null) {
            oldSmsApplicationData =
                    SmsApplication.getSmsApplicationData(oldSmsComponent.getPackageName(), this);
            if (oldSmsApplicationData.mPackageName.equals(mNewSmsApplicationData.mPackageName)) {
                return false;
            }
        }

        // Compose dialog; get
        final AlertController.AlertParams p = mAlertParams;

        p.mTitle = getString(R.string.sms_change_default_dialog_title2);
        // rebestm WBT not used
        //        String current_app = oldSmsApplicationData.mApplicationName;
        String new_app = mNewSmsApplicationData.mApplicationName;

        if (oldSmsApplicationData != null) {
            if (Utils.hasFeatureSMSsummary()) {
                p.mMessage = getString(R.string.sms_change_default_dialog_text2,
                        oldSmsApplicationData.mApplicationName,
                        mNewSmsApplicationData.mApplicationName);
            } else {
                p.mMessage = getString(R.string.sms_change_default_dialog_text2,
                        mNewSmsApplicationData.mApplicationName,
                        oldSmsApplicationData.mApplicationName);
            }
            // org
            //            p.mMessage = getString(R.string.sms_change_default_dialog_text2
            //            , oldSmsApplicationData.mApplicationName, mNewSmsApplicationData.mApplicationName);

            // test
            //            p.mMessage = getString(R.string.sms_change_default_dialog_text2,
            //            , Formatter.class.getFields().toString().equals("new_app") ? new_app : current_app
            //            , Format.class.getFields().toString().equals("current_app") ? current_app : new_app );

        } else {
            p.mMessage = getString(R.string.sms_change_default_no_previous_dialog_text2, new_app);
        }

        if ("VZW".equals(Config.getOperator())) {
            p.mPositiveButtonText = getString(R.string.def_yes_btn_caption);
            p.mNegativeButtonText = getString(R.string.def_no_btn_caption);
        } else {
            p.mPositiveButtonText = getString(android.R.string.yes);
            p.mNegativeButtonText = getString(android.R.string.no);
        }
        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;
        setupAlert();

        return true;
    }

    // rebestm - Add SMS Warning popup - not used
    //    public void SmsDefaultDialogWarning() {
    //        
    //        final Context mContext = this.getApplicationContext();
    //        Builder AlertDlg = new AlertDialog.Builder(mContext);
    //        AlertDlg.setTitle(getString(R.string.sp_dlg_note_NORMAL));
    //        AlertDlg.setMessage(getString(R.string.sms_change_default_no_previous_dialog_text));
    //        AlertDlg.setPositiveButton(getString(R.string.def_yes_btn_caption)
    //                                   , new DialogInterface.OnClickListener() {
    //           public void onClick(DialogInterface dialog, int id) {
    ////               SmsApplication.setDefaultApplication(mNewSmsApplicationData.mPackageName
    //, mContext);
    ////               setResult(RESULT_OK);
    //           }
    //       }).setNegativeButton(R.string.def_no_btn_caption, new DialogInterface.OnClickListener() {
    //           public void onClick(DialogInterface dialog, int id) {
    //               dialog.cancel();
    //               dialog.dismiss();
    //               setResult(RESULT_CANCELED);
    //               }
    //       }).setOnCancelListener(new DialogInterface.OnCancelListener() {
    //           public void onCancel(DialogInterface dialog) {
    //               setResult(RESULT_CANCELED);
    //           }
    //       }).setIconAttribute(android.R.attr.alertDialogIcon).show();
    //   }

}