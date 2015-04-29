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
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ResetSetFullBackupPassword extends Activity {
    static final String TAG = "ResetSetFullBackupPassword";

    IBackupManager mBackupManager;
    TextView mCurrentPw;
    Button mCancel, mOK;

    OnClickListener mButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mOK) {
                final String curPw = mCurrentPw.getText().toString();

                // TODO: should we distinguish cases of has/hasn't set a pw before?

                if (setBackupPassword(curPw, "")) {
                    // success
                    Log.d(TAG, "password set successfully");
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    // failure -- bad existing pw, usually
                    Log.d(TAG, "failure; password mismatch?");
                    Toast.makeText(ResetSetFullBackupPassword.this,
                            R.string.local_backup_password_toast_validation_failure,
                            Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            } else if (v == mCancel) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            } else {
                Log.w(TAG, "Click on unknown view");
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));

        setContentView(R.layout.reset_set_backup_pw);

        mCurrentPw = (TextView)findViewById(R.id.current_backup_pw);

        mCancel = (Button)findViewById(R.id.backup_pw_cancel_button);
        mOK = (Button)findViewById(R.id.backup_pw_ok_button);

        mCancel.setOnClickListener(mButtonListener);
        mOK.setOnClickListener(mButtonListener);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean setBackupPassword(String currentPw, String newPw) {
        try {
            return mBackupManager.setBackupPassword(currentPw, newPw);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to communicate with backup manager");
            return false;
        }
    }
}
