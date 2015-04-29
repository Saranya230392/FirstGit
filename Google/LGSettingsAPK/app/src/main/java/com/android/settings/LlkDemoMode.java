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

import android.app.ActionBar;
import android.app.Activity;
import android.app.backup.IBackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceActivity;
import com.android.settings.SettingsPreferenceFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class LlkDemoMode extends SettingsPreferenceFragment {
    static final String TAG = "LlkDemoMode";

    TextView mConfirmNewPw;
    Button mCancel, mSet;    
    CheckBox visible;

    OnClickListener mButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mSet) {
                final String newPw = "#VerizonDemoUnit#";
                final String confirmPw = mConfirmNewPw.getText().toString();

                if (newPw.equals(confirmPw)) {
                    startAccountSettings();
                    finish();
                } else if (!newPw.equals(confirmPw)) {
                    // Mismatch between new pw and its confirmation re-entry
                    Log.i(TAG, "password mismatch");
                    Toast.makeText(getActivity(),
                            R.string.llk_password_failure,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (v == mCancel) {
                finish();
            } else if (v == visible) {
                Log.w(TAG, "Click on visible");
                int selectionStart = mConfirmNewPw.getSelectionStart();
                int selectionEnd = mConfirmNewPw.getSelectionEnd();
                
                if (visible.isChecked()) {
                    mConfirmNewPw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mConfirmNewPw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                ((EditText)mConfirmNewPw).setSelection(selectionStart, selectionEnd);
            } else {
                Log.w(TAG, "Click on unknown view");
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.llk_demo_mode, null);
//        setContentView(R.layout.llk_demo_mode);
        mConfirmNewPw = (TextView)view.findViewById(R.id.password_entry);

        mCancel = (Button)view.findViewById(R.id.backup_pw_cancel_button);
        mSet = (Button)view.findViewById(R.id.backup_pw_set_button);
        visible = (CheckBox)view.findViewById(R.id.visible_check);

        mCancel.setOnClickListener(mButtonListener);
        mSet.setOnClickListener(mButtonListener);
        visible.setOnClickListener(mButtonListener);

        return view;
    }

    private void startAccountSettings() {
        final PreferenceActivity activity = (PreferenceActivity)getActivity();
        activity.startPreferencePanel(
        MasterClearConfirm.class.getName(), null,
        R.string.master_clear_title_ui42, null, LlkDemoMode.this,
        0);
    }    
}
