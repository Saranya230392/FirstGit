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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.content.BroadcastReceiver;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

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

public class GestureKnockOnAlertActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "RCSeAlertActivity";
    private static final String GESTURESETTINGS_ACTION_NAME = "com.lge.settings.GestureSettingsActivity";
    private static final String DB_TABLE_GESTURESETTING[] = { "gesture_trun_screen_on_do_not_show" };
    private static CheckBox checkbox;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");

        final AlertController.AlertParams p = mAlertParams;

        ContentResolver resolver = getContentResolver();

        Boolean checkbox_status = Settings.System.getInt(resolver,
                DB_TABLE_GESTURESETTING[0], 0) == 1 ? true : false;

        if (checkbox_status) {
            finish();
        }

        p.mIcon = getResources().getDrawable(com.lge.R.drawable.ic_dialog_alert_holo);
        p.mTitle = getString(R.string.gesture_Knock_on_guide_popup_title);
        p.mView = getCreateView();
        // p.mMessage = getString(R.string.rcse_multi_clent_dexcription);
        p.mPositiveButtonText = getString(R.string.font_size_save);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.gesture_Knock_on_guide_popup_btn);
        p.mNegativeButtonListener = this;
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        setupAlert();
        Log.d(TAG, "onCreate++++++++");

    }

    private View getCreateView() {

        View mView = getLayoutInflater()
                .inflate(R.layout.gesture_knockon, null);

        ImageView imageview = (ImageView)mView.findViewById(R.id.img_View_1);
        imageview.setImageResource(R.drawable.help_screen_on_off);

        TextView textview2 = (TextView)mView.findViewById(R.id.txt_View_2);
        textview2.setText(R.string.gesture_Knock_on_guide_popup_des);

        checkbox = (CheckBox)mView
                .findViewById(R.id.checkbox_donotshow);

        if (!"VZW".equals(Config.getOperator())) {
            checkbox.setVisibility(View.GONE);
        }

        return mView;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {

            ContentResolver resolver = getContentResolver();
            Settings.System.putInt(resolver,
                    DB_TABLE_GESTURESETTING[0], checkbox.isChecked() ? 1 : 0);

        } else if (which == AlertDialog.BUTTON_NEGATIVE) {
            Intent i = new Intent(GESTURESETTINGS_ACTION_NAME);
            startActivity(i);

        }

        finish();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy++++++++");

        super.onDestroy();

    }

}
